# MVP A – Offene Aufgaben

## Für MVP A fehlt jetzt noch

---

## 1. Door-Marker als Zwischenpunkt

Aktuell läuft Bob direkt:

```text
bed → work
```

oder:

```text
work → bed
```

Für MVP A sauberer wäre:

```text
bed → door → work
```

und zurück:

```text
work → door → bed
```

---

## 2. Animation beim Bewegen

Nicht perfekt, aber mindestens:

- Wenn Ziel aktiv ist: Walking-/Move-Animation starten
- Wenn Ziel erreicht ist: Idle-/Sleep-/Work-State setzen
- Animation nicht bei jedem Tick neu starten

---

## 3. AvatarPreset laden

Das ist ein wichtiger MVP-A-Plus-Punkt.

Benötigt:

- `setAppearance("Player")`
- JSON aus `AvatarPresets.json` laden
- JSON zu `PlayerSkin` parsen
- Skin dem NPC geben

### Ziel

Bob sieht wie ein echter Hytale-Avatar aus, nicht nur wie ein Platzhaltermodell.

---

## 4. Echten Hytale-Timer/Tick anbinden

In der Plugin-Datei steht noch ein TODO für echten Server-Tick/Timer.

Für MVP A sollte der Scheduler sauber über Hytale laufen, nicht nur als Java-Testlogik.

---

## 5. Speicherpfad sauber machen

Auch der Datenpfad ist noch TODO.

Bedeutet:

`state.json` soll sicher im richtigen Mod-/Serverdatenordner landen.

---

## 6. MVP-A-Testliste machen

Teste genau diese Fälle:

- Marker setzen
- Bob spawnen
- Bob läuft bei Tag zu Work
- Bob läuft bei Nacht zu Bed
- Server/Welt neu starten
- Bob + Marker werden wieder geladen
- Bob läuft nach Restart weiter korrekt
- Fehlender Marker macht keinen Crash
