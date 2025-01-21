package com.example.addon.modules.spiralfly;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.io.Serializable;

class Save implements Serializable {

    int sideAmount, x, y, z, dirID, reps;

    Save(BlockPos goalPos, int sideAmount, Direction direction, int reps) {
        this.x = goalPos.getX();
        this.z = goalPos.getZ();
        this.y = goalPos.getY();
        this.reps = reps;
        this.sideAmount = sideAmount;
        this.dirID = direction.getId();
    }

    public int getReps() {
        return reps;
    }

    public Direction getDirection() {
        return Direction.byId(dirID);
    }

    public int getSideAmount() {
        return sideAmount;
    }

    public BlockPos getGoalPos() {
        return new BlockPos(x,y,z);
    }
}
