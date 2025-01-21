package com.example.addon.modules.spiralfly;

import com.example.addon.AddonTemplate;
import com.example.addon.modules.utils.ElytraUtils;
import com.example.addon.modules.utils.FileSaveUtils;
import com.example.addon.modules.utils.ItemUtils;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.movement.elytrafly.ElytraFly;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.meteorclient.utils.world.CardinalDirection;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ingame.ShulkerBoxScreen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class SpiralFly extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgSwap = settings.createGroup("Swap");
    private final SettingGroup sgManual = settings.createGroup("Manual Setup");

    public final Setting<Double> regularSpeed = sgGeneral.add(new DoubleSetting.Builder()
        .name("horizontal-speed")
        .description("How fast to fly forwards and backwards.")
        .defaultValue(2.418)
        .sliderRange(0, 5)
        .build()
    );

    public final Setting<Double> ySpeed = sgGeneral.add(new DoubleSetting.Builder()
        .name("vertical-speed")
        .description("How fast to fly upwards and downwards.")
        .defaultValue(1.0)
        .sliderRange(0, 5)
        .build()
    );

    /**
    public final Setting<Bypasses> bypasses = sgGeneral.add(new EnumSetting.Builder<Bypasses>()
        .name("bypasses")
        .description("Which server bypass to activate.")
        .defaultValue(Bypasses.None)
        .build()
    );
     **/

    public final Setting<Integer> yLevel = sgGeneral.add(new IntSetting.Builder()
        .name("y-level")
        .description("The height to fly at.")
        .defaultValue(250)
        .sliderRange(1, 350)
        .build()
    );

    private final Setting<Integer> renderDistance = sgGeneral.add(new IntSetting.Builder()
        .name("render-distance")
        .description("The render distance of the server (keep 6 on const, 10 on ecme).")
        .defaultValue(6)
        .sliderRange(2, 24)
        .build()
    );

    private final Setting<Boolean> sideLengthTrack = sgGeneral.add(new BoolSetting.Builder()
        .name("side-length-info")
        .description("Pastes the current side length in chat.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> disableElytraFly = sgGeneral.add(new BoolSetting.Builder()
        .name("toggle-elytra-fly")
        .description("Automatically disables ElytraFly.")
        .defaultValue(true)
        .build()
    );

    // Swap

    private final Setting<Boolean> elytraSwap = sgSwap.add(new BoolSetting.Builder()
        .name("elytra-swap")
        .description("The module will automatically swap elytras for you.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> swapDura = sgSwap.add(new IntSetting.Builder()
        .name("swap-durability")
        .description("The durability to swap the elytras at.")
        .range(1, Items.ELYTRA.getComponents().get(DataComponentTypes.MAX_DAMAGE) - 1)
        .sliderRange(1, Items.ELYTRA.getComponents().get(DataComponentTypes.MAX_DAMAGE) - 1)
        .visible(elytraSwap::get)
        .build()
    );

    private final Setting<Boolean> refill = sgSwap.add(new BoolSetting.Builder()
        .name("refill")
        .description("Places a shulker from inventory and takes elytra from it.")
        .defaultValue(true)
        .visible(elytraSwap::get)
        .build()
    );

    private final Setting<Boolean> rotate = sgSwap.add(new BoolSetting.Builder()
        .name("rotate")
        .description("Rotates when placing the shulker.")
        .defaultValue(true)
        .visible(elytraSwap::get)
        .build()
    );

    // Manual

    private final Setting<Boolean> manual = sgManual.add(new BoolSetting.Builder()
        .name("manual")
        .description("Manually set the side length and start direction (usually to continue where you left off).")
        .defaultValue(false)
        .build()
    );

    private final Setting<Integer> manualSideAmount = sgManual.add(new IntSetting.Builder()
        .name("side-length")
        .description("How many render distances you will travel forward before beginning to spiral.")
        .defaultValue(5)
        .sliderRange(1, 100)
        .visible(manual::get)
        .build()
    );

    private final Setting<CardinalDirection> manualDirection = sgManual.add(new EnumSetting.Builder<CardinalDirection>()
        .name("direction")
        .description("The initial starting direction it will start flying in.")
        .defaultValue(CardinalDirection.North)
        .visible(manual::get)
        .build()
    );

    private BlockPos goal, shulkerPos;
    private Direction currentDir;
    private int sideAmount, reps;
    private boolean offsetCheck, started, refilling, placedShulker;

    Modules modules = Modules.get();

    public SpiralFly() {
        super(AddonTemplate.CATEGORY, "spiral-fly", "Searches an area in a spiral pattern.");
    }

    @Override
    public void onActivate() {
        if (mc.player.getEquippedStack(EquipmentSlot.CHEST).getItem() != Items.ELYTRA) {
            info("No elytra equipped.");
            toggle();
        }

        if (!started) {
            reset();
            started = true;
        }

        if (modules.isActive(ElytraFly.class) && disableElytraFly.get()) {
            modules.get(ElytraFly.class).toggle();
        }
    }

    @EventHandler
    public void onTick(TickEvent.Pre event) {
        BlockPos currentPlayerPos = mc.player.getBlockPos();
        BlockPos yLockedPlayerPos = new BlockPos(mc.player.getBlockX(), yLevel.get(), mc.player.getBlockZ());

        //if (bypasses.get() == Bypasses.ECME) swingHand();

        // If not elytra flying attempts to open elytra
        if (!mc.player.isOnGround()) {
            mc.player.jump();
            mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
        }

        // Todo: work on the refiller - Yeetus
        if (refilling && !placedShulker) {
            shulkerPos = currentPlayerPos.offset(Direction.DOWN, 2);
            BlockUtils.place(shulkerPos, findShulkerBox(), rotate.get(), 0, true, false);
            placedShulker = true;
            return;
        } else if (placedShulker && refilling) {
            openShulker(shulkerPos);
            if (mc.currentScreen instanceof ShulkerBoxScreen) {
                grabAllElytras();
                mc.currentScreen.close();
            }
            placedShulker = false;
            refilling = false;
            return;
        }

        // If elytra durability is below the threshold it swaps
        ItemStack elytraStack = mc.player.getInventory().getArmorStack(2);

        if (elytraStack.getMaxDamage() - elytraStack.getDamage() <= swapDura.get() && elytraSwap.get()) {
            FindItemResult elytra = InvUtils.find(itemStack -> itemStack.getItem() == Items.ELYTRA && itemStack.getDamage() < itemStack.getMaxDamage() - swapDura.get());
            info("Swapped elytra.");
            if (!elytra.found() && refill.get()) {
                refilling = true;
            }
            InvUtils.move().from(elytra.slot()).toArmor(2);
            InvUtils.drop().from(elytra.slot());
        }

        // If the player is within 3 blocks of the goal it rotates the bot
        if (currentPlayerPos.toCenterPos().distanceTo(goal.toCenterPos()) <= 3) {
            currentDir = currentDir.rotateYClockwise();

            matchForwardRenderDistance(sideAmount, currentDir, yLockedPlayerPos);
            offsetCheck = false;
            reps++;
            if (sideLengthTrack.get()) {
                info("Current side length: " + sideAmount + ".");
            }
        }

        // Creates the spiral motion by adding a render distance every other line (offsetCheck boolean is just there to keep it from spamming cuz I'm a monkey)
        if (reps % 2 == 0 && !offsetCheck) {
            sideAmount++;
            offsetCheck = true;
        }

        if (mc.player.getBlockY() == goal.getY()) {
            ElytraUtils.standardFlight(goal, regularSpeed.get(), ySpeed.get());
        } else {
            ElytraUtils.standardFlight(yLockedPlayerPos, regularSpeed.get(), ySpeed.get());
        }

        // Forces the player to look in the direction the bot is flying in order for the anti cheat to not monkey
        mc.player.setYaw((float) Rotations.getYaw(goal));
    }

    @Override
    public WWidget getWidget(GuiTheme theme) {
        WVerticalList list = theme.verticalList();
        WHorizontalList b = list.add(theme.horizontalList()).expandX().widget();
        WButton reset = b.add(theme.button("Reset")).expandX().widget();
        reset.action = () -> {
            started = false;
            refilling = false;
            placedShulker = false;
        };

        WButton load = b.add(theme.button("Load")).expandX().widget();
        load.action = () -> {
            if (currentDir == null || goal == null) {
                warning("Error saving.");
                return;
            }

            try {
                Save saveData = (Save) FileSaveUtils.load("spiralFlySave");
                sideAmount = saveData.getSideAmount();
                currentDir = saveData.getDirection();
                goal = saveData.getGoalPos();
                reps = saveData.getReps();
                info("Data loaded!" +
                    "\nGoal: " + goal.toShortString() +
                    "\nSide amount: " + sideAmount +
                    "\nDirection: " + currentDir.toString() +
                    "\nReps: " + reps);
            } catch (Exception e) {
                info(e.toString());
            }
        };

        WButton save = b.add(theme.button("Save")).expandX().widget();
        save.action = () -> {
            try {
                Save saveData = new Save(goal, sideAmount, currentDir, reps);
                FileSaveUtils.saveToFile(saveData, "spiralFlySave");
                info("Data saved!" +
                    "\nGoal: " + goal.toShortString() +
                    "\nSide amount: " + sideAmount +
                    "\nDirection: " + currentDir.toString() +
                    "\nReps: " + reps);
            } catch (Exception e) {
                info(e.toString());
            }
        };
        return list;
    }

/* *
    @EventHandler
    private void onScreenOpen(OpenScreenEvent event) {if (log.get() && event.screen instanceof DisconnectedScreen) toggle();}
    @EventHandler
    private void onGameLeft(GameLeftEvent event) {if (log.get()) toggle();}
* */

    private void reset() {
        if (manual.get()) {
            sideAmount = manualSideAmount.get();
            currentDir = manualDirection.get().toDirection();
        } else {
            sideAmount = 1;
            currentDir = mc.player.getHorizontalFacing();
        }
        BlockPos yLockedPos = new BlockPos(mc.player.getBlockX(), yLevel.get(), mc.player.getBlockZ());
        matchForwardRenderDistance(sideAmount, currentDir, yLockedPos);
        reps = 0;
    }

    // Gets render distance of player and multiplies by 16 to get that in blocks and then multiplies by the amount of render distances it should move forward
    public void matchForwardRenderDistance(int amount, Direction currentDir, BlockPos playerPos) {
        int blocks = (renderDistance.get() * 16) * amount;
        goal = playerPos.offset(currentDir, blocks);
    }

    public FindItemResult findShulkerBox() {
        return InvUtils.find(itemStack -> ItemUtils.shulkers.contains(itemStack.getItem()));
    }

    private void openShulker(BlockPos shulkerPos) {
        Vec3d shulkerVec = new Vec3d(shulkerPos.getX(), shulkerPos.getY(), shulkerPos.getZ());
        BlockHitResult table = new BlockHitResult(shulkerVec, Direction.UP, shulkerPos, false);
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, table);
    }

    private void grabAllElytras() {
        for (int i = 0; i < mc.player.currentScreenHandler.slots.size() - 36; i++) {
            Item item = mc.player.currentScreenHandler.getSlot(i).getStack().getItem();
            if (item.equals(Items.ELYTRA)) {
                InvUtils.quickSwap().from(i);
            }
        }
    }

    /**
     public enum Bypasses {
        CONST,
        ECME,
        None
     }
     **/
}
