# Critical Logic Error Hunt — Round 4

Projekt: **Hytale-Mod „NPCMod / KeystoneNPC“**  
Modus: **PLAN-/ANALYSE-MODE**  
Scope: **Nur Analyse. Keine Implementierung. Keine Codebase-Änderung.**

Untersuchte ZIP: `NPCMod.zip`

---

## 1. Gesamturteil

**FAIL — Es gibt weitere kritische Logikfehler / Hidden-Failure-Pfade.**

Diese Runde hat nicht nochmal nur die alten Respawn-/Relink-Fehler wiederholt, sondern gezielt nach weiteren Fehlern gesucht, die später NPCs falsch binden, State verlieren, Marker-Kontext canceln, Commands falsch ausführen oder Recovery blockieren können.

Neue oder stärker präzisierte kritische Fehler:

1. Active-Marker-Staging wird nach Spawn gelöscht und persistiert.
2. Marker-Set hat keinen Rollback bei Save-Failure.
3. Fehlende/kaputte `currentPosition` kann zu Spawn an `(0,0,0)` führen.
4. `linkEntityRef(...)` ist ein öffentlicher Blind-Bind zu `ACTIVE`.
5. Anchor-Relink kann UUID-lose Entity akzeptieren und alte UUID behalten.
6. UUID-Ownership ist nicht canonicalisiert; Case-Mismatch kann Claims brechen.
7. Restore-/Validation-Pfade mutieren Marker und `setup()` speichert direkt.
8. Spawn-Precheck-/Rollback sichert nur Identity, nicht Marker-Fallback-Mutationen.
9. Transiente Routine-/WorldTime-/Config-Fehler können dauerhaft `PAUSED_MISSING_MARKER` speichern.
10. Initial-Respawn-Once-Flag wird vor echter World-Bereitschaft gesetzt.
11. `tickStore(...)` triggert globales Restore/Respawn pro Store-Tick.
12. Admin-Commands speichern mit `saveState()` ohne Ergebnisprüfung.
13. `EntitySyncService.readPosition(...)` akzeptiert nicht-finite Live-Positionen.
14. `hasLiveEntity(...)` / gültige `entityRef` kann falsche Entity als aktiv behandeln.
15. Missing/invalid `currentPosition` ist nicht unterscheidbar von echter Origin-Position.

---

## 2. Neue / präzisierte kritische Fehler

---

### Fehler 1 — Active-Marker-Staging wird nach jedem Spawn gelöscht und persistiert

**Betroffene Datei:**  
`src/main/java/keystone/npc/commands/spawn/SpawnNpcCommand.java`

**Betroffene Stelle:**  
Nach erfolgreichem Spawn:

```text
markerRegistry.clearActive();
plugin.saveStateSafely();
```

**Statusfluss:**

```text
User setzt aktive Marker
-> /knpc spawn lumberjack Bob
-> NPC wird gespawnt
-> markerRegistry.clearActive()
-> state.json speichert activeMarkerIds leer
```

**Warum kritisch?**

Das ist nicht direkt NPC-Datenverlust, aber es löscht den aktiven Marker-Kontext dauerhaft. Wenn der User mehrere NPCs mit denselben vorbereiteten Markern spawnen möchte, wird der zweite Spawn plötzlich blockiert oder bekommt keinen aktiven Marker-Kontext mehr.

Das ist besonders riskant, weil `clearActive()` nicht nur Runtime-Komfort löscht, sondern über `saveStateSafely()` direkt persistent wird.

**Mögliche Folge:**

```text
Spawn 1 funktioniert.
Spawn 2 direkt danach schlägt fehl, weil active markers weg sind.
User denkt: Marker-System / Spawn-System kaputt.
```

**Risiko:** mittel bis hoch

**Minimaler Fix-Vorschlag:**

- Entweder active markers nicht automatisch nach Spawn löschen,
- oder nur mit expliziter Config,
- oder nur Runtime löschen, aber nicht persistieren,
- oder pro Spawn klar anzeigen und optional `--consume-markers` verlangen.

**Safety-Regel ergänzen:**

```text
Spawn darf Marker-Staging nicht still persistent löschen, außer dies ist explizit vom User gewollt.
```

---

### Fehler 2 — MarkerSet hat keinen echten Rollback bei Save-Failure

**Betroffene Datei:**  
`src/main/java/keystone/npc/commands/marker/MarkerSetCommand.java`

**Betroffene Stelle:**

```text
markerRegistry.setActive(...)
scheduler.assignMarkerToNpc(...)
plugin.saveStateSafely()
```

**Statusfluss:**

```text
Marker wird runtime gesetzt
optional NPC-Zuordnung wird runtime geändert
Save schlägt fehl
Command meldet nur Warnung
Runtime bleibt geändert
```

Durch den bereits gefundenen Save-Fehler in `JsonFileStateStore.save(...)` ist es noch schlimmer:

```text
Save schlägt intern fehl
saveStateSafely() glaubt Erfolg
User sieht Erfolg
Disk ist unverändert
Runtime ist verändert
```

**Warum kritisch?**

Der User kann Marker setzen und NPC-Zuordnungen ändern, aber nach Restart ist alles weg. Gleichzeitig können nachfolgende Runtime-Aktionen auf einem Marker laufen, der nie sicher persistiert wurde.

**Risiko:** hoch

**Minimaler Fix-Vorschlag:**

- Vor MarkerSet Snapshot von MarkerRegistry + NPC markerAssignments nehmen.
- Wenn Save fehlschlägt: Runtime rollback.
- Save-Fehler müssen überhaupt erst korrekt nach außen signalisiert werden.

---

### Fehler 3 — Fehlende `currentPosition` fällt effektiv auf `(0,0,0)` zurück

**Betroffene Dateien:**

- `src/main/java/keystone/npc/domain/NpcRecord.java`
- `src/main/java/keystone/npc/persistence/JsonFileStateStore.java`
- `src/main/java/keystone/npc/routine/NpcRoutineRunner.java`

**Beobachtung:**

`NpcRecord` setzt im Konstruktor:

```text
currentPosition = new Vec3(0, 0, 0)
```

`JsonFileStateStore.toNpcRecord(...)` setzt eine gespeicherte Position nur, wenn `currentPosition` im JSON vorhanden ist.

`spawnNpcEntity(...)` nutzt:

```text
Vec3 position = npc.currentPosition() != null ? npc.currentPosition() : new Vec3(0, 0, 0);
```

Da `NpcRecord.currentPosition` nie `null` sein darf, ist eine fehlende Position praktisch nicht von einer echten Position `(0,0,0)` unterscheidbar.

**Kritischer Statusfluss:**

```text
state.json enthält NPC ohne currentPosition
-> NpcRecord defaultet auf (0,0,0)
-> Restore/Respawn nutzt currentPosition
-> NPC kann am Weltursprung gespawnt werden
```

**Warum kritisch?**

Bei Auto-Respawn oder Force-Respawn kann ein NPC am Ursprung landen, obwohl das eigentlich ein kaputter persistierter Zustand ist.

Besonders gefährlich zusammen mit:

```text
Chunk-Gate prüft Markerposition
Spawn nutzt currentPosition
```

Dann kann das Chunk-Gate „okay“ sagen, aber der Spawn findet an einer anderen Position statt.

**Risiko:** hoch

**Minimaler Fix-Vorschlag:**

- Fehlende Position als unsicheren State behandeln.
- Nicht default `(0,0,0)` verwenden.
- Entweder `positionKnown` einführen oder `currentPosition` beim Restore nullable/quarantänefähig machen.
- Auto-Respawn blockieren, wenn Spawn-Position nicht sicher bekannt ist.

---

### Fehler 4 — `linkEntityRef(...)` ist ein öffentlicher Blind-Bind zu `ACTIVE`

**Betroffene Datei:**  
`src/main/java/keystone/npc/routine/NpcRoutineRunner.java`

**Betroffene Methode:**  
`linkEntityRef(String npcId, Ref<EntityStore> entityRef)`

**Ablauf:**

```text
npc.entityRef(entityRef)
npc.entityId(1)
npc.entityStatus(ACTIVE)
updatePersistedEntityIdentity(...)
```

**Warum kritisch?**

Die Methode prüft nicht hart:

- passt die UUID?
- passt die Engine-Role?
- gehört die Entity einem anderen NPC?
- ist die Entity schon claimed?
- ist die Entity überhaupt ein NPC der erwarteten Rolle?

Ich habe im aktuellen Code keinen aktiven In-Tree-Caller gefunden. Trotzdem ist die Methode öffentlich und gefährlich, weil spätere Commands/Agent-Fixes sie leicht verwenden könnten.

**Risiko:** mittel aktuell, hoch bei späterer Nutzung

**Minimaler Fix-Vorschlag:**

- Methode `private` machen, falls ungenutzt.
- Oder in `safeLinkEntityRef(...)` umbauen mit UUID-/Role-/Ownership-Gate.
- Nie blind `ACTIVE` setzen.

**Safety-Regel:**

```text
ACTIVE darf nur nach UUID-Hard-Match, sicherem Anchor-Relink oder kontrolliertem Respawn gesetzt werden.
```

---

### Fehler 5 — Anchor-Relink kann UUID-lose Entity akzeptieren und alte UUID behalten

**Betroffene Dateien:**

- `src/main/java/keystone/npc/relink/RelinkWorkflowService.java`
- `src/main/java/keystone/npc/routine/entity/EntitySyncService.java`

**Ablauf im Anchor-Relink:**

```text
candidateUuid = readEntityUuid(candidateRef)
ownerByUuid(candidateUuid, claimedEntityUuids)
wenn candidateUuid null -> ownerByUuid gibt null
candidate wird akzeptiert
npc.entityStatus(ACTIVE)
entitySync.updatePersistedEntityIdentity(npc, keepRef)
```

`EntitySyncService.updatePersistedEntityIdentity(...)` macht bei fehlender UUID-Komponente:

```text
log ENTITY_UUID_MISSING
alte persisted UUID bleibt erhalten
```

**Kritischer Statusfluss:**

```text
NPC hat alte UUID A
Anchor-Relink findet Entity ohne lesbare UUID
-> NPC wird ACTIVE
-> entityRef zeigt auf neue/andere Entity
-> entityUuid bleibt A
-> state.json speichert ACTIVE + alte UUID A
```

**Warum kritisch?**

Beim nächsten Restart versucht UUID-Relink wieder gegen A. Gleichzeitig zeigte die vorige Runtime auf eine andere Entity. Das erzeugt instabile Identity:

```text
ACTIVE runtime truth != persisted UUID truth
```

**Risiko:** hoch

**Minimaler Fix-Vorschlag:**

Anchor-Relink darf nur `ACTIVE` setzen, wenn:

```text
candidate UUID lesbar
candidate UUID nicht von anderem NPC claimed
persistierte UUID entweder passt oder bewusst ersetzt werden darf
```

Wenn UUID nicht lesbar:

```text
kein ACTIVE
kein persistiertes UUID-Behalten
Status NEEDS_RELINK/MISSING_ENTITY
```

---

### Fehler 6 — UUID-Ownership ist nicht canonicalisiert

**Betroffene Datei:**  
`src/main/java/keystone/npc/relink/RelinkWorkflowService.java`

**Betroffene Methode:**  
`ownerByUuid(...)`

Aktuell:

```text
return claimedEntityUuids.get(candidateUuid)
```

**Problem:**

An anderen Stellen wird UUID case-insensitive verglichen. Die Ownership-Map selbst nutzt aber offenbar Raw-Strings. Wenn `state.json` oder eine Engine-Quelle Groß-/Kleinschreibung unterschiedlich liefert:

```text
Persisted: ABCD...
Live: abcd...
```

Dann kann `ownerByUuid(...)` keinen Claim finden.

**Folge:**

Eine eigentlich geclaimte Entity kann als unclaimed wirken. Das betrifft:

- Anchor-Relink
- Dedupe
- Cleanup-Orphans
- Dry-run / Force-Respawn-Diagnose

**Risiko:** hoch

**Minimaler Fix-Vorschlag:**

UUIDs canonicalisieren:

```text
load/save/buildOwnershipSnapshot/readEntityUuid -> lowercase UUID string
ownerByUuid(...) -> lookup mit lowercase(candidateUuid)
```

---

### Fehler 7 — Restore-/Validation-Pfade mutieren Marker und `setup()` speichert sofort

**Betroffene Dateien:**

- `src/main/java/keystone/npc/routine/NpcRoutineRunner.java`
- `src/main/java/keystone/npc/routine/marker/MarkerResolver.java`
- `src/main/java/keystone/npc/recovery/RespawnRecoveryService.java`
- `src/main/java/keystone/npc/KeystoneNpcPlugin.java`

**Problem:**

`resolveRequiredMarkerWithFallback(...)` mutiert Marker-Zuweisungen:

```text
setMarkerIdForType(npc, markerType, newMarkerId)
```

Diese Methode wird aber auch in scheinbaren Check-/Restore-/Validation-Pfaden aufgerufen, z. B.:

```text
reconcilePersistedMarkerAssignments(...)
staleReasonForRestore(...)
hasRequiredMarkers(...)
missingRequiredMarkers(...)
resolveDesiredTarget(...)
```

Danach macht `KeystoneNpcPlugin.setup()` direkt:

```text
saveState()
```

**Kritischer Statusfluss:**

```text
state.json lädt NPC mit Marker A
Marker A fehlt temporär
Restore-Reconcile sucht Fallback
Marker B wird zugewiesen
setup() speichert sofort
state.json verliert Marker A dauerhaft
```

**Warum kritisch?**

Ein Startvorgang wird dadurch zu einem Auto-Migrations-/Auto-Rewrite-Pfad, ohne Quarantäne und ohne User-Bestätigung.

**Risiko:** hoch

**Minimaler Fix-Vorschlag:**

- Read-only Resolver für Checks.
- Mutierender Resolver nur in expliziten Recovery-/Spawn-/Admin-Pfaden.
- Kein automatischer Save direkt nach Restore-Mutation, wenn es eine Fallback-Reparatur war.

---

### Fehler 8 — Spawn-Rollback sichert nur Identity, nicht Marker-Fallback-Mutationen

**Betroffene Datei:**  
`src/main/java/keystone/npc/routine/NpcRoutineRunner.java`

**Beobachtung:**

`spawnNpcEntity(...)` führt vor dem eigentlichen Spawn mehrere Schritte aus, die Marker verändern können:

```text
reconcilePersistedMarkerAssignments(npc)
validateMarkerRequirements(...)
resolveRequiredMarkerWithFallback(...)
```

Der Snapshot für Rollback enthält aber nur:

```text
oldEntityUuid
oldEntityRef
oldEntityId
oldEntityStatus
```

**Kritischer Ablauf:**

```text
Spawn-Versuch
-> Marker-Fallback verändert markerAssignment
-> Spawn schlägt später fehl
-> Identity wird zurückgerollt
-> MarkerAssignment bleibt verändert
```

**Warum kritisch?**

Ein fehlgeschlagener Spawn darf keine dauerhafte Marker-Migration verursachen.

**Risiko:** hoch

**Minimaler Fix-Vorschlag:**

- Spawn-Precheck darf Marker nicht mutieren.
- Oder Rollback-Snapshot muss MarkerAssignments/currentPosition ebenfalls sichern.
- Besser: alle Checks read-only, erst nach erfolgreichem Spawn mutieren.

---

### Fehler 9 — Transiente Routine-/WorldTime-/Config-Fehler können dauerhaft `PAUSED_MISSING_MARKER` speichern

**Betroffene Dateien:**

- `src/main/java/keystone/npc/routine/state/NpcTickPipeline.java`
- `src/main/java/keystone/npc/routine/state/StateTargetingService.java`
- `src/main/java/keystone/npc/persistence/JsonFileStateStore.java`

**Ablauf:**

`NpcTickPipeline` setzt bei mehreren transienten Fehlern:

```text
npc.state(PAUSED_MISSING_MARKER)
```

Beispiele:

- RoleDefinition fehlt
- RequiredMarker check schlägt fehl
- DesiredTarget ist null
- Routine fehlt
- keine aktive Routine-Zeit
- WorldTimeResource/Time Query wirft Exception

`JsonFileStateStore.toPersistedNpc(...)` speichert später:

```text
state = npc.state().name()
```

**Kritischer Statusfluss:**

```text
kurzzeitiger Routine-/WorldTime-/Config-Fehler
-> npc.state = PAUSED_MISSING_MARKER
-> Shutdown save
-> state.json speichert PAUSED_MISSING_MARKER
-> Restart startet aus Pause-State
```

**Warum kritisch?**

Ein runtime-temporärer Fehler wird zu persistiertem Zustand.

**Risiko:** hoch

**Minimaler Fix-Vorschlag:**

- Runtime-Pause-State nicht autoritativ speichern.
- Persistenten logical state getrennt führen.
- Beim Save transient states normalisieren, z. B.:

```text
WALKING_* -> Ziel-/Idle-State
PAUSED_MISSING_MARKER -> letzter stabiler logical state oder IDLE
```

---

### Fehler 10 — Initial-Respawn-Once-Flag wird vor echter World-Bereitschaft gesetzt

**Betroffene Datei:**  
`src/main/java/keystone/npc/KeystoneNpcPlugin.java`

**Ablauf:**

```text
start()
-> queueInitialRespawnIfNeeded("plugin-start")
-> initialRespawnQueued = true
-> scheduler.spawnRestoredNpcs(...)
```

Spätere Events:

```text
AllWorldsLoadedEvent
AllNPCsLoadedEvent
```

werden übersprungen, weil `initialRespawnQueued` schon true ist.

**Kritischer Statusfluss:**

```text
plugin-start läuft zu früh
Worlds/NPC stores noch nicht bereit
spawnRestoredNpcs findet nichts / kann nichts tun
initialRespawnQueued bleibt true
all-worlds-loaded-event wird ignoriert
```

Tick-Retry kann später teilweise retten, aber die expliziten Ready-Events sind damit logisch entwertet.

**Risiko:** mittel bis hoch

**Minimaler Fix-Vorschlag:**

- `plugin-start` nicht als Once-final behandeln.
- Flag erst setzen, wenn Worlds wirklich verfügbar sind.
- Oder getrennte Flags:

```text
pluginStartAttempted
worldsLoadedAttempted
npcsLoadedAttempted
```

---

### Fehler 11 — `tickStore(...)` triggert globales Restore/Respawn pro Store-Tick

**Betroffene Datei:**  
`src/main/java/keystone/npc/routine/NpcRoutineRunner.java`

**Ablauf:**

```text
public void tickStore(Store<EntityStore> store) {
    spawnRestoredNpcs("tick-retry");
    ...
}
```

`spawnRestoredNpcs(...)` läuft global über alle NPCs, nicht nur über den übergebenen Store.

**Warum kritisch?**

Wenn mehrere Stores/Worlds pro Server-Tick laufen:

```text
Store A tick -> global spawnRestoredNpcs
Store B tick -> global spawnRestoredNpcs
Store C tick -> global spawnRestoredNpcs
```

Das kann Retry-Counts, Log-Cooldowns, Auto-Respawn-Backoff und Relink-Schleifen unnötig mehrfach triggern.

**Risiko:** mittel bis hoch

**Minimaler Fix-Vorschlag:**

- Globalen Restore/Respawn-Pass entkoppeln von per-store tick.
- Pro Tick höchstens einmal global ausführen.
- Oder `spawnRestoredNpcsForWorld(storeWorld)` statt global.

---

### Fehler 12 — Admin-Commands speichern mit `saveState()` ohne Ergebnisprüfung

**Betroffene Dateien:**

- `src/main/java/keystone/npc/commands/admin/NpcRemoveCommand.java`
- `src/main/java/keystone/npc/commands/admin/NpcClearCommand.java`

**Ablauf:**

```text
scheduler.removeNpcByIndex(...)
plugin.saveState()
context.sendMessage("Removed NPC...")
```

und:

```text
scheduler.clearNpcs()
plugin.saveState()
context.sendMessage("Cleared NPCs...")
```

**Warum kritisch?**

`saveState()` gibt nichts zurück und ignoriert intern das Ergebnis von `saveStateSafely()`.

Zusammen mit verschluckten Save-Fehlern bedeutet das:

```text
Remove/Clear wird runtime durchgeführt
Save scheitert
Command meldet Erfolg
Restart bringt alte Daten zurück oder erzeugt Orphans
```

**Risiko:** hoch

**Minimaler Fix-Vorschlag:**

- Admin-Commands müssen `saveStateSafely()` direkt prüfen.
- Bei Save-Failure muss Runtime-Rollback oder klarer „unsicherer Zustand“-Fehler erfolgen.
- Für Clear: Dry-run/Force/Confirm und Save-Gate.

---

### Fehler 13 — `EntitySyncService.readPosition(...)` akzeptiert nicht-finite Live-Positionen

**Betroffene Datei:**  
`src/main/java/keystone/npc/routine/entity/EntitySyncService.java`

**Ablauf:**

```text
Vector3d position = transform.getPosition();
return new Vec3(position.x(), position.y(), position.z());
```

Keine Prüfung auf:

```text
Double.isFinite(x/y/z)
```

**Warum kritisch?**

Wenn Engine/Transform temporär `NaN` oder `Infinity` liefert, kann das in `npc.currentPosition` landen und später persistiert werden.

Folgen:

- JSON enthält NaN/Infinity-ähnliche Werte oder fehlerhafte Zahlen.
- Chunk-Gate / distanceSq / Marker-Fallback kann kaputtgehen.
- `state.json` kann beim nächsten Load brechen.
- Spawn-/Relink-Positionen werden unbrauchbar.

**Risiko:** mittel bis hoch

**Minimaler Fix-Vorschlag:**

- Positionen aus Entity immer finite-checken.
- Ungültige Live-Position niemals in `NpcRecord` übernehmen.
- Log einmalig/cooldown.

---

### Fehler 14 — Gültige `entityRef` wird zu stark als Wahrheit behandelt

**Betroffene Datei:**  
`src/main/java/keystone/npc/routine/NpcRoutineRunner.java`

**Betroffene Pfade:**

```text
hasLiveEntity(...)
spawnRestoredNpcs(...)
spawnNpcEntity(...)
passesLiveEntityGate(...)
```

**Problem:**

Eine gültige `entityRef` beweist nur:

```text
Es gibt eine gültige Ref.
```

Sie beweist nicht:

```text
diese Ref gehört zu diesem npcId
UUID passt
Role passt
Entity ist nicht von anderem Record claimed
```

**Kritischer Statusfluss:**

```text
npc.entityRef ist gültig, aber zeigt auf falsche Entity
-> hasLiveEntity true
-> entityStatus ACTIVE
-> updatePersistedEntityIdentity ersetzt ggf. UUID
-> state.json bindet NPC an falsche Entity
```

**Risiko:** hoch

**Minimaler Fix-Vorschlag:**

`hasLiveEntity(...)` in zwei Ebenen trennen:

```text
hasValidRefOnly(...)
hasOwnedLiveEntity(...)
```

Statuswechsel zu `ACTIVE` nur mit:

```text
valid ref + UUID/Ownership/Role match
```

---

### Fehler 15 — `currentPosition` kann durch Restore-Normalisierung still überschrieben werden

**Betroffene Datei:**  
`src/main/java/keystone/npc/routine/marker/IdleMarkerService.java`

**Ablauf:**

Beim Restore kann `normalizeRestorePosition(...)` aus Marker-Ankern eine neue Position setzen:

```text
currentPosition -> anchor position
```

Weil `setup()` danach sofort speichert, wird diese Normalisierung persistent.

**Warum kritisch?**

Wenn Marker-Fallback vorher falsch war, wird nicht nur die Marker-Zuordnung falsch, sondern auch die persistierte NPC-Position.

**Risiko:** hoch bei falschem Marker-Fallback, mittel sonst

**Minimaler Fix-Vorschlag:**

- Restore-Normalisierung nicht sofort speichern.
- Oder nur speichern, wenn Marker-Zuordnung hard-valid war.
- Fallback-basierte Positionen müssen als unsichere Recovery gelten, nicht als autoritativer State.

---

## 3. Kritische Simulationsfälle

---

### Fall A — Spawn löscht aktive Marker

```text
1. /knpc marker set bed
2. /knpc marker set work
3. /knpc spawn lumberjack Bob
4. Spawn erfolgreich
5. clearActive()
6. state.json activeMarkerIds leer
7. /knpc spawn lumberjack Alice
8. Fehler: required active marker fehlt
```

**Ergebnis:** Marker-Staging wird gecancelt.

---

### Fall B — Fehlende Position spawnt an Origin

```text
1. state.json NPC ohne currentPosition
2. Load erzeugt NpcRecord mit default (0,0,0)
3. Entity fehlt
4. Auto-/Force-Respawn erlaubt
5. spawnNpcEntity nutzt currentPosition
6. NPC spawnt bei (0,0,0)
```

**Ergebnis:** NPC landet falsch, obwohl eigentlich Restore blockieren müsste.

---

### Fall C — UUID-lose Anchor-Entity wird ACTIVE

```text
1. Persistierter NPC hat alte entityUuid A
2. Live-Entity nahe Anchor hat keine lesbare UUIDComponent
3. Anchor-Relink findet genau einen Kandidaten
4. ownerByUuid(null) -> null
5. Candidate wird accepted
6. updatePersistedEntityIdentity kann UUID nicht lesen
7. Alte UUID A bleibt
8. Status ACTIVE
```

**Ergebnis:** Runtime-Entity und persistierte UUID widersprechen sich.

---

### Fall D — Kurzzeitiger Routine-Fehler wird dauerhaft gespeichert

```text
1. NPC ist ACTIVE
2. WorldTimeResource query wirft temporär
3. resolveDesiredTarget catch
4. state = PAUSED_MISSING_MARKER
5. Server shutdown
6. saveState speichert PAUSED_MISSING_MARKER
7. Restart startet mit Pause-State
```

**Ergebnis:** Temporärer Fehler wird persistenter Zustand.

---

### Fall E — Initial Restore läuft zu früh und Ready-Event wird blockiert

```text
1. start()
2. queueInitialRespawnIfNeeded("plugin-start")
3. initialRespawnQueued = true
4. Worlds noch nicht bereit
5. spawnRestoredNpcs kann nichts tun
6. AllWorldsLoadedEvent feuert
7. skipped wegen initialRespawnQueued
```

**Ergebnis:** Der eigentlich richtige Ready-Trigger wird weg gecancelt.

---

## 4. Patch-Priorität nach dieser Runde

### P0 — Save/Load- und Save-Erfolg zuerst reparieren

Ohne das sind alle anderen Fixes unsicher.

**Warum?**

Jede Runtime-Korrektur kann wieder verloren gehen, wenn Save-Fehler als Erfolg gelten.

---

### P1 — `ACTIVE`-Bindung härten

Betroffene Themen:

```text
hasLiveEntity
linkEntityRef
Anchor-Relink
UUID-lose Entities
UUID canonicalization
```

Ziel:

```text
ACTIVE nur bei harter Ownership-/UUID-/Role-Sicherheit.
```

---

### P2 — Position-Safety

Betroffene Themen:

```text
currentPosition default (0,0,0)
finite checks
spawn position == chunk-gate position
restore normalization
```

Ziel:

```text
Keine Origin-Spawns durch fehlende/kaputte Persistenz.
```

---

### P3 — Marker-Mutation und Active-Marker-Staging

Betroffene Themen:

```text
clearActive nach Spawn
resolveRequiredMarkerWithFallback mutiert in Checks
MarkerSet rollback
restore auto-save
```

Ziel:

```text
Kein stilles Löschen oder Umschreiben von Marker-Kontext.
```

---

### P4 — Runtime-State-Persistenz stoppen

Betroffene Themen:

```text
PAUSED_MISSING_MARKER
WALKING_*
transiente Routine-/WorldTime-Fehler
```

Ziel:

```text
state.json speichert nur autoritative Restart-Zustände.
```

---

## 5. Minimal betroffene Dateien

Wahrscheinlich minimal betroffen:

```text
src/main/java/keystone/npc/persistence/JsonFileStateStore.java
src/main/java/keystone/npc/KeystoneNpcPlugin.java
src/main/java/keystone/npc/routine/NpcRoutineRunner.java
src/main/java/keystone/npc/routine/entity/EntitySyncService.java
src/main/java/keystone/npc/relink/RelinkWorkflowService.java
src/main/java/keystone/npc/routine/marker/MarkerResolver.java
src/main/java/keystone/npc/routine/marker/IdleMarkerService.java
src/main/java/keystone/npc/routine/state/NpcTickPipeline.java
src/main/java/keystone/npc/routine/state/StateTargetingService.java
src/main/java/keystone/npc/commands/spawn/SpawnNpcCommand.java
src/main/java/keystone/npc/commands/marker/MarkerSetCommand.java
src/main/java/keystone/npc/commands/admin/NpcRemoveCommand.java
src/main/java/keystone/npc/commands/admin/NpcClearCommand.java
```

---

## 6. Dateien / Systeme, die nicht unnötig geändert werden sollten

```text
Server/NPC/Roles/*
Door-System
Pathfinding-System
Animation-System
Dedupe-System, außer UUID/Ownership direkt betroffen
JSON-Role-Struktur, außer Safety-Datei verlangt es ausdrücklich
setRoleName / Engine-Role-Architektur
```

Weiterhin verboten:

```text
Kein setRoleName("KeystoneNPC_...")
Kein blindes Relinken per gleicher Role
Kein Auto-Respawn bei AMBIGUOUS
Keine dynamischen per-NPC Engine-Roles
Keine aggressive Dedupe-Löschung
```

---

## 7. Neue Safety-Regeln

### ACTIVE-Bindung

```text
Gültige entityRef alleine darf keinen ACTIVE-Status beweisen.
ACTIVE verlangt UUID-/Ownership-/Role-Sicherheit.
```

### Position-Safety

```text
Fehlende Position darf nicht zu (0,0,0) defaulten.
Spawn-Position und Chunk-Gate-Position müssen identisch sein.
Nicht-finite Positionen dürfen nie persistiert oder für Spawn/Relink genutzt werden.
```

### Marker-Staging

```text
Spawn darf active marker context nicht still löschen und persistieren.
MarkerSet muss bei Save-Failure rollbackfähig sein.
```

### Runtime-State

```text
Transiente Fehlerzustände wie PAUSED_MISSING_MARKER dürfen nicht autoritativ in state.json gespeichert werden.
```

### Initial-Restore

```text
Initial-Respawn darf nicht durch einen zu frühen plugin-start Versuch dauerhaft als erledigt gelten.
```

---

## 8. Test-Gates

Pflicht nach Fixes:

```bash
mvn -q -DskipTests test-compile
```

In meiner Analyseumgebung konnte ich Maven nicht ausführen:

```text
bash: mvn: command not found
```

Zusätzliche Tests:

```text
[ ] Spawn 2 NPCs nacheinander mit denselben active markers
[ ] Prüfen: active markers werden nicht unerwartet persistent gelöscht
[ ] MarkerSet mit absichtlich fehlschlagendem Save
[ ] Prüfen: Runtime rollback oder klare Fehlermeldung
[ ] state.json ohne currentPosition
[ ] Prüfen: kein Spawn bei (0,0,0)
[ ] Anchor-Relink mit Entity ohne UUIDComponent
[ ] Prüfen: kein ACTIVE mit alter UUID
[ ] UUID in state.json uppercase, Live UUID lowercase
[ ] Prüfen: Ownership Claim funktioniert trotzdem
[ ] WorldTimeResource temporär null/fehlerhaft
[ ] Prüfen: kein persistiertes PAUSED_MISSING_MARKER
[ ] plugin-start vor World-ready simulieren
[ ] Prüfen: AllWorldsLoadedEvent darf Restore trotzdem ausführen
[ ] Mehrere stores/worlds
[ ] Prüfen: spawnRestoredNpcs läuft nicht mehrfach global pro Tick
[ ] Live entityRef gültig, aber falsche Role/UUID
[ ] Prüfen: Status wird nicht ACTIVE
```

---

## 9. Kurzfazit

Diese Runde zeigt: Neben den bereits gefundenen Restart-/Relink-/Respawn-Fehlern gibt es noch einen zweiten großen Risikoblock:

```text
Das System verwechselt Runtime-Bequemlichkeit mit persistenter Wahrheit.
```

Kritische Beispiele:

```text
gültige entityRef => ACTIVE
fehlende Position => (0,0,0)
temporary PAUSED => state.json
fallback marker => echte assignment
spawn success => active markers gelöscht
```

Die wichtigste technische Leitregel für die nächsten Fixes:

```text
Nichts darf dauerhaft in state.json landen, nur weil ein Runtime-Fallback, ein temporärer Tick-Fehler oder ein unsicherer Repair-Pfad kurz funktioniert hat.
```
