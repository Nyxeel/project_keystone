Bericht — Biome-Spawn-Pläne für NPCs ohne Prefab
1. Grundidee

Für NPCs ohne Haus oder Prefab, zum Beispiel feindliche NPCs im Wald, braucht man ein eigenes Biome-Spawn-System.

Die einfache Regel:

Biome entscheidet: Wo darf etwas entstehen?
Spawn-System entscheidet: Wann wird es erzeugt?
MarkerRegistry speichert: echte Weltposition + Typ + Radius
NPC bekommt: markerAssignments -> guard_area
Routine sagt: bewache diesen Bereich

Wichtig:

Der NPC setzt den Marker nicht selbst.
Das Spawn-System setzt den Marker.
Der NPC benutzt ihn nur.
2. Warum nicht der NPC selbst Marker setzen soll

Schlecht wäre:

NPC läuft herum
→ setzt selbst Marker
→ bei Restart/Chunk-Load entstehen doppelte Marker oder falsche Marker

Gefahr:

doppelte NPCs
doppelte Marker
Marker in falscher Welt
falsche Position nach Restart
kaputte state.json

Besser:

Chunk wird geladen
→ Spawn-System prüft Biom
→ Spawn-System erzeugt Marker
→ NPC bekommt diesen Marker
3. Beispiel: Bandit bewacht Waldgebiet

Ein hostile NPC wie ein Bandit braucht keinen Bett-/Hausmarker.

Er braucht eher:

guard_area

Dieser Marker bedeutet:

Hier ist das Gebiet, das der NPC bewacht.

Dazu passt ein MarkerType wie:

GUARD

Der Marker hätte später ungefähr diese Daten:

markerId
markerType = GUARD
worldId
position
radius = 24

Der NPC bekommt dann:

markerAssignments:
  guard_area -> GUARD-Marker

Die Routine sagt dann:

Bewache guard_area.
Greife Spieler im Radius an.
Kehre zurück, wenn du zu weit weg bist.
4. Mögliche neue MarkerTypes

Für Biome-/Hostile-Spawns wären sinnvoll:

GUARD
PATROL
AMBUSH
NEST
CAMP

Bedeutung:

MarkerType	Zweck
GUARD	NPC bewacht Radius
PATROL	NPC läuft Route oder Gebiet ab
AMBUSH	NPC wartet auf Spieler
NEST	Monster-/Tierbau
CAMP	kleines Lager ohne festes Prefab
5. Biome-Spawn-Ablauf

Wenn ein Chunk geladen wird:

1. Chunk wird geladen.
2. System prüft: Welches Biom ist hier?
3. System prüft: Darf dieser NPC in diesem Biom entstehen?
4. System prüft: Gibt es hier schon einen passenden Marker/NPC?
5. Wenn nein: sichere Position suchen.
6. MarkerRecord erzeugen.
7. NPC-Record erzeugen.
8. markerAssignments setzen.
9. NPC spawnen.

Beispiel:

dark_forest
→ Bandit erlaubt
→ guard_area Marker erzeugen
→ Bandit bekommt diesen Marker
→ Bandit bewacht Radius 24
6. Wichtige Safety-Regeln

Das System darf nicht blind spawnen.

Vor jedem Spawn muss geprüft werden:

Ist der Chunk wirklich geladen?
Ist das Biom erlaubt?
Gibt es schon einen NPC/Marker dort?
Ist die Position sicher?
Ist worldId korrekt?
Ist markerType korrekt?
Kann state.json sicher gespeichert werden?

Wenn etwas unklar ist:

nicht spawnen
nicht relinken
nicht überschreiben
nur loggen/warnen
7. Wo gehört das im Projekt hin?

Nicht direkt in Marker-v2 Step 2.x.

Das ist ein späterer eigener Feature-Block, zum Beispiel:

BiomeEncounterSpawnService
BiomeMarkerPlacementService

Passende JSON-/Profilbereiche:

Server/NPC/Keystone/spawns/
Server/NPC/Keystone/combat/
Server/NPC/Keystone/routines/

Marker-v2 liefert nur die Grundlage:

markerAssignments kann guard_area speichern.

Das Biome-Spawn-System entscheidet später:

Wann und wo wird guard_area erzeugt?
8. Abgrenzung zu Prefabs

Mit Prefab:

Prefab enthält relative Marker.
Haus wird platziert.
Marker werden aus Prefab-Position berechnet.

Ohne Prefab:

Biome/Spawn-System sucht Weltposition.
Marker wird direkt im Biom erzeugt.
NPC bekommt diesen Marker.

Kurz:

Prefab = Marker kommt aus Struktur.
Biome-Spawn = Marker kommt aus Welt-/Biome-Regel.
9. Spätere Agent-Phase

Dieser Feature-Block sollte später kommen:

Phase X — Biome Marker & Encounter Spawn

Nicht jetzt in Marker-v2 mischen.

Vorher müssen fertig sein:

[ ] Marker-v2 Modell
[ ] Read-only Resolver
[ ] Commands schreiben markerAssignments
[ ] Remove/Clear kennt markerAssignments
[ ] Migration ist explizit und sicher
[ ] Safety-Doku aktuell
10. Kurzfazit

Die Idee ist gut und logisch:

Hostile NPCs brauchen keine Häuser.
Sie brauchen Gebiet-Marker.
Diese Marker entstehen durch Biome-/Spawn-Regeln.
NPCs benutzen diese Marker über markerAssignments.

Wichtigster Satz:

Die Welt setzt den Marker.
Der NPC benutzt den Marker.
Der NPC erzeugt den Marker nicht selbst.