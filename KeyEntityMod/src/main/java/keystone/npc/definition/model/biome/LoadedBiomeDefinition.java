package keystone.npc.definition.model.biome;

import java.util.Objects;

/*
 * LoadedBiomeDefinition beschreibt eine geladene Biom-Definition aus resources.
 *
 * Diese Klasse sagt:
 * Dieses Biom benutzt diese Theme-Bindings.
 *
 * Beispiel:
 * biomeId = sand_desert
 * themes.structureTheme = sand
 * themes.bodyTheme = sand
 * themes.outfitTheme = sand
 * themes.nameTheme = sand
 *
 * Wichtig:
 * Diese Klasse fragt kein echtes Welt-Biom ab.
 * Sie liest keine Spielerposition.
 * Sie platziert keine Gebäude.
 * Sie speichert keine state.json-Daten.
 */
public final class LoadedBiomeDefinition {

	private final String biomeId;				// Eindeutige Biom-ID, z. B. sand_desert.
	private final BiomeThemeBinding themes;		// Theme-Zuordnung für dieses Biom.

	/*
	 * Erstellt eine geladene Biom-Definition.
	 */
	public LoadedBiomeDefinition(
		String biomeId,				// Eindeutige Biom-ID.
		BiomeThemeBinding themes	// Theme-Zuordnung dieses Bioms.
	) {
		this.biomeId = requireText(biomeId, "biomeId");
		this.themes = Objects.requireNonNull(themes, "themes must not be null");

		if (!this.biomeId.equals(this.themes.biomeId())) {
			throw new IllegalArgumentException("biomeId does not match themes.biomeId: " + this.biomeId + " != " + this.themes.biomeId());
		}
	}

	/*
	 * Gibt die Biom-ID zurück.
	 */
	public String biomeId() {
		return biomeId;
	}

	/*
	 * Gibt die Theme-Zuordnung dieses Bioms zurück.
	 */
	public BiomeThemeBinding themes() {
		return themes;
	}

	/*
	 * Prüft Pflicht-Textfelder und entfernt unnötige Leerzeichen.
	 */
	private static String requireText(String value, String fieldName) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(fieldName + " must not be null or blank");
		}

		return value.trim();
	}
}