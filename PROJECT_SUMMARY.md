# Project Summary

## Project

Fragment Words is a vocabulary-learning project built around lightweight word exposure through notifications, notebook review, and spaced repetition.

The repository currently has three tracks:

- `app/`: the active native Android client mainline
- `backend/`: the active Spring Boot backend mainline
- `fragment-words/`: an old uni-app prototype and no longer the product path

The real product direction is now:

- a stable Android beta client
- a callable backend API slice
- a verified Android-to-backend learning loop

## Current Product State

### Android

The Android app is beta-usable and supports both local fallback and backend-first behavior on the current main slice.

It can currently:

- load local word libraries from assets
- display word notifications
- handle known / unknown actions
- write unknown words into a local notebook
- display notebook entries inside the app
- track local review progress with Ebbinghaus-style timing
- switch libraries and reflect the change in subsequent notifications
- cleanly stop runtime refresh behavior when push is disabled
- fetch next word from backend first
- sync feedback to backend
- fetch notebook count/list from backend first
- sync current vocab selection to backend

### Backend

The backend is no longer only under route cleanup. The core API slice is running and verified.

Active route families:

- `/api/v1/auth`
- `/api/v1/vocabs`
- `/api/v1/notebook`
- `/api/v1/learning`

Recent backend improvements:

- UTF-8 response headers are explicit
- unauthorized responses are returned as JSON `Result`
- internal server errors are returned as JSON `Result`
- current auth info endpoint requires authentication

## What Was Completed In This Pass

### Android integration and tooling

- backend-first next-word fetch was connected
- backend-first notebook reads were connected
- feedback sync to backend was connected
- local fallback behavior was retained
- debug-only network security for emulator HTTP was isolated into the debug source set
- API base URL handling was moved into build-type-aware configuration
- local helper scripts were added for:
  - backend startup
  - debug install and launch
  - local smoke verification
  - automated notification `unknown` smoke

### Backend response cleanup

- `JwtAuthInterceptor` now returns JSON `401` responses
- global exception handling now returns JSON `Result`
- database-related internal failures no longer fall back to Spring default error pages

## Important Files

Android mainline files:

- `app/src/main/java/com/fragmentwords/data/WordRepository.kt`
- `app/src/main/java/com/fragmentwords/service/WordService.kt`
- `app/src/main/java/com/fragmentwords/receiver/WordActionReceiver.kt`
- `app/src/main/java/com/fragmentwords/HomeFragment.kt`
- `app/src/main/java/com/fragmentwords/NotebookFragment.kt`
- `app/src/main/java/com/fragmentwords/SettingsFragment.kt`
- `app/src/main/java/com/fragmentwords/network/ApiService.kt`
- `app/src/main/java/com/fragmentwords/network/ResolvedApiConfig.kt`

Android local tooling files:

- `install-debug-and-launch.bat`
- `run-local-smoke.bat`
- `run-local-unknown-smoke.cmd`
- `run-local-unknown-smoke.ps1`

Backend files:

- `backend/src/main/java/com/fragmentwords/common/Result.java`
- `backend/src/main/java/com/fragmentwords/config/JwtAuthInterceptor.java`
- `backend/src/main/java/com/fragmentwords/config/GlobalExceptionHandler.java`
- `backend/src/main/java/com/fragmentwords/controller/VocabController.java`
- `backend/src/main/java/com/fragmentwords/controller/UnknownWordController.java`
- `backend/src/main/java/com/fragmentwords/controller/LearningController.java`
- `backend/src/main/java/com/fragmentwords/controller/UserController.java`
- `backend/src/main/resources/application.yml`
- `backend/start-local.bat`

Context / handoff files:

- `CURRENT_STATUS.md`
- `PROJECT_CONTEXT.md`
- `PROJECT_HANDOFF_NEXT_STEPS.md`
- `SESSION_LOG.md`
- `LOCAL_INTEGRATION_QUICKSTART.md`

## What Was Verified

The following have been verified in the current repository state:

- Android `:app:compileDebugKotlin` passes
- Android `:app:assembleDebug` passes
- backend `mvn -q -DskipTests compile` passes
- local emulator integration scripts can install and launch the app
- notification action smoke automation can exercise the `unknown` action path
- `/api/v1/auth/info/{userId}` returns JSON `401`
- `/api/v1/vocabs`
- `/api/v1/notebook/count`
- `/api/v1/learning/next`
- `/api/v1/learning/stats`

## Remaining Known Risk

Current meaningful open risks are:

- local backend startup still depends on correct external DB credentials being provided at runtime
- `UserServiceImpl` exception semantics still need deeper cleanup
- full real-device Android validation is still incomplete
- release build validation is still affected by local Gradle wrapper/cache environment issues
- multi-device cloud sync remains unfinished

## Recommended Next Step

Do not do another broad rewrite.

The highest-value next step is:

1. finish local/release environment cleanup
2. complete backend auth semantics cleanup
3. run a short real-device validation pass
4. prepare a clean commit and push

## Final Assessment

The project is no longer just an Android prototype.

It should currently be described as:

- a beta-usable Android client mainline
- a backend service mainline with a working core API slice
- a software project in release-prep integration cleanup
