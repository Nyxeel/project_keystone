<div align="center">

# 🧩 KeyEntityMod

**A Hytale NPC mod: persistent identity, state, roles, routines, and behavior on top of Hytale's native ECS architecture.**

![Language](https://img.shields.io/badge/language-Java-orange?logo=openjdk&logoColor=white)
![Engine](https://img.shields.io/badge/engine-Hytale-blueviolet)
![Status](https://img.shields.io/badge/status-foundation%20%2F%20skeleton-yellow)

</div>

---

## 📖 About

KeyEntityMod is a Java mod for Hytale that gives NPCs a life beyond a single spawned entity: persistent records, roles, routines, and behavior, built on top of Hytale's native ECS (Entity-Component-System) architecture instead of fighting it.

## 📑 Table of Contents

- [Features](#-features)
- [Design Philosophy](#-design-philosophy)
- [Architecture (ECS Background)](#-architecture-ecs-background)
- [Package Structure](#-package-structure)
- [Development Phases](#-development-phases)
- [Not Yet in Scope](#-not-yet-in-scope)
- [Requirements](#️-requirements)
- [Author](#️-author)

## ✨ Features

- **Persistent NPC records** — role, world, position, marker assignments — survive server restarts via a per-world `state.json`.
- **Runtime vs. persisted separation** — live entity data (`RuntimeNpc`) is never written to disk; only durable data (`NpcRecord`) is persisted.
- **JSON-driven definitions** — roles, routines, navigation, combat, appearance, and more are described in data, not hardcoded in Java.
- **Markers & structures** — NPCs bind to logical markers (e.g. `main_worker`, `spouse`) inside prefab-based structures instead of fixed coordinates.
- **Safety-first lifecycle** — spawn, relink, respawn, and removal all go through explicit gates (chunk loaded, entity alive, ownership proven) before anything happens.

## 🎯 Design Philosophy

The current build stage is **foundation only** — a skeleton that defines data shapes, boundaries, and failure checks before any real engine behavior is wired in.

Guiding rules followed throughout the codebase:

- **No fake engine logic.** Hytale API behavior is verified against `patcher.zip` and [hytalemodding.dev](https://hytalemodding.dev) before anything is implemented — nothing is guessed.
- **Strict separation of concerns.** Data models never contain runtime/engine objects; managers never touch files directly; state stores never contain business logic.
- **Fail safe, not silent.** Failed loads never overwrite existing state, failed saves are reported (not swallowed), and missing markers pause NPCs rather than "repairing" them behind the scenes.
- **Everything is data-first.** Definitions, profiles, routines, and compositions are described in JSON and loaded generically.

## 🧠 Architecture (ECS Background)

KeyEntityMod builds on top of Hytale's native **Entity-Component-System**, rather than classic OOP inheritance:

- **Entities** are just IDs — no data, no logic.
- **Components** are pure data (`Position`, `Health`, `AI`, ...) that can be freely combined.
- **Systems** contain all behavior and operate on any entity that has the right components.

The engine hierarchy is `Universe → World → EntityStore`, with entities accessed safely through `Ref` wrappers and filtered efficiently through queries and archetypes. KeyEntityMod's own systems (routines, navigation, lifecycle, etc.) are designed to sit cleanly on top of this model rather than fight it.

## 📂 Package Structure

| Package | Responsibility |
|---|---|
| `model/` | Data shapes: `NpcRecord`, `RuntimeNpc`, `PersistedWorldState`, `NpcState`, `NpcEntityStatus` |
| `state/` | Per-world `state.json` load/save, path resolution, JSON codec, backups |
| `core/` | `NpcManager` — the in-RAM bridge between persisted state and live NPCs |
| `world/` | World identification (`WorldKey`), chunk gating and load tracking |
| `definition/` | JSON-driven NPC definitions and behavior profiles (routine, navigation, movement, combat, appearance, skill, spawn, action, AI, mana, magic) |
| `marker/` | Logical → concrete marker resolution and assignment per NPC instance |
| `structure/` | Prefab-bound structures, NPC slots, spawn composition, protection policy |
| `lifecycle/` | Spawn, relink, respawn, and removal — each with explicit result objects |
| `runtime/` | Tick pipeline gated behind a valid, live entity (`LiveEntityGate`) |
| `routine/` & `action/` | Day-plan routines and the actions (animation/sound) they trigger at markers |
| `navigation/` & `road/` | Generic, profile-driven movement and road-graph walking |
| `command/`, `config/`, `logging/`, `validation/`, `error/` | Safety tooling: safe-by-default commands, centralized validation, cooldown-aware logging, rollback-capable error handling |
| `event/` & `features/` | Event bus plus higher-level features (quests, raids, world-gen v2) built *on top of* the core systems, never bypassing them |

## 🗺 Development Phases

The mod is being built in a strict, dependency-ordered sequence — each phase only becomes real once the one before it is solid:

1. **Model skeleton** — persisted vs. runtime data shapes
2. **State system** — per-world `state.json` load/save/backup
3. **State ↔ NpcManager bridge** — restoring records into RAM
4. **World system** — world identification & chunk gating
5. **Definition skeleton** — JSON-driven NPC blueprints & profiles
6. **Marker v2** — logical markers → concrete marker instances
7. **Structure / prefab skeleton** — prefab-bound NPC slots & compositions
8. **Lifecycle results** — spawn / relink / respawn / remove outcomes
9. **Runtime & tick** — `LiveEntityGate`-protected tick pipeline
10. **Routine & action** — day plans and marker-bound actions
11. **Navigation & road** — generic, profile-driven movement
12. **Command / config / logging / validation / error** — safety tooling
13. **Event & feature layer** — quests, raids, world-gen v2 on top of the core

Every phase ships with its own **failcheck list** (e.g. *"no spawn without a record"*, *"no tick without an EntityRef"*, *"missing marker pauses, never silently repairs"*) that guards against shortcuts creeping into the foundation.

## 🚧 Not Yet in Scope

The current skeleton deliberately does **not** yet include:

- Real Hytale entity spawning
- A real `EntityRef` type / UUID-based relinking
- Real chunk gating
- Real prefab placement
- Real animation/sound execution
- Real combat logic
- Real world generation
- Real quest or raid logic
- Real mana/magic runtime

These are held back until the corresponding Hytale API behavior has been verified against `patcher.zip` and hytalemodding.dev.

## ⚙️ Requirements

- Java (matching the target Hytale server modding API)
- A Hytale server environment supporting entity/component modding

## 🙋 Author

**[@Nyxeel](https://github.com/Nyxeel)**
