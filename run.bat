@echo off
setlocal enabledelayedexpansion

cd /d "%~dp0"

echo ================================================
echo   Cinema Express - Starting Application...
echo ================================================

set "JAVAC="
set "JAVA="

:: 1. Check if javac is directly available in PATH
where javac >nul 2>nul
if %errorlevel% equ 0 (
    set "JAVAC=javac"
    set "JAVA=java"
    goto :JAVA_LOCATED
)

:: 2. Check JAVA_HOME (cleaning quotes if present)
if defined JAVA_HOME (
    set "CLEAN_JH=%JAVA_HOME:"=%"
    if exist "!CLEAN_JH!\bin\javac.exe" (
        set "JAVAC=!CLEAN_JH!\bin\javac.exe"
        set "JAVA=!CLEAN_JH!\bin\java.exe"
        goto :JAVA_LOCATED
    )
)

:: 3. Scan 64-bit Program Files JDK directories
for /d %%D in ("%ProgramFiles%\Java\jdk*" "%ProgramFiles%\Eclipse Adoptium\jdk*" "%ProgramFiles%\Microsoft\jdk*" "%ProgramFiles%\Amazon Corretto\jdk*" "%ProgramFiles%\BellSoft\jdk*" "%ProgramFiles%\Zulu\zulu*") do (
    if exist "%%D\bin\javac.exe" (
        set "JAVAC=%%D\bin\javac.exe"
        set "JAVA=%%D\bin\java.exe"
        goto :JAVA_LOCATED
    )
)

:: 4. Scan 32-bit Program Files (x86) if exists
if defined ProgramFiles(x86) (
    for /d %%D in ("!ProgramFiles(x86)!\Java\jdk*") do (
        if exist "%%D\bin\javac.exe" (
            set "JAVAC=%%D\bin\javac.exe"
            set "JAVA=%%D\bin\java.exe"
            goto :JAVA_LOCATED
        )
    )
)

:: 5. Scan user profile directories (IntelliJ IDEA & LocalAppData)
if defined USERPROFILE (
    for /d %%D in ("%USERPROFILE%\.jdks\*" "%LOCALAPPDATA%\Programs\Eclipse Adoptium\jdk*") do (
        if exist "%%D\bin\javac.exe" (
            set "JAVAC=%%D\bin\javac.exe"
            set "JAVA=%%D\bin\java.exe"
            goto :JAVA_LOCATED
        )
    )
)

:JAVA_LOCATED
if "%JAVAC%"=="" (
    echo.
    echo ========================================================
    echo  [ERROR] Java Development Kit (JDK) not found!
    echo ========================================================
    echo  Cinema Express requires a JDK (javac) to compile and run.
    echo.
    echo  Please install Java JDK (v17 or higher recommended):
    echo  • Eclipse Temurin: https://adoptium.net/
    echo  • Oracle JDK:      https://www.oracle.com/java/technologies/downloads/
    echo.
    echo  If already installed, ensure JAVA_HOME is configured.
    echo ========================================================
    echo.
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
    echo.
    echo [ERROR] Compilation failed! Check error messages above.
    pause
    exit /b %COMPILE_STATUS%
)

echo [2/2] Launching Cinema Express...
"%JAVA%" -cp "bin;lib\*" com.cinemats.Main %*

if %errorlevel% neq 0 (
    pause
)
