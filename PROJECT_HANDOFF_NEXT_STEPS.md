# Project Handoff Next Steps

## Immediate Priority

The first real Android-to-backend integration baseline is already complete.

The immediate priority is now release-prep cleanup, not broad feature work.

## Recommended Execution Order

1. stabilize local and release environment configuration
2. finish backend auth/error semantics cleanup
3. run short real-device validation
4. prepare a clean commit/push package

## Highest-Priority Remaining Work

### 1. Runtime Environment Cleanup

Current requirement:

- backend database credentials must be injected correctly at runtime

Keep:

- local startup through `backend/start-local.bat`
- Android debug install through `install-debug-and-launch.bat`
- smoke verification through:
  - `run-local-smoke.bat`
  - `run-local-unknown-smoke.cmd`

Goal:

- make local backend startup deterministic
- keep release API URL injection externalized

### 2. Backend Auth Semantics Cleanup

Current status:

- `/api/v1/auth` route family exists
- `JwtAuthInterceptor` exists
- unauthenticated access now returns JSON `401`
- `auth/info` is now guarded

Still needed:

- clean `UserServiceImpl` exception mapping
- remove remaining controller-local error fallbacks where possible
- ensure conflict / unauthorized / notFound semantics are consistent

### 3. Release Validation

Current status:

- debug build is validated
- release build validation is still noisy because of local Gradle wrapper/cache behavior

Goal:

- confirm release build path under a clean Gradle environment
- keep release API URL externalized through Gradle property / CI env var

### 4. Device Validation

Still needed:

- short real-device validation of:
  - notification display
  - foreground service behavior
  - push toggle on/off
  - `known / unknown` notification actions

## Validation Target

The next milestone should be:

- backend startup is deterministic with external DB credentials
- auth and error responses are consistent
- Android debug smoke remains green
- one short real-device pass is completed

## Working Rule

Until release-prep cleanup is complete:

- avoid broad Android UI rewrites
- avoid new major product features
- prioritize environment stability, auth semantics, and validation over polish
