$projectRoot = $PSScriptRoot
Set-Location -LiteralPath $projectRoot

$sources = Get-ChildItem -Path (Join-Path $projectRoot "src") -Recurse -Filter *.java |
    ForEach-Object { $_.FullName }

if ($sources.Count -eq 0) {
    Write-Error "No Java source files found under src."
    exit 1
}

New-Item -ItemType Directory -Force -Path (Join-Path $projectRoot "bin") | Out-Null

javac -d bin -cp "lib/*;src" $sources
if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}

java -cp "bin;lib/*" com.cinemats.Main
exit $LASTEXITCODE
