package com.yoyo.battery.dashboard.data.model

import android.os.BatteryManager

enum class BatteryStatus {
    CHARGING,
    DISCHARGING,
    FULL,
    NOT_CHARGING,
    UNKNOWN;

    companion object {
        fun fromExtra(extra: Int): BatteryStatus = when (extra) {
            BatteryManager.BATTERY_STATUS_CHARGING -> CHARGING
            BatteryManager.BATTERY_STATUS_DISCHARGING -> DISCHARGING
            BatteryManager.BATTERY_STATUS_FULL -> FULL
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> NOT_CHARGING
            else -> UNKNOWN
        }
    }
}

enum class PluggedSource {
    AC,
    USB,
    WIRELESS,
    DOCK,
    UNPLUGGED;

    companion object {
        fun fromExtra(extra: Int): PluggedSource = when {
            (extra and BatteryManager.BATTERY_PLUGGED_AC) != 0 -> AC
            (extra and BatteryManager.BATTERY_PLUGGED_USB) != 0 -> USB
            (extra and BatteryManager.BATTERY_PLUGGED_WIRELESS) != 0 -> WIRELESS
            (extra and 8) != 0 -> DOCK
            else -> UNPLUGGED
        }
    }
}

enum class HealthStatus {
    GOOD,
    OVERHEAT,
    DEAD,
    OVER_VOLTAGE,
    UNSPECIFIED_FAILURE,
    COLD,
    UNKNOWN;

    companion object {
        fun fromExtra(extra: Int): HealthStatus = when (extra) {
            BatteryManager.BATTERY_HEALTH_GOOD -> GOOD
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> OVERHEAT
            BatteryManager.BATTERY_HEALTH_DEAD -> DEAD
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> OVER_VOLTAGE
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> UNSPECIFIED_FAILURE
            BatteryManager.BATTERY_HEALTH_COLD -> COLD
            else -> UNKNOWN
        }
    }

    fun toLabel(): String = when (this) {
        GOOD -> "Excellent"
        OVERHEAT -> "Overheating"
        DEAD -> "Dead"
        OVER_VOLTAGE -> "Over Voltage"
        UNSPECIFIED_FAILURE -> "Failure"
        COLD -> "Cold"
        UNKNOWN -> "Unknown"
    }
}

enum class ChargingSpeedCategory(val label: String, val badgeColorHex: Long) {
    SLOW("Slow Charge (< 7.5W)", 0xFFFFB74D),              // Amber (< 7.5 W)
    NORMAL("Standard Charge (7.5-15W)", 0xFF38BDF8),       // Sky Blue (7.5 W - 15 W)
    FAST("Fast Charge (15-30W)", 0xFF10B981),              // Emerald Green (15 W - 30 W)
    SUPER_FAST("Super Fast (30-65W)", 0xFFA855F7),         // Electric Violet (30 W - 65 W)
    HYPER_CHARGE("HyperCharge (65W+)", 0xFFFF3B30),        // Neon Red/Orange (> 65 W)
    ULTRA_FAST("Ultra-Fast Charge", 0xFFA855F7),           // Alias for backward compatibility
    DISCHARGING("Discharging", 0xFF94A3B8);

    companion object {
        fun fromWatts(watts: Double, isCharging: Boolean): ChargingSpeedCategory {
            if (!isCharging || watts <= 0.5) return DISCHARGING
            return when {
                watts >= 65.0 -> HYPER_CHARGE
                watts >= 30.0 -> SUPER_FAST
                watts >= 15.0 -> FAST
                watts >= 7.5 -> NORMAL
                else -> SLOW
            }
        }
    }
}

enum class TemperatureState(
    val label: String,
    val containerDarkHex: Long,
    val contentDarkHex: Long,
    val accentDarkHex: Long,
    val containerLightHex: Long,
    val contentLightHex: Long,
    val accentLightHex: Long
) {
    COLD(
        label = "Cool / Cold",
        containerDarkHex = 0xFF0C2136,
        contentDarkHex = 0xFF90CAF9,
        accentDarkHex = 0xFF2979FF,
        containerLightHex = 0xFFE0F2FE,
        contentLightHex = 0xFF0369A1,
        accentLightHex = 0xFF0284C7
    ),
    NORMAL(
        label = "Optimal Temp",
        containerDarkHex = 0xFF0D2818,
        contentDarkHex = 0xFFB9F6CA,
        accentDarkHex = 0xFF00E676,
        containerLightHex = 0xFFECFDF5,
        contentLightHex = 0xFF047857,
        accentLightHex = 0xFF059669
    ),
    HOT(
        label = "Warm / Hot",
        containerDarkHex = 0xFF2C0B12,
        contentDarkHex = 0xFFFFCDD2,
        accentDarkHex = 0xFFFF1744,
        containerLightHex = 0xFFFEF2F2,
        contentLightHex = 0xFFB91C1C,
        accentLightHex = 0xFFDC2626
    );

    companion object {
        fun fromCelsius(tempC: Float): TemperatureState = when {
            tempC < 25.0f -> COLD
            tempC <= 38.0f -> NORMAL
            else -> HOT
        }
    }
}

data class BatteryInfo(
    val level: Int = 0,
    val status: BatteryStatus = BatteryStatus.UNKNOWN,
    val pluggedSource: PluggedSource = PluggedSource.UNPLUGGED,
    val healthStatus: HealthStatus = HealthStatus.UNKNOWN,
    val voltageMv: Int = 0,
    val temperatureTenthsCelsius: Int = 0,
    val technology: String = "Li-ion",
    val currentNowMa: Int = 0,
    val currentAvgMa: Int = 0,
    val chargeCounterUah: Int = 0,
    val designCapacityMah: Int = 0,
    val estimatedCapacityMah: Int = 0,
    val cycleCount: Int? = null,
    val isPresent: Boolean = true,
    val maxChargingCurrentMicroAmps: Int = 0,
    val maxChargingVoltageMicroVolts: Int = 0,
    val chargerProtocol: String = "Unknown",
    val hardwareChargerType: String = ""
) {
    val voltageVolts: Double
        get() = voltageMv / 1000.0

    val tempCelsius: Float
        get() = temperatureTenthsCelsius / 10.0f

    val tempFahrenheit: Float
        get() = (tempCelsius * 9f / 5f) + 32f

    val isCharging: Boolean
        get() = status == BatteryStatus.CHARGING || 
                status == BatteryStatus.FULL || 
                (pluggedSource != PluggedSource.UNPLUGGED && status != BatteryStatus.DISCHARGING && currentNowMa > 0)

    val powerWatts: Double
        get() = voltageVolts * (kotlin.math.abs(currentNowMa) / 1000.0)

    val maxChargerWatts: Double
        get() {
            if (maxChargingCurrentMicroAmps > 0 && maxChargingVoltageMicroVolts > 0) {
                val a = maxChargingCurrentMicroAmps / 1_000_000.0
                val v = maxChargingVoltageMicroVolts / 1_000_000.0
                val w = a * v
                if (w in 2.0..240.0) return w
            }
            if (isCharging && powerWatts > 1.0) {
                return when {
                    powerWatts >= 60.0 -> 67.0
                    powerWatts >= 40.0 -> 45.0
                    powerWatts >= 28.0 -> 33.0
                    powerWatts >= 20.0 -> 25.0
                    powerWatts >= 13.0 -> 18.0
                    powerWatts >= 8.0 -> 10.0
                    else -> 5.0
                }
            }
            return 0.0
        }

    val chargerCapacity: String
        get() {
            if (!isCharging && pluggedSource == PluggedSource.UNPLUGGED) return "Disconnected"

            // 1. If explicit voltage & current are negotiated via USB-PD / sysfs
            if (maxChargingVoltageMicroVolts > 0 && maxChargingCurrentMicroAmps > 0) {
                val v = maxChargingVoltageMicroVolts / 1_000_000.0
                val a = maxChargingCurrentMicroAmps / 1_000_000.0
                val vStr = if (v % 1.0 == 0.0) "${v.toInt()}V" else String.format(java.util.Locale.US, "%.1fV", v)
                val aStr = if (a % 1.0 == 0.0) "${a.toInt()}A" else String.format(java.util.Locale.US, "%.1fA", a)
                val w = (v * a).toInt()
                return "$vStr ${aStr} (${w}W)"
            }

            // 2. Real-time inferred capacity from power & current telemetry
            val curMaAbs = kotlin.math.abs(currentNowMa)
            val watts = if (maxChargerWatts > powerWatts) maxChargerWatts else powerWatts

            return when {
                watts >= 100.0 -> "20V 6A (120W)"
                watts >= 75.0 -> "20V 5A (100W)"
                watts >= 58.0 -> "10V 6.5A (65W)"
                watts >= 40.0 -> "10V 4.5A (45W)"
                watts >= 28.0 -> "11V 3A (33W)"
                watts >= 22.0 -> "10V 2.5A (25W)"
                watts >= 16.0 -> "9V 2A (18W)"
                watts >= 13.0 -> "5V 3A (15W)"
                watts >= 10.5 -> "4V 3A (12W)"
                watts >= 8.5 -> "5V 2A (10W)"
                watts >= 6.0 -> "5V 1.5A (7.5W)"
                pluggedSource == PluggedSource.WIRELESS -> if (watts >= 10.0) "9V 1.67A (15W Wireless)" else "5V 1A (5W Wireless)"
                pluggedSource == PluggedSource.USB -> "5V 1A / 500mA"
                curMaAbs > 2500 -> "11V 3A (33W)"
                curMaAbs > 2000 -> "4V 3A (12W)"
                curMaAbs > 1600 -> "5V 2A (10W)"
                curMaAbs > 1000 -> "5V 1.5A (7.5W)"
                curMaAbs > 0 -> "5V 1A (5W)"
                else -> "5V 2A (10W)"
            }
        }

    val chargerRatingLabel: String
        get() {
            if (!isCharging && pluggedSource == PluggedSource.UNPLUGGED) return "Disconnected"
            val watts = maxChargerWatts
            val protocolSuffix = if (chargerProtocol.isNotBlank() && chargerProtocol != "Unknown") " · $chargerProtocol" else ""
            return when {
                watts >= 100.0 -> String.format(java.util.Locale.US, "%.0fW Ultra HyperCharge%s", watts, protocolSuffix)
                watts >= 60.0 -> String.format(java.util.Locale.US, "%.0fW Super Fast Charge%s", watts, protocolSuffix)
                watts >= 28.0 -> String.format(java.util.Locale.US, "%.0fW Turbo Fast Charge%s", watts, protocolSuffix)
                watts >= 15.0 -> String.format(java.util.Locale.US, "%.0fW Fast Charge%s", watts, protocolSuffix)
                watts >= 7.5 -> String.format(java.util.Locale.US, "%.0fW Standard Charge%s", watts, protocolSuffix)
                watts > 0.0 -> String.format(java.util.Locale.US, "%.0fW Basic USB%s", watts, protocolSuffix)
                else -> when (pluggedSource) {
                    PluggedSource.AC -> "AC Wall Adapter"
                    PluggedSource.USB -> "USB Port"
                    PluggedSource.WIRELESS -> "Wireless Charging Pad"
                    PluggedSource.DOCK -> "Dock"
                    PluggedSource.UNPLUGGED -> "Discharging"
                }
            }
        }

    val chargingSpeedCategory: ChargingSpeedCategory
        get() {
            val effWatts = if (maxChargerWatts > powerWatts) maxChargerWatts else powerWatts
            return ChargingSpeedCategory.fromWatts(effWatts, isCharging)
        }

    val chargingSpeedDescription: String
        get() {
            if (!isCharging) return "Discharging"
            val effWatts = maxOf(maxChargerWatts, powerWatts)
            val currentAmps = kotlin.math.abs(currentNowMa) / 1000.0
            return when {
                effWatts >= 15.0 || currentAmps >= 2.5 -> "Charging Rapidly"
                effWatts >= 7.5 || currentAmps >= 1.2 -> "Charging Normally"
                effWatts > 0.0 || currentAmps > 0.0 -> "Charging Slowly"
                else -> "Charging Normally"
            }
        }

    val temperatureState: TemperatureState
        get() = TemperatureState.fromCelsius(tempCelsius)

    val healthPercentage: Int
        get() {
            if (designCapacityMah > 0 && estimatedCapacityMah > 0) {
                val ratio = (estimatedCapacityMah.toDouble() / designCapacityMah.toDouble()) * 100.0
                return ratio.coerceIn(1.0, 100.0).toInt()
            }
            return if (healthStatus == HealthStatus.GOOD) 100 else 80
        }

    val timeRemainingFormatted: String
        get() {
            val currentAbsMa = kotlin.math.abs(currentNowMa)
            if (currentAbsMa < 50 || designCapacityMah <= 0) return "Calculating time..."

            return if (isCharging) {
                val remainingMah = (designCapacityMah * (100 - level) / 100.0).coerceAtLeast(1.0)
                val totalMins = ((remainingMah / currentAbsMa.toDouble()) * 60).toInt()
                if (totalMins <= 0 || totalMins > 1440) return "Calculating time..."
                val hours = totalMins / 60
                val mins = totalMins % 60
                if (hours > 0) "⏱ ~$hours hr $mins mins until 100%" else "⏱ ~$mins mins until 100%"
            } else {
                val availableMah = (designCapacityMah * level / 100.0).coerceAtLeast(1.0)
                val totalMins = ((availableMah / currentAbsMa.toDouble()) * 60).toInt()
                if (totalMins <= 0 || totalMins > 2880) return "Calculating time..."
                val hours = totalMins / 60
                val mins = totalMins % 60
                if (hours > 0) "⏱ ~$hours hrs $mins mins remaining" else "⏱ ~$mins mins remaining"
            }
        }
}
