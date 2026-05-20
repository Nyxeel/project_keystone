###
fuer implementation der spawn phase interessant

World.getEntityRef(UUID)
EntityStore.getRefFromUUID(UUID)

Das passt gut zu deinem späteren UUID-Relink.

Auch gefunden:

NPCPlugin.spawnEntity(...)
NPCPlugin.getIndex(...)

###


NOTIZ — requireText zentralisieren

Problem:
In mehreren Dateien existiert eine eigene requireText-Methode.
Das ist doppelte Logik und kann später zu uneinheitlichem Verhalten führen.

Ziel:
Eine zentrale Utility-Klasse bauen, z. B.:

src/main/java/keystone/npc/util/TextChecks.java

Dort später zentral sammeln:
- requireText(String value, String fieldName)
- cleanOptionalText(String value)
- eventuell requirePresentText(String value, String fieldName)

Wichtig:
Nicht blind alles ersetzen.
Manche requireText-Methoden geben String zurück.
Andere prüfen nur und geben nichts zurück.

Dateien mit requireText prüfen:

- src/main/java/keystone/npc/KeystoneNpcPlugin.java
  -> prüfen, ob lokale requireText durch TextChecks ersetzt werden kann.

- src/main/java/keystone/npc/model/marker/NpcMarkerAssignments.java
  -> prüfen, ob lokale Textprüfung identisch ist.

- src/main/java/keystone/npc/model/PersistedWorldState.java
  -> prüfen, ob requireText String zurückgibt und trimmt.

- src/main/java/keystone/npc/model/identity/NpcIdentity.java
  -> prüfen, ob Identity-spezifische Regeln existieren.

- src/main/java/keystone/npc/model/RuntimeNpc.java
  -> vorsichtig prüfen, weil Runtime-Klasse nicht mit Definition-Model vermischt werden soll.

- src/main/java/keystone/npc/world/WorldData.java
  -> prüfen, ob worldKey/worldId eigene strengere Regeln brauchen.

- src/main/java/keystone/npc/command/NpcCommands.java
  -> prüfen, ob Command-Argumente eigene Fehlermeldungen brauchen.

- src/main/java/keystone/npc/lifecycle/NpcSpawn.java
  -> vorsichtig prüfen, weil Spawn-Safety betroffen sein kann.

- src/main/java/keystone/npc/marker/MarkerAssignment.java
  -> prüfen, ob Marker-Namen eigene Regeln brauchen.

- src/main/java/keystone/npc/marker/MarkerRecord.java
  -> prüfen, ob MarkerRecord strengere Validierung braucht.

- src/main/java/keystone/npc/definition/model/ProfileTypeRule.java
  -> guter Kandidat für zentrale TextChecks.

- src/main/java/keystone/npc/definition/model/NpcEngineDefinition.java
  -> guter Kandidat für zentrale TextChecks.

- src/main/java/keystone/npc/definition/model/NpcProfile.java
  -> guter Kandidat für zentrale TextChecks.

- src/main/java/keystone/npc/definition/model/LoadedNpcDefinition.java
  -> guter Kandidat für zentrale TextChecks.

- src/main/java/keystone/npc/core/NpcManager.java
  -> prüfen, ob Manager-spezifische Fehlertexte erhalten bleiben sollen.

- src/main/java/keystone/npc/state/StateLoadResult.java
  -> vorsichtig prüfen, weil State-/Load-Fehler sauber bleiben müssen.

- src/main/java/keystone/npc/state/StateSaveResult.java
  -> vorsichtig prüfen, weil Save-Fehler sichtbar bleiben müssen.

- src/main/java/keystone/npc/runtime/NpcTick.java
  -> vorsichtig prüfen, weil Runtime-Safety betroffen sein kann.

- src/main/java/keystone/npc/navigation/NpcNavigation.java
  -> vorsichtig prüfen, weil Navigation keine falschen Defaults bekommen darf.

Empfohlener späterer Mini-Step:
1. TextChecks.java anlegen.
2. Nur Definition-Model-Dateien umstellen:
   - ProfileTypeRule.java
   - NpcEngineDefinition.java
   - NpcProfile.java
   - LoadedNpcDefinition.java
3. Compile prüfen:
   mvn -q -DskipTests test-compile
4. Danach erst state/model/runtime-Dateien einzeln prüfen.

Nicht in diesem Refactor tun:
- keine Spawn-Logik ändern
- keine state.json ändern
- keine Marker-Logik ändern
- keine Runtime-Logik ändern
- keine Safety-Regeln ändern

####
