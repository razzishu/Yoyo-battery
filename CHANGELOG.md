# Changelog

All notable changes and special implementations in Yoyo Battery are documented here.

## [2.0.5] - 2026-10-26 (Codename: firststeps)

### 🚀 Universal Device Info & Multi-OEM Reliability
- **Universal Hardware Resolution**: Enhanced hardware fetching to accurately detect device details and marketing names across Xiaomi, Samsung, OnePlus, Oppo, Realme, Vivo, iQOO, Motorola, Google Pixel, and Unisoc devices.
- **Exact SoC Disambiguation**: Eliminated ambiguous slash-separated naming (e.g., MediaTek Dimensity 7300, 7300 Energy, 7300-Ultra) by using OEM brand metadata, platform properties, and clock thresholds to pinpoint the exact silicon variant.
- **Comprehensive Camera2 Hardware Diagnostics**: Added detailed optical and sensor properties including pixel pitch (microns) with pixel-binning ratios, optical field of view (FOV in degrees), Camera2 API hardware level (Level 3/Full), ISO sensitivity range, shutter speed exposure times, focus systems (PDAF/Macro), optical/digital zoom range, and RAW10/DNG capture support.
- **Static Spec Caching & Performance**: Cached heavy one-time hardware queries (Camera2 characteristics, EGL14 GPU profiles, sensor lists, display metrics) to prevent camera service locks and driver delays, while keeping CPU core frequencies, load, and thermals dynamic with sub-millisecond latency.

### 🔋 Hero Mastercard & Tachometer Meter Redesign
- **Refined Card Visual Hierarchy**: Organized the Mastercard with a prominent Charging Status Card at the top, and a sleek Remaining Hours Card right below it with bold typography and balanced sizing.
- **Precision Tachometer Battery Meter**: Replaced the previous circular gauge with a futuristic automotive tachometer design featuring 22 precision tick markers, multi-stop radiant gradient track, pulsing glow aura, and an illuminated beacon dot at the arc tip.
- **Rich First-Glance Metrics**: Instant visibility into live power (Watts) with sparkline history, instantaneous amperage (mA), terminal voltage (V), temperature (°C), battery health %, chemistry technology, capacity (mAh), and active/idle drain rates.

### 🎨 Clean UI & Visual Defect Fixes
- **Eliminated Tinted Square Shadows**: Removed tonal elevation artifacts behind translucent cards across the dashboard and popups by resetting card elevation to 0.dp and applying precise rounded corner clipping.
- **Compact & High-Density Statusbar Notification**: Redesigned ongoing notification so it stays in compact normal mode by default while displaying all vital metrics (Level %, Status, Watts, Time to Full/Remaining, Current mA, Terminal Volts, and Temp °C), smoothly expanding on pull-down for full battery telemetry.
- **Brand-New Yo-Yo + Battery Concept Logo**: Introduced a distinctive app icon featuring twin aerodynamic spinning yo-yo crescent discs surrounding an illuminated battery cell with an electric charge bolt, updated across all launcher mipmaps (`hdpi`, `mdpi`, `xhdpi`, `xxhdpi`, `xxxhdpi`) and vector drawables.

### 🛡️ Deep Stability, Memory-Leak Prevention & Audit
- **Zero File Descriptor & Native Leaks**: Converted all Linux kernel virtual sysfs and `/proc` file accesses to AutoCloseable `bufferedReader().use` patterns, and enclosed EGL14 GPU driver display/surface teardowns in `try ... finally` blocks.
- **Instant Charger Polarity Snapping**: Fixed EMA filter lag where disconnecting or connecting a charger would slowly drift across 0mA for several seconds; current now snaps instantaneously on polarity change while maintaining smooth jitter filtering.
- **Android 14+ Broadcast Compatibility**: Explicitly passed `RECEIVER_EXPORTED` flag for background power and battery state receivers to prevent `SecurityException` crashes on Android 14 and 15.
- **Live Delta CPU Calculation**: Replaced cumulative lifetime average with real-time jiffy delta calculation for accurate instant CPU load percentage.
- **Defensive AppWidget Updates**: Guarded widget update callbacks against remote view and system service exceptions.

---

## [1.1.0] - 2026-10-25

### 🎨 Material You & Glassmorphic Redesign
- **Adaptive Wallpaper Colors**: Unified all dashboard cards to use standard Material You expressive dynamic wallpaper colors instead of mismatched background colors, creating a cohesive and elegant look.
- **iOS-Style Frosted Glass Popups**: Modal sheets for telemetry diagnostics and app settings now feature translucent frosted glass styling with subtle borders and elevation.
- **Vibrant Icon Badges**: Cards and quick tiles now feature colorful, high-saturation circular badges to keep metrics recognizable while keeping backgrounds and text clean and easy to read.
- **Smooth Spring Animations**: Seamless scale, fade, and slide transitions with spring physics when opening and closing popup screens.

### 📱 Swipeable Hardware Diagnostics
- **Horizontal Swipe Between Tabs**: The Device Info popup now supports fluid swipe gestures across all 7 hardware categories (SOC, Device, System, Battery, Thermal, Sensors, Cameras) with synchronized category chip scrolling.

### ⚡ Calculations & Reliability Audit
- **Telemetry Precision**: Guarded all battery health, drain rate, and session integration math against zero divisions, negative values, and idle fluctuations.
- **Settings & Version Update**: Bumped app version to 1.1 (Build 2) with refreshed architecture details and updated telemetry explanations.

---

## [1.0.0] - 2026-10-25

### ⚡ Hardware & Device Specifications (Hardware Diagnostics Engine)
- **Live Kernel Core Frequencies**: Directly reads Linux kernel cpufreq EAS policies (`/sys/devices/system/cpu/cpufreq/policy0..15`) instead of guessing. Shows real-time MHz for each physical core and clearly identifies sleeping/offline cores as "Stopped".
- **Physical Display Panel Specs**: Queries unscaled physical display metrics (`getRealMetrics`), true panel resolution (e.g., 1080×2400), dynamic refresh rates (60Hz / 90Hz / 120Hz), HDR support, and pixel density.
- **Accurate Chipset & Board Detection**: Automatically identifies the silicon manufacturer (Qualcomm Snapdragon, MediaTek Dimensity, Google Tensor, Samsung Exynos) from Android board properties and system files, avoiding generic fallback names.
- **Hardware Sensor & Biometric Telemetry**: Reads integrated sensor hardware (6-axis gyro, step counter, barometer, NFC, optical fingerprint) with vendor names and power consumption.
- **Camera Array & Sensor Capture**: Lists active Camera2 output capture resolutions, hardware OIS detection, focal lengths, aperture, and includes a clear disclaimer explaining quad-bayer pixel binning.
- **Adaptive Text Formatting**: Stat rows automatically switch to a two-line stacked layout for long technical values (build fingerprint, kernel version, resolution streams) so titles never get squeezed into vertical columns.

### 🔋 Background Session Tracking & Reliability
- **Instant Charger Detection**: Registered event-driven system broadcast receivers (`ACTION_POWER_CONNECTED`, `ACTION_POWER_DISCONNECTED`, `ACTION_BATTERY_CHANGED`) so plug and unplug events start/stop session recording immediately without polling delays.
- **Boot Persistence**: Added `BootCompletedReceiver` to automatically restore background session tracking after your phone restarts.
- **Smarter Permission Handling**: 
  - Required Notification permission is clearly separated from optional system settings.
  - The notification card automatically disappears the instant permission is granted.
  - Added an ON_RESUME lifecycle observer so returning from phone settings immediately updates permission status without restarting the app.
  - The battery optimization card can now be dismissed or granted directly via Android's native one-tap exemption prompt.
- **Private SQLite History**: All charging sessions are stored locally in an offline SQLite database. Filter history by Day, Week, Month, 3 Months, or All Time.

### 🛡️ Battery Health Engine & Protection Alarms
- **Coulomb Counter Integration**: Estimates true battery capacity and wear by measuring actual electric charge (mAh) delivered across benchmark charging sessions.
- **80% Charge Limit Alert**: Optional alarm that reminds you to unplug at 80% to reduce chemical stress and extend cell longevity.
- **20% Low Battery Alert**: Reminds you to plug in before the battery enters deep discharge state.
- **Critical Overheat Alert**: Immediate warning if cell temperature reaches dangerous levels (configurable: 40°C, 42°C, 45°C).
- **Nightly Quiet Hours**: Automatic silent or vibration-only mode during chosen night hours (default 22:00 to 07:00).

### 🎨 Clean Layouts & Visual Polish
- **Balanced Metric Panels**: Mastercard, current flow, voltage, temperature, and power source popups have balanced padding and spacing for high-density readability.
- **Live Power & Speed Badges**: Color-coded charging speed categories (Slow, Normal, Fast, Rapid, Turbo) and real-time Wattage calculation.
- **Data Portability**: Full CSV and JSON export and restore options for all your charging logs.
