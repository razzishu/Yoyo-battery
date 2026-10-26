package com.yoyo.battery.dashboard.data.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            val prefs = context.getSharedPreferences("yoyo_settings_prefs", Context.MODE_PRIVATE)
            val isBackgroundTrackingEnabled = prefs.getBoolean("is_background_tracking_enabled", true)
            if (isBackgroundTrackingEnabled) {
                try {
                    BatteryMonitoringService.startService(context)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}
