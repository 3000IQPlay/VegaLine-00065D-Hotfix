package ru.govno.client.module.modules;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCactus;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import ru.govno.client.Client;
import ru.govno.client.event.EventTarget;
import ru.govno.client.event.events.EventCanPlaceBlock;
import ru.govno.client.event.events.EventReceivePacket;
import ru.govno.client.module.Module;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.module.settings.FloatSettings;
import ru.govno.client.module.settings.ModeSettings;
import ru.govno.client.utils.InventoryUtil;
import ru.govno.client.utils.Command.impl.Clip;
import ru.govno.client.utils.Math.BlockUtils;
import ru.govno.client.utils.Math.TimerHelper;
import ru.govno.client.utils.Movement.MoveMeHelp;

public class HighJump extends Module {
   public static HighJump get;
   public BoolSettings GroundJump;
   public BoolSettings WaterJump;
   public BoolSettings WaterYPort;
   public BoolSettings CactusJump;
   public BoolSettings CactusVastum;
   public BoolSettings WebJump;
   public BoolSettings WebBoost;
   public BoolSettings SelfWebPlace;
   public ModeSettings JumpMode;
   public FloatSettings JumpPulse;
   public FloatSettings MotionY;
   public FloatSettings WaterPulse;
   public FloatSettings CactusPulse;
   public FloatSettings WebPulse;
   TimerHelper timer = new TimerHelper();
   TimerHelper wait = new TimerHelper();
   boolean toDo = false;
   int popitka = 0;
   double togglePos;
   BlockPos state;
   public static boolean toPlace = true;
   boolean doPlace = false;
   int ticksLeft = 0;

   public HighJump() {
      super("HighJump", 0, Module.Category.MOVEMENT);
      this.settings.add(this.GroundJump = new BoolSettings("GroundJump", true, this));
      this.settings
         .add(
            this.JumpMode = new ModeSettings(
               "JumpMode", "MatrixNew", this, new String[]{"Normal", "MatrixOld", "MatrixNew", "MatrixWait", "MatrixDestruct"}, () -> this.GroundJump.getBool()
            )
         );
      this.settings
         .add(
            this.JumpPulse = new FloatSettings(
               "JumpPulse",
               1.0F,
               10.0F,
               0.0F,
               this,
               () -> this.GroundJump.getBool()
                     && (this.JumpMode.currentMode.equalsIgnoreCase("Normal") || this.JumpMode.currentMode.equalsIgnoreCase("MatrixWait"))
            )
         );
      this.settings
         .add(
            this.MotionY = new FloatSettings(
               "MotionY",
               3.0F,
               10.0F,
               0.6F,
               this,
               () -> this.GroundJump.getBool()
                     && (this.JumpMode.currentMode.equalsIgnoreCase("MatrixNew") || this.JumpMode.currentMode.equalsIgnoreCase("MatrixDestruct"))
            )
         );
      this.settings.add(this.WaterJump = new BoolSettings("WaterJump", false, this));
      this.settings.add(this.WaterPulse = new FloatSettings("WaterPulse", 10.0F, 16.0F, 0.0F, this, () -> this.WaterJump.getBool()));
      this.settings.add(this.WaterYPort = new BoolSettings("WaterYPort", true, this, () -> this.WaterJump.getBool()));
      this.settings.add(this.CactusJump = new BoolSettings("CactusJump", true, this));
      this.settings.add(this.CactusPulse = new FloatSettings("CactusPulse", 3.0F, 10.0F, 0.0F, this, () -> this.CactusJump.getBool()));
      this.settings.add(this.CactusVastum = new BoolSettings("CactusVastum", false, this, () -> this.CactusJump.getBool()));
      this.settings.add(this.WebJump = new BoolSettings("WebJump", true, this));
      this.settings.add(this.WebPulse = new FloatSettings("WebPulse", 3.0F, 10.0F, 0.0F, this, () -> this.WebJump.getBool()));
      this.settings.add(this.WebBoost = new BoolSettings("WebBoost", false, this, () -> this.WebJump.getBool()));
      this.settings.add(this.SelfWebPlace = new BoolSettings("SelfWebPlace", true, this, () -> this.WebJump.getBool()));
      get = this;
   }

   private boolean stackIsBlockStack(ItemStack stack) {
      return stack != null && stack.getItem() instanceof ItemBlock;
   }

   private void selfPlace(EnumHand hand) {
      double y = Minecraft.player.posY;

      for (double offset : new double[]{0.42F, 0.7531999805212024, 1.0013359791121417, 1.1661092609382138}) {
         mc.getConnection().sendPacket(new CPacketPlayer.Position(Minecraft.player.posX, y + offset, Minecraft.player.posZ, false));
         Minecraft.player.setPosition(Minecraft.player.posX, y + offset, Minecraft.player.posZ);
      }

      float prevPitch = Minecraft.player.rotationPitch;
      Minecraft.player.rotationPitch = 90.0F;
      mc.entityRenderer.getMouseOver(1.0F);
      if (mc.objectMouseOver != null && mc.objectMouseOver.getBlockPos() != null && mc.objectMouseOver.hitVec != null && mc.objectMouseOver.sideHit != null) {
         mc.playerController
            .processRightClickBlock(Minecraft.player, mc.world, mc.objectMouseOver.getBlockPos(), mc.objectMouseOver.sideHit, mc.objectMouseOver.hitVec, hand);
      }

      Minecraft.player.rotationPitch = prevPitch;
      mc.entityRenderer.getMouseOver(1.0F);
   }

   private void sentBlockPlacement(boolean canUseInventory) {
      int oldSlot = Minecraft.player.inventory.currentItem;
      int currentSlot = -1;
      EnumHand placeHand = null;
      ItemStack offStack = Minecraft.player.getHeldItemOffhand();
      if (this.stackIsBlockStack(offStack)) {
         placeHand = EnumHand.OFF_HAND;
      } else {
         ItemStack mainStack = Minecraft.player.getHeldItemMainhand();
         if (this.stackIsBlockStack(mainStack)) {
            placeHand = EnumHand.MAIN_HAND;
         }
      }

      if (placeHand == null) {
         for (int slot = 0; slot < (canUseInventory ? 44 : 8); slot++) {
            ItemStack stackInSlot = Minecraft.player.inventory.getStackInSlot(slot);
            if (this.stackIsBlockStack(stackInSlot)) {
               currentSlot = slot;
               placeHand = EnumHand.MAIN_HAND;
               break;
            }
         }

         if (placeHand == EnumHand.MAIN_HAND && currentSlot != -1) {
            if (currentSlot <= 8) {
               Minecraft.player.inventory.currentItem = currentSlot;
               mc.playerController.syncCurrentPlayItem();
            } else {
               mc.playerController.windowClick(0, currentSlot, oldSlot, ClickType.SWAP, Minecraft.player);
               mc.playerController.windowClickMemory(0, currentSlot, oldSlot, ClickType.SWAP, Minecraft.player, 50);
            }

            this.selfPlace(placeHand);
            if (currentSlot <= 8) {
               Minecraft.player.inventory.currentItem = oldSlot;
               mc.playerController.syncCurrentPlayItem();
            }

            return;
         }
      }

      if (placeHand != null) {
         this.selfPlace(placeHand);
      }
   }

   private void onEnableMatrixDestructPulse() {
      if (this.MotionY.getFloat() != 0.0F && Minecraft.player.onGround) {
         this.sentBlockPlacement(true);
         Minecraft.player.motionY = (double)this.MotionY.getFloat();
         Minecraft.player.jumpMovementFactor = 0.0F;
         Minecraft.player.multiplyMotionXZ(0.0F);
         this.toggle(false);
      } else {
         if (!Minecraft.player.onGround) {
            Client.msg("§f§lModules:§r §7[§l" + this.getName() + "§r§7] §7находитесь на земле.", false);
         }

         if (this.MotionY.getFloat() == 0.0F) {
            Client.msg("§f§lModules:§r §7[§l" + this.getName() + "§r§7] §7насройка излишне мала.", false);
         }

         this.toggle(false);
      }
   }

   @EventTarget
   public void onPacketReceive(EventReceivePacket event) {
      if (this.actived && Minecraft.player != null) {
         if (this.GroundJump.getBool()
            && this.JumpMode.currentMode.equalsIgnoreCase("MatrixWait")
            && event.getPacket() instanceof SPacketChat packet
            && packet.getChatComponent().getUnformattedText().contains("Извините, но вы не можете")) {
            this.toDo = true;
            event.setCancelled(true);
         }
      }
   }

   @Override
   public void onUpdate() {
      if (this.actived) {
         if (this.GroundJump.getBool()) {
            this.groundJump();
         }

         if (this.CactusJump.getBool()) {
            this.cactusJump();
         }

         if (this.WaterJump.getBool()) {
            this.waterLeave();
         }

         if (this.WebJump.getBool()) {
            this.webLeave();
         }
      }
   }

   void groundJump() {
      if (this.JumpMode.currentMode.equalsIgnoreCase("MatrixNew")) {
         float jump = this.MotionY.getFloat();
         float speed = 0.0F;
         MoveMeHelp.setSpeed(0.0);
         MoveMeHelp.setCuttingSpeed(0.0);
         Minecraft.player.connection.sendPacket(new CPacketPlayer(true));
         if (Minecraft.player.onGround && !Minecraft.player.isJumping()) {
            Minecraft.player.motionY = 0.42;
         }

         new Thread(() -> {
            if (this.togglePos == 0.0) {
               this.togglePos = Minecraft.player.posY;
            }

            Minecraft.player.motionY = (double)jump;
            mc.timer.speed = 1.9;

            try {
               TimeUnit.MILLISECONDS.sleep(75L);
            } catch (InterruptedException var3x) {
               var3x.printStackTrace();
            }

            Minecraft.player.motionY = (double)jump - (0.098 + 0.01 * (double)(jump * 2.0F - 2.0F));
            MoveMeHelp.setSpeed(0.0);
            MoveMeHelp.setCuttingSpeed(1.0E-45);
            Minecraft.player.jumpMovementFactor = 0.0F;
            mc.timer.speed = 1.0;
            if (this.actived) {
               this.toggle(false);
            }

            if (this.togglePos != 0.0) {
               Minecraft.player.posY = this.togglePos;
            }
         }).start();
      }

      if (this.JumpMode.currentMode.equalsIgnoreCase("MatrixOld") && mc.gameSettings.keyBindJump.isKeyDown()) {
         Minecraft.player.jump();
         MoveMeHelp.setSpeed(0.0);
         Minecraft.player.motionY += 0.55;
         mc.timer.speed = 1.45F;
      }

      if (this.JumpMode.currentMode.equalsIgnoreCase("MatrixWait")) {
         boolean has = this.state != null
            && this.wait.hasReached((double)(250.0F + mc.world.getBlockState(this.state).getBlockHardness(mc.world, this.state) * 1400.0F));
         if (this.state != null && has && (Minecraft.player.onGround || JesusSpeed.isJesused) || this.toDo && Minecraft.player.onGround) {
            Minecraft.player.motionY = (double)this.currentFloatValue("JumpPulse");
            MoveMeHelp.setSpeed(0.0);
            MoveMeHelp.setCuttingSpeed(0.0);
            Minecraft.player.jumpMovementFactor = 0.0F;
            this.wait.reset();
            this.toDo = false;
         }

         if (Minecraft.player.motionY > 0.43 || Minecraft.player.motionY < -0.6) {
            Minecraft.player.jumpMovementFactor = 0.0F;
            Minecraft.player.setSprinting(true);
         }

         List<BlockPos> mixPoses = new CopyOnWriteArrayList<>();
         Vec3d ePos = new Vec3d(Minecraft.player.posX, Minecraft.player.posY, Minecraft.player.posZ);
         float r = 5.0F;

         for (float x = -5.0F; x < 5.0F; x++) {
            for (float y = -5.0F; y < 0.0F; y++) {
               for (float z = -5.0F; z < 5.0F; z++) {
                  BlockPos poss = new BlockPos((double)x + ePos.xCoord, (double)y + ePos.yCoord, (double)z + ePos.zCoord);
                  Block block = mc.world.getBlockState(poss).getBlock();
                  if (block != Blocks.AIR
                     && block != Blocks.BARRIER
                     && block != Blocks.BEDROCK
                     && poss != null
                     && Minecraft.player.getDistanceAtEye((double)poss.getX(), (double)poss.getY(), (double)poss.getZ()) <= 5.0) {
                     mixPoses.add(poss);
                  }
               }
            }
         }

         if (mixPoses.size() != 0) {
            mixPoses.sort(Comparator.comparing(current -> mc.world.getBlockState(current).getBlockHardness(mc.world, current)));
            this.state = mixPoses.get(0);
            if (this.state != null && !this.toDo && !has) {
               Minecraft.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, this.state, EnumFacing.UP));
               Minecraft.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, this.state, EnumFacing.UP));
            }
         }
      }
   }

   void cactusJump() {
      if (BlockCactus.canLeave && Minecraft.player.hurtTime > 0) {
         if (this.CactusVastum.getBool() && Minecraft.player.onGround) {
            Minecraft.player.onGround = false;
            Minecraft.player
               .setPosition(
                  Minecraft.player.posX + 1.0E-6F,
                  Minecraft.player.posY + (double)(1000.0F * (this.CactusPulse.getFloat() / 10.0F)),
                  Minecraft.player.posZ + 1.0E-6F
               );
            Minecraft.player.motionY = 0.0;
         } else {
            Minecraft.player.motionY = (double)this.CactusPulse.getFloat();
         }

         BlockCactus.canLeave = false;
      }
   }

   private final int getSlotWebInHotbar() {
      return InventoryUtil.getItemInHotbar(Item.getItemFromBlock(Blocks.WEB));
   }

   private final boolean haveWebInOffhand() {
      return Minecraft.player.getHeldItemOffhand().getItem() == Item.getItemFromBlock(Blocks.WEB);
   }

   private final boolean haveWebInInventory() {
      return this.haveWebInOffhand() || this.getSlotWebInHotbar() != -1;
   }

   boolean canPlaceWeb(double x, double y, double z) {
      return mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() == Blocks.AIR && !Speed.posBlock(x, y, z);
   }

   private final EnumFacing getPlaceableSide(BlockPos var0) {
      for (EnumFacing v3 : EnumFacing.values()) {
         BlockPos v4 = var0.offset(v3);
         if (mc.world.getBlockState(v4).getBlock().canCollideCheck(mc.world.getBlockState(v4), false)) {
            IBlockState v5 = mc.world.getBlockState(v4);
            if (!v5.getMaterial().isReplaceable()) {
               return v3;
            }
         }
      }

      return null;
   }

   private final boolean lookingAtPos(float yaw, float pitch, BlockPos pos, float range) {
      RayTraceResult zalupa = Minecraft.player.rayTraceCustom((double)range, mc.getRenderPartialTicks(), yaw, pitch);
      Vec3d hitVec = zalupa.hitVec;
      if (hitVec == null) {
         return false;
      } else if (hitVec.xCoord - (double)pos.getX() > 1.0 || hitVec.xCoord - (double)pos.getX() < 0.0) {
         return false;
      } else {
         return !(hitVec.yCoord - (double)pos.getY() > 1.0) && !(hitVec.yCoord - (double)pos.getY() < 0.0)
            ? !(hitVec.zCoord - (double)pos.getZ() > 1.0) && !(hitVec.zCoord - (double)pos.getZ() < 0.0)
            : false;
      }
   }

   private final void placeWeb(EnumHand hand, BlockPos currentPosToPlace) {
      if (currentPosToPlace != null) {
         EnumFacing v4 = this.getPlaceableSide(currentPosToPlace);
         if (v4 != null) {
            EnumFacing v2 = v4.getOpposite();
            BlockPos v1 = currentPosToPlace.offset(v4);
            Vec3d v3 = new Vec3d(v1).addVector(0.5, 0.5, 0.5).add(new Vec3d(v2.getDirectionVec()).scale(0.5));
            if (InventoryUtil.getItemInHotbar(Item.getItemFromBlock(Blocks.WEB)) != -1
               && hand == EnumHand.MAIN_HAND
               && Minecraft.player.inventory.currentItem != InventoryUtil.getItemInHotbar(Item.getItemFromBlock(Blocks.WEB))) {
               Minecraft.player.connection.sendPacket(new CPacketHeldItemChange(InventoryUtil.getItemInHotbar(Item.getItemFromBlock(Blocks.WEB))));
            }

            mc.playerController.processRightClickBlock(Minecraft.player, mc.world, v1, v2, v3, hand);
            if (InventoryUtil.getItemInHotbar(Item.getItemFromBlock(Blocks.WEB)) != -1
               && hand == EnumHand.MAIN_HAND
               && Minecraft.player.inventory.currentItem != InventoryUtil.getItemInHotbar(Item.getItemFromBlock(Blocks.WEB))) {
               Minecraft.player.connection.sendPacket(new CPacketHeldItemChange(Minecraft.player.inventory.currentItem));
            }
         }
      }
   }

   @EventTarget
   public void can(EventCanPlaceBlock event) {
      if (this.actived && this.doPlace) {
         double x = Minecraft.player.posX;
         int y = (int)Minecraft.player.posY;
         double z = Minecraft.player.posZ;
         this.placeWeb(this.haveWebInOffhand() ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, new BlockPos(x, (double)y, z));
         this.doPlace = false;
      }
   }

   void webLeave() {
      double x = Minecraft.player.posX;
      double y = Minecraft.player.posY;
      double z = Minecraft.player.posZ;
      if (toPlace) {
         this.placeWeb(this.haveWebInOffhand() ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, BlockUtils.getEntityBlockPos(Minecraft.player));
         toPlace = false;
      }

      if (Minecraft.player.isInWeb) {
         Minecraft.player.motionY = 0.0;
         MoveMeHelp.setSpeed(0.0);
         Minecraft.player.motionY++;
      }

      if (this.SelfWebPlace.getBool() && this.haveWebInInventory() && this.canPlaceWeb(x, y, z)) {
         toPlace = true;
         this.ticksLeft++;
         if (Minecraft.player.isCollidedVertically && Minecraft.player.onGround && !Minecraft.player.isJumping()) {
            Minecraft.player.jump();
         }

         if (this.ticksLeft == 1) {
            this.doPlace = true;
         } else if (this.ticksLeft < 1) {
            Minecraft.player.multiplyMotionXZ(0.0F);
            Minecraft.player.jumpMovementFactor = 0.0F;
         }
      }

      float ex = 0.2F;
      if ((
            mc.world.getBlockState(new BlockPos(x, y - (double)ex, z)).getBlock() == Blocks.WEB
               || mc.world.getBlockState(new BlockPos(x - 0.3F, y - (double)ex, z)).getBlock() == Blocks.WEB
               || mc.world.getBlockState(new BlockPos(x, y - (double)ex, z - 0.3F)).getBlock() == Blocks.WEB
               || mc.world.getBlockState(new BlockPos(x - 0.3F, y - (double)ex, z - 0.3F)).getBlock() == Blocks.WEB
               || mc.world.getBlockState(new BlockPos(x + 0.3F, y - (double)ex, z)).getBlock() == Blocks.WEB
               || mc.world.getBlockState(new BlockPos(x, y - (double)ex, z + 0.3F)).getBlock() == Blocks.WEB
               || mc.world.getBlockState(new BlockPos(x + 0.3F, y - (double)ex, z + 0.3F)).getBlock() == Blocks.WEB
               || mc.world.getBlockState(new BlockPos(x + 0.3F, y - (double)ex, z - 0.3F)).getBlock() == Blocks.WEB
               || mc.world.getBlockState(new BlockPos(x - 0.3F, y - (double)ex, z + 0.3F)).getBlock() == Blocks.WEB
         )
         && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.WEB
         && Minecraft.player.fallDistance == 0.0F
         && !Minecraft.player.isInWeb
         && Minecraft.player.motionY < 0.0) {
         Minecraft.player.motionY = (double)this.WebPulse.getFloat();
         if (this.WebBoost.getBool()) {
            MoveMeHelp.setSpeed(2.9F);
            MoveMeHelp.setCuttingSpeed(2.7358494F);
         }
      }
   }

   public boolean waterLeaveCanSolid() {
      return Minecraft.player != null && get != null && this.actived && this.WaterJump.getBool();
   }

   void waterLeave() {
      double x = Minecraft.player.posX;
      double y = Minecraft.player.posY;
      double z = Minecraft.player.posZ;
      if (!this.waterLeaveCanSolid() || !Minecraft.player.onGround || Minecraft.player.posY != (double)((int)Minecraft.player.posY + 1) - 1.0E-5) {
         boolean boost = mc.world.getBlockState(new BlockPos(x, y + 1.0, z)).getBlock() == Blocks.AIR
            && !(mc.world.getBlockState(new BlockPos(x, y - 1.0E-10, z)).getBlock() instanceof BlockLiquid);
         if (mc.world.getBlockState(new BlockPos(x, y + 0.1, z)).getBlock() instanceof BlockLiquid && Minecraft.player.fallDistance == 0.0F) {
            mc.gameSettings.keyBindJump.pressed = false;
            double speedUp = boost ? 0.42 : 0.19;
            Minecraft.player.motionY = speedUp;
            Entity.motiony = speedUp - 0.01;
            Minecraft.player.onGround = false;
         }
      } else if (this.WaterYPort.getBool()) {
         Clip.goClip(ElytraBoost.canElytra() ? 190.0 : 50.0, 0.0, ElytraBoost.canElytra());
         if (!ElytraBoost.canElytra()) {
            Clip.goClip(50.0, 1.0, false);
         }
      } else {
         Minecraft.player.motionY = (double)(this.WaterPulse.getFloat() - 0.2F);
      }
   }

   @Override
   public void onToggled(boolean actived) {
      toPlace = false;
      this.ticksLeft = 0;
      this.doPlace = false;
      this.wait.reset();
      this.toDo = false;
      this.togglePos = this.actived ? Minecraft.player.posY : 0.0;
      BlockCactus.canLeave = false;
      if (!actived && this.GroundJump.getBool() && this.JumpMode.currentMode.equalsIgnoreCase("MatrixOld") && mc.timer.speed == 1.45F) {
         mc.timer.speed = 1.0;
      }

      if (actived && this.JumpMode.currentMode.equalsIgnoreCase("MatrixDestruct")) {
         this.onEnableMatrixDestructPulse();
      }

      super.onToggled(actived);
   }
}
