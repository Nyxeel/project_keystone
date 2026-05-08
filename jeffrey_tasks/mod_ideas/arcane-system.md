# Keystone – Manastein, Decay & Arcane Flow System

# Keystone – Manastein, Decay & Arcane Flow System
## Headings Overview

## 1. Scope & Phase Definition

### Launch Phase Focus (PvE-Only)

## 2. Dual Energy Architecture

### A) Decay Energy (Claim Layer)

### B) City Mana (Arcane Flow Layer)

## 3. Manastein / Resonance Crystal

### Core Properties

### Capacity Logic

## 4. City Growth & Scaling

### Growth Method

### Scaling Philosophy

## 5. Plot & Claim Model

## 6. Global Spawn Energy System

## 7. Prestige & Mana Output System

### Prestige Acquisition

### Approval-Based Prestige Levels

### Prestige Cap

### Output Example

### Civic Resonance (Temporary Participation Bonus)

## 8. Mana Sources

## 9. Mana Costs Structure

### One-Time Costs (Infrastructure)

### Running Costs (Active Power)

## 10. Automation Mana

## 11. Economic Guardrails

## 12. Technical Architecture

## 13. UX & Feedback

## 14. PvP Mines (Phase 2 – Reserved)

## 15. Architectural Philosophy

## 16. Open Design Areas (To Be Balanced)

## System Identity


## Consolidated Core Specification (PvE Start Phase)

This document merges all previously defined systems:
- Decay / Claim Energy
- Manastein / Resonance Crystal
- Prestige & Mana Output
- Arcane Flow Economy
- City Growth
- PvP-Mine Phase 2 Planning
- Economy Constraints
- Technical & Architectural Decisions

No previously defined rule has been removed.

---

# 1. Scope & Phase Definition

## Launch Phase Focus (PvE-Only)

- No PvP at launch
- No raid mechanics
- No mine control system active
- Focus on:
  - Claim protection
  - Energy economy
  - City progression
  - Global PvE events
  - Prestige & Housing voting

PvP crystal mines exist as a Phase 2 feature (design reserved, not implemented at start).

---

# 2. Dual Energy Architecture

Keystone uses two distinct energy layers.

---

## A) Decay Energy (Claim Layer)

### Purpose
Maintains plot protection and contributes to city stability.

### Properties

- Manually inserted into a **1-block Energy Converter**
- Non-automatable
- Consumes Energy Crystals
- Converts crystals into mana streams flowing toward the central Manastein

### Effects

- Keeps individual claim active
- Contributes indirectly to city mana pool
- If energy stops:
  - Claim protection falls
  - Buildings remain
  - No destruction occurs

Decay = protection system, not destruction mechanic.

---

## B) City Mana (Arcane Flow Layer)

Flow:

Player → Energy Crystal → Converter → Arcane Flow → Manastein → City Mana Pool

City Mana is used for:

- Infrastructure
- Upgrades
- Public projects
- Buffs
- Guard maintenance
- Automation

Prestige modifies output only at this layer.

---

# 3. Manastein / Resonance Crystal

Each city has one central Manastein.

## Core Properties

- Has storage capacity
- Capacity increases per level
- Stores City Mana
- Acts as:
  - Energy reservoir
  - Upgrade gate
  - Event trigger core
  - Progression regulator

---

## Capacity Logic

- Capacity scales per level
- If storage reaches maximum:
  - Bonus event may trigger
  - Special merchant may appear
  - Temporary +2% city mana output possible
- Overflow should not silently waste energy (design recommendation)

The Manastein is both:
- Storage
- Soft regulator

---

# 4. City Growth & Scaling

## Growth Method

- Via Manastein upgrades
- Requires:
  - Mana from pool
  - Materials
  - Possibly boss materials
  - No PvP dependency at launch

## Scaling Philosophy

- Larger city → higher contribution threshold
- Not a drain model
- Positive milestone model

Possible scaling logic:
Similar to Bitcoin difficulty — level difficulty increases with total output.

Prevents mass-player snowballing.

---

# 5. Plot & Claim Model

- 32×32 grid plots
- Max 2 per account (Decay spec)
- Claim logic per plot
- Claim protection tied to Decay Energy

When claim expires:

- Only protection is removed
- No block deletion

---

# 6. Global Spawn Energy System

A portion of city energy flows into a global meter.

At thresholds:

- Random global events
- Rare merchants
- Environmental anomalies
- PvE encounters

Event rules:

- Probabilistic triggers
- No scheduled waves
- Balanced for large vs small cities

---

# 7. Prestige & Mana Output System

Prestige increases only **city mana output**, not personal power.

---

## Prestige Acquisition

Only via successful Housing Voting.

Requirements:

- 3-day voting cycle
- Minimum participation achieved
- Valid vote conclusion

---

## Approval-Based Prestige Levels

- ≥60% → +1 level
- ≥80% → +2 levels
- ≥90% → +3 levels (rare)

---

## Prestige Cap

Example:

- P1: +4%
- P2: +7%
- P3: +9%
- P4: +10.5%
- P5: +12% (hard cap)

Above cap:

- Visual upgrades only
- No further mechanical bonus

---

## Output Example

Default:
100 energy → 100 city mana

Prestige P3 (+9%):
100 energy → 109 city mana

Extra mana flows only into city systems.

---

## Civic Resonance (Temporary Participation Bonus)

- +3–5% personal efficiency
- 24h duration
- Non-stackable
- Granted for full voting participation
- Separate from Prestige

Prestige = permanent city bonus
Resonance = temporary participation incentive

---

# 8. Mana Sources

- Default player ≈ 100 mana baseline
- Prestige player ≈ up to 115 mana
- Phase 2: Neutral crystal mines → 150 mana fixed
  - Equivalent to 1–2 players
  - Not upgradable
  - Boost, not win condition

Mine system reserved for Phase 2.

---

# 9. Mana Costs Structure

## One-Time Costs (Infrastructure)

- Manastein level upgrades
- New plot rows
- City-plot unlock
- Library
- Automation Hall
- Auction Hall
- Ritual Plaza
- Technology unlocks

Infrastructure never drains continuously.

---

## Running Costs (Active Power)

- Mine guards (Phase 2)
- Temporary city buffs
- Shield reinforcement
- Active automation
- Resonance Overdrive

Principle:
Power & advantage cost maintenance.
Structure does not.

---

# 10. Automation Mana

- Separate energy layer
- Machines consume mana dynamically
- Behavior under shortage:
  - Pause (recommended)
  - No damage
- Limited slot system
- City-wide, not individual spam

---

# 11. Economic Guardrails

- No voting-generated gold
- Gold only from gameplay
- Strong gold sinks
- Physical player shops
- Zoned economy
- No global auction house

Mana economy and gold economy remain distinct.

---

# 12. Technical Architecture

- Event-based system
- No world scans
- Uses counters, scheduled tasks, events
- Persistence options:
  - JSON
  - SQLite
  - API-dependent
- Audit logs:
  - Energy insertion
  - Claim changes
  - Upgrades

Admin tools:
- Read-only overview
- Repair options (restricted)

---

# 13. UX & Feedback

- HUD display
- Warning thresholds (60 / 30 / 10 min)
- Mana Seal above plots
- Visible expansion rings
- Lore explanation for Resonance

Transparency recommended for:
- City energy levels
- Upgrade thresholds

---

# 14. PvP Mines (Phase 2 – Reserved)

- 2–3 neutral mines on shared island
- Fixed output ~150 mana
- Controlled via PvP
- Guard entities purchasable via mana
- Guard maintenance optional
- Control not game-deciding
- Boost only (~5% city output scale)

Not active at launch.

---

# 15. Architectural Philosophy

- No punishment-first mechanics
- Bonus-based incentives
- City strength = collective participation
- Prestige is political, not combative
- Decay removes protection, not builds
- Infrastructure is stable
- Active power requires upkeep

---

# 16. Open Design Areas (To Be Balanced)

- Exact mana-per-player math
- Level upgrade cost curve
- Capacity per level
- Maintenance ceilings
- Overflow event probabilities
- Global event fairness
- Automation slot scaling
- Mine control mechanics (Phase 2)

---

# System Identity

Keystone is not:

- A grind simulator
- A PvP domination ladder
- A destruction-based decay system

It is:

A collective civilization engine driven by energy flow, political approval, and visible architectural evolution.
