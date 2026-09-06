# Meta Ray-Ban Assistant

Android companion app for Meta Ray-Ban smart glasses, built on the **Meta Wearables Device Access
Toolkit**. It registers the app with the glasses, discovers and selects a device, opens an SDK
session and routes audio to the glasses' speakers over Bluetooth HFP — the groundwork for driving
the glasses from a phone-side app rather than from Meta's own assistant.

It ships with its own **over-the-air update channel**, so builds reach the phone without going
through the Play Store.

## Architecture

- **Wearables session layer** (`data/source/WearablesManager.kt`) — wraps the Meta Wearables SDK:
  observes registration state, enumerates paired glasses, opens and closes a session, and exposes
  connection state as Kotlin `StateFlow`s the UI collects.
- **Audio routing** — once a session is live, audio output is switched to the Bluetooth SCO/HFP
  device so a notification tone plays through the glasses rather than the phone speaker.
- **Bluetooth device selection** (`ui/screens/DeviceSelectionScreen.kt`) — runtime permission
  handling for `BLUETOOTH_SCAN` and `BLUETOOTH_CONNECT`, then a list of discovered devices.
- **Self-hosted OTA updates** (`data/source/UpdateChecker.kt`, `ApkInstaller.kt`) — the app polls a
  `version.json` served from this repository, with aggressive cache busting so a fresh publish is
  seen immediately, downloads the APK attached to the matching GitHub Release, and hands it to the
  package installer through a `FileProvider`.
- **Single-activity Compose UI** — Material 3, state hoisted into the activity, one home screen that
  surfaces registration state, connection state and update availability.

## Stack

Kotlin, Jetpack Compose (Material 3), Meta Wearables Device Access Toolkit (`mwdat-core`,
`mwdat-camera` 0.4.0), Coroutines, Retrofit + OkHttp + Gson, Gradle KTS. minSdk 29, targetSdk 34.

## Build

```bash
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

Release builds are signed from a `keystore.properties` file at the repository root (keys:
`storeFile`, `storePassword`, `keyAlias`, `keyPassword`). That file and the keystore itself are
gitignored; without them, `assembleRelease` falls back to the debug signing config so the project
still builds from a clean clone.

To ship an update: bump `versionCode` and `versionName`, build the release APK, attach it to a
GitHub Release, then update `version.json` on the default branch — installed apps pick it up on the
next check.

## Status

Personal project, distributed only through this OTA channel. Not on the Play Store. Requires Meta
Ray-Ban glasses paired with the phone and the Meta AI app installed for SDK registration to succeed.

## Licence

No licence granted. Published for reading.
