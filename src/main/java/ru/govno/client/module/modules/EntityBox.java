package ru.govno.client.module.modules;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import ru.govno.client.module.Module;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.module.settings.FloatSettings;
import ru.govno.client.utils.Math.MathUtils;

public class EntityBox
extends Module {
    public static EntityBox get;
    public BoolSettings BoxScalling = new BoolSettings("BoxScalling", true, this);
    public BoolSettings Predict;
    public BoolSettings ExtendedRange;
    public FloatSettings BoxScale;
    public FloatSettings TicksCount;
    public FloatSettings EntitiesReach;
    public FloatSettings BlocksReach;
    private static float boxScale;
    private static float blocksReach;
    private static float entitiesReach;
    private static float ticksOffset;

    public EntityBox() {
        super("EntityBox", 0, Module.Category.COMBAT);
        this.settings.add(this.BoxScalling);
        this.BoxScale = new FloatSettings("BoxScale", 1.2f, 2.0f, 0.75f, this, () -> this.BoxScalling.getBool());
        this.settings.add(this.BoxScale);
        this.Predict = new BoolSettings("Predict", true, this);
        this.settings.add(this.Predict);
        this.TicksCount = new FloatSettings("TicksCount", 1.0f, 4.0f, 0.25f, this, () -> this.Predict.getBool());
        this.settings.add(this.TicksCount);
        this.ExtendedRange = new BoolSettings("ExtendedRange", true, this);
        this.settings.add(this.ExtendedRange);
        this.EntitiesReach = new FloatSettings("EntitiesReach", 0.6f, 3.0f, 0.0f, this, () -> this.ExtendedRange.getBool());
        this.settings.add(this.EntitiesReach);
        this.BlocksReach = new FloatSettings("BlocksReach", 0.4f, 2.0f, 0.0f, this, () -> this.ExtendedRange.getBool());
        this.settings.add(this.BlocksReach);
        get = this;
    }

    public static AxisAlignedBB getExtendedHitbox(Vec3d addPos, Entity entityIn, float scale, AxisAlignedBB prevBox) {
        if (mc.isSingleplayer()) {
            return prevBox;
        }
        if (entityIn == null) {
            return null;
        }
        if (prevBox == null) {
            return null;
        }
        if (entityIn instanceof EntityPlayerSP || entityIn.ignoreFrustumCheck) {
            return prevBox;
        }
        double x = entityIn.posX + addPos.xCoord;
        double w = (prevBox.maxX - prevBox.minX) / 2.0;
        double y = entityIn.posY + addPos.yCoord;
        double z = entityIn.posZ + addPos.zCoord;
        Vec3d firstPos = new Vec3d(x - w * (double)scale, y, z - w * (double)scale);
        double h = prevBox.maxY - prevBox.minY;
        Vec3d secondPos = new Vec3d(x + w * (double)scale, y + h, z + w * (double)scale);
        AxisAlignedBB aabb = new AxisAlignedBB(firstPos, secondPos);
        return aabb == null ? entityIn.boundingBox : aabb;
    }

    @Override
    public void onRender2D(ScaledResolution sr) {
        boxScale = MathUtils.lerp(1.0f, EntityBox.get.BoxScale.getAnimation(), EntityBox.get.BoxScalling.getAnimation());
        blocksReach = EntityBox.get.ExtendedRange.getAnimation() * EntityBox.get.BlocksReach.getAnimation();
        entitiesReach = EntityBox.get.ExtendedRange.getAnimation() * EntityBox.get.EntitiesReach.getAnimation();
        ticksOffset = EntityBox.get.Predict.getAnimation() * EntityBox.get.TicksCount.getAnimation();
    }

    public static boolean hitboxModState() {
        return EntityBox.get.actived;
    }

    public static float hitboxModSizeBox() {
        return boxScale;
    }

    public static float hitboxModReachBlocks() {
        return blocksReach;
    }

    public static float hitboxModReachEntities() {
        return entitiesReach;
    }

    public static float hitboxModPredictSize() {
        return ticksOffset;
    }

    public static Vec3d hitboxModPredictVec(Entity entityIn, float ticks) {
        return new Vec3d(-(entityIn.prevPosX - entityIn.posX) * (double)ticks, -(entityIn.prevPosY - entityIn.posY) * (double)ticks, -(entityIn.prevPosZ - entityIn.posZ) * (double)ticks);
    }

    public static boolean entityIsCurrentToExtend(Entity entityIn) {
        return entityIn != null && entityIn instanceof EntityLivingBase && !(entityIn instanceof EntityPlayerSP);
    }

    static {
        boxScale = 1.0f;
    }
}

