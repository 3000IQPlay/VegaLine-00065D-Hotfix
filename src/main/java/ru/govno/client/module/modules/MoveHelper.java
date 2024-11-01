package ru.govno.client.module.modules;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemFood;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.potion.Potion;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.input.Keyboard;
import ru.govno.client.event.EventTarget;
import ru.govno.client.event.events.EventSlowLay;
import ru.govno.client.event.events.EventSlowSneak;
import ru.govno.client.event.events.EventSprintBlock;
import ru.govno.client.module.Module;
import ru.govno.client.module.modules.OnFalling;
import ru.govno.client.module.modules.Speed;
import ru.govno.client.module.modules.Timer;
import ru.govno.client.module.modules.Velocity;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.module.settings.FloatSettings;
import ru.govno.client.module.settings.ModeSettings;
import ru.govno.client.utils.BlockHelper;
import ru.govno.client.utils.Math.MathUtils;
import ru.govno.client.utils.Math.TimerHelper;
import ru.govno.client.utils.Movement.MoveMeHelp;

public class MoveHelper
extends Module {
    public static MoveHelper instance;
    public BoolSettings NoJumpDelay;
    public BoolSettings FastLadders;
    public BoolSettings GmFlyStrafe;
    public BoolSettings MatrixSnowFix;
    public BoolSettings StairSpeed;
    public BoolSettings NoSlowDown;
    public BoolSettings NoJumpSlowGrim;
    public BoolSettings NCPFapBypass;
    public BoolSettings InWebMotion;
    public BoolSettings WebZoom;
    public BoolSettings AnchoorHole;
    public BoolSettings Step;
    public BoolSettings ReverseStep;
    public BoolSettings NoSlowSneak;
    public BoolSettings NoSlowLay;
    public BoolSettings TrapdoorSpeed;
    public BoolSettings GroundHalt;
    public BoolSettings LevitateControl;
    public BoolSettings NoSlowSoul;
    public BoolSettings FastPilingUp;
    public BoolSettings IgnoreHunger;
    public FloatSettings WebSpeedXZ;
    public FloatSettings WebSpeedYPlus;
    public FloatSettings WebSpeedYMinus;
    public ModeSettings FlyStrafeMode;
    public ModeSettings NoSlowMode;
    public ModeSettings WebMotionMode;
    public ModeSettings StepUpMode;
    public ModeSettings SneakSlowBypass;
    public ModeSettings LaySlowBypass;
    private final TimerHelper notRotTime = TimerHelper.TimerHelperReseted();
    private boolean canPress;
    public static boolean stairTick;
    private static int tickCounter;
    public static boolean holeTick;
    public static int holeTicks;

    public MoveHelper() {
        super("MoveHelper", 0, Module.Category.MOVEMENT);
        instance = this;
        this.NoJumpDelay = new BoolSettings("NoJumpDelay", true, this);
        this.settings.add(this.NoJumpDelay);
        this.FastLadders = new BoolSettings("FastLadders", true, this);
        this.settings.add(this.FastLadders);
        this.GmFlyStrafe = new BoolSettings("GmFlyStrafe", true, this);
        this.settings.add(this.GmFlyStrafe);
        this.FlyStrafeMode = new ModeSettings("FlyStrafeMode", "Matrix", this, new String[]{"Matrix", "NCP", "Matrix&AAC", "Matrix&NCP"}, () -> this.GmFlyStrafe.getBool());
        this.settings.add(this.FlyStrafeMode);
        this.MatrixSnowFix = new BoolSettings("MatrixSnowFix", false, this);
        this.settings.add(this.MatrixSnowFix);
        this.StairSpeed = new BoolSettings("StairSpeed", true, this);
        this.settings.add(this.StairSpeed);
        this.NoSlowDown = new BoolSettings("NoSlowDown", true, this);
        this.settings.add(this.NoSlowDown);
        this.NoSlowMode = new ModeSettings("NoSlowMode", "MatrixLatest", this, new String[]{"Vanilla", "MatrixOld", "MatrixLatest", "AACOld", "NCP+", "Grim", "Intave"}, () -> this.NoSlowDown.getBool());
        this.settings.add(this.NoSlowMode);
        this.NoJumpSlowGrim = new BoolSettings("NoJumpSlowGrim", true, this, () -> this.NoSlowDown.getBool() && (this.NoSlowMode.currentMode.equalsIgnoreCase("Grim") || this.NoSlowMode.currentMode.equalsIgnoreCase("Intave")));
        this.settings.add(this.NoJumpSlowGrim);
        this.NCPFapBypass = new BoolSettings("NCP+FapBypass", true, this, () -> this.NoSlowDown.getBool() && this.NoSlowMode.currentMode.equalsIgnoreCase("NCP+"));
        this.settings.add(this.NCPFapBypass);
        this.InWebMotion = new BoolSettings("InWebMotion", true, this);
        this.settings.add(this.InWebMotion);
        this.WebMotionMode = new ModeSettings("WebMotionMode", "Matrix", this, new String[]{"Custom", "Matrix", "NoCollide"}, () -> this.InWebMotion.getBool());
        this.settings.add(this.WebMotionMode);
        this.WebZoom = new BoolSettings("WebZoom", false, this, () -> this.InWebMotion.getBool() && this.WebMotionMode.currentMode.equalsIgnoreCase("Matrix"));
        this.settings.add(this.WebZoom);
        this.WebSpeedXZ = new FloatSettings("WebSpeedXZ", 0.5f, 1.0f, 0.1f, this, () -> this.InWebMotion.getBool() && this.WebMotionMode.currentMode.equalsIgnoreCase("Custom"));
        this.settings.add(this.WebSpeedXZ);
        this.WebSpeedYPlus = new FloatSettings("WebSpeedY+", 0.5f, 1.0f, 0.0f, this, () -> this.InWebMotion.getBool() && this.WebMotionMode.currentMode.equalsIgnoreCase("Custom"));
        this.settings.add(this.WebSpeedYPlus);
        this.WebSpeedYMinus = new FloatSettings("WebSpeedY-", 0.4f, 1.0f, 0.0f, this, () -> this.InWebMotion.getBool() && this.WebMotionMode.currentMode.equalsIgnoreCase("Custom"));
        this.settings.add(this.WebSpeedYMinus);
        this.AnchoorHole = new BoolSettings("AnchoorHole", true, this);
        this.settings.add(this.AnchoorHole);
        this.Step = new BoolSettings("Step", false, this);
        this.settings.add(this.Step);
        this.StepUpMode = new ModeSettings("StepUpMode", "Vanilla", this, new String[]{"Vanilla", "Matrix"}, () -> this.Step.getBool());
        this.settings.add(this.StepUpMode);
        this.ReverseStep = new BoolSettings("Reverse", false, this, () -> this.Step.getBool());
        this.settings.add(this.ReverseStep);
        this.NoSlowSneak = new BoolSettings("NoSlowSneak", false, this);
        this.settings.add(this.NoSlowSneak);
        this.SneakSlowBypass = new ModeSettings("SneakSlowBypass", "Vanilla", this, new String[]{"Vanilla", "Matrix", "NCP", "Grim"}, () -> this.NoSlowSneak.getBool());
        this.settings.add(this.SneakSlowBypass);
        this.NoSlowLay = new BoolSettings("NoSlowLay", false, this, () -> Minecraft.player.hasNewVersionMoves);
        this.settings.add(this.NoSlowLay);
        this.LaySlowBypass = new ModeSettings("LaySlowBypass", "Matrix", this, new String[]{"Vanilla", "Matrix", "NCP", "Grim"}, () -> this.NoSlowLay.getBool() && Minecraft.player.hasNewVersionMoves);
        this.settings.add(this.LaySlowBypass);
        this.TrapdoorSpeed = new BoolSettings("TrapdoorSpeed", false, this);
        this.settings.add(this.TrapdoorSpeed);
        this.GroundHalt = new BoolSettings("GroundHalt", false, this);
        this.settings.add(this.GroundHalt);
        this.LevitateControl = new BoolSettings("LevitateControl", false, this);
        this.settings.add(this.LevitateControl);
        this.NoSlowSoul = new BoolSettings("NoSlowSoul", false, this);
        this.settings.add(this.NoSlowSoul);
        this.FastPilingUp = new BoolSettings("FastPilingUp", false, this);
        this.settings.add(this.FastPilingUp);
        this.IgnoreHunger = new BoolSettings("IgnoreHunger", false, this);
        this.settings.add(this.IgnoreHunger);
    }

    @EventTarget
    public void onSlowSneak(EventSlowSneak event) {
        if (this.NoSlowSneak.getBool()) {
            if (!Minecraft.player.isSneaking()) {
                return;
            }
            if (Velocity.get.isActived() && Velocity.get.OnKnockBack.getBool() && !Velocity.pass && Velocity.get.KnockType.currentMode.equalsIgnoreCase("Sneaking") && Velocity.get.sneakTicks > 0) {
                return;
            }
            switch (this.SneakSlowBypass.currentMode) {
                case "Vanilla": {
                    event.cancel();
                    break;
                }
                case "Matrix": {
                    event.cancel();
                    if (Minecraft.player.ticksExisted % 2 == 0 && Minecraft.player.onGround && !Minecraft.player.isJumping()) {
                        Minecraft.player.multiplyMotionXZ(Minecraft.player.moveStrafing == 0.0f ? 0.5f : 0.4f);
                        break;
                    }
                    if (!((double)Minecraft.player.fallDistance > 0.725 && (double)Minecraft.player.fallDistance <= 2.5) && !((double)Minecraft.player.fallDistance > 2.5)) break;
                    Minecraft.player.multiplyMotionXZ((double)Minecraft.player.fallDistance > 1.15 ? ((double)Minecraft.player.fallDistance > 1.4 ? 0.9375f : 0.9575f) : (Minecraft.player.moveStrafing == 0.0f ? 0.9725f : 0.9675f));
                    break;
                }
                case "NCP": {
                    if (Minecraft.player.isJumping()) {
                        event.setSlowFactor(0.82);
                        break;
                    }
                    if (!Minecraft.player.onGround) break;
                    event.setSlowFactor(Minecraft.player.moveStrafing == 0.0f ? 0.62 : 0.44);
                    break;
                }
                case "Grim": {
                    if (!(MoveMeHelp.getSpeed() < 0.2) || !Minecraft.player.onGround) break;
                    event.setSlowFactor(Minecraft.player.moveStrafing == 0.0f ? (double)0.799f : (double)0.65f);
                }
            }
        }
    }

    @EventTarget
    public void onSlowLay(EventSlowLay event) {
        if (this.NoSlowLay.getBool()) {
            switch (this.LaySlowBypass.currentMode) {
                case "Vanilla": {
                    event.cancel();
                    break;
                }
                case "Matrix": {
                    event.cancel();
                    if (Minecraft.player.ticksExisted % 2 == 0 && Minecraft.player.onGround && !Minecraft.player.isJumping()) {
                        Minecraft.player.multiplyMotionXZ(Minecraft.player.moveStrafing == 0.0f ? 0.5f : 0.4f);
                        break;
                    }
                    if (!((double)Minecraft.player.fallDistance > 0.725 && (double)Minecraft.player.fallDistance <= 2.5) && !((double)Minecraft.player.fallDistance > 2.5)) break;
                    Minecraft.player.multiplyMotionXZ((double)Minecraft.player.fallDistance > 1.15 ? ((double)Minecraft.player.fallDistance > 1.4 ? 0.9375f : 0.9575f) : (Minecraft.player.moveStrafing == 0.0f ? 0.9725f : 0.9675f));
                    break;
                }
                case "NCP": {
                    if (Minecraft.player.isJumping()) {
                        event.setSlowFactor(0.82);
                        break;
                    }
                    if (!Minecraft.player.onGround) break;
                    event.setSlowFactor(Minecraft.player.moveStrafing == 0.0f ? 0.62 : 0.44);
                    break;
                }
                case "Grim": {
                    if (!(MoveMeHelp.getSpeed() < 0.2) || !Minecraft.player.onGround) break;
                    event.setSlowFactor(Minecraft.player.moveStrafing == 0.0f ? (double)0.799f : (double)0.65f);
                }
            }
        }
    }

    public static boolean stopSlowingSoul(Entity entity) {
        boolean can = false;
        if (entity instanceof EntityPlayerSP) {
            EntityPlayerSP SP = (EntityPlayerSP)entity;
            if (MoveHelper.instance.actived && MoveHelper.instance.NoSlowSoul.getBool()) {
                SP.multiplyMotionXZ(SP.movementInput.jump ? 0.8f : 0.855f);
                can = true;
            }
        }
        return can;
    }

    @Override
    public void onUpdate() {
        if (this.TrapdoorSpeed.getBool() && MoveMeHelp.getSpeed() > 0.14 && !Minecraft.player.isSneaking() && MoveMeHelp.trapdoorAdobedEntity(Minecraft.player) && (!Minecraft.player.isJumping() || Minecraft.player.jumpTicks != 0)) {
            if (Minecraft.player.onGround) {
                Minecraft.player.jump();
            } else {
                Minecraft.player.posY -= 0.015;
            }
        }
        if (this.FastPilingUp.getBool()) {
            AxisAlignedBB B = Minecraft.player.boundingBox;
            if (!Minecraft.player.onGround && Minecraft.player.motionY == 0.08307781780646721 && !MoveHelper.mc.world.getCollisionBoxes(Minecraft.player, B.offsetMinDown(0.25)).isEmpty() && MoveHelper.mc.world.getCollisionBoxes(Minecraft.player, new AxisAlignedBB(B.minX, B.minY, B.minZ, B.maxX, B.minY + 1.0, B.maxZ)).isEmpty()) {
                Entity.motiony = -1.0;
            }
        }
        if (this.LevitateControl.getBool()) {
            boolean isLevitating = Minecraft.player.isPotionActive(Potion.getPotionById(25));
            double motionY = Minecraft.player.motionY;
            if (isLevitating) {
                motionY = Minecraft.player.isJumping() ? 0.8 - 0.08 * Math.random() : (Minecraft.player.isSneaking() ? 0.0 : motionY);
            }
            Minecraft.player.motionY = motionY;
        }
        if (this.GroundHalt.getBool() && Minecraft.player.onGround && Minecraft.player.isCollidedVertically && MoveMeHelp.getSpeed() < 0.15 && !MoveMeHelp.moveKeysPressed()) {
            Minecraft.player.multiplyMotionXZ(0.45f);
        }
        if (this.NoJumpDelay.getBool() && (MoveMeHelp.isBlockAboveHead() || (double)Minecraft.player.fallDistance < 0.25 || !MoveHelper.mc.world.getCollisionBoxes(Minecraft.player, new AxisAlignedBB(Minecraft.player.getPositionVector()).expand(0.3, 0.0, 0.3).offsetMinDown(0.25)).isEmpty())) {
            Minecraft.player.jumpTicks = 0;
        }
        if (this.GmFlyStrafe.getBool() && Minecraft.player.capabilities.isFlying) {
            float min = 0.23f;
            float max = 1.199f;
            float motY = (float)(Entity.Getmotiony + (double)(Minecraft.player.isJumping() ? 1 : (Minecraft.player.isSneaking() ? -1 : 0)));
            if (this.FlyStrafeMode.currentMode.equalsIgnoreCase("NCP")) {
                min = 0.6f;
                max = 1.152f;
                motY = (float)(Entity.Getmotiony + (Minecraft.player.isJumping() ? (Minecraft.player.isInWater() ? 0.61 : 0.7) : (Minecraft.player.isSneaking() ? -(Speed.posBlock(Minecraft.player.posX, Minecraft.player.posY - 3.0, Minecraft.player.posZ) ? 0.5 : 2.6) : 0.0)));
            }
            if (this.FlyStrafeMode.currentMode.equalsIgnoreCase("Matrix&AAC")) {
                if (Minecraft.player.rotationYaw - Minecraft.player.lastReportedYaw != 0.0f || Minecraft.player.rotationPitch - EntityPlayerSP.lastReportedPitch != 0.0f || !MoveMeHelp.isMoving() || Minecraft.player.isSneaking()) {
                    this.notRotTime.reset();
                }
                if (MoveHelper.mc.world != null) {
                    double offMotion = 8.0;
                    if (MoveHelper.mc.world.rayTraceBlocks(Minecraft.player.getPositionVector().addVector(-offMotion, 0.0, 0.0), Minecraft.player.getPositionVector().addVector(offMotion, 0.0, 0.0)) != null || MoveHelper.mc.world.rayTraceBlocks(Minecraft.player.getPositionVector().addVector(0.0, -offMotion, 0.0), Minecraft.player.getPositionVector().addVector(0.0, offMotion, 0.0)) != null || MoveHelper.mc.world.rayTraceBlocks(Minecraft.player.getPositionVector().addVector(0.0, 0.0, -offMotion), Minecraft.player.getPositionVector().addVector(0.0, 0.0, offMotion)) != null) {
                        this.notRotTime.reset();
                    }
                }
                MoveHelper.mc.timer.tempSpeed = 1.0;
                if (this.notRotTime.hasReached(50.0)) {
                    MoveHelper.mc.timer.tempSpeed = 1.0 + MathUtils.easeInOutQuadWave(((float)this.notRotTime.getTime() - 50.0f) / 500.0f) * 2.75;
                    if (this.notRotTime.hasReached(550.0)) {
                        this.notRotTime.reset();
                    }
                    Timer.get.setTempCancel();
                }
            } else if (this.FlyStrafeMode.getMode().equalsIgnoreCase("Matrix&NCP")) {
                int tickFly;
                if (!(Minecraft.player.rotationYaw - Minecraft.player.lastReportedYaw != 0.0f || Minecraft.player.rotationPitch - EntityPlayerSP.lastReportedPitch != 0.0f || MoveMeHelp.isMoving() || Minecraft.player.isSneaking() || Minecraft.player.isJumping())) {
                    this.notRotTime.reset();
                }
                if ((tickFly = (int)(this.notRotTime.getTime() / 50L)) > 10) {
                    MoveHelper.mc.timer.tempSpeed = MoveHelper.mc.timer.tempSpeed * (tickFly % 2 == 0 ? (double)1.075f : (double)1.15f);
                }
                min = 0.9f;
                float calcMotionY = (float)Math.max(Math.abs(Minecraft.player.motionY) * 3.0, (double)0.8f);
                float speedY = MathUtils.clamp(calcMotionY, -4.0f, 4.0f);
                motY = (float)(Entity.Getmotiony + (double)(Minecraft.player.isJumping() ? speedY : (Minecraft.player.isSneaking() ? -speedY : 0.0f)));
            } else {
                this.notRotTime.reset();
                MoveHelper.mc.timer.tempSpeed = 1.0;
            }
            double speed = MathUtils.clamp(MoveMeHelp.getSpeed() * 1.5, (double)min, (double)max);
            double speed2 = MathUtils.clamp(MoveMeHelp.getSpeed() * 1.5, (double)min, (double)max);
            MoveMeHelp.setSpeed(speed2, 0.8f);
            if (MoveMeHelp.isMoving()) {
                MoveMeHelp.setCuttingSpeed(speed / 1.06);
            }
            Minecraft.player.motionY = motY;
            Minecraft.player.motionY /= 2.0;
        }
        if (this.FastLadders.getBool()) {
            if (Minecraft.player.isOnLadder()) {
                MoveMeHelp.setCuttingSpeed((Minecraft.player.ticksExisted % 2 == 0 ? 0.2498 : 0.2499) / 1.06);
                Minecraft.player.motionY = 0.0;
                double d = Minecraft.player.isJumping() ? 0.12 : (Minecraft.player.isSneaking() ? -1.0 : (Entity.motiony = Minecraft.player.ticksExisted % 2 == 0 ? 0.0032 : -0.0032));
                if (MoveHelper.mc.timer.speed == 1.0) {
                    MoveHelper.mc.timer.speed = 1.04832343;
                }
            } else if (MoveHelper.mc.timer.speed == 1.04832343) {
                MoveHelper.mc.timer.speed = 1.0;
            }
        }
        if (this.MatrixSnowFix.getBool()) {
            if (BlockHelper.getBlock(new BlockPos(Minecraft.player.posX, Minecraft.player.posY, Minecraft.player.posZ)) == Blocks.SNOW_LAYER && BlockHelper.getBlock(new BlockPos(Minecraft.player.posX, Minecraft.player.posY - 1.0, Minecraft.player.posZ)) == Blocks.SOUL_SAND) {
                this.canPress = true;
                float ex = 1.0f;
                float ex2 = 1.0f;
                Minecraft.player.jumpTicks = 0;
                if (!(MoveHelper.mc.world.getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY - (double)ex, Minecraft.player.posZ)).getBlock() == Blocks.AIR && MoveHelper.mc.world.getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY - (double)ex, Minecraft.player.posZ)).getBlock() == Blocks.AIR && MoveHelper.mc.world.getBlockState(new BlockPos(Minecraft.player.posX - (double)ex2, Minecraft.player.posY - (double)ex, Minecraft.player.posZ - (double)ex2)).getBlock() == Blocks.AIR && MoveHelper.mc.world.getBlockState(new BlockPos(Minecraft.player.posX + (double)ex2, Minecraft.player.posY - (double)ex, Minecraft.player.posZ + (double)ex2)).getBlock() == Blocks.AIR && MoveHelper.mc.world.getBlockState(new BlockPos(Minecraft.player.posX - (double)ex2, Minecraft.player.posY - (double)ex, Minecraft.player.posZ + (double)ex2)).getBlock() == Blocks.AIR && MoveHelper.mc.world.getBlockState(new BlockPos(Minecraft.player.posX + (double)ex2, Minecraft.player.posY - (double)ex, Minecraft.player.posZ - (double)ex2)).getBlock() == Blocks.AIR && MoveHelper.mc.world.getBlockState(new BlockPos(Minecraft.player.posX + (double)ex2, Minecraft.player.posY - (double)ex, Minecraft.player.posZ)).getBlock() == Blocks.AIR && MoveHelper.mc.world.getBlockState(new BlockPos(Minecraft.player.posX - (double)ex2, Minecraft.player.posY - (double)ex, Minecraft.player.posZ)).getBlock() == Blocks.AIR && MoveHelper.mc.world.getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY - (double)ex, Minecraft.player.posZ + (double)ex2)).getBlock() == Blocks.AIR && MoveHelper.mc.world.getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY - (double)ex, Minecraft.player.posZ - (double)ex2)).getBlock() == Blocks.AIR || MoveHelper.mc.world.getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY - (double)ex, Minecraft.player.posZ)).getMaterial() == Material.SNOW || MoveHelper.mc.world.getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY - (double)ex, Minecraft.player.posZ)).getMaterial() == Material.SNOW || MoveHelper.mc.world.getBlockState(new BlockPos(Minecraft.player.posX - (double)ex2, Minecraft.player.posY - (double)ex, Minecraft.player.posZ - (double)ex2)).getMaterial() == Material.SNOW || MoveHelper.mc.world.getBlockState(new BlockPos(Minecraft.player.posX + (double)ex2, Minecraft.player.posY - (double)ex, Minecraft.player.posZ + (double)ex2)).getMaterial() == Material.SNOW || MoveHelper.mc.world.getBlockState(new BlockPos(Minecraft.player.posX - (double)ex2, Minecraft.player.posY - (double)ex, Minecraft.player.posZ + (double)ex2)).getMaterial() == Material.SNOW || MoveHelper.mc.world.getBlockState(new BlockPos(Minecraft.player.posX + (double)ex2, Minecraft.player.posY - (double)ex, Minecraft.player.posZ - (double)ex2)).getMaterial() == Material.SNOW || MoveHelper.mc.world.getBlockState(new BlockPos(Minecraft.player.posX + (double)ex2, Minecraft.player.posY - (double)ex, Minecraft.player.posZ)).getMaterial() == Material.SNOW || MoveHelper.mc.world.getBlockState(new BlockPos(Minecraft.player.posX - (double)ex2, Minecraft.player.posY - (double)ex, Minecraft.player.posZ)).getMaterial() == Material.SNOW || MoveHelper.mc.world.getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY - (double)ex, Minecraft.player.posZ + (double)ex2)).getMaterial() == Material.SNOW || MoveHelper.mc.world.getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY - (double)ex, Minecraft.player.posZ - (double)ex2)).getMaterial() == Material.SNOW || MoveHelper.mc.world.getBlockState((BlockPos)new BlockPos((double)Minecraft.player.posX, (double)(Minecraft.player.posY - (double)ex), (double)Minecraft.player.posZ)).getMaterial().getMaterialMapColor().colorIndex == 7 || MoveHelper.mc.world.getBlockState((BlockPos)new BlockPos((double)Minecraft.player.posX, (double)(Minecraft.player.posY - (double)ex), (double)Minecraft.player.posZ)).getMaterial().getMaterialMapColor().colorIndex == 7 || MoveHelper.mc.world.getBlockState((BlockPos)new BlockPos((double)(Minecraft.player.posX - (double)ex2), (double)(Minecraft.player.posY - (double)ex), (double)(Minecraft.player.posZ - (double)ex2))).getMaterial().getMaterialMapColor().colorIndex == 7 || MoveHelper.mc.world.getBlockState((BlockPos)new BlockPos((double)(Minecraft.player.posX + (double)ex2), (double)(Minecraft.player.posY - (double)ex), (double)(Minecraft.player.posZ + (double)ex2))).getMaterial().getMaterialMapColor().colorIndex == 7 || MoveHelper.mc.world.getBlockState((BlockPos)new BlockPos((double)(Minecraft.player.posX - (double)ex2), (double)(Minecraft.player.posY - (double)ex), (double)(Minecraft.player.posZ + (double)ex2))).getMaterial().getMaterialMapColor().colorIndex == 7 || MoveHelper.mc.world.getBlockState((BlockPos)new BlockPos((double)(Minecraft.player.posX + (double)ex2), (double)(Minecraft.player.posY - (double)ex), (double)(Minecraft.player.posZ - (double)ex2))).getMaterial().getMaterialMapColor().colorIndex == 7 || MoveHelper.mc.world.getBlockState((BlockPos)new BlockPos((double)(Minecraft.player.posX + (double)ex2), (double)(Minecraft.player.posY - (double)ex), (double)Minecraft.player.posZ)).getMaterial().getMaterialMapColor().colorIndex == 7 || MoveHelper.mc.world.getBlockState((BlockPos)new BlockPos((double)(Minecraft.player.posX - (double)ex2), (double)(Minecraft.player.posY - (double)ex), (double)Minecraft.player.posZ)).getMaterial().getMaterialMapColor().colorIndex == 7 || MoveHelper.mc.world.getBlockState((BlockPos)new BlockPos((double)Minecraft.player.posX, (double)(Minecraft.player.posY - (double)ex), (double)(Minecraft.player.posZ + (double)ex2))).getMaterial().getMaterialMapColor().colorIndex == 7 || MoveHelper.mc.world.getBlockState((BlockPos)new BlockPos((double)Minecraft.player.posX, (double)(Minecraft.player.posY - (double)ex), (double)(Minecraft.player.posZ - (double)ex2))).getMaterial().getMaterialMapColor().colorIndex == 7 || MoveMeHelp.isBlockAboveHead() || Minecraft.player.isCollidedHorizontally)) {
                    Minecraft.player.onGround = true;
                    MoveMeHelp.setSpeed(MoveMeHelp.getSpeed());
                }
            } else {
                this.canPress = false;
            }
            if (MoveHelper.mc.currentScreen == null || this.canPress) {
                boolean bl = MoveHelper.mc.gameSettings.keyBindJump.pressed = this.canPress || Keyboard.isKeyDown((int)MoveHelper.mc.gameSettings.keyBindJump.getKeyCode());
            }
        }
        if (this.StairSpeed.getBool()) {
            if (stairTick && ++tickCounter % 2 == 0 && Minecraft.player.rayGround == Minecraft.player.onGround) {
                double prev = Minecraft.player.motionY;
                Minecraft.player.jump();
                Minecraft.player.motionY = prev;
            }
            Minecraft.player.rayGround = Minecraft.player.onGround;
            if (!stairTick) {
                tickCounter = 0;
            }
            stairTick = false;
        }
        if (this.NoSlowDown.getBool()) {
            if (this.NoSlowMode.currentMode.equalsIgnoreCase("Vanilla")) {
                Minecraft.player.skipStopSprintOnEat = true;
                Minecraft.player.tempSlowEatingFactor = 1.0f;
            } else if (this.NoSlowMode.currentMode.equalsIgnoreCase("MatrixOld")) {
                if (!(Minecraft.player.isEating() && Minecraft.player.isBlocking() && Minecraft.player.isBowing() && Minecraft.player.isDrinking() || MoveHelper.mc.timer.speed != (double)1.094745f)) {
                    MoveHelper.mc.timer.speed = 1.0;
                }
                if (Minecraft.player.isEating() || Minecraft.player.isBlocking() || Minecraft.player.isBowing() || Minecraft.player.isDrinking()) {
                    if (MoveHelper.mc.gameSettings.keyBindForward.isKeyDown() || MoveHelper.mc.gameSettings.keyBindBack.isKeyDown() || Minecraft.player.isMoving()) {
                        Minecraft.player.applyEntityCollision(Minecraft.player);
                        MoveHelper.mc.timer.speed = 1.094745f;
                    }
                    if (MoveHelper.mc.gameSettings.keyBindSprint.isKeyDown() && MoveHelper.mc.gameSettings.keyBindForward.isKeyDown() && !Minecraft.player.isSprinting()) {
                        Minecraft.player.setSprinting(this.actived);
                    }
                    if (!Minecraft.player.isMoving() && MoveHelper.mc.gameSettings.keyBindForward.isKeyDown() && !Minecraft.player.isJumping()) {
                        Minecraft.player.motionX *= (double)0.31f;
                        Minecraft.player.motionZ *= (double)0.31f;
                    }
                    if (Minecraft.player.isJumping() && MoveHelper.mc.gameSettings.keyBindForward.isKeyDown() && !Minecraft.player.isMoving()) {
                        Minecraft.player.motionX *= (double)0.97245f;
                        Minecraft.player.motionZ *= (double)0.97245f;
                    }
                    if (Minecraft.player.isMoving() && MoveHelper.mc.gameSettings.keyBindForward.isKeyDown() && !Minecraft.player.isJumping()) {
                        Minecraft.player.motionX *= (double)0.425f;
                        Minecraft.player.motionZ *= (double)0.425f;
                    }
                    if (Minecraft.player.isMoving() && MoveHelper.mc.gameSettings.keyBindForward.isKeyDown() && Minecraft.player.isJumping()) {
                        Minecraft.player.motionX *= (double)0.9725f;
                        Minecraft.player.motionZ *= (double)0.9725f;
                    }
                    if (!Minecraft.player.isMoving() && MoveHelper.mc.gameSettings.keyBindBack.isKeyDown() && !Minecraft.player.isJumping()) {
                        Minecraft.player.motionX *= (double)0.6645f;
                        Minecraft.player.motionZ *= (double)0.6645f;
                    }
                    if (Minecraft.player.isJumping() && MoveHelper.mc.gameSettings.keyBindBack.isKeyDown() && !Minecraft.player.isMoving()) {
                        Minecraft.player.motionX *= (double)0.9845f;
                        Minecraft.player.motionZ *= (double)0.9845f;
                    }
                    if (Minecraft.player.isMoving() && MoveHelper.mc.gameSettings.keyBindBack.isKeyDown() && Minecraft.player.isJumping()) {
                        Minecraft.player.motionX *= (double)0.9845f;
                        Minecraft.player.motionZ *= (double)0.9845f;
                    }
                    if (Minecraft.player.isMoving() && MoveHelper.mc.gameSettings.keyBindBack.isKeyDown() && !Minecraft.player.isJumping()) {
                        Minecraft.player.motionX *= (double)0.64f;
                        Minecraft.player.motionZ *= (double)0.64f;
                    }
                    if (Minecraft.player.isMoving() && !MoveHelper.mc.gameSettings.keyBindBack.isKeyDown() && !Minecraft.player.isJumping()) {
                        Minecraft.player.motionX *= (double)0.6645f;
                        Minecraft.player.motionZ *= (double)0.6645f;
                    }
                }
                Minecraft.player.skipStopSprintOnEat = true;
                Minecraft.player.tempSlowEatingFactor = 1.0f;
            } else if (this.NoSlowMode.currentMode.equalsIgnoreCase("MatrixLatest")) {
                boolean stop = Minecraft.player.isInWater() || Minecraft.player.isInLava() || Minecraft.player.isInWeb || Minecraft.player.capabilities.isFlying || Minecraft.player.getTicksElytraFlying() > 1 || !MoveMeHelp.isMoving();
                boolean ignoreOffHandGround = false;
                if (Minecraft.player.isHandActive() && Minecraft.player.getItemInUseMaxCount() > 3 && !stop) {
                    if (!(Minecraft.player.ticksExisted % 2 != 0 || !Minecraft.player.onGround || Minecraft.player.isJumping() || ignoreOffHandGround && Minecraft.player.getActiveHand() != null && Minecraft.player.getActiveHand() == EnumHand.OFF_HAND)) {
                        Minecraft.player.multiplyMotionXZ(Minecraft.player.moveStrafing == 0.0f ? 0.5f : 0.4f);
                    } else if ((double)Minecraft.player.fallDistance > 0.725 && (double)Minecraft.player.fallDistance <= 2.5 || (double)Minecraft.player.fallDistance > 2.5) {
                        Minecraft.player.multiplyMotionXZ((double)Minecraft.player.fallDistance > 1.15 ? ((double)Minecraft.player.fallDistance > 1.4 ? 0.9375f : 0.9575f) : (Minecraft.player.moveStrafing == 0.0f ? 0.9725f : 0.9675f));
                    }
                }
                Minecraft.player.skipStopSprintOnEat = true;
                Minecraft.player.tempSlowEatingFactor = 1.0f;
            } else if (this.NoSlowMode.currentMode.equalsIgnoreCase("AACOld")) {
                Minecraft.player.skipStopSprintOnEat = true;
                Minecraft.player.tempSlowEatingFactor = 1.0f;
                if (Minecraft.player.isHandActive()) {
                    if (MoveHelper.mc.timer.speed == (double)1.1f) {
                        MoveHelper.mc.timer.speed = 1.0;
                    }
                    if (Minecraft.player.onGround && !Minecraft.player.isJumping()) {
                        Minecraft.player.multiplyMotionXZ(0.601f);
                    }
                    if (Minecraft.player.isJumping()) {
                        MoveHelper.mc.timer.speed = 1.0;
                        if (Minecraft.player.onGround) {
                            MoveHelper.mc.timer.speed = 1.1f;
                            Minecraft.player.multiplyMotionXZ(0.45f);
                        } else {
                            Minecraft.player.jumpMovementFactor = 0.02f;
                        }
                    }
                } else if (MoveHelper.mc.timer.speed == 1.1) {
                    MoveHelper.mc.timer.speed = 1.0;
                }
            } else if (this.NoSlowMode.currentMode.equalsIgnoreCase("NCP+")) {
                boolean stop;
                boolean bypassed;
                boolean bl = bypassed = Minecraft.player.getActiveHand() == EnumHand.MAIN_HAND && this.NCPFapBypass.getBool();
                if (!(bypassed || !Minecraft.player.isHandActive() || Minecraft.player.getActiveHand() != EnumHand.MAIN_HAND || Minecraft.player.isBlocking() || Minecraft.player.getActiveHand() != EnumHand.OFF_HAND || EntityLivingBase.isMatrixDamaged || Minecraft.player.isInWater() || Minecraft.player.isInLava())) {
                    float pc = MathUtils.clamp((float)Minecraft.player.getItemInUseMaxCount() / 28.0f, 0.0f, 1.0f);
                    float noslowPercent = 1.0f - MathUtils.clamp((Minecraft.player.onGround ? 0.43f : (Entity.Getmotiony > 0.0 ? 0.57f : 0.24f)) * pc, 0.0f, 0.3f);
                    Minecraft.player.multiplyMotionXZ(noslowPercent);
                    Minecraft.player.skipStopSprintOnEat = true;
                    Minecraft.player.tempSlowEatingFactor = 1.0f;
                }
                if (bypassed && Minecraft.player.getItemInUseMaxCount() == 1) {
                    Minecraft.player.connection.sendPacket(new CPacketHeldItemChange((Minecraft.player.inventory.currentItem + 1) % 8));
                    Minecraft.player.connection.sendPacket(new CPacketHeldItemChange(Minecraft.player.inventory.currentItem));
                }
                boolean bl2 = stop = Minecraft.player.isInWater() || Minecraft.player.isInLava() || Minecraft.player.isInWeb || Minecraft.player.capabilities.isFlying || Minecraft.player.getTicksElytraFlying() > 1 || !MoveMeHelp.isMoving();
                if (Minecraft.player.isHandActive() && Minecraft.player.getItemInUseMaxCount() > 3 && !stop && Minecraft.player.ticksExisted % 2 == 0 && Minecraft.player.onGround && !Minecraft.player.isJumping()) {
                    Minecraft.player.multiplyMotionXZ(Minecraft.player.moveStrafing == 0.0f ? 0.5f : 0.4f);
                }
                Minecraft.player.skipStopSprintOnEat = true;
                Minecraft.player.tempSlowEatingFactor = 1.0f;
            } else if (this.NoSlowMode.currentMode.equalsIgnoreCase("Grim")) {
                if (Minecraft.player.isHandActive()) {
                    if (Minecraft.player.getItemInUseMaxCount() >= 2 || !Minecraft.player.onGround) {
                        Minecraft.player.tempSlowEatingFactor = 1.0f;
                        Minecraft.player.skipStopSprintOnEat = true;
                    }
                    if (Minecraft.player.onGround && !Minecraft.player.isJumping()) {
                        boolean speed;
                        boolean bl = speed = Minecraft.player.getActivePotionEffect(MobEffects.SPEED) != null;
                        Minecraft.player.multiplyMotionXZ(speed ? (Minecraft.player.getItemInUseMaxCount() % 2 == 0 ? 0.7f : 0.719f) : (Minecraft.player.getItemInUseMaxCount() % 2 == 0 ? 0.85f : 0.9f));
                    } else if (!this.NoJumpSlowGrim.getBool() && Minecraft.player.getItemInUseMaxCount() == 32 && Minecraft.player.getActiveItemStack() != null && Minecraft.player.getActiveItemStack().getItem() instanceof ItemFood) {
                        Minecraft.player.connection.sendPacket(new CPacketEntityAction(Minecraft.player, CPacketEntityAction.Action.STOP_SPRINTING));
                        Minecraft.player.multiplyMotionXZ(0.5f);
                    }
                }
            } else if (this.NoSlowMode.currentMode.equalsIgnoreCase("Intave") && Minecraft.player.isHandActive()) {
                int activeTicks;
                List players;
                boolean slowRight;
                boolean bl = slowRight = Minecraft.player.getActiveHand().equals((Object)EnumHand.MAIN_HAND) && !this.NoJumpSlowGrim.getBool();
                if (slowRight && !(players = MoveHelper.mc.world.getLoadedEntityList().stream().map(Entity::getLivingBaseOf).filter(Objects::nonNull).filter(player -> player != Minecraft.player && player.isEntityAlive() && (double)Minecraft.player.getDistanceToEntity((Entity)player) < 1.8).collect(Collectors.toList())).isEmpty()) {
                    slowRight = false;
                }
                if ((activeTicks = Minecraft.player.getItemInUseMaxCount()) == 1 || activeTicks % 4 == 3) {
                    if (Minecraft.player.getActiveHand().equals((Object)EnumHand.OFF_HAND)) {
                        Minecraft.player.connection.sendPacket(new CPacketHeldItemChange((Minecraft.player.inventory.currentItem + 1) % 8));
                        Minecraft.player.connection.sendPacket(new CPacketHeldItemChange(Minecraft.player.inventory.currentItem));
                    } else if (!slowRight) {
                        Minecraft.player.connection.sendPacket(new CPacketPlayerTryUseItem(EnumHand.OFF_HAND));
                    }
                }
                if (MathUtils.getDifferenceOf(Minecraft.player.lastTickPosY, Minecraft.player.posY) > 0.0) {
                    Minecraft.player.skipStopSprintOnEat = !slowRight;
                    Minecraft.player.tempSlowEatingFactor = slowRight ? 0.2f : 1.0f;
                } else if (Minecraft.player.onGround) {
                    boolean speedPot = Minecraft.player.isPotionActive(MobEffects.SPEED);
                    boolean bl3 = Minecraft.player.skipStopSprintOnEat = activeTicks > (speedPot ? 4 : 3) && !slowRight;
                    if (activeTicks == 1) {
                        Minecraft.player.multiplyMotionXZ(speedPot && slowRight ? 0.25f : 0.5f);
                        if (Minecraft.player.isSprinting()) {
                            Minecraft.player.setSprinting(false);
                            Minecraft.player.connection.sendPacket(new CPacketEntityAction(Minecraft.player, CPacketEntityAction.Action.STOP_SPRINTING));
                        }
                    }
                    Minecraft.player.tempSlowEatingFactor = !slowRight && activeTicks <= 2 ? 0.1f : 1.0f;
                    if (slowRight) {
                        boolean f = Minecraft.player.movementInput.forwardKeyDown;
                        boolean b = Minecraft.player.movementInput.backKeyDown;
                        boolean l = Minecraft.player.movementInput.leftKeyDown;
                        boolean r = Minecraft.player.movementInput.rightKeyDown;
                        boolean diagonal = !(!f && !b || f == b || !l && !r || l == r);
                        Minecraft.player.multiplyMotionXZ((diagonal ? 0.614f : 0.659f) * (speedPot ? 0.8f : 1.0f));
                    }
                }
            }
        }
        if (this.InWebMotion.getBool()) {
            if (this.WebMotionMode.currentMode.equalsIgnoreCase("NoCollide")) {
                Minecraft.player.isInWeb = false;
            } else if (this.WebMotionMode.currentMode.equalsIgnoreCase("Custom")) {
                if (Minecraft.player.isInWeb) {
                    Minecraft.player.jumpMovementFactor = this.WebSpeedXZ.getFloat();
                    Minecraft.player.motionY = 0.0;
                    if (MoveHelper.mc.gameSettings.keyBindJump.isKeyDown()) {
                        Minecraft.player.motionY += (double)(this.WebSpeedYPlus.getFloat() * 4.0f - 0.001f);
                    }
                    if (MoveHelper.mc.gameSettings.keyBindSneak.isKeyDown()) {
                        Minecraft.player.motionY -= (double)(this.WebSpeedYMinus.getFloat() * 4.0f - 0.001f);
                    }
                }
            } else if (this.WebMotionMode.currentMode.equalsIgnoreCase("Matrix")) {
                float ex = 0.1f;
                float ex2 = 0.01f;
                double x = Minecraft.player.posX;
                double y = Minecraft.player.posY;
                double z = Minecraft.player.posZ;
                if (Minecraft.player.isInWeb) {
                    Minecraft.player.motionY = 0.0;
                    Minecraft.player.jumpMovementFactor = 0.49f;
                    if (MoveHelper.mc.gameSettings.keyBindJump.isKeyDown()) {
                        Minecraft.player.motionY += (double)1.999f;
                    } else if (MoveHelper.mc.gameSettings.keyBindSneak.isKeyDown()) {
                        Minecraft.player.motionY -= (double)1.999f;
                    }
                } else if (!MoveHelper.getBlockWithExpand(Minecraft.player.width / 2.0f, x, y + (double)ex2, z, Blocks.WEB) && MoveHelper.getBlockWithExpand(Minecraft.player.width / 2.0f, x, y - (double)ex, z, Blocks.WEB) && !Minecraft.player.isCollidedHorizontally && !Minecraft.player.onGround) {
                    if (this.WebZoom.getBool() && Minecraft.player.motionY == -0.0784000015258789 && MoveMeHelp.getSpeed() < 0.2499) {
                        MoveMeHelp.setSpeed(1.484);
                    }
                    Minecraft.player.motionY = -0.06f;
                }
            }
        }
    }

    @Override
    public void onUpdateMovement() {
        if (this.AnchoorHole.getBool()) {
            holeTick = false;
            float w = Minecraft.player.width / 2.0f;
            double x = Minecraft.player.posX;
            double y = Minecraft.player.posY + (Minecraft.player.lastTickPosY - Minecraft.player.posY) / 2.0;
            double z = Minecraft.player.posZ;
            if ((this.isHoledPosFull(new BlockPos(x, y, z)) || this.isHoledPosFull(new BlockPos(x, y - 1.0, z)) || this.isHoledPosFull(new BlockPos(x, y - 1.3, z))) && MoveHelper.getBlockFullWithExpand(w, x, y - 1.0, z, Blocks.AIR) && MoveHelper.getBlockFullWithExpand(w, x, y - 1.3, z, Blocks.AIR)) {
                Minecraft.player.jumpMovementFactor = 0.0f;
                MoveMeHelp.setSpeed(0.0);
                MoveMeHelp.setCuttingSpeed(0.0);
                if (this.Step.getBool() && this.ReverseStep.getBool()) {
                    Entity.motiony = -3.0;
                    holeTicks = 0;
                } else {
                    holeTicks = -10;
                }
                holeTick = true;
            }
        }
        if (this.Step.getBool() && this.StepUpMode.currentMode.equalsIgnoreCase("Matrix") && !Minecraft.player.isJumping() && Minecraft.player.isCollidedHorizontally && MoveMeHelp.isMoving()) {
            double moveYaw = MoveMeHelp.getMotionYaw();
            double offsetY = 1.001335979112147;
            double extendXZ = 1.0E-5;
            double sin = -Math.sin(Math.toRadians(moveYaw)) * extendXZ;
            double cos = Math.cos(Math.toRadians(moveYaw)) * extendXZ;
            AxisAlignedBB aabb = Minecraft.player.getEntityBoundingBox().offset(0.0, -0.42, 0.0);
            AxisAlignedBB aabbOff = Minecraft.player.getEntityBoundingBox().offset(sin, offsetY, cos);
            if (MoveHelper.mc.world.getCollisionBoxes(Minecraft.player, aabbOff).isEmpty() && !MoveHelper.mc.world.getCollisionBoxes(Minecraft.player, aabb).isEmpty()) {
                Minecraft.player.onGround = true;
                Minecraft.player.jump();
            }
        }
        if (this.Step.getBool() && this.ReverseStep.getBool() && Minecraft.player.onGround && Minecraft.player.isCollidedVertically && Minecraft.player.motionY < 0.0 && !Speed.posBlock(Minecraft.player.posX, Minecraft.player.posY - 0.6, Minecraft.player.posZ) && OnFalling.getDistanceTofall() < 4.0 && !Minecraft.player.isJumping()) {
            Entity.motiony = -3.0;
            holeTicks = 0;
        }
        if (this.Step.getBool() && this.StepUpMode.currentMode.equalsIgnoreCase("Vanilla")) {
            if (!Minecraft.player.isSneaking() && MoveMeHelp.moveKeysPressed()) {
                ++holeTicks;
            }
            if (holeTicks > 5 && !Minecraft.player.isSneaking() && MoveMeHelp.moveKeysPressed()) {
                Minecraft.player.stepHeight = 2.000121f;
            }
        } else if (Minecraft.player.stepHeight == 2.000121f) {
            Minecraft.player.stepHeight = 0.6f;
        }
    }

    @Override
    public void onToggled(boolean actived) {
        if (!actived) {
            holeTicks = 0;
            holeTick = false;
            if (Minecraft.player.stepHeight == 2.000121f) {
                Minecraft.player.stepHeight = 0.6f;
            }
        }
        super.onToggled(actived);
    }

    private Block getBlock(BlockPos position) {
        if (MoveHelper.mc.world != null) {
            return MoveHelper.mc.world.getBlockState(position).getBlock();
        }
        return Blocks.AIR;
    }

    private boolean isBedrock(BlockPos position) {
        Block state = Blocks.BEDROCK;
        return this.getBlock(position) == state;
    }

    private boolean isObsidian(BlockPos position) {
        Block state = Blocks.OBSIDIAN;
        return this.getBlock(position) == state;
    }

    private boolean isCurrentBlock(BlockPos position) {
        return this.isBedrock(position) || this.isObsidian(position);
    }

    private boolean isHoled(BlockPos position) {
        Block state = Blocks.AIR;
        return this.isCurrentBlock(position.add(1, 0, 0)) && this.isCurrentBlock(position.add(-1, 0, 0)) && this.isCurrentBlock(position.add(0, 0, 1)) && this.isCurrentBlock(position.add(0, 0, -1)) && Speed.posBlock(position.add(0, -1, 0).getX(), position.add(0, -1, 0).getY(), position.add(0, -1, 0).getZ()) && this.getBlock(position) == state && this.getBlock(position.add(0, 1, 0)) == state && this.getBlock(position.add(0, 2, 0)) == state;
    }

    private boolean isHoledPosFull(BlockPos pos) {
        return this.isHoled(new BlockPos(pos.getX(), pos.getY(), pos.getZ())) && !Speed.posBlock(pos.getX(), pos.getY(), pos.getZ());
    }

    public static boolean getBlockWithExpand(float expand, double x, double y, double z, Block block) {
        return MoveHelper.mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() == block || MoveHelper.mc.world.getBlockState(new BlockPos(x + (double)expand, y, z + (double)expand)).getBlock() == block || MoveHelper.mc.world.getBlockState(new BlockPos(x - (double)expand, y, z - (double)expand)).getBlock() == block || MoveHelper.mc.world.getBlockState(new BlockPos(x + (double)expand, y, z - (double)expand)).getBlock() == block || MoveHelper.mc.world.getBlockState(new BlockPos(x - (double)expand, y, z + (double)expand)).getBlock() == block || MoveHelper.mc.world.getBlockState(new BlockPos(x + (double)expand, y, z)).getBlock() == block || MoveHelper.mc.world.getBlockState(new BlockPos(x - (double)expand, y, z)).getBlock() == block || MoveHelper.mc.world.getBlockState(new BlockPos(x, y, z + (double)expand)).getBlock() == block || MoveHelper.mc.world.getBlockState(new BlockPos(x, y, z - (double)expand)).getBlock() == block;
    }

    public static boolean getBlockFullWithExpand(float expand, double x, double y, double z, Block block) {
        return MoveHelper.mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() == block && MoveHelper.mc.world.getBlockState(new BlockPos(x + (double)expand, y, z + (double)expand)).getBlock() == block && MoveHelper.mc.world.getBlockState(new BlockPos(x - (double)expand, y, z - (double)expand)).getBlock() == block && MoveHelper.mc.world.getBlockState(new BlockPos(x + (double)expand, y, z - (double)expand)).getBlock() == block && MoveHelper.mc.world.getBlockState(new BlockPos(x - (double)expand, y, z + (double)expand)).getBlock() == block && MoveHelper.mc.world.getBlockState(new BlockPos(x + (double)expand, y, z)).getBlock() == block && MoveHelper.mc.world.getBlockState(new BlockPos(x - (double)expand, y, z)).getBlock() == block && MoveHelper.mc.world.getBlockState(new BlockPos(x, y, z + (double)expand)).getBlock() == block && MoveHelper.mc.world.getBlockState(new BlockPos(x, y, z - (double)expand)).getBlock() == block;
    }

    @EventTarget
    public void onSprintSetEvent(EventSprintBlock event) {
    }

    static {
        stairTick = false;
        holeTick = false;
        holeTicks = 0;
    }
}

