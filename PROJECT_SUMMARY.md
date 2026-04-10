# Project Summary

## Project

Fragment Words is a native Android vocabulary-learning app that uses notification and lock-screen style word cards to turn small moments of phone usage into lightweight study time.

The current repository has three parallel lines of work:

- pp/: the active native Android mainline
- ackend/: a Spring Boot backend that is still only partially integrated
- ragment-words/: an older uni-app prototype and no longer the main run path

The only path that should currently be treated as the usable mainline is pp/.

## Current Product State

The Android app is now in a beta-usable state for the local offline learning loop.

The mainline can currently do the following:

- load local word libraries from assets
- display word notifications
- handle known / unknown notification actions
- write unknown words into a local notebook
- read and display notebook entries inside the app
- track local learning progress with Ebbinghaus-style review timing
- show push state, selected library, and notebook count on the home screen

## What Was Completed In This Cleanup Pass

### Mainline stabilization

- notification action handling was cleaned up and made more deterministic
- unknown -> notebook was verified as a working path
- notebook display was refreshed and stabilized
- duplicated settings entry points were reduced to a single fragment-based path
- several obviously unused Android leftovers were removed from the mainline

### Runtime behavior improvements

- startup notification update spam was reduced
- exact alarm scheduling now falls back when exact alarm permission is unavailable
- boot restore behavior was reduced to schedule recovery instead of directly restoring the whole foreground-service path

### Documentation cleanup

- README.md was rewritten into a clean project-facing document
- CURRENT_STATUS.md now reflects the real current mainline
- SESSION_LOG.md was updated with recent work context
- ANDROID_VALIDATION_CHECKLIST.md was added for short real-device validation

## Important Files

Key Android mainline files:

- pp/src/main/java/com/fragmentwords/HomeFragment.kt
- pp/src/main/java/com/fragmentwords/MainActivity.kt
- pp/src/main/java/com/fragmentwords/NotebookFragment.kt
- pp/src/main/java/com/fragmentwords/service/WordService.kt
- pp/src/main/java/com/fragmentwords/receiver/WordActionReceiver.kt
- pp/src/main/java/com/fragmentwords/receiver/ScreenUnlockReceiver.kt
- pp/src/main/java/com/fragmentwords/utils/AlarmScheduler.kt
- pp/src/main/java/com/fragmentwords/manager/LearningManager.kt
- pp/src/main/java/com/fragmentwords/data/WordRepository.kt
- pp/src/main/java/com/fragmentwords/database/WordDatabase.kt

Supporting status documents:

- CURRENT_STATUS.md
- SESSION_LOG.md
- ANDROID_VALIDATION_CHECKLIST.md

## What Was Verified

The following were verified during local emulator validation:

- the app compiles successfully
- the app installs successfully on the configured emulator
- the home page renders and displays push state and notebook count
- notification cards are generated
- the notebook contains real words such as rticle and last
- the notebook screen can display stored data

## Remaining Known Risk

One runtime issue still remains partially open:

- some Android 14/15 emulator runs still emit a foreground-service-type warning when WordService starts

Current judgment:

- this warning is not currently blocking the main local user loop
- it behaves more like a compatibility tail than a core feature failure
- it should still be checked on a real device before calling the app fully stabilized

## Removed Or Consolidated Redundancy

The following obvious mainline leftovers were removed or consolidated:

- NotebookActivity.kt
- ApiLearningManager.kt
- ApiRepository.kt
- WordLibrary.kt
- SettingsActivity.kt
- ctivity_settings.xml

Settings now live in SettingsFragment.kt.

## Recommended Next Step

Do not do another broad code rewrite immediately.

The highest-value next step is a short real-device validation pass.

Recommended order:

1. enable push from the home screen
2. wait for a word notification
3. tap 不认识
4. confirm the word appears in the notebook
5. tap 认识
6. lock and unlock once
7. confirm refresh happens at most once
8. turn push off and confirm notifications stop

Use ANDROID_VALIDATION_CHECKLIST.md for that pass.

## Final Assessment

The app is no longer in rough prototype shape.

It should currently be described as:

- a beta-usable Android local mainline
- suitable for continued development, demo use, and short real-device validation
- not yet a fully closed production-ready release
