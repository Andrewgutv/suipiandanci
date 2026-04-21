# Project Context

This document captures the current handoff state of the repository after the Android beta cleanup pass and the first backend P0 standardization pass.

## Current State

This repository now has two meaningful active tracks:

- `app/`: native Android client, beta-usable for the local notification + notebook learning loop
- `backend/`: Spring Boot backend that has now entered active P0 standardization work for future client-server integration

The old `fragment-words/` uni-app prototype remains historical and is not the mainline.

## Android Track

The Android app is already in a beta-usable state.

What is currently working:

- local word library loading from assets
- notification-based word cards
- known / unknown feedback actions
- notebook persistence and display
- local Ebbinghaus-style review scheduling
- push toggle lifecycle cleanup
- library switching affecting subsequent notifications

Recent Android stabilization work also closed several runtime issues:

- turning push off now clears runtime notification state consistently
- stale alarm / worker refreshes are short-circuited when push is disabled
- old notification actions no longer refresh another word after push has been disabled
- `WordService` start behavior now has extra runtime protection around foreground-service startup

Latest debug APK path:

- `app/build/outputs/apk/debug/app-debug.apk`

Recommended Android next step:

- run a short real-device validation pass before doing more Android feature work

## Backend Track

The backend was previously a partial skeleton. It has now started moving toward a standard client-server API shape.

### What was standardized in this pass

`vocab` route:

- route family moved to `/api/v1/vocabs`
- current vocab read/write now uses `X-Device-Id`
- new DTO: `VocabSelectionResponseDTO`
- new persistence entity: `DevicePreference`
- new mapper: `DevicePreferenceMapper`
- `VocabServiceImpl` now reads/writes current vocab from `device_preference`

`notebook` route:

- route family moved to `/api/v1/notebook`
- `deviceId` query parameter replaced with `X-Device-Id`

`learning` route:

- route family moved to `/api/v1/learning`
- header naming normalized to `X-Device-Id`
- legacy `X-User-ID` flow removed from controller entrypoints
- `Authorization: Bearer <token>` is now accepted and resolved into `userId` in `LearningController`

### Backend compile status

The backend compiles successfully with:

```powershell
cmd /c "C:\apache-maven-3.9.9\bin\mvn.cmd" -q -DskipTests compile
```

## What Is Still Not Fully Closed

These areas remain unfinished and should be treated as the next engineering priority:

- `auth` route family is not yet standardized to `/api/v1/auth`
- JWT validation still happens only at controller/helper level rather than a true auth filter/interceptor
- notebook responses still directly expose `Page<Word>` instead of a stable response DTO
- `Result` error codes are still minimal and not yet normalized for production-style frontend handling
- some legacy backend code still contains corrupted comments / text noise and should be cleaned up later
- Android client still primarily runs on local SQLite logic and has not yet switched to backend-driven data flow

## Recommended Next Step

The highest-value next step is not another Android rewrite.

Recommended order:

1. standardize `auth` to `/api/v1/auth`
2. add proper JWT auth middleware/filtering
3. normalize notebook response DTOs
4. start Android-to-backend integration against:
   - `/api/v1/vocabs`
   - `/api/v1/vocabs/current`
   - `/api/v1/notebook`
   - `/api/v1/learning/next`
   - `/api/v1/learning/feedback`

## Practical Summary

If someone asks where the project stands right now, the accurate answer is:

- Android mainline: beta-usable and ready for real-device validation
- backend mainline: no longer just skeleton code, now entering real P0 API standardization
- overall project: moving from a single-device prototype toward a proper frontend-backend software project, but not yet fully integrated
