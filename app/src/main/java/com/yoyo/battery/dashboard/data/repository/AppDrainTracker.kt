package com.yoyo.battery.dashboard.data.repository

import android.app.AppOpsManager
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Process

data class AppDrainItem(
    val packageName: String,
    val appName: String,
    val appIcon: Drawable?,
    val foregroundTimeMs: Long,
    val foregroundFormatted: String,
    val drainPercentOfTotal: Float,
    val estimatedMah: Double
)

object AppDrainTracker {

    fun hasUsageStatsPermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager ?: return false
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun getAppDrainList(context: Context, designCapacityMah: Int): List<AppDrainItem> {
        if (!hasUsageStatsPermission(context)) return emptyList()

        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            ?: return emptyList()

        val calendar = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        val startOfToday = calendar.timeInMillis
        val now = System.currentTimeMillis()

        val aggregatedMap = mutableMapOf<String, Long>()

        // 1. Gold standard: Calculate exact foreground time since midnight today using UsageEvents
        try {
            val usageEvents = usageStatsManager.queryEvents(startOfToday, now)
            val event = android.app.usage.UsageEvents.Event()
            val startTimes = mutableMapOf<String, Long>()

            while (usageEvents != null && usageEvents.hasNextEvent()) {
                usageEvents.getNextEvent(event)
                val pkg = event.packageName ?: continue
                val timeStamp = event.timeStamp

                when (event.eventType) {
                    android.app.usage.UsageEvents.Event.ACTIVITY_RESUMED,
                    android.app.usage.UsageEvents.Event.MOVE_TO_FOREGROUND -> {
                        startTimes[pkg] = timeStamp
                    }
                    android.app.usage.UsageEvents.Event.ACTIVITY_PAUSED,
                    android.app.usage.UsageEvents.Event.ACTIVITY_STOPPED,
                    android.app.usage.UsageEvents.Event.MOVE_TO_BACKGROUND -> {
                        val startTime = startTimes.remove(pkg)
                        if (startTime != null && timeStamp >= startTime) {
                            val duration = timeStamp - startTime
                            aggregatedMap[pkg] = (aggregatedMap[pkg] ?: 0L) + duration
                        }
                    }
                }
            }

            // Close any active session still ongoing up to 'now'
            for ((pkg, startTime) in startTimes) {
                if (now >= startTime) {
                    val duration = now - startTime
                    aggregatedMap[pkg] = (aggregatedMap[pkg] ?: 0L) + duration
                }
            }
        } catch (_: Exception) {
            // Fallback below if queryEvents fails
        }

        // 2. Fallback to INTERVAL_DAILY query filtered strictly for today if UsageEvents produced no data
        if (aggregatedMap.isEmpty()) {
            val dailyStats: List<UsageStats> = try {
                usageStatsManager.queryUsageStats(
                    UsageStatsManager.INTERVAL_DAILY,
                    startOfToday,
                    now
                ) ?: emptyList()
            } catch (_: Exception) {
                emptyList()
            }

            for (u in dailyStats) {
                // Must have been used today after midnight
                if (u.totalTimeInForeground > 0 && u.lastTimeUsed >= startOfToday) {
                    aggregatedMap[u.packageName] = u.totalTimeInForeground
                }
            }
        }

        if (aggregatedMap.isEmpty()) return emptyList()

        val pm: PackageManager = context.packageManager
        val totalForegroundAllAppsMs = aggregatedMap.values.sum().coerceAtLeast(1L)
        val nominalDailyDrainMah = if (designCapacityMah > 0) designCapacityMah * 0.70 else 2500.0

        val items = mutableListOf<AppDrainItem>()

        for ((pkg, timeMs) in aggregatedMap) {
            // Keep all apps with at least 2 seconds of foreground usage
            if (timeMs < 2000L) continue

            // Resolve app name & icon with robust fallback so no user app is dropped
            val (appName, icon) = try {
                val appInfo = pm.getApplicationInfo(pkg, 0)
                Pair(pm.getApplicationLabel(appInfo).toString(), pm.getApplicationIcon(appInfo))
            } catch (_: Exception) {
                val fallbackName = pkg.substringAfterLast('.').replaceFirstChar { 
                    if (it.isLowerCase()) it.titlecase() else it.toString() 
                }
                Pair(fallbackName, null)
            }

            val ratio = (timeMs.toFloat() / totalForegroundAllAppsMs.toFloat()).coerceIn(0f, 1f)
            val percent = (ratio * 100f)
            val estimatedMah = nominalDailyDrainMah * ratio

            val totalSeconds = (timeMs / 1000)
            val minutes = totalSeconds / 60
            val hours = minutes / 60
            val remMin = minutes % 60
            val formatted = when {
                hours > 0 -> "${hours}h ${remMin}m"
                minutes > 0 -> "${minutes}m"
                else -> "${totalSeconds}s"
            }

            items.add(
                AppDrainItem(
                    packageName = pkg,
                    appName = appName,
                    appIcon = icon,
                    foregroundTimeMs = timeMs,
                    foregroundFormatted = formatted,
                    drainPercentOfTotal = percent,
                    estimatedMah = estimatedMah
                )
            )
        }

        return items.sortedByDescending { it.foregroundTimeMs }.take(100)
    }
}
