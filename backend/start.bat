@echo off
REM ============================================
REM 锁屏背单词 - 后端服务启动脚本
REM ============================================

chcp 65001 >nul
echo.
echo ============================================
echo  锁屏背单词 - 后端服务启动中...
echo ============================================
echo.

REM 检查Java环境
echo [1/4] 检查Java环境...
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] 未检测到Java环境，请先安装JDK 17或更高版本
    pause
    exit /b 1
)
echo [√] Java环境检查通过
echo.

REM 检查MySQL连接
echo [2/4] 检查MySQL数据库连接...
mysql -uroot -p123456 -e "USE fragment_words;" >nul 2>&1
if %errorlevel% neq 0 (
    echo [警告] 无法连接到MySQL数据库
    echo 请确保MySQL已启动，并执行以下操作：
    echo   1. 创建数据库：mysql -uroot -p123456
    echo   2. 执行脚本：source backend/src/main/resources/sql/init.sql
    echo.
    set /p continue="是否继续启动？(Y/N): "
    if /i not "%continue%"=="Y" (
        pause
        exit /b 1
    )
) else (
    echo [√] MySQL数据库连接成功
)
echo.

REM 检查Maven环境
echo [3/4] 检查Maven环境...
call mvnw.cmd --version >nul 2>&1
if %errorlevel% neq 0 (
    echo [警告] Maven Wrapper不可用，尝试使用系统Maven...
    call mvn --version >nul 2>&1
    if %errorlevel% neq 0 (
        echo [错误] 未检测到Maven环境
        pause
        exit /b 1
    )
    set MAVEN_CMD=mvn
) else (
    set MAVEN_CMD=mvnw.cmd
)
echo [√] Maven环境检查通过
echo.

REM 启动Spring Boot应用
echo [4/4] 启动Spring Boot应用...
echo 服务地址: http://localhost:8080
echo API文档: http://localhost:8080/swagger-ui/index.html
echo.
echo 按 Ctrl+C 可停止服务
echo ============================================
echo.

call %MAVEN_CMD% spring-boot:run

pause
