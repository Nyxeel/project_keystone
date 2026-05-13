# Phase 0 — Sofortige Stabilitätsfixes vor Marker-v2

MODUS:
Enger Safety-Fix-Plan für NPCMod / KeystoneNPC.

ZIEL:
Phase 0 schließt nur die kleinen offenen P1-Failchecks vor Marker-v2.

Phase 0 darf NICHT:
- Marker-v2 implementieren
- neue markerAssignments-Hauptarchitektur bauen
- NpcRoutineRunner groß refactoren
- Door/Navigation umbauen
- Respawn-Policy neu designen
- Role-System ändern
- Legacy-Felder löschen
- alte erledigte Fehler erneut öffnen

GRUNDREGEL:
Ein Step = ein kleines Problem.
Nach jedem Step = Review.
Bei FAIL = nur diesen Step fixen.
Erst nach PASS = nächster Step.

QUELLEN-PRIORITÄT:
1. aktueller Codezustand
2. neueste Patchreports
3. aktuelle docs/safety-Dateien
4. NPCMod_Lagebericht
5. ältere TODOs / alte Prompts

ANTI-KREIS-REGEL:
Vor jeder Änderung prüfen:
- Existiert der Fehler im aktuellen Code wirklich noch?
- Wurde er laut Patchreport bereits gefixt?
- Ist es echter Codefehler oder nur alte Doku?
- Wenn bereits erledigt: NOOP melden, keine Datei ändern.

PFLICHTQUELLEN:
- /home/pj/projects/hytale/project_keystone/NPCMod/AGENTS.md
- /home/pj/projects/hytale/project_keystone/NPCMod/docs/safety/npc_restart_relink_control.md
- /home/pj/projects/hytale/project_keystone/NPCMod/docs/safety/json_hierarchy.md

COMPILE-GATE:
Nach jedem Java-/Ressourcen-Step:

mvn -q -DskipTests test-compile

Markdown-only Steps brauchen kein Maven-Compile, aber eine Doku-Konsistenzprüfung.

============================================================
A) Kurzurteil zu Phase 0
============================================================

Phase 0 ist geeignet und notwendig.

Echte offene P1-Punkte:

1. NpcRespawnMissingCommand prüft saveStateSafely() noch nicht hart genug.
2. Marker-Zuweisung braucht ein hartes markerId/type/worldId-Gate.
3. SpawnNpcCommand soll bei Save-Failure detailed RemoveResult statt boolean removeNpc(...) auswerten.
4. Safety-Doku darf keine alten Marker-Fallback-Methoden mehr als erlaubten Mutationspfad darstellen.

Kreisarbeitsgefahr:
Mittel, wenn alte Marker-Fallback-Prompts wiederverwendet werden.
Niedrig, wenn jeder Step vorher Codezustand prüft und bei erledigten Punkten NOOP meldet.

Marker-v2:
Darf in Phase 0 nicht implementiert werden.
Nach PASS aller Phase-0-Steps darf Marker-v2 im PLAN Mode gestartet werden.

============================================================
B) Widerspruchs- und Logic-Error-Analyse
============================================================

1. Problem:
NpcRespawnMissingCommand kann Save-Failure übersehen.

Risiko:
Runtime ändert sich, state.json speichert nicht, Command meldet trotzdem Erfolg.

Status:
Offen / Codeprüfung erforderlich.

Empfehlung:
saveStateSafely() Ergebnis prüfen.
Bei false keine normale Erfolgsmeldung.

------------------------------------------------------------

2. Problem:
Marker-Zuweisung prüft markerId/type/worldId nicht hart genug.

Risiko:
NPC kann Marker aus falscher Welt bekommen.
Marker-v2 würde diesen Fehler später nur sauberer speichern, aber nicht lösen.

Status:
Offen / Codeprüfung erforderlich.

Empfehlung:
assignMarkerToNpc(...) muss prüfen:
- Marker existiert in MarkerRegistry
- MarkerType passt
- marker.worldId == npc.worldId
- MarkerType ist für roleId erlaubt

------------------------------------------------------------

3. Problem:
SpawnNpcCommand nutzt nach Save-Failure noch boolean removeNpc(...).

Risiko:
Command kann nicht unterscheiden:
- Rollback wirklich fertig
- Removal blockiert
- Save fehlgeschlagen
- Entity-Removal unsicher

Status:
Offen / Codeprüfung erforderlich.

Empfehlung:
Detailed RemoveNpcResult auswerten und ehrlich melden.

------------------------------------------------------------

4. Problem:
Safety-Doku kann Versionsdrift haben.

Risiko:
Spätere AI reaktiviert entfernte Marker-Fallbacks.

Status:
Doku-Drift möglich.

Empfehlung:
Safety-Doku muss einheitlich sagen:
- resolveRequiredMarkerWithFallbackAssigning(...) entfernt
- resolveRequiredMarkerWithFallback(...) entfernt
- resolveRequiredMarkerReadOnly(...) verbindlich
- getNextAvailable(...) deprecated und nicht in read-only Pfaden

============================================================
C) Offene Punkte nach Priorität
============================================================

| Priorität | Block | Thema | Status | Warum wichtig | Nächster Schritt |
|---|---|---|---|---|---|
| P1 | 8 + 3 + 4 | NpcRespawnMissingCommand Save-Failure | Codeprüfung nötig | verhindert Runtime/state.json-Drift | Step 0.1 |
| P1 | 7 + 3 + 8 | Marker worldId/type/id Gate | Codeprüfung nötig | verhindert falsche Markerbindung | Step 0.2 |
| P1 | 4 + 8 | SpawnNpcCommand Rollback-Ergebnis | Codeprüfung nötig | verhindert falsche Rollback-Erfolgsmeldung | Step 0.3 |
| P1/P2 | 9 | Safety-Doku Versionsdrift | Dokuprüfung nötig | verhindert Reaktivierung alter Fallbacks | Step 0.4 |
| P2 | 7 | Marker-v2 Plan Mode | erst nach Phase 0 | neues Feature braucht stabile Basis | nach Phase 0 |

============================================================
D) Phasenplan
============================================================

## Phase 0 — Safety-Fixes vor Marker-v2

Ziel:
Kleine P1-Failchecks schließen.

Startbedingung:
Aktueller Code und Safety-Dokumente liegen vor.

Nicht ändern:
Marker-v2, Navigation, Door, Role-System, große Lifecycle-Refactors.

Ende-Bedingung:
- Step 0.1 PASS
- Step 0.2 PASS
- Step 0.3 PASS
- Step 0.4 PASS
- Compile grün bei Java-Steps
- Patchreport geschrieben
- Safety-Doku geprüft/aktualisiert

Review-Gate:
Nach jedem Step eigener Review.

------------------------------------------------------------

## Phase 1 — Marker-v2 PLAN Mode

Startbedingung:
Phase 0 vollständig PASS.

Ziel:
Marker-v2 nur planen.

Nicht ändern:
Keine Java-Umsetzung, keine Migration, keine Legacy-Löschung.

Ende-Bedingung:
Marker-v2-Plan enthält:
- Schema
- Migration
- Legacy-Kompatibilität
- Commands
- Resolver
- Remove/Clear Cleanup
- Tests
- Rollback

============================================================
E) Agent-Step-Liste
============================================================

# Agent Step 0.1 — NpcRespawnMissingCommand Save-Failure prüfen

MODUS:
Enger Safety-Fix.

KONTEXT:
Phase 0 vor Marker-v2. Commands dürfen keinen Erfolg melden, wenn Persistenz fehlschlägt.

BLOCK / PRIORITÄT:
Block 8 + 3 + 4 / P1.

ZIEL:
NpcRespawnMissingCommand prüft saveStateSafely() Ergebnis.

ERLAUBTE DATEIEN / BEREICHE:
- NpcRespawnMissingCommand.java
- nur falls absolut nötig: kleine Hilfsmethode für Command-Fehlermeldung

PFLICHTPRÜFUNG VOR ÄNDERUNG:
1. Suche die Stelle:
   result.stateChanged() && !dryRun
2. Prüfe, ob plugin.saveStateSafely() Ergebnis aktuell ignoriert wird.
3. Wenn Ergebnis bereits geprüft wird:
   NOOP melden, keine Datei ändern.
4. Wenn Ergebnis nicht geprüft wird:
   nur diesen Fehler fixen.

KONKRETE AUFGABEN:
- saveStateSafely() Rückgabe speichern.
- Wenn false:
  - keine normale Erfolgsmeldung
  - klare Fehlermeldung: Runtime geändert, state.json konnte nicht gespeichert werden
  - keine weiteren Recovery-/Respawn-Policy-Änderungen
- Wenn true:
  - bisherige Erfolgsmeldung bleibt erlaubt.

NICHT ÄNDERN:
- kein Marker-v2
- keine Respawn-Policy ändern
- kein Relink-Refactor
- kein Command-Redesign
- kein NpcRoutineRunner-Refactor
- keine Safety-Doku in diesem Step, außer Review fordert es ausdrücklich

SAFETY-REGELN:
- Save-Failure zählt nie als Erfolg.
- Dry-run darf nichts speichern.
- Keine Runtime/state.json-Drift verstecken.

COMPILE:
mvn -q -DskipTests test-compile

ABSCHLUSSBERICHT:
- Geänderte Dateien
- Ob Fehler noch existierte
- Was geändert wurde
- Wie Save-Failure jetzt gemeldet wird
- Ob Compile erfolgreich war
- Restgefahren

------------------------------------------------------------

# Review Step 0.1 — NpcRespawnMissingCommand Save-Failure

REVIEW MODUS:
Nur prüfen, nicht implementieren.

PRÜFE:
1. Wurde nur NpcRespawnMissingCommand geändert?
2. Wird saveStateSafely() Ergebnis geprüft?
3. Kann der Command nach Save-Failure noch normal Erfolg melden?
4. Bleibt dryRun read-only?
5. Wurde Respawn-Policy nicht verändert?
6. Wurde Marker-v2 nicht berührt?
7. Gibt es neue Logic Errors?
8. War mvn -q -DskipTests test-compile erfolgreich?

ENTSCHEIDUNG:
- PASS, wenn Save-Failure nicht mehr als Erfolg erscheinen kann.
- FAIL, wenn Save-Failure noch ignoriert wird.
- Nächster Step nur bei PASS erlaubt.

------------------------------------------------------------

# Fix Prompt Step 0.1 — falls FAIL

Fixe ausschließlich Review-Fails aus Step 0.1.

NICHT:
- keinen nächsten Step anfangen
- kein Marker-v2
- keine Respawn-Policy ändern
- kein Refactor

AUFGABE:
- saveStateSafely() Ergebnis korrekt prüfen
- bei false normale Erfolgsmeldung blockieren
- Fehlermeldung klar machen
- Compile erneut ausführen

Danach erneut Review Step 0.1.

============================================================

# Agent Step 0.2 — Marker-Zuweisung mit markerId/type/worldId-Gate härten

MODUS:
Enger Safety-Fix.

KONTEXT:
Vor Marker-v2 muss garantiert sein, dass ein NPC keinen Marker aus falscher Welt oder falschem Typ bekommt.

BLOCK / PRIORITÄT:
Block 7 + 3 + 8 / P1.

ZIEL:
assignMarkerToNpc(...) schreibt nur, wenn markerId, MarkerType und worldId sicher passen.

ERLAUBTE DATEIEN / BEREICHE:
- NpcRoutineRunner.java oder die Datei, in der assignMarkerToNpc(...) aktuell liegt
- MarkerRegistry.java nur falls reine Lookup-Methode fehlt
- MarkerSetCommand.java nur falls Command-Ergebnis angepasst werden muss

PFLICHTPRÜFUNG VOR ÄNDERUNG:
1. Suche assignMarkerToNpc(...).
2. Prüfe, ob markerId in MarkerRegistry validiert wird.
3. Prüfe, ob marker.type gegen gewünschten MarkerType geprüft wird.
4. Prüfe, ob marker.worldId gegen npc.worldId geprüft wird.
5. Wenn alles bereits vorhanden ist:
   NOOP melden, keine Datei ändern.
6. Wenn etwas fehlt:
   nur fehlendes Gate ergänzen.

KONKRETE AUFGABEN:
- MarkerRecord per markerId aus Registry lesen.
- Wenn markerId nicht existiert: blockieren.
- Wenn MarkerType nicht passt: blockieren.
- Wenn marker.worldId != npc.worldId: blockieren.
- Bestehende Prüfung „MarkerType ist für roleId erlaubt“ erhalten.
- No-Op weiterhin sauber behandeln.
- Keine kaputte MarkerId in NPC-State schreiben.
- Command muss Fehler ehrlich melden.

NICHT ÄNDERN:
- kein Marker-v2
- keine neue markerAssignments-Map
- keine Legacy-Felder löschen
- keine Reconcile-Änderung
- keine Marker-Migration
- keine Door-/Navigation-Änderung

SAFETY-REGELN:
- NPC darf keinen Marker aus anderer Welt bekommen.
- Read-only Pfade bleiben read-only.
- Keine automatische Marker-Reparatur.
- Kein getNextAvailable(...) als Fallback.

COMPILE:
mvn -q -DskipTests test-compile

ABSCHLUSSBERICHT:
- Geänderte Dateien
- Welche Gates ergänzt wurden
- Verhalten bei falscher markerId
- Verhalten bei falschem MarkerType
- Verhalten bei falscher worldId
- Ob Compile erfolgreich war
- Restgefahren

------------------------------------------------------------

# Review Step 0.2 — Marker worldId/type/id Gate

REVIEW MODUS:
Nur prüfen, nicht implementieren.

PRÜFE:
1. Wurde nur erlaubter Scope geändert?
2. Existiert ein hartes markerId-Existenz-Gate?
3. Existiert ein hartes MarkerType-Gate?
4. Existiert ein hartes worldId-Gate?
5. Bleibt roleId/MarkerType-Erlaubnisprüfung erhalten?
6. Kann ein NPC noch Marker aus anderer Welt bekommen?
7. Wurde Marker-v2 nicht eingebaut?
8. Wurden Legacy-Felder nicht gelöscht?
9. Wurde Reconcile nicht verändert?
10. War mvn -q -DskipTests test-compile erfolgreich?

ENTSCHEIDUNG:
- PASS, wenn markerId/type/worldId sicher blockieren.
- FAIL, wenn falsche Welt oder falscher Typ noch möglich ist.
- Nächster Step nur bei PASS erlaubt.

------------------------------------------------------------

# Fix Prompt Step 0.2 — falls FAIL

Fixe ausschließlich Review-Fails aus Step 0.2.

NICHT:
- kein Marker-v2
- keine markerAssignments-Hauptstruktur
- keine Legacy-Löschung
- keine Reconcile-Änderung

AUFGABE:
- fehlendes markerId/type/worldId-Gate ergänzen
- Fehler klar melden
- Compile erneut ausführen

Danach erneut Review Step 0.2.

============================================================

# Agent Step 0.3 — SpawnNpcCommand Save-Failure-Rollback detaillieren

MODUS:
Enger Safety-Fix.

KONTEXT:
Wenn Spawn erfolgreich war, aber state.json nicht gespeichert werden kann, muss Rollback ehrlich gemeldet werden.

BLOCK / PRIORITÄT:
Block 4 + 8 / P1.

ZIEL:
SpawnNpcCommand nutzt bei Save-Failure nicht nur boolean removeNpc(...), sondern wertet detailed RemoveNpcResult aus.

ERLAUBTE DATEIEN / BEREICHE:
- SpawnNpcCommand.java
- NpcRoutineRunner.java nur, falls vorhandener detailed Remove-Pfad nicht erreichbar ist
- keine neue Removal-Architektur

PFLICHTPRÜFUNG VOR ÄNDERUNG:
1. Suche Save-Failure-Pfad in SpawnNpcCommand.
2. Prüfe, ob scheduler.removeNpc(npc.npcId()) boolean genutzt wird.
3. Prüfe, ob removeNpcDetailed(...) oder RemoveNpcResult bereits existiert.
4. Wenn detailed Result bereits genutzt wird:
   NOOP melden, keine Datei ändern.
5. Wenn boolean genutzt wird:
   auf detailed Result umstellen.

KONKRETE AUFGABEN:
- Bei saveStateSafely() false:
  - detailed RemoveNpcResult abrufen
  - Ergebnis unterscheiden:
    - removed
    - blocked
    - unsafe entity removal
    - save failed
    - rollback failed
  - keine falsche Erfolgsmeldung
  - klare Admin-Meldung ausgeben
- Kein Entity-Removal-Redesign bauen.

NICHT ÄNDERN:
- kein Spawn-System umbauen
- kein Respawn-System umbauen
- kein Entity-Removal-Redesign
- kein Marker-v2
- keine Remove/Clear UX-Entscheidung
- kein Admin-Force-Pfad

SAFETY-REGELN:
- Rollback-Failure zählt nie als Erfolg.
- Unsicheres Entity-Removal darf nicht verschleiert werden.
- Kein Record löschen, wenn Entity-Removal unsicher ist.

COMPILE:
mvn -q -DskipTests test-compile

ABSCHLUSSBERICHT:
- Geänderte Dateien
- Ob boolean-Pfad existierte
- Welcher detailed Result-Pfad genutzt wird
- Welche Meldungen bei blocked/failed kommen
- Ob Compile erfolgreich war
- Restgefahren

------------------------------------------------------------

# Review Step 0.3 — Spawn rollback detailed Result

REVIEW MODUS:
Nur prüfen, nicht implementieren.

PRÜFE:
1. Wurde nur SpawnNpcCommand / notwendiger Minimal-Scope geändert?
2. Wird boolean removeNpc(...) im Save-Failure-Rollback nicht mehr blind genutzt?
3. Wird detailed RemoveNpcResult ausgewertet?
4. Kann Command noch behaupten, Rollback sei fertig, obwohl Remove blockiert war?
5. Wurde Entity-Removal-Design nicht umgebaut?
6. Wurde kein Admin-Force-Pfad eingebaut?
7. Wurde Marker-v2 nicht berührt?
8. War mvn -q -DskipTests test-compile erfolgreich?

ENTSCHEIDUNG:
- PASS, wenn Rollback-Ergebnis ehrlich gemeldet wird.
- FAIL, wenn boolean oder falsche Erfolgsmeldung bleibt.
- Nächster Step nur bei PASS erlaubt.

------------------------------------------------------------

# Fix Prompt Step 0.3 — falls FAIL

Fixe ausschließlich Review-Fails aus Step 0.3.

NICHT:
- kein Entity-Removal-Redesign
- kein Admin-Force-Pfad
- kein Marker-v2
- keinen nächsten Step anfangen

AUFGABE:
- detailed RemoveNpcResult korrekt auswerten
- falsche Erfolgsmeldung entfernen
- Compile erneut ausführen

Danach erneut Review Step 0.3.

============================================================

# Agent Step 0.4 — Safety-Doku Versionsdrift bereinigen

MODUS:
Markdown-only Safety-Doku-Sync.

KONTEXT:
Alte Doku darf entfernte Marker-Fallback-Methoden nicht mehr als erlaubten Mutationspfad darstellen.

BLOCK / PRIORITÄT:
Block 9 / P1/P2.

ZIEL:
Safety-Dokumente sagen einheitlich den aktuellen Marker-Resolver-Stand.

ERLAUBTE DATEIEN:
- /home/pj/projects/hytale/project_keystone/NPCMod/docs/safety/npc_restart_relink_control.md
- /home/pj/projects/hytale/project_keystone/NPCMod/docs/safety/json_hierarchy.md
- optional AGENTS.md nur, wenn dort direkter Widerspruch steht

PFLICHTPRÜFUNG VOR ÄNDERUNG:
1. Suche in docs/safety nach:
   - resolveRequiredMarkerWithFallbackAssigning
   - resolveRequiredMarkerWithFallback
   - getNextAvailable
   - MarkerRegistry.getNextAvailable
2. Prüfe, ob alte entfernte Methoden als erlaubter Mutationspfad beschrieben werden.
3. Wenn keine Doku-Drift existiert:
   NOOP melden, keine Datei ändern.
4. Wenn Doku-Drift existiert:
   nur Doku korrigieren.

KONKRETE AUFGABEN:
Safety-Doku muss sagen:

- resolveRequiredMarkerWithFallbackAssigning(...) ist entfernt.
- resolveRequiredMarkerWithFallback(...) ist entfernt.
- resolveRequiredMarkerReadOnly(...) ist verbindlich für read-only Pfade.
- Mutierende Marker-Zuweisung ist nur in expliziten Spawn/Admin/Repair/Cleanup-Kontexten erlaubt.
- getNextAvailable(...) ist deprecated und darf nicht in Restore/Tick/Diagnose/Relink/Respawn-Policy genutzt werden.
- Marker-v2 ist nicht Teil von Phase 0.

NICHT ÄNDERN:
- kein Java-Code
- kein Marker-v2
- keine neuen Regeln ohne Codebezug
- keine alten Safety-Regeln still löschen
- keine widersprüchliche Regel selbst entscheiden

DOKU-KONSISTENZPRÜFUNG:
- rg "resolveRequiredMarkerWithFallback" docs/safety
- rg "getNextAvailable" docs/safety
- prüfen, ob alle Treffer korrekt als entfernt/deprecated/No-Go beschrieben sind

ABSCHLUSSBERICHT:
- Geänderte Dateien
- Gefundene alte Formulierungen
- Neue Formulierungen
- Ob Java-Code unangetastet blieb
- Ob Doku-Konsistenzprüfung bestanden hat
- Ob Regelkonflikte gefunden wurden

------------------------------------------------------------

# Review Step 0.4 — Safety-Doku Sync

REVIEW MODUS:
Nur prüfen, nicht implementieren.

PRÜFE:
1. Wurde nur Markdown/Safety-Doku geändert?
2. Gibt es noch alte Formulierungen, die removed Methoden als erlaubten Mutationspfad nennen?
3. Wird getNextAvailable(...) klar als deprecated / nicht read-only-tauglich beschrieben?
4. Bleibt Marker-v2 klar als späteres Feature abgegrenzt?
5. Wurden keine neuen Code-Regeln ohne Codebezug erfunden?
6. Gibt es Widersprüche zwischen safety-Dateien?
7. Ist kein Java-Code geändert?

ENTSCHEIDUNG:
- PASS, wenn Safety-Doku konsistent ist.
- FAIL, wenn alte Fallback-Formulierungen bleiben.
- Nächster Step nur bei PASS erlaubt.

------------------------------------------------------------

# Fix Prompt Step 0.4 — falls FAIL

Fixe ausschließlich Review-Fails aus Step 0.4.

NICHT:
- kein Java-Code
- kein Marker-v2
- keine neue Architekturregel ohne Codebezug

AUFGABE:
- alte widersprüchliche Formulierungen korrigieren
- Doku-Konsistenzprüfung erneut durchführen
- danach wieder Review Step 0.4

============================================================

# Agent Step 0.5 — Finaler Phase-0-Abschluss: Safety-Doku und Patchreport

MODUS:
Finaler Doku-/Patchreport-Step.

STARTBEDINGUNG:
Nur starten, wenn Step 0.1 bis 0.4 jeweils PASS sind.

ZIEL:
Phase 0 sauber abschließen und dokumentieren.

ERLAUBTE DATEIEN:
- docs/safety/* nur falls durch Steps geändert oder bewusst geprüft
- /home/pj/projects/hytale/project_keystone/NPCMod/docs/patch_reports/YYYY-MM-DD_HH-MM_Phase-0-Stability-Fixes-Before-Marker-v2-Patch.md

KONKRETE AUFGABEN:
1. Prüfen, ob Safety-Dateien final konsistent sind.
2. Patchreport erstellen.
3. Patchreport speichern unter:

/home/pj/projects/hytale/project_keystone/NPCMod/docs/patch_reports

Namensformat:

YYYY-MM-DD_HH-MM_Phase-0-Stability-Fixes-Before-Marker-v2-Patch.md

Patchreport muss enthalten:
- Ziel von Phase 0
- geänderte Dateien pro Step
- PASS/FAIL pro Step
- Compile-Ergebnisse
- Safety-Dateien geprüft/aktualisiert
- Regelkonflikte ja/nein
- Marker-v2 nicht implementiert
- Marker-v2 Startfreigabe ja/nein
- Restgefahren
- Nächster erlaubter Step: Marker-v2 PLAN Mode

NICHT ÄNDERN:
- kein Java-Code
- kein Marker-v2
- keine neuen Features

DOKU-CHECK:
Markdown-only, kein Maven nötig.
Wenn Java in Step 0.1–0.3 geändert wurde, muss deren Compile-Ergebnis im Patchreport stehen.

ABSCHLUSSBERICHT:
- Patchreport-Pfad
- Safety-Doku-Status
- Marker-v2-Freigabe:
  - nur PLAN Mode erlaubt
  - Implementierung erst nach Marker-v2 Plan-Review

============================================================
F) Fortschritts-Checkliste
============================================================

[ ] Phase-0-Plan geprüft
[ ] Step 0.1 umgesetzt oder NOOP begründet
[ ] Step 0.1 reviewed
[ ] Step 0.1 final PASS
[ ] Step 0.2 umgesetzt oder NOOP begründet
[ ] Step 0.2 reviewed
[ ] Step 0.2 final PASS
[ ] Step 0.3 umgesetzt oder NOOP begründet
[ ] Step 0.3 reviewed
[ ] Step 0.3 final PASS
[ ] Step 0.4 umgesetzt oder NOOP begründet
[ ] Step 0.4 reviewed
[ ] Step 0.4 final PASS
[ ] Step 0.5 Patchreport geschrieben
[ ] Safety-Doku geprüft/aktualisiert
[ ] Marker-v2 nicht implementiert
[ ] Marker-v2 PLAN Mode freigegeben

============================================================
G) Marker-v2-Einordnung nach Phase 0
============================================================

Marker-v2 darf NICHT in Phase 0 implementiert werden.

Marker-v2 PLAN Mode darf erst starten, wenn:

[ ] NpcRespawnMissingCommand Save-Failure sicher behandelt
[ ] assignMarkerToNpc markerId/type/worldId prüft
[ ] SpawnNpcCommand Rollback-Ergebnis ehrlich meldet
[ ] Safety-Doku keine alten Fallback-Widersprüche enthält
[ ] Compile grün ist
[ ] Patchreport geschrieben ist

Marker-v2 Implementierung darf erst starten, wenn zusätzlich der Marker-v2 PLAN reviewed und PASS ist.

Legacy-Felder bleiben bis dahin erhalten:

- bedMarkerId
- workMarkerId
- doorMarkerId
- foodMarkerId
- chestMarkerId
- chillMarkerId

Verboten vor Marker-v2:

- Legacy-Felder löschen
- automatische Load-Migration
- markerAssignments als Hauptstruktur erzwingen
- getNextAvailable(...) als Fallback reaktivieren
- resolveRequiredMarkerWithFallback(...) zurückbringen