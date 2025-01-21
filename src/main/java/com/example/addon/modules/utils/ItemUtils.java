package com.example.addon.modules.utils;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

import java.util.ArrayList;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class ItemUtils {
    /**
        public static boolean isHoldingUsable(boolean mainhandOnly) {
            if (isUsable(mc.player.getMainHandStack().getItem())) return true;
            return !mainhandOnly && isUsable(mc.player.getOffHandStack().getItem());
        }
     */

    public static boolean isUsable(Item item) {
        return item == Items.BOW
            || item == Items.CROSSBOW
            || item == Items.END_CRYSTAL
            || item == Items.ENDER_EYE
            || item == Items.ENDER_PEARL
            || item == Items.EXPERIENCE_BOTTLE
            || item == Items.FIRE_CHARGE
            || item == Items.LINGERING_POTION
            || item == Items.POTION
            || item == Items.SHIELD
            || item == Items.SNOWBALL
            || item == Items.SPYGLASS
            || item == Items.TRIDENT
            || item.getComponents().contains(DataComponentTypes.FOOD);
    }

    public static boolean isHoldingGapple() {
        Item mainItem = mc.player.getMainHandStack().getItem();
        if (mainItem == Items.GOLDEN_APPLE || mainItem == Items.ENCHANTED_GOLDEN_APPLE) return true;

        Item offItem = mc.player.getOffHandStack().getItem();
        return offItem == Items.GOLDEN_APPLE || offItem == Items.ENCHANTED_GOLDEN_APPLE;
    }

    public static int missingArmors(PlayerEntity player) {
        int missing = 0;
        for (int i = 0; i < 4; i++) {
            if (player.getInventory().getArmorStack(i).isEmpty()) missing++;
        }
        return missing;
    }

    /**
        public static boolean isHoldingWeapon(PlayerEntity player) {
            Item item = player.getMainHandStack().getItem();
            return item instanceof AxeItem || item instanceof SwordItem;
        }
     */

    public static ArrayList<Item> shulkers = new ArrayList<>(){{
        add(Items.SHULKER_BOX);
        add(Items.WHITE_SHULKER_BOX);
        add(Items.ORANGE_SHULKER_BOX);
        add(Items.MAGENTA_SHULKER_BOX);
        add(Items.LIGHT_BLUE_SHULKER_BOX);
        add(Items.YELLOW_SHULKER_BOX);
        add(Items.LIME_SHULKER_BOX);
        add(Items.PINK_SHULKER_BOX);
        add(Items.GRAY_SHULKER_BOX);
        add(Items.LIGHT_GRAY_SHULKER_BOX);
        add(Items.CYAN_SHULKER_BOX);
        add(Items.PURPLE_SHULKER_BOX);
        add(Items.BLUE_SHULKER_BOX);
        add(Items.BROWN_SHULKER_BOX);
        add(Items.GREEN_SHULKER_BOX);
        add(Items.RED_SHULKER_BOX);
        add(Items.BLACK_SHULKER_BOX);
    }};
}
