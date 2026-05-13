# Gesamtbericht — State / Load / Marker / Position / Observability Safety Patch

**Zeitpunkt:** 2026-05-13 04:22:00 CEST  
**Thema:** P0–P3 Safety-Härtung für NPCMod / KeystoneNPC  
**Scope:** Agent Steps 1–10  
**Ergebnis:** FINAL PASS  
**Commitfähig:** Ja, sofern `mvn -q -DskipTests test-compile` im echten Projekt erfolgreich war.

---

## 1. Kurzfazit

Der Patch härtet die NPCMod an mehreren kritischen Stellen.

Wichtigste Ergebnisse:

- Kaputte `state.json` wird nicht mehr als gültiger leerer State behandelt.
- Einzelne kaputte Records können isoliert übersprungen werden.
- Partial-Load überschreibt keine kaputten/skipped Daten automatisch.
- Save-Fehler zählen nicht mehr als Erfolg.
- Dirty wird nur nach echtem Save gelöscht.
- Remove/Clear erzeugt keine Orphans durch unsichere Entity-Removal.
- `ACTIVE` wird nicht mehr blind an fremde Entities gebunden.
- `MISSING_ENTITY` bleibt sticky.
- `RelinkOutcome.PENDING` stoppt schwächere Fallbacks im selben Durchlauf.
- `respawnAfterRestart` ist echtes opt-in und erreicht die Auto-Respawn-Policy.
- Marker-Fallbacks sind in Runtime-Pfaden read-only.
- Restore/Tick/Diagnose mutieren keine MarkerAssignments mehr.
- Fehlende Position wird nicht mehr als echte `(0,0,0)`-Position behandelt.
- Runtime-States wie `WALKING_*` werden nicht mehr autoritativ gespeichert.
- Dry-run und Logs melden PENDING/AMBIGUOUS korrekt und ohne Tick-Spam.

---

## 2. Geänderte / betroffene Dateien

### 2.1 `JsonFileStateStore.java`

Diese Datei ist der Kern für sichere `state.json`-Persistenz.

#### Load-Failure wird nicht mehr als Empty-State behandelt

Vorher:

```text
kaputte state.json
→ Load-Fehler
→ PluginState.empty()
→ setup konnte leer speichern
```

Problem:

Eine kaputte Datei konnte wie ein gültiger leerer Erststart wirken.

Jetzt:

```text
Datei fehlt      -> gültiger leerer Erststart
Datei kaputt     -> loadSuccessful=false
blank/null/error -> loadSuccessful=false
```

`load()` gibt bei echtem Failure nicht mehr still `PluginState.empty()` zurück.

---

#### Partial-Load eingeführt

Einzelne kaputte Records brechen nicht mehr den kompletten Load ab.

Jetzt:

```text
guter Record -> wird geladen
kaputter Record -> wird geskippt und geloggt
partialLoad=true
kein Auto-Save
```

Dadurch bleiben gute Daten nutzbar, ohne kaputte/skipped Daten automatisch aus `state.json` zu löschen.

---

#### Invalide Marker und NPCs isoliert

Isoliert behandelt werden:

```text
invalid MarkerType
invalid Marker-Position
invalid Marker-Eintrag
invalid NpcState
invalid EntityStatus
fehlende/null/non-finite currentPosition
kaputter NPC-Eintrag
```

Gute Records bleiben trotzdem ladbar.

---

#### Fehlende Position wird nicht zu Origin repariert

Vorher konnte ein fehlendes `currentPosition` indirekt zu `(0,0,0)` werden.

Jetzt:

```text
currentPosition fehlt -> Position unbekannt
currentPosition invalid -> Position unbekannt / Record skip je nach Pfad
kein stiller Origin-Fallback
```

Beim Speichern wird Position nur geschrieben, wenn sie als bekannt gilt.

---

#### Runtime-State wird vor Save normalisiert

Nicht mehr autoritativ gespeichert:

```text
WALKING_TO_DOOR
WALKING_TO_CHEST
WALKING_TO_FOOD
WALKING_TO_WORK
WALKING_TO_CHILL
WALKING_TO_BED
PAUSED_MISSING_MARKER
null
```

Normalisierung:

```text
WALKING_TO_DOOR  -> OPENING_DOOR
WALKING_TO_CHEST -> USING_CHEST
WALKING_TO_FOOD  -> EATING
WALKING_TO_WORK  -> WORKING
WALKING_TO_CHILL -> CHILLING
WALKING_TO_BED   -> SLEEPING
PAUSED_MISSING_MARKER -> IDLE
null -> IDLE
```

Stabile logische States bleiben persistierbar.

---

### 2.2 `KeystoneNpcPlugin.java`

#### Globales Load-Failure-Flag

Bei kaputtem Load:

```text
stateLoadFailed=true
kein Restore
kein Auto-Save
spätere Saves werden blockiert
```

Damit kann `shutdown()` keine kaputte Datei mit leerem State überschreiben.

---

#### Partial-Load-Save-Block

Bei partialLoad:

```text
gute Records dürfen restored werden
aber kein Auto-Save
spätere automatische Saves werden blockiert
```

Das verhindert:

```text
1 kaputter NPC wird geskippt
→ State wirkt bereinigt
→ Save löscht kaputten Record aus Datei
```

---

#### Save-Failure sichtbar

`saveStateSafely()` gibt nur `true` zurück, wenn wirklich gespeichert wurde.

Bei Fehler:

```text
return false
Log sichtbar
Dirty darf nicht gelöscht werden
```

---

### 2.3 `NpcRoutineRunner.java`

Diese Datei wurde in mehreren Safety-Bereichen gehärtet.

#### Remove-Orphan-Safety

`removeNpc(...)` löscht Records nicht mehr, wenn Entity-Removal unsicher ist.

Sicheres Prinzip:

```text
Entity bleibt + Record bleibt = sicher
Entity bleibt + Record gelöscht = verboten
```

Record-Delete ist nur erlaubt, wenn das Removal-Outcome sicher ist.

---

#### Entity-Identity bleibt bei Removal-Fehler erhalten

Vorher konnte Identity zu früh gelöscht werden.

Jetzt:

```text
Removal unsicher / unbestätigt
→ entityUuid bleibt
→ entityRef bleibt, falls vorhanden
→ entityStatus wird nicht falsch bereinigt
→ Record bleibt
```

---

#### Safe ACTIVE Binding

`linkEntityRef(...)` darf keine fremde Entity blind auf `ACTIVE` setzen.

Neues Gate:

```text
Live-UUID lesbar
persistedUuid vorhanden
persistedUuid == liveUuid
UUID nicht fremd claimed
Ref nicht fremd claimed
NPC-Komponente vorhanden
Role passt
erst dann ACTIVE
```

Dadurch kann keine fremde gleiche-Role-Entity als eigener NPC gebunden werden.

---

#### MISSING_ENTITY bleibt sticky

Restore-Regel:

```text
DISABLED bleibt DISABLED
MISSING_ENTITY bleibt MISSING_ENTITY
andere Zustände mit entityUuid -> NEEDS_RELINK
```

Live-Entity-Gate setzt `MISSING_ENTITY` nicht mehr zurück auf `NEEDS_RELINK`.

---

#### PENDING stoppt Fallbacks

Wenn UUID-Relink `RelinkOutcome.PENDING` liefert:

```text
kein RolePrefix-Fallback
kein Anchor-Fallback
kein Auto-Respawn im selben Durchlauf
continue / stop
```

Das verhindert Fallback-Kaskaden nach `RELINK_GIVEUP_MARKED_MISSING`.

---

#### respawnAfterRestart erreicht Auto-Respawn-Policy

Auto-Respawn ist jetzt opt-in:

```text
global false -> blockiert alles
global true + JSON true -> erlaubt Prüfung
global true + JSON false/missing/null -> blockiert
```

`DISABLED` und `AMBIGUOUS` bleiben blockierend.

---

#### Position-Safety

Spawn und Chunk-Gate verwenden dieselbe sichere Positionsauflösung.

Nicht mehr erlaubt:

```text
fehlende Position -> (0,0,0)
invalid Position -> Spawn bei Origin
Chunk-Gate prüft andere Position als Spawn nutzt
```

Jetzt:

```text
Position bekannt + finite -> darf geprüft werden
Position unbekannt/invalid -> Auto-Respawn blockiert
```

---

#### MarkerAssignments nicht mehr in Restore/Tick mutieren

Restore/Tick nutzen nur noch read-only Diagnose.

Nicht mehr erlaubt:

```text
Restore löscht MarkerAssignment
Tick löscht MarkerAssignment
Diagnose ersetzt MarkerAssignment
Relink-Anker weist Marker neu zu
```

Mutierende Marker-Reconcile ist nur noch in expliziten Schreibkontexten erlaubt.

---

#### Log-Cooldown / Dry-run-Zähler

Relink-/Missing-/Respawn-nahe Logs werden gedrosselt.

Dry-run unterscheidet jetzt sauber:

```text
PENDING -> blocked / pending
AMBIGUOUS -> unsafe / blocked
would spawn -> nur wenn kein pending/ambiguous blockiert
```

---

### 2.4 `RelinkWorkflowService.java`

#### UUID-lose Anchor-Kandidaten blockiert

Anchor-Relink akzeptiert keine Kandidaten ohne lesbare Live-UUID.

Vor ACTIVE gilt:

```text
Live-UUID muss lesbar sein
Ownership muss passen
AMBIGUOUS blockiert
```

Bei erfolgreichem Anchor-Relink wird die gelesene Live-UUID persistiert.

---

#### Role-Prefix bleibt deaktiviert

Nicht reaktiviert:

```text
KeystoneNPC_<npcId>_<roleId>_Role
setRoleName("KeystoneNPC_...")
Role-Prefix-Fallback
```

Das ist wichtig, weil Hytale `roleName` als echte Engine-Role behandelt.

---

#### Marker-Anker read-only

Marker-Anker für Relink/Dedupe werden jetzt nur read-only aufgelöst.

Das verhindert:

```text
Relink-Diagnose
→ Marker fehlt
→ Ersatzmarker wird automatisch zugewiesen
→ state.json speichert falschen Marker
```

---

### 2.5 `MarkerResolver.java`

#### Read-only und assigning getrennt

Neue klare Trennung:

```text
resolveRequiredMarkerReadOnly(...)
→ liest nur bestehende Assignments
→ mutiert nichts

resolveRequiredMarkerWithFallbackAssigning(...)
→ darf bewusst zuweisen
→ nur in Spawn/Admin/Repair-Kontext
```

Die alte Legacy-Methode bleibt nur noch begrenzt/kompatibel und darf nicht mehr in Runtime-Pfaden verwendet werden.

---

### 2.6 `IdleMarkerService.java`

Idle-/Restore-/Tick-Pfade nutzen jetzt read-only Marker-Resolve.

Geschützt werden:

```text
Restore-Position
Authoritative Idle Position
Idle-State-Check
Marker-Kandidaten für Diagnose
```

Diese Pfade verändern keine MarkerAssignments mehr.

---

### 2.7 `StateTargetingService.java`

Routine-Zielauflösung, Required-Marker-Checks und Diagnose laufen read-only.

Damit gilt:

```text
Tick-Ziel suchen -> keine Marker-Zuweisung
Validation -> keine Marker-Zuweisung
Diagnose -> keine Marker-Zuweisung
```

---

### 2.8 `RespawnRecoveryService.java`

Respawn-/Restore-Diagnose nutzt read-only Marker-Resolve.

Dadurch kann Recovery-Prüfung keine MarkerAssignments nebenbei verändern.

---

### 2.9 `MarkerClearCommand.java`

#### Referenzierte Marker werden geschützt

Mass-Clear blockiert, wenn Marker noch von NPCs referenziert werden.

Dadurch wird verhindert:

```text
Marker löschen
→ NPC behält tote Assignment-Referenz
→ Routine/Respawn kaputt
```

#### Save-Failure mit Rollback

Wenn Save fehlschlägt:

```text
Runtime-Markerzustand wird wiederhergestellt
keine falsche Erfolgsmeldung
```

---

### 2.10 `MarkerCommandGroup.java`

Marker-Clear ist in die sicherere Command-Struktur eingebunden.

Keine destructive Mass-Clear-Aktion ohne Referenzprüfung.

---

### 2.11 `NpcRemoveCommand.java`

Remove-Command nutzt `saveStateSafely()` mit Ergebnisprüfung.

Bei Save-Fehler:

```text
keine falsche "saved state"-Meldung
User bekommt sichtbare Fehlermeldung
```

---

### 2.12 `NpcClearCommand.java`

Clear-Command nutzt ebenfalls `saveStateSafely()`.

Bei Save-Fehler:

```text
Runtime kann geändert sein
Persistenz eventuell nicht
User wird klar informiert
```

---

### 2.13 `NpcRespawnMissingCommand.java`

Dry-run ist jetzt klarer.

Neu sichtbar:

```text
PENDING ist Blocker
AMBIGUOUS ist unsafe/blocked
would spawn nur bei wirklich spawnbarem Fall
pendingBlocked in Summary
```

---

### 2.14 `PersistenceProfile.java`

`respawnAfterRestart` wird null-safe ausgewertet.

Regel:

```text
true  -> erlaubt Auto-Respawn-Policy-Prüfung
false -> blockiert
null/missing -> blockiert
```

---

### 2.15 `NpcTemplateResolver.java`

Persistence-Profil wird pro Role/Definition ausgewertet.

Der Wert erreicht `NpcRoutineRunner` und damit die Auto-Respawn-Entscheidung.

---

### 2.16 `NpcRecord.java`

Position hat jetzt einen expliziten Known-Status.

Dadurch ist unterscheidbar:

```text
Position fehlt
vs.
echte Position ist (0,0,0)
```

Das schützt Auto-Respawn vor Origin-Fallbacks.

---

## 3. Logische Wirkung des gesamten Patches

### Fall 1: Kaputte `state.json`

Ablauf:

```text
state.json existiert
Datei ist kaputt / unlesbar / leer-invalid
LoadResult = failure
stateLoadFailed = true
kein Restore
kein Auto-Save
shutdown überschreibt nicht leer
```

Bewertung: korrekt.

---

### Fall 2: Einzelner kaputter NPC-Record

Ablauf:

```text
state.json ist grundsätzlich lesbar
1 NPC kaputt
4 NPCs gültig
4 NPCs werden geladen
1 NPC wird geskippt
partialLoad=true
kein Auto-Save
```

Bewertung: korrekt.

---

### Fall 3: Save schlägt fehl

Ablauf:

```text
saveStateSafely() versucht Save
Save-Fehler tritt auf
return false
Dirty bleibt erhalten
keine falsche Erfolgsmeldung
```

Bewertung: korrekt.

---

### Fall 4: Remove mit unsicherer Entity-Removal

Ablauf:

```text
removeNpc(...)
Entity-Removal nicht sicher bestätigbar
Record wird nicht gelöscht
Identity bleibt erhalten
User/Log sieht Blocker
```

Bewertung: korrekt.

---

### Fall 5: Fremde Entity mit gleicher Role

Ablauf:

```text
NPC hat persistedUuid A
Live-Kandidat hat UUID B
Role passt vielleicht
UUID mismatch
kein ACTIVE
keine Identity-Mutation
```

Bewertung: korrekt.

---

### Fall 6: MISSING_ENTITY nach Restart

Ablauf:

```text
state.json lädt MISSING_ENTITY
Status bleibt MISSING_ENTITY
kein Rückfall zu NEEDS_RELINK
kein automatischer Relink-Loop
Policy entscheidet später sicher
```

Bewertung: korrekt.

---

### Fall 7: RelinkOutcome.PENDING

Ablauf:

```text
UUID-Relink liefert PENDING
aktueller Durchlauf stoppt
kein RolePrefix
kein Anchor
kein Auto-Respawn im selben Durchlauf
```

Bewertung: korrekt.

---

### Fall 8: respawnAfterRestart fehlt oder ist false

Ablauf:

```text
global true
JSON respawnAfterRestart fehlt/false/null
Auto-Respawn blockiert
Status bleibt sicher
```

Bewertung: korrekt.

---

### Fall 9: Marker fehlt beim Restart

Ablauf:

```text
Restore liest MarkerAssignments read-only
Marker fehlt
kein Ersatzmarker wird zugewiesen
kein Save mit falschem Marker
Diagnose/Recovery meldet Problem
```

Bewertung: korrekt.

---

### Fall 10: currentPosition fehlt

Ablauf:

```text
currentPosition fehlt
hasKnownCurrentPosition=false
Auto-Respawn blockiert
kein Spawn bei (0,0,0)
```

Bewertung: korrekt.

---

### Fall 11: NPC war beim Save in WALKING_TO_BED

Ablauf:

```text
Runtime-State WALKING_TO_BED
Save normalisiert zu SLEEPING
Restart startet nicht in transientem Laufzustand
Routine kann frisch entscheiden
```

Bewertung: korrekt.

---

### Fall 12: Dry-run bei PENDING oder AMBIGUOUS

Ablauf:

```text
PENDING -> pendingBlocked
AMBIGUOUS -> unsafe/blocked
kein irreführendes would spawn
```

Bewertung: korrekt.

---

## 4. Bewusst nicht geändert

Der Patch hat nicht angefasst:

```text
Marker-v2 als neues Feature
Door-System
Navigation-System
Animation-System
JSON-Roles / Engine-Roles
Dedupe-Löschlogik als neues System
Respawn-/Relink-Policy außerhalb der geprüften Gates
Chunk-Preloading
Worldgen / Settlement-Registrierung
Appearance-Apply-Logik
Combat-System
neue Hytale Metadata-/Component-ID-Lösung
```

Das ist gut, weil der Patch ein Safety-Patch bleibt und keine neuen Features vermischt.

---

## 5. Safety-Abgleich

### Geprüfte Baselines

Relevant waren besonders:

```text
AGENTS.md
npc_restart_relink_control.md
json_hierarchy.md
```

Wichtige eingehaltene Regeln:

```text
kein dynamisches setRoleName("KeystoneNPC_...")
kein Role-Prefix-Fallback
kein Blind-Relink
kein Auto-Respawn bei AMBIGUOUS
kein Save-Failure als Erfolg
kein Load-Failure als Empty-State
keine MarkerAssignment-Mutation in Restore/Tick/Diagnose
kein Auto-Respawn ohne bekannte finite Position
kein Tick-Log-Spam
```

---

## 6. Resthinweise vor Commit

### Compile

Agent meldete für alle Steps:

```text
mvn -q -DskipTests test-compile
erfolgreich
```

Hinweis:

In dieser Review-Umgebung wurde Compile nicht selbst ausgeführt, weil `mvn` nicht verfügbar war.

---

### Zusätzliche Doku-/Prompt-Dateien

Im Verlauf gab es bewusst vom Nutzer stammende Doku-/Prompt-Dateien.

Wichtig:

```text
Nicht automatisch als Scope-Fehler werten,
wenn sie bewusst vom Nutzer ins Projekt gelegt wurden.
```

Besonders:

```text
docs/feature_plans/JSON_structure_feature.md
```

soll laut Nutzer bleiben.

---

### Marker-v2 Folgepunkt

Marker-v2 ist noch nicht als neues Feature umgesetzt.

Aber dieser Patch bereitet Marker-v2 sicher vor:

```text
read-only Marker-Resolve in Runtime-Pfaden
mutierende Marker-Zuweisung nur in Spawn/Admin/Repair-Kontext
kein automatischer Marker-Ersatz beim Restart
kein automatisches Löschen alter MarkerAssignments im Tick
```

Für Marker-v2 später besonders prüfen:

```text
resolveRequiredMarkerWithFallback(...) nicht wieder in Restore/Tick/Diagnose/Relink-Anker nutzen
reconcile/cleanup darf MarkerAssignments nicht automatisch persistenzwirksam ändern
fehlender Marker bedeutet Diagnose/Repair, nicht Auto-Ersatz
```

---

## 7. Gesamtfazit

Der Patch ist logisch sauber.

Er löst mehrere alte Safety-Probleme:

```text
kaputte state.json
→ kein Empty-Overwrite

kaputter Einzelrecord
→ gute Records bleiben ladbar
→ kein Auto-Löschen skipped Records

unsichere Entity-Removal
→ kein Orphan durch Record-Delete

fremde Live-Entity
→ kein blindes ACTIVE-Bind

MISSING_ENTITY
→ bleibt sticky
→ kein Relink-/Fallback-Loop

respawnAfterRestart
→ echtes opt-in
→ globaler Kill-Switch bleibt stark

Marker-Fallback
→ Runtime read-only
→ keine falschen Marker in state.json

fehlende Position
→ kein Origin-Respawn

Runtime-State
→ keine transienten Restart-States

Dry-run/Logs
→ klarer, sicherer, weniger Spam
```

---

## 8. Ergebnis

```text
FINAL PASS
Commitfähig: ja
```
