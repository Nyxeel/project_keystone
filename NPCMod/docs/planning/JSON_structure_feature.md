# Keystone JSON-Hierarchy Feature-Plan

> **Projekt:** Hytale-Mod `NPCMod / KeystoneNPC`  
> **Feature:** Neue `Server/NPC` JSON-, Role-, Skill- und Keystone-Hierarchie  
> **Status:** Abschlussplan / validierte Feature-Struktur  
> **Ziel:** Die JSON-Struktur soll verschiedene NPCs datengetrieben erzeugen können, ohne Hytale-Engine-Roles, Keystone-Rollen und konkrete NPC-Instanzen zu vermischen.

---

## Geänderte Hauptbereiche

```text
src/main/java/keystone/npc/definition/
src/main/java/keystone/npc/skills/
src/main/java/keystone/npc/routine/
src/main/java/keystone/npc/relink/
src/main/resources/Server/NPC/Roles/
src/main/resources/Server/NPC/Keystone/
docs/safety/json_hierarchy.md
docs/safety/npc_restart_relink_control.md
AGENTS.md
```

Wichtige betroffene Dateien / Klassen:

```text
NpcDefinitionRegistry.java
NpcDefinition.java
EffectiveNpcDefinition.java
NpcTemplateResolver.java
RoleDefinitionRegistry.java
NpcProfileRefs.java
SkillResolver.java
SkillSet.java
SkillProfile.java
SkillChecks.java
NpcSkill.java
NpcRoutineRunner.java
RelinkWorkflowService.java
NpcDebugSupport.java
NpcStatusCommand.java
ActionDefinition.java
NpcDebugDefinition.java
```

Wichtige Ressourcen:

```text
Server/NPC/Roles/Template_Human_Friendly.json
Server/NPC/Roles/Lumberjack.json
Server/NPC/Roles/Lumberjack_Wife.json
Server/NPC/Roles/Lumberjack_Oldman.json
Server/NPC/Roles/Lumberjack_Oldwife.json
Server/NPC/Roles/Test.json

Server/NPC/Keystone/npc/index.json
Server/NPC/Keystone/npc/test/test_group.json
Server/NPC/Keystone/npc/lumberjack/lumberjack_group.json
Server/NPC/Keystone/skills/human_worker.json
Server/NPC/Keystone/movement/human_walk.json
Server/NPC/Keystone/navigation/friendly_worker.json
Server/NPC/Keystone/combat/peaceful.json
Server/NPC/Keystone/spawns/forest_village.json
Server/NPC/Keystone/persistence/persistent_citizen.json
Server/NPC/Keystone/README.md
```

---

# Neuer Feature-Plan: JSON-Hierarchy v1 sicher finalisieren

## Grundentscheidung

Die JSON-Hierarchy wurde nicht als reiner Ordner-Refactor behandelt.

Sie schützt drei getrennte Ebenen:

```text
1. Hytale Engine-Role
2. Keystone role / roleId
3. konkrete NPC-Instanz / npcId
```

Wichtigster Satz:

```text
Hytale Engine-Role ≠ Keystone roleId ≠ npcId
```

Darum gilt:

```text
Server/NPC/Roles/
= echte Hytale Engine-Roles

Server/NPC/Keystone/
= Keystone-Mod-Konfiguration

state.json
= konkrete NPC-Instanzen
```

---

## Zielarchitektur

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

---

# Phase 0 — Audit: aktuelle JSON-/Role-Struktur prüfen

## Ziel

Nur prüfen, nichts ändern.

## Prüfen

```text
Server/NPC/Roles/
Server/NPC/Keystone/
Server/NPC/npc/
Server/NPC/templates/
Server/NPC/skills/
Server/NPC/capabilities/
Server/NPC/movement/
Server/NPC/navigation/
Server/NPC/combat/
Server/NPC/spawns/
Server/NPC/persistence/
Server/NPC/drops/
```

Java-Prüfung:

```text
NpcDefinitionRegistry.java
NpcDefinition.java
NpcTemplateResolver.java
RoleDefinitionRegistry.java
NpcProfileRefs.java
CapabilityResolver / SkillResolver
SpawnNpcCommand.java
NpcStatusCommand.java
NpcDebugSupport.java
NpcRoutineRunner.java
RelinkWorkflowService.java
```

Besonders suchen nach:

```text
npc/index.json
Keystone/npc/index.json
capabilities
skills
hytaleRole
role
npcId
KeystoneNPC_
setRoleName(...)
undead_guard
Server/NPC/npc/lumberjack
```

## Ergebnis dieses Steps

- Liste aktiver Legacy-Referenzen
- Liste alter `capabilities`-Stellen
- Liste löschbarer Legacy-Dateien
- Liste unsicherer Dateien
- Entscheidung, ob Loader zuerst umgebaut werden muss

## Review-Fragen

```text
[ ] Wurde target/ ignoriert?
[ ] Wurden aktive Legacy-Referenzen gefunden?
[ ] Wurde unterschieden zwischen aktiv und nur Doku?
[ ] Wurde geprüft, ob Keystone/npc/index.json bereits geladen wird?
[ ] Wurde nichts geändert?
```

---

# Phase 1 — Keystone-Index und skills-first vorbereiten

## Ziel

Der Loader nutzt zuerst:

```text
Server/NPC/Keystone/npc/index.json
```

Der alte Index ist nur Übergangs-Fallback:

```text
Server/NPC/npc/index.json
```

## Neue Regel

```text
Keystone-Index existiert und ist kaputt
→ klar loggen
→ nicht still Legacy laden
```

Nur erlaubt:

```text
Keystone-Index fehlt oder ist leer
→ Legacy-Fallback erlaubt
```

## Skills-Regel

Neu:

```json
"skills": "skills/human_worker.json"
```

Legacy-Fallback:

```json
"capabilities": "skills/human_worker.json"
```

## Review-Fragen

```text
[ ] Keystone-Index ist Primärquelle?
[ ] Legacy nur bei fehlendem/leeren Keystone-Index?
[ ] Kaputter Keystone-Index fällt nicht still zurück?
[ ] skills wird vor capabilities gelesen?
[ ] Keystone-relative Pfade funktionieren?
[ ] Compile grün?
```

---

# Phase 2 — Keystone-Basisstruktur anlegen

## Ziel

Neue Basisprofile unter `Server/NPC/Keystone/` anlegen.

## Erstellen

```text
Server/NPC/Keystone/npc/index.json
Server/NPC/Keystone/skills/human_worker.json
Server/NPC/Keystone/movement/human_walk.json
Server/NPC/Keystone/navigation/friendly_worker.json
Server/NPC/Keystone/combat/peaceful.json
Server/NPC/Keystone/spawns/forest_village.json
Server/NPC/Keystone/persistence/persistent_citizen.json
```

## Index

```json
[
  "lumberjack/lumberjack_group.json",
  "test/test_group.json"
]
```

## Review-Fragen

```text
[ ] Index enthält nur lumberjack und test?
[ ] Kein undead_guard im Keystone-Index?
[ ] Kein capabilities-Ordner unter Keystone?
[ ] spawns liegt unter Keystone?
[ ] persistence liegt unter Keystone?
[ ] Compile grün?
```

---

# Phase 3 — Hytale-Roles flach finalisieren

## Ziel

`Server/NPC/Roles/` enthält nur echte Hytale Engine-Roles.

## Erlaubte Dateien

```text
Template_Human_Friendly.json
Lumberjack.json
Lumberjack_Wife.json
Lumberjack_Oldman.json
Lumberjack_Oldwife.json
Test.json
```

## No-Go

Nicht erlaubt:

```text
role-template.md
lumberjack.template.json
Template_Lumberjack.json
KeystoneNPC_<npcId>_<roleId>_Role.json
Unterordner in Roles/
```

## Review-Fragen

```text
[ ] Roles ist flach?
[ ] Nur echte Engine-Roles vorhanden?
[ ] Test.json ist Variant von Template_Human_Friendly?
[ ] Keine Keystone-Mod-Logik in Roles?
[ ] Compile grün?
```

---

# Phase 3.5 — Dynamic Runtime Role entfernen

## Ziel

Dynamische Engine-Roles verhindern.

## Fehlerbild

Alte falsche Idee:

```text
KeystoneNPC_<npcId>_<roleId>_Role
```

Gefährlich, wenn gesetzt über:

```text
NPCEntity.setRoleName(...)
```

Hytale erwartet dann echte Role-Dateien unter:

```text
Server/NPC/Roles/
```

## Verboten

```text
NPCEntity.setRoleName("KeystoneNPC_<npcId>_<roleId>_Role")
```

## Final gültig

```text
Engine-roleName = echte Hytale Role
Keystone roleId = fachliche Rolle
npcId = Instanz-ID in state.json
```

## Review-Fragen

```text
[ ] Kein aktiver KeystoneNPC_-Role-Generator?
[ ] Kein dynamisches setRoleName mit npcId?
[ ] Role-Prefix-Fallback entschärft?
[ ] UUID-Relink bleibt Hauptweg?
[ ] Anchor-Fallback bleibt letzter Fallback?
[ ] Kein Ambiguous-Auto-Respawn?
[ ] Compile grün?
```

---

# Phase 4 — npc_group / shared / variants Loader

## Ziel

Der Loader kann Gruppen laden.

## Neues Format

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
      "hytaleRole": "Test"
    }
  ]
}
```

## Regeln

```text
npc_group ist nur Container
jede variant wird zu fertiger NpcDefinition
shared gilt für alle variants
variant darf shared überschreiben
```

## Wichtig

Keine Marker-v2-Logik in diesem Step.

## Review-Fragen

```text
[ ] Alte concrete-Definitionen funktionieren weiter?
[ ] npc_group wird erkannt?
[ ] variants werden einzeln registriert?
[ ] shared wird gemerged?
[ ] variant überschreibt shared?
[ ] kaputte group-Datei wird nicht heimlich als concrete geladen?
[ ] Compile grün?
```

---

# Phase 5 — Test-NPC als echtes npc_group

## Ziel

Minimaler Test-NPC im neuen Format.

## Erwartung

```text
test_group.json
type = npc_group
genau 1 variant
id = test
role = test
hytaleRole = Test
```

## Marker

```text
requiredMarkers:
- bed
- chest
- food
- work

markerRoles:
bed -> BED
chest -> CHEST
food -> FOOD
work -> WORK
```

## No-Go

```text
Kein root-nahes Keystone/npc/test.json
Kein capabilities-Feld
Keine doppelte aktive Test-Definition
```

## Review-Fragen

```text
[ ] test_group.json ist type=npc_group?
[ ] genau eine Variante?
[ ] hytaleRole=Test?
[ ] requiredMarkers und markerRoles passen exakt?
[ ] kein capabilities-Feld?
[ ] keine root-nahe test.json?
[ ] Compile grün?
```

---

# Phase 6 — Lumberjack-Gruppe migrieren

## Ziel

Lumberjack wird als echte Gruppe mit mehreren Varianten definiert.

## Varianten

```text
lumberjack
lumberjack_wife
lumberjack_oldman
lumberjack_oldwife
```

## hytaleRole

```text
lumberjack -> Lumberjack
lumberjack_wife -> Lumberjack_Wife
lumberjack_oldman -> Lumberjack_Oldman
lumberjack_oldwife -> Lumberjack_Oldwife
```

## Shared Profile

```json
"shared": {
  "skills": "skills/human_worker.json",
  "movement": "movement/human_walk.json",
  "navigation": "navigation/friendly_worker.json",
  "combat": "combat/peaceful.json",
  "spawn": "spawns/forest_village.json",
  "persistence": "persistence/persistent_citizen.json"
}
```

## Review-Fragen

```text
[ ] lumberjack_group.json existiert?
[ ] type=npc_group?
[ ] alle vier Varianten vorhanden?
[ ] jede Variante hat hytaleRole?
[ ] hytaleRole-Dateien existieren in Roles?
[ ] kein root-nahes Keystone/npc/lumberjack.json?
[ ] kein capabilities-Feld?
[ ] Compile grün?
```

---

# Phase 7 — Validierung härten

## Ziel

Ungültige Definitionen dürfen nicht spawnbar sein.

## Harte Regeln

```text
hytaleRole Pflicht bei npc_group-variants
kein PascalCase-Fallback für npc_group ohne hytaleRole
duplicate id blockieren
duplicate role blockieren
duplicate hytaleRole blockieren
requiredMarkers Pflicht
markerRoles Pflicht
markerRoles Keys exakt gleich requiredMarkers
MarkerType-Werte validieren
```

## Verboten

```text
allowDuplicateHytaleRole
automatische Marker-Ableitung aus routine
automatische Marker-Ableitung aus actions
automatische Marker-Ableitung aus structures
```

## Review-Fragen

```text
[ ] hytaleRole Pflicht?
[ ] kein PascalCase-Fallback für npc_group?
[ ] duplicate id/role/hytaleRole blockiert?
[ ] requiredMarkers Pflicht?
[ ] markerRoles Pflicht?
[ ] Keys exakt gleich?
[ ] invalid definitions nicht spawnbar?
[ ] kein Marker-v2 nebenbei?
[ ] Compile grün?
```

---

# Phase 8 — Appearance-Priorität dokumentieren

## Ziel

Klarstellen, wo das sichtbare NPC-Modell geändert wird.

## Regel

```text
Das echte Engine-Model kommt aus:
Server/NPC/Roles/<RoleName>.json
```

Keystone-Appearance:

```text
Server/NPC/Keystone/npc/.../appearances/
```

ist aktuell nur Mod-Konfiguration.

## Noch nicht aktiv

```text
Kein automatisches setAppearance
Kein automatisches Skin-Apply
Kein Model-Wechsel aus Keystone-Appearance
```

## Review-Fragen

```text
[ ] Dokumentiert, dass Roles/<RoleName>.json gewinnt?
[ ] Dokumentiert, dass Keystone-Appearance nur Konfig ist?
[ ] Keine neue Apply-Logik eingebaut?
[ ] Compile grün?
```

---

# Phase 9 — Legacy-Pfade entfernen

## Ziel

Alte Ressourcen entfernen, wenn referenzfrei.

## Entfernen

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

## Neue Regel

```text
Vor jedem Löschen Referenzsuche.
Bei Unsicherheit nicht löschen.
```

## Review-Fragen

```text
[ ] Vor Löschung Referenzen gesucht?
[ ] Nur referenzfreie Dateien gelöscht?
[ ] kein aktiver undead_guard?
[ ] kein alter Server/NPC/npc/lumberjack-Pfad?
[ ] alter npc/index.json nicht aktiv?
[ ] Compile grün?
```

---

# Phase 10 — Keystone README schreiben

## Ziel

Die neue Struktur einfach erklären.

## README muss erklären

```text
Roles/ vs Keystone/
npc/index.json
npc_group
shared
variants
hytaleRole vs role
npcId als Instanz-ID
Appearance-Regel
skills statt capabilities
Marker-Grundregel ohne Marker-v2
```

## Review-Fragen

```text
[ ] README existiert?
[ ] erklärt Roles vs Keystone?
[ ] erklärt hytaleRole vs role?
[ ] erklärt npcId?
[ ] erklärt Appearance korrekt?
[ ] baut kein Marker-v2 ein?
```

---

# Phase 11 — Finaler Gesamtcheck

## Ziel

Gesamtzustand prüfen.

## Prüfen

```text
Roles sauber und flach
Keystone-Struktur sauber
test_group echtes npc_group
lumberjack_group echtes npc_group
alle hytaleRole vorhanden
keine dynamischen Runtime-Roles
kein aktives undead_guard
kein alter lumberjack-Pfad
skills aktiv
capabilities höchstens Legacy-Fallback
spawns/persistence unter Keystone
Validierung aktiv
README vorhanden
Marker-v2 nicht eingebaut
Compile grün
```

## Review-Fragen

```text
[ ] index.json zeigt nur auf existierende Gruppen?
[ ] alle hytaleRole-Dateien existieren?
[ ] keine Legacy-Dateien wieder drin?
[ ] keine KeystoneNPC_ Runtime-Role?
[ ] Compile grün?
```

---

# Phase 12 — Internes Naming Capability → Skill

## Ziel

Interne Java-Begriffe sauber auf Skill umstellen.

## Umbenennen

```text
CapabilityResolver -> SkillResolver
CapabilitySet -> SkillSet
CapabilityProfile -> SkillProfile
CapabilityChecks -> SkillChecks
NpcCapability -> NpcSkill
capabilities() -> skills()
```

## Bewusst erhalten

Legacy-Fallbacks:

```java
@SerializedName("capabilities")
@SerializedName(value = "requiresSkill", alternate = {"requiresCapability"})
@SerializedName(value = "logSkillChecks", alternate = {"logCapabilityChecks"})
```

## Review-Fragen

```text
[ ] keine aktiven CapabilityResolver/CapabilitySet-Klassen?
[ ] package keystone.npc.capabilities nicht mehr aktiv importiert?
[ ] skills funktioniert weiter?
[ ] capabilities-Fallback bleibt bewusst?
[ ] kein Marker-v2 eingebaut?
[ ] Compile grün?
```

---

# Welche JSON-Einträge steuern Java bereits?

## Aktiv

```text
npc/index.json
type = npc_group
shared
variants
id
role
hytaleRole
requiredMarkers
markerRoles
routine
skills
```

## Teilweise aktiv

```text
movement
actions
navigation
debug
displayName
nameTranslationKey
defaultState
```

## Noch Platzhalter / vorbereitet

```text
appearance
combat
spawn
persistence
stats
drops
attitude
npcType
faction
structure
markerActions
action animation/sound/loop
```

## Ziel später

Langfristig sollen alle JSON-Einträge Java-Logik steuern.

Aber aktuell gilt:

```text
Nur Felder als aktiv behandeln, die wirklich vom Java-Code genutzt werden.
Platzhalter-Profile nicht als fertige Runtime-Features verkaufen.
```

---

# Logic Errors im bisherigen JSON-Plan

## Fehler 1 — Group-Format geplant, aber Loader konnte es nicht

Problem:

```text
test_group.json sollte npc_group sein,
aber Loader konnte nur concrete NpcDefinition lesen.
```

Fix:

```text
Erst Group-Loader bauen,
dann test_group.json umbauen.
```

---

## Fehler 2 — hytaleRole fehlte als harte Pflicht

Problem:

```text
PascalCase-Fallback hätte falsche oder zufällige Engine-Roles erzeugen können.
```

Fix:

```text
npc_group-variant ohne hytaleRole = invalid.
```

---

## Fehler 3 — Engine-Role und Keystone-Identität wurden vermischt

Problem:

```text
KeystoneNPC_<npcId>_<roleId>_Role
```

als Hytale roleName.

Fix:

```text
Dynamisches setRoleName deaktiviert.
Engine-Role bleibt echte Role-Datei.
```

---

## Fehler 4 — Legacy-Dateien konnten wieder aktiv werden

Problem:

```text
Alter npc/index.json konnte undead_guard oder alte lumberjack-Struktur wieder aktivieren.
```

Fix:

```text
Keystone-Index primär.
Legacy-Fallback nur bei fehlendem/leeren Keystone-Index.
Legacy-Ressourcen nach Referenzcheck entfernen.
```

---

## Fehler 5 — skills außen, capabilities innen war verwirrend

Problem:

```text
Neue JSONs nutzen skills,
interner Java-Code hieß weiter Capability.
```

Fix:

```text
Interne Namen auf Skill umgestellt.
Legacy @SerializedName("capabilities") bleibt bewusst erhalten.
```

---

# Weitere Logic Errors, die besonders gefährlich bleiben

## 1. Alte ZIP-/Branch-Versionen können Legacy zurückbringen

Gefahr:

```text
Server/NPC/npc/index.json taucht wieder auf
undead_guard taucht wieder auf
lumberjack_group fehlt trotz Indexeintrag
```

Regel:

```text
Vor Final-Review immer echten src/main/resources Baum prüfen.
target/ ignorieren.
```

---

## 2. Index zeigt auf fehlende Dateien

Gefahr:

```text
index.json lädt lumberjack/lumberjack_group.json
Datei fehlt
Lumberjack wird nicht registriert
```

Regel:

```text
Jeder Index-Eintrag muss auf existierende Datei zeigen.
```

---

## 3. Role-Dateien fehlen für hytaleRole

Gefahr:

```text
hytaleRole = Lumberjack_Wife
aber Server/NPC/Roles/Lumberjack_Wife.json fehlt
```

Regel:

```text
Jede hytaleRole braucht echte Role-Datei.
```

---

## 4. Platzhalter-Profile werden als aktive Logik missverstanden

Gefahr:

```text
spawn/persistence/combat existieren,
aber steuern Runtime noch nicht vollständig.
```

Regel:

```text
Aktiv vs vorbereitet immer klar dokumentieren.
```

---

## 5. Marker-v2 wird nebenbei halb eingebaut

Gefahr:

```text
requiredMarkers/markerRoles werden verändert,
aber Runtime bleibt alter MarkerType-State-Mix.
```

Regel:

```text
Marker-v2 nur in eigenem Plan.
Nicht neben JSON-Hierarchy ändern.
```

---

# Neue Reihenfolge kurz

```text
1. Audit
2. Keystone-Index + skills-first
3. Keystone-Basisstruktur
4. flache Engine-Roles
5. Dynamic Runtime Role entfernen
6. npc_group/shared/variants Loader
7. Test-NPC als npc_group
8. Lumberjack als npc_group
9. Validierung härten
10. Appearance-Priorität dokumentieren
11. Legacy-Pfade entfernen
12. README schreiben
13. Finaler Gesamtcheck
14. Capability -> Skill Naming
15. Danach erst Marker-v2 planen
```

---

# Wichtigster Satz für den Agent

```text
Die JSON-Hierarchy ist erst sicher, wenn Engine-Roles, Keystone-Definitionen und NPC-Instanzen strikt getrennt bleiben und kein Loader-, Runtime- oder Legacy-Fallback diese Trennung heimlich wieder aufhebt.
```

---

# Pflicht-Review bei jeder späteren Änderung

```text
[ ] target/ ignoriert?
[ ] safety/json_hierarchy.md geprüft?
[ ] safety/npc_restart_relink_control.md geprüft, falls Runtime berührt?
[ ] Keine dynamische Engine-Role?
[ ] Roles bleibt flach?
[ ] Keystone-Index zeigt nur auf existierende Gruppen?
[ ] Jede variant hat hytaleRole?
[ ] hytaleRole-Datei existiert?
[ ] skills bleibt neuer Profilpfad?
[ ] Legacy capabilities nur bewusst als Fallback?
[ ] keine alten Legacy-Ressourcen wieder eingeführt?
[ ] keine Marker-v2-Teilumsetzung?
[ ] Compile grün?
```

---

# Commit-Regel

Gute Commit-Beispiele:

```text
feat(npc-json): add keystone npc_group loader
refactor(npc-json): split engine roles from keystone definitions
fix(npc-role): disable dynamic runtime engine roles
refactor(npc-skills): rename capability internals to skill
chore(npc-resources): remove legacy npc resource paths
docs(npc-json): add json hierarchy feature plan
```

Nicht erlaubt:

```text
JSON-Hierarchy + Marker-v2 + Runtime-Respawn + Door-Rework in einem Commit
```

---

# Abschluss

Dieses Feature ist die Grundlage dafür, später NPCs vollständig datengetrieben über JSON zu erzeugen.

Aktueller Stand:

```text
JSON lädt und validiert NPC-Gruppen.
Engine-Roles sind sauber getrennt.
Skill-Naming ist umgestellt.
Mehrere NPC-Varianten sind möglich.
Viele Profile sind vorbereitet.
Noch nicht alle Profile steuern bereits echte Runtime-Logik.
```

Nächste große Features nach dieser Basis:

```text
1. Marker-v2
2. Appearance-Apply-Logik
3. Action-System für Animation/Sound
4. SpawnProfile Runtime-Anbindung
5. PersistenceProfile Runtime-Anbindung
6. CombatProfile Runtime-Anbindung
```
