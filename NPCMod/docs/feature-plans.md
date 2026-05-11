

# MVP B PLanung der Features

der richtige Schritt Richtung MVP B: nicht mehr „Tag = Work, Nacht = Bed“, sondern ein echter Schedule-Kreis aus mehreren Stationen.

Nachgedacht für ein paar Sekunden

Ja, so würde ich es bauen:

NPC Definition JSON
→ sagt: welche Routine benutzt dieser NPC?

Routine JSON
→ sagt: welche Tagespunkte gibt es?

Enum
→ sagt: welche States technisch existieren?
1. Enum nicht mit fixer Reihenfolge überladen

Der Enum sollte nicht direkt „Bob macht zuerst Bett, dann Truhe, dann Essen“ speichern.

Besser:

NpcState:
SLEEPING
WALKING_TO_MARKER
USING_CHEST
EATING
WORKING
CHILLING
IDLE

Der Enum beschreibt also was der NPC gerade tut, nicht den ganzen Tagesplan.

2. Tagesablauf kommt aus JSON

Zum Beispiel:

{
  "routineId": "lumberjack_default",
  "requiredMarkers": [
    "bed",
    "door",
    "chest",
    "food",
    "work",
    "chill"
  ],
  "schedule": [
    {
      "time": "06:00",
      "marker": "chest",
      "state": "USING_CHEST"
    },
    {
      "time": "07:00",
      "marker": "food",
      "state": "EATING"
    },
    {
      "time": "08:00",
      "marker": "work",
      "state": "WORKING"
    },
    {
      "time": "12:00",
      "marker": "food",
      "state": "EATING"
    },
    {
      "time": "13:00",
      "marker": "work",
      "state": "WORKING"
    },
    {
      "time": "18:00",
      "marker": "chill",
      "state": "CHILLING"
    },
    {
      "time": "21:00",
      "marker": "bed",
      "state": "SLEEPING"
    }
  ]
}
3. Dann zirkuliert der NPC automatisch

Deine Logik macht dann:

aktuelle Hytale-Zeit prüfen
→ passenden Schedule-Eintrag finden
→ Zielmarker laden
→ NPC dorthin bewegen
→ angekommen: passenden State setzen
→ warten bis nächster Zeitblock kommt
→ nächster Marker

Also nicht mehr hart im Java-Code:

wenn Tag → work
wenn Nacht → bed

sondern:

frage JSON: Was soll dieser NPC um diese Uhrzeit tun?
4. Pflichtmarker sind wichtig

Beim Laden der Routine prüfst du:

Hat dieses Haus/NPC-Setup alle requiredMarkers?

Wenn nein:

NPC nicht starten
oder
NPC geht in BROKEN_ROUTINE / IDLE

Beispiel:

Routine braucht: bed, chest, food, work
Haus hat nur: bed, work

→ Routine ungültig

Das ist wichtig für Worldgen später.

5. Door bleibt Sonderfall

door_marker ist kein normaler Tagespunkt.

Er ist eher ein Zwischenpunkt:

NPC will zu work
→ Ziel liegt außerhalb vom Haus
→ erst zu door
→ Tür öffnen
→ dann zu work

Also:

bed → door → work

Aber im Tagesplan steht nur:

work

Die Navigation entscheidet dann selbst: „Ich brauche unterwegs die Tür.“

Meine Empfehlung für deine Architektur
NpcState enum
= technische Zustände

Routine JSON
= Tagesplan

Marker JSON / House JSON
= verfügbare Punkte

NavigationController
= bringt NPC von Punkt zu Punkt

RoutineController
= entscheidet, welcher Punkt jetzt dran ist

Kurz gesagt:

Ja, Tageszeiten + Pflichtmarker gehören ins JSON.
Der Enum soll nur die möglichen Aktionen/States kennen.
Die Reihenfolge soll aus der Routine-JSON kommen.

#####

für MVP B kannst du alles in einer einzigen JSON machen.

Später kannst du es trennen, aber am Anfang ist eine Datei leichter.

Beispiel:

{
  "npcType": "lumberjack",
  "displayName": "Bob der Holzfäller",

  "requiredMarkers": [
    "bed",
    "door",
    "chest",
    "food",
    "work",
    "chill"
  ],

  "markers": {
    "bed": {
      "markerId": "lumberjack_bed_01",
      "role": "BED"
    },
    "door": {
      "markerId": "lumberjack_door_01",
      "role": "DOOR"
    },
    "chest": {
      "markerId": "lumberjack_chest_01",
      "role": "CHEST"
    },
    "food": {
      "markerId": "lumberjack_food_01",
      "role": "FOOD"
    },
    "work": {
      "markerId": "lumberjack_work_01",
      "role": "WORK"
    },
    "chill": {
      "markerId": "lumberjack_chill_01",
      "role": "CHILL"
    }
  },

  "routine": [
    {
      "time": "06:00",
      "targetMarker": "chest",
      "state": "USING_CHEST",
      "durationMinutes": 20
    },
    {
      "time": "06:30",
      "targetMarker": "food",
      "state": "EATING",
      "durationMinutes": 30
    },
    {
      "time": "07:30",
      "targetMarker": "work",
      "state": "WORKING",
      "durationMinutes": 240
    },
    {
      "time": "12:00",
      "targetMarker": "food",
      "state": "EATING",
      "durationMinutes": 60
    },
    {
      "time": "13:00",
      "targetMarker": "work",
      "state": "WORKING",
      "durationMinutes": 240
    },
    {
      "time": "18:00",
      "targetMarker": "chill",
      "state": "CHILLING",
      "durationMinutes": 120
    },
    {
      "time": "20:00",
      "targetMarker": "food",
      "state": "EATING",
      "durationMinutes": 45
    },
    {
      "time": "21:00",
      "targetMarker": "bed",
      "state": "SLEEPING",
      "durationMinutes": 540
    }
  ]
}

Dazu passt dein Java ungefähr so:

NpcState enum
= USING_CHEST, EATING, WORKING, CHILLING, SLEEPING

JSON requiredMarkers
= sagt, was dieses NPC-Setup braucht

JSON markers
= verbindet Namen wie "bed" mit echten Marker-IDs

JSON routine
= sagt, wann der NPC wohin geht

RoutineController
= liest aktuelle Uhrzeit und wählt den passenden routine-Eintrag

NavigationController
= bewegt NPC zum targetMarker

Wichtig: door steht zwar in requiredMarkers, aber nicht zwingend in der Routine.

Warum?

Weil door meistens nur ein Zwischenpunkt ist:

NPC ist im Haus und Ziel ist draußen
→ gehe erst zu door
→ öffne Tür
→ gehe zu work

Die Routine sagt nur:

07:30 → work

Die Navigation entscheidet dann:

Dafür muss ich vorher durch die door.

Für MVP B: eine JSON pro NPC-Typ reicht völlig.
Später besser trennen in:

lumberjack_npc.json
lumberjack_routine.json
lumberjack_house.json

####

## 2. Animation beim Bewegen

Nicht perfekt, aber mindestens:

- Wenn Ziel aktiv ist: Walking-/Move-Animation starten
- Wenn Ziel erreicht ist: Idle-/Sleep-/Work-State setzen
- Animation nicht bei jedem Tick neu starten

---

## 3. AvatarPreset laden

Das ist ein wichtiger MVP-A-Plus-Punkt.

Benötigt:

- `setAppearance("Player")`
- JSON aus `AvatarPresets.json` laden
- JSON zu `PlayerSkin` parsen
- Skin dem NPC geben

### Ziel

Bob sieht wie ein echter Hytale-Avatar aus, nicht nur wie ein Platzhaltermodell.

---


## MVP C – NPC Capability System

Ziel:
NPCs bekommen technische Fähigkeiten aus JSON.

Beispiele:
- OPEN_DOORS
- SWIM
- USE_TOOLS
- TRADE
- ATTACK_MELEE
- FOLLOW_ROUTINE

Struktur:
- Template definiert Basisfähigkeiten
- NPC-JSON kann Fähigkeiten hinzufügen
- NPC-JSON kann Fähigkeiten entfernen
- DoorwayFlow prüft OPEN_DOORS
- Navigation prüft SWIM / Movement-Fähigkeiten
- Combat prüft ATTACK-Fähigkeiten

Wichtig:
Role bleibt Beruf/Routine.
Faction bleibt Verhalten gegenüber Spielern.
Capabilities entscheiden, was der NPC technisch darf.



##
MVP D

Hostile NPC Basis:

hostile faction
aggro range
simple attack state
kein door opening

###
MVP E

Combat Profiles:

health
damage
attack range
cooldown
aggro behavior
drops

###
MVP F

Worldgen / Biome NPCs:

forest humans
cave undead
water creatures
hostile camps



