# Phase 2 — Detailplan zur Umsetzung
# Thema: State-System stabilisieren

## Kurzantwort

Ja, diese 8 Dateien sind die eigentlichen Phase-2-Pflichtdateien:

1. state/NpcStateStore.java
2. state/StateLoadResult.java
3. state/StateSaveResult.java
4. state/internal/WorldStateStore.java
5. state/internal/StatePathResolver.java
6. state/internal/StateFileIO.java
7. state/internal/StateJsonCodec.java
8. state/internal/StateBackupStore.java

Zusätzlich wichtig, aber keine neue Phase-2-Datei:

- model/PersistedWorldState.java

Warum?
StateLoadResult soll geladene PersistedWorldState-Daten zurückgeben.
StateJsonCodec soll JSON später in PersistedWorldState umwandeln.
WorldStateStore soll pro Welt PersistedWorldState merken.

Also:
Die 8 State-Dateien sind Phase 2.
PersistedWorldState ist Phase-1-Modell, wird aber in Phase 2 benutzt.

────────────────────────────────────────
PHASE-2-ZIEL IN EINFACHER SPRACHE
────────────────────────────────────────

Phase 2 soll nur klären:

Wie laden und speichern wir state.json sicher?

Noch NICHT bauen:

- kein echter Hytale Spawn
- kein EntityRef
- kein Relink
- kein ChunkGate
- kein Worldgen
- keine Marker-v2-Runtime
- keine Commands
- keine Routine
- keine Navigation
- keine Action
- kein Combat

Phase 2 ist nur:
Datei lesen.
JSON prüfen.
Java-State daraus machen.
Java-State speichern.
Fehler ehrlich melden.
Kaputte state.json niemals überschreiben.

────────────────────────────────────────
AKTUELLER STAND AUS DEINEM CODE
────────────────────────────────────────

Du hast die Phase-2-Dateien bereits angelegt.

Gut vorhanden:

- NpcStateStore existiert
- StateLoadResult existiert
- StateSaveResult existiert
- WorldStateStore existiert
- StatePathResolver existiert
- StateFileIO existiert
- StateJsonCodec existiert
- StateBackupStore existiert

Hauptproblem aktuell:

WorldStateStore arbeitet noch stark mit rohem JSON:

- Map<String, String> loadedWorldJson

Für Phase 2 besser:

- Map<String, PersistedWorldState> loadedWorldStates

Warum?
Phase 2 sagt:
StateLoadResult soll geladene PersistedWorldState enthalten.

────────────────────────────────────────
GESAMTREIHENFOLGE FÜR PHASE 2
────────────────────────────────────────

Sichere Reihenfolge:

Step 2.0 — Phase-2-Precheck
Step 2.1 — StateSaveResult härten
Step 2.2 — StatePathResolver final als Skeleton absichern
Step 2.3 — StateFileIO härten
Step 2.4 — StateBackupStore härten
Step 2.5 — StateJsonCodec auf PersistedWorldState vorbereiten
Step 2.6 — WorldStateStore von raw JSON auf PersistedWorldState umstellen
Step 2.7 — NpcStateStore Dirty-/Load-/Save-Verhalten prüfen
Step 2.8 — Phase-2-Failcheck-Review
Step 2.9 — Compile-Gate

Wichtig:
Immer nur einen Step umsetzen.
Danach Review.
Bei FAIL nur diesen Step fixen.

────────────────────────────────────────
STEP 2.0 — PRECHECK
────────────────────────────────────────

Ziel:
Vor dem Ändern prüfen, ob Phase 1 stabil genug ist.

Prüfen:

- Existiert PersistedWorldState?
- Existiert NpcRecord?
- PersistedWorldState enthält keine Runtime-Daten?
- NpcRecord enthält kein entityRef?
- RuntimeNpc wird nicht in state.json geschrieben?
- worldKey ist der zentrale Welt-Schlüssel?

Erlaubte Änderungen:
Keine.

Ergebnis:
Nur Bericht.

PASS wenn:

- Phase-1-Modelle reichen, um state.json pro Welt vorzubereiten.

FAIL wenn:

- PersistedWorldState noch gar nicht sinnvoll nutzbar ist.
- NpcRecord Runtime-Daten enthält.
- state.json RuntimeNpc speichern würde.

────────────────────────────────────────
STEP 2.1 — StateSaveResult.java härten
────────────────────────────────────────

Datei:
state/StateSaveResult.java

Ziel:
Save-Ergebnis muss immer ehrlich sein.

Aktueller Sinn:
StateSaveResult sagt:
Hat Speichern geklappt oder nicht?

Umsetzen:

1. Compact Constructor ergänzen.
2. message darf nicht null sein.
3. message darf nicht blank sein.
4. success(...) muss nur success=true bauen.
5. failed(...) muss nur success=false bauen.
6. Keine Exception beim normalen Save-Fehler werfen.
7. Save-Fehler bleiben als failed Result sichtbar.

Warum wichtig?
Andere Klassen prüfen später:

result.success()

Wenn message kaputt ist oder Fehler als Erfolg zurückkommen, kann state.json falsch behandelt werden.

Nicht tun:

- keine Runtime-Logik
- keine Datei-Logik
- keine JSON-Logik
- keine Hytale-API

Failchecks:

- StateSaveResult.failed("x").success() == false
- StateSaveResult.success("x").success() == true
- null message wird blockiert
- blank message wird blockiert

Review-Frage:
Kann ein Save-Fehler irgendwo als Erfolg durchrutschen?

────────────────────────────────────────
STEP 2.2 — StatePathResolver.java final als Skeleton absichern
────────────────────────────────────────

Datei:
state/internal/StatePathResolver.java

Ziel:
Pfade sicher vorbereiten.

Aktueller Sinn:
StatePathResolver entscheidet:
Wo liegt state.json?

Umsetzen:

1. worldId/worldKey darf nicht null/blank sein.
2. sanitizeWorldId bleibt streng.
3. Pfad darf nicht aus worldsDir ausbrechen.
4. baseDir = Path.of("keystone-npc") bleibt erlaubt, aber klarer TODO-Kommentar:
   "Skeleton-Pfad. Später durch echten Plugin-Datenpfad ersetzen."
5. prepareBaseDirectories darf Fehler nicht still schlucken.
6. worldDirectory darf bei Fehler nicht fake-erfolgreich sein.

Warum wichtig?
Ein kaputter Pfad darf nicht versehentlich irgendwo außerhalb des Mod-Ordners schreiben.

Nicht tun:

- keinen echten Hytale Plugin-Datenpfad erraten
- keine Hytale API verwenden
- kein Worldgen
- kein WorldManager einbauen

Failchecks:

- worldId leer -> Fehler
- "../test" darf nicht ausbrechen
- Pfad bleibt unter keystone-npc/worlds/
- harter Pfad ist als Skeleton-TODO markiert

Review-Frage:
Kann eine falsche worldId außerhalb des State-Ordners schreiben?

────────────────────────────────────────
STEP 2.3 — StateFileIO.java härten
────────────────────────────────────────

Datei:
state/internal/StateFileIO.java

Ziel:
Datei lesen/schreiben ohne kaputte halbe state.json.

Aktueller Sinn:
StateFileIO macht nur Dateioperationen.

Umsetzen:

1. readString(Path file) bleibt einfach:
   - null oder nicht vorhanden -> null
   - IOException -> null + Fehlermeldung

2. writeAtomic(Path file, String content) härten:
   - file null -> false
   - content null -> false
   - parent folder erstellen
   - zuerst .tmp schreiben
   - dann move auf state.json
   - wenn ATOMIC_MOVE nicht geht, normal ersetzen
   - bei Fehler false zurückgeben

3. Optional sinnvoll:
   - Wenn write fehlschlägt, tempFile best effort löschen.
   - Aber Fehler beim temp cleanup darf nicht den eigentlichen Fehler verstecken.

Warum wichtig?
state.json darf nicht halb geschrieben werden.

Nicht tun:

- kein JSON prüfen
- kein PersistedWorldState kennen
- keine NPC-Logik
- keine Backup-Logik hier reinziehen

Failchecks:

- writeAtomic(null, "...") -> false
- writeAtomic(file, null) -> false
- IOException -> false
- keine Exception nach außen bei normalem IO-Fehler
- StateFileIO kennt keine NPC-Klassen

Review-Frage:
Kann ein Schreibfehler als Erfolg zurückkommen?

────────────────────────────────────────
STEP 2.4 — StateBackupStore.java härten
────────────────────────────────────────

Datei:
state/internal/StateBackupStore.java

Ziel:
Vor dem Überschreiben einer existierenden state.json Backup machen.

Aktueller Sinn:
StateBackupStore erstellt Sicherungskopien.

Umsetzen:

1. backupBeforeSave(worldId, stateFile) bleibt Pflicht vor Save.
2. Wenn keine state.json existiert:
   - success: Backup skipped
3. Wenn stateFile existiert, aber keine reguläre Datei ist:
   - failed
4. Wenn Backup nicht erstellt werden kann:
   - failed
5. Backup-Fehler blockiert Save.
6. Backup-Dateinamen dürfen sich nicht überschreiben.
7. worldId wird sanitized.

Warum wichtig?
Wenn Save kaputtgeht, soll alte state.json nicht verloren sein.

Nicht tun:

- keine Migration
- kein Restore-System
- keine Admin-Commands
- keine Marker-Reparatur

Failchecks:

- fehlende state.json -> Backup skipped, Save darf weitergehen
- vorhandene state.json -> Backup wird erstellt
- Backup-Fehler -> Save muss blockieren
- Backup überschreibt kein altes Backup

Review-Frage:
Kann Save weiterlaufen, obwohl Backup nötig war und fehlschlug?

────────────────────────────────────────
STEP 2.5 — StateJsonCodec.java auf PersistedWorldState vorbereiten
────────────────────────────────────────

Datei:
state/internal/StateJsonCodec.java

Ziel:
JSON nicht nur grob prüfen, sondern mit PersistedWorldState verbinden.

Aktueller Stand:
StateJsonCodec arbeitet noch mit raw JSON.

Phase-2-Ziel:
StateJsonCodec soll mindestens diese Richtung können:

- PersistedWorldState -> JSON
- JSON -> PersistedWorldState

Umsetzen:

1. Neue Methode planen/ergänzen:

   encodeWorldState(PersistedWorldState worldState)

   Aufgabe:
   - worldState darf nicht null sein
   - worldKey aus worldState schreiben
   - version schreiben
   - npcs schreiben
   - markers erstmal als leeres Array behalten, falls Marker-v2 noch nicht Phase 2 ist

2. Neue Methode planen/ergänzen:

   decodeWorldState(String worldKey, String json)

   Aufgabe:
   - worldKey prüfen
   - json darf nicht null/blank sein
   - JSON-Grundsyntax prüfen
   - version muss existieren
   - npcs muss existieren
   - markers darf existieren, aber Marker-Runtime noch nicht ausbauen
   - Ergebnis ist PersistedWorldState

3. Für jetzt minimal erlaubte Skeleton-Variante:

   - Wenn npcs leer ist:
     -> neuer PersistedWorldState(worldKey)

   - Wenn npcs später echte Records enthält:
     -> entweder sauber parsen oder als "noch nicht unterstützt" failed zurückgeben

4. Wichtig:
   Kaputtes JSON darf niemals zu leerem Default-State werden.

Warum wichtig?
Wenn kaputtes JSON einfach als leerer State behandelt wird, kann echte state.json überschrieben werden.

Nicht tun:

- keine große JSON-Library hinzufügen, außer bewusst geplant
- keine Marker-v2-Logik
- keine StructureInstance-Logik
- keine Runtime-Felder serialisieren
- kein entityRef
- kein Entity-Objekt
- keine aktive Navigation
- keine Action Runtime

Failchecks:

- leeres JSON -> Fehler
- ungültiges JSON -> Fehler
- fehlende version -> Fehler
- fehlende npcs -> Fehler
- kaputtes JSON erzeugt keinen leeren PersistedWorldState
- Runtime-Daten werden nie geschrieben
- encode/decode arbeiten mit PersistedWorldState

Review-Frage:
Kann eine kaputte state.json in einen leeren gültigen State verwandelt werden?

────────────────────────────────────────
STEP 2.6 — WorldStateStore.java von raw JSON auf PersistedWorldState umstellen
────────────────────────────────────────

Datei:
state/internal/WorldStateStore.java

Ziel:
WorldStateStore merkt echte geladene Welt-States, nicht nur Strings.

Aktueller Risikopunkt:
Map<String, String> loadedWorldJson

Besser für Phase 2:
Map<String, PersistedWorldState> loadedWorldStates

Umsetzen:

1. Feld ändern:

   Von:
   loadedWorldJson

   Zu:
   loadedWorldStates

2. loadWorld(worldId):

   Fall A: state.json existiert nicht
   - neuen PersistedWorldState(worldId) erstellen
   - in loadedWorldStates merken
   - StateLoadResult.success(message, worldState) zurückgeben
   - Noch NICHT automatisch speichern

   Wichtig:
   "Kein state.json gefunden" ist kein Load-Failure.
   Aber es darf nicht sofort ungefragt state.json geschrieben werden.

3. loadWorld(worldId):

   Fall B: state.json existiert
   - Datei lesen
   - wenn readString null -> StateLoadResult.failed
   - JSON decodeWorldState(...)
   - wenn decode fehlschlägt -> StateLoadResult.failed
   - NICHT loadedWorldStates überschreiben
   - NICHT default state speichern
   - bei Erfolg loadedWorldStates.put(...)
   - StateLoadResult.success(message, worldState)

4. saveWorld(worldId):

   - worldId prüfen
   - nur speichern, wenn worldId in loadedWorldStates existiert
   - wenn nicht geladen -> failed
   - PersistedWorldState aus Map holen
   - jsonCodec.encodeWorldState(...)
   - wenn encode fehlschlägt -> failed
   - stateFile auflösen
   - backupBeforeSave
   - wenn Backup fail -> failed
   - fileIO.writeAtomic
   - wenn write fail -> failed
   - success zurückgeben

5. saveAllLoadedWorlds():

   - über Kopie der worldIds laufen
   - bei erstem Fehler abbrechen
   - failed Result zurückgeben
   - nur wenn alle Save erfolgreich: success

6. putRawWorldJson(worldId, json):

   Entscheidung:
   Entweder entfernen/entschärfen oder intern decodeWorldState nutzen.

   Sicherer:
   - Methode behalten, falls Tests/Debug sie nutzen
   - aber nicht raw String speichern
   - JSON validieren und in PersistedWorldState umwandeln
   - dann loadedWorldStates.put(...)

Warum wichtig?
Phase 2 will echte geladene PersistedWorldState-Daten.
Raw JSON ist zu schwach, weil NpcManager später Java-Objekte braucht.

Nicht tun:

- kein NpcManager restore hier
- kein Spawn beim Load
- kein Relink beim Load
- kein Save nach Load-Failure
- keine kaputte Welt als leer speichern
- keine RuntimeNpc speichern

Failchecks:

- Load-Failure überschreibt keine state.json
- Partial-Load wird nicht als Erfolg behandelt
- Save nicht möglich, wenn Welt nie geladen wurde
- loadWorld gibt StateLoadResult mit worldState zurück
- loadedWorldStates enthält keine raw JSON Strings
- WorldStateStore liest/schreibt nur State, keine Runtime

Review-Frage:
Kann eine kaputte state.json dazu führen, dass ein leerer State gespeichert wird?

────────────────────────────────────────
STEP 2.7 — NpcStateStore.java Dirty-/Load-/Save-Verhalten prüfen
────────────────────────────────────────

Datei:
state/NpcStateStore.java

Ziel:
Obere State-Tür bleibt ehrlich und einfach.

Aktueller Sinn:
NpcStateStore ist die öffentliche State-Schicht.

Umsetzen / prüfen:

1. loadState():

   - ruft worldStateStore.loadAllKnownWorlds()
   - bei Fehler dirty=false
   - kein Save
   - kein Spawn
   - kein Relink

2. loadWorldState(worldId):

   - ruft worldStateStore.loadWorld(worldId)
   - bei Fehler dirty=false
   - kein Save
   - kein Spawn
   - kein Relink

3. saveStateSafely():

   - wenn dirty=false -> true
   - wenn dirty=true -> saveAllLoadedWorlds()
   - nur bei success dirty=false
   - bei failure dirty=true
   - Fehler sichtbar loggen
   - false zurückgeben

4. saveWorldState(worldId):

   - ruft saveWorld(worldId)
   - bei failure dirty=true
   - bei success nicht automatisch global dirty=false, außer bewusst begründet
   - Fehler sichtbar loggen

5. markDirty():

   - setzt dirty=true

Warum wichtig?
Dirty bedeutet:
Es gibt Änderungen, die gespeichert werden müssen.

Dirty darf nur gelöscht werden, wenn Save wirklich erfolgreich war.

Nicht tun:

- kein NpcManager hier direkt einbauen
- kein RestoreRecords
- keine Commands
- keine Hytale-API
- keine RuntimeNpc

Failchecks:

- Save-Failure gibt false
- Dirty bleibt true nach Save-Failure
- Dirty wird nur nach erfolgreichem saveStateSafely false
- Load-Failure speichert nichts
- NpcStateStore speichert keine RuntimeNpc-Objekte

Review-Frage:
Kann dirty false werden, obwohl Save fehlgeschlagen ist?

────────────────────────────────────────
STEP 2.8 — Phase-2-Failcheck-Review
────────────────────────────────────────

Ziel:
Prüfen, ob Phase 2 abgeschlossen werden kann.

Review muss prüfen:

1. Load-Failure überschreibt keine state.json
2. Partial-Load wird nicht als Erfolg behandelt
3. Save-Failure gibt false / failed Result
4. Dirty wird nur nach echtem Save gelöscht
5. Keine Runtime-Daten im JSON-Modell
6. Pro Welt eigener State
7. StatePathResolver nutzt später Plugin-Datenpfad
8. Harte Pfade aktuell nur als Skeleton-TODO markiert

Zusätzlich prüfen:

- Kein Spawn beim Load
- Kein Relink beim Load
- Kein ChunkGate
- Kein Worldgen
- Kein Marker-v2 nebenbei
- Kein NpcManager restore in Phase 2
- Keine Hytale EntityRef in state.json
- Kein Entity object in state.json
- Keine aktive Navigation in state.json
- Keine Action Runtime in state.json

Ergebnis:

PASS:
Phase 2 darf als fertig gelten.

PARTIAL:
Nur kleine Punkte fehlen, z. B. Compile nicht geprüft oder TODO-Kommentar fehlt.

FAIL:
State kann kaputte JSON überschreiben, Save-Fehler wird verschluckt, Runtime wird gespeichert oder Load-Failure erzeugt Default-Save.

────────────────────────────────────────
STEP 2.9 — Compile-Gate
────────────────────────────────────────

Befehl:

mvn -q -DskipTests test-compile

Bewertung:

PASS:
Compile läuft sauber durch.

PARTIAL:
Code sieht logisch richtig aus, aber Compile wurde nicht ausgeführt.

FAIL:
Compile schlägt fehl.

Wichtig:
Ohne Compile sollte Phase 2 nicht final als fertig markiert werden.

────────────────────────────────────────
DATEI-FÜR-DATEI-PLAN
────────────────────────────────────────

## 1. NpcStateStore.java

Aufgabe:
Öffentliche Tür zum State-System.

Muss können:

- prepareBaseDirectories()
- loadState()
- loadWorldState(worldId)
- saveStateSafely()
- saveWorldState(worldId)
- markDirty()
- isDirty()

Wichtigste Regel:
Dirty darf nur nach echtem erfolgreichem Save verschwinden.

Nicht rein:

- NpcManager restore
- Spawn
- Relink
- Commands
- RuntimeNpc
- EntityRef

PASS wenn:

- Save-Failure sichtbar bleibt
- saveStateSafely false bei Fehler gibt
- load failure nichts speichert
- dirty korrekt bleibt

## 2. StateLoadResult.java

Aufgabe:
Ehrliches Load-Ergebnis.

Muss enthalten:

- success
- partial
- loadFailed
- message
- List<PersistedWorldState> worldStates

Wichtigste Regel:
Genau ein Status darf aktiv sein.

Nicht rein:

- Datei-Logik
- JSON-Logik
- Runtime-Logik

PASS wenn:

- success und failed nicht gleichzeitig möglich
- partial nicht als success gilt
- worldStates nie null ist
- message nie null/blank ist

## 3. StateSaveResult.java

Aufgabe:
Ehrliches Save-Ergebnis.

Muss enthalten:

- success
- message

Wichtigste Regel:
Save-Fehler dürfen nie als Erfolg gelten.

Nicht rein:

- Datei-Logik
- JSON-Logik
- Backup-Logik

PASS wenn:

- failed(...) immer success=false ist
- success(...) immer success=true ist
- message validiert wird

## 4. WorldStateStore.java

Aufgabe:
State pro Welt verwalten.

Muss können:

- loadAllKnownWorlds()
- loadWorld(worldId)
- saveWorld(worldId)
- saveAllLoadedWorlds()
- optional putRawWorldJson(worldId, json), aber sicher

Wichtigste Änderung:
Nicht mehr Map<String, String>.
Besser Map<String, PersistedWorldState>.

Nicht rein:

- NpcManager
- Spawn
- Relink
- ChunkGate
- Worldgen

PASS wenn:

- pro Welt eigener State
- Load-Failure überschreibt nichts
- Save nur für geladene Welt möglich
- StateLoadResult gibt PersistedWorldState zurück
- Save nutzt Backup vor Überschreiben

## 5. StatePathResolver.java

Aufgabe:
Sichere Pfade bauen.

Muss können:

- baseDir vorbereiten
- worldsDir vorbereiten
- backupsDir vorbereiten
- worldDirectory(worldId)
- stateFile(worldId)
- backupsDir()
- sanitizeWorldId(worldId)

Wichtigste Regel:
worldId darf nicht aus dem State-Ordner ausbrechen.

Nicht rein:

- NPC-Logik
- JSON-Logik
- Runtime-Logik
- echte Hytale-Pfade erraten

PASS wenn:

- worldId wird validiert
- Pfad bleibt unter worldsDir
- harter Skeleton-Pfad ist als TODO markiert

## 6. StateFileIO.java

Aufgabe:
Dateien lesen und atomar schreiben.

Muss können:

- exists(path)
- readString(path)
- writeAtomic(path, content)

Wichtigste Regel:
Schreibfehler dürfen nicht als Erfolg gelten.

Nicht rein:

- JSON-Prüfung
- Backup
- NPC-Logik

PASS wenn:

- writeAtomic false bei Fehler gibt
- temporäre Datei genutzt wird
- state.json nicht halb geschrieben wird

## 7. StateJsonCodec.java

Aufgabe:
JSON prüfen und umwandeln.

Muss Phase 2 können:

- JSON-Grundsyntax prüfen
- Pflichtfelder prüfen
- PersistedWorldState zu JSON schreiben
- JSON zu PersistedWorldState laden
- kaputtes JSON blockieren

Wichtigste Regel:
Kaputtes JSON darf niemals automatisch zu leerem Default-State werden.

Nicht rein:

- RuntimeNpc
- EntityRef
- Marker-v2-Resolver
- Structure-System
- Spawn
- Relink

PASS wenn:

- ungültiges JSON blockiert wird
- leeres JSON blockiert wird
- fehlende version blockiert wird
- fehlende npcs blockiert wird
- encode/decode mit PersistedWorldState arbeitet

## 8. StateBackupStore.java

Aufgabe:
Backups vor Save.

Muss können:

- backupBeforeSave(worldId, stateFile)
- eindeutigen Backup-Dateinamen erzeugen
- Backup-Fehler als StateSaveResult.failed melden

Wichtigste Regel:
Wenn Backup nötig ist und fehlschlägt, darf Save nicht weiterlaufen.

Nicht rein:

- Restore
- Migration
- Commands
- Worldgen

PASS wenn:

- vorhandene state.json wird gesichert
- fehlende state.json blockiert Save nicht
- Backup-Fehler blockiert Save
- alte Backups werden nicht überschrieben

────────────────────────────────────────
WAS FEHLT NICHT IN PHASE 2
────────────────────────────────────────

Diese Dinge NICHT jetzt ergänzen:

- NpcManager.restoreRecords(...)
- Bootstrap-Verbindung
- echte Welt-Erkennung
- WorldKey-System ausbauen
- ChunkGate
- Marker-v2-Assignment
- MarkerCleanup
- StructureInstance
- SpawnResult
- RelinkResult
- Runtime Tick
- Commands
- Hytale EntityRef
- Hytale Entity-Objekte
- echte API-Recherche für Spawn

Diese Sachen kommen später.

────────────────────────────────────────
MINIMALE DEFINITION VON "PHASE 2 FERTIG"
────────────────────────────────────────

Phase 2 ist fertig, wenn:

1. Alle 8 State-Dateien existieren.
2. StateLoadResult enthält PersistedWorldState.
3. StateSaveResult ist ehrlich und validiert message.
4. WorldStateStore speichert intern PersistedWorldState pro Welt.
5. StateJsonCodec kann PersistedWorldState minimal encode/decode.
6. Load-Failure überschreibt keine state.json.
7. Save-Failure gibt failed/false zurück.
8. Dirty wird nur nach echtem Save gelöscht.
9. Keine Runtime-Daten landen im JSON-Modell.
10. Pro Welt gibt es eigenen State.
11. Harte Pfade sind als Skeleton-TODO markiert.
12. Compile-Gate ist grün:

    mvn -q -DskipTests test-compile

────────────────────────────────────────
EMPFOHLENER ERSTER UMSETZUNGSSTEP
────────────────────────────────────────

Starte mit:

Step 2.1 — StateSaveResult.java härten

Warum?
Klein.
Sicher.
Wenig Risiko.
Gute Grundlage für alle Save-Pfade.

Danach:
Review nur für Step 2.1.
Erst bei PASS weiter zu Step 2.2.