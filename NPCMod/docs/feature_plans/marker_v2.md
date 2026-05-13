Neuer Feature-Plan: Marker-v2 sicher umsetzen
Grundentscheidung
Marker-v2 wird NICHT direkt gebaut.Erst P0-Safety:1. Save/Load sicher machen2. MarkerSet/MarkerClear transaktional machen3. Resolver read-only machen4. Spawn/Respawn-Rollback auf Marker erweitern5. Same-position Navigation fixenErst danach:Marker-v2 mit markerAssignments bauen.

Phase 0 — Audit: aktuellen Marker-Code prüfen
Ziel: Nur prüfen, nichts ändern.
Prüfen:
MarkerResolverMarkerRegistryMarkerSetCommandMarkerClearCommandSpawnNpcCommandNpcClearCommandNpcRemoveCommandNpcRespawnMissingCommandJsonFileStateStoreActiveMarkerIdMapperNpcRecordStateTargetingServiceNpcTickPipelineRespawnRecoveryServiceRelinkWorkflowServiceIdleMarkerService
Besonders suchen nach:
resolveRequiredMarkerWithFallback(...)setMarkerIdForType(...)markerRegistry.clear()markerRegistry.clearActive()MarkerType.valueOf(...)bedMarkerId / foodMarkerId / workMarkerId ...activeMarkerIdslastByTypeWALKING_TO_*
Ergebnis dieses Steps:
- Liste aller mutierenden Read-Pfade- Liste aller Commands ohne Rollback- Liste aller Save/Load-Risiken- Liste aller Marker-Fallbacks- Liste aller Legacy-MVP-A-Stellen

Phase 1 — Save/Load-Safety vor Marker-v2
Ziel
Ein kaputter Marker darf niemals den ganzen State zerstören.
Aktuelles Risiko:
1 kaputter MarkerType→ Load Exception→ PluginState.empty()→ später Save→ state.json leer
Neue Regel:
State-Load darf nie komplett leer zurückgeben,nur weil ein einzelner Marker kaputt ist.
Umsetzen:
- Marker einzeln laden- kaputte Marker überspringen/quarantänen- NPCs einzeln laden- kaputte NPCs überspringen/quarantänen- kein kompletter PluginState.empty() bei Teilfehler- state.json nicht sofort überschreiben nach fehlerhaftem Load- Save atomar machen:  temp file schreiben  dann ersetzen
Review-Fragen:
[ ] Ein kaputter Marker zerstört nicht alle NPCs?[ ] Ein kaputter NPC zerstört nicht alle Marker?[ ] Save ist atomar?[ ] Kein leerer State wird nach Load-Fehler automatisch gespeichert?[ ] Compile grün?

Phase 2 — MarkerResolver strikt read-only machen
Ziel
Read-/Check-/Tick-/Restore-Pfade dürfen MarkerAssignments niemals ändern.
Aktuelles Problem:
resolveRequiredMarkerWithFallback(...)klingt wie Lesen,mutiert aber NPC-Markerfelder.
Neue Regel:
resolveRequiredMarker(...)= nur lesenrepair/reassignMarker(...)= darf mutieren, aber nur Admin/Recovery mit Dry-run/Force
Aufteilen:
resolveAssignedMarkerReadOnly(...)validateAssignedMarkerReadOnly(...)findFallbackCandidateReadOnly(...)applyMarkerRecoveryExplicit(...)
Verboten in Read-Pfaden:
npc.bedMarkerId(...)npc.foodMarkerId(...)npc.workMarkerId(...)npc.markerAssignments.put(...)saveState()
Betroffene Pfade:
NpcRoutineRunnerStateTargetingServiceIdleMarkerServiceRespawnRecoveryServiceRelinkWorkflowServiceNpcTickPipeline
Review-Fragen:
[ ] hasRequiredMarkers mutiert nichts?[ ] missingRequiredMarkers mutiert nichts?[ ] resolveDesiredTarget mutiert keine Marker?[ ] Restore mutiert keine Marker?[ ] Respawn-Dry-run mutiert keine Marker?[ ] Compile grün?

Phase 3 — MarkerSet transaktional machen
Ziel
/knpc marker set darf Runtime und state.json nicht auseinanderbringen.
Aktuelles Risiko:
Marker wird runtime gesetztSave schlägt fehlCommand meldet halb ErfolgRestart verliert Marker
Neue Regel:
MarkerSet ist eine Transaktion.Wenn Save fehlschlägt, wird Runtime zurückgerollt.
Umsetzen:
1. Snapshot MarkerRegistry2. Snapshot betroffener NPC-MarkerAssignments3. Marker setzen4. ggf. NPC zuweisen5. saveStateSafely()6. wenn Save fehlschlägt:   - MarkerRegistry zurückrollen   - NPC markerAssignments zurückrollen   - klare Fehlermeldung7. nur bei Save-Erfolg Erfolg melden
Zusätzlich:
/knpc marker set <role> <markerName>
nicht mehr:
/knpc marker set <MarkerType>
Review-Fragen:
[ ] Save-Failure rollt Runtime zurück?[ ] Kein Erfolg bei Save-Failure?[ ] Kein Alias cook -> FOOD?[ ] markerName wird gegen requiredMarkers geprüft?[ ] Compile grün?

Phase 4 — MarkerClear absichern
Ziel
/knpc marker clear darf keine NPC-Zuweisungen zerstören.
Aktuelles Risiko:
MarkerRegistry leerNPC zeigt noch auf alte markerIdstate.json inkonsistent
Neue Regel:
MarkerClear löscht standardmäßig keine Marker,die von NPCs referenziert werden.
Neuer Ablauf:
/knpc marker clear→ Dry-run / Sicherheitsprüfung→ wenn Marker von NPCs benutzt werden: blockieren/knpc marker clear --force→ nur mit Bericht→ entweder NPC-Zuweisungen sauber entfernen→ oder Marker nicht löschen
Besser:
/knpc marker clear pending→ löscht nur unzugewiesene pending Marker/knpc marker clear unused→ löscht nur Marker ohne NPC-Referenz/knpc marker clear all --force→ gefährlich, klare Warnung
Review-Fragen:
[ ] Referenzprüfung vorhanden?[ ] Benutzte Marker werden ohne --force nicht gelöscht?[ ] Save-Failure rollt Runtime zurück?[ ] Clear meldet nicht falsch Erfolg?[ ] Compile grün?

Phase 5 — Spawn darf active marker nicht still löschen
Ziel
Nach Spawn darf Marker-Kontext nicht heimlich verschwinden.
Aktuelles Risiko:
/knpc spawn lumberjack AmarkerRegistry.clearActive()/knpc spawn lumberjack BMarker fehlen
Neue Regel:
Spawn konsumiert Marker nicht automatisch.
Optionen:
Standard:Marker bleiben erhalten.Optional später:--consume-markerslöscht nur pending marker dieser Role,aber niemals globale MarkerRecords.
Wichtig:
Spawn darf keine MarkerRegistry-Daten löschen,außer der Command sagt es ausdrücklich.
Review-Fragen:
[ ] clearActive nach Spawn entfernt oder kontrolliert?[ ] Zweiter Spawn mit gleichen Markern möglich?[ ] Kein stilles Löschen?[ ] Save-Failure rollbackt Spawn und Marker?[ ] Compile grün?

Phase 6 — Spawn-/Respawn-Rollback schützt Marker
Ziel
Fehlgeschlagener Spawn/Respawn darf MarkerAssignments nicht verändern.
Neue Regel:
Spawn-Rollback rollbackt:- entityUuid- entityRef- entityStatus- npcId bleibt stabil- markerAssignments- pending marker context
Besonders für:
SpawnNpcCommandNpcRoutineRunner.spawnNpcWithEntityRespawnRecoveryServiceNpcRespawnMissingCommand
Dry-run-Regel:
Dry-run darf niemals MarkerAssignments ändern.
Review-Fragen:
[ ] MarkerAssignment-Snapshot vor Spawn?[ ] Rollback bei Spawn-Fehler?[ ] Rollback bei Save-Fehler?[ ] Dry-run komplett read-only?[ ] Compile grün?

Phase 7 — markerAssignments als echte Marker-v2-Wahrheit
Ziel
Alte feste Felder werden nicht mehr Hauptsystem.
Alt:
bedMarkerIdfoodMarkerIdworkMarkerIdchestMarkerIddoorMarkerIdchillMarkerId
Neu:
markerAssignments:{  "bed": "marker-uuid-1",  "food": "marker-uuid-2",  "work": "marker-uuid-3"}
Regeln:
Key = MarkerName aus requiredMarkersValue = markerIdMarkerType kommt aus markerRoles[markerName]
Wichtig:
requiredMarkers["food"]markerRoles["food"] = "FOOD"routine targetMarker = "food"state.json markerAssignments["food"] = markerId
Kein:
cook -> FOODtable -> FOODsleep -> BED
Review-Fragen:
[ ] markerAssignments existiert?[ ] Keys sind markerName, nicht MarkerType?[ ] alte Felder nur Kompatibilität?[ ] keine automatische Reparatur beim Load?[ ] Compile grün?

Phase 8 — Referenzielle Marker-Validierung
Ziel
Jede NPC-Zuweisung muss auf echten Marker zeigen.
Prüfen:
Für jeden NPC:- jeder requiredMarker hat markerAssignments[key]- markerAssignments key existiert in requiredMarkers- markerId existiert in MarkerRegistry- MarkerRecord worldId == npc.worldId- MarkerRecord type == markerRoles[key]- keine extraneous markerAssignments
Bei Fehler:
nicht automatisch reparierennicht fallbackennicht speichernnur Status/Warnung
Admin-Recovery separat:
/knpc marker repair --dry-run/knpc marker repair --force
Review-Fragen:
[ ] Validierung mutiert nichts?[ ] Fehler werden gemeldet?[ ] Kein Auto-Fallback?[ ] Keine falsche Speicherung?[ ] Compile grün?

Phase 9 — Same-coordinate / zero-distance Navigation fix
Ziel
Wenn aktueller Marker und nächster Routine-Marker exakt gleiche Koordinaten haben, darf keine WALKING-Dauerschleife entstehen.
Problemfall:
NPC steht auf foodnächste Routine: chestfood und chest haben gleiche PositionCode startet WALKING_TO_CHESTEngine bewegt nichtState bleibt walkingMarker wird nie erreicht
Neue Regel:
Wenn currentPosition bereits am targetMarker ist:→ keine Navigation starten→ sofort Ziel-State setzen→ currentMarker = targetMarker→ currentMarkerType setzen→ Action starten→ NavigationState clearen
Gilt auch bei:
- gleicher Marker- anderer Marker, gleiche Koordinate- Distanz <= stopDistance- Distanz <= arrival epsilon
Wichtig:
MarkerId darf trotzdem wechseln.Position gleich heißt nicht Marker gleich.
Also:
food markerId != chest markerIdPosition gleich→ kein Walk→ aber currentMarker wird chest→ State wird USING_CHEST
Review-Fragen:
[ ] Kein WALKING-State bei 0 Distanz?[ ] anderer Marker gleiche Position wird korrekt erreicht?[ ] Action wechselt korrekt?[ ] pendingAction wird nicht hängen gelassen?[ ] NavigationState wird gecleart?[ ] Compile grün?

Phase 10 — Runtime auf MarkerName statt MarkerType umstellen
Ziel
Routine arbeitet mit MarkerName.
Nicht mehr:
State -> MarkerType -> bedMarkerId
Sondern:
Routine targetMarker→ markerAssignments[targetMarker]→ MarkerRecord→ markerRoles[targetMarker]
Runtime-Felder:
movementState = WALKING / IDLEtargetMarker = foodtargetMarkerType = FOODactivityState = EATINGcurrentMarker = foodcurrentMarkerType = FOOD
Alte States:
WALKING_TO_BEDWALKING_TO_WORKWALKING_TO_FOOD
Noch nicht sofort löschen, aber nicht weiter ausbauen.
Review-Fragen:
[ ] Runtime nutzt targetMarker als String?[ ] MarkerType nur aus markerRoles?[ ] Keine State->MarkerType-Hauptlogik?[ ] alte WALKING_TO_* nur Legacy?[ ] Compile grün?

Phase 11 — JSON-Dateien Marker-v2-konform machen
Aktive JSONs prüfen:
test_group.jsonlumberjack_group.jsondefault_day_cycle.json
Regeln:
requiredMarkers:- bed- chest- food- workmarkerRoles:bed -> BEDchest -> CHESTfood -> FOODwork -> WORKRoutine targetMarker:nur bed/chest/food/work
Review-Fragen:
[ ] requiredMarkers == markerRoles keys?[ ] markerRoles values == uppercase key?[ ] Routine targetMarker erlaubt?[ ] keine Aliase?[ ] Compile grün?

Logic Errors im bisherigen Plan
Der alte Plan war zu optimistisch.
Fehler 1
Marker-v2 wurde vor Save/Load-Safety geplant.
Besser:
Save/Load zuerst.
Fehler 2
Resolver-Fallback wurde nicht hart getrennt.
Besser:
Read-only Resolver und explicit Recovery trennen.
Fehler 3
MarkerSet-Rollback war zu spät oder fehlte.
Besser:
Commands transaktional machen.
Fehler 4
MarkerClear war nicht sicher genug.
Besser:
Referenzprüfung + Dry-run + --force.
Fehler 5
Spawn clearActive wurde zu harmlos behandelt.
Besser:
kein stilles Konsumieren von Markern.
Fehler 6
Same-coordinate Marker wurden nicht bedacht.
Besser:
Immediate-arrival Branch vor Navigation.
Fehler 7
markerAssignments wurden eingeführt, bevor Mutationen kontrolliert waren.
Besser:
erst Mutationsverbot, dann markerAssignments.

Weitere Logic Errors, die ich zusätzlich sehe
1. MarkerRegistry Restore kann alte Active-Marker wiederbeleben
Wenn activeMarkerIds fehlt, wird aus allen Markern wieder lastByType aufgebaut.
Gefahr:
alte Marker werden plötzlich wieder activeSpawn nimmt falschen Marker
2. Marker gehören keinem Besitzer
Aktuell hat ein MarkerRecord keine klare Besitzer-/Scope-Info.
Gefahr:
NPC A und NPC B teilen versehentlich MarkerFallback nimmt Marker vom anderen NPC
Für Marker-v2:
kein implizites TeilenSharing nur später explizit
3. MarkerName-Normalisierung kann Kollisionen erzeugen
Beispiele:
"Food"" food ""FOOD"
Muss sauber normalisiert werden zu:
food
Aber:
"foo-d""foo_d"
dürfen nicht versehentlich gleich werden, außer klar definiert.
4. MarkerAssignments können extraneous Keys behalten
Beispiel:
requiredMarkers: bed, foodmarkerAssignments: bed, food, old_work
Gefahr:
alte Marker bleiben ewig im State
5. Routine-Wechsel während aktiver Navigation
Wenn Routine während Bewegung von work auf food wechselt:
navState targetMarkerId muss verglichen werdentargetMarkerName muss auch verglichen werdentargetAction muss auch verglichen werden
Sonst läuft NPC zum alten Marker weiter.
6. MarkerClear während aktiver Navigation
Wenn NPC gerade zu Marker X läuft und Marker X gelöscht wird:
NavigationState zeigt auf gelöschten MarkerNPC läuft trotzdem weiter
Neue Regel:
MarkerClear muss aktive Navigationen prüfen/blockieren.
7. Save-Failure bei /knpc clear und /knpc remove
Aktuell gefährlich, weil:
Runtime entfernt NPCSave kann fehlschlagenCommand meldet trotzdem Erfolg oder ist nicht sauber rollbackbar
Für Marker-v2 wichtig, weil sonst NPC-Record/Entity/Marker auseinanderlaufen.
8. Index-basiertes Remove kann falschen NPC treffen
/knpc remove <index> ist gefährlich bei wechselnder Liste.
Besser später:
/knpc remove <npcId>
oder mindestens:
Dry-run zeigt npcId/name/role vor --force.
9. MarkerType.valueOf bleibt gefährlich beim Laden
Alles mit valueOf muss einzeln abgefangen werden.
Regel:
ein kaputter MarkerType darf nur diesen Marker invalid machen,nicht den ganzen State.
10. Gleiches Ziel, andere Action
Wenn Marker gleich bleibt, aber Action wechselt:
NPC darf nicht "already there" bleiben und alte Action behalten.
Beispiel:
food marker gleich12:00 EATING eat_meal13:00 EATING drink
Action muss wechseln.

Neue Reihenfolge kurz
1. Audit2. Save/Load Safety3. MarkerResolver read-only4. MarkerSet Rollback5. MarkerClear Safety6. Spawn clearActive entfernen/kontrollieren7. Spawn/Respawn Marker-Rollback8. markerAssignments einführen9. Referenzielle Validierung10. Same-coordinate Arrival fix11. Runtime auf markerName umstellen12. JSONs anpassen13. Finaler Restart-/Multi-NPC-Test
Wichtigster Satz für den Agent
Marker-v2 darf erst umgesetzt werden, wenn MarkerAssignments nicht mehr heimlich durch Read-, Tick-, Restore-, Dry-run- oder Fallback-Pfade verändert werden können.



Detailbericht — NPCs, die sterben können
Grundregel

friendly und hostile bestimmen nur Verhalten gegenüber Spielern:

friendly = greift Spieler nicht automatisch an
hostile = greift Spieler an

Das entscheidet nicht, ob ein NPC sterben darf oder respawnt.

Dafür brauchst du extra:

deathPolicy
1. Warum deathPolicy wichtig ist

Ein NPC kann verschiedene Bedeutungen haben:

Dorf-NPC → Teil der Welt
Quest-Begleiter → Teil einer Quest
Söldner → nur temporär angeheuert
Monster → Gegner, soll wiederkommen
wichtiger Händler → darf nicht dauerhaft verschwinden

Alle können friendly oder hostile sein, aber ihre Todeslogik ist unterschiedlich.

2. Empfohlene Death-Policies
2.1 IMMORTAL

NPC kann nicht sterben.

Geeignet für:

Questgeber
wichtige Händler
Stadtführer
wichtige System-NPCs

Bei Schaden:

NPC nimmt keinen Schaden
oder Schaden wird ignoriert

Marker:

bleiben immer erhalten

State:

NPC bleibt ACTIVE
2.2 RESPAWN

NPC kann sterben, kommt aber wieder.

Geeignet für:

Dorf-NPCs
Landschafts-NPCs
Quest-Begleiter
Monster
Wachen

Bei Tod:

Entity verschwindet
NPC-Record bleibt
Marker bleiben
Status wird RESPAWN_PENDING
nextRespawnAt wird gesetzt

Nach Timer:

neue Entity wird gespawnt
gleiche npcId bleibt
neue entityUuid wird gespeichert

Wichtig:

Respawn nur wenn Chunk geladen ist
kein blindes Spawnen bei ungeladenem Chunk
2.3 PERMANENT_DEATH

NPC stirbt endgültig.

Geeignet für:

angeheuerter Söldner
temporärer Begleiter
einmalige Event-NPCs

Bei Tod:

Entity weg
NPC-Record aus state.json raus
NPC-eigene Marker raus
saveStateSafely prüfen

Wichtig:

Nur löschen, wenn Entity-Death wirklich sicher bestätigt ist.
Nicht bei MISSING_ENTITY löschen.
2.4 DESPAWN_ONLY

NPC verschwindet, aber Tod zählt nicht als echter Tod.

Geeignet für:

temporäre Illusionen
Event-NPCs
Deko-NPCs
Script-NPCs

Bei Despawn:

Entity weg
Record je nach System entfernen oder pausieren
keine Death-Strafe
kein Loot
kein Respawn-Timer, außer extra definiert
3. Wo deathPolicy speichern?

Am besten in:

Server/NPC/Keystone/persistence/*.json

Nicht in friendly/hostile.

Beispiel-Profile
persistent_citizen.json
→ normale Bürger, bleiben in der Welt

protected_citizen.json
→ wichtige NPCs, unsterblich

escort_quest_npc.json
→ Begleiter, kann sterben, respawnt später

hired_temporary_npc.json
→ angeheuerter Söldner, stirbt endgültig

hostile_respawn.json
→ Gegner, respawnt nach Timer
4. Was gehört in state.json?

In state.json steht nicht die Grundregel, sondern der aktuelle Zustand.

Speichern:

npcId
entityUuid
entityStatus
worldId
currentPosition
deathState
deadAt
nextRespawnAt
deathCount
Marker-Zuweisungen

Nicht speichern:

entityRef
aktive Navigation
Door-Runtime
aktive Action
temporäre Tick-Daten
5. Marker-Regeln bei Tod
Wenn NPC respawnt
NPC-Record bleibt
Marker bleiben
npcId bleibt gleich
nur entityUuid wird später ersetzt

Beispiel:

Dorf-NPC stirbt
→ Marker bleiben
→ NPC kommt später wieder an seinen Ort
Wenn NPC permanent stirbt
NPC-Record löschen
NPC-eigene Marker löschen

Aber nur, wenn kein anderer NPC diese Marker benutzt.

Wenn NPC nur MISSING_ENTITY ist
Marker NICHT löschen
Record NICHT löschen

MISSING_ENTITY heißt nur:

Entity wurde gerade nicht gefunden

Nicht:

NPC ist tot
6. Friendly-NPC-Beispiele
Normaler Bürger
attitude = friendly
deathPolicy = RESPAWN oder IMMORTAL

Empfehlung:

wichtige Bürger: IMMORTAL
normale Ambient-Bürger: RESPAWN
Quest-Begleiter
attitude = friendly
deathPolicy = RESPAWN

Bei Tod:

Quest pausiert
NPC respawnt am Quest-Checkpoint
Spieler muss ihn wieder abholen
Angeheuerter Söldner
attitude = friendly
deathPolicy = PERMANENT_DEATH

Bei Tod:

Söldner weg
Record weg
Marker weg
Vertrag beendet

Das passt, weil er nicht Teil der normalen Welt ist.

7. Hostile-NPC-Beispiele
Normales Monster
attitude = hostile
deathPolicy = RESPAWN

Bei Tod:

Loot droppen
Entity entfernen
Respawn-Timer starten
SpawnPoint bleibt
Boss
attitude = hostile
deathPolicy = RESPAWN

Aber mit:

langer Respawn-Timer
Loot-Cooldown
eventuell globaler Boss-State
8. Wichtige Status-Idee

Du brauchst wahrscheinlich neue oder klare Statuswerte:

ACTIVE
MISSING_ENTITY
RESPAWN_PENDING
DEAD_PERMANENT
DISABLED

Einfach erklärt:

ACTIVE = NPC lebt
MISSING_ENTITY = Entity gerade nicht gefunden
RESPAWN_PENDING = NPC ist tot, kommt später wieder
DEAD_PERMANENT = NPC ist endgültig tot / soll gelöscht werden
DISABLED = NPC ist absichtlich deaktiviert
9. Wichtigste Sicherheitsregel

Nicht vermischen:

MISSING_ENTITY ≠ tot
Entity gelöscht ≠ NPC-Record gelöscht
NPC-Tod ≠ immer Marker löschen

Richtig:

Respawn-NPC tot → Record bleibt, Marker bleiben
Permanent-Death-NPC tot → Record weg, Marker weg
MISSING_ENTITY → nichts löschen
10. Meine Empfehlung für Keystone

Für den Start:

IMMORTAL
RESPAWN
PERMANENT_DEATH

DESPAWN_ONLY erst später.

Erste Profile
protected_citizen.json
deathPolicy = IMMORTAL

persistent_citizen.json
deathPolicy = RESPAWN

escort_quest_npc.json
deathPolicy = RESPAWN

hired_temporary_npc.json
deathPolicy = PERMANENT_DEATH

hostile_respawn.json
deathPolicy = RESPAWN
Kurzfazit

NPC-Tod sollte nicht über friendly/hostile laufen.

Besser:

attitude = Kampfverhalten
persistence/deathPolicy = Todes- und Respawnlogik

Das ist sauber, flexibel und verhindert, dass wichtige Welt-NPCs aus Versehen dauerhaft verschwinden.



####

Grundregel

/knpc remove und /knpc clear sind Admin-Löschbefehle.

Das heißt:

deathPolicy wird NICHT befolgt.

Also auch wenn ein NPC deathPolicy = RESPAWN hat:

/knpc remove
→ NPC endgültig löschen
→ kein Respawn
→ aus state.json raus
→ seine Marker raus

deathPolicy gilt nur bei echtem Tod im Spiel.

1. /knpc remove <index>

Ablauf:

1. NPC über index finden
2. npcId merken
3. alle MarkerIds des NPC merken
   - bedMarkerId
   - workMarkerId
   - foodMarkerId
   - chestMarkerId
   - doorMarkerId
   - chillMarkerId

4. Live-Entity entfernen, falls vorhanden
5. NPC aus npcs[] löschen
6. seine Marker aus markers[] löschen
   aber nur, wenn kein anderer NPC sie benutzt
7. activeMarkerIds für diesen NPC bereinigen
8. saveStateSafely() aufrufen
9. Save-Ergebnis prüfen

Wichtig:

Wenn Save fehlschlägt → nicht "erfolgreich gelöscht" melden.
2. /knpc clear

Ablauf:

1. Alle NPCs sammeln
2. Für jeden NPC alle MarkerIds sammeln
3. Alle Live-Entities sicher entfernen, falls vorhanden
4. Alle NPC-Records aus npcs[] löschen
5. Alle gesammelten NPC-Marker aus markers[] löschen
6. activeMarkerIds leeren oder passend bereinigen
7. saveStateSafely() prüfen

Danach sollte state.json ungefähr so aussehen:

{
  "markers": [],
  "npcs": [],
  "activeMarkerIds": {}
}

Oder, falls globale Marker später erlaubt sind:

{
  "markers": [
    "nur Marker, die keinem gelöschten NPC gehörten"
  ],
  "npcs": [],
  "activeMarkerIds": {}
}
3. NPC stirbt im Spiel

Hier gilt dann deathPolicy.

PERMANENT_DEATH
Entity tot
→ NPC-Record löschen
→ NPC-Marker löschen
→ saveStateSafely()
RESPAWN
Entity tot
→ NPC-Record behalten
→ Marker behalten
→ entityStatus = RESPAWN_PENDING
→ nextRespawnAt setzen
→ alte entityUuid entfernen oder ersetzen beim Respawn
IMMORTAL
Tod verhindern
oder Tod sofort rückgängig machen
Sehr wichtige Trennung
/knpc remove = Admin löscht NPC endgültig
/knpc clear = Admin löscht alle NPCs endgültig
NPC death = deathPolicy entscheidet
MISSING_ENTITY = nichts löschen
Wichtig für deine JSON-Länge

Wenn /knpc remove oder /knpc clear aktuell nur npcs[] löscht, aber nicht markers[], dann entstehen genau diese Marker-Leichen.

Der Fix sollte heißen:

Remove/Clear müssen owned marker cleanup machen.

Nicht automatisch bei Restart. Nur bei echtem Admin-Delete oder PERMANENT_DEATH.