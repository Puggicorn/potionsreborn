package com.puggicorn.potionsreborn.potion;

import com.puggicorn.potionsreborn.PotionsRebornMod;
import com.puggicorn.potionsreborn.effect.ModEffects;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModPotions {
    public static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(Registries.POTION, PotionsRebornMod.MODID);

    /**
     * Base potion used for extraction output. It carries no effects of its own; the actual effects
     * live in the potion's custom effects component, and it reads "Extracted Potion".
     */
    public static final DeferredHolder<Potion, Potion> EXTRACTED = POTIONS.register("extracted", () -> new Potion("potionsreborn.extracted"));

    /**
     * Named base potions for the mod's custom effects, so centrifuged single-effect results read
     * "Potion of Climbing" etc. The name drives the vanilla translation key
     * {@code item.minecraft.potion.effect.<name>}.
     */
    public static final DeferredHolder<Potion, Potion> CLIMBING = POTIONS.register("climbing",
        () -> new Potion("climbing", new MobEffectInstance(ModEffects.CLIMBING, 3600)));
    public static final DeferredHolder<Potion, Potion> BURNING_RAGE = POTIONS.register("burning_rage",
        () -> new Potion("burning_rage", new MobEffectInstance(ModEffects.BURNING_RAGE, 1800)));

    private ModPotions() {
    }

    public static void register(IEventBus modEventBus) {
        POTIONS.register(modEventBus);
    }

    /**
     * Finds a base potion whose single effect matches the given effect, searching the entire potion
     * registry (vanilla and modded), so centrifuge output reads "Potion of <Name>". Empty when no
     * registered base potion provides this effect.
     */
    public static java.util.Optional<Holder<Potion>> basePotionFor(Registry<Potion> potionRegistry, Holder<MobEffect> effect) {
        for (Potion potion : potionRegistry) {
            java.util.List<MobEffectInstance> effects = potion.getEffects();
            if (effects.size() == 1 && effects.get(0).getEffect().is(effect.getKey())) {
                return java.util.Optional.of(potionRegistry.wrapAsHolder(potion));
            }
        }
        return java.util.Optional.empty();
    }
}
