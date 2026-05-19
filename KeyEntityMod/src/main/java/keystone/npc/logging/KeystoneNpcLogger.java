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
public final class KeystoneNpcLogger {

	private static final HytaleLogger LOGGER = HytaleLogger.get("KeystoneNPC");

	public void info(String tag, String message) {
		LOGGER.at(Level.INFO).log("[%s] %s", cleanTag(tag), cleanMessage(message));
	}

	public void warn(String tag, String message){
			LOGGER.at(Level.WARNING).log("[%s] %s", cleanTag(tag), cleanMessage(message));
	}

	public void error(String tag, String message){
			LOGGER.at(Level.ERROR).log("[%s] %s", cleanTag(tag), cleanMessage(message));
	}

	public void error(String tag, String message, Throwable cause) {
	if (cause == null) {
		error(tag, message);
		return;
	}

		LOGGER.at(Level.SEVERE)
			.withCause(cause)
			.log("[%s] %s", cleanTag(tag), cleanMessage(message));
	}

	public void debug(String tag, String message){
			LOGGER.at(Level.DEBUG).log("[%s] %s", cleanTag(tag), cleanMessage(message));
	}


	private String cleanTag(String tag) {
		return Objects.requireNonNullElse(tag, "NO_TAG").trim();
	}

	private String cleanMessage(String message) {
		return Objects.requireNonNullElse(message, "");
	}
}




