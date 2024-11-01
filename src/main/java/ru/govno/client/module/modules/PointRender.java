package ru.govno.client.module.modules;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector4d;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.glu.Sphere;
import ru.govno.client.module.modules.PointTrace;
import ru.govno.client.newfont.CFontRenderer;
import ru.govno.client.newfont.Fonts;
import ru.govno.client.utils.Math.MathUtils;
import ru.govno.client.utils.Render.ColorUtils;
import ru.govno.client.utils.Render.RenderUtils;

public class PointRender {
    private static final ResourceLocation TEXTURE = new ResourceLocation("vegaline/system/points/pointsmark.png");
    private static final ResourceLocation TEXTURE2 = new ResourceLocation("vegaline/system/points/pointsmark2.png");
    private final IntBuffer viewport = GLAllocation.createDirectIntBuffer(16);
    private final FloatBuffer modelview = GLAllocation.createDirectFloatBuffer(16);
    private final FloatBuffer projection = GLAllocation.createDirectFloatBuffer(16);
    private final FloatBuffer vector = GLAllocation.createDirectFloatBuffer(4);

    public void render2D() {
        this.renderVoid2d();
    }

    public void render3D() {
        this.renderVoid3d();
    }

    public void renderPoints2d() {
        List<PointTrace> pointsList = PointTrace.getPointList().stream().filter(point -> point.getServerName().equalsIgnoreCase(Minecraft.getMinecraft().isSingleplayer() || Minecraft.getMinecraft().getCurrentServerData() == null ? "SinglePlayer" : Minecraft.getMinecraft().getCurrentServerData().serverIP)).toList();
        if (pointsList.isEmpty()) {
            return;
        }
        GL11.glPushMatrix();
        GL11.glDisable((int)2896);
        float partialTicks = Minecraft.getMinecraft().getRenderPartialTicks();
        int scaleFactor = ScaledResolution.getScaleFactor();
        double scaling = (double)scaleFactor / Math.pow(scaleFactor, 2.0);
        GL11.glScaled((double)scaling, (double)scaling, (double)scaling);
        EntityRenderer entityRenderer = Minecraft.getMinecraft().entityRenderer;
        for (PointTrace points : pointsList) {
            boolean isDeathPoint = points.getName().startsWith("Death");
            double px = PointTrace.getX(points);
            double py = PointTrace.getY(points);
            double pz = PointTrace.getZ(points);
            if (Minecraft.player.dimension == -1 && PointTrace.getDemension(points) != -1) {
                px = PointTrace.getX(points) / 8.0;
                py = PointTrace.getY(points);
                pz = PointTrace.getZ(points) / 8.0;
            } else if (Minecraft.player.dimension != -1 && PointTrace.getDemension(points) == -1) {
                px = PointTrace.getX(points) * 8.0;
                py = PointTrace.getY(points);
                pz = PointTrace.getZ(points) * 8.0;
            }
            float pTicks = Minecraft.getMinecraft().getRenderPartialTicks();
            double xposme = Minecraft.player.lastTickPosX + (Minecraft.player.posX - Minecraft.player.lastTickPosX) * (double)pTicks;
            double yposme = Minecraft.player.lastTickPosY + (Minecraft.player.posY - Minecraft.player.lastTickPosY) * (double)pTicks;
            double zposme = Minecraft.player.lastTickPosZ + (Minecraft.player.posZ - Minecraft.player.lastTickPosZ) * (double)pTicks;
            double x = px;
            double y = py;
            double z = pz;
            double distance = MathHelper.sqrt((px - xposme) * (px - xposme) + (py - yposme) * (py - yposme) + (pz - zposme) * (pz - zposme));
            double distanceXZ = MathHelper.sqrt((px - xposme) * (px - xposme) + (pz - zposme) * (pz - zposme));
            double maxDistance = 128.0;
            if (distanceXZ > maxDistance) {
                Vec3d fixatedDistantVec = new Vec3d(x - xposme, y - (yposme + (double)Minecraft.player.getEyeHeight()), z - zposme).scale(1.0 / distance * maxDistance).addVector(xposme, yposme + (double)Minecraft.player.getEyeHeight(), zposme);
                x = fixatedDistantVec.xCoord;
                y = fixatedDistantVec.yCoord;
                z = fixatedDistantVec.zCoord;
            }
            AxisAlignedBB aabb = new AxisAlignedBB(x, y, z, x, y, z);
            x = px;
            y = py;
            z = pz;
            Vector3d[] vectors = new Vector3d[]{new Vector3d(aabb.minX, aabb.minY, aabb.minZ), new Vector3d(aabb.minX, aabb.maxY, aabb.minZ), new Vector3d(aabb.maxX, aabb.minY, aabb.minZ), new Vector3d(aabb.maxX, aabb.maxY, aabb.minZ), new Vector3d(aabb.minX, aabb.minY, aabb.maxZ), new Vector3d(aabb.minX, aabb.maxY, aabb.maxZ), new Vector3d(aabb.maxX, aabb.minY, aabb.maxZ), new Vector3d(aabb.maxX, aabb.maxY, aabb.maxZ)};
            entityRenderer.setupCameraTransform(partialTicks, 0);
            Vector4d position = null;
            Vector3d[] var32 = vectors;
            int var33 = vectors.length;
            for (int var34 = 0; var34 < var33; ++var34) {
                Vector3d vector = var32[var34];
                vector = this.project2D(scaleFactor, vector.x - RenderManager.viewerPosX, vector.y - RenderManager.viewerPosY, vector.z - RenderManager.viewerPosZ);
                if (vector == null || !(vector.z >= 0.0) || !(vector.z < 1.0)) continue;
                if (position == null) {
                    position = new Vector4d(vector.x, vector.y, vector.z, 0.0);
                }
                position.x = Math.min(vector.x, position.x);
                position.y = Math.min(vector.y, position.y);
                position.z = Math.max(vector.x, position.z);
                position.w = Math.max(vector.y, position.w);
            }
            if (position == null) continue;
            entityRenderer.setupOverlayRendering();
            double posX = position.x;
            double posY = position.y;
            double endPosX = position.z;
            double endPosY = position.w;
            CFontRenderer font = Fonts.mntsb_7;
            int timeInterval = isDeathPoint ? 350 : 700;
            float pcTime = (float)((System.currentTimeMillis() + (long)points.getIndex() * 350L) % (long)timeInterval) / (float)timeInterval;
            pcTime = ((double)pcTime > 0.5 ? 1.0f - pcTime : pcTime) * 2.0f;
            pcTime *= pcTime;
            float yExtend = 0.0f;
            if (points.getName().toLowerCase().contains("home") || points.getName().toLowerCase().contains("\u0434\u043e\u043c") || points.getName().toLowerCase().contains("death")) {
                yExtend += -(isDeathPoint ? 2.0f : 5.0f) * pcTime;
            }
            float xp = (float)(posX + (endPosX - posX) / 2.0);
            float yp = (float)(posY - 5.0) + yExtend;
            GlStateManager.enableBlend();
            Minecraft.getMinecraft().getTextureManager().bindTexture(isDeathPoint ? TEXTURE2 : TEXTURE);
            int markCol = ColorUtils.swapAlpha(PointTrace.getDemension(points) == -1 ? ColorUtils.getColor(255, 40, 95) : (PointTrace.getDemension(points) == 1 ? ColorUtils.getColor(255, 255, 95) : ColorUtils.getColor(40, 255, 95)), 155.0f);
            double texW = isDeathPoint ? 12.0 : 8.0;
            double texH = 12.0;
            double texX = (double)xp - texW / 2.0;
            double texY = (double)yp - texH;
            String coords = (int)x + " " + (int)y + " " + (int)z;
            String dst = String.format("%.1f", distanceXZ) + "m";
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuffer();
            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            bufferbuilder.pos(texX, texY + texH).tex(0.0, 1.0).color(markCol).endVertex();
            bufferbuilder.pos(texX + texW, texY + texH).tex(1.0, 1.0).color(markCol).endVertex();
            bufferbuilder.pos(texX + texW, texY).tex(1.0, 0.0).color(markCol).endVertex();
            bufferbuilder.pos(texX, texY).tex(0.0, 0.0).color(markCol).endVertex();
            GL11.glShadeModel((int)7425);
            GL11.glEnable((int)3553);
            tessellator.draw();
            markCol = ColorUtils.getOverallColorFrom(markCol, -1);
            if (isDeathPoint) {
                float name$dstWidth = (float)font.getStringWidth(points.getName() + " " + dst) + 2.0f;
                font.drawString(points.getName() + " " + dst, texX + texW / 2.0 - (double)(name$dstWidth / 2.0f), texY + texH - (double)yExtend + 2.0, markCol);
                font.drawString(coords, texX + texW / 2.0 - (double)((float)font.getStringWidth(coords) / 2.0f), texY - (double)yExtend + texH + 6.0, markCol);
                continue;
            }
            font.drawString(dst + " " + points.getName(), texX - (double)((float)font.getStringWidth(dst + " " + points.getName()) / 2.0f) + texW / 2.0, texY + texH + 2.0 - (double)(yExtend / 2.0f), markCol);
            font.drawString(coords, texX - (double)((float)font.getStringWidth(coords) / 2.0f) + texW / 2.0, texY + texH + 5.5 - (double)(yExtend / 1.5f), markCol);
        }
        GL11.glEnable((int)2929);
        GlStateManager.enableBlend();
        entityRenderer.setupOverlayRendering();
        GL11.glPopMatrix();
    }

    public void drawSphere3dPolygon(double x, double y, double z, float radius, int shapes, int color) {
        Sphere sphere = new Sphere();
        RenderUtils.glColor(color);
        GL11.glTranslated((double)x, (double)y, (double)z);
        GL11.glRotated((double)90.0, (double)1.0, (double)0.0, (double)0.0);
        sphere.setDrawStyle(100012);
        sphere.draw(radius, shapes, shapes * 2);
        GL11.glRotated((double)90.0, (double)-1.0, (double)0.0, (double)0.0);
        GL11.glTranslated((double)(-x), (double)(-y), (double)(-z));
        GlStateManager.resetColor();
    }

    public void drawSphere3dPoints(double x, double y, double z, float radius, int shapes, int color) {
        Sphere sphere = new Sphere();
        RenderUtils.glColor(color);
        GL11.glTranslated((double)x, (double)y, (double)z);
        GL11.glRotated((double)90.0, (double)1.0, (double)0.0, (double)0.0);
        sphere.setDrawStyle(100010);
        sphere.draw(radius, shapes, shapes * 2);
        GL11.glRotated((double)90.0, (double)-1.0, (double)0.0, (double)0.0);
        GL11.glTranslated((double)(-x), (double)(-y), (double)(-z));
        GlStateManager.resetColor();
    }

    public void drawSphere3dLines(double x, double y, double z, float radius, int shapes, int color) {
        Sphere sphere = new Sphere();
        RenderUtils.glColor(color);
        GL11.glTranslated((double)x, (double)y, (double)z);
        GL11.glRotated((double)90.0, (double)1.0, (double)0.0, (double)0.0);
        sphere.setDrawStyle(100011);
        sphere.draw(radius, shapes, shapes * 2);
        GL11.glRotated((double)90.0, (double)-1.0, (double)0.0, (double)0.0);
        GL11.glTranslated((double)(-x), (double)(-y), (double)(-z));
        GlStateManager.resetColor();
    }

    public void renderPoints3d() {
        long stepTime;
        List<PointTrace> pointsList = PointTrace.getPointList().stream().filter(point -> point.getServerName().equalsIgnoreCase(Minecraft.getMinecraft().isSingleplayer() || Minecraft.getMinecraft().getCurrentServerData() == null ? "SinglePlayer" : Minecraft.getMinecraft().getCurrentServerData().serverIP)).toList();
        if (pointsList.isEmpty()) {
            return;
        }
        long time = System.currentTimeMillis();
        float timePC = (float)(time % (stepTime = 1500L)) / (float)stepTime;
        float animDelta = (float)MathUtils.easeInOutQuadWave(timePC);
        if (animDelta != 0.0f && Minecraft.player != null) {
            double minRange = 0.7f;
            double maxRange = 0.85f;
            int minSteps = 20;
            int maxSteps = 70;
            int alphaTrace = 255;
            int minShapes = 6;
            int maxShapes = 9;
            double glX = RenderManager.viewerPosX;
            double glY = RenderManager.viewerPosY;
            double glZ = RenderManager.viewerPosZ;
            GL11.glPushMatrix();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_CONSTANT_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            Minecraft.getMinecraft().entityRenderer.disableLightmap();
            GL11.glEnable((int)3042);
            GL11.glAlphaFunc((int)516, (float)0.0f);
            GL11.glLineWidth((float)1.0f);
            GL11.glDisable((int)2884);
            GL11.glDepthMask((boolean)false);
            GL11.glDisable((int)3553);
            GL11.glEnable((int)2929);
            GL11.glDisable((int)2896);
            GL11.glPointSize((float)1.0f);
            GL11.glEnable((int)2832);
            GL11.glShadeModel((int)7425);
            GL11.glTranslated((double)(-glX), (double)(-glY), (double)(-glZ));
            double rotate = (float)(System.currentTimeMillis() % 9000L) / 9000.0f * 360.0f;
            for (PointTrace point2 : pointsList) {
                float dstAPC;
                if (point2.dimension != Minecraft.player.dimension || (dstAPC = (float)MathUtils.clamp((10.0 - Minecraft.player.getDistance(point2.x, point2.y, point2.z)) / 9.0, 0.0, 1.0)) == 0.0f) continue;
                int steps = (int)MathUtils.lerp((double)minSteps, (double)maxSteps, Math.min(MathUtils.easeOutCirc(dstAPC) * (double)animDelta * 1.5, 1.0));
                int shapes = maxShapes;
                if (steps == 0) continue;
                double statRange = MathUtils.lerp(minRange, maxRange, (double)(animDelta / 5.0f));
                int markCol = PointTrace.getDemension(point2) == -1 ? ColorUtils.getColor(255, 40, 95) : (PointTrace.getDemension(point2) == 1 ? ColorUtils.getColor(255, 255, 95) : ColorUtils.getColor(40, 255, 95));
                GL11.glTranslated((double)point2.x, (double)point2.y, (double)point2.z);
                GL11.glRotated((double)rotate, (double)0.0, (double)1.0, (double)0.0);
                GL11.glTranslated((double)(-point2.x), (double)(-point2.y), (double)(-point2.z));
                GL11.glHint((int)3154, (int)4354);
                GL11.glEnable((int)2848);
                GL11.glLineWidth((float)0.1f);
                this.drawSphere3dLines(point2.x, point2.y, point2.z, (float)statRange, shapes, ColorUtils.swapAlpha(markCol, 255.0f * (float)((double)0.1f + MathUtils.easeInOutQuadWave(animDelta) * (double)0.4f) * dstAPC));
                GL11.glHint((int)3154, (int)4352);
                GL11.glDisable((int)2848);
                GL11.glEnable((int)2884);
                this.drawSphere3dPolygon(point2.x, point2.y, point2.z, (float)statRange, shapes, ColorUtils.swapAlpha(markCol, 20.0f * (float)((double)0.1f + MathUtils.easeInOutQuadWave(animDelta) * (double)0.9f) * dstAPC));
                GL11.glDisable((int)2884);
                GL11.glPointSize((float)0.025f);
                double trans = 0.0;
                for (int indexStep = 0; indexStep < steps; ++indexStep) {
                    float ciclePC = (float)indexStep / (float)steps;
                    int color = ColorUtils.swapAlpha(markCol, (float)alphaTrace * dstAPC * animDelta * (float)MathUtils.easeInOutQuadWave(1.0f - ciclePC * (1.0f - 1.0f / (float)steps)));
                    float range = (float)MathUtils.lerp(statRange, maxRange, (double)(animDelta * ciclePC));
                    GL11.glTranslated((double)0.0, (double)(trans += MathUtils.easeInCircle(ciclePC) * (double)range / 8.0 / (double)steps), (double)0.0);
                    this.drawSphere3dPoints(point2.x, point2.y, point2.z, range, shapes, color);
                }
                GL11.glTranslated((double)0.0, (double)(-trans), (double)0.0);
                GL11.glTranslated((double)point2.x, (double)point2.y, (double)point2.z);
                GL11.glRotated((double)rotate, (double)0.0, (double)-1.0, (double)0.0);
                GL11.glTranslated((double)(-point2.x), (double)(-point2.y), (double)(-point2.z));
            }
            GL11.glTranslated((double)glX, (double)glY, (double)glZ);
            GL11.glLineWidth((float)1.0f);
            GL11.glShadeModel((int)7424);
            GL11.glEnable((int)3553);
            GL11.glDepthMask((boolean)true);
            GL11.glEnable((int)2929);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.resetColor();
            GL11.glEnable((int)2884);
            GL11.glAlphaFunc((int)516, (float)0.1f);
            GL11.glPointSize((float)1.0f);
            GL11.glPopMatrix();
        }
    }

    void renderVoid2d() {
        this.renderPoints2d();
    }

    void renderVoid3d() {
        this.renderPoints3d();
    }

    private Vector3d project2D(int scaleFactor, double x, double y, double z) {
        GL11.glGetFloat((int)2982, (FloatBuffer)this.modelview);
        GL11.glGetFloat((int)2983, (FloatBuffer)this.projection);
        GL11.glGetInteger((int)2978, (IntBuffer)this.viewport);
        return GLU.gluProject((float)((float)x), (float)((float)y), (float)((float)z), (FloatBuffer)this.modelview, (FloatBuffer)this.projection, (IntBuffer)this.viewport, (FloatBuffer)this.vector) ? new Vector3d((double)(this.vector.get(0) / (float)scaleFactor), (double)(((float)Display.getHeight() - this.vector.get(1)) / (float)scaleFactor), (double)this.vector.get(2)) : null;
    }
}

