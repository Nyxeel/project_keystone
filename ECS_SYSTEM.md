## ########################################
## Hytale ECS System – Concise Overview
## ########################################


### Core Idea: Why Hytale Uses ECS

Hytale does not use a traditional object-oriented model based on classes and inheritance for game entities. Instead, it relies on an **Entity-Component System (ECS)**.

The reason is straightforward: inheritance breaks down when features must be freely combinable. Common problems include rigid class hierarchies, the diamond problem, and class explosion. ECS avoids these issues by favoring **composition over inheritance**.

---

### The Three Pillars of ECS

**Entities** are nothing more than unique identifiers. They contain no data and no logic. An entity is simply an ID, such as “Entity 42”.

**Components** are pure data containers with no behavior. Examples include Position, Health, Velocity, AI, Flying, or FireBreath. Components can be combined freely. A “flying fire pig” is created by attaching the appropriate components rather than defining a special class.

**Systems** contain all behavior and logic. They have no persistent state and operate on entities that possess a required set of components. For example, a Movement System processes entities with Position and Velocity, while a Damage System processes entities with Health. Systems do not care what an entity *is*, only what it *has*.

This requires a mental shift: stop asking “What type of object is this?” and instead ask, **“What capabilities does this entity have?”**

---

### Hytale ECS Architecture

At the top level is the **Universe**, a singleton that exists once per server. It is accessed via `Universe.get()` and contains Worlds, PlayerRefs, and PlayerStorage.

A **World** represents a single game instance. It contains the EntityStore (the ECS core), chunk and terrain data, players currently in the world, and world-specific state such as time and weather.

The **EntityStore** is the heart of the ECS. It stores all entities and their components and supports adding, removing, retrieving, and checking components. All interaction with entities happens through references rather than direct objects.

---

### Ref (Entity Reference)

A **Ref** is a wrapper around an entity ID and is comparable to a pointer or address. It can become invalid if the entity is removed and is used throughout systems and events to refer to entities safely.

---

### ComponentType

Every component has a unique **ComponentType** identifier, internally represented by an integer. This approach is far faster than reflection or class-based lookups and is critical for high-performance iteration over large numbers of entities.

---

### Common ECS Patterns

Reading components follows a consistent pattern: obtain a Ref, access the EntityStore, request the component via its ComponentType, and read the data.

Writing components involves adding a new component, modifying an existing one, or removing a component entirely.

A common trigger pattern uses components as signals. For example, teleporting an entity involves adding a TeleportComponent. A TeleportSystem processes it and then removes the component afterward.

---

### System Types in Hytale

An **EntityTickingSystem** runs every tick or frame and uses queries and delta time (`dt`). A typical example is poison damage applied over time.

A **RefSystem** provides lifecycle hooks such as `onEntityAdded` and `onEntityRemoved`. These are used for join and leave logic, initialization, and cleanup.

A **RefChangeSystem** reacts to component changes.

A **DamageEventSystem** intercepts and modifies damage, enabling armor calculations, invulnerability, and damage logging.

Queries are central to all systems. They efficiently filter entities using AND, OR, and NOT logic so systems only process relevant entities.

---

### Archetypes and Performance

Entities with identical component sets are grouped into **archetypes**. Queries can skip entire archetype groups that do not match, dramatically improving performance. Components are stored contiguously in memory using a cache-friendly **Structure of Arrays** layout, which is a key reason for Hytale’s ECS efficiency.

---

### Important Specialized Systems

The **StatMap / EntityStats** system provides a unified and extensible stat framework for health, mana, stamina, hunger, and more. Mods can easily add custom stats.

The **Transform** component stores position and body rotation, while **HeadRotation** stores camera or view direction. Both are required for correct teleportation and orientation handling.

---

### Interactions (Not ECS)

Interactions are a separate system used for time-based player actions such as attacking, placing blocks, or firing projectiles. They handle animation timing, execution order, and cooldowns. ECS modifications occur only at the execution moment, such as when a damage event is applied.

Interactions are often defined in JSON rather than being hardcoded.

---

### The Big Picture

The overall structure is **Universe → World → EntityStore**.
Entities are IDs, components are data, and systems are logic. Queries and archetypes provide performance, while interactions manage action timing outside the ECS.

Hytale does not model objects. It models **capabilities, data flow, and systems**.
