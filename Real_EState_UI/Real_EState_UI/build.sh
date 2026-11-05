#!/bin/bash
echo "Compiling..."
mkdir -p out
javac -cp lib/mysql-connector.jar -d out src/*.java
if [ $? -ne 0 ]; then echo "Compilation failed."; exit 1; fi
echo "Packaging..."
jar cfm RealEstateUI.jar MANIFEST.MF -C out .
echo ""
echo "Done. Run with:"
echo "  java -jar RealEstateUI.jar"
