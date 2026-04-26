#!/usr/bin/env sh

DIRNAME="$(dirname "$0")"
APP_HOME="$DIRNAME"
APP_BASE_NAME=`basename "$0"`
JAVA_EXE=java
exec "$JAVA_EXE" -Dorg.gradle.appname="$APP_BASE_NAME" -classpath "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain "$@"
