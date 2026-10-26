package com.yoyo.battery.dashboard

import com.yoyo.battery.dashboard.data.repository.BatteryDrainStats
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BatteryDrainTest {

    @Test
    fun testDrainStatsFormatting() {
        val stats = BatteryDrainStats(
            activeDrainRatePerHour = 10.0f,
            activeCurrentMa = 450,
            screenOnDurationMs = 7200000L, // 2 hours
            screenOnDropPercent = 20,
            estimatedScreenOnHours = 8.0f,

            idleDrainRatePerHour = 0.8f,
            idleCurrentMa = 35,
            screenOffDurationMs = 28800000L, // 8 hours
            screenOffDropPercent = 6,
            estimatedStandbyHours = 100.0f,

            deepSleepPercentage = 95.0f,
            deepSleepDurationMs = 27360000L, // ~7.6 hours

            combinedDrainRatePerHour = 3.56f,
            combinedEstimatedHours = 22.4f,

            isScreenOn = true,
            liveCurrentMa = -420,
            livePowerWatts = 1.68,
            batteryLevel = 80
        )

        assertEquals("2h 0m", stats.formattedScreenOnTime)
        assertEquals("8h 0m", stats.formattedScreenOffTime)
        assertEquals("7h 36m", stats.formattedDeepSleepTime)
        assertEquals(80, stats.batteryLevel)
        assertTrue(stats.isScreenOn)
        assertEquals(10.0f, stats.activeDrainRatePerHour, 0.01f)
        assertEquals(0.8f, stats.idleDrainRatePerHour, 0.01f)
        assertEquals(95.0f, stats.deepSleepPercentage, 0.01f)
    }

    @Test
    fun testShortDurationFormatting() {
        val stats = BatteryDrainStats(
            activeDrainRatePerHour = 12.0f,
            activeCurrentMa = 500,
            screenOnDurationMs = 45000L, // 45 seconds
            screenOnDropPercent = 0,
            estimatedScreenOnHours = 5.0f,
            idleDrainRatePerHour = 1.0f,
            idleCurrentMa = 40,
            screenOffDurationMs = 150000L, // 2m 30s
            screenOffDropPercent = 0,
            estimatedStandbyHours = 60.0f,
            deepSleepPercentage = 90.0f,
            deepSleepDurationMs = 135000L,
            combinedDrainRatePerHour = 4.3f,
            combinedEstimatedHours = 14.0f,
            isScreenOn = false,
            liveCurrentMa = -35,
            livePowerWatts = 0.14,
            batteryLevel = 60
        )

        assertEquals("45s", stats.formattedScreenOnTime)
        assertEquals("2m", stats.formattedScreenOffTime)
        assertEquals("2m", stats.formattedDeepSleepTime)
    }
}
