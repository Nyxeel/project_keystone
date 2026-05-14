#!/usr/bin/env bash
set -euo pipefail

# -----------------------------
# 1) Java-Ordner anlegen
# -----------------------------

mkdir -p \
  src/main/java/keystone/npc/bootstrap \
  src/main/java/keystone/npc/service \
  src/main/java/keystone/npc/core \
  src/main/java/keystone/npc/model \
  src/main/java/keystone/npc/world \
  src/main/java/keystone/npc/state/internal \
  src/main/java/keystone/npc/definition/profiles \
  src/main/java/keystone/npc/marker/internal \
  src/main/java/keystone/npc/structure/internal \
  src/main/java/keystone/npc/lifecycle \
  src/main/java/keystone/npc/runtime \
  src/main/java/keystone/npc/routine \
  src/main/java/keystone/npc/action \
  src/main/java/keystone/npc/navigation \
  src/main/java/keystone/npc/road \
  src/main/java/keystone/npc/command/admin \
  src/main/java/keystone/npc/command/marker \
  src/main/java/keystone/npc/command/spawn \
  src/main/java/keystone/npc/command/debug \
  src/main/java/keystone/npc/command/structure \
  src/main/java/keystone/npc/command/worldgen \
  src/main/java/keystone/npc/config \
  src/main/java/keystone/npc/logging \
  src/main/java/keystone/npc/validation \
  src/main/java/keystone/npc/error \
  src/main/java/keystone/npc/event/events \
  src/main/java/keystone/npc/features/quest \
  src/main/java/keystone/npc/features/raid \
  src/main/java/keystone/npc/features/worldgen_v2

# -----------------------------
# 2) State-Unterklassen nach state/internal verschieben
# -----------------------------

move_if_exists() {
  src="$1"
  dst="$2"

  if [ -f "$src" ]; then
    if [ -f "$dst" ]; then
      echo "SKIP: Ziel existiert schon: $dst"
    else
      mkdir -p "$(dirname "$dst")"
      mv "$src" "$dst"
      echo "MOVED: $src -> $dst"
    fi
  else
    echo "SKIP: Quelle fehlt: $src"
  fi
}

move_if_exists src/main/java/keystone/npc/state/WorldStateStore.java \
               src/main/java/keystone/npc/state/internal/WorldStateStore.java

move_if_exists src/main/java/keystone/npc/state/StatePathResolver.java \
               src/main/java/keystone/npc/state/internal/StatePathResolver.java

move_if_exists src/main/java/keystone/npc/state/StateFileIO.java \
               src/main/java/keystone/npc/state/internal/StateFileIO.java

move_if_exists src/main/java/keystone/npc/state/StateJsonCodec.java \
               src/main/java/keystone/npc/state/internal/StateJsonCodec.java

move_if_exists src/main/java/keystone/npc/state/StateBackupStore.java \
               src/main/java/keystone/npc/state/internal/StateBackupStore.java

# -----------------------------
# 3) Resources-Ordner anlegen
# -----------------------------

mkdir -p \
  src/main/resources/Server/NPC/Roles \
  src/main/resources/Server/NPC/Keystone/npc/lumberjack/appearances \
  src/main/resources/Server/NPC/Keystone/npc/lumberjack/routines \
  src/main/resources/Server/NPC/Keystone/npc/lumberjack/actions \
  src/main/resources/Server/NPC/Keystone/npc/citizen/appearances \
  src/main/resources/Server/NPC/Keystone/npc/citizen/routines \
  src/main/resources/Server/NPC/Keystone/npc/citizen/actions \
  src/main/resources/Server/NPC/Keystone/npc/traveler/appearances \
  src/main/resources/Server/NPC/Keystone/npc/traveler/routines \
  src/main/resources/Server/NPC/Keystone/npc/traveler/actions \
  src/main/resources/Server/NPC/Keystone/npc/hostile/appearances \
  src/main/resources/Server/NPC/Keystone/npc/hostile/routines \
  src/main/resources/Server/NPC/Keystone/npc/hostile/actions \
  src/main/resources/Server/NPC/Keystone/npc/mage/appearances \
  src/main/resources/Server/NPC/Keystone/npc/mage/routines \
  src/main/resources/Server/NPC/Keystone/npc/mage/actions \
  src/main/resources/Server/NPC/Keystone/profiles/skills \
  src/main/resources/Server/NPC/Keystone/profiles/movement \
  src/main/resources/Server/NPC/Keystone/profiles/navigation \
  src/main/resources/Server/NPC/Keystone/profiles/combat \
  src/main/resources/Server/NPC/Keystone/profiles/persistence \
  src/main/resources/Server/NPC/Keystone/profiles/spawn \
  src/main/resources/Server/NPC/Keystone/profiles/ai \
  src/main/resources/Server/NPC/Keystone/profiles/mana \
  src/main/resources/Server/NPC/Keystone/profiles/magic \
  src/main/resources/Server/NPC/Keystone/structures/houses \
  src/main/resources/Server/NPC/Keystone/structures/workplaces \
  src/main/resources/Server/NPC/Keystone/structures/roads \
  src/main/resources/Server/NPC/Keystone/structures/villages \
  src/main/resources/Server/NPC/Keystone/prefabs/houses \
  src/main/resources/Server/NPC/Keystone/prefabs/workplaces \
  src/main/resources/Server/NPC/Keystone/prefabs/village_pieces \
  src/main/resources/Server/NPC/Keystone/spawn_pools \
  src/main/resources/Server/NPC/Keystone/worldgen_v2/village_rules \
  src/main/resources/Server/NPC/Keystone/worldgen_v2/road_rules \
  src/main/resources/Server/NPC/Keystone/worldgen_v2/placement_sets \
  src/main/resources/Server/NPC/Keystone/features/quest/quests \
  src/main/resources/Server/NPC/Keystone/features/raid/raid_rules \
  src/main/resources/Server/NPC/Keystone/features/raid/raid_waves

echo "Fertig: Ordnerstruktur angelegt und State-Internal-Dateien verschoben."