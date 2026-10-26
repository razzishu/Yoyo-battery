package com.yoyo.battery.dashboard.ui.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.RemoteViews
import com.yoyo.battery.dashboard.MainActivity
import com.yoyo.battery.dashboard.R
import com.yoyo.battery.dashboard.data.model.BatteryStatus
import com.yoyo.battery.dashboard.data.repository.BatteryDrainTracker
import com.yoyo.battery.dashboard.data.repository.BatteryRepository
import java.util.Locale

class BatteryAppWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle?
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        updateAppWidget(context, appWidgetManager, appWidgetId)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == Intent.ACTION_BATTERY_CHANGED) {
            updateAllWidgets(context)
        }
    }

    companion object {
        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_battery)

            val batteryRepo = BatteryRepository.getInstance(context)
            val info = batteryRepo.fetchCurrentBatteryInfo()

            // Level & Progress Bar
            views.setTextViewText(R.id.widget_battery_percent, "${info.level}%")
            views.setProgressBar(R.id.widget_progress_bar, 100, info.level, false)

            // Status Badge
            val statusBadgeText = when {
                info.status == BatteryStatus.FULL -> "✓ FULL"
                info.isCharging -> {
                    if (info.maxChargerWatts > 0) "⚡ ${info.chargerRatingLabel.uppercase()}"
                    else "⚡ CHARGING"
                }
                else -> "🔋 DISCHARGING"
            }
            views.setTextViewText(R.id.widget_status_badge, statusBadgeText)

            // Current mA and Power Watts
            val sign = if (info.currentNowMa > 0) "+" else ""
            views.setTextViewText(R.id.widget_current_ma, "$sign${info.currentNowMa} mA")
            views.setTextViewText(
                R.id.widget_power_watts,
                String.format(Locale.US, "%.2f Watts", info.powerWatts)
            )

            // Temperature and Voltage
            views.setTextViewText(
                R.id.widget_temperature,
                String.format(Locale.US, "🌡 %.1f°C", info.tempCelsius)
            )
            views.setTextViewText(
                R.id.widget_voltage,
                String.format(Locale.US, "⚡ %.2f V", info.voltageVolts)
            )

            // Adaptive Dimensions Evaluation
            val options = appWidgetManager.getAppWidgetOptions(appWidgetId)
            val minHeight = options?.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 0) ?: 0
            val minWidth = options?.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 0) ?: 0

            // Show expanded detail rows when widget is resized larger (height >= 110dp or width >= 230dp)
            val isExpanded = minHeight >= 110 || minWidth >= 230

            if (isExpanded) {
                views.setViewVisibility(R.id.widget_expanded_container, View.VISIBLE)

                // 1. Time Remaining
                views.setTextViewText(R.id.widget_time_remaining, "⏱ ${info.timeRemainingFormatted}")

                // 2. Battery Health
                val healthLabel = if (info.healthStatus.name.isNotBlank()) info.healthStatus.name.lowercase().replaceFirstChar { it.uppercase() } else "Good"
                views.setTextViewText(R.id.widget_health_score, "💚 Health: $healthLabel")

                // 3. Charger Capacity or Live Drain Rate
                val chargerOrDrain = if (info.isCharging) {
                    "🔌 ${info.chargerCapacity}"
                } else {
                    val drainStats = BatteryDrainTracker.getInstance(context).getDrainStats(info)
                    val rate = if (drainStats.activeDrainRatePerHour > 0) drainStats.activeDrainRatePerHour else 7.5f
                    String.format(Locale.US, "📉 Drain: %.1f%%/h", rate)
                }
                views.setTextViewText(R.id.widget_charger_capacity, chargerOrDrain)

                // 4. Battery Technology & Design Capacity
                views.setTextViewText(
                    R.id.widget_battery_tech,
                    "🔋 ${info.technology} · ${info.designCapacityMah} mAh"
                )
            } else {
                views.setViewVisibility(R.id.widget_expanded_container, View.GONE)
            }

            // Launch MainActivity on widget tap
            val launchIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        fun updateAllWidgets(context: Context) {
            try {
                val appWidgetManager = AppWidgetManager.getInstance(context) ?: return
                val thisWidget = ComponentName(context, BatteryAppWidgetProvider::class.java)
                val allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget) ?: return
                for (widgetId in allWidgetIds) {
                    try {
                        updateAppWidget(context, appWidgetManager, widgetId)
                    } catch (_: Exception) {}
                }
            } catch (_: Exception) {}
        }
    }
}
