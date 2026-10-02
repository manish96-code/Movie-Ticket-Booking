@echo off
setlocal enabledelayedexpansion

cd /d "%~dp0"

echo ================================================
echo   Cinema Express - MySQL Database Seeder Tool
echo ================================================

set "JAVAC="
set "JAVA="

where javac >nul 2>nul
if %errorlevel% equ 0 (
    set "JAVAC=javac"
    set "JAVA=java"
    goto :JAVA_LOCATED
)

if defined JAVA_HOME (
    set "CLEAN_JH=%JAVA_HOME:"=%"
    if exist "!CLEAN_JH!\bin\javac.exe" (
        set "JAVAC=!CLEAN_JH!\bin\javac.exe"
        set "JAVA=!CLEAN_JH!\bin\java.exe"
        goto :JAVA_LOCATED
    )
)

for /d %%D in ("%ProgramFiles%\Java\jdk*" "%ProgramFiles%\Eclipse Adoptium\jdk*" "%ProgramFiles%\Microsoft\jdk*" "%ProgramFiles%\Amazon Corretto\jdk*" "%ProgramFiles%\BellSoft\jdk*" "%ProgramFiles%\Zulu\zulu*") do (
    if exist "%%D\bin\javac.exe" (
        set "JAVAC=%%D\bin\javac.exe"
        set "JAVA=%%D\bin\java.exe"
        goto :JAVA_LOCATED
    )
)

:JAVA_LOCATED
if "%JAVAC%"=="" (
    echo [ERROR] JDK not found. Please install JDK 17+ or set JAVA_HOME.
    pause
    exit /b 1
)

if not exist bin mkdir bin

echo [1/2] Compiling sources...
if exist sources.txt del sources.txt
for /r src %%F in (*.java) do (
    echo "%%F">>sources.txt
)

"%JAVAC%" -d bin -cp "lib\*;src" @sources.txt
set COMPILE_STATUS=%errorlevel%
if exist sources.txt del sources.txt

if %COMPILE_STATUS% neq 0 (
    echo [ERROR] Compilation failed!
    pause
    exit /b %COMPILE_STATUS%
)

echo [2/2] Running MySQL Database Seeder...
"%JAVA%" -cp "bin;lib\*" com.cinemats.data.DatabaseSeeder %*
