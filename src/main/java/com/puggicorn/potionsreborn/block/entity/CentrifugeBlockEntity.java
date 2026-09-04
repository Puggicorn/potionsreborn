package com.puggicorn.potionsreborn.block.entity;

import com.puggicorn.potionsreborn.Config;
import com.puggicorn.potionsreborn.item.ModItems;
import com.puggicorn.potionsreborn.menu.CentrifugeMenu;
import com.puggicorn.potionsreborn.potion.ModPotions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Block entity for the Centrifuge. Mirrors the brewing stand layout (input potion on top, three
 * bottles below, Breeze Powder fuel) but is started by a button rather than running automatically,
 * and separates a multi-effect potion into its individual effects.
 *
 * <p>Slots: 0-2 output bottles, 3 input potion, 4 fuel.
 */
public class CentrifugeBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer {
    public static final int BOTTLE_SLOT_START = 0;
    public static final int BOTTLE_SLOT_END = 2;
    public static final int INPUT_SLOT = 3;
    public static final int FUEL_SLOT = 4;
    public static final int SLOT_COUNT = 5;
    private static final int[] SLOTS_FOR_UP = new int[]{INPUT_SLOT};
    private static final int[] SLOTS_FOR_DOWN = new int[]{BOTTLE_SLOT_START, BOTTLE_SLOT_START + 1, BOTTLE_SLOT_END, INPUT_SLOT};
    private static final int[] SLOTS_FOR_SIDES = new int[]{BOTTLE_SLOT_START, BOTTLE_SLOT_START + 1, BOTTLE_SLOT_END, FUEL_SLOT};
    public static final int DATA_PROCESS_TIME = 0;
    public static final int DATA_FUEL_USES = 1;
    public static final int DATA_RUNNING = 2;
    public static final int NUM_DATA_VALUES = 3;

    private NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    int processTime;
    int fuel;
    boolean running;
    protected final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case DATA_PROCESS_TIME -> CentrifugeBlockEntity.this.processTime;
                case DATA_FUEL_USES -> CentrifugeBlockEntity.this.fuel;
                case DATA_RUNNING -> CentrifugeBlockEntity.this.running ? 1 : 0;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case DATA_PROCESS_TIME -> CentrifugeBlockEntity.this.processTime = value;
                case DATA_FUEL_USES -> CentrifugeBlockEntity.this.fuel = value;
                case DATA_RUNNING -> CentrifugeBlockEntity.this.running = value != 0;
            }
        }

        @Override
        public int getCount() {
            return NUM_DATA_VALUES;
        }
    };

    public CentrifugeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CENTRIFUGE.get(), pos, state);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.potionsreborn.centrifuge");
    }

    @Override
    public int getContainerSize() {
        return this.items.size();
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    /** Called when the player presses the start button. */
    public void setRunning(boolean running) {
        this.running = running;
        if (!running) {
            this.processTime = 0;
        }
        this.setChanged();
    }

    public boolean isRunning() {
        return this.running;
    }

    /** Current remaining fuel, for the menu's start-button check. */
    public int getFuel() {
        return this.fuel;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CentrifugeBlockEntity centrifuge) {
        ItemStack fuelStack = centrifuge.items.get(FUEL_SLOT);
        if (centrifuge.fuel <= 0 && fuelStack.is(ModItems.BREEZE_POWDER.get())) {
            centrifuge.fuel = Config.CENTRIFUGE_FUEL_USES.getAsInt();
            fuelStack.shrink(1);
            setChanged(level, pos, state);
        }

        if (centrifuge.running && centrifuge.fuel > 0 && centrifuge.canProcess()) {
            centrifuge.processTime++;
            if (centrifuge.processTime >= Config.CENTRIFUGE_PROCESS_TIME.getAsInt()) {
                centrifuge.processTime = 0;
                centrifuge.fuel--;
                centrifuge.separate(level, pos);
                centrifuge.running = false; // stop automatically once extraction finishes
            }
            setChanged(level, pos, state);
        } else if (centrifuge.processTime > 0) {
            centrifuge.processTime = 0;
            setChanged(level, pos, state);
        }
    }

    /**
     * @return true if the input potion has effects to separate and at least one valid output bottle
     * is present.
     */
    public boolean canProcess() {
        ItemStack input = this.items.get(INPUT_SLOT);
        if (!isValidInput(input)) {
            return false;
        }
        if (!input.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).hasEffects()) {
            return false;
        }

        for (int i = BOTTLE_SLOT_START; i <= BOTTLE_SLOT_END; i++) {
            if (isValidBottle(this.items.get(i))) {
                return true;
            }
        }
        return false;
    }

    /** The input must be a potion that actually carries effects. */
    private static boolean isValidInput(ItemStack stack) {
        return !stack.isEmpty()
            && stack.getItem() instanceof net.minecraft.world.item.PotionItem
            && stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).hasEffects();
    }

    /** Output bottles accept only water bottles or awkward potions. */
    private static boolean isValidBottle(ItemStack stack) {
        if (stack.isEmpty() || !stack.has(DataComponents.POTION_CONTENTS)) {
            return false;
        }
        PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
        if (contents == null || contents.potion().isEmpty()) {
            return false;
        }
        Holder<Potion> potion = contents.potion().get();
        return potion.is(Potions.WATER.unwrapKey().get()) || potion.is(Potions.AWKWARD.unwrapKey().get());
    }

    /**
     * Distributes the input potion's effects over the output bottles — one effect per bottle when
     * possible, otherwise leftovers land on random remaining bottles. The emptied input leaves a
     * glass bottle behind.
     */
    private void separate(Level level, BlockPos pos) {
        ItemStack input = this.items.get(INPUT_SLOT);
        PotionContents contents = input.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        List<MobEffectInstance> effects = new ArrayList<>();
        contents.forEachEffect(effects::add);
        if (effects.isEmpty()) {
            return;
        }

        List<Integer> bottles = new ArrayList<>();
        for (int i = BOTTLE_SLOT_START; i <= BOTTLE_SLOT_END; i++) {
            if (isValidBottle(this.items.get(i))) {
                bottles.add(i);
            }
        }
        if (bottles.isEmpty()) {
            return;
        }

        // Shuffle so which effects end up alone vs merged is random.
        for (int i = effects.size() - 1; i > 0; i--) {
            Collections.swap(effects, i, level.random.nextInt(i + 1));
        }

        Map<Integer, List<MobEffectInstance>> assignment = new HashMap<>();
        for (int i = 0; i < effects.size(); i++) {
            int slot = i < bottles.size() ? bottles.get(i) : bottles.get(level.random.nextInt(bottles.size()));
            assignment.computeIfAbsent(slot, key -> new ArrayList<>()).add(effects.get(i));
        }

        net.minecraft.core.Registry<Potion> potionRegistry = level.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.POTION);
        for (Map.Entry<Integer, List<MobEffectInstance>> entry : assignment.entrySet()) {
            List<MobEffectInstance> assigned = entry.getValue();
            ItemStack result = this.items.get(entry.getKey()).copy();
            // Only when exactly one effect was separated do we try to give it a named vanilla base.
            Optional<Holder<Potion>> vanillaBase = assigned.size() == 1
                ? ModPotions.basePotionFor(potionRegistry, assigned.get(0).getEffect())
                : Optional.empty();
            if (vanillaBase.isPresent()) {
                // Vanilla base potion provides both the name ("Potion of <Name>") and the effect.
                result.set(DataComponents.POTION_CONTENTS, new PotionContents(vanillaBase, Optional.empty(), List.of()));
            } else {
                // Modded/unmatched effect (or a merged set): keep the extracted effect(s) as custom
                // effects on the generic Extracted base so nothing is lost.
                result.set(DataComponents.POTION_CONTENTS, new PotionContents(
                    Optional.<Holder<Potion>>of(ModPotions.EXTRACTED), Optional.empty(), List.copyOf(assigned)));
            }
            this.items.set(entry.getKey(), result);
        }

        input.shrink(1);
        this.items.set(INPUT_SLOT, input.isEmpty() ? new ItemStack(Items.GLASS_BOTTLE) : input);
        level.levelEvent(1035, pos, 0);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, this.items, registries);
        this.processTime = tag.getShort("ProcessTime");
        this.fuel = tag.getByte("Fuel");
        this.running = tag.getBoolean("Running");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putShort("ProcessTime", (short)this.processTime);
        ContainerHelper.saveAllItems(tag, this.items, registries);
        tag.putByte("Fuel", (byte)this.fuel);
        tag.putBoolean("Running", this.running);
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        if (index == INPUT_SLOT) {
            return isValidInput(stack);
        } else if (index == FUEL_SLOT) {
            return stack.is(ModItems.BREEZE_POWDER.get());
        } else {
            return isValidBottle(stack) && this.getItem(index).isEmpty();
        }
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        if (side == Direction.UP) {
            return SLOTS_FOR_UP;
        } else {
            return side == Direction.DOWN ? SLOTS_FOR_DOWN : SLOTS_FOR_SIDES;
        }
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, @javax.annotation.Nullable Direction direction) {
        return this.canPlaceItem(index, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return index == INPUT_SLOT ? stack.is(Items.GLASS_BOTTLE) : true;
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new CentrifugeMenu(containerId, inventory, this, this.dataAccess);
    }
}
