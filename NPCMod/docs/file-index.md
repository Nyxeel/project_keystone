# Dateiliste mit Kurzbeschreibung (aktuell)

Stand: Feature-first Struktur nach Migration.
Hinweis: Build-Output unter target wird absichtlich nicht dokumentiert.

## /
- .gitignore: Git-Ignore-Regeln fuer lokale und Build-Dateien.
- pom.xml: Maven-Projektdefinition mit Abhaengigkeiten und Build.
- README.md: Hauptdokumentation im Modul-Root.

## debug
- game.log: Laufzeit-Log fuer Debugging.

## debug/screenshots
- collision-missing-hitbox.png: Screenshot zu Hitbox/Collision-Thema.
- door-labyrinth.png: Screenshot zu Door-/Pfadrouting.
- door-stop-too-early.png: Screenshot zum fruehen Stoppen vor Marker/Tuer.
- movement-test.png: Screenshot zu Bewegungstests.
- teleport-offset.png: Screenshot zu Teleport-/Positionsoffset.

## docs
- debug-notes.md: Gesammelte Crash- und Laufzeitnotizen.
- file-index.md: Diese Dateiuebersicht.
- todos.md: Offene MVP-A Aufgaben.

## libs
- HytaleServer.jar: Lokale Server-API/Runtime als Build-Library.

## src/main/java/keystone/npc
- KeystoneNpcPlugin.java: Plugin-Entrypoint (Setup, Events, Save/Load, Commands).

## src/main/java/keystone/npc/commands
- KeystoneNpcCommands.java: Root-Commandstruktur fuer /knpc.
- NpcCommandRegistrar.java: Registriert die Command-Collection.

## src/main/java/keystone/npc/commands/admin
- NpcClearCommand.java: Loescht alle NPC-Slots.
- NpcListCommand.java: Listet NPC-Slots.
- NpcRemoveCommand.java: Entfernt einen NPC per Index.

## src/main/java/keystone/npc/commands/debug
- NpcStatusCommand.java: Gibt Marker-/NPC-Status aus.

## src/main/java/keystone/npc/commands/marker
- MarkerClearCommand.java: Setzt Marker zurueck.
- MarkerCommandGroup.java: Gruppiert Marker-Subcommands.
- MarkerSetCommand.java: Setzt Marker an Spielerposition.

## src/main/java/keystone/npc/commands/spawn
- SpawnNpcCommand.java: Spawn-Command mit Rollen- und Markerpruefung.

## src/main/java/keystone/npc/domain
- NpcRecord.java: Laufzeit- und Persistenzmodell eines NPC-Slots.
- NpcRole.java: Rollenmodell fuer NPC-Typen.
- NpcState.java: State-Enum fuer Idle/Walking/Pause.
- StateType.java: Kategorisierung von Zustandstypen.
- TargetRole.java: Marker-bezogene Zielrolle pro Zustand.

## src/main/java/keystone/npc/doorway
- ActiveDoorPass.java: Datensatz fuer geoeffnete Tueren pro NPC-Durchgang.
- DoorPassTracker.java: Verwalten geoeffneter Tueren pro NPC.
- DoorwayConfig.java: Konfigurationswerte fuer Door-Handling.
- DoorwayFlow.java: Door-Ablauf waehrend Navigation.
- DoorwayScanner.java: Door-Erkennung sowie Open/Close/Fallback-Basis.
- PendingDoorAttempt.java: Datensatz fuer laufende Door-Interaktionsversuche.

## src/main/java/keystone/npc/markers
- MarkerRecord.java: Marker-Datenrecord (Typ, Welt, Position).
- MarkerRegistry.java: Marker-Verwaltung und Lookup.
- MarkerRingTraversal.java: Deterministische Ring-Fallback-Auswahl.
- MarkerType.java: Marker-Typen (BED, DOOR, WORK).
- Vec3.java: Einfacher persistenter 3D-Vektor.
- WorldId.java: Welt-Identifier als Wertobjekt.

## src/main/java/keystone/npc/navigation
- EngineNavigationController.java: Adapter zur Engine-Navigation.
- NavigationTarget.java: Zustand einer aktiven Navigation.
- NpcNavigation.java: Hilfslogik fuer Navigationszeiten.

## src/main/java/keystone/npc/persistence
- ActiveMarkerIdMapper.java: Mapping ActiveMarker <-> persistiertes JSON.
- JsonFileStateStore.java: Laden/Speichern von State als JSON.
- JsonPersistedModels.java: Persistenz-Records fuer JSON-Struktur.
- PluginState.java: Aggregierter geladener Plugin-State.
- StateStore.java: Interface fuer Persistenzimplementierungen.

## src/main/java/keystone/npc/recovery
- RespawnRecoveryService.java: Respawn-Retry- und Recovery-Logik.

## src/main/java/keystone/npc/relink
- RelinkSupport.java: Hilfen fuer Distanz- und Ref-Vergleiche.
- RelinkWorkflowService.java: UUID-/Anchor-Relink und Dedupe-Workflow.

## src/main/java/keystone/npc/roles
- DailyRoutine.java: Schlaf-/Arbeitszeiten einer Rolle.
- RoleDefinition.java: Rollenobjekt inkl. Marker-Anforderungen und Tagesroutine.
- RoleDefinitionParsingSupport.java: Parser- und Merge-Helfer fuer Rollen.
- RoleDefinitionPersistedModels.java: Persistenz-Records fuer Rollen-JSON.
- RoleDefinitionRegistry.java: Laden und Lookup von Rollen.

## src/main/java/keystone/npc/routine
- NpcRoutineRunner.java: Zentrale Routine-Orchestrierung.
- NpcTickSystem.java: ECS-Tick-Bridge zur Routine.

## src/main/java/keystone/npc/routine/entity
- EntitySyncService.java: Sync zwischen Entity-Transform und NPC-State.

## src/main/java/keystone/npc/routine/marker
- IdleMarkerService.java: Idle-Marker-Autoritaet und Restore-Ausrichtung.
- MarkerResolver.java: Marker-Aufloesung mit Fallback-Ringstrategie.

## src/main/java/keystone/npc/routine/pathfinding
- NavigationRuntimeService.java: Runtime-Navigation (Tick/Finish/Maintenance).
- PathfindingSupport.java: Geometrie- und Pfad-Helfer.

## src/main/java/keystone/npc/routine/state
- NpcTickPipeline.java: Voller NPC-Update-Ablauf je Tick.
- StateTargetingService.java: Zielzustand und Start von Bed/Work-Navigation.

## src/main/resources
- manifest.json: Mod-Manifest (Metadaten, Main, Version-Pinning).

## src/main/resources/Server/NPC/Roles
- lumberjack.json: Konkrete Rollen-Definition fuer Lumberjack.
- lumberjack.template.json: Rollen-Template als Basisdatei.
- role-template.md: Erklaerung der Rollen-Template-Struktur.
