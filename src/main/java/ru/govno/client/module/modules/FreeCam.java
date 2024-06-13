package ru.govno.client.module.modules;

import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.gui.GuiDownloadTerrain;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.network.play.client.CPacketConfirmTeleport;
import net.minecraft.network.play.client.CPacketConfirmTransaction;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.server.SPacketConfirmTransaction;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;
import ru.govno.client.Client;
import ru.govno.client.event.EventTarget;
import ru.govno.client.event.events.Event3D;
import ru.govno.client.event.events.EventMove2;
import ru.govno.client.event.events.EventPlayerMotionUpdate;
import ru.govno.client.event.events.EventReceivePacket;
import ru.govno.client.event.events.EventSendPacket;
import ru.govno.client.module.Module;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.module.settings.FloatSettings;
import ru.govno.client.module.settings.ModeSettings;
import ru.govno.client.newfont.CFontRenderer;
import ru.govno.client.newfont.Fonts;
import ru.govno.client.utils.InventoryUtil;
import ru.govno.client.utils.Math.MathUtils;
import ru.govno.client.utils.Movement.MoveMeHelp;
import ru.govno.client.utils.Render.ColorUtils;
import ru.govno.client.utils.Render.RenderUtils;

public class FreeCam extends Module {
   public static Module get;
   private float yaw;
   private float pitch;
   private float yawHead;
   private float gamma;
   private EntityPlayer other;
   private float old;
   public static EntityPlayer fakePlayer = null;
   private double oldX;
   private double oldY;
   private double oldZ;
   private double newX;
   private double newY;
   private double newZ;
   private double oldYaw;
   private double oldPitch;
   private boolean isSneaking = false;
   FloatSettings SpeedI;
   ModeSettings PortMode;
   BoolSettings PosRender;
   BoolSettings LiquidPort;
   BoolSettings NoFlightKick;
   BoolSettings NoDessaturate;
   public static String coords = "";
   float scale = 0.0F;
   float lqExtend = 0.0F;
   float scaledAlpha = 0.0F;

   public FreeCam() {
      super("FreeCam", 0, Module.Category.PLAYER);
      get = this;
      this.settings.add(this.SpeedI = new FloatSettings("Speed", 0.5F, 1.0F, 0.1F, this));
      this.settings.add(this.PosRender = new BoolSettings("PosRender", true, this));
      this.settings.add(this.LiquidPort = new BoolSettings("LiquidPort", true, this));
      this.settings.add(this.PortMode = new ModeSettings("PortMode", "Matrix", this, new String[]{"Vanilla", "Matrix"}, () -> this.LiquidPort.getBool()));
      this.settings.add(this.NoFlightKick = new BoolSettings("NoFlightKick", true, this));
      this.settings.add(this.NoDessaturate = new BoolSettings("NoDessaturate", false, this));
   }

   public static void matrixTp(double x, double y, double z, boolean canElytra) {
      int de = (int)MathUtils.clamp(Minecraft.player.getDistance(x, y, z) / 11.0, 1.0, 17.0);
      int de2 = (int)(Math.abs(y / 11.0) + Math.abs(Minecraft.player.getDistance(x, Minecraft.player.posY, z) / 2.5));
      boolean elytraEquiped = Minecraft.player.inventory.armorInventory.get(2).getItem() == Items.ELYTRA;
      if (canElytra) {
         for (int i = 0; i < MathUtils.clamp(de2, 1, 17); i++) {
            Minecraft.player.connection.sendPacket(new CPacketPlayer(false));
         }

         if (elytraEquiped) {
            Minecraft.player.connection.sendPacket(new CPacketPlayer.Position(Minecraft.player.posX, Minecraft.player.posY, Minecraft.player.posZ, false));
            Minecraft.player.connection.sendPacket(new CPacketPlayer.Position(Minecraft.player.posX, Minecraft.player.posY, Minecraft.player.posZ, false));
            Minecraft.player.connection.sendPacket(new CPacketEntityAction(Minecraft.player, CPacketEntityAction.Action.START_FALL_FLYING));
            Minecraft.player.connection.sendPacket(new CPacketPlayer.Position(x, y, z, false));
            Minecraft.player.connection.sendPacket(new CPacketEntityAction(Minecraft.player, CPacketEntityAction.Action.START_FALL_FLYING));
         } else {
            int elytra = InventoryUtil.getElytra();
            if (elytra != -1) {
               mc.playerController.windowClick(0, elytra < 9 ? elytra + 36 : elytra, 1, ClickType.PICKUP, Minecraft.player);
               mc.playerController.windowClick(0, 6, 1, ClickType.PICKUP, Minecraft.player);
            }

            Minecraft.player.connection.sendPacket(new CPacketPlayer.Position(Minecraft.player.posX, Minecraft.player.posY, Minecraft.player.posZ, false));
            Minecraft.player.connection.sendPacket(new CPacketPlayer.Position(Minecraft.player.posX, Minecraft.player.posY, Minecraft.player.posZ, false));
            Minecraft.player.connection.sendPacket(new CPacketEntityAction(Minecraft.player, CPacketEntityAction.Action.START_FALL_FLYING));
            Minecraft.player.connection.sendPacket(new CPacketPlayer.Position(x, y, z, false));
            Minecraft.player.connection.sendPacket(new CPacketEntityAction(Minecraft.player, CPacketEntityAction.Action.START_FALL_FLYING));
            if (elytra != -1) {
               mc.playerController.windowClick(0, 6, 1, ClickType.PICKUP, Minecraft.player);
               mc.playerController.windowClick(0, elytra < 9 ? elytra + 36 : elytra, 1, ClickType.PICKUP, Minecraft.player);
            }
         }

         Minecraft.player.setPositionAndUpdate(x, y, z);
      } else {
         for (int i = 0; i < MathUtils.clamp(de2, 0, 19); i++) {
            Minecraft.player.connection.sendPacket(new CPacketPlayer(false));
         }

         Minecraft.player.connection.sendPacket(new CPacketPlayer.Position(x, y, z, false));
         Minecraft.player.setPositionAndUpdate(x, y, z);
      }
   }

   void port(double x, double y, double z, String mode) {
      if (mode.equalsIgnoreCase("Vanilla")) {
         Minecraft.player.setPositionAndUpdate(this.oldX, this.oldY, this.oldZ);
      } else {
         matrixTp(x, y, z, InventoryUtil.getElytra() != -1);
      }
   }

   private void toggleFakePlayer(boolean spawn) {
      if (mc.world != null) {
         if (spawn) {
            fakePlayer = new EntityOtherPlayerMP(
               mc.world, new GameProfile(UUID.fromString("70ee432d-0a96-4137-a2c0-37cc9df67f03"), "§6" + Minecraft.player.getName() + "§f > §cNPC§r")
            );
            fakePlayer.inventory.currentItem = Minecraft.player.inventory.currentItem;
            fakePlayer.setPosition(Minecraft.player.posX, Minecraft.player.posY, Minecraft.player.posZ);
            fakePlayer.rotationYaw = Minecraft.player.rotationYaw;
            fakePlayer.rotationPitch = Minecraft.player.rotationPitch;
            fakePlayer.rotationYawHead = Minecraft.player.rotationYawHead;
            fakePlayer.rotationPitchHead = Minecraft.player.rotationPitchHead;
            fakePlayer.renderYawOffset = fakePlayer.rotationYaw;
            BlockPos pt = new BlockPos(fakePlayer.posX, fakePlayer.posY - 0.9999, fakePlayer.posZ);
            fakePlayer.onGround = mc.world.getBlockState(pt) != null && mc.world.getBlockState(pt).getCollisionBoundingBox(mc.world, pt) != null;
            fakePlayer.fallDistance = Minecraft.player.fallDistance;
            mc.world.addEntityToWorld(462462999, fakePlayer);
         } else {
            if (fakePlayer != null) {
               mc.world.removeEntityFromWorld(462462999);
            }

            fakePlayer = null;
         }
      }
   }

   @Override
   public void onToggled(boolean actived) {
      ResourceLocation shader = new ResourceLocation("shaders/post/desaturate.json");
      if (actived) {
         if (!this.NoDessaturate.getBool()) {
            mc.entityRenderer.loadShader(shader);
         }

         this.isSneaking = Minecraft.player.isSneaking();
         Minecraft.player.motionY = 0.0;
         Minecraft.player.setNoGravity(true);
         this.oldX = Minecraft.player.posX;
         this.oldY = Minecraft.player.posY;
         this.oldZ = Minecraft.player.posZ;
         this.oldYaw = (double)Minecraft.player.rotationYaw;
         this.oldPitch = (double)Minecraft.player.rotationPitch;
         this.toggleFakePlayer(true);
      } else {
         if (mc.entityRenderer.isShaderActive()) {
            mc.entityRenderer.theShaderGroup = null;
         }

         Minecraft.player.setNoGravity(false);
         Minecraft.player.motionY = -0.0031F;
         Minecraft.player.jumpMovementFactor = 0.02F;
         MoveMeHelp.setSpeed(0.0);
         MoveMeHelp.setCuttingSpeed(MoveMeHelp.getSpeed() / 1.06F);
         if (this.LiquidPort.getBool()) {
            try {
               if (this.doTeleport()) {
                  this.port(this.newX, this.newY, this.newZ, this.PortMode.getMode());
               } else {
                  Minecraft.player.setPositionAndUpdate(this.oldX, this.oldY, this.oldZ);
                  Minecraft.player.rotationYaw = (float)this.oldYaw;
                  Minecraft.player.rotationPitch = (float)this.oldPitch;
                  mc.renderGlobal.loadRenderers();
               }
            } catch (Exception var4) {
               Client.msg("Something went wrong.", true);
               System.out.println(this.name + " Something went wrong.");
            }
         } else {
            Minecraft.player.setPositionAndUpdate(this.oldX, this.oldY, this.oldZ);
         }

         this.toggleFakePlayer(false);
      }

      super.onToggled(actived);
   }

   @EventTarget
   public void onMotionUpdate(EventPlayerMotionUpdate motionUpdate) {
      if (Minecraft.player != null && mc.world != null && !Minecraft.player.isDead && this.actived && Minecraft.player.ticksExisted > 4 && fakePlayer != null) {
         float prevRPH = Minecraft.player.rotationPitchHead;
         motionUpdate.setYaw(fakePlayer.rotationYaw);
         motionUpdate.setPitch(fakePlayer.rotationPitch);
         motionUpdate.setX(fakePlayer.posX);
         motionUpdate.setY(fakePlayer.posY);
         motionUpdate.setZ(fakePlayer.posZ);
         motionUpdate.setGround(fakePlayer.onGround);
         Minecraft.player.rotationPitchHead = prevRPH;
      }
   }

   @EventTarget
   public void onPacket(EventSendPacket event) {
      if (Minecraft.player != null
         && mc.world != null
         && !Minecraft.player.isDead
         && this.actived
         && Minecraft.player.ticksExisted > 4
         && this.NoFlightKick.getBool()
         && (
            event.getPacket() instanceof CPacketPlayer.Position
               || event.getPacket() instanceof CPacketPlayer.Rotation
               || event.getPacket() instanceof CPacketConfirmTransaction
               || event.getPacket() instanceof CPacketPlayer.Position
               || event.getPacket() instanceof CPacketPlayer.PositionRotation
               || event.getPacket() instanceof CPacketEntityAction
               || event.getPacket() instanceof CPacketConfirmTeleport
         )) {
         event.setCancelled(true);
      }
   }

   @EventTarget
   public void onPacket(EventReceivePacket event) {
      if (Minecraft.player != null
         && mc.world != null
         && !Minecraft.player.isDead
         && this.actived
         && Minecraft.player.ticksExisted > 4
         && (event.getPacket() instanceof SPacketConfirmTransaction || event.getPacket() instanceof SPacketPlayerPosLook)) {
         if (event.getPacket() instanceof SPacketPlayerPosLook look
            && fakePlayer != null
            && fakePlayer.getDistanceToVec3d(new Vec3d(look.getX(), look.getY(), look.getZ())) > 0.2) {
            fakePlayer.rotationYaw = look.getYaw();
            fakePlayer.rotationYaw = look.getPitch();
            fakePlayer.setPosition(look.getX(), look.getY(), look.getZ());
         }

         event.setCancelled(true);
      }
   }

   @Override
   public void onUpdate() {
      Minecraft.player.isInWeb = false;
      if (fakePlayer != null) {
         fakePlayer.setHealth(Minecraft.player.getHealth());
         fakePlayer.setAbsorptionAmount(Minecraft.player.getAbsorptionAmount());
         fakePlayer.entityCollisionReduction = 1.0F;
         fakePlayer.hurtTime = Minecraft.player.hurtTime;
         fakePlayer.setPrimaryHand(Minecraft.player.getPrimaryHand());
         fakePlayer.openContainer = Minecraft.player.openContainer;
         if (Minecraft.player.getActiveHand() != null && Minecraft.player.isHandActive()) {
            fakePlayer.setActiveHand(Minecraft.player.getActiveHand());
         } else {
            fakePlayer.resetActiveHand();
         }

         fakePlayer.setBurning(Minecraft.player.isBurning());
         fakePlayer.inventory = Minecraft.player.inventory;
         fakePlayer.isSwingInProgress = Minecraft.player.isSwingInProgress;
         fakePlayer.swingingHand = Minecraft.player.swingingHand;
         fakePlayer.setSneaking(this.isSneaking);
      }

      Minecraft.player.noClip = true;
      this.newX = Minecraft.player.posX;
      this.newY = Minecraft.player.posY;
      this.newZ = Minecraft.player.posZ;
      float totalSpeed = this.SpeedI.getFloat() * 2.0F;
      double motion = Entity.Getmotiony;
      if (Minecraft.player.isJumping()) {
         motion += 1.5 * (double)totalSpeed;
      }

      if (Minecraft.player.isSneaking()) {
         motion -= 1.5 * (double)totalSpeed;
      }

      Minecraft.player.motionY += motion;
      Minecraft.player.motionY = MathUtils.clamp(Minecraft.player.motionY / 3.0, (double)(-totalSpeed), (double)totalSpeed);
      double speed2 = MathUtils.clamp(
         Math.sqrt(Entity.Getmotionx * Entity.Getmotionx + Entity.Getmotionz * Entity.Getmotionz) * 1.4,
         (double)(0.5F * totalSpeed),
         (double)(1.5F * totalSpeed)
      );
      MoveMeHelp.setSpeed(speed2, 0.8F);
   }

   @Override
   public String getDisplayName() {
      return this.getDisplayByDouble((double)this.SpeedI.getFloat());
   }

   @EventTarget
   public void onMove(EventMove2 move) {
      if (this.actived) {
         if (MoveMeHelp.isMoving()) {
            move.ignoreHorizontal = true;
         }

         if (Minecraft.player.isJumping() || Minecraft.player.isSneaking()) {
            move.ignoreVertical = true;
         }
      }
   }

   @EventTarget
   public void onEvent3D(Event3D event) {
      if (this.actived && this.PosRender.getBool() && (double)this.scale > 0.9 && fakePlayer != null) {
         Entity entity = fakePlayer;
         if (entity == null) {
            return;
         }

         boolean old = mc.gameSettings.viewBobbing;
         mc.gameSettings.viewBobbing = false;
         mc.entityRenderer.setupCameraTransform(event.getPartialTicks(), 0);
         mc.gameSettings.viewBobbing = old;
         mc.entityRenderer.disableLightmap();
         GL11.glBlendFunc(770, 771);
         GL11.glEnable(3042);
         GL11.glLineWidth(0.01F);
         GL11.glEnable(2848);
         GlStateManager.shadeModel(7425);
         GL11.glDisable(3553);
         GL11.glDisable(2929);
         GL11.glDepthMask(false);
         mc.entityRenderer.disableLightmap();
         float py = entity.height;

         assert mc.getRenderViewEntity() != null;

         Vec3d vec3d = new Vec3d(0.0, 0.0, 1.0);
         vec3d = vec3d.rotatePitch(-((float)Math.toRadians((double)Minecraft.player.rotationPitch)));
         Vec3d vec3d2 = vec3d.rotateYaw(-((float)Math.toRadians((double)Minecraft.player.rotationYaw)));
         RenderUtils.setupColor(ColorUtils.getColor(168, 62, 62), MathUtils.clamp(Minecraft.player.getDistanceToEntity(entity) * 3.0F, 26.0F, 255.0F));
         GL11.glLineWidth(0.001F);
         GL11.glBegin(1);
         GL11.glVertex3d(vec3d2.xCoord, (double)Minecraft.player.getEyeHeight() + vec3d2.yCoord, vec3d2.zCoord);
         GL11.glVertex3d(this.oldX - RenderManager.viewerPosX, this.oldY - RenderManager.viewerPosY, this.oldZ - RenderManager.viewerPosZ);
         GL11.glEnd();
         GlStateManager.resetColor();
         GL11.glEnable(3553);
         GL11.glEnable(2929);
         GL11.glDepthMask(true);
         GlStateManager.shadeModel(7424);
         GlStateManager.resetColor();
      }
   }

   boolean doTeleport() {
      return this.scale > 0.0F && this.LiquidPort.getBool() && !this.isInsideBlock()
         ? Minecraft.player.isInWater()
            || Minecraft.player.isInLava()
            || Minecraft.player.isInWeb
            || mc.world.getBlockState(new BlockPos(this.oldX, this.oldY, this.oldZ)).getBlock() == Blocks.WATER
            || mc.world.getBlockState(new BlockPos(this.oldX, this.oldY, this.oldZ)).getBlock() == Blocks.LAVA
            || mc.world.getBlockState(new BlockPos(this.oldX, this.oldY, this.oldZ)).getBlock() == Blocks.WEB
         : false;
   }

   boolean isInsideBlock() {
      double x = Minecraft.player.posX;
      double y = Minecraft.player.posY;
      double z = Minecraft.player.posZ;

      for (float i = 0.0F; (double)i < (Minecraft.player.isSneaking() ? 1.6 : 1.8); i += 0.2F) {
         if (Speed.posBlock(x, y + (double)i, z)
            || Speed.posBlock(x + 0.275F, y + (double)i, z + 0.275F)
            || Speed.posBlock(x - 0.275F, y + (double)i, z - 0.275F)
            || Speed.posBlock(x + 0.275F, y + (double)i, z)
            || Speed.posBlock(x - 0.275F, y + (double)i, z)
            || Speed.posBlock(x, y + (double)i, z + 0.275F)
            || Speed.posBlock(x, y + (double)i, z - 0.275F)
            || Speed.posBlock(x + 0.275F, y + (double)i, z - 0.275F)
            || Speed.posBlock(x - 0.275F, y + (double)i, z + 0.275F)) {
            return true;
         }
      }

      return false;
   }

   @Override
   public void alwaysRender2D(ScaledResolution sr) {
      if (this.actived && mc.currentScreen != null && mc.currentScreen instanceof GuiDownloadTerrain) {
         Minecraft.player.closeScreen();
      }

      if (this.actived) {
         if (!this.NoDessaturate.getBool()) {
            if (mc.entityRenderer.theShaderGroup == null || !mc.entityRenderer.theShaderGroup.getShaderGroupName().contains("desaturate")) {
               mc.entityRenderer.loadShader(new ResourceLocation("shaders/post/desaturate.json"));
            }
         } else if (mc.entityRenderer.theShaderGroup != null && mc.entityRenderer.theShaderGroup.getShaderGroupName().contains("desaturate")) {
            mc.entityRenderer.theShaderGroup = null;
         }
      }

      if (this.actived && this.scale != 1.0F || !this.actived && this.scale != 0.0F) {
         this.scale = MathUtils.harp(this.scale, this.actived ? 1.0F : 0.0F, (float)Minecraft.frameTime * 0.0075F);
      }

      boolean PosRender = this.currentBooleanValue("PosRender") && this.scale != 0.0F;
      if (this.doTeleport() && this.lqExtend != 1.0F || !this.doTeleport() && this.lqExtend != 0.0F || !this.actived && this.lqExtend != 0.0F) {
         this.lqExtend = MathUtils.harp(this.lqExtend, this.actived && this.doTeleport() ? 1.0F : 0.0F, (float)Minecraft.frameTime * 0.01F);
      }

      if (this.lqExtend == 1.0F && this.scaledAlpha != 1.0F || this.lqExtend != 1.0F && this.scaledAlpha != 0.0F || !this.actived && this.scaledAlpha != 0.0F) {
         this.scaledAlpha = MathUtils.harp(
            this.lqExtend, this.actived && this.doTeleport() && this.scaledAlpha == 1.0F ? 1.0F : 0.0F, (float)Minecraft.frameTime * 2.5E-4F
         );
      }

      if (PosRender) {
         coords = "Ydiff: §c"
            + Math.round(Minecraft.player.posY - this.oldY)
            + "§r DistXZ: §c"
            + Math.round(Minecraft.player.getSmoothDistanceToEntityXZ(fakePlayer))
            + "§r";
         String lqTp = "Port on disable";
         CFontRenderer font = Fonts.noise_14;
         float extendX = 2.0F;
         float extendY = 1.0F;
         float w = (float)font.getStringWidth(coords) + extendX;
         float h = (float)font.getHeight();
         float x = (float)(sr.getScaledWidth() / 2) - w / 2.0F;
         float x2 = (float)(sr.getScaledWidth() / 2) + w / 2.0F;
         float y = (float)(sr.getScaledHeight() / 2) + h * this.scale * 3.0F - extendY;
         float y2 = (float)(sr.getScaledHeight() / 2) + h * this.scale * 4.0F + extendY;
         float[] MxMy = Crosshair.get.crossPosMotions;
         x += MxMy[0];
         y += MxMy[1];
         x2 += MxMy[0];
         y2 += MxMy[1];
         float extLine = 1.5F;
         int bgColor = ColorUtils.swapAlpha(Integer.MAX_VALUE, (float)ColorUtils.getAlphaFromColor(Integer.MAX_VALUE) * this.scale);
         int bColor = ColorUtils.getColor(168, 62, 62, 255.0F * this.scale);
         GlStateManager.pushMatrix();
         RenderUtils.customScaledObject2D(x, y + 20.0F, x2 - x, 1.0F, this.scale);
         RenderUtils.drawBloomedFullShadowFullGradientRectBool(
            x - extLine, y, x2 + extLine, y2 + (h + extendY * 1.5F) * this.lqExtend, 3.0F, bgColor, bgColor, bgColor, bgColor, 70, 20, true, true, true
         );
         RenderUtils.resetBlender();
         RenderUtils.drawAlphedRect((double)(x - extLine), (double)y, (double)x, (double)(y2 + (h + extendY * 1.5F) * this.lqExtend), bColor);
         RenderUtils.drawAlphedRect((double)x2, (double)y, (double)(x2 + extLine), (double)(y2 + (h + extendY * 1.5F) * this.lqExtend), bColor);
         RenderUtils.drawAlphedRect(
            (double)(x + (float)font.getStringWidth("Ydiff: ") + 0.5F),
            (double)y,
            (double)(x + (float)font.getStringWidth("Ydiff: ") + (float)font.getStringWidth(Math.round(Minecraft.player.posY - this.oldY) + "") + 2.5F),
            (double)y2,
            Integer.MIN_VALUE
         );
         RenderUtils.drawAlphedRect(
            (double)(
               x
                  + (float)font.getStringWidth(coords)
                  - (float)font.getStringWidth(Math.round(Minecraft.player.getSmoothDistanceToEntityXZ(fakePlayer)) + "")
                  + 0.5F
            ),
            (double)y,
            (double)(x + (float)font.getStringWidth(coords) + 2.0F),
            (double)y2,
            Integer.MIN_VALUE
         );
         font.drawString(coords, (double)(x + extendX / 2.0F), (double)(y + extendY * 2.0F), bgColor);
         if (this.scaledAlpha != 0.0F && this.scaledAlpha * 255.0F >= 26.0F) {
            font.drawString(
               lqTp,
               (double)(x + extendX / 2.0F),
               (double)(y2 + (h + extendY * 1.5F) * this.lqExtend - h),
               ColorUtils.swapAlpha(Integer.MIN_VALUE, (float)ColorUtils.getAlphaFromColor(Integer.MIN_VALUE) * this.scaledAlpha * this.scale)
            );
         }

         GlStateManager.enableAlpha();
         GlStateManager.enableBlend();
         GlStateManager.popMatrix();
      }
   }
}
