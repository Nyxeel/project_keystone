
Zentrale JSON fuer verschieden NPC Typen:
{
  "id": "lumberjack",
  "version": 1,

  "type": "concrete",
  "template": "templates/template_lumberjack.json",

  "displayName": "Holzfäller",
  "nameTranslationKey": "server.npcRoles.Lumberjack.name",

  "npcType": "human",
  "faction": "friendly",
  "role": "lumberjack",

  "appearance": {
    "profile": "appearances/lumberjack_default.json",
    "value": "Temple_Mithril_Guard"
  },

  "stats": {
    "maxHealth": 100
  },

  "drops": {
    "dropList": "Empty",
    "profile": "drops/empty.json"
  },

  "attitude": {
    "defaultPlayerAttitude": "Neutral",
    "defaultNPCAttitude": "Ignore"
  },

  "profiles": {
    "routine": "routines/lumberjack_day_cycle.json",
    "capabilities": "skills/human_worker.json",
    "actions": "actions/lumberjack_actions.json",
    "movement": "movement/human_walk.json",
    "combat": "combat/peaceful.json",
    "spawn": "spawns/forest_village.json",
    "structure": "structures/lumberjack_house.json",
    "persistence": "persistence/persistent_citizen.json"
  },

  "requiredMarkers": [
    "bed",
    "door",
    "chest",
    "food",
    "work",
    "chill"
  ],

  "markerRoles": {
    "bed": "BED",
    "door": "DOOR",
    "chest": "CHEST",
    "food": "FOOD",
    "work": "WORK",
    "chill": "CHILL"
  },

  "motionControllerList": [
    {
      "type": "Walk",
      "maxWalkSpeed": 2,
      "gravity": 10,
      "maxFallSpeed": 8,
      "acceleration": 10
    }
  ],

  "instructions": [
    {
      "sensor": {
        "type": "Leash",
        "range": 0.5
      },
      "bodyMotion": {
        "type": "Seek",
        "reachable": false,
        "relativeSpeed": 1.0,
        "slowDownDistance": 1.5,
        "stopDistance": 0.5,
        "switchToSteeringDistance": 2.0,
        "useSteering": true,
        "usePathfinder": true
      }
    },
    {
      "sensor": {
        "type": "Any"
      },
      "bodyMotion": {
        "type": "Nothing"
      }
    }
  ],

  "defaultState": "IDLE",

  "debug": {
    "showMarkers": false,
    "logRoutineChanges": true,
    "logCapabilityChecks": false,
    "logMotionChanges": true
  }
}

id: "lumberjack" wird fuer spawn <role> verwendet also /knpc spawn lumberjack <name>




Die zentrale JSON ist der Haupt-Bauplan für einen NPC.

Sie soll nicht jede einzelne Logik selbst enthalten, sondern viele andere JSON-Dateien miteinander verbinden. Man kann sie sich vorstellen wie eine Steckzentrale:

lumberjack_friendly.json
= Wer ist dieser NPC?
= Welche Profile benutzt er?
= Welche Marker braucht er?
= Wie bewegt er sich?
= Welche Standardwerte hat er?

Diese Datei ist also der Einstiegspunkt, wenn der Code später einen NPC laden möchte.

1. Grundidee der zentralen JSON

Die Datei beschreibt einen konkreten NPC-Typ:

"id": "lumberjack_friendly",
"type": "concrete",
"template": "templates/template_lumberjack.json"

Das bedeutet:

id
= eindeutiger Name dieses NPC-Bauplans

type: concrete
= dieser NPC kann wirklich gespawnt werden

template
= Basis-Vorlage, von der dieser NPC Werte übernimmt

Der Unterschied ist wichtig:

template_lumberjack.json
= allgemeine Vorlage

lumberjack_friendly.json
= echter NPC-Bauplan

Die Vorlage kann gemeinsame Werte speichern, die mehrere Lumberjack-NPCs benutzen. Die zentrale JSON kann diese Werte übernehmen oder überschreiben.

2. Menschlich lesbare NPC-Daten
"displayName": "Bob der Holzfäller",
"nameTranslationKey": "server.npcRoles.Lumberjack.name"

Hier geht es um den Namen.

displayName
= direkter sichtbarer Name, gut für Tests

nameTranslationKey
= Übersetzungsschlüssel für spätere Sprachdateien

Warum beides?

Für MVPs ist displayName praktisch, weil du sofort siehst:

Bob der Holzfäller

Später ist nameTranslationKey besser, weil du damit Übersetzungen machen kannst:

Deutsch: Holzfäller
Englisch: Lumberjack
3. NPC-Kategorie
"npcType": "human",
"faction": "friendly",
"role": "lumberjack"

Diese drei Felder sind bewusst getrennt.

npcType

Sagt, was der NPC biologisch oder technisch ist.

Beispiele:

human
undead
wolf
fish_creature
golem

Ein human kann später andere Basiswerte haben als ein undead.

faction

Sagt, wie der NPC grundsätzlich gegenüber Spielern oder Gruppen eingestellt ist.

Beispiele:

friendly
neutral
hostile

Friendly heißt nicht automatisch, dass der NPC nichts kann.
Hostile heißt nicht automatisch, dass er keine Routine haben darf.

Darum ist faction nur die soziale Seite.

role

Sagt, welche Aufgabe der NPC hat.

Beispiele:

lumberjack
guard
merchant
miner
raider
villager

Wichtig:

role = Beruf / Aufgabe
faction = Freundlich oder feindlich
npcType = Wesenstyp

Ein human kann also friendly oder hostile sein.
Ein guard kann friendly oder hostile sein.
Ein undead kann auch eine Routine haben.

4. Appearance / Aussehen
"appearance": {
  "profile": "appearances/lumberjack_default.json",
  "value": "Temple_Mithril_Guard"
}

Hier wird geplant, wie der NPC aussieht.

profile
= verweist auf eine extra Appearance-Datei

value
= aktueller Hytale-Appearance-Wert

Temple_Mithril_Guard ist hier der konkrete Wert, den Hytale aktuell als Modell/Aussehen laden kann.

Langfristig kann appearances/lumberjack_default.json mehr enthalten:

Model
AvatarPreset
Kleidung
Haut
Haare
Animation Set
Kosmetik

Die zentrale JSON muss dann nicht riesig werden.

5. Stats / Werte
"stats": {
  "maxHealth": 100
}

Hier stehen einfache Statuswerte.

Aktuell:

maxHealth = maximale Lebenspunkte

Später könnten dort auch stehen:

armor
movementSpeedBonus
knockbackResistance
stamina
mana

Aber für den Anfang reicht maxHealth.

Wichtig: Diese Werte können später auch ausgelagert werden:

stats/human_common.json
stats/undead_basic.json
stats/guard_tank.json
6. Drops
"drops": {
  "dropList": "Empty",
  "profile": "drops/empty.json"
}

Hier wird geplant, was der NPC fallen lässt, wenn er stirbt.

dropList: Empty
= aktuell keine Drops

profile
= extra Datei für Drop-Regeln

Für friendly NPCs ist Empty sinnvoll, weil man nicht will, dass Spieler Stadt-NPCs farmen.

Später für hostile NPCs:

drops/undead_basic.json
drops/bandit_common.json
drops/boss_raider.json

Dort könnten dann Items, Drop-Chancen und Mengen stehen.

7. Attitude / Grundverhalten gegenüber anderen
"attitude": {
  "defaultPlayerAttitude": "Neutral",
  "defaultNPCAttitude": "Ignore"
}

Das ist nicht dasselbe wie faction.

faction ist die grobe Gruppenzugehörigkeit:

friendly
hostile
neutral

attitude ist das konkrete Standardverhalten.

defaultPlayerAttitude: Neutral
= gegenüber Spielern erstmal neutral

defaultNPCAttitude: Ignore
= andere NPCs werden ignoriert

Das ist wichtig, weil ein friendly NPC nicht automatisch jedem Spieler folgen, handeln oder helfen muss.

Später könnte ein Guard so aussehen:

defaultPlayerAttitude: Friendly
defaultHostileAttitude: Attack
defaultNPCAttitude: Ignore

Oder ein Untoter:

defaultPlayerAttitude: Hostile
defaultNPCAttitude: Ignore
8. Profiles: Die wichtigste Verknüpfungsstelle
"profiles": {
  "routine": "routines/lumberjack_day_cycle.json",
  "capabilities": "skills/human_worker.json",
  "actions": "actions/lumberjack_actions.json",
  "movement": "movement/human_walk.json",
  "combat": "combat/peaceful.json",
  "spawn": "spawns/forest_village.json",
  "structure": "structures/lumberjack_house.json",
  "persistence": "persistence/persistent_citizen.json"
}

Das ist der wichtigste Teil der zentralen JSON.

Hier steht:

Welche anderen JSON-Dateien gehören zu diesem NPC?

Die zentrale JSON bleibt dadurch sauber.

routine
"routine": "routines/lumberjack_day_cycle.json"

Diese Datei beschreibt den Tagesablauf.

Beispiel:

06:00 → Truhe
06:30 → Essen
07:30 → Arbeiten
12:00 → Essen
13:00 → Arbeiten
18:00 → Chillen
21:00 → Schlafen

Die Routine sagt also:

Wann soll der NPC wohin gehen?
Welchen Zustand soll er dort haben?
Welche Action soll dort starten?

Wichtig: Die Routine sollte keine echten Marker-IDs enthalten, sondern nur logische Namen:

bed
door
work
food
chill

So kann dieselbe Routine für mehrere Lumberjacks benutzt werden.

capabilities
"capabilities": "skills/human_worker.json"

Diese Datei sagt, was der NPC darf.

Beispiele:

OPEN_DOORS: true
SWIM: false
USE_TOOLS: true
USE_CHEST: true
USE_BED: true
TRADE: true
ATTACK_MELEE: false

Das ist extrem wichtig für spätere friendly und hostile NPCs.

Der Code soll später nicht fragen:

Ist das ein Lumberjack?

Sondern:

Hat dieser NPC OPEN_DOORS?
Hat dieser NPC SWIM?
Hat dieser NPC ATTACK_MELEE?

Das macht das System viel flexibler.

Beispiel:

Human Lumberjack
→ darf Türen öffnen

Undead Raider
→ darf keine Türen öffnen

Water Creature
→ darf schwimmen

Guard
→ darf angreifen
actions
"actions": "actions/lumberjack_actions.json"

Diese Datei beschreibt, was bei bestimmten Aktionen passiert.

Beispiele:

chop_wood
open_chest
eat
sleep
idle_relax

Eine Action kann später enthalten:

Animation
Soundeffekt
Loop ja/nein
Sound-Intervall
benötigte Capability

Beispiel:

chop_wood
= Holz hacken Animation
= Axt-Sound alle 1.8 Sekunden
= benötigt USE_TOOLS

Wichtig:

Routine sagt: Gehe zu work und mache chop_wood.
Action sagt: chop_wood benutzt Animation und Sound.

Dadurch bleibt Routine sauber.

movement
"movement": "movement/human_walk.json"

Diese Datei ist für Bewegung.

Dort können später stehen:

Walk Animation
Run Animation
Swim Animation
Footstep Sounds
Surface-based Schritte
Bewegungsgeschwindigkeit

Schritte gehören nicht in die Routine.

Warum?

Ein NPC läuft bei vielen Dingen:

zur Arbeit
zum Bett
zur Truhe
auf Patrouille
bei Flucht
beim Angriff

Darum ist Bewegung ein eigenes Profil.

combat
"combat": "combat/peaceful.json"

Diese Datei beschreibt Kampfverhalten.

Für Bob:

peaceful
= kein Angriff
= vielleicht fliehen
= keine Aggro

Für hostile NPCs später:

combat/weak_melee.json
combat/undead_guard.json
combat/bandit_archer.json

Dort könnten stehen:

damage
attackRange
attackCooldown
aggroRange
leashRange
targetRules

Für MVP muss das noch nicht komplett rein. Aber die zentrale JSON ist schon vorbereitet.

spawn
"spawn": "spawns/forest_village.json"

Diese Datei beschreibt, wo dieser NPC vorkommen darf.

Für friendly NPCs:

forest_village
lumberjack_house
settlement

Für hostile NPCs später:

cursed_forest
undead_ruin
cave_depth_2
swamp

Wichtig: Biome-Vorkommen sollte eher im Spawn-Profil liegen, nicht direkt hart im NPC selbst.

Warum?

Ein NPC ist nur der Bauplan.
Das Spawn-Profil sagt, wo dieser Bauplan verwendet wird.

structure
"structure": "structures/lumberjack_house.json"

Diese Datei beschreibt, zu welchem Prefab oder Haus der NPC gehört.

Zum Beispiel:

lumberjack_house
braucht bed
braucht door
braucht chest
braucht food
braucht work
braucht chill

Später kann ein Prefab sagen:

Dieses Haus erzeugt einen Lumberjack.
Dieses Haus hat Marker für Bett, Tür, Arbeitspunkt usw.

Das ist wichtig, weil NPCs an Prefabs gebunden sein sollen.

Beispiel:

Lumberjack → an Holzfällerhaus gebunden
Guard → an Burg / Stadtmauer gebunden
Zombie → an Ruine oder Biome-Zone gebunden
Wolf → an Biome gebunden
persistence
"persistence": "persistence/persistent_citizen.json"

Diese Datei sagt, ob und wie der NPC gespeichert wird.

Für Bob:

persistent
= ja, er soll nach Server-Restart wieder existieren

Dort könnte später stehen:

savePosition
saveState
saveHome
saveRoutineProgress
respawnAfterRestart
despawnWhenFarAway

Für friendly Stadt-NPCs ist Persistenz wichtig.

Für random hostile Biome-Mobs vielleicht nicht.

9. Required Markers
"requiredMarkers": [
  "bed",
  "door",
  "chest",
  "food",
  "work",
  "chill"
]

Das ist eine Liste aller logischen Marker, die dieser NPC braucht.

Bob braucht:

bed
= Schlafplatz

door
= Eingang / Ausgang

chest
= Truhe

food
= Essplatz

work
= Arbeitsplatz

chill
= Freizeitpunkt

Diese Marker sind noch keine echten Weltkoordinaten.

Sie sind logische Namen.

Später wird daraus:

bed → konkrete Position im Haus
door → konkrete Türposition
work → konkreter Arbeitsplatz

Warum ist das gut?

Weil mehrere Lumberjacks dieselbe JSON benutzen können, aber jeder seine eigenen Marker bekommt.

10. Marker Roles
"markerRoles": {
  "bed": "BED",
  "door": "DOOR",
  "chest": "CHEST",
  "food": "FOOD",
  "work": "WORK",
  "chill": "CHILL"
}

Hier wird jedem Marker eine technische Rolle gegeben.

bed → BED
door → DOOR
chest → CHEST
food → FOOD
work → WORK
chill → CHILL

Das hilft dem Code.

Die Routine sagt vielleicht:

targetMarker: "bed"

Der Code kann dann erkennen:

bed hat Rolle BED
→ dort kann SLEEPING passieren

Oder:

door hat Rolle DOOR
→ vor dem Betreten prüfen: OPEN_DOORS?

Das ist besonders wichtig für Capabilities.

Beispiel:

Human hat OPEN_DOORS
→ darf door benutzen

Undead hat kein OPEN_DOORS
→ darf door nicht normal benutzen
11. Motion Controller List
"motionControllerList": [
  {
    "type": "Walk",
    "maxWalkSpeed": 2,
    "gravity": 10,
    "maxFallSpeed": 8,
    "acceleration": 10
  }
]

Das ist sehr wichtig, weil es aktuell aus deiner template_lumberjack.json kommt.

Dieser Bereich beschreibt, wie sich die Entity physisch bewegen kann.

type: Walk
= NPC benutzt Laufbewegung

maxWalkSpeed: 2
= maximale Gehgeschwindigkeit

gravity: 10
= Schwerkraftwert

maxFallSpeed: 8
= maximale Fallgeschwindigkeit

acceleration: 10
= Beschleunigung

Das ist die technische Bewegungsschicht.

Die Routine sagt nur:

Gehe zu work.

Aber der Motion Controller sagt:

Wie kann die Entity laufen?
Wie schnell?
Mit welcher Gravitation?

Ohne diese Daten weiß der NPC zwar sein Ziel, aber nicht sauber, wie er sich bewegen soll.

12. Instructions
"instructions": [
  {
    "sensor": {
      "type": "Leash",
      "range": 0.5
    },
    "bodyMotion": {
      "type": "Seek",
      "reachable": false,
      "relativeSpeed": 1.0,
      "slowDownDistance": 1.5,
      "stopDistance": 0.5,
      "switchToSteeringDistance": 2.0,
      "useSteering": true,
      "usePathfinder": true
    }
  },
  {
    "sensor": {
      "type": "Any"
    },
    "bodyMotion": {
      "type": "Nothing"
    }
  }
]

Das ist die Engine-Bewegungslogik.

Einfach gesagt:

Wenn Ziel in Reichweite / Leash aktiv:
→ bewege dich mit Seek zum Ziel

Sonst:
→ mache nichts
Sensor
"sensor": {
  "type": "Leash",
  "range": 0.5
}

Der Sensor beschreibt, wann diese Bewegungsregel aktiv ist.

Leash
= der NPC hat ein Ziel / eine Bindung / einen Bewegungsanker

range: 0.5
= sehr nah am Ziel gilt als angekommen
BodyMotion
"bodyMotion": {
  "type": "Seek",
  "reachable": false,
  "relativeSpeed": 1.0,
  "slowDownDistance": 1.5,
  "stopDistance": 0.5,
  "switchToSteeringDistance": 2.0,
  "useSteering": true,
  "usePathfinder": true
}

Das beschreibt die eigentliche Bewegung.

type: Seek
= suche/bewege dich zum Ziel

relativeSpeed: 1.0
= normale Geschwindigkeit

slowDownDistance: 1.5
= kurz vor Ziel langsamer werden

stopDistance: 0.5
= bei 0.5 Abstand stoppen

switchToSteeringDistance: 2.0
= nahe am Ziel eher Steering benutzen

useSteering: true
= lokale Bewegung/Lenkung verwenden

usePathfinder: true
= Pfadfindung benutzen

Das ist wichtig für dein Problem mit:

NPC läuft durch Wände
NPC schwebt
NPC bewegt sich falsch zwischen Höhen

Mit usePathfinder: true planst du, dass Hytales Pfadsystem benutzt wird, statt nur stumpf Position A zu Position B zu bewegen.

Fallback Instruction
{
  "sensor": {
    "type": "Any"
  },
  "bodyMotion": {
    "type": "Nothing"
  }
}

Das heißt:

Wenn keine andere Bewegungsregel passt:
→ nichts machen

Das ist ein sicherer Fallback.

Der NPC soll nicht dauerhaft irgendeine Bewegung erzwingen.

13. Default State
"defaultState": "IDLE"

Das ist der Startzustand.

Wenn der NPC gespawnt wird und noch keine Routine aktiv ist, startet er mit:

IDLE

Also:

steht herum
macht nichts Besonderes
wartet auf Routine / Ziel / Event

Später kann der Routine-Controller dann sagen:

Es ist 07:30
→ nächster Zustand WORKING
→ Ziel work
→ Action chop_wood
14. Debug-Bereich
"debug": {
  "showMarkers": false,
  "logRoutineChanges": true,
  "logCapabilityChecks": false,
  "logMotionChanges": true
}

Das ist nur für Entwicklung.

showMarkers
= Marker sichtbar machen oder nicht

logRoutineChanges
= ausgeben, wenn NPC Routine wechselt

logCapabilityChecks
= ausgeben, wenn geprüft wird, ob NPC etwas darf

logMotionChanges
= ausgeben, wenn Bewegungsmodus/Ziel wechselt

Das ist extrem hilfreich, solange du testest.

Beispiel-Logs:

Bob routine changed: IDLE → WALKING_TO_WORK
Bob reached marker: work
Bob action started: chop_wood
Bob motion changed: Seek → Nothing

Für finale Server kann man diese Logs deaktivieren.

15. Warum diese Struktur gut ist

Diese JSON trennt sauber verschiedene Fragen:

Wer ist der NPC?
→ id, displayName, npcType, faction, role

Wie sieht er aus?
→ appearance

Was kann er?
→ capabilities profile

Was macht er wann?
→ routine profile

Was passiert bei Aktionen?
→ actions profile

Wie bewegt er sich?
→ movement, motionControllerList, instructions

Wo spawnt er?
→ spawn profile

Zu welcher Struktur gehört er?
→ structure profile

Wird er gespeichert?
→ persistence profile

Das verhindert Chaos.

16. Warum nicht alles in eine riesige JSON?

Man könnte alles direkt in lumberjack_friendly.json schreiben.

Aber das wird später schlecht.

Warum?

Weil du dann für jeden NPC alles doppelt hast:

Lumberjack hat human movement
Guard hat human movement
Merchant hat human movement
Farmer hat human movement

Wenn du dann human_walk ändern willst, musst du 20 Dateien ändern.

Besser:

Alle Menschen benutzen movement/human_walk.json

Dann änderst du es einmal.

17. Template + Profile + Override Gedanke

Die zentrale JSON kann später so funktionieren:

1. Lade template_lumberjack.json
2. Lade lumberjack_friendly.json
3. Lade alle Profile
4. Baue daraus eine fertige EffectiveNpcDefinition

Diese fertige Definition enthält dann alles, was der Code braucht.

Beispiel:

EffectiveNpcDefinition
= template Werte
+ zentrale NPC Werte
+ capabilities
+ routine
+ movement
+ appearance
+ combat
+ spawn
+ persistence

Das ist wichtig, weil der Runtime-Code nicht ständig JSON-Dateien durchsuchen soll.

Besser:

Beim Start laden und zusammenbauen.
Danach schnelle Java-Objekte benutzen.
18. Friendly NPC Beispiel: Was Bob dadurch kann

Mit dieser JSON ist Bob:

ein konkreter NPC
ein Mensch
friendly
Holzfäller
neutral gegenüber Spielern
ignoriert andere NPCs
hat 100 Leben
hat keine Drops
benutzt Temple_Mithril_Guard als Appearance
hat eine Tagesroutine
hat Human-Worker-Fähigkeiten
hat Lumberjack-Actions
benutzt Human-Walk-Bewegung
spawnt in Forest Villages
gehört zu einem Lumberjack House
wird persistent gespeichert
startet im Zustand IDLE

Das ist schon sehr viel Information, aber sauber getrennt.

19. Wie das später im Code ablaufen soll

Einfacher Ablauf:

1. NPC-Definition laden
2. Template laden
3. Profile laden
4. Required Markers prüfen
5. NPC spawnen
6. Appearance setzen
7. Stats setzen
8. Motion Controller setzen
9. Instructions setzen
10. Routine starten
11. Capabilities bei Bedarf prüfen
12. State und Position speichern

Beispiel:

Routine will zur Tür
→ Code prüft targetMarker = door
→ markerRole = DOOR
→ Capability OPEN_DOORS prüfen
→ wenn true: DoorwayFlow starten
→ wenn false: anderes Verhalten

Oder:

Routine will work ausführen
→ targetMarker = work
→ action = chop_wood
→ Action-Profil laden
→ prüfe USE_TOOLS
→ Animation starten
→ Sound alle X Sekunden spielen
20. Wichtig für spätere hostile NPCs

Diese Struktur ist nicht nur für Bob.

Sie ist schon für hostile NPCs vorbereitet.

Ein hostile NPC könnte dieselbe Grundstruktur haben:

npcType: undead
faction: hostile
role: castle_guard
capabilities: undead_guard
routine: undead_guard_patrol
combat: undead_melee
spawn: cursed_castle
structure: undead_ruin

Und dann:

OPEN_DOORS: false
SWIM: false
ATTACK_MELEE: true
FOLLOW_ROUTINE: true

Dadurch kann ein Untoter eine Routine haben, aber trotzdem keine Türen öffnen.

Das ist genau das Ziel.

21. Wichtig für Biome und Prefabs

Du hast geplant:

NPCs können an Prefabs gebunden sein
oder an Biome gebunden sein

Diese zentrale JSON unterstützt beides.

Prefab-NPC
structure: structures/lumberjack_house.json
spawn: spawns/forest_village.json

Beispiel:

Bob gehört zu einem Holzfällerhaus.
Seine Marker kommen aus diesem Haus.
Biome-NPC
structure: null oder eigene Spawn-Zone
spawn: spawns/cursed_forest.json

Beispiel:

Untoter läuft im ganzen verfluchten Wald herum.
Er spawnt aber nicht in freundlichen Dörfern.

Das Spawn-Profil kann später blockierte Struktur-Tags enthalten:

blockedStructureTags:
- friendly_village
- human_house
22. Wichtig für Animationen und Sounds

Animationen und Sounds gehören nicht direkt in die zentrale JSON.

Die zentrale JSON verweist nur hierhin:

"actions": "actions/lumberjack_actions.json",
"movement": "movement/human_walk.json"

Warum?

Weil Animationen und Sounds zu Aktionen und Bewegung gehören.

Beispiele:

chop_wood
→ Hack-Animation
→ Holz-Hack-Sound

open_chest
→ Truhe-Öffnen-Animation
→ Chest-Sound

walk
→ Laufanimation
→ Schrittgeräusche

swim
→ Schwimmanimation
→ Wassergeräusche

Das ist sauberer als alles direkt in routine zu schreiben.

23. Wichtig für Performance

Die JSON ist nur Planung/Lade-Daten.

Zur Laufzeit sollte nicht ständig JSON gelesen werden.

Besser:

Serverstart:
JSON laden

NPC Spawn:
fertige Definition benutzen

Runtime:
nur Java-Objekte / Komponenten benutzen

Animationen und Sounds sollten eventbasiert sein:

NPC erreicht work
→ starte chop_wood einmal
→ Animation loopt
→ Sound über Intervall

NPC verlässt work
→ stoppe chop_wood

Nicht:

jeden Tick JSON lesen
jeden Tick Animation neu starten
jeden Tick Sound neu spielen

Das wäre schlecht.

24. Kurze Gesamtformel

Diese zentrale JSON ist:

NPC-Bauplan + Profil-Verknüpfung + Engine-Basisdaten

Sie enthält:

Identität
Typ
Fraktion
Rolle
Aussehen
Leben
Drops
Attitude
Profil-Verweise
benötigte Marker
Marker-Rollen
Motion Controller
Engine Instructions
Startzustand
Debug-Einstellungen

Sie enthält bewusst nicht komplett:

ganze Routine
alle Capabilities
alle Sounds
alle Animationen
alle Spawn-Regeln
alle Combat-Regeln
alle Persistence-Regeln

Diese Dinge werden ausgelagert.

25. Meine empfohlene Regel für dich

Alles, was ein NPC eindeutig ist, kommt in die zentrale JSON:

id
npcType
faction
role
displayName
template
profile references
requiredMarkers

Alles, was wiederverwendbar ist, kommt in eigene Profile:

routine
capabilities
actions
movement
combat
spawn
structure
persistence
appearance
drops

Alles, was Hytale-Engine-Bewegung direkt betrifft, bleibt erstmal sichtbar in der zentralen JSON oder wird später in movement ausgelagert:

motionControllerList
instructions
usePathfinder
useSteering
stopDistance
slowDownDistance

So vergisst du keine wichtigen Werte aus der alten template_lumberjack.json, aber bekommst trotzdem eine saubere Struktur für spätere MVPs.