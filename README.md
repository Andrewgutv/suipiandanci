# Fragment Words

Fragment Words is an Android vocabulary-learning app designed around short study sessions from the notification tray and lock-screen flow. The active product line in this repository is the native Android client in `app/` plus the Spring Boot backend in `backend/`. The older `fragment-words/` uni-app prototype is kept only as legacy reference.

## Overview

- Product type: Android learning app with optional backend sync
- Client state: `beta-usable`
- Backend state: local integration path available
- Primary learning loop: show word, collect `known / unknown` feedback, update notebook and review progress

## Implemented Capabilities

- Local vocabulary loading from bundled datasets
- Notification-based word cards
- `known / unknown` feedback actions
- Local Ebbinghaus-style review scheduling
- Local notebook persistence and notebook page display
- Home page summary for selected vocabulary and notebook count
- Backend-first next-word fetch, feedback sync, and notebook sync

## Known Limitation

- Android 14/15 emulators may still show a foreground-service type warning

This warning does not block the main flow, but the project should still be treated as a beta build rather than a finished release.

## Tech Stack

- Android client: Kotlin, AndroidX, WorkManager, Retrofit, OkHttp, SQLite
- Backend: Java 17, Spring Boot, MyBatis-Plus, MySQL
- Tooling: Gradle, Android Studio, Maven wrapper scripts

## Repository Layout

```text
app/                     Native Android app
backend/                 Spring Boot backend for sync and notebook APIs
fragment-words/          Legacy uni-app prototype
CURRENT_STATUS.md        Current project status snapshot
LOCAL_RUNBOOK.md         Android + backend integration steps
ANDROID_VALIDATION_CHECKLIST.md
```

## Key Entry Points

- `app/src/main/java/com/fragmentwords/service/WordService.kt`
- `app/src/main/java/com/fragmentwords/receiver/WordActionReceiver.kt`
- `app/src/main/java/com/fragmentwords/receiver/ScreenUnlockReceiver.kt`
- `app/src/main/java/com/fragmentwords/manager/LearningManager.kt`
- `app/src/main/java/com/fragmentwords/data/WordRepository.kt`
- `backend/src/main/java/com/fragmentwords/controller/LearningController.java`

## Quick Start

### Run the Android client

Prerequisites:

- Android Studio
- Android SDK 34+
- JDK 17 or the runtime bundled with Android Studio
- A valid `local.properties`

Steps:

1. Open `D:\workspace\app` in Android Studio.
2. Confirm `local.properties` points `sdk.dir` to the installed Android SDK.
3. Wait for Gradle sync to finish.
4. Run the `app` module on an emulator or device.
5. Grant notification permission on first launch.
6. Enable push on the home page and verify the notebook flow.

### Run Android + backend integration

See [LOCAL_RUNBOOK.md](./LOCAL_RUNBOOK.md) for the full setup. Typical backend startup:

```powershell
cd D:\workspace\app\backend
$env:DB_PASSWORD = "your_real_password"
.\start-local.ps1
```

If port `8080` is occupied:

```powershell
$env:APP_PORT = "8081"
.\start-local.ps1
```

### Command-line build

```powershell
.\gradlew.bat :app:compileDebugKotlin
.\gradlew.bat :app:installDebug
```

## Recommended Validation Path

1. Enable word push from the home page.
2. Wait for a notification card.
3. Tap `unknown`.
4. Open the notebook page and verify the new entry.
5. Tap `known`.
6. Lock and unlock the device once and confirm a single refresh.
7. Disable push and confirm no new cards appear.

## Related Documents

- [ANDROID_VALIDATION_CHECKLIST.md](./ANDROID_VALIDATION_CHECKLIST.md)
- [CURRENT_STATUS.md](./CURRENT_STATUS.md)
- [LOCAL_RUNBOOK.md](./LOCAL_RUNBOOK.md)
- [PROJECT_SUMMARY.md](./PROJECT_SUMMARY.md)

## Status Summary

The current repository can be used as the main Android plus backend line for development and demos. The local closed loop works, one real backend sync path is already connected, and the remaining work is mainly around production hardening and Android foreground-service cleanup.
