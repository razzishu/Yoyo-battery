package com.yoyo.battery.dashboard

import com.yoyo.battery.dashboard.data.model.BatteryInfo
import com.yoyo.battery.dashboard.data.model.BatteryStatus
import com.yoyo.battery.dashboard.data.model.ChargingSession
import com.yoyo.battery.dashboard.data.model.HealthStatus
import com.yoyo.battery.dashboard.data.model.PluggedSource
import com.yoyo.battery.dashboard.data.repository.BatteryHealthEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BatteryHealthEngineTest {

    private val engine = BatteryHealthEngine()

    @Test
    fun testCalibrationPhaseWithFewerThanThreeSessions() {
        val info = BatteryInfo(
            level = 50,
            status = BatteryStatus.CHARGING,
            pluggedSource = PluggedSource.AC,
            designCapacityMah = 5000
        )

        // Only 1 session with > 10% gain
        val sessions = listOf(
            ChargingSession(
                id = "1",
                startTimeMs = System.currentTimeMillis() - 3600000,
                endTimeMs = System.currentTimeMillis(),
                startLevel = 20,
                endLevel = 45, // +25%
                chargeAddedMah = 1200.0,
                plugType = "AC",
                estimatedFullCapacityMah = 4800.0
            )
        )

        val result = engine.calculateAccurateHealth(info, sessions)

        assertTrue("Should be in calibration phase", result.isCalibrating)
        assertNull("Health score should be null during calibration", result.healthScorePercent)
        assertEquals(1, result.completedBenchmarkSessionsCount)
        assertEquals(3, result.requiredSessionsCount)
        assertTrue(result.confidenceRating.contains("Calibrating"))
    }

    @Test
    fun testUnlockedHealthWithTenBenchmarkSessions() {
        val info = BatteryInfo(
            level = 80,
            status = BatteryStatus.CHARGING,
            healthStatus = HealthStatus.GOOD,
            designCapacityMah = 5000
        )

        // Create 10 valid benchmark sessions each estimating 4750 mAh
        val sessions = (1..10).map { i ->
            ChargingSession(
                id = "$i",
                startTimeMs = System.currentTimeMillis() - (i * 3600000L),
                endTimeMs = System.currentTimeMillis() - (i * 3600000L) + 1800000L,
                startLevel = 20,
                endLevel = 60, // 40% gain
                chargeAddedMah = 1900.0,
                plugType = "AC",
                estimatedFullCapacityMah = 4750.0
            )
        }

        val result = engine.calculateAccurateHealth(info, sessions)

        assertFalse("Should be unlocked and no longer calibrating", result.isCalibrating)
        assertNotNull("Health score should not be null", result.healthScorePercent)
        assertEquals(4750, result.estimatedCapacityMah)
        // 4750 / 5000 = 95%
        assertEquals(95, result.healthScorePercent)
        assertEquals(10, result.totalSessionsAnalyzed)
        assertTrue(result.confidenceRating.contains("High Precision"))
    }

    @Test
    fun testTipCalculationDoesNotThrowOnOverflow() {
        val info = BatteryInfo(level = 50, temperatureTenthsCelsius = 300)
        val sessions = emptyList<ChargingSession>()

        // Ensure calculation handles any timestamp without throwing IndexOutOfBoundsException
        val result = engine.calculateAccurateHealth(info, sessions)
        assertNotNull(result.dynamicTip)
        assertTrue(result.dynamicTip.isNotBlank())
    }

    @Test
    fun testChargingSessionRateAndDuration() {
        val now = System.currentTimeMillis()
        val session = ChargingSession(
            id = "test_sess",
            startTimeMs = now - (30 * 60 * 1000L), // 30 mins ago
            endTimeMs = now,
            startLevel = 20,
            endLevel = 60,
            chargeAddedMah = 1500.0,
            plugType = "AC"
        )

        assertEquals(30L, session.durationMinutes)
        assertEquals(40, session.gainedPercent)
        // 1500 mAh / 30 mins = 50.0 mAh/min
        assertEquals(50.0, session.chargingSpeedMahPerMin, 0.01)
        assertFalse(session.isOngoing)
    }
}
