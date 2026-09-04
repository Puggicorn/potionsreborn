# Potions Reborn — Brewing Concepts

## How the system works

- **Extraction brewing:** Brewing stands no longer use the vanilla per-potion recipes for mapped
  ingredients. Instead, brewing an ingredient extracts **up to 3 effects** directly into every
  valid bottle below it (including plain water bottles and even empty glass bottles — no Nether
  Wart primer required). Extracted potions use the *Extracted Potion* base and stack effects from
  multiple ingredient brews.
- **Data-driven:** The ingredient → effects mapping is a NeoForge **data map** attached to items.
  It lives in `data/<namespace>/data_maps/item/brewing_effects.json` and can be added to,
  overridden, or removed by datapacks. Tags are supported as keys (`"#minecraft:logs"` etc.).
- **The Centrifuge:** A brewing-stand-like station (Breeze Powder fuel, 20 s cycle) that
  *separates* a multi-effect potion. Put the potion in the top slot and empty bottles (or potions
  to overwrite) below. Each effect is moved into its own bottle. **If there are fewer bottles than
  effects, the leftover effects are distributed randomly over the available bottles** — e.g. a
  3-effect potion with only 2 bottles yields one single-effect and one double-effect bottle.

### Data format

```json
{
  "values": {
    "minecraft:spider_eye": {
      "effects": [
        { "id": "minecraft:poison", "duration": 900 },
        { "id": "potionsreborn:climbing", "duration": 3600, "amplifier": 0 }
      ]
    }
  }
}
```

- `id` — any registered mob effect (vanilla or modded).
- `duration` — ticks (20 = 1 second). Optional, defaults to 600 (30 s).
- `amplifier` — optional, defaults to 0 (level I).
- 1–3 effects per ingredient; the codec rejects empty or oversized lists.

## Vanilla ingredient research & effect concepts

Every vanilla brewing ingredient, what it is, and the extracted effects it ships with.
Most sets deliberately mix helpful and harmful effects so that the Centrifuge matters.

| Ingredient | Why (theme) | Effects | Good / Bad |
|---|---|---|---|
| **Spider Eye** | Spider venom, dark-dwelling eyes, wall-crawling legs | Poison 0:45 · **Climbing** 3:00 *(custom)* · Night Vision 3:00 | bad / good / good |
| **Fermented Spider Eye** | Vanilla's corruption agent — decayed, deceitful | Weakness 1:30 · Slowness 1:30 · Invisibility 1:00 | bad / bad / good |
| **Sugar** | Sugar rush, then the crash | Speed 3:00 · Jump Boost 1:30 · Hunger 0:30 | good / good / bad |
| **Rabbit's Foot** | Lucky charm; rabbits are jumpy but always eating | Jump Boost 3:00 · Luck 5:00 · Hunger 0:30 | good / good / bad |
| **Glistering Melon Slice** | Healing fruit dusted in glittering gold | Instant Health · Saturation · Glowing 0:30 | good / good / bad |
| **Pufferfish** | Toxic, but lets you survive its element | Water Breathing 3:00 · Poison 1:00 · Nausea 0:15 | good / bad / bad |
| **Magma Cream** | Living lava — heatproof, viscous, heat-hazed | Fire Resistance 3:00 · Slowness 0:45 · Nausea 0:20 | good / bad / bad |
| **Golden Carrot** | Enhanced sight, then light-sensitivity rebound | Night Vision 3:00 · Saturation · Blindness 0:15 | good / good / bad |
| **Blaze Powder** | Inner fire: burning strength that betrays and consumes | Strength 3:00 · Glowing 1:00 · Hunger 0:30 | good / bad / bad |
| **Ghast Tear** | Floaty, mournful creature — mends but frail | Regeneration 0:45 · Slow Falling 1:30 · Weakness 0:45 | good / good / bad |
| **Turtle Scute** | Turtle Master + aquatic nature | Slowness 1:30 · Resistance 0:20 · Water Breathing 3:00 | bad / good / good |
| **Phantom Membrane** | Wing membrane that still remembers flight | Slow Falling 3:00 · Levitation 0:03 · Nausea 0:20 | good / bad / bad |
| **Breeze Rod / Breeze Powder** | Compressed wind — tailwind, updraft, wind burst | Wind Charged 3:00 · Speed 1:30 · Levitation 0:02 | neutral / good / bad |
| **Slime Block** | Bouncy, sticky, and oozy | Oozing 3:00 · Jump Boost 1:30 · Slowness 0:30 | neutral / good / bad |
| **Stone** | Silverfish hide in it; mining through rock | Infested 3:00 · Haste 3:00 · Blindness 0:10 | bad / good / bad |
| **Cobweb** | Webs slow prey but spiders traverse them freely | Weaving 3:00 · **Climbing** 3:00 *(custom)* · Slowness 1:00 | neutral / good / bad |

### Ingredients intentionally left on vanilla behavior

These are *modifier* reagents rather than effect sources, so no data map entry ships for them and
brewing stands keep their vanilla behavior:

| Ingredient | Vanilla role | Notes |
|---|---|---|
| **Nether Wart** | Base primer (Awkward potion) | Optional in this system — extraction works on water bottles directly. Map it if you want it extractable. |
| **Glowstone Dust** | Amplifier (tier II) | Kept so extraction results can still be upgraded. |
| **Redstone Dust** | Duration extender | Kept for the same reason. |
| **Gunpowder** | Splash conversion | Kept — extracted potions can still be made splash/lingering. |
| **Dragon's Breath** | Lingering conversion | Kept for the same reason. |

> **Warning for datapack authors:** giving one of these modifiers a `brewing_effects` entry makes
> extraction *replace* its vanilla use entirely (extraction wins whenever a mapping exists).

## Custom content shipped

| Thing | Details |
|---|---|
| `potionsreborn:climbing` | Push against a wall to climb it like a spider; sneak to cling in place. Negates fall damage while active. Only affects players. |
| `potionsreborn:breeze_powder` | Crafted from a Breeze Rod (shapeless, ×2). Used as Centrifuge fuel; also extractable as an ingredient (see table above). |

## Known limitations / TODO

- Centrifuge model uses a placeholder vanilla texture (`smooth_stone`); a real model/texture is TODO.
- The centrifuge GUI reuses the vanilla brewing stand texture.
- Climbing only affects players (not mobs).
- Balance values (durations, fuel cost, cycle time) are first-pass concepts, not tuned.
