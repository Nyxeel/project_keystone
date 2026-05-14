#!/usr/bin/env bash
set -euo pipefail

MOD_DIR="NPCMod"
OUT_ZIP="NPCMod.zip"

if [ ! -d "$MOD_DIR" ]; then
    echo "Fehler: Ordner '$MOD_DIR' nicht gefunden."
    exit 1
fi

rm -f "$OUT_ZIP"

zip -r "$OUT_ZIP" "$MOD_DIR" \
    -x "*.jar" \
    -x "*/target/*" \
    -x "*/.git/*" \
    -x "*/.idea/*" \
    -x "*/.vscode/*"

echo "Fertig: $OUT_ZIP wurde erstellt, ohne .jar-Dateien."