# Dateiliste mit Kurzbeschreibung (aktuell)

Stand: 2026-05-13.
Hinweis: Build-Output unter target wird absichtlich nicht dokumentiert.
Hinweis: Diese Datei ist eine orientierende Uebersicht fuer Entwickler und keine vollstaendige API-Referenz.

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
- 2026-05-13_Block-Logic-Error-Report.md: Fehleranalyse/Review-Report.
- feature-plans.md: Sammeldatei fuer Feature-Planung.
- file-index.md: Diese Dateiuebersicht.
- json_plan.md: JSON-Struktur-/Migrationsnotizen.
- todos.md: Laufende Aufgaben und Notizen.

## docs/errors
- critical_fixes.md: Kritische Fixes und Sofortmassnahmen.
- errors_fixes.md: Fehler/Fix-Sammlung.
- marker-fixes.md: Marker-bezogene Fehler und Fixes.
- worldID_fix.md: WorldId-bezogene Fix-Notizen.

## docs/feature_plans
- check_legacy_code_marker_v2.md: Legacy-Code-Pruefplan fuer Marker-v2.
- JSON_structure_feature.md: Plan fuer JSON-Struktur-Feature.
- marker_v2.md: Marker-v2-Planungsdokument.

## docs/patch_reports
- 2026-05-13_01-21_Restart-Relink-Respawn-Safety-Patch.md: Safety-Patchreport Restart/Relink/Respawn.
- 2026-05-13_04-22_State-Load-Marker-Position-Observability-Safety-Patch.md: Safety-Patchreport Load/Marker-Position.
- 2026-05-13_04-50_Marker-State-Reconcile-Safety-Patch.md: Safety-Patchreport Marker-Reconcile.
- 2026-05-13_06-48_Safety-Docs-Marker-Resolver-Allowlist-Update.md: Safety-Doku-Abgleich Marker-Resolver.

## docs/prompts
- find_logic_error.md: Promptvorlage fuer Logikfehler-Suche.
- PlanAgentPrompt.md: Promptvorlage fuer Plan-/Agent-Workflow.

## docs/safety
- feature_safety_blueprint.md: Blueprint fuer neue Safety-Kontrolldateien.
- json_hierarchy.md: Validierte Safety-Baseline fuer JSON-Hierarchie.
- npc_restart_relink_control.md: Validierte Safety-Baseline fuer Restart/Relink.

## libs
- HytaleServer.jar: Lokale Server-API/Runtime als Build-Library.

## src/main/java/keystone/npc
- KeystoneNpcPlugin.java: Plugin-Entrypoint (Setup, Events, Save/Load, Commands).

## src/main/java/keystone/npc/actions
- ActionDefinition.java: Definition einzelner NPC-Aktionen.
- ActionProfile.java: Konfiguration fuer Action-Handling.
- ActionRunner.java: Ausfuehrungslogik fuer Aktionen.

## src/main/java/keystone/npc/combat
- CombatProfile.java: Basisprofil fuer Combat-bezogene Optionen.

## src/main/java/keystone/npc/commands
- KeystoneNpcCommands.java: Root-Commandstruktur fuer /knpc.
- NpcCommandRegistrar.java: Registriert die Command-Collection.

## src/main/java/keystone/npc/commands/admin
- NpcClearCommand.java: Loescht alle NPC-Slots.
- NpcCleanupOrphansCommand.java: Cleanup-Command fuer verwaiste Kandidaten mit Schutzlogik.
- NpcListCommand.java: Listet NPC-Slots.
- NpcRemoveCommand.java: Entfernt einen NPC per Index.
- NpcRespawnMissingCommand.java: Prueft/respawnt fehlende NPCs (inkl. Dry-run/Force-Pfaden).

## src/main/java/keystone/npc/commands/debug
- NpcStatusCommand.java: Gibt Marker-/NPC-Status aus.

## src/main/java/keystone/npc/commands/marker
- MarkerClearCommand.java: Setzt Marker zurueck.
- MarkerCommandGroup.java: Gruppiert Marker-Subcommands.
- MarkerSetCommand.java: Setzt Marker und triggert sichere Reroute-Pfade.

## src/main/java/keystone/npc/commands/spawn
- SpawnNpcCommand.java: Spawn-Command mit Rollen- und Markerpruefung.

## src/main/java/keystone/npc/debug
- NpcDebugSupport.java: Debug-Helfer fuer Status-/Marker-Ausgaben.
- RequiredMarkerStatus.java: Struktur fuer required-marker Diagnosezustand.

## src/main/java/keystone/npc/definition
- EffectiveNpcDefinition.java: Effektive zusammengefuehrte Definitionen.
- NpcAppearanceDefinition.java: Appearance-bezogene Felder.
- NpcAttitudeDefinition.java: Attitude-/Verhaltensfelder.
- NpcDebugDefinition.java: Debug-relevante Definitionswerte.
- NpcDefinition.java: Kernmodell einer NPC-Definition.
- NpcDefinitionRegistry.java: Laden und Verwalten von NPC-Definitionen.
- NpcDropsDefinition.java: DropList/Drop-Verhalten.
- NpcInstructionDefinition.java: Bewegungs-/Instruktionsdaten.
- NpcMotionControllerDefinition.java: MotionController-Werte.
- NpcProfileRefs.java: Referenzsammlung fuer Skill/Movement/Navigation/... Profile.
- NpcStatsDefinition.java: Werte wie MaxHealth.
- NpcTemplateResolver.java: Aufloesung von Varianten/Template-Referenzen.

## src/main/java/keystone/npc/domain
- NpcEntityStatus.java: Entity-Lifecycle-Status (ACTIVE, NEEDS_RELINK, ...).
- NpcRecord.java: Laufzeit- und Persistenzmodell eines NPC-Slots.
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
- MarkerRegistry.java: Marker-Verwaltung und aktive Marker.
- MarkerRingTraversal.java: Interne deterministische Ring-Iteration.
- MarkerType.java: Marker-Typen (BED, DOOR, CHEST, FOOD, WORK, CHILL).
- RequiredMarkerResolver.java: Loest requiredMarkers je Rolle auf.
- Vec3.java: Einfacher persistenter 3D-Vektor.
- WorldId.java: Welt-Identifier als Wertobjekt.

## src/main/java/keystone/npc/movement
- InstructionDefinition.java: Bewegungsinstruktionsmodell.
- MotionControllerDefinition.java: Modell fuer Motion-Controller.
- MovementProfile.java: Laufzeitprofil fuer Bewegung.

## src/main/java/keystone/npc/navigation
- EngineNavigationController.java: Adapter zur Engine-Navigation.
- NavigationTarget.java: Zustand einer aktiven Navigation inkl. Zielidentitaet.
- NpcNavigation.java: Hilfslogik fuer Navigationszeiten.
- NpcNavigationProfile.java: Konfigurationsprofil fuer Navigationsverhalten.

## src/main/java/keystone/npc/persistence
- ActiveMarkerIdMapper.java: Mapping ActiveMarker <-> persistiertes JSON.
- JsonFileStateStore.java: Laden/Speichern von State als JSON.
- JsonPersistedModels.java: Persistenz-Records fuer JSON-Struktur.
- PluginState.java: Aggregierter geladener Plugin-State.
- StateStore.java: Interface fuer Persistenzimplementierungen.

## src/main/java/keystone/npc/persistence/profile
- PersistenceProfile.java: Persistenznahe Profilwerte (inkl. Respawn-Gates).

## src/main/java/keystone/npc/recovery
- RespawnPolicyConfig.java: Konfiguriert Recovery/Respawn-Entscheidungen.
- RespawnRecoveryService.java: Respawn-Retry- und Recovery-Logik.

## src/main/java/keystone/npc/relink
- RelinkSupport.java: Hilfen fuer Distanz- und Ref-Vergleiche.
- RelinkWorkflowService.java: UUID-/Anchor-Relink und Dedupe-Workflow.

## src/main/java/keystone/npc/roles
- RoleDefinition.java: Rollenobjekt inkl. Marker-Anforderungen.
- RoleDefinitionRegistry.java: Laden und Lookup von Rollen.

## src/main/java/keystone/npc/routine
- NpcRoutineRunner.java: Zentrale Routine-Orchestrierung.
- NpcTickSystem.java: ECS-Tick-Bridge zur Routine.
- RoutineDefinition.java: Datenmodell fuer Routine-Definitionen.
- RoutineDefinitionRegistry.java: Laden und Lookup von Routinen.
- RoutineEntry.java: Einzelne Routineneintraege.
- RoutineRunner.java: Laufzeitlogik fuer Routinen.

## src/main/java/keystone/npc/routine/entity
- EntitySyncService.java: Sync zwischen Entity-Transform und NPC-State.

## src/main/java/keystone/npc/routine/marker
- IdleMarkerService.java: Idle-Marker-Autoritaet und Restore-Ausrichtung.
- MarkerResolver.java: Marker-Aufloesung mit read-only und mutierenden Pfaden.

## src/main/java/keystone/npc/routine/pathfinding
- NavigationRuntimeService.java: Runtime-Navigation (Tick/Finish/Maintenance).
- PathfindingSupport.java: Geometrie- und Pfad-Helfer.

## src/main/java/keystone/npc/routine/state
- NpcTickPipeline.java: Voller NPC-Update-Ablauf je Tick.
- StateTargetingService.java: Zielzustand, DesiredTarget und Start/Immediate-Navigation.

## src/main/java/keystone/npc/skills
- NpcSkill.java: Enum der unterstuetzten Skills.
- SkillChecks.java: Hilfspruefungen fuer Skills.
- SkillProfile.java: Konfiguration fuer Skill-Auswertung.
- SkillResolver.java: Ermittelt effektive NPC-Skills.
- SkillSet.java: Datenstruktur fuer aktive Skills.

## src/main/java/keystone/npc/spawn
- SpawnProfile.java: Spawn-bezogene Profilwerte.

## src/main/resources
- manifest.json: Mod-Manifest (Metadaten, Main, Version-Pinning).

## src/main/resources/Server/NPC/Roles
- Lumberjack.json: Konkrete Hytale-Role fuer Lumberjack.
- Lumberjack_Oldman.json: Hytale-Role Variante Oldman.
- Lumberjack_Oldwife.json: Hytale-Role Variante Oldwife.
- Lumberjack_Wife.json: Hytale-Role Variante Wife.
- Template_Human_Friendly.json: Basis-Template fuer freundliche Human-Roles.
- Test.json: Test-Role fuer Validierung und lokale Tests.
