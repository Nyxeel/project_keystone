Status

Keine Dateien geändert.
Kein fixer Zielpfad vorgegeben.
Keine Migration geplant.
Zielarchitektur
Quellen klar getrennt:
requiredMarkers kommt aus NPC-Definition.
markerRoles mappt markerName auf MarkerType.
markerAssignments gehört zur NPC-Instanz als einzige persistente Marker-Wahrheit.
Active Spawn Marker ist nur temporäres, role-spezifisches Staging.
Neues Staging-Konzept:
ActiveSpawnMarkerStore
Struktur: activeSpawnMarkersByRole[role][markerName] = stagedMarker
stagedMarker enthält mindestens markerId, markerType, worldId, gesetztAm
Schlüsselinvariante:
Schlüssel ist immer role + markerName
Nie nur MarkerType
Nie global über MarkerRegistry.lastByType
Keine Shared Marker:
Ein markerId darf nicht gleichzeitig in markerAssignments mehrerer NPCs genutzt werden
Shared-Marker-Precheck ist blockierend vor Spawn-Verbrauch
Datenmodell
Persistiert:
npc.markerAssignments
markerAssignments[markerName] = MarkerAssignment(markerId, markerType)
Nicht persistiert:
ActiveSpawnMarkerStore (temporär, runtime-scope)
Normalisierung:
role normalisiert wie RoleDefinition-Logik (klein/trimmed)
markerName normalisiert auf trimmed + lower-case
Keine Alias-Mappings wie cook->food
Command-Semantik
A. Spawn-Staging
Befehl: /knpc marker set <role> <markerName>
Ablauf:
role muss existieren
markerName muss in requiredMarkers der role enthalten sein
markerRoles[markerName] muss existieren und MarkerType liefern
Marker an Spielerposition erzeugen
Marker nur im role-spezifischen Staging ablegen
Muss garantiert sein:
Schreibt keine markerAssignments
Ändert keinen NPC
Ändert keine Role-Datei
Ändert keine JSON-Definition
Alte Ein-Argument-Syntax ohne role wird geblockt
B. Spawn

Befehl: /knpc spawn <role> <npcName>
Ablauf:
npcName muss eindeutig und nicht leer sein
Alle requiredMarkers müssen im Role-Staging vorhanden sein
Shared-Marker-/Ownership-Precheck muss vor Kopie PASS sein
Erst dann markerAssignments in neuen NPC kopieren
saveStateSafely prüfen
Role-Staging erst nach erfolgreichem Save leeren
Bei Fehler:
Keine Erfolgsmeldung
Kein stiller Ersatzmarker
Kein getNextAvailable-Fallback
C. Reassign (später, gated)

Zielsyntax: /knpc marker set <markerName> <npcName>
Harte Bedingung:
Nur erlaubt, wenn npcName-Eindeutigkeit global PASS bleibt
Sonst blockieren oder auf spätere npcId-Variante schieben
Ambiguitätsschutz:
Wenn Syntax nicht eindeutig zwischen Staging und Reassign auflösbar ist, blockieren
Klare Usage-Meldung ausgeben
Step-Reihenfolge
Step 1.2 (dieser Plan) abgeschlossen
Step 2.1 ActiveSpawnMarkerStore einführen (ohne lastByType)
Step 2.2 /knpc marker set <role> <markerName> als reines Staging
Step 2.3 Shared-Marker-/Ownership-Precheck (read-only)
Step 2.4 /knpc spawn <role> <npcName> mit sicherem Staging-Verbrauch
Step 3.1 Reassign per npcName nur bei weiterem PASS der Name-Eindeutigkeit
Danach Phase 4/5/6/7 wie vereinbart
Safety-Regeln
Verboten:
Legacy-Migration (auto/admin/load)
/knpc marker migrate --dry-run
/knpc marker migrate --apply
MarkerRegistry.lastByType als role-staging Basis
getNextAvailable als Ersatzlogik
resolveRequiredMarkerWithFallback in neuem Pfad
Shared Marker
Doppelte npcNames
Pflicht:
Save-Failure nie als Erfolg melden
Blockierendes Verhalten bei Ownership-Konflikt
Keine Mutation in reinen Prüfpfaden
Tests (Plan)
Compile-Gate nach jedem Implementierungsstep:
mvn -q -DskipTests test-compile
Funktions-Tests:
Spawn mit leerem Namen wird blockiert
Spawn mit doppeltem Namen wird blockiert
/knpc marker set <role> <markerName> ohne role wird blockiert
MarkerName außerhalb requiredMarkers wird blockiert
MarkerType aus markerRoles wird korrekt erzwungen
Spawn blockiert bei fehlendem required marker im Staging
Spawn blockiert bei Shared-Marker-Konflikt
Spawn leert Staging nur nach erfolgreichem Save
Save-Failure führt zu keiner Erfolgsmeldung
Risiken
Parser-Ambiguität zwischen Staging und Reassign-Syntax
Rollback-Komplexität bei Save-Failure in kombinierten Marker/Spawn-Pfaden
Race-Risiko bei parallelen Command-Aufrufen auf dasselbe role-staging
Staging-Verlust bei Server-Neustart (bewusst, da temporär)
Altwelten mit Legacy-State bleiben inkompatibel und müssen klar gemeldet werden
Backlog
Klare Diagnoseausgabe: staged marker anzeigen pro role
Optionaler Command zum expliziten Verwerfen von role-staging
Optionaler späterer Reassign per npcId als Fallback
Telemetrie/Logs für Ownership-Blockgründe
Spätere Harmonisierung der Marker-Command-UX ohne Ambiguität
Review — Marker-v2 Plan

Urteil

PASS
Scope-Check

Kein fixer Zielpfad vorgegeben: ja
Keine sofortige Implementierung geplant: ja
Dateien geändert: nein
Fix-Ziel

Keine Legacy-Migration: ja
Keine optionale Admin-Migration: ja
Kein lastByType-Aufbau: ja
role + markerName als Staging-Schlüssel: ja
requiredMarkers, markerRoles, markerAssignments sauber getrennt: ja
/knpc marker set als Spawn-Staging definiert: ja
/knpc spawn sauber definiert: ja
Reassign von npcName-Eindeutigkeit abhängig: ja
Alte Syntax ohne role wird geblockt: ja
Tests enthalten: ja
Keine Safety-Widersprüche: ja