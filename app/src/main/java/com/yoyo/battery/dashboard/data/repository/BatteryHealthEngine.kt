package com.yoyo.battery.dashboard.data.repository

import android.content.Context
import com.yoyo.battery.dashboard.data.model.BatteryInfo
import com.yoyo.battery.dashboard.data.model.ChargingSession
import com.yoyo.battery.dashboard.data.model.HealthStatus
import java.util.Locale
import kotlin.math.roundToInt

data class BatteryHealthDetails(
    val healthScorePercent: Int?,
    val isCalibrating: Boolean,
    val requiredSessionsCount: Int = 3,
    val completedBenchmarkSessionsCount: Int,
    val recentSessionsWindowSize: Int,
    val estimatedCapacityMah: Int,
    val designCapacityMah: Int,
    val healthConditionLabel: String,
    val confidenceRating: String,
    val confidenceScorePercent: Int,
    val totalSessionsAnalyzed: Int,
    val totalMahMeasured: Double,
    val dynamicTip: String,
    val calculationMethod: String,
    val detailedExplanation: String,
    val calibrationAdvice: String,
    val maintenanceTips: List<String>,
    // Newly added detailed measurement telemetry:
    val capacityLossMah: Int = 0,
    val wearPercentage: Float = 0f,
    val totalPercentAnalyzed: Int = 0,
    val averageEfficiencyMahPerPercent: Double = 0.0,
    val benchmarkSessions: List<ChargingSession> = emptyList(),
    val coulombFormulaMathString: String = "",
    val calibrationStage: Int = 1 // 1: Baseline Hardware, 2: Active Learning (1-2), 3: High Precision (3-10)
)

class BatteryHealthEngine(context: Context? = null) {

    private val allMaintenanceTips = listOf(
        "💡 Maintain 20%-80%: Charging between 20% and 80% reduces internal chemical stress and doubles overall lifespan.",
        "🔥 Avoid Thermal Stress: Never charge your phone under direct sunlight or hot pillows—keep temperatures under 38°C.",
        "⚡ Moderate Charging Speeds: Slow to medium charging overnight lowers heat build-up and protects battery cell health.",
        "🔋 Don't Leave at 100%: Leaving your phone plugged in for hours after reaching 100% causes high voltage wear.",
        "🎯 Calibration Advice: Charge continuously by 20%+ in single sessions to generate high-precision health data points."
    )

    fun calculateAccurateHealth(info: BatteryInfo, sessions: List<ChargingSession>): BatteryHealthDetails {
        val designMah = info.designCapacityMah.coerceAtLeast(3000)

        // Filter valid benchmark charging sessions (gained at least 10% level) and take the MOST RECENT 10 sessions
        val recent10BenchmarkSessions = sessions
            .filter { it.gainedPercent >= 10 && it.estimatedFullCapacityMah > 1500 && it.estimatedFullCapacityMah < 12000 }
            .take(10)

        var weightedCapacitySum = 0.0
        var totalWeightGainedPct = 0.0
        var totalChargeMeasuredMah = 0.0

        for (session in recent10BenchmarkSessions) {
            val weight = session.gainedPercent.toDouble()
            weightedCapacitySum += session.estimatedFullCapacityMah * weight
            totalWeightGainedPct += weight
            totalChargeMeasuredMah += session.chargeAddedMah
        }

        val requiredSessions = 3
        val benchmarkCount = recent10BenchmarkSessions.size
        val isCalibrating = benchmarkCount < requiredSessions

        val estimatedCapacityMah: Int
        val accuracyPercent: Int
        val healthScorePercent: Int?
        val confidenceRating: String
        val calibrationStage: Int

        if (!isCalibrating && totalWeightGainedPct >= 10.0) {
            // UNLOCKED REAL HEALTH MODE (Calculated over the LAST 10 Benchmark Sessions)
            val weightedAvgCapacity = weightedCapacitySum / totalWeightGainedPct
            estimatedCapacityMah = weightedAvgCapacity.roundToInt()
            calibrationStage = 3

            // Accuracy grows linearly/smoothly from 70% at 3 sessions up to 98% at 10 sessions
            val sessionFraction = (benchmarkCount.toDouble() / 10.0).coerceIn(0.3, 1.0)
            accuracyPercent = (70.0 + (28.0 * sessionFraction)).roundToInt().coerceIn(70, 98)

            val rawRatio = (estimatedCapacityMah.toDouble() / designMah.toDouble()) * 100.0
            var computedHealth = rawRatio.coerceIn(20.0, 100.0).roundToInt()

            if (info.healthStatus != HealthStatus.GOOD && info.healthStatus != HealthStatus.UNKNOWN) {
                computedHealth = computedHealth.coerceAtMost(70)
            }
            healthScorePercent = computedHealth

            val precisionCategory = when {
                accuracyPercent >= 90 -> "High Precision"
                accuracyPercent >= 80 -> "Good Precision"
                else -> "Moderate Precision"
            }
            confidenceRating = "$precisionCategory ($accuracyPercent%)"
        } else {
            // CALIBRATION PHASE (< 3 Benchmark Sessions)
            calibrationStage = if (benchmarkCount == 0) 1 else 2
            healthScorePercent = null
            estimatedCapacityMah = 0
            accuracyPercent = (40 + benchmarkCount * 12).coerceIn(40, 65)
            confidenceRating = "Calibrating ($benchmarkCount/$requiredSessions Sessions)"
        }

        val capacityLoss = if (!isCalibrating && estimatedCapacityMah > 0) {
            (designMah - estimatedCapacityMah).coerceAtLeast(0)
        } else {
            0
        }

        val wearPct = if (!isCalibrating && designMah > 0 && capacityLoss > 0) {
            ((capacityLoss.toDouble() / designMah.toDouble()) * 100.0).toFloat()
        } else {
            0f
        }

        val avgEfficiency = if (totalWeightGainedPct > 0) {
            totalChargeMeasuredMah / totalWeightGainedPct
        } else {
            0.0
        }

        val formulaMathString = if (!isCalibrating && totalWeightGainedPct > 0) {
            String.format(
                Locale.US,
                "%.0f mAh ÷ (%.0f%% ÷ 100) = %d mAh",
                totalChargeMeasuredMah,
                totalWeightGainedPct,
                estimatedCapacityMah
            )
        } else {
            "Coulomb Integration active: awaiting ≥ 3 benchmark sessions (≥10% gain each)"
        }

        val selectedTip = when {
            isCalibrating -> "⚡ Calibration Phase ($benchmarkCount/$requiredSessions): Charge continuously by 10%+ in single sessions to unlock high-precision health."
            info.tempCelsius > 38f -> "🔥 High Temperature Warning: Battery is warm (${info.tempCelsius}°C). Pause fast charging to prevent degradation."
            info.level >= 95 -> "🔋 High Voltage Limit: Battery is at ${info.level}%. Unplug to maximize battery longevity."
            else -> {
                val tipOffset = (sessions.size.toLong() + (System.currentTimeMillis() / 100_000L))
                val safeIndex = kotlin.math.abs(tipOffset % allMaintenanceTips.size).toInt()
                allMaintenanceTips[safeIndex]
            }
        }

        val explanation = if (isCalibrating) {
            """
                Battery Health Calibration in Progress ($benchmarkCount/$requiredSessions Benchmark Sessions Recorded).
                
                To guarantee laboratory-grade precision, Yoyo Battery requires at least 3 benchmark charging sessions (where you charge by at least 10% in a single uninterrupted session).
                
                Once completed, the Coulomb Integration engine processes your MOST RECENT 10 charging sessions to calculate your real battery health percentage with up to 98% confidence.
            """.trimIndent()
        } else {
            """
                Real Battery Health Unlocked (Calculated from the LAST $benchmarkCount Benchmark Charging Sessions).
                
                Method: Analyzes the most recent 10 benchmark charging sessions in SQLite using Coulomb Integration:
                Estimated Capacity = Charge Added (mAh) ÷ (Level Gained ÷ 100)
                
                Focusing on the 10 most recent sessions ensures your battery health score reflects current battery condition without being skewed by old historical data.
            """.trimIndent()
        }

        val calibrationAdvice = if (isCalibrating) {
            "⚡ Calibration Needed: $benchmarkCount/$requiredSessions benchmark sessions recorded. Charge by 10%+ continuously to complete calibration."
        } else {
            "✓ Active Health Window: Processing the LAST $benchmarkCount benchmark charging sessions (${totalWeightGainedPct.roundToInt()}% charge analyzed)."
        }

        return BatteryHealthDetails(
            healthScorePercent = healthScorePercent,
            isCalibrating = isCalibrating,
            requiredSessionsCount = requiredSessions,
            completedBenchmarkSessionsCount = benchmarkCount,
            recentSessionsWindowSize = benchmarkCount,
            estimatedCapacityMah = estimatedCapacityMah,
            designCapacityMah = designMah,
            healthConditionLabel = if (isCalibrating) "Calibrating" else info.healthStatus.toLabel(),
            confidenceRating = confidenceRating,
            confidenceScorePercent = accuracyPercent,
            totalSessionsAnalyzed = benchmarkCount,
            totalMahMeasured = totalChargeMeasuredMah,
            dynamicTip = selectedTip,
            calculationMethod = "Coulomb Integration over LAST 10 Benchmark Sessions",
            detailedExplanation = explanation,
            calibrationAdvice = calibrationAdvice,
            maintenanceTips = allMaintenanceTips,
            capacityLossMah = capacityLoss,
            wearPercentage = wearPct,
            totalPercentAnalyzed = totalWeightGainedPct.roundToInt(),
            averageEfficiencyMahPerPercent = avgEfficiency,
            benchmarkSessions = recent10BenchmarkSessions,
            coulombFormulaMathString = formulaMathString,
            calibrationStage = calibrationStage
        )
    }
}
