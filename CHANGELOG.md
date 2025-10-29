# Changelog

All notable changes to the Pegel Wear OS app will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2025-10-29

### Added
- Initial release of Pegel drinking tracker for Wear OS 3.0+
- Five drink type tracking: Beer (🍺), Wine (🍷), Shot (🥃), Cocktail (🍸), Long Drink (🍹)
- Wear OS Tile with quick-access emoji buttons for instant drink logging
- Haptic feedback (50ms vibration) when logging drinks via tile
- Main app view showing today's drink log with timestamps
- Summary card displaying total drinks and breakdown by type
- Empty state screen when no drinks have been logged
- Reset functionality with confirmation dialog to clear today's log
- Data persistence using DataStore Preferences
- Automatic daily filtering (only shows drinks from current day)
- Offline-first architecture - no internet connection required
- Privacy-focused: all data stored locally on watch only
- Material Design 3 theming optimized for Wear OS
- Jetpack Compose UI with ScalingLazyColumn for smooth scrolling
- MVVM architecture with ViewModel and Repository pattern
- Kotlin Coroutines and Flow for reactive data streams
- JSON serialization for drink log storage
- ProGuard/R8 configuration for release builds
- Comprehensive documentation (README.md, DEPLOYMENT.md, PROJECT_STRUCTURE.md)

### Technical Details
- **Min SDK**: 30 (Android 11 - required for Wear OS 3.0)
- **Target SDK**: 34 (Android 14)
- **Kotlin**: 1.9.22
- **Compose**: 1.6.0
- **Wear Compose**: 1.3.0
- **Wear Tiles**: 1.2.0
- **DataStore**: 1.0.0
- **Lifecycle**: 2.7.0
- **Horologist**: 0.5.18

### Architecture
- Clean Architecture with separate UI, Data, and Domain layers
- Repository pattern for data access abstraction
- StateFlow for reactive UI updates
- Tile Service with BroadcastReceiver for tile button actions
- Composition over inheritance for Compose UI components

### Known Limitations
- Historical data beyond today is stored but not displayed in UI
- No settings screen yet (planned for future release)
- No export functionality (planned for future release)
- Tile requires user to add it manually to watch face
- No watch face complications support yet

### Tested On
- Google Pixel Watch
- Wear OS 3.0+
- Android 11+ (API level 30+)

---

## Future Releases

See [README.md](README.md#roadmap) for planned features and enhancements.
