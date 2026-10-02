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

if command -v javac >/dev/null 2>&1 && command -v java >/dev/null 2>&1; then
  JAVAC_CMD="javac"
  JAVA_CMD="java"
fi

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
fi

if [ -z "$JAVAC_CMD" ] || [ -z "$JAVA_CMD" ]; then
  echo "=================================================="
  echo " [ERROR] Java Development Kit (JDK) not found!"
  echo "=================================================="
  exit 1
fi

echo "================================================"
echo "  Cinema Express - Database Seeder Tool"
echo "  Target: Checking database..."
echo "================================================"

mkdir -p bin
echo "[1/2] Compiling sources..."
find src -name "*.java" | sed 's/^/"/;s/$/"/' > sources.txt
"$JAVAC_CMD" -d bin -cp "lib/*${CP_SEP}src" @sources.txt
rm -f sources.txt

echo "[2/2] Running Database Seeder..."
"$JAVA_CMD" -cp "bin${CP_SEP}lib/*" com.cinemats.data.DatabaseSeeder "$@"
