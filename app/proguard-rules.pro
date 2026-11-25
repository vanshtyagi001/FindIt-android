# Keep Retrofit & Gson data models
-keepattributes Signature, *Annotation*
-keep class com.vanshtyagi.findit.data.remote.** { *; }
-keepclassmembers class com.vanshtyagi.findit.data.remote.** { *; }

# Gson
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken
