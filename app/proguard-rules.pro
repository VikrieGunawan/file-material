# ProGuard & R8 Optimization rules for File Material

# Keep attributes for reflection, annotations, and generics
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Keep File Material data models
-keep class com.file.material.data.model.** { *; }

# Keep AppPreferences
-keep class com.file.material.data.preferences.** { *; }

# Keep AndroidX FileProvider
-keep class androidx.core.content.FileProvider { *; }

# Keep ViewModel constructors for lifecycle reflection
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# Keep Compose Runtime and Preview
-dontwarn androidx.compose.**
-dontwarn androidx.lifecycle.**
-dontwarn coil.**
-dontwarn okio.**
-dontwarn kotlinx.coroutines.**

# Strip verbose/debug logs in Release builds to shrink code
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
}
