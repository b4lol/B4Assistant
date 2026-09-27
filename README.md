# B4Assistant

[![Android build](https://github.com/b4lol/B4Assistant/actions/workflows/android.yml/badge.svg)](https://github.com/b4lol/B4Assistant/actions/workflows/android.yml)

A rooted Android Quick Settings toolkit with a Material 3 Expressive home screen. This project is a fork of [MeowAssistant](https://github.com/MeowDump/MeowAssistant), licensed under MIT.

## Features

- Quick Settings tiles for screenshots, Wi-Fi, mobile data, private DNS, Caffeine, and screen locking
- Shortcuts for [QuietKill](https://github.com/MeowDump/QuietKill) and [Integrity-Box](https://github.com/MeowDump/Integrity-Box) module scripts
- Dynamic color on Android 12 and later
- Background root commands with timeouts, bounded output, and cached root status

## Requirements

- Android 10 or later
- Root access for system actions and scripts
- Optional modules for their respective shortcuts

## Build and install

Use Android Studio with JDK 21 and Android SDK 36, or run `./gradlew assembleDebug`. The project uses Android Gradle Plugin 8.13.2, Gradle 8.13, Java 21 and Kotlin JVM 21 targets, and Compose Material 3 Expressive. Java APIs newer than those available on the minimum Android version are not used. CI builds debug and release variants; the debug APK is available as an artifact of a successful [Android build](https://github.com/b4lol/B4Assistant/actions/workflows/android.yml). Release builds are unsigned.

The package ID is `com.b4lol.assistant`, so this fork installs separately from the original app. After installation, grant root access when prompted and add the desired tiles from Android's Quick Settings editor.
