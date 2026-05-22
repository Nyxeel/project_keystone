# P3 PLAN — DefinitionLoader / DefinitionRegistry
# Neues Biom-, Theme- und PoolGroup-System
# Version: P3 Biome-System v1

ZIEL VON P3:
NPC-, Biome-, Theme-, Pool-, Structure- und Composition-Definitionen aus resources laden,
prüfen und im RAM registrieren.

P3 lädt Baupläne.
P3 erzeugt keine echten NPCs.
P3 platziert keine Gebäude.
P3 würfelt keine Pools aus.
P3 schreibt keine state.json.

Die wichtigste neue Regel:

Biom → Themes → PoolGroups/Pools → spätere konkrete Auswahl

Nicht mehr:

Spielerposition → Biom → Gebäude
AppearancePool → selectedAppearanceId
FolkPool als Hauptsystem
simple_worker_house_*
sand_lumberjack

Sondern:

Placement-Position → Biom → StructureTheme
BodyTheme → BodyPoolGroup → BodyPool
NameTheme → NamePoolGroup → NamePool
OutfitTheme → OutfitPoolGroup → OutfitPool
StructureTheme → StructurePool → Structure/Prefab
RoleId bleibt unabhängig vom Biom


################################################################################
1. GRUNDREGEL: RESOURCE VS STATE
################################################################################

Resource-JSONs sind Baupläne.

Beispiele:
- NPC-Groups
- Profile
- Biome-Definitionen
- Theme-Bindings
- BodyPoolGroups
- NamePoolGroups
- OutfitPoolGroups
- StructurePools
- BodyPools
- NamePools
- OutfitPools
- CompositionPools
- Contracts
- Prefabs
- Territories

state.json speichert konkrete Welt-/NPC-Instanzen.

Beispiele:
- npcId
- roleId
- entityUuid
- worldId
- selectedBodyId
- selectedNameId
- currentOutfitId
- selectedCompositionId
- selectedPrefabId
- structureInstanceId
- markerAssignments

P3 lädt nur resources.
P3 verändert keine state.json.

Merksatz:

Resource = Was kann es geben?
state.json = Was gibt es wirklich in dieser Welt?


################################################################################
2. NICHT IN P3
################################################################################

In P3 gibt es nicht:

- kein Spawn
- kein Relink
- kein Auto-Respawn
- kein NPC aus state.json aktivieren
- kein saveStateSafely()
- keine EntityRef
- keine RuntimeNpc-Logik
- keine Pool-Auswahl
- kein Zufallswurf
- kein DataStore<T>
- keine MarkerAssignments schreiben
- keine state.json-Änderung
- kein automatischer Repair
- kein Outfit-Wechsel ausführen
- keine StructureInstance erzeugen
- keine TerritoryInstance erzeugen
- keine SpawnAnchor-Instanz erzeugen
- keine MarkerRecords erzeugen
- keine NPC-Entity erzeugen
- keine Spielerposition als Biome-Wahrheit verwenden


################################################################################
3. NEUES BIOM-/THEME-GRUNDSYSTEM
################################################################################

Biome-Dateien bestimmen Themes.

Beispiel:

Biome:
sand_desert

Theme-Bindung:
StructureTheme = sand
BodyTheme     = sand
OutfitTheme   = sand
NameTheme     = sand

Diese Themes werden später benutzt, um passende Pools zu finden.

Beispiel-Ablauf später:

1. Placement-System plant ein Gebäude.
2. Es gibt eine geplante Gebäude-Position.
3. An dieser Gebäude-Position wird das Biom geprüft.
4. Dieses Biom liefert StructureTheme.
5. StructureTheme filtert passende Gebäude.
6. Gebäude wird nur erlaubt, wenn es wirklich zum Theme passt.

Wichtig:

Nicht die Spielerposition entscheidet.
Die geplante Gebäude-Position entscheidet.

Falsch:

PlayerPosition → Biome → StructureTheme → Gebäude

Richtig:

PlacementCandidatePosition → Biome → StructureTheme → Gebäude

Warum:

Wenn ein Spieler an der Grenze zwischen sand und darklands läuft,
darf auf sand kein darklands-Gebäude spawnen
und in darklands kein sand-Gebäude.

Darum muss das Biom dort geprüft werden,
wo das Gebäude wirklich stehen soll.


################################################################################
4. BIOM-GRENZREGEL FÜR STRUCTURES
################################################################################

Ein Gebäude darf nicht nur wegen der Spielerposition ausgewählt werden.

Für Structure-Placement gilt später:

- Kandidatenposition bestimmen
- Biom an Kandidatenposition prüfen
- StructureTheme aus diesem Biom lesen
- StructurePool nach diesem StructureTheme filtern
- nur passende Structures erlauben

Bei größeren Gebäuden reicht nicht immer nur die Mitte.

Sichere spätere Regel:

Prüfe den Footprint:
- Center
- Ecke vorne links
- Ecke vorne rechts
- Ecke hinten links
- Ecke hinten rechts
- optional weitere Randpunkte

Wenn Pflichtpunkte unterschiedliche StructureThemes haben:

- normale Platzierung blockieren
- oder nur spezielle Übergangs-Strukturen erlauben

Beispiel:

sand_house darf nur spawnen, wenn der geprüfte Footprint sand ist.

darklands_house darf nur spawnen, wenn der geprüfte Footprint darklands ist.

P3 selbst macht diese Abfrage noch nicht.
P3 lädt nur die Regeln, damit spätere Systeme sie sauber anwenden können.


################################################################################
5. ROLEID BLEIBT JOB / LOGIK, NICHT BIOM
################################################################################

RoleId beschreibt den Job oder die logische NPC-Rolle.

Richtig:

keystone:lumberjack
keystone:blacksmith
keystone:trader
keystone:guard
keystone:worker_spouse

Falsch:

keystone:sand_lumberjack
keystone:darklands_lumberjack
keystone:sand_blacksmith
keystone:forest_trader

Warum:

Das Biom soll Aussehen, Namen, Kleidung und Gebäude-Stil bestimmen.
Die RoleId soll bestimmen, was der NPC fachlich ist.

Also:

Biome/Theme entscheidet Stil.
RoleId entscheidet Funktion.

Beispiel:

RoleId:
keystone:trader

In sand_desert:
- BodyTheme sand
- OutfitTheme sand
- NameTheme sand
- StructureTheme sand

In darklands:
- BodyTheme darklands
- OutfitTheme darklands
- NameTheme darklands
- StructureTheme darklands

Aber RoleId bleibt:

keystone:trader


################################################################################
6. KEINE SAND-LUMBERJACKS
################################################################################

Für das neue System gilt:

Keine Sand-Lumberjacks.

Das bedeutet:

- Kein sand_lumberjack als eigene RoleId.
- Kein sand_lumberjack_group.json als eigene Hauptlogik.
- Kein sand_lumberjack nur wegen Biom.
- Keine biome-spezifische Kopie derselben Job-Rolle.

Lumberjack bleibt lumberjack.

Aber:

Ein Lumberjack kann später je nach Biom unterschiedlich aussehen,
wenn die Definition ihn in diesem Biom überhaupt erlaubt.

Wichtig für dein aktuelles Design:

Wenn du keine Sand-Lumberjacks willst,
darf die Composition / Structure / Biome-Regel in sand_desert keinen lumberjack auswählen.

Also:

Sand-Biom kann erlauben:
- trader
- blacksmith
- guard
- citizen

Und blockieren:
- lumberjack

Das ist keine andere RoleId.
Das ist eine Biome-/Composition-/Structure-Regel.


################################################################################
7. BODYPOOLGROUP / BODYPOOL
################################################################################

BodyPoolGroup:

Eine BodyPoolGroup ist eine Auswahlgruppe.
Sie sagt, welche BodyPools für welches BodyTheme erlaubt sind.

Beispiel:

human_body_pool_group.json

enthält Zuordnung zu:

BodyTheme sand:
- sand_male_body_pool.json
- sand_female_body_pool.json

BodyTheme forest:
- forest_male_body_pool.json
- forest_female_body_pool.json

BodyTheme mountain:
- mountain_male_body_pool.json
- mountain_female_body_pool.json

BodyTheme darklands:
- darklands_male_body_pool.json
- darklands_female_body_pool.json

BodyPool:

Ein BodyPool enthält Körper-/Identitätsoptik.

BodyPool enthält:
- Körper
- Haut
- Haare
- Gesicht
- Augen

BodyPool enthält NICHT:
- Kleidung
- Rüstung
- Job-Outfit

Warum:

Körper ist Identität.
Kleidung ist Outfit.

BodyPool bleibt nach Spawn stabil.

Später in state.json:

selectedBodyId

Nicht mehr:

selectedAppearanceId


################################################################################
8. NAMEPOOLGROUP / NAMEPOOL
################################################################################

NamePoolGroup:

Eine NamePoolGroup ist eine Auswahlgruppe für Namen.
Sie sagt, welche NamePools für welches NameTheme erlaubt sind.

Beispiel:

human_name_pool_group.json

NameTheme sand:
- sand_male_name_pool.json
- sand_female_name_pool.json

NameTheme forest:
- forest_male_name_pool.json
- forest_female_name_pool.json

NameTheme darklands:
- darklands_male_name_pool.json
- darklands_female_name_pool.json

NamePool:

Ein NamePool enthält mögliche Namen.

NamePool kann trennen nach:
- Theme
- Geschlecht
- Kultur
- Rolle
- Seltenheit

P3 prüft nur:
- Datei existiert
- JSON ist gültig
- Id existiert
- Version existiert
- Type == NamePool
- Entries existieren
- Entries haben stabile IDs oder stabile Namen-Keys

P3 wählt keinen Namen aus.

Später in state.json möglich:

selectedNameId
displayName


################################################################################
9. OUTFITPOOLGROUP / OUTFITPOOL
################################################################################

OutfitPoolGroup:

Eine OutfitPoolGroup ist eine Auswahlgruppe für Kleidung.
Sie sagt, welche OutfitPools für welches OutfitTheme erlaubt sind.

Beispiel:

human_worker_outfit_pool_group.json

OutfitTheme sand:
- sand_worker_outfit_pool.json
- sand_trader_outfit_pool.json
- sand_blacksmith_outfit_pool.json

OutfitTheme forest:
- forest_worker_outfit_pool.json
- forest_trader_outfit_pool.json
- forest_blacksmith_outfit_pool.json

OutfitPool:

Ein OutfitPool enthält Kleidung.

OutfitPool enthält:
- Kleidung
- Arbeitskleidung
- Berufskleidung
- optionale Varianten

OutfitPool enthält NICHT:
- Körper
- Haut
- Haare
- Gesicht
- Augen

Outfit darf später wechseln.

Beispiel:
Alle 2–4 Ingame-Tage neue Kleidung aus gleichem passenden OutfitPool.

P3 prüft nur.
P3 wechselt kein Outfit.

Später in state.json:

selectedOutfitId optional
currentOutfitId
outfitPoolId
lastOutfitChangeDay
nextOutfitChangeDay


################################################################################
10. STRUCTURETHEME / STRUCTUREPOOL
################################################################################

StructureTheme bestimmt den Gebäude-Stil.

Beispiele:

StructureTheme sand:
- sand_house_trader
- sand_house_blacksmith
- sand_guard_post

StructureTheme forest:
- forest_house_trader
- forest_house_blacksmith
- forest_guard_post

StructureTheme darklands:
- darklands_house_trader
- darklands_house_blacksmith
- darklands_guard_post

StructurePool:

Ein StructurePool enthält mögliche Structures/Prefabs für ein StructureTheme.

Wichtig:

StructureTheme kommt vom Biom an der geplanten Gebäude-Position,
nicht vom Spieler.

P3 prüft:

- StructurePool existiert
- StructurePool hat Id
- StructurePool hat Version
- StructurePool hat Type == StructurePool
- StructureTheme existiert
- Entries existieren
- Entries verweisen auf existierende Structure/Prefab-Definitionen
- keine Entry verweist auf fehlende Datei

P3 platziert kein Gebäude.


################################################################################
11. COMPOSITION-NAMING
################################################################################

Alte Namen raus:

simple_worker_house_*

Neue Namen:

simple_house_<job>_compositions.json

Beispiele:

simple_house_trader_compositions.json
simple_house_blacksmith_compositions.json
simple_house_guard_compositions.json
simple_house_lumberjack_compositions.json

Warum:

worker ist zu ungenau.
Der Job soll klar im Namen stehen.

Composition sagt später:

Welche NPC-Rollen gehören zu dieser Structure?

Beispiel:

simple_house_trader_compositions.json

kann enthalten:
- trader
- worker_spouse
- optional child
- optional guard

P3 prüft nur:

- Composition-Datei existiert
- JSON ist gültig
- Id existiert
- Version existiert
- Type == CompositionPool oder CompositionDefinition
- RoleIds existieren oder sind bewusst als später markiert
- Slot-Namen sind gültig
- RequiredTags/SupportedTags sind gültig, falls vorhanden

P3 wählt keine Composition aus.


################################################################################
12. VARIANT = GENAU EINE ROLEID
################################################################################

Eine Variant ist genau ein NPC-Bauplan.

Richtig:

Variant:
RoleId = trader

Variant:
RoleId = blacksmith

Variant:
RoleId = guard

Falsch:

Eine Variant enthält mehrere RoleIds:
- trader
- blacksmith
- guard

Warum:

Wenn eine Variant mehrere RoleIds enthält, weiß die Registry später nicht sauber:
- Welche RoleId wird gespawnt?
- Welche Profile gehören dazu?
- Welche Marker gelten?
- Welche Debug-Regeln gelten?
- Welche Display-Daten gelten?

Merksatz:

Group = Sammlung mehrerer Baupläne
Variant = genau ein Bauplan
RoleId = eindeutige ID dieses Bauplans
Composition = entscheidet später, welche RoleIds zusammen auftreten


################################################################################
13. SHAREDPROFILES + VARIANT.PROFILES
################################################################################

SharedProfiles sind Defaults für alle Variants einer Group.

Variant.Profiles überschreibt nur einzelne Keys.

Merge-Regel:

1. SharedProfiles laden
2. Variant.Profiles darüberlegen
3. Wenn gleicher Key existiert:
   Variant gewinnt

Beispiel:

SharedProfiles:
- Movement = profiles/movement/human_walk.json
- Navigation = profiles/navigation/friendly_worker.json
- Persistence = profiles/persistence/persistent_citizen.json
- BodyPoolGroup = pools/body/human_body_pool_group.json
- NamePoolGroup = pools/name/human_name_pool_group.json

Variant.Profiles:
- OutfitPoolGroup = pools/outfit/trader_outfit_pool_group.json
- Routine = npc/trader/routines/trader_day_cycle.json
- Actions = npc/trader/actions/trader_actions.json

Effektiv:

- Movement aus SharedProfiles
- Navigation aus SharedProfiles
- Persistence aus SharedProfiles
- BodyPoolGroup aus SharedProfiles
- NamePoolGroup aus SharedProfiles
- OutfitPoolGroup aus Variant
- Routine aus Variant
- Actions aus Variant

Wichtig:

Variant.Profiles löscht SharedProfiles nicht automatisch.
Variant.Profiles überschreibt nur gleiche Keys.

Wenn später ein Profil bewusst deaktiviert werden soll,
nicht mit null arbeiten.

Besser später:

DisabledProfiles:
- Dialogue


################################################################################
14. PROFIL-REFS BLEIBEN GENERISCH
################################################################################

Nicht als feste Java-Felder:

routine
actions
movement
navigation
combat
...

Sondern:

Map<String, NpcProfileRef>

Warum:

Neue Profile sollen später möglich sein, ohne jedes Mal Java umzubauen.

Beispiele für Profile-Keys:

Required:
- Routine
- Actions
- Movement
- Navigation
- Persistence

Optional:
- Combat
- Events
- Dialogue
- Trading
- Reputation
- SeasonalOutfits
- Spawn
- BodyPoolGroup
- NamePoolGroup
- OutfitPoolGroup
- StructurePool
- CompositionPool
- AI
- Mana
- Magic

Nicht mehr aktiv:
- FolkPool
- Appearance
- AppearancePool

Custom:
- CustomSomething
- VillageMood
- ReputationHook
- FutureMagicSystem

Regel:

Required fehlt nach Merge → Fehler
Optional fehlt → okay
Eingetragen, aber Datei fehlt → Fehler
Eingetragen, aber JSON kaputt → Fehler
Custom eingetragen und gültig → okay
Custom eingetragen, aber Datei fehlt/kaputt → Fehler
Nicht eingetragen → nicht aktiv


################################################################################
15. APPEARANCEPOOL IST RAUS
################################################################################

Das neue System nutzt nicht mehr AppearancePool als aktives System.

Raus:

- Appearance als aktives Optional-Core-Profil
- AppearancePool als aktives Optional-Core-Profil
- selectedAppearanceId
- selectedAppearanceId gegen AppearancePool prüfen

Warum:

AppearancePool vermischt zu viel:
- Körper
- Haut
- Haare
- Gesicht
- Augen
- Kleidung

Das neue System trennt sauber:

BodyPool:
- Körper
- Haut
- Haare
- Gesicht
- Augen

OutfitPool:
- Kleidung

NamePool:
- Namen

Darum:

selectedAppearanceId wird ersetzt durch:

selectedBodyId
selectedNameId optional
selectedOutfitId optional
currentOutfitId


################################################################################
16. FOLKPOOL IST NICHT DAS HAUPTSYSTEM
################################################################################

FolkPool ist im neuen P3 nicht das Hauptsystem.

Raus:

- FolkPool als zentrale Auswahl
- FolkPool als Pflichtsystem
- FolkPool als Ersatz für BodyTheme

Wenn später Species/Race gebraucht wird,
kann es als eigenes System zurückkommen.

Beispiele:
- human
- elf
- orc
- vampire
- wolf

Aber aktuell für Biom-Stil gilt:

BodyTheme
NameTheme
OutfitTheme
StructureTheme

Nicht:

FolkPool entscheidet alles.


################################################################################
17. HYTALEROLE VS ROLEID
################################################################################

HytaleRole:

Echte Hytale Engine-Role.
Liegt unter:

Server/NPC/Roles/

Beispiel:

HytaleRole = Keystone_Human_Worker

RoleId:

Keystone-interne Logik-ID.

Beispiel:

RoleId = keystone:trader
RoleId = keystone:blacksmith
RoleId = keystone:guard

Mehrere RoleIds dürfen dieselbe HytaleRole nutzen.

Beispiel:

keystone:trader
keystone:blacksmith
keystone:guard

dürfen alle nutzen:

HytaleRole = Keystone_Human_Worker

Wichtig:

Duplicate namespacedRoleId = Fehler
Duplicate HytaleRole = erlaubt

Verboten:

setRoleName("KeystoneNPC_<npcId>_<roleId>_Role")

Warum:

HytaleRole muss eine echte Role-Datei sein.
Keystone-Identität gehört in deine Mod-/State-Daten,
nicht in dynamische Engine-Role-Namen.


################################################################################
18. NAMESPACES
################################################################################

RoleId ohne Namespace:

trader

wird intern:

keystone:trader

RoleId mit Namespace:

othermod:trader

bleibt:

othermod:trader

Duplicate-Regel:

keystone:trader + keystone:trader = Fehler
keystone:trader + othermod:trader = erlaubt

Auch Profile/Pools/Assets sollten langfristig Namespace/AssetId vorbereiten.

NpcProfileRef sollte vorbereiten:

- namespace optional
- assetId optional
- path Pflicht

Für P3 reicht:

path ist Pflicht.
namespace/assetId sind optional vorbereitet.


################################################################################
19. RESOURCE ROOTS
################################################################################

KeystoneRoot:
Server/NPC/Keystone/

NpcRoot:
Server/NPC/Keystone/npc/

ProfilesRoot:
Server/NPC/Keystone/profiles/

BiomesRoot:
Server/NPC/Keystone/biomes/

PoolsRoot:
Server/NPC/Keystone/pools/

BodyPoolsRoot:
Server/NPC/Keystone/pools/body/

NamePoolsRoot:
Server/NPC/Keystone/pools/name/

OutfitPoolsRoot:
Server/NPC/Keystone/pools/outfit/

StructurePoolsRoot:
Server/NPC/Keystone/pools/structure/

StructuresRoot:
Server/NPC/Keystone/structures/

ContractsRoot:
Server/NPC/Keystone/structures/contracts/

PrefabsRoot:
Server/NPC/Keystone/structures/prefabs/

TerritoriesRoot:
Server/NPC/Keystone/territories/

RolesRoot:
Server/NPC/Roles/

ConfigRoot:
Server/NPC/Keystone/config/


################################################################################
20. RESOURCE PATH REGELN
################################################################################

index.json liegt hier:

Server/NPC/Keystone/npc/index.json

Group-Pfade aus index.json sind relativ zu:

Server/NPC/Keystone/npc/

Profile-Pfade sind relativ zu:

Server/NPC/Keystone/

PoolGroup-Pfade sind relativ zu:

Server/NPC/Keystone/

Pool-Pfade sind relativ zu:

Server/NPC/Keystone/

Structure-/Prefab-Pfade sind relativ zu:

Server/NPC/Keystone/

Contract-Pfade sind relativ zu:

Server/NPC/Keystone/

Territory-Pfade sind relativ zu:

Server/NPC/Keystone/

HytaleRole verweist auf:

Server/NPC/Roles/<HytaleRole>.json

Global Debug liegt unter:

Server/NPC/Keystone/config/debug.json

Sicherheitsregel:

Kein Pfad darf aus dem Resource-Root ausbrechen.

Also keine Pfade wie:

../
../../
/home/...
C:\...


################################################################################
21. P3.0 RESOURCE-PREFLIGHT
################################################################################

Ziel:

Vor dem Java-Loader prüfen, ob aktive resources grob sauber sind.

Prüfen:

- index.json existiert
- index.json ist gültiges JSON
- aktive Group-Dateien existieren
- aktive Group-Dateien sind gültiges JSON
- aktive Profile existieren
- aktive PoolGroups existieren
- aktive Pools existieren
- aktive Contracts existieren
- aktive Structures/Prefabs existieren
- aktive Territories existieren, falls eingetragen
- keine aktive Resource nutzt AppearancePool
- keine aktive Resource nutzt FolkPool als Hauptsystem
- keine aktive Resource nutzt simple_worker_house_*
- keine aktive Resource nutzt sand_lumberjack als RoleId

JSON-Key-Regel:

PascalCase für Hauptkeys:

Id
Version
Type
Profiles
Variants
RoleId
Engine
Display
Markers
Debug
Entries
BodyTheme
NameTheme
OutfitTheme
StructureTheme

Kleine Namen dürfen klein bleiben:

Marker:
- bed
- work
- door
- table_seat
- safety_zone
- market_stall

ActionIds:
- chop_wood
- open_chest
- eat_meal
- sleep


################################################################################
22. P3.1 DEFINITION-DATENMODELLE
################################################################################

P3 braucht reine Datenmodelle für Baupläne.

Wichtig:

Diese Modelle enthalten keine Runtime-Daten.
Diese Modelle enthalten keine state.json-Instanzdaten.

Modelle:

LoadedNpcDefinition:
- namespacedRoleId
- localRoleId
- groupId
- namespace
- engine
- display
- profileRefs
- markers
- debug
- sourcePath optional

NpcProfileRefs:
- Map<String, NpcProfileRef> profiles

NpcProfileRef:
- profileKey
- path
- namespace optional
- assetId optional
- knownProfileType
- required
- validationMode
- handlerKey optional später

ProfileTypeRule:
- profileKey
- required oder optional
- expectedType optional
- validationMode
- known/custom
- handlerKey optional später

ProfileTypeRegistry:
- Required-Core-Regeln
- Optional-Core-Regeln
- Custom-Fallback-Regel

NpcEngineDefinition:
- hytaleRole
- templateReference optional

NpcDisplayDefinition:
- fallbackName
- nameTranslationKey optional

NpcMarkerDefinition:
- requiredMarkers
- markerRoles
- routineMarkers optional
- eventMarkers optional
- safetyMarkers optional
- optionalMarkers optional

NpcDebugDefinition:
- enabled
- logRoutine
- logMarkers
- logNavigation
- logActions

Neue Theme-/Pool-Modelle:

LoadedBiomeDefinition:
- biomeId
- themes

BiomeThemeBinding:
- biomeId
- structureTheme
- bodyTheme
- outfitTheme
- nameTheme

LoadedBodyPoolGroup:
- poolGroupId
- entriesByTheme

LoadedNamePoolGroup:
- poolGroupId
- entriesByTheme

LoadedOutfitPoolGroup:
- poolGroupId
- entriesByTheme

LoadedStructurePool:
- poolId
- structureTheme
- entries

LoadedBodyPool:
- poolId
- bodyTheme
- entries

LoadedNamePool:
- poolId
- nameTheme
- entries

LoadedOutfitPool:
- poolId
- outfitTheme
- entries


################################################################################
23. P3.2 DEFINITIONREGISTRY
################################################################################

DefinitionRegistry hält geladene NPC-Definitionen im RAM.

Maps:

- byNamespacedRoleId
- byHytaleRole als MultiMap/List
- byGroupId optional

Regeln:

- Duplicate namespacedRoleId = Fehler
- Duplicate HytaleRole = erlaubt
- Duplicate HytaleRole höchstens Info/Warnung
- Registry ersetzt Daten atomisch
- Bei Fehler bleibt alte gültige Registry erhalten oder Registry bleibt leer
- Keine halb geladene Registry aktivieren
- Keine mutable Map nach außen geben

Methoden:

- replaceAll(Collection<LoadedNpcDefinition>)
- hasRoleId(String roleId)
- hasNamespacedRoleId(String namespacedRoleId)
- getByRoleId(String roleId)
- getByNamespacedRoleId(String namespacedRoleId)
- getAllByHytaleRole(String hytaleRole)
- isSpawnable(String roleId)
- size()

Wichtig:

isSpawnable(roleId) bedeutet nur:

Definition ist vollständig geladen und grundsätzlich verwendbar.

isSpawnable spawnt nichts.


################################################################################
24. P3.3 BIOME-/THEME-REGISTRY
################################################################################

BiomeThemeRegistry hält die Zuordnung:

BiomeId → Themes

Beispiel:

sand_desert:
- StructureTheme = sand
- BodyTheme = sand
- OutfitTheme = sand
- NameTheme = sand

darklands:
- StructureTheme = darklands
- BodyTheme = darklands
- OutfitTheme = darklands
- NameTheme = darklands

Regeln:

- Duplicate biomeId = Fehler
- fehlendes StructureTheme = Fehler
- fehlendes BodyTheme = Fehler
- fehlendes OutfitTheme = Fehler
- fehlendes NameTheme = Fehler
- Theme-Strings dürfen nicht leer sein
- Registry ist read-only nach außen

Wichtig:

Diese Registry sagt nur:
Welches Theme gehört zu welchem Biom?

Sie platziert nichts.
Sie liest keine Spielerposition.
Sie fragt keine Weltposition ab.


################################################################################
25. P3.4 POOLGROUP-REGISTRY
################################################################################

PoolGroupRegistry hält:

- BodyPoolGroups
- NamePoolGroups
- OutfitPoolGroups

Regeln:

- Duplicate PoolGroupId = Fehler
- jede PoolGroup hat Id
- jede PoolGroup hat Version
- jede PoolGroup hat Type
- jede PoolGroup hat Entries
- jede Entry verweist auf existierende Pool-Dateien
- Theme-Key darf nicht leer sein

Beispiel:

BodyPoolGroup:
human_body_pool_group

Theme sand:
- sand_male_body_pool
- sand_female_body_pool

Theme darklands:
- darklands_male_body_pool
- darklands_female_body_pool

P3 prüft nur.
P3 wählt keinen Pool aus.


################################################################################
26. P3.5 INDEX.JSON LADEN
################################################################################

index.json:

Server/NPC/Keystone/npc/index.json

Prüfen:

- Id vorhanden
- Version vorhanden
- Type == NpcIndex
- Groups vorhanden
- Groups nicht leer
- jeder Group-Eintrag ist String
- jede Group-Datei existiert
- jede Group-Datei liegt unter NpcRoot
- kein Path-Traversal

Wichtig:

Keine hardcoded lumberjack_group.json-only Lösung.

index.json ist die Quelle für aktive NPC-Groups.


################################################################################
27. P3.6 GROUP.JSON + VARIANTS PARSEN
################################################################################

Group prüfen:

- Id nicht leer
- Version vorhanden
- Type == NpcGroup
- Namespace leer erlaubt → default keystone
- SharedProfiles optional
- Variants vorhanden
- Variants nicht leer

Variant prüfen:

- RoleId nicht leer
- genau eine RoleId
- Engine.HytaleRole nicht leer
- Display.FallbackName optional, aber empfohlen
- Profiles als Map<String, String>
- Markers pro Variant
- Debug pro Variant optional

Merge:

EffectiveProfiles = SharedProfiles + Variant.Profiles

Wenn gleicher Key:

Variant.Profiles gewinnt.

Wichtig:

- Marker bleiben pro Variant
- Marker sind nicht global für die ganze Group
- Display ist keine technische Identität
- RoleId wird namespaced normalisiert
- Nicht eingetragen = nicht aktiv
- Eingetragen aber kaputt = Fehler
- Profile-Inhalte werden nicht in state.json gespeichert


################################################################################
28. P3.7 HYTALEROLE PRÜFEN
################################################################################

Für jede LoadedNpcDefinition:

- HytaleRole muss gesetzt sein
- Server/NPC/Roles/<HytaleRole>.json muss existieren
- Role-Datei muss gültiges JSON sein
- Reference darf vorhanden sein
- lokale Reference prüfen, falls sie im Mod liegt
- Base-/Hytale-Reference nicht blind als Fehler behandeln

Wichtig:

- Nur Existenz und minimale JSON-Gültigkeit prüfen
- Role-Datei noch nicht vollständig interpretieren
- HytaleRole bleibt Engine-Anbindung
- RoleId bleibt Keystone-Bauplan-ID
- mehrere RoleIds dürfen dieselbe HytaleRole nutzen
- kein Role-Prefix-Fallback
- kein dynamisches setRoleName("KeystoneNPC_...")


################################################################################
29. P3.8 PROFILE PRÜFEN
################################################################################

Required-Core:

- Routine
- Actions
- Movement
- Navigation
- Persistence

Optional-Core:

- Combat
- Events
- Dialogue
- Trading
- Reputation
- SeasonalOutfits
- Spawn
- BodyPoolGroup
- NamePoolGroup
- OutfitPoolGroup
- StructurePool
- CompositionPool
- AI
- Mana
- Magic

Nicht mehr aktiv:

- FolkPool
- Appearance
- AppearancePool

Regeln:

Required-Core fehlt nach Merge → Fehler
Optional fehlt → okay
Eingetragen aber Datei fehlt → Fehler
Eingetragen aber JSON kaputt → Fehler
Custom eingetragen und gültig → okay
Custom eingetragen aber kaputt → Fehler

Minimale Validierung:

- Datei existiert
- JSON ist syntaktisch gültig
- Id existiert
- Version existiert
- Type existiert oder ist laut Rule optional

Wichtig:

- keine Action ausführen
- keine Routine starten
- keine Profile in state.json schreiben
- keine Gameplay-Ausführung
- nur prüfen und registrieren


################################################################################
30. P3.9 BODYPOOLGROUP / NAMEPOOLGROUP / OUTFITPOOLGROUP PRÜFEN
################################################################################

BodyPoolGroup prüfen:

- Datei existiert
- Id existiert
- Version existiert
- Type == BodyPoolGroup
- Entries existieren
- Theme-Zuordnung existiert
- referenzierte BodyPools existieren

NamePoolGroup prüfen:

- Datei existiert
- Id existiert
- Version existiert
- Type == NamePoolGroup
- Entries existieren
- Theme-Zuordnung existiert
- referenzierte NamePools existieren

OutfitPoolGroup prüfen:

- Datei existiert
- Id existiert
- Version existiert
- Type == OutfitPoolGroup
- Entries existieren
- Theme-Zuordnung existiert
- referenzierte OutfitPools existieren

P3 prüft nur.
P3 wählt nichts aus.


################################################################################
31. P3.10 BODYPOOL / NAMEPOOL / OUTFITPOOL PRÜFEN
################################################################################

BodyPool prüfen:

- Datei existiert
- Id existiert
- Version existiert
- Type == BodyPool
- BodyTheme existiert
- Entries existieren
- jeder Entry hat stabile Id
- Weight gültig, falls vorhanden
- Körper-/Haut-/Haar-/Gesicht-/Augen-Refs gültig, falls vorhanden
- RequiredTags gültig, falls vorhanden
- SupportedTags gültig, falls vorhanden

NamePool prüfen:

- Datei existiert
- Id existiert
- Version existiert
- Type == NamePool
- NameTheme existiert
- Entries existieren
- jeder Entry hat stabile Id oder Namen-Key
- Weight gültig, falls vorhanden
- RequiredTags gültig, falls vorhanden
- SupportedTags gültig, falls vorhanden

OutfitPool prüfen:

- Datei existiert
- Id existiert
- Version existiert
- Type == OutfitPool
- OutfitTheme existiert
- Entries existieren
- jeder Entry hat stabile Id
- Weight gültig, falls vorhanden
- Kleidung-/Outfit-Refs gültig, falls vorhanden
- RequiredTags gültig, falls vorhanden
- SupportedTags gültig, falls vorhanden

Wichtig:

- BodyPool = Körper/Haut/Haare/Gesicht/Augen
- OutfitPool = Kleidung
- NamePool = Namen
- kein AppearancePool
- keine selectedAppearanceId


################################################################################
32. P3.11 STRUCTUREPOOL / STRUCTURES / PREFABS PRÜFEN
################################################################################

StructurePool prüfen:

- Datei existiert
- Id existiert
- Version existiert
- Type == StructurePool
- StructureTheme existiert
- Entries existieren
- Entries verweisen auf existierende Structures/Prefabs
- Weight gültig, falls vorhanden
- RequiredTags gültig, falls vorhanden
- SupportedTags gültig, falls vorhanden

Structure/Prefab prüfen:

- Id existiert
- Version existiert
- Type existiert
- PrefabId oder StructureId existiert
- PrefabPath existiert, falls verwendet
- StructureTheme existiert oder ist über Pool eindeutig gebunden
- ProvidesContracts existieren
- Tags gültig
- Slots gültig
- Markers gültig
- CompositionPools existieren, falls eingetragen

Wichtig:

- P3 platziert kein Prefab
- P3 erzeugt keine StructureInstance
- P3 erzeugt keine MarkerRecords
- P3 reserviert keine Slots
- P3 wählt keine Composition
- P3 fragt keine Spielerposition ab


################################################################################
33. P3.12 CONTRACTS PRÜFEN
################################################################################

Contract prüfen:

- Datei existiert
- Id existiert
- Version existiert
- Type == Contract
- ContractId existiert
- RequiredSlots optional
- RequiredMarkers optional
- Purpose optional

Cross-Validation:

- ProvidesContracts in Prefabs müssen existierende Contracts sein
- RequiredContracts in Composition/Territory müssen existieren
- unbekannte ContractIds = Fehler

Bedeutung:

Contracts entscheiden technische Eignung.

Beispiel:

blacksmith braucht:
- ResidenceContract
- BlacksmithWorkstationContract

Prefab liefert:
- ResidenceContract
- BlacksmithWorkstationContract

Dann passt es technisch.


################################################################################
34. P3.13 COMPOSITIONS PRÜFEN
################################################################################

Composition-Dateien heißen:

simple_house_<job>_compositions.json

Beispiele:

simple_house_trader_compositions.json
simple_house_blacksmith_compositions.json
simple_house_guard_compositions.json
simple_house_lumberjack_compositions.json

Prüfen:

- Datei existiert
- Id existiert
- Version existiert
- Type existiert
- CompositionId existiert
- Slots existieren
- RoleIds sind gültig oder bewusst als später markiert
- RequiredSex gültig, falls vorhanden
- RequiredTags gültig, falls vorhanden
- SupportedTags gültig, falls vorhanden
- HomeSlot existiert, falls verwendet
- WorkSlot existiert, falls verwendet
- benötigte Marker sind in Structure/Prefab möglich

Wichtig:

- keine simple_worker_house_*
- keine sand_lumberjack RoleId
- Composition wählt später RoleIds zusammen
- P3 wählt keine konkrete Composition aus


################################################################################
35. P3.14 TERRITORY-DEFINITIONEN PRÜFEN
################################################################################

TerritoryProfile prüfen:

- Id existiert
- Version existiert
- Type == TerritoryProfile
- TerritoryId existiert
- Binding existiert
- Radius positiv
- Spawning existiert, falls aktiv
- RolePool existiert, falls aktiv
- RequiredContracts existieren, falls aktiv

Wichtig:

- P3 erzeugt keine TerritoryInstance
- P3 erzeugt keine SpawnAnchorRecord
- P3 spawnt keine Hostiles
- P3 weist keine PatrolSlots zu


################################################################################
36. P3.15 CROSS-VALIDATION ROUTINE / ACTIONS / MARKER
################################################################################

Actions:

- Routine darf nur ActionIds verwenden, die in Actions.json existieren
- fehlende ActionId = Fehler
- kaputte Actions-Datei = Fehler

Marker:

- RequiredMarkers sind Pflichtmarker
- RoutineMarkers sind Marker für Tagesroutine
- EventMarkers sind Marker für Events
- SafetyMarkers sind Marker für Sicherheit/Flucht
- OptionalMarkers dürfen fehlen

Wichtig:

Event-/Safety-Marker sind nicht automatisch RequiredMarkers.

Routine darf nutzen:

- RoutineMarkers
- RequiredMarkers
- OptionalMarkers, wenn Fallback existiert

Events dürfen nutzen:

- EventMarkers
- SafetyMarkers
- OptionalMarkers, wenn Event-Fallback existiert

Harter Fehler:

- Routine verweist auf unbekannte ActionId
- Routine verweist auf unbekannten Marker
- Action-Datei fehlt
- Routine-Datei fehlt
- MarkerName ist nicht in erlaubten Markerlisten

Kein harter Fehler:

- OptionalMarker fehlt
- Routine benutzt nicht jeden Marker
- EventMarker fehlt später bei konkreter Instanz, wenn nur Event blockiert wird

Wichtig:

- kein Alias-System
- kein cook -> FOOD-Fallback
- Marker-Namen müssen exakt passen
- kein automatisches Reparieren
- kein state.json-Schreiben


################################################################################
37. P3.16 GLOBAL DEBUG LADEN
################################################################################

debug.json:

Server/NPC/Keystone/config/debug.json

Regel:

Debug-Log nur wenn:

global.Enabled == true
UND role.Debug.Enabled == true
UND passender Bereich == true

Wichtig:

- Global Debug ist Master-Schalter
- Role Debug ist Feinsteuerung
- Role Debug darf Global nicht überschreiben
- Debug kommt nicht in state.json
- keine Tick-Spam-Logs


################################################################################
38. P3.17 BOOTSTRAP-ABLAUF
################################################################################

Bootstrap-Reihenfolge:

1. loadWorldState()
2. loadDefinitions()
3. validateLoadedStateAgainstDefinitions()
   - bleibt in P3 nur TODO/no-op
4. registerCommands()
5. registerStartupEvents()

loadDefinitions() lädt und prüft:

- index.json
- group.json
- SharedProfiles + Variant.Profiles
- namespaces
- HytaleRoles
- Required/Optional/Custom-Profile
- Biome-Definitionen
- BiomeThemeBindings
- BodyPoolGroups
- NamePoolGroups
- OutfitPoolGroups
- StructurePools
- BodyPools
- NamePools
- OutfitPools
- CompositionPools
- Contracts
- Prefabs/Structures
- Territories
- Global Debug

Fehlerregel:

- Definition-Fehler stoppt Bootstrap
- fehlender Required-Core stoppt Bootstrap
- fehlende aktiv referenzierte Datei stoppt Bootstrap
- kaputte aktiv referenzierte JSON stoppt Bootstrap
- AppearancePool als aktives System stoppt Bootstrap
- FolkPool als Hauptsystem stoppt Bootstrap
- simple_worker_house_* stoppt Bootstrap
- sand_lumberjack-artige RoleId wird als Designfehler blockiert oder mindestens hart gemeldet
- Definition-Fehler löst kein Save aus
- Definition-Fehler überschreibt keine state.json
- Commands werden nicht registriert, wenn Definitionen kaputt sind
- StartupEvents werden nicht registriert, wenn Definitionen kaputt sind


################################################################################
39. P3.18 DIAGNOSE-AUSGABE
################################################################################

Mögliche Diagnose:

- Anzahl geladener NPC-Definitionen
- geladene RoleIds
- geladene namespacedRoleIds
- geladene HytaleRoles
- geladene BiomeThemeBindings
- geladene BodyThemes
- geladene NameThemes
- geladene OutfitThemes
- geladene StructureThemes
- Anzahl BodyPoolGroups
- Anzahl NamePoolGroups
- Anzahl OutfitPoolGroups
- Anzahl StructurePools
- Anzahl BodyPools
- Anzahl NamePools
- Anzahl OutfitPools
- Anzahl CompositionPools
- Anzahl Contracts
- Anzahl Prefabs
- Anzahl Territories
- Profile ohne Handler

Wichtig:

- Logs debug-gated
- kein Tick-Spam
- keine Runtime-Daten
- keine state.json-Instanzdaten
- keine NPC-Entity-Daten
- keine selectedBodyId/currentOutfitId aus echten NPCs loggen


################################################################################
40. P3.19 validateLoadedStateAgainstDefinitions() ALS TODO
################################################################################

In P3 noch keine echte State-Reconcile-Logik.

Diese Methode bleibt sichtbar als TODO.

Späterer Zweck:

state.json:
konkrete NPC-Instanzen

resources:
aktuelle Baupläne

validateLoadedStateAgainstDefinitions():
prüft gespeicherte NPCs gegen aktuelle Definitionen

Aber:

- löscht nichts automatisch
- schreibt nichts automatisch
- würfelt nichts neu
- repariert nichts automatisch
- setzt später nur Diagnose / Blocker

Spätere Checks:

- NpcRecord.roleId gegen aktuelle Definition prüfen
- selectedBodyId gegen BodyPool prüfen
- selectedNameId gegen NamePool prüfen, falls gespeichert
- currentOutfitId gegen OutfitPool prüfen
- selectedOutfitId gegen OutfitPool prüfen, falls genutzt
- selectedCompositionId gegen CompositionPool prüfen
- selectedPrefabId gegen Prefab-Definition prüfen
- structureInstanceId gegen StructureRecord prüfen
- territoryId gegen TerritoryRecord prüfen
- spawnAnchorId gegen SpawnAnchorRecord prüfen
- markerAssignments gegen aktuelle MarkerDefinition prüfen

Nicht mehr:

- selectedAppearanceId gegen AppearancePool prüfen

P3 selbst:

- kein NpcRecord-Reconcile
- kein MarkerAssignment-Reconcile
- kein selectedBodyId-Check gegen echte state.json
- kein currentOutfitId-Check gegen echte state.json
- kein selectedCompositionId-Check gegen echte state.json
- kein state.json-Save
- kein Repair
- keine Runtime-Seiteneffekte


################################################################################
41. STATE.JSON ZIELFELDER SPÄTER
################################################################################

NpcRecord später:

- npcId
- roleId oder namespacedRoleId
- entityUuid
- worldKey / worldId
- status
- lastKnownPosition
- selectedBodyId
- selectedNameId optional
- selectedOutfitId optional
- currentOutfitId
- outfitPoolId
- lastOutfitChangeDay
- nextOutfitChangeDay
- selectedCompositionId
- selectedPrefabId
- structureInstanceId optional
- territoryId optional
- spawnAnchorId optional
- homeSlotId optional
- workSlotId optional
- markerAssignments

Nicht speichern:

- selectedAppearanceId
- AppearancePool-Inhalte
- BodyPool-Inhalte
- NamePool-Inhalte
- OutfitPool-Inhalte
- Profile-Inhalte
- Routine-Inhalte
- Actions-Inhalte
- EntityRef
- RuntimeNpc
- aktive Navigation
- laufende Action
- Door Runtime
- Tick Runtime


################################################################################
42. RESTART-REGEL
################################################################################

Restart bedeutet nicht neu würfeln.

Beim Restart:

1. state.json lädt konkrete gespeicherte Auswahl.
2. resources laden aktuelle Baupläne.
3. gespeicherte Auswahl wird später gegen aktuelle Definitionen geprüft.
4. fehlende Definitionen erzeugen Diagnose.
5. nichts wird automatisch gelöscht.
6. nichts wird automatisch ersetzt.
7. nichts wird automatisch neu gewürfelt.

Beispiel:

currentOutfitId existiert nicht mehr im OutfitPool.

Dann:

- NPC bleibt gespeichert
- Diagnose SELECTED_OUTFIT_MISSING
- kein neues Outfit automatisch als Wahrheit speichern
- Fallback höchstens runtime
- dauerhafte Änderung nur durch expliziten Repair/Migration-Step


################################################################################
43. DEFINITION GEÄNDERT ≠ NPC LÖSCHEN
################################################################################

Wenn resources geändert werden:

- NPC nicht löschen
- NPC nicht automatisch umschreiben
- NPC nicht automatisch neu würfeln
- NPC nicht automatisch in andere RoleId umwandeln
- state.json nicht automatisch überschreiben

Definition geändert bedeutet:

- aktuelle Definition neu laden
- gespeicherte NPCs später neu bewerten
- ungültige Teile blockieren
- Diagnose setzen
- alte konkrete Auswahl behalten


################################################################################
44. BIOME-/THEME-ÄNDERUNG SPÄTER
################################################################################

Wenn ein BiomeTheme geändert wird:

Beispiel:

sand_desert nutzt jetzt neues OutfitTheme.

Dann:

- neue NPCs nutzen später neues OutfitTheme
- alte NPCs behalten currentOutfitId
- alte NPCs werden nicht automatisch neu eingekleidet
- wenn Outfit nicht mehr gültig ist: Diagnose
- kein automatisches Speichern eines Ersatzes

Wenn StructureTheme geändert wird:

- neue Structures nutzen später neues StructureTheme
- bestehende StructureInstances bleiben erhalten
- selectedPrefabId bleibt Wahrheit
- kein automatischer Gebäudetausch
- Diagnose, wenn Prefab nicht mehr zur Definition passt


################################################################################
45. FINAL P3 PASS-KRITERIEN
################################################################################

P3 ist gut, wenn:

- index.json wird geladen
- aktive group.json-Dateien werden geladen
- jede Variant erzeugt genau eine LoadedNpcDefinition
- jede Variant hat genau eine RoleId
- SharedProfiles + Variant.Profiles werden korrekt gemerged
- Required-Core Profile sind vorhanden:
  - Routine
  - Actions
  - Movement
  - Navigation
  - Persistence

Optional-Core Profile dürfen fehlen:
- Combat
- Events
- Dialogue
- Trading
- Reputation
- SeasonalOutfits
- Spawn
- BodyPoolGroup
- NamePoolGroup
- OutfitPoolGroup
- StructurePool
- CompositionPool
- AI
- Mana
- Magic

Neue Biom-/Theme-Kriterien:

- Biome-Dateien werden geladen
- BiomeThemeBinding wird geladen
- BodyTheme wird geprüft
- NameTheme wird geprüft
- OutfitTheme wird geprüft
- StructureTheme wird geprüft
- BodyPoolGroups werden geprüft
- NamePoolGroups werden geprüft
- OutfitPoolGroups werden geprüft
- StructurePools werden geprüft
- BodyPools werden geprüft
- NamePools werden geprüft
- OutfitPools werden geprüft
- StructurePools filtern nach StructureTheme
- BodyPool enthält Körper/Haut/Haare/Gesicht/Augen
- OutfitPool enthält Kleidung
- NamePool enthält Namen
- RoleId bleibt unabhängig vom Biom
- keine Sand-Lumberjacks
- keine biome-spezifischen Job-RoleIds
- keine simple_worker_house_* Namen
- Composition-Dateien nutzen simple_house_<job>_compositions.json
- Biome-Grenzregel ist im Design berücksichtigt:
  PlacementCandidatePosition, nicht PlayerPosition

Muss raus sein:

- FolkPool als Hauptsystem
- AppearancePool als aktives System
- Appearance als aktives System
- Appearance oder AppearancePool erlaubt
- selectedAppearanceId gegen AppearancePool prüfen
- simple_worker_house_* Namen
- sand_lumberjack als RoleId

Sicherheit:

- kein Spawn
- kein Relink
- kein Save
- keine state.json-Änderung
- keine RuntimeNpc-Erzeugung
- keine EntityRef
- kein automatischer Repair
- keine Pool-Auswahl
- keine neue selectedBodyId
- keine neue selectedNameId
- keine neue selectedOutfitId
- keine neue currentOutfitId
- keine neue selectedCompositionId
- keine neue selectedAppearanceId
- kein Neu-Auswürfeln

Compile:

mvn -q -DskipTests test-compile

P3 Entscheidung:

PASS, wenn Definitionen vollständig geladen, validiert und atomisch registriert werden,
ohne Runtime-/State-/Spawn-Seiteneffekte.