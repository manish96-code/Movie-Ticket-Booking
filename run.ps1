$projectRoot = $PSScriptRoot
Set-Location -LiteralPath $projectRoot

$javaHome = $env:JAVA_HOME
if (-not $javaHome) {
    $javaCmd = Get-Command java -ErrorAction Stop
    $javaHome = Split-Path -Parent (Split-Path -Parent $javaCmd.Source)
}

$javacPath = Join-Path $javaHome "bin\javac.exe"
$javaPath = Join-Path $javaHome "bin\java.exe"

if (-not (Test-Path $javacPath)) { Write-Error "JDK compiler not found at $javacPath"; exit 1 }
if (-not (Test-Path $javaPath)) { Write-Error "JRE not found at $javaPath"; exit 1 }

$sources = Get-ChildItem -Path (Join-Path $projectRoot "src") -Recurse -Filter *.java |
    ForEach-Object { $_.FullName }

if ($sources.Count -eq 0) {
    Write-Error "No Java source files found under src."
    exit 1
}

if (Test-Path (Join-Path $projectRoot "bin")) {
    Remove-Item -Recurse -Force (Join-Path $projectRoot "bin")
}
New-Item -ItemType Directory -Force -Path (Join-Path $projectRoot "bin") | Out-Null

& $javacPath -d bin -cp "lib/*;src" $sources
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

& $javaPath -cp "bin;lib/*" com.cinemats.Main
exit $LASTEXITCODE
