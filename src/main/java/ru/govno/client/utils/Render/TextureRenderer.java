package ru.govno.client.utils.Render;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;
import ru.govno.client.Client;

public class TextureRenderer {
   public static double[] calcTexPC(int vertexIndex, int vertexCount) {
      double texOffset0 = (double)vertexIndex / (double)vertexCount;
      texOffset0 = texOffset0 < 0.0 ? 0.0 : Math.min(texOffset0, 1.0);
      double texOffset1 = ((double)vertexIndex + 1.0) / (double)vertexCount;
      texOffset1 = texOffset1 < 0.0 ? 0.0 : Math.min(texOffset1, 1.0);
      return new double[]{texOffset0, texOffset1};
   }

   private static void drawVerticalTexQuads3d(
      Tessellator tessellator,
      int glBeginNum,
      VertexFormat vertexFormat,
      int drawLoopCount,
      List<TextureRenderer.Vec3dColored> upVecs,
      List<TextureRenderer.Vec3dColored> downVecs
   ) {
      int upVecsCount = upVecs.size();
      int downVecsCount = downVecs.size();
      int texturesCount;
      if ((texturesCount = Math.min(upVecsCount, downVecsCount) / 2) != 0) {
         List<TextureRenderer.TexQuad> texQuads = new ArrayList<>();

         for (int texNum = 0; texNum < texturesCount; texNum++) {
            double[] calcTexX = calcTexPC(texNum, texturesCount);
            double[] calcTexY = new double[]{0.0, 1.0};
            int index = Math.min(Math.max(texNum * 2, 0), texturesCount * 2 - 1);
            int prevIndex = Math.min(Math.max(index - 1, 0), texturesCount * 2 - 1);
            texQuads.add(
               new TextureRenderer.TexQuad(
                  new double[]{calcTexX[1], calcTexX[1], calcTexX[0], calcTexX[0]},
                  new double[]{calcTexY[0], calcTexY[1], calcTexY[1], calcTexY[0]},
                  new TextureRenderer.Vec3dColored[]{upVecs.get(index), downVecs.get(index), downVecs.get(prevIndex), upVecs.get(prevIndex)}
               )
            );
         }

         if (!texQuads.isEmpty()) {
            BufferBuilder bufferBuilder = tessellator.getBuffer();
            texQuads.stream()
               .filter(quad3d -> Arrays.stream(quad3d.vecs).noneMatch(Objects::isNull))
               .forEach(
                  quad3d -> {
                     bufferBuilder.begin(glBeginNum, vertexFormat);
                     List<TextureRenderer.Vec3dColored> quadVecs = Arrays.stream(quad3d.vecs).toList();
                     quadVecs.forEach(
                        vec -> {
                           int vecIndex;
                           bufferBuilder.pos(vec.getX(), vec.getY(), vec.getZ())
                              .tex(quad3d.texX[vecIndex = quadVecs.indexOf(vec)], quad3d.texY[vecIndex])
                              .color(vec.getColor())
                              .endVertex();
                        }
                     );
                     tessellator.draw(Math.max(drawLoopCount, 1));
                  }
               );
            Client.msg(texQuads.stream().map(a -> a.getVecs()).collect(Collectors.toList()).size(), false);
         }
      }
   }

   private static double doubleLerp(double firstValue, double secondValue, double offsetPC) {
      return (secondValue - firstValue) * offsetPC + firstValue;
   }

   public static TextureRenderer.Vec3dColored[][] generateCylinder(
      Vec3d pos,
      double firstRadius,
      double secondRadius,
      double yAppend,
      double startDegree,
      double endDegree,
      double degreeStep,
      int[] firstColors,
      int... secondColors
   ) {
      int degreeCount = (int)(Math.abs(endDegree - startDegree) / degreeStep);
      int firstColorsCount = firstColors.length;
      int secondColorsCount = secondColors.length;
      TextureRenderer.Vec3dColored[] upVecs = new TextureRenderer.Vec3dColored[degreeCount];
      Arrays.stream(new int[degreeCount])
         .forEach(
            num -> {
               double radian = Math.toRadians(doubleLerp(startDegree, endDegree, (double)num / (double)degreeCount));
               double sinUp = -Math.sin(radian) * firstRadius;
               double cosUp = Math.cos(radian) * firstRadius;
               double sinDown = -Math.sin(radian) * secondRadius;
               double cosDown = Math.cos(radian) * secondRadius;
               upVecs[num] = new TextureRenderer.Vec3dColored(
                  pos.xCoord + sinUp,
                  pos.yCoord + yAppend,
                  pos.zCoord + cosUp,
                  firstColors[(int)((double)num / (double)degreeCount * (double)(firstColorsCount - 1))]
               );
               upVecs[num] = new TextureRenderer.Vec3dColored(
                  pos.xCoord + sinDown,
                  pos.yCoord,
                  pos.zCoord + cosDown,
                  secondColors[(int)((double)num / (double)degreeCount * (double)(secondColorsCount - 1))]
               );
            }
         );
      return new TextureRenderer.Vec3dColored[][]{upVecs, upVecs};
   }

   public static void drawTexturedCylinder(Vec3d pos, ResourceLocation texLoc, double upRadius, double downRadius, double yAppend, int color, boolean bloom) {
      int[] colorCast = new int[]{color};
      TextureRenderer.Vec3dColored[][] allVecs = generateCylinder(pos, upRadius, downRadius, yAppend, 0.0, 360.0, 12.0, colorCast, colorCast);
      GL11.glPushMatrix();
      GL11.glEnable(3042);
      GL11.glBlendFunc(770, bloom ? '耄' : 771);
      GL11.glDisable(2929);
      GL11.glDisable(3008);
      GL11.glDisable(2884);
      boolean prevTextureFunc = GL11.glIsEnabled(3553);
      GL11.glEnable(3553);
      Minecraft.getMinecraft().getTextureManager().bindTexture(texLoc);
      GL11.glDepthMask(false);
      GL11.glTranslated(-RenderManager.viewerPosX, -RenderManager.viewerPosY, -RenderManager.viewerPosZ);
      drawVerticalTexQuads3d(Tessellator.getInstance(), 9, DefaultVertexFormats.POSITION_TEX_COLOR, 1, Arrays.asList(allVecs[0]), Arrays.asList(allVecs[1]));
      if (bloom) {
         GL11.glBlendFunc(770, 771);
      }

      GL11.glEnable(2929);
      GL11.glEnable(3008);
      GL11.glEnable(2884);
      if (prevTextureFunc) {
         GL11.glEnable(3553);
      } else {
         GL11.glDisable(3553);
      }

      GL11.glDepthMask(true);
      GL11.glPopMatrix();
   }

   private static class TexQuad {
      private final double[] texX;
      private final double[] texY;
      private final TextureRenderer.Vec3dColored[] vecs;

      public double[] getTexX() {
         return this.texX;
      }

      public double[] getTexY() {
         return this.texY;
      }

      public TextureRenderer.Vec3dColored[] getVecs() {
         return this.vecs;
      }

      public TexQuad(double[] texX, double[] texY, TextureRenderer.Vec3dColored[] vecs) {
         this.texX = texX;
         this.texY = texY;
         this.vecs = vecs;
      }
   }

   public static class Vec3dColored {
      double x;
      double y;
      double z;
      int color;

      public int getColor() {
         return this.color;
      }

      public void setX(double x) {
         this.x = x;
      }

      public void setY(double y) {
         this.y = y;
      }

      public void setZ(double z) {
         this.z = z;
      }

      public void setColor(int color) {
         this.color = color;
      }

      public Vec3dColored(double x, double y, double z, int color) {
         this.x = x;
         this.y = y;
         this.z = z;
         this.color = color;
      }

      public double getX() {
         return this.x;
      }

      public double getY() {
         return this.y;
      }

      public double getZ() {
         return this.z;
      }
   }
}
