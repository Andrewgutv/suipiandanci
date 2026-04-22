# Fragment Words Current Status

This file reflects the repository state after the Android-to-backend integration baseline was completed on April 19, 2026.

## Overview

The repository now has two active product tracks:

- `app/`: the native Android client mainline
- `backend/`: the Spring Boot API mainline

The old `fragment-words/` uni-app prototype remains historical and is not the product mainline.

## Current Project Phase

The project is no longer in the "local Android prototype" stage.

It is now in a release-prep integration stage:

- Android local learning loop is beta-usable
- backend core API routes are live and callable
- one real Android-to-backend learning path has been verified end-to-end
- local development smoke scripts now exist for repeated validation

## Android Mainline Status

The Android app is currently beta-usable and supports both:

- local fallback behavior
- backend-first integration behavior on the core learning path

Working Android capabilities:

- local word library loading from `app/src/main/assets/data/`
- local SQLite notebook and learning progress fallback
- notification-based word cards
- `known / unknown` actions
- notebook display inside the app
- local Ebbinghaus-style review scheduling
- multi-library switching
- push on/off runtime cleanup
- backend-first next-word fetch
- backend-first notebook count/list read
- backend feedback sync for `known / unknown`
- backend current-vocab sync

Latest debug APK path:

- `app/build/outputs/apk/debug/app-debug.apk`

## Backend Mainline Status

The backend is no longer only under route standardization. The core integration slice is now running.

Active route families:

- `/api/v1/auth`
- `/api/v1/vocabs`
- `/api/v1/notebook`
- `/api/v1/learning`

Verified backend behaviors:

- `GET /api/v1/vocabs`
- `GET /api/v1/vocabs/current`
- `PUT /api/v1/vocabs/current`
- `GET /api/v1/notebook/count`
- `GET /api/v1/notebook`
- `POST /api/v1/notebook`
- `POST /api/v1/learning/next`
- `POST /api/v1/learning/feedback`
- `GET /api/v1/learning/stats`

Recent backend cleanup also improved response behavior:

- UTF-8 JSON response headers are explicit
- unauthorized requests now return JSON `Result`
- forbidden requests now return JSON `Result`
- internal server errors now return JSON `Result`
- auth info endpoint is now guarded by `@RequireAuth`
- auth conflict / unauthorized / forbidden cases now use explicit domain exceptions

Backend compile status:

```powershell
cmd /c "C:\apache-maven-3.9.9\bin\mvn.cmd" -q -DskipTests compile
```

## What Was Verified

The following are verified in the current repository state:

- Android `:app:compileDebugKotlin` passes
- Android `:app:assembleDebug` passes
- backend `mvn -q test` passes
- backend `mvn -q -DskipTests compile` passes
- Android emulator smoke path can install and launch through local scripts
- notification `unknown` action automation script can reach backend and verify notebook/stats deltas
- unauthenticated `/api/v1/auth/info/{userId}` returns JSON `401`
- authenticated cross-user `/api/v1/auth/info/{userId}` returns JSON `403`

## Remaining High-Priority Risk

These are the meaningful open risks now:

- full real-device validation of Android notification and foreground-service behavior is still incomplete
- backend startup currently depends on correct local MySQL credentials being injected at runtime
- release build validation is still blocked by local Gradle wrapper/cache environment issues
- cloud sync and multi-device account behavior remain unfinished

## Recommended Next Step

Do not do another broad rewrite.

Recommended order from here:

1. finish release-prep environment cleanup
   - stable local DB startup path
   - stable release API URL injection
   - repeatable release build validation
2. run a short real-device validation pass
3. extend the same response semantics if more guarded backend routes are added
4. then trim remaining local-only fallback assumptions where backend is now primary

## Practical Summary

If someone asks where the project stands right now, the accurate answer is:

- Android mainline: beta-usable
- backend mainline: core API slice is live
- integration status: core Android-to-backend learning path is already connected and verified
- overall project: in release-prep integration cleanup, not prototype discovery
