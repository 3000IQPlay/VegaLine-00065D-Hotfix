package ru.govno.client.utils.Render;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import org.lwjgl.opengl.GL11;

public class PrimitiveRenderer {
   private float[] getRGBAF(int hash) {
      return new float[]{
         (float)(hash >> 16 & 0xFF) / 255.0F, (float)(hash >> 8 & 0xFF) / 255.0F, (float)(hash & 0xFF) / 255.0F, (float)(hash >> 24 & 0xFF) / 255.0F
      };
   }

   private float[] glColorMassiveOf(int... hashs) {
      int dataSize;
      float[] data = new float[(dataSize = hashs.length) * 4];
      dataSize--;

      while (dataSize > 0) {
         float[] rgbaf;
         data[dataSize * 4] = (rgbaf = this.getRGBAF(hashs[dataSize]))[0];
         data[dataSize * 4 + 1] = rgbaf[1];
         data[dataSize * 4 + 2] = rgbaf[2];
         data[dataSize * 4 + 3] = rgbaf[3];
         dataSize--;
      }

      return data;
   }

   private PrimitiveRenderer.Vec2[] vecsOfQuad(float... val) {
      return new PrimitiveRenderer.Vec2[]{
         new PrimitiveRenderer.Vec2(val[0], val[1]),
         new PrimitiveRenderer.Vec2(val[2], val[1]),
         new PrimitiveRenderer.Vec2(val[2], val[3]),
         new PrimitiveRenderer.Vec2(val[0], val[3])
      };
   }

   private float[] glVectorArrays(PrimitiveRenderer.Vec2... vec2s) {
      int vecMax;
      float[] vertices = new float[(vecMax = vec2s.length) * 2];

      while (vecMax > 0) {
         PrimitiveRenderer.Vec2 vec2;
         vertices[vecMax * 2] = (vec2 = vec2s[--vecMax]).x;
         vertices[vecMax * 2 + 1] = vec2.y;
      }

      return vertices;
   }

   private float[] glVectorArrays(PrimitiveRenderer.Vec3... vec3s) {
      int vecMax;
      float[] vertices = new float[(vecMax = vec3s.length) * 2];

      while (vecMax > 0) {
         PrimitiveRenderer.Vec3 vec3;
         vertices[vecMax * 2] = (vec3 = vec3s[--vecMax]).x;
         vertices[vecMax * 2 + 1] = vec3.y;
         vertices[vecMax * 2 + 2] = vec3.z;
      }

      return vertices;
   }

   private FloatBuffer floatBufferAs(float[] values, int datalineStageStep) {
      return ByteBuffer.allocateDirect(values.length * datalineStageStep).order(ByteOrder.nativeOrder()).asFloatBuffer().put(values).position(0);
   }

   private void drawPrimitiveVA(int glMode, float[] vecCoords, int dataSizeInVertex, int vecStageStep, float[] colorData) {
      GL11.glEnableClientState(32884);
      GL11.glVertexPointer(vecStageStep, 5126, this.floatBufferAs(vecCoords, vecStageStep));
      GL11.glEnableClientState(32886);
      int colorDataSize = 4;
      GL11.glColorPointer(colorDataSize, 5126, this.floatBufferAs(colorData, colorDataSize));
      GL11.glDrawArrays(glMode, 0, vecCoords.length / dataSizeInVertex);
      GL11.glDisableClientState(32884);
      GL11.glDisableClientState(32886);
   }

   private void drawQuad(float x, float y, float x2, float y2, int color) {
      this.drawPrimitiveVA(9, this.glVectorArrays(this.vecsOfQuad(x, y, x2, y2)), 2, 4, this.glColorMassiveOf(color));
   }

   private class Vec2 {
      private final float x;
      private final float y;

      public float getX() {
         return this.x;
      }

      public float getY() {
         return this.y;
      }

      public Vec2(float x, float y) {
         this.x = x;
         this.y = y;
      }
   }

   private class Vec3 {
      private final float x;
      private final float y;
      private final float z;

      public float getX() {
         return this.x;
      }

      public float getY() {
         return this.y;
      }

      public float getZ() {
         return this.z;
      }

      public Vec3(float x, float y, float z) {
         this.x = x;
         this.y = y;
         this.z = z;
      }
   }
}
