package keystone.npc.lifecycle;

import java.util.Objects;

import keystone.npc.definition.NpcDefinition;
import keystone.npc.state.NpcStateStore;

/*
 * NpcRespawn ist die Skeleton-Schicht für kontrollierten NPC-Respawn.
 *
 * Später darf diese Klasse fehlende NPCs nur sicher ersetzen:
 * - nicht bei unsicherem Relink-Zustand
 * - nicht bei AMBIGUOUS
 * - nicht ohne Respawn-Policy
 * - nicht ohne Save-/Rollback-Sicherheit
 *
 * Wichtig:
 * Diese Klasse darf keine EntityRef persistieren und keine NPCs blind duplizieren.
 */
public final class NpcRespawn {

    private final NpcStateStore stateStore;
    private final NpcDefinition definition;
    private final NpcRelink relink;
    private final NpcSpawn spawn;

    /*
     * Erstellt den Respawn-Service mit Zugriff auf State, Definitionen, Relink und Spawn.
     */
    public NpcRespawn(
            NpcStateStore stateStore,
            NpcDefinition definition,
            NpcRelink relink,
            NpcSpawn spawn
    ) {
        this.stateStore = Objects.requireNonNull(stateStore, "stateStore must not be null");
        this.definition = Objects.requireNonNull(definition, "definition must not be null");
        this.relink = Objects.requireNonNull(relink, "relink must not be null");
        this.spawn = Objects.requireNonNull(spawn, "spawn must not be null");
    }

    /*
     * Plant später den ersten Respawn-Check nach dem Serverstart.
     * Im Skeleton wird noch nichts geplant.
     */
    public void queueInitialRespawnCheck() {
        // TODO: Initialen Respawn-Check erst planen, wenn Welten und NPC-Daten sicher geladen sind.
    }

    /*
     * Prüft später fehlende NPCs und ersetzt sie nur kontrolliert.
     * Im Skeleton wird noch nichts gespawnt.
     */
    public void respawnMissingNpcs(boolean dryRun, boolean force) {
        // TODO: Wenn dryRun true ist, darf diese Methode niemals State oder Welt verändern.
        // TODO: force darf später nur Safety-Gates bewusst erweitern, aber nicht AMBIGUOUS blind ignorieren.
    }

    /*
     * Prüft später, ob ein einzelner NPC respawnen darf.
     * Im Skeleton ist Respawn sicherheitshalber nicht erlaubt.
     */
    public boolean canRespawn(String npcId) {
        if (npcId == null || npcId.isBlank()) {
            return false;
        }

        // TODO: Respawn-Policy prüfen.
        return false;
    }
}