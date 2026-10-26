package com.yoyo.battery.dashboard.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun MicroPowerSparkline(
    powerWatts: Double,
    isCharging: Boolean,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier,
    powerHistory: List<Double> = emptyList()
) {
    val graphColor = if (isCharging) {
        if (isDarkTheme) Color(0xFF10B981) else Color(0xFF059669)
    } else {
        if (isDarkTheme) Color(0xFF06B6D4) else Color(0xFF0284C7)
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Determine points from actual power history if available
        val points = if (powerHistory.size >= 2) {
            val maxPower = (powerHistory.maxOrNull() ?: 1.0).coerceAtLeast(5.0)
            powerHistory.map { p ->
                (p / maxPower).toFloat().coerceIn(0.1f, 0.95f)
            }
        } else {
            val normalized = (powerWatts.toFloat() / 25f).coerceIn(0.15f, 0.95f)
            listOf(
                normalized * 0.7f,
                normalized * 0.85f,
                normalized * 0.8f,
                normalized * 0.95f,
                normalized * 0.9f,
                normalized
            )
        }

        if (points.size >= 2) {
            val path = Path()
            val stepX = width / (points.size - 1)

            points.forEachIndexed { i, p ->
                val x = i * stepX
                val y = height * (1f - p)
                if (i == 0) {
                    path.moveTo(x, y)
                } else {
                    val prevX = (i - 1) * stepX
                    val prevY = height * (1f - points[i - 1])
                    val cx = (prevX + x) / 2f
                    path.cubicTo(cx, prevY, cx, y, x, y)
                }
            }

            // Draw sparkline path
            drawPath(
                path = path,
                color = graphColor,
                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
            )

            // Draw gradient fill under sparkline
            val fillPath = Path().apply {
                addPath(path)
                lineTo(width, height)
                lineTo(0f, height)
                close()
            }

            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(graphColor.copy(alpha = 0.25f), Color.Transparent)
                )
            )
        }
    }
}
