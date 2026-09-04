package com.puggicorn.potionsreborn.client;

import com.puggicorn.potionsreborn.menu.CentrifugeMenu;
import com.puggicorn.potionsreborn.network.CentrifugeStartPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Screen for the Centrifuge. Uses the vanilla brewing stand GUI — including its arrow and bubbles
 * animation — but draws the fuel bar in cyan and adds a start button.
 */
@OnlyIn(Dist.CLIENT)
public class CentrifugeScreen extends AbstractContainerScreen<CentrifugeMenu> {
    private static final ResourceLocation BREW_PROGRESS_SPRITE = ResourceLocation.withDefaultNamespace("container/brewing_stand/brew_progress");
    private static final ResourceLocation BUBBLES_SPRITE = ResourceLocation.withDefaultNamespace("container/brewing_stand/bubbles");
    private static final ResourceLocation FUEL_LENGTH_SPRITE = ResourceLocation.fromNamespaceAndPath("potionsreborn", "container/centrifuge/fuel_length");
    private static final ResourceLocation TEXTURE = ResourceLocation.withDefaultNamespace("textures/gui/container/brewing_stand.png");
    private static final int[] BUBBLE_LENGTHS = new int[]{29, 24, 20, 16, 11, 6, 0};
    private static final int BUTTON_X = 123;
    private static final int BUTTON_Y = 33;
    private static final int BUTTON_W = 48;
    private static final int BUTTON_H = 16;

    private Button startButton;

    public CentrifugeScreen(CentrifugeMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
        this.startButton = this.addRenderableWidget(Button.builder(this.getButtonLabel(), button -> this.toggleRunning())
            .bounds(this.leftPos + BUTTON_X, this.topPos + BUTTON_Y, BUTTON_W, BUTTON_H)
            .build());
    }

    private Component getButtonLabel() {
        return this.menu.isRunning()
            ? Component.translatable("gui.potionsreborn.centrifuge.stop")
            : Component.translatable("gui.potionsreborn.centrifuge.start");
    }

    private void toggleRunning() {
        boolean newState = !this.menu.isRunning();
        this.menu.setRunning(newState);
        PacketDistributor.sendToServer(new CentrifugeStartPayload(newState));
        this.startButton.setMessage(this.getButtonLabel());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Keep the button label and enabled state in sync with the server-side block entity.
        this.startButton.setMessage(this.getButtonLabel());
        this.startButton.active = this.menu.isRunning() || this.menu.canStart();
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(TEXTURE, i, j, 0, 0, this.imageWidth, this.imageHeight);

        // Cyan-tinted copy of the vanilla fuel bar sprite, cropped to the remaining fuel.
        int fuel = this.menu.getFuel();
        int fuelLength = Mth.clamp((18 * fuel + this.menu.getFuelUses() - 1) / this.menu.getFuelUses(), 0, 18);
        if (fuelLength > 0) {
            guiGraphics.blitSprite(FUEL_LENGTH_SPRITE, 18, 4, 0, 0, i + 60, j + 44, fuelLength, 4);
        }

        // Vanilla arrow + bubbles animation while processing. The centrifuge's processTime counts
        // up from 0 to the total, so the fill fraction is processTicks / processTime directly.
        int processTicks = this.menu.getProcessTicks();
        if (processTicks > 0) {
            int processTime = this.menu.getProcessTime();
            int arrow = (int)(28.0F * ((float)processTicks / (float)processTime));
            if (arrow > 0) {
                guiGraphics.blitSprite(BREW_PROGRESS_SPRITE, 9, 28, 0, 0, i + 97, j + 16, 9, arrow);
            }

            int bubbleLength = BUBBLE_LENGTHS[processTicks / 2 % 7];
            if (bubbleLength > 0) {
                guiGraphics.blitSprite(BUBBLES_SPRITE, 12, 29, 0, 29 - bubbleLength, i + 63, j + 14 + 29 - bubbleLength, 12, bubbleLength);
            }
        }
    }
}
