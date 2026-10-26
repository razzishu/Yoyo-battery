package com.yoyo.battery.dashboard.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yoyo.battery.dashboard.data.db.TimeRangeFilter
import com.yoyo.battery.dashboard.data.model.BatteryInfo
import com.yoyo.battery.dashboard.data.model.ChargingSession
import com.yoyo.battery.dashboard.data.repository.BatteryHealthDetails
import com.yoyo.battery.dashboard.data.repository.BatteryHealthEngine
import com.yoyo.battery.dashboard.data.repository.BatteryRepository
import com.yoyo.battery.dashboard.data.repository.ChargingSessionManager
import com.yoyo.battery.dashboard.data.service.BatteryMonitoringService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import android.net.Uri
import com.yoyo.battery.dashboard.data.export.DataExportManager
import com.yoyo.battery.dashboard.data.repository.AppDrainItem
import com.yoyo.battery.dashboard.data.repository.AppDrainTracker
import com.yoyo.battery.dashboard.data.repository.BatteryDrainStats
import com.yoyo.battery.dashboard.data.repository.BatteryDrainTracker
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class DetailType {
    HERO_BATTERY,
    BATTERY_HEALTH,
    BATTERY_DRAIN,
    CYCLES,
    CHARGING_HISTORY,
    CURRENT_FLOW,
    VOLTAGE,
    TEMPERATURE,
    POWER_SOURCE,
    APP_DRAIN,
    DEVICE_INFO
}

class BatteryViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("yoyo_settings_prefs", Context.MODE_PRIVATE)

    private val repository = BatteryRepository.getInstance(application.applicationContext)
    private val sessionManager = ChargingSessionManager.getInstance(application.applicationContext)
    private val healthEngine = BatteryHealthEngine(application.applicationContext)
    private val drainTracker = BatteryDrainTracker.getInstance(application.applicationContext)

    private val _batteryInfo = MutableStateFlow(BatteryInfo())
    val batteryInfo: StateFlow<BatteryInfo> = _batteryInfo.asStateFlow()

    private val _powerHistory = MutableStateFlow<List<Double>>(emptyList())
    val powerHistory: StateFlow<List<Double>> = _powerHistory.asStateFlow()

    private val _drainStats = MutableStateFlow(drainTracker.getDrainStats(BatteryInfo()))
    val drainStats: StateFlow<BatteryDrainStats> = _drainStats.asStateFlow()

    private val _healthDetails = MutableStateFlow(
        healthEngine.calculateAccurateHealth(BatteryInfo(), sessionManager.getSessionsByFilter(TimeRangeFilter.ALL_TIME))
    )
    val healthDetails: StateFlow<BatteryHealthDetails> = _healthDetails.asStateFlow()

    private val _chargingHistory = MutableStateFlow<List<ChargingSession>>(emptyList())
    val chargingHistory: StateFlow<List<ChargingSession>> = _chargingHistory.asStateFlow()

    private val _selectedTimeFilter = MutableStateFlow(TimeRangeFilter.ALL_TIME)
    val selectedTimeFilter: StateFlow<TimeRangeFilter> = _selectedTimeFilter.asStateFlow()

    private val _activeDetailType = MutableStateFlow<DetailType?>(null)
    val activeDetailType: StateFlow<DetailType?> = _activeDetailType.asStateFlow()

    private val _isSettingsOpen = MutableStateFlow(false)
    val isSettingsOpen: StateFlow<Boolean> = _isSettingsOpen.asStateFlow()

    private val _isBackgroundTrackingEnabled = MutableStateFlow(
        prefs.getBoolean("is_background_tracking_enabled", true)
    )
    val isBackgroundTrackingEnabled: StateFlow<Boolean> = _isBackgroundTrackingEnabled.asStateFlow()

    private val _isFahrenheit = MutableStateFlow(
        prefs.getBoolean("is_fahrenheit", false)
    )
    val isFahrenheit: StateFlow<Boolean> = _isFahrenheit.asStateFlow()

    private val _isAlarm80Enabled = MutableStateFlow(
        prefs.getBoolean("alarm_80_enabled", false)
    )
    val isAlarm80Enabled: StateFlow<Boolean> = _isAlarm80Enabled.asStateFlow()

    private val _isAlarm20Enabled = MutableStateFlow(
        prefs.getBoolean("alarm_20_enabled", false)
    )
    val isAlarm20Enabled: StateFlow<Boolean> = _isAlarm20Enabled.asStateFlow()

    // 1. Overheat Alarm State
    private val _isOverheatAlarmEnabled = MutableStateFlow(
        prefs.getBoolean("overheat_alert_enabled", true)
    )
    val isOverheatAlarmEnabled: StateFlow<Boolean> = _isOverheatAlarmEnabled.asStateFlow()

    private val _overheatThresholdCelsius = MutableStateFlow(
        prefs.getInt("overheat_threshold_celsius", 42)
    )
    val overheatThresholdCelsius: StateFlow<Int> = _overheatThresholdCelsius.asStateFlow()

    // 2. Nightly Quiet Hours State
    private val _isQuietHoursEnabled = MutableStateFlow(
        prefs.getBoolean("quiet_hours_enabled", false)
    )
    val isQuietHoursEnabled: StateFlow<Boolean> = _isQuietHoursEnabled.asStateFlow()

    private val _quietHoursStartHour = MutableStateFlow(
        prefs.getInt("quiet_hours_start_hour", 22)
    )
    val quietHoursStartHour: StateFlow<Int> = _quietHoursStartHour.asStateFlow()

    private val _quietHoursEndHour = MutableStateFlow(
        prefs.getInt("quiet_hours_end_hour", 7)
    )
    val quietHoursEndHour: StateFlow<Int> = _quietHoursEndHour.asStateFlow()

    private val _isQuietHoursVibrateOnly = MutableStateFlow(
        prefs.getBoolean("quiet_hours_vibrate_only", true)
    )
    val isQuietHoursVibrateOnly: StateFlow<Boolean> = _isQuietHoursVibrateOnly.asStateFlow()

    // 3. Per-App Battery Drain Attribution State
    private val _appDrainList = MutableStateFlow<List<AppDrainItem>>(emptyList())
    val appDrainList: StateFlow<List<AppDrainItem>> = _appDrainList.asStateFlow()

    private val _hasUsageStatsPermission = MutableStateFlow(
        AppDrainTracker.hasUsageStatsPermission(application.applicationContext)
    )
    val hasUsageStatsPermission: StateFlow<Boolean> = _hasUsageStatsPermission.asStateFlow()

    init {
        try {
            if (_isBackgroundTrackingEnabled.value) {
                BatteryMonitoringService.startService(application.applicationContext)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        startBatteryMonitoring()
    }

    private var foregroundMonitoringJob: Job? = null
    private var lastHealthCalcLevel: Int = -1
    private var lastHealthCalcCharging: Boolean? = null
    private var lastHealthCalcTimeMs: Long = 0L

    fun setForegroundVisible(visible: Boolean) {
        if (visible) {
            if (foregroundMonitoringJob?.isActive != true) {
                startBatteryMonitoring()
            }
        } else {
            foregroundMonitoringJob?.cancel()
            foregroundMonitoringJob = null
        }
    }

    private fun startBatteryMonitoring() {
        foregroundMonitoringJob?.cancel()
        foregroundMonitoringJob = viewModelScope.launch {
            repository.getBatteryInfoFlow(1000L).collect { info ->
                _batteryInfo.value = info

                // Maintain genuine live power history buffer (last 16 measurements)
                _powerHistory.update { history ->
                    (history + info.powerWatts).takeLast(16)
                }

                // Process active and idle battery drain telemetry
                drainTracker.processBatteryUpdate(info)
                _drainStats.value = drainTracker.getDrainStats(info)

                // Process battery updates asynchronously off the main thread
                val currentFilter = _selectedTimeFilter.value
                val history = sessionManager.processBatteryUpdateAsync(info, currentFilter)
                _chargingHistory.value = history

                // Optimize: Recalculate health only when status changes, level changes, or every 60s
                val now = System.currentTimeMillis()
                val statusChanged = (info.level != lastHealthCalcLevel) || (info.isCharging != lastHealthCalcCharging)
                val periodicElapsed = (now - lastHealthCalcTimeMs) >= 60000L

                if (statusChanged || periodicElapsed || lastHealthCalcLevel == -1) {
                    val allSessionsForHealth = sessionManager.getSessionsByFilterAsync(TimeRangeFilter.ALL_TIME)
                    val health = healthEngine.calculateAccurateHealth(info, allSessionsForHealth)
                    _healthDetails.value = health
                    lastHealthCalcLevel = info.level
                    lastHealthCalcCharging = info.isCharging
                    lastHealthCalcTimeMs = now
                }
            }
        }
    }

    fun toggleBackgroundTracking(enabled: Boolean) {
        _isBackgroundTrackingEnabled.value = enabled
        prefs.edit().putBoolean("is_background_tracking_enabled", enabled).apply()

        if (enabled) {
            BatteryMonitoringService.startService(getApplication())
        } else {
            BatteryMonitoringService.stopService(getApplication())
        }
    }

    fun toggleAlarm80(enabled: Boolean) {
        _isAlarm80Enabled.value = enabled
        prefs.edit().putBoolean("alarm_80_enabled", enabled).apply()
    }

    fun toggleAlarm20(enabled: Boolean) {
        _isAlarm20Enabled.value = enabled
        prefs.edit().putBoolean("alarm_20_enabled", enabled).apply()
    }

    fun openSettings() {
        _isSettingsOpen.value = true
    }

    fun closeSettings() {
        _isSettingsOpen.value = false
    }

    fun setTimeFilter(filter: TimeRangeFilter) {
        _selectedTimeFilter.value = filter
        viewModelScope.launch {
            val history = sessionManager.getSessionsByFilterAsync(filter)
            _chargingHistory.value = history
        }
    }

    fun openDetail(type: DetailType) {
        _activeDetailType.value = type
        if (type == DetailType.CHARGING_HISTORY || type == DetailType.BATTERY_HEALTH) {
            viewModelScope.launch {
                val history = sessionManager.getSessionsByFilterAsync(_selectedTimeFilter.value)
                _chargingHistory.value = history
            }
        } else if (type == DetailType.APP_DRAIN) {
            refreshAppDrain()
        }
    }

    fun closeDetail() {
        _activeDetailType.value = null
    }

    fun toggleTemperatureUnit() {
        val next = !_isFahrenheit.value
        _isFahrenheit.value = next
        prefs.edit().putBoolean("is_fahrenheit", next).apply()
    }

    fun clearHistory() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                sessionManager.clearAllSessions()
            }
            val emptySessions = emptyList<ChargingSession>()
            _chargingHistory.value = emptySessions
            val health = healthEngine.calculateAccurateHealth(_batteryInfo.value, emptySessions)
            _healthDetails.value = health
        }
    }

    // 1. Overheat Alarm Settings
    fun toggleOverheatAlarm(enabled: Boolean) {
        _isOverheatAlarmEnabled.value = enabled
        prefs.edit().putBoolean("overheat_alert_enabled", enabled).apply()
    }

    fun setOverheatThreshold(celsius: Int) {
        _overheatThresholdCelsius.value = celsius
        prefs.edit().putInt("overheat_threshold_celsius", celsius).apply()
    }

    // 2. Quiet Hours Settings
    fun toggleQuietHours(enabled: Boolean) {
        _isQuietHoursEnabled.value = enabled
        prefs.edit().putBoolean("quiet_hours_enabled", enabled).apply()
    }

    fun setQuietHoursRange(startHour: Int, endHour: Int) {
        _quietHoursStartHour.value = startHour
        _quietHoursEndHour.value = endHour
        prefs.edit()
            .putInt("quiet_hours_start_hour", startHour)
            .putInt("quiet_hours_end_hour", endHour)
            .apply()
    }

    fun toggleQuietHoursVibrateOnly(enabled: Boolean) {
        _isQuietHoursVibrateOnly.value = enabled
        prefs.edit().putBoolean("quiet_hours_vibrate_only", enabled).apply()
    }

    // 3. Per-App Battery Drain
    fun refreshAppDrain() {
        val context = getApplication<Application>().applicationContext
        val hasPerm = AppDrainTracker.hasUsageStatsPermission(context)
        _hasUsageStatsPermission.value = hasPerm
        if (hasPerm) {
            viewModelScope.launch(Dispatchers.IO) {
                val list = AppDrainTracker.getAppDrainList(context, _batteryInfo.value.designCapacityMah)
                _appDrainList.value = list
            }
        }
    }

    // 4. Data Export Methods
    fun exportChargingSessionsCsv(context: Context): Uri? {
        val sessions = _chargingHistory.value
        return DataExportManager.exportSessionsToCsv(context, sessions)
    }

    fun exportChargingSessionsJson(context: Context): Uri? {
        val sessions = _chargingHistory.value
        return DataExportManager.exportSessionsToJson(context, sessions)
    }

    fun shareExportFile(context: Context, uri: Uri, mimeType: String, title: String) {
        DataExportManager.shareExportFile(context, uri, mimeType, title)
    }

    // 5. Data Restore Method
    fun restoreChargingSessions(context: Context, uri: Uri, onResult: (Boolean, Int) -> Unit) {
        viewModelScope.launch {
            val sessions = withContext(Dispatchers.IO) {
                DataExportManager.importSessionsFromUri(context, uri)
            }
            if (sessions.isNotEmpty()) {
                val count = sessionManager.restoreSessionsAsync(sessions)
                val updatedSessions = sessionManager.getSessionsByFilterAsync(_selectedTimeFilter.value)
                _chargingHistory.value = updatedSessions
                val health = healthEngine.calculateAccurateHealth(
                    _batteryInfo.value,
                    sessionManager.getSessionsByFilterAsync(TimeRangeFilter.ALL_TIME)
                )
                _healthDetails.value = health
                onResult(true, count)
            } else {
                onResult(false, 0)
            }
        }
    }

    // 6. Battery Drain Stats
    fun resetDrainStats() {
        drainTracker.resetStats()
        _drainStats.value = drainTracker.getDrainStats(_batteryInfo.value)
    }
}
