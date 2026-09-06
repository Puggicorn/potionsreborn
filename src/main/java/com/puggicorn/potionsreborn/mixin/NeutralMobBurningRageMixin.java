package com.puggicorn.potionsreborn.mixin;

import com.puggicorn.potionsreborn.effect.ModEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.NeutralMob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Makes neutral mobs report as angry while Burning Rage is active. */
@Mixin(NeutralMob.class)
public interface NeutralMobBurningRageMixin {
    @Inject(method = "isAngry", at = @At("RETURN"), cancellable = true)
    private void potionsreborn$burningRageCountsAsAngry(CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue() && (Object)this instanceof LivingEntity entity
            && entity.hasEffect(ModEffects.BURNING_RAGE)) {
            cir.setReturnValue(true);
        }
    }
}
