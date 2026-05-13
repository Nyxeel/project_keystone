Du arbeitest an meiner Hytale-Mod „NPCMod / KeystoneNPC“.

MODUS:
Safety-Fix vor Marker-v2.

Ziel:
Prüfe den zentralen Scheduler/Runner:
NpcRoutineRunner

Wichtig:
Der Name "scheduler" in Commands meint keinen echten Timer-Scheduler.
Er meint den zentralen NPC-Manager / NpcRoutineRunner.

NICHT ändern:
- kein Marker-v2
- keine neue markerAssignments-Hauptarchitektur
- keine Door-/Navigation-/Animation-/Role-Refactors
- kein neues Feature
- kein dynamisches setRoleName("KeystoneNPC_...")
- kein Role-Prefix-Fallback
- kein Blind-Respawn

============================================================
1. Prüfe zentrale Remove-Logik
============================================================

Prüfe:

- NpcRoutineRunner.removeNpc(...)
- NpcRoutineRunner.removeNpcByIndex(...)
- NpcRoutineRunner.clearNpcs(...)

Hauptfrage:
Kann removeNpc(...) eine Entity-Removal queuen, aber danach den Record behalten?

Gefährlicher Ablauf:

1. removeNpc(...) findet NPC.
2. Live-Entity wird entfernt oder Removal wird queued.
3. Removal-Ergebnis ist REMOVAL_QUEUED_UNCONFIRMED.
4. safeToDeleteRecord() ist false.
5. Record bleibt in npcs/state.json.
6. Entity verschwindet später trotzdem.

Das darf nicht passieren.

Ziel:
Kein Zustand, bei dem Entity gelöscht/queued ist, aber Record unklar weiterlebt.

Wenn Removal unsicher ist:
- kein falscher Erfolg
- kein Record blind löschen
- aber auch keine Entity halb entfernen und Record unverändert lassen
- klare Fehlermeldung / Status / Recovery-Strategie

============================================================
2. Boolean-Rückgaben ersetzen oder absichern
============================================================

Prüfe, ob removeNpc(...) nur boolean zurückgibt.

Problem:
boolean reicht nicht.

Man kann nicht unterscheiden:

- NPC nicht gefunden
- ungültiger Index
- Entity-Removal unsicher
- Record nicht gelöscht
- Rollback fehlgeschlagen
- Save fehlgeschlagen

Ziel:
Wenn möglich RemoveResult einführen oder bestehende Rückgabe so erweitern, dass Commands sauber unterscheiden können.

Mögliche Ergebnisse:

- REMOVED
- NOT_FOUND
- INVALID_INDEX
- BLOCKED_ENTITY_UNCONFIRMED
- BLOCKED_INVALID_REF
- BLOCKED_WORLD_MISSING
- SAVE_FAILED
- ROLLBACK_FAILED

Keine falsche Command-Meldung wie "Invalid NPC index", wenn eigentlich Removal blockiert wurde.

============================================================
3. NPC-owned Marker Cleanup zentral prüfen
============================================================

Marker-Cleanup gehört in den zentralen Remove-Pfad,
nicht dupliziert in jedem Command.

Wenn ein NPC wirklich gelöscht wird:
- eigene MarkerIds sammeln:
  - bedMarkerId
  - workMarkerId
  - foodMarkerId
  - chestMarkerId
  - doorMarkerId
  - chillMarkerId
- prüfen, ob ein anderer NPC denselben Marker noch benutzt
- nur unbenutzte eigene Marker löschen
- MarkerRegistry intern konsistent halten:
  - markers
  - orderedIdsByType
  - lastByType
  - activeMarkerIds

Marker NICHT löschen bei:

- MISSING_ENTITY
- NEEDS_RELINK
- Restart-Unsicherheit
- ungeladenem Chunk
- unsicherer Entity-Removal

Marker löschen nur bei echtem NPC-Delete:

- /knpc remove
- /knpc clear
- permanenter Tod, falls angebunden
- Spawn-Rollback, falls NPC wirklich entfernt wurde

============================================================
4. assignMarkerToNpc(...) prüfen
============================================================

Prüfe:

- NpcRoutineRunner.assignMarkerToNpc(...)

Problem:
Die Methode darf nicht "Reroute gestartet" als Rückgabe liefern,
wenn der Command "Assignment erfolgreich" erwartet.

Ziel:
Logisch trennen:

- Marker assignment erfolgreich?
- Marker wirklich geändert?
- Reroute gestartet?
- Save erfolgreich?

Zusätzlich prüfen:

- markerId existiert wirklich in MarkerRegistry
- markerId hat passenden MarkerType
- marker.worldId passt zu npc.worldId
- No-Op wird erkannt
- keine kaputte MarkerId in NPC-State schreiben

============================================================
5. Role-Prefix-Altlast prüfen
============================================================

Suche in NpcRoutineRunner und RelinkWorkflowService nach:

- tryRolePrefixRelinkEntityRef(...)
- evaluateRolePrefixRelinkEntityRefDetailed(...)
- KeystoneNPC_
- setRoleName(...)

Erwartung:

- kein aktiver Role-Prefix-Relink
- kein dynamisches setRoleName(...)
- wenn Code tot ist: entfernen oder klar deaktiviert dokumentieren
- keine Reaktivierung ohne sichere Metadata-/Component-API

============================================================
6. Active-Marker-Legacy prüfen
============================================================

Prüfe alte MVP-Logik:

- bindActiveMarkersByRole(...)
- markerRegistry.getActive(...)
- markerRegistry.clearActive(...)
- markerRegistry.clear(...)

Ziel:

- Spawn darf Marker nicht heimlich konsumieren
- Spawn darf Marker nicht global löschen
- clearActive darf nicht versehentlich fremde aktive Marker entfernen
- getNextAvailable darf nicht als Restore/Tick/Respawn-Fallback-Wahrheit genutzt werden

Wenn Legacy noch nötig:
- nicht hart löschen
- als Legacy/@Deprecated markieren
- keine neue Marker-v2-Struktur bauen

============================================================
7. Commands am Ende prüfen
============================================================

Commands nutzen den Scheduler/Runner.

Prüfe:

- NpcRemoveCommand
  -> scheduler.removeNpcByIndex(...)

- NpcClearCommand
  -> scheduler.clearNpcs(...)

- SpawnNpcCommand bei Save-Failure
  -> scheduler.removeNpc(npc.npcId())

Ziel:

- Commands sollen Marker-Cleanup nicht duplizieren
- Commands sollen zentralen Remove-Pfad nutzen
- Commands müssen RemoveResult prüfen
- kein Erfolg bei unsicherem Remove
- kein Erfolg bei Save-Failure
- SpawnNpcCommand darf nicht behaupten, Rollback sei erfolgreich, wenn scheduler.removeNpc(...) fehlgeschlagen ist

============================================================
8. Compile-Gate
============================================================

Nach jeder Änderung:

mvn -q -DskipTests test-compile

Abschlussbericht:

- PASS / FAIL / PARTIAL
- geänderte Dateien
- gefundene Scheduler-Logic-Errors
- gefundene Altlasten
- reine Failcheck-Lücken
- ob Marker-v2 unangetastet blieb
- ob Compile erfolgreich war
- Restgefahren



####



1. removeNpc(...) kann Entity-Removal anstoßen, aber den Record behalten

Gefährlichster Fehler.

Ablauf:

Entity wird entfernt oder zum Entfernen queued
aber Record bleibt in npcs/state.json

Folge:

/knpc list zeigt NPC noch
Entity ist weg oder verschwindet später
Relink/Respawn kann später Chaos machen

Fix: removeNpc(...) darf keinen halben Delete machen. Entweder sicher löschen oder blockieren.

2. NPC-eigene Marker werden beim echten Löschen nicht entfernt

Bei:

/knpc remove
/knpc clear
Spawn-Rollback
permanentem Tod

müssen eigene Marker mit raus, aber nur wenn kein anderer NPC sie benutzt.

Aktuell bleiben wahrscheinlich Marker-Leichen in state.json.

Fix: zentral in NpcRoutineRunner.removeNpc(...):

NPC löschen
→ eigene MarkerIds sammeln
→ prüfen ob unbenutzt
→ MarkerRegistry/state.json bereinigen

Nicht löschen bei:

MISSING_ENTITY
NEEDS_RELINK
Restart
Chunk nicht geladen
3. Commands/Scheduler melden teilweise Erfolg trotz unsicherem Zustand

Besonders:

SpawnNpcCommand
→ saveStateSafely() fail
→ scheduler.removeNpc(...)
→ Ergebnis nicht sauber genug geprüft

und:

NpcRespawnMissingCommand
→ saveStateSafely() wird teils ohne Rückgabeprüfung genutzt

Folge:

Runtime geändert
state.json nicht gespeichert
Command klingt trotzdem erfolgreich

Fix: Commands müssen RemoveResult/Save-Ergebnis prüfen und ehrlich melden:

REMOVED
BLOCKED
SAVE_FAILED
ROLLBACK_FAILED

Nicht nur true/false.

Kurz-Reihenfolge
1. removeNpc(...) sicher machen
2. NPC-owned Marker Cleanup zentral einbauen
3. Command-Erfolgsmeldungen + Save/rollback checks härten