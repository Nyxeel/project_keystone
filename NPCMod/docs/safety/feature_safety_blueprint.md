# Blueprint-Prompt: Feature-Kontrolldatei erstellen

> **Zweck:**
> Dieser Prompt erzeugt für jedes neue Feature eine ausführliche Markdown-Kontrolldatei.
---

## Prompt

```text
Du bekommst gleich eine technische Auswertung, Review-Historie oder Feature-Beschreibung zu meinem Projekt.

Deine Aufgabe:
Erstelle daraus eine schöne, ausführliche Markdown-Kontrolldatei nach dem gleichen Prinzip wie:

npc_restart_relink_safety_control.md

Ziel der Datei:
Die Datei soll später von einer AI / einem Agent / Entwickler genutzt werden, um bei neuen Änderungen zu prüfen, ob die validierte Funktionalität und Architektur des Features weiterhin erhalten bleibt.

Wichtig:
Die Datei ist keine kurze Zusammenfassung.
Sie ist eine dauerhafte Kontroll-, Review- und Safety-Datei.

Sie muss enthalten:
- klare Architekturregeln
- validierten Ist-Zustand
- No-Go-Regeln
- Review-Fragen
- Test-Gates
- bekannte Fehlerbilder
- Diagnose-Hinweise
- Update-Pflicht, falls spätere Änderungen dieses Feature verändern
- Pflicht-Checkliste für spätere AI-Patches
- Commit-/Patch-Regeln

Sprache:
Deutsch, technisch klar, gut strukturiert.

Format:
Markdown mit schönem Layout:
- Titel
- Statusbox am Anfang
- Inhaltsverzeichnis
- klare Überschriften
- Tabellen, wo sinnvoll
- Checklisten mit `[ ]`
- Codeblöcke für Befehle, Regeln, Log-Beispiele
- Warnhinweise mit Markdown-Blockquotes, z. B.:
  > [!IMPORTANT]
  > ...
  > [!CAUTION]
  > ...

Wichtig:
Wenn die Auswertung mehrere Agent-Steps enthält, dann jeden Step einzeln dokumentieren:
- Ziel
- validierter Zustand
- was geschützt wird
- was nicht kaputtgehen darf
- Review-Fragen
- Test-Erwartung
- bekannte Risiken

Wenn die Auswertung Fehler/Fixes enthält:
- ursprüngliche Idee dokumentieren
- Fehlerbild dokumentieren
- final gültigen Zustand dokumentieren
- klare No-Go-Regel ergänzen
- Diagnose für dieses Fehlerbild ergänzen

Wenn ein Feature später bewusst geändert wird:
Die Datei muss sagen, dass sie aktualisiert werden muss:
- alte Regel ersetzt durch neue Regel
- neue Tests ergänzen
- neue Risiken dokumentieren
- neue Review-Fragen ergänzen

Die Kontroll-Datei soll mindestens diese Struktur haben:

# [FEATURE-NAME] Control File

> Status: Validated Baseline / Draft / Needs Review
> Projekt: [Projektname]
> Zweck: [kurzer Zweck der Kontroll-Datei]

## Inhaltsverzeichnis

1. Grundprinzipien
2. Zielarchitektur
3. Validierter Zustand
4. Validierte Agent Steps / Feature-Phasen
5. Kritische No-Go-Regeln
6. Standard Review-Prozess für spätere Patches
7. Compile- und Test-Gates
8. Bekannte Fehlerbilder und Diagnose
9. Pflicht bei Änderungen an diesem Feature
10. Aktueller validierter Abschlussstand
11. Kurze Pflicht-Checkliste für jede spätere AI
12. Empfohlene Commit-Regel

## 1. Grundprinzipien

Beschreibe hier:
- wichtigste Architekturregel
- wichtigste Safety-Regel
- welche Begriffe strikt getrennt werden müssen
- welche Zustände runtime-only sind
- welche Zustände persistent sind

Nutze Warnboxen:

> [!IMPORTANT]
> Wichtigste Regel des Features.

> [!CAUTION]
> Gefährlicher Fehler, der nicht wieder eingeführt werden darf.

## 2. Zielarchitektur

Beschreibe:
- relevante Komponenten
- relevante Dateien/Ordner
- Datenfluss
- Zustandsfluss
- persistente Daten
- runtime-only Daten
- Schnittstellen zu anderen Systemen

Falls sinnvoll, nutze Tabellen:

| Bereich | Aufgabe | Darf persistieren? |
|---|---|---|
| ... | ... | ... |

## 3. Validierter Zustand

Beschreibe den aktuell geprüften Zustand:
- was funktioniert
- was absichtlich deaktiviert ist
- was nur später geplant ist
- welche Annahmen gelten
- welche Tests bereits bestanden wurden

## 4. Validierte Agent Steps / Feature-Phasen

Für jeden Step:

## Step X — [Name]

### Ziel

Was sollte dieser Step erreichen?

### Validierter Zustand

Was wurde als korrekt reviewed?

### Was geschützt wird

Welche Fehler verhindert dieser Step?

### Darf nicht kaputtgehen

Welche späteren Änderungen dürfen diesen Step nicht brechen?

### Review-Fragen

- [ ] Frage 1
- [ ] Frage 2
- [ ] Frage 3

### Test-Erwartung

Welche Logs, Zustände oder Dateien müssen nach dem Test sichtbar sein?

## 5. Kritische No-Go-Regeln

Liste harte Verbote:

## 5.1 [No-Go-Regel]

Verboten:

```text
konkretes falsches Verhalten
```

Erlaubt / erforderlich:

```text
korrektes Verhalten
```

## 6. Standard Review-Prozess für spätere Patches

Erstelle Review-Fragen je betroffenem Bereich.

Beispiel:

## Wenn [Datei/System] geändert wurde

Prüfen:

- [ ] Bleibt Regel X erhalten?
- [ ] Wird Zustand Y nicht falsch gespeichert?
- [ ] Wird Safety-Pfad Z nicht umgangen?

## 7. Compile- und Test-Gates

Immer enthalten:

```bash
mvn -q -DskipTests test-compile
```

Falls das Feature nicht Java/Maven ist, stattdessen den passenden Compile-/Test-Befehl aus der Auswertung verwenden.

Test-Gates auflisten:
- Minimaltest
- Restart-Test, falls relevant
- Negativ-Test
- Multi-Objekt-Test, falls relevant
- Persistenz-Test
- Log-Spam-Test, falls relevant

## 8. Bekannte Fehlerbilder und Diagnose

Für jedes bekannte Fehlerbild:

## 8.X [Fehlerbild]

Symptom:

```text
Log / Verhalten / sichtbarer Fehler
```

Mögliche Ursache:

```text
technische Ursache
```

Prüfen:

- Datei / Log / State
- erwartete Werte
- verbotene Werte

Sofortiger Fix / Richtung:

```text
sichere Fix-Richtung
```

## 9. Pflicht bei Änderungen an diesem Feature

Schreibe klar:

Wenn eine AI dieses Feature bewusst verändert, muss sie diese Datei aktualisieren.

Bei Änderung an:
- Architektur
- Persistenz
- Runtime-Verhalten
- Safety-Gates
- Tests
- Fehlerbehandlung
- Ordner-/Dateistruktur

muss ergänzt werden:

```text
- Was wurde geändert?
- Welche alte Regel wurde ersetzt?
- Welche neue Regel gilt?
- Welche neuen Tests sind Pflicht?
- Welche Risiken entstehen?
- Wie wird Regression verhindert?
```

## 10. Aktueller validierter Abschlussstand

Kurze Liste:

```text
Step 1: ...
Step 2: ...
Step 3: ...
```

Oder:

```text
Feature A: validiert
Feature B: deaktiviert
Feature C: später geplant
```

## 11. Kurze Pflicht-Checkliste für jede spätere AI

Erstelle eine kurze End-Checkliste:

```text
[ ] Compile grün?
[ ] Keine No-Go-Regel verletzt?
[ ] Persistenz korrekt?
[ ] Runtime-State nicht falsch gespeichert?
[ ] Safety-Gates erhalten?
[ ] Logs geprüft?
[ ] Tests ergänzt?
[ ] Falls Regel geändert: diese Datei aktualisiert?
```

## 12. Empfohlene Commit-Regel

Schreibe:
- Feature-/Safety-Patches getrennt committen
- keine großen unrelated Refactors im selben Commit
- Beispiel-Commit-Namen passend zum Feature

Ergebnis:
Gib die komplette Kontroll-Datei als Markdown aus.

Wichtig:
Nicht nur erklären, sondern direkt die fertige `.md`-Datei inhaltlich schreiben.

Hier ist die Auswertung / Review-Historie / Feature-Beschreibung:

<<AUSWERTUNG ODER FEATURE-BESCHREIBUNG EINFÜGEN>>
```

---

## Verwendung

1. Diesen Prompt kopieren.
2. Unten bei `<<AUSWERTUNG ODER FEATURE-BESCHREIBUNG EINFÜGEN>>` die technische Auswertung einfügen.
3. AI erzeugt daraus eine vollständige Kontroll-Datei im gleichen Stil wie `npc_restart_relink_safety_control.md`.
4. Datei im Projekt ablegen, z. B.:

```text
docs/[feature_name]_control.md
```

---

## Ziel

Jedes größere Feature bekommt dadurch eine eigene dauerhafte Kontroll-Datei, damit spätere Patches nicht versehentlich validierte Architektur- und Safety-Regeln beschädigen.


## ABSCHLUSS


Ergänze am Ende der Kontroll-Datei noch einen kurzen Abschnitt „AI-Ergänzung aus Gesprächskontext“. Nutze dafür nur Punkte, die aus dem bisherigen Gespräch fachlich wichtig erscheinen und noch nicht ausreichend abgedeckt sind. Ergänze keine neuen Features und keine Spekulationen. Fokus: spätere AI-Agenten sollen erkennen, welche Safety-Regeln besonders leicht versehentlich gebrochen werden, z. B. durch unrelated Refactors, JSON-/Role-Strukturänderungen, Restore-/Respawn-Änderungen oder falsche Nutzung von Hytale-Engine-Feldern. Halte den Abschnitt kurz, aber konkret, mit maximal 5 Bulletpoints und klarer Update-Pflicht, falls einer dieser Punkte bewusst verändert wird.


## LETZTE REGEL
- **Wenn eine dieser Regeln bewusst geändert wird, muss diese Kontroll-Datei sofort aktualisiert werden.** Die Änderung braucht dann neue Review-Fragen, neue Negativ-Tests und eine klare Begründung, warum die neue Architektur sicherer oder notwendig ist.






