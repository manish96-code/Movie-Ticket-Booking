@echo off
setlocal enabledelayedexpansion

cd /d "%~dp0"

echo ================================================
echo   Cinema Express - Starting Application...
echo ================================================

where javac >nul 2>nul
if %errorlevel% neq 0 (
    if defined JAVA_HOME (
        set "JAVAC=%JAVA_HOME%\bin\javac.exe"
        set "JAVA=%JAVA_HOME%\bin\java.exe"
    ) else (
        echo [ERROR] JDK (javac) not found in PATH or JAVA_HOME.
        echo Please install Java JDK and configure JAVA_HOME.
        pause
        exit /b 1
    )
) else (
    set "JAVAC=javac"
    set "JAVA=java"
)

if not exist bin mkdir bin

echo [1/2] Compiling sources...
dir /s /b src\*.java > sources.txt
"%JAVAC%" -d bin -cp "lib\*;src" @sources.txt
set COMPILE_STATUS=%errorlevel%
del sources.txt

if %COMPILE_STATUS% neq 0 (
    echo [ERROR] Compilation failed!
    pause
    exit /b %COMPILE_STATUS%
)

echo [2/2] Launching Cinema Express...
"%JAVA%" -cp "bin;lib\*" com.cinemats.Main %*

if %errorlevel% neq 0 (
    pause
)
