package keystone.npc.lifecycle;

import java.util.Objects;

import keystone.npc.definition.NpcDefinition;
import keystone.npc.state.NpcStateStore;



/*
 * NpcRelink ist die Skeleton-Schicht für das Wiederfinden alter NPC-Entities.
 *
 * Später soll diese Klasse nach einem Serverstart prüfen:
 * - zuerst über entityUuid
 * - Anchor-Relink nur als letzter sicherer Fallback
 *
 * Wichtig:
 * Diese Klasse darf keine EntityRef persistieren.
 * Runtime-Entity-Daten dürfen nur später im RuntimeNpc landen, niemals in state.json.
 */

public final class NpcRelink {

    private final NpcStateStore stateStore;
    private final NpcDefinition definition;

 	/*
 	* Erstellt den Relink-Service mit Zugriff auf State und NPC-Definitionen.
 	*/
	public NpcRelink(
	        NpcStateStore stateStore,
	        NpcDefinition definition
	) {
	    this.stateStore = Objects.requireNonNull(stateStore, "stateStore must not be null");
	    this.definition = Objects.requireNonNull(definition, "definition must not be null");
	}

    public void prepareRelinkAfterStartup() {
        // TODO: Relink nach Start vorbereiten.
    }

	/*
	 * Versucht später, einen NPC sicher wiederzufinden.
	 * Im Skeleton ist Relink noch nicht implementiert.
	 */
	public Object tryRelink(String npcId) {
	    requireNpcId(npcId);

	    // TODO: UUID-Relink zuerst prüfen, danach Anchor-Relink nur als letzter sicherer Fallback.
	    throw new UnsupportedOperationException("NPC Relink is not implemented yet.");
	}

	/*
	 * Versucht später den sicheren UUID-Relink.
	 * Das muss vor jedem Anchor-Fallback passieren.
	 */
	public Object tryUuidRelink(String npcId) {
	    requireNpcId(npcId);

	    // TODO: Entity später über die persistierte entityUuid suchen.
	    throw new UnsupportedOperationException("NPC UUID relink is not implemented yet.");
	}

	/*
	 * Versucht später den Anchor-Relink.
	 * Dieser Weg darf nur letzter Fallback sein.
	 */
	public Object tryAnchorRelink(String npcId) {
	    requireNpcId(npcId);

	    // TODO: Anchor-Relink später nur nutzen, wenn UUID-Relink sicher fehlgeschlagen ist und der Anchor eindeutig ist.
	    throw new UnsupportedOperationException("NPC Anchor relink is not implemented yet.");
	}


	/*
	 * Prüft, ob eine npcId vorhanden ist.
	 * Ohne gültige npcId darf kein Relink gestartet werden.
	 */
	private static void requireNpcId(String npcId) {
	    if (npcId == null || npcId.isBlank()) {
	        throw new IllegalArgumentException("npcId must not be null or blank.");
	    }
	}
}