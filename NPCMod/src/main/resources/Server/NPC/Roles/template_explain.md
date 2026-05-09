# Template_Lumberjack.json Erklärung

## Überblick
Dies ist ein **Abstract Role Template** - eine Vorlage für NPC-Rollen. Sie definiert alle Grundeigenschaften eines Lumberjack-NPCs und kann von Variant-Rollen (wie Lumberjack.json) referenziert und überschrieben werden.

---

## Struktur Breakdown

### `"Type": "Abstract"`
- **Bedeutung**: Dies ist eine Vorlage/Template
- **Verwendung**: Kann nicht direkt gespawnt werden, wird von Variant-Rollen referenziert
- **Gegensatz**: `"Type": "Variant"` = konkrete, spawnbare Rolle

---

### `"Parameters": { ... }`
Definiert **Vorlagen-Variablen**, die von Variant-Rollen überschrieben werden können.

#### `"Appearance"`
- **Value**: `"Goblin_Ogre"`
- **Bedeutung**: Das 3D-Modell des NPCs
- **Verwendung**: Wird mit `"Compute": "Appearance"` verlinkt
- **Optionen**: Andere Modelle wie "Human_Varyn", "Feran_Beast", etc.

#### `"DropList"`
- **Value**: `"Empty"`
- **Bedeutung**: Items die der NPC dropped wenn er stirbt
- **Verwendung**: Wird mit `"Compute": "Droplist"` verlinkt
- **Optionen**: "Empty", "CommonLoot", "RareLoot", etc.

#### `"MaxHealth"`
- **Value**: `100`
- **Bedeutung**: Maximale Lebenspunkte des NPCs
- **Verwendung**: Wird mit `"Compute": "MaxHealth"` verlinkt
- **Auswirkung**: Beeinflusst wie lange der NPC überlebt

#### `"NameTranslationKey"`
- **Value**: `"server.npcRoles.Lumberjack.name"`
- **Bedeutung**: Sprachschlüssel für die Anzeige des NPC-Namens im Spiel
- **Verwendung**: Wird mit `"Compute": "NameTranslationKey"` verlinkt
- **Format**: `"server.npcRoles.[RoleName].name"` für Konsistenz

---

### Compute Fields (Verbindung Parameter → Wert)

```json
"Appearance": { "Compute": "Appearance" },
"DropList": { "Compute": "Droplist" },
"MaxHealth": { "Compute": "MaxHealth" },
"NameTranslationKey": { "Compute": "NameTranslationKey" }
```

- **Bedeutung**: Diese Felder aktivieren die entsprechenden Parameter
- **Funktionsweise**: `"Compute": "ParameterName"` bedeutet: "Verwende den Wert aus Parameters.ParameterName"
- **Varianten können das überschreiben**: Z.B. kann Lumberjack.json `"Appearance": "Different_Model"` in "Modify" setzen
- **Wichtig**: Ohne diese Compute-Felder würde der NPC die Parameter ignorieren

---

### Attitude (Verhalten gegenüber Spielern/NPCs)

```json
"AttitudeGroup": "PlayerFriendly",
"DefaultPlayerAttitude": "Friendly",
"DefaultNPCAttitude": "Neutral"
```

#### `"AttitudeGroup": "PlayerFriendly"`
- **Bedeutung**: Kategorie/Gruppierung der NPC-Einstellung
- **Auswirkung**: Der NPC gehört zur Kategorie "PlayerFriendly"
- **Verwendung**: Kann für Dialogue-Trees und spezielle Verhalten genutzt werden
- **Andere Optionen**: "Hostile", "Neutral", "Merchant", etc.

#### `"DefaultPlayerAttitude": "Friendly"`
- **Bedeutung**: Standard-Verhalten gegenüber dem Spieler
- **Auswirkung**: Der NPC greift den Spieler nicht an, kann interagiert werden
- **Optionen**:
  - `"Friendly"` = NPC ist hilfreich und freundlich
  - `"Hostile"` = NPC greift den Spieler an
  - `"Neutral"` = NPC ignoriert den Spieler

#### `"DefaultNPCAttitude": "Neutral"`
- **Bedeutung**: Standard-Verhalten gegenüber anderen NPCs
- **Auswirkung**: Der NPC ignoriert andere NPCs
- **Optionen**:
  - `"Friendly"` = NPC arbeitet mit anderen NPCs zusammen
  - `"Hostile"` = NPC greift andere NPCs an
  - `"Neutral"` = NPC ignoriert andere NPCs

---

### `"MotionControllerList": [ ... ]`
Definiert wie der NPC **sich bewegt** und mit Physik interagiert.

```json
{
  "Type": "Walk",
  "MaxWalkSpeed": 3,
  "Gravity": 10,
  "MaxFallSpeed": 8,
  "Acceleration": 10
}
```

#### `"Type": "Walk"`
- **Bedeutung**: Der NPC kann gehen (nicht fliegen, nicht schwimmen)
- **Andere Typen**: Z.B. "Swim", "Fly", "Climb"
- **Auswirkung**: Bestimmt welche Animationen und Bewegungsmuster der NPC nutzen kann

#### `"MaxWalkSpeed": 3`
- **Bedeutung**: Maximum Gehgeschwindigkeit
- **Einheit**: Hytale Einheiten pro Sekunde
- **Auswirkung**: Schnellere NPCs sind schwerer zu treffen
- **Balance**: 3 ist normal für Arbeiter-NPCs

#### `"Gravity": 10`
- **Bedeutung**: Wie schnell der NPC fällt
- **Auswirkung**: Höherer Wert = schnellerer Fall (realistische Physik)
- **Typischer Wert**: 10 ist Standard-Erdgravitation

#### `"MaxFallSpeed": 8`
- **Bedeutung**: Maximum Fallgeschwindigkeit (Limit)
- **Auswirkung**: Begrenzt wie schnell der NPC maximal fallen kann
- **Sicherheit**: Verhindert Clippen durch den Boden bei zu schnellen Falls

#### `"Acceleration": 10`
- **Bedeutung**: Wie schnell der NPC von 0 auf MaxWalkSpeed beschleunigt
- **Auswirkung**: Höherer Wert = schneller reagiert der NPC auf Bewegungsbefehle
- **Gameplay**: Höhere Werte machen den NPC reaktiver und agiler

---

### `"Instructions": [ ... ]`
Definiert die **KI-Logik** und Verhalten-Sensoren des NPCs.

```json
{
  "Sensor": {
    "Type": "Any"
  },
  "BodyMotion": {
    "Type": "Nothing"
  }
}
```

#### `"Sensor": { "Type": "Any" }`
- **Bedeutung**: Der NPC reagiert auf alle Eingaben/Events
- **Type "Any"**: Keine Filterung, alles wird erfasst
- **Funktionsweise**: Der NPC empfängt Signale von allen Quellen
- **Alternative Typen**:
  - `"PlayerProximity"` = reagiert nur wenn Spieler in der Nähe
  - `"Sight"` = reagiert nur auf sichtbare Dinge
  - `"Damage"` = reagiert nur auf Schaden

#### `"BodyMotion": { "Type": "Nothing" }`
- **Bedeutung**: Der NPC führt keine automatische/vordefinierte Aktion aus
- **Type "Nothing"**: Wartet auf externe Befehle (z.B. vom Scheduler in der Mod)
- **Funktionsweise**: Der NPC ist "passiv" und wird vom Code gesteuert
- **Alternative Typen**:
  - `"PatrolWaypoints"` = NPC patrouilliert automatisch
  - `"AttackNearestEnemy"` = NPC greift automatisch an
  - `"Idle"` = NPC macht zufällige Idle-Animationen

---

## Type und Variant: Detaillierte Erklärung

### Type: "Abstract" (Template)

```json
{
  "Type": "Abstract",
  "Parameters": {
    "Appearance": { "Value": "Goblin_Ogre", "Description": "..." },
    "MaxHealth": { "Value": 100, "Description": "..." }
  },
  "Appearance": { "Compute": "Appearance" },
  "MaxHealth": { "Compute": "MaxHealth" },
  "AttitudeGroup": "PlayerFriendly",
  "MotionControllerList": [ ... ],
  "Instructions": [ ... ]
}
```

#### Was ist ein "Abstract"?
- **Definition**: Ein **Bauplan** oder **Vorlage** für NPC-Rollen
- **Abstraktheit**: Enthält nur die Struktur, noch keine konkreten Werte (nur Parameter)
- **Instanziierung**: Kann NICHT direkt gespawnt werden
- **Verwendung**: Wird von `Variant`-Dateien referenziert

#### Struktur eines Abstract
1. **Type**: MUSS `"Abstract"` sein
2. **Parameters**: Definiert alle variablen Werte
   - Jeder Parameter hat `"Value"` und `"Description"`
   - Diese Werte können von Varianten überschrieben werden
3. **Compute Fields**: Verbindet Parameter mit der Rolle
   - `"Compute": "ParameterName"` aktiviert den Parameter
4. **Feste Eigenschaften**: AttitudeGroup, MotionControllerList, Instructions
   - Diese gelten für ALLE Varianten, die dieses Template nutzen

#### Warum braucht man Abstracts?
✅ **Code-Wiederverwendung**: Eine Vorlage für alle Lumberjack-Varianten
✅ **Konsistenz**: Alle Varianten haben die gleiche Basis
✅ **Wartung**: Änderung im Template wirkt auf alle Varianten
✅ **Speicher**: Eine Abstract-Datei, mehrere Variant-Dateien

#### Beispiel: Abstract ist wie ein Klassen-Blueprint
```
Abstract = Klasse
Variant = Instanz der Klasse
```

---

### Type: "Variant" (Konkrete Rolle)

```json
{
  "Type": "Variant",
  "Reference": "Template_Lumberjack",
  "Modify": {
    "Appearance": "Goblin_Ogre",
    "MaxHealth": 100,
    "MaxWalkSpeed": 3
  }
}
```

#### Was ist ein "Variant"?
- **Definition**: Eine **konkrete, spawnbare NPC-Rolle**
- **Konkretheit**: Definiert einen SPAWNBAREN NPC mit konkreten Werten
- **Instanziierung**: KANN direkt gespawnt werden
- **Verwendung**: Das ist das, was du in der Spielwelt siehst

#### Struktur eines Variant
1. **Type**: MUSS `"Variant"` sein
2. **Reference**: Name des Abstract-Templates
   - `"Reference": "Template_Lumberjack"` = nutze dieses Template als Basis
3. **Modify** (Optional): Werte die das Template überschreiben
   - Nur hier änderbare Werte sollten stehen
   - Was nicht in "Modify" steht, wird vom Template geerbt

#### Was wird geerbt vom Template?
- ❌ NICHT geändert: MotionControllerList, Instructions, AttitudeGroup
- ✅ KANN geändert werden (in "Modify"): Parameter aus dem Template

#### Beispiel: Variant ist wie eine konkrete Instanz
```
Abstract = Klasse Car { color, speed, wheels }
Variant = Car myCar = new Car() { color: "red", speed: 50 }
```

---

## Zusammenhang: Template vs. Variant - Praktisches Beispiel

### Szenario: Drei verschiedene Lumberjack-Typen

**Template_Lumberjack.json** (Abstract - einmal für alle)
```json
{
  "Type": "Abstract",
  "Parameters": {
    "Appearance": { "Value": "Goblin_Ogre" },
    "MaxHealth": { "Value": 100 },
    "MaxWalkSpeed": { "Value": 3 }
  },
  "Appearance": { "Compute": "Appearance" },
  "MaxHealth": { "Compute": "MaxHealth" },
  "MaxWalkSpeed": { "Compute": "MaxWalkSpeed" },
  "AttitudeGroup": "PlayerFriendly",
  "DefaultPlayerAttitude": "Friendly",
  "MotionControllerList": [ { "Type": "Walk", ... } ],
  "Instructions": [ { "Type": "Any", "BodyMotion": "Nothing" } ]
}
```

**Lumberjack.json** (Variant - Standard)
```json
{
  "Type": "Variant",
  "Reference": "Template_Lumberjack",
  "Modify": {
    "Appearance": "Goblin_Ogre",
    "MaxHealth": 100,
    "MaxWalkSpeed": 3
  }
}
```
→ Normale Lumberjacks: Goblin, 100 HP, langsam

**Lumberjack_Elite.json** (Variant - Stärker)
```json
{
  "Type": "Variant",
  "Reference": "Template_Lumberjack",
  "Modify": {
    "Appearance": "Goblin_Ogre",
    "MaxHealth": 200,
    "MaxWalkSpeed": 5
  }
}
```
→ Elite Lumberjacks: Goblin, 200 HP, schneller

**Lumberjack_Human.json** (Variant - Andere Rasse)
```json
{
  "Type": "Variant",
  "Reference": "Template_Lumberjack",
  "Modify": {
    "Appearance": "Human_Varyn",
    "MaxHealth": 100,
    "MaxWalkSpeed": 3
  }
}
```
→ Menschliche Lumberjacks: Human, 100 HP, langsam

#### Was alle gemeinsam haben (vom Template):
✅ AttitudeGroup: "PlayerFriendly"
✅ DefaultPlayerAttitude: "Friendly"
✅ MotionControllerList gleich
✅ Instructions gleich

#### Was unterschiedlich ist (in "Modify"):
❌ Appearance unterschiedlich
❌ MaxHealth unterschiedlich
❌ MaxWalkSpeed unterschiedlich

---

## Lade-Prozess: Abstracts + Variants

### Schritt-für-Schritt was passiert

#### User ruft auf:
```java
NPCPlugin.get().getIndex("Lumberjack_Elite");
```

#### Server macht folgendes:

**1. Suche nach Variant-Datei**
```
Suche: NPC/Roles/Lumberjack_Elite.json
Gefunden: Type "Variant", Reference "Template_Lumberjack"
```

**2. Lade das Template**
```
Suche: NPC/Roles/Template_Lumberjack.json
Gefunden: Type "Abstract", Parameters {...}
```

**3. Merge Prozess**
```
Template = Basis (alle Properties aus Abstract)
Modify = Überrides (aus Variant)
Result = Template.merge(Modify)
```

**Pseudo-Code**:
```
template = load("Template_Lumberjack.json")
variant = load("Lumberjack_Elite.json")

finalRole = {
  // Alles vom Template
  ...template,
  // ABER überschrieben mit Modify-Werten
  ...variant.Modify
}

// finalRole ist jetzt:
{
  Appearance: "Goblin_Ogre",        // from Modify
  MaxHealth: 200,                    // from Modify
  MaxWalkSpeed: 5,                   // from Modify
  AttitudeGroup: "PlayerFriendly",  // from Template (nicht in Modify, also erhalten)
  MotionControllerList: [...],      // from Template
  Instructions: [...]                // from Template
}
```

**4. NPC wird mit finalen Werten gespawnt**
```
NPCPlugin.spawnEntity(store, roleIndex, pos, rot, null, null)
→ Erstellt Entity mit 200 HP, Goblin-Modell, 5 Speed
```

---

## Debugging: Type-Fehler

### Problem: "NPCPlugin.getIndex() gibt -1 zurück"

**Häufige Fehler**:

❌ **Variant hat falschen Template-Namen**
```json
// FALSCH:
{
  "Type": "Variant",
  "Reference": "Template_LumberJack"  // Großbuchstaben falsch!
}
```
→ Server kann Template nicht finden → -1

✅ **Richtig**:
```json
{
  "Type": "Variant",
  "Reference": "Template_Lumberjack"  // Exakter Name!
}
```

---

❌ **Abstract hat kein Parameters-Block**
```json
{
  "Type": "Abstract",
  // FEHLT: "Parameters": { ... }
  "Appearance": { "Compute": "Appearance" }
}
```
→ Compute kann nicht auf Parameter zugreifen → Fehler

✅ **Richtig**:
```json
{
  "Type": "Abstract",
  "Parameters": {
    "Appearance": { "Value": "Goblin_Ogre" }
  },
  "Appearance": { "Compute": "Appearance" }
}
```

---

❌ **Variant mit fehlender Modify**
```json
{
  "Type": "Variant",
  "Reference": "Template_Lumberjack"
  // FEHLT: "Modify": { ... }
}
```
→ Technisch OK, aber alle Werte vom Template (kann gut sein, muss aber bewusst sein)

---

## Key Takeaways: Type: Abstract vs Variant

| Aspekt | Abstract | Variant |
|--------|----------|---------|
| **Type-Wert** | `"Abstract"` | `"Variant"` |
| **Zweck** | Vorlage/Blueprint | Konkrete Rolle |
| **Kann gespawnt werden?** | ❌ Nein | ✅ Ja |
| **Enthält Parameters?** | ✅ Ja | ❌ Nein |
| **Referenziert andere?** | ❌ Nein | ✅ Ja (via Reference) |
| **Kann überschrieben werden?** | ❌ (aber Template kann geändert werden) | ✅ Ja (via Modify) |
| **Beispiel-Datei** | `Template_Lumberjack.json` | `Lumberjack.json` |
| **Datei-Menge** | 1 pro Basis-Rolle | Mehrere pro Rolle (verschiedene Varianten) |

---

## Lade-Prozess: Von JSON zur Spielwelt

### Schritt 1: Command wird ausgeführt
```java
int roleIndex = NPCPlugin.get().getIndex("Lumberjack");
```

### Schritt 2: NPCPlugin sucht die Rolle
- Sucht nach `Lumberjack.json` in `NPC/Roles/`
- Findet: `"Reference": "Template_Lumberjack"`
- Lädt auch: `Template_Lumberjack.json`

### Schritt 3: Template + Variant werden gemergt
- Template liefert: Alle Eigenschaften
- Variant überschreibt: Werte in `"Modify"`
- Resultat: Komplette Rollen-Definition

### Schritt 4: NPC wird gespawnt
```java
Pair<Ref<EntityStore>, NPCEntity> pair =
  NPCPlugin.get().spawnEntity(store, roleIndex, position, rotation, null, null);
```
- Server erstellt Entity mit Lumberjack-Eigenschaften
- Entity erhält Goblin_Ogre Modell, 100 HP, Friendly Attitude

### Schritt 5: NpcRecord wird erstellt
- `NpcScheduler.spawnLumberjack()` erstellt neuen `NpcRecord`
- `scheduler.linkEntityRef()` verbindet die Entity mit dem Record
- NPC ist nun aktiv und kann tickt werden

---

## Praktische Beispiele: Modifikationen

### Beispiel 1: Schnellerer Lumberjack
Ändere in `Lumberjack.json`:
```json
"Modify": {
  "Appearance": "Goblin_Ogre",
  "MaxHealth": 100,
  "MaxWalkSpeed": 5
}
```
→ Lumberjack läuft doppelt so schnell

### Beispiel 2: Aggressiver Lumberjack
Erstelle `Lumberjack_Angry.json`:
```json
{
  "Type": "Variant",
  "Reference": "Template_Lumberjack",
  "Modify": {
    "DefaultPlayerAttitude": "Hostile",
    "DefaultNPCAttitude": "Hostile"
  }
}
```
→ Neue hostile Variante, greift Spieler und NPCs an

### Beispiel 3: Verschiedene Modelle
Erstelle `Lumberjack_Human.json`:
```json
{
  "Type": "Variant",
  "Reference": "Template_Lumberjack",
  "Modify": {
    "Appearance": "Human_Varyn"
  }
}
```
→ Menschlicher Lumberjack statt Goblin

---

## Key Takeaways

✅ **Template** = Definition aller Eigenschaften
✅ **Variant** = Konkrete Instanz mit möglichen Änderungen
✅ **Parameters** = Variablen die überschrieben werden können
✅ **Compute** = Aktiviert einen Parameter
✅ **Attitude** = Kontrolliert Freundlichkeit/Feindlichkeit
✅ **MotionController** = Physik und Bewegung
✅ **Instructions** = KI-Verhalten und Sensoren

---

## Debugging Tips

**Problem**: NPCPlugin.getIndex("Lumberjack") gibt -1 zurück
- ✓ Stelle sicher `Lumberjack.json` existiert in `NPC/Roles/`
- ✓ Stelle sicher die JSON Syntax ist korrekt (keine Kommas vergessen)
- ✓ Stelle sicher `manifest.json` hat `"IncludesAssetPack": true`

**Problem**: NPC sieht seltsam aus
- ✓ Überprüfe `"Appearance": "Goblin_Ogre"` - Modell existiert?

**Problem**: NPC bewegt sich nicht
- ✓ Überprüfe `"BodyMotion": { "Type": "Nothing" }` - das ist korrekt
- ✓ Überprüfe NpcScheduler - gibt es den richtigen Bewegungsbefehl?

**Problem**: NPC attackiert Spieler obwohl er freundlich sein soll
- ✓ Überprüfe `"DefaultPlayerAttitude": "Friendly"`
- ✓ Manchmal überschreiben andere Regeln die Attitude
