# Kotlin Serialization
-keepattributes *Annotation*, EnclosingMethod, InnerClasses
-keepclassmembers class ** {
    @kotlinx.serialization.SerialName <fields>;
}
-keepclassmembers class com.example.medicinetracker.data.model.** {
    *** <fields>;
}
-keep,allowobfuscation,allowshrinking class kotlinx.serialization.json.JsonObjectSerializer
-keep,allowobfuscation,allowshrinking class kotlinx.serialization.json.JsonArraySerializer
-keep,allowobfuscation,allowshrinking class kotlinx.serialization.json.JsonPrimitiveSerializer

# Room
-keep class * extends androidx.room.RoomDatabase
-keep class com.example.medicinetracker.data.local.** { *; }

# Models
-keep class com.example.medicinetracker.data.model.** { *; }

# Android Security / Tink
-keep class androidx.security.crypto.** { *; }
-dontwarn com.google.errorprone.annotations.**
-dontwarn javax.annotation.**
