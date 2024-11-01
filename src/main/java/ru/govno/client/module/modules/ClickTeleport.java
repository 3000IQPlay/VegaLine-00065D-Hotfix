package ru.govno.client.module.modules;

import javax.vecmath.Vector2f;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketEntityAction.Action;
import net.minecraft.network.play.client.CPacketPlayer.Position;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import ru.govno.client.event.EventTarget;
import ru.govno.client.event.events.Event3D;
import ru.govno.client.event.events.EventInput;
import ru.govno.client.module.Module;
import ru.govno.client.module.settings.ModeSettings;
import ru.govno.client.utils.InventoryUtil;
import ru.govno.client.utils.Math.MathUtils;
import ru.govno.client.utils.Render.ColorUtils;
import ru.govno.client.utils.Render.RenderUtils;

public class ClickTeleport extends Module {
   public static ClickTeleport get;
   ModeSettings Mode;
   BlockPos posBlock = null;

   public ClickTeleport() {
      super("ClickTeleport", 0, Module.Category.MOVEMENT);
      get = this;
      this.settings.add(this.Mode = new ModeSettings("Mode", "Matrix", this, new String[]{"Matrix", "Vanilla", "Spartan", "Matrix&AAC"}));
   }

   @EventTarget
   public void onRender3D(Event3D event) {
      EntityLivingBase base = MathUtils.getPointedEntity(new Vector2f(Minecraft.player.rotationYaw, Minecraft.player.rotationPitch), 200.0, 1.0F, false);
      AxisAlignedBB axis = null;
      if (this.actived && this.posBlock != null && Minecraft.player != null && base == null) {
         axis = mc.world.getBlockState(this.posBlock).getSelectedBoundingBox(mc.world, this.posBlock);
      }

      if (this.actived && Minecraft.player != null && base != null) {
         axis = base.getRenderBoundingBox();
      }

      if (axis != null) {
         AxisAlignedBB box = axis;
         RenderUtils.setup3dForBlockPos(() -> RenderUtils.drawCanisterBox(box, true, false, true, -1, 0, ColorUtils.getColor(255, 255, 255, 50)), false);
      }
   }

   public static void matrixAACTp(double xe, double ye, double ze) {
      double dY = (double)((int)Math.abs(ye - Minecraft.player.posY));
      double dx = Math.abs(xe - Minecraft.player.posX);
      double dz = Math.abs(ze - Minecraft.player.posZ);
      double dzx = Math.sqrt(dx * dx + dz * dz);
      double maxXZSpeed = Minecraft.player.capabilities.isFlying ? 1.1993 : 0.25;
      double maxYSpeed = 10.0;
      int xzCrate = (int)(dzx / maxXZSpeed + 1.0);
      int yCrate = (int)(dY / maxYSpeed);
      double mw = (double)Minecraft.player.width;
      double mh = (double)Minecraft.player.height;
      AxisAlignedBB aabb = null;
      boolean xzIsFirst = true;

      for (int indexXZ = 0; indexXZ < xzCrate; indexXZ++) {
         float circlePC = (float)indexXZ / (float)xzCrate;
         double x = MathUtils.lerp(Minecraft.player.posX, xe, (double)circlePC);
         double z = MathUtils.lerp(Minecraft.player.posZ, ze, (double)circlePC);
         double y = Minecraft.player.posY;
         aabb = new AxisAlignedBB(x - mw / 2.0, y, z - mw / 2.0, x + mw / 2.0, y + mh, z + mw / 2.0);
         if (!mc.world.getCollisionBoxes(Minecraft.player, aabb).isEmpty()) {
            xzIsFirst = false;
            break;
         }
      }

      boolean sendSneak = Minecraft.player.isSneaking() || Minecraft.player.isNewSneak;
      if (xzIsFirst) {
         for (int indexXZx = 0; indexXZx < xzCrate; indexXZx++) {
            double ciclePC = (double)indexXZx / (double)xzCrate;
            double x = MathUtils.lerp(Minecraft.player.posX, xe, ciclePC);
            double z = MathUtils.lerp(Minecraft.player.posZ, ze, ciclePC);
            Minecraft.player.connection.sendPacket(new Position(x, Minecraft.player.posY, z, Minecraft.player.onGround));
         }

         Minecraft.player.connection.sendPacket(new Position(xe, Minecraft.player.posY, ze, Minecraft.player.onGround));

         for (int indexY = 0; indexY < yCrate; indexY++) {
            Minecraft.player.connection.sendPacket(new CPacketPlayer(Minecraft.player.onGround));
         }

         Minecraft.player.connection.sendPacket(new Position(xe, ye, ze, Minecraft.player.onGround));
      } else {
         for (int indexY = 0; indexY < yCrate; indexY++) {
            Minecraft.player.connection.sendPacket(new CPacketPlayer(Minecraft.player.onGround));
         }

         Minecraft.player.connection.sendPacket(new Position(xe, ye, ze, Minecraft.player.onGround));

         for (int indexXZx = 0; indexXZx < xzCrate; indexXZx++) {
            double ciclePC = (double)indexXZx / (double)xzCrate;
            double x = MathUtils.lerp(Minecraft.player.posX, xe, ciclePC);
            double z = MathUtils.lerp(Minecraft.player.posZ, ze, ciclePC);
            Minecraft.player.connection.sendPacket(new Position(x, Minecraft.player.posY, z, Minecraft.player.onGround));
         }

         Minecraft.player.connection.sendPacket(new Position(xe, Minecraft.player.posY, ze, Minecraft.player.onGround));
      }

      if (sendSneak) {
         Minecraft.getMinecraft().getConnection().preSendPacket(new CPacketEntityAction(Minecraft.player, Action.START_SNEAKING));
      }

      Minecraft.player.connection.sendPacket(new Position(xe, ye, ze, Minecraft.player.onGround));
      if (sendSneak) {
         Minecraft.getMinecraft().getConnection().sendPacket(new CPacketEntityAction(Minecraft.player, Action.STOP_SNEAKING), 100);
      }

      Minecraft.player.setPositionAndUpdate(xe, ye, ze);
   }

   public static void matrixTp(double x, double y, double z, boolean canElytra) {
      float f = Minecraft.player.rotationYaw * (float) (Math.PI / 180.0);
      double h = Minecraft.player.getDistance(Minecraft.player.posX + x, Minecraft.player.posY, Minecraft.player.posZ + z);
      y += h / 100.0;
      int de = (int)MathUtils.clamp(Minecraft.player.getDistance(x, y, z) / 11.0, 1.0, 17.0);
      int de2 = (int)(Math.abs(y / 11.0) + Math.abs(h / 2.5));
      boolean elytraEquiped = ((ItemStack)Minecraft.player.inventory.armorInventory.get(2)).getItem() == Items.ELYTRA;
      if (canElytra) {
         for (int i = 0; i < MathUtils.clamp(de2, 1, 17); i++) {
            Minecraft.player.connection.sendPacket(new CPacketPlayer(false));
         }

         if (elytraEquiped) {
            Minecraft.player.connection.sendPacket(new Position(Minecraft.player.posX, Minecraft.player.posY, Minecraft.player.posZ, false));
            Minecraft.player.connection.sendPacket(new Position(Minecraft.player.posX, Minecraft.player.posY, Minecraft.player.posZ, false));
            Minecraft.player.connection.sendPacket(new CPacketEntityAction(Minecraft.player, Action.START_FALL_FLYING));
            Minecraft.player.connection.sendPacket(new Position(Minecraft.player.posX + x, Minecraft.player.posY + y, Minecraft.player.posZ + z, false));
            Minecraft.player.connection.sendPacket(new CPacketEntityAction(Minecraft.player, Action.START_FALL_FLYING));
         } else {
            int elytra = InventoryUtil.getElytra();
            if (elytra != -1) {
               mc.playerController.windowClick(0, elytra < 9 ? elytra + 36 : elytra, 1, ClickType.PICKUP, Minecraft.player);
               mc.playerController.windowClick(0, 6, 1, ClickType.PICKUP, Minecraft.player);
            }

            Minecraft.player.connection.sendPacket(new Position(Minecraft.player.posX, Minecraft.player.posY, Minecraft.player.posZ, false));
            Minecraft.player.connection.sendPacket(new Position(Minecraft.player.posX, Minecraft.player.posY, Minecraft.player.posZ, false));
            Minecraft.player.connection.sendPacket(new CPacketEntityAction(Minecraft.player, Action.START_FALL_FLYING));
            Minecraft.player.connection.sendPacket(new Position(Minecraft.player.posX + x, Minecraft.player.posY + y, Minecraft.player.posZ + z, false));
            Minecraft.player.connection.sendPacket(new CPacketEntityAction(Minecraft.player, Action.START_FALL_FLYING));
            if (elytra != -1) {
               mc.playerController.windowClick(0, 6, 1, ClickType.PICKUP, Minecraft.player);
               mc.playerController.windowClick(0, elytra < 9 ? elytra + 36 : elytra, 1, ClickType.PICKUP, Minecraft.player);
            }
         }

         Minecraft.player.setPositionAndUpdate(Minecraft.player.posX + x, Minecraft.player.posY + y, Minecraft.player.posZ + z);
      } else {
         for (int i = 0; i < MathUtils.clamp(de2 + 1, 0, 19); i++) {
            Minecraft.player.connection.sendPacket(new CPacketPlayer(false));
         }

         Minecraft.player.connection.sendPacket(new Position(Minecraft.player.posX + x, Minecraft.player.posY + y, Minecraft.player.posZ + z, false));
         Minecraft.player.setPositionAndUpdate(Minecraft.player.posX + x, Minecraft.player.posY + y, Minecraft.player.posZ + z);
      }

      Minecraft.player.swingArm(EnumHand.MAIN_HAND);
   }

   void teleport(double x, double y, double z) {
      if (this.Mode.getMode().equalsIgnoreCase("Vanilla")) {
         Minecraft.player.setPositionAndUpdate(x, y, z);
      } else if (this.Mode.getMode().equalsIgnoreCase("Spartan")) {
         y -= 2.0;
         Minecraft.player.connection.sendPacket(new CPacketEntityAction(Minecraft.player, Action.START_SNEAKING));
         Minecraft.player.connection.sendPacket(new Position(x, y - 1.0, z, false));
         Minecraft.player.connection.sendPacket(new Position(x, y, z, false));
         Minecraft.player.connection.sendPacket(new Position(x, 1.0, z, false));
         Minecraft.player.connection.sendPacket(new Position(x, y, z, false));
         Minecraft.player.connection.sendPacket(new Position(x, y + 0.42, z, true));
         Minecraft.player.connection.sendPacket(new Position(x, y, z, false));
         Minecraft.player.connection.sendPacket(new CPacketEntityAction(Minecraft.player, Action.STOP_SNEAKING));
      }

      if (this.Mode.getMode().equalsIgnoreCase("Matrix")) {
         boolean trouble = true;

         for (int i = 0; i < 45; i++) {
            ItemStack itemStack = Minecraft.player.inventoryContainer.getSlot(i).getStack();
            if (itemStack.getItem() == Items.ELYTRA) {
               trouble = false;
            }
         }

         boolean elytra = !trouble;
         matrixTp(x - Minecraft.player.posX, y - Minecraft.player.posY, z - Minecraft.player.posZ, elytra);
      }

      if (this.Mode.getMode().equalsIgnoreCase("Matrix&AAC")) {
         matrixAACTp(x, y, z);
      }
   }

   @EventTarget
   public void onClick(EventInput e) {
      if (e.getKey() == 1) {
         if (this.posBlock == null || mc.currentScreen != null) {
            return;
         }

         EntityLivingBase base = MathUtils.getPointedEntity(new Vector2f(Minecraft.player.rotationYaw, Minecraft.player.rotationPitch), 100.0, 1.0F, false);
         if (base != null) {
            this.teleport(base.posX, base.posY + (this.Mode.getMode().equalsIgnoreCase("Matrix&AAC") ? 0.0 : 0.012), base.posZ);
         } else {
            int y = this.posBlock.getY();
            AxisAlignedBB aabbBlock = mc.world.getBlockState(this.posBlock).getSelectedBoundingBox(mc.world, this.posBlock);
            this.teleport(
               (double)((float)this.posBlock.getX() + 0.5F),
               MathUtils.clamp((double)y + (this.Mode.getMode().contains("Matrix") ? aabbBlock.maxY - aabbBlock.minY : 0.0), 5.0, 256.0),
               (double)((float)this.posBlock.getZ() + 0.5F)
            );
         }
      }
   }

   @Override
   public void onUpdate() {
      this.posBlock = mc.objectMouseOver.getBlockPos();
      if (this.posBlock != null) {
         BlockPos e = this.posBlock;

         for (int i = 256; i > 0; i--) {
            Material material = mc.world.getBlockState(e).getMaterial();
            boolean isReplacelable = material.isReplaceable();
            if (isReplacelable) {
               e = e.down();
            }
         }

         this.posBlock = e;
      }
   }
}
