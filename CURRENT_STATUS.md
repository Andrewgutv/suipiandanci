# Fragment Words Current Status

This file reflects the current usable state of the repository after the latest Android cleanup and emulator validation pass.

## Overview

This repository contains three parallel lines of work:

- `app/`: the active native Android app written in Kotlin
- `backend/`: a Spring Boot backend that is still only partially integrated into the Android flow
- `fragment-words/`: an older uni-app prototype and no longer the main run path

The only path that should currently be treated as the runnable mainline is the native Android app in `app/`.

## Current Mainline Status

The Android app is now in a beta-usable state for the local offline flow.

The following core loop is working on the current mainline:

- local word library loading from `app/src/main/assets/data/`
- local SQLite storage for words, notebook entries, and learning progress
- notification-based word cards
- known / unknown feedback actions
- notebook persistence and reading
- local Ebbinghaus-style review scheduling
- basic multi-library framework

Important code paths:

- `app/src/main/java/com/fragmentwords/service/WordService.kt`
- `app/src/main/java/com/fragmentwords/receiver/WordActionReceiver.kt`
- `app/src/main/java/com/fragmentwords/receiver/ScreenUnlockReceiver.kt`
- `app/src/main/java/com/fragmentwords/HomeFragment.kt`
- `app/src/main/java/com/fragmentwords/NotebookFragment.kt`
- `app/src/main/java/com/fragmentwords/manager/LearningManager.kt`
- `app/src/main/java/com/fragmentwords/data/WordRepository.kt`
- `app/src/main/java/com/fragmentwords/database/WordDatabase.kt`

## What Was Verified In This Pass

### Build and install

- `:app:compileDebugKotlin` passed
- `:app:installDebug` passed
- the app launched successfully on the configured emulator

### Functional checks confirmed

- the home screen renders and shows push state, selected library, and notebook count
- the notification card is generated and includes known / unknown actions
- tapping `unknown` reaches the receiver path and stores notebook data locally
- notebook data can be read back by `NotebookFragment`
- the notebook screen can display real stored words
- duplicate notification update spam during startup was reduced to a single effective update

### Data points observed during emulator validation

- notebook data included words such as `article` and `blast`
- the home screen showed `生词本：2 个单词`
- `NotebookFragment` logged successful loading and display of notebook words

## What Is Improved Compared To The Earlier State

The following parts were actively tightened during the latest cleanup pass:

- notification action handling was rewritten to avoid blocking receiver execution
- word data passed through notification actions is now more complete
- notebook screen refresh logic was tightened and made more deterministic
- home screen push state handling and service scheduling logic were unified
- unlock refresh timing logic was simplified and rate-limited
- startup duplication in `WordService` was reduced
- several mainline Kotlin files and visible strings were cleaned up from corrupted / noisy state

## Remaining Known Risk

There is still one notable runtime warning in the emulator environment:

- Android logs may still show a foreground-service type warning when `WordService` starts

Current judgment:

- this warning is not currently blocking the core local product loop
- it appears to be an Android 14/15 emulator compatibility tail rather than a full functional failure
- it should still be treated as unfinished engineering work before calling the app fully stabilized

## What Is Not Fully Closed Yet

These areas should still be considered incomplete:

- backend-driven learning flow
- login / registration end-to-end product flow
- cloud sync across devices
- online vocabulary download as the default path
- full real-device validation of notification and foreground-service behavior
- complete cleanup of all historical repo noise outside the active Android mainline

## Recommended Next Step

The most practical next step is not another large code rewrite.

Recommended order:

1. run a short real-device validation pass for the current Android app
2. verify the `known` path end to end: action -> progress update -> unlock refresh once
3. verify the `unknown` path end to end: action -> notebook write -> notebook page visible
4. only then decide whether the remaining foreground-service warning needs another compatibility pass

## Recommended Run Path

### Path A: Native Android local mode

This is the recommended way to run the project.

Prerequisites:

- Android Studio
- Android SDK 34+
- JDK 17 or Android Studio bundled runtime
- a valid `local.properties`

Steps:

1. Open `D:\workspace\app` in Android Studio.
2. Confirm `local.properties` points `sdk.dir` to your Android SDK.
3. Let Gradle sync finish.
4. Run the `app` module on an emulator or Android device.
5. Grant notification permission on first launch.
6. Turn on push from the home screen and test the notification / notebook flow.

### Path B: Backend inspection only

You can still run `backend/` independently for development or inspection, but it is not yet the stable mainline for the Android app.

## Practical Summary

If someone asks whether the current Android app is usable, the honest answer is:

- yes for the local notification + notebook learning loop
- not yet fully closed as a polished production-ready app
- backend integration and full device compatibility remain unfinished

