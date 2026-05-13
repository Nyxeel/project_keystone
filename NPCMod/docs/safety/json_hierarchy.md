# JSON Hierarchy Control File

> **Status:** Validated Baseline
> **Projekt:** Hytale-Mod „NPCMod / KeystoneNPC“
> **Zweck:** Dauerhafte Kontroll- und Review-Datei für die neue `Server/NPC` JSON-, Role-, Skill- und Keystone-Hierarchie. Diese Datei schützt die validierte Architektur vor versehentlichen Regressionen durch spätere AI-/Agent-/Entwickler-Patches.

---

## Inhaltsverzeichnis

1. [Grundprinzipien](#1-grundprinzipien)
2. [Zielarchitektur](#2-zielarchitektur)
3. [Validierter Zustand](#3-validierter-zustand)
4. [Validierte Agent Steps / Feature-Phasen](#4-validierte-agent-steps--feature-phasen)
5. [Kritische No-Go-Regeln](#5-kritische-no-go-regeln)
6. [Standard Review-Prozess für spätere Patches](#6-standard-review-prozess-für-spätere-patches)
7. [Compile- und Test-Gates](#7-compile--und-test-gates)
8. [Bekannte Fehlerbilder und Diagnose](#8-bekannte-fehlerbilder-und-diagnose)
9. [Pflicht bei Änderungen an diesem Feature](#9-pflicht-bei-änderungen-an-diesem-feature)
10. [Aktueller validierter Abschlussstand](#10-aktueller-validierter-abschlussstand)
11. [Kurze Pflicht-Checkliste für jede spätere AI](#11-kurze-pflicht-checkliste-für-jede-spätere-ai)
12. [Empfohlene Commit-Regel](#12-empfohlene-commit-regel)
13. [AI-Ergänzung aus Gesprächskontext](#13-ai-ergänzung-aus-gesprächskontext)

---

## 1. Grundprinzipien

Die neue Keystone-NPC-Struktur trennt strikt zwischen **Hytale Engine-Roles**, **Keystone Mod-Konfiguration** und **konkreten NPC-Instanzen**.

> [!IMPORTANT]
> **Hytale Engine-Role ≠ Keystone roleId ≠ npcId.**
> Diese drei Begriffe dürfen nie vermischt werden.

| Begriff | Bedeutung | Beispiel | Persistenz / Ort |
|---|---|---|---|
| `hytaleRole` | Echte Hytale Engine-Role | `Lumberjack`, `Test` | `Server/NPC/Roles/<RoleName>.json` |
| Keystone `role` | Fachliche Mod-Rolle | `lumberjack`, `test` | NPC-Definition / `state.json` |
| `npcId` | Konkrete NPC-Instanz | UUID | `state.json` |
| `entityUuid` | Live-Entity-Verknüpfung | UUID der Hytale Entity | `state.json` |
| `requiredMarkers` | Logische Pflichtmarker | `bed`, `food`, `work` | NPC-Definition |
| `markerRoles` | MarkerName → MarkerType | `bed: BED` | NPC-Definition |

> [!CAUTION]
> Niemals wieder dynamische Engine-Roles wie `KeystoneNPC_<npcId>_<roleId>_Role` erzeugen oder per `NPCEntity.setRoleName(...)` setzen. Hytale erwartet dafür echte Role-Dateien unter `Server/NPC/Roles/`. Fehlen diese, können NPCs nach Restart/Reload verschwinden.

### Persistente Daten

Persistent sind konkrete Instanzdaten:

```text
state.json:
- npcId
- role
- entityUuid
- entityStatus
- markerAssignments
- worldId
- currentPosition
```

### Runtime-only Daten

Runtime-only sind Live-Entity-Referenzen und temporäre Entscheidungen:

```text
- EntityRef / Live Entity Object
- aktueller Tick-Status
- temporäre Navigation-Ziele
- temporäre Skill-Check-Entscheidungen
```

Diese dürfen nicht blind in Role-Dateien oder Definitionen zurückgeschrieben werden.

---

## 2. Zielarchitektur

### 2.1 Ordnerstruktur

```text
Server/NPC/
├── Roles/
│   ├── Template_Human_Friendly.json
│   ├── Lumberjack.json
│   ├── Lumberjack_Wife.json
│   ├── Lumberjack_Oldman.json
│   ├── Lumberjack_Oldwife.json
│   └── Test.json
│
└── Keystone/
    ├── README.md
    ├── npc/
    │   ├── index.json
    │   ├── lumberjack/
    │   │   ├── lumberjack_group.json
    │   │   ├── appearances/
    │   │   ├── routines/
    │   │   └── actions/
    │   └── test/
    │       ├── test_group.json
    │       ├── appearances/
    │       ├── routines/
    │       └── actions/
    ├── skills/
    ├── movement/
    ├── navigation/
    ├── combat/
    ├── spawns/
    └── persistence/
```

### 2.2 Zuständigkeiten

| Bereich | Aufgabe | Darf konkrete NPC-Instanzen speichern? | Darf Engine-Model bestimmen? |
|---|---|---:|---:|
| `Server/NPC/Roles/` | Echte Hytale Engine-Roles | Nein | Ja |
| `Server/NPC/Keystone/npc/index.json` | Aktive Keystone NPC-Gruppen auflisten | Nein | Nein |
| `*_group.json` | Gruppen + Varianten definieren | Nein | Nur über `hytaleRole`-Referenz |
| `skills/` | Skill-Profil, z. B. Türen öffnen | Nein | Nein |
| `movement/` | Bewegungsprofil / Navigation-Tuning | Nein | Nein |
| `navigation/` | Policy-Konfiguration für spätere Wegfindung | Nein | Nein |
| `combat/` | Kampfprofil, aktuell weitgehend vorbereitet | Nein | Nein |
| `spawns/` | Spawnprofil, aktuell weitgehend vorbereitet | Nein | Nein |
| `persistence/` | Persistenzprofil, aktuell weitgehend vorbereitet | Nein | Nein |
| `state.json` | Konkrete NPC-Instanzen | Ja | Nein |

### 2.3 Datenfluss

```text
Keystone/npc/index.json
        ↓
*_group.json
        ↓
shared + variants
        ↓
NpcDefinition pro Variante
        ↓
RoleDefinitionRegistry prüft hytaleRole
        ↓
Spawn nutzt echte Engine-Role aus Server/NPC/Roles/
        ↓
state.json speichert konkrete Instanzdaten
```

### 2.4 Group-Format

`npc_group` ist nur ein Container. Jede `variant` wird intern zu einer vollständigen `NpcDefinition`.

```json
{
  "type": "npc_group",
  "shared": {
    "skills": "skills/human_worker.json",
    "movement": "movement/human_walk.json",
    "navigation": "navigation/friendly_worker.json",
    "combat": "combat/peaceful.json",
    "spawn": "spawns/forest_village.json",
    "persistence": "persistence/persistent_citizen.json"
  },
  "variants": [
    {
      "id": "test",
      "role": "test",
      "hytaleRole": "Test",
      "displayName": "Test NPC"
    }
  ]
}
```

### 2.5 Aktive vs. vorbereitete JSON-Einträge

| JSON-Eintrag | Java-Status | Hinweis |
|---|---|---|
| `npc/index.json` | Aktiv | Lädt Keystone-Gruppen primär |
| `type: npc_group` | Aktiv | Wird als Gruppe geparst |
| `shared` | Aktiv | Wird in Varianten gemerged |
| `variants` | Aktiv | Jede Variante wird zu `NpcDefinition` |
| `id` | Aktiv | Definition-ID, Duplicate-Check |
| `role` | Aktiv | Fachliche Keystone-Rolle |
| `hytaleRole` | Aktiv | Echte Hytale Engine-Role, Pflicht in Gruppen |
| `requiredMarkers` | Aktiv | Pflicht und nicht leer |
| `markerRoles` | Aktiv | Muss exakt zu `requiredMarkers` passen |
| `routine` | Aktiv | Tagesroutine wird geladen |
| `skills` | Aktiv | Skill-Profilpfad, skills-first |
| `movement` | Teilweise aktiv | Einige Bewegungswerte werden genutzt/validiert |
| `navigation` | Teilweise aktiv | Geladen/validiert, Policy-Logik noch ausbaubar |
| `actions` | Teilweise aktiv | Action-IDs werden geprüft; Animation/Sound noch nicht vollständig verdrahtet |
| `debug` | Teilweise aktiv | Debug-Flags können Ausgaben steuern |
| `appearance` | Platzhalter / Mod-Konfig | Engine-Model kommt aktuell aus Role-Datei |
| `combat` | Platzhalter / vorbereitet | Kein vollständiges Combat-Runtime-System |
| `spawn` | Platzhalter / vorbereitet | Spawnprofil steuert Spawn noch nicht vollständig |
| `persistence` | Platzhalter / vorbereitet | Persistenzprofil steuert Persistenz noch nicht pro NPC vollständig |
| `stats` | Platzhalter / Role-nah | MaxHealth wirkt primär über Engine-Role |
| `drops` | Platzhalter | Keine aktive Drop-Logik |
| `attitude` | Platzhalter / Engine-nah | Nicht als Keystone-Runtime-System aktiv |
| `npcType` | Platzhalter | Noch keine starke Verhaltenslogik |
| `faction` | Platzhalter | Noch keine echte Faction-Logik |
| `structure` | Platzhalter / später | Kein aktives Structure-Spawn-System |

---

## 3. Validierter Zustand

### 3.1 Validiert

- Keystone-Index ist Primärquelle.
- Legacy-Index ist nur Fallback, wenn Keystone-Index fehlt oder leer ist.
- Kaputter Keystone-Index darf nicht still auf Legacy fallen.
- `skills` wird zuerst gelesen.
- Legacy-`capabilities` bleibt nur als Fallback erhalten.
- Keystone-relative Pfade funktionieren für neue Profile.
- `Server/NPC/Roles/` ist flach und enthält nur echte Engine-Roles.
- Dynamische Runtime-Roles wurden deaktiviert.
- `npc_group / shared / variants` wird unterstützt.
- `test_group.json` ist echtes `npc_group`.
- `lumberjack_group.json` ist echtes `npc_group`.
- `hytaleRole` ist bei Gruppen-Varianten Pflicht.
- Duplicate `id`, `role`, `hytaleRole` wird blockiert.
- `requiredMarkers` und `markerRoles` werden streng validiert.
- Appearance-Priorität ist dokumentiert.
- Legacy-Pfade wurden als Zielzustand entfernt.
- README für Keystone-Struktur ist vorgesehen / validiert.
- Interne Java-Namen wurden von `Capability` auf `Skill` umgestellt.
- `PersistenceProfile.respawnAfterRestart` wird aktiv als Restart-Auto-Respawn-Gate ausgewertet (kein impliziter Default-Respawn).

### 3.2 Absichtlich noch nicht umgesetzt

- Marker-v2 ist noch nicht umgesetzt.
- Appearance-Apply-Logik ist noch nicht umgesetzt.
- CombatProfile steuert noch kein vollständiges Kampfverhalten.
- SpawnProfile steuert noch kein vollständiges Spawn-System.
- PersistenceProfile steuert derzeit nur Teilbereiche aktiv (u. a. `respawnAfterRestart`-Gate); vollständige per-NPC-Runtime-Steuerung ist noch nicht umgesetzt.
- Action-Animation/Sound/Loop ist noch nicht vollständig an Runtime angebunden.

> [!IMPORTANT]
> Platzhalter-Profile dürfen geladen und validiert werden, aber spätere Patches müssen klar unterscheiden: „wird schon aktiv genutzt“ vs. „ist nur vorbereitet“.

---

## 4. Validierte Agent Steps / Feature-Phasen

## Step 1 — Keystone-Index + skills-first

### Ziel

Der Loader soll primär `Server/NPC/Keystone/npc/index.json` laden und neue Profile über `skills` unterstützen.

### Validierter Zustand

- Keystone-Index ist Primärquelle.
- Legacy-Index ist nur Fallback.
- Keystone-relative Profilpfade werden unterstützt.
- `skills` wird zuerst gelesen.
- Legacy-`capabilities` bleibt Fallback.

### Was geschützt wird

- Neue Struktur wird aktiv geladen.
- Alte Legacy-Struktur kann nicht still dominieren.
- Neue JSONs müssen nicht mehr `capabilities` als Profilpfad nutzen.

### Darf nicht kaputtgehen

- Kein stiller Fallback bei kaputtem Keystone-Index.
- Kein Bruch alter Legacy-Dateien während Übergangsphase.
- Kein Zugriff auf `target/` als Quelle.

### Review-Fragen

- [ ] Lädt der Loader `Keystone/npc/index.json` zuerst?
- [ ] Wird Legacy nur bei fehlendem/leeren Keystone-Index genutzt?
- [ ] Wird ein kaputter Keystone-Index klar geloggt?
- [ ] Funktioniert `skills` vor `capabilities`?
- [ ] Werden Keystone-relative Pfade korrekt aufgelöst?

### Test-Erwartung

```bash
mvn -q -DskipTests test-compile
```

---

## Step 2 — Keystone-Basisstruktur

### Ziel

Neue Basisordner und Standardprofile unter `Server/NPC/Keystone/` anlegen.

### Validierter Zustand

- `skills/`, `movement/`, `navigation/`, `combat/`, `spawns/`, `persistence/` existieren unter Keystone.
- Kein neuer `capabilities/`-Ordner.
- `npc/index.json` enthält nur `lumberjack/lumberjack_group.json` und `test/test_group.json`.

### Was geschützt wird

- Neue Ressourcen liegen nicht mehr gemischt unter altem `Server/NPC/`-Root.

### Darf nicht kaputtgehen

- Neue Profile dürfen nicht wieder außerhalb von Keystone angelegt werden.
- Kein neuer `capabilities/`-Ordner.

### Review-Fragen

- [ ] Liegen `spawns/` und `persistence/` unter Keystone?
- [ ] Gibt es keinen neuen `capabilities/`-Ordner?
- [ ] Enthält der Index nur Test und Lumberjack?

### Test-Erwartung

```bash
mvn -q -DskipTests test-compile
```

---

## Step 3 — Flache Hytale Engine-Roles

### Ziel

`Server/NPC/Roles/` enthält nur echte flache Engine-Roles.

### Validierter Zustand

Erlaubte Dateien:

```text
Template_Human_Friendly.json
Lumberjack.json
Lumberjack_Wife.json
Lumberjack_Oldman.json
Lumberjack_Oldwife.json
Test.json
```

### Was geschützt wird

- Hytale kann Role-Dateien beim Reload finden.
- Keine Mod-Definitionen oder Doku-Dateien in `Roles/`.

### Darf nicht kaputtgehen

- Keine Unterordner in `Roles/`.
- Keine Keystone-Mod-Konfiguration in `Roles/`.

### Review-Fragen

- [ ] Ist `Roles/` flach?
- [ ] Enthält `Roles/` nur echte Engine-Roles?
- [ ] Hat `Test.json` `Reference: Template_Human_Friendly`?

### Test-Erwartung

```bash
mvn -q -DskipTests test-compile
```

---

## Step 3.5 — Dynamic Runtime Role entfernen

### Ziel

Dynamische Rollen wie `KeystoneNPC_<npcId>_<roleId>_Role` entfernen oder entschärfen.

### Validierter Zustand

- Kein aktiver Generator für `KeystoneNPC_..._Role`.
- Kein dynamisches `setRoleName(...)` für Keystone Runtime-Roles.
- Role-Prefix-Fallback ist deaktiviert/entschärft.
- UUID-Relink bleibt Hauptweg.
- Anchor-Relink bleibt letzter Fallback.

### Was geschützt wird

- NPCs verschwinden nicht mehr durch fehlende dynamische Engine-Role-Dateien.
- `npcId` bleibt Mod-Identität, nicht Engine-Role.

### Darf nicht kaputtgehen

- Kein blindes Anchor-Binden als Ersatz.
- Kein automatischer Ersatzspawn bei Ambiguous.
- Keine Record-Löschung.

### Review-Fragen

- [ ] Gibt es noch `KeystoneNPC_` im aktiven Java-Code?
- [ ] Wird `setRoleName(...)` noch dynamisch verwendet?
- [ ] Bleibt UUID-Relink zuerst?
- [ ] Bleibt Anchor-Fallback letzter Fallback?
- [ ] Blockiert Ambiguous weiterhin?

### Test-Erwartung

Nach Restart darf kein Log erscheinen:

```text
Reloading nonexistent role KeystoneNPC_...
```

---

## Step 4a — npc_group / shared / variants Loader

### Ziel

Der Loader kann `npc_group`-Dateien laden und Varianten zu `NpcDefinition` machen.

### Validierter Zustand

- Alte Einzeldefinitionen funktionieren weiter.
- `npc_group` wird erkannt.
- `shared` wird in Varianten gemerged.
- Varianten überschreiben `shared`.
- Kaputte Gruppen werden nicht heimlich als concrete geladen.

### Was geschützt wird

- Viele Rollen können generisch über Gruppen geladen werden.
- Kein Hardcoding pro NPC-Typ.

### Darf nicht kaputtgehen

- Kein Marker-v2 nebenbei.
- Keine Runtime-Änderungen.
- Kein stilles Fallback bei kaputten Gruppen.

### Review-Fragen

- [ ] Wird `type = npc_group` erkannt?
- [ ] Werden Varianten einzeln registriert?
- [ ] Wird `shared` korrekt gemerged?
- [ ] Ist `hytaleRole` in Gruppen Pflicht?

### Test-Erwartung

```bash
mvn -q -DskipTests test-compile
```

---

## Step 4b — Test-NPC als echte Gruppe

### Ziel

`test_group.json` nutzt echtes `npc_group`-Format.

### Validierter Zustand

- Genau eine Variante: `test`.
- `hytaleRole = Test`.
- `requiredMarkers`: `bed`, `chest`, `food`, `work`.
- `markerRoles`: `bed=BED`, `chest=CHEST`, `food=FOOD`, `work=WORK`.
- Kein root-nahes `Keystone/npc/test.json`.

### Was geschützt wird

- Test-NPC dient als saubere Minimalreferenz.

### Darf nicht kaputtgehen

- Keine doppelte aktive Test-Definition.
- Kein `capabilities`-Profilpfad in neuer Definition.

### Review-Fragen

- [ ] Ist `test_group.json` `type=npc_group`?
- [ ] Gibt es genau eine Variante?
- [ ] Zeigt `hytaleRole` auf `Roles/Test.json`?

### Test-Erwartung

```bash
mvn -q -DskipTests test-compile
```

---

## Step 5 — Lumberjack als echte Gruppe

### Ziel

Lumberjack wird als `npc_group` mit mehreren Varianten migriert.

### Validierter Zustand

Varianten:

```text
lumberjack
lumberjack_wife
lumberjack_oldman
lumberjack_oldwife
```

Engine-Roles:

```text
Lumberjack
Lumberjack_Wife
Lumberjack_Oldman
Lumberjack_Oldwife
```

### Was geschützt wird

- Mehrere Rollen können über eine Gruppe verwaltet werden.
- `hytaleRole` bleibt echte Engine-Role.

### Darf nicht kaputtgehen

- Keine root-nahe `Keystone/npc/lumberjack.json`.
- Keine doppelte aktive Lumberjack-Definition.
- Keine Legacy-Löschung in diesem Step.

### Review-Fragen

- [ ] Existiert `lumberjack_group.json`?
- [ ] Ist es `type=npc_group`?
- [ ] Haben alle Varianten `hytaleRole`?
- [ ] Zeigen alle `hytaleRole` auf echte Role-Dateien?

### Test-Erwartung

```bash
mvn -q -DskipTests test-compile
```

---

## Step 6 — Validierung härten

### Ziel

Ungültige Definitionen werden nicht spawnbar.

### Validierter Zustand

- Duplicate `id` wird blockiert.
- Duplicate `role` wird blockiert.
- Duplicate `hytaleRole` wird blockiert.
- `hytaleRole` ist für Gruppen-Varianten Pflicht.
- `requiredMarkers` ist Pflicht und nicht leer.
- `markerRoles` ist Pflicht.
- `markerRoles` muss exakt dieselben Keys wie `requiredMarkers` haben.
- MarkerType-Werte werden validiert.

### Was geschützt wird

- Keine stillen Überschreibungen.
- Keine fehlerhaften Spawns wegen unvollständiger Definitionen.

### Darf nicht kaputtgehen

- Keine Option `allowDuplicateHytaleRole`.
- Keine Marker-Ableitung aus Routine/Actions/Structures.

### Review-Fragen

- [ ] Werden Duplicate `id`, `role`, `hytaleRole` blockiert?
- [ ] Sind invalid definitions nicht spawnbar?
- [ ] Werden Marker nicht automatisch abgeleitet?

### Test-Erwartung

```bash
mvn -q -DskipTests test-compile
```

---

## Step 7 — Appearance-Priorität dokumentieren

### Ziel

Klar dokumentieren, dass das echte sichtbare NPC-Model aus der Engine-Role kommt.

### Validierter Zustand

- `Server/NPC/Roles/<RoleName>.json` bestimmt das Engine-Model.
- Keystone-Appearance ist nur Mod-Konfiguration.
- Kein aktiver Appearance-Apply-Code vorhanden.

### Was geschützt wird

- Spätere Entwickler ändern nicht versehentlich nur Keystone-Appearance und erwarten Engine-Model-Wechsel.

### Darf nicht kaputtgehen

- Keine falsche Behauptung, dass Keystone-Appearance automatisch wirkt.
- Keine neue Apply-Logik ohne eigenen Step.

### Review-Fragen

- [ ] Ist die Appearance-Priorität dokumentiert?
- [ ] Wurde keine Apply-Logik nebenbei eingebaut?

### Test-Erwartung

```bash
mvn -q -DskipTests test-compile
```

---

## Step 8 — Legacy-Pfade entfernen

### Ziel

Alte ungenutzte Legacy-Ressourcen sicher entfernen.

### Validierter Zustand

Zu entfernende Legacy-Kandidaten:

```text
Server/NPC/npc/index.json
Server/NPC/npc/lumberjack/
Server/NPC/npc/undead_guard/
Server/NPC/templates/template_lumberjack.json
Server/NPC/templates/template_undead.json
Server/NPC/skills/undead_guard.json
Server/NPC/movement/undead_walk.json
Server/NPC/combat/undead_melee.json
Server/NPC/drops/undead_basic.json
Server/NPC/spawns/cursed_castle.json
Server/NPC/spawns/cursed_forest.json
```

### Was geschützt wird

- Legacy-Fallback kann nicht versehentlich alte `undead_guard`- oder alte Lumberjack-Definitionen aktivieren.

### Darf nicht kaputtgehen

- Nur referenzfreie Dateien löschen.
- Keine Java-Refactors.
- Keine Runtime-Änderungen.

### Review-Fragen

- [ ] Wurde vor Löschung nach Referenzen gesucht?
- [ ] Sind keine aktiven `undead_guard`-Referenzen übrig?
- [ ] Ist alter `Server/NPC/npc/index.json` weg?

### Test-Erwartung

```bash
mvn -q -DskipTests test-compile
```

---

## Step 9 — Keystone README

### Ziel

Neue Struktur für spätere Entwickler und AI-Agenten dokumentieren.

### Validierter Zustand

`Server/NPC/Keystone/README.md` erklärt:

- `Roles/` vs. `Keystone/`
- `npc/index.json`
- `npc_group`, `shared`, `variants`
- `hytaleRole` vs. `role`
- `npcId` als Instanz-ID
- Appearance-Regel
- `skills` statt neue `capabilities`-Pfade
- Marker-Grundregel ohne Marker-v2

### Was geschützt wird

- Spätere Änderungen verstehen die Struktur ohne neue Fehlinterpretation.

### Darf nicht kaputtgehen

- README darf keine falschen Aussagen über automatische Appearance-Änderung machen.

### Review-Fragen

- [ ] Existiert README?
- [ ] Erklärt sie Engine-Role vs. Keystone role?
- [ ] Vermeidet sie Marker-v2-Scheinumsetzung?

### Test-Erwartung

Markdown-only. Compile optional.

---

## Step 10 — Finaler Gesamtcheck

### Ziel

Gesamte Struktur prüfen.

### Validierter Zustand

- Roles sauber und flach.
- Keystone-Struktur sauber.
- Test und Lumberjack sind echte Gruppen.
- Keine dynamischen Runtime-Roles.
- Kein aktives `undead_guard`.
- Kein alter aktiver Lumberjack-Pfad.
- `skills` aktiv.
- `spawns/` und `persistence/` unter Keystone.
- Validierung aktiv.
- README vorhanden.
- Marker-v2 nicht eingebaut.

### Was geschützt wird

- Gesamtarchitektur bleibt konsistent.

### Darf nicht kaputtgehen

- Keine Legacy-Ressourcen versehentlich wieder einchecken.
- Keine fehlenden Gruppen-Dateien trotz Indexeintrag.

### Review-Fragen

- [ ] Zeigt `index.json` nur auf existierende Gruppen?
- [ ] Sind Legacy-Pfade wirklich weg?
- [ ] Sind alle `hytaleRole`-Dateien vorhanden?

### Test-Erwartung

```bash
mvn -q -DskipTests test-compile
```

---

## Step 11 — Capability → Skill Naming

### Ziel

Interne Java-Namen von Capability auf Skill umstellen, ohne Verhalten zu ändern.

### Validierter Zustand

- `SkillResolver`, `SkillSet`, `SkillProfile`, `SkillChecks`, `NpcSkill` sind aktiv.
- Alte aktive Klassen `CapabilityResolver`, `CapabilitySet`, `CapabilityProfile`, `CapabilityChecks`, `NpcCapability` sind entfernt.
- Package `keystone.npc.capabilities` wird nicht mehr aktiv importiert.
- `skills()` wird im Code verwendet.
- Legacy-Fallbacks bleiben bewusst erhalten:

```java
@SerializedName("capabilities")
@SerializedName(value = "requiresSkill", alternate = {"requiresCapability"})
@SerializedName(value = "logSkillChecks", alternate = {"logCapabilityChecks"})
```

### Was geschützt wird

- Neue Begriffe sind sauber.
- Alte JSONs können weiterhin gelesen werden.

### Darf nicht kaputtgehen

- Kein Entfernen des Legacy-Fallbacks ohne eigenen Step.
- Keine JSON-Strukturänderung nebenbei.
- Kein Marker-v2.

### Review-Fragen

- [ ] Gibt es noch aktive `Capability...`-Klassen?
- [ ] Funktioniert `skills` weiterhin?
- [ ] Bleibt Legacy-`capabilities` absichtlich lesbar?

### Test-Erwartung

```bash
mvn -q -DskipTests test-compile
```

---

## 5. Kritische No-Go-Regeln

## 5.1 Keine dynamischen Hytale Engine-Roles

Verboten:

```text
NPCEntity.setRoleName("KeystoneNPC_<npcId>_<roleId>_Role")
```

Erlaubt / erforderlich:

```text
Engine-roleName bleibt echte existierende Role aus Server/NPC/Roles/<RoleName>.json
npcId bleibt nur Mod-Instanz-ID in state.json
```

## 5.2 Keine Vermischung von Engine-Role und Keystone role

Verboten:

```text
role = "Lumberjack" als Engine-Role behandeln
hytaleRole = dynamisch aus npcId erzeugen
```

Erlaubt / erforderlich:

```text
role = fachliche Keystone-Rolle
hytaleRole = echte Hytale Engine-Role
```

## 5.3 Kein stiller Legacy-Fallback bei kaputtem Keystone-Index

Verboten:

```text
Keystone/npc/index.json existiert, ist kaputt, Loader fällt still auf npc/index.json zurück
```

Erlaubt / erforderlich:

```text
Kaputten Keystone-Index klar loggen und nicht still Legacy aktivieren
```

## 5.4 Keine neue `capabilities/`-Struktur für neue Dateien

Verboten:

```text
Server/NPC/Keystone/capabilities/
"capabilities": "capabilities/human_worker.json"
```

Erlaubt / erforderlich:

```text
Server/NPC/Keystone/skills/
"skills": "skills/human_worker.json"
```

## 5.5 Keine Duplicate-Definitionen

Verboten:

```text
zwei Varianten mit gleicher id
zwei Varianten mit gleicher role
zwei Varianten mit gleicher hytaleRole
```

Erlaubt / erforderlich:

```text
Definition invalid markieren und Spawn blockieren
```

## 5.6 Keine automatische Marker-Ableitung

Verboten:

```text
requiredMarkers aus routine ableiten
requiredMarkers aus actions ableiten
requiredMarkers aus structures ableiten
```

Erlaubt / erforderlich:

```text
requiredMarkers und markerRoles explizit in group variant definieren
```

## 5.7 Kein Marker-v2 nebenbei

Verboten:

```text
/knpc marker set umbauen
WALKING_TO_BED entfernen
currentMarker/targetMarker Runtime neu modellieren
```

Erlaubt / erforderlich:

```text
Marker-v2 nur in eigenem Plan/Step umsetzen
```

## 5.8 Kein stilles Reconcile-Overwrite von markerAssignments

Verboten:

```text
Load/Restore/Validation/Diagnose/Tick
-> markerAssignments werden still bereinigt
-> stateDirty wird durch read-only Reconcile gesetzt
-> saveStateSafely() wird durch read-only Reconcile ausgelöst
```

Erlaubt / erforderlich:

```text
Read-only Kontexte dürfen markerAssignments nur lesen/diagnostizieren.
Read-only Kontexte dürfen Legacy-Markerfelder niemals mutieren.
Mutierende Marker-Zuweisung/Reconcile ist nur in explizitem Spawn/Admin/Repair/Cleanup-Kontext erlaubt (harte Allowlist).
Kaputte markerAssignments dürfen nicht still in state.json zurückgeschrieben werden.
```

Methodenstatus (Marker-Audit):

```text
resolveRequiredMarkerWithFallbackAssigning(...) ist entfernt (nicht mehr aktiv)
resolveRequiredMarkerWithFallback(...) ist entfernt (nicht mehr aktiv)
resolveRequiredMarkerReadOnly(...) ist der verbindliche read-only Resolver
MarkerRegistry.getNextAvailable(...) bleibt deprecated Lookup-Helfer und keine Reconcile-Wahrheit
MarkerRingTraversal bleibt intern in der Marker-Registry gekapselt
```

---

## 6. Standard Review-Prozess für spätere Patches

## Wenn `NpcDefinitionRegistry.java` geändert wurde

Prüfen:

- [ ] Keystone-Index bleibt Primärquelle.
- [ ] Legacy-Index bleibt nur Fallback.
- [ ] Kaputter Keystone-Index fällt nicht still auf Legacy.
- [ ] `npc_group` wird nicht als `concrete` fehlinterpretiert.
- [ ] `shared` wird korrekt in Varianten gemerged.
- [ ] Duplicate `id` wird blockiert.

## Wenn `RoleDefinitionRegistry.java` geändert wurde

Prüfen:

- [ ] `hytaleRole` wird für Gruppen genutzt.
- [ ] Keine dynamische Role wird erzeugt.
- [ ] Duplicate `hytaleRole` wird blockiert.
- [ ] Engine-Role-Datei muss existieren.

## Wenn `NpcTemplateResolver.java` geändert wurde

Prüfen:

- [ ] `requiredMarkers` bleibt Pflicht.
- [ ] `markerRoles` bleibt Pflicht.
- [ ] Keys müssen exakt passen.
- [ ] Keine Marker-Ableitung aus Routine/Actions/Structures.
- [ ] `skills` bleibt primär.
- [ ] Legacy-Fallback bleibt bewusst, nicht zufällig.

## Wenn `SkillResolver`, `SkillSet`, `SkillProfile`, `SkillChecks` geändert wurden

Prüfen:

- [ ] `skills`-Pfad funktioniert.
- [ ] Legacy-`capabilities`-Innenfeld bleibt lesbar, falls noch nötig.
- [ ] Keine Rückbenennung auf Capability-Namen.
- [ ] Skill-Checks verändern nicht ungewollt Tür-/Routine-Verhalten.

## Wenn `NpcRoutineRunner.java` oder `RelinkWorkflowService.java` geändert wurde

Prüfen:

- [ ] Keine dynamische Engine-Role wird gesetzt.
- [ ] UUID-Relink bleibt Hauptweg.
- [ ] Anchor-Fallback bleibt letzter Fallback.
- [ ] Ambiguous blockiert weiterhin.
- [ ] Kein Blind-Respawn.
- [ ] Spawn-Rollback bleibt intakt.

## Wenn JSON-Ressourcen geändert wurden

Prüfen:

- [ ] `index.json` zeigt nur auf existierende Gruppen.
- [ ] Jede Gruppe ist `type=npc_group`.
- [ ] Jede Variante hat `hytaleRole`.
- [ ] `hytaleRole`-Datei existiert in `Roles/`.
- [ ] Kein `capabilities`-Profilpfad in neuer Definition.
- [ ] Keine Legacy-Dateien wieder eingeführt.

---

## 7. Compile- und Test-Gates

### Pflicht-Compile

```bash
mvn -q -DskipTests test-compile
```

### Minimaltest

- Server startet ohne Definition-Loader-Fehler.
- Keystone-Index wird geladen.
- Test-NPC ist registriert.
- Lumberjack-Varianten sind registriert.

### Negativ-Test: kaputter Keystone-Index

Erwartung:

```text
Keystone index parse error wird geloggt
kein stiller Fallback auf Legacy npc/index.json
```

### Negativ-Test: fehlende hytaleRole

Erwartung:

```text
Variant invalid / nicht spawnbar
kein PascalCase-Fallback für npc_group
```

### Negativ-Test: Duplicate hytaleRole

Erwartung:

```text
beide betroffenen Rollen invalid / blockiert
kein Spawn
```

### Negativ-Test: Marker-Mismatch

Beispiel falsch:

```json
"requiredMarkers": ["bed", "food"],
"markerRoles": {
  "bed": "BED",
  "cook": "FOOD"
}
```

Erwartung:

```text
invalid definition
kein Spawn
```

### Restart-Test

Erwartung:

```text
kein Log: Reloading nonexistent role KeystoneNPC_...
NPC-Records bleiben erhalten
UUID-Relink versucht zuerst
kein Blind-Anchor-Bind
```

### Legacy-Test

Erwartung:

```text
Legacy-Fallback nur bei fehlendem/leeren Keystone-Index
alte capabilities-Innenfelder bleiben lesbar, falls bewusst erlaubt
```

---

## 8. Bekannte Fehlerbilder und Diagnose

## 8.1 Dynamische Runtime-Role beim Restart

Symptom:

```text
Reloading nonexistent role KeystoneNPC_..._lumberjack_Role
NPC verschwindet nach Restart, Record bleibt in state.json
```

Mögliche Ursache:

```text
npcId wurde in Engine-roleName gemischt
NPCEntity.setRoleName(...) setzt dynamische Role
```

Prüfen:

- Suche nach `KeystoneNPC_`.
- Suche nach `setRoleName`.
- Prüfe `RelinkWorkflowService` und `NpcRoutineRunner`.

Sofortiger Fix / Richtung:

```text
Dynamisches setRoleName deaktivieren
hytaleRole auf echte Role-Datei beschränken
UUID-Relink beibehalten
kein blindes Anchor-Binden
```

## 8.2 Keystone-Index fehlt oder ist kaputt

Symptom:

```text
unerwartet alte NPCs aktiv
undead_guard taucht wieder auf
alte npc/index.json wird geladen
```

Mögliche Ursache:

```text
Keystone/npc/index.json fehlt, ist leer oder kaputt
Legacy-Fallback wird aktiv
```

Prüfen:

- Existiert `Server/NPC/Keystone/npc/index.json`?
- Enthält er nur `lumberjack/lumberjack_group.json` und `test/test_group.json`?
- Existieren diese Dateien wirklich?

Sofortiger Fix / Richtung:

```text
Keystone-Index reparieren
nicht still auf Legacy fallen, wenn Keystone-Index kaputt ist
```

## 8.3 Fehlende group-Datei trotz Indexeintrag

Symptom:

```text
Index lädt lumberjack/lumberjack_group.json
Datei nicht gefunden
Lumberjack wird nicht registriert
```

Mögliche Ursache:

```text
ZIP/Repo-Version inkonsistent
Datei gelöscht oder nicht committed
```

Prüfen:

- `Server/NPC/Keystone/npc/lumberjack/lumberjack_group.json`
- `Server/NPC/Keystone/npc/index.json`

Sofortiger Fix / Richtung:

```text
Fehlende group-Datei wiederherstellen oder Index korrigieren
```

## 8.4 Duplicate id / role / hytaleRole

Symptom:

```text
Definition invalid
Spawn blockiert
Rolle erscheint nicht in Status/Spawn-Liste
```

Mögliche Ursache:

```text
Zwei Varianten teilen id, role oder hytaleRole
```

Prüfen:

- Alle Varianten in allen `*_group.json`.
- RoleDefinitionRegistry invalid reasons.

Sofortiger Fix / Richtung:

```text
id, role und hytaleRole eindeutig machen
keine allowDuplicateHytaleRole-Option einbauen
```

## 8.5 Marker-Mismatch

Symptom:

```text
Spawn blockiert wegen fehlender Marker
Definition invalid wegen markerRoles mismatch
```

Mögliche Ursache:

```text
requiredMarkers und markerRoles haben unterschiedliche Keys
MarkerType-Wert ungültig
```

Prüfen:

- `requiredMarkers`
- `markerRoles`
- gültige MarkerType-Werte

Sofortiger Fix / Richtung:

```text
Keys exakt gleich machen
keine Alias-Logik einbauen
```

## 8.6 Falsche Appearance-Erwartung

Symptom:

```text
Keystone appearance/default.json geändert, NPC-Modell ändert sich nicht
```

Mögliche Ursache:

```text
Keystone-Appearance ist nur Mod-Konfig
Engine-Model kommt aus Server/NPC/Roles/<RoleName>.json
```

Prüfen:

- `hytaleRole` der Variante.
- passende Datei in `Server/NPC/Roles/`.
- `Modify.Appearance` in Role-Datei.

Sofortiger Fix / Richtung:

```text
Engine-Model in Role-Datei ändern
Appearance-Apply-Logik nur in eigenem Feature-Step bauen
```

## 8.7 Alte Legacy-Ressourcen wieder eingeführt

Symptom:

```text
Server/NPC/npc/index.json ist wieder vorhanden
Server/NPC/npc/undead_guard/ ist wieder vorhanden
alte lumberjack-Dateien tauchen wieder auf
```

Mögliche Ursache:

```text
alte ZIP/Branch-Version gemerged
unrelated Refactor hat alte Ressourcen wiederhergestellt
```

Prüfen:

- `Server/NPC/npc/`
- `Server/NPC/templates/`
- `Server/NPC/skills/undead_guard.json`
- `Server/NPC/spawns/cursed_*.json`

Sofortiger Fix / Richtung:

```text
Referenzprüfung durchführen
nur referenzfreie Legacy-Dateien entfernen
```

---

## 9. Pflicht bei Änderungen an diesem Feature

Wenn eine AI oder ein Entwickler dieses Feature bewusst verändert, muss diese Datei aktualisiert werden.

Das gilt bei Änderungen an:

- Architektur
- Loader-Verhalten
- Role-Auflösung
- JSON-Struktur
- Persistenz
- Runtime-Verhalten
- Relink/Restore/Respawn
- Duplicate-Regeln
- Marker-Regeln
- Skill-/Legacy-Fallback
- Tests
- Fehlerbehandlung
- Ordner-/Dateistruktur

Bei Änderung muss ergänzt werden:

```text
- Was wurde geändert?
- Welche alte Regel wurde ersetzt?
- Welche neue Regel gilt?
- Welche neuen Tests sind Pflicht?
- Welche Risiken entstehen?
- Wie wird Regression verhindert?
```

> [!IMPORTANT]
> Wenn eine validierte Regel bewusst geändert wird, muss diese Datei im selben Patch angepasst werden.

---

## 10. Aktueller validierter Abschlussstand

```text
Step 1: Keystone-Index + skills-first validiert
Step 2: Keystone-Basisstruktur validiert
Step 3: Flache Engine-Roles validiert
Step 3.5: Dynamische Runtime-Roles deaktiviert
Step 4a: npc_group/shared/variants Loader validiert
Step 4b: Test-NPC als npc_group validiert
Step 5: Lumberjack-Gruppe validiert
Step 6: hytaleRole/Duplicate/Marker-Basisvalidierung validiert
Step 7: Appearance-Priorität dokumentiert
Step 8: Legacy-Pfade als Zielzustand entfernt
Step 9: Keystone README validiert
Step 10: Finaler Gesamtcheck validiert
Step 11: Capability → Skill interner Naming-Refactor validiert
```

### Feature-Status

```text
JSON-Hierarchie: validiert
Role-Trennung: validiert
Group-Loader: validiert
Skill-Naming: validiert
Legacy-Fallback: bewusst erhalten
Marker-v2: später geplant
Appearance-Apply: später geplant
Combat/Spawn Runtime-Anbindung: später geplant
Persistence Runtime-Anbindung: teilweise aktiv (respawnAfterRestart-Gate), sonst später geplant
```

---

## 11. Kurze Pflicht-Checkliste für jede spätere AI

```text
[ ] Compile grün?
[ ] target/ ignoriert?
[ ] Keine No-Go-Regel verletzt?
[ ] Roles bleibt flach?
[ ] Keystone/npc/index.json zeigt nur auf existierende Gruppen?
[ ] Jede npc_group-Variante hat hytaleRole?
[ ] hytaleRole-Dateien existieren in Server/NPC/Roles/ ?
[ ] Keine dynamischen KeystoneNPC_... Engine-Roles?
[ ] respawnAfterRestart bleibt als explizites Profile-Gate erhalten (kein impliziter Auto-Respawn)?
[ ] Kein undead_guard wieder aktiv?
[ ] Kein alter Server/NPC/npc/lumberjack-Pfad wieder aktiv?
[ ] skills bleibt neuer Pfadname?
[ ] Legacy-capabilities-Fallback nur bewusst erhalten?
[ ] Duplicate id/role/hytaleRole wird blockiert?
[ ] requiredMarkers/markerRoles bleiben strikt?
[ ] Marker-v2 nicht versehentlich halb eingebaut?
[ ] Read-only Reconcile überschreibt markerAssignments/state.json nicht still?
[ ] Marker-Mutation bleibt auf Spawn/Admin/Repair/Cleanup begrenzt?
[ ] resolveRequiredMarkerWithFallbackAssigning/resolveRequiredMarkerWithFallback bleiben entfernt?
[ ] getNextAvailable bleibt deprecated und wird nicht in read-only Pfaden verwendet?
[ ] Runtime-Safety-Gates erhalten?
[ ] Logs geprüft?
[ ] Tests ergänzt, falls Verhalten geändert?
[ ] Falls Regel geändert: diese Datei aktualisiert?
```

---

## 12. Empfohlene Commit-Regel

Feature-/Safety-Patches müssen getrennt committet werden.

Nicht erlaubt:

```text
JSON-Struktur + Runtime-Relink + Marker-v2 + Rename-Refactor in einem Commit
```

Empfohlen:

```text
feat(npc-json): add keystone npc_group loader
fix(npc-role): prevent dynamic runtime engine roles
refactor(npc-skills): rename capability internals to skill
chore(npc-resources): remove legacy npc resource paths
_docs(npc-json): add json hierarchy control file
```

Commit-Regeln:

- Keine großen unrelated Refactors im selben Commit.
- Jede Safety-Regel braucht eigenen Review.
- Jeder Lösch-Commit braucht vorher Referenzprüfung.
- Jede Änderung an Loader/Relink/Spawn braucht Compile-Gate.
- Jede bewusste Architekturänderung aktualisiert diese Datei.

---

## 13. AI-Ergänzung aus Gesprächskontext

- Spätere AI-Agenten dürfen alte ZIP-/Branch-Zustände nicht blind als final betrachten. Wenn Legacy-Dateien wieder auftauchen oder Gruppen fehlen, muss zuerst der aktuelle Ressourcenbaum geprüft werden.
- `target/` und compiled `.jar` dürfen für diese Kontrolllogik nicht als Quelle gelten; relevant sind `src/main/java` und `src/main/resources`.
- Besonders gefährlich sind unrelated Refactors an `NpcRoutineRunner`, `RelinkWorkflowService`, `NpcDefinitionRegistry`, `RoleDefinitionRegistry` und `NpcTemplateResolver`, weil sie validierte Safety-Regeln indirekt brechen können.
- Hytale-Engine-Felder wie `roleName` dürfen nicht für Keystone-Identität, `npcId` oder fachliche `role` missbraucht werden.
- Wenn eine dieser Regeln bewusst geändert wird, muss diese Kontroll-Datei sofort aktualisiert werden: neue Review-Fragen, neue Negativ-Tests und klare Begründung, warum die neue Architektur sicherer oder notwendig ist.

---

## Letzte Regel

> [!CAUTION]
> **Wenn eine dieser Regeln bewusst geändert wird, muss diese Kontroll-Datei sofort aktualisiert werden.**
> Die Änderung braucht neue Review-Fragen, neue Negativ-Tests und eine klare Begründung, warum die neue Architektur sicherer oder notwendig ist.
