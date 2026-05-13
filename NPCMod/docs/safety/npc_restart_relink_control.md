# NPC Restart / Relink / Respawn Safety Control File

> **Status:** Validated Baseline
> **Projekt:** NPCMod / KeystoneNPC
> **Zweck:** Kontroll-Datei für Restart-, Relink-, Respawn-, Persistenz- und Entity-Safety

Diese Datei beschreibt den aktuell gültigen, geprüften Sicherheitsstand der NPCMod.

Sie dient als Kontroll- und Review-Datei für spätere Änderungen an anderen Systemen, zum Beispiel:

- JSON-/Role-Hierarchie
- Marker-System
- Routine-System
- Navigation
- Door-System
- Commands
- Persistence
- Spawn / Respawn / Relink
- Save/Dirty-System
- Dedupe / Ownership
- Hytale Engine-Roles

Wenn eine AI, ein Agent oder ein Entwickler später Änderungen macht, muss diese Datei verwendet werden, um sicherzustellen, dass die hier validierten Safety-Funktionen nicht versehentlich beschädigt werden.

Wenn eine Änderung dieses Safety-System bewusst verändert, muss diese Datei aktualisiert werden:

- neue Funktion beschreiben
- alte Regel als ersetzt markieren
- neue Tests ergänzen
- neue Risiken dokumentieren
- Compile-/Regression-Gates anpassen

---

## Inhaltsverzeichnis

1. [Grundprinzipien](#1-grundprinzipien)
2. [Gültige Zielarchitektur](#2-gültige-zielarchitektur)
3. [Validierte Agent Steps](#3-validierte-agent-steps)
4. [Nicht registrierte NPCs / MVP-B-Hinweis](#4-nicht-registrierte-npcs--mvp-b-hinweis)
5. [Kritische No-Go-Regeln](#5-kritische-no-go-regeln)
6. [Standard Review-Prozess für spätere Patches](#6-standard-review-prozess-für-spätere-patches)
7. [Compile- und Test-Gates](#7-compile--und-test-gates)
8. [Bekannte Fehlerbilder und Diagnose](#8-bekannte-fehlerbilder-und-diagnose)
9. [Pflicht bei Änderungen an diesem Safety-System](#9-pflicht-bei-änderungen-an-diesem-safety-system)
10. [Aktueller validierter Abschlussstand](#10-aktueller-validierter-abschlussstand)
11. [Kurze Pflicht-Checkliste für jede spätere AI](#11-kurze-pflicht-checkliste-für-jede-spätere-ai)
12. [Empfohlene Commit-Regel](#12-empfohlene-commit-regel)

---

# 1. Grundprinzipien

## 1.1 Wichtigste Regel

> [!IMPORTANT]
> Ein NPC ohne gültige Live-EntityRef darf keine normale NPC-Logik ausführen.

Das bedeutet:

- keine Routine
- keine Navigation
- keine Action
- keine Door-Logik
- kein Movement-Fallback
- keine Position-Sync-Versuche
- keine alte Route fortsetzen
- kein Auto-Respawn ohne Sicherheitsprüfung
- kein Tick-Log-Spam

Nur Relink-, Recovery- oder kontrollierte Respawn-Systeme dürfen mit NPCs ohne gültige EntityRef arbeiten.

---

## 1.2 Identitäten strikt trennen

Die NPCMod trennt drei Identitäten:

| Ebene | Bedeutung | Persistenz |
|---|---|---|
| `npcId` | stabile Keystone-/Mod-Identität | ja |
| `entityUuid` | persistierte Hytale-/Engine-Entity-UUID | ja |
| `entityRef` | Runtime-Handle auf Live-Entity | nein |

Wichtig:

- `entityRef` ist kein dauerhafter Identitätsbeweis.
- `entityUuid` ist der stärkste Persistenzbeweis für eine bestehende Engine-Entity.
- `npcId` ist die stabile Mod-Identität.
- `entityRef` darf nicht in `state.json` gespeichert werden.

---

## 1.3 Hytale Engine-Role ist nicht Keystone Identity

> [!CAUTION]
> `Hytale Engine-Role ≠ Keystone NPC Identity`

Die frühere Idee, eine dynamische Runtime-Role wie:

```text
KeystoneNPC_<npcId>_<roleId>_Role
```

über `NPCEntity.setRoleName(...)` zu setzen, wurde als gefährlich erkannt.

Grund:

Hytale behandelt `roleName` offenbar als echte Engine-Role, die beim Restart aus `Server/NPC/Roles/...` geladen werden muss.

Wenn eine dynamische Role nicht als echte JSON-Role existiert, entstehen Fehler wie:

```text
Reloading nonexistent role KeystoneNPC_..._lumberjack_Role
```

Folge:

- Live-Entities können beim Restart verschwinden.
- Records bleiben in `state.json`.
- `/knpc list` zeigt NPCs weiterhin.
- Ingame sind die Entities weg.
- UUID-Relink findet nichts mehr.

Darum gilt ab jetzt:

```text
NPCEntity.setRoleName(...) darf nur echte existierende Hytale Engine-Roles setzen.
```

Keystone-Identität bleibt in:

- `npcId`
- `entityUuid`
- `entityStatus`
- Marker-Zuweisungen
- `state.json`
- später eventuell sichere Custom Metadata / Component / Tag, falls Hytale dafür eine stabile API bietet

---

# 2. Gültige Zielarchitektur

## 2.1 Persistenter NPC-State

Persistiert werden sollen:

- `npcId`
- `npcName`
- `role` / `roleId`
- `state`
- `entityStatus`
- `worldId`
- `currentPosition`
- `entityUuid`
- Marker-Zuweisungen:
  - `bedMarkerId`
  - `workMarkerId`
  - `foodMarkerId`
  - `chestMarkerId`
  - `doorMarkerId`
  - spätere Marker über `markerAssignments`

Nicht persistiert werden dürfen:

- `entityRef`
- Runtime-Navigation
- Runtime-Door-State
- aktive Door-Passes
- pending Door Attempts
- active Action Runtime
- temporary movement target
- transient Hytale handles

---

## 2.2 Runtime-State

Runtime-only:

- `entityRef`
- aktive Navigation
- aktive Action
- pending Action
- Door-Pending-State
- Active-Door-Pass-State
- Tick-/Cooldown-Maps
- in-flight Spawn Requests
- temporary Relink candidates

Diese Daten dürfen nach Restart nicht als Wahrheit übernommen werden.

---

## 2.3 Ordnerstruktur-Regel

### `Server/NPC/Roles/`

Enthält nur echte Hytale Engine-Roles.

Beispiele:

```text
Server/NPC/Roles/Lumberjack.json
Server/NPC/Roles/Lumberjack_Oldman.json
Server/NPC/Roles/Lumberjack_Oldwife.json
Server/NPC/Roles/Lumberjack_Wife.json
Server/NPC/Roles/Template_Human_Friendly.json
Server/NPC/Roles/Test.json
```

Diese Rollen müssen von Hytale beim Restart geladen werden können.

### `Server/NPC/Keystone/`

Enthält Keystone-Mod-Logik, z. B.:

```text
Server/NPC/Keystone/npcs/
Server/NPC/Keystone/routines/
Server/NPC/Keystone/actions/
Server/NPC/Keystone/groups/
Server/NPC/Keystone/structures/
Server/NPC/Keystone/markers/
```

Keystone-spezifische Identitäten dürfen nicht in Engine-Roles hineingemischt werden.

---

# 3. Validierte Agent Steps

## Step 1 — Live-Entity-Gate

### Ziel

Kein normaler NPC-Tick ohne gültige Live-EntityRef.

### Validierter Zustand

Es gibt einen harten Guard im NPC-Tick-/Runner-Pfad.

Wenn `entityStatus == DISABLED`:

```text
sofort return
```

Wenn `entityRef == null` oder invalid:

```text
entityRef = null
entityId = 0
wenn entityStatus bereits MISSING_ENTITY: bleibt MISSING_ENTITY (sticky)
sonst bei entityUuid vorhanden: entityStatus = NEEDS_RELINK
wenn entityUuid fehlt: entityStatus = MISSING_ENTITY
Runtime-Navigation clearen
pendingActionId löschen
activeActionId löschen
Door-/Pending-Runtime-State löschen
normale NPC-Logik beenden
```

### Was geschützt wird

Ohne gültige EntityRef darf nicht laufen:

- Routine
- Navigation
- Action
- Door-Logik
- Movement-Fallback
- Position-Sync
- Ziel-Reroute
- alte Navigation

### Darf nicht kaputtgehen

Spätere Änderungen dürfen nicht dazu führen, dass bei fehlender EntityRef wieder Folgendes passiert:

```text
Cannot move NPC: missing or invalid EntityRef
```

Oder:

```text
Routine startet ohne Live-Entity
Navigation startet ohne Live-Entity
Door-Logik läuft ohne Live-Entity
```

### Review-Fragen

- [ ] Gibt es weiterhin einen frühen Guard?
- [ ] Wird `DISABLED` sauber ignoriert?
- [ ] Wird fehlende Entity nicht als fehlender Marker behandelt?
- [ ] Wird nicht `PAUSED_MISSING_MARKER` gesetzt, wenn eigentlich die Entity fehlt?
- [ ] Wird Runtime-State beim Gate gecleart?

---

## Step 2 — Restore-Navigation löschen

### Ziel

Nach Restart darf keine alte Bewegung fortgesetzt werden.

### Validierter Zustand

Persistierte Navigation wird beim Laden ignoriert bzw. neutralisiert.

Beim Restore:

```text
navigationState.clear()
pendingActionId(null)
activeActionId(null)
lastActionNoRestartLog(null)
Door-/Pending-Runtime-State clearen
```

Alte Felder aus früheren `state.json`-Dateien dürfen weiterhin lesbar sein, aber sie dürfen keine aktive Route reaktivieren.

### Was geschützt wird

Nach Restart ist eine alte Route unsicher, weil:

- EntityRef eventuell fehlt
- Chunk eventuell nicht geladen ist
- Türzustand unbekannt ist
- Pathfinding neu berechnet werden muss
- Uhrzeit/Routine-Ziel inzwischen anders sein kann

### Erwartetes Verhalten

Nach Relink entscheidet die Routine frisch:

```text
Welche Uhrzeit ist jetzt?
Welcher State passt?
Welcher Marker ist Ziel?
Welche Navigation muss neu gestartet werden?
```

### Darf nicht kaputtgehen

Spätere Änderungen dürfen nicht wieder aktivieren:

- `remainingMs` aus alter Route
- alte `targetPosition`
- altes `targetState`
- alte `markerId` als aktive Route
- alte Door-Pending-State

### Review-Fragen

- [ ] Wird alte Navigation beim Load ignoriert?
- [ ] Wird keine alte Route resumed?
- [ ] Bleiben `npcId`, `entityUuid`, `roleId`, Marker und State erhalten?
- [ ] Bleibt `state.json` kompatibel?

---

## Step 3 — Log-Cooldown

### Ziel

Kein Tick-Log-Spam bei Missing-/Relink-/Pending-Zuständen.

### Validierter Zustand

Es gibt ein Cooldown-System pro NPC und Event-Key.

Gedrosselte Events können sein:

```text
ENTITY_REF_INVALID
MISSING_ENTITY
NEEDS_RELINK
RELINK_ATTEMPT
RELINK_RETRY
RELINK_PENDING
AUTO_RESPAWN_SKIPPED
AUTO_RESPAWN_DISABLED
DEDUPE_ROLEID_CANDIDATE_UNCLAIMED
```

Cooldown-Ziel:

```text
maximal alle ca. 5 Sekunden pro NPC/Event
```

### Erfolgslogs bleiben sichtbar

Nicht gedrosselt oder weiterhin klar sichtbar:

```text
RELINK_SUCCESS
RESPAWN_CREATED_REPLACEMENT
SPAWN_ENTITY_ROLLBACK
UUID_CONFIRMED
```

### Kritische Fehler

Kritische Fehler dürfen nicht verschluckt werden.

Insbesondere dürfen echte Fehler nicht still verschwinden:

```text
SPAWN_ENTITY_ROLLBACK_*
RESPAWN_EXCEPTION
RELINK_GIVEUP_MARKED_MISSING
RESPAWN_FORCE_PRECHECK_FAILED
```

### Darf nicht kaputtgehen

Spätere Änderungen dürfen keine neue Log-Flut erzeugen, z. B.:

- pro Tick `MISSING_ENTITY`
- pro Tick `NEEDS_RELINK`
- pro Tick `AUTO_RESPAWN_SKIPPED`
- pro Tick `Cannot move NPC`
- nach `RELINK_GIVEUP_MARKED_MISSING` sofort im nächsten Tick wieder derselbe Relink-Zyklus

### Review-Fragen

- [ ] Wird pro NPC + Event-Key gedrosselt?
- [ ] Bleiben Erfolgslogs sichtbar?
- [ ] Werden kritische Fehler nicht verschluckt?
- [ ] Gibt es keine neue Tick-Flut?

---

## Step 4 — RelinkHelper nach HyCitizens-Art

### Ziel

UUID-Relink zentralisieren und absichern.

### Validierter Zustand

UUID-Relink nutzt eine zentrale Resolve-Methode.

Reihenfolge:

```text
1. world.getEntityRef(uuid), falls API verfügbar
2. fallback: world.getEntityStore().getRefFromUUID(uuid)
3. ref.isValid prüfen
4. NPCEntity-Komponente prüfen
5. UUIDComponent aus Live-Entity lesen
6. Live-UUID hart gegen persistierte entityUuid vergleichen
7. Ownership prüfen
8. Rollen-/RoleIndex-Prüfung
9. erst dann ACTIVE setzen
```

### Wichtigste Safety-Regel

Live-UUID muss exakt zur persistierten UUID passen:

```text
liveUuid.equalsIgnoreCase(entityUuid.toString())
```

Wenn nicht:

```text
kein Relink
return null / NO_MATCH
```

### Was geschützt wird

- falsches Relinken
- falsche EntityRef
- falsche API-Auflösung
- UUID-/String-Mismatch
- Entity einer anderen NPC-Record

### Darf nicht kaputtgehen

Spätere Änderungen dürfen nicht wieder nur nach Rolle oder Nähe relinken.

Nicht erlaubt:

```text
Ref gefunden → einfach binden
```

Erlaubt nur:

```text
Ref gefunden
→ valid
→ NPCEntity vorhanden
→ UUIDComponent vorhanden
→ UUID stimmt exakt
→ Ownership/Role passt
→ Relink
```

### Review-Fragen

- [ ] Wird zuerst `world.getEntityRef(uuid)` versucht?
- [ ] Gibt es Store-Fallback?
- [ ] Wird `ref.isValid()` geprüft?
- [ ] Wird `UUIDComponent` gelesen?
- [ ] Wird Live-UUID gegen persistierte UUID verglichen?
- [ ] Wird `entityUuid` nach erfolgreichem Relink aus Live-Entity bestätigt?

---

## Step 5 — Chunk-Gate vor Auto-Respawn

### Ziel

Auto-Respawn darf nur passieren, wenn der relevante Bereich sicher geladen ist.

### Validierter Zustand

Im Auto-Respawn-Pfad sitzt ein Chunk-Gate vor:

```text
spawnRequestsInFlight.add(...)
world.execute(...)
spawnNpcEntity(...)
```

### Geprüfte Position

```text
currentPosition
```

Wichtig:

```text
nur wenn currentPosition bekannt (hasKnownCurrentPosition)
und alle Koordinaten finite sind
```

Nicht erlaubt:

```text
Origin-/Default-Fallback (0,0,0)
Idle-Marker als implizite Spawn-Position im Auto-Respawn
```

### Wenn Chunk nicht geladen oder nicht sicher prüfbar ist

Dann:

```text
kein Auto-Respawn
entityRef = null
entityId = 0
wenn bereits MISSING_ENTITY: bleibt MISSING_ENTITY (sticky)
sonst entityStatus = NEEDS_RELINK
Log via Cooldown
kein Tick-Spam
```

### Safety-Prinzip

Wenn keine zuverlässige Chunk-/Position-Loaded-API gefunden wird:

```text
return false
```

Also:

```text
nicht blind spawnen
```

### Was geschützt wird

Ohne Chunk-Gate kann passieren:

```text
Server startet
NPC-Chunk nicht geladen
EntityRef fehlt
Mod denkt: Entity fehlt
Mod spawnt Ersatz
später lädt alter NPC doch
Duplicate entsteht
```

Chunk-Gate verhindert genau das.

### Force-/Admin-Pfade

Manual / Force darf nicht versehentlich kaputtgehen.

Auto-Respawn ist streng.

Force/Admin muss getrennt betrachtet werden.

### Darf nicht kaputtgehen

Spätere Änderungen dürfen nicht wieder Auto-Respawn in ungeladenen Bereichen erlauben.

### Review-Fragen

- [ ] Sitzt der Gate vor Auto-Respawn?
- [ ] Wird world geprüft?
- [ ] Wird nur sichere bekannte finite currentPosition verwendet?
- [ ] Wird bei ungeladenem Chunk wirklich nicht gespawnt?
- [ ] Bleibt bei Missing der Status `MISSING_ENTITY` (sticky)?
- [ ] Kein Tick-Spam?
- [ ] Kein globales Chunk-Preloading?

---

## Step 6 — Role-Prefix-Fallback: korrigierter finaler Zustand

### Ursprüngliche Idee

Dynamische Runtime-Role:

```text
KeystoneNPC_<npcId>_<roleId>_Role
```

### Befund

Diese Idee ist für Hytale über `NPCEntity.setRoleName(...)` gefährlich.

Hytale versucht solche RoleNames beim Restart als echte Engine-Roles zu laden.

Fehlerbild:

```text
Reloading nonexistent role KeystoneNPC_..._lumberjack_Role
```

Folge:

- Entities verschwinden beim Restart
- Records bleiben erhalten
- `/knpc list` zeigt NPCs
- ingame sind sie weg

### Final validierter Zustand

Dynamisches `setRoleName(...)` ist deaktiviert.

Es darf kein aktiver Code existieren, der Folgendes setzt:

```text
NPCEntity.setRoleName("KeystoneNPC_<npcId>_<roleId>_Role")
```

### Erlaubt

`NPCEntity.setRoleName(...)` nur für echte existierende Engine-Roles.

### Role-Prefix-Fallback

Der Role-Prefix-Fallback ist derzeit entschärft/deaktiviert.

Er darf nicht davon ausgehen, dass `KeystoneNPC_<npcId>_` im Hytale-roleName steht.

### Final gültige Relink-Beweise

Aktuell gültig:

```text
1. UUID-Relink
2. Anchor-Relink als letzter Fallback
3. Ambiguous blockiert
```

Role-Prefix darf erst wieder aktiviert werden, wenn es eine sichere API gibt, z. B.:

- Custom Component
- persistent metadata
- tags
- andere stabile Nicht-Role-Identität

### Darf nicht kaputtgehen

Spätere Änderungen dürfen nicht erneut dynamische Hytale Roles setzen.

Verboten:

```text
KeystoneNPC_<npcId>_<roleId>_Role als Engine-roleName
```

### Review-Fragen

- [ ] Gibt es aktive Strings `KeystoneNPC_` im Runtime-Role-Kontext?
- [ ] Gibt es aktive `setRoleName(...)`-Calls mit dynamischen Namen?
- [ ] Bleibt Engine-roleName echte Hytale Role?
- [ ] Ist Role-Prefix-Fallback deaktiviert oder sicher neutralisiert?
- [ ] Gibt es keine `Reloading nonexistent role KeystoneNPC_...` Logs mehr?

---

## Step 7 — Anchor-Relink-Reihenfolge

### Ziel

Anchor-Relink darf nur letzter Fallback sein.

### Finaler Zustand nach Role-Prefix-Korrektur

Da Role-Prefix deaktiviert ist, gilt praktisch:

```text
1. UUID-Relink
2. Anchor-Relink als letzter Fallback
3. AMBIGUOUS blockiert
4. kein Auto-Respawn bei Ambiguous
```

Wenn in Zukunft ein sicherer Metadata-/Component-Identifier eingeführt wird, darf die Reihenfolge werden:

```text
1. UUID-Relink
2. sichere Keystone-Metadata-ID
3. Anchor-Relink
4. AMBIGUOUS blockiert
```

### Anchor-Fallback darf nur binden, wenn eindeutig

Wenn mehrere Kandidaten:

```text
AMBIGUOUS
kein Relink
kein Auto-Respawn
kein Entity-Delete
```

### Was geschützt wird

- falsches Binden bei mehreren gleichen NPCs
- falsches Binden bei nahen NPCs
- Auto-Respawn trotz möglicher Kandidaten
- aggressive Dedupe-Deletes

### Dedupe-Regel

Unclaimed ähnliche Entities dürfen nicht blind gelöscht werden.

Nur löschen, wenn es ein sicherer Same-Record-Duplicate ist.

### Review-Fragen

- [ ] Ist UUID der erste Beweis?
- [ ] Ist Anchor nur letzter Fallback?
- [ ] Blockiert AMBIGUOUS?
- [ ] Verhindert AMBIGUOUS Auto-Respawn?
- [ ] Werden Ownership-Claims respektiert?
- [ ] Werden unclaimed Kandidaten nicht gelöscht?

---

## Step 8 — Save/Dirty-System

### Ziel

State wird nur bei echten Änderungen gespeichert, nicht pro Tick.

### Validierter Zustand

Es gibt Dirty-State:

```text
stateDirty
nextDirtySaveAtMs
DIRTY_SAVE_INTERVAL_MS
```

Flush:

```text
nur wenn dirty
gebündelt ca. alle 5 Sekunden
```

### Speichern bei echten Änderungen

Dirty markieren bei:

- Spawn erfolgreich
- Relink erfolgreich
- Respawn erfolgreich
- Statusänderung
- Entity-Identity-Änderung
- Marker gesetzt
- NPC erstellt
- NPC entfernt
- Shutdown Save

### Wichtig

Nicht blind pro Tick `markDirty()`.

Besser:

```text
Snapshot vorher
Änderung durchführen
Snapshot nachher vergleichen
nur bei echter Persistenzänderung dirty markieren
```

### Persistenzrelevante Felder

- `entityStatus`
- `entityUuid`
- `currentPosition`
- Marker-Zuweisungen
- NPC create/remove
- ggf. role/state, wenn gezielt geändert

### Nicht speichern

- `entityRef`
- Runtime-Navigation
- Runtime-Door-State
- pending actions
- active actions
- live-only handles

Zusätzlich gilt:

```text
transiente Laufzustände werden vor Persistenz normalisiert
(z. B. WALKING_TO_* -> stabiler Zielzustand, PAUSED_MISSING_MARKER -> IDLE)
```

### Darf nicht kaputtgehen

Spätere Änderungen dürfen nicht:

- pro Tick speichern
- Runtime-Navigation wieder speichern
- `entityRef` speichern
- Door-Runtime speichern
- echte Statusänderungen ohne Dirty-Markierung lassen

### Review-Fragen

- [ ] Wird nicht pro Tick gespeichert?
- [ ] Werden echte Änderungen gespeichert?
- [ ] Bleibt Shutdown-Save?
- [ ] Wird `entityRef` nicht gespeichert?
- [ ] Wird Runtime-Navigation nicht gespeichert?
- [ ] Bleibt `state.json` kompatibel?

---

## Step 9 — Load/Save-Failure + Dirty-Reset-Regeln

### Ziel

Fehler bei Load/Save dürfen niemals als Erfolg gewertet werden oder still `state.json` zerstören.

### Validierter Zustand

Load-Failure:

```text
stateLoadFailed = true
kein Restore-Autosave
saveStateSafely() blockiert spätere Saves
```

Partial-Load (defensive skips):

```text
stateLoadPartial = true
automatic save blockiert
kein stilles Überschreiben mit teilgeladenem Zustand
```

Save-Failure:

```text
saveStateSafely() gibt false zurück
Fehler bleibt Fehler (kein Erfolgssignal)
```

Dirty-Reset:

```text
stateDirty wird nur nach bestätigtem Save gelöscht
bei Save-Fehler bleibt/bleibt wieder dirty und retry wird geplant
```

### Was geschützt wird

- kein Empty-Overwrite nach fehlgeschlagenem Load
- kein "Save erfolgreich" trotz Save-Fehler
- kein Verlust von Retry-Signal bei Dirty-State

### Review-Fragen

- [ ] Blockiert Load-Failure alle automatischen Überschreibungen?
- [ ] Blockiert Partial-Load den automatischen Save?
- [ ] Gibt Save-Fehler korrekt `false` zurück?
- [ ] Wird Dirty nur nach echtem Save gelöscht?

---

## Step 10 — Relink/Respawn Policy (PENDING + Profile + Position)

### Ziel

Keine irreführenden Recovery-Pfade: PENDING stoppt Fallbacks, Policy bleibt strikt.

### Validierter Zustand

PENDING-Regel:

```text
RelinkOutcome.PENDING stoppt den aktuellen Relink/Respawn-Zyklus
keine schwächeren Fallbacks, kein Ersatzspawn in diesem Zyklus
```

Restart-Auto-Respawn-Policy (`respawnAfterRestart`):

```text
nur für restaurierte Records
nur wenn autoRespawnMissingNpc global aktiv
nur wenn persistence profile respawnAfterRestart=true
nur mit gültiger role/definition
nur mit persistierter entityUuid ohne Ownership-Konflikt
nur mit vollständig auflösbaren required markers (read-only)
```

Position-Safety:

```text
Auto-Respawn/Spawn nur mit sicherer bekannter finite currentPosition
kein impliziter Marker-/Origin-Fallback
```

### Was geschützt wird

- kein falsches Dry-run-/Recovery-Signal bei PENDING
- kein Auto-Respawn trotz policy-blocked profile
- kein Spawn auf unbekannter/kaputter Position

### Review-Fragen

- [ ] Stoppt PENDING weiterhin jeden schwächeren Fallback im Zyklus?
- [ ] Erzwingt respawnAfterRestart=true im Persistence-Profile den Gate?
- [ ] Blockieren UUID-Ownership-Konflikte den Auto-Respawn?
- [ ] Bleibt Spawn ohne bekannte finite Position blockiert?

---

## Step 11 — Marker-Fallback, Remove/Clear-Orphan, ACTIVE-Bind

### Ziel

Mutation strikt trennen und destruktive Pfade nur bei sicherer Beweislage erlauben.

### Validierter Zustand

Marker-Fallback:

```text
read-only: resolveRequiredMarkerReadOnly(...) bleibt in Restore/Tick/Diagnose/Validation/Recovery/Respawn-Policy/Relink-Ankerprüfung strikt read-only
mutierend: Marker-Zuweisung ist nur in explizitem Spawn/Admin/Repair/Cleanup-Kontext erlaubt (harte Allowlist)
read-only Kontexte dürfen MarkerAssignments und Legacy-Markerfelder niemals verändern
```

Methodenstatus (Marker-Resolver-Audit):

```text
resolveRequiredMarkerWithFallbackAssigning(...) ist entfernt (nicht mehr aktiv)
resolveRequiredMarkerWithFallback(...) ist entfernt (nicht mehr aktiv)
resolveRequiredMarkerReadOnly(...) ist der verbindliche read-only Resolver
MarkerRegistry.getNextAvailable(...) bleibt nur deprecated Lookup-Helfer
MarkerRingTraversal bleibt internes Registry-Hilfsmittel und ist keine Persistenz-Wahrheit
```

Remove/Clear-Orphan-Safety:

```text
removeNpc löscht Record nur bei sicherem Outcome (NO_IDENTITY)
ungeklärte/ungeprüfte Entity-Entfernung blockiert Record-Delete
cleanup-orphans blockiert ohne --force bei offenen NEEDS_RELINK/MISSING/invalid ACTIVE
cleanup-orphans löscht keine claimed oder ownership-ambiguous Kandidaten
```

ACTIVE-Bind-Sicherheitsregeln:

```text
ACTIVE erst nach UUID+Ownership+Role-Prüfung
UUID-Relink: live UUID muss exakt zur persistierten UUID passen
Anchor-Relink: nur eindeutiger ownership-sicherer Kandidat
AMBIGUOUS => blockiert, kein Blind-Bind
```

### Review-Fragen

- [ ] Bleiben read-only und mutierende Marker-Resolver getrennt?
- [ ] Ist die Mutations-Allowlist weiter strikt: nur Spawn/Admin/Repair/Cleanup?
- [ ] Bleiben `resolveRequiredMarkerWithFallbackAssigning(...)` und `resolveRequiredMarkerWithFallback(...)` entfernt?
- [ ] Bleibt `getNextAvailable(...)` deprecated und außerhalb read-only Pfade (Restore/Tick/Validation/Diagnose/Recovery/Respawn-Policy/Relink-Ankerprüfung)?
- [ ] Wird remove/clear bei unbestätigter Entity-Entfernung blockiert?
- [ ] Blockiert cleanup-orphans weiterhin bei offenen Relink-Risiken ohne `--force`?
- [ ] Wird `entityStatus=ACTIVE` nur nach vollständiger Sicherheitsprüfung gesetzt?

### Reconcile-/Cleanup-Regel für MarkerAssignments (final)

`reconcilePersistedMarkerAssignments(...)` bzw. äquivalente Reconcile-Logik darf in read-only Kontexten niemals persistente MarkerAssignments verändern.

Read-only-Kontexte:

- Load
- Restore
- Validation
- Diagnose
- Tick

In diesen Kontexten ist erlaubt:

- MarkerAssignments lesen
- Inkonsistenzen erkennen
- warnen/loggen
- sichere Diagnose ausgeben

In diesen Kontexten ist verboten:

- MarkerAssignments automatisch löschen
- MarkerAssignments automatisch ersetzen
- `bedMarkerId`/`doorMarkerId`/`chestMarkerId`/`foodMarkerId`/`workMarkerId`/`chillMarkerId` automatisch auf `null` setzen
- `stateDirty` nur wegen Reconcile setzen
- `saveStateSafely()` nur wegen Reconcile auslösen
- kaputte/inkonsistente MarkerAssignments still bereinigen und in `state.json` zurückschreiben

Mutierendes Reconcile ist nur in expliziten Kontexten erlaubt:

- Admin-Repair
- Cleanup-Command
- Spawn
- Admin-Kontext

Symbol-Audit (Markdown-only, ohne Compile):

```bash
cd project_keystone/NPCMod
rg -n "resolveRequiredMarkerWithFallbackAssigning|resolveRequiredMarkerWithFallback" src/main/java
rg -n "getNextAvailable\(" src/main/java/keystone/npc
```

Erwartung:

```text
keine Treffer für resolveRequiredMarkerWithFallbackAssigning/resolveRequiredMarkerWithFallback
getNextAvailable(...) nur als deprecated Registry-/Traversal-Helfer, nicht in Restore/Tick/Validation/Diagnose/Recovery/Respawn-Policy/Relink-Ankerprüfung
```

Safety-Ziel:

```text
Restart ersetzt keine Marker automatisch.
Restart löscht keine Marker automatisch.
Validation und Diagnose mutieren keine MarkerAssignments.
state.json wird nicht wegen read-only Reconcile überschrieben.
```

---

# 4. Nicht registrierte NPCs / MVP-B-Hinweis

## Aktueller Stand

Nicht registrierte NPCs werden beim Chunk-Laden nicht automatisch Teil der Mod.

Das ist absichtlich sicher.

### Registrierter NPC

Wenn NPC in `state.json` existiert:

```text
Server startet
Record wird geladen
entityUuid ist bekannt
entityRef fehlt
Status NEEDS_RELINK
Chunk später geladen
UUID-Relink versucht
NPC wird ACTIVE
Routine läuft frisch weiter
```

### Nicht registrierter NPC

Wenn NPC nicht in `state.json` existiert:

```text
Chunk lädt
Live-Entity existiert vielleicht
Mod adoptiert sie NICHT blind
```

Das ist korrekt.

## Später MVP B / Worldgen / Settlement

Später braucht es ein eigenes Registrierungssystem:

```text
Chunk/Struktur wird geladen
geplante NPC-Spawns werden geprüft
npcId wird erzeugt oder aus Strukturinstanz geladen
NPC wird in state.json registriert
Entity wird gespawnt
```

Wichtig:

Unbekannte Live-Entities dürfen nicht blind adoptiert werden.

Sichere spätere Reihenfolge:

```text
1. entityUuid aus state.json
2. sichere Keystone-Metadata/Component-ID
3. Anchor/Marker-Nähe nur als letzter Fallback
```

---

# 5. Kritische No-Go-Regeln

## 5.1 Kein Movement ohne EntityRef

Verboten:

```text
Navigation startet ohne valid entityRef
Routine startet ohne valid entityRef
Door-Logik startet ohne valid entityRef
Movement-Fallback setzt Position ohne valid entityRef
```

---

## 5.2 Kein dynamisches roleName

Verboten:

```text
NPCEntity.setRoleName("KeystoneNPC_<npcId>_<roleId>_Role")
```

---

## 5.3 Kein blinder Auto-Respawn

Verboten:

```text
entityRef fehlt
→ sofort Ersatzspawn
```

Erforderlich:

```text
Relink versuchen
Chunk-Gate prüfen
Ambiguous prüfen
Rollback bereit
erst dann kontrollierter Spawn
```

---

## 5.4 Kein Anchor-Bind bei Mehrdeutigkeit

Verboten:

```text
mehrere nahe Kandidaten
→ irgendeinen nehmen
```

Erforderlich:

```text
AMBIGUOUS
blockieren
kein Spawn
kein Delete
```

---

## 5.5 Kein Save pro Tick

Verboten:

```text
save() in jedem Tick
markDirty() blind in jedem Tick
```

---

## 5.6 Keine Runtime-Daten in state.json

Verboten:

```text
entityRef
active route
remainingMs als aktive Route
door pending state
active action runtime
```

---

## 5.7 Kein mutierendes Reconcile in read-only Pfaden

Verboten:

```text
Load/Restore/Validation/Diagnose/Tick
-> Reconcile löscht oder ersetzt MarkerAssignments
-> Reconcile setzt stateDirty
-> Reconcile löst saveStateSafely() aus
```

Erforderlich:

```text
Read-only Pfade dürfen nur erkennen/loggen/blockieren.
Mutationen nur in expliziten Admin-Repair/Cleanup/Spawn/Admin-Kontexten.
```

---

# 6. Standard Review-Prozess für spätere Patches

Jede AI, die später Änderungen macht, muss vor Abschluss diese Fragen beantworten.

## 6.1 Allgemeine Fragen

- [ ] Welche Dateien wurden geändert?
- [ ] Betrifft die Änderung Spawn, Relink, Respawn, Persistence, Navigation, Door oder Marker?
- [ ] Gibt es Änderungen an `NpcRoutineRunner.java`?
- [ ] Gibt es Änderungen an `RelinkWorkflowService.java`?
- [ ] Gibt es Änderungen an `JsonFileStateStore.java`?
- [ ] Gibt es Änderungen an `RoleDefinitionRegistry.java`?
- [ ] Gibt es Änderungen an `Server/NPC/Roles/`?
- [ ] Gibt es Änderungen an `state.json`-Format?
- [ ] Gibt es neue `setRoleName(...)`-Calls?
- [ ] Gibt es neue Auto-Respawn-Pfade?
- [ ] Gibt es neue Save-Pfade?

---

## 6.2 Wenn `NpcRoutineRunner.java` geändert wurde

Prüfen:

- [ ] Live-Entity-Gate bleibt erhalten?
- [ ] Keine Routine ohne entityRef?
- [ ] Keine Navigation ohne entityRef?
- [ ] Auto-Respawn weiterhin chunk-gated?
- [ ] Dirty-Save nicht pro Tick?
- [ ] Runtime-Navigation nicht restored?
- [ ] Spawn-Rollback bleibt intakt?
- [ ] `spawnRequestsInFlight` bleibt erhalten?
- [ ] `respawnRetryAtMs` bleibt erhalten?
- [ ] `respawnFailureCounts` bleibt erhalten?

---

## 6.3 Wenn `RelinkWorkflowService.java` geändert wurde

Prüfen:

- [ ] UUID-Relink bleibt stärkster Beweis?
- [ ] Live-UUID wird aus UUIDComponent gelesen?
- [ ] Live-UUID wird gegen persistierte UUID verglichen?
- [ ] Ownership-Claims bleiben erhalten?
- [ ] Anchor-Fallback bleibt letzter Fallback?
- [ ] AMBIGUOUS blockiert?
- [ ] Keine dynamische Runtime-Role über `setRoleName(...)`?
- [ ] Role-Prefix bleibt deaktiviert oder wird nur über sichere Metadata-Lösung reaktiviert?

---

## 6.4 Wenn `JsonFileStateStore.java` geändert wurde

Prüfen:

- [ ] `entityRef` wird nicht gespeichert?
- [ ] Runtime-Navigation wird nicht als aktive Route gespeichert?
- [ ] Alte `state.json` bleibt kompatibel?
- [ ] `npcId`, `entityUuid`, `roleId`, Marker und State bleiben erhalten?
- [ ] Beim Load werden Runtime-Felder gecleart?
- [ ] Persistenzmodell bleibt migrationssicher?

---

## 6.5 Wenn `Server/NPC/Roles/` geändert wurde

Prüfen:

- [ ] Enthält der Ordner nur echte Hytale Engine-Roles?
- [ ] Jede Role-Datei kann von Hytale geladen werden?
- [ ] Keine dynamischen `KeystoneNPC_<npcId>_...` Role-Dateien?
- [ ] Keine Vermischung von Keystone-Instanz-Identität und Engine-Role?
- [ ] `npcPluginRoleName` zeigt auf echte Role?
- [ ] RoleIndex-Prüfung bleibt möglich?

---

## 6.6 Wenn Marker/Routine geändert wurden

Prüfen:

- [ ] Markeränderungen erzeugen keine Entity-Identity-Änderung ohne Dirty?
- [ ] Reroute passiert nur bei gültiger EntityRef?
- [ ] Routine startet nicht ohne valid EntityRef?
- [ ] Marker fehlt ≠ Entity fehlt
- [ ] Fehlende Marker setzen nicht fälschlich Entity-Status kaputt

---

## 6.7 Wenn Door-System geändert wurde

Prüfen:

- [ ] Door-Logik läuft nicht ohne valid EntityRef?
- [ ] Door-Pending-State wird beim Live-Entity-Gate gecleart?
- [ ] Door-Pending-State wird beim Restore gecleart?
- [ ] Door-System speichert keine Runtime-Daten dauerhaft?
- [ ] Kein Door-Tick-Spam?

---

# 7. Compile- und Test-Gates

## 7.1 Pflicht-Compile

Nach jedem Patch:

```bash
mvn -q -DskipTests test-compile
```

Akzeptiert:

```text
bekannte Unsafe-Warnungen
```

Nicht akzeptiert:

```text
BUILD FAILURE
Compilation error
```

---

## 7.2 Minimaler Restart-Test

Nach Änderungen an Spawn/Relink/Respawn/Persistence:

```text
1. Server starten
2. NPC spawnen
3. state.json prüfen: npcId + entityUuid vorhanden
4. Server stoppen
5. Server starten
6. Einloggen
7. prüfen: NPC sichtbar
8. prüfen: kein Duplicate
9. prüfen: RELINK_SUCCESS oder kontrollierter Recovery-Pfad
```

---

## 7.3 Kein-Movement-ohne-EntityRef-Test

Im Log darf nicht auftauchen:

```text
Cannot move NPC: missing or invalid EntityRef
```

Während `entityRef` fehlt, darf nicht laufen:

- Routine
- Navigation
- Door
- Action
- Movement-Fallback

---

## 7.4 Restore-Navigation-Test

```text
1. NPC während Bewegung stoppen
2. Server neu starten
3. NPC relinken lassen
```

Erwartung:

```text
alte Route wird nicht fortgesetzt
remainingMs wird nicht resumed
Routine entscheidet Ziel neu
```

---

## 7.5 Log-Spam-Test

```text
1. NPC in NEEDS_RELINK/MISSING_ENTITY bringen
2. Server 30 Sekunden laufen lassen
3. Logs prüfen
```

Erwartung:

```text
keine Tick-Flut
Logs höchstens über Cooldown
Erfolgslogs sichtbar
```

---

## 7.6 Multi-NPC-Test

```text
1. 2–4 gleiche NPCs nahe beieinander spawnen
2. state.json prüfen: jede npcId/entityUuid eindeutig
3. Server stoppen
4. Server starten
5. alle NPCs prüfen
```

Erwartung:

```text
kein falscher Relink
kein Duplicate
kein Entity-Verlust
kein blindes Anchor-Binden
AMBIGUOUS blockiert, falls nicht eindeutig
```

Wichtig:

Nach der Korrektur von Step 6 darf kein Log erscheinen:

```text
Reloading nonexistent role KeystoneNPC_
```

---

## 7.7 Chunk-Gate-Test

```text
1. NPC weit entfernt platzieren
2. Server stoppen
3. Server starten
4. Nicht zum NPC gehen
5. Logs prüfen
```

Erwartung:

```text
kein Auto-Respawn im ungeladenen Chunk
Status bleibt NEEDS_RELINK
kein Tick-Spam
```

Dann:

```text
6. Zum NPC gehen
7. Chunk laden lassen
```

Erwartung:

```text
UUID-Relink oder kontrollierter Respawn
kein Duplicate
```

---

## 7.8 Save/Dirty-Test

```text
1. NPC spawnen
2. Marker setzen
3. Relink auslösen
4. Statusänderung auslösen
5. 10 Sekunden warten
6. state.json prüfen
```

Erwartung:

```text
echte Änderungen gespeichert
kein Save pro Tick
entityRef nicht gespeichert
Runtime-Navigation nicht gespeichert
npcId/entityUuid/Marker/state stabil
```

---

# 8. Bekannte Fehlerbilder und Diagnose

## 8.1 `/knpc list` zeigt NPC, aber ingame ist er weg

Mögliche Ursache:

```text
Record existiert
Live-Entity existiert nicht
```

Prüfen:

- `entityUuid` in `state.json`
- Logs nach `RELINK_RETRY`
- Logs nach `RELINK_GIVEUP_MARKED_MISSING`
- Logs nach `Reloading nonexistent role`
- Logs nach `AUTO_RESPAWN_SKIPPED`
- Chunk-Gate blockiert?
- Entity durch alte dynamische Role verloren?

---

## 8.2 `Reloading nonexistent role KeystoneNPC_...`

Ursache:

Dynamisches `setRoleName(...)` wurde wieder eingeführt.

Sofortiger Fix:

- dynamisches RoleName-Setzen entfernen
- Engine Role muss echte Role aus `Server/NPC/Roles/` bleiben
- Role-Prefix-Fallback deaktivieren
- Compile
- Server-Restart-Test

---

## 8.3 Duplicate nach Restart

Mögliche Ursachen:

- Auto-Respawn ohne Chunk-Gate
- Ambiguous ignoriert
- Anchor-Fallback bindet falsch
- UUID-Relink fehlgeschlagen, aber alter Chunk später geladen
- Dedupe zu aggressiv oder zu schwach

Prüfen:

- `spawnRequestsInFlight`
- `respawnRetryAtMs`
- `respawnFailureCounts`
- Chunk-Gate
- AMBIGUOUS-Logs
- UUID/Ownership-Prüfung

---

## 8.4 Log-Flut

Mögliche Ursachen:

- neues Event nicht im Cooldown
- direkter Logger außerhalb `logInfo(...)`
- Fehlerpfad tickt ohne Cooldown

Prüfen:

- Event-Key
- npcId in Log-Message
- Cooldown-Map
- `logSevere(...)` nur für echte kritische Fehler

---

# 9. Pflicht bei Änderungen an diesem Safety-System

Wenn eine AI dieses System bewusst verändert, muss sie diese Datei aktualisieren.

## 9.1 Update-Pflicht

Bei Änderung an:

- Live-Entity-Gate
- Restore-Navigation
- RelinkHelper
- Chunk-Gate
- Role/Identity-System
- Anchor-Fallback
- Save/Dirty-System
- Dedupe/Ownership
- JSON-/Role-Hierarchie

muss ergänzt werden:

```text
- Was wurde geändert?
- Welche alte Regel wurde ersetzt?
- Welche neue Regel gilt?
- Welche neuen Tests sind Pflicht?
- Welche Risiken entstehen?
- Wie wird Rollback verhindert?
- Wie wird Duplicate verhindert?
- Wie wird falscher Relink verhindert?
```

---

## 9.2 Neue Tests eintragen

Jede neue Safety-Funktion braucht:

- Compile-Test
- mindestens einen Restart-Test
- mindestens einen Negativ-Test
- Log-Erwartung
- Persistenz-Erwartung

---

## 9.3 Kein stilles Überschreiben

Eine AI darf diese Datei nicht still ignorieren.

Wenn ein Patch eine Regel aus dieser Datei verletzt, muss sie explizit berichten:

```text
Diese Änderung verletzt Regel X.
Grund:
Alternative:
Benötigte Anpassung der Kontroll-Datei:
Neue Tests:
```

---

# 10. Aktueller validierter Abschlussstand

Diese Safety-Blöcke gelten als validiert:

```text
Step 1: Live-Entity-Gate
Step 2: Restore-Navigation löschen
Step 3: Log-Cooldown
Step 4: RelinkHelper mit UUID-Hard-Match
Step 5: Chunk-Gate vor Auto-Respawn
Step 6: dynamischer Role-Prefix über setRoleName deaktiviert
Step 7: Anchor-Fallback nur letzter Fallback, AMBIGUOUS blockiert
Step 8: Save/Dirty-System mit diff-basierter Dirty-Markierung
Step 9: Load/Save-Failure blockiert destruktive Overwrites, Dirty-Reset nur nach echtem Save
Step 10: PENDING stoppt Fallbacks; respawnAfterRestart und Position-Safety sind harte Gates
Step 11: Marker-Fallback read-only vs mutierend getrennt; Remove/Clear-Orphan und ACTIVE-Bind gehärtet
Step 12: Reconcile-/Cleanup-Regeln für MarkerAssignments in read-only Pfaden finalisiert
```

Wichtigster finaler Architekturentscheid:

```text
Hytale Engine-Role bleibt echte Role.
Keystone NPC Identity bleibt Mod-/state.json-Identität.
Kein KeystoneNPC_<npcId>_<roleId>_Role über setRoleName.
```

---

# 11. Kurze Pflicht-Checkliste für jede spätere AI

Vor Abschluss eines Patches muss beantwortet werden:

```text
[ ] Compile grün?
[ ] Kein Movement ohne EntityRef?
[ ] Keine alte Navigation nach Restart?
[ ] Kein Auto-Respawn ohne Chunk-Gate?
[ ] Kein Auto-Respawn ohne bekannte finite currentPosition?
[ ] Kein dynamisches setRoleName mit KeystoneNPC_?
[ ] UUID-Relink vergleicht Live-UUID hart?
[ ] RelinkOutcome.PENDING stoppt weiterhin Fallback/Spawn im Zyklus?
[ ] Anchor-Fallback blockiert AMBIGUOUS?
[ ] respawnAfterRestart=false blockiert Auto-Respawn zuverlässig?
[ ] Keine unclaimed Entity blind gelöscht?
[ ] Kein Save pro Tick?
[ ] Load-Failure/Partial-Load blockiert automatische Overwrite-Saves?
[ ] Save-Failure zählt nie als Erfolg?
[ ] Dirty wird nur nach echtem Save gelöscht?
[ ] Marker read-only Pfade mutieren keine Assignments?
[ ] Marker-Mutation bleibt auf Spawn/Admin/Repair/Cleanup begrenzt?
[ ] resolveRequiredMarkerWithFallbackAssigning/resolveRequiredMarkerWithFallback bleiben entfernt?
[ ] getNextAvailable bleibt deprecated und wird nicht in read-only Pfaden verwendet?
[ ] Reconcile mutiert nicht in Load/Restore/Validation/Diagnose/Tick?
[ ] Reconcile setzt in read-only Pfaden weder stateDirty noch Save-Trigger?
[ ] entityRef nicht persistiert?
[ ] Runtime-Navigation nicht persistiert?
[ ] state.json kompatibel?
[ ] Keine neue Log-Flut?
[ ] Falls Safety-Regel geändert: Diese Datei aktualisiert?
```

---

# 12. Empfohlene Commit-Regel

Safety-Patches getrennt committen.

Empfohlene Commit-Namen:

```text
Stabilize NPC restart relink and respawn safety
Guard auto respawn behind loaded chunk check
Disable dynamic runtime role names for NPC reload safety
Harden NPC state dirty persistence
```

Keine großen JSON-/Role-Strukturänderungen im selben Commit wie Safety-Fixes, außer die Änderung ist ausdrücklich Teil des Fixes.

---

ABSCHLUSS

---

# 13. AI-Ergänzung aus Gesprächskontext

Diese Punkte wurden im Gespräch als besonders fehleranfällig erkannt und müssen bei späteren Patches aktiv mitgeprüft werden:

- **Unrelated Refactors dürfen Safety nicht nebenbei verändern.** Änderungen an JSON-Struktur, Roles, Registry, Commands oder Ressourcen dürfen Live-Entity-Gate, Relink, Chunk-Gate, Dirty-Save und Rollback nicht unbemerkt beeinflussen.

- **Hytale-Engine-Felder dürfen nicht als Keystone-Metadaten missbraucht werden.** Besonders `roleName` darf nicht für dynamische Keystone-Identität wie `KeystoneNPC_<npcId>_<roleId>_Role` genutzt werden, solange diese Rollen nicht echte Hytale-Role-Assets sind.

- **Records und Live-Entities sind getrennt zu behandeln.** `/knpc list` beweist nur, dass Records existieren; es beweist nicht, dass die Live-Entity geladen, sichtbar oder korrekt relinkt ist.

- **Restore/Respawn-Änderungen müssen immer gegen Duplikate und Entity-Verlust geprüft werden.** Besonders gefährlich sind Änderungen, die Auto-Respawn vor Relink, vor Chunk-Gate oder trotz `AMBIGUOUS` erlauben.

- **Wenn eine dieser Regeln bewusst geändert wird, muss diese Kontroll-Datei sofort aktualisiert werden.** Die Änderung braucht dann neue Review-Fragen, neue Negativ-Tests und eine klare Begründung, warum die neue Architektur sicherer oder notwendig ist.


# Ende der Kontroll-Datei
