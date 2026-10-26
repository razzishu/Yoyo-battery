package com.yoyo.battery.dashboard.data.repository

import android.content.Context
import android.os.PowerManager
import android.os.SystemClock
import com.yoyo.battery.dashboard.data.model.BatteryInfo
import kotlin.math.abs

data class BatteryDrainStats(
    val activeDrainRatePerHour: Float,
    val activeCurrentMa: Int,
    val screenOnDurationMs: Long,
    val screenOnDropPercent: Int,
    val estimatedScreenOnHours: Float,

    val idleDrainRatePerHour: Float,
    val idleCurrentMa: Int,
    val screenOffDurationMs: Long,
    val screenOffDropPercent: Int,
    val estimatedStandbyHours: Float,

    val deepSleepPercentage: Float,
    val deepSleepDurationMs: Long,

    val combinedDrainRatePerHour: Float,
    val combinedEstimatedHours: Float,

    val isScreenOn: Boolean,
    val liveCurrentMa: Int,
    val livePowerWatts: Double,
    val batteryLevel: Int
) {
    val formattedScreenOnTime: String
        get() = formatDuration(screenOnDurationMs)

    val formattedScreenOffTime: String
        get() = formatDuration(screenOffDurationMs)

    val formattedDeepSleepTime: String
        get() = formatDuration(deepSleepDurationMs)

    private fun formatDuration(ms: Long): String {
        val totalSec = ms / 1000
        val hours = totalSec / 3600
        val mins = (totalSec % 3600) / 60
        return when {
            hours > 0 -> "${hours}h ${mins}m"
            mins > 0 -> "${mins}m"
            else -> "${totalSec}s"
        }
    }
}

class BatteryDrainTracker private constructor(context: Context) {

    private val appContext = context.applicationContext
    private val powerManager = appContext.getSystemService(Context.POWER_SERVICE) as? PowerManager
    private val prefs = appContext.getSharedPreferences("yoyo_battery_drain_prefs", Context.MODE_PRIVATE)

    companion object {
        @Volatile
        private var instance: BatteryDrainTracker? = null

        fun getInstance(context: Context): BatteryDrainTracker {
            return instance ?: synchronized(this) {
                instance ?: BatteryDrainTracker(context.applicationContext).also { instance = it }
            }
        }
    }

    private var lastRecordedLevel: Int = -1
    private var lastRealtimeMs: Long = SystemClock.elapsedRealtime()
    private var lastUptimeMs: Long = SystemClock.uptimeMillis()

    // In-memory counters backed by SharedPreferences
    private var screenOnDurationMs: Long = prefs.getLong("screen_on_duration_ms", 0L)
    private var screenOffDurationMs: Long = prefs.getLong("screen_off_duration_ms", 0L)
    private var screenOffDeepSleepMs: Long = prefs.getLong("screen_off_deep_sleep_ms", 0L)
    private var screenOnDropPercent: Int = prefs.getInt("screen_on_drop_pct", 0)
    private var screenOffDropPercent: Int = prefs.getInt("screen_off_drop_pct", 0)

    private var activeCurrentSumMa: Long = prefs.getLong("active_current_sum_ma", 0L)
    private var activeCurrentSamples: Int = prefs.getInt("active_current_samples", 0)

    private var idleCurrentSumMa: Long = prefs.getLong("idle_current_sum_ma", 0L)
    private var idleCurrentSamples: Int = prefs.getInt("idle_current_samples", 0)
    private var lastSaveTimeMs: Long = 0L

    @Synchronized
    fun processBatteryUpdate(info: BatteryInfo) {
        val nowRealtime = SystemClock.elapsedRealtime()
        val nowUptime = SystemClock.uptimeMillis()

        val rawElapsedReal = nowRealtime - lastRealtimeMs
        val rawElapsedUptime = nowUptime - lastUptimeMs

        // If device was rebooted, reset clock baseline safely
        if (rawElapsedReal < 0L || rawElapsedUptime < 0L) {
            lastRealtimeMs = nowRealtime
            lastUptimeMs = nowUptime
            lastRecordedLevel = info.level
            return
        }

        val elapsedReal = rawElapsedReal.coerceAtLeast(0L)
        val elapsedUptime = rawElapsedUptime.coerceIn(0L, elapsedReal)

        lastRealtimeMs = nowRealtime
        lastUptimeMs = nowUptime

        val isScreenOn = powerManager?.isInteractive ?: true

        // Only track discharge drain when not plugged into a charger
        if (!info.isCharging) {
            val level = info.level
            if (lastRecordedLevel !in 0..100) {
                lastRecordedLevel = level
            }

            val drop = if (level < lastRecordedLevel) {
                (lastRecordedLevel - level).coerceIn(1, 15)
            } else 0

            val currentMa = abs(info.currentNowMa)

            if (isScreenOn) {
                screenOnDurationMs += elapsedReal
                if (drop > 0) screenOnDropPercent += drop
                if (currentMa > 40) {
                    activeCurrentSumMa += currentMa
                    activeCurrentSamples++
                }
            } else {
                screenOffDurationMs += elapsedReal
                // Deep sleep: CPU clock was suspended while elapsedReal continued ticking
                val sleepTime = (elapsedReal - elapsedUptime).coerceIn(0L, elapsedReal)
                screenOffDeepSleepMs += sleepTime

                if (drop > 0) screenOffDropPercent += drop
                if (currentMa in 5..300) {
                    idleCurrentSumMa += currentMa
                    idleCurrentSamples++
                }
            }

            lastRecordedLevel = level

            // Throttle disk writes: flush on actual drop or once every 60 seconds
            val nowReal = SystemClock.elapsedRealtime()
            if (drop > 0 || (nowReal - lastSaveTimeMs) >= 60000L) {
                saveStats()
                lastSaveTimeMs = nowReal
            }
        } else {
            // When charging, update baseline level without accumulating discharge drop
            lastRecordedLevel = info.level
        }
    }

    @Synchronized
    fun getDrainStats(info: BatteryInfo): BatteryDrainStats {
        val isScreenOn = powerManager?.isInteractive ?: true
        val designCap = info.designCapacityMah.coerceAtLeast(3000)

        // 1. Calculate Active (Screen-On) Drain Rate (% / hr)
        val avgActiveCurrent = if (activeCurrentSamples > 0) (activeCurrentSumMa / activeCurrentSamples).toInt() else 420
        val activeHours = screenOnDurationMs / 3600000.0f
        val activeRatePerHour = if (activeHours >= 0.15f && screenOnDropPercent > 0) {
            (screenOnDropPercent / activeHours).coerceIn(6.0f, 35.0f)
        } else {
            ((avgActiveCurrent.toFloat() / designCap.toFloat()) * 100f).coerceIn(7.5f, 25.0f)
        }

        // 2. Calculate Idle (Screen-Off / Standby) Drain Rate (% / hr)
        val avgIdleCurrent = if (idleCurrentSamples > 0) (idleCurrentSumMa / idleCurrentSamples).toInt() else 32
        val idleHours = screenOffDurationMs / 3600000.0f
        val idleRatePerHour = if (idleHours >= 0.25f && screenOffDropPercent > 0) {
            (screenOffDropPercent / idleHours).coerceIn(0.3f, 6.0f)
        } else {
            ((avgIdleCurrent.toFloat() / designCap.toFloat()) * 100f).coerceIn(0.4f, 2.5f)
        }

        // 3. Deep Sleep Ratio (% of screen-off time spent in hardware deep sleep)
        val deepSleepRatio = if (screenOffDurationMs > 30000L) {
            (screenOffDeepSleepMs.toFloat() / screenOffDurationMs.toFloat() * 100f).coerceIn(0f, 100f)
        } else {
            92.5f // Hardware baseline expectation
        }

        // 4. Projected Runtime based on current remaining battery level
        val currentLvl = info.level.coerceIn(1, 100)
        val estimatedScreenOnHours = (currentLvl / activeRatePerHour).coerceIn(0.5f, 24.0f)
        val estimatedStandbyHours = (currentLvl / idleRatePerHour).coerceIn(2.0f, 200.0f)

        // 5. Combined Usage (realistic 30% screen-on / 70% screen-off daily routine)
        val combinedRatePerHour = (0.30f * activeRatePerHour) + (0.70f * idleRatePerHour)
        val combinedEstimatedHours = (currentLvl / combinedRatePerHour).coerceIn(1.0f, 72.0f)

        return BatteryDrainStats(
            activeDrainRatePerHour = activeRatePerHour,
            activeCurrentMa = avgActiveCurrent,
            screenOnDurationMs = screenOnDurationMs,
            screenOnDropPercent = screenOnDropPercent,
            estimatedScreenOnHours = estimatedScreenOnHours,

            idleDrainRatePerHour = idleRatePerHour,
            idleCurrentMa = avgIdleCurrent,
            screenOffDurationMs = screenOffDurationMs,
            screenOffDropPercent = screenOffDropPercent,
            estimatedStandbyHours = estimatedStandbyHours,

            deepSleepPercentage = deepSleepRatio,
            deepSleepDurationMs = screenOffDeepSleepMs,

            combinedDrainRatePerHour = combinedRatePerHour,
            combinedEstimatedHours = combinedEstimatedHours,

            isScreenOn = isScreenOn,
            liveCurrentMa = info.currentNowMa,
            livePowerWatts = info.powerWatts,
            batteryLevel = currentLvl
        )
    }

    @Synchronized
    fun resetStats() {
        screenOnDurationMs = 0L
        screenOffDurationMs = 0L
        screenOffDeepSleepMs = 0L
        screenOnDropPercent = 0
        screenOffDropPercent = 0
        activeCurrentSumMa = 0L
        activeCurrentSamples = 0
        idleCurrentSumMa = 0L
        idleCurrentSamples = 0
        lastRecordedLevel = -1
        lastRealtimeMs = SystemClock.elapsedRealtime()
        lastUptimeMs = SystemClock.uptimeMillis()
        saveStats()
        lastSaveTimeMs = SystemClock.elapsedRealtime()
    }

    private fun saveStats() {
        prefs.edit()
            .putLong("screen_on_duration_ms", screenOnDurationMs)
            .putLong("screen_off_duration_ms", screenOffDurationMs)
            .putLong("screen_off_deep_sleep_ms", screenOffDeepSleepMs)
            .putInt("screen_on_drop_pct", screenOnDropPercent)
            .putInt("screen_off_drop_pct", screenOffDropPercent)
            .putLong("active_current_sum_ma", activeCurrentSumMa)
            .putInt("active_current_samples", activeCurrentSamples)
            .putLong("idle_current_sum_ma", idleCurrentSumMa)
            .putInt("idle_current_samples", idleCurrentSamples)
            .apply()
    }
}
