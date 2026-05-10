# Keystone – Core System Open Topics

## Status
Internal Design Checklist – Pre-Balance Phase

This document summarizes all major core-system topics that still require definition, numerical balancing, or structural finalization.

---

# 1. Arcane Flow / Mana System

### Core Balance Questions
- Exact mana generation per player (baseline vs. prestige distribution)
- Continuous mana flow vs. interval-based pulse system
- Crystal storage capacity per level (exact numbers)
- Upgrade cost curve (linear, exponential, hybrid?)
- Maximum sustainable running costs before deficit
- Behavior when mana pool is empty (neutral state vs. soft instability)
- Overflow trigger logic when crystal reaches full capacity

This is the heart of the system.

---

# 2. City Progression & Expansion

- Exact plot size (fixed 32×32 or scalable?)
- Expansion unlock condition (e.g. 90% occupancy threshold?)
- Maximum city size (hard cap?)
- Timing of first expansion milestone
- Total number of crystal levels planned

---

# 3. Prestige System – Finalization

- Can prestige decay or be revalidated?
- Is prestige permanent once earned?
- Are there visual tiers beyond mechanical cap?
- How often can a building re-enter voting?

---

# 4. PvP Crystal Mines (Phase 2)

- Capture mechanics (timer, interaction, event-based?)
- Duration of control
- Automatic reset rules
- Transport mechanic for crystals (physical or direct deposit?)
- Maximum boost cap per city
- Guard scaling limits and maintenance rules

---

# 5. City Plot & Organization System

- Role assignment method (manual vs. voted)
- Term duration for leadership roles
- Safeguards against abuse of power
- Rehabilitation system after city-plot ban

---

# 6. Migration & Population Stability

- Minimum active population per city
- Rules if a city drops below threshold
- Inactivity handling for plots
- Possibility of city merge or restructuring

---

# 7. World Integration

- Farm world ↔ City transport risk mechanics
- Dungeon reward structure (raw crystals vs. boss materials)
- PvP outpost spawn logic
- Performance limits for city entities (guards, automation, NPC density)

---

# 8. Automation System

- Number of processing slots per city level
- Queue management rules
- Maintenance mana cost scaling
- Output balance
- Pause / disable mechanic
- Automation cap to prevent dominance

---

# 9. Economic Scaling

- Gold ↔ Mana relationship (exchange or separation?)
- Long-term gold sink sufficiency
- Inflation risk in large cities
- Economic interaction between cities

---

# 10. Long-Term Meta Progression

- Realistic duration per crystal level
- Weeks/months to reach Metropolis stage
- Permanent progression vs. seasonal reset model
- Endgame cap or soft ceiling

---

# Next Step

Prioritize:

- Must be finalized before launch
- Can be introduced in Phase 2
- Requires mathematical simulation

This checklist defines the remaining structural work before balancing begins.
