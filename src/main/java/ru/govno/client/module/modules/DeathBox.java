package ru.govno.client.module.modules;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector4d;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec2f;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;
import ru.govno.client.Client;
import ru.govno.client.module.Module;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.module.settings.FloatSettings;
import ru.govno.client.newfont.CFontRenderer;
import ru.govno.client.newfont.Fonts;
import ru.govno.client.utils.Math.MathUtils;
import ru.govno.client.utils.Render.AnimationUtils;
import ru.govno.client.utils.Render.ColorUtils;
import ru.govno.client.utils.Render.RenderUtils;

public class DeathBox extends Module {
   private final List<DeathBox.Box> DEATH_BOXES_LIST = new ArrayList<>();
   private final FloatSettings LifeTime;
   private final BoolSettings Players;
   private final BoolSettings Mobs;
   private final IntBuffer viewport = GLAllocation.createDirectIntBuffer(16);
   private final FloatBuffer modelview = GLAllocation.createDirectFloatBuffer(16);
   private final FloatBuffer projection = GLAllocation.createDirectFloatBuffer(16);
   private final FloatBuffer vector = GLAllocation.createDirectFloatBuffer(4);
   Tessellator tessellator = Tessellator.getInstance();
   BufferBuilder buffer = this.tessellator.getBuffer();

   public DeathBox() {
      super("DeathBox", 0, Module.Category.RENDER);
      this.settings.add(this.LifeTime = new FloatSettings("LifeTime", 15000.0F, 20000.0F, 2000.0F, this));
      this.settings.add(this.Players = new BoolSettings("Players", true, this));
      this.settings.add(this.Mobs = new BoolSettings("Mobs", false, this));
   }

   private Vector3d project2D(int scaleFactor, float x, float y, float z) {
      GL11.glGetFloat(2982, this.modelview);
      GL11.glGetFloat(2983, this.projection);
      GL11.glGetInteger(2978, this.viewport);
      return GLU.gluProject(x, y, z, this.modelview, this.projection, this.viewport, this.vector)
         ? new Vector3d(
            (double)(this.vector.get(0) / (float)scaleFactor),
            (double)(((float)Display.getHeight() - this.vector.get(1)) / (float)scaleFactor),
            (double)this.vector.get(2)
         )
         : null;
   }

   private Vec2f getNameTagPosVec(Vec3d vec3dPos, float pTicks, ScaledResolution sr, RenderManager renderMng) {
      Vec2f vec2f = new Vec2f(-1617.0F, -1617.0F);
      Vector3d[] vectors = new Vector3d[]{
         new Vector3d(vec3dPos.xCoord, vec3dPos.yCoord, vec3dPos.zCoord),
         new Vector3d(vec3dPos.xCoord, vec3dPos.yCoord, vec3dPos.zCoord),
         new Vector3d(vec3dPos.xCoord, vec3dPos.yCoord, vec3dPos.zCoord),
         new Vector3d(vec3dPos.xCoord, vec3dPos.yCoord, vec3dPos.zCoord),
         new Vector3d(vec3dPos.xCoord, vec3dPos.yCoord, vec3dPos.zCoord),
         new Vector3d(vec3dPos.xCoord, vec3dPos.yCoord, vec3dPos.zCoord),
         new Vector3d(vec3dPos.xCoord, vec3dPos.yCoord, vec3dPos.zCoord),
         new Vector3d(vec3dPos.xCoord, vec3dPos.yCoord, vec3dPos.zCoord)
      };
      mc.entityRenderer.setupCameraTransform(pTicks, 1);
      Vector4d position = null;
      Vector3d[] vecList = vectors;
      int vecLength = vectors.length;

      for (int l = 0; l < vecLength; l++) {
         Vector3d vector = this.project2D(
            ScaledResolution.getScaleFactor(),
            (float)(vecList[l].x - RenderManager.viewerPosX),
            (float)(vecList[l].y - RenderManager.viewerPosY),
            (float)(vecList[l].z - RenderManager.viewerPosZ)
         );
         if (vector != null && vector.z >= 0.0 && vector.z < 1.0) {
            if (position == null) {
               position = new Vector4d(vector.x, vector.y, vector.z, 0.0);
            }

            position.x = Math.min(vector.x, position.x);
            position.y = Math.min(vector.y, position.y);
            position.z = Math.max(vector.x, position.z);
            position.w = Math.max(vector.y, position.w);
         }
      }

      mc.entityRenderer.setupOverlayRendering();
      if (position != null) {
         double posX = position.x;
         double posY = position.y;
         double endPosX = position.z;
         vec2f.x = (float)(posX + (endPosX - posX) / 2.0);
         vec2f.y = (float)posY;
      }

      return vec2f;
   }

   private boolean nameTagVecSetSuccess(Vec2f vec2f) {
      return true;
   }

   private List<DeathBox.Box> getDeathBoxesList() {
      return this.DEATH_BOXES_LIST;
   }

   private Vec3d getEntityVec3dPosition(Entity entityIn) {
      return new Vec3d(entityIn.posX, entityIn.posY, entityIn.posZ);
   }

   private String getEntityCoordsString(Entity entityIn) {
      Vec3d pos = this.getEntityVec3dPosition(entityIn);
      return TextFormatting.DARK_AQUA
         + String.format("%.1f", (float)pos.xCoord)
         + TextFormatting.WHITE
         + ", "
         + TextFormatting.GREEN
         + String.format("%.1f", (float)pos.yCoord)
         + TextFormatting.WHITE
         + ", "
         + TextFormatting.RED
         + String.format("%.1f", (float)pos.zCoord);
   }

   private DeathBox.InfoDeathEntity getDeathInfoFromEntity(Entity entityIn) {
      return new DeathBox.InfoDeathEntity(entityIn.getName(), entityIn.getDisplayName().getUnformattedText(), this.getEntityCoordsString(entityIn));
   }

   private AxisAlignedBB getEntityBox(Entity entity) {
      Vec3d pos = this.getEntityVec3dPosition(entity);
      double width = (double)entity.width;
      double height = (double)entity.height;
      if (entity instanceof EntityPlayer player) {
         width = 0.6;
         height = player.hasNewVersionMoves && player.isLay ? 0.6 : (player.hasNewVersionMoves && player.isNewSneak ? 1.5 : (player.isSneaking() ? 1.65 : 1.8));
      } else {
         AxisAlignedBB aabb;
         if (entity != null && (aabb = entity.getEntityBoundingBox()) != null) {
            width = aabb.maxX - aabb.minX;
            height = aabb.maxY - aabb.minY;
         }
      }

      Vec3d first = new Vec3d(pos.xCoord - width / 2.0, pos.yCoord, pos.zCoord - width / 2.0);
      Vec3d second = new Vec3d(pos.xCoord + width / 2.0, pos.yCoord + height, pos.zCoord + width / 2.0);
      return new AxisAlignedBB(first, second);
   }

   private void processingDeathEntities(boolean players, boolean mobs) {
      mc.world
         .getLoadedEntityList()
         .forEach(
            entity -> {
               if (entity != null
                  && entity instanceof EntityLivingBase base
                  && base.getHealth() == 0.0F
                  && base.deathTime == 0
                  && (players && base instanceof EntityPlayer || mobs && !(base instanceof EntityPlayer))
                  && this.getDeathBoxesList().size() < 35) {
                  this.DEATH_BOXES_LIST.add(new DeathBox.Box(this.getEntityVec3dPosition(base), this.getEntityBox(base), this.getDeathInfoFromEntity(base)));
               }
            }
         );
   }

   private AxisAlignedBB getDeathBoxAxisBoxByVec(DeathBox.Box box, float alphaPC) {
      float dePC = 1.0F - (1.0F - alphaPC) / 1.5F;
      double diffX = box.box.maxX - box.box.minX;
      double diffY = box.box.maxY - box.box.minY;
      double diffZ = box.box.maxZ - box.box.minZ;
      return new AxisAlignedBB(
         box.pos.xCoord - diffX * (double)dePC / 2.0,
         box.pos.yCoord,
         box.pos.zCoord - diffZ * (double)dePC / 2.0,
         box.pos.xCoord + diffX * (double)dePC / 2.0,
         box.pos.yCoord + diffY,
         box.pos.zCoord + diffZ * (double)dePC / 2.0
      );
   }

   private static void setupDrawBox(Runnable render) {
      double glX = RenderManager.viewerPosX;
      double glY = RenderManager.viewerPosY;
      double glZ = RenderManager.viewerPosZ;
      GL11.glPushMatrix();
      GL11.glEnable(3042);
      GL11.glDisable(3553);
      GL11.glDepthMask(false);
      GL11.glEnable(2929);
      GL11.glShadeModel(7425);
      GL11.glTranslated(-glX, -glY, -glZ);
      GlStateManager.tryBlendFuncSeparate(
         GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
      );
      render.run();
      GlStateManager.tryBlendFuncSeparate(
         GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
      );
      GL11.glLineWidth(1.0F);
      GL11.glTranslated(glX, glY, glZ);
      GL11.glShadeModel(7424);
      GL11.glEnable(3553);
      GL11.glEnable(2929);
      GL11.glEnable(2929);
      GL11.glEnable(3042);
      GL11.glDepthMask(true);
      GL11.glPopMatrix();
   }

   private void drawProcessBox(DeathBox.Box box, float alphaPC) {
      if ((alphaPC = alphaPC * box.alphaPC.getAnim()) >= 0.03F) {
         AxisAlignedBB axisBB = this.getDeathBoxAxisBoxByVec(box, alphaPC);
         GL11.glEnable(2929);
         this.drawBoxFullGradient(axisBB, this.getClientColorsMassive(alphaPC * 0.75F), true, 2.0F);
         GL11.glDisable(2929);
         this.drawBoxFullGradient(axisBB, this.getClientColorsMassive(alphaPC * 0.3333F), true, 1.0F);
         this.drawBoxFullGradient(axisBB, this.getClientColorsMassive(alphaPC * 0.1F), false, 0.0F);
         GL11.glEnable(2929);
      }
   }

   private void drawBoxFullGradient(AxisAlignedBB bb, int[] color, boolean lineMode, float lineW) {
      this.drawBoxFullGradient(bb, color[0], color[1], color[2], color[3], color[4], color[5], color[6], color[7], lineMode, lineW);
   }

   private int[] getClientColorsMassive(float alphaPC) {
      return new int[]{
         ClientColors.getColor1(0, alphaPC),
         ClientColors.getColor2(-135, alphaPC),
         ClientColors.getColor1(90, alphaPC),
         ClientColors.getColor2(-45, alphaPC),
         ClientColors.getColor1(180, alphaPC),
         ClientColors.getColor2(45, alphaPC),
         ClientColors.getColor1(270, alphaPC),
         ClientColors.getColor2(135, alphaPC)
      };
   }

   private void drawBoxFullGradient(
      AxisAlignedBB bb, int color1, int color2, int color3, int color4, int color5, int color6, int color7, int color8, boolean linesMode, float lineWidth
   ) {
      double x = bb.minX;
      double y = bb.minY;
      double z = bb.minZ;
      double x2 = bb.maxX;
      double y2 = bb.maxY;
      double z2 = bb.maxZ;
      GlStateManager.shadeModel(7425);
      GlStateManager.enableBlend();
      GlStateManager.disableAlpha();
      GlStateManager.disableCull();
      if (linesMode) {
         GlStateManager.glLineWidth(lineWidth);
         GL11.glLineStipple(3, Short.reverseBytes((short)-24769));
         GL11.glEnable(2852);
         GL11.glEnable(2848);
         GL11.glHint(3154, 4354);
      }

      if (linesMode) {
         this.buffer.begin(2, DefaultVertexFormats.POSITION_COLOR);
         this.buffer.pos(x, y, z).color(color1).endVertex();
         this.buffer.pos(x, y, z2).color(color3).endVertex();
         this.buffer.pos(x2, y, z2).color(color5).endVertex();
         this.buffer.pos(x2, y, z).color(color7).endVertex();
         this.tessellator.draw();
      } else {
         this.buffer.begin(9, DefaultVertexFormats.POSITION_COLOR);
         this.buffer.pos(x, y, z).color(color1).endVertex();
         this.buffer.pos(x, y, z2).color(color3).endVertex();
         this.buffer.pos(x2, y, z2).color(color5).endVertex();
         this.buffer.pos(x2, y, z).color(color7).endVertex();
         this.tessellator.draw();
      }

      if (linesMode) {
         this.buffer.begin(2, DefaultVertexFormats.POSITION_COLOR);
         this.buffer.pos(x, y2, z).color(color2).endVertex();
         this.buffer.pos(x, y2, z2).color(color4).endVertex();
         this.buffer.pos(x2, y2, z2).color(color6).endVertex();
         this.buffer.pos(x2, y2, z).color(color8).endVertex();
         this.tessellator.draw();
      } else {
         this.buffer.begin(9, DefaultVertexFormats.POSITION_COLOR);
         this.buffer.pos(x, y2, z).color(color2).endVertex();
         this.buffer.pos(x, y2, z2).color(color4).endVertex();
         this.buffer.pos(x2, y2, z2).color(color6).endVertex();
         this.buffer.pos(x2, y2, z).color(color8).endVertex();
         this.tessellator.draw();
      }

      if (linesMode) {
         this.buffer.begin(1, DefaultVertexFormats.POSITION_COLOR);
         this.buffer.pos(x, y2, z).color(color2).endVertex();
         this.buffer.pos(x, y, z).color(color1).endVertex();
         this.tessellator.draw();
      } else {
         this.buffer.begin(9, DefaultVertexFormats.POSITION_COLOR);
         this.buffer.pos(x, y2, z).color(color2).endVertex();
         this.buffer.pos(x, y2, z2).color(color4).endVertex();
         this.buffer.pos(x, y, z2).color(color3).endVertex();
         this.buffer.pos(x, y, z).color(color1).endVertex();
         this.tessellator.draw();
      }

      if (linesMode) {
         this.buffer.begin(1, DefaultVertexFormats.POSITION_COLOR);
         this.buffer.pos(x2, y2, z2).color(color6).endVertex();
         this.buffer.pos(x2, y, z2).color(color5).endVertex();
         this.tessellator.draw();
      } else {
         this.buffer.begin(9, DefaultVertexFormats.POSITION_COLOR);
         this.buffer.pos(x2, y2, z).color(color8).endVertex();
         this.buffer.pos(x2, y2, z2).color(color6).endVertex();
         this.buffer.pos(x2, y, z2).color(color5).endVertex();
         this.buffer.pos(x2, y, z).color(color7).endVertex();
         this.tessellator.draw();
      }

      if (linesMode) {
         this.buffer.begin(1, DefaultVertexFormats.POSITION_COLOR);
         this.buffer.pos(x, y2, z2).color(color4).endVertex();
         this.buffer.pos(x, y, z2).color(color3).endVertex();
         this.tessellator.draw();
      } else {
         this.buffer.begin(9, DefaultVertexFormats.POSITION_COLOR);
         this.buffer.pos(x, y2, z2).color(color4).endVertex();
         this.buffer.pos(x2, y2, z2).color(color6).endVertex();
         this.buffer.pos(x2, y, z2).color(color5).endVertex();
         this.buffer.pos(x, y, z2).color(color3).endVertex();
         this.tessellator.draw();
      }

      if (linesMode) {
         this.buffer.begin(1, DefaultVertexFormats.POSITION_COLOR);
         this.buffer.pos(x2, y2, z).color(color8).endVertex();
         this.buffer.pos(x2, y, z).color(color7).endVertex();
         this.tessellator.draw();
      } else {
         this.buffer.begin(9, DefaultVertexFormats.POSITION_COLOR);
         this.buffer.pos(x, y2, z).color(color2).endVertex();
         this.buffer.pos(x2, y2, z).color(color8).endVertex();
         this.buffer.pos(x2, y, z).color(color7).endVertex();
         this.buffer.pos(x, y, z).color(color1).endVertex();
         this.tessellator.draw();
      }

      if (linesMode) {
         GlStateManager.glLineWidth(1.0F);
         GL11.glDisable(2852);
         GL11.glDisable(2848);
         GL11.glHint(3154, 4352);
      }

      GlStateManager.enableCull();
      GlStateManager.shadeModel(7424);
      GlStateManager.enableAlpha();
   }

   private void drawDeathBoxTag(final Box box, final float alphaPC, final ScaledResolution sr) {
      float alpha = box.alphaPC.getAnim() * alphaPC;
      alpha *= alpha;
      if (alpha <= 0.15f) {
         return;
      }
      final int textColor = ColorUtils.swapAlpha(ColorUtils.getFixedWhiteColor(), ColorUtils.getAlphaFromColor(ColorUtils.getFixedWhiteColor()) * alpha);
      final int bgColor2 = ColorUtils.swapAlpha(0, 170.0f * alpha);
      final int bgColor3 = ColorUtils.swapAlpha(0, 60.0f * alpha);
      final double xPos = box.pos.xCoord;
      final double yPos = box.pos.yCoord + (this.getDeathBoxAxisBoxByVec(box, alphaPC).maxY - this.getDeathBoxAxisBoxByVec(box, alphaPC).minY) + 0.10000000149011612;
      final double zPos = box.pos.zCoord;
      final Vec2f pos = this.getNameTagPosVec(new Vec3d(xPos, yPos, zPos), DeathBox.mc.getRenderPartialTicks(), sr, DeathBox.mc.renderManager);
      if (this.nameTagVecSetSuccess(pos)) {
         final CFontRenderer font = Fonts.mntsb_12;
         final String nameString = TextFormatting.RED + "DEAD" + TextFormatting.GRAY + " -> " + TextFormatting.RESET + " " + box.info.displayName + TextFormatting.RESET;
         final String coordString = TextFormatting.DARK_AQUA + "X" + TextFormatting.GREEN + "Y" + TextFormatting.RED + "Z" + TextFormatting.WHITE + ": " + TextFormatting.RESET + " " + box.info.coords;
         final float nameStrW = (float)font.getStringWidth(nameString);
         final float coordsStrW = (float)font.getStringWidth(coordString);
         final float tagTextW = (coordsStrW > nameStrW) ? coordsStrW : nameStrW;
         final float tagBGW = tagTextW + 4.0f;
         final float h = 14.0f;
         final float x = pos.x;
         final float y = pos.y - h;
         final int c1 = ClientColors.getColor1(0, alpha);
         final int c2 = ClientColors.getColor2(0, alpha);
         if (box.wantToAddSparks) {
            for (int i = 0; i < 3; ++i) {
               box.SPARKS_LIST.add(new Spark(-tagBGW / 2.0f - 2.0f, h, c1, c2, 650));
               box.SPARKS_LIST.add(new Spark(tagBGW / 2.0f + 2.0f, h, c2, c1, 650));
            }
            box.wantToAddSparks = false;
         }
         final float pTicks = DeathBox.mc.getRenderPartialTicks();
         GL11.glDepthMask(false);
         final float alpha2 = alpha;
         RenderUtils.drawTwoAlphedSideways(x - tagBGW / 2.0f, y, x + tagBGW / 2.0f, y + h, bgColor3, bgColor2, false);
         RenderUtils.drawVGradientRectBloom(x - tagBGW / 2.0f - 1.5f, y - 1.0f, x - tagBGW / 2.0f, y + h + 1.0f, c1, c2);
         RenderUtils.drawVGradientRectBloom(x + tagBGW / 2.0f, y - 1.0f, x + tagBGW / 2.0f + 1.5f, y + h + 1.0f, c2, c1);
         RenderUtils.drawAlphedSideways(x - tagBGW / 2.0f, y - 0.5f, x, y, c1, 0, true);
         RenderUtils.drawAlphedSideways(x, y - 0.5f, x + tagBGW / 2.0f, y, 0, c2, true);
         RenderUtils.drawAlphedSideways(x - tagBGW / 2.0f, y + h, x, y + h + 0.5f, c2, 0, true);
         RenderUtils.drawAlphedSideways(x, y + h, x + tagBGW / 2.0f, y + h + 0.5f, 0, c1, true);
         GL11.glEnable(3042);
         GL11.glBlendFunc(770, 32772);
         GL11.glDisable(3553);
         GL11.glDisable(3008);
         GL11.glEnable(2832);
         GL11.glPointSize(alphaPC * 2.5f + 1.0E-4f);
         GL11.glBegin(0);
         box.SPARKS_LIST.forEach(spark -> {
            final float sparkAPC = spark.alphaPC();
            final float sparkAPC2 = ((sparkAPC > 0.5f) ? (1.0f - sparkAPC) : sparkAPC) * 4.0f;
            final float sparkAPC3 = (sparkAPC2 > 1.0f) ? 1.0f : sparkAPC2;
            final int sparkColor = ColorUtils.getOverallColorFrom(spark.color, -1, sparkAPC3 / 2.0f);
            RenderUtils.glColor(ColorUtils.swapAlpha(sparkColor, ColorUtils.getAlphaFromColor(sparkColor) * sparkAPC3 * alpha2));
            GL11.glVertex2d(x + spark.getX(pTicks), y + spark.getY(pTicks));
            return;
         });
         GL11.glEnd();
         GL11.glPointSize(1.0f);
         GL11.glEnable(3008);
         GL11.glEnable(3553);
         GL11.glBlendFunc(770, 771);
         GlStateManager.resetColor();
         if (ColorUtils.getAlphaFromColor(textColor) >= 33) {
            font.drawStringWithShadow(nameString, x - nameStrW / 2.0f, y + 3.0f, textColor);
            font.drawStringWithShadow(coordString, x - coordsStrW / 2.0f, y + 9.0f, textColor);
         }
      }
      else if (!box.SPARKS_LIST.isEmpty()) {
         box.SPARKS_LIST.clear();
      }
      GL11.glEnable(3042);
      GL11.glDepthMask(true);
   }

   private float getAlphaPC() {
      this.stateAnim.to = this.actived ? 1.0F : 0.0F;
      float aPC = this.stateAnim.getAnim();
      return aPC < 0.03F ? 0.0F : (aPC > 0.97F ? 1.0F : aPC);
   }

   @Override
   public void alwaysRender3D() {
      float alphaPC = this.getAlphaPC();
      if (alphaPC != 0.0F) {
         if (mc.world != null && Minecraft.player != null) {
            if (this.getDeathBoxesList() != null && this.getDeathBoxesList().size() != 0) {
               RenderUtils.setup3dForBlockPos(() -> this.getDeathBoxesList().forEach(box -> this.drawProcessBox(box, alphaPC)), false);
               GL11.glEnable(2929);
               GL11.glDepthMask(true);
            }
         }
      }
   }

   @Override
   public void alwaysRender2D(ScaledResolution sr) {
      float alphaPC = this.getAlphaPC();
      if (alphaPC != 0.0F) {
         if (mc.world != null && Minecraft.player != null) {
            if (this.getDeathBoxesList() != null && this.getDeathBoxesList().size() != 0) {
               this.getDeathBoxesList().forEach(box -> this.drawDeathBoxTag(box, alphaPC, sr));
               GL11.glEnable(2929);
               GL11.glDepthMask(true);
            }
         }
      }
   }

   @Override
   public void onUpdate() {
      if (mc.world != null && Minecraft.player != null) {
         if (!this.Players.getBool() && !this.Mobs.getBool()) {
            this.toggle(false);
            Client.msg("§f§lModules:§r §7[§l" + this.getName() + "§r§7]: §7включите что-нибудь в настройках.", false);
         } else {
            this.processingDeathEntities(this.Players.getBool(), this.Mobs.getBool());
            this.getDeathBoxesList().forEach(DeathBox.Box::updateStatus);
            this.getDeathBoxesList().removeIf(DeathBox.Box::isWantToRemove);
         }
      }
   }

   private float getMaxTime() {
      return this.LifeTime.getFloat();
   }

   private class Box {
      public List<DeathBox.Spark> SPARKS_LIST = new ArrayList<>();
      private final DeathBox.InfoDeathEntity info;
      private final AnimationUtils alphaPC = new AnimationUtils(0.0F, 1.0F, 0.05F);
      private final long time = System.currentTimeMillis();
      private final Vec3d pos;
      private final AxisAlignedBB box;
      private boolean toRemove;
      public boolean wantToAddSparks;

      private Box(Vec3d pos, AxisAlignedBB box, DeathBox.InfoDeathEntity info) {
         this.pos = pos;
         this.box = box;
         this.info = info;
      }

      private final long getTime() {
         return System.currentTimeMillis() - this.time;
      }

      private final void updateStatus() {
         if (this.alphaPC.to != 0.0F && this.getDeltaTime() >= 1.0F) {
            this.alphaPC.to = 0.0F;
         } else if (this.alphaPC.to == 0.0F && (double)this.alphaPC.getAnim() <= 0.03) {
            this.toRemove = true;
         } else {
            this.wantToAddSparks = true;
         }

         this.SPARKS_LIST.forEach(DeathBox.Spark::updatePositions);
         this.SPARKS_LIST.removeIf(DeathBox.Spark::isToRemove);
      }

      private final boolean isWantToRemove() {
         return this.toRemove;
      }

      private final boolean isWantToAddSparks() {
         return this.wantToAddSparks = false;
      }

      private final float getDeltaTime() {
         return (float)this.getTime() / DeathBox.this.getMaxTime();
      }
   }

   private class InfoDeathEntity {
      String name;
      String displayName;
      String coords;

      private InfoDeathEntity(String name, String displayName, String coords) {
         this.name = name;
         this.displayName = displayName;
         this.coords = coords;
      }
   }

   private class Spark {
      float xOffset;
      float yOffset;
      float prevXOffset;
      float prevYOffset;
      float motionX;
      float motionY;
      int color;
      int maxTime;
      long startTime = System.currentTimeMillis();

      public Spark(float xOffset, float yOffset, int color, int color2, int maxTime) {
         this.xOffset = xOffset;
         this.motionX = (float)(Math.random() * (double)(xOffset < 0.0F ? -1.0F : 1.0F));
         this.yOffset = (float)((double)yOffset * Math.random());
         this.motionY = (float)((-0.5 + Math.random()) / 4.0);
         this.color = ColorUtils.getOverallColorFrom(color, color2, (float)MathUtils.clamp(Math.random(), 0.0, 1.0));
         this.maxTime = maxTime;
         this.prevXOffset = xOffset;
         this.prevYOffset = yOffset;
      }

      public void updatePositions() {
         this.prevXOffset = this.xOffset;
         this.prevYOffset = this.yOffset;
         this.xOffset = this.xOffset + this.motionX;
         this.yOffset = this.yOffset + this.motionY;
      }

      public float timePC() {
         return MathUtils.clamp((float)(System.currentTimeMillis() - this.startTime) / (float)this.maxTime, 0.0F, 1.0F);
      }

      public float alphaPC() {
         return 1.0F - this.timePC();
      }

      public boolean isToRemove() {
         return this.alphaPC() == 0.0F;
      }

      public float getX(float pTicks) {
         return this.prevXOffset + (this.xOffset - this.prevXOffset) * pTicks;
      }

      public float getY(float pTicks) {
         return this.prevYOffset + (this.yOffset - this.prevYOffset) * pTicks;
      }
   }
}