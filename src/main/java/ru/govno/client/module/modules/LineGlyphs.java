package ru.govno.client.module.modules;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import org.lwjgl.opengl.GL11;
import ru.govno.client.module.Module;
import ru.govno.client.module.modules.ClientColors;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.module.settings.ColorSettings;
import ru.govno.client.module.settings.FloatSettings;
import ru.govno.client.module.settings.ModeSettings;
import ru.govno.client.utils.Math.MathUtils;
import ru.govno.client.utils.Render.AnimationUtils;
import ru.govno.client.utils.Render.ColorUtils;

public class LineGlyphs
extends Module {
    public static LineGlyphs get;
    public FloatSettings GlyphsCount;
    public BoolSettings SlowSpeed;
    public ModeSettings ColorMode;
    public ColorSettings PickColor1;
    public ColorSettings PickColor2;
    private final Random RAND = new Random(93882L);
    private final List<Vec3d> temp3dVecs = new ArrayList<Vec3d>();
    private static final Tessellator tessellator;
    private final List<GliphsVecGen> GLIPHS_VEC_GENS = new ArrayList<GliphsVecGen>();

    public LineGlyphs() {
        super("LineGlyphs", 0, Module.Category.RENDER);
        get = this;
        this.GlyphsCount = new FloatSettings("GlyphsCount", 70.0f, 200.0f, 10.0f, this);
        this.settings.add(this.GlyphsCount);
        this.SlowSpeed = new BoolSettings("SlowSpeed", false, this);
        this.settings.add(this.SlowSpeed);
        this.ColorMode = new ModeSettings("ColorMode", "Client", this, new String[]{"Rainbow", "Client", "Picker", "DoublePicker"});
        this.settings.add(this.ColorMode);
        this.PickColor1 = new ColorSettings("PickColor1", ColorUtils.getColor(100, 255, 100), this, () -> this.ColorMode.currentMode.contains("Picker"));
        this.settings.add(this.PickColor1);
        this.PickColor2 = new ColorSettings("PickColor2", ColorUtils.getColor(60, 60, 255), this, () -> this.ColorMode.currentMode.endsWith("DoublePicker"));
        this.settings.add(this.PickColor2);
    }

    private static int stateColor(int index, float alphaPC) {
        int color = -1;
        if (get != null) {
            switch (LineGlyphs.get.ColorMode.getMode()) {
                case "Rainbow": {
                    color = ColorUtils.rainbowGui(0, index);
                    break;
                }
                case "Client": {
                    color = ClientColors.getColor1(index, 1.0f);
                    break;
                }
                case "Picker": {
                    color = LineGlyphs.get.PickColor1.getCol();
                    break;
                }
                case "DoublePicker": {
                    color = ColorUtils.fadeColor(LineGlyphs.get.PickColor1.getCol(), LineGlyphs.get.PickColor2.getCol(), 0.3f, (int)((float)index / 0.3f / 8.0f));
                }
            }
        }
        return ColorUtils.swapAlpha(color, (float)ColorUtils.getAlphaFromColor(color) * alphaPC);
    }

    private int[] lineMoveSteps() {
        return new int[]{0, 3};
    }

    private int[] lineStepsAmount() {
        return new int[]{7, 12};
    }

    private int[] spawnRanges() {
        return new int[]{6, 24, 0, 12};
    }

    private int maxObjCount() {
        return this.GlyphsCount.getInt();
    }

    private int minGliphsJointDst() {
        return 8;
    }

    private int getR360X() {
        return this.RAND.nextInt(0, 4) * 90;
    }

    private int getR360Y() {
        return this.RAND.nextInt(-2, 2) * 90;
    }

    private int[] getR360XY() {
        return new int[]{this.RAND.nextInt(0, 4) * 90, this.RAND.nextInt(-1, 1) * 90};
    }

    private int[] getA90R(int[] outdated) {
        int maxAttempt;
        int b;
        int a;
        int ao = a = outdated[0];
        int bo = b = outdated[1];
        for (maxAttempt = 150; maxAttempt > 0 && Math.abs(b - bo) != 90; --maxAttempt) {
            b = this.getR360Y();
        }
        for (maxAttempt = 5; maxAttempt > 0 && (Math.abs(a - ao) != 90 || Math.abs(a - ao) != 270); --maxAttempt) {
            a = this.getR360X();
        }
        return new int[]{a, b};
    }

    private Vec3i offsetFromRXYR(Vec3i vec3i, int[] rxy, int r) {
        double yawR = Math.toRadians(rxy[0]);
        double pitchR = Math.toRadians(rxy[1]);
        double r1 = r;
        int ry = (int)(Math.sin(pitchR) * r1);
        if (pitchR != 0.0) {
            r1 = 0.0;
        }
        int rx = (int)(-(Math.sin(yawR) * r1));
        int rz = (int)(Math.cos(yawR) * r1);
        int xi = vec3i.getX() + rx;
        int yi = vec3i.getY() + ry;
        int zi = vec3i.getZ() + rz;
        return new Vec3i(xi, yi, zi);
    }

    private float moveAdvanceFromTicks(int ticksSet, int ticksExpiring, float pTicks) {
        return Math.min(Math.max(1.0f - ((float)ticksExpiring - pTicks) / (float)ticksSet, 0.0f), 1.0f);
    }

    private List<Vec3d> getSmoothTickedFromList(List<Vec3i> vec3is, float moveAdvance) {
        if (!this.temp3dVecs.isEmpty()) {
            this.temp3dVecs.clear();
        }
        for (Vec3i vec3i : vec3is) {
            double x = vec3i.getX();
            double y = vec3i.getY();
            double z = vec3i.getZ();
            if (vec3is.size() >= 1 && vec3i == vec3is.get(vec3is.size() - 1)) {
                Vec3i prevVec3i = vec3is.get(vec3is.size() - 2);
                x = MathUtils.lerp((double)prevVec3i.getX(), x, (double)moveAdvance);
                y = MathUtils.lerp((double)prevVec3i.getY(), y, (double)moveAdvance);
                z = MathUtils.lerp((double)prevVec3i.getZ(), z, (double)moveAdvance);
            }
            this.temp3dVecs.add(new Vec3d(x, y, z));
        }
        return this.temp3dVecs;
    }

    private Vec3i randGliphSpawnPos() {
        int[] spawnRanges = this.spawnRanges();
        double dst = this.RAND.nextInt(spawnRanges[0], spawnRanges[1]);
        double fov = LineGlyphs.mc.gameSettings.fovSetting;
        double radianYaw = Math.toRadians(this.RAND.nextInt((int)((double)Minecraft.player.rotationYaw - fov * 0.75), (int)((double)Minecraft.player.rotationYaw + fov * 0.75)));
        int randXOff = (int)(-(Math.sin(radianYaw) * dst));
        int randYOff = this.RAND.nextInt(-spawnRanges[2], spawnRanges[3]);
        int randZOff = (int)(Math.cos(radianYaw) * dst);
        return new Vec3i(RenderManager.viewerPosX + (double)randXOff, RenderManager.viewerPosY + (double)randYOff, RenderManager.viewerPosZ + (double)randZOff);
    }

    private Vec3i genWhiteGliphPos(List<GliphsVecGen> gliphVecGens, Frustum frustum) {
        Vec3i tempVec = this.randGliphSpawnPos();
        if (gliphVecGens.size() >= 2) {
            Vec3i finalTempVec = tempVec;
            for (int maxAttempt = 1; maxAttempt > 0 && gliphVecGens.stream().filter(gliphVecGen -> gliphVecGen.vecGens.size() >= 2).anyMatch(gliphVecGen -> gliphVecGen.vecGens.get(0) != null && Math.sqrt(gliphVecGen.vecGens.get(0).distanceSq(finalTempVec)) <= (double)this.minGliphsJointDst()); --maxAttempt) {
                Vec3i vec3i = this.randGliphSpawnPos();
                if (frustum.isBoundingBoxInFrustum(LineGlyphs.aabbFromVec3d(new Vec3d(vec3i.getX(), vec3i.getY(), vec3i.getZ())))) continue;
                tempVec = this.randGliphSpawnPos();
            }
        }
        return tempVec;
    }

    private void addAllGliphs(int countCap, Frustum frustum) {
        for (int maxAttempt = 8; maxAttempt > 0 && this.GLIPHS_VEC_GENS.stream().filter(gliphsVecGen -> gliphsVecGen.alphaPC.to != 0.0f).count() < (long)countCap; --maxAttempt) {
            int[] lineStepsAmount = this.lineStepsAmount();
            while (this.GLIPHS_VEC_GENS.size() < countCap) {
                Vec3i pos = this.randGliphSpawnPos();
                this.GLIPHS_VEC_GENS.add(new GliphsVecGen(pos, this.RAND.nextInt(lineStepsAmount[0], lineStepsAmount[1])));
            }
        }
    }

    private void gliphsRemoveAuto(float moduleAlphaPC, Frustum frustum) {
        this.GLIPHS_VEC_GENS.removeIf(gliphsVecGen -> gliphsVecGen.isToRemove(moduleAlphaPC, frustum));
    }

    private void gliphsUpdate() {
        if (!this.GLIPHS_VEC_GENS.isEmpty()) {
            this.GLIPHS_VEC_GENS.forEach(GliphsVecGen::update);
        }
    }

    private void gliphsClear() {
        if (!this.GLIPHS_VEC_GENS.isEmpty()) {
            this.GLIPHS_VEC_GENS.clear();
        }
    }

    private static AxisAlignedBB aabbFromVec3d(Vec3d pos) {
        return new AxisAlignedBB(pos).expandXyz(0.1);
    }

    private void drawAllGliphs(float alphaPC, float pTicks, Frustum frustum) {
        if (this.GLIPHS_VEC_GENS.isEmpty()) {
            return;
        }
        List<GliphsVecGen> filteredGens = this.GLIPHS_VEC_GENS.stream().filter(gliphsVecGen -> alphaPC * gliphsVecGen.getAlphaPC() * 255.0f >= 1.0f).toList();
        if (filteredGens.isEmpty()) {
            return;
        }
        GliphVecRenderer.set3DRendering(true, () -> {
            int colorIndex = 0;
            for (GliphsVecGen filteredGen : filteredGens) {
                GliphVecRenderer.clientColoredBegin(filteredGen, ++colorIndex, 180, alphaPC * filteredGen.alphaPC.anim, pTicks, frustum);
            }
        });
    }

    @Override
    public void onToggled(boolean actived) {
        this.stateAnim.to = actived ? 1.0f : 0.0f;
        super.onToggled(actived);
    }

    @Override
    public void onUpdate() {
        this.gliphsUpdate();
        this.addAllGliphs(this.maxObjCount(), new Frustum(RenderManager.renderPosX, RenderManager.renderPosY, RenderManager.renderPosZ));
    }

    @Override
    public void alwaysRender3D(float partialTicks) {
        float alphaPC;
        if (this.actived) {
            this.stateAnim.to = 1.0f;
            alphaPC = this.stateAnim.getAnim();
        } else {
            if (this.stateAnim.anim < 0.03f && this.stateAnim.to == 0.0f) {
                this.gliphsClear();
                return;
            }
            this.stateAnim.to = 0.0f;
            alphaPC = this.stateAnim.getAnim();
        }
        Frustum frustum = new Frustum(RenderManager.renderPosX, RenderManager.renderPosY, RenderManager.renderPosZ);
        this.gliphsRemoveAuto(alphaPC, frustum);
        this.drawAllGliphs(alphaPC, partialTicks, frustum);
    }

    static {
        tessellator = Tessellator.getInstance();
    }

    private class GliphsVecGen {
        private final List<Vec3i> vecGens = new ArrayList<Vec3i>();
        private int currentStepTicks;
        private int lastStepSet;
        private int stepsAmount;
        private int[] lastYawPitch;
        private final AnimationUtils alphaPC = new AnimationUtils(0.1f, 1.0f, 0.075f);

        public GliphsVecGen(Vec3i spawnPos, int maxStepsAmount) {
            this.vecGens.add(spawnPos);
            this.lastYawPitch = LineGlyphs.this.getR360XY();
            this.stepsAmount = maxStepsAmount;
        }

        private void update() {
            if (this.stepsAmount == 0) {
                this.alphaPC.to = 0.0f;
            }
            if (this.currentStepTicks > 0) {
                this.currentStepTicks -= LineGlyphs.this.SlowSpeed.getBool() ? 1 : 2;
                if (this.currentStepTicks < 0) {
                    this.currentStepTicks = 0;
                }
                return;
            }
            this.lastYawPitch = LineGlyphs.this.getA90R(this.lastYawPitch);
            this.lastStepSet = this.currentStepTicks = LineGlyphs.this.RAND.nextInt(LineGlyphs.this.lineMoveSteps()[0], LineGlyphs.this.lineMoveSteps()[1]);
            this.vecGens.add(LineGlyphs.this.offsetFromRXYR(this.vecGens.get(this.vecGens.size() - 1), this.lastYawPitch, this.currentStepTicks));
            --this.stepsAmount;
        }

        public List<Vec3d> getPosVectors(float pTicks) {
            return LineGlyphs.this.getSmoothTickedFromList(this.vecGens, LineGlyphs.this.moveAdvanceFromTicks(this.lastStepSet, this.currentStepTicks, pTicks));
        }

        public float getAlphaPC() {
            return MathUtils.clamp(this.alphaPC.getAnim(), 0.0f, 1.0f);
        }

        public void setWantToRemove() {
            this.stepsAmount = 0;
        }

        public boolean isToRemove(float moduleAlphaPC, Frustum frustum) {
            return moduleAlphaPC * (this.alphaPC.to == 0.0f ? this.getAlphaPC() : 1.0f) * 255.0f < 1.0f;
        }
    }

    private class GliphVecRenderer {
        private GliphVecRenderer() {
        }

        private static void set3DRendering(boolean bloom, Runnable render) {
            GL11.glPushMatrix();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, bloom ? GlStateManager.DestFactor.ONE : GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GL11.glEnable((int)3042);
            GL11.glLineWidth((float)1.0f);
            GL11.glPointSize((float)1.0f);
            GL11.glEnable((int)2832);
            GL11.glDisable((int)3553);
            Module.mc.entityRenderer.disableLightmap();
            GL11.glDisable((int)2896);
            GL11.glShadeModel((int)7425);
            GL11.glAlphaFunc((int)516, (float)0.003921569f);
            GL11.glDisable((int)2884);
            GL11.glDepthMask((boolean)false);
            GL11.glEnable((int)2848);
            GL11.glHint((int)3154, (int)4354);
            GL11.glTranslated((double)(-RenderManager.viewerPosX), (double)(-RenderManager.viewerPosY), (double)(-RenderManager.viewerPosZ));
            render.run();
            GL11.glLineWidth((float)1.0f);
            GL11.glHint((int)3154, (int)4352);
            GL11.glDepthMask((boolean)true);
            GL11.glEnable((int)2884);
            GL11.glAlphaFunc((int)516, (float)0.1f);
            GL11.glLineWidth((float)1.0f);
            GL11.glPointSize((float)1.0f);
            GL11.glShadeModel((int)7424);
            GL11.glEnable((int)3553);
            GlStateManager.resetColor();
            if (bloom) {
                GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            }
            GL11.glPopMatrix();
        }

        private static float calcLineWidth(GliphsVecGen gliphVecGen) {
            Vec3d cameraPos = new Vec3d(RenderManager.renderPosX, RenderManager.renderPosY, RenderManager.renderPosZ);
            Vec3i pos = gliphVecGen.vecGens.stream().sorted(Comparator.comparingDouble(vec3i -> -vec3i.distanceTo(cameraPos))).findAny().orElse(new Vec3i(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE));
            double dst = cameraPos.getDistanceAtEyeByVec(Minecraft.player, pos.getX(), pos.getY(), pos.getZ());
            return 1.0E-4f + 3.0f * (float)MathUtils.clamp(1.0 - dst / 20.0, 0.0, 1.0);
        }

        private static void clientColoredBegin(GliphsVecGen gliphVecGen, int objIndex, int colorIndexStep, float alphaPC, float pTicks, Frustum frustum) {
            float aPC;
            if (alphaPC * 255.0f < 1.0f || gliphVecGen.vecGens.size() < 2) {
                return;
            }
            float lineWidth = GliphVecRenderer.calcLineWidth(gliphVecGen);
            GL11.glLineWidth((float)lineWidth);
            tessellator.getBuffer().begin(3, DefaultVertexFormats.POSITION_COLOR);
            int colorIndex = objIndex;
            int index = 0;
            for (Vec3d vec3d : gliphVecGen.getPosVectors(pTicks)) {
                aPC = alphaPC * (0.25f + (float)index / (float)gliphVecGen.vecGens.size() / 1.75f);
                tessellator.getBuffer().pos(vec3d).color(LineGlyphs.stateColor(colorIndex, aPC)).endVertex();
                colorIndex += colorIndexStep;
                ++index;
            }
            tessellator.draw();
            GL11.glPointSize((float)(lineWidth * 3.0f));
            tessellator.getBuffer().begin(0, DefaultVertexFormats.POSITION_COLOR);
            colorIndex = objIndex;
            index = 0;
            for (Vec3d vec3d : gliphVecGen.getPosVectors(pTicks)) {
                aPC = alphaPC * (0.25f + (float)index / (float)gliphVecGen.vecGens.size() / 1.75f);
                tessellator.getBuffer().pos(vec3d).color(LineGlyphs.stateColor(colorIndex, aPC)).endVertex();
                colorIndex += colorIndexStep;
                ++index;
            }
            tessellator.draw();
        }
    }
}

