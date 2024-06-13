package ru.govno.client.module.modules;

import com.mojang.realmsclient.gui.ChatFormatting;
import java.awt.Color;
import java.util.ArrayList;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.network.play.server.SPacketMultiBlockChange;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import ru.govno.client.event.EventTarget;
import ru.govno.client.event.events.Event3D;
import ru.govno.client.event.events.EventReceivePacket;
import ru.govno.client.event.events.EventRender2D;
import ru.govno.client.module.Module;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.module.settings.FloatSettings;
import ru.govno.client.module.settings.ModeSettings;
import ru.govno.client.newfont.CFontRenderer;
import ru.govno.client.newfont.Fonts;
import ru.govno.client.utils.Render.ColorUtils;
import ru.govno.client.utils.Render.RenderUtils;

public class Xray extends Module {
   ArrayList<BlockPos> ores = new ArrayList<>();
   ArrayList<BlockPos> toCheck = new ArrayList<>();
   public static int done;
   public static int all;
   public static Xray get;
   public ModeSettings XrayMode;
   public FloatSettings CheckSpeed;
   public FloatSettings RadiusHorizontal;
   public FloatSettings RadiusVertical;
   public BoolSettings Diamond;
   public BoolSettings Redstone;
   public BoolSettings Emerald;
   public BoolSettings Quartz;
   public BoolSettings Lapis;
   public BoolSettings Gold;
   public BoolSettings Iron;
   public BoolSettings Coal;

   public Xray() {
      super("Xray", 0, Module.Category.PLAYER);
      this.settings.add(this.XrayMode = new ModeSettings("XrayMode", "Default", this, new String[]{"Default", "BrutForce"}));
      this.settings
         .add(this.CheckSpeed = new FloatSettings("CheckSpeed", 3.0F, 10.0F, 1.0F, this, () -> this.currentMode("XrayMode").equalsIgnoreCase("BrutForce")));
      this.settings
         .add(
            this.RadiusHorizontal = new FloatSettings(
               "RadiusHorizontal", 20.0F, 100.0F, 0.0F, this, () -> this.currentMode("XrayMode").equalsIgnoreCase("BrutForce")
            )
         );
      this.settings
         .add(
            this.RadiusVertical = new FloatSettings("RadiusVertical", 8.0F, 30.0F, 0.0F, this, () -> this.currentMode("XrayMode").equalsIgnoreCase("BrutForce"))
         );
      this.settings.add(this.Diamond = new BoolSettings("Diamond", true, this));
      this.settings.add(this.Redstone = new BoolSettings("Redstone", false, this));
      this.settings.add(this.Emerald = new BoolSettings("Emerald", false, this));
      this.settings.add(this.Quartz = new BoolSettings("Quartz", false, this));
      this.settings.add(this.Lapis = new BoolSettings("Lapis", false, this));
      this.settings.add(this.Gold = new BoolSettings("Gold", true, this));
      this.settings.add(this.Iron = new BoolSettings("Iron", true, this));
      this.settings.add(this.Coal = new BoolSettings("Coal", false, this));
      get = this;
   }

   public static IBlockState getState(BlockPos pos) {
      return mc.world.getBlockState(pos);
   }

   public static ArrayList<BlockPos> getAllInBox(BlockPos from, BlockPos to) {
      ArrayList<BlockPos> blocks = new ArrayList<>();
      BlockPos min = new BlockPos(Math.min(from.getX(), to.getX()), Math.min(from.getY(), to.getY()), Math.min(from.getZ(), to.getZ()));
      BlockPos max = new BlockPos(Math.max(from.getX(), to.getX()), Math.max(from.getY(), to.getY()), Math.max(from.getZ(), to.getZ()));

      for (int x = min.getX(); x <= max.getX(); x++) {
         for (int y = min.getY(); y <= max.getY(); y++) {
            for (int z = min.getZ(); z <= max.getZ(); z++) {
               blocks.add(new BlockPos(x, y, z));
            }
         }
      }

      return blocks;
   }

   @Override
   public void onToggled(boolean actived) {
      this.ores.clear();
      this.toCheck.clear();
      if (this.XrayMode.getMode().equalsIgnoreCase("BrutForce")) {
         int radXZ = (int)this.RadiusHorizontal.getFloat();
         int radY = (int)this.RadiusVertical.getFloat();

         for (BlockPos pos : this.getBlocks(radXZ, radY, radXZ)) {
            IBlockState state = getState(pos);
            if (this.isCheckableOre(Block.getIdFromBlock(state.getBlock()))) {
               this.toCheck.add(pos);
            }
         }
      }

      all = this.toCheck.size();
      done = 0;
      if (this.XrayMode.getMode().equalsIgnoreCase("Default")) {
         mc.renderGlobal.loadRenderers();
      }

      super.onToggled(actived);
   }

   @Override
   public String getDisplayName() {
      return this.actived && !this.XrayMode.getMode().equalsIgnoreCase("Default")
         ? this.getDisplayByDouble((double)done / (double)all * 100.0) + "%"
         : this.getName();
   }

   @EventTarget
   @Override
   public void onUpdate() {
      if (this.actived) {
         if (this.XrayMode.getMode().equalsIgnoreCase("BrutForce")) {
            for (int i = 0; i < (int)this.currentFloatValue("CheckSpeed"); i++) {
               if (this.toCheck.size() < 1) {
                  return;
               }

               BlockPos pos = this.toCheck.remove(0);
               done++;
               mc.getConnection().sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, pos, EnumFacing.UP));
               mc.getConnection().sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, pos, EnumFacing.UP));
            }
         }
      }
   }

   @EventTarget
   public void onReceivePacket(EventReceivePacket e) {
      if (this.actived) {
         if (this.currentMode("XrayMode").equalsIgnoreCase("BrutForce")) {
            if (e.getPacket() instanceof SPacketBlockChange) {
               SPacketBlockChange p = (SPacketBlockChange)e.getPacket();
               if (this.isEnabledOre(Block.getIdFromBlock(p.getBlockState().getBlock()))) {
                  this.ores.add(p.getBlockPosition());
               }
            } else if (e.getPacket() instanceof SPacketMultiBlockChange) {
               SPacketMultiBlockChange p = (SPacketMultiBlockChange)e.getPacket();

               for (SPacketMultiBlockChange.BlockUpdateData dat : p.getChangedBlocks()) {
                  if (this.isEnabledOre(Block.getIdFromBlock(dat.getBlockState().getBlock()))) {
                     this.ores.add(dat.getPos());
                  }
               }
            }
         }
      }
   }

   @EventTarget
   public void onEvent(Event3D e) {
      if (this.actived) {
         if (this.currentMode("XrayMode").equalsIgnoreCase("BrutForce")) {
            if (this.ores.isEmpty()) {
               return;
            }

            RenderUtils.setup3dForBlockPos(() -> this.ores.forEach(pos -> {
                  IBlockState state = getState(pos);
                  Block mat = state.getBlock();
                  int color = 0;
                  switch (Block.getIdFromBlock(mat)) {
                     case 56:
                        color = ColorUtils.getColor(0, 255, 255);
                     case 14:
                        color = ColorUtils.getColor(255, 215, 0);
                     case 15:
                        color = ColorUtils.getColor(213, 213, 213);
                     case 129:
                        color = ColorUtils.getColor(0, 255, 77);
                     case 153:
                        color = ColorUtils.getColor(255, 255, 255);
                     case 73:
                        color = ColorUtils.getColor(255, 0, 0);
                     case 16:
                        color = ColorUtils.getColor(0, 0, 0);
                     case 21:
                        color = ColorUtils.getColor(38, 97, 156);
                     default:
                        if (color != 0) {
                           int c1 = ColorUtils.swapAlpha(color, 150.0F);
                           int c2 = ColorUtils.swapAlpha(color, 26.0F);
                           AxisAlignedBB axis = mc.world.getBlockState(pos).getSelectedBoundingBox(mc.world, pos);
                           RenderUtils.drawCanisterBox(axis, true, false, true, c1, 0, c2);
                        }
                  }
               }), false);
         }
      }
   }

   @EventTarget
   public void onRender2D(EventRender2D e) {
      if (this.actived) {
         if (this.currentMode("XrayMode").equalsIgnoreCase("BrutForce")) {
            float a = (float)done;
            float b = (float)all;
            ScaledResolution sr = new ScaledResolution(mc);
            int valuePercent = (int)(a / b * 100.0F);
            int value = (int)(b * 100.0F);
            int color = ColorUtils.blendColors(
                  new float[]{0.0F, 1.0F, 1.0F, 0.0F, 1.0F},
                  new Color[]{new Color(255, 0, 0), Color.GREEN, Color.YELLOW, Color.ORANGE, Color.RED},
                  a / b - 0.01F
               )
               .brighter()
               .getRGB();
            CFontRenderer font = Fonts.neverlose500_15;
            String text = ChatFormatting.LIGHT_PURPLE
               + "Produced: "
               + ChatFormatting.RESET
               + valuePercent
               + "%"
               + ChatFormatting.GRAY
               + " / "
               + ChatFormatting.GOLD
               + "Total: "
               + ChatFormatting.RED
               + "100%";
            RenderUtils.drawVGradientRect(
               (float)sr.getScaledWidth() / 2.0F - (float)(font.getStringWidth(text) / 2) - 6.0F,
               10.5F,
               (float)sr.getScaledWidth() / 2.0F + (float)(font.getStringWidth(text) / 2) + 6.0F,
               25.0F,
               ColorUtils.getColor(12, 12, 12),
               0
            );
            RenderUtils.drawGradientSideways(
               (double)((float)sr.getScaledWidth() / 2.0F - (float)(font.getStringWidth(text) / 2) - 4.5F),
               11.5,
               (double)((float)sr.getScaledWidth() / 2.0F + 5.5F),
               12.5,
               color,
               0
            );
            RenderUtils.drawGradientSideways(
               (double)((float)sr.getScaledWidth() / 2.0F - 5.5F),
               11.5,
               (double)((float)sr.getScaledWidth() / 2.0F + (float)(font.getStringWidth(text) / 2) + 4.5F),
               12.5,
               0,
               color
            );
            RenderUtils.drawVGradientRect(
               (float)sr.getScaledWidth() / 2.0F - (float)(font.getStringWidth(text) / 2) - 5.0F,
               12.0F,
               (float)sr.getScaledWidth() / 2.0F - (float)(font.getStringWidth(text) / 2) - 4.0F,
               18.0F,
               color,
               0
            );
            RenderUtils.drawVGradientRect(
               (float)sr.getScaledWidth() / 2.0F + (float)(font.getStringWidth(text) / 2) + 4.0F,
               12.0F,
               (float)sr.getScaledWidth() / 2.0F + (float)(font.getStringWidth(text) / 2) + 5.0F,
               18.0F,
               color,
               0
            );
            if (valuePercent == 100) {
               font.drawString(
                  text,
                  (double)((float)sr.getScaledWidth() / 2.0F - (float)(font.getStringWidth(text) / 2)),
                  16.5,
                  ColorUtils.TwoColoreffect(new Color(24, 125, 24), new Color(12, 255, 12), (double)Math.abs(System.currentTimeMillis() / 4L) / 150.0 + 0.1275)
                     .getRGB()
               );
            } else {
               font.drawString(text, (double)((float)sr.getScaledWidth() / 2.0F - (float)(font.getStringWidth(text) / 2)), 16.5, color);
            }
         }
      }
   }

   private boolean isCheckableOre(int id) {
      int check = 0;
      int check1 = 0;
      int check2 = 0;
      int check3 = 0;
      int check4 = 0;
      int check5 = 0;
      int check6 = 0;
      if (this.currentBooleanValue("Diamond") && id != 0) {
         check = 56;
      }

      if (this.currentBooleanValue("Gold") && id != 0) {
         check1 = 14;
      }

      if (this.currentBooleanValue("Iron") && id != 0) {
         check2 = 15;
      }

      if (this.currentBooleanValue("Emerald") && id != 0) {
         check3 = 129;
      }

      if (this.currentBooleanValue("Quartz") && id != 0) {
         check3 = 153;
      }

      if (this.currentBooleanValue("Redstone") && id != 0) {
         check4 = 73;
      }

      if (this.currentBooleanValue("Coal") && id != 0) {
         check5 = 16;
      }

      if (this.currentBooleanValue("Lapis") && id != 0) {
         check6 = 21;
      }

      return id == 0 ? false : id == check || id == check1 || id == check2 || id == check3 || id == check4 || id == check5 || id == check6;
   }

   private boolean isEnabledOre(int id) {
      int check = 0;
      int check1 = 0;
      int check2 = 0;
      int check3 = 0;
      int check4 = 0;
      int check5 = 0;
      int check6 = 0;
      if (this.currentBooleanValue("Diamond") && id != 0) {
         check = 56;
      }

      if (this.currentBooleanValue("Gold") && id != 0) {
         check1 = 14;
      }

      if (this.currentBooleanValue("Iron") && id != 0) {
         check2 = 15;
      }

      if (this.currentBooleanValue("Emerald") && id != 0) {
         check3 = 129;
      }

      if (this.currentBooleanValue("Quartz") && id != 0) {
         check = 153;
      }

      if (this.currentBooleanValue("Redstone") && id != 0) {
         check4 = 73;
      }

      if (this.currentBooleanValue("Coal") && id != 0) {
         check5 = 16;
      }

      if (this.currentBooleanValue("Lapis") && id != 0) {
         check6 = 21;
      }

      return id == 0 ? false : id == check || id == check1 || id == check2 || id == check3 || id == check4 || id == check5 || id == check6;
   }

   private ArrayList<BlockPos> getBlocks(int x, int y, int z) {
      BlockPos min = new BlockPos(Minecraft.player.posX - (double)x, Minecraft.player.posY - (double)y, Minecraft.player.posZ - (double)z);
      BlockPos max = new BlockPos(Minecraft.player.posX + (double)x, Minecraft.player.posY + (double)y, Minecraft.player.posZ + (double)z);
      return getAllInBox(min, max);
   }
}
