Du arbeitest an meiner Hytale-Mod „NPCMod / KeystoneNPC“.

MODUS:
Strenger Datei-Review + kleiner Fix, falls nötig.

ZU PRÜFENDE DATEI:
<DATEIPFAD_HIER_EINTRAGEN>

ZIEL:
Prüfe diese eine Datei extrem kritisch auf:

1. Null-Fehler
2. Blank-/Empty-Fehler
3. falsche Optional-Nutzung
4. falsche Map/List-Mutation
5. stille gefährliche Ersetzungen
6. fehlende Failchecks
7. Compile-Probleme
8. Logikfehler
9. falsche Runtime/Persistence-Trennung
10. spätere Integrationsprobleme mit NpcServices, NpcStateStore, NpcManager, WorldManager oder Marker-v2

WICHTIG:
- Nur diese Datei prüfen und ändern.
- Keine neuen Features einbauen.
- Keine großen Refactors.
- Keine Git-Befehle verwenden.
- Kein git diff.
- Kein git stash.
- Kein git restore.
- Kein git reset.
- Kein Commit.
- Keine Repository-Verwaltungsbefehle.
- Keine Hytale-API erraten.
- Wenn Hytale-API nötig ist: patcher.zip und hytalemodding.dev als Wahrheit nutzen.
- Wenn API unklar ist: nicht implementieren, sondern TODO / BLOCKER markieren.

PFLICHT-SAFETY:
Prüfe besonders:

- Darf diese Datei persistente Daten enthalten?
- Darf diese Datei Runtime-Daten enthalten?
- Wird entityRef irgendwo gespeichert? Falls ja: FAIL.
- Wird ein Hytale Entity-Objekt gespeichert? Falls ja: FAIL.
- Wird Runtime-Navigation gespeichert? Falls ja: FAIL.
- Kann null in Konstruktoren, Setter oder Methoden kommen?
- Können leere Strings reinkommen?
- Können Collections von außen verändert werden?
- Gibt es stille Ersetzung bestehender Daten ohne Warnung?
- Kann ein Save-Fehler als Erfolg behandelt werden?
- Kann ein Load-Fehler später state.json überschreiben?
- Kann Runtime-State nach Restart versehentlich als aktiv gelten?
- Kann ein NPC ACTIVE sein, obwohl keine gültige Runtime-Entity existiert?
- Gibt es fehlende Checks für worldKey/worldId?
- Gibt es fehlende Checks für markerId / MarkerType / worldKey?
- Gibt es TODOs, die vor Integration Pflicht sind?

DATEI-KOMMENTARE:
Falls du Methoden ergänzt oder änderst:
- Über jeder Methode muss ein kurzer Kommentar stehen.
- Oben nach den Imports muss ein File-Kommentar stehen.
- Kommentare sollen für Anfänger verständlich erklären, was passiert.

FIX-REGEL:
Wenn du einen Fehler findest:
- Fixe nur diesen Fehler in dieser Datei.
- Keine angrenzenden Systeme ändern.
- Keine neuen Klassen erstellen, außer absolut nötig und vorher als BLOCKER melden.
- Keine Methoden löschen, wenn unklar ist, ob sie später gebraucht werden.
- Keine Logik durch Fake-Implementierung vortäuschen.

INTEGRATIONSPRÜFUNG:
Prüfe, ob diese Datei später sauber zusammenpasst mit:

- NpcServices
- NpcStateStore
- WorldStateStore
- StateLoadResult / StateSaveResult
- NpcManager
- NpcRecord
- RuntimeNpc
- PersistedWorldState
- WorldManager / WorldKey
- MarkerRegistry / MarkerAssignment

Besonders prüfen:
- Gibt diese Datei Daten zurück, die andere Services sicher benutzen können?
- Wird ein Fehlerzustand klar zurückgegeben?
- Wird nicht einfach null zurückgegeben, wenn ein Result-Objekt sinnvoller wäre?
- Gibt es klare Grenzen zwischen persistent und runtime?

COMPILE-GATE:
Nach dem Fix ausführen:

mvn -q -DskipTests test-compile

Wenn Compile nicht möglich ist:
- klar sagen: nicht geprüft
- Grund nennen

AUSGABEFORMAT:

1. Urteil:
PASS / FAIL / PARTIAL

2. Geprüfte Datei:
<DATEIPFAD>

3. Gefundene Probleme:
- ...

4. Durchgeführte Fixes:
- ...

5. Nicht gefixt / bewusst offen:
- ...

6. Safety-Check:
- Runtime/Persistence getrennt: ja/nein
- Nullchecks geprüft: ja/nein
- Blank-Checks geprüft: ja/nein
- Save-/Load-Risiko geprüft: ja/nein
- Hytale-API nötig: ja/nein
- Hytale-API geprüft: ja/nein/nicht nötig

7. Integration:
- Passt zu NpcServices: ja/nein
- Passt zu NpcStateStore: ja/nein
- Passt zu NpcManager: ja/nein
- Spätere Risiken: ...

8. Compile:
- Ergebnis von mvn -q -DskipTests test-compile

9. Nächster empfohlener kleiner Step:
- ...