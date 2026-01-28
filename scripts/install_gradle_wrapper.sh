#!/bin/bash
set -e

# Define variables
GRADLE_VERSION="8.2"
GRADLE_ZIP="gradle-${GRADLE_VERSION}-bin.zip"
GRADLE_URL="https://services.gradle.org/distributions/${GRADLE_ZIP}"
TEMP_DIR="temp_gradle_install"

echo "1. Creating temp directory..."
mkdir -p $TEMP_DIR
cd $TEMP_DIR

echo "2. Downloading Gradle ${GRADLE_VERSION}..."
curl -L -O $GRADLE_URL

echo "3. Unzipping Gradle..."
unzip -q $GRADLE_ZIP

echo "4. Generating Gradle Wrapper..."
# Go back to project root
cd ..
"$TEMP_DIR/gradle-$GRADLE_VERSION/bin/gradle" wrapper --gradle-version $GRADLE_VERSION

echo "5. Cleaning up..."
rm -rf $TEMP_DIR

echo "6. Verifying installation..."
./gradlew --version

echo "Success! Gradle Wrapper has been installed."
