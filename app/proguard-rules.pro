# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# General Android rules
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class com.android.vending.licensing.ILicensingService

# Keep accessibility service
-keep class com.rewardsbot.service.SearchAccessibilityService { *; }
-keep class com.rewardsbot.service.FloatingButtonService { *; }
-keep class com.rewardsbot.service.SearchService { *; }

# Keep MainActivity and ViewModels
-keep class com.rewardsbot.MainActivity { *; }
-keep class com.rewardsbot.viewmodel.** { *; }

# Keep search generator utility
-keep class com.rewardsbot.utils.SearchGenerator { *; }

# Kotlin serialization and coroutines
-keepattributes *Annotation*
-dontwarn kotlinx.**
-keep class kotlinx.coroutines.** { *; }

# Keep data classes and their properties
-keepclassmembers class * {
    @com.squareup.moshi.Json <fields>;
}
-keep @com.squareup.moshi.JsonClass class *
-keepclassmembers @com.squareup.moshi.JsonClass class * { <init>(); }

# Moshi rules
-keep class com.squareup.moshi.** { *; }
-keep interface com.squareup.moshi.** { *; }
-dontwarn okio.**
-dontwarn javax.annotation.**
-keepclasseswithmembers class * {
    @com.squareup.moshi.* <methods>;
}
-keepclasseswithmembers class * {
    @com.squareup.moshi.* <fields>;
}

# OkHttp rules
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# Jetpack Compose rules
-keep class androidx.compose.** { *; }
-keep @androidx.compose.runtime.Stable class *
-keep class * extends androidx.compose.runtime.RememberObserver
-keep class kotlin.reflect.jvm.internal.** { *; }

# Keep material design components
-keep class com.google.android.material.** { *; }
-dontwarn com.google.android.material.**

# Prevent warnings and errors
-dontwarn java.lang.instrument.ClassFileTransformer
-dontwarn sun.misc.SignalHandler
-dontwarn java.lang.instrument.Instrumentation
-dontwarn sun.misc.Signal
-dontwarn java.beans.**

# Keep line numbers for debugging
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Remove logging in release builds
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}

# Keep enum classes
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep Parcelable implementations
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Keep custom exceptions
-keep public class * extends java.lang.Exception

# Prevent obfuscation of classes with native methods
-keepclasseswithmembernames class * {
    native <methods>;
}