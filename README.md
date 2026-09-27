# B4Assistant

Android Quick Settings tiles and root-powered shortcuts. Forked from [MeowAssistant](https://github.com/MeowDump/MeowAssistant) under its MIT license.

## Features

- Screenshot, Wi-Fi, mobile data, private DNS, Caffeine, and lock screen tiles
- Script shortcuts for QuietKill and Integrity-Box modules
- Root status and module availability checks

## Requirements

- Android 10 or newer
- Root access for system actions and scripts
- Optional [QuietKill](https://github.com/MeowDump/QuietKill) or [Integrity-Box](https://github.com/MeowDump/Integrity-Box) for their respective shortcuts

## Build

Use Android Studio with JDK 17 and Android SDK 36, or run `./gradlew assembleDebug`. The app uses Android Gradle Plugin 8.13.2 and Gradle 8.13.

The package ID is `com.b4lol.assistant`, so this fork installs separately from the original app. Release builds use R8 and resource shrinking.

## Install

Build the APK, install it, grant root access when prompted, and add the desired tiles from Android's Quick Settings editor.
