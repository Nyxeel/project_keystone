package keystone.npc.definition.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/*
 * ProfileTypeRegistry kennt die Regeln für Profil-Typen.
 *
 * Beispiel:
 * Routine ist Pflicht.
 * Combat ist optional.
 * BodyPoolGroup ist optional und zeigt auf eine Gruppe von BodyPools.
 * CustomSomething ist unbekannt, aber erlaubt und wird basic validiert.
 *
 * Diese Klasse lädt keine Profile.
 * Sie sagt nur, welche Profile required, optional oder custom sind.
 *
 * Wichtig für das neue Biom-System:
 * - FolkPool ist kein aktives Hauptsystem mehr.
 * - Appearance / AppearancePool sind kein aktives System mehr.
 * - BodyPoolGroup, NamePoolGroup, OutfitPoolGroup und StructurePool sind die neuen Theme-Anbindungen.
 */
public final class ProfileTypeRegistry {

	private final Map<String, ProfileTypeRule> rules; // Alle bekannten Profil-Regeln, Key = Profilname.
	private final ProfileTypeRule customFallbackRule; // Fallback-Regel für unbekannte Custom-Profile.

	/*
	 * Erstellt eine Registry mit Core-Regeln und Custom-Fallback.
	 */
	public ProfileTypeRegistry() {
		this.rules = createDefaultRules();
		this.customFallbackRule = new ProfileTypeRule(
			"Custom",		// Technischer Name für unbekannte Profile.
			false,			// Custom-Profile sind nicht automatisch Pflicht.
			null,			// Kein fester erwarteter JSON-Type.
			"basic",		// Custom-Profile werden nur grob geprüft.
			false,			// Custom ist kein bekannter Core-Profiltyp.
			null			// Kein Handler-Key, solange kein System dafür existiert.
		);
	}

	/*
	 * Gibt die passende Regel für einen Profil-Key zurück.
	 */
	public ProfileTypeRule ruleFor(String profileKey) {
		if (profileKey == null || profileKey.isBlank()) {
			return customFallbackRule;
		}

		ProfileTypeRule rule = rules.get(profileKey.trim());

		if (rule == null) {
			return customFallbackFor(profileKey);
		}

		return rule;
	}

	/*
	 * Prüft, ob ein Profil-Key ein bekannter Core-Profiltyp ist.
	 */
	public boolean isKnownProfileType(String profileKey) {
		if (profileKey == null || profileKey.isBlank()) {
			return false;
		}

		return rules.containsKey(profileKey.trim());
	}

	/*
	 * Prüft, ob ein Profil-Key required ist.
	 */
	public boolean isRequired(String profileKey) {
		return ruleFor(profileKey).required();
	}

	/*
	 * Gibt alle bekannten Regeln unveränderlich zurück.
	 */
	public Map<String, ProfileTypeRule> rules() {
		return rules;
	}

	/*
	 * Erstellt die Standard-Regeln für Required-Core und Optional-Core.
	 */
	private static Map<String, ProfileTypeRule> createDefaultRules() {
		Map<String, ProfileTypeRule> result = new LinkedHashMap<>();

		addRequired(result, "Routine", "RoutineProfile", "strict", "routine");
		addRequired(result, "Actions", "ActionProfile", "strict", "actions");
		addRequired(result, "Movement", "MovementProfile", "strict", "movement");
		addRequired(result, "Navigation", "NavigationProfile", "strict", "navigation");
		addRequired(result, "Persistence", "PersistenceProfile", "strict", "persistence");

		addOptional(result, "Combat", "CombatProfile", "basic", "combat");
		addOptional(result, "Events", "EventProfile", "basic", "events");
		addOptional(result, "Dialogue", "DialogueProfile", "basic", "dialogue");
		addOptional(result, "Trading", "TradingProfile", "basic", "trading");
		addOptional(result, "Reputation", "ReputationProfile", "basic", "reputation");
		addOptional(result, "SeasonalOutfits", "SeasonalOutfitProfile", "basic", "seasonal_outfits");
		addOptional(result, "Spawn", "SpawnProfile", "basic", "spawn");

		addOptional(result, "CreatureType", "CreatureTypeProfile", "basic", null);
		addOptional(result, "BodyPool", "BodyPool", "basic", null);
		addOptional(result, "NamePool", "NamePool", "basic", null);
		addOptional(result, "OutfitPool", "OutfitPool", "basic", null);
		addOptional(result, "StructurePool", "StructurePool", "basic", null);
		addOptional(result, "CompositionPool", "CompositionPool", "basic", null);

		return Collections.unmodifiableMap(result);
	}

	/*
	 * Fügt eine Pflicht-Regel hinzu.
	 */
	private static void addRequired(
		Map<String, ProfileTypeRule> rules,	// Ziel-Map für die Regel.
		String profileKey,					// Name des Profil-Typs.
		String expectedType,				// Erwarteter JSON-Type.
		String validationMode,				// Validierungsart.
		String handlerKey					// Optionaler Handler-Key.
	) {
		rules.put(profileKey, new ProfileTypeRule(
			profileKey,
			true,
			expectedType,
			validationMode,
			true,
			handlerKey
		));
	}

	/*
	 * Fügt eine optionale Regel hinzu.
	 */
	private static void addOptional(
		Map<String, ProfileTypeRule> rules,	// Ziel-Map für die Regel.
		String profileKey,					// Name des Profil-Typs.
		String expectedType,				// Erwarteter JSON-Type.
		String validationMode,				// Validierungsart.
		String handlerKey					// Optionaler Handler-Key.
	) {
		rules.put(profileKey, new ProfileTypeRule(
			profileKey,
			false,
			expectedType,
			validationMode,
			true,
			handlerKey
		));
	}

	/*
	 * Erstellt eine Custom-Regel für einen unbekannten Profil-Key.
	 */
	private ProfileTypeRule customFallbackFor(String profileKey) {
		return new ProfileTypeRule(
			profileKey.trim(),
			customFallbackRule.required(),
			customFallbackRule.expectedType(),
			customFallbackRule.validationMode(),
			customFallbackRule.knownProfileType(),
			customFallbackRule.handlerKey()
		);
	}
}