P3 ACTION PLAN — loadDefinitions() / DefinitionLoader / DefinitionRegistry

ZIEL VON P3:
NPC-Definitionen aus resources laden, prüfen und im RAM registrieren.

P3 lädt Baupläne.
P3 erzeugt keine echten NPCs.

WICHTIGE GRUNDREGEL:
Resource-JSONs = Baupläne / Definitionen / Profile / Pools
state.json = konkrete Welt-/NPC-Instanzen

Pool laden ≠ Pool auswürfeln.

In P3 werden Pools, Profile und Definitionen nur geladen und geprüft.
Eine konkrete Auswahl aus einem Pool passiert erst später beim Spawn einer echten NPC-Instanz.

Beispiel:
loadDefinitions()
→ lädt AppearancePool / OutfitPool / CompositionPool

spawn RoleId lumberjack
→ wählt selectedAppearanceId / selectedOutfitId / selectedCompositionId
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
- kein DataStore<T>
- keine MarkerAssignments schreiben
- keine state.json-Änderung
- kein automatischer Repair
- kein Outfit-Wechsel-System ausführen


────────────────────────────────────────
P3.0 — Resource-Preflight
────────────────────────────────────────

AGENT-AUFGABE:
- Prüfe und fixe nur resources.
- index.json soll vorerst nur aktive, vollständige Groups enthalten.
- Wenn test_group nur Test ist und keine passenden Roles existieren, test/test_group.json aus index.json entfernen.
- Keine Java-Dateien ändern.

BETROFFENE DATEIEN:
- src/main/resources/Server/NPC/Keystone/npc/index.json
- ggf. test/test_group.json nur anfassen, wenn es aktiv bleiben soll

RESOURCE-REGELN:
- JSON-Keys nutzen PascalCase / Großbuchstaben am Anfang.
- Marker-Namen dürfen klein bleiben:
  - bed
  - chest
  - food
  - work
- ActionIds dürfen klein bleiben:
  - chop_wood
  - open_chest
  - eat_meal

REVIEW:
- index.json verweist nur auf existierende Group-Dateien.
- Jede aktive Group verweist nur auf existierende HytaleRole-Dateien.
- JSON-Syntax gültig.
- Keine Java-Änderungen.

ENTSCHEIDUNG:
PASS, wenn P3-Loader nicht sofort an absichtlich kaputten Testdaten scheitert.


────────────────────────────────────────
P3.1 — Definition-Datenmodelle anlegen
────────────────────────────────────────

AGENT-AUFGABE:
Lege reine Datenmodelle für geladene NPC-Baupläne an.

NEUE DATEIEN:
- LoadedNpcDefinition.java
- NpcProfileRefs.java
- NpcEngineDefinition.java
- NpcDisplayDefinition.java
- NpcMarkerDefinition.java
- NpcDebugDefinition.java

PACKAGE:
src/main/java/keystone/npc/definition/model/

INHALT:
LoadedNpcDefinition:
- roleId
- engine
- display
- profiles
- markers
- debug

NpcProfileRefs:
- appearance
- routine
- actions
- skills
- movement
- navigation
- combat
- spawn
- persistence
- events später
- appearancePool später
- outfitPool später
- compositionPool später

NpcEngineDefinition:
- hytaleRole
- templateReference optional

NpcDisplayDefinition:
- fallbackName
- nameTranslationKey

NpcMarkerDefinition:
- requiredMarkers
- markerRoles
- eventMarkers später
- optionalMarkers später

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
- keine selectedAppearanceId
- keine selectedOutfitId
- keine selectedCompositionId
- nur Bauplan-Daten

REVIEW:
- Modelle enthalten nur Definition-/Resource-Daten.
- Keine Runtime- oder Persistenzdaten wurden hineingemischt.
- roleId ist Keystone-Bauplan-ID, nicht npcId.
- selectedAppearanceId / selectedOutfitId / selectedCompositionId sind NICHT Teil der Definition.
- Compile-Gate:
  mvn -q -DskipTests test-compile


────────────────────────────────────────
P3.2 — DefinitionRegistry bauen
────────────────────────────────────────

AGENT-AUFGABE:
Baue eine Registry, die geladene Definitionen im RAM hält.

BETROFFENE / NEUE DATEIEN:
- NpcDefinition.java oder neue NpcDefinitionRegistry.java

REGISTRY-MAPS:
- byRoleId
- byHytaleRole
- optional byGroupId später

METHODEN:
- replaceAll(Collection<LoadedNpcDefinition>)
- hasRoleId(String roleId)
- getByRoleId(String roleId)
- isSpawnable(String roleId)
- size()

WICHTIG:
- Registry ersetzt Daten atomisch.
- Bei Fehler darf keine halb geladene Registry aktiv bleiben.
- Duplicate RoleId blockieren.
- Duplicate HytaleRole blockieren.
- isSpawnable(roleId) bedeutet nur:
  Definition ist vollständig geladen und grundsätzlich verwendbar.
- isSpawnable(roleId) spawnt nichts.

REVIEW:
- Registry ist read-only nach außen.
- Keine direkte mutable Map wird herausgegeben.
- Duplicate-Prüfung vorhanden.
- isSpawnable(roleId) prüft nur Definition-Existenz und Gültigkeit.
- Compile-Gate:
  mvn -q -DskipTests test-compile


────────────────────────────────────────
P3.3 — ResourcePathResolver / ResourceReader für Definitionen
────────────────────────────────────────

AGENT-AUFGABE:
Baue einen kleinen Loader-Helfer für Resource-Pfade.

ROOT-REGELN:
- KeystoneRoot = Server/NPC/Keystone/
- NpcRoot = Server/NPC/Keystone/npc/
- ProfilesRoot = Server/NPC/Keystone/profiles/
- RolesRoot = Server/NPC/Roles/
- ConfigRoot = Server/NPC/Keystone/config/

PFADREGELN:
- index.json liegt unter Keystone/npc/index.json
- Group-Pfade aus index.json sind relativ zu Keystone/npc/
- SharedProfiles sind relativ zu Keystone/
  Beispiel:
  profiles/skills/human_worker.json
- Variant Profiles sind relativ zu Keystone/
  Beispiel:
  npc/lumberjack/appearances/lumberjack.json
- Engine.HytaleRole verweist auf:
  Server/NPC/Roles/<HytaleRole>.json
- Global Debug liegt unter:
  Server/NPC/Keystone/config/debug.json

WICHTIG:
- Kein File-System-Path für externe state.json verwenden.
- Nur resources lesen.
- Fehlende Datei = harter Load-Fehler.
- Pfade dürfen nicht aus Resource-Root ausbrechen.

REVIEW:
- Pfadregeln sind klar getrennt.
- Kein Pfad kann aus Resource-Root ausbrechen.
- Fehlende Resource wird nicht ignoriert.
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

WICHTIG:
- index.json darf nicht leer sein.
- Jeder Eintrag muss String sein.
- Jede referenzierte Group-Datei muss existieren.
- Bei Fehler:
  IllegalStateException oder DefinitionLoadResult.failed
- Bootstrap darf bei Fehler nicht weitermachen.

REVIEW:
- index.json wird wirklich benutzt.
- Keine hardcoded lumberjack_group.json-only Lösung.
- Fehler stoppen Bootstrap.
- Keine state.json wird geschrieben.
- Kein Spawn.
- Compile-Gate:
  mvn -q -DskipTests test-compile


────────────────────────────────────────
P3.5 — group.json + Variants minimal parsen
────────────────────────────────────────

AGENT-AUFGABE:
Parse pro Group:
- Id
- Version
- Type
- SharedProfiles
- Variants[]

Parse pro Variant:
- RoleId
- Engine.HytaleRole
- Display.FallbackName
- Display.NameTranslationKey
- Profiles.Appearance
- Profiles.Routine
- Profiles.Actions
- Markers.RequiredMarkers
- Markers.MarkerRoles
- Debug

VALIDIERUNG:
- Id nicht leer
- Version vorhanden
- Type == NpcGroup
- Variants nicht leer
- RoleId nicht leer
- HytaleRole nicht leer
- RequiredMarkers nicht leer
- MarkerRoles vorhanden

WICHTIG:
- SharedProfiles + Variant.Profiles werden zur finalen LoadedNpcDefinition zusammengeführt.
- Profile überschreiben nur ihren eigenen Bereich.
- Marker bleiben pro Variant / pro RoleId.
- Marker sind nicht global für die ganze Gruppe.
- Display ist keine technische Identität.
- RoleId ersetzt alte id/role-Dopplung.

REVIEW:
- RoleId ersetzt alte id/role-Dopplung.
- HytaleRole bleibt getrennt von RoleId.
- Display ist nur Anzeige/Fallback, keine Logik-ID.
- Markers sind pro RoleId geladen.
- Compile-Gate:
  mvn -q -DskipTests test-compile


────────────────────────────────────────
P3.6 — HytaleRole + Template prüfen
────────────────────────────────────────

AGENT-AUFGABE:
Für jede LoadedNpcDefinition:
- Server/NPC/Roles/<HytaleRole>.json muss existieren.
- Role-Datei lesen.
- Wenn "Reference" vorhanden ist:
  - Server/NPC/Roles/<Reference>.json muss existieren.

BEISPIEL:
HytaleRole = Lumberjack
→ Server/NPC/Roles/Lumberjack.json
→ Reference = Template_Human_Friendly
→ Server/NPC/Roles/Template_Human_Friendly.json

WICHTIG:
- Nur Existenz und minimale JSON-Gültigkeit prüfen.
- Noch nicht alle Hytale Role-Felder vollständig interpretieren.
- Appearance aus Keystone-Profil bleibt später höhere Wahrheit als Role-Fallback.
- HytaleRole ist Engine-Anbindung.
- RoleId ist Keystone-Bauplan-ID.

REVIEW:
- Fehlende HytaleRole blockiert Definition-Load.
- Fehlende Template-Reference blockiert Definition-Load.
- Kein Role-Prefix-Fallback.
- Kein dynamisches setRoleName("KeystoneNPC_...").
- Compile-Gate:
  mvn -q -DskipTests test-compile


────────────────────────────────────────
P3.7 — Profil-Dateien prüfen
────────────────────────────────────────

AGENT-AUFGABE:
Für jede LoadedNpcDefinition prüfen:
- Appearance-Datei existiert.
- Routine-Datei existiert.
- Actions-Datei existiert.
- SharedProfiles existieren:
  - Skills
  - Movement
  - Navigation
  - Combat
  - Spawn
  - Persistence

MINIMALE VALIDIERUNG:
- Jede Profil-Datei hat Id.
- Jede Profil-Datei hat Version.
- Actions-Datei hat Actions-Objekt.
- Routine-Datei hat Routine-/Schedule-/Steps-Struktur, soweit aktuell vorhanden.
- Persistence-Datei hat RespawnAfterRestart oder relevante Persistence-Felder, falls vorhanden.

WICHTIG:
- Noch keine Action ausführen.
- Noch keine Routine starten.
- Nur prüfen und IDs sammeln.
- Profile-Inhalte werden NICHT in state.json geschrieben.
- Profile sind Baupläne und kommen bei jedem Restart frisch aus resources.

REVIEW:
- Fehlende Profil-Datei blockiert Definition-Load.
- Profile werden nicht still ignoriert.
- Keine Gameplay-Logik.
- Compile-Gate:
  mvn -q -DskipTests test-compile


────────────────────────────────────────
P3.8 — Pool-Dateien vorbereiten / prüfen, aber NICHT würfeln
────────────────────────────────────────

AGENT-AUFGABE:
Falls Profile auf Pools verweisen:
- AppearancePool prüfen
- OutfitPool prüfen
- CompositionPool prüfen

MINIMALE VALIDIERUNG:
- Pool-Datei existiert.
- Pool hat Id.
- Pool hat Version.
- Pool hat Entries.
- Jeder Entry hat stabile ID.
- Jeder Entry verweist auf existierende Profil-Datei oder Wert.
- Weight muss gültig sein, falls vorhanden.

WICHTIG:
- P3 lädt Pools nur als Baupläne.
- P3 wählt keine konkrete Appearance.
- P3 wählt kein konkretes Outfit.
- P3 wählt keine Composition.
- selectedAppearanceId / currentOutfitId / selectedCompositionId entstehen erst später beim Spawn oder durch bewusste Runtime-Systeme.
- Beim Restart wird später die gespeicherte Auswahl aus state.json verwendet und nicht neu gewürfelt.

REVIEW:
- Pools werden nicht ausgeführt.
- Keine Zufallsauswahl in P3.
- Keine state.json-Änderung.
- Pool-Einträge sind nur validierte Möglichkeiten.
- Compile-Gate:
  mvn -q -DskipTests test-compile


────────────────────────────────────────
P3.9 — Cross-Validation: Routine, Actions, Marker
────────────────────────────────────────

AGENT-AUFGABE:
Prüfe Verweise innerhalb einer LoadedNpcDefinition.

ACTIONS:
- Routine darf nur ActionIds verwenden, die in Actions.json existieren.

MARKER:
- Routine darf nur TargetMarker verwenden, die in Markers.RequiredMarkers stehen oder in später erlaubten Marker-Gruppen definiert sind.
- Für aktuellen Stand:
  Marker-Namen müssen exakt zu erlaubten Markern passen.
- MarkerRoles.keys müssen exakt RequiredMarkers entsprechen.
- MarkerRoles.values müssen zu bekannten MarkerType-Werten passen.

WICHTIG:
- Kein Alias-System.
- Kein cook -> FOOD-Fallback.
- Marker-Namen müssen exakt passen.
- Routine muss NICHT jeden Marker benutzen.
- Safety-/Event-Marker können später existieren, ohne Teil der Routine zu sein.
- Bei Fehler Bootstrap blockieren oder klare Diagnose erzeugen.
- Kein automatisches Reparieren.

REVIEW:
- Ungültige ActionId wird erkannt.
- Ungültiger TargetMarker wird erkannt.
- MarkerRoles und RequiredMarkers müssen exakt zusammenpassen.
- Routine nutzt nur erlaubte Marker.
- Kein Schreiben in state.json.
- Compile-Gate:
  mvn -q -DskipTests test-compile


────────────────────────────────────────
P3.10 — Global Debug laden
────────────────────────────────────────

AGENT-AUFGABE:
Lade globale Debug-Konfiguration aus:
Server/NPC/Keystone/config/debug.json

REGEL:
Global Debug = Master-Schalter
Role Debug = Feinsteuerung

Log nur, wenn:
global.Enabled == true
UND role.Debug.Enabled == true
UND passender Bereich == true

WICHTIG:
- Global false blockiert alle Debug-Logs.
- Role Debug darf Global nicht überschreiben.
- Role Debug verfeinert nur.
- Keine Tick-Spam-Logs.

REVIEW:
- debug.json wird gelesen.
- Global Debug ist Master-Schalter.
- Role Debug überschreibt Global nicht.
- Logs sind optional und debug-gated.
- Compile-Gate:
  mvn -q -DskipTests test-compile


────────────────────────────────────────
P3.11 — Bootstrap-Integration
────────────────────────────────────────

AGENT-AUFGABE:
Binde DefinitionLoader in NpcPluginBootstrap.loadDefinitions() ein.

ABLAUF:
- loadWorldState()
- loadDefinitions()
- validateLoadedStateAgainstDefinitions() bleibt vorerst TODO/no-op, aber sichtbar
- registerCommands()
- registerStartupEvents()

WICHTIG:
- loadDefinitions() muss bei Fehler Bootstrap stoppen.
- Keine Exception schlucken.
- Wenn catch nur zum Loggen genutzt wird, danach erneut werfen.
- Definition-Fehler darf kein Save auslösen.
- Definition-Fehler darf keine state.json überschreiben.

REVIEW:
- Bootstrap bricht bei Definition-Load-Failure ab.
- Keine Commands werden registriert, wenn Definitionen kaputt sind.
- Keine StartupEvents starten, wenn Definitionen kaputt sind.
- Kein Save bei Definition-Fehler.
- Compile-Gate:
  mvn -q -DskipTests test-compile


────────────────────────────────────────
P3.12 — Diagnose-Ausgabe vorbereiten
────────────────────────────────────────

AGENT-AUFGABE:
Nur interne Debug-/Status-Ausgabe vorbereiten, noch keine Commands erzwingen.

MÖGLICH:
- Anzahl geladener Definitionen loggen
- geladene RoleIds loggen, falls global Debug LogDefinitions true
- geladene HytaleRoles loggen
- geladene Profile/Pools count loggen

WICHTIG:
- Global Debug ist Master-Schalter.
- Role Debug verfeinert später nur.
- Keine Tick-Spam-Logs.
- Keine sensitive Runtime-Daten.
- Keine NPC-Instanzdaten aus state.json loggen.

REVIEW:
- Logs sind optional und debug-gated.
- Keine sensitive Runtime-Daten.
- Keine Spawns.
- Compile-Gate:
  mvn -q -DskipTests test-compile


────────────────────────────────────────
P3.13 — validateLoadedStateAgainstDefinitions() nur als sichtbarer TODO
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

WICHTIG:
In P3 noch:
- kein NpcRecord-Reconcile
- kein MarkerAssignment-Reconcile
- kein selectedAppearanceId-Check
- kein Outfit-Check
- kein state.json-Save

REVIEW:
- Methode existiert sichtbar als nächster Architekturpunkt.
- Keine Runtime- oder State-Seiteneffekte.
- Compile-Gate:
  mvn -q -DskipTests test-compile


────────────────────────────────────────
FINAL REVIEW P3
────────────────────────────────────────

PRÜFUNG:
- index.json wird geladen.
- lumberjack_group.json wird geladen.
- Alle Variants werden als LoadedNpcDefinition registriert:
  - lumberjack
  - lumberjack_wife
  - lumberjack_oldman
  - lumberjack_oldwife

- Duplicate RoleId blockiert.
- Duplicate HytaleRole blockiert.
- Fehlende HytaleRole-Datei blockiert.
- Fehlende Template-Reference blockiert.
- Fehlende Profile blockieren.
- Fehlende Pools blockieren, falls referenziert.
- Ungültige Marker blockieren.
- Ungültige ActionId aus Routine blockiert.
- Ungültiger TargetMarker aus Routine blockiert.

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
- Keine neue selectedAppearanceId.
- Keine neue currentOutfitId.
- Keine neue selectedCompositionId.

COMPILE:
- mvn -q -DskipTests test-compile muss PASS sein.

P3 ENTSCHEIDUNG:
PASS, wenn Definitionen vollständig geladen, validiert und atomisch registriert werden,
ohne Runtime-/State-/Spawn-Seiteneffekte.


################################################################################
################################################################################
################################################################################
RESTART- UND DEFINITION-ÄNDERUNGSREGELN
################################################################################
################################################################################
################################################################################

Was passiert, wenn Profile / Definitionen nach einem Restart geändert wurden?

Grundregel:
Definitionen dürfen sich ändern.
Bereits gespeicherte NPCs dürfen dadurch aber nicht gelöscht, überschrieben oder kaputt gespeichert werden.

Wichtig:
Resource-JSONs speichern Baupläne.
state.json speichert konkrete Welt-/NPC-Instanzen.

Nicht in state.json speichern:
- Routine-Inhalte
- Actions-Inhalte
- Movement-Inhalte
- Navigation-Inhalte
- Combat-Inhalte
- Skills-Inhalte
- Persistence-Profil-Inhalte
- HytaleRole-Datei-Inhalte
- vollständige Appearance-Profil-Inhalte
- vollständige Pool-Inhalte
- EntityRef
- RuntimeNpc
- aktive Navigation / aktuelle Route
- laufende Action
- Tick-/Door-/Runtime-State

In state.json speichern:
- npcId
- roleId
- entityUuid
- worldKey / worldId
- status
- lastKnownPosition
- structureInstanceId / prefabInstanceId
- slotId
- selectedAppearanceId
- selectedOutfitId
- selectedCompositionId
- outfitPoolId
- currentOutfitId
- lastOutfitChangeDay
- nextOutfitChangeDay
- markerAssignments
- MarkerRecords / Marker-Koordinaten
- Settlement-/Structure-Discovery später
- globale Welt-Fakten, die alle Spieler sehen sollen


────────────────────────────────────────
Ablauf beim Server-Restart
────────────────────────────────────────

1. state.json laden
	→ alte gespeicherte NPCs laden
	→ MarkerAssignments laden
	→ MarkerRecords / Koordinaten laden
	→ konkrete Appearance-/Outfit-Auswahl laden
	→ Structure-/Slot-Zuordnung laden

2. aktuelle Definitionen laden
	→ neue group.json laden
	→ neue profiles laden
	→ neue routines laden
	→ neue actions laden
	→ neue marker rules laden
	→ neue pools laden
	→ neue event rules laden

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
- neue RoleId wird spawnbar, wenn alle Profile gültig sind
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
- roleId bleibt stabil
- NPC bleibt gespeichert
- neue Profile werden aus den aktuellen Resource-JSONs geladen
- NpcRecord wird gegen die neuen Profile neu bewertet
- wenn Profile gültig sind: NPC kann später wieder aktivierbar sein
- wenn Profile ungültig sind: NPC bleibt gespeichert, aber wird blockiert
- kein Spawn, kein Relink, keine Runtime-Logik bei ungültiger Definition
- state.json wird nicht automatisch verändert


────────────────────────────────────────
Appearance geändert
────────────────────────────────────────

Beispiel:
Appearance-Profil zeigt jetzt auf andere Kleidung / anderes Grundmodell.

Handling:
- NPC bleibt gespeichert
- selectedAppearanceId aus state.json bleibt stabil
- beim Restart wird selectedAppearanceId gegen aktuelle Appearance-/Pool-Definition geprüft
- wenn selectedAppearanceId noch existiert: dieselbe Appearance wird wieder benutzt
- wenn selectedAppearanceId nicht mehr existiert: Diagnose SELECTED_APPEARANCE_MISSING
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
- Routine muss safety_zone nicht enthalten
- EventProfile darf safety_zone benutzen
- wenn safety_zone fehlt: Event-Logik für diesen NPC blockieren
- normale Routine kann trotzdem laufen, wenn ihre eigenen Marker gültig sind
- NPC bleibt gespeichert
- EventMarker dürfen neu aufgelöst werden
- Marker-Koordinaten bleiben erhalten


────────────────────────────────────────
Marker-Koordinaten / MarkerRecords
────────────────────────────────────────

Handling:
- Marker-Koordinaten dürfen nicht blind gelöscht werden
- alte MarkerRecords bleiben erhalten
- alte Koordinaten bleiben erhalten
- nur aktive Zuordnung zur aktuellen RoleId wird neu bewertet
- obsolete MarkerAssignments werden nicht mehr aktiv benutzt
- MarkerRecord bleibt als gespeicherter Welt-/Prefab-/Structure-Marker erhalten

MarkerRecord speichert später:
- markerId
- markerName
- markerType
- worldKey / worldId
- structureInstanceId / prefabInstanceId
- relativePosition
- worldPosition optional als Cache


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
→ MarkerAssignments neu auflösen
→ Marker-Koordinaten behalten
→ state.json nicht automatisch kaputt überschreiben

Restart
≠ neu auswürfeln

Restart
→ konkrete Auswahl aus state.json laden
→ aktuelle Resource-Definitionen anwenden
→ nur bewusst geplante Systeme wie Outfit-Wechsel dürfen später neue Auswahl treffen