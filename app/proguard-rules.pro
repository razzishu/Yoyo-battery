# ProGuard & R8 optimization rules for Yoyo Battery

# 1. Keep all app package classes, methods, and constructors
-keep class com.yoyo.battery.dashboard.** { *; }
-keepclassmembers class com.yoyo.battery.dashboard.** { *; }
-keepclassmembernames class com.yoyo.battery.dashboard.** { *; }

# 2. Keep ViewModels & AndroidViewModels and their reflection constructors
-keep class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}
-keep class * extends androidx.lifecycle.AndroidViewModel {
    <init>(...);
}
-keep class com.yoyo.battery.dashboard.ui.BatteryViewModel {
    public <init>(android.app.Application);
}

# 3. Keep Android framework components
-keep class * extends android.app.Activity { *; }
-keep class * extends android.app.Service { *; }
-keep class * extends android.content.BroadcastReceiver { *; }
-keep class * extends android.content.ContentProvider { *; }
-keep class * extends android.app.Application { *; }

# 4. Keep Data Models
-keep class com.yoyo.battery.dashboard.data.model.** { *; }
-keepclassmembers class com.yoyo.battery.dashboard.data.model.** { *; }

# 5. Keep SQLite OpenHelper & Database classes
-keep class * extends android.database.sqlite.SQLiteOpenHelper { *; }
-keep class com.yoyo.battery.dashboard.data.db.** { *; }

# 6. Keep Coroutines & Flows
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# 7. Keep Compose runtime and layout internals
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# 8. Suppress warnings for PowerProfile internal reflection
-dontwarn com.android.internal.os.PowerProfile
-dontnote com.android.internal.os.PowerProfile
-dontwarn com.yoyo.battery.dashboard.**
