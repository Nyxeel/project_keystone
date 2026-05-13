# Patch Report: Phase 0 Stability Fixes Before Marker-v2

Date/Time: 2026-05-13 16:43
Type: Final Phase-0 closure report
Scope: Commands, marker assignment gates, safety-doc consistency

## Ziel von Phase 0
Phase 0 schliesst kritische Safety-Luecken vor Marker-v2, ohne Marker-v2 selbst zu implementieren.
Schwerpunkte:
- Save-failure darf nie als Erfolg gemeldet werden.
- Marker-Zuweisung darf keine invaliden/fremdweltigen Marker persistieren.
- Spawn-Save-failure-Rollback muss ehrlich und detailliert ausgewertet werden.
- Safety-Dokumente muessen den aktuellen Marker-Resolver-Stand konsistent widerspiegeln.

## Step-Historie (1-4)

### Step 1 - NpcRespawnMissingCommand Save-Failure
Status: PASS
- Ergebnis: Save-failure-Handling im Respawn-Missing-Command als Safety-Ziel umgesetzt/abgesichert.
- Zielregel: Kein Erfolg bei saveStateSafely()==false.

Geaenderte Datei(en):
- src/main/java/keystone/npc/commands/admin/NpcRespawnMissingCommand.java

Compile:
- mvn -q -DskipTests test-compile -> PASS

### Step 2 - Marker worldId/type/id Gate
Status: PASS
- Ergebnis: assignMarkerToNpc(...) blockiert invalid markerId, type mismatch und world mismatch vor Persistenzmutation.

Geaenderte Datei(en):
- src/main/java/keystone/npc/routine/NpcRoutineRunner.java

Compile:
- mvn -q -DskipTests test-compile -> PASS

### Step 3 - Spawn Save-Failure Rollback mit detailed Result
Status: PASS
- Ergebnis: SpawnNpcCommand nutzt im Save-failure-Pfad detailed RemoveNpcResult statt blindem boolean removeNpc(...).
- Rollback-Auswertung trennt removed / blocked / unknown mit ehrlicher Admin-Meldung.

Geaenderte Datei(en):
- src/main/java/keystone/npc/commands/spawn/SpawnNpcCommand.java
- src/main/java/keystone/npc/routine/NpcRoutineRunner.java

Compile:
- mvn -q -DskipTests test-compile -> PASS

### Step 4 - Safety-Doku Versionsdrift
Status: PASS (NOOP)
- Ergebnis: Safety-Dokumente sind bereits konsistent zum Marker-Resolver-Stand.
- Kein Markdown-Edit erforderlich.

Geaenderte Datei(en):
- keine

Compile:
- nicht noetig (Markdown-only)

## Doku-Konsistenzpruefung (Step 4/5)
Pruefstatus:
- resolveRequiredMarkerWithFallbackAssigning(...): entfernt (nicht mehr aktiv)
- resolveRequiredMarkerWithFallback(...): entfernt (nicht mehr aktiv)
- resolveRequiredMarkerReadOnly(...): verbindlicher read-only Resolver
- MarkerRegistry.getNextAvailable(...): deprecated Lookup-Helfer, nicht fuer read-only Restore/Tick/Diagnose/Relink/Respawn-Policy
- Marker-Mutation nur in expliziten Spawn/Admin/Repair/Cleanup-Kontexten

## Safety-Dateien geprueft / geaendert
Geprueft:
- docs/safety/npc_restart_relink_control.md
- docs/safety/json_hierarchy.md
- AGENTS.md

Geaendert in Step 5:
- keine (nur finaler Patchreport)

## Regelkonflikte
- Regelkonflikte gefunden: nein
- Kein Konflikt wurde still entschieden.

## Marker-v2 Status
- Marker-v2 implementiert: nein
- Marker-v2 ist nicht Teil von Phase 0.
- Startfreigabe Marker-v2: nur PLAN Mode.

## Restgefahren
1. Bei Save-failure bleibt weiterhin Runtime/state.json drift risk moeglich; wird aber explizit gemeldet.
2. Rollback unknown/blocked-Faelle bleiben als Risiko transparent und werden nicht als Erfolg dargestellt.
3. Marker-v2-spezifische Architekturarbeit (z. B. markerAssignments-Hauptstruktur) ist bewusst verschoben.

## Backlog
| Risiko | Name | Betroffene Datei(en) | Warum nicht jetzt fixen? | Spaeterer Step |
|---|---|---|---|---|
| P2 | Marker-v2 Hauptstruktur | src/main/java/keystone/npc/** | Nicht Teil von Phase 0; bewusst abgegrenzt | Marker-v2 PLAN Mode |
| P2 | Erweiterte Rollback-Telemetrie | src/main/java/keystone/npc/commands/spawn/SpawnNpcCommand.java | Kein Feature-Mix in Safety-Phase | Nach Marker-v2 Planung |

## Abschluss
Phase 0 ist auf Stability-Fixes vor Marker-v2 fokussiert abgeschlossen und dokumentiert.
Naechster erlaubter Schritt: Marker-v2 ausschliesslich als PLAN-Mode-Planung mit separatem Review.
