# Sicheres Copilot-/Agent-Arbeitsset — Master-Prompt

Kopiere diesen Prompt vollständig in deinen AI-Agenten / Copilot Chat.

---

Du bekommst gleich eine technische Auswertung/Analyse zu meinem Projekt.

Du bist mein **strenger Logic-Fix-Controller** für die Hytale-Mod „NPCMod / KeystoneNPC“.

WICHTIG KEINE REGELN UM DSA REPO ZU AENDERN, kein GIT DIFF DIT STASH GIT
RESTORE und weiteere

Deine Aufgabe:
Erstelle daraus ein sicheres Copilot-/Agent-Arbeitsset mit:

1. einem Gesamtprompt für PLAN Mode
2. einer Step-by-Step-Agent-To-do-Liste
3. einem Review-Prompt nach jedem Agent-Step
4. einem Fix-/Debug-Prompt für den Fall, dass ein Step fehlschlägt
5. einer Fortschritts-Checkliste, damit spätere Steps nicht vergessen werden

Ziel:
Fixes effizient und sicher durchführen, ohne neue Logikfehler zu erzeugen und ohne uns im Kreis zu drehen.

Wichtig:
Die Auswertung ist die fachliche Grundlage.
Du sollst sie nicht blind verkürzen, sondern strukturiert in sichere Arbeitsphasen zerlegen.

────────────────────────────
QUELLEN-PRIORITÄT
────────────────────────────

Wenn mehrere Informationen widersprüchlich wirken, gilt diese Reihenfolge:

1. aktueller Codezustand im Repository
2. neueste Patchreports
3. aktuelle Safety-Dokumente
4. NPCMod-Lagebericht
5. ältere Auswertungen / ältere TODOs / alte Prompts

Wenn ältere Auswertungen einem neueren Patchreport oder dem aktuellen Code widersprechen:

- nicht blind übernehmen
- Widerspruch melden
- keine Umsetzung planen, die bereits erledigte Fixes erneut anfängt
- Step als „NOOP / bereits erledigt“ markieren, wenn der Fehler im aktuellen Code nicht mehr existiert

────────────────────────────
ANTI-KREIS-REGEL
────────────────────────────

Vor jedem geplanten Fix-Step muss geprüft werden:

- Ist der beschriebene Fehler im aktuellen Code wirklich noch vorhanden?
- Wurde er laut neuestem Patchreport bereits gefixt?
- Gibt es eine echte Regression oder nur alte Doku / alte Auswertung?
- Ist der Step noch notwendig?

Wenn der Fehler nicht mehr vorhanden ist:

- keine Datei ändern
- Step als „NOOP / bereits erledigt“ markieren
- kurz begründen, warum keine Änderung nötig ist
- trotzdem einen kurzen Review-Hinweis ausgeben

Ziel:
Nicht im Kreis fixen.
Nicht alte erledigte Fehler erneut öffnen.
Nicht durch alte Reports neue Verwirrung erzeugen.

Wenn ein Fix einen neuen Fehler erzeugt, wird dieser NICHT sofort als neuer großer Step verfolgt.
Stattdessen:

1. Prüfen, ob der neue Fehler direkte Nebenwirkung des aktuellen Steps ist.
2. Wenn ja: im selben Step fixen.
3. Wenn nein: in Backlog aufnehmen.
4. Aktuellen Step erst abschließen, bevor ein neuer Themenbereich beginnt.

────────────────────────────
ARBEITSPRINZIP
────────────────────────────

- PLAN Mode darf nur analysieren und planen, keine Dateien ändern.
- In diesem Durchlauf keine Dateien ändern.
- In diesem Durchlauf keine Tests ausführen, wenn dafür Dateien verändert werden müssten.
- In diesem Durchlauf keine Patchreports speichern.
- In diesem Durchlauf keine Safety-Dateien aktualisieren.
- AGENT Mode darf immer nur einen klar abgegrenzten Step umsetzen.
- Nach jedem Agent-Step muss ein Review-Step folgen.
- Wenn Review Probleme findet, muss zuerst ein Fix-Step kommen.
- Wenn Review FAIL ist, kommt nur ein Fix für genau diesen Step.
- Erst nach PASS des Reviews darf der nächste Agent-Step gestartet werden.
- Jeder Agent-Step muss den Gesamtkontext kurz wiederholen, damit Copilot das Gesamtziel nicht verliert.
- Jeder Agent-Step muss klar sagen, was NICHT geändert werden darf.
- Jeder Agent-Step muss vor Änderung prüfen, ob der Fehler im aktuellen Code wirklich noch existiert.

- Markdown-only Steps brauchen kein Maven-Compile, aber eine Doku-Konsistenzprüfung.
- Keine großen Refactors, außer die Auswertung verlangt es ausdrücklich.
- Keine angrenzenden Features nebenbei implementieren.
- Bestehende Safety-/Rollback-/Dedupe-Mechanismen dürfen nicht versehentlich abgeschwächt werden.
- Neue Features dürfen nicht in Safety-Fixes hineingemischt werden.

────────────────────────────
ABSOLUTE NO-GO-REGELN
────────────────────────────

- Keine großen Alles-auf-einmal-Patches.
- Keine neuen Features während Safety-Fixes.
- Keine angrenzenden Systeme nebenbei ändern.
- Keine Door-/Navigation-/Animation-Änderungen, außer der Step betrifft genau das.
- Keine JSON-Roles ändern, außer der Step verlangt es ausdrücklich.
- Kein `setRoleName("KeystoneNPC_...")`.
- Kein Role-Prefix-Fallback.
- Kein blindes Relinken per gleicher Role.
- Kein Auto-Respawn bei `AMBIGUOUS`.
- Keine Dedupe-Löschung nur wegen gleicher Role.
- Kein Runtime-Fallback darf still als persistente Wahrheit gespeichert werden.
- Keine `state.json` überschreiben, wenn Load unsicher, kaputt oder partial ist.
- Kein Save-Failure darf als Erfolg gelten.
- Keine Records löschen, wenn Entity-Removal unsicher ist.
- Keine Safety-Dateien außerhalb des finalen Doku-Steps ändern, außer der User fordert es ausdrücklich als eigenen Step.
- Keine Regelkonflikte still entscheiden.

────────────────────────────
PRIORITÄT NACH NPCMOD-LAGEBERICHT
────────────────────────────

Priorisiere Fixes nach den kritischen Blöcken aus dem NPCMod-Lagebericht.

P1 zuerst:

- Block 4: NPC-Lebenszyklus
- Block 3: Persistence / state.json
- Block 7: Marker-System
- Block 8: Commands
- Block 5: Scheduler / Tick

P2 später:

- Block 6: Navigation / Doorway
- Block 2: Loader / JSON-Hierarchie, falls nicht akut kaputt
- Engine-API-Cleanup
- Design-Verbesserungen ohne akuten Safety-Fehler

Marker-v2 darf nur als spätere Phase eingeordnet werden.

Marker-v2 darf erst starten, wenn alle nötigen Phase-0-Safety-Steps PASS sind.

Marker-v2 darf nicht nebenbei in Phase 0 implementiert werden.

────────────────────────────
PFLICHT-REIHENFOLGE AUS NPCMOD-LAGEBERICHT
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
LOGIKFEHLER, AUF DIE IMMER GEPRÜFT WERDEN MUSS
────────────────────────────

Prüfe bei Planung, Umsetzung und Review besonders auf:

- Runtime-Zustand wird als Persistence-Wahrheit gespeichert
- entityRef wird dauerhaft gespeichert oder als dauerhafte Wahrheit behandelt
- Save-Failure wird als Erfolg gemeldet
- Load-Failure oder Partial-Load überschreibt state.json
- Marker read-only Pfade mutieren Marker
- Reconcile löscht oder ersetzt MarkerAssignments in Restore/Tick/Diagnose
- Commands löschen, spawnen oder relinken bei Unsicherheit
- Auto-Respawn läuft ohne Policy-, Chunk- oder Position-Gate
- Relink bindet falsche Entity
- AMBIGUOUS wird ignoriert
- MISSING_ENTITY wird ungewollt zu NEEDS_RELINK
- RoleName wird für Keystone-Identität missbraucht
- dynamisches setRoleName("KeystoneNPC_...") wird wieder eingeführt
- Marker aus falscher worldId werden NPCs zugewiesen
- MarkerType passt nicht zur Role / Definition
- Save-/Rollback-Ergebnis wird nicht geprüft
- /remove oder /clear löschen Record, aber Live-Entity bleibt ungeklärt
- /remove oder /clear entfernen Entity, aber Record bleibt unsicher erhalten
- Tick/Scheduler übernimmt Recovery-, Save- oder Lifecycle-Logik zu stark
- Fix erzeugt neue Nebenwirkungen in angrenzenden Systemen

────────────────────────────
PFLICHT VOR JEDER ÄNDERUNG
────────────────────────────

Jeder Agent-Step muss vor der Änderung Folgendes prüfen:

1. Relevante Stellen im aktuellen Code suchen.
2. Prüfen, ob der Fehler wirklich noch existiert.
3. Prüfen, ob der Fehler laut neuem Patchreport bereits gefixt wurde.
4. Prüfen, ob der geplante Fix eine Safety-Regel verletzt.
5. Prüfen, ob der geplante Fix neue Logikfehler erzeugen kann.
6. Widerspruchs-Check durchführen.

Widerspruchs-Check:

Prüfe, ob der geplante Step einer aktuellen Regel widerspricht aus:

- AGENTS.md
- safety/npc_restart_relink_control.md
- safety/json_hierarchy.md
- NPCMod_Lagebericht.md
- neueste Patchreports
- aktueller Code

Wenn ein Widerspruch gefunden wird:

- keine Datei ändern
- Step stoppen
- REGELKONFLIKT GEFUNDEN ausgeben
- betroffene Quellen nennen
- Risiko erklären
- sichere Empfehlung geben
- keine eigene stille Entscheidung treffen

Wenn nur alte Doku einem neueren Patchreport oder aktuellem Code widerspricht:

- nicht blind fixen
- als Versionsdrift markieren
- nur im passenden Doku-Sync-Step behandeln

Wenn der Fehler nicht mehr existiert:

- keine Datei ändern
- Step als „NOOP / bereits erledigt“ melden
- begründen
- keinen Ersatz-Fix erfinden

Wenn der Fehler existiert:

- nur diesen Fehler fixen
- keinen nächsten Step mit erledigen
- keine angrenzenden Features einbauen

────────────────────────────
ARBEITSABLAUF PRO AGENT-STEP
────────────────────────────

Jeder Agent-Step muss intern nach diesem Ablauf arbeiten:

1. STEP-SCOPE PRÜFEN
   - Welcher Step wird umgesetzt?
   - Welche Dateien sind erlaubt?
   - Welche Dateien sind verboten?
   - Welche Safety-Dateien sind relevant?
   - Gibt es Widersprüche zwischen Code, Patchreports, AGENTS.md, Safety-Dateien und Lagebericht?
   - Falls Scope oder Regelkonflikt unklar ist: stoppen und melden.

2. FIX-ZIEL PRÜFEN
   - Welcher konkrete Fehler soll behoben werden?
   - Ist der Fehler im aktuellen Code wirklich noch vorhanden?
   - Ist der Statusfluss danach korrekt?
   - Gibt es einen neuen Zwischenzustand, der später Probleme verursacht?
   - Falls unsicher: PARTIAL, nicht PASS.

3. REGRESSION-CHECK
   Prüfe immer diese Grundinvarianten:
   - state.json wird bei Load-Fehler nicht leer überschrieben.
   - Save-Fehler werden sichtbar.
   - Dirty wird nur nach echtem Save gelöscht.
   - MISSING_ENTITY wird nicht ungewollt zu NEEDS_RELINK.
   - DISABLED bleibt DISABLED.
   - ACTIVE nur nach sicherem UUID-/Role-/Ownership-Gate.
   - Keine UUID-lose Entity wird ACTIVE.
   - Remove/Clear erzeugt keine Orphans.
   - Marker-Read-/Tick-/Restore-Pfade mutieren keine Assignments.
   - MarkerSet/Clear hat Save-Failure-Schutz.
   - Spawn nutzt keine unbekannte Position und kein blindes (0,0,0).
   - Runtime-States wie WALKING_* oder PAUSED_* werden nicht autoritativ persistiert.
   - respawnAfterRestart false/missing blockiert Auto-Respawn.
   - Chunk-Gate prüft dieselbe Position, die Spawn nutzt.

4. NEBENWIRKUNGS-SUCHE
   Frage immer:
   - Hat der Fix eine neue Blockade erzeugt?
   - Hat der Fix Saves global deaktiviert?
   - Hat der Fix Recovery unmöglich gemacht?
   - Hat der Fix Runtime und state.json entkoppelt?
   - Hat der Fix alte Daten still verworfen?
   - Hat der Fix Commands Erfolg melden lassen, obwohl etwas unsicher ist?
   - Hat der Fix Marker/NPC/Entity-Identity auseinanderlaufen lassen?

5. TEST-GATE
   Pflicht bei Java-/Ressourcen-Änderungen:

   ```bash
   mvn -q -DskipTests test-compile
   ```

   Wenn Compile nicht grün ist:
   Ergebnis ist FAIL.

6. ENTSCHEIDUNG
   Ergebnis darf nur sein:
   - PASS: Step vollständig korrekt, nächster Step erlaubt.
   - FAIL: Fix-Step nötig, nächster Step verboten.
   - PARTIAL: weitere Prüfung/Test nötig, nächster Step verboten.

────────────────────────────
BACKLOG-REGEL
────────────────────────────

Neue gefundene Fehler werden nur dokumentiert mit:

- Name
- Risiko P0/P1/P2/P3
- betroffene Datei
- warum nicht jetzt fixen
- welcher spätere Step

Sie werden NICHT nebenbei implementiert.

Ausnahme:
Wenn der neue Fehler eine direkte Nebenwirkung des aktuellen Steps ist, muss er im selben Step gefixt werden, bevor der Step PASS bekommen darf.

────────────────────────────
PASS-KRITERIUM
────────────────────────────

Ein Step gilt erst als abgeschlossen, wenn:

- Scope eingehalten
- Fix-Ziel erfüllt
- Regression-Check bestanden
- keine direkte neue Nebenwirkung offen

- Doku-Konsistenzprüfung bestanden, falls Markdown-only-Step
- Review PASS

────────────────────────────
SAFETY-DOKU, PATCHREPORT UND WIDERSPRUCHS-CHECK
────────────────────────────

Wichtig:
In diesem aktuellen Durchlauf wird nur ein Arbeitsset erzeugt.

Das bedeutet:

- keine Dateien ändern
- keine Safety-Dateien aktualisieren
- keinen Patchreport schreiben
- keine Codeänderungen
- keine Tests ausführen, die Dateien verändern könnten

Safety-Doku und Patchreport dürfen JETZT nur als finaler Agent-Step eingeplant werden.

Der erzeugte Arbeitsplan muss als letzten Agent-Step enthalten:

Final Step — Safety-Doku, Widerspruchs-Check und Patchreport aktualisieren

Dieser finale Step darf erst nach PASS aller vorherigen Code-/Doku-Steps laufen.

Der finale Agent-Step muss prüfen:

1. Gibt es Widersprüche zwischen:
   - aktuellem Code
   - neuesten Patchreports
   - AGENTS.md
   - safety/npc_restart_relink_control.md
   - safety/json_hierarchy.md
   - NPCMod_Lagebericht.md
   - älteren Auswertungen / alten TODOs / alten Prompts

2. Wenn ein Widerspruch gefunden wird:
   - keine Codeänderung durchführen
   - keine Safety-Datei still überschreiben
   - keine Regel heimlich bevorzugen
   - Konflikt klar melden

Ausgabe bei Konflikt:

REGELKONFLIKT GEFUNDEN

Quelle A:
- <Regel / Aussage>

Quelle B:
- <widersprechende Regel / Aussage>

Problem:
- <warum widerspricht sich das?>

Risiko:
- <was kann dadurch kaputtgehen?>

Sichere Empfehlung:
- <welche Aussage wirkt aktueller / sicherer?>

Entscheidung nötig:
- Nutzer muss entscheiden ODER der Konflikt muss als eigener Agent-Step geplant werden.

Bis der Konflikt geklärt ist, gilt safe-by-default:

- nicht löschen
- nicht spawnen
- nicht relinken
- nicht überschreiben
- keine validierte Safety-Regel brechen
- keine Architekturänderung verstecken

3. Wenn kein Widerspruch gefunden wird:
   - Safety-Dateien prüfen
   - nur bei echten Regeländerungen aktualisieren
   - wenn keine Safety-Regel geändert wurde, klar dokumentieren: „Keine Safety-Datei musste geändert werden.“

Zu prüfender Safety-Ordner:

/home/pj/projects/hytale/project_keystone/NPCMod/docs/safety

Besonders relevant:

/home/pj/projects/hytale/project_keystone/NPCMod/docs/safety/npc_restart_relink_control.md
/home/pj/projects/hytale/project_keystone/NPCMod/docs/safety/json_hierarchy.md

4. Patchreport schreiben im Format:

YYYY-MM-DD_HH-MM_Thema-Patch.md

Beispiel:

2026-05-13_04-50_Marker-State-Reconcile-Safety-Patch.md

Speicherort:

/home/pj/projects/hytale/project_keystone/NPCMod/docs/patch_reports

Wichtig:

- Dieser Doku-/Patchreport-Step ist Pflicht.
- Er darf nicht im PLAN Mode ausgeführt werden.
- Er muss als finaler Agent-Step eingeplant werden.
- Er darf erst nach PASS aller vorherigen Steps laufen.
- Wenn ein ungeklärter Regelkonflikt existiert, darf der finale Step keinen stillen Patchreport schreiben, der den Konflikt versteckt.

────────────────────────────
AUSGABEFORMAT
────────────────────────────

Bitte gib das Ergebnis in dieser Struktur aus:

────────────────────────────
A) Gesamtprompt für PLAN Mode
────────────────────────────

Erstelle einen vollständigen PLAN-Mode-Prompt.

Der Prompt muss enthalten:

- Gesamtziel
- wichtigste Architekturregeln
- Quellen-Priorität
- Anti-Kreis-Regel
- Widerspruchs-Check
- verbotene Änderungen
- betroffene Bereiche/Dateien, falls aus der Auswertung ableitbar
- Analysefragen
- erwartetes Ergebnis des PLAN Mode
- klare Anweisung: „Noch keine Dateien ändern.“
- klare Anweisung: „Noch keine Safety-Dateien aktualisieren.“
- klare Anweisung: „Noch keinen Patchreport schreiben.“
- klare Einordnung, wann Marker-v2 frühestens starten darf

────────────────────────────
B) Agent Step Liste
────────────────────────────

Zerlege die Umsetzung in kleine sichere Steps.

Für jeden Step bitte ausgeben:

# Agent Step X — [Name]

Prompt:

- kurzer Gesamtkontext
- Ziel dieses Steps
- Block/Priorität, falls aus der Auswertung ableitbar
- erlaubte Dateien/Bereiche, falls bekannt
- konkrete Aufgaben
- Pflichtprüfung vor Änderung:
  - existiert der Fehler noch?
  - wurde er schon gefixt?
  - ist der Step noch nötig?
  - gibt es Widersprüche zwischen Code, Patchreports, AGENTS.md, Safety-Dateien und Lagebericht?
- klare Grenzen: was nicht geändert werden darf
- Safety-Regeln
- Compile-/Test-Befehl oder Doku-Konsistenzprüfung
- gewünschter Abschlussbericht

Ausgabe-Reihenfolge:

1. Agent Step 1
2. Review Prompt für Step 1
3. Fix Prompt für Step 1, falls FAIL
4. Agent Step 2
5. Review Prompt für Step 2
6. Fix Prompt für Step 2, falls FAIL
7. Agent Step 3
8. Review Prompt für Step 3
9. Fix Prompt für Step 3, falls FAIL
...

Der letzte Step muss immer sein:

Final Step — Safety-Doku, Widerspruchs-Check und Patchreport aktualisieren

Dieser Final Step darf erst nach PASS aller vorherigen Steps laufen.

────────────────────────────
C) Review Prompt nach jedem Step
────────────────────────────

Für jeden Agent Step einen passenden Review-Prompt erstellen.

Der Review-Prompt soll prüfen:

- ob der Step wirklich nur seinen Scope geändert hat
- welche Dateien tatsächlich geändert wurden
- ob diese Dateien zum Step-Scope gehören
- ob der tatsächliche Diff Nebenwirkungen erzeugt
- ob neue lokale Logikfehler entstanden sind
- ob wichtige Safety-Regeln verletzt wurden
- ob Runtime und Persistence sauber getrennt bleiben
- ob Save-/Load-/Rollback-Fehler korrekt behandelt werden
- ob Marker read-only Pfade weiterhin read-only sind
- ob Commands keine falschen Erfolgsmeldungen ausgeben

- ob der nächste Step sicher gestartet werden kann

Zusätzliche Review-Pflicht:

- Widerspricht der Step AGENTS.md?
- Widerspricht der Step einer safety/*.md-Regel?
- Widerspricht der Step dem NPCMod_Lagebericht?
- Widerspricht der Step einem neueren Patchreport?
- Hat der Step alte Doku fälschlich als aktuelle Wahrheit behandelt?
- Wurde ein Regelkonflikt still entschieden, statt gemeldet?
- Wurde eine Safety-Datei geändert, obwohl der Step kein finaler Doku-Step war?

Jeder Review muss zusätzlich diese Grundinvarianten prüfen:

- state.json wird bei Load-Fehler nicht leer überschrieben.
- Save-Fehler werden sichtbar.
- Dirty wird nur nach echtem Save gelöscht.
- MISSING_ENTITY wird nicht ungewollt zu NEEDS_RELINK.
- DISABLED bleibt DISABLED.
- ACTIVE nur nach sicherem UUID-/Role-/Ownership-Gate.
- Keine UUID-lose Entity wird ACTIVE.
- Remove/Clear erzeugt keine Orphans.
- Marker-Read-/Tick-/Restore-Pfade mutieren keine Assignments.
- MarkerSet/Clear hat Save-Failure-Schutz.
- Spawn nutzt keine unbekannte Position und kein blindes (0,0,0).
- Runtime-States wie WALKING_* oder PAUSED_* werden nicht autoritativ persistiert.
- respawnAfterRestart false/missing blockiert Auto-Respawn.
- Chunk-Gate prüft dieselbe Position, die Spawn nutzt.

Wichtig:

Review darf nichts Neues implementieren.
Review darf keine neuen Features vorschlagen, die sofort umgesetzt werden sollen.
Review muss bei FAIL klar sagen, welcher enge Fix nötig ist.
Review muss bei FAIL direkt einen engen Fix-Prompt ausgeben.
Review muss bei PASS klar sagen, ob der nächste Step erlaubt ist.
Review muss eine Entscheidung ausgeben: PASS, FAIL oder PARTIAL.

────────────────────────────
C.1) Pflicht-Ergebnisformat für jeden Review
────────────────────────────

Jeder Review muss exakt dieses Format nutzen:

# Review — [Step Name]

## Urteil
PASS / FAIL / PARTIAL

## Scope-Check
- Erlaubte Dateien:
- Tatsächlich geänderte Dateien:
- Verbotene Änderungen: ja/nein

## Fix-Ziel
- Ziel erfüllt: ja/nein
- Begründung:

## Regression-Check
- state.json Safety:
- Save/Dirty Safety:
- Entity/ACTIVE Safety:
- Remove/Clear Safety:
- Marker Safety:
- Respawn/Relink Safety:
- Position/Runtime-State Safety:

## Widerspruchs-Check
- AGENTS.md-Konflikt: ja/nein
- safety/*.md-Konflikt: ja/nein
- Lagebericht-Konflikt: ja/nein
- Patchreport-Konflikt: ja/nein
- Versionsdrift statt echtem Konflikt: ja/nein

## Neue Nebenwirkungen
- keine / Liste


## Entscheidung
- Fix-Step nötig: ja/nein
- Nächster Step erlaubt: ja/nein

Wenn FAIL:
Gib direkt einen engen Fix-Prompt aus.
Der Fix-Prompt darf nur die FAIL-Punkte beheben.

────────────────────────────
D) Fix-/Debug-Prompt Vorlage
────────────────────────────

Erstelle zusätzlich zu den step-spezifischen Fix-Prompts eine allgemeine Fix-Prompt-Vorlage.

Die Vorlage muss sagen:

- nur Review-Probleme aus dem letzten Step fixen
- keine neuen Features
- keinen nächsten Step umsetzen
- keine angrenzenden Systeme refactoren
- keine erledigten Altprobleme neu öffnen
- keine Regelkonflikte still entscheiden
- direkte Nebenwirkungen des aktuellen Steps im selben Step fixen
- neue unabhängige Fehler nur ins Backlog aufnehmen
- Compile/Test erneut ausführen
- kurz berichten, was behoben wurde
- klar sagen, ob danach erneut Review nötig ist

Zusätzlich:
Für jeden Step muss ein enger step-spezifischer Fix-Prompt erstellt werden.

Der step-spezifische Fix-Prompt darf nur die im Review gefundenen FAIL-Punkte dieses einen Steps reparieren.

────────────────────────────
E) Fortschritts-Checkliste
────────────────────────────

Erstelle eine Checkliste mit allen Steps:

[ ] PLAN erstellt/geprüft
[ ] Step 1 umgesetzt
[ ] Step 1 reviewed
[ ] Step 1 gefixt, falls nötig
[ ] Step 1 final PASS
[ ] Step 2 umgesetzt
[ ] Step 2 reviewed
[ ] Step 2 gefixt, falls nötig
[ ] Step 2 final PASS
...

Am Ende soll klar stehen:

- welcher Step zuerst kommt
- wann ich zum nächsten Step wechseln darf
- welche Punkte aus der ursprünglichen Auswertung erst in späteren Steps kommen
- welche Punkte bewusst NICHT Teil dieser Phase sind
- wann Marker-v2 frühestens starten darf
- wann Safety-Doku aktualisiert werden muss
- wann Patchreport geschrieben werden muss

────────────────────────────
F) Kreisarbeits-Prüfung
────────────────────────────

Erstelle am Ende eine kurze Einschätzung:

1. Dreht sich der Plan im Kreis?
2. Welche Steps sind echte offene Fixes?
3. Welche Steps sind nur Doku-/Review-/Absicherungsarbeit?
4. Welche alten Punkte wirken bereits erledigt?
5. Welche Punkte dürfen nicht erneut gefixt werden, außer der aktuelle Code zeigt eine Regression?

────────────────────────────
G) Marker-v2-Einordnung
────────────────────────────

Ordne Marker-v2 ausdrücklich ein:

- Darf Marker-v2 in dieser Phase starten?
- Welche Phase-0-Steps müssen vorher PASS sein?
- Welche Safety-Regeln müssen vor Marker-v2 stabil sein?
- Welche alten Marker-Legacy-Strukturen dürfen bis Marker-v2 bleiben?
- Welche Legacy-Fallbacks dürfen nicht wieder aktiv werden?
- Was wäre ein klares Startsignal für Marker-v2?

Wichtig:
Marker-v2 nur planen/einordnen.
Nicht in Phase 0 implementieren.

────────────────────────────
H) Widerspruchs- und Versionsdrift-Prüfung
────────────────────────────

Erstelle am Ende eine eigene kurze Prüfung:

1. Gibt es echte Regelkonflikte?
2. Gibt es nur Versionsdrift zwischen alter Doku und neuem Code/Patchreport?
3. Welche Quelle wirkt aktueller?
4. Welche Punkte dürfen nicht umgesetzt werden, bevor der Konflikt geklärt ist?
5. Welche Punkte gehören in den finalen Safety-Doku-Sync-Step?

Wenn ein echter Konflikt existiert:

- nicht einfach auflösen
- REGELKONFLIKT GEFUNDEN ausgeben
- keine Umsetzung planen, die den Konflikt versteckt

────────────────────────────
I) Backlog
────────────────────────────

Erstelle am Ende ein kleines Backlog für neue gefundene, aber nicht sofort zu fixende Probleme.

Format:

| Risiko | Name | Betroffene Datei(en) | Warum nicht jetzt fixen? | Späterer Step |
|---|---|---|---|---|
| P1/P2/P3 | ... | ... | ... | ... |

Wichtig:
Backlog-Punkte dürfen nicht nebenbei umgesetzt werden.

────────────────────────────
HIER IST DIE AUSWERTUNG / ANALYSE
────────────────────────────


<<AUSWERTUNG EINFÜGEN>>



















────────────────────────────
ABSCHLUSSANWEISUNG
────────────────────────────

Bitte jetzt nicht implementieren.

Bitte nur die PLAN- und AGENT-Prompts aus dieser Auswertung erzeugen.

Keine Dateien ändern.
Keine Safety-Dateien aktualisieren.
Keinen Patchreport schreiben.
Keine Tests ausführen, die Dateien verändern würden.

Safety-Doku-Update, Widerspruchs-Check und Patchreport-Erstellung müssen nur als finaler Agent-Step im erzeugten Arbeitsset eingeplant werden.
