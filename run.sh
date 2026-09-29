#!/usr/bin/env bash
set -e

# Always find the true directory where this script is located
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$PROJECT_ROOT"

echo "================================================"
echo "  Cinema Express - Starting Application..."
echo "  Root: $PROJECT_ROOT"
echo "================================================"

mkdir -p bin

echo "[1/2] Compiling sources..."
javac -d bin -cp "lib/*:src" $(find src -name "*.java")

echo "[2/2] Launching Cinema Express..."
java -cp "bin:lib/*" com.cinemats.Main "$@"
