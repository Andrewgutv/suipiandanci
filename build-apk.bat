@echo off
echo ========================================
echo   碎片单词 - APK 打包工具
echo ========================================
echo.
echo 正在构建 APK...
echo.

call gradlew.bat assembleDebug --warning-mode none

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo   构建成功！
    echo ========================================
    echo.
    echo APK 文件位置：
    echo app\build\outputs\apk\debug\app-debug.apk
    echo.
    echo 正在打开文件夹...
    explorer app\build\outputs\apk\debug\
) else (
    echo.
    echo ========================================
    echo   构建失败！
    echo ========================================
    echo.
    pause
)
