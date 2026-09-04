# Potions Reborn

A NeoForge mod for **Minecraft 1.21.1** about **extracting** potion effects and **separating** them.

## Extraction brewing

Brew an ingredient in a brewing stand to pull **up to 3 effects** out of it. You still need a real
base potion first — Nether Wart → Awkward Potion, as usual. The result is an **Extracted Potion**,
and brewing more ingredients into it **stacks** effects. Most ingredients give a mix of good and
bad effects, so raw extracts are messy.

## The Centrifuge

Splits a multi-effect potion back into clean potions. Craft it like a brewing stand: a **Breeze Rod
over 3 Iron Blocks**.

- **Top slot:** one potion with effects to separate.
- **Bottom 3 slots:** water bottles or awkward potions (these become the results).
- **Corner slot:** **Breeze Powder** fuel (crafted shapeless from a Breeze Rod → 2).

Press **Start**. Each effect moves into its own bottle; if there are fewer bottles than effects,
the extras land in random bottles.

## For datapacks: extraction recipes

Each ingredient's effects are a normal datapack recipe of type `potionsreborn:effect_extraction`,
one file per ingredient in `data/<namespace>/recipe/`. Datapacks can add or override them.

```json
{
  "type": "potionsreborn:effect_extraction",
  "ingredient": { "item": "minecraft:spider_eye" },
  "effects": [
    { "id": "minecraft:poison", "duration": 900 },
    { "id": "potionsreborn:climbing", "duration": 3600, "amplifier": 0 },
    { "id": "minecraft:night_vision", "duration": 3600 }
  ]
}
```

- `id` — any mob effect, vanilla or modded.
- `duration` — ticks (20 = 1 s); optional, default 600.
- `amplifier` — optional, 0 = level I.
- 1–3 effects per recipe.

`ingredient` can also use a tag:

```json
"ingredient": { "tag": "minecraft:logs" }
```

## Config

`config/potionsreborn-common.toml`:

- `centrifugeFuelUses` (default `20`) — operations per Breeze Powder.
- `centrifugeProcessTime` (default `400`) — ticks per separation.