1. NpcRespawnMissingCommand fertig härten

Offen:
- saveStateSafely() Ergebnis wird noch nicht sauber geprüft
- bei Save-Failure fehlt klare Runtime/state.json-Drift-Meldung

Fix-Ziel:
- if (!plugin.saveStateSafely()) -> Fehler melden
- keine normale Erfolgsmeldung bei Save-Failure

2. Marker/worldId bei Marker-Zuweisung härter prüfen

Offen:
- MarkerSetCommand setzt Marker in Spieler-Welt
- Ziel-NPC könnte theoretisch in anderer worldId sein
- assignMarkerToNpc(...) prüft neue markerId nicht hart genug gegen:
  - existiert Marker?
  - MarkerType passt?
  - marker.worldId == npc.worldId?

Fix-Ziel:
NPC darf keinen Marker aus anderer Welt bekommen.


3. Mehrere Server / Save-IDs trennen

Offen als Backlog:
- state.json trennt aktuell über worldId
- aber nicht sauber über serverId/saveId

Späterer Fix:
state/<serverId>/state.json
oder vergleichbare Server-/Save-Trennung.
Nicht blind migrieren.


4. Permanenter NPC-Tod / Death-System später anbinden

Offen nur falls Death-System kommt:
- bei permanentem NPC-Tod zentralen Remove-Pfad nutzen
- eigene unbenutzte Marker löschen
- keine Marker bei MISSING_ENTITY/NEEDS_RELINK löschen


5. Live-NPC Remove Designentscheidung

Aktuell sicher, aber streng:
- Live-Entity + unconfirmed queued removal wird blockiert

Später entscheiden:
A) bestätigten Remove-Outcome bauen
oder
B) expliziten sicheren Admin-Force-Pfad bauen


1. Step 6 finaler Zustand im Abschlussbericht nachtragen
   - Step 6 war am Ende PASS
   - BUILD SUCCESS wurde bestätigt

2. Alte offene TODO-Liste bereinigen
   - alten Step-7-Fix-Prompt entfernen
   - nur Guard-Regeln behalten

3. Optionaler Mini-Cleanup
   - Log-Tippfehler prüfen: SPWAN_ROLLBACK_COMPLETED_AFTER_SAVE_FAILURE
   - falls vorhanden: zu SPAWN_ROLLBACK_COMPLETED_AFTER_SAVE_FAILURE korrigieren

4. Späterer Designpunkt
   - /knpc remove und /knpc clear sind bei Live-Entity aktuell sehr streng
   - später entscheiden:
     a) bestätigten Remove-Outcome bauen
     b) oder expliziten sicheren Admin-Force-Pfad bauen

5. Marker-v2 erst danach planen
   - nicht aus den alten Fallback-Prompts übernehmen
   - neue saubere Planung auf Basis der jetzigen Safety-Regeln


   