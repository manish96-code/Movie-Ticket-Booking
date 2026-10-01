#!/usr/bin/env bash
set -e

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$PROJECT_ROOT"

# Detect Operating System (Linux, macOS, or Windows via Git Bash/MSYS/Cygwin)
OS_NAME="$(uname -s 2>/dev/null || echo "Linux")"
case "$OS_NAME" in
  CYGWIN*|MINGW*|MSYS*)
    IS_WINDOWS=true
    CP_SEP=";"
    ;;
  *)
    IS_WINDOWS=false
    CP_SEP=":"
    ;;
esac

# Locate Java compiler (javac) and runtime (java)
JAVAC_CMD=""
JAVA_CMD=""

# 1. Check if javac is already in PATH
if command -v javac >/dev/null 2>&1 && command -v java >/dev/null 2>&1; then
  JAVAC_CMD="javac"
  JAVA_CMD="java"
fi

# 2. Check JAVA_HOME if set
if [ -z "$JAVAC_CMD" ] && [ -n "${JAVA_HOME:-}" ]; then
  JH="$JAVA_HOME"
  if [ "$IS_WINDOWS" = true ] && command -v cygpath >/dev/null 2>&1; then
    JH="$(cygpath -u "$JAVA_HOME")"
  fi
  if [ -x "$JH/bin/javac" ] || [ -f "$JH/bin/javac.exe" ]; then
    JAVAC_CMD="$JH/bin/javac"
    JAVA_CMD="$JH/bin/java"
  fi
fi

# 3. If on Windows (Git Bash), search standard Windows JDK install paths
if [ -z "$JAVAC_CMD" ] && [ "$IS_WINDOWS" = true ]; then
  for dir in \
    "/c/Program Files/Java"/jdk* \
    "/c/Program Files/Eclipse Adoptium"/jdk* \
    "/c/Program Files/Microsoft"/jdk* \
    "/c/Program Files/Amazon Corretto"/jdk* \
    "/c/Program Files/BellSoft"/jdk* \
    "/c/Program Files/Zulu"/zulu* \
    "/c/Program Files (x86)/Java"/jdk* \
    "$HOME/.jdks"/* \
    "/c/Users/"*/.jdks/* \
    "/c/Users/"*/AppData/Local/Programs/Eclipse\ Adoptium/jdk*; do
    if [ -d "$dir" ] && ([ -x "$dir/bin/javac.exe" ] || [ -f "$dir/bin/javac.exe" ]); then
      JAVAC_CMD="$dir/bin/javac.exe"
      JAVA_CMD="$dir/bin/java.exe"
      break
    fi
  done
fi

# 4. If on Linux / macOS, search standard Unix JDK install paths
if [ -z "$JAVAC_CMD" ] && [ "$IS_WINDOWS" = false ]; then
  if command -v java >/dev/null 2>&1; then
    RESOLVED_JAVA="$(readlink -f "$(command -v java)" 2>/dev/null || true)"
    if [ -n "$RESOLVED_JAVA" ]; then
      JH="$(dirname "$(dirname "$RESOLVED_JAVA")")"
      if [ -x "$JH/bin/javac" ]; then
        JAVAC_CMD="$JH/bin/javac"
        JAVA_CMD="$JH/bin/java"
      fi
    fi
  fi
  if [ -z "$JAVAC_CMD" ]; then
    for dir in /usr/lib/jvm/java-* /usr/lib/jvm/jdk* /Library/Java/JavaVirtualMachines/*/Contents/Home "$HOME/.jdks"/*; do
      if [ -x "$dir/bin/javac" ]; then
        JAVAC_CMD="$dir/bin/javac"
        JAVA_CMD="$dir/bin/java"
        break
      fi
    done
  fi
fi

if [ -z "$JAVAC_CMD" ] || [ -z "$JAVA_CMD" ]; then
  echo "=================================================="
  echo " [ERROR] Java Development Kit (JDK) not found!"
  echo "=================================================="
  echo " Cinema Express requires a JDK (with javac) to compile and run."
  echo " Please install JDK 17 or higher:"
  echo " • Eclipse Temurin: https://adoptium.net/"
  echo " • Or ensure JAVA_HOME is configured."
  echo ""
  if [ "$IS_WINDOWS" = true ]; then
    echo " Tip for Windows: You can also double-click 'run.bat'!"
  fi
  exit 1
fi

JAVA_VERSION_STR="$("$JAVA_CMD" -version 2>&1 | head -n 1)"

echo "================================================"
echo "  Cinema Express - Starting Application..."
echo "  Root: $PROJECT_ROOT"
echo "  OS:   $OS_NAME"
echo "  Java: $JAVA_VERSION_STR"
echo "================================================"

rm -rf bin
mkdir -p bin

echo "[1/2] Compiling sources..."
find src -name "*.java" | sed 's/^/"/;s/$/"/' > sources.txt
"$JAVAC_CMD" -d bin -cp "lib/*${CP_SEP}src" @sources.txt
rm -f sources.txt

echo "[2/2] Launching Cinema Express..."
"$JAVA_CMD" -cp "bin${CP_SEP}lib/*" com.cinemats.Main "$@"
