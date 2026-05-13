# Destructive Logic Errors — Respawn / Relink / State / Marker Control

Projekt: **Hytale-Mod „NPCMod / KeystoneNPC“**  
Modus: **PLAN-/ANALYSE-MODE**  
Scope: **Nur Analyse. Keine Implementierung. Keine Dateiänderungen an der Codebase.**

Untersuchte ZIP: `NPCMod.zip`

---

## 1. Gesamturteil

**FAIL — Es gibt weitere destruktive Logikfehler.**

Diese Runde fokussiert nicht auf normale Feature-Bugs, sondern auf Fehler, die Daten zerstören, NPCs verlieren, Entities verwaisen lassen, Marker-Zuordnungen überschreiben, State-Dateien leeren, Runtime-Zustände dauerhaft speichern oder Recovery-Pfade still abbrechen können.

Die kritischsten neuen Befunde:

1. `JsonFileStateStore.save(...)` verschluckt Save-Fehler, dadurch glaubt der Plugin-Code fälschlich, der Save sei erfolgreich.
2. `saveStateSafely()` kann deshalb `true` zurückgeben, obwohl nichts gespeichert wurde.
3. Dirty-State kann nach fehlgeschlagenem Save gelöscht werden.
4. Spawn-Rollback kann nicht greifen, wenn Save-Fehler verschluckt werden.
5. `removeNpc(...)` löscht den NPC-Record, bevor die Live-Entity sicher entfernt wurde.
6. `removeLiveEntity(...)` löscht Entity-Identity vor erfolgreicher Entity-Entfernung.
7. Marker-Clear löscht alle Marker runtime-seitig ohne sofortigen sicheren Save und ohne NPC-Zuordnungen zu prüfen.
8. Marker-Resolver kann Marker-Zuweisungen in eigentlich lesenden Validierungs-/Tick-Pfaden überschreiben.
9. Ein einzelner kaputter Marker-Type / NPC-State / Positionseintrag kann beim Laden den gesamten State auf leer fallbacken.
10. Danach kann `setup()` den leeren State direkt zurückschreiben.
11. Runtime-States wie Walking/Paused werden weiterhin in `state.json` gespeichert.
12. Config-/Definition-Fehler können persistierte NPCs auf `DISABLED` setzen und direkt speichern.
13. Orphan-Cleanup kann nach State-Load-Fehlern echte persistierte Entities als Orphans löschen.
14. Auto-/Manual-Dry-Run kann bei `RelinkOutcome.PENDING` falsche „would spawn“-Signale liefern.
15. Live-Entity-Gate vertraut gültiger `Ref`, ohne harte UUID-/Role-Ownership-Prüfung.
16. `updatePersistedEntityIdentity(...)` kann bei fehlender Entity-UUID alte UUIDs behalten und damit falsche Identity persistieren.
17. Duplicate `npcId` in `state.json` wird beim Restore still überschrieben.
18. `queueInitialRespawnIfNeeded(...)` setzt den Once-Flag vor sicherem Erfolg.
19. Cleanup-/Clear-Commands haben zu wenig Sicherheitsbremsen gegen versehentliches Mass-Löschen.
20. Index-basierte Remove-Commands können bei veränderter Liste den falschen NPC löschen.

---

## 2. Destructive Error Register

### Fehler 1 — Save-Fehler werden verschluckt, Caller glaubt an Erfolg

**Betroffene Datei:** `JsonFileStateStore.java`  
**Betroffene Methode:** `save(...)`  
**Risiko:** **hoch**

#### Aktueller Ablauf

`JsonFileStateStore.save(...)` führt den Schreibvorgang aus:

```text
Files.writeString(..., CREATE, TRUNCATE_EXISTING, WRITE)
```

Wenn dabei `IOException`, `RuntimeException` oder `LinkageError` auftritt, wird nur geloggt. Der Fehler wird nicht nach außen weitergereicht. Die Methode hat `void` und signalisiert dem Caller keinen Fehlschlag.

`KeystoneNpcPlugin.saveStateSafely()` ruft diese Methode auf und gibt `true` zurück, solange keine Exception aus `save(...)` herauskommt.

#### Warum destruktiv?

Dadurch entstehen falsche Erfolgssignale:

```text
Save scheitert
-> JsonFileStateStore.save(...) fängt Fehler
-> saveStateSafely() glaubt: Erfolg
-> Command / Scheduler / Dirty-Flush glaubt: Daten sind sicher gespeichert
```

Folgen:

- Spawn-Rollback wird nicht ausgeführt.
- Dirty-Flag kann gelöscht werden.
- Commands melden Erfolg, obwohl nichts persistiert ist.
- Runtime-Änderungen gehen beim Restart verloren.
- Live-Entities können existieren, ohne dass sie in `state.json` gespeichert wurden.

#### Minimaler Fix-Vorschlag

`JsonFileStateStore.save(...)` muss einen Fehler klar signalisieren:

Option A:

```text
save(...) wirft Exception weiter
```

Option B:

```text
save(...) gibt boolean zurück
```

Danach darf `saveStateSafely()` nur `true` liefern, wenn wirklich geschrieben wurde.

#### Safety-Regel

Neue Safety-Regel nötig:

```text
Ein Save darf nie still scheitern. Jeder persistenzrelevante Caller muss Save-Failure erkennen können.
```

---

### Fehler 2 — Dirty-State kann nach fehlgeschlagenem Save gelöscht werden

**Betroffene Datei:** `NpcRoutineRunner.java`  
**Betroffene Methode:** `flushDirtyStateIfDue(...)`  
**Risiko:** **hoch**

#### Aktueller Ablauf

`flushDirtyStateIfDue(...)` ruft den Save-Callback auf. Wenn dieser `true` liefert, wird `stateDirty = false` gesetzt.

Da `saveStateSafely()` wegen Fehler 1 auch bei internem Save-Failure `true` liefern kann, passiert:

```text
stateDirty = true
Save scheitert intern
saveStateSafely() liefert trotzdem true
stateDirty = false
```

#### Warum destruktiv?

Der Scheduler glaubt, der Zustand sei auf Disk. Tatsächlich ist er es nicht. Danach wird kein weiterer Save erzwungen.

Mögliche verlorene Daten:

- neue `entityUuid` nach Respawn
- neuer `entityStatus`
- neue Marker-Zuordnung
- neue Position
- neu gespawnter NPC
- `MISSING_ENTITY` / `ACTIVE` Statuswechsel

#### Minimaler Fix-Vorschlag

Zuerst Fehler 1 beheben. Danach:

```text
Dirty nur löschen, wenn Save nachweislich erfolgreich war.
```

Zusätzlich: Save-Fehler mit Warnung im Command/Log sichtbar machen.

---

### Fehler 3 — Spawn kann Entity erzeugen, aber Persistenz-Rollback greift nicht

**Betroffene Datei:** `SpawnNpcCommand.java`  
**Betroffene Methode:** `execute(...)`  
**Risiko:** **hoch**

#### Aktueller Ablauf

Nach erfolgreichem Spawn:

```text
scheduler.spawnNpc(...)
markerRegistry.clearActive()
plugin.saveStateSafely()
wenn false -> scheduler.removeNpc(npcId)
```

Wegen Fehler 1 kann `saveStateSafely()` aber `true` zurückgeben, obwohl der Save fehlgeschlagen ist.

#### Destruktiver Ablauf

```text
NPC wird live gespawnt
entityUuid existiert runtime-seitig
saveStateSafely() scheitert intern, gibt aber true zurück
kein Rollback
Server restartet
state.json kennt diesen NPC nicht
Live-Entity kann als Orphan übrig bleiben oder später dupliziert werden
```

#### Minimaler Fix-Vorschlag

- Save-Fehler müssen sichtbar werden.
- Spawn-Command darf erst Erfolg melden, wenn Persistenz sicher ist.
- Bei Save-Failure muss Rollback garantiert sein oder der NPC muss als unsicher markiert werden.

---

### Fehler 4 — `removeNpc(...)` löscht Record vor sicherer Entity-Entfernung

**Betroffene Datei:** `NpcRoutineRunner.java`  
**Betroffene Methode:** `removeNpc(...)`  
**Risiko:** **hoch**

#### Aktueller Ablauf

```text
npc = npcs.remove(npcId)
Runtime Maps löschen
removeLiveEntity(npc)
markDirty()
return true
```

#### Warum destruktiv?

Der persistierte Record wird runtime-seitig gelöscht, bevor klar ist, ob die Live-Entity entfernt werden konnte.

Wenn `removeLiveEntity(...)` fehlschlägt:

```text
Record weg
state.json wird später ohne NPC gespeichert
Live-Entity bleibt in Welt
Entity ist jetzt Orphan
```

Fehlerfälle:

- `worldAccessor` ist null
- World nicht gefunden
- `entityRef` null/invalid
- queued world removal läuft nicht
- Exception im async `world.execute(...)`
- Entity existiert, aber gespeicherte `entityRef` ist leer

#### Minimaler Fix-Vorschlag

Für sichere Remove-Architektur:

```text
1. Remove-Operation vorbereiten
2. Live-Entity entfernen oder Removal-Job sicher registrieren
3. Erst nach Erfolg Record löschen
4. Wenn Entity nicht entfernt werden kann: Status MISSING_ENTITY/NEEDS_CLEANUP statt Record löschen
```

Minimaler Patch:

```text
removeNpc darf Record nicht entfernen, wenn removeLiveEntity nicht mindestens queued oder bestätigt ist.
```

---

### Fehler 5 — `removeLiveEntity(...)` löscht Identity vor Entity-Removal

**Betroffene Datei:** `NpcRoutineRunner.java`  
**Betroffene Methode:** `removeLiveEntity(...)`  
**Risiko:** **hoch**

#### Aktueller Ablauf

`removeLiveEntity(...)` nimmt die aktuelle `entityRef`, ruft dann aber früh:

```text
clearEntityIdentity(npc)
```

Danach wird geprüft, ob die Live-Entity überhaupt entfernt werden kann.

#### Destruktiver Ablauf

```text
clearEntityIdentity()
World nicht gefunden
return
```

Ergebnis:

```text
NPC hat keine entityUuid/entityRef mehr
Live-Entity kann trotzdem noch existieren
späterer Relink ist schwerer oder unmöglich
```

#### Minimaler Fix-Vorschlag

Identity erst löschen, wenn Entfernung bestätigt oder mindestens sicher queued wurde.

Besser:

```text
oldEntityIdentity behalten
Removal versuchen
wenn Erfolg -> clear identity
wenn failure -> identity behalten + status NEEDS_CLEANUP/MISSING_ENTITY
```

---

### Fehler 6 — Marker-Clear löscht alle Marker ohne NPC-Zuordnungen zu prüfen

**Betroffene Datei:** `MarkerClearCommand.java`  
**Betroffene Methode:** `execute(...)`  
**Risiko:** **hoch**

#### Aktueller Ablauf

```text
markerRegistry.clear()
reply: "Cleared markers..."
```

Kein sofortiger sicherer Save. Keine Prüfung, ob NPCs diese Marker verwenden.

#### Warum destruktiv?

Wenn danach irgendein Save passiert:

```text
state.json markers = []
NPCs behalten aber markerAssignments mit alten markerIds
```

Folge:

- Marker-Registry leer
- NPCs referenzieren nicht mehr existierende Marker
- Routine/Spawn/Restore können kaputtgehen
- MarkerResolver kann später automatisch andere Marker zuweisen
- `state.json` wird inkonsistent

Wenn kein Save passiert:

```text
Runtime leer
Disk noch alt
Restart bringt alte Marker zurück
```

Das Command ist also in beide Richtungen gefährlich:

- entweder es zerstört persistierte Marker
- oder es lügt über den dauerhaften Zustand

#### Minimaler Fix-Vorschlag

`/knpc marker clear` braucht mindestens:

```text
- Warnung / force-Modus
- Prüfung: welche NPCs nutzen Marker?
- Option: nur active markers clearen
- nach echtem Clear: sicherer Save
- bei Save-Failure: Runtime rollback oder klare Fehlermeldung
```

---

### Fehler 7 — Marker-Resolver überschreibt Marker-Zuweisungen in Lese-/Validierungspfaden

**Betroffene Dateien:**

- `MarkerResolver.java`
- `NpcRoutineRunner.java`
- `RespawnRecoveryService.java`
- `StateTargetingService.java`

**Betroffene Methode:** `resolveRequiredMarkerWithFallback(...)`  
**Risiko:** **hoch**

#### Aktueller Ablauf

`resolveRequiredMarkerWithFallback(...)` klingt wie eine reine Auflösung. Tatsächlich schreibt sie:

```text
npc.setMarkerIdForType(markerType, fallback.markerId())
```

Diese Methode wird unter anderem aufgerufen bei:

- Restore-Reconcile
- Stale-Reason-Check
- Tick-Update
- Required-Marker-Check
- StateTargeting
- Respawn-Vorbereitung

#### Warum destruktiv?

Ein „read/check“-Pfad kann die persistierte Marker-Assignment heimlich ändern.

Beispiel:

```text
NPC hat workMarkerId = A
Marker A fehlt temporär oder Registry ist leer
Resolver findet irgendeinen Fallback B
NPC bekommt workMarkerId = B
später Save
state.json verliert A und speichert B
```

Das kann Marker-Zuordnung dauerhaft verfälschen.

#### Besonders gefährlich bei mehreren Lumberjacks

Wenn mehrere Lumberjacks nahe beieinander stehen, kann ein Fallback-Ring den falschen Marker ziehen. Danach wird dieser falsche Marker später als „gültig“ persistiert.

#### Minimaler Fix-Vorschlag

Aufteilen:

```text
resolveRequiredMarkerReadOnly(...)
assignFallbackMarkerExplicitly(...)
```

Validierung/Tick darf nicht mutieren. Fallback-Zuweisung nur in explizitem Recovery-/Admin-/Spawn-Pfad.

---

### Fehler 8 — Ein einzelner kaputter JSON-Eintrag kann den gesamten State leeren

**Betroffene Datei:** `JsonFileStateStore.java`  
**Betroffene Methoden:** `load(...)`, `toNpcRecord(...)`, `toMarkerRecord(...)`, `toVec3(...)`  
**Risiko:** **kritisch**

#### Problematische Stellen

- `NpcState.valueOf(npc.state())`
- `MarkerType.valueOf(marker.type())`
- `toVec3(...)` ohne Null-/Finite-Checks
- ungefangene Runtime-Fehler innerhalb einzelner Record-Konvertierung

Diese Fehler werden außen im großen `load()` gefangen. Danach:

```text
return PluginState.empty()
```

#### Destruktiver Ablauf

```text
state.json enthält 10 NPCs
1 NPC hat invaliden state string
load() bricht ab
PluginState.empty()
setup() ruft saveState()
state.json wird leer überschrieben
```

#### Minimaler Fix-Vorschlag

Load muss record-isoliert sein:

```text
- fehlerhaften Record überspringen oder quarantänen
- gute Records behalten
- niemals wegen eines Records komplett empty zurückgeben
- bei Load-Fehler nie automatisch überschreiben
```

Zusätzlich:

```text
state.json.bak oder state.json.corrupt.<timestamp>
```

---

### Fehler 9 — `setup()` speichert direkt nach Load und kann leeren Fallback persistieren

**Betroffene Datei:** `KeystoneNpcPlugin.java`  
**Betroffene Methode:** `setup()`  
**Risiko:** **kritisch**

#### Aktueller Ablauf

```text
loaded = stateStore.load()
markerRegistry.restore(...)
scheduler.restore(...)
saveState()
```

Wenn `load()` wegen Fehlern `PluginState.empty()` liefert, wird der leere Zustand direkt gespeichert.

#### Warum destruktiv?

Das ist die direkte Verstärkung von Fehler 8:

```text
kleiner JSON-Fehler
-> load empty
-> setup save
-> kompletter State dauerhaft gelöscht
```

#### Minimaler Fix-Vorschlag

Nach Load muss unterschieden werden:

```text
LOAD_OK
LOAD_EMPTY_FILE
LOAD_CORRUPT
LOAD_PARTIAL
LOAD_FAILED
```

Nur bei echtem OK darf direkt normalisiert gespeichert werden. Bei corrupt/failed:

```text
kein Save
Server warnen
Recovery/Dry-run verlangen
Backup behalten
```

---

### Fehler 10 — Runtime-States werden dauerhaft in `state.json` gespeichert

**Betroffene Datei:** `JsonFileStateStore.java`  
**Betroffene Methode:** `toPersistedNpc(...)`  
**Risiko:** **mittel bis hoch**

#### Aktueller Ablauf

Persistiert wird:

```text
state = npc.state().name()
```

Damit können gespeichert werden:

- `WALKING_TO_BED`
- `WALKING_TO_WORK`
- `PAUSED_MISSING_MARKER`
- andere transient/routine-nahe States

#### Warum destruktiv?

Nach Restart kann der NPC in einem temporären Zustand wiederhergestellt werden, der eigentlich nur runtime gilt.

Beispiele:

```text
NPC war gerade unterwegs
state.json speichert WALKING_TO_WORK
Restart
Restore löscht Navigation
State ist aber noch Walking
Targeting/Idle-Position kann falsche Marker wählen
```

Oder:

```text
Tick findet kurz Marker nicht
state = PAUSED_MISSING_MARKER
später Save
Restart
NPC bleibt paused, obwohl Marker wieder da wären
```

#### Minimaler Fix-Vorschlag

Persistence muss unterscheiden:

```text
persistentLogicalState
runtimeNavigationState
```

Minimal:

```text
Beim Save runtime/walking/paused states auf sicheren idle/logical state normalisieren.
```

Safety-Regel:

```text
Runtime-State darf nicht als autoritativer Restart-State gespeichert werden.
```

---

### Fehler 11 — Definition-/Config-Fehler können NPCs dauerhaft `DISABLED` speichern

**Betroffene Dateien:**

- `NpcRoutineRunner.java`
- `RespawnRecoveryService.java`
- `RoleDefinitionRegistry.java`
- `NpcDefinitionRegistry.java`

**Risiko:** **hoch**

#### Aktueller Ablauf

Beim Restore wird geprüft:

```text
staleReasonForRestore(npc)
wenn stale -> npc.entityStatus(DISABLED)
```

Danach speichert `setup()` den Zustand.

#### Warum destruktiv?

Wenn eine JSON-Definition temporär kaputt ist, z. B.:

- duplicate roleId
- duplicate hytaleRole
- missing required markers
- external override defekt
- resource file kaputt

dann können existierende NPCs dauerhaft auf `DISABLED` gesetzt und gespeichert werden.

Danach ist der Runtime-Fehler nicht mehr nur temporär, sondern persistiert in `state.json`.

#### Minimaler Fix-Vorschlag

Unterscheiden:

```text
DISABLED_BY_USER
RESTORE_BLOCKED_BY_INVALID_DEFINITION
```

Temporäre Config-/Definition-Fehler sollten nicht sofort als permanentes `DISABLED` gespeichert werden.

---

### Fehler 12 — Orphan-Cleanup kann nach State-Load-Fehlern echte Entities löschen

**Betroffene Datei:** `NpcRoutineRunner.java`  
**Betroffene Methode:** `cleanupOrphans(...)`  
**Risiko:** **kritisch**

#### Aktueller Ablauf

Cleanup prüft offene Relink-Records aus dem aktuellen in-memory `npcs`.

Wenn aber `load()` vorher auf `PluginState.empty()` gefallen ist:

```text
npcs = []
openRelinkRecords = 0
```

Dann kann Cleanup glauben, es gebe keine offenen Records.

#### Destruktiver Ablauf

```text
state.json konnte nicht geladen werden
npcs leer
alte Live-Entities stehen noch in Welt
cleanup orphans safe mode
openRelinkRecords = 0
Entities wirken orphan
Cleanup löscht sie
```

Das kann echte persistierte NPC-Entities zerstören.

#### Minimaler Fix-Vorschlag

Cleanup darf nicht laufen, wenn letzter State-Load nicht eindeutig erfolgreich war.

Neue Safety-Regel:

```text
Orphan-Cleanup ist verboten nach LOAD_FAILED / LOAD_CORRUPT / PARTIAL_RESTORE.
```

---

### Fehler 13 — `hasLiveEntity(...)` vertraut gültiger Ref ohne harte Ownership-Prüfung

**Betroffene Datei:** `NpcRoutineRunner.java`  
**Betroffene Methode:** `hasLiveEntity(...)`  
**Risiko:** **hoch**

#### Aktueller Ablauf

Wenn `entityRef` nicht null und gültig ist, gilt die Entity als live.

#### Warum destruktiv?

Eine gültige `Ref` beweist noch nicht:

- dass es dieselbe UUID ist
- dass es die passende Role ist
- dass sie nicht einem anderen NPC gehört
- dass sie nicht stale/falsch gecached wurde

Folgepfade setzen dann `ACTIVE` oder aktualisieren persistierte Identity.

#### Destruktiver Ablauf

```text
npc.entityRef zeigt auf gültige, aber falsche Entity
hasLiveEntity = true
spawnRestoredNpcs setzt ACTIVE
updatePersistedEntityIdentity ersetzt entityUuid
state.json bindet NPC an falsche Entity
```

#### Minimaler Fix-Vorschlag

`hasLiveEntity(...)` für Statusentscheidungen härten:

```text
valid ref
+ UUID match oder sichere Ownership
+ role match
```

Reine Ref-Gültigkeit darf nur „has some ref“ bedeuten, nicht „this NPC is live“.

---

### Fehler 14 — `updatePersistedEntityIdentity(...)` behält alte UUID, wenn Entity keine UUID liefert

**Betroffene Datei:** `EntitySyncService.java`  
**Betroffene Methode:** `updatePersistedEntityIdentity(...)`  
**Risiko:** **hoch**

#### Aktueller Ablauf

Wenn aus der Entity keine UUID gelesen werden kann, wird die alte persistierte UUID behalten.

#### Warum destruktiv?

Nach einem neuen Spawn oder Anchor-Relink kann die Entity-Identity dann inkonsistent werden:

```text
entityRef = neue Entity
entityUuid = alte UUID
status = ACTIVE
```

Beim nächsten Restart wird gegen die alte UUID gerelinkt. Das kann zu Missing, falschem Relink oder Duplikaten führen.

#### Minimaler Fix-Vorschlag

Bei erfolgreichem Spawn/Relink muss Entity-UUID hart lesbar sein.

Wenn nicht:

```text
nicht ACTIVE setzen
nicht alte UUID behalten
Status = NEEDS_RELINK oder MISSING_ENTITY
klarer Fehlerlog
```

---

### Fehler 15 — Duplicate `npcId` in `state.json` wird still überschrieben

**Betroffene Datei:** `NpcRoutineRunner.java`  
**Betroffene Methode:** `restore(...)`  
**Risiko:** **mittel bis hoch**

#### Aktueller Ablauf

```text
npcs.put(npc.npcId(), npc)
```

Wenn zwei Records dieselbe `npcId` haben, überschreibt der zweite den ersten still.

#### Warum destruktiv?

Ein persistierter NPC verschwindet ohne expliziten Fehler.

Zusätzlich kann die überschreibende Version andere Marker/UUID/Status haben.

#### Minimaler Fix-Vorschlag

Beim Restore:

```text
wenn npcId bereits existiert:
  beide Records blockieren/quarantänen
  nicht still überschreiben
  kein Auto-Save
```

---

### Fehler 16 — `queueInitialRespawnIfNeeded(...)` setzt Once-Flag vor sicherem Erfolg

**Betroffene Datei:** `KeystoneNpcPlugin.java`  
**Betroffene Methode:** `queueInitialRespawnIfNeeded(...)`  
**Risiko:** **mittel**

#### Aktueller Ablauf

```text
if initialRespawnQueued return
initialRespawnQueued = true
scheduler.spawnRestoredNpcs(trigger)
```

Wenn der erste Aufruf zu früh kommt oder intern nicht sinnvoll arbeiten kann, werden spätere Initial-Events blockiert.

#### Warum problematisch?

Beispiel:

```text
plugin-start kommt, Worlds noch nicht bereit
initialRespawnQueued = true
spawnRestoredNpcs kann wegen world null nichts tun
AllWorldsLoaded kommt später
wird übersprungen
```

Der Tick-Retry kann das teilweise auffangen, aber der Initial-Gate ist logisch fehlerhaft.

#### Minimaler Fix-Vorschlag

Flag erst setzen, wenn Initial-Restore/Respawn wirklich erfolgreich durchlaufen konnte oder bewusst in Tick-Retry übergeben wurde.

---

### Fehler 17 — Auto-Respawn kann endlos fehlschlagen und weiter versuchen

**Betroffene Datei:** `NpcRoutineRunner.java`  
**Betroffene Methode:** `spawnRestoredNpcs(...)`  
**Risiko:** **mittel bis hoch**

#### Aktueller Ablauf

Auto-Respawn erhöht `respawnFailureCounts` und setzt Backoff. Es gibt aber im Auto-Pfad keinen echten terminalen Max-Failure-Stop wie im `RespawnRecoveryService`.

#### Warum destruktiv?

Bei dauerhaft kaputtem Spawn-Grund:

- Role fehlt
- Marker falsch
- spawnEntity wirft
- World-Position ungültig
- Chunk-Gate falsch positiv

läuft Auto-Respawn weiter in regelmäßigen Abständen.

Folgen:

- Log-Spam
- wiederholte Identity-Snapshot-Manipulation
- Status-Churn
- mögliche Orphan-Risiken bei Teilfehlern

#### Minimaler Fix-Vorschlag

Auto-Respawn muss denselben Max-Failure-Mechanismus nutzen wie manuelle Recovery:

```text
nach N Fehlern:
  Status MISSING_ENTITY
  Auto-Respawn terminal blockieren
  klare manuelle Recovery nötig
```

---

### Fehler 18 — Chunk-Gate prüft eventuell andere Position als Spawn

**Betroffene Datei:** `NpcRoutineRunner.java`  
**Betroffene Methoden:**

- `passesAutoRespawnChunkGate(...)`
- `spawnNpcEntity(...)`

**Risiko:** **mittel bis hoch**

#### Aktueller Ablauf

Chunk-Gate nutzt:

```text
resolveStatePreferredMarker(npc)
sonst npc.currentPosition()
```

Spawn nutzt später aber:

```text
npc.currentPosition()
```

#### Warum destruktiv?

Wenn Gate-Position und Spawn-Position auseinanderfallen:

```text
Marker-Chunk geladen
currentPosition-Chunk nicht geladen
Gate sagt ja
Spawn passiert in ungeprüftem Chunk
```

Oder:

```text
currentPosition ok
Marker fehlt/falsch
Gate blockt oder erlaubt falsch
```

#### Minimaler Fix-Vorschlag

Die Position, die im Chunk-Gate geprüft wird, muss exakt dieselbe sein, an der gespawnt wird.

---

### Fehler 19 — Manual Respawn Dry-Run kann `PENDING` als „würde spawnen“ werten

**Betroffene Datei:** `NpcRoutineRunner.java`  
**Betroffene Methode:** `respawnMissingNpcsInWorld(...)`  
**Risiko:** **mittel**

#### Aktueller Ablauf

Wenn UUID-Relink `PENDING` ist, läuft der Dry-Run weiter und kann am Ende `wouldSpawn++` zählen.

#### Warum gefährlich?

User/Agent bekommt falsche Diagnose:

```text
Dry-run: würde spawnen
Realität: UUID-Relink ist noch nicht final entschieden
```

Dann kann ein Force-Respawn ausgelöst werden, obwohl eigentlich noch ein Relink laufen sollte.

#### Minimaler Fix-Vorschlag

`PENDING` muss im Dry-Run als eigener Zustand gezählt werden:

```text
pendingRelink++
kein wouldSpawn++
```

---

### Fehler 20 — Index-basierter Remove kann falschen NPC löschen

**Betroffene Datei:** `NpcRemoveCommand.java`  
**Betroffene Methode:** `execute(...)`  
**Risiko:** **mittel**

#### Aktueller Ablauf

Command löscht nach Listenindex:

```text
/knpc remove <index>
```

Die Liste wird dynamisch aus aktuellem Runtime-State gebaut.

#### Warum destruktiv?

Wenn zwischen `/knpc list` und `/knpc remove 2` ein NPC dazukommt, verschwindet oder sortiert wird, kann Index 2 ein anderer NPC sein.

#### Minimaler Fix-Vorschlag

Löschen per stabiler `npcId`, oder Remove-Command muss Name+ID bestätigen.

---

### Fehler 21 — `clearNpcs()` / Clear-Command mass-löscht ohne zweistufige Bremse

**Betroffene Dateien:**

- `NpcClearCommand.java`
- `NpcRoutineRunner.java`

**Risiko:** **hoch**

#### Aktueller Ablauf

Clear entfernt alle NPCs per `removeNpc(...)` und speichert danach.

Wegen Fehler 4/5 kann das bedeuten:

```text
Records weg
Live-Entities nicht garantiert weg
state.json ohne NPCs gespeichert
```

#### Minimaler Fix-Vorschlag

Mass-Clear braucht:

```text
- dry-run
- force confirm
- Liste betroffener NPCs
- Removal-Erfolg/queued-Erfolg je NPC
- kein Save, wenn Removal nicht sicher war
```

---

### Fehler 22 — Restore kann Positionen automatisch überschreiben

**Betroffene Datei:** `IdleMarkerService.java`  
**Betroffene Methode:** `normalizeRestorePosition(...)`  
**Risiko:** **mittel**

#### Aktueller Ablauf

Beim Restore kann `currentPosition` anhand von Marker/Idle-State neu gesetzt werden.

#### Warum destruktiv?

Wenn Marker-Zuweisungen falsch/fallbacked sind, wird auch die Position falsch normalisiert.

Danach speichert `setup()` direkt.

Folge:

```text
alte reale Position verloren
falsche Marker-Position wird neue persisted position
```

#### Minimaler Fix-Vorschlag

Restore-Normalisierung sollte nicht sofort persistiert werden, oder nur mit klarer Diagnose/Dirty-Reason.

---

### Fehler 23 — External JSON Override kann packaged Definition shadowen und NPCs canceln

**Betroffene Datei:** `NpcDefinitionRegistry.java`  
**Betroffene Methode:** `readText(...)` / Definition Loading  
**Risiko:** **mittel bis hoch**

#### Ablauf

Externe Resource-Dateien können interne packaged JSONs überschreiben.

Wenn eine externe Datei kaputt ist:

```text
Role/Definition invalid
Restore staleReasonForRestore
NPC wird DISABLED
setup save
DISABLED bleibt persistiert
```

#### Minimaler Fix-Vorschlag

Bei external override failure:

```text
nicht sofort NPC-Status dauerhaft ändern
erst Definition-Load als failed markieren
keinen State-Save aus Restore-Normalisierung
```

---

### Fehler 24 — Ownership Map erkennt UUID-Case-Varianten nicht sicher

**Betroffene Datei:** `RelinkWorkflowService.java`  
**Betroffene Methode:** `ownerByUuid(...)` / Ownership Snapshot  
**Risiko:** **mittel**

#### Problem

An mehreren Stellen wird UUID case-insensitive verglichen. Ownership Map nutzt aber Raw-String-Key.

Wenn `state.json` manuell oder durch andere Quelle uppercase/mixed UUID enthält, kann Ownership Claim nicht greifen.

#### Folge

- Claimed Entity wirkt unclaimed
- Anchor/Dedupe-Entscheidung kann zu großzügig sein
- Falsches Binding wahrscheinlicher

#### Minimaler Fix-Vorschlag

UUIDs vor Speicherung und Ownership-Map normalisieren:

```text
lowercase canonical UUID string
```

---

### Fehler 25 — `rollbackSpawnedEntityAfterSpawnFailure(...)` kann Erfolg suggerieren, obwohl Entity nicht entfernt wurde

**Betroffene Datei:** `NpcRoutineRunner.java`  
**Betroffene Methode:** `rollbackSpawnedEntityAfterSpawnFailure(...)`  
**Risiko:** **mittel bis hoch**

#### Problem

Wenn direkte Entfernung fehlschlägt, wird queued removal versucht. Wenn beides nicht sicher gelingt, kann dennoch ein Log erscheinen, der „orphan prevented“ suggeriert.

#### Warum gefährlich?

Debug/Review glaubt, Orphan sei verhindert. Tatsächlich kann eine Entity übrig geblieben sein.

#### Minimaler Fix-Vorschlag

Log muss differenzieren:

```text
directRemoved
queuedRemoval
failedToRemove
```

Bei `failedToRemove` muss Record/State als unsicher markiert werden.

---

## 3. Destructive Statusfluss-Beispiele

### Szenario A — Save Failure nach Spawn

```text
/knpc spawn lumberjack Bob
-> Entity wird live erzeugt
-> NpcRecord wird runtime registriert
-> saveStateSafely()
   -> JsonFileStateStore.save() scheitert
   -> Fehler wird verschluckt
   -> saveStateSafely() gibt true
-> Command meldet Erfolg
-> Restart
-> Bob steht eventuell live in Welt, aber nicht in state.json
```

Ergebnis:

```text
Orphan / Duplicate-Gefahr
```

---

### Szenario B — Kaputter State löscht alles

```text
state.json enthält 20 NPCs
1 Marker hat invaliden Typ
load()
-> MarkerType.valueOf(...) wirft
-> catch global
-> PluginState.empty()
setup()
-> saveState()
-> state.json wird leer überschrieben
```

Ergebnis:

```text
Alle NPC-/Marker-Daten verloren
```

---

### Szenario C — Clear löscht Marker und bricht NPC-Routinen

```text
NPCs haben markerAssignments
/knpc marker clear
-> MarkerRegistry leer
später Save
-> state.json markers leer
-> NPCs referenzieren alte markerIds
Restart
-> Marker fehlen
-> Resolver fallbacked
-> NPCs bekommen ggf. falsche Marker
```

Ergebnis:

```text
Marker-Topologie zerstört
NPC-Routinen falsch
```

---

### Szenario D — Remove erzeugt Orphan

```text
/knpc remove 1
-> npcs.remove(npcId)
-> clear runtime maps
-> removeLiveEntity()
   -> world nicht gefunden
   -> return
-> markDirty
-> save
```

Ergebnis:

```text
state.json löscht NPC
Live-Entity bleibt
```

---

### Szenario E — Temporärer JSON-Fehler disabled alle NPCs

```text
Definition JSON versehentlich duplicate roleId
restore()
-> role invalid
-> staleReasonForRestore
-> npc.entityStatus(DISABLED)
setup()
-> saveState()
```

Ergebnis:

```text
NPCs bleiben disabled, auch nachdem JSON wieder repariert wurde
```

---

## 4. Priorisierte Fix-Reihenfolge

### P0 — State-Datei vor Datenverlust schützen

**Ziel:** Kein leerer Save nach Load-Fehler.

Minimal:

1. `JsonFileStateStore.load()` darf bei corrupt/failed nicht einfach `PluginState.empty()` als normalen Zustand ausgeben.
2. `KeystoneNpcPlugin.setup()` darf nach Load-Error nicht automatisch speichern.
3. Backup/Quarantine für kaputte `state.json`.

**Erlaubte Dateien:**

- `JsonFileStateStore.java`
- `KeystoneNpcPlugin.java`

---

### P1 — Save-Failure korrekt signalisieren

**Ziel:** Kein falsches `saveStateSafely() == true`.

Minimal:

1. `JsonFileStateStore.save(...)` darf Exceptions nicht verschlucken.
2. `saveStateSafely()` darf nur bei echtem Save `true` liefern.
3. Dirty-State nur nach echtem Save löschen.

**Erlaubte Dateien:**

- `JsonFileStateStore.java`
- `KeystoneNpcPlugin.java`
- `NpcRoutineRunner.java`

---

### P2 — Remove/Clear gegen Orphans absichern

**Ziel:** Kein Record löschen, solange Live-Entity nicht sicher entfernt/queued ist.

Minimal:

1. `removeNpc(...)` nicht vor Entity-Removal finalisieren.
2. `removeLiveEntity(...)` Identity nicht vor Removal löschen.
3. Clear-Command nur mit Force/Dry-run oder sicherem Removal-Status.

**Erlaubte Dateien:**

- `NpcRoutineRunner.java`
- `NpcClearCommand.java`
- `NpcRemoveCommand.java`

---

### P3 — Marker-Mutation aus Read-Pfaden entfernen

**Ziel:** Keine stillen Marker-Rewrites.

Minimal:

1. Read-only Resolver.
2. Explicit assignment Resolver.
3. Restore/Tick/Validation verwenden read-only.
4. Fallback-Zuweisung nur im Spawn/Admin-Recovery-Pfad.

**Erlaubte Dateien:**

- `MarkerResolver.java`
- `NpcRoutineRunner.java`
- `RespawnRecoveryService.java`
- `StateTargetingService.java`

---

### P4 — Runtime-State nicht persistieren

**Ziel:** Keine Walking/Paused-Zustände in `state.json`.

Minimal:

1. Beim Save runtime states normalisieren.
2. Persistent state getrennt von runtime state betrachten.
3. `PAUSED_MISSING_MARKER` nicht dauerhaft als normale NPC-State speichern.

**Erlaubte Dateien:**

- `JsonFileStateStore.java`
- eventuell `NpcState.java`

---

## 5. Dateien, die wahrscheinlich minimal geändert werden müssten

```text
src/main/java/keystone/npc/persistence/JsonFileStateStore.java
src/main/java/keystone/npc/KeystoneNpcPlugin.java
src/main/java/keystone/npc/routine/NpcRoutineRunner.java
src/main/java/keystone/npc/markers/MarkerResolver.java
src/main/java/keystone/npc/markers/MarkerClearCommand.java
src/main/java/keystone/npc/commands/NpcClearCommand.java
src/main/java/keystone/npc/commands/NpcRemoveCommand.java
src/main/java/keystone/npc/routine/RespawnRecoveryService.java
src/main/java/keystone/npc/routine/StateTargetingService.java
src/main/java/keystone/npc/routine/EntitySyncService.java
```

---

## 6. Dateien, die nicht unnötig geändert werden sollten

```text
Server/NPC/Roles/*
Server/NPC/Keystone/roles/*
Door-System
Navigation-System
Dedupe-System, außer Ownership/UUID-Normalisierung direkt betroffen
setRoleName / Engine-Role-Architektur
```

---

## 7. Neue Safety-Regeln, die ergänzt werden sollten

### Save-/Load-Safety

```text
Ein Load-Fehler darf niemals automatisch zu einem leeren Save führen.
Ein Save-Fehler darf niemals still als Erfolg gelten.
Dirty-State darf nur nach nachweislich erfolgreichem Save gelöscht werden.
```

### Remove-Safety

```text
Ein NPC-Record darf erst gelöscht werden, wenn die Live-Entity entfernt oder sicher zur Entfernung queued wurde.
Entity-Identity darf nicht vor erfolgreicher Entity-Entfernung gelöscht werden.
```

### Marker-Safety

```text
Read-/Validation-/Tick-Pfade dürfen Marker-Assignments nicht mutieren.
Marker-Fallback-Zuweisung ist ein expliziter Recovery-/Spawn-Schritt.
Marker-Clear darf nicht NPC-Zuordnungen zerstören, ohne Force/Dry-run/Safe-Save.
```

### Runtime-State-Safety

```text
Runtime-Zustände wie Walking, Paused, transient navigation/action states dürfen nicht autoritativ in state.json gespeichert werden.
```

### Cleanup-Safety

```text
Orphan-Cleanup ist verboten, wenn der letzte State-Load fehlgeschlagen, partiell oder unsicher war.
```

---

## 8. Test-Gates

Pflicht nach Fixes:

```bash
mvn -q -DskipTests test-compile
```

Zusätzliche destruktive Tests:

```text
[ ] Save-Failure simulieren: Schreibrechte auf state.json entfernen
[ ] Prüfen: Spawn rollbackt sauber oder meldet Fehler
[ ] Prüfen: Dirty bleibt true nach Save-Failure
[ ] Kaputte state.json mit invalidem MarkerType testen
[ ] Prüfen: kein empty overwrite
[ ] Kaputte state.json mit invalidem NpcState testen
[ ] Prüfen: gute Records bleiben erhalten oder Load blockt ohne Save
[ ] /knpc marker clear mit NPCs, die Marker verwenden
[ ] Prüfen: kein inkonsistenter Save ohne Force
[ ] /knpc clear bei nicht geladener World
[ ] Prüfen: keine Orphans / keine gelöschten Records ohne Removal
[ ] Cleanup-Orphans nach absichtlich fehlgeschlagenem State-Load
[ ] Prüfen: Cleanup wird blockiert
[ ] Walking-State speichern/restarten
[ ] Prüfen: state.json speichert keinen transienten Walking-State
[ ] Temporär kaputte Role-JSON einbauen
[ ] Prüfen: NPCs werden nicht dauerhaft DISABLED gespeichert
[ ] Duplicate npcId in state.json
[ ] Prüfen: Restore blockiert/quarantänisiert statt still zu überschreiben
```

---

## 9. Kurzfazit

Die aktuell gefährlichsten destruktiven Fehler sind nicht mehr nur Relink-/Respawn-Statusübergänge.

Der größte systemische Risikoblock ist:

```text
Load/Save-Fehler werden zu normalem leerem State oder falschem Save-Erfolg.
```

Danach kommen:

```text
Remove/Clear löscht Records/Marker vor sicherem Entity-/State-Erfolg.
Marker-Fallbacks mutieren Daten in Read-Pfaden.
Runtime-Zustände werden persistiert.
```

Wenn du das System wirklich stabil machen willst, sollte der nächste Agent-Plan nicht mit Respawn anfangen, sondern mit:

```text
P0: State Load/Save Safety
P1: Remove/Clear Orphan Safety
P2: Marker Mutation Safety
```
