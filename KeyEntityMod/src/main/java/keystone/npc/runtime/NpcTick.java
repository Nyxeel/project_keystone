package keystone.npc.runtime;

import java.util.Objects;

import keystone.npc.definition.NpcDefinition;
import keystone.npc.lifecycle.NpcRelink;
import keystone.npc.navigation.NpcNavigation;
import keystone.npc.state.NpcStateStore;

/*
 * NpcTick ist die Skeleton-Schicht für den regelmäßigen NPC-Tick.
 *
 * Später wird diese Klasse alle aktiven NPCs regelmäßig prüfen.
 * Dabei darf normale NPC-Logik nur laufen, wenn der NPC eine gültige Live-Entity hat.
 *
 * Wichtig:
 * Diese Klasse darf keine EntityRef persistieren.
 * Diese Klasse darf keine Runtime-Navigation in state.json speichern.
 * Diese Klasse darf keine NPC-Logik ausführen, wenn der NPC nicht sicher live ist.
 */
public final class NpcTick {

    private final NpcStateStore stateStore;
    private final NpcDefinition definition;
    private final NpcRelink relink;
    private final NpcNavigation navigation;

    private boolean running;

    /*
     * Erstellt die Tick-Schicht mit Zugriff auf State, Definitionen, Relink und Navigation.
     */
    public NpcTick(
            NpcStateStore stateStore,
            NpcDefinition definition,
            NpcRelink relink,
            NpcNavigation navigation
    ) {
        this.stateStore = Objects.requireNonNull(stateStore, "stateStore must not be null");
        this.definition = Objects.requireNonNull(definition, "definition must not be null");
        this.relink = Objects.requireNonNull(relink, "relink must not be null");
        this.navigation = Objects.requireNonNull(navigation, "navigation must not be null");
    }

    /*
     * Startet später den NPC-Tick.
     * Im Skeleton wird noch kein echter Scheduler gestartet.
     */
    public void start() {
        if (running) {
            throw new IllegalStateException("NPC tick is already running.");
        }

        running = true;

        // TODO: Später echten Tick/Scheduler starten.
        // TODO: Dabei sicherstellen, dass kein zweiter Tick parallel gestartet wird.

	}

    /*
     * Stoppt später den NPC-Tick.
     * Im Skeleton wird nur der Running-Status zurückgesetzt.
     */
    public void stop() {
        if (!running) {
            return;
        }

        running = false;

        // TODO: Später echten Tick/Scheduler stoppen.
        // TODO: Danach Runtime-Navigation sicher leeren, falls nötig.
    }

    /*
     * Tick später für alle NPCs.
     * Im Skeleton passiert nichts, wenn der Tick nicht läuft.
     */
    public void tickAll() {
        if (!running) {
            return;
        }

        // TODO: Später alle NPCs aus dem State/Manager durchlaufen.
        // TODO: Pro NPC zuerst Live-Entity-Gate prüfen.
        // TODO: Keine Navigation/Routine ausführen, wenn EntityRef fehlt oder ungültig ist.
    }

    /*
     * Tick später für einen einzelnen NPC.
     * Im Skeleton wird nur die npcId geprüft.
     */
    public void tickNpc(String npcId) {
        requireText(npcId, "npcId");

        if (!running) {
            return;
        }

        // TODO: Live-Entity-Gate prüfen.
        // TODO: Nur bei ACTIVE + gültiger EntityRef Navigation/Routine ausführen.
        // TODO: Bei fehlender EntityRef nur Relink/Recovery erlauben.
    }

    /*
     * Gibt zurück, ob der NPC-Tick aktuell laufen soll.
     */
    public boolean isRunning() {
        return running;
    }

    /*
     * Prüft, ob ein Textwert vorhanden ist.
     */
    private static void requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be null or blank.");
        }
    }
}