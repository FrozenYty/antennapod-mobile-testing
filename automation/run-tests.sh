#!/bin/bash
# Run all instrumented tests on a connected device.
# Usage: ./automation/run-tests.sh [class-name]
#
# Examples:
#   ./automation/run-tests.sh                                    # run all tests
#   ./automation/run-tests.sh TC001_AppLaunchTest                # run a single class
#   ./automation/run-tests.sh "de.danoeh.antennapod.espresso.*"  # run all espresso tests

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
APP_DIR="$PROJECT_DIR/app-under-test/antennapod"

if [ ! -d "$APP_DIR" ]; then
    echo "Error: app-under-test/antennapod not found at $APP_DIR"
    exit 1
fi

cd "$APP_DIR"

# Disable animations
echo "Disabling animations..."
adb shell settings put global window_animation_scale 0.0 2>/dev/null || true
adb shell settings put global transition_animation_scale 0.0 2>/dev/null || true
adb shell settings put global animator_duration_scale 0.0 2>/dev/null || true

# Install the app
echo "Installing app..."
chmod +x gradlew
./gradlew :app:installPlayDebug --quiet

# Run tests
CLASS_ARG="${1:-}"
if [ -n "$CLASS_ARG" ]; then
    echo "Running: $CLASS_ARG"
    ./gradlew :app:connectedPlayDebugAndroidTest \
        -Pandroid.testInstrumentationRunnerArguments.class="$CLASS_ARG"
else
    echo "Running all instrumented tests..."
    ./gradlew :app:connectedPlayDebugAndroidTest
fi

echo "Done."
