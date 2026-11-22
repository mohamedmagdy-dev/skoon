-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.sukoon.launcher.data.**$$serializer { *; }
-keepclassmembers class com.sukoon.launcher.data.** {
    *** Companion;
}
-keepclasseswithmembers class com.sukoon.launcher.data.** {
    kotlinx.serialization.KSerializer serializer(...);
}
