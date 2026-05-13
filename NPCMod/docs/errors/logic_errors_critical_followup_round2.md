# Deep Logic Error Review – Restart / Relink / Respawn / Persistence

Projekt: **NPCMod / KeystoneNPC**  
Quelle: `NPCMod.zip`  
Modus: **PLAN-/ANALYSE-MODE**  
Status: **keine Dateien geändert**

---

## 0. Gesamturteil

**FAIL – es wurden weitere echte Logikfehler gefunden.**

Die erste Review hat bereits die Hauptfehler im Restart-/Relink-/Respawn-Gate gezeigt:

- `MISSING_ENTITY` ist nicht vollständig sticky.
- `RelinkOutcome.PENDING` wird im Caller nicht als Stop behandelt.
- `respawnAfterRestart` existiert in JSON/Java, wird aber nicht bis zur Respawn-Entscheidung genutzt.
- Auto-Respawn hängt nur an der globalen System-Property.

Die zweite tiefere Suche zeigt zusätzlich schwerere Folgefehler im State-Load, Save-on-Setup, Marker-Fallback, Chunk-Gate, Retry-Verhalten und Orphan-Handling.

**Höchstes Risiko:** Ein beschädigtes oder teilweise ungültiges `state.json` kann beim Start als leerer Zustand geladen und danach sofort überschrieben werden. Das kann alle NPC-/Marker-Daten löschen.

---

## 1. Verbindliche Baselines geprüft

Gefunden:

- `docs/safety/npc_restart_relink_control.md`
- `docs/safety/json_hierarchy.md`

Nicht gefunden:

- `AGENTS.md`

Wichtige aktive Safety-Regeln, gegen die geprüft wurde:

1. Kein Tick ohne gültige EntityRef.
2. Alte Navigation darf nach Restart nicht resumed werden.
3. UUID-Relink nur mit hartem Live-UUID-Match.
4. Auto-Respawn nur mit Chunk-Gate.
5. Kein dynamisches `setRoleName("KeystoneNPC_...")`.
6. Kein blindes Relinken per gleicher Role.
7. Anchor-Fallback muss AMBIGUOUS blockieren.
8. `MISSING_ENTITY` darf nicht automatisch pro Tick zu `NEEDS_RELINK` werden.
9. `DISABLED` bleibt `DISABLED`.
10. Runtime-State darf `state.json` nicht verfälschen.
11. Status-/Identity-Änderungen müssen dirty-safe gespeichert werden.
12. Keine Dedupe-Löschung nur wegen gleicher Role.

---

## 2. Neue kritische Logikfehler

---

### Fehler 1 — State-Load-Fehler kann komplettes `state.json` leeren

**Risiko:** kritisch  
**Betroffene Datei:** `JsonFileStateStore.java`  
**Betroffene Methode:** `load()`  
**Codebereich:** `JsonFileStateStore.java:43–95`  
**Folge-Datei:** `KeystoneNpcPlugin.java`  
**Folge-Methode:** `setup()`  
**Codebereich:** `KeystoneNpcPlugin.java:80–83`

Aktueller Ablauf:

```text
setup()
-> stateStore.load()
-> load() liest state.json
-> irgendein Parse-/Runtime-/Linkage-Fehler
-> return PluginState.empty()
-> markerRegistry.restore(empty)
-> scheduler.restore(empty)
-> saveState()
-> state.json wird leer/trunkiert gespeichert
```

Kritische Stellen:

```java
// JsonFileStateStore.java:80–95
catch (...) {
    return PluginState.empty();
}
```

```java
// KeystoneNpcPlugin.java:80–83
var loaded = stateStore.load();
markerRegistry.restore(loaded.markers(), loaded.activeMarkerIds());
scheduler.restore(loaded.npcs());
saveState();
```

Warum falsch:

`PluginState.empty()` wird sowohl für „Datei existiert nicht“ als auch für „Datei existiert, ist aber kaputt“ verwendet. Danach wird sofort gespeichert. Dadurch kann ein einziger kaputter Eintrag alle NPCs und Marker löschen.

Verletzte Safety-Idee:

- Persistenz darf bei Load-Fehlern nicht destruktiv reagieren.
- Restart darf keine Records verlieren, nur weil ein einzelner Eintrag fehlerhaft ist.
- Save/Dirty-System darf keinen kaputten Load als absichtlich leeren Zustand interpretieren.

Minimaler Fix-Vorschlag:

- `load()` muss ein Ergebnis mit Status liefern, z. B. `LoadResult { state, success, error }`.
- `setup()` darf nach `loadFailed` **nicht** `saveState()` ausführen.
- Optional: beschädigte Datei nach `state.json.corrupt.<timestamp>` sichern.
- Optional: per-record Quarantine statt kompletter Empty-State.

Safety-Datei anpassen:

Ja. `npc_restart_relink_control.md` sollte eine Regel enthalten:

```text
Ein fehlgeschlagener State-Load darf niemals automatisch als leerer State zurückgespeichert werden.
```

---

### Fehler 2 — Ein ungültiger NPC-State kann den gesamten State-Load abbrechen

**Risiko:** kritisch  
**Betroffene Datei:** `JsonFileStateStore.java`  
**Betroffene Methode:** `toNpcRecord(...)`  
**Codebereich:** `JsonFileStateStore.java:176–221`

Problemstelle:

```java
// JsonFileStateStore.java:184–186
if (npc.state() != null) {
    record.state(NpcState.valueOf(npc.state()));
}
```

Für `entityStatus` gibt es einen lokalen Fallback:

```java
// JsonFileStateStore.java:190–203
try {
    record.entityStatus(NpcEntityStatus.valueOf(npc.entityStatus()));
} catch (IllegalArgumentException ex) {
    ... fallback ...
}
```

Für `state` gibt es diesen Schutz nicht.

Aktueller Fehlerfluss:

```text
Ein NPC in state.json hat state="OLD_STATE" oder Tippfehler
-> NpcState.valueOf(...) wirft IllegalArgumentException
-> load() catcht global
-> return PluginState.empty()
-> setup() speichert leer
-> alle NPCs/Marker weg
```

Warum falsch:

Ein einzelner kaputter Runtime-State darf nicht die gesamte Persistenz zerstören.

Minimaler Fix:

- `NpcState.valueOf(...)` lokal absichern.
- Ungültigen State auf sicheren Restore-State setzen, z. B. `IDLE` oder marker-autorisierte Zielstate-Normalisierung.
- Fehler pro NPC loggen, nicht gesamten Load abbrechen.

Safety-Datei anpassen:

Ja.

---

### Fehler 3 — Ungültiger Marker-Type kann ebenfalls kompletten Load zerstören

**Risiko:** kritisch  
**Betroffene Datei:** `JsonFileStateStore.java`  
**Betroffene Methode:** `toMarkerRecord(...)`  
**Codebereich:** `JsonFileStateStore.java:167–173`

Problemstelle:

```java
return new MarkerRecord(
    Objects.requireNonNull(marker.markerId()),
    new WorldId(Objects.requireNonNull(marker.worldId())),
    toVec3(marker.position()),
    MarkerType.valueOf(Objects.requireNonNull(marker.type()))
);
```

Fehlerfluss:

```text
Ein Marker hat type="bed" statt "BED"
oder alter MarkerType aus Legacy-Zeit
-> MarkerType.valueOf(...) wirft
-> load() gibt PluginState.empty()
-> setup() speichert leer
```

Warum falsch:

Marker sind zentrale Anchor-/Relink-Daten. Ein einzelner ungültiger Marker darf nicht alle NPC-Records löschen.

Minimaler Fix:

- Marker pro Eintrag validieren.
- Ungültige Marker quarantänen/überspringen.
- `state.json` nicht sofort überschreiben.

---

### Fehler 4 — Restore normalisiert Status und Marker und speichert das sofort zurück

**Risiko:** hoch  
**Betroffene Dateien:**

- `NpcRoutineRunner.java`
- `KeystoneNpcPlugin.java`

**Betroffene Methoden:**

- `restore(...)`
- `reconcilePersistedMarkerAssignments(...)`
- `setup()`

Ablauf:

```text
state.json wird geladen
-> scheduler.restore(...)
-> restore(...) verändert Runtime/Persistenz-Felder
-> setup() ruft sofort saveState()
-> veränderte Zustände werden vor echtem Relink/Respawn gespeichert
```

Kritische Folgen:

1. Persistiertes `MISSING_ENTITY` kann zu `NEEDS_RELINK` werden.
2. Marker-Fallback kann Marker-Zuweisungen ändern.
3. Positionen können normalisiert werden.
4. Diese Zwischenzustände werden sofort in `state.json` geschrieben.

Warum falsch:

Restore sollte zwischen „disk state lesen“, „runtime vorbereiten“ und „persistente Korrektur bewusst speichern“ unterscheiden. Aktuell kann reines Laden den gespeicherten Zustand verändern.

Minimaler Fix:

- `setup()` nicht blind direkt nach Restore speichern.
- Restore-Normalisierung nur speichern, wenn bewusst `markDirty` und valide Recovery abgeschlossen ist.
- Marker-Fallback im Restore nicht mutierend ausführen.

---

### Fehler 5 — Marker-Resolver hat versteckte Persistenz-Seiteneffekte

**Risiko:** hoch  
**Betroffene Datei:** `MarkerResolver.java`  
**Betroffene Methode:** `resolveRequiredMarkerWithFallback(...)`  
**Codebereich:** `MarkerResolver.java:68–92`

Problemstelle:

```java
Optional<MarkerRecord> fallback = markerRegistry.getNextAvailable(...);
if (fallback.isPresent()) {
    setMarkerIdForType(npc, markerType, newMarkerId);
    logInfo("MARKER_FALLBACK_SELECTED", ...);
    return fallback;
}
```

Diese Methode klingt wie ein reiner Resolver, verändert aber den NPC-Record.

Sie wird unter anderem benutzt in:

- Restore-/Marker-Reconcile
- Required-Marker-Checks
- Routine-Target-Resolve
- Missing-Marker-Diagnose
- Anchor-/Fallback-nahe Logik
- Chunk-Gate-Zielsuche über State-Preferred-Marker

Warum falsch:

Ein read/check/resolve-Pfad darf nicht heimlich persistente Marker-Zuweisungen ändern. Besonders gefährlich ist das beim Restart, weil `setup()` danach direkt speichert.

Beispiel-Fehlerfluss:

```text
NPC hat alten bedMarkerId
Marker fehlt
resolveRequiredMarkerWithFallback(...) findet irgendeinen verfügbaren BED-Marker
-> schreibt neue bedMarkerId in NPC
-> setup() speichert state.json
-> NPC ist dauerhaft an falsches Bett gebunden
```

Bei mehreren Lumberjacks kann das später Anchor-Fallback verfälschen.

Minimaler Fix:

- Trennen in:
  - `peekRequiredMarkerWithFallback(...)` ohne Mutation
  - `assignFallbackMarker(...)` mit explizitem Dirty/Command-Kontext
- Im Restore keine automatische Marker-Neuzuweisung ohne klare Regel.

Safety-Datei anpassen:

Ja, besonders `json_hierarchy.md` und `npc_restart_relink_control.md`.

---

### Fehler 6 — Chunk-Gate prüft eventuell andere Position als tatsächlicher Spawn

**Risiko:** hoch  
**Betroffene Datei:** `NpcRoutineRunner.java`  
**Betroffene Methoden:**

- `passesAutoRespawnChunkGate(...)`
- `resolveAutoRespawnChunkGateTarget(...)`
- `spawnNpcEntity(...)`

Codebereiche:

- Chunk-Gate: `NpcRoutineRunner.java:1462–1514`
- Spawn-Position: `NpcRoutineRunner.java:1305–1312`

Chunk-Gate-Ziel:

```java
Optional<MarkerRecord> idleMarker = resolveStatePreferredMarker(npc);
if (idleMarker.isPresent()) {
    return new AutoRespawnChunkGateTarget(idleMarker.get().position(), "idle-marker");
}
return currentPosition;
```

Tatsächlicher Spawn:

```java
Vec3 position = npc.currentPosition() != null ? npc.currentPosition() : new Vec3(0, 0, 0);
NPCPlugin.get().spawnEntity(..., spawnPosition, ...);
```

Fehlerfluss:

```text
NPC war beim Shutdown WALKING_TO_WORK
currentPosition liegt in Chunk A
resolveStatePreferredMarker(...) gibt WORK-Marker in Chunk B zurück
Chunk B ist geladen -> Gate PASS
spawnNpcEntity(...) spawnt aber bei currentPosition in Chunk A
Chunk A ist eventuell nicht geladen
```

Warum falsch:

Die Safety-Regel „kein Auto-Respawn ohne Chunk-Gate“ wird semantisch verletzt, weil nicht garantiert ist, dass der geprüfte Chunk auch der Spawn-Chunk ist.

Minimaler Fix:

- Chunk-Gate muss exakt dieselbe Position prüfen, die `spawnNpcEntity(...)` nutzt.
- Oder `spawnNpcEntity(...)` muss die vom Gate geprüfte sichere Position verwenden.
- Bei Walking-State nach Restart besser auf authoritative idle marker normalisieren oder Auto-Respawn blockieren.

---

### Fehler 7 — `RelinkOutcome.PENDING` vermischt Retry, Fehler und terminales Missing

**Risiko:** hoch  
**Betroffene Datei:** `RelinkWorkflowService.java`  
**Betroffene Methode:** `tryRelinkEntityRef(...)`  
**Codebereich:** ca. `RelinkWorkflowService.java:120–245`

`PENDING` wird für unterschiedliche Situationen verwendet:

- UUID fehlt.
- UUID ist ungültig.
- UUID wurde noch nicht gefunden, Retry läuft.
- UUID-Giveup hat gerade `MISSING_ENTITY` gesetzt.
- UUID gehört anderem Record.
- Live-UUID gehört anderem Record.
- Entity hat falsche Role.

Caller:

```java
RelinkOutcome relinkOutcome = tryRelinkEntityRef(...);
if (relinkOutcome == SUCCESS) continue;
// PENDING läuft weiter zu RolePrefix, Anchor, Auto-Respawn
```

Warum falsch:

`PENDING` ist keine ausreichende Entscheidungsinformation. Ein Retry-Pending muss den aktuellen Durchlauf stoppen. Ein terminales Missing muss ebenfalls stoppen oder in einen klaren Recovery-Pfad gehen. Ein echter No-Match darf Fallbacks erlauben.

Minimaler Fix:

Outcome aufsplitten, z. B.:

```text
SUCCESS
RETRY_PENDING
TERMINAL_MISSING
NO_UUID
NO_MATCH
BLOCKED_CLAIMED
BLOCKED_WRONG_ROLE
```

Dann im Caller nur bei echtem `NO_MATCH` zu Anchor-Fallback/Respawn weitergehen.

---

### Fehler 8 — Nach `RELINK_GIVEUP_MARKED_MISSING` beginnt der UUID-Retry später erneut

**Risiko:** hoch  
**Betroffene Datei:** `RelinkWorkflowService.java`  
**Betroffene Methode:** `tryRelinkEntityRef(...)`

Ablauf:

```text
UUID nicht gefunden
-> Miss-Count steigt
-> Giveup
-> entityStatus = MISSING_ENTITY
-> uuidRelinkMissCounts.remove(npcId)
-> return PENDING
```

Danach ruft `spawnRestoredNpcs("tick-retry")` weiter regelmäßig `tryRelinkEntityRef(...)` auf.

Weil die Miss-Counter gelöscht sind, startet die Retry-Sequenz wieder von vorne.

Warum falsch:

`MISSING_ENTITY` ist kein echter Ruhe-Endzustand. Das erzeugt wiederholte Retry-/Giveup-Zyklen und potenziell Log-Spam.

Minimaler Fix:

- `MISSING_ENTITY` nicht automatisch durch `tryRelinkEntityRef` schicken.
- Oder Giveup als terminalen Zustand mit Cooldown/Recovery-Key speichern.
- Severe-Logs für Giveup nicht zyklisch auslösen.

---

### Fehler 9 — Auto-Respawn hat Failure-Backoff, aber keinen echten Max-Failure-Stop

**Risiko:** mittel bis hoch  
**Betroffene Datei:** `NpcRoutineRunner.java`  
**Betroffene Methode:** `spawnRestoredNpcs(...)`  
**Codebereich:** `NpcRoutineRunner.java:1064–1089`

Bei Spawn-Fehler:

```java
failureCount++
delayMs = min(max, exponential)
respawnRetryAtMs = now + delayMs
```

Es gibt keinen finalen Stop nach `RESPAWN_MAX_FAILURES`.

Auffällig:

`RespawnRecoveryService` hat Logik für maximale Fehlerzahl, aber diese Service-Logik scheint im Auto-Respawn-Pfad nicht verwendet zu werden.

Fehlerfluss:

```text
NPC MISSING_ENTITY
Auto-Respawn erlaubt
Spawn schlägt dauerhaft fehl
-> Retry mit wachsendem Delay
-> nie terminal DISABLED/MISSING_STUCK/REQUIRES_MANUAL
-> wiederkehrende Fehlerlogs
```

Warum falsch:

Das System kann in einem endlosen Auto-Recovery-Loop bleiben.

Minimaler Fix:

- Nach Max-Failures Auto-Respawn für diesen NPC blockieren.
- Status bleibt `MISSING_ENTITY`, aber mit `manualRecoveryRequired` oder interner Cooldown-Struktur.
- Log nur einmal pro Schwelle.

---

### Fehler 10 — Globaler Auto-Respawn läuft pro World-Store-Tick über alle NPCs

**Risiko:** mittel bis hoch  
**Betroffene Datei:** `NpcRoutineRunner.java`  
**Betroffene Methode:** `tickStore(...)`  
**Codebereich:** `NpcRoutineRunner.java:1664–1690`

Problemstelle:

```java
public void tickStore(Store<EntityStore> store) {
    World world = store.getExternalData().getWorld();
    spawnRestoredNpcs("tick-retry");
    ...
}
```

`spawnRestoredNpcs(...)` läuft global über `npcs.values()` und sucht selbst die World per `Universe`.

Fehler in Multi-World-Kontext:

```text
2 Worlds / Stores ticken
-> tickStore World A ruft spawnRestoredNpcs für alle NPCs
-> tickStore World B ruft spawnRestoredNpcs erneut für alle NPCs
-> UUID-MissCounts, Relink-Retry und Logs laufen doppelt so schnell
```

`spawnRequestsInFlight` schützt nur vor parallelem Spawn pro NPC, aber nicht vor mehrfach beschleunigtem Relink-/Giveup-/Log-Verhalten.

Minimaler Fix:

- `spawnRestoredNpcs` nur einmal global pro Server-Tick ausführen.
- Oder world-scoped Variante: `spawnRestoredNpcsForWorld(world, trigger)`.
- Retry-Counter nur in der richtigen World erhöhen.

---

### Fehler 11 — UUID-Ownership-Maps sind potenziell case-sensitiv

**Risiko:** mittel  
**Betroffene Datei:** `RelinkWorkflowService.java`  
**Betroffene Bereiche:** Ownership-Snapshot / UUID-Claims

Beobachtung:

Mehrere Stellen vergleichen UUIDs robust mit `equalsIgnoreCase` oder `UUID.fromString(...)`. Ownership-Maps speichern/prüfen aber offenbar raw UUID-Strings.

Fehlerfluss:

```text
Record A speichert UUID uppercase
Live entity liefert UUID lowercase
ownerByUuid lookup findet Record A nicht
anderer Record kann Kandidat als unclaimed sehen
```

Warum falsch:

UUID-Identität muss normalisiert werden. Case darf keine Ownership-Sicherheitsprüfung umgehen.

Minimaler Fix:

- Alle UUID-Keys in Ownership-Maps normalisieren: `toLowerCase(Locale.ROOT)`.
- Beim Persistieren optional ebenfalls normalisieren.

---

### Fehler 12 — `hasLiveEntity(...)` / Live-Gate vertraut gültiger Ref ohne harte Ownership-Prüfung

**Risiko:** mittel  
**Betroffene Datei:** `NpcRoutineRunner.java`  
**Betroffene Methoden:**

- `spawnRestoredNpcs(...)`
- `passesLiveEntityGate(...)`
- `spawnNpcEntity(...)`

Problem:

Wenn `npc.entityRef()` gültig ist, wird sie als korrekt akzeptiert.

Beispiel:

```java
if (hasLiveEntity(npc)) {
    npc.entityStatus(ACTIVE);
    updatePersistedEntityIdentity(npc, npc.entityRef());
    continue;
}
```

Warum kritisch:

Nach Restart ist `entityRef` absichtlich null, also trifft es nicht den häufigsten Fall. Aber innerhalb eines laufenden Servers kann ein falscher Ref durch einen Bug oder zukünftigen Command gesetzt werden. Dann überschreibt `updatePersistedEntityIdentity(...)` die gespeicherte UUID mit der falschen Live-Entity.

Minimaler Fix:

- Live-Gate sollte bei vorhandener `entityUuid` prüfen:
  - Live UUID == persisted UUID
  - oder Record hat keine UUID und Ref wurde gerade durch sicheren Spawn gesetzt.
- Optional roleIndex/owner check vor `ACTIVE`.

---

### Fehler 13 — `linkEntityRef(...)` ist gefährlich offen

**Risiko:** mittel  
**Betroffene Datei:** `NpcRoutineRunner.java`  
**Betroffene Methode:** `linkEntityRef(...)`

Beobachtung:

Die Methode setzt EntityRef und Status sehr direkt. Auch wenn sie aktuell nicht prominent genutzt wird, ist sie als API im System gefährlich, weil sie keinen harten UUID-/Role-/Ownership-Check erzwingt.

Warum relevant:

Spätere Commands oder Recovery-Tools könnten diese Methode verwenden und damit Safety-Regeln umgehen.

Minimaler Fix:

- Methode intern machen oder umbenennen in `unsafeLinkEntityRefForSpawnOnly`.
- Validierte Variante erzwingen: `linkEntityRefAfterVerifiedUuidMatch(...)`.

---

### Fehler 14 — Manuelles Respawn-Dry-Run kann `PENDING` als „würde spawnen“ interpretieren

**Risiko:** mittel bis hoch  
**Betroffene Datei:** `NpcRoutineRunner.java`  
**Betroffene Methode:** `respawnMissingNpcsInWorld(...)`  
**Codebereich:** `NpcRoutineRunner.java:563–836`

Ablauf:

```text
status == MISSING_ENTITY
-> dryRun: evaluateRelinkEntityRefDetailed(...)
-> outcome PENDING wird nicht separat gestoppt
-> RolePrefix/Anchor werden geprüft
-> wenn nichts found: WOULD_SPAWN + FORCE_REQUIRED
```

Problem:

Wenn `PENDING` eigentlich „UUID-Relink noch nicht final“ bedeutet, darf Dry-Run nicht sagen „würde spawnen“.

Minimaler Fix:

- Auch hier Outcome differenzieren.
- Dry-run muss zwischen `wouldRetry`, `wouldStayMissing`, `wouldSpawnWithForce` unterscheiden.

---

### Fehler 15 — Manuelles Force-Respawn ignoriert JSON-Policy und Auto-Respawn-Safety-Ziele

**Risiko:** mittel  
**Betroffene Datei:** `NpcRoutineRunner.java`  
**Betroffene Methode:** `respawnMissingNpcsInWorld(...)`

Manuelles `--force` ist grundsätzlich okay, aber aus Safety-Sicht muss klar sein:

- Es nutzt nicht `respawnAfterRestart`.
- Es ist nicht dasselbe wie Auto-Respawn.
- Es muss sehr deutlich als manuelle Recovery gelten.

Minimaler Fix:

- Safety-Datei ergänzen:
  - JSON `respawnAfterRestart=false` blockiert Auto-Respawn, nicht zwingend Admin-Force.
  - Admin-Force braucht harte Prechecks, Log und idealerweise Dry-Run-Hinweis.

---

### Fehler 16 — `state.json` speichert weiterhin Runtime-nahe States

**Risiko:** mittel  
**Betroffene Datei:** `JsonFileStateStore.java`  
**Betroffene Methode:** `toPersistedNpc(...)`  
**Codebereich:** `JsonFileStateStore.java:143–164`

Problemstelle:

```java
npc.state().name()
```

Es wird immer der aktuelle `NpcState` gespeichert, z. B. potenziell:

- `WALKING_TO_BED`
- `WALKING_TO_WORK`
- `PAUSED_MISSING_MARKER`
- Zwischenzustände aus Door-/Action-/Routine-Logik

Gleichzeitig existiert im PersistenceProfile:

```java
Boolean saveState
Boolean saveRoutineProgress
```

Diese Felder werden aber nicht aktiv genutzt.

Warum falsch:

Safety sagt, Runtime-State darf `state.json` nicht verfälschen. Wenn Walking-/Pause-Zustände persistiert werden, startet der NPC eventuell nach Restart in einem transienten Zustand, obwohl Navigation bewusst gelöscht wurde.

Minimaler Fix:

- Beim Speichern persistente und runtime States trennen.
- Wenn `saveRoutineProgress=false`: nur authoritative Idle-/Routine-State oder `IDLE` speichern.
- Walking-State nach Restart nicht als aktive Navigation interpretieren.

---

### Fehler 17 — `currentPosition` wird immer gespeichert, `savePosition` wird ignoriert

**Risiko:** mittel  
**Betroffene Datei:** `JsonFileStateStore.java`  
**Betroffene Methode:** `toPersistedNpc(...)`

`PersistenceProfile` enthält:

```java
Boolean savePosition
```

Aber `toPersistedNpc(...)` speichert immer:

```java
position == null ? null : new PersistedVec3(...)
```

Warum relevant:

Für persistente Bürger kann das okay sein. Für despawnable/hostile Profile ist es aber falsch. Besonders in Kombination mit Auto-Respawn kann eine alte, transient gelaufene Position zum Spawn-Anker werden.

Minimaler Fix:

- PersistenceProfile aktiv auswerten.
- `savePosition=false` respektieren.
- Für Auto-Respawn klar definieren, welche Position erlaubt ist.

---

### Fehler 18 — Remove/Clear kann Live-Entity-Orphans erzeugen, wenn World fehlt oder Queue nicht ausführt

**Risiko:** mittel  
**Betroffene Datei:** `NpcRoutineRunner.java`  
**Betroffene Methoden:**

- `removeNpc(...)`
- `clearNpcs(...)`
- `removeLiveEntity(...)`

Codebereiche:

- `removeNpc(...)`: `NpcRoutineRunner.java:507–523`
- `removeLiveEntity(...)`: `NpcRoutineRunner.java:1563–1583`

Ablauf:

```text
removeNpc(...)
-> npcs.remove(npcId)
-> clear runtime maps
-> removeLiveEntity(npc)
-> markDirty()
```

`removeLiveEntity(...)`:

```java
Ref liveRef = npc.entityRef();
clearEntityIdentity(npc);
if liveRef invalid return;
World world = Universe.get().getWorld(...);
if world == null return;
world.execute(() -> removeEntity(liveRef));
```

Problem:

Der Record wird zuerst aus der Map entfernt. Wenn die World nicht gefunden wird oder die queued removal nicht mehr ausführt, bleibt die Live-Entity in der Welt, aber der Record ist weg. Beim nächsten Start kann sie als Orphan erscheinen.

Minimaler Fix:

- Bei fehlender World nicht sofort Record löschen oder zumindest Tombstone/cleanup marker speichern.
- Removal-Erfolg/queued state explizit verfolgen.
- Clear/Remove sollte nach Möglichkeit erst nach erfolgreicher Entity-Removal final speichern.

---

### Fehler 19 — `spawnNpcEntity(...)` kann bei vorhandener LiveRef Erfolg melden, ohne UUID zu fixen

**Risiko:** niedrig bis mittel  
**Betroffene Datei:** `NpcRoutineRunner.java`  
**Betroffene Methode:** `spawnNpcEntity(...)`  
**Codebereich:** `NpcRoutineRunner.java:1264–1269`

Problemstelle:

```java
if (hasLiveEntity(npc)) {
    npc.entityId(1);
    npc.entityStatus(NpcEntityStatus.ACTIVE);
    return true;
}
```

Anders als `spawnRestoredNpcs(...)` aktualisiert dieser Early-Return nicht die persistierte Entity-UUID und markiert nicht dirty.

Warum relevant:

Wenn diese Methode mit gültiger Ref, aber leerer/staler `entityUuid` aufgerufen wird, meldet sie Erfolg, ohne Persistenz zu reparieren.

Minimaler Fix:

- Auch im Early-Return `updatePersistedEntityIdentity(...)` + Dirty-Check ausführen.
- Oder Early-Return entfernen und LiveRef vorher validieren.

---

### Fehler 20 — `AUTO_RESPAWN_DISABLED` ist als Severe-Log wahrscheinlich zu hart und kann zyklisch wiederkehren

**Risiko:** niedrig bis mittel  
**Betroffene Datei:** `NpcRoutineRunner.java`  
**Betroffene Methode:** `spawnRestoredNpcs(...)`  
**Codebereich:** `NpcRoutineRunner.java:1041–1048`

Wenn Auto-Respawn global deaktiviert ist, wird `AUTO_RESPAWN_DISABLED` als `logSevere` ausgegeben. Zwar verhindert `lastValidationWarningKey` Wiederholung pro NPC im laufenden Prozess, aber nach Restart oder Statusmutation kann es wieder auftreten.

Warum relevant:

Wenn `MISSING_ENTITY` sticky sein soll, ist „Auto-Respawn disabled“ eher ein einmaliger Statushinweis, kein Severe-Fehler pro Recovery-Runde.

Minimaler Fix:

- Als info/warn mit dauerhaftem per-NPC Cooldown behandeln.
- Nur severe, wenn Konfiguration widersprüchlich ist, z. B. JSON verlangt Respawn, global kill-switch blockiert.

---

## 3. Bereits bestätigte Fehler aus Review 1, weiterhin gültig

Diese Punkte bleiben bestehen und werden durch die neue Analyse nicht widerlegt:

### A — `MISSING_ENTITY` wird beim Restore zu `NEEDS_RELINK`

**Datei:** `NpcRoutineRunner.java`  
**Methode:** `restore(...)`

Fehlerfluss:

```text
state.json: entityStatus=MISSING_ENTITY, entityUuid vorhanden
-> restore(...)
-> entityUuid vorhanden
-> entityStatus=NEEDS_RELINK
```

Folge:

`MISSING_ENTITY` ist über Restart hinweg nicht sticky.

---

### B — `respawnAfterRestart` ist nur vorbereitet, nicht aktiv

**Dateien:**

- `PersistenceProfile.java`
- `NpcTemplateResolver.java`
- `RespawnPolicyConfig.java`
- `NpcRoutineRunner.java`

Trace:

```text
persistent_citizen.json
-> PersistenceProfile Feld existiert
-> NpcTemplateResolver validiert nur Pfad
-> EffectiveNpcDefinition enthält kein PersistenceProfile
-> NpcRecord enthält kein respawnAfterRestart
-> spawnRestoredNpcs fragt nur System-Property
```

---

### C — Globaler Auto-Respawn-Kill-Switch ist einzige aktive Policy

**Datei:** `RespawnPolicyConfig.java`

Aktiv ist nur:

```text
-Dknpc.enableAutoRespawnMissingNpc=true/false
```

Nicht aktiv:

```text
profiles.persistence.respawnAfterRestart
```

---

### D — Role-Prefix-Fallback ist deaktiviert

**Bewertung:** gut / Safety-konform

Aktiver Java-Code enthält keinen dynamischen `setRoleName("KeystoneNPC_...")`.

Treffer für `KeystoneNPC_` und `setRoleName` liegen nur in Safety-Dokumenten.

---

### E — Dedupe löscht nicht blind wegen gleicher Role

**Bewertung:** gut / Safety-konform

Dedupe löscht nicht einfach gleiche Lumberjack-Roles. Es braucht Ownership-/Same-Record-Beweis.

---

## 4. Gefährliche Fehlerketten

### Fehlerkette 1 — Kaputter State wird gelöscht

```text
state.json enthält einen ungültigen NpcState
-> JsonFileStateStore.toNpcRecord wirft
-> load() gibt PluginState.empty()
-> setup() restored leere Marker/NPCs
-> saveState() überschreibt state.json leer
-> alle NPCs/Marker verloren
```

**Priorität:** sofort prüfen/fixen.

---

### Fehlerkette 2 — Missing-Entity-Loop

```text
NPC fehlt nach Restart
-> NEEDS_RELINK
-> UUID-Retry
-> Giveup -> MISSING_ENTITY
-> Counter gelöscht
-> tick-retry ruft tryRelink wieder auf
-> Retry startet neu
```

**Priorität:** hoch.

---

### Fehlerkette 3 — Falscher Marker durch Resolver-Side-Effect

```text
Persistierter Marker fehlt
-> restore/check/routine ruft resolveRequiredMarkerWithFallback
-> Resolver weist neuen Marker zu
-> setup speichert direkt
-> NPC bindet dauerhaft falschen Marker
-> Anchor-Fallback kann später falsche Entity wählen
```

**Priorität:** hoch.

---

### Fehlerkette 4 — Chunk-Gate prüft Marker, Spawn nutzt alte Position

```text
NPC ist WALKING_TO_WORK
-> Gate prüft WORK-Marker-Chunk
-> Spawn nutzt currentPosition
-> Spawn kann in ungeprüftem Chunk passieren
```

**Priorität:** hoch.

---

## 5. Statuswechsel-Matrix – aktualisiert

| Von | Nach | Bewertung | Wo | Problem |
|---|---|---:|---|---|
| RESTORE | NEEDS_RELINK | teilweise erlaubt | `NpcRoutineRunner.restore` | falsch, wenn vorher `MISSING_ENTITY` war |
| RESTORE | MISSING_ENTITY | erlaubt | `NpcRoutineRunner.restore` | nur bei fehlender UUID |
| RESTORE | leerer State | kritisch falsch | `JsonFileStateStore.load` + `setup.saveState` | Load-Fehler kann Persistenz löschen |
| NEEDS_RELINK | ACTIVE | erlaubt | `RelinkWorkflowService` | nur bei UUID-Hard-Match gut |
| NEEDS_RELINK | MISSING_ENTITY | erlaubt | `RelinkWorkflowService` | nach Giveup, aber nicht terminal genug |
| MISSING_ENTITY | NEEDS_RELINK | falsch | `restore(...)` | Sticky verletzt |
| MISSING_ENTITY | RELINK_RETRY | kritisch | `spawnRestoredNpcs` + `tryRelink` | Loop möglich |
| MISSING_ENTITY | ACTIVE | erlaubt | Relink/Respawn | nur bei geprüftem Relink/Respawn |
| ACTIVE | ACTIVE mit fremder Ref | riskant | `hasLiveEntity`-Pfad | Ref wird nicht hart validiert |
| DISABLED | irgendwas | nicht gefunden | mehrere Guards | aktuell weitgehend geschützt |

---

## 6. Priorisierter Minimal-Fix-Plan

### Step 1 — State-Load darf niemals destruktiv leer speichern

**Erlaubte Dateien:**

- `JsonFileStateStore.java`
- `KeystoneNpcPlugin.java`

**Ziel:**

- `load()` unterscheidet „nicht vorhanden“ vs. „kaputt“.
- `setup()` speichert nach Load-Fehler nicht.
- Fehlerhafte Einträge werden später quarantänisiert.

**Test-Gate:**

- Kaputtes `state.json` mit ungültigem NPC-State.
- Serverstart darf Originaldatei nicht überschreiben.

---

### Step 2 — `MISSING_ENTITY` sticky machen

**Erlaubte Dateien:**

- `NpcRoutineRunner.java`
- optional `RelinkWorkflowService.java`

**Ziel:**

- Restore respektiert persistiertes `MISSING_ENTITY`.
- `MISSING_ENTITY` geht nicht automatisch wieder in UUID-Retry.

---

### Step 3 — Relink-Outcomes aufsplitten

**Erlaubte Dateien:**

- `RelinkWorkflowService.java`
- `NpcRoutineRunner.java`

**Ziel:**

- `PENDING` nicht mehr als Sammelzustand für alles.
- Caller stoppt korrekt bei Retry/Terminal/Blocked.

---

### Step 4 — Marker-Resolver side-effect-frei machen

**Erlaubte Dateien:**

- `MarkerResolver.java`
- `StateTargetingService.java`
- `IdleMarkerService.java`
- `NpcRoutineRunner.java`

**Ziel:**

- Resolver darf nicht heimlich Marker-IDs setzen.
- Explizite Marker-Neuzuweisung nur in Command/Recovery-Kontext.

---

### Step 5 — Auto-Respawn-Position und Chunk-Gate vereinheitlichen

**Erlaubte Dateien:**

- `NpcRoutineRunner.java`

**Ziel:**

- Dieselbe Position prüfen und spawnen.
- Walking-State nach Restart nicht ungeprüft als Spawn-Position nutzen.

---

### Step 6 — PersistenceProfile aktiv machen oder Safety-Datei klar halten

**Erlaubte Dateien:**

- `NpcTemplateResolver.java`
- `EffectiveNpcDefinition.java`
- `PersistenceProfile.java`
- `NpcRoutineRunner.java`
- `JsonFileStateStore.java`
- `docs/safety/json_hierarchy.md`

**Ziel:**

- Entweder `respawnAfterRestart`, `saveState`, `savePosition` aktiv nutzen.
- Oder dokumentieren, dass diese Felder noch keine Runtime-Wirkung haben.

---

## 7. Dateien, die wahrscheinlich geändert werden müssten

Minimal bei echten Fixes:

```text
src/main/java/keystone/npc/persistence/JsonFileStateStore.java
src/main/java/keystone/npc/KeystoneNpcPlugin.java
src/main/java/keystone/npc/routine/NpcRoutineRunner.java
src/main/java/keystone/npc/relink/RelinkWorkflowService.java
src/main/java/keystone/npc/routine/marker/MarkerResolver.java
src/main/java/keystone/npc/routine/marker/IdleMarkerService.java
src/main/java/keystone/npc/routine/state/StateTargetingService.java
src/main/java/keystone/npc/definition/NpcTemplateResolver.java
src/main/java/keystone/npc/definition/EffectiveNpcDefinition.java
docs/safety/npc_restart_relink_control.md
docs/safety/json_hierarchy.md
```

---

## 8. Dateien/Systeme, die nicht unnötig geändert werden sollten

```text
Server/NPC/Roles/*
Door-System
Dedupe-Löschlogik, außer Outcome/Ownership-Normalisierung nötig
Spawn-Rollback, außer direkt betroffen
Engine-Role-Architektur
setRoleName / dynamische KeystoneNPC_-Roles
```

Nicht wieder einführen:

```text
KeystoneNPC_<npcId>_<roleId>_Role
NPCEntity.setRoleName("KeystoneNPC_...")
Blindes Relinken per gleicher Role
Auto-Respawn bei AMBIGUOUS
Dedupe-Löschung nur wegen gleicher Role
```

---

## 9. Test-Gates

Pflicht lokal im Projekt:

```bash
mvn -q -DskipTests test-compile
```

Hinweis:

In dieser Analyseumgebung ist `mvn` nicht verfügbar, daher konnte der Compile-Gate hier nicht ausgeführt werden.

Zusätzliche Tests:

```text
[ ] state.json mit ungültigem NPC state -> darf nicht leer überschrieben werden
[ ] state.json mit ungültigem Marker type -> darf nicht leer überschrieben werden
[ ] Restart mit MISSING_ENTITY + entityUuid -> bleibt MISSING_ENTITY
[ ] Restart mit fehlender Entity + respawnAfterRestart=false -> kein Respawn, kein Loop
[ ] Restart mit fehlender Entity + respawnAfterRestart=true -> nur mit globaler Freigabe + Chunk-Gate
[ ] Walking-State beim Restart -> Gate-Position == Spawn-Position
[ ] Mehrere Lumberjacks nahe beieinander -> AMBIGUOUS, kein Auto-Respawn
[ ] Marker fehlt -> Resolver darf nicht heimlich neuen Marker persistieren
[ ] Multi-World -> Relink-Retry zählt nicht mehrfach pro Server-Tick
[ ] Remove/Clear bei world=null -> keine stillen Orphans
[ ] 30 Sekunden Log-Beobachtung bei MISSING_ENTITY -> kein zyklisches Severe-Spam
```

---

## 10. Kurzfazit

Das System ist bereits deutlich sicherer als eine naive Respawn-Implementierung, aber der kritische Bereich ist nicht nur Relink selbst. Die tiefsten Logikfehler liegen jetzt in diesen vier Ketten:

1. **State-Load-Fehler + sofortiges Save = möglicher Datenverlust.**
2. **`MISSING_ENTITY` ist nicht terminal genug und kann weiter Retry-/Fallback-Pfade auslösen.**
3. **Marker-Resolver verändert Persistenz in eigentlich lesenden Pfaden.**
4. **Chunk-Gate und tatsächliche Spawn-Position können auseinanderlaufen.**

Priorität für den nächsten Agent-Step sollte daher nicht zuerst `respawnAfterRestart` sein, sondern:

```text
Step 1: State-Load destructive-save verhindern
Step 2: MISSING_ENTITY sticky/terminal machen
Step 3: RelinkOutcome sauber aufsplitten
Step 4: Marker-Resolver side-effect-frei machen
Step 5: Chunk-Gate == Spawn-Position garantieren
```
