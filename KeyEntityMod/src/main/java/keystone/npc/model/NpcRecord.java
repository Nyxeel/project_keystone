package keystone.npc.model;

import java.util.Map;
import java.util.Objects;

import keystone.npc.model.binding.NpcAppearanceSelection;
import keystone.npc.model.binding.NpcSpawnSelection;
import keystone.npc.model.binding.NpcStructureBinding;
import keystone.npc.model.identity.NpcEntityIdentity;
import keystone.npc.model.identity.NpcIdentity;
import keystone.npc.model.location.NpcPosition;
import keystone.npc.model.location.NpcWorldBinding;
import keystone.npc.model.marker.NpcMarkerAssignments;

/*
 * NpcRecord ist die persistente Wahrheit eines konkreten NPCs.
 *
 * Diese Klasse darf später in state.json gespeichert werden.
 *
 * Wichtig:
 * Hier darf KEIN entityRef rein.
 * Hier darf KEIN Hytale Entity-Objekt rein.
 * Hier darf KEINE aktive Navigation rein.
 *
 * NpcRecord ist jetzt die Haupt-Hülle.
 * Die Detailbereiche liegen in kleinen Unterklassen:
 * - identity
 * - location
 * - binding
 * - marker
 */
public final class NpcRecord {

    private final NpcIdentity identity;
    private final NpcEntityIdentity entityIdentity;
    private final NpcWorldBinding worldBinding;
    private final NpcStructureBinding structureBinding;
    private final NpcSpawnSelection spawnSelection;
    private final NpcAppearanceSelection appearanceSelection;
    private final NpcMarkerAssignments markerAssignments;

    private NpcEntityStatus entityStatus;
    private NpcState state;
    private NpcPosition currentPosition;

    /*
     * Erstellt einen neuen persistenten NPC-Record.
     */
    public NpcRecord(String npcId, String npcName, String roleId, String worldKey) {
        this.identity = new NpcIdentity(npcId, npcName, roleId);
        this.entityIdentity = new NpcEntityIdentity();
        this.worldBinding = new NpcWorldBinding(worldKey);
        this.structureBinding = new NpcStructureBinding(); 			//prefab id zb lumberajck_id5
        this.spawnSelection = new NpcSpawnSelection();
        this.appearanceSelection = new NpcAppearanceSelection(); 	//apperance pool
        this.markerAssignments = new NpcMarkerAssignments();		

        this.entityStatus = NpcEntityStatus.NEEDS_RELINK;
        this.state = NpcState.IDLE;
    }

    /*
     * Gibt den Identitätsblock zurück.
     */
    public NpcIdentity identity() {
        return identity;
    }

    /*
     * Gibt die stabile Mod-ID dieses konkreten NPCs zurück.
     */
    public String npcId() {
        return identity.npcId();
    }

    /*
     * Gibt den Anzeigenamen des NPCs zurück.
     */
    public String npcName() {
        return identity.npcName();
    }

    /*
     * Gibt die Keystone-Rolle zurück, z. B. lumberjack.
     */
    public String roleId() {
        return identity.roleId();
    }

    /*
     * Gibt die persistierte Hytale Entity UUID zurück.
     */
    public String entityUuid() {
        return entityIdentity.entityUuid();
    }

    /*
     * Setzt die persistierte Hytale Entity UUID.
     */
    public void setEntityUuid(String entityUuid) {
        entityIdentity.setEntityUuid(entityUuid);
    }

    /*
     * Gibt den technischen Entity-Status zurück.
     */
    public NpcEntityStatus entityStatus() {
        return entityStatus;
    }

    /*
     * Setzt den technischen Entity-Status.
     */
    public void setEntityStatus(NpcEntityStatus entityStatus) {
        this.entityStatus = Objects.requireNonNull(entityStatus, "entityStatus must not be null");
    }

    /*
     * Gibt den fachlichen NPC-Zustand zurück.
     */
    public NpcState state() {
        return state;
    }

    /*
     * Setzt den fachlichen NPC-Zustand.
     */
    public void setState(NpcState state) {
        this.state = Objects.requireNonNull(state, "state must not be null");
    }

    //Gibt den stabilen Welt-Key zurück.

    public String worldKey() {
		return worldBinding.worldKey();
	}

    /*
     * Gibt die letzte bekannte Position zurück.
     */
    public NpcPosition currentPosition() {
        return currentPosition;
    }

    /*
     * Setzt die letzte bekannte Position.
     * Null ist erlaubt, wenn noch keine Position bekannt ist.
     */
    public void setCurrentPosition(NpcPosition currentPosition) {
        if (currentPosition != null && !currentPosition.isFinite()) {
            throw new IllegalArgumentException("currentPosition must be finite.");
        }

        this.currentPosition = currentPosition;
    }

    /*
     * Gibt die Strukturinstanz zurück, falls der NPC an ein Prefab gebunden ist.
     */
    public String structureInstanceId() {
        return structureBinding.structureInstanceId();
    }

    /*
     * Setzt die Strukturinstanz, z. B. house_007.
     */
    public void setStructureInstanceId(String structureInstanceId) {
        structureBinding.setStructureInstanceId(structureInstanceId);
    }

    /*
     * Gibt den Slot innerhalb der Struktur zurück, z. B. main_worker oder spouse.
     */
    public String slotId() {
        return structureBinding.slotId();
    }

    /*
     * Setzt den Slot innerhalb der Struktur.
     */
    public void setSlotId(String slotId) {
        structureBinding.setSlotId(slotId);
    }

    /*
     * Gibt die ausgewählte Appearance-Variante zurück.
     */
    public String selectedAppearanceId() {
        return appearanceSelection.selectedAppearanceId();
    }

    /*
     * Setzt die ausgewählte Appearance-Variante.
     */
    public void setSelectedAppearanceId(String selectedAppearanceId) {
        appearanceSelection.setSelectedAppearanceId(selectedAppearanceId);
    }

    /*
     * Gibt die gewählte Spawn-Composition zurück.
     */
    public String selectedCompositionId() {
        return spawnSelection.selectedCompositionId();
    }

    /*
     * Setzt die gewählte Spawn-Composition.
     */
    public void setSelectedCompositionId(String selectedCompositionId) {
        spawnSelection.setSelectedCompositionId(selectedCompositionId);
    }

    /*
     * Gibt das gewählte Prefab zurück.
     */
    public String selectedPrefabId() {
        return spawnSelection.selectedPrefabId();
    }

    /*
     * Setzt das gewählte Prefab.
     */
    public void setSelectedPrefabId(String selectedPrefabId) {
        spawnSelection.setSelectedPrefabId(selectedPrefabId);
    }

    /*
     * Gibt alle konkreten Marker-Zuweisungen als sichere Kopie zurück.
     */
    public Map<String, String> markerAssignments() {
        return markerAssignments.assignments();
    }

    /*
     * Setzt eine konkrete Marker-Zuweisung.
     */
    public void assignMarker(String logicalMarkerName, String markerId) {
        markerAssignments.assignMarker(logicalMarkerName, markerId);
    }

    /*
     * Entfernt eine konkrete Marker-Zuweisung.
     */
    public void removeMarkerAssignment(String logicalMarkerName) {
        markerAssignments.removeMarkerAssignment(logicalMarkerName);
    }
}
