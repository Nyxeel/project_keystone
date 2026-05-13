# Phase 0 — Sofortige Stabilitätsfixes vor Marker-v2

**Projekt:** NPCMod / KeystoneNPC  
**Ziel:** Kleine, sichere P1-Fixes vor Marker-v2  
**Modus:** Immer nur ein kleiner Step, danach Review, dann erst nächster Step  
**Status:** Prompt-Arbeitsset für Coder / Agent

---

# 0. Grundregel für den Coder

Du arbeitest an meiner Hytale-Mod **NPCMod / KeystoneNPC**.

Diese Phase ist **Pflicht vor Marker-v2**.

Die bisherigen Patches waren lang dokumentiert, wurden aber in kleinen Steps mit Review umgesetzt. Genau so soll es weitergehen:

```text
1 kleiner Step
→ Compile
→ Review
→ bei FAIL nur diesen Step fixen
→ erst bei PASS nächster Step
```

Wichtig:

- Nicht alles auf einmal ändern.
- Keine großen Refactors.
- Keine neuen Features.
- Kein Marker-v2 in Phase 0.
- Keine neue markerAssignments-Hauptarchitektur.
- Keine Legacy-Felder löschen.
- Keine Door-/Navigation-/Animation-/Role-Refactors.
- Keine Respawn-/Relink-Policy umbauen.
- Keine neue Engine-Identity über roleName.
- Kein `setRoleName("KeystoneNPC_...")`.
- Kein Role-Prefix-Fallback.
- Kein Blind-Respawn.
- Kein Erfolg melden, wenn Save oder Rollback fehlgeschlagen ist.

Wenn ein Problem schon behoben ist:

```text
Keine Datei ändern.
Nur begründet PASS / already fixed melden.
```

Wenn etwas unklar ist:

```text
nicht löschen
nicht spawnen
nicht relinken
nicht überschreiben
nur warnen / blockieren / melden
```

Pflicht nach jeder Java-Änderung:

```bash
mvn -q -DskipTests test-compile
```

---

# 1. Phase-0-Ziel

Phase 0 ist keine Feature-Phase.

Phase 0 räumt die letzten kleinen Safety-Lücken auf, damit Marker-v2 später nicht auf wackeligem Fundament gebaut wird.

Marker-v2 darf erst starten, wenn diese Punkte PASS sind:

```text
[ ] NpcRespawnMissingCommand meldet keinen Erfolg bei Save-Failure.
[ ] Marker-Zuweisung prüft markerId, MarkerType und worldId hart.
[ ] SpawnNpcCommand wertet Save-Failure-Rollback sauber aus.
[ ] Safety-Doku widerspricht dem aktuellen Marker-Resolver-Stand nicht.
[ ] mvn -q -DskipTests test-compile ist grün.
```

---

# 2. Arbeitsweise pro Step

Jeder Step besteht aus drei Teilen:

```text
A) Agent-/Coder-Prompt
B) Review-Prompt
C) Fix-Prompt bei FAIL
```

Regel:

```text
Nach A immer B ausführen.
Wenn B FAIL meldet, nur C für diesen Step ausführen.
Danach denselben Review nochmal.
```

---

# Step 0.1 — NpcRespawnMissingCommand Save-Failure prüfen

**Block:** 8 Command-System + 3 Persistence + 4 NPC-Lebenszyklus  
**Priorität:** P1  
**Scope:** sehr klein

## A) Agent-/Coder-Prompt

```text
Du arbeitest an meiner Hytale-Mod „NPCMod / KeystoneNPC“.

MODUS:
Enger Safety-Fix.
Nur Step 0.1.

Ziel:
Härte NpcRespawnMissingCommand gegen Save-Failure.

Problem:
Wenn ein Respawn-/Recovery-Command den Runtime- oder Record-State ändert, aber saveStateSafely() fehlschlägt, darf der Command keinen normalen Erfolg melden.

Prüfe:
- NpcRespawnMissingCommand
- alle Stellen, an denen nach result.stateChanged() und !dryRun gespeichert wird
- alle direkten oder indirekten Aufrufe von plugin.saveStateSafely()

Fix-Ziel:
Wenn result.stateChanged() und !dryRun:
- saveStateSafely() muss geprüft werden
- bei false: klare Fehlermeldung ausgeben
- keine normale Erfolgsmeldung wie „Respawn complete“ / „done“ / „success“
- wenn Runtime geändert wurde, aber Save fehlschlug: ehrlich melden, dass Runtime und state.json auseinanderlaufen können

Nicht ändern:
- keine Respawn-Policy ändern
- kein Relink-Refactor
- kein Marker-v2
- keine Marker-Architektur ändern
- kein Command-Redesign
- kein neues Recovery-System
- kein Auto-Respawn-Verhalten ändern
- kein Role-Prefix-Fallback
- kein dynamisches setRoleName("KeystoneNPC_...")

Wenn der Code bereits sicher ist:
- keine Datei ändern
- begründet melden, welche Prüfung schon vorhanden ist

Pflicht:
- mvn -q -DskipTests test-compile

Abschlussbericht:
- PASS / FAIL / PARTIAL
- geänderte Dateien
- wo saveStateSafely() geprüft wird
- ob Save-Failure noch Erfolg melden kann
- ob Marker-v2 unangetastet blieb
- Compile-Ergebnis
```

## B) Review-Prompt

```text
Reviewe ausschließlich Step 0.1 — NpcRespawnMissingCommand Save-Failure.

Nicht implementieren.
Keine neuen Features.

Prüfe:
1. Wird saveStateSafely() geprüft, wenn result.stateChanged() und !dryRun?
2. Meldet der Command bei Save-Failure keine normale Erfolgsmeldung?
3. Gibt es eine klare Fehlermeldung bei Save-Failure?
4. Wird ehrlich gemeldet, falls Runtime geändert wurde, aber state.json nicht gespeichert werden konnte?
5. Wurde keine Respawn-Policy geändert?
6. Wurde kein Relink-Refactor gemacht?
7. Wurde kein Marker-v2 eingebaut?
8. Wurde kein Command-Redesign gemacht?
9. War mvn -q -DskipTests test-compile erfolgreich?

Harte FAIL-Frage:
Kann der Command nach Save-Failure noch normal „Respawn complete“ oder vergleichbaren Erfolg melden?
Wenn ja: FAIL.

Ergebnisformat:
- PASS / FAIL / PARTIAL
- Probleme
- Fix nötig: ja/nein
- nächster Step erlaubt: ja/nein
```

## C) Fix-Prompt bei FAIL

```text
Fixe ausschließlich Step 0.1.

Problem aus Review:
NpcRespawnMissingCommand kann nach Save-Failure noch Erfolg melden oder prüft saveStateSafely() nicht sauber.

Erlaubter Fix:
- saveStateSafely() Rückgabe prüfen
- bei false: Fehler melden und normalen Erfolgsfluss abbrechen
- keine Policy ändern
- keine anderen Commands ändern, außer direkt nötig für denselben Save-Failure-Pfad

Nicht ändern:
- kein Marker-v2
- kein Relink-/Respawn-Policy-Umbau
- kein Command-Redesign

Danach:
- mvn -q -DskipTests test-compile
- kurzen Fixbericht ausgeben
```

---

# Step 0.2 — Marker-Zuweisung mit worldId/type/id-Gate härten

**Block:** 7 Marker-System + 3 Persistence + 8 Command-System  
**Priorität:** P1  
**Scope:** klein

## A) Agent-/Coder-Prompt

```text
Du arbeitest an meiner Hytale-Mod „NPCMod / KeystoneNPC“.

MODUS:
Enger Safety-Fix.
Nur Step 0.2.

Ziel:
Härte Marker-Zuweisung, damit ein NPC keinen falschen Marker bekommt.

Problem:
MarkerSetCommand setzt Marker in der Spieler-Welt. Der Ziel-NPC könnte aber theoretisch in einer anderen worldId sein. Außerdem muss assignMarkerToNpc(...) hart prüfen, ob markerId, MarkerType und worldId passen.

Prüfe:
- MarkerSetCommand
- NpcRoutineRunner.assignMarkerToNpc(...)
- MarkerRegistry Lookup
- NpcRecord worldId
- MarkerRecord worldId
- Role-/Definition-Regeln für erlaubte MarkerTypes

Fix-Ziel:
assignMarkerToNpc(...) darf nur schreiben, wenn:
- markerId existiert wirklich in MarkerRegistry
- marker.type passt zum gewünschten MarkerType
- marker.worldId == npc.worldId
- MarkerType ist für diese roleId laut requiredMarkers/markerRoles erlaubt
- No-Op wird sauber erkannt
- keine kaputte markerId in NPC-State geschrieben wird
- Save-Failure wird nicht als Erfolg gemeldet

Wichtig:
Die Methode darf nicht „Reroute gestartet“ mit „Assignment erfolgreich“ verwechseln.
Trenne logisch:
- Assignment erfolgreich?
- Marker wirklich geändert?
- Reroute gestartet?
- Save erfolgreich?

Nicht ändern:
- kein Marker-v2
- keine neue markerAssignments-Map als Hauptarchitektur
- keine Legacy-Felder löschen
- keine Reconcile-Änderung
- keine Door-/Navigation-Änderung
- keine Respawn-/Relink-Änderung

Wenn der Code bereits sicher ist:
- keine Datei ändern
- begründet melden, wo die Gates bereits geprüft werden

Pflicht:
- mvn -q -DskipTests test-compile

Abschlussbericht:
- PASS / FAIL / PARTIAL
- geänderte Dateien
- geprüfte Marker-Gates
- ob worldId-Mismatch blockiert wird
- ob Marker-v2 unangetastet blieb
- Compile-Ergebnis
```

## B) Review-Prompt

```text
Reviewe ausschließlich Step 0.2 — Marker-Zuweisung worldId/type/id-Gate.

Nicht implementieren.
Keine neuen Features.

Prüfe:
1. Wird markerId gegen MarkerRegistry geprüft?
2. Wird MarkerType geprüft?
3. Wird marker.worldId == npc.worldId geprüft?
4. Wird geprüft, ob der MarkerType für die roleId erlaubt ist?
5. Wird No-Op erkannt?
6. Kann keine kaputte markerId in den NPC-State geschrieben werden?
7. Werden Assignment-Erfolg, Reroute und Save-Erfolg logisch getrennt?
8. Wird Save-Failure nicht als Erfolg gemeldet?
9. Wurde kein Marker-v2 eingebaut?
10. Wurden keine Legacy-Felder gelöscht?
11. Wurde Reconcile nicht geändert?
12. War mvn -q -DskipTests test-compile erfolgreich?

Harte FAIL-Frage:
Kann ein NPC einen Marker aus einer anderen Welt bekommen?
Wenn ja: FAIL.

Ergebnisformat:
- PASS / FAIL / PARTIAL
- Probleme
- Fix nötig: ja/nein
- nächster Step erlaubt: ja/nein
```

## C) Fix-Prompt bei FAIL

```text
Fixe ausschließlich Step 0.2.

Problem aus Review:
Marker-Zuweisung erlaubt noch falsche markerId, falschen MarkerType oder falsche worldId.

Erlaubter Fix:
- harte Validierung in assignMarkerToNpc(...) oder direkt davor ergänzen
- MarkerRegistry Lookup prüfen
- MarkerType prüfen
- worldId prüfen
- roleId/markerRoles prüfen
- Fehler sauber an Command melden

Nicht ändern:
- kein Marker-v2
- keine markerAssignments-Hauptarchitektur
- keine Legacy-Migration
- keine Reconcile-Änderung

Danach:
- mvn -q -DskipTests test-compile
- kurzen Fixbericht ausgeben
```

---

# Step 0.3 — SpawnNpcCommand Save-Failure-Rollback detaillieren

**Block:** 4 NPC-Lebenszyklus + 8 Command-System  
**Priorität:** P1  
**Scope:** klein

## A) Agent-/Coder-Prompt

```text
Du arbeitest an meiner Hytale-Mod „NPCMod / KeystoneNPC“.

MODUS:
Enger Safety-Fix.
Nur Step 0.3.

Ziel:
SpawnNpcCommand soll bei Save-Failure den Rollback ehrlich und detailliert auswerten.

Problem:
Wenn Spawn erfolgreich eine Live-Entity erzeugt, aber saveStateSafely() fehlschlägt, wird ein Rollback über scheduler.removeNpc(...) versucht. Boolean reicht dafür nicht aus, weil man nicht unterscheiden kann:
- wirklich entfernt
- blockiert
- Entity-Removal unsicher
- Save fehlgeschlagen
- Rollback fehlgeschlagen

Prüfe:
- SpawnNpcCommand
- NpcRoutineRunner.removeNpc(...)
- removeNpcByIndex(...), falls betroffen
- RemoveNpcResult / vorhandene Result-Struktur
- Command-Erfolgsmeldungen nach Save-Failure

Fix-Ziel:
SpawnNpcCommand darf bei Save-Failure nicht behaupten, Rollback sei fertig, wenn removeNpc(...) blockiert oder unsicher war.

Besser:
- RemoveNpcResult auswerten
- removed / blocked / save failed / rollback failed / unsafe outcome klar melden
- bei unsicherem Rollback keine Erfolgsmeldung
- keine Runtime/state.json-Entkopplung verstecken

Nicht ändern:
- kein Spawn-System komplett umbauen
- kein Respawn-System umbauen
- kein Entity-Removal-Redesign
- kein Marker-v2
- keine neue Death-Policy
- keine Dedupe-Policy ändern
- kein Role-Prefix-Fallback

Wenn RemoveNpcResult noch nicht existiert:
- nur minimal einführen oder bestehende Result-Struktur nutzen
- kein großes Removal-Framework bauen

Wenn der Code bereits sicher ist:
- keine Datei ändern
- begründet melden, welches Result geprüft wird

Pflicht:
- mvn -q -DskipTests test-compile

Abschlussbericht:
- PASS / FAIL / PARTIAL
- geänderte Dateien
- wie Save-Failure behandelt wird
- wie Rollback-Ergebnis geprüft wird
- ob unsicherer Rollback noch Erfolg melden kann
- ob Marker-v2 unangetastet blieb
- Compile-Ergebnis
```

## B) Review-Prompt

```text
Reviewe ausschließlich Step 0.3 — SpawnNpcCommand Save-Failure-Rollback.

Nicht implementieren.
Keine neuen Features.

Prüfe:
1. Wird nach Save-Failure das Rollback-Ergebnis detailliert geprüft?
2. Wird nicht nur ein unklarer boolean als sichere Wahrheit behandelt?
3. Meldet der Command removed / blocked / rollback failed / unsafe outcome unterscheidbar?
4. Gibt es keine normale Erfolgsmeldung bei blockiertem oder unsicherem Rollback?
5. Wird kein Spawn-/Respawn-System groß umgebaut?
6. Wurde kein Entity-Removal-Redesign gemacht?
7. Wurde kein Marker-v2 eingebaut?
8. Wurde keine Dedupe-/Role-/Relink-Policy geändert?
9. War mvn -q -DskipTests test-compile erfolgreich?

Harte FAIL-Frage:
Kann Spawn bei Save-Failure noch behaupten, Rollback sei fertig, obwohl removeNpc(...) blockiert oder unsicher war?
Wenn ja: FAIL.

Ergebnisformat:
- PASS / FAIL / PARTIAL
- Probleme
- Fix nötig: ja/nein
- nächster Step erlaubt: ja/nein
```

## C) Fix-Prompt bei FAIL

```text
Fixe ausschließlich Step 0.3.

Problem aus Review:
SpawnNpcCommand wertet Rollback nach Save-Failure nicht ehrlich genug aus.

Erlaubter Fix:
- RemoveNpcResult oder vorhandenes Ergebnis sauber auswerten
- keine Erfolgsmeldung bei BLOCKED / UNSAFE / SAVE_FAILED / ROLLBACK_FAILED
- klare Admin-Fehlermeldung ausgeben

Nicht ändern:
- kein Spawn-/Respawn-Redesign
- kein Marker-v2
- keine Removal-Architektur groß umbauen

Danach:
- mvn -q -DskipTests test-compile
- kurzen Fixbericht ausgeben
```

---

# Step 0.4 — Safety-Doku Versionsdrift bereinigen

**Block:** 9 Safety / Kontrollregeln  
**Priorität:** P1/P2  
**Scope:** Markdown-only

## A) Agent-/Coder-Prompt

```text
Du arbeitest an meiner Hytale-Mod „NPCMod / KeystoneNPC“.

MODUS:
Markdown-only Safety-Doku-Abgleich.
Nur Step 0.4.

Ziel:
Prüfe, ob die Safety-Dokumente denselben Marker-Resolver-Stand wie der aktuelle Code sagen.

Wichtig:
Dieser Step ist Markdown-only.
Kein Java-Code ändern.
Kein Marker-v2.

Prüfe:
- docs/safety/npc_restart_relink_control.md
- docs/safety/json_hierarchy.md
- AGENTS.md, falls dort widersprüchliche alte Regeln stehen

Aktueller Zielstand:
- resolveRequiredMarkerWithFallbackAssigning(...) ist entfernt
- resolveRequiredMarkerWithFallback(...) ist entfernt
- resolveRequiredMarkerReadOnly(...) ist verbindlicher Read-only-Resolver
- MarkerRegistry.getNextAvailable(...) ist deprecated Lookup-Helfer
- getNextAvailable(...) darf nicht in read-only Restore/Tick/Diagnose/Validation/Respawn-Policy-Pfaden genutzt werden
- mutierende Marker-Zuweisung/Reconcile nur in explizitem Spawn/Admin/Repair/Cleanup-Kontext
- read-only Kontexte dürfen markerAssignments und Legacy-Markerfelder niemals mutieren

Aufgabe:
- suche alte Formulierungen, die entfernte Methoden noch als erlaubten Mutationspfad nennen
- suche widersprüchliche Regeln zwischen Safety-Dateien
- falls alles bereits aktuell ist: keine Änderung, PASS melden
- falls Drift existiert: nur Markdown korrigieren

Nicht ändern:
- kein Java-Code
- kein Marker-v2
- keine neuen Regeln ohne Codebezug
- keine Architekturentscheidung neu erfinden

Compile:
- Bei Markdown-only ist Maven-Compile nicht nötig.
- Trotzdem melden: „Markdown-only, kein Compile erforderlich“.

Abschlussbericht:
- PASS / FAIL / PARTIAL
- geänderte Dateien
- gefundene alte Formulierungen
- ob Regelkonflikte gefunden wurden
- ob Java unangetastet blieb
```

## B) Review-Prompt

```text
Reviewe ausschließlich Step 0.4 — Safety-Doku Versionsdrift.

Nicht implementieren.
Keine Java-Änderung.
Kein Marker-v2.

Prüfe:
1. Stimmen npc_restart_relink_control.md und json_hierarchy.md beim Marker-Resolver-Stand überein?
2. Werden resolveRequiredMarkerWithFallbackAssigning(...) und resolveRequiredMarkerWithFallback(...) nicht mehr als erlaubte aktive Mutationspfade beschrieben?
3. Ist resolveRequiredMarkerReadOnly(...) als verbindlicher Read-only-Resolver beschrieben?
4. Ist getNextAvailable(...) als deprecated / nicht read-only-fähiger Fallback beschrieben?
5. Ist klar, dass read-only Pfade nicht mutieren dürfen?
6. Ist klar, dass mutierende Marker-Zuweisung nur in Spawn/Admin/Repair/Cleanup erlaubt ist?
7. Wurde kein Java-Code geändert?
8. Wurde kein Marker-v2 geplant oder eingebaut?

Harte FAIL-Frage:
Gibt es noch alte Formulierungen, die entfernte Methoden als erlaubten Mutationspfad nennen?
Wenn ja: FAIL.

Ergebnisformat:
- PASS / FAIL / PARTIAL
- Probleme
- Fix nötig: ja/nein
- nächster Step erlaubt: ja/nein
```

## C) Fix-Prompt bei FAIL

```text
Fixe ausschließlich Step 0.4.

Problem aus Review:
Safety-Doku enthält alte oder widersprüchliche Marker-Resolver-Regeln.

Erlaubter Fix:
- nur Markdown anpassen
- entfernte Methoden als entfernt dokumentieren
- read-only Resolver klar als verbindlich dokumentieren
- getNextAvailable(...) klar als deprecated/nicht für read-only Pfade dokumentieren
- keine neue Architektur erfinden

Nicht ändern:
- kein Java-Code
- kein Marker-v2

Danach:
- kurzen Markdown-Fixbericht ausgeben
```

---

# 3. Marker-v2 Startfreigabe

Marker-v2 darf erst begonnen werden, wenn Phase 0 vollständig PASS ist.

## Marker-v2 darf starten, wenn:

```text
[ ] Step 0.1 PASS
[ ] Step 0.2 PASS
[ ] Step 0.3 PASS
[ ] Step 0.4 PASS oder bereits aktuell bestätigt
[ ] keine Regelkonflikte zwischen AGENTS.md und safety/*.md
[ ] kein Save-Failure wird als Erfolg gemeldet
[ ] kein NPC kann Marker aus anderer Welt bekommen
[ ] Spawn-Rollback nach Save-Failure wird ehrlich gemeldet
[ ] Safety-Doku beschreibt den aktuellen Resolver-Stand korrekt
[ ] mvn -q -DskipTests test-compile ist nach Java-Steps erfolgreich
```

## Marker-v2 darf noch NICHT starten, wenn:

```text
[ ] irgendein Phase-0-Step FAIL oder PARTIAL ist
[ ] Commands noch falsche Erfolge melden können
[ ] assignMarkerToNpc(...) noch falsche worldId/type/id akzeptiert
[ ] Save-Failure noch ignoriert wird
[ ] Safety-Doku alte entfernte Resolver als erlaubt beschreibt
[ ] ein Review unklare Runtime/state.json-Drift meldet
```

---

# 4. Was nach Phase 0 kommt

Erst nach Phase 0 beginnt Marker-v2 als eigene Phase.

Marker-v2 sollte dann nicht direkt alles ändern, sondern wieder klein:

```text
Marker-v2 Phase 1: Datenmodell planen, keine Migration
Marker-v2 Phase 2: markerAssignments parallel lesbar machen
Marker-v2 Phase 3: Schreibpfad kontrolliert umstellen
Marker-v2 Phase 4: Legacy-Felder nur kompatibel halten, nicht sofort löschen
Marker-v2 Phase 5: Migration mit Backup/Partial-Load/Save-Failure-Schutz
```

Wichtig:

```text
Marker-v2 ist eine Feature-Migration.
Phase 0 ist eine Safety-Stabilisierung.
Diese beiden Dinge nicht vermischen.
```

---

# 5. Kurze End-Checkliste für den Coder

Vor Abschluss von Phase 0 beantworten:

```text
[ ] Wurde wirklich nur Phase 0 geändert?
[ ] Wurde Marker-v2 nicht eingebaut?
[ ] Wurden keine Legacy-Markerfelder gelöscht?
[ ] Prüft NpcRespawnMissingCommand Save-Failure?
[ ] Prüft assignMarkerToNpc markerId/type/worldId/role-Erlaubnis?
[ ] Meldet SpawnNpcCommand Rollback-Ergebnisse ehrlich?
[ ] Ist Safety-Doku konsistent?
[ ] Gibt es keine neue Runtime/state.json-Drift ohne Warnung?
[ ] Gibt es keine falsche Erfolgsmeldung bei Save-Failure?
[ ] Gibt es keinen neuen Role-Prefix-Fallback?
[ ] Gibt es kein dynamisches setRoleName("KeystoneNPC_...")?
[ ] War Compile nach Java-Steps erfolgreich?
[ ] Ist Marker-v2 jetzt freigegeben: ja/nein?
```
