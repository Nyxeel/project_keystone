# Variablen-Übersicht: Hytale-Rolle vs. Keystone-Mod-Rolle

Diese Datei trennt klar zwischen **Hytale Engine** (Server/NPC/Roles, Engine-Bewegung, Engine-Entity)
und **Keystone Mod** (eigene NPC-Identität, eigene Profile, eigene Persistenz).

Prefix-Konvention:
- `hy...`  → gehört zu Hytale / Engine
- `key...` → gehört zu Keystone / Mod
- ohne Prefix → neutrale JSON-Felder, die kontextklar im Keystone-Ordner liegen

Legende:
- **Aktuell** = wie der Wert heute im Code oder JSON heißt
- **Empfohlen** = wie er nach der Umbenennung heißen soll
- **Bedeutung** = wofür er da ist

---

## 1. Hytale-Seite (Engine / `Server/NPC/Roles`)

### 1.1 Engine-Identität

| Aktuell | Empfohlen | Bedeutung |
|---|---|---|
| `hytaleRole` (JSON) | `hyRoleId` | Echte Hytale Engine-Role-Datei unter `Server/NPC/Roles/`, z. B. `Lumberjack`. |
| `entityUuid` (Java) | `hyEntityUuid` | Persistierte UUID der echten Hytale-Entity. **Nicht** deine NPC-Instanz-ID. |
| `entityRef` (Java) | `hyEntityRef` / `runtimeHyEntityRef` | Live-Handle zur Hytale-Entity im RAM. **Nie speichern**, nur Runtime. |
| `appearance.value` / `Modify.Appearance` (JSON) | `hyAppearanceId` | Engine-Aussehen, z. B. `Temple_Mithril_Guard`. **Nicht** deine Auswahl-ID. |
| `nameTranslationKey` / `NameTranslationKey` (JSON) | `hyNameTranslationKey` | Übersetzungs-Key, wenn er direkt an Hytale Role / Engine geht. |

### 1.2 Engine-Role-Struktur (nicht umbenennen — Hytale-Pflichtfelder)

| Feld (JSON) | Bedeutung |
|---|---|
| `Type` | Hytale Role-/Template-Typ, z. B. `Variant`. |
| `Reference` | Hytale Role erbt/referenziert ein Template, z. B. `Template_Human_Friendly`. |
| `Modify` | Hytale Role überschreibt Werte aus dem referenzierten Template. |
| `MaxHealth` | Hytale-seitiger Lebenswert der Engine-Role. Nicht mit Keystone-Stats vermischen. |

### 1.3 Engine-Bewegung & Sensoren

| Aktuell (JSON) | Bedeutung |
|---|---|
| `motionControllerList` / `MotionControllerList` | Engine-Bewegungscontroller, z. B. `Walk`. |
| `instructions` / `Instructions` | Engine-AI-Instructions mit Sensor und BodyMotion. **Keine** Keystone-Routine. |
| `sensor` / `Sensor` | Engine-Sensor, z. B. `Leash`, `Any`. |
| `bodyMotion` / `BodyMotion` | Engine-Bewegungslogik, z. B. `Seek`, `Nothing`. |
| `usePathfinder` / `UsePathfinder` | Hytale-Pathfinder verwenden? |
| `useSteering` / `UseSteering` | Engine-Steering verwenden? |
| `reachable` / `Reachable` | Engine-BodyMotion-Parameter. |
| `relativeSpeed` / `RelativeSpeed` | Bewegungsgeschwindigkeit relativ zum Controller. |
| `stopDistance` / `StopDistance` | Abstand, bei dem Engine-Bewegung stoppt. |
| `slowDownDistance` / `SlowDownDistance` | Abstand, ab dem Engine-Bewegung langsamer wird. |
| `switchToSteeringDistance` / `SwitchToSteeringDistance` | Engine-naher Bewegungsparameter. |
| `range` / `Range` | Sensor-/Combat-/Aggro-Reichweite (je nach Datei). |

---

## 2. Keystone-Seite (Mod / `state.json` / eigene Profile)

### 2.1 NPC-Identität

| Aktuell | Empfohlen | Bedeutung |
|---|---|---|
| `npcId` (Java) | `keyNpcId` | **Wichtigste Keystone-ID.** Eindeutige NPC-Instanz. |
| `npcName` (Java) | `keyNpcName` | Anzeigen-/Admin-Name. **Keine** Identität. |
| `roleId` (Java) / `role` (JSON) | `keyRoleId` | Keystone-Rolle, z. B. `lumberjack`. Steuert Profile, Marker, Routine, Spawn. |
| `worldKey` (Java) | `keyWorldKey` | Stabiler Welt-Schlüssel für `state.json` pro Welt. |
| `worldId` (Java, MarkerRecord) | `keyWorldKey` | Welt-Zuordnung eines Markers. Soll mit `worldKey` vereinheitlicht werden. |

### 2.2 NPC-Lifecycle & Zustand

| Aktuell | Empfohlen | Bedeutung |
|---|---|---|
| `entityStatus` (Java) | `keyEntityStatus` | Technischer Lifecycle: `REGISTERED`, `NEEDS_RELINK`, `MISSING_ENTITY`, `DISABLED`. |
| `state` (Java / JSON) | `keyNpcState` | Fachlicher NPC-Zustand: `IDLE`, `WORKING`, `SLEEPING`. |
| `currentPosition` (Java) | `keyCurrentPosition` | Letzte bekannte persistierbare Position. **Kein** `EntityRef`. |

### 2.3 Marker & Positionen

| Aktuell | Empfohlen | Bedeutung |
|---|---|---|
| `position` (Java, MarkerRecord) | `keyMarkerPosition` | Position eines konkreten Markers. |
| `x` / `y` / `z` (Java) | `x` / `y` / `z` | Koordinaten — Kontext über `Position`-Klasse klar. |
| `markerAssignments` (Java) | `keyMarkerAssignments` | Konkrete Zuweisungen, z. B. `bed -> marker_house_007_bed_main`. |
| `markerId` (Java) | `keyMarkerId` | Konkrete Marker-Instanz-ID. |
| `markerName` (Java) | `keyMarkerName` | Logischer Markername, z. B. `bed`, `work`, `chest`. |
| `markerType` (Java) | `keyMarkerType` | Typ eines Markers, z. B. `BED`, `WORK`, `CHEST`. |
| `requiredMarkers` (JSON) | `keyRequiredMarkers` | Liste logischer Marker, die eine Rolle braucht. |
| `markerRoles` (JSON) | `keyMarkerRoles` | Mapping logischer Markername → `MarkerType`. |

### 2.4 Struktur-Bindung & Spawn-Auswahl

| Aktuell | Empfohlen | Bedeutung |
|---|---|---|
| `structureInstanceId` (Java) | `keyStructureInstanceId` | Konkrete platzierte Struktur, z. B. `house_007`. |
| `slotId` (Java) | `keySlotId` | Slot innerhalb einer Struktur, z. B. `main_worker`, `spouse`. |
| `selectedAppearanceId` (Java) | `keySelectedAppearanceId` | Gespeicherte Auswahl der Appearance-Variante. **Nicht** = `hyAppearanceId`. |
| `selectedCompositionId` (Java) | `keySelectedCompositionId` | Gewählte Spawn-/Haus-Composition, z. B. `composition_full_family`. |
| `selectedPrefabId` (Java) | `keySelectedPrefabId` | Gewähltes Prefab, z. B. `lumberjack_house_family`. |

### 2.5 Keystone JSON-Datei-Header (kontextklar, ohne Prefix ok)

| Feld (JSON) | Bedeutung |
|---|---|
| `id` | Allgemeine Datei-/Profil-ID. Für Rollen lieber `keyRoleId`. |
| `version` | Version der Keystone-JSON-Struktur. |
| `displayName` | Menschlich lesbarer Name für Debug/UI/Admin. |
| `shared` | Gemeinsame Profile einer `npc_group`. |
| `variants` | Varianten innerhalb einer `npc_group`. |

### 2.6 Keystone-Profile (Verweise, ohne Prefix ok)

| Feld (JSON) | Bedeutung |
|---|---|
| `skills` | Profil für Fähigkeiten, z. B. `human_worker`. |
| `movement` | Bewegungsprofil (Inhalt teilweise Engine-nah). |
| `navigation` | Navigations-Profil, z. B. `friendly_worker`. |
| `combat` | Combat-Profil. |
| `spawn` | Spawn-Service / Spawn-Profil. |
| `persistence` | Persistence-Profil, z. B. `persistent_citizen`. |
| `routine` | Verweis auf Routine-Datei. |
| `actions` | Verweis auf Action-Datei oder Action-Map. |

### 2.7 Routine & Schedule

| Feld (JSON) | Empfohlen | Bedeutung |
|---|---|---|
| `schedule` | — | Liste der Routine-Schritte. |
| `time` | — | Zeitpunkt innerhalb einer Routine. |
| `targetMarker` | `keyTargetMarkerName` | Logischer Marker, zu dem ein Schritt gehen soll. |
| `action` | — | Welche Action ausgeführt wird, z. B. `chop_wood`. |
| `durationMinutes` | — | Dauer eines Routine-Schritts. |
| `animation` | optional `hyAnimationId` | Animation, die die Action verwendet. |
| `sound` | optional `hySoundId` | Sound, den die Action verwendet. |
| `requiresCapability` | `requiresSkill` / `requiredSkillId` | Fähigkeit für eine Action (Projektrichtung: `skills` statt `capabilities`). |

### 2.8 Persistence-Profil

| Feld (JSON) | Bedeutung |
|---|---|
| `persistent` | NPC wird langfristig gespeichert. |
| `savePosition` | Position speichern. |
| `saveState` | Fachlichen Zustand speichern. |
| `saveHome` | Home-/Strukturbindung speichern. |
| `saveRoutineProgress` | Routine-Fortschritt speichern (später). |
| `respawnAfterRestart` | Respawn nach Restart erlaubt. |
| `despawnWhenFarAway` | Despawn-Verhalten bei Distanz. |

### 2.9 Runtime / StateStore (Java-intern)

| Aktuell | Empfohlen | Bedeutung |
|---|---|---|
| `dirty` | `dirty` (ok) | StateStore hat ungespeicherte Änderungen. |
| `worldStates` | `keyWorldStates` / `persistedWorldStates` | Geladene `PersistedWorldState`-Objekte. |
| `npcRecordsById` / `recordsById` | `keyNpcRecordsById` | Map `keyNpcId` → `NpcRecord`. |
| `runtimeById` | `runtimeNpcsByKeyNpcId` | Map `keyNpcId` → `RuntimeNpc`. |

---

## 3. Bewertung bestehender Namen

### Nicht ideal / verwirrend (umbenennen empfohlen)
- `roleId` — unklar ob Hytale oder Keystone
- `hytaleRole` — Stilbruch, besser `hyRoleId`
- `entityUuid` / `entityRef` — fehlender `hy`-Prefix
- `worldId` vs. `worldKey` — Doppelbegriff vereinheitlichen
- `selectedAppearanceId` vs. `appearance.value` — Auswahl vs. Engine-Wert mischen sich
- `id` / `state` — zu generisch

### Gut oder okay (so lassen)
- `npcId`, `markerAssignments`, `structureInstanceId`, `slotId`
- `selectedCompositionId`, `selectedPrefabId`
- `requiredMarkers`, `markerRoles`

---

## 4. Faustregeln

1. **Engine-Wert oder Engine-Handle?** → `hy...`
2. **Eigene NPC-Identität / eigene Profile / state.json?** → `key...`
3. **Hytale-Pflichtfeld in `Server/NPC/Roles/`?** → **niemals umbenennen** (`Type`, `Reference`, `Modify`, `MaxHealth`, …).
4. **Im Zweifel** zwischen Auswahl-ID (Keystone) und Engine-Wert (Hytale) **immer trennen**:
   `keySelectedAppearanceId` ≠ `hyAppearanceId`.

---


## 5. Probleme mit Variablennamen

### A. Harte Konflikte (Code ≠ Doku)

#### A1. `roleId` (Java) ↔ `role` (JSON) ↔ `hytaleRole` (JSON)

- **Keystone-JSON**: `"role": "lumberjack"` und `"hytaleRole": "Lumberjack"` (`lumberjack_group.json:16`)
- **Java**: `NpcIdentity.roleId` (`NpcIdentity.java:19`)
- `NpcSpawn.spawnNpc(String roleId, …)` (`NpcSpawn.java:47`) — Name lässt offen, ob Keystone- oder Hytale-Role
- `NpcDefinition.isSpawnable(String roleId)` (`NpcDefinition.java:49`) — gleiche Mehrdeutigkeit

`hytaleRole` existiert **nur im JSON**, **nicht** als Feld auf `NpcRecord`. Der Spawn-Pfad hat damit derzeit keinen typsicheren Zugriff auf die Hytale-Role — sie wird vermutlich indirekt über `NpcDefinition` aufgelöst. Architektur-Lücke relativ zur Doku-Forderung „trennen".

#### A2. `appearance` hat drei verschiedene Bedeutungen

| Ort | Bedeutung |
|---|---|
| `Lumberjack.json:5` `"Appearance": "Temple_Mithril_Guard"` | Hytale Engine-Wert (`hyAppearanceId` laut Doku) |
| `lumberjack_group.json:20` `"appearance": { "profile": "…" }` | Keystone-Profilverweis |
| `NpcAppearanceSelection.java:28` `selectedAppearanceId` | Keystone-Auswahl (`keySelectedAppearanceId`) |

Genau das warnt die Doku — und genau das ist im Code so. Reale Verwechslungsgefahr.

#### A3. `nameTranslationKey` mit zwei Namespaces unter demselben Feldnamen

- **Hytale-Role-JSON**: `"NameTranslationKey": "server.npcRoles.Lumberjack.name"` (`Lumberjack.json:7`)
- **Keystone-Group-JSON**: `"nameTranslationKey": "keystone.npc.roles.lumberjack.name"` (`lumberjack_group.json:19`)

Beides heißt fast gleich (`NameTranslationKey` vs. `nameTranslationKey`), zeigt aber auf unterschiedliche Translation-Trees. Doku-Empfehlung `hyNameTranslationKey` für die Hytale-Seite ist im Code/JSON nicht umgesetzt.

#### A4. `requiresCapability` widerspricht der Doku-Empfehlung `requiresSkill`

Alle Action-JSONs nutzen noch `requiresCapability`:

- `test_actions.json`
- `lumberjack_actions.json`

Die Doku sagt „Projektrichtung: `skills` statt `capabilities`" — JSON ist aber noch nicht migriert. Stilbruch zur eigenen Architektur (es gibt schon ein `skills`-Profil-Feld).

---

### B. Doku-interne Widersprüche

#### B1. `nameTranslationKey` — Doku-Aussage selbst zweideutig

Tabelle 1.1 listet `nameTranslationKey` als Hytale-Wert mit Empfehlung `hyNameTranslationKey`, aber das Keystone-JSON nutzt denselben Namen für einen Keystone-eigenen Translation-Key. Es fehlt ein eigener Eintrag auf der Keystone-Seite (z. B. `keyNameTranslationKey`). Sonst ist nicht erklärt, wie das Keystone-Feld heißen soll.

#### B2. `requiresCapability` vs. `skills`-Profil

Die Doku fordert Migration zu `requiresSkill`, gleichzeitig listet sie `skills` (2.6) als bestehendes Profil. Es fehlt die explizite Regel: „Action darf nicht `capability` heißen, wenn der Bezugsraum `skills` ist."

---

### C. Was die Doku noch nicht abdeckt (relevante Lücken)

1. `hytaleRole` hat keinen Platz auf `NpcRecord`. Die Doku sollte klarstellen: wird `hyRoleId` persistiert oder nur über `NpcDefinition`/`roleId` aufgelöst?
2. `NpcSpawn.spawnNpc(String roleId, …)` — die Doku müsste festlegen: „Spawn-API nimmt Keystone-`keyRoleId`, niemals Hytale-`hyRoleId`."
3. `MarkerRecord.worldId` ist ein Record-Component — Umbenennen zu `worldKey` ist Breaking Change. Doku sollte das markieren (Persistenz-Migration nötig).
4. `setEntityRef(Object, boolean liveEntityValid)` (`RuntimeNpc.java:58`) — der Caller bestimmt, ob die `EntityRef` gültig ist. Das widerspricht der „Live-Entity-Grundregel" aus `AGENTS.md`: die Validitätsprüfung gehört nicht in den Caller, sondern in `RuntimeNpc` selbst (per Hytale-API-Check).

---

### D. Empfohlene minimale Korrekturen an der Doku (ohne Code-Änderungen)

1. Neue Zeile in 2.1: `keyNameTranslationKey` (Keystone-eigener Translation-Key, getrennt von `hyNameTranslationKey`).
2. Neue Zeile in 2.4: `keyHyRoleId` ist Pflichtfeld auf `NpcRecord` (heute nur in JSON `hytaleRole` — Lücke).
3. Regel in §4 ergänzen: „Spawn-/Definition-APIs nehmen ausschließlich `keyRoleId`. Engine-Role wird intern über `NpcDefinition` → `hyRoleId` aufgelöst."
4. Regel in §4 ergänzen: „`requiresCapability` ist deprecated; neue Actions schreiben `requiresSkill`."