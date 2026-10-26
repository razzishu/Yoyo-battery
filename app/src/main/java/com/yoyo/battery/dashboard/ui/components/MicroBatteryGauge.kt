package com.yoyo.battery.dashboard.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun MicroBatteryGauge(
    level: Int,
    isCharging: Boolean,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier,
    chargingSpeedLabel: String = ""
) {
    val animatedLevel by animateFloatAsState(
        targetValue = level.coerceIn(0, 100) / 100f,
        animationSpec = tween(durationMillis = 950),
        label = "MicroGaugeAnimation"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "GaugePulseTransition")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseAlpha"
    )

    // Vibrant Modern Color Gradients
    val neonCyan = Color(0xFF00E5FF)
    val vibrantEmerald = Color(0xFF10B981)
    val electricBlue = Color(0xFF38BDF8)
    val amberWarning = Color(0xFFF59E0B)
    val redCritical = Color(0xFFEF4444)

    val mainColors = when {
        isCharging -> listOf(neonCyan, vibrantEmerald, electricBlue)
        level > 80 -> listOf(neonCyan, vibrantEmerald)
        level > 25 -> listOf(electricBlue, Color(0xFF3B82F6))
        level > 12 -> listOf(amberWarning, Color(0xFFEA580C))
        else -> listOf(redCritical, Color(0xFFB91C1C))
    }

    val trackColor = if (isDarkTheme) Color(0xFF1A2234) else Color(0xFFE2E8F0)
    val textColor = if (isDarkTheme) Color.White else Color(0xFF0F172A)
    val mutedText = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF64748B)

    Box(
        modifier = modifier.aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            val strokeWidth = 11.5.dp.toPx()
            val startAngle = 135f
            val sweepAngle = 270f
            val currentSweep = sweepAngle * animatedLevel

            // 1. Outer Concentric Precision Halo Track
            drawArc(
                color = trackColor.copy(alpha = if (isDarkTheme) 0.35f else 0.45f),
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
            )

            // 2. Main Background Track
            drawArc(
                color = trackColor,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // 3. Ambient Radiant Glow Layer (when charging or active)
            if (isCharging || level > 20) {
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = mainColors.map { it.copy(alpha = pulseAlpha * 0.38f) }
                    ),
                    startAngle = startAngle,
                    sweepAngle = currentSweep,
                    useCenter = false,
                    style = Stroke(width = strokeWidth * 1.6f, cap = StrokeCap.Round)
                )
            }

            // 4. Primary High-Precision Gradient Progress Arc
            drawArc(
                brush = Brush.linearGradient(
                    colors = mainColors,
                    start = Offset(0f, size.height),
                    end = Offset(size.width, 0f)
                ),
                startAngle = startAngle,
                sweepAngle = currentSweep,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // 5. Segmented Tachometer Precision Ticks (22 discrete markers)
            val totalTicks = 22
            val center = size / 2f
            val radius = (size.minDimension / 2f) - (strokeWidth * 1.5f)

            for (i in 0..totalTicks) {
                val tickFraction = i.toFloat() / totalTicks
                val angleDeg = startAngle + (sweepAngle * tickFraction)
                val angleRad = Math.toRadians(angleDeg.toDouble())

                val tickLen = if (i % 5 == 0) 5.5.dp.toPx() else 3.5.dp.toPx()
                val tickW = if (i % 5 == 0) 2.2.dp.toPx() else 1.2.dp.toPx()

                val startX = center.width + (radius * cos(angleRad)).toFloat()
                val startY = center.height + (radius * sin(angleRad)).toFloat()
                val endX = center.width + ((radius - tickLen) * cos(angleRad)).toFloat()
                val endY = center.height + ((radius - tickLen) * sin(angleRad)).toFloat()

                val tickActive = tickFraction <= animatedLevel
                val tickColor = if (tickActive) mainColors.first().copy(alpha = 0.9f) else trackColor.copy(alpha = 0.5f)

                drawLine(
                    color = tickColor,
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = tickW,
                    cap = StrokeCap.Round
                )
            }

            // 6. Glowing Orbital Pointer Beacon Dot at Arc Tip
            if (animatedLevel > 0.02f) {
                val tipAngleDeg = startAngle + currentSweep
                val tipAngleRad = Math.toRadians(tipAngleDeg.toDouble())
                val tipRadius = (size.minDimension / 2f) - (strokeWidth / 2f)
                val tipX = center.width + (tipRadius * cos(tipAngleRad)).toFloat()
                val tipY = center.height + (tipRadius * sin(tipAngleRad)).toFloat()

                // Glow halo behind beacon
                drawCircle(
                    color = mainColors.last().copy(alpha = pulseAlpha * 0.6f),
                    radius = strokeWidth * 0.9f,
                    center = Offset(tipX, tipY)
                )

                // Crisp beacon core
                drawCircle(
                    color = Color.White,
                    radius = strokeWidth * 0.42f,
                    center = Offset(tipX, tipY)
                )
            }
        }

        // 7. Center Content: Level %, Charging Bolt & Status
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 6.dp)
        ) {
            if (isCharging) {
                Icon(
                    imageVector = AppIcons.ElectricBolt,
                    contentDescription = "Charging",
                    tint = neonCyan,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.height(1.dp))
            }

            Text(
                text = "$level%",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = if (isCharging) 25.sp else 28.sp,
                    letterSpacing = (-0.5).sp
                ),
                color = textColor,
                maxLines = 1
            )

            val statusBadge = when {
                isCharging && chargingSpeedLabel.isNotBlank() -> chargingSpeedLabel
                isCharging -> "CHARGING"
                level <= 15 -> "LOW"
                level >= 95 -> "FULL"
                else -> "ACTIVE"
            }

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (isCharging) neonCyan.copy(alpha = 0.16f) else Color.Transparent
            ) {
                Text(
                    text = statusBadge,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 9.sp,
                        letterSpacing = 0.5.sp
                    ),
                    color = if (isCharging) neonCyan else mutedText,
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                )
            }
        }
    }
}
