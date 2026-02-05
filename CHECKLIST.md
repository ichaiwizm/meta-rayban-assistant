# ✅ Implementation Checklist - Bluetooth Meta Ray-Ban

## Before You Build

### 🔑 Required Configuration

- [ ] **GitHub Personal Access Token**
  - [ ] Created at https://github.com/settings/tokens/new
  - [ ] Scope: `read:packages` selected
  - [ ] Added to `gradle.properties` (replace `YOUR_GITHUB_TOKEN_HERE`)

- [ ] **Meta Application ID**
  - [ ] Account created at https://developers.meta.com/
  - [ ] Wearables app created
  - [ ] Application ID copied (UUID format)
  - [ ] Added to `AndroidManifest.xml` (replace `YOUR_META_APPLICATION_ID_HERE`)

---

## Build Verification

### 📦 Configuration Check
- [ ] `gradle.properties` contains valid GitHub token
- [ ] `AndroidManifest.xml` contains valid Meta App ID
- [ ] `gradle.properties` is in `.gitignore`

### 🔨 Build Steps
- [ ] Run `./gradlew clean`
- [ ] Run `./gradlew --refresh-dependencies`
- [ ] Gradle sync completes without errors
- [ ] Run `./gradlew assembleDebug`
- [ ] APK builds successfully

---

## Code Verification

### ✨ Files Created
- [ ] `data/model/BluetoothDevice.kt`
- [ ] `data/model/ConnectionState.kt`
- [ ] `data/source/BluetoothManager.kt`
- [ ] `ui/screens/DeviceSelectionScreen.kt`

### 📝 Files Modified
- [ ] `gradle.properties` - GitHub token
- [ ] `settings.gradle.kts` - Meta Maven repo
- [ ] `app/build.gradle.kts` - minSdk=29, version=2.0.0, Meta SDK deps
- [ ] `AndroidManifest.xml` - Bluetooth permissions + Meta App ID
- [ ] `MainActivity.kt` - Bluetooth integration
- [ ] `HomeScreen.kt` - Bluetooth UI

### 🔍 Version Check
- [ ] `minSdk = 29` (Android 10)
- [ ] `versionCode = 10`
- [ ] `versionName = "2.0.0"`
- [ ] Meta SDK dependencies added (core, camera, mockdevice)

---

## Testing

### 🧪 Mock Device Testing (Debug Build)
- [ ] Install APK on device (API 29+)
- [ ] Launch app
- [ ] Click "Connecter aux lunettes"
- [ ] Bluetooth permission dialog appears
- [ ] Grant permissions
- [ ] Device selection sheet appears
- [ ] Mock device visible in list
- [ ] Can select mock device
- [ ] Connection status updates
- [ ] Can disconnect

### 🕶️ Physical Glasses Testing
- [ ] Glasses paired via Android Settings
- [ ] Glasses powered on
- [ ] Launch app
- [ ] Click "Connecter aux lunettes"
- [ ] Glasses appear in device list
- [ ] Can connect to glasses
- [ ] Status shows "Connecté à [name]"
- [ ] Can disconnect
- [ ] Connection survives screen rotation
- [ ] Connection survives app backgrounding

---

## Runtime Checks

### 📱 Permissions
- [ ] BLUETOOTH_SCAN permission requested (Android 12+)
- [ ] BLUETOOTH_CONNECT permission requested (Android 12+)
- [ ] Permissions granted successfully
- [ ] App handles permission denial gracefully

### 🔄 State Management
- [ ] Connection state updates properly
- [ ] UI reflects current state (Disconnected/Scanning/Connecting/Connected/Error)
- [ ] Device list updates during scan
- [ ] Status messages are clear
- [ ] Error messages are user-friendly

### 🎨 UI/UX
- [ ] Bluetooth section visible on HomeScreen
- [ ] Button text changes (Connecter/Déconnecter)
- [ ] Button color changes when connected (red)
- [ ] Status card displays correctly
- [ ] Device selection bottom sheet works
- [ ] Loading indicator shows during scan
- [ ] Can dismiss device selection sheet

---

## Edge Cases

### 🛡️ Error Handling
- [ ] Handles no devices found
- [ ] Handles connection failure
- [ ] Handles permission denial
- [ ] Handles Bluetooth disabled
- [ ] Handles disconnect during use
- [ ] Shows appropriate error messages

### 🔁 Lifecycle
- [ ] BluetoothManager cleaned up in onDestroy
- [ ] No memory leaks
- [ ] StateFlow collectors work correctly
- [ ] App doesn't crash on rotation
- [ ] App doesn't crash on background/foreground

---

## Documentation

- [ ] Read `BLUETOOTH_IMPLEMENTATION.md`
- [ ] Understand architecture
- [ ] Know how to configure credentials
- [ ] Know limitations (no camera/audio yet)
- [ ] Know next iteration goals

---

## Security

- [ ] GitHub token NOT committed to Git
- [ ] `gradle.properties` in `.gitignore`
- [ ] Meta App ID is public (OK to commit)
- [ ] No hardcoded credentials in code

---

## Final Verification

- [ ] All checkboxes above are checked
- [ ] App builds without errors
- [ ] App installs on device
- [ ] App launches without crash
- [ ] Can scan for devices
- [ ] Can connect to mock device
- [ ] Can disconnect from device
- [ ] UI updates correctly

---

## Issues?

If any checkbox fails:
1. Check error messages carefully
2. Verify credentials are correct
3. Ensure device is Android 10+ (API 29+)
4. Check logs: `adb logcat | grep -E "BluetoothManager|Meta"`
5. Try rebuilding: `./gradlew clean assembleDebug`
6. Try with mock device before physical glasses

---

**Status:** Ready for testing
**Version:** 2.0.0
**Date:** 2026-02-04
