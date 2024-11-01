package ru.govno.client.module.modules;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;
import ru.govno.client.event.EventTarget;
import ru.govno.client.event.events.Event3D;
import ru.govno.client.event.events.EventSendPacket;
import ru.govno.client.module.Module;
import ru.govno.client.module.modules.ClientColors;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.module.settings.FloatSettings;
import ru.govno.client.module.settings.ModeSettings;
import ru.govno.client.utils.Math.MathUtils;
import ru.govno.client.utils.Math.TimerHelper;
import ru.govno.client.utils.Render.AnimationUtils;
import ru.govno.client.utils.Render.ColorUtils;
import ru.govno.client.utils.Render.RenderUtils;

public class Particles
extends Module {
    List<Part> parts = new ArrayList<Part>();
    ModeSettings Mode;
    ModeSettings Particle;
    ModeSettings ParticleType;
    BoolSettings RandomizePos;
    FloatSettings RandomStrength;
    private static final ResourceLocation[] TEXTURES = new ResourceLocation[]{new ResourceLocation("vegaline/modules/particles/particle1.png"), new ResourceLocation("vegaline/modules/particles/particle2.png"), new ResourceLocation("vegaline/modules/particles/particle3.png")};
    private final Tessellator tessellator = Tessellator.getInstance();
    private final BufferBuilder buffer = this.tessellator.getBuffer();

    public Particles() {
        super("Particles", 0, Module.Category.RENDER);
        this.Mode = new ModeSettings("Mode", "Client", this, new String[]{"Client", "Minecraft"});
        this.settings.add(this.Mode);
        this.Particle = new ModeSettings("Particle", "Crit", this, new String[]{"Explode", "Largeexplode", "HugeExplosion", "FireworksSpark", "Bubble", "Splash", "Wake", "Suspended", "DepthSuspend", "Crit", "MagicCrit", "Smoke", "LargeSmoke", "Spell", "InstantSpell", "MobSpell", "MobSpellAmbient", "WitchMagic", "DripWater", "DripLava", "AngryVillager", "HappyVillager", "TownAura", "Note", "Portal", "EnchantmentTable", "Flame", "Lava", "Footstep", "Cloud", "Reddust", "SnowBallPoof", "SnowShovel", "Slime", "Heart", "Barrier", "IconCrack", "BlockCrack", "BlockDust", "Droplet", "Take", "MobAppearance", "DragonBreath", "EndRod", "DamageIndicator", "SweepAttack", "FallingDust", "Totem", "Spit"}, () -> this.Mode.currentMode.equalsIgnoreCase("Minecraft"));
        this.settings.add(this.Particle);
        this.ParticleType = new ModeSettings("ParticleType", "Dollar", this, new String[]{"Dollar", "Bitcoin", "Star"}, () -> this.Mode.currentMode.equalsIgnoreCase("Client"));
        this.settings.add(this.ParticleType);
        this.RandomizePos = new BoolSettings("RandomizePos", true, this, () -> this.Mode.getMode().equalsIgnoreCase("Minecraft"));
        this.settings.add(this.RandomizePos);
        this.RandomStrength = new FloatSettings("RandomStrength", 0.9f, 1.0f, 0.05f, this, () -> this.Mode.getMode().equalsIgnoreCase("Minecraft"));
        this.settings.add(this.RandomStrength);
        for (int i = 0; i < TEXTURES.length; ++i) {
            try {
                DynamicTexture dynamicTexture = new DynamicTexture(TextureUtil.readBufferedImage(Minecraft.getMinecraft().getResourceManager().getResource(TEXTURES[i]).getInputStream()));
                dynamicTexture.setBlurMipmap(true, false);
                Particles.TEXTURES[i] = Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation(dynamicTexture.toString(), dynamicTexture);
                continue;
            }
            catch (Exception e) {
                e.fillInStackTrace();
            }
        }
    }

    private ResourceLocation getCurrentTexture() {
        switch (this.ParticleType.getMode()) {
            case "Dollar": {
                return TEXTURES[0];
            }
            case "Bitcoin": {
                return TEXTURES[1];
            }
            case "Star": {
                return TEXTURES[2];
            }
        }
        return null;
    }

    @EventTarget
    public void onPacketSend(EventSendPacket event) {
        block7: {
            Entity entityIn;
            CPacketUseEntity packet;
            block8: {
                int maxTime;
                Packet packet2 = event.getPacket();
                if (!(packet2 instanceof CPacketUseEntity)) break block7;
                packet = (CPacketUseEntity)packet2;
                entityIn = null;
                if (packet.getAction() == CPacketUseEntity.Action.ATTACK && packet instanceof CPacketUseEntity) {
                    if (Particles.mc.world == null) {
                        return;
                    }
                    entityIn = packet.getEntityFromWorld(Particles.mc.world);
                }
                if (!Minecraft.player.isEntityAlive()) {
                    return;
                }
                if (!this.Mode.getMode().equalsIgnoreCase("Client")) break block8;
                if (!(entityIn instanceof EntityLivingBase)) break block7;
                boolean lowestAndRotates = this.ParticleType.getMode().equalsIgnoreCase("Dollar") || this.ParticleType.getMode().equalsIgnoreCase("Bitcoin");
                int n = maxTime = lowestAndRotates ? 4000 : 5000;
                if (entityIn == null && !entityIn.isEntityAlive() || entityIn instanceof EntityPlayerSP) {
                    return;
                }
                float w = entityIn.width / 2.0f;
                float h = entityIn.height;
                Vec3d vec = entityIn.getPositionVector().addVector((double)(-w) + (double)(w * 2.0f) * Math.random(), (double)h * Math.random(), (double)(-w) + (double)(w * 2.0f) * Math.random());
                for (int i = 0; i < ((double)Minecraft.player.getCooledAttackStrength(0.0f) < 0.7 ? 4 : 1); ++i) {
                    this.parts.add(new Part(vec, maxTime, lowestAndRotates));
                }
                break block7;
            }
            if (event.getPacket() instanceof CPacketUseEntity && packet.getAction() == CPacketUseEntity.Action.ATTACK && packet instanceof CPacketUseEntity) {
                for (int count = 0; count < 60; ++count) {
                    this.addParticlesFor(entityIn, 1.0f);
                }
            }
        }
    }

    int getById(String mode) {
        if (mode.equalsIgnoreCase("Explode")) {
            return 0;
        }
        if (mode.equalsIgnoreCase("Largeexplode")) {
            return 1;
        }
        if (mode.equalsIgnoreCase("HugeExplosion")) {
            return 2;
        }
        if (mode.equalsIgnoreCase("FireworksSpark")) {
            return 3;
        }
        if (mode.equalsIgnoreCase("Bubble")) {
            return 4;
        }
        if (mode.equalsIgnoreCase("Splash")) {
            return 5;
        }
        if (mode.equalsIgnoreCase("Wake")) {
            return 6;
        }
        if (mode.equalsIgnoreCase("Suspended")) {
            return 7;
        }
        if (mode.equalsIgnoreCase("DepthSuspend")) {
            return 8;
        }
        if (mode.equalsIgnoreCase("Crit")) {
            return 9;
        }
        if (mode.equalsIgnoreCase("MagicCrit")) {
            return 10;
        }
        if (mode.equalsIgnoreCase("Smoke")) {
            return 11;
        }
        if (mode.equalsIgnoreCase("LargeSmoke")) {
            return 12;
        }
        if (mode.equalsIgnoreCase("Spell")) {
            return 13;
        }
        if (mode.equalsIgnoreCase("InstantSpell")) {
            return 14;
        }
        if (mode.equalsIgnoreCase("MobSpell")) {
            return 15;
        }
        if (mode.equalsIgnoreCase("MobSpellAmbient")) {
            return 16;
        }
        if (mode.equalsIgnoreCase("WitchMagic")) {
            return 17;
        }
        if (mode.equalsIgnoreCase("DripWater")) {
            return 18;
        }
        if (mode.equalsIgnoreCase("DripLava")) {
            return 19;
        }
        if (mode.equalsIgnoreCase("AngryVillager")) {
            return 20;
        }
        if (mode.equalsIgnoreCase("HappyVillager")) {
            return 21;
        }
        if (mode.equalsIgnoreCase("TownAura")) {
            return 22;
        }
        if (mode.equalsIgnoreCase("Note")) {
            return 23;
        }
        if (mode.equalsIgnoreCase("Portal")) {
            return 24;
        }
        if (mode.equalsIgnoreCase("EnchantmentTable")) {
            return 25;
        }
        if (mode.equalsIgnoreCase("Flame")) {
            return 26;
        }
        if (mode.equalsIgnoreCase("Lava")) {
            return 27;
        }
        if (mode.equalsIgnoreCase("Footstep")) {
            return 28;
        }
        if (mode.equalsIgnoreCase("Cloud")) {
            return 29;
        }
        if (mode.equalsIgnoreCase("Reddust")) {
            return 30;
        }
        if (mode.equalsIgnoreCase("SnowBallPoof")) {
            return 31;
        }
        if (mode.equalsIgnoreCase("SnowShovel")) {
            return 32;
        }
        if (mode.equalsIgnoreCase("Slime")) {
            return 33;
        }
        if (mode.equalsIgnoreCase("Heart")) {
            return 34;
        }
        if (mode.equalsIgnoreCase("Barrier")) {
            return 35;
        }
        if (mode.equalsIgnoreCase("IconCrack")) {
            return 36;
        }
        if (mode.equalsIgnoreCase("BlockCrack")) {
            return 37;
        }
        if (mode.equalsIgnoreCase("BlockDust")) {
            return 38;
        }
        if (mode.equalsIgnoreCase("Droplet")) {
            return 39;
        }
        if (mode.equalsIgnoreCase("Take")) {
            return 40;
        }
        if (mode.equalsIgnoreCase("MobAppearance")) {
            return 41;
        }
        if (mode.equalsIgnoreCase("DragonBreath")) {
            return 42;
        }
        if (mode.equalsIgnoreCase("EndRod")) {
            return 43;
        }
        if (mode.equalsIgnoreCase("DamageIndicator")) {
            return 44;
        }
        if (mode.equalsIgnoreCase("SweepAttack")) {
            return 45;
        }
        if (mode.equalsIgnoreCase("FallingDust")) {
            return 46;
        }
        if (mode.equalsIgnoreCase("Totem")) {
            return 47;
        }
        if (mode.equalsIgnoreCase("Spit")) {
            return 48;
        }
        return -1;
    }

    void addParticlesFor(Entity entityIn, float amount) {
        if (entityIn == null) {
            return;
        }
        String mode = this.Particle.currentMode;
        double posX = entityIn.posX;
        double posY = entityIn.posY;
        double posZ = entityIn.posZ;
        double next = 0.0;
        double next2 = (float)MathUtils.getRandomInRange(-1, 1) * this.RandomStrength.getFloat();
        double next3 = (float)(-MathUtils.getRandomInRange(-1, 1)) * this.RandomStrength.getFloat();
        double next4 = (float)MathUtils.getRandomInRange(-1, 1) * -this.RandomStrength.getFloat();
        if (this.RandomizePos.getBool()) {
            next = 2.0f * this.RandomStrength.getFloat();
            double w = entityIn.width * this.RandomStrength.getFloat();
            double h = entityIn.getEyeHeight() * this.RandomStrength.getFloat();
            posX += MathUtils.getRandomInRange(-w / 2.0, w / 2.0) * (next * 4.0);
            posY += MathUtils.getRandomInRange(-h / 2.0, h / 2.0) * (next * 2.0);
            posZ += MathUtils.getRandomInRange(-w / 2.0, w / 2.0) * (next * 4.0);
        }
        EnumParticleTypes particle2 = null;
        if (this.getById(mode) != -1) {
            particle2 = EnumParticleTypes.getParticleFromId(this.getById(mode));
        }
        if (particle2 != null) {
            int oldSetting = Particles.mc.gameSettings.particleSetting;
            boolean oldSetting2 = Particles.mc.gameSettings.ofFireworkParticles;
            boolean oldSetting3 = Particles.mc.gameSettings.ofPortalParticles;
            boolean oldSetting4 = Particles.mc.gameSettings.ofPotionParticles;
            boolean oldSetting5 = Particles.mc.gameSettings.ofVoidParticles;
            boolean oldSetting6 = Particles.mc.gameSettings.ofWaterParticles;
            Particles.mc.gameSettings.particleSetting = 1;
            Particles.mc.gameSettings.ofFireworkParticles = true;
            Particles.mc.gameSettings.ofPortalParticles = true;
            Particles.mc.gameSettings.ofPotionParticles = true;
            Particles.mc.gameSettings.ofVoidParticles = true;
            Particles.mc.gameSettings.ofWaterParticles = true;
            Particles.mc.world.spawnParticle(particle2, posX, posY, posZ, next2, next3, next4, 1);
            Particles.mc.gameSettings.particleSetting = oldSetting;
            Particles.mc.gameSettings.ofFireworkParticles = oldSetting2;
            Particles.mc.gameSettings.ofPortalParticles = oldSetting3;
            Particles.mc.gameSettings.ofPotionParticles = oldSetting4;
            Particles.mc.gameSettings.ofVoidParticles = oldSetting5;
            Particles.mc.gameSettings.ofWaterParticles = oldSetting6;
        }
    }

    @EventTarget
    public void onRender3D(Event3D event) {
        if (this.Mode.currentMode.equalsIgnoreCase("Client") && !this.parts.isEmpty()) {
            boolean colorize = this.ParticleType.getMode().equalsIgnoreCase("Star");
            this.setupRenderParts(() -> {
                float i = 0.0f;
                if (this.parts.isEmpty()) {
                    return;
                }
                mc.getTextureManager().bindTexture(this.getCurrentTexture());
                for (Part part : this.parts) {
                    if (part == null || part.toRemove) continue;
                    float gradPC = i / (float)this.parts.size();
                    int c1 = colorize ? ClientColors.getColor1((int)(i * 5.0f)) : ColorUtils.getColor(160);
                    int c2 = colorize ? ClientColors.getColor2((int)(i * 5.0f)) : ColorUtils.getColor(160);
                    int col = ColorUtils.getOverallColorFrom(c1, c2, MathUtils.clamp(gradPC, 0.0f, 1.0f));
                    part.vertexColored(col);
                    i += 1.0f;
                }
            }, colorize);
        }
    }

    boolean setupRenderParts(Runnable render, boolean bloom) {
        Vec3d renderPos = new Vec3d(RenderManager.viewerPosX, RenderManager.viewerPosY, RenderManager.viewerPosZ);
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.depthMask(false);
        GlStateManager.disableCull();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, bloom ? GlStateManager.DestFactor.ONE : GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.translate(-renderPos.xCoord, -renderPos.yCoord, -renderPos.zCoord);
        GlStateManager.shadeModel(7425);
        Particles.mc.entityRenderer.disableLightmap();
        render.run();
        GlStateManager.translate(renderPos.xCoord, renderPos.yCoord, renderPos.zCoord);
        GlStateManager.shadeModel(7424);
        if (bloom) {
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        }
        GlStateManager.enableCull();
        GlStateManager.depthMask(true);
        GlStateManager.resetColor();
        GlStateManager.popMatrix();
        return true;
    }

    @Override
    public void onUpdate() {
        if (this.Mode.currentMode.equalsIgnoreCase("Client") && !this.parts.isEmpty()) {
            for (int i = 0; i < this.parts.size(); ++i) {
                Part part = this.parts.get(i);
                if (part == null) continue;
                if (part.toRemove) {
                    this.parts.remove(i);
                    continue;
                }
                part.updatePart();
            }
        } else if (!this.parts.isEmpty()) {
            this.parts.clear();
        }
    }

    class Part {
        AnimationUtils alphaPC = new AnimationUtils(0.1f, 1.0f, 0.035f);
        boolean toRemove = false;
        float[] randomXYZM = new float[]{(float)MathUtils.getRandomInRange(0.3, -0.3), (float)MathUtils.getRandomInRange(0.12, -0.09), (float)MathUtils.getRandomInRange(0.3, -0.3)};
        AnimationUtils posX;
        AnimationUtils posY;
        AnimationUtils posZ;
        float motionX = this.randomXYZM[0];
        float motionY = this.randomXYZM[1];
        float motionZ = this.randomXYZM[2];
        TimerHelper time = new TimerHelper();
        long maxTime;
        boolean lower;
        float[] randomYPR;

        Part(Vec3d vec, long maxTime, boolean lower) {
            this.posX = new AnimationUtils((float)vec.xCoord, (float)vec.xCoord, 0.08f);
            this.posY = new AnimationUtils((float)vec.yCoord, (float)vec.yCoord, 0.08f);
            this.posZ = new AnimationUtils((float)vec.zCoord, (float)vec.zCoord, 0.08f);
            this.maxTime = maxTime;
            this.time.reset();
            this.lower = lower;
            if (this.lower) {
                this.randomYPR = new float[8];
                this.randomYPR[0] = -360.0f + 720.0f * (float)Math.random();
                this.randomYPR[1] = -180.0f + 360.0f * (float)Math.random();
                this.randomYPR[2] = this.randomYPR[0] / 10.0f;
                this.randomYPR[3] = this.randomYPR[1] / 10.0f;
                this.randomYPR[4] = this.randomYPR[0];
                this.randomYPR[5] = this.randomYPR[1];
            }
        }

        long getTime() {
            return this.time.getTime();
        }

        float getSpeed() {
            return (float)Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
        }

        void gravityAndMove() {
            boolean gravityY;
            if (this.lower) {
                this.randomYPR[6] = this.randomYPR[4];
                this.randomYPR[7] = this.randomYPR[5];
                this.randomYPR[4] = this.randomYPR[4] + this.randomYPR[2];
                this.randomYPR[5] = this.randomYPR[5] + this.randomYPR[3];
                this.randomYPR[2] = this.randomYPR[2] * 0.96f;
                this.randomYPR[3] = this.randomYPR[3] * 0.96f;
            }
            this.posX.to = this.posX.getAnim() + this.motionX;
            this.posY.to = this.posY.getAnim() + this.motionY;
            this.posZ.to = this.posZ.getAnim() + this.motionZ;
            float x = this.posX.anim;
            float y = this.posY.anim;
            float z = this.posZ.anim;
            float motionX = this.motionX;
            float motionY = this.motionY;
            float motionZ = this.motionZ;
            BlockPos xPrePos = new BlockPos(x + motionX * 2.0f, (double)(y - motionY) + 0.1, z);
            BlockPos yPrePos = new BlockPos(x, y + motionY * 2.0f, z);
            BlockPos zPrePos = new BlockPos(x, (double)(y - motionY) + 0.1, z + motionZ * 2.0f);
            IBlockState xState = Module.mc.world.getBlockState(xPrePos);
            IBlockState yState = Module.mc.world.getBlockState(yPrePos);
            IBlockState zState = Module.mc.world.getBlockState(zPrePos);
            boolean collideX = xState.getCollisionBoundingBox(Module.mc.world, xPrePos) != null;
            boolean collideY = yState.getCollisionBoundingBox(Module.mc.world, yPrePos) != null;
            boolean collideZ = zState.getCollisionBoundingBox(Module.mc.world, zPrePos) != null;
            boolean isInLiquid = xState.getBlock() instanceof BlockLiquid || zState.getBlock() instanceof BlockLiquid || yState.getBlock() instanceof BlockLiquid;
            boolean bl = gravityY = motionY != 0.0f;
            if (gravityY) {
                this.motionY -= this.lower ? 0.003333f : 0.01f;
            }
            if (isInLiquid) {
                this.motionX *= 0.975f;
                this.motionY *= 0.75f;
                this.motionY -= 0.025f;
                this.motionZ *= 0.975f;
            }
            if (collideY) {
                this.motionY = -this.motionY * (this.lower ? 0.7f : 0.9f);
            }
            if (collideX) {
                this.motionX *= this.lower ? -0.6f : -1.0f;
            }
            if (collideZ) {
                this.motionZ *= this.lower ? -0.6f : -1.0f;
            }
        }

        void updatePart() {
            if (this.getTime() > this.maxTime) {
                this.alphaPC.to = 0.0f;
            }
            if ((double)this.alphaPC.getAnim() < 0.005) {
                this.toRemove = true;
            }
            if (this.toRemove) {
                return;
            }
            this.gravityAndMove();
        }

        void vertexColored(int color) {
            float alphaPC = ColorUtils.getGLAlphaFromColor(color) * this.alphaPC.getAnim();
            color = ColorUtils.swapAlpha(color, alphaPC * 255.0f);
            GlStateManager.pushMatrix();
            GL11.glTranslated((double)this.posX.getAnim(), (double)((double)this.posY.getAnim() + 0.25), (double)this.posZ.getAnim());
            if (this.lower) {
                float pTicks = Module.mc.getRenderPartialTicks();
                GL11.glRotatef((float)MathUtils.lerp(this.randomYPR[6], this.randomYPR[4], pTicks), (float)0.0f, (float)-1.0f, (float)0.0f);
                GL11.glRotatef((float)MathUtils.lerp(this.randomYPR[7], this.randomYPR[5], pTicks), (float)1.0f, (float)0.0f, (float)0.0f);
                if (!Particles.this.ParticleType.getMode().equalsIgnoreCase("Bitcoin")) {
                    GL11.glScaled((double)2.0, (double)2.0, (double)2.0);
                }
            } else {
                float fixed = Module.mc.gameSettings.thirdPersonView == 2 ? -1.0f : 1.0f;
                GL11.glRotatef((float)(-Module.mc.getRenderManager().playerViewY), (float)0.0f, (float)1.0f, (float)0.0f);
                GL11.glRotatef((float)Module.mc.getRenderManager().playerViewX, (float)fixed, (float)0.0f, (float)0.0f);
            }
            GL11.glScaled((double)-0.2, (double)-0.2, (double)-0.2);
            Particles.this.buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            Particles.this.buffer.pos(0.0, 0.0, 0.0).tex(0.0, 0.0).color(color).endVertex();
            Particles.this.buffer.pos(0.0, 1.0, 0.0).tex(0.0, 1.0).color(color).endVertex();
            Particles.this.buffer.pos(1.0, 1.0, 0.0).tex(1.0, 1.0).color(color).endVertex();
            Particles.this.buffer.pos(1.0, 0.0, 0.0).tex(1.0, 0.0).color(color).endVertex();
            RenderUtils.customRotatedObject2D(0.0f, 0.0f, 1.0f, 1.0f, (float)this.getTime() / (float)this.maxTime * 1200.0f * (float)(this.randomXYZM[0] > 0.0f ? 1 : -1));
            Particles.this.tessellator.draw();
            GlStateManager.popMatrix();
        }
    }
}

