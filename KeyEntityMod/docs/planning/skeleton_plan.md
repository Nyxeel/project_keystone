# Scope-Plan — Skeleton nach zielzustand.md

## Grundregel

Dieser Plan baut nur das Skeleton / Fundament.

Noch NICHT Ziel:

- echter Hytale Spawn
- echter EntityRef-Typ
- echtes Relink über UUID
- echtes ChunkGate
- echte Prefab-Platzierung
- echte Animation/Sound-Ausführung
- echte Combat-Logik
- echtes Worldgen
- echte Questlogik
- echte Raidlogik
- echte Mana-/Magic-Runtime

Grund:

Hytale-API-Verhalten muss zuerst über patcher.zip und hytalemodding.dev geprüft werden.
Keine Fake-Engine-Logik bauen.

---

# Phase 1 — Model-Skeleton finalisieren

## Ziel

Definieren, welche Datenformen existieren.

## Dateien

- model/NpcRecord.java
- model/RuntimeNpc.java
- model/PersistedWorldState.java
- model/NpcEntityStatus.java
- model/NpcState.java

## Zweck

NpcRecord
→ persistente NPC-Daten

RuntimeNpc
→ Live-Daten, niemals speichern

PersistedWorldState
→ kompletter state.json-Inhalt pro Welt

NpcEntityStatus
→ technischer Entity-Zustand

NpcState
→ grober fachlicher Zustand

## Failchecks

- [ ] kein entityRef in NpcRecord
- [ ] kein Hytale Entity-Objekt in NpcRecord
- [ ] RuntimeNpc wird nie persistiert
- [ ] npcId nicht null/blank
- [ ] roleId nicht null/blank
- [ ] worldKey nicht null/blank
- [ ] currentPosition finite, wenn gesetzt
- [ ] markerAssignments keine leeren Keys/Values
- [ ] NpcState nicht mit jeder JSON-Action vollstopfen

---

# Phase 2 — State-System stabilisieren

## Ziel

state.json pro Welt vorbereiten und sichere Load-/Save-Ergebnisse haben.

## Dateien

- state/NpcStateStore.java
- state/StateLoadResult.java
- state/StateSaveResult.java
- state/internal/WorldStateStore.java
- state/internal/StatePathResolver.java
- state/internal/StateFileIO.java
- state/internal/StateJsonCodec.java
- state/internal/StateBackupStore.java

## Skeleton-Ziel

NpcStateStore
→ öffentliche State-Tür

WorldStateStore
→ verwaltet State pro Welt

StatePathResolver
→ Pfade und Ordner

StateFileIO
→ Datei lesen/schreiben

StateJsonCodec
→ JSON umwandeln

StateBackupStore
→ Backups

StateLoadResult
→ Load-Ergebnis + geladene PersistedWorldState

StateSaveResult
→ Save-Ergebnis

## Failchecks

- [ ] Load-Failure überschreibt keine state.json
- [ ] Partial-Load wird nicht als Erfolg behandelt
- [ ] Save-Failure gibt false / failed Result
- [ ] Dirty wird nur nach echtem Save gelöscht
- [ ] keine Runtime-Daten im JSON-Modell
- [ ] pro Welt eigener State
- [ ] StatePathResolver nutzt später Plugin-Datenpfad
- [ ] aktuell harte Pfade nur als Skeleton-TODO markieren

---

# Phase 3 — State ↔ NpcManager-Brücke

## Ziel

Geladene NPC-Records aus state.json gehen in den RAM-Manager.

## Dateien

- core/NpcManager.java
- state/StateLoadResult.java
- state/internal/WorldStateStore.java
- bootstrap/NpcPluginBootstrap.java

## Ablauf später

NpcStateStore lädt PersistedWorldState
↓
StateLoadResult enthält worldStates
↓
NpcPluginBootstrap nimmt Records
↓
NpcManager.restoreRecords(...)
↓
RuntimeNpc wird leer vorbereitet
↓
Relink läuft später separat

## Failchecks

- [ ] StateStore liest nur Datei
- [ ] NpcManager liest keine Dateien
- [ ] Bootstrap verbindet nur
- [ ] RuntimeNpc nach Load ohne EntityRef
- [ ] kein Spawn beim Load
- [ ] kein Relink beim reinen Load
- [ ] kein Save nach fehlgeschlagenem Load

---

# Phase 4 — World-System-Skeleton

## Ziel

Welten sauber identifizieren.

## Dateien

- world/WorldManager.java
- world/WorldKey.java
- world/ChunkGate.java
- world/ChunkLoadTracker.java

## Zweck

WorldManager
→ erkennt später Server-Spielwelten

WorldKey
→ stabiler Key für state.json pro Welt

ChunkGate
→ prüft später, ob Zielbereich geladen ist

ChunkLoadTracker
→ merkt später Chunk-/Load-Zustände

## Failchecks

- [ ] worldKey nicht leer
- [ ] worldKey wird sanitisiert
- [ ] Welt wird nicht geraten, wenn API unklar ist
- [ ] ChunkGate lädt keine Chunks als Safety-Beweis
- [ ] kein Worldgen hier
- [ ] kein Spawn hier

---

# Phase 5 — Definition-Skeleton

## Ziel

JSON beschreibt NPCs. Java lädt generisch.

## Dateien

- definition/NpcDefinition.java
- definition/NpcDefinitionRegistry.java
- definition/NpcGroupLoader.java
- definition/NpcProfileLoader.java
- definition/EffectiveNpcDefinition.java
- definition/SpawnPoolDefinition.java
- definition/NpcSlotDefinition.java
- definition/profiles/RoutineProfile.java
- definition/profiles/NavigationProfile.java
- definition/profiles/MovementProfile.java
- definition/profiles/CombatProfile.java
- definition/profiles/PersistenceProfile.java
- definition/profiles/AppearanceProfile.java
- definition/profiles/SkillProfile.java
- definition/profiles/SpawnProfile.java
- definition/profiles/ActionProfile.java
- definition/profiles/AiProfile.java
- definition/profiles/ManaProfile.java
- definition/profiles/MagicProfile.java

## Zweck

NpcDefinition
→ geladener NPC-Bauplan

EffectiveNpcDefinition
→ fertig gemergte Definition

Profile
→ reine Datenmodelle, noch keine Runtime-Ausführung

## Failchecks

- [ ] roleId nicht leer
- [ ] hytaleRole nicht leer
- [ ] keine dynamischen KeystoneNPC_... RoleNames
- [ ] requiredMarkers nicht leer
- [ ] Profile dürfen nur fehlen, wenn erlaubt
- [ ] keine Runtime-Logik in Definition-Klassen
- [ ] keine Hytale-Role-Datei als Keystone-Instanzdaten missbrauchen

---

# Phase 6 — Marker-v2-Skeleton

## Ziel

Logische Marker aus JSON werden konkrete Marker-IDs pro NPC-Instanz.

## Dateien

- marker/MarkerRegistry.java
- marker/MarkerAssignment.java
- marker/MarkerRecord.java
- marker/MarkerType.java
- marker/MarkerInstance.java
- marker/RequiredMarkerResolver.java
- marker/internal/MarkerAssignmentValidator.java
- marker/internal/MarkerCleanupPlanner.java

## Zweck

requiredMarkers
→ logische Namen aus JSON

markerAssignments
→ konkrete markerId pro NPC

MarkerRecord / MarkerInstance
→ Marker existieren konkret in Welt/Struktur

## Failchecks

- [ ] Marker-ID existiert
- [ ] MarkerType passt
- [ ] worldKey passt
- [ ] read-only Resolve mutiert nie
- [ ] kein Auto-Reassign beim Load/Tick
- [ ] Marker aus anderer Welt blockieren
- [ ] fehlender Marker pausiert, repariert nicht heimlich

---

# Phase 7 — Structure-/Prefab-Skeleton

## Ziel

Prefab-gebundene NPCs sauber vorbereiten.

## Dateien

- structure/StructureDefinition.java
- structure/StructureInstance.java
- structure/StructureInstanceRegistry.java
- structure/StructureMarkerDefinition.java
- structure/StructureMarkerInstance.java
- structure/StructureNpcSlot.java
- structure/StructureSpawnComposition.java
- structure/StructureSpawnPool.java
- structure/StructureProtectionPolicy.java
- structure/PrefabBinding.java
- structure/PrefabPlacement.java
- structure/PrefabPlacementResult.java
- structure/internal/StructureInstanceIdFactory.java
- structure/internal/PrefabMarkerExtractor.java
- structure/internal/PrefabSlotResolver.java
- structure/internal/CompositionResolver.java

## Zweck

StructureDefinition
→ Prefab-Bauplan

StructureInstance
→ konkret platzierte Struktur, z. B. house_007

StructureNpcSlot
→ main_worker / spouse / child

StructureSpawnComposition
→ sinnvolle NPC-Kombination

StructureProtectionPolicy
→ protected / repairable / admin_only / unprotected

## Failchecks

- [ ] structureInstanceId stabil
- [ ] prefabId nicht leer
- [ ] worldKey nicht leer
- [ ] relative Marker und absolute Marker getrennt
- [ ] Composition passt zu Slots
- [ ] keine NPCs direkt aus Worldgen spawnen
- [ ] beschädigte Struktur löscht keine Marker heimlich

---

# Phase 8 — Lifecycle-Result-Skeleton

## Ziel

Spawn, Relink, Respawn, Remove bekommen klare Ergebnisobjekte.

## Dateien

- lifecycle/NpcSpawn.java
- lifecycle/NpcRelink.java
- lifecycle/NpcRespawn.java
- lifecycle/NpcRemoval.java
- lifecycle/SpawnResult.java
- lifecycle/RelinkResult.java
- lifecycle/RespawnResult.java
- lifecycle/RemoveResult.java

## Zweck

NpcSpawn
→ später Record + Entity erzeugen

NpcRelink
→ später EntityRef wiederfinden

NpcRespawn
→ später fehlende Entity nur nach Gates ersetzen

NpcRemoval
→ später sicher entfernen

## Failchecks

- [ ] kein Spawn ohne Record
- [ ] kein Erfolg bei Save-Failure
- [ ] kein Relink bei AMBIGUOUS
- [ ] kein Respawn ohne ChunkGate
- [ ] kein Remove ohne Ownership-Beweis
- [ ] keine Hytale-API erraten

---

# Phase 9 — Runtime / Tick / LiveEntityGate

## Ziel

Normale NPC-Logik läuft nur mit gültiger Live-Entity.

## Dateien

- runtime/NpcTick.java
- runtime/NpcTickPipeline.java
- runtime/LiveEntityGate.java

## Zweck

LiveEntityGate
→ prüft ACTIVE + RuntimeNpc.hasLiveEntity()

NpcTick
→ ruft später Routine/Navigation/Action nur nach Gate

NpcTickPipeline
→ ordnet spätere Tick-Schritte

## Failchecks

- [ ] kein Tick ohne EntityRef
- [ ] kein Save pro Tick
- [ ] kein Auto-Respawn im Tick
- [ ] kein Tick-Spam
- [ ] Runtime-Verlust cleart RuntimeNpc

---

# Phase 10 — Routine + Action-Skeleton

## Ziel

NPCs führen Aktionen aus JSON an Markern aus.

## Dateien

- routine/RoutineManager.java
- routine/RoutineRunner.java
- routine/RoutineDefinition.java
- routine/RoutineEntry.java
- routine/RoutineTarget.java
- action/NpcActionSystem.java
- action/ActionDefinition.java
- action/ActionRunner.java
- action/AnimationBinding.java
- action/SoundBinding.java

## Zweck

RoutineDefinition
→ Tagesplan

RoutineEntry
→ einzelner Schritt

ActionDefinition
→ Animation / Sound / Dauer

ActionRunner
→ spätere Runtime-Ausführung

## Failchecks

- [ ] keine Action ohne LiveEntityGate
- [ ] fehlender Marker pausiert
- [ ] fehlender Marker repariert nicht heimlich
- [ ] keine Action-Runtime persistieren
- [ ] keine Hytale Animation/Sound API erraten

---

# Phase 11 — Navigation / Road-Skeleton

## Ziel

Navigation bleibt generisch und wird über JSON-Profile gesteuert.

## Dateien

- navigation/NpcNavigation.java
- navigation/NavigationLogicRegistry.java
- navigation/NavigationTarget.java
- navigation/NavigationRoute.java
- navigation/EngineNavigationController.java
- road/RoadGraph.java
- road/RoadNode.java
- road/RoadEdge.java
- road/RoadRoute.java
- road/RoadWalkerController.java

## Zweck

NpcNavigation
→ bewegt nur

NavigationLogicRegistry
→ wählt spätere Logik aus Profil

RoadGraph
→ Straßenstruktur

RoadWalkerController
→ wählt nächste RoadNode

## Failchecks

- [ ] keine eigene Fake-Pathfinding-Engine
- [ ] Hytale Pathfinder später prüfen
- [ ] Navigation nur mit EntityRef
- [ ] RoadGraph entscheidet Ziel
- [ ] NpcNavigation führt nur Bewegung aus

---

# Phase 12 — Command / Config / Logging / Validation / Error

## Ziel

Sicherheits- und Steuerungswerkzeuge vorbereiten.

## Dateien

- command/NpcCommands.java
- command/NpcCommandRegistrar.java
- command/admin/
- command/marker/
- command/spawn/
- command/debug/
- command/structure/
- command/worldgen/
- config/NpcConfig.java
- logging/NpcLogger.java
- logging/LogCooldown.java
- validation/NpcValidation.java
- error/NpcErrorHandler.java
- error/RollbackResult.java

## Failchecks

- [ ] Commands safe-by-default
- [ ] gefährliche Commands später nur mit --force
- [ ] dry-run mutiert nichts
- [ ] Logger kein Tick-Spam
- [ ] Validation zentralisiert Null/Blank/World/Marker Checks
- [ ] ErrorHandler verschluckt keine kritischen Fehler

---

# Phase 13 — Event-/Feature-Skeleton

## Ziel

Features später ankoppeln, ohne Core-Systeme zu umgehen.

## Dateien

- event/NpcEventBus.java
- event/NpcEvent.java
- event/events/NpcSpawnedEvent.java
- event/events/NpcRemovedEvent.java
- event/events/MarkerAssignedEvent.java
- event/events/StructurePlacedEvent.java
- event/events/StructureDamagedEvent.java
- features/quest/QuestFeature.java
- features/quest/QuestDefinition.java
- features/quest/QuestState.java
- features/quest/QuestCommands.java
- features/raid/RaidFeature.java
- features/raid/RaidDefinition.java
- features/raid/RaidWave.java
- features/raid/RaidTargetSelector.java
- features/worldgen_v2/WorldgenV2Feature.java
- features/worldgen_v2/WorldgenRuleRegistry.java
- features/worldgen_v2/WorldgenPlacementPlanner.java
- features/worldgen_v2/WorldgenPlacementExecutor.java
- features/worldgen_v2/PendingPlacementQueue.java

## Failchecks

- [ ] Features umgehen nicht NpcSpawn
- [ ] Features umgehen nicht NpcStateStore
- [ ] Features umgehen nicht MarkerAssignment
- [ ] Worldgen platziert Structures, nicht direkt NPC-Logik
- [ ] Quest/Raid bekommen eigenen State nur über klare Schnittstellen

---

# Aktuelle Reihenfolge ab jetzt

Da Foundation schon begonnen wurde:

1. Aktuelle model/state/core Dateien per Failcheck-Review härten
2. State ↔ NpcManager-Brücke fertig skeletonisieren
3. WorldKey / State pro Welt sauber verbinden
4. Definition-Skeleton
5. Marker-v2-Skeleton
6. Structure-Skeleton
7. Lifecycle-Result-Skeleton
8. Runtime/LiveEntityGate-Skeleton
9. Routine/Action-Skeleton
10. Navigation/Road-Skeleton
11. Commands/Validation/Logging/Error
12. Event/Feature-Skeleton

---

# Wichtigster Merksatz

Erst Daten + Manager + State stabil.
Dann Definition.
Dann Marker.
Dann Structure.
Erst danach Spawn, Routine und Navigation.