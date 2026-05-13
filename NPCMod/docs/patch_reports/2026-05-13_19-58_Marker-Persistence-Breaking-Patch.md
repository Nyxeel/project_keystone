# Patch Report: Marker Persistence Breaking Change

Date/Time: 2026-05-13 19:58
Type: Final docs/patchreport step
Scope: Safety docs sync for Step 3.6-3.10 final architecture

## Ziel
Safety-Doku auf den bewusst eingeführten Breaking Change bringen:
- keine Legacy-Migration
- keine Legacy-Kompatibilität als aktive Persistenzquelle
- markerAssignments ist einzige persistente Marker-Wahrheit
- alte Legacy-state.json wird nicht migriert/repariert

## Widerspruchs-Check
Geprüft gegen:
- aktueller Code (NpcRecord, JsonPersistedModels, JsonFileStateStore, Remove/Clear-Pfade)
- neueste Patchreports
- AGENTS.md
- safety/*.md
- NPCMod_Lagebericht.md
- ältere Prompts/TODO-Quellen

Ergebnis:
- Kein echter Regelkonflikt zwischen AGENTS.md und safety/*.md.
- Es lag Versionsdrift zwischen älteren Safety-/Patchreport-Texten und aktuellem Code vor (Migration/Legacy-Kompatibilität war noch dokumentiert, aber im Code nicht mehr gültig).
- Drift wurde in diesem Schritt bereinigt.

## Geänderte Dateien
- docs/safety/npc_restart_relink_control.md
- docs/safety/json_hierarchy.md

## Wichtigste neue Regeln
1. Persistente Marker-Wahrheit ist ausschließlich markerAssignments.
2. Legacy-Markerfelder gelten als entfernt und nicht als kompatible Persistenzquelle.
3. Legacy-state.json mit alten Markerfeldern wird nur erkannt/geloggt und als partial-load behandelt.
4. Keine Migration, keine Auto-Reparatur, kein stilles Umschreiben alter state.json.

## Entfernte/ersetzte alte Regeln
- Regeln, die eine explizite Legacy->markerAssignments-Migration per /knpc marker migrate dokumentierten.
- Regeln, die Legacy-Kompatibilität als notwendiges Ziel formulierten.
- Formulierungen, die alte bed/work/food/chest/door/chill-Felder als persistente Hauptquelle führten.

## Sicherheitswirkung
- Keine stillen Persistenzmutationen in Load/Restore/Diagnose/Tick.
- Partial-load bleibt Save-Blocker (kein destruktives Auto-Overwrite).
- Breaking Change ist jetzt explizit und konsistent dokumentiert.

## Hinweis
Keine Java-Codeänderung in diesem finalen Doku-/Patchreport-Step.
