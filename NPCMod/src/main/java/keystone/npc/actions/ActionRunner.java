package keystone.npc.actions;

public final class ActionRunner {
    private String activeActionId;

    public boolean startAction(String actionId) {
        if (actionId == null || actionId.isBlank()) {
            return false;
        }
        if (actionId.equals(activeActionId)) {
            return false;
        }
        activeActionId = actionId;
        return true;
    }

    public boolean stopAction() {
        if (activeActionId == null) {
            return false;
        }
        activeActionId = null;
        return true;
    }

    public String activeActionId() {
        return activeActionId;
    }
}
