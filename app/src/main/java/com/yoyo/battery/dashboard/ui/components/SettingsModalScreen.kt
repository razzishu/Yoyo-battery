package com.yoyo.battery.dashboard.ui.components

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SettingsModalScreen(
    isBackgroundTrackingEnabled: Boolean,
    isFahrenheit: Boolean,
    onToggleBackgroundTracking: (Boolean) -> Unit,
    onToggleTemperatureUnit: () -> Unit,
    onDismiss: () -> Unit,
    isAlarm80Enabled: Boolean = false,
    isAlarm20Enabled: Boolean = false,
    onToggleAlarm80: (Boolean) -> Unit = {},
    onToggleAlarm20: (Boolean) -> Unit = {},
    onClearHistory: () -> Unit = {},
    isOverheatAlarmEnabled: Boolean = true,
    overheatThresholdCelsius: Int = 42,
    onToggleOverheatAlarm: (Boolean) -> Unit = {},
    onSetOverheatThreshold: (Int) -> Unit = {},
    isQuietHoursEnabled: Boolean = false,
    quietHoursStartHour: Int = 22,
    quietHoursEndHour: Int = 7,
    isQuietHoursVibrateOnly: Boolean = true,
    onToggleQuietHours: (Boolean) -> Unit = {},
    onToggleQuietHoursVibrateOnly: (Boolean) -> Unit = {},
    onExportCsv: () -> Unit = {},
    onExportJson: () -> Unit = {},
    onRestoreData: () -> Unit = {},
    onOpenAppDrain: () -> Unit = {}
) {
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme
    val isDarkTheme = isSystemInDarkTheme()
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val performHaptic = {
        try {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        } catch (_: Exception) {}
    }

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

    var showClearConfirmation by remember { mutableStateOf(false) }

    val prefs = remember { context.getSharedPreferences("yoyo_settings_prefs", Context.MODE_PRIVATE) }
    var isBatteryOptDismissed by remember {
        mutableStateOf(prefs.getBoolean("battery_opt_card_dismissed", false))
    }

    // Check runtime notification permission status (Android 13+ / API 33)
    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    // Check system battery optimization exemption status
    var isBatteryOptimizationIgnored by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
                powerManager?.isIgnoringBatteryOptimizations(context.packageName) ?: false
            } else {
                true
            }
        )
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    hasNotificationPermission = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
                    isBatteryOptimizationIgnored = powerManager?.isIgnoringBatteryOptimizations(context.packageName) ?: true
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
        if (isGranted) {
            onToggleBackgroundTracking(true)
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
                            .padding(20.dp)
                    ) {
                        // Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "App Settings & Service",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                ),
                                color = colorScheme.onSurface
                            )

                            IconButton(onClick = {
                                performHaptic()
                                dismissWithAnimation()
                            }) {
                                Surface(
                                    shape = CircleShape,
                                    color = colorScheme.surfaceContainerHigh
                                ) {
                                    Box(
                                        modifier = Modifier.size(32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("✕", fontWeight = FontWeight.Bold, color = colorScheme.onSurface)
                                    }
                                }
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. Background Session Tracker Switch
                    ElevatedCard(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = colorScheme.surfaceContainerHigh)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Background Session Tracking",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (isBackgroundTrackingEnabled) "Service Active (Background Logging On)" else "Service Disabled",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isBackgroundTrackingEnabled) Color(0xFF4CAF50) else colorScheme.onSurfaceVariant
                                    )
                                }

                                Switch(
                                    checked = isBackgroundTrackingEnabled,
                                    onCheckedChange = { newState ->
                                        performHaptic()
                                        if (newState) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
                                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                            } else {
                                                onToggleBackgroundTracking(true)
                                            }
                                        } else {
                                            onToggleBackgroundTracking(false)
                                        }
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = Color(0xFF4CAF50)
                                    )
                                )
                            }
                        }
                    }

                    // 2. Battery Protection Alarms
                    ElevatedCard(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = colorScheme.surfaceContainerHigh)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Text(
                                text = "Battery Health Alarms",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = colorScheme.onSurface
                            )

                            // 80% Alarm
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "80% Charge Limit Alert",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Alerts to unplug and extend battery lifespan.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = isAlarm80Enabled,
                                    onCheckedChange = {
                                        performHaptic()
                                        onToggleAlarm80(it)
                                    }
                                )
                            }

                            HorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.5f))

                            // 20% Alarm
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "20% Low Battery Alert",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Alerts to plug in and avoid deep cell exhaustion.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = isAlarm20Enabled,
                                    onCheckedChange = {
                                        performHaptic()
                                        onToggleAlarm20(it)
                                    }
                                )
                            }

                            HorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.5f))

                            // Critical Overheat Alarm
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "🔥 Critical Overheat Alert",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Warns immediately when cell temperature reaches dangerous levels.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = isOverheatAlarmEnabled,
                                    onCheckedChange = {
                                        performHaptic()
                                        onToggleOverheatAlarm(it)
                                    }
                                )
                            }

                            if (isOverheatAlarmEnabled) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    listOf(40, 42, 45).forEach { threshold ->
                                        val isSel = overheatThresholdCelsius == threshold
                                        Surface(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable {
                                                    performHaptic()
                                                    onSetOverheatThreshold(threshold)
                                                },
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (isSel) Color(0xFFEF4444) else colorScheme.surfaceVariant,
                                            contentColor = if (isSel) Color.White else colorScheme.onSurfaceVariant
                                        ) {
                                            Box(
                                                modifier = Modifier.padding(vertical = 8.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "${threshold}°C / ${((threshold * 9 / 5) + 32)}°F",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 2B. Nightly Smart Charging & Quiet Hours
                    ElevatedCard(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = colorScheme.surfaceContainerHigh)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "🌙 Nightly Quiet Hours Schedule",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (isQuietHoursEnabled) "Active (${quietHoursStartHour}:00 – ${quietHoursEndHour}:00)" else "Disabled",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isQuietHoursEnabled) Color(0xFF38BDF8) else colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = isQuietHoursEnabled,
                                    onCheckedChange = {
                                        performHaptic()
                                        onToggleQuietHours(it)
                                    }
                                )
                            }

                            if (isQuietHoursEnabled) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Vibrate Only Mode",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                            color = colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Suppresses audio alarms; pulses gentle vibration only.",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Switch(
                                        checked = isQuietHoursVibrateOnly,
                                        onCheckedChange = {
                                            performHaptic()
                                            onToggleQuietHoursVibrateOnly(it)
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // 2C. Per-App Battery Drain Shortcut
                    ElevatedCard(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = colorScheme.surfaceContainerHigh)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "⚡ Per-App Battery Drain",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = colorScheme.onSurface
                                )
                                Text(
                                    text = "Inspect foreground runtimes and screen-on energy consumption.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colorScheme.onSurfaceVariant
                                )
                            }
                            Button(
                                onClick = {
                                    performHaptic()
                                    onOpenAppDrain()
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF), contentColor = Color(0xFF080C14))
                            ) {
                                Text("View ➔", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // 3A. Required Notification Permission Card (disappears immediately once granted)
                    if (!hasNotificationPermission) {
                        ElevatedCard(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = Color(0xFFFFF3E0),
                                contentColor = Color(0xFFE65100)
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Action Required: Notification Permission",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFFE65100)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Notification permission is required to run the battery monitoring service in the background and display live charging status in the status bar.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFBF360C)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = {
                                        performHaptic()
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFE65100),
                                        contentColor = Color.White
                                    )
                                ) {
                                    Text("Grant Notification Permission ➔", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // 3B. Recommended Battery Optimization Card (dismissible and auto-hides once unrestricted)
                    if (hasNotificationPermission && !isBatteryOptimizationIgnored && !isBatteryOptDismissed) {
                        ElevatedCard(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.elevatedCardColors(containerColor = colorScheme.surfaceContainerHigh)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "💡 Recommended: Unrestricted Battery Mode",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = colorScheme.onSurface
                                )
                                Text(
                                    text = "Exempt Yoyo Battery from battery optimization so Android does not pause background session tracking or alarms while your phone is asleep.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colorScheme.onSurfaceVariant
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            performHaptic()
                                            isBatteryOptDismissed = true
                                            prefs.edit().putBoolean("battery_opt_card_dismissed", true).apply()
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Dismiss")
                                    }
                                    Button(
                                        onClick = {
                                            performHaptic()
                                            requestIgnoreBatteryOptimization(context)
                                        },
                                        modifier = Modifier.weight(1.3f),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = colorScheme.primary,
                                            contentColor = colorScheme.onPrimary
                                        )
                                    ) {
                                        Text("Unrestrict", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    // 4. Temperature Unit Switch
                    ElevatedCard(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = colorScheme.surfaceContainerHigh)
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
                                    text = "Temperature Unit",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = colorScheme.onSurface
                                )
                                Text(
                                    text = if (isFahrenheit) "Display in Fahrenheit (°F)" else "Display in Celsius (°C)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colorScheme.onSurfaceVariant
                                )
                            }

                            Button(
                                onClick = {
                                    performHaptic()
                                    onToggleTemperatureUnit()
                                },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(if (isFahrenheit) "°F" else "°C", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // 5. Data & Privacy Management (Clear History)
                    ElevatedCard(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = colorScheme.surfaceContainerHigh)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Data & History Management",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Reset recorded charging sessions and calibration history stored in the local SQLite database.",
                                style = MaterialTheme.typography.bodySmall,
                                color = colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            // Data Export & Restore Buttons
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        performHaptic()
                                        onExportCsv()
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                                ) {
                                    Text("Export CSV 📊", fontWeight = FontWeight.Bold, color = Color.White)
                                }
                                Button(
                                    onClick = {
                                        performHaptic()
                                        onExportJson()
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                                ) {
                                    Text("Export JSON 💾", fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }

                            Button(
                                onClick = {
                                    performHaptic()
                                    onRestoreData()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA855F7))
                            ) {
                                Text("Restore / Import Backup 📥", fontWeight = FontWeight.Bold, color = Color.White)
                            }

                            if (!showClearConfirmation) {
                                OutlinedButton(
                                    onClick = {
                                        performHaptic()
                                        showClearConfirmation = true
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626))
                                ) {
                                    Text("Clear Charging History", fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            performHaptic()
                                            showClearConfirmation = false
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Cancel")
                                    }
                                    Button(
                                        onClick = {
                                            performHaptic()
                                            onClearHistory()
                                            showClearConfirmation = false
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                                    ) {
                                        Text("Confirm Delete", color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    // 6. About Yoyo Battery Section
                    Card(
                        modifier = Modifier.clip(RoundedCornerShape(20.dp)),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = colorScheme.surfaceContainer.copy(alpha = if (isDarkTheme) 0.75f else 0.88f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        border = BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = if (isDarkTheme) 0.30f else 0.45f))
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF38BDF8).copy(alpha = 0.16f),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("⚡", fontSize = 18.sp)
                                    }
                                }
                                Column {
                                    Text(
                                        text = "About Yoyo Battery",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Version 2.0.5 · Codename: \"firststeps\" (Build 3)",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = colorScheme.primary
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.35f))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = """
                                    Yoyo Battery is a privacy-first battery and hardware companion designed to give you real-time visibility into charging speed, battery health, and battery longevity.
                                    
                                    ⚡ 1. Live Power Meter & Coulomb Integration:
                                    Measures charging speed in Watts (W) and current flow (mAh/min) with accurate real-time delta integration.
                                    
                                    🎨 2. Material You & Glassmorphic UI:
                                    Expressive adaptive wallpaper palette with iOS-style translucent frosted glass depth and colorful icon accents.
                                    
                                    📱 3. Deep Hardware & Multi-Tab Diagnostics:
                                    Swipeable hardware specifications across SOC, Device, System, Battery, Thermal, Sensors, and Dual Cameras with authentic non-guessed telemetry.
                                    
                                    📊 4. Private Charging History Log:
                                    Stores sessions privately on your device. Filter by Day, Week, Month, 3 Months, or All Time with CSV/JSON export.
                                    
                                    🌡️ 5. Thermal & Battery Protection Alarms:
                                    Color-coded thermal diagnostics, 80% charge limit warnings, and 20% cell exhaustion alerts.
                                """.trimIndent(),
                                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                                color = colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // 7. Community, Source Code & Support Section
                    Card(
                        modifier = Modifier.clip(RoundedCornerShape(20.dp)),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = colorScheme.surfaceContainer.copy(alpha = if (isDarkTheme) 0.75f else 0.88f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        border = BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = if (isDarkTheme) 0.30f else 0.45f))
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF10B981).copy(alpha = 0.16f),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("🌐", fontSize = 18.sp)
                                    }
                                }
                                Column {
                                    Text(
                                        text = "Community & Support",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = colorScheme.onSurface
                                    )
                                    Text(
                                        text = "GitHub, Telegram & F-Droid Releases",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = Color(0xFF10B981)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            HorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.35f))
                            Spacer(modifier = Modifier.height(10.dp))

                            val openUrl: (String) -> Unit = { url ->
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    }
                                    context.startActivity(intent)
                                } catch (_: Exception) {}
                            }

                            // GitHub Link Tile
                            SupportLinkRow(
                                icon = AppIcons.GitHub,
                                iconTint = if (isDarkTheme) Color.White else Color(0xFF181717),
                                iconBg = if (isDarkTheme) Color(0xFF24292E) else Color(0xFFE2E8F0),
                                title = "GitHub Repository",
                                subtitle = "github.com/razzishu/Yoyo-battery",
                                onClick = {
                                    performHaptic()
                                    openUrl("https://github.com/razzishu/Yoyo-battery")
                                }
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Telegram Link Tile
                            SupportLinkRow(
                                icon = AppIcons.Telegram,
                                iconTint = Color(0xFF229ED9),
                                iconBg = Color(0xFF229ED9).copy(alpha = 0.16f),
                                title = "Telegram Channel",
                                subtitle = "t.me/Yoyoapps · Official Community",
                                onClick = {
                                    performHaptic()
                                    openUrl("https://t.me/Yoyoapps")
                                }
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // F-Droid / Open Source Releases Tile
                            SupportLinkRow(
                                icon = AppIcons.FDroid,
                                iconTint = Color(0xFF1976D2),
                                iconBg = Color(0xFF1976D2).copy(alpha = 0.16f),
                                title = "F-Droid & Direct Releases",
                                subtitle = "Free & Open Source software catalog",
                                onClick = {
                                    performHaptic()
                                    openUrl("https://github.com/razzishu/Yoyo-battery/releases")
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        performHaptic()
                        dismissWithAnimation()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Close Settings", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
}
}

private fun requestIgnoreBatteryOptimization(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        try {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:${context.packageName}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            return
        } catch (_: Exception) {}
        try {
            val fallback = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(fallback)
            return
        } catch (_: Exception) {}
    }
    try {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (_: Exception) {
        try {
            val fallbackIntent = Intent(Settings.ACTION_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(fallbackIntent)
        } catch (_: Exception) {}
    }
}

@Composable
private fun SupportLinkRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    iconBg: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val isDarkTheme = isSystemInDarkTheme()

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = colorScheme.surfaceContainerHigh.copy(alpha = if (isDarkTheme) 0.55f else 0.80f),
        border = BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = if (isDarkTheme) 0.20f else 0.35f)),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = iconBg,
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                    color = colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                imageVector = AppIcons.OpenInNew,
                contentDescription = null,
                tint = colorScheme.onSurfaceVariant.copy(alpha = 0.70f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
