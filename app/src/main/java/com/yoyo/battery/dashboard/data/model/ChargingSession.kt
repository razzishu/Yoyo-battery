package com.yoyo.battery.dashboard.data.model

data class ChargingSession(
    val id: String,
    val startTimeMs: Long,
    val endTimeMs: Long? = null,
    val startLevel: Int,
    val endLevel: Int? = null,
    val chargeAddedMah: Double = 0.0,
    val plugType: String,
    val maxCurrentMa: Int = 0,
    val avgCurrentMa: Int = 0,
    val maxTemperatureCelsius: Float = 0f,
    val maxVoltageVolts: Double = 0.0,
    val maxPowerWatts: Double = 0.0,
    val estimatedFullCapacityMah: Double = 0.0
) {
    val durationMinutes: Long
        get() {
            val end = endTimeMs ?: System.currentTimeMillis()
            return ((end - startTimeMs) / (1000 * 60)).coerceAtLeast(1)
        }

    val gainedPercent: Int
        get() {
            val end = endLevel ?: startLevel
            return (end - startLevel).coerceAtLeast(0)
        }

    val isOngoing: Boolean
        get() = endTimeMs == null

    /**
     * Charging rate in mAh per minute
     */
    val chargingSpeedMahPerMin: Double
        get() {
            val mins = durationMinutes.toDouble()
            return if (mins > 0 && chargeAddedMah > 0) chargeAddedMah / mins else 0.0
        }
}
