P3 ACTION PLAN — loadDefinitions() / DefinitionLoader / DefinitionRegistry
UPDATED: ausführliche Version mit Modularitätsregeln, Namespaces, ProfileRefs, Pools, Contracts, Structure-bound, Territory-bound und Restart-Regeln

ZIEL VON P3:
NPC-Definitionen aus resources laden, prüfen und im RAM registrieren.

P3 lädt Baupläne.
P3 erzeugt keine echten NPCs.

WICHTIGE GRUNDREGEL:
Resource-JSONs = Baupläne / Definitionen / Profile / Pools / Contracts / Prefabs / Territories
state.json = konkrete Welt-/NPC-Instanzen

Pool laden ≠ Pool auswürfeln.

In P3 werden Pools, Profile, Definitionen, Contracts, Prefab-Definitionen und Territory-Definitionen nur geladen und geprüft.
Eine konkrete Auswahl aus einem Pool passiert erst später beim Spawn einer echten NPC-Instanz.

Beispiel:
loadDefinitions()
→ lädt SpeciesPool / BodyPool / AppearancePool / OutfitPool / CompositionPool

spawn RoleId lumberjack
→ wählt selectedSpeciesId / selectedBodyProfileId / selectedAppearanceId / currentOutfitId / selectedCompositionId
→ speichert diese konkrete Auswahl später in state.json

Restart
→ lädt gespeicherte Auswahl aus state.json
→ würfelt NICHT neu


NICHT IN P3:
- kein Spawn
- kein Relink
- kein NpcRecord aus state.json aktivieren
- kein saveStateSafely()
- keine EntityRef
- keine RuntimeNpc-Logik
- keine Pools auswürfeln
- kein DataStore(T)
- keine MarkerAssignments schreiben
- keine state.json-Änderung
- kein automatischer Repair
- kein Outfit-Wechsel-System ausführen
- keine StructureInstance erzeugen
- keine TerritoryInstance erzeugen
- keine SpawnAnchor-Instanz erzeugen
- keine MarkerRecords erzeugen
- keine NPC-Entity erzeugen


################################################################################
################################################################################
GLOBALE P3-GRUNDREGELN
################################################################################
################################################################################

────────────────────────────────────────
1. Eine Variant = genau eine RoleId
────────────────────────────────────────

Eine Variant ist genau ein Bauplan.

Richtig:

Variants:
- RoleId: lumberjack
- RoleId: lumberjack_wife
- RoleId: blacksmith
- RoleId: trader

Falsch:

Eine Variant mit:
RoleIds:
- lumberjack
- lumberjack_wife

Warum:
Wenn eine Variant mehrere RoleIds enthält, weiß die Registry später nicht sauber:
- Welche RoleId wird gespawnt?
- Welche Profile gehören genau dazu?
- Welche DisplayName gilt?
- Welche Marker gelten?
- Welche Debug-Regeln gelten?

Merksatz:
Group = Sammlung mehrerer Baupläne
Variant = genau ein Bauplan
RoleId = eindeutige ID dieses Bauplans
CompositionPool = entscheidet später, welche RoleIds zusammen spawnen


────────────────────────────────────────
2. SharedProfiles + Variant.Profiles werden gemerged
────────────────────────────────────────

SharedProfiles sind Defaults für alle Variants der Group.

Variant.Profiles überschreibt nur einzelne Profile.

Merge-Regel:

1. SharedProfiles laden
2. Variant.Profiles darüberlegen
3. Wenn derselbe Key existiert:
   Variant gewinnt

Beispiel:

SharedProfiles:
- SpeciesPool = pools/species/human_worker_species_pool.json
- BodyPool = pools/body/human_worker_body_pool.json
- Movement = profiles/movement/human_walk.json
- Navigation = profiles/navigation/friendly_worker.json
- Combat = profiles/combat/peaceful.json
- Persistence = profiles/persistence/persistent_citizen.json

Variant.Profiles:
- BodyPool = pools/body/human_old_body_pool.json
- OutfitPool = pools/outfits/lumberjack_outfit_pool.json
- Routine = npc/lumberjack/routines/lumberjack_day_cycle.json
- Actions = npc/lumberjack/actions/lumberjack_actions.json

Effektives Ergebnis:
- SpeciesPool = aus SharedProfiles
- BodyPool = aus Variant
- Movement = aus SharedProfiles
- Navigation = aus SharedProfiles
- Combat = aus SharedProfiles
- Persistence = aus SharedProfiles
- OutfitPool = aus Variant
- Routine = aus Variant
- Actions = aus Variant

Wichtig:
Variant.Profiles löscht nicht automatisch SharedProfiles.
Variant.Profiles überschreibt nur gleiche Keys.

Wenn später ein Profil bewusst deaktiviert werden soll, dann nicht mit null arbeiten.
Besser später:
DisabledProfiles:
- Dialogue


────────────────────────────────────────
3. ProfileRefs bleiben generisch
────────────────────────────────────────

Nicht:

NpcProfileRefs:
- routine
- actions
- movement
- navigation
- combat
- ...

Sondern:

NpcProfileRefs:
- Map(String, NpcProfileRef) profiles

Warum:
Neue Profile-Keys wie Dialogue, Trading, Reputation, Events, SeasonalOutfits oder CustomSomething sollen später ergänzt werden können, ohne dass NpcProfileRefs als Java-Klasse jedes Mal geändert werden muss.

Java soll generisch bleiben.
JSON beschreibt, was der NPC nutzt.


────────────────────────────────────────
4. Required / Optional / Custom Profile
────────────────────────────────────────

Required Core Profiles:
- Routine
- Actions
- Movement
- Navigation
- Persistence

Diese müssen nach dem Merge aus SharedProfiles + Variant.Profiles vorhanden sein.

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

Diese dürfen fehlen.
Wenn sie aber eingetragen sind, müssen sie gültig sein.

Custom Profiles:
- unbekannte neue Keys
- werden erlaubt
- werden basic validiert
- werden als CustomProfileRef gespeichert
- werden nicht ausgeführt, solange kein Handler existiert

Regel:
Required-Core fehlt → Fehler
Optional-Core fehlt → okay
Eingetragen, aber Datei fehlt → Fehler
Eingetragen, aber JSON kaputt → Fehler
Unbekannter Key vorhanden und Datei gültig → okay
Unbekannter Key vorhanden, aber Datei fehlt/kaputt → Fehler
Nicht eingetragen → nicht aktiv


────────────────────────────────────────
5. Appearance / AppearancePool / BodyPool / OutfitPool
────────────────────────────────────────

Appearance und AppearancePool sind nicht zwingend beide nötig.

Regel:
Entweder Appearance ODER AppearancePool.
Nicht zwingend beides.

Zusätzlich ist unser neueres Modell:

SpeciesPool:
= Volk/Wesen
Beispiele:
- human
- elf
- orc
- vampire
- wolf

BodyPool:
= Körperprofil
Beispiele:
- human_male
- human_female
- human_old_male
- human_old_female
- human_child

AppearancePool:
= dauerhaftes Grundaussehen
Kann Gesicht, Haare, Augen, Ohren, Mund, Körperdetails usw. enthalten.

OutfitPool:
= Kleidung
Darf später alle 2–4 Ingame-Tage wechseln.

Wichtig:
Body/Appearance = Identität, bleibt nach Spawn stabil.
Outfit = Kleidung, darf geplant wechseln.

P3 lädt nur.
P3 wählt nichts aus.


────────────────────────────────────────
6. HytaleRole vs RoleId
────────────────────────────────────────

HytaleRole:
= echte Hytale Engine-Role
= liegt unter Server/NPC/Roles/
= Engine-Basis / Bewegungsart / Grundfähigkeit

RoleId:
= Keystone-Logik-ID / Job / soziale Rolle
= steht in group.json / Variant
= wird später in state.json gespeichert

Beispiel:
RoleId = keystone:blacksmith
HytaleRole = Keystone_Human_Worker

Mehrere RoleIds dürfen dieselbe HytaleRole nutzen.

Beispiel:
keystone:lumberjack
keystone:blacksmith
keystone:trader
keystone:worker_spouse

dürfen alle:
HytaleRole = Keystone_Human_Worker

Wichtig:
Duplicate namespacedRoleId = Fehler
Duplicate HytaleRole = erlaubt

Kein dynamisches:
setRoleName("KeystoneNPC_npcId_roleId_Role")

HytaleRole muss echte Role-Datei sein oder eine valide Base-Reference nutzen.


────────────────────────────────────────
7. Namespaces
────────────────────────────────────────

RoleId ohne Namespace:
lumberjack

wird intern:
keystone:lumberjack

RoleId mit Namespace:
othermod:lumberjack

bleibt:
othermod:lumberjack

Duplicate-Regel:
keystone:lumberjack + keystone:lumberjack = Fehler
keystone:lumberjack + othermod:lumberjack = erlaubt

Auch Assets/Profile/Pools sollen langfristig Namespace/AssetId vorbereiten.

NpcProfileRef sollte deshalb Felder vorbereiten:
- namespace
- assetId
- path

Für P3 reicht:
- path ist Pflicht
- namespace/assetId optional vorbereitet


────────────────────────────────────────
8. Structure-bound und Territory-bound
────────────────────────────────────────

Structure-bound NPC:
= an Haus / Prefab / StructureInstance gebunden

Beispiele:
- Bewohner eines Hauses
- Schmied in einer Schmiede
- Händler im Shop
- Holzfäller in Worker-House + Arbeitsbereich

Territory-bound NPC:
= an Gebiet / SpawnAnchor / GuardZone gebunden

Beispiele:
- Bandit in Lager
- Wolf im Waldgebiet
- Guard an einem Tor
- Raid-Gegner an Raid-Anchor

P3 lädt dafür nur Baupläne.

P3 erzeugt keine:
- StructureInstance
- TerritoryInstance
- SpawnAnchorRecord
- MarkerRecord


────────────────────────────────────────
9. Contracts und Tags
────────────────────────────────────────

Contracts:
= harte technische Eignung

Beispiel:
blacksmith braucht:
- ResidenceContract
- BlacksmithWorkstationContract

Prefab liefert:
- ResidenceContract
- BlacksmithWorkstationContract

Dann passt es.

Tags:
= weiche Auswahl / Stil / Region

Beispiele:
- city
- village
- stone
- wood
- worker
- medium

Contracts entscheiden:
Kann diese Structure funktional genutzt werden?

Tags entscheiden:
Welche passende Structure sieht stilistisch gut aus?

P3 prüft:
- Contract-Dateien existieren
- Prefab-Definitionen verweisen nur auf existierende Contracts
- Tags sind optional


################################################################################
################################################################################
P3 STEPS
################################################################################
################################################################################

────────────────────────────────────────
P3.0 — Resource-Preflight
────────────────────────────────────────

AGENT-AUFGABE:
- Prüfe und fixe nur resources.
- Keine Java-Dateien ändern.
- index.json soll nur aktive, vollständige Groups enthalten.
- Leere Platzhalter-Dateien sind okay, solange sie nicht aktiv required/eingetragen sind.
- Eingetragene Profile/Pools/Groups/Contracts/Prefabs müssen existieren und gültig sein.

BETROFFENE DATEIEN:
- src/main/resources/Server/NPC/Keystone/npc/index.json
- aktive group.json-Dateien
- aktiv referenzierte Profile
- aktiv referenzierte Pools
- aktiv referenzierte Contracts
- aktiv referenzierte Prefabs
- aktiv referenzierte Territories

RESOURCE-REGELN:
- JSON-Keys nutzen PascalCase / Großbuchstaben am Anfang.
- Marker-Namen dürfen klein bleiben:
  - bed
  - table_seat
  - safety_zone
  - market_stall
- ActionIds dürfen klein bleiben:
  - chop_wood
  - open_chest
  - eat_meal
- Eine Variant hat genau eine RoleId.
- SharedProfiles sind Defaults.
- Variant.Profiles überschreibt einzelne Keys.

REVIEW:
- index.json verweist nur auf existierende Group-Dateien.
- Jede aktive Group ist gültiges JSON.
- Jede aktive Group enthält mindestens eine Variant.
- Jede Variant hat genau eine RoleId.
- Aktive eingetragene Profile existieren.
- Aktive eingetragene Pools existieren.
- Aktive eingetragene Contracts existieren.
- Aktive eingetragene Prefabs existieren.
- Keine Java-Änderungen.

ENTSCHEIDUNG:
PASS, wenn der P3-Loader nicht an kaputten aktiven Ressourcen scheitert.


────────────────────────────────────────
P3.1 — Definition-Datenmodelle anlegen
────────────────────────────────────────

AGENT-AUFGABE:
Lege reine Datenmodelle für geladene NPC-Baupläne an.

NEUE DATEIEN:
- LoadedNpcDefinition.java
- NpcProfileRefs.java
- NpcProfileRef.java
- ProfileTypeRule.java
- ProfileTypeRegistry.java
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
- sourcePath optional

NpcProfileMap:
- Map(String, NpcProfile) profiles

NpcProfile:
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
- Map(String, ProfileTypeRule)
- Required-Core-Regeln
- Optional-Core-Regeln
- Fallback-Regel für Custom-Profile

NpcEngineDefinition:
- hytaleRole
- templateReference optional

NpcDisplayDefinition:
- fallbackName
- nameTranslationKey

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

WICHTIG:
- kein npcId
- keine EntityRef
- keine RuntimeNpc
- keine state.json-Felder
- keine selectedSpeciesId
- keine selectedBodyProfileId
- keine selectedAppearanceId
- keine selectedOutfitId
- keine currentOutfitId
- keine selectedCompositionId
- nur Bauplan-Daten
- keine festen Profilfelder in NpcProfileRefs
- NpcProfileRef ist nur ein Verweis, nicht der Profilinhalt
- Core-Profile brauchen in P3 nicht automatisch eigene Java-Klassen

REVIEW:
- Modelle enthalten nur Definition-/Resource-Daten.
- Keine Runtime-Daten wurden hineingemischt.
- Keine Persistenzdaten wurden hineingemischt.
- NpcProfileRefs nutzt Map(String, NpcProfileRef)
- ProfileTypeRegistry bildet Required/Optional/Custom ab.
- namespacedRoleId ist Registry-Key.
- Compile-Gate:
  mvn -q -DskipTests test-compile


────────────────────────────────────────
P3.2 — DefinitionRegistry bauen
────────────────────────────────────────

AGENT-AUFGABE:
Baue eine Registry, die geladene Definitionen im RAM hält.

BETROFFENE / NEUE DATEIEN:
- NpcDefinitionRegistry.java

REGISTRY-MAPS:
- byNamespacedRoleId
- byHytaleRole als MultiMap/List
- optional byGroupId später

WICHTIG:
byHytaleRole darf mehrere Definitionen enthalten.

Warum:
Mehrere Keystone-RoleIds dürfen dieselbe HytaleRole nutzen.

Duplicate-Regeln:
- Duplicate namespacedRoleId = Fehler
- Duplicate HytaleRole = erlaubt
- Duplicate HytaleRole höchstens Info/Warnung, kein harter Fehler

METHODEN:
- replaceAll(Collection(LoadedNpcDefinition))
- hasRoleId(String roleId)
- hasNamespacedRoleId(String namespacedRoleId)
- getByRoleId(String roleId)
- getByNamespacedRoleId(String namespacedRoleId)
- getAllByHytaleRole(String hytaleRole)
- isSpawnable(String roleId)
- size()

WICHTIG:
- Registry ersetzt Daten atomisch.
- Bei Fehler darf keine halb geladene Registry aktiv bleiben.
- Registry ist read-only nach außen.
- Keine direkte mutable Map herausgeben.
- isSpawnable(roleId) bedeutet nur:
  Definition ist vollständig geladen und grundsätzlich verwendbar.
- isSpawnable(roleId) spawnt nichts.

REVIEW:
- Duplicate namespacedRoleId blockiert.
- Duplicate HytaleRole blockiert nicht.
- Registry-Key ist namespacedRoleId.
- replaceAll ist atomisch.
- Keine mutable Map nach außen.
- Compile-Gate:
  mvn -q -DskipTests test-compile


────────────────────────────────────────
P3.3 — ResourcePathResolver / ResourceReader
────────────────────────────────────────

AGENT-AUFGABE:
Baue Loader-Helfer für Resource-Pfade.

ROOT-REGELN:
- KeystoneRoot = Server/NPC/Keystone/
- NpcRoot = Server/NPC/Keystone/npc/
- ProfilesRoot = Server/NPC/Keystone/profiles/
- PoolsRoot = Server/NPC/Keystone/pools/
- StructuresRoot = Server/NPC/Keystone/structures/
- TerritoriesRoot = Server/NPC/Keystone/territories/
- RolesRoot = Server/NPC/Roles/
- ConfigRoot = Server/NPC/Keystone/config/

PFADREGELN:
- index.json liegt unter Keystone/npc/index.json
- Group-Pfade aus index.json sind relativ zu Keystone/npc/
- Profile-Pfade sind relativ zu KeystoneRoot
- Pool-Pfade sind relativ zu KeystoneRoot
- Structure-/Prefab-Pfade sind relativ zu KeystoneRoot
- Contract-Pfade sind relativ zu KeystoneRoot
- Territory-Pfade sind relativ zu KeystoneRoot
- Engine.HytaleRole verweist auf Server/NPC/Roles/(HytaleRole).json
- Global Debug liegt unter Server/NPC/Keystone/config/debug.json

GENERIC-PROFILE-REGEL:
Profiles-Map kann beliebige Keys enthalten:
- Routine
- Actions
- Movement
- Navigation
- Combat
- Events
- Dialogue
- Trading
- CustomSomething

ResourceReader:
- iteriert über Map
- erwartet keine feste Profilfeld-Struktur
- prüft Path-Traversal
- prüft Existenz
- liest JSON
- basic validiert unbekannte Keys

WICHTIG:
- Kein File-System-Path für externe state.json verwenden.
- Nur resources lesen.
- Fehlende eingetragene Resource = harter Load-Fehler.
- Pfade dürfen nicht aus Resource-Root ausbrechen.
- Eine hardcoded Core-Liste darf nur in ProfileTypeRegistry existieren, nicht als starre NpcProfileRefs-Struktur.

REVIEW:
- Pfadregeln sind klar getrennt.
- Kein Pfad kann aus Resource-Root ausbrechen.
- ResourceReader kann Profile aus Map iterieren.
- ResourceReader kann Pools/Contracts/Prefabs/Territories lesen.
- Keine hardcoded lumberjack-only Lösung.
- Compile-Gate:
  mvn -q -DskipTests test-compile


────────────────────────────────────────
P3.4 — index.json laden
────────────────────────────────────────

AGENT-AUFGABE:
Implementiere in loadDefinitions() oder DefinitionLoader:
- index.json lesen
- JSON-Syntax prüfen
- Liste von Group-Dateien extrahieren
- jede Group-Datei laden

AKTUELL:
- Server/NPC/Keystone/npc/index.json reicht für P3.

LANGFRISTIG NICHT VERBAUEN:
- mehrere Module
- mehrere Namespaces
- mehrere index.json-Dateien
- eigene RoleIds pro Modul
- keine stillen Overrides

VALIDIERUNG:
- Id vorhanden
- Version vorhanden
- Type == NpcIndex
- Groups nicht leer
- jeder Eintrag String
- jede Group-Datei existiert

WICHTIG:
- index.json wird wirklich benutzt.
- Keine hardcoded lumberjack_group.json-only Lösung.
- Fehler stoppen Bootstrap.
- Keine state.json wird geschrieben.
- Kein Spawn.

REVIEW:
- index.json wird geladen.
- Groups werden aus JSON gelesen.
- Keine hardcoded Group-Liste.
- Fehler stoppt Bootstrap.
- Kein Save.
- Kein Spawn.
- Compile-Gate:
  mvn -q -DskipTests test-compile


────────────────────────────────────────
P3.5 — group.json + Variants parsen
────────────────────────────────────────

AGENT-AUFGABE:
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
- Display.FallbackName
- Display.NameTranslationKey
- Profiles als Map(String, String)
- Markers.RequiredMarkers
- Markers.MarkerRoles
- Markers.RoutineMarkers optional
- Markers.EventMarkers optional
- Markers.SafetyMarkers optional
- Markers.OptionalMarkers optional
- Debug

VALIDIERUNG:
- Id nicht leer
- Version vorhanden
- Type == NpcGroup
- Namespace leer erlaubt → default keystone
- Variants nicht leer
- RoleId nicht leer
- eine Variant hat genau eine RoleId
- HytaleRole nicht leer
- Profiles kann leer sein, wenn SharedProfiles Required-Core liefert
- finale ProfileRefs nach Merge müssen Required-Core erfüllen

MERGE:
EffectiveProfiles = SharedProfiles + Variant.Profiles

Wenn gleicher Key:
Variant.Profiles gewinnt.

WICHTIG:
- SharedProfiles sind Defaults.
- Variant.Profiles überschreibt gezielt.
- Profile überschreiben nur ihren eigenen Bereich.
- Marker bleiben pro Variant / pro RoleId.
- Marker sind nicht global für die ganze Gruppe.
- Display ist keine technische Identität.
- RoleId wird namespaced normalisiert.
- Nicht eingetragen = nicht aktiv.
- Eingetragen aber kaputt = Fehler.
- Profile-Inhalte werden nicht in state.json gespeichert.
- Profiles definieren, was eine RoleId können kann.
- state.json speichert später nur konkrete Instanzdaten.

REVIEW:
- RoleId ersetzt alte id/role-Dopplung.
- HytaleRole bleibt getrennt von RoleId.
- Display ist nur Anzeige/Fallback.
- Profiles wird generisch als Map gelesen.
- SharedProfiles + Variant.Profiles werden korrekt gemerged.
- Required-Core wird nach Merge geprüft.
- Eine Variant hat genau eine RoleId.
- Compile-Gate:
  mvn -q -DskipTests test-compile


────────────────────────────────────────
P3.6 — HytaleRole + Reference prüfen
────────────────────────────────────────

AGENT-AUFGABE:
Für jede LoadedNpcDefinition:
- Server/NPC/Roles/(HytaleRole).json muss existieren oder als Base-Reference erlaubt sein.
- Role-Datei lesen.
- Wenn "Reference" vorhanden ist:
  - lokale Reference prüfen, falls sie im Mod liegen soll
  - Base-/Hytale-Reference nicht blind als Fehler behandeln, wenn sie aus Hytale/Base-Resources kommt

BEISPIEL:
HytaleRole = Keystone_Human_Worker
→ Server/NPC/Roles/Keystone_Human_Worker.json

Reference = Template_Human_Friendly
→ darf lokale Datei sein
→ oder Base-Hytale-Reference, wenn Loader das erlaubt

WICHTIG:
- Nur Existenz und minimale JSON-Gültigkeit prüfen.
- Noch nicht alle Hytale Role-Felder vollständig interpretieren.
- HytaleRole ist Engine-Anbindung.
- RoleId ist Keystone-Bauplan-ID.
- Mehrere RoleIds dürfen dieselbe HytaleRole nutzen.
- HytaleRole wird nicht in state.json als Profilinhalt gespeichert.
- state.json speichert roleId/namespacedRoleId der konkreten NPC-Instanz.
- Kein Role-Prefix-Fallback.
- Kein dynamisches setRoleName("KeystoneNPC_...").

REVIEW:
- Fehlende HytaleRole blockiert Definition-Load.
- Base-Reference-Regel ist klar.
- Duplicate HytaleRole blockiert nicht.
- RoleId/HytaleRole bleiben getrennt.
- Keine dynamischen Role-Namen.
- Compile-Gate:
  mvn -q -DskipTests test-compile


────────────────────────────────────────
P3.7 — Profile-Dateien prüfen
────────────────────────────────────────

AGENT-AUFGABE:
Für jede LoadedNpcDefinition:
- alle NpcProfileRefs aus EffectiveProfiles prüfen
- Required-Core streng prüfen
- Optional-Core prüfen, falls vorhanden
- Custom-Profile basic validieren, falls vorhanden

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
- SpeciesPool
- BodyPool
- OutfitPool
- CompositionPool
- Appearance
- AppearancePool

Appearance-Regel:
- Appearance oder AppearancePool darf existieren
- beides ist nicht zwingend
- wenn beides fehlt: aktuell okay, wenn BodyPool/SpeciesPool genutzt wird
- wenn später Appearance-System required wird: eigene ProfileTypeRule anpassen

MINIMALE VALIDIERUNG FÜR ALLE PROFILE:
- Datei existiert
- JSON ist syntaktisch gültig
- Id existiert
- Version existiert
- Type existiert oder ist laut Rule optional

FÜR BEKANNTE PROFILE:
- je nach ProfileTypeRule zusätzliche Struktur prüfen

BEISPIELE:
Actions:
- Actions-Objekt existiert

Routine:
- Schedule oder passende Routine-Struktur existiert

Persistence:
- PersistenceId / Save / Respawn / Safety grob vorhanden, falls Rule es fordert

Custom:
- nur Grundcheck
- kein Ausführen
- kein Blockieren wegen unbekanntem Key selbst
- Blockieren nur, wenn eingetragene Custom-Datei fehlt oder kaputt ist

WICHTIG:
- Noch keine Action ausführen.
- Noch keine Routine starten.
- Nur prüfen und IDs sammeln.
- Profile-Inhalte werden NICHT in state.json geschrieben.
- Profile sind Baupläne und kommen bei jedem Restart frisch aus resources.
- Fehlende optionale nicht eingetragene Profile sind okay.
- Fehlende eingetragene Profile sind Fehler.

REVIEW:
- Required-Core fehlt nach Merge → Fehler.
- Optional fehlt → okay.
- Eingetragen aber fehlt → Fehler.
- Eingetragen aber kaputt → Fehler.
- Custom eingetragen aber gültig → okay.
- Keine Gameplay-Ausführung.
- Compile-Gate:
  mvn -q -DskipTests test-compile


────────────────────────────────────────
P3.8 — Pool-Dateien vorbereiten / prüfen, aber NICHT würfeln
────────────────────────────────────────

AGENT-AUFGABE:
Falls Profile auf Pools verweisen:
- SpeciesPool prüfen
- BodyPool prüfen
- AppearancePool prüfen
- OutfitPool prüfen
- CompositionPool prüfen

MINIMALE VALIDIERUNG:
- Pool-Datei existiert
- Pool hat Id
- Pool hat Version
- Pool hat Type
- Pool hat Entries
- jeder Entry hat stabile Id
- Weight gültig, falls vorhanden
- Entry.Profile existiert, falls verwendet

SpeciesPool:
= mögliche Wesen/Volk-Auswahl

BodyPool:
= mögliche Körperprofile

AppearancePool:
= dauerhaftes Grundaussehen

OutfitPool:
= wechselbare Kleidung

CompositionPool:
= Zusammensetzung / Slots / RoleId-Kombination

P3-REGEL:
- Pool-Dateien laden
- Pool-Dateien prüfen
- Pool-Refs registrieren
- KEINE Auswahl treffen
- KEINE selectedSpeciesId erzeugen
- KEINE selectedBodyProfileId erzeugen
- KEINE selectedAppearanceId erzeugen
- KEINE currentOutfitId erzeugen
- KEINE selectedOutfitId erzeugen
- KEINE selectedCompositionId erzeugen

WICHTIG:
- Pool laden ≠ Pool auswürfeln.
- Pools sind Baupläne.
- Pool-Entries brauchen stabile IDs.
- Entfernte Pool-Einträge dürfen später alte NPCs nicht automatisch neu auswürfeln.
- Beim Restart wird später die gespeicherte Auswahl aus state.json verwendet und nicht neu gewürfelt.
- Kein state.json-Schreiben in P3.

REVIEW:
- Pools werden nicht ausgeführt.
- Keine Zufallsauswahl in P3.
- Keine state.json-Änderung.
- Pool-Einträge sind nur validierte Möglichkeiten.
- Compile-Gate:
  mvn -q -DskipTests test-compile


────────────────────────────────────────
P3.9 — Contracts / Prefabs / Structures prüfen
────────────────────────────────────────

AGENT-AUFGABE:
Neue Resource-Struktur berücksichtigen:
- structures/contracts/
- structures/prefabs/
- structures/tags/

Contract prüfen:
- Id
- Version
- Type
- ContractId
- RequiredSlots optional
- RequiredMarkers optional
- Purpose optional

Prefab prüfen:
- Id
- Version
- Type
- PrefabId
- PrefabPath
- ProvidesContracts
- Tags
- Slots
- Markers
- CompositionPools optional

Cross-Validation:
- ProvidesContracts müssen existierende Contracts sein.
- CompositionPools müssen existieren, wenn eingetragen.
- MarkerTypes müssen später zu MarkerType.java passen.
- Tags sind optional/weich.

WICHTIG:
- P3 platziert kein Prefab.
- P3 erzeugt keine StructureInstance.
- P3 erzeugt keine MarkerRecords.
- P3 reserviert keine Slots.
- P3 wählt keine Composition.

REVIEW:
- Contracts entscheiden technische Eignung.
- Tags entscheiden nur Stil/Region.
- Prefabs verweisen nur auf existierende Contracts.
- Kein Spawn.
- Kein state.json-Schreiben.
- Compile-Gate:
  mvn -q -DskipTests test-compile


────────────────────────────────────────
P3.10 — Territory-Definitionen prüfen
────────────────────────────────────────

AGENT-AUFGABE:
territories/*.json prüfen.

TerritoryProfile prüfen:
- Id
- Version
- Type == TerritoryProfile
- TerritoryId
- Binding
- Radius
- Spawning
- RolePool
- RequiredContracts

Cross-Validation:
- RequiredContracts müssen existieren.
- Radius-Werte müssen positiv sein.
- RolePool.RoleId muss als Definition existieren oder als spätere noch nicht aktive Role markiert sein.

WICHTIG:
- P3 erzeugt keine TerritoryInstance.
- P3 erzeugt keine SpawnAnchorRecord.
- P3 spawnt keine Hostiles.
- P3 weist keine PatrolSlots zu.

REVIEW:
- Territory-bound ist nur Bauplan.
- Kein Spawn.
- Kein state.json-Schreiben.
- Compile-Gate:
  mvn -q -DskipTests test-compile


────────────────────────────────────────
P3.11 — Cross-Validation: Routine, Actions, Marker
────────────────────────────────────────

AGENT-AUFGABE:
Prüfe Verweise innerhalb einer LoadedNpcDefinition.

ACTIONS:
- Routine darf nur ActionIds verwenden, die in Actions.json existieren.

MARKER:
- RoutineMarkers sind für Tagesroutine.
- EventMarkers sind für Events.
- SafetyMarkers sind für Flucht/Sicherheit.
- OptionalMarkers dürfen fehlen.

WICHTIGE ÄNDERUNG:
Event-/Safety-Marker sind nicht automatisch RequiredMarkers.

Routine darf:
- RoutineMarkers nutzen
- RequiredMarkers nutzen
- OptionalMarkers nutzen, wenn Step Fallback besitzt

Events dürfen:
- EventMarkers nutzen
- SafetyMarkers nutzen
- OptionalMarkers nutzen, wenn Event Fallback besitzt

Harter Fehler:
- Routine verweist auf unbekannte ActionId.
- Routine verweist auf Marker, der weder required/routine/optional/event/safety erlaubt ist.
- Action-Datei fehlt oder ist kaputt.
- Routine-Datei fehlt oder ist kaputt.

Kein harter Fehler:
- OptionalMarker fehlt.
- EventMarker fehlt später bei konkreter Instanz, wenn nur dieses Event blockiert wird.
- Routine benutzt nicht jeden Marker.

WICHTIG:
- Kein Alias-System.
- Kein cook -> FOOD-Fallback.
- Marker-Namen müssen exakt passen.
- Routine muss NICHT jeden Marker benutzen.
- Safety-/Event-Marker können existieren, ohne Teil der Routine zu sein.
- CustomProfile ohne Handler wird nicht tief cross-validiert.
- Kein automatisches Reparieren.
- Kein Schreiben in state.json.

REVIEW:
- Ungültige ActionId wird erkannt.
- Ungültiger TargetMarker wird erkannt.
- Event/Safety-Marker sind getrennt vom Routine-System.
- Routine muss nicht jeden Marker benutzen.
- OptionalMarker fehlt → okay.
- Compile-Gate:
  mvn -q -DskipTests test-compile


────────────────────────────────────────
P3.12 — Global Debug laden
────────────────────────────────────────

AGENT-AUFGABE:
Lade globale Debug-Konfiguration aus:
Server/NPC/Keystone/config/debug.json

REGEL:
Log nur wenn:
global.Enabled == true
UND role.Debug.Enabled == true
UND passender Bereich == true

WICHTIG:
- Global Debug = Master-Schalter.
- Role Debug = Feinsteuerung.
- Global false blockiert alle Debug-Logs.
- Role Debug darf Global nicht überschreiben.
- Debug ist Resource-/Config-Daten.
- Debug kommt nicht in state.json.
- Keine Tick-Spam-Logs.

REVIEW:
- debug.json wird gelesen.
- Global Debug ist Master-Schalter.
- Role Debug überschreibt Global nicht.
- Logs sind optional und debug-gated.
- Compile-Gate:
  mvn -q -DskipTests test-compile


────────────────────────────────────────
P3.13 — Bootstrap-Integration
────────────────────────────────────────

AGENT-AUFGABE:
Binde DefinitionLoader in NpcPluginBootstrap.loadDefinitions() ein.

ABLAUF:
- loadWorldState()
- loadDefinitions()
- validateLoadedStateAgainstDefinitions() bleibt TODO/no-op
- registerCommands()
- registerStartupEvents()

loadDefinitions() muss:
- index.json laden
- group.json laden
- SharedProfiles + Variant.Profiles mergen
- namespaces normalisieren
- ProfileTypeRegistry nutzen
- Required/Optional/Custom-Regeln anwenden
- Profile prüfen
- Pools prüfen
- Contracts prüfen
- Prefabs prüfen
- Territories prüfen
- atomisch in Registry setzen

FEHLERREGEL:
- loadDefinitions() muss bei Fehler Bootstrap stoppen.
- Keine Exception schlucken.
- Wenn catch nur zum Loggen genutzt wird, danach erneut werfen.
- Definition-Fehler darf kein Save auslösen.
- Definition-Fehler darf keine state.json überschreiben.
- Keine Commands registrieren, wenn Definitionen kaputt sind.
- Keine StartupEvents registrieren, wenn Definitionen kaputt sind.

REVIEW:
- Bootstrap bricht bei Definition-Load-Failure ab.
- Fehlender optionaler Key stoppt Bootstrap nicht.
- Eingetragene fehlende Datei stoppt Bootstrap.
- Kein Save bei Definition-Fehler.
- Keine state.json-Änderung.
- Compile-Gate:
  mvn -q -DskipTests test-compile


────────────────────────────────────────
P3.14 — Diagnose-Ausgabe vorbereiten
────────────────────────────────────────

AGENT-AUFGABE:
Nur interne Debug-/Status-Ausgabe vorbereiten.
Noch keine Commands erzwingen.

MÖGLICH:
- Anzahl geladener Definitionen
- geladene RoleIds
- geladene namespacedRoleIds
- geladene HytaleRoles
- Anzahl bekannter Profile
- Anzahl CustomProfiles
- Anzahl Pools
- Anzahl Contracts
- Anzahl Prefabs
- Anzahl Territories
- Profile ohne Handler

WICHTIG:
- Global Debug ist Master-Schalter.
- Keine Tick-Spam-Logs.
- Keine sensitive Runtime-Daten.
- Keine NPC-Instanzdaten aus state.json loggen.
- Keine selectedAppearanceId/currentOutfitId loggen.
- Profile ohne Handler werden nur informativ gemeldet, nicht als Fehler.

REVIEW:
- Logs sind debug-gated.
- Keine Runtime-/State-Daten.
- Keine Spawns.
- Compile-Gate:
  mvn -q -DskipTests test-compile


────────────────────────────────────────
P3.15 — validateLoadedStateAgainstDefinitions() nur als sichtbarer TODO
────────────────────────────────────────

AGENT-AUFGABE:
Noch keine echte State-Reconcile-Logik implementieren.
Nur Methode sichtbar lassen / kommentieren.

ZWECK SPÄTER:
Nach Restart:
state.json
→ konkrete NPC-Instanzen

Definitions
→ aktuelle Baupläne

validateLoadedStateAgainstDefinitions()
→ prüft gespeicherte NPCs gegen aktuelle Definitionen
→ löscht nichts automatisch
→ schreibt nichts automatisch
→ würfelt nichts automatisch neu
→ setzt später Diagnose / Blocker

SPÄTERER CHECK:
- NpcRecord.roleId/namespacedRoleId gegen aktuelle Definition prüfen
- selectedSpeciesId gegen SpeciesPool prüfen
- selectedBodyProfileId gegen BodyPool prüfen
- selectedAppearanceId gegen AppearancePool prüfen
- currentOutfitId gegen OutfitPool prüfen
- selectedOutfitId gegen OutfitPool prüfen, falls genutzt
- selectedCompositionId gegen CompositionPool prüfen
- selectedPrefabId gegen Prefab-Definition prüfen
- structureInstanceId gegen StructureRecord prüfen
- territoryId gegen TerritoryRecord prüfen
- spawnAnchorId gegen SpawnAnchorRecord prüfen
- markerAssignments gegen aktuelle MarkerDefinition prüfen

P3 SELBST:
- kein NpcRecord-Reconcile
- kein MarkerAssignment-Reconcile
- kein selectedAppearanceId-Check
- kein Outfit-Check
- kein selectedCompositionId-Check
- kein state.json-Save
- kein Repair
- keine Runtime- oder State-Seiteneffekte

TODO-KOMMENTAR MUSS NENNEN:
- Restart ≠ neu würfeln
- state.json = konkrete Auswahl / Instanzdaten
- resources = Baupläne / Profile / Pools
- Profile-Inhalte gehören nicht in state.json
- CustomProfiles werden nicht aus state.json geladen
- obsolete oder fehlende Definitionsdaten löschen keine NPCs automatisch

REVIEW:
- Methode existiert sichtbar.
- Kommentar beschreibt Restart ≠ neu würfeln.
- Kommentar beschreibt state.json = konkrete Auswahl, resources = Baupläne.
- Keine Runtime- oder State-Seiteneffekte.
- Compile-Gate:
  mvn -q -DskipTests test-compile


################################################################################
################################################################################
FINAL REVIEW P3
################################################################################
################################################################################

PASS-KRITERIEN:
- index.json wird geladen.
- aktive group.json-Dateien werden geladen.
- jede Variant erzeugt genau eine LoadedNpcDefinition.
- eine Variant hat genau eine RoleId.
- SharedProfiles + Variant.Profiles werden korrekt gemerged.
- Required-Core Profile sind nach Merge vorhanden:
  - Routine
  - Actions
  - Movement
  - Navigation
  - Persistence

- Optional-Core Profile dürfen fehlen:
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

- Appearance oder AppearancePool ist erlaubt.
- Beides ist nicht zwingend.
- OutfitPool ist getrennt von Appearance.
- Duplicate namespacedRoleId blockiert.
- Duplicate HytaleRole blockiert nicht.
- Namespaces sind vorbereitet.
- Asset/Profile namespace/assetId ist vorbereitet.
- Unbekannte Profile werden erlaubt und basic validiert.
- Eingetragene kaputte Profile blockieren.
- Eingetragene fehlende Profile blockieren.
- Pools werden geprüft, aber nicht ausgeführt.
- Contracts werden geprüft.
- Prefab-Definitionen werden geprüft.
- Territory-Definitionen werden geprüft.
- Event/Safety-Marker sind getrennt vom Routine-System.
- Keine state.json-Änderung.
- Kein Spawn.
- Kein Relink.
- Keine RuntimeNpc.
- Keine EntityRef.
- Kein automatischer Repair.
- Kein Neu-Auswürfeln.

SICHERHEIT:
- Kein Spawn.
- Kein Relink.
- Kein Save.
- Keine state.json-Änderung.
- Keine RuntimeNpc-Erzeugung.
- Keine EntityRef.
- Kein Role-Prefix-Fallback.
- Kein automatischer Repair.
- Keine Pool-Auswahl.
- Keine neue selectedSpeciesId.
- Keine neue selectedBodyProfileId.
- Keine neue selectedAppearanceId.
- Keine neue selectedOutfitId.
- Keine neue currentOutfitId.
- Keine neue selectedCompositionId.
- Kein Neu-Auswürfeln.

COMPILE:
mvn -q -DskipTests test-compile muss PASS sein.

P3 ENTSCHEIDUNG:
PASS, wenn Definitionen vollständig geladen, validiert und atomisch registriert werden,
ohne Runtime-/State-/Spawn-Seiteneffekte.


################################################################################
################################################################################
RESTART- UND DEFINITION-ÄNDERUNGSREGELN
################################################################################
################################################################################

────────────────────────────────────────
Grundregel
────────────────────────────────────────

Definitionen dürfen sich ändern.
Bereits gespeicherte NPCs dürfen dadurch aber nicht gelöscht, überschrieben oder kaputt gespeichert werden.

Resource-JSONs speichern Baupläne.
state.json speichert konkrete Welt-/NPC-Instanzen.

Definition geändert
≠ NPC löschen

Restart
≠ neu auswürfeln


────────────────────────────────────────
Nicht in state.json speichern
────────────────────────────────────────

Nicht speichern:
- Profile-Map
- NpcProfileRefs
- ProfileTypeRules
- Routine-Inhalte
- Actions-Inhalte
- Movement-Inhalte
- Navigation-Inhalte
- Combat-Inhalte
- Skills-Inhalte
- Persistence-Profil-Inhalte
- Dialogue-Inhalte
- Trading-Inhalte
- Events-Inhalte
- HytaleRole-Datei-Inhalte
- vollständige Appearance-Profil-Inhalte
- vollständige Pool-Inhalte
- EntityRef
- RuntimeNpc
- aktive Navigation / aktuelle Route
- laufende Action
- Tick-/Door-/Runtime-State


────────────────────────────────────────
In state.json speichern
────────────────────────────────────────

NpcRecord:
- npcId
- roleId oder namespacedRoleId
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
Nicht jeder NPC speichert alles.
Structure-bound NPCs speichern Structure-Bindung.
Territory-bound NPCs speichern Territory-Bindung.

Keine MarkerRecords in jedem NPC duplizieren.
NpcRecord verweist nur auf IDs.


────────────────────────────────────────
Ablauf beim Server-Restart
────────────────────────────────────────

1. state.json laden
→ alte gespeicherte NPCs laden
→ MarkerAssignments laden
→ MarkerRecords / Koordinaten laden
→ konkrete Species/Body/Appearance/Outfit-Auswahl laden
→ Structure-/Slot-Zuordnung laden
→ Territory-/SpawnAnchor-Zuordnung laden

2. aktuelle Definitionen laden
→ neue group.json laden
→ neue profiles laden
→ neue routines laden
→ neue actions laden
→ neue marker rules laden
→ neue pools laden
→ neue contracts laden
→ neue prefabs laden
→ neue territories laden
→ neue event rules laden
→ neue CustomProfileRefs laden
→ neue ProfileTypeRules anwenden

3. validateLoadedStateAgainstDefinitions()
→ gespeicherte NPCs gegen aktuelle Definitionen prüfen
→ aber NICHT automatisch löschen
→ NICHT automatisch state.json überschreiben
→ NICHT automatisch neue zufällige Auswahl würfeln
→ ungültige Teile blockieren oder Diagnose setzen

4. Runtime vorbereiten
→ erst nach gültiger Prüfung darf ein NPC später relinkt/aktiviert werden
→ Live-NPCs nutzen nach Restart die aktuellen Definitionen


────────────────────────────────────────
Neue RoleId hinzugefügt
────────────────────────────────────────

Beispiel:
Neue RoleId fisher kommt in group.json dazu.

Handling:
- neue RoleId wird normal geladen
- neue RoleId wird namespaced normalisiert
- neue RoleId wird spawnbar, wenn alle required/eingetragenen Profile gültig sind
- alte NPCs bleiben unverändert
- keine state.json-Änderung nötig
- neue NPCs können später diese neue RoleId verwenden


────────────────────────────────────────
RoleId entfernt
────────────────────────────────────────

Beispiel:
state.json enthält roleId lumberjack_oldman,
aber group.json enthält lumberjack_oldman nicht mehr.

Handling:
- NPC-Record bleibt erhalten
- NPC wird NICHT gelöscht
- NPC wird NICHT gespawnt
- NPC wird NICHT automatisch auf eine andere RoleId umgeschrieben
- NPC bekommt Diagnose-Status: DEFINITION_MISSING
- keine automatische Reparatur
- state.json wird nicht überschrieben


────────────────────────────────────────
Profile geändert
────────────────────────────────────────

Beispiel:
lumberjack nutzt jetzt andere Routine, Actions, Movement, Combat, Skills oder Persistence.

Handling:
- roleId/namespacedRoleId bleibt stabil
- NPC bleibt gespeichert
- neue Profile werden aus aktuellen Resource-JSONs geladen
- NpcRecord wird gegen die neuen Profile neu bewertet
- wenn Profile gültig sind: NPC kann später wieder aktivierbar sein
- wenn Profile ungültig sind: NPC bleibt gespeichert, aber wird blockiert
- kein Spawn, kein Relink, keine Runtime-Logik bei ungültiger Definition
- state.json wird nicht automatisch verändert
- Profile-Inhalte werden nicht in state.json gespeichert


────────────────────────────────────────
Neuer optionaler Profile-Key hinzugefügt
────────────────────────────────────────

Beispiel:
lumberjack bekommt Dialogue.

Handling:
- Dialogue wird aus resources geladen, wenn eingetragen
- wenn Datei existiert und basic valid ist: RoleId bekommt ProfileRef
- wenn Datei fehlt oder kaputt ist: Definition blockieren
- wenn Dialogue nicht eingetragen ist: kein Dialogue-System aktiv
- alte NPCs bleiben gespeichert
- state.json wird nicht geändert
- Dialogue-Inhalte werden nicht in state.json gespeichert
- Ausführung passiert später nur, wenn ein Handler existiert


────────────────────────────────────────
CustomProfile hinzugefügt
────────────────────────────────────────

Beispiel:
CustomSomething wird in Profiles eingetragen.

Handling:
- Loader erlaubt unbekannten Key
- Datei muss existieren
- JSON muss basic valid sein
- Id muss existieren
- Version muss existieren
- CustomProfileRef wird an RoleId gebunden
- kein Ausführen ohne Handler
- kein state.json-Schreiben
- keine NPC-Instanzdaten daraus erzeugen


────────────────────────────────────────
CustomProfile entfernt
────────────────────────────────────────

Beispiel:
CustomSomething war früher eingetragen, ist jetzt entfernt.

Handling:
- Definition lädt ohne dieses CustomProfile
- NPC-Record bleibt erhalten
- keine state.json-Änderung
- keine automatische Reparatur
- falls später state.json-Fortschritt zu diesem System existiert:
  - Fortschritt nicht automatisch löschen
  - Diagnose setzen
  - nur expliziter Migration-/Repair-Step darf dauerhaft speichern


────────────────────────────────────────
Appearance / Body geändert
────────────────────────────────────────

Beispiel:
BodyPool oder AppearancePool enthält andere Einträge.

Handling:
- NPC bleibt gespeichert
- selectedBodyProfileId bleibt stabil
- selectedAppearanceId bleibt stabil
- beim Restart werden beide gegen aktuelle Pools geprüft
- wenn gespeicherte ID noch existiert: weiter verwenden
- wenn gespeicherte ID fehlt: Diagnose setzen
  - SELECTED_BODY_MISSING
  - SELECTED_APPEARANCE_MISSING
- kein automatisches Neu-Auswürfeln beim Restart
- keine automatische state.json-Änderung
- neue NPCs dürfen neue Pool-Auswahl verwenden


────────────────────────────────────────
Outfit / Kleidung geändert
────────────────────────────────────────

Beispiel:
NPC soll alle 2–4 Ingame-Tage neue Kleidung aus einem Pool bekommen.

Handling:
- currentOutfitId wird in state.json gespeichert
- selectedOutfitId kann zusätzlich als Spawn-/Basis-Auswahl gespeichert werden, falls das Modell es braucht
- outfitPoolId wird in state.json gespeichert
- lastOutfitChangeDay wird in state.json gespeichert
- nextOutfitChangeDay wird in state.json gespeichert
- beim Restart bleibt currentOutfitId gleich
- nur wenn currentDay >= nextOutfitChangeDay, darf ein neues Outfit gewählt werden
- neues Outfit wird in Runtime gesetzt
- NpcRecord.currentOutfitId wird aktualisiert
- dirty=true setzen
- nicht sofort bei jeder Änderung direkt auf Disk schreiben
- Autosave alle 5–10 Minuten oder Shutdown speichert später gesammelt


────────────────────────────────────────
Pool-Eintrag entfernt
────────────────────────────────────────

Beispiel:
currentOutfitId = blacksmith_apron_brown,
aber dieser Entry existiert im OutfitPool nicht mehr.

Handling:
- NPC bleibt gespeichert
- kein Neu-Auswürfeln beim Restart
- Diagnose setzen:
  - SELECTED_OUTFIT_MISSING
- betroffene Darstellung/Logik blockieren oder Fallback nur runtime verwenden
- Fallback darf nicht automatisch als neue Wahrheit gespeichert werden
- nur expliziter Migration-/Repair-Step darf dauerhaft ändern


────────────────────────────────────────
Composition geändert
────────────────────────────────────────

Beispiel:
CompositionPool ändert blacksmith_couple.

Handling:
- selectedCompositionId wird in state.json gespeichert
- beim Restart bleibt selectedCompositionId gleich
- selectedCompositionId wird gegen aktuellen CompositionPool geprüft
- wenn selectedCompositionId noch existiert: weiter verwenden
- wenn selectedCompositionId fehlt: Diagnose SELECTED_COMPOSITION_MISSING
- kein Neu-Auswürfeln beim Restart
- keine automatische state.json-Änderung


────────────────────────────────────────
Prefab geändert
────────────────────────────────────────

Beispiel:
selectedPrefabId verweist auf simple_stone_worker_house_blacksmith,
aber Prefab-Definition wurde entfernt oder geändert.

Handling:
- StructureRecord bleibt erhalten
- NPC bleibt erhalten
- MarkerRecords bleiben erhalten
- keine automatische Löschung
- Diagnose setzen:
  - PREFAB_DEFINITION_MISSING
  - PREFAB_CONTRACT_MISMATCH
- Runtime/Spawn blockieren, wenn Binding nicht mehr sicher ist
- kein automatisches Ersetzen durch anderes Prefab


────────────────────────────────────────
Actions geändert
────────────────────────────────────────

Beispiel:
Actions.json entfernt chop_wood oder fügt neue Action hinzu.

Handling:
- neue Actions werden aus aktueller Actions.json geladen
- Routine darf nur ActionIds benutzen, die in Actions.json existieren
- fehlende ActionId blockiert diese Definition
- NPC bleibt gespeichert
- keine Routine starten, solange Action-Verweise ungültig sind
- keine automatische Action-Ersetzung
- Actions-Inhalte werden nicht in state.json gespeichert


────────────────────────────────────────
Routine geändert
────────────────────────────────────────

Beispiel:
Routine nutzt neue Marker oder neue Actions.

Handling:
- neue Routine wird beim Serverstart aus aktueller Routine-JSON geladen
- neue Routine wird gegen aktuelle Actions.json geprüft
- neue Routine wird gegen aktuelle MarkerDefinition der RoleId geprüft
- Routine muss NICHT jeden Marker benutzen
- Routine nutzt nur die Marker, die sie für Tagesablauf braucht
- wenn Routine auf unbekannte Action zeigt: Definition blockieren
- wenn Routine auf unbekannten Marker zeigt: Definition blockieren oder Diagnose
- NPC-Record bleibt gespeichert
- MarkerAssignments / Marker-Koordinaten aus state.json werden gegen neue Routine-/Marker-Definition neu bewertet
- Live-NPC muss nach erfolgreichem Restart/Relink sofort die neue Routine verwenden
- alte Routine-Fortschritte aus state.json dürfen nicht blind weiterlaufen
- falls SaveRoutineProgress später existiert: nur übernehmen, wenn es zur aktuellen Routine-Version passt
- wenn Routine-Version nicht passt: Fortschritt verwerfen und NPC sauber im neuen Routine-System starten


────────────────────────────────────────
RequiredMarkers geändert
────────────────────────────────────────

Beispiel:
alte Definition hatte work,
neue Definition hat lumber_work oder safety_zone.

Handling:
- RequiredMarkers dürfen sich nach Restart ändern
- gespeicherter NPC wird dadurch NICHT gelöscht
- alte MarkerAssignments werden gegen aktuelle Definition neu bewertet
- nicht mehr passende Assignments werden nicht aktiv benutzt
- MarkerRecords / Koordinaten bleiben erhalten
- neue RequiredMarkers werden neu aufgelöst
- wenn neue Pflichtmarker fehlen: NPC bleibt gespeichert, aber betroffene Routine/Event-Logik startet nicht
- Diagnose: MISSING_REQUIRED_MARKER
- nur expliziter Repair-/Migration-Step darf dauerhaft speichern


────────────────────────────────────────
Event-/Safety-Marker hinzugefügt
────────────────────────────────────────

Beispiel:
neues Event-System braucht safety_zone für Überfälle.

Handling:
- EventMarkers sind eigene Marker-Verwendung
- SafetyMarkers sind eigene Marker-Verwendung
- Routine muss safety_zone nicht enthalten
- EventProfile darf safety_zone benutzen
- wenn safety_zone fehlt: Event-Logik für diesen NPC blockieren
- normale Routine kann trotzdem laufen, wenn ihre eigenen Marker gültig sind
- NPC bleibt gespeichert
- EventMarker dürfen neu aufgelöst werden
- Marker-Koordinaten bleiben erhalten


────────────────────────────────────────
OptionalMarker / POI fehlt
────────────────────────────────────────

Beispiel:
Worker-Frau soll morgens zum Markt gehen, wenn market_stall existiert.
Wenn nicht, soll sie waschen.

Handling:
- OptionalMarker/POI fehlt → kein harter NPC-Fehler
- Routine/Event-Zweig nutzt Fallback
- NPC bleibt aktiv, wenn Required-Routine gültig ist
- kein state.json-Repair
- kein automatisches Marker-Erzeugen

Beispiel:
Market vorhanden:
→ gehe zu market_stall

Market fehlt:
→ gehe zu washing_place oder idle


────────────────────────────────────────
Marker-Koordinaten / MarkerRecords
────────────────────────────────────────

Handling:
- Marker-Koordinaten dürfen nicht blind gelöscht werden
- alte MarkerRecords bleiben erhalten
- alte Koordinaten bleiben erhalten
- nur aktive Zuordnung zur aktuellen RoleId wird neu bewertet
- obsolete MarkerAssignments werden nicht mehr aktiv benutzt
- MarkerRecord bleibt als gespeicherter Welt-/Prefab-/Structure-/Territory-Marker erhalten

MarkerRecord speichert später:
- markerId
- markerName
- markerType
- worldKey / worldId
- structureInstanceId optional
- territoryId optional
- slotId optional
- relativePosition
- worldPosition optional/cache


────────────────────────────────────────
MarkerAssignment passt nicht mehr
────────────────────────────────────────

Beispiel:
NPC hatte Assignment work -> marker_123,
aber aktuelle Definition erlaubt work nicht mehr.

Handling:
- Assignment nicht aktiv verwenden
- MarkerRecord marker_123 nicht löschen
- Koordinaten behalten
- Diagnose: OBSOLETE_MARKER_ASSIGNMENT
- neue Marker anhand aktueller Definition neu suchen
- nur expliziter Repair-/Migration-Step darf dauerhaft speichern
- kein automatisches Überschreiben beim normalen Restart


────────────────────────────────────────
Movement / Navigation geändert
────────────────────────────────────────

Handling:
- neue Movement-/Navigation-Profile werden geladen
- NPC bleibt gespeichert
- alte aktive Navigation wird nie aus state.json übernommen
- nach Restart gibt es keine fortgesetzte alte Route
- Runtime-Navigation wird später frisch aus aktueller Definition aufgebaut
- Live-NPC nutzt nach Restart das neue Movement-/Navigation-Profil


────────────────────────────────────────
Persistence geändert
────────────────────────────────────────

Beispiel:
RespawnAfterRestart wird von true auf false geändert.

Handling:
- aktuelle Persistence-Definition gilt für neue Entscheidungen
- NPC-Record bleibt erhalten
- wenn RespawnAfterRestart jetzt false ist: kein Auto-Respawn
- kein Löschen alter NPC-Daten
- kein Überschreiben der state.json nur wegen Config-Änderung
- Persistence-Inhalte selbst werden nicht in state.json gespeichert


────────────────────────────────────────
Combat / Event-Verhalten geändert
────────────────────────────────────────

Handling:
- neue Combat-/Event-Profile werden geladen
- NPC bleibt gespeichert
- Event-Logik startet nur, wenn benötigte EventMarker gültig sind
- fehlende EventMarker blockieren Event-Verhalten, aber nicht zwingend die komplette NPC-Existenz
- keine automatische Reparatur ohne expliziten Repair-Step
- Combat-/Event-Inhalte werden nicht in state.json gespeichert


────────────────────────────────────────
Structure-bound Binding geändert
────────────────────────────────────────

Handling:
- Structure-bound NPC bleibt gespeichert
- structureInstanceId bleibt Wahrheit
- selectedPrefabId bleibt Wahrheit
- selectedCompositionId bleibt Wahrheit
- homeSlotId/workSlotId bleiben Wahrheit
- wenn StructureRecord fehlt: Diagnose STRUCTURE_MISSING
- wenn Slot fehlt: Diagnose SLOT_MISSING
- wenn Contract nicht mehr passt: Diagnose STRUCTURE_CONTRACT_MISMATCH
- kein automatischer Umzug
- kein automatisches neues Haus wählen


────────────────────────────────────────
Territory-bound Binding geändert
────────────────────────────────────────

Handling:
- Territory-bound NPC bleibt gespeichert
- territoryId bleibt Wahrheit
- spawnAnchorId bleibt Wahrheit
- territoryCenter/Radius können gegen TerritoryRecord geprüft werden
- wenn Territory fehlt: Diagnose TERRITORY_MISSING
- wenn SpawnAnchor fehlt: Diagnose SPAWN_ANCHOR_MISSING
- kein automatischer neuer Anchor
- kein automatischer Respawn an anderer Stelle


────────────────────────────────────────
Autosave / große Zufallsevents
────────────────────────────────────────

Handling:
- normale Runtime-Änderungen setzen dirty=true
- Autosave speichert alle 5–10 Minuten, wenn dirty=true
- Shutdown speichert, wenn dirty=true
- vor großen Zufallsevents muss saveWorldStateSafely() erfolgreich laufen
- wenn Save vor großem Event fehlschlägt: Event blockieren oder verschieben
- nach Event-Änderungen dirty=true setzen


────────────────────────────────────────
Wichtigste Schutzregel
────────────────────────────────────────

Definition geändert
≠ NPC löschen

Definition geändert
→ NPC neu gegen aktuelle Definition bewerten
→ ungültige Teile blockieren
→ stabile Auswahl aus state.json behalten
→ MarkerAssignments neu bewerten
→ Marker-Koordinaten behalten
→ Structure/Territory-Bindung behalten
→ state.json nicht automatisch kaputt überschreiben

Restart
≠ neu auswürfeln

Restart
→ konkrete Auswahl aus state.json laden
→ aktuelle Resource-Definitionen anwenden
→ nur bewusst geplante Systeme wie Outfit-Wechsel dürfen später neue Auswahl treffen