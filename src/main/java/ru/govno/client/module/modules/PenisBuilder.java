package ru.govno.client.module.modules;

import com.google.common.collect.Lists;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerDigging.Action;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;
import ru.govno.client.event.EventTarget;
import ru.govno.client.event.events.EventMovementInput;
import ru.govno.client.event.events.EventPlayerMotionUpdate;
import ru.govno.client.event.events.EventPostPlace;
import ru.govno.client.event.events.EventRotationJump;
import ru.govno.client.event.events.EventRotationStrafe;
import ru.govno.client.module.Module;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.utils.Combat.RotationUtil;
import ru.govno.client.utils.Math.BlockUtils;
import ru.govno.client.utils.Math.MathUtils;
import ru.govno.client.utils.Movement.MoveMeHelp;
import ru.govno.client.utils.Render.ColorUtils;
import ru.govno.client.utils.Render.RenderUtils;

public class PenisBuilder extends Module {
   BoolSettings SamiLegit;
   BoolSettings RotateMoveSide;
   BoolSettings Rendering;
   private int ticksSynced;
   private List<BlockPos> posesToPlace = Lists.newArrayList();
   private BlockPos currentPosition;
   private float[] lastRotated;
   private final Tessellator tessellator = Tessellator.getInstance();
   private final BufferBuilder buffer = this.tessellator.getBuffer();
   private final List<PenisBuilder.FragBoxAnimation> fragBoxAnimations = Lists.newArrayList();

   public PenisBuilder() {
      super("PenisBuilder", 0, Module.Category.MISC);
      this.settings.add(this.SamiLegit = new BoolSettings("SamiLegit", true, this));
      this.settings.add(this.RotateMoveSide = new BoolSettings("RotateMoveSide", false, this));
      this.settings.add(this.Rendering = new BoolSettings("Rendering", false, this));
   }

   private int getPenisHeight() {
      return 4;
   }

   private int getEffectsColor() {
      this.stateAnim.to = this.isActived() ? 1.0F : 0.0F;
      return ClientColors.getColor1(0, this.stateAnim.getAnim());
   }

   private long getEffectsLongestAliveTime() {
      return 400L + (this.SamiLegit.getBool() ? (2L + (long)this.getPenisHeight()) * 50L : 200L);
   }

   private boolean canReplaceBlock(BlockPos pos) {
      if (pos != null) {
         if (BlockUtils.getBlockMaterial(pos).isReplaceable() && !BlockUtils.getBlockMaterial(pos).isLiquid()) {
            return !BlockUtils.isOccupiedByEnt(pos, false);
         } else {
            IBlockState state = mc.world.getBlockState(pos);
            return state.getBlockHardness(mc.world, pos) == 0.0F && !BlockUtils.isOccupiedByEnt(pos, false) && !BlockUtils.getBlockMaterial(pos).isLiquid();
         }
      } else {
         return false;
      }
   }

   private List<BlockPos> getPlacePositions(BlockPos pos, int penisHeight) {
      List<BlockPos> positions = new LinkedList<>();
      EnumFacing faceOff = Minecraft.player.getHorizontalFacing().rotateY();
      positions.add(new BlockPos(pos.getX() + faceOff.getFrontOffsetX(), pos.getY(), pos.getZ() + faceOff.getFrontOffsetZ()));
      faceOff = faceOff.getOpposite();
      positions.add(new BlockPos(pos.getX() + faceOff.getFrontOffsetX(), pos.getY(), pos.getZ() + faceOff.getFrontOffsetZ()));
      BlockPos tempBP = new BlockPos(pos.getX(), pos.getY(), pos.getZ());

      for (short yOffset = 1; yOffset < penisHeight; yOffset++) {
         positions.add(tempBP = tempBP.up());
      }

      return positions.stream().filter(this::canReplaceBlock).collect(Collectors.toList());
   }

   private boolean getIsHaveRequiredAmountBlockItems(ItemStack stack) {
      return !stack.isEmpty() && stack.getItem() instanceof ItemBlock && stack.stackSize >= 1 + this.getPenisHeight();
   }

   private boolean getIsBlockPosFromMouseOver(BlockPos samplePos) {
      return samplePos != null
         && mc.objectMouseOver != null
         && mc.objectMouseOver.getBlockPos() != null
         && mc.objectMouseOver.getBlockPos().distanceSq(samplePos) == 0.0;
   }

   private boolean runRmbBlockClick(EnumHand hand, BlockPos pos) {
      if (hand != null && pos != null) {
         EnumFacing enumFace = BlockUtils.getPlaceableSideSeen(pos, Minecraft.player);
         if (enumFace == null) {
            enumFace = BlockUtils.getPlaceableSide(pos);
            if (enumFace == null) {
               return false;
            }
         }

         if (!BlockUtils.getBlockMaterial(pos).isReplaceable()) {
            IBlockState state = mc.world.getBlockState(pos);
            if (state.getBlockHardness(mc.world, pos) <= 0.6F && mc.playerController.onPlayerDestroyBlock(pos)) {
               mc.getConnection().sendPacket(new CPacketPlayerDigging(Action.START_DESTROY_BLOCK, pos, EnumFacing.UP));
               mc.getConnection().sendPacket(new CPacketPlayerDigging(Action.STOP_DESTROY_BLOCK, pos, EnumFacing.UP));
            }
         }

         boolean sn = Minecraft.player.isSneaking();
         EnumFacing faceOpposite = enumFace.getOpposite();
         BlockPos offsetPos = pos.offset(enumFace);
         Vec3d facingVec = new Vec3d(offsetPos).addVector(0.5, 0.5, 0.5).add(new Vec3d(faceOpposite.getDirectionVec()).scale(0.5));
         if (!sn) {
            mc.getConnection()
               .sendPacket(new CPacketEntityAction(Minecraft.player, net.minecraft.network.play.client.CPacketEntityAction.Action.START_SNEAKING));
         }

         mc.playerController.processRightClickBlock(Minecraft.player, mc.world, offsetPos, faceOpposite, facingVec, hand);
         if (!sn) {
            mc.getConnection()
               .sendPacket(new CPacketEntityAction(Minecraft.player, net.minecraft.network.play.client.CPacketEntityAction.Action.STOP_SNEAKING));
         }

         return true;
      } else {
         return false;
      }
   }

   private boolean runRmbBlockClicks(EnumHand hand, List<BlockPos> positions) {
      for (BlockPos pos : positions) {
         if (!this.runRmbBlockClick(hand, pos)) {
            return false;
         }
      }

      return true;
   }

   private boolean getIsPlaceListAviable(List<BlockPos> positions, int penisHeight) {
      return positions != null && positions.size() == penisHeight + 1;
   }

   private EnumHand getPuttingEnumHand(int penisHeight) {
      if (Minecraft.player.getHeldItemOffhand().getItem() instanceof ItemBlock && Minecraft.player.getHeldItemOffhand().stackSize >= penisHeight + 1) {
         return EnumHand.OFF_HAND;
      } else {
         return Minecraft.player.getHeldItemMainhand().getItem() instanceof ItemBlock && Minecraft.player.getHeldItemMainhand().stackSize >= penisHeight + 1
            ? EnumHand.MAIN_HAND
            : null;
      }
   }

   @EventTarget
   public void onPlaceBlock(EventPostPlace event) {
      if (this.ticksSynced == 0 && this.posesToPlace.isEmpty()) {
         int penisHeight = this.getPenisHeight();
         this.ticksSynced = this.SamiLegit.getBool() ? (3 + penisHeight) * 2 : 3;
         if (this.getIsHaveRequiredAmountBlockItems(event.getStack())) {
            EnumHand currentHand = this.getPuttingEnumHand(penisHeight);
            if (currentHand != null) {
               List<BlockPos> positions = this.getPlacePositions(event.getPosition(), penisHeight);
               if (this.getIsPlaceListAviable(positions, penisHeight)) {
                  this.posesToPlace = positions;
                  positions.add(event.getPosition());
                  this.addEffects(positions, this.getEffectsLongestAliveTime());
                  if (!this.SamiLegit.getBool()) {
                     this.runRmbBlockClicks(currentHand, positions);
                  }
               }
            }
         }
      }
   }

   @Override
   public void onUpdate() {
      if (this.ticksSynced > 0) {
         this.ticksSynced--;
      } else {
         this.posesToPlace.clear();
      }

      this.posesToPlace.removeIf(pos -> !this.canReplaceBlock(pos));
      this.currentPosition = !this.posesToPlace.isEmpty() && this.posesToPlace.get(0) != null ? this.posesToPlace.get(0) : null;
      if (this.currentPosition != null) {
         this.runRmbBlockClick(
            this.getPuttingEnumHand(this.getPenisHeight()), BlockUtils.canPlaceBlock(this.currentPosition) ? this.currentPosition : this.currentPosition.down()
         );
      }
   }

   @EventTarget
   public void onPlayerUpdate(EventPlayerMotionUpdate event) {
      if (this.SamiLegit.getBool()) {
         this.lastRotated = null;
         if (this.currentPosition != null) {
            Vec3d posToRotate = new Vec3d(this.currentPosition).addVector(0.5, 0.5, 0.5);
            EnumFacing faceRotOff = BlockUtils.getPlaceableSideSeen(this.currentPosition, Minecraft.player);
            if (faceRotOff == null) {
               faceRotOff = BlockUtils.getPlaceableSide(this.currentPosition);
            }

            if (faceRotOff != null) {
               faceRotOff = faceRotOff.getOpposite();
               posToRotate.add(new Vec3d(faceRotOff.getDirectionVec()).scale(0.5));
            }

            if (posToRotate != null) {
               this.lastRotated = RotationUtil.getNeededFacing(posToRotate, false, Minecraft.player, false);
               Minecraft.player.rotationYawHead = this.lastRotated[0];
               Minecraft.player.renderYawOffset = this.lastRotated[0];
               Minecraft.player.rotationPitchHead = this.lastRotated[1];
               HitAura.get.rotations = new float[]{this.lastRotated[0], this.lastRotated[1]};
               if (this.RotateMoveSide.getBool()
                  && Minecraft.player.toCancelSprintTicks <= 1
                  && MathUtils.getDifferenceOf(Minecraft.player.rotationYaw, this.lastRotated[0]) >= 45.0) {
                  Minecraft.player.toCancelSprintTicks = 2;
               }

               event.setYaw(this.lastRotated[0]);
               event.setPitch(this.lastRotated[1]);
            }
         }
      }
   }

   @EventTarget
   public void onMovementInput(EventMovementInput event) {
      if (this.lastRotated != null && this.SamiLegit.getBool() && this.RotateMoveSide.getBool()) {
         MoveMeHelp.fixDirMove(event, this.lastRotated[0]);
      }
   }

   @EventTarget
   public void onSilentStrafe(EventRotationStrafe event) {
      if (this.lastRotated != null && this.SamiLegit.getBool() && this.RotateMoveSide.getBool()) {
         event.setYaw(this.lastRotated[0]);
      }
   }

   @EventTarget
   public void onSilentJump(EventRotationJump event) {
      if (this.lastRotated != null && this.SamiLegit.getBool() && this.RotateMoveSide.getBool()) {
         event.setYaw(this.lastRotated[0]);
      }
   }

   private void set3DPolygonsMode(Runnable renderVoid) {
      RenderUtils.setup3dForBlockPos(() -> {
         GL11.glEnable(2929);
         GL11.glDepthMask(false);
         renderVoid.run();
         GL11.glDepthMask(true);
      }, true);
   }

   private void addEffects(List<BlockPos> positions, long effectLongest) {
      int globalColor = this.getEffectsColor();
      positions.forEach(pos -> this.fragBoxAnimations.add(new PenisBuilder.FragBoxAnimation(pos, effectLongest, globalColor)));
   }

   private void renderAndControlEffects() {
      if (!this.fragBoxAnimations.isEmpty()) {
         this.fragBoxAnimations.removeIf(PenisBuilder.FragBoxAnimation::removeIf);
         this.set3DPolygonsMode(() -> this.fragBoxAnimations.forEach(PenisBuilder.FragBoxAnimation::drawVertices));
      }
   }

   @Override
   public void alwaysRender3D() {
      if (!(this.stateAnim.getAnim() * 255.0F < 1.0F)) {
         this.renderAndControlEffects();
      }
   }

   private class FragBoxAnimation {
      private final BlockPos pos;
      private final long sysTime = System.currentTimeMillis();
      private final long maxAliveTime;
      private final int color;

      public FragBoxAnimation(BlockPos pos, long maxAliveTime, int color) {
         this.pos = pos;
         this.maxAliveTime = maxAliveTime;
         this.color = color;
      }

      public float getTimePC() {
         return Math.min((float)(System.currentTimeMillis() - this.sysTime) / (float)this.maxAliveTime, 1.0F);
      }

      public float getAlphaPC() {
         float timePC = this.getTimePC();
         return (float)MathUtils.easeInOutQuad((double)MathUtils.valWave01(timePC));
      }

      public void drawVertices() {
         float aPC = this.getAlphaPC() * (BlockUtils.blockMaterialIsCurrent(this.pos) ? 1.0F : 0.15F);
         int color = ColorUtils.swapAlpha(this.color, (float)ColorUtils.getAlphaFromColor(this.color) * aPC);
         if (ColorUtils.getAlphaFromColor(color) >= 1) {
            for (float extend = 0.0F; extend < 0.4F * aPC; extend += 0.05F) {
               int col = ColorUtils.swapAlpha(
                  ColorUtils.getOverallColorFrom(color, ColorUtils.swapAlpha(-1, (float)ColorUtils.getAlphaFromColor(color)), 0.2F),
                  (float)ColorUtils.getAlphaFromColor(color) * 0.3F * MathUtils.valWave01(extend / 0.4F)
               );
               RenderUtils.drawCanisterBox(
                  new AxisAlignedBB(this.pos).expandXyz((double)(extend / 10.0F)), true, extend == 0.05F, true, col, 0, ColorUtils.swapAlpha(col, 1.0F)
               );
            }
         }
      }

      public boolean removeIf() {
         return this.getTimePC() == 1.0F;
      }
   }
}
