#!/usr/bin/env bash
set -e

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$PROJECT_ROOT"

JAVA_HOME_VALUE="${JAVA_HOME:-}"
if [ -z "$JAVA_HOME_VALUE" ]; then
  if command -v java >/dev/null 2>&1; then
    JAVA_HOME_VALUE="$(dirname "$(dirname "$(readlink -f "$(command -v java)")")")"
  fi
fi

if [ -z "$JAVA_HOME_VALUE" ] || [ ! -x "$JAVA_HOME_VALUE/bin/java" ] || [ ! -x "$JAVA_HOME_VALUE/bin/javac" ]; then
  echo "Java JDK not found in JAVA_HOME or PATH. Please install a JDK and retry."
  exit 1
fi

echo "================================================"
echo "  Cinema Express - Starting Application..."
echo "  Root: $PROJECT_ROOT"
echo "  Java: $JAVA_HOME_VALUE"
echo "================================================"

rm -rf bin
mkdir -p bin

echo "[1/2] Compiling sources..."
"$JAVA_HOME_VALUE/bin/javac" -d bin -cp "lib/*:src" $(find src -name "*.java")

echo "[2/2] Launching Cinema Express..."
"$JAVA_HOME_VALUE/bin/java" -cp "bin:lib/*" com.cinemats.Main "$@"
