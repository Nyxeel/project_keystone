LOGGER ACTION PLAN — KeystoneNpcLogger anlegen

ZIEL:
Eine zentrale Logger-Klasse für KeystoneNPC erstellen.

DATEI:
src/main/java/keystone/npc/logging/KeystoneNpcLogger.java

PACKAGE:
keystone.npc.logging

HYTALE-API:
com.hypixel.hytale.logger.HytaleLogger

GRUNDIDEE:
KeystoneNpcLogger ist dein eigener Wrapper.
Der restliche Code soll später nicht direkt HytaleLogger benutzen.

Warum:
Wenn sich die Hytale-Logger-API ändert,
musst du später nur KeystoneNpcLogger anpassen.

LOGGER-API NUTZEN:
- HytaleLogger.get("KeystoneNPC")
- LOGGER.at(Level.INFO).log(...)
- LOGGER.at(Level.WARNING).log(...)
- LOGGER.at(Level.SEVERE).log(...)

JAVA-LEVEL:
java.util.logging.Level

METHODEN IN KeystoneNpcLogger:
- info(String tag, String message)
- warn(String tag, String message)
- error(String tag, String message)
- error(String tag, String message, Throwable cause)
- debug(String tag, String message)

REGELN:
- Logger schreibt nur Meldungen.
- Logger wirft keine Exceptions.
- Logger entscheidet nicht, ob etwas fehlschlägt.
- Fataler Fehler wird außerhalb vom Logger per Exception gestoppt.
- Logger soll Tags standardisieren.

BEISPIEL-TAG-FORMAT:
[KeystoneNPC][SETUP_STARTED]
[KeystoneNPC][SETUP_FAILED]
[KeystoneNPC][DEFINITION_LOAD_FAILED]
[KeystoneNPC][STATE_READ_FAILED]

WICHTIG:
- Keine direkte Gameplay-Logik in KeystoneNpcLogger.
- Kein Zugriff auf state.json.
- Kein Zugriff auf NPCs.
- Kein Spawn.
- Kein Ingame-Chat.
- Nur Server-Konsole / Serverlog.

FALLBACK:
Wenn HytaleLogger beim Kompilieren Probleme macht,
darf KeystoneNpcLogger intern vorübergehend System.out/System.err nutzen.
Die öffentliche KeystoneNpcLogger-API bleibt trotzdem gleich.

COMPILE-GATE:
mvn -q -DskipTests test-compile