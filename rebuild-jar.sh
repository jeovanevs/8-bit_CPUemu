#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
JDK_DIR="$ROOT_DIR/zulu8.82.0.21-ca-fx-jdk8.0.432-linux_x64"

if [[ ! -x "$JDK_DIR/bin/java" ]]; then
    printf 'JDK 8 with JavaFX not found: %s\n' "$JDK_DIR" >&2
    exit 1
fi

export JAVA_HOME="$JDK_DIR"
export PATH="$JAVA_HOME/bin:$PATH"

cd "$ROOT_DIR"
ant -q jar
