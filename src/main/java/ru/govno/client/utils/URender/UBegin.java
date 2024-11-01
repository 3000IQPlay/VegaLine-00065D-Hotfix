package ru.govno.client.utils.URender;

import java.util.List;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import ru.govno.client.utils.URender.UGL;
import ru.govno.client.utils.URender.UVertex;

public class UBegin {
    private final int begin;
    public boolean bloom;
    public boolean antialiasing;
    private float scaleValue = 1.0f;
    private final List<UVertex> vertices;

    public List<UVertex> getVertices() {
        return this.vertices;
    }

    private UBegin(int begin, List<UVertex> vertices) {
        this.begin = begin;
        this.vertices = vertices;
    }

    private UBegin(int begin, List<UVertex> vertices, float scaleValue) {
        this.begin = begin;
        this.vertices = vertices;
        this.scaleValue = scaleValue;
    }

    public static UBegin begin(int begin, List<UVertex> vertices) {
        return new UBegin(begin, vertices);
    }

    public static UBegin begin(int begin, List<UVertex> vertices, float scaleValue) {
        return new UBegin(begin, vertices, scaleValue);
    }

    public UBegin setAntialiasing(boolean antialiasing) {
        this.antialiasing = antialiasing;
        return this;
    }

    public UBegin setScaleValue(float scaleValue) {
        this.scaleValue = scaleValue;
        return this;
    }

    public UBegin setBloom(boolean bloom) {
        this.bloom = bloom;
        return this;
    }

    public boolean hasResized() {
        return this.scaleValue != 1.0f && (this.begin == 0 || this.begin == 1 || this.begin == 3 || this.begin == 2);
    }

    public UBegin doUGL() {
        if (this.vertices.isEmpty()) {
            return this;
        }
        boolean rescale = this.hasResized();
        if (rescale) {
            if (this.begin == 0) {
                UGL.setPointSize(this.scaleValue);
            } else {
                UGL.setLineWidth(this.scaleValue);
            }
        }
        if (this.bloom) {
            UGL.blend(this.bloom);
        }
        UGL.setAntialiasing(this.begin, this.antialiasing);
        UGL.cull(false);
        UGL.disable(3008);
        UGL.begin(this.begin);
        UGL.verts(this.vertices);
        UGL.end();
        UGL.cull(true);
        UGL.setAntialiasing(this.begin, false);
        if (this.bloom) {
            UGL.blend(false);
        }
        if (rescale) {
            if (this.begin == 0) {
                UGL.resetPointSize();
            } else {
                UGL.resetLineWidth();
            }
        }
        return this;
    }

    public UBegin doMcVBO(Tessellator tessellator, int tryCount) {
        if (this.vertices.isEmpty()) {
            return this;
        }
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(this.begin, UGL.isTextured() ? DefaultVertexFormats.POSITION_TEX_COLOR : DefaultVertexFormats.POSITION_COLOR);
        this.vertices.forEach(vert -> vert.doMcVBO(buffer));
        boolean rescale = this.hasResized();
        if (rescale) {
            if (this.begin == 0) {
                UGL.setPointSize(this.scaleValue);
            } else {
                UGL.setLineWidth(this.scaleValue);
            }
        }
        UGL.color(-1);
        if (this.bloom) {
            UGL.blend(true);
        }
        UGL.setAntialiasing(this.begin, this.antialiasing);
        UGL.cull(false);
        tessellator.draw(tryCount);
        UGL.cull(true);
        UGL.setAntialiasing(this.begin, false);
        if (this.bloom) {
            UGL.blend(false);
        }
        if (rescale) {
            if (this.begin == 0) {
                UGL.resetPointSize();
            } else {
                UGL.resetLineWidth();
            }
        }
        return this;
    }

    public void doMcVBO(Tessellator tessellator) {
        this.doMcVBO(tessellator, 1);
    }
}

