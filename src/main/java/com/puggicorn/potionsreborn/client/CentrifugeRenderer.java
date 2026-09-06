package com.puggicorn.potionsreborn.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.puggicorn.potionsreborn.PotionsRebornMod;
import com.puggicorn.potionsreborn.block.entity.CentrifugeBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.neoforged.neoforge.client.RenderTypeHelper;
import net.neoforged.neoforge.client.model.data.ModelData;

/** Renders the authored centrifuge rotor separately so it can rotate without moving the stand. */
public class CentrifugeRenderer implements BlockEntityRenderer<CentrifugeBlockEntity> {
    public static final ModelResourceLocation ROTOR_MODEL = ModelResourceLocation.standalone(
        ResourceLocation.fromNamespaceAndPath(PotionsRebornMod.MODID, "block/centrifuge"));

    @Override
    public void render(CentrifugeBlockEntity centrifuge, float partialTick, PoseStack poseStack,
            MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        BakedModel model = Minecraft.getInstance().getModelManager().getModel(ROTOR_MODEL);
        poseStack.pushPose();
        // The rotor's Blockbench origin is [8, 15, 8]. Rotate around that exact in-block pivot.
        poseStack.translate(0.5D, 15.0D / 16.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(centrifuge.getRotorAngle(partialTick)));
        poseStack.translate(-0.5D, -15.0D / 16.0D, -0.5D);

        ModelBlockRenderer renderer = Minecraft.getInstance().getBlockRenderer().getModelRenderer();
        for (RenderType renderType : model.getRenderTypes(centrifuge.getBlockState(), RandomSource.create(42L), ModelData.EMPTY)) {
            renderer.renderModel(
                poseStack.last(),
                bufferSource.getBuffer(RenderTypeHelper.getEntityRenderType(renderType, false)),
                centrifuge.getBlockState(), model, 1.0F, 1.0F, 1.0F,
                packedLight == LightTexture.FULL_BRIGHT ? packedLight : packedLight,
                packedOverlay, ModelData.EMPTY, renderType);
        }
        poseStack.popPose();
    }
}