@echo off
setlocal

set "ROOT_DIR=%~dp0"
powershell -NoProfile -ExecutionPolicy Bypass -File "%ROOT_DIR%run-local-unknown-smoke.ps1"
exit /b %ERRORLEVEL%

echo [1/7] Checking backend on localhost:8080...
call :ensure_backend
if errorlevel 1 exit /b 1

echo.
echo [2/7] Installing and launching Android debug app...
call "%ROOT_DIR%install-debug-and-launch.bat"
if errorlevel 1 exit /b 1

echo.
echo [3/7] Resolving connected adb device...
call :resolve_device
if errorlevel 1 exit /b 1
echo Device: %TARGET_SERIAL%

echo.
echo [4/7] Reading app device_id and current word...
call :read_device_id
if errorlevel 1 exit /b 1
call :read_current_word
if errorlevel 1 exit /b 1
echo App device_id : %APP_DEVICE_ID%
echo Current word  : %CURRENT_WORD%

echo.
echo [5/7] Reading backend baseline...
call :read_backend_state BEFORE_
if errorlevel 1 exit /b 1
echo Notebook count before : %BEFORE_NOTEBOOK_COUNT%
echo Total words before    : %BEFORE_TOTAL_WORDS%
echo Need review before    : %BEFORE_NEED_REVIEW%

echo.
echo [6/7] Expanding notifications and tapping "unknown"...
call :tap_unknown_action
if errorlevel 1 exit /b 1

echo.
echo [7/7] Reading backend state after action...
call :read_backend_state AFTER_
if errorlevel 1 exit /b 1
echo Notebook count after  : %AFTER_NOTEBOOK_COUNT%
echo Total words after     : %AFTER_TOTAL_WORDS%
echo Need review after     : %AFTER_NEED_REVIEW%

set /a NOTEBOOK_DELTA=%AFTER_NOTEBOOK_COUNT%-%BEFORE_NOTEBOOK_COUNT%
set /a TOTAL_DELTA=%AFTER_TOTAL_WORDS%-%BEFORE_TOTAL_WORDS%
set /a REVIEW_DELTA=%AFTER_NEED_REVIEW%-%BEFORE_NEED_REVIEW%

echo.
echo Delta notebook count  : %NOTEBOOK_DELTA%
echo Delta total words     : %TOTAL_DELTA%
echo Delta need review     : %REVIEW_DELTA%
echo.
echo ========================================
echo   Unknown Smoke Finished
echo ========================================
echo.
exit /b 0

:ensure_backend
netstat -ano | findstr /r /c:":8080 .*LISTENING" >nul
if not errorlevel 1 exit /b 0

echo [ERROR] Backend is not listening on localhost:8080
echo Start backend\start-local.bat first.
exit /b 1

:resolve_device
set "TARGET_SERIAL="
for /f "skip=1 tokens=1,2" %%a in ('"%ADB%" devices') do (
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
  "$raw = & '%ADB%' -s '%TARGET_SERIAL%' shell run-as com.fragmentwords cat /data/data/com.fragmentwords/shared_prefs/fragment_words_prefs.xml; " ^
  "$xml = [xml]($raw -join [Environment]::NewLine); " ^
  "$node = $xml.map.string | Where-Object { $_.name -eq 'device_id' } | Select-Object -First 1; " ^
  "if ($node) { $node.'#text' }"`) do set "APP_DEVICE_ID=%%i"
if not defined APP_DEVICE_ID (
    echo [ERROR] Could not read app device_id.
    exit /b 1
)
exit /b 0

:read_current_word
set "CURRENT_WORD="
set "CURRENT_WORD_JSON="
for /f "usebackq delims=" %%i in (`powershell -NoProfile -Command ^
  "$raw = & '%ADB%' -s '%TARGET_SERIAL%' shell run-as com.fragmentwords cat /data/data/com.fragmentwords/shared_prefs/word_prefs.xml; " ^
  "$xml = [xml]($raw -join [Environment]::NewLine); " ^
  "$node = $xml.map.string | Where-Object { $_.name -eq 'current_word' } | Select-Object -First 1; " ^
  "if ($node) { $node.'#text' }"`) do set "CURRENT_WORD_JSON=%%i"
if not defined CURRENT_WORD_JSON (
    echo [ERROR] Could not read current_word from app prefs.
    exit /b 1
)
echo %CURRENT_WORD_JSON% | findstr /c:"\"id\"" >nul
if errorlevel 1 (
    echo [ERROR] Current word is missing backend id.
    echo The app is likely using local fallback. Switch to CET4 and refresh push first.
    exit /b 1
)
for /f "usebackq delims=" %%i in (`powershell -NoProfile -Command ^
  "$json = '%CURRENT_WORD_JSON%'; " ^
  "$obj = $json | ConvertFrom-Json; " ^
  "Write-Output $obj.word"`) do set "CURRENT_WORD=%%i"
exit /b 0

:read_backend_state
set "%~1NOTEBOOK_COUNT="
set "%~1TOTAL_WORDS="
set "%~1NEED_REVIEW="
for /f "tokens=1,2,3 delims=," %%a in ('powershell -NoProfile -Command ^
  "$headers = @{ 'X-Device-Id' = '%APP_DEVICE_ID%' }; " ^
  "$notebook = (Invoke-RestMethod -Method Get -Uri 'http://localhost:8080/api/v1/notebook/count' -Headers $headers).data; " ^
  "$stats = (Invoke-RestMethod -Method Get -Uri 'http://localhost:8080/api/v1/learning/stats' -Headers $headers).data; " ^
  "Write-Output ($notebook.ToString() + ',' + $stats.totalWords + ',' + $stats.needReviewWords)"') do (
    set "%~1NOTEBOOK_COUNT=%%a"
    set "%~1TOTAL_WORDS=%%b"
    set "%~1NEED_REVIEW=%%c"
)
if not defined %~1NOTEBOOK_COUNT (
    echo [ERROR] Backend state query failed.
    exit /b 1
)
exit /b 0

:tap_unknown_action
powershell -NoProfile -Command ^
  "$adb = '%ADB%'; " ^
  "& $adb -s '%TARGET_SERIAL%' shell cmd statusbar expand-notifications | Out-Null; " ^
  "Start-Sleep -Seconds 2; " ^
  "$dump = & $adb -s '%TARGET_SERIAL%' exec-out uiautomator dump /dev/tty; " ^
  "$xmlText = ($dump | Where-Object { $_ -notlike 'UI hierchary dumped to:*' }) -join [Environment]::NewLine; " ^
  "$xml = [xml]$xmlText; " ^
  "$node = $xml.SelectSingleNode('//node[@resource-id=''android:id/action1'' or @content-desc=''不认识'' or @content-desc=''unknown'']'); " ^
  "if (-not $node) { Write-Error 'unknown action not found'; exit 1 }; " ^
  "$bounds = $node.GetAttribute('bounds'); " ^
  "if ($bounds -notmatch '\[(\d+),(\d+)\]\[(\d+),(\d+)\]') { Write-Error 'bounds parse failed'; exit 1 }; " ^
  "$x = [int](($matches[1] + $matches[3]) / 2); " ^
  "$y = [int](($matches[2] + $matches[4]) / 2); " ^
  "& $adb -s '%TARGET_SERIAL%' shell input tap $x $y | Out-Null; " ^
  "Write-Host ('Tapped unknown action at ' + $x + ',' + $y); " ^
  "Start-Sleep -Seconds 4"
if errorlevel 1 (
    echo [ERROR] Failed to tap notification unknown action.
    exit /b 1
)
exit /b 0
