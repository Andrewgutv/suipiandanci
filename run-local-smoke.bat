@echo off
setlocal

set "ROOT_DIR=%~dp0"
if "%APP_PORT%"=="" set "APP_PORT=8080"
cd /d "%ROOT_DIR%"

echo ========================================
echo   Fragment Words Local Smoke Runner
echo ========================================
echo.

echo [1/5] Checking backend on localhost:%APP_PORT%...
call :ensure_backend
if errorlevel 1 exit /b 1

echo.
echo [2/5] Installing and launching Android debug app...
call "%ROOT_DIR%install-debug-and-launch.bat"
if errorlevel 1 exit /b 1

echo.
echo [3/5] Resolving connected adb device...
call :resolve_device
if errorlevel 1 exit /b 1
echo Device: %TARGET_SERIAL%

echo.
echo [4/5] Reading app device_id from shared preferences...
call :read_device_id
if errorlevel 1 exit /b 1
echo App device_id: %APP_DEVICE_ID%

echo.
echo [5/5] Querying backend state for this device...
powershell -NoProfile -Command ^
  "$headers = @{ 'X-Device-Id' = '%APP_DEVICE_ID%' }; " ^
  "$vocabs = Invoke-RestMethod -Method Get -Uri 'http://localhost:%APP_PORT%/api/v1/vocabs/current' -Headers $headers; " ^
  "$notebook = Invoke-RestMethod -Method Get -Uri 'http://localhost:%APP_PORT%/api/v1/notebook/count' -Headers $headers; " ^
  "$stats = Invoke-RestMethod -Method Get -Uri 'http://localhost:%APP_PORT%/api/v1/learning/stats' -Headers $headers; " ^
  "Write-Host ('current vocab   : ' + ($vocabs.data.vocabId)); " ^
  "Write-Host ('notebook count  : ' + ($notebook.data)); " ^
  "Write-Host ('total words     : ' + ($stats.data.totalWords)); " ^
  "Write-Host ('need review     : ' + ($stats.data.needReviewWords));"
if errorlevel 1 (
    echo.
    echo [ERROR] Backend smoke query failed.
    exit /b 1
)

echo.
echo ========================================
echo   Smoke Runner Finished
echo ========================================
echo.
echo Suggested manual finish:
echo 1. Expand notification shade
echo 2. Tap "unknown"
echo 3. Re-run this script to see notebook/stats changes
echo.
exit /b 0

:ensure_backend
netstat -ano | findstr /r /c:":%APP_PORT% .*LISTENING" >nul
if not errorlevel 1 exit /b 0

if "%DB_PASSWORD%"=="" (
    echo [ERROR] Backend is not listening on localhost:%APP_PORT%.
    echo         Set DB_PASSWORD first if you want this script to auto-start backend\start-local.bat,
    echo         or start the backend manually before retrying.
    exit /b 1
)

echo Backend is not listening. Starting backend\start-local.bat for localhost:%APP_PORT% in a new window...
start "Fragment Words Backend" cmd /c ""%ROOT_DIR%backend\start-local.bat""

set /a WAIT_COUNT=0
:wait_backend
if %WAIT_COUNT% GEQ 24 (
    echo.
    echo [ERROR] Backend did not become ready within 60 seconds.
    exit /b 1
)
timeout /t 3 /nobreak >nul
netstat -ano | findstr /r /c:":%APP_PORT% .*LISTENING" >nul
if not errorlevel 1 exit /b 0
set /a WAIT_COUNT+=1
goto :wait_backend

:resolve_device
set "TARGET_SERIAL="
for /f "skip=1 tokens=1,2" %%a in ('"C:\Users\Andrew\AppData\Local\Android\Sdk\platform-tools\adb.exe" devices') do (
    if "%%b"=="device" if not defined TARGET_SERIAL set "TARGET_SERIAL=%%a"
)
if not defined TARGET_SERIAL (
    echo [ERROR] No online adb device found.
    exit /b 1
)
exit /b 0

:read_device_id
set "APP_DEVICE_ID="
for /f "usebackq delims=" %%i in (`powershell -NoProfile -Command ^
  "$raw = & 'C:\Users\Andrew\AppData\Local\Android\Sdk\platform-tools\adb.exe' -s '%TARGET_SERIAL%' shell run-as com.fragmentwords cat /data/data/com.fragmentwords/shared_prefs/fragment_words_prefs.xml; " ^
  "$xml = [xml]($raw -join [Environment]::NewLine); " ^
  "$node = $xml.map.string | Where-Object { $_.name -eq 'device_id' } | Select-Object -First 1; " ^
  "if ($node) { $node.'#text' }"`) do set "APP_DEVICE_ID=%%i"
if not defined APP_DEVICE_ID (
    echo [ERROR] Could not read app device_id.
    exit /b 1
)
exit /b 0
