# Pegel Wear OS - Deployment Guide

This guide provides step-by-step instructions to build and deploy the Pegel drinking tracker app to a Pixel Watch.

## Prerequisites

Before you begin, ensure you have the following:

- **Android Studio** (version 2023.1 or later) - Download from [developer.android.com](https://developer.android.com/studio)
- **Pixel Watch** (running Wear OS 3.0 or later)
- **Android phone** paired with your Pixel Watch via Google Wear OS app
- **USB-C cable** (optional, for direct connection via ADB)
- **USB debugging enabled** on both phone and watch
- **Java 11 or later** installed on your development machine

## Step 1: Enable Developer Mode on Pixel Watch

### Via Phone Settings (Recommended):

1. On your **Android phone**, open the **Wear OS Companion app**
2. Tap **Advanced settings** (gear icon)
3. Tap **Developer options**
4. Toggle **Enable ADB debugging** to **ON**
5. Toggle **Debug over Bluetooth** to **ON**

### Alternative - Via Watch Settings:

1. On your **Pixel Watch**, go to **Settings** → **System** → **About watch**
2. Scroll down to **Build number** and **tap it 7 times** (repeatedly tap)
   - You'll see: "Developer mode enabled" or similar message
3. Go back and navigate to **Settings** → **System** → **Developer options**
4. Toggle **ADB debugging** to **ON**
5. On your **phone's Wear OS app**, enable **Debug over Bluetooth**

## Step 2: Enable ADB Debugging Over Bluetooth

### On Your Phone:

1. Open **Settings** → **Developer options** (tap Build number 7 times if not visible)
2. Toggle **USB Debugging** to **ON**
3. Open the **Wear OS Companion app**
4. Tap **Advanced settings** (gear icon)
5. Tap **Developer options**
6. Toggle **Debug over Bluetooth** to **ON**

### On Your Watch:

1. Go to **Settings** → **System** → **Developer options** (see Step 1 if needed)
2. Toggle **ADB debugging** to **ON**
3. Keep your watch nearby (Bluetooth connection required)

## Step 3: Set Up ADB Connection

### Connect via Bluetooth (No USB Cable Needed):

Open a terminal/command prompt on your computer and run:

```bash
adb devices
```

You should see your watch listed. If not, try:

```bash
# List connected Bluetooth devices
adb devices -l

# Connect to the watch (replace PORT with the port shown, typically 5037)
adb connect <watch-ip>:5555
```

**Note:** To find your watch's IP address, go to **Settings** → **System** → **About** → **Status** on your watch and look for the IP address.

### Optional - Connect via USB:

If your watch supports USB-C connection:

1. Connect your Pixel Watch to your computer via USB-C cable
2. Run:
   ```bash
   adb devices
   ```
3. Your watch should appear in the list

### Verify Connection:

```bash
adb devices
```

Expected output:
```
List of attached devices
ABCD1234E56F7890     device
```

If you see `unauthorized`, approve the ADB debugging request on your watch.

## Step 4: Build the APK

Navigate to the project directory and build the debug APK:

```bash
# Using Gradle wrapper (recommended)
./gradlew assembleDebug

# Or if using Windows
gradlew.bat assembleDebug
```

The APK will be generated at:
```
app/build/outputs/apk/debug/app-debug.apk
```

### Build Options:

- **Debug APK**: `./gradlew assembleDebug` (fast, includes debug info)
- **Release APK**: `./gradlew assembleRelease` (optimized, requires signing)
- **Full Build**: `./gradlew build` (runs tests and creates all APK variants)

## Step 5: Install the APK to Your Watch

### Install Using ADB:

```bash
# Install the debug APK
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Install via Android Studio (Graphical):

1. Open **Android Studio**
2. Click **Run** → **Run 'app'** (or press `Shift + F10`)
3. Select your Pixel Watch from the device list
4. Click **OK**
5. Wait for the app to build and install

### Verify Installation:

```bash
# Check if app is installed
adb shell pm list packages | grep com.pegel.wearos

# Output should show:
# package:com.pegel.wearos
```

## Step 6: Add the Tile to Your Watch Face

The Pegel app includes a Wear OS tile for quick access to the drinking tracker:

1. On your **Pixel Watch**, swipe left from the watch face (or tap and hold the watch face)
2. Tap **Edit** or **Customize**
3. Swipe to the **Tiles** section
4. Tap the **+** icon to add a new tile
5. Search for **"Pegel"** or scroll to find it
6. Tap **Pegel** to add the tile
7. Tap the checkmark or **Done** to save

### Launch the App:

- **From watch face**: Swipe to the Pegel tile and tap it
- **From app menu**: Swipe up from watch face to open app drawer and tap Pegel
- **From launcher**: Use voice command "Open Pegel"

## Step 7: Run and Test the App

### Launch the App:

```bash
# Open the app via ADB
adb shell am start -n com.pegel.wearos/.MainActivity
```

### View App Logs:

```bash
# View real-time logs from the app
adb logcat | grep pegel

# Or filter by app package
adb logcat --pid=$(adb shell pidof com.pegel.wearos)
```

### Check App Permissions:

```bash
# Verify required permissions are granted
adb shell pm list permissions -d | grep pegel
```

## Troubleshooting

### ADB Connection Issues

**Problem**: `adb devices` shows "unauthorized"

**Solutions**:
1. Check your watch screen for an "Allow ADB debugging?" dialog
2. Tap **Allow** or **OK** to authorize the connection
3. Disconnect and reconnect:
   ```bash
   adb disconnect
   adb connect <watch-ip>:5555
   ```
4. Restart the ADB daemon:
   ```bash
   adb kill-server
   adb start-server
   adb devices
   ```

**Problem**: `adb devices` shows nothing or "offline"

**Solutions**:
1. Verify Bluetooth is enabled on both phone and watch
2. Ensure Watch is paired with phone in Bluetooth settings
3. Enable "Debug over Bluetooth" in Wear OS Companion app
4. Check watch IP address: **Settings** → **System** → **About** → **Status**
5. Try connecting directly:
   ```bash
   adb connect <watch-ip>:5555
   ```
6. Restart both devices (watch and phone)

**Problem**: `adb: command not found`

**Solutions**:
1. Add Android SDK tools to PATH:
   - **Linux/macOS**: `export PATH=$PATH:~/Android/Sdk/platform-tools`
   - **Windows**: Add `C:\Users\YourUsername\AppData\Local\Android\Sdk\platform-tools` to System PATH
2. Or use full path: `/path/to/android/sdk/platform-tools/adb devices`
3. Verify Android SDK is installed in Android Studio

### Build Issues

**Problem**: Build fails with "Gradle sync failed"

**Solutions**:
1. Clean build cache:
   ```bash
   ./gradlew clean
   ./gradlew build
   ```
2. Update Gradle wrapper:
   ```bash
   ./gradlew wrapper --gradle-version=8.2.0
   ```
3. Check Java version (requires 11+):
   ```bash
   java -version
   ```
4. In Android Studio: **File** → **Invalidate Caches** → **Invalidate and Restart**

**Problem**: "Unable to locate Android SDK"

**Solutions**:
1. Set `ANDROID_HOME` environment variable:
   - **Linux/macOS**: `export ANDROID_HOME=~/Android/Sdk`
   - **Windows**: Set `ANDROID_HOME=C:\Users\YourUsername\AppData\Local\Android\Sdk`
2. In Android Studio: **File** → **Settings** → **Appearance & Behavior** → **System Settings** → **Android SDK** → Set SDK location

### Installation Issues

**Problem**: `adb install` fails with "INSTALL_FAILED_INVALID_APK"

**Solutions**:
1. Rebuild the APK:
   ```bash
   ./gradlew clean assembleDebug
   ```
2. Verify APK exists at the expected location:
   ```bash
   ls -la app/build/outputs/apk/debug/app-debug.apk
   ```
3. Try installing with verbose output:
   ```bash
   adb install -r -g app/build/outputs/apk/debug/app-debug.apk
   ```

**Problem**: `adb install` fails with "INSTALL_FAILED_INSUFFICIENT_STORAGE"

**Solutions**:
1. Free up space on the watch (delete unused apps)
2. Try uninstalling first:
   ```bash
   adb uninstall com.pegel.wearos
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

**Problem**: App crashes on launch

**Solutions**:
1. Check logs for errors:
   ```bash
   adb logcat | grep -E "pegel|Crash|Exception"
   ```
2. Verify app is installed:
   ```bash
   adb shell pm list packages | grep com.pegel.wearos
   ```
3. Clear app data:
   ```bash
   adb shell pm clear com.pegel.wearos
   ```
4. Reinstall the app:
   ```bash
   adb uninstall com.pegel.wearos
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

### Tile Issues

**Problem**: Tile doesn't appear in tile list

**Solutions**:
1. Ensure app is properly installed:
   ```bash
   adb shell pm list packages | grep com.pegel.wearos
   ```
2. Restart Wear OS launcher:
   ```bash
   adb shell am force-stop com.google.android.wearable.watchfaces
   adb shell am start -n com.google.android.wearable.watchfaces/com.google.android.wearable.watchfaces.watchfaceserver.WatchFaceServer
   ```
3. Reinstall the app and wait 30 seconds for discovery
4. Check AndroidManifest.xml to ensure TileService is properly declared

**Problem**: Tile crashes when tapped

**Solutions**:
1. View tile service logs:
   ```bash
   adb logcat | grep -E "DrinkTileService|TileService"
   ```
2. Ensure tile implementation is complete
3. Clear app cache:
   ```bash
   adb shell pm clear com.pegel.wearos
   ```

### Performance Issues

**Problem**: App is slow or laggy

**Solutions**:
1. Check for ANR (Application Not Responding) errors:
   ```bash
   adb logcat | grep ANR
   ```
2. Profile the app using Android Studio Profiler
3. Check device memory usage:
   ```bash
   adb shell dumpsys meminfo com.pegel.wearos
   ```
4. Restart the watch

## Testing Checklist

Use this checklist to verify the app is working correctly:

### Installation & Launch
- [ ] App builds without errors
- [ ] App installs successfully via ADB
- [ ] App appears in Wear OS app drawer
- [ ] App launches without crashing
- [ ] App renders UI correctly on watch screen

### Core Functionality
- [ ] Can see current water intake display
- [ ] Quick action buttons are responsive
- [ ] Tapping buttons logs water intake
- [ ] Progress bar updates correctly
- [ ] Daily reset occurs at midnight

### Tile Integration
- [ ] Tile appears in tile selection menu
- [ ] Tile can be added to watch face
- [ ] Tile displays current water intake
- [ ] Tile quick actions work correctly
- [ ] Tapping tile opens main app

### Navigation
- [ ] Can swipe through screens
- [ ] Settings screen is accessible
- [ ] Can navigate back to main screen
- [ ] No navigation crashes

### Data Persistence
- [ ] Water intake is saved between sessions
- [ ] Settings are preserved after app restart
- [ ] Data persists after device restart
- [ ] Daily intake resets each day

### Hardware Integration
- [ ] Vibration feedback works
- [ ] Touch input is responsive
- [ ] Display updates smoothly
- [ ] Battery usage is reasonable

### Error Handling
- [ ] App handles device rotation gracefully
- [ ] App recovers from crashes
- [ ] Error messages are user-friendly
- [ ] No permission denied errors

## Additional Resources

- [Wear OS Documentation](https://developer.android.com/wear)
- [Wear Tiles Developer Guide](https://developer.android.com/wear/tiles)
- [Android Debug Bridge (ADB) Reference](https://developer.android.com/tools/adb)
- [Pixel Watch Developer Documentation](https://developer.android.com/wear/android-5-1-apis)
- [Compose for Wear OS](https://developer.android.com/wear/compose)

## Uninstalling the App

To remove the app from your Pixel Watch:

```bash
adb uninstall com.pegel.wearos
```

Or via Settings:
1. On your watch, go to **Settings** → **Apps** → **App permissions**
2. Find **Pegel**
3. Tap **Uninstall**

## Updating the App

To push a new version to your watch:

1. Make code changes
2. Rebuild the APK:
   ```bash
   ./gradlew assembleDebug
   ```
3. Reinstall:
   ```bash
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```
   (The `-r` flag replaces the existing app)

## Getting Help

If you encounter issues not covered in this guide:

1. Check the Android Logcat for error messages
2. Review the project's GitHub issues
3. Consult the Wear OS documentation
4. Enable verbose logging: `adb logcat -v threadtime`
