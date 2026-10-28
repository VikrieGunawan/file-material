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

# Explicitly keep default constructors to resolve R8 constructor warnings
-keep public class com.google.vending.licensing.ILicensingService { void <init>(); }
-keep public class com.android.vending.licensing.ILicensingService { void <init>(); }
-keep public class com.google.android.vending.licensing.ILicensingService { void <init>(); }
-keep class android.support.annotation.Keep { void <init>(); }
-keep,allowrepackaging,allowobfuscation,allowshrinking class * extends androidx.navigation.Navigator { void <init>(); }
-keep,allowshrinking class * extends androidx.compose.ui.node.ModifierNodeElement { void <init>(); }
-keep,allowshrinking class okhttp3.internal.publicsuffix.PublicSuffixDatabase { void <init>(); }
-keep class * implements androidx.versionedparcelable.VersionedParcelable { void <init>(); }
-keep public class androidx.versionedparcelable.ParcelImpl { void <init>(); }
-keep,allowshrinking class * extends androidx.startup.Initializer { void <init>(); }
-keep,allowrepackaging,allowobfuscation @interface androidx.annotation.Keep

# Strip verbose/debug logs in Release builds to shrink code
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
}
