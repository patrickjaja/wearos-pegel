# Pegel - Wear OS Drinking Tracker

A lightweight and intuitive drinking tracker app for Wear OS 3.0+ smartwatches, including Google Pixel Watch. "Pegel" (German slang for alcohol level/BAC) helps you keep track of your alcoholic beverages consumed throughout the day.

## Features

- **Quick Drink Logging**: One-tap emoji buttons to log different drink types (🍺 Beer, 🍷 Wine, 🥃 Shot, 🍸 Cocktail, 🍹 Long Drink)
- **Daily Log View**: View all drinks logged today with timestamps, sorted by most recent
- **Drink Summary**: See total count and breakdown by drink type at a glance
- **Wear OS Tile**: Quick-access tile on your watch face for instant drink logging without opening the app
- **Haptic Feedback**: Get tactile confirmation when logging drinks via the tile
- **Data Persistence**: Your drink history is saved locally on the watch using DataStore
- **Reset Functionality**: Clear today's log with a confirmation dialog
- **Lightweight & Battery Efficient**: Minimal impact on device battery life
- **Material Design 3**: Modern, watch-optimized UI using Jetpack Compose
- **Offline-First**: Works completely offline without internet connection
- **Privacy-Focused**: All data stays on your watch - no cloud sync, no tracking

## Screenshots

_Screenshots coming soon_

## Installation

For detailed step-by-step deployment instructions, see [DEPLOYMENT.md](DEPLOYMENT.md).

### Quick Start

1. **Prerequisites**: Ensure you have Android Studio installed and your Pixel Watch is paired with your phone
2. **Enable Developer Mode**: Follow the guide in [DEPLOYMENT.md](DEPLOYMENT.md#step-1-enable-developer-mode-on-pixel-watch)
3. **Build & Install**:
   ```bash
   ./gradlew assembleDebug
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```
4. **Add to Watch Face**: Swipe left on your watch face, select edit, and add the Pegel tile

## How to Use

### Logging Drinks via Tile (Recommended)

1. **Access Tile**: Swipe left from your watch face to access tiles
2. **Find Pegel Tile**: Locate the Pegel tile (shows "Today: X")
3. **Quick Log**: Tap one of the five emoji buttons to log a drink:
   - **🍺 Beer**: Standard beer
   - **🍷 Wine**: Glass of wine
   - **🥃 Shot**: Spirit shot
   - **🍸 Cocktail**: Mixed drink
   - **🍹 Long Drink**: Long mixed drink
4. **Haptic Feedback**: Feel a short vibration confirming your log
5. **View Count**: The tile updates immediately showing total drinks today

### Viewing Your Log

1. **Open the App**: Tap the Pegel icon in your app drawer, or tap the tile area
2. **Empty State**: If no drinks logged, you'll see "🍺 No drinks yet - Use the tile to log"
3. **Log View**: Once drinks are logged, you'll see:
   - **Summary Card**: Total count and breakdown by drink type (e.g., "🍺 3  🍷 2")
   - **Today's Log**: Chronological list showing each drink with timestamp (HH:MM format)
   - **Reset Button**: Clear all drinks logged today
4. **Reset Confirmation**: Tapping "Reset All" shows a confirmation dialog to prevent accidents

## Tech Stack

### Mobile Framework
- **Wear OS**: 3.0+
- **Kotlin**: 1.9.22
- **Android API Level**: 30 (min) to 34 (target)

### UI Framework
- **Jetpack Compose**: 1.6.0
- **Wear Compose**: 1.3.0
- **Material Design 3**: For modern, consistent theming

### Core Libraries
- **Wear Tiles**: 1.2.0 (for tile implementation)
- **Horologist**: 0.5.18 (Google's Wear OS Compose extensions)
- **DataStore**: 1.0.0 (for local data persistence)
- **Lifecycle**: 2.7.0 (for ViewModel and lifecycle management)
- **Kotlin Serialization**: 1.6.0 (for data serialization)

### Build & Development
- **Gradle**: 8.2.0
- **Android Studio**: 2023.1+
- **Java**: 11+

## Project Structure

```
wearos-pegel/
├── app/                          # Main app module
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/pegel/wearos/
│   │   │   │   ├── MainActivity.kt           # Main app entry point with Compose UI
│   │   │   │   ├── data/                     # Data layer
│   │   │   │   │   ├── DrinkLog.kt           # Data class for drink entries
│   │   │   │   │   ├── DrinkType.kt          # Enum for drink types (Beer, Wine, etc.)
│   │   │   │   │   └── DrinkRepository.kt    # Repository for DataStore operations
│   │   │   │   ├── tile/                     # Tile-related classes
│   │   │   │   │   ├── DrinkTileService.kt   # Tile service implementation
│   │   │   │   │   └── TileActionReceiver.kt # Broadcast receiver for tile actions
│   │   │   │   └── ui/
│   │   │   │       ├── MainViewModel.kt      # ViewModel for state management
│   │   │   │       └── theme/
│   │   │   │           ├── Color.kt          # Compose color definitions
│   │   │   │           └── Theme.kt          # Material theme setup
│   │   │   ├── res/
│   │   │   │   ├── values/
│   │   │   │   │   ├── strings.xml           # String resources
│   │   │   │   │   ├── colors.xml            # XML color definitions
│   │   │   │   │   └── themes.xml            # Theme definitions
│   │   │   │   ├── drawable/                 # App icons & drawables
│   │   │   │   └── mipmap/                   # Launcher icons
│   │   │   └── AndroidManifest.xml           # App manifest (activity + tile service)
│   │   ├── test/                             # Unit tests (TBD)
│   │   └── androidTest/                      # Instrumentation tests (TBD)
│   ├── build.gradle.kts                      # App-level build config
│   └── proguard-rules.pro                    # ProGuard/R8 rules
├── build.gradle.kts                          # Project-level build config
├── settings.gradle.kts                       # Gradle settings
├── gradle.properties                         # Gradle configuration
├── gradlew & gradlew.bat                     # Gradle wrapper scripts
├── DEPLOYMENT.md                             # Detailed deployment guide
├── PROJECT_STRUCTURE.md                      # Project architecture details
├── CHANGELOG.md                              # Version history
└── README.md                                 # This file
```

## Development Guide

### Building the Project

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK (requires signing configuration)
./gradlew assembleRelease

# Run all tests
./gradlew test

# Format Kotlin code
./gradlew ktlint

# Build and run on device
./gradlew installDebug
```

### Running on Emulator

1. Open Android Studio
2. Click **Device Manager** (phone icon on right toolbar)
3. Create a new virtual device with Wear OS 3.0 or later
4. Run the app: **Run** → **Run 'app'**

### Running on Physical Device

See [DEPLOYMENT.md](DEPLOYMENT.md) for detailed instructions on:
- Enabling developer mode
- Setting up ADB debugging
- Installing via command line or Android Studio
- Troubleshooting common issues

### Code Style

The project uses Kotlin and follows:
- **Kotlin Coding Conventions**: [kotlinlang.org](https://kotlinlang.org/docs/coding-conventions.html)
- **Android Code Style**: [Google's Android Code Style](https://developer.android.com/kotlin/style-guide)
- **Compose Best Practices**: [Compose API Guidelines](https://developer.android.com/jetpack/compose/api-guidelines)

### Testing

```bash
# Run all unit tests
./gradlew test

# Run instrumentation tests on device
./gradlew connectedAndroidTest

# Run specific test class
./gradlew test --tests "com.pegel.wearos.YourTestClass"
```

## Architecture

### UI Layer
- **MainActivity.kt**: Entry point with Compose UI, renders main screen
  - Empty state when no drinks logged
  - Scrollable drink log list with summary card
  - Reset dialog with confirmation
- **MainViewModel.kt**: Manages UI state using StateFlows
  - Observes drink logs from repository
  - Provides actions (reset, log drink)
- **Jetpack Compose for Wear OS**: Declarative UI with ScalingLazyColumn
- **Material Design 3**: Wear OS optimized theme

### Data Layer
- **DrinkRepository.kt**: Single source of truth for drink data
  - Uses DataStore Preferences for persistence
  - Provides Flow-based reactive data streams
  - Auto-filters logs to show only today's drinks
- **DrinkLog.kt**: Immutable data class with timestamp and drink type
  - Helper methods: `isFromToday()`, `getTimeOfDay()`
- **DrinkType.kt**: Enum defining 5 drink types with emoji and display names
- **Kotlin Serialization**: JSON serialization for DrinkLog list storage

### Tile Integration
- **DrinkTileService.kt**: Renders tile UI with ProtoLayout
  - Displays "Today: X" count at top
  - Shows 5 emoji buttons (Beer, Wine, Shot, Cocktail, Long Drink)
  - Updates immediately when drinks are logged
- **TileActionReceiver.kt**: BroadcastReceiver handling tile button taps
  - Logs drink to repository
  - Triggers haptic feedback (50ms vibration)
  - Requests tile update via TileService

## Roadmap

### Version 1.0.0 (Current - Initial Release)
- [x] Basic drink logging via tile with 5 drink types
- [x] Drink log view with timestamps
- [x] Summary card showing totals by type
- [x] Data persistence with DataStore
- [x] Haptic feedback on tile button press
- [x] Reset functionality with confirmation dialog
- [x] Daily auto-filter (only shows today's drinks)
- [x] Wear OS Tile service with emoji buttons

### Future Enhancements (Planned)
- [ ] Weekly/monthly view with historical data
- [ ] Export log as CSV or text
- [ ] Custom drink types with user-defined emojis
- [ ] Statistics view (busiest day, favorite drink, etc.)
- [ ] Dark theme / theme customization
- [ ] Watch face complications
- [ ] Reminder to pace drinking
- [ ] Estimated BAC calculator (with disclaimer)
- [ ] Water tracker integration (alternate mode)
- [ ] Companion phone app with detailed analytics

## Contributing

To contribute to Pegel:

1. Create a feature branch from `main`
2. Make your changes and ensure tests pass
3. Follow the code style guidelines
4. Submit a pull request with a clear description

## License

This project is provided as-is for personal and educational use.

## Data Storage & Privacy

### What Gets Stored
- **Drink logs**: Timestamp and drink type for each logged drink
- **Storage location**: Local DataStore on the watch only
- **Data format**: JSON-serialized list stored in app preferences

### Privacy
- **No internet required**: App works 100% offline
- **No cloud sync**: All data stays on your watch
- **No analytics**: No tracking or telemetry
- **No permissions**: App doesn't request any special permissions
- **Manual reset**: You control when to clear your data

### Data Retention
- Drinks are automatically filtered to show only today's entries
- Historical data remains in DataStore but isn't displayed
- Use "Reset All" button to clear all stored data
- Uninstalling the app removes all data permanently

## Support & Troubleshooting

For deployment and installation issues, refer to [DEPLOYMENT.md](DEPLOYMENT.md#troubleshooting).

Common issues:
- **ADB connection problems**: See [DEPLOYMENT.md](DEPLOYMENT.md#adb-connection-issues)
- **Build failures**: See [DEPLOYMENT.md](DEPLOYMENT.md#build-issues)
- **Installation errors**: See [DEPLOYMENT.md](DEPLOYMENT.md#installation-issues)
- **Tile not appearing**: Restart Wear OS launcher or reinstall app
- **App crashes**: Check logcat output with `adb logcat | grep pegel`

## Useful Resources

- [Wear OS Developer Documentation](https://developer.android.com/wear)
- [Jetpack Compose for Wear OS](https://developer.android.com/wear/compose)
- [Wear Tiles Development Guide](https://developer.android.com/wear/tiles)
- [Material Design 3 for Watches](https://m3.material.io/)
- [Android Debug Bridge (ADB) Guide](https://developer.android.com/tools/adb)
- [Pixel Watch Features & Support](https://support.google.com/pixelwatch)

## Acknowledgments

Built with:
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Horologist by Google](https://github.com/google/horologist)
- [Kotlin Language](https://kotlinlang.org/)
- [Material Design](https://material.io/)

---

**Version**: 1.0.0
**Target Devices**: Pixel Watch, Wear OS 3.0+
**Status**: Active Development

For questions or feedback, please check the project documentation or reach out through the appropriate channels.
