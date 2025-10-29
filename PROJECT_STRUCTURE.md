# Pegel Wear OS Project Structure

## Overview
Pegel is a drinking tracker app for Wear OS 3.0+ built with Kotlin and Compose for Wear OS.

## Directory Structure

```
wearos-pegel/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/pegel/wearos/
│   │   │   │   ├── MainActivity.kt              # Main entry point
│   │   │   │   └── ui/theme/
│   │   │   │       ├── Color.kt                 # Compose colors
│   │   │   │       └── Theme.kt                 # Material theme
│   │   │   ├── res/
│   │   │   │   └── values/
│   │   │   │       ├── strings.xml              # String resources
│   │   │   │       ├── colors.xml               # XML color definitions
│   │   │   │       └── themes.xml               # XML theme definitions
│   │   │   └── AndroidManifest.xml              # App manifest
│   │   ├── androidTest/
│   │   └── test/
│   ├── build.gradle.kts                         # App-level build config
│   └── proguard-rules.pro                       # ProGuard configuration
├── build.gradle.kts                             # Project-level build config
├── settings.gradle.kts                          # Gradle settings
├── gradle.properties                            # Gradle properties
└── .gitignore                                   # Git ignore rules
```

## Key Configuration Details

### Build Configuration
- **Gradle Version**: 8.2.0
- **Kotlin Version**: 1.9.22
- **Minimum SDK**: 30 (Wear OS 3.0)
- **Target SDK**: 34
- **Compile SDK**: 34
- **Java Version**: 11

### Dependencies
- **Wear OS**: androidx.wear:wear:1.3.0
- **Compose**: 1.6.0
- **Wear Compose**: 1.3.0
- **Wear Tiles**: 1.2.0
- **DataStore**: 1.0.0
- **Horologist**: 0.5.18 (Google's Wear OS Compose extensions)

### App Configuration
- **Package Name**: com.pegel.wearos
- **App Name**: Pegel
- **Permissions**: VIBRATE
- **Features**: Wear OS watch hardware

## Next Steps

### Phase 1: Core Features
- [ ] Implement main drinking tracker UI
  - [ ] Display current daily water intake
  - [ ] Quick action buttons (200ml, 250ml, 500ml)
  - [ ] Progress towards daily goal
- [ ] Implement data persistence with DataStore
- [ ] Add haptic feedback for water intake logging

### Phase 2: Navigation & Settings
- [ ] Implement swipe-based navigation
- [ ] Create settings screen
  - [ ] Configurable daily goal
  - [ ] Unit preferences (ml, liters, oz)
- [ ] Add water intake history view

### Phase 3: Tiles & Complications
- [ ] Implement Wear OS Tile service
  - [ ] Quick water logging tile
  - [ ] Current intake display
- [ ] Add watch face complications

### Phase 4: Advanced Features
- [ ] Reminder notifications
- [ ] Sync with companion app (when created)
- [ ] Health integration (if available)
- [ ] Multiple profiles/users

## Development Notes

### Building the Project
```bash
./gradlew build
```

### Running on Emulator
```bash
./gradlew assembleDebug
```

### Formatting Code
```bash
./gradlew ktlint
```

### Testing
```bash
./gradlew test
./gradlew connectedAndroidTest
```

## Architecture Decisions

- **UI Framework**: Jetpack Compose with Wear OS extensions
- **State Management**: ViewModel + DataStore (to be implemented)
- **Navigation**: Compose Navigation (to be implemented)
- **Theming**: Material Design 3 with Wear OS customizations
- **Dependency Injection**: Manual injection (can be enhanced with Hilt later)

## Resource Files

- `strings.xml`: All user-facing text strings
- `colors.xml`: XML color definitions for XML-based resources
- `Color.kt`: Compose-friendly color definitions
- `Theme.kt`: Material theme configuration for Compose

## Notes

- All TODO comments indicate future implementation areas
- The Tile service declaration in AndroidManifest.xml is commented out until implemented
- The project uses Kotlin DSL (`.kts`) for Gradle configuration
- ProGuard rules are configured for production builds
