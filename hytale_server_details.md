# Hytale Server `src` Struktur – Modding-Landkarte

> Erstellt aus `/mnt/data/hytale_server.zip`. Ziel: einfache Orientierung für Hytale-Modding-Anfänger.

**Stand:** 2026-05-09  
**Projekt:** `hytale-server`  
**Java-Dateien:** 5752  
**Ordner unter `src/main/java`:** 1001

## 1. Kurzfazit für dich

Der Code sieht stark nach einem **Plugin-/Mod-System mit ECS-Komponenten** aus. Für dich als Modder sind zuerst diese Bereiche wichtig:

- `server/core/plugin` – wie ein Plugin geladen wird.
- `common/plugin` – wie `manifest.json` verstanden wird.
- `server/core/command` – Vorlage für eigene Commands.
- `server/npc` – NPC-Logik, Rollen, Bewegung, Blackboard, State Machine.
- `server/spawning` – Spawnmarker und Spawnjobs.
- `server/core/universe` – Welt, Chunks, Storage, Spieler und Positionen.
- `assetstore`, `codec`, `server/core/asset` – JSON/Assets/AssetPacks laden.
- `builtin/*` – eingebaute Beispiel-Plugins, an denen du Struktur lernen kannst.

## 2. Was ich zusätzlich in diese Datei eingetragen habe

- Grobe Projektübersicht.
- Wichtige Einstiegspunkte für Mods.
- Modding-Relevanz pro wichtigem Ordner.
- Vollständiger Directory-Anhang für alle Ordner unter `src/main/java`.
- Suchbegriffe für VSCode.
- Empfehlung, welche Ordner du zuerst anschauen solltest.

## 3. Grober Ordnerbaum

```text
java/ (5752 .java)
└── com/ (5752 .java)
    └── hypixel/ (5752 .java)
        ├── fastutil/ (66 .java)
        │   ├── bytes/ (9 .java)
        │   ├── chars/ (9 .java)
        │   ├── doubles/ (9 .java)
        │   ├── floats/ (9 .java)
        │   ├── ints/ (9 .java)
        │   ├── longs/ (9 .java)
        │   ├── shorts/ (9 .java)
        │   └── util/ (2 .java)
        └── hytale/ (5686 .java)
            ├── assetstore/ (40 .java)
            ├── builtin/ (1475 .java)
            ├── codec/ (127 .java)
            ├── common/ (49 .java)
            ├── component/ (92 .java)
            ├── event/ (15 .java)
            ├── function/ (37 .java)
            ├── lib/ (6 .java)
            ├── logger/ (11 .java)
            ├── math/ (74 .java)
            ├── metrics/ (10 .java)
            ├── plugin/ (3 .java)
            ├── procedurallib/ (151 .java)
            ├── protocol/ (885 .java)
            ├── registry/ (2 .java)
            ├── server/ (2694 .java)
            ├── sneakythrow/ (10 .java)
            ├── storage/ (2 .java)
            └── unsafe/ (1 .java)
```

## 4. Top-Level-Pakete unter `com/hypixel/hytale`

### `com/hypixel/hytale/assetstore/`

- **Java-Dateien direkt:** 16
- **Java-Dateien gesamt:** 40
- **Beispiele:** AssetConstants.java, AssetExtraInfo.java, AssetHolder.java, AssetKeyValidator.java, AssetLoadResult.java, AssetMap.java, AssetPack.java, AssetReferences.java …
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/builtin/`

- **Java-Dateien direkt:** 0
- **Java-Dateien gesamt:** 1475
- **Beispiele:** keine direkten .java Dateien
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/codec/`

- **Java-Dateien direkt:** 12
- **Java-Dateien gesamt:** 127
- **Beispiele:** Codec.java, DirectDecodeCodec.java, DocumentContainingCodec.java, EmptyExtraInfo.java, ExtraInfo.java, InheritCodec.java, KeyedCodec.java, PrimitiveCodec.java …
- **Was passiert hier?** Codec-System: Daten aus JSON/Assets lesen und schreiben.
- **Hilft beim Modding?** Wichtig, wenn du eigene config-/assetbasierte Daten sauber laden willst.

### `com/hypixel/hytale/common/`

- **Java-Dateien direkt:** 0
- **Java-Dateien gesamt:** 49
- **Beispiele:** keine direkten .java Dateien
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/component/`

- **Java-Dateien direkt:** 28
- **Java-Dateien gesamt:** 92
- **Beispiele:** AddReason.java, Archetype.java, ArchetypeChunk.java, CommandBuffer.java, Component.java, ComponentAccessor.java, ComponentRegistration.java, ComponentRegistry.java …
- **Was passiert hier?** ECS/Component-System: Daten hängen als Komponenten an Entities/Chunks/Systeme.
- **Hilft beim Modding?** Sehr wichtig: Hytale nutzt stark Component-Logik statt einfache Minecraft-Objekte.

### `com/hypixel/hytale/event/`

- **Java-Dateien direkt:** 15
- **Java-Dateien gesamt:** 15
- **Beispiele:** AsyncEventBusRegistry.java, EventBus.java, EventBusRegistry.java, EventPriority.java, EventRegistration.java, EventRegistry.java, IAsyncEvent.java, IBaseEvent.java …
- **Was passiert hier?** Event-System: Ereignisse registrieren und auslösen.
- **Hilft beim Modding?** Wichtig, um auf Server-Events zu reagieren, ohne überall Code zu patchen.

### `com/hypixel/hytale/function/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 37
- **Beispiele:** package-info.java
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/lib/`

- **Java-Dateien direkt:** 0
- **Java-Dateien gesamt:** 6
- **Beispiele:** keine direkten .java Dateien
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/logger/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 11
- **Beispiele:** HytaleLogger.java
- **Was passiert hier?** Logging-System.
- **Hilft beim Modding?** Wichtig für Debug-Ausgaben deiner Mod.

### `com/hypixel/hytale/math/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 74
- **Beispiele:** Axis.java, Range.java
- **Was passiert hier?** Mathe-Hilfen: Vektoren, Matrizen, Raycasts, Shapes, Hitdetection.
- **Hilft beim Modding?** Wichtig für Positionen, Blickrichtung, Marker und Entfernungen.

### `com/hypixel/hytale/metrics/`

- **Java-Dateien direkt:** 6
- **Java-Dateien gesamt:** 10
- **Beispiele:** ExecutorMetricsRegistry.java, InitStackThread.java, JVMMetrics.java, MetricProvider.java, MetricResults.java, MetricsRegistry.java
- **Was passiert hier?** Metriken/Performance-Beobachtung.
- **Hilft beim Modding?** Später nützlich, wenn du Serverlast messen willst.

### `com/hypixel/hytale/plugin/`

- **Java-Dateien direkt:** 0
- **Java-Dateien gesamt:** 3
- **Beispiele:** keine direkten .java Dateien
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/procedurallib/`

- **Java-Dateien direkt:** 5
- **Java-Dateien gesamt:** 151
- **Beispiele:** NoiseFunction.java, NoiseFunction2d.java, NoiseFunction3d.java, NoiseFunctionPair.java, NoiseType.java
- **Was passiert hier?** Prozedurale Hilfen: Noise, Zufall, Bedingungen, JSON-Logik.
- **Hilft beim Modding?** Wichtig für Worldgen und zufällige Platzierung.

### `com/hypixel/hytale/protocol/`

- **Java-Dateien direkt:** 426
- **Java-Dateien gesamt:** 885
- **Beispiele:** AOECircleSelector.java, AOECylinderSelector.java, AbilityEffects.java, AccumulationMode.java, ActiveAnimationsUpdate.java, AmbienceFX.java, AmbienceFXAltitude.java, AmbienceFXAmbientBed.java …
- **Was passiert hier?** Netzwerk-/Datenmodelle zwischen Server und Client.
- **Hilft beim Modding?** Eher fortgeschritten; nützlich zum Verstehen von Datenpaketen, aber nicht erster Einstieg.

### `com/hypixel/hytale/registry/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** Registration.java, Registry.java
- **Was passiert hier?** Registry-System: Dinge anmelden und später wiederfinden.
- **Hilft beim Modding?** Wichtig für Modding, weil vieles erst registriert werden muss.

### `com/hypixel/hytale/server/`

- **Java-Dateien direkt:** 0
- **Java-Dateien gesamt:** 2694
- **Beispiele:** keine direkten .java Dateien
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/sneakythrow/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 10
- **Beispiele:** SneakyThrow.java, ThrowableRunnable.java
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/storage/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** IndexedStorageFile.java, package-info.java
- **Was passiert hier?** Speicher-/Persistenz-Basis.
- **Hilft beim Modding?** Wichtig für gespeicherte NPCs, Marker und Claim-Daten.

### `com/hypixel/hytale/unsafe/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** UnsafeUtil.java
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

## 5. Wichtigste Serverbereiche

## `com/hypixel/hytale/server/core/`

### `com/hypixel/hytale/server/core/`

- **Java-Dateien direkt:** 7
- **Java-Dateien gesamt:** 1591
- **Beispiele:** Constants.java, HytaleServer.java, HytaleServerConfig.java, Message.java, NameMatching.java, Options.java, ShutdownReason.java
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/server/core/asset/`

- **Java-Dateien direkt:** 7
- **Java-Dateien gesamt:** 286
- **Beispiele:** AssetModule.java, AssetNotifications.java, AssetPackRegisterEvent.java, AssetPackUnregisterEvent.java, AssetRegistryLoader.java, HytaleAssetStore.java, LoadAssetEvent.java
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/server/core/auth/`

- **Java-Dateien direkt:** 15
- **Java-Dateien gesamt:** 20
- **Beispiele:** AuthConfig.java, AuthConfigGenerated.java, AuthCredentialStoreProvider.java, CertificateUtil.java, DefaultAuthCredentialStore.java, EncryptedAuthCredentialStore.java, EncryptedAuthCredentialStoreProvider.java, HttpResponseException.java …
- **Was passiert hier?** Login/Auth-System.
- **Hilft beim Modding?** Für dein Modding meist nicht zuerst wichtig.

### `com/hypixel/hytale/server/core/blocktype/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 2
- **Beispiele:** BlockTypeModule.java
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/server/core/client/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** ClientFeatureHandler.java
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/server/core/codec/`

- **Java-Dateien direkt:** 6
- **Java-Dateien gesamt:** 8
- **Beispiele:** BoolDoublePairCodec.java, LayerEntryCodec.java, PairCodec.java, ProtocolCodecs.java, ShapeCodecs.java, WeightedMapCodec.java
- **Was passiert hier?** Codec-System: Daten aus JSON/Assets lesen und schreiben.
- **Hilft beim Modding?** Wichtig, wenn du eigene config-/assetbasierte Daten sauber laden willst.

### `com/hypixel/hytale/server/core/command/`

- **Java-Dateien direkt:** 0
- **Java-Dateien gesamt:** 226
- **Beispiele:** keine direkten .java Dateien
- **Was passiert hier?** Command-System und fertige Server-/Spieler-/Debug-Commands.
- **Hilft beim Modding?** Sehr wichtig: Vorlage für eigene Admin-Commands wie /keystone marker oder /npc spawn.

### `com/hypixel/hytale/server/core/config/`

- **Java-Dateien direkt:** 7
- **Java-Dateien gesamt:** 7
- **Beispiele:** BackupConfig.java, ModConfig.java, RateLimitConfig.java, ServerWorldMapConfig.java, UpdateConfig.java, WorldMapConfig.java, WorldWorldMapConfig.java
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/server/core/console/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 3
- **Beispiele:** ConsoleModule.java, ConsoleSender.java
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/server/core/cosmetics/`

- **Java-Dateien direkt:** 15
- **Java-Dateien gesamt:** 16
- **Beispiele:** BodyType.java, CosmeticAssetValidator.java, CosmeticRegistry.java, CosmeticType.java, CosmeticsModule.java, Emote.java, EmoteAsset.java, EmoteAssetPacketGenerator.java …
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/server/core/entity/`

- **Java-Dateien direkt:** 16
- **Java-Dateien gesamt:** 68
- **Beispiele:** AnimationUtils.java, ChainSyncStorage.java, Entity.java, EntitySnapshot.java, EntityUtils.java, ExplosionConfig.java, ExplosionUtils.java, Frozen.java …
- **Was passiert hier?** Entity-System: Spieler, Kreaturen, Komponenten, Schaden, Movement, Stats.
- **Hilft beim Modding?** Wichtig für NPCs, Spielerinteraktion und eigene Entity-Logik.

### `com/hypixel/hytale/server/core/event/`

- **Java-Dateien direkt:** 0
- **Java-Dateien gesamt:** 36
- **Beispiele:** keine direkten .java Dateien
- **Was passiert hier?** Event-System: Ereignisse registrieren und auslösen.
- **Hilft beim Modding?** Wichtig, um auf Server-Events zu reagieren, ohne überall Code zu patchen.

### `com/hypixel/hytale/server/core/inventory/`

- **Java-Dateien direkt:** 9
- **Java-Dateien gesamt:** 47
- **Beispiele:** ActiveSlotInventoryComponent.java, Inventory.java, InventoryComponent.java, InventorySystems.java, InventoryUtils.java, ItemContext.java, ItemStack.java, MaterialQuantity.java …
- **Was passiert hier?** Inventar-/Item-System.
- **Hilft beim Modding?** Wichtig für Belohnungen, Kosten, Shop, Energy Crystals oder NPC-Handel.

### `com/hypixel/hytale/server/core/io/`

- **Java-Dateien direkt:** 7
- **Java-Dateien gesamt:** 39
- **Beispiele:** NetworkSerializable.java, NetworkSerializer.java, NetworkSerializers.java, PacketHandler.java, PacketStatsRecorderImpl.java, ProtocolVersion.java, ServerManager.java
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/server/core/liveconfig/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** LiveConfigModule.java, LiveConfigService.java, LiveConfigSnapshot.java
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/server/core/meta/`

- **Java-Dateien direkt:** 9
- **Java-Dateien gesamt:** 9
- **Beispiele:** AbstractMetaStore.java, ArrayMetaStore.java, DynamicMetaStore.java, IMetaRegistry.java, IMetaStore.java, IMetaStoreImpl.java, MetaKey.java, MetaRegistry.java …
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/server/core/modules/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 458
- **Beispiele:** LegacyModule.java
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/server/core/permissions/`

- **Java-Dateien direkt:** 4
- **Java-Dateien gesamt:** 18
- **Beispiele:** HytalePermissions.java, PermissionHolder.java, PermissionValidation.java, PermissionsModule.java
- **Was passiert hier?** Rechte-/Permission-System.
- **Hilft beim Modding?** Wichtig für Admin-Commands und Spielerrollen.

### `com/hypixel/hytale/server/core/plugin/`

- **Java-Dateien direkt:** 10
- **Java-Dateien gesamt:** 20
- **Beispiele:** JavaPlugin.java, JavaPluginInit.java, MissingPluginDependencyException.java, PluginBase.java, PluginClassLoader.java, PluginInit.java, PluginListPageManager.java, PluginManager.java …
- **Was passiert hier?** Plugin-System: Laden, Manifest, Lifecycle und Plugin-Registry.
- **Hilft beim Modding?** Sehr wichtig: hier lernst du, wie Mods/Plugins geladen und verwaltet werden.

### `com/hypixel/hytale/server/core/prefab/`

- **Java-Dateien direkt:** 7
- **Java-Dateien gesamt:** 30
- **Beispiele:** PrefabCopyableComponent.java, PrefabEntry.java, PrefabLoadException.java, PrefabRotation.java, PrefabSaveException.java, PrefabStore.java, PrefabWeights.java
- **Was passiert hier?** Prefab-System: platzierbare Strukturen/Objekte und Spawner.
- **Hilft beim Modding?** Wichtig für Häuser, Marker und Settlement-Strukturen.

### `com/hypixel/hytale/server/core/receiver/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** IEventTitleReceiver.java, IMessageReceiver.java, IPacketReceiver.java
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/server/core/registry/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** ClientFeatureRegistration.java, ClientFeatureRegistry.java
- **Was passiert hier?** Registry-System: Dinge anmelden und später wiederfinden.
- **Hilft beim Modding?** Wichtig für Modding, weil vieles erst registriert werden muss.

### `com/hypixel/hytale/server/core/schema/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** SchemaGenerator.java
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/server/core/task/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** TaskRegistration.java, TaskRegistry.java
- **Was passiert hier?** System-/Task-Code: wiederkehrende oder eventbasierte Verarbeitung.
- **Hilft beim Modding?** Wichtig für saubere Serverlogik ohne dauernde Welt-Scans.

### `com/hypixel/hytale/server/core/telemetry/`

- **Java-Dateien direkt:** 6
- **Java-Dateien gesamt:** 6
- **Beispiele:** TelemetryDataCollector.java, TelemetryJsonSerializer.java, TelemetryModule.java, TelemetryPackets.java, TelemetryService.java, TelemetryStorage.java
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/server/core/ui/`

- **Java-Dateien direkt:** 8
- **Java-Dateien gesamt:** 18
- **Beispiele:** Anchor.java, Area.java, DropdownEntryInfo.java, ItemGridSlot.java, LocalizableString.java, PatchStyle.java, Value.java, ValueCodec.java
- **Was passiert hier?** UI/Page-System für servergesteuerte Oberflächen.
- **Hilft beim Modding?** Wichtig für spätere Mod-Menüs, Admin-Seiten oder NPC-Dialoge.

### `com/hypixel/hytale/server/core/universe/`

- **Java-Dateien direkt:** 4
- **Java-Dateien gesamt:** 218
- **Beispiele:** PlayerRef.java, StorageManager.java, Universe.java, WorldLoadCancelledException.java
- **Was passiert hier?** Welt-/Universum-System: Worlds, Chunks, Storage, PlayerData und Welt-Events.
- **Hilft beim Modding?** Wichtig für Positionen, Marker, Persistenz und Weltzugriff.

### `com/hypixel/hytale/server/core/update/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 10
- **Beispiele:** UpdateModule.java, UpdateService.java
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/server/core/util/`

- **Java-Dateien direkt:** 19
- **Java-Dateien gesamt:** 27
- **Beispiele:** AssetUtil.java, BsonUtil.java, Config.java, ConsoleColorUtil.java, DumpUtil.java, EventTitleUtil.java, FillerBlockUtil.java, HashUtil.java …
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

## `com/hypixel/hytale/server/npc/`

### `com/hypixel/hytale/server/npc/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 774
- **Beispiele:** AllNPCsLoadedEvent.java, NPCPlugin.java
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/animations/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** NPCAnimationSlot.java
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/asset/`

- **Java-Dateien direkt:** 0
- **Java-Dateien gesamt:** 153
- **Beispiele:** keine direkten .java Dateien
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/blackboard/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 30
- **Beispiele:** Blackboard.java
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/commands/`

- **Java-Dateien direkt:** 24
- **Java-Dateien gesamt:** 24
- **Beispiele:** NPCAllCommand.java, NPCAppearanceCommand.java, NPCAttackCommand.java, NPCBenchmarkCommand.java, NPCBlackboardCommand.java, NPCCleanCommand.java, NPCCommand.java, NPCCommandUtils.java …
- **Was passiert hier?** Command-System und fertige Server-/Spieler-/Debug-Commands.
- **Hilft beim Modding?** Sehr wichtig: Vorlage für eigene Admin-Commands wie /keystone marker oder /npc spawn.

### `com/hypixel/hytale/server/npc/components/`

- **Java-Dateien direkt:** 7
- **Java-Dateien gesamt:** 17
- **Beispiele:** FailedSpawnComponent.java, SortBufferProviderResource.java, SpawnBeaconReference.java, SpawnMarkerReference.java, SpawnReference.java, StepComponent.java, Timers.java
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/config/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 3
- **Beispiele:** AttitudeGroup.java, ItemAttitudeGroup.java
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/corecomponents/`

- **Java-Dateien direkt:** 14
- **Java-Dateien gesamt:** 329
- **Beispiele:** ActionBase.java, ActionWithDelay.java, AnnotatedComponentBase.java, BlockTarget.java, BodyMotionBase.java, EntityFilterBase.java, HeadMotionBase.java, IEntityFilter.java …
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/decisionmaker/`

- **Java-Dateien direkt:** 0
- **Java-Dateien gesamt:** 24
- **Beispiele:** keine direkten .java Dateien
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/entities/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** NPCEntity.java, PathManager.java
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/instructions/`

- **Java-Dateien direkt:** 10
- **Java-Dateien gesamt:** 14
- **Beispiele:** Action.java, ActionList.java, BodyMotion.java, HeadMotion.java, Instruction.java, InstructionRandomized.java, Motion.java, NullSensor.java …
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/interactions/`

- **Java-Dateien direkt:** 5
- **Java-Dateien gesamt:** 5
- **Beispiele:** ContextualUseNPCInteraction.java, NPCInteractionSimulationHandler.java, SpawnNPCInteraction.java, SpawnNPCInteractionFailureTracker.java, UseNPCInteraction.java
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/metadata/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** CapturedNPCMetadata.java
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/movement/`

- **Java-Dateien direkt:** 8
- **Java-Dateien gesamt:** 29
- **Beispiele:** FlockMembershipType.java, FlockPlayerMembership.java, GroupSteeringAccumulator.java, MotionKind.java, MovementMode.java, MovementState.java, NavState.java, Steering.java
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/navigation/`

- **Java-Dateien direkt:** 12
- **Java-Dateien gesamt:** 12
- **Beispiele:** AStarBase.java, AStarDebugBase.java, AStarDebugWithTarget.java, AStarEvaluator.java, AStarNode.java, AStarNodePool.java, AStarNodePoolProvider.java, AStarNodePoolProviderSimple.java …
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/pages/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** EntitySpawnPage.java
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/path/`

- **Java-Dateien direkt:** 0
- **Java-Dateien gesamt:** 2
- **Beispiele:** keine direkten .java Dateien
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/role/`

- **Java-Dateien direkt:** 5
- **Java-Dateien gesamt:** 17
- **Beispiele:** Role.java, RoleDebugDisplay.java, RoleDebugFlags.java, RoleUtils.java, SpawnEffect.java
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/sensorinfo/`

- **Java-Dateien direkt:** 11
- **Java-Dateien gesamt:** 20
- **Beispiele:** CachedPositionProvider.java, EntityPositionProvider.java, ExtraInfoProvider.java, IPathProvider.java, IPositionProvider.java, InfoProvider.java, InfoProviderBase.java, PathProvider.java …
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/statetransition/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 4
- **Beispiele:** StateTransitionController.java
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/storage/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** AlarmStore.java, ParameterStore.java, PersistentParameter.java
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/systems/`

- **Java-Dateien direkt:** 26
- **Java-Dateien gesamt:** 26
- **Beispiele:** AvoidanceSystem.java, BalancingInitialisationSystem.java, BlackboardSystems.java, ComputeVelocitySystem.java, FailedSpawnSystem.java, MessageSupportSystem.java, MovementStatesSystem.java, NPCDamageSystems.java …
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/util/`

- **Java-Dateien direkt:** 22
- **Java-Dateien gesamt:** 51
- **Beispiele:** AimingData.java, AimingHelper.java, Alarm.java, AttitudeMemoryEntry.java, BlockPlacementHelper.java, ComponentInfo.java, DamageData.java, IAnnotatedComponent.java …
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/validators/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** NPCLoadTimeValidationHelper.java, NPCRoleValidator.java
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/valuestore/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** ValueStore.java, ValueStoreValidator.java
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

## `com/hypixel/hytale/server/spawning/`

### `com/hypixel/hytale/server/spawning/`

- **Java-Dateien direkt:** 7
- **Java-Dateien gesamt:** 82
- **Beispiele:** ISpawnable.java, ISpawnableWithModel.java, LoadedNPCEvent.java, SpawnRejection.java, SpawnTestResult.java, SpawningContext.java, SpawningPlugin.java
- **Was passiert hier?** Spawn-System: Spawnmarker, Spawnjobs, lokale/world Spawns und Suppression.
- **Hilft beim Modding?** Wichtig, wenn NPCs später automatisch erscheinen sollen.

### `com/hypixel/hytale/server/spawning/assets/`

- **Java-Dateien direkt:** 0
- **Java-Dateien gesamt:** 7
- **Beispiele:** keine direkten .java Dateien
- **Was passiert hier?** Spawn-System: Spawnmarker, Spawnjobs, lokale/world Spawns und Suppression.
- **Hilft beim Modding?** Wichtig, wenn NPCs später automatisch erscheinen sollen.

### `com/hypixel/hytale/server/spawning/beacons/`

- **Java-Dateien direkt:** 4
- **Java-Dateien gesamt:** 4
- **Beispiele:** InitialBeaconDelay.java, LegacySpawnBeaconEntity.java, SpawnBeacon.java, SpawnBeaconSystems.java
- **Was passiert hier?** Spawn-System: Spawnmarker, Spawnjobs, lokale/world Spawns und Suppression.
- **Hilft beim Modding?** Wichtig, wenn NPCs später automatisch erscheinen sollen.

### `com/hypixel/hytale/server/spawning/blockstates/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** SpawnMarkerBlock.java, SpawnMarkerBlockReference.java, SpawnMarkerBlockStateSystems.java
- **Was passiert hier?** Spawn-System: Spawnmarker, Spawnjobs, lokale/world Spawns und Suppression.
- **Hilft beim Modding?** Wichtig, wenn NPCs später automatisch erscheinen sollen.

### `com/hypixel/hytale/server/spawning/commands/`

- **Java-Dateien direkt:** 6
- **Java-Dateien gesamt:** 6
- **Beispiele:** SpawnBeaconsCommand.java, SpawnCommand.java, SpawnMarkersCommand.java, SpawnPopulateCommand.java, SpawnStatsCommand.java, SpawnSuppressionCommand.java
- **Was passiert hier?** Command-System und fertige Server-/Spieler-/Debug-Commands.
- **Hilft beim Modding?** Sehr wichtig: Vorlage für eigene Admin-Commands wie /keystone marker oder /npc spawn.

### `com/hypixel/hytale/server/spawning/controllers/`

- **Java-Dateien direkt:** 4
- **Java-Dateien gesamt:** 4
- **Beispiele:** BeaconSpawnController.java, SpawnController.java, SpawnControllerSystem.java, SpawnJobSystem.java
- **Was passiert hier?** Spawn-System: Spawnmarker, Spawnjobs, lokale/world Spawns und Suppression.
- **Hilft beim Modding?** Wichtig, wenn NPCs später automatisch erscheinen sollen.

### `com/hypixel/hytale/server/spawning/corecomponents/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 2
- **Beispiele:** ActionTriggerSpawnBeacon.java
- **Was passiert hier?** Spawn-System: Spawnmarker, Spawnjobs, lokale/world Spawns und Suppression.
- **Hilft beim Modding?** Wichtig, wenn NPCs später automatisch erscheinen sollen.

### `com/hypixel/hytale/server/spawning/interactions/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** TriggerSpawnMarkersInteraction.java
- **Was passiert hier?** Spawn-System: Spawnmarker, Spawnjobs, lokale/world Spawns und Suppression.
- **Hilft beim Modding?** Wichtig, wenn NPCs später automatisch erscheinen sollen.

### `com/hypixel/hytale/server/spawning/jobs/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** NPCBeaconSpawnJob.java, SpawnJob.java
- **Was passiert hier?** Spawn-System: Spawnmarker, Spawnjobs, lokale/world Spawns und Suppression.
- **Hilft beim Modding?** Wichtig, wenn NPCs später automatisch erscheinen sollen.

### `com/hypixel/hytale/server/spawning/local/`

- **Java-Dateien direkt:** 7
- **Java-Dateien gesamt:** 7
- **Beispiele:** LocalSpawnBeacon.java, LocalSpawnBeaconSystem.java, LocalSpawnController.java, LocalSpawnControllerSystem.java, LocalSpawnForceTriggerSystem.java, LocalSpawnSetupSystem.java, LocalSpawnState.java
- **Was passiert hier?** Spawn-System: Spawnmarker, Spawnjobs, lokale/world Spawns und Suppression.
- **Hilft beim Modding?** Wichtig, wenn NPCs später automatisch erscheinen sollen.

### `com/hypixel/hytale/server/spawning/managers/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** BeaconSpawnManager.java, SpawnManager.java
- **Was passiert hier?** Spawn-System: Spawnmarker, Spawnjobs, lokale/world Spawns und Suppression.
- **Hilft beim Modding?** Wichtig, wenn NPCs später automatisch erscheinen sollen.

### `com/hypixel/hytale/server/spawning/spawnmarkers/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** SpawnMarkerEntity.java, SpawnMarkerSystems.java
- **Was passiert hier?** Spawn-System: Spawnmarker, Spawnjobs, lokale/world Spawns und Suppression.
- **Hilft beim Modding?** Wichtig, wenn NPCs später automatisch erscheinen sollen.

### `com/hypixel/hytale/server/spawning/suppression/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 9
- **Beispiele:** SpawnSuppressorEntry.java, SuppressionSpanHelper.java
- **Was passiert hier?** Spawn-System: Spawnmarker, Spawnjobs, lokale/world Spawns und Suppression.
- **Hilft beim Modding?** Wichtig, wenn NPCs später automatisch erscheinen sollen.

### `com/hypixel/hytale/server/spawning/systems/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** BeaconSpatialSystem.java, LegacyBeaconSpatialSystem.java, SpawnMarkerSpatialSystem.java
- **Was passiert hier?** Spawn-System: Spawnmarker, Spawnjobs, lokale/world Spawns und Suppression.
- **Hilft beim Modding?** Wichtig, wenn NPCs später automatisch erscheinen sollen.

### `com/hypixel/hytale/server/spawning/util/`

- **Java-Dateien direkt:** 6
- **Java-Dateien gesamt:** 6
- **Beispiele:** ChunkColumnMask.java, FloodFillEntryPoolProviderSimple.java, FloodFillEntryPoolSimple.java, FloodFillPositionSelector.java, LightRangePredicate.java, RandomChunkColumnIterator.java
- **Was passiert hier?** Spawn-System: Spawnmarker, Spawnjobs, lokale/world Spawns und Suppression.
- **Hilft beim Modding?** Wichtig, wenn NPCs später automatisch erscheinen sollen.

### `com/hypixel/hytale/server/spawning/world/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 15
- **Beispiele:** ChunkEnvironmentSpawnData.java, WorldEnvironmentSpawnData.java, WorldNPCSpawnStat.java
- **Was passiert hier?** Spawn-System: Spawnmarker, Spawnjobs, lokale/world Spawns und Suppression.
- **Hilft beim Modding?** Wichtig, wenn NPCs später automatisch erscheinen sollen.

### `com/hypixel/hytale/server/spawning/wrappers/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** BeaconSpawnWrapper.java, SpawnWrapper.java
- **Was passiert hier?** Spawn-System: Spawnmarker, Spawnjobs, lokale/world Spawns und Suppression.
- **Hilft beim Modding?** Wichtig, wenn NPCs später automatisch erscheinen sollen.

## `com/hypixel/hytale/server/worldgen/`

### `com/hypixel/hytale/server/worldgen/`

- **Java-Dateien direkt:** 6
- **Java-Dateien gesamt:** 214
- **Beispiele:** BiomeDataSystem.java, ChunkGeneratorResource.java, HytaleWorldGenProvider.java, SeedStringResource.java, WorldGenConfig.java, WorldGenConstants.java
- **Was passiert hier?** Worldgen: Biome, Zonen, Prefabs, Caves, Container und Loader.
- **Hilft beim Modding?** Später wichtig für MVP B: NPC-Häuser automatisch in der Welt platzieren.

### `com/hypixel/hytale/server/worldgen/benchmark/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** ChunkWorldgenBenchmark.java
- **Was passiert hier?** Worldgen: Biome, Zonen, Prefabs, Caves, Container und Loader.
- **Hilft beim Modding?** Später wichtig für MVP B: NPC-Häuser automatisch in der Welt platzieren.

### `com/hypixel/hytale/server/worldgen/biome/`

- **Java-Dateien direkt:** 6
- **Java-Dateien gesamt:** 6
- **Beispiele:** Biome.java, BiomeInterpolation.java, BiomePatternGenerator.java, CustomBiome.java, CustomBiomeGenerator.java, TileBiome.java
- **Was passiert hier?** Worldgen: Biome, Zonen, Prefabs, Caves, Container und Loader.
- **Hilft beim Modding?** Später wichtig für MVP B: NPC-Häuser automatisch in der Welt platzieren.

### `com/hypixel/hytale/server/worldgen/cache/`

- **Java-Dateien direkt:** 7
- **Java-Dateien gesamt:** 7
- **Beispiele:** CaveGeneratorCache.java, ChunkGeneratorCache.java, CoordinateCache.java, CoreDataCacheEntry.java, ExtendedCoordinateCache.java, InterpolatedBiomeCountList.java, UniquePrefabCache.java
- **Was passiert hier?** Worldgen: Biome, Zonen, Prefabs, Caves, Container und Loader.
- **Hilft beim Modding?** Später wichtig für MVP B: NPC-Häuser automatisch in der Welt platzieren.

### `com/hypixel/hytale/server/worldgen/cave/`

- **Java-Dateien direkt:** 8
- **Java-Dateien gesamt:** 32
- **Beispiele:** Cave.java, CaveBiomeMaskFlags.java, CaveBlockPriorityModifier.java, CaveGenerator.java, CaveNodeType.java, CavePrefabPlacement.java, CaveType.java, CaveYawMode.java
- **Was passiert hier?** Worldgen: Biome, Zonen, Prefabs, Caves, Container und Loader.
- **Hilft beim Modding?** Später wichtig für MVP B: NPC-Häuser automatisch in der Welt platzieren.

### `com/hypixel/hytale/server/worldgen/chunk/`

- **Java-Dateien direkt:** 8
- **Java-Dateien gesamt:** 12
- **Beispiele:** BlockPriorityChunk.java, BlockPriorityModifier.java, ChunkGenerator.java, ChunkGeneratorExecution.java, HeightThresholdInterpolator.java, MaskProvider.java, ValidationUtil.java, ZoneBiomeResult.java
- **Was passiert hier?** Worldgen: Biome, Zonen, Prefabs, Caves, Container und Loader.
- **Hilft beim Modding?** Später wichtig für MVP B: NPC-Häuser automatisch in der Welt platzieren.

### `com/hypixel/hytale/server/worldgen/climate/`

- **Java-Dateien direkt:** 9
- **Java-Dateien gesamt:** 12
- **Beispiele:** ClimateColor.java, ClimateGraph.java, ClimateMaskProvider.java, ClimateNoise.java, ClimatePoint.java, ClimateSearch.java, ClimateType.java, DirectGrid.java …
- **Was passiert hier?** Worldgen: Biome, Zonen, Prefabs, Caves, Container und Loader.
- **Hilft beim Modding?** Später wichtig für MVP B: NPC-Häuser automatisch in der Welt platzieren.

### `com/hypixel/hytale/server/worldgen/container/`

- **Java-Dateien direkt:** 8
- **Java-Dateien gesamt:** 8
- **Beispiele:** CoverContainer.java, EnvironmentContainer.java, FadeContainer.java, LayerContainer.java, PrefabContainer.java, TintContainer.java, UniquePrefabContainer.java, WaterContainer.java
- **Was passiert hier?** Worldgen: Biome, Zonen, Prefabs, Caves, Container und Loader.
- **Hilft beim Modding?** Später wichtig für MVP B: NPC-Häuser automatisch in der Welt platzieren.

### `com/hypixel/hytale/server/worldgen/loader/`

- **Java-Dateien direkt:** 6
- **Java-Dateien gesamt:** 77
- **Beispiele:** AssetFileSystem.java, ChunkGeneratorJsonLoader.java, MaskProviderJsonLoader.java, WorldGenPrefabLoader.java, WorldGenPrefabSupplier.java, ZonesJsonLoader.java
- **Was passiert hier?** Worldgen: Biome, Zonen, Prefabs, Caves, Container und Loader.
- **Hilft beim Modding?** Später wichtig für MVP B: NPC-Häuser automatisch in der Welt platzieren.

### `com/hypixel/hytale/server/worldgen/map/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** GeneratorChunkWorldMap.java
- **Was passiert hier?** Worldgen: Biome, Zonen, Prefabs, Caves, Container und Loader.
- **Hilft beim Modding?** Später wichtig für MVP B: NPC-Häuser automatisch in der Welt platzieren.

### `com/hypixel/hytale/server/worldgen/prefab/`

- **Java-Dateien direkt:** 5
- **Java-Dateien gesamt:** 7
- **Beispiele:** PrefabCategory.java, PrefabLoadingCache.java, PrefabPasteUtil.java, PrefabPatternGenerator.java, PrefabStoreRoot.java
- **Was passiert hier?** Worldgen: Biome, Zonen, Prefabs, Caves, Container und Loader.
- **Hilft beim Modding?** Später wichtig für MVP B: NPC-Häuser automatisch in der Welt platzieren.

### `com/hypixel/hytale/server/worldgen/util/`

- **Java-Dateien direkt:** 11
- **Java-Dateien gesamt:** 34
- **Beispiele:** ArrayUtli.java, BlockArray.java, BlockFluidEntry.java, ChunkThreadPoolExecutor.java, ChunkWorkerThreadFactory.java, ConstantNoiseProperty.java, ListPool.java, LogUtil.java …
- **Was passiert hier?** Worldgen: Biome, Zonen, Prefabs, Caves, Container und Loader.
- **Hilft beim Modding?** Später wichtig für MVP B: NPC-Häuser automatisch in der Welt platzieren.

### `com/hypixel/hytale/server/worldgen/zone/`

- **Java-Dateien direkt:** 7
- **Java-Dateien gesamt:** 7
- **Beispiele:** Zone.java, ZoneColorMapping.java, ZoneDiscoveryConfig.java, ZoneGeneratorResult.java, ZonePatternGenerator.java, ZonePatternGeneratorCache.java, ZonePatternProvider.java
- **Was passiert hier?** Worldgen: Biome, Zonen, Prefabs, Caves, Container und Loader.
- **Hilft beim Modding?** Später wichtig für MVP B: NPC-Häuser automatisch in der Welt platzieren.

### `com/hypixel/hytale/server/worldgen/zoom/`

- **Java-Dateien direkt:** 4
- **Java-Dateien gesamt:** 4
- **Beispiele:** ExactZoom.java, FuzzyZoom.java, PixelDistanceProvider.java, PixelProvider.java
- **Was passiert hier?** Worldgen: Biome, Zonen, Prefabs, Caves, Container und Loader.
- **Hilft beim Modding?** Später wichtig für MVP B: NPC-Häuser automatisch in der Welt platzieren.

## `com/hypixel/hytale/builtin/`

### `com/hypixel/hytale/builtin/`

- **Java-Dateien direkt:** 0
- **Java-Dateien gesamt:** 1475
- **Beispiele:** keine direkten .java Dateien
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/adventure/`

- **Java-Dateien direkt:** 0
- **Java-Dateien gesamt:** 227
- **Beispiele:** keine direkten .java Dateien
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/ambience/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 5
- **Beispiele:** AmbiencePlugin.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/asseteditor/`

- **Java-Dateien direkt:** 10
- **Java-Dateien gesamt:** 30
- **Beispiele:** AssetEditorGamePacketHandler.java, AssetEditorPacketHandler.java, AssetEditorPlugin.java, AssetPath.java, AssetSpecificFunctionality.java, AssetTree.java, AssetTypeRegistry.java, EditorClient.java …
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/audio/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 11
- **Beispiele:** AudioPlugin.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/beds/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 20
- **Beispiele:** BedsPlugin.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/blockphysics/`

- **Java-Dateien direkt:** 5
- **Java-Dateien gesamt:** 5
- **Beispiele:** BlockPhysicsPlugin.java, BlockPhysicsSystems.java, BlockPhysicsUtil.java, PrefabBufferValidator.java, WorldValidationUtil.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/blockspawner/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 7
- **Beispiele:** BlockSpawnerEntry.java, BlockSpawnerPlugin.java, BlockSpawnerTable.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/blocktick/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 5
- **Beispiele:** BlockTickPlugin.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/buildertools/`

- **Java-Dateien direkt:** 12
- **Java-Dateien gesamt:** 203
- **Beispiele:** BlockColorIndex.java, BuilderToolsPacketHandler.java, BuilderToolsPlugin.java, BuilderToolsSystems.java, BuilderToolsUserData.java, BuilderToolsUserDataSystem.java, CopyCutSettings.java, EditOperation.java …
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/commandmacro/`

- **Java-Dateien direkt:** 9
- **Java-Dateien gesamt:** 9
- **Beispiele:** EchoCommand.java, MacroCommandBase.java, MacroCommandBuilder.java, MacroCommandCollection.java, MacroCommandParameter.java, MacroCommandPlugin.java, MacroCommandReplacement.java, MacroCommandTreeBuilder.java …
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/crafting/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 18
- **Beispiele:** BenchRecipeRegistry.java, CraftingPlugin.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/creativehub/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 7
- **Beispiele:** CreativeHubPlugin.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/crouchslide/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** CrouchSlidePlugin.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/deployables/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 16
- **Beispiele:** DeployablesPlugin.java, DeployablesUtils.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/fallingblocks/`

- **Java-Dateien direkt:** 5
- **Java-Dateien gesamt:** 5
- **Beispiele:** BreakFallingBlockImpact.java, FallingBlock.java, FallingBlockTickingSystem.java, FallingBlocksPlugin.java, PlaceFallingBlockImpact.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/fluid/`

- **Java-Dateien direkt:** 5
- **Java-Dateien gesamt:** 5
- **Beispiele:** DisabledFluidResource.java, FluidCommand.java, FluidPlugin.java, FluidState.java, FluidSystems.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/hytalegenerator/`

- **Java-Dateien direkt:** 13
- **Java-Dateien gesamt:** 573
- **Beispiele:** ArrayUtil.java, BlockMask.java, EntityPlacementData.java, FutureUtils.java, GridUtils.java, LoggerUtil.java, MaterialSet.java, PropRuntime.java …
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/instances/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 29
- **Beispiele:** InstanceValidator.java, InstancesPlugin.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/landiscovery/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** LANDiscoveryCommand.java, LANDiscoveryPlugin.java, LANDiscoveryThread.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/locate/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 10
- **Beispiele:** LocatePlugin.java, PrefabPatternSearchUtil.java, SpiralSearchUtil.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/mantling/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** MantlingPlugin.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/model/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 3
- **Beispiele:** ModelPlugin.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/mounts/`

- **Java-Dateien direkt:** 9
- **Java-Dateien gesamt:** 18
- **Beispiele:** BlockMountAPI.java, BlockMountComponent.java, MountGamePacketHandler.java, MountPlugin.java, MountSystems.java, MountedByComponent.java, MountedComponent.java, NPCMountComponent.java …
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/npccombatactionevaluator/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 27
- **Beispiele:** CombatActionEvaluatorSystems.java, NPCCombatActionEvaluatorPlugin.java, Positioning.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/npceditor/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** NPCEditorPlugin.java, NPCRoleAssetTypeHandler.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/parkour/`

- **Java-Dateien direkt:** 4
- **Java-Dateien gesamt:** 7
- **Beispiele:** ParkourCheckpoint.java, ParkourCheckpointSystems.java, ParkourCommand.java, ParkourPlugin.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/path/`

- **Java-Dateien direkt:** 6
- **Java-Dateien gesamt:** 29
- **Beispiele:** PathPlugin.java, PathSpatialSystem.java, PrefabPathCollection.java, PrefabPathSystems.java, WorldPathBuilder.java, WorldPathData.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/portals/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 53
- **Beispiele:** PortalsPlugin.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/randomtick/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 5
- **Beispiele:** RandomTick.java, RandomTickPlugin.java, RandomTickSystem.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/safetyroll/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** SafetyRollPlugin.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/sprintforce/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** SprintForcePlugin.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/tagset/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 4
- **Beispiele:** TagSet.java, TagSetLookupTable.java, TagSetPlugin.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/teleport/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 26
- **Beispiele:** TeleportPlugin.java, Warp.java, WarpListPage.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/triggervolumes/`

- **Java-Dateien direkt:** 4
- **Java-Dateien gesamt:** 68
- **Beispiele:** AssetSourceProvider.java, EntityTargetType.java, TriggerVolumeToolPacketHandler.java, TriggerVolumesPlugin.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/weather/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 8
- **Beispiele:** WeatherPlugin.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/worldgen/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 33
- **Beispiele:** WorldGenPlugin.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

## 6. Plugin-/Modding-Einstieg

### Was ein eigenes Plugin vermutlich braucht

- Eine `manifest.json` mit Feldern wie `Group`, `Name`, `Version`, `Main`, `ServerVersion`, `IncludesAssetPack`.
- Eine Main-Klasse, die von `JavaPlugin` erbt.
- Optional: Registrierung von Commands, Events, Assets, Tasks, Components oder Entity Stores.

### Lifecycle einfach erklärt

- `setup()` = vorbereiten und Dinge registrieren.
- `start()` = Plugin aktiv starten.
- `shutdown()` = sauber aufräumen.

### Wichtige Registries in `PluginBase`

- `getCommandRegistry()` für Commands.
- `getEventRegistry()` für Events.
- `getTaskRegistry()` für Tasks.
- `getEntityRegistry()` für Entities.
- `getEntityStoreRegistry()` und `getChunkStoreRegistry()` für gespeicherte Entity-/Chunk-Daten.
- `getAssetRegistry()` und `getCodecRegistry()` für Assets/JSON-Daten.

## 7. Für dein Keystone-NPC-MVP besonders wichtig

- `server/npc/corecomponents/movement` – Bewegung/Navigation von NPCs.
- `server/npc/role` – Rollen-System, passend für `lumberjack`.
- `server/npc/blackboard` – NPC-Gedächtnis/Arbeitsdaten.
- `server/npc/systems` – laufende NPC-Logik.
- `server/npc/commands` – Beispiele für NPC-Admin-Commands.
- `server/spawning/spawnmarkers` – später für Marker-Spawns.
- `server/core/command/system/basecommands` – gute Vorlage für deine `/keystone ...` Commands.
- `server/core/universe/world/storage` – interessant für gespeicherte Marker/NPC-Daten.

## 8. Suchbegriffe für VSCode

```text
JavaPlugin
PluginBase
PluginManifest
CommandBase
AbstractPlayerCommand
getCommandRegistry
getEventRegistry
getTaskRegistry
NPCPlugin
Role
Blackboard
StateMachine
SpawnMarker
Prefab
World
ChunkStore
EntityStore
Component
Codec
AssetRegistry
```

## 9. Gefundene Plugin-Klassen `extends JavaPlugin`

Diese Klassen sind gute Beispiele, weil sie echte eingebaute Plugins/Module sind.

- `CameraPlugin` → `com/hypixel/hytale/builtin/adventure/camera/CameraPlugin.java`

- `FarmingPlugin` → `com/hypixel/hytale/builtin/adventure/farming/FarmingPlugin.java`

- `MemoriesPlugin` → `com/hypixel/hytale/builtin/adventure/memories/MemoriesPlugin.java`

- `NPCObjectivesPlugin` → `com/hypixel/hytale/builtin/adventure/npcobjectives/NPCObjectivesPlugin.java`

- `NPCReputationPlugin` → `com/hypixel/hytale/builtin/adventure/npcreputation/NPCReputationPlugin.java`

- `NPCShopPlugin` → `com/hypixel/hytale/builtin/adventure/npcshop/NPCShopPlugin.java`

- `ObjectiveReputationPlugin` → `com/hypixel/hytale/builtin/adventure/objectivereputation/ObjectiveReputationPlugin.java`

- `ObjectiveShopPlugin` → `com/hypixel/hytale/builtin/adventure/objectiveshop/ObjectiveShopPlugin.java`

- `ReputationPlugin` → `com/hypixel/hytale/builtin/adventure/reputation/ReputationPlugin.java`

- `ShopPlugin` → `com/hypixel/hytale/builtin/adventure/shop/ShopPlugin.java`

- `ShopReputationPlugin` → `com/hypixel/hytale/builtin/adventure/shopreputation/ShopReputationPlugin.java`

- `StashPlugin` → `com/hypixel/hytale/builtin/adventure/stash/StashPlugin.java`

- `TeleporterPlugin` → `com/hypixel/hytale/builtin/adventure/teleporter/TeleporterPlugin.java`

- `WorldLocationConditionPlugin` → `com/hypixel/hytale/builtin/adventure/worldlocationcondition/WorldLocationConditionPlugin.java`

- `AmbiencePlugin` → `com/hypixel/hytale/builtin/ambience/AmbiencePlugin.java`

- `AssetEditorPlugin` → `com/hypixel/hytale/builtin/asseteditor/AssetEditorPlugin.java`

- `AudioPlugin` → `com/hypixel/hytale/builtin/audio/AudioPlugin.java`

- `BedsPlugin` → `com/hypixel/hytale/builtin/beds/BedsPlugin.java`

- `BlockPhysicsPlugin` → `com/hypixel/hytale/builtin/blockphysics/BlockPhysicsPlugin.java`

- `BlockSpawnerPlugin` → `com/hypixel/hytale/builtin/blockspawner/BlockSpawnerPlugin.java`

- `BlockTickPlugin` → `com/hypixel/hytale/builtin/blocktick/BlockTickPlugin.java`

- `MacroCommandPlugin` → `com/hypixel/hytale/builtin/commandmacro/MacroCommandPlugin.java`

- `CraftingPlugin` → `com/hypixel/hytale/builtin/crafting/CraftingPlugin.java`

- `CreativeHubPlugin` → `com/hypixel/hytale/builtin/creativehub/CreativeHubPlugin.java`

- `CrouchSlidePlugin` → `com/hypixel/hytale/builtin/crouchslide/CrouchSlidePlugin.java`

- `DeployablesPlugin` → `com/hypixel/hytale/builtin/deployables/DeployablesPlugin.java`

- `FallingBlocksPlugin` → `com/hypixel/hytale/builtin/fallingblocks/FallingBlocksPlugin.java`

- `FluidPlugin` → `com/hypixel/hytale/builtin/fluid/FluidPlugin.java`

- `HytaleGenerator` → `com/hypixel/hytale/builtin/hytalegenerator/plugin/HytaleGenerator.java`

- `InstancesPlugin` → `com/hypixel/hytale/builtin/instances/InstancesPlugin.java`

- `LANDiscoveryPlugin` → `com/hypixel/hytale/builtin/landiscovery/LANDiscoveryPlugin.java`

- `LocatePlugin` → `com/hypixel/hytale/builtin/locate/LocatePlugin.java`

- `MantlingPlugin` → `com/hypixel/hytale/builtin/mantling/MantlingPlugin.java`

- `ModelPlugin` → `com/hypixel/hytale/builtin/model/ModelPlugin.java`

- `MountPlugin` → `com/hypixel/hytale/builtin/mounts/MountPlugin.java`

- `NPCCombatActionEvaluatorPlugin` → `com/hypixel/hytale/builtin/npccombatactionevaluator/NPCCombatActionEvaluatorPlugin.java`

- `NPCEditorPlugin` → `com/hypixel/hytale/builtin/npceditor/NPCEditorPlugin.java`

- `ParkourPlugin` → `com/hypixel/hytale/builtin/parkour/ParkourPlugin.java`

- `PathPlugin` → `com/hypixel/hytale/builtin/path/PathPlugin.java`

- `PortalsPlugin` → `com/hypixel/hytale/builtin/portals/PortalsPlugin.java`

- `RandomTickPlugin` → `com/hypixel/hytale/builtin/randomtick/RandomTickPlugin.java`

- `SafetyRollPlugin` → `com/hypixel/hytale/builtin/safetyroll/SafetyRollPlugin.java`

- `SprintForcePlugin` → `com/hypixel/hytale/builtin/sprintforce/SprintForcePlugin.java`

- `TagSetPlugin` → `com/hypixel/hytale/builtin/tagset/TagSetPlugin.java`

- `TeleportPlugin` → `com/hypixel/hytale/builtin/teleport/TeleportPlugin.java`

- `TriggerVolumesPlugin` → `com/hypixel/hytale/builtin/triggervolumes/TriggerVolumesPlugin.java`

- `WeatherPlugin` → `com/hypixel/hytale/builtin/weather/WeatherPlugin.java`

- `WorldGenPlugin` → `com/hypixel/hytale/builtin/worldgen/WorldGenPlugin.java`

- `AssetModule` → `com/hypixel/hytale/server/core/asset/AssetModule.java`

- `CommonAssetModule` → `com/hypixel/hytale/server/core/asset/common/CommonAssetModule.java`

- `BlockTypeModule` → `com/hypixel/hytale/server/core/blocktype/BlockTypeModule.java`

- `ConsoleModule` → `com/hypixel/hytale/server/core/console/ConsoleModule.java`

- `CosmeticsModule` → `com/hypixel/hytale/server/core/cosmetics/CosmeticsModule.java`

- `ServerManager` → `com/hypixel/hytale/server/core/io/ServerManager.java`

- `LiveConfigModule` → `com/hypixel/hytale/server/core/liveconfig/LiveConfigModule.java`

- `LegacyModule` → `com/hypixel/hytale/server/core/modules/LegacyModule.java`

- `AccessControlModule` → `com/hypixel/hytale/server/core/modules/accesscontrol/AccessControlModule.java`

- `AnchorActionModule` → `com/hypixel/hytale/server/core/modules/anchoraction/AnchorActionModule.java`

- `BlockModule` → `com/hypixel/hytale/server/core/modules/block/BlockModule.java`

- `BlockHealthModule` → `com/hypixel/hytale/server/core/modules/blockhealth/BlockHealthModule.java`

- `BlockSetModule` → `com/hypixel/hytale/server/core/modules/blockset/BlockSetModule.java`

- `FlyCameraModule` → `com/hypixel/hytale/server/core/modules/camera/FlyCameraModule.java`

- `CollisionModule` → `com/hypixel/hytale/server/core/modules/collision/CollisionModule.java`

- `DebugPlugin` → `com/hypixel/hytale/server/core/modules/debug/DebugPlugin.java`

- `DamageModule` → `com/hypixel/hytale/server/core/modules/entity/damage/DamageModule.java`

- `StaminaModule` → `com/hypixel/hytale/server/core/modules/entity/stamina/StaminaModule.java`

- `EntityStatsModule` → `com/hypixel/hytale/server/core/modules/entitystats/EntityStatsModule.java`

- `EntityUIModule` → `com/hypixel/hytale/server/core/modules/entityui/EntityUIModule.java`

- `I18nModule` → `com/hypixel/hytale/server/core/modules/i18n/I18nModule.java`

- `ItemModule` → `com/hypixel/hytale/server/core/modules/item/ItemModule.java`

- `MigrationModule` → `com/hypixel/hytale/server/core/modules/migrations/MigrationModule.java`

- `PrefabSpawnerModule` → `com/hypixel/hytale/server/core/modules/prefabspawner/PrefabSpawnerModule.java`

- `ProjectileModule` → `com/hypixel/hytale/server/core/modules/projectile/ProjectileModule.java`

- `ServerPlayerListModule` → `com/hypixel/hytale/server/core/modules/serverplayerlist/ServerPlayerListModule.java`

- `SingleplayerModule` → `com/hypixel/hytale/server/core/modules/singleplayer/SingleplayerModule.java`

- `SplitVelocity` → `com/hypixel/hytale/server/core/modules/splitvelocity/SplitVelocity.java`

- `TimeModule` → `com/hypixel/hytale/server/core/modules/time/TimeModule.java`

- `VoiceModule` → `com/hypixel/hytale/server/core/modules/voice/VoiceModule.java`

- `PermissionsModule` → `com/hypixel/hytale/server/core/permissions/PermissionsModule.java`

- `TelemetryModule` → `com/hypixel/hytale/server/core/telemetry/TelemetryModule.java`

- `ConnectedBlocksModule` → `com/hypixel/hytale/server/core/universe/world/connectedblocks/ConnectedBlocksModule.java`

- `UpdateModule` → `com/hypixel/hytale/server/core/update/UpdateModule.java`

- `FlockPlugin` → `com/hypixel/hytale/server/flock/FlockPlugin.java`

- `SpawningPlugin` → `com/hypixel/hytale/server/spawning/SpawningPlugin.java`

## 10. Besonders interessante Dateien

- `com/hypixel/hytale/server/core/plugin/PluginBase.java`

- `com/hypixel/hytale/server/core/plugin/JavaPlugin.java`

- `com/hypixel/hytale/common/plugin/PluginManifest.java`

- `com/hypixel/hytale/server/core/plugin/PluginManager.java`

- `com/hypixel/hytale/server/core/command/system/CommandRegistry.java`

- `com/hypixel/hytale/server/core/command/system/basecommands/CommandBase.java`

- `com/hypixel/hytale/server/core/command/system/basecommands/AbstractPlayerCommand.java`

- `com/hypixel/hytale/server/core/command/commands/player/WhereAmICommand.java`

- `com/hypixel/hytale/server/npc/NPCPlugin.java`

- `com/hypixel/hytale/server/spawning/SpawningPlugin.java`

- `com/hypixel/hytale/server/core/modules/prefabspawner/PrefabSpawnerModule.java`

## 11. Empfehlung: Reihenfolge beim Lesen

1. `common/plugin/PluginManifest.java` – versteht deine `manifest.json`.

2. `server/core/plugin/JavaPlugin.java` und `PluginBase.java` – versteht den Plugin-Lifecycle.

3. `server/core/command/system/basecommands/CommandBase.java` – versteht einfache Commands.

4. `server/core/command/commands/player/WhereAmICommand.java` – gutes Beispiel für Position/Welt/Spieler.

5. `server/npc/NPCPlugin.java` – Einstieg ins NPC-System.

6. `server/npc/role`, `server/npc/blackboard`, `server/npc/corecomponents/movement`.

7. Erst danach `server/worldgen` und `builtin/hytalegenerator` für automatische Weltplatzierung.

## 12. Vollständiger Directory-Anhang

Hinweis: Dieser Anhang ist bewusst lang. Jeder Ordner bekommt einen kurzen Eintrag.

### `com/`

- **Java-Dateien direkt:** 0
- **Java-Dateien gesamt:** 5752
- **Beispiele:** keine direkten .java Dateien
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/`

- **Java-Dateien direkt:** 0
- **Java-Dateien gesamt:** 5752
- **Beispiele:** keine direkten .java Dateien
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/fastutil/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 66
- **Beispiele:** FastCollection.java
- **Was passiert hier?** Performance-Hilfscode für schnelle Maps/Collections.
- **Hilft beim Modding?** Meist nur verstehen, nicht direkt für Mods anfassen.

### `com/hypixel/fastutil/bytes/`

- **Java-Dateien direkt:** 9
- **Java-Dateien gesamt:** 9
- **Beispiele:** Byte2ByteOperator.java, Byte2CharOperator.java, Byte2DoubleOperator.java, Byte2FloatOperator.java, Byte2IntOperator.java, Byte2LongOperator.java, Byte2ObjectConcurrentHashMap.java, Byte2ObjectOperator.java …
- **Was passiert hier?** Performance-Hilfscode für schnelle Maps/Collections.
- **Hilft beim Modding?** Meist nur verstehen, nicht direkt für Mods anfassen.

### `com/hypixel/fastutil/chars/`

- **Java-Dateien direkt:** 9
- **Java-Dateien gesamt:** 9
- **Beispiele:** Char2ByteOperator.java, Char2CharOperator.java, Char2DoubleOperator.java, Char2FloatOperator.java, Char2IntOperator.java, Char2LongOperator.java, Char2ObjectConcurrentHashMap.java, Char2ObjectOperator.java …
- **Was passiert hier?** Performance-Hilfscode für schnelle Maps/Collections.
- **Hilft beim Modding?** Meist nur verstehen, nicht direkt für Mods anfassen.

### `com/hypixel/fastutil/doubles/`

- **Java-Dateien direkt:** 9
- **Java-Dateien gesamt:** 9
- **Beispiele:** Double2ByteOperator.java, Double2CharOperator.java, Double2DoubleOperator.java, Double2FloatOperator.java, Double2IntOperator.java, Double2LongOperator.java, Double2ObjectConcurrentHashMap.java, Double2ObjectOperator.java …
- **Was passiert hier?** Performance-Hilfscode für schnelle Maps/Collections.
- **Hilft beim Modding?** Meist nur verstehen, nicht direkt für Mods anfassen.

### `com/hypixel/fastutil/floats/`

- **Java-Dateien direkt:** 9
- **Java-Dateien gesamt:** 9
- **Beispiele:** Float2ByteOperator.java, Float2CharOperator.java, Float2DoubleOperator.java, Float2FloatOperator.java, Float2IntOperator.java, Float2LongOperator.java, Float2ObjectConcurrentHashMap.java, Float2ObjectOperator.java …
- **Was passiert hier?** Performance-Hilfscode für schnelle Maps/Collections.
- **Hilft beim Modding?** Meist nur verstehen, nicht direkt für Mods anfassen.

### `com/hypixel/fastutil/ints/`

- **Java-Dateien direkt:** 9
- **Java-Dateien gesamt:** 9
- **Beispiele:** Int2ByteOperator.java, Int2CharOperator.java, Int2DoubleOperator.java, Int2FloatOperator.java, Int2IntOperator.java, Int2LongOperator.java, Int2ObjectConcurrentHashMap.java, Int2ObjectOperator.java …
- **Was passiert hier?** Performance-Hilfscode für schnelle Maps/Collections.
- **Hilft beim Modding?** Meist nur verstehen, nicht direkt für Mods anfassen.

### `com/hypixel/fastutil/longs/`

- **Java-Dateien direkt:** 9
- **Java-Dateien gesamt:** 9
- **Beispiele:** Long2ByteOperator.java, Long2CharOperator.java, Long2DoubleOperator.java, Long2FloatOperator.java, Long2IntOperator.java, Long2LongOperator.java, Long2ObjectConcurrentHashMap.java, Long2ObjectOperator.java …
- **Was passiert hier?** Performance-Hilfscode für schnelle Maps/Collections.
- **Hilft beim Modding?** Meist nur verstehen, nicht direkt für Mods anfassen.

### `com/hypixel/fastutil/shorts/`

- **Java-Dateien direkt:** 9
- **Java-Dateien gesamt:** 9
- **Beispiele:** Short2ByteOperator.java, Short2CharOperator.java, Short2DoubleOperator.java, Short2FloatOperator.java, Short2IntOperator.java, Short2LongOperator.java, Short2ObjectConcurrentHashMap.java, Short2ObjectOperator.java …
- **Was passiert hier?** Performance-Hilfscode für schnelle Maps/Collections.
- **Hilft beim Modding?** Meist nur verstehen, nicht direkt für Mods anfassen.

### `com/hypixel/fastutil/util/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** SneakyThrow.java, TLRUtil.java
- **Was passiert hier?** Performance-Hilfscode für schnelle Maps/Collections.
- **Hilft beim Modding?** Meist nur verstehen, nicht direkt für Mods anfassen.

### `com/hypixel/hytale/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 5686
- **Beispiele:** LateMain.java, Main.java
- **Was passiert hier?** Startpaket mit Main/LateMain.
- **Hilft beim Modding?** Nur interessant, um Serverstart grob zu verstehen.

### `com/hypixel/hytale/assetstore/`

- **Java-Dateien direkt:** 16
- **Java-Dateien gesamt:** 40
- **Beispiele:** AssetConstants.java, AssetExtraInfo.java, AssetHolder.java, AssetKeyValidator.java, AssetLoadResult.java, AssetMap.java, AssetPack.java, AssetReferences.java …
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/assetstore/codec/`

- **Java-Dateien direkt:** 4
- **Java-Dateien gesamt:** 4
- **Beispiele:** AssetBuilderCodec.java, AssetCodec.java, AssetCodecMapCodec.java, ContainedAssetCodec.java
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/assetstore/event/`

- **Java-Dateien direkt:** 9
- **Java-Dateien gesamt:** 9
- **Beispiele:** AssetMonitorEvent.java, AssetStoreEvent.java, AssetStoreMonitorEvent.java, AssetsEvent.java, GenerateAssetsEvent.java, LoadedAssetsEvent.java, RegisterAssetStoreEvent.java, RemoveAssetStoreEvent.java …
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/assetstore/iterator/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** AssetStoreIterator.java, CircularDependencyException.java
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/assetstore/map/`

- **Java-Dateien direkt:** 9
- **Java-Dateien gesamt:** 9
- **Beispiele:** AssetMapWithIndexes.java, BlockTypeAssetMap.java, CaseInsensitiveHashStrategy.java, DefaultAssetMap.java, IndexedAssetMap.java, IndexedLookupTableAssetMap.java, JsonAssetWithMap.java, LookupTableAssetMap.java …
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/builtin/`

- **Java-Dateien direkt:** 0
- **Java-Dateien gesamt:** 1475
- **Beispiele:** keine direkten .java Dateien
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/adventure/`

- **Java-Dateien direkt:** 0
- **Java-Dateien gesamt:** 227
- **Beispiele:** keine direkten .java Dateien
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/adventure/camera/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 13
- **Beispiele:** CameraPlugin.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/adventure/camera/asset/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 9
- **Beispiele:** CameraShakeConfig.java, EasingConfig.java, NoiseConfig.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/adventure/camera/asset/cameraeffect/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** CameraShakeEffect.java, ShakeIntensity.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/adventure/camera/asset/camerashake/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** CameraShake.java, CameraShakePacketGenerator.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/adventure/camera/asset/viewbobbing/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** ViewBobbing.java, ViewBobbingPacketGenerator.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/adventure/camera/command/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** CameraEffectCommand.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/adventure/camera/interaction/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** CameraShakeInteraction.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/adventure/camera/system/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** CameraEffectSystem.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/adventure/farming/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 23
- **Beispiele:** FarmingPlugin.java, FarmingSystems.java, FarmingUtil.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/adventure/farming/component/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** CoopResidentComponent.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/adventure/farming/config/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 10
- **Beispiele:** FarmingCoopAsset.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/adventure/farming/config/modifiers/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** FertilizerGrowthModifierAsset.java, LightLevelGrowthModifierAsset.java, WaterGrowthModifierAsset.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/adventure/farming/config/stages/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 6
- **Beispiele:** BlockStateFarmingStageData.java, BlockTypeFarmingStageData.java, PrefabFarmingStageData.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/adventure/farming/config/stages/spread/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** DirectionalGrowthBehaviour.java, SpreadFarmingStageData.java, SpreadGrowthBehaviour.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/adventure/farming/interactions/`

- **Java-Dateien direkt:** 6
- **Java-Dateien gesamt:** 6
- **Beispiele:** ChangeFarmingStageInteraction.java, FertilizeSoilInteraction.java, HarvestCropInteraction.java, UseCaptureCrateInteraction.java, UseCoopInteraction.java, UseWateringCanInteraction.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/adventure/farming/states/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** CoopBlock.java, FarmingBlock.java, TilledSoilBlock.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/adventure/memories/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 22
- **Beispiele:** MemoriesGameplayConfig.java, MemoriesPlugin.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/adventure/memories/commands/`

- **Java-Dateien direkt:** 6
- **Java-Dateien gesamt:** 6
- **Beispiele:** MemoriesCapacityCommand.java, MemoriesClearCommand.java, MemoriesCommand.java, MemoriesLevelCommand.java, MemoriesSetCountCommand.java, MemoriesUnlockCommand.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/adventure/memories/component/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** PlayerMemories.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/adventure/memories/interactions/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** MemoriesConditionInteraction.java, SetMemoriesCapacityInteraction.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/adventure/memories/memories/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 4
- **Beispiele:** Memory.java, MemoryProvider.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/adventure/memories/memories/npc/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** NPCMemory.java, NPCMemoryProvider.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/adventure/memories/page/`

- **Java-Dateien direkt:** 4
- **Java-Dateien gesamt:** 4
- **Beispiele:** MemoriesPage.java, MemoriesPageSupplier.java, MemoriesUnlockedPage.java, MemoriesUnlockedPageSuplier.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/adventure/memories/temple/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** ForgottenTempleConfig.java, TempleRespawnPlayersSystem.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/adventure/memories/window/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** MemoriesWindow.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/adventure/npcobjectives/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 22
- **Beispiele:** NPCObjectivesPlugin.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/adventure/npcobjectives/assets/`

- **Java-Dateien direkt:** 4
- **Java-Dateien gesamt:** 4
- **Beispiele:** BountyObjectiveTaskAsset.java, KillObjectiveTaskAsset.java, KillSpawnBeaconObjectiveTaskAsset.java, KillSpawnMarkerObjectiveTaskAsset.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/adventure/npcobjectives/npc/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 7
- **Beispiele:** ActionCompleteTask.java, ActionStartObjective.java, SensorHasTask.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/adventure/npcobjectives/npc/builders/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** BuilderActionCompleteTask.java, BuilderActionStartObjective.java, BuilderSensorHasTask.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/adventure/npcobjectives/npc/validators/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** ObjectiveExistsValidator.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/adventure/npcobjectives/resources/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** KillTrackerResource.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/adventure/npcobjectives/systems/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** KillTrackerSystem.java, SpawnBeaconCheckRemovalSystem.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/adventure/npcobjectives/task/`

- **Java-Dateien direkt:** 6
- **Java-Dateien gesamt:** 6
- **Beispiele:** BountyObjectiveTask.java, KillNPCObjectiveTask.java, KillObjectiveTask.java, KillSpawnBeaconObjectiveTask.java, KillSpawnMarkerObjectiveTask.java, KillTask.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/adventure/npcobjectives/transaction/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** KillTaskTransaction.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/adventure/npcreputation/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** NPCReputationHolderSystem.java, NPCReputationPlugin.java, ReputationAttitudeSystem.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/adventure/npcshop/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 7
- **Beispiele:** NPCShopPlugin.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/adventure/npcshop/npc/`

- **Java-Dateien direkt:** 4
- **Java-Dateien gesamt:** 6
- **Beispiele:** ActionOpenBarterShop.java, ActionOpenShop.java, BarterShopExistsValidator.java, ShopExistsValidator.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/adventure/npcshop/npc/builders/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** BuilderActionOpenBarterShop.java, BuilderActionOpenShop.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/adventure/objectivereputation/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 4
- **Beispiele:** ObjectiveReputationPlugin.java, ReputationCompletion.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/adventure/objectivereputation/assets/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** ReputationCompletionAsset.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/adventure/objectivereputation/historydata/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** ReputationObjectiveRewardHistoryData.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/adventure/objectives/`

- **Java-Dateien direkt:** 4
- **Java-Dateien gesamt:** 87
- **Beispiele:** DialogPage.java, Objective.java, ObjectiveDataStore.java, ObjectivePlugin.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/adventure/objectives/admin/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** ObjectiveAdminPanelPage.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/adventure/objectives/blockstates/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** TreasureChestBlock.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/adventure/objectives/commands/`

- **Java-Dateien direkt:** 7
- **Java-Dateien gesamt:** 7
- **Beispiele:** ObjectiveCommand.java, ObjectiveCompleteCommand.java, ObjectiveHistoryCommand.java, ObjectiveLocationMarkerCommand.java, ObjectivePanelCommand.java, ObjectiveReachLocationMarkerCommand.java, ObjectiveStartCommand.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/adventure/objectives/completion/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** ClearObjectiveItemsCompletion.java, GiveItemsCompletion.java, ObjectiveCompletion.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/adventure/objectives/components/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** ObjectiveHistoryComponent.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/adventure/objectives/config/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 32
- **Beispiele:** ObjectiveAsset.java, ObjectiveLineAsset.java, ObjectiveLocationMarkerAsset.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/adventure/objectives/config/completion/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** ClearObjectiveItemsCompletionAsset.java, GiveItemsCompletionAsset.java, ObjectiveCompletionAsset.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/adventure/objectives/config/gameplayconfig/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** ObjectiveGameplayConfig.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/adventure/objectives/config/markerarea/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** ObjectiveLocationAreaBox.java, ObjectiveLocationAreaRadius.java, ObjectiveLocationMarkerArea.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/adventure/objectives/config/objectivesetup/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** ObjectiveTypeSetup.java, SetupObjective.java, SetupObjectiveLine.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/adventure/objectives/config/task/`

- **Java-Dateien direkt:** 10
- **Java-Dateien gesamt:** 10
- **Beispiele:** BlockTagOrItemIdField.java, CountObjectiveTaskAsset.java, CraftObjectiveTaskAsset.java, GatherObjectiveTaskAsset.java, ObjectiveTaskAsset.java, ReachLocationTaskAsset.java, TaskSet.java, TreasureMapObjectiveTaskAsset.java …
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/adventure/objectives/config/taskcondition/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** SoloInventoryCondition.java, TaskConditionAsset.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/adventure/objectives/config/triggercondition/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** HourRangeTriggerCondition.java, ObjectiveLocationTriggerCondition.java, WeatherTriggerCondition.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/adventure/objectives/config/worldlocationproviders/`

- **Java-Dateien direkt:** 4
- **Java-Dateien gesamt:** 4
- **Beispiele:** CheckTagWorldHeightRadiusProvider.java, LocationRadiusProvider.java, LookBlocksBelowProvider.java, WorldLocationProvider.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/adventure/objectives/events/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** TreasureChestOpeningEvent.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/adventure/objectives/historydata/`

- **Java-Dateien direkt:** 5
- **Java-Dateien gesamt:** 5
- **Beispiele:** CommonObjectiveHistoryData.java, ItemObjectiveRewardHistoryData.java, ObjectiveHistoryData.java, ObjectiveLineHistoryData.java, ObjectiveRewardHistoryData.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/adventure/objectives/interactions/`

- **Java-Dateien direkt:** 4
- **Java-Dateien gesamt:** 4
- **Beispiele:** CanBreakRespawnPointInteraction.java, DestroyTreasureConditionInteraction.java, OpenTreasureContainerInteraction.java, StartObjectiveInteraction.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/adventure/objectives/markers/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 7
- **Beispiele:** ObjectiveMarkerProvider.java, ObjectiveTaskMarker.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/adventure/objectives/markers/objectivelocation/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** ObjectiveLocationMarker.java, ObjectiveLocationMarkerSystems.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/adventure/objectives/markers/reachlocation/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** ReachLocationMarker.java, ReachLocationMarkerAsset.java, ReachLocationMarkerSystems.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/adventure/objectives/systems/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** ObjectiveInventoryChangeSystem.java, ObjectiveItemEntityRemovalSystem.java, ObjectivePlayerSetupSystem.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/adventure/objectives/task/`

- **Java-Dateien direkt:** 10
- **Java-Dateien gesamt:** 10
- **Beispiele:** CountObjectiveTask.java, CraftObjectiveTask.java, GatherObjectiveTask.java, InventoryChangeAware.java, ObjectiveTask.java, ObjectiveTaskRef.java, ReachLocationTask.java, TreasureMapObjectiveTask.java …
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/adventure/objectives/transaction/`

- **Java-Dateien direkt:** 8
- **Java-Dateien gesamt:** 8
- **Beispiele:** RegistrationTransactionRecord.java, SpawnEntityTransactionRecord.java, SpawnTreasureChestTransactionRecord.java, TransactionRecord.java, TransactionStatus.java, TransactionUtil.java, UseEntityTransactionRecord.java, WorldTransactionRecord.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/adventure/objectiveshop/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** CanStartObjectiveRequirement.java, ObjectiveShopPlugin.java, StartObjectiveInteraction.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/adventure/reputation/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 12
- **Beispiele:** ReputationGameplayConfig.java, ReputationGroupComponent.java, ReputationPlugin.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/adventure/reputation/assets/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** ReputationGroup.java, ReputationRank.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/adventure/reputation/choices/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** ReputationRequirement.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/adventure/reputation/command/`

- **Java-Dateien direkt:** 5
- **Java-Dateien gesamt:** 5
- **Beispiele:** ReputationAddCommand.java, ReputationCommand.java, ReputationRankCommand.java, ReputationSetCommand.java, ReputationValueCommand.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/adventure/reputation/store/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** ReputationDataResource.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/adventure/shop/`

- **Java-Dateien direkt:** 6
- **Java-Dateien gesamt:** 16
- **Beispiele:** GiveItemInteraction.java, ShopAsset.java, ShopElement.java, ShopPage.java, ShopPageSupplier.java, ShopPlugin.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/adventure/shop/barter/`

- **Java-Dateien direkt:** 10
- **Java-Dateien gesamt:** 10
- **Beispiele:** BarterItemStack.java, BarterPage.java, BarterShopAsset.java, BarterShopState.java, BarterTrade.java, FixedTradeSlot.java, PoolTradeSlot.java, RefreshInterval.java …
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/adventure/shopreputation/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** ShopReputationPlugin.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/adventure/stash/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** StashGameplayConfig.java, StashPlugin.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/adventure/teleporter/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 10
- **Beispiele:** TeleporterPlugin.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/adventure/teleporter/component/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** Teleporter.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/adventure/teleporter/interaction/`

- **Java-Dateien direkt:** 0
- **Java-Dateien gesamt:** 2
- **Beispiele:** keine direkten .java Dateien
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/adventure/teleporter/interaction/server/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** TeleporterInteraction.java, UsedTeleporter.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/adventure/teleporter/page/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** TeleporterSettingsPage.java, TeleporterSettingsPageSupplier.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/adventure/teleporter/system/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** ClearUsedTeleporterSystem.java, CreateWarpWhenTeleporterPlacedSystem.java, TurnOffTeleportersSystem.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/adventure/teleporter/util/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** CannedWarpNames.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/adventure/worldlocationcondition/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** NeighbourBlockTagsLocationCondition.java, WorldLocationConditionPlugin.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/ambience/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 5
- **Beispiele:** AmbiencePlugin.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/ambience/commands/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** AmbienceCommands.java, AmbienceEmitterAddCommand.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/ambience/components/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** AmbientEmitterComponent.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/ambience/systems/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** AmbientEmitterSystems.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/asseteditor/`

- **Java-Dateien direkt:** 10
- **Java-Dateien gesamt:** 30
- **Beispiele:** AssetEditorGamePacketHandler.java, AssetEditorPacketHandler.java, AssetEditorPlugin.java, AssetPath.java, AssetSpecificFunctionality.java, AssetTree.java, AssetTypeRegistry.java, EditorClient.java …
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/asseteditor/assettypehandler/`

- **Java-Dateien direkt:** 4
- **Java-Dateien gesamt:** 4
- **Beispiele:** AssetStoreTypeHandler.java, AssetTypeHandler.java, CommonAssetTypeHandler.java, JsonTypeHandler.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/asseteditor/data/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** AssetState.java, AssetUndoRedoInfo.java, ModifiedAsset.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/asseteditor/datasource/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** DataSource.java, StandardDataSource.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/asseteditor/event/`

- **Java-Dateien direkt:** 8
- **Java-Dateien gesamt:** 8
- **Beispiele:** AssetEditorActivateButtonEvent.java, AssetEditorAssetCreatedEvent.java, AssetEditorClientDisconnectEvent.java, AssetEditorFetchAutoCompleteDataEvent.java, AssetEditorRequestDataSetEvent.java, AssetEditorSelectAssetEvent.java, AssetEditorUpdateWeatherPreviewLockEvent.java, EditorClientEvent.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/asseteditor/util/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** AssetPathUtil.java, AssetStoreUtil.java, BsonTransformationUtil.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/audio/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 11
- **Beispiele:** AudioPlugin.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/audio/commands/`

- **Java-Dateien direkt:** 6
- **Java-Dateien gesamt:** 6
- **Beispiele:** AudioCommands.java, AudioMusicClearCommand.java, AudioMusicCommands.java, AudioMusicForceCommand.java, AudioStateCommands.java, AudioStateSetCommand.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/audio/components/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** AudioStateComponent.java, ForcedMusicTracker.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/audio/systems/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** AudioStateSystems.java, ForcedMusicSystems.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/beds/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 20
- **Beispiele:** BedsPlugin.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/beds/interactions/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** BedInteraction.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/beds/respawn/`

- **Java-Dateien direkt:** 4
- **Java-Dateien gesamt:** 4
- **Beispiele:** OverrideNearbyRespawnPointPage.java, RespawnPointPage.java, SelectOverrideRespawnPointPage.java, SetNameRespawnPointPage.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/beds/sleep/`

- **Java-Dateien direkt:** 0
- **Java-Dateien gesamt:** 14
- **Beispiele:** keine direkten .java Dateien
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/beds/sleep/components/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** PlayerSleep.java, PlayerSomnolence.java, SleepTracker.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/beds/sleep/resources/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** WorldSleep.java, WorldSlumber.java, WorldSomnolence.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/beds/sleep/systems/`

- **Java-Dateien direkt:** 0
- **Java-Dateien gesamt:** 8
- **Beispiele:** keine direkten .java Dateien
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/beds/sleep/systems/player/`

- **Java-Dateien direkt:** 5
- **Java-Dateien gesamt:** 5
- **Beispiele:** EnterBedSystem.java, RegisterTrackerSystem.java, SleepNotificationSystem.java, UpdateSleepPacketSystem.java, WakeUpOnDismountSystem.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/beds/sleep/systems/world/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** CanSleepInWorld.java, StartSlumberSystem.java, UpdateWorldSlumberSystem.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/blockphysics/`

- **Java-Dateien direkt:** 5
- **Java-Dateien gesamt:** 5
- **Beispiele:** BlockPhysicsPlugin.java, BlockPhysicsSystems.java, BlockPhysicsUtil.java, PrefabBufferValidator.java, WorldValidationUtil.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/blockspawner/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 7
- **Beispiele:** BlockSpawnerEntry.java, BlockSpawnerPlugin.java, BlockSpawnerTable.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/blockspawner/command/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** BlockSpawnerCommand.java, BlockSpawnerGetCommand.java, BlockSpawnerSetCommand.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/blockspawner/state/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** BlockSpawner.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/blocktick/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 5
- **Beispiele:** BlockTickPlugin.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/blocktick/procedure/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** BasicChanceBlockGrowthProcedure.java, SplitChanceBlockGrowthProcedure.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/blocktick/system/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** ChunkBlockTickSystem.java, MergeWaitingBlocksSystem.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/buildertools/`

- **Java-Dateien direkt:** 12
- **Java-Dateien gesamt:** 203
- **Beispiele:** BlockColorIndex.java, BuilderToolsPacketHandler.java, BuilderToolsPlugin.java, BuilderToolsSystems.java, BuilderToolsUserData.java, BuilderToolsUserDataSystem.java, CopyCutSettings.java, EditOperation.java …
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/buildertools/commands/`

- **Java-Dateien direkt:** 38
- **Java-Dateien gesamt:** 38
- **Beispiele:** ClearBlocksCommand.java, ClearEditHistory.java, ClearEntitiesCommand.java, ContractSelectionCommand.java, CopyCommand.java, CutCommand.java, DeselectCommand.java, EditLineCommand.java …
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/buildertools/imageimport/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** ImageImportCommand.java, ImageImportPage.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/buildertools/interactions/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** PickupItemInteraction.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/buildertools/objimport/`

- **Java-Dateien direkt:** 6
- **Java-Dateien gesamt:** 6
- **Beispiele:** MeshVoxelizer.java, MtlParser.java, ObjImportCommand.java, ObjImportPage.java, ObjParser.java, TextureSampler.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/buildertools/prefabeditor/`

- **Java-Dateien direkt:** 11
- **Java-Dateien gesamt:** 38
- **Beispiele:** PrefabAnchor.java, PrefabDirtySystems.java, PrefabEditSession.java, PrefabEditSessionManager.java, PrefabEditingMetadata.java, PrefabEditorCreationContext.java, PrefabEditorCreationSettings.java, PrefabLoadingState.java …
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/buildertools/prefabeditor/commands/`

- **Java-Dateien direkt:** 14
- **Java-Dateien gesamt:** 14
- **Beispiele:** PrefabEditBackCommand.java, PrefabEditCommand.java, PrefabEditCreateNewCommand.java, PrefabEditExitCommand.java, PrefabEditInfoCommand.java, PrefabEditKillEntitiesCommand.java, PrefabEditLoadCommand.java, PrefabEditModifiedCommand.java …
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/buildertools/prefabeditor/enums/`

- **Java-Dateien direkt:** 5
- **Java-Dateien gesamt:** 5
- **Beispiele:** PrefabAlignment.java, PrefabRootDirectory.java, PrefabRowSplitMode.java, PrefabStackingAxis.java, WorldGenType.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/buildertools/prefabeditor/saving/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** PrefabSaveContributor.java, PrefabSaver.java, PrefabSaverSettings.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/buildertools/prefabeditor/ui/`

- **Java-Dateien direkt:** 5
- **Java-Dateien gesamt:** 5
- **Beispiele:** PrefabEditorExitConfirmPage.java, PrefabEditorLoadOptionsPage.java, PrefabEditorLoadSettingsPage.java, PrefabEditorSaveSettingsPage.java, PrefabTeleportPage.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/buildertools/prefablist/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** AssetPrefabFileProvider.java, PrefabPage.java, PrefabSavePage.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/buildertools/scriptedbrushes/`

- **Java-Dateien direkt:** 5
- **Java-Dateien gesamt:** 70
- **Beispiele:** BrushConfig.java, BrushConfigChunkAccessor.java, BrushConfigCommandExecutor.java, BrushConfigEditStore.java, ScriptedBrushAsset.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/buildertools/scriptedbrushes/commands/`

- **Java-Dateien direkt:** 6
- **Java-Dateien gesamt:** 6
- **Beispiele:** BrushConfigClearCommand.java, BrushConfigCommand.java, BrushConfigDebugStepCommand.java, BrushConfigExitCommand.java, BrushConfigListCommand.java, BrushConfigLoadCommand.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/buildertools/scriptedbrushes/operations/`

- **Java-Dateien direkt:** 0
- **Java-Dateien gesamt:** 58
- **Beispiele:** keine direkten .java Dateien
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/buildertools/scriptedbrushes/operations/global/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** DebugBrushOperation.java, DisableHoldInteractionOperation.java, IgnoreExistingBrushDataOperation.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/buildertools/scriptedbrushes/operations/sequential/`

- **Java-Dateien direkt:** 22
- **Java-Dateien gesamt:** 51
- **Beispiele:** BlockPatternOperation.java, BreakpointOperation.java, ClearOperationMaskOperation.java, ClearRotationOperation.java, DeleteOperation.java, EchoOnceOperation.java, EchoOperation.java, ErodeOperation.java …
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/buildertools/scriptedbrushes/operations/sequential/dimensions/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** DimensionsOperation.java, RandomizeDimensionsOperation.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/buildertools/scriptedbrushes/operations/sequential/flowcontrol/`

- **Java-Dateien direkt:** 8
- **Java-Dateien gesamt:** 13
- **Beispiele:** ExitOperation.java, JumpIfBlockTypeOperation.java, JumpIfClickType.java, JumpIfCompareOperation.java, JumpIfStringMatchOperation.java, JumpIfToolArgOperation.java, JumpToIndexOperation.java, JumpToRandomIndex.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/buildertools/scriptedbrushes/operations/sequential/flowcontrol/loops/`

- **Java-Dateien direkt:** 5
- **Java-Dateien gesamt:** 5
- **Beispiele:** CircleOffsetAndLoopOperation.java, CircleOffsetFromArgOperation.java, LoadLoopFromToolArgOperation.java, LoopOperation.java, LoopRandomOperation.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/buildertools/scriptedbrushes/operations/sequential/masks/`

- **Java-Dateien direkt:** 6
- **Java-Dateien gesamt:** 6
- **Beispiele:** AppendMaskFromToolArgOperation.java, AppendMaskOperation.java, HistoryMaskOperation.java, MaskOperation.java, UseBrushMaskOperation.java, UseOperationMaskOperation.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/buildertools/scriptedbrushes/operations/sequential/offsets/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** OffsetOperation.java, RandomOffsetOperation.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/buildertools/scriptedbrushes/operations/sequential/saveandload/`

- **Java-Dateien direkt:** 5
- **Java-Dateien gesamt:** 5
- **Beispiele:** LoadBrushConfigOperation.java, LoadOperationsFromAssetOperation.java, PersistentDataOperation.java, SaveBrushConfigOperation.java, SaveIndexOperation.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/buildertools/scriptedbrushes/operations/sequential/transforms/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** RotateOperation.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/buildertools/scriptedbrushes/operations/system/`

- **Java-Dateien direkt:** 4
- **Java-Dateien gesamt:** 4
- **Beispiele:** BrushOperation.java, BrushOperationSetting.java, GlobalBrushOperation.java, SequenceBrushOperation.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/buildertools/scriptedbrushes/ui/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** ScriptedBrushPage.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/buildertools/snapshot/`

- **Java-Dateien direkt:** 12
- **Java-Dateien gesamt:** 12
- **Beispiele:** BlockSelectionSnapshot.java, ClipboardBoundsSnapshot.java, ClipboardContentsSnapshot.java, ClipboardSnapshot.java, EntityAddSnapshot.java, EntityFreezeSnapshot.java, EntityRemoveSnapshot.java, EntityScaleSnapshot.java …
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/buildertools/tooloperations/`

- **Java-Dateien direkt:** 12
- **Java-Dateien gesamt:** 17
- **Beispiele:** LaserPointerOperation.java, LayersOperation.java, NoiseOperation.java, OperationFactory.java, PaintOperation.java, RevolveOperation.java, ScatterOperation.java, SculptOperation.java …
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/buildertools/tooloperations/transform/`

- **Java-Dateien direkt:** 5
- **Java-Dateien gesamt:** 5
- **Beispiele:** Composite.java, Mirror.java, Rotate.java, Transform.java, Translate.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/buildertools/utils/`

- **Java-Dateien direkt:** 4
- **Java-Dateien gesamt:** 4
- **Beispiele:** FluidPatternHelper.java, Material.java, PasteToolUtil.java, RecursivePrefabLoader.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/commandmacro/`

- **Java-Dateien direkt:** 9
- **Java-Dateien gesamt:** 9
- **Beispiele:** EchoCommand.java, MacroCommandBase.java, MacroCommandBuilder.java, MacroCommandCollection.java, MacroCommandParameter.java, MacroCommandPlugin.java, MacroCommandReplacement.java, MacroCommandTreeBuilder.java …
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/crafting/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 18
- **Beispiele:** BenchRecipeRegistry.java, CraftingPlugin.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/crafting/commands/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** RecipeCommand.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/crafting/component/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** BenchBlock.java, CraftingManager.java, ProcessingBenchBlock.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/crafting/interaction/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** LearnRecipeInteraction.java, OpenBenchPageInteraction.java, OpenProcessingBenchInteraction.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/crafting/system/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** BenchSystems.java, PlayerCraftingSystems.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/crafting/window/`

- **Java-Dateien direkt:** 7
- **Java-Dateien gesamt:** 7
- **Beispiele:** BenchWindow.java, CraftingWindow.java, DiagramCraftingWindow.java, FieldCraftingWindow.java, ProcessingBenchWindow.java, SimpleCraftingWindow.java, StructuralCraftingWindow.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/creativehub/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 7
- **Beispiele:** CreativeHubPlugin.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/creativehub/command/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** HubCommand.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/creativehub/config/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** CreativeHubEntityConfig.java, CreativeHubWorldConfig.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/creativehub/interactions/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** HubPortalInteraction.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/creativehub/systems/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** ReturnToHubButtonSystem.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/creativehub/ui/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** ReturnToHubButtonUI.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/crouchslide/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** CrouchSlidePlugin.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/deployables/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 16
- **Beispiele:** DeployablesPlugin.java, DeployablesUtils.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/deployables/component/`

- **Java-Dateien direkt:** 4
- **Java-Dateien gesamt:** 4
- **Beispiele:** DeployableComponent.java, DeployableOwnerComponent.java, DeployableProjectileComponent.java, DeployableProjectileShooterComponent.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/deployables/config/`

- **Java-Dateien direkt:** 6
- **Java-Dateien gesamt:** 6
- **Beispiele:** DeployableAoeConfig.java, DeployableConfig.java, DeployableSpawner.java, DeployableTrapConfig.java, DeployableTrapSpawnerConfig.java, DeployableTurretConfig.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/deployables/interaction/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** SpawnDeployableAtHitLocationInteraction.java, SpawnDeployableAtLocationInteraction.java, SpawnDeployableFromRaycastInteraction.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/deployables/system/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** DeployablesSystem.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/fallingblocks/`

- **Java-Dateien direkt:** 5
- **Java-Dateien gesamt:** 5
- **Beispiele:** BreakFallingBlockImpact.java, FallingBlock.java, FallingBlockTickingSystem.java, FallingBlocksPlugin.java, PlaceFallingBlockImpact.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/fluid/`

- **Java-Dateien direkt:** 5
- **Java-Dateien gesamt:** 5
- **Beispiele:** DisabledFluidResource.java, FluidCommand.java, FluidPlugin.java, FluidState.java, FluidSystems.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/hytalegenerator/`

- **Java-Dateien direkt:** 13
- **Java-Dateien gesamt:** 573
- **Beispiele:** ArrayUtil.java, BlockMask.java, EntityPlacementData.java, FutureUtils.java, GridUtils.java, LoggerUtil.java, MaterialSet.java, PropRuntime.java …
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/hytalegenerator/assets/`

- **Java-Dateien direkt:** 4
- **Java-Dateien gesamt:** 267
- **Beispiele:** AssetManager.java, Cleanable.java, SettingsAsset.java, ValidatorUtil.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/hytalegenerator/assets/assignments/`

- **Java-Dateien direkt:** 6
- **Java-Dateien gesamt:** 6
- **Beispiele:** AssignmentsAsset.java, ConstantAssignmentsAsset.java, FieldFunctionAssignmentsAsset.java, ImportedAssignmentsAsset.java, SandwichAssignmentsAsset.java, WeightedAssignmentsAsset.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/hytalegenerator/assets/biomes/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** BiomeAsset.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/hytalegenerator/assets/blockmask/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** BlockMaskAsset.java, BlockMaskEntryAsset.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/hytalegenerator/assets/blockset/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** MaterialSetAsset.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/hytalegenerator/assets/bounds/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** DecimalBounds3dAsset.java, IntegerBounds3dAsset.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/hytalegenerator/assets/curves/`

- **Java-Dateien direkt:** 19
- **Java-Dateien gesamt:** 23
- **Beispiele:** CeilingCurveAsset.java, ClampCurveAsset.java, ConstantCurveAsset.java, CurveAsset.java, DistanceExponentialCurveAsset.java, DistanceSCurveAsset.java, FloorCurveAsset.java, ImportedCurveAsset.java …
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/hytalegenerator/assets/curves/legacy/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** NodeFunctionYOutAsset.java, PointYOutAsset.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/hytalegenerator/assets/curves/manual/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** ManualCurveAsset.java, PointInOutAsset.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/hytalegenerator/assets/delimiters/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** RangeDoubleAsset.java, RangeIntAsset.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/hytalegenerator/assets/density/`

- **Java-Dateien direkt:** 68
- **Java-Dateien gesamt:** 84
- **Beispiele:** AbsDensityAsset.java, AmplitudeConstantAsset.java, AmplitudeDensityAsset.java, AnchorDensityAsset.java, AngleDensityAsset.java, AxisDensityAsset.java, BaseHeightDensityAsset.java, Cache2dDensityAsset_Deprecated.java …
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/hytalegenerator/assets/density/positions/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 16
- **Beispiele:** Positions3DDensityAsset.java, PositionsCellNoiseDensityAsset.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/hytalegenerator/assets/density/positions/distancefunctions/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** DistanceFunctionAsset.java, EuclideanDistanceFunctionAsset.java, ManhattanDistanceFunctionAsset.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/hytalegenerator/assets/density/positions/returntypes/`

- **Java-Dateien direkt:** 11
- **Java-Dateien gesamt:** 11
- **Beispiele:** CellValueReturnTypeAsset.java, CurveReturnTypeAsset.java, DensityReturnTypeAsset.java, Distance2AddReturnTypeAsset.java, Distance2DivReturnTypeAsset.java, Distance2MulReturnTypeAsset.java, Distance2ReturnTypeAsset.java, Distance2SubReturnTypeAsset.java …
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/hytalegenerator/assets/environmentproviders/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** ConstantEnvironmentProviderAsset.java, DensityDelimitedEnvironmentProviderAsset.java, EnvironmentProviderAsset.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/hytalegenerator/assets/framework/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** DecimalConstantsFrameworkAsset.java, FrameworkAsset.java, PositionsFrameworkAsset.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/hytalegenerator/assets/interpolationasset/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** BiomeFrontierAsset.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/hytalegenerator/assets/material/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** MaterialAsset.java, OrthogonalRotationAsset.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/hytalegenerator/assets/materialproviders/`

- **Java-Dateien direkt:** 14
- **Java-Dateien gesamt:** 28
- **Beispiele:** ConstantMaterialProviderAsset.java, DownwardDepthMaterialProviderAsset.java, DownwardSpaceMaterialProviderAsset.java, FieldFunctionMaterialProviderAsset.java, ImportedMaterialProviderAsset.java, MaterialProviderAsset.java, QueueMaterialProviderAsset.java, SimpleHorizontalMaterialProviderAsset.java …
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/hytalegenerator/assets/materialproviders/spaceanddepth/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 14
- **Beispiele:** SpaceAndDepthMaterialProviderAsset.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/hytalegenerator/assets/materialproviders/spaceanddepth/conditionassets/`

- **Java-Dateien direkt:** 8
- **Java-Dateien gesamt:** 8
- **Beispiele:** AlwaysTrueConditionAsset.java, AndConditionAsset.java, ConditionAsset.java, EqualsConditionAsset.java, GreaterThanConditionAsset.java, NotConditionAsset.java, OrConditionAsset.java, SmallerThanConditionAsset.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/hytalegenerator/assets/materialproviders/spaceanddepth/layerassets/`

- **Java-Dateien direkt:** 5
- **Java-Dateien gesamt:** 5
- **Beispiele:** ConstantThicknessLayerAsset.java, LayerAsset.java, NoiseThicknessAsset.java, RangeThicknessAsset.java, WeightedThicknessLayerAsset.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/hytalegenerator/assets/noisegenerators/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** CellNoiseAsset.java, NoiseAsset.java, SimplexNoiseAsset.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/hytalegenerator/assets/patterns/`

- **Java-Dateien direkt:** 16
- **Java-Dateien gesamt:** 16
- **Beispiele:** AndPatternAsset.java, BlockSetPatternAsset.java, CeilingPatternAsset.java, ConstantPatternAsset.java, CuboidPatternAsset.java, DensityPatternAsset.java, FloorPatternAsset.java, ImportedPatternAsset.java …
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/hytalegenerator/assets/pointgenerators/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** MeshPointGeneratorAsset.java, NoPointGeneratorAsset.java, PointGeneratorAsset.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/hytalegenerator/assets/positionproviders/`

- **Java-Dateien direkt:** 23
- **Java-Dateien gesamt:** 23
- **Beispiele:** AnchorPositionProviderAsset.java, BaseHeightPositionProviderAsset.java, BoundPositionProviderAsset.java, CachedPositionProviderAsset.java, ClustersPositionProviderAsset.java, EmptyPositionProviderAsset.java, FieldFunctionOccurrencePositionProviderAsset.java, FieldFunctionPositionProviderAsset.java …
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/hytalegenerator/assets/propdistribution/`

- **Java-Dateien direkt:** 7
- **Java-Dateien gesamt:** 7
- **Beispiele:** AssignedPropDistributionAsset.java, ConstantPropDistributionAsset.java, ImportedPropDistributionAsset.java, NoPropDistributionAsset.java, PositionsPropDistributionAsset.java, PropDistributionAsset.java, UnionPropDistributionAsset.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/hytalegenerator/assets/propruntime/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** PropRuntimeAsset.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/hytalegenerator/assets/props/`

- **Java-Dateien direkt:** 20
- **Java-Dateien gesamt:** 28
- **Beispiele:** BoxPropAsset.java, ClusterPropAsset.java, ColumnPropAsset.java, CuboidPropAsset.java, DensityPropAsset.java, DensitySelectorPropAsset.java, EmptyPropAsset.java, ImportedPropAsset.java …
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/hytalegenerator/assets/props/prefabprop/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 8
- **Beispiele:** PrefabFileVisitor.java, PrefabLoader.java, PrefabPropAsset.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/hytalegenerator/assets/props/prefabprop/directionality/`

- **Java-Dateien direkt:** 5
- **Java-Dateien gesamt:** 5
- **Beispiele:** DirectionalityAsset.java, ImportedDirectionalityAsset.java, PatternDirectionalityAsset.java, RandomDirectionalityAsset.java, StaticDirectionalityAsset.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/hytalegenerator/assets/scanners/`

- **Java-Dateien direkt:** 10
- **Java-Dateien gesamt:** 10
- **Beispiele:** AreaScannerAsset.java, ColumnLinearScannerAsset.java, ColumnRandomScannerAsset.java, DirectScannerAsset.java, ImportedScannerAsset.java, LinearScannerAsset.java, QueueScannerAsset.java, RadialScannerAsset.java …
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/hytalegenerator/assets/terrains/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** DensityTerrainAsset.java, TerrainAsset.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/hytalegenerator/assets/tintproviders/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** ConstantTintProviderAsset.java, DensityDelimitedTintProviderAsset.java, TintProviderAsset.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/hytalegenerator/assets/vectorproviders/`

- **Java-Dateien direkt:** 6
- **Java-Dateien gesamt:** 6
- **Beispiele:** CacheVectorProviderAsset.java, ConstantVectorProviderAsset.java, DensityGradientVectorProviderAsset.java, ExportedVectorProviderAsset.java, ImportedVectorProviderAsset.java, VectorProviderAsset.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/hytalegenerator/assets/worldstructures/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 3
- **Beispiele:** WorldStructureAsset.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/hytalegenerator/assets/worldstructures/basic/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** BasicWorldStructureAsset.java, BiomeRangeAsset.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/hytalegenerator/assignments/`

- **Java-Dateien direkt:** 5
- **Java-Dateien gesamt:** 5
- **Beispiele:** Assignments.java, ConstantAssignments.java, FieldFunctionAssignments.java, SandwichAssignments.java, WeightedAssignments.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/hytalegenerator/biome/`

- **Java-Dateien direkt:** 6
- **Java-Dateien gesamt:** 6
- **Beispiele:** Biome.java, EnvironmentSource.java, MaterialSource.java, PropsSource.java, SimpleBiome.java, TintSource.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/hytalegenerator/bounds/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** Bounds3d.java, Bounds3i.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/hytalegenerator/cartas/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** SimpleNoiseCarta.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/hytalegenerator/commands/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** CreateCommand.java, ViewportCommand.java, WorldGenCommand.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/hytalegenerator/delimiters/`

- **Java-Dateien direkt:** 4
- **Java-Dateien gesamt:** 4
- **Beispiele:** DelimiterDouble.java, DelimiterInt.java, RangeDouble.java, RangeInt.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/hytalegenerator/density/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 77
- **Beispiele:** Density.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/hytalegenerator/density/nodes/`

- **Java-Dateien direkt:** 62
- **Java-Dateien gesamt:** 76
- **Beispiele:** AbsDensity.java, AmplitudeConstantDensity.java, AmplitudeDensity.java, AnchorDensity.java, AngleDensity.java, AxisDensity.java, BaseHeightDensity.java, CacheDensity.java …
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/hytalegenerator/density/nodes/positions/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 14
- **Beispiele:** PositionsDensity.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/hytalegenerator/density/nodes/positions/distancefunctions/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** DistanceFunction.java, EuclideanDistanceFunction.java, ManhattanDistanceFunction.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/hytalegenerator/density/nodes/positions/returntypes/`

- **Java-Dateien direkt:** 10
- **Java-Dateien gesamt:** 10
- **Beispiele:** CellValueReturnType.java, CurveReturnType.java, DensityReturnType.java, Distance2AddReturnType.java, Distance2DivReturnType.java, Distance2MulReturnType.java, Distance2ReturnType.java, Distance2SubReturnType.java …
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/hytalegenerator/engine/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 29
- **Beispiele:** TerrainDensityProvider.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/hytalegenerator/engine/bufferbundle/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 9
- **Beispiele:** BufferBundle.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/hytalegenerator/engine/bufferbundle/buffers/`

- **Java-Dateien direkt:** 6
- **Java-Dateien gesamt:** 8
- **Beispiele:** Buffer.java, CountedPixelBuffer.java, EntityBuffer.java, PixelBuffer.java, SimplePixelBuffer.java, VoxelBuffer.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/hytalegenerator/engine/bufferbundle/buffers/type/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** BufferType.java, ParametrizedBufferType.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/hytalegenerator/engine/chunkgenerator/`

- **Java-Dateien direkt:** 4
- **Java-Dateien gesamt:** 4
- **Beispiele:** ChunkGenerator.java, ChunkRequest.java, FallbackGenerator.java, StagedChunkGenerator.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/hytalegenerator/engine/containers/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** FloatContainer3d.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/hytalegenerator/engine/entityfunnel/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** EntityFunnel.java, RotationEntityFunnel.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/hytalegenerator/engine/performanceinstruments/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** MemInstrument.java, TimeInstrument.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/hytalegenerator/engine/stages/`

- **Java-Dateien direkt:** 7
- **Java-Dateien gesamt:** 7
- **Beispiele:** BiomeDistanceStage.java, BiomeStage.java, EnvironmentStage.java, PropStage.java, Stage.java, TerrainStage.java, TintStage.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/hytalegenerator/engine/views/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** EntityBufferView.java, PixelBufferView.java, VoxelBufferView.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/hytalegenerator/environmentproviders/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** ConstantEnvironmentProvider.java, DensityDelimitedEnvironmentProvider.java, EnvironmentProvider.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/hytalegenerator/material/`

- **Java-Dateien direkt:** 4
- **Java-Dateien gesamt:** 4
- **Beispiele:** FluidMaterial.java, Material.java, MaterialCache.java, SolidMaterial.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/hytalegenerator/materialproviders/`

- **Java-Dateien direkt:** 13
- **Java-Dateien gesamt:** 27
- **Beispiele:** ConstantMaterialProvider.java, DownwardDepthMaterialProvider.java, DownwardSpaceMaterialProvider.java, FieldFunctionMaterialProvider.java, HorizontalMaterialProvider.java, MaterialProvider.java, QueueMaterialProvider.java, SolidityMaterialProvider.java …
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/hytalegenerator/materialproviders/functions/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** DoubleFunctionXZ.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/hytalegenerator/materialproviders/spaceanddepth/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 13
- **Beispiele:** SpaceAndDepthMaterialProvider.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/hytalegenerator/materialproviders/spaceanddepth/conditions/`

- **Java-Dateien direkt:** 8
- **Java-Dateien gesamt:** 8
- **Beispiele:** AlwaysTrueCondition.java, AndCondition.java, ConditionParameter.java, EqualsCondition.java, GreaterThanCondition.java, NotCondition.java, OrCondition.java, SmallerThanCondition.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/hytalegenerator/materialproviders/spaceanddepth/layers/`

- **Java-Dateien direkt:** 4
- **Java-Dateien gesamt:** 4
- **Beispiele:** ConstantThicknessLayer.java, NoiseThickness.java, RangedThicknessLayer.java, WeightedThicknessLayer.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/hytalegenerator/math/`

- **Java-Dateien direkt:** 5
- **Java-Dateien gesamt:** 5
- **Beispiele:** Calculator.java, InterpolatedCurve.java, Interpolation.java, NodeFunction.java, Normalizer.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/hytalegenerator/noise/`

- **Java-Dateien direkt:** 5
- **Java-Dateien gesamt:** 8
- **Beispiele:** CellNoiseField.java, FastNoiseLite.java, NoiseField.java, Simplex.java, SimplexNoiseField.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/hytalegenerator/noise/pointprovider/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** JitterPointField.java, PointField.java, PointProvider.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/hytalegenerator/patterns/`

- **Java-Dateien direkt:** 13
- **Java-Dateien gesamt:** 13
- **Beispiele:** AndPattern.java, ConstantPattern.java, CuboidPattern.java, FieldFunctionPattern.java, MaterialPattern.java, MaterialSetPattern.java, NotPattern.java, OffsetPattern.java …
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/hytalegenerator/pipe/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** Control.java, Pipe.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/hytalegenerator/plugin/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 6
- **Beispiele:** Handle.java, HandleProvider.java, HytaleGenerator.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/hytalegenerator/plugin/editor/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** AssetPackUtil.java, BiomeEditor.java, BiomeEditorPage.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/hytalegenerator/positionproviders/`

- **Java-Dateien direkt:** 18
- **Java-Dateien gesamt:** 22
- **Beispiele:** AnchorPositionProvider.java, BaseHeightPositionProvider.java, BoundPositionProvider.java, ClustersPositionProvider.java, EmptyPositionProvider.java, FieldFunctionOccurrencePositionProvider.java, FieldFunctionPositionProvider.java, Jitter2dPositionProvider.java …
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/hytalegenerator/positionproviders/cached/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** CacheThreadMemory.java, CachedPositionProvider.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/hytalegenerator/positionproviders/deprecated/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** Mesh2DPositionProvider.java, Mesh3DPositionProvider.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/hytalegenerator/propdistributions/`

- **Java-Dateien direkt:** 6
- **Java-Dateien gesamt:** 6
- **Beispiele:** AssignedPropDistribution.java, ConstantPropDistribution.java, NoPropDistribution.java, PositionsPropDistribution.java, PropDistribution.java, UnionPropDistribution.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/hytalegenerator/props/`

- **Java-Dateien direkt:** 16
- **Java-Dateien gesamt:** 36
- **Beispiele:** CuboidProp.java, DensityProp.java, DensitySelectorProp.java, EmptyProp.java, LocatorProp.java, ManualProp.java, MaskProp.java, OffsetProp.java …
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/hytalegenerator/props/deprecated/`

- **Java-Dateien direkt:** 7
- **Java-Dateien gesamt:** 20
- **Beispiele:** BoxProp.java, ClusterProp.java, ColumnProp.java, DensityProp.java, PositionListScanResult.java, PositionScanResult.java, ScanResult.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/hytalegenerator/props/deprecated/directionality/`

- **Java-Dateien direkt:** 7
- **Java-Dateien gesamt:** 7
- **Beispiele:** Directionality.java, OrthogonalDirection.java, PatternDirectionality.java, RandomDirectionality.java, RotatedPosition.java, RotatedPositionsScanResult.java, StaticDirectionality.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/hytalegenerator/props/deprecated/filler/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** FillerPropScanResult.java, PondFillerProp.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/hytalegenerator/props/deprecated/prefab/`

- **Java-Dateien direkt:** 4
- **Java-Dateien gesamt:** 4
- **Beispiele:** MoldingDirection.java, PrefabMoldingConfiguration.java, PrefabProp.java, PrefabPropUtil.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/hytalegenerator/rangemaps/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** DoubleRange.java, DoubleRangeMap.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/hytalegenerator/referencebundle/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** ReferenceBundle.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/hytalegenerator/rng/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** Rng.java, RngField.java, SeedBox.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/hytalegenerator/scanners/`

- **Java-Dateien direkt:** 7
- **Java-Dateien gesamt:** 10
- **Beispiele:** DirectScanner.java, EmptyScanner.java, LinearScanner.java, QueueScanner.java, RadialScanner.java, RandomScanner.java, Scanner.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/hytalegenerator/scanners/deprecated/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** AreaScanner.java, ColumnLinearScanner.java, ColumnRandomScanner.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/hytalegenerator/tintproviders/`

- **Java-Dateien direkt:** 4
- **Java-Dateien gesamt:** 4
- **Beispiele:** ConstantTintProvider.java, DensityDelimitedTintProvider.java, NoTintProvider.java, TintProvider.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/hytalegenerator/vectorproviders/`

- **Java-Dateien direkt:** 4
- **Java-Dateien gesamt:** 4
- **Beispiele:** CacheVectorProvider.java, ConstantVectorProvider.java, DensityGradientVectorProvider.java, VectorProvider.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/hytalegenerator/voxelspace/`

- **Java-Dateien direkt:** 7
- **Java-Dateien gesamt:** 7
- **Beispiele:** ArrayVoxelSpace.java, MaskVoxelSpace.java, NullSpace.java, RotationVoxelSpace.java, VoxelSpace.java, VoxelSpaceUtil.java, WindowVoxelSpace.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/hytalegenerator/workerindexer/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** WorkerIndexer.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/hytalegenerator/worldstructure/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** BiCarta.java, WorldStructure.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/instances/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 29
- **Beispiele:** InstanceValidator.java, InstancesPlugin.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/instances/blocks/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** ConfigurableInstanceBlock.java, InstanceBlock.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/instances/command/`

- **Java-Dateien direkt:** 8
- **Java-Dateien gesamt:** 8
- **Beispiele:** InstanceEditCopyCommand.java, InstanceEditListCommand.java, InstanceEditLoadCommand.java, InstanceEditNewCommand.java, InstanceExitCommand.java, InstanceMigrateCommand.java, InstanceSpawnCommand.java, InstancesCommand.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/instances/config/`

- **Java-Dateien direkt:** 5
- **Java-Dateien gesamt:** 5
- **Beispiele:** ExitInstance.java, InstanceDiscoveryConfig.java, InstanceEntityConfig.java, InstanceWorldConfig.java, WorldReturnPoint.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/instances/event/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** DiscoverInstanceEvent.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/instances/interactions/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** ExitInstanceInteraction.java, TeleportConfigInstanceInteraction.java, TeleportInstanceInteraction.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/instances/page/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** ConfigureInstanceBlockPage.java, InstanceListPage.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/instances/removal/`

- **Java-Dateien direkt:** 6
- **Java-Dateien gesamt:** 6
- **Beispiele:** IdleTimeoutCondition.java, InstanceDataResource.java, RemovalCondition.java, RemovalSystem.java, TimeoutCondition.java, WorldEmptyCondition.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/landiscovery/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** LANDiscoveryCommand.java, LANDiscoveryPlugin.java, LANDiscoveryThread.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/locate/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 10
- **Beispiele:** LocatePlugin.java, PrefabPatternSearchUtil.java, SpiralSearchUtil.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/locate/command/`

- **Java-Dateien direkt:** 7
- **Java-Dateien gesamt:** 7
- **Beispiele:** AbstractLocateSubcommand.java, LocateBiomeCommand.java, LocateCommand.java, LocatePrefabCommand.java, LocateRegionCommand.java, LocateZoneCommand.java, PrefabSearchUtil.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/mantling/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** MantlingPlugin.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/model/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 3
- **Beispiele:** ModelPlugin.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/model/commands/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** ModelCommand.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/model/pages/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** ChangeModelPage.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/mounts/`

- **Java-Dateien direkt:** 9
- **Java-Dateien gesamt:** 18
- **Beispiele:** BlockMountAPI.java, BlockMountComponent.java, MountGamePacketHandler.java, MountPlugin.java, MountSystems.java, MountedByComponent.java, MountedComponent.java, NPCMountComponent.java …
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/mounts/commands/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** DismountCommand.java, MountCheckCommand.java, MountCommand.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/mounts/interactions/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** MountInteraction.java, SeatingInteraction.java, SpawnMinecartInteraction.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/mounts/minecart/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** MinecartComponent.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/mounts/npc/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 2
- **Beispiele:** ActionMount.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/mounts/npc/builders/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** BuilderActionMount.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/npccombatactionevaluator/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 27
- **Beispiele:** CombatActionEvaluatorSystems.java, NPCCombatActionEvaluatorPlugin.java, Positioning.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/npccombatactionevaluator/conditions/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** RecentSustainedDamageCondition.java, TargetMemoryCountCondition.java, TotalSustainedDamageCondition.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/npccombatactionevaluator/config/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** CombatBalanceAsset.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/npccombatactionevaluator/corecomponents/`

- **Java-Dateien direkt:** 5
- **Java-Dateien gesamt:** 10
- **Beispiele:** ActionAddToTargetMemory.java, ActionCombatAbility.java, CombatTargetCollector.java, SensorCombatActionEvaluator.java, SensorHasHostileTargetMemory.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/npccombatactionevaluator/corecomponents/builders/`

- **Java-Dateien direkt:** 5
- **Java-Dateien gesamt:** 5
- **Beispiele:** BuilderActionAddToTargetMemory.java, BuilderActionCombatAbility.java, BuilderCombatTargetCollector.java, BuilderSensorCombatActionEvaluator.java, BuilderSensorHasHostileTargetMemory.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/npccombatactionevaluator/evaluator/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 6
- **Beispiele:** CombatActionEvaluator.java, CombatActionEvaluatorConfig.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/npccombatactionevaluator/evaluator/combatactions/`

- **Java-Dateien direkt:** 4
- **Java-Dateien gesamt:** 4
- **Beispiele:** AbilityCombatAction.java, BasicAttackTargetCombatAction.java, CombatActionOption.java, StateCombatAction.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/npccombatactionevaluator/memory/`

- **Java-Dateien direkt:** 4
- **Java-Dateien gesamt:** 4
- **Beispiele:** DamageMemory.java, DamageMemorySystems.java, TargetMemory.java, TargetMemorySystems.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/npceditor/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** NPCEditorPlugin.java, NPCRoleAssetTypeHandler.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/parkour/`

- **Java-Dateien direkt:** 4
- **Java-Dateien gesamt:** 7
- **Beispiele:** ParkourCheckpoint.java, ParkourCheckpointSystems.java, ParkourCommand.java, ParkourPlugin.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/parkour/commands/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** CheckpointAddCommand.java, CheckpointRemoveCommand.java, CheckpointResetCommand.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/path/`

- **Java-Dateien direkt:** 6
- **Java-Dateien gesamt:** 29
- **Beispiele:** PathPlugin.java, PathSpatialSystem.java, PrefabPathCollection.java, PrefabPathSystems.java, WorldPathBuilder.java, WorldPathData.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/path/commands/`

- **Java-Dateien direkt:** 16
- **Java-Dateien gesamt:** 16
- **Beispiele:** PrefabPathAddCommand.java, PrefabPathCommand.java, PrefabPathEditCommand.java, PrefabPathHelper.java, PrefabPathListCommand.java, PrefabPathMergeCommand.java, PrefabPathNewCommand.java, PrefabPathNodesCommand.java …
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/path/entities/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** PatrolPathMarkerEntity.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/path/path/`

- **Java-Dateien direkt:** 4
- **Java-Dateien gesamt:** 4
- **Beispiele:** IPrefabPath.java, PatrolPath.java, TransientPath.java, TransientPathDefinition.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/path/waypoint/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** IPrefabPathWaypoint.java, RelativeWaypointDefinition.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/portals/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 53
- **Beispiele:** PortalsPlugin.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/portals/commands/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 7
- **Beispiele:** FragmentCommands.java, PortalWorldCommandBase.java, TimerFragmentCommand.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/portals/commands/player/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** LeaveCommand.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/portals/commands/utils/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** CursedHeldItemCommand.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/portals/commands/voidevent/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** StartVoidEventCommand.java, VoidEventCommands.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/portals/components/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 7
- **Beispiele:** PortalDevice.java, PortalDeviceConfig.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/portals/components/voidevent/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 5
- **Beispiele:** VoidEvent.java, VoidSpawner.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/portals/components/voidevent/config/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** InvasionPortalConfig.java, VoidEventConfig.java, VoidEventStage.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/portals/integrations/`

- **Java-Dateien direkt:** 4
- **Java-Dateien gesamt:** 4
- **Beispiele:** FragmentOriginGameplayConfig.java, PortalGameplayConfig.java, PortalMarkerProvider.java, PortalRemovalCondition.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/portals/interactions/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** EnterPortalInteraction.java, ReturnPortalInteraction.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/portals/resources/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** PortalWorld.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/portals/systems/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 12
- **Beispiele:** CloseWorldWhenBreakingDeviceSystems.java, PortalInvalidDestinationSystem.java, PortalTrackerSystems.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/portals/systems/curse/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** CurseItemDropsSystem.java, DeleteCursedItemsOnSpawnSystem.java, DiedInPortalSystem.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/portals/systems/voidevent/`

- **Java-Dateien direkt:** 6
- **Java-Dateien gesamt:** 6
- **Beispiele:** StartVoidEventInFragmentSystem.java, VoidEventPlayerJoinSystem.java, VoidEventRefSystem.java, VoidEventStagesSystem.java, VoidInvasionPortalsSpawnSystem.java, VoidSpawnerSystems.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/portals/ui/`

- **Java-Dateien direkt:** 4
- **Java-Dateien gesamt:** 4
- **Beispiele:** PortalDeviceActivePage.java, PortalDevicePageSupplier.java, PortalDeviceSummonPage.java, PortalSpawnFinder.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/portals/utils/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 15
- **Beispiele:** BlockTypeUtils.java, CursedItems.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/portals/utils/posqueries/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 12
- **Beispiele:** PositionPredicate.java, SpatialQuery.java, SpatialQueryDebug.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/portals/utils/posqueries/generators/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** SearchBelow.java, SearchCircular.java, SearchCone.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/portals/utils/posqueries/predicates/`

- **Java-Dateien direkt:** 4
- **Java-Dateien gesamt:** 6
- **Beispiele:** FitsAPortal.java, NotNearAnyInHashGrid.java, NotNearPoint.java, NotNearPointXZ.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/portals/utils/posqueries/predicates/generic/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** FilterQuery.java, FlatMapQuery.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/portals/utils/spatial/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** SpatialHashGrid.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/randomtick/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 5
- **Beispiele:** RandomTick.java, RandomTickPlugin.java, RandomTickSystem.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/randomtick/procedures/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** ChangeIntoBlockProcedure.java, SpreadToProcedure.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/safetyroll/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** SafetyRollPlugin.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/sprintforce/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** SprintForcePlugin.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/tagset/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 4
- **Beispiele:** TagSet.java, TagSetLookupTable.java, TagSetPlugin.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/tagset/config/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** NPCGroup.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/teleport/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 26
- **Beispiele:** TeleportPlugin.java, Warp.java, WarpListPage.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/teleport/commands/`

- **Java-Dateien direkt:** 0
- **Java-Dateien gesamt:** 22
- **Beispiele:** keine direkten .java Dateien
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/teleport/commands/teleport/`

- **Java-Dateien direkt:** 11
- **Java-Dateien gesamt:** 15
- **Beispiele:** SpawnCommand.java, SpawnSetCommand.java, SpawnSetDefaultCommand.java, TeleportAllCommand.java, TeleportBackCommand.java, TeleportCommand.java, TeleportForwardCommand.java, TeleportHistoryCommand.java …
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/teleport/commands/teleport/variant/`

- **Java-Dateien direkt:** 4
- **Java-Dateien gesamt:** 4
- **Beispiele:** TeleportOtherToPlayerCommand.java, TeleportPlayerToCoordinatesCommand.java, TeleportToCoordinatesCommand.java, TeleportToPlayerCommand.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/teleport/commands/warp/`

- **Java-Dateien direkt:** 7
- **Java-Dateien gesamt:** 7
- **Beispiele:** WarpCommand.java, WarpGoCommand.java, WarpGoVariantCommand.java, WarpListCommand.java, WarpReloadCommand.java, WarpRemoveCommand.java, WarpSetCommand.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/teleport/components/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** TeleportHistory.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/triggervolumes/`

- **Java-Dateien direkt:** 4
- **Java-Dateien gesamt:** 68
- **Beispiele:** AssetSourceProvider.java, EntityTargetType.java, TriggerVolumeToolPacketHandler.java, TriggerVolumesPlugin.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/triggervolumes/asset/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** TriggerEffectAsset.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/triggervolumes/command/`

- **Java-Dateien direkt:** 18
- **Java-Dateien gesamt:** 18
- **Beispiele:** TriggerVolumeArgTypes.java, TriggerVolumeAssignEffectCommand.java, TriggerVolumeAssignGroupEffectCommand.java, TriggerVolumeBrowseCommand.java, TriggerVolumeCommand.java, TriggerVolumeCreateCommand.java, TriggerVolumeDisableCommand.java, TriggerVolumeDisableTagCommand.java …
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/triggervolumes/component/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** TriggerVolume.java, TriggerVolumeGroup.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/triggervolumes/effect/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 21
- **Beispiele:** TriggerContext.java, TriggerEffect.java, TriggerEventType.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/triggervolumes/effect/builtin/`

- **Java-Dateien direkt:** 18
- **Java-Dateien gesamt:** 18
- **Beispiele:** ConditionalEffect.java, ControlDoorsEffect.java, DamageEntityEffect.java, DestroyVolumeEffect.java, EntityEffectEffect.java, GiveItemEffect.java, PastePrefabEffect.java, PlaySoundEffect.java …
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/triggervolumes/event/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** TriggerVolumeEvent.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/triggervolumes/interaction/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** DestroyTaggedVolumesInteraction.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/triggervolumes/manager/`

- **Java-Dateien direkt:** 4
- **Java-Dateien gesamt:** 4
- **Beispiele:** CooldownMode.java, GroupEntry.java, TriggerVolumeManager.java, VolumeEntry.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/triggervolumes/prefab/`

- **Java-Dateien direkt:** 6
- **Java-Dateien gesamt:** 6
- **Beispiele:** TriggerVolumeChunkRegenSystem.java, TriggerVolumeGroupWorldGenHandler.java, TriggerVolumePasteHandler.java, TriggerVolumePrefabContributor.java, TriggerVolumePrefabPasteRemapSystem.java, TriggerVolumeWorldGenHandler.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/triggervolumes/shape/`

- **Java-Dateien direkt:** 4
- **Java-Dateien gesamt:** 4
- **Beispiele:** BoxShape.java, CylinderShape.java, SphereShape.java, TriggerVolumeShape.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/triggervolumes/snapshot/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** TriggerVolumeSnapshot.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/triggervolumes/system/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** DelayedEffectScheduler.java, TriggerVolumeTickingSystem.java, VolumeSpatialIndex.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/triggervolumes/ui/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** TriggerVolumeBrowsePage.java, TriggerVolumeEffectEditorPage.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/weather/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 8
- **Beispiele:** WeatherPlugin.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/weather/commands/`

- **Java-Dateien direkt:** 4
- **Java-Dateien gesamt:** 4
- **Beispiele:** WeatherCommand.java, WeatherGetCommand.java, WeatherResetCommand.java, WeatherSetCommand.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/weather/components/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** WeatherTracker.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/weather/resources/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** WeatherResource.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/weather/systems/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** WeatherSystem.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/worldgen/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 33
- **Beispiele:** WorldGenPlugin.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/worldgen/modifier/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 32
- **Beispiele:** EventHandler.java, Target.java, WorldGenModifier.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/worldgen/modifier/content/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 23
- **Beispiele:** Codecs.java, Content.java, FileContent.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/worldgen/modifier/content/cave/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 3
- **Beispiele:** CaveTypeContent.java, CaveTypeGenerator.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/worldgen/modifier/content/cave/ore/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** OreCluster.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/worldgen/modifier/content/common/`

- **Java-Dateien direkt:** 5
- **Java-Dateien gesamt:** 5
- **Beispiele:** BlockEntry.java, BlockMask.java, HeightMask.java, NoiseMask.java, PointGrid.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/worldgen/modifier/content/cover/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** BiomeCoverContent.java, CaveCoverContent.java, CoverContent.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/worldgen/modifier/content/fluid/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** BiomeFluidContent.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/worldgen/modifier/content/layer/`

- **Java-Dateien direkt:** 4
- **Java-Dateien gesamt:** 4
- **Beispiele:** BiomeDynamicLayerContent.java, BiomeStaticLayerContent.java, LayerContent.java, NoiseBlockEntry.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/worldgen/modifier/content/prefab/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** BiomePrefabContent.java, CavePrefabContent.java, PrefabContent.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/worldgen/modifier/content/tint/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** BiomeTintContent.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/worldgen/modifier/event/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** ModifyEvent.java, ModifyEvents.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/builtin/worldgen/modifier/op/`

- **Java-Dateien direkt:** 4
- **Java-Dateien gesamt:** 4
- **Beispiele:** AddOp.java, LogOp.java, Op.java, RemoveOp.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/codec/`

- **Java-Dateien direkt:** 12
- **Java-Dateien gesamt:** 127
- **Beispiele:** Codec.java, DirectDecodeCodec.java, DocumentContainingCodec.java, EmptyExtraInfo.java, ExtraInfo.java, InheritCodec.java, KeyedCodec.java, PrimitiveCodec.java …
- **Was passiert hier?** Codec-System: Daten aus JSON/Assets lesen und schreiben.
- **Hilft beim Modding?** Wichtig, wenn du eigene config-/assetbasierte Daten sauber laden willst.

### `com/hypixel/hytale/codec/builder/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** BuilderCodec.java, BuilderField.java, StringTreeMap.java
- **Was passiert hier?** Codec-System: Daten aus JSON/Assets lesen und schreiben.
- **Hilft beim Modding?** Wichtig, wenn du eigene config-/assetbasierte Daten sauber laden willst.

### `com/hypixel/hytale/codec/codecs/`

- **Java-Dateien direkt:** 5
- **Java-Dateien gesamt:** 30
- **Beispiele:** BsonDocumentCodec.java, EnumCodec.java, InetSocketAddressCodec.java, StringIntegerCodec.java, UUIDBinaryCodec.java
- **Was passiert hier?** Codec-System: Daten aus JSON/Assets lesen und schreiben.
- **Hilft beim Modding?** Wichtig, wenn du eigene config-/assetbasierte Daten sauber laden willst.

### `com/hypixel/hytale/codec/codecs/array/`

- **Java-Dateien direkt:** 5
- **Java-Dateien gesamt:** 5
- **Beispiele:** ArrayCodec.java, DoubleArrayCodec.java, FloatArrayCodec.java, IntArrayCodec.java, LongArrayCodec.java
- **Was passiert hier?** Codec-System: Daten aus JSON/Assets lesen und schreiben.
- **Hilft beim Modding?** Wichtig, wenn du eigene config-/assetbasierte Daten sauber laden willst.

### `com/hypixel/hytale/codec/codecs/map/`

- **Java-Dateien direkt:** 10
- **Java-Dateien gesamt:** 10
- **Beispiele:** EnumMapCodec.java, Float2ObjectMapCodec.java, Int2ObjectMapCodec.java, MapCodec.java, MergedEnumMapCodec.java, Object2DoubleMapCodec.java, Object2FloatMapCodec.java, Object2IntMapCodec.java …
- **Was passiert hier?** Codec-System: Daten aus JSON/Assets lesen und schreiben.
- **Hilft beim Modding?** Wichtig, wenn du eigene config-/assetbasierte Daten sauber laden willst.

### `com/hypixel/hytale/codec/codecs/set/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** SetCodec.java
- **Was passiert hier?** Codec-System: Daten aus JSON/Assets lesen und schreiben.
- **Hilft beim Modding?** Wichtig, wenn du eigene config-/assetbasierte Daten sauber laden willst.

### `com/hypixel/hytale/codec/codecs/simple/`

- **Java-Dateien direkt:** 9
- **Java-Dateien gesamt:** 9
- **Beispiele:** BooleanCodec.java, ByteCodec.java, DoubleCodec.java, FloatCodec.java, IntegerCodec.java, LongCodec.java, NullableBooleanCodec.java, ShortCodec.java …
- **Was passiert hier?** Codec-System: Daten aus JSON/Assets lesen und schreiben.
- **Hilft beim Modding?** Wichtig, wenn du eigene config-/assetbasierte Daten sauber laden willst.

### `com/hypixel/hytale/codec/exception/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** CodecException.java, CodecValidationException.java
- **Was passiert hier?** Codec-System: Daten aus JSON/Assets lesen und schreiben.
- **Hilft beim Modding?** Wichtig, wenn du eigene config-/assetbasierte Daten sauber laden willst.

### `com/hypixel/hytale/codec/function/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** BsonFunctionCodec.java, FunctionCodec.java
- **Was passiert hier?** Codec-System: Daten aus JSON/Assets lesen und schreiben.
- **Hilft beim Modding?** Wichtig, wenn du eigene config-/assetbasierte Daten sauber laden willst.

### `com/hypixel/hytale/codec/lookup/`

- **Java-Dateien direkt:** 9
- **Java-Dateien gesamt:** 9
- **Beispiele:** ACodecMapCodec.java, AMapProvidedMapCodec.java, BuilderCodecMapCodec.java, CodecMapCodec.java, MapKeyMapCodec.java, MapProvidedMapCodec.java, ObjectCodecMapCodec.java, Priority.java …
- **Was passiert hier?** Codec-System: Daten aus JSON/Assets lesen und schreiben.
- **Hilft beim Modding?** Wichtig, wenn du eigene config-/assetbasierte Daten sauber laden willst.

### `com/hypixel/hytale/codec/schema/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 28
- **Beispiele:** NamedSchema.java, SchemaContext.java, SchemaConvertable.java
- **Was passiert hier?** Codec-System: Daten aus JSON/Assets lesen und schreiben.
- **Hilft beim Modding?** Wichtig, wenn du eigene config-/assetbasierte Daten sauber laden willst.

### `com/hypixel/hytale/codec/schema/config/`

- **Java-Dateien direkt:** 8
- **Java-Dateien gesamt:** 8
- **Beispiele:** ArraySchema.java, BooleanSchema.java, IntegerSchema.java, NullSchema.java, NumberSchema.java, ObjectSchema.java, Schema.java, StringSchema.java
- **Was passiert hier?** Codec-System: Daten aus JSON/Assets lesen und schreiben.
- **Hilft beim Modding?** Wichtig, wenn du eigene config-/assetbasierte Daten sauber laden willst.

### `com/hypixel/hytale/codec/schema/metadata/`

- **Java-Dateien direkt:** 5
- **Java-Dateien gesamt:** 17
- **Beispiele:** AllowEmptyObject.java, HytaleType.java, Metadata.java, NoDefaultValue.java, VirtualPath.java
- **Was passiert hier?** Codec-System: Daten aus JSON/Assets lesen und schreiben.
- **Hilft beim Modding?** Wichtig, wenn du eigene config-/assetbasierte Daten sauber laden willst.

### `com/hypixel/hytale/codec/schema/metadata/ui/`

- **Java-Dateien direkt:** 12
- **Java-Dateien gesamt:** 12
- **Beispiele:** UIButton.java, UICreateButtons.java, UIDefaultCollapsedState.java, UIDisplayMode.java, UIEditor.java, UIEditorFeatures.java, UIEditorPreview.java, UIEditorSectionStart.java …
- **Was passiert hier?** Codec-System: Daten aus JSON/Assets lesen und schreiben.
- **Hilft beim Modding?** Wichtig, wenn du eigene config-/assetbasierte Daten sauber laden willst.

### `com/hypixel/hytale/codec/store/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** CodecKey.java, CodecStore.java, StoredCodec.java
- **Was passiert hier?** Codec-System: Daten aus JSON/Assets lesen und schreiben.
- **Hilft beim Modding?** Wichtig, wenn du eigene config-/assetbasierte Daten sauber laden willst.

### `com/hypixel/hytale/codec/util/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** Documentation.java, RawJsonReader.java
- **Was passiert hier?** Codec-System: Daten aus JSON/Assets lesen und schreiben.
- **Hilft beim Modding?** Wichtig, wenn du eigene config-/assetbasierte Daten sauber laden willst.

### `com/hypixel/hytale/codec/validation/`

- **Java-Dateien direkt:** 8
- **Java-Dateien gesamt:** 36
- **Beispiele:** LateValidator.java, LegacyValidator.java, ThrowingValidationResults.java, ValidatableCodec.java, ValidationResults.java, Validator.java, ValidatorCache.java, Validators.java
- **Was passiert hier?** Codec-System: Daten aus JSON/Assets lesen und schreiben.
- **Hilft beim Modding?** Wichtig, wenn du eigene config-/assetbasierte Daten sauber laden willst.

### `com/hypixel/hytale/codec/validation/validator/`

- **Java-Dateien direkt:** 28
- **Java-Dateien gesamt:** 28
- **Beispiele:** ArraySizeRangeValidator.java, ArraySizeValidator.java, ArrayValidator.java, DeprecatedValidator.java, DoubleArraySizeValidator.java, DoubleArrayValidator.java, EqualValidator.java, FloatArrayValidator.java …
- **Was passiert hier?** Codec-System: Daten aus JSON/Assets lesen und schreiben.
- **Hilft beim Modding?** Wichtig, wenn du eigene config-/assetbasierte Daten sauber laden willst.

### `com/hypixel/hytale/common/`

- **Java-Dateien direkt:** 0
- **Java-Dateien gesamt:** 49
- **Beispiele:** keine direkten .java Dateien
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/common/benchmark/`

- **Java-Dateien direkt:** 4
- **Java-Dateien gesamt:** 4
- **Beispiele:** ContinuousValueRecorder.java, DiscreteValueRecorder.java, TimeDistributionRecorder.java, TimeRecorder.java
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/common/collection/`

- **Java-Dateien direkt:** 5
- **Java-Dateien gesamt:** 5
- **Beispiele:** BucketItem.java, BucketItemPool.java, BucketList.java, Flag.java, Flags.java
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/common/fastutil/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** HLongOpenHashSet.java, HLongSet.java, HObjectOpenHashSet.java
- **Was passiert hier?** Performance-Hilfscode für schnelle Maps/Collections.
- **Hilft beim Modding?** Meist nur verstehen, nicht direkt für Mods anfassen.

### `com/hypixel/hytale/common/map/`

- **Java-Dateien direkt:** 4
- **Java-Dateien gesamt:** 4
- **Beispiele:** DefaultMap.java, IWeightedElement.java, IWeightedMap.java, WeightedMap.java
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/common/plugin/`

- **Java-Dateien direkt:** 5
- **Java-Dateien gesamt:** 5
- **Beispiele:** AuthorInfo.java, Mod.java, ModLoadOrderException.java, PluginIdentifier.java, PluginManifest.java
- **Was passiert hier?** Gemeinsame Plugin-Daten wie Manifest und Identifier.
- **Hilft beim Modding?** Wichtig für manifest.json: Group, Name, Main, ServerVersion, Dependencies.

### `com/hypixel/hytale/common/semver/`

- **Java-Dateien direkt:** 4
- **Java-Dateien gesamt:** 4
- **Beispiele:** Semver.java, SemverComparator.java, SemverRange.java, SemverSatisfies.java
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/common/thread/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 2
- **Beispiele:** HytaleForkJoinThreadFactory.java
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/common/thread/ticking/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** Tickable.java
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/common/tuple/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** BoolDoublePair.java, BoolIntPair.java
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/common/util/`

- **Java-Dateien direkt:** 19
- **Java-Dateien gesamt:** 20
- **Beispiele:** ArrayUtil.java, AudioUtil.java, BitSetUtil.java, BitUtil.java, CompletableFutureUtil.java, ExceptionUtil.java, FormatUtil.java, GCUtil.java …
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/common/util/java/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** ManifestUtil.java
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/component/`

- **Java-Dateien direkt:** 28
- **Java-Dateien gesamt:** 92
- **Beispiele:** AddReason.java, Archetype.java, ArchetypeChunk.java, CommandBuffer.java, Component.java, ComponentAccessor.java, ComponentRegistration.java, ComponentRegistry.java …
- **Was passiert hier?** ECS/Component-System: Daten hängen als Komponenten an Entities/Chunks/Systeme.
- **Hilft beim Modding?** Sehr wichtig: Hytale nutzt stark Component-Logik statt einfache Minecraft-Objekte.

### `com/hypixel/hytale/component/data/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 10
- **Beispiele:** ForEachTaskData.java
- **Was passiert hier?** ECS/Component-System: Daten hängen als Komponenten an Entities/Chunks/Systeme.
- **Hilft beim Modding?** Sehr wichtig: Hytale nutzt stark Component-Logik statt einfache Minecraft-Objekte.

### `com/hypixel/hytale/component/data/change/`

- **Java-Dateien direkt:** 7
- **Java-Dateien gesamt:** 7
- **Beispiele:** ChangeType.java, ComponentChange.java, DataChange.java, ResourceChange.java, SystemChange.java, SystemGroupChange.java, SystemTypeChange.java
- **Was passiert hier?** ECS/Component-System: Daten hängen als Komponenten an Entities/Chunks/Systeme.
- **Hilft beim Modding?** Sehr wichtig: Hytale nutzt stark Component-Logik statt einfache Minecraft-Objekte.

### `com/hypixel/hytale/component/data/unknown/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** TempUnknownComponent.java, UnknownComponents.java
- **Was passiert hier?** ECS/Component-System: Daten hängen als Komponenten an Entities/Chunks/Systeme.
- **Hilft beim Modding?** Sehr wichtig: Hytale nutzt stark Component-Logik statt einfache Minecraft-Objekte.

### `com/hypixel/hytale/component/dependency/`

- **Java-Dateien direkt:** 8
- **Java-Dateien gesamt:** 8
- **Beispiele:** Dependency.java, DependencyGraph.java, Order.java, OrderPriority.java, RootDependency.java, SystemDependency.java, SystemGroupDependency.java, SystemTypeDependency.java
- **Was passiert hier?** ECS/Component-System: Daten hängen als Komponenten an Entities/Chunks/Systeme.
- **Hilft beim Modding?** Sehr wichtig: Hytale nutzt stark Component-Logik statt einfache Minecraft-Objekte.

### `com/hypixel/hytale/component/event/`

- **Java-Dateien direkt:** 4
- **Java-Dateien gesamt:** 4
- **Beispiele:** EntityEventType.java, EntityHolderEventType.java, EventSystemType.java, WorldEventType.java
- **Was passiert hier?** ECS/Component-System: Daten hängen als Komponenten an Entities/Chunks/Systeme.
- **Hilft beim Modding?** Sehr wichtig: Hytale nutzt stark Component-Logik statt einfache Minecraft-Objekte.

### `com/hypixel/hytale/component/metric/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** ArchetypeChunkData.java, SystemMetricData.java
- **Was passiert hier?** ECS/Component-System: Daten hängen als Komponenten an Entities/Chunks/Systeme.
- **Hilft beim Modding?** Sehr wichtig: Hytale nutzt stark Component-Logik statt einfache Minecraft-Objekte.

### `com/hypixel/hytale/component/query/`

- **Java-Dateien direkt:** 7
- **Java-Dateien gesamt:** 7
- **Beispiele:** AndQuery.java, AnyQuery.java, ExactArchetypeQuery.java, NotQuery.java, OrQuery.java, Query.java, ReadWriteArchetypeQuery.java
- **Was passiert hier?** ECS/Component-System: Daten hängen als Komponenten an Entities/Chunks/Systeme.
- **Hilft beim Modding?** Sehr wichtig: Hytale nutzt stark Component-Logik statt einfache Minecraft-Objekte.

### `com/hypixel/hytale/component/spatial/`

- **Java-Dateien direkt:** 6
- **Java-Dateien gesamt:** 6
- **Beispiele:** KDTree.java, MortonCode.java, SpatialData.java, SpatialResource.java, SpatialStructure.java, SpatialSystem.java
- **Was passiert hier?** ECS/Component-System: Daten hängen als Komponenten an Entities/Chunks/Systeme.
- **Hilft beim Modding?** Sehr wichtig: Hytale nutzt stark Component-Logik statt einfache Minecraft-Objekte.

### `com/hypixel/hytale/component/system/`

- **Java-Dateien direkt:** 17
- **Java-Dateien gesamt:** 25
- **Beispiele:** ArchetypeChunkSystem.java, CancellableEcsEvent.java, DelayedSystem.java, EcsEvent.java, EntityEventSystem.java, EntityHolderEventSystem.java, EventSystem.java, HolderSystem.java …
- **Was passiert hier?** ECS/Component-System: Daten hängen als Komponenten an Entities/Chunks/Systeme.
- **Hilft beim Modding?** Sehr wichtig: Hytale nutzt stark Component-Logik statt einfache Minecraft-Objekte.

### `com/hypixel/hytale/component/system/data/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** ArchetypeDataSystem.java, EntityDataSystem.java
- **Was passiert hier?** ECS/Component-System: Daten hängen als Komponenten an Entities/Chunks/Systeme.
- **Hilft beim Modding?** Sehr wichtig: Hytale nutzt stark Component-Logik statt einfache Minecraft-Objekte.

### `com/hypixel/hytale/component/system/tick/`

- **Java-Dateien direkt:** 6
- **Java-Dateien gesamt:** 6
- **Beispiele:** ArchetypeTickingSystem.java, DelayedEntitySystem.java, EntityTickingSystem.java, RunWhenPausedSystem.java, TickableSystem.java, TickingSystem.java
- **Was passiert hier?** ECS/Component-System: Daten hängen als Komponenten an Entities/Chunks/Systeme.
- **Hilft beim Modding?** Sehr wichtig: Hytale nutzt stark Component-Logik statt einfache Minecraft-Objekte.

### `com/hypixel/hytale/component/task/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** ParallelRangeTask.java, ParallelTask.java
- **Was passiert hier?** ECS/Component-System: Daten hängen als Komponenten an Entities/Chunks/Systeme.
- **Hilft beim Modding?** Sehr wichtig: Hytale nutzt stark Component-Logik statt einfache Minecraft-Objekte.

### `com/hypixel/hytale/event/`

- **Java-Dateien direkt:** 15
- **Java-Dateien gesamt:** 15
- **Beispiele:** AsyncEventBusRegistry.java, EventBus.java, EventBusRegistry.java, EventPriority.java, EventRegistration.java, EventRegistry.java, IAsyncEvent.java, IBaseEvent.java …
- **Was passiert hier?** Event-System: Ereignisse registrieren und auslösen.
- **Hilft beim Modding?** Wichtig, um auf Server-Events zu reagieren, ohne überall Code zu patchen.

### `com/hypixel/hytale/function/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 37
- **Beispiele:** package-info.java
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/function/consumer/`

- **Java-Dateien direkt:** 12
- **Java-Dateien gesamt:** 12
- **Beispiele:** BiIntConsumer.java, BooleanConsumer.java, DoubleQuadObjectConsumer.java, FloatConsumer.java, IntBiObjectConsumer.java, IntObjectConsumer.java, IntTriObjectConsumer.java, QuadConsumer.java …
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/function/function/`

- **Java-Dateien direkt:** 10
- **Java-Dateien gesamt:** 10
- **Beispiele:** BiDoubleToDoubleFunction.java, BiIntToDoubleFunction.java, BiLongToDoubleFunction.java, BiToFloatFunction.java, QuadBoolFunction.java, ToFloatFunction.java, TriBoolFunction.java, TriFunction.java …
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/function/predicate/`

- **Java-Dateien direkt:** 12
- **Java-Dateien gesamt:** 12
- **Beispiele:** BiFloatPredicate.java, BiIntPredicate.java, Int3TriIntBiObjPredicate.java, LongTriIntBiObjPredicate.java, ObjectPositionBlockFunction.java, QuadObjectDoublePredicate.java, QuadPredicate.java, TriIntObjPredicate.java …
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/function/supplier/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** CachedSupplier.java, SupplierUtil.java
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/lib/`

- **Java-Dateien direkt:** 0
- **Java-Dateien gesamt:** 6
- **Beispiele:** keine direkten .java Dateien
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/lib/quiche/`

- **Java-Dateien direkt:** 6
- **Java-Dateien gesamt:** 6
- **Beispiele:** QuicheChannel.java, QuicheConnection.java, QuicheListener.java, QuicheNative.java, QuicheServerCredentials.java, QuicheUtil.java
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/logger/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 11
- **Beispiele:** HytaleLogger.java
- **Was passiert hier?** Logging-System.
- **Hilft beim Modding?** Wichtig für Debug-Ausgaben deiner Mod.

### `com/hypixel/hytale/logger/backend/`

- **Java-Dateien direkt:** 6
- **Java-Dateien gesamt:** 6
- **Beispiele:** HytaleConsole.java, HytaleFileHandler.java, HytaleLogFormatter.java, HytaleLogManager.java, HytaleLoggerBackend.java, HytaleUncaughtExceptionHandler.java
- **Was passiert hier?** Logging-System.
- **Hilft beim Modding?** Wichtig für Debug-Ausgaben deiner Mod.

### `com/hypixel/hytale/logger/sentry/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** HytaleSentryHandler.java, SkipSentryException.java
- **Was passiert hier?** Logging-System.
- **Hilft beim Modding?** Wichtig für Debug-Ausgaben deiner Mod.

### `com/hypixel/hytale/logger/util/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** GithubMessageUtil.java, LoggerPrintStream.java
- **Was passiert hier?** Logging-System.
- **Hilft beim Modding?** Wichtig für Debug-Ausgaben deiner Mod.

### `com/hypixel/hytale/math/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 74
- **Beispiele:** Axis.java, Range.java
- **Was passiert hier?** Mathe-Hilfen: Vektoren, Matrizen, Raycasts, Shapes, Hitdetection.
- **Hilft beim Modding?** Wichtig für Positionen, Blickrichtung, Marker und Entfernungen.

### `com/hypixel/hytale/math/block/`

- **Java-Dateien direkt:** 10
- **Java-Dateien gesamt:** 10
- **Beispiele:** BlockConeUtil.java, BlockCubeUtil.java, BlockCylinderUtil.java, BlockDiamondUtil.java, BlockDomeUtil.java, BlockInvertedDomeUtil.java, BlockPyramidUtil.java, BlockSphereUtil.java …
- **Was passiert hier?** Mathe-Hilfen: Vektoren, Matrizen, Raycasts, Shapes, Hitdetection.
- **Hilft beim Modding?** Wichtig für Positionen, Blickrichtung, Marker und Entfernungen.

### `com/hypixel/hytale/math/codec/`

- **Java-Dateien direkt:** 5
- **Java-Dateien gesamt:** 5
- **Beispiele:** FloatRangeArrayCodec.java, IntRangeArrayCodec.java, Vector2dArrayCodec.java, Vector3dArrayCodec.java, Vector3iArrayCodec.java
- **Was passiert hier?** Codec-System: Daten aus JSON/Assets lesen und schreiben.
- **Hilft beim Modding?** Wichtig, wenn du eigene config-/assetbasierte Daten sauber laden willst.

### `com/hypixel/hytale/math/data/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** Int3ObjectOpenHashMap.java, Int3OpenHashSet.java, VarInt.java
- **Was passiert hier?** Mathe-Hilfen: Vektoren, Matrizen, Raycasts, Shapes, Hitdetection.
- **Hilft beim Modding?** Wichtig für Positionen, Blickrichtung, Marker und Entfernungen.

### `com/hypixel/hytale/math/hitdetection/`

- **Java-Dateien direkt:** 5
- **Java-Dateien gesamt:** 8
- **Beispiele:** HitDetectionBuffer.java, HitDetectionExecutor.java, LineOfSightProvider.java, MatrixProvider.java, Vector4dBufferList.java
- **Was passiert hier?** Mathe-Hilfen: Vektoren, Matrizen, Raycasts, Shapes, Hitdetection.
- **Hilft beim Modding?** Wichtig für Positionen, Blickrichtung, Marker und Entfernungen.

### `com/hypixel/hytale/math/hitdetection/projection/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** FrustumProjectionProvider.java, OrthogonalProjectionProvider.java
- **Was passiert hier?** Mathe-Hilfen: Vektoren, Matrizen, Raycasts, Shapes, Hitdetection.
- **Hilft beim Modding?** Wichtig für Positionen, Blickrichtung, Marker und Entfernungen.

### `com/hypixel/hytale/math/hitdetection/view/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** DirectionViewProvider.java
- **Was passiert hier?** Mathe-Hilfen: Vektoren, Matrizen, Raycasts, Shapes, Hitdetection.
- **Hilft beim Modding?** Wichtig für Positionen, Blickrichtung, Marker und Entfernungen.

### `com/hypixel/hytale/math/iterator/`

- **Java-Dateien direkt:** 6
- **Java-Dateien gesamt:** 6
- **Beispiele:** BlockIterator.java, BoxBlockIterator.java, CircleIterator.java, CircleSpiralIterator.java, LineIterator.java, SpiralIterator.java
- **Was passiert hier?** Mathe-Hilfen: Vektoren, Matrizen, Raycasts, Shapes, Hitdetection.
- **Hilft beim Modding?** Wichtig für Positionen, Blickrichtung, Marker und Entfernungen.

### `com/hypixel/hytale/math/matrix/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** Matrix4dUtil.java
- **Was passiert hier?** Mathe-Hilfen: Vektoren, Matrizen, Raycasts, Shapes, Hitdetection.
- **Hilft beim Modding?** Wichtig für Positionen, Blickrichtung, Marker und Entfernungen.

### `com/hypixel/hytale/math/random/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** RandomExtra.java
- **Was passiert hier?** Mathe-Hilfen: Vektoren, Matrizen, Raycasts, Shapes, Hitdetection.
- **Hilft beim Modding?** Wichtig für Positionen, Blickrichtung, Marker und Entfernungen.

### `com/hypixel/hytale/math/range/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** FloatRange.java, IntRange.java, IntRangeBoundValidator.java
- **Was passiert hier?** Mathe-Hilfen: Vektoren, Matrizen, Raycasts, Shapes, Hitdetection.
- **Hilft beim Modding?** Wichtig für Positionen, Blickrichtung, Marker und Entfernungen.

### `com/hypixel/hytale/math/raycast/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** RaycastAABB.java
- **Was passiert hier?** Mathe-Hilfen: Vektoren, Matrizen, Raycasts, Shapes, Hitdetection.
- **Hilft beim Modding?** Wichtig für Positionen, Blickrichtung, Marker und Entfernungen.

### `com/hypixel/hytale/math/shape/`

- **Java-Dateien direkt:** 13
- **Java-Dateien gesamt:** 13
- **Beispiele:** Box.java, Box2D.java, Cylinder.java, Ellipsoid.java, OriginShape.java, Quad2d.java, Quad4d.java, Rectangle.java …
- **Was passiert hier?** Mathe-Hilfen: Vektoren, Matrizen, Raycasts, Shapes, Hitdetection.
- **Hilft beim Modding?** Wichtig für Positionen, Blickrichtung, Marker und Entfernungen.

### `com/hypixel/hytale/math/util/`

- **Java-Dateien direkt:** 7
- **Java-Dateien gesamt:** 7
- **Beispiele:** ChunkUtil.java, FastRandom.java, HashUtil.java, MathUtil.java, NearestBlockUtil.java, NumberUtil.java, TrigMathUtil.java
- **Was passiert hier?** Mathe-Hilfen: Vektoren, Matrizen, Raycasts, Shapes, Hitdetection.
- **Hilft beim Modding?** Wichtig für Positionen, Blickrichtung, Marker und Entfernungen.

### `com/hypixel/hytale/math/vector/`

- **Java-Dateien direkt:** 14
- **Java-Dateien gesamt:** 14
- **Beispiele:** Location.java, Rotation3f.java, Rotation3fc.java, Transform.java, Vector2dUtil.java, Vector2fUtil.java, Vector2iUtil.java, Vector3LUtil.java …
- **Was passiert hier?** Mathe-Hilfen: Vektoren, Matrizen, Raycasts, Shapes, Hitdetection.
- **Hilft beim Modding?** Wichtig für Positionen, Blickrichtung, Marker und Entfernungen.

### `com/hypixel/hytale/metrics/`

- **Java-Dateien direkt:** 6
- **Java-Dateien gesamt:** 10
- **Beispiele:** ExecutorMetricsRegistry.java, InitStackThread.java, JVMMetrics.java, MetricProvider.java, MetricResults.java, MetricsRegistry.java
- **Was passiert hier?** Metriken/Performance-Beobachtung.
- **Hilft beim Modding?** Später nützlich, wenn du Serverlast messen willst.

### `com/hypixel/hytale/metrics/metric/`

- **Java-Dateien direkt:** 4
- **Java-Dateien gesamt:** 4
- **Beispiele:** AverageCollector.java, HistoricMetric.java, Metric.java, SynchronizedAverageCollector.java
- **Was passiert hier?** Metriken/Performance-Beobachtung.
- **Hilft beim Modding?** Später nützlich, wenn du Serverlast messen willst.

### `com/hypixel/hytale/plugin/`

- **Java-Dateien direkt:** 0
- **Java-Dateien gesamt:** 3
- **Beispiele:** keine direkten .java Dateien
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/plugin/early/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** ClassTransformer.java, EarlyPluginLoader.java, TransformingClassLoader.java
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/procedurallib/`

- **Java-Dateien direkt:** 5
- **Java-Dateien gesamt:** 151
- **Beispiele:** NoiseFunction.java, NoiseFunction2d.java, NoiseFunction3d.java, NoiseFunctionPair.java, NoiseType.java
- **Was passiert hier?** Prozedurale Hilfen: Noise, Zufall, Bedingungen, JSON-Logik.
- **Hilft beim Modding?** Wichtig für Worldgen und zufällige Platzierung.

### `com/hypixel/hytale/procedurallib/condition/`

- **Java-Dateien direkt:** 21
- **Java-Dateien gesamt:** 21
- **Beispiele:** BasicHeightThresholdInterpreter.java, ConstantBlockFluidCondition.java, ConstantIntCondition.java, DefaultCoordinateCondition.java, DefaultCoordinateRndCondition.java, DefaultDoubleCondition.java, DefaultDoubleThresholdCondition.java, DoubleThreshold.java …
- **Was passiert hier?** Prozedurale Hilfen: Noise, Zufall, Bedingungen, JSON-Logik.
- **Hilft beim Modding?** Wichtig für Worldgen und zufällige Platzierung.

### `com/hypixel/hytale/procedurallib/file/`

- **Java-Dateien direkt:** 4
- **Java-Dateien gesamt:** 4
- **Beispiele:** AssetLoader.java, AssetPath.java, FileIO.java, FileIOSystem.java
- **Was passiert hier?** Prozedurale Hilfen: Noise, Zufall, Bedingungen, JSON-Logik.
- **Hilft beim Modding?** Wichtig für Worldgen und zufällige Platzierung.

### `com/hypixel/hytale/procedurallib/json/`

- **Java-Dateien direkt:** 38
- **Java-Dateien gesamt:** 38
- **Beispiele:** AbstractCellJitterJsonLoader.java, BasicHeightThresholdInterpreterJsonLoader.java, BlendNoisePropertyJsonLoader.java, BranchNoiseJsonLoader.java, CellBorderDistanceFunctionJsonLoader.java, CellDistanceFunctionJsonLoader.java, CellNoiseJsonLoader.java, ConstantNoiseJsonLoader.java …
- **Was passiert hier?** Prozedurale Hilfen: Noise, Zufall, Bedingungen, JSON-Logik.
- **Hilft beim Modding?** Wichtig für Worldgen und zufällige Platzierung.

### `com/hypixel/hytale/procedurallib/logic/`

- **Java-Dateien direkt:** 16
- **Java-Dateien gesamt:** 42
- **Beispiele:** BranchNoise.java, CellNoise.java, CellularNoise.java, ConstantNoise.java, DistanceNoise.java, DoubleArray.java, GeneralNoise.java, GridNoise.java …
- **Was passiert hier?** Prozedurale Hilfen: Noise, Zufall, Bedingungen, JSON-Logik.
- **Hilft beim Modding?** Wichtig für Worldgen und zufällige Platzierung.

### `com/hypixel/hytale/procedurallib/logic/cell/`

- **Java-Dateien direkt:** 9
- **Java-Dateien gesamt:** 20
- **Beispiele:** BorderDistanceFunction.java, CellDistanceFunction.java, CellPointFunction.java, CellType.java, DistanceCalculationMode.java, GridCellDistanceFunction.java, HexCellDistanceFunction.java, MeasurementMode.java …
- **Was passiert hier?** Prozedurale Hilfen: Noise, Zufall, Bedingungen, JSON-Logik.
- **Hilft beim Modding?** Wichtig für Worldgen und zufällige Platzierung.

### `com/hypixel/hytale/procedurallib/logic/cell/evaluator/`

- **Java-Dateien direkt:** 8
- **Java-Dateien gesamt:** 8
- **Beispiele:** BorderPointEvaluator.java, BranchEvaluator.java, DensityPointEvaluator.java, DistancePointEvaluator.java, JitterPointEvaluator.java, NormalPointEvaluator.java, PointEvaluator.java, SkipCellPointEvaluator.java
- **Was passiert hier?** Prozedurale Hilfen: Noise, Zufall, Bedingungen, JSON-Logik.
- **Hilft beim Modding?** Wichtig für Worldgen und zufällige Platzierung.

### `com/hypixel/hytale/procedurallib/logic/cell/jitter/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** CellJitter.java, ConstantCellJitter.java, DefaultCellJitter.java
- **Was passiert hier?** Prozedurale Hilfen: Noise, Zufall, Bedingungen, JSON-Logik.
- **Hilft beim Modding?** Wichtig für Worldgen und zufällige Platzierung.

### `com/hypixel/hytale/procedurallib/logic/point/`

- **Java-Dateien direkt:** 6
- **Java-Dateien gesamt:** 6
- **Beispiele:** DistortedPointGenerator.java, IPointGenerator.java, OffsetPointGenerator.java, PointConsumer.java, PointGenerator.java, ScaledPointGenerator.java
- **Was passiert hier?** Prozedurale Hilfen: Noise, Zufall, Bedingungen, JSON-Logik.
- **Hilft beim Modding?** Wichtig für Worldgen und zufällige Platzierung.

### `com/hypixel/hytale/procedurallib/property/`

- **Java-Dateien direkt:** 18
- **Java-Dateien gesamt:** 18
- **Beispiele:** BlendNoiseProperty.java, CurveNoiseProperty.java, DistortedNoiseProperty.java, FractalNoiseProperty.java, GradientNoiseProperty.java, InvertNoiseProperty.java, MaxNoiseProperty.java, MinNoiseProperty.java …
- **Was passiert hier?** Prozedurale Hilfen: Noise, Zufall, Bedingungen, JSON-Logik.
- **Hilft beim Modding?** Wichtig für Worldgen und zufällige Platzierung.

### `com/hypixel/hytale/procedurallib/random/`

- **Java-Dateien direkt:** 5
- **Java-Dateien gesamt:** 5
- **Beispiele:** CoordinateOriginRotator.java, CoordinateRandomizer.java, CoordinateRotator.java, ICoordinateRandomizer.java, RotatedCoordinateRandomizer.java
- **Was passiert hier?** Prozedurale Hilfen: Noise, Zufall, Bedingungen, JSON-Logik.
- **Hilft beim Modding?** Wichtig für Worldgen und zufällige Platzierung.

### `com/hypixel/hytale/procedurallib/supplier/`

- **Java-Dateien direkt:** 17
- **Java-Dateien gesamt:** 17
- **Beispiele:** ConstantDoubleCoordinateHashSupplier.java, ConstantFloatCoordinateHashSupplier.java, DoubleRange.java, DoubleRangeCoordinateHashSupplier.java, DoubleRangeNoiseSupplier.java, FloatRange.java, FloatRangeNoiseSupplier.java, FloatSupplier.java …
- **Was passiert hier?** Prozedurale Hilfen: Noise, Zufall, Bedingungen, JSON-Logik.
- **Hilft beim Modding?** Wichtig für Worldgen und zufällige Platzierung.

### `com/hypixel/hytale/procedurallib/util/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** IntToIntFunction.java
- **Was passiert hier?** Prozedurale Hilfen: Noise, Zufall, Bedingungen, JSON-Logik.
- **Hilft beim Modding?** Wichtig für Worldgen und zufällige Platzierung.

### `com/hypixel/hytale/protocol/`

- **Java-Dateien direkt:** 426
- **Java-Dateien gesamt:** 885
- **Beispiele:** AOECircleSelector.java, AOECylinderSelector.java, AbilityEffects.java, AccumulationMode.java, ActiveAnimationsUpdate.java, AmbienceFX.java, AmbienceFXAltitude.java, AmbienceFXAmbientBed.java …
- **Was passiert hier?** Netzwerk-/Datenmodelle zwischen Server und Client.
- **Hilft beim Modding?** Eher fortgeschritten; nützlich zum Verstehen von Datenpaketen, aber nicht erster Einstieg.

### `com/hypixel/hytale/protocol/io/`

- **Java-Dateien direkt:** 10
- **Java-Dateien gesamt:** 13
- **Beispiele:** ChannelConnection.java, ConnectionHandler.java, NoopPacketStatsRecorder.java, PacketIO.java, PacketStatsRecorder.java, ProtocolException.java, ServerListener.java, ValidationResult.java …
- **Was passiert hier?** Netzwerk-/Datenmodelle zwischen Server und Client.
- **Hilft beim Modding?** Eher fortgeschritten; nützlich zum Verstehen von Datenpaketen, aber nicht erster Einstieg.

### `com/hypixel/hytale/protocol/io/netty/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** PacketDecoder.java, PacketEncoder.java, ProtocolUtil.java
- **Was passiert hier?** Netzwerk-/Datenmodelle zwischen Server und Client.
- **Hilft beim Modding?** Eher fortgeschritten; nützlich zum Verstehen von Datenpaketen, aber nicht erster Einstieg.

### `com/hypixel/hytale/protocol/packets/`

- **Java-Dateien direkt:** 0
- **Java-Dateien gesamt:** 446
- **Beispiele:** keine direkten .java Dateien
- **Was passiert hier?** Netzwerk-/Datenmodelle zwischen Server und Client.
- **Hilft beim Modding?** Eher fortgeschritten; nützlich zum Verstehen von Datenpaketen, aber nicht erster Einstieg.

### `com/hypixel/hytale/protocol/packets/asseteditor/`

- **Java-Dateien direkt:** 72
- **Java-Dateien gesamt:** 72
- **Beispiele:** AssetEditorActivateButton.java, AssetEditorAsset.java, AssetEditorAssetListSetup.java, AssetEditorAssetListUpdate.java, AssetEditorAssetPackSetup.java, AssetEditorAssetType.java, AssetEditorAssetUpdated.java, AssetEditorAuthorization.java …
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/protocol/packets/assets/`

- **Java-Dateien direkt:** 50
- **Java-Dateien gesamt:** 50
- **Beispiele:** TrackOrUpdateObjective.java, UntrackObjective.java, UpdateAmbienceFX.java, UpdateAudioCategories.java, UpdateAudioStates.java, UpdateBlockBreakingDecals.java, UpdateBlockGroups.java, UpdateBlockHitboxes.java …
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/protocol/packets/auth/`

- **Java-Dateien direkt:** 8
- **Java-Dateien gesamt:** 8
- **Beispiele:** AuthGrant.java, AuthToken.java, ClientReferral.java, ConnectAccept.java, PasswordAccepted.java, PasswordRejected.java, PasswordResponse.java, ServerAuthToken.java
- **Was passiert hier?** Netzwerk-/Datenmodelle zwischen Server und Client.
- **Hilft beim Modding?** Eher fortgeschritten; nützlich zum Verstehen von Datenpaketen, aber nicht erster Einstieg.

### `com/hypixel/hytale/protocol/packets/buildertools/`

- **Java-Dateien direkt:** 52
- **Java-Dateien gesamt:** 52
- **Beispiele:** Axis.java, BrushAxis.java, BrushOrigin.java, BrushShape.java, BuilderToolAction.java, BuilderToolArg.java, BuilderToolArgType.java, BuilderToolArgUpdate.java …
- **Was passiert hier?** Netzwerk-/Datenmodelle zwischen Server und Client.
- **Hilft beim Modding?** Eher fortgeschritten; nützlich zum Verstehen von Datenpaketen, aber nicht erster Einstieg.

### `com/hypixel/hytale/protocol/packets/camera/`

- **Java-Dateien direkt:** 4
- **Java-Dateien gesamt:** 4
- **Beispiele:** CameraShakeEffect.java, RequestFlyCameraMode.java, SetFlyCameraMode.java, SetServerCamera.java
- **Was passiert hier?** Netzwerk-/Datenmodelle zwischen Server und Client.
- **Hilft beim Modding?** Eher fortgeschritten; nützlich zum Verstehen von Datenpaketen, aber nicht erster Einstieg.

### `com/hypixel/hytale/protocol/packets/connection/`

- **Java-Dateien direkt:** 12
- **Java-Dateien gesamt:** 12
- **Beispiele:** ClientDisconnect.java, ClientDisconnectReason.java, ClientType.java, Connect.java, DisconnectType.java, InsecurePlayerOptions.java, Ping.java, Pong.java …
- **Was passiert hier?** Netzwerk-/Datenmodelle zwischen Server und Client.
- **Hilft beim Modding?** Eher fortgeschritten; nützlich zum Verstehen von Datenpaketen, aber nicht erster Einstieg.

### `com/hypixel/hytale/protocol/packets/entities/`

- **Java-Dateien direkt:** 8
- **Java-Dateien gesamt:** 8
- **Beispiele:** ApplyKnockback.java, ChangeVelocity.java, EntityUpdates.java, MountMovement.java, PlayAnimation.java, PlayEmote.java, SetEntitySeed.java, SpawnModelParticles.java
- **Was passiert hier?** Entity-System: Spieler, Kreaturen, Komponenten, Schaden, Movement, Stats.
- **Hilft beim Modding?** Wichtig für NPCs, Spielerinteraktion und eigene Entity-Logik.

### `com/hypixel/hytale/protocol/packets/interaction/`

- **Java-Dateien direkt:** 6
- **Java-Dateien gesamt:** 6
- **Beispiele:** CancelInteractionChain.java, DismountNPC.java, MountNPC.java, PlayInteractionFor.java, SyncInteractionChain.java, SyncInteractionChains.java
- **Was passiert hier?** Interaktionen: Klicks, Items, Blocks, Chains und Gameplay-Aktionen.
- **Hilft beim Modding?** Wichtig, wenn Spieler mit Blöcken/NPCs deiner Mod interagieren sollen.

### `com/hypixel/hytale/protocol/packets/interface_/`

- **Java-Dateien direkt:** 78
- **Java-Dateien gesamt:** 78
- **Beispiele:** AddToServerPlayerList.java, ArgCacheInvalidation.java, ArgValuesRequest.java, ArgValuesResponse.java, BlockChange.java, ChatMessage.java, ChatTagType.java, ChatType.java …
- **Was passiert hier?** Netzwerk-/Datenmodelle zwischen Server und Client.
- **Hilft beim Modding?** Eher fortgeschritten; nützlich zum Verstehen von Datenpaketen, aber nicht erster Einstieg.

### `com/hypixel/hytale/protocol/packets/inventory/`

- **Java-Dateien direkt:** 10
- **Java-Dateien gesamt:** 10
- **Beispiele:** DropCreativeItem.java, DropItemStack.java, InventoryAction.java, MoveItemStack.java, SetActiveSlot.java, SetCreativeItem.java, SmartGiveCreativeItem.java, SmartMoveItemStack.java …
- **Was passiert hier?** Inventar-/Item-System.
- **Hilft beim Modding?** Wichtig für Belohnungen, Kosten, Shop, Energy Crystals oder NPC-Handel.

### `com/hypixel/hytale/protocol/packets/machinima/`

- **Java-Dateien direkt:** 4
- **Java-Dateien gesamt:** 4
- **Beispiele:** RequestMachinimaActorModel.java, SceneUpdateType.java, SetMachinimaActorModel.java, UpdateMachinimaScene.java
- **Was passiert hier?** Netzwerk-/Datenmodelle zwischen Server und Client.
- **Hilft beim Modding?** Eher fortgeschritten; nützlich zum Verstehen von Datenpaketen, aber nicht erster Einstieg.

### `com/hypixel/hytale/protocol/packets/player/`

- **Java-Dateien direkt:** 45
- **Java-Dateien gesamt:** 45
- **Beispiele:** AddOrUpdateTriggerVolumeDisplay.java, ClearDebugShapes.java, ClientMovement.java, ClientPlaceBlock.java, ClientReady.java, ClientTeleport.java, DamageInfo.java, DisplayDebug.java …
- **Was passiert hier?** Netzwerk-/Datenmodelle zwischen Server und Client.
- **Hilft beim Modding?** Eher fortgeschritten; nützlich zum Verstehen von Datenpaketen, aber nicht erster Einstieg.

### `com/hypixel/hytale/protocol/packets/serveraccess/`

- **Java-Dateien direkt:** 4
- **Java-Dateien gesamt:** 4
- **Beispiele:** Access.java, RequestServerAccess.java, SetServerAccess.java, UpdateServerAccess.java
- **Was passiert hier?** Netzwerk-/Datenmodelle zwischen Server und Client.
- **Hilft beim Modding?** Eher fortgeschritten; nützlich zum Verstehen von Datenpaketen, aber nicht erster Einstieg.

### `com/hypixel/hytale/protocol/packets/setup/`

- **Java-Dateien direkt:** 16
- **Java-Dateien gesamt:** 16
- **Beispiele:** AssetFinalize.java, AssetInitialize.java, AssetPart.java, ClientFeature.java, RemoveAssets.java, RequestAssets.java, RequestCommonAssetsRebuild.java, ServerTags.java …
- **Was passiert hier?** Netzwerk-/Datenmodelle zwischen Server und Client.
- **Hilft beim Modding?** Eher fortgeschritten; nützlich zum Verstehen von Datenpaketen, aber nicht erster Einstieg.

### `com/hypixel/hytale/protocol/packets/stream/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** StreamOpen.java, StreamOpenResponse.java, StreamType.java
- **Was passiert hier?** Netzwerk-/Datenmodelle zwischen Server und Client.
- **Hilft beim Modding?** Eher fortgeschritten; nützlich zum Verstehen von Datenpaketen, aber nicht erster Einstieg.

### `com/hypixel/hytale/protocol/packets/voice/`

- **Java-Dateien direkt:** 4
- **Java-Dateien gesamt:** 4
- **Beispiele:** RelayedVoiceData.java, VoiceCodec.java, VoiceConfig.java, VoiceData.java
- **Was passiert hier?** Netzwerk-/Datenmodelle zwischen Server und Client.
- **Hilft beim Modding?** Eher fortgeschritten; nützlich zum Verstehen von Datenpaketen, aber nicht erster Einstieg.

### `com/hypixel/hytale/protocol/packets/window/`

- **Java-Dateien direkt:** 16
- **Java-Dateien gesamt:** 16
- **Beispiele:** CancelCraftingAction.java, ChangeBlockAction.java, ClientOpenWindow.java, CloseWindow.java, CraftItemAction.java, CraftRecipeAction.java, OpenWindow.java, SelectSlotAction.java …
- **Was passiert hier?** UI/Page-System für servergesteuerte Oberflächen.
- **Hilft beim Modding?** Wichtig für spätere Mod-Menüs, Admin-Seiten oder NPC-Dialoge.

### `com/hypixel/hytale/protocol/packets/world/`

- **Java-Dateien direkt:** 37
- **Java-Dateien gesamt:** 37
- **Beispiele:** ClearEditorTimeOverride.java, PaletteType.java, PlaySoundEvent2D.java, PlaySoundEvent3D.java, PlaySoundEventEntity.java, PlaySoundEventLocalPlayer.java, RotationAxis.java, RotationDirection.java …
- **Was passiert hier?** Netzwerk-/Datenmodelle zwischen Server und Client.
- **Hilft beim Modding?** Eher fortgeschritten; nützlich zum Verstehen von Datenpaketen, aber nicht erster Einstieg.

### `com/hypixel/hytale/protocol/packets/worldmap/`

- **Java-Dateien direkt:** 17
- **Java-Dateien gesamt:** 17
- **Beispiele:** BiomeData.java, ClearWorldMap.java, ContextMenuItem.java, CreateUserMarker.java, HeightDeltaIconComponent.java, MapChunk.java, MapImage.java, MapMarker.java …
- **Was passiert hier?** Netzwerk-/Datenmodelle zwischen Server und Client.
- **Hilft beim Modding?** Eher fortgeschritten; nützlich zum Verstehen von Datenpaketen, aber nicht erster Einstieg.

### `com/hypixel/hytale/registry/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** Registration.java, Registry.java
- **Was passiert hier?** Registry-System: Dinge anmelden und später wiederfinden.
- **Hilft beim Modding?** Wichtig für Modding, weil vieles erst registriert werden muss.

### `com/hypixel/hytale/server/`

- **Java-Dateien direkt:** 0
- **Java-Dateien gesamt:** 2694
- **Beispiele:** keine direkten .java Dateien
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/server/core/`

- **Java-Dateien direkt:** 7
- **Java-Dateien gesamt:** 1591
- **Beispiele:** Constants.java, HytaleServer.java, HytaleServerConfig.java, Message.java, NameMatching.java, Options.java, ShutdownReason.java
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/server/core/asset/`

- **Java-Dateien direkt:** 7
- **Java-Dateien gesamt:** 286
- **Beispiele:** AssetModule.java, AssetNotifications.java, AssetPackRegisterEvent.java, AssetPackUnregisterEvent.java, AssetRegistryLoader.java, HytaleAssetStore.java, LoadAssetEvent.java
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/server/core/asset/common/`

- **Java-Dateien direkt:** 10
- **Java-Dateien gesamt:** 13
- **Beispiele:** BlockyAnimationCache.java, CommonAsset.java, CommonAssetModule.java, CommonAssetRegistry.java, CommonAssetValidator.java, HytaleFileTypes.java, OggVorbisInfoCache.java, PlayerCommonAssets.java …
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/server/core/asset/common/asset/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** FileCommonAsset.java
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/server/core/asset/common/events/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** CommonAssetMonitorEvent.java, SendCommonAssetsEvent.java
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/server/core/asset/modifiers/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** MovementEffects.java
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/server/core/asset/monitor/`

- **Java-Dateien direkt:** 7
- **Java-Dateien gesamt:** 7
- **Beispiele:** AssetMonitor.java, AssetMonitorHandler.java, DirectoryHandlerChangeTask.java, EventKind.java, FileChangeTask.java, PathEvent.java, PathWatcherThread.java
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/server/core/asset/packet/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** AssetPacketGenerator.java, DefaultAssetPacketGenerator.java, SimpleAssetPacketGenerator.java
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/server/core/asset/type/`

- **Java-Dateien direkt:** 0
- **Java-Dateien gesamt:** 254
- **Beispiele:** keine direkten .java Dateien
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/server/core/asset/type/ambiencefx/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 9
- **Beispiele:** AmbienceFXPacketGenerator.java
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/server/core/asset/type/ambiencefx/config/`

- **Java-Dateien direkt:** 8
- **Java-Dateien gesamt:** 8
- **Beispiele:** AmbienceFX.java, AmbienceFXAmbientBed.java, AmbienceFXBlockSoundSet.java, AmbienceFXConditions.java, AmbienceFXMusic.java, AmbienceFXPhysicalMaterial.java, AmbienceFXSound.java, AmbienceFXSoundEffect.java
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/server/core/asset/type/attitude/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** Attitude.java
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/server/core/asset/type/audiocategory/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 2
- **Beispiele:** AudioCategoryPacketGenerator.java
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/server/core/asset/type/audiocategory/config/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** AudioCategory.java
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/server/core/asset/type/audiostate/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 8
- **Beispiele:** AudioStatePacketGenerator.java
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/server/core/asset/type/audiostate/config/`

- **Java-Dateien direkt:** 7
- **Java-Dateien gesamt:** 7
- **Beispiele:** AmbienceStateWriteConfig.java, AudioState.java, AudioStateCodecs.java, AudioStateResolver.java, StateBindingConfig.java, StateDeltaConfig.java, StateTransitionConfig.java
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/server/core/asset/type/blockbreakingdecal/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 2
- **Beispiele:** BlockBreakingDecalPacketGenerator.java
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/server/core/asset/type/blockbreakingdecal/config/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** BlockBreakingDecal.java
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/server/core/asset/type/blockhitbox/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** BlockBoundingBoxes.java, BlockBoundingBoxesPacketGenerator.java
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/server/core/asset/type/blockparticle/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 2
- **Beispiele:** BlockParticleSetPacketGenerator.java
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/server/core/asset/type/blockparticle/config/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** BlockParticleSet.java
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/server/core/asset/type/blockset/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 2
- **Beispiele:** BlockSetPacketGenerator.java
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/server/core/asset/type/blockset/config/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** BlockSet.java
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/server/core/asset/type/blocksound/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 2
- **Beispiele:** BlockSoundSetPacketGenerator.java
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/server/core/asset/type/blocksound/config/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** BlockSoundSet.java
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/server/core/asset/type/blocktick/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 5
- **Beispiele:** BlockTickManager.java, BlockTickStrategy.java, IBlockTickProvider.java
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/server/core/asset/type/blocktick/config/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** RandomTickProcedure.java, TickProcedure.java
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/server/core/asset/type/blocktype/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 40
- **Beispiele:** BlockGroupPacketGenerator.java, BlockTypePacketGenerator.java
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/server/core/asset/type/blocktype/config/`

- **Java-Dateien direkt:** 24
- **Java-Dateien gesamt:** 38
- **Beispiele:** BlockBreakingDropType.java, BlockFace.java, BlockFaceSupport.java, BlockFlipType.java, BlockGathering.java, BlockMigration.java, BlockMovementSettings.java, BlockPlacementSettings.java …
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/server/core/asset/type/blocktype/config/bench/`

- **Java-Dateien direkt:** 7
- **Java-Dateien gesamt:** 7
- **Beispiele:** Bench.java, BenchTierLevel.java, BenchUpgradeRequirement.java, CraftingBench.java, DiagramCraftingBench.java, ProcessingBench.java, StructuralCraftingBench.java
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/server/core/asset/type/blocktype/config/fallingblocks/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** FallingBlockImpact.java, FallingBlockSettings.java
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/server/core/asset/type/blocktype/config/farming/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** FarmingData.java, FarmingStageData.java, GrowthModifierAsset.java
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/server/core/asset/type/blocktype/config/mountpoints/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** BlockMountPoint.java, RotatedMountPointsArray.java
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/server/core/asset/type/buildertool/`

- **Java-Dateien direkt:** 0
- **Java-Dateien gesamt:** 16
- **Beispiele:** keine direkten .java Dateien
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/server/core/asset/type/buildertool/config/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 16
- **Beispiele:** BlockTypeListAsset.java, BuilderTool.java, PrefabListAsset.java
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/server/core/asset/type/buildertool/config/args/`

- **Java-Dateien direkt:** 13
- **Java-Dateien gesamt:** 13
- **Beispiele:** BlockArg.java, BoolArg.java, BrushAxisArg.java, BrushOriginArg.java, BrushRotationArg.java, BrushShapeArg.java, FloatArg.java, IntArg.java …
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/server/core/asset/type/camera/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** CameraEffect.java
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/server/core/asset/type/entityeffect/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 7
- **Beispiele:** EntityEffectPacketGenerator.java
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/server/core/asset/type/entityeffect/config/`

- **Java-Dateien direkt:** 6
- **Java-Dateien gesamt:** 6
- **Beispiele:** AbilityEffects.java, ApplicationEffects.java, EntityEffect.java, ModelOverride.java, OverlapBehavior.java, RemovalBehavior.java
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/server/core/asset/type/environment/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 3
- **Beispiele:** EnvironmentPacketGenerator.java
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/server/core/asset/type/environment/config/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** Environment.java, WeatherForecast.java
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/server/core/asset/type/equalizereffect/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 2
- **Beispiele:** EqualizerEffectPacketGenerator.java
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/server/core/asset/type/equalizereffect/config/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** EqualizerEffect.java
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/server/core/asset/type/fluid/`

- **Java-Dateien direkt:** 6
- **Java-Dateien gesamt:** 6
- **Beispiele:** DefaultFluidTicker.java, FiniteFluidTicker.java, FireFluidTicker.java, Fluid.java, FluidTicker.java, FluidTypePacketGenerator.java
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/server/core/asset/type/fluidfx/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 3
- **Beispiele:** FluidFXPacketGenerator.java
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/server/core/asset/type/fluidfx/config/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** FluidFX.java, FluidParticle.java
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/server/core/asset/type/gamemode/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** GameModeType.java
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/server/core/asset/type/gameplay/`

- **Java-Dateien direkt:** 14
- **Java-Dateien gesamt:** 21
- **Beispiele:** BrokenPenalties.java, CameraEffectsConfig.java, CombatConfig.java, CraftingConfig.java, DeathConfig.java, GameplayConfig.java, GatheringConfig.java, GatheringEffectsConfig.java …
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/server/core/asset/type/gameplay/respawn/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** HomeOrSpawnPoint.java, RespawnController.java, WorldSpawnPoint.java
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/server/core/asset/type/gameplay/sleep/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** SleepConfig.java, SleepSoundsConfig.java
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/server/core/asset/type/gameplay/worldmap/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** PlayersMapMarkerConfig.java, UserMapMarkerConfig.java
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/server/core/asset/type/item/`

- **Java-Dateien direkt:** 4
- **Java-Dateien gesamt:** 39
- **Beispiele:** DroplistCommand.java, FieldcraftCategoryPacketGenerator.java, ItemCategoryPacketGenerator.java, ResourceTypePacketGenerator.java
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/server/core/asset/type/item/config/`

- **Java-Dateien direkt:** 26
- **Java-Dateien gesamt:** 35
- **Beispiele:** AssetIconProperties.java, BlockGroup.java, BlockSelectorToolData.java, BuilderToolItemReferenceAsset.java, CraftingRecipe.java, FieldcraftCategory.java, Item.java, ItemAppearanceCondition.java …
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/server/core/asset/type/item/config/container/`

- **Java-Dateien direkt:** 6
- **Java-Dateien gesamt:** 6
- **Beispiele:** ChoiceItemDropContainer.java, DroplistItemDropContainer.java, EmptyItemDropContainer.java, ItemDropContainer.java, MultipleItemDropContainer.java, SingleItemDropContainer.java
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/server/core/asset/type/item/config/damageData/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** DamageBreakdown.java, WeaponDamageDataCollector.java
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/server/core/asset/type/item/config/metadata/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** AdventureMetadata.java
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/server/core/asset/type/itemanimation/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 2
- **Beispiele:** ItemPlayerAnimationsPacketGenerator.java
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/server/core/asset/type/itemanimation/config/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** ItemPlayerAnimations.java
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/server/core/asset/type/itemsound/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 2
- **Beispiele:** ItemSoundSetPacketGenerator.java
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/server/core/asset/type/itemsound/config/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** ItemSoundSet.java
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/server/core/asset/type/model/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 8
- **Beispiele:** BlockyModelBoundsParser.java
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/server/core/asset/type/model/config/`

- **Java-Dateien direkt:** 5
- **Java-Dateien gesamt:** 7
- **Beispiele:** DetailBox.java, Model.java, ModelAsset.java, ModelAttachment.java, ModelParticle.java
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/server/core/asset/type/model/config/camera/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** CameraAxis.java, CameraSettings.java
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/server/core/asset/type/modelvfx/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 2
- **Beispiele:** ModelVFXPacketGenerator.java
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/server/core/asset/type/modelvfx/config/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** ModelVFX.java
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/server/core/asset/type/musiccontainer/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 9
- **Beispiele:** MusicContainerPacketGenerator.java
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/server/core/asset/type/musiccontainer/config/`

- **Java-Dateien direkt:** 8
- **Java-Dateien gesamt:** 8
- **Beispiele:** BarBeatDuration.java, HorizontalMusicContainer.java, LayerPlacement.java, MusicContainer.java, RandomMusicContainer.java, SegmentMusicContainer.java, SequenceMusicContainer.java, SingleTrackMusicContainer.java
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/server/core/asset/type/particle/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 13
- **Beispiele:** ParticleSpawnerPacketGenerator.java, ParticleSystemPacketGenerator.java
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/server/core/asset/type/particle/commands/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** ParticleCommand.java, ParticleSpawnCommand.java
- **Was passiert hier?** Command-System und fertige Server-/Spieler-/Debug-Commands.
- **Hilft beim Modding?** Sehr wichtig: Vorlage für eigene Admin-Commands wie /keystone marker oder /npc spawn.

### `com/hypixel/hytale/server/core/asset/type/particle/config/`

- **Java-Dateien direkt:** 8
- **Java-Dateien gesamt:** 8
- **Beispiele:** Particle.java, ParticleAnimationFrame.java, ParticleAttractor.java, ParticleCollision.java, ParticleSpawner.java, ParticleSpawnerGroup.java, ParticleSystem.java, WorldParticle.java
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/server/core/asset/type/particle/pages/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** ParticleSpawnPage.java
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/server/core/asset/type/physicalmaterial/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 2
- **Beispiele:** PhysicalMaterialPacketGenerator.java
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/server/core/asset/type/physicalmaterial/config/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** PhysicalMaterial.java
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/server/core/asset/type/portalworld/`

- **Java-Dateien direkt:** 4
- **Java-Dateien gesamt:** 4
- **Beispiele:** PillTag.java, PortalDescription.java, PortalSpawnConfig.java, PortalType.java
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/server/core/asset/type/projectile/`

- **Java-Dateien direkt:** 0
- **Java-Dateien gesamt:** 1
- **Beispiele:** keine direkten .java Dateien
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/server/core/asset/type/projectile/config/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** Projectile.java
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/server/core/asset/type/responsecurve/`

- **Java-Dateien direkt:** 4
- **Java-Dateien gesamt:** 9
- **Beispiele:** ScaledResponseCurve.java, ScaledSwitchResponseCurve.java, ScaledXResponseCurve.java, ScaledXYResponseCurve.java
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/server/core/asset/type/responsecurve/config/`

- **Java-Dateien direkt:** 5
- **Java-Dateien gesamt:** 5
- **Beispiele:** ExponentialResponseCurve.java, LogisticResponseCurve.java, ResponseCurve.java, SineWaveResponseCurve.java, SwitchResponseCurve.java
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/server/core/asset/type/reverbeffect/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 2
- **Beispiele:** ReverbEffectPacketGenerator.java
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/server/core/asset/type/reverbeffect/config/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** ReverbEffect.java
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/server/core/asset/type/soundevent/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 4
- **Beispiele:** SoundEventPacketGenerator.java
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/server/core/asset/type/soundevent/config/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** SoundEvent.java, SoundEventLayer.java
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/server/core/asset/type/soundevent/validator/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** SoundEventValidators.java
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/server/core/asset/type/soundset/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 2
- **Beispiele:** SoundSetPacketGenerator.java
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/server/core/asset/type/soundset/config/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** SoundSet.java
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/server/core/asset/type/tagpattern/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 7
- **Beispiele:** TagPatternPacketGenerator.java
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/server/core/asset/type/tagpattern/config/`

- **Java-Dateien direkt:** 6
- **Java-Dateien gesamt:** 6
- **Beispiele:** AndPatternOp.java, EqualsTagOp.java, MultiplePatternOp.java, NotPatternOp.java, OrPatternOp.java, TagPattern.java
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/server/core/asset/type/trail/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 4
- **Beispiele:** TrailPacketGenerator.java
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/server/core/asset/type/trail/config/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** Animation.java, Edge.java, Trail.java
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/server/core/asset/type/weather/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 8
- **Beispiele:** WeatherPacketGenerator.java
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/server/core/asset/type/weather/config/`

- **Java-Dateien direkt:** 7
- **Java-Dateien gesamt:** 7
- **Beispiele:** Cloud.java, DayTexture.java, FogOptions.java, TimeColor.java, TimeColorAlpha.java, TimeFloat.java, Weather.java
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/server/core/asset/type/wordlist/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** WordList.java
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/server/core/asset/util/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** ColorParseUtil.java
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/server/core/auth/`

- **Java-Dateien direkt:** 15
- **Java-Dateien gesamt:** 20
- **Beispiele:** AuthConfig.java, AuthConfigGenerated.java, AuthCredentialStoreProvider.java, CertificateUtil.java, DefaultAuthCredentialStore.java, EncryptedAuthCredentialStore.java, EncryptedAuthCredentialStoreProvider.java, HttpResponseException.java …
- **Was passiert hier?** Login/Auth-System.
- **Hilft beim Modding?** Für dein Modding meist nicht zuerst wichtig.

### `com/hypixel/hytale/server/core/auth/oauth/`

- **Java-Dateien direkt:** 5
- **Java-Dateien gesamt:** 5
- **Beispiele:** OAuthBrowserFlow.java, OAuthClient.java, OAuthDeviceFlow.java, OAuthFlow.java, OAuthResult.java
- **Was passiert hier?** Login/Auth-System.
- **Hilft beim Modding?** Für dein Modding meist nicht zuerst wichtig.

### `com/hypixel/hytale/server/core/blocktype/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 2
- **Beispiele:** BlockTypeModule.java
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/server/core/blocktype/component/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** BlockPhysics.java
- **Was passiert hier?** ECS/Component-System: Daten hängen als Komponenten an Entities/Chunks/Systeme.
- **Hilft beim Modding?** Sehr wichtig: Hytale nutzt stark Component-Logik statt einfache Minecraft-Objekte.

### `com/hypixel/hytale/server/core/client/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** ClientFeatureHandler.java
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/server/core/codec/`

- **Java-Dateien direkt:** 6
- **Java-Dateien gesamt:** 8
- **Beispiele:** BoolDoublePairCodec.java, LayerEntryCodec.java, PairCodec.java, ProtocolCodecs.java, ShapeCodecs.java, WeightedMapCodec.java
- **Was passiert hier?** Codec-System: Daten aus JSON/Assets lesen und schreiben.
- **Hilft beim Modding?** Wichtig, wenn du eigene config-/assetbasierte Daten sauber laden willst.

### `com/hypixel/hytale/server/core/codec/protocol/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** ColorAlphaCodec.java, ColorCodec.java
- **Was passiert hier?** Codec-System: Daten aus JSON/Assets lesen und schreiben.
- **Hilft beim Modding?** Wichtig, wenn du eigene config-/assetbasierte Daten sauber laden willst.

### `com/hypixel/hytale/server/core/command/`

- **Java-Dateien direkt:** 0
- **Java-Dateien gesamt:** 226
- **Beispiele:** keine direkten .java Dateien
- **Was passiert hier?** Command-System und fertige Server-/Spieler-/Debug-Commands.
- **Hilft beim Modding?** Sehr wichtig: Vorlage für eigene Admin-Commands wie /keystone marker oder /npc spawn.

### `com/hypixel/hytale/server/core/command/commands/`

- **Java-Dateien direkt:** 0
- **Java-Dateien gesamt:** 161
- **Beispiele:** keine direkten .java Dateien
- **Was passiert hier?** Command-System und fertige Server-/Spieler-/Debug-Commands.
- **Hilft beim Modding?** Sehr wichtig: Vorlage für eigene Admin-Commands wie /keystone marker oder /npc spawn.

### `com/hypixel/hytale/server/core/command/commands/debug/`

- **Java-Dateien direkt:** 15
- **Java-Dateien gesamt:** 30
- **Beispiele:** AssetTagsCommand.java, AssetsCommand.java, AssetsDuplicatesCommand.java, DebugPlayerPositionCommand.java, HitDetectionCommand.java, HudManagerTestCommand.java, LogCommand.java, MessageTranslationTestCommand.java …
- **Was passiert hier?** Command-System und fertige Server-/Spieler-/Debug-Commands.
- **Hilft beim Modding?** Sehr wichtig: Vorlage für eigene Admin-Commands wie /keystone marker oder /npc spawn.

### `com/hypixel/hytale/server/core/command/commands/debug/component/`

- **Java-Dateien direkt:** 0
- **Java-Dateien gesamt:** 6
- **Beispiele:** keine direkten .java Dateien
- **Was passiert hier?** Command-System und fertige Server-/Spieler-/Debug-Commands.
- **Hilft beim Modding?** Sehr wichtig: Vorlage für eigene Admin-Commands wie /keystone marker oder /npc spawn.

### `com/hypixel/hytale/server/core/command/commands/debug/component/hitboxcollision/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** HitboxCollisionAddCommand.java, HitboxCollisionCommand.java, HitboxCollisionRemoveCommand.java
- **Was passiert hier?** Command-System und fertige Server-/Spieler-/Debug-Commands.
- **Hilft beim Modding?** Sehr wichtig: Vorlage für eigene Admin-Commands wie /keystone marker oder /npc spawn.

### `com/hypixel/hytale/server/core/command/commands/debug/component/repulsion/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** RepulsionAddCommand.java, RepulsionCommand.java, RepulsionRemoveCommand.java
- **Was passiert hier?** Command-System und fertige Server-/Spieler-/Debug-Commands.
- **Hilft beim Modding?** Sehr wichtig: Vorlage für eigene Admin-Commands wie /keystone marker oder /npc spawn.

### `com/hypixel/hytale/server/core/command/commands/debug/packs/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** PacksCommand.java, PacksListCommand.java
- **Was passiert hier?** Command-System und fertige Server-/Spieler-/Debug-Commands.
- **Hilft beim Modding?** Sehr wichtig: Vorlage für eigene Admin-Commands wie /keystone marker oder /npc spawn.

### `com/hypixel/hytale/server/core/command/commands/debug/server/`

- **Java-Dateien direkt:** 7
- **Java-Dateien gesamt:** 7
- **Beispiele:** ServerCommand.java, ServerDumpCommand.java, ServerGCCommand.java, ServerStatsCommand.java, ServerStatsCpuCommand.java, ServerStatsGcCommand.java, ServerStatsMemoryCommand.java
- **Was passiert hier?** Command-System und fertige Server-/Spieler-/Debug-Commands.
- **Hilft beim Modding?** Sehr wichtig: Vorlage für eigene Admin-Commands wie /keystone marker oder /npc spawn.

### `com/hypixel/hytale/server/core/command/commands/player/`

- **Java-Dateien direkt:** 13
- **Java-Dateien gesamt:** 42
- **Beispiele:** DamageCommand.java, GameModeCommand.java, HideCommand.java, KillCommand.java, PlayerCommand.java, PlayerResetCommand.java, PlayerRespawnCommand.java, PlayerZoneCommand.java …
- **Was passiert hier?** Command-System und fertige Server-/Spieler-/Debug-Commands.
- **Hilft beim Modding?** Sehr wichtig: Vorlage für eigene Admin-Commands wie /keystone marker oder /npc spawn.

### `com/hypixel/hytale/server/core/command/commands/player/camera/`

- **Java-Dateien direkt:** 8
- **Java-Dateien gesamt:** 8
- **Beispiele:** CameraDemo.java, PlayerCameraDemoActivateCommand.java, PlayerCameraDemoDeactivateCommand.java, PlayerCameraDemoSubCommand.java, PlayerCameraResetCommand.java, PlayerCameraSideScrollerCommand.java, PlayerCameraSubCommand.java, PlayerCameraTopdownCommand.java
- **Was passiert hier?** Command-System und fertige Server-/Spieler-/Debug-Commands.
- **Hilft beim Modding?** Sehr wichtig: Vorlage für eigene Admin-Commands wie /keystone marker oder /npc spawn.

### `com/hypixel/hytale/server/core/command/commands/player/effect/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** PlayerEffectApplyCommand.java, PlayerEffectClearCommand.java, PlayerEffectSubCommand.java
- **Was passiert hier?** Command-System und fertige Server-/Spieler-/Debug-Commands.
- **Hilft beim Modding?** Sehr wichtig: Vorlage für eigene Admin-Commands wie /keystone marker oder /npc spawn.

### `com/hypixel/hytale/server/core/command/commands/player/inventory/`

- **Java-Dateien direkt:** 8
- **Java-Dateien gesamt:** 8
- **Beispiele:** GiveArmorCommand.java, GiveCommand.java, InventoryBackpackCommand.java, InventoryClearCommand.java, InventoryCommand.java, InventoryItemCommand.java, InventorySeeCommand.java, ItemStateCommand.java
- **Was passiert hier?** Command-System und fertige Server-/Spieler-/Debug-Commands.
- **Hilft beim Modding?** Sehr wichtig: Vorlage für eigene Admin-Commands wie /keystone marker oder /npc spawn.

### `com/hypixel/hytale/server/core/command/commands/player/stats/`

- **Java-Dateien direkt:** 7
- **Java-Dateien gesamt:** 7
- **Beispiele:** PlayerStatsAddCommand.java, PlayerStatsDumpCommand.java, PlayerStatsGetCommand.java, PlayerStatsResetCommand.java, PlayerStatsSetCommand.java, PlayerStatsSetToMaxCommand.java, PlayerStatsSubCommand.java
- **Was passiert hier?** Command-System und fertige Server-/Spieler-/Debug-Commands.
- **Hilft beim Modding?** Sehr wichtig: Vorlage für eigene Admin-Commands wie /keystone marker oder /npc spawn.

### `com/hypixel/hytale/server/core/command/commands/player/viewradius/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** PlayerViewRadiusGetCommand.java, PlayerViewRadiusSetCommand.java, PlayerViewRadiusSubCommand.java
- **Was passiert hier?** Command-System und fertige Server-/Spieler-/Debug-Commands.
- **Hilft beim Modding?** Sehr wichtig: Vorlage für eigene Admin-Commands wie /keystone marker oder /npc spawn.

### `com/hypixel/hytale/server/core/command/commands/server/`

- **Java-Dateien direkt:** 4
- **Java-Dateien gesamt:** 13
- **Beispiele:** KickCommand.java, MaxPlayersCommand.java, StopCommand.java, WhoCommand.java
- **Was passiert hier?** Command-System und fertige Server-/Spieler-/Debug-Commands.
- **Hilft beim Modding?** Sehr wichtig: Vorlage für eigene Admin-Commands wie /keystone marker oder /npc spawn.

### `com/hypixel/hytale/server/core/command/commands/server/auth/`

- **Java-Dateien direkt:** 9
- **Java-Dateien gesamt:** 9
- **Beispiele:** AuthCancelCommand.java, AuthCommand.java, AuthLoginBrowserCommand.java, AuthLoginCommand.java, AuthLoginDeviceCommand.java, AuthLogoutCommand.java, AuthPersistenceCommand.java, AuthSelectCommand.java …
- **Was passiert hier?** Command-System und fertige Server-/Spieler-/Debug-Commands.
- **Hilft beim Modding?** Sehr wichtig: Vorlage für eigene Admin-Commands wie /keystone marker oder /npc spawn.

### `com/hypixel/hytale/server/core/command/commands/utility/`

- **Java-Dateien direkt:** 7
- **Java-Dateien gesamt:** 33
- **Beispiele:** BackupCommand.java, ConvertPrefabsCommand.java, EventTitleCommand.java, NotifyCommand.java, StashCommand.java, UIGalleryCommand.java, ValidateCPBCommand.java
- **Was passiert hier?** Command-System und fertige Server-/Spieler-/Debug-Commands.
- **Hilft beim Modding?** Sehr wichtig: Vorlage für eigene Admin-Commands wie /keystone marker oder /npc spawn.

### `com/hypixel/hytale/server/core/command/commands/utility/help/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** HelpCommand.java
- **Was passiert hier?** Command-System und fertige Server-/Spieler-/Debug-Commands.
- **Hilft beim Modding?** Sehr wichtig: Vorlage für eigene Admin-Commands wie /keystone marker oder /npc spawn.

### `com/hypixel/hytale/server/core/command/commands/utility/lighting/`

- **Java-Dateien direkt:** 7
- **Java-Dateien gesamt:** 7
- **Beispiele:** LightingCalculationCommand.java, LightingCommand.java, LightingGetCommand.java, LightingInfoCommand.java, LightingInvalidateCommand.java, LightingSendCommand.java, LightingSendToggleCommand.java
- **Was passiert hier?** Command-System und fertige Server-/Spieler-/Debug-Commands.
- **Hilft beim Modding?** Sehr wichtig: Vorlage für eigene Admin-Commands wie /keystone marker oder /npc spawn.

### `com/hypixel/hytale/server/core/command/commands/utility/metacommands/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** CommandsCommand.java, DumpCommandsCommand.java
- **Was passiert hier?** Command-System und fertige Server-/Spieler-/Debug-Commands.
- **Hilft beim Modding?** Sehr wichtig: Vorlage für eigene Admin-Commands wie /keystone marker oder /npc spawn.

### `com/hypixel/hytale/server/core/command/commands/utility/net/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** NetworkCommand.java
- **Was passiert hier?** Command-System und fertige Server-/Spieler-/Debug-Commands.
- **Hilft beim Modding?** Sehr wichtig: Vorlage für eigene Admin-Commands wie /keystone marker oder /npc spawn.

### `com/hypixel/hytale/server/core/command/commands/utility/sleep/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** SleepCommand.java, SleepOffsetCommand.java, SleepTestCommand.java
- **Was passiert hier?** Command-System und fertige Server-/Spieler-/Debug-Commands.
- **Hilft beim Modding?** Sehr wichtig: Vorlage für eigene Admin-Commands wie /keystone marker oder /npc spawn.

### `com/hypixel/hytale/server/core/command/commands/utility/sound/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** SoundCommand.java, SoundPlay2DCommand.java, SoundPlay3DCommand.java
- **Was passiert hier?** Command-System und fertige Server-/Spieler-/Debug-Commands.
- **Hilft beim Modding?** Sehr wichtig: Vorlage für eigene Admin-Commands wie /keystone marker oder /npc spawn.

### `com/hypixel/hytale/server/core/command/commands/utility/worldmap/`

- **Java-Dateien direkt:** 9
- **Java-Dateien gesamt:** 9
- **Beispiele:** WorldMapClearMarkersCommand.java, WorldMapCommand.java, WorldMapDiscoverCommand.java, WorldMapReloadCommand.java, WorldMapUndiscoverCommand.java, WorldMapViewRadiusGetCommand.java, WorldMapViewRadiusRemoveCommand.java, WorldMapViewRadiusSetCommand.java …
- **Was passiert hier?** Command-System und fertige Server-/Spieler-/Debug-Commands.
- **Hilft beim Modding?** Sehr wichtig: Vorlage für eigene Admin-Commands wie /keystone marker oder /npc spawn.

### `com/hypixel/hytale/server/core/command/commands/world/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 43
- **Beispiele:** SpawnBlockCommand.java
- **Was passiert hier?** Command-System und fertige Server-/Spieler-/Debug-Commands.
- **Hilft beim Modding?** Sehr wichtig: Vorlage für eigene Admin-Commands wie /keystone marker oder /npc spawn.

### `com/hypixel/hytale/server/core/command/commands/world/chunk/`

- **Java-Dateien direkt:** 14
- **Java-Dateien gesamt:** 14
- **Beispiele:** ChunkCommand.java, ChunkFixHeightMapCommand.java, ChunkForceTickCommand.java, ChunkInfoCommand.java, ChunkLightingCommand.java, ChunkLoadCommand.java, ChunkLoadedCommand.java, ChunkMarkSaveCommand.java …
- **Was passiert hier?** Command-System und fertige Server-/Spieler-/Debug-Commands.
- **Hilft beim Modding?** Sehr wichtig: Vorlage für eigene Admin-Commands wie /keystone marker oder /npc spawn.

### `com/hypixel/hytale/server/core/command/commands/world/entity/`

- **Java-Dateien direkt:** 15
- **Java-Dateien gesamt:** 25
- **Beispiele:** EntityCleanCommand.java, EntityCloneCommand.java, EntityCommand.java, EntityCountCommand.java, EntityDumpCommand.java, EntityEffectCommand.java, EntityHideFromAdventurePlayersCommand.java, EntityIntangibleCommand.java …
- **Was passiert hier?** Command-System und fertige Server-/Spieler-/Debug-Commands.
- **Hilft beim Modding?** Sehr wichtig: Vorlage für eigene Admin-Commands wie /keystone marker oder /npc spawn.

### `com/hypixel/hytale/server/core/command/commands/world/entity/snapshot/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** EntitySnapshotHistoryCommand.java, EntitySnapshotLengthCommand.java, EntitySnapshotSubCommand.java
- **Was passiert hier?** Command-System und fertige Server-/Spieler-/Debug-Commands.
- **Hilft beim Modding?** Sehr wichtig: Vorlage für eigene Admin-Commands wie /keystone marker oder /npc spawn.

### `com/hypixel/hytale/server/core/command/commands/world/entity/stats/`

- **Java-Dateien direkt:** 7
- **Java-Dateien gesamt:** 7
- **Beispiele:** EntityStatsAddCommand.java, EntityStatsDumpCommand.java, EntityStatsGetCommand.java, EntityStatsResetCommand.java, EntityStatsSetCommand.java, EntityStatsSetToMaxCommand.java, EntityStatsSubCommand.java
- **Was passiert hier?** Command-System und fertige Server-/Spieler-/Debug-Commands.
- **Hilft beim Modding?** Sehr wichtig: Vorlage für eigene Admin-Commands wie /keystone marker oder /npc spawn.

### `com/hypixel/hytale/server/core/command/commands/world/worldgen/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** WorldGenBenchmarkCommand.java, WorldGenCommand.java, WorldGenReloadCommand.java
- **Was passiert hier?** Command-System und fertige Server-/Spieler-/Debug-Commands.
- **Hilft beim Modding?** Sehr wichtig: Vorlage für eigene Admin-Commands wie /keystone marker oder /npc spawn.

### `com/hypixel/hytale/server/core/command/system/`

- **Java-Dateien direkt:** 15
- **Java-Dateien gesamt:** 65
- **Beispiele:** AbbreviationMap.java, AbstractCommand.java, CommandContext.java, CommandManager.java, CommandOwner.java, CommandRegistration.java, CommandRegistry.java, CommandSender.java …
- **Was passiert hier?** Command-System und fertige Server-/Spieler-/Debug-Commands.
- **Hilft beim Modding?** Sehr wichtig: Vorlage für eigene Admin-Commands wie /keystone marker oder /npc spawn.

### `com/hypixel/hytale/server/core/command/system/arguments/`

- **Java-Dateien direkt:** 0
- **Java-Dateien gesamt:** 32
- **Beispiele:** keine direkten .java Dateien
- **Was passiert hier?** Command-System und fertige Server-/Spieler-/Debug-Commands.
- **Hilft beim Modding?** Sehr wichtig: Vorlage für eigene Admin-Commands wie /keystone marker oder /npc spawn.

### `com/hypixel/hytale/server/core/command/system/arguments/system/`

- **Java-Dateien direkt:** 8
- **Java-Dateien gesamt:** 8
- **Beispiele:** AbstractOptionalArg.java, ArgWrapper.java, Argument.java, DefaultArg.java, FlagArg.java, OptionalArg.java, RequiredArg.java, WrappedArg.java
- **Was passiert hier?** Command-System und fertige Server-/Spieler-/Debug-Commands.
- **Hilft beim Modding?** Sehr wichtig: Vorlage für eigene Admin-Commands wie /keystone marker oder /npc spawn.

### `com/hypixel/hytale/server/core/command/system/arguments/types/`

- **Java-Dateien direkt:** 24
- **Java-Dateien gesamt:** 24
- **Beispiele:** AbstractAssetArgumentType.java, ArgTypes.java, ArgumentType.java, AssetArgumentType.java, BooleanFlagArgumentType.java, Coord.java, EntityWrappedArg.java, EnumArgumentType.java …
- **Was passiert hier?** Command-System und fertige Server-/Spieler-/Debug-Commands.
- **Hilft beim Modding?** Sehr wichtig: Vorlage für eigene Admin-Commands wie /keystone marker oder /npc spawn.

### `com/hypixel/hytale/server/core/command/system/basecommands/`

- **Java-Dateien direkt:** 10
- **Java-Dateien gesamt:** 10
- **Beispiele:** AbstractAsyncCommand.java, AbstractAsyncPlayerCommand.java, AbstractAsyncWorldCommand.java, AbstractCommandCollection.java, AbstractPlayerCommand.java, AbstractTargetEntityCommand.java, AbstractTargetPlayerCommand.java, AbstractTargetPlayersCommand.java …
- **Was passiert hier?** Command-System und fertige Server-/Spieler-/Debug-Commands.
- **Hilft beim Modding?** Sehr wichtig: Vorlage für eigene Admin-Commands wie /keystone marker oder /npc spawn.

### `com/hypixel/hytale/server/core/command/system/exceptions/`

- **Java-Dateien direkt:** 4
- **Java-Dateien gesamt:** 4
- **Beispiele:** CommandException.java, GeneralCommandException.java, NoPermissionException.java, SenderTypeException.java
- **Was passiert hier?** Command-System und fertige Server-/Spieler-/Debug-Commands.
- **Hilft beim Modding?** Sehr wichtig: Vorlage für eigene Admin-Commands wie /keystone marker oder /npc spawn.

### `com/hypixel/hytale/server/core/command/system/pages/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** CommandListPage.java, UIGalleryPage.java
- **Was passiert hier?** Command-System und fertige Server-/Spieler-/Debug-Commands.
- **Hilft beim Modding?** Sehr wichtig: Vorlage für eigene Admin-Commands wie /keystone marker oder /npc spawn.

### `com/hypixel/hytale/server/core/command/system/suggestion/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** SuggestionProvider.java, SuggestionResult.java
- **Was passiert hier?** Command-System und fertige Server-/Spieler-/Debug-Commands.
- **Hilft beim Modding?** Sehr wichtig: Vorlage für eigene Admin-Commands wie /keystone marker oder /npc spawn.

### `com/hypixel/hytale/server/core/config/`

- **Java-Dateien direkt:** 7
- **Java-Dateien gesamt:** 7
- **Beispiele:** BackupConfig.java, ModConfig.java, RateLimitConfig.java, ServerWorldMapConfig.java, UpdateConfig.java, WorldMapConfig.java, WorldWorldMapConfig.java
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/server/core/console/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 3
- **Beispiele:** ConsoleModule.java, ConsoleSender.java
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/server/core/console/command/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** SayCommand.java
- **Was passiert hier?** Command-System und fertige Server-/Spieler-/Debug-Commands.
- **Hilft beim Modding?** Sehr wichtig: Vorlage für eigene Admin-Commands wie /keystone marker oder /npc spawn.

### `com/hypixel/hytale/server/core/cosmetics/`

- **Java-Dateien direkt:** 15
- **Java-Dateien gesamt:** 16
- **Beispiele:** BodyType.java, CosmeticAssetValidator.java, CosmeticRegistry.java, CosmeticType.java, CosmeticsModule.java, Emote.java, EmoteAsset.java, EmoteAssetPacketGenerator.java …
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/server/core/cosmetics/commands/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** EmoteCommand.java
- **Was passiert hier?** Command-System und fertige Server-/Spieler-/Debug-Commands.
- **Hilft beim Modding?** Sehr wichtig: Vorlage für eigene Admin-Commands wie /keystone marker oder /npc spawn.

### `com/hypixel/hytale/server/core/entity/`

- **Java-Dateien direkt:** 16
- **Java-Dateien gesamt:** 68
- **Beispiele:** AnimationUtils.java, ChainSyncStorage.java, Entity.java, EntitySnapshot.java, EntityUtils.java, ExplosionConfig.java, ExplosionUtils.java, Frozen.java …
- **Was passiert hier?** Entity-System: Spieler, Kreaturen, Komponenten, Schaden, Movement, Stats.
- **Hilft beim Modding?** Wichtig für NPCs, Spielerinteraktion und eigene Entity-Logik.

### `com/hypixel/hytale/server/core/entity/damage/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** DamageDataComponent.java, DamageDataSetupSystem.java
- **Was passiert hier?** Entity-System: Spieler, Kreaturen, Komponenten, Schaden, Movement, Stats.
- **Hilft beim Modding?** Wichtig für NPCs, Spielerinteraktion und eigene Entity-Logik.

### `com/hypixel/hytale/server/core/entity/effect/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** ActiveEntityEffect.java, EffectControllerComponent.java
- **Was passiert hier?** Entity-System: Spieler, Kreaturen, Komponenten, Schaden, Movement, Stats.
- **Hilft beim Modding?** Wichtig für NPCs, Spielerinteraktion und eigene Entity-Logik.

### `com/hypixel/hytale/server/core/entity/entities/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 38
- **Beispiele:** BlockEntity.java, Player.java, ProjectileComponent.java
- **Was passiert hier?** Entity-System: Spieler, Kreaturen, Komponenten, Schaden, Movement, Stats.
- **Hilft beim Modding?** Wichtig für NPCs, Spielerinteraktion und eigene Entity-Logik.

### `com/hypixel/hytale/server/core/entity/entities/player/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 35
- **Beispiele:** CameraManager.java, HiddenPlayersManager.java, HotbarManager.java
- **Was passiert hier?** Entity-System: Spieler, Kreaturen, Komponenten, Schaden, Movement, Stats.
- **Hilft beim Modding?** Wichtig für NPCs, Spielerinteraktion und eigene Entity-Logik.

### `com/hypixel/hytale/server/core/entity/entities/player/data/`

- **Java-Dateien direkt:** 5
- **Java-Dateien gesamt:** 5
- **Beispiele:** PlayerConfigData.java, PlayerDeathPositionData.java, PlayerRespawnPointData.java, PlayerWorldData.java, UniqueItemUsagesComponent.java
- **Was passiert hier?** Entity-System: Spieler, Kreaturen, Komponenten, Schaden, Movement, Stats.
- **Hilft beim Modding?** Wichtig für NPCs, Spielerinteraktion und eigene Entity-Logik.

### `com/hypixel/hytale/server/core/entity/entities/player/hud/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** CustomUIHud.java, HudManager.java
- **Was passiert hier?** Entity-System: Spieler, Kreaturen, Komponenten, Schaden, Movement, Stats.
- **Hilft beim Modding?** Wichtig für NPCs, Spielerinteraktion und eigene Entity-Logik.

### `com/hypixel/hytale/server/core/entity/entities/player/movement/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** MovementConfig.java, MovementManager.java
- **Was passiert hier?** Entity-System: Spieler, Kreaturen, Komponenten, Schaden, Movement, Stats.
- **Hilft beim Modding?** Wichtig für NPCs, Spielerinteraktion und eigene Entity-Logik.

### `com/hypixel/hytale/server/core/entity/entities/player/pages/`

- **Java-Dateien direkt:** 5
- **Java-Dateien gesamt:** 13
- **Beispiele:** BasicCustomUIPage.java, CustomUIPage.java, InteractiveCustomUIPage.java, PageManager.java, RespawnPage.java
- **Was passiert hier?** Entity-System: Spieler, Kreaturen, Komponenten, Schaden, Movement, Stats.
- **Hilft beim Modding?** Wichtig für NPCs, Spielerinteraktion und eigene Entity-Logik.

### `com/hypixel/hytale/server/core/entity/entities/player/pages/audio/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** PlaySoundPage.java
- **Was passiert hier?** Entity-System: Spieler, Kreaturen, Komponenten, Schaden, Movement, Stats.
- **Hilft beim Modding?** Wichtig für NPCs, Spielerinteraktion und eigene Entity-Logik.

### `com/hypixel/hytale/server/core/entity/entities/player/pages/choices/`

- **Java-Dateien direkt:** 4
- **Java-Dateien gesamt:** 4
- **Beispiele:** ChoiceBasePage.java, ChoiceElement.java, ChoiceInteraction.java, ChoiceRequirement.java
- **Was passiert hier?** Entity-System: Spieler, Kreaturen, Komponenten, Schaden, Movement, Stats.
- **Hilft beim Modding?** Wichtig für NPCs, Spielerinteraktion und eigene Entity-Logik.

### `com/hypixel/hytale/server/core/entity/entities/player/pages/itemrepair/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** ItemRepairElement.java, ItemRepairPage.java, RepairItemInteraction.java
- **Was passiert hier?** Entity-System: Spieler, Kreaturen, Komponenten, Schaden, Movement, Stats.
- **Hilft beim Modding?** Wichtig für NPCs, Spielerinteraktion und eigene Entity-Logik.

### `com/hypixel/hytale/server/core/entity/entities/player/windows/`

- **Java-Dateien direkt:** 10
- **Java-Dateien gesamt:** 10
- **Beispiele:** BlockWindow.java, ContainerBlockWindow.java, ContainerWindow.java, ItemContainerWindow.java, ItemStackContainerWindow.java, MaterialContainerWindow.java, MaterialExtraResourcesSection.java, ValidatedWindow.java …
- **Was passiert hier?** Entity-System: Spieler, Kreaturen, Komponenten, Schaden, Movement, Stats.
- **Hilft beim Modding?** Wichtig für NPCs, Spielerinteraktion und eigene Entity-Logik.

### `com/hypixel/hytale/server/core/entity/group/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** EntityGroup.java
- **Was passiert hier?** Entity-System: Spieler, Kreaturen, Komponenten, Schaden, Movement, Stats.
- **Hilft beim Modding?** Wichtig für NPCs, Spielerinteraktion und eigene Entity-Logik.

### `com/hypixel/hytale/server/core/entity/knockback/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** KnockbackComponent.java, KnockbackSystems.java
- **Was passiert hier?** Entity-System: Spieler, Kreaturen, Komponenten, Schaden, Movement, Stats.
- **Hilft beim Modding?** Wichtig für NPCs, Spielerinteraktion und eigene Entity-Logik.

### `com/hypixel/hytale/server/core/entity/movement/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** MovementStatesComponent.java, MovementStatesSystems.java
- **Was passiert hier?** Entity-System: Spieler, Kreaturen, Komponenten, Schaden, Movement, Stats.
- **Hilft beim Modding?** Wichtig für NPCs, Spielerinteraktion und eigene Entity-Logik.

### `com/hypixel/hytale/server/core/entity/nameplate/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** Nameplate.java, NameplateSystems.java
- **Was passiert hier?** Entity-System: Spieler, Kreaturen, Komponenten, Schaden, Movement, Stats.
- **Hilft beim Modding?** Wichtig für NPCs, Spielerinteraktion und eigene Entity-Logik.

### `com/hypixel/hytale/server/core/entity/reference/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** InvalidatablePersistentRef.java, PersistentRef.java, PersistentRefCount.java
- **Was passiert hier?** Entity-System: Spieler, Kreaturen, Komponenten, Schaden, Movement, Stats.
- **Hilft beim Modding?** Wichtig für NPCs, Spielerinteraktion und eigene Entity-Logik.

### `com/hypixel/hytale/server/core/event/`

- **Java-Dateien direkt:** 0
- **Java-Dateien gesamt:** 36
- **Beispiele:** keine direkten .java Dateien
- **Was passiert hier?** Event-System: Ereignisse registrieren und auslösen.
- **Hilft beim Modding?** Wichtig, um auf Server-Events zu reagieren, ohne überall Code zu patchen.

### `com/hypixel/hytale/server/core/event/events/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 36
- **Beispiele:** BootEvent.java, PrepareUniverseEvent.java, ShutdownEvent.java
- **Was passiert hier?** Event-System: Ereignisse registrieren und auslösen.
- **Hilft beim Modding?** Wichtig, um auf Server-Events zu reagieren, ohne überall Code zu patchen.

### `com/hypixel/hytale/server/core/event/events/ecs/`

- **Java-Dateien direkt:** 12
- **Java-Dateien gesamt:** 12
- **Beispiele:** BreakBlockEvent.java, ChangeGameModeEvent.java, CraftRecipeEvent.java, DamageBlockEvent.java, DiscoverZoneEvent.java, DropItemEvent.java, InteractivelyPickupItemEvent.java, InventoryActiveSlotRequestEvent.java …
- **Was passiert hier?** Event-System: Ereignisse registrieren und auslösen.
- **Hilft beim Modding?** Wichtig, um auf Server-Events zu reagieren, ohne überall Code zu patchen.

### `com/hypixel/hytale/server/core/event/events/entity/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** EntityEvent.java, EntityRemoveEvent.java, LivingEntityUseBlockEvent.java
- **Was passiert hier?** Event-System: Ereignisse registrieren und auslösen.
- **Hilft beim Modding?** Wichtig, um auf Server-Events zu reagieren, ohne überall Code zu patchen.

### `com/hypixel/hytale/server/core/event/events/permissions/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** GroupPermissionChangeEvent.java, PlayerGroupEvent.java, PlayerPermissionChangeEvent.java
- **Was passiert hier?** Event-System: Ereignisse registrieren und auslösen.
- **Hilft beim Modding?** Wichtig, um auf Server-Events zu reagieren, ohne überall Code zu patchen.

### `com/hypixel/hytale/server/core/event/events/player/`

- **Java-Dateien direkt:** 15
- **Java-Dateien gesamt:** 15
- **Beispiele:** AddPlayerToWorldEvent.java, DrainPlayerFromWorldEvent.java, PlayerChatEvent.java, PlayerConnectEvent.java, PlayerCraftEvent.java, PlayerDisconnectEvent.java, PlayerEvent.java, PlayerInteractEvent.java …
- **Was passiert hier?** Event-System: Ereignisse registrieren und auslösen.
- **Hilft beim Modding?** Wichtig, um auf Server-Events zu reagieren, ohne überall Code zu patchen.

### `com/hypixel/hytale/server/core/inventory/`

- **Java-Dateien direkt:** 9
- **Java-Dateien gesamt:** 47
- **Beispiele:** ActiveSlotInventoryComponent.java, Inventory.java, InventoryComponent.java, InventorySystems.java, InventoryUtils.java, ItemContext.java, ItemStack.java, MaterialQuantity.java …
- **Was passiert hier?** Inventar-/Item-System.
- **Hilft beim Modding?** Wichtig für Belohnungen, Kosten, Shop, Energy Crystals oder NPC-Handel.

### `com/hypixel/hytale/server/core/inventory/container/`

- **Java-Dateien direkt:** 15
- **Java-Dateien gesamt:** 23
- **Beispiele:** CombinedItemContainer.java, DelegateItemContainer.java, EmptyItemContainer.java, FetchedItemContainer.java, InternalContainerUtilItemStack.java, InternalContainerUtilMaterial.java, InternalContainerUtilResource.java, InternalContainerUtilTag.java …
- **Was passiert hier?** Inventar-/Item-System.
- **Hilft beim Modding?** Wichtig für Belohnungen, Kosten, Shop, Energy Crystals oder NPC-Handel.

### `com/hypixel/hytale/server/core/inventory/container/filter/`

- **Java-Dateien direkt:** 8
- **Java-Dateien gesamt:** 8
- **Beispiele:** ArmorSlotAddFilter.java, FilterActionType.java, FilterType.java, ItemSlotFilter.java, NoDuplicateFilter.java, ResourceFilter.java, SlotFilter.java, TagFilter.java
- **Was passiert hier?** Inventar-/Item-System.
- **Hilft beim Modding?** Wichtig für Belohnungen, Kosten, Shop, Energy Crystals oder NPC-Handel.

### `com/hypixel/hytale/server/core/inventory/transaction/`

- **Java-Dateien direkt:** 15
- **Java-Dateien gesamt:** 15
- **Beispiele:** ActionType.java, ClearTransaction.java, ItemStackSlotTransaction.java, ItemStackTransaction.java, ListTransaction.java, MaterialSlotTransaction.java, MaterialTransaction.java, MoveTransaction.java …
- **Was passiert hier?** Inventar-/Item-System.
- **Hilft beim Modding?** Wichtig für Belohnungen, Kosten, Shop, Energy Crystals oder NPC-Handel.

### `com/hypixel/hytale/server/core/io/`

- **Java-Dateien direkt:** 7
- **Java-Dateien gesamt:** 39
- **Beispiele:** NetworkSerializable.java, NetworkSerializer.java, NetworkSerializers.java, PacketHandler.java, PacketStatsRecorderImpl.java, ProtocolVersion.java, ServerManager.java
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/server/core/io/adapter/`

- **Java-Dateien direkt:** 5
- **Java-Dateien gesamt:** 5
- **Beispiele:** PacketAdapters.java, PacketFilter.java, PacketWatcher.java, PlayerPacketFilter.java, PlayerPacketWatcher.java
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/server/core/io/commands/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** BindingsCommand.java
- **Was passiert hier?** Command-System und fertige Server-/Spieler-/Debug-Commands.
- **Hilft beim Modding?** Sehr wichtig: Vorlage für eigene Admin-Commands wie /keystone marker oder /npc spawn.

### `com/hypixel/hytale/server/core/io/handlers/`

- **Java-Dateien direkt:** 7
- **Java-Dateien gesamt:** 12
- **Beispiele:** GenericConnectionPacketHandler.java, GenericPacketHandler.java, IPacketHandler.java, IWorldPacketHandler.java, InitialPacketHandler.java, SetupPacketHandler.java, SubPacketHandler.java
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/server/core/io/handlers/game/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** GamePacketHandler.java, InventoryPacketHandler.java
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/server/core/io/handlers/login/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** AuthenticationPacketHandler.java, HandshakeHandler.java, PasswordPacketHandler.java
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/server/core/io/netty/`

- **Java-Dateien direkt:** 6
- **Java-Dateien gesamt:** 6
- **Beispiele:** HytaleChannelInitializer.java, LatencySimulationHandler.java, NettyUtil.java, PacketArrayEncoder.java, PlayerChannelHandler.java, RateLimitHandler.java
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/server/core/io/stream/`

- **Java-Dateien direkt:** 4
- **Java-Dateien gesamt:** 4
- **Beispiele:** PendingStreamConnectionHandler.java, PendingStreamHandler.java, StreamConnectionHandlerAdapter.java, StreamManager.java
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/server/core/io/transport/`

- **Java-Dateien direkt:** 4
- **Java-Dateien gesamt:** 4
- **Beispiele:** QUICTransport.java, QuicheTransport.java, Transport.java, TransportType.java
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/server/core/liveconfig/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** LiveConfigModule.java, LiveConfigService.java, LiveConfigSnapshot.java
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/server/core/meta/`

- **Java-Dateien direkt:** 9
- **Java-Dateien gesamt:** 9
- **Beispiele:** AbstractMetaStore.java, ArrayMetaStore.java, DynamicMetaStore.java, IMetaRegistry.java, IMetaStore.java, IMetaStoreImpl.java, MetaKey.java, MetaRegistry.java …
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/server/core/modules/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 458
- **Beispiele:** LegacyModule.java
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/server/core/modules/accesscontrol/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 20
- **Beispiele:** AccessControlModule.java
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/server/core/modules/accesscontrol/ban/`

- **Java-Dateien direkt:** 5
- **Java-Dateien gesamt:** 5
- **Beispiele:** AbstractBan.java, Ban.java, BanParser.java, InfiniteBan.java, TimedBan.java
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/server/core/modules/accesscontrol/commands/`

- **Java-Dateien direkt:** 10
- **Java-Dateien gesamt:** 10
- **Beispiele:** BanCommand.java, UnbanCommand.java, WhitelistAddCommand.java, WhitelistClearCommand.java, WhitelistCommand.java, WhitelistDisableCommand.java, WhitelistEnableCommand.java, WhitelistListCommand.java …
- **Was passiert hier?** Command-System und fertige Server-/Spieler-/Debug-Commands.
- **Hilft beim Modding?** Sehr wichtig: Vorlage für eigene Admin-Commands wie /keystone marker oder /npc spawn.

### `com/hypixel/hytale/server/core/modules/accesscontrol/provider/`

- **Java-Dateien direkt:** 4
- **Java-Dateien gesamt:** 4
- **Beispiele:** AccessProvider.java, ClientDelegatingProvider.java, HytaleBanProvider.java, HytaleWhitelistProvider.java
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/server/core/modules/anchoraction/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** AnchorActionHandler.java, AnchorActionModule.java
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/server/core/modules/block/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 6
- **Beispiele:** BlockEntity.java, BlockModule.java, BlockReplaceEvent.java
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/server/core/modules/block/components/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** ItemContainerBlock.java
- **Was passiert hier?** ECS/Component-System: Daten hängen als Komponenten an Entities/Chunks/Systeme.
- **Hilft beim Modding?** Sehr wichtig: Hytale nutzt stark Component-Logik statt einfache Minecraft-Objekte.

### `com/hypixel/hytale/server/core/modules/block/system/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** ItemContainerBlockSpatialSystem.java, ItemContainerSystems.java
- **Was passiert hier?** System-/Task-Code: wiederkehrende oder eventbasierte Verarbeitung.
- **Hilft beim Modding?** Wichtig für saubere Serverlogik ohne dauernde Welt-Scans.

### `com/hypixel/hytale/server/core/modules/blockhealth/`

- **Java-Dateien direkt:** 4
- **Java-Dateien gesamt:** 4
- **Beispiele:** BlockHealth.java, BlockHealthChunk.java, BlockHealthModule.java, FragileBlock.java
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/server/core/modules/blockset/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 3
- **Beispiele:** BlockSetLookupTable.java, BlockSetModule.java
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/server/core/modules/blockset/commands/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** BlockSetCommand.java
- **Was passiert hier?** Command-System und fertige Server-/Spieler-/Debug-Commands.
- **Hilft beim Modding?** Sehr wichtig: Vorlage für eigene Admin-Commands wie /keystone marker oder /npc spawn.

### `com/hypixel/hytale/server/core/modules/camera/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** FlyCameraModule.java
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/server/core/modules/collision/`

- **Java-Dateien direkt:** 28
- **Java-Dateien gesamt:** 29
- **Beispiele:** BasicCollisionData.java, BlockCollisionData.java, BlockCollisionProvider.java, BlockContactData.java, BlockData.java, BlockDataProvider.java, BlockTracker.java, BoxBlockIntersectionEvaluator.java …
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/server/core/modules/collision/commands/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** HitboxCommand.java
- **Was passiert hier?** Command-System und fertige Server-/Spieler-/Debug-Commands.
- **Hilft beim Modding?** Sehr wichtig: Vorlage für eigene Admin-Commands wie /keystone marker oder /npc spawn.

### `com/hypixel/hytale/server/core/modules/debug/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 11
- **Beispiele:** DebugPlugin.java, DebugUtils.java
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/server/core/modules/debug/commands/`

- **Java-Dateien direkt:** 9
- **Java-Dateien gesamt:** 9
- **Beispiele:** DebugCommand.java, DebugShapeArrowCommand.java, DebugShapeClearCommand.java, DebugShapeConeCommand.java, DebugShapeCubeCommand.java, DebugShapeCylinderCommand.java, DebugShapeShowForceCommand.java, DebugShapeSphereCommand.java …
- **Was passiert hier?** Command-System und fertige Server-/Spieler-/Debug-Commands.
- **Hilft beim Modding?** Sehr wichtig: Vorlage für eigene Admin-Commands wie /keystone marker oder /npc spawn.

### `com/hypixel/hytale/server/core/modules/entity/`

- **Java-Dateien direkt:** 10
- **Java-Dateien gesamt:** 141
- **Beispiele:** AllLegacyEntityTypesQuery.java, AllLegacyLivingEntityTypesQuery.java, BlockEntitySystems.java, BlockMigrationExtraInfo.java, DespawnComponent.java, DespawnSystem.java, EntityModule.java, EntityRegistration.java …
- **Was passiert hier?** Entity-System: Spieler, Kreaturen, Komponenten, Schaden, Movement, Stats.
- **Hilft beim Modding?** Wichtig für NPCs, Spielerinteraktion und eigene Entity-Logik.

### `com/hypixel/hytale/server/core/modules/entity/component/`

- **Java-Dateien direkt:** 30
- **Java-Dateien gesamt:** 30
- **Beispiele:** ActiveAnimationComponent.java, AudioComponent.java, BoundingBox.java, CachedStatsComponent.java, CollisionResultComponent.java, DisplayNameComponent.java, DynamicLight.java, EntityScaleComponent.java …
- **Was passiert hier?** ECS/Component-System: Daten hängen als Komponenten an Entities/Chunks/Systeme.
- **Hilft beim Modding?** Sehr wichtig: Hytale nutzt stark Component-Logik statt einfache Minecraft-Objekte.

### `com/hypixel/hytale/server/core/modules/entity/condition/`

- **Java-Dateien direkt:** 18
- **Java-Dateien gesamt:** 18
- **Beispiele:** AliveCondition.java, ChargingCondition.java, CheckPlayerGameModeCondition.java, Condition.java, EntityStatBoundCondition.java, EnvironmentCondition.java, GlidingCondition.java, HasEffectCondition.java …
- **Was passiert hier?** Entity-System: Spieler, Kreaturen, Komponenten, Schaden, Movement, Stats.
- **Hilft beim Modding?** Wichtig für NPCs, Spielerinteraktion und eigene Entity-Logik.

### `com/hypixel/hytale/server/core/modules/entity/damage/`

- **Java-Dateien direkt:** 12
- **Java-Dateien gesamt:** 14
- **Beispiele:** Damage.java, DamageCalculatorSystems.java, DamageCause.java, DamageEventSystem.java, DamageModule.java, DamageSystems.java, DeathComponent.java, DeathItemLoss.java …
- **Was passiert hier?** Entity-System: Spieler, Kreaturen, Komponenten, Schaden, Movement, Stats.
- **Hilft beim Modding?** Wichtig für NPCs, Spielerinteraktion und eigene Entity-Logik.

### `com/hypixel/hytale/server/core/modules/entity/damage/commands/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** DesyncDamageCommand.java
- **Was passiert hier?** Command-System und fertige Server-/Spieler-/Debug-Commands.
- **Hilft beim Modding?** Sehr wichtig: Vorlage für eigene Admin-Commands wie /keystone marker oder /npc spawn.

### `com/hypixel/hytale/server/core/modules/entity/damage/event/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** KillFeedEvent.java
- **Was passiert hier?** Event-System: Ereignisse registrieren und auslösen.
- **Hilft beim Modding?** Wichtig, um auf Server-Events zu reagieren, ohne überall Code zu patchen.

### `com/hypixel/hytale/server/core/modules/entity/dynamiclight/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** DynamicLightSystems.java
- **Was passiert hier?** Entity-System: Spieler, Kreaturen, Komponenten, Schaden, Movement, Stats.
- **Hilft beim Modding?** Wichtig für NPCs, Spielerinteraktion und eigene Entity-Logik.

### `com/hypixel/hytale/server/core/modules/entity/hitboxcollision/`

- **Java-Dateien direkt:** 4
- **Java-Dateien gesamt:** 4
- **Beispiele:** HitboxCollision.java, HitboxCollisionConfig.java, HitboxCollisionConfigPacketGenerator.java, HitboxCollisionSystems.java
- **Was passiert hier?** Entity-System: Spieler, Kreaturen, Komponenten, Schaden, Movement, Stats.
- **Hilft beim Modding?** Wichtig für NPCs, Spielerinteraktion und eigene Entity-Logik.

### `com/hypixel/hytale/server/core/modules/entity/item/`

- **Java-Dateien direkt:** 10
- **Java-Dateien gesamt:** 10
- **Beispiele:** ItemComponent.java, ItemMergeSystem.java, ItemPhysicsComponent.java, ItemPhysicsSystem.java, ItemPrePhysicsSystem.java, ItemSystems.java, PickupItemComponent.java, PickupItemSystem.java …
- **Was passiert hier?** Entity-System: Spieler, Kreaturen, Komponenten, Schaden, Movement, Stats.
- **Hilft beim Modding?** Wichtig für NPCs, Spielerinteraktion und eigene Entity-Logik.

### `com/hypixel/hytale/server/core/modules/entity/livingentity/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** LivingEntityEffectClearChangesSystem.java, LivingEntityEffectSystem.java
- **Was passiert hier?** Entity-System: Spieler, Kreaturen, Komponenten, Schaden, Movement, Stats.
- **Hilft beim Modding?** Wichtig für NPCs, Spielerinteraktion und eigene Entity-Logik.

### `com/hypixel/hytale/server/core/modules/entity/player/`

- **Java-Dateien direkt:** 19
- **Java-Dateien gesamt:** 19
- **Beispiele:** ApplyRandomSkinPersistedComponent.java, ChunkTracker.java, KnockbackPredictionSystems.java, KnockbackSimulation.java, PlayerCameraAddSystem.java, PlayerChunkTrackerSystems.java, PlayerConnectionFlushSystem.java, PlayerCreativeSettings.java …
- **Was passiert hier?** Entity-System: Spieler, Kreaturen, Komponenten, Schaden, Movement, Stats.
- **Hilft beim Modding?** Wichtig für NPCs, Spielerinteraktion und eigene Entity-Logik.

### `com/hypixel/hytale/server/core/modules/entity/repulsion/`

- **Java-Dateien direkt:** 4
- **Java-Dateien gesamt:** 4
- **Beispiele:** Repulsion.java, RepulsionConfig.java, RepulsionConfigPacketGenerator.java, RepulsionSystems.java
- **Was passiert hier?** Entity-System: Spieler, Kreaturen, Komponenten, Schaden, Movement, Stats.
- **Hilft beim Modding?** Wichtig für NPCs, Spielerinteraktion und eigene Entity-Logik.

### `com/hypixel/hytale/server/core/modules/entity/stamina/`

- **Java-Dateien direkt:** 4
- **Java-Dateien gesamt:** 4
- **Beispiele:** SprintStaminaRegenDelay.java, StaminaGameplayConfig.java, StaminaModule.java, StaminaSystems.java
- **Was passiert hier?** Entity-System: Spieler, Kreaturen, Komponenten, Schaden, Movement, Stats.
- **Hilft beim Modding?** Wichtig für NPCs, Spielerinteraktion und eigene Entity-Logik.

### `com/hypixel/hytale/server/core/modules/entity/system/`

- **Java-Dateien direkt:** 19
- **Java-Dateien gesamt:** 19
- **Beispiele:** AudioSystems.java, DisplayNameSystems.java, EntityInteractableSystems.java, EntitySpatialSystem.java, EntitySystems.java, HideEntitySystems.java, IntangibleSystems.java, InvulnerableSystems.java …
- **Was passiert hier?** Entity-System: Spieler, Kreaturen, Komponenten, Schaden, Movement, Stats.
- **Hilft beim Modding?** Wichtig für NPCs, Spielerinteraktion und eigene Entity-Logik.

### `com/hypixel/hytale/server/core/modules/entity/teleport/`

- **Java-Dateien direkt:** 4
- **Java-Dateien gesamt:** 4
- **Beispiele:** PendingTeleport.java, Teleport.java, TeleportRecord.java, TeleportSystems.java
- **Was passiert hier?** Entity-System: Spieler, Kreaturen, Komponenten, Schaden, Movement, Stats.
- **Hilft beim Modding?** Wichtig für NPCs, Spielerinteraktion und eigene Entity-Logik.

### `com/hypixel/hytale/server/core/modules/entity/tracker/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** EntityTrackerSystems.java, NetworkId.java
- **Was passiert hier?** Entity-System: Spieler, Kreaturen, Komponenten, Schaden, Movement, Stats.
- **Hilft beim Modding?** Wichtig für NPCs, Spielerinteraktion und eigene Entity-Logik.

### `com/hypixel/hytale/server/core/modules/entitystats/`

- **Java-Dateien direkt:** 5
- **Java-Dateien gesamt:** 12
- **Beispiele:** EntityStatMap.java, EntityStatValue.java, EntityStatsModule.java, EntityStatsSystems.java, RegeneratingValue.java
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/server/core/modules/entitystats/asset/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 4
- **Beispiele:** DefaultEntityStatTypes.java, EntityStatType.java, EntityStatTypePacketGenerator.java
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/server/core/modules/entitystats/asset/modifier/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** RegeneratingModifier.java
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/server/core/modules/entitystats/modifier/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** DefaultModifiers.java, Modifier.java, StaticModifier.java
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/server/core/modules/entityui/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 11
- **Beispiele:** EntityUIModule.java, UIComponentList.java, UIComponentSystems.java
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/server/core/modules/entityui/asset/`

- **Java-Dateien direkt:** 8
- **Java-Dateien gesamt:** 8
- **Beispiele:** CombatTextUIComponent.java, CombatTextUIComponentAnimationEvent.java, CombatTextUIComponentOpacityAnimationEvent.java, CombatTextUIComponentPositionAnimationEvent.java, CombatTextUIComponentScaleAnimationEvent.java, EntityStatUIComponent.java, EntityUIComponent.java, EntityUIComponentPacketGenerator.java
- **Was passiert hier?** Asset-System: Assets laden, validieren, registrieren und Asset Packs verwalten.
- **Hilft beim Modding?** Wichtig, wenn deine Mod eigene JSONs, UI, Models oder Prefabs mitbringt.

### `com/hypixel/hytale/server/core/modules/i18n/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 8
- **Beispiele:** I18nModule.java
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/server/core/modules/i18n/commands/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** EnableTmpTagsCommand.java, GenerateI18nCommand.java, InternationalizationCommands.java
- **Was passiert hier?** Command-System und fertige Server-/Spieler-/Debug-Commands.
- **Hilft beim Modding?** Sehr wichtig: Vorlage für eigene Admin-Commands wie /keystone marker oder /npc spawn.

### `com/hypixel/hytale/server/core/modules/i18n/event/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** GenerateDefaultLanguageEvent.java, MessagesUpdated.java
- **Was passiert hier?** Event-System: Ereignisse registrieren und auslösen.
- **Hilft beim Modding?** Wichtig, um auf Server-Events zu reagieren, ohne überall Code zu patchen.

### `com/hypixel/hytale/server/core/modules/i18n/generator/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** TranslationMap.java
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/server/core/modules/i18n/parser/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** LangFileParser.java
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/server/core/modules/interaction/`

- **Java-Dateien direkt:** 7
- **Java-Dateien gesamt:** 131
- **Beispiele:** BlockHarvestUtils.java, BlockInteractionUtils.java, BlockPlaceUtils.java, IInteractionSimulationHandler.java, InteractionModule.java, InteractionSimulationHandler.java, Interactions.java
- **Was passiert hier?** Interaktionen: Klicks, Items, Blocks, Chains und Gameplay-Aktionen.
- **Hilft beim Modding?** Wichtig, wenn Spieler mit Blöcken/NPCs deiner Mod interagieren sollen.

### `com/hypixel/hytale/server/core/modules/interaction/blocktrack/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** BlockCounter.java, TrackedPlacement.java
- **Was passiert hier?** Interaktionen: Klicks, Items, Blocks, Chains und Gameplay-Aktionen.
- **Hilft beim Modding?** Wichtig, wenn Spieler mit Blöcken/NPCs deiner Mod interagieren sollen.

### `com/hypixel/hytale/server/core/modules/interaction/commands/`

- **Java-Dateien direkt:** 6
- **Java-Dateien gesamt:** 6
- **Beispiele:** InteractionClearCommand.java, InteractionCommand.java, InteractionRunCommand.java, InteractionRunSpecificCommand.java, InteractionSetSnapshotSourceCommand.java, InteractionSnapshotSourceCommand.java
- **Was passiert hier?** Command-System und fertige Server-/Spieler-/Debug-Commands.
- **Hilft beim Modding?** Sehr wichtig: Vorlage für eigene Admin-Commands wie /keystone marker oder /npc spawn.

### `com/hypixel/hytale/server/core/modules/interaction/components/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** PlacedByInteractionComponent.java
- **Was passiert hier?** ECS/Component-System: Daten hängen als Komponenten an Entities/Chunks/Systeme.
- **Hilft beim Modding?** Sehr wichtig: Hytale nutzt stark Component-Logik statt einfache Minecraft-Objekte.

### `com/hypixel/hytale/server/core/modules/interaction/interaction/`

- **Java-Dateien direkt:** 5
- **Java-Dateien gesamt:** 113
- **Beispiele:** CooldownHandler.java, InteractionPacketGenerator.java, RootInteractionPacketGenerator.java, UnarmedInteractions.java, UnarmedInteractionsPacketGenerator.java
- **Was passiert hier?** Interaktionen: Klicks, Items, Blocks, Chains und Gameplay-Aktionen.
- **Hilft beim Modding?** Wichtig, wenn Spieler mit Blöcken/NPCs deiner Mod interagieren sollen.

### `com/hypixel/hytale/server/core/modules/interaction/interaction/config/`

- **Java-Dateien direkt:** 13
- **Java-Dateien gesamt:** 102
- **Beispiele:** Interaction.java, InteractionCameraSettings.java, InteractionConfiguration.java, InteractionEffects.java, InteractionPriority.java, InteractionPriorityCodec.java, InteractionRules.java, InteractionTypeUtils.java …
- **Was passiert hier?** Interaktionen: Klicks, Items, Blocks, Chains und Gameplay-Aktionen.
- **Hilft beim Modding?** Wichtig, wenn Spieler mit Blöcken/NPCs deiner Mod interagieren sollen.

### `com/hypixel/hytale/server/core/modules/interaction/interaction/config/client/`

- **Java-Dateien direkt:** 25
- **Java-Dateien gesamt:** 25
- **Beispiele:** AddItemInteraction.java, ApplyForceInteraction.java, BlockConditionInteraction.java, BreakBlockInteraction.java, ChainingInteraction.java, ChangeBlockInteraction.java, ChangeStateInteraction.java, ChargingInteraction.java …
- **Was passiert hier?** Interaktionen: Klicks, Items, Blocks, Chains und Gameplay-Aktionen.
- **Hilft beim Modding?** Wichtig, wenn Spieler mit Blöcken/NPCs deiner Mod interagieren sollen.

### `com/hypixel/hytale/server/core/modules/interaction/interaction/config/data/`

- **Java-Dateien direkt:** 6
- **Java-Dateien gesamt:** 6
- **Beispiele:** Collector.java, CollectorTag.java, ListCollector.java, SingleCollector.java, StringTag.java, TreeCollector.java
- **Was passiert hier?** Interaktionen: Klicks, Items, Blocks, Chains und Gameplay-Aktionen.
- **Hilft beim Modding?** Wichtig, wenn Spieler mit Blöcken/NPCs deiner Mod interagieren sollen.

### `com/hypixel/hytale/server/core/modules/interaction/interaction/config/none/`

- **Java-Dateien direkt:** 16
- **Java-Dateien gesamt:** 19
- **Beispiele:** BuilderToolInteraction.java, CameraInteraction.java, CancelChainInteraction.java, ChainFlagInteraction.java, ChangeActiveSlotInteraction.java, ConditionInteraction.java, EffectConditionInteraction.java, ParallelInteraction.java …
- **Was passiert hier?** Interaktionen: Klicks, Items, Blocks, Chains und Gameplay-Aktionen.
- **Hilft beim Modding?** Wichtig, wenn Spieler mit Blöcken/NPCs deiner Mod interagieren sollen.

### `com/hypixel/hytale/server/core/modules/interaction/interaction/config/none/simple/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** ApplyEffectInteraction.java, RemoveEntityInteraction.java, SendMessageInteraction.java
- **Was passiert hier?** Interaktionen: Klicks, Items, Blocks, Chains und Gameplay-Aktionen.
- **Hilft beim Modding?** Wichtig, wenn Spieler mit Blöcken/NPCs deiner Mod interagieren sollen.

### `com/hypixel/hytale/server/core/modules/interaction/interaction/config/selector/`

- **Java-Dateien direkt:** 10
- **Java-Dateien gesamt:** 10
- **Beispiele:** AOECircleSelector.java, AOECylinderSelector.java, ClientSourcedSelector.java, HorizontalSelector.java, PlayerMatcher.java, RaycastSelector.java, Selector.java, SelectorType.java …
- **Was passiert hier?** Interaktionen: Klicks, Items, Blocks, Chains und Gameplay-Aktionen.
- **Hilft beim Modding?** Wichtig, wenn Spieler mit Blöcken/NPCs deiner Mod interagieren sollen.

### `com/hypixel/hytale/server/core/modules/interaction/interaction/config/server/`

- **Java-Dateien direkt:** 21
- **Java-Dateien gesamt:** 29
- **Beispiele:** ChangeStatBaseInteraction.java, ChangeStatInteraction.java, ChangeStatWithModifierInteraction.java, CheckUniqueItemUsageInteraction.java, ClearEntityEffectInteraction.java, DamageEntityInteraction.java, DoorInteraction.java, EquipItemInteraction.java …
- **Was passiert hier?** Interaktionen: Klicks, Items, Blocks, Chains und Gameplay-Aktionen.
- **Hilft beim Modding?** Wichtig, wenn Spieler mit Blöcken/NPCs deiner Mod interagieren sollen.

### `com/hypixel/hytale/server/core/modules/interaction/interaction/config/server/combat/`

- **Java-Dateien direkt:** 8
- **Java-Dateien gesamt:** 8
- **Beispiele:** DamageCalculator.java, DamageClass.java, DamageEffects.java, DirectionalKnockback.java, ForceKnockback.java, Knockback.java, PointKnockback.java, TargetEntityEffect.java
- **Was passiert hier?** Interaktionen: Klicks, Items, Blocks, Chains und Gameplay-Aktionen.
- **Hilft beim Modding?** Wichtig, wenn Spieler mit Blöcken/NPCs deiner Mod interagieren sollen.

### `com/hypixel/hytale/server/core/modules/interaction/interaction/operation/`

- **Java-Dateien direkt:** 4
- **Java-Dateien gesamt:** 4
- **Beispiele:** JumpOperation.java, Label.java, Operation.java, OperationsBuilder.java
- **Was passiert hier?** Interaktionen: Klicks, Items, Blocks, Chains und Gameplay-Aktionen.
- **Hilft beim Modding?** Wichtig, wenn Spieler mit Blöcken/NPCs deiner Mod interagieren sollen.

### `com/hypixel/hytale/server/core/modules/interaction/interaction/util/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** InteractionTarget.java, InteractionValidation.java
- **Was passiert hier?** Interaktionen: Klicks, Items, Blocks, Chains und Gameplay-Aktionen.
- **Hilft beim Modding?** Wichtig, wenn Spieler mit Blöcken/NPCs deiner Mod interagieren sollen.

### `com/hypixel/hytale/server/core/modules/interaction/suppliers/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** ItemRepairPageSupplier.java
- **Was passiert hier?** Interaktionen: Klicks, Items, Blocks, Chains und Gameplay-Aktionen.
- **Hilft beim Modding?** Wichtig, wenn Spieler mit Blöcken/NPCs deiner Mod interagieren sollen.

### `com/hypixel/hytale/server/core/modules/interaction/system/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** InteractionSystems.java
- **Was passiert hier?** Interaktionen: Klicks, Items, Blocks, Chains und Gameplay-Aktionen.
- **Hilft beim Modding?** Wichtig, wenn Spieler mit Blöcken/NPCs deiner Mod interagieren sollen.

### `com/hypixel/hytale/server/core/modules/item/`

- **Java-Dateien direkt:** 6
- **Java-Dateien gesamt:** 7
- **Beispiele:** CraftingRecipePacketGenerator.java, ItemModule.java, ItemPacketGenerator.java, ItemQualityPacketGenerator.java, ItemReticleConfigPacketGenerator.java, RecipePacketGenerator.java
- **Was passiert hier?** Inventar-/Item-System.
- **Hilft beim Modding?** Wichtig für Belohnungen, Kosten, Shop, Energy Crystals oder NPC-Handel.

### `com/hypixel/hytale/server/core/modules/item/commands/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** SpawnItemCommand.java
- **Was passiert hier?** Command-System und fertige Server-/Spieler-/Debug-Commands.
- **Hilft beim Modding?** Sehr wichtig: Vorlage für eigene Admin-Commands wie /keystone marker oder /npc spawn.

### `com/hypixel/hytale/server/core/modules/migrations/`

- **Java-Dateien direkt:** 4
- **Java-Dateien gesamt:** 4
- **Beispiele:** ChunkColumnMigrationSystem.java, ChunkSectionMigrationSystem.java, Migration.java, MigrationModule.java
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/server/core/modules/physics/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 21
- **Beispiele:** RestingSupport.java, SimplePhysicsProvider.java
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/server/core/modules/physics/component/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** PhysicsValues.java, Velocity.java
- **Was passiert hier?** ECS/Component-System: Daten hängen als Komponenten an Entities/Chunks/Systeme.
- **Hilft beim Modding?** Sehr wichtig: Hytale nutzt stark Component-Logik statt einfache Minecraft-Objekte.

### `com/hypixel/hytale/server/core/modules/physics/systems/`

- **Java-Dateien direkt:** 4
- **Java-Dateien gesamt:** 4
- **Beispiele:** GenericVelocityInstructionSystem.java, IVelocityModifyingSystem.java, PhysicsValuesAddSystem.java, VelocitySystems.java
- **Was passiert hier?** System-/Task-Code: wiederkehrende oder eventbasierte Verarbeitung.
- **Hilft beim Modding?** Wichtig für saubere Serverlogik ohne dauernde Welt-Scans.

### `com/hypixel/hytale/server/core/modules/physics/util/`

- **Java-Dateien direkt:** 13
- **Java-Dateien gesamt:** 13
- **Beispiele:** ForceAccumulator.java, ForceProvider.java, ForceProviderEntity.java, ForceProviderStandard.java, ForceProviderStandardState.java, PhysicsBodyState.java, PhysicsBodyStateUpdater.java, PhysicsBodyStateUpdaterMidpoint.java …
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/server/core/modules/prefabspawner/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 7
- **Beispiele:** PrefabSpawnerBlock.java, PrefabSpawnerModule.java
- **Was passiert hier?** Prefab-System: platzierbare Strukturen/Objekte und Spawner.
- **Hilft beim Modding?** Wichtig für Häuser, Marker und Settlement-Strukturen.

### `com/hypixel/hytale/server/core/modules/prefabspawner/commands/`

- **Java-Dateien direkt:** 5
- **Java-Dateien gesamt:** 5
- **Beispiele:** PrefabSpawnerCommand.java, PrefabSpawnerGetCommand.java, PrefabSpawnerSetCommand.java, PrefabSpawnerWeightCommand.java, TargetPrefabSpawnerCommand.java
- **Was passiert hier?** Command-System und fertige Server-/Spieler-/Debug-Commands.
- **Hilft beim Modding?** Sehr wichtig: Vorlage für eigene Admin-Commands wie /keystone marker oder /npc spawn.

### `com/hypixel/hytale/server/core/modules/projectile/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 15
- **Beispiele:** ProjectileModule.java
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/server/core/modules/projectile/component/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** PredictedProjectile.java, Projectile.java
- **Was passiert hier?** ECS/Component-System: Daten hängen als Komponenten an Entities/Chunks/Systeme.
- **Hilft beim Modding?** Sehr wichtig: Hytale nutzt stark Component-Logik statt einfache Minecraft-Objekte.

### `com/hypixel/hytale/server/core/modules/projectile/config/`

- **Java-Dateien direkt:** 9
- **Java-Dateien gesamt:** 9
- **Beispiele:** BallisticData.java, BallisticDataProvider.java, BounceConsumer.java, ImpactConsumer.java, PhysicsConfig.java, ProjectileConfig.java, ProjectileConfigPacketGenerator.java, StandardPhysicsConfig.java …
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/server/core/modules/projectile/interaction/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** ProjectileInteraction.java
- **Was passiert hier?** Interaktionen: Klicks, Items, Blocks, Chains und Gameplay-Aktionen.
- **Hilft beim Modding?** Wichtig, wenn Spieler mit Blöcken/NPCs deiner Mod interagieren sollen.

### `com/hypixel/hytale/server/core/modules/projectile/system/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** PredictedProjectileSystems.java, StandardPhysicsTickSystem.java
- **Was passiert hier?** System-/Task-Code: wiederkehrende oder eventbasierte Verarbeitung.
- **Hilft beim Modding?** Wichtig für saubere Serverlogik ohne dauernde Welt-Scans.

### `com/hypixel/hytale/server/core/modules/serverplayerlist/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** ServerPlayerListModule.java
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/server/core/modules/singleplayer/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 7
- **Beispiele:** SingleplayerModule.java, SingleplayerRequestAccessEvent.java
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/server/core/modules/singleplayer/commands/`

- **Java-Dateien direkt:** 5
- **Java-Dateien gesamt:** 5
- **Beispiele:** PlayCommand.java, PlayCommandBase.java, PlayFriendCommand.java, PlayLanCommand.java, PlayOnlineCommand.java
- **Was passiert hier?** Command-System und fertige Server-/Spieler-/Debug-Commands.
- **Hilft beim Modding?** Sehr wichtig: Vorlage für eigene Admin-Commands wie /keystone marker oder /npc spawn.

### `com/hypixel/hytale/server/core/modules/splitvelocity/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** SplitVelocity.java, VelocityConfig.java
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/server/core/modules/time/`

- **Java-Dateien direkt:** 6
- **Java-Dateien gesamt:** 7
- **Beispiele:** TimeModule.java, TimePacketSystem.java, TimeResource.java, TimeSystem.java, WorldTimeResource.java, WorldTimeSystems.java
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/server/core/modules/time/commands/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** TimeCommand.java
- **Was passiert hier?** Command-System und fertige Server-/Spieler-/Debug-Commands.
- **Hilft beim Modding?** Sehr wichtig: Vorlage für eigene Admin-Commands wie /keystone marker oder /npc spawn.

### `com/hypixel/hytale/server/core/modules/voice/`

- **Java-Dateien direkt:** 6
- **Java-Dateien gesamt:** 7
- **Beispiele:** VoiceModule.java, VoiceModuleConfig.java, VoicePacketHandler.java, VoicePlayerState.java, VoiceRouter.java, VoiceStreamHandler.java
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/server/core/modules/voice/commands/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** VoiceCommand.java
- **Was passiert hier?** Command-System und fertige Server-/Spieler-/Debug-Commands.
- **Hilft beim Modding?** Sehr wichtig: Vorlage für eigene Admin-Commands wie /keystone marker oder /npc spawn.

### `com/hypixel/hytale/server/core/permissions/`

- **Java-Dateien direkt:** 4
- **Java-Dateien gesamt:** 18
- **Beispiele:** HytalePermissions.java, PermissionHolder.java, PermissionValidation.java, PermissionsModule.java
- **Was passiert hier?** Rechte-/Permission-System.
- **Hilft beim Modding?** Wichtig für Admin-Commands und Spielerrollen.

### `com/hypixel/hytale/server/core/permissions/commands/`

- **Java-Dateien direkt:** 8
- **Java-Dateien gesamt:** 12
- **Beispiele:** GroupArgumentType.java, PermCommand.java, PermGroupCommand.java, PermListCommand.java, PermReloadCommand.java, PermTestCommand.java, PermUserCommand.java, SetGroupCommand.java
- **Was passiert hier?** Command-System und fertige Server-/Spieler-/Debug-Commands.
- **Hilft beim Modding?** Sehr wichtig: Vorlage für eigene Admin-Commands wie /keystone marker oder /npc spawn.

### `com/hypixel/hytale/server/core/permissions/commands/op/`

- **Java-Dateien direkt:** 4
- **Java-Dateien gesamt:** 4
- **Beispiele:** OpAddCommand.java, OpCommand.java, OpRemoveCommand.java, OpSelfCommand.java
- **Was passiert hier?** Command-System und fertige Server-/Spieler-/Debug-Commands.
- **Hilft beim Modding?** Sehr wichtig: Vorlage für eigene Admin-Commands wie /keystone marker oder /npc spawn.

### `com/hypixel/hytale/server/core/permissions/provider/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** HytalePermissionsProvider.java, PermissionProvider.java
- **Was passiert hier?** Rechte-/Permission-System.
- **Hilft beim Modding?** Wichtig für Admin-Commands und Spielerrollen.

### `com/hypixel/hytale/server/core/plugin/`

- **Java-Dateien direkt:** 10
- **Java-Dateien gesamt:** 20
- **Beispiele:** JavaPlugin.java, JavaPluginInit.java, MissingPluginDependencyException.java, PluginBase.java, PluginClassLoader.java, PluginInit.java, PluginListPageManager.java, PluginManager.java …
- **Was passiert hier?** Plugin-System: Laden, Manifest, Lifecycle und Plugin-Registry.
- **Hilft beim Modding?** Sehr wichtig: hier lernst du, wie Mods/Plugins geladen und verwaltet werden.

### `com/hypixel/hytale/server/core/plugin/commands/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** PluginCommand.java
- **Was passiert hier?** Plugin-System: Laden, Manifest, Lifecycle und Plugin-Registry.
- **Hilft beim Modding?** Sehr wichtig: hier lernst du, wie Mods/Plugins geladen und verwaltet werden.

### `com/hypixel/hytale/server/core/plugin/event/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** PluginEvent.java, PluginSetupEvent.java
- **Was passiert hier?** Plugin-System: Laden, Manifest, Lifecycle und Plugin-Registry.
- **Hilft beim Modding?** Sehr wichtig: hier lernst du, wie Mods/Plugins geladen und verwaltet werden.

### `com/hypixel/hytale/server/core/plugin/pages/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** PluginListPage.java
- **Was passiert hier?** Plugin-System: Laden, Manifest, Lifecycle und Plugin-Registry.
- **Hilft beim Modding?** Sehr wichtig: hier lernst du, wie Mods/Plugins geladen und verwaltet werden.

### `com/hypixel/hytale/server/core/plugin/pending/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** PendingLoadJavaPlugin.java, PendingLoadPlugin.java
- **Was passiert hier?** Plugin-System: Laden, Manifest, Lifecycle und Plugin-Registry.
- **Hilft beim Modding?** Sehr wichtig: hier lernst du, wie Mods/Plugins geladen und verwaltet werden.

### `com/hypixel/hytale/server/core/plugin/registry/`

- **Java-Dateien direkt:** 4
- **Java-Dateien gesamt:** 4
- **Beispiele:** AssetRegistry.java, CodecMapRegistry.java, IRegistry.java, MapKeyMapRegistry.java
- **Was passiert hier?** Plugin-System: Laden, Manifest, Lifecycle und Plugin-Registry.
- **Hilft beim Modding?** Sehr wichtig: hier lernst du, wie Mods/Plugins geladen und verwaltet werden.

### `com/hypixel/hytale/server/core/prefab/`

- **Java-Dateien direkt:** 7
- **Java-Dateien gesamt:** 30
- **Beispiele:** PrefabCopyableComponent.java, PrefabEntry.java, PrefabLoadException.java, PrefabRotation.java, PrefabSaveException.java, PrefabStore.java, PrefabWeights.java
- **Was passiert hier?** Prefab-System: platzierbare Strukturen/Objekte und Spawner.
- **Hilft beim Modding?** Wichtig für Häuser, Marker und Settlement-Strukturen.

### `com/hypixel/hytale/server/core/prefab/config/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** SelectionPrefabSerializer.java
- **Was passiert hier?** Prefab-System: platzierbare Strukturen/Objekte und Spawner.
- **Hilft beim Modding?** Wichtig für Häuser, Marker und Settlement-Strukturen.

### `com/hypixel/hytale/server/core/prefab/event/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** PrefabPasteEvent.java, PrefabPlaceEntityEvent.java
- **Was passiert hier?** Prefab-System: platzierbare Strukturen/Objekte und Spawner.
- **Hilft beim Modding?** Wichtig für Häuser, Marker und Settlement-Strukturen.

### `com/hypixel/hytale/server/core/prefab/selection/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 20
- **Beispiele:** SelectionManager.java, SelectionProvider.java
- **Was passiert hier?** Prefab-System: platzierbare Strukturen/Objekte und Spawner.
- **Hilft beim Modding?** Wichtig für Häuser, Marker und Settlement-Strukturen.

### `com/hypixel/hytale/server/core/prefab/selection/buffer/`

- **Java-Dateien direkt:** 7
- **Java-Dateien gesamt:** 11
- **Beispiele:** BinaryPrefabBufferCodec.java, BsonPrefabBufferDeserializer.java, PrefabBufferCall.java, PrefabBufferUtil.java, PrefabLoader.java, PrefabSupplier.java, UpdateBinaryPrefabException.java
- **Was passiert hier?** Prefab-System: platzierbare Strukturen/Objekte und Spawner.
- **Hilft beim Modding?** Wichtig für Häuser, Marker und Settlement-Strukturen.

### `com/hypixel/hytale/server/core/prefab/selection/buffer/impl/`

- **Java-Dateien direkt:** 4
- **Java-Dateien gesamt:** 4
- **Beispiele:** IPrefabBuffer.java, PrefabBuffer.java, PrefabBufferBlockEntry.java, PrefabBufferColumn.java
- **Was passiert hier?** Prefab-System: platzierbare Strukturen/Objekte und Spawner.
- **Hilft beim Modding?** Wichtig für Häuser, Marker und Settlement-Strukturen.

### `com/hypixel/hytale/server/core/prefab/selection/mask/`

- **Java-Dateien direkt:** 4
- **Java-Dateien gesamt:** 4
- **Beispiele:** BlockFilter.java, BlockMask.java, BlockPattern.java, MultiBlockMask.java
- **Was passiert hier?** Prefab-System: platzierbare Strukturen/Objekte und Spawner.
- **Hilft beim Modding?** Wichtig für Häuser, Marker und Settlement-Strukturen.

### `com/hypixel/hytale/server/core/prefab/selection/standard/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** BlockSelection.java, FeedbackConsumer.java, RotateBlockMode.java
- **Was passiert hier?** Prefab-System: platzierbare Strukturen/Objekte und Spawner.
- **Hilft beim Modding?** Wichtig für Häuser, Marker und Settlement-Strukturen.

### `com/hypixel/hytale/server/core/receiver/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** IEventTitleReceiver.java, IMessageReceiver.java, IPacketReceiver.java
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/server/core/registry/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** ClientFeatureRegistration.java, ClientFeatureRegistry.java
- **Was passiert hier?** Registry-System: Dinge anmelden und später wiederfinden.
- **Hilft beim Modding?** Wichtig für Modding, weil vieles erst registriert werden muss.

### `com/hypixel/hytale/server/core/schema/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** SchemaGenerator.java
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/server/core/task/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** TaskRegistration.java, TaskRegistry.java
- **Was passiert hier?** System-/Task-Code: wiederkehrende oder eventbasierte Verarbeitung.
- **Hilft beim Modding?** Wichtig für saubere Serverlogik ohne dauernde Welt-Scans.

### `com/hypixel/hytale/server/core/telemetry/`

- **Java-Dateien direkt:** 6
- **Java-Dateien gesamt:** 6
- **Beispiele:** TelemetryDataCollector.java, TelemetryJsonSerializer.java, TelemetryModule.java, TelemetryPackets.java, TelemetryService.java, TelemetryStorage.java
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/server/core/ui/`

- **Java-Dateien direkt:** 8
- **Java-Dateien gesamt:** 18
- **Beispiele:** Anchor.java, Area.java, DropdownEntryInfo.java, ItemGridSlot.java, LocalizableString.java, PatchStyle.java, Value.java, ValueCodec.java
- **Was passiert hier?** UI/Page-System für servergesteuerte Oberflächen.
- **Hilft beim Modding?** Wichtig für spätere Mod-Menüs, Admin-Seiten oder NPC-Dialoge.

### `com/hypixel/hytale/server/core/ui/browser/`

- **Java-Dateien direkt:** 7
- **Java-Dateien gesamt:** 7
- **Beispiele:** AssetPackSaveBrowser.java, AssetPackSaveBrowserConfig.java, AssetPackSaveBrowserEventData.java, FileBrowserConfig.java, FileBrowserEventData.java, FileListProvider.java, ServerFileBrowser.java
- **Was passiert hier?** UI/Page-System für servergesteuerte Oberflächen.
- **Hilft beim Modding?** Wichtig für spätere Mod-Menüs, Admin-Seiten oder NPC-Dialoge.

### `com/hypixel/hytale/server/core/ui/builder/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** EventData.java, UICommandBuilder.java, UIEventBuilder.java
- **Was passiert hier?** UI/Page-System für servergesteuerte Oberflächen.
- **Hilft beim Modding?** Wichtig für spätere Mod-Menüs, Admin-Seiten oder NPC-Dialoge.

### `com/hypixel/hytale/server/core/universe/`

- **Java-Dateien direkt:** 4
- **Java-Dateien gesamt:** 218
- **Beispiele:** PlayerRef.java, StorageManager.java, Universe.java, WorldLoadCancelledException.java
- **Was passiert hier?** Welt-/Universum-System: Worlds, Chunks, Storage, PlayerData und Welt-Events.
- **Hilft beim Modding?** Wichtig für Positionen, Marker, Persistenz und Weltzugriff.

### `com/hypixel/hytale/server/core/universe/datastore/`

- **Java-Dateien direkt:** 4
- **Java-Dateien gesamt:** 4
- **Beispiele:** DataStore.java, DataStoreProvider.java, DiskDataStore.java, DiskDataStoreProvider.java
- **Was passiert hier?** Welt-/Universum-System: Worlds, Chunks, Storage, PlayerData und Welt-Events.
- **Hilft beim Modding?** Wichtig für Positionen, Marker, Persistenz und Weltzugriff.

### `com/hypixel/hytale/server/core/universe/playerdata/`

- **Java-Dateien direkt:** 4
- **Java-Dateien gesamt:** 4
- **Beispiele:** DefaultPlayerStorageProvider.java, DiskPlayerStorageProvider.java, PlayerStorage.java, PlayerStorageProvider.java
- **Was passiert hier?** Welt-/Universum-System: Worlds, Chunks, Storage, PlayerData und Welt-Events.
- **Hilft beim Modding?** Wichtig für Positionen, Marker, Persistenz und Weltzugriff.

### `com/hypixel/hytale/server/core/universe/system/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** PlayerRefAddedSystem.java, PlayerVelocityInstructionSystem.java, WorldConfigSaveSystem.java
- **Was passiert hier?** Welt-/Universum-System: Worlds, Chunks, Storage, PlayerData und Welt-Events.
- **Hilft beim Modding?** Wichtig für Positionen, Marker, Persistenz und Weltzugriff.

### `com/hypixel/hytale/server/core/universe/world/`

- **Java-Dateien direkt:** 16
- **Java-Dateien gesamt:** 203
- **Beispiele:** ClientEffectWorldSettings.java, IWorldChunks.java, IWorldChunksAsync.java, ParticleUtil.java, PlaceBlockSettings.java, PlayerUtil.java, SetBlockSettings.java, SoundUtil.java …
- **Was passiert hier?** Welt-/Universum-System: Worlds, Chunks, Storage, PlayerData und Welt-Events.
- **Hilft beim Modding?** Wichtig für Positionen, Marker, Persistenz und Weltzugriff.

### `com/hypixel/hytale/server/core/universe/world/accessor/`

- **Java-Dateien direkt:** 6
- **Java-Dateien gesamt:** 6
- **Beispiele:** BlockAccessor.java, ChunkAccessor.java, EmptyBlockAccessor.java, IChunkAccessorSync.java, LocalCachedChunkAccessor.java, OverridableChunkAccessor.java
- **Was passiert hier?** Welt-/Universum-System: Worlds, Chunks, Storage, PlayerData und Welt-Events.
- **Hilft beim Modding?** Wichtig für Positionen, Marker, Persistenz und Weltzugriff.

### `com/hypixel/hytale/server/core/universe/world/chunk/`

- **Java-Dateien direkt:** 9
- **Java-Dateien gesamt:** 32
- **Beispiele:** AbstractCachedAccessor.java, BlockChunk.java, BlockComponentChunk.java, BlockOperations.java, BlockRotationUtil.java, ChunkColumn.java, ChunkFlag.java, EntityChunk.java …
- **Was passiert hier?** Welt-/Universum-System: Worlds, Chunks, Storage, PlayerData und Welt-Events.
- **Hilft beim Modding?** Wichtig für Positionen, Marker, Persistenz und Weltzugriff.

### `com/hypixel/hytale/server/core/universe/world/chunk/environment/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** EnvironmentChunk.java, EnvironmentColumn.java, EnvironmentRange.java
- **Was passiert hier?** Welt-/Universum-System: Worlds, Chunks, Storage, PlayerData und Welt-Events.
- **Hilft beim Modding?** Wichtig für Positionen, Marker, Persistenz und Weltzugriff.

### `com/hypixel/hytale/server/core/universe/world/chunk/palette/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** BitFieldArr.java, IntBytePalette.java, ShortBytePalette.java
- **Was passiert hier?** Welt-/Universum-System: Worlds, Chunks, Storage, PlayerData und Welt-Events.
- **Hilft beim Modding?** Wichtig für Positionen, Marker, Persistenz und Weltzugriff.

### `com/hypixel/hytale/server/core/universe/world/chunk/section/`

- **Java-Dateien direkt:** 6
- **Java-Dateien gesamt:** 16
- **Beispiele:** BlockSection.java, ChunkLightData.java, ChunkLightDataBuilder.java, ChunkSection.java, ChunkSectionReference.java, FluidSection.java
- **Was passiert hier?** Welt-/Universum-System: Worlds, Chunks, Storage, PlayerData und Welt-Events.
- **Hilft beim Modding?** Wichtig für Positionen, Marker, Persistenz und Weltzugriff.

### `com/hypixel/hytale/server/core/universe/world/chunk/section/blockpositions/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** BlockPositionData.java, BlockPositionProvider.java, IBlockPositionData.java
- **Was passiert hier?** Welt-/Universum-System: Worlds, Chunks, Storage, PlayerData und Welt-Events.
- **Hilft beim Modding?** Wichtig für Positionen, Marker, Persistenz und Weltzugriff.

### `com/hypixel/hytale/server/core/universe/world/chunk/section/palette/`

- **Java-Dateien direkt:** 7
- **Java-Dateien gesamt:** 7
- **Beispiele:** AbstractSectionPalette.java, ByteSectionPalette.java, EmptySectionPalette.java, HalfByteSectionPalette.java, PaletteSetProvider.java, PaletteTypeEnum.java, ShortSectionPalette.java
- **Was passiert hier?** Welt-/Universum-System: Worlds, Chunks, Storage, PlayerData und Welt-Events.
- **Hilft beim Modding?** Wichtig für Positionen, Marker, Persistenz und Weltzugriff.

### `com/hypixel/hytale/server/core/universe/world/chunk/systems/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** ChunkSystems.java
- **Was passiert hier?** Welt-/Universum-System: Worlds, Chunks, Storage, PlayerData und Welt-Events.
- **Hilft beim Modding?** Wichtig für Positionen, Marker, Persistenz und Weltzugriff.

### `com/hypixel/hytale/server/core/universe/world/commands/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 39
- **Beispiele:** SetTickingCommand.java, WorldSettingsCommand.java
- **Was passiert hier?** Command-System und fertige Server-/Spieler-/Debug-Commands.
- **Hilft beim Modding?** Sehr wichtig: Vorlage für eigene Admin-Commands wie /keystone marker oder /npc spawn.

### `com/hypixel/hytale/server/core/universe/world/commands/block/`

- **Java-Dateien direkt:** 12
- **Java-Dateien gesamt:** 16
- **Beispiele:** BlockCommand.java, BlockGetCommand.java, BlockGetStateCommand.java, BlockInspectFillerCommand.java, BlockInspectPhysicsCommand.java, BlockInspectRotationCommand.java, BlockRowCommand.java, BlockSelectCommand.java …
- **Was passiert hier?** Command-System und fertige Server-/Spieler-/Debug-Commands.
- **Hilft beim Modding?** Sehr wichtig: Vorlage für eigene Admin-Commands wie /keystone marker oder /npc spawn.

### `com/hypixel/hytale/server/core/universe/world/commands/block/bulk/`

- **Java-Dateien direkt:** 4
- **Java-Dateien gesamt:** 4
- **Beispiele:** BlockBulkCommand.java, BlockBulkFindCommand.java, BlockBulkFindHereCommand.java, BlockBulkReplaceCommand.java
- **Was passiert hier?** Command-System und fertige Server-/Spieler-/Debug-Commands.
- **Hilft beim Modding?** Sehr wichtig: Vorlage für eigene Admin-Commands wie /keystone marker oder /npc spawn.

### `com/hypixel/hytale/server/core/universe/world/commands/world/`

- **Java-Dateien direkt:** 9
- **Java-Dateien gesamt:** 14
- **Beispiele:** WorldAddCommand.java, WorldCommand.java, WorldListCommand.java, WorldLoadCommand.java, WorldPruneCommand.java, WorldRemoveCommand.java, WorldRocksDbCommand.java, WorldSaveCommand.java …
- **Was passiert hier?** Command-System und fertige Server-/Spieler-/Debug-Commands.
- **Hilft beim Modding?** Sehr wichtig: Vorlage für eigene Admin-Commands wie /keystone marker oder /npc spawn.

### `com/hypixel/hytale/server/core/universe/world/commands/world/perf/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** WorldPerfCommand.java, WorldPerfGraphCommand.java, WorldPerfResetCommand.java
- **Was passiert hier?** Command-System und fertige Server-/Spieler-/Debug-Commands.
- **Hilft beim Modding?** Sehr wichtig: Vorlage für eigene Admin-Commands wie /keystone marker oder /npc spawn.

### `com/hypixel/hytale/server/core/universe/world/commands/world/tps/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** WorldTpsCommand.java, WorldTpsResetCommand.java
- **Was passiert hier?** Command-System und fertige Server-/Spieler-/Debug-Commands.
- **Hilft beim Modding?** Sehr wichtig: Vorlage für eigene Admin-Commands wie /keystone marker oder /npc spawn.

### `com/hypixel/hytale/server/core/universe/world/commands/worldconfig/`

- **Java-Dateien direkt:** 7
- **Java-Dateien gesamt:** 7
- **Beispiele:** WorldConfigCommand.java, WorldConfigPauseTimeCommand.java, WorldConfigSeedCommand.java, WorldConfigSetPvpCommand.java, WorldConfigSetSpawnCommand.java, WorldConfigSetSpawnDefaultCommand.java, WorldPauseCommand.java
- **Was passiert hier?** Command-System und fertige Server-/Spieler-/Debug-Commands.
- **Hilft beim Modding?** Sehr wichtig: Vorlage für eigene Admin-Commands wie /keystone marker oder /npc spawn.

### `com/hypixel/hytale/server/core/universe/world/connectedblocks/`

- **Java-Dateien direkt:** 12
- **Java-Dateien gesamt:** 16
- **Beispiele:** ConnectedBlockFaceTags.java, ConnectedBlockPatternRule.java, ConnectedBlockRuleSet.java, ConnectedBlockShape.java, ConnectedBlocksModule.java, ConnectedBlocksUtil.java, CustomConnectedBlockPattern.java, CustomConnectedBlockTemplateAsset.java …
- **Was passiert hier?** Welt-/Universum-System: Worlds, Chunks, Storage, PlayerData und Welt-Events.
- **Hilft beim Modding?** Wichtig für Positionen, Marker, Persistenz und Weltzugriff.

### `com/hypixel/hytale/server/core/universe/world/connectedblocks/builtin/`

- **Java-Dateien direkt:** 4
- **Java-Dateien gesamt:** 4
- **Beispiele:** ConnectedBlockOutput.java, RoofConnectedBlockRuleSet.java, StairConnectedBlockRuleSet.java, StairLikeConnectedBlockRuleSet.java
- **Was passiert hier?** Eingebaute Gameplay-Plugins/Module, also Beispielcode direkt aus dem Server.
- **Hilft beim Modding?** Sehr nützlich als Lernmaterial: hier siehst du echte Hytale-Plugin-Struktur.

### `com/hypixel/hytale/server/core/universe/world/events/`

- **Java-Dateien direkt:** 7
- **Java-Dateien gesamt:** 10
- **Beispiele:** AddWorldEvent.java, AllWorldsLoadedEvent.java, ChunkEvent.java, ChunkPreLoadProcessEvent.java, RemoveWorldEvent.java, StartWorldEvent.java, WorldEvent.java
- **Was passiert hier?** Event-System: Ereignisse registrieren und auslösen.
- **Hilft beim Modding?** Wichtig, um auf Server-Events zu reagieren, ohne überall Code zu patchen.

### `com/hypixel/hytale/server/core/universe/world/events/ecs/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** ChunkSaveEvent.java, ChunkUnloadEvent.java, MoonPhaseChangeEvent.java
- **Was passiert hier?** Event-System: Ereignisse registrieren und auslösen.
- **Hilft beim Modding?** Wichtig, um auf Server-Events zu reagieren, ohne überall Code zu patchen.

### `com/hypixel/hytale/server/core/universe/world/lighting/`

- **Java-Dateien direkt:** 5
- **Java-Dateien gesamt:** 5
- **Beispiele:** CalculationResult.java, ChunkLightingManager.java, FloodLightCalculation.java, FullBrightLightCalculation.java, LightCalculation.java
- **Was passiert hier?** Welt-/Universum-System: Worlds, Chunks, Storage, PlayerData und Welt-Events.
- **Hilft beim Modding?** Wichtig für Positionen, Marker, Persistenz und Weltzugriff.

### `com/hypixel/hytale/server/core/universe/world/map/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** WorldMap.java
- **Was passiert hier?** Welt-/Universum-System: Worlds, Chunks, Storage, PlayerData und Welt-Events.
- **Hilft beim Modding?** Wichtig für Positionen, Marker, Persistenz und Weltzugriff.

### `com/hypixel/hytale/server/core/universe/world/meta/`

- **Java-Dateien direkt:** 0
- **Java-Dateien gesamt:** 5
- **Beispiele:** keine direkten .java Dateien
- **Was passiert hier?** Welt-/Universum-System: Worlds, Chunks, Storage, PlayerData und Welt-Events.
- **Hilft beim Modding?** Wichtig für Positionen, Marker, Persistenz und Weltzugriff.

### `com/hypixel/hytale/server/core/universe/world/meta/state/`

- **Java-Dateien direkt:** 4
- **Java-Dateien gesamt:** 5
- **Beispiele:** BlockMapMarker.java, BlockMapMarkersResource.java, LaunchPad.java, RespawnBlock.java
- **Was passiert hier?** Welt-/Universum-System: Worlds, Chunks, Storage, PlayerData und Welt-Events.
- **Hilft beim Modding?** Wichtig für Positionen, Marker, Persistenz und Weltzugriff.

### `com/hypixel/hytale/server/core/universe/world/meta/state/exceptions/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** NoSuchBlockStateException.java
- **Was passiert hier?** Welt-/Universum-System: Worlds, Chunks, Storage, PlayerData und Welt-Events.
- **Hilft beim Modding?** Wichtig für Positionen, Marker, Persistenz und Weltzugriff.

### `com/hypixel/hytale/server/core/universe/world/npc/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** INonPlayerCharacter.java
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/core/universe/world/path/`

- **Java-Dateien direkt:** 6
- **Java-Dateien gesamt:** 6
- **Beispiele:** IPath.java, IPathWaypoint.java, SimplePathWaypoint.java, WorldPath.java, WorldPathChangedEvent.java, WorldPathConfig.java
- **Was passiert hier?** Welt-/Universum-System: Worlds, Chunks, Storage, PlayerData und Welt-Events.
- **Hilft beim Modding?** Wichtig für Positionen, Marker, Persistenz und Weltzugriff.

### `com/hypixel/hytale/server/core/universe/world/spawn/`

- **Java-Dateien direkt:** 4
- **Java-Dateien gesamt:** 4
- **Beispiele:** FitToHeightMapSpawnProvider.java, GlobalSpawnProvider.java, ISpawnProvider.java, IndividualSpawnProvider.java
- **Was passiert hier?** Welt-/Universum-System: Worlds, Chunks, Storage, PlayerData und Welt-Events.
- **Hilft beim Modding?** Wichtig für Positionen, Marker, Persistenz und Weltzugriff.

### `com/hypixel/hytale/server/core/universe/world/storage/`

- **Java-Dateien direkt:** 7
- **Java-Dateien gesamt:** 20
- **Beispiele:** BufferChunkLoader.java, BufferChunkSaver.java, ChunkStore.java, EntityStore.java, GetChunkFlags.java, IChunkLoader.java, IChunkSaver.java
- **Was passiert hier?** Welt-/Universum-System: Worlds, Chunks, Storage, PlayerData und Welt-Events.
- **Hilft beim Modding?** Wichtig für Positionen, Marker, Persistenz und Weltzugriff.

### `com/hypixel/hytale/server/core/universe/world/storage/component/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** ChunkSavingSystems.java, ChunkUnloadingSystem.java
- **Was passiert hier?** ECS/Component-System: Daten hängen als Komponenten an Entities/Chunks/Systeme.
- **Hilft beim Modding?** Sehr wichtig: Hytale nutzt stark Component-Logik statt einfache Minecraft-Objekte.

### `com/hypixel/hytale/server/core/universe/world/storage/provider/`

- **Java-Dateien direkt:** 7
- **Java-Dateien gesamt:** 7
- **Beispiele:** BackupChunkLoader.java, DefaultChunkStorageProvider.java, EmptyChunkStorageProvider.java, IChunkStorageProvider.java, IndexedStorageChunkStorageProvider.java, MigrationChunkStorageProvider.java, RocksDbChunkStorageProvider.java
- **Was passiert hier?** Welt-/Universum-System: Worlds, Chunks, Storage, PlayerData und Welt-Events.
- **Hilft beim Modding?** Wichtig für Positionen, Marker, Persistenz und Weltzugriff.

### `com/hypixel/hytale/server/core/universe/world/storage/resources/`

- **Java-Dateien direkt:** 4
- **Java-Dateien gesamt:** 4
- **Beispiele:** DefaultResourceStorageProvider.java, DiskResourceStorageProvider.java, EmptyResourceStorageProvider.java, IResourceStorageProvider.java
- **Was passiert hier?** Welt-/Universum-System: Worlds, Chunks, Storage, PlayerData und Welt-Events.
- **Hilft beim Modding?** Wichtig für Positionen, Marker, Persistenz und Weltzugriff.

### `com/hypixel/hytale/server/core/universe/world/system/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** WorldPregenerateSystem.java
- **Was passiert hier?** Welt-/Universum-System: Worlds, Chunks, Storage, PlayerData und Welt-Events.
- **Hilft beim Modding?** Wichtig für Positionen, Marker, Persistenz und Weltzugriff.

### `com/hypixel/hytale/server/core/universe/world/worldgen/`

- **Java-Dateien direkt:** 11
- **Java-Dateien gesamt:** 15
- **Beispiele:** GeneratedBlockChunk.java, GeneratedBlockStateChunk.java, GeneratedChunk.java, GeneratedChunkSection.java, GeneratedEntityChunk.java, IBenchmarkableWorldGen.java, IWorldGen.java, IWorldGenBenchmark.java …
- **Was passiert hier?** Worldgen: Biome, Zonen, Prefabs, Caves, Container und Loader.
- **Hilft beim Modding?** Später wichtig für MVP B: NPC-Häuser automatisch in der Welt platzieren.

### `com/hypixel/hytale/server/core/universe/world/worldgen/provider/`

- **Java-Dateien direkt:** 4
- **Java-Dateien gesamt:** 4
- **Beispiele:** DummyWorldGenProvider.java, FlatWorldGenProvider.java, IWorldGenProvider.java, VoidWorldGenProvider.java
- **Was passiert hier?** Worldgen: Biome, Zonen, Prefabs, Caves, Container und Loader.
- **Hilft beim Modding?** Später wichtig für MVP B: NPC-Häuser automatisch in der Welt platzieren.

### `com/hypixel/hytale/server/core/universe/world/worldlocationcondition/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** WorldLocationCondition.java
- **Was passiert hier?** Welt-/Universum-System: Worlds, Chunks, Storage, PlayerData und Welt-Events.
- **Hilft beim Modding?** Wichtig für Positionen, Marker, Persistenz und Weltzugriff.

### `com/hypixel/hytale/server/core/universe/world/worldmap/`

- **Java-Dateien direkt:** 4
- **Java-Dateien gesamt:** 25
- **Beispiele:** IWorldMap.java, WorldMapLoadException.java, WorldMapManager.java, WorldMapSettings.java
- **Was passiert hier?** Welt-/Universum-System: Worlds, Chunks, Storage, PlayerData und Welt-Events.
- **Hilft beim Modding?** Wichtig für Positionen, Marker, Persistenz und Weltzugriff.

### `com/hypixel/hytale/server/core/universe/world/worldmap/markers/`

- **Java-Dateien direkt:** 4
- **Java-Dateien gesamt:** 16
- **Beispiele:** MapMarkerBuilder.java, MapMarkerTracker.java, MarkersCollector.java, MarkersCollectorImpl.java
- **Was passiert hier?** Welt-/Universum-System: Worlds, Chunks, Storage, PlayerData und Welt-Events.
- **Hilft beim Modding?** Wichtig für Positionen, Marker, Persistenz und Weltzugriff.

### `com/hypixel/hytale/server/core/universe/world/worldmap/markers/providers/`

- **Java-Dateien direkt:** 7
- **Java-Dateien gesamt:** 7
- **Beispiele:** DeathMarkerProvider.java, OtherPlayersMarkerProvider.java, POIMarkerProvider.java, PersonalMarkersProvider.java, RespawnMarkerProvider.java, SharedMarkersProvider.java, SpawnMarkerProvider.java
- **Was passiert hier?** Welt-/Universum-System: Worlds, Chunks, Storage, PlayerData und Welt-Events.
- **Hilft beim Modding?** Wichtig für Positionen, Marker, Persistenz und Weltzugriff.

### `com/hypixel/hytale/server/core/universe/world/worldmap/markers/user/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** UserMapMarker.java, UserMapMarkersStore.java, UserMarkerValidator.java
- **Was passiert hier?** Welt-/Universum-System: Worlds, Chunks, Storage, PlayerData und Welt-Events.
- **Hilft beim Modding?** Wichtig für Positionen, Marker, Persistenz und Weltzugriff.

### `com/hypixel/hytale/server/core/universe/world/worldmap/markers/utils/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** MapMarkerUtils.java
- **Was passiert hier?** Welt-/Universum-System: Worlds, Chunks, Storage, PlayerData und Welt-Events.
- **Hilft beim Modding?** Wichtig für Positionen, Marker, Persistenz und Weltzugriff.

### `com/hypixel/hytale/server/core/universe/world/worldmap/markers/worldstore/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** WorldMarkersResource.java
- **Was passiert hier?** Welt-/Universum-System: Worlds, Chunks, Storage, PlayerData und Welt-Events.
- **Hilft beim Modding?** Wichtig für Positionen, Marker, Persistenz und Weltzugriff.

### `com/hypixel/hytale/server/core/universe/world/worldmap/provider/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 5
- **Beispiele:** DisabledWorldMapProvider.java, IWorldMapProvider.java
- **Was passiert hier?** Welt-/Universum-System: Worlds, Chunks, Storage, PlayerData und Welt-Events.
- **Hilft beim Modding?** Wichtig für Positionen, Marker, Persistenz und Weltzugriff.

### `com/hypixel/hytale/server/core/universe/world/worldmap/provider/chunk/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** ChunkWorldMap.java, ImageBuilder.java, WorldGenWorldMapProvider.java
- **Was passiert hier?** Welt-/Universum-System: Worlds, Chunks, Storage, PlayerData und Welt-Events.
- **Hilft beim Modding?** Wichtig für Positionen, Marker, Persistenz und Weltzugriff.

### `com/hypixel/hytale/server/core/update/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 10
- **Beispiele:** UpdateModule.java, UpdateService.java
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/server/core/update/command/`

- **Java-Dateien direkt:** 8
- **Java-Dateien gesamt:** 8
- **Beispiele:** UpdateApplyCommand.java, UpdateCancelCommand.java, UpdateCheckCommand.java, UpdateCommand.java, UpdateDownloadCommand.java, UpdatePatchlineCommand.java, UpdateSetupCommand.java, UpdateStatusCommand.java
- **Was passiert hier?** Command-System und fertige Server-/Spieler-/Debug-Commands.
- **Hilft beim Modding?** Sehr wichtig: Vorlage für eigene Admin-Commands wie /keystone marker oder /npc spawn.

### `com/hypixel/hytale/server/core/util/`

- **Java-Dateien direkt:** 19
- **Java-Dateien gesamt:** 27
- **Beispiele:** AssetUtil.java, BsonUtil.java, Config.java, ConsoleColorUtil.java, DumpUtil.java, EventTitleUtil.java, FillerBlockUtil.java, HashUtil.java …
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/server/core/util/backup/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** BackupTask.java, BackupUtil.java
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/server/core/util/concurrent/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** ThreadUtil.java
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/server/core/util/io/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** BlockingDiskFile.java, FileUtil.java, MemorySegmentUtil.java
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/server/core/util/message/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** MessageFormat.java
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/server/core/util/thread/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** TickingThread.java
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/server/flock/`

- **Java-Dateien direkt:** 8
- **Java-Dateien gesamt:** 33
- **Beispiele:** Flock.java, FlockDeathSystems.java, FlockMembership.java, FlockMembershipSystems.java, FlockPlugin.java, FlockSystems.java, PersistentFlockData.java, StoredFlock.java
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/server/flock/commands/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** NPCFlockCommand.java
- **Was passiert hier?** Command-System und fertige Server-/Spieler-/Debug-Commands.
- **Hilft beim Modding?** Sehr wichtig: Vorlage für eigene Admin-Commands wie /keystone marker oder /npc spawn.

### `com/hypixel/hytale/server/flock/config/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** FlockAsset.java, RangeSizeFlockAsset.java, WeightedSizeFlockAsset.java
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/server/flock/corecomponents/`

- **Java-Dateien direkt:** 10
- **Java-Dateien gesamt:** 20
- **Beispiele:** ActionFlockBeacon.java, ActionFlockJoin.java, ActionFlockLeave.java, ActionFlockSetTarget.java, ActionFlockState.java, BodyMotionFlock.java, EntityFilterFlock.java, SensorFlockCombatDamage.java …
- **Was passiert hier?** ECS/Component-System: Daten hängen als Komponenten an Entities/Chunks/Systeme.
- **Hilft beim Modding?** Sehr wichtig: Hytale nutzt stark Component-Logik statt einfache Minecraft-Objekte.

### `com/hypixel/hytale/server/flock/corecomponents/builders/`

- **Java-Dateien direkt:** 10
- **Java-Dateien gesamt:** 10
- **Beispiele:** BuilderActionFlockBeacon.java, BuilderActionFlockJoin.java, BuilderActionFlockLeave.java, BuilderActionFlockSetTarget.java, BuilderActionFlockState.java, BuilderBodyMotionFlock.java, BuilderEntityFilterFlock.java, BuilderSensorFlockCombatDamage.java …
- **Was passiert hier?** ECS/Component-System: Daten hängen als Komponenten an Entities/Chunks/Systeme.
- **Hilft beim Modding?** Sehr wichtig: Hytale nutzt stark Component-Logik statt einfache Minecraft-Objekte.

### `com/hypixel/hytale/server/flock/decisionmaker/`

- **Java-Dateien direkt:** 0
- **Java-Dateien gesamt:** 1
- **Beispiele:** keine direkten .java Dateien
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/server/flock/decisionmaker/conditions/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** FlockSizeCondition.java
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/server/npc/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 774
- **Beispiele:** AllNPCsLoadedEvent.java, NPCPlugin.java
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/animations/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** NPCAnimationSlot.java
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/asset/`

- **Java-Dateien direkt:** 0
- **Java-Dateien gesamt:** 153
- **Beispiele:** keine direkten .java Dateien
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/asset/builder/`

- **Java-Dateien direkt:** 39
- **Java-Dateien gesamt:** 153
- **Beispiele:** Builder.java, BuilderAttributeDescriptor.java, BuilderBase.java, BuilderBaseWithType.java, BuilderCodecObjectHelper.java, BuilderCombatConfig.java, BuilderComponent.java, BuilderContext.java …
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/asset/builder/expression/`

- **Java-Dateien direkt:** 15
- **Java-Dateien gesamt:** 15
- **Beispiele:** BuilderExpression.java, BuilderExpressionDynamic.java, BuilderExpressionDynamicBoolean.java, BuilderExpressionDynamicBooleanArray.java, BuilderExpressionDynamicNumber.java, BuilderExpressionDynamicNumberArray.java, BuilderExpressionDynamicString.java, BuilderExpressionDynamicStringArray.java …
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/asset/builder/holder/`

- **Java-Dateien direkt:** 19
- **Java-Dateien gesamt:** 19
- **Beispiele:** ArrayHolder.java, AssetArrayHolder.java, AssetHolder.java, BooleanArrayHolder.java, BooleanHolder.java, DeferEvaluateAssetHolder.java, DoubleHolder.java, DoubleHolderBase.java …
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/asset/builder/providerevaluators/`

- **Java-Dateien direkt:** 8
- **Java-Dateien gesamt:** 8
- **Beispiele:** FeatureProviderEvaluator.java, ParameterProviderEvaluator.java, ParameterType.java, ProviderEvaluator.java, ProviderEvaluatorTypeRegistry.java, ReferenceProviderEvaluator.java, UnconditionalFeatureProviderEvaluator.java, UnconditionalParameterProviderEvaluator.java
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/asset/builder/util/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** StringListHelpers.java
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/asset/builder/validators/`

- **Java-Dateien direkt:** 53
- **Java-Dateien gesamt:** 71
- **Beispiele:** AnyBooleanValidator.java, AnyPresentValidator.java, ArrayNotEmptyValidator.java, ArrayValidator.java, ArraysOneSetValidator.java, AssetValidator.java, AtMostOneBooleanValidator.java, AttributeRelationValidator.java …
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/asset/builder/validators/asset/`

- **Java-Dateien direkt:** 18
- **Java-Dateien gesamt:** 18
- **Beispiele:** AttitudeGroupExistsValidator.java, BeaconSpawnExistsValidator.java, BlockSetExistsValidator.java, CombatInteractionValidator.java, EntityEffectExistsValidator.java, EntityStatExistsValidator.java, EnvironmentExistsValidator.java, FlockAssetExistsValidator.java …
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/blackboard/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 30
- **Beispiele:** Blackboard.java
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/blackboard/view/`

- **Java-Dateien direkt:** 6
- **Java-Dateien gesamt:** 29
- **Beispiele:** BlockRegionView.java, BlockRegionViewManager.java, IBlackboardView.java, IBlackboardViewManager.java, PrioritisedProviderView.java, SingletonBlackboardViewManager.java
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/blackboard/view/attitude/`

- **Java-Dateien direkt:** 4
- **Java-Dateien gesamt:** 4
- **Beispiele:** AttitudeMap.java, AttitudeView.java, IAttitudeProvider.java, ItemAttitudeMap.java
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/blackboard/view/blocktype/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** BlockPositionEntryGenerator.java, BlockTypeView.java, BlockTypeViewManager.java
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/blackboard/view/combat/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** CombatViewSystems.java, InterpretedCombatData.java
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/blackboard/view/event/`

- **Java-Dateien direkt:** 5
- **Java-Dateien gesamt:** 9
- **Beispiele:** EntityEventNotification.java, EventNotification.java, EventTypeRegistration.java, EventView.java, IEventCallback.java
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/blackboard/view/event/block/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** BlockEventType.java, BlockEventView.java
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/blackboard/view/event/entity/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** EntityEventType.java, EntityEventView.java
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/blackboard/view/interaction/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** InteractionView.java, ReservationProvider.java, ReservationStatus.java
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/blackboard/view/resource/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** ResourceView.java, ResourceViewManager.java
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/commands/`

- **Java-Dateien direkt:** 24
- **Java-Dateien gesamt:** 24
- **Beispiele:** NPCAllCommand.java, NPCAppearanceCommand.java, NPCAttackCommand.java, NPCBenchmarkCommand.java, NPCBlackboardCommand.java, NPCCleanCommand.java, NPCCommand.java, NPCCommandUtils.java …
- **Was passiert hier?** Command-System und fertige Server-/Spieler-/Debug-Commands.
- **Hilft beim Modding?** Sehr wichtig: Vorlage für eigene Admin-Commands wie /keystone marker oder /npc spawn.

### `com/hypixel/hytale/server/npc/components/`

- **Java-Dateien direkt:** 7
- **Java-Dateien gesamt:** 17
- **Beispiele:** FailedSpawnComponent.java, SortBufferProviderResource.java, SpawnBeaconReference.java, SpawnMarkerReference.java, SpawnReference.java, StepComponent.java, Timers.java
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/components/messaging/`

- **Java-Dateien direkt:** 10
- **Java-Dateien gesamt:** 10
- **Beispiele:** BeaconSupport.java, EntityEventSupport.java, EventMessage.java, EventSupport.java, MessageSupport.java, NPCBlockEventSupport.java, NPCEntityEventSupport.java, NPCMessage.java …
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/config/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 3
- **Beispiele:** AttitudeGroup.java, ItemAttitudeGroup.java
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/config/balancing/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** BalanceAsset.java
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/corecomponents/`

- **Java-Dateien direkt:** 14
- **Java-Dateien gesamt:** 329
- **Beispiele:** ActionBase.java, ActionWithDelay.java, AnnotatedComponentBase.java, BlockTarget.java, BodyMotionBase.java, EntityFilterBase.java, HeadMotionBase.java, IEntityFilter.java …
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/corecomponents/audiovisual/`

- **Java-Dateien direkt:** 7
- **Java-Dateien gesamt:** 14
- **Beispiele:** ActionAppearance.java, ActionDisplayName.java, ActionModelAttachment.java, ActionPlayAnimation.java, ActionPlaySound.java, ActionSpawnParticles.java, SensorAnimation.java
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/corecomponents/audiovisual/builders/`

- **Java-Dateien direkt:** 7
- **Java-Dateien gesamt:** 7
- **Beispiele:** BuilderActionAppearance.java, BuilderActionDisplayName.java, BuilderActionModelAttachment.java, BuilderActionPlayAnimation.java, BuilderActionPlaySound.java, BuilderActionSpawnParticles.java, BuilderSensorAnimation.java
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/corecomponents/builders/`

- **Java-Dateien direkt:** 11
- **Java-Dateien gesamt:** 11
- **Beispiele:** BuilderActionBase.java, BuilderActionWithDelay.java, BuilderBodyMotionBase.java, BuilderEntityFilterBase.java, BuilderEntityFilterWithToggle.java, BuilderHeadMotionBase.java, BuilderMotionBase.java, BuilderSensorBase.java …
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/corecomponents/combat/`

- **Java-Dateien direkt:** 6
- **Java-Dateien gesamt:** 12
- **Beispiele:** ActionApplyEntityEffect.java, ActionAttack.java, BodyMotionAimCharge.java, HeadMotionAim.java, SensorDamage.java, SensorIsBackingAway.java
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/corecomponents/combat/builders/`

- **Java-Dateien direkt:** 6
- **Java-Dateien gesamt:** 6
- **Beispiele:** BuilderActionApplyEntityEffect.java, BuilderActionAttack.java, BuilderBodyMotionAimCharge.java, BuilderHeadMotionAim.java, BuilderSensorDamage.java, BuilderSensorIsBackingAway.java
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/corecomponents/debug/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 6
- **Beispiele:** ActionLog.java, ActionTest.java, BodyMotionTestProbe.java
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/corecomponents/debug/builders/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** BuilderActionLog.java, BuilderActionTest.java, BuilderBodyMotionTestProbe.java
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/corecomponents/entity/`

- **Java-Dateien direkt:** 16
- **Java-Dateien gesamt:** 73
- **Beispiele:** ActionBeacon.java, ActionIgnoreForAvoidance.java, ActionNotify.java, ActionOverrideAttitude.java, ActionReleaseTarget.java, ActionSetMarkedTarget.java, ActionSetStat.java, HeadMotionWatch.java …
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/corecomponents/entity/builders/`

- **Java-Dateien direkt:** 16
- **Java-Dateien gesamt:** 16
- **Beispiele:** BuilderActionBeacon.java, BuilderActionIgnoreForAvoidance.java, BuilderActionNotify.java, BuilderActionOverrideAttitude.java, BuilderActionReleaseTarget.java, BuilderActionSetMarkedTarget.java, BuilderActionSetStat.java, BuilderHeadMotionWatch.java …
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/corecomponents/entity/filters/`

- **Java-Dateien direkt:** 19
- **Java-Dateien gesamt:** 38
- **Beispiele:** EntityFilterAltitude.java, EntityFilterAnd.java, EntityFilterAttitude.java, EntityFilterCombat.java, EntityFilterEntityEffect.java, EntityFilterHeightDifference.java, EntityFilterInsideBlock.java, EntityFilterInventory.java …
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/corecomponents/entity/filters/builders/`

- **Java-Dateien direkt:** 19
- **Java-Dateien gesamt:** 19
- **Beispiele:** BuilderEntityFilterAltitude.java, BuilderEntityFilterAnd.java, BuilderEntityFilterAttitude.java, BuilderEntityFilterCombat.java, BuilderEntityFilterEntityEffect.java, BuilderEntityFilterHeightDifference.java, BuilderEntityFilterInsideBlock.java, BuilderEntityFilterInventory.java …
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/corecomponents/entity/prioritisers/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 3
- **Beispiele:** SensorEntityPrioritiserAttitude.java, SensorEntityPrioritiserDefault.java
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/corecomponents/entity/prioritisers/builders/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** BuilderSensorEntityPrioritiserAttitude.java
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/corecomponents/interaction/`

- **Java-Dateien direkt:** 5
- **Java-Dateien gesamt:** 10
- **Beispiele:** ActionLockOnInteractionTarget.java, ActionSetInteractable.java, SensorCanInteract.java, SensorHasInteracted.java, SensorInteractionContext.java
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/corecomponents/interaction/builders/`

- **Java-Dateien direkt:** 5
- **Java-Dateien gesamt:** 5
- **Beispiele:** BuilderActionLockOnInteractionTarget.java, BuilderActionSetInteractable.java, BuilderSensorCanInteract.java, BuilderSensorHasInteracted.java, BuilderSensorInteractionContext.java
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/corecomponents/items/`

- **Java-Dateien direkt:** 4
- **Java-Dateien gesamt:** 8
- **Beispiele:** ActionDropItem.java, ActionInventory.java, ActionPickUpItem.java, SensorDroppedItem.java
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/corecomponents/items/builders/`

- **Java-Dateien direkt:** 4
- **Java-Dateien gesamt:** 4
- **Beispiele:** BuilderActionDropItem.java, BuilderActionInventory.java, BuilderActionPickUpItem.java, BuilderSensorDroppedItem.java
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/corecomponents/lifecycle/`

- **Java-Dateien direkt:** 7
- **Java-Dateien gesamt:** 14
- **Beispiele:** ActionDelayDespawn.java, ActionDespawn.java, ActionDie.java, ActionRemove.java, ActionRole.java, ActionSpawn.java, SensorAge.java
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/corecomponents/lifecycle/builders/`

- **Java-Dateien direkt:** 7
- **Java-Dateien gesamt:** 7
- **Beispiele:** BuilderActionDelayDespawn.java, BuilderActionDespawn.java, BuilderActionDie.java, BuilderActionRemove.java, BuilderActionRole.java, BuilderActionSpawn.java, BuilderSensorAge.java
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/corecomponents/movement/`

- **Java-Dateien direkt:** 21
- **Java-Dateien gesamt:** 42
- **Beispiele:** ActionCrouch.java, ActionOverrideAltitude.java, ActionRecomputePath.java, BodyMotionFind.java, BodyMotionFindBase.java, BodyMotionFindWithTarget.java, BodyMotionLand.java, BodyMotionLeave.java …
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/corecomponents/movement/builders/`

- **Java-Dateien direkt:** 21
- **Java-Dateien gesamt:** 21
- **Beispiele:** BuilderActionCrouch.java, BuilderActionOverrideAltitude.java, BuilderActionRecomputePath.java, BuilderBodyMotionFind.java, BuilderBodyMotionFindBase.java, BuilderBodyMotionFindWithTarget.java, BuilderBodyMotionLand.java, BuilderBodyMotionLeave.java …
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/corecomponents/statemachine/`

- **Java-Dateien direkt:** 5
- **Java-Dateien gesamt:** 10
- **Beispiele:** ActionParentState.java, ActionState.java, ActionToggleStateEvaluator.java, SensorIsBusy.java, SensorState.java
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/corecomponents/statemachine/builders/`

- **Java-Dateien direkt:** 5
- **Java-Dateien gesamt:** 5
- **Beispiele:** BuilderActionParentState.java, BuilderActionState.java, BuilderActionToggleStateEvaluator.java, BuilderSensorIsBusy.java, BuilderSensorState.java
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/corecomponents/timer/`

- **Java-Dateien direkt:** 7
- **Java-Dateien gesamt:** 20
- **Beispiele:** ActionSetAlarm.java, ActionTimer.java, BodyMotionTimer.java, HeadMotionTimer.java, MotionTimer.java, SensorAlarm.java, SensorTimer.java
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/corecomponents/timer/builders/`

- **Java-Dateien direkt:** 13
- **Java-Dateien gesamt:** 13
- **Beispiele:** BuilderActionSetAlarm.java, BuilderActionTimer.java, BuilderActionTimerContinue.java, BuilderActionTimerModify.java, BuilderActionTimerPause.java, BuilderActionTimerRestart.java, BuilderActionTimerStart.java, BuilderActionTimerStop.java …
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/corecomponents/utility/`

- **Java-Dateien direkt:** 22
- **Java-Dateien gesamt:** 45
- **Beispiele:** ActionNothing.java, ActionRandom.java, ActionResetInstructions.java, ActionSequence.java, ActionSetFlag.java, ActionTimeout.java, BodyMotionNothing.java, BodyMotionSequence.java …
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/corecomponents/utility/builders/`

- **Java-Dateien direkt:** 23
- **Java-Dateien gesamt:** 23
- **Beispiele:** BuilderActionNothing.java, BuilderActionRandom.java, BuilderActionResetInstructions.java, BuilderActionSequence.java, BuilderActionSetFlag.java, BuilderActionTimeout.java, BuilderBodyMotionNothing.java, BuilderBodyMotionSequence.java …
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/corecomponents/world/`

- **Java-Dateien direkt:** 25
- **Java-Dateien gesamt:** 50
- **Beispiele:** ActionMakePath.java, ActionPlaceBlock.java, ActionResetBlockSensors.java, ActionResetPath.java, ActionResetSearchRays.java, ActionSetBlockToPlace.java, ActionSetLeashPosition.java, ActionStorePosition.java …
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/corecomponents/world/builders/`

- **Java-Dateien direkt:** 25
- **Java-Dateien gesamt:** 25
- **Beispiele:** BuilderActionMakePath.java, BuilderActionPlaceBlock.java, BuilderActionResetBlockSensors.java, BuilderActionResetPath.java, BuilderActionResetSearchRays.java, BuilderActionSetBlockToPlace.java, BuilderActionSetLeashPosition.java, BuilderActionStorePosition.java …
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/decisionmaker/`

- **Java-Dateien direkt:** 0
- **Java-Dateien gesamt:** 24
- **Beispiele:** keine direkten .java Dateien
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/decisionmaker/core/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 22
- **Beispiele:** EvaluationContext.java, Evaluator.java, Option.java
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/decisionmaker/core/conditions/`

- **Java-Dateien direkt:** 15
- **Java-Dateien gesamt:** 19
- **Beispiele:** HasTargetCondition.java, IsInStateCondition.java, LineOfSightCondition.java, NearbyCountCondition.java, RandomiserCondition.java, SelfHasEffectCondition.java, SelfStatAbsoluteCondition.java, SelfStatPercentageCondition.java …
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/decisionmaker/core/conditions/base/`

- **Java-Dateien direkt:** 4
- **Java-Dateien gesamt:** 4
- **Beispiele:** Condition.java, CurveCondition.java, ScaledCurveCondition.java, SimpleCondition.java
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/decisionmaker/stateevaluator/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** StateEvaluator.java, StateOption.java
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/entities/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** NPCEntity.java, PathManager.java
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/instructions/`

- **Java-Dateien direkt:** 10
- **Java-Dateien gesamt:** 14
- **Beispiele:** Action.java, ActionList.java, BodyMotion.java, HeadMotion.java, Instruction.java, InstructionRandomized.java, Motion.java, NullSensor.java …
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/instructions/builders/`

- **Java-Dateien direkt:** 4
- **Java-Dateien gesamt:** 4
- **Beispiele:** BuilderActionList.java, BuilderInstruction.java, BuilderInstructionRandomized.java, BuilderInstructionReference.java
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/interactions/`

- **Java-Dateien direkt:** 5
- **Java-Dateien gesamt:** 5
- **Beispiele:** ContextualUseNPCInteraction.java, NPCInteractionSimulationHandler.java, SpawnNPCInteraction.java, SpawnNPCInteractionFailureTracker.java, UseNPCInteraction.java
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/metadata/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** CapturedNPCMetadata.java
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/movement/`

- **Java-Dateien direkt:** 8
- **Java-Dateien gesamt:** 29
- **Beispiele:** FlockMembershipType.java, FlockPlayerMembership.java, GroupSteeringAccumulator.java, MotionKind.java, MovementMode.java, MovementState.java, NavState.java, Steering.java
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/movement/constraints/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** RelaxedConstraint.java
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/movement/controllers/`

- **Java-Dateien direkt:** 7
- **Java-Dateien gesamt:** 12
- **Beispiele:** BuilderMotionControllerMapUtil.java, MotionController.java, MotionControllerBase.java, MotionControllerDive.java, MotionControllerFly.java, MotionControllerWalk.java, ProbeMoveData.java
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/movement/controllers/builders/`

- **Java-Dateien direkt:** 5
- **Java-Dateien gesamt:** 5
- **Beispiele:** BuilderMotionControllerBase.java, BuilderMotionControllerDive.java, BuilderMotionControllerFly.java, BuilderMotionControllerMap.java, BuilderMotionControllerWalk.java
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/movement/steeringforces/`

- **Java-Dateien direkt:** 8
- **Java-Dateien gesamt:** 8
- **Beispiele:** SteeringForce.java, SteeringForceAvoidCollision.java, SteeringForceEvade.java, SteeringForcePursue.java, SteeringForceRotate.java, SteeringForceWander.java, SteeringForceWithGroup.java, SteeringForceWithTarget.java
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/navigation/`

- **Java-Dateien direkt:** 12
- **Java-Dateien gesamt:** 12
- **Beispiele:** AStarBase.java, AStarDebugBase.java, AStarDebugWithTarget.java, AStarEvaluator.java, AStarNode.java, AStarNodePool.java, AStarNodePoolProvider.java, AStarNodePoolProviderSimple.java …
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/pages/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** EntitySpawnPage.java
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/path/`

- **Java-Dateien direkt:** 0
- **Java-Dateien gesamt:** 2
- **Beispiele:** keine direkten .java Dateien
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/path/builders/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** BuilderRelativeWaypointDefinition.java, BuilderTransientPathDefinition.java
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/role/`

- **Java-Dateien direkt:** 5
- **Java-Dateien gesamt:** 17
- **Beispiele:** Role.java, RoleDebugDisplay.java, RoleDebugFlags.java, RoleUtils.java, SpawnEffect.java
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/role/builders/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** BuilderRole.java, BuilderRoleAbstract.java, BuilderRoleVariant.java
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/role/support/`

- **Java-Dateien direkt:** 9
- **Java-Dateien gesamt:** 9
- **Beispiele:** CombatSupport.java, DebugSupport.java, EntityList.java, EntitySupport.java, MarkedEntitySupport.java, PositionCache.java, RoleStats.java, StateSupport.java …
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/sensorinfo/`

- **Java-Dateien direkt:** 11
- **Java-Dateien gesamt:** 20
- **Beispiele:** CachedPositionProvider.java, EntityPositionProvider.java, ExtraInfoProvider.java, IPathProvider.java, IPositionProvider.java, InfoProvider.java, InfoProviderBase.java, PathProvider.java …
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/sensorinfo/parameterproviders/`

- **Java-Dateien direkt:** 9
- **Java-Dateien gesamt:** 9
- **Beispiele:** DoubleParameterProvider.java, IntParameterProvider.java, MultipleParameterProvider.java, ParameterProvider.java, SingleDoubleParameterProvider.java, SingleIntParameterProvider.java, SingleParameterProvider.java, SingleStringParameterProvider.java …
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/statetransition/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 4
- **Beispiele:** StateTransitionController.java
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/statetransition/builders/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** BuilderStateTransition.java, BuilderStateTransitionController.java, BuilderStateTransitionEdges.java
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/storage/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** AlarmStore.java, ParameterStore.java, PersistentParameter.java
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/systems/`

- **Java-Dateien direkt:** 26
- **Java-Dateien gesamt:** 26
- **Beispiele:** AvoidanceSystem.java, BalancingInitialisationSystem.java, BlackboardSystems.java, ComputeVelocitySystem.java, FailedSpawnSystem.java, MessageSupportSystem.java, MovementStatesSystem.java, NPCDamageSystems.java …
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/util/`

- **Java-Dateien direkt:** 22
- **Java-Dateien gesamt:** 51
- **Beispiele:** AimingData.java, AimingHelper.java, Alarm.java, AttitudeMemoryEntry.java, BlockPlacementHelper.java, ComponentInfo.java, DamageData.java, IAnnotatedComponent.java …
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/util/expression/`

- **Java-Dateien direkt:** 6
- **Java-Dateien gesamt:** 29
- **Beispiele:** ExecutionContext.java, Expression.java, Scope.java, StdLib.java, StdScope.java, ValueType.java
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/util/expression/compile/`

- **Java-Dateien direkt:** 8
- **Java-Dateien gesamt:** 23
- **Beispiele:** CompileContext.java, Lexer.java, LexerContext.java, OperatorBinary.java, OperatorUnary.java, Parser.java, Token.java, TokenFlags.java
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/util/expression/compile/ast/`

- **Java-Dateien direkt:** 15
- **Java-Dateien gesamt:** 15
- **Beispiele:** AST.java, ASTOperand.java, ASTOperandBoolean.java, ASTOperandBooleanArray.java, ASTOperandEmptyArray.java, ASTOperandIdentifier.java, ASTOperandNumber.java, ASTOperandNumberArray.java …
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/validators/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** NPCLoadTimeValidationHelper.java, NPCRoleValidator.java
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/npc/valuestore/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** ValueStore.java, ValueStoreValidator.java
- **Was passiert hier?** NPC-System: Rollen, Blackboard, Bewegung, Sensoren, State Machine und NPC-Commands.
- **Hilft beim Modding?** Extrem wichtig für dein Lumberjack-NPC-MVP.

### `com/hypixel/hytale/server/spawning/`

- **Java-Dateien direkt:** 7
- **Java-Dateien gesamt:** 82
- **Beispiele:** ISpawnable.java, ISpawnableWithModel.java, LoadedNPCEvent.java, SpawnRejection.java, SpawnTestResult.java, SpawningContext.java, SpawningPlugin.java
- **Was passiert hier?** Spawn-System: Spawnmarker, Spawnjobs, lokale/world Spawns und Suppression.
- **Hilft beim Modding?** Wichtig, wenn NPCs später automatisch erscheinen sollen.

### `com/hypixel/hytale/server/spawning/assets/`

- **Java-Dateien direkt:** 0
- **Java-Dateien gesamt:** 7
- **Beispiele:** keine direkten .java Dateien
- **Was passiert hier?** Spawn-System: Spawnmarker, Spawnjobs, lokale/world Spawns und Suppression.
- **Hilft beim Modding?** Wichtig, wenn NPCs später automatisch erscheinen sollen.

### `com/hypixel/hytale/server/spawning/assets/spawnmarker/`

- **Java-Dateien direkt:** 0
- **Java-Dateien gesamt:** 1
- **Beispiele:** keine direkten .java Dateien
- **Was passiert hier?** Spawn-System: Spawnmarker, Spawnjobs, lokale/world Spawns und Suppression.
- **Hilft beim Modding?** Wichtig, wenn NPCs später automatisch erscheinen sollen.

### `com/hypixel/hytale/server/spawning/assets/spawnmarker/config/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** SpawnMarker.java
- **Was passiert hier?** Spawn-System: Spawnmarker, Spawnjobs, lokale/world Spawns und Suppression.
- **Hilft beim Modding?** Wichtig, wenn NPCs später automatisch erscheinen sollen.

### `com/hypixel/hytale/server/spawning/assets/spawns/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 5
- **Beispiele:** LightType.java
- **Was passiert hier?** Spawn-System: Spawnmarker, Spawnjobs, lokale/world Spawns und Suppression.
- **Hilft beim Modding?** Wichtig, wenn NPCs später automatisch erscheinen sollen.

### `com/hypixel/hytale/server/spawning/assets/spawns/config/`

- **Java-Dateien direkt:** 4
- **Java-Dateien gesamt:** 4
- **Beispiele:** BeaconNPCSpawn.java, NPCSpawn.java, RoleSpawnParameters.java, WorldNPCSpawn.java
- **Was passiert hier?** Spawn-System: Spawnmarker, Spawnjobs, lokale/world Spawns und Suppression.
- **Hilft beim Modding?** Wichtig, wenn NPCs später automatisch erscheinen sollen.

### `com/hypixel/hytale/server/spawning/assets/spawnsuppression/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** SpawnSuppression.java
- **Was passiert hier?** Spawn-System: Spawnmarker, Spawnjobs, lokale/world Spawns und Suppression.
- **Hilft beim Modding?** Wichtig, wenn NPCs später automatisch erscheinen sollen.

### `com/hypixel/hytale/server/spawning/beacons/`

- **Java-Dateien direkt:** 4
- **Java-Dateien gesamt:** 4
- **Beispiele:** InitialBeaconDelay.java, LegacySpawnBeaconEntity.java, SpawnBeacon.java, SpawnBeaconSystems.java
- **Was passiert hier?** Spawn-System: Spawnmarker, Spawnjobs, lokale/world Spawns und Suppression.
- **Hilft beim Modding?** Wichtig, wenn NPCs später automatisch erscheinen sollen.

### `com/hypixel/hytale/server/spawning/blockstates/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** SpawnMarkerBlock.java, SpawnMarkerBlockReference.java, SpawnMarkerBlockStateSystems.java
- **Was passiert hier?** Spawn-System: Spawnmarker, Spawnjobs, lokale/world Spawns und Suppression.
- **Hilft beim Modding?** Wichtig, wenn NPCs später automatisch erscheinen sollen.

### `com/hypixel/hytale/server/spawning/commands/`

- **Java-Dateien direkt:** 6
- **Java-Dateien gesamt:** 6
- **Beispiele:** SpawnBeaconsCommand.java, SpawnCommand.java, SpawnMarkersCommand.java, SpawnPopulateCommand.java, SpawnStatsCommand.java, SpawnSuppressionCommand.java
- **Was passiert hier?** Command-System und fertige Server-/Spieler-/Debug-Commands.
- **Hilft beim Modding?** Sehr wichtig: Vorlage für eigene Admin-Commands wie /keystone marker oder /npc spawn.

### `com/hypixel/hytale/server/spawning/controllers/`

- **Java-Dateien direkt:** 4
- **Java-Dateien gesamt:** 4
- **Beispiele:** BeaconSpawnController.java, SpawnController.java, SpawnControllerSystem.java, SpawnJobSystem.java
- **Was passiert hier?** Spawn-System: Spawnmarker, Spawnjobs, lokale/world Spawns und Suppression.
- **Hilft beim Modding?** Wichtig, wenn NPCs später automatisch erscheinen sollen.

### `com/hypixel/hytale/server/spawning/corecomponents/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 2
- **Beispiele:** ActionTriggerSpawnBeacon.java
- **Was passiert hier?** Spawn-System: Spawnmarker, Spawnjobs, lokale/world Spawns und Suppression.
- **Hilft beim Modding?** Wichtig, wenn NPCs später automatisch erscheinen sollen.

### `com/hypixel/hytale/server/spawning/corecomponents/builders/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** BuilderActionTriggerSpawnBeacon.java
- **Was passiert hier?** Spawn-System: Spawnmarker, Spawnjobs, lokale/world Spawns und Suppression.
- **Hilft beim Modding?** Wichtig, wenn NPCs später automatisch erscheinen sollen.

### `com/hypixel/hytale/server/spawning/interactions/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** TriggerSpawnMarkersInteraction.java
- **Was passiert hier?** Spawn-System: Spawnmarker, Spawnjobs, lokale/world Spawns und Suppression.
- **Hilft beim Modding?** Wichtig, wenn NPCs später automatisch erscheinen sollen.

### `com/hypixel/hytale/server/spawning/jobs/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** NPCBeaconSpawnJob.java, SpawnJob.java
- **Was passiert hier?** Spawn-System: Spawnmarker, Spawnjobs, lokale/world Spawns und Suppression.
- **Hilft beim Modding?** Wichtig, wenn NPCs später automatisch erscheinen sollen.

### `com/hypixel/hytale/server/spawning/local/`

- **Java-Dateien direkt:** 7
- **Java-Dateien gesamt:** 7
- **Beispiele:** LocalSpawnBeacon.java, LocalSpawnBeaconSystem.java, LocalSpawnController.java, LocalSpawnControllerSystem.java, LocalSpawnForceTriggerSystem.java, LocalSpawnSetupSystem.java, LocalSpawnState.java
- **Was passiert hier?** Spawn-System: Spawnmarker, Spawnjobs, lokale/world Spawns und Suppression.
- **Hilft beim Modding?** Wichtig, wenn NPCs später automatisch erscheinen sollen.

### `com/hypixel/hytale/server/spawning/managers/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** BeaconSpawnManager.java, SpawnManager.java
- **Was passiert hier?** Spawn-System: Spawnmarker, Spawnjobs, lokale/world Spawns und Suppression.
- **Hilft beim Modding?** Wichtig, wenn NPCs später automatisch erscheinen sollen.

### `com/hypixel/hytale/server/spawning/spawnmarkers/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** SpawnMarkerEntity.java, SpawnMarkerSystems.java
- **Was passiert hier?** Spawn-System: Spawnmarker, Spawnjobs, lokale/world Spawns und Suppression.
- **Hilft beim Modding?** Wichtig, wenn NPCs später automatisch erscheinen sollen.

### `com/hypixel/hytale/server/spawning/suppression/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 9
- **Beispiele:** SpawnSuppressorEntry.java, SuppressionSpanHelper.java
- **Was passiert hier?** Spawn-System: Spawnmarker, Spawnjobs, lokale/world Spawns und Suppression.
- **Hilft beim Modding?** Wichtig, wenn NPCs später automatisch erscheinen sollen.

### `com/hypixel/hytale/server/spawning/suppression/component/`

- **Java-Dateien direkt:** 4
- **Java-Dateien gesamt:** 4
- **Beispiele:** ChunkSuppressionEntry.java, ChunkSuppressionQueue.java, SpawnSuppressionComponent.java, SpawnSuppressionController.java
- **Was passiert hier?** Spawn-System: Spawnmarker, Spawnjobs, lokale/world Spawns und Suppression.
- **Hilft beim Modding?** Wichtig, wenn NPCs später automatisch erscheinen sollen.

### `com/hypixel/hytale/server/spawning/suppression/system/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** ChunkSuppressionSystems.java, SpawnMarkerSuppressionSystem.java, SpawnSuppressionSystems.java
- **Was passiert hier?** Spawn-System: Spawnmarker, Spawnjobs, lokale/world Spawns und Suppression.
- **Hilft beim Modding?** Wichtig, wenn NPCs später automatisch erscheinen sollen.

### `com/hypixel/hytale/server/spawning/systems/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** BeaconSpatialSystem.java, LegacyBeaconSpatialSystem.java, SpawnMarkerSpatialSystem.java
- **Was passiert hier?** Spawn-System: Spawnmarker, Spawnjobs, lokale/world Spawns und Suppression.
- **Hilft beim Modding?** Wichtig, wenn NPCs später automatisch erscheinen sollen.

### `com/hypixel/hytale/server/spawning/util/`

- **Java-Dateien direkt:** 6
- **Java-Dateien gesamt:** 6
- **Beispiele:** ChunkColumnMask.java, FloodFillEntryPoolProviderSimple.java, FloodFillEntryPoolSimple.java, FloodFillPositionSelector.java, LightRangePredicate.java, RandomChunkColumnIterator.java
- **Was passiert hier?** Spawn-System: Spawnmarker, Spawnjobs, lokale/world Spawns und Suppression.
- **Hilft beim Modding?** Wichtig, wenn NPCs später automatisch erscheinen sollen.

### `com/hypixel/hytale/server/spawning/world/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 15
- **Beispiele:** ChunkEnvironmentSpawnData.java, WorldEnvironmentSpawnData.java, WorldNPCSpawnStat.java
- **Was passiert hier?** Spawn-System: Spawnmarker, Spawnjobs, lokale/world Spawns und Suppression.
- **Hilft beim Modding?** Wichtig, wenn NPCs später automatisch erscheinen sollen.

### `com/hypixel/hytale/server/spawning/world/component/`

- **Java-Dateien direkt:** 4
- **Java-Dateien gesamt:** 4
- **Beispiele:** ChunkSpawnData.java, ChunkSpawnedNPCData.java, SpawnJobData.java, WorldSpawnData.java
- **Was passiert hier?** Spawn-System: Spawnmarker, Spawnjobs, lokale/world Spawns und Suppression.
- **Hilft beim Modding?** Wichtig, wenn NPCs später automatisch erscheinen sollen.

### `com/hypixel/hytale/server/spawning/world/manager/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** EnvironmentSpawnParameters.java, WorldSpawnManager.java, WorldSpawnWrapper.java
- **Was passiert hier?** Spawn-System: Spawnmarker, Spawnjobs, lokale/world Spawns und Suppression.
- **Hilft beim Modding?** Wichtig, wenn NPCs später automatisch erscheinen sollen.

### `com/hypixel/hytale/server/spawning/world/system/`

- **Java-Dateien direkt:** 5
- **Java-Dateien gesamt:** 5
- **Beispiele:** ChunkSpawningSystems.java, MoonPhaseChangeEventSystem.java, WorldSpawnJobSystems.java, WorldSpawnTrackingSystem.java, WorldSpawningSystem.java
- **Was passiert hier?** Spawn-System: Spawnmarker, Spawnjobs, lokale/world Spawns und Suppression.
- **Hilft beim Modding?** Wichtig, wenn NPCs später automatisch erscheinen sollen.

### `com/hypixel/hytale/server/spawning/wrappers/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** BeaconSpawnWrapper.java, SpawnWrapper.java
- **Was passiert hier?** Spawn-System: Spawnmarker, Spawnjobs, lokale/world Spawns und Suppression.
- **Hilft beim Modding?** Wichtig, wenn NPCs später automatisch erscheinen sollen.

### `com/hypixel/hytale/server/worldgen/`

- **Java-Dateien direkt:** 6
- **Java-Dateien gesamt:** 214
- **Beispiele:** BiomeDataSystem.java, ChunkGeneratorResource.java, HytaleWorldGenProvider.java, SeedStringResource.java, WorldGenConfig.java, WorldGenConstants.java
- **Was passiert hier?** Worldgen: Biome, Zonen, Prefabs, Caves, Container und Loader.
- **Hilft beim Modding?** Später wichtig für MVP B: NPC-Häuser automatisch in der Welt platzieren.

### `com/hypixel/hytale/server/worldgen/benchmark/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** ChunkWorldgenBenchmark.java
- **Was passiert hier?** Worldgen: Biome, Zonen, Prefabs, Caves, Container und Loader.
- **Hilft beim Modding?** Später wichtig für MVP B: NPC-Häuser automatisch in der Welt platzieren.

### `com/hypixel/hytale/server/worldgen/biome/`

- **Java-Dateien direkt:** 6
- **Java-Dateien gesamt:** 6
- **Beispiele:** Biome.java, BiomeInterpolation.java, BiomePatternGenerator.java, CustomBiome.java, CustomBiomeGenerator.java, TileBiome.java
- **Was passiert hier?** Worldgen: Biome, Zonen, Prefabs, Caves, Container und Loader.
- **Hilft beim Modding?** Später wichtig für MVP B: NPC-Häuser automatisch in der Welt platzieren.

### `com/hypixel/hytale/server/worldgen/cache/`

- **Java-Dateien direkt:** 7
- **Java-Dateien gesamt:** 7
- **Beispiele:** CaveGeneratorCache.java, ChunkGeneratorCache.java, CoordinateCache.java, CoreDataCacheEntry.java, ExtendedCoordinateCache.java, InterpolatedBiomeCountList.java, UniquePrefabCache.java
- **Was passiert hier?** Worldgen: Biome, Zonen, Prefabs, Caves, Container und Loader.
- **Hilft beim Modding?** Später wichtig für MVP B: NPC-Häuser automatisch in der Welt platzieren.

### `com/hypixel/hytale/server/worldgen/cave/`

- **Java-Dateien direkt:** 8
- **Java-Dateien gesamt:** 32
- **Beispiele:** Cave.java, CaveBiomeMaskFlags.java, CaveBlockPriorityModifier.java, CaveGenerator.java, CaveNodeType.java, CavePrefabPlacement.java, CaveType.java, CaveYawMode.java
- **Was passiert hier?** Worldgen: Biome, Zonen, Prefabs, Caves, Container und Loader.
- **Hilft beim Modding?** Später wichtig für MVP B: NPC-Häuser automatisch in der Welt platzieren.

### `com/hypixel/hytale/server/worldgen/cave/element/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** CaveElement.java, CaveNode.java, CavePrefab.java
- **Was passiert hier?** Worldgen: Biome, Zonen, Prefabs, Caves, Container und Loader.
- **Hilft beim Modding?** Später wichtig für MVP B: NPC-Häuser automatisch in der Welt platzieren.

### `com/hypixel/hytale/server/worldgen/cave/prefab/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** CavePrefabContainer.java
- **Was passiert hier?** Worldgen: Biome, Zonen, Prefabs, Caves, Container und Loader.
- **Hilft beim Modding?** Später wichtig für MVP B: NPC-Häuser automatisch in der Welt platzieren.

### `com/hypixel/hytale/server/worldgen/cave/shape/`

- **Java-Dateien direkt:** 11
- **Java-Dateien gesamt:** 20
- **Beispiele:** AbstractCaveNodeShape.java, CaveNodeShape.java, CaveNodeShapeEnum.java, CaveNodeShapeUtils.java, CylinderCaveNodeShape.java, DistortedCaveNodeShape.java, EllipsoidCaveNodeShape.java, EmptyLineCaveNodeShape.java …
- **Was passiert hier?** Worldgen: Biome, Zonen, Prefabs, Caves, Container und Loader.
- **Hilft beim Modding?** Später wichtig für MVP B: NPC-Häuser automatisch in der Welt platzieren.

### `com/hypixel/hytale/server/worldgen/cave/shape/distorted/`

- **Java-Dateien direkt:** 9
- **Java-Dateien gesamt:** 9
- **Beispiele:** AbstractDistortedBody.java, AbstractDistortedExtrusion.java, AbstractDistortedShape.java, DistortedCylinderShape.java, DistortedEllipsoidShape.java, DistortedPipeShape.java, DistortedShape.java, DistortedShapes.java …
- **Was passiert hier?** Worldgen: Biome, Zonen, Prefabs, Caves, Container und Loader.
- **Hilft beim Modding?** Später wichtig für MVP B: NPC-Häuser automatisch in der Welt platzieren.

### `com/hypixel/hytale/server/worldgen/chunk/`

- **Java-Dateien direkt:** 8
- **Java-Dateien gesamt:** 12
- **Beispiele:** BlockPriorityChunk.java, BlockPriorityModifier.java, ChunkGenerator.java, ChunkGeneratorExecution.java, HeightThresholdInterpolator.java, MaskProvider.java, ValidationUtil.java, ZoneBiomeResult.java
- **Was passiert hier?** Worldgen: Biome, Zonen, Prefabs, Caves, Container und Loader.
- **Hilft beim Modding?** Später wichtig für MVP B: NPC-Häuser automatisch in der Welt platzieren.

### `com/hypixel/hytale/server/worldgen/chunk/populator/`

- **Java-Dateien direkt:** 4
- **Java-Dateien gesamt:** 4
- **Beispiele:** BlockPopulator.java, CavePopulator.java, PrefabPopulator.java, WaterPopulator.java
- **Was passiert hier?** Worldgen: Biome, Zonen, Prefabs, Caves, Container und Loader.
- **Hilft beim Modding?** Später wichtig für MVP B: NPC-Häuser automatisch in der Welt platzieren.

### `com/hypixel/hytale/server/worldgen/climate/`

- **Java-Dateien direkt:** 9
- **Java-Dateien gesamt:** 12
- **Beispiele:** ClimateColor.java, ClimateGraph.java, ClimateMaskProvider.java, ClimateNoise.java, ClimatePoint.java, ClimateSearch.java, ClimateType.java, DirectGrid.java …
- **Was passiert hier?** Worldgen: Biome, Zonen, Prefabs, Caves, Container und Loader.
- **Hilft beim Modding?** Später wichtig für MVP B: NPC-Häuser automatisch in der Welt platzieren.

### `com/hypixel/hytale/server/worldgen/climate/util/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** DistanceTransform.java, DoubleMap.java, IntMap.java
- **Was passiert hier?** Worldgen: Biome, Zonen, Prefabs, Caves, Container und Loader.
- **Hilft beim Modding?** Später wichtig für MVP B: NPC-Häuser automatisch in der Welt platzieren.

### `com/hypixel/hytale/server/worldgen/container/`

- **Java-Dateien direkt:** 8
- **Java-Dateien gesamt:** 8
- **Beispiele:** CoverContainer.java, EnvironmentContainer.java, FadeContainer.java, LayerContainer.java, PrefabContainer.java, TintContainer.java, UniquePrefabContainer.java, WaterContainer.java
- **Was passiert hier?** Worldgen: Biome, Zonen, Prefabs, Caves, Container und Loader.
- **Hilft beim Modding?** Später wichtig für MVP B: NPC-Häuser automatisch in der Welt platzieren.

### `com/hypixel/hytale/server/worldgen/loader/`

- **Java-Dateien direkt:** 6
- **Java-Dateien gesamt:** 77
- **Beispiele:** AssetFileSystem.java, ChunkGeneratorJsonLoader.java, MaskProviderJsonLoader.java, WorldGenPrefabLoader.java, WorldGenPrefabSupplier.java, ZonesJsonLoader.java
- **Was passiert hier?** Worldgen: Biome, Zonen, Prefabs, Caves, Container und Loader.
- **Hilft beim Modding?** Später wichtig für MVP B: NPC-Häuser automatisch in der Welt platzieren.

### `com/hypixel/hytale/server/worldgen/loader/biome/`

- **Java-Dateien direkt:** 7
- **Java-Dateien gesamt:** 7
- **Beispiele:** BiomeInterpolationJsonLoader.java, BiomeJsonLoader.java, BiomeMaskJsonLoader.java, BiomePatternGeneratorJsonLoader.java, CustomBiomeGeneratorJsonLoader.java, CustomBiomeJsonLoader.java, TileBiomeJsonLoader.java
- **Was passiert hier?** Worldgen: Biome, Zonen, Prefabs, Caves, Container und Loader.
- **Hilft beim Modding?** Später wichtig für MVP B: NPC-Häuser automatisch in der Welt platzieren.

### `com/hypixel/hytale/server/worldgen/loader/cave/`

- **Java-Dateien direkt:** 12
- **Java-Dateien gesamt:** 20
- **Beispiele:** CaveBiomeMaskJsonLoader.java, CaveGeneratorJsonLoader.java, CaveNodeChildEntryJsonLoader.java, CaveNodeCoverEntryJsonLoader.java, CaveNodeTypeJsonLoader.java, CaveNodeTypeStorage.java, CavePrefabConfigJsonLoader.java, CavePrefabContainerJsonLoader.java …
- **Was passiert hier?** Worldgen: Biome, Zonen, Prefabs, Caves, Container und Loader.
- **Hilft beim Modding?** Später wichtig für MVP B: NPC-Häuser automatisch in der Welt platzieren.

### `com/hypixel/hytale/server/worldgen/loader/cave/shape/`

- **Java-Dateien direkt:** 8
- **Java-Dateien gesamt:** 8
- **Beispiele:** CaveNodeShapeGeneratorJsonLoader.java, CylinderCaveNodeShapeGeneratorJsonLoader.java, DistortedCaveNodeShapeGeneratorJsonLoader.java, EllipsoidCaveNodeShapeGeneratorJsonLoader.java, EmptyLineCaveNodeShapeGeneratorJsonLoader.java, PipeCaveNodeShapeGeneratorJsonLoader.java, PrefabCaveNodeShapeGeneratorJsonLoader.java, ShapeDistortionJsonLoader.java
- **Was passiert hier?** Worldgen: Biome, Zonen, Prefabs, Caves, Container und Loader.
- **Hilft beim Modding?** Später wichtig für MVP B: NPC-Häuser automatisch in der Welt platzieren.

### `com/hypixel/hytale/server/worldgen/loader/climate/`

- **Java-Dateien direkt:** 11
- **Java-Dateien gesamt:** 11
- **Beispiele:** ClimateColorJsonLoader.java, ClimateGraphJsonLoader.java, ClimateGridJsonLoader.java, ClimateMaskJsonLoader.java, ClimateNoiseJsonLoader.java, ClimatePointJsonLoader.java, ClimateRuleJsonLoader.java, ClimateTypeJsonLoader.java …
- **Was passiert hier?** Worldgen: Biome, Zonen, Prefabs, Caves, Container und Loader.
- **Hilft beim Modding?** Später wichtig für MVP B: NPC-Häuser automatisch in der Welt platzieren.

### `com/hypixel/hytale/server/worldgen/loader/container/`

- **Java-Dateien direkt:** 8
- **Java-Dateien gesamt:** 8
- **Beispiele:** CoverContainerJsonLoader.java, EnvironmentContainerJsonLoader.java, FadeContainerJsonLoader.java, LayerContainerJsonLoader.java, PrefabContainerJsonLoader.java, TintContainerJsonLoader.java, UniquePrefabContainerJsonLoader.java, WaterContainerJsonLoader.java
- **Was passiert hier?** Worldgen: Biome, Zonen, Prefabs, Caves, Container und Loader.
- **Hilft beim Modding?** Später wichtig für MVP B: NPC-Häuser automatisch in der Welt platzieren.

### `com/hypixel/hytale/server/worldgen/loader/context/`

- **Java-Dateien direkt:** 6
- **Java-Dateien gesamt:** 6
- **Beispiele:** BiomeFileContext.java, CaveFileContext.java, FileContext.java, FileContextLoader.java, FileLoadingContext.java, ZoneFileContext.java
- **Was passiert hier?** Worldgen: Biome, Zonen, Prefabs, Caves, Container und Loader.
- **Hilft beim Modding?** Später wichtig für MVP B: NPC-Häuser automatisch in der Welt platzieren.

### `com/hypixel/hytale/server/worldgen/loader/prefab/`

- **Java-Dateien direkt:** 4
- **Java-Dateien gesamt:** 5
- **Beispiele:** BlockPlacementMaskJsonLoader.java, BlockPlacementMaskRegistry.java, PrefabPatternGeneratorJsonLoader.java, WeightedPrefabMapJsonLoader.java
- **Was passiert hier?** Worldgen: Biome, Zonen, Prefabs, Caves, Container und Loader.
- **Hilft beim Modding?** Später wichtig für MVP B: NPC-Häuser automatisch in der Welt platzieren.

### `com/hypixel/hytale/server/worldgen/loader/prefab/unique/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** UniquePrefabConfigurationJsonLoader.java
- **Was passiert hier?** Worldgen: Biome, Zonen, Prefabs, Caves, Container und Loader.
- **Hilft beim Modding?** Später wichtig für MVP B: NPC-Häuser automatisch in der Welt platzieren.

### `com/hypixel/hytale/server/worldgen/loader/util/`

- **Java-Dateien direkt:** 7
- **Java-Dateien gesamt:** 7
- **Beispiele:** ColorUtil.java, FileMaskCache.java, NoiseBlockArrayJsonLoader.java, ResolvedBlockArrayJsonLoader.java, ResolvedVariantsBlockArrayLoader.java, Vector2dJsonLoader.java, Vector3dJsonLoader.java
- **Was passiert hier?** Worldgen: Biome, Zonen, Prefabs, Caves, Container und Loader.
- **Hilft beim Modding?** Später wichtig für MVP B: NPC-Häuser automatisch in der Welt platzieren.

### `com/hypixel/hytale/server/worldgen/loader/zone/`

- **Java-Dateien direkt:** 7
- **Java-Dateien gesamt:** 7
- **Beispiele:** UniqueZoneEntryJsonLoader.java, ZoneBiomesJsonLoader.java, ZoneColorMappingJsonLoader.java, ZoneCustomBiomesJsonLoader.java, ZoneJsonLoader.java, ZonePatternProviderJsonLoader.java, ZoneRequirementJsonLoader.java
- **Was passiert hier?** Worldgen: Biome, Zonen, Prefabs, Caves, Container und Loader.
- **Hilft beim Modding?** Später wichtig für MVP B: NPC-Häuser automatisch in der Welt platzieren.

### `com/hypixel/hytale/server/worldgen/map/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** GeneratorChunkWorldMap.java
- **Was passiert hier?** Worldgen: Biome, Zonen, Prefabs, Caves, Container und Loader.
- **Hilft beim Modding?** Später wichtig für MVP B: NPC-Häuser automatisch in der Welt platzieren.

### `com/hypixel/hytale/server/worldgen/prefab/`

- **Java-Dateien direkt:** 5
- **Java-Dateien gesamt:** 7
- **Beispiele:** PrefabCategory.java, PrefabLoadingCache.java, PrefabPasteUtil.java, PrefabPatternGenerator.java, PrefabStoreRoot.java
- **Was passiert hier?** Worldgen: Biome, Zonen, Prefabs, Caves, Container und Loader.
- **Hilft beim Modding?** Später wichtig für MVP B: NPC-Häuser automatisch in der Welt platzieren.

### `com/hypixel/hytale/server/worldgen/prefab/unique/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** UniquePrefabConfiguration.java, UniquePrefabGenerator.java
- **Was passiert hier?** Worldgen: Biome, Zonen, Prefabs, Caves, Container und Loader.
- **Hilft beim Modding?** Später wichtig für MVP B: NPC-Häuser automatisch in der Welt platzieren.

### `com/hypixel/hytale/server/worldgen/util/`

- **Java-Dateien direkt:** 11
- **Java-Dateien gesamt:** 34
- **Beispiele:** ArrayUtli.java, BlockArray.java, BlockFluidEntry.java, ChunkThreadPoolExecutor.java, ChunkWorkerThreadFactory.java, ConstantNoiseProperty.java, ListPool.java, LogUtil.java …
- **Was passiert hier?** Worldgen: Biome, Zonen, Prefabs, Caves, Container und Loader.
- **Hilft beim Modding?** Später wichtig für MVP B: NPC-Häuser automatisch in der Welt platzieren.

### `com/hypixel/hytale/server/worldgen/util/bounds/`

- **Java-Dateien direkt:** 4
- **Java-Dateien gesamt:** 4
- **Beispiele:** ChunkBounds.java, IChunkBounds.java, IWorldBounds.java, WorldBounds.java
- **Was passiert hier?** Worldgen: Biome, Zonen, Prefabs, Caves, Container und Loader.
- **Hilft beim Modding?** Später wichtig für MVP B: NPC-Häuser automatisch in der Welt platzieren.

### `com/hypixel/hytale/server/worldgen/util/cache/`

- **Java-Dateien direkt:** 6
- **Java-Dateien gesamt:** 6
- **Beispiele:** Cache.java, CleanupFutureAction.java, CleanupRunnable.java, ConcurrentSizedTimeoutCache.java, SizedTimeoutCache.java, TimeoutCache.java
- **Was passiert hier?** Worldgen: Biome, Zonen, Prefabs, Caves, Container und Loader.
- **Hilft beim Modding?** Später wichtig für MVP B: NPC-Häuser automatisch in der Welt platzieren.

### `com/hypixel/hytale/server/worldgen/util/condition/`

- **Java-Dateien direkt:** 6
- **Java-Dateien gesamt:** 10
- **Beispiele:** BlockMaskCondition.java, FilteredBlockFluidCondition.java, HashSetBlockFluidCondition.java, HashSetIntCondition.java, IntConditionBuilder.java, RandomCoordinateCondition.java
- **Was passiert hier?** Worldgen: Biome, Zonen, Prefabs, Caves, Container und Loader.
- **Hilft beim Modding?** Später wichtig für MVP B: NPC-Häuser automatisch in der Welt platzieren.

### `com/hypixel/hytale/server/worldgen/util/condition/flag/`

- **Java-Dateien direkt:** 4
- **Java-Dateien gesamt:** 4
- **Beispiele:** CompositeInt2Flags.java, ConstantInt2Flags.java, FlagOperator.java, Int2FlagsCondition.java
- **Was passiert hier?** Worldgen: Biome, Zonen, Prefabs, Caves, Container und Loader.
- **Hilft beim Modding?** Später wichtig für MVP B: NPC-Häuser automatisch in der Welt platzieren.

### `com/hypixel/hytale/server/worldgen/util/function/`

- **Java-Dateien direkt:** 3
- **Java-Dateien gesamt:** 3
- **Beispiele:** ConstantCoordinateDoubleSupplier.java, ICoordinateDoubleSupplier.java, RandomCoordinateDoubleSupplier.java
- **Was passiert hier?** Worldgen: Biome, Zonen, Prefabs, Caves, Container und Loader.
- **Hilft beim Modding?** Später wichtig für MVP B: NPC-Häuser automatisch in der Welt platzieren.

### `com/hypixel/hytale/server/worldgen/zone/`

- **Java-Dateien direkt:** 7
- **Java-Dateien gesamt:** 7
- **Beispiele:** Zone.java, ZoneColorMapping.java, ZoneDiscoveryConfig.java, ZoneGeneratorResult.java, ZonePatternGenerator.java, ZonePatternGeneratorCache.java, ZonePatternProvider.java
- **Was passiert hier?** Worldgen: Biome, Zonen, Prefabs, Caves, Container und Loader.
- **Hilft beim Modding?** Später wichtig für MVP B: NPC-Häuser automatisch in der Welt platzieren.

### `com/hypixel/hytale/server/worldgen/zoom/`

- **Java-Dateien direkt:** 4
- **Java-Dateien gesamt:** 4
- **Beispiele:** ExactZoom.java, FuzzyZoom.java, PixelDistanceProvider.java, PixelProvider.java
- **Was passiert hier?** Worldgen: Biome, Zonen, Prefabs, Caves, Container und Loader.
- **Hilft beim Modding?** Später wichtig für MVP B: NPC-Häuser automatisch in der Welt platzieren.

### `com/hypixel/hytale/sneakythrow/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 10
- **Beispiele:** SneakyThrow.java, ThrowableRunnable.java
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/sneakythrow/consumer/`

- **Java-Dateien direkt:** 4
- **Java-Dateien gesamt:** 4
- **Beispiele:** ThrowableBiConsumer.java, ThrowableConsumer.java, ThrowableIntConsumer.java, ThrowableTriConsumer.java
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/sneakythrow/function/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** ThrowableBiFunction.java, ThrowableFunction.java
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/sneakythrow/supplier/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** ThrowableIntSupplier.java, ThrowableSupplier.java
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.

### `com/hypixel/hytale/storage/`

- **Java-Dateien direkt:** 2
- **Java-Dateien gesamt:** 2
- **Beispiele:** IndexedStorageFile.java, package-info.java
- **Was passiert hier?** Speicher-/Persistenz-Basis.
- **Hilft beim Modding?** Wichtig für gespeicherte NPCs, Marker und Claim-Daten.

### `com/hypixel/hytale/unsafe/`

- **Java-Dateien direkt:** 1
- **Java-Dateien gesamt:** 1
- **Beispiele:** UnsafeUtil.java
- **Was passiert hier?** Hilfs-/Fachordner innerhalb des Servercodes.
- **Hilft beim Modding?** Bei Bedarf ansehen, wenn dein Feature diesen Bereich berührt.
