# NPCMod / KeystoneNPC — Block-Report zu groben logischen Errors

**Datum:** 2026-05-13  
**Thema:** Architektur-Blöcke und grobe logische Error-Risiken  
**Basis:** Mindmap-Überblick, AGENTS.md, json_hierarchy.md, npc_restart_relink_control.md

---

## Kurzfazit

Die größten echten Gefahren sitzen nicht im Loader allein, sondern in der Kette:

```text
state.json → Restore / Relink / Respawn → Tick / Scheduler → Marker → Commands
```

Also genau dort, wo **Persistence** und **Runtime** sich berühren.

---

## Übersicht nach Blöcken

| Block | Bereich | Grobe logische Errors | Bericht |
|---:|---|---:|---|
| **1** | Plugin / Einstieg | **3** | Risiko ist vor allem die **Start-Reihenfolge**: Commands, JSON-Loader, state.json und Runtime-Systeme dürfen nicht in falscher Reihenfolge starten. Fehler wäre: Runtime läuft, bevor Definitionen/State sicher geladen sind. |
| **2** | Loader & JSON-Hierarchie | **5** | Fehler möglich bei **Engine-Role vs. Keystone-role vs. npcId**, kaputtem Legacy-Fallback, Duplicate-Rollen, falschen `requiredMarkers/markerRoles` und vorbereiteten Profilen, die fälschlich als aktiv behandelt werden. |
| **3** | Persistenz / state.json | **6** | Sehr kritisch. Fehler wären: Runtime-Daten speichern, `entityRef` persistieren, Save pro Tick, kaputten Partial-Load überschreiben, MarkerAssignments heimlich ändern, oder `currentPosition` als sichere Wahrheit behandeln, obwohl Chunk/Entity unsicher sind. |
| **4** | NPC-Lebenszyklus | **8** | Größter Hotspot. Hier entstehen Ghost-NPCs, Duplicates und verlorene NPCs. Fehlerquellen: Relink falsch, Respawn zu früh, Chunk nicht geladen, `MISSING_ENTITY` nicht sticky, `AMBIGUOUS` ignoriert, Rollback fehlt, ACTIVE ohne UUID-Prüfung, DISABLED wird nicht blockiert. |
| **5** | Routine / Scheduler / Tick | **6** | Kritisch, weil Tick oft alles triggert. Fehler: Routine läuft ohne gültige EntityRef, Navigation läuft ohne Entity, alte Route wird nach Restart fortgesetzt, Log-Spam, Tick verändert State zu oft, Scheduler wird zu sehr mit Restore/Relink/Persistence vermischt. |
| **6** | Navigation / Pathfinding / Doorway | **5** | Fehler: eigene Fake-Bewegung statt Hytale-Pathfinder, Door-Logik ohne gültige Route, Türen öffnen nur wegen Nähe statt wegen Pfad, Teleport-/Fallback-Bewegung, Navigation läuft trotz fehlender EntityRef. |
| **7** | Marker-System | **7** | Sehr kritischer Block. Hauptfehler: read-only Resolve mutiert MarkerAssignments, fehlende Marker werden automatisch ersetzt, Marker werden bei Restart falsch gespeichert, Remove/Clear löscht Marker trotz unsicherem NPC-Zustand, Markerbesitz ist unklar, Marker-v2 halb eingebaut. |
| **8** | Command-System | **6** | Commands sind gefährlich, weil sie direkt löschen/spawnen/ändern. Fehler: `/clear` löscht Record aber Entity bleibt, `/remove` löscht bei unsicherer Entity, Dry-run mutiert State, Respawn-Command umgeht Relink/Chunk-Gate, destruktive Commands ohne `--force`, Status zeigt Record aber nicht Live-Entity-Lage. |
| **9** | Safety / Kontrollregeln | **2** | Weniger Code-Fehler, aber wichtig als Schutzsystem. Fehler: Safety-Dateien werden nicht geprüft, oder AGENTS.md und safety/*.md widersprechen sich und der Agent entscheidet trotzdem selbst. |
| **10** | Hytale API / Engine | **4** | Fehler entstehen, wenn Engine-Verhalten falsch geraten wird: `roleName` als Keystone-ID missbrauchen, EntityRef/UUID falsch auflösen, Chunk-Loaded-Status falsch annehmen, Door/Interaction/Pathfinder selbst nachbauen statt API nutzen. |

---

## Detailbericht pro Block

## 1. Plugin / Einstieg

**Grobe logische Errors:** 3

### Mögliche Probleme

1. Runtime startet, bevor JSON-Definitionen vollständig geladen sind.
2. Runtime startet, bevor `state.json` sicher geladen wurde.
3. Commands werden registriert, obwohl wichtige Systeme noch nicht bereit sind.

### Warum gefährlich?

Der Einstieg ist wie der Hauptschalter. Wenn die Reihenfolge falsch ist, laufen spätere Systeme mit halben Daten.

### Sichere Regel

Erst laden, dann validieren, dann Runtime starten.

---

## 2. Loader & JSON-Hierarchie

**Grobe logische Errors:** 5

### Mögliche Probleme

1. `hytaleRole`, Keystone-`role` und `npcId` werden vermischt.
2. Kaputter Keystone-Index fällt still auf Legacy-Dateien zurück.
3. Duplicate `id`, `role` oder `hytaleRole` wird nicht hart blockiert.
4. `requiredMarkers` und `markerRoles` passen nicht exakt zusammen.
5. Vorbereitete Profile wie Combat/Spawn/Persistence werden so behandelt, als wären sie schon vollständig aktiv.

### Warum gefährlich?

Der Loader bestimmt, welche NPC-Typen überhaupt existieren dürfen. Wenn hier falsche Definitionen durchrutschen, entstehen später falsche Spawns, falsche Rollen oder kaputte Marker-Zuweisungen.

### Sichere Regel

Engine-Role, Keystone-role und konkrete NPC-Instanz immer getrennt halten.

---

## 3. Persistenz / state.json

**Grobe logische Errors:** 6

### Mögliche Probleme

1. `entityRef` wird gespeichert, obwohl es nur Runtime ist.
2. Runtime-Navigation oder Door-State wird gespeichert.
3. Save passiert pro Tick.
4. Load-Failure oder Partial-Load überschreibt trotzdem `state.json`.
5. MarkerAssignments werden heimlich beim Restore/Reconcile verändert.
6. `currentPosition` wird als sichere Spawn-Wahrheit benutzt, obwohl Chunk/Entity unsicher sind.

### Warum gefährlich?

`state.json` ist die langfristige Wahrheit. Wenn dort falsche Daten landen, bleiben Fehler auch nach Restart erhalten.

### Sichere Regel

Nur stabile Daten persistieren: `npcId`, `entityUuid`, `entityStatus`, `worldId`, `currentPosition`, MarkerAssignments.

Nicht persistieren: `entityRef`, Navigation, Door-State, Action-Runtime.

---

## 4. NPC-Lebenszyklus

**Grobe logische Errors:** 8

### Mögliche Probleme

1. Relink bindet falsche Entity.
2. Respawn passiert, bevor sicher geprüft wurde, ob die alte Entity noch existiert.
3. Auto-Respawn läuft in ungeladenem Chunk.
4. `MISSING_ENTITY` wird nicht sticky gehalten.
5. `AMBIGUOUS` wird ignoriert.
6. Rollback nach fehlerhaftem Spawn fehlt.
7. Status wird auf `ACTIVE` gesetzt, ohne UUID/Ownership sauber zu prüfen.
8. `DISABLED` blockiert nicht zuverlässig Relink/Respawn/Tick.

### Warum gefährlich?

Hier entstehen die schlimmsten Fehler: Ghost-Spawns, doppelte NPCs, verlorene NPCs oder falsche Relinks.

### Sichere Regel

Reihenfolge:

```text
UUID prüfen → EntityRef prüfen → Ownership prüfen → Chunk prüfen → erst dann Relink oder Respawn
```

---

## 5. Routine / Scheduler / Tick

**Grobe logische Errors:** 6

### Mögliche Probleme

1. Routine läuft ohne gültige EntityRef.
2. Navigation läuft ohne Live-Entity.
3. Alte Route wird nach Restart fortgesetzt.
4. Tick erzeugt Log-Spam.
5. Tick markiert State zu oft dirty.
6. Scheduler übernimmt zu viele Aufgaben aus Restore/Relink/Persistence.

### Warum gefährlich?

Tick läuft ständig. Ein kleiner Fehler wird dadurch tausendfach wiederholt.

### Sichere Regel

Ohne gültige EntityRef dürfen nur Relink, Recovery, Status und sichere Admin-Checks laufen.

---

## 6. Navigation / Pathfinding / Doorway

**Grobe logische Errors:** 5

### Mögliche Probleme

1. Eigene Fake-Bewegung ersetzt Hytale-Pathfinder.
2. Door-Logik läuft ohne gültige Route.
3. Türen öffnen nur wegen Nähe, nicht weil sie wirklich auf dem Pfad liegen.
4. Teleport- oder Position-Fallback bewegt NPCs unnatürlich.
5. Navigation läuft trotz fehlender EntityRef.

### Warum gefährlich?

Navigation beeinflusst direkt sichtbares Verhalten. Fehler sieht man sofort: NPC läuft durch Wände, teleportiert oder benutzt Türen falsch.

### Sichere Regel

Hytale-Pathfinder und InteractionChain bevorzugen. Keine eigene Haupt-Pathfinding-Engine bauen.

---

## 7. Marker-System

**Grobe logische Errors:** 7

### Mögliche Probleme

1. Read-only Marker-Resolve verändert MarkerAssignments.
2. Fehlende Marker werden beim Restart automatisch ersetzt.
3. Ersatzmarker werden gespeichert, obwohl Zustand unsicher ist.
4. Marker werden bei Remove/Clear gelöscht, obwohl NPC-Zustand unsicher ist.
5. Markerbesitz ist nicht eindeutig.
6. Marker-v2 wird halb eingebaut und vermischt sich mit Legacy-System.
7. Marker fehlen, aber Fehler wird fälschlich als Entity-Problem behandelt.

### Warum gefährlich?

Marker sind Ankerpunkte für Routine, Spawn, Relink und Navigation. Wenn Marker falsch gebunden werden, läuft der ganze NPC falsch.

### Sichere Regel

Read-only Resolve darf niemals speichern oder MarkerAssignments verändern.

---

## 8. Command-System

**Grobe logische Errors:** 6

### Mögliche Probleme

1. `/knpc clear` löscht Records, aber Live-Entities bleiben in der Welt.
2. `/knpc remove` löscht Record trotz unsicherer Entity-Entfernung.
3. Dry-run verändert State.
4. Respawn-Command umgeht Relink/Chunk-Gate.
5. Destruktive Commands laufen ohne `--force`.
6. `/knpc status` zeigt nur Record-Zustand, aber nicht echte Live-Entity-Lage.

### Warum gefährlich?

Commands sind direkte Admin-Werkzeuge. Ein falscher Command kann gute Daten löschen oder Ghost-Entities erzeugen.

### Sichere Regel

Commands müssen safe-by-default sein. Gefährliche Aktionen brauchen `--force` und klare Prechecks.

---

## 9. Safety / Kontrollregeln

**Grobe logische Errors:** 2

### Mögliche Probleme

1. Safety-Dateien werden bei Codeänderungen nicht geprüft.
2. Widersprüche zwischen AGENTS.md und safety/*.md werden ignoriert.

### Warum gefährlich?

Safety-Dateien schützen die Architektur. Wenn sie ignoriert werden, kommen alte Fehler wieder zurück.

### Sichere Regel

Bei Widerspruch nicht selbst entscheiden. Stoppen und melden.

---

## 10. Hytale API / Engine

**Grobe logische Errors:** 4

### Mögliche Probleme

1. `roleName` wird als Keystone-ID missbraucht.
2. EntityRef/UUID wird falsch aufgelöst.
3. Chunk-Loaded-Zustand wird geraten statt geprüft.
4. Door/Interaction/Pathfinder wird selbst nachgebaut, obwohl Hytale Systeme besitzt.

### Warum gefährlich?

Wenn Engine-Verhalten falsch verstanden wird, baut man Logik gegen die Engine. Das erzeugt Fehler nach Restart, Reload oder Chunk-Laden.

### Sichere Regel

Hytale-API zuerst prüfen. Eigene Logik nur bauen, wenn es keine sichere Engine-Lösung gibt.

---

## Ranking: meiste bis wenigste logische Errors

| Rang | Block | Bereich | Grobe logische Errors |
|---:|---:|---|---:|
| 1 | **4** | NPC-Lebenszyklus | **8** |
| 2 | **7** | Marker-System | **7** |
| 3 | **3** | Persistenz / state.json | **6** |
| 4 | **5** | Routine / Scheduler / Tick | **6** |
| 5 | **8** | Command-System | **6** |
| 6 | **2** | Loader & JSON-Hierarchie | **5** |
| 7 | **6** | Navigation / Pathfinding / Doorway | **5** |
| 8 | **10** | Hytale API / Engine | **4** |
| 9 | **1** | Plugin / Einstieg | **3** |
| 10 | **9** | Safety / Kontrollregeln | **2** |

---

## Wichtigste Baustellen-Reihenfolge

Wenn du das Projekt stabilisieren willst, wäre die sichere Reihenfolge:

1. **NPC-Lebenszyklus stabilisieren**
2. **Marker-System read-only vs. mutierend sauber trennen**
3. **state.json Save/Load hart absichern**
4. **Commands safe-by-default machen**
5. **Tick/Scheduler von Recovery/Persistence sauber trennen**
6. **Navigation/Doorway erst danach ausbauen**
7. **Loader/JSON-Hierarchie weiter sauber halten**

---

## Merksatz

```text
Runtime darf handeln.
Persistence darf erinnern.
Aber Persistence darf niemals unsichere Runtime-Zustände als Wahrheit speichern.
```
