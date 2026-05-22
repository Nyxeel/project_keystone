package keystone.npc.definition.model.biome;

/*
 * BiomeThemeBinding verbindet ein Hytale-/Keystone-Biom mit Keystone-Themes.
 *
 * Beispiel:
 * biomeId = sand_desert
 * structureTheme = sand
 * bodyTheme = sand
 * outfitTheme = sand
 * nameTheme = sand
 *
 * Wichtig:
 * Diese Klasse fragt kein echtes Welt-Biom ab.
 * Sie speichert nur die Zuordnung aus Resource-JSONs.
 *
 * Die echte Regel später ist:
 * PlacementCandidatePosition -> BiomeId -> BiomeThemeBinding -> StructureTheme
 */
public final class BiomeThemeBinding {

	private final String biomeId;			// Biom-ID, z. B. sand_desert.
	private final String structureTheme;	// Gebäude-Theme, z. B. sand.
	private final String bodyTheme;			// Körper-Theme, z. B. sand.
	private final String outfitTheme;		// Kleidungs-Theme, z. B. sand.
	private final String nameTheme;			// Namen-Theme, z. B. sand.

	/*
	 * Erstellt eine Theme-Zuordnung für ein Biom.
	 */
	public BiomeThemeBinding(
		String biomeId,			// Biom-ID.
		//TODO: Subbiomes pro Biome rausfinden!

		String structureTheme,	// Gebäude-Theme.
		String bodyTheme,		// Körper-Theme.
		String outfitTheme,		// Kleidungs-Theme.
		String nameTheme		// Namen-Theme.
	) {
		this.biomeId = requireText(biomeId, "biomeId");
		this.structureTheme = requireText(structureTheme, "structureTheme");
		this.bodyTheme = requireText(bodyTheme, "bodyTheme");
		this.outfitTheme = requireText(outfitTheme, "outfitTheme");
		this.nameTheme = requireText(nameTheme, "nameTheme");
	}

	/*
	 * Gibt die Biom-ID zurück.
	 */
	public String biomeId() {
		return biomeId;
	}

	/*
	 * Gibt das Gebäude-Theme zurück.
	 */
	public String structureTheme() {
		return structureTheme;
	}

	/*
	 * Gibt das Körper-Theme zurück.
	 */
	public String bodyTheme() {
		return bodyTheme;
	}

	/*
	 * Gibt das Kleidungs-Theme zurück.
	 */
	public String outfitTheme() {
		return outfitTheme;
	}

	/*
	 * Gibt das Namen-Theme zurück.
	 */
	public String nameTheme() {
		return nameTheme;
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