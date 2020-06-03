#!/bin/sh

# Use --debug to activate debug mode with an optional argument to specify the port.
# Usage : standalone.sh --debug
#         standalone.sh --debug 9797

# By default debug mode is disabled.
DEBUG_MODE="${DEBUG:-false}"
DEBUG_PORT="${DEBUG_PORT:-8787}"
GC_LOG="$GC_LOG"
SERVER_OPTS=""

# Display our environment
echo "========================================================================="
echo ""
echo "  JBoss Bootstrap Environment"
echo ""
echo "  JBOSS_HOME: $JBOSS_HOME"
echo ""
echo "  JAVA: $JAVA"
echo ""
echo "  JAVA_OPTS: $JAVA_OPTS"
echo ""
echo "  JAVA_TOOL_OPTIONS: $JAVA_TOOL_OPTIONS"
echo ""
echo "  _JAVA_OPTIONS: $_JAVA_OPTIONS"
echo ""
echo "  IBM_JAVA_OPTIONS: $IBM_JAVA_OPTIONS"
echo ""
echo "========================================================================="
echo ""

