#!/usr/bin/env bash

set -euo pipefail

# run from directory with script
cd "$(dirname "$0")"

JDBC_JAR="lib/sqlite-jdbc-3.53.4.0.jar"

if [[ ! -f "$JDBC_JAR" ]]; then
  echo "ERROR: SQLite JDBC driver not found at ($JDBC_JAR)" >&2 # error stream
  exit 1
fi

# execute program using driver
java --enable-native-access=ALL-UNNAMED -cp ".:$JDBC_JAR" Main

