# Gesamtbericht — Restart / Relink / Respawn Safety Patch

**Zeitpunkt:** 2026-05-13 01:21:14 CEST  
**Thema:** Restart-/Relink-/Respawn-Safety für NPCMod / KeystoneNPC  
**Ergebnis:** FINAL PASS  
**Commitfähig:** Ja, sofern `mvn -q -DskipTests test-compile` im echten Projekt erfolgreich war.

---

## 1. Kurzfazit

Der Patch behebt den Restart-/Relink-/Respawn-Kreislauf sauber.

Wichtigste Ergebnisse:

- `MISSING_ENTITY` bleibt sticky.
- `MISSING_ENTITY` wird nicht mehr automatisch zu `NEEDS_RELINK`.
- `respawnAfterRestart` wird aus JSON geladen.
- Auto-Respawn läuft nur über Policy + globale Freigabe + Chunk-Gate.
- Keine falsche Lumberjack-Zuordnung.
- Kein unsicherer Role-Prefix-Fallback.
- Keine Dedupe-Löschung nur wegen gleicher Role.
- Erfolgreicher Respawn wird sofort persistiert.
- Save-Fehler bleiben sichtbar und setzen Dirty nicht fälschlich zurück.

---

## 2. Geänderte / betroffene Dateien

### 2.1 `NpcRoutineRunner.java`

Diese Datei ist der Kern des Patches.

#### Restore-Logik repariert

Vorher:

```text
entityUuid vorhanden
→ immer NEEDS_RELINK
```

Problem:

Auch ein gespeicherter `MISSING_ENTITY` wurde beim Serverstart wieder zu `NEEDS_RELINK`.

Jetzt:

```text
DISABLED bleibt DISABLED
MISSING_ENTITY bleibt MISSING_ENTITY
ohne entityUuid -> MISSING_ENTITY
mit entityUuid und nicht MISSING_ENTITY -> NEEDS_RELINK
```

Damit ist `MISSING_ENTITY` beim Restore sticky.

---

#### `restoredNpcIds` eingeführt

Es gibt jetzt ein Runtime-Set für NPCs, die wirklich aus `state.json` geladen wurden.

Zweck:

```text
Auto-Respawn darf nur für restored Records laufen.
Nicht für frisch erzeugte NPCs.
Nicht für Commands.
Nicht für zufällige Runtime-Records.
```

Das Set wird beim Restore gefüllt und bei neu erzeugten oder gelöschten NPCs wieder bereinigt.

---

#### Relink-Zyklus bei `MISSING_ENTITY` gestoppt

Vorher konnte nach einem Relink-Giveup immer wieder ein neuer Relink-/Fallback-Zyklus starten.

Jetzt:

```text
Wenn Status MISSING_ENTITY:
kein automatischer UUID-Relink-Zyklus
kein Role-Prefix-Fallback
kein Anchor-Fallback im selben Loop
```

Dadurch entsteht kein `RELINK_GIVEUP_MARKED_MISSING`-Spam pro Tick.

---

#### Auto-Respawn-Policy eingebaut

Neue zentrale Entscheidung:

```text
passesRestartAutoRespawnPolicy(...)
```

Diese Methode entscheidet nur, ob Auto-Respawn erlaubt ist.

Sie führt selbst keinen Spawn aus.

Geprüft wird:

```text
world != null
npc != null
Status ist MISSING_ENTITY
Status ist nicht DISABLED
npcId ist stabil vorhanden
NPC wurde aus state.json restored
globaler Kill-Switch erlaubt Auto-Respawn
respawnAfterRestart für roleId ist true
roleId / Definition existiert
entityUuid ist vorhanden
entityUuid gehört keinem anderen aktiven NPC
requiredMarkers sind auflösbar
```

---

#### Auto-Respawn bleibt hinter mehreren Gates

Spawn passiert nur nach:

```text
MISSING_ENTITY
+ restoredNpcIds enthält npcId
+ globaler Auto-Respawn aktiv
+ respawnAfterRestart=true
+ gültige Role/Definition
+ gültige Marker
+ kein Ownership-Konflikt
+ Chunk-Gate bestanden
+ kein Spawn bereits in flight
```

Das verhindert Blind-Respawn.

---

#### Respawn nutzt bestehenden `NpcRecord`

Beim Respawn wird kein neuer NPC-Record erzeugt.

Dadurch bleiben stabil:

```text
npcId
roleId
npcName
markerAssignments
worldId
```

Nur die Live-Entity-Identity wird ersetzt.

---

#### Neue `entityUuid` wird korrekt übernommen

Nach erfolgreichem Spawn:

```text
neue Live-Entity wird gespawnt
entityRef wird gesetzt
entityUuid wird aus Live-Entity gelesen
entityUuid wird in den Record geschrieben
UUID wird hart gegen Live-UUID geprüft
entityStatus wird ACTIVE
```

Wenn die UUID fehlt oder nicht passt, wird der Spawn als Fehler behandelt.

---

#### `currentPosition` wird aktualisiert

Nach Spawn wird versucht, die echte Live-Position zu lesen.

Reihenfolge:

```text
Live-Transform / echte Entity-Position
Fallback: Spawn-Position
```

Damit bleibt `state.json` nach Respawn sinnvoll.

---

#### Sofortiger Save nach Auto-Respawn

Nach erfolgreichem Auto-Respawn wird sofort ein Save versucht.

Ziel:

```text
neue entityUuid
neuer ACTIVE-Status
aktuelle Position
```

sollen direkt in `state.json` landen.

Wenn Save klappt:

```text
stateDirty = false
nextDirtySaveAtMs = 0
```

Wenn Save fehlschlägt:

```text
stateDirty = true
Deferred Retry bleibt aktiv
```

---

#### Rollback bleibt intakt

Bei Spawn-Fehlern wird die alte Identity wiederhergestellt:

```text
alte entityUuid
alte entityRef, falls gültig
alter entityStatus
alter entityId
```

Wenn Respawn fehlschlägt, bleibt `MISSING_ENTITY` erhalten.

---

#### Role-Prefix bleibt deaktiviert

Die Methode für Runtime-Role-Zuweisung bleibt neutral:

```text
keine dynamische KeystoneNPC_<npcId>_<roleId>_Role
kein setRoleName mit Fake-Role
```

Das ist wichtig, weil Hytale `roleName` als echte Engine-Role behandelt.

---

### 2.2 `NpcTemplateResolver.java`

#### Persistence-Profil wird geladen

Der Loader liest jetzt das Persistence-Profil einer NPC-Definition.

Dadurch wird aus JSON verfügbar:

```text
respawnAfterRestart
```

#### Neuer Getter für Respawn-Policy

Es gibt sinngemäß:

```text
respawnAfterRestartEnabledForRole(roleId)
```

Dieser Wert wird in `NpcRoutineRunner` für die Auto-Respawn-Entscheidung genutzt.

#### Sicherer Default

Wenn kein Profil existiert oder das Feld fehlt:

```text
respawnAfterRestart = false
```

Auto-Respawn ist also opt-in, nicht opt-out.

---

### 2.3 `PersistenceProfile.java`

Neues Feld:

```text
respawnAfterRestart
```

Bedeutung:

```text
true  -> diese Role darf nach Restart kontrolliert auto-respawnen
false -> diese Role darf nicht auto-respawnen
fehlend -> false
```

---

### 2.4 JSON-Persistence-Profile

Betroffene Ressourcen:

```text
Server/NPC/Keystone/persistence/persistent_citizen.json
Server/NPC/Keystone/persistence/despawnable_hostile.json
```

#### `persistent_citizen.json`

Enthält:

```text
respawnAfterRestart=true
```

Das erlaubt kontrollierten Restart-Respawn für persistente Bürger-/Citizen-NPCs.

#### `despawnable_hostile.json`

Enthält:

```text
respawnAfterRestart=false
```

Hostile/despawnable NPCs werden dadurch nicht automatisch wiederhergestellt.

---

### 2.5 `JsonFileStateStore.java`

#### Save-Fehler werden nicht mehr geschluckt

Vorher wurden Save-Fehler intern geloggt, aber nicht zuverlässig nach oben gemeldet.

Problem:

```text
NpcRoutineRunner dachte: Save erfolgreich
obwohl state.json eventuell nicht geschrieben wurde
```

Jetzt:

```text
IOException -> IllegalStateException
RuntimeException -> wird weitergeworfen
LinkageError -> wird weitergeworfen
```

Dadurch kann der Runner korrekt erkennen:

```text
Save fehlgeschlagen
→ Dirty bleibt true
→ Deferred Retry bleibt aktiv
```

---

### 2.6 `RelinkWorkflowService.java`

Im finalen Verhalten wichtig geprüft:

#### Role-Prefix-Fallback bleibt neutralisiert

Kein aktiver unsicherer Fallback über:

```text
Lumberjack
roleId
KeystoneNPC_...
```

#### Anchor-Fallback bleibt letzter Fallback

Anchor wird nicht blind benutzt.

Wenn mehrere Kandidaten existieren:

```text
AMBIGUOUS
kein Relink
kein Respawn
kein Delete
```

#### Dedupe bleibt sicher

Keine Löschung nur wegen gleicher Role.

Gelöscht werden darf nur bei sicherem Same-Record-Beweis.

---

### 2.7 `npc_restart_relink_control.md`

Die Safety-Datei wurde am Ende passend nachgezogen.

Geändert wurde die Regel für:

```text
MISSING_ENTITY bleibt sticky
kein automatischer Rückfall zu NEEDS_RELINK
kein sofortiger neuer Relink-Zyklus nach RELINK_GIVEUP_MARKED_MISSING
Chunk-Gate respektiert sticky MISSING_ENTITY
```

Das ist korrekt, weil die Safety-Dateien erst am Ende des Gesamtpatches aktualisiert werden.

---

## 3. Logische Wirkung des gesamten Patches

### Fall 1: Alte Entity wird nach Restart gefunden

Ablauf:

```text
state.json lädt NPC
entityUuid vorhanden
Status wird NEEDS_RELINK
UUID-Relink findet Live-Entity
entityUuid wird bestätigt
entityRef wird gesetzt
Status wird ACTIVE
kein Ersatzspawn
```

Bewertung: korrekt.

---

### Fall 2: Alte Entity fehlt + `respawnAfterRestart=true`

Ablauf:

```text
state.json lädt NPC
Relink findet alte Entity nicht
NPC wird MISSING_ENTITY
kein endloser Relink-Zyklus
Policy prüft globalen Kill-Switch
Policy prüft respawnAfterRestart=true
Policy prüft Role, Marker, Ownership, Chunk
Spawn läuft
neue entityUuid wird gespeichert
Status wird ACTIVE
state.json wird sofort gespeichert
```

Bewertung: korrekt.

---

### Fall 3: Alte Entity fehlt + `respawnAfterRestart=false`

Ablauf:

```text
state.json lädt NPC
Relink findet alte Entity nicht
NPC wird MISSING_ENTITY
Policy sieht respawnAfterRestart=false
kein Auto-Respawn
Status bleibt MISSING_ENTITY
kein Tick-Spam
```

Bewertung: korrekt.

---

### Fall 4: Mehrere Lumberjacks nebeneinander

Ablauf:

```text
mehrere gleiche roleId
kein Role-only Relink
kein blinder Lumberjack-Fallback
Anchor nur wenn eindeutig
Ambiguous blockiert
keine Dedupe-Löschung wegen gleicher Role
```

Bewertung: korrekt.

---

### Fall 5: `DISABLED`

Ablauf:

```text
DISABLED wird beim Restore geschützt
kein Relink
kein Respawn
kein Statuswechsel
keine Routine
keine Navigation
```

Bewertung: korrekt.

---

## 4. Bewusst nicht geändert

Der Patch hat nicht angefasst:

```text
Marker-v2
Chunk-Preloading
neues Metadata-/Component-ID-System
neue Hytale Role-Dateien
Admin-Command-Refactor
Dedupe-Redesign
Door-System
Navigation-System
Appearance-System
Combat-System
Worldgen/Settlement-Registrierung
```

Das ist gut, weil der Patch auf Restart-/Relink-/Respawn-Safety begrenzt bleibt.

---

## 5. Resthinweise vor Commit

### Compile

Agent meldet:

```text
mvn -q -DskipTests test-compile
erfolgreich
```

Das wurde in meiner Umgebung nicht selbst geprüft, weil `mvn` dort nicht installiert war.

### Zusätzliche Doku-Dateien

Im ZIP lagen zusätzlich:

```text
docs/logic_analyzer/npc_restart_relink_control_logic_errors.md
docs/logic_analyzer/npc_restart_relink_control_logic_errors2.md
docs/logic_analyzer/npc_restart_relink_control_logic_errors3.md
```

Vor Commit kurz entscheiden:

```text
bewusst mitcommiten
oder aus dem Commit rauslassen
```

---

## 6. Gesamtfazit

Der Patch ist logisch sauber.

Er löst das Hauptproblem:

```text
NPC nach Restart fehlt
→ MISSING_ENTITY bleibt stabil
→ kein Relink-Spam
→ kein falscher Lumberjack-Relink
→ Auto-Respawn nur kontrolliert
→ neue Entity wird korrekt gespeichert
```

---

## 7. Ergebnis

```text
FINAL PASS
Commitfähig: ja
```
