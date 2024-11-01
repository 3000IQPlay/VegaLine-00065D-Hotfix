package ru.govno.client.utils.Render;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import org.lwjgl.opengl.GL11;

public class PrimitiveRenderer {
    private float[] getRGBAF(int hash) {
        return new float[]{(float)(hash >> 16 & 0xFF) / 255.0f, (float)(hash >> 8 & 0xFF) / 255.0f, (float)(hash & 0xFF) / 255.0f, (float)(hash >> 24 & 0xFF) / 255.0f};
    }

    private float[] glColorMassiveOf(int ... hashs) {
        int dataSize = hashs.length;
        float[] data = new float[dataSize * 4];
        --dataSize;
        while (dataSize > 0) {
            float[] rgbaf = this.getRGBAF(hashs[dataSize]);
            data[dataSize * 4] = rgbaf[0];
            data[dataSize * 4 + 1] = rgbaf[1];
            data[dataSize * 4 + 2] = rgbaf[2];
            data[dataSize * 4 + 3] = rgbaf[3];
            --dataSize;
        }
        return data;
    }

    private Vec2[] vecsOfQuad(float ... val) {
        return new Vec2[]{new Vec2(val[0], val[1]), new Vec2(val[2], val[1]), new Vec2(val[2], val[3]), new Vec2(val[0], val[3])};
    }

    private float[] glVectorArrays(Vec2 ... vec2s) {
        int vecMax = vec2s.length;
        float[] vertices = new float[vecMax * 2];
        while (vecMax > 0) {
            Vec2 vec2 = vec2s[--vecMax];
            vertices[vecMax * 2] = vec2.x;
            vertices[vecMax * 2 + 1] = vec2.y;
        }
        return vertices;
    }

    private float[] glVectorArrays(Vec3 ... vec3s) {
        int vecMax = vec3s.length;
        float[] vertices = new float[vecMax * 2];
        while (vecMax > 0) {
            Vec3 vec3 = vec3s[--vecMax];
            vertices[vecMax * 2] = vec3.x;
            vertices[vecMax * 2 + 1] = vec3.y;
            vertices[vecMax * 2 + 2] = vec3.z;
        }
        return vertices;
    }

    private FloatBuffer floatBufferAs(float[] values, int datalineStageStep) {
        return ByteBuffer.allocateDirect(values.length * datalineStageStep).order(ByteOrder.nativeOrder()).asFloatBuffer().put(values).position(0);
    }

    private void drawPrimitiveVA(int glMode, float[] vecCoords, int dataSizeInVertex, int vecStageStep, float[] colorData) {
        GL11.glEnableClientState((int)32884);
        GL11.glVertexPointer((int)vecStageStep, (int)5126, (FloatBuffer)this.floatBufferAs(vecCoords, vecStageStep));
        GL11.glEnableClientState((int)32886);
        int colorDataSize = 4;
        GL11.glColorPointer((int)colorDataSize, (int)5126, (FloatBuffer)this.floatBufferAs(colorData, colorDataSize));
        GL11.glDrawArrays((int)glMode, (int)0, (int)(vecCoords.length / dataSizeInVertex));
        GL11.glDisableClientState((int)32884);
        GL11.glDisableClientState((int)32886);
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

