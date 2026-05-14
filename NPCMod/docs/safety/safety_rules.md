# Zielzustand — KeystoneNPC / NPCMod

## 0. Kurzfassung

Die Mod soll eine generische NPC-Foundation für Hytale werden.

Java soll möglichst wenig spezielle NPC-Logik kennen.

Die Grundregel lautet:

Java führt generische Systeme aus.
JSON beschreibt, was ein NPC ist, wie er aussieht, wo er lebt, wie er sich bewegt, was er tut und welche Marker er benutzt.

Ziel ist eine modulare Basis, mit der später viele Features möglich sind:

- prefab-gebundene Friendly-NPCs
- organische Dörfer über Worldgen v2
- NPC-Haushalte mit sinnvollen Kombinationen
- Biome-NPCs mit Radius
- Straßen-NPCs wie Wanderer, Händler, Karawanen
- Quest-NPCs
- Base-Arbeiter
- Hostile-Raids / Überfälle
- spätere Magier-/Mana-/AI-Logik aus JSON

Wichtig:

Die Core-Foundation muss stabil sein, damit neue Features später nicht jedes Mal Spawn, Marker, state.json, Relink oder Navigation kaputtmachen.

---

# 1. Grundidee der NPCs

## 1.1 NPCs sind Instanzen, keine bloßen Rollen

Eine NPC-Rolle wie `lumberjack` ist nur ein Bauplan.

Eine echte NPC-Instanz ist etwas Konkretes:

- npcId
- roleId
- entityUuid
- worldId
- structureInstanceId
- slotId
- markerAssignments
- currentPosition
- entityStatus

Beispiel:

lumberjack in Haus_007:

work -> marker_house_007_work_01
bed  -> marker_house_007_bed_main
door -> marker_house_007_door

Das bedeutet:

Nicht alle Holzfäller benutzen denselben `work`-Marker.
Jeder konkrete NPC bekommt eigene Marker-IDs aus seiner konkreten Hausinstanz.

---

# 2. Prefab-gebundene Friendly-NPCs

## 2.1 Hauptidee

Die meisten Friendly-NPCs sollen an Prefabs gebunden sein.

Ein Prefab ist z. B.:

- Holzfällerhaus
- Schmiede
- Bauernhof
- Bürgerhaus
- Marktstand
- Dorfzentrum
- Wachposten
- Straßenlager

Das Prefab enthält Marker und NPC-Slots.

Nicht die Rolle besitzt direkt ein Haus.

Besser:

lumberjack_house prefab
  -> marker
  -> npcSlots
  -> allowed compositions

## 2.2 Warum Prefab-Instanzen wichtig sind

Später kann es 20 gleiche Holzfällerhäuser geben.

Deshalb reicht nicht:

lumberjack_house

Sondern jedes platzierte Haus braucht eine eigene Instanz:

house_001
house_002
house_003
...
house_020

Jede Instanz hat eigene Marker.

Beispiel:

house_007:
  marker_house_007_bed_main
  marker_house_007_bed_spouse
  marker_house_007_work_01
  marker_house_007_kitchen
  marker_house_007_door

NPCs in diesem Haus speichern nur die Marker-IDs dieser Instanz.

---

# 3. Holzfällerhaus-Beispiel

## 3.1 Prefab-Pool

Worldgen will ein Holzfällerhaus platzieren.

Dann wählt die Logik zufällig ein passendes Prefab aus einem Pool:

- lumberjack_house_small
- lumberjack_house_old
- lumberjack_house_family
- lumberjack_house_workshop
- lumberjack_house_forest_edge

Das erzeugt Vielfalt in der Welt.

## 3.2 Composition-Pool

Danach wird eine sinnvolle NPC-Kombination gewählt.

Nicht jeder Slot wird komplett einzeln zufällig gewürfelt, weil dadurch unlogische Kombinationen entstehen könnten.

Besser:

composition_young_couple:
  main_worker -> lumberjack
  spouse      -> lumberjack_wife

composition_old_couple:
  main_worker -> lumberjack_oldman
  spouse      -> lumberjack_oldwife

composition_full_family:
  main_worker -> lumberjack
  spouse      -> lumberjack_wife
  child       -> lumberjack_child

composition_all_four:
  main_worker -> lumberjack
  spouse      -> lumberjack_wife
  elder_male  -> lumberjack_oldman
  elder_female -> lumberjack_oldwife

Die genaue Endlogik wird noch nicht festgelegt.
Wichtig ist nur:

Es muss sinnvolle Compositions geben.

## 3.3 Slots im Prefab

Ein Holzfällerhaus kann Slots haben wie:

main_worker
spouse
elder_male
elder_female
child_optional
visitor_optional

Jeder Slot sagt:

- welche Rolle oder Rollengruppe dort erlaubt ist
- welche Marker dieser Slot braucht
- ob der Slot Pflicht oder optional ist

Beispiel:

main_worker braucht:
- bed_main
- work
- door
- wood_storage

spouse braucht:
- bed_spouse
- kitchen
- garden
- door

oldman braucht:
- bed_elder_male
- chair
- firewood
- door

oldwife braucht:
- bed_elder_female
- kitchen
- table
- door

---

# 4. Marker-v2 Zielidee

## 4.1 Logische Namen vs. konkrete Marker-IDs

Definitionen benutzen logische Namen:

bed
work
door
kitchen
patrol_a
wood_storage
table
garden

Runtime / state.json benutzt konkrete Marker-IDs:

marker_house_007_bed_main
marker_house_007_work_01
marker_house_007_door
marker_house_007_kitchen

Das ist gut und gewollt.

## 4.2 Warum das wichtig ist

Wenn ein NPC in Haus_007 wohnt, darf er nicht versehentlich Marker aus Haus_008 benutzen.

Deshalb:

requiredMarkers aus JSON:
  bed
  work
  door

markerAssignments in state.json:
  bed -> marker_house_007_bed_main
  work -> marker_house_007_work_01
  door -> marker_house_007_door

## 4.3 Marker gehören zur Instanz

Prefab-Marker sind zuerst relative Marker im Prefab.

Nach Platzierung werden daraus konkrete Marker-Instanzen:

Prefab:
  bed_main bei relativer Position 3,1,4

Hausinstanz:
  marker_house_007_bed_main bei Weltposition 120,64,300

NPC:
  bed -> marker_house_007_bed_main

---

# 5. Zufalls-Schichten für Vielfalt

Die Welt soll vielfältig wirken, ohne dass alles manuell gebaut werden muss.

Dafür gibt es mehrere getrennte Zufalls-Schichten:

1. Prefab-Pool
   Welches Haus / Lager / Gebäude wird platziert?

2. Composition-Pool
   Welche NPC-Kombination wohnt oder arbeitet dort?

3. Appearance-Pool
   Wie sieht jeder konkrete NPC aus?

4. Marker aus Prefab
   Wo sind Bett, Arbeit, Tür, Küche, Tisch usw.?

5. Profile aus Role / Group
   Welche Routine, Bewegung, Navigation, Combat, Persistence nutzt der NPC?

6. Spätere AI-/Mana-/Magie-Profile
   Welche Speziallogik, Mana-Nutzung oder Magierverhalten besitzt der NPC?

## Beispiel-Ablauf

Worldgen will Holzfällerhaus platzieren
↓
wählt zufällig:
lumberjack_house_small oder lumberjack_house_old oder lumberjack_house_family
↓
wählt passende NPC-Kombination:
lumberjack + wife
oder oldman + oldwife
oder alle 4
↓
jeder NPC bekommt eigene markerAssignments aus genau dieser Hausinstanz
↓
Appearance kommt zufällig aus Pool
↓
Routine / Movement / Pathfinding kommt aus roleId / group definition
↓
NPCs werden als Records gespeichert
↓
erst dann werden Entities gespawnt

---

# 6. JSON bestimmt Verhalten

Java soll nicht hart wissen:

Das ist ein Bürger.
Das ist ein Worker.
Das ist Friendly.
Das ist Hostile.
Das ist ein Holzfäller.
Das ist ein Magier.

Java soll nur wissen:

Dieser NPC hat Definition X.
Definition X hat Profile.
Profile sagen, was zu tun ist.

JSON bestimmt:

- roleId
- hytaleRole
- appearancePool
- routineProfile
- movementProfile
- navigationProfile
- combatProfile
- persistenceProfile
- spawnProfile
- prefabBinding
- requiredMarkers
- actions
- animations
- sounds
- später AIProfile
- später ManaProfile
- später MagicProfile

---

# 7. Routine, Animationen und Sounds

Prefab-NPCs sollen an Markern Aktionen ausführen.

Beispiele:

sleep_marker:
  action: sleep
  animation: sleep_loop
  sound: breathing_soft

table_marker:
  action: eat
  animation: sit_eat
  sound: eating

work_marker:
  action: chop_wood
  animation: axe_work_loop
  sound: axe_hit

kitchen_marker:
  action: cook
  animation: cooking_loop
  sound: pot_stir

Java soll nicht hart kodieren:

Wenn Marker kitchen, dann spiele cooking_loop.

Besser:

Routine sagt:
  gehe zu kitchen
  führe action cook aus

Action-Profil sagt:
  animation = cooking_loop
  sound = pot_stir
  duration = 20s

So bleibt Java generisch.

---

# 8. Navigation bleibt generisch

Es soll keine getrennte Java-Logik für Friendly und Hostile geben.

Stattdessen:

navigationProfile bestimmt die Logik.

Beispiele:

friendly_worker:
  logic: MARKER_ROUTINE
  pathfinder: engine
  avoidCombat: true

hostile_chaser:
  logic: CHASE_TARGET
  pathfinder: engine
  repathIntervalTicks: 20

road_walker:
  logic: ROAD_GRAPH
  pathfinder: engine
  allowedRoadMarkers:
    - ROAD_NODE
    - ROAD_CAMP
    - ROAD_CROSSING

biome_wanderer:
  logic: WANDER_RADIUS
  pathfinder: engine
  radius: 40

guard_patrol:
  logic: PATROL_MARKERS
  pathfinder: engine

Java lädt nur das Profil und wählt die passende generische Strategie.

---

# 9. Straßen-NPCs

Straßen-NPCs hängen nicht an Häusern.

Beispiele:

- Wanderer
- reisende Händler
- Karawanen
- Patrouillen
- Boten

Diese nutzen:

RoadGraph
RoadNode
RoadEdge
RoadRoute
RoadMarker

Ablauf:

RoadNode A -> RoadNode B -> RoadNode C

Das gehört nicht direkt in `NpcNavigation`.
`NpcNavigation` bewegt nur.
Das Road-System entscheidet, welches Straßen-Ziel als nächstes kommt.

---

# 10. Biome-NPCs

Biome-NPCs hängen nicht zwingend an Prefabs.

Sie können an Biome-Markern oder Spawn-Ankern hängen.

Beispiele:

forest_wolf_anchor:
  biome: forest
  radius: 80
  roles:
    - wolf

bandit_camp_anchor:
  biome: cursed_forest
  radius: 40
  roles:
    - bandit
    - bandit_archer

Biome-NPCs brauchen:

- worldId
- biomeId
- centerPosition
- radius
- rolePool
- spawnLimit
- respawnPolicy
- despawnPolicy

---

# 11. Worldgen v2

Worldgen v2 soll nicht direkt NPC-Logik machen.

Worldgen v2 soll planen und Strukturen platzieren.

Sicherer Ablauf:

1. Region / Chunk wird betrachtet
2. Worldgen-Regel entscheidet: Hier darf ein Dorf entstehen
3. PlacementPlanner wählt Dorf-Layout
4. Prefab-Pool wählt einzelne Gebäude
5. Straßen werden logisch verbunden
6. StructureInstances werden erzeugt
7. Marker aus Prefabs werden registriert
8. SpawnCompositions werden gewählt
9. NPC-Records werden erstellt
10. NPC-Entities werden gespawnt, wenn sicher
11. Routine startet erst bei ACTIVE + gültiger EntityRef

Wichtig:

Worldgen v2 platziert Structures.
NpcSpawn erstellt NPCs.
MarkerAssignment weist Marker zu.
StateStore speichert Instanzen.

Diese Systeme dürfen nicht vermischt werden.

---

# 12. Prefab-Schutz

Friendly-NPC-Prefabs sollen wahrscheinlich nicht normal zerstörbar sein.

Grund:

Wenn Spieler ein Holzfällerhaus zerstören, können wichtige Dinge kaputtgehen:

- Marker verschwinden
- Tür blockiert
- Bett fehlt
- Arbeitsplatz fehlt
- NPC-Routine bricht
- Weg wird unzugänglich

Deshalb wird die Architektur vorbereitet für:

StructureProtectionPolicy

Mögliche Modi:

protected:
  Spieler dürfen nicht abbauen oder platzieren

repairable:
  Spieler können beschädigen, aber System erkennt Schaden und pausiert NPC

admin_only:
  Nur Admins dürfen Struktur ändern

unprotected:
  Struktur darf normal verändert werden

Wenn Struktur beschädigt ist:

Nicht:
  Marker heimlich löschen
  NPC blind respawnen
  neue Marker automatisch suchen

Sondern:
  StructureInstance = DAMAGED
  NPC = PAUSED_STRUCTURE_BLOCKED
  Admin/Repair-System kann reagieren

Die genaue Endentscheidung bleibt offen.
Aber die Architektur muss es erlauben.

---

# 13. State / Persistenz

state.json speichert konkrete Instanzen, nicht nur Definitionen.

Persistiert werden sollen:

NpcRecord:
  npcId
  roleId
  entityUuid
  entityStatus
  worldId
  structureInstanceId
  slotId
  currentPosition
  markerAssignments
  selectedAppearanceId
  selectedCompositionId
  selectedPrefabId

StructureInstance:
  structureInstanceId
  prefabId
  worldId
  position
  rotation
  markerInstances
  npcSlots
  protectionState
  damageState

Nicht speichern:

entityRef
Entity object
aktive Navigation
Door Runtime
Action Runtime
Cooldown Maps
Hytale Runtime Handles

Grundsatz:

Runtime darf handeln.
Persistence darf erinnern.
Persistence darf niemals unsichere Runtime-Zustände als Wahrheit speichern.

---

# 14. Java-Zielstruktur

Die Java-Struktur soll generisch bleiben.

Empfohlene Struktur:

src/main/java/keystone/npc/
  KeystoneNPCPlugin.java

  bootstrap/
    NpcPluginBootstrap.java

  service/
    NpcServices.java

  core/
    NpcManager.java
    NpcFeatureRegistry.java
    NpcFeatureModule.java
    NpcFeatureContext.java

  model/
    NpcRecord.java
    RuntimeNpc.java
    PersistedWorldState.java
    NpcEntityStatus.java
    NpcState.java

  world/
    WorldManager.java
    WorldKey.java
    ChunkGate.java
    ChunkLoadTracker.java

  state/
    NpcStateStore.java
    StateLoadResult.java
    StateSaveResult.java
    internal/
      WorldStateStore.java
      StatePathResolver.java
      StateFileIO.java
      StateJsonCodec.java
      StateBackupStore.java

  definition/
    NpcDefinition.java
    NpcDefinitionRegistry.java
    NpcGroupLoader.java
    NpcProfileLoader.java
    EffectiveNpcDefinition.java
    SpawnPoolDefinition.java
    NpcSlotDefinition.java
    profiles/
      RoutineProfile.java
      NavigationProfile.java
      MovementProfile.java
      CombatProfile.java
      PersistenceProfile.java
      AppearanceProfile.java
      SkillProfile.java
      SpawnProfile.java
      ActionProfile.java
      AiProfile.java
      ManaProfile.java
      MagicProfile.java

  marker/
    MarkerRegistry.java
    MarkerAssignment.java
    MarkerRecord.java
    MarkerType.java
    MarkerInstance.java
    RequiredMarkerResolver.java
    internal/
      MarkerAssignmentValidator.java
      MarkerCleanupPlanner.java

  structure/
    StructureDefinition.java
    StructureInstance.java
    StructureInstanceRegistry.java
    StructureMarkerDefinition.java
    StructureMarkerInstance.java
    StructureNpcSlot.java
    StructureSpawnComposition.java
    StructureSpawnPool.java
    StructureProtectionPolicy.java
    PrefabBinding.java
    PrefabPlacement.java
    PrefabPlacementResult.java
    internal/
      StructureInstanceIdFactory.java
      PrefabMarkerExtractor.java
      PrefabSlotResolver.java
      CompositionResolver.java

  lifecycle/
    NpcSpawn.java
    NpcRelink.java
    NpcRespawn.java
    NpcRemoval.java
    SpawnResult.java
    RelinkResult.java
    RespawnResult.java
    RemoveResult.java

  runtime/
    NpcTick.java
    NpcTickPipeline.java
    LiveEntityGate.java

  routine/
    RoutineManager.java
    RoutineRunner.java
    RoutineDefinition.java
    RoutineEntry.java
    RoutineTarget.java

  action/
    NpcActionSystem.java
    ActionDefinition.java
    ActionRunner.java
    AnimationBinding.java
    SoundBinding.java

  navigation/
    NpcNavigation.java
    NavigationLogicRegistry.java
    NavigationTarget.java
    NavigationRoute.java
    EngineNavigationController.java

  road/
    RoadGraph.java
    RoadNode.java
    RoadEdge.java
    RoadRoute.java
    RoadWalkerController.java

  command/
    NpcCommands.java
    NpcCommandRegistrar.java
    admin/
    marker/
    spawn/
    debug/
    structure/
    worldgen/

  config/
    NpcConfig.java

  logging/
    NpcLogger.java
    LogCooldown.java

  validation/
    NpcValidation.java

  error/
    NpcErrorHandler.java
    RollbackResult.java

  event/
    NpcEventBus.java
    NpcEvent.java
    events/
      NpcSpawnedEvent.java
      NpcRemovedEvent.java
      MarkerAssignedEvent.java
      StructurePlacedEvent.java
      StructureDamagedEvent.java

  features/
    quest/
      QuestFeature.java
      QuestDefinition.java
      QuestState.java
      QuestCommands.java

    raid/
      RaidFeature.java
      RaidDefinition.java
      RaidWave.java
      RaidTargetSelector.java

    worldgen_v2/
      WorldgenV2Feature.java
      WorldgenRuleRegistry.java
      WorldgenPlacementPlanner.java
      WorldgenPlacementExecutor.java
      PendingPlacementQueue.java

Wichtig:

Keine eigenen Java-Features für citizen, worker, hostile nur deshalb, weil es andere NPC-Typen sind.

Citizen, Worker, Hostile, Magier usw. werden über JSON-Profile beschrieben.

Eigene Java-Features nur, wenn ein System eigenen State oder eigene Speziallogik braucht, z. B.:

- Quest
- Raid
- Worldgen v2
- Economy
- Diplomatie
- große AI-Simulation

---

# 15. Resources-Zielstruktur

src/main/resources/
  Server/
    NPC/
      Roles/
        Template_Human_Friendly.json
        Template_Human_Hostile.json
        Template_Human_Mage.json
        Lumberjack.json
        Lumberjack_Wife.json
        Lumberjack_Oldman.json
        Lumberjack_Oldwife.json
        Traveling_Merchant.json
        Bandit.json
        Wolf.json

      Keystone/
        README.md

        npc/
          index.json

          lumberjack/
            lumberjack_group.json
            appearances/
              lumberjack_male_pool.json
              lumberjack_female_pool.json
              lumberjack_old_pool.json
            routines/
              lumberjack_day.json
              lumberjack_wife_day.json
              lumberjack_oldman_day.json
              lumberjack_oldwife_day.json
            actions/
              chop_wood.json
              cook.json
              eat.json
              sleep.json

          citizen/
            citizen_group.json
            appearances/
            routines/
            actions/

          traveler/
            traveler_group.json
            appearances/
            routines/
            actions/

          hostile/
            bandit_group.json
            appearances/
            routines/
            actions/

          mage/
            mage_group.json
            appearances/
            routines/
            actions/

        profiles/
          skills/
            human_worker.json
            trader.json
            guard.json
            hostile_basic.json
            mage_basic.json

          movement/
            human_walk.json
            human_run.json
            beast_walk.json

          navigation/
            friendly_worker.json
            road_walker.json
            guard_patrol.json
            hostile_chaser.json
            biome_wanderer.json
            mage_patrol.json

          combat/
            peaceful.json
            defensive_guard.json
            bandit_melee.json
            beast_basic.json
            mage_spellcaster.json

          persistence/
            persistent_citizen.json
            transient_traveler.json
            despawnable_hostile.json
            persistent_mage.json

          spawn/
            prefab_bound.json
            biome_radius.json
            road_traveler.json

          ai/
            simple_worker.json
            trader_social.json
            hostile_aggressive.json
            mage_study_magic.json

          mana/
            no_mana.json
            low_mana_user.json
            village_mage.json

          magic/
            none.json
            basic_healer.json
            fire_mage.json

        structures/
          index.json

          houses/
            lumberjack_house.json
            citizen_house_small.json
            mage_tower_small.json

          workplaces/
            lumberyard.json
            forge.json
            farm_plot.json
            magic_study.json

          roads/
            road_piece_straight.json
            road_crossing.json
            road_camp.json

          villages/
            forest_village_small.json
            forest_village_medium.json

        prefabs/
          houses/
            lumberjack_house_small/
              prefab.json
              markers.json
              npc_slots.json
              compositions.json
              protection.json

            lumberjack_house_old/
              prefab.json
              markers.json
              npc_slots.json
              compositions.json
              protection.json

            lumberjack_house_family/
              prefab.json
              markers.json
              npc_slots.json
              compositions.json
              protection.json

          workplaces/
            lumberyard/
              prefab.json
              markers.json
              npc_slots.json
              compositions.json
              protection.json

          village_pieces/
            village_center/
              prefab.json
              markers.json
              npc_slots.json
              compositions.json
              protection.json

            road_piece_straight/
              prefab.json
              road_nodes.json

            road_crossing/
              prefab.json
              road_nodes.json

        spawn_pools/
          lumberjack_prefab_pool.json
          lumberjack_house_composition_pool.json
          forest_citizen_pool.json
          road_traveler_pool.json
          forest_hostile_pool.json
          mage_tower_pool.json

        worldgen_v2/
          village_rules/
            forest_village.json

          road_rules/
            forest_roads.json

          placement_sets/
            forest_village_set.json

        features/
          quest/
            quest_index.json
            quests/
              first_delivery.json

          raid/
            raid_rules/
              small_bandit_raid.json
            raid_waves/
              bandit_wave_1.json

---

# 16. Scope-Regel

Die Vision ist groß, aber sinnvoll.

Nicht alles sofort bauen.

Sichere Reihenfolge:

1. Core Foundation
2. State pro Welt
3. StructureInstance
4. Prefab-Marker-Schema
5. Marker-v2
6. Ein manuell platziertes Holzfällerhaus
7. Eine sinnvolle Composition
8. NPCs bekommen eigene markerAssignments
9. Routine + Action + Animation/Sound aus JSON
10. Appearance-Pool
11. Prefab-Pool
12. Worldgen v2 plant erstes kleines Dorf
13. RoadGraph
14. Traveler
15. Biome-NPCs
16. Quests
17. Raids
18. Magier / Mana / AI-Profile

Wenn ein Feature später sehr groß wird, kann es eine eigene Mod werden.

Kandidaten für spätere eigene Mods:

- QuestMod
- RaidMod
- WorldgenVillageMod
- Economy/BaseWorkerMod
- AdvancedMagicNpcMod

Aber jetzt noch nicht splitten.
Jetzt zuerst eine stabile Foundation bauen.

---

# 17. Wichtigster Ziel-Satz

Die Mod soll nicht jeden NPC manuell behandeln.

Du lieferst Vorlagen:

- Prefabs
- Marker
- Slots
- Compositions
- Role-Groups
- Appearance-Pools
- Routine-Profile
- Movement-Profile
- Navigation-Profile
- Combat-Profile
- AI-/Mana-/Magic-Profile

Die Mod kombiniert daraus logisch passende Weltinhalte.

Java bleibt generisch.
JSON erzeugt die Vielfalt.

Das ist der gewünschte Zielzustand.


###


UPDATE Kurzer Bericht: typische Fehler-Muster, die sich gezeigt haben

Das Hauptschema ist:

Nicht erst handeln, dann prüfen.
Sondern immer:

erst prüfen → dann ändern → dann Erfolg ehrlich melden

1. Erst löschen, dann prüfen

Typischer Fehler:

Man macht zuerst clear() oder überschreibt eine Map und prüft danach erst, ob die neuen Daten gültig sind.

Warum gefährlich:
Wenn die neuen Daten kaputt sind, sind die alten Daten schon weg.

Besser:

erst neue Daten komplett prüfen, in temporäre Maps legen, doppelte IDs erkennen, dann erst alten Zustand ersetzen.

Name dafür:

atomic replace / transaktionales Ersetzen

Einfach gesagt:
Alles klappt oder nichts wird geändert.

2. Stille Ersetzung

Typischer Fehler:

Eine Map bekommt einfach:

put(id, value)

Wenn die ID schon existiert, wird der alte Wert still überschrieben.

Warum gefährlich:
Ein NPC, Marker oder Record kann heimlich ersetzt werden.

Besser:

Vorher prüfen:

existiert die ID schon?
wenn ja: Fehler werfen oder false zurückgeben
3. Null oder leere Strings nicht prüfen

Typischer Fehler:

Methoden nehmen npcId, worldId, markerId, roleId, npcName, aber prüfen sie nicht.

Warum gefährlich:
Später entstehen komische Fehler an anderer Stelle.

Besser:

Bei IDs fast immer prüfen:

nicht null
nicht blank

isBlank() ist besser als isEmpty(), weil " " auch ungültig ist.

4. void bei Methoden, die fehlschlagen können

Typischer Fehler:

Eine Methode verändert State, gibt aber nichts zurück.

Warum gefährlich:
Der Aufrufer weiß nicht, ob es geklappt hat.

Besser:

Bei wichtigen Aktionen ein Ergebnis zurückgeben, z. B.:

Erfolg
Fehlergrund
nichts geändert

Für Skeleton reicht manchmal boolean, später besser ein eigenes Result-Objekt.

5. return null als Platzhalter

Typischer Fehler:

Eine nicht fertige Methode gibt null zurück.

Warum gefährlich:
Andere Systeme könnten denken: „Okay, Ergebnis ist halt null“, und weiterlaufen.

Besser:

Bei noch nicht implementierter Logik lieber klar abbrechen:

UnsupportedOperationException + TODO

Dann ist sichtbar: Diese Logik existiert noch nicht.

6. Runtime ohne persistenten Record erzeugen

Typischer Fehler:

RuntimeNpc wird erstellt, obwohl kein NpcRecord existiert.

Warum gefährlich:
Dann gibt es Live-/Runtime-Daten ohne echte persistente Wahrheit.

Besser:

Runtime nur erzeugen, wenn der persistente Record existiert.

Merksatz:

Persistence ist die Wahrheit. Runtime ist nur die laufende Kopie.

7. Save-/Load-Ergebnisse ignorieren

Typischer Fehler:

saveStateSafely() oder loadState() wird aufgerufen, aber das Ergebnis wird nicht geprüft.

Warum gefährlich:
Ein Save kann fehlschlagen, aber das System tut so, als wäre alles okay.

Besser:

Jedes Save-/Load-Ergebnis sichtbar behandeln.

8. Skeleton tut so, als wäre es echte Logik

Typischer Fehler:

Eine Methode ist noch TODO, gibt aber scheinbar gültige Werte zurück.

Beispiel: false, null oder leerer Default-State kann okay sein, aber nur wenn klar ist, dass es Skeleton ist.

Warum gefährlich:
Später glaubt ein anderer Teil des Codes, das System sei schon fertig.

Besser:

Kommentare klar halten:

„Skeleton“
„noch nicht implementiert“
„verändert noch nichts“
„darf später erst mit Safety-Gates aktiv werden“
9. Read-only und mutierende Methoden vermischen

Typischer Fehler:

Eine Methode heißt „resolve“ oder „read“, verändert aber heimlich State.

Warum gefährlich:
Beim bloßen Lesen können Marker oder state.json verändert werden.

Besser:

Klare Trennung:

resolve/read = nur lesen
assign/clear/repair = darf ändern, aber nur mit Checks
10. Doppelte Klassen oder doppelte Systeme

Typischer Fehler:

Ähnliche Klassen existieren zweimal, z. B. eine alte und eine neue State-Schicht.

Warum gefährlich:
Man fixt später die falsche Datei.

Besser:

Früh entscheiden:

welche Klasse ist aktiv?
welche ist Legacy?
welche darf gelöscht oder ignoriert werden?
11. Konstruktoren ohne requireNonNull

Typischer Fehler:

Services werden im Konstruktor gespeichert, aber nicht geprüft.

Warum gefährlich:
Der Fehler kommt später irgendwo anders und ist schwerer zu finden.

Besser:

Direkt im Konstruktor prüfen.
Dann weiß man sofort: Dieser Service wurde falsch gebaut.

12. Direkte Map-/Collection-Mutation von außen

Typischer Fehler:

Eine Klasse gibt ihre interne Map oder Collection direkt heraus.

Warum gefährlich:
Außenstehender Code kann die Daten ändern, ohne Checks auszulösen.

Besser:

Nur unveränderbare Kopien oder read-only Views herausgeben.

Merksatz für dein Projekt

Für NPCMod gilt fast immer:

Keine stille Änderung. Keine stille Reparatur. Kein stiller Erfolg.

Sicheres Muster:

Eingaben prüfen
Existenz prüfen
Duplikate prüfen
temporär vorbereiten
erst dann übernehmen
Fehler ehrlich zurückgeben oder sichtbar abbrechen
Runtime nie als persistente Wahrheit behandeln