package keystone.npc.definition.model;

/*
 * ProfileTypeRule beschreibt die Lade- und Prüfregel für einen Profil-Key.
 *
 * Beispiel:
 * Routine ist required.
 * Dialogue ist optional.
 * CustomSomething ist unbekannt, aber basic erlaubt.
 *
 * Diese Klasse sagt nur:
 * - ist das Profil Pflicht?
 * - welcher Type wird erwartet?
 * - wie streng soll validiert werden?
 * - gibt es später einen Handler?
 *
 * Wichtig:
 * Diese Klasse lädt kein Profil.
 * Diese Klasse führt kein Profil aus.
 */
public final class ProfileTypeRule {

}