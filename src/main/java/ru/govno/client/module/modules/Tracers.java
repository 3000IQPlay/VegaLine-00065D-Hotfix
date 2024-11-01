package ru.govno.client.module.modules;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;
import ru.govno.client.Client;
import ru.govno.client.module.Module;
import ru.govno.client.module.modules.WorldRender;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.module.settings.ColorSettings;
import ru.govno.client.module.settings.FloatSettings;
import ru.govno.client.utils.Command.impl.Panic;
import ru.govno.client.utils.Render.AnimationUtils;
import ru.govno.client.utils.Render.ColorUtils;

public class Tracers
extends Module {
    BoolSettings Players;
    BoolSettings Friends;
    BoolSettings Mobs;
    ColorSettings PlayerPick;
    ColorSettings FriendPick;
    ColorSettings MobsPick;
    FloatSettings LineWidthI;
    private final AnimationUtils alphaPC = new AnimationUtils(0.0f, 0.0f, 0.025f);
    private final Tessellator tessellator = Tessellator.getInstance();
    private final BufferBuilder buffer = this.tessellator.getBuffer();
    private final List<Entity> entities = new CopyOnWriteArrayList<Entity>();

    public Tracers() {
        super("Tracers", 0, Module.Category.RENDER);
        this.Players = new BoolSettings("Players", true, this);
        this.settings.add(this.Players);
        this.PlayerPick = new ColorSettings("Player color", ColorUtils.getColor(255, 40, 95, 195), this, () -> this.Players.getBool());
        this.settings.add(this.PlayerPick);
        this.Friends = new BoolSettings("Friends", true, this);
        this.settings.add(this.Friends);
        this.FriendPick = new ColorSettings("Friend color", ColorUtils.getColor(0, 255, 0), this, () -> this.Friends.getBool());
        this.settings.add(this.FriendPick);
        this.Mobs = new BoolSettings("Mobs", false, this);
        this.settings.add(this.Mobs);
        this.MobsPick = new ColorSettings("Mob color", ColorUtils.getColor(0, 170, 120, 110), this, () -> this.Mobs.getBool());
        this.settings.add(this.MobsPick);
        this.LineWidthI = new FloatSettings("Line width", 0.05f, 3.5f, 0.05f, this, () -> this.Players.getBool() || this.Friends.getBool() || this.Mobs.getBool());
        this.settings.add(this.LineWidthI);
    }

    private float alphaPC(boolean modIsEnabled, List<Entity> entities) {
        this.alphaPC.to = modIsEnabled && entities != null && Tracers.mc.gameSettings.thirdPersonView == 0 ? 1.0f : 0.0f;
        return this.alphaPC.getAnim();
    }

    private int[] getColor(Entity entityIn, float alphaPC) {
        int color = Integer.MIN_VALUE;
        if (entityIn instanceof EntityOtherPlayerMP) {
            EntityOtherPlayerMP player = (EntityOtherPlayerMP)entityIn;
            color = Client.friendManager.isFriend(player.getName()) ? this.FriendPick.getCol() : this.PlayerPick.getCol();
        } else if (entityIn instanceof EntityMob || entityIn instanceof EntityAnimal) {
            color = Client.friendManager.isFriend(entityIn.getName()) ? ColorUtils.getOverallColorFrom(this.MobsPick.getCol(), this.FriendPick.getCol()) : this.MobsPick.getCol();
        }
        color = ColorUtils.swapAlpha(color, alphaPC * (float)ColorUtils.getAlphaFromColor(color));
        int color2 = ColorUtils.swapAlpha(color, alphaPC * (float)ColorUtils.getAlphaFromColor(color) / 2.0f);
        return new int[]{color, color2};
    }

    private double interpolate(double val, double val2, float pt) {
        return val + (val2 - val) * (double)pt;
    }

    private Vec3d getRenderEntityPos(Entity fromEntity, float pTicks) {
        double x = this.interpolate(fromEntity.lastTickPosX, fromEntity.posX, pTicks);
        double y = this.interpolate(fromEntity.lastTickPosY, fromEntity.posY, pTicks);
        double z = this.interpolate(fromEntity.lastTickPosZ, fromEntity.posZ, pTicks);
        return new Vec3d(x, y, z);
    }

    private DVec3d DVecToEntity(Entity fromEntity, RenderManager manager) {
        float radiansF = (float)(-Math.PI) / 180;
        Vec3d returnPos = new Vec3d(RenderManager.viewerPosX, RenderManager.viewerPosY, RenderManager.viewerPosZ);
        Vec3d second = this.getRenderEntityPos(fromEntity, mc.getRenderPartialTicks()).addVector(0.0, (double)fromEntity.height * 0.5, 0.0).addVector(-returnPos.xCoord, -returnPos.yCoord, -returnPos.zCoord);
        Vec3d first = new Vec3d(0.0, 0.0, 0.285).rotatePitch(Minecraft.player.rotationPitch * radiansF).rotateYaw(Minecraft.player.rotationYaw * radiansF).addVector(0.0, Minecraft.player.getEyeHeight(), 0.0);
        return new DVec3d(first, second);
    }

    private void vertexFromVec3d(Vec3d vec, int color) {
        this.buffer.pos(vec).color(color).endVertex();
    }

    private void setup3dLinesRender(Runnable displays, float lineW) {
        boolean viewBobbing = Tracers.mc.gameSettings.viewBobbing;
        Tracers.mc.gameSettings.viewBobbing = false;
        Tracers.mc.entityRenderer.setupCameraTransform(mc.getRenderPartialTicks(), 0);
        Tracers.mc.gameSettings.viewBobbing = viewBobbing;
        Tracers.mc.entityRenderer.disableLightmap();
        GL11.glDisable((int)2896);
        GL11.glBlendFunc((int)770, (int)771);
        GL11.glShadeModel((int)7425);
        GL11.glDisable((int)3553);
        GL11.glEnable((int)3042);
        GL11.glDisable((int)3008);
        GL11.glEnable((int)2848);
        GL11.glHint((int)3154, (int)4354);
        GL11.glLineWidth((float)(1.0E-4f + lineW));
        this.buffer.begin(3, DefaultVertexFormats.POSITION_COLOR);
        displays.run();
        this.tessellator.draw();
        GL11.glLineWidth((float)1.0f);
        GL11.glHint((int)3154, (int)4352);
        GL11.glDisable((int)2848);
        GL11.glEnable((int)3008);
        GL11.glEnable((int)3553);
        GL11.glShadeModel((int)7424);
    }

    private static boolean[] enabledTypesENTITY(Tracers mod) {
        return new boolean[]{mod.Players.getBool(), mod.Friends.getBool(), mod.Mobs.getBool()};
    }

    private boolean updatedList(boolean[] currents) {
        if (Tracers.mc.world == null) {
            return false;
        }
        this.entities.clear();
        Tracers.mc.world.getLoadedEntityList().forEach(entity -> {
            block2: {
                EntityLivingBase base;
                block4: {
                    block3: {
                        if (entity == null || !(entity instanceof EntityLivingBase) || !(base = (EntityLivingBase)entity).isEntityAlive()) break block2;
                        if (!(base instanceof EntityOtherPlayerMP)) break block3;
                        EntityOtherPlayerMP player = (EntityOtherPlayerMP)base;
                        if (currents[0] && !Client.friendManager.isFriend(player.getName()) || currents[1] && Client.friendManager.isFriend(player.getName())) break block4;
                    }
                    if (!currents[2] || !(base instanceof EntityMob) && !(base instanceof EntityAnimal)) break block2;
                }
                this.entities.add(base);
            }
        });
        return Minecraft.player != null && this.entities != null || !this.entities.isEmpty();
    }

    private void drawBeginTracer(Entity fromEntity, RenderManager manager, float alphaPC) {
        int[] color = this.getColor(fromEntity, alphaPC);
        if (ColorUtils.getAlphaFromColor(color[0]) < 1 || ColorUtils.getAlphaFromColor(color[0]) > 255) {
            return;
        }
        DVec3d dvec = this.DVecToEntity(fromEntity, manager);
        assert (mc.getRenderViewEntity() != null);
        this.vertexFromVec3d(dvec.second, color[0]);
        this.vertexFromVec3d(dvec.first, color[1]);
    }

    private void drawTracers(List entities, RenderManager manager, float alphaPC, float lineW) {
        this.setup3dLinesRender(() -> {
            GL11.glTranslated((double)(-WorldRender.get.getLastTranslated().xCoord), (double)(-WorldRender.get.getLastTranslated().yCoord), (double)(-WorldRender.get.getLastTranslated().zCoord));
            entities.forEach(e -> this.drawBeginTracer((Entity)e, manager, alphaPC));
        }, lineW);
    }

    @Override
    public void alwaysRender3D() {
        float alphaPC = this.alphaPC(this.actived, this.entities);
        if (Panic.stop || alphaPC < 0.05f) {
            return;
        }
        RenderManager manager = mc.getRenderManager();
        boolean[] currentTypes = Tracers.enabledTypesENTITY(this);
        if (this.updatedList(currentTypes)) {
            this.drawTracers(this.entities, manager, alphaPC, this.LineWidthI.getFloat());
        }
    }

    @Override
    public void onUpdate() {
        if (WorldRender.get.isActived() && WorldRender.get.CameraTweaks.getBool()) {
            Client.msg("\u00a7f\u00a7lModules:\u00a7r \u00a77[\u00a7l" + this.getName() + "\u00a7r\u00a77]: Tracers \u043d\u0435 \u0441\u043e\u0432\u043c\u0435\u0441\u0442\u0438\u043c \u0441", false);
            Client.msg("\u00a77CameraTweaks \u0432 WorldRender, \u0432\u044b\u043a\u043b\u044e\u0447\u0438\u0442\u0435", false);
            Client.msg("\u00a77CameraTweaks \u0447\u0442\u043e-\u0431\u044b \u0432\u043a\u043b\u044e\u0447\u0438\u0442\u044c Tracers.", false);
            this.toggle();
        }
    }

    @Override
    public void onToggled(boolean actived) {
        if (actived && WorldRender.get.isActived() && WorldRender.get.CameraTweaks.getBool()) {
            Client.msg("\u00a7f\u00a7lModules:\u00a7r \u00a77[\u00a7l" + this.getName() + "\u00a7r\u00a77]: Tracers \u043d\u0435 \u0441\u043e\u0432\u043c\u0435\u0441\u0442\u0438\u043c \u0441", false);
            Client.msg("\u00a77CameraTweaks \u0432 WorldRender, \u0432\u044b\u043a\u043b\u044e\u0447\u0438\u0442\u0435", false);
            Client.msg("\u00a77CameraTweaks \u0447\u0442\u043e-\u0431\u044b \u0432\u043a\u043b\u044e\u0447\u0438\u0442\u044c Tracers.", false);
            this.toggle();
        }
        super.onToggled(actived);
    }

    protected final class DVec3d {
        Vec3d first;
        Vec3d second;

        private DVec3d(Vec3d first, Vec3d second) {
            this.first = first;
            this.second = second;
        }
    }
}

