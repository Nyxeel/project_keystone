# Patch Report: Final Safety Docs and Migration Policy

Date/Time: 2026-05-13 17:50
Type: Final documentation step
Scope: safety doc sync + explicit migration policy documentation

## Ziel
Finalen Doku-Step abschliessen:
- Widerspruchspruefung gegen Code + Safety + Reports
- Safety-Doku nur bei echter Regelaenderung aktualisieren
- Patchreport schreiben

## Gepruefte Quellen
- AGENTS.md
- docs/safety/npc_restart_relink_control.md
- docs/safety/json_hierarchy.md
- docs/NPCMod_Lagebericht.md
- neuere und aeltere Patchreports in docs/patch_reports/
- aktueller Codepfad fuer Marker-v2 Steps 2.1-2.5

## Konfliktpruefung
Ergebnis: Kein Regelkonflikt zwischen AGENTS.md, Safety-Dateien und aktuellem Code gefunden.

Hinweis zu historischen Quellen:
- Aeltere Reports/Lagebericht enthalten zeitbezogene Statusaussagen (z. B. Marker-v2 noch nicht gebaut).
- Diese wurden als historische Statusdrift eingeordnet, nicht als aktiver Safety-Regelkonflikt.

## Safety-Regelaenderung
Es gab eine echte Regelergänzung durch Step 2.5:
- Legacy->markerAssignments Migration darf nur explizit per Admin-Command passieren.
- Keine implizite Migration in Load/Restore/Diagnose/Tick.

Daher wurden Safety-Dateien bewusst aktualisiert.

## Geaenderte Dateien
1) docs/safety/npc_restart_relink_control.md
- Ergaenzt: explizite Migrations-Allowlist
  - /knpc marker migrate --dry-run
  - /knpc marker migrate --apply
- Ergaenzt: Pflicht-Gates
  - Backup vor Apply
  - Dry-run vor Apply
  - Block bei stateLoadFailed
  - Block bei stateLoadPartial
  - Save-Ergebnispruefung
  - bei Save-Failure: keine Erfolgsmeldung + Rollback/Drift-Warnung
  - keine Legacy-Feldloeschung im ersten Step
- Ergaenzt: explizit verbotene implizite Migration in Load/Restore/Diagnose/Tick

2) docs/safety/json_hierarchy.md
- Abschnitt 5.8 erweitert um denselben expliziten Migrationsrahmen
- Read-only-Kontext-Regeln um Migrationsverbote und Gate-Pflichten konkretisiert

## Unveraendert gelassen
- Keine stille Ueberschreibung alter Safety-Regeln
- Keine nachtraegliche Umschreibung alter Patchreports
- Keine Konfliktverschleierung

## Abschluss
- Startbedingung eingehalten: kein offener Regelkonflikt festgestellt.
- Safety-Doku wurde nur wegen echter Regelergaenzung aktualisiert.
- Finaler Patchreport wurde erstellt.
