package ru.govno.client.module.modules;

import java.util.Objects;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketConfirmTeleport;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.MovementInput;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;
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
import ru.govno.client.utils.InventoryUtil;
import ru.govno.client.utils.TimerUtils;
import ru.govno.client.utils.Command.impl.Clip;
import ru.govno.client.utils.Math.BlockUtils;
import ru.govno.client.utils.Math.MathUtils;
import ru.govno.client.utils.Movement.MoveMeHelp;
import ru.govno.client.utils.Render.AnimationUtils;
import ru.govno.client.utils.Render.ColorUtils;
import ru.govno.client.utils.Render.RenderUtils;

public class NoClip extends Module {
   public static NoClip get;
   public ModeSettings Mode;
   public BoolSettings DoorPhase;
   public BoolSettings MatrixLift;
   public BoolSettings MatrixTP;
   public BoolSettings TimerBoost;
   public BoolSettings YMoveBrains;
   public FloatSettings TPRange;
   public FloatSettings SpeedY;
   public FloatSettings SpeedF;
   public FloatSettings Boost;
   boolean flag = false;
   float colorEx = 0.0F;
   TimerUtils timer = new TimerUtils();
   boolean tickGo = false;
   float ticker = 0.0F;
   AnimationUtils animSHKALE = new AnimationUtils(0.0F, 0.0F, 0.08F);

   public NoClip() {
      super("NoClip", 0, Module.Category.MOVEMENT);
      get = this;
      this.settings
         .add(this.Mode = new ModeSettings("Mode", "Vanilla", this, new String[]{"Vanilla", "Packet", "Packet2", "Matrix", "Sunrise", "Reallyworld"}));
      this.settings
         .add(
            this.DoorPhase = new BoolSettings(
               "DoorPhase", true, this, () -> this.Mode.currentMode.equalsIgnoreCase("Vanilla") || this.Mode.currentMode.equalsIgnoreCase("Matrix")
            )
         );
      this.settings
         .add(
            this.MatrixLift = new BoolSettings(
               "MatrixLift", false, this, () -> this.Mode.currentMode.equalsIgnoreCase("Vanilla") || this.Mode.currentMode.equalsIgnoreCase("Matrix")
            )
         );
      this.settings
         .add(
            this.MatrixTP = new BoolSettings(
               "MatrixTP",
               true,
               this,
               () -> this.Mode.currentMode.equalsIgnoreCase("Vanilla")
                     || this.Mode.currentMode.equalsIgnoreCase("Matrix")
                     || this.Mode.currentMode.equalsIgnoreCase("Sunrise")
            )
         );
      this.settings
         .add(
            this.TPRange = new FloatSettings(
               "TPRange",
               100.0F,
               300.0F,
               0.0F,
               this,
               () -> (
                        this.Mode.currentMode.equalsIgnoreCase("Vanilla")
                           || this.Mode.currentMode.equalsIgnoreCase("Matrix")
                           || this.Mode.currentMode.equalsIgnoreCase("Sunrise")
                     )
                     && this.MatrixTP.getBool()
            )
         );
      this.settings
         .add(
            this.SpeedY = new FloatSettings(
               "SpeedY", 0.2F, 1.0F, 0.0F, this, () -> this.Mode.currentMode.equalsIgnoreCase("Vanilla") || this.Mode.currentMode.equalsIgnoreCase("Matrix")
            )
         );
      this.settings
         .add(
            this.SpeedF = new FloatSettings(
               "Speed", 1.0F, 1.0F, 0.01F, this, () -> this.Mode.currentMode.equalsIgnoreCase("Vanilla") || this.Mode.currentMode.equalsIgnoreCase("Matrix")
            )
         );
      this.settings
         .add(
            this.TimerBoost = new BoolSettings(
               "TimerBoost",
               true,
               this,
               () -> !this.Mode.currentMode.equalsIgnoreCase("Reallyworld")
                     && !this.Mode.currentMode.equalsIgnoreCase("Packet")
                     && !this.Mode.currentMode.equalsIgnoreCase("Packet2")
            )
         );
      this.settings
         .add(
            this.Boost = new FloatSettings(
               "Boost",
               0.4F,
               1.0F,
               0.0F,
               this,
               () -> !this.Mode.currentMode.equalsIgnoreCase("Reallyworld")
                     && !this.Mode.currentMode.equalsIgnoreCase("Packet")
                     && !this.Mode.currentMode.equalsIgnoreCase("Packet2")
                     && this.TimerBoost.getBool()
            )
         );
      this.settings.add(this.YMoveBrains = new BoolSettings("YMoveBrains", true, this, () -> this.Mode.currentMode.equalsIgnoreCase("Sunrise")));
   }

   @EventTarget
   public void onMove(EventMove2 move) {
      if (this.actived && this.Mode.currentMode.equalsIgnoreCase("Reallyworld") && this.tickGo) {
         if (Minecraft.player.isCollidedHorizontally) {
            move.setIgnoreHorizontalCollision();
         }

         if (Minecraft.player.isSneaking()) {
            move.setIgnoreVerticalCollision();
         }
      }

      if (!this.MatrixLift.getBool()
         || !isNoClip(Minecraft.player)
         || this.Mode.currentMode.equalsIgnoreCase("Sunrise")
         || this.Mode.currentMode.equalsIgnoreCase("Packet")
         || this.Mode.currentMode.equalsIgnoreCase("Packet2")
         || Minecraft.player.isInWater()
         || mc.world.getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY - 0.5, Minecraft.player.posZ)).getBlock() == Blocks.WATER
         || !Minecraft.player.isJumping()) {
         if (this.actived
            && (
               this.Mode.currentMode.equalsIgnoreCase("Sunrise")
                  || this.Mode.currentMode.equalsIgnoreCase("Matrix")
                  || this.Mode.currentMode.equalsIgnoreCase("Vanilla")
            )) {
            double x = Minecraft.player.posX;
            double y = Minecraft.player.posY;
            double z = Minecraft.player.posZ;
            boolean isInBlock = false;
            float w = Minecraft.player.width / 2.0F - 0.05F;

            for (float i = 0.0F; (double)i < (Minecraft.player.isSneaking() ? 1.6 : 1.8); i++) {
               if (posBlock(x, y + (double)i, z)
                  || posBlock(x + (double)w, y + (double)i, z + (double)w)
                  || posBlock(x - (double)w, y + (double)i, z - (double)w)
                  || posBlock(x + (double)w, y + (double)i, z)
                  || posBlock(x - (double)w, y + (double)i, z)
                  || posBlock(x, y + (double)i, z + (double)w)
                  || posBlock(x, y + (double)i, z - (double)w)
                  || posBlock(x + (double)w, y + (double)i, z - (double)w)
                  || posBlock(x - (double)w, y + (double)i, z + (double)w)) {
                  boolean smartYHops = this.YMoveBrains.getBool();
                  boolean stabbleCheckFlot = move.toGround() && (double)Minecraft.player.fallDistance < 0.3 && Minecraft.player.isJumping()
                     || MathUtils.getDifferenceOf(Minecraft.player.motionY, 0.42) < 0.01;
                  boolean stabbleCheckUpWard = !move.toGround();
                  boolean stabbleCheckDownWard = !move.toGround() || (double)Minecraft.player.fallDistance < 1.5 && Minecraft.player.isJumping();
                  float rotY = Minecraft.player.rotationPitch;
                  boolean yCancel = (
                        rotY > 65.0F ? stabbleCheckDownWard : (!(rotY < 5.0F) && MoveMeHelp.moveKeysPressed() ? stabbleCheckFlot : stabbleCheckUpWard)
                     )
                     || Minecraft.player.isSneaking() && (double)Minecraft.player.fallDistance < 3.5 - Math.abs(Entity.Getmotiony);
                  if ((!move.toGround() && Minecraft.player.motionY > 0.0 && Minecraft.player.isJumping() || Minecraft.player.isSneaking()) && !smartYHops
                     || smartYHops && yCancel) {
                     move.setIgnoreVerticalCollision();
                  }

                  move.setIgnoreHorizontalCollision();
               }
            }
         }
      }
   }

   public float blockBreakSpeed(IBlockState blockMaterial, ItemStack tool) {
      float mineSpeed = tool.getStrVsBlock(blockMaterial);
      int efficiencyFactor = EnchantmentHelper.getEnchantmentLevel(Enchantments.EFFICIENCY, tool);
      mineSpeed = (float)(
         (double)mineSpeed > 1.0 && efficiencyFactor > 0 ? (double)((float)(efficiencyFactor * efficiencyFactor) + mineSpeed) + 1.0 : (double)mineSpeed
      );
      if (Minecraft.player.getActivePotionEffect(MobEffects.HASTE) != null) {
         mineSpeed *= 1.0F + (float)Objects.requireNonNull(Minecraft.player.getActivePotionEffect(MobEffects.HASTE)).getAmplifier() * 0.2F;
      }

      return mineSpeed;
   }

   public double blockBrokenTime(BlockPos pos, ItemStack tool) {
      if (pos != null && tool != null) {
         IBlockState blockMaterial = mc.world.getBlockState(pos);
         float damageTicks = this.blockBreakSpeed(blockMaterial, tool) / blockMaterial.getBlockHardness(mc.world, pos) / 30.0F;
         return Math.ceil((double)(1.0F / damageTicks)) * GameSyncTPS.getConpenseMath(1.0, 0.8F);
      } else {
         return 0.0;
      }
   }

   ItemStack getBestStack(BlockPos pos) {
      int bestSlot = -1;
      if (pos == null) {
         return Minecraft.player.inventory.getCurrentItem();
      } else {
         for (int i = 0; i < 9; i++) {
            ItemStack stack = Minecraft.player.inventory.getStackInSlot(i);
            double max = 0.0;
            if (!stack.isEmpty()) {
               float speed = stack.getStrVsBlock(mc.world.getBlockState(pos));
               if (speed > 1.0F) {
                  int eff;
                  speed = (float)(
                     (double)speed
                        + ((eff = EnchantmentHelper.getEnchantmentLevel(Enchantments.EFFICIENCY, stack)) > 0 ? Math.pow((double)eff, 2.0) + 1.0 : 0.0)
                  );
                  if ((double)speed > max) {
                     max = (double)speed;
                     bestSlot = i;
                  }

                  if (mc.world.getBlockState(pos).getBlock() == Blocks.WEB && InventoryUtil.getItemInHotbar(Items.SHEARS) != -1) {
                     bestSlot = InventoryUtil.getItemInHotbar(Items.SHEARS);
                  }
               }
            }
         }

         return bestSlot >= 0 && bestSlot <= 8 ? Minecraft.player.inventory.getStackInSlot(bestSlot) : Minecraft.player.inventory.getCurrentItem();
      }
   }

   @Override
   public void onRender2D(ScaledResolution sr) {
      if (this.actived && this.Mode.currentMode.equalsIgnoreCase("Reallyworld")) {
         float x = (float)(sr.getScaledWidth() / 2);
         float y = 50.0F;
         float w = 30.0F;
         float h = 1.0F;
         x -= w;
         y -= h;
         w *= 2.0F;
         h *= 2.0F;
         float x2 = x + w;
         float y2 = y + h;
         int c1 = -1;
         int c2 = -1;
         int cBG = ColorUtils.getColor(0, 0, 0, 130);
         RenderUtils.drawAlphedRect((double)x, (double)y, (double)x2, (double)y2, cBG);
         RenderUtils.drawLightContureRectFullGradient(x, y, x2, y2, c1, c2, false);
         RenderUtils.drawRect((double)x, (double)y, (double)(x + (x2 - x) * this.animSHKALE.getAnim()), (double)y2, c1);
      }
   }

   @EventTarget
   public void onPlayerMotionUpdate(EventPlayerMotionUpdate e) {
      float sp = 0.023F;
      double yaw = (double)MoveMeHelp.moveYaw(Minecraft.player.rotationYaw) * 0.017453292;
      if (this.Mode.currentMode.equalsIgnoreCase("Packet2") && this.actived) {
         Minecraft.player.onGround = false;
         e.setCancelled(true);
         float speedY = 0.0F;
         if (Minecraft.player.movementInput.jump) {
            speedY = 0.031F;
         } else if (Minecraft.player.movementInput.sneak) {
            Minecraft.player.motionY = Minecraft.player.ticksExisted % 7 == 1 ? -0.05 : -100.0;
         } else {
            Minecraft.player.motionY = (double)speedY;
         }

         MoveMeHelp.setSpeed(0.23);
         if (Minecraft.player.isCollidedHorizontally
               && mc.gameSettings.keyBindForward.isKeyDown()
               && (double)Minecraft.player.hurtTime < 3.5
               && Minecraft.player.isCollidedHorizontally
               && (Minecraft.player.hurtTime > 2 || Minecraft.player.hurtTime == 0)
            || Minecraft.player.movementInput.sneak && Minecraft.player.ticksExisted % 7 == 1
            || Minecraft.player.movementInput.jump) {
            if (!Minecraft.player.movementInput.sneak && !Minecraft.player.movementInput.jump) {
               Minecraft.player.motionX = -Math.sin(yaw) * (double)sp;
               Minecraft.player.motionZ = Math.cos(yaw) * (double)sp;
            }

            double x = Minecraft.player.posX + Minecraft.player.motionX;
            double y = Minecraft.player.posY + Minecraft.player.motionY;
            double z = Minecraft.player.posZ + Minecraft.player.motionZ;
            Minecraft.player
               .connection
               .sendPacket(
                  new CPacketPlayer.PositionRotation(
                     Minecraft.player.posX + Minecraft.player.motionX,
                     Minecraft.player.posY + Minecraft.player.motionY,
                     Minecraft.player.posZ + Minecraft.player.motionZ,
                     Minecraft.player.rotationYaw,
                     Minecraft.player.rotationPitch,
                     Minecraft.player.onGround
                  )
               );
            y -= -1337.0;
            Minecraft.player.connection.sendPacket(new CPacketPlayer.Position(x, y, z, Minecraft.player.onGround));
            Minecraft.player
               .connection
               .sendPacket(
                  new CPacketPlayer.PositionRotation(
                     Minecraft.player.posX + Minecraft.player.motionX,
                     Minecraft.player.posY + Minecraft.player.motionY,
                     Minecraft.player.posZ + Minecraft.player.motionZ,
                     Minecraft.player.rotationYaw,
                     Minecraft.player.rotationPitch,
                     Minecraft.player.onGround
                  )
               );
            y -= -1337.0;
            Minecraft.player.connection.sendPacket(new CPacketPlayer.Position(x, y, z, Minecraft.player.onGround));
         }
      }
   }

   public static double[] forward(double speed) {
      float forward = MovementInput.moveForward;
      float side = MovementInput.moveStrafe;
      float yaw = Minecraft.player.prevRotationYaw + (Minecraft.player.rotationYaw - Minecraft.player.prevRotationYaw) * mc.getRenderPartialTicks();
      if (forward != 0.0F) {
         if (side > 0.0F) {
            yaw += (float)(forward > 0.0F ? -45 : 45);
         } else if (side < 0.0F) {
            yaw += (float)(forward > 0.0F ? 45 : -45);
         }

         side = 0.0F;
         if (forward > 0.0F) {
            forward = 1.0F;
         } else if (forward < 0.0F) {
            forward = -1.0F;
         }
      }

      double sin = Math.sin(Math.toRadians((double)(yaw + 90.0F)));
      double cos = Math.cos(Math.toRadians((double)(yaw + 90.0F)));
      double posX = (double)forward * speed * cos + (double)side * speed * sin;
      double posZ = (double)forward * speed * sin - (double)side * speed * cos;
      return new double[]{posX, posZ};
   }

   @EventTarget
   public void onPacket(EventReceivePacket event) {
      if (this.Mode.currentMode.equalsIgnoreCase("Packet2") && this.actived && event.getPacket() instanceof SPacketPlayerPosLook packet) {
         Minecraft.player.connection.sendPacket(new CPacketConfirmTeleport(packet.getTeleportId()));
         Minecraft.player
            .connection
            .sendPacket(new CPacketPlayer.PositionRotation(packet.getX(), Minecraft.player.posY, packet.getZ(), packet.getYaw(), packet.getPitch(), false));
         Minecraft.player.setPosition(packet.getX(), Minecraft.player.posY, packet.getZ());
         event.setCancelled(true);
      }
   }

   @EventTarget
   public void onPacket(EventSendPacket event) {
      if (Minecraft.player != null && mc.world != null && !Minecraft.player.isDead && this.actived && event.getPacket() instanceof CPacketConfirmTeleport) {
         this.flag = true;
      }
   }

   @EventTarget
   public void onRender3D(Event3D event) {
      if (!this.flag) {
         this.colorEx = MathUtils.lerp(this.colorEx, 0.0F, 0.05F);
         this.timer.reset();
      } else {
         this.colorEx = MathUtils.lerp(this.colorEx, 1.0F, 0.2F);
         if (this.timer.hasReached(300.0)) {
            this.flag = false;
         }
      }

      if (this.actived) {
         int color = ColorUtils.getProgressColor(1.0F - this.colorEx).getRGB();
         EntityPlayer entity = Minecraft.player;
         double x = 0.0;
         double y = 0.0;
         double z = 0.0;
         GL11.glPushMatrix();
         GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO
         );
         mc.entityRenderer.disableLightmap();
         GL11.glEnable(3042);
         GL11.glLineWidth(1.0F);
         GL11.glDisable(3553);
         GL11.glDisable(2929);
         GL11.glShadeModel(7425);
         RenderUtils.setupColor(color, Minecraft.player.isCollidedHorizontally ? 255.0F : 75.0F);
         GL11.glBegin(3);
         GL11.glVertex3d(-0.3, 0.0, -0.3);
         GL11.glVertex3d(-0.3, 0.0, 0.3);
         GL11.glVertex3d(0.3, 0.0, 0.3);
         GL11.glVertex3d(0.3, 0.0, -0.3);
         GL11.glVertex3d(-0.3, 0.0, -0.3);
         GL11.glEnd();
         GL11.glBegin(3);
         GL11.glVertex3d(-0.29, 0.0, -0.29);
         GL11.glVertex3d(-0.29, 0.0, 0.29);
         GL11.glVertex3d(0.29, 0.0, 0.29);
         GL11.glVertex3d(0.29, 0.0, -0.29);
         GL11.glVertex3d(-0.29, 0.0, -0.29);
         GL11.glEnd();
         GL11.glBegin(3);
         GL11.glVertex3d(-0.28, 0.0, -0.28);
         GL11.glVertex3d(-0.28, 0.0, 0.28);
         GL11.glVertex3d(0.28, 0.0, 0.28);
         GL11.glVertex3d(0.28, 0.0, -0.28);
         GL11.glVertex3d(-0.28, 0.0, -0.28);
         GL11.glEnd();
         GL11.glBegin(3);
         GL11.glVertex3d(-0.27, 0.0, -0.27);
         GL11.glVertex3d(-0.27, 0.0, 0.27);
         GL11.glVertex3d(0.27, 0.0, 0.27);
         GL11.glVertex3d(0.27, 0.0, -0.27);
         GL11.glVertex3d(-0.27, 0.0, -0.27);
         GL11.glEnd();
         mc.entityRenderer.enableLightmap();
         GL11.glLineWidth(1.0F);
         GL11.glShadeModel(7424);
         GL11.glEnable(3553);
         GL11.glEnable(2929);
         GlStateManager.enableAlpha();
         GlStateManager.enableBlend();
         GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO
         );
         GlStateManager.resetColor();
         GL11.glPopMatrix();
      }
   }

   @Override
   public void onUpdate() {
      if (this.actived && this.Mode.currentMode.equalsIgnoreCase("Reallyworld")) {
         float moveYaw = (float)Math.toRadians((double)MoveMeHelp.moveYaw(Minecraft.player.rotationYaw - 15.0F));
         float rs = MoveMeHelp.moveKeysPressed() ? 1.0F : 0.0F;
         double sin = -Math.sin((double)moveYaw) * (double)rs;
         double cos = Math.cos((double)moveYaw) * (double)rs;
         BlockPos pos = BlockUtils.getEntityBlockPos(Minecraft.player).add(sin, -0.0, cos);
         Block und = mc.world.getBlockState(pos).getBlock();
         if (und == Blocks.AIR) {
            pos = pos.down(2);
         }

         this.ticker--;
         if (pos != null) {
            PlayerHelper.currentBlock = pos;
            ItemStack stack = this.getBestStack(pos);
            double time = this.blockBrokenTime(pos, stack) * 4.0;
            int item = InventoryUtil.getItemInHotbar(stack.getItem());
            if (item != -1) {
               EnumFacing face = BlockUtils.getPlaceableSide(pos);
               if (face == null) {
                  face = EnumFacing.UP;
               }

               int slot = Minecraft.player.inventory.currentItem;
               Minecraft.player.inventory.currentItem = item;
               mc.playerController.blockHitDelay = 0;
               mc.playerController.onPlayerDamageBlock(pos, face);
               if (Minecraft.player.ticksExisted % 3 == 0) {
                  Minecraft.player.connection.sendPacket(new CPacketAnimation(EnumHand.OFF_HAND));
               }

               if (mc.world.getBlockState(pos).getBlock() == Blocks.AIR) {
                  this.ticker = 7.0F;
               }

               if (this.ticker == 6.0F && Minecraft.player.isJumping() && MoveMeHelp.moveKeysPressed()) {
                  Minecraft.player.multiplyMotionXZ(1.5F);
               }

               Minecraft.player.inventory.currentItem = slot;
               this.animSHKALE.to = MathUtils.clamp(mc.playerController.curBlockDamageMP * 1.05F, 0.0F, 1.0F);
               this.animSHKALE.speed = 0.125F;
            }
         }

         this.tickGo = this.ticker > 0.0F || Minecraft.player.ticksExisted <= 80;
      }

      if (this.flag && !this.timer.hasReached(50.0)) {
         Minecraft.player.motionX = -Minecraft.player.motionX * 2.0;
         Minecraft.player.motionZ = -Minecraft.player.motionZ * 2.0;
      }

      Minecraft.player.noClip = true;
      Minecraft.player.stepHeight = 0.0F;
      double x = Minecraft.player.posX;
      double y = Minecraft.player.posY;
      double z = Minecraft.player.posZ;
      boolean isInBlock = false;

      for (float i = 0.0F; (double)i < (Minecraft.player.isSneaking() ? 1.6 : 1.8); i++) {
         if (posBlock(x, y + (double)i, z)
            || posBlock(x + 0.275F, y + (double)i, z + 0.275F)
            || posBlock(x - 0.275F, y + (double)i, z - 0.275F)
            || posBlock(x + 0.275F, y + (double)i, z)
            || posBlock(x - 0.275F, y + (double)i, z)
            || posBlock(x, y + (double)i, z + 0.275F)
            || posBlock(x, y + (double)i, z - 0.275F)
            || posBlock(x + 0.275F, y + (double)i, z - 0.275F)
            || posBlock(x - 0.275F, y + (double)i, z + 0.275F)) {
            isInBlock = true;
         }
      }

      double yaw = (double)Minecraft.player.rotationYaw * 0.017453292;
      float speed = 1.0E-7F;
      float TMSpeed = this.Boost.getFloat();
      if (this.Mode.currentMode.equalsIgnoreCase("Packet") && isInBlock) {
         Minecraft.player.motionY = 0.0;
         Minecraft.player.motionX = 0.0;
         Minecraft.player.motionZ = 0.0;
         Minecraft.player.onGround = false;
         Minecraft.player.jumpMovementFactor = 0.0F;
         if (mc.gameSettings.keyBindForward.pressed) {
            Minecraft.player
               .connection
               .sendPacket(
                  new CPacketPlayer.Position(
                     Minecraft.player.posX - Math.sin(Math.toRadians((double)Minecraft.player.rotationYaw)) * 15.0,
                     Minecraft.player.posY,
                     Minecraft.player.posZ + Math.cos(Math.toRadians((double)Minecraft.player.rotationYaw)) * 15.0,
                     true
                  )
               );
            Minecraft.player
               .connection
               .sendPacket(new CPacketPlayer.Position(Minecraft.player.posX, Minecraft.player.posY - 15.0, Minecraft.player.posZ, true));
            if (Minecraft.player.ticksExisted % 9 == 0) {
               Minecraft.player
                  .connection
                  .sendPacket(new CPacketPlayer.Position(Minecraft.player.posX, Minecraft.player.posY + 1.0, Minecraft.player.posZ, true));
            }
         }

         if (mc.gameSettings.keyBindSneak.pressed) {
            Minecraft.player.connection.sendPacket(new CPacketPlayer.Position(Minecraft.player.posX, Minecraft.player.posY - 2.0, Minecraft.player.posZ, true));
         }
      }

      if (this.TimerBoost.getBool() && !this.Mode.currentMode.equalsIgnoreCase("Packet") && !this.Mode.currentMode.equalsIgnoreCase("Packet2")) {
         if (!mc.gameSettings.keyBindForward.isKeyDown()
               && !mc.gameSettings.keyBindBack.isKeyDown()
               && !mc.gameSettings.keyBindSneak.isKeyDown()
               && !Minecraft.player.isMoving()
            || !isInBlock
            || this.Mode.currentMode.equalsIgnoreCase("Matrix") && Minecraft.player.isSneaking()) {
            if (mc.timer.speed == (double)(TMSpeed + 1.0F)) {
               mc.timer.speed = 1.0;
            }
         } else {
            mc.timer.speed = (double)(TMSpeed + 1.0F);
         }
      }

      if (this.Mode.currentMode.equalsIgnoreCase("Matrix")) {
         if (isInBlock) {
            Minecraft.player.isEntityInsideOpaqueBlock = true;
            mc.timer.speed = Minecraft.player.ticksExisted % 2 == 0 ? 8.0 : 0.5;
            Minecraft.player.motionY = Minecraft.player.isSneaking() ? -0.7 : (Minecraft.player.isJumping() ? 0.42 : Minecraft.player.motionY);
            if (Minecraft.player.isJumping()) {
               Minecraft.player.setPosY(Minecraft.player.posY + 1.0E-10);
            }
         } else if (mc.timer.speed == 8.0 || mc.timer.speed == 0.5) {
            mc.timer.speed = 1.0;
         }
      }

      if (this.MatrixLift.getBool()
         && isInBlock
         && !this.Mode.currentMode.equalsIgnoreCase("Sunrise")
         && !this.Mode.currentMode.equalsIgnoreCase("Packet")
         && !this.Mode.currentMode.equalsIgnoreCase("Packet2")
         && !Minecraft.player.isInWater()
         && mc.world.getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY - 0.5, Minecraft.player.posZ)).getBlock() != Blocks.WATER
         && Minecraft.player.isJumping()) {
         Minecraft.player.onGround = true;
         Entity.motionx = -0.05 + 0.1 * Math.random();
         Entity.motionz = -0.05 + 0.1 * Math.random();
         Minecraft.player.connection.sendPacket(new CPacketPlayer(false));
      }

      boolean nocull = false;
      if (this.MatrixTP.getBool() && !this.Mode.currentMode.equalsIgnoreCase("Packet") && !this.Mode.currentMode.equalsIgnoreCase("Packet2")) {
         if (!Minecraft.player.isInWater()
            && mc.world.getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY - 0.5, Minecraft.player.posZ)).getBlock() != Blocks.WATER
            && Minecraft.player.isEntityInsideOpaqueBlock()) {
            Minecraft.player.motionY = 0.0;
         }

         int VerticalRange = this.TPRange.getInt();

         for (float ix = 0.0F; ix < (float)VerticalRange; ix += 0.005F) {
            float o = mc.gameSettings.keyBindJump.isKeyDown() && !mc.gameSettings.keyBindSneak.isKeyDown() ? ix : -ix;
            if (mc.world.getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY + (double)o + 1.0, Minecraft.player.posZ)).getBlock()
                  == Blocks.AIR
               && (
                  mc.world.getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY + (double)o + 0.005, Minecraft.player.posZ)).getBlock()
                        == Blocks.AIR
                     || mc.world
                           .getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY + (double)o + 0.005, Minecraft.player.posZ))
                           .getBlock()
                        == Blocks.WATER
                     || mc.world
                           .getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY + (double)o + 0.005, Minecraft.player.posZ))
                           .getBlock()
                        == Blocks.LAVA
                     || mc.world
                           .getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY + (double)o + 0.005, Minecraft.player.posZ))
                           .getBlock()
                        == Blocks.WEB
                     || mc.world.getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY + (double)o, Minecraft.player.posZ)).getBlock()
                        == Blocks.TRAPDOOR
                     || mc.world.getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY + (double)o, Minecraft.player.posZ)).getBlock()
                        == Blocks.IRON_TRAPDOOR
               )
               && mc.world.getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY + (double)o - 0.002, Minecraft.player.posZ)).getBlock()
                  != Blocks.AIR
               && ix > 2.0F) {
               if (mc.world.getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY - 0.1, Minecraft.player.posZ)).getBlock() != Blocks.WATER
                  && mc.gameSettings.keyBindJump.isKeyDown()) {
                  Minecraft.player.onGround = true;
                  Minecraft.player.motionY = 0.0;
               }

               if (mc.gameSettings.keyBindSneak.isKeyDown()) {
                  Minecraft.player.onGround = true;
                  mc.gameSettings.keyBindJump.pressed = false;
               }

               if (mc.gameSettings.keyBindJump.isKeyDown() || mc.gameSettings.keyBindSneak.isKeyDown()) {
                  Minecraft.player.fallDistance = 4.5682973E-5F;
                  nocull = true;
                  double clip = mc.gameSettings.keyBindJump.isKeyDown() && !mc.gameSettings.keyBindSneak.isKeyDown()
                     ? (double)ix + 0.05
                     : (double)(-ix) + 0.002;
                  if (mc.world.getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY + (double)o - 1.0, Minecraft.player.posZ)).getBlock()
                     == Blocks.WATER) {
                     clip--;
                  }

                  if (Minecraft.player.ticksExisted % 3 == 0) {
                     if (mc.world.getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY - 0.1, Minecraft.player.posZ)).getBlock()
                        != Blocks.WATER) {
                        Minecraft.player.onGround = true;
                     }

                     Clip.goClip(clip, 0.0, ElytraBoost.canElytra());
                  }
               }
            }
         }
      }

      float speedY = this.SpeedY.getFloat();
      if ((this.Mode.currentMode.equalsIgnoreCase("Vanilla") || this.Mode.currentMode.equalsIgnoreCase("Matrix")) && isInBlock) {
         if (!this.Mode.currentMode.equalsIgnoreCase("Matrix") || !Minecraft.player.isSneaking()) {
            Minecraft.player.motionY = 0.0;
         }

         MoveMeHelp.setSpeed(MoveMeHelp.getSpeed() * (double)this.SpeedF.getFloat());
         if (mc.gameSettings.keyBindJump.isKeyDown()) {
            Minecraft.player.motionY = Minecraft.player.motionY + (this.Mode.currentMode.equalsIgnoreCase("Matrix") ? 0.42 : (double)speedY);
         }

         if (mc.gameSettings.keyBindSneak.isKeyDown()) {
            Minecraft.player.motionY = Minecraft.player.motionY - (this.Mode.currentMode.equalsIgnoreCase("Matrix") ? -0.5 : (double)speedY);
         }
      }

      if (this.DoorPhase.getBool()
         && !this.Mode.currentMode.equalsIgnoreCase("Sunrise")
         && !this.Mode.currentMode.equalsIgnoreCase("Packet")
         && !this.Mode.currentMode.equalsIgnoreCase("Packet2")) {
         float val = 1.0F;
         double dx = x - (double)(MathHelper.sin((float)yaw) * val);
         double dx2 = x - (double)MathHelper.sin((float)yaw) * 0.3001;
         double dy = y + (double)Minecraft.player.height - 1.0E-5;
         double dz = z + (double)(MathHelper.cos((float)yaw) * val);
         double dz2 = z + (double)MathHelper.cos((float)yaw) * 0.3001;
         if (mc.world.getBlockState(new BlockPos(dx2, dy, dz2)).getBlock() instanceof BlockDoor
            && Minecraft.player.isCollidedHorizontally
            && Minecraft.player.isCollidedHorizontally) {
            Minecraft.player.isSneaking = !isInBlock;
            if (Minecraft.player.motionY < 0.2 && Minecraft.player.ticksExisted % 2 == 0) {
               Minecraft.player.onGround = true;
               Minecraft.player.motionY = 0.42F;
            }

            if (Minecraft.player.hurtTime > 0) {
               Minecraft.player.setPosition(dx, dy, dz);
               mc.getConnection().sendPacket(new CPacketPlayer.Position(x, y, z, false));
            }
         }
      }
   }

   public static boolean posBlock(double x, double y, double z) {
      return mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.AIR
         && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.WATER
         && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.LAVA
         && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.BED
         && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.CAKE
         && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.TALLGRASS
         && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.GRASS_PATH
         && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.FLOWER_POT
         && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.CHORUS_FLOWER
         && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.RED_FLOWER
         && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.YELLOW_FLOWER
         && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.SAPLING
         && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.VINE
         && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.ACACIA_FENCE
         && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.ACACIA_FENCE_GATE
         && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.BIRCH_FENCE
         && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.BIRCH_FENCE_GATE
         && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.DARK_OAK_FENCE
         && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.DARK_OAK_FENCE_GATE
         && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.JUNGLE_FENCE
         && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.JUNGLE_FENCE_GATE
         && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.NETHER_BRICK_FENCE
         && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.OAK_FENCE
         && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.OAK_FENCE_GATE
         && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.SPRUCE_FENCE
         && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.SPRUCE_FENCE_GATE
         && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.ENCHANTING_TABLE
         && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.END_PORTAL_FRAME
         && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.DOUBLE_PLANT
         && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.STANDING_SIGN
         && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.WALL_SIGN
         && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.SKULL
         && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.DAYLIGHT_DETECTOR
         && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.DAYLIGHT_DETECTOR_INVERTED
         && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.PURPUR_SLAB
         && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.STONE_SLAB
         && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.WOODEN_SLAB
         && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.CARPET
         && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.DEADBUSH
         && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.REDSTONE_WIRE
         && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.SNOW_LAYER;
   }

   public static boolean isNoClip(Entity entity) {
      if (Minecraft.player == null) {
         return false;
      } else {
         double x = Minecraft.player.posX;
         double y = Minecraft.player.posY;
         double z = Minecraft.player.posZ;
         double yaw = (double)Minecraft.player.rotationYaw * 0.017453292;
         double dx = x - Math.sin(yaw) * 0.05F;
         double dy = y + (double)Minecraft.player.height;
         double dz = z + Math.cos(yaw) * 0.05F;
         if (!get.Mode.currentMode.equalsIgnoreCase("Sunrise")
            && !get.Mode.currentMode.equalsIgnoreCase("Packet")
            && !get.Mode.currentMode.equalsIgnoreCase("Packet2")
            && get.actived
            && Minecraft.player != null
            && (Minecraft.player.ridingEntity == null || entity == Minecraft.player.ridingEntity)) {
            for (float i = 0.0F; (double)i < (Minecraft.player.isSneaking() ? 1.6 : 1.8); i++) {
               if (posBlock(x, y + (double)i, z)
                  || posBlock(x + 0.275F, y + (double)i, z + 0.275F)
                  || posBlock(x - 0.275F, y + (double)i, z - 0.275F)
                  || posBlock(x + 0.275F, y + (double)i, z)
                  || posBlock(x - 0.275F, y + (double)i, z)
                  || posBlock(x, y + (double)i, z + 0.275F)
                  || posBlock(x, y + (double)i, z - 0.275F)
                  || posBlock(x + 0.275F, y + (double)i, z - 0.275F)
                  || posBlock(x - 0.275F, y + (double)i, z + 0.275F)) {
                  return true;
               }
            }

            if (mc.world.getBlockState(new BlockPos(dx, dy, dz)) instanceof BlockDoor && Minecraft.player.isCollidedHorizontally) {
               return false;
            }
         }

         return entity.noClip;
      }
   }

   @Override
   public String getDisplayName() {
      return this.getDisplayByMode(this.Mode.currentMode);
   }

   @Override
   public void onToggled(boolean actived) {
      if (!actived) {
         this.colorEx = 0.0F;
         this.timer.reset();
         mc.timer.speed = 1.0;
         Minecraft.player.stepHeight = 0.6F;
      }

      super.onToggled(actived);
   }
}
