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
