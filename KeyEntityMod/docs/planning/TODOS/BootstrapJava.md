
///////////////////////////////////////////////////////////////////////////
////////////////////////////  LOAD / SAVE  ////////////////////////////////
///////////////////////////////////////////////////////////////////////////


ALLE folgenden Punkte fuer prepareWorldSystem();



1. worldKey-Check später ergänzen
TODO: Sobald PersistedWorldState einen eigenen worldKey oder worldUuid speichert, muss dieser gegen den erwarteten Storage-Key geprüft werden.
Bei mismatch: Load failed, kein Save, kein Auto-Repair, kein Überschreiben mit Default-State.

2. Anti-Datenverlust in WorldStateToJson() einbauen
TODO: Solange echtes NPC-Encoding fehlt, darf WorldStateToJson() non-empty NpcRecords nicht als "npcs": [] speichern.
Wenn NPCs vorhanden sind, muss Save blockieren oder failed zurückgeben.

3. Save über geladene States statt Engine-Worlds
TODO: saveWorldStateSafely() soll nicht über worldManager.worldKeys() speichern, sondern über die wirklich geladenen loadedWorldState.keySet().
Am besten macht das WorldStateStore.saveAllLoadedWorlds() intern.

4. World-Load-Timing prüfen
TODO: Prüfen, ob Universe.get().getWorlds() in setup() schon zuverlässige Worlds liefert oder ob der Load erst nach AllWorldsLoadedEvent passieren darf.
Wenn die Worlds zu früh leer sind, darf loadWorldState() nicht still erfolgreich durchlaufen.

5. Backup-Load sichtbar machen
TODO: Wenn ein Backup statt der normalen state.json geladen wird, muss das als Warnzustand sichtbar sein.
Backup-Success darf nicht komplett wie ein normaler Load wirken, sonst bleibt eine kaputte Hauptdatei unbemerkt.

6. Leere Default-state.json vereinheitlichen
TODO: emptyWorldStateJson() sollte später dieselbe Grundstruktur haben wie normal gespeicherte States.
Wenn du langfristig worldKey in der JSON speicherst, muss auch die leere Startdatei dieses Feld enthalten.

7. Regex-JSON nur als Übergang behandeln
TODO: Die aktuelle Regex-Prüfung ist nur für das Skeleton mit leerem "npcs": [] okay.
Sobald echte NPC-Daten kommen, muss ein echter JSON-Parser/Codec verwendet werden.

8 TODO Phase später / DataStore<T>-Migration:
Beim Laden eines PersistedWorldState in loadWorld prüfen,
ob der gespeicherte interne worldKey/worldUuid
zum erwarteten Storage-Key passt.

Grund:
Wenn eine state.json oder ein DataStore-Eintrag versehentlich kopiert, verschoben oder falsch
zugeordnet wird, darf der Inhalt nicht still als Zustand einer anderen Welt akzeptiert werden.

Regel:
- Storage-Key / DataStore-Key bleibt die primäre Adresse.
- Wenn PersistedWorldState später ein worldKey/worldUuid-Feld enthält:
  expectedWorldKey == persistedWorldState.worldKey()
- Bei mismatch:
  Load failed
  kein Save
  kein Auto-Repair
  kein Überschreiben mit leerem Default-State