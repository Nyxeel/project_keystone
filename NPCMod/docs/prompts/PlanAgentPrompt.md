Du bekommst gleich eine technische Auswertung/Analyse zu meinem Projekt.

Deine Aufgabe:
Erstelle daraus ein sicheres Copilot-/Agent-Arbeitsset mit:

1. einem Gesamtprompt für PLAN Mode
2. einer Step-by-Step-Agent-To-do-Liste
3. einem Review-Prompt nach jedem Agent-Step
4. einem Fix-/Debug-Prompt für den Fall, dass ein Step fehlschlägt
5. einer Fortschritts-Checkliste, damit spätere Steps nicht vergessen werden

Wichtig:
Die Auswertung ist die fachliche Grundlage.
Du sollst sie nicht blind verkürzen, sondern strukturiert in sichere Arbeitsphasen zerlegen.

Arbeitsprinzip:
- PLAN Mode darf nur analysieren und planen, keine Dateien ändern.
- AGENT Mode darf immer nur einen klar abgegrenzten Step umsetzen.
- Nach jedem Agent-Step muss ein Review-Step folgen.
- Wenn Review Probleme findet, muss zuerst ein Fix-Step kommen.
- Erst danach darf der nächste Agent-Step gestartet werden.
- Jeder Agent-Step muss den Gesamtkontext kurz wiederholen, damit Copilot das Gesamtziel nicht verliert.
- Jeder Agent-Step muss klar sagen, was NICHT geändert werden darf.
- Jeder Agent-Step muss ein Compile-/Test-Gate enthalten.
- Keine großen Refactors, außer die Auswertung verlangt es ausdrücklich.
- Keine angrenzenden Features nebenbei implementieren.
- Bestehende Safety-/Rollback-/Dedupe-Mechanismen dürfen nicht versehentlich abgeschwächt werden.

Bitte gib das Ergebnis in dieser Struktur aus:

────────────────────────────
A) Gesamtprompt für PLAN Mode
────────────────────────────

Erstelle einen vollständigen PLAN-Mode-Prompt.

Der Prompt muss enthalten:
- Gesamtziel
- wichtigste Architekturregeln
- verbotene Änderungen
- betroffene Bereiche/Dateien, falls aus der Auswertung ableitbar
- Analysefragen
- erwartetes Ergebnis des PLAN Mode
- klare Anweisung: „Noch keine Dateien ändern.“

────────────────────────────
B) Agent Step Liste
────────────────────────────

Zerlege die Umsetzung in kleine sichere Steps.

Für jeden Step bitte ausgeben:

# Agent Step X — [Name]

Prompt:
- kurzer Gesamtkontext
- Ziel dieses Steps
- erlaubte Dateien/Bereiche, falls bekannt
- konkrete Aufgaben
- klare Grenzen: was nicht geändert werden darf
- Safety-Regeln
- Compile-/Test-Befehl
- gewünschter Abschlussbericht

────────────────────────────
C) Review Prompt nach jedem Step
────────────────────────────

Für jeden Agent Step einen passenden Review-Prompt erstellen.

Der Review-Prompt soll prüfen:
- ob der Step wirklich nur seinen Scope geändert hat
- ob Nebenwirkungen entstanden sind
- ob wichtige Safety-Regeln verletzt wurden
- ob Compile/Test erfolgreich war
- ob der nächste Step sicher gestartet werden kann

Wichtig:
Review darf noch nichts Neues implementieren.

────────────────────────────
D) Fix-/Debug-Prompt Vorlage
────────────────────────────

Erstelle eine allgemeine Fix-Prompt-Vorlage:

- nur Review-Probleme aus dem letzten Step fixen
- keine neuen Features
- keinen nächsten Step umsetzen
- Compile/Test erneut ausführen
- kurz berichten, was behoben wurde

────────────────────────────
E) Fortschritts-Checkliste
────────────────────────────

Erstelle eine Checkliste mit allen Steps:

[ ] PLAN erstellt/geprüft
[ ] Step 1 umgesetzt
[ ] Step 1 reviewed
[ ] Step 1 gefixt, falls nötig
[ ] Step 2 umgesetzt
[ ] Step 2 reviewed
[ ] Step 2 gefixt, falls nötig
...

Am Ende soll klar stehen:
- welcher Step zuerst kommt
- wann ich zum nächsten Step wechseln darf
- welche Punkte aus der ursprünglichen Auswertung erst in späteren Steps kommen

Hier ist die Auswertung/Analyse:





<<AUSWERTUNG EINFÜGEN>>






Bitte jetzt nicht implementieren.
Bitte nur die PLAN- und AGENT-Prompts aus dieser Auswertung erzeugen.



WICHTIG!!!!!
Schreib dazu das am ende des komplettes durchfuehrung des plans auch die files in
/home/pj/projects/hytale/project_keystone/NPCMod/docs/safety geupdatet werden muessen!
DAS IST PFLICHT!!!