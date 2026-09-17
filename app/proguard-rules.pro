# Add project specific ProGuard rules here.
-keepattributes *Annotation*
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class com.fittracker.app.data.remote.** { *; }
-keep class com.fittracker.app.data.local.entities.** { *; }
