package ru.govno.client.module.modules;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCocoa;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockNetherWart;
import net.minecraft.block.BlockOre;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntityBed;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Sphere;
import ru.govno.client.event.EventTarget;
import ru.govno.client.event.events.Event3D;
import ru.govno.client.event.events.EventPlayerMotionUpdate;
import ru.govno.client.event.events.EventRotationJump;
import ru.govno.client.event.events.EventRotationStrafe;
import ru.govno.client.module.Module;
import ru.govno.client.module.modules.PotionThrower;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.module.settings.FloatSettings;
import ru.govno.client.module.settings.ModeSettings;
import ru.govno.client.utils.Combat.RotationUtil;
import ru.govno.client.utils.Math.BlockUtils;
import ru.govno.client.utils.Math.MathUtils;
import ru.govno.client.utils.Render.ColorUtils;
import ru.govno.client.utils.Render.RenderUtils;

public class Nuker
extends Module {
    FloatSettings MaxBlocksShare;
    BoolSettings Rotations;
    BoolSettings ClientLook;
    BoolSettings SilentMoveRot;
    BoolSettings IgnoreWalls;
    BoolSettings BreakFermBlocks;
    ModeSettings Target;
    public static Nuker get;
    private double smoothProgress = 0.0;
    private float alphaHPG = 0.0f;
    private float hpgX = 0.0f;
    private float hpgY = 0.0f;
    private float hpgZ = 0.0f;
    private Vec3d animatedHPG = null;
    final List<BlockPos> positions = new ArrayList<BlockPos>();
    List<BlockPos> targetedPoses = new ArrayList<BlockPos>();
    private static BlockPos targetedPosition;
    public static BlockPos renderPosition;
    float yaw;
    float lastRYaw;
    float lastRPitch;

    public Nuker() {
        super("Nuker", 0, Module.Category.MISC);
        this.MaxBlocksShare = new FloatSettings("MaxBlocksShare", 1.0f, 4.0f, 1.0f, this);
        this.settings.add(this.MaxBlocksShare);
        this.Rotations = new BoolSettings("Rotations", true, this);
        this.settings.add(this.Rotations);
        this.ClientLook = new BoolSettings("ClientLook", false, this, () -> this.Rotations.getBool());
        this.settings.add(this.ClientLook);
        this.SilentMoveRot = new BoolSettings("SilentMoveRot", true, this, () -> this.Rotations.getBool());
        this.settings.add(this.SilentMoveRot);
        this.IgnoreWalls = new BoolSettings("IgnoreWalls", false, this);
        this.settings.add(this.IgnoreWalls);
        this.Target = new ModeSettings("Target", "All", this, new String[]{"All", "Ores", "Wooden", "Stones", "Ferma", "Bed", "Web", "Leaves"});
        this.settings.add(this.Target);
        this.BreakFermBlocks = new BoolSettings("BreakFermBlocks", false, this, () -> this.Target.currentMode.equalsIgnoreCase("Ferma"));
        this.settings.add(this.BreakFermBlocks);
        get = this;
    }

    private Vec3d playerVec3dPos() {
        return new Vec3d(mc.getRenderManager().getRenderPosX() + Minecraft.player.motionX, mc.getRenderManager().getRenderPosY() + 0.51, mc.getRenderManager().getRenderPosZ() + Minecraft.player.motionZ);
    }

    private ArrayList<BlockPos> positionsZone(Vec3d pos, float range) {
        ArrayList<BlockPos> poses = new ArrayList<BlockPos>();
        int x = (int)(-range);
        while ((float)x < range) {
            int z = (int)(-range);
            while ((float)z < range) {
                int y = 0;
                while ((float)y < range) {
                    BlockPos pos12 = new BlockPos(pos.xCoord + (double)x, pos.yCoord + (double)y, pos.zCoord + (double)z);
                    poses.add(pos12);
                    ++y;
                }
                ++z;
            }
            ++x;
        }
        poses.sort(Comparator.comparing(pos1 -> pos.distanceTo(new Vec3d((Vec3i)pos1).addVector(0.5, 0.5, 0.5))));
        return poses;
    }

    private boolean isUnbreakebleBlock(Block block) {
        return block == Blocks.AIR || (block == Blocks.BEDROCK || block == Blocks.BARRIER) && !Minecraft.player.isCreative() || block instanceof BlockLiquid;
    }

    private Vec3d[] getPositionsZone01(Vec3d playerPos, float range) {
        return new Vec3d[]{new Vec3d(playerPos.xCoord - (double)range, playerPos.yCoord, playerPos.zCoord - (double)range), new Vec3d(playerPos.xCoord + (double)range, playerPos.yCoord + (double)range, playerPos.zCoord + (double)range)};
    }

    private void drawZone(float range) {
        Vec3d vec = this.playerVec3dPos();
        GL11.glAlphaFunc((int)516, (float)0.003921569f);
        int color = ColorUtils.getColor(255, 255, 255, 10);
        int color2 = ColorUtils.getColor(255, 255, 255, 5);
        Sphere sphere = new Sphere();
        GL11.glTranslated((double)vec.xCoord, (double)vec.yCoord, (double)vec.zCoord);
        GL11.glRotated((double)90.0, (double)1.0, (double)0.0, (double)0.0);
        sphere.setDrawStyle(100011);
        RenderUtils.glColor(color);
        sphere.draw(range, 12, 12);
        RenderUtils.glColor(color2);
        sphere.setDrawStyle(100012);
        sphere.draw(range, 12, 12);
        GL11.glRotated((double)90.0, (double)-1.0, (double)0.0, (double)0.0);
        GL11.glTranslated((double)(-vec.xCoord), (double)(-vec.yCoord), (double)(-vec.zCoord));
        GL11.glAlphaFunc((int)516, (float)0.1f);
        GlStateManager.resetColor();
    }

    public void resetRenderHittingProgress() {
        this.smoothProgress = 0.0;
    }

    private void drawHittingProgress() {
        float animationsSpeed = (float)((double)0.02f * Minecraft.frameTime);
        BlockPos pos = this.getRenderPosition();
        if (pos != null) {
            float toX = pos.getX();
            float toY = pos.getY();
            float toZ = pos.getZ();
            this.hpgX = MathUtils.harp(this.hpgX, toX, animationsSpeed);
            this.hpgY = MathUtils.harp(this.hpgY, toY, animationsSpeed);
            this.hpgZ = MathUtils.harp(this.hpgZ, toZ, animationsSpeed);
            this.alphaHPG = MathUtils.harp(this.alphaHPG, 255.0f, animationsSpeed * 3.0f);
        } else if (MathUtils.getDifferenceOf(this.alphaHPG, 0.0f) > 0.0) {
            this.alphaHPG = MathUtils.harp(this.alphaHPG, 0.0f, animationsSpeed);
        }
        this.animatedHPG = new Vec3d((double)this.hpgX + 0.5, (double)this.hpgY + 0.5, (double)this.hpgZ + 0.5);
        float progress = Nuker.mc.playerController.isHittingBlock ? Nuker.mc.playerController.curBlockDamageMP : 0.0f;
        this.smoothProgress = MathUtils.lerp((float)this.smoothProgress, progress, animationsSpeed * 3.0f);
        if (this.smoothProgress != 0.0) {
            Vec3d firstPoint = new Vec3d(this.animatedHPG.xCoord - 0.5 * this.smoothProgress, this.animatedHPG.yCoord - 0.5 * this.smoothProgress, this.animatedHPG.zCoord - 0.5 * this.smoothProgress);
            Vec3d lastPoint = new Vec3d(this.animatedHPG.xCoord + 0.5 * this.smoothProgress, this.animatedHPG.yCoord + 0.5 * this.smoothProgress, this.animatedHPG.zCoord + 0.5 * this.smoothProgress);
            AxisAlignedBB axisBox = new AxisAlignedBB(firstPoint.xCoord, firstPoint.yCoord, firstPoint.zCoord, lastPoint.xCoord, lastPoint.yCoord, lastPoint.zCoord);
            GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)(this.alphaHPG / 255.0f));
            int color = ColorUtils.getColor(255, 255, 255, this.alphaHPG / 3.0f);
            int color2 = ColorUtils.getColor(255, 255, 255, this.alphaHPG / 25.5f);
            RenderUtils.drawCanisterBox(axisBox, true, true, true, color, color, color2);
        }
    }

    private boolean seenBlockPos(BlockPos pos) {
        return Minecraft.player.canEntityBeSeenCoords(pos.getX(), pos.getY(), pos.getZ());
    }

    private boolean canSeenBlock(BlockPos pos, boolean ignoreNoSeen) {
        if (this.seenBlockPos(pos) || this.seenBlockPos(pos.add(1, 1, 1)) || this.seenBlockPos(pos.add(-1, 1, -1)) || this.seenBlockPos(pos.add(1, 1, -1)) || this.seenBlockPos(pos.add(-1, 1, 1)) || this.seenBlockPos(pos.add(1, 1, 0)) || this.seenBlockPos(pos.add(-1, 1, 0)) || this.seenBlockPos(pos.add(0, 1, 1)) || this.seenBlockPos(pos.add(0, 1, -1)) || this.seenBlockPos(pos.add(1, 0, 1)) || this.seenBlockPos(pos.add(-1, 0, -1)) || this.seenBlockPos(pos.add(1, 0, -1)) || this.seenBlockPos(pos.add(-1, 0, 1)) || this.seenBlockPos(pos.add(1, 0, 0)) || this.seenBlockPos(pos.add(-1, 0, 0)) || this.seenBlockPos(pos.add(0, 0, 1)) || this.seenBlockPos(pos.add(0, 0, -1)) || pos == Minecraft.player.getPosition().add(0, 1, 0)) {
            return true;
        }
        return ignoreNoSeen;
    }

    private boolean blockIsInRange(BlockPos pos, float range, boolean returnTrue) {
        int pointX = pos.getX() + ((double)pos.getX() < this.playerVec3dPos().xCoord ? 1 : 0);
        int pointY = pos.getY() + ((double)pos.getY() < this.playerVec3dPos().yCoord + (double)Minecraft.player.getEyeHeight() ? 1 : 0);
        int pointZ = pos.getZ() + ((double)pos.getZ() < this.playerVec3dPos().zCoord ? 1 : 0);
        double xDifference = (double)pointX - this.playerVec3dPos().xCoord;
        double yDifference = (double)pointY - (this.playerVec3dPos().yCoord + (double)Minecraft.player.getEyeHeight());
        double zDifference = (double)pointZ - this.playerVec3dPos().zCoord;
        return returnTrue || Math.sqrt(xDifference * xDifference + yDifference * yDifference + zDifference * zDifference) < (double)range;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private boolean canBreakBlock(BlockPos pos, String mode) {
        IBlockState state = Nuker.mc.world.getBlockState(pos);
        Block block = state.getBlock();
        Material mat = state.getMaterial();
        switch (mode) {
            case "All": {
                return true;
            }
            case "Ores": {
                if (block instanceof BlockOre) return true;
                if (block == Blocks.LIT_REDSTONE_ORE) return true;
                if (block != Blocks.REDSTONE_ORE) return false;
                return true;
            }
            case "Wooden": {
                if (mat != Material.WOOD) return false;
                return true;
            }
            case "Stones": {
                if (mat != Material.ROCK) return false;
                if (block instanceof BlockOre) return false;
                if (block == Blocks.LIT_REDSTONE_ORE) return false;
                if (block == Blocks.REDSTONE_ORE) return false;
                return true;
            }
            case "Ferma": {
                if (block instanceof BlockCrops) {
                    BlockCrops crop = (BlockCrops)block;
                    if (crop.isMaxAge(state)) return true;
                }
                if (block == Blocks.REEDS) {
                    if (!Minecraft.player.capabilities.isFlying && Nuker.mc.world.getBlockState(pos.up()).getBlock() == Blocks.REEDS) {
                        if (Nuker.mc.world.getBlockState(pos.down()).getBlock() != Blocks.REEDS) return true;
                    }
                    if (Minecraft.player.capabilities.isFlying && Nuker.mc.world.getBlockState(pos.up()).getBlock() == Blocks.REEDS) {
                        if (Nuker.mc.world.getBlockState(pos.down()).getBlock() != Blocks.REEDS) return true;
                    }
                }
                if (block instanceof BlockNetherWart) {
                    if (state.getValue(BlockNetherWart.AGE) == 3) return true;
                }
                if (block != Blocks.MELON_BLOCK && block != Blocks.PUMPKIN) {
                    if (!(block instanceof BlockCocoa)) return false;
                    BlockCocoa cocoa = (BlockCocoa)block;
                    if (state.getValue(BlockCocoa.AGE) != 2) return false;
                }
                if (!this.BreakFermBlocks.getBool()) return false;
                return true;
            }
            case "Bed": {
                return Nuker.mc.world.getLoadedTileEntityList().stream().map(tile -> tile instanceof TileEntityBed ? (TileEntityBed)tile : null).filter(Objects::nonNull).anyMatch(bed -> bed.getPos() != null && bed.getPos().equals(pos));
            }
            case "Web": {
                if (block != Blocks.WEB) return false;
                return true;
            }
            case "Leaves": {
                return block instanceof BlockLeaves;
            }
        }
        return false;
    }

    private List<BlockPos> getTargetBlocks(int maxCount, Vec3d playerPos, float range, boolean ignoreWalls, boolean checkDistance, String mode) {
        this.positions.clear();
        for (BlockPos position : this.positionsZone(playerPos, range + 1.0f)) {
            IBlockState state = Nuker.mc.world.getBlockState(position);
            Block block = state.getBlock();
            if (!this.blockIsInRange(position, range - 0.3f, checkDistance) || this.isUnbreakebleBlock(block) || !this.canSeenBlock(position, ignoreWalls) || !this.canBreakBlock(position, mode) || this.positions.size() >= maxCount || block == Blocks.AIR) continue;
            this.positions.add(position);
        }
        return this.positions;
    }

    private float getRange() {
        return 5.0f;
    }

    private void setTargetPositions(Vec3d playerPos, float range, boolean ignoreWalls, boolean checkDistance, int maxPosesCount) {
        this.targetedPoses = this.getTargetBlocks(maxPosesCount, playerPos, range, ignoreWalls, checkDistance, this.Target.currentMode);
        targetedPosition = this.targetedPoses == null || this.targetedPoses.isEmpty() || this.targetedPoses.get(0) == null ? null : this.targetedPoses.get(0);
    }

    private BlockPos getTargetedPosition() {
        return targetedPosition;
    }

    private BlockPos getRenderPosition() {
        return renderPosition;
    }

    private void processBreakBlock(List<BlockPos> poses) {
        if (poses.isEmpty()) {
            return;
        }
        poses.forEach(pos -> {
            if (pos != null) {
                EnumFacing face = BlockUtils.getPlaceableSide(pos);
                if (face == null) {
                    face = EnumFacing.UP;
                }
                face = face.getOpposite();
                if (!this.IgnoreWalls.getBool() && this.Rotations.getBool() && Nuker.mc.objectMouseOver != null) {
                    float prevYaw = Minecraft.player.rotationYaw;
                    float prevPitch = Minecraft.player.rotationPitch;
                    Minecraft.player.rotationYaw = this.lastRYaw;
                    Minecraft.player.rotationPitch = this.lastRPitch;
                    Nuker.mc.entityRenderer.getMouseOver(1.0f);
                    face = Nuker.mc.objectMouseOver.sideHit;
                    pos = Nuker.mc.objectMouseOver.getBlockPos();
                    Nuker.mc.entityRenderer.getMouseOver(1.0f);
                    Minecraft.player.rotationYaw = prevYaw;
                    Minecraft.player.rotationPitch = prevPitch;
                }
                if (face != null && pos != null && Nuker.mc.playerController.onPlayerDamageBlock((BlockPos)pos, face)) {
                    Minecraft.player.swingArm(EnumHand.MAIN_HAND);
                }
            }
        });
    }

    private void rotations(EventPlayerMotionUpdate e, BlockPos pos) {
        if (pos == null) {
            return;
        }
        AxisAlignedBB blockAABB = new AxisAlignedBB(pos);
        if (Nuker.mc.world != null) {
            blockAABB = Nuker.mc.world.getBlockState(pos).getSelectedBoundingBox(Nuker.mc.world, pos);
        }
        e.setYaw(RotationUtil.getMatrixRotations4BlockPos(pos.add(0.5, (blockAABB.maxY - blockAABB.minY) / 2.0, 0.5))[0]);
        e.setPitch(RotationUtil.getMatrixRotations4BlockPos(pos.add(0.5, (blockAABB.maxY - blockAABB.minY) / 3.0, 0.5))[1]);
        Minecraft.player.rotationYawHead = e.getYaw();
        Minecraft.player.renderYawOffset = e.getYaw();
        Minecraft.player.rotationPitchHead = e.getPitch();
        if (this.ClientLook.getBool()) {
            Minecraft.player.rotationYaw = e.getYaw();
            Minecraft.player.rotationPitch = e.getPitch();
        }
        this.lastRYaw = e.getYaw();
        this.lastRPitch = e.getPitch();
    }

    @Override
    public void onUpdate() {
        boolean ignoreWalls = this.IgnoreWalls.getBool();
        boolean checkDistance = false;
        int maxBlocksSame = this.MaxBlocksShare.getInt();
        this.setTargetPositions(this.playerVec3dPos(), this.getRange(), ignoreWalls, false, maxBlocksSame);
        this.processBreakBlock(this.targetedPoses);
    }

    @EventTarget
    public void onUpdate(EventPlayerMotionUpdate e) {
        if (this.Rotations.getBool() && !PotionThrower.get.forceThrow && !PotionThrower.get.callThrowPotions) {
            this.rotations(e, this.getTargetedPosition());
            this.yaw = e.getYaw();
        } else {
            this.yaw = -10001.0f;
        }
    }

    @EventTarget
    public void onStrafeSide(EventRotationStrafe e) {
        if (this.SilentMoveRot.getBool() && this.Rotations.getBool() && this.getTargetedPosition() != null && this.yaw != -10001.0f) {
            e.setYaw(this.yaw);
        }
    }

    @EventTarget
    public void onJumpSide(EventRotationJump e) {
        if (this.SilentMoveRot.getBool() && this.Rotations.getBool() && this.getTargetedPosition() != null && this.yaw != -10001.0f) {
            e.setYaw(this.yaw);
        }
    }

    @EventTarget
    public void onRender3D(Event3D e) {
        RenderUtils.setup3dForBlockPos(() -> {
            this.drawHittingProgress();
            GL11.glEnable((int)2929);
            this.drawZone(this.getRange());
        }, true);
    }

    static {
        targetedPosition = null;
        renderPosition = null;
    }
}

