# Local Integration Quickstart

This project now supports a real Android-to-backend smoke path.

## Current baseline

- Android client: `app/`
- Spring Boot backend: `backend/`
- Legacy uni-app prototype: `fragment-words/` (not mainline)

The Android app can now:

- fetch the next word from backend first, with local fallback
- sync `认识 / 不认识` feedback to backend
- read notebook count/list from backend first, with local fallback
- sync current vocab selection to backend

## Prerequisites

- Android SDK installed and `sdk.dir` configured in `local.properties`
- JDK 17
- MySQL running on `localhost:3307`
- backend database initialized from `backend/src/main/resources/sql/init.sql`

## API base URL configuration

Android now reads the API base URL from `BuildConfig`.

`debug` build priority:

1. `local.properties` -> `debugApiBaseUrl`
2. fallback -> `http://10.0.2.2:8080/`

`release` build priority:

1. Gradle property `releaseApiBaseUrl`
2. environment variable `RELEASE_API_BASE_URL`
3. fallback placeholder -> `https://api.fragmentwords.example/`

Example `local.properties` override for debug:

```properties
debugApiBaseUrl=http://10.0.2.2:8080/
```

## Start backend

Use the local runner:

```powershell
cd D:\workspace\app\backend
$env:DB_PASSWORD = "your_real_password"
.\start-local.ps1
```

Compatible legacy entry:

```powershell
cd D:\workspace\app\backend
$env:DB_PASSWORD = "your_real_password"
.\start-local.bat
```

This uses a project-local Maven cache under `backend\.m2repo`.

The local runner now fails fast before Spring Boot starts if any of these are true:

- `DB_PASSWORD` is missing
- `java` is not available on `PATH`
- `backend\mvnw.cmd` is missing
- MySQL is not reachable at `DB_HOST:DB_PORT`

If you need to bypass only the TCP port preflight, set:

```powershell
$env:SKIP_DB_PREFLIGHT = "1"
.\start-local.ps1
```

```cmd
set SKIP_DB_PREFLIGHT=1 && start-local.bat
```

## Build Android debug APK

```powershell
cd D:\workspace\app
.\gradlew.bat :app:assembleDebug
```

Or install and launch the current debug APK in one step:

```powershell
cd D:\workspace\app
.\install-debug-and-launch.bat
```

Or run the local smoke helper:

```powershell
cd D:\workspace\app
.\run-local-smoke.bat
```

This script will:

1. verify backend port `8080`
2. install and launch the current debug APK
3. read the app `device_id` from shared preferences
4. query backend vocab/notebook/stats for that device

To automate the notification `unknown` action:

```powershell
cd D:\workspace\app
.\run-local-unknown-smoke.cmd
```

This script will:

1. install and launch the app
2. resolve the app `device_id`
3. expand the notification shade
4. tap the notification `unknown` action
5. query backend notebook/stats before and after

If the current word is already in notebook, notebook delta can remain `0`.

If you want the script to force a rebuild first:

```powershell
cd D:\workspace\app
set FORCE_BUILD=1
.\install-debug-and-launch.bat
```

APK output:

```text
app\build\outputs\apk\debug\app-debug.apk
```

## Emulator smoke flow

Recommended AVD base URL:

- emulator -> `http://10.0.2.2:8080/`

Smoke steps:

1. Start backend on `localhost:8080`
2. Launch emulator
3. Install debug APK
4. Open app
5. Grant notification permission
6. In Settings, select `CET4`
7. Enable push
8. Expand notification shade
9. Tap `不认识`
10. Verify backend notebook and learning stats update

## Verified backend endpoints

- `GET /api/v1/vocabs`
- `GET /api/v1/vocabs/current`
- `PUT /api/v1/vocabs/current`
- `POST /api/v1/learning/next`
- `POST /api/v1/learning/feedback`
- `GET /api/v1/learning/stats`
- `GET /api/v1/notebook/count`
- `GET /api/v1/notebook`
- `POST /api/v1/notebook`

## Notes

- Debug build only: cleartext HTTP to `10.0.2.2` is allowed.
- Main manifest no longer keeps development-only cleartext settings.
- Release URL should be injected via Gradle property or CI environment variable, not committed into local debug files.
