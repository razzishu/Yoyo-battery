package com.yoyo.battery.dashboard.data.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.yoyo.battery.dashboard.MainActivity
import com.yoyo.battery.dashboard.data.model.BatteryInfo
import com.yoyo.battery.dashboard.data.repository.BatteryDrainTracker
import com.yoyo.battery.dashboard.data.repository.BatteryRepository
import com.yoyo.battery.dashboard.data.repository.ChargingSessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import com.yoyo.battery.dashboard.ui.widget.BatteryAppWidgetProvider
import com.yoyo.battery.dashboard.R
import java.util.Calendar
import java.util.Locale

class BatteryMonitoringService : Service() {

    companion object {
        private const val CHANNEL_ID = "yoyo_battery_service_channel"
        private const val ALARM_CHANNEL_ID = "yoyo_battery_alarm_channel"
        private const val NOTIFICATION_ID = 1001
        private const val ALARM_NOTIFICATION_ID = 1002
        private const val OVERHEAT_NOTIFICATION_ID = 1003

        fun startService(context: Context) {
            try {
                val intent = Intent(context, BatteryMonitoringService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun stopService(context: Context) {
            try {
                val intent = Intent(context, BatteryMonitoringService::class.java)
                context.stopService(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Default + serviceJob)

    private lateinit var batteryRepository: BatteryRepository
    private lateinit var sessionManager: ChargingSessionManager
    private lateinit var drainTracker: BatteryDrainTracker

    private var lastLevel: Int = -1
    private var lastChargingState: Boolean? = null
    private var lastNotificationTimeMs: Long = 0L
    private var hasAlerted80Percent: Boolean = false
    private var hasAlerted20Percent: Boolean = false
    private var hasAlertedOverheat: Boolean = false

    private val batteryReceiver = object : android.content.BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            serviceScope.launch {
                try {
                    val info = batteryRepository.fetchCurrentBatteryInfo()
                    sessionManager.processBatteryUpdate(info)
                    drainTracker.processBatteryUpdate(info)
                    checkBatteryAlarms(info)
                    updateNotification(info)
                    lastLevel = info.level
                    lastChargingState = info.isCharging
                    lastNotificationTimeMs = System.currentTimeMillis()
                    try {
                        BatteryAppWidgetProvider.updateAllWidgets(applicationContext)
                    } catch (_: Exception) {}
                } catch (_: Exception) {}
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        batteryRepository = BatteryRepository.getInstance(applicationContext)
        sessionManager = ChargingSessionManager.getInstance(applicationContext)
        drainTracker = BatteryDrainTracker.getInstance(applicationContext)

        createNotificationChannels()
        val initialInfo = batteryRepository.fetchCurrentBatteryInfo()
        val initialNotification = buildNotification(initialInfo)

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ServiceCompat.startForeground(
                    this,
                    NOTIFICATION_ID,
                    initialNotification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                )
            } else {
                startForeground(NOTIFICATION_ID, initialNotification)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            val filter = android.content.IntentFilter().apply {
                addAction(Intent.ACTION_POWER_CONNECTED)
                addAction(Intent.ACTION_POWER_DISCONNECTED)
                addAction(Intent.ACTION_BATTERY_CHANGED)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(batteryReceiver, filter, Context.RECEIVER_EXPORTED)
            } else {
                registerReceiver(batteryReceiver, filter)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        startMonitoringLoop()
    }

    private fun startMonitoringLoop() {
        serviceScope.launch {
            while (isActive) {
                val info = batteryRepository.fetchCurrentBatteryInfo()
                sessionManager.processBatteryUpdate(info)
                drainTracker.processBatteryUpdate(info)

                val now = System.currentTimeMillis()
                val isCharging = info.isCharging
                val level = info.level

                // Check battery alarms
                checkBatteryAlarms(info)

                val statusChanged = (isCharging != lastChargingState) || (level != lastLevel)
                val throttleElapsed = (now - lastNotificationTimeMs) >= 10000L // 10 seconds throttle

                if (statusChanged || throttleElapsed) {
                    updateNotification(info)
                    lastLevel = level
                    lastChargingState = isCharging
                    lastNotificationTimeMs = now

                    try {
                        BatteryAppWidgetProvider.updateAllWidgets(applicationContext)
                    } catch (_: Exception) {}
                }

                // Adaptive delay:
                // - 1500ms while actively charging for accurate Coulomb math
                // - 4000ms while discharging with screen ON (responsive UI/widgets)
                // - 20000ms while screen is OFF (allows CPU to enter and stay in deep sleep)
                val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
                val isInteractive = powerManager?.isInteractive ?: true
                val nextDelay = when {
                    isCharging -> 1500L
                    isInteractive -> 4000L
                    else -> 20000L
                }
                delay(nextDelay)
            }
        }
    }

    private fun checkBatteryAlarms(info: BatteryInfo) {
        val prefs = getSharedPreferences("yoyo_settings_prefs", Context.MODE_PRIVATE)
        val alarm80Enabled = prefs.getBoolean("alarm_80_enabled", false)
        val alarm20Enabled = prefs.getBoolean("alarm_20_enabled", false)
        val overheatEnabled = prefs.getBoolean("overheat_alert_enabled", true)
        val overheatThreshold = prefs.getInt("overheat_threshold_celsius", 42)

        // 1. Critical Overheat Detection
        val tempC = info.tempCelsius
        if (overheatEnabled && tempC >= overheatThreshold && !hasAlertedOverheat) {
            hasAlertedOverheat = true
            sendAlarmNotification(
                notificationId = OVERHEAT_NOTIFICATION_ID,
                title = "🔥 Critical Overheat Warning (${String.format(Locale.US, "%.1f°C", tempC)})",
                message = "Battery is dangerously hot! Disconnect charger or close intensive apps immediately to prevent cell degradation."
            )
        } else if (tempC < (overheatThreshold - 3.0f)) {
            hasAlertedOverheat = false
        }

        // 2. Battery Percentage Protection
        if (info.isCharging) {
            hasAlerted20Percent = false
            if (alarm80Enabled && info.level >= 80 && !hasAlerted80Percent) {
                hasAlerted80Percent = true
                sendAlarmNotification(
                    notificationId = ALARM_NOTIFICATION_ID,
                    title = "🔋 80% Battery Reached",
                    message = "Unplug your charger to protect long-term battery cell health."
                )
            }
        } else {
            hasAlerted80Percent = false
            if (alarm20Enabled && info.level <= 20 && !hasAlerted20Percent) {
                hasAlerted20Percent = true
                sendAlarmNotification(
                    notificationId = ALARM_NOTIFICATION_ID,
                    title = "⚡ Low Battery Warning (20%)",
                    message = "Connect your charger soon to avoid deep discharge strain."
                )
            }
        }
    }

    private fun sendAlarmNotification(notificationId: Int, title: String, message: String) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val prefs = getSharedPreferences("yoyo_settings_prefs", Context.MODE_PRIVATE)
        val quietHoursEnabled = prefs.getBoolean("quiet_hours_enabled", false)
        val quietStart = prefs.getInt("quiet_hours_start_hour", 22)
        val quietEnd = prefs.getInt("quiet_hours_end_hour", 7)
        val quietVibrateOnly = prefs.getBoolean("quiet_hours_vibrate_only", true)

        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val isQuietHour = if (quietHoursEnabled) {
            if (quietStart > quietEnd) {
                currentHour >= quietStart || currentHour < quietEnd
            } else {
                currentHour in quietStart until quietEnd
            }
        } else false

        val builder = NotificationCompat.Builder(this, ALARM_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_stat_battery)
            .setColor(if (notificationId == OVERHEAT_NOTIFICATION_ID || title.contains("20%")) 0xFFFF5252.toInt() else 0xFF00E676.toInt())
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        if (isQuietHour) {
            if (quietVibrateOnly) {
                builder.setVibrate(longArrayOf(0, 350, 200, 350))
            } else {
                builder.setSilent(true)
            }
        } else {
            builder.setDefaults(NotificationCompat.DEFAULT_ALL)
        }

        manager.notify(notificationId, builder.build())
    }

    private fun buildNotification(info: BatteryInfo): Notification {
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Material You adaptive dynamic color based on charging speed / health
        val adaptiveColor = when {
            info.tempCelsius >= 42f -> 0xFFEF4444.toInt() // Overheat warning
            info.isCharging && info.powerWatts >= 30.0 -> 0xFF00E5FF.toInt() // Rapid / Turbo
            info.isCharging -> 0xFF10B981.toInt() // Clean Emerald charging
            info.level <= 20 -> 0xFFF59E0B.toInt() // Low battery amber
            else -> 0xFF38BDF8.toInt() // Cool sky blue discharging
        }

        val title = if (info.isCharging) {
            val speedLabel = if (info.chargingSpeedCategory.label.isNotBlank()) info.chargingSpeedCategory.label else "Charging"
            "⚡ ${info.level}% · $speedLabel (${String.format(Locale.US, "%.1fW", info.powerWatts)})"
        } else {
            val powerStr = String.format(Locale.US, "%.1fW", info.powerWatts)
            "🔋 ${info.level}% · Discharging ($powerStr)"
        }

        val shortSummary = if (info.isCharging) {
            String.format(
                Locale.US,
                "%s to full · %+d mA · %.2fV · %.1f°C",
                info.timeRemainingFormatted,
                info.currentNowMa,
                info.voltageVolts,
                info.tempCelsius
            )
        } else {
            String.format(
                Locale.US,
                "%s left · %d mA · %.2fV · %.1f°C",
                info.timeRemainingFormatted,
                info.currentNowMa,
                info.voltageVolts,
                info.tempCelsius
            )
        }

        val subText = if (info.isCharging) {
            "${info.pluggedSource.name} · ${info.technology}"
        } else {
            "Health: ${info.healthStatus.toLabel()} · ${info.technology}"
        }

        val expandedBody = if (info.isCharging) {
            """
            ⚡ Power Delivery: ${String.format(Locale.US, "%.2f Watts", info.powerWatts)} (${info.chargingSpeedDescription})
            ⏱ Time to Full: ${info.timeRemainingFormatted} (until 100% capacity)
            🔋 Electric Flow: ${String.format(Locale.US, "%+d mA", info.currentNowMa)} · Terminal: ${String.format(Locale.US, "%.3f V (%d mV)", info.voltageVolts, info.voltageMv)}
            🌡️ Temperature: ${String.format(Locale.US, "%.1f°C (%.1f°F)", info.tempCelsius, info.tempFahrenheit)} · Thermal: ${info.temperatureState.label}
            🔌 Charger Protocol: ${info.chargerCapacity} via ${info.pluggedSource.name} (${info.technology})
            🛡️ Cell Health: ${info.healthStatus.toLabel()} · Optimal Operating Range
            """.trimIndent()
        } else {
            """
            🔋 Battery Level: ${info.level}% (${info.timeRemainingFormatted} estimated remaining)
            ⚡ Hardware Drain: ${String.format(Locale.US, "%.2f Watts", info.powerWatts)} (${String.format(Locale.US, "%d mA", kotlin.math.abs(info.currentNowMa))})
            ⚡ Terminal Voltage: ${String.format(Locale.US, "%.3f V (%d mV)", info.voltageVolts, info.voltageMv)}
            🌡️ Temperature: ${String.format(Locale.US, "%.1f°C (%.1f°F)", info.tempCelsius, info.tempFahrenheit)} · ${info.temperatureState.label}
            🛡️ Battery Health: ${info.healthStatus.toLabel()} · Chemistry: ${info.technology}
            """.trimIndent()
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(shortSummary)
            .setSubText(subText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(expandedBody))
            .setSmallIcon(R.drawable.ic_stat_battery)
            .setColor(adaptiveColor)
            .setContentIntent(contentIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateNotification(info: BatteryInfo) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, buildNotification(info))
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val monitorChannel = NotificationChannel(
                CHANNEL_ID,
                "Battery Session Monitoring",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps session tracking active in the background for accurate battery health analytics."
                setShowBadge(false)
            }
            manager.createNotificationChannel(monitorChannel)

            val alarmChannel = NotificationChannel(
                ALARM_CHANNEL_ID,
                "Battery Protection Alarms",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts when reaching 80% charge or 20% low battery threshold."
            }
            manager.createNotificationChannel(alarmChannel)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        try {
            unregisterReceiver(batteryReceiver)
        } catch (_: Exception) {}
        serviceJob.cancel()
        super.onDestroy()
    }
}
