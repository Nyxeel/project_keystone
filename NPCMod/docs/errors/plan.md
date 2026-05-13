0) Globaler Vorspann für jeden Prompt
Du arbeitest an meiner Hytale-Mod „NPCMod / KeystoneNPC“.

Du bist mein strenger Logic-Fix-Controller.

WICHTIG:
Keine Repository-Verwaltungsbefehle verwenden:
- kein git diff
- kein git stash
- kein git restore
- kein git reset
- kein Commit
- kein Branch-Wechsel
- keine Repository-Verwaltungsbefehle

Arbeitsprinzip:
- immer nur ein kleiner Step
- danach Review
- bei FAIL nur Fix für genau diesen Step
- erst nach PASS weiter
- keine angrenzenden Features
- keine großen Refactors
- keine Safety-Regeln still brechen
- keine fremden Änderungen außerhalb des Step-Scopes bewerten, ändern oder zurücksetzen

Fremdänderungen:
Wenn der Nutzer parallel andere Dateien ändert:
- ignorieren
- nicht stoppen
- nicht zurücksetzen
- nicht bewerten
- nur melden, wenn sie direkt den aktuellen Step-Scope blockieren

Safety-Regeln:
Wenn unklar:
- nicht löschen
- nicht spawnen
- nicht relinken
- nicht überschreiben
- keinen Save-Failure als Erfolg melden
- keinen Load-Failure als gültigen leeren State behandeln
- keine Marker in read-only Pfaden mutieren
- kein dynamisches setRoleName("KeystoneNPC_...")
- kein Role-Prefix-Fallback
- kein getNextAvailable als Restore-/Tick-/Spawn-Fallback
A) Gesamtprompt für PLAN Mode
MODUS:
PLAN Mode.

AUFGABE:
Erstelle einen sicheren Umsetzungsplan für NPCMod / KeystoneNPC.

WICHTIG:
Noch keine Dateien ändern.
Noch keine Tests ausführen.
Noch keine Safety-Dateien aktualisieren.
Noch keinen Patchreport schreiben.
Keine Repository-Verwaltungsbefehle verwenden.

ZIEL:
Plane die nächsten Steps so, dass keine Kreisarbeit entsteht und Marker-v2 nicht zu früh implementiert wird.

AKTUELLE KORREKTUR:
Phase-0-Steps 0.1 bis 0.3 gelten als bereits PASS.
Sie dürfen nur noch als Regression-NOOP-Prüfung geplant werden.

Das bedeutet:
- nicht blind neu fixen
- zuerst prüfen, ob Regression existiert
- wenn kein Fehler existiert: NOOP / bereits erledigt
- keinen Ersatzfix erfinden

PHASE-0-STEPS:
0.1 NpcRespawnMissingCommand Save-Failure — nur Regression-NOOP
0.2 Marker worldId/type/id Gate — nur Regression-NOOP
0.3 SpawnNpcCommand Rollback detailed Result — nur Regression-NOOP

WICHTIGE KORREKTUREN:
- Step 0.2 darf NpcRoutineRunner.java anfassen, wenn dort die konkrete Marker-Zuweisungslogik liegt.
- Legacy-Migration komplett entfernen.
- Keine automatische Migration.
- Keine Admin-Migration.
- Kein /knpc marker migrate --dry-run.
- Kein /knpc marker migrate --apply.
- Keinen festen Marker-v2-Zielpfad vorgeben.
- Marker-v2-Plan nur als Planinhalt ausgeben oder nur in eine Datei schreiben, wenn der Nutzer den Pfad ausdrücklich vorgibt.
- Role-basierte aktive Marker dürfen nicht auf MarkerRegistry.lastByType aufbauen.
- Stattdessen eigene role-spezifische Staging-Struktur planen.
- npcName-Eindeutigkeit als eigenen Vor-Step planen.
- Reassign per npcName darf erst nach PASS dieses Vor-Steps starten.
- Falls npcName-Eindeutigkeit nicht PASS ist, muss Reassign per npcName blockiert bleiben oder später per npcId geplant werden.

QUELLEN-PRIORITÄT:
1. aktueller Code
2. neueste Patchreports
3. safety/*.md
4. aktuelle finale User-Entscheidung
5. NPCMod_Lagebericht
6. ältere Analysen / TODOs / Prompts

PHASEN:
Phase 0:
- nur Regression-NOOP-Prüfungen für bereits erledigte Safety-Fixes

Phase 1:
- npcName-Eindeutigkeit als Vor-Step
- Marker-v2 Plan Mode, keine Implementierung

Phase 2:
- role-spezifisches Spawn-Staging
- /knpc marker set <role> <markerName>
- Shared-Marker-/Ownership-Precheck vor Spawn-Verbrauch
- /knpc spawn <role> <npcName> verbraucht Role-Staging nur nach Shared-Marker-Check

Phase 3:
- Reassign per npcName, nur wenn npcName-Eindeutigkeit PASS ist

Phase 4:
- /knpc marker clear entfernen/deaktivieren

Phase 5:
- Remove/Clear safe-by-default mit Marker-Cleanup

Phase 6:
- Legacy-Abbau ohne Migration

Phase 7:
- finaler Safety-Doku- und Patchreport-Step

AUSGABE:
Erstelle:
1. Agent Step Liste
2. Review Prompt nach jedem Step
3. Fix Prompt für jeden FAIL-Fall
4. Fortschritts-Checkliste
5. Kreisarbeits-Prüfung
6. Marker-v2-Einordnung
7. Widerspruchs-/Versionsdrift-Prüfung
8. Backlog

REGEL:
Jeder normale Agent-Step muss eine konkrete Änderung oder berechtigten NOOP-Ausgang haben.
Keine reinen Prüfsteps, außer Phase-0-Regression-NOOP.
B) Phase 0 — Regression-NOOP-Prompts

Agent Step 0.1 — RespawnMissing Save-Failure Regression prüfen
MODUS:
Enger Regression-NOOP-Check.

ZIEL:
Prüfe, ob NpcRespawnMissingCommand weiterhin saveStateSafely() korrekt auswertet.

STATUS:
Dieser Step gilt bereits als PASS.
Nur prüfen, ob eine Regression existiert.

ERLAUBTE DATEI BEI ECHTER REGRESSION:
- NpcRespawnMissingCommand.java

NICHT ÄNDERN:
- keine Marker
- kein Marker-v2
- keine Respawn-Policy
- kein Relink
- kein Spawn
- keine Navigation
- keine Door-Logik
- keine Safety-Doku

AUFGABEN:
1. Suche den Pfad result.stateChanged() && !dryRun.
2. Prüfe, ob saveStateSafely() Ergebnis geprüft wird.
3. Prüfe, ob Save-Failure normale Erfolgsmeldungen verhindert.
4. Prüfe, ob dry-run read-only bleibt.

WENN KORREKT:
- keine Datei ändern
- Ergebnis: NOOP / bereits erledigt

WENN REGRESSION:
- nur NpcRespawnMissingCommand.java fixen
- bei Save-Failure klare Admin-Meldung
- Drift-Hinweis Runtime vs state.json
- keine normale Erfolgsmeldung
- sofort abbrechen

TEST BEI JAVA-ÄNDERUNG:
mvn -q -DskipTests test-compile

ABSCHLUSSBERICHT:
- NOOP oder Fix?
- Geänderte Dateien
- Compile-Ergebnis, falls Änderung
- Restgefahren

Review Prompt 0.1
Reviewe nur Step 0.1.

NICHT implementieren.
Keine Dateien ändern.

Prüfe:
- War NOOP berechtigt?
- Wird saveStateSafely() ausgewertet?
- Wird Save-Failure nicht als Erfolg gemeldet?
- Bleibt dry-run read-only?
- Wurde nur NpcRespawnMissingCommand.java geändert, falls überhaupt?
- Wurde kein Marker-v2 eingebaut?
- Wurde keine fremde Änderung bewertet?

ERGEBNISFORMAT:
# Review — Step 0.1 RespawnMissing Save-Failure

## Urteil
PASS / FAIL / PARTIAL / NOOP

## Scope-Check
- Erlaubte Dateien:
- Tatsächlich geänderte Dateien:
- Verbotene Änderungen: ja/nein
- Fremdänderungen ignoriert: ja/nein

## Fix-Ziel
- saveStateSafely geprüft: ja/nein
- Save-Failure blockiert Erfolgsmeldung: ja/nein
- dry-run read-only: ja/nein
- NOOP berechtigt: ja/nein

## Compile
- Ergebnis:

## Entscheidung
- Fix nötig: ja/nein
- Nächster Step erlaubt: ja/nein

Wenn FAIL:
Gib direkt den engen Fix-Prompt für Step 0.1 aus.
Fix Prompt 0.1
Fixe nur Step 0.1.

ERLAUBTE DATEI:
- NpcRespawnMissingCommand.java

ZIEL:
saveStateSafely() muss im result.stateChanged() && !dryRun Pfad ausgewertet werden.

Bei false:
- klare Admin-Fehlermeldung
- Runtime/state.json-Drift-Hinweis
- keine normale Erfolgsmeldung
- sofortiger Abbruch

NICHT ÄNDERN:
- Marker
- Spawn
- Relink
- Respawn-Policy
- Navigation
- Door
- Safety-Doku

TEST:
mvn -q -DskipTests test-compile

ABSCHLUSS:
- geänderte Dateien
- was gefixt wurde
- Compile-Ergebnis
- erneuter Review nötig: ja
Agent Step 0.2 — Marker worldId/type/id Gate Regression prüfen
MODUS:
Enger Regression-NOOP-Check.

ZIEL:
Prüfe, ob Marker-Zuweisung weiterhin sicher prüft:
- markerId existiert
- MarkerType passt
- marker.worldId == npc.worldId
- Role erlaubt diesen Marker

STATUS:
Dieser Step gilt bereits als PASS.
Nur prüfen, ob eine Regression existiert.

ERLAUBTE DATEIEN BEI ECHTER REGRESSION:
- MarkerSetCommand.java
- MarkerRegistry.java
- MarkerRecord / Marker-Modell, falls direkt nötig
- NpcRoutineRunner.java, falls dort die konkrete Marker-Zuweisungslogik liegt

WICHTIG:
NpcRoutineRunner.java ist erlaubt, aber nur für die konkrete Marker-Zuweisungslogik dieses Steps.

NICHT ÄNDERN:
- kein Marker-v2
- keine markerAssignments-Neuarchitektur
- keine Legacy-Felder löschen
- kein Reconcile-Refactor
- kein Remove/Clear-Umbau
- keine Navigation-/Door-Änderung
- keine Safety-Doku

AUFGABEN:
1. Finde die Marker-Zuweisungsstelle.
2. Prüfe markerId-Existenz.
3. Prüfe MarkerType.
4. Prüfe worldId.
5. Prüfe Role-Erlaubnis.
6. Prüfe, ob bad IDs nie gespeichert werden.

WENN KORREKT:
- keine Datei ändern
- Ergebnis: NOOP / bereits erledigt

WENN REGRESSION:
- nur konkreten Zuweisungspfad fixen
- bei Fehler blockieren
- nichts speichern
- klare Admin-Meldung

TEST BEI JAVA-ÄNDERUNG:
mvn -q -DskipTests test-compile

ABSCHLUSSBERICHT:
- NOOP oder Fix?
- Geänderte Dateien
- Welche Gates geprüft/gefixt wurden
- Compile-Ergebnis
Review Prompt 0.2
Reviewe nur Step 0.2.

NICHT implementieren.
Keine Dateien ändern.

Prüfe:
- War NOOP berechtigt?
- Gibt es markerId-Existenzprüfung?
- Gibt es MarkerType-Prüfung?
- Gibt es worldId-Prüfung?
- Gibt es Role-Erlaubnisprüfung?
- Kann ein NPC noch Marker aus falscher Welt bekommen?
- Werden bad IDs nicht gespeichert?
- Wurde NpcRoutineRunner.java nur dann geändert, wenn dort der konkrete Zuweisungspfad liegt?
- Wurde kein Marker-v2 eingebaut?
- Wurde keine Legacy-Entfernung gemacht?
- Wurde keine fremde Änderung bewertet?

ERGEBNISFORMAT:
# Review — Step 0.2 Marker worldId/type/id Gate

## Urteil
PASS / FAIL / PARTIAL / NOOP

## Scope-Check
- Erlaubte Dateien:
- Tatsächlich geänderte Dateien:
- NpcRoutineRunner.java berechtigt geändert: ja/nein/nicht betroffen
- Verbotene Änderungen: ja/nein
- Fremdänderungen ignoriert: ja/nein

## Fix-Ziel
- markerId-Prüfung: ja/nein
- MarkerType-Prüfung: ja/nein
- worldId-Prüfung: ja/nein
- Role-Erlaubnisprüfung: ja/nein
- NOOP berechtigt: ja/nein

## Compile
- Ergebnis:

## Entscheidung
- Fix nötig: ja/nein
- Nächster Step erlaubt: ja/nein

Wenn FAIL:
Gib direkt den engen Fix-Prompt für Step 0.2 aus.
Fix Prompt 0.2
Fixe nur Step 0.2.

ERLAUBTE DATEIEN:
- MarkerSetCommand.java
- MarkerRegistry.java
- MarkerRecord / Marker-Modell, falls direkt nötig
- NpcRoutineRunner.java, falls dort die konkrete Marker-Zuweisungslogik liegt

ZIEL:
Vor Marker-Zuweisung muss gelten:
- markerId existiert
- marker.type passt
- marker.worldId == npc.worldId
- Role erlaubt markerName/MarkerType

Bei Fehler:
- blockieren
- nichts speichern
- klare Admin-Meldung

NICHT ÄNDERN:
- Marker-v2
- markerAssignments-Neuarchitektur
- Legacy-Felder
- Reconcile
- Remove/Clear
- Navigation/Door
- Safety-Doku

TEST:
mvn -q -DskipTests test-compile

ABSCHLUSS:
- geänderte Dateien
- was gefixt wurde
- Compile-Ergebnis
- erneuter Review nötig: ja
Agent Step 0.3 — Spawn Rollback detailed Result Regression prüfen
MODUS:
Enger Regression-NOOP-Check.

ZIEL:
Prüfe, ob SpawnNpcCommand bei Save-Failure weiterhin ein detailed RemoveResult nutzt.

STATUS:
Dieser Step gilt bereits als PASS.
Nur prüfen, ob eine Regression existiert.

ERLAUBTE DATEI BEI ECHTER REGRESSION:
- SpawnNpcCommand.java

NICHT ÄNDERN:
- kein Spawn-Redesign
- kein Respawn-Redesign
- kein Marker-v2
- kein Remove/Clear-Neubau
- keine Safety-Doku

AUFGABEN:
1. Suche Save-Failure-Pfad nach Spawn.
2. Prüfe, ob detailed RemoveResult genutzt wird.
3. Prüfe, ob boolean removeNpc(...) nicht als sichere Wahrheit verwendet wird.
4. Prüfe, ob blocked / rollback failed / unsafe ehrlich gemeldet werden.

WENN KORREKT:
- keine Datei ändern
- Ergebnis: NOOP / bereits erledigt

WENN REGRESSION:
- nur SpawnNpcCommand.java fixen
- detailed RemoveResult auswerten
- keine Erfolgsmeldung bei unklarem Rollback

TEST BEI JAVA-ÄNDERUNG:
mvn -q -DskipTests test-compile

ABSCHLUSSBERICHT:
- NOOP oder Fix?
- Geänderte Dateien
- Compile-Ergebnis
Review Prompt 0.3
Reviewe nur Step 0.3.

NICHT implementieren.
Keine Dateien ändern.

Prüfe:
- War NOOP berechtigt?
- Nutzt Save-Failure-Rollback detailed RemoveResult?
- Wird boolean removeNpc(...) nicht als sichere Wahrheit behandelt?
- Werden blocked / rollback failed / unsafe sauber gemeldet?
- Gibt es keine Erfolgsmeldung bei unklarem Rollback?
- Wurde kein Marker-v2 eingebaut?
- Wurde kein Spawn-Redesign gemacht?
- Wurde keine fremde Änderung bewertet?

ERGEBNISFORMAT:
# Review — Step 0.3 Spawn Rollback detailed Result

## Urteil
PASS / FAIL / PARTIAL / NOOP

## Scope-Check
- Erlaubte Dateien:
- Tatsächlich geänderte Dateien:
- Verbotene Änderungen: ja/nein
- Fremdänderungen ignoriert: ja/nein

## Fix-Ziel
- detailed RemoveResult genutzt: ja/nein
- boolean-Pfad entschärft: ja/nein
- Rollback-Failure ehrlich gemeldet: ja/nein
- NOOP berechtigt: ja/nein

## Compile
- Ergebnis:

## Entscheidung
- Fix nötig: ja/nein
- Nächster Step erlaubt: ja/nein

Wenn FAIL:
Gib direkt den engen Fix-Prompt für Step 0.3 aus.
Fix Prompt 0.3
Fixe nur Step 0.3.

ERLAUBTE DATEI:
- SpawnNpcCommand.java

ZIEL:
Bei Save-Failure nach Spawn muss detailed RemoveResult ausgewertet werden.

Nicht erlaubt:
- Spawn-Syntax ändern
- Marker-v2
- Respawn ändern
- Remove/Clear redesignen
- Safety-Doku ändern

TEST:
mvn -q -DskipTests test-compile

ABSCHLUSS:
- geänderte Dateien
- was gefixt wurde
- Compile-Ergebnis
- erneuter Review nötig: ja
C) Phase 1 — npcName-Vor-Step
Agent Step 1.1 — npcName-Eindeutigkeit erzwingen
MODUS:
Enger Feature-Vorbereitungsstep.

ZIEL:
npcName muss eindeutig sein, bevor Reassign per npcName erlaubt wird.

WARUM:
Später soll Reassign so funktionieren:
/knpc marker set <markerName> <npcName>

Das ist nur sicher, wenn npcName eindeutig ist.

ERLAUBTE DATEIEN:
- SpawnNpcCommand.java
- NpcRecord / State-Modell, falls npcName dort fehlt oder unsicher ist
- vorhandener NPC-Lookup/List-Helper, falls direkt nötig

NICHT ÄNDERN:
- kein Marker-v2
- keine Marker-Zuweisung
- kein Reassign
- kein Remove/Clear
- keine Legacy-Entfernung
- keine Migration
- keine Safety-Doku

AUFGABEN:
1. Prüfe, ob npcName bereits persistiert wird.
2. Prüfe, ob npcName bei Spawn eindeutig sein muss.
3. Wenn bereits eindeutig:
   - NOOP / bereits erledigt melden
4. Wenn nicht:
   - leeren npcName blockieren
   - doppelten npcName blockieren
   - klare Admin-Meldung
   - keine NPC-Erstellung bei ungültigem Namen
5. Keine Reassign-Logik einbauen.

TEST:
mvn -q -DskipTests test-compile

ABSCHLUSSBERICHT:
- geänderte Dateien
- wie Eindeutigkeit geprüft wird
- Compile-Ergebnis
Review Prompt 1.1
Reviewe nur Step 1.1 — npcName-Eindeutigkeit.

NICHT implementieren.
Keine Dateien ändern.

Prüfe:
- Wird leerer npcName blockiert?
- Wird doppelter npcName blockiert?
- Entsteht kein NPC bei ungültigem Namen?
- Bleibt npcName persistierbar/stabil?
- Wurde kein Marker-v2 eingebaut?
- Wurde kein Reassign eingebaut?
- Wurde keine Migration eingebaut?
- Wurde keine fremde Änderung bewertet?
- Compile grün?

ERGEBNISFORMAT:
# Review — Step 1.1 npcName-Eindeutigkeit

## Urteil
PASS / FAIL / PARTIAL / NOOP

## Scope-Check
- Erlaubte Dateien:
- Tatsächlich geänderte Dateien:
- Verbotene Änderungen: ja/nein
- Fremdänderungen ignoriert: ja/nein

## Fix-Ziel
- leerer npcName blockiert: ja/nein
- doppelter npcName blockiert: ja/nein
- kein NPC bei ungültigem Namen: ja/nein
- Reassign nicht eingebaut: ja/nein

## Compile
- Ergebnis:

## Entscheidung
- Fix nötig: ja/nein
- Reassign per npcName später erlaubt: ja/nein
- Nächster Step erlaubt: ja/nein

Wenn FAIL:
Gib direkt den engen Fix-Prompt für Step 1.1 aus.
Fix Prompt 1.1
Fixe nur Step 1.1.

ERLAUBTE DATEIEN:
- SpawnNpcCommand.java
- NpcRecord / State-Modell, falls direkt nötig
- vorhandener Lookup/List-Helper, falls direkt nötig

ZIEL:
Spawn darf keinen leeren oder doppelten npcName erzeugen.

NICHT ÄNDERN:
- Marker
- Marker-v2
- Reassign
- Remove/Clear
- Legacy
- Migration
- Safety-Doku

TEST:
mvn -q -DskipTests test-compile

ABSCHLUSS:
- geänderte Dateien
- was gefixt wurde
- Compile-Ergebnis
- erneuter Review nötig: ja
D) Phase 1.2 — Marker-v2 PLAN Mode
Marker-v2 Plan Prompt
MODUS:
PLAN Mode.

ZIEL:
Erstelle den Marker-v2-Plan.
Nicht implementieren.

WICHTIG:
Keinen festen Zielpfad vorgeben.
Kein docs/plans/marker_v2_plan.md erzwingen.
Wenn eine Datei gewünscht ist, muss der Nutzer den Pfad separat nennen.
Ansonsten Plan nur als Text/Arbeitsinhalt ausgeben.

NICHT ÄNDERN:
- kein Java-Code
- keine JSON-Ressourcen
- keine Safety-Doku
- kein Patchreport
- keine Migration

MARKER-v2 ZIELARCHITEKTUR:
- requiredMarkers kommen aus NPC-Definition.
- markerRoles mappen markerName auf MarkerType.
- markerAssignments gehören zur konkreten NPC-Instanz.
- aktive Spawn-Marker sind nur temporäres Spawn-Staging pro Role.

VERBOTEN:
- keine Legacy-Migration
- keine Admin-Migration
- keine automatische Load-Migration
- kein /knpc marker migrate --dry-run
- kein /knpc marker migrate --apply
- kein MarkerRegistry.lastByType als Basis für role-basierte aktive Marker
- kein getNextAvailable als Ersatzlogik
- kein resolveRequiredMarkerWithFallback
- keine Shared Marker
- keine gleichen npcNames

ROLE-SPEZIFISCHES STAGING:
Plane eine eigene Struktur, z. B. logisch:
activeSpawnMarkersByRole[role][markerName] = markerId

Oder einen eigenen Service:
ActiveSpawnMarkerStore

Wichtig:
Schlüssel ist role + markerName.
Nicht nur MarkerType.
Nicht global lastByType.

COMMAND-ZIELE:
Spawn-Staging:
- /knpc marker set <role> <markerName>
- schreibt nur temporäre aktive Spawn-Marker für diese Role
- schreibt keine markerAssignments
- verändert keinen NPC
- verändert keine Role-Datei
- verändert keine JSON-Definition

Spawn:
- /knpc spawn <role> <npcName>
- npcName eindeutig
- requiredMarkers vollständig vorhanden
- aktive Role-Marker werden in markerAssignments kopiert
- Role-Staging wird erst nach erfolgreichem Save geleert

Reassign:
- /knpc marker set <markerName> <npcName>
- erst nach PASS von npcName-Eindeutigkeit
- sonst blockieren oder später npcId-Variante planen

Legacy:
- keine Migration
- alte Legacy-Welten nicht automatisch unterstützen
- Legacy-State nicht still übernehmen
- bei Legacy-State klar blockieren oder inkompatibel melden

AUSGABE:
Erstelle:
1. Zielarchitektur
2. Datenmodell
3. Command-Semantik
4. Step-Reihenfolge
5. Safety-Regeln
6. Tests
7. Risiken
8. Backlog
Review Prompt Marker-v2 Plan
Reviewe nur den Marker-v2 Plan.

NICHT implementieren.
Keine Dateien ändern.

Prüfe:
- Wurde kein fixer Zielpfad vorgegeben?
- Wurde keine Implementierung geplant, die sofort Code ändert?
- Gibt es keine Legacy-Migration?
- Gibt es keine optionale Admin-Migration?
- Wird MarkerRegistry.lastByType nicht als Basis verwendet?
- Gibt es role + markerName als Staging-Schlüssel?
- Sind requiredMarkers, markerRoles, markerAssignments sauber getrennt?
- Ist /knpc marker set <role> <markerName> nur Spawn-Staging?
- Ist /knpc spawn <role> <npcName> sauber definiert?
- Ist Reassign per npcName von npcName-Eindeutigkeit abhängig?
- Wird alte Syntax /knpc marker set <markerName> ohne Role blockiert?
- Gibt es Tests?
- Gibt es keine Safety-Widersprüche?

ERGEBNISFORMAT:
# Review — Marker-v2 Plan

## Urteil
PASS / FAIL / PARTIAL

## Scope-Check
- Dateien geändert: ja/nein
- Fester Zielpfad vorgegeben: ja/nein
- Verbotene Implementierung: ja/nein

## Fix-Ziel
- keine Migration: ja/nein
- kein lastByType-Aufbau: ja/nein
- role-spezifisches Staging: ja/nein
- npcName-Vorbedingung beachtet: ja/nein
- Legacy-Abbau ohne Migration: ja/nein

## Entscheidung
- Fix nötig: ja/nein
- Marker-v2 technische Vorbereitung erlaubt: ja/nein

Wenn FAIL:
Gib direkt den engen Fix-Prompt für den Plan aus.
Fix Prompt Marker-v2 Plan
Fixe nur den Marker-v2 Plan.

ZIEL:
Korrigiere nur die Review-FAIL-Punkte.

NICHT ERLAUBT:
- keine Implementierung
- keine Dateiänderung, außer der Nutzer hat ausdrücklich einen Pfad genannt
- keine Migration ergänzen
- keinen festen Zielpfad ergänzen
- nicht MarkerRegistry.lastByType als Basis verwenden

MUSS GELTEN:
- role + markerName als Staging-Schlüssel
- keine Legacy-Migration
- Reassign per npcName erst nach npcName-Eindeutigkeit

ABSCHLUSS:
- kurz sagen, was korrigiert wurde
- erneuter Review nötig: ja
E) Phase 2 — Marker-v2 technische Vorbereitung
Agent Step 2.1 — Role-spezifisches Spawn-Staging vorbereiten
MODUS:
Enger Marker-v2-Vorbereitungsschritt.

ZIEL:
Eine role-spezifische aktive Spawn-Marker-Struktur einführen.

WICHTIG:
Nicht auf MarkerRegistry.lastByType aufbauen.

ERLAUBTE DATEIEN:
- neue oder bestehende Klasse für Spawn-Staging, falls passend
- Command-/Plugin-Service-Verkabelung nur minimal, falls nötig
- keine MarkerRegistry.lastByType-Nutzung als Architekturgrundlage

NICHT ÄNDERN:
- keine markerAssignments schreiben
- keinen NPC ändern
- keine Role-Datei ändern
- keine JSON-Definition ändern
- keine Legacy-Migration
- kein Reassign
- kein Remove/Clear

AUFGABEN:
1. Struktur für activeSpawnMarkersByRole planen/ergänzen.
2. Schlüssel muss role + markerName sein.
3. MarkerType allein reicht nicht.
4. lastByType darf nicht genutzt werden.
5. Noch keine Marker in NPC-Instanzen schreiben.

TEST:
mvn -q -DskipTests test-compile

ABSCHLUSSBERICHT:
- geänderte Dateien
- Struktur erklärt
- bestätigt: kein lastByType
- Compile-Ergebnis
Review Prompt 2.1
Reviewe nur Step 2.1.

Prüfe:
- Gibt es role-spezifisches Staging?
- Ist Schlüssel role + markerName?
- Wird MarkerRegistry.lastByType nicht als Basis genutzt?
- Werden keine markerAssignments geschrieben?
- Wird kein NPC geändert?
- Keine Migration?
- Kein Reassign?
- Compile grün?

ERGEBNIS:
PASS / FAIL / PARTIAL

Wenn FAIL:
engen Fix-Prompt für Step 2.1 ausgeben.
Fix Prompt 2.1
Fixe nur Step 2.1.

ZIEL:
Role-spezifisches Spawn-Staging ohne MarkerRegistry.lastByType.

NICHT ÄNDERN:
markerAssignments, NPC-State, Reassign, Migration, Remove/Clear.

TEST:
mvn -q -DskipTests test-compile
Agent Step 2.2 — /knpc marker set <role> <markerName> als Spawn-Staging
MODUS:
Enger Command-Step.

ZIEL:
/knpc marker set <role> <markerName> setzt aktive Spawn-Marker für diese Role.

ERLAUBTE DATEIEN:
- MarkerSetCommand.java
- role-spezifischer Spawn-Staging-Service
- Definition/Registry-Lookup nur minimal, falls nötig

NICHT ÄNDERN:
- keine markerAssignments
- kein NPC-State
- kein Reassign
- keine Migration
- kein Remove/Clear
- keine Role-Dateien
- keine JSON-Definitionen

AUFGABEN:
1. role aus geladener Registry prüfen.
2. markerName gegen requiredMarkers der Role prüfen.
3. markerRoles[markerName] prüfen.
4. MarkerType aus markerRoles ableiten.
5. Marker an Spielerposition erzeugen.
6. Marker nur im role-spezifischen Spawn-Staging speichern.
7. /knpc marker set <markerName> ohne Role blockieren.

TEST:
mvn -q -DskipTests test-compile
Review Prompt 2.2
Reviewe nur Step 2.2.

Prüfe:
- Ist /knpc marker set <role> <markerName> aktiv?
- Wird role aus Registry gelesen?
- Wird markerName gegen requiredMarkers geprüft?
- Wird markerRoles geprüft?
- Wird MarkerType aus markerRoles abgeleitet?
- Schreibt der Command keine markerAssignments?
- Ändert der Command keinen NPC?
- Wird /knpc marker set <markerName> ohne Role blockiert?
- Kein lastByType?
- Compile grün?

ERGEBNIS:
PASS / FAIL / PARTIAL

Wenn FAIL:
engen Fix-Prompt für Step 2.2 ausgeben.
Fix Prompt 2.2
Fixe nur Step 2.2.

ZIEL:
/knpc marker set <role> <markerName> ist Spawn-Staging.

NICHT ÄNDERN:
NPC-State, markerAssignments, Reassign, Migration, Remove/Clear.

MUSS GELTEN:
role aus Registry.
markerName aus requiredMarkers.
MarkerType aus markerRoles.
Kein lastByType.
Alte Ein-Argument-Syntax blockieren.

TEST:
mvn -q -DskipTests test-compile
Agent Step 2.3 — Shared-Marker-/Ownership-Precheck für Role-Staging vorbereiten
MODUS:
Enger Safety-Vorbereitungsstep.

ZIEL:
Bevor Spawn Role-Staging verbrauchen darf, muss ein sicherer Precheck existieren, der geteilte Marker erkennt und blockiert.

WARUM:
Spawn darf niemals auch nur zwischenzeitlich Marker in markerAssignments kopieren, die bereits einem anderen NPC gehören.

VORAUSSETZUNG:
Step 1.1 npcName-Eindeutigkeit PASS.
Step 2.1 und 2.2 PASS.

ERLAUBTE DATEIEN:
- SpawnNpcCommand.java
- MarkerAssignment-/NpcRecord-Lookup, falls direkt nötig
- role-spezifischer Spawn-Staging-Service, falls direkt nötig
- kleiner Ownership-/MarkerAssignment-Helper, falls direkt nötig

NICHT ÄNDERN:
- noch kein Spawn-Verbrauch von Role-Staging
- keine markerAssignments schreiben
- keinen NPC erzeugen oder ändern
- kein Reassign
- kein Marker clear
- kein Remove/Clear
- keine Legacy-Migration
- kein Ersatzmarker
- kein getNextAvailable
- kein lastByType

AUFGABEN:
1. Vorbereiten/ergänzen einer Prüfung, die für aktive Role-Marker erkennt, ob markerId bereits in markerAssignments eines anderen NPC genutzt wird.
2. Ownership-Prüfung muss markerName + markerId sauber betrachten.
3. Wenn Marker schon genutzt wird: Ergebnis muss blockierend sein.
4. Keine automatische Ersatzsuche.
5. Keine Mutation als Teil dieses Checks.
6. Klare Fehler-/Admin-Meldung für späteren Spawnpfad vorbereiten oder direkt in vorhandenen Precheck integrieren.

TEST:
mvn -q -DskipTests test-compile
Review Prompt 2.3
Reviewe nur Step 2.3.

Prüfe:
- Gibt es einen Shared-Marker-/Ownership-Precheck vor Spawn-Verbrauch?
- Erkennt der Check markerIds, die bereits in markerAssignments eines anderen NPC genutzt werden?
- Blockiert der Check geteilte Marker?
- Ist der Check read-only, solange kein Spawn durchgeführt wird?
- Werden keine markerAssignments geschrieben?
- Wird kein NPC erzeugt oder geändert?
- Keine Ersatzsuche?
- Kein getNextAvailable?
- Kein lastByType?
- Keine Migration?
- Kein Reassign?
- Compile grün?

ERGEBNIS:
PASS / FAIL / PARTIAL

Wenn FAIL:
engen Fix-Prompt für Step 2.3 ausgeben.
Fix Prompt 2.3
Fixe nur Step 2.3.

ZIEL:
Shared-Marker-/Ownership-Precheck für Role-Staging vorbereiten, bevor Spawn Role-Staging verbrauchen darf.

NICHT ÄNDERN:
Spawn-Verbrauch, markerAssignments-Schreibpfad, Reassign, Remove/Clear, Migration, Legacy-Abbau, Navigation/Door.

MUSS GELTEN:
- markerId bereits in anderem NPC markerAssignments => blockierendes Ergebnis
- keine Ersatzsuche
- kein getNextAvailable
- kein lastByType
- keine Mutation im Precheck

TEST:
mvn -q -DskipTests test-compile
Agent Step 2.4 — Spawn verbraucht aktive Role-Marker nur nach Shared-Marker-Check
MODUS:
Enger Spawn-Step.

ZIEL:
/knpc spawn <role> <npcName> kopiert aktive Role-Marker in markerAssignments, aber nur wenn der Shared-Marker-/Ownership-Precheck PASS/blockierungsfrei ist.

VORAUSSETZUNG:
Step 1.1 npcName-Eindeutigkeit PASS.
Step 2.1, 2.2 und 2.3 PASS.

ERLAUBTE DATEIEN:
- SpawnNpcCommand.java
- NpcRecord / State-Modell, falls markerAssignments ergänzt werden müssen
- role-spezifischer Spawn-Staging-Service
- Persistence-Modell, falls direkt nötig
- MarkerAssignment-/Ownership-Helper, falls direkt nötig

NICHT ÄNDERN:
- kein Reassign
- kein Marker clear
- kein Remove/Clear
- keine Legacy-Migration
- kein getNextAvailable
- kein lastByType
- keine automatische Ersatzsuche

AUFGABEN:
1. Spawn-Syntax /knpc spawn <role> <npcName> prüfen.
2. npcName eindeutig prüfen.
3. Alle requiredMarkers aus Role-Staging verlangen.
4. Vor dem Schreiben der markerAssignments den Shared-Marker-/Ownership-Precheck aus Step 2.3 ausführen.
5. Wenn ein aktiver Role-Marker bereits einem anderen NPC gehört: Spawn blockieren.
6. Keine automatische Ersatzsuche.
7. Aktive Role-Marker nur nach bestandenem Precheck in markerAssignments des neuen NPC kopieren.
8. Save prüfen.
9. Role-Staging erst nach erfolgreichem Save leeren.
10. Bei Save-Failure keine Erfolgsmeldung.

TEST:
mvn -q -DskipTests test-compile
Review Prompt 2.4
Reviewe nur Step 2.4.

Prüfe:
- Verbraucht Spawn Role-Staging nur nach Shared-Marker-/Ownership-Check?
- Werden bereits genutzte Marker erkannt?
- Wird Spawn bei geteilten Markern blockiert?
- Werden markerAssignments nur beim konkreten neuen NPC geschrieben?
- Wird Staging erst nach erfolgreichem Save geleert?
- Blockiert fehlende requiredMarkers?
- Blockiert doppelter npcName?
- Keine Ersatzsuche?
- Kein getNextAvailable?
- Kein lastByType?
- Keine Shared Marker?
- Keine Migration?
- Compile grün?

ERGEBNIS:
PASS / FAIL / PARTIAL

Wenn FAIL:
engen Fix-Prompt für Step 2.4 ausgeben.
Fix Prompt 2.4
Fixe nur Step 2.4.

ZIEL:
Spawn kopiert aktive Role-Marker sicher in markerAssignments und leert Staging nur nach erfolgreichem Save. Geteilte Marker müssen vor dem Kopieren blockiert werden.

NICHT ÄNDERN:
Reassign, Remove/Clear, Migration, Legacy-Abbau, Navigation/Door.

MUSS GELTEN:
- kein Spawn mit geteilten Markern
- markerAssignments erst nach bestandenem Shared-Marker-Check
- Role-Staging erst nach erfolgreichem Save leeren
- keine Ersatzsuche
- kein getNextAvailable
- kein lastByType

TEST:
mvn -q -DskipTests test-compile
F) Phase 3 — Reassign per npcName
Agent Step 3.1 — Reassign per npcName
MODUS:
Enger Reassign-Step.

VORAUSSETZUNG:
Step 1.1 npcName-Eindeutigkeit PASS.

ZIEL:
/knpc marker set <markerName> <npcName> ersetzt MarkerAssignment eines existierenden NPC.

ERLAUBTE DATEIEN:
- MarkerSetCommand.java
- MarkerAssignment-/NpcRecord-Service
- MarkerRegistry, falls direkt nötig

NICHT ÄNDERN:
- kein Spawn-Staging umbauen
- keine Migration
- kein Remove/Clear
- keine Legacy-Löschung
- keine Role-Dateien
- keine JSON-Definitionen

AUFGABEN:
1. npcName eindeutig finden.
2. NPC role lesen.
3. markerName gegen requiredMarkers dieser Role prüfen.
4. markerRoles[markerName] lesen.
5. MarkerType daraus ableiten.
6. neuen Marker an Spielerposition erzeugen.
7. markerAssignments[markerName] ersetzen.
8. Save prüfen.
9. Bei Save-Failure keine Erfolgsmeldung.
10. Alte Marker nur löschen, wenn eindeutig diesem NPC gehörend und sicher entfernbar.
11. Wenn Syntax sowohl Spawn-Staging als auch Reassign sein könnte:
    AMBIGUOUS blockieren.

TEST:
mvn -q -DskipTests test-compile
Review Prompt 3.1
Reviewe nur Step 3.1.

Prüfe:
- Ist npcName-Eindeutigkeit vorher PASS?
- Findet Reassign nur eindeutigen npcName?
- Prüft markerName gegen Role-requiredMarkers?
- Nutzt markerRoles für MarkerType?
- Schreibt nur markerAssignments dieses NPC?
- Save-Failure wird nicht als Erfolg gemeldet?
- Ambiguous Syntax blockiert?
- Kein Spawn-Staging kaputt?
- Keine Migration?
- Compile grün?

ERGEBNIS:
PASS / FAIL / PARTIAL

Wenn FAIL:
engen Fix-Prompt für Step 3.1 ausgeben.
Fix Prompt 3.1
Fixe nur Step 3.1.

ZIEL:
Reassign per npcName sicher machen.

NICHT ÄNDERN:
Spawn-Staging, Spawn-Verbrauch, Remove/Clear, Migration, Legacy-Abbau.

MUSS GELTEN:
npcName eindeutig.
markerName aus Role.
MarkerType aus markerRoles.
Save-Failure blockiert Erfolg.
AMBIGUOUS blockiert.

TEST:
mvn -q -DskipTests test-compile
G) Phase 4 — Marker clear entfernen/deaktivieren
Agent Step 4.1 — /knpc marker clear deaktivieren
MODUS:
Enger Command-Safety-Step.

ZIEL:
/knpc marker clear darf keine Einzelmarker mehr löschen.

ERLAUBTE DATEIEN:
- MarkerClearCommand.java
- Command-Registrierung, falls direkt nötig
- Help/Syntax-Ausgabe, falls direkt nötig

NICHT ÄNDERN:
- kein Remove/Clear-NPC-Design
- kein Reassign
- keine Migration
- keine Marker-v2-Architektur ändern
- keine Legacy-Felder löschen

AUFGABEN:
1. /knpc marker clear als Einzelmarker-Löschung blockieren oder deaktivieren.
2. Klare Admin-Meldung:
   Marker werden nur mit sicher entferntem NPC gelöscht.
3. Keine Marker entfernen.
4. Keine state.json Änderung.

TEST:
mvn -q -DskipTests test-compile
Review Prompt 4.1
Reviewe nur Step 4.1.

Prüfe:
- Kann /knpc marker clear noch Einzelmarker löschen?
- Wird klare Meldung ausgegeben?
- Wird keine state.json geändert?
- Wird kein Marker entfernt?
- Kein Remove/Clear-Neudesign?
- Keine Migration?
- Compile grün?

ERGEBNIS:
PASS / FAIL / PARTIAL

Wenn FAIL:
engen Fix-Prompt für Step 4.1 ausgeben.
Fix Prompt 4.1
Fixe nur Step 4.1.

ZIEL:
/knpc marker clear darf keine Einzelmarker löschen.

NICHT ÄNDERN:
Remove/Clear-NPC-Design, Reassign, Migration, Legacy-Abbau.

TEST:
mvn -q -DskipTests test-compile
H) Phase 5 — Remove/Clear safe-by-default
Agent Step 5.1 — NPC Remove/Clear entfernt eigene Marker nur bei sicherem Delete
MODUS:
Enger Remove/Clear-Safety-Step.

ZIEL:
Beim sicheren Entfernen eines NPCs werden seine eigenen Marker entfernt.
Unsichere NPC-Zustände blockieren.

ERLAUBTE DATEIEN:
- NpcRemoveCommand.java
- NpcClearCommand.java
- NpcRoutineRunner.java oder Removal-Service, falls dort Removal liegt
- Marker-Cleanup-Service, falls vorhanden

NICHT ÄNDERN:
- kein Marker-Staging
- kein Reassign
- keine Migration
- kein Legacy-Umbau
- keine Navigation/Door

AUFGABEN:
1. ACTIVE/live nur entfernen, wenn Entity-Removal sicher bestätigt ist.
2. NEEDS_RELINK blockieren.
3. MISSING_ENTITY safe-by-default blockieren oder klar gesichert behandeln.
4. AMBIGUOUS blockieren.
5. Ungeladene Chunks blockieren.
6. Record nicht löschen, wenn Entity-Removal unsicher ist.
7. Eigene Marker nur löschen, wenn NPC sicher entfernt wurde.
8. Keine fremden Marker löschen.

TEST:
mvn -q -DskipTests test-compile
Review Prompt 5.1
Reviewe nur Step 5.1.

Prüfe:
- Löscht Remove/Clear Record nur bei sicherem Entity-Removal?
- Blockiert NEEDS_RELINK?
- Blockiert AMBIGUOUS?
- Löscht Marker nur bei sicher entferntem NPC?
- Werden fremde Marker geschützt?
- Keine Migration?
- Kein Reassign?
- Compile grün?

ERGEBNIS:
PASS / FAIL / PARTIAL

Wenn FAIL:
engen Fix-Prompt für Step 5.1 ausgeben.
Fix Prompt 5.1
Fixe nur Step 5.1.

ZIEL:
Remove/Clear safe-by-default.
Eigene Marker nur nach sicherem NPC-Delete entfernen.

NICHT ÄNDERN:
Spawn-Staging, Reassign, Migration, Legacy-Abbau, Navigation/Door.

TEST:
mvn -q -DskipTests test-compile
I) Phase 6 — Legacy-Abbau ohne Migration
Agent Step 6.1 — Legacy-Markerfelder nicht mehr als aktive Wahrheit nutzen
MODUS:
Enger Legacy-Abbau-Step.

ZIEL:
Legacy-Markerfelder dürfen nicht mehr als aktive Marker-Wahrheit genutzt werden.

WICHTIG:
Keine Migration.
Keine Admin-Migration.
Keine automatische Load-Migration.
Alte Welten werden nicht unterstützt.

ERLAUBTE DATEIEN:
- NpcRecord.java
- JsonPersistedModels.java
- JsonFileStateStore.java
- Marker-Resolver/Assignment-Service, falls direkt nötig

NICHT ÄNDERN:
- kein /knpc marker migrate
- kein Dry-run-Migrate
- kein Apply-Migrate
- kein stilles Umschreiben beim Load
- kein Spawn-Staging ändern
- kein Reassign ändern
- kein Remove/Clear ändern

AUFGABEN:
1. Prüfen, ob Legacy-Felder noch aktive Wahrheit sind.
2. Wenn ja: aktive Nutzung entfernen oder blockieren.
3. Legacy-State beim Load nicht still migrieren.
4. Legacy-State klar als inkompatibel/unsupported melden.
5. Kein automatisches Speichern eines migrierten Zustands.

TEST:
mvn -q -DskipTests test-compile
Review Prompt 6.1
Reviewe nur Step 6.1.

Prüfe:
- Gibt es keine Legacy-Migration?
- Gibt es keine Admin-Migration?
- Gibt es kein automatisches Load-Umschreiben?
- Werden Legacy-Felder nicht als aktive Marker-Wahrheit genutzt?
- Wird Legacy-State klar blockiert/unsupported gemeldet?
- Keine Spawn/Reassign/Remove-Nebenänderung?
- Compile grün?

ERGEBNIS:
PASS / FAIL / PARTIAL

Wenn FAIL:
engen Fix-Prompt für Step 6.1 ausgeben.
Fix Prompt 6.1
Fixe nur Step 6.1.

ZIEL:
Legacy-Markerfelder nicht mehr als aktive Wahrheit.

NICHT ERLAUBT:
Migration, Admin-Migration, Dry-run, Apply, automatisches Load-Umschreiben.

TEST:
mvn -q -DskipTests test-compile
J) Phase 7 — Final Safety-Doku und Patchreport
Agent Step 7.1 — Safety-Doku und Patchreport final aktualisieren
MODUS:
Finaler Markdown-Step.

STARTBEDINGUNG:
Alle vorherigen Steps sind PASS oder berechtigt NOOP.

ZIEL:
Safety-Doku und Patchreport aktualisieren.

ERLAUBTE DATEIEN:
- docs/safety/npc_restart_relink_control.md
- docs/safety/json_hierarchy.md
- docs/patch_reports/<timestamp>_<thema>-Patch.md

NICHT ÄNDERN:
- kein Java-Code
- keine JSON-Ressourcen
- keine Marker-v2-Implementierung
- keine Migration

AUFGABEN:
1. Widerspruchs-Check zwischen:
   - aktuellem Code
   - neuesten Patchreports
   - AGENTS.md
   - safety/*.md
   - NPCMod_Lagebericht
2. Wenn echter Konflikt:
   REGELKONFLIKT GEFUNDEN
   keine stille Entscheidung
3. Wenn nur Versionsdrift:
   Safety-Doku gezielt aktualisieren.
4. Dokumentieren:
   - Phase-0-Steps waren Regression-NOOP/PASS
   - keine Legacy-Migration
   - kein MarkerRegistry.lastByType für Role-Staging
   - npcName-Eindeutigkeit als Reassign-Voraussetzung
   - Marker-v2 nur nach Plan/Steps
5. Patchreport schreiben.

DOKU-PRÜFUNG:
Kein Maven-Compile nötig, wenn nur Markdown geändert wurde.
Review Prompt 7.1
Reviewe nur Step 7.1.

Prüfe:
- Lief der Step wirklich zuletzt?
- Wurden nur Safety-Doku und Patchreport geändert?
- Kein Java-Code?
- Keine JSON-Ressourcen?
- Keine Migration dokumentiert oder eingebaut?
- Kein lastByType als Role-Staging-Basis?
- npcName-Eindeutigkeit korrekt als Reassign-Voraussetzung dokumentiert?
- Regelkonflikte nicht still entschieden?
- Patchreport vorhanden?

ERGEBNIS:
PASS / FAIL / PARTIAL

Wenn FAIL:
engen Fix-Prompt für Step 7.1 ausgeben.
Fix Prompt 7.1
Fixe nur Step 7.1.

ERLAUBTE DATEIEN:
- betroffene Safety-Doku
- betroffener Patchreport

NICHT ÄNDERN:
Java, JSON, Marker-v2-Code, Migration.

ZIEL:
Nur Doku-/Patchreport-Fehler korrigieren.
Echte Regelkonflikte nicht still entscheiden.

Wenn Konflikt:
REGELKONFLIKT GEFUNDEN ausgeben und stoppen.
K) Fortschritts-Checkliste
[ ] PLAN Mode Prompt geprüft

[ ] Step 0.1 Regression-NOOP geprüft
[ ] Step 0.1 Review PASS/NOOP

[ ] Step 0.2 Regression-NOOP geprüft
[ ] Step 0.2 Review PASS/NOOP

[ ] Step 0.3 Regression-NOOP geprüft
[ ] Step 0.3 Review PASS/NOOP

[ ] Step 1.1 npcName-Eindeutigkeit umgesetzt
[ ] Step 1.1 Review PASS

[ ] Marker-v2 Plan erstellt
[ ] Marker-v2 Plan Review PASS

[ ] Step 2.1 Role-Staging ohne lastByType
[ ] Step 2.1 Review PASS

[ ] Step 2.2 /knpc marker set <role> <markerName>
[ ] Step 2.2 Review PASS

[ ] Step 2.3 Shared-Marker-/Ownership-Precheck vor Spawn-Verbrauch
[ ] Step 2.3 Review PASS

[ ] Step 2.4 Spawn verbraucht Role-Staging nur nach Shared-Marker-Check
[ ] Step 2.4 Review PASS

[ ] Step 3.1 Reassign per npcName
[ ] Step 3.1 Review PASS

[ ] Step 4.1 marker clear deaktivieren
[ ] Step 4.1 Review PASS

[ ] Step 5.1 Remove/Clear safe-by-default
[ ] Step 5.1 Review PASS

[ ] Step 6.1 Legacy-Abbau ohne Migration
[ ] Step 6.1 Review PASS

[ ] Step 7.1 Safety-Doku + Patchreport
[ ] Step 7.1 Review PASS
L) Wichtigste Korrekturen gegenüber dem alten Plan
1. Phase 0 nicht neu fixen.
   Nur Regression-NOOP prüfen.

2. Step 0.2 darf NpcRoutineRunner.java anfassen,
   wenn dort die konkrete Marker-Zuweisung liegt.

3. Legacy-Migration komplett weg.
   Keine automatische Migration.
   Keine Admin-Migration.
   Kein Dry-run.
   Kein Apply.

4. Kein fester Marker-v2-Zielpfad.

5. Role-basierte aktive Marker nicht auf MarkerRegistry.lastByType aufbauen.

6. npcName-Eindeutigkeit als eigener Vor-Step.

7. Reassign per npcName erst nach PASS von npcName-Eindeutigkeit.

8. Wenn npcName-Eindeutigkeit nicht sicher ist:
   Reassign per npcName blockieren oder später per npcId planen.