package keystone.npc.world;

import java.nio.file.Path;
import java.util.Objects;
import java.util.UUID;

import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.WorldConfig;

/*
 * WorldData beschreibt eine einzelne Hytale-World eindeutig für unsere Mod.
 *
 * key = sicherer Speicher-Key für Ordner.
 * worldUuid = echte Hytale-World-UUID.
 * worldName = technischer World-Name / Ordnername.
 * worldConfig = schöner Anzeigename.
 * savePath = echter Welt-Speicherpfad.
 */
public record WorldData(
	String worldKey,
	UUID worldUuid,
	String worldName,
	WorldConfig worldConfig,
	String savePath
) {

	/*
	 * Prüft alle Werte direkt beim Erstellen.
	 */
	public WorldData {
		worldKey = sanitize(worldKey);
		worldUuid = Objects.requireNonNull(worldUuid, "worldUuid must not be null.");
		worldName = requireText(worldName, "worldName");
		worldConfig = Objects.requireNonNull(worldConfig, "worldConfig must not be null.");
		savePath = Objects.requireNonNull(savePath, "savePath must not be null.");
	}

	/*
	 * Baut einen WorldData direkt aus einer Hytale-World.
	 */
	public static WorldData fromWorld(String worldKey, World world) {
		Objects.requireNonNull(world, "world must not be null.");

		String key = worldKey;
		UUID uuid = world.getWorldConfig().getUuid();
		String name = world.getName();
		WorldConfig worldConfig = world.getWorldConfig();
		Path savePath = world.getSavePath();

		return new WorldData(
			key,
			uuid,
			name,
			worldConfig,
			savePath.toString()
		);
	}


	/*
	 * Macht einen Text sicher für Ordnernamen.
	 */
	public static String sanitize(String value) {
		String checkedValue = requireText(value, "world key");

		String safeValue = checkedValue.trim()
			.replace('\\', '_')
			.replace('/', '_')
			.replace(':', '_')
			.replaceAll("[^A-Za-z0-9._-]", "_")
			.replaceAll("\\.{2,}", "_")
			.replaceAll("^\\.+", "_")
			.replaceAll("_+", "_");

		if (safeValue.isBlank() || ".".equals(safeValue) || "..".equals(safeValue)) {
			throw new IllegalArgumentException("world key cannot be converted to a safe path name.");
		}

		return safeValue;
	}

	/*
	 * Prüft Pflicht-Text.
	 */
	private static String requireText(String value, String fieldName) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(fieldName + " must not be null or blank.");
		}

		return value.trim();
	}

}