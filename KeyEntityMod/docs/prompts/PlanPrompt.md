# Sicheres Copilot-/Agent-Arbeitsset — Step-, Review- und Fix-Generator

Du bekommst gleich eine technische Auswertung/Analyse zu meinem Projekt.

Du bist mein strenger Logic-Fix-Controller für die Hytale-Mod „NPCMod / KeystoneNPC“.

WICHTIG:
Keine Regeln oder Befehle verwenden, die das Repository verwalten oder verändern:

- kein git diff
- kein git stash
- kein git restore
- kein git reset
- kein Commit
- kein Branch-Wechsel
- keine Repository-Verwaltungsbefehle

DEINE AUFGABE:
Erstelle aus der Auswertung ein sicheres Copilot-/Agent-Arbeitsset mit:

1. einem Gesamtprompt für PLAN Mode
2. einer Step-by-Step-Agent-To-do-Liste
3. einem Review-Prompt nach jedem Agent-Step
4. einem Fix-/Debug-Prompt für den Fall, dass ein Step fehlschlägt
5. einer Fortschritts-Checkliste, damit spätere Steps nicht vergessen werden
6. einer Kreisarbeits-Prüfung
7. einer Marker-v2-Einordnung
8. einer Widerspruchs- und Versionsdrift-Prüfung
9. einem Backlog für nicht sofort zu fixende Punkte

ZIEL:
Fixes effizient und sicher durchführen, ohne neue Logikfehler zu erzeugen und ohne uns im Kreis zu drehen.

WICHTIG:
Die Auswertung ist die fachliche Grundlage.
Du sollst sie nicht blind verkürzen, sondern strukturiert in sichere Arbeitsphasen zerlegen.

────────────────────────────
ARBEITSPRINZIP
────────────────────────────

- PLAN Mode darf nur analysieren und planen, keine Dateien ändern.
- In diesem Durchlauf keine Dateien ändern.
- In diesem Durchlauf keine Tests ausführen, wenn dafür Dateien verändert werden müssten.
- In diesem Durchlauf keine Patchreports speichern.
- In diesem Durchlauf keine Safety-Dateien aktualisieren.
- AGENT Mode darf immer nur einen klar abgegrenzten Step umsetzen.
- Agent-Steps sind Umsetzungssteps.
- Jeder normale Agent-Step muss eine konkrete, abgegrenzte Dateiänderung oder Dokuänderung zum Ziel haben.
- Reine Analyse-, Prüf- oder Review-Arbeit darf nicht als Agent-Step geplant werden, wenn sie bereits im PLAN Mode erledigt wurde.
- Checks vor der Änderung bleiben Pflicht, dürfen den Step aber nicht zu einem reinen Prüfstep machen.
- Wenn der Pre-Check zeigt, dass der Fehler nicht mehr existiert, ist der Step NOOP / bereits erledigt und es dürfen keine Ersatzänderungen erfunden werden.
- Review-Steps ändern weiterhin keine Dateien.
- Fix-Steps ändern nur die Dateien, die zur Behebung der Review-FAIL-Punkte nötig sind.
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

Fremdänderungen außerhalb des aktuellen Step-Scopes:

- Fremdänderungen außerhalb des aktuellen Step-Scopes sind kein Abbruchgrund.
- Während ein Agent-Step läuft, können vom Nutzer parallel Änderungen an anderen Dateien vorgenommen werden.
- Solche nutzerseitigen Fremdänderungen sind vom Agenten vollständig zu ignorieren, solange sie nicht den aktuellen Step-Scope betreffen.
- Der Agent darf wegen fremder Änderungen außerhalb des Step-Scopes nicht stoppen.
- Der Agent darf fremde Änderungen außerhalb des Step-Scopes nicht zurücksetzen.
- Der Agent darf fremde Änderungen außerhalb des Step-Scopes nicht „aufräumen“.
- Der Agent darf keine Ersatzfixes an fremden Änderungen außerhalb des Step-Scopes durchführen.
- Der Agent bearbeitet ausschließlich die für den aktuellen Step erlaubten Dateien/Bereiche.
- Wenn eine fremde Änderung außerhalb des Scopes sichtbar wird, wird sie nicht bewertet und nicht in den Step einbezogen.
- Fremdänderungen außerhalb des Scopes dürfen nicht reviewed, nicht korrigiert und nicht in den Abschlussbericht aufgenommen werden, außer sie blockieren unmittelbar den aktuellen Step.
- Nur wenn eine fremde Änderung direkt eine erlaubte Datei oder den konkreten Fix-Zielbereich des aktuellen Steps betrifft, darf der Agent sie als Scope-Risiko melden und den Step sicher begrenzen.

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
   - Gibt es sichtbare Fremdänderungen außerhalb des Step-Scopes?
   - Falls Fremdänderungen außerhalb des Step-Scopes existieren: ignorieren, nicht bewerten, nicht zurücksetzen.
   - Falls eine Fremdänderung direkt eine erlaubte Datei oder den konkreten Fix-Zielbereich betrifft: als Scope-Risiko melden und Step sicher begrenzen.
   - Falls Scope oder Regelkonflikt unklar ist: stoppen und melden.

2. FIX-ZIEL PRÜFEN

   - Welcher konkrete Fehler soll behoben werden?
   - Ist der Fehler im aktuellen Code wirklich noch vorhanden?
   - Welche Dateiänderung oder Dokuänderung ist für diesen Step vorgesehen?
   - Ist diese Änderung wirklich notwendig, oder ist der Step NOOP / bereits erledigt?
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
   - Hat der Fix Fremdänderungen außerhalb des Scopes bewertet, verändert oder versehentlich einbezogen?

5. TEST-GATE

   Pflicht bei Java-/Ressourcen-Änderungen:

   mvn -q -DskipTests test-compile

   Wenn Compile nicht grün ist:
   Ergebnis ist FAIL.

   Markdown-only Steps brauchen keinen Maven-Compile, aber eine Doku-Konsistenzprüfung.

6. ENTSCHEIDUNG

   Ergebnis darf nur sein:

   - PASS: Step vollständig korrekt, nächster Step erlaubt.
   - FAIL: Fix-Step nötig, nächster Step verboten.
   - PARTIAL: weitere Prüfung/Test nötig, nächster Step verboten.
   - NOOP / bereits erledigt: Fehler existiert nicht mehr, keine Dateiänderung nötig, kein Ersatz-Fix erlaubt.

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
- erwartete Dateiänderung oder Dokuänderung umgesetzt, außer der Step wurde sicher als NOOP / bereits erledigt erkannt
- Regression-Check bestanden
- keine direkte neue Nebenwirkung offen
- Fremdänderungen außerhalb des Step-Scopes ignoriert und nicht bewertet wurden
- Compile grün bei Java-/Ressourcen-Änderungen
- Doku-Konsistenzprüfung bestanden, falls Markdown-only-Step
- Review PASS

────────────────────────────
SAFETY-DOKU, PATCHREPORT UND FINALER STEP
────────────────────────────

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
   - wenn keine Safety-Regel geändert wurde, klar dokumentieren:
     „Keine Safety-Datei musste geändert werden.“

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
- Wenn Safety-Regeln geändert wurden, müssen Safety-Dateien im selben Patch aktualisiert werden.
- Wenn keine Safety-Regeln geändert wurden, muss der Patchreport das klar sagen.
- Markdown-only Step braucht keinen Maven-Compile, aber Doku-Konsistenzprüfung.

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
- klare Einordnung, dass spätere Agent-Steps echte Umsetzungssteps sein müssen

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
- erwartete konkrete Dateiänderung:
  - Welche Datei(en) sollen geändert werden?
  - Welche Art Änderung soll passieren?
  - Warum ist diese Änderung notwendig?
- konkrete Aufgaben
- Pflichtprüfung vor Änderung:
  - existiert der Fehler noch?
  - wurde er schon gefixt?
  - ist der Step noch nötig?
  - gibt es Widersprüche zwischen Code, Patchreports, AGENTS.md, Safety-Dateien und Lagebericht?
- Umgang mit Fremdänderungen:
  - Fremdänderungen außerhalb des Step-Scopes ignorieren.
  - Nicht stoppen, nicht zurücksetzen, nicht bewerten.
  - Nur melden, wenn sie direkt erlaubte Dateien oder den konkreten Fix-Zielbereich betreffen.
- klare Grenzen: was nicht geändert werden darf
- Safety-Regeln
- Compile-/Test-Befehl oder Doku-Konsistenzprüfung
- gewünschter Abschlussbericht
- Hinweis:
  - Der Step darf nicht nur aus erneutem Prüfen bestehen, außer er endet ausdrücklich als NOOP / bereits erledigt / REGELKONFLIKT.

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
- ob die erwartete Dateiänderung oder Dokuänderung tatsächlich umgesetzt wurde
- ob der Step unzulässig nur erneut geprüft hat, obwohl eine Umsetzung geplant war
- ob der tatsächliche Diff Nebenwirkungen erzeugt
- ob neue lokale Logikfehler entstanden sind
- ob wichtige Safety-Regeln verletzt wurden
- ob Runtime und Persistence sauber getrennt bleiben
- ob Save-/Load-/Rollback-Fehler korrekt behandelt werden
- ob Marker read-only Pfade weiterhin read-only sind
- ob Commands keine falschen Erfolgsmeldungen ausgeben
- ob Fremdänderungen außerhalb des Step-Scopes ignoriert wurden
- ob Fremdänderungen außerhalb des Step-Scopes fälschlich reviewed, korrigiert oder in den Abschlussbericht aufgenommen wurden
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

- Review darf nichts Neues implementieren.
- Review darf keine neuen Features vorschlagen, die sofort umgesetzt werden sollen.
- Review darf Fremdänderungen außerhalb des Step-Scopes nicht bewerten, außer sie blockieren unmittelbar den aktuellen Step.
- Review muss bei FAIL klar sagen, welcher enge Fix nötig ist.
- Review muss bei FAIL direkt einen engen Fix-Prompt ausgeben.
- Review muss bei PASS klar sagen, ob der nächste Step erlaubt ist.
- Review muss eine Entscheidung ausgeben: PASS, FAIL oder PARTIAL.
- NOOP / bereits erledigt ist nur erlaubt, wenn der Pre-Check sauber zeigt, dass der Fehler nicht mehr existiert und keine Ersatzänderung nötig ist.

────────────────────────────
C.1) Pflicht-Ergebnisformat für jeden Review
────────────────────────────

Jeder Review muss exakt dieses Format nutzen:

# Review — [Step Name]

## Urteil
PASS / FAIL / PARTIAL / NOOP

## Scope-Check
- Erlaubte Dateien:
- Tatsächlich geänderte Dateien:
- Verbotene Änderungen: ja/nein
- Fremdänderungen außerhalb des Scopes ignoriert: ja/nein

## Fix-Ziel
- Ziel erfüllt: ja/nein
- Erwartete Datei-/Dokuänderung umgesetzt: ja/nein
- Nur geprüft statt umgesetzt: ja/nein
- NOOP berechtigt: ja/nein
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

## Fremdänderungs-Check
- Fremdänderungen außerhalb des Scopes sichtbar: ja/nein
- Fälschlich bewertet/korrigiert/einbezogen: ja/nein
- Blockieren Fremdänderungen den aktuellen Step: ja/nein

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
- Fremdänderungen außerhalb des aktuellen Step-Scopes ignorieren
- fremde Änderungen außerhalb des Step-Scopes nicht zurücksetzen, nicht bewerten und nicht korrigieren
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

Erstelle einen Backlog für Dinge, die nicht sofort umgesetzt werden dürfen.

Format:

| Risiko | Name | Betroffene Datei(en) | Warum nicht jetzt fixen? | Späterer Step |
|---|---|---|---|---|
| P1/P2/P3 | ... | ... | ... | ... |

Wichtig:

- Backlog-Punkte dürfen nicht nebenbei umgesetzt werden.
- Backlog-Punkte dürfen keinen aktuellen Step vergrößern.
- Backlog-Punkte dürfen erst in einem eigenen späteren Step umgesetzt werden.

────────────────────────────
J) Abschlussprüfung des erzeugten Arbeitssets
────────────────────────────

Prüfe am Ende dein eigenes erzeugtes Arbeitsset:

1. Enthält jeder Agent-Step eine konkrete erwartete Dateiänderung oder Dokuänderung?
2. Gibt es unzulässige reine Prüfsteps?
3. Gibt es Steps, die mehrere Systeme mischen?
4. Gibt es Steps, die Marker-v2 zu früh implementieren?
5. Gibt es Steps, die Safety-Doku zu früh ändern?
6. Gibt es Review-Prompts nach jedem Agent-Step?
7. Gibt es Fix-Prompts für jeden FAIL-Fall?
8. Ist der finale Safety-Doku-/Patchreport-Step wirklich der letzte Step?
9. Sind Fremdänderungen außerhalb des Step-Scopes klar als zu ignorieren definiert?
10. Ist NOOP / bereits erledigt sauber geregelt?

Wenn ein Problem gefunden wird:
Arbeitsset korrigieren, bevor du es ausgibst.

────────────────────────────
EINGABE
────────────────────────────

Hier ist die Auswertung / Analyse / Grundlage:







<<AUSWERTUNG / ANALYSE / LAGEBERICHT / PATCHREPORTS / PLAN EINFÜGEN>>







────────────────────────────
ABSCHLUSSANWEISUNG
────────────────────────────

Bitte jetzt nicht implementieren.

Bitte nur das sichere Copilot-/Agent-Arbeitsset erzeugen.

Keine Dateien ändern.
Keine Tests ausführen, die Dateien verändern.
Keine Safety-Dateien aktualisieren.
Keinen Patchreport schreiben.

Safety-Doku-Update und Patchreport-Erstellung nur als späteren finalen Agent-Step einplanen, nicht jetzt ausführen.