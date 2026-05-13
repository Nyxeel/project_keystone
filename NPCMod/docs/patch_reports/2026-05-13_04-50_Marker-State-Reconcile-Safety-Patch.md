# Gesamtbericht — Marker-State / Reconcile / state.json Safety Patch

**Zeitpunkt:** 2026-05-13 04:50:00 CEST  
**Thema:** Marker-State-, Reconcile- und state.json-Safety-Härtung für NPCMod / KeystoneNPC  
**Scope:** Agent Steps 1–7  
**Ergebnis:** FINAL PASS  
**Commitfähig:** Ja, sofern der lokal gemeldete `mvn -q -DskipTests test-compile` im echten Projekt weiterhin erfolgreich ist.

---

## 1. Kurzfazit

Der Patch stabilisiert den bestehenden Marker-State und die `state.json`-Speicherung, ohne Marker-v2 einzubauen.

Wichtigste Ergebnisse:

- Marker-Read-/Tick-/Restore-/Diagnose-/Validation-/Respawn-Policy-Pfade mutieren keine Marker mehr.
- Der gefährliche automatische mutierende Marker-Fallback wurde entfernt.
- `resolveRequiredMarkerWithFallbackAssigning(...)` ist entfernt.
- Ring-/Nearest-Fallback-Schreibpfade sind entfernt.
- `MarkerSetCommand` und `MarkerClearCommand` melden Save-Failure nicht mehr als Erfolg.
- `MarkerClearCommand` rollt Runtime bei Save-Failure zurück.
- `MarkerSetCommand` meldet Runtime/state.json-Drift ehrlich, statt Erfolg vorzutäuschen.
- Unbekannte aktive Marker-Typen in `state.json` markieren jetzt Partial-Load statt stiller Bereinigung.
- Reconcile-/Cleanup-Logik löscht oder ersetzt MarkerAssignments nicht mehr in read-only/restore-/respawn-nahen Pfaden.
- Safety-Dateien wurden final aktualisiert.
- Marker-v2 wurde bewusst nicht umgesetzt.
- Door, Navigation, Animation, Roles, Dedupe und Respawn-/Relink-Policy wurden nicht als Feature geändert.

---

## 2. Geänderte / betroffene Dateien

### 2.1 `MarkerResolver.java`

Diese Datei wurde von gefährlicher Compatibility-/Fallback-Logik bereinigt.

#### Read-only bleibt read-only

Jetzt bleiben nur sichere Marker-Resolver-Funktionen übrig:

```text
markerIdForType(...)
setMarkerIdForType(...)
resolveRequiredMarkerReadOnly(...)
```

`resolveRequiredMarkerReadOnly(...)` liest nur bestehende Marker-Zuweisungen.

Nicht erlaubt in read-only Resolve:

```text
Marker automatisch ersetzen
bedMarkerId/workMarkerId/... ändern
stateDirty setzen
saveStateSafely() auslösen
Fallback als persistente Wahrheit vorbereiten
```

---

#### Mutierender Fallback entfernt

Entfernt oder blockiert wurden:

```text
resolveRequiredMarkerWithFallbackAssigning(...)
resolveRequiredMarkerWithFallback(...)
resolveRingFallbackAnchorMarkerId(...)
MARKER_FALLBACK_SELECTED
automatischer markerRegistry.getNextAvailable(...)-Fallback aus Resolver/Runner-Kontext
```

Damit kann ein fehlender Marker nicht mehr automatisch durch einen anderen Marker ersetzt und später indirekt in `state.json` gespeichert werden.

---

### 2.2 `NpcRoutineRunner.java`

Diese Datei war der zentrale Risikobereich für Marker-Resolve, Spawn/Admin-Mutation und Reconcile.

#### Validation-Pfad auf read-only umgestellt

Vorher konnte die Spawn-/Marker-Validation indirekt mutieren.

Jetzt:

```text
Validation -> resolveRequiredMarkerReadOnly(...)
```

Dadurch gilt:

```text
Validation prüft Marker
Validation meldet fehlende Marker
Validation ersetzt Marker nicht
Validation speichert nicht
```

---

#### Restore / Tick / Diagnose bleiben read-only

Bestätigte read-only Pfade:

```text
Restore-Diagnose
Tick-nahe Diagnose
Marker-Diagnose
Spawn-Validation
Routine-/Target-Validation
Respawn-Policy-Prüfung
Recovery-Check
```

Diese Pfade dürfen keine Marker-Felder ändern.

---

#### Mutierende Pfade bleiben explizit

Erlaubte mutierende Kontexte bleiben:

```text
Spawn/Admin-Erzeugung
aktive Markerbindung bei explizitem Spawn/Admin
explizite Admin-Marker-Zuweisung
```

Nicht erlaubt:

```text
Restore mutiert Marker
Tick mutiert Marker
Diagnose repariert Marker
Validation repariert Marker
Respawn-Policy repariert Marker
```

---

#### Reconcile aus restore-/respawn-nahem Generic-Spawnpfad entfernt

Die relevante Reconcile-Methode heißt im aktuellen Code nicht `reconcilePersistedMarkerAssignments(...)`, sondern:

```text
reconcileMarkerAssignmentsForSpawnOrAdmin(...)
```

Problem:

Ein mutierendes Reconcile im restore-/respawn-nahen Pfad kann alte MarkerAssignments still löschen oder ersetzen.

Fix:

```text
spawnNpcEntity(...)
-> kein automatisches reconcileMarkerAssignmentsForSpawnOrAdmin(...)
```

Damit gilt:

```text
Restart löscht Marker nicht automatisch
Restart ersetzt Marker nicht automatisch
Validation mutiert MarkerAssignments nicht
Diagnose mutiert MarkerAssignments nicht
state.json wird nicht wegen Marker-Reconcile überschrieben
```

---

### 2.3 `MarkerSetCommand.java`

#### Save-Failure wird nicht mehr als Erfolg behandelt

`/knpc marker set` prüft jetzt das Save-Ergebnis.

Wichtiges Verhalten:

```text
Markeränderung durchgeführt
saveStateSafely() schlägt fehl
-> keine Erfolgsmeldung
-> klare Fehlermeldung
-> Runtime geändert, state.json nicht aktualisiert wird ehrlich gemeldet
```

Das verhindert stille Runtime/state.json-Entkopplung.

---

#### Zusätzliche Validierung

Ergänzt wurde:

```text
finite Positionsprüfung
No-Op-Erkennung
nur echte Änderung führt zu Save
```

Damit werden keine ungültigen Markerpositionen geschrieben.

---

### 2.4 `MarkerClearCommand.java`

#### Save-Failure mit Rollback

`/knpc marker clear` prüft `saveStateSafely()`.

Bei Save-Failure:

```text
Runtime-Markerzustand wird wiederhergestellt
aktive Marker-Map wird zurückgesetzt
keine Erfolgsmeldung
klare Fehlermeldung
```

Dadurch bleibt Runtime/state.json konsistenter als beim alten Verhalten.

---

#### Referenzschutz bleibt erhalten

Marker werden nicht gelöscht, wenn persistierte NPCs noch darauf referenzieren.

Dadurch wird verhindert:

```text
Marker wird gelöscht
NPC behält tote Assignment-Referenz
Routine/Restore/Respawn wird kaputt
```

---

### 2.5 `JsonFileStateStore.java`

Diese Datei wurde im Kontext `state.json` Load-/Save-/Dirty-Safety geprüft und punktuell gehärtet.

#### Unbekannte aktive Marker-Typen markieren Partial-Load

Neu:

```text
unknown activeMarkerIds key
-> loggen
-> partialLoad=true
-> Auto-Overwrite blockieren
```

Vorher konnten unbekannte aktive Marker-Typen still in den typisierten State fallen oder bereinigt werden.

Jetzt verhindert Partial-Load, dass problematische alte Daten automatisch aus `state.json` verschwinden.

---

#### Legacy-Marker-Felder bleiben kompatibel

Weiterhin kompatibel:

```text
bedMarkerId
doorMarkerId
chestMarkerId
foodMarkerId
workMarkerId
chillMarkerId
```

Nicht gemacht:

```text
alte Marker-Felder löschen
state.json hart migrieren
Marker-v2 erzwingen
unbekannte Marker still verwerfen
```

---

#### Persistenzfilter bestätigt

Nicht gespeichert werden:

```text
entityRef
Runtime-Navigation
Runtime-Door-State
autoritative WALKING_*/PAUSED_* Runtime-State-Wahrheit
```

Runtime-Zustände werden nicht als dauerhafte Wahrheit in `state.json` behandelt.

---

### 2.6 `docs/safety/npc_restart_relink_control.md`

Die Safety-Dokumentation wurde final aktualisiert.

Ergänzt wurde:

```text
Read-only-Kontexte dürfen MarkerAssignments nicht löschen
Read-only-Kontexte dürfen MarkerAssignments nicht ersetzen
Reconcile darf in read-only Kontexten kein stateDirty setzen
Reconcile darf in read-only Kontexten kein saveStateSafely() auslösen
kaputte/inkonsistente MarkerAssignments dürfen nicht still bereinigt und zurückgeschrieben werden
mutierendes Reconcile nur in Admin-Repair/Cleanup/Spawn/Admin-Kontext
```

Damit ist die neue Safety-Regel dauerhaft dokumentiert.

---

### 2.7 `docs/safety/json_hierarchy.md`

Die JSON-Hierarchie-Safety wurde gespiegelt.

Ergänzt wurde:

```text
No-Go gegen stilles Reconcile-Overwrite von markerAssignments/state.json
Pflicht-Checklistenpunkt für Reconcile-/MarkerAssignments-Safety
```

Damit ist dokumentiert:

```text
JSON-Safety schützt alte Marker-Daten
Reconcile darf keine stille Migration/Bereinigung erzeugen
Marker-v2 bleibt späteres Feature
```

---

## 3. Logische Wirkung des gesamten Patches

### Fall 1: Marker fehlt beim Restart

Ablauf jetzt:

```text
state.json lädt alten Marker-State
Restore prüft Marker read-only
Marker fehlt
kein Ersatzmarker wird gewählt
kein Markerfeld wird überschrieben
kein Save wegen Fallback
Diagnose/Blockade statt stiller Reparatur
```

Bewertung: korrekt.

---

### Fall 2: Tick erkennt fehlenden Marker

Ablauf jetzt:

```text
Tick/Routine braucht Marker
read-only Resolve findet keinen gültigen Marker
Problem wird gemeldet/blockiert
kein Fallback-Marker
kein stateDirty
kein saveStateSafely()
```

Bewertung: korrekt.

---

### Fall 3: Diagnose prüft MarkerAssignments

Ablauf jetzt:

```text
Diagnose liest MarkerAssignments
Inkonsistenz erkannt
Warnung/Diagnose möglich
keine automatische Bereinigung
keine Persistenzänderung
```

Bewertung: korrekt.

---

### Fall 4: Validation prüft Required-Marker

Ablauf jetzt:

```text
Validation nutzt read-only Resolve
fehlender Marker -> Validation schlägt sicher fehl oder meldet Problem
kein automatischer Ersatz
kein Save
```

Bewertung: korrekt.

---

### Fall 5: MarkerSet mit Save-Failure

Ablauf jetzt:

```text
/knpc marker set
Marker wird runtime-seitig geändert
saveStateSafely() schlägt fehl
User bekommt klare Fehlermeldung
kein falscher Erfolg
Runtime/state.json-Drift wird ehrlich gemeldet
```

Bewertung: akzeptiert im Step-Scope.

Hinweis:

Ein vollständiger transaktionaler Rollback für `marker set` bleibt optionaler späterer Cleanup.

---

### Fall 6: MarkerClear mit Save-Failure

Ablauf jetzt:

```text
/knpc marker clear
Runtime wird geändert
saveStateSafely() schlägt fehl
Runtime wird restored
User bekommt Fehlermeldung
kein falscher Erfolg
```

Bewertung: korrekt.

---

### Fall 7: `activeMarkerIds` enthält unbekannten Typ

Ablauf jetzt:

```text
state.json enthält unbekannten activeMarkerIds-Key
Load erkennt unbekannten Typ
partialLoad=true
Auto-Save blockiert
alte Daten werden nicht still überschrieben
```

Bewertung: korrekt.

---

### Fall 8: Reconcile im Restart-/Respawn-Umfeld

Ablauf jetzt:

```text
Load/Restore/respawn-naher generic Spawnpfad
kein mutierendes Reconcile
keine MarkerAssignments-Löschung
keine automatische state.json-Bereinigung
```

Bewertung: korrekt.

---

### Fall 9: Expliziter Spawn/Admin-Kontext

Ablauf jetzt:

```text
Admin/Spawn weist Marker bewusst zu
mutierende Marker-API darf verwendet werden
Reconcile darf nur in diesem bewussten Kontext wirken
```

Bewertung: korrekt.

---

## 4. Bewusst nicht geändert

Dieser Patch hat nicht angefasst:

```text
Marker-v2 als neue Hauptarchitektur
markerAssignments als vollständige neue Hauptstruktur
Door-System
Navigation-System
Animation-System
Hytale Engine Roles
JSON-Roles
Dedupe-Löschlogik
neue Auto-Respawn-Logik
Respawn-/Relink-Policy außerhalb Marker-Resolve/Reconcile-Safety
Worldgen / Settlement-Registrierung
Combat-System
Appearance-System
```

Das ist wichtig, weil der Patch ein enger Safety-Patch bleibt.

---

## 5. Safety-Abgleich

### Geprüfte Baselines

Relevant waren:

```text
AGENTS.md
docs/safety/json_hierarchy.md
docs/safety/npc_restart_relink_control.md
```

Wichtige eingehaltene Regeln:

```text
kein Marker-v2 nebenbei
keine neuen Features
kein Door-/Navigation-/Animation-Refactor
kein Role-Prefix-Fallback
kein setRoleName("KeystoneNPC_...")
kein blindes Relinken per gleicher Role
kein Auto-Respawn bei AMBIGUOUS
keine Dedupe-Löschung nur wegen gleicher Role
kein Runtime-Fallback als persistente Wahrheit
keine state.json-Überschreibung bei Load-Failure/Partial-Load
kein Save-Failure als Erfolg
keine Records löschen bei unsicherer Entity-Removal
```

---

## 6. Step-by-Step Ergebnis

### Step 1 — Marker-State Audit / Safety-Baseline

Ergebnis:

```text
PASS
```

Wirkung:

```text
Marker-Resolve-Methoden kartiert
mutierende/read-only/gefährliche Pfade erkannt
Command-Safety-Risiken dokumentiert
state.json-Risiken dokumentiert
```

---

### Step 2 — Marker-Resolve read-only vs. mutierend trennen

Ergebnis:

```text
PASS
```

Wirkung:

```text
Validation nutzt read-only
Compatibility-Fallback entschärft
mutierende API separat gehalten
```

---

### Step 3 — MarkerSet / MarkerClear Command-Safety

Ergebnis:

```text
PASS
```

Wirkung:

```text
saveStateSafely() Rückgabe geprüft
Save-Failure wird nicht als Erfolg gemeldet
MarkerClear rollbackt Runtime
MarkerSet meldet Drift ehrlich
```

---

### Step 4 — state.json Load / Save / Dirty Safety

Ergebnis:

```text
PASS
```

Wirkung:

```text
unbekannte activeMarkerIds erzeugen Partial-Load
Auto-Overwrite wird blockiert
Legacy-Marker-Felder bleiben kompatibel
Runtime-Felder bleiben nicht autoritativ persistiert
```

---

### Step 5 — Versteckte Fallback-Saves final entfernen/blockieren

Ergebnis:

```text
PASS nach Fix
```

Wirkung:

```text
mutierender Marker-Fallback entfernt
Ring-/Nearest-Fallback entfernt
Fallback-Symbole weg
kein automatischer Marker-Ersatz über Resolver/Runner-Kontext
```

---

### Step 6 — Reconcile-/Cleanup-Logik für MarkerAssignments prüfen

Ergebnis:

```text
PASS
```

Wirkung:

```text
kein mutierendes Reconcile in Load/Restore/Diagnose/Validation/Tick
kein Reconcile im restore-/respawn-nahen generic Spawnpfad
keine automatische MarkerAssignments-Löschung beim Restart
```

---

### Step 7 — Safety-Dateien final aktualisieren

Ergebnis:

```text
PASS
```

Wirkung:

```text
npc_restart_relink_control.md aktualisiert
json_hierarchy.md aktualisiert
Reconcile-No-Go dokumentiert
Marker-v2 bewusst als späteres Feature abgegrenzt
```

---

## 7. Resthinweise vor Commit

### Compile

Gemeldet wurde:

```text
mvn -q -DskipTests test-compile
erfolgreich
```

Hinweis:

In dieser Review-Umgebung wurde Maven nicht selbst ausgeführt, weil `mvn` hier nicht verfügbar war. Die lokale erfolgreiche Ausführung im echten Projekt zählt als Gate.

---

### Optionaler späterer Cleanup

Nicht blockierend:

```text
MarkerSetCommand könnte später volltransaktionalen Rollback bekommen
reconcileMarkerAssignmentsForSpawnOrAdmin(...) könnte später in explicitSpawn vs explicitRepair getrennt werden
MarkerRegistry.getNextAvailable(...) existiert noch als API, aber ohne aktiven Resolver-/Runner-Fallbackpfad
```

Diese Punkte sind Backlog, nicht Teil dieses Safety-Patches.

---

### Marker-v2 Folgepunkt

Marker-v2 bleibt späteres Feature.

Für Marker-v2 später besonders prüfen:

```text
keine read-only Pfade mutieren lassen
markerAssignments nicht automatisch bereinigen und speichern
fehlende Marker bedeuten Diagnose/Repair, nicht Auto-Ersatz
Reconcile nur in expliziten Admin-/Repair-Kontexten mutierend erlauben
alte Marker-Felder sauber migrieren, nicht hart brechen
```

---

## 8. Gesamtfazit

Der Patch ist logisch sauber abgeschlossen.

Er löst die zentralen Marker-State-Safety-Probleme:

```text
gefährlicher mutierender Fallback
-> entfernt

Restore/Tick/Diagnose/Validation mutieren Marker
-> blockiert/read-only

Save-Failure bei Marker-Commands
-> sichtbar, kein falscher Erfolg

state.json unbekannte aktive Marker
-> Partial-Load statt stille Bereinigung

Reconcile löscht/ersetzt MarkerAssignments im falschen Kontext
-> blockiert

Safety-Dokumentation
-> aktualisiert
```

---

## 9. Ergebnis

```text
FINAL PASS
Commitfähig: ja
```
