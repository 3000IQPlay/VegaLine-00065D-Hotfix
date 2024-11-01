package ru.govno.client.module.modules;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import ru.govno.client.Client;
import ru.govno.client.event.EventTarget;
import ru.govno.client.event.events.EventPlayerMotionUpdate;
import ru.govno.client.event.events.EventSendPacket;
import ru.govno.client.module.Module;
import ru.govno.client.module.modules.Criticals;
import ru.govno.client.module.modules.HitAura;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.module.settings.FloatSettings;
import ru.govno.client.module.settings.ModeSettings;
import ru.govno.client.utils.Combat.GCDFix;
import ru.govno.client.utils.Math.MathUtils;
import ru.govno.client.utils.RandomUtils;

public class BowAimbot
extends Module {
    public static BowAimbot get;
    ModeSettings ShotTo = new ModeSettings("ShotTo", "Chestplate", this, new String[]{"Boots", "Leggings", "Chestplate", "Helmet"});
    ModeSettings RotationType;
    FloatSettings Range;
    BoolSettings Walls;
    BoolSettings Predict;
    public static float yaw;
    public static float pitch;
    private boolean doRotate = false;
    public static EntityLivingBase target;

    public BowAimbot() {
        super("BowAimbot", 0, Module.Category.COMBAT);
        this.settings.add(this.ShotTo);
        this.RotationType = new ModeSettings("RotationType", "Silent", this, new String[]{"Silent", "Camera", "Packet"});
        this.settings.add(this.RotationType);
        this.Range = new FloatSettings("Range", 25.0f, 50.0f, 3.0f, this);
        this.settings.add(this.Range);
        this.Walls = new BoolSettings("Walls", false, this);
        this.settings.add(this.Walls);
        this.Predict = new BoolSettings("Predict", true, this);
        this.settings.add(this.Predict);
        get = this;
    }

    private boolean doEntityBoxPredictSync() {
        return true;
    }

    private float theta(double v, double g, double x, double y) {
        double yv = 2.0 * y * (v * v);
        double gx = g * (x * x);
        double g2 = g * (gx + yv);
        double insqrt = v * v * v * v - g2;
        double sqrt = Math.sqrt(insqrt);
        double numerator = v * v + sqrt;
        double numerator2 = v * v - sqrt;
        double atan1 = Math.atan2(numerator, g * x);
        double atan2 = Math.atan2(numerator2, g * x);
        return (float)Math.min(atan1, atan2);
    }

    private List<Vec3d> getPointsOfThrowable(int maxDensity, float[] rotation) {
        ArrayList<Vec3d> vecs = new ArrayList<Vec3d>();
        if (BowAimbot.mc.world == null) {
            return vecs;
        }
        EntityPlayer entityOf = BowAimbot.getMe();
        if (entityOf == null || !entityOf.isBowing()) {
            return vecs;
        }
        float rotYaw = rotation[0];
        float rotPitch = rotation[1];
        double[] selfHeadRotateWR = new double[]{rotYaw, rotPitch, Math.toRadians(rotYaw), Math.toRadians(rotPitch)};
        Vec3d playerVector = new Vec3d(entityOf.posX, entityOf.posY + (double)entityOf.getEyeHeight(), entityOf.posZ);
        double throwOfX = playerVector.xCoord;
        double throwOfY = playerVector.yCoord;
        double throwOfZ = playerVector.zCoord;
        double shiftX = -Math.sin(selfHeadRotateWR[2]) * Math.cos(selfHeadRotateWR[3]) + (entityOf.capabilities.isFlying ? 0.0 : entityOf.posX - entityOf.lastTickPosX);
        double shiftY = -Math.sin(selfHeadRotateWR[3]) + (entityOf.capabilities.isFlying ? 0.0 : entityOf.posY - entityOf.lastTickPosY);
        double shiftZ = Math.cos(selfHeadRotateWR[2]) * Math.cos(selfHeadRotateWR[3]) + (entityOf.capabilities.isFlying ? 0.0 : entityOf.posZ - entityOf.lastTickPosZ);
        double throwMotion = Math.sqrt(shiftX * shiftX + shiftY * shiftY + shiftZ * shiftZ);
        shiftX /= throwMotion;
        shiftY /= throwMotion;
        shiftZ /= throwMotion;
        float tightPower = (72000.0f - (float)entityOf.getItemInUseCount()) / 20.0f;
        tightPower = (tightPower = (tightPower * tightPower + tightPower * 2.0f) / 3.0f) < 0.1f ? 0.0f : (tightPower > 1.0f ? 1.0f : tightPower);
        shiftX *= (double)(tightPower *= 3.0f);
        shiftY *= (double)tightPower;
        shiftZ *= (double)tightPower;
        while (maxDensity > 0) {
            vecs.add(new Vec3d(throwOfX, throwOfY, throwOfZ));
            double asellate = 0.999;
            shiftY = shiftY * asellate - 0.005;
            if (BowAimbot.mc.world.rayTraceBlocks(playerVector, new Vec3d(throwOfX += (shiftX *= asellate) * 0.1, throwOfY += shiftY * 0.1, throwOfZ += (shiftZ *= asellate) * 0.1)) != null) break;
            --maxDensity;
        }
        return vecs;
    }

    private int getCurrentTicksArrowFly(EntityLivingBase base) {
        float[] rotation = this.getYawPitch(base, false);
        int arrowTicks = this.getPointsOfThrowable(500, rotation).size();
        return arrowTicks;
    }

    private float getTotalPredictTicks(EntityLivingBase base, boolean calc) {
        NetworkPlayerInfo info;
        float ticks = 0.0f;
        if (BowAimbot.mc.world == null || BowAimbot.getMe() == null || !this.Predict.getBool()) {
            return ticks;
        }
        NetHandlerPlayClient connection = mc.getConnection();
        if (connection != null && (info = connection.getPlayerInfo(Minecraft.player.getUniqueID())) != null) {
            float pingedTicks = Math.max(Math.min(((float)info.getResponseTime() - 49.0f) / 50.0f, 20.0f), 0.0f);
            ticks += pingedTicks;
        }
        if (calc) {
            boolean awp;
            boolean bl = awp = Criticals.get != null && Criticals.get.isActived() && Criticals.get.Bowing.getBool() && Criticals.get.BowMode.getMode().equalsIgnoreCase("Vanilla");
            ticks = awp ? (ticks += 1.5f) : (ticks += (float)this.getCurrentTicksArrowFly(base) / 10.0f);
        }
        return ticks;
    }

    private Vec3d getValidBowingEntityRepos(EntityLivingBase base, boolean calc) {
        AxisAlignedBB aabb;
        Vec3d pos = base.getPositionVector();
        if (this.doEntityBoxPredictSync() && (aabb = base.getEntityBoundingBox()) != null) {
            pos.xCoord = aabb.minX + (aabb.maxX - aabb.minX) / 2.0;
            pos.yCoord = aabb.minY;
            pos.zCoord = aabb.minZ + (aabb.maxZ - aabb.minZ) / 2.0;
        }
        float predictValue = this.getTotalPredictTicks(base, calc);
        double deltaMotionX = (base.posX - base.prevPosX) * (double)predictValue;
        double deltaMotionY = (base.posY - base.prevPosY) * (double)predictValue;
        double deltaMotionZ = (base.posZ - base.prevPosZ) * (double)predictValue;
        deltaMotionX = Math.min(Math.max(deltaMotionX, -7.0), 7.0);
        deltaMotionY = Math.min(deltaMotionY / 4.0, (double)0.42f);
        deltaMotionZ = Math.min(Math.max(deltaMotionZ, -7.0), 7.0);
        return pos.addVector(deltaMotionX, deltaMotionY, deltaMotionZ);
    }

    private float getLaunchAngle(EntityLivingBase entity, double v, double g, Vec3d entityValidPos) {
        String mode = this.ShotTo.currentMode;
        float pc = mode.equalsIgnoreCase("Boots") ? entity.getEyeHeight() / 8.0f : (mode.equalsIgnoreCase("Leggings") ? entity.getEyeHeight() / 3.0f : (mode.equalsIgnoreCase("Chestplate") ? entity.getEyeHeight() / 1.85f : (mode.equalsIgnoreCase("Helmet") ? entity.getEyeHeight() / 1.2f : entity.getEyeHeight())));
        double xDiff = entityValidPos.xCoord - BowAimbot.getMe().posX;
        double zDiff = entityValidPos.zCoord - BowAimbot.getMe().posZ;
        double yDiff = entityValidPos.yCoord + (double)pc - (BowAimbot.getMe().posY + (double)BowAimbot.getMe().getEyeHeight()) - (BowAimbot.getMe().posY - BowAimbot.getMe().lastTickPosY) * (double)((float)Math.sqrt(xDiff * xDiff + zDiff * zDiff)) / Math.PI;
        double xCoord = Math.sqrt(xDiff * xDiff + zDiff * zDiff);
        return this.theta(v + 2.0, g, xCoord, yDiff);
    }

    private float[] getYawPitch(EntityLivingBase entity, boolean calc) {
        float akb = (float)BowAimbot.getMe().getItemInUseMaxCount() / 20.0f;
        akb = (akb * akb + akb * 2.0f) / 3.0f;
        akb = MathHelper.clamp_float(akb, 0.0f, 1.0f);
        double v = akb * 3.0f;
        double g = 0.05f;
        if (akb > 1.0f) {
            akb = 1.0f;
        }
        Vec3d entityValidPos = this.getValidBowingEntityRepos(entity, calc);
        float bowTr = (float)((double)((float)(-Math.toDegrees(this.getLaunchAngle(entity, v, g, entityValidPos)))) - (double)4.35f);
        double diffX = entityValidPos.xCoord - BowAimbot.getMe().posX;
        double diffZ = entityValidPos.zCoord - BowAimbot.getMe().posZ;
        float tThetaYaw = (float)(Math.atan2(diffZ, diffX) * 180.0 / Math.PI - 90.0);
        tThetaYaw = BowAimbot.getMe().rotationYaw + GCDFix.getFixedRotation(MathHelper.wrapAngleTo180_float(tThetaYaw - BowAimbot.getMe().rotationYaw));
        return new float[]{tThetaYaw, MathUtils.clamp(bowTr, -90.0f, 90.0f)};
    }

    private static EntityPlayer getMe() {
        return Minecraft.player;
    }

    private boolean entityIsCurrentToFilter(EntityLivingBase entity) {
        EntityPlayer player;
        return !(entity == null || entity.getHealth() == 0.0f || entity instanceof EntityPlayerSP || entity instanceof EntityArmorStand || entity instanceof EntityEnderman || !this.Walls.getBool() && !BowAimbot.getMe().canEntityBeSeen(entity) || entity instanceof EntityPlayer && (player = (EntityPlayer)entity).isCreative() || !(BowAimbot.getMe().getDistanceToEntity(entity) <= this.Range.getFloat()) || Client.friendManager.isFriend(entity.getName()) || Client.summit(entity));
    }

    public final EntityLivingBase getCurrentTarget() {
        return BowAimbot.getMe().isBowing() && (HitAura.TARGET_ROTS == null || !HitAura.get.actived || HitAura.get.Rotation.currentMode.equalsIgnoreCase("None")) ? (EntityLivingBase)BowAimbot.mc.world.getLoadedEntityList().stream().map(Entity::getLivingBaseOf).filter(Objects::nonNull).filter(e -> this.entityIsCurrentToFilter((EntityLivingBase)e)).findFirst().orElse(null) : null;
    }

    public final EntityLivingBase getTarget() {
        return target;
    }

    public static float[] getVirt() {
        return new float[]{yaw, Criticals.get.actived && Criticals.get.Bowing.getBool() ? -1.0f : pitch};
    }

    private void virtRotate(EventPlayerMotionUpdate e, EntityLivingBase entity) {
        if (BowAimbot.getMe().isBowing() && this.entityIsCurrentToFilter(entity) && MathUtils.getDifferenceOf(BowAimbot.getMe().rotationPitch, 0.0f) < 60.0) {
            this.doRotate = true;
            yaw += MathUtils.clamp(this.getYawPitch(entity, true)[0] - yaw, -45.0f, 45.0f);
            pitch += MathUtils.clamp(this.getYawPitch(entity, true)[1] - pitch, -15.0f, 15.0f);
            float f = BowAimbot.mc.gameSettings.mouseSensitivity * 0.6f + 0.2f;
            float gcd = f * f * f * 1.2f + (float)RandomUtils.randomNumber((int)f, (int)(-f));
            yaw -= yaw % gcd % gcd;
            pitch -= pitch % gcd % gcd;
        } else if (MathUtils.getDifferenceOf(yaw, e.getYaw()) >= 1.0 && MathUtils.getDifferenceOf(pitch, e.getPitch()) >= 1.0 && this.doRotate) {
            yaw += MathUtils.clamp(e.getYaw() - yaw, -45.0f, 45.0f);
            pitch += MathUtils.clamp(e.getPitch() - pitch, -15.0f, 15.0f);
            float f = BowAimbot.mc.gameSettings.mouseSensitivity * 0.6f + 0.2f;
            float gcd = f * f * f * 1.2f + (float)RandomUtils.randomNumber((int)f, (int)(-f));
            yaw -= yaw % gcd % gcd;
            pitch -= pitch % gcd % gcd;
        } else {
            this.doRotate = false;
            yaw = e.getYaw();
            pitch = e.getPitch();
        }
    }

    private void rotate(EventPlayerMotionUpdate e) {
        if (e == null) {
            mc.getConnection().preSendPacket(new CPacketPlayer.Rotation(yaw, pitch, Minecraft.player.onGround));
            BowAimbot.getMe().rotationYawHead = yaw;
            BowAimbot.getMe().renderYawOffset = yaw;
            BowAimbot.getMe().rotationPitchHead = pitch;
            return;
        }
        e.setYaw(yaw);
        e.setPitch(pitch);
        BowAimbot.getMe().rotationYawHead = yaw;
        BowAimbot.getMe().renderYawOffset = yaw;
        BowAimbot.getMe().rotationPitchHead = pitch;
        if (this.RotationType.getMode().equalsIgnoreCase("Camera")) {
            BowAimbot.getMe().rotationYaw = yaw;
            BowAimbot.getMe().rotationPitch = pitch;
        }
    }

    @EventTarget
    public void onPlayerMotionUpdate(EventPlayerMotionUpdate e) {
        if (!this.actived || BowAimbot.mc.world == null || BowAimbot.getMe() == null) {
            return;
        }
        target = this.getCurrentTarget();
        this.virtRotate(e, target);
        if (this.doRotate && !this.RotationType.getMode().equalsIgnoreCase("Packet")) {
            this.rotate(e);
        }
    }

    @EventTarget
    public void onSend(EventSendPacket event) {
        CPacketPlayerDigging packet;
        Packet packet2;
        if (this.isActived() && (packet2 = event.getPacket()) instanceof CPacketPlayerDigging && (packet = (CPacketPlayerDigging)packet2).getAction() == CPacketPlayerDigging.Action.RELEASE_USE_ITEM && this.doRotate) {
            this.rotate(null);
        }
    }

    @Override
    public void onToggled(boolean actived) {
        target = null;
        super.onToggled(actived);
    }

    static {
        target = null;
    }
}

