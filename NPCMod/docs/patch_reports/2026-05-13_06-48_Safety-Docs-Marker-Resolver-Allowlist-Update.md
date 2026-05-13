# Patch Report: Safety Docs Marker Resolver Allowlist Update

Date/Time: 2026-05-13 06:48
Type: Markdown-only safety documentation synchronization
Scope: docs/safety

## Ziel
Die Safety-Kontrolldateien wurden auf den finalen Code-Zustand synchronisiert, mit Fokus auf Marker-Resolver-Methodenstatus (entfernt/deprecated) und harte Read-only-vs-Mutationsregeln.

## Geaenderte Dateien
- docs/safety/npc_restart_relink_control.md
- docs/safety/json_hierarchy.md

## Was aktualisiert wurde
1. Harte Pflichtregel geschaerft:
- Mutierende Marker-Zuweisung/Reconcile ist nur in explizitem Spawn/Admin/Repair/Cleanup-Kontext erlaubt (Allowlist).
- Read-only-Kontexte duerfen MarkerAssignments und Legacy-Markerfelder niemals mutieren.

2. Methodenstatus dokumentiert (Code-Abgleich):
- resolveRequiredMarkerWithFallbackAssigning(...) entfernt
- resolveRequiredMarkerWithFallback(...) entfernt
- resolveRequiredMarkerReadOnly(...) als verbindlicher Read-only-Resolver
- MarkerRegistry.getNextAvailable(...) bleibt deprecated Lookup-Helfer
- MarkerRingTraversal bleibt internes Registry-Hilfsmittel

3. Review- und Checklisten erweitert:
- Neue Audit-Fragen fuer Allowlist-Einhaltung
- Neue Audit-Fragen fuer entfernte Legacy-Methoden
- Neue Audit-Fragen fuer getNextAvailable(deprecated)-Nutzung

4. Symbol-Audit-Schritte in Safety-Doku ergaenzt:
- grep/rg-basierte Pruefung der Legacy-Symbole
- Erwartungstext fuer zulassige Trefferlage dokumentiert

## Doku-Pruefung (Markdown-only, ohne Compile)
Durchgefuehrte Symbolpruefung im Code:
- Keine Treffer fuer resolveRequiredMarkerWithFallbackAssigning/resolveRequiredMarkerWithFallback in src/main/java
- getNextAvailable(...) nur in:
  - src/main/java/keystone/npc/markers/MarkerRegistry.java
  - src/main/java/keystone/npc/markers/MarkerRingTraversal.java
- MarkerRingTraversal nur intern in Marker-Registry referenziert

Hinweis: Da Markdown-only-Update, kein Maven-Compile-Gate erforderlich.

## Regelkonflikte
- Kein Widerspruch zwischen AGENTS.md, docs/safety/json_hierarchy.md, docs/safety/npc_restart_relink_control.md und aktuellem Code festgestellt.

## Risiko/Restgefahr
- Niedrig: Dokumentationsdrift ist reduziert.
- Verbleibendes Risiko fuer spaetere Patches: Falls getNextAvailable(...) wieder in read-only Pfade eingebaut wird, verletzt das die neue Safety-Allowlist und muss im Review sofort blockiert werden.

## Ergebnis
Safety-Dokumentation und aktueller Code-Zustand sind fuer den Marker-Resolver-/Reconcile-Bereich wieder konsistent.
