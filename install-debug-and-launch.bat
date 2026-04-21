@echo off
setlocal

set "ROOT_DIR=%~dp0"
cd /d "%ROOT_DIR%"
set "GRADLE_USER_HOME=%ROOT_DIR%.gradle-user-home"
set "APK_PATH=app\build\outputs\apk\debug\app-debug.apk"

echo ========================================
echo   Fragment Words Debug Installer
echo ========================================
echo.

if not exist "local.properties" (
    echo [ERROR] local.properties not found.
    echo Configure sdk.dir before running this script.
    exit /b 1
)

for /f "usebackq delims=" %%i in (`powershell -NoProfile -Command "$line = Get-Content 'local.properties' | Where-Object { $_ -like 'sdk.dir=*' } | Select-Object -First 1; if (-not $line) { exit 1 }; $sdk = $line.Substring($line.IndexOf('=') + 1); $sdk = $sdk.Replace('\:', ':').Replace('\\', '\'); Write-Output $sdk"`) do set "SDK_DIR=%%i"

if not defined SDK_DIR (
    echo [ERROR] sdk.dir not found in local.properties.
    exit /b 1
)

set "ADB=%SDK_DIR%\platform-tools\adb.exe"

if not exist "%ADB%" (
    echo [ERROR] adb not found:
    echo         %ADB%
    exit /b 1
)

echo [1/4] Preparing debug APK...
if /i "%FORCE_BUILD%"=="1" goto BUILD_APK
if exist "%APK_PATH%" goto APK_READY

:BUILD_APK
echo Building debug APK...
call gradlew.bat :app:assembleDebug
if errorlevel 1 (
    echo.
    echo [ERROR] Debug APK build failed.
    exit /b 1
)

:APK_READY
if not exist "%APK_PATH%" (
    echo.
    echo [ERROR] Debug APK not found:
    echo         %APK_PATH%
    exit /b 1
)

echo.
echo [2/4] Checking connected devices...
"%ADB%" start-server >nul
"%ADB%" devices

set "TARGET_SERIAL="
for /f "skip=1 tokens=1,2" %%a in ('"%ADB%" devices') do (
    if "%%b"=="device" if not defined TARGET_SERIAL set "TARGET_SERIAL=%%a"
)

if not defined TARGET_SERIAL (
    echo.
    echo [ERROR] No online adb device found.
    echo Start an emulator or connect a device, then run this script again.
    exit /b 1
)

echo.
echo [3/4] Installing APK to %TARGET_SERIAL%...
"%ADB%" -s "%TARGET_SERIAL%" install -r "%APK_PATH%"
if errorlevel 1 (
    echo.
    echo [ERROR] APK install failed.
    exit /b 1
)

echo.
echo [4/4] Granting notification permission and launching app...
"%ADB%" -s "%TARGET_SERIAL%" shell pm grant com.fragmentwords android.permission.POST_NOTIFICATIONS >nul 2>&1
"%ADB%" -s "%TARGET_SERIAL%" shell am start -n com.fragmentwords/.MainActivity

echo.
echo ========================================
echo   Done
echo ========================================
echo Device: %TARGET_SERIAL%
echo APK:    %APK_PATH%
echo.

endlocal
