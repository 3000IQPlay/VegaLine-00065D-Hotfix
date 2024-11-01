package ru.govno.client.module.modules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;
import ru.govno.client.event.EventTarget;
import ru.govno.client.event.events.Event3D;
import ru.govno.client.module.Module;
import ru.govno.client.module.modules.ClientColors;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.module.settings.ColorSettings;
import ru.govno.client.module.settings.FloatSettings;
import ru.govno.client.module.settings.ModeSettings;
import ru.govno.client.utils.Math.MathUtils;
import ru.govno.client.utils.Movement.MoveMeHelp;
import ru.govno.client.utils.Render.ColorUtils;
import ru.govno.client.utils.Render.RenderUtils;

public class JumpCircles
extends Module {
    public static JumpCircles get;
    List<List<ResourceLocation>> animatedGroups = Arrays.asList(new ArrayList(), new ArrayList());
    FloatSettings MaxTime;
    FloatSettings Range;
    ModeSettings Texture;
    ModeSettings ColorMode;
    ColorSettings PickColor1;
    ColorSettings PickColor2;
    BoolSettings DeepestLight;
    private final String staticLoc = "vegaline/modules/jumpcircles/default/";
    private final String animatedLoc = "vegaline/modules/jumpcircles/animated/";
    private final ResourceLocation JUMP_CIRCLE = new ResourceLocation("vegaline/modules/jumpcircles/default/circle.png");
    private final ResourceLocation JUMP_KONCHAL = new ResourceLocation("vegaline/modules/jumpcircles/default/konchal.png");
    private static final List<JumpRenderer> circles;
    private final Tessellator tessellator = Tessellator.getInstance();
    private final BufferBuilder buffer = this.tessellator.getBuffer();

    private ResourceLocation jumpTexture(int index, float progress) {
        String tex = this.Texture.currentMode;
        if (tex.equalsIgnoreCase("CubicalPieces") || tex.equalsIgnoreCase("Leeches")) {
            List<ResourceLocation> currentGroupTextures = tex.equalsIgnoreCase("CubicalPieces") ? this.animatedGroups.get(0) : this.animatedGroups.get(1);
            boolean animateByProgress = tex.equalsIgnoreCase("Leeches");
            if (tex.equalsIgnoreCase("Leeches")) {
                progress += 0.6f;
            }
            float frameOffset01 = progress % 1.0f;
            if (!animateByProgress) {
                int ms = 1500;
                frameOffset01 = (float)((System.currentTimeMillis() + (long)index) % 1500L) / 1500.0f;
            }
            return currentGroupTextures.get((int)Math.min(frameOffset01 * ((float)currentGroupTextures.size() - 0.5f), (float)currentGroupTextures.size()));
        }
        return tex.equalsIgnoreCase("Circle") ? this.JUMP_CIRCLE : this.JUMP_KONCHAL;
    }

    public JumpCircles() {
        super("JumpCircles", 0, Module.Category.RENDER);
        boolean anotherSys;
        get = this;
        int[] groupsFramesLength = new int[]{100, 200};
        String[] groupsFramesFormat = new String[]{"jpeg", "png"};
        boolean bl = anotherSys = !System.getProperty("os.name").startsWith("Win");
        for (int groupIndex = groupsFramesLength.length - 1; groupIndex >= 0; --groupIndex) {
            int framesCounter = 0;
            while (framesCounter < groupsFramesLength[groupIndex]) {
                ResourceLocation loc;
                ++framesCounter;
                if (anotherSys) {
                    loc = new ResourceLocation("vegaline/modules/jumpcircles/animated/animation" + (groupIndex + 1) + "/circleframe_" + framesCounter + "." + groupsFramesFormat[groupIndex]);
                } else {
                    loc = new ResourceLocation("vegaline/modules/jumpcircles/animated/animation" + (groupIndex + 1) + "/circleframe_" + framesCounter + "." + groupsFramesFormat[groupIndex]);
                    mc.getTextureManager().bindTexture(loc);
                }
                this.animatedGroups.get(groupIndex).add(loc);
            }
        }
        this.MaxTime = new FloatSettings("MaxTime", 3500.0f, 8000.0f, 2000.0f, this);
        this.settings.add(this.MaxTime);
        this.Range = new FloatSettings("Range", 2.0f, 3.0f, 1.0f, this);
        this.settings.add(this.Range);
        this.Texture = new ModeSettings("Texture", "Circle", this, new String[]{"Circle", "KonchalEbal", "CubicalPieces", "Leeches"});
        this.settings.add(this.Texture);
        this.ColorMode = new ModeSettings("ColorMode", "Rainbow", this, new String[]{"Client", "Rainbow", "Picker", "PickerFade"});
        this.settings.add(this.ColorMode);
        this.PickColor1 = new ColorSettings("PickColor1", ColorUtils.getColor(255, 80, 0), this, () -> this.ColorMode.currentMode.contains("Picker"));
        this.settings.add(this.PickColor1);
        this.PickColor2 = new ColorSettings("PickColor2", ColorUtils.getColor(255, 142, 0), this, () -> this.ColorMode.currentMode.endsWith("Fade"));
        this.settings.add(this.PickColor2);
        this.DeepestLight = new BoolSettings("DeepestLight", true, this);
        this.settings.add(this.DeepestLight);
    }

    public static void onEntityMove(Entity entityIn, Vec3d prev) {
        EntityPlayerSP base;
        if (entityIn instanceof EntityPlayerSP && (base = (EntityPlayerSP)entityIn).isEntityAlive()) {
            double motionY = entityIn.posY - prev.yCoord;
            double[] motions = new double[]{0.42f, 0.20000004768365898};
            if (MoveMeHelp.isBlockAboveHead(entityIn)) {
                motions = new double[]{0.42f, 0.20000004768365898, 0.20000004768371582, 0.07840000152587834, 0.01250004768371582};
            }
            boolean spawn = false;
            double[] dArray = motions;
            int n = dArray.length;
            for (int i = 0; i < n; ++i) {
                Double cur = dArray[i];
                if (!(MathUtils.getDifferenceOf(motionY, cur) < 0.001)) continue;
                spawn = true;
                break;
            }
            if (entityIn.onGround != entityIn.rayGround && motionY > 0.0 || spawn) {
                JumpCircles.addCircleForEntity(entityIn);
            }
            entityIn.rayGround = entityIn.onGround;
        }
    }

    private static void addCircleForEntity(Entity entity) {
        Vec3d vec = JumpCircles.getVec3dFromEntity(entity).addVector(0.0, 0.001, 0.0);
        BlockPos pos = new BlockPos(vec);
        IBlockState state = JumpCircles.mc.world.getBlockState(pos);
        if (state.getBlock() == Blocks.SNOW_LAYER || state.getBlock() == Blocks.SOUL_SAND) {
            vec = vec.addVector(0.0, 0.14, 0.0);
        } else {
            for (EnumFacing facing : EnumFacing.values()) {
                if (!facing.getAxis().isHorizontal() || (state = JumpCircles.mc.world.getBlockState(pos.add(facing.getFrontOffsetX(), 0, facing.getFrontOffsetZ()))).getBlock() != Blocks.SNOW_LAYER && state.getBlock() != Blocks.SOUL_SAND) continue;
                vec = vec.addVector(0.0, 0.14, 0.0);
                break;
            }
        }
        circles.add(new JumpRenderer(vec, circles.size()));
    }

    @EventTarget
    public void onRender3d(Event3D event) {
        if (circles.size() == 0) {
            return;
        }
        circles.removeIf(circle -> (double)circle.getDeltaTime() >= 1.0);
        if (circles.isEmpty()) {
            return;
        }
        boolean preBindTex = this.Texture.currentMode.equalsIgnoreCase("CubicalPieces");
        float deepestLightAnim = this.DeepestLight.getAnimation();
        float immersiveStrengh = 0.0f;
        if (deepestLightAnim >= 0.003921569f) {
            switch (this.Texture.currentMode) {
                case "Circle": {
                    immersiveStrengh = 0.05f;
                    break;
                }
                case "KonchalEbal": {
                    immersiveStrengh = 0.04f;
                    break;
                }
                case "CubicalPieces": {
                    immersiveStrengh = 0.08f;
                    break;
                }
                case "Leeches": {
                    immersiveStrengh = 0.15f;
                }
            }
        }
        float finalImmersiveStrengh = immersiveStrengh;
        this.setupDraw(() -> circles.forEach(circle -> this.doCircle(circle.pos, this.Range.getFloat(), 1.0f - circle.getDeltaTime(), circle.getIndex() * 30, !preBindTex, deepestLightAnim, finalImmersiveStrengh)), preBindTex);
    }

    private int getColor(int index, float alphaPC) {
        String colorMode = this.ColorMode.currentMode;
        int color = 0;
        switch (colorMode) {
            case "Client": {
                color = ClientColors.getColor1(index, alphaPC);
                break;
            }
            case "Rainbow": {
                color = ColorUtils.swapAlpha(ColorUtils.rainbowGui(0, index), 255.0f * alphaPC);
                break;
            }
            case "Picker": {
                color = ColorUtils.swapAlpha(this.PickColor1.color, (float)ColorUtils.getAlphaFromColor(this.PickColor1.color) * alphaPC);
                break;
            }
            case "PickerFade": {
                color = ColorUtils.fadeColor(ColorUtils.swapAlpha(this.PickColor1.color, (float)ColorUtils.getAlphaFromColor(this.PickColor1.color) * alphaPC), ColorUtils.swapAlpha(this.PickColor2.color, (float)ColorUtils.getAlphaFromColor(this.PickColor2.color) * alphaPC), 0.3f, (int)((float)index / 0.3f / 8.0f));
            }
        }
        color = ColorUtils.getOverallColorFrom(color, ColorUtils.swapAlpha(-1, ColorUtils.getAlphaFromColor(color)), 0.125f);
        return color;
    }

    private void doCircle(Vec3d pos, double maxRadius, float deltaTime, int index, boolean doBindTex, float immersiveShift, float immersiveIntense) {
        boolean immersive = immersiveShift >= 0.003921569F;
        float waveDelta = MathUtils.valWave01(1.0F - deltaTime);
        float alphaPC = (float)MathUtils.easeOutCirc((double)MathUtils.valWave01(1.0F - deltaTime));
        if (deltaTime < 0.5F) {
            alphaPC *= (float)MathUtils.easeInOutExpo((double)alphaPC);
        }

        float radius = (float)(
                (deltaTime > 0.5F ? MathUtils.easeOutElastic((double)(waveDelta * waveDelta)) : MathUtils.easeOutBack((double)waveDelta)) * maxRadius
        );
        double rotate = MathUtils.easeInOutElastic((double)waveDelta) * 90.0 / (1.0 + (double)waveDelta);
        if (doBindTex) {
            mc.getTextureManager().bindTexture(this.jumpTexture(index, deltaTime));
        }

        this.buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        this.buffer.pos(0.0, 0.0).tex(0.0, 0.0).color(this.getColor(index, alphaPC)).endVertex();
        this.buffer.pos(0.0, (double)radius).tex(0.0, 1.0).color(this.getColor((int)(324.0F + (float)index), alphaPC)).endVertex();
        this.buffer.pos((double)radius, (double)radius).tex(1.0, 1.0).color(this.getColor((int)(648.0F + (float)index), alphaPC)).endVertex();
        this.buffer.pos((double)radius, 0.0).tex(1.0, 0.0).color(this.getColor((int)(972.0F + (float)index), alphaPC)).endVertex();
        GL11.glPushMatrix();
        GL11.glTranslated(pos.xCoord - (double)radius / 2.0, pos.yCoord, pos.zCoord - (double)radius / 2.0);
        GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
        RenderUtils.customRotatedObject2D(0.0F, 0.0F, radius, radius, rotate);
        this.tessellator.draw();
        GL11.glPopMatrix();
        if (immersive) {
            int[] colors = new int[]{
                    this.getColor(index, 1.0F),
                    this.getColor((int)(324.0F + (float)index), 1.0F),
                    this.getColor((int)(648.0F + (float)index), 1.0F),
                    this.getColor((int)(972.0F + (float)index), 1.0F)
            };
            float minAPC = immersiveIntense * immersiveShift;
            float maxAPC = (float)MathUtils.easeInOutQuad((double)alphaPC);
            float polygons = 40.0F * maxAPC;
            float extMaxY = radius * maxAPC / 4.0F;
            float extMaxXZ = radius / 12.0F;
            this.buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);

            for (int i = 1; i < (int)polygons; i++) {
                float iPC = (float)i / polygons;
                float extY = extMaxY * (float)i / polygons - extMaxY / polygons;
                float aPC;
                if (!((aPC = MathUtils.lerp(maxAPC * minAPC, 0.0F, iPC * ((polygons - 1.0F) / polygons))) * 255.0F < 1.0F)) {
                    float radiusPost = radius + (float)MathUtils.easeOutCirc((double)MathUtils.valWave01(iPC - 1.5F / polygons)) * extMaxXZ;
                    this.buffer
                            .pos((double)(-radiusPost / 2.0F), (double)extY, (double)(-radiusPost / 2.0F))
                            .tex(0.0, 0.0)
                            .color(ColorUtils.toDark(colors[0], aPC))
                            .endVertex();
                    this.buffer
                            .pos((double)(-radiusPost / 2.0F), (double)extY, (double)(radiusPost / 2.0F))
                            .tex(0.0, 1.0)
                            .color(ColorUtils.toDark(colors[1], aPC))
                            .endVertex();
                    this.buffer
                            .pos((double)(radiusPost / 2.0F), (double)extY, (double)(radiusPost / 2.0F))
                            .tex(1.0, 1.0)
                            .color(ColorUtils.toDark(colors[2], aPC))
                            .endVertex();
                    this.buffer
                            .pos((double)(radiusPost / 2.0F), (double)extY, (double)(-radiusPost / 2.0F))
                            .tex(1.0, 0.0)
                            .color(ColorUtils.toDark(colors[3], aPC))
                            .endVertex();
                }
            }

            GL11.glPushMatrix();
            GL11.glTranslated(pos.xCoord, pos.yCoord, pos.zCoord);
            GL11.glRotated(rotate, 0.0, -1.0, 0.0);
            this.tessellator.draw();
            GL11.glPopMatrix();
        }
    }

    @Override
    public void onToggled(boolean actived) {
        circles.clear();
        super.onToggled(actived);
    }

    private static Vec3d getVec3dFromEntity(Entity entityIn) {
        float PT = mc.getRenderPartialTicks();
        double dx = entityIn.posX - entityIn.lastTickPosX;
        double dy = entityIn.posY - entityIn.lastTickPosY;
        double dz = entityIn.posZ - entityIn.lastTickPosZ;
        return new Vec3d(entityIn.lastTickPosX + dx * (double)PT + dx * 2.0, entityIn.lastTickPosY + dy * (double)PT, entityIn.lastTickPosZ + dz * (double)PT + dz * 2.0);
    }

    private void setupDraw(Runnable render, boolean preBindTex) {
        EntityRenderer renderer = JumpCircles.mc.entityRenderer;
        Vec3d revert = new Vec3d(RenderManager.viewerPosX, RenderManager.viewerPosY, RenderManager.viewerPosZ);
        boolean light = GL11.glIsEnabled((int)2896);
        boolean doShade = this.ColorMode.currentMode.equalsIgnoreCase("Picker") || this.getColor(0, 1.0f) != this.getColor(90, 1.0f);
        GL11.glPushMatrix();
        GL11.glEnable((int)3042);
        GL11.glEnable((int)3008);
        GL11.glAlphaFunc((int)516, (float)0.0f);
        GL11.glDepthMask((boolean)false);
        GL11.glDisable((int)2884);
        if (light) {
            GL11.glDisable((int)2896);
        }
        GL11.glShadeModel((int)(doShade ? 7425 : 7424));
        GL11.glBlendFunc((int)770, (int)32772);
        renderer.disableLightmap();
        GL11.glTranslated((double)(-revert.xCoord), (double)(-revert.yCoord), (double)(-revert.zCoord));
        if (preBindTex) {
            mc.getTextureManager().bindTexture(this.jumpTexture(0, 0.0f));
        }
        GL11.glTexParameteri((int)3553, (int)10240, (int)9728);
        render.run();
        GL11.glTexParameteri((int)3553, (int)10240, (int)9729);
        GL11.glTranslated((double)revert.xCoord, (double)revert.yCoord, (double)revert.zCoord);
        GL11.glBlendFunc((int)770, (int)771);
        GL11.glColor3f((float)1.0f, (float)1.0f, (float)1.0f);
        GL11.glShadeModel((int)7424);
        if (light) {
            GL11.glEnable((int)2896);
        }
        GL11.glEnable((int)2884);
        GL11.glDepthMask((boolean)true);
        GL11.glAlphaFunc((int)516, (float)0.1f);
        GL11.glEnable((int)3008);
        GL11.glPopMatrix();
    }

    static {
        circles = new ArrayList<JumpRenderer>();
    }

    private static final class JumpRenderer {
        private final long time = System.currentTimeMillis();
        private final Vec3d pos;
        int index;

        private JumpRenderer(Vec3d pos, int index) {
            this.pos = pos;
            this.index = index;
        }

        private float getDeltaTime() {
            return (float)(System.currentTimeMillis() - this.time) / JumpCircles.get.MaxTime.getFloat();
        }

        private int getIndex() {
            return this.index;
        }
    }
}

