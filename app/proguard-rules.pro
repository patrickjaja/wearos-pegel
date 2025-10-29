# ProGuard rules for Pegel Wear OS app

# Compose
-keep class androidx.compose.** { *; }

# Wear OS
-keep class androidx.wear.** { *; }

# Tiles
-keep class androidx.wear.tiles.** { *; }

# DataStore
-keep class androidx.datastore.** { *; }

# Keep Kotlin metadata
-keepattributes RuntimeVisibleAnnotations
-keepattributes SourceFile,LineNumberTable
-keep class kotlin.Metadata { *; }

# Keep BuildConfig
-keep class com.pegel.wearos.BuildConfig { *; }
