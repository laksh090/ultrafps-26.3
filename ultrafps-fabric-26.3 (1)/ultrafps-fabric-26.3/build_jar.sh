#!/usr/bin/env bash
set -e
echo "==================================================="
echo "  Building UltraFPS Boost Mod (.jar) for MC 26.3"
echo "==================================================="

if [ -f "./gradlew" ]; then
    chmod +x ./gradlew
    ./gradlew build
elif command -v gradle >/dev/null 2>&1; then
    gradle build
else
    echo "[ERROR] Gradle not found. Install Gradle 9.6.0+ or open this project in IntelliJ IDEA."
    exit 1
fi

echo ""
echo "[SUCCESS] Mod JAR created at: build/libs/ultrafps-boost-1.0.0+26.3.jar"
