package keystone.npc.logging;

import com.hypixel.hytale.logger.HytaleLogger;

import java.util.Objects;
import java.util.logging.Level;

/*
 * KeystoneNpcLogger ist der zentrale Logger für KeystoneNPC.
 *
 * Diese Klasse schreibt nur Meldungen in Server-Konsole / Serverlog.
 * Sie wirft keine Exceptions und entscheidet nicht über Erfolg oder Fehler.
 */
public final class KeyNpcLogger {

	private static final HytaleLogger LOGGER = HytaleLogger.get("KeystoneNPC");

	public static void info(String tag, String message) {
		LOGGER.at(Level.INFO).log("[%s] %s", cleanTag(tag), cleanMessage(message));
	}

	public static void warn(String tag, String message){
			LOGGER.at(Level.WARNING).log("[%s] %s", cleanTag(tag), cleanMessage(message));
	}

	public static void error(String tag, String message){
			LOGGER.at(Level.SEVERE).log("[%s] %s", cleanTag(tag), cleanMessage(message));
	}

	public static void error(String tag, String message, Throwable cause) {
	if (cause == null) {
		error(tag, message);
		return;
	}

		LOGGER.at(Level.SEVERE)
			.withCause(cause)
			.log("[%s] %s", cleanTag(tag), cleanMessage(message));
	}

	public static void debug(String tag, String message){
			LOGGER.at(Level.FINE).log("[%s] %s", cleanTag(tag), cleanMessage(message));
	}


	private static String cleanTag(String tag) {
		return Objects.requireNonNullElse(tag, "NO_TAG").trim();
	}

	private static String cleanMessage(String message) {
		return Objects.requireNonNullElse(message, "");
	}
}




