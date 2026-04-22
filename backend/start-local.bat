@echo off
setlocal

set "SCRIPT_DIR=%~dp0"
cd /d "%SCRIPT_DIR%"

echo ========================================
echo   Fragment Words Backend Local Runner
echo ========================================
echo.
echo This script uses a project-local Maven cache:
echo   %SCRIPT_DIR%.m2repo
echo.

if "%DB_HOST%"=="" set "DB_HOST=127.0.0.1"
if "%DB_PORT%"=="" set "DB_PORT=3307"
if "%DB_NAME%"=="" set "DB_NAME=fragment_words"
if "%DB_USER%"=="" set "DB_USER=root"
if "%APP_PORT%"=="" set "APP_PORT=8080"
if "%SKIP_DB_PREFLIGHT%"=="" set "SKIP_DB_PREFLIGHT=0"

set "SPRING_DATASOURCE_URL=jdbc:mysql://%DB_HOST%:%DB_PORT%/%DB_NAME%?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai"
set "SPRING_DATASOURCE_USERNAME=%DB_USER%"
set "SPRING_DATASOURCE_PASSWORD=%DB_PASSWORD%"
set "SERVER_PORT=%APP_PORT%"

echo Expected local services:
echo   MySQL host: %DB_HOST%
echo   MySQL port: %DB_PORT%
echo   MySQL name: %DB_NAME%
echo   MySQL user: %DB_USER%
echo   App port:   %APP_PORT%
echo.

if "%DB_PASSWORD%"=="" (
    echo [ERROR] DB_PASSWORD is not set.
    echo         PowerShell example:
    echo           $env:DB_PASSWORD="your_password"
    echo           .\start-local.bat
    echo         cmd.exe example:
    echo           set DB_PASSWORD=your_password ^&^& start-local.bat
    exit /b 1
)

java -version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Java is not available on PATH. Install JDK 17+ and retry.
    exit /b 1
)

if not exist "%SCRIPT_DIR%mvnw.cmd" (
    echo [ERROR] mvnw.cmd was not found in %SCRIPT_DIR%
    exit /b 1
)

if not "%SKIP_DB_PREFLIGHT%"=="1" (
    powershell -NoProfile -Command ^
      "$client = New-Object System.Net.Sockets.TcpClient; try { $async = $client.BeginConnect('%DB_HOST%', %DB_PORT%, $null, $null); if (-not $async.AsyncWaitHandle.WaitOne(2000, $false)) { exit 1 }; $client.EndConnect($async); exit 0 } catch { exit 1 } finally { $client.Dispose() }"
    if errorlevel 1 (
        echo [ERROR] Cannot reach MySQL at %DB_HOST%:%DB_PORT%.
        echo         Start MySQL first, or set SKIP_DB_PREFLIGHT=1 to bypass the TCP check.
        exit /b 1
    )
)

powershell -NoProfile -Command ^
  "$client = New-Object System.Net.Sockets.TcpClient; try { $async = $client.BeginConnect('127.0.0.1', %APP_PORT%, $null, $null); if (-not $async.AsyncWaitHandle.WaitOne(2000, $false)) { exit 1 }; $client.EndConnect($async); exit 0 } catch { exit 1 } finally { $client.Dispose() }"
if not errorlevel 1 (
    echo [ERROR] Application port %APP_PORT% is already in use on localhost.
    echo         Stop the existing process, or set APP_PORT to a free port before starting the backend.
    exit /b 1
)

call mvnw.cmd ^
  -Dmaven.repo.local="%SCRIPT_DIR%.m2repo" ^
  org.springframework.boot:spring-boot-maven-plugin:3.3.4:run

if errorlevel 1 exit /b %errorlevel%

endlocal
