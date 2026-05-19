package keystone.npc.definition.model;

/*
 * ProfileTypeRegistry kennt alle bekannten Profil-Regeln.
 *
 * Hier werden zentrale Regeln gesammelt:
 *
 * Required:
 * - Routine
 * - Actions
 * - Movement
 * - Navigation
 * - Persistence
 *
 * Optional:
 * - Combat
 * - Events
 * - Dialogue
 * - Trading
 * - Reputation
 * - SeasonalOutfits
 * - Spawn
 * - SpeciesPool
 * - BodyPool
 * - OutfitPool
 * - CompositionPool
 * - Appearance
 * - AppearancePool
 *
 * Unbekannte Profile werden später als CustomProfile erlaubt,
 * aber nur basic validiert und nicht automatisch ausgeführt.
 *
 * Wichtig:
 * Diese Registry enthält nur Regeln.
 * Sie liest keine Dateien.
 * Sie schreibt keine state.json.
 */
public final class ProfileTypeRegistry {

}