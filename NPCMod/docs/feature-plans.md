# Nicht implementierte / nur teilweise implementierte Systeme

**Projekt:** NPCMod / KeystoneNPC  
**Datum:** 2026-05-13  
**Thema:** Abgleich der zentralen NPC-JSON-Idee mit dem aktuellen validierten Stand

---

## Kurzfazit

Die zentrale NPC-JSON ist ein gutes **Zielbild**.

Aktuell fertig oder weitgehend validiert sind vor allem:

- Loader-Grundstruktur
- `npc_group` mit `shared` + `variants`
- Trennung von Hytale Engine-Role, Keystone-Role und `npcId`
- Skills-Pfad statt neuer Capabilities-Struktur
- strikte `requiredMarkers` / `markerRoles`-Validierung
- Basis-Routine- und Marker-Anbindung
- teilweise Persistence-Safety, besonders `respawnAfterRestart`

Noch nicht fertig sind vor allem die echten Runtime-Systeme hinter vielen JSON-Feldern.

---

# 1. Zentrale Concrete-JSON

## Noch nicht final implementiert

Die Idee einer einzelnen zentralen Datei wie:

```text
lumberjack_friendly.json
 type: concrete
 template: templates/template_lumberjack.json
 profiles: {...}
```

ist noch nicht der aktuelle Hauptweg.

## Aktueller validierter Weg

Aktuell ist eher dieses Modell validiert:

```text
npc_group
 shared
 variants
```

Das bedeutet:

- Eine Gruppe beschreibt gemeinsame Werte.
- Varianten beschreiben konkrete NPC-Typen.
- Jede Variante wird intern zu einer `NpcDefinition`.

## Offene Baustelle

Später muss entschieden werden:

- Bleibt `npc_group` der Hauptweg?
- Kommt `type: concrete` zusätzlich zurück?
- Oder wird `concrete` nur als Legacy-/Sonderformat behalten?

---

# 2. EffectiveNpcDefinition

## Nur teilweise implementiert

Die Idee ist:

```text
Template
+ zentrale NPC-JSON
+ Profile
= fertige EffectiveNpcDefinition
```

Das ist noch nicht vollständig umgesetzt.

## Bereits vorhanden

- `npc_group` kann gemeinsame Daten mit Varianten mergen.
- Definitionen werden geladen und validiert.
- Einige Profilpfade werden gelesen oder geprüft.

## Noch offen

- Alle Profile vollständig laden
- Profile logisch zusammenführen
- fertige Runtime-Definition bauen
- Runtime-Code nur noch mit fertigen Java-Objekten arbeiten lassen
- nicht ständig JSON erneut lesen

---

# 3. Appearance / Aussehen

## Noch nicht aktiv implementiert

Diese Felder sind noch kein echtes Runtime-System:

```json
"appearance": {
  "profile": "appearances/lumberjack_default.json",
  "value": "Temple_Mithril_Guard"
}
```

## Aktueller Stand

Das sichtbare NPC-Modell kommt aktuell hauptsächlich aus der echten Hytale Engine-Role:

```text
Server/NPC/Roles/<RoleName>.json
```

## Offene Baustelle

Später braucht es einen eigenen Feature-Step:

```text
Appearance-Apply-System
```

Dabei muss klar bleiben:

```text
Keystone appearance != automatisch sichtbares Engine-Model
```

---

# 4. Stats

## Noch nicht als eigenes Keystone-System fertig

Beispiel:

```json
"stats": {
  "maxHealth": 100
}
```

## Aktueller Stand

Stats sind eher vorbereitet oder Engine-Role-nah.

## Noch offen

- `maxHealth` aus Keystone-Definition wirklich anwenden
- Health sauber mit Hytale-Entity verbinden
- Restart-/Respawn-Verhalten beachten
- keine Werte doppelt aus Role-Datei und Keystone-Datei widersprüchlich setzen

---

# 5. Drops

## Noch nicht aktiv implementiert

Beispiel:

```json
"drops": {
  "dropList": "Empty",
  "profile": "drops/empty.json"
}
```

## Noch offen

- Drop-Profil laden
- Tod des NPC erkennen
- Drop-Regeln anwenden
- Friendly-NPCs gegen Farming schützen
- Hostile-NPC-Drops später getrennt planen

---

# 6. Attitude

## Noch nicht als eigenes Verhalten-System aktiv

Beispiel:

```json
"attitude": {
  "defaultPlayerAttitude": "Neutral",
  "defaultNPCAttitude": "Ignore"
}
```

## Noch offen

- Verhalten gegenüber Spielern steuern
- Verhalten gegenüber NPCs steuern
- Unterschied zwischen `faction` und `attitude` sauber nutzen
- Combat-/Dialog-/Trade-System später daran anbinden

---

# 7. npcType

## Noch nicht aktiv als Runtime-Logik

Beispiel:

```json
"npcType": "human"
```

## Noch offen

- Mensch, Untoter, Tier, Golem usw. unterschiedlich behandeln
- Basiswerte je NPC-Typ
- erlaubte Skills je Typ
- Animationen/Movement je Typ
- spätere Spawn-Regeln je Typ

---

# 8. Faction

## Noch nicht vollständig aktiv

Beispiel:

```json
"faction": "friendly"
```

## Wichtig

`faction` bedeutet nur grob:

```text
friendly / neutral / hostile
```

Es darf nicht automatisch Death-, Respawn- oder Persistence-Logik bestimmen.

## Noch offen

- Friendly/Hostile-Verhalten sauber anbinden
- Aggro-Regeln
- Zielauswahl
- Schutzregeln für Stadt-NPCs
- keine Vermischung mit `deathPolicy`

---

# 9. Combat-Profil

## Noch nicht vollständig implementiert

Beispiel:

```json
"combat": "combat/peaceful.json"
```

## Noch offen

- Combat-Profil laden
- Aggro-Reichweite
- Angriffsschaden
- Angriffscooldown
- Target-Regeln
- Friendly-NPCs ohne Kampf
- Hostile-NPCs mit Kampf
- Guards später separat planen

---

# 10. Spawn-Profil

## Noch nicht vollständig aktiv

Beispiel:

```json
"spawn": "spawns/forest_village.json"
```

## Aktueller Stand

Spawn ist noch nicht als komplettes Biome-/Worldgen-/Prefab-System fertig.

## Noch offen

- Spawn-Orte aus Profil lesen
- Biome-Regeln
- Struktur-Regeln
- Spawn-Limits
- Server-Konfig für Dichte
- keine Spieler-Kontrolle über NPC-Dichte
- später MVP-B / Worldgen

---

# 11. Structure / Prefab-Bindung

## Noch nicht aktiv implementiert

Beispiel:

```json
"structure": "structures/lumberjack_house.json"
```

## Noch offen

- Haus/Prefab als echte Instanz erkennen
- Marker aus Prefab lesen
- NPC an konkrete Strukturinstanz binden
- wiederholte gleiche Häuser eindeutig unterscheiden
- Strukturinstanz-ID speichern
- NPC-Spawns aus Struktur planen

---

# 12. Persistence-Profil

## Teilweise implementiert

Beispiel:

```json
"persistence": "persistence/persistent_citizen.json"
```

## Bereits aktiv

- `respawnAfterRestart` wird als wichtiges Gate verwendet.
- State-Safety ist validiert.
- Runtime-Daten dürfen nicht in `state.json`.
- Load-/Save-Failure darf state.json nicht kaputt überschreiben.

## Noch offen

- vollständige per-NPC Persistence-Regeln
- `savePosition`
- `saveState`
- `saveHome`
- `saveRoutineProgress`
- `despawnWhenFarAway`
- Death-/Respawn-Policy sauber anbinden

---

# 13. Actions / Animation / Sound

## Noch nicht vollständig an Runtime angebunden

Beispiel:

```json
"actions": "actions/lumberjack_actions.json"
```

## Noch offen

- Action-Profil vollständig laden
- Animation starten
- Sound starten
- Loop-Logik
- Sound-Intervall
- Action stoppen, wenn NPC Ziel verlässt
- Skill-Gates für Actions prüfen

Beispiel:

```text
Routine sagt: work + chop_wood
Action sagt: Animation + Sound + USE_TOOLS nötig
```

---

# 14. Movement-Profil

## Nur teilweise aktiv

Beispiel:

```json
"movement": "movement/human_walk.json"
```

## Noch offen

- Bewegungsprofil komplett anwenden
- Walk-/Run-/Swim-Animationen
- Footstep-Sounds
- Speed-Tuning
- Bewegung je NPC-Typ
- klare Trennung zwischen Engine-Motion und Keystone-Routine

---

# 15. Navigation-Profil

## Nur teilweise aktiv

Beispiel:

```json
"navigation": "navigation/friendly_worker.json"
```

## Noch offen

- Navigation-Policy vollständig nutzen
- Door-aware Routing sauber anbinden
- Pathfinding-Regeln über Profil steuern
- kein eigenes Fake-Pathfinding bauen
- Route nur mit gültiger EntityRef starten

---

# 16. MotionControllerList und Instructions

## Noch nicht als sauberes ausgelagertes System fertig

Beispiel:

```json
"motionControllerList": [...]
"instructions": [...]
```

## Noch offen

- entscheiden: bleiben diese Werte in zentraler JSON?
- oder wandern sie vollständig nach `movement/`?
- Hytale-Engine-Kompatibilität prüfen
- keine Werte doppelt in Template, Role und Keystone-JSON führen

---

# 17. Debug-System

## Nur teilweise aktiv

Beispiel:

```json
"debug": {
  "showMarkers": false,
  "logRoutineChanges": true,
  "logCapabilityChecks": false,
  "logMotionChanges": true
}
```

## Noch offen

- Debug pro NPC/Definition steuern
- Marker-Anzeige sauber anbinden
- Motion-Logs gezielt aktivieren
- kein Tick-Spam
- Debug darf nichts mutieren

---

# 18. Marker-v2

## Noch nicht implementiert

Marker-v2 ist eine eigene große Baustelle.

## Noch offen

- stabile `markerAssignments` pro NPC
- read-only Resolve darf nichts verändern
- mutierende Zuweisung nur in Spawn/Admin/Repair
- keine automatische Marker-Reparatur beim Restart
- keine falschen Marker-Saves
- Markerbesitz sauber prüfen

## Wichtig

Marker-v2 darf nicht nebenbei in einem anderen Patch halb eingebaut werden.

---

# 19. Biome-NPCs / Worldgen / Settlement

## Noch nicht implementiert

## Noch offen

- Chunks/Strukturen erkennen
- geplante NPC-Spawns registrieren
- Strukturinstanz-ID erzeugen oder lesen
- NPC in `state.json` registrieren
- erst danach Entity spawnen
- keine unbekannten Live-Entities blind adoptieren

## Wichtig

Aktuell ist es richtig, unbekannte NPCs nicht automatisch zu übernehmen.

---

# 20. Commands in Bezug auf neue Systeme

## Teilweise vorhanden, aber noch nicht vollständig passend zu allen Zielsystemen

Commands wie:

```text
/knpc spawn
/knpc marker set
/knpc marker clear
/knpc remove
/knpc clear
/knpc respawn-missing
/knpc status
```

müssen später mit den neuen Systemen sauber verbunden werden.

## Noch offen

- Commands für Marker-v2 härten
- Remove/Clear darf keine unsicheren Entities verlieren
- Respawn-Command darf Chunk-/Relink-Safety nicht umgehen
- Dry-run darf niemals State verändern
- destruktive Aktionen brauchen `--force`

---

# Priorität: Was zuerst angehen?

## P0 — Sicherheitsbasis behalten

Nicht zuerst neue Features bauen, wenn diese Regeln unsicher sind:

1. Kein Tick ohne gültige EntityRef
2. Kein Auto-Respawn ohne Chunk-Gate
3. Kein dynamisches `KeystoneNPC_...` RoleName
4. Kein Save bei Load-Failure/Partial-Load
5. Kein read-only Marker-Resolve mit Mutation
6. Keine Commands, die Records löschen, obwohl Entity-Zustand unsicher ist

## P1 — Marker-v2 vorbereiten

Vor Marker-v2 müssen alte Marker-Fallbacks entfernt oder entschärft sein.

## P2 — Runtime-Systeme einzeln bauen

Danach einzeln:

1. Appearance-Apply
2. Stats
3. Actions/Animation/Sound
4. Movement/Navigation-Profil
5. Combat
6. Spawn/Profile
7. Structure/Prefab-Bindung
8. vollständige Persistence-Profile

---

# Endfazit

Dein Dokument beschreibt eine gute Zielarchitektur.

Aktuell ist aber noch nicht alles echte Funktion.

Viele Felder sind im Moment eher:

```text
geplant
vorbereitet
teilweise validiert
noch nicht an Runtime angebunden
```

Die nächsten Schritte sollten nicht alles gleichzeitig bauen.

Sicherer Weg:

```text
Safety stabil halten
Marker-v2 sauber vorbereiten
Runtime-Systeme einzeln aktivieren
nach jedem Step Review + Compile
```
