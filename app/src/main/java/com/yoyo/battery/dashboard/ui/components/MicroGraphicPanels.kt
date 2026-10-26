package com.yoyo.battery.dashboard.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yoyo.battery.dashboard.data.model.BatteryInfo
import com.yoyo.battery.dashboard.data.model.ChargingSession
import com.yoyo.battery.dashboard.data.repository.BatteryDrainStats
import com.yoyo.battery.dashboard.data.repository.BatteryHealthDetails
import java.util.Locale

@Composable
fun HeroMicroPanel(
    info: BatteryInfo,
    drainStats: BatteryDrainStats,
    isDarkTheme: Boolean,
    onClick: () -> Unit,
    onOpenDrainDetail: () -> Unit,
    modifier: Modifier = Modifier,
    powerHistory: List<Double> = emptyList()
) {
    val colorScheme = MaterialTheme.colorScheme
    val panelBg = colorScheme.surfaceContainer.copy(alpha = if (isDarkTheme) 0.72f else 0.85f)
    val textColor = colorScheme.onSurface
    val mutedText = colorScheme.onSurfaceVariant
    val cyanAccent = Color(0xFF00E5FF)
    val borderColor = colorScheme.outlineVariant.copy(alpha = if (isDarkTheme) 0.30f else 0.45f)

    val speedCategory = info.chargingSpeedCategory
    val speedDesc = info.chargingSpeedDescription
    val statusText = if (info.isCharging) {
        "⚡ ${speedDesc.uppercase(Locale.US)} · ${info.chargerCapacity.uppercase(Locale.US)}"
    } else {
        "🔋 DISCHARGING"
    }
    val badgeColor = if (info.isCharging) Color(speedCategory.badgeColorHex) else Color(0xFF64748B)

    val timeMain = info.timeRemainingFormatted
    val timeLabel = if (info.isCharging) "TIME TO FULL (100%)" else "TIME REMAINING"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = panelBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. TOP: CHARGING STATUS CARD (Prominent Header Card)
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = badgeColor.copy(alpha = if (isDarkTheme) 0.18f else 0.14f),
                border = BorderStroke(1.dp, badgeColor.copy(alpha = if (isDarkTheme) 0.40f else 0.50f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Icon(
                            imageVector = if (info.isCharging) AppIcons.Bolt else AppIcons.Bedtime,
                            contentDescription = null,
                            tint = badgeColor,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 14.5.sp,
                                letterSpacing = 0.5.sp
                            ),
                            color = badgeColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = colorScheme.surfaceContainerHighest.copy(alpha = 0.85f)
                    ) {
                        Text(
                            text = "${info.level}%",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp
                            ),
                            color = textColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // 2. REMAINING HOURS CARD (Below charging status card, bold at top, little smaller)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = colorScheme.surfaceContainerHigh.copy(alpha = if (isDarkTheme) 0.55f else 0.80f),
                border = BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = if (isDarkTheme) 0.25f else 0.40f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = timeMain,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp
                            ),
                            color = textColor
                        )
                        Text(
                            text = timeLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.5.sp,
                                letterSpacing = 0.8.sp
                            ),
                            color = if (info.isCharging) cyanAccent else mutedText
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = cyanAccent.copy(alpha = if (isDarkTheme) 0.16f else 0.12f)
                    ) {
                        Text(
                            text = if (info.isCharging) String.format(Locale.US, "%.1f W", info.powerWatts) else "${info.currentNowMa} mA",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            color = cyanAccent,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.5.dp)
                        )
                    }
                }
            }

            // 2. MAIN SECTION: REDESIGNED GAUGE ON LEFT, COMPREHENSIVE FIRST-GLANCE METRICS ON RIGHT
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Left: Redesigned Micro Gauge with Tachometer Ticks & Beacon
                Box(
                    modifier = Modifier
                        .weight(0.42f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    MicroBatteryGauge(
                        level = info.level,
                        isCharging = info.isCharging,
                        isDarkTheme = isDarkTheme,
                        chargingSpeedLabel = speedDesc.uppercase(Locale.US),
                        modifier = Modifier
                            .fillMaxHeight()
                            .aspectRatio(1f)
                    )
                }

                // Right: High-Density First-Glance Diagnostics Matrix
                Column(
                    modifier = Modifier
                        .weight(0.58f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Row 1: Live Wattage + Sparkline Graph
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val powerText = String.format(Locale.US, "%.1f W", info.powerWatts)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            Icon(
                                imageVector = AppIcons.ElectricBolt,
                                contentDescription = null,
                                tint = if (info.isCharging) badgeColor else cyanAccent,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = powerText,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 22.sp
                                ),
                                color = textColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        MicroPowerSparkline(
                            powerWatts = info.powerWatts,
                            isCharging = info.isCharging,
                            isDarkTheme = isDarkTheme,
                            powerHistory = powerHistory,
                            modifier = Modifier
                                .width(56.dp)
                                .height(22.dp)
                        )
                    }

                    // Row 2: Live Amperage & Terminal Voltage
                    val currentSign = if (info.currentNowMa > 0) "+" else ""
                    val currentVoltsText = String.format(
                        Locale.US,
                        "%s%d mA · %.2f V",
                        currentSign,
                        info.currentNowMa,
                        info.voltageVolts
                    )
                    Text(
                        text = currentVoltsText,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp
                        ),
                        color = textColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Row 3: Temperature + Battery Health
                    val tempText = String.format(Locale.US, "%.1f°C", info.tempCelsius)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = when {
                                info.tempCelsius >= 45f -> Color(0xFFEF4444)
                                info.tempCelsius >= 38f -> Color(0xFFFF9800)
                                else -> Color(0xFF10B981)
                            },
                            modifier = Modifier.size(8.dp)
                        ) {}
                        Text(
                            text = "$tempText · Health: ${info.healthStatus.toLabel()}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            color = textColor.copy(alpha = 0.9f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Row 4: Battery Technology & Capacity
                    val capText = if (info.designCapacityMah > 0) "${info.designCapacityMah} mAh" else "Design Capacity"
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = colorScheme.surfaceContainerHigh.copy(alpha = if (isDarkTheme) 0.65f else 0.85f),
                        border = BorderStroke(0.5.dp, colorScheme.outlineVariant.copy(alpha = if (isDarkTheme) 0.35f else 0.5f))
                    ) {
                        Text(
                            text = "${info.technology} · $capText ➔ Specs",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.5.sp
                            ),
                            color = textColor.copy(alpha = 0.9f),
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // 3. BOTTOM: ACTIVE & IDLE DRAIN BAR (Clickable -> Opens Battery Drain Modal)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onOpenDrainDetail() },
                shape = RoundedCornerShape(12.dp),
                color = colorScheme.surfaceContainerHigh.copy(alpha = if (isDarkTheme) 0.6f else 0.8f),
                border = BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = if (isDarkTheme) 0.30f else 0.45f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Active Drain
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = AppIcons.ElectricBolt,
                            contentDescription = null,
                            tint = Color(0xFFFF9800),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "Active: ",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.sp
                            ),
                            color = mutedText
                        )
                        Text(
                            text = String.format(Locale.US, "%.1f%%/h", drainStats.activeDrainRatePerHour),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 11.sp
                            ),
                            color = Color(0xFFFF9800)
                        )
                    }

                    // Divider dot
                    Text(
                        text = "•",
                        color = mutedText.copy(alpha = 0.5f),
                        fontSize = 11.sp
                    )

                    // Middle: Idle Drain
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = AppIcons.Bedtime,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "Idle: ",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.sp
                            ),
                            color = mutedText
                        )
                        Text(
                            text = String.format(Locale.US, "%.2f%%/h", drainStats.idleDrainRatePerHour),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 11.sp
                            ),
                            color = Color(0xFF38BDF8)
                        )
                    }

                    // Right: Pill Link
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = cyanAccent.copy(alpha = if (isDarkTheme) 0.16f else 0.12f)
                    ) {
                        Text(
                            text = "Drain ➔",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            ),
                            color = cyanAccent,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HealthMicroPanel(
    health: BatteryHealthDetails,
    isDarkTheme: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val panelBg = colorScheme.surfaceContainer.copy(alpha = if (isDarkTheme) 0.72f else 0.85f)
    val textColor = colorScheme.onSurface
    val mutedText = colorScheme.onSurfaceVariant
    val emeraldColor = Color(0xFF10B981)
    val borderColor = colorScheme.outlineVariant.copy(alpha = if (isDarkTheme) 0.30f else 0.45f)

    val progressValue = if (health.isCalibrating) {
        (health.completedBenchmarkSessionsCount.toFloat() / health.requiredSessionsCount.toFloat()).coerceIn(0.15f, 0.95f)
    } else {
        (health.healthScorePercent ?: 100) / 100f
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progressValue,
        animationSpec = tween(durationMillis = 800),
        label = "HealthProgress"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = panelBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header: Colorful Icon badge + Title & Condition
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = emeraldColor.copy(alpha = if (isDarkTheme) 0.18f else 0.14f),
                        modifier = Modifier.size(24.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = AppIcons.Favorite,
                                contentDescription = null,
                                tint = emeraldColor,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Battery Health",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = emeraldColor.copy(alpha = 0.18f),
                    contentColor = emeraldColor
                ) {
                    Text(
                        text = health.healthConditionLabel,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Score % or Calibrating Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (health.isCalibrating) {
                    Text(
                        text = "Calibrating...",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp
                        ),
                        color = textColor
                    )
                } else {
                    Text(
                        text = "${health.healthScorePercent}%",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 26.sp
                        ),
                        color = textColor
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = colorScheme.surfaceContainerHigh.copy(alpha = 0.8f),
                    contentColor = colorScheme.onSurfaceVariant
                ) {
                    Text(
                        text = health.confidenceRating,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Calibration Stage Plain Segmented Bars or Calibrated Score Progress Bar
            if (health.isCalibrating) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    LinearProgressIndicator(
                        progress = { 1.0f },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(3.dp)),
                        color = emeraldColor,
                        trackColor = emeraldColor.copy(alpha = 0.22f)
                    )
                    val bar2Progress = if (health.calibrationStage >= 3) 1.0f
                    else if (health.calibrationStage == 2) {
                        (health.completedBenchmarkSessionsCount.toFloat() / health.requiredSessionsCount.toFloat()).coerceIn(0.1f, 1.0f)
                    } else 0.0f
                    LinearProgressIndicator(
                        progress = { bar2Progress },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (health.calibrationStage >= 2) emeraldColor else emeraldColor.copy(alpha = 0.22f),
                        trackColor = emeraldColor.copy(alpha = 0.22f)
                    )
                    val bar3Progress = if (health.calibrationStage >= 3) 1.0f else 0.0f
                    LinearProgressIndicator(
                        progress = { bar3Progress },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (health.calibrationStage >= 3) emeraldColor else emeraldColor.copy(alpha = 0.22f),
                        trackColor = emeraldColor.copy(alpha = 0.22f)
                    )
                }
            } else {
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = emeraldColor,
                    trackColor = emeraldColor.copy(alpha = 0.22f)
                )
            }

            // Capacity Details
            val capacityText = if (health.isCalibrating) {
                "Benchmarking (${health.completedBenchmarkSessionsCount}/${health.requiredSessionsCount} Sessions) ➔"
            } else {
                String.format(Locale.US, "Actual: %d / %d mAh ➔", health.estimatedCapacityMah, health.designCapacityMah)
            }

            Text(
                text = capacityText,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = mutedText
            )
        }
    }
}

@Composable
fun CyclesMicroPanel(
    info: BatteryInfo,
    isDarkTheme: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val panelBg = colorScheme.surfaceContainer.copy(alpha = if (isDarkTheme) 0.72f else 0.85f)
    val textColor = colorScheme.onSurface
    val mutedText = colorScheme.onSurfaceVariant
    val purpleColor = Color(0xFFA855F7)
    val borderColor = colorScheme.outlineVariant.copy(alpha = if (isDarkTheme) 0.30f else 0.45f)

    val cyclesCount = info.cycleCount ?: 120

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = panelBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = purpleColor.copy(alpha = if (isDarkTheme) 0.18f else 0.14f),
                    modifier = Modifier.size(24.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = AppIcons.Shield,
                            contentDescription = null,
                            tint = purpleColor,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Cycles",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "$cyclesCount Cycles",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp
                ),
                color = textColor
            )

            Text(
                text = "Charge Cycles & Wear ➔",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = mutedText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun ChargingHistoryMicroPanel(
    history: List<ChargingSession>,
    isDarkTheme: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val panelBg = colorScheme.surfaceContainer.copy(alpha = if (isDarkTheme) 0.72f else 0.85f)
    val textColor = colorScheme.onSurface
    val mutedText = colorScheme.onSurfaceVariant
    val amberColor = Color(0xFFF59E0B)
    val borderColor = colorScheme.outlineVariant.copy(alpha = if (isDarkTheme) 0.30f else 0.45f)

    val lastSession = history.firstOrNull()
    val sessionSummaryText = if (lastSession != null) {
        val gain = if (lastSession.endLevel != null) "+${lastSession.gainedPercent}%" else "In Progress"
        val added = if (lastSession.chargeAddedMah > 0) String.format(Locale.US, " (%.0f mAh)", lastSession.chargeAddedMah) else ""
        String.format(Locale.US, "Latest: %s in %dm%s via %s", gain, lastSession.durationMinutes, added, lastSession.plugType)
    } else {
        "No sessions recorded yet in SQLite DB. Plug in to track."
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = panelBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = amberColor.copy(alpha = if (isDarkTheme) 0.18f else 0.14f),
                        modifier = Modifier.size(24.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = AppIcons.Bolt,
                                contentDescription = null,
                                tint = amberColor,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Charging History",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = amberColor.copy(alpha = 0.18f),
                    contentColor = amberColor
                ) {
                    Text(
                        text = "${history.size} Sessions",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Text(
                text = sessionSummaryText,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "Tap to view timeline & select time filters ➔",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = mutedText
            )
        }
    }
}

@Composable
fun MicroDiagnosticCard(
    icon: ImageVector,
    title: String,
    value: String,
    subtitle: String,
    iconColor: Color,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    onClick: (() -> Unit)? = null
) {
    val colorScheme = MaterialTheme.colorScheme
    val cardModifier = if (onClick != null) {
        modifier
            .clip(RoundedCornerShape(22.dp))
            .clickable { onClick() }
    } else {
        modifier.clip(RoundedCornerShape(22.dp))
    }

    Card(
        modifier = cardModifier,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surfaceContainer.copy(alpha = if (isDarkTheme) 0.72f else 0.85f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = if (isDarkTheme) 0.30f else 0.45f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(11.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = iconColor.copy(alpha = if (isDarkTheme) 0.18f else 0.14f),
                    modifier = Modifier.size(24.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = iconColor,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 17.sp
                ),
                color = colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
