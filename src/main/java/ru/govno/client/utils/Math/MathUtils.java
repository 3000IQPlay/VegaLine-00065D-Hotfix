package ru.govno.client.utils.Math;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import javax.vecmath.Vector2f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import optifine.Reflector;
import optifine.ReflectorMethod;
import ru.govno.client.module.modules.BackTrack;

public class MathUtils {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final Random random = new Random();

    public static double easeOutBounce(double value) {
        double n1 = 7.5625;
        double d1 = 2.75;
        if (value < 1.0 / d1) {
            return n1 * value * value;
        }
        if (value < 2.0 / d1) {
            return n1 * (value -= 1.5 / d1) * value + 0.75;
        }
        if (value < 2.5 / d1) {
            return n1 * (value -= 2.25 / d1) * value + 0.9375;
        }
        return n1 * (value -= 2.625 / d1) * value + 0.984375;
    }

    public static double easeInOutElastic(double value) {
        double c5 = 1.3962634015954636;
        return value < 0.0 ? 0.0 : (value > 1.0 ? 1.0 : (value < 0.5 ? -(Math.pow(2.0, 20.0 * value - 10.0) * Math.sin((20.0 * value - 11.125) * c5)) / 2.0 : Math.pow(2.0, -20.0 * value + 10.0) * Math.sin((20.0 * value - 11.125) * c5) / 2.0 + 1.0));
    }

    public static double easeOutElastic(double value) {
        double c4 = 2.0943951023931953;
        return value < 0.0 ? 0.0 : (value > 1.0 ? 1.0 : Math.pow(2.0, -10.0 * value) * Math.sin((value * 10.0 - 0.75) * c4) + 1.0);
    }

    public static double easeOutBack(double value) {
        double c1 = 1.70158;
        double c3 = c1 + 1.0;
        return 1.0 + c3 * Math.pow(value - 1.0, 3.0) + c1 * Math.pow(value - 1.0, 2.0);
    }

    public static double easeOutCubic(double value) {
        return 1.0 - Math.pow(1.0 - value, 3.0);
    }

    public static float valWave01(float value) {
        return ((double)value > 0.5 ? 1.0f - value : value) * 2.0f;
    }

    public static double easeInOutQuad(double value) {
        return value < 0.5 ? 2.0 * value * value : 1.0 - Math.pow(-2.0 * value + 2.0, 2.0) / 2.0;
    }

    public static double easeInOutQuadWave(double value) {
        value = (value > 0.5 ? 1.0 - value : value) * 2.0;
        double d = value = value < 0.5 ? 2.0 * value * value : 1.0 - Math.pow(-2.0 * value + 2.0, 2.0) / 2.0;
        value = value > 1.0 ? 1.0 : (value < 0.0 ? 0.0 : value);
        return value;
    }

    public static double easeInCircle(double value) {
        return 1.0 - Math.sqrt(1.0 - Math.pow(value, 2.0));
    }

    public static double easeOutCirc(double value) {
        return Math.sqrt(1.0 - Math.pow(value - 1.0, 2.0));
    }

    public static double easeInOutExpo(double value) {
        return value < 0.0 ? 0.0 : (value > 1.0 ? 1.0 : (value < 0.5 ? Math.pow(2.0, 20.0 * value - 10.0) / 2.0 : (2.0 - Math.pow(2.0, -20.0 * value + 10.0)) / 2.0));
    }

    public static double roundPROBLYA(float num, double increment) {
        double v = (double)Math.round((double)num / increment) * increment;
        BigDecimal bd = new BigDecimal(v);
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static float calculateGaussianValue(float x, float sigma) {
        double PI = 3.141592653;
        double output = 1.0 / Math.sqrt(2.0 * PI * (double)(sigma * sigma));
        return (float)(output * Math.exp((double)(-(x * x)) / (2.0 * (double)(sigma * sigma))));
    }

    public static Double interpolate(double oldValue, double newValue, double interpolationValue) {
        return oldValue + (newValue - oldValue) * interpolationValue;
    }

    public static String getStringPercent(String text, float percent10) {
        if (text.isEmpty()) {
            return text;
        }
        text = text.substring(0, (int)(MathUtils.clamp(percent10, 0.0f, 1.0f) * (float)text.length()));
        return text;
    }

    public static float interpolateFloat(float oldValue, float newValue, double interpolationValue) {
        return MathUtils.interpolate(oldValue, newValue, (float)interpolationValue).floatValue();
    }

    public static double toPeriud(double value) {
        String append = value + ".";
        for (int i = 0; i < 184; ++i) {
            append = append + value;
        }
        return Double.valueOf(append);
    }

    public static int interpolateInt(int oldValue, int newValue, double interpolationValue) {
        return MathUtils.interpolate(oldValue, newValue, (float)interpolationValue).intValue();
    }

    public static int getMiddle(int i, int j) {
        return (i + j) / 2;
    }

    public static double getMiddleDouble(int i, int j) {
        return ((double)i + (double)j) / 2.0;
    }

    public static float wrapAngleTo180_float(float p_76142_0_) {
        if ((p_76142_0_ %= 360.0f) >= 180.0f) {
            p_76142_0_ -= 360.0f;
        }
        if (p_76142_0_ < -180.0f) {
            p_76142_0_ += 360.0f;
        }
        return p_76142_0_;
    }

    public static double getDifferenceOf(float num1, float num2) {
        return Math.abs(num2 - num1) > Math.abs(num1 - num2) ? (double)Math.abs(num1 - num2) : (double)Math.abs(num2 - num1);
    }

    public static double getDifferenceOf(double num1, double num2) {
        return Math.abs(num2 - num1) > Math.abs(num1 - num2) ? Math.abs(num1 - num2) : Math.abs(num2 - num1);
    }

    public static double getDifferenceOf(int num1, int num2) {
        return Math.abs(num2 - num1) > Math.abs(num1 - num2) ? (double)Math.abs(num1 - num2) : (double)Math.abs(num2 - num1);
    }

    public static double round(double value, int places) {
        if (places < 0) {
            throw new IllegalArgumentException();
        }
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static float getAngleDifference(float direction, float rotationYaw) {
        float phi = Math.abs(rotationYaw - direction) % 360.0f;
        float distance = phi > 180.0f ? 360.0f - phi : phi;
        return distance;
    }

    public static int clamp(int num, int min, int max) {
        return num < min ? min : (num > max ? max : num);
    }

    public static float clamp(float num, float min, float max) {
        return num < min ? min : (num > max ? max : num);
    }

    public static double clamp(double num, double min, double max) {
        return num < min ? min : (num > max ? max : num);
    }

    public static int floor(float value) {
        return MathHelper.floor(value);
    }

    public static int floor(double value) {
        return MathHelper.floor(value);
    }

    public static int ceil(float value) {
        return MathHelper.ceil(value);
    }

    public static int ceil(double value) {
        return MathHelper.ceil(value);
    }

    public static float sin(float value) {
        return MathHelper.sin(value);
    }

    public static float cos(float value) {
        return MathHelper.cos(value);
    }

    public static float wrapDegrees(float value) {
        return MathHelper.wrapDegrees(value);
    }

    public static double wrapDegrees(double value) {
        return MathHelper.wrapDegrees(value);
    }

    public static double getRandomInRange(double max, double min) {
        return min + (max - min) * random.nextDouble();
    }

    public static BigDecimal round(float f, int times) {
        BigDecimal bd = new BigDecimal(Float.toString(f));
        bd = bd.setScale(times, RoundingMode.HALF_UP);
        return bd;
    }

    public static int getRandomInRange(int max, int min) {
        return (int)((double)min + (double)(max - min) * random.nextDouble());
    }

    public static boolean isEven(int number) {
        return number % 2 == 0;
    }

    public static double roundToPlace(double value, int places) {
        if (places < 0) {
            throw new IllegalArgumentException();
        }
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static double preciseRound(double value, double precision) {
        double scale = Math.pow(10.0, precision);
        return (double)Math.round(value * scale) / scale;
    }

    public static double randomNumber(double max, double min) {
        return Math.random() * (max - min) + min;
    }

    public static int randomize(int max, int min) {
        return -min + (int)(Math.random() * (double)(max - -min + 1));
    }

    public static double getIncremental(double val, double inc) {
        double one = 1.0 / inc;
        return (double)Math.round(val * one) / one;
    }

    public static boolean isInteger(Double variable) {
        return variable == Math.floor(variable) && !Double.isInfinite(variable);
    }

    public static float[] constrainAngle(float[] vector) {
        vector[0] = vector[0] % 360.0f;
        vector[1] = vector[1] % 360.0f;
        while (vector[0] <= -180.0f) {
            vector[0] = vector[0] + 360.0f;
        }
        while (vector[1] <= -180.0f) {
            vector[1] = vector[1] + 360.0f;
        }
        while (vector[0] > 180.0f) {
            vector[0] = vector[0] - 360.0f;
        }
        while (vector[1] > 180.0f) {
            vector[1] = vector[1] - 360.0f;
        }
        return vector;
    }

    public static double roundToDecimalPlace(double value, double inc) {
        double halfOfInc = inc / 2.0;
        double floored = Math.floor(value / inc) * inc;
        if (value >= floored + halfOfInc) {
            return new BigDecimal(Math.ceil(value / inc) * inc, MathContext.DECIMAL64).stripTrailingZeros().doubleValue();
        }
        return new BigDecimal(floored, MathContext.DECIMAL64).stripTrailingZeros().doubleValue();
    }

    public static float lerp(float a, float b, float f) {
        return a + f * (b - a);
    }

    public static double lerp(double a, double b, double f) {
        return a + f * (b - a);
    }

    public static float harp(float val, float current, float speed) {
        return (float)MathUtils.harpD(val, current, speed);
    }

    public static double harpD(double val, double current, double speed) {
        double emi = (current - val) * (speed / 2.0) > 0.0 ? Math.max(speed, Math.min(current - val, (current - val) * (speed / 2.0))) : Math.max(current - val, Math.min(-(speed / 2.0), (current - val) * (speed / 2.0)));
        return val + emi;
    }

    public static RayTraceResult getPointed(Vector2f rot, double dst, float scale, boolean walls) {
        EntityPlayerSP entity = Minecraft.player;
        double d0 = dst;
        RayTraceResult objectMouseOver = MathUtils.rayTrace(d0, rot.x, rot.y, walls);
        Vec3d vec3d = entity.getPositionEyes(1.0f);
        boolean flag = false;
        double d1 = d0;
        if (objectMouseOver != null) {
            d1 = objectMouseOver.hitVec.distanceTo(vec3d);
        }
        Vec3d vec3d1 = MathUtils.getLook(rot.x, rot.y);
        Vec3d vec3d2 = vec3d.addVector(vec3d1.xCoord * d0, vec3d1.yCoord * d0, vec3d1.zCoord * d0);
        Entity pointedEntity = null;
        Vec3d vec3d3 = null;
        List<Entity> list = MathUtils.mc.world.getEntitiesInAABBexcluding(entity, entity.getEntityBoundingBox().addCoord(vec3d1.xCoord * d0, vec3d1.yCoord * d0, vec3d1.zCoord * d0).expand(1.0, 1.0, 1.0), (Predicate<? super Entity>)Predicates.and(EntitySelectors.NOT_SPECTATING, (Predicate)new Predicate<Entity>(){

            public boolean apply(@Nullable Entity p_apply_1_) {
                return p_apply_1_ != null && p_apply_1_.canBeCollidedWith();
            }
        })).stream().sorted(Comparator.comparing(a -> Float.valueOf(-entity.getDistanceToEntity((Entity)a)))).toList();
        double d2 = d1;
        block0: for (int j = 0; j < list.size(); ++j) {
            Entity entity1 = list.get(j);
            for (AxisAlignedBB axisalignedbb : BackTrack.get.getTracksAsEntity(entity1, entity1.getEntityBoundingBox().expandXyz(entity1.getCollisionBorderSize()), false)) {
                double d3;
                RayTraceResult raytraceresult = axisalignedbb.calculateIntercept(vec3d, vec3d2);
                if (axisalignedbb.isVecInside(vec3d)) {
                    if (!(d2 >= 0.0)) continue;
                    pointedEntity = entity1;
                    vec3d3 = raytraceresult == null ? vec3d : raytraceresult.hitVec;
                    d2 = 0.0;
                    continue block0;
                }
                if (raytraceresult == null || !((d3 = vec3d.distanceTo(raytraceresult.hitVec)) < d2) && d2 != 0.0) continue;
                boolean flag1 = false;
                if (Reflector.ForgeEntity_canRiderInteract.exists()) {
                    flag1 = Reflector.callBoolean((Object)entity1, (ReflectorMethod)Reflector.ForgeEntity_canRiderInteract, (Object[])new Object[0]);
                }
                if (!flag1 && entity1.getLowestRidingEntity() == entity.getLowestRidingEntity()) {
                    if (d2 != 0.0) continue;
                    pointedEntity = entity1;
                    vec3d3 = raytraceresult.hitVec;
                    continue block0;
                }
                pointedEntity = entity1;
                vec3d3 = raytraceresult.hitVec;
                d2 = d3;
                continue block0;
            }
        }
        if (pointedEntity != null && flag && vec3d.distanceTo(vec3d3) > dst) {
            pointedEntity = null;
            objectMouseOver = new RayTraceResult(RayTraceResult.Type.MISS, vec3d3, null, new BlockPos(vec3d3));
        }
        if (pointedEntity != null && (d2 < d1 || objectMouseOver == null)) {
            objectMouseOver = new RayTraceResult(pointedEntity, vec3d3);
        }
        return objectMouseOver;
    }

    public static double getDistanceToPointedEntity(Vector2f rot, Entity self, Entity target) {
        Entity entity = target;
        Vec3d vec3d = self.getPositionEyes(1.0f);
        Vec3d vec3d1 = MathUtils.getLook(rot.x, rot.y);
        Vec3d vec3d2 = vec3d.addVector(vec3d1.xCoord * 200.0, vec3d1.yCoord * 200.0, vec3d1.zCoord * 200.0);
        AxisAlignedBB eBox = new AxisAlignedBB(target.posX - (double)target.width / 2.0, target.posY, target.posZ - (double)target.width / 2.0, target.posX + (double)target.width / 2.0, target.posY + (double)target.height, target.posZ + (double)target.width / 2.0);
        AxisAlignedBB axisalignedbb = eBox.expandXyz(target.getCollisionBorderSize());
        double dst = 201.0;
        for (AxisAlignedBB aabb : BackTrack.get.getTracksAsEntity(target, axisalignedbb, false)) {
            RayTraceResult raytraceresult = aabb.calculateIntercept(vec3d, vec3d2);
            if (raytraceresult != null && raytraceresult.entityHit != null) {
                return vec3d.distanceTo(raytraceresult.hitVec);
            }
            dst = MathUtils.clamp((double)self.getDistanceToEntity(entity) + (double)target.width / 2.0, 0.0, 201.0);
        }
        return dst;
    }

    public static EntityLivingBase getPointedEntityAt(Entity entity, Vector2f rot, double dst, float scale, boolean walls) {
        double d0 = dst;
        RayTraceResult objectMouseOver = MathUtils.rayTrace(d0, rot.x, rot.y, walls);
        Vec3d vec3d = entity.getPositionEyes(1.0f);
        boolean flag = false;
        double d1 = d0;
        if (objectMouseOver != null) {
            d1 = objectMouseOver.hitVec.distanceTo(vec3d);
        }
        Vec3d vec3d1 = MathUtils.getLook(rot.x, rot.y);
        Vec3d vec3d2 = vec3d.addVector(vec3d1.xCoord * d0, vec3d1.yCoord * d0, vec3d1.zCoord * d0);
        Entity pointedEntity = null;
        Vec3d vec3d3 = null;
        List<Entity> list = MathUtils.mc.world.getEntitiesInAABBexcluding(entity, entity.getEntityBoundingBox().addCoord(vec3d1.xCoord * d0, vec3d1.yCoord * d0, vec3d1.zCoord * d0).expand(1.0, 1.0, 1.0), (Predicate<? super Entity>)Predicates.and(EntitySelectors.NOT_SPECTATING, (Predicate)new Predicate<Entity>(){

            public boolean apply(@Nullable Entity p_apply_1_) {
                return p_apply_1_ != null && p_apply_1_.canBeCollidedWith();
            }
        })).stream().sorted(Comparator.comparing(a -> Float.valueOf(-entity.getDistanceToEntity((Entity)a)))).toList();
        double d2 = d1;
        block0: for (int j = 0; j < list.size(); ++j) {
            Entity entity1 = list.get(j);
            for (AxisAlignedBB axisalignedbb : BackTrack.get.getTracksAsEntity(entity1, entity1.getEntityBoundingBox().expandXyz(entity1.getCollisionBorderSize()), true)) {
                double d3;
                RayTraceResult raytraceresult = axisalignedbb.calculateIntercept(vec3d, vec3d2);
                if (axisalignedbb.isVecInside(vec3d)) {
                    if (!(d2 >= 0.0)) continue;
                    pointedEntity = entity1;
                    vec3d3 = raytraceresult == null ? vec3d : raytraceresult.hitVec;
                    d2 = 0.0;
                    continue block0;
                }
                if (raytraceresult == null || !((d3 = vec3d.distanceTo(raytraceresult.hitVec)) < d2) && d2 != 0.0) continue;
                boolean flag1 = false;
                if (Reflector.ForgeEntity_canRiderInteract.exists()) {
                    flag1 = Reflector.callBoolean((Object)entity1, (ReflectorMethod)Reflector.ForgeEntity_canRiderInteract, (Object[])new Object[0]);
                }
                if (!flag1 && entity1.getLowestRidingEntity() == entity.getLowestRidingEntity()) {
                    if (d2 != 0.0) continue;
                    pointedEntity = entity1;
                    vec3d3 = raytraceresult.hitVec;
                    continue block0;
                }
                pointedEntity = entity1;
                vec3d3 = raytraceresult.hitVec;
                d2 = d3;
                continue block0;
            }
        }
        if (pointedEntity != null && flag && vec3d.distanceTo(vec3d3) > dst) {
            pointedEntity = null;
            objectMouseOver = new RayTraceResult(RayTraceResult.Type.MISS, vec3d3, null, new BlockPos(vec3d3));
        }
        if (pointedEntity != null && (d2 < d1 || objectMouseOver == null)) {
            objectMouseOver = new RayTraceResult(pointedEntity, vec3d3);
        }
        return objectMouseOver != null ? (objectMouseOver.entityHit instanceof EntityLivingBase ? (EntityLivingBase)objectMouseOver.entityHit : null) : null;
    }

    public static EntityLivingBase getPointedEntity(Vector2f rot, double dst, float scale, boolean walls) {
        EntityPlayerSP entity = Minecraft.player;
        double d0 = dst;
        RayTraceResult objectMouseOver = MathUtils.rayTrace(d0, rot.x, rot.y, walls);
        Vec3d vec3d = entity.getPositionEyes(1.0f);
        boolean flag = false;
        double d1 = d0;
        if (objectMouseOver != null) {
            d1 = objectMouseOver.hitVec.distanceTo(vec3d);
        }
        Vec3d vec3d1 = MathUtils.getLook(rot.x, rot.y);
        Vec3d vec3d2 = vec3d.addVector(vec3d1.xCoord * d0, vec3d1.yCoord * d0, vec3d1.zCoord * d0);
        Entity pointedEntity = null;
        Vec3d vec3d3 = null;
        List<Entity> list = MathUtils.mc.world.getEntitiesInAABBexcluding(entity, entity.getEntityBoundingBox().addCoord(vec3d1.xCoord * d0, vec3d1.yCoord * d0, vec3d1.zCoord * d0).expand(1.0, 1.0, 1.0), (Predicate<? super Entity>)Predicates.and(EntitySelectors.NOT_SPECTATING, (Predicate)new Predicate<Entity>(){

            public boolean apply(@Nullable Entity p_apply_1_) {
                return p_apply_1_ != null && p_apply_1_.canBeCollidedWith();
            }
        })).stream().sorted(Comparator.comparing(a -> Float.valueOf(-entity.getDistanceToEntity((Entity)a)))).toList();
        double d2 = d1;
        block0: for (int j = 0; j < list.size(); ++j) {
            Entity entity1 = list.get(j);
            for (AxisAlignedBB axisalignedbb : BackTrack.get.getTracksAsEntity(entity1, entity1.getEntityBoundingBox().expandXyz(entity1.getCollisionBorderSize()), true)) {
                double d3;
                RayTraceResult raytraceresult = axisalignedbb.calculateIntercept(vec3d, vec3d2);
                if (axisalignedbb.isVecInside(vec3d)) {
                    if (!(d2 >= 0.0)) continue;
                    pointedEntity = entity1;
                    vec3d3 = raytraceresult == null ? vec3d : raytraceresult.hitVec;
                    d2 = 0.0;
                    continue block0;
                }
                if (raytraceresult == null || !((d3 = vec3d.distanceTo(raytraceresult.hitVec)) < d2) && d2 != 0.0) continue;
                boolean flag1 = false;
                if (Reflector.ForgeEntity_canRiderInteract.exists()) {
                    flag1 = Reflector.callBoolean((Object)entity1, (ReflectorMethod)Reflector.ForgeEntity_canRiderInteract, (Object[])new Object[0]);
                }
                if (!flag1 && entity1.getLowestRidingEntity() == entity.getLowestRidingEntity()) {
                    if (d2 != 0.0) continue;
                    pointedEntity = entity1;
                    vec3d3 = raytraceresult.hitVec;
                    continue block0;
                }
                pointedEntity = entity1;
                vec3d3 = raytraceresult.hitVec;
                d2 = d3;
                continue block0;
            }
        }
        if (pointedEntity != null && flag && vec3d.distanceTo(vec3d3) > dst) {
            pointedEntity = null;
            objectMouseOver = new RayTraceResult(RayTraceResult.Type.MISS, vec3d3, null, new BlockPos(vec3d3));
        }
        if (pointedEntity != null && (d2 < d1 || objectMouseOver == null)) {
            objectMouseOver = new RayTraceResult(pointedEntity, vec3d3);
        }
        if (objectMouseOver != null && objectMouseOver.entityHit instanceof EntityLivingBase) {
            return (EntityLivingBase)objectMouseOver.entityHit;
        }
        return null;
    }

    public static RayTraceResult rayTrace(double blockReachDistance, float yaw, float pitch, boolean walls) {
        if (!walls) {
            return null;
        }
        Vec3d vec3d = Minecraft.player.getPositionEyes(1.0f);
        Vec3d vec3d1 = MathUtils.getLook(yaw, pitch);
        Vec3d vec3d2 = vec3d.addVector(vec3d1.xCoord * blockReachDistance, vec3d1.yCoord * blockReachDistance, vec3d1.zCoord * blockReachDistance);
        return MathUtils.mc.world.rayTraceBlocks(vec3d, vec3d2, true, true, true);
    }

    static Vec3d getVectorForRotation(float pitch, float yaw) {
        float f = MathHelper.cos(-yaw * ((float)Math.PI / 180) - (float)Math.PI);
        float f1 = MathHelper.sin(-yaw * ((float)Math.PI / 180) - (float)Math.PI);
        float f2 = -MathHelper.cos(-pitch * ((float)Math.PI / 180));
        float f3 = MathHelper.sin(-pitch * ((float)Math.PI / 180));
        return new Vec3d(f1 * f2, f3, f * f2);
    }

    static Vec3d getLook(float yaw, float pitch) {
        return MathUtils.getVectorForRotation(pitch, yaw);
    }
}

