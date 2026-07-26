HAUPT-GROUP-JSON
Datei z. B.:
Server/NPC/Keystone/npc/lumberjack/lumberjack_group.json

Id
= eindeutige ID dieser Group-Datei.
Beispiel: lumberjack_group

Version
= Versionsnummer der JSON-Struktur.
Wichtig für spätere Migrationen.

Type
= Typ dieser Datei.
Beispiel: NpcGroup

Namespace
= optionaler Modul-Namespace.
Wenn fehlt: automatisch keystone.
Beispiel: keystone

SharedProfiles
= Profile, die alle Variants dieser Group gemeinsam nutzen.
Kann später auch leer sein, wenn alles pro Variant gesetzt wird.

Variants
= Liste der RoleIds / Baupläne innerhalb dieser Gruppe.


────────────────────────────────────────
VARIANT / ROLEID-EINTRAG
────────────────────────────────────────

RoleId
= Keystone-Logik-ID / Job / soziale Rolle.
Beispiel: lumberjack, blacksmith, trader

NamespacedRoleId
= intern normalisierte RoleId.
Beispiel: keystone:lumberjack
Muss nicht zwingend in JSON stehen, kann beim Laden erzeugt werden.

Engine
= Block für Hytale-Engine-Anbindung.

Engine.HytaleRole
= Hytale-Role-Datei unter Server/NPC/Roles/.
Beispiel: Keystone_Human_Worker

Display
= Anzeigeinformationen, keine technische Logik-ID.

Display.FallbackName
= Name, falls keine Übersetzung genutzt wird.
Beispiel: Lumberjack

Display.NameTranslationKey
= Übersetzungs-Key.
Beispiel: keystone.npc.roles.lumberjack.name

Profiles
= Map<String, String>.
Enthält alle Profil-Verweise, bekannte und unbekannte Keys.

Markers
= Marker-Anforderungen dieser RoleId.

Debug
= role-spezifische Debug-Einstellungen.


────────────────────────────────────────
PROFILES-MAP
────────────────────────────────────────

Profiles.SpeciesPool
= Pool für Volk/Wesen.
Beispiel: human, elf, orc, vampire.

Profiles.BodyPool
= Pool für Körperprofil.
Beispiel: male, old_male, female, old_female.

Profiles.AppearancePool
= Pool für dauerhaftes Grundaussehen.
Kann Species + Body + Gesicht + Haare + Ohren usw. enthalten.

Profiles.OutfitPool
= Pool für Kleidung.
Darf später alle 2–4 Ingame-Tage wechseln.

Profiles.CompositionPool
= Pool für Struktur-/Gruppen-Zusammensetzung.
Beispiel: blacksmith_couple, trader_family.

Profiles.Routine
= Tagesablauf / Zeitplan / Verhalten über den Tag.

Profiles.Actions
= Action-Lexikon.
Beispiel: chop_wood, eat_meal, open_chest.

Profiles.Movement
= Bewegungswerte.
Beispiel: normaler Gang, langsamer alter Mensch.

Profiles.Navigation
= Navigationslogik.
Beispiel: friendly_worker, guard_patrol.

Profiles.Combat
= Kampfverhalten.
Optional bei friedlichen NPCs.

Profiles.Spawn
= Spawn-Regeln.
Beispiel: structure_bound oder territory_bound.

Profiles.Persistence
= Speicher-/Respawn-Regeln.
Beispiel: RespawnAfterRestart.

Profiles.Events
= Event-Verhalten.
Beispiel: Raid, Feuer, Alarm, safety_zone.

Profiles.Dialogue
= Dialogprofil.
Optional / Custom.

Profiles.Trading
= Handelsprofil.
Optional / Custom.

Profiles.Reputation
= Ruf-/Beziehungsprofil.
Optional / Custom.

Profiles.CustomSomething
= unbekannter neuer Key.
Wird geladen/basic validiert, aber nur ausgeführt, wenn später ein Handler existiert.


────────────────────────────────────────
MARKERS-BLOCK
────────────────────────────────────────

Markers.RequiredMarkers
= Marker, die diese RoleId grundsätzlich braucht.

Markers.MarkerRoles
= Zuordnung MarkerName -> MarkerType.
Beispiel: bed -> BED

Markers.RoutineMarkers
= Marker, die Routine-Schritte nutzen dürfen.
Optional später.

Markers.EventMarkers
= Marker für Events.
Beispiel: safety_zone, alarm_point.

Markers.OptionalMarkers
= Marker, die genutzt werden, wenn vorhanden.
Fehlen darf kein harter Fehler sein.


────────────────────────────────────────
DEBUG-BLOCK
────────────────────────────────────────

Debug.Enabled
= Debug für diese RoleId grundsätzlich aktiv.

Debug.LogRoutine
= Routine-Logs für diese RoleId.

Debug.LogMarkers
= Marker-Logs für diese RoleId.

Debug.LogNavigation
= Navigations-Logs für diese RoleId.

Debug.LogActions
= Action-Logs für diese RoleId.

Wichtig:
Global Debug muss zusätzlich true sein.


────────────────────────────────────────
PROFILETYPE-REGELN
────────────────────────────────────────

ProfileKey
= Name des Profil-Keys.
Beispiel: Routine, Actions, OutfitPool, Dialogue.

Required
= ob dieses Profil fehlen darf.

ExpectedType
= erwarteter JSON-Type, falls genutzt.
Beispiel: RoutineProfile, ActionProfile, OutfitPool.

ValidationMode
= wie streng geprüft wird.
Beispiel: Basic, Actions, Routine, Pool.

KnownProfileType
= true bei Core-Profilen, false bei Custom-Profilen.

HandlerKey
= optionaler späterer System-Handler.
Beispiel: DialogueHandler.


────────────────────────────────────────
POOL-JSON
────────────────────────────────────────

Id
= eindeutige Pool-ID.

Version
= Versionsnummer.

Type
= Pool-Typ.
Beispiel: BodyPool, OutfitPool, CompositionPool.

Entries
= Liste möglicher Einträge.

Entries.Id
= stabile Entry-ID.
Diese ID kann später in state.json gespeichert werden.

Entries.Weight
= Gewicht für Zufallsauswahl.

Entries.Profile
= Verweis auf konkretes Profil.

Entries.Tags
= optionale Tags.
Beispiel: male, female, worker, city, village.

Wichtig:
Pool wird in P3 nur geladen, nicht ausgewürfelt.


────────────────────────────────────────
STRUCTURE / PREFAB-JSON
────────────────────────────────────────

PrefabId
= eindeutige Prefab-ID.
Beispiel: simple_stone_worker_house_blacksmith

PrefabPath
= echter Hytale-Prefab-Pfad, falls getrennt.

ProvidesContracts
= welche Funktionen dieses Prefab liefert.
Beispiel: ResidenceContract, BlacksmithWorkstationContract.

Tags
= Stil-/Region-Tags.
Beispiel: stone, city, worker, medium.

MarkerDefinitions
= welche Marker im Prefab erwartet werden.

Slots
= Wohn-/Arbeitsplätze.
Beispiel: resident_1, resident_2, work_slot_1.


────────────────────────────────────────
CONTRACT-JSON
────────────────────────────────────────

Id
= Contract-ID.
Beispiel: BlacksmithWorkstationContract

Version
= Versionsnummer.

Type
= Contract-Typ.
Beispiel: StructureContract

RequiredMarkers
= Marker, die eine Struktur liefern muss.

RequiredSlots
= benötigte Slots.
Beispiel: ResidenceSlots: 2

Purpose
= kurzer Zweck.
Beispiel: erlaubt Schmiedearbeit.


────────────────────────────────────────
COMPOSITION-JSON
────────────────────────────────────────

Id
= CompositionPool-ID.

Version
= Versionsnummer.

Type
= CompositionPool.

Entries
= mögliche Besetzungen.

Entries.Id
= stabile Composition-ID.

Entries.Weight
= Gewichtung.

Entries.RequiredContracts
= welche Contracts die Struktur liefern muss.

Entries.Npcs
= welche RoleIds gespawnt werden sollen.

Entries.SlotAssignments
= welcher NPC welchen Slot bekommt.

Beispiel:
resident_1 -> keystone:blacksmith
resident_2 -> keystone:worker_spouse


────────────────────────────────────────
TERRITORY-JSON
────────────────────────────────────────

Id
= Territory-Profil-ID.

Version
= Versionsnummer.

Type
= TerritoryProfile.

TerritoryRadius
= Bewegungsradius.

PatrolRadius
= Patrouillenradius.

ChaseRadius
= Verfolgungsradius.

ReturnRadius
= Rückkehr-/Reset-Radius.

SpawnAnchors
= mögliche Spawnpunkte.

RolePool
= welche RoleIds dort spawnen dürfen.

MaxNpcCount
= maximale NPC-Anzahl im Gebiet.


────────────────────────────────────────
STATE.JSON — SPÄTER, NICHT P3
────────────────────────────────────────

NpcId
= konkrete NPC-Instanz.

RoleId / NamespacedRoleId
= welcher Bauplan genutzt wird.

EntityUuid
= Hytale-Entity zum Wiederfinden.

WorldKey / WorldId
= Weltzuordnung.

Status
= ACTIVE, NEEDS_RELINK, MISSING_ENTITY usw.

LastKnownPosition
= letzte bekannte Position.

SelectedSpeciesId
= gewähltes Volk/Wesen.

SelectedBodyProfileId
= gewählter Körper.

SelectedAppearanceId
= dauerhaftes Aussehen.

SelectedOutfitId
= ursprüngliches Outfit beim Spawn, falls benötigt.

CurrentOutfitId
= aktuell getragenes Outfit.

OutfitPoolId
= Pool, aus dem Kleidung gewechselt wird.

LastOutfitChangeDay
= letzter Outfit-Wechseltag.

NextOutfitChangeDay
= nächster Outfit-Wechseltag.

SelectedCompositionId
= gewählte Haus-/Gruppen-Zusammensetzung.

SelectedPrefabId
= gewählte Prefab-Variante.

StructureInstanceId
= konkrete Strukturinstanz.

TerritoryId
= konkrete Gebietsbindung bei Territory-bound NPCs.

SpawnAnchorId
= Spawnanker bei Territory-bound NPCs.

HomeSlotId
= Wohnslot.

WorkSlotId
= Arbeitsslot.

MarkerAssignments
= Zuordnung NPC -> MarkerId.

MarkerRecords
= echte Marker-Koordinaten.


────────────────────────────────────────
NICHT IN STATE.JSON
────────────────────────────────────────

Routine-Inhalte
Actions-Inhalte
Movement-Inhalte
Navigation-Inhalte
Combat-Inhalte
Skills-Inhalte
Dialogue-Inhalte
Trading-Inhalte
Events-Inhalte
ProfileTypeRules
NpcProfileRefs
vollständige Pool-Inhalte
HytaleRole-Datei-Inhalte
EntityRef
RuntimeNpc
aktive Navigation
laufende Action
Tick-State


────────────────────────────────────────
KURZREGEL
────────────────────────────────────────

Resource-JSONs:
definieren Möglichkeiten und Baupläne.

state.json:
speichert konkrete Auswahl und konkrete Weltinstanzen.

RoleId:
steht in group.json / Variant.

HytaleRole:
steht in Variant.Engine und verweist auf Server/NPC/Roles/.

Profile:
stehen als flexible Map in Variant.Profiles.

Pools:
werden geladen und geprüft, aber erst später pro NPC ausgewürfelt.

Contracts:
sagen, ob eine Struktur funktional passt.

Tags:
helfen bei Stil, Region und Auswahl.



ZIELARCHITEKTUR — NPC-Bindung: Structure-bound und Territory-bound

Grundidee:
Nicht jeder NPC ist an ein Haus gebunden.

Es gibt mindestens zwei große Bindungsarten:

1. Structure-bound NPC
= an Haus / Prefab / StructureInstance gebunden

2. Territory-bound NPC
= an Gebiet / SpawnAnchor / GuardZone gebunden


────────────────────────────────────────
Structure-bound NPC
────────────────────────────────────────

Structure-bound NPCs sind NPCs, die zu einer konkreten Struktur gehören.

Beispiele:
- Bewohner eines Hauses
- Schmied in einer Schmiede
- Händler in einem Shop
- Holzfäller in einem Worker-House + Arbeitsbereich
- Stadtwache in einem Wachhaus

Sie sind gebunden an:

- structureInstanceId
- selectedPrefabId
- selectedCompositionId
- homeSlotId
- workSlotId
- markerAssignments
- MarkerRecords / Marker-Koordinaten

Beispiel:

simple_stone_worker_house_blacksmith
liefert:
- ResidenceContract
- BlacksmithWorkstationContract

simple_stone_worker_house_shop
liefert:
- ResidenceContract
- ShopContract

simple_stone_worker_house_storage
liefert:
- ResidenceContract
- StorageWorkContract

Wichtig:
Der Dateiname ist nur für Menschen.
Die Contracts sind die Wahrheit für die Logik.

Nicht:

blacksmith darf nur:
- simple_stone_worker_house_blacksmith
- simple_wooden_worker_house_blacksmith

Sondern:

blacksmith braucht:
- ResidenceContract
- BlacksmithWorkstationContract

Und jedes Prefab sagt selbst, was es liefert.

Dadurch kann später ein neues Prefab ergänzt werden:

orc_blacksmith_hut
liefert:
- ResidenceContract
- BlacksmithWorkstationContract

Dann passt es automatisch für Blacksmith-Compositions,
ohne dass irgendwo eine große Allowed-Prefab-Liste gepflegt werden muss.


────────────────────────────────────────
Contracts und Tags
────────────────────────────────────────

Contracts entscheiden:
Funktioniert diese Struktur für diese Role / Composition?

Tags entscheiden:
Welche passende Struktur ist stilistisch / regional sinnvoll?

Beispiel Contracts:

simple_stone_worker_house_blacksmith
Provides:
- ResidenceSlots: 2
- BlacksmithWorkstation: 1

blacksmith_couple
Requires:
- ResidenceSlots: 2
- BlacksmithWorkstation: 1

→ passt.

Beispiel Tags:

simple_stone_worker_house_blacksmith
Tags:
- stone
- city
- worker
- medium

simple_wooden_worker_house_blacksmith
Tags:
- wood
- village
- worker
- small

Dann kann Worldgen sagen:

Stadtgebiet:
- bevorzuge tag city
- bevorzuge tag stone

Landgebiet:
- bevorzuge tag village
- bevorzuge tag wood

Wichtig:
Contracts = harte technische Eignung.
Tags = weiche Auswahl / Stil / Region.


────────────────────────────────────────
Structure-bound state.json
────────────────────────────────────────

Für Structure-bound NPCs speichert state.json später:

NpcRecord:
- npcId
- roleId / namespacedRoleId
- entityUuid
- worldKey / worldId
- status
- lastKnownPosition
- selectedSpeciesId
- selectedBodyProfileId
- selectedAppearanceId
- currentOutfitId
- outfitPoolId
- selectedCompositionId
- selectedPrefabId
- structureInstanceId
- homeSlotId
- workSlotId
- markerAssignments

StructureRecord:
- structureInstanceId
- selectedPrefabId
- selectedCompositionId
- worldKey / worldId
- position
- rotation
- occupiedSlots
- markerRecords

MarkerRecord:
- markerId
- markerName
- markerType
- structureInstanceId
- slotId optional
- relativePosition
- worldPosition optional/cache


────────────────────────────────────────
Territory-bound NPC
────────────────────────────────────────

Territory-bound NPCs sind NPCs, die nicht an ein Haus gebunden sind,
sondern an ein Gebiet.

Beispiele:
- Bandit bewacht ein Lager
- Wolf bewegt sich in einem Wald-Radius
- hostile animal bewacht eine Höhle
- Guard patrouilliert um ein Tor
- Event-Gegner spawnt an einem Raid-Anchor

Sie sind gebunden an:

- spawnAnchorId
- territoryId
- territoryCenter
- territoryRadius
- guardZone
- patrolMarkers optional
- leashRadius
- chaseRadius
- returnRadius

Beispiel:

RoleId: keystone:bandit_guard
HytaleRole: Keystone_Hostile_Humanoid

Territory:
- territoryId: bandit_camp_001
- spawnAnchorId: bandit_guard_spawn_1
- centerPosition: X/Y/Z
- patrolRadius: 24
- chaseRadius: 40
- returnRadius: 55

Verhalten:
- innerhalb Radius patrouillieren
- bei Spieler-Kontakt verfolgen
- nach Chase zurück zum Anchor
- bei zu großer Entfernung zurücksetzen oder respawnen


────────────────────────────────────────
Territory-bound state.json
────────────────────────────────────────

Für Territory-bound NPCs speichert state.json später eher:

NpcRecord:
- npcId
- roleId / namespacedRoleId
- entityUuid
- worldKey / worldId
- status
- lastKnownPosition
- spawnAnchorId
- territoryId
- territoryCenter
- territoryRadius
- patrolAssignment optional
- selectedSpeciesId
- selectedBodyProfileId
- selectedAppearanceId
- currentOutfitId optional

TerritoryRecord:
- territoryId
- worldKey / worldId
- centerPosition
- radius
- anchorIds
- patrolMarkerIds
- maxNpcCount
- activeNpcIds

SpawnAnchorRecord:
- spawnAnchorId
- territoryId
- position
- anchorType
- allowedRoleIds / rolePool
- respawnRules


────────────────────────────────────────
Structure-bound vs Territory-bound
────────────────────────────────────────

Structure-bound:
- braucht StructureInstance
- nutzt StructureContracts
- nutzt Slots
- nutzt MarkerRecords aus Prefab/Structure
- gut für Häuser, Shops, Schmieden, Stadtgebäude

Territory-bound:
- braucht SpawnAnchor / TerritoryZone
- nutzt Radius / GuardZone / PatrolMarker
- braucht kein Haus
- gut für hostile NPCs, Tiere, Guards, Events, Camps

Beide nutzen trotzdem:

- RoleId
- HytaleRole
- Species/Body
- Outfit
- Routine/Actions/Movement/Navigation/Combat
- state.json für konkrete Instanzdaten

Der Unterschied ist nur die Bindung:

Structure-bound:
NPC hängt an Struktur + Slots + Marker.

Territory-bound:
NPC hängt an Gebiet + Anchor + Radius.


────────────────────────────────────────
P3-Auswirkung
────────────────────────────────────────

In P3 wird noch nichts gespawnt.

P3 lädt nur Baupläne:

- RoleId
- HytaleRole
- SpeciesPool/Profile
- BodyPool/Profile
- OutfitPool
- Routine
- Actions
- Movement
- Navigation
- Combat
- Events
- Structure-/Prefab-/Composition-Refs
- Territory-/SpawnAnchor-/GuardZone-Refs
- Marker-/Contract-Definitionen

P3 macht NICHT:
- kein Prefab platzieren
- keine StructureInstance erzeugen
- keine TerritoryInstance erzeugen
- keine SpawnAnchor-Instanz erzeugen
- keine MarkerRecords schreiben
- keine markerAssignments schreiben
- keine Composition auswählen
- keine NPCs spawnen
- keine state.json ändern


────────────────────────────────────────
Wichtigster Merksatz
────────────────────────────────────────

Structure-bound NPC
= gehört zu einer konkreten Struktur.

Territory-bound NPC
= gehört zu einem konkreten Gebiet.

Contracts
= sagen, ob eine Struktur funktional passt.

Tags
= helfen, passende Varianten nach Stil/Region auszuwählen.

RoleId
= sagt, welche Logik der NPC hat.

state.json
= speichert später die konkrete Bindung:
  Struktur oder Territory.



#### Struktur

src/main/resources/
└── Server/
    └── NPC/
        ├── Roles/
        │   ├── Keystone_Human_Worker.json
        │   ├── Keystone_Human_Guard.json
        │   ├── Keystone_Hostile_Humanoid.json
        │   ├── Keystone_Friendly_Animal.json
        │   └── Keystone_Hostile_Animal.json
        │
        └── Keystone/
            ├── config/
            │   └── debug.json
            │
            ├── npc/
            │   ├── index.json
            │   │
            │   ├── lumberjack/
            │   │   └── lumberjack_group.json
            │   │       └── HIER stehen die RoleIds
            │   │
            │   ├── blacksmith/
            │   │   └── blacksmith_group.json
            │   │       └── HIER stehen die RoleIds
            │   │
            │   ├── trader/
            │   │   └── trader_group.json
            │   │       └── HIER stehen die RoleIds
            │   │
            │   └── guard/
            │       └── guard_group.json
            │           └── HIER stehen die RoleIds
            │
            ├── profiles/
            │   ├── species/
            │   │   ├── human.json
            │   │   ├── elf.json
            │   │   ├── orc.json
            │   │   └── vampire.json
            │   │
            │   ├── body/
            │   │   ├── human_male.json
            │   │   ├── human_old_male.json
            │   │   ├── human_female.json
            │   │   ├── human_young_female.json
            │   │   └── human_old_female.json
            │   │
            │   ├── movement/
            │   │   ├── human_walk.json
            │   │   └── old_human_walk.json
            │   │
            │   ├── navigation/
            │   │   ├── friendly_worker.json
            │   │   └── guard_patrol.json
            │   │
            │   ├── combat/
            │   │   ├── peaceful.json
            │   │   ├── defensive_guard.json
            │   │   └── hostile_melee.json
            │   │
            │   ├── spawn/
            │   │   ├── structure_bound.json
            │   │   └── territory_bound.json
            │   │
            │   ├── persistence/
            │   │   └── persistent_citizen.json
            │   │
            │   ├── events/
            │   │   ├── worker_events.json
            │   │   └── guard_events.json
            │   │
            │   └── dialogue/
            │       └── worker_dialogue.json
            │
            ├── pools/
            │   ├── species/
            │   │   └── human_worker_species_pool.json
            │   │
            │   ├── body/
            │   │   └── human_worker_body_pool.json
            │   │
            │   ├── outfits/
            │   │   ├── lumberjack_outfit_pool.json
            │   │   ├── blacksmith_outfit_pool.json
            │   │   ├── trader_outfit_pool.json
            │   │   └── worker_common_outfit_pool.json
            │   │
            │   └── compositions/
            │       ├── simple_worker_house_blacksmith_compositions.json
            │       ├── simple_worker_house_shop_compositions.json
            │       └── bandit_camp_compositions.json
            │
            ├── structures/
            │   ├── prefabs/
            │   │   ├── worker_house/
            │   │   │   ├── simple_stone_worker_house_blacksmith.json
            │   │   │   ├── simple_stone_worker_house_shop.json
            │   │   │   ├── simple_stone_worker_house_storage.json
            │   │   │   ├── simple_wooden_worker_house_blacksmith.json
            │   │   │   ├── simple_wooden_worker_house_shop.json
            │   │   │   └── simple_wooden_worker_house_storage.json
            │   │   │
            │   │   └── camps/
            │   │       └── bandit_camp_basic.json
            │   │
            │   ├── contracts/
            │   │   ├── residence_contract.json
            │   │   ├── blacksmith_workstation_contract.json
            │   │   ├── shop_contract.json
            │   │   ├── storage_work_contract.json
            │   │   └── territory_guard_contract.json
            │   │
            │   └── tags/
            │       ├── city.json
            │       ├── village.json
            │       ├── stone.json
            │       └── wood.json
            │
            └── territories/
                ├── bandit_camp_territory.json
                ├── wolf_forest_territory.json
                └── guard_gate_territory.json