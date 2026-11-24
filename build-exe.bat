@echo off
setlocal
cd /d "%~dp0"

echo ========================================================
echo   Cinema Express - Building Windows Desktop App (.exe)
echo ========================================================

where jpackage >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERROR] jpackage not found in PATH!
    echo Please install JDK 17 or higher from https://adoptium.net/
    pause
    exit /b 1
)

if not exist "dist\CinemaExpress.jar" (
    echo [ERROR] dist\CinemaExpress.jar missing!
    echo Please run CinemaExpress.bat or run.bat first to build the JAR.
    pause
    exit /b 1
)

echo [1/3] Packaging standalone executable using jpackage...
if exist "dist\CinemaExpress" (
    rmdir /s /q "dist\CinemaExpress" 2>nul
)

jpackage --type app-image -d dist -n CinemaExpress ^
    --icon assets\icon.ico ^
    --main-class com.cinemats.Main ^
    --main-jar CinemaExpress.jar ^
    -i dist\

if %errorlevel% neq 0 (
    echo [ERROR] jpackage packaging failed.
    pause
    exit /b 1
)

echo [2/3] Copying runtime configuration and assets...
if exist "db.properties" copy /y "db.properties" "dist\CinemaExpress\" >nul
if exist "whatsapp.properties" copy /y "whatsapp.properties" "dist\CinemaExpress\" >nul
if exist "assets" xcopy /e /i /y "assets" "dist\CinemaExpress\assets" >nul

echo [3/3] Creating Windows Desktop Shortcut...
powershell -NoProfile -Command "$ws = New-Object -ComObject WScript.Shell; $s = $ws.CreateShortcut([Environment]::GetFolderPath('Desktop') + '\Cinema Express.lnk'); $s.TargetPath = '%~dp0dist\CinemaExpress\CinemaExpress.exe'; $s.WorkingDirectory = '%~dp0dist\CinemaExpress'; if (Test-Path '%~dp0assets\icon.ico') { $s.IconLocation = '%~dp0assets\icon.ico'; } $s.Save()" 2>nul

echo.
echo ========================================================
echo   [SUCCESS] Build Complete!
echo   • Executable: dist\CinemaExpress\CinemaExpress.exe
echo   • Desktop Shortcut created: 'Cinema Express' on Desktop
echo ========================================================
echo.
pause
