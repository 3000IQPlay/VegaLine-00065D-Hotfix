package ru.govno.client.utils.Command.impl;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import ru.govno.client.Client;
import ru.govno.client.module.modules.ElytraBoost;
import ru.govno.client.module.modules.Speed;
import ru.govno.client.utils.Command.Command;
import ru.govno.client.utils.Math.MathUtils;
import ru.govno.client.utils.Math.TimerHelper;

// TODO: Translate

public class Clip extends Command {
   private static final Minecraft mc = Minecraft.getMinecraft();
   static double ty = 0.0;
   static double th = 0.0;
   static boolean ely = false;
   static int grace = 0;
   static TimerHelper timer = new TimerHelper();

   public Clip() {
      super("Clip", new String[]{"vclip", "hclip", "dclip", "vc", "hc", "dc", "up", "down", "bd"});
   }

   public static void onClipUpdate() {
      if (timer.hasReached(450.0)) {
         if ((th != 0.0 || ty != 0.0) && grace > 0) {
            grace--;
            goClip(MathUtils.clamp(ty, -200.0, 200.0), MathUtils.clamp(th, -10.0, 10.0), ely);
            ty = ty - MathUtils.clamp(ty, -200.0, 200.0);
            th = th - MathUtils.clamp(th, -10.0, 10.0);
            timer.reset();
            Minecraft.player.motionY = -0.02;
         }
      } else if (grace > 0) {
         Minecraft.player.motionY = 0.0;
      }
   }

   public static void runClip(double y, double h, boolean canElytra) {
      grace = 1 + (int)(Math.abs(y) / 200.0) + (int)(Math.abs(h) / 10.0 + 1.0E-45);
      ty = y;
      th = h;
      ely = canElytra;
   }

   @Override
   public void onCommand(String[] args) {
      try {
         if (Minecraft.player == null) {
            Client.msg("§d§lClip:§r §7Ваш персонаж ещё не иницилизировался.", false);
            return;
         }

         boolean trouble = true;

         for (int i = 0; i < 45; i++) {
            ItemStack itemStack = Minecraft.player.inventoryContainer.getSlot(i).getStack();
            if (itemStack.getItem() == Items.ELYTRA) {
               trouble = false;
            }
         }

         boolean elytra = !trouble;
         String ClipANDEclip = elytra ? "§d§lEClip:§r " : "§d§lClip:§r ";
         double y = 0.0;
         double h = 0.0;
         if (args[0].equalsIgnoreCase("vclip") || args[0].equalsIgnoreCase("vc") || args[0].equals("down") || args[0].equals("up") || args[0].equals("bd")) {
            if (!args[0].equals("down") && !args[0].equals("up") && !args[0].equals("bd")) {
               y = Double.valueOf(args[1]);
            } else {
               if (args[0].equals("down")) {
                  int x = (int)Math.floor(Minecraft.player.posX);
                  int z = (int)Math.floor(Minecraft.player.posZ);

                  for (int yS = 0; yS < (int)Minecraft.player.posY; yS++) {
                     if (Speed.posBlock((double)x, (double)yS, (double)z)
                        && !Speed.posBlock((double)x, (double)(yS - 1), (double)z)
                        && !Speed.posBlock((double)x, (double)(yS - 2), (double)z)) {
                        y = (double)(yS - 2) - Minecraft.player.posY + 0.2;
                     }

                     y -= 0.01F;
                  }

                  if (!mc.isSingleplayer()
                     && mc.getCurrentServerData() != null
                     && mc.getCurrentServerData().serverIP != null
                     && mc.getCurrentServerData().serverIP.equalsIgnoreCase("mc.reallyworld.ru")) {
                     y--;
                  }

                  if (y < -Minecraft.player.posY) {
                     Client.msg("§d§lClip:§r §7нет свободного места.", false);
                     return;
                  }
               }

               if (args[0].equals("up")) {
                  int VerticalRange = 200;

                  for (float ix = 0.0F; ix < (float)VerticalRange; ix += 0.005F) {
                     if (mc.world
                              .getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY + (double)ix + 1.0, Minecraft.player.posZ))
                              .getBlock()
                           == Blocks.AIR
                        && (
                           mc.world
                                    .getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY + (double)ix + 0.005, Minecraft.player.posZ))
                                    .getBlock()
                                 == Blocks.AIR
                              || mc.world
                                    .getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY + (double)ix + 0.005, Minecraft.player.posZ))
                                    .getBlock()
                                 == Blocks.WATER
                              || mc.world
                                    .getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY + (double)ix + 0.005, Minecraft.player.posZ))
                                    .getBlock()
                                 == Blocks.LAVA
                              || mc.world
                                    .getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY + (double)ix + 0.005, Minecraft.player.posZ))
                                    .getBlock()
                                 == Blocks.WEB
                              || mc.world
                                    .getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY + (double)ix, Minecraft.player.posZ))
                                    .getBlock()
                                 == Blocks.TRAPDOOR
                              || mc.world
                                    .getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY + (double)ix, Minecraft.player.posZ))
                                    .getBlock()
                                 == Blocks.IRON_TRAPDOOR
                        )
                        && mc.world
                              .getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY + (double)ix - 0.002, Minecraft.player.posZ))
                              .getBlock()
                           != Blocks.AIR
                        && ix > 2.0F
                        && mc.world.getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY - 0.1, Minecraft.player.posZ)).getBlock()
                           != Blocks.WATER) {
                        if (mc.gameSettings.keyBindJump.isKeyDown()) {
                           Minecraft.player.onGround = true;
                           Minecraft.player.motionY = 0.0;
                        }

                        y = (double)ix;
                        y += 0.01;
                        if (!mc.isSingleplayer()
                           && mc.getCurrentServerData() != null
                           && mc.getCurrentServerData().serverIP != null
                           && mc.getCurrentServerData().serverIP.equalsIgnoreCase("mc.reallyworld.ru")) {
                           y++;
                        }

                        if (mc.world.getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY + y - 0.49, Minecraft.player.posZ)).getBlock()
                           == Blocks.WATER) {
                           y--;
                        }
                     }
                  }
               }

               if (args[0].equals("bd")) {
                  y = MathUtils.roundPROBLYA((float)((double)(-Minecraft.player.height) - Minecraft.player.posY - 0.01F), 0.01F);
               }
            }

            if (args[0].equals("bd") && y <= 0.0) {
               Client.msg(ClipANDEclip + "§7Вы тепнулись под бедрок.", false);
            } else {
               Client.msg(
                  ClipANDEclip
                     + "§7Вы тепнулись на §b"
                     + String.format("%.1f", Double.valueOf(y) > 0.0 ? Double.valueOf(y) : -Double.valueOf(y))
                     + "§7 блоков "
                     + (Double.valueOf(y) > 0.0 ? "вверх." : "вниз."),
                  false
               );
            }
         }

         if (args[0].equalsIgnoreCase("hclip") || args[0].equalsIgnoreCase("hc")) {
            h = Double.valueOf(args[1]);
            Client.msg(
               ClipANDEclip
                  + "§7Вы тепнулись на §b"
                  + String.format("%.1f", Double.valueOf(y) > 0.0 ? Double.valueOf(y) : -Double.valueOf(y))
                  + "§7 блоков "
                  + (Double.valueOf(args[1]) > 0.0 ? "вперёд." : "назад."),
               false
            );
         }

         if (args[0].equalsIgnoreCase("dclip") || args[0].equalsIgnoreCase("dc")) {
            y = Double.valueOf(args[1]);
            h = Double.valueOf(args[2]);
            Client.msg(
               ClipANDEclip
                  + "§7Вы тепнулись на §b"
                  + String.format("%.1f", Double.valueOf(y) > 0.0 ? Double.valueOf(y) : -Double.valueOf(y))
                  + "Y : "
                  + (Double.valueOf(args[2]) > 0.0 ? Double.valueOf(args[2]) : -Double.valueOf(args[2]))
                  + "XZ§7 блоков диагонально.",
               false
            );
         }

         if (y != 0.0 || h != 0.0) {
            runClip(y, h, elytra);
            if (MathUtils.getDifferenceOf(y, Minecraft.player.posY) > 100.0
               || MathUtils.getDifferenceOf(h, Math.sqrt(Minecraft.player.posX * Minecraft.player.posX + Minecraft.player.posZ * Minecraft.player.posZ)) > 60.0
               )
             {
               mc.renderGlobal.loadRenderers();
            }
         }
      } catch (Exception var12) {
         Client.msg("§d§lClip:§r §7Комманда написана неверно.", false);
         Client.msg("§d§lClip:§r §7vclip: vclip/vc [§ly+§r§7]", false);
         Client.msg("§d§lClip:§r §7up/down/bd", false);
         Client.msg("§d§lClip:§r §7hclip: hclip/hc [§lh+§r§7]", false);
         Client.msg("§d§lClip:§r §7dclip: dclip/dc [§lv+,h+§r§7]", false);
         var12.printStackTrace();
      }
   }

   public static void goClip(double y, double h, boolean canElytra) {
      boolean isRW = !mc.isSingleplayer()
         && mc.getCurrentServerData() != null
         && mc.getCurrentServerData().serverIP != null
         && mc.getCurrentServerData().serverIP.equalsIgnoreCase("mc.reallyworld.ru");
      if (!isRW) {
         BlockPos pos = null;
         List<BlockPos> mixPoses = new CopyOnWriteArrayList<>();
         Vec3d ePos = new Vec3d(Minecraft.player.posX, Minecraft.player.posY, Minecraft.player.posZ);
         float r = 4.1F;

         for (float xs = -4.1F; xs < 4.1F; xs++) {
            for (float ys = -4.1F; ys < 1.0F; ys++) {
               for (float zs = -4.1F; zs < 4.1F; zs++) {
                  BlockPos poss = new BlockPos((double)xs + ePos.xCoord, (double)ys + ePos.yCoord, (double)zs + ePos.zCoord);
                  Block block = mc.world.getBlockState(poss).getBlock();
                  if (block != Blocks.AIR
                     && block != Blocks.BARRIER
                     && block != Blocks.BEDROCK
                     && poss != null
                     && Minecraft.player.getDistanceAtEye((double)poss.getX(), (double)poss.getY(), (double)poss.getZ()) <= 4.1F) {
                     mixPoses.add(poss);
                  }
               }
            }
         }

         if (mixPoses.size() != 0) {
            mixPoses.sort(Comparator.comparing(current -> mc.world.getBlockState(current).getBlockHardness(mc.world, current)));
            pos = mixPoses.get(0);
            if (pos != null) {
               Minecraft.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, pos, EnumFacing.UP));
               Minecraft.player.connection.sendPacket(new CPacketAnimation(EnumHand.OFF_HAND));
               Minecraft.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, pos, EnumFacing.UP));
            }
         }
      }

      y += h / 100.0;
      y = MathUtils.clamp(y, -200.0, 200.0);
      float f = Minecraft.player.rotationYaw * (float) (Math.PI / 180.0);
      double x = -((double)MathHelper.sin(f) * h);
      double z = (double)MathHelper.cos(f) * h;
      int de = (int)(Math.abs(y / 10.0) + Math.abs(h));

      for (int i = 0; i < MathUtils.clamp(de - (Math.abs(y) == 20.0 ? 1 : 0), 0, 19); i++) {
         Minecraft.player.connection.sendPacket(new CPacketPlayer(false));
      }

      if ((canElytra || Minecraft.player.inventory.armorInventory.get(2).getItem() == Items.ELYTRA) && !Minecraft.player.isElytraFlying()) {
         ElytraBoost.eq();
         if (!Minecraft.player.onGround) {
            Minecraft.player.connection.sendPacket(new CPacketPlayer(Minecraft.player.onGround));
         }

         if (Minecraft.player.onGround) {
            Minecraft.player.connection.sendPacket(new CPacketPlayer(false));
         }

         ElytraBoost.badPacket();
         ElytraBoost.deq();
         Minecraft.player.connection.sendPacket(new CPacketPlayer(true));
      }

      Minecraft.player
         .connection
         .sendPacket(new CPacketPlayer.Position(Minecraft.player.posX + x, Minecraft.player.posY + y, Minecraft.player.posZ + z, false));
      Minecraft.player.setPosition(Minecraft.player.posX + x, Minecraft.player.posY + y, Minecraft.player.posZ + z);
   }
}
