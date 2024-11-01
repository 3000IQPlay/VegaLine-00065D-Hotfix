package ru.govno.client.module.modules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector4d;
import net.minecraft.block.BlockLiquid;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemEnderPearl;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.RayTraceResult.Type;
import org.lwjgl.opengl.GL11;
import ru.govno.client.Client;
import ru.govno.client.event.EventTarget;
import ru.govno.client.event.events.Event3D;
import ru.govno.client.event.events.EventMovementInput;
import ru.govno.client.event.events.EventPlayerMotionUpdate;
import ru.govno.client.event.events.EventRotationJump;
import ru.govno.client.event.events.EventRotationStrafe;
import ru.govno.client.module.Module;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.module.settings.FloatSettings;
import ru.govno.client.utils.CrystalField;
import ru.govno.client.utils.MusicHelper;
import ru.govno.client.utils.Combat.RotationUtil;
import ru.govno.client.utils.Math.MathUtils;
import ru.govno.client.utils.Math.TimerHelper;
import ru.govno.client.utils.Movement.MoveMeHelp;
import ru.govno.client.utils.Render.ColorUtils;
import ru.govno.client.utils.Render.RenderUtils;

public class ThrowFollow extends Module {
   public static ThrowFollow get;
   public BoolSettings PearlFromInventory;
   public BoolSettings RotateMoveSide;
   public BoolSettings LClickFollow;
   public BoolSettings RenderMarkers;
   public FloatSettings AnyTargetRange;
   public FloatSettings MaxTpSpread;
   private boolean wantToPlayTrajPop;
   private long playTrajPopDelay;
   private final TimerHelper playTrajPopWaiter = TimerHelper.TimerHelperReseted();
   private final List<ThrowFollow.TimedTarget> TIMED_TARGETS = new ArrayList<>();
   private final List<EntityLivingBase> tempListEntities = new CopyOnWriteArrayList<>();
   private final List<Vec3d> toThrowVectors = new ArrayList<>();
   public int runTicks;
   private float[] currentRot;
   private int slot;
   private final List<ThrowFollow.TimedVec3d> markers = new ArrayList<>();

   public ThrowFollow() {
      super("ThrowFollow", 0, Module.Category.COMBAT);
      this.settings.add(this.PearlFromInventory = new BoolSettings("PearlFromInventory", true, this));
      this.settings.add(this.AnyTargetRange = new FloatSettings("AnyTargetRange", 5.0F, 9.0F, 0.0F, this));
      this.settings.add(this.MaxTpSpread = new FloatSettings("MaxTpSpread", 7.0F, 10.0F, 1.0F, this));
      this.settings.add(this.RotateMoveSide = new BoolSettings("RotateMoveSide", false, this));
      this.settings.add(this.LClickFollow = new BoolSettings("LClickFollow", false, this));
      this.settings.add(this.RenderMarkers = new BoolSettings("RenderMarkers", true, this));
      get = this;
   }

   private void playTrajOpn() {
      MusicHelper.playSound("trajopn.wav", 0.3F);
   }

   private void playTrajNew() {
      MusicHelper.playSound("trajnew.wav", 0.15F);
   }

   private void playTrajPop(long wait) {
      this.wantToPlayTrajPop = true;
      this.playTrajPopDelay = wait;
      this.playTrajPopWaiter.reset();
   }

   private void playTrajPopUpdater() {
      if (this.wantToPlayTrajPop && this.playTrajPopWaiter.hasReached((double)this.playTrajPopDelay)) {
         MusicHelper.playSound("trajpop.wav", 0.25F);
         this.wantToPlayTrajPop = false;
      }
   }

   private Vector4d virtPearlFlyingDetachPos(double x, double y, double z, double mx, double my, double mz, boolean checkSelfBox) {
      int attempt = 0;

      while (attempt < 100) {
         Vec3d prevPos = new Vec3d(x, y, z);
         x += mx;
         y += my;
         z += mz;
         if (!(y < 1.0)) {
            double slowingFactor = mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() instanceof BlockLiquid ? 0.8 : 0.99;
            mx *= slowingFactor;
            my *= slowingFactor;
            mz *= slowingFactor;
            my -= 0.03;
            Vec3d pos = new Vec3d(x, y, z);
            RayTraceResult rayTrace = mc.world.rayTraceBlocks(prevPos, pos, false, false, true);
            if (rayTrace == null || (attempt <= 1 || !rayTrace.typeOfHit.equals(Type.ENTITY)) && !rayTrace.typeOfHit.equals(Type.BLOCK)) {
               if (checkSelfBox) {
                  AxisAlignedBB aabb = Minecraft.player.boundingBox;
                  if (aabb != null
                     && pos.xCoord >= aabb.minX
                     && pos.xCoord <= aabb.maxX
                     && pos.yCoord >= aabb.minY
                     && pos.yCoord <= aabb.maxY
                     && pos.zCoord >= aabb.minZ
                     && pos.zCoord <= aabb.maxZ) {
                     return new Vector4d(prevPos.xCoord, prevPos.yCoord, prevPos.zCoord, (double)attempt);
                  }
               }

               attempt++;
               continue;
            }

            return new Vector4d(rayTrace.hitVec.xCoord, rayTrace.hitVec.yCoord, rayTrace.hitVec.zCoord, (double)attempt);
         }
         break;
      }

      return null;
   }

   private Vector4d getDetachPosForVirtPearl(EntityPlayer entitySelf, float yaw, float pitch) {
      double yawRad = Math.toRadians((double)yaw);
      double pitchRad = Math.toRadians((double)pitch);
      double x = entitySelf.lastTickPosX - Math.cos(yawRad) * 0.16;
      double y = entitySelf.posY + (double)entitySelf.getEyeHeight() - 0.1000000014901161;
      double z = entitySelf.lastTickPosZ - Math.sin(yawRad) * 0.16;
      double motionX = -Math.sin(yawRad) * Math.cos(pitchRad) * 0.4;
      double motionY = -Math.sin(pitchRad) * 0.4;
      double motionZ = Math.cos(yawRad) * Math.cos(pitchRad) * 0.4;
      double mX = entitySelf instanceof EntityPlayerSP sp ? sp.motionX : entitySelf.posX - entitySelf.lastTickPosX;
      double mY = entitySelf.onGround ? 0.0 : (entitySelf instanceof EntityPlayerSP spx ? spx.motionY : entitySelf.posY - entitySelf.lastTickPosY);
      double mZ = entitySelf instanceof EntityPlayerSP spxx ? spxx.motionZ : entitySelf.posZ - entitySelf.lastTickPosZ;
      double motionSQRT3 = Math.sqrt((double)((float)(motionX * motionX + motionY * motionY + motionZ * motionZ)));
      motionX /= motionSQRT3;
      motionY /= motionSQRT3;
      motionZ /= motionSQRT3;
      motionX *= 1.5;
      motionY *= 1.5;
      motionZ *= 1.5;
      motionX += mX;
      if (mY < 0.0) {
         mY /= 2.0;
      }

      if (mY < -1.0) {
         mY /= 1.3333333F;
      }

      motionY += mY;
      motionZ += mZ;
      return this.virtPearlFlyingDetachPos(x, y, z, motionX, motionY, motionZ, false);
   }

   private Vector4d getPearlDetachPos(EntityEnderPearl pearl, float pTicks) {
      if (pearl == null) {
         return null;
      } else {
         Entity thrower = pearl.parsedThrower;
         if (thrower == null) {
            return null;
         } else {
            double posX = MathUtils.lerp(pearl.prevPosX, pearl.posX, (double)pTicks);
            double posY = MathUtils.lerp(pearl.prevPosY, pearl.posY, (double)pTicks);
            double posZ = MathUtils.lerp(pearl.prevPosZ, pearl.posZ, (double)pTicks);
            double motionX = pearl.motionX + (thrower instanceof EntityPlayerSP ? thrower.motionX : thrower.posX - thrower.lastTickPosX);
            double motionY = pearl.motionY
               + (thrower.onGround ? 0.0 : (thrower instanceof EntityPlayerSP ? thrower.motionY : thrower.posY - thrower.lastTickPosY));
            double motionZ = pearl.motionZ + (thrower instanceof EntityPlayerSP ? thrower.motionZ : thrower.posZ - thrower.lastTickPosZ);
            return this.virtPearlFlyingDetachPos(posX, posY, posZ, motionX, motionY, motionZ, true);
         }
      }
   }

   private float calcPitch(Vec3d vecAt, Vec3d vecTo) {
      double dstXZ = Math.hypot(vecTo.xCoord - vecAt.xCoord, vecTo.zCoord - vecAt.zCoord);
      double yTheta = 6.125 * (vecTo.yCoord - vecAt.yCoord);
      double calibre = 0.05F;
      yTheta = calibre * (calibre * dstXZ * dstXZ + yTheta);
      yTheta = Math.sqrt(9.378906F - yTheta);
      double maxTheta = 3.0625 - yTheta;
      yTheta = Math.atan2(maxTheta * maxTheta + yTheta, calibre * dstXZ);
      maxTheta = Math.atan2(maxTheta, calibre * dstXZ);
      float pitch = (float)(-Math.toDegrees(Math.min(yTheta, maxTheta)));
      return Float.isNaN(pitch) ? 0.0F : pitch;
   }

   private VecRotBuffer getBufferForThrowPearl(Vec3d toCalcNearest, double maxDistanceToNearestPos, double minDistanceToSelf, int yawFilteringOffset, float maxYawStep) {
      double d;
      if (ThrowFollow.mc.world == null || Minecraft.player == null || !Minecraft.player.isEntityAlive() || toCalcNearest == null || !ThrowFollow.mc.world.isBlockLoaded(new BlockPos(toCalcNearest), true)) {
         return null;
      }
      EntityPlayerSP entitySelf = Minecraft.player;
      double sX = entitySelf.posX;
      double sZ = entitySelf.posZ;
      if (entitySelf instanceof EntityPlayerSP) {
         EntityPlayerSP sp = entitySelf;
         d = sp.motionY;
      } else {
         d = entitySelf.posY - entitySelf.lastTickPosY;
      }
      double mY = d;
      toCalcNearest = toCalcNearest.addVector((sX - entitySelf.lastTickPosX) * 3.0, 0.0, (sZ - entitySelf.lastTickPosZ) * 3.0);
      double dX = sX - toCalcNearest.xCoord;
      double dZ = sZ - toCalcNearest.zCoord;
      float middleYaw = (float)Math.toDegrees(Math.atan2(dZ, dX)) + 90.0f;
      float pitch = this.calcPitch(entitySelf.getPositionVector().addVector(0.0, (double)entitySelf.getEyeHeight(), 0.0), toCalcNearest);
      ArrayList<VecRotBuffer> buffers = new ArrayList<VecRotBuffer>();
      for (float yaw = middleYaw - (float)yawFilteringOffset; yaw < middleYaw + (float)yawFilteringOffset; yaw += maxYawStep) {
         Vec3d detachVec;
         Vector4d detach = this.getDetachPosForVirtPearl((EntityPlayer)entitySelf, yaw, pitch);
         if (detach == null || (detachVec = new Vec3d(detach.x, detach.y, detach.z)).distanceTo(toCalcNearest) > maxDistanceToNearestPos || detachVec.distanceTo(entitySelf.getPositionVector()) < minDistanceToSelf) continue;
         buffers.add(new VecRotBuffer(new float[]{yaw, pitch}, detachVec, (int)detach.w));
      }
      if (!buffers.isEmpty()) {
         if (buffers.size() > 1) {
            Vec3d finalToCalcNearest = toCalcNearest;
            Collections.sort(buffers, Comparator.comparingDouble(obj -> obj.getCalculatedFinal().distanceTo(finalToCalcNearest)));
         }
         if (buffers.get(0) != null) {
            return (VecRotBuffer)buffers.get(0);
         }
      }
      return null;
   }

   private List<EntityLivingBase> getCurrentTargets() {
      if (!this.tempListEntities.isEmpty()) {
         this.tempListEntities.clear();
      }

      if (HitAura.TARGET_ROTS != null && this.tempListEntities.stream().noneMatch(base -> base.getEntityId() == HitAura.TARGET_ROTS.getEntityId())) {
         this.tempListEntities.add(HitAura.TARGET_ROTS);
      }

      if (BowAimbot.target != null && this.tempListEntities.stream().noneMatch(base -> base.getEntityId() == BowAimbot.target.getEntityId())) {
         this.tempListEntities.add(BowAimbot.target);
      }

      for (EntityLivingBase cTarget : CrystalField.getTargets()) {
         if (cTarget != null && this.tempListEntities.stream().noneMatch(base -> base.getEntityId() == cTarget.getEntityId())) {
            this.tempListEntities.add(cTarget);
         }
      }

      EntityLivingBase thudTarget = TargetHUD.getTarget();
      if (thudTarget != null
         && thudTarget != Minecraft.player
         && this.tempListEntities.stream().noneMatch(base -> base.getEntityId() == thudTarget.getEntityId())) {
         this.tempListEntities.add(thudTarget);
      }

      if (this.AnyTargetRange.getFloat() != 0.0F) {
         for (EntityPlayer player : mc.world.playerEntities.stream().filter(playerx -> {
            if (playerx instanceof EntityOtherPlayerMP MP && Minecraft.player.getDistanceToEntity(MP) <= this.AnyTargetRange.getFloat()) {
               return true;
            }

            return false;
         }).collect(Collectors.toList())) {
            List<EntityLivingBase> tempEnts = this.tempListEntities.stream().filter(Objects::nonNull).toList();
            if (tempEnts.isEmpty() || tempEnts.stream().noneMatch(base -> base.getEntityId() == player.getEntityId())) {
               this.tempListEntities.add(player);
            }
         }
      }

      this.tempListEntities.removeIf(temp -> Client.friendManager.isFriend(temp.getName()) || !temp.isEntityAlive());
      return this.tempListEntities;
   }

   private void updateTempTargets(float maxMemoryTime, float pTicks, double pearlMinDistanceAtTarget) {
      List<EntityLivingBase> currents = this.getCurrentTargets();
      this.TIMED_TARGETS.removeIf(ThrowFollow.TimedTarget::isToRemove);

      for (EntityLivingBase current : currents) {
         ThrowFollow.TimedTarget timedTarget = this.TIMED_TARGETS.stream().filter(targetx -> targetx.entityID == current.getEntityId()).findAny().orElse(null);
         if (timedTarget != null) {
            timedTarget.resetTime();
         } else {
            this.TIMED_TARGETS.add(new ThrowFollow.TimedTarget(current, maxMemoryTime));
         }
      }

      if (!Minecraft.player.isElytraFlying() && !Fly.get.actived && !(MoveMeHelp.getSpeed() > 2.0) && !ElytraBoost.get.isActived()) {
         for (ThrowFollow.TimedTarget target : this.TIMED_TARGETS) {
            Vec3d targetVec = target.target.getPositionVector();

            for (EntityEnderPearl pearl : Objects.requireNonNull(target.getSingleTickPearls())) {
               Vector4d vecInfo = this.getPearlDetachPos(pearl, pTicks);
               if (vecInfo != null) {
                  Vec3d vec = new Vec3d(vecInfo.x, vecInfo.y, vecInfo.z);
                  if (vec.distanceTo(targetVec) >= pearlMinDistanceAtTarget) {
                     this.toThrowVectors.add(vec);
                  }

                  this.playTrajOpn();
                  pearl.check = true;
               }
            }
         }
      }
   }

   private int getPearlSlot() {
      if (Minecraft.player.getHeldItemOffhand().getItem() instanceof ItemEnderPearl) {
         return -2;
      } else {
         for (int slotNum = 0; slotNum <= (this.PearlFromInventory.getBool() ? 36 : 8); slotNum++) {
            ItemStack stack = Minecraft.player.inventory.getStackInSlot(slotNum);
            if (!stack.isEmpty() && stack.getItem() instanceof ItemEnderPearl) {
               return slotNum;
            }
         }

         return -1;
      }
   }

   private void switchAndThrow(int slot) {
      if (!Minecraft.player.getCooldownTracker().hasCooldown(Items.ENDER_PEARL)) {
         EnumHand hand = slot == -2 ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND;
         int handSlot = Minecraft.player.inventory.currentItem;
         boolean hasMain = hand == EnumHand.MAIN_HAND && handSlot != slot;
         boolean inInv = slot > 8;
         if (hasMain) {
            if (inInv) {
               mc.playerController.windowClick(0, slot, handSlot, ClickType.SWAP, Minecraft.player);
               mc.playerController.windowClickMemory(0, slot, handSlot, ClickType.SWAP, Minecraft.player, 100);
            } else {
               Minecraft.player.inventory.currentItem = slot;
               mc.playerController.updateController();
            }
         }

         mc.playerController.processRightClick(Minecraft.player, mc.world, hand);
         if (hasMain && !inInv) {
            Minecraft.player.inventory.currentItem = handSlot;
            mc.playerController.updateController();
         }
      }
   }

   private float[] getFixedRotation(float[] yaw$pitch) {
      return new float[]{RotationUtil.getFixedRotation(yaw$pitch[0]), RotationUtil.getFixedRotation(yaw$pitch[1])};
   }

   private void rotate(EventPlayerMotionUpdate event) {
      float yaw = this.currentRot[0];
      float pitch = this.currentRot[1];
      if (event != null) {
         event.setYaw(yaw);
         if (this.RotateMoveSide.getBool() && Minecraft.player.toCancelSprintTicks <= 1 && MathUtils.getDifferenceOf(Minecraft.player.rotationYaw, yaw) >= 45.0
            )
          {
            Minecraft.player.toCancelSprintTicks = 2;
         }

         event.setPitch(pitch);
      }

      HitAura.get.noRotateTick = true;
      RotationUtil.Yaw = yaw;
      RotationUtil.Pitch = pitch;
      HitAura.get.rotations = new float[]{yaw, pitch};
      Minecraft.player.rotationYawHead = yaw;
      Minecraft.player.rotationPitchHead = pitch;
   }

   public boolean isLeftClickMouseCanceled() {
      if (this.LClickFollow.getBool() && this.slot != -1) {
         Entity hover = MathUtils.getPointedEntity(new Vector2f(Minecraft.player.rotationYaw, Minecraft.player.rotationPitch), 70.0, 1.0F, false);
         if (hover != null
            && hover instanceof EntityOtherPlayerMP mp
            && mp.isEntityAlive()
            && !Client.friendManager.isFriend(mp.getName())
            && Minecraft.player.canEntityBeSeen(mp)
            && Minecraft.player.getDistanceToEntity(mp) > 6.0F) {
            this.toThrowVectors.add(mp.getPositionVector());
            this.playTrajNew();
            Minecraft.player.getSwing(this.slot == -2 ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND);
            return true;
         }
      }

      return false;
   }

   @Override
   public void onUpdate() {
      if (this.runTicks > 0) {
         this.runTicks--;
      }

      this.playTrajPopUpdater();
      if ((this.slot = this.getPearlSlot()) != -1) {
         if (this.runTicks == 1) {
            this.rotate(null);
            this.switchAndThrow(this.slot);
         }

         this.updateTempTargets(2500.0F, mc.getRenderPartialTicks(), 6.0);
         Vec3d toThrow = this.toThrowVectors.isEmpty() ? null : this.toThrowVectors.get(0);
         if (toThrow != null) {
            ThrowFollow.VecRotBuffer calc = this.getBufferForThrowPearl(toThrow, (double)this.MaxTpSpread.getFloat(), 5.0, 8, 0.03333333F);
            if (calc != null
               && calc.getRot() != null
               && calc.getCalculatedFinal() != null
               && this.runTicks == 0
               && Minecraft.player.getCooldownTracker().getCooldown(Items.ENDER_PEARL, 0.0F) <= 1.0F) {
               if (this.RenderMarkers.getBool()) {
                  this.addMarker(calc);
               }

               this.currentRot = this.getFixedRotation(calc.getRot());
               this.runTicks = 3;
               this.playTrajPop((long)((float)calc.getFlyingTicks() * 50.0F - 100.0F));
            }
         }

         if (!this.toThrowVectors.isEmpty()) {
            this.toThrowVectors.clear();
         }
      }
   }

   @EventTarget
   public void onEventUpdate(EventPlayerMotionUpdate event) {
      if (this.runTicks == 2) {
         this.rotate(event);
      }
   }

   @EventTarget
   public void onMovementInput(EventMovementInput event) {
      if (this.runTicks == 2 && this.RotateMoveSide.getBool()) {
         MoveMeHelp.fixDirMove(event, this.currentRot[0]);
      }
   }

   @EventTarget
   public void onSilentStrafe(EventRotationStrafe event) {
      if (this.runTicks == 2 && this.RotateMoveSide.getBool()) {
         event.setYaw(this.currentRot[0]);
      }
   }

   @EventTarget
   public void onSilentJump(EventRotationJump event) {
      if (this.runTicks == 2 && this.RotateMoveSide.getBool()) {
         event.setYaw(this.currentRot[0]);
      }
   }

   private void addMarker(ThrowFollow.VecRotBuffer buffer) {
      this.markers.add(new ThrowFollow.TimedVec3d(buffer.getCalculatedFinal(), (float)(buffer.getFlyingTicks() * 50 + 100)));
   }

   @Override
   public void onToggled(boolean actived) {
      this.markers.clear();
      this.toThrowVectors.clear();
      this.TIMED_TARGETS.clear();
      this.runTicks = 0;
      super.onToggled(actived);
   }

   private void drawMarker(ThrowFollow.TimedVec3d timedVec) {
      float effectScale = 1.0F;
      float timePC = timedVec.getTimePC();
      float alphaPC = 1.0F - timePC;
      alphaPC = (alphaPC > 0.5F ? 1.0F - alphaPC : alphaPC) * 2.0F;
      Vec3d vec = timedVec.getVec();
      float offPCRadius = (float)MathUtils.easeInOutQuad((double)MathUtils.clamp(timePC * 1.1F, 0.0F, 1.0F));
      float rPC = (float)MathUtils.easeInOutQuad((double)MathUtils.clamp(timePC * 3.0F, 0.0F, 1.0F));
      float radius1 = rPC;
      float radius2 = rPC * offPCRadius;
      float lineWidthMin = 1.5F;
      float lineWidthMax = 7.0F;
      int color1 = ColorUtils.getColor(255, 255, 255, 255.0F * alphaPC * rPC * offPCRadius);
      int color2 = ColorUtils.getColor(255, 255, 255, 255.0F * alphaPC);
      GL11.glPushMatrix();
      GL11.glTranslated(vec.xCoord, vec.yCoord, vec.zCoord);
      GL11.glRotated((double)(mc.getRenderManager().playerViewY + WorldRender.get.offYawOrient), 0.0, -1.0, 0.0);
      GL11.glRotated((double)(mc.getRenderManager().playerViewX + WorldRender.get.offPitchOrient), mc.gameSettings.thirdPersonView == 2 ? -1.0 : 1.0, 0.0, 0.0);
      GL11.glScalef(effectScale, effectScale, effectScale);
      double vecStep = 9.0;
      GL11.glLineWidth(lineWidthMin);
      RenderUtils.glColor(color1);
      GL11.glBegin(2);

      for (double i = 0.0; i < 360.0; i += 9.0) {
         double radian1 = Math.toRadians(i);
         double sin1 = Math.sin(radian1) * (double)radius1;
         double cos1 = -Math.cos(radian1) * (double)radius1;
         GL11.glVertex2d(sin1, cos1);
      }

      GL11.glEnd();
      GL11.glLineWidth(MathUtils.lerp(lineWidthMin, lineWidthMax, MathUtils.valWave01(offPCRadius)));
      RenderUtils.glColor(color2);
      GL11.glBegin(2);

      for (double i = 0.0; i < 360.0; i += 9.0) {
         double radian1 = Math.toRadians(i);
         double sin1 = Math.sin(radian1) * (double)radius2;
         double cos1 = -Math.cos(radian1) * (double)radius2;
         GL11.glVertex2d(sin1, cos1);
      }

      GL11.glEnd();
      GL11.glLineWidth(1.0F);
      GL11.glPopMatrix();
   }

   @EventTarget
   public void onRender3d(Event3D event) {
      if (!this.markers.isEmpty()) {
         RenderUtils.setup3dForBlockPos(() -> {
            GL11.glDisable(3008);
            GL11.glEnable(2848);
            GL11.glHint(3154, 4354);
            this.markers.forEach(mark -> this.drawMarker(mark));
            GL11.glHint(3154, 4352);
            GL11.glDisable(2848);
            GL11.glEnable(3008);
         }, false);
      }

      this.markers.removeIf(ThrowFollow.TimedVec3d::isToRemove);
   }

   private class TimedTarget {
      EntityLivingBase target;
      int entityID;
      long initTime = System.currentTimeMillis();
      float maxMemoryTime;

      public TimedTarget(EntityLivingBase target, float maxMemoryTime) {
         this.target = target;
         if (target != null) {
            this.entityID = target.getEntityId();
         }

         this.maxMemoryTime = maxMemoryTime;
      }

      public EntityLivingBase getEntity() {
         return this.target;
      }

      public void resetTime() {
         this.initTime = System.currentTimeMillis();
      }

      public boolean isToRemove() {
         return this.getEntity() == null || (float)(System.currentTimeMillis() - this.initTime) >= this.maxMemoryTime;
      }

      public List<EntityEnderPearl> getSingleTickPearls() {
         return Module.mc
            .world
            .getLoadedEntityList()
            .stream()
            .map(entity -> entity instanceof EntityEnderPearl ? (EntityEnderPearl)entity : null)
            .filter(Objects::nonNull)
            .filter(pearl -> !pearl.check && pearl.parsedThrower != null && pearl.parsedThrower.getEntityId() == this.entityID)
            .collect(Collectors.toList());
      }
   }

   private class TimedVec3d {
      TimerHelper timer = TimerHelper.TimerHelperReseted();
      float maxTime;
      Vec3d vec;

      public TimedVec3d(Vec3d vec, float maxTime) {
         this.vec = vec;
         this.maxTime = maxTime;
      }

      public float getTimePC() {
         float timePC = (float)this.timer.getTime() / this.maxTime;
         return timePC > 1.0F ? 1.0F : timePC;
      }

      public boolean isToRemove() {
         return this.getTimePC() == 1.0F;
      }

      public Vec3d getVec() {
         return this.vec;
      }
   }

   private class VecRotBuffer {
      private float[] rot;
      private final Vec3d calculatedFinal;
      private int flyingTicks;

      public float[] getRot() {
         return this.rot;
      }

      public void setRot(float[] rot) {
         this.rot = rot;
      }

      public Vec3d getCalculatedFinal() {
         return this.calculatedFinal;
      }

      public int getFlyingTicks() {
         return this.flyingTicks;
      }

      public void setFlyingTicks(int flyingTicks) {
         this.flyingTicks = flyingTicks;
      }

      public VecRotBuffer(float[] rot, Vec3d calculatedFinal, int flyingTicks) {
         this.rot = rot;
         this.calculatedFinal = calculatedFinal;
         this.flyingTicks = flyingTicks;
      }
   }
}
