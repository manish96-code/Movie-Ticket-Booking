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
    -i dist\ ^
    --win-shortcut ^
    --win-menu

echo.
echo [SUCCESS] Windows executable created inside dist\CinemaExpress\CinemaExpress.exe!
pause
