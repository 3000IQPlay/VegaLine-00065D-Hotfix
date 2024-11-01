package ru.govno.client.module.modules;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import ru.govno.client.Client;
import ru.govno.client.module.Module;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.module.settings.FloatSettings;
import ru.govno.client.utils.Math.MathUtils;
import ru.govno.client.utils.Minimap.MinimapData;
import ru.govno.client.utils.Render.AnimationUtils;
import ru.govno.client.utils.Render.ColorUtils;
import ru.govno.client.utils.Render.RenderUtils;
import ru.govno.client.utils.Render.StencilUtil;

public class MiniMap
extends Module {
    public static MiniMap get;
    private final MinimapData data;
    public FloatSettings MapOpacity;
    public FloatSettings MapX;
    public FloatSettings MapY;
    public FloatSettings MapScale;
    public BoolSettings MapSmoothing;
    public BoolSettings ShowMaximalLoad;
    public BoolSettings MapBlooming;
    private final boolean[] ISHOVERED = new boolean[2];
    private final AnimationUtils mousePushX = new AnimationUtils(0.0f, 0.0f, 0.04f);
    private final AnimationUtils mousePushY = new AnimationUtils(0.0f, 0.0f, 0.04f);
    private final AnimationUtils hoverInScaleFactor = new AnimationUtils(1.0f, 1.0f, 0.07f);
    private final Tessellator tessellator = Tessellator.getInstance();
    private final BufferBuilder buffer = this.tessellator.getBuffer();
    private float smoothMapRotate;

    public MiniMap() {
        super("MiniMap", 0, Module.Category.RENDER);
        this.data = new MinimapData(64);
        this.MapOpacity = new FloatSettings("MapOpacity", 0.8f, 1.0f, 0.1f, this);
        this.settings.add(this.MapOpacity);
        this.MapX = new FloatSettings("MapX", 0.025f, 1.0f, 0.0f, this, () -> false);
        this.settings.add(this.MapX);
        this.MapY = new FloatSettings("MapY", 0.2f, 1.0f, 0.0f, this, () -> false);
        this.settings.add(this.MapY);
        this.MapScale = new FloatSettings("MapScale", 80.0f, 400.0f, 40.0f, this, () -> false);
        this.settings.add(this.MapScale);
        this.MapSmoothing = new BoolSettings("MapSmoothing", true, this);
        this.settings.add(this.MapSmoothing);
        this.ShowMaximalLoad = new BoolSettings("ShowMaximalLoad", false, this);
        this.settings.add(this.ShowMaximalLoad);
        this.MapBlooming = new BoolSettings("MapBlooming", false, this);
        this.settings.add(this.MapBlooming);
        get = this;
    }

    public float getMapX(ScaledResolution sr) {
        return (float)sr.getScaledWidth() * this.MapX.getAnimation();
    }

    public float getMapY(ScaledResolution sr) {
        return (float)sr.getScaledHeight() * this.MapY.getAnimation();
    }

    public float getMapScale() {
        return this.MapScale.getAnimation();
    }

    public boolean[] isHoveredToMinimap(int mouseX, int mouseY, ScaledResolution sr) {
        if (this.isActived()) {
            this.ISHOVERED[1] = RenderUtils.isHovered(mouseX, mouseY, this.getMapX(sr) + this.getMapScale() - 8.0f, this.getMapY(sr) - 8.0f + this.getMapScale(), 8.0f, 8.0f);
            this.ISHOVERED[0] = !this.ISHOVERED[1] && RenderUtils.isHovered(mouseX, mouseY, this.getMapX(sr), this.getMapY(sr), this.getMapScale(), this.getMapScale());
        }
        return this.ISHOVERED;
    }

    private void mouseHoverPushScreen(int mouseX, int mouseY, float centerX, float centerY, float mapScale, ScaledResolution sr) {
        boolean[] hover = this.isHoveredToMinimap(mouseX, mouseY, sr);
        boolean isInChat = mc.currentScreen instanceof GuiChat;
        boolean hoverScreen = isInChat && (hover[0] || ((GuiChat)mc.currentScreen).dragging12[0]) && !((GuiChat)mc.currentScreen).dragging12[1];
        boolean hoverDrag = isInChat && ((GuiChat)mc.currentScreen).dragging12[1] && !hoverScreen;
        float scaleTo = hoverDrag ? 0.9F : (hoverScreen ? 1.5F : 1.0F);
        this.hoverInScaleFactor.to = scaleTo;
        float moveFactor = this.hoverInScaleFactor.getAnim() * 1.5F;
        float scallingToX = hoverDrag ? centerX + mapScale / 2.0F : (hoverScreen ? MathUtils.lerp(centerX, (float)mouseX, moveFactor) : centerX);
        float scallingToY = hoverDrag ? centerY + mapScale / 2.0F : (hoverScreen ? MathUtils.lerp(centerY, (float)mouseY, moveFactor) : centerY);
        if (this.mousePushX.getAnim() == 0.0F) {
            this.mousePushX.setAnim(centerX);
        }

        if (this.mousePushY.getAnim() == 0.0F) {
            this.mousePushY.setAnim(centerY);
        }

        this.mousePushX.speed = scallingToX == centerX ? 0.03F : (isInChat && ((GuiChat)mc.currentScreen).dragging12[0] ? 1.0F : 0.08F);
        this.mousePushY.speed = scallingToY == centerY ? 0.03F : (isInChat && ((GuiChat)mc.currentScreen).dragging12[0] ? 1.0F : 0.08F);
        this.mousePushX.to = scallingToX;
        this.mousePushY.to = scallingToY;
        RenderUtils.customScaledObject2D(this.mousePushX.anim, this.mousePushY.anim, 0.0F, 0.0F, this.hoverInScaleFactor.anim);
    }

    public float getRound() {
        return this.getMapScale() / 10.0f;
    }

    private void drawVecsOfPoints(Runnable drawMap, boolean bloom) {
        GL11.glDepthMask((boolean)false);
        GL11.glEnable((int)3042);
        GL11.glBlendFunc((int)770, (int)771);
        GL11.glDisable((int)2884);
        GL11.glEnable((int)3553);
        GL11.glAlphaFunc((int)516, (float)0.003921569f);
        if (bloom) {
            GL11.glBlendFunc((int)770, (int)32772);
        }
        drawMap.run();
        if (bloom) {
            GL11.glBlendFunc((int)770, (int)771);
        }
        GlStateManager.resetColor();
        GlStateManager.enableRescaleNormal();
        GL11.glEnable((int)2884);
        GL11.glAlphaFunc((int)516, (float)0.1f);
        GL11.glDepthMask((boolean)true);
    }

    private List<EntityLivingBase> basesToPointing() {
        return MiniMap.mc.world == null ? new ArrayList<EntityLivingBase>() : MiniMap.mc.world.getLoadedEntityList().stream().map(Entity::getLivingBaseOf).filter(base -> !(base instanceof EntityPlayerSP)).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private int getEntityPointColor(EntityLivingBase base) {
        int color = ColorUtils.getColor(164, 255, 100, 70);
        if (base instanceof EntityPlayer) {
            color = Client.friendManager.isFriend(base.getName()) ? ColorUtils.getColor(40, 255, 40) : (!base.getUniqueID().equals(UUID.nameUUIDFromBytes(("OfflinePlayer:" + base.getName()).getBytes(StandardCharsets.UTF_8))) && base instanceof EntityOtherPlayerMP ? ColorUtils.getColor(220, 125, 30) : ColorUtils.getColor(255, 40, 20));
        }
        return color;
    }

    private void drawEntityPoint(EntityLivingBase base, float centerX, float centerY, float cameraX, float cameraZ, float mapScale, float scaleFactor, float pTicks, float alphaPC, MinimapData data) {
        float y;
        if (base == null || !base.isEntityAlive()) {
            return;
        }
        scaleFactor *= mapScale;
        scaleFactor /= (float)data.getRange();
        double smoothPosX = base.prevPosX + (base.posX - base.prevPosX) * (double)pTicks;
        float x = (float)(smoothPosX - (double)cameraX) * (scaleFactor /= 1.5f);
        double smoothPosZ = base.prevPosZ + (base.posZ - base.prevPosZ) * (double)pTicks;
        if (Math.sqrt(x * x + (y = (float)(smoothPosZ - (double)cameraZ) * scaleFactor) * y) > (double)mapScale) {
            return;
        }
        x += centerX;
        y += centerY;
        float degrRadius = 0.05f * mapScale * 60.0f / (float)data.getRange();
        float degress = 52.5f;
        float pointS1 = mapScale / (base instanceof EntityOtherPlayerMP ? 16.0f : 32.0f);
        float pointS2 = pointS1 * 1.125f;
        int baseColor = this.getEntityPointColor(base);
        baseColor = ColorUtils.swapAlpha(baseColor, (float)ColorUtils.getAlphaFromColor(baseColor) * alphaPC);
        GL11.glDisable((int)3553);
        GL11.glShadeModel((int)7425);
        float entityYaw = base.prevRotationYaw + (base.rotationYaw - base.prevRotationYaw) * pTicks + 180.0f;
        double rotMin = entityYaw - degress;
        double rotMax = entityYaw + degress;
        double rotMinRadian = Math.toRadians(rotMin);
        double rotMaxRadian = Math.toRadians(rotMax);
        if (base instanceof EntityOtherPlayerMP) {
            GL11.glBlendFunc((int)770, (int)32772);
            GL11.glLineWidth((float)0.5f);
            this.buffer.begin(3, DefaultVertexFormats.POSITION_COLOR);
            this.buffer.pos((double)x + Math.sin(rotMinRadian) * (double)degrRadius, (double)y - Math.cos(rotMinRadian) * (double)degrRadius).color(ColorUtils.swapAlpha(baseColor, (float)ColorUtils.getAlphaFromColor(baseColor) / 5.0f)).endVertex();
            this.buffer.pos(x, y).color(baseColor).endVertex();
            this.buffer.pos((double)x + Math.sin(rotMaxRadian) * (double)degrRadius, (double)y - Math.cos(rotMaxRadian) * (double)degrRadius).color(ColorUtils.swapAlpha(baseColor, (float)ColorUtils.getAlphaFromColor(baseColor) / 5.0f)).endVertex();
            this.tessellator.draw();
            GL11.glLineWidth((float)1.0f);
            int polygonC1 = ColorUtils.swapAlpha(baseColor, (float)ColorUtils.getAlphaFromColor(baseColor) / 4.0f);
            int polygonC2 = ColorUtils.swapAlpha(baseColor, 0.0f);
            this.buffer.begin(6, DefaultVertexFormats.POSITION_COLOR);
            this.buffer.pos(x, y).color(polygonC1).endVertex();
            for (double rot = rotMin; rot <= rotMax; rot += (rotMax - rotMin) / 3.0) {
                double radian = Math.toRadians(rot);
                this.buffer.pos((double)x + Math.sin(radian) * (double)degrRadius, (double)y - Math.cos(radian) * (double)degrRadius).color(polygonC2).endVertex();
            }
            this.tessellator.draw();
            GL11.glBlendFunc((int)770, (int)771);
        }
        GL11.glEnable((int)2832);
        GL11.glPointSize((float)pointS2);
        this.buffer.begin(0, DefaultVertexFormats.POSITION_COLOR);
        this.buffer.pos(x, y).color(ColorUtils.toDark(baseColor, 0.275f)).endVertex();
        this.tessellator.draw();
        GL11.glPointSize((float)pointS1);
        this.buffer.begin(0, DefaultVertexFormats.POSITION_COLOR);
        this.buffer.pos(x, y).color(baseColor).endVertex();
        this.tessellator.draw();
        GL11.glPointSize((float)1.0f);
        GL11.glShadeModel((int)7424);
        GL11.glEnable((int)3553);
    }

    private void drawSelfPoint(float screenX, float screenY, float mapScale, float scaleFactor, float alphaPC) {
        GlStateManager.resetColor();
        GL11.glDisable((int)3553);
        GL11.glBlendFunc((int)770, (int)32772);
        GL11.glEnable((int)2832);
        int shadowCol = ColorUtils.swapAlpha(-1, 40.0f * alphaPC);
        int shadowCol2 = ColorUtils.swapAlpha(-1, 35.0f * alphaPC);
        float shadowSize = 10.0f * scaleFactor * mapScale / 200.0f;
        float shadowRadius = 2.25f * mapScale / 200.0f * 1.5f;
        RenderUtils.drawCroneShadow(screenX, screenY, 0, 360, shadowRadius, shadowSize, shadowCol, 0, false);
        RenderUtils.drawCroneShadow(screenX, screenY, 0, 360, shadowRadius, shadowSize / 5.0f, shadowCol2, 0, false);
        RenderUtils.drawCroneShadow(screenX, screenY, 0, 360, shadowRadius / 1.5f, shadowRadius / 3.0f, 0, shadowCol2, false);
        GL11.glDisable((int)3553);
        GL11.glBlendFunc((int)770, (int)771);
        GL11.glPointSize((float)(15.0f * scaleFactor * mapScale / 200.0f * 1.05f));
        this.buffer.begin(0, DefaultVertexFormats.POSITION_COLOR);
        this.buffer.pos(screenX, screenY).color(ColorUtils.swapAlpha(ColorUtils.toDark(-1, 0.5f), 255.0f * alphaPC)).endVertex();
        this.tessellator.draw();
        GL11.glPointSize((float)(15.0f * scaleFactor * mapScale / 200.0f));
        this.buffer.begin(0, DefaultVertexFormats.POSITION_COLOR);
        this.buffer.pos(screenX, screenY).color(ColorUtils.swapAlpha(-1, 255.0f * alphaPC)).endVertex();
        this.tessellator.draw();
        GL11.glPointSize((float)(15.0f * scaleFactor * mapScale / 200.0f / 1.25f));
        this.buffer.begin(0, DefaultVertexFormats.POSITION_COLOR);
        this.buffer.pos(screenX, screenY).color(ColorUtils.swapAlpha(ColorUtils.toDark(-1, 0.25f), 255.0f * alphaPC)).endVertex();
        this.tessellator.draw();
        GL11.glPointSize((float)(15.0f * scaleFactor * mapScale / 200.0f / 1.75f));
        this.buffer.begin(0, DefaultVertexFormats.POSITION_COLOR);
        this.buffer.pos(screenX, screenY).color(ColorUtils.swapAlpha(ColorUtils.toDark(-1, 0.75f), 255.0f * alphaPC)).endVertex();
        this.tessellator.draw();
        GL11.glPointSize((float)1.0f);
        GL11.glBlendFunc((int)770, (int)771);
        GL11.glEnable((int)3553);
    }

    private void drawAllChunkColorBuffers(MinimapData data, float screenX, float screenY, float scale, float alphaPC, float mapRotate, float mapScale, float pTicks, ScaledResolution sr, boolean smoothPixels, boolean bloom) {
        if (Minecraft.player == null || alphaPC * 255.0f < 1.0f) {
            return;
        }
        float delta = (float)MathUtils.easeInOutQuad(MathUtils.clamp(MathUtils.getDifferenceOf(this.smoothMapRotate, mapRotate) / 90.0, 0.0, 1.0));
        delta *= delta;
        this.smoothMapRotate = MathUtils.lerp(this.smoothMapRotate, mapRotate, delta);
        double dx = data.getLastPlayerDX() - MathUtils.lerp(Minecraft.player.lastTickPosX, Minecraft.player.posX, (double)pTicks);
        double dz = data.getLastPlayerDZ() - MathUtils.lerp(Minecraft.player.lastTickPosZ, Minecraft.player.posZ, (double)pTicks);
        float round = this.getRound();
        int bColor1 = ColorUtils.getColor(0, (int)(70.0f * alphaPC));
        int outColor2 = ColorUtils.getColor(0, (int)(95.0f * alphaPC));
        int outColor3 = ColorUtils.getColor(0, (int)(145.0f * alphaPC));
        RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(screenX, screenY, screenX + scale, screenY + scale, round, round / 4.0f, bColor1, bColor1, bColor1, bColor1, false, true, true);
        RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(screenX, screenY, screenX + scale, screenY + scale, round, 0.5f, outColor3, outColor3, outColor3, outColor3, false, false, true);
        StencilUtil.initStencilToWrite();
        RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(screenX, screenY, screenX + scale, screenY + scale, round, 0.0f, -1, -1, -1, -1, false, true, false);
        StencilUtil.readStencilBuffer(1);
        GL11.glPushMatrix();
        this.mouseHoverPushScreen((int)((double)Mouse.getX() / 2.0), (int)((double)sr.getScaledHeight() - (double)Mouse.getY() / 2.0), screenX + scale / 2.0f, screenY + scale / 2.0f, scale, sr);
        float finalScreenX = (float)((double)screenX + dx * (double)scale / (double)data.getRange() - 0.25);
        float finalScreenY = (float)((double)screenY + dz * (double)scale / (double)data.getRange() - 0.25);
        if (mapScale != 1.0f) {
            RenderUtils.customScaledObject2D(screenX, screenY, scale, scale, mapScale);
        }
        int mapColor = ColorUtils.swapAlpha(-1, 255.0f * alphaPC);
        this.drawVecsOfPoints(() -> {
            GL11.glPushMatrix();
            data.getTexture().setBlurMipmap(smoothPixels, smoothPixels);
            TextureUtil.bindTexture(data.getTexture().getGlTextureId());
            RenderUtils.customRotatedObject2D(screenX, screenY, scale, scale, -this.smoothMapRotate + 180.0f);
            this.buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            this.buffer.pos(finalScreenX, finalScreenY).tex(0.0, 0.0).color(mapColor).endVertex();
            this.buffer.pos(finalScreenX + scale, finalScreenY).tex(1.0, 0.0).color(mapColor).endVertex();
            this.buffer.pos(finalScreenX + scale, finalScreenY + scale).tex(1.0, 1.0).color(mapColor).endVertex();
            this.buffer.pos(finalScreenX, finalScreenY + scale).tex(0.0, 1.0).color(mapColor).endVertex();
            GL11.glAlphaFunc((int)516, (float)(27.0f * alphaPC / 255.0f));
            if (bloom) {
                GL11.glBlendFunc((int)770, (int)32772);
            }
            this.tessellator.draw();
            if (bloom) {
                GL11.glBlendFunc((int)770, (int)771);
            }
            GL11.glAlphaFunc((int)516, (float)0.003921569f);
            this.basesToPointing().forEach(base -> this.drawEntityPoint((EntityLivingBase)base, screenX + scale / 2.0f, screenY + scale / 2.0f, (float)RenderManager.renderPosX, (float)RenderManager.renderPosZ, scale, mapScale, pTicks, alphaPC, data));
            this.drawSelfPoint(screenX + scale / 2.0f, screenY + scale / 2.0f, scale, mapScale, alphaPC);
            GL11.glPopMatrix();
        }, bloom);
        GL11.glPopMatrix();
        StencilUtil.uninitStencilBuffer();
        RenderUtils.drawInsideFullRoundedFullGradientShadowRectWithBloomBool(screenX, screenY, screenX + scale, screenY + scale, round - 1.0f, 1.0f, outColor3, outColor3, outColor3, outColor3, false);
        RenderUtils.drawRoundedFullGradientInsideShadow(screenX, screenY, screenX + scale, screenY + scale, round * 2.0f, outColor2, outColor2, outColor2, outColor2, false);
        GuiScreen guiScreen = MiniMap.mc.currentScreen;
        if (guiScreen instanceof GuiChat) {
            GuiChat chat = (GuiChat)guiScreen;
            if (!chat.dragging12[1]) {
                int moveMarkColor = ColorUtils.swapAlpha(-1, 155.0f * (0.3f + 0.7f * alphaPC));
                RenderUtils.drawLightContureRectSmooth(screenX + scale - 8.0f, screenY + scale - 8.0f, screenX + scale, screenY + scale, moveMarkColor);
                RenderUtils.render2D(3, DefaultVertexFormats.POSITION_COLOR, 0.5f, () -> {
                    RenderUtils.buffer.pos(screenX + scale - 8.0f, screenY + scale).color(moveMarkColor).endVertex();
                    RenderUtils.buffer.pos(screenX + scale, screenY + scale - 8.0f).color(moveMarkColor).endVertex();
                });
            }
        }
    }

    @Override
    public void alwaysRender2D(ScaledResolution sr) {
        if (!this.isActived()) {
            this.stateAnim.to = 0.0f;
            if (this.stateAnim.anim * 255.0f < 1.0f) {
                return;
            }
        }
        float alphaPC = this.stateAnim.getAnim();
        if (this.actived && this.stateAnim.to == 0.0f) {
            this.stateAnim.to = 1.0f;
        }
        alphaPC *= this.MapOpacity.getFloat();
        float mapX = this.getMapX(sr);
        float mapY = this.getMapY(sr);
        float scale = this.getMapScale();
        float pTicks = mc.getRenderPartialTicks();
        float yaw = Minecraft.player.lastReportedPreYaw + (Minecraft.player.rotationYaw - Minecraft.player.lastReportedPreYaw) * pTicks;
        if (Minecraft.player.isRiding()) {
            float pYaw = Minecraft.player.rotationYaw;
            yaw = Minecraft.player.ridingEntity.prevRotationYaw + (Minecraft.player.ridingEntity.rotationYaw - Minecraft.player.ridingEntity.prevRotationYaw) * pTicks;
            yaw += pYaw - yaw;
        }
        this.drawAllChunkColorBuffers(this.data, mapX, mapY, scale, alphaPC, yaw, this.ShowMaximalLoad.getBool() ? 1.0f : 1.4f, pTicks, sr, this.MapSmoothing.getBool(), this.MapBlooming.getBool());
    }

    @Override
    public void onUpdate() {
        if (MiniMap.mc.world != null && Minecraft.player != null) {
            int range = (int)MathUtils.clamp((float)(16 * MiniMap.mc.gameSettings.renderDistanceChunks + 16) / 1.5f, 48.0f, 192.0f);
            if (range != this.data.getRange()) {
                this.data.setRange(range);
            }
            this.data.updateMap(MiniMap.mc.world, Minecraft.player);
        }
    }
}

