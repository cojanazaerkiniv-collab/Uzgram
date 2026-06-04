#!/usr/bin/env sh

#
# Copyright 2015 the original author or authors.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

##############################################################################
##
##  Gradle start up script for UN*X
##
##############################################################################

# Attempt to set APP_HOME
# Resolve links: $0 may be a link
PRG="$0"
# Need this for relative symlinks.
while [ -h "$PRG" ] ; do
    ls -ld "$PRG"
    LINK=`ls -l "$PRG" | awk '{print $NF}'`
    case $LINK in
      /*) PRG="$LINK" ;;
      *) PRG=`dirname "$PRG"`"/$LINK" ;;
    esac
done
SAVED="`pwd`"
cd "`dirname \"$PRG\"`" >/dev/null
APP_HOME="`pwd -P`"
cd "$SAVED" >/dev/null

APP_NAME="Gradle"
APP_BASE_NAME=`basename "$0"`

# Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS to pass JVM options to this script.
DEFAULT_JVM_OPTS='" -Xmx64m" -Xms64m"'

# Use the maximum available, or set MAX_FD != maximum.
MAX_FD="maximum"

warn () {
    echo "$*" >&2
}

die () {
    echo
    echo "$*"
    echo
    exit 1
}

# OS specific support (must be 'true' or 'false').
darwin=false
msys=false
cygwin=false
freebsd=false
case "`uname`" in
  Darwin* )
    darwin=true
    ;;
  MINGW* )
    msys=true
    ;;
  CYGWIN* )
    cygwin=true
    ;;
  FreeBSD* )
    freebsd=true
    ;;
esac

# For Cygwin or MSYS, switch paths to Windows format before running java
if [ "$cygwin" = "true" ] -o [ "$msys" = "true" ] ; then
    APP_HOME=`(cd "$APP_HOME" && pwd -W)`
    CLASSPATH=`(cd "$CLASSPATH" && pwd -W)`
    # classpath might contain spaces
    CLASSPATH="\"$CLASSPATH\""
    JAVACMD=`(cd \"$(dirname \"$JAVACMD\")\" && pwd -W)`/java
fi

if [ ! -x "$JAVACMD" ] ; then
    die "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH."
fi

if [ -z "$JAVA_HOME" ] ; then
    warn "WARNING: JAVA_HOME environment variable is not set."
fi

JARVER=`$JAVACMD -version 2>&1 | grep "version" | awk '{print $3}' | awk -F. '{print $1}'`
if [ "$JARVER" -lt 7 ] ; then
    die "ERROR: Gradle 7.0+ requires Java 7 or higher. You are currently using Java $JARVER."
fi

# Increase the maximum file descriptors if we can.
if [ "$darwin" = "true" ] -o [ "$freebsd" = "true" ] ; then
    MAX_FD_LIMIT=`ulimit -H -n`
    if [ $? -eq 0 ] ; then
        if [ "$MAX_FD_LIMIT" != "unlimited" ] ; then
            MAX_FD=`expr $MAX_FD_LIMIT`
            if [ $? -eq 0 ] ; then
                ulimit -n $MAX_FD
            fi
        fi
    fi
fi

# For Darwin, add options to specify how the application appears in the dock
if [ "$darwin" = "true" ] ; then
    GRADLE_OPTS="\"$GRADLE_OPTS\" \"-Xdock:name=$APP_NAME\" \"-Xdock:icon=$APP_HOME/media/gradle.icns\""
fi

# For Cygwin or MSYS, switch paths to Windows format before running java
if [ "$cygwin" = "true" ] -o [ "$msys" = "true" ] ; then
    APP_HOME=`(cd "$APP_HOME" && pwd -W)`
    CLASSPATH=`(cd "$CLASSPATH" && pwd -W)`
    GRADLE_OPTS=`(cd "$GRADLE_OPTS" && pwd -W)`
fi

exec "$JAVACMD" $DEFAULT_JVM_OPTS $JAVA_OPTS $GRADLE_OPTS -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
