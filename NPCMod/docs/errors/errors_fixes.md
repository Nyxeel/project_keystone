# Marker-State / state.json / Restart — Logic-Review Zusammenfassung

**Projekt:** Hytale-Mod `NPCMod / KeystoneNPC`
**Thema:** Marker-State, `state.json`, Restart-Sicherheit, Commands, Legacy-Fallbacks
**Datum:** 2026-05-13
**Status:** Analyse / Review-Zusammenfassung
**Umsetzung:** Keine Codeänderung in diesem Bericht

---

## 1. Gesamturteil

**Urteil: PARTIAL FAIL**

Der Code ist **nicht grundsätzlich kaputt**, aber er ist noch **nicht sauber genug** für:

- einheitliche `state.json`-Persistenz
- sichere Marker-Zuweisungen
- stabile Restart-/Restore-Logik
- Marker-v2 als spätere Erweiterung
- sichere Commands ohne falsche Erfolgsmeldungen

Das wichtigste Risiko ist:

```text
Marker dürfen in Restore-/Tick-/Validation-/Diagnose-/Recovery-Pfaden nicht automatisch ersetzt, gelöscht oder gespeichert werden.
```

Wenn das passiert, kann `state.json` nach einem Restart falsche Marker enthalten oder gültige Marker verlieren.

---

## 2. Was aktuell gut ist

### 2.1 state.json ist grundsätzlich zentral angebunden

Der Persistenzfluss läuft im Kern über:

- `KeystoneNpcPlugin`
- `JsonFileStateStore`
- `NpcRoutineRunner.restore(...)`
- `saveStateSafely()`

Das ist grundsätzlich gut, weil `state.json` nicht komplett verstreut verwaltet wird.

### 2.2 EntityRef wird nicht persistiert

Gut ist:

```text
entityRef wird nicht in state.json gespeichert.
```

Das ist wichtig, weil `entityRef` nur ein Runtime-Handle ist. Nach einem Restart ist diese Referenz nicht zuverlässig.

Persistente Wahrheit sollen stattdessen sein:

- `npcId`
- `entityUuid`
- `entityStatus`
- `role` / `roleId`
- `state`
- Marker-Zuweisungen
- `worldId`
- `currentPosition`

### 2.3 Load-Failure / Partial-Load blockiert Save

Gut ist:

```text
Wenn state.json unsicher geladen wurde, darf sie nicht automatisch überschrieben werden.
```

Das verhindert, dass eine kaputte oder teilweise geladene Datei durch einen leeren oder unvollständigen State ersetzt wird.

### 2.4 Navigation wird beim Restart gelöscht

Gut ist:

```text
Alte Runtime-Navigation wird nach Restart nicht als Wahrheit übernommen.
```

Das ist korrekt, weil alte Routen nach einem Restart unsicher sind:

- EntityRef kann fehlen
- Chunk kann ungeladen sein
- Zielmarker kann fehlen
- Uhrzeit/Routine kann sich geändert haben
- Door-/Pathfinding-Zustand ist nicht mehr sicher

### 2.5 UUID-Relink ist stark abgesichert

Gut ist:

```text
UUID-Relink ist der stärkste Beweis für dieselbe Entity.
```

Das ist viel sicherer als ein Relink nur über Rolle oder Nähe.

### 2.6 Dynamisches setRoleName("KeystoneNPC_...") ist deaktiviert

Gut ist:

```text
Kein dynamisches NPCEntity.setRoleName("KeystoneNPC_<npcId>_<roleId>_Role")
```

Das ist wichtig, weil Hytale `roleName` als echte Engine-Role behandelt. Dynamische Roles ohne echte Role-Dateien können beim Restart zu verschwundenen NPCs führen.

---

## 3. Hauptproblem: Marker-State ist noch nicht einheitlich genug

### 3.1 Aktueller Zustand

Aktuell werden Marker noch über einzelne Legacy-Felder gespeichert:

```text
bedMarkerId
doorMarkerId
chestMarkerId
foodMarkerId
workMarkerId
chillMarkerId
```

Das funktioniert aktuell grundsätzlich.

### 3.2 Zielzustand für später

Langfristig soll der konkrete NPC-State eher so aussehen:

```text
markerAssignments
```

Also eine einheitliche Struktur pro NPC-Instanz.

### 3.3 Bewertung

**Bewertung:** P2 / strukturelles Risiko

Das ist noch kein kompletter Defekt, aber es ist eine Legacy-Struktur. Für Marker-v2 ist das nicht sauber genug.

### 3.4 Wichtig

Dieser Punkt bedeutet **nicht**, dass Marker-v2 sofort gebaut werden soll.

Die richtige Reihenfolge ist:

1. bestehenden Marker-State stabilisieren
2. mutierende Fallbacks entfernen/entschärfen
3. Commands härten
4. Reconcile/Cleanup absichern
5. Safety-Dateien aktualisieren
6. erst später Marker-v2 planen

---

## 4. Harte Logic-Errors / Risiken im Detail

---

## 4.1 Problem 1 — Mutierender Marker-Fallback

### Befund

Es existiert ein gefährlicher Marker-Fallback, zum Beispiel:

```text
resolveRequiredMarkerWithFallbackAssigning(...)
```

oder alte/kompatible Methoden wie:

```text
resolveRequiredMarkerWithFallback(...)
```

### Problem

Diese Methoden können beim Lesen oder Auflösen eines Markers automatisch einen Ersatzmarker finden und zuweisen.

Gefährlicher Ablauf:

```text
1. NPC hatte Marker A.
2. Marker A fehlt kurz oder Registry ist unsicher.
3. Fallback findet Marker B.
4. NPC bekommt Marker B.
5. Save passiert.
6. state.json speichert Marker B dauerhaft.
```

Damit wird ein Runtime-Fallback plötzlich zur persistenten Wahrheit.

### Warum das gefährlich ist

Beim Restart können Marker kurzzeitig nicht verfügbar sein, obwohl sie später wieder korrekt wären.

Wenn dann automatisch ein Ersatzmarker gespeichert wird, ist der alte korrekte Marker verloren oder überschrieben.

### Betroffene Pfade

Besonders gefährlich sind Fallbacks in:

- Restore
- Tick
- Validation
- Diagnose
- Relink-Anker
- Respawn-Policy
- Recovery

### Zielzustand

Es muss zwei klare Arten geben:

```text
resolveRequiredMarkerReadOnly(...)
```

für:

- Restore
- Tick
- Diagnose
- Validation
- Respawn-Policy
- Relink-Anker

und eine bewusst mutierende Methode nur für:

- Spawn initial
- Admin-Command
- explizites Repair

### Bewertung

**Bewertung:** P1 / kritisch für sichere Marker-Logik

Das ist einer der wichtigsten Fixes vor Marker-v2.

---

## 4.2 Problem 2 — Reconcile-/Cleanup-Logik kann Marker still entfernen

### Befund

Es gibt eine alte oder gefährliche Cleanup-/Reconcile-Logik, zum Beispiel:

```text
reconcilePersistedMarkerAssignments(...)
```

### Problem

Diese Methode kann gespeicherte MarkerAssignments oder alte Marker-Felder automatisch bereinigen.

Gefährlicher Ablauf:

```text
1. state.json enthält gespeicherte Marker.
2. Definition oder Registry passt temporär nicht.
3. Reconcile entfernt Marker automatisch.
4. Dirty wird gesetzt oder Save wird später ausgelöst.
5. state.json wird ohne diese Marker überschrieben.
```

### Warum das gefährlich ist

Das ist nicht dasselbe wie der Fallback-Resolve.

Unterschied:

| Problem | Art |
|---|---|
| `resolveRequiredMarkerWithFallback(...)` | ersetzt Marker beim Lesen |
| `reconcilePersistedMarkerAssignments(...)` | löscht/bereinigt Marker beim Abgleich |

Beide verletzen aber dieselbe Grundregel:

```text
Read-/Restore-/Load-/Diagnose-Pfade dürfen MarkerAssignments nicht heimlich ändern.
```

### Zielzustand

In read-only Kontexten darf Reconcile nur:

- lesen
- prüfen
- warnen
- Diagnose ausgeben
- optional als repair-needed markieren, falls bestehende Logik das erlaubt

Nicht erlaubt:

- Marker automatisch löschen
- Marker automatisch ersetzen
- alte Markerfelder automatisch null setzen
- Dirty nur wegen Reconcile setzen
- Save nur wegen Reconcile auslösen
- kaputte state.json still bereinigen und überschreiben

### Mutierendes Reconcile darf nur erlaubt sein in:

- Admin-Repair-Command
- explizitem Cleanup-Command
- bewusst mutierendem Spawn/Admin-Kontext

### Bewertung

**Bewertung:** P1 / kritisch für Restart- und state.json-Sicherheit

Dieser Punkt gehört zum Marker-State-Safety-Plan, aber eng begrenzt. Kein großer Marker-v2-Umbau.

---

## 4.3 Problem 3 — Commands prüfen Assignment-/Save-Ergebnis nicht streng genug

### Befund

`/knpc marker set ...` schreibt grundsätzlich Marker in `state.json`.

Aber es gibt Risiko durch Aufrufe wie:

```text
scheduler.assignMarkerToNpc(...)
```

wenn deren Ergebnis nicht geprüft wird.

### Problem

Gefährlicher Ablauf:

```text
1. Command wird ausgeführt.
2. Intern schlägt Marker-Zuweisung fehl oder ist unsicher.
3. Ergebnis wird nicht geprüft.
4. Command meldet trotzdem Erfolg.
5. Save passiert eventuell trotzdem.
```

### Warum das gefährlich ist

Der Spieler/Admin glaubt, dass der Marker sicher gesetzt wurde.

In Wahrheit kann Runtime und `state.json` auseinanderlaufen.

### Zielzustand

Commands müssen streng sein:

```text
1. Marker-Zuweisung validieren.
2. Ergebnis prüfen.
3. Nur echte Änderung dirty markieren.
4. saveStateSafely() aufrufen, wenn sofort gespeichert werden soll.
5. Rückgabe prüfen.
6. Save-Failure niemals als Erfolg melden.
7. Bei Save-Failure klar melden oder rollbacken.
```

### Bewertung

**Bewertung:** P1 / Command-Save-Sicherheit

Commands sind Admin-Werkzeuge. Wenn sie falschen Erfolg melden, wird Debugging sehr schwer.

---

## 4.4 Problem 4 — /knpc spawn Rollback nach Save-Failure ist riskant

### Befund

Ablauf ungefähr:

```text
1. NPC wird gespawnt.
2. saveStateSafely() wird ausgeführt.
3. Wenn Save fehlschlägt, wird scheduler.removeNpc(...) aufgerufen.
```

### Problem

`removeNpc(...)` ist nur dann sicher, wenn Live-Entity-Removal auch sicher bestätigt wird.

Gefährlicher Ablauf:

```text
1. Entity wurde gespawnt.
2. Save schlägt fehl.
3. removeNpc(...) versucht aufzuräumen.
4. Entity-Removal ist unsicher oder schlägt fehl.
5. Record wird trotzdem entfernt oder nicht gespeichert.
6. Entity lebt ohne state.json-Record weiter.
```

Das erzeugt einen Orphan.

### Zielzustand

Bei Save-Failure nach Spawn muss klar gelten:

- kein Erfolg melden
- Runtime und Persistenz nicht heimlich entkoppeln
- Entity-Removal muss sicher sein
- wenn Removal unsicher ist, Record nicht blind löschen
- klarer Fehlerbericht

### Bewertung

**Bewertung:** P1/P2

Besser als blindes Speichern, aber noch riskant.

---

## 4.5 Problem 5 — /knpc respawn-missing ignoriert Save-Ergebnis

### Befund

Es gibt Stellen wie:

```text
plugin.saveStateSafely();
```

ohne Rückgabeprüfung.

### Problem

Gefährlicher Ablauf:

```text
1. Respawn oder Relink ändert Runtime/Record.
2. saveStateSafely() schlägt fehl.
3. Rückgabe wird ignoriert.
4. Command läuft weiter und meldet normal.
5. Nach Restart fehlt die Änderung in state.json.
```

### Zielzustand

Jeder Command, der Persistenz-relevante Änderung auslöst, muss prüfen:

```text
boolean saved = plugin.saveStateSafely();
```

Wenn `false`:

- keine Erfolgsmeldung
- klare Fehlermeldung
- optional Rollback oder explizite Warnung: Runtime geändert, Persistenz fehlgeschlagen

### Bewertung

**Bewertung:** P1

Das ist ein klarer Command-Save-Safety-Fail.

---

## 5. Legacy-Fallbacks im Überblick

| Bereich | Status | Risiko |
|---|---|---|
| `capabilities` Legacy-Fallback | bewusst erlaubt | niedrig, solange dokumentiert |
| alte `PersistedNavigation` im Model | lesbar, aber wird gelöscht/ignoriert | niedrig |
| `RolePrefixRelink` | deaktiviert, aber Code-Reste existieren | mittel, falls reaktiviert |
| `activeMarkerIds` Restore-Fallback | Legacy-Kompatibilität | mittel |
| Marker-Fallback mit Auto-Zuweisung | gefährlich | hoch |
| Reconcile/Cleanup von MarkerAssignments | gefährlich, wenn mutierend im Load/Restore | hoch |

Wichtigster Punkt:

```text
Marker-Fallback und Marker-Reconcile dürfen in Restart-/Recovery-/Validation-/Diagnose-Pfaden nicht mutieren.
```

---

## 6. Hytale-API vs. eigene Java-Logik

---

## 6.1 Was bereits gut ist

Der Code nutzt schon echte Hytale-/Engine-Strukturen wie:

- `Ref<EntityStore>`
- `UUIDComponent`
- `TransformComponent`
- `NPCPlugin.spawnEntity(...)`
- `InteractionChain` für Türen
- echte Engine-Roles
- MotionController / Role-System

Das ist grundsätzlich die richtige Richtung.

---

## 6.2 Problematische eigene Java-Logik

### 6.2.1 Direktes Bewegen per Transform

Problematischer Bereich:

```text
EntitySyncService.updateEntityPosition(...)
TransformComponent.setPosition(...)
```

Das wirkt eher wie Teleport/Manual-Movement.

Für normale NPC-Bewegung sollte langfristig Hytale-Pathfinding / MotionController / Instructions genutzt werden.

### Bewertung

**Bewertung:** P2 / späterer Engine-Integration-Fix

Nicht Teil des Marker-State-Fixes.

---

### 6.2.2 Fallback-Bewegung in NpcTickPipeline

Problem:

Wenn EngineNavigation nicht greift, setzt eigene Java-Logik eventuell Positionen aus `NavigationTarget`.

Das widerspricht der Regel:

```text
Keine lineare Eigenbewegung als Hauptsystem.
```

### Bewertung

**Bewertung:** P2

Später separat angehen. Nicht in Marker-State-Fix mischen.

---

### 6.2.3 Chunk-Gate nutzt Reflection

Problem:

Chunk-Loaded-Prüfung nutzt offenbar Reflection auf Methoden wie:

```text
isChunkLoaded(...)
```

Gleichzeitig gibt es bereits direkte Nutzung von:

```text
world.getChunkIfInMemory(ChunkUtil.indexChunkFromBlock(...))
```

### Zielzustand

Chunk-Gate sollte möglichst einheitlich und direkt mit echter Hytale-API arbeiten.

### Bewertung

**Bewertung:** P2

Späterer Engine-API-Cleanup. Nicht Teil des Marker-State-Fixes.

---

### 6.2.4 Door-Fallback manipuliert Block-State direkt

Gut:

```text
InteractionChain
```

Problematisch:

```text
world.setBlockInteractionState(...)
```

Das sollte nicht der normale Hauptpfad sein.

### Bewertung

**Bewertung:** P2/P3

Späterer Door-Engine-Fix. Nicht Teil des Marker-State-Fixes.

---

### 6.2.5 Door-Logik nur für BED/WORK

Aktuell greift Door-Logik offenbar stark an Zuständen wie:

```text
WALKING_TO_BED
WALKING_TO_WORK
```

Problem:

Wenn NPC zu anderen Zielen läuft, zum Beispiel:

- FOOD
- CHEST
- CHILL

kann Door-Logik fehlen oder falsch sein.

### Bewertung

**Bewertung:** P2/P3

Späterer Door-/Routine-Fix. Nicht Teil des Marker-State-Fixes.

---

## 7. Empfohlene Fix-Reihenfolge

---

## Step 1 — Marker-State Audit / Safety-Baseline

Ziel:

```text
Alle Marker-Resolve-, Marker-Command-, Marker-Persistence- und Reconcile-Pfade finden.
```

Noch keine Codeänderung.

Prüfen:

- `MarkerResolver`
- `IdleMarkerService`
- `NpcRoutineRunner`
- `StateTargetingService`
- `RespawnRecoveryService`
- `RelinkWorkflowService`
- `MarkerSetCommand`
- `MarkerClearCommand`
- `JsonFileStateStore`
- `NpcRecord`

---

## Step 2 — Marker-Resolve read-only vs. mutierend trennen

Ziel:

```text
Restore/Tick/Diagnose/Validation/Respawn-Policy nutzen nur read-only Resolve.
```

Mutierender Fallback nur:

- Spawn initial
- Admin-Command
- explizites Repair

---

## Step 3 — MarkerSet / MarkerClear Command-Safety

Ziel:

```text
Commands melden Erfolg nur, wenn Assignment und Save sicher waren.
```

Pflicht:

- Assignment-Ergebnis prüfen
- `saveStateSafely()` Ergebnis prüfen
- Save-Failure nicht als Erfolg melden
- Runtime/state.json-Entkopplung verhindern oder klar melden

---

## Step 4 — state.json Load / Save / Dirty Safety

Ziel:

```text
Load-Failure, Partial-Load, Save-Failure und Dirty-State sauber absichern.
```

Pflicht:

- Save-Failure gibt `false`
- Dirty nur nach echtem Save löschen
- Partial Load blockiert Auto-Overwrite
- Legacy-Marker-Felder kompatibel lassen

---

## Step 5 — Versteckte Fallback-Saves final entfernen/blockieren

Ziel:

```text
Kein Marker-Fallback darf im Hintergrund speichern oder Dirty setzen.
```

Final suchen nach:

- `resolveRequiredMarkerWithFallback`
- `setMarkerId`
- alten Marker-Feldern
- `stateDirty`
- `saveState`
- `saveStateSafely`

---

## Step 6 — Reconcile-/Cleanup-Logik für MarkerAssignments prüfen

Ziel:

```text
reconcilePersistedMarkerAssignments(...) darf in Load/Restore/Validation/Diagnose nicht mutieren.
```

Pflicht:

- alle Aufrufer finden
- prüfen, ob Marker automatisch gelöscht/ersetzt werden
- prüfen, ob danach Save möglich ist
- mutierendes Reconcile nur in Admin/Repair/Cleanup/Spawn-Kontext erlauben

---

## Step 7 — Safety-Dateien aktualisieren

Ziel:

```text
Die finalen Marker-State-Safety-Regeln dokumentieren.
```

Pflichtdateien:

```text
/home/pj/projects/hytale/project_keystone/NPCMod/docs/safety/json_hierarchy.md
/home/pj/projects/hytale/project_keystone/NPCMod/docs/safety/npc_restart_relink_control.md
```

Dokumentieren:

- read-only Marker-Pfade dürfen nicht mutieren
- Reconcile darf in read-only Kontexten nicht löschen/ersetzen
- Commands müssen Save-Failure prüfen
- alte Marker-Felder bleiben bis Marker-v2 kompatibel
- Marker-v2 war nicht Teil dieses Fixes

---

## 8. Was ausdrücklich NICHT Teil dieses Fix-Plans ist

Nicht in diesen Marker-State-Safety-Fix mischen:

- Marker-v2 vollständig bauen
- neue `markerAssignments`-Hauptarchitektur vollständig einführen
- Door-System umbauen
- Navigation umbauen
- Animation umbauen
- Hytale Roles ändern
- Dedupe ändern
- neue Auto-Respawn-Logik bauen
- Pathfinding neu entwickeln
- Engine-Movement komplett ersetzen
- Door-Interaction komplett neu schreiben

Diese Themen gehören später in eigene Pläne.

---

## 9. Wichtigste Review-Fragen

Nach jedem Fix-Step prüfen:

```text
[ ] Wurde nur der erlaubte Scope geändert?
[ ] Wurde kein Marker-v2 eingebaut?
[ ] Mutieren Restore/Tick/Diagnose/Validation/Respawn-Policy keine Marker?
[ ] Löscht Reconcile im Load/Restore nichts automatisch?
[ ] Setzt read-only Resolve kein stateDirty?
[ ] Löst read-only Resolve keinen Save aus?
[ ] Prüfen Commands saveStateSafely()?
[ ] Wird Save-Failure nicht als Erfolg gemeldet?
[ ] Bleiben alte Marker-Felder kompatibel?
[ ] Wird state.json bei Load-Failure/Partial-Load nicht überschrieben?
[ ] Wird Dirty nur nach echtem Save gelöscht?
[ ] Wurden Door/Navigation/Animation/Roles/Dedupe nicht geändert?
[ ] War mvn -q -DskipTests test-compile erfolgreich?
```

---

## 10. Kurzfazit

Der Code ist auf einem guten Fundament, aber der Marker-State ist noch nicht sicher genug.

Die größten echten Probleme sind:

1. Marker-State ist noch Legacy-strukturiert.
2. Mutierender Marker-Fallback kann falsche Marker dauerhaft speichern.
3. Reconcile/Cleanup kann Marker still löschen, wenn nicht strikt getrennt.
4. Marker-Commands prüfen Assignment-/Save-Ergebnis nicht streng genug.
5. Spawn/Respawn-Commands haben Save-Failure-Risiken.
6. Einige eigene Java-Fallbacks sollten später durch sauberere Hytale-API-Nutzung ersetzt werden.

Nächster sinnvoller Fix:

```text
Marker-State-Safety stabilisieren.
Nicht Marker-v2 bauen.
Nicht Door/Navigation/Engine-Fixes mischen.
```



###

Marker aus state.json entfernen wenn richtig!

Richtige Regel
MISSING_ENTITY = NPC existiert noch → Marker behalten
/knpc remove = NPC wird gelöscht → seine Marker löschen
/knpc clear = alle NPCs werden gelöscht → deren Marker löschen
NPC stirbt permanent = NPC löschen → seine Marker löschen
Wichtigster Punkt

Marker gehören bei dir zur NPC-Instanz, nicht nur zur Role.

Also nicht:

role lumberjack hat Marker

sondern:

npcId 18a0... hat bed/work/food/chest Marker
Warum deine state.json wächst

Wahrscheinlich passiert bei /knpc remove index oder /knpc clear:

NPC-Record wird entfernt
aber seine Marker bleiben in markers[]

Dann bleiben alte Marker-Leichen übrig.

Saubere Lösch-Reihenfolge

Bei /knpc remove <index>:

1. NPC finden
2. seine MarkerIds merken:
   bedMarkerId
   workMarkerId
   foodMarkerId
   chestMarkerId
   doorMarkerId
   chillMarkerId

3. Entity sicher entfernen
4. NPC-Record aus npcs[] entfernen
5. genau diese Marker aus markers[] entfernen
   aber nur, wenn kein anderer NPC sie benutzt
6. saveStateSafely() prüfen
7. bei Save-Failure nicht als Erfolg melden

Bei /knpc clear gleich, nur für alle NPCs.

Bei NPC-Tod

Da brauchst du eine klare Policy:

Wenn Tod permanent ist
Entity death event
→ NPC-Record löschen
→ NPC-Marker löschen
→ state.json speichern
Wenn NPC später respawnen soll
Entity death event
→ Record behalten
→ Marker behalten
→ Status z. B. MISSING_ENTITY / RESPAWN_PENDING

Für dein aktuelles System klingt es so, als willst du:

NPC tot = NPC komplett raus

Dann ja: Record + Marker löschen.

Wichtig für den Fix

Das ist nicht derselbe Fix wie read-only Marker-Fallback.

Das ist ein eigener Safety-Step:

Remove/Clear/Death muss NPC-eigene Marker sauber löschen

Nicht nebenbei in Step 4 oder Marker-v2 mischen.
