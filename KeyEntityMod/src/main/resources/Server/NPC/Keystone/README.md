# Keystone NPC JSON Structure

Diese README beschreibt die neue Struktur fuer NPC-Konfigurationen im KeystoneNPC-Mod.

## 1) Server/NPC/Roles/
Echte Hytale Engine-Roles.

## 2) Server/NPC/Keystone/
Keystone-Mod-Konfiguration.

## 3) npc/index.json
Laedt NPC-Gruppen.

## 4) npc/<name>/<name>_group.json
Definiert eine NPC-Gruppe mit `shared` und `variants`.

## 5) variants
Konkrete NPC-Definitionen (spawnbare Varianten).

## 6) hytaleRole
Echte Engine-Role aus Server/NPC/Roles/.

## 7) role
Fachliche Keystone-Rolle.

## 8) npcId
Konkrete Instanz in state.json, nicht die Engine-Role.

## 9) appearances/
Mod-Konfiguration, aendert das Engine-Model nicht automatisch.

## 10) Echtes Model aendern
Wenn das sichtbare NPC-Model geaendert werden soll, geschieht das ueber:

Server/NPC/Roles/<RoleName>.json

## 11) skills/
Ersetzt neue skill-Pfade.

## 12) Profilbereiche
- movement/
- navigation/
- combat/
- spawns/
- persistence/
- actions/
- routines/

## 13) Marker-Grundregel
`requiredMarkers` und `markerRoles` muessen zusammenpassen.


Marker-v2 kommt separat spaeter.
