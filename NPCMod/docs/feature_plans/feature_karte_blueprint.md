# Feature-Karte — [FEATURE-NAME]

Status:
[ ] Idee
[ ] geplant
[ ] bereit für Agent-Steps
[ ] in Umsetzung
[ ] fertig
[ ] abgebrochen

────────────────────────────
1. Kurzbeschreibung
────────────────────────────

## Was soll das Feature tun?

[Kurze Erklärung in 2–5 Sätzen]

Beispiel:
Das Feature soll NPCs erlauben, ihre Marker-Zuweisungen über markerAssignments zu speichern, statt über einzelne Legacy-Felder wie bedMarkerId oder workMarkerId.

## Warum brauchen wir das?

[Welches Problem löst es?]

## Was ist der Nutzen?

- [Nutzen 1]
- [Nutzen 2]
- [Nutzen 3]

────────────────────────────
2. Nicht-Ziele
────────────────────────────

Dieses Feature darf NICHT:

[ ] große Refactors auslösen
[ ] Marker-v2 nebenbei komplett einbauen
[ ] NpcRoutineRunner groß umbauen
[ ] state.json automatisch migrieren
[ ] alte Daten löschen
[ ] neue Respawn-/Relink-Policy erfinden
[ ] Navigation/Doorway ändern
[ ] Hytale Engine-Roles ändern
[ ] Runtime-State persistieren
[ ] Commands gefährlicher machen
[ ] Safety-Dateien außerhalb eines eigenen Doku-Steps ändern

Konkrete Nicht-Ziele:

- [Nicht-Ziel 1]
- [Nicht-Ziel 2]
- [Nicht-Ziel 3]

────────────────────────────
3. Betroffene Mindmap-Blöcke
────────────────────────────

Welche Systeme berührt das Feature?

[ ] 1 Plugin / Einstieg
[ ] 2 Loader & JSON-Hierarchie
[ ] 3 Persistenz / state.json
[ ] 4 NPC-Lebenszyklus
[ ] 5 Routine / Scheduler / Tick
[ ] 6 Navigation / Pathfinding / Doorway
[ ] 7 Marker-System
[ ] 8 Command-System
[ ] 9 Safety / Kontrollregeln
[ ] 10 Hytale API / Engine

## Hauptblock

Der wichtigste Block ist:

[Blocknummer + Name]

## Nebenblöcke

Zusätzlich betroffen:

- [Block]
- [Block]
- [Block]

## Warnung

Wenn mehr als 3 Blöcke stark betroffen sind:
Feature NICHT direkt bauen.
Erst in mehrere Phasen splitten.

────────────────────────────
4. Quellen und aktuelle Wahrheit
────────────────────────────

Vor Planung prüfen:

[ ] aktueller Code
[ ] neueste Patchreports
[ ] AGENTS.md
[ ] safety/npc_restart_relink_control.md
[ ] safety/json_hierarchy.md
[ ] NPCMod_Lagebericht.md
[ ] Mindmap
[ ] alte TODOs nur als Hinweis, nicht als Wahrheit

## Quellen-Priorität

Wenn Quellen widersprechen, gilt:

1. aktueller Code
2. neueste Patchreports
3. aktuelle Safety-Dateien
4. NPCMod_Lagebericht
5. ältere Prompts / alte TODOs

## Ergebnis der Quellenprüfung

- Aktueller Code geprüft: ja/nein
- Patchreports geprüft: ja/nein
- Safety-Dateien geprüft: ja/nein
- Widersprüche gefunden: ja/nein

Falls ja:

REGELKONFLIKT GEFUNDEN

Quelle A:
- [...]

Quelle B:
- [...]

Problem:
- [...]

Risiko:
- [...]

Entscheidung nötig:
- [...]

────────────────────────────
5. Datenfluss
────────────────────────────

Welche Daten liest das Feature?

- [z. B. state.json]
- [z. B. NPC-Definition]
- [z. B. MarkerRegistry]
- [z. B. EntityRef]
- [z. B. Command-Argumente]

Welche Daten schreibt das Feature?

- [z. B. state.json]
- [z. B. markerAssignments]
- [z. B. NPC-Record]
- [z. B. MarkerRegistry]

Welche Daten dürfen NICHT geschrieben werden?

- entityRef
- Runtime-Navigation
- Door-Runtime-State
- activeAction
- pendingAction
- temporäre Tick-Zustände
- unsichere Fallback-Werte

## Persistenz-Regel

[ ] Dieses Feature schreibt nicht in state.json.

oder:

[ ] Dieses Feature schreibt in state.json, deshalb braucht es Save-Failure-Schutz.

Wenn Save fehlschlägt:

- keine Erfolgsmeldung
- klare Warnung
- kein Dirty fälschlich löschen
- kein Runtime/state.json-Drift verstecken

────────────────────────────
6. Logic-Error-Check
────────────────────────────

## Persistence / state.json

Kann das Feature ...

[ ] entityRef speichern?
[ ] Runtime-Navigation speichern?
[ ] Door-Runtime speichern?
[ ] Save-Failure als Erfolg melden?
[ ] Dirty nach Save-Failure löschen?
[ ] Partial-Load überschreiben?
[ ] currentPosition ohne worldId als sichere Wahrheit nutzen?

Wenn ja:
STOP. Feature splitten oder Safety-Fix planen.

## Lifecycle / Relink / Respawn

Kann das Feature ...

[ ] zweiten NPC für denselben Record erzeugen?
[ ] MISSING_ENTITY falsch zu NEEDS_RELINK machen?
[ ] DISABLED ignorieren?
[ ] AMBIGUOUS ignorieren?
[ ] Auto-Respawn vor Relink starten?
[ ] Auto-Respawn ohne Chunk-Gate starten?
[ ] ACTIVE ohne UUID-/Ownership-Prüfung setzen?
[ ] UUID-lose Entity ACTIVE machen?

Wenn ja:
STOP. Erst Safety-Design ergänzen.

## Marker

Kann das Feature ...

[ ] Marker aus falscher worldId zuweisen?
[ ] MarkerType nicht prüfen?
[ ] Marker beim Restart automatisch ersetzen?
[ ] read-only Resolver mutieren lassen?
[ ] Marker bei MISSING_ENTITY löschen?
[ ] Marker bei NEEDS_RELINK löschen?
[ ] getNextAvailable wieder als Tick-/Restore-Fallback nutzen?
[ ] markerAssignments heimlich im Diagnosepfad ändern?

Wenn ja:
STOP. Marker-Regeln härten.

## Commands

Kann das Feature ...

[ ] Erfolg melden trotz Save-Failure?
[ ] Erfolg melden trotz Rollback-Failure?
[ ] Dry-run verändern lassen?
[ ] gefährliche Aktion ohne --force erlauben?
[ ] Record löschen, obwohl Entity-Removal unsicher ist?
[ ] Entity entfernen, aber Record unsicher behalten?

Wenn ja:
STOP. Command-Step kleiner planen.

## Hytale API / Engine

Kann das Feature ...

[ ] roleName als Keystone-ID missbrauchen?
[ ] setRoleName("KeystoneNPC_...") verwenden?
[ ] eigene Fake-Pathfinding-Engine bauen?
[ ] lineare Transform-Bewegung als Hauptnavigation nutzen?
[ ] Door-State direkt hacken?
[ ] Hytale-API-Verhalten raten statt prüfen?

Wenn ja:
STOP. Erst API prüfen.

────────────────────────────
7. Widerspruchs-Check
────────────────────────────

Prüfe:

[ ] Widerspricht das Feature AGENTS.md?
[ ] Widerspricht es safety/npc_restart_relink_control.md?
[ ] Widerspricht es safety/json_hierarchy.md?
[ ] Widerspricht es dem Lagebericht?
[ ] Widerspricht es neueren Patchreports?
[ ] Nutzt es alte Doku als Wahrheit?
[ ] Öffnet es bereits gefixte Fehler wieder?

## Ergebnis

[ ] kein Widerspruch
[ ] nur Doku-Drift
[ ] echter Regelkonflikt
[ ] Codeprüfung nötig

Bei echtem Konflikt:

- nicht implementieren
- keine Datei ändern
- Konflikt melden
- Entscheidung abwarten

────────────────────────────
8. Risiko-Einstufung
────────────────────────────

Risiko:

[ ] P0 — kann Daten zerstören / Duplicates / Ghost-NPCs erzeugen
[ ] P1 — kann state.json, Marker, Commands oder Lifecycle falsch machen
[ ] P2 — Architektur-/Wartbarkeitsrisiko
[ ] P3 — Komfort, Optik, kleine Verbesserung

## Warum dieses Risiko?

[Kurze Begründung]

## Darf das Feature jetzt umgesetzt werden?

[ ] ja
[ ] nein, erst Safety-Fix nötig
[ ] nur PLAN Mode
[ ] nur Doku
[ ] nur Prototyp ohne Runtime-Anbindung

────────────────────────────
9. Kompatibilität
────────────────────────────

Muss alte state.json weiter funktionieren?

[ ] ja
[ ] nein, neues Testprojekt / frischer Start

Müssen Legacy-Felder bleiben?

[ ] ja
[ ] nein

Falls ja, welche?

- bedMarkerId
- workMarkerId
- doorMarkerId
- foodMarkerId
- chestMarkerId
- chillMarkerId
- andere: [...]

## Migrationsregel

[ ] keine Migration nötig
[ ] Migration später
[ ] Migration nur per Admin-Befehl
[ ] Migration nur mit --dry-run
[ ] Migration nur mit Backup
[ ] Migration nie automatisch beim Load

Wichtig:

state.json darf beim Load nicht automatisch umgeschrieben werden.

────────────────────────────
10. Kleinste sichere Umsetzungsschritte
────────────────────────────

Feature in Mini-Steps splitten.

Jeder Step darf nur ein klares Ziel haben.

## Step 1 — [Name]

Ziel:
[...]

Erlaubte Dateien:
- [...]

Nicht ändern:
- [...]

Erwartete Änderung:
[...]

Compile/Test:
[...]

Review-Frage:
[...]

## Step 2 — [Name]

Ziel:
[...]

Erlaubte Dateien:
- [...]

Nicht ändern:
- [...]

Erwartete Änderung:
[...]

Compile/Test:
[...]

Review-Frage:
[...]

## Step 3 — [Name]

Ziel:
[...]

Erlaubte Dateien:
- [...]

Nicht ändern:
- [...]

Erwartete Änderung:
[...]

Compile/Test:
[...]

Review-Frage:
[...]

## Step 4 — [Name]

Ziel:
[...]

Erlaubte Dateien:
- [...]

Nicht ändern:
- [...]

Erwartete Änderung:
[...]

Compile/Test:
[...]

Review-Frage:
[...]

## Final Step — Safety-Doku und Patchreport

Erst nach PASS aller vorherigen Steps.

Aufgaben:
- Safety-Doku prüfen
- Widersprüche prüfen
- nur nötige Doku aktualisieren
- Patchreport schreiben
- keine neuen Features

────────────────────────────
11. Step-Splitting-Regel
────────────────────────────

Ein Step ist zu groß, wenn er gleichzeitig:

[ ] state.json ändert
[ ] Commands ändert
[ ] Tick/Routine ändert
[ ] Marker ändert
[ ] Relink/Respawn ändert
[ ] Navigation/Door ändert
[ ] JSON-Loader ändert
[ ] Safety-Doku ändert

Wenn mehr als 2 davon zutreffen:

Step splitten.

Beispiel:

Schlecht:
Marker-v2 komplett einbauen.

Gut:
1. Modell ergänzen
2. Resolver read-only lesen lassen
3. Command schreibt bewusst
4. Remove/Clear anpassen
5. Migration später
6. Doku/Patchreport

────────────────────────────
12. Testplan
────────────────────────────

## Pflicht bei Java-/Ressourcen-Änderungen

mvn -q -DskipTests test-compile

## Manuelle Tests

[ ] Server startet
[ ] NPC spawn funktioniert
[ ] state.json enthält nur stabile Daten
[ ] kein entityRef in state.json
[ ] kein Runtime-Navigation-State in state.json
[ ] Save-Failure wird gemeldet
[ ] Dry-run verändert nichts
[ ] Marker worldId wird geprüft
[ ] MarkerType wird geprüft
[ ] MISSING_ENTITY bleibt sicher
[ ] NEEDS_RELINK bleibt sicher
[ ] DISABLED bleibt sicher
[ ] kein Auto-Respawn bei AMBIGUOUS
[ ] kein dynamisches setRoleName
[ ] kein Tick-Log-Spam

## Feature-spezifische Tests

- [Test 1]
- [Test 2]
- [Test 3]

────────────────────────────
13. Rollback-Plan
────────────────────────────

Wenn der Step fehlschlägt:

[ ] keine weiteren Steps starten
[ ] Review FAIL ausgeben
[ ] nur diesen Step fixen
[ ] keine angrenzenden Systeme ändern
[ ] keine neuen Features einbauen
[ ] neue unabhängige Fehler nur ins Backlog
[ ] Compile erneut ausführen
[ ] erneutes Review

## Was darf zurückgerollt werden?

- [...]

## Was darf NICHT gelöscht werden?

- state.json ohne Backup
- NPC-Records bei unsicherer Entity-Removal
- Marker bei MISSING_ENTITY
- Marker bei NEEDS_RELINK
- fremde Marker anderer NPCs
- alte Daten bei Partial-Load

────────────────────────────
14. Review-Gate
────────────────────────────

Nach jedem Step prüfen:

[ ] Scope eingehalten
[ ] nur erlaubte Dateien geändert
[ ] keine Fremdänderungen bewertet
[ ] Ziel erfüllt
[ ] keine Ersatzfixes eingebaut
[ ] kein Marker-v2 nebenbei
[ ] kein großer Refactor
[ ] keine Safety-Regel verletzt
[ ] keine Runtime-Daten persistiert
[ ] Save-Failure sichtbar
[ ] Rollback ehrlich gemeldet
[ ] Marker read-only bleibt read-only
[ ] Commands melden keinen falschen Erfolg
[ ] Compile grün
[ ] nächster Step sicher

Urteil:

[ ] PASS
[ ] FAIL
[ ] PARTIAL
[ ] NOOP / bereits erledigt

Nächster Step erlaubt?

[ ] ja
[ ] nein

────────────────────────────
15. Backlog
────────────────────────────

Dinge, die NICHT in dieses Feature gehören:

| Risiko | Thema | Warum nicht jetzt? | Späterer Step |
|---|---|---|---|
| P1/P2/P3 | [...] | [...] | [...] |

Regel:

Backlog wird nicht nebenbei umgesetzt.

────────────────────────────
16. Fertig-Kriterium
────────────────────────────

Das Feature gilt erst als fertig, wenn:

[ ] alle geplanten Steps PASS haben
[ ] keine offenen FAILs existieren
[ ] Compile grün ist
[ ] keine direkten Nebenwirkungen offen sind
[ ] Safety-Doku geprüft wurde
[ ] Patchreport geschrieben wurde
[ ] bekannte Restpunkte im Backlog stehen
[ ] Marker-v2 / Legacy / Runtime / state.json sauber eingeordnet sind

────────────────────────────
17. Kurzentscheidung
────────────────────────────

Dieses Feature ist aktuell:

[ ] sofort umsetzbar
[ ] nur in Mini-Steps umsetzbar
[ ] nur PLAN Mode
[ ] blockiert durch Regelkonflikt
[ ] blockiert durch offenen Safety-Fix
[ ] Backlog / später

Begründung:

[...]