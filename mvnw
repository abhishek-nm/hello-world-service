#!/bin/sh
# ----------------------------------------------------------------------------
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements. See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership. The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License. You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied. See the License for the
# specific language governing permissions and limitations
# under the License.
# ----------------------------------------------------------------------------

# ----------------------------------------------------------------------------
# Apache Maven Wrapper startup batch script, version 3.2.0
#
# Required ENV vars:
# ------------------
# JAVA_HOME - location of a JDK home dir
#
# Optional ENV vars
# -----------------
# MAVEN_OPTS - parameters passed to the Java VM when running Maven
# MAVEN_SKIP_RC - flag to disable loading of mavenrc files
# ----------------------------------------------------------------------------

if [ -z "$MAVEN_SKIP_RC" ]; then
  if [ -f /usr/local/etc/mavenrc ]; then . /usr/local/etc/mavenrc; fi
  if [ -f /etc/mavenrc ]; then . /etc/mavenrc; fi
  if [ -f "$HOME/.mavenrc" ]; then . "$HOME/.mavenrc"; fi
fi

cygwin=false
darwin=false
mingw=false
case "$(uname)" in
  CYGWIN*) cygwin=true ;;
  MINGW*) mingw=true ;;
  Darwin*)
    darwin=true
    if [ -z "$JAVA_HOME" ]; then
      if [ -x "/usr/libexec/java_home" ]; then
        JAVA_HOME="$(/usr/libexec/java_home)"
        export JAVA_HOME
      else
        JAVA_HOME="/Library/Java/Home"
        export JAVA_HOME
      fi
    fi
    ;;
esac

if [ -z "$JAVA_HOME" ] && [ -r /etc/gentoo-release ]; then
  JAVA_HOME=$(java-config --jre-home)
fi

if $cygwin; then
  [ -n "$JAVA_HOME" ] && JAVA_HOME=$(cygpath --unix "$JAVA_HOME")
  [ -n "$CLASSPATH" ] && CLASSPATH=$(cygpath --path --unix "$CLASSPATH")
fi

if $mingw; then
  [ -n "$JAVA_HOME" ] && [ -d "$JAVA_HOME" ] && JAVA_HOME="$(cd "$JAVA_HOME" && pwd)"
fi

if [ -z "$JAVA_HOME" ]; then
  javaExecutable="$(which javac 2>/dev/null)"
  if [ -n "$javaExecutable" ]; then
    readLink=$(which readlink 2>/dev/null)
    if [ -n "$readLink" ]; then
      if $darwin; then
        javaHome="$(dirname "$javaExecutable")"
        javaExecutable="$(cd "$javaHome" && pwd -P)/javac"
      else
        javaExecutable="$(readlink -f "$javaExecutable")"
      fi
      javaHome="$(dirname "$javaExecutable")"
      JAVA_HOME="$(dirname "$javaHome")"
      export JAVA_HOME
    fi
  fi
fi

if [ -z "$JAVACMD" ]; then
  if [ -n "$JAVA_HOME" ]; then
    if [ -x "$JAVA_HOME/jre/sh/java" ]; then
      JAVACMD="$JAVA_HOME/jre/sh/java"
    else
      JAVACMD="$JAVA_HOME/bin/java"
    fi
  else
    JAVACMD="$(command -v java 2>/dev/null)"
  fi
fi

if [ ! -x "$JAVACMD" ]; then
  echo "Error: JAVA_HOME is not defined correctly or java is not on PATH." >&2
  echo "  We cannot execute $JAVACMD" >&2
  exit 1
fi

find_maven_basedir() {
  if [ -z "$1" ]; then
    echo "Path not specified to find_maven_basedir" >&2
    return 1
  fi
  basedir="$1"
  wdir="$1"
  while [ "$wdir" != '/' ]; do
    if [ -d "$wdir/.mvn" ]; then
      basedir=$wdir
      break
    fi
    wdir="$(cd "$wdir/.." 2>/dev/null && pwd)"
    [ -z "$wdir" ] && break
  done
  printf '%s' "$(cd "$basedir" 2>/dev/null && pwd)"
}

concat_lines() {
  if [ -f "$1" ]; then
    tr -s '\r\n' ' ' <"$1"
  fi
}

log() {
  [ "$MVNW_VERBOSE" = true ] && printf '%s\n' "$1"
}

trim() {
  printf "%s" "${1}" | tr -d '[:space:]'
}

BASE_DIR=$(find_maven_basedir "$(dirname "$0")")
[ -z "$BASE_DIR" ] && exit 1

MAVEN_PROJECTBASEDIR="${MAVEN_BASEDIR:-$BASE_DIR}"
export MAVEN_PROJECTBASEDIR
log "$MAVEN_PROJECTBASEDIR"

wrapperJarPath="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.jar"
if [ ! -r "$wrapperJarPath" ]; then
  log "Couldn't find $wrapperJarPath, downloading it ..."
  wrapperUrl="https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar"
  while IFS="=" read -r key value; do
    case "$key" in wrapperUrl) wrapperUrl=$(trim "${value}"); break ;; esac
  done <"$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.properties"
  log "Downloading from: $wrapperUrl"
  if command -v curl >/dev/null; then
    curl -sL -o "$wrapperJarPath" "$wrapperUrl" -f || rm -f "$wrapperJarPath"
  elif command -v wget >/dev/null; then
    wget -q -O "$wrapperJarPath" "$wrapperUrl" || rm -f "$wrapperJarPath"
  else
    echo "Need curl or wget to download Maven wrapper jar." >&2
    exit 1
  fi
fi

MAVEN_OPTS="$(concat_lines "$MAVEN_PROJECTBASEDIR/.mvn/jvm.config") $MAVEN_OPTS"
MAVEN_CMD_LINE_ARGS="$MAVEN_CONFIG $*"
export MAVEN_CMD_LINE_ARGS

WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain
exec "$JAVACMD" $MAVEN_OPTS $MAVEN_DEBUG_OPTS \
  -classpath "$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.jar" \
  "-Dmaven.multiModuleProjectDirectory=${MAVEN_PROJECTBASEDIR}" \
  ${WRAPPER_LAUNCHER} $MAVEN_CONFIG "$@"
