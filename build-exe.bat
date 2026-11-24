@echo off
setlocal
cd /d "%~dp0"
echo ========================================================
echo   Building CinemaExpress.exe using jpackage...
echo ========================================================
where jpackage >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERROR] jpackage not found in PATH!
    echo Please install JDK 17 or higher from https://adoptium.net/
    pause
    exit /b 1
)

if not exist dist\CinemaExpress.jar (
    echo [ERROR] dist\CinemaExpress.jar missing! Run run.bat first to build.
    pause
    exit /b 1
)

echo Packaging CinemaExpress.exe...
jpackage --type app-image -d dist -n CinemaExpress ^
    --icon assets\icon.ico ^
    --main-class com.cinemats.Main ^
    --main-jar CinemaExpress.jar ^
    -i dist\

if exist "db.properties" copy /y "db.properties" "dist\CinemaExpress\" >nul
if exist "whatsapp.properties" copy /y "whatsapp.properties" "dist\CinemaExpress\" >nul
if exist "assets" xcopy /e /i /y "assets" "dist\CinemaExpress\assets" >nul

echo Creating Desktop shortcut...
powershell -NoProfile -Command "$ws = New-Object -ComObject WScript.Shell; $s = $ws.CreateShortcut([Environment]::GetFolderPath('Desktop') + '\Cinema Express.lnk'); $s.TargetPath = '%~dp0dist\CinemaExpress\CinemaExpress.exe'; $s.WorkingDirectory = '%~dp0dist\CinemaExpress'; if (Test-Path '%~dp0assets\icon.ico') { $s.IconLocation = '%~dp0assets\icon.ico'; } $s.Save()" 2>nul

echo.
echo [SUCCESS] Windows executable created inside dist\CinemaExpress\CinemaExpress.exe!
pause
