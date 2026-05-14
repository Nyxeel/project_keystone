# NPCMod / KeystoneNPC — Inhaltlicher Auswertungs- und Logic-Check-Prompt

Du bekommst gleich eine technische Grundlage zu meinem Projekt NPCMod / KeystoneNPC.

Diese Grundlage kann sein:

- ein Lagebericht
- Patchreports
- eine Phase wie „Phase 0“
- Review-Ergebnisse
- alte TODOs
- Safety-Dokumente
- Code-Audit-Ergebnisse
- eine geplante Feature-Phase wie Marker-v2
- ein bestehender Plan

DEINE AUFGABE:
Erstelle daraus eine sichere technische Auswertung.

WICHTIG:
Du sollst NICHT implementieren.
Du sollst NICHT selbst Dateien ändern.
Du sollst NICHT blind alte Reports übernehmen.

Du sollst logisch prüfen:

- Was ist wirklich noch offen?
- Was wurde laut Patchreports schon erledigt?
- Wo gibt es Widersprüche?
- Wo droht Kreisarbeit?
- Wo könnte ein Fix neue Logic Errors erzeugen?
- Welche Reihenfolge ist sicher?
- Welche Punkte sind echte offene Fixes?
- Welche Punkte sind nur Doku-Drift?
- Welche Punkte müssen NOOP / bereits erledigt sein?

────────────────────────────
1. QUELLEN-PRIORITÄT
────────────────────────────

Wenn mehrere Informationen widersprüchlich wirken, gilt diese Reihenfolge:

1. aktueller Codezustand im Repository
2. neueste Patchreports
3. aktuelle Dateien in /home/pj/projects/hytale/project_keystone/NPCMod/docs/safety
4. NPCMod-Lagebericht
5. ältere Auswertungen / alte TODOs / alte Prompts

Wenn ältere Auswertungen einem neueren Patchreport oder aktuellem Code widersprechen:

- nicht blind übernehmen
- Widerspruch klar melden
- keinen Fix planen, der schon erledigt ist
- Step als „NOOP / bereits erledigt“ markieren, wenn der Fehler im aktuellen Code nicht mehr existiert

Wenn aktueller Code nicht geprüft werden kann:

- klar sagen: „Codezustand nicht verifiziert“
- keine endgültige Behauptung machen, dass etwas offen oder erledigt ist
- den Step als „Codeprüfung erforderlich“ markieren

────────────────────────────
2. ANTI-KREIS-REGEL
────────────────────────────

Vor jedem empfohlenen Fix-Step muss geprüft werden:

1. Ist der Fehler im aktuellen Code wirklich noch vorhanden?
2. Wurde er laut neuestem Patchreport bereits gefixt?
3. Ist es ein echter Codefehler oder nur alte Doku?
4. Ist es eine Regression oder ein alter erledigter Punkt?
5. Bringt der Step echten Fortschritt?

Wenn nein:

- keine Umsetzung empfehlen
- als „NOOP / bereits erledigt / nur Doku-Drift“ markieren
- kurz begründen

Ziel:

- Nicht im Kreis fixen.
- Nicht alte Fehler wieder öffnen.
- Nicht große Safety-Runden ohne konkreten offenen Fehler erzeugen.
- Nicht durch alte Reports neue Verwirrung erzeugen.

Wenn ein Fix einen neuen Fehler erzeugt, wird dieser NICHT sofort als neuer großer Step verfolgt.

Stattdessen:

1. Prüfen, ob der neue Fehler direkte Nebenwirkung des aktuellen Steps ist.
2. Wenn ja: im selben Step fixen.
3. Wenn nein: in Backlog aufnehmen.
4. Aktuellen Step erst abschließen, bevor ein neuer Themenbereich beginnt.

────────────────────────────
3. SAFETY-GRUNDREGELN
────────────────────────────

Diese Regeln gelten immer:

- Wenn unklar: nicht löschen, nicht spawnen, nicht relinken, nicht überschreiben.
- Keine Ghost-Spawns.
- Keine Orphans.
- Keine falschen Relinks.
- Keine kaputten JSON-/Persistenz-Zustände.
- Kein Save-Failure als Erfolg.
- Kein Load-Failure als leerer gültiger State.
- Kein Auto-Respawn ohne Policy-, Chunk- und Position-Gate.
- Kein Marker-Ersatz in read-only Pfaden.
- Kein dynamisches setRoleName("KeystoneNPC_...").
- Kein Role-Prefix-Fallback.
- Kein blindes Relinken per gleicher Role.
- Kein Auto-Respawn bei AMBIGUOUS.
- Keine Dedupe-Löschung nur wegen gleicher Role.
- Kein Runtime-Fallback darf still als persistente Wahrheit gespeichert werden.
- Keine state.json überschreiben, wenn Load unsicher, kaputt oder partial ist.
- Keine Records löschen, wenn Entity-Removal unsicher ist.
- Keine großen Refactors in Safety-Fixes.
- Keine angrenzenden Features nebenbei.
- Kein Marker-v2 nebenbei in Phase 0.
- Keine Door-/Navigation-/Animation-Änderungen, außer der Step betrifft genau das.
- Keine JSON-Roles ändern, außer der Step verlangt es ausdrücklich.
- Keine Safety-Dateien außerhalb des finalen Doku-Steps ändern, außer der User fordert es ausdrücklich als eigenen Step.
- Keine Regelkonflikte still entscheiden.

Pflichtquellen bei späterer Umsetzung:

/home/pj/projects/hytale/project_keystone/NPCMod/docs/safety/npc_restart_relink_control.md
/home/pj/projects/hytale/project_keystone/NPCMod/docs/safety/json_hierarchy.md
/home/pj/projects/hytale/project_keystone/NPCMod/AGENTS.md

Wenn AGENTS.md und safety/*.md widersprechen:

- nicht selbst entscheiden
- keinen Code ändern
- REGELKONFLIKT melden
- betroffene Regeln nennen
- Risiko erklären
- sichere Empfehlung geben
- Nutzerentscheidung verlangen

────────────────────────────
4. LOGIC-ERROR-CHECK
────────────────────────────

Prüfe jeden Punkt auf diese Fehlerarten:

Persistence / state.json:

- Wird entityRef gespeichert?
- Wird Runtime-Navigation gespeichert?
- Wird Door-/Action-Runtime gespeichert?
- Wird Save-Failure als Erfolg behandelt?
- Wird Dirty nach Save-Failure gelöscht?
- Wird Partial-Load automatisch überschrieben?
- Wird currentPosition ohne worldId als sichere Wahrheit benutzt?

Lifecycle / Relink / Respawn:

- Kann ein zweiter NPC für denselben Record entstehen?
- Wird MISSING_ENTITY ungewollt zu NEEDS_RELINK?
- Wird AMBIGUOUS ignoriert?
- Wird Anchor-Fallback zu früh genutzt?
- Wird Auto-Respawn vor Relink oder Chunk-Gate gestartet?
- Wird ACTIVE ohne UUID-/Ownership-Prüfung gesetzt?
- Wird eine UUID-lose Entity ACTIVE?
- Bleibt DISABLED wirklich DISABLED?
- Blockiert respawnAfterRestart false/missing Auto-Respawn?
- Prüft Chunk-Gate dieselbe Position, die Spawn nutzt?

Marker:

- Mutiert ein read-only Resolve?
- Wird ein Marker beim Restart automatisch ersetzt?
- Wird ein Marker aus falscher worldId zugewiesen?
- Wird MarkerType nicht geprüft?
- Werden Marker bei MISSING_ENTITY/NEEDS_RELINK gelöscht?
- Werden Marker bei echtem Delete nicht gelöscht?
- Wird getNextAvailable(...) wieder als Restore-/Tick-Fallback genutzt?
- Löscht oder ersetzt Reconcile MarkerAssignments in Restore/Tick/Diagnose?

Commands:

- Meldet ein Command Erfolg trotz Save-Failure?
- Meldet ein Command Erfolg trotz Rollback-Failure?
- Löscht /remove oder /clear Record, obwohl Entity-Removal unsicher ist?
- Verändert Dry-run Welt oder State?
- Fehlt --force bei gefährlichen Aktionen?
- Entfernt /remove oder /clear Entity, aber Record bleibt unsicher erhalten?
- Löscht /remove oder /clear Record, aber Live-Entity bleibt ungeklärt?

Engine / Hytale:

- Wird roleName für Keystone-Identität missbraucht?
- Wird eigene Fake-Pathfinding-Logik als Hauptsystem gebaut?
- Wird lineare Transform-Bewegung als Hauptnavigation genutzt?
- Wird Door-State direkt gehackt, obwohl InteractionChain möglich wäre?
- Wird Hytale-API-Verhalten geraten statt geprüft?
- Tick/Scheduler übernimmt Recovery-, Save- oder Lifecycle-Logik zu stark?

────────────────────────────
5. PHASEN-LOGIK
────────────────────────────

Erzeuge keine chaotische Alles-auf-einmal-Liste.

Ordne die Arbeit in Phasen ein:

Phase 0:
Sofortige Safety-Fixes vor Marker-v2.
Nur kleine P1-Failchecks.
Kein Marker-v2.
Keine großen Refactors.

Phase 1:
Marker-v2 PLAN Mode.
Nur planen, nicht implementieren.
Schema, Migration, Legacy-Kompatibilität, Commands, Tests, Rollback.

Phase 2:
Marker-v2 technische Vorbereitung.
Kleine Schritte.
Legacy-Felder bleiben kompatibel.
Read-only bleibt read-only.
Keine automatische Migration beim Load.

Phase 3:
Marker-v2 Umsetzung in kleinen Steps.
Commands, Resolver, Persistence, Cleanup getrennt.

Phase 4:
Legacy-Abbau erst später.
Nichts hart löschen, bevor Migration und Tests stabil sind.

Phase 5:
NpcRoutineRunner später entlasten.
Nicht mit Marker-v2 oder Safety-Fixes mischen.

Phase 6:
Navigation / Doorway / Hytale-Engine-Cleanup später.
Nicht mit state.json-/Marker-Fixes mischen.

────────────────────────────
6. PRIORITÄT
────────────────────────────

Priorisiere nach Risiko und nach kritischen Blöcken aus dem NPCMod-Lagebericht.

P1 zuerst:

- Block 4: NPC-Lebenszyklus
- Block 3: Persistence / state.json
- Block 7: Marker-System
- Block 8: Commands
- Block 5: Scheduler / Tick
- Save-Failure / Rollback-Failure
- Marker worldId/type/id
- Commands mit falscher Erfolgsmeldung
- Relink / Respawn / Lifecycle
- Safety-Doku-Widersprüche, wenn sie Agenten falsch steuern könnten

P2 später:

- Block 6: Navigation / Doorway
- Block 2: Loader / JSON-Hierarchie, falls nicht akut kaputt
- ServerId / SaveId Namespace
- NpcRoutineRunner aufteilen
- Role-Prefix-Stubs entfernen/deutlicher markieren
- Marker-v2 technische Vorbereitung
- Engine-API-Cleanup
- Design-Verbesserungen ohne akuten Safety-Fehler

P3 später:

- Navigation schöner machen
- Doorway verbessern
- Animation / Appearance / Combat / Drops / Faction
- große Engine-API-Optimierung

────────────────────────────
7. PFLICHT-REIHENFOLGE AUS NPCMOD-LAGEBERICHT
────────────────────────────

Wenn die Auswertung keine neueren, eindeutig wichtigeren Fehler zeigt, müssen die ersten Steps aus dem NPCMod-Lagebericht übernommen werden:

1. NpcRespawnMissingCommand Save-Failure prüfen
2. Marker worldId/type/id Gate härten
3. SpawnNpcCommand Rollback auf detailed RemoveResult umstellen
4. Safety-Doku Sync / Versionsdrift bereinigen
5. Marker-v2 nur PLAN Mode

Diese Reihenfolge darf nur geändert werden, wenn der aktuelle Code oder ein neuerer Patchreport klar zeigt, dass ein Step bereits erledigt oder nicht mehr relevant ist.

Wenn ein Step bereits erledigt ist:

- Step als „NOOP / bereits erledigt“ markieren
- kurz begründen
- keinen Ersatz-Fix erfinden
- trotzdem Review-Hinweis ausgeben

────────────────────────────
8. SPEZIALREGEL FÜR PATCHREPORTS
────────────────────────────

Wenn du Patchreports auswertest:

- Erstelle zuerst eine Timeline.
- Sortiere von alt nach neu.
- Markiere, welche Fehler laut neueren Reports erledigt sind.
- Markiere, welche Fehler noch offen sind.
- Markiere, welche Reports veraltet wirken.
- Übernimm keine alten Fix-Prompts blind.
- Prüfe, ob neue Patchreports alte Regeln ersetzt haben.

Ausgabe:

| Zeit | Patchreport | Aussage | Status heute | Risiko |
|---|---|---|---|---|

────────────────────────────
9. SPEZIALREGEL FÜR PLAN-DURCHFÜHRUNG
────────────────────────────

Wenn die Grundlage bereits ein Plan ist:

- Prüfe zuerst, ob die Reihenfolge logisch ist.
- Prüfe, ob Steps zu groß sind.
- Prüfe, ob ein Step mehrere Systeme mischt.
- Prüfe, ob ein Step Marker-v2 zu früh einbaut.
- Prüfe, ob Review-Gates fehlen.
- Prüfe, ob Fix-Prompts zu breit sind.
- Prüfe, ob der Plan im Kreis arbeitet.

Dann verbessere den Plan.

Nicht direkt implementieren.

────────────────────────────
10. SPEZIALREGEL FÜR PHASE 0
────────────────────────────

Wenn die Grundlage Phase 0 ist:

Phase 0 darf nur Safety-Fixes enthalten.

Erlaubt:

- Save-Failure prüfen
- Rollback-Ergebnis prüfen
- Marker worldId/type/id prüfen
- Command-Erfolgsmeldungen härten
- Safety-Doku-Versionen angleichen

Nicht erlaubt:

- Marker-v2 implementieren
- neue markerAssignments-Hauptarchitektur bauen
- NpcRoutineRunner groß refactoren
- Door/Navigation umbauen
- Respawn-Policy neu designen
- Role-System ändern
- Legacy-Felder löschen

Phase 0 ist fertig, wenn:

[ ] offene P1-Failchecks geschlossen sind
[ ] Reviews PASS sind
[ ] Compile grün ist
[ ] Safety-Doku geprüft/aktualisiert ist
[ ] Patchreport geschrieben ist
[ ] Marker-v2 nur geplant, aber nicht implementiert wurde

────────────────────────────
11. SPEZIALREGEL FÜR MARKER-v2
────────────────────────────

Marker-v2 darf erst nach Phase 0 starten.

Vor Marker-v2 müssen erfüllt sein:

[ ] saveStateSafely() wird in relevanten Commands geprüft
[ ] Marker-Zuweisung prüft markerId, markerType, worldId
[ ] Spawn Save-Failure Rollback meldet detailed Result
[ ] Safety-Doku widerspricht nicht mehr
[ ] Read-only Resolver mutiert nicht
[ ] Keine removed Fallback-Methoden werden wieder genutzt
[ ] Marker bei MISSING_ENTITY/NEEDS_RELINK bleiben erhalten
[ ] Marker bei echtem Delete werden zentral und sicher gelöscht

Marker-v2 Grundregeln:

- requiredMarkers kommen aus NPC-Definition.
- markerRoles mappen logischen Namen auf MarkerType.
- markerAssignments gehören zur konkreten NPC-Instanz.
- markerAssignments dürfen beim Load nicht automatisch migriert und gespeichert werden.
- Migration nur explizit, mit Dry-run, Backup und Save-Failcheck.
- Legacy-Felder bleiben zuerst kompatibel.
- Keine harte Löschung alter Felder im ersten Marker-v2-Step.
- Marker-v2 darf nur als spätere Phase eingeordnet werden.
- Marker-v2 darf nicht nebenbei in Phase 0 implementiert werden.

────────────────────────────
12. AUSGABEFORMAT
────────────────────────────

Gib das Ergebnis genau so aus:

A) Kurzurteil

- Ist die Grundlage geeignet?
- Gibt es Widersprüche?
- Gibt es Kreisarbeitsgefahr?
- Was ist wirklich der nächste sinnvolle Schritt?

B) Widerspruchs- und Logic-Error-Analyse

Für jeden gefundenen Punkt:

- Problem
- Quelle / Hinweis
- Risiko
- Status:
  - offen
  - erledigt
  - unklar
  - nur Doku-Drift
  - Codeprüfung nötig
- Empfehlung

C) Offene Punkte nach Priorität

Tabelle:

| Priorität | Block | Thema | Status | Warum wichtig | Nächster Schritt |
|---|---|---|---|---|

D) Phasenplan

Für jede Phase:

- Ziel
- Startbedingung
- Nicht ändern
- Ende-Bedingung
- Review-Gate

E) Kreisarbeits-Prüfung

1. Dreht sich der Plan im Kreis?
2. Welche Steps sind echte offene Fixes?
3. Welche Steps sind nur Doku-/Review-/Absicherungsarbeit?
4. Welche alten Punkte wirken bereits erledigt?
5. Welche Punkte dürfen nicht erneut gefixt werden, außer der aktuelle Code zeigt eine Regression?

F) Marker-v2-Einordnung

Beantworte klar:

- Darf Marker-v2 jetzt starten?
- Darf Marker-v2 nur geplant werden?
- Welche Phase-0-Steps müssen vorher PASS sein?
- Welche Legacy-Felder bleiben vorerst?
- Welche Fallbacks dürfen nicht zurückkommen?
- Was ist das Startsignal für Marker-v2 Implementierung?

G) Widerspruchs- und Versionsdrift-Prüfung

1. Gibt es echte Regelkonflikte?
2. Gibt es nur Versionsdrift zwischen alter Doku und neuem Code/Patchreport?
3. Welche Quelle wirkt aktueller?
4. Welche Punkte dürfen nicht umgesetzt werden, bevor der Konflikt geklärt ist?
5. Welche Punkte gehören in den finalen Safety-Doku-Sync-Step?

Wenn ein echter Konflikt existiert:

- nicht einfach auflösen
- REGELKONFLIKT GEFUNDEN ausgeben
- keine Umsetzung planen, die den Konflikt versteckt

H) Backlog

| Risiko | Name | Betroffene Datei(en) | Warum nicht jetzt fixen? | Späterer Step |
|---|---|---|---|---|
| P1/P2/P3 | ... | ... | ... | ... |

Wichtig:
Backlog-Punkte dürfen nicht nebenbei umgesetzt werden.

────────────────────────────
13. EINGABE
────────────────────────────

Hier ist die Grundlage:

<<AUSWERTUNG / PATCHREPORTS / PLAN / PHASE EINFÜGEN>>

────────────────────────────
14. ABSCHLUSSANWEISUNG
────────────────────────────

Bitte jetzt nicht implementieren.

Bitte nur sicher auswerten, logisch prüfen, Widersprüche markieren, Kreisarbeit verhindern und eine klare Phasen-/Prioritäts-Einschätzung erzeugen.

Keine Dateien ändern.
Keine Tests ausführen, die Dateien verändern.
Keine Safety-Dateien aktualisieren.
Keinen Patchreport schreiben.

Safety-Doku-Update und Patchreport-Erstellung nur als späteren finalen Agent-Step empfehlen, nicht jetzt ausführen.