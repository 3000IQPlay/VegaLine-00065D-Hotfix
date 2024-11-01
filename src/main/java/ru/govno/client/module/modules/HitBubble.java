package ru.govno.client.module.modules;

import java.util.ArrayList;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;
import ru.govno.client.module.Module;
import ru.govno.client.module.modules.ClientColors;
import ru.govno.client.module.modules.WorldRender;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.module.settings.ColorSettings;
import ru.govno.client.module.settings.ModeSettings;
import ru.govno.client.utils.Math.MathUtils;
import ru.govno.client.utils.Render.ColorUtils;
import ru.govno.client.utils.Render.RenderUtils;

public class HitBubble
extends Module {
    public static HitBubble get;
    static final ArrayList<Bubble> bubbles;
    BoolSettings MoreEffects;
    ModeSettings ColorMode;
    ColorSettings PickColor1;
    ColorSettings PickColor2;
    private Vec3d cameraPosUpdated = Vec3d.ZERO;
    private final Tessellator tessellator = Tessellator.getInstance();
    private final BufferBuilder buffer = this.tessellator.getBuffer();
    private final ResourceLocation BUBBLE_TEXTURE = new ResourceLocation("vegaline/modules/hitbubble/bubble.png");
    private final Random RAND = new Random(9012739L);

    public HitBubble() {
        super("HitBubble", 0, Module.Category.RENDER);
        this.ColorMode = new ModeSettings("ColorMode", "Client", this, new String[]{"Rainbow", "Client", "Picker", "DoublePicker"});
        this.settings.add(this.ColorMode);
        this.PickColor1 = new ColorSettings("PickColor1", ColorUtils.getColor(100, 255, 100), this, () -> this.ColorMode.currentMode.contains("Picker"));
        this.settings.add(this.PickColor1);
        this.PickColor2 = new ColorSettings("PickColor2", ColorUtils.getColor(60, 60, 255), this, () -> this.ColorMode.currentMode.endsWith("DoublePicker"));
        this.settings.add(this.PickColor2);
        this.MoreEffects = new BoolSettings("MoreEffects", true, this);
        this.settings.add(this.MoreEffects);
        get = this;
    }

    private Vec3d cameraPos() {
        EntityPlayerSP player;
        if (HitBubble.mc.world != null && (player = Minecraft.player) != null) {
            float partialTicks = mc.getRenderPartialTicks();
            float f = player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * partialTicks;
            float f1 = player.prevRotationYaw + (player.rotationYaw - player.prevRotationYaw) * partialTicks;
            double d0 = player.prevPosX + (player.posX - player.prevPosX) * (double)partialTicks;
            double d1 = player.prevPosY + (player.posY - player.prevPosY) * (double)partialTicks + (double)player.getEyeHeight();
            double d2 = player.prevPosZ + (player.posZ - player.prevPosZ) * (double)partialTicks;
            f += WorldRender.get.offPitchOrient;
            f1 += WorldRender.get.offYawOrient;
            if (Minecraft.getMinecraft().gameSettings.thirdPersonView != 0) {
                int sideMul = Minecraft.getMinecraft().gameSettings.thirdPersonView == 1 ? 1 : -1;
                double camDist = WorldRender.get.cameraRedistance(4.0) * (double)sideMul;
                d0 += Math.sin(Math.toRadians(f1)) * camDist;
                d1 += Math.sin(Math.toRadians(f)) * camDist;
                d2 += -Math.cos(Math.toRadians(f1)) * camDist;
            }
            return new Vec3d(d0, d1, d2).add(WorldRender.get.getLastTranslated());
        }
        return new Vec3d(RenderManager.renderPosX, RenderManager.renderPosY, RenderManager.renderPosZ).add(WorldRender.get.getLastTranslated());
    }

    private static int stateColor(int index, float alphaPC) {
        int color = -1;
        if (get != null) {
            switch (HitBubble.get.ColorMode.getMode()) {
                case "Rainbow": {
                    color = ColorUtils.rainbowGui(0, index);
                    break;
                }
                case "Client": {
                    color = ClientColors.getColor1(index, 1.0f);
                    break;
                }
                case "Picker": {
                    color = HitBubble.get.PickColor1.getCol();
                    break;
                }
                case "DoublePicker": {
                    color = ColorUtils.fadeColor(HitBubble.get.PickColor1.getCol(), HitBubble.get.PickColor2.getCol(), 0.3f, (int)((float)index / 0.3f / 8.0f));
                }
            }
        }
        return ColorUtils.swapAlpha(color, (float)ColorUtils.getAlphaFromColor(color) * alphaPC);
    }

    @Override
    public void onToggled(boolean actived) {
        this.stateAnim.to = actived ? 1.0f : 0.0f;
        super.onToggled(actived);
    }

    @Override
    public void onUpdate() {
        this.stateAnim.to = 1.0f;
    }

    private float getAlphaPC() {
        return this.stateAnim.getAnim();
    }

    private static float getMaxTime() {
        return (double)Minecraft.player.getCooledAttackStrength(0.0f) > 0.8 ? 3000.0f : 1700.0f;
    }

    public static void onAttack(Entity entity) {
        if (entity instanceof EntityLivingBase) {
            EntityLivingBase base = (EntityLivingBase)entity;
            if (base == null || !base.isEntityAlive()) {
                return;
            }
            Vec3d to = base.getPositionVector().addVector(0.0, base.height / 1.55f / (base.isChild() ? 2.25f : 1.0f), 0.0);
            HitBubble.addBubble(to);
        }
    }

    private static void addBubble(Vec3d addToCoord) {
        RenderManager manager = mc.getRenderManager();
        bubbles.add(new Bubble(manager.playerViewX, -manager.playerViewY, addToCoord));
    }

    private void setupDrawsBubbles3D(Runnable render) {
        RenderManager manager = mc.getRenderManager();
        Vec3d conpense = new Vec3d(manager.getRenderPosX(), manager.getRenderPosY(), manager.getRenderPosZ());
        GL11.glDisable((int)2896);
        HitBubble.mc.entityRenderer.disableLightmap();
        GL11.glDepthMask((boolean)false);
        GL11.glDisable((int)2884);
        GL11.glEnable((int)3042);
        GL11.glDisable((int)3008);
        GlStateManager.shadeModel(7425);
        GL11.glTranslated((double)(-conpense.xCoord), (double)(-conpense.yCoord), (double)(-conpense.zCoord));
        mc.getTextureManager().bindTexture(this.BUBBLE_TEXTURE);
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        render.run();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GL11.glTranslated((double)conpense.xCoord, (double)conpense.yCoord, (double)conpense.zCoord);
        GlStateManager.shadeModel(7424);
        GL11.glEnable((int)3008);
        GL11.glDepthMask((boolean)true);
        GL11.glEnable((int)2884);
        GlStateManager.resetColor();
    }

    private void drawBubble(Bubble bubble, float alphaPC) {
        GL11.glPushMatrix();
        Vec3d bXYZ = bubble.pos;
        GL11.glTranslated((double)bXYZ.xCoord, (double)bXYZ.yCoord, (double)bXYZ.zCoord);
        float extS = bubble.getDeltaTime();
        GlStateManager.translate(-Math.sin(Math.toRadians(bubble.viewPitch)) * (double)extS / 3.0, Math.sin(Math.toRadians(bubble.viewYaw)) * (double)extS / 2.0, -Math.cos(Math.toRadians(bubble.viewPitch)) * (double)extS / 3.0);
        GL11.glNormal3d((double)1.0, (double)1.0, (double)1.0);
        GL11.glRotated((double)bubble.viewPitch, (double)0.0, (double)1.0, (double)0.0);
        GL11.glRotated((double)bubble.viewYaw, (double)(HitBubble.mc.gameSettings.thirdPersonView == 2 ? -1.0 : 1.0), (double)0.0, (double)0.0);
        GL11.glScaled((double)-0.1, (double)-0.1, (double)0.1);
        this.drawBeginsNullCoord(bubble, alphaPC);
        GL11.glPopMatrix();
    }

    private void drawBeginsNullCoord(Bubble bubble, float alphaPC) {
        float aPC = (float)MathUtils.easeInOutQuadWave(MathUtils.clamp(bubble.getDeltaTime() + 0.1f, 0.0f, 1.0f) * alphaPC) * 2.0f;
        float f = aPC = aPC > 1.0f ? 1.0f : aPC;
        if ((double)bubble.getDeltaTime() > 0.5) {
            aPC *= aPC;
        }
        float r = 12.5f * (aPC *= alphaPC);
        int speedRotate = 4;
        float III = (float)(System.currentTimeMillis() % (long)(3600 / speedRotate)) / 10.0f * (float)speedRotate;
        this.buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        this.buffer.pos(0.0, 0.0, 0.0).tex(0.0, 0.0).color(HitBubble.stateColor(0, aPC)).endVertex();
        this.buffer.pos(0.0, r, 0.0).tex(0.0, 1.0).color(HitBubble.stateColor(90, aPC)).endVertex();
        this.buffer.pos(r, r, 0.0).tex(1.0, 1.0).color(HitBubble.stateColor(180, aPC)).endVertex();
        this.buffer.pos(r, 0.0, 0.0).tex(1.0, 0.0).color(HitBubble.stateColor(270, aPC)).endVertex();
        RenderUtils.customRotatedObject2D(-1.0f, -1.0f, 2.0f, 2.0f, -III);
        GlStateManager.translate(-r / 2.0f, -r / 2.0f, 0.0f);
        this.tessellator.draw();
        GlStateManager.translate(r / 2.0f, r / 2.0f, 0.0f);
        RenderUtils.customRotatedObject2D(-1.0f, -1.0f, 2.0f, 2.0f, III);
        GlStateManager.translate(-r / 2.0f, -r / 2.0f, 0.0f);
        if (!bubble.parts.isEmpty()) {
            this.buffer.begin(0, DefaultVertexFormats.POSITION_COLOR);
            for (BubblePart part : bubble.parts) {
                float timePC = part.getDeltaTime();
                float radian = (float)part.getRotate(timePC);
                float radiusPC = part.getRPC(timePC) * 1.1f;
                float px = (float)Math.sin(radian) * radiusPC * r * 2.0f + r / 2.0f;
                float py = (float)(-Math.cos(radian)) * radiusPC * r * 2.0f + r / 2.0f;
                float extend = part.getExtend(timePC);
                float partAlphaPC = (1.0f - timePC) * (1.0f - timePC) * aPC * aPC;
                partAlphaPC = (float)MathUtils.easeInOutQuadWave(partAlphaPC);
                int col = HitBubble.stateColor((int)(radian * 180.0f + III), partAlphaPC);
                col = ColorUtils.getOverallColorFrom(col, ColorUtils.getColor(255, 255, 255, ColorUtils.getAlphaFromColor(col)), timePC / 2.0f);
                this.buffer.pos(px, py, -extend * r / 4.0f).color(col).endVertex();
                this.buffer.pos(px, py, extend * r / 4.0f).color(col).endVertex();
            }
            double dst = this.cameraPosUpdated.getDistanceAtEyeByVec(Minecraft.player, bubble.pos.xCoord, bubble.pos.yCoord, bubble.pos.zCoord);
            float partScale = 0.025f + 9.5f * (float)MathUtils.clamp(1.0 - dst / 7.0, 0.0, 1.0);
            GL11.glDisable((int)3553);
            GL11.glPointSize((float)partScale);
            this.tessellator.draw();
            GL11.glPointSize((float)1.0f);
            GL11.glEnable((int)3553);
        }
        GlStateManager.translate(r / 2.0f, r / 2.0f, 0.0f);
    }

    @Override
    public void alwaysRender3D() {
        float aPC = this.getAlphaPC();
        if ((double)aPC < 0.05) {
            return;
        }
        if (bubbles.isEmpty()) {
            return;
        }
        this.removeAuto();
        if (bubbles.isEmpty()) {
            return;
        }
        this.cameraPosUpdated = this.cameraPos();
        if (this.MoreEffects.getBool()) {
            this.addAuto();
        }
        this.setupDrawsBubbles3D(() -> bubbles.forEach(bubble -> {
            if (bubble != null && bubble.getDeltaTime() <= 1.0f) {
                this.drawBubble((Bubble)bubble, aPC);
            }
        }));
    }

    private void removeAuto() {
        bubbles.removeIf(bubble -> bubble.getDeltaTime() >= 1.0f);
        bubbles.forEach(bubble -> bubble.parts.removeIf(part -> part.getDeltaTime() >= 1.0f));
    }

    private void addAuto() {
        int addHZ;
        int addHZCopy = addHZ = 1;
        bubbles.forEach(bubble -> {
            for (int i = 0; i < addHZCopy; ++i) {
                bubble.parts.add(new BubblePart(0.45f, 0.65f, bubble.maxTime / 4.0f * (0.5f + this.RAND.nextFloat(0.5f))));
            }
        });
    }

    static {
        bubbles = new ArrayList();
    }

    private static final class Bubble {
        private final ArrayList<BubblePart> parts = new ArrayList();
        Vec3d pos;
        long time = System.currentTimeMillis();
        float maxTime = HitBubble.getMaxTime();
        float viewYaw;
        float viewPitch;

        public Bubble(float viewYaw, float viewPitch, Vec3d pos) {
            this.viewYaw = viewYaw;
            this.viewPitch = viewPitch;
            this.pos = pos;
        }

        private float getDeltaTime() {
            return (float)(System.currentTimeMillis() - this.time) / this.maxTime;
        }
    }

    private class BubblePart {
        float startRPC;
        float endRPC;
        float randRotate;
        float rotateOffset;
        float extend;
        long time;
        float maxTime;

        public BubblePart(float startRPC, float endRPC, float maxTime) {
            this.randRotate = HitBubble.this.RAND.nextFloat(360.0f);
            this.rotateOffset = HitBubble.this.RAND.nextFloat(-180.0f, 0.0f);
            this.extend = HitBubble.this.RAND.nextFloat(0.5f, 1.5f);
            this.time = System.currentTimeMillis();
            this.startRPC = startRPC;
            this.endRPC = endRPC;
            this.maxTime = maxTime;
        }

        private float getDeltaTime() {
            return (float)(System.currentTimeMillis() - this.time) / this.maxTime;
        }

        private double getRotate(float deltaTime) {
            return Math.toRadians((double)(this.randRotate + this.rotateOffset * deltaTime) % 360.0);
        }

        private float getRPC(float deltaTime) {
            return (float)MathUtils.easeInCircle(MathUtils.lerp(this.startRPC, this.endRPC, deltaTime));
        }

        private float getExtend(float deltaTime) {
            return (float)MathUtils.easeInCircle(deltaTime) * this.extend;
        }
    }
}

