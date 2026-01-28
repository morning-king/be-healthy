#!/bin/bash
set -e

# Define variables
GRADLE_VERSION="8.5"
GRADLE_ZIP="gradle-${GRADLE_VERSION}-bin.zip"
GRADLE_URL="https://services.gradle.org/distributions/${GRADLE_ZIP}"
TEMP_DIR="temp_gradle_install"
GEN_DIR="/tmp/behealthy_wrapper_gen_$(date +%s)"

echo "Cleaning up potential bad cache..."
rm -rf /Users/mac/.gradle/caches/jars-9/18366b31678c0171857be093a3b8ec22/
# Also kill any daemons
pkill -f java || true

echo "1. Creating temp directory..."
mkdir -p $TEMP_DIR
cd $TEMP_DIR

echo "2. Downloading Gradle ${GRADLE_VERSION}..."
if [ ! -f $GRADLE_ZIP ]; then
    curl -L -O $GRADLE_URL
fi

echo "3. Unzipping Gradle..."
unzip -q -o $GRADLE_ZIP

echo "4. Generating Gradle Wrapper in external dir..."
mkdir -p $GEN_DIR
touch $GEN_DIR/settings.gradle
"$PWD/gradle-$GRADLE_VERSION/bin/gradle" wrapper --gradle-version $GRADLE_VERSION --project-dir $GEN_DIR

echo "5. Copying wrapper files to project..."
cd ..
cp $GEN_DIR/gradlew .
cp $GEN_DIR/gradlew.bat .
mkdir -p gradle/wrapper
cp $GEN_DIR/gradle/wrapper/gradle-wrapper.jar gradle/wrapper/
cp $GEN_DIR/gradle/wrapper/gradle-wrapper.properties gradle/wrapper/

echo "6. Cleaning up..."
rm -rf $TEMP_DIR
rm -rf $GEN_DIR

echo "7. Setting permissions..."
chmod +x gradlew

echo "8. Verifying wrapper..."
./gradlew --version

echo "Success! Gradle Wrapper restored."
