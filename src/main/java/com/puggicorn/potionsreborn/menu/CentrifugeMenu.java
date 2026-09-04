package com.puggicorn.potionsreborn.menu;

import com.puggicorn.potionsreborn.block.entity.CentrifugeBlockEntity;
import com.puggicorn.potionsreborn.item.ModItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;

/**
 * Menu for the Centrifuge, mirroring the brewing stand: input potion up top (center), three output
 * bottles in the triangle below, Breeze Powder fuel in the top-left corner, and a start button.
 */
public class CentrifugeMenu extends AbstractContainerMenu {
    private static final int BOTTLE_SLOT_START = CentrifugeBlockEntity.BOTTLE_SLOT_START;
    private static final int INPUT_SLOT = CentrifugeBlockEntity.INPUT_SLOT;
    private static final int FUEL_SLOT = CentrifugeBlockEntity.FUEL_SLOT;
    private static final int SLOT_COUNT = CentrifugeBlockEntity.SLOT_COUNT;
    private static final int DATA_COUNT = CentrifugeBlockEntity.NUM_DATA_VALUES;
    private static final int INV_SLOT_START = SLOT_COUNT;
    private static final int INV_SLOT_END = SLOT_COUNT + 27;
    private static final int USE_ROW_SLOT_START = INV_SLOT_END;
    private static final int USE_ROW_SLOT_END = USE_ROW_SLOT_START + 9;
    /** Dimmed breeze powder outline rendered in the fuel slot while it is empty. */
    private static final ResourceLocation EMPTY_SLOT_BREEZE_POWDER = ResourceLocation.fromNamespaceAndPath("potionsreborn", "item/empty_slot_breeze_powder");

    private final Container centrifuge;
    private final ContainerData centrifugeData;

    public CentrifugeMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new SimpleContainer(SLOT_COUNT), new SimpleContainerData(DATA_COUNT));
    }

    public CentrifugeMenu(int containerId, Inventory playerInventory, Container centrifugeContainer, ContainerData centrifugeData) {
        super(ModMenus.CENTRIFUGE.get(), containerId);
        checkContainerSize(centrifugeContainer, SLOT_COUNT);
        checkContainerDataCount(centrifugeData, DATA_COUNT);
        this.centrifuge = centrifugeContainer;
        this.centrifugeData = centrifugeData;

        // Three output bottles in the brewing stand triangle, input potion above center, fuel top-left.
        this.addSlot(new BottleSlot(centrifugeContainer, 0, 56, 51));
        this.addSlot(new BottleSlot(centrifugeContainer, 1, 79, 58));
        this.addSlot(new BottleSlot(centrifugeContainer, 2, 102, 51));
        this.addSlot(new InputSlot(centrifugeContainer, INPUT_SLOT, 79, 17));
        this.addSlot(new FuelSlot(centrifugeContainer, FUEL_SLOT, 17, 17))
            .setBackground(InventoryMenu.BLOCK_ATLAS, EMPTY_SLOT_BREEZE_POWDER);
        this.addDataSlots(centrifugeData);

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for (int k = 0; k < 9; k++) {
            this.addSlot(new Slot(playerInventory, k, 8 + k * 18, 142));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return this.centrifuge.stillValid(player);
    }

    /** Toggles the running state on the server-side block entity. */
    public void setRunning(boolean running) {
        if (this.centrifuge instanceof CentrifugeBlockEntity be) {
            be.setRunning(running);
        }
    }

    public boolean isRunning() {
        return this.centrifugeData.get(CentrifugeBlockEntity.DATA_RUNNING) != 0;
    }

    /** Whether the start button should be enabled (has fuel and a potion with effects to separate). */
    public boolean canStart() {
        // Prefer the live block entity (singleplayer) so the check is exact; otherwise fall back to
        // the synced fuel value plus the slot contents, which are always present on the client.
        boolean hasFuel = this.centrifuge instanceof CentrifugeBlockEntity be ? be.getFuel() > 0 : this.getFuel() > 0;
        return hasFuel && hasSeparableInput() && hasOutputBottle();
    }

    private boolean hasSeparableInput() {
        return InputSlot.mayPlaceItem(this.centrifuge.getItem(CentrifugeBlockEntity.INPUT_SLOT));
    }

    private boolean hasOutputBottle() {
        for (int i = CentrifugeBlockEntity.BOTTLE_SLOT_START; i <= CentrifugeBlockEntity.BOTTLE_SLOT_END; i++) {
            if (BottleSlot.mayPlaceItem(this.centrifuge.getItem(i))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();
            if (index >= 0 && index < SLOT_COUNT) {
                if (!this.moveItemStackTo(stack, INV_SLOT_START, USE_ROW_SLOT_END, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(stack, result);
            } else if (FuelSlot.mayPlaceItem(stack)) {
                if (!this.moveItemStackTo(stack, FUEL_SLOT, FUEL_SLOT + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (InputSlot.mayPlaceItem(stack)) {
                if (!this.moveItemStackTo(stack, INPUT_SLOT, INPUT_SLOT + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (BottleSlot.mayPlaceItem(stack)) {
                if (!this.moveItemStackTo(stack, BOTTLE_SLOT_START, BOTTLE_SLOT_START + 3, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= INV_SLOT_START && index < INV_SLOT_END) {
                if (!this.moveItemStackTo(stack, USE_ROW_SLOT_START, USE_ROW_SLOT_END, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= USE_ROW_SLOT_START && index < USE_ROW_SLOT_END) {
                if (!this.moveItemStackTo(stack, INV_SLOT_START, INV_SLOT_END, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(stack, INV_SLOT_START, USE_ROW_SLOT_END, false)) {
                return ItemStack.EMPTY;
            }

            if (stack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (stack.getCount() == result.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, stack);
        }

        return result;
    }

    public int getFuel() {
        return this.centrifugeData.get(CentrifugeBlockEntity.DATA_FUEL_USES);
    }

    public int getProcessTicks() {
        return this.centrifugeData.get(CentrifugeBlockEntity.DATA_PROCESS_TIME);
    }

    public int getProcessTime() {
        return com.puggicorn.potionsreborn.Config.CENTRIFUGE_PROCESS_TIME.getAsInt();
    }

    public int getFuelUses() {
        return com.puggicorn.potionsreborn.Config.CENTRIFUGE_FUEL_USES.getAsInt();
    }

    /** Input potion slot (center top) — must carry effects. */
    static class InputSlot extends Slot {
        public InputSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return mayPlaceItem(stack);
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }

        public static boolean mayPlaceItem(ItemStack stack) {
            return stack.getItem() instanceof PotionItem
                && stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).hasEffects();
        }
    }

    /** Output bottle slots (triangle below) — water bottles or awkward potions only. */
    static class BottleSlot extends Slot {
        public BottleSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return mayPlaceItem(stack);
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }

        public static boolean mayPlaceItem(ItemStack stack) {
            if (!(stack.getItem() instanceof PotionItem) || !stack.has(DataComponents.POTION_CONTENTS)) {
                return false;
            }
            PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
            if (contents == null || contents.potion().isEmpty()) {
                return false;
            }
            net.minecraft.core.Holder<net.minecraft.world.item.alchemy.Potion> potion = contents.potion().get();
            return potion.is(Potions.WATER.unwrapKey().get()) || potion.is(Potions.AWKWARD.unwrapKey().get());
        }
    }

    static class FuelSlot extends Slot {
        public FuelSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return mayPlaceItem(stack);
        }

        public static boolean mayPlaceItem(ItemStack stack) {
            return stack.is(ModItems.BREEZE_POWDER.get());
        }
    }
}
