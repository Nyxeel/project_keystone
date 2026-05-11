package keystone.npc.markers;

import java.util.Objects;

/** Placeholder bis echte Hytale-World-ID verwendet wird. */
public final class WorldId {
    private final String value;

    public WorldId(String value) {
        this.value = Objects.requireNonNull(value);
    }

    public String value() {
        return value;
    }

    @Override public String toString() { return value; }
    @Override public boolean equals(Object o) {
        return (this == o) || (o instanceof WorldId other && value.equals(other.value));
    }
    @Override public int hashCode() { return value.hashCode(); }
}
