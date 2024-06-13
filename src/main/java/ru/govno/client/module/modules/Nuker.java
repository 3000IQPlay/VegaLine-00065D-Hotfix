package ru.govno.client.module.modules;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCocoa;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockOre;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntityBed;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;
import ru.govno.client.event.EventTarget;
import ru.govno.client.event.events.Event3D;
import ru.govno.client.event.events.EventPlayerMotionUpdate;
import ru.govno.client.event.events.EventRotationJump;
import ru.govno.client.event.events.EventRotationStrafe;
import ru.govno.client.module.Module;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.module.settings.FloatSettings;
import ru.govno.client.module.settings.ModeSettings;
import ru.govno.client.utils.Combat.RotationUtil;
import ru.govno.client.utils.Math.BlockUtils;
import ru.govno.client.utils.Math.MathUtils;
import ru.govno.client.utils.Render.ColorUtils;
import ru.govno.client.utils.Render.RenderUtils;

public class Nuker extends Module {
   FloatSettings MaxBlocksShare;
   BoolSettings Rotations;
   BoolSettings ClientLook;
   BoolSettings SilentMoveRot;
   BoolSettings CheckAsACube;
   BoolSettings IgnoreWalls;
   BoolSettings BreakFermBlocks;
   ModeSettings Target;
   private double dzX = 0.0;
   private double dzY = 0.0;
   private double dzZ = 0.0;
   private double dzX2 = 0.0;
   private double dzY2 = 0.0;
   private double dzZ2 = 0.0;
   private double smoothProgress = 0.0;
   private float alphaHPG = 0.0F;
   private float hpgX = 0.0F;
   private float hpgY = 0.0F;
   private float hpgZ = 0.0F;
   private Vec3d animatedHPG = null;
   final List<BlockPos> positions = new ArrayList<>();
   List<BlockPos> targetedPoses = new ArrayList<>();
   private static BlockPos targetedPosition = null;
   public static BlockPos renderPosition = null;
   float yaw;
   float lastRYaw;
   float lastRPitch;

   public Nuker() {
      super("Nuker", 0, Module.Category.MISC);
      this.settings.add(this.MaxBlocksShare = new FloatSettings("MaxBlocksShare", 1.0F, 4.0F, 1.0F, this));
      this.settings.add(this.Rotations = new BoolSettings("Rotations", true, this));
      this.settings.add(this.ClientLook = new BoolSettings("ClientLook", false, this, () -> this.Rotations.getBool()));
      this.settings.add(this.SilentMoveRot = new BoolSettings("SilentMoveRot", true, this, () -> this.Rotations.getBool()));
      this.settings.add(this.CheckAsACube = new BoolSettings("CheckAsACube", false, this));
      this.settings.add(this.IgnoreWalls = new BoolSettings("IgnoreWalls", false, this));
      this.settings.add(this.Target = new ModeSettings("Target", "All", this, new String[]{"All", "Ores", "Wooden", "Stones", "Ferma", "Bed"}));
      this.settings.add(this.BreakFermBlocks = new BoolSettings("BreakFermBlocks", false, this, () -> this.Target.currentMode.equalsIgnoreCase("Ferma")));
   }

   private Vec3d playerVec3dPos() {
      return new Vec3d(mc.getRenderManager().getRenderPosX(), mc.getRenderManager().getRenderPosY() + 0.51, mc.getRenderManager().getRenderPosZ());
   }

   private ArrayList<BlockPos> positionsZone(Vec3d playerPos, float[] ranges) {
      ArrayList<BlockPos> poses = new ArrayList<>();

      for (int x = (int)(-ranges[0]); (float)x < ranges[0]; x++) {
         for (int z = (int)(-ranges[0]); (float)z < ranges[0]; z++) {
            for (int y = (int)(-ranges[2]); (float)y < ranges[1]; y++) {
               BlockPos pos;
               if ((pos = new BlockPos(playerPos.xCoord + (double)x, playerPos.yCoord + (double)y, playerPos.zCoord + (double)z)) != null) {
                  poses.add(pos);
               }
            }
         }
      }

      poses.sort(Comparator.comparingInt(posx -> (int)posx.getDistanceToBlockPos(BlockUtils.getEntityBlockPos(Minecraft.player))));
      return poses;
   }

   private boolean isUnbreakebleBlock(Block block) {
      return block == Blocks.AIR || (block == Blocks.BEDROCK || block == Blocks.BARRIER) && !Minecraft.player.isCreative() || block instanceof BlockLiquid;
   }

   private Vec3d[] getPositionsZone01(Vec3d playerPos, float[] ranges) {
      return new Vec3d[]{
         new Vec3d(
            playerPos.xCoord - (double)this.getRanges()[0], playerPos.yCoord - (double)this.getRanges()[2], playerPos.zCoord - (double)this.getRanges()[0]
         ),
         new Vec3d(
            playerPos.xCoord + (double)this.getRanges()[0], playerPos.yCoord + (double)this.getRanges()[1], playerPos.zCoord + (double)this.getRanges()[0]
         )
      };
   }

   private void drawZone(float[] ranges) {
      float ext = 0.01F;
      Vec3d vecMin = this.getPositionsZone01(this.playerVec3dPos(), ranges)[0].addVector((double)ext, (double)ext, (double)ext);
      Vec3d vecMax = this.getPositionsZone01(this.playerVec3dPos(), ranges)[1].addVector((double)(-ext), (double)(-ext), (double)(-ext));
      float animationsSpeed = (float)(0.0125F * Minecraft.frameTime);
      double toX = vecMin.xCoord;
      double toY = vecMin.yCoord;
      double toZ = vecMin.zCoord;
      double toX2 = vecMax.xCoord;
      double toY2 = vecMax.yCoord;
      double toZ2 = vecMax.zCoord;
      this.dzX = MathUtils.harpD(this.dzX, toX, (double)animationsSpeed);
      this.dzY = MathUtils.harpD(this.dzY, toY, (double)animationsSpeed);
      this.dzZ = MathUtils.harpD(this.dzZ, toZ, (double)animationsSpeed);
      this.dzX2 = MathUtils.harpD(this.dzX2, toX2, (double)animationsSpeed);
      this.dzY2 = MathUtils.harpD(this.dzY2, toY2, (double)animationsSpeed);
      this.dzZ2 = MathUtils.harpD(this.dzZ2, toZ2, (double)animationsSpeed);
      AxisAlignedBB axisBox = new AxisAlignedBB(this.dzX, this.dzY, this.dzZ, this.dzX2, this.dzY2, this.dzZ2);
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.33333F);
      int color = ColorUtils.getColor(255, 255, 255, 50);
      int color2 = ColorUtils.getColor(255, 255, 255, 5);
      RenderUtils.drawCanisterBox(axisBox, true, false, true, color, 0, color2);
   }

   private void drawHittingProgress() {
      float animationsSpeed = (float)(0.02F * Minecraft.frameTime);
      BlockPos pos = this.getRenderPosition();
      if (pos != null) {
         float toX = (float)pos.getX();
         float toY = (float)pos.getY();
         float toZ = (float)pos.getZ();
         this.hpgX = MathUtils.harp(this.hpgX, toX, animationsSpeed);
         this.hpgY = MathUtils.harp(this.hpgY, toY, animationsSpeed);
         this.hpgZ = MathUtils.harp(this.hpgZ, toZ, animationsSpeed);
         this.alphaHPG = MathUtils.harp(this.alphaHPG, 255.0F, animationsSpeed * 3.0F);
      } else if (MathUtils.getDifferenceOf(this.alphaHPG, 0.0F) > 0.0) {
         this.alphaHPG = MathUtils.harp(this.alphaHPG, 0.0F, animationsSpeed);
      }

      this.animatedHPG = new Vec3d((double)this.hpgX + 0.5, (double)this.hpgY + 0.5, (double)this.hpgZ + 0.5);
      float progress = mc.playerController.curBlockDamageMP;
      this.smoothProgress = (double)MathUtils.lerp((float)this.smoothProgress, progress, animationsSpeed * 3.0F);
      Vec3d firstPoint = new Vec3d(
         this.animatedHPG.xCoord - 0.5 * this.smoothProgress,
         this.animatedHPG.yCoord - 0.5 * this.smoothProgress,
         this.animatedHPG.zCoord - 0.5 * this.smoothProgress
      );
      Vec3d lastPoint = new Vec3d(
         this.animatedHPG.xCoord + 0.5 * this.smoothProgress,
         this.animatedHPG.yCoord + 0.5 * this.smoothProgress,
         this.animatedHPG.zCoord + 0.5 * this.smoothProgress
      );
      AxisAlignedBB axisBox = new AxisAlignedBB(firstPoint.xCoord, firstPoint.yCoord, firstPoint.zCoord, lastPoint.xCoord, lastPoint.yCoord, lastPoint.zCoord);
      GL11.glColor4f(1.0F, 1.0F, 1.0F, this.alphaHPG / 255.0F);
      int color = ColorUtils.getColor(255, 255, 255, this.alphaHPG / 3.0F);
      int color2 = ColorUtils.getColor(255, 255, 255, this.alphaHPG / 25.5F);
      RenderUtils.drawCanisterBox(axisBox, true, true, true, color, color, color2);
   }

   private boolean seenBlockPos(BlockPos pos) {
      return Minecraft.player.canEntityBeSeenCoords((double)pos.getX(), (double)pos.getY(), (double)pos.getZ());
   }

   private boolean canSeenBlock(BlockPos pos, boolean ignoreNoSeen) {
      return !this.seenBlockPos(pos)
            && !this.seenBlockPos(pos.add(1, 1, 1))
            && !this.seenBlockPos(pos.add(-1, 1, -1))
            && !this.seenBlockPos(pos.add(1, 1, -1))
            && !this.seenBlockPos(pos.add(-1, 1, 1))
            && !this.seenBlockPos(pos.add(1, 1, 0))
            && !this.seenBlockPos(pos.add(-1, 1, 0))
            && !this.seenBlockPos(pos.add(0, 1, 1))
            && !this.seenBlockPos(pos.add(0, 1, -1))
            && !this.seenBlockPos(pos.add(1, 0, 1))
            && !this.seenBlockPos(pos.add(-1, 0, -1))
            && !this.seenBlockPos(pos.add(1, 0, -1))
            && !this.seenBlockPos(pos.add(-1, 0, 1))
            && !this.seenBlockPos(pos.add(1, 0, 0))
            && !this.seenBlockPos(pos.add(-1, 0, 0))
            && !this.seenBlockPos(pos.add(0, 0, 1))
            && !this.seenBlockPos(pos.add(0, 0, -1))
            && pos != Minecraft.player.getPosition().add(0, 1, 0)
         ? ignoreNoSeen
         : true;
   }

   private boolean blockIsInRange(BlockPos pos, float[] ranges, boolean returnTrue) {
      int pointX = pos.getX() + ((double)pos.getX() < this.playerVec3dPos().xCoord ? 1 : 0);
      int pointY = pos.getY() + ((double)pos.getY() < this.playerVec3dPos().yCoord + 1.0 ? 1 : 0);
      int pointZ = pos.getZ() + ((double)pos.getZ() < this.playerVec3dPos().zCoord ? 1 : 0);
      float yRange = (double)pos.getY() < this.playerVec3dPos().xCoord + 1.0 ? ranges[2] : ranges[1];
      double sqrtRangeToPos = Math.abs(Math.sqrt((double)(ranges[0] * ranges[0] + yRange * yRange)));
      double xDifference = (double)pointX - this.playerVec3dPos().xCoord;
      double yDifference = (double)pointY - (this.playerVec3dPos().yCoord + 1.0);
      double zDifference = (double)pointZ - this.playerVec3dPos().zCoord;
      return returnTrue
         || Math.abs(Math.sqrt(xDifference * xDifference + zDifference * zDifference)) + Math.abs(Math.sqrt(yDifference * yDifference)) <= sqrtRangeToPos;
   }

   private boolean canBreakBlock(BlockPos pos, String mode) {
      IBlockState state = mc.world.getBlockState(pos);
      Block block = state.getBlock();
      Material mat = state.getMaterial();
      switch (mode) {
         case "All":
            return true;
         case "Ores":
            return block instanceof BlockOre || block == Blocks.LIT_REDSTONE_ORE || block == Blocks.REDSTONE_ORE;
         case "Wooden":
            return mat == Material.WOOD;
         case "Stones":
            return mat == Material.ROCK && !(block instanceof BlockOre) && block != Blocks.LIT_REDSTONE_ORE && block != Blocks.REDSTONE_ORE;
         case "Ferma":
            return block instanceof BlockCrops crop && crop.isMaxAge(state)
               || (block == Blocks.MELON_BLOCK || block == Blocks.PUMPKIN || block instanceof BlockCocoa cocoa && state.getValue(BlockCocoa.AGE) == 2)
                  && this.BreakFermBlocks.getBool();
         case "Bed":
            return mc.world
               .getLoadedTileEntityList()
               .stream()
               .map(tile -> tile instanceof TileEntityBed ? (TileEntityBed)tile : null)
               .filter(Objects::nonNull)
               .anyMatch(bed -> bed.getPos() != null && bed.getPos().equals(pos));
         default:
            return false;
      }
   }

   private List<BlockPos> getTargetBlocks(int maxCount, Vec3d playerPos, float[] ranges, boolean ignoreWalls, boolean checkDistance, String mode) {
      this.positions.clear();

      for (BlockPos position : this.positionsZone(playerPos, ranges)) {
         IBlockState state = mc.world.getBlockState(position);
         Block block = state.getBlock();
         if (this.blockIsInRange(position, ranges, checkDistance)
            && !this.isUnbreakebleBlock(block)
            && this.canSeenBlock(position, ignoreWalls)
            && this.canBreakBlock(position, mode)
            && this.positions.size() < maxCount) {
            this.positions.add(position);
         }
      }

      return this.positions;
   }

   private float[] getRanges() {
      return new float[]{3.5F, 5.0F, 0.0F};
   }

   private void setTargetPositions(Vec3d playerPos, float[] ranges, boolean ignoreWalls, boolean checkDistance, int maxPosesCount) {
      this.targetedPoses = this.getTargetBlocks(maxPosesCount, playerPos, ranges, ignoreWalls, checkDistance, this.Target.currentMode);
      targetedPosition = this.targetedPoses != null && !this.targetedPoses.isEmpty() && this.targetedPoses.get(0) != null ? this.targetedPoses.get(0) : null;
   }

   private BlockPos getTargetedPosition() {
      return targetedPosition;
   }

   private BlockPos getRenderPosition() {
      return renderPosition;
   }

   private void processBreakBlock(List<BlockPos> poses) {
      if (!poses.isEmpty()) {
         poses.forEach(pos -> {
            if (pos != null) {
               EnumFacing face = BlockUtils.getPlaceableSide(pos);
               if (face != null) {
                  face = face.getOpposite();
               }

               if (!this.IgnoreWalls.getBool() && this.Rotations.getBool() && mc.objectMouseOver != null) {
                  float prevYaw = Minecraft.player.rotationYaw;
                  float prevPitch = Minecraft.player.rotationPitch;
                  Minecraft.player.rotationYaw = this.lastRYaw;
                  Minecraft.player.rotationPitch = this.lastRPitch;
                  mc.entityRenderer.getMouseOver(1.0F);
                  face = mc.objectMouseOver.sideHit;
                  pos = mc.objectMouseOver.getBlockPos();
                  mc.entityRenderer.getMouseOver(1.0F);
                  Minecraft.player.rotationYaw = prevYaw;
                  Minecraft.player.rotationPitch = prevPitch;
               }

               if (face != null && pos != null && mc.playerController.onPlayerDamageBlock(pos, face)) {
                  Minecraft.player.swingArm(EnumHand.MAIN_HAND);
               }
            }
         });
      }
   }

   private void rotations(EventPlayerMotionUpdate e, BlockPos pos) {
      if (pos != null) {
         AxisAlignedBB blockAABB = new AxisAlignedBB(pos);
         if (mc.world != null) {
            blockAABB = mc.world.getBlockState(pos).getSelectedBoundingBox(mc.world, pos);
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
   }

   @Override
   public void onUpdate() {
      boolean ignoreWalls = this.IgnoreWalls.getBool();
      boolean checkDistance = this.CheckAsACube.getBool();
      int maxBlocksSame = this.MaxBlocksShare.getInt();
      this.setTargetPositions(this.playerVec3dPos(), this.getRanges(), ignoreWalls, checkDistance, maxBlocksSame);
      this.processBreakBlock(this.targetedPoses);
   }

   @EventTarget
   public void onUpdate(EventPlayerMotionUpdate e) {
      if (this.Rotations.getBool() && !PotionThrower.get.forceThrow && !PotionThrower.get.callThrowPotions) {
         this.rotations(e, this.getTargetedPosition());
         this.yaw = e.getYaw();
      } else {
         this.yaw = -10001.0F;
      }
   }

   @EventTarget
   public void onStrafeSide(EventRotationStrafe e) {
      if (this.SilentMoveRot.getBool() && this.Rotations.getBool() && this.getTargetedPosition() != null && this.yaw != -10001.0F) {
         e.setYaw(this.yaw);
      }
   }

   @EventTarget
   public void onJumpSide(EventRotationJump e) {
      if (this.SilentMoveRot.getBool() && this.Rotations.getBool() && this.getTargetedPosition() != null && this.yaw != -10001.0F) {
         e.setYaw(this.yaw);
      }
   }

   @EventTarget
   public void onRender3D(Event3D e) {
      RenderUtils.setup3dForBlockPos(() -> {
         this.drawHittingProgress();
         GL11.glEnable(2929);
         this.drawZone(this.getRanges());
      }, true);
   }
}
