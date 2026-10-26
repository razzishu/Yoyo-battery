package com.yoyo.battery.dashboard.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.Image
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.yoyo.battery.dashboard.data.repository.DeviceInfoProvider
import com.yoyo.battery.dashboard.data.repository.ComprehensiveDeviceReport
import androidx.compose.ui.platform.LocalContext
import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.Process
import android.os.StatFs
import com.yoyo.battery.dashboard.data.db.TimeRangeFilter
import com.yoyo.battery.dashboard.data.model.BatteryInfo
import com.yoyo.battery.dashboard.data.model.ChargingSession
import com.yoyo.battery.dashboard.data.repository.AppDrainItem
import com.yoyo.battery.dashboard.data.repository.BatteryDrainStats
import com.yoyo.battery.dashboard.data.repository.BatteryHealthDetails
import com.yoyo.battery.dashboard.ui.DetailType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DetailModalScreen(
    detailType: DetailType,
    info: BatteryInfo,
    healthDetails: BatteryHealthDetails,
    chargingHistory: List<ChargingSession>,
    isFahrenheit: Boolean,
    onDismiss: () -> Unit,
    selectedFilter: TimeRangeFilter = TimeRangeFilter.ALL_TIME,
    onFilterChange: (TimeRangeFilter) -> Unit = {},
    drainStats: BatteryDrainStats = BatteryDrainStats(0f, 0, 0L, 0, 0f, 0f, 0, 0L, 0, 0f, 0f, 0L, 0f, 0f, true, 0, 0.0, 100),
    onResetDrainStats: () -> Unit = {},
    onOpenAppDrain: () -> Unit = {},
    appDrainList: List<AppDrainItem> = emptyList(),
    hasUsageStatsPermission: Boolean = false,
    onRequestUsageStatsPermission: () -> Unit = {},
    onExportCsv: () -> Unit = {},
    onExportJson: () -> Unit = {},
    onRestoreData: () -> Unit = {}
) {
    val isDarkTheme = isSystemInDarkTheme()
    val colorScheme = MaterialTheme.colorScheme
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    val dismissWithAnimation: () -> Unit = {
        coroutineScope.launch {
            isVisible = false
            delay(170)
            onDismiss()
        }
    }

    Dialog(
        onDismissRequest = dismissWithAnimation,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(220)) +
                        scaleIn(initialScale = 0.90f, animationSpec = spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessMediumLow)) +
                        slideInVertically(animationSpec = spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessMediumLow)) { it / 6 },
                exit = fadeOut(tween(160)) +
                       scaleOut(targetScale = 0.94f, animationSpec = tween(160)) +
                       slideOutVertically(animationSpec = tween(160)) { it / 8 }
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 580.dp)
                        .fillMaxHeight(0.94f),
                    shape = RoundedCornerShape(32.dp),
                    color = colorScheme.surface.copy(alpha = if (isDarkTheme) 0.88f else 0.93f),
                    tonalElevation = 8.dp,
                    shadowElevation = 16.dp,
                    border = BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = if (isDarkTheme) 0.35f else 0.50f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(22.dp)
                    ) {
                        // Modern Header Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val title = when (detailType) {
                                DetailType.HERO_BATTERY -> "Battery & Charger Telemetry"
                                DetailType.BATTERY_HEALTH -> "Accurate Battery Health"
                                DetailType.BATTERY_DRAIN -> "Active & Idle Battery Drain"
                                DetailType.CYCLES -> "Battery Cycle & Wear Stats"
                                DetailType.CHARGING_HISTORY -> "Charging Session Timeline"
                                DetailType.CURRENT_FLOW -> "Current Flow Diagnostics"
                                DetailType.VOLTAGE -> "Terminal Voltage Telemetry"
                                DetailType.TEMPERATURE -> "Thermal Health Diagnostics"
                                DetailType.POWER_SOURCE -> "Power Source Details"
                                DetailType.APP_DRAIN -> "Per-App Battery Drain Attribution"
                                DetailType.DEVICE_INFO -> "Device & Hardware Specifications"
                            }

                            Text(
                                text = title,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 20.sp
                                ),
                                color = colorScheme.onSurface
                            )

                            IconButton(onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                dismissWithAnimation()
                            }) {
                                Surface(
                                    shape = CircleShape,
                                    color = colorScheme.surfaceContainerHigh.copy(alpha = 0.8f)
                                ) {
                                    Box(
                                        modifier = Modifier.size(34.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("✕", fontWeight = FontWeight.Bold, color = colorScheme.onSurface)
                                    }
                                }
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = colorScheme.outlineVariant.copy(alpha = 0.30f))

                        // Body content based on detailType
                        Box(modifier = Modifier.weight(1f)) {
                            when (detailType) {
                                DetailType.HERO_BATTERY -> HeroBatteryDetailContent(info)
                                DetailType.BATTERY_HEALTH -> BatteryHealthDetailContent(healthDetails)
                                DetailType.BATTERY_DRAIN -> BatteryDrainDetailContent(
                                    drainStats = drainStats,
                                    onResetStats = onResetDrainStats,
                                    onOpenAppDrain = onOpenAppDrain
                                )
                                DetailType.CYCLES -> CyclesDetailContent(info)
                                DetailType.CHARGING_HISTORY -> ChargingHistoryDetailContent(
                                    sessions = chargingHistory,
                                    selectedFilter = selectedFilter,
                                    onFilterChange = onFilterChange,
                                    onExportCsv = onExportCsv,
                                    onExportJson = onExportJson,
                                    onRestoreData = onRestoreData
                                )
                                DetailType.CURRENT_FLOW -> CurrentFlowDetailContent(info)
                                DetailType.VOLTAGE -> VoltageDetailContent(info)
                                DetailType.TEMPERATURE -> TemperatureDetailContent(info, isFahrenheit)
                                DetailType.POWER_SOURCE -> PowerSourceDetailContent(info)
                                DetailType.APP_DRAIN -> AppDrainDetailContent(
                                    appDrainList = appDrainList,
                                    hasUsageStatsPermission = hasUsageStatsPermission,
                                    onRequestPermission = onRequestUsageStatsPermission,
                                    designCapacityMah = info.designCapacityMah
                                )
                                DetailType.DEVICE_INFO -> DeviceInfoDetailContent(info, healthDetails)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                dismissWithAnimation()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorScheme.primary,
                                contentColor = colorScheme.onPrimary
                            )
                        ) {
                            Text("Close", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BatteryHealthDetailContent(health: BatteryHealthDetails) {
    val emeraldPrimary = Color(0xFF10B981)
    val emeraldGlow = Color(0xFF34D399)
    val purpleAccent = Color(0xFFA855F7)
    val amberWarning = Color(0xFFF59E0B)

    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // 1. HERO GAUGE BANNER (CIRCULAR RING + HEALTH SCORE)
        item {
            Card(
                modifier = Modifier.clip(RoundedCornerShape(26.dp)),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF042F2E).copy(alpha = 0.65f),
                    contentColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, emeraldPrimary.copy(alpha = 0.35f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left: Mini Health Ring
                    Box(
                        modifier = Modifier.size(90.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val progress = if (health.isCalibrating) {
                            (health.completedBenchmarkSessionsCount.toFloat() / health.requiredSessionsCount.toFloat()).coerceIn(0.15f, 0.95f)
                        } else {
                            (health.healthScorePercent ?: 100) / 100f
                        }

                        val animatedScore by animateFloatAsState(
                            targetValue = progress,
                            animationSpec = tween(1000),
                            label = "HealthScoreAnimation"
                        )

                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val strokeWidth = 8.dp.toPx()
                            drawArc(
                                color = emeraldPrimary.copy(alpha = 0.2f),
                                startAngle = 0f,
                                sweepAngle = 360f,
                                useCenter = false,
                                style = Stroke(width = strokeWidth)
                            )
                            drawArc(
                                brush = Brush.sweepGradient(listOf(emeraldPrimary, emeraldGlow, emeraldPrimary)),
                                startAngle = -90f,
                                sweepAngle = 360f * animatedScore,
                                useCenter = false,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                        }

                        if (health.isCalibrating) {
                            Text(
                                text = "${health.completedBenchmarkSessionsCount}/${health.requiredSessionsCount}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp,
                                color = emeraldGlow
                            )
                        } else {
                            Text(
                                text = "${health.healthScorePercent}%",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 24.sp,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Right: Condition & Accuracy Pills
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = if (health.isCalibrating) "Calibration Phase" else "${health.healthScorePercent}% Battery Health",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = Color.White
                        )

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = emeraldPrimary.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "Status: ${health.healthConditionLabel}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = emeraldGlow,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = purpleAccent.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = health.confidenceRating,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFFD8B4FE),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }

        // 2. CALIBRATION PROGRESS / STATUS
        item {
            if (health.isCalibrating) {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                    border = androidx.compose.foundation.BorderStroke(1.dp, emeraldPrimary.copy(alpha = 0.28f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Calibration Progress",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                val calibrationPct = ((health.completedBenchmarkSessionsCount.toFloat() / health.requiredSessionsCount.toFloat()) * 100).toInt().coerceIn(15, 95)
                                Text(
                                    text = "Stage ${health.calibrationStage} of 3 ($calibrationPct% Converged)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = amberWarning.copy(alpha = 0.16f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, amberWarning.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = "In Progress",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                    color = amberWarning,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        // THREE PLAIN SEGMENTED CALIBRATION PROGRESS BARS
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Bar 1: Hardware Baseline (Always 100% full once initialized)
                            val bar1Progress by animateFloatAsState(
                                targetValue = 1.0f,
                                animationSpec = tween(600),
                                label = "Bar1Progress"
                            )
                            LinearProgressIndicator(
                                progress = { bar1Progress },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = emeraldPrimary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )

                            // Bar 2: Active Benchmark Sessions
                            val bar2Target = if (health.calibrationStage >= 3) 1.0f
                            else if (health.calibrationStage == 2) {
                                (health.completedBenchmarkSessionsCount.toFloat() / health.requiredSessionsCount.toFloat()).coerceIn(0.1f, 1.0f)
                            } else 0.0f

                            val bar2Progress by animateFloatAsState(
                                targetValue = bar2Target,
                                animationSpec = tween(600),
                                label = "Bar2Progress"
                            )
                            LinearProgressIndicator(
                                progress = { bar2Progress },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = if (health.calibrationStage >= 2) emeraldPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )

                            // Bar 3: Statistical Convergence (Rolling 10 Sessions)
                            val bar3Target = if (health.calibrationStage >= 3) {
                                (health.completedBenchmarkSessionsCount.toFloat() / 10f).coerceIn(0.4f, 1.0f)
                            } else 0.0f

                            val bar3Progress by animateFloatAsState(
                                targetValue = bar3Target,
                                animationSpec = tween(600),
                                label = "Bar3Progress"
                            )
                            LinearProgressIndicator(
                                progress = { bar3Progress },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = if (health.calibrationStage >= 3) emeraldPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }

                        // Stage Descriptions Under Plain Bars
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "1. Baseline (Ready)",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                                color = emeraldPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "2. Learning (${health.completedBenchmarkSessionsCount}/${health.requiredSessionsCount})",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                                color = if (health.calibrationStage >= 2) emeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "3. Unlocked (98%)",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                                color = if (health.calibrationStage >= 3) emeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.End,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = health.calibrationAdvice,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                            )
                        }
                    }
                }
            } else {
                // CALIBRATED: The 3 bars are completely hidden and shows only "✓ Calibrated"
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF042F2E).copy(alpha = 0.55f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, emeraldPrimary.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = emeraldPrimary.copy(alpha = 0.25f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("✓", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = emeraldGlow)
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "✓ Calibrated",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                    color = Color.White
                                )
                                Text(
                                    text = "High Precision Coulomb Convergence Active",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = emeraldGlow
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = emeraldPrimary.copy(alpha = 0.22f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, emeraldPrimary.copy(alpha = 0.6f))
                        ) {
                            Text(
                                text = health.confidenceRating,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // 3. SCIENTIFIC TELEMETRY MEASUREMENTS GRID
        item {
            Text(
                text = "Precision Battery Telemetry",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val actualCapText = if (health.isCalibrating) "Calibrating..." else "${health.estimatedCapacityMah} mAh"
                val wearText = if (health.isCalibrating) "Calibrating..." else "-${health.capacityLossMah} mAh (${String.format(Locale.US, "%.1f%%", health.wearPercentage)} wear)"

                MetricMeasurementRow(
                    icon = "🔋",
                    label = "Measured Real Capacity",
                    value = actualCapText,
                    helper = "Actual electric energy holding capacity"
                )
                MetricMeasurementRow(
                    icon = "🏭",
                    label = "Factory Design Capacity",
                    value = "${health.designCapacityMah} mAh",
                    helper = "Nominal specification from device profile"
                )
                MetricMeasurementRow(
                    icon = "📉",
                    label = "Estimated Cell Wear",
                    value = wearText,
                    helper = "Capacity lost to chemical aging"
                )
                MetricMeasurementRow(
                    icon = "⚡",
                    label = "Total Charge Integrated",
                    value = String.format(Locale.US, "%.0f mAh", health.totalMahMeasured),
                    helper = "Sum of electrons measured across benchmark window"
                )
                MetricMeasurementRow(
                    icon = "📊",
                    label = "Total Level Gained Analyzed",
                    value = "+${health.totalPercentAnalyzed}%",
                    helper = "Total cumulative state-of-charge analyzed"
                )
                MetricMeasurementRow(
                    icon = "⚖️",
                    label = "Coulomb Efficiency",
                    value = String.format(Locale.US, "%.1f mAh / 1%%", health.averageEfficiencyMahPerPercent),
                    helper = "Average energy delivered per 1% battery percentage"
                )
            }
        }

        // 4. HOW THE CALCULATION WORKS (DEEP DIVE & PHYSICS)
        item {
            Text(
                text = "How Battery Health Calculation Works",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF0F172A),
                    contentColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "1. Physics: Coulomb Integration (Q = ∫ I dt)",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = Color(0xFF38BDF8)
                    )
                    Text(
                        text = "Smartphone operating systems only show estimated percentage based on instantaneous voltage tables. Yoyo Battery continuously samples real electrical current (mA) entering the battery cell every second and integrates total Coulomb charge (mAh):\n\nEstimated Capacity = Charge Added (mAh) ÷ (Level Gained % ÷ 100)",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFCBD5E1)
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF1E293B),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Live Math Formula:",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF94A3B8)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = health.coulombFormulaMathString,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = Color(0xFF4ADE80)
                            )
                        }
                    }

                    Text(
                        text = "2. The Rolling 10-Session Window",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = Color(0xFF38BDF8)
                    )
                    Text(
                        text = "Batteries chemically degrade over time. Averaging charging sessions from 6 months ago would hide current wear. Yoyo Battery takes the LAST 10 benchmark charging sessions in SQLite, weighting each session by its percentage gain (wi = Δ%). Sessions with larger charge gains carries proportionally higher mathematical weight, eliminating sensor noise.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFCBD5E1)
                    )

                    Text(
                        text = "3. Strict Benchmark Criteria",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = Color(0xFF38BDF8)
                    )
                    Text(
                        text = "Only sessions with at least 10% charge gain are qualified as benchmarks. Micro-charges (e.g. plugging in for 2% while transferring photos) are automatically ignored to protect data purity.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFCBD5E1)
                    )
                }
            }
        }

        // 5. CONTRIBUTING BENCHMARK SESSIONS BREAKDOWN
        if (health.benchmarkSessions.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Active Contributing Sessions",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${health.benchmarkSessions.size} Sessions",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = emeraldPrimary
                    )
                }
            }

            items(health.benchmarkSessions) { session ->
                val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.US)
                val dateStr = dateFormat.format(Date(session.startTimeMs))
                val endLvl = session.endLevel ?: session.startLevel

                ElevatedCard(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = dateStr,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = emeraldPrimary.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "+${session.gainedPercent}%",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                        color = emeraldPrimary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${session.startLevel}% → $endLvl%  ·  ${session.durationMinutes}m via ${session.plugType}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = String.format(Locale.US, "%.0f mAh", session.chargeAddedMah),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = String.format(Locale.US, "Cap: %.0f mAh", session.estimatedFullCapacityMah),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // 6. BATTERY PRESERVATION RECOMMENDATIONS
        item {
            Text(
                text = "Longevity & Preservation Recommendations",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                health.maintenanceTips.forEach { tip ->
                    ElevatedCard(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                    ) {
                        Text(
                            text = tip,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }

        // 7. BEST PRACTICES FOR BATTERY LONGEVITY
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                border = androidx.compose.foundation.BorderStroke(1.dp, emeraldPrimary.copy(alpha = 0.25f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "💡 Best Practices to Maximize Battery Health",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• The 20-80% Optimal Range: Lithium-ion cells experience the lowest physical crystal degradation when operating between 20% and 80%. Turn on the 80% Protection Alarm in Settings to double lifespan.\n\n• Avoid Thermal Overheating: Temperatures above 40°C cause accelerated electrolyte decay and lithium plating. Remove thick phone cases while fast-charging.\n\n• Minimise Micro-Trickle: Leaving phone on charger at 100% holds high anode voltage for prolonged hours. Unplug promptly when charge completes.",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp, lineHeight = 16.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun MetricMeasurementRow(
    icon: String,
    label: String,
    value: String,
    helper: String
) {
    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = icon, fontSize = 22.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = helper,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun HeroBatteryDetailContent(info: BatteryInfo) {
    val colorScheme = MaterialTheme.colorScheme
    val speedCat = info.chargingSpeedCategory
    val badgeColor = if (info.isCharging) Color(speedCat.badgeColorHex) else Color(0xFF64748B)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. TOP HERO CARD
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceContainerHigh),
                border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.45f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = badgeColor.copy(alpha = 0.18f),
                                modifier = Modifier.size(46.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (info.isCharging) AppIcons.ElectricBolt else AppIcons.BatteryFull,
                                        contentDescription = null,
                                        tint = badgeColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "${info.level}% Charged",
                                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                                    color = colorScheme.onSurface
                                )
                                Text(
                                    text = info.timeRemainingFormatted,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                    color = Color(0xFF00E5FF)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = badgeColor.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = if (info.isCharging) speedCat.label.uppercase(Locale.US) else "DISCHARGING",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, fontSize = 11.sp),
                                color = badgeColor,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }

                    LinearProgressIndicator(
                        progress = { (info.level / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = badgeColor,
                        trackColor = colorScheme.surfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Power", style = MaterialTheme.typography.labelSmall, color = colorScheme.onSurfaceVariant)
                            Text(String.format(Locale.US, "%.1f W", info.powerWatts), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = colorScheme.onSurface)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Current", style = MaterialTheme.typography.labelSmall, color = colorScheme.onSurfaceVariant)
                            val sign = if (info.currentNowMa > 0) "+" else ""
                            Text("$sign${info.currentNowMa} mA", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = badgeColor)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Voltage", style = MaterialTheme.typography.labelSmall, color = colorScheme.onSurfaceVariant)
                            Text(String.format(Locale.US, "%.2f V", info.voltageVolts), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = colorScheme.onSurface)
                        }
                    }
                }
            }
        }

        // 2. DETAILED TELEMETRY CARDS (EVENLY SPACED)
        item {
            Text(
                text = "Detailed Battery Telemetry",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = colorScheme.onSurface
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                val statusLabel = if (info.isCharging) "Charging (${info.pluggedSource.name})" else "Discharging"

                DetailStatCard("Current Battery Level", "${info.level}%")
                DetailStatCard("Charging State", statusLabel)
                if (info.isCharging) {
                    DetailStatCard("Charger Capacity", info.chargerCapacity)
                }
                DetailStatCard("Charger Speed Classification", speedCat.label)
                DetailStatCard("Power Plug Source", info.pluggedSource.name)
                DetailStatCard("Live Power Consumption / Delivery", String.format(Locale.US, "%.2f Watts", info.powerWatts))
                DetailStatCard("Live Electric Current Flow", "${info.currentNowMa} mA")
                DetailStatCard("Terminal Cell Voltage", String.format(Locale.US, "%.3f V (%d mV)", info.voltageVolts, info.voltageMv))
                DetailStatCard("Estimated Remaining Time", info.timeRemainingFormatted)
                DetailStatCard("Battery Chemistry Technology", info.technology)
            }
        }
    }
}

@Composable
fun CyclesDetailContent(info: BatteryInfo) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            ElevatedCard(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    val countText = if (info.cycleCount != null) "${info.cycleCount} Full Cycles" else "Registered Cycle Profile"
                    Text(
                        text = countText,
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = Color(0xFFA855F7)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Recorded battery charge-discharge cycles from kernel power supply driver.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Text(
                text = "What is a Battery Cycle?",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "One complete charge cycle occurs when you discharge an amount equal to 100% of your battery's total capacity. For example, consuming 40% today, recharging to 100%, and consuming 60% tomorrow counts as one complete cycle. Standard modern Lithium-ion cells retain ~80% capacity after 500 to 800 cycles.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ChargingHistoryDetailContent(
    sessions: List<ChargingSession>,
    selectedFilter: TimeRangeFilter = TimeRangeFilter.ALL_TIME,
    onFilterChange: (TimeRangeFilter) -> Unit = {},
    onExportCsv: () -> Unit = {},
    onExportJson: () -> Unit = {},
    onRestoreData: () -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current
    val filteredSessions = remember(sessions, selectedFilter) {
        val now = System.currentTimeMillis()
        val minTimeMs = when (selectedFilter) {
            TimeRangeFilter.DAY -> {
                val cal = java.util.Calendar.getInstance().apply {
                    set(java.util.Calendar.HOUR_OF_DAY, 0)
                    set(java.util.Calendar.MINUTE, 0)
                    set(java.util.Calendar.SECOND, 0)
                    set(java.util.Calendar.MILLISECOND, 0)
                }
                cal.timeInMillis
            }
            TimeRangeFilter.WEEK -> now - (7L * 24 * 3600 * 1000L)
            TimeRangeFilter.MONTH -> now - (30L * 24 * 3600 * 1000L)
            TimeRangeFilter.THREE_MONTHS -> now - (90L * 24 * 3600 * 1000L)
            TimeRangeFilter.ALL_TIME -> 0L
        }
        if (minTimeMs > 0L) {
            sessions.filter { it.startTimeMs >= minTimeMs }
        } else {
            sessions
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Export & Restore Action Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Button(
                onClick = onExportCsv,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
            ) {
                Text("CSV 📊", fontWeight = FontWeight.Bold, color = Color.White)
            }
            Button(
                onClick = onExportJson,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
            ) {
                Text("JSON 💾", fontWeight = FontWeight.Bold, color = Color.White)
            }
            Button(
                onClick = onRestoreData,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA855F7))
            ) {
                Text("Restore 📥", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
        // Time Filter Pills
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            TimeRangeFilter.entries.forEach { filter ->
                val isSelected = filter == selectedFilter
                Surface(
                    modifier = Modifier.clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onFilterChange(filter)
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                ) {
                    Text(
                        text = filter.label,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        if (filteredSessions.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No charging sessions recorded for ${selectedFilter.label}.\nConnect your charger to begin tracking!",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredSessions) { session ->
                    val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.US)
                    val dateStr = dateFormat.format(Date(session.startTimeMs))
                    val endLvlText = if (session.endLevel != null) "${session.endLevel}%" else "Ongoing"
                    val gainText = if (session.endLevel != null) "+${session.gainedPercent}%" else "In Progress"

                    ElevatedCard(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(dateStr, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                                Text(gainText, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold, color = Color(0xFF10B981)))
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Plug: ${session.plugType}  ·  ${session.startLevel}% → $endLvlText  ·  ${session.durationMinutes}m",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = String.format(Locale.US, "Added: %.0f mAh  ·  Peak: %.1f W  ·  Max: %.1f°C", session.chargeAddedMah, session.maxPowerWatts, session.maxTemperatureCelsius),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CurrentFlowDetailContent(info: BatteryInfo) {
    val colorScheme = MaterialTheme.colorScheme
    val isCharging = info.currentNowMa > 0
    val flowColor = if (isCharging) Color(0xFF10B981) else Color(0xFFFF9800)
    val sign = if (isCharging) "+" else ""

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. TOP HERO CARD
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceContainerHigh),
                border = BorderStroke(1.dp, flowColor.copy(alpha = 0.45f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = flowColor.copy(alpha = 0.18f),
                                modifier = Modifier.size(46.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (isCharging) AppIcons.ElectricBolt else AppIcons.Bedtime,
                                        contentDescription = null,
                                        tint = flowColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "$sign${info.currentNowMa} mA",
                                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                                    color = flowColor
                                )
                                Text(
                                    text = if (isCharging) "Charging Current Entering Battery" else "Active Hardware Current Drain",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = flowColor.copy(alpha = 0.18f),
                            border = BorderStroke(1.dp, flowColor.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = if (isCharging) "CHARGING" else "DISCHARGING",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, fontSize = 11.sp),
                                color = flowColor,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Instantaneous", style = MaterialTheme.typography.labelSmall, color = colorScheme.onSurfaceVariant)
                            Text("$sign${info.currentNowMa} mA", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = colorScheme.onSurface)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Average Current", style = MaterialTheme.typography.labelSmall, color = colorScheme.onSurfaceVariant)
                            Text("${info.currentAvgMa} mA", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = colorScheme.onSurface)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Power Transfer", style = MaterialTheme.typography.labelSmall, color = colorScheme.onSurfaceVariant)
                            Text(String.format(Locale.US, "%.1f W", info.powerWatts), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = flowColor)
                        }
                    }
                }
            }
        }

        // 2. DETAILED STAT CARDS (EVENLY SPACED)
        item {
            Text(
                text = "Current Telemetry Breakdown",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = colorScheme.onSurface
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                DetailStatCard("Instantaneous Electric Current", "$sign${info.currentNowMa} mA")
                DetailStatCard("Smoothed Average Current", "${info.currentAvgMa} mA")
                DetailStatCard("Current Direction", if (isCharging) "Net Energy Entering Battery" else "Hardware Current Draw")
                DetailStatCard("Power Transfer Rate", String.format(Locale.US, "%.2f Watts", info.powerWatts))
            }
        }

        // 3. EDUCATIONAL CARD
        item {
            ElevatedCard(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = colorScheme.surfaceContainerHigh)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = AppIcons.Info,
                            contentDescription = null,
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Understanding Current Flow (mA)",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "Milliamperes (mA) measures the rate of electrical charge flowing into or out of your phone's battery. Fast charging pumps positive current into the cell (e.g. +3,000 mA), whereas screen brightness, CPU load, and gaming draw negative current (e.g. -450 mA).",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp, lineHeight = 16.sp),
                        color = colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun VoltageDetailContent(info: BatteryInfo) {
    val colorScheme = MaterialTheme.colorScheme
    val voltageRatio = ((info.voltageVolts - 3.2f) / (4.45f - 3.2f)).toFloat().coerceIn(0f, 1f)
    val voltColor = Color(0xFF00E5FF)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. TOP HERO CARD
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceContainerHigh),
                border = BorderStroke(1.dp, voltColor.copy(alpha = 0.45f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = voltColor.copy(alpha = 0.18f),
                                modifier = Modifier.size(46.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = AppIcons.ElectricBolt,
                                        contentDescription = null,
                                        tint = voltColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = String.format(Locale.US, "%.3f Volts", info.voltageVolts),
                                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                                    color = colorScheme.onSurface
                                )
                                Text(
                                    text = "${info.voltageMv} Millivolts (mV)",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                    color = voltColor
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = voltColor.copy(alpha = 0.18f),
                            border = BorderStroke(1.dp, voltColor.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "LITHIUM CELL",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, fontSize = 11.sp),
                                color = voltColor,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        LinearProgressIndicator(
                            progress = { voltageRatio },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = voltColor,
                            trackColor = colorScheme.surfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("3.20 V (Cutoff)", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = colorScheme.onSurfaceVariant)
                            Text("3.85 V (Nominal)", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = colorScheme.onSurfaceVariant)
                            Text("4.45 V (Ceiling)", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        // 2. DETAILED STAT CARDS (EVENLY SPACED)
        item {
            Text(
                text = "Voltage Telemetry Breakdown",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = colorScheme.onSurface
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                DetailStatCard("Terminal Cell Voltage", String.format(Locale.US, "%.3f Volts", info.voltageVolts))
                DetailStatCard("Exact Millivolts (mV)", "${info.voltageMv} mV")
                DetailStatCard("Safe Operating Range", "3.20 V (empty) – 4.45 V (full)")
                val chemState = when {
                    info.voltageVolts >= 4.35 -> "Full Chemical Saturation (~100%)"
                    info.voltageVolts >= 4.10 -> "High Anode Potential (70% - 90%)"
                    info.voltageVolts >= 3.80 -> "Nominal Cell Plateau (30% - 70%)"
                    else -> "Low Reserve Envelope (< 30%)"
                }
                DetailStatCard("Chemical Potential State", chemState)
            }
        }

        // 3. EDUCATIONAL CARD
        item {
            ElevatedCard(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = colorScheme.surfaceContainerHigh)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = AppIcons.Info,
                            contentDescription = null,
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Voltage & Lithium Cell Chemistry",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "Lithium-ion cells have a chemical potential between ~3.4V (0%) and ~4.35V - 4.45V (100%). Operating near 4.4V for prolonged periods under high temperatures causes internal cathode degradation. Keeping charge between 20% and 80% (3.8V - 4.15V) significantly prolongs usable lifespan.",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp, lineHeight = 16.sp),
                        color = colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun TemperatureDetailContent(info: BatteryInfo, isFahrenheit: Boolean) {
    val colorScheme = MaterialTheme.colorScheme
    val tempStr = if (isFahrenheit) String.format(Locale.US, "%.1f °F", info.tempFahrenheit) else String.format(Locale.US, "%.1f °C", info.tempCelsius)
    val thermalColor = when {
        info.tempCelsius > 42f -> Color(0xFFEF4444)
        info.tempCelsius > 38f -> Color(0xFFFF9800)
        info.tempCelsius < 15f -> Color(0xFF00E5FF)
        else -> Color(0xFF10B981)
    }
    val stateLabel = when {
        info.tempCelsius > 42f -> "High Heat Warning"
        info.tempCelsius > 38f -> "Warm Charging Load"
        info.tempCelsius < 10f -> "Low Temperature Mobility"
        else -> "Optimal Thermal Envelope"
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. TOP HERO CARD
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceContainerHigh),
                border = BorderStroke(1.dp, thermalColor.copy(alpha = 0.45f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = thermalColor.copy(alpha = 0.18f),
                                modifier = Modifier.size(46.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = AppIcons.Thermostat,
                                        contentDescription = null,
                                        tint = thermalColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = tempStr,
                                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                                    color = colorScheme.onSurface
                                )
                                Text(
                                    text = stateLabel,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                    color = thermalColor
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = thermalColor.copy(alpha = 0.18f),
                            border = BorderStroke(1.dp, thermalColor.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = if (info.tempCelsius <= 38f) "SAFE" else "WARM",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, fontSize = 11.sp),
                                color = thermalColor,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        LinearProgressIndicator(
                            progress = { (info.tempCelsius / 50f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = thermalColor,
                            trackColor = colorScheme.surfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("10°C (Cold)", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = colorScheme.onSurfaceVariant)
                            Text("35°C (Target)", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = colorScheme.onSurfaceVariant)
                            Text("45°C (Limit)", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        // 2. DETAILED STAT CARDS (EVENLY SPACED)
        item {
            Text(
                text = "Thermal Metrics & Limits",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = colorScheme.onSurface
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                DetailStatCard("Battery Temperature (Celsius)", String.format(Locale.US, "%.1f °C", info.tempCelsius))
                DetailStatCard("Battery Temperature (Fahrenheit)", String.format(Locale.US, "%.1f °F", info.tempFahrenheit))
                DetailStatCard("Thermal Stress Classification", stateLabel)
                DetailStatCard("Recommended Maximum Ceiling", "40.0 °C (104.0 °F)")
                DetailStatCard("Safe Operational Envelope", "20.0 °C – 38.0 °C")
            }
        }

        // 3. EDUCATIONAL CARD
        item {
            ElevatedCard(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = colorScheme.surfaceContainerHigh)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = AppIcons.Info,
                            contentDescription = null,
                            tint = Color(0xFFFF9800),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Thermal Protection & Battery Longevity",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "Heat is the #1 enemy of Lithium battery health. Charging above 40°C (104°F) accelerates solid-electrolyte interphase (SEI) layer growth, reducing capacity permanently. Never leave your phone charging under pillows or in direct sunlight.",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp, lineHeight = 16.sp),
                        color = colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun PowerSourceDetailContent(info: BatteryInfo) {
    val colorScheme = MaterialTheme.colorScheme
    val speedCat = info.chargingSpeedCategory
    val pwrColor = if (info.isCharging) Color(speedCat.badgeColorHex) else Color(0xFF64748B)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. TOP HERO CARD
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceContainerHigh),
                border = BorderStroke(1.dp, pwrColor.copy(alpha = 0.45f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = pwrColor.copy(alpha = 0.18f),
                                modifier = Modifier.size(46.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (info.isCharging) AppIcons.Power else AppIcons.BatteryFull,
                                        contentDescription = null,
                                        tint = pwrColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = info.pluggedSource.name,
                                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                                    color = colorScheme.onSurface
                                )
                                Text(
                                    text = if (info.isCharging) info.chargerCapacity else "Operating on Internal Cell",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                    color = pwrColor
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = pwrColor.copy(alpha = 0.18f),
                            border = BorderStroke(1.dp, pwrColor.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = speedCat.label.uppercase(Locale.US),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, fontSize = 11.sp),
                                color = pwrColor,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Delivery Power", style = MaterialTheme.typography.labelSmall, color = colorScheme.onSurfaceVariant)
                            Text(String.format(Locale.US, "%.1f W", info.powerWatts), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = colorScheme.onSurface)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Voltage", style = MaterialTheme.typography.labelSmall, color = colorScheme.onSurfaceVariant)
                            Text(String.format(Locale.US, "%.2f V", info.voltageVolts), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = colorScheme.onSurface)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Current", style = MaterialTheme.typography.labelSmall, color = colorScheme.onSurfaceVariant)
                            val sign = if (info.currentNowMa > 0) "+" else ""
                            Text("$sign${info.currentNowMa} mA", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = pwrColor)
                        }
                    }
                }
            }
        }

        // 2. DETAILED STAT CARDS (EVENLY SPACED)
        item {
            Text(
                text = "Charger Hardware Specifications",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = colorScheme.onSurface
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                DetailStatCard("Power Plug Connection", info.pluggedSource.name)
                if (info.isCharging) {
                    DetailStatCard("Charger Capacity Rating", info.chargerCapacity)
                    if (info.chargerProtocol != "Standard Charger" && info.chargerProtocol.isNotBlank()) {
                        DetailStatCard("Fast Charging Protocol", info.chargerProtocol)
                    }
                }
                DetailStatCard("Charging Speed Classification", speedCat.label)
                DetailStatCard("Live Power Intake", String.format(Locale.US, "%.2f Watts", info.powerWatts))
                DetailStatCard("Terminal Cell Voltage", String.format(Locale.US, "%.3f V (%d mV)", info.voltageVolts, info.voltageMv))
                DetailStatCard("Live Electric Current Flow", "${info.currentNowMa} mA")
                DetailStatCard("Battery Chemistry Technology", info.technology)
            }
        }

        // 3. EDUCATIONAL CARD
        item {
            ElevatedCard(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = colorScheme.surfaceContainerHigh)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = AppIcons.Info,
                            contentDescription = null,
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Power Negotiation & Charging Safety",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "The Battery Management System (BMS) continuously negotiates current and voltage dynamically with the attached power supply. Fast chargers start at peak wattage while battery percentage is low, and taper down gradually as cell saturation approaches 80%-100% to protect electrolyte stability.",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp, lineHeight = 16.sp),
                        color = colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun DetailStatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    valueColor: Color? = null
) {
    val isLongValue = value.length > 24 || value.contains("\n") || (label.length + value.length > 38)

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        if (isLongValue) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (icon != null) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = valueColor ?: MaterialTheme.colorScheme.onSurface
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1.1f)
                ) {
                    if (icon != null) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                    }
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = value,
                    modifier = Modifier.weight(0.9f, fill = false),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = valueColor ?: MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

@Composable
fun AppDrainDetailContent(
    appDrainList: List<AppDrainItem>,
    hasUsageStatsPermission: Boolean,
    onRequestPermission: () -> Unit,
    designCapacityMah: Int
) {
    if (!hasUsageStatsPermission) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = Color(0xFFF59E0B).copy(alpha = 0.2f),
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = AppIcons.Shield,
                        contentDescription = null,
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Usage Access Permission Required",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Android protects application runtime statistics for user privacy. To monitor foreground runtimes and calculate accurate per-app energy attribution, please grant Usage Access in System Settings.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onRequestPermission,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00E5FF),
                    contentColor = Color(0xFF080C14)
                )
            ) {
                Text("Grant Usage Access in Settings ➔", fontWeight = FontWeight.Bold)
            }
        }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Screen-On Energy Attribution",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Past 24 Hours • ${appDrainList.size} Active Applications",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            if (appDrainList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No foreground usage detected in the last 24 hours.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(appDrainList) { app ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val bitmap = remember(app.appIcon) {
                                    try {
                                        app.appIcon?.let { drawableToBitmap(it) }
                                    } catch (_: Exception) {
                                        null
                                    }
                                }

                                if (bitmap != null) {
                                    Image(
                                        bitmap = bitmap.asImageBitmap(),
                                        contentDescription = app.appName,
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                    )
                                } else {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                        modifier = Modifier.size(42.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = app.appName.take(1),
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = app.appName,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${app.foregroundFormatted} active",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = String.format(Locale.US, "%.1f%%", app.drainPercentOfTotal),
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                                        color = Color(0xFF00E5FF)
                                    )
                                    Text(
                                        text = String.format(Locale.US, "~%.0f mAh", app.estimatedMah),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            LinearProgressIndicator(
                                progress = { (app.drainPercentOfTotal / 100f).coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(5.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = Color(0xFF00E5FF),
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BatteryDrainDetailContent(
    drainStats: BatteryDrainStats,
    onResetStats: () -> Unit,
    onOpenAppDrain: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. LIVE STATUS BANNER
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceContainerHigh),
                border = androidx.compose.foundation.BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (drainStats.isScreenOn) Color(0xFFFF9800).copy(alpha = 0.16f) else Color(0xFF00E5FF).copy(alpha = 0.16f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (drainStats.isScreenOn) Color(0xFFFF9800).copy(alpha = 0.45f) else Color(0xFF00E5FF).copy(alpha = 0.45f)
                            )
                        ) {
                            Text(
                                text = if (drainStats.isScreenOn) "⚡ SCREEN ACTIVE (ON)" else "🌙 STANDBY SLEEP (OFF)",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, fontSize = 11.sp),
                                color = if (drainStats.isScreenOn) Color(0xFFFF9800) else Color(0xFF00E5FF),
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                            )
                        }

                        Text(
                            text = "${drainStats.batteryLevel}% Battery",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Live Discharge Current",
                                style = MaterialTheme.typography.labelSmall,
                                color = colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${kotlin.math.abs(drainStats.liveCurrentMa)} mA",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = colorScheme.onSurface
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Instant Power Draw",
                                style = MaterialTheme.typography.labelSmall,
                                color = colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = String.format(Locale.US, "%.2f W", drainStats.livePowerWatts),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // 2. ACTIVE (SCREEN-ON) DRAIN METRICS CARD
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceContainerHigh),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF9800).copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFFF9800).copy(alpha = 0.16f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = AppIcons.ElectricBolt,
                                        contentDescription = null,
                                        tint = Color(0xFFFF9800),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Active Drain Rate",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                    color = colorScheme.onSurface
                                )
                                Text(
                                    text = "Screen-On discharge speed",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Text(
                            text = String.format(Locale.US, "%.1f%%/h", drainStats.activeDrainRatePerHour),
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = Color(0xFFFF9800)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LinearProgressIndicator(
                        progress = { (drainStats.activeDrainRatePerHour / 30f).coerceIn(0.08f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = Color(0xFFFF9800),
                        trackColor = colorScheme.surfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Screen-On Time", style = MaterialTheme.typography.labelSmall, color = colorScheme.onSurfaceVariant)
                            Text(drainStats.formattedScreenOnTime, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = colorScheme.onSurface)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Active Drop", style = MaterialTheme.typography.labelSmall, color = colorScheme.onSurfaceVariant)
                            Text("-${drainStats.screenOnDropPercent}%", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Color(0xFFFF9800))
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Projected SOT", style = MaterialTheme.typography.labelSmall, color = colorScheme.onSurfaceVariant)
                            Text(String.format(Locale.US, "%.1f hrs", drainStats.estimatedScreenOnHours), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = colorScheme.onSurface)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Average active load: ~${drainStats.activeCurrentMa} mA. At this rate, full battery gives ~${String.format(Locale.US, "%.1f", 100f / drainStats.activeDrainRatePerHour)} hours continuous screen time.",
                            style = MaterialTheme.typography.labelSmall,
                            color = colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // 3. IDLE (SCREEN-OFF) DRAIN METRICS CARD
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceContainerHigh),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF38BDF8).copy(alpha = 0.16f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = AppIcons.Bedtime,
                                        contentDescription = null,
                                        tint = Color(0xFF38BDF8),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Idle Drain Rate",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                    color = colorScheme.onSurface
                                )
                                Text(
                                    text = "Screen-Off standby speed",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Text(
                            text = String.format(Locale.US, "%.2f%%/h", drainStats.idleDrainRatePerHour),
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = Color(0xFF38BDF8)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LinearProgressIndicator(
                        progress = { (drainStats.idleDrainRatePerHour / 3f).coerceIn(0.05f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = Color(0xFF38BDF8),
                        trackColor = colorScheme.surfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Standby Time", style = MaterialTheme.typography.labelSmall, color = colorScheme.onSurfaceVariant)
                            Text(drainStats.formattedScreenOffTime, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = colorScheme.onSurface)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Standby Drop", style = MaterialTheme.typography.labelSmall, color = colorScheme.onSurfaceVariant)
                            Text("-${drainStats.screenOffDropPercent}%", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Color(0xFF38BDF8))
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Pure Standby", style = MaterialTheme.typography.labelSmall, color = colorScheme.onSurfaceVariant)
                            Text(String.format(Locale.US, "%.0f hrs", drainStats.estimatedStandbyHours), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = colorScheme.onSurface)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Standby sleep load: ~${drainStats.idleCurrentMa} mA. Normal target is < 1.0%/hr. Good overnight standby is 3-6% drop in 8 hours.",
                            style = MaterialTheme.typography.labelSmall,
                            color = colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // 4. DEEP SLEEP & HARDWARE EFFICIENCY CARD
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceContainerHigh),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF10B981).copy(alpha = 0.16f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = AppIcons.Shield,
                                        contentDescription = null,
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Deep Sleep Ratio",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                    color = colorScheme.onSurface
                                )
                                Text(
                                    text = "Hardware CPU low-power state",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Text(
                            text = String.format(Locale.US, "%.1f%%", drainStats.deepSleepPercentage),
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = Color(0xFF10B981)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LinearProgressIndicator(
                        progress = { (drainStats.deepSleepPercentage / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = Color(0xFF10B981),
                        trackColor = colorScheme.surfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    val (verdictTitle, verdictBadgeColor) = when {
                        drainStats.deepSleepPercentage >= 90f -> "Optimal Deep Sleep — Minimal WakeLocks" to Color(0xFF10B981)
                        drainStats.deepSleepPercentage >= 75f -> "Normal Standby — Mild background sync" to Color(0xFF00E5FF)
                        drainStats.deepSleepPercentage >= 50f -> "Moderate Standby Drain — Apps waking CPU" to Color(0xFFFF9800)
                        else -> "High Standby Drain — Rogue background WakeLocks" to Color(0xFFEF4444)
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = verdictBadgeColor.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, verdictBadgeColor.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Status: $verdictTitle (${drainStats.formattedDeepSleepTime} deep sleep)",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = verdictBadgeColor,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // 5. COMBINED REALISTIC DAILY PROJECTION CARD
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceContainerHigh),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA855F7).copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFA855F7).copy(alpha = 0.16f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = AppIcons.Speed,
                                        contentDescription = null,
                                        tint = Color(0xFFA855F7),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Combined Routine Life",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                    color = colorScheme.onSurface
                                )
                                Text(
                                    text = "30% Active + 70% Standby daily mix",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Text(
                            text = String.format(Locale.US, "%.1f hrs", drainStats.combinedEstimatedHours),
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = Color(0xFFA855F7)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Composite drain rate: ~${String.format(Locale.US, "%.1f", drainStats.combinedDrainRatePerHour)}%/hr across day & night. This projection dynamically adjusts as your screen time and standby habits evolve.",
                        style = MaterialTheme.typography.labelSmall,
                        color = colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 6. HOW DRAIN CALCULATION WORKS
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceContainerLow),
                border = androidx.compose.foundation.BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.18f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ℹ️ How Battery Drain Is Tracked",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Active Drain: Measured from percentage drops and instant milliampere current draws when the screen is interactive and unlocked.\n\n• Idle Standby: Measured while the screen is locked and in pocket/sleep mode to show overnight efficiency.\n\n• Deep Sleep: Calculated using hardware monotonic timers (elapsedRealtime vs uptimeMillis). When the CPU is suspended, uptime stops counting while real time advances.",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp, lineHeight = 16.sp),
                        color = colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 7. ACTION BUTTONS: RESET & PER-APP DRAIN
        item {
            val haptic = LocalHapticFeedback.current
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onResetStats()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.surfaceContainerHigh,
                        contentColor = colorScheme.onSurface
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.3f))
                ) {
                    Icon(
                        imageVector = AppIcons.Refresh,
                        contentDescription = "Reset Stats",
                        tint = colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Reset Stats",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onOpenAppDrain()
                    },
                    modifier = Modifier.weight(1.3f),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00E5FF).copy(alpha = 0.16f),
                        contentColor = Color(0xFF00E5FF)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.45f))
                ) {
                    Icon(
                        imageVector = AppIcons.Bolt,
                        contentDescription = "Per-App Drain",
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Per-App Drain ➔",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

@Composable
fun DeviceInfoDetailContent(info: BatteryInfo, healthDetails: BatteryHealthDetails) {
    val context = LocalContext.current
    var deviceReport by remember { mutableStateOf(DeviceInfoProvider.getDeviceReport(context)) }
    val haptic = LocalHapticFeedback.current

    // Periodically refresh dynamic live stats (CPU load, frequencies, and thermal zones)
    LaunchedEffect(Unit) {
        while (true) {
            delay(1500)
            try {
                deviceReport = DeviceInfoProvider.getDeviceReport(context)
            } catch (_: Exception) {}
        }
    }

    val categories = remember { listOf("SOC", "DEVICE", "SYSTEM", "BATTERY", "THERMAL", "SENSORS", "CAMERAS") }
    val pagerState = rememberPagerState(pageCount = { categories.size })
    val coroutineScope = rememberCoroutineScope()
    val chipScrollState = rememberLazyListState()

    LaunchedEffect(pagerState.currentPage) {
        try {
            chipScrollState.animateScrollToItem(pagerState.currentPage)
        } catch (_: Exception) {}
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // 1. Hero Device Identity Banner (Pinned cleanly above tabs)
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.72f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF00E5FF).copy(alpha = 0.16f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = AppIcons.PhoneAndroid,
                            contentDescription = null,
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = deviceReport.identity.marketingName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${deviceReport.identity.manufacturer} · Model: ${deviceReport.identity.model}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF00E5FF).copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "Codename: ${deviceReport.identity.codename}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                                color = Color(0xFF00E5FF),
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFA855F7).copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "Uptime: ${deviceReport.identity.uptimeFormatted}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                                color = Color(0xFFA855F7),
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        // 2. Horizontal Scrollable Category Filter Chips (Synchronized with Pager)
        LazyRow(
            state = chipScrollState,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(categories) { index, cat ->
                val isSelected = (pagerState.currentPage == index)
                val chipBg = if (isSelected) Color(0xFF00E5FF) else MaterialTheme.colorScheme.surfaceContainerHigh
                val chipText = if (isSelected) Color(0xFF070B14) else MaterialTheme.colorScheme.onSurface

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = chipBg,
                    modifier = Modifier.clickable {
                        try {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        } catch (_: Exception) {}
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    border = BorderStroke(1.dp, if (isSelected) Color(0xFF00E5FF) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                ) {
                    Text(
                        text = cat,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                        ),
                        color = chipText,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                    )
                }
            }
        }

        // 3. Swipeable HorizontalPager hosting all 7 tabs
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) { pageIndex ->
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // ==================== TAB: SOC ====================
                if (categories[pageIndex] == "SOC") {
                // Live CPU Load Gauge Card
                item {
                    ElevatedCard(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = AppIcons.Cpu,
                                        contentDescription = null,
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "CPU Load & Frequency Meter",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (deviceReport.soc.cpuUsagePercent > 70) Color(0xFFEF4444).copy(alpha = 0.2f) else Color(0xFF10B981).copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "${deviceReport.soc.cpuUsagePercent}% Load",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                        color = if (deviceReport.soc.cpuUsagePercent > 70) Color(0xFFEF4444) else Color(0xFF10B981),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            // Dynamic CPU Load Bar
                            val cpuUsageProgress = (deviceReport.soc.cpuUsagePercent.toFloat() / 100f).coerceIn(0.04f, 1f)
                            LinearProgressIndicator(
                                progress = { cpuUsageProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(5.dp)),
                                color = if (deviceReport.soc.cpuUsagePercent > 70) Color(0xFFEF4444) else if (deviceReport.soc.cpuUsagePercent > 40) Color(0xFFF59E0B) else Color(0xFF10B981),
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )

                            // Clusters Summary
                            Text(
                                text = "CPU Architecture & Clusters",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            deviceReport.soc.clusters.forEach { cluster ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = cluster.name,
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "${cluster.coreCount} Cores (Cores ${cluster.cores.joinToString(", ")}) · ${cluster.architectureName}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Text(
                                            text = "${cluster.minFreqMhz} - ${cluster.maxFreqMhz} MHz",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                                            color = Color(0xFF00E5FF)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Individual Core Frequencies (Live Kernel EAS: "Stopped" if sleeping)
                item {
                    ElevatedCard(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Live Per-Core Real-Time Frequencies",
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${deviceReport.soc.coresCount} Physical Cores",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            deviceReport.soc.cores.forEach { core ->
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Core #${core.coreIndex} (${core.governor})",
                                            modifier = Modifier.weight(1f),
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = if (!core.isOnline) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (!core.isOnline) "Stopped" else if (core.curFreqMhz > 0) "${core.curFreqMhz} MHz / ${core.maxFreqMhz} MHz" else "${core.maxFreqMhz} MHz (Dynamic)",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                            color = if (!core.isOnline) Color(0xFFEF4444) else Color(0xFF00E5FF)
                                        )
                                    }
                                    val coreFrac = if (!core.isOnline) 0f else if (core.maxFreqMhz > 0 && core.curFreqMhz > 0) (core.curFreqMhz.toFloat() / core.maxFreqMhz.toFloat()).coerceIn(0f, 1f) else 0.4f
                                    LinearProgressIndicator(
                                        progress = { coreFrac },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(5.dp)
                                            .clip(RoundedCornerShape(3.dp)),
                                        color = if (!core.isOnline) Color(0xFFEF4444) else Color(0xFF00E5FF),
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // SoC Specifications Card
                item {
                    ElevatedCard(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "SoC & Silicon Specifications",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            DetailStatCard("Processor Manufacturer", deviceReport.soc.manufacturer)
                            DetailStatCard("Chipset Model", deviceReport.soc.chipsetName)
                            if (deviceReport.soc.processNode.isNotBlank()) {
                                DetailStatCard("Fabrication Process Node", deviceReport.soc.processNode)
                            }
                            DetailStatCard("CPU Architecture", deviceReport.soc.architecture)
                            DetailStatCard("CPU Revision", deviceReport.soc.cpuRevision)
                            DetailStatCard("Total Physical Cores", "${deviceReport.soc.coresCount} Cores")
                            DetailStatCard("Clock Speed Range", deviceReport.soc.clockSpeedRange)
                            DetailStatCard("Instruction Sets / ABIs", deviceReport.soc.supportedAbis)
                            DetailStatCard("Hardware Board Platform", deviceReport.identity.board)
                        }
                    }
                }

                // GPU & 3D Accelerator Card
                item {
                    ElevatedCard(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = AppIcons.Speed,
                                    contentDescription = null,
                                    tint = Color(0xFFA855F7),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "GPU & 3D Graphics Accelerator",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            DetailStatCard("GPU Vendor", deviceReport.gpu.vendor)
                            DetailStatCard("GPU Renderer", deviceReport.gpu.renderer)
                            if (deviceReport.gpu.clockSpeed.isNotBlank()) {
                                DetailStatCard("GPU Clock / Load", deviceReport.gpu.clockSpeed)
                            }
                            DetailStatCard("OpenGL ES API", deviceReport.gpu.openGlVersion)
                            DetailStatCard("Vulkan API Support", deviceReport.gpu.vulkanVersion)
                        }
                    }
                }
            }

            // ==================== TAB: DEVICE ====================
            if (categories[pageIndex] == "DEVICE") {
                // Identity Specs
                item {
                    ElevatedCard(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "Device Identity & Hardware Model",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            DetailStatCard("Device Commercial Name", deviceReport.identity.marketingName)
                            DetailStatCard("Hardware Model", deviceReport.identity.model)
                            DetailStatCard("Device Manufacturer", deviceReport.identity.manufacturer)
                            DetailStatCard("Brand Name", deviceReport.identity.brand)
                            DetailStatCard("Motherboard Platform", deviceReport.identity.board)
                            DetailStatCard("Hardware Identifier", deviceReport.identity.hardware)
                            DetailStatCard("Device Codename", deviceReport.identity.codename)
                            DetailStatCard("Product Name", deviceReport.identity.product)
                        }
                    }
                }

                // Display Card
                item {
                    ElevatedCard(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = AppIcons.Display,
                                    contentDescription = null,
                                    tint = Color(0xFF00E5FF),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Screen & Display Panel",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            DetailStatCard("Screen Physical Size", deviceReport.display.screenDiagonalInches)
                            DetailStatCard("Display Resolution", deviceReport.display.resolution)
                            DetailStatCard("Current Refresh Rate", "${deviceReport.display.refreshRateHz} Hz")
                            DetailStatCard("Supported Refresh Rates", deviceReport.display.supportedRefreshRates.joinToString(", ") { "$it Hz" })
                            DetailStatCard("Pixel Density", "${deviceReport.display.densityDpi} DPI (${deviceReport.display.densityCategory})")
                            DetailStatCard("Aspect Ratio", deviceReport.display.aspectRatio)
                            DetailStatCard("HDR Dynamic Range", deviceReport.display.hdrSupport)
                            DetailStatCard("Wide Color Gamut (DCI-P3)", if (deviceReport.display.isWideColorGamut) "Supported (10-bit Color)" else "Standard sRGB")
                        }
                    }
                }

                // Memory & Storage Subsystem
                item {
                    ElevatedCard(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = AppIcons.Memory,
                                        contentDescription = null,
                                        tint = Color(0xFF00E5FF),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Memory & Storage Subsystem",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF00E5FF).copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "${deviceReport.memoryStorage.ramUsagePercent}% RAM Used",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                                        color = Color(0xFF00E5FF),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            // RAM Meter
                            val mem = deviceReport.memoryStorage
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(
                                        text = "Total RAM (${mem.ramUsagePercent}% Used)",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "${mem.ramTotalMb} MB (${String.format(Locale.US, "%.2f GB", mem.ramTotalGb)})",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF00E5FF)
                                    )
                                }
                                LinearProgressIndicator(
                                    progress = { (mem.ramUsagePercent.toFloat() / 100f).coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = Color(0xFF00E5FF),
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(
                                        text = "Available: ${mem.ramAvailMb} MB (${String.format(Locale.US, "%.2f GB", mem.ramAvailGb)})",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Used: ${mem.ramUsedMb} MB",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                        color = Color(0xFF00E5FF)
                                    )
                                }
                            }

                            // Storage Meter
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(
                                        text = "Internal Storage (${mem.storageUsagePercent}% Used)",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = String.format(Locale.US, "%.2f / %.2f GB", mem.storageUsedGb, mem.storageTotalGb),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFFA855F7)
                                    )
                                }
                                LinearProgressIndicator(
                                    progress = { (mem.storageUsagePercent.toFloat() / 100f).coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = Color(0xFFA855F7),
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(
                                        text = String.format(Locale.US, "Available: %.2f GB Free", mem.storageFreeGb),
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Block Size: ${mem.storageBlockSize} B",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                        color = Color(0xFFA855F7)
                                    )
                                }
                            }

                            DetailStatCard("Memory Low State", if (mem.isLowMemory) "Low Memory Warning" else "Normal (Sufficient Headroom)")
                            DetailStatCard("Low Memory Threshold", "${mem.ramThresholdMb} MB")
                            DetailStatCard("JVM Max Runtime Heap", "${mem.jvmMaxHeapMb} MB")
                        }
                    }
                }
            }

            // ==================== TAB: SYSTEM ====================
            if (categories[pageIndex] == "SYSTEM") {
                item {
                    ElevatedCard(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "Operating System & Platform Software",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            DetailStatCard("Android Version", "Android ${deviceReport.os.androidVersion} (${deviceReport.os.releaseCodename})")
                            DetailStatCard("SDK API Level", "API ${deviceReport.os.apiLevel}")
                            DetailStatCard("Security Patch Level", deviceReport.os.securityPatch)
                            DetailStatCard("Bootloader Version", deviceReport.identity.bootloader)
                            DetailStatCard("Build Release ID", deviceReport.os.buildId)
                            DetailStatCard("Build Fingerprint", deviceReport.identity.fingerprint)
                            DetailStatCard("Linux Kernel Architecture", deviceReport.os.kernelArchitecture)
                            DetailStatCard("Kernel Version", deviceReport.os.kernelVersion)
                            DetailStatCard("Java Runtime / ART VM", deviceReport.os.javaVmVersion)
                            DetailStatCard("Root Access", deviceReport.os.rootAccess)
                            DetailStatCard("Project Treble Architecture", if (deviceReport.os.isTrebleEnabled) "Enabled & Treble Compliant" else "Legacy Architecture")
                            DetailStatCard("Radio / Baseband Firmware", deviceReport.identity.radioVersion)
                            DetailStatCard("System Uptime", deviceReport.os.systemUptime)
                        }
                    }
                }
            }

            // ==================== TAB: BATTERY ====================
            if (categories[pageIndex] == "BATTERY") {
                item {
                    ElevatedCard(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = AppIcons.ElectricBolt,
                                    contentDescription = null,
                                    tint = Color(0xFF00E5FF),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Battery Status & Charging Hardware",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            DetailStatCard("Battery Health Status", info.healthStatus.name.lowercase().replaceFirstChar { it.titlecase(Locale.US) })
                            DetailStatCard("Battery Percentage Level", "${info.level}%")
                            DetailStatCard("Power Plug State", if (info.isCharging) "Charging (${info.pluggedSource.name})" else "Discharging")
                            DetailStatCard("Power Connection Source", info.pluggedSource.name)
                            DetailStatCard("Charger Capacity", info.chargerCapacity)
                            if (info.chargerProtocol != "Standard Charger" && info.chargerProtocol.isNotBlank()) {
                                DetailStatCard("Charger Protocol", info.chargerProtocol)
                            }
                            if (info.maxChargerWatts > 0) {
                                DetailStatCard("Negotiated Charger Output", "${info.maxChargerWatts} Watts (${info.chargerRatingLabel})")
                            }
                            DetailStatCard("Battery Chemistry", info.technology)
                            DetailStatCard("Design Factory Capacity", "${info.designCapacityMah} mAh")
                            DetailStatCard("Calibrated Estimated Capacity", "${healthDetails.estimatedCapacityMah} mAh (${healthDetails.healthScorePercent ?: info.level}%)")
                            DetailStatCard("Health Precision Rating", healthDetails.confidenceRating)
                            DetailStatCard("Terminal Cell Voltage", String.format(Locale.US, "%.3f V (%d mV)", info.voltageVolts, info.voltageMv))
                            DetailStatCard("Live Current Flow Rate", "${info.currentNowMa} mA (${String.format(Locale.US, "%.1f W", info.powerWatts)})")
                            DetailStatCard("Cell Temperature", String.format(Locale.US, "%.1f °C / %.1f °F (%s)", info.tempCelsius, info.tempFahrenheit, info.temperatureState.label))
                        }
                    }
                }
            }

            // ==================== TAB: THERMAL (Thermal Sensor Zones) ====================
            if (categories[pageIndex] == "THERMAL") {
                item {
                    ElevatedCard(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = AppIcons.Thermostat,
                                        contentDescription = null,
                                        tint = Color(0xFFFF5722),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Thermal Zones Monitoring",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFFF5722).copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "${deviceReport.thermals.size} Zones Active",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color(0xFFFF5722),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Text(
                                text = "Live thermal telemetry read from Linux kernel /sys/class/thermal/ subsystem.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            deviceReport.thermals.forEach { zone ->
                                val tempColor = when {
                                    zone.tempCelsius >= 55f -> Color(0xFFEF4444)
                                    zone.tempCelsius >= 40f -> Color(0xFFF59E0B)
                                    else -> Color(0xFF10B981)
                                }

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = zone.name,
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = zone.category,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = tempColor.copy(alpha = 0.16f)
                                        ) {
                                            Text(
                                                text = String.format(Locale.US, "%.1f °C", zone.tempCelsius),
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                                                color = tempColor,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ==================== TAB: SENSORS (Hardware & Inertial Sensors) ====================
            if (categories[pageIndex] == "SENSORS") {
                // Sensor Connectivity Feature Overview
                item {
                    ElevatedCard(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "Biometrics & Wireless Subsystems",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            val sens = deviceReport.sensors
                            DetailStatCard("Total Hardware Sensors", "${sens.totalSensorsCount} Integrated Sensors")
                            DetailStatCard("Fingerprint Biometric Scanner", if (sens.hasFingerprint) "Present (Under-Display / Capacitive)" else "Not Present")
                            DetailStatCard("Face Recognition Unlock", if (sens.hasFaceUnlock) "Present (Hardware Face Biometric)" else "Standard")
                            DetailStatCard("NFC (Near Field Communication)", if (sens.hasNfc) "Supported (Google Wallet / Contactless)" else "Not Supported")
                            DetailStatCard("Barometer (Atmospheric Pressure)", if (sens.hasBarometer) "Hardware Barometer Present" else "Not Present")
                            DetailStatCard("Gyroscope & Compass", if (sens.hasGyroscope && sens.hasCompass) "6-Axis Gyro + 3-Axis Magnetometer" else "Accelerometer Only")
                            DetailStatCard("Step Counter Hardware", if (sens.hasStepCounter) "Hardware Step Detector Present" else "Software Tracking")
                            DetailStatCard("Wi-Fi Standard", sens.wifiStandard)
                            DetailStatCard("Bluetooth Protocol", sens.bluetoothVersion)
                            DetailStatCard("Cellular Network Support", sens.cellularGenerations)
                        }
                    }
                }

                // Full Hardware Sensors List
                item {
                    ElevatedCard(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Installed Hardware Sensors",
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${deviceReport.sensors.sensorList.size} Listed",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF00E5FF)
                                )
                            }

                            deviceReport.sensors.sensorList.forEach { sensor ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = sensor.typeName,
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                color = Color(0xFF00E5FF)
                                            )
                                            Text(
                                                text = String.format(Locale.US, "%.3f mA", sensor.powerMa),
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        Text(
                                            text = sensor.name,
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "Vendor: ${sensor.vendor}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = "Res: ${sensor.resolution}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ==================== TAB: CAMERAS ====================
            if (categories[pageIndex] == "CAMERAS") {
                item {
                    ElevatedCard(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = AppIcons.Camera,
                                        contentDescription = null,
                                        tint = Color(0xFFFF9800),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Camera Array Hardware",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFFF9800).copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "${deviceReport.cameras.size} Sensors",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color(0xFFFF9800),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            // Hardware Resolution & Pixel Binning Notice
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        imageVector = AppIcons.Info,
                                        contentDescription = null,
                                        tint = Color(0xFFFF9800),
                                        modifier = Modifier.size(18.dp).padding(top = 2.dp)
                                    )
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(
                                            text = "Sensor Resolution & Pixel Binning Note",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Resolutions displayed reflect active output capture streams reported by the Android Camera2 API. Smartphone camera sensors commonly use Quad-Bayer or Nona-Bayer pixel binning (e.g., 48MP, 64MP, 108MP, or 200MP binned 4:1 or 9:1 down to 12MP/16MP) executed inside the proprietary ISP/DSP without exposing unbinned hardware metadata or proprietary sensor part numbers.",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 15.sp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            deviceReport.cameras.forEachIndexed { idx, cam ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = cam.roleTitle,
                                                modifier = Modifier.weight(1f),
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                color = if (cam.isFront) Color(0xFFA855F7) else Color(0xFFFF9800)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = String.format(Locale.US, "%.1f Megapixels", cam.megapixels),
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        DetailStatCard("Active Stream Resolution", cam.resolutionPx)
                                        DetailStatCard("Pixel Pitch & Binning", cam.pixelPitchMicrons)
                                        DetailStatCard("Lens Aperture", cam.aperture)
                                        DetailStatCard("Focal Length", cam.focalLengthMm)
                                        DetailStatCard("Field of View (FOV)", cam.fieldOfViewDegrees)
                                        DetailStatCard("Physical Sensor Dimensions", cam.physicalSizeMm)
                                        DetailStatCard("Hardware Camera2 Level", cam.hardwareSupportLevel)
                                        DetailStatCard("ISO Sensitivity Range", cam.isoRange)
                                        DetailStatCard("Shutter / Exposure Time", cam.exposureTimeRange)
                                        DetailStatCard("Focus System", cam.focusModes)
                                        DetailStatCard("Optical Image Stabilization (OIS)", if (cam.hasOis) "✓ Hardware OIS Enabled" else "Electronic / EIS")
                                        DetailStatCard("Zoom Ratio Range", cam.zoomRange)
                                        DetailStatCard("RAW Sensor Capture", cam.rawCaptureSupport)
                                        DetailStatCard("Flash Capability", if (cam.hasFlash) "Supported (True Tone LED)" else "Screen Flash")
                                        DetailStatCard("Max Video Capabilities", cam.videoCapabilities)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Bottom padding spacer
            item {
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}
}

fun drawableToBitmap(drawable: android.graphics.drawable.Drawable): android.graphics.Bitmap {
    val targetWidth = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth.coerceIn(48, 128) else 96
    val targetHeight = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight.coerceIn(48, 128) else 96

    if (drawable is android.graphics.drawable.BitmapDrawable && drawable.bitmap != null) {
        val b = drawable.bitmap
        if (b.width in 48..128 && b.height in 48..128) return b
        return android.graphics.Bitmap.createScaledBitmap(b, targetWidth, targetHeight, true)
    }

    val bitmap = android.graphics.Bitmap.createBitmap(targetWidth, targetHeight, android.graphics.Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}

