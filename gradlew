#!/bin/sh
#
# Minimal Gradle launcher for GitHub Actions.
# Downloads/uses the configured Gradle distribution via the wrapper JAR.
#
APP_HOME=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
exec java -classpath "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain "$@"
