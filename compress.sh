#!/usr/bin/env bash

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
FOLDER="$SCRIPT_DIR/NPCMod"
ZIP_FILE="$SCRIPT_DIR/NPCMod.zip"

if [ ! -d "$FOLDER" ]; then
  echo "Fehler: Ordner nicht gefunden: $FOLDER"
  exit 1
fi

cd "$SCRIPT_DIR"

zip -r "$ZIP_FILE" "NPCMod" -x "*.jar"

echo "Fertig: $ZIP_FILE"