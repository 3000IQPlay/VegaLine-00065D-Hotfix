package ru.govno.client.utils.URender;

import java.util.HashSet;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import ru.govno.client.utils.URender.UVertex;

public class UGL {
    private static boolean inBegin;
    private static ResourceLocation lastBindedTexture;
    private static float lineWidth;
    private static float pointSize;

    public static void setAlphaMin(float value) {
        GL11.glAlphaFunc((int)516, (float)value);
    }

    public static float r(int c) {
        return (float)(c >> 16 & 0xFF) / 255.0f;
    }

    public static float g(int c) {
        return (float)(c >> 8 & 0xFF) / 255.0f;
    }

    public static float b(int c) {
        return (float)(c & 0xFF) / 255.0f;
    }

    public static float a(int c) {
        return (float)(c >> 24 & 0xFF) / 255.0f;
    }

    public static boolean isOn(int func) {
        return GL11.glIsEnabled((int)func);
    }

    public static void toggle(int func, boolean active) {
        if (active) {
            UGL.enable(func);
        } else {
            UGL.disable(func);
        }
    }

    public static void enable(int func) {
        if (!UGL.isOn(func)) {
            GL11.glEnable((int)func);
        }
    }

    public static void disable(int func) {
        if (UGL.isOn(func)) {
            GL11.glDisable((int)func);
        }
    }

    public static void shade(boolean enabled) {
        GL11.glShadeModel((int)(enabled ? 7425 : 7424));
    }

    public static void shade(List<UVertex> vertices) {
        UGL.shade(new HashSet<Integer>(vertices.stream().map(UVertex::getColor).toList()).size() != 1);
    }

    public static void blend(boolean bloom) {
        GL11.glBlendFunc((int)770, (int)(bloom ? 1 : 771));
    }

    public static void hint(int hint, int type2) {
        GL11.glHint((int)hint, (int)type2);
    }

    public static void vert(float x, float y) {
        GL11.glVertex2f((float)x, (float)y);
    }

    public static void vert(float x, float y, float z) {
        GL11.glVertex3f((float)x, (float)y, (float)z);
    }

    public static void vert(double x, double y) {
        GL11.glVertex2d((double)x, (double)y);
    }

    public static void vert(double x, double y, double z) {
        GL11.glVertex3d((double)x, (double)y, (double)z);
    }

    public static void vert(float x, float y, int c) {
        if (!inBegin) {
            return;
        }
        UGL.color(c);
        GL11.glVertex2f((float)x, (float)y);
    }

    public static void vert(float x, float y, float z, int c) {
        if (!inBegin) {
            return;
        }
        UGL.color(c);
        GL11.glVertex3f((float)x, (float)y, (float)z);
    }

    public static void vert(double x, double y, int c) {
        if (!inBegin) {
            return;
        }
        UGL.color(c);
        GL11.glVertex2d((double)x, (double)y);
    }

    public static void vert(double x, double y, double z, int c) {
        if (!inBegin) {
            return;
        }
        UGL.color(c);
        GL11.glVertex3d((double)x, (double)y, (double)z);
    }

    public static void verts(List<UVertex> vertices) {
        if (!inBegin) {
            return;
        }
        vertices.forEach(UVertex::doGl);
    }

    public static void color(float r, float g, float b, float a) {
        GL11.glColor4f((float)r, (float)g, (float)b, (float)a);
    }

    public static void color(int c) {
        UGL.color(UGL.r(c), UGL.g(c), UGL.b(c), UGL.a(c));
    }

    public static void begin(int mode) {
        GL11.glBegin((int)mode);
        inBegin = true;
    }

    public static void end() {
        GL11.glEnd();
        inBegin = false;
    }

    public static void trans(float x, float y) {
        GL11.glTranslatef((float)x, (float)y, (float)0.0f);
    }

    public static void trans(float x, float y, float z) {
        GL11.glTranslatef((float)x, (float)y, (float)z);
    }

    public static void scale(float x, float y, float z) {
        GL11.glScalef((float)x, (float)y, (float)z);
    }

    public static void scale(float x, float y) {
        GL11.glScalef((float)x, (float)y, (float)1.0f);
    }

    public static void tex(float x, float y) {
        GL11.glTexCoord2f((float)x, (float)y);
    }

    public static void scaleAt(float x, float y, float z, float scale) {
        UGL.trans(x, y, z);
        UGL.scale(scale, scale, scale);
        UGL.trans(-x, -y, -z);
    }

    public static void scaleAt(float x, float y, float scale) {
        UGL.trans(x, y);
        UGL.scale(scale, scale);
        UGL.trans(-x, -y);
    }

    public static boolean isTextured() {
        return UGL.isOn(3553);
    }

    public static void depth(boolean enabled) {
        UGL.toggle(2929, enabled);
    }

    public static void mask(boolean enabled) {
        GL11.glDepthMask((boolean)enabled);
    }

    public static void cull(boolean enabled) {
        UGL.toggle(2884, enabled);
    }

    public static void bindTex(ResourceLocation location) {
        if ((lastBindedTexture == null || lastBindedTexture != location) && location != null) {
            TextureManager manager = Minecraft.getMinecraft().getTextureManager();
            ITextureObject itextureobject = manager.getMapTextureObjects().get(location);
            if (itextureobject == null) {
                itextureobject = new SimpleTexture(location);
                manager.loadTexture(location, itextureobject);
            }
            TextureUtil.bindTexture(itextureobject.getGlTextureId());
            lastBindedTexture = location;
        }
    }

    public static ResourceLocation getLastBindedTexture() {
        return lastBindedTexture;
    }

    public static void setLineWidth(float lineWidth1) {
        lineWidth = lineWidth1;
        GL11.glLineWidth((float)lineWidth);
    }

    public static void setPointSize(float pointSize1) {
        pointSize = pointSize1;
        GL11.glPointSize((float)pointSize);
    }

    public static void resetLineWidth() {
        lineWidth = 1.0f;
        GL11.glLineWidth((float)1.0f);
    }

    public static void resetPointSize() {
        pointSize = 1.0f;
        GL11.glPointSize((float)1.0f);
    }

    public static float getLastLineWidth() {
        return lineWidth;
    }

    public static float getLastPointSize() {
        return pointSize;
    }

    public static void setAntialiasing(int begin, boolean enable) {
        switch (begin) {
            case 0: {
                UGL.toggle(2832, enable);
                break;
            }
            case 1: 
            case 2: 
            case 3: {
                UGL.toggle(2848, enable);
                UGL.hint(3154, enable ? 4354 : 4352);
                break;
            }
            case 4: 
            case 5: 
            case 6: 
            case 7: 
            case 8: 
            case 9: {
                UGL.toggle(2881, enable);
                UGL.hint(3155, enable ? 4354 : 4352);
            }
        }
    }

    public static void resetColor() {
        GL11.glColor3b((byte)1, (byte)1, (byte)1);
    }

    static {
        lineWidth = 1.0f;
        pointSize = 1.0f;
    }
}

