AUSWAHLREIHENFOLGE: BIOM → NPC

1. Spieler / Chunk ist in einem Biom
   Beispiel:
   sand_desert

2. Biome-Datei sagt, welches Theme gilt
   Beispiel:
   StructureTheme = sand
   BodyTheme     = sand
   OutfitTheme   = sand
   NameTheme     = sand

3. StructurePool sucht passende Gebäude
   Filter:
   StructureTheme = sand

   Ergebnis:
   sand_worker_house_shop
   sand_worker_house_blacksmith
   sand_worker_house_storage

4. Eine Structure wird gewählt
   Beispiel:
   sand_worker_house_shop

5. Structure sagt, welche Composition möglich ist
   Beispiel:
   simple_worker_house_shop_compositions.json

6. Composition sagt, welche NPCs gebraucht werden
   Beispiel:
   trader:
     RoleId = keystone:trader
     HomeSlot = resident_1
     WorkSlot = shop_work_1
     RequiredSex = male
     RequiredTags = worker, trader

   spouse:
     RoleId = keystone:worker_spouse
     HomeSlot = resident_2
     RequiredSex = female
     RequiredTags = citizen

7. NPC-Definition wird über RoleId geladen
   Beispiel:
   keystone:trader

   Daraus kommen:
   - HytaleRole
   - Routine
   - Actions
   - Movement
   - Navigation
   - Persistence
   - SelectionTags

8. BodyPoolGroup wählt passende Body-Datei
   Filter:
   BodyTheme = sand
   Sex = male

   Ergebnis:
   pools/body/human/sand_male_body_pool.json

9. Aus der Body-Datei wird ein konkreter Body gewählt
   Beispiel:
   sand_male_adult_02

   Enthält:
   - Körperform
   - Haut
   - Haare
   - Haarfarbe
   - Augenfarbe
   - Gesicht

10. NamePoolGroup wählt passende Namen-Datei
   Filter:
   NameTheme = sand
   Sex = male

   Ergebnis:
   pools/names/human/sand_male_name_pool.json

11. Ein Name wird gewählt
   Beispiel:
   Kharim

12. OutfitPoolGroup wählt passende Outfit-Datei
   Filter:
   OutfitTheme = sand
   Sex = male

   Ergebnis:
   pools/outfits/human/sand_male_outfit_pool.json

13. Outfit-Datei filtert nach RequiredTags / SelectionTags
   Beispiel:
   trader braucht:
   - worker
   - trader

   Ergebnis:
   sand_male_trader_outfit_01

14. Marker aus der Structure werden zugewiesen
   Beispiel:
   bed        -> resident_1.bed
   table_seat -> resident_1.table_seat
   door       -> door
   counter    -> shop_work_1.counter
   storage    -> shop_work_1.storage

15. state.json speichert später die konkrete Auswahl
   Beispiel:
   roleId
   selectedBodyId
   selectedNameId
   currentOutfitId
   selectedPrefabId
   selectedCompositionId
   structureInstanceId
   markerAssignments

MERKSATZ:
Biome wählt Theme.
Theme wählt Gebäude, Body, Name und Outfit.
Composition wählt Rollen und male/female.
RoleId wählt Verhalten.
state.json speichert die konkrete NPC-Instanz.


######
######
######
######
###### 



P3 ACTION PLAN — loadDefinitions() / DefinitionLoader / DefinitionRegistry
UPDATED: Hytale-API-bewusst, modular, AssetStore-ready, neue JSON-Keys/Files erweiterbar

ZIEL VON P3:
NPC-Definitionen aus resources laden, prüfen und im RAM registrieren.

P3 lädt Baupläne.
P3 erzeugt keine echten NPCs.

WICHTIGE GRUNDREGEL:
Resource-JSONs = Baupläne / Definitionen / Profile / Pools / Contracts / Prefabs / Territories
state.json = konkrete Welt-/NPC-Instanzen

Pool laden ≠ Pool auswürfeln.

P3 lädt:
- index.json
- group.json
- RoleIds
- HytaleRoles
- ProfileRefs
- Pools
- Contracts
- Prefab-Definitionen
- Territory-Definitionen
- CustomProfileRefs
- CustomJsonDefinitionRefs

P3 registriert:
- geprüfte LoadedNpcDefinitions
- gelesene ProfileRefs
- gelesene PoolRefs
- gelesene StructureDefinitionRefs
- gelesene TerritoryDefinitionRefs

P3 erzeugt NICHT:
- keine NPC-Entity
- keine EntityRef
- keine RuntimeNpc
- keine StructureInstance
- keine TerritoryInstance
- keine SpawnAnchorRecord
- keine MarkerRecords
- keine MarkerAssignments
- keine state.json-Änderung
- keine Pool-Auswahl
- kein Outfit-Auswürfeln
- kein Spawn
- kein Relink
- kein Repair


################################################################################
################################################################################
0. HYTALE-API-GRUNDENTSCHEIDUNG
################################################################################
################################################################################

P3 soll Hytale-APIs dort nutzen, wo sie sinnvoll sind.

Aber P3 darf NICHT Runtime-/Spawn-APIs benutzen.

────────────────────────────────────────
0.1 Was direkt sinnvoll ist
────────────────────────────────────────

Für P3 sinnvoll:

NPCPlugin
= Hytale-NPC-System-Zugang
= nützlich, um echte HytaleRoles zu prüfen

BuilderManager
= lädt/kennt Server/NPC/Roles
= nützlich für HytaleRole-Validierung

AssetModule
= kennt geladene AssetPacks
= später nützlich für Multi-Pack-/Multi-Mod-Discovery

AssetPack
= beschreibt ein geladenes AssetPack
= später nützlich für Namespace/Root/Manifest

LoadAssetEvent
= später nützlich für Asset-Ladephase und Fehlerweitergabe

AssetStore
= später sehr nützlich für echte Hytale-Asset-Pipeline


────────────────────────────────────────
0.2 Was in P3 NICHT direkt benutzt wird
────────────────────────────────────────

NPCEntity
= echte Runtime-Entity
= nicht P3

RoleBuilderSystem
= baut echte Role an gespawnter Entity
= nicht P3

Verbot in P3:
- kein NPCEntity erzeugen
- keine NPCEntity mutieren
- kein setRoleName(...)
- kein spawnNPC(...)
- kein spawnEntity(...)
- kein RoleBuilderSystem direkt triggern


────────────────────────────────────────
0.3 Neue Architektur-Regel
────────────────────────────────────────

P3 bekommt eine eigene Adapter-Schicht:

Keystone Definition-System
→ nutzt Interfaces

Hytale API
→ wird nur über Adapter angesprochen

Warum:
Damit wir später von eigenem ResourceReader auf AssetStore wechseln können,
ohne die ganze P3-Logik neu zu schreiben.


################################################################################
################################################################################
1. MODULARER P3-AUFBAU
################################################################################
################################################################################

────────────────────────────────────────
1.1 Schichten
────────────────────────────────────────

P3 soll in Schichten gebaut werden:

Layer A: Hytale-API-Adapter
- HytaleRoleGateway
- HytaleAssetPackScanner
- HytaleLoadFailureReporter optional später

Layer B: Resource-/Asset-Source
- KeystoneDefinitionSource
- ResourceDefinitionSource
- AssetPackDefinitionSource später
- AssetStoreDefinitionSource später

Layer C: JSON-Dokument-Layer
- KeystoneJsonDocument
- KeystoneAssetId
- KeystoneResourcePath
- JsonType
- JsonDocumentReader

Layer D: Definition-Parser
- NpcIndexParser
- NpcGroupParser
- NpcProfileRefParser
- PoolParser
- StructureParser
- ContractParser
- TerritoryParser
- GenericJsonDefinitionParser

Layer E: Validation
- ProfileTypeRegistry
- JsonDefinitionTypeRegistry
- DefinitionValidator
- CrossDefinitionValidator

Layer F: Registry
- NpcDefinitionRegistry
- JsonDefinitionRegistry optional
- PoolDefinitionRegistry optional
- StructureDefinitionRegistry optional

Layer G: Bootstrap Integration
- loadDefinitions()
- Fehler stoppen Bootstrap
- keine Commands/Events bei kaputten Definitionen


────────────────────────────────────────
1.2 Wichtiges Ziel
────────────────────────────────────────

P3 darf nicht so gebaut werden:

DefinitionLoader liest direkt Dateien per Path
und verteilt danach überall harte Sonderlogik.

Besser:

DefinitionLoader fragt:
KeystoneDefinitionSource

KeystoneDefinitionSource kann heute sein:
ResourceDefinitionSource

Später:
AssetPackDefinitionSource
AssetStoreDefinitionSource

DefinitionLoader bleibt gleich.


################################################################################
################################################################################
2. NEUE INTERFACES FÜR ASSETSTORE-READY DESIGN
################################################################################
################################################################################

────────────────────────────────────────
2.1 KeystoneDefinitionSource
────────────────────────────────────────

Zweck:
Quelle für Keystone-JSONs.

MVP-Implementierung:
ResourceDefinitionSource

Spätere Implementierungen:
AssetPackDefinitionSource
AssetStoreDefinitionSource

Methoden-Idee:

- readJson(KeystoneAssetId assetId)
- exists(KeystoneAssetId assetId)
- listIndexes()
- resolveRelative(base, path)
- sourceName()
- namespace()

Wichtig:
Der DefinitionLoader kennt nur dieses Interface,
nicht direkt AssetModule, AssetPack oder Java-File-System.


────────────────────────────────────────
2.2 KeystoneAssetId
────────────────────────────────────────

Zweck:
Einheitliche ID für JSON-Assets.

Felder:
- namespace
- logicalPath
- sourceKind
- sourceName optional

Beispiele:
- keystone:npc/lumberjack/lumberjack_group.json
- keystone:profiles/movement/human_walk.json
- othermod:npc/fisher/fisher_group.json

Regel:
Wenn Namespace fehlt:
→ keystone

Wenn Namespace vorhanden:
→ behalten

Warum:
Damit spätere Module eigene Dateien liefern können.


────────────────────────────────────────
2.3 KeystoneJsonDocument
────────────────────────────────────────

Zweck:
Gelesenes JSON mit Metadaten.

Felder:
- assetId
- rawJson
- rootObject
- id
- version
- type
- sourcePath
- sourcePack optional
- namespace
- isPlaceholder optional

Wichtig:
Parser und Validator bekommen JsonDocument,
nicht nur nackten String.

Vorteil:
Fehlermeldungen können exakt sagen:
welche Datei
welcher Key
welcher Pfad
welcher Source-Pack


────────────────────────────────────────
2.4 HytaleRoleGateway
────────────────────────────────────────

Zweck:
HytaleRole prüfen, ohne den restlichen Loader an NPCPlugin/BuilderManager zu koppeln.

MVP-Implementierung:
NpcPluginHytaleRoleGateway

Nutzt intern:
- NPCPlugin
- BuilderManager

Methoden-Idee:
- roleExists(String hytaleRole)
- isSpawnableRole(String hytaleRole)
- getTemplateReference(String hytaleRole)
- describeRole(String hytaleRole)

Wichtig:
Diese Klasse darf NICHT spawnen.

Erlaubt:
- prüfen
- lesen
- validieren

Verboten:
- spawnNPC
- spawnEntity
- setRoleName


────────────────────────────────────────
2.5 HytaleAssetPackScanner
────────────────────────────────────────

Zweck:
Später alle AssetPacks nach Keystone-Definitionen durchsuchen.

MVP:
Kann NOOP sein oder nur aktuelle Resources verwenden.

Später:
Nutzt AssetModule.get().getAssetPacks()

Aufgabe später:
- finde alle Packs mit Server/NPC/Keystone/npc/index.json
- leite Namespace aus Pack/Manifest ab
- liefere KeystoneDefinitionSource pro Pack

Wichtig:
P3 darf heute schon so gebaut werden, dass mehrere Sources möglich sind.


────────────────────────────────────────
2.6 AssetStore-ready Vorbereitung
────────────────────────────────────────

Noch NICHT direkt voll AssetStore bauen.

Aber Architektur vorbereiten:

Interface:
KeystoneDefinitionSource

Später:
AssetStoreDefinitionSource implements KeystoneDefinitionSource

Dann kann AssetStore:
- JSONs laden
- reloaden
- validieren
- Events liefern

P3-Logik bleibt:
- parse
- validate
- registry replaceAll

Nicht hart an AssetStore koppeln.


################################################################################
################################################################################
3. DATEI-/JSON-MODULARITÄT
################################################################################
################################################################################

────────────────────────────────────────
3.1 Neue JSON-Keys
────────────────────────────────────────

Profiles ist eine Map.

Also:

Profiles:
- Routine
- Actions
- Movement
- Navigation
- Persistence
- Combat
- Events
- Dialogue
- Trading
- Reputation
- SeasonalOutfits
- CustomSomething

Regel:
Neuer Key darf den Loader nicht crashen.

Wenn neuer Key eingetragen ist:
- Datei muss existieren
- JSON muss basic valid sein
- Id muss existieren
- Version muss existieren
- Type soll existieren, wenn möglich
- als CustomProfileRef speichern, wenn Key unbekannt ist

Wenn kein Handler existiert:
- nicht ausführen
- nur registrieren


────────────────────────────────────────
3.2 Neue JSON-Dateitypen
────────────────────────────────────────

Nicht nur Profile müssen modular sein.
Auch neue Dateitypen sollen später möglich sein.

Beispiele:
- QuestProfile
- RaidProfile
- EconomyProfile
- FactionProfile
- MagicProfile
- ManaProfile
- AIProfile
- RoadProfile
- CaravanProfile

Dafür braucht P3:

JsonDefinitionTypeRegistry

Diese Registry kennt:
- NpcIndex
- NpcGroup
- RoutineProfile
- ActionProfile
- MovementProfile
- NavigationProfile
- PersistenceProfile
- Pool-Typen
- StructureContract
- StructurePrefab
- TerritoryProfile
- CustomUnknown

Für unbekannte Type:
- basic validieren
- als GenericJsonDefinition speichern
- nicht ausführen


────────────────────────────────────────
3.3 Unterschied zwischen ProfileKey und Json Type
────────────────────────────────────────

ProfileKey:
steht in Variant.Profiles

Beispiel:
"Dialogue": "profiles/dialogue/worker_dialogue.json"

Json Type:
steht in der Datei

Beispiel:
"Type": "DialogueProfile"

Beide müssen getrennt betrachtet werden.

Warum:
Ein Key kann später auf verschiedene Type-Versionen zeigen.
Ein Type kann von mehreren Keys genutzt werden.

P3-Regel:
- ProfileKey entscheidet, wie die RoleId das Profil nutzt
- Type entscheidet, wie die Datei validiert wird


────────────────────────────────────────
3.4 HandlerKey
────────────────────────────────────────

ProfileTypeRule enthält:
- ProfileKey
- Required/Optional
- ExpectedType
- ValidationMode
- HandlerKey optional

Beispiel:
Dialogue:
- ExpectedType = DialogueProfile
- ValidationMode = Basic
- HandlerKey = DialogueHandler

Wenn Handler fehlt:
- Profil wird geladen
- Profil wird nicht ausgeführt

Das macht neue Systeme später leicht.


################################################################################
################################################################################
4. P3 STEPS — DETAILPLAN
################################################################################
################################################################################

────────────────────────────────────────
P3.0 — API-Research / Boundary festlegen
────────────────────────────────────────

AGENT-AUFGABE:
Dokumentiere im Plan/Code-Kommentar klar:

Welche Hytale-APIs P3 nutzen darf:
- NPCPlugin
- BuilderManager
- AssetModule optional/future
- AssetPack optional/future
- LoadAssetEvent optional/future
- AssetStore optional/future

Welche Hytale-APIs P3 nicht nutzen darf:
- NPCEntity
- RoleBuilderSystem direkt
- spawnNPC
- spawnEntity
- setRoleName

ZIEL:
P3 bleibt Definition-Load.
P3 wird Hytale-API-bewusst.
P3 bleibt spawn-frei.

REVIEW:
- Keine Runtime-/Entity-API im P3-Loader.
- HytaleRole-Prüfung nur über Gateway.
- Kein Spawn.
- Kein EntityRef.
- Compile:
  mvn -q -DskipTests test-compile


────────────────────────────────────────
P3.1 — Loader-Interfaces anlegen
────────────────────────────────────────

NEUE DATEIEN:
- KeystoneDefinitionSource.java
- KeystoneAssetId.java
- KeystoneJsonDocument.java
- KeystoneResourcePath.java
- DefinitionLoadException.java
- DefinitionLoadResult.java optional

PACKAGE:
src/main/java/keystone/npc/definition/source/

AUFGABE:
Abstraktionen bauen, damit der Loader nicht direkt vom File-System abhängt.

WICHTIG:
Heute:
ResourceDefinitionSource nutzt ClassLoader/resources.

Später:
AssetPackDefinitionSource nutzt AssetModule/AssetPack.
AssetStoreDefinitionSource nutzt AssetStore.

REVIEW:
- Loader hängt gegen Interface.
- Kein direkter File-System-Zwang.
- Namespace/AssetId vorbereitet.
- Fehler enthalten assetId/sourcePath.
- Compile-Gate:
  mvn -q -DskipTests test-compile


────────────────────────────────────────
P3.2 — HytaleRoleGateway anlegen
────────────────────────────────────────

NEUE DATEIEN:
- HytaleRoleGateway.java
- NpcPluginHytaleRoleGateway.java
- HytaleRoleInfo.java optional

PACKAGE:
src/main/java/keystone/npc/definition/hytale/

AUFGABE:
HytaleRole-Validierung kapseln.

Gateway darf:
- prüfen ob Role existiert
- prüfen ob Role spawnbar ist, falls API sicher
- Template/Reference lesen, falls möglich
- klare Fehler erzeugen

Gateway darf NICHT:
- spawnen
- NPCEntity erzeugen
- RoleBuilderSystem triggern
- setRoleName aufrufen

WICHTIG:
NPCPlugin/BuilderManager werden nur hier benutzt.

REVIEW:
- Keine direkte NPCPlugin-Nutzung im GroupParser.
- Keine direkte BuilderManager-Nutzung im NpcDefinitionRegistry.
- Alles läuft über HytaleRoleGateway.
- Compile-Gate:
  mvn -q -DskipTests test-compile


────────────────────────────────────────
P3.3 — AssetPackScanner vorbereiten
────────────────────────────────────────

NEUE DATEIEN:
- HytaleAssetPackScanner.java
- AssetPackDefinitionSource.java optional als Skeleton
- DefinitionSourceDiscovery.java

PACKAGE:
src/main/java/keystone/npc/definition/source/

MVP:
- kann erstmal nur Keystone-Default-Source liefern
- keine echte Multi-Pack-Discovery nötig

Aber Architektur:
- Liste von KeystoneDefinitionSource zurückgeben
- später mehrere AssetPacks möglich
- kein stiller Override zwischen Namespaces

SPÄTER:
AssetModule.get().getAssetPacks()
→ pro Pack nach Server/NPC/Keystone/npc/index.json suchen
→ Namespace aus Manifest/Pack ableiten
→ Source registrieren

REVIEW:
- SourceDiscovery gibt Liste zurück, nicht nur eine einzelne Source.
- Mehrere Namespaces sind vorbereitet.
- Keine AssetStore-Hard-Abhängigkeit in MVP.
- Compile-Gate:
  mvn -q -DskipTests test-compile


────────────────────────────────────────
P3.4 — Definition-Datenmodelle anlegen
────────────────────────────────────────

NEUE DATEIEN:
- LoadedNpcDefinition.java
- NpcProfileRefs.java
- NpcProfileRef.java
- ProfileTypeRule.java
- ProfileTypeRegistry.java
- JsonDefinitionTypeRule.java
- JsonDefinitionTypeRegistry.java
- GenericJsonDefinition.java
- NpcEngineDefinition.java
- NpcDisplayDefinition.java
- NpcMarkerDefinition.java
- NpcDebugDefinition.java

PACKAGE:
src/main/java/keystone/npc/definition/model/

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
- sourceAssetId

NpcProfileRefs:
- Map(String, NpcProfileRef) profiles

NpcProfileRef:
- profileKey
- path
- assetId
- namespace optional
- knownProfileType
- required
- validationMode
- expectedType optional
- handlerKey optional später

ProfileTypeRule:
- profileKey
- required/optional
- expectedType
- validationMode
- known/custom
- handlerKey optional

JsonDefinitionTypeRule:
- jsonType
- validationMode
- known/custom
- expectedTopLevelFields
- handlerKey optional

GenericJsonDefinition:
- assetId
- id
- version
- type
- rawDocument
- handlerKey optional
- known/unknown

WICHTIG:
- Keine Runtime-Daten.
- Keine state.json-Felder.
- Keine EntityRef.
- Keine RuntimeNpc.
- Keine ausgewürfelten IDs.
- ProfileRefs speichern nur Verweise.

REVIEW:
- NpcProfileRefs ist Map-basiert.
- Neue ProfileKeys brauchen keine neue Java-Felder.
- Neue JsonTypes können als GenericJsonDefinition geladen werden.
- Compile-Gate:
  mvn -q -DskipTests test-compile


────────────────────────────────────────
P3.5 — ProfileTypeRegistry bauen
────────────────────────────────────────

AUFGABE:
Regeln für bekannte ProfileKeys zentral definieren.

Required Core Profiles:
- Routine
- Actions
- Movement
- Navigation
- Persistence

Optional Core Profiles:
- Combat
- Events
- Dialogue
- Trading
- Reputation
- SeasonalOutfits
- Spawn
- SpeciesPool
- BodyPool
- OutfitPool
- CompositionPool
- Appearance
- AppearancePool

Custom:
- alle unbekannten Keys

REGELN:
Required fehlt nach Merge → Fehler.
Optional fehlt → okay.
Eingetragen und Datei fehlt → Fehler.
Eingetragen und JSON kaputt → Fehler.
Custom eingetragen und basic valid → okay.
Custom ohne Handler → nicht ausführen.

Appearance-Regel:
- Appearance oder AppearancePool erlaubt
- nicht zwingend beides
- OutfitPool ist getrennt davon

REVIEW:
- Required/Optional/Custom klar abgebildet.
- Kein hardcoded Profilfeld in NpcProfileRefs.
- Appearance/Outfit klar getrennt.
- Compile-Gate:
  mvn -q -DskipTests test-compile


────────────────────────────────────────
P3.6 — JsonDefinitionTypeRegistry bauen
────────────────────────────────────────

AUFGABE:
Regeln für JSON-Dateitypen zentral definieren.

Bekannte Types:
- NpcIndex
- NpcGroup
- SpeciesProfile
- BodyProfile
- MovementProfile
- NavigationProfile
- CombatProfile
- SpawnProfile
- PersistenceProfile
- EventProfile
- DialogueProfile
- ActionProfile
- RoutineProfile
- SpeciesPool
- BodyPool
- AppearancePool
- OutfitPool
- CompositionPool
- StructureContract
- TerritoryContract
- StructureTag
- StructurePrefab
- TerritoryPrefab
- TerritoryProfile
- DebugConfig

Unbekannter Type:
- basic validieren
- GenericJsonDefinition
- nicht ausführen

WICHTIG:
Damit später neue JSON-Dateien nicht das System sprengen.

REVIEW:
- bekannte Types validieren strenger
- unbekannte Types basic valid
- kein Handler = keine Ausführung
- Compile-Gate:
  mvn -q -DskipTests test-compile


────────────────────────────────────────
P3.7 — ResourcePathResolver / AssetIdResolver
────────────────────────────────────────

AUFGABE:
Pfade und AssetIds sauber auflösen.

ROOTS:
- KeystoneRoot = Server/NPC/Keystone/
- NpcRoot = Server/NPC/Keystone/npc/
- ProfilesRoot = Server/NPC/Keystone/profiles/
- PoolsRoot = Server/NPC/Keystone/pools/
- StructuresRoot = Server/NPC/Keystone/structures/
- TerritoriesRoot = Server/NPC/Keystone/territories/
- RolesRoot = Server/NPC/Roles/
- ConfigRoot = Server/NPC/Keystone/config/

REGEL:
Profile/Pools/Contracts/Prefabs/Territories werden als KeystoneAssetId gedacht.

Beispiele:
- keystone:npc/lumberjack/lumberjack_group.json
- keystone:profiles/movement/human_walk.json
- keystone:pools/outfits/lumberjack_outfit_pool.json

SICHERHEIT:
- kein ../ Ausbruch
- keine absoluten Pfade
- keine state.json-Pfade
- nur Resource-/Asset-Pfade

REVIEW:
- Pfade können ResourceRoot nicht verlassen.
- Fehler nennen assetId.
- Compile-Gate:
  mvn -q -DskipTests test-compile


────────────────────────────────────────
P3.8 — DefinitionSourceDiscovery
────────────────────────────────────────

AUFGABE:
Alle DefinitionSources finden.

MVP:
- ResourceDefinitionSource für eigenen Mod
- lädt Server/NPC/Keystone/npc/index.json

Später:
- AssetPackDefinitionSource pro AssetPack
- optional AssetStoreDefinitionSource

WICHTIG:
Der Loader bekommt:
List(KeystoneDefinitionSource)

Nicht:
eine hardcoded Einzelquelle.

NAMESPACE:
- fehlender Namespace = keystone
- andere Sources können eigene Namespaces liefern
- kein stiller Override

REVIEW:
- mehrere Sources möglich
- Reihenfolge klar
- duplicate namespacedRoleId wird später blockiert
- Compile-Gate:
  mvn -q -DskipTests test-compile


────────────────────────────────────────
P3.9 — index.json laden
────────────────────────────────────────

AUFGABE:
Für jede KeystoneDefinitionSource:
- index.json suchen
- index lesen
- JSON-Syntax prüfen
- Groups extrahieren
- Group-Dateien laden

VALIDIERUNG:
- Id vorhanden
- Version vorhanden
- Type == NpcIndex
- Groups nicht leer
- jeder Eintrag String
- jede Group-Datei existiert

WICHTIG:
- keine hardcoded lumberjack_group.json-only Lösung
- keine state.json
- kein Spawn

REVIEW:
- index.json wird wirklich benutzt
- mehrere Sources vorbereitet
- Fehler mit Source/AssetId sichtbar
- Compile-Gate:
  mvn -q -DskipTests test-compile


────────────────────────────────────────
P3.10 — group.json + Variants parsen
────────────────────────────────────────

AUFGABE:
Parse pro Group:
- Id
- Version
- Type
- Namespace
- SharedProfiles
- Variants[]

Parse pro Variant:
- RoleId
- Engine.HytaleRole
- Display
- Profiles als Map(String,String)
- Markers
- Debug

REGEL:
Eine Variant = genau eine RoleId.

MERGE:
EffectiveProfiles = SharedProfiles + Variant.Profiles

Wenn gleicher Key:
Variant.Profiles gewinnt.

VALIDIERUNG:
- Type == NpcGroup
- Variant.RoleId nicht leer
- HytaleRole nicht leer
- Required-Core nach Merge vorhanden

WICHTIG:
- SharedProfiles sind Defaults
- Variant überschreibt einzelne Keys
- Markers bleiben pro Variant
- Display ist keine technische ID
- RoleId wird namespacedRoleId

REVIEW:
- eine Variant hat genau eine RoleId
- Profile-Merge korrekt
- Required-Core nach Merge erfüllt
- Compile-Gate:
  mvn -q -DskipTests test-compile


────────────────────────────────────────
P3.11 — HytaleRole validieren
────────────────────────────────────────

AUFGABE:
Für jede LoadedNpcDefinition:
- HytaleRole über HytaleRoleGateway prüfen
- lokale Role-Datei lesen, falls vorhanden
- BuilderManager/NPCPlugin nutzen, falls sicher verfügbar
- Reference/Template prüfen, soweit sicher

REGEL:
Duplicate HytaleRole ist erlaubt.

Warum:
Mehrere RoleIds dürfen dieselbe Engine-Basis nutzen.

Harter Fehler:
- HytaleRole existiert nicht
- HytaleRole ist nicht nutzbar/spawnbar, falls API das sicher prüfen kann
- Role-Datei kaputt, wenn lokale Datei vorhanden ist

Kein Fehler:
- mehrere RoleIds nutzen dieselbe HytaleRole

VERBOT:
- kein spawnNPC
- kein spawnEntity
- kein NPCEntity
- kein RoleBuilderSystem direkt
- kein dynamisches setRoleName

REVIEW:
- HytaleRoleGateway genutzt
- keine direkte Spawn-API
- Duplicate HytaleRole nicht blockiert
- Compile-Gate:
  mvn -q -DskipTests test-compile


────────────────────────────────────────
P3.12 — Profile-Dateien laden und prüfen
────────────────────────────────────────

AUFGABE:
Für jede LoadedNpcDefinition:
- alle EffectiveProfiles lesen
- jedes Profil als KeystoneJsonDocument laden
- ProfileTypeRegistry anwenden
- JsonDefinitionTypeRegistry anwenden

MINIMAL FÜR ALLE:
- Datei existiert
- JSON gültig
- Id existiert
- Version existiert
- Type existiert oder laut Rule optional

Bekannte Profile:
- strenger prüfen

CustomProfile:
- basic validieren
- als GenericJsonDefinition speichern
- nicht ausführen

WICHTIG:
- keine Action ausführen
- keine Routine starten
- keine Profile-Inhalte in state.json
- keine Handler-Ausführung

REVIEW:
- eingetragene fehlende Profile blockieren
- optionale fehlende Profile blockieren nicht
- CustomProfile funktioniert
- Compile-Gate:
  mvn -q -DskipTests test-compile


────────────────────────────────────────
P3.13 — Pools laden und prüfen
────────────────────────────────────────

AUFGABE:
Falls Profile auf Pools verweisen:
- SpeciesPool
- BodyPool
- AppearancePool
- OutfitPool
- CompositionPool

VALIDIERUNG:
- Id
- Version
- Type
- Entries
- Entry.Id stabil
- Weight gültig
- Entry.Profile existiert, falls vorhanden
- RoleIds in Composition existieren oder sind als future/placeholder markiert

WICHTIG:
- keine Auswahl
- kein selectedSpeciesId
- kein selectedBodyProfileId
- kein selectedAppearanceId
- kein currentOutfitId
- kein selectedCompositionId

REVIEW:
- Pool laden ≠ würfeln
- Entries stabil
- keine state.json-Änderung
- Compile-Gate:
  mvn -q -DskipTests test-compile


────────────────────────────────────────
P3.14 — Contracts / Prefabs / Tags prüfen
────────────────────────────────────────

AUFGABE:
Definitionen aus structures/ prüfen:

Contracts:
- StructureContract
- TerritoryContract

Tags:
- StructureTag

Prefabs:
- StructurePrefab
- TerritoryPrefab

VALIDIERUNG Contract:
- Id
- Version
- Type
- ContractId

VALIDIERUNG Prefab:
- Id
- Version
- Type
- PrefabId
- PrefabPath
- ProvidesContracts
- Slots
- Markers
- CompositionPools optional

Cross-Validation:
- ProvidesContracts existieren
- CompositionPools existieren, wenn eingetragen
- MarkerTypes sind bekannt oder als pending/future markiert
- Tags sind weich/optional

WICHTIG:
- kein Prefab platzieren
- keine StructureInstance
- keine MarkerRecords

REVIEW:
- Contracts = harte Eignung
- Tags = weiche Auswahl
- kein Spawn
- Compile-Gate:
  mvn -q -DskipTests test-compile


────────────────────────────────────────
P3.15 — Territories prüfen
────────────────────────────────────────

AUFGABE:
territories/*.json prüfen.

TerritoryProfile:
- Id
- Version
- Type
- TerritoryId
- Binding
- Radius
- Spawning
- RolePool
- RequiredContracts

Cross-Validation:
- RequiredContracts existieren
- Radius positiv
- RolePool RoleIds existieren oder sind future/placeholder

WICHTIG:
- keine TerritoryInstance
- keine SpawnAnchorRecord
- keine Hostile spawns
- keine PatrolAssignment

REVIEW:
- Territory-bound ist nur Bauplan
- kein Spawn
- Compile-Gate:
  mvn -q -DskipTests test-compile


────────────────────────────────────────
P3.16 — Cross-Validation: Routine / Actions / Marker
────────────────────────────────────────

AUFGABE:
Innerhalb einer LoadedNpcDefinition prüfen.

Actions:
- Routine darf nur ActionIds verwenden, die in Actions.json existieren.

Marker:
- RoutineMarkers = Tagesroutine
- EventMarkers = Events
- SafetyMarkers = Flucht/Sicherheit
- OptionalMarkers = falls vorhanden nutzbar

Regel:
Event-/Safety-Marker sind nicht automatisch RequiredMarkers.

Routine darf:
- RequiredMarkers nutzen
- RoutineMarkers nutzen
- OptionalMarkers nutzen, wenn Fallback existiert

Events dürfen:
- EventMarkers nutzen
- SafetyMarkers nutzen
- OptionalMarkers nutzen, wenn Fallback existiert

Harter Fehler:
- unbekannte ActionId
- unbekannter Marker ohne erlaubten Bereich
- kaputte Routine-Datei
- kaputte Actions-Datei

Kein harter Fehler:
- Routine benutzt nicht jeden Marker
- OptionalMarker fehlt später in Instanz
- EventMarker fehlt später und blockiert nur Event

REVIEW:
- Routine/Action-Verweise geprüft
- Event/Safety getrennt
- kein Repair
- keine state.json
- Compile-Gate:
  mvn -q -DskipTests test-compile


────────────────────────────────────────
P3.17 — DefinitionRegistry bauen / atomisch ersetzen
────────────────────────────────────────

AUFGABE:
Alle geladenen Definitionen in Registry setzen.

Registry:
- byNamespacedRoleId
- byHytaleRole als MultiMap/List
- byGroupId optional
- profileRefs pro Definition
- sourceAssetId pro Definition

Duplicate-Regeln:
- Duplicate namespacedRoleId = Fehler
- Duplicate HytaleRole = erlaubt

replaceAll:
- erst neue Maps bauen
- alles validieren
- dann atomisch ersetzen
- bei Fehler alte Registry behalten

isSpawnable:
- prüft nur Definition
- spawnt nichts

REVIEW:
- atomisch
- keine mutable Map nach außen
- duplicate namespacedRoleId blockiert
- duplicate HytaleRole erlaubt
- Compile-Gate:
  mvn -q -DskipTests test-compile


────────────────────────────────────────
P3.18 — Global Debug laden
────────────────────────────────────────

AUFGABE:
config/debug.json laden.

Regel:
Log nur wenn:
global.Enabled == true
UND role.Debug.Enabled == true
UND Bereich aktiv

WICHTIG:
- Debug ist Config/Resource
- nicht state.json
- keine Tick-Spam-Logs

REVIEW:
- Global Debug Master-Schalter
- Role Debug überschreibt Global nicht
- Compile-Gate:
  mvn -q -DskipTests test-compile


────────────────────────────────────────
P3.19 — Bootstrap-Integration
────────────────────────────────────────

ABLAUF:
- loadWorldState()
- loadDefinitions()
- validateLoadedStateAgainstDefinitions() bleibt TODO/no-op
- registerCommands()
- registerStartupEvents()

loadDefinitions():
- SourceDiscovery
- index laden
- groups laden
- profiles mergen
- HytaleRoleGateway prüfen
- Profile prüfen
- Pools prüfen
- Contracts prüfen
- Prefabs prüfen
- Territories prüfen
- Registry atomisch ersetzen

Fehler:
- Bootstrap stoppen
- keine Commands registrieren
- keine StartupEvents registrieren
- kein Save
- keine state.json-Änderung

Optional später:
LoadAssetEvent nutzen:
- bei Asset-Load Fehler event.failed(true, reason)
- aber erst, wenn Hytale-Asset-Lifecycle sauber verstanden ist

REVIEW:
- keine Exception schlucken
- bei catch erneut werfen
- keine Runtime-Seiteneffekte
- Compile-Gate:
  mvn -q -DskipTests test-compile


────────────────────────────────────────
P3.20 — Diagnose-Ausgabe
────────────────────────────────────────

MÖGLICH:
- Anzahl Sources
- Anzahl Index-Dateien
- Anzahl Groups
- Anzahl LoadedNpcDefinitions
- namespacedRoleIds
- HytaleRoles
- Profile count
- CustomProfile count
- GenericJsonDefinition count
- Pool count
- Contract count
- Prefab count
- Territory count
- Profile ohne Handler
- Source/Namespace pro Definition

WICHTIG:
- debug-gated
- keine NPC-Instanzdaten
- keine selectedAppearanceId
- keine currentOutfitId
- keine state.json-Daten

REVIEW:
- Diagnose zeigt Baupläne, keine Runtime
- kein Tick-Spam
- Compile-Gate:
  mvn -q -DskipTests test-compile


────────────────────────────────────────
P3.21 — validateLoadedStateAgainstDefinitions() nur TODO
────────────────────────────────────────

Noch keine echte State-Reconcile-Logik.

TODO-Kommentar muss sagen:

Später prüfen:
- roleId/namespacedRoleId gegen Definition
- selectedSpeciesId gegen SpeciesPool
- selectedBodyProfileId gegen BodyPool
- selectedAppearanceId gegen AppearancePool
- currentOutfitId gegen OutfitPool
- selectedCompositionId gegen CompositionPool
- selectedPrefabId gegen Prefab
- structureInstanceId gegen StructureRecord
- territoryId gegen TerritoryRecord
- spawnAnchorId gegen SpawnAnchorRecord
- markerAssignments gegen MarkerDefinition

Aber:
- nichts löschen
- nichts neu würfeln
- nichts automatisch reparieren
- nichts speichern

REVIEW:
- Methode sichtbar
- keine State-Seiteneffekte
- Restart ≠ neu würfeln
- Compile-Gate:
  mvn -q -DskipTests test-compile


################################################################################
################################################################################
5. ASSETSTORE-MIGRATIONSPLAN
################################################################################
################################################################################

────────────────────────────────────────
5.1 Warum nicht sofort voll AssetStore?
────────────────────────────────────────

AssetStore ist langfristig gut,
aber für MVP wahrscheinlich zu schwer.

Du müsstest sofort bauen:
- eigene Asset-Klassen
- Codecs
- AssetMap
- Store-Registrierung
- Reload-Regeln
- Dependency-Regeln

Für P3 MVP besser:
- eigene Parser/Validatoren
- aber über Interfaces

Dadurch bleibt die Migration leicht.


────────────────────────────────────────
5.2 Was muss heute schon AssetStore-ready sein?
────────────────────────────────────────

Heute schon:
- KeystoneDefinitionSource Interface
- KeystoneAssetId
- KeystoneJsonDocument
- JsonDefinitionTypeRegistry
- ProfileTypeRegistry
- Parser getrennt von Source
- Validator getrennt von Source
- Registry getrennt von Source

Dann später:
ResourceDefinitionSource raus
AssetStoreDefinitionSource rein

DefinitionLoader bleibt fast gleich.


────────────────────────────────────────
5.3 Späterer AssetStore-Umbau
────────────────────────────────────────

Später mögliche Stores:
- KeystoneNpcGroupAssetStore
- KeystoneProfileAssetStore
- KeystonePoolAssetStore
- KeystoneStructureAssetStore
- KeystoneTerritoryAssetStore

Jeder Store lädt einen bestimmten Type.

Dann:
AssetStore lädt JSON
→ gibt JsonDocument/Asset zurück
→ DefinitionLoader nutzt dieselbe Validierung
→ Registry wird atomisch ersetzt

Wichtig:
Keine P3-Logik darf davon abhängen,
ob JSON aus ClassLoader, AssetPack oder AssetStore kommt.


################################################################################
################################################################################
6. RESTART- UND STATE-REGELN
################################################################################
################################################################################

────────────────────────────────────────
6.1 Grundregel
────────────────────────────────────────

Definitionen dürfen sich ändern.
Gespeicherte NPCs dürfen dadurch nicht gelöscht oder überschrieben werden.

Definition geändert
≠ NPC löschen

Restart
≠ neu auswürfeln


────────────────────────────────────────
6.2 Nicht in state.json speichern
────────────────────────────────────────

Nicht speichern:
- Profile-Map
- NpcProfileRefs
- ProfileTypeRules
- JsonDefinitionTypeRules
- Routine-Inhalte
- Actions-Inhalte
- Movement-Inhalte
- Navigation-Inhalte
- Combat-Inhalte
- Dialogue-Inhalte
- Trading-Inhalte
- Events-Inhalte
- HytaleRole-Datei-Inhalte
- vollständige Pool-Inhalte
- EntityRef
- RuntimeNpc
- aktive Navigation
- laufende Action
- Tick-State


────────────────────────────────────────
6.3 In state.json später speichern
────────────────────────────────────────

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

StructureRecord:
- structureInstanceId
- selectedPrefabId
- selectedCompositionId
- worldKey / worldId
- position
- rotation
- occupiedSlots
- markerRecords

TerritoryRecord:
- territoryId
- worldKey / worldId
- centerPosition
- radius
- anchorIds
- patrolMarkerIds
- maxNpcCount
- activeNpcIds

MarkerRecord:
- markerId
- markerName
- markerType
- worldKey / worldId
- structureInstanceId optional
- territoryId optional
- slotId optional
- relativePosition
- worldPosition optional/cache

Wichtig:
NpcRecord verweist nur auf IDs.
MarkerRecords/StructureRecords/TerritoryRecords nicht pro NPC duplizieren.


────────────────────────────────────────
6.4 Wenn Definitionen geändert werden
────────────────────────────────────────

Neue RoleId:
- wird geladen
- alte NPCs bleiben

RoleId entfernt:
- NPC bleibt gespeichert
- Diagnose DEFINITION_MISSING
- kein Löschen

Profile geändert:
- neue Profile gelten nach Restart
- NPC bleibt gespeichert
- ungültige Definition blockiert Runtime

Pool-Eintrag entfernt:
- NPC bleibt gespeichert
- Diagnose SELECTED_*_MISSING
- kein Neu-Auswürfeln beim Restart

Prefab entfernt:
- StructureRecord bleibt
- Diagnose PREFAB_DEFINITION_MISSING
- kein automatischer Ersatz

Territory entfernt:
- NPC bleibt gespeichert
- Diagnose TERRITORY_MISSING
- kein neuer Anchor automatisch

CustomProfile entfernt:
- System läuft nicht mehr
- NPC bleibt gespeichert
- alter Fortschritt nicht automatisch löschen


################################################################################
################################################################################
7. FINAL REVIEW P3
################################################################################
################################################################################

PASS-KRITERIEN:
- DefinitionSource-Abstraktion existiert.
- HytaleRoleGateway existiert.
- P3 nutzt NPCPlugin/BuilderManager nur über Gateway.
- Kein NPCEntity in P3.
- Kein RoleBuilderSystem in P3.
- Kein Spawn.
- Kein Relink.
- Kein setRoleName.
- ResourceDefinitionSource funktioniert als MVP.
- AssetPack/AssetStore-Migration ist vorbereitet.
- index.json wird geladen.
- group.json wird geladen.
- jede Variant hat genau eine RoleId.
- SharedProfiles + Variant.Profiles werden gemerged.
- NpcProfileRefs nutzt Map (String, NpcProfileRef).
- ProfileTypeRegistry unterstützt Required/Optional/Custom.
- JsonDefinitionTypeRegistry unterstützt bekannte und unbekannte Types.
- Neue ProfileKeys können geladen werden.
- Neue JsonTypes können basic geladen werden.
- Required-Core fehlt → Fehler.
- Optional fehlt → okay.
- Eingetragen aber fehlt/kaputt → Fehler.
- Duplicate namespacedRoleId → Fehler.
- Duplicate HytaleRole → erlaubt.
- Pools werden geprüft, aber nicht ausgeführt.
- Contracts werden geprüft.
- Prefabs werden geprüft.
- Territories werden geprüft.
- Registry ersetzt atomisch.
- Keine state.json-Änderung.
- Keine Pool-Auswahl.
- Keine Runtime-Seiteneffekte.

COMPILE:
mvn -q -DskipTests test-compile muss PASS sein.

P3 ENTSCHEIDUNG:
PASS, wenn Definitionen vollständig, modular, API-bewusst und atomisch geladen werden,
ohne Spawn-/Runtime-/State-Seiteneffekte.


################################################################################
################################################################################
8. WICHTIGSTER MERKSATZ
################################################################################
################################################################################

P3 ist kein Spawn-System.

P3 ist ein modularer Definition-Loader.

Heute liest er Keystone-JSONs über ResourceDefinitionSource.

Morgen kann dieselbe Architektur über AssetPack oder AssetStore laufen.

NPCPlugin / BuilderManager prüfen nur echte HytaleRoles.

AssetModule / AssetPack / AssetStore werden vorbereitet,
damit andere Mods später eigene JSONs liefern können.

NPCEntity / RoleBuilderSystem bleiben außerhalb von P3.

Neue JSON-Keys und neue JSON-Dateitypen werden basic geladen,
aber erst ausgeführt, wenn es einen Handler gibt.

Java bleibt generisch.
JSON erzeugt die Vielfalt.