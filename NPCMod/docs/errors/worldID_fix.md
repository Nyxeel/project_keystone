Du arbeitest an meiner Hytale-Mod „NPCMod / KeystoneNPC“.

MODUS:
Analyse + enger Safety-Fix, falls nötig.

Ziel:
Prüfe, ob state.json sauber zwischen Welten und verschiedenen Servern unterscheiden kann.

Wichtig:
Nicht Marker-v2 bauen.
Keine große Persistenz-Architektur umbauen.
Keine JSON-Hierarchie-Refactors.
Nur prüfen und sichere Schutzlogik planen/fixen.

============================================================
1. Prüfe Speicherort der state.json
============================================================

Prüfe:

- JsonFileStateStore
- KeystoneNpcPlugin
- plugin data folder
- Pfad zur state.json

Frage:
Wird pro Server eine eigene state.json verwendet,
oder teilen mehrere Server denselben Plugin-Datenordner?

Erwartung prüfen:

- Wenn jeder Server eigenen Datenordner hat:
  - state.json ist automatisch servergetrennt

- Wenn mehrere Server denselben Datenordner nutzen:
  - gleiche state.json wird geladen
  - NPCs/Marker können vermischt werden

Bericht klar ausgeben:

- aktueller state.json-Pfad
- ob serverId/serverName/saveName im Pfad enthalten ist
- ob serverId im JSON enthalten ist
- Risiko bei mehreren Serverinstanzen

============================================================
2. Prüfe worldId-Nutzung
============================================================

Prüfe, ob NPCs und Marker worldId speichern und nutzen.

Prüfe:

- NpcRecord
- MarkerRecord
- JsonFileStateStore
- MarkerRegistry
- Spawn
- Restore
- Relink
- MarkerSetCommand
- MarkerClearCommand
- remove/clear Marker-Cleanup

Ziel:

- NPC.worldId bleibt persistiert
- Marker.worldId bleibt persistiert
- Marker darf nur NPC zugewiesen werden, wenn worldId passt
- Marker-Cleanup darf keine Marker aus anderer worldId löschen
- Spawn/Respawn darf nicht ohne passende worldId arbeiten
- currentPosition ohne worldId reicht nicht als sichere Wahrheit

============================================================
3. Prüfe Server-Trennung
============================================================

Prüfe, ob state.json eine Server-Identität kennt.

Suche nach:

- serverId
- serverName
- serverUuid
- saveName
- worldSaveId
- universeId
- dataFolder namespace

Wenn nichts davon existiert:
Melde:

state.json unterscheidet aktuell nur über den Datenordner,
nicht über ein eigenes serverId-Feld.

Risiko:

- mehrere Server teilen denselben Ordner
- ein Testserver lädt NPCs vom anderen Server
- entityUuid passt nicht mehr
- worldId kann gleich heißen, aber andere Welt meinen
- Marker/NPCs werden falsch restored oder gelöscht

============================================================
4. Safe-by-default Verhalten prüfen
============================================================

Wenn worldId fehlt oder unklar ist:

Nicht erlaubt:

- NPC spawnen
- Marker automatisch ersetzen
- Marker löschen
- Record löschen
- state.json überschreiben

Erlaubt:

- warnen
- Diagnose ausgeben
- Recovery blockieren
- Admin-Repair verlangen

============================================================
5. Minimaler Fix, falls nötig
============================================================

Wenn keine Server-Trennung existiert, aber aktueller Scope keinen Umbau erlaubt:

Nicht sofort große Migration bauen.

Stattdessen:

- klaren Warnlog beim Start ergänzen, wenn kein server namespace existiert
- Dokumentation ergänzen
- Backlog-Punkt erstellen:
  state.json später unter server-/save-spezifischem Namespace speichern

Möglicher Zielzustand später:

keystone-npc/
  state/
    <serverId>/
      state.json

oder:

keystone-npc/
  worlds/
    <worldId>/
      state.json

Aber nicht sofort umsetzen, wenn dadurch bestehende Saves migriert werden müssten.

============================================================
6. Migration nur planen, nicht blind ausführen
============================================================

Falls serverId eingeführt werden soll:

Vorher planen:

- Wie wird serverId bestimmt?
- Ist serverId stabil über Restart?
- Was passiert mit alter state.json?
- Wird alte state.json kopiert oder verschoben?
- Gibt es Backup?
- Wird Migration atomisch gespeichert?
- Was passiert bei Fehler?
- Wird Load-Failure/Partial-Load blockiert?
- Wird Save-Failure geprüft?

Nicht erlaubt:

- alte state.json still verschieben
- alte state.json löschen
- leere neue state.json speichern und alte überschreiben
- Marker/NPCs wegen serverId-Mismatch automatisch löschen

============================================================
7. Compile-Gate
============================================================

Nach Java-Änderungen:

mvn -q -DskipTests test-compile

Abschlussbericht:

- PASS / FAIL / PARTIAL
- aktueller state.json-Pfad
- ob worldId persistiert wird
- ob worldId bei Marker-Zuweisung geprüft wird
- ob mehrere Server dieselbe state.json laden könnten
- ob serverId/saveName existiert
- ob Änderungen gemacht wurden
- ob Migration nötig ist
- ob Compile erfolgreich war
- Restgefahren