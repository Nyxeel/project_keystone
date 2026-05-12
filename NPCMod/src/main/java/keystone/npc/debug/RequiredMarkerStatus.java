package keystone.npc.debug;

import keystone.npc.markers.MarkerRecord;
import keystone.npc.markers.MarkerType;

public record RequiredMarkerStatus(String name, MarkerType markerType, MarkerRecord resolvedMarker, boolean supported) {
}
