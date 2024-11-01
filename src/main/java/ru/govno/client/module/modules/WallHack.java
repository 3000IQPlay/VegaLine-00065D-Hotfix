package ru.govno.client.module.modules;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.model.ModelEnderCrystal;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityMinecartContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import org.lwjgl.opengl.GL11;
import ru.govno.client.Client;
import ru.govno.client.module.Module;
import ru.govno.client.module.modules.ClientColors;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.module.settings.ColorSettings;
import ru.govno.client.module.settings.FloatSettings;
import ru.govno.client.module.settings.ModeSettings;
import ru.govno.client.utils.Math.MathUtils;
import ru.govno.client.utils.Render.ColorUtils;
import ru.govno.client.utils.Render.RenderUtils;

public class WallHack
extends Module {
    public static WallHack get;
    BoolSettings Players;
    BoolSettings Friends;
    BoolSettings Crystals;
    BoolSettings Mobs;
    BoolSettings Tiles;
    ModeSettings ColorMode;
    FloatSettings Opacity;
    ColorSettings PickColor;

    public WallHack() {
        super("WallHack", 0, Module.Category.RENDER);
        get = this;
        this.Players = new BoolSettings("Players", true, this);
        this.settings.add(this.Players);
        this.Friends = new BoolSettings("Friends", true, this);
        this.settings.add(this.Friends);
        this.Mobs = new BoolSettings("Mobs", true, this);
        this.settings.add(this.Mobs);
        this.Crystals = new BoolSettings("Crystals", true, this);
        this.settings.add(this.Crystals);
        this.ColorMode = new ModeSettings("CrysColorMode", "Client", this, new String[]{"Client", "Picker"}, () -> this.Crystals.getBool());
        this.settings.add(this.ColorMode);
        this.Opacity = new FloatSettings("CrysOpacity", 0.7f, 1.0f, 0.05f, this, () -> this.Crystals.getBool() && this.ColorMode.currentMode.equalsIgnoreCase("Client"));
        this.settings.add(this.Opacity);
        this.PickColor = new ColorSettings("CrysPickColor", ColorUtils.getColor(110, 160, 255, 225), this, () -> this.Crystals.getBool() && this.ColorMode.currentMode.equalsIgnoreCase("Picker"));
        this.settings.add(this.PickColor);
        this.Tiles = new BoolSettings("Tiles", true, this);
        this.settings.add(this.Tiles);
    }

    @Override
    public void onUpdate() {
        if (!(this.Players.getBool() || this.Friends.getBool() || this.Crystals.getBool() || this.Mobs.getBool())) {
            this.toggle(false);
            Client.msg("\u00a7f\u00a7lModules:\u00a7r \u00a77[\u00a7l" + this.getName() + "\u00a7r\u00a77]: \u00a77\u0432\u043a\u043b\u044e\u0447\u0438\u0442\u0435 \u0447\u0442\u043e-\u043d\u0438\u0431\u0443\u0434\u044c \u0432 \u043d\u0430\u0441\u0442\u0440\u043e\u0439\u043a\u0430\u0445.", false);
        }
    }

    private boolean[] getEtypes() {
        return new boolean[]{this.Players.getBool(), this.Friends.getBool(), this.Crystals.getBool(), this.Mobs.getBool(), this.Tiles.getBool()};
    }

    private boolean isCurrent(boolean[] entTypes, Entity entity) {
        EntityOtherPlayerMP mp;
        EntityLivingBase base;
        if (entity == null) {
            return false;
        }
        if (entity instanceof EntityLivingBase && ((base = (EntityLivingBase)entity) instanceof EntityOtherPlayerMP && entTypes[Client.friendManager.isFriend((mp = (EntityOtherPlayerMP)base).getName()) ? 1 : 0] || base instanceof EntityLivingBase && !(base instanceof EntityPlayer) && entTypes[3])) {
            return base.isEntityAlive() && !Minecraft.player.canEntityBeSeen(entity);
        }
        if (entity instanceof EntityEnderCrystal) {
            EntityEnderCrystal crystal = (EntityEnderCrystal)entity;
            if (!crystal.isDead) {
                return entTypes[2];
            }
        }
        if (entity instanceof EntityMinecartContainer || entity instanceof IProjectile || entity instanceof EntityArmorStand) {
            return entTypes[4] && !Minecraft.player.canEntityBeSeen(entity);
        }
        return false;
    }

    private boolean isCurrent(boolean[] entTypes, TileEntity tileEntity) {
        return entTypes[4];
    }

    private int getChamsColor(Entity entityIn) {
        int color = 0;
        if (entityIn instanceof EntityEnderCrystal) {
            switch (this.ColorMode.currentMode) {
                case "Client": {
                    color = ClientColors.getColor1(Math.abs(entityIn.getEntityId()), this.Opacity.getFloat());
                    break;
                }
                case "Picker": {
                    color = this.PickColor.color;
                }
            }
        }
        return color;
    }

    private void crystalPreChams(Runnable renderModel) {
        float hds = ((float)Minecraft.player.ticksExisted + mc.getRenderPartialTicks()) % 20.0f / 20.0f;
        hds = (float)MathUtils.easeInOutQuadWave(hds);
        float startScale = 1.025f;
        float endScale = 1.05f + 0.6f * hds;
        float alphaStart = 0.3f;
        float alphaEnd = 0.003921569f;
        int iterations = 5 + (int)(10.0f * hds);
        GL11.glEnable((int)3042);
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE_MINUS_CONSTANT_ALPHA, GlStateManager.DestFactor.ZERO);
        GL11.glDepthMask((boolean)false);
        GL11.glEnable((int)2884);
        GL11.glCullFace((int)1028);
        WallHack.mc.entityRenderer.disableLightmap();
        GlStateManager.disableLighting();
        GL11.glAlphaFunc((int)516, (float)0.003921569f);
        ModelEnderCrystal.cancelBase = true;
        for (int index = 0; index < iterations; ++index) {
            float scale = MathUtils.lerp(startScale, endScale, (float)index / (float)iterations);
            float alphaPC = MathUtils.lerp(alphaStart, alphaEnd, (float)index / (float)iterations);
            GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)alphaPC);
            float descale = 1.0f / scale;
            float append = 0.5f;
            GL11.glTranslated((double)0.0, (double)append, (double)0.0);
            GL11.glScaled((double)scale, (double)scale, (double)scale);
            GL11.glTranslated((double)0.0, (double)(-append), (double)0.0);
            renderModel.run();
            GL11.glTranslated((double)0.0, (double)append, (double)0.0);
            GL11.glScaled((double)descale, (double)descale, (double)descale);
            GL11.glTranslated((double)0.0, (double)(-append), (double)0.0);
        }
        ModelEnderCrystal.cancelBase = false;
        GL11.glAlphaFunc((int)516, (float)0.1f);
        GlStateManager.enableLighting();
        WallHack.mc.entityRenderer.enableLightmap();
        GL11.glEnable((int)3553);
        GL11.glCullFace((int)1029);
        GL11.glDepthMask((boolean)true);
        GL11.glDisable((int)2884);
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.resetColor();
    }

    private void renderChams(boolean chamsMode, boolean pre, int col, Runnable renderModel, boolean isRenderItems) {
        if (pre) {
            if (!isRenderItems && chamsMode) {
                GlStateManager.enableBlend();
                GL11.glDisable((int)3553);
                GL11.glDisable((int)3008);
                GL11.glEnable((int)2884);
                GL11.glDisable((int)2896);
                RenderUtils.glColor(ColorUtils.swapAlpha(col, (float)ColorUtils.getAlphaFromColor(col) / 2.0f));
                GL11.glDepthMask((boolean)false);
                WallHack.mc.entityRenderer.disableLightmap();
                GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            }
            if (isRenderItems) {
                GL11.glEnable((int)3553);
            } else {
                GL11.glDepthRange((double)0.0, (double)0.01);
            }
            renderModel.run();
            GL11.glDepthRange((double)0.0, (double)1.0);
            if (!isRenderItems && chamsMode) {
                GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                WallHack.mc.entityRenderer.enableLightmap();
                GL11.glEnable((int)2896);
                GL11.glDepthMask((boolean)true);
                GL11.glEnable((int)3553);
                GL11.glEnable((int)3008);
                GlStateManager.resetColor();
                GlStateManager.color(1.0f, 1.0f, 1.0f);
            }
        }
    }

    @Override
    public void preRenderLivingBase(Entity baseIn, Runnable renderModel, boolean isRenderItems) {
        if (!this.isCurrent(this.getEtypes(), baseIn)) {
            return;
        }
        WallHack.mc.renderManager.renderShadow = false;
        boolean crystal = baseIn instanceof EntityEnderCrystal;
        if (!isRenderItems && crystal && ModelEnderCrystal.canDeformate) {
            this.crystalPreChams(renderModel);
        }
        this.renderChams(crystal, true, this.getChamsColor(baseIn), renderModel, isRenderItems);
    }

    @Override
    public void postRenderLivingBase(Entity baseIn, Runnable renderModel, boolean isRenderItems) {
        if (!this.isCurrent(this.getEtypes(), baseIn)) {
            return;
        }
        WallHack.mc.renderManager.renderShadow = WallHack.mc.gameSettings.entityShadows;
        boolean crystal = baseIn instanceof EntityEnderCrystal;
        this.renderChams(crystal, false, 0, renderModel, isRenderItems);
    }

    public void preRenderTileEntity(TileEntity tileIn, Runnable renderModel) {
        if (!this.isCurrent(this.getEtypes(), tileIn)) {
            return;
        }
        WallHack.mc.renderManager.renderShadow = false;
        this.renderChams(false, true, 0, renderModel, false);
    }

    public void postRenderTileEntity(TileEntity tileIn, Runnable renderModel) {
        if (!this.isCurrent(this.getEtypes(), tileIn)) {
            return;
        }
        WallHack.mc.renderManager.renderShadow = WallHack.mc.gameSettings.entityShadows;
        this.renderChams(false, true, 0, renderModel, false);
    }
}

