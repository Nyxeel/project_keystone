



















# Restart / Relink / Respawn Control Gate — Logic Error Report

**Projekt:** Hytale-Mod `NPCMod / KeystoneNPC`
**Modus:** PLAN-/ANALYSE-MODE
**Quelle:** hochgeladene Codebase `NPCMod.zip`
**Datum:** 2026-05-13
**Ziel:** Strenge Logikprüfung des Restart-/Relink-/Respawn-Control-Gates für persistierte NPCs.

---

## 0. Ergebnis in einem Satz

**Gesamturteil: FAIL.**

Die wichtigsten Sicherheitsmechanismen sind teilweise vorhanden, aber es gibt mehrere echte Logikfehler:

1. `MISSING_ENTITY` ist nicht vollständig sticky.
2. Ein persistierter `MISSING_ENTITY`-NPC mit vorhandener `entityUuid` wird beim Restore wieder zu `NEEDS_RELINK` normalisiert.
3. `RelinkOutcome.PENDING` wird im Caller nicht als harter Stop behandelt.
4. Nach `RELINK_GIVEUP_MARKED_MISSING` kann erneut ein Relink-Zyklus starten.
5. `respawnAfterRestart` existiert in JSON und Java-Record, wird aber nicht bis zur Auto-Respawn-Entscheidung genutzt.
6. Auto-Respawn hängt aktuell nur an einer globalen System-Property.
7. Safety-Datei und Code widersprechen sich beim Chunk-Gate-Statusverhalten.
8. Compile-Gate konnte in der Analyseumgebung nicht ausgeführt werden, weil `mvn` nicht installiert ist.

---

## 1. Geprüfte Baselines

### 1.1 Gefundene Safety-Dateien

Gefunden:

- `docs/safety/npc_restart_relink_control.md`
- `docs/safety/json_hierarchy.md`

Nicht gefunden:

- `AGENTS.md`

Bewertung:

- Die zwei Safety-Dateien sind vorhanden und enthalten relevante Baseline-Regeln.
- `AGENTS.md` konnte in der ZIP nicht gefunden werden und konnte daher nicht als verbindliche Baseline geprüft werden.

---

## 2. Geprüfte Codepfade

Gezielt geprüft wurden:

- `JsonFileStateStore`
- `NpcRoutineRunner.restore(...)`
- `NpcRoutineRunner.spawnRestoredNpcs(...)`
- `NpcRoutineRunner.passesAutoRespawnChunkGate(...)`
- `NpcRoutineRunner.passesLiveEntityGate(...)`
- `RelinkWorkflowService.tryRelinkEntityRef(...)`
- `RelinkWorkflowService.tryRolePrefixRelinkEntityRef(...)`
- `RelinkWorkflowService.tryAnchorRelinkEntityRef(...)`
- `RespawnPolicyConfig`
- `PersistenceProfile`
- `persistent_citizen.json`
- `despawnable_hostile.json`
- relevante Safety-Abschnitte zu Restart, Relink, MISSING_ENTITY, Chunk-Gate, Role-Prefix, dynamischem `setRoleName`

---

## 3. PASS/FAIL Gesamturteil

| Bereich | Urteil | Kurzbegründung |
|---|---:|---|
| Restore alter Runtime-EntityRef | PASS | `entityRef` und `entityId` werden beim Restore gelöscht. |
| Alte Navigation nach Restart | PASS | Restore ruft `resetNavigationForRetarget(...)` und `normalizeRestorePosition(...)` auf. |
| DISABLED-Schutz | PASS | `DISABLED` wird in Restore, Tick und Spawn-Restore-Pfad überwiegend sauber geschützt. |
| UUID-Hard-Relink | PASS | UUID wird nur bei echtem Live-Match akzeptiert. |
| Dynamisches `setRoleName("KeystoneNPC_...")` | PASS | Kein aktiver Java-Treffer gefunden; Role-Prefix-Fallback ist neutralisiert. |
| Anchor-Fallback AMBIGUOUS | PASS mit Warnung | Mehrdeutige Anchor-Kandidaten blockieren. Warnung: Anchor kann nach `PENDING` zu früh laufen. |
| Dedupe nur wegen gleicher Role | PASS | Kein blindes Löschen nur wegen gleicher Role gefunden. |
| `MISSING_ENTITY` sticky | FAIL | Restore normalisiert vorhandenes `MISSING_ENTITY` mit UUID zu `NEEDS_RELINK`. |
| Relink-Pending-Stop | FAIL | `PENDING` wird im Caller nicht als Stop behandelt. |
| Relink-Giveup-Endzustand | FAIL | Nach Giveup können neue Retry-Zyklen entstehen. |
| `respawnAfterRestart` | FAIL | Feld existiert, steuert aber die Respawn-Entscheidung nicht. |
| Auto-Respawn-Policy | FAIL | Nur globale Property entscheidet. JSON-Policy wird ignoriert. |
| Safety-Datei vs Code | PARTIAL/FAIL | Chunk-Gate-Regel in Safety-Datei ist teilweise veraltet/widersprüchlich. |
| Compile-Gate | PARTIAL | `mvn` war in der Analyseumgebung nicht installiert. |

---

## 4. Gefundene Logikfehler

---

### Fehler 1 — Persistiertes `MISSING_ENTITY` wird beim Restore wieder zu `NEEDS_RELINK`

**Risiko:** hoch
**Kategorie:** Status-Sticky-Bug / Restart-State-Verlust
**Betroffene Datei:** `src/main/java/keystone/npc/routine/NpcRoutineRunner.java`
**Betroffene Methode:** `restore(List<NpcRecord> loaded)`
**Betroffene Zeilen:** ca. `273–330`, besonders `311–325`

#### Aktueller Codefluss

Beim Laden aus `state.json` setzt `JsonFileStateStore` zunächst den gespeicherten `entityStatus` korrekt in den `NpcRecord`:

- vorhandener gültiger Status wird übernommen,
- invalides Statusfeld wird defensiv normalisiert,
- fehlender Status wird anhand von `entityUuid` auf `MISSING_ENTITY` oder `NEEDS_RELINK` gesetzt.

Danach überschreibt `NpcRoutineRunner.restore(...)` diesen Status erneut:

```text
if npc.entityStatus != DISABLED:
    if entityUuid fehlt:
        entityStatus = MISSING_ENTITY
    else:
        entityStatus = NEEDS_RELINK
```

Das bedeutet konkret:

```text
state.json:
  entityStatus = MISSING_ENTITY
  entityUuid = "alte-uuid"

Server-Restart:
  JsonFileStateStore liest MISSING_ENTITY korrekt
  NpcRoutineRunner.restore überschreibt auf NEEDS_RELINK
```

#### Warum das falsch ist

Wenn `MISSING_ENTITY` als sticky Recovery-Zustand gelten soll, darf ein Restart diesen Zustand nicht automatisch wieder in `NEEDS_RELINK` verwandeln.

Aktuell wird der gespeicherte Status nicht als fachlicher Zustand respektiert, sondern nur die Existenz einer UUID entscheidet.

Dadurch verliert der NPC seinen terminalen Missing-Zustand nach jedem Serverstart.

#### Verstoß gegen Safety-Ziel

Verletzt bzw. widerspricht dem gewünschten Ziel:

- `MISSING_ENTITY darf nicht automatisch pro Tick zu NEEDS_RELINK werden.`
- `MISSING_ENTITY soll sticky werden, bis Relink oder Respawn erfolgreich war.`
- `DISABLED bleibt DISABLED.` wird zwar eingehalten, aber die gleiche Schutzlogik fehlt für `MISSING_ENTITY`.

#### Exakter fehlerhafter Statusfluss

```text
MISSING_ENTITY aus state.json
-> restore(...)
-> entityUuid vorhanden
-> NEEDS_RELINK
-> spawnRestoredNpcs(...)
-> neuer Relink-Versuch
```

#### Erwarteter Statusfluss

```text
MISSING_ENTITY aus state.json
-> restore(...)
-> bleibt MISSING_ENTITY
-> kein normaler Relink-Loop
-> nur expliziter Recovery-Pfad darf weiterarbeiten
```

#### Minimaler Fix-Vorschlag

In `NpcRoutineRunner.restore(...)` muss der vorhandene Status respektiert werden.

Minimalregel:

```text
DISABLED bleibt DISABLED.
MISSING_ENTITY bleibt MISSING_ENTITY.
ACTIVE mit persistierter UUID wird zu NEEDS_RELINK.
Fehlender/inkonsistenter Status wird defensiv normalisiert.
```

Nur `ACTIVE` oder unbekannte/alte Zustände sollten bei vorhandener UUID zu `NEEDS_RELINK` werden.

#### Muss Safety-Datei angepasst werden?

**Ja.**

`safety/npc_restart_relink_control.md` sollte explizit festhalten:

```text
Persistiertes MISSING_ENTITY bleibt beim Restore MISSING_ENTITY, auch wenn entityUuid vorhanden ist.
Nur ACTIVE/unklarer Restore-Status mit entityUuid wird zu NEEDS_RELINK normalisiert.
```

---

### Fehler 2 — `RelinkOutcome.PENDING` wird im Caller nicht als Stop behandelt

**Risiko:** hoch
**Kategorie:** Fallback-Reihenfolge / Relink-Control-Gate
**Betroffene Datei:** `src/main/java/keystone/npc/routine/NpcRoutineRunner.java`
**Betroffene Methode:** `spawnRestoredNpcs(String trigger)`
**Betroffene Zeilen:** ca. `980–1029`

#### Aktueller Codefluss

`spawnRestoredNpcs(...)` ruft zuerst UUID-Relink auf:

```text
RelinkOutcome relinkOutcome = tryRelinkEntityRef(...)
if relinkOutcome == SUCCESS:
    continue
```

Danach läuft der Code weiter zu:

1. Role-Prefix-Fallback,
2. Anchor-Fallback,
3. `MISSING_ENTITY`-Check,
4. Auto-Respawn-Entscheidung.

Problem:

`PENDING` wird nicht separat behandelt.

#### Warum das falsch ist

`PENDING` bedeutet semantisch:

```text
UUID-Relink ist noch offen.
Die Entscheidung ist noch nicht final.
Es darf noch kein schwächerer Fallback laufen.
```

Aktuell bedeutet `PENDING` im Caller praktisch:

```text
nicht SUCCESS -> versuche weitere Fallbacks
```

Das ist gefährlich, weil Anchor-Fallback oder Respawn zu früh greifen können, obwohl die UUID-Suche noch innerhalb des Retry-Fensters läuft.

#### Problematischer Statusfluss

```text
NEEDS_RELINK
-> UUID nicht sofort gefunden
-> tryRelinkEntityRef returns PENDING
-> Caller behandelt PENDING nicht als Stop
-> RolePrefix-Fallback läuft
-> Anchor-Fallback läuft
-> eventuell MISSING/Respawn-Pfad wird erreicht
```

#### Erwarteter Statusfluss

```text
NEEDS_RELINK
-> UUID nicht sofort gefunden
-> PENDING
-> Stop für diesen Durchlauf
-> später neuer UUID-Retry
```

Erst wenn UUID-Relink final aufgegeben wurde, dürfen schwächere Recovery-Pfade geprüft werden.

#### Minimaler Fix-Vorschlag

In `spawnRestoredNpcs(...)`:

```text
if relinkOutcome == SUCCESS:
    continue
if relinkOutcome == PENDING:
    continue
```

Besser noch:

`RelinkOutcome` sollte semantisch erweitert werden:

```text
SUCCESS
PENDING
GIVEUP_MARKED_MISSING
NO_MATCH
```

Dann kann der Caller klar unterscheiden:

- `PENDING` = Stop,
- `GIVEUP_MARKED_MISSING` = Missing-Endzustand oder explizite Respawn-Policy prüfen,
- `NO_MATCH` = nur wenn wirklich kein UUID-Pfad existiert.

#### Muss Safety-Datei angepasst werden?

**Ja.**

Neue Safety-Regel:

```text
RelinkOutcome.PENDING darf keine RolePrefix-, Anchor- oder Auto-Respawn-Fallbacks im selben Durchlauf auslösen.
```

---

### Fehler 3 — Nach `RELINK_GIVEUP_MARKED_MISSING` kann ein neuer Relink-Zyklus entstehen

**Risiko:** hoch
**Kategorie:** Relink-Loop / Missing nicht terminal
**Betroffene Datei:** `src/main/java/keystone/npc/relink/RelinkWorkflowService.java`
**Betroffene Methode:** `tryRelinkEntityRef(...)`
**Betroffene Zeilen:** ca. `182–225`
**Zusätzlich betroffen:** `NpcRoutineRunner.tick(...)`, `spawnRestoredNpcs("tick-retry")`

#### Aktueller Codefluss

Wenn die gespeicherte UUID nicht gefunden wird:

```text
misses++
firstMissAt setzen
wenn Retry-Limit/Zeit noch nicht erreicht:
    return PENDING
wenn Giveup erreicht:
    entityStatus = MISSING_ENTITY
    log RELINK_GIVEUP_MARKED_MISSING
    uuidRelinkMissCounts.remove(npcId)
    uuidRelinkFirstMissAtMs.remove(npcId)
    return PENDING
```

Der wichtige Punkt:

Nach Giveup werden die Miss-Counter gelöscht.

Gleichzeitig ruft `NpcRoutineRunner.tick(...)` regelmäßig wieder `spawnRestoredNpcs("tick-retry")` auf.

#### Warum das falsch ist

Nach `RELINK_GIVEUP_MARKED_MISSING` sollte der NPC in einem ruhigen Zustand bleiben.

Aktuell kann passieren:

```text
MISSING_ENTITY
-> spawnRestoredNpcs("tick-retry")
-> tryRelinkEntityRef erneut
-> Miss-Counter beginnt wieder bei 1
-> RELINK_RETRY
-> später wieder RELINK_GIVEUP_MARKED_MISSING
-> wieder von vorne
```

Damit ist `MISSING_ENTITY` kein echter terminaler Zustand.

#### Erwartetes Verhalten

Nach Giveup:

```text
NEEDS_RELINK
-> UUID-Retry endgültig fehlgeschlagen
-> MISSING_ENTITY
-> kein weiterer automatischer UUID-Relink pro Tick
-> nur expliziter Recovery-Pfad:
   - manuelles /knpc respawn-missing
   - erlaubter Auto-Respawn mit JSON-Policy + Chunk-Gate
   - manuelles Relink/Debug-Kommando
```

#### Minimaler Fix-Vorschlag

Es gibt zwei saubere Varianten.

Variante A, minimal:

```text
spawnRestoredNpcs(...):
  wenn entityStatus == MISSING_ENTITY:
      überspringe UUID-Relink
      prüfe nur explizit erlaubte Recovery-/Respawn-Policy
```

Variante B, klarer:

```text
RelinkOutcome.GIVEUP_MARKED_MISSING einführen
Caller behandelt diesen Outcome als terminalen Missing-Übergang
keine weiteren Fallbacks im selben Durchlauf
kein erneuter UUID-Relink ohne explizite Freigabe
```

#### Muss Safety-Datei angepasst werden?

**Ja.**

Neue Safety-Regel:

```text
Nach RELINK_GIVEUP_MARKED_MISSING darf kein automatischer Relink-Loop neu starten. MISSING_ENTITY ist ein terminaler Ruhe-Status bis zu expliziter Recovery oder erlaubtem Auto-Respawn.
```

---

### Fehler 4 — `respawnAfterRestart` existiert, steuert aber keine Respawn-Entscheidung

**Risiko:** hoch
**Kategorie:** JSON-Konfigurationsfeld ohne aktive Java-Logik
**Betroffene Dateien:**

- `src/main/resources/Server/NPC/Keystone/persistence/persistent_citizen.json`
- `src/main/resources/Server/NPC/Keystone/persistence/despawnable_hostile.json`
- `src/main/java/keystone/npc/persistence/profile/PersistenceProfile.java`
- `src/main/java/keystone/npc/definition/NpcTemplateResolver.java`
- `src/main/java/keystone/npc/routine/NpcRoutineRunner.java`
- `src/main/java/keystone/npc/recovery/RespawnPolicyConfig.java`

#### Aktueller Zustand

JSON enthält:

```text
persistent_citizen.json:
  respawnAfterRestart = true

despawnable_hostile.json:
  respawnAfterRestart = false
```

Java-Record existiert:

```text
PersistenceProfile(..., Boolean respawnAfterRestart, ...)
```

Aber der aktive Respawn-Pfad fragt das Feld nicht ab.

Aktive Entscheidung in `NpcRoutineRunner.spawnRestoredNpcs(...)`:

```text
if !respawnPolicyConfig.enableAutoRespawnMissingNpc():
    AUTO_RESPAWN_DISABLED
    continue
```

#### Tatsächlicher Datenfluss

```text
JSON persistence file
-> PersistenceProfile record existiert
-> profiles.persistence wird als Pfad validiert
-> kein aktives Profilobjekt im Runtime-Record
-> kein respawnAfterRestart in NpcRecord
-> keine Abfrage in spawnRestoredNpcs
-> Respawn-Entscheidung ignoriert JSON
```

#### Warum das falsch ist

Die Testfälle B und C können so nicht korrekt funktionieren:

- `respawnAfterRestart=false` verhindert Auto-Respawn nicht.
- `respawnAfterRestart=true` erlaubt Auto-Respawn nicht automatisch.
- Fehlendes Feld hat keinen definierten Sicherheitsdefault.

Aktuell entscheidet nur die globale System-Property.

#### Minimaler Fix-Vorschlag

Es muss einen aktiven Datenfluss geben:

```text
JSON-Datei
-> Parser/Loader
-> PersistenceProfile
-> EffectiveNpcDefinition oder RoleDefinition
-> pro NPC/roleId abfragbare Runtime-Policy
-> Auto-Respawn-Entscheidung
```

Sicherer Default:

```text
respawnAfterRestart missing/null => false
```

Auto-Respawn nur wenn:

```text
global Kill-Switch erlaubt
AND respawnAfterRestart == true
AND status == MISSING_ENTITY
AND Chunk-Gate bestanden
AND kein AMBIGUOUS-Relink-Zustand
```

#### Muss Safety-Datei angepasst werden?

**Ja.**

`json_hierarchy.md` muss von „vorbereitet“ auf „aktive Pflichtlogik“ aktualisiert werden, falls du erwartest, dass JSON das Verhalten wirklich steuert.

---

### Fehler 5 — Globale Auto-Respawn-Property ist alleinige Policy

**Risiko:** hoch
**Kategorie:** zu grobe Policy / JSON ignoriert
**Betroffene Datei:** `src/main/java/keystone/npc/recovery/RespawnPolicyConfig.java`
**Betroffene Methode:** `loadFromSystemProperties()`
**Betroffene Zeilen:** ca. `8–17`

#### Aktueller Codefluss

```text
knpc.enableAutoRespawnMissingNpc=false default
```

Auto-Respawn ist nur möglich, wenn diese Property true ist.

#### Problem 1 — zu stark blockierend

Wenn JSON sagt:

```text
respawnAfterRestart = true
```

aber globale Property default false bleibt, passiert trotzdem kein Auto-Respawn.

Das kann als globaler Kill-Switch okay sein, aber dann muss klar sein:

```text
JSON true reicht nicht.
Global false blockiert alles.
```

#### Problem 2 — zu grob erlaubend

Wenn globale Property true ist, aber JSON sagt:

```text
respawnAfterRestart = false
```

kann Auto-Respawn trotzdem in den aktiven Respawn-Pfad laufen, weil JSON nicht geprüft wird.

Das ist fachlich gefährlicher.

#### Erwartete Policy

```text
global false:
  Auto-Respawn immer aus

global true:
  Auto-Respawn nur, wenn pro NPC/Role JSON respawnAfterRestart == true
```

#### Minimaler Fix-Vorschlag

`RespawnPolicyConfig` als globale Policy behalten, aber in `NpcRoutineRunner` zusätzlich pro NPC prüfen:

```text
if !globalEnable:
    block
if !persistenceProfile.respawnAfterRestart:
    block
```

#### Muss Safety-Datei angepasst werden?

**Ja.**

Neue Regel:

```text
Globaler Auto-Respawn ist nur ein Kill-Switch. Er darf JSON-Policy nicht ersetzen.
```

---

### Fehler 6 — Chunk-Gate und Safety-Datei widersprechen sich

**Risiko:** mittel
**Kategorie:** Safety-Dokument veraltet oder Code-Semantik unklar
**Betroffene Datei:** `src/main/java/keystone/npc/routine/NpcRoutineRunner.java`
**Betroffene Methode:** `passesAutoRespawnChunkGate(...)`
**Betroffene Zeilen:** ca. `1462–1499`
**Betroffene Safety-Datei:** `docs/safety/npc_restart_relink_control.md`

#### Aktueller Codefluss

Im Chunk-Gate steht sinngemäß:

```text
wenn Chunk nicht sicher geladen/verifizierbar:
    wenn Status weder DISABLED noch MISSING_ENTITY:
        Status = NEEDS_RELINK
    return false
```

Das heißt:

```text
MISSING_ENTITY bleibt MISSING_ENTITY
```

#### Safety-Widerspruch

In `npc_restart_relink_control.md` gibt es Formulierungen, die sagen:

```text
entityStatus = NEEDS_RELINK
Status bleibt NEEDS_RELINK
```

für Chunk-Gate-Situationen.

#### Bewertung

Der aktuelle Code ist für das gewünschte Sticky-Missing-Ziel wahrscheinlich besser als die alte Safety-Formulierung.

Aber die Safety-Datei ist dadurch nicht eindeutig.

#### Minimaler Fix-Vorschlag

Nicht primär Code ändern, sondern Safety-Datei präzisieren:

```text
Bei ungeladenem Chunk:
- NEEDS_RELINK bleibt NEEDS_RELINK.
- MISSING_ENTITY bleibt MISSING_ENTITY.
- Es darf kein Respawn stattfinden.
- Chunk-Gate darf MISSING_ENTITY nicht zu NEEDS_RELINK zurücksetzen.
```

#### Muss Safety-Datei angepasst werden?

**Ja.**

---

### Fehler 7 — `MISSING_ENTITY` wird im aktiven Serverlauf teilweise geschützt, aber nicht als offizieller Ruhe-Endzustand modelliert

**Risiko:** mittel bis hoch
**Kategorie:** unklare Status-Semantik
**Betroffene Dateien:**

- `NpcRoutineRunner.java`
- `RelinkWorkflowService.java`
- `RespawnRecoveryService.java`
- `NpcEntityStatus.java`

#### Beobachtung

Mehrere Stellen behandeln `MISSING_ENTITY` defensiv:

- `passesLiveEntityGate(...)` überschreibt `MISSING_ENTITY` nicht erneut.
- `passesAutoRespawnChunkGate(...)` überschreibt `MISSING_ENTITY` nicht auf `NEEDS_RELINK`.
- `RelinkWorkflowService` setzt bei harten Fehlern `MISSING_ENTITY`.

Aber es gibt keinen zentralen Zustand:

```text
MISSING_ENTITY = terminaler Recovery-Wartezustand
```

Der Status wird also an manchen Stellen geschützt, an anderen Stellen aber erneut in Workflows hineingelassen.

#### Warum das gefährlich ist

Dadurch entstehen schwer nachvollziehbare Flows:

```text
MISSING_ENTITY bleibt manchmal ruhig
MISSING_ENTITY wird nach Restart wieder NEEDS_RELINK
MISSING_ENTITY kann wieder durch tryRelinkEntityRef laufen
MISSING_ENTITY kann bei global true in Auto-Respawn laufen
```

Das ist kein klares Gate-Modell.

#### Minimaler Fix-Vorschlag

Eine zentrale Regel in `spawnRestoredNpcs(...)`:

```text
if status == MISSING_ENTITY:
    kein UUID-Relink
    kein RolePrefix-Fallback
    kein Anchor-Fallback
    nur explicitRecoveryAllowed / autoRespawnAllowed prüfen
```

---

## 5. Statuswechsel-Matrix

| Von | Nach | Erlaubt? | Wo im Code? | Bewertung |
|---|---:|---:|---|---|
| RESTORE | DISABLED | ja | `NpcRoutineRunner.restore`, `staleReasonForRestore` | Korrekt. Invalide Records bleiben für manuelle Recovery erhalten. |
| RESTORE | MISSING_ENTITY | ja | `NpcRoutineRunner.restore`, wenn UUID fehlt | Korrekt bei fehlender UUID. |
| RESTORE | NEEDS_RELINK | ja | `NpcRoutineRunner.restore`, wenn UUID vorhanden | Korrekt für vorher ACTIVE, falsch für gespeichertes MISSING_ENTITY. |
| RESTORE MISSING_ENTITY | NEEDS_RELINK | nein | `NpcRoutineRunner.restore` | Logikfehler. Sticky wird gebrochen. |
| NEEDS_RELINK | ACTIVE | ja | `RelinkWorkflowService.tryRelinkEntityRef` | Korrekt bei hartem UUID-Live-Match. |
| NEEDS_RELINK | MISSING_ENTITY | ja | `RelinkWorkflowService.tryRelinkEntityRef` | Korrekt nach Giveup, aber danach nicht terminal genug. |
| NEEDS_RELINK | NEEDS_RELINK | ja | UUID-Retry / Chunk nicht geladen | Korrekt, solange kein Spam entsteht. |
| MISSING_ENTITY | NEEDS_RELINK | nein, außer manuell begründet | Restore aktuell | Fehlerhaft beim Restart. |
| MISSING_ENTITY | ACTIVE | ja, aber nur über echten Relink/Respawn | Anchor/UUID/Respawn | Erlaubt, aber nur kontrolliert. |
| ACTIVE | NEEDS_RELINK | ja | `passesLiveEntityGate`, wenn Live-Ref invalid und UUID vorhanden | Korrekt. |
| ACTIVE | MISSING_ENTITY | ja | `passesLiveEntityGate`, wenn Live-Ref invalid und UUID fehlt | Korrekt. |
| DISABLED | irgendwas | nein | mehrere Guards | Aktuell überwiegend korrekt geschützt. |

---

## 6. `respawnAfterRestart` Trace

### 6.1 JSON-Ebene

Vorhanden:

```text
Server/NPC/Keystone/persistence/persistent_citizen.json
  respawnAfterRestart: true

Server/NPC/Keystone/persistence/despawnable_hostile.json
  respawnAfterRestart: false
```

Bewertung:

- Feld ist in Ressourcen vorhanden.
- Unterschiedliche Werte existieren und sollen offenbar Verhalten steuern.

### 6.2 Java-Datenmodell

Vorhanden:

```text
PersistenceProfile.java
  Boolean respawnAfterRestart
```

Bewertung:

- Das Java-Record kennt das Feld.
- Das allein reicht nicht; es muss aktiv geladen und genutzt werden.

### 6.3 Loader/Resolver

Befund:

- `profiles.persistence` wird im Definition-/Template-Kontext offenbar als Pfad validiert.
- Es wurde kein belastbarer aktiver Datenfluss gefunden, der `PersistenceProfile.respawnAfterRestart` in eine Runtime-Entscheidung überführt.

Bewertung:

```text
vorhanden: teilweise
aktiv genutzt: nein
```

### 6.4 Runtime/NpcRecord

Befund:

- `NpcRecord` besitzt kein direktes `respawnAfterRestart`-Feld.
- `spawnRestoredNpcs(...)` fragt kein Profilfeld pro NPC ab.

Bewertung:

```text
fehlt
```

### 6.5 Respawn-Entscheidung

Aktive Entscheidung:

```text
respawnPolicyConfig.enableAutoRespawnMissingNpc()
```

Nicht aktiv:

```text
respawnAfterRestart
respawnOnMissingEntity
preserveIdentity
```

Bewertung:

```text
respawnAfterRestart wird ignoriert
```

### 6.6 Default-Verhalten

Aktuell:

- fehlendes Feld hat keine direkte Bedeutung,
- `false` hat keine direkte Bedeutung,
- `true` hat keine direkte Bedeutung.

Empfohlener sicherer Default:

```text
respawnAfterRestart == true  -> darf Auto-Respawn erlauben, wenn alle anderen Gates passen
respawnAfterRestart == false -> kein Auto-Respawn
respawnAfterRestart fehlt    -> kein Auto-Respawn
```

---

## 7. Kernfragen vollständig beantwortet

### 1. Wo wird ein NPC nach Restore auf `NEEDS_RELINK` gesetzt?

In `NpcRoutineRunner.restore(...)`, wenn `entityUuid` vorhanden ist und der NPC nicht `DISABLED` ist.

### 2. Ist dieser Restore-Status korrekt?

Teilweise.

Korrekt für:

```text
ACTIVE vor Restart + entityUuid vorhanden -> NEEDS_RELINK
```

Nicht korrekt für:

```text
MISSING_ENTITY vor Restart + entityUuid vorhanden -> NEEDS_RELINK
```

### 3. Wo wird `MISSING_ENTITY` gesetzt?

Gefundene relevante Stellen:

- `JsonFileStateStore.toNpcRecord(...)`
- `NpcRoutineRunner.restore(...)`
- `NpcRoutineRunner.passesLiveEntityGate(...)`
- `RelinkWorkflowService.tryRelinkEntityRef(...)`
- `RespawnRecoveryService.scheduleRespawnRetry(...)`
- Rollback-/Identity-Restore-Pfade in `NpcRoutineRunner`

### 4. Welche Bedingungen führen zu `MISSING_ENTITY`?

- keine persistierte `entityUuid`,
- invalid persistierte UUID,
- UUID gehört anderem Record,
- UUID findet Entity, aber Live-UUID/Claim passt nicht,
- UUID-Retry gibt auf,
- EntityRef invalid und keine UUID vorhanden,
- Respawn-Recovery schlägt wiederholt fehl.

### 5. Gibt es Code, der `MISSING_ENTITY` wieder zu `NEEDS_RELINK` macht?

Ja.

Hauptfund:

- `NpcRoutineRunner.restore(...)`

Bedingung:

```text
entityStatus != DISABLED
AND entityUuid vorhanden
```

### 6. Welche Methode, welche Bedingung, welche Folge?

Methode:

```text
NpcRoutineRunner.restore(...)
```

Bedingung:

```text
entityUuid != null && !blank
```

Folge:

```text
MISSING_ENTITY aus state.json wird zu NEEDS_RELINK
```

### 7. Ist `MISSING_ENTITY` sticky?

Nein, nicht vollständig.

- Innerhalb eines laufenden Servers teilweise geschützt.
- Über Restart hinweg nicht sticky.
- Nach Giveup kann erneut Relink-Logik starten.

### 8. Falls nein: welche minimale Änderung wäre nötig?

Minimal:

```text
restore(...): MISSING_ENTITY respektieren.
spawnRestoredNpcs(...): MISSING_ENTITY nicht automatisch durch UUID-Relink schicken.
```

### 9. Wo wird nach `RELINK_GIVEUP_MARKED_MISSING` weitergetickt?

In `NpcRoutineRunner.tick(...)`, das regelmäßig `spawnRestoredNpcs("tick-retry")` aufruft.

### 10. Kann danach sofort wieder `RELINK_RETRY` entstehen?

Ja.

Nach Giveup werden die Miss-Counter gelöscht. Beim nächsten Durchlauf kann der gleiche NPC mit gleicher UUID erneut in `tryRelinkEntityRef(...)` landen und einen neuen Retry-Zyklus starten.

### 11. Gibt es einen Retry-Cooldown?

Für UUID-Relink gibt es Miss-Counter und Zeitfenster über:

- `uuidRelinkMissCounts`
- `uuidRelinkFirstMissAtMs`
- `relinkRetryCount`
- `relinkRetryDelayMs`

Aber es gibt keinen terminalen Cooldown für:

```text
MISSING_ENTITY nach Giveup
```

### 12. Gibt es Log-Spam-Risiko?

Ja.

Nicht zwingend jede Tick-Iteration, aber zyklisch wiederkehrend:

- `RELINK_RETRY`
- `RELINK_GIVEUP_MARKED_MISSING`
- `AUTO_RESPAWN_DISABLED`

`AUTO_RESPAWN_DISABLED` wird durch `lastValidationWarningKey` gedrosselt. `RELINK_GIVEUP_MARKED_MISSING` ist aber nicht in gleicher Weise terminal unterbunden.

### 13. Wo wird entschieden, ob Auto-Respawn erlaubt ist?

In `NpcRoutineRunner.spawnRestoredNpcs(...)` über:

```text
respawnPolicyConfig.enableAutoRespawnMissingNpc()
```

### 14. Wird `respawnAfterRestart` aus JSON geladen?

Das Feld existiert im JSON und im Java-Record.

Aber ein aktiver Lade-/Nutzungsfluss bis zur Respawn-Entscheidung wurde nicht gefunden.

### 15. Wird `respawnAfterRestart` bis zur Respawn-Entscheidung weitergereicht?

Nein.

### 16. Falls nein: welche Datenstruktur / Methode fehlt?

Es fehlt mindestens eine aktive Abfrage wie:

```text
resolvePersistenceProfileForNpc(npc)
```

oder ein Runtime-Feld/Policy-Objekt in:

```text
EffectiveNpcDefinition
RoleDefinition
NpcRecord
```

Wichtig: Nicht zwingend alles refactoren. Minimal reicht ein sauberer Resolver, der pro `roleId` das PersistenceProfile liefert.

### 17. Blockiert die globale System-Property Auto-Respawn vollständig?

Ja.

Default:

```text
knpc.enableAutoRespawnMissingNpc = false
```

### 18. Ist das laut Safety-Ziel korrekt oder zu stark?

Als globaler Kill-Switch ist es korrekt.

Als einzige Policy ist es zu stark bzw. fachlich falsch, weil JSON-Policy ignoriert wird.

### 19. Was passiert bei `respawnAfterRestart=false`?

Aktuell nichts Spezielles. Das Feld wird ignoriert.

Wenn globale Auto-Respawn-Property true ist, kann Auto-Respawn trotzdem passieren.

### 20. Was passiert bei fehlendem Feld?

Aktuell nichts Spezielles.

Empfohlen:

```text
fehlend/null => false
```

### 21. Was passiert bei DISABLED?

`DISABLED` wird sauber geschützt:

- kein Restore zu NEEDS_RELINK,
- kein Spawn-Restore,
- kein normaler Tick,
- kein Auto-Respawn.

### 22. Was passiert bei mehreren gleichen Lumberjacks nahe beieinander?

Anchor-Fallback sammelt Kandidaten gleicher Engine-Role in Nähe der Dedupe-Anker.

Bei mehreren Kandidaten:

```text
AnchorRelinkOutcome.AMBIGUOUS
```

Dann wird nicht gebunden und nicht gespawnt.

Bewertung: gut.

Warnung: Anchor-Fallback darf nicht schon laufen, wenn UUID-Relink noch `PENDING` ist.

### 23. Gibt es irgendwo blindes Relinken per gleicher Role?

Role-Prefix-Fallback ist deaktiviert und gibt `NO_MATCH` zurück.

Anchor-Fallback nutzt gleiche Engine-Role plus Nähe plus Ownership-Checks. Das ist kein komplett blindes Role-Relink, aber ein defensiver Fallback.

### 24. Gibt es irgendwo blindes Löschen wegen gleicher Role?

Kein gefährlicher Treffer gefunden.

Dedupe scheint nicht einfach alle gleichen Lumberjacks zu löschen, sondern Ownership/UUID/Ref-Kontext zu prüfen.

### 25. Bleibt `npcId` beim Respawn stabil?

Ja.

Respawn arbeitet am bestehenden `NpcRecord`. `npcId` wird nicht neu erzeugt.

### 26. Wird `entityUuid` beim Respawn korrekt ersetzt?

Ja, über `updatePersistedEntityIdentity(...)` nach erfolgreichem Spawn/Relink.

### 27. Bleiben Marker-Zuweisungen erhalten?

Ja, weil derselbe `NpcRecord` weiterverwendet wird.

### 28. Wird `state.json` nach erfolgreichem Respawn gespeichert?

Dirty wird markiert.

- Bei Command-Pfaden wird typischerweise sofort gespeichert.
- Bei Auto-Respawn wird deferred gespeichert.

Bewertung: grundsätzlich okay, aber nach Statusfix erneut testen.

### 29. Werden Runtime-Felder weiterhin nicht gespeichert?

Grundsätzlich ja:

- `entityRef` wird nicht persistiert,
- Navigation-Runtime wird nicht persistiert,
- Door-Runtime wird nicht persistiert.

Aber Achtung:

`entityStatus` ist kein reines Runtime-Feld. Er wird persistiert. Deshalb ist das Restore-Überschreiben besonders kritisch, weil es nach `saveState()` wieder in `state.json` landen kann.

### 30. Welche Safety-Datei müsste angepasst werden?

Mindestens:

- `docs/safety/npc_restart_relink_control.md`
- `docs/safety/json_hierarchy.md`

---

## 8. Testfall-Simulationen

---

### Testfall A — Normaler Restart, Entity wird gefunden

#### Start

```text
Record in state.json
entityUuid vorhanden
Entity existiert nach Restart
```

#### Erwartung

```text
RESTORE -> NEEDS_RELINK -> ACTIVE
```

#### Tatsächlicher Codefluss

```text
JsonFileStateStore lädt Record
NpcRoutineRunner.restore:
  entityRef = null
  entityId = 0
  entityUuid vorhanden -> NEEDS_RELINK
spawnRestoredNpcs:
  tryRelinkEntityRef
  UUID wird gefunden
  ACTIVE
```

#### Bewertung

**PASS.**

Keine neue UUID, kein Respawn, keine Duplicate, keine alte Navigation.

---

### Testfall B — Entity fehlt, `respawnAfterRestart=false`

#### Start

```text
Record in state.json
entityUuid vorhanden
Entity nicht auffindbar
respawnAfterRestart=false oder fehlt
```

#### Erwartung

```text
RESTORE -> NEEDS_RELINK -> MISSING_ENTITY
Dann sticky MISSING_ENTITY.
Kein Auto-Respawn.
Kein Relink-Loop.
Kein Tick-Spam.
```

#### Tatsächlicher Codefluss

```text
RESTORE -> NEEDS_RELINK
tryRelinkEntityRef:
  UUID nicht gefunden
  RELINK_RETRY
  nach Giveup MISSING_ENTITY
spawnRestoredNpcs tick-retry:
  kann erneut tryRelinkEntityRef aufrufen
respawnAfterRestart=false:
  wird ignoriert
```

#### Bewertung

**FAIL.**

Fehler:

- `respawnAfterRestart=false` wird nicht berücksichtigt.
- `MISSING_ENTITY` wird nicht als ruhiger Endzustand garantiert.
- Relink-Loop-Risiko bleibt.

---

### Testfall C — Entity fehlt, `respawnAfterRestart=true`

#### Start

```text
Record in state.json
entityUuid vorhanden
Entity nicht auffindbar
respawnAfterRestart=true
globaler Kill-Switch nicht blockierend
Chunk geladen
```

#### Erwartung

```text
RESTORE -> NEEDS_RELINK -> kontrollierter Respawn -> ACTIVE
npcId bleibt gleich
entityUuid wird ersetzt
Marker bleiben erhalten
state.json wird gespeichert
```

#### Tatsächlicher Codefluss

```text
respawnAfterRestart=true wird ignoriert
nur knpc.enableAutoRespawnMissingNpc entscheidet
```

Wenn globale Property false/default:

```text
AUTO_RESPAWN_DISABLED
kein Auto-Respawn
```

Wenn globale Property true:

```text
Auto-Respawn kann passieren,
auch wenn JSON für diesen NPC false wäre
```

#### Bewertung

**FAIL.**

Das JSON-Feld steuert nicht das Verhalten.

---

### Testfall D — Entity fehlt, Chunk nicht geladen

#### Start

```text
Entity fehlt
respawnAfterRestart=true
Chunk nicht geladen
```

#### Erwartung

```text
Kein Respawn.
Status bleibt sinnvoll.
Kein Tick-Spam.
Später bei geladenem Chunk erneute sichere Prüfung.
```

#### Tatsächlicher Codefluss

```text
passesAutoRespawnChunkGate -> false
kein Respawn
MISSING_ENTITY bleibt MISSING_ENTITY
NEEDS_RELINK kann NEEDS_RELINK bleiben
```

#### Bewertung

**PARTIAL PASS.**

Gut:

- kein Respawn ohne Chunk-Gate,
- `MISSING_ENTITY` wird vom Chunk-Gate nicht auf `NEEDS_RELINK` zurückgesetzt.

Problem:

- Vor dem Chunk-Gate kann weiter Relink-Logik laufen.
- Safety-Datei ist nicht eindeutig/teilweise widersprüchlich.

---

### Testfall E — DISABLED

#### Start

```text
entityStatus = DISABLED
```

#### Erwartung

```text
Kein Relink.
Kein Respawn.
Kein Statuswechsel.
Kein Tick-Spam.
```

#### Tatsächlicher Codefluss

- `restore(...)` behält `DISABLED`,
- `spawnRestoredNpcs(...)` überspringt `DISABLED`,
- `passesLiveEntityGate(...)` stoppt bei `DISABLED`,
- Tick-Pipeline respektiert `DISABLED`.

#### Bewertung

**PASS.**

---

### Testfall F — Mehrere Lumberjacks nahe beieinander

#### Start

```text
2–4 gleiche Rollen nahe zusammen
UUID einer Entity fehlt oder ist nicht auffindbar
```

#### Erwartung

```text
Kein blindes Relinken per Role.
Anchor nur bei eindeutiger Zuordnung.
Bei Mehrdeutigkeit AMBIGUOUS.
Kein Auto-Respawn bei AMBIGUOUS.
Keine Dedupe-Löschung wegen gleicher Role.
```

#### Tatsächlicher Codefluss

- Role-Prefix-Fallback ist deaktiviert.
- Anchor-Fallback prüft Role + Nähe + Ownership.
- Mehrere Kandidaten ergeben `AMBIGUOUS`.
- Bei `AMBIGUOUS` wird nicht gespawnt.

#### Bewertung

**PASS mit Warnung.**

Warnung:

Anchor-Fallback darf erst laufen, wenn UUID-Relink final abgeschlossen ist. Aktuell kann er nach `PENDING` zu früh laufen.

---

### Testfall G — Alte dynamische Role-Falle

#### Prüfpunkte

```text
Kein KeystoneNPC_<npcId>_<roleId>_Role per setRoleName
Keine Runtime-Logs: Reloading nonexistent role KeystoneNPC_
```

#### Suchergebnis

- Kein aktiver Java-Treffer für dynamisches `setRoleName("KeystoneNPC_...")`.
- `tryRolePrefixRelinkEntityRef(...)` gibt direkt `NO_MATCH` zurück.
- Strings `KeystoneNPC_` kommen nur in Safety-Dokumenten vor.

#### Bewertung

**PASS.**

---

## 9. Safety-Regeln gegen Code geprüft

| Safety-Regel | Ergebnis | Begründung |
|---|---:|---|
| Kein normaler NPC-Tick ohne gültige EntityRef | PASS | Live-Entity-Gate vorhanden. |
| Alte Navigation darf nach Restart nicht resumed werden | PASS | Restore resetet Navigation. |
| UUID-Relink nur mit hartem Live-UUID-Match | PASS | UUID-Match wird streng geprüft. |
| Auto-Respawn nicht ohne Chunk-Gate | PASS | `passesAutoRespawnChunkGate(...)` vorhanden. |
| Kein dynamisches `setRoleName("KeystoneNPC_...")` | PASS | Kein aktiver Java-Treffer. |
| Role-Prefix-Fallback bleibt deaktiviert | PASS | Gibt `NO_MATCH` zurück. |
| Anchor-Fallback letzter Fallback und AMBIGUOUS blockiert | PARTIAL | AMBIGUOUS blockiert, aber Anchor läuft nach `PENDING` zu früh. |
| `MISSING_ENTITY` darf nicht automatisch zu `NEEDS_RELINK` werden | FAIL | Restore macht genau das bei vorhandener UUID. |
| `DISABLED` bleibt `DISABLED` | PASS | Sauber geschützt. |
| Runtime-State darf nicht in state.json gespeichert werden | PASS/PARTIAL | Runtime-Felder nicht, aber Status wird beim Restore überschrieben und gespeichert. |
| Status-/Identity-Änderungen dirty-safe speichern | PASS/PARTIAL | Viele Pfade markieren dirty, aber Restore-Überschreibung ist fachlich falsch. |
| Keine Dedupe-Löschung nur wegen gleicher Lumberjack-Role | PASS | Kein gefährlicher Treffer gefunden. |

---

## 10. Log-Spam-Risiko

### 10.1 Bereits gedrosselte Logs

`AUTO_RESPAWN_DISABLED` nutzt `lastValidationWarningKey`, dadurch kein harter Per-Tick-Spam.

### 10.2 Problematische Logs

Potentiell wiederkehrend:

- `RELINK_RETRY`
- `RELINK_GIVEUP_MARKED_MISSING`
- `RELINK_ATTEMPT`

Der größte Risikopunkt ist nicht, dass jede Tick-Iteration loggt, sondern dass nach Giveup die Retry-Zähler gelöscht werden und dadurch neue Retry-/Giveup-Zyklen entstehen können.

### 10.3 Erwartetes Ziel

```text
Pro NPC und Fehlerursache einmal loggen,
danach nur bei echter Zustandsänderung erneut.
```

Beispiel:

```text
RELINK_GIVEUP_MARKED_MISSING einmal pro npcId + entityUuid
AUTO_RESPAWN_DISABLED einmal pro npcId + Policy-Grund
CHUNK_NOT_LOADED einmal pro npcId + Chunk/Position mit Cooldown
```

---

## 11. Minimaler Patch-Plan

Dieser Plan ist bewusst minimal. Keine Architektur-Explosion, keine neuen Features, kein Door-/Navigation-/Dedupe-Refactor.

---

### Step 1 — `MISSING_ENTITY` sticky machen

#### Ziel

Persistiertes `MISSING_ENTITY` darf beim Restore nicht zu `NEEDS_RELINK` werden.

#### Erlaubte Dateien

- `NpcRoutineRunner.java`
- optional `JsonFileStateStore.java`, falls eine klarere Normalisierung nötig ist

#### Verbotene Dateien

- Door-System
- Navigation-System
- Dedupe-System
- Hytale Engine Role JSONs
- `RoleDefinitionRegistry`, außer zwingend nötig

#### Exakte Änderung

In `restore(...)`:

```text
if status == DISABLED:
    bleibt DISABLED
else if status == MISSING_ENTITY:
    bleibt MISSING_ENTITY
else if entityUuid fehlt:
    MISSING_ENTITY
else:
    NEEDS_RELINK
```

#### Test-Gate

```text
state.json mit MISSING_ENTITY + entityUuid laden
prüfen: nach Restore bleibt MISSING_ENTITY
prüfen: saveState schreibt nicht wieder NEEDS_RELINK
```

---

### Step 2 — `RelinkOutcome.PENDING` als Stop behandeln

#### Ziel

Solange UUID-Relink noch pending ist, dürfen keine schwächeren Fallbacks laufen.

#### Erlaubte Dateien

- `NpcRoutineRunner.java`
- optional `RelinkWorkflowService.java`

#### Exakte Änderung

In `spawnRestoredNpcs(...)`:

```text
if relinkOutcome == SUCCESS:
    continue
if relinkOutcome == PENDING:
    continue
```

Optional besser:

```text
RelinkOutcome.GIVEUP_MARKED_MISSING einführen
```

#### Test-Gate

```text
UUID fehlt temporär
RelinkOutcome.PENDING
Anchor-Fallback wird nicht aufgerufen
Auto-Respawn wird nicht im selben Durchlauf gestartet
```

---

### Step 3 — Relink-Loop nach Giveup stoppen

#### Ziel

Nach `RELINK_GIVEUP_MARKED_MISSING` darf nicht automatisch ein neuer Retry-Zyklus beginnen.

#### Erlaubte Dateien

- `NpcRoutineRunner.java`
- `RelinkWorkflowService.java`

#### Exakte Änderung

Eine zentrale Regel einführen:

```text
status == MISSING_ENTITY:
    kein UUID-Relink mehr automatisch
```

Ausnahmen nur:

- manuelles Debug-/Recovery-Kommando,
- explizit erlaubter Auto-Respawn,
- klar definierter Retry-Cooldown mit expliziter Policy.

#### Test-Gate

```text
Entity fehlt dauerhaft
nach Giveup bleibt Status MISSING_ENTITY
30 Sekunden beobachten
kein neuer RELINK_RETRY/GIVEUP-Loop
```

---

### Step 4 — `respawnAfterRestart` bis zur Entscheidung durchreichen

#### Ziel

JSON-Policy soll Auto-Respawn pro NPC/Role steuern.

#### Erlaubte Dateien

- `PersistenceProfile.java`
- `NpcTemplateResolver.java`
- `EffectiveNpcDefinition.java` oder eine minimal passende Runtime-Zugriffsstelle
- `NpcRoutineRunner.java`

#### Exakte Änderung

Datenfluss herstellen:

```text
profiles.persistence
-> PersistenceProfile laden
-> pro roleId/NPC abfragbar machen
-> spawnRestoredNpcs prüft respawnAfterRestart
```

#### Sicherer Default

```text
missing/null/invalid => false
```

#### Test-Gate

```text
persistent_citizen: respawnAfterRestart=true -> darf bei global true respawnen
hostile/despawnable: respawnAfterRestart=false -> darf auch bei global true nicht auto-respawnen
fehlendes Feld -> kein Auto-Respawn
```

---

### Step 5 — Auto-Respawn-Policy minimal korrigieren

#### Ziel

Globale Property ist Kill-Switch, aber nicht alleinige Policy.

#### Erlaubte Dateien

- `RespawnPolicyConfig.java`
- `NpcRoutineRunner.java`

#### Exakte Regel

```text
if globalAutoRespawn == false:
    block
else if respawnAfterRestart != true:
    block
else if chunkGate fails:
    block
else:
    controlled respawn
```

#### Test-Gate

```text
global false + JSON true -> block
global true + JSON false -> block
global true + JSON true + chunk loaded -> respawn
global true + JSON true + chunk not loaded -> block
```

---

### Step 6 — Safety-Dateien aktualisieren

#### Erlaubte Dateien

- `docs/safety/npc_restart_relink_control.md`
- `docs/safety/json_hierarchy.md`

#### Neue Regeln

```text
MISSING_ENTITY ist terminal/sticky bis expliziter Recovery-Pfad.
RelinkOutcome.PENDING ist ein harter Stop für schwächere Fallbacks.
Auto-Respawn braucht globale Freigabe + JSON-Freigabe + Chunk-Gate.
Chunk-Gate darf MISSING_ENTITY nicht zu NEEDS_RELINK zurücksetzen.
```

---

## 12. Minimal nötige Dateien für Fix

Wahrscheinlich nötig:

```text
src/main/java/keystone/npc/routine/NpcRoutineRunner.java
src/main/java/keystone/npc/relink/RelinkWorkflowService.java
src/main/java/keystone/npc/recovery/RespawnPolicyConfig.java
src/main/java/keystone/npc/persistence/profile/PersistenceProfile.java
src/main/java/keystone/npc/definition/NpcTemplateResolver.java
src/main/java/keystone/npc/definition/EffectiveNpcDefinition.java
```

Dokumentation/Safety:

```text
docs/safety/npc_restart_relink_control.md
docs/safety/json_hierarchy.md
```

Optional, nur wenn der gewählte Datenfluss es braucht:

```text
src/main/java/keystone/npc/domain/NpcRecord.java
```

---

## 13. Dateien, die ausdrücklich nicht geändert werden sollten

Nicht anfassen, außer eine spätere Analyse beweist direkte Notwendigkeit:

```text
src/main/resources/Server/NPC/Roles/*
src/main/java/keystone/npc/doorway/*
src/main/java/keystone/npc/navigation/*
src/main/java/keystone/npc/routine/pathfinding/*
src/main/java/keystone/npc/relink/Dedupe-Logik, außer direkt im Relink-Gate nötig
src/main/java/keystone/npc/commands/*, außer ein Test-/Debug-Kommando ist ausdrücklich Scope
```

Strikt verboten:

```text
kein setRoleName("KeystoneNPC_...")
keine dynamischen per-NPC Engine-Roles
kein blindes Relinken per gleicher Role
kein Auto-Respawn bei AMBIGUOUS
keine Dedupe-Löschung nur wegen gleicher Role
kein Door-/Navigation-Refactor in diesem Patch
```

---

## 14. Test-Gates nach Fix

### 14.1 Compile

Pflicht:

```text
mvn -q -DskipTests test-compile
```

Hinweis:

In der Analyseumgebung konnte dieser Befehl nicht ausgeführt werden:

```text
mvn: command not found
```

Daher muss der Compile lokal im Projekt geprüft werden.

### 14.2 Manuelle Tests

```text
[ ] Restart mit gefundener Entity
[ ] Restart mit fehlender Entity + respawnAfterRestart=false
[ ] Restart mit fehlender Entity + respawnAfterRestart=true
[ ] Restart mit fehlender Entity + fehlendem respawnAfterRestart-Feld
[ ] Restart mit globalAutoRespawn=false + JSON true
[ ] Restart mit globalAutoRespawn=true + JSON false
[ ] Restart mit globalAutoRespawn=true + JSON true + Chunk geladen
[ ] Restart mit globalAutoRespawn=true + JSON true + Chunk nicht geladen
[ ] DISABLED NPC bleibt DISABLED
[ ] Mehrere Lumberjacks nahe beieinander -> AMBIGUOUS, kein Respawn
[ ] 30 Sekunden Log beobachten: kein Relink-/Giveup-Loop
[ ] Prüfen: kein `Reloading nonexistent role KeystoneNPC_`
[ ] Prüfen: kein dynamisches `setRoleName("KeystoneNPC_...")`
```

### 14.3 state.json-Prüfung

Nach Tests prüfen:

```text
[ ] npcId bleibt stabil
[ ] markerAssignments bleiben erhalten
[ ] entityUuid wird nur nach erfolgreichem Relink/Respawn ersetzt
[ ] MISSING_ENTITY bleibt MISSING_ENTITY, wenn keine Recovery erlaubt ist
[ ] ACTIVE wird nur mit gültiger Live-EntityRef gesetzt
[ ] DISABLED wird nie automatisch geändert
[ ] keine Runtime-Navigation in state.json
[ ] keine Runtime-EntityRef in state.json
```

---

## 15. Empfohlene Priorität

### Höchste Priorität

1. `MISSING_ENTITY` beim Restore sticky machen.
2. `RelinkOutcome.PENDING` als Stop behandeln.
3. Relink-Loop nach `RELINK_GIVEUP_MARKED_MISSING` stoppen.

### Danach

4. `respawnAfterRestart` aktiv an die Respawn-Entscheidung anbinden.
5. Auto-Respawn-Policy global + JSON kombinieren.
6. Safety-Dateien präzisieren.

### Nicht jetzt

- keine Door-Logik,
- keine Pathfinding-Logik,
- keine Model-/Animation-Logik,
- keine JSON-Hierarchy-Großmigration,
- kein Dedupe-Refactor,
- keine neuen dynamischen Engine-Roles.

---

## 16. Kurzdiagnose für Agent-Step 1

Der erste Agent-Step sollte nur diese Logik absichern:

```text
MISSING_ENTITY bleibt beim Restore MISSING_ENTITY.
RelinkOutcome.PENDING stoppt schwächere Fallbacks.
Nach Giveup entsteht kein automatischer neuer UUID-Relink-Loop.
```

Nicht in Step 1:

```text
respawnAfterRestart komplett implementieren
PersistenceProfile-Refactor
RoleDefinitionRegistry umbauen
Door-System anfassen
Dedupe-System anfassen
```

---

## 17. Schlussfazit

Der aktuelle Code hat bereits viele richtige Sicherheitsideen:

- Runtime-EntityRefs werden nach Restart gelöscht.
- DISABLED ist geschützt.
- dynamische `KeystoneNPC_...` Engine-Roles sind deaktiviert.
- UUID-Relink ist hart.
- Chunk-Gate existiert.
- Anchor-Fallback blockiert AMBIGUOUS.
- Dedupe wirkt nicht blind nur wegen gleicher Role.

Der eigentliche Fehler liegt jetzt nicht mehr bei der alten dynamischen Role-Falle, sondern im Control-Gate selbst:

```text
MISSING_ENTITY ist noch kein echter terminaler Zustand.
PENDING wird nicht als Stop interpretiert.
respawnAfterRestart ist noch nicht Teil der aktiven Respawn-Policy.
```

Solange diese drei Punkte nicht behoben sind, können Restart-/Relink-/Respawn-Flows weiter instabil bleiben.
