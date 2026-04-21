$ErrorActionPreference = "Stop"

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $scriptDir

$dbHost = if ($env:DB_HOST) { $env:DB_HOST } else { "127.0.0.1" }
$dbPort = if ($env:DB_PORT) { $env:DB_PORT } else { "3307" }
$dbName = if ($env:DB_NAME) { $env:DB_NAME } else { "fragment_words" }
$dbUser = if ($env:DB_USER) { $env:DB_USER } else { "root" }
$dbPassword = $env:DB_PASSWORD
$skipDbPreflight = $env:SKIP_DB_PREFLIGHT -eq "1"

function Fail-AndExit {
    param(
        [string]$Message,
        [int]$Code = 1
    )

    Write-Host "[ERROR] $Message" -ForegroundColor Red
    exit $Code
}

function Test-TcpPort {
    param(
        [string]$HostName,
        [int]$Port,
        [int]$TimeoutMs = 2000
    )

    $client = New-Object System.Net.Sockets.TcpClient
    try {
        $async = $client.BeginConnect($HostName, $Port, $null, $null)
        if (-not $async.AsyncWaitHandle.WaitOne($TimeoutMs, $false)) {
            return $false
        }

        $client.EndConnect($async)
        return $true
    } catch {
        return $false
    } finally {
        $client.Dispose()
    }
}

Write-Host "========================================"
Write-Host "  Fragment Words Backend Local Runner"
Write-Host "========================================"
Write-Host ""
Write-Host "This script uses a project-local Maven cache:"
Write-Host "  $scriptDir\.m2repo"
Write-Host ""
Write-Host "Expected local services:"
Write-Host "  MySQL host: $dbHost"
Write-Host "  MySQL port: $dbPort"
Write-Host "  MySQL name: $dbName"
Write-Host "  MySQL user: $dbUser"
Write-Host "  App port:   8080"
Write-Host ""

if ([string]::IsNullOrWhiteSpace($dbPassword)) {
    Fail-AndExit @"
DB_PASSWORD is not set.
PowerShell example:
  `$env:DB_PASSWORD = "your_real_password"
  .\start-local.ps1
"@
}

if (-not (Get-Command java -ErrorAction SilentlyContinue)) {
    Fail-AndExit "Java is not available on PATH. Install JDK 17+ and retry."
}

if (-not (Test-Path ".\mvnw.cmd")) {
    Fail-AndExit "mvnw.cmd was not found in $scriptDir."
}

if (-not $skipDbPreflight -and -not (Test-TcpPort -HostName $dbHost -Port ([int]$dbPort))) {
    Fail-AndExit @"
Cannot reach MySQL at ${dbHost}:${dbPort}.
Start MySQL first, or set SKIP_DB_PREFLIGHT=1 if you intentionally want to bypass the TCP check.
"@
}

$env:SPRING_DATASOURCE_URL = "jdbc:mysql://$dbHost`:$dbPort/$dbName?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai"
$env:SPRING_DATASOURCE_USERNAME = $dbUser
$env:SPRING_DATASOURCE_PASSWORD = $dbPassword

$process = Start-Process `
    -FilePath ".\mvnw.cmd" `
    -ArgumentList @(
        "-Dmaven.repo.local=$scriptDir\.m2repo",
        "org.springframework.boot:spring-boot-maven-plugin:3.3.4:run"
    ) `
    -NoNewWindow `
    -Wait `
    -PassThru

if ($process.ExitCode -ne 0) {
    exit $process.ExitCode
}
