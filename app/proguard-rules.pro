# UzGram ProGuard Rules

# ── Keep app entry points ─────────────────────────────────────────────────────
-keep class com.uzgram.messenger.** { *; }

# ── Kotlin ────────────────────────────────────────────────────────────────────
-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }
-keepclassmembers class **$WhenMappings { <fields>; }
-keepclassmembers class kotlin.Metadata { *; }

# ── Hilt ─────────────────────────────────────────────────────────────────────
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keepclasseswithmembers class * { @dagger.hilt.android.lifecycle.HiltViewModel <methods>; }

# ── Retrofit ──────────────────────────────────────────────────────────────────
-keepattributes Signature
-keepattributes Exceptions
-keep class retrofit2.** { *; }
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# ── OkHttp ────────────────────────────────────────────────────────────────────
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }

# ── Gson ─────────────────────────────────────────────────────────────────────
-keepattributes *Annotation*
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# ── Room ─────────────────────────────────────────────────────────────────────
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# ── Socket.IO ─────────────────────────────────────────────────────────────────
-keep class io.socket.** { *; }
-keep class io.socket.client.** { *; }

# ── Coil ─────────────────────────────────────────────────────────────────────
-keep class coil.** { *; }

# ── WebRTC ────────────────────────────────────────────────────────────────────
-keep class org.webrtc.** { *; }
-keepclassmembers class org.webrtc.** { *; }

# ── BouncyCastle ─────────────────────────────────────────────────────────────
-keep class org.bouncycastle.** { *; }
-dontwarn org.bouncycastle.**

# ── ExoPlayer ─────────────────────────────────────────────────────────────────
-keep class androidx.media3.** { *; }

# ── Lottie ────────────────────────────────────────────────────────────────────
-keep class com.airbnb.lottie.** { *; }

# ── Firebase ─────────────────────────────────────────────────────────────────
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# ── Data Transfer Objects ─────────────────────────────────────────────────────
-keep class com.uzgram.messenger.data.remote.dto.** { *; }
-keep class com.uzgram.messenger.domain.model.** { *; }
-keep class com.uzgram.messenger.data.local.entity.** { *; }

# ── Suppress warnings ─────────────────────────────────────────────────────────
-dontwarn com.google.errorprone.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
