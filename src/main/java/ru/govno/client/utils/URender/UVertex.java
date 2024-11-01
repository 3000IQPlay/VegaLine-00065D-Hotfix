package ru.govno.client.utils.URender;

import net.minecraft.client.renderer.BufferBuilder;
import ru.govno.client.utils.URender.UGL;

public class UVertex {
    public float x;
    public float y;
    public float z;
    public float u;
    public float v;
    public int c;
    public boolean d3;
    public boolean t;

    private UVertex(float x, float y, float z, float u, float v, boolean d, boolean t, int c) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.u = u;
        this.v = v;
        this.d3 = d;
        this.t = t;
        this.c = c;
    }

    public static UVertex vertex(float x, float y, float z, float u, float v, int c) {
        return new UVertex(x, y, z, u, v, true, true, c);
    }

    public static UVertex vertex(float x, float y, float z, int c) {
        return new UVertex(x, y, z, 0.0f, 0.0f, true, false, c);
    }

    public static UVertex vertex(float x, float y, float u, float v, int c) {
        return new UVertex(x, y, 0.0f, u, v, false, true, c);
    }

    public static UVertex vertex(float x, float y, int c) {
        return new UVertex(x, y, 0.0f, 0.0f, 0.0f, false, false, c);
    }

    public int getColor() {
        return this.c;
    }

    public void doGl() {
        UGL.color(this.c);
        if (this.t) {
            UGL.tex(this.u, this.v);
            if (this.d3) {
                UGL.vert(this.x, this.y, this.z);
            } else {
                UGL.vert(this.x, this.y, this.z);
            }
            return;
        }
        if (this.d3) {
            UGL.vert(this.x, this.y, this.z);
        } else {
            UGL.vert(this.x, this.y);
        }
    }

    public void doMcVBO(BufferBuilder buffer) {
        if (this.t) {
            if (this.d3) {
                buffer.pos(this.x, this.y, this.z).tex(this.u, this.v).color(this.c).endVertex();
            } else {
                buffer.pos(this.x, this.y).tex(this.u, this.v).color(this.c).endVertex();
            }
            return;
        }
        if (this.d3) {
            buffer.pos(this.x, this.y, this.z).color(this.c).endVertex();
        } else {
            buffer.pos(this.x, this.y).color(this.c).endVertex();
        }
    }
}

