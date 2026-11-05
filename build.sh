#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
mkdir -p out
javac -cp "lib/mysql-connector-j-9.6.0.jar" -d out src/*.java
jar cfm RealEstateUI.jar MANIFEST.MF -C out .
echo "Build complete. Run: java -jar RealEstateUI.jar"
