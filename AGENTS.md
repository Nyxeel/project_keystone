# AGENTS.md — NPCMod / KeystoneNPC / KeyEntityMod

Du arbeitest an meiner Hytale-Mod:

NPCMod / KeystoneNPC / KeyEntityMod

Diese Datei enthält die allgemeinen Agent-Regeln und die aktuelle inhaltliche Projektausrichtung.

WICHTIG:
Die detaillierten, validierten Safety-Regeln stehen nicht vollständig hier,
sondern in den Kontroll-Dateien unter:

project_keystone/KeyEntityMod/docs/safety

Die Gesamtplanung und der Zielzustand des Projekts stehen unter:

project_keystone/KeyEntityMod/docs/planning

Diese Dateien sind Pflichtquellen und dürfen nicht ignoriert werden.

============================================================
0. AKTUELLER PROJEKTZUSTAND
============================================================

Das Projekt ist eine generische NPC-Foundation für Hytale.

Grundidee:

Java bleibt generisch.
JSON beschreibt die Vielfalt.

Java soll nicht hart wissen:

- Das ist ein Bürger.
- Das ist ein Worker.
- Das ist Friendly.
- Das ist Hostile.
- Das ist ein Holzfäller.
- Das ist ein Magier.

Java soll nur wissen:

- Dieser NPC hat eine Definition.
- Diese Definition hat Profile.
- Diese Profile sagen, was zu tun ist.
- Runtime-Systeme führen diese Profile sicher aus.

Die Mod soll später ermöglichen:

- prefab-gebundene Friendly-NPCs
- organische Dörfer über Worldgen v2
- NPC-Haushalte mit sinnvollen Compositions
- Biome-NPCs mit Radius
- Straßen-NPCs wie Wanderer, Händler, Karawanen
- Quest-NPCs
- Base-Arbeiter
- Hostile-Raids
- spätere Magier-/Mana-/AI-Logik aus JSON

Wichtigster Ziel-Satz:

Die Mod soll nicht jeden NPC manuell behandeln.

Sie liefert Vorlagen:

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

============================================================
0.1 AKTUELLE ARBEITSREIHENFOLGE
============================================================

Die Vision ist groß, aber nicht alles wird sofort gebaut.

Aktuelle sichere Reihenfolge:

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

Aktueller Skeleton-Merksatz:

Erst Daten + Manager + State stabil.
Dann Definition.
Dann Marker.
Dann Structure.
Erst danach Spawn, Routine und Navigation.

============================================================
0.2 AKTUELLER PHASENSTAND
============================================================

Phase 1:
Model-Skeleton.

Ziel:
Definieren, welche Datenformen existieren.

Wichtige Dateien:

- model/NpcRecord.java
- model/RuntimeNpc.java
- model/PersistedWorldState.java
- model/NpcEntityStatus.java
- model/NpcState.java

Inhaltliche Regeln:

NpcRecord
→ persistente NPC-Daten

RuntimeNpc
→ Live-Daten, niemals speichern

PersistedWorldState
→ kompletter state.json-Inhalt pro Welt

NpcEntityStatus
→ technischer/persistenter Entity-Lifecycle

NpcState
→ grober fachlicher Zustand

Phase-1-Failchecks:

- kein entityRef in NpcRecord
- kein Hytale Entity-Objekt in NpcRecord
- RuntimeNpc wird nie persistiert
- npcId nicht null/blank
- roleId nicht null/blank
- worldKey nicht null/blank
- currentPosition finite, wenn gesetzt
- markerAssignments keine leeren Keys/Values
- NpcState nicht mit jeder JSON-Action vollstopfen

Phase 2:
State-System stabilisieren.

Ziel:
state.json pro Welt sicher laden und speichern.

Pflichtdateien:

- state/NpcStateStore.java
- state/StateLoadResult.java
- state/StateSaveResult.java
- state/internal/WorldStateStore.java
- state/internal/StatePathResolver.java
- state/internal/StateFileIO.java
- state/internal/StateJsonCodec.java
- state/internal/StateBackupStore.java

Zusätzlich wichtig, aber keine neue Phase-2-Datei:

- model/PersistedWorldState.java

Phase 2 baut NICHT:

- keinen echten Hytale Spawn
- kein EntityRef
- kein Relink
- kein ChunkGate
- kein Worldgen
- keine Marker-v2-Runtime
- keine Commands
- keine Routine
- keine Navigation
- keine Action
- kein Combat
- keinen NpcManager.restoreRecords(...)
- keine Bootstrap-Verbindung
- keine echte Welt-Erkennung
- keine Hytale Entity-Objekte

Phase 2 ist nur:

- Datei lesen
- JSON prüfen
- Java-State daraus machen
- Java-State speichern
- Fehler ehrlich melden
- kaputte state.json niemals überschreiben

Phase-2-Mindestziel:

- StateLoadResult enthält PersistedWorldState.
- StateSaveResult ist ehrlich und validiert message.
- WorldStateStore speichert intern PersistedWorldState pro Welt.
- StateJsonCodec kann PersistedWorldState minimal encode/decode.
- Load-Failure überschreibt keine state.json.
- Save-Failure gibt failed/false zurück.
- Dirty wird nur nach echtem Save gelöscht.
- Keine Runtime-Daten landen im JSON-Modell.
- Pro Welt gibt es eigenen State.
- Harte Pfade sind nur als Skeleton-TODO markiert.
- Compile-Gate ist grün.

============================================================
1. ABSOLUTE GRUNDREGEL
============================================================

Nach jeder Code-Änderung musst du deinen eigenen Code nochmal prüfen.

Wenn du ein Problem findest:

- direkt fixen
- nicht nur erklären

Wenn etwas unklar ist:

- nicht löschen
- nicht spawnen
- nicht relinken
- nicht überschreiben
- nur warnen / loggen / Nutzer informieren

Ziel:

- keine Ghost-Spawns
- keine Orphans
- keine falschen Relinks
- keine NullPointer-Crashes
- keine versehentlichen Entity-Deletes
- keine kaputten JSON-/Persistenz-Zustände
- keine Commands mit gefährlichem Default-Verhalten
- keine Regression gegen safety/*.md
- keine Runtime-Daten in state.json
- kein kaputtes JSON als leerer Default-State speichern
- keine Fake-Hytale-Engine-Logik bauen

============================================================
2. PFLICHTQUELLEN UND QUELLENPRIORITÄT
============================================================

Vor jeder Änderung prüfen:

1. Aktueller Code im Repo
2. docs/planning/zielzustand.md
3. docs/planning/skeleton_plan.md
4. docs/planning/phase2Plan.md
5. docs/safety/*.md
6. Neueste Patchreports / Reviews
7. Ältere Analysen nur als Kontext

Wenn zwei Quellen widersprechen:

- nicht selbst still entscheiden
- keine Codeänderung durchführen, die eine validierte Safety-Regel brechen würde
- Konflikt melden
- betroffene Dateien nennen
- sichere Option empfehlen
- Nutzerentscheidung abwarten

Meldungsformat:

REGELKONFLIKT GEFUNDEN

Datei A:
- <Regel aus Datei A>

Datei B:
- <Regel aus Datei B>

Problem:
- <warum widerspricht sich das?>

Risiko:
- <was kann kaputtgehen?>

Empfehlung:
- <welche Regel wirkt sicherer / aktueller?>

Benötigte Entscheidung:
- Nutzer muss entscheiden, welche Regel gilt.
- Danach muss die betroffene Plan-/Safety-Datei aktualisiert werden.

============================================================
3. HYTALE-API-RESEARCH
============================================================

Wenn Hytale-API-Verhalten unklar ist:

- patcher.zip / dekompilierte HytaleServer-Klassen als Research-Werkzeug nutzen
- hytalemodding.dev als Dokumentation nutzen
- keine eigene Fake-Engine-Logik bauen, wenn Hytale dafür ein System besitzt

Vor eigener Logik prüfen, ob Hytale bereits etwas anbietet:

- Role-System
- Sensor
- BodyMotion
- Pathfinder
- InteractionChain
- DoorInteraction
- Entity Metadata
- World Query
- Scheduler/Event
- UUID-/Entity-Store-Lookup
- EntityRef-Lookup

Verboten:

- keine eigene Pathfinding-Engine als Hauptsystem
- keine lineare Eigenbewegung als Hauptsystem
- keine Block-State-Hacks, wenn InteractionChain möglich ist
- keine Hytale-API erraten
- keine temporäre Fake-API als Wahrheit ins Projekt einbauen

============================================================
4. NPC-IDENTITÄT
============================================================

NPCs sind Instanzen, keine bloßen Rollen.

Eine NPC-Rolle wie lumberjack ist nur ein Bauplan.

Eine konkrete NPC-Instanz besitzt:

- npcId
- roleId
- entityUuid
- worldId / worldKey
- structureInstanceId
- slotId
- markerAssignments
- currentPosition
- entityStatus
- selectedAppearanceId
- selectedCompositionId
- selectedPrefabId

Wichtig:

roleId
→ Keystone-interne Mod-Rolle.

hytaleRole
→ echte Hytale Engine-Role unter Server/NPC/Roles/.

npcId
→ eindeutige konkrete NPC-Instanz.

entityUuid
→ stabile Entity-Identität, falls vorhanden.

entityRef
→ nur Runtime, niemals persistente Wahrheit.

Verboten:

- keine dynamischen KeystoneNPC_<npcId>_<roleId>_Role über Hytale roleName
- kein setRoleName("KeystoneNPC_...")
- keine Hytale Role-Datei als Keystone-Instanzdaten missbrauchen
- keine NPC-Identität über npcName allein
- keine doppelte Wahrheit zwischen Runtime und Persistence

============================================================
5. LIVE-ENTITY-GRUNDREGEL
============================================================

Ohne gültige EntityRef ist ein NPC nicht live.

Dann dürfen nur laufen:

- Relink
- Recovery
- Status / Debug
- sichere Admin-Checks

Dann dürfen NICHT laufen:

- Routine
- Navigation
- Action
- Door-Logik
- Movement-Fallback
- setTarget
- updateEntityPosition
- Auto-Respawn ohne finalen Precheck

EntityRef ist niemals persistente Wahrheit.

Persistente Wahrheit sind:

- npcId
- entityUuid
- worldId / worldKey
- roleId
- state
- markerAssignments
- lastKnownPosition
- structureInstanceId
- slotId
- selectedAppearanceId
- selectedCompositionId
- selectedPrefabId

Runtime-only sind:

- EntityRef
- Entity object
- navigation handle
- active action handle
- door interaction state
- cooldown maps
- runtime caches
- Hytale Runtime Handles

Wichtig:

NpcEntityStatus beweist nicht allein, dass eine Live-Entity gültig ist.
NpcState beweist nicht, dass eine Live-Entity gültig ist.
Die echte Runtime-Prüfung passiert über RuntimeNpc.hasLiveEntity() und später LiveEntityGate.

Ein persistierter Status allein darf niemals Tick-, Routine-, Navigation- oder Action-Logik erlauben.

============================================================
6. STATE / PERSISTENZ
============================================================

state.json speichert konkrete Instanzen, nicht Definitionen und nicht Runtime.

Persistiert werden sollen später:

NpcRecord:

- npcId
- roleId
- entityUuid
- entityStatus
- worldId / worldKey
- structureInstanceId
- slotId
- currentPosition
- markerAssignments
- selectedAppearanceId
- selectedCompositionId
- selectedPrefabId

StructureInstance:

- structureInstanceId
- prefabId
- worldId / worldKey
- position
- rotation
- markerInstances
- npcSlots
- protectionState
- damageState

Nicht speichern:

- entityRef
- Entity object
- aktive Navigation
- Door Runtime
- Action Runtime
- Cooldown Maps
- Hytale Runtime Handles
- RuntimeNpc

Grundsatz:

Runtime darf handeln.
Persistence darf erinnern.
Persistence darf niemals unsichere Runtime-Zustände als Wahrheit speichern.

Phase-2-State-Regeln:

- Load-Failure überschreibt keine state.json.
- Partial-Load wird nicht als Erfolg behandelt.
- Save-Failure gibt false / failed Result.
- Dirty wird nur nach echtem Save gelöscht.
- Pro Welt gibt es eigenen PersistedWorldState.
- Kaputtes JSON darf niemals automatisch zu leerem Default-State werden.
- Fehlende state.json ist kein Load-Failure, darf aber nicht automatisch ungefragt gespeichert werden.
- Save ist nur für geladene Welten erlaubt.
- Backup-Fehler blockiert Save, wenn Backup nötig ist.
- Raw JSON darf nicht langfristig die geladene Wahrheit sein.
- WorldStateStore soll PersistedWorldState pro Welt halten.

============================================================
7. MARKER-ZIELBILD
============================================================

Marker-v2 ist noch nicht Runtime-Scope, aber das Zielbild gilt schon.

Definitionen benutzen logische Namen:

- bed
- work
- door
- kitchen
- patrol_a
- wood_storage
- table
- garden

state.json / Runtime benutzt konkrete Marker-IDs:

- marker_house_007_bed_main
- marker_house_007_work_01
- marker_house_007_door
- marker_house_007_kitchen

Wichtig:

Nicht alle Holzfäller benutzen denselben work-Marker.
Jeder konkrete NPC bekommt eigene Marker-IDs aus seiner konkreten Hausinstanz.

requiredMarkers aus JSON:

- bed
- work
- door

markerAssignments in state.json:

- bed -> marker_house_007_bed_main
- work -> marker_house_007_work_01
- door -> marker_house_007_door

Marker-Regeln:

- Marker-ID muss existieren.
- MarkerType muss passen.
- worldKey muss passen.
- Marker aus anderer Welt blockieren.
- Read-only Resolve mutiert nie.
- Kein Auto-Reassign beim Load/Tick.
- Fehlender Marker pausiert, repariert aber nicht heimlich.
- Marker-v2 darf nicht nebenbei halb in Phase 2 eingebaut werden.

============================================================
8. PREFAB / STRUCTURE-ZIELBILD
============================================================

Friendly-NPCs sollen später oft an Prefabs gebunden sein.

Ein Prefab kann sein:

- Holzfällerhaus
- Schmiede
- Bauernhof
- Bürgerhaus
- Marktstand
- Dorfzentrum
- Wachposten
- Straßenlager

Das Prefab enthält:

- Marker
- NPC-Slots
- erlaubte Compositions
- Protection-Policy

Nicht die Rolle besitzt direkt ein Haus.

Besser:

lumberjack_house prefab
→ marker
→ npcSlots
→ allowed compositions

Wichtig:

Wenn es 20 gleiche Holzfällerhäuser gibt, braucht jedes Haus eine eigene StructureInstance:

- house_001
- house_002
- house_003
- house_007

Jede Instanz hat eigene Marker.

NPCs in diesem Haus speichern nur Marker-IDs dieser Instanz.

Wenn Struktur beschädigt ist:

Nicht:

- Marker heimlich löschen
- NPC blind respawnen
- neue Marker automatisch suchen

Sondern:

- StructureInstance als beschädigt markieren
- NPC pausieren oder blockieren
- Admin-/Repair-System später reagieren lassen

============================================================
9. JSON-PROFILE UND GENERISCHE SYSTEME
============================================================

JSON bestimmt Verhalten.

Java soll generisch bleiben.

Wichtige Profilfelder:

roleId
→ Keystone-interne Rolle.

hytaleRole
→ echte Hytale Engine-Role.

appearancePool
→ zufällige Optik-Varianten.

routineProfile
→ Tagesablauf.

movementProfile
→ Bewegungswerte.

navigationProfile
→ Navigationslogik.

combatProfile
→ Kampfverhalten.

persistenceProfile
→ Speicher-/Lebensdauer-Regeln.

spawnProfile
→ Spawn-Art.

prefabBinding
→ Verbindung zu Prefab oder StructureInstance.

requiredMarkers
→ logische Marker, die der NPC braucht.

actions
→ Aktionen wie sleep, eat, chop_wood, cook.

animations
→ Animationen für Actions.

sounds
→ Sounds für Actions.

AIProfile
→ spätere Entscheidungslogik.

ManaProfile
→ spätere Mana-Regeln.

MagicProfile
→ spätere Magie-Regeln.

Regel:

Keine eigenen Java-Features für citizen, worker, hostile nur deshalb, weil es andere NPC-Typen sind.

Citizen, Worker, Hostile, Magier usw. werden über JSON-Profile beschrieben.

Eigene Java-Features nur, wenn ein System eigenen State oder eigene Speziallogik braucht, z. B.:

- Quest
- Raid
- Worldgen v2
- Economy
- Diplomatie
- große AI-Simulation

============================================================
10. ROUTINE / ACTION / NAVIGATION
============================================================

Routine und Action kommen später.

Noch nicht in Phase 2 bauen.

Zielbild:

Routine sagt:

- gehe zu Marker
- führe Action aus

Action-Profil sagt:

- animation
- sound
- duration
- loop / interval

Java soll nicht hart kodieren:

Wenn Marker kitchen, dann spiele cooking_loop.

Besser:

Routine sagt:
gehe zu kitchen und führe cook aus.

Action-Profil sagt:
cook nutzt cooking_loop und pot_stir.

Navigation bleibt generisch.

navigationProfile bestimmt die Logik:

- MARKER_ROUTINE
- ROAD_GRAPH
- WANDER_RADIUS
- CHASE_TARGET
- PATROL_MARKERS

Wichtig:

- keine Navigation ohne gültige EntityRef
- keine Action ohne LiveEntityGate
- fehlender Marker pausiert
- fehlender Marker repariert nicht heimlich
- keine Action-Runtime persistieren
- keine Hytale Animation/Sound API erraten
- keine eigene Fake-Pathfinding-Engine

============================================================
11. WORLDGEN V2
============================================================

Worldgen v2 soll nicht direkt NPC-Logik machen.

Sicherer Ablauf später:

1. Region / Chunk wird betrachtet.
2. Worldgen-Regel entscheidet: Hier darf ein Dorf entstehen.
3. PlacementPlanner wählt Dorf-Layout.
4. Prefab-Pool wählt einzelne Gebäude.
5. Straßen werden logisch verbunden.
6. StructureInstances werden erzeugt.
7. Marker aus Prefabs werden registriert.
8. SpawnCompositions werden gewählt.
9. NPC-Records werden erstellt.
10. NPC-Entities werden gespawnt, wenn sicher.
11. Routine startet erst bei gültiger LiveEntity.

Wichtig:

Worldgen v2 platziert Structures.
NpcSpawn erstellt NPCs.
MarkerAssignment weist Marker zu.
StateStore speichert Instanzen.

Diese Systeme dürfen nicht vermischt werden.

============================================================
12. SAFE-BY-DEFAULT COMMANDS
============================================================

Commands kommen später.

Wenn Commands gebaut werden, müssen sie safe-by-default sein.

Gefährliche Aktionen brauchen --force:

- Löschen
- Purge
- Ersatzspawn
- Cleanup
- Überschreiben
- Massenänderung

Dry-run darf niemals Welt oder State verändern.

Bei Fehler:

- klare Chatmeldung
- keine Aktion ausführen

Commands dürfen nicht:

- Runtime-Safety umgehen
- Entity ohne Record erzeugen
- Record ohne sichere Entity-Entfernung löschen
- Load-Failure durch Save überschreiben
- Marker heimlich reparieren
- fremde Marker übernehmen

============================================================
13. LOGGING
============================================================

Kein Tick-Spam.

Logs nur bei:

- Statuswechsel
- Fehler
- Admin-Command
- Recovery
- wichtigen Entscheidungen

Relink-/Missing-/Respawn-Logs brauchen Cooldown.

Erlaubt:

- logOncePerNpc(eventKey, npcId)
- logCooldown(eventKey, npcId, 5000ms)

============================================================
14. ALLGEMEINE SAFETY-CHECKLISTE
============================================================

Nach jeder Änderung prüfen:

- Kann hier null reinkommen?
- Kann Hytale hier null zurückgeben?
- Kann ein Index -1 sein?
- Kann ein String leer sein?
- Kann ein JSON-Feld fehlen?
- Kann ein Command falsche Argumente bekommen?
- Kann saveState fehlschlagen?
- Kann eine Entity erzeugt werden, ohne Record?
- Kann ein Record als live behandelt werden, ohne gültige Entity?
- Kann eine echte Entity versehentlich gelöscht werden?
- Kann ein zweiter NPC für denselben Record entstehen?
- Kann ein System bei Relog/Chunk-Reload kaputtgehen?
- Kann ein Debug-/Cleanup-Command zu destruktiv sein?
- Verletzt die Änderung eine Regel aus safety/*.md?
- Muss eine Safety-Datei aktualisiert werden?
- Gibt es einen Widerspruch zwischen Safety-Dateien?
- Gibt es einen Widerspruch zwischen Planung, Code und Zielzustand?
- Wird Phase-2-Scope überschritten?
- Wird Marker-v2 oder Spawn versehentlich zu früh eingebaut?

Wenn unklar:

- nicht löschen
- nicht spawnen
- nicht relinken
- nicht überschreiben
- nur warnen/loggen

============================================================
15. TEST-GATE
============================================================

Nach Codeänderungen mindestens prüfen:

mvn -q -DskipTests test-compile

Wenn Spawn / Relink / Restart / Persistence / JSON-Hierarchie geändert wurde:

- passende Tests aus safety/*.md prüfen
- relevante Restart-/Negativtests nennen
- bei geänderter Safety-Regel die Safety-Datei aktualisieren

Für Phase 2 besonders prüfen:

- StateSaveResult.failed("x").success() == false
- StateSaveResult.success("x").success() == true
- null/blank message wird blockiert
- writeAtomic(null, "...") gibt false
- writeAtomic(file, null) gibt false
- ungültiges JSON wird blockiert
- leeres JSON wird blockiert
- fehlende version wird blockiert
- fehlende npcs wird blockiert
- kaputtes JSON erzeugt keinen leeren PersistedWorldState
- Load-Failure überschreibt keine state.json
- Save-Failure gibt false / failed Result
- Dirty bleibt true nach Save-Failure
- Dirty wird nur nach erfolgreichem saveStateSafely false

============================================================
16. ABSCHLUSSBERICHT NACH JEDER UMSETZUNG
============================================================

Nach jeder Umsetzung ausgeben:

- Geänderte Dateien
- Welche planning/*.md geprüft wurden
- Welche safety/*.md geprüft wurden
- Ob Regelkonflikte gefunden wurden
- Welche Safety-Checks eingebaut wurden
- Welche Nullchecks ergänzt wurden
- Welche Failchecks ergänzt wurden
- Welche Rollback-Pfade existieren
- Welche Commands safe-by-default sind
- Welche --force-Pfade existieren
- Welche Tests erfolgreich waren
- Welche Restgefahren bleiben
- Ob eine safety/*.md aktualisiert werden musste
- Ob eine planning/*.md aktualisiert werden musste
- Ob der aktuelle Phasen-Scope eingehalten wurde

Wenn Regelkonflikt gefunden:

REGELKONFLIKT GEFUNDEN
keine Umsetzung durchgeführt
Nutzerentscheidung nötig

============================================================
17. AKTUELLER PHASE-2-AGENT-FOKUS
============================================================

Wenn der Nutzer keinen anderen Step nennt, gilt aktuell:

Phase 2 — State-System stabilisieren.

Nicht eigenständig zu Phase 3+ springen.

Sichere Step-Reihenfolge:

Step 2.0 — Phase-2-Precheck
Step 2.1 — StateSaveResult härten
Step 2.2 — StatePathResolver final als Skeleton absichern
Step 2.3 — StateFileIO härten
Step 2.4 — StateBackupStore härten
Step 2.5 — StateJsonCodec auf PersistedWorldState vorbereiten
Step 2.6 — WorldStateStore von raw JSON auf PersistedWorldState umstellen
Step 2.7 — NpcStateStore Dirty-/Load-/Save-Verhalten prüfen
Step 2.8 — Phase-2-Failcheck-Review
Step 2.9 — Compile-Gate

Arbeitsregel:

Immer nur einen Step umsetzen.
Danach Review.
Bei FAIL nur diesen Step fixen.
Erst nach PASS weiter.

Phase 2 darf NICHT enthalten:

- NpcManager.restoreRecords(...)
- Bootstrap-Verbindung
- echte Welt-Erkennung
- WorldKey-System ausbauen
- ChunkGate
- Marker-v2-Assignment
- MarkerCleanup
- StructureInstance
- SpawnResult
- RelinkResult
- Runtime Tick
- Commands
- Hytale EntityRef
- Hytale Entity-Objekte
- echte API-Recherche für Spawn

ENDE