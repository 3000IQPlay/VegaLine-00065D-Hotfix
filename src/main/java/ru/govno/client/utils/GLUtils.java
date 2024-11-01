package ru.govno.client.utils;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.BufferUtils;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;

public final class GLUtils {
    private static final AxisAlignedBB DEFAULT_AABB = new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0);
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final Random random = new Random();
    private static final Tessellator tessellator = Tessellator.getInstance();
    public static float zLevel;
    public static List<Integer> vbos;
    public static List<Integer> textures;

    public static void glScissor(int[] rect) {
        GLUtils.glScissor(rect[0], rect[1], rect[0] + rect[2], rect[1] + rect[3]);
    }

    public static void glScissor(float x, float y, float x1, float y1) {
        int scaleFactor = GLUtils.getScaleFactor();
        GL11.glScissor((int)((int)(x * (float)scaleFactor)), (int)((int)((float)Minecraft.getMinecraft().displayHeight - y1 * (float)scaleFactor)), (int)((int)((x1 - x) * (float)scaleFactor)), (int)((int)((y1 - y) * (float)scaleFactor)));
    }

    public static int getScaleFactor() {
        int scaleFactor = 1;
        boolean isUnicode = Minecraft.getMinecraft().isUnicode();
        int guiScale = Minecraft.getMinecraft().gameSettings.guiScale;
        if (guiScale == 0) {
            guiScale = 1000;
        }
        while (scaleFactor < guiScale && Minecraft.getMinecraft().displayWidth / (scaleFactor + 1) >= 320 && Minecraft.getMinecraft().displayHeight / (scaleFactor + 1) >= 240) {
            ++scaleFactor;
        }
        if (isUnicode && scaleFactor % 2 != 0 && scaleFactor != 1) {
            --scaleFactor;
        }
        return scaleFactor;
    }

    public static int getMouseX() {
        return Mouse.getX() * GLUtils.getScreenWidth() / Minecraft.getMinecraft().displayWidth;
    }

    public static int getMouseY() {
        return GLUtils.getScreenHeight() - Mouse.getY() * GLUtils.getScreenHeight() / Minecraft.getMinecraft().displayWidth - 1;
    }

    public static int getScreenWidth() {
        return Minecraft.getMinecraft().displayWidth / GLUtils.getScaleFactor();
    }

    public static int getScreenHeight() {
        return Minecraft.getMinecraft().displayHeight / GLUtils.getScaleFactor();
    }

    public static boolean isHovered(int x, int y, int width, int height, int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY < y + height;
    }

    public static int genVBO() {
        int id = GL15.glGenBuffers();
        vbos.add(id);
        GL15.glBindBuffer((int)34962, (int)id);
        return id;
    }

    public static int getTexture() {
        int textureID = GL11.glGenTextures();
        textures.add(textureID);
        return textureID;
    }

    public static int applyTexture(int texId, File file, int filter, int wrap) throws IOException {
        GLUtils.applyTexture(texId, ImageIO.read(file), filter, wrap);
        return texId;
    }

    public static int applyTexture(int texId, BufferedImage image, int filter, int wrap) {
        int[] pixels = new int[image.getWidth() * image.getHeight()];
        image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());
        ByteBuffer buffer = BufferUtils.createByteBuffer((int)(image.getWidth() * image.getHeight() * 4));
        for (int y = 0; y < image.getHeight(); ++y) {
            for (int x = 0; x < image.getWidth(); ++x) {
                int pixel = pixels[y * image.getWidth() + x];
                buffer.put((byte)(pixel >> 16 & 0xFF));
                buffer.put((byte)(pixel >> 8 & 0xFF));
                buffer.put((byte)(pixel & 0xFF));
                buffer.put((byte)(pixel >> 24 & 0xFF));
            }
        }
        buffer.flip();
        GLUtils.applyTexture(texId, image.getWidth(), image.getHeight(), buffer, filter, wrap);
        return texId;
    }

    public static int applyTexture(int texId, int width, int height, ByteBuffer pixels, int filter, int wrap) {
        GL11.glBindTexture((int)3553, (int)texId);
        GL11.glTexParameteri((int)3553, (int)10241, (int)filter);
        GL11.glTexParameteri((int)3553, (int)10240, (int)filter);
        GL11.glTexParameteri((int)3553, (int)10242, (int)wrap);
        GL11.glTexParameteri((int)3553, (int)10243, (int)wrap);
        GL11.glPixelStorei((int)3317, (int)1);
        GL11.glTexImage2D((int)3553, (int)0, (int)32856, (int)width, (int)height, (int)0, (int)6408, (int)5121, (ByteBuffer)pixels);
        GL11.glBindTexture((int)3553, (int)0);
        return texId;
    }

    public static void cleanup() {
        GL15.glBindBuffer((int)34962, (int)0);
        GL11.glBindTexture((int)3553, (int)0);
        for (int texture : vbos) {
            GL15.glDeleteBuffers((int)texture);
        }
        for (int texture : textures) {
            GL11.glDeleteTextures((int)texture);
        }
    }

    public static void drawBorderRect(float x, float y, float x1, float y1, float borderSize) {
        GLUtils.drawBorder(borderSize, x, y, x1, y1);
        GLUtils.drawRect(x, y, x1, y1);
    }

    public static void drawBorder(float size, float x, float y, float x1, float y1) {
        GL11.glLineWidth((float)size);
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(770, 771);
        BufferBuilder vertexBuffer = tessellator.getBuffer();
        vertexBuffer.begin(2, DefaultVertexFormats.POSITION);
        vertexBuffer.pos(x, y, 0.0).endVertex();
        vertexBuffer.pos(x, y1, 0.0).endVertex();
        vertexBuffer.pos(x1, y1, 0.0).endVertex();
        vertexBuffer.pos(x1, y, 0.0).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
    }

    public static void drawRect(float x, float y, float w, float h) {
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(770, 771);
        BufferBuilder vertexBuffer = tessellator.getBuffer();
        vertexBuffer.begin(7, DefaultVertexFormats.POSITION);
        vertexBuffer.pos(x, h, 0.0).endVertex();
        vertexBuffer.pos(w, h, 0.0).endVertex();
        vertexBuffer.pos(w, y, 0.0).endVertex();
        vertexBuffer.pos(x, y, 0.0).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
    }

    public static void drawSolidBox() {
        GLUtils.drawSolidBox(DEFAULT_AABB);
    }

    public static void drawSolidBox(AxisAlignedBB bb) {
        GL11.glBegin((int)7);
        GL11.glVertex3d((double)bb.minX, (double)bb.minY, (double)bb.minZ);
        GL11.glVertex3d((double)bb.maxX, (double)bb.minY, (double)bb.minZ);
        GL11.glVertex3d((double)bb.maxX, (double)bb.minY, (double)bb.maxZ);
        GL11.glVertex3d((double)bb.minX, (double)bb.minY, (double)bb.maxZ);
        GL11.glVertex3d((double)bb.minX, (double)bb.maxY, (double)bb.minZ);
        GL11.glVertex3d((double)bb.minX, (double)bb.maxY, (double)bb.maxZ);
        GL11.glVertex3d((double)bb.maxX, (double)bb.maxY, (double)bb.maxZ);
        GL11.glVertex3d((double)bb.maxX, (double)bb.maxY, (double)bb.minZ);
        GL11.glVertex3d((double)bb.minX, (double)bb.minY, (double)bb.minZ);
        GL11.glVertex3d((double)bb.minX, (double)bb.maxY, (double)bb.minZ);
        GL11.glVertex3d((double)bb.maxX, (double)bb.maxY, (double)bb.minZ);
        GL11.glVertex3d((double)bb.maxX, (double)bb.minY, (double)bb.minZ);
        GL11.glVertex3d((double)bb.maxX, (double)bb.minY, (double)bb.minZ);
        GL11.glVertex3d((double)bb.maxX, (double)bb.maxY, (double)bb.minZ);
        GL11.glVertex3d((double)bb.maxX, (double)bb.maxY, (double)bb.maxZ);
        GL11.glVertex3d((double)bb.maxX, (double)bb.minY, (double)bb.maxZ);
        GL11.glVertex3d((double)bb.minX, (double)bb.minY, (double)bb.maxZ);
        GL11.glVertex3d((double)bb.maxX, (double)bb.minY, (double)bb.maxZ);
        GL11.glVertex3d((double)bb.maxX, (double)bb.maxY, (double)bb.maxZ);
        GL11.glVertex3d((double)bb.minX, (double)bb.maxY, (double)bb.maxZ);
        GL11.glVertex3d((double)bb.minX, (double)bb.minY, (double)bb.minZ);
        GL11.glVertex3d((double)bb.minX, (double)bb.minY, (double)bb.maxZ);
        GL11.glVertex3d((double)bb.minX, (double)bb.maxY, (double)bb.maxZ);
        GL11.glVertex3d((double)bb.minX, (double)bb.maxY, (double)bb.minZ);
        GL11.glEnd();
    }

    public static void drawOutlinedBox() {
        GLUtils.drawOutlinedBox(DEFAULT_AABB);
    }

    public static void drawOutlinedBox(AxisAlignedBB bb) {
        GL11.glBegin((int)1);
        GL11.glVertex3d((double)bb.minX, (double)bb.minY, (double)bb.minZ);
        GL11.glVertex3d((double)bb.maxX, (double)bb.minY, (double)bb.minZ);
        GL11.glVertex3d((double)bb.maxX, (double)bb.minY, (double)bb.minZ);
        GL11.glVertex3d((double)bb.maxX, (double)bb.minY, (double)bb.maxZ);
        GL11.glVertex3d((double)bb.maxX, (double)bb.minY, (double)bb.maxZ);
        GL11.glVertex3d((double)bb.minX, (double)bb.minY, (double)bb.maxZ);
        GL11.glVertex3d((double)bb.minX, (double)bb.minY, (double)bb.maxZ);
        GL11.glVertex3d((double)bb.minX, (double)bb.minY, (double)bb.minZ);
        GL11.glVertex3d((double)bb.minX, (double)bb.minY, (double)bb.minZ);
        GL11.glVertex3d((double)bb.minX, (double)bb.maxY, (double)bb.minZ);
        GL11.glVertex3d((double)bb.maxX, (double)bb.minY, (double)bb.minZ);
        GL11.glVertex3d((double)bb.maxX, (double)bb.maxY, (double)bb.minZ);
        GL11.glVertex3d((double)bb.maxX, (double)bb.minY, (double)bb.maxZ);
        GL11.glVertex3d((double)bb.maxX, (double)bb.maxY, (double)bb.maxZ);
        GL11.glVertex3d((double)bb.minX, (double)bb.minY, (double)bb.maxZ);
        GL11.glVertex3d((double)bb.minX, (double)bb.maxY, (double)bb.maxZ);
        GL11.glVertex3d((double)bb.minX, (double)bb.maxY, (double)bb.minZ);
        GL11.glVertex3d((double)bb.maxX, (double)bb.maxY, (double)bb.minZ);
        GL11.glVertex3d((double)bb.maxX, (double)bb.maxY, (double)bb.minZ);
        GL11.glVertex3d((double)bb.maxX, (double)bb.maxY, (double)bb.maxZ);
        GL11.glVertex3d((double)bb.maxX, (double)bb.maxY, (double)bb.maxZ);
        GL11.glVertex3d((double)bb.minX, (double)bb.maxY, (double)bb.maxZ);
        GL11.glVertex3d((double)bb.minX, (double)bb.maxY, (double)bb.maxZ);
        GL11.glVertex3d((double)bb.minX, (double)bb.maxY, (double)bb.minZ);
        GL11.glEnd();
    }

    public static void drawSelectionBoundingBox(AxisAlignedBB boundingBox) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder vertexbuffer = tessellator.getBuffer();
        vertexbuffer.begin(3, DefaultVertexFormats.POSITION);
        vertexbuffer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex();
        vertexbuffer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).endVertex();
        vertexbuffer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).endVertex();
        vertexbuffer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).endVertex();
        vertexbuffer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex();
        tessellator.draw();
        vertexbuffer.begin(3, DefaultVertexFormats.POSITION);
        vertexbuffer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex();
        vertexbuffer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).endVertex();
        vertexbuffer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).endVertex();
        vertexbuffer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).endVertex();
        vertexbuffer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex();
        tessellator.draw();
        vertexbuffer.begin(1, DefaultVertexFormats.POSITION);
        vertexbuffer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex();
        vertexbuffer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex();
        vertexbuffer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).endVertex();
        vertexbuffer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).endVertex();
        vertexbuffer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).endVertex();
        vertexbuffer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).endVertex();
        vertexbuffer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).endVertex();
        vertexbuffer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).endVertex();
        tessellator.draw();
    }

    public static void drawBlockESP(BlockPos pos, float red, float green, float blue) {
        GL11.glPushMatrix();
        GL11.glEnable((int)3042);
        GL11.glBlendFunc((int)770, (int)771);
        GL11.glEnable((int)2848);
        GL11.glLineWidth((float)1.0f);
        GL11.glDisable((int)3553);
        GL11.glEnable((int)2884);
        GL11.glDisable((int)2929);
        GL11.glDisable((int)2896);
        mc.getRenderManager();
        double renderPosX = RenderManager.viewerPosX;
        mc.getRenderManager();
        double renderPosY = RenderManager.viewerPosY;
        mc.getRenderManager();
        double renderPosZ = RenderManager.viewerPosZ;
        GL11.glTranslated((double)(-renderPosX), (double)(-renderPosY), (double)(-renderPosZ));
        GL11.glTranslated((double)pos.getX(), (double)pos.getY(), (double)pos.getZ());
        GL11.glColor4f((float)red, (float)green, (float)blue, (float)0.3f);
        GLUtils.drawSolidBox();
        GL11.glColor4f((float)red, (float)green, (float)blue, (float)0.7f);
        GLUtils.drawOutlinedBox();
        GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        GL11.glEnable((int)2896);
        GL11.glEnable((int)2929);
        GL11.glEnable((int)3553);
        GL11.glDisable((int)3042);
        GL11.glDisable((int)2848);
        GL11.glPopMatrix();
    }

    public static void drawGradientRect(int x, int y, int w, int h, int startColor, int endColor) {
        float f = (float)(startColor >> 24 & 0xFF) / 255.0f;
        float f1 = (float)(startColor >> 16 & 0xFF) / 255.0f;
        float f2 = (float)(startColor >> 8 & 0xFF) / 255.0f;
        float f3 = (float)(startColor & 0xFF) / 255.0f;
        float f4 = (float)(endColor >> 24 & 0xFF) / 255.0f;
        float f5 = (float)(endColor >> 16 & 0xFF) / 255.0f;
        float f6 = (float)(endColor >> 8 & 0xFF) / 255.0f;
        float f7 = (float)(endColor & 0xFF) / 255.0f;
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.shadeModel(7425);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder vertexbuffer = tessellator.getBuffer();
        vertexbuffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        vertexbuffer.pos((double)x + (double)w, y, 0.0).color(f1, f2, f3, f).endVertex();
        vertexbuffer.pos(x, y, 0.0).color(f1, f2, f3, f).endVertex();
        vertexbuffer.pos(x, (double)y + (double)h, 0.0).color(f5, f6, f7, f4).endVertex();
        vertexbuffer.pos((double)x + (double)w, (double)y + (double)h, 0.0).color(f5, f6, f7, f4).endVertex();
        tessellator.draw();
        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    public static void enableGL2D() {
        GL11.glDisable((int)2929);
        GL11.glEnable((int)3042);
        GL11.glDisable((int)3553);
        GL11.glBlendFunc((int)770, (int)771);
        GL11.glDepthMask((boolean)true);
        GL11.glEnable((int)2848);
        GL11.glHint((int)3154, (int)4354);
        GL11.glHint((int)3155, (int)4354);
    }

    public static void disableGL2D() {
        GL11.glEnable((int)3553);
        GL11.glEnable((int)2929);
        GL11.glDisable((int)2848);
        GL11.glHint((int)3154, (int)4352);
        GL11.glHint((int)3155, (int)4352);
    }

    public static void glColor(float red, float green, float blue, float alpha) {
        GlStateManager.color(red, green, blue, alpha);
    }

    public static void glColor(Color color) {
        GlStateManager.color((float)color.getRed() / 255.0f, (float)color.getGreen() / 255.0f, (float)color.getBlue() / 255.0f, (float)color.getAlpha() / 255.0f);
    }

    public static void glColor(int color) {
        GlStateManager.color((float)(color >> 16 & 0xFF) / 255.0f, (float)(color >> 8 & 0xFF) / 255.0f, (float)(color & 0xFF) / 255.0f, (float)(color >> 24 & 0xFF) / 255.0f);
    }

    public static Color getHSBColor(float hue, float sturation, float luminance) {
        return Color.getHSBColor(hue, sturation, luminance);
    }

    public static Color getRandomColor(int saturationRandom, float luminance) {
        float hue = random.nextFloat();
        float saturation = ((float)random.nextInt(saturationRandom) + (float)saturationRandom) / (float)saturationRandom + (float)saturationRandom;
        return GLUtils.getHSBColor(hue, saturation, luminance);
    }

    public static Color getRandomColor() {
        return GLUtils.getRandomColor(1000, 0.6f);
    }

    static {
        vbos = new ArrayList<Integer>();
        textures = new ArrayList<Integer>();
    }
}

