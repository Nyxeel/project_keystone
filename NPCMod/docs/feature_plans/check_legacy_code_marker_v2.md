Du arbeitest an meiner Hytale-Mod „NPCMod / KeystoneNPC“.

MODUS:
AGENT Step 7 Fix — P2 Marker-Fallback Read-only Trennung.

WICHTIG:
Nur den fehlgeschlagenen Step-7-Fix umsetzen.
Keine neuen Features.
Keine angrenzenden Refactors.
Keine Änderungen an Respawn-/Relink-Policy, Door, Navigation, Animation, JSON-Roles oder Dedupe-Löschlogik.

Ziel:
Restore-/Validation-/Tick-/Diagnose-Pfade dürfen keine MarkerAssignments mutieren.
Marker-Fallback-Zuweisung darf nur in expliziten mutierenden Kontexten passieren, z. B. Spawn/Admin-Zuweisung.

Gefundene Review-Probleme:
1. IdleMarkerService nutzt weiterhin markerResolver.resolveRequiredMarkerWithFallback(...), das auf die mutierende Variante delegiert.
2. Dadurch können Restore- und Tick-Pfade weiterhin MarkerAssignments automatisch ersetzen.
3. RelinkWorkflowService nutzt für Marker-Anker ebenfalls noch die mutierende Fallback-Methode.
4. Die Trennung read-only vs. assigning ist dadurch noch nicht vollständig sicher.

Erlaubte Dateien:
- src/main/java/keystone/npc/routine/marker/MarkerResolver.java
- src/main/java/keystone/npc/routine/marker/IdleMarkerService.java
- src/main/java/keystone/npc/routine/NpcRoutineRunner.java
- src/main/java/keystone/npc/relink/RelinkWorkflowService.java nur für read-only Marker-Anker, ohne Dedupe-/Relink-Policy zu ändern
- src/main/java/keystone/npc/routine/state/StateTargetingService.java nur falls nötig
- src/main/java/keystone/npc/recovery/RespawnRecoveryService.java nur falls nötig

Pflicht-Fixes:

1. IdleMarkerService read-only machen

In IdleMarkerService folgende Methoden auf read-only Marker-Resolve umstellen:

- resolveStatePreferredMarker(...)
- enforceAuthoritativeIdlePosition(...)
- addRestoreMarkerCandidate(...)

Ersetze dort:

markerResolver.resolveRequiredMarkerWithFallback(...)

durch:

markerResolver.resolveRequiredMarkerReadOnly(...)

Wichtig:
Diese Methoden dürfen keine MarkerAssignments verändern.

2. Restore darf keinen Marker automatisch ersetzen

normalizeRestorePosition(...) darf beim Restore nur bereits persistierte gültige MarkerAssignments lesen.

Wenn ein Marker fehlt/ungültig ist:
- kein Fallback-Assign
- keine automatische neue MarkerId setzen
- kein persistenzwirksamer Ersatz

3. Tick darf keinen Marker automatisch ersetzen

NpcTickPipeline / idle-state-check / idle-marker-authority darf über IdleMarkerService keine MarkerAssignments ändern.

Wenn Marker fehlt:
- keine automatische Ersatz-Zuweisung
- bestehende Missing-/Pause-/Warnlogik nutzen

4. Relink/Dedupe-Anker read-only machen

In RelinkWorkflowService.addMarkerAnchor(...) oder vergleichbarer Marker-Anker-Logik:

- keine mutierende Fallback-Methode verwenden
- nur resolveRequiredMarkerReadOnly(...)
- keine Dedupe-Löschlogik ändern
- keine Relink-Policy ändern

5. Alte Kompatibilitätsmethode absichern oder klar begrenzen

Prüfe alle Aufrufer von:

resolveRequiredMarkerWithFallback(...)

Nach dem Fix darf diese Methode nicht mehr in Restore-/Validation-/Tick-/Diagnose-/Relink-Anker-Pfaden genutzt werden.

Erlaubt bleibt mutierend nur explizit:

resolveRequiredMarkerWithFallbackAssigning(...)

in Spawn/Admin-Zuweisungskontexten.

6. Explizite Spawn-/Admin-Zuweisungen erhalten

Nicht beschädigen:
- Spawn darf weiterhin explizit mutierend required Marker initial zuweisen, falls das aktuell so vorgesehen ist.
- Admin Marker Set darf weiterhin explizit Marker zuweisen.
- Bereits vorhandene MarkerAssignments bleiben erhalten.

Nicht ändern:
- Respawn-/Relink-Policy
- respawnAfterRestart
- Remove/Clear
- Dedupe-Löschlogik
- Door
- Navigation
- Animation
- JSON-Roles
- Marker-v2

Nach der Änderung:
- Suche nach resolveRequiredMarkerWithFallback(...)
- Berichte alle verbleibenden Aufrufer und warum sie erlaubt sind
- mvn -q -DskipTests test-compile ausführen
- keine weiteren Features starten

Abschlussbericht:
1. Geänderte Dateien
2. Welche Restore-/Tick-/Diagnose-Pfade jetzt read-only sind
3. Welche mutierenden Pfade bewusst erlaubt bleiben
4. Warum beim Restart kein Marker automatisch ersetzt und gespeichert wird
5. Ob Respawn-/Relink-Policy unverändert blieb
6. Ob Dedupe-Löschlogik unverändert blieb
7. Compile-Ergebnis
8. Ob Review Step 7 erneut gestartet werden darf


Es gibt zwei alte gefährliche Marker-Pfade:

1. Alter Fallback-Resolve
resolveRequiredMarkerWithFallback(...)

Problem:
Sucht Ersatzmarker und kann MarkerAssignments heimlich ändern.

2. Alte Reconcile-/Cleanup-Logik
reconcilePersistedMarkerAssignments(...)

Problem:
Entfernt MarkerAssignments automatisch, wenn sie nicht mehr zur aktuellen Definition passen.