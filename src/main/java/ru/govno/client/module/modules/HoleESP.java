package ru.govno.client.module.modules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;
import ru.govno.client.module.Module;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.module.settings.ColorSettings;
import ru.govno.client.module.settings.ModeSettings;
import ru.govno.client.utils.Math.BlockUtils;
import ru.govno.client.utils.Math.MathUtils;
import ru.govno.client.utils.Render.AnimationUtils;
import ru.govno.client.utils.Render.ColorUtils;
import ru.govno.client.utils.Render.RenderUtils;

public class HoleESP extends Module {
   final List<HoleESP.Hole> holesList = new ArrayList<>();
   ModeSettings EspMode;
   ColorSettings ColorGood;
   ColorSettings ColorNormal;
   ColorSettings ColorOnPlayer;
   ColorSettings SidesColor;
   BoolSettings PlayersHolesides;
   private boolean callRenderSidePoses;
   private final AnimationUtils alphaMod = new AnimationUtils(0.0F, 0.0F, 0.1F);
   int checkTick = 0;
   private final List<HoleESP.HoledSide> allSides = Arrays.asList(HoleESP.HoledSide.XP, HoleESP.HoledSide.XM, HoleESP.HoledSide.ZP, HoleESP.HoledSide.ZM);
   private final List<HoleESP.RenderSidePos> renderSidePoses = new ArrayList<>();

   public HoleESP() {
      super("HoleESP", 0, Module.Category.RENDER);
      this.settings.add(this.EspMode = new ModeSettings("EspMode", "Box", this, new String[]{"Box", "Gradient"}));
      this.settings.add(this.ColorGood = new ColorSettings("ColorGood", ColorUtils.getColor(40, 255, 20, 155), this));
      this.settings.add(this.ColorNormal = new ColorSettings("ColorNormal", ColorUtils.getColor(205, 40, 255, 155), this));
      this.settings.add(this.ColorOnPlayer = new ColorSettings("ColorOnPlayer", ColorUtils.getColor(255, 185, 40, 155), this));
      this.settings.add(this.PlayersHolesides = new BoolSettings("PlayersHolesides", true, this));
      this.settings
         .add(this.SidesColor = new ColorSettings("SidesColor", ColorUtils.getColor(200, 145, 200, 175), this, () -> this.PlayersHolesides.getBool()));
   }

   @Override
   public void alwaysRender3D() {
      if ((double)this.alphaMod.getAnim() > 0.01) {
         this.draw3d(this.alphaMod.getAnim());
      }
   }

   @Override
   public void alwaysUpdate() {
      this.alphaMod.to = this.actived ? 1.0F : 0.0F;
      if ((double)this.alphaMod.getAnim() > 0.01) {
         this.checkTick++;
         if (this.checkTick % 5 == 0) {
            float[] ranges = this.getRanges();
            this.collectHolesArround(this.getEntityBlockPos(this.getMe()), ranges[0], ranges[1], ranges[2]);
         } else if (this.checkTick % 5 == 3) {
            this.endCollectHolesArround();
         }
      }

      this.callRenderSidePoses = this.ifCanSetupRenderSidePoses(this.PlayersHolesides.getBool(), 16.0);
   }

   private float[] getRanges() {
      return new float[]{9.0F, 2.0F, 2.0F};
   }

   private void drawHoleBox(HoleESP.Hole hole, float alphaPC, boolean isBoxRender, int color) {
      int colorOutline = ColorUtils.swapAlpha(color, (float)ColorUtils.getAlphaFromColor(color) * alphaPC * hole.alpha.getAnim());
      int colorFill = ColorUtils.swapAlpha(color, (float)ColorUtils.getAlphaFromColor(color) * (isBoxRender ? 0.2F : 0.4F) * alphaPC * hole.alpha.getAnim());
      AxisAlignedBB aabbHole = new AxisAlignedBB(hole.pos, (isBoxRender ? 0.1F : 0.7F) * hole.alpha.getAnim());
      RenderUtils.glColor(color);
      if (isBoxRender) {
         RenderUtils.drawCanisterBox(aabbHole, true, true, true, colorOutline, colorOutline, colorFill);
      } else {
         RenderUtils.drawGradientAlphaBox(aabbHole, true, true, colorOutline, colorFill);
      }
   }

   private void draw3d(float alphaPC) {
      if (this.holesList != null) {
         double glX = RenderManager.viewerPosX;
         double glY = RenderManager.viewerPosY;
         double glZ = RenderManager.viewerPosZ;
         GL11.glPushMatrix();
         GL11.glBlendFunc(770, 1);
         GL11.glEnable(3042);
         GL11.glLineWidth(1.0E-5F);
         GL11.glDisable(3553);
         GL11.glDisable(2929);
         GL11.glDisable(2896);
         mc.entityRenderer.disableLightmap();
         GL11.glShadeModel(7425);
         GL11.glTranslated(-glX, -glY, -glZ);
         boolean isBoxModeRender = this.EspMode.getMode().equalsIgnoreCase("Box");
         int colorGood = this.ColorGood.getCol();
         int colorNormal = this.ColorNormal.getCol();
         int colorPlayer = this.ColorOnPlayer.getCol();
         List<EntityOtherPlayerMP> otherPlayers = mc.world
            .getLoadedEntityList()
            .stream()
            .map(Entity::getOtherPlayerOf)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
         this.holesList.forEach(hole -> {
            boolean playerIn = otherPlayers.stream().anyMatch(otherPlayer -> this.getEntityBlockPos(otherPlayer).equals(hole.getPos()));
            int color = playerIn ? colorPlayer : (hole.good ? colorGood : colorNormal);
            this.drawHoleBox(hole, alphaPC, isBoxModeRender, color);
         });
         GL11.glTranslated(glX, glY, glZ);
         GL11.glLineWidth(1.0F);
         GL11.glShadeModel(7424);
         GL11.glEnable(3553);
         GL11.glEnable(2929);
         GlStateManager.enableAlpha();
         GlStateManager.enableBlend();
         GL11.glBlendFunc(770, 771);
         GlStateManager.resetColor();
         GL11.glPopMatrix();
      }

      if (this.callRenderSidePoses) {
         int colorise = this.currentColorValue("SidesColor");
         RenderUtils.setup3dForBlockPos(() -> {
            GlStateManager.disableCull();
            GlStateManager.disableAlpha();
            GL11.glEnable(2848);
            GL11.glHint(3154, 4354);
            GL11.glLineWidth(0.001F);
            int colorSide = ColorUtils.swapAlpha(colorise, (float)ColorUtils.getAlphaFromColor(colorise) * alphaPC);
            this.getRenderSidePoses().forEach(side -> side.drawSide(colorSide));
            GL11.glLineWidth(1.0F);
            GL11.glHint(3154, 4352);
            GL11.glDisable(2848);
            GlStateManager.enableAlpha();
            GlStateManager.enableCull();
         }, true);
      }
   }

   final EntityPlayer getMe() {
      return (EntityPlayer)(FreeCam.get.actived && FreeCam.fakePlayer != null ? FreeCam.fakePlayer : Minecraft.player);
   }

   final BlockPos getEntityBlockPos(Entity entity) {
      return new BlockPos(entity.posX, entity.posY, entity.posZ);
   }

   boolean isInRanges(BlockPos myPosition, BlockPos holePos, float rangeXZ, float rangeYplus, float rangeYminus) {
      return (float)myPosition.getY() + rangeYplus > (float)holePos.getY()
         && (float)myPosition.getY() - rangeYminus <= (float)holePos.getY()
         && this.getMe().getDistanceToVec3dXZ(new Vec3d((double)holePos.getX(), (double)holePos.getY(), (double)holePos.getZ())) <= (double)rangeXZ;
   }

   List<BlockPos> blocksZone(float rangeXZ, float rangeYplus, float rangeYminus) {
      List<BlockPos> poses = new CopyOnWriteArrayList<>();
      double xzRange = (double)rangeXZ;
      double yRangeUp = (double)rangeYplus;
      double yRangeDown = (double)rangeYminus;
      double xPos = (double)this.getEntityBlockPos(this.getMe()).getX();
      double yPos = (double)this.getEntityBlockPos(this.getMe()).getY();
      double zPos = (double)this.getEntityBlockPos(this.getMe()).getZ();
      BlockPos selfPos = this.getEntityBlockPos(this.getMe());

      for (double x = -xzRange; x < xzRange; x++) {
         for (double y = -yRangeDown; y < yRangeDown; y++) {
            for (double z = -xzRange; z < xzRange; z++) {
               poses.add(selfPos.add(x, y, z));
            }
         }
      }

      return poses;
   }

   private void collectHolesArround(BlockPos myPosition, float rangeXZ, float rangeYplus, float rangeYminus) {
      BlockPos selfPos = this.getEntityBlockPos(this.getMe());
      float[] ranges = this.getRanges();
      List<BlockPos> toAddHolesPoses = this.blocksZone(rangeXZ, rangeYplus, rangeYminus)
         .stream()
         .filter(pos -> !selfPos.equals(pos))
         .filter(pos -> this.isInRanges(selfPos, pos, ranges[0], ranges[1], ranges[2]))
         .filter(pos -> this.isHoled(pos, false, true))
         .filter(Objects::nonNull)
         .collect(Collectors.toList());
      if (this.holesList.size() < 35) {
         toAddHolesPoses.forEach(pos -> {
            HoleESP.Hole wearHole = new HoleESP.Hole(pos, this.isHoled(pos, true, true));
            if (!this.holesList.contains(wearHole)) {
               this.holesList.add(wearHole);
            }
         });
      }
   }

   private void endCollectHolesArround() {
      if (this.holesList != null && this.holesList.size() > 0) {
         BlockPos selfPos = this.getEntityBlockPos(this.getMe());
         float[] ranges = this.getRanges();
         List<HoleESP.Hole> toDetachHoles = this.holesList
            .stream()
            .filter(
               hole -> !this.isInRanges(selfPos, hole.pos, ranges[0], ranges[1], ranges[2])
                     || !this.isHoled(hole.pos, hole.good, true)
                     || selfPos.equals(hole.pos)
            )
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
         toDetachHoles.forEach(hole -> {
            hole.alpha.to = 0.0F;
            hole.toRemove = true;
         });
         this.holesList.removeIf(hole -> hole.toRemove && (double)hole.alpha.getAnim() < 0.01);
      }
   }

   final Block getBlock(BlockPos position) {
      return mc.world.getBlockState(position).getBlock();
   }

   private final boolean isBedrock(BlockPos position) {
      Block state = Blocks.BEDROCK;
      return this.getBlock(position) == state;
   }

   private final boolean isObsidian(BlockPos position) {
      Block state = Blocks.OBSIDIAN;
      return this.getBlock(position) == state;
   }

   private final boolean isCurrentBlock(BlockPos position, boolean goodHole) {
      return goodHole ? this.isBedrock(position) : this.isBedrock(position) || this.isObsidian(position);
   }

   private final boolean isHoled(BlockPos position, boolean goodHole, boolean up1) {
      Block state = Blocks.AIR;
      return this.isCurrentBlock(position.add(1, 0, 0), goodHole)
         && this.isCurrentBlock(position.add(-1, 0, 0), goodHole)
         && this.isCurrentBlock(position.add(0, 0, 1), goodHole)
         && this.isCurrentBlock(position.add(0, 0, -1), goodHole)
         && Speed.posBlock((double)position.add(0, -1, 0).getX(), (double)position.add(0, -1, 0).getY(), (double)position.add(0, -1, 0).getZ())
         && this.getBlock(position) == state
         && (this.getBlock(position.add(0, 1, 0)) == state && this.getBlock(position.add(0, 2, 0)) == state || !up1);
   }

   private boolean isHoledNoGoodEntity(EntityLivingBase base) {
      BlockPos pos = BlockUtils.getEntityBlockPos(base);
      return this.isHoled(pos, false, false);
   }

   private List<HoleESP.HoledSide> entityHoledSideList(EntityLivingBase base) {
      BlockPos pos = BlockUtils.getEntityBlockPos(base);
      return this.allSides.stream().filter(side -> this.isObsidian(pos.add(side.getAdded()))).collect(Collectors.toList());
   }

   private List<EntityLivingBase> targetEntitiesToHoleCheck(double range) {
      return mc.world
         .getLoadedEntityList()
         .stream()
         .map(Entity::getOtherPlayerOf)
         .filter(Objects::nonNull)
         .filter(EntityLivingBase::isEntityAlive)
         .filter(other -> this.isHoledNoGoodEntity(other))
         .filter(other -> (double)this.getMe().getSmoothDistanceToEntity(other) <= range)
         .collect(Collectors.toList());
   }

   private boolean ifCanSetupRenderSidePoses(boolean enabled, double range) {
      if (enabled) {
         this.renderSidePoses.clear();
         this.targetEntitiesToHoleCheck(range)
            .forEach(
               player -> this.entityHoledSideList(player)
                     .forEach(side -> this.renderSidePoses.add(new HoleESP.RenderSidePos(BlockUtils.getEntityBlockPos(player), side)))
            );
      } else if (!this.renderSidePoses.isEmpty()) {
         this.renderSidePoses.clear();
      }

      return enabled && !this.renderSidePoses.isEmpty();
   }

   private List<HoleESP.RenderSidePos> getRenderSidePoses() {
      return this.renderSidePoses;
   }

   public class Hole {
      BlockPos pos;
      boolean good;
      boolean toRemove = false;
      AnimationUtils alpha = new AnimationUtils(0.0F, 1.0F, 0.05F);

      @Override
      public boolean equals(Object obj) {
         return obj instanceof HoleESP.Hole hole ? hole.pos.equals(this.pos) : false;
      }

      public Hole(BlockPos pos, boolean good) {
         this.pos = pos;
         this.good = good;
      }

      public void setPos(BlockPos pos) {
         this.pos = pos;
      }

      public BlockPos getPos() {
         return this.pos;
      }
   }

   private static enum HoledSide {
      XP(new BlockPos(1, 0, 0)),
      XM(new BlockPos(-1, 0, 0)),
      ZP(new BlockPos(0, 0, 1)),
      ZM(new BlockPos(0, 0, -1));

      BlockPos added;

      private HoledSide(BlockPos added) {
         this.added = added;
      }

      BlockPos getAdded() {
         return this.added;
      }
   }

   private class RenderSidePos {
      BlockPos pos;
      HoleESP.HoledSide side;

      public RenderSidePos(BlockPos pos, HoleESP.HoledSide side) {
         this.pos = pos;
         this.side = side;
      }

      public BlockPos getPos() {
         return this.pos;
      }

      public HoleESP.HoledSide getSide() {
         return this.side;
      }

      public void drawSide(int color) {
         float animDelay = 800.0F;
         float timePC = (float)(System.currentTimeMillis() % (long)((int)(animDelay * 2.0F))) / (animDelay * 2.0F);
         float animPC = (float)MathUtils.easeInOutQuadWave((double)timePC);
         color = ColorUtils.swapAlpha(
            color, (float)ColorUtils.getAlphaFromColor(color) * (0.5F + (float)MathUtils.easeInOutQuad((double)(1.0F - animPC)) * 0.5F)
         );
         if (ColorUtils.getAlphaFromColor(color) >= 1) {
            RenderUtils.glColor(ColorUtils.swapAlpha(color, (float)ColorUtils.getAlphaFromColor(color) * 0.2F));
            float translatePC = 0.05F;
            float vMin = 0.5F - (0.5F + (1.0F - animPC) * translatePC / 2.0F);
            float vMax = 0.5F + 0.5F + (1.0F - animPC) * translatePC / 2.0F;
            GL11.glTranslated((double)this.getPos().getX(), (double)this.getPos().getY(), (double)this.getPos().getZ());
            GL11.glTranslated(0.5, 0.85, 0.5);
            GL11.glScaled(0.95, 0.075, 0.95);
            GL11.glTranslated(-0.5, -0.85, -0.5);
            GL11.glBegin(7);
            switch (this.getSide()) {
               case XP:
                  GL11.glVertex3d((double)vMax, (double)vMin, (double)vMin);
                  GL11.glVertex3d((double)vMax, (double)vMax, (double)vMin);
                  GL11.glVertex3d((double)vMax, (double)vMax, (double)vMax);
                  GL11.glVertex3d((double)vMax, (double)vMin, (double)vMax);
                  break;
               case XM:
                  GL11.glVertex3d((double)vMin, (double)vMin, (double)vMin);
                  GL11.glVertex3d((double)vMin, (double)vMax, (double)vMin);
                  GL11.glVertex3d((double)vMin, (double)vMax, (double)vMax);
                  GL11.glVertex3d((double)vMin, (double)vMin, (double)vMax);
                  break;
               case ZP:
                  GL11.glVertex3d((double)vMin, (double)vMin, (double)vMax);
                  GL11.glVertex3d((double)vMin, (double)vMax, (double)vMax);
                  GL11.glVertex3d((double)vMax, (double)vMax, (double)vMax);
                  GL11.glVertex3d((double)vMax, (double)vMin, (double)vMax);
                  break;
               case ZM:
                  GL11.glVertex3d((double)vMin, (double)vMin, (double)vMin);
                  GL11.glVertex3d((double)vMin, (double)vMax, (double)vMin);
                  GL11.glVertex3d((double)vMax, (double)vMax, (double)vMin);
                  GL11.glVertex3d((double)vMax, (double)vMin, (double)vMin);
            }

            GL11.glEnd();
            RenderUtils.glColor(color);
            GL11.glBegin(2);
            switch (this.getSide()) {
               case XP:
                  GL11.glVertex3d((double)vMax, (double)vMin, (double)vMin);
                  GL11.glVertex3d((double)vMax, (double)vMax, (double)vMin);
                  GL11.glVertex3d((double)vMax, (double)vMax, (double)vMax);
                  GL11.glVertex3d((double)vMax, (double)vMin, (double)vMax);
                  break;
               case XM:
                  GL11.glVertex3d((double)vMin, (double)vMin, (double)vMin);
                  GL11.glVertex3d((double)vMin, (double)vMax, (double)vMin);
                  GL11.glVertex3d((double)vMin, (double)vMax, (double)vMax);
                  GL11.glVertex3d((double)vMin, (double)vMin, (double)vMax);
                  break;
               case ZP:
                  GL11.glVertex3d((double)vMin, (double)vMin, (double)vMax);
                  GL11.glVertex3d((double)vMin, (double)vMax, (double)vMax);
                  GL11.glVertex3d((double)vMax, (double)vMax, (double)vMax);
                  GL11.glVertex3d((double)vMax, (double)vMin, (double)vMax);
                  break;
               case ZM:
                  GL11.glVertex3d((double)vMin, (double)vMin, (double)vMin);
                  GL11.glVertex3d((double)vMin, (double)vMax, (double)vMin);
                  GL11.glVertex3d((double)vMax, (double)vMax, (double)vMin);
                  GL11.glVertex3d((double)vMax, (double)vMin, (double)vMin);
            }

            GL11.glEnd();
            GL11.glTranslated(0.5, 0.85, 0.5);
            GL11.glScaled(1.0526315789473684, 13.333333333333334, 1.0526315789473684);
            GL11.glTranslated(-0.5, -0.85, -0.5);
            GL11.glTranslated((double)(-this.getPos().getX()), (double)(-this.getPos().getY()), (double)(-this.getPos().getZ()));
            GlStateManager.resetColor();
         }
      }
   }
}
