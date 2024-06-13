package ru.govno.client.module.modules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import ru.govno.client.event.EventTarget;
import ru.govno.client.event.events.Event3D;
import ru.govno.client.event.events.EventMove2;
import ru.govno.client.event.events.EventPlayerMotionUpdate;
import ru.govno.client.module.Module;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.module.settings.FloatSettings;
import ru.govno.client.module.settings.ModeSettings;
import ru.govno.client.utils.Combat.RotationUtil;
import ru.govno.client.utils.Math.BlockUtils;
import ru.govno.client.utils.Math.MathUtils;
import ru.govno.client.utils.Math.TimerHelper;
import ru.govno.client.utils.Movement.MoveMeHelp;
import ru.govno.client.utils.Render.ColorUtils;
import ru.govno.client.utils.Render.RenderUtils;

public class Surround extends Module {
   public static Surround get;
   public BoolSettings Rotations;
   public BoolSettings IgnoreWalls;
   public BoolSettings AutoCenter;
   public BoolSettings PostPlacing;
   public ModeSettings PlaceHand;
   public ModeSettings Mode;
   public ModeSettings Switch;
   public FloatSettings PlaceMS;
   private final TimerHelper delayTimer = new TimerHelper();
   public boolean centered = true;
   private boolean postPlaceSet;
   private EnumHand tempHand;
   private BlockPos tempPlacePos;
   public static List<BlockPos> toPlacePoses = new ArrayList<>();

   public Surround() {
      super("Surround", 0, Module.Category.MISC);
      this.settings.add(this.Rotations = new BoolSettings("Rotations", false, this));
      this.settings.add(this.IgnoreWalls = new BoolSettings("IgnoreWalls", false, this));
      this.settings.add(this.AutoCenter = new BoolSettings("AutoCenter", true, this));
      this.settings.add(this.PlaceHand = new ModeSettings("PlaceHand", "Auto", this, new String[]{"OffHand", "MainHand", "Auto"}));
      this.settings.add(this.Mode = new ModeSettings("Mode", "Fast", this, new String[]{"Fast", "Queue"}));
      this.settings.add(this.Switch = new ModeSettings("Switch", "Client", this, new String[]{"Client", "Silent"}));
      this.settings.add(this.PlaceMS = new FloatSettings("PlaceMS", 100.0F, 250.0F, 0.0F, this));
      this.settings.add(this.PostPlacing = new BoolSettings("PostPlacing", true, this, () -> this.Mode.getMode().equalsIgnoreCase("Queue")));
      get = this;
   }

   private boolean switchIsSilent() {
      return this.Switch.getMode().equalsIgnoreCase("Silent");
   }

   private void drawPosESP(BlockPos pos, int color) {
      AxisAlignedBB aabb = new AxisAlignedBB(
         (double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), (double)pos.getX() + 1.0, (double)pos.getY() + 1.0, (double)pos.getZ() + 1.0
      );
      int col2 = ColorUtils.swapAlpha(color, (float)ColorUtils.getAlphaFromColor(color) / 2.0F);
      int col3 = ColorUtils.swapAlpha(color, (float)ColorUtils.getAlphaFromColor(color) / 5.0F);
      RenderUtils.drawCanisterBox(aabb, true, true, true, color, col2, col3);
      GlStateManager.resetColor();
   }

   private BlockPos getEntityBlockPos(Entity entity) {
      return new BlockPos(entity.posX, entity.posY + 0.3F, entity.posZ);
   }

   private Vec3d getEntityVec3dPos(Entity entity) {
      return new Vec3d(entity.posX, entity.posY, entity.posZ);
   }

   private EntityPlayer getMe() {
      return (EntityPlayer)(FreeCam.fakePlayer != null && FreeCam.get.actived ? FreeCam.fakePlayer : Minecraft.player);
   }

   private boolean meIsCentered() {
      Vec3d myVec = this.getEntityVec3dPos(this.getMe());
      float w = this.getMe().width - 1.0E-5F;
      float xzDS = 0.5F - w / 2.0F;
      double xGrate = Math.floor(myVec.xCoord) + 0.5;
      double zGrate = Math.floor(myVec.zCoord) + 0.5;
      double xMe = myVec.xCoord;
      double zMe = myVec.zCoord;
      boolean cX = xMe > xGrate - (double)xzDS && xMe < xGrate + (double)xzDS;
      boolean cZ = zMe > zGrate - (double)xzDS && zMe < zGrate + (double)xzDS;
      return cX && cZ;
   }

   private Vec3d getCentereVec() {
      return new Vec3d(
         (double)this.getEntityBlockPos(this.getMe()).getX() + 0.5,
         (double)this.getEntityBlockPos(this.getMe()).getY(),
         (double)this.getEntityBlockPos(this.getMe()).getZ() + 0.5
      );
   }

   private boolean posNotCollide(BlockPos pos) {
      List<Entity> e = new CopyOnWriteArrayList<>();
      if (mc.world == null) {
         return true;
      } else {
         mc.world.getLoadedEntityList().forEach(ents -> {
            if (ents != null && ents instanceof EntityItem item) {
               e.add(item);
            }

            if (ents != null && ents instanceof EntityEnderCrystal crystal) {
               e.add(crystal);
            }
         });
         return pos != null;
      }
   }

   public boolean getBlockWithExpand(float expand, double x, double y, double z, Block block) {
      return mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() == block
         || mc.world.getBlockState(new BlockPos(x + (double)expand, y, z + (double)expand)).getBlock() == block
         || mc.world.getBlockState(new BlockPos(x - (double)expand, y, z - (double)expand)).getBlock() == block
         || mc.world.getBlockState(new BlockPos(x + (double)expand, y, z - (double)expand)).getBlock() == block
         || mc.world.getBlockState(new BlockPos(x - (double)expand, y, z + (double)expand)).getBlock() == block
         || mc.world.getBlockState(new BlockPos(x + (double)expand, y, z)).getBlock() == block
         || mc.world.getBlockState(new BlockPos(x - (double)expand, y, z)).getBlock() == block
         || mc.world.getBlockState(new BlockPos(x, y, z + (double)expand)).getBlock() == block
         || mc.world.getBlockState(new BlockPos(x, y, z - (double)expand)).getBlock() == block;
   }

   public boolean getBlockWithExpand(float expand, BlockPos pos, Block block) {
      return this.getBlockWithExpand(expand, (double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), block);
   }

   private Material getBlockMaterial(BlockPos pos) {
      return mc.world.getBlockState(pos).getBlock().getMaterial(mc.world.getBlockState(pos));
   }

   private boolean blockMaterialIsCurrent(BlockPos pos) {
      return !this.getBlockMaterial(pos).isReplaceable() && !this.getBlockMaterial(pos).isLiquid() && this.getBlockMaterial(pos).blocksMovement();
   }

   private boolean blockMaterialIsCurrentWithSideSets(BlockPos pos) {
      return this.blockMaterialIsCurrent(pos.west())
         || this.blockMaterialIsCurrent(pos.east())
         || this.blockMaterialIsCurrent(pos.south())
         || this.blockMaterialIsCurrent(pos.north())
         || this.blockMaterialIsCurrent(pos.down());
   }

   private boolean canPlaceObsidian(BlockPos pos, boolean ignoreWalls) {
      boolean aired = this.getBlockMaterial(pos).isReplaceable();
      boolean neared = this.blockMaterialIsCurrentWithSideSets(pos);
      return aired && neared && this.posNotCollide(pos) && (ignoreWalls || BlockUtils.getPlaceableSideSeen(pos, this.getMe()) != null);
   }

   private void moveTo(Vec3d vec, EventMove2 move, float speed) {
      float yawToVec = RotationUtil.getLookAngles(vec)[0];
      double sin = -Math.sin(Math.toRadians((double)yawToVec)) * (double)speed;
      double cos = Math.cos(Math.toRadians((double)yawToVec)) * (double)speed;
      move.motion().xCoord = sin;
      move.motion().zCoord = cos;
      MoveMeHelp.multiplySpeed(0.01F);
   }

   private double getSpeedMove() {
      return MathUtils.clamp(MoveMeHelp.getCuttingSpeed(), 0.23, 0.5);
   }

   @EventTarget
   public void onMove(EventMove2 move) {
      if (!this.centered && this.AutoCenter.getBool()) {
         this.moveTo(this.getCentereVec(), move, (float)this.getSpeedMove());
      }
   }

   private List<BlockPos> getCurPoses() {
      BlockPos myPos = this.basePosAtMe();
      List<BlockPos> cur = new ArrayList<>();
      Arrays.stream(EnumFacing.HORIZONTALS)
         .filter(
            face -> this.getBlockMaterial(myPos.down().add(face.getDirectionVec())).isReplaceable()
                  && BlockUtils.canPlaceBlock(myPos.down().add(face.getDirectionVec()))
         )
         .forEach(face -> cur.add(myPos.down().add(face.getDirectionVec())));
      Arrays.stream(EnumFacing.HORIZONTALS)
         .filter(
            face -> this.getBlockMaterial(myPos.add(face.getDirectionVec())).isReplaceable() && BlockUtils.canPlaceBlock(myPos.add(face.getDirectionVec()))
         )
         .forEach(face -> cur.add(myPos.add(face.getDirectionVec())));
      return cur;
   }

   private void updatePoses() {
      toPlacePoses = this.getCurPoses().stream().filter(temp -> this.canPlaceObsidian(temp, this.IgnoreWalls.getBool())).collect(Collectors.toList());
   }

   private EnumFacing getPlaceableSide(BlockPos pos) {
      for (EnumFacing facing : EnumFacing.values()) {
         BlockPos offset = pos.offset(facing);
         if (mc.world.getBlockState(offset).getBlock().canCollideCheck(mc.world.getBlockState(offset), false)) {
            IBlockState state = mc.world.getBlockState(offset);
            if (!state.getMaterial().isReplaceable()) {
               return facing;
            }
         }
      }

      return null;
   }

   private boolean itemInOffHand() {
      return Minecraft.player.getHeldItemOffhand().getItem() instanceof ItemBlock;
   }

   private boolean haveItemInMainHand() {
      return getItemInHotbar() != -1;
   }

   private boolean haveItem() {
      return (this.haveItemInMainHand() || Minecraft.player.getHeldItemOffhand().getItem() instanceof ItemBlock) && this.getUsedHand() != null;
   }

   private EnumHand getUsedHand() {
      boolean off = this.PlaceHand.currentMode.equalsIgnoreCase("OffHand");
      boolean main = this.PlaceHand.currentMode.equalsIgnoreCase("MainHand");
      boolean auto = this.PlaceHand.currentMode.equalsIgnoreCase("Auto");
      return off && this.itemInOffHand()
         ? EnumHand.OFF_HAND
         : (main && this.haveItemInMainHand() ? EnumHand.MAIN_HAND : (auto ? (this.itemInOffHand() ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND) : null));
   }

   public static int getItemInHotbar() {
      for (int i = 0; i < 9; i++) {
         if (Minecraft.player.inventory.getStackInSlot(i).getItem() instanceof ItemBlock) {
            return i;
         }
      }

      return -1;
   }

   private int getSlotForItem() {
      return getItemInHotbar();
   }

   private void placeBlock(BlockPos pos, EnumHand placeHand) {
      EnumFacing v4 = BlockUtils.getPlaceableSide(pos);
      if (v4 != null) {
         EnumFacing v2 = v4.getOpposite();
         BlockPos v1 = pos.offset(v4);
         Vec3d v3 = new Vec3d(v1).addVector(0.5, 0.5, 0.5).add(new Vec3d(v2.getDirectionVec()).scale(0.5));
         mc.playerController.processRightClickBlock(Minecraft.player, mc.world, v1, v2, v3, placeHand);
         Minecraft.player.connection.sendPacket(new CPacketAnimation(placeHand));
      }
   }

   private void placeBlockPost(BlockPos pos, EnumHand placeHand) {
      if (!this.postPlaceSet) {
         this.tempPlacePos = pos;
         this.tempHand = placeHand;
         this.postPlaceSet = true;
      }
   }

   private void updatePlaceBlockPost() {
      if (this.postPlaceSet) {
         this.placeBlockPost(this.tempPlacePos, this.tempHand);
         this.tempPlacePos = null;
         this.tempHand = null;
         this.postPlaceSet = false;
      }
   }

   private void handSlotSnapFor(EnumHand placeHand, boolean packetSwap, Runnable code) {
      int startSlot = Minecraft.player.inventory.currentItem;
      int currentSlot = this.getSlotForItem();
      if (placeHand == EnumHand.MAIN_HAND && startSlot != this.getSlotForItem()) {
         Minecraft.player.inventory.currentItem = currentSlot;
         if (packetSwap) {
            mc.playerController.syncCurrentPlayItem();
         }
      }

      code.run();
      if (placeHand == EnumHand.MAIN_HAND && packetSwap) {
         Minecraft.player.inventory.currentItem = startSlot;
      }
   }

   private BlockPos basePosAtMe() {
      BlockPos pos = this.getEntityBlockPos(this.getMe());
      if (!this.getMe().onGround) {
         int x = pos.getX();
         int z = pos.getZ();

         for (int y = pos.getY() - 5; y < pos.getY(); y++) {
            if (Speed.posBlock((double)x, (double)y, (double)z) && !Speed.posBlock((double)x, (double)(y + 1), (double)z)) {
               pos = new BlockPos(x, y, z).up();
            }
         }
      }

      return pos;
   }

   private boolean canPlace() {
      return this.haveItem() && (!this.getMe().isJumping() || !MoveMeHelp.isMoving());
   }

   @EventTarget
   public void onEventPlayerUpdate(EventPlayerMotionUpdate event) {
      BlockPos bpos = toPlacePoses.isEmpty() ? this.tempPlacePos : toPlacePoses.get(0);
      if (bpos != null && this.Rotations.getBool()) {
         EnumFacing face = BlockUtils.getPlaceableSide(bpos);
         Vec3d toRot = new Vec3d(bpos)
            .addVector(0.5, 0.5, 0.5)
            .addVector((double)face.getFrontOffsetX() * 0.5, (double)face.getFrontOffsetY() * 0.5, (double)face.getFrontOffsetZ() * 0.5);
         if (toRot == null) {
            return;
         }

         float[] rotate = RotationUtil.getNeededFacing(new Vec3d(toRot.xCoord, toRot.yCoord, toRot.zCoord), false, Minecraft.player, false);
         if (rotate == null) {
            return;
         }

         event.setYaw(rotate[0]);
         event.setPitch(rotate[1]);
         this.getMe().rotationYawHead = rotate[0];
         this.getMe().renderYawOffset = rotate[0];
         this.getMe().rotationPitchHead = rotate[1];
         HitAura.get.rotations = rotate;
      }
   }

   @Override
   public void onUpdate() {
      if (this.canPlace()) {
         this.centered = this.meIsCentered();
         this.updatePoses();
         boolean isFast = this.Mode.currentMode.equalsIgnoreCase("Fast");
         this.updatePlaceBlockPost();
         if (toPlacePoses != null && toPlacePoses.size() > 0 && this.delayTimer.hasReached((double)this.PlaceMS.getFloat())) {
            this.handSlotSnapFor(this.getUsedHand(), this.switchIsSilent(), () -> {
               boolean sneak = Minecraft.player.isSneaking();
               if (!sneak) {
                  Minecraft.player.connection.preSendPacket(new CPacketEntityAction(Minecraft.player, CPacketEntityAction.Action.START_SNEAKING));
               }

               if (isFast) {
                  toPlacePoses.forEach(pos -> this.placeBlock(pos, this.getUsedHand()));
               } else if (toPlacePoses.get(0) != null) {
                  this.placeBlock(toPlacePoses.get(0), this.getUsedHand());
               }

               if (!sneak) {
                  Minecraft.player.connection.preSendPacket(new CPacketEntityAction(Minecraft.player, CPacketEntityAction.Action.STOP_SNEAKING));
               }
            });
            this.delayTimer.reset();
         }
      } else {
         this.centered = true;
         toPlacePoses.clear();
      }
   }

   @EventTarget
   public void onRender3D(Event3D event) {
      if (this.actived) {
         BlockPos myPos = this.basePosAtMe();
         if (toPlacePoses != null && toPlacePoses.size() > 0) {
            RenderUtils.setup3dForBlockPos(() -> {
               if (!this.centered) {
                  this.drawPosESP(myPos, ColorUtils.getColor(255, 40, 40, 180));
               }

               if (!this.centered) {
                  this.drawPosESP(myPos.up(), ColorUtils.getColor(255, 40, 40, 180));
               }

               toPlacePoses.forEach(pos -> this.drawPosESP(pos, ColorUtils.getColor(100, 255, 100, 100)));
            }, true);
         }
      }
   }
}
