Wichtigkeit 1 — DeathPolicy / NPC-Tod

Das ist nach Marker-v2 und Lifecycle-Sicherheit der nächste große Logikblock.

Offen:

IMMORTAL
RESPAWN
PERMANENT_DEATH
DESPAWN_ONLY
Begleiter-NPCs
angeheuerte NPCs
Hostile NPC Death
Friendly NPC Death

Wichtig dabei:

PERMANENT_DEATH -> Record löschen + eigene Marker löschen
RESPAWN -> Record behalten + Marker behalten
DESPAWN_ONLY -> Entity weg, aber kein Welt-NPC daraus machen
IMMORTAL -> Tod verhindern oder sofort kontrolliert recovern

Das muss später sauber an den zentralen Remove-/Cleanup-Pfad angebunden werden. Im Lagebericht steht bereits, dass permanenter Tod später den zentralen Remove-Pfad nutzen und Marker nur bei echtem Delete löschen soll.

Wichtigkeit 2 — Worldgen / Settlement-Registrierung

Noch offen:

NPCs aus Prefabs / Siedlungen automatisch registrieren
Strukturinstanz-ID speichern
NPCs beim Chunk-/Structure-Load erzeugen
keine unbekannten Live-Entities blind adoptieren

Das ist wichtig für spätere Dörfer, Städte, Biome und hostile Camps.

Regel:

Nicht registrierte NPCs dürfen nicht blind übernommen werden.

Erst braucht es ein sicheres Registrierungssystem:

Structure lädt
geplante NPCs prüfen
npcId erzeugen oder aus Strukturinstanz lesen
state.json registrieren
Entity spawnen
Wichtigkeit 3 — bessere Admin-/Repair-Tools

Auch nach Phase 0–6 bleibt Admin-Komfort offen.

Besonders:

/knpc repair
/knpc diagnose
/knpc marker migrate --dry-run
/knpc marker migrate --apply
/knpc remove --force, falls später sicher geplant
/knpc status mit Live-Entity + state.json Vergleich

Warum wichtig?

Weil du später Fehler nicht manuell in state.json reparieren willst.

Aktuell sind /remove und /clear sicher, aber streng. Später brauchst du entweder einen bestätigten Remove-Outcome oder einen sehr klaren Admin-Force-Pfad.

Wichtigkeit 4 — ServerId / SaveId wirklich fertig machen

Phase 5 plant oder beginnt das Thema.

Danach kann noch offen bleiben:

stabile serverId bestimmen
saveId/worldSaveId bestimmen
alte state.json sicher migrieren
Backup-System
Rollback bei Migration
Diagnose bei falschem State

Nicht blind migrieren.

Ziel bleibt:

state/<serverId>/state.json

oder ähnlich, damit Testserver und echter Server nicht denselben NPC-State teilen. Der Lagebericht nennt genau dieses Risiko als späteren P2-Fix.

Wichtigkeit 5 — Combat / Spawn / Persistence Profile wirklich aktivieren

Im JSON-System sind einige Profile vorbereitet, aber noch nicht voll aktiv:

CombatProfile
SpawnProfile
Appearance
PersistenceProfile
Drops
Faction

Der Lagebericht sagt klar: Diese Profile dürfen nicht so behandelt werden, als würden sie schon vollständig Runtime-Verhalten steuern.

Später heißt das:

Combat wirklich anbinden
Spawn-Regeln wirklich nutzen
Persistence pro NPC-Typ erweitern
Drops sauber steuern
Faction-Logik bauen

Das ist aber Feature-Arbeit, kein akuter Safety-Brand.

Wichtigkeit 6 — Hytale-Engine-Research / API-Cleanup

Noch offen:

Chunk-loaded API sicher prüfen
EntityRef / UUID lookup final prüfen
Pathfinder / Instructions / MotionController besser nutzen
DoorInteraction / InteractionChain sauberer nutzen

Wichtig: keine eigene Fake-Engine bauen.

Der Logic-Report nennt als Risiko: roleName falsch nutzen, Chunk-Status raten, Door/Pathfinder selbst nachbauen, obwohl Hytale Systeme hat.

Wichtigkeit 7 — Navigation / Doorway schöner machen

Nach Phase 6 ist es besser, aber wahrscheinlich noch nicht „final schön“.

Später offen:

NPC läuft natürlicher
Door-Logik wirklich routenbasiert
weniger Transform-Fallback
keine Türen nur wegen Nähe öffnen
bessere Bewegungsprofile

Das ist sichtbar wichtig, aber nicht so gefährlich wie falsche state.json-Daten. Darum kommt es später.

Wichtigkeit 8 — Performance / große Städte

Wenn später viele NPCs kommen:

Tick-Budget
Chunk-basierte Simulation
NPC-Caps
Siedlungs-Caps
keine globalen Scans
keine per-tick Full-Checks

Das wird wichtig für große Städte und viele Settlements.

Wichtigkeit 9 — Appearance / Animation / Drops / Faction

Das sind eher Feature- und Polish-Themen:

Appearance-Apply
Animationen
Sound/Action-Loops
Drops
Faction-Verhalten
Ruf/Reaktion

Nicht unwichtig, aber später. Der Lagebericht nennt diese Sachen ausdrücklich als eher zu vernachlässigen, weil sie weniger gefährlich sind als kaputte Persistenz, falscher Relink oder falsch gespeicherte Marker.

Kurz gesagt

Nach dem Phasenplan bleibt diese Reihenfolge:

1. DeathPolicy / NPC-Tod
2. Worldgen-/Settlement-Registrierung
3. Admin-/Repair-Tools
4. ServerId/SaveId finalisieren
5. Combat/Spawn/Persistence-Profile aktivieren
6. Hytale-API-Research
7. Navigation/Doorway schöner machen
8. Performance für viele NPCs
9. Appearance/Animation/Drops/Faction