package com.yoyo.battery.dashboard.ui

import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yoyo.battery.dashboard.data.db.TimeRangeFilter
import com.yoyo.battery.dashboard.data.model.BatteryInfo
import com.yoyo.battery.dashboard.data.model.ChargingSession
import com.yoyo.battery.dashboard.data.model.PluggedSource
import com.yoyo.battery.dashboard.data.repository.AppDrainItem
import com.yoyo.battery.dashboard.data.repository.BatteryDrainStats
import com.yoyo.battery.dashboard.data.repository.BatteryHealthDetails
import com.yoyo.battery.dashboard.ui.components.AppIcons
import com.yoyo.battery.dashboard.ui.components.ChargingHistoryMicroPanel
import com.yoyo.battery.dashboard.ui.components.CyclesMicroPanel
import com.yoyo.battery.dashboard.ui.components.DetailModalScreen
import com.yoyo.battery.dashboard.ui.components.HealthMicroPanel
import com.yoyo.battery.dashboard.ui.components.HeroMicroPanel
import com.yoyo.battery.dashboard.ui.components.MicroDiagnosticCard
import com.yoyo.battery.dashboard.ui.components.SettingsModalScreen
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: BatteryViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val info by viewModel.batteryInfo.collectAsState()
    val powerHistory by viewModel.powerHistory.collectAsState()
    val health by viewModel.healthDetails.collectAsState()
    val history by viewModel.chargingHistory.collectAsState()
    val drainStats by viewModel.drainStats.collectAsState()
    val isFahrenheit by viewModel.isFahrenheit.collectAsState()
    val activeDetail by viewModel.activeDetailType.collectAsState()
    val isSettingsOpen by viewModel.isSettingsOpen.collectAsState()
    val isBackgroundTrackingEnabled by viewModel.isBackgroundTrackingEnabled.collectAsState()
    val isAlarm80Enabled by viewModel.isAlarm80Enabled.collectAsState()
    val isAlarm20Enabled by viewModel.isAlarm20Enabled.collectAsState()
    val selectedTimeFilter by viewModel.selectedTimeFilter.collectAsState()
    val isOverheatAlarmEnabled by viewModel.isOverheatAlarmEnabled.collectAsState()
    val overheatThresholdCelsius by viewModel.overheatThresholdCelsius.collectAsState()
    val isQuietHoursEnabled by viewModel.isQuietHoursEnabled.collectAsState()
    val quietHoursStartHour by viewModel.quietHoursStartHour.collectAsState()
    val quietHoursEndHour by viewModel.quietHoursEndHour.collectAsState()
    val isQuietHoursVibrateOnly by viewModel.isQuietHoursVibrateOnly.collectAsState()
    val appDrainList by viewModel.appDrainList.collectAsState()
    val hasUsageStatsPermission by viewModel.hasUsageStatsPermission.collectAsState()

    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.restoreChargingSessions(context, uri) { success, count ->
                if (success) {
                    Toast.makeText(context, "Successfully restored $count charging sessions!", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Failed to restore backup. Please ensure file is valid JSON or CSV.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.refreshAppDrain()
    }

    DashboardContent(
        info = info,
        powerHistory = powerHistory,
        healthDetails = health,
        chargingHistory = history,
        drainStats = drainStats,
        isFahrenheit = isFahrenheit,
        activeDetailType = activeDetail,
        isSettingsOpen = isSettingsOpen,
        isBackgroundTrackingEnabled = isBackgroundTrackingEnabled,
        isAlarm80Enabled = isAlarm80Enabled,
        isAlarm20Enabled = isAlarm20Enabled,
        selectedTimeFilter = selectedTimeFilter,
        isOverheatAlarmEnabled = isOverheatAlarmEnabled,
        overheatThresholdCelsius = overheatThresholdCelsius,
        isQuietHoursEnabled = isQuietHoursEnabled,
        quietHoursStartHour = quietHoursStartHour,
        quietHoursEndHour = quietHoursEndHour,
        isQuietHoursVibrateOnly = isQuietHoursVibrateOnly,
        appDrainList = appDrainList,
        hasUsageStatsPermission = hasUsageStatsPermission,
        onToggleBackgroundTracking = { viewModel.toggleBackgroundTracking(it) },
        onToggleTemperatureUnit = { viewModel.toggleTemperatureUnit() },
        onToggleAlarm80 = { viewModel.toggleAlarm80(it) },
        onToggleAlarm20 = { viewModel.toggleAlarm20(it) },
        onToggleOverheatAlarm = { viewModel.toggleOverheatAlarm(it) },
        onSetOverheatThreshold = { viewModel.setOverheatThreshold(it) },
        onToggleQuietHours = { viewModel.toggleQuietHours(it) },
        onToggleQuietHoursVibrateOnly = { viewModel.toggleQuietHoursVibrateOnly(it) },
        onClearHistory = { viewModel.clearHistory() },
        onSelectTimeFilter = { viewModel.setTimeFilter(it) },
        onOpenSettings = { viewModel.openSettings() },
        onCloseSettings = { viewModel.closeSettings() },
        onOpenDetail = { viewModel.openDetail(it) },
        onCloseDetail = { viewModel.closeDetail() },
        onRequestUsageStatsPermission = {
            try {
                val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (_: Exception) {}
        },
        onExportCsv = {
            val uri = viewModel.exportChargingSessionsCsv(context)
            if (uri != null) {
                viewModel.shareExportFile(context, uri, "text/csv", "Export Battery Charging Sessions (CSV)")
            }
        },
        onExportJson = {
            val uri = viewModel.exportChargingSessionsJson(context)
            if (uri != null) {
                viewModel.shareExportFile(context, uri, "application/json", "Export Battery Charging Sessions (JSON)")
            }
        },
        onRestoreData = {
            restoreLauncher.launch("*/*")
        },
        onRefreshAppDrain = { viewModel.refreshAppDrain() },
        onResetDrainStats = { viewModel.resetDrainStats() },
        modifier = modifier
    )
}

@Composable
fun DashboardContent(
    info: BatteryInfo,
    powerHistory: List<Double> = emptyList(),
    healthDetails: BatteryHealthDetails,
    chargingHistory: List<ChargingSession>,
    drainStats: BatteryDrainStats = BatteryDrainStats(0f, 0, 0L, 0, 0f, 0f, 0, 0L, 0, 0f, 0f, 0L, 0f, 0f, true, 0, 0.0, 100),
    isFahrenheit: Boolean,
    activeDetailType: DetailType?,
    isSettingsOpen: Boolean,
    isBackgroundTrackingEnabled: Boolean,
    isAlarm80Enabled: Boolean = false,
    isAlarm20Enabled: Boolean = false,
    selectedTimeFilter: TimeRangeFilter = TimeRangeFilter.ALL_TIME,
    isOverheatAlarmEnabled: Boolean = true,
    overheatThresholdCelsius: Int = 42,
    isQuietHoursEnabled: Boolean = false,
    quietHoursStartHour: Int = 22,
    quietHoursEndHour: Int = 7,
    isQuietHoursVibrateOnly: Boolean = true,
    appDrainList: List<AppDrainItem> = emptyList(),
    hasUsageStatsPermission: Boolean = false,
    onToggleBackgroundTracking: (Boolean) -> Unit,
    onToggleTemperatureUnit: () -> Unit,
    onToggleAlarm80: (Boolean) -> Unit = {},
    onToggleAlarm20: (Boolean) -> Unit = {},
    onToggleOverheatAlarm: (Boolean) -> Unit = {},
    onSetOverheatThreshold: (Int) -> Unit = {},
    onToggleQuietHours: (Boolean) -> Unit = {},
    onToggleQuietHoursVibrateOnly: (Boolean) -> Unit = {},
    onClearHistory: () -> Unit = {},
    onSelectTimeFilter: (TimeRangeFilter) -> Unit = {},
    onOpenSettings: () -> Unit,
    onCloseSettings: () -> Unit,
    onOpenDetail: (DetailType) -> Unit,
    onCloseDetail: () -> Unit,
    onRequestUsageStatsPermission: () -> Unit = {},
    onExportCsv: () -> Unit = {},
    onExportJson: () -> Unit = {},
    onRestoreData: () -> Unit = {},
    onRefreshAppDrain: () -> Unit = {},
    onResetDrainStats: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isDarkTheme = isSystemInDarkTheme()
    val bgCanvas = if (isDarkTheme) Color(0xFF070B14) else Color(0xFFF8FAFC)
    val haptic = LocalHapticFeedback.current
    val performHaptic = {
        try {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        } catch (_: Exception) {}
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = bgCanvas
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 1. Header Bar with Settings Button
                HeaderBar(
                    isDarkTheme = isDarkTheme,
                    isCharging = info.isCharging,
                    statusLabel = info.chargingSpeedCategory.label,
                    onOpenSettings = {
                        performHaptic()
                        onOpenSettings()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(34.dp)
                )

                // 2. HERO Micrographic Panel (Adaptive proportional height)
                HeroMicroPanel(
                    info = info,
                    drainStats = drainStats,
                    isDarkTheme = isDarkTheme,
                    powerHistory = powerHistory,
                    onClick = {
                        performHaptic()
                        onOpenDetail(DetailType.HERO_BATTERY)
                    },
                    onOpenDrainDetail = {
                        performHaptic()
                        onOpenDetail(DetailType.BATTERY_DRAIN)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(2.45f)
                )

                // 3. Middle Row: Health & Cycles Micro-Panels (Adaptive proportional height)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1.20f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HealthMicroPanel(
                        health = healthDetails,
                        isDarkTheme = isDarkTheme,
                        onClick = {
                            performHaptic()
                            onOpenDetail(DetailType.BATTERY_HEALTH)
                        },
                        modifier = Modifier
                            .weight(1.15f)
                            .fillMaxSize()
                    )

                    CyclesMicroPanel(
                        info = info,
                        isDarkTheme = isDarkTheme,
                        onClick = {
                            performHaptic()
                            onOpenDetail(DetailType.CYCLES)
                        },
                        modifier = Modifier
                            .weight(0.85f)
                            .fillMaxSize()
                    )
                }

                // 4. Diagnostics Micro-Ribbon Grid (2 x 2) (Adaptive proportional height)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1.60f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Column 1
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Current Flow
                        val currentSign = if (info.currentNowMa > 0) "+" else ""
                        val currentValText = String.format(Locale.US, "%s%d mA", currentSign, info.currentNowMa)

                        MicroDiagnosticCard(
                            icon = AppIcons.Speed,
                            title = "Current Flow",
                            value = currentValText,
                            subtitle = if (info.currentNowMa > 0) "Charging Rate" else "Drain Rate",
                            iconColor = Color(0xFF00E5FF),
                            isDarkTheme = isDarkTheme,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            onClick = {
                                performHaptic()
                                onOpenDetail(DetailType.CURRENT_FLOW)
                            }
                        )

                        // Terminal Volts
                        val voltageValText = String.format(Locale.US, "%.2f V", info.voltageVolts)
                        MicroDiagnosticCard(
                            icon = AppIcons.ElectricBolt,
                            title = "Terminal Volts",
                            value = voltageValText,
                            subtitle = "${info.voltageMv} mV",
                            iconColor = Color(0xFF3B82F6),
                            isDarkTheme = isDarkTheme,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            onClick = {
                                performHaptic()
                                onOpenDetail(DetailType.VOLTAGE)
                            }
                        )
                    }

                    // Column 2
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Temperature
                        val tempState = info.temperatureState
                        val tempValText = if (isFahrenheit) {
                            String.format(Locale.US, "%.1f °F", info.tempFahrenheit)
                        } else {
                            String.format(Locale.US, "%.1f °C", info.tempCelsius)
                        }
                        val tempIconColor = when {
                            info.tempCelsius >= 45f -> Color(0xFFEF4444)
                            info.tempCelsius >= 38f -> Color(0xFFFF9800)
                            else -> Color(0xFF10B981)
                        }

                        MicroDiagnosticCard(
                            icon = AppIcons.Thermostat,
                            title = "Temperature",
                            value = tempValText,
                            subtitle = tempState.label,
                            iconColor = tempIconColor,
                            isDarkTheme = isDarkTheme,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            onClick = {
                                performHaptic()
                                onOpenDetail(DetailType.TEMPERATURE)
                            }
                        )

                        // Power Source
                        val plugValText = when (info.pluggedSource) {
                            PluggedSource.AC -> if (info.isCharging) info.chargerCapacity else "AC Charger"
                            PluggedSource.USB -> if (info.isCharging) info.chargerCapacity else "USB Port"
                            PluggedSource.WIRELESS -> if (info.isCharging) info.chargerCapacity else "Wireless"
                            PluggedSource.DOCK -> "Dock"
                            PluggedSource.UNPLUGGED -> "Discharging"
                        }

                        MicroDiagnosticCard(
                            icon = AppIcons.Power,
                            title = "Power Source",
                            value = plugValText,
                            subtitle = if (info.isCharging) "${info.pluggedSource.name} · ${info.technology}" else info.technology,
                            iconColor = Color(0xFF10B981),
                            isDarkTheme = isDarkTheme,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            onClick = {
                                performHaptic()
                                onOpenDetail(DetailType.POWER_SOURCE)
                            }
                        )
                    }
                }

                // 5. Adaptive Bottom Quick-Access Hub (Sessions, Per-App Drain, Device Hardware)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.95f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Tile 1: Charging Sessions
                    DashboardQuickTile(
                        icon = AppIcons.History,
                        title = "Sessions",
                        value = "${chargingHistory.size} Logged",
                        subtitle = if (chargingHistory.isNotEmpty()) "+${chargingHistory.first().gainedPercent}% recent" else "Timeline",
                        iconColor = Color(0xFFF59E0B),
                        isDarkTheme = isDarkTheme,
                        onClick = {
                            performHaptic()
                            onOpenDetail(DetailType.CHARGING_HISTORY)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                    )

                    // Tile 2: App Drain
                    DashboardQuickTile(
                        icon = AppIcons.Analytics,
                        title = "App Drain",
                        value = if (appDrainList.isNotEmpty()) "${appDrainList.size} Apps" else "Attribution",
                        subtitle = "Screen & Live",
                        iconColor = Color(0xFF06B6D4),
                        isDarkTheme = isDarkTheme,
                        onClick = {
                            performHaptic()
                            onRefreshAppDrain()
                            onOpenDetail(DetailType.APP_DRAIN)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                    )

                    // Tile 3: Device Info
                    DashboardQuickTile(
                        icon = AppIcons.PhoneAndroid,
                        title = "Hardware",
                        value = android.os.Build.MODEL,
                        subtitle = "Diagnostics",
                        iconColor = Color(0xFFA855F7),
                        isDarkTheme = isDarkTheme,
                        onClick = {
                            performHaptic()
                            onOpenDetail(DetailType.DEVICE_INFO)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                    )
                }
            }

            // Modal detail view
            if (activeDetailType != null) {
                DetailModalScreen(
                    detailType = activeDetailType,
                    info = info,
                    healthDetails = healthDetails,
                    chargingHistory = chargingHistory,
                    isFahrenheit = isFahrenheit,
                    selectedFilter = selectedTimeFilter,
                    onFilterChange = onSelectTimeFilter,
                    onDismiss = {
                        performHaptic()
                        onCloseDetail()
                    },
                    drainStats = drainStats,
                    onResetDrainStats = {
                        performHaptic()
                        onResetDrainStats()
                    },
                    onOpenAppDrain = {
                        performHaptic()
                        onRefreshAppDrain()
                        onOpenDetail(DetailType.APP_DRAIN)
                    },
                    appDrainList = appDrainList,
                    hasUsageStatsPermission = hasUsageStatsPermission,
                    onRequestUsageStatsPermission = onRequestUsageStatsPermission,
                    onExportCsv = onExportCsv,
                    onExportJson = onExportJson,
                    onRestoreData = onRestoreData
                )
            }

            // Settings Modal Screen
            if (isSettingsOpen) {
                SettingsModalScreen(
                    isBackgroundTrackingEnabled = isBackgroundTrackingEnabled,
                    isFahrenheit = isFahrenheit,
                    isAlarm80Enabled = isAlarm80Enabled,
                    isAlarm20Enabled = isAlarm20Enabled,
                    onToggleBackgroundTracking = onToggleBackgroundTracking,
                    onToggleTemperatureUnit = onToggleTemperatureUnit,
                    onToggleAlarm80 = onToggleAlarm80,
                    onToggleAlarm20 = onToggleAlarm20,
                    onClearHistory = onClearHistory,
                    onDismiss = onCloseSettings,
                    isOverheatAlarmEnabled = isOverheatAlarmEnabled,
                    overheatThresholdCelsius = overheatThresholdCelsius,
                    onToggleOverheatAlarm = onToggleOverheatAlarm,
                    onSetOverheatThreshold = onSetOverheatThreshold,
                    isQuietHoursEnabled = isQuietHoursEnabled,
                    quietHoursStartHour = quietHoursStartHour,
                    quietHoursEndHour = quietHoursEndHour,
                    isQuietHoursVibrateOnly = isQuietHoursVibrateOnly,
                    onToggleQuietHours = onToggleQuietHours,
                    onToggleQuietHoursVibrateOnly = onToggleQuietHoursVibrateOnly,
                    onExportCsv = onExportCsv,
                    onExportJson = onExportJson,
                    onRestoreData = onRestoreData,
                    onOpenAppDrain = {
                        onRefreshAppDrain()
                        onCloseSettings()
                        onOpenDetail(DetailType.APP_DRAIN)
                    }
                )
            }
        }
    }
}

@Composable
fun HeaderBar(
    isDarkTheme: Boolean,
    isCharging: Boolean,
    statusLabel: String,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val titleColor = colorScheme.onSurface
    val buttonBg = colorScheme.surfaceContainer.copy(alpha = if (isDarkTheme) 0.72f else 0.85f)
    val buttonBorder = colorScheme.outlineVariant.copy(alpha = if (isDarkTheme) 0.30f else 0.45f)
    val buttonIconColor = colorScheme.onSurface

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isCharging) Color(0xFF00E5FF).copy(alpha = 0.2f) else Color(0xFF10B981).copy(alpha = 0.2f),
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isCharging) AppIcons.ElectricBolt else AppIcons.Bolt,
                        contentDescription = null,
                        tint = if (isCharging) Color(0xFF00E5FF) else Color(0xFF10B981),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column {
                Text(
                    text = "Yoyo Battery",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        letterSpacing = (-0.5).sp
                    ),
                    color = titleColor
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isCharging) Color(0xFF00E5FF).copy(alpha = 0.15f) else Color(0xFF10B981).copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isCharging) Color(0xFF00E5FF).copy(alpha = 0.35f) else Color(0xFF10B981).copy(alpha = 0.35f)
                )
            ) {
                Text(
                    text = if (isCharging) "⚡ CHARGING" else "🔋 ACTIVE",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, fontSize = 10.sp),
                    color = if (isCharging) Color(0xFF00E5FF) else Color(0xFF10B981),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            IconButton(onClick = onOpenSettings) {
                Surface(
                    shape = CircleShape,
                    color = buttonBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, buttonBorder),
                    shadowElevation = if (isDarkTheme) 2.dp else 4.dp
                ) {
                    Box(
                        modifier = Modifier.size(36.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = AppIcons.Settings,
                            contentDescription = "Settings",
                            tint = buttonIconColor,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardQuickTile(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    subtitle: String,
    iconColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = isSystemInDarkTheme()
) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surfaceContainer.copy(alpha = if (isDarkTheme) 0.72f else 0.85f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = if (isDarkTheme) 0.30f else 0.45f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = iconColor.copy(alpha = if (isDarkTheme) 0.18f else 0.14f),
                    modifier = Modifier.size(22.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = iconColor,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    ),
                    color = colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp
                ),
                color = colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 10.sp
                ),
                color = colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

