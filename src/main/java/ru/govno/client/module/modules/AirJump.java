package ru.govno.client.module.modules;

import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import ru.govno.client.module.Module;
import ru.govno.client.module.modules.Speed;
import ru.govno.client.module.settings.ModeSettings;
import ru.govno.client.utils.Math.MathUtils;
import ru.govno.client.utils.Math.TimerHelper;
import ru.govno.client.utils.Movement.MoveMeHelp;
import ru.govno.client.utils.RandomUtils;

public class AirJump
extends Module {
    public static AirJump get;
    ModeSettings Mode;
    final TimerHelper tick = new TimerHelper();

    public AirJump() {
        super("AirJump", 0, Module.Category.MOVEMENT);
        this.Mode = new ModeSettings("Mode", "Matrix", this, new String[]{"Matrix", "Matrix2", "Default"});
        this.settings.add(this.Mode);
        get = this;
    }

    @Override
    public String getDisplayName() {
        return this.getDisplayByMode(this.Mode.currentMode);
    }

    @Override
    public void onUpdate() {
        if (this.Mode.currentMode.equalsIgnoreCase("Matrix")) {
            float ex = 1.0f;
            float ex2 = 1.0f;
            if (AirJump.mc.world.getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY - (double)ex, Minecraft.player.posZ)).getBlock() == Blocks.PURPUR_SLAB || AirJump.mc.world.getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY - (double)ex, Minecraft.player.posZ)).getBlock() == Blocks.STONE_SLAB || AirJump.mc.world.getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY - (double)ex, Minecraft.player.posZ)).getBlock() == Blocks.WOODEN_SLAB) {
                ex += 0.6f;
            }
            Minecraft.player.jumpTicks = 0;
            if ((Speed.posBlock(Minecraft.player.posX, Minecraft.player.posY - (double)ex, Minecraft.player.posZ) || (Speed.posBlock(Minecraft.player.posX - (double)ex2, Minecraft.player.posY - (double)ex, Minecraft.player.posZ - (double)ex2) || Speed.posBlock(Minecraft.player.posX + (double)ex2, Minecraft.player.posY - (double)ex, Minecraft.player.posZ + (double)ex2) || Speed.posBlock(Minecraft.player.posX - (double)ex2, Minecraft.player.posY - (double)ex, Minecraft.player.posZ + (double)ex2) || Speed.posBlock(Minecraft.player.posX + (double)ex2, Minecraft.player.posY - (double)ex, Minecraft.player.posZ - (double)ex2) || Speed.posBlock(Minecraft.player.posX - (double)ex2, Minecraft.player.posY - (double)ex, Minecraft.player.posZ) || Speed.posBlock(Minecraft.player.posX + (double)ex2, Minecraft.player.posY - (double)ex, Minecraft.player.posZ) || Speed.posBlock(Minecraft.player.posX, Minecraft.player.posY - (double)ex, Minecraft.player.posZ - (double)ex2) || Speed.posBlock(Minecraft.player.posX, Minecraft.player.posY - (double)ex, Minecraft.player.posZ + (double)ex2)) && MoveMeHelp.isMoving()) && !Minecraft.player.isCollidedHorizontally && AirJump.mc.world.getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY - 0.5, Minecraft.player.posZ)).getBlock() != Blocks.WATER && AirJump.mc.world.getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY - 1.0, Minecraft.player.posZ)).getBlock() != Blocks.WATER && (!Minecraft.player.isCollidedVertically || Minecraft.player.ticksExisted % 2 == 0)) {
                Minecraft.player.motionY = 0.0;
                Minecraft.player.onGround = true;
                Minecraft.player.fallDistance = 0.0f;
                if (MoveMeHelp.getSpeed() < -0.05 && !MoveMeHelp.isMoving() && Minecraft.player.isJumping()) {
                    Minecraft.player.motionX -= Math.sin(Math.toRadians(Minecraft.player.rotationYaw)) * MathUtils.clamp(RandomUtils.getRandomDouble(-1.0, 1.0), -0.005, 0.005);
                    Minecraft.player.motionZ += Math.cos(Math.toRadians(Minecraft.player.rotationYaw)) * MathUtils.clamp(RandomUtils.getRandomDouble(-1.0, 1.0), -0.005, 0.005);
                }
                Minecraft.player.isCollidedVertically = Minecraft.player.onGround;
                Minecraft.player.motionY = 0.0;
                if (Minecraft.player.motionY >= 0.0) {
                    Minecraft.player.fallDistance = 0.0f;
                }
            }
            AxisAlignedBB B = Minecraft.player.boundingBox;
            if (Minecraft.player.isCollidedHorizontally && (Minecraft.player.motionY < -0.1 || Minecraft.player.ticksExisted % 2 == 0)) {
                Minecraft.player.onGround = true;
                Minecraft.player.jump();
                Minecraft.player.fallDistance = 0.0f;
            }
        } else if (this.Mode.currentMode.equalsIgnoreCase("Matrix2")) {
            if (Minecraft.player.isJumping() && Minecraft.player.motionY < 0.0) {
                float w = Minecraft.player.width / 2.0f - 0.001f;
                if (Speed.posBlock(Minecraft.player.posX, Minecraft.player.posY - 1.0, Minecraft.player.posZ) || Speed.posBlock(Minecraft.player.posX + (double)w, Minecraft.player.posY - 1.0, Minecraft.player.posZ + (double)w) || Speed.posBlock(Minecraft.player.posX - (double)w, Minecraft.player.posY - 1.0, Minecraft.player.posZ - (double)w) || Speed.posBlock(Minecraft.player.posX + (double)w, Minecraft.player.posY - 1.0, Minecraft.player.posZ - (double)w) || Speed.posBlock(Minecraft.player.posX - (double)w, Minecraft.player.posY - 1.0, Minecraft.player.posZ + (double)w) || Speed.posBlock(Minecraft.player.posX + (double)w, Minecraft.player.posY - 1.0, Minecraft.player.posZ) || Speed.posBlock(Minecraft.player.posX - (double)w, Minecraft.player.posY - 1.0, Minecraft.player.posZ + (double)w) || Speed.posBlock(Minecraft.player.posX, Minecraft.player.posY - 1.0, Minecraft.player.posZ - (double)w)) {
                    Minecraft.player.onGround = true;
                    Minecraft.player.fallDistance = 0.0f;
                }
            }
            if (Minecraft.player.isJumping() && Minecraft.player.isCollidedHorizontally && Minecraft.player.ticksExisted % 5 == 0) {
                Minecraft.player.onGround = true;
                Minecraft.player.fallDistance = 0.0f;
            }
        } else if (this.Mode.currentMode.equalsIgnoreCase("Default")) {
            Minecraft.player.onGround = true;
        }
    }
}

