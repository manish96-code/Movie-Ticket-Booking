@echo off
setlocal
cd /d "%~dp0"
where javaw >nul 2>nul
if %errorlevel% equ 0 (
    start "" javaw -jar "dist\CinemaExpress.jar" %*
) else (
    where java >nul 2>nul
    if %errorlevel% equ 0 (
        start "" java -jar "dist\CinemaExpress.jar" %*
    ) else (
        call run.bat %*
    )
)
