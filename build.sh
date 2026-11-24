#!/usr/bin/env bash
set -e

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$PROJECT_ROOT"

echo "=========================================================="
echo "  🎬 Cinema Express - Standalone Executable Packaging"
echo "=========================================================="

# 1. Compile sources
echo "[1/5] Compiling Java sources..."
rm -rf bin
mkdir -p bin
find src -name "*.java" > sources.txt
javac -d bin -cp "bin:lib/*" @sources.txt
rm -f sources.txt

# 2. Generate Icon if missing
if [ ! -f "assets/icon.png" ]; then
    echo "[2/5] Generating application icon..."
    python3 -c '
from PIL import Image, ImageDraw
size = 256
im = Image.new("RGBA", (size, size), (0, 0, 0, 0))
draw = ImageDraw.Draw(im)
draw.rounded_rectangle([8, 8, size-8, size-8], radius=40, fill=(15, 23, 42, 255), outline=(37, 99, 235, 255), width=6)
draw.rounded_rectangle([50, 60, 206, 100], radius=8, fill=(30, 41, 59, 255), outline=(148, 163, 184, 255), width=3)
for x in range(65, 195, 30):
    draw.polygon([(x, 60), (x+15, 60), (x, 100), (x-15, 100)], fill=(241, 245, 249, 230))
draw.rounded_rectangle([50, 106, 206, 185], radius=8, fill=(15, 23, 42, 255), outline=(148, 163, 184, 255), width=3)
draw.polygon([(115, 128), (150, 145), (115, 162)], fill=(245, 158, 11, 255))
draw.text((size//2, 210), "CINEMA EXPRESS", fill=(255, 255, 255, 240), anchor="mm")
im.save("assets/icon.png", "PNG")
im.resize((64, 64)).save("assets/icon.ico", format="ICO")
' 2>/dev/null || true
fi

# 3. Build Standalone Fat JAR
echo "[3/5] Building standalone executable JAR (dist/CinemaExpress.jar)..."
mkdir -p dist
STAGING="/tmp/cinema_fat_jar_build"
rm -rf "$STAGING"
mkdir -p "$STAGING"

# Extract all lib JARs
for jar in lib/*.jar; do
    if [ -f "$jar" ]; then
        unzip -q -o "$jar" -d "$STAGING"
    fi
done

# Copy class files and assets
cp -r bin/* "$STAGING/"
if [ -d "assets" ]; then
    mkdir -p "$STAGING/assets"
    cp -r assets/* "$STAGING/assets/"
fi

# Strip signatures from unpacked third-party jars
rm -f "$STAGING"/META-INF/*.SF "$STAGING"/META-INF/*.DSA "$STAGING"/META-INF/*.RSA

# Write Clean Manifest
mkdir -p "$STAGING/META-INF"
cat << 'EOF' > "$STAGING/META-INF/MANIFEST.MF"
Manifest-Version: 1.0
Main-Class: com.cinemats.Main
Created-By: Cinema Express Distribution Packager

EOF

rm -f dist/CinemaExpress.jar
jar cf0m dist/CinemaExpress.jar "$STAGING/META-INF/MANIFEST.MF" -C "$STAGING" .
rm -rf "$STAGING"

# Copy runtime config to dist
cp -n db.properties dist/ 2>/dev/null || true
cp -n whatsapp.properties dist/ 2>/dev/null || true
cp -r assets dist/ 2>/dev/null || true

# 4. Build Native Linux App Bundle via jpackage
echo "[4/5] Building native executable bundle via jpackage..."
rm -rf dist/CinemaExpress
if command -v jpackage >/dev/null 2>&1; then
    jpackage --type app-image -d dist/ -n "CinemaExpress" \
        --icon "assets/icon.png" \
        --main-class com.cinemats.Main \
        --main-jar "CinemaExpress.jar" \
        -i "dist/" >/dev/null 2>&1 || true

    # Copy assets and config inside app bundle directory
    if [ -d "dist/CinemaExpress" ]; then
        cp -n db.properties dist/CinemaExpress/ 2>/dev/null || true
        cp -n whatsapp.properties dist/CinemaExpress/ 2>/dev/null || true
        cp -r assets dist/CinemaExpress/ 2>/dev/null || true
    fi
fi

# 5. Create Desktop Entry & Launchers
echo "[5/5] Creating 1-click launchers for Linux & Windows..."

# A. Linux Desktop Shortcut
cat << EOF > "$PROJECT_ROOT/Cinema Express.desktop"
[Desktop Entry]
Version=1.0
Type=Application
Name=Cinema Express
Comment=Cinema Express Box Office POS & HQ Control Panel
Exec=bash -c "cd '$PROJECT_ROOT' && java -jar dist/CinemaExpress.jar"
Icon=$PROJECT_ROOT/assets/icon.png
Terminal=false
Categories=Office;Finance;Java;
StartupNotify=true
StartupWMClass=com-cinemats-Main
EOF
chmod +x "$PROJECT_ROOT/Cinema Express.desktop"

# Copy to ~/Desktop if writable
if [ -d "$HOME/Desktop" ] && [ -w "$HOME/Desktop" ]; then
    cp "$PROJECT_ROOT/Cinema Express.desktop" "$HOME/Desktop/" 2>/dev/null || true
    chmod +x "$HOME/Desktop/Cinema Express.desktop" 2>/dev/null || true
fi

# B. Linux One-Click Shell Launcher
cat << 'EOF' > "$PROJECT_ROOT/CinemaExpress.sh"
#!/usr/bin/env bash
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$PROJECT_ROOT"

if [ -f "dist/CinemaExpress/bin/CinemaExpress" ]; then
    exec "./dist/CinemaExpress/bin/CinemaExpress" "$@"
elif [ -f "dist/CinemaExpress.jar" ]; then
    exec java -jar "dist/CinemaExpress.jar" "$@"
else
    exec ./run.sh "$@"
fi
EOF
chmod +x "$PROJECT_ROOT/CinemaExpress.sh"

# C. Windows Silent Launcher (.vbs - No Black Window)
cat << 'EOF' > "$PROJECT_ROOT/CinemaExpress.vbs"
Set WshShell = CreateObject("WScript.Shell")
strPath = CreateObject("Scripting.FileSystemObject").GetParentFolderName(WScript.ScriptFullName)
WshShell.CurrentDirectory = strPath
WshShell.Run "javaw -jar dist\CinemaExpress.jar", 0, False
EOF

# D. Windows Batch Launcher (.bat)
cat << 'EOF' > "$PROJECT_ROOT/CinemaExpress.bat"
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
EOF

# E. Windows .exe Builder script for users on Windows
cat << 'EOF' > "$PROJECT_ROOT/build-exe.bat"
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
EOF

echo ""
echo "=========================================================="
echo "  ✅ Build Complete!"
echo "  • Standalone Executable JAR: dist/CinemaExpress.jar"
echo "  • Native Linux Executable:   dist/CinemaExpress/bin/CinemaExpress"
echo "  • Linux Desktop Shortcut:    'Cinema Express.desktop'"
echo "  • Linux 1-Click Script:      './CinemaExpress.sh'"
echo "  • Windows Silent Launcher:   'CinemaExpress.vbs'"
echo "  • Windows 1-Click Batch:     'CinemaExpress.bat'"
echo "  • Windows .exe Builder:      'build-exe.bat'"
echo "=========================================================="

