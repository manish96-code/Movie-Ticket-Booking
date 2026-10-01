# Cinema Express PowerShell Launcher for Windows
$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Definition
Set-Location $projectRoot

Write-Host "================================================" -ForegroundColor Cyan
Write-Host "  Cinema Express - Starting Application..." -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan

$javacCmd = $null
$javaCmd = $null

# 1. Check PATH
if (Get-Command javac -ErrorAction SilentlyContinue) {
    $javacCmd = "javac"
    $javaCmd = "java"
}

# 2. Check JAVA_HOME
if (-not $javacCmd -and $env:JAVA_HOME) {
    $candJavac = Join-Path $env:JAVA_HOME "bin\javac.exe"
    $candJava = Join-Path $env:JAVA_HOME "bin\java.exe"
    if (Test-Path $candJavac) {
        $javacCmd = $candJavac
        $javaCmd = $candJava
    }
}

# 3. Check common JDK directories
if (-not $javacCmd) {
    $searchPaths = @(
        "$env:ProgramFiles\Java\jdk*",
        "$env:ProgramFiles\Eclipse Adoptium\jdk*",
        "$env:ProgramFiles\Microsoft\jdk*",
        "$env:ProgramFiles\Amazon Corretto\jdk*",
        "$env:ProgramFiles\BellSoft\jdk*",
        "$env:ProgramFiles\Zulu\zulu*",
        "${env:ProgramFiles(x86)}\Java\jdk*"
    )
    foreach ($pattern in $searchPaths) {
        $found = Get-Item $pattern -ErrorAction SilentlyContinue | Select-Object -First 1
        if ($found) {
            $candJavac = Join-Path $found.FullName "bin\javac.exe"
            $candJava = Join-Path $found.FullName "bin\java.exe"
            if (Test-Path $candJavac) {
                $javacCmd = $candJavac
                $javaCmd = $candJava
                break
            }
        }
    }
}

if (-not $javacCmd) {
    Write-Host "`n[ERROR] Java Development Kit (JDK) not found!" -ForegroundColor Red
    Write-Host "Cinema Express requires a JDK (javac) to compile and run." -ForegroundColor Yellow
    Write-Host "Please install JDK 17+ from: https://adoptium.net/" -ForegroundColor Yellow
    Read-Host "Press Enter to exit..."
    exit 1
}

if (-not (Test-Path "bin")) {
    New-Item -ItemType Directory -Path "bin" | Out-Null
}

Write-Host "[1/2] Compiling sources..." -ForegroundColor Green
$sources = Get-ChildItem -Path "src" -Filter "*.java" -Recurse | ForEach-Object { $_.FullName }
$sources | Out-File -FilePath "sources.txt" -Encoding utf8

& $javacCmd -d bin -cp "lib/*;src" "@sources.txt"
$compileSuccess = ($LASTEXITCODE -eq 0)
Remove-Item -Force "sources.txt" -ErrorAction SilentlyContinue

if (-not $compileSuccess) {
    Write-Host "`n[ERROR] Compilation failed!" -ForegroundColor Red
    Read-Host "Press Enter to exit..."
    exit 1
}

Write-Host "[2/2] Launching Cinema Express..." -ForegroundColor Green
& $javaCmd -cp "bin;lib/*" com.cinemats.Main $args
