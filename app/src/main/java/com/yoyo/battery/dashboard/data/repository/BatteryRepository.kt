package com.yoyo.battery.dashboard.data.repository

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import com.yoyo.battery.dashboard.data.model.BatteryInfo
import com.yoyo.battery.dashboard.data.model.BatteryStatus
import com.yoyo.battery.dashboard.data.model.HealthStatus
import com.yoyo.battery.dashboard.data.model.PluggedSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File
import kotlin.math.abs

class BatteryRepository private constructor(private val context: Context) {

    companion object {
        @Volatile
        private var instance: BatteryRepository? = null

        fun getInstance(context: Context): BatteryRepository {
            return instance ?: synchronized(this) {
                instance ?: BatteryRepository(context.applicationContext).also { instance = it }
            }
        }
    }

    private val batteryManager: BatteryManager by lazy {
        context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
    }

    private val cachedDesignCapacityMah: Int by lazy {
        fetchDesignCapacityMah()
    }

    private var smoothedCurrentMa: Double = 0.0
    private var smoothedVoltageMv: Double = 0.0

    fun getBatteryInfoFlow(pollIntervalMs: Long = 1000L): Flow<BatteryInfo> = flow {
        while (true) {
            val info = fetchCurrentBatteryInfo()
            emit(info)
            delay(pollIntervalMs)
        }
    }

    fun fetchCurrentBatteryInfo(): BatteryInfo {
        val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryIntent: Intent? = context.registerReceiver(null, intentFilter)

        val rawLevel = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
        val level = if (rawLevel >= 0 && scale > 0) {
            ((rawLevel.toFloat() / scale.toFloat()) * 100f).toInt()
        } else {
            batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        }

        val statusExtra = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val batteryStatus = BatteryStatus.fromExtra(statusExtra)

        val pluggedExtra = batteryIntent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: -1
        val pluggedSource = PluggedSource.fromExtra(pluggedExtra)

        val healthExtra = batteryIntent?.getIntExtra(BatteryManager.EXTRA_HEALTH, -1) ?: -1
        val healthStatus = HealthStatus.fromExtra(healthExtra)

        val rawVoltageMv = batteryIntent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) ?: 0
        val tempTenths = batteryIntent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
        val technology = batteryIntent?.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Li-ion"
        val isPresent = batteryIntent?.getBooleanExtra(BatteryManager.EXTRA_PRESENT, true) ?: true

        val cycleCount: Int? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val countFromIntent = batteryIntent?.getIntExtra(BatteryManager.EXTRA_CYCLE_COUNT, -1) ?: -1
            if (countFromIntent != -1) countFromIntent else null
        } else {
            readCycleCountFromSys()
        }

        val rawCurrentNow = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
        val rawCurrentAvg = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_AVERAGE)
        val chargeCounterUah = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)

        val sanitizedCurrentMa = sanitizeCurrentToMa(rawCurrentNow, batteryStatus, pluggedSource)
        val sanitizedCurrentAvgMa = sanitizeCurrentToMa(rawCurrentAvg, batteryStatus, pluggedSource)

        // Stronger Exponential Moving Average (EMA) smoothing for rock-stable live metrics and time remaining
        val currentNowMa = smoothCurrent(sanitizedCurrentMa)
        val voltageMv = smoothVoltage(rawVoltageMv)

        val estimatedCapacityMah = calculateEstimatedTotalCapacity(chargeCounterUah, level)

        val designCapacity = if (cachedDesignCapacityMah > 0) {
            cachedDesignCapacityMah
        } else {
            if (estimatedCapacityMah > 0) estimatedCapacityMah else 4000
        }

        val maxCurrentFromIntent = batteryIntent?.getIntExtra("max_charging_current", 0) ?: 0
        val maxVoltageFromIntent = batteryIntent?.getIntExtra("max_charging_voltage", 0) ?: 0

        val hwChargerType = readHardwareChargerType()
        val sysMaxCurrent = if (maxCurrentFromIntent <= 0) readMaxCurrentFromSys() else 0
        val sysMaxVoltage = if (maxVoltageFromIntent <= 0) readMaxVoltageFromSys() else 0
        val resolvedMaxCurrent = if (maxCurrentFromIntent > 0) maxCurrentFromIntent else sysMaxCurrent
        val resolvedMaxVoltage = if (maxVoltageFromIntent > 0) {
            maxVoltageFromIntent
        } else if (sysMaxVoltage > 0) {
            sysMaxVoltage
        } else if (resolvedMaxCurrent > 0) {
            // Standard USB baseline voltage if current is known
            5_000_000
        } else {
            0
        }

        val chargerProtocol = detectChargerProtocol(pluggedSource, hwChargerType)

        return BatteryInfo(
            level = level,
            status = batteryStatus,
            pluggedSource = pluggedSource,
            healthStatus = healthStatus,
            voltageMv = voltageMv,
            temperatureTenthsCelsius = tempTenths,
            technology = if (technology.isNotBlank()) technology else "Li-ion",
            currentNowMa = currentNowMa,
            currentAvgMa = sanitizedCurrentAvgMa,
            chargeCounterUah = chargeCounterUah,
            designCapacityMah = designCapacity,
            estimatedCapacityMah = estimatedCapacityMah,
            cycleCount = cycleCount,
            isPresent = isPresent,
            maxChargingCurrentMicroAmps = resolvedMaxCurrent,
            maxChargingVoltageMicroVolts = resolvedMaxVoltage,
            chargerProtocol = chargerProtocol ?: "Power Adapter",
            hardwareChargerType = hwChargerType ?: ""
        )
    }

    private fun detectChargerProtocol(pluggedSource: PluggedSource, hwType: String?): String? {
        if (pluggedSource == PluggedSource.UNPLUGGED) return null
        val typeUpper = hwType?.uppercase() ?: ""
        return when {
            typeUpper.contains("PD") || typeUpper.contains("POWER_DELIVERY") -> "USB Power Delivery (PD)"
            typeUpper.contains("QC") || typeUpper.contains("HVDCP") -> "Qualcomm Quick Charge"
            typeUpper.contains("VOOC") || typeUpper.contains("WARP") || typeUpper.contains("DASH") -> "SuperVOOC / Warp Charge"
            typeUpper.contains("TURBO") || typeUpper.contains("HYPER") -> "Turbo / HyperCharge"
            typeUpper.contains("PUMP") -> "MediaTek Pump Express"
            typeUpper.contains("DCP") -> "Dedicated Fast Charger (DCP)"
            typeUpper.contains("CDP") -> "Charging Downstream Port (CDP)"
            typeUpper.contains("SDP") -> "Standard USB Port (SDP)"
            pluggedSource == PluggedSource.WIRELESS -> "Qi Wireless Inductive"
            pluggedSource == PluggedSource.AC -> "AC Fast Adapter"
            pluggedSource == PluggedSource.USB -> "USB Cable / Hub"
            pluggedSource == PluggedSource.DOCK -> "Smart Charging Dock"
            else -> "Power Adapter"
        }
    }

    private fun readHardwareChargerType(): String? {
        val paths = listOf(
            "/sys/class/power_supply/usb/type",
            "/sys/class/power_supply/usb/real_type",
            "/sys/class/power_supply/battery/charge_type",
            "/sys/class/power_supply/main/type"
        )
        for (path in paths) {
            try {
                val file = File(path)
                if (file.exists() && file.canRead()) {
                    val text = file.bufferedReader().use { it.readLine()?.trim() }
                    if (!text.isNullOrBlank() && !text.equals("unknown", ignoreCase = true)) {
                        return text
                    }
                }
            } catch (_: Exception) {}
        }
        return null
    }

    private fun readMaxCurrentFromSys(): Int {
        val paths = listOf(
            "/sys/class/power_supply/usb/current_max",
            "/sys/class/power_supply/main/current_max",
            "/sys/class/power_supply/battery/constant_charge_current_max"
        )
        for (path in paths) {
            try {
                val file = File(path)
                if (file.exists() && file.canRead()) {
                    val text = file.bufferedReader().use { it.readLine()?.trim()?.toIntOrNull() }
                    if (text != null && text > 0) return text
                }
            } catch (_: Exception) {}
        }
        return 0
    }

    private fun readMaxVoltageFromSys(): Int {
        val paths = listOf(
            "/sys/class/power_supply/usb/voltage_max",
            "/sys/class/power_supply/main/voltage_max",
            "/sys/class/power_supply/charger/voltage_max",
            "/sys/class/power_supply/battery/voltage_max"
        )
        for (path in paths) {
            try {
                val file = File(path)
                if (file.exists() && file.canRead()) {
                    val text = file.bufferedReader().use { it.readLine()?.trim()?.toIntOrNull() }
                    if (text != null && text > 0) return text
                }
            } catch (_: Exception) {}
        }
        return 0
    }

    private fun smoothCurrent(rawMa: Int): Int {
        if (rawMa == 0) return 0
        if (smoothedCurrentMa == 0.0 || (rawMa > 0 && smoothedCurrentMa < 0.0) || (rawMa < 0 && smoothedCurrentMa > 0.0)) {
            // Instantly snap on charger connect/disconnect rather than slowly drifting across zero
            smoothedCurrentMa = rawMa.toDouble()
        } else {
            val alpha = 0.12 // Smooth yet responsive EMA filter
            smoothedCurrentMa = (alpha * rawMa) + ((1.0 - alpha) * smoothedCurrentMa)
        }
        return smoothedCurrentMa.toInt()
    }

    private fun smoothVoltage(rawMv: Int): Int {
        if (rawMv <= 0) return 0
        if (smoothedVoltageMv == 0.0) {
            smoothedVoltageMv = rawMv.toDouble()
        } else {
            val alpha = 0.1
            smoothedVoltageMv = (alpha * rawMv) + ((1.0 - alpha) * smoothedVoltageMv)
        }
        return smoothedVoltageMv.toInt()
    }

    private fun sanitizeCurrentToMa(rawCurrentMicro: Int, status: BatteryStatus, pluggedSource: PluggedSource): Int {
        if (rawCurrentMicro == Int.MIN_VALUE || rawCurrentMicro == 0) return 0
        val absVal = abs(rawCurrentMicro)

        // Convert µA to mA if value is in microamperes
        val currentMa = if (absVal > 10000) {
            rawCurrentMicro / 1000
        } else {
            rawCurrentMicro
        }

        val absMa = abs(currentMa)

        return when {
            pluggedSource == PluggedSource.UNPLUGGED -> -absMa
            status == BatteryStatus.CHARGING -> absMa
            status == BatteryStatus.DISCHARGING || status == BatteryStatus.NOT_CHARGING -> -absMa
            status == BatteryStatus.FULL -> currentMa.coerceIn(-200, 200)
            else -> if (currentMa != 0) currentMa else -absMa
        }
    }

    private fun calculateEstimatedTotalCapacity(chargeCounterUah: Int, level: Int): Int {
        if (chargeCounterUah <= 0 || level <= 0) return 0
        val chargeCounterMah = abs(chargeCounterUah) / 1000.0
        val totalCapacity = chargeCounterMah / (level / 100.0)
        return totalCapacity.toInt()
    }

    private fun fetchDesignCapacityMah(): Int {
        try {
            val powerProfileClass = Class.forName("com.android.internal.os.PowerProfile")
            val powerProfile = powerProfileClass.getConstructor(Context::class.java).newInstance(context)

            // 1. Try getBatteryCapacity()
            try {
                val m = powerProfileClass.getMethod("getBatteryCapacity")
                val cap = m.invoke(powerProfile) as? Double
                if (cap != null && cap > 500.0) return cap.toInt()
            } catch (_: Exception) {}

            // 2. Try getAveragePower("battery.capacity")
            try {
                val m = powerProfileClass.getMethod("getAveragePower", String::class.java)
                val cap = m.invoke(powerProfile, "battery.capacity") as? Double
                if (cap != null && cap > 500.0) return cap.toInt()
            } catch (_: Exception) {}
        } catch (_: Exception) {}

        val sysfsPaths = listOf(
            "/sys/class/power_supply/battery/charge_full_design",
            "/sys/class/power_supply/bms/charge_full_design",
            "/sys/class/power_supply/battery/energy_full_design",
            "/sys/class/power_supply/battery/charge_full",
            "/sys/class/power_supply/battery/capacity_design",
            "/sys/class/power_supply/qcom-battery/charge_full_design",
            "/sys/class/power_supply/bms/charge_full"
        )

        for (path in sysfsPaths) {
            try {
                val file = File(path)
                if (file.exists() && file.canRead()) {
                    val valInt = file.bufferedReader().use { it.readLine()?.trim()?.toIntOrNull() }
                    if (valInt != null && valInt > 0) {
                        val mah = if (valInt > 10000) valInt / 1000 else valInt
                        if (mah in 500..25000) return mah
                    }
                }
            } catch (_: Exception) {}
        }

        return 5000
    }

    private fun readCycleCountFromSys(): Int? {
        val sysfsPaths = listOf(
            "/sys/class/power_supply/battery/cycle_count",
            "/sys/class/power_supply/bms/cycle_count"
        )
        for (path in sysfsPaths) {
            try {
                val file = File(path)
                if (file.exists() && file.canRead()) {
                    val valInt = file.bufferedReader().use { it.readLine()?.trim()?.toIntOrNull() }
                    if (valInt != null && valInt >= 0) return valInt
                }
            } catch (_: Exception) {}
        }
        return null
    }
}
