package com.yoyo.battery.dashboard

import com.yoyo.battery.dashboard.data.model.BatteryInfo
import com.yoyo.battery.dashboard.data.model.BatteryStatus
import com.yoyo.battery.dashboard.data.model.HealthStatus
import com.yoyo.battery.dashboard.data.model.PluggedSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BatteryInfoTest {

    @Test
    fun testVoltageAndTemperatureCalculations() {
        val info = BatteryInfo(
            voltageMv = 4120,
            temperatureTenthsCelsius = 325
        )

        assertEquals(4.12, info.voltageVolts, 0.001)
        assertEquals(32.5f, info.tempCelsius, 0.01f)
        assertEquals(90.5f, info.tempFahrenheit, 0.01f)
    }

    @Test
    fun testPowerWattsCalculation() {
        val infoCharging = BatteryInfo(
            status = BatteryStatus.CHARGING,
            voltageMv = 4000, // 4.0 V
            currentNowMa = 2000 // 2000 mA = 2.0 A
        )

        // Power = 4.0 V * 2.0 A = 8.0 W
        assertEquals(8.0, infoCharging.powerWatts, 0.01)
        assertTrue(infoCharging.isCharging)

        val infoDischarging = BatteryInfo(
            status = BatteryStatus.DISCHARGING,
            voltageMv = 3800, // 3.8 V
            currentNowMa = -500 // -500 mA = 0.5 A
        )

        // Power = 3.8 V * 0.5 A = 1.9 W
        assertEquals(1.9, infoDischarging.powerWatts, 0.01)
        assertFalse(infoDischarging.isCharging)
    }

    @Test
    fun testHealthPercentageCalculation() {
        val infoHealth = BatteryInfo(
            healthStatus = HealthStatus.GOOD,
            designCapacityMah = 5000,
            estimatedCapacityMah = 4500
        )

        // Ratio: 4500 / 5000 = 90%
        assertEquals(90, infoHealth.healthPercentage)
    }

    @Test
    fun testPluggedSourceAndStatusFromExtra() {
        val statusCharging = BatteryStatus.fromExtra(2) // BATTERY_STATUS_CHARGING
        assertEquals(BatteryStatus.CHARGING, statusCharging)

        val pluggedAc = PluggedSource.fromExtra(1) // BATTERY_PLUGGED_AC
        assertEquals(PluggedSource.AC, pluggedAc)

        val healthGood = HealthStatus.fromExtra(2) // BATTERY_HEALTH_GOOD
        assertEquals(HealthStatus.GOOD, healthGood)
        assertEquals("Excellent", healthGood.toLabel())
    }
}
