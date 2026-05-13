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

DEINE AUFGABE:
Erstelle daraus sichere AI-/Coder-Anweisungen für PLAN Mode, Agent Steps, Reviews und Fix-Prompts.

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

────────────────────────────
1. QUELLEN-PRIORITÄT
────────────────────────────

Wenn Quellen widersprüchlich sind, gilt diese Reihenfolge:

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

Vor jedem geplanten Fix-Step muss geprüft werden:

1. Ist der Fehler im aktuellen Code wirklich noch vorhanden?
2. Wurde er laut neuestem Patchreport bereits gefixt?
3. Ist es ein echter Codefehler oder nur alte Doku?
4. Ist es eine Regression oder ein alter erledigter Punkt?
5. Bringt der Step echten Fortschritt?

Wenn nein:

- keine Umsetzung planen
- als „NOOP / bereits erledigt / nur Doku-Drift“ markieren
- kurz begründen

Ziel:
Nicht im Kreis fixen.
Nicht alte Fehler wieder öffnen.
Nicht große Safety-Runden ohne konkreten offenen Fehler erzeugen.

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
- Keine Runtime-Daten als persistente Wahrheit speichern.
- Keine großen Refactors in Safety-Fixes.
- Keine angrenzenden Features nebenbei.
- Kein Marker-v2 nebenbei in Phase 0.

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

Prüfe jeden geplanten Step auf diese Fehlerarten:

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

Marker:
- Mutiert ein read-only Resolve?
- Wird ein Marker beim Restart automatisch ersetzt?
- Wird ein Marker aus falscher worldId zugewiesen?
- Wird MarkerType nicht geprüft?
- Werden Marker bei MISSING_ENTITY/NEEDS_RELINK gelöscht?
- Werden Marker bei echtem Delete nicht gelöscht?
- Wird getNextAvailable(...) wieder als Restore-/Tick-Fallback genutzt?

Commands:
- Meldet ein Command Erfolg trotz Save-Failure?
- Meldet ein Command Erfolg trotz Rollback-Failure?
- Löscht /remove oder /clear Record, obwohl Entity-Removal unsicher ist?
- Verändert Dry-run Welt oder State?
- Fehlt --force bei gefährlichen Aktionen?

Engine / Hytale:
- Wird roleName für Keystone-Identität missbraucht?
- Wird eigene Fake-Pathfinding-Logik als Hauptsystem gebaut?
- Wird lineare Transform-Bewegung als Hauptnavigation genutzt?
- Wird Door-State direkt gehackt, obwohl InteractionChain möglich wäre?
- Wird Hytale-API-Verhalten geraten statt geprüft?

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

Priorisiere nach Risiko:

P1 zuerst:
- Save-Failure / Rollback-Failure
- state.json / Persistence
- Marker worldId/type/id
- Commands mit falscher Erfolgsmeldung
- Relink / Respawn / Lifecycle
- Safety-Doku-Widersprüche, wenn sie Agenten falsch steuern könnten

P2 später:
- ServerId / SaveId Namespace
- NpcRoutineRunner aufteilen
- Role-Prefix-Stubs entfernen/deutlicher markieren
- Marker-v2 technische Vorbereitung

P3 später:
- Navigation schöner machen
- Doorway verbessern
- Animation / Appearance / Combat / Drops / Faction
- große Engine-API-Optimierung

────────────────────────────
7. AUSGABEFORMAT
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
|---|---|---|---|---|---|

D) Phasenplan

Für jede Phase:

- Ziel
- Startbedingung
- Nicht ändern
- Ende-Bedingung
- Review-Gate

E) Agent-Step-Liste

Für jeden Step:

# Agent Step X — Name

Enthalten muss sein:

- Modus
- kurzer Gesamtkontext
- Block / Priorität
- Ziel
- erlaubte Dateien / Bereiche
- Pflichtprüfung vor Änderung:
  - existiert der Fehler noch?
  - wurde er schon gefixt?
  - ist der Step noch nötig?
- konkrete Aufgaben
- was NICHT geändert werden darf
- Safety-Regeln
- Compile-/Test-Gate
- erwarteter Abschlussbericht

F) Review-Prompt je Step

Für jeden Step:

# Review Step X — Name

Der Review muss prüfen:

- Wurde nur der Scope geändert?
- Welche Dateien wurden geändert?
- Passen die Dateien zum Step?
- Gibt es neue Logic Errors?
- Gibt es neue Nebenwirkungen?
- Wurden Safety-Dateien beachtet?
- Wurde Compile/Test ausgeführt?
- Ist der nächste Step erlaubt?

Review darf NICHT implementieren.

G) Fix-Prompt je Step bei FAIL

Für jeden Step:

# Fix Prompt Step X — Name

Der Fix-Prompt muss sagen:

- Nur die Review-Fails dieses Steps beheben.
- Keinen nächsten Step anfangen.
- Keine neuen Features.
- Keine angrenzenden Refactors.


- Danach wieder Review.

H) Fortschritts-Checkliste

Beispiel:

[ ] Plan geprüft
[ ] Step 0.1 umgesetzt
[ ] Step 0.1 reviewed
[ ] Step 0.1 final PASS
[ ] Step 0.2 umgesetzt
[ ] Step 0.2 reviewed
[ ] Step 0.2 final PASS
...

I) Marker-v2-Einordnung

Beantworte klar:

- Darf Marker-v2 jetzt starten?
- Darf Marker-v2 nur geplant werden?
- Welche Phase-0-Steps müssen vorher PASS sein?
- Welche Legacy-Felder bleiben vorerst?
- Welche Fallbacks dürfen nicht zurückkommen?
- Was ist das Startsignal für Marker-v2 Implementierung?

J) Abschluss-Doku-Step

Plane als letzten Step immer:

Final Step — Safety-Doku und Patchreport

Dieser Step darf erst nach PASS aller vorherigen Steps laufen.

Aufgaben:

1. Prüfen, ob Safety-Dateien aktualisiert werden müssen:

/home/pj/projects/hytale/project_keystone/NPCMod/docs/safety

Besonders:

/home/pj/projects/hytale/project_keystone/NPCMod/docs/safety/npc_restart_relink_control.md
/home/pj/projects/hytale/project_keystone/NPCMod/docs/safety/json_hierarchy.md

2. Patchreport erstellen im Format:

YYYY-MM-DD_HH-MM_Thema-Patch.md

Beispiel:

2026-05-13_04-50_Marker-State-Reconcile-Safety-Patch.md

3. Patchreport speichern unter:

/home/pj/projects/hytale/project_keystone/NPCMod/docs/patch_reports

Wichtig:

- Wenn Safety-Regeln geändert wurden, müssen Safety-Dateien im selben Patch aktualisiert werden.
- Wenn keine Safety-Regeln geändert wurden, muss der Patchreport das klar sagen.
- Markdown-only Step braucht keinen Maven-Compile, aber Doku-Konsistenzprüfung.

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

────────────────────────────
12. EINGABE
────────────────────────────

Hier ist die Grundlage:

<<AUSWERTUNG / PATCHREPORTS / PLAN / PHASE EINFÜGEN>>

────────────────────────────
13. ABSCHLUSSANWEISUNG
────────────────────────────

Bitte jetzt nicht implementieren.

Bitte nur sichere AI-/Coder-Anweisungen, Agent Steps, Review-Prompts, Fix-Prompts und Checklisten erzeugen.

Keine Dateien ändern.
Keine Tests ausführen, die Dateien verändern.
Keine Safety-Dateien aktualisieren.
Keinen Patchreport schreiben.

Safety-Doku-Update und Patchreport-Erstellung nur als finalen Agent-Step einplanen.