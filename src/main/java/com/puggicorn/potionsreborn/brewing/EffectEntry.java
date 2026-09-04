package com.puggicorn.potionsreborn.brewing;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;

/**
 * A single effect that can be extracted from an ingredient.
 *
 * @param effect    the mob effect
 * @param duration  duration in ticks (defaults to {@value #DEFAULT_DURATION})
 * @param amplifier the effect level - 1 (defaults to 0)
 */
public record EffectEntry(Holder<MobEffect> effect, int duration, int amplifier) {
    public static final int DEFAULT_DURATION = 600;

    public static final Codec<EffectEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        MobEffect.CODEC.fieldOf("id").forGetter(EffectEntry::effect),
        Codec.INT.optionalFieldOf("duration", DEFAULT_DURATION).forGetter(EffectEntry::duration),
        Codec.INT.optionalFieldOf("amplifier", 0).forGetter(EffectEntry::amplifier))
        .apply(instance, EffectEntry::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, EffectEntry> STREAM_CODEC = StreamCodec.composite(
        MobEffect.STREAM_CODEC,
        EffectEntry::effect,
        ByteBufCodecs.INT,
        EffectEntry::duration,
        ByteBufCodecs.INT,
        EffectEntry::amplifier,
        EffectEntry::new);

    public MobEffectInstance createInstance() {
        return new MobEffectInstance(this.effect, this.duration, this.amplifier);
    }
}
