@echo off
echo ========================================
echo   锁屏背单词 - 快速构建脚本
echo   包含最新Bug修复
echo ========================================
echo.
echo 正在清理旧构建...
echo.

call gradlew.bat clean

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo 清理失败，但继续构建...
    echo.
)

echo.
echo ========================================
echo   正在构建APK（包含Bug修复）...
echo ========================================
echo.

call gradlew.bat assembleDebug --warning-mode none --stacktrace

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo   ✅ 构建成功！
    echo ========================================
    echo.
    echo APK文件位置：
    echo app\build\outputs\apk\debug\app-debug.apk
    echo.

    REM 显示APK信息
    for %%f in (app\build\outputs\apk\debug\app-debug.apk) do (
        echo 文件大小：%%~zf bytes
        echo 修改时间：%%~tf
    )

    echo.
    echo ========================================
    echo   正在打开APK文件夹...
    echo ========================================
    explorer app\build\outputs\apk\debug\

    echo.
    echo ========================================
    echo   📱 下一步操作：
    echo   1. 将APK传输到手机
    echo   2. 卸载旧版本（重要！）
    echo   3. 安装新版本
    echo   4. 测试生词本功能
    echo ========================================
    echo.

    pause
) else (
    echo.
    echo ========================================
    echo   ❌ 构建失败！
    echo ========================================
    echo.
    echo 可能的原因：
    echo 1. JDK未安装或配置错误
    echo 2. 网络连接问题
    echo 3. Gradle配置错误
    echo.
    echo 建议：
    echo 1. 检查JDK是否已安装
    echo 2. 查看上方错误信息
    echo 3. 尝试运行 gradlew.bat --stacktrace
    echo.

    pause
)
