package com.example.addon.modules.utils;

import net.minecraft.util.math.BlockPos;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class ElytraUtils {
    public static void standardFlight(BlockPos pos, double regSpeed, double verticalSpeed) {
        double x = 0.0, y = 0.0, z = 0.0;
        double xDiff = (pos.getX() + 0.5) - mc.player.getX();
        double yDiff = (pos.getY() + 0.4) - mc.player.getY();
        double zDiff = (pos.getZ() + 0.5) - mc.player.getZ();
        boolean movingOnX = true;
        boolean movingOnZ = true;

        if ((int) xDiff > 0.0) {
            x = regSpeed;
        } else if ((int) xDiff < 0.0) {
            x = -regSpeed;
        } else {
            movingOnX = false;
        }

        if ((int) yDiff > 0.0) {
            y = verticalSpeed;
        } else if ((int) yDiff < 0.0) {
            y = -verticalSpeed;
        }

        if ((int) zDiff > 0.0) {
            z = regSpeed;
        } else if ((int) zDiff < 0.0) {
            z = -regSpeed;
        } else {
            movingOnZ = false;
        }

        if (movingOnX && movingOnZ) {
            double DIAGONAL = 1 / Math.sqrt(2);
            x *= DIAGONAL;
            z *= DIAGONAL;
        }

        mc.player.setVelocity(x, y, z);

        double centerSpeed = 0.2;
        double centerCheck = 0.1;

        mc.player.setVelocity((x == 0 ? xDiff > centerCheck ? centerSpeed : xDiff < -centerCheck ? -centerSpeed : 0 : mc.player.getVelocity().x), (y == 0 ? yDiff > centerCheck ? centerSpeed : yDiff < -centerCheck ? -centerSpeed : 0 : mc.player.getVelocity().y), (z == 0 ? zDiff > centerCheck ? centerSpeed : zDiff < -centerCheck ? -centerSpeed : 0 : mc.player.getVelocity().z));
    }
}
