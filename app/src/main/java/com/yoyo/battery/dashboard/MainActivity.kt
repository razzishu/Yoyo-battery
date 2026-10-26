package com.yoyo.battery.dashboard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.yoyo.battery.dashboard.data.model.BatteryInfo
import com.yoyo.battery.dashboard.data.model.BatteryStatus
import com.yoyo.battery.dashboard.data.model.ChargingSession
import com.yoyo.battery.dashboard.data.model.HealthStatus
import com.yoyo.battery.dashboard.data.model.PluggedSource
import com.yoyo.battery.dashboard.data.repository.BatteryHealthDetails
import com.yoyo.battery.dashboard.ui.BatteryViewModel
import com.yoyo.battery.dashboard.ui.DashboardContent
import com.yoyo.battery.dashboard.ui.DashboardScreen
import com.yoyo.battery.dashboard.ui.theme.YoyoBatteryTheme

class MainActivity : ComponentActivity() {

    private val viewModel: BatteryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }

        setContent {
            YoyoBatteryTheme {
                DashboardScreen(viewModel = viewModel)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.setForegroundVisible(true)
    }

    override fun onStop() {
        viewModel.setForegroundVisible(false)
        super.onStop()
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DashboardPreview() {
    YoyoBatteryTheme {
        DashboardContent(
            info = BatteryInfo(
                level = 88,
                status = BatteryStatus.CHARGING,
                pluggedSource = PluggedSource.AC,
                healthStatus = HealthStatus.GOOD,
                voltageMv = 4150,
                temperatureTenthsCelsius = 312,
                technology = "Li-ion",
                currentNowMa = 1850,
                currentAvgMa = 1600,
                chargeCounterUah = 3960000,
                designCapacityMah = 4500,
                estimatedCapacityMah = 4338,
                cycleCount = 124
            ),
            healthDetails = BatteryHealthDetails(
                healthScorePercent = 96,
                isCalibrating = false,
                requiredSessionsCount = 3,
                completedBenchmarkSessionsCount = 10,
                recentSessionsWindowSize = 10,
                estimatedCapacityMah = 4338,
                designCapacityMah = 4500,
                healthConditionLabel = "Excellent",
                confidenceRating = "High Precision (98%)",
                confidenceScorePercent = 98,
                totalSessionsAnalyzed = 10,
                totalMahMeasured = 28400.0,
                dynamicTip = "💡 Maintain 20%-80%: Charging between 20% and 80% reduces internal chemical stress.",
                calculationMethod = "Coulomb Integration over LAST 10 Benchmark Sessions",
                detailedExplanation = "Sample integration across multiple voltage levels in persistent database",
                calibrationAdvice = "✓ Active Health Window: Processing the LAST 10 benchmark charging sessions.",
                maintenanceTips = listOf("Keep charge between 20% and 80%", "Avoid thermal stress"),
                capacityLossMah = 162,
                wearPercentage = 3.6f,
                totalPercentAnalyzed = 430,
                averageEfficiencyMahPerPercent = 43.4,
                calibrationStage = 3,
                coulombFormulaMathString = "28,400 mAh ÷ (650% ÷ 100) = 4,338 mAh"
            ),
            chargingHistory = listOf(
                ChargingSession(
                    id = "1",
                    startTimeMs = System.currentTimeMillis() - 3600000,
                    endTimeMs = System.currentTimeMillis(),
                    startLevel = 45,
                    endLevel = 88,
                    chargeAddedMah = 1935.0,
                    plugType = "AC",
                    maxCurrentMa = 2400,
                    avgCurrentMa = 1850,
                    maxTemperatureCelsius = 34.5f,
                    maxVoltageVolts = 4.2,
                    maxPowerWatts = 18.5,
                    estimatedFullCapacityMah = 4338.0
                )
            ),
            isFahrenheit = false,
            activeDetailType = null,
            isSettingsOpen = false,
            isBackgroundTrackingEnabled = true,
            onToggleBackgroundTracking = {},
            onToggleTemperatureUnit = {},
            onOpenSettings = {},
            onCloseSettings = {},
            onOpenDetail = {},
            onCloseDetail = {}
        )
    }
}
