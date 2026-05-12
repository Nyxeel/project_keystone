# KeystoneNPC - Hytale NPC Mod

KeystoneNPC ist eine server-first Hytale-NPC-Mod. Der aktuelle Fokus liegt auf einem Lumberjack-NPC mit manuell gesetzten Markern.

Der NPC kann gespawnt und gespeichert werden, nutzt eine Rolle aus Server/NPC/Roles, bewegt sich ueber die Hytale Engine-Navigation (Leash/Seek) und besitzt bereits einen eigenen Doorway-Bereich fuer Tuerlogik. Save/Load ueber JSON-State ist vorhanden.

Das Projekt ist damit nicht mehr nur ein Skeleton, sondern ein funktionierender Uebergang zwischen MVP A und MVP B.

## Aktueller Funktionsstand

- Plugin-Entrypoint: keystone.npc.KeystoneNpcPlugin
- Manifest: src/main/resources/manifest.json
- ServerVersion wird aus manifest.json verwendet (aktueller Eintrag dort ist die Quelle)
- Rolle im Fokus: lumberjack
- NPC-Template: Template_Lumberjack (Datei: src/main/resources/Server/NPC/Roles/lumberjack.template.json)
- Marker-Typen im Lumberjack-MVP-Flow: bed, door, work
- Commands (Kernpfad):
	- /knpc marker set <bed|door|work>
	- /knpc marker clear
	- /knpc spawn <role> <name>
	- /knpc list
	- /knpc remove <index>
	- /knpc clear
	- /knpc status
- Persistenz:
	- Marker werden gespeichert
	- NPC-Daten werden gespeichert
	- aktive Marker-IDs werden gespeichert
- Recovery/Relink:
	- NPCs koennen nach Restart wiederhergestellt oder relinkt werden
- Doorway-Bausteine:
	- DoorwayScanner
	- DoorwayFlow
	- DoorPassTracker
	- ActiveDoorPass
	- PendingDoorAttempt

Hinweis fuer den Ist-Stand: Die MarkerType-Enum enthaelt zusaetzlich chest, food und chill. Fuer den aktuellen Lumberjack-MVP sind aber bed, door und work der relevante Standardfluss.

## Aktueller MVP-Stand

MVP A ist groesstenteils umgesetzt:
- Lumberjack-Rolle
- bed/door/work Marker
- Spawn-Command
- Save/Load
- einfache Tag/Nacht-Routine
- Engine-basierte Bewegung
- erste Door-Integration

MVP B ist im Uebergang:
- bessere Routine-Struktur
- mehrere Tagesstationen spaeter
- bessere Door-Navigation
- Animationen spaeter
- Collision/Yield spaeter

## Grobe Architektur

- commands/ = alle /knpc Commands
- markers/ = Marker-Daten und aktive Marker
- roles/ = Rollen aus JSON laden
- routine/ = NPC-Tick, Tageslogik und Zielwechsel
- navigation/ = Uebergabe an Engine-Bewegung
- doorway/ = Tuer-Erkennung und Tuer-Durchgang
- persistence/ = Speichern und Laden
- relink/ und recovery/ = NPC nach Restart wiederfinden oder bei Bedarf kontrolliert neu erzeugen

## Bekannte Einschraenkungen

- Im MVP-Standard sind aktuell bed/door/work voll durchgaengig modelliert
- noch keine echte mehrstufige Routine mit chest/food/chill
- noch keine echte Action-/Animation-Ausfuehrung
- Door-System existiert, aber perfektes Path-Routing durch Tueren ist noch nicht final
- Walking-Animation und Laufgeschwindigkeit muessen weiter verbessert werden
- Collision/Yield zwischen mehreren NPCs ist noch offen
- AvatarPreset/PlayerSkin-System ist noch nicht final integriert

## Naechste Schritte

1. Doku aktuell halten
2. Routine-System erweitern
3. Action-/Animationssystem anbinden
4. Door-Routing verbessern
5. Collision/Yield loesen
