$ErrorActionPreference = "Stop"

$rootDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$adb = "C:\Users\Andrew\AppData\Local\Android\Sdk\platform-tools\adb.exe"

function Ensure-Backend {
    Write-Host "[1/7] Checking backend on localhost:8080..."
    $listening = netstat -ano | Select-String ":8080" | Select-String "LISTENING"
    if (-not $listening) {
        throw "Backend is not listening on localhost:8080. Start backend\start-local.bat first."
    }
}

function Install-And-LaunchApp {
    Write-Host ""
    Write-Host "[2/7] Installing and launching Android debug app..."
    & (Join-Path $rootDir "install-debug-and-launch.bat")
    if ($LASTEXITCODE -ne 0) {
        throw "install-debug-and-launch.bat failed."
    }
}

function Resolve-DeviceSerial {
    Write-Host ""
    Write-Host "[3/7] Resolving connected adb device..."
    $devices = & $adb devices
    $deviceLine = $devices | Select-String "\sdevice$" | Select-Object -First 1
    if (-not $deviceLine) {
        throw "No online adb device found."
    }
    $serial = ($deviceLine -split "\s+")[0]
    Write-Host "Device: $serial"
    return $serial
}

function Read-AppXmlValue {
    param(
        [string]$Serial,
        [string]$PrefsFile,
        [string]$Key
    )

    $raw = & $adb -s $Serial shell run-as com.fragmentwords cat "/data/data/com.fragmentwords/shared_prefs/$PrefsFile"
    [xml]$xml = ($raw -join [Environment]::NewLine)
    $node = $xml.map.string | Where-Object { $_.name -eq $Key } | Select-Object -First 1
    if (-not $node) {
        throw "Could not read '$Key' from $PrefsFile."
    }
    return $node.'#text'
}

function Get-UiDumpXml {
    param([string]$Serial)

    $dump = & $adb -s $Serial exec-out uiautomator dump /dev/tty
    $rawText = ($dump -join [Environment]::NewLine)
    $startIndex = $rawText.IndexOf("<?xml")
    $endIndex = $rawText.LastIndexOf("</hierarchy>")
    if ($startIndex -lt 0 -or $endIndex -lt 0) {
        throw "Could not extract XML from UI dump."
    }
    $xmlText = $rawText.Substring($startIndex, ($endIndex - $startIndex) + "</hierarchy>".Length)
    return [xml]$xmlText
}

function Tap-UiNode {
    param(
        [string]$Serial,
        [string]$XPath,
        [string]$Description
    )

    $xml = Get-UiDumpXml -Serial $Serial
    $node = $xml.SelectSingleNode($XPath)
    if (-not $node) {
        throw "Could not find UI node for $Description."
    }

    $bounds = $node.GetAttribute("bounds")
    if ($bounds -notmatch "\[(\d+),(\d+)\]\[(\d+),(\d+)\]") {
        throw "Could not parse bounds for $Description."
    }

    $x = [int](($Matches[1] + $Matches[3]) / 2)
    $y = [int](($Matches[2] + $Matches[4]) / 2)
    & $adb -s $Serial shell input tap $x $y | Out-Null
    Start-Sleep -Milliseconds 800
}

function Ensure-BackendBackedWord {
    param([string]$Serial)

    Write-Host "Current word is local fallback. Restarting app to request a backend-backed word..."
    & $adb -s $Serial shell am force-stop com.fragmentwords | Out-Null
    & $adb -s $Serial shell cmd statusbar collapse | Out-Null
    & $adb -s $Serial shell am start -n com.fragmentwords/.MainActivity | Out-Null
    Start-Sleep -Seconds 4
}

function Read-CurrentWord {
    param([string]$Serial)

    Write-Host ""
    Write-Host "[4/7] Reading app device_id and current word..."
    $deviceId = Read-AppXmlValue -Serial $Serial -PrefsFile "fragment_words_prefs.xml" -Key "device_id"
    $currentWordJson = Read-AppXmlValue -Serial $Serial -PrefsFile "word_prefs.xml" -Key "current_word"
    $currentWord = $currentWordJson | ConvertFrom-Json

    if (-not $currentWord.PSObject.Properties.Name.Contains("id")) {
        Ensure-BackendBackedWord -Serial $Serial
        $currentWordJson = Read-AppXmlValue -Serial $Serial -PrefsFile "word_prefs.xml" -Key "current_word"
        $currentWord = $currentWordJson | ConvertFrom-Json
    }

    if (-not $currentWord.PSObject.Properties.Name.Contains("id")) {
        throw "Current word is still missing backend id after restart. Switch the app to CET4 and try again."
    }

    Write-Host "App device_id : $deviceId"
    Write-Host "Current word  : $($currentWord.word)"

    return @{
        DeviceId = $deviceId
        Word = $currentWord
    }
}

function Read-BackendState {
    param([string]$DeviceId)

    $headers = @{ "X-Device-Id" = $DeviceId }

    for ($attempt = 1; $attempt -le 3; $attempt++) {
        try {
            $notebook = Invoke-RestMethod -Method Get -Uri "http://localhost:8080/api/v1/notebook/count" -Headers $headers
            $notebookPage = Invoke-RestMethod -Method Get -Uri "http://localhost:8080/api/v1/notebook" -Headers $headers
            $stats = Invoke-RestMethod -Method Get -Uri "http://localhost:8080/api/v1/learning/stats" -Headers $headers

            return @{
                NotebookCount = [int]$notebook.data
                NotebookWords = @($notebookPage.data.items | ForEach-Object { $_.word })
                TotalWords = [int]$stats.data.totalWords
                NeedReview = [int]$stats.data.needReviewWords
            }
        } catch {
            if ($attempt -eq 3) {
                throw
            }
            Start-Sleep -Seconds 2
        }
    }
}

function Tap-UnknownAction {
    param([string]$Serial)

    Write-Host ""
    Write-Host "[6/7] Expanding notifications and tapping `"unknown`"..."
    & $adb -s $Serial shell cmd statusbar expand-notifications | Out-Null
    Start-Sleep -Seconds 2

    $xml = Get-UiDumpXml -Serial $Serial

    $node = $xml.SelectSingleNode("//node[@resource-id='android:id/action1' or @content-desc='unknown']")
    if (-not $node) {
        throw "Could not find the notification unknown action."
    }

    $bounds = $node.GetAttribute("bounds")
    $match = [regex]::Match($bounds, "\[(\d+),(\d+)\]\[(\d+),(\d+)\]")
    if (-not $match.Success) {
        throw "Could not parse notification action bounds."
    }

    $left = [int]$match.Groups[1].Value
    $top = [int]$match.Groups[2].Value
    $right = [int]$match.Groups[3].Value
    $bottom = [int]$match.Groups[4].Value
    $x = [int](($left + $right) / 2)
    $y = [int](($top + $bottom) / 2)
    & $adb -s $Serial shell input tap $x $y | Out-Null
    Write-Host "Tapped unknown action at $x,$y"
    Start-Sleep -Seconds 4
}

Write-Host "========================================"
Write-Host "  Fragment Words Unknown Action Smoke"
Write-Host "========================================"
Write-Host ""

Ensure-Backend
Install-And-LaunchApp
$serial = Resolve-DeviceSerial
$appState = Read-CurrentWord -Serial $serial

Write-Host ""
Write-Host "[5/7] Reading backend baseline..."
$before = Read-BackendState -DeviceId $appState.DeviceId
$beforeHasWord = $before.NotebookWords -contains $appState.Word.word
Write-Host "Notebook count before : $($before.NotebookCount)"
Write-Host "Total words before    : $($before.TotalWords)"
Write-Host "Need review before    : $($before.NeedReview)"
Write-Host "Word already in notebook before : $beforeHasWord"

Tap-UnknownAction -Serial $serial

Write-Host ""
Write-Host "[7/7] Reading backend state after action..."
$after = Read-BackendState -DeviceId $appState.DeviceId
$afterHasWord = $after.NotebookWords -contains $appState.Word.word
Write-Host "Notebook count after  : $($after.NotebookCount)"
Write-Host "Total words after     : $($after.TotalWords)"
Write-Host "Need review after     : $($after.NeedReview)"
Write-Host "Word in notebook after        : $afterHasWord"
Write-Host ""
Write-Host "Delta notebook count  : $($after.NotebookCount - $before.NotebookCount)"
Write-Host "Delta total words     : $($after.TotalWords - $before.TotalWords)"
Write-Host "Delta need review     : $($after.NeedReview - $before.NeedReview)"
Write-Host ""
Write-Host "========================================"
Write-Host "  Unknown Smoke Finished"
Write-Host "========================================"
