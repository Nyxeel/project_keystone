# NPCMod / KeystoneNPC — Finaler Lagebericht, Baustellenkarte und Fix-Reihenfolge

**Datum:** 2026-05-13 07:36 CEST  
**Projekt:** Hytale-Mod `NPCMod / KeystoneNPC`  
**Ziel:** Überblick behalten, logische Fehler erkennen, Widersprüche melden, Fix-Reihenfolge festlegen, Marker-v2 einordnen  
**Status dieses Berichts:** Architektur-/Safety-Lagebericht auf Basis der hochgeladenen Reports, der Mindmap und eines statischen Code-Checks im ZIP  

---

## 0. Ganz kurzes Urteil

Du drehst dich **nicht komplett im Kreis**. Es gab echte Fortschritte:

- Restart / Relink / Respawn wurde deutlich sicherer.
- `MISSING_ENTITY` ist jetzt sticky.
- Auto-Respawn ist stärker gegated.
- `state.json` wird bei Load-/Partial-Load-/Save-Problemen viel sicherer behandelt.
- Mutierende Marker-Fallbacks wurden laut Patchreports und Code-Audit entfernt.
- Read-only Marker-Pfade sind jetzt viel besser geschützt.

Aber: Du bist an einem Punkt, wo weitere große „Safety-Runden“ ohne enges Ziel schnell wieder Kreisarbeit werden.

Der nächste sinnvolle Schritt ist deshalb **nicht**: noch ein großer Alles-Review.  
Der nächste sinnvolle Schritt ist:

```text
1. sehr kleine offene P1-Failchecks schließen
2. danach Marker-v2 sauber planen
3. erst danach Marker-v2 in kleinen Schritten bauen
```

Die wichtigsten offenen Baustellen sind aktuell:

```text
P0/P1: NpcRespawnMissingCommand prüft saveStateSafely() noch nicht sauber.
P1: Marker-Zuweisung prüft worldId/MarkerId/MarkerType noch nicht hart genug.
P1: SpawnNpcCommand nutzt bei Save-Failure noch boolean removeNpc(...), obwohl es detailed RemoveResult gibt.
P1/P2: /remove und /clear sind sicher, aber bei Live-Entities aktuell sehr streng/blockierend.
P1/P2: Docs haben leichte Versionsdrift zwischen alter Safety-Datei und neuem Code-/Patchstand.
P2: state.json trennt Welten über worldId, aber noch nicht sauber über serverId/saveId.
P2: NpcRoutineRunner ist weiterhin zu groß.
P2/P3: Navigation/Doorway/Engine-Bewegung später getrennt prüfen.
```

Marker-v2 solltest du **noch nicht sofort implementieren**.  
Du kannst aber **jetzt mit dem Marker-v2-Plan anfangen**, sobald die kleinen P1-Fixpunkte oben geschlossen sind.

---

# 1. Quellenbasis

Dieser Bericht fasst zusammen und bewertet:

- `todos.md`
- `2026-05-13_Block-Logic-Error-Report.md`
- `critical_fixes.md`
- `errors_fixes.md`
- `marker-fixes.md`
- `worldID_fix.md`
- `check_legacy_code_marker_v2.md`
- `2026-05-13_01-21_Restart-Relink-Respawn-Safety-Patch.md`
- `2026-05-13_04-22_State-Load-Marker-Position-Observability-Safety-Patch.md`
- `2026-05-13_04-50_Marker-State-Reconcile-Safety-Patch.md`
- `2026-05-13_06-48_Safety-Docs-Marker-Resolver-Allowlist-Update.md`
- `AGENTS.md`
- `json_hierarchy.md`
- `npc_restart_relink_control.md`
- `MindMap_Overview.png`
- statischer Code-Check in `NPCMod.zip`

Wichtig: Ich konnte hier **kein Maven-Compile-Gate ausführen**, weil `mvn` in dieser Umgebung nicht installiert ist. Der statische Code-Check war aber möglich.

---

# 2. Zentrale Erkenntnis aus allen Reports

Der gefährlichste Ablauf bleibt:

```text
state.json
  -> Restore / Relink / Respawn
  -> Tick / Scheduler
  -> Marker
  -> Commands
  -> Save zurück nach state.json
```

Dort treffen zwei Welten aufeinander:

```text
Persistence = dauerhafte Wahrheit
Runtime     = Live-Zustand im laufenden Server
```

Die Grundregel bleibt:

```text
Runtime darf handeln.
Persistence darf erinnern.
Aber Persistence darf niemals unsichere Runtime-Zustände als Wahrheit speichern.
```

Einfach gesagt:

- `state.json` darf nicht durch einen unsicheren Tick kaputtgeschrieben werden.
- Marker dürfen nicht beim Lesen heimlich ersetzt werden.
- Commands dürfen nicht Erfolg melden, wenn Save oder Rollback fehlschlägt.
- Eine fehlende Live-Entity heißt nicht automatisch: „Spawn Ersatz“.
- Ein fehlender Marker heißt nicht automatisch: „Nimm irgendeinen anderen Marker“.

---

# 3. Mindmap-Gesamtbewertung nach Blöcken

## Block 4 — NPC-Lebenszyklus

**Priorität:** P0/P1  
**Status:** stark verbessert, aber weiterhin zentraler Hotspot  
**Risiko:** Ghost-NPCs, Duplicates, verlorene Records, falsche Relinks

Der Lebenszyklus ist der wichtigste Block. Hier passieren:

- Spawn
- Restore
- UUID-Relink
- Anchor-Fallback
- Respawn
- Remove
- Rollback
- Save

### Was schon besser ist

Die Patchreports zeigen echte Fortschritte:

- `MISSING_ENTITY` bleibt sticky.
- Auto-Respawn läuft nur über Policy, globale Freigabe und Chunk-Gate.
- Role-Prefix-Fallback ist neutralisiert.
- Respawn nutzt den bestehenden NPC-Record und ersetzt nur die Entity-Identity.
- Save-Fehler bleiben sichtbar.

### Was noch offen ist

Der Lifecycle steckt immer noch stark im `NpcRoutineRunner`.

Das ist nicht sofort kaputt, aber gefährlich für spätere Features.

Warum?

Weil `NpcRoutineRunner` aktuell zu viele Rollen gleichzeitig hat:

```text
NPC-Liste verwalten
Restore/Relink/Respawn steuern
Marker zuweisen
Remove/Clear durchführen
Navigation triggern
Door-Runtime halten
Dirty-State markieren
Save-nahe Entscheidungen vorbereiten
```

Wenn du Marker-v2, Death-Policy oder größere NPC-Gruppen später einbaust, wird dieser Block sonst wieder chaotisch.

### Aktuelles konkretes Problem

`SpawnNpcCommand` nutzt nach Save-Failure noch einen boolean-Pfad:

```text
scheduler.removeNpc(npc.npcId())
```

Im Code existiert aber schon ein besserer detailed Remove-Pfad:

```text
removeNpcDetailed(...)
RemoveNpcResult
```

Das ist ein Logik-Risiko, weil boolean nicht sauber unterscheidet:

```text
wirklich entfernt
blockiert
Save fehlgeschlagen
Rollback unvollständig
Entity-Removal unsicher
```

### Empfehlung

**Kurzfristig:** nicht sofort Lifecycle groß refactoren.  
**Zuerst:** Save-/Rollback-Failchecks schließen.  
**Danach:** `NpcRoutineRunner` schrittweise aufteilen.

Zielstruktur später:

```text
NpcLifecycleService
NpcSpawnService
NpcRelinkService
NpcRespawnService
NpcRemovalService
NpcDeathService
```

Aber nicht alles in einem Patch.

---

## Block 7 — Marker-System

**Priorität:** P1  
**Status:** Safety wurde deutlich verbessert, Marker-v2 noch nicht gebaut  
**Risiko:** falsche Marker in `state.json`, Marker-Leichen, falsche NPC-Ziele

Dieser Block ist für Marker-v2 natürlich der wichtigste.

### Was schon erledigt ist

Laut Patchreports und Code-Audit:

- `resolveRequiredMarkerWithFallbackAssigning(...)` ist aus dem aktiven Java-Code entfernt.
- `resolveRequiredMarkerWithFallback(...)` ist aus dem aktiven Java-Code entfernt.
- `resolveRequiredMarkerReadOnly(...)` ist der verbindliche Read-only-Resolver.
- `MarkerRegistry.getNextAvailable(...)` existiert noch, aber nur deprecated und intern im Registry-/Traversal-Bereich.
- Read-only Pfade dürfen Marker nicht mehr heimlich ändern.
- Reconcile darf MarkerAssignments in Restore/Tick/Diagnose/Validation nicht heimlich löschen oder ersetzen.

Das ist ein echter Fortschritt.

### Was noch offen ist

Marker-v2 ist noch nicht umgesetzt.

Aktuell gibt es weiterhin Legacy-Felder:

```text
bedMarkerId
workMarkerId
doorMarkerId
foodMarkerId
chestMarkerId
chillMarkerId
```

Später soll daraus eher werden:

```text
markerAssignments:
  bed   -> markerId
  work  -> markerId
  food  -> markerId
  chest -> markerId
  door  -> markerId
  chill -> markerId
```

### Aktuelles konkretes Problem

`MarkerSetCommand` erzeugt Marker in der Spieler-Welt:

```text
worldId = new WorldId(world.getName())
```

Dann kann er einem Ziel-NPC zugewiesen werden. Wenn der Ziel-NPC theoretisch in einer anderen Welt ist, muss hart geprüft werden:

```text
marker.worldId == npc.worldId
```

Im aktuellen statischen Code-Check prüft `assignMarkerToNpc(...)` zwar:

```text
MarkerType ist für Rolle erlaubt
```

aber nicht hart genug:

```text
Marker existiert wirklich in MarkerRegistry?
Marker hat passenden MarkerType?
Marker.worldId passt zu npc.worldId?
```

Das ist vor Marker-v2 wichtig, weil Marker-v2 sonst eine falsche Weltbindung sauberer speichert, aber logisch trotzdem falsch bleibt.

### Marker-v2-Einordnung

Marker-v2 darf anfangen, sobald diese Punkte erfüllt sind:

```text
[ ] NpcRespawnMissingCommand Save-Failure geschlossen
[ ] MarkerSet / assignMarkerToNpc prüft markerId, markerType, worldId
[ ] SpawnNpcCommand Save-Failure-Rollback nutzt detailed RemoveResult
[ ] Safety-Doku Versionsdrift bereinigt
[ ] klar ist: Marker bei MISSING_ENTITY/NEEDS_RELINK bleiben erhalten
[ ] klar ist: Marker bei echtem NPC-Delete werden zentral gelöscht
```

Dann kannst du Marker-v2 **planen**.

Marker-v2 **implementieren** erst danach.

---

## Block 3 — Persistenz / state.json

**Priorität:** P1  
**Status:** stark verbessert, aber weiter kritisch  
**Risiko:** kaputte Daten bleiben nach Restart dauerhaft falsch

### Was schon besser ist

Der 04:22 Safety-Patch ist hier wichtig:

- kaputte `state.json` wird nicht mehr als leerer gültiger State behandelt.
- Partial-Load überschreibt keine skipped Records automatisch.
- Save-Fehler zählen nicht als Erfolg.
- Dirty wird nur nach echtem Save gelöscht.
- fehlende Position wird nicht mehr zu `(0,0,0)` gemacht.
- Runtime-States wie `WALKING_*` werden nicht mehr als dauerhafte Wahrheit gespeichert.

Das ist ein großer Fortschritt.

### Was noch offen ist

`state.json` trennt aktuell wahrscheinlich über:

```text
worldId
Plugin-Datenordner
```

Aber noch nicht sauber über:

```text
serverId
saveId
worldSaveId
universeId
```

Das ist kein akuter P0-Fehler, solange du nur eine Serverinstanz / einen Datenordner nutzt.

Es wird aber gefährlich, wenn:

```text
Testserver und echter Server denselben Plugin-Datenordner teilen
oder
zwei Saves gleiche worldId-Namen haben
oder
alte state.json in anderem Save geladen wird
```

Dann können passieren:

```text
entityUuid passt nicht mehr
worldId heißt gleich, meint aber andere Welt
Marker werden falscher Welt zugeordnet
NPCs werden falsch restored oder gelöscht
```

### Empfehlung

Nicht blind migrieren.

Zuerst:

```text
Warnung / Diagnose einbauen
aktuellen state.json-Pfad ausgeben
serverId/saveId als Backlog dokumentieren
```

Später Ziel:

```text
keystone-npc/state/<serverId>/state.json
```

oder:

```text
keystone-npc/worlds/<worldSaveId>/state.json
```

Aber nur mit Backup, atomarer Migration und Fail-Schutz.

---

## Block 8 — Command-System

**Priorität:** P1  
**Status:** besser, aber noch nicht fertig  
**Risiko:** Admin-Command erzeugt Runtime/state.json-Drift

Commands sind gefährlich, weil sie direkte Eingriffe machen:

```text
/knpc spawn
/knpc remove
/knpc clear
/knpc marker set
/knpc marker clear
/knpc respawn-missing
```

### Was schon besser ist

- `NpcRemoveCommand` nutzt `RemoveNpcResult`.
- `NpcClearCommand` nutzt `ClearNpcsResult` und Rollback.
- `MarkerSetCommand` prüft Save-Failure und meldet Drift-Risiko.
- `MarkerClearCommand` rollbackt bei Save-Failure.

### Was noch offen ist

#### 1. `NpcRespawnMissingCommand`

Der Code ruft aktuell:

```text
if (result.stateChanged() && !dryRun) {
    plugin.saveStateSafely();
}
```

Das Ergebnis wird nicht geprüft.

Das ist ein klarer P1-Failcheck.

Richtige Regel:

```text
if (!plugin.saveStateSafely())
    keine normale Erfolgsmeldung
    klare Runtime/state.json-Drift-Warnung
```

#### 2. `SpawnNpcCommand`

Bei Save-Failure ruft der Code aktuell noch:

```text
scheduler.removeNpc(npc.npcId())
```

Das gibt nur boolean zurück.

Besser:

```text
removeNpcDetailed / RemoveNpcResult verwenden
blocked / rollback failed / removed sauber melden
```

#### 3. `/remove` und `/clear`

Aktuell sehr sicher, aber streng:

- Wenn Live-Entity vorhanden ist und Removal nicht sicher bestätigt werden kann, wird Record-Delete blockiert.
- Das verhindert Orphans.
- Aber es bedeutet auch: sichtbare Live-NPCs können eventuell nicht bequem gelöscht werden.

Das ist sicher, aber UX-mäßig noch nicht final.

Später brauchst du eine Designentscheidung:

```text
A) bestätigter Remove-Outcome
oder
B) expliziter Admin-Force-Pfad
```

Nicht sofort erzwingen.

---

## Block 5 — Routine / Scheduler / Tick

**Priorität:** P1/P2  
**Status:** Safety verbessert, Architektur noch eng gekoppelt  
**Risiko:** Tick triggert zu viele Systeme zu oft

### Gute aktuelle Regel

Ohne gültige EntityRef darf nicht laufen:

```text
Routine
Navigation
Action
Door-Logik
Movement-Fallback
Position-Sync
```

Erlaubt sind nur:

```text
Relink
Recovery
Status / Debug
sichere Admin-Checks
```

### Was noch offen ist

Der Scheduler/Runner ist weiterhin zu zentral.

Das muss nicht sofort umgebaut werden, aber für spätere Features ist es eine Bremse:

- Marker-v2
- DeathPolicy
- mehrere NPC-Typen
- Settlement-System
- große Städte
- bessere Debug-Diagnose

### Empfehlung

Erst nach den P1-Failchecks:

```text
NpcRoutineRunner in kleinere Services schneiden
aber nur Step-by-Step
nicht als Big-Bang-Refactor
```

Sinnvolle Reihenfolge:

```text
1. NpcRemovalService extrahieren
2. NpcMarkerAssignmentService extrahieren
3. NpcRespawnService extrahieren
4. NpcTickService / RoutineService klarer trennen
```

---

## Block 6 — Navigation / Pathfinding / Doorway

**Priorität:** P2/P3  
**Status:** wichtig, aber nicht jetzt zuerst  
**Risiko:** sichtbares Fehlverhalten, aber weniger gefährlich als state.json/commands

### Problemfelder

- eigene Bewegung über Transform kann wie Teleport wirken.
- Door-Logik darf nicht ohne echte Route laufen.
- Türen sollen nicht nur wegen Nähe öffnen.
- Door-State darf nicht in `state.json` landen.
- Navigation darf nicht ohne Live-Entity laufen.

### Einordnung

Das ist wichtig, aber nicht der nächste Fix.

Warum?

Weil Navigation sichtbar falsch sein kann, aber Marker-/Persistence-/Command-Fehler speichern falsche Wahrheit dauerhaft.

Also:

```text
Erst Sicherheit der Daten.
Dann Bewegung schöner machen.
```

### Späterer Zielzustand

```text
Routine entscheidet Ziel
Navigation nutzt Hytale Pathfinder / Instructions
Doorway behandelt Türen auf der Route
Persistence speichert keine Door-/Navigation-Runtime
```

---

## Block 2 — Loader & JSON-Hierarchie

**Priorität:** P2  
**Status:** relativ stabil  
**Risiko:** falsche Definitionen erzeugen falsche Spawns

### Was gut ist

Die JSON-/Role-Trennung ist grundsätzlich richtig:

```text
Hytale Engine-Role != Keystone roleId != npcId
```

### Was du weiter schützen musst

- keine dynamischen `KeystoneNPC_...` Engine-Roles
- kein kaputter Keystone-Index mit stillem Legacy-Fallback
- keine Duplicate `id`, `role`, `hytaleRole`
- `requiredMarkers` und `markerRoles` müssen exakt passen
- vorbereitete Profile nicht als voll aktiv behandeln

### Marker-v2-Bezug

Marker-v2 muss später mit der JSON-Hierarchie zusammenpassen:

```text
requiredMarkers = was die Role braucht
markerRoles     = welcher Marker-Typ dahinter steckt
markerAssignments = konkrete NPC-Instanz-Zuweisung in state.json
```

Nicht vermischen:

```text
Role definiert Bedarf.
NPC-Instanz speichert konkrete Marker.
```

---

## Block 1 — Plugin / Einstieg

**Priorität:** P2  
**Status:** nicht größter Brand  
**Risiko:** falsche Start-Reihenfolge

Wichtig bleibt:

```text
1. Plugin init
2. JSON-Definitionen laden
3. state.json laden
4. validieren
5. Runtime-Systeme starten
6. Commands registrieren oder nutzbar machen
```

Wenn Commands zu früh nutzbar wären, könnten sie mit halben Daten arbeiten.

Aktuell sehe ich diesen Block nicht als nächsten Fix-Hotspot.

---

## Block 9 — Safety / Kontrollregeln

**Priorität:** dauerhaft P1  
**Status:** gut, aber Versionsdrift beachten  
**Risiko:** alte Regeln widersprechen neuen Patches

### Wichtigster Befund

Es gibt eine kleine Versionsdrift zwischen einzelnen hochgeladenen Safety-Dateien und dem aktuellen ZIP-/Patchstand.

Beispiel:

- Die neueren Patchreports sagen: `resolveRequiredMarkerWithFallbackAssigning(...)` ist entfernt.
- Der aktuelle Code im ZIP bestätigt: kein aktiver Treffer in `src/main/java`.
- Eine ältere standalone `npc_restart_relink_control.md` enthält aber noch die alte Formulierung, dass `resolveRequiredMarkerWithFallbackAssigning(...)` mutierend in Spawn/Admin-Flows genutzt werde.

Das ist kein Code-Fehler, aber ein Doku-Widerspruch / Versionsdrift.

### Empfehlung

Safety-Dateien im Projekt-ZIP als aktuelle Wahrheit nehmen, aber standalone Datei bereinigen oder nicht mehr als aktiv betrachten.

Konkreter Fix:

```text
Alle Safety-Dokumente müssen einheitlich sagen:
resolveRequiredMarkerWithFallbackAssigning(...) ist entfernt.
resolveRequiredMarkerWithFallback(...) ist entfernt.
Mutierende Marker-Zuweisung läuft nur über explizite Admin-/Spawn-/Repair-Methoden.
getNextAvailable(...) bleibt deprecated und darf nicht in read-only Pfade.
```

---

## Block 10 — Hytale API / Engine

**Priorität:** P2/P3  
**Status:** Research wichtig, aber nicht nächster Daten-Safety-Fix  
**Risiko:** eigene Logik baut gegen Engine

Wichtige No-Gos:

```text
kein dynamisches setRoleName("KeystoneNPC_...")
keine eigene Haupt-Pathfinding-Engine
keine lineare Eigenbewegung als Hauptsystem
keine Door-Block-State-Hacks als Hauptpfad, wenn InteractionChain funktioniert
kein Raten bei Chunk-Loaded-Status
```

### Empfehlung

Hytale-API-Research später gezielt machen:

```text
1. EntityRef / UUID lookup prüfen
2. Chunk loaded API sauber prüfen
3. Pathfinder / Instruction / MotionController prüfen
4. DoorInteraction / InteractionChain prüfen
```

Aber nicht in den nächsten Marker-v2-Fix mischen.

---

# 4. Logische Errors und Widersprüche

## 4.1 Echte offene Logic-Errors / Failcheck-Lücken

### P1 — `NpcRespawnMissingCommand` ignoriert Save-Ergebnis

**Problem:**  
`saveStateSafely()` wird aufgerufen, aber Ergebnis wird nicht geprüft.

**Gefahr:**  
Runtime ändert sich, `state.json` speichert nicht, Command meldet trotzdem Erfolg.

**Fix:**  
Wenn Save fehlschlägt:

```text
keine normale Erfolgsmeldung
klare Warnung: Runtime/state.json drift risk
optional: Rollback, falls sicher möglich
```

---

### P1 — Marker-Zuweisung prüft worldId nicht hart genug

**Problem:**  
Marker wird in Spieler-Welt gesetzt, Ziel-NPC kann theoretisch in anderer Welt sein.

**Gefahr:**  
NPC bekommt Marker aus falscher Welt.

**Fix:**  
`assignMarkerToNpc(...)` muss prüfen:

```text
markerId existiert in MarkerRegistry
marker.type == requested MarkerType
marker.worldId == npc.worldId
```

---

### P1 — `SpawnNpcCommand` nutzt boolean Rollback

**Problem:**  
Nach Save-Failure wird `scheduler.removeNpc(...)` mit boolean-Rückgabe genutzt.

**Gefahr:**  
Command kann nicht sauber melden, ob Entity-Removal blockiert, Record nicht gelöscht oder Rollback unvollständig war.

**Fix:**  
`SpawnNpcCommand` soll detailed RemoveResult nutzen.

---

### P1/P2 — Live-NPC Remove ist sicher, aber blockierend

**Problem:**  
`removeLiveEntity(...)` blockiert aktuell, wenn live Entity removal nicht sicher bestätigt werden kann.

**Gut:**  
Das verhindert Orphans.

**Schlecht:**  
Admin kann sichtbare Live-NPCs eventuell nicht praktisch löschen.

**Fix später:**  
Entweder:

```text
A) bestätigter Remove-Outcome mit echter Entity-Removal-Bestätigung
```

oder:

```text
B) expliziter Admin-Force-Pfad mit sehr klarer Warnung und Recovery-Mechanik
```

---

### P2 — Server-/Save-Trennung fehlt wahrscheinlich

**Problem:**  
`worldId` reicht nicht immer, wenn mehrere Server/Saves denselben Datenordner teilen.

**Gefahr:**  
Falsche NPCs/Marker aus anderem Save werden geladen.

**Fix später:**  
Nicht sofort migrieren. Erst Diagnose/Warnung, später saubere Namespace-Struktur.

---

### P2 — Role-Prefix-Code ist deaktiviert, aber noch sichtbar

**Problem:**  
Methoden wie `tryRolePrefixRelinkEntityRef(...)` existieren noch als Stub / No-Match-Pfad.

**Gut:**  
Sie geben aktuell `NO_MATCH` zurück und setzen kein dynamisches roleName.

**Risiko:**  
Späterer Agent könnte sie fälschlich reaktivieren.

**Fix:**  
Später entfernen oder noch deutlicher als verboten/deprecated markieren.

---

## 4.2 Keine akuten Widersprüche im Kerncode

Im aktuellen Code-Audit sehe ich keinen aktiven Widerspruch wie:

```text
read-only Marker-Fallback mutiert noch aktiv
```

oder:

```text
aktives setRoleName("KeystoneNPC_...")
```

oder:

```text
resolveRequiredMarkerWithFallbackAssigning(...) wird noch in Java genutzt
```

Das ist gut.

---

## 4.3 Doku-/Versionsdrift

Der größte Widerspruch liegt nicht im Code, sondern zwischen alten und neuen Dokumentständen.

### Alte Aussage

```text
mutierend: resolveRequiredMarkerWithFallbackAssigning(...) nur in spawn/admin assignment flows
```

### Neuer gültiger Stand

```text
resolveRequiredMarkerWithFallbackAssigning(...) ist entfernt.
resolveRequiredMarkerWithFallback(...) ist entfernt.
```

### Entscheidung

Die neue Aussage ist die richtige Richtung.

### Fix

Alte Formulierungen ersetzen durch:

```text
Mutierende Marker-Zuweisung ist nur in expliziten Spawn/Admin/Repair/Cleanup-Kontexten erlaubt.
Die alten Fallback-Methoden bleiben entfernt.
```

---

# 5. Drehen wir uns im Kreis?

## Kurze Antwort

**Teilweise Gefahr ja, aber aktuell noch nicht komplett.**

Du hast echte technische Fortschritte gemacht. Besonders:

```text
Restart/Relink/Respawn Safety
state.json Load/Save Safety
Marker Read-only Safety
Reconcile Safety
Safety-Doku Sync
```

Das war nicht Kreisarbeit.

## Wo die Kreis-Gefahr beginnt

Du drehst dich im Kreis, wenn du jetzt wieder:

```text
noch einen riesigen Gesamtplan
noch einen großen Safety-Review
noch einen neuen Marker-v2-Plan
noch einen neuen Architekturbericht
```

machst, ohne die kleinen offenen P1-Punkte umzusetzen.

## Wie du aus dem Kreis rauskommst

Ab jetzt gilt:

```text
Ein Step = ein kleines Problem.
Nach jedem Step = Review.
Bei FAIL = nur diesen Step fixen.
Erst bei PASS = nächster Step.
```

Die nächsten Steps müssen sehr konkret sein:

```text
Step A: NpcRespawnMissingCommand Save-Failure
Step B: Marker worldId/type/id Validation
Step C: Spawn rollback detailed RemoveResult
Step D: Safety-Doku Versionsdrift bereinigen
Step E: Marker-v2 Plan Mode
```

Wenn du stattdessen wieder alles gleichzeitig prüfst, kommst du langsamer voran.

---

# 6. Fix-Reihenfolge nach Priorität

## Phase 0 — Sofortige Stabilitätsfixes vor Marker-v2

Diese Phase ist Pflicht, bevor Marker-v2 wirklich implementiert wird.

---

### Step 0.1 — `NpcRespawnMissingCommand` Save-Failure prüfen

**Block:** 8 + 3 + 4  
**Priorität:** P1  
**Scope:** sehr klein

Ziel:

```text
Wenn result.stateChanged() und !dryRun:
saveStateSafely() prüfen.
```

Erfolg nur melden, wenn Save erfolgreich war.

Nicht ändern:

```text
keine Respawn-Policy ändern
kein Marker-v2
kein Relink-Refactor
kein Command-Redesign
```

Review-Frage:

```text
Kann der Command nach Save-Failure noch normal „Respawn complete“ melden?
```

Wenn ja: FAIL.

---

### Step 0.2 — Marker-Zuweisung mit worldId/type/id-Gate härten

**Block:** 7 + 3 + 8  
**Priorität:** P1  
**Scope:** klein

Ziel:

`assignMarkerToNpc(...)` darf nur schreiben, wenn:

```text
markerId existiert
marker.type passt
marker.worldId == npc.worldId
MarkerType ist für roleId erlaubt
```

Nicht ändern:

```text
kein Marker-v2
keine neue markerAssignments-Map
keine Legacy-Felder löschen
keine Reconcile-Änderung
```

Review-Frage:

```text
Kann ein NPC einen Marker aus anderer Welt bekommen?
```

Wenn ja: FAIL.

---

### Step 0.3 — `SpawnNpcCommand` Save-Failure-Rollback detaillieren

**Block:** 4 + 8  
**Priorität:** P1  
**Scope:** klein

Ziel:

`SpawnNpcCommand` soll bei Save-Failure nicht mehr nur boolean `removeNpc(...)` nutzen.

Besser:

```text
RemoveNpcResult auswerten
removed / blocked / rollback failed / unsafe outcome sauber melden
```

Nicht ändern:

```text
kein Spawn-System umbauen
kein Respawn-System umbauen
kein Entity-Removal-Redesign
```

Review-Frage:

```text
Kann Spawn bei Save-Failure noch behaupten, Rollback sei fertig, obwohl Remove blockiert war?
```

Wenn ja: FAIL.

---

### Step 0.4 — Safety-Doku Versionsdrift bereinigen

**Block:** 9  
**Priorität:** P1/P2  
**Scope:** Markdown-only

Ziel:

Alle Safety-Dokumente müssen den gleichen Marker-Resolver-Stand sagen:

```text
resolveRequiredMarkerWithFallbackAssigning entfernt
resolveRequiredMarkerWithFallback entfernt
resolveRequiredMarkerReadOnly verbindlich
getNextAvailable deprecated, nicht in read-only Pfaden
```

Nicht ändern:

```text
kein Java-Code
kein Marker-v2
keine neuen Regeln ohne Codebezug
```

Review-Frage:

```text
Gibt es noch alte Formulierungen, die removed Methoden als erlaubten Mutationspfad nennen?
```

Wenn ja: FAIL.

---

## Phase 1 — Marker-v2 Plan Mode

**Start:** nach Phase 0  
**Priorität:** P1/P2  
**Ziel:** Marker-v2 planen, noch nicht bauen

Jetzt darfst du Marker-v2 aktiv einplanen.

Aber nur PLAN Mode.

### Marker-v2 Ziel

Von Legacy:

```text
bedMarkerId
workMarkerId
doorMarkerId
foodMarkerId
chestMarkerId
chillMarkerId
```

zu:

```text
markerAssignments:
  bed:
    markerId: ...
    markerType: BED
  work:
    markerId: ...
    markerType: WORK
```

### Wichtigste Marker-v2 Regeln

```text
requiredMarkers kommen aus NPC-Definition.
markerRoles mappen logischen Namen auf MarkerType.
markerAssignments gehören zur konkreten NPC-Instanz.
MarkerRecord speichert worldId + position + type.
Read-only Resolve mutiert nie.
Migration mutiert nie automatisch beim Load.
```

### Marker-v2 darf NICHT bedeuten

```text
fehlender Marker wird automatisch ersetzt
alte Legacy-Felder werden hart gelöscht
state.json wird beim Load automatisch migriert und überschrieben
Commands ändern Marker ohne Save-Failcheck
NPC bekommt Marker aus anderer Welt
```

---

## Phase 2 — Marker-v2 technische Vorbereitung

**Start:** erst nach Plan-Review PASS

### Step 2.1 — MarkerAssignment-Modell ergänzen

Ziel:

```text
MarkerAssignment als Modell ergänzen
NpcRecord kann markerAssignments halten
Legacy-Felder bleiben vorerst kompatibel
```

Nicht sofort:

```text
keine automatische Migration
keine Legacy-Felder löschen
keine Commands komplett umbauen
```

---

### Step 2.2 — Read-only Resolver auf neue Struktur vorbereiten

Ziel:

Resolver liest:

```text
1. markerAssignments, falls vorhanden
2. Legacy-Feld als Kompatibilitätsfallback
```

Aber:

```text
kein Schreiben im read-only Pfad
kein stateDirty
kein Save
```

---

### Step 2.3 — Commands schreiben bewusst in Marker-v2

Ziel:

`/knpc marker set` schreibt in `markerAssignments`.

Pflicht-Gates:

```text
marker exists
marker type passt
marker worldId passt
role erlaubt marker
saveStateSafely true
rollback bei Save-Failure oder klare Drift-Warnung
```

---

### Step 2.4 — Remove/Clear sammelt Marker aus beiden Strukturen

Ziel:

Owned Marker Cleanup muss sammeln aus:

```text
legacy fields
markerAssignments
runtime door/navigation references, falls relevant
```

Aber:

```text
MISSING_ENTITY -> Marker behalten
NEEDS_RELINK -> Marker behalten
echter Delete -> unbenutzte eigene Marker löschen
```

---

### Step 2.5 — Migration nur explizit

Ziel:

Alte Records können umgestellt werden.

Aber nicht automatisch beim Load.

Sichere Optionen:

```text
/knpc marker migrate --dry-run
/knpc marker migrate --apply
Backup vorher
Save-Ergebnis prüfen
Partial-Load blockiert Migration
```

---

## Phase 3 — Marker-v2 Abschluss und Legacy-Abbau

**Start:** erst wenn Marker-v2 stabil läuft

Dann erst:

```text
Legacy-Felder als deprecated markieren
Schreibpfade auf markerAssignments umstellen
Diagnose anzeigen: legacy vs v2
später alte Felder nicht mehr neu schreiben
noch später alte Felder entfernen
```

Nicht zu früh löschen.

---

## Phase 4 — Lifecycle-Refactor

**Start:** nach Marker-v2 Basis oder parallel nur als einzelner kleiner Service-Step

Ziel:

```text
NpcRoutineRunner entlasten
```

Sinnvolle Reihenfolge:

```text
1. NpcRemovalService
2. NpcMarkerAssignmentService
3. NpcRespawnService
4. NpcRelinkService
5. NpcTickPipeline weiter entkoppeln
```

Wichtig:

Nicht mit Marker-v2 in einem Commit mischen.

---

## Phase 5 — ServerId / SaveId Persistenz-Namespace

**Start:** nach Daten-Safety und Marker-v2 Plan

Ziel:

```text
state.json nicht versehentlich zwischen Servern/Saves teilen
```

Erst Diagnose:

```text
aktueller state.json Pfad
worldId vorhanden?
serverId/saveId vorhanden?
Warnung bei fehlender Namespace-Info
```

Später Migration:

```text
Backup
neuer Pfad
atomarer Save
Load-Failure-Schutz
Rollback
kein Löschen alter state.json ohne Bestätigung
```

---

## Phase 6 — Navigation / Doorway / Hytale Engine

**Start:** nach Daten-/Marker-Safety

Ziel:

```text
Hytale Pathfinder / InteractionChain sauberer nutzen
Transform-Fallback minimieren
Door-Logik routenbasiert statt Nähe-basiert
```

Nicht mit Marker-v2 mischen.

---

# 7. Was aktuell eher zu vernachlässigen ist

Nicht ignorieren, aber später:

```text
CombatProfile vollständig aktivieren
Appearance-Apply-System
Drops
Faction-System
SpawnProfile als vollständiges Spawn-System
Worldgen / Settlement-Auto-Registrierung
große Doorway-Optimierung
Animation-Schönheit
```

Warum später?

Weil sie weniger gefährlich sind als:

```text
state.json kaputt speichern
NPC doppelt spawnen
falsch relinken
Marker falsch persistieren
Command meldet falschen Erfolg
```

---

# 8. Wichtigste Regression-Tests

Nach jedem Safety-Fix:

```text
mvn -q -DskipTests test-compile
```

Zusätzlich manuell:

## Test A — Save-Failure bei RespawnMissing

```text
Save künstlich fehlschlagen lassen
/knpc respawn-missing ausführen
Erwartung: keine normale Erfolgsmeldung
```

## Test B — Marker worldId mismatch

```text
NPC in Welt A
Spieler in Welt B
/knpc marker set work <npc>
Erwartung: blockiert
```

## Test C — Spawn Save-Failure Rollback

```text
NPC spawnen
Save fehlschlagen lassen
Erwartung: detaillierter Rollback-Status, keine falsche Erfolgsmeldung
```

## Test D — Restart mit fehlendem Marker

```text
Marker aus Registry entfernen / fehlt
Server restart
Erwartung: kein Ersatzmarker, kein Save mit falschem Marker
```

## Test E — MISSING_ENTITY Marker behalten

```text
NPC MISSING_ENTITY
/knpc clear oder cleanup prüfen
Erwartung: Marker nicht wegen Missing/Relink-Unsicherheit löschen
```

## Test F — Echter Delete entfernt eigene unbenutzte Marker

```text
NPC ohne unsichere Live-Entity löschen
Erwartung: eigene unbenutzte Marker weg
andere NPCs mit gleichem Marker bleiben geschützt
```

## Test G — Kein Role-Prefix

```text
grep KeystoneNPC_ im aktiven Java-Code
Erwartung: kein dynamisches setRoleName
```

## Test H — Keine mutierenden Marker-Fallbacks

```text
grep resolveRequiredMarkerWithFallback
Erwartung: keine aktiven Java-Treffer
```

---

# 9. Konkreter nächster Agent-Arbeitsplan

## Gesamtregel

Immer nur ein Step.

Nicht mehrere Baustellen in einem Patch.

---

## Agent Step 1 — RespawnMissing Save-Failure

```text
MODUS: enger Safety-Fix
Ziel: NpcRespawnMissingCommand prüft saveStateSafely() Ergebnis.
Nicht ändern: Marker-v2, Respawn-Policy, Relink, Navigation, Door, JSON-Hierarchie.
Compile: mvn -q -DskipTests test-compile
Review: Kann Save-Failure noch als Erfolg erscheinen?
```

---

## Agent Step 2 — Marker worldId/type/id Gate

```text
MODUS: enger Safety-Fix
Ziel: assignMarkerToNpc(...) blockiert falsche markerId/type/worldId.
Nicht ändern: Marker-v2, Reconcile, Legacy-Felder, Door, Navigation.
Compile: mvn -q -DskipTests test-compile
Review: Kann NPC Marker aus anderer Welt bekommen?
```

---

## Agent Step 3 — Spawn rollback detailed Result

```text
MODUS: enger Safety-Fix
Ziel: SpawnNpcCommand nutzt detailed RemoveResult statt boolean removeNpc(...).
Nicht ändern: Entity-Removal-Design, Respawn-Policy, Marker-v2.
Compile: mvn -q -DskipTests test-compile
Review: Wird Rollback-Ergebnis ehrlich gemeldet?
```

---

## Agent Step 4 — Safety-Doku Sync

```text
MODUS: Markdown-only
Ziel: alte Resolver-Formulierungen entfernen.
Nicht ändern: Java-Code.
Review: Keine Doku sagt mehr, removed fallback method sei erlaubter Mutationspfad.
```

---

## Agent Step 5 — Marker-v2 Plan Mode

```text
MODUS: PLAN ONLY
Ziel: Marker-v2 technische Architektur planen.
Nicht implementieren.
Muss enthalten:
- Schema
- Migration
- Legacy-Kompatibilität
- Commands
- Read-only Resolver
- Remove/Clear Cleanup
- Tests
- Rollback
```

---

# 10. Finales Fazit

Dein Projekt ist nicht verloren und muss nicht neu gebaut werden.

Du hast echte Safety-Fortschritte gemacht.

Der nächste Fehler wäre aber, jetzt zu früh Marker-v2 zu bauen oder wieder einen riesigen Refactor zu starten.

Die richtige Reihenfolge ist:

```text
1. kleine offene P1-Failchecks schließen
2. Safety-Doku-Versionen angleichen
3. Marker-v2 nur planen
4. Marker-v2 in kleinen Schritten bauen
5. danach NpcRoutineRunner schrittweise entlasten
6. danach Navigation/Door/Hytale-Engine schöner machen
```

Der wichtigste Satz bleibt:

```text
Erst Daten-Sicherheit.
Dann Marker-v2.
Dann Komfort und schöne Bewegung.
```

