package ru.govno.client.utils;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class RotationUtils {
    public static float getSmoothSensitivity(float value) {
        Minecraft mc = Minecraft.getMinecraft();
        float f1;
        f1 = (f1 = (float)((double)mc.gameSettings.mouseSensitivity * 0.6 + 0.2)) * f1 * f1 * 8.0F;
        return (float)Math.round(value / (float)((double)f1 * 0.15)) * (float)((double)f1 * 0.15);
    }

    public static float[] setRotationsToVec3d(Vec3d vec) {
        Minecraft mc = Minecraft.getMinecraft();
        Vec3d eyesPos = new Vec3d(
                Minecraft.player.posX, Minecraft.player.getEntityBoundingBox().minY + (double)Minecraft.player.getEyeHeight(), Minecraft.player.posZ
        );
        double diffX = vec.xCoord - eyesPos.xCoord;
        double diffY = vec.yCoord - eyesPos.yCoord;
        double diffZ = vec.zCoord - eyesPos.zCoord;
        double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);
        float yaw = (float)(Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0);
        float pitch = (float)(-Math.toDegrees(Math.atan2(diffY, diffXZ)));
        pitch = MathHelper.clamp(pitch, -90.0F, 90.0F);
        return new float[]{yaw, pitch};
    }

    public static float updateRotation(float current, float finalState, float speed) {
        float wrapDegrees = MathHelper.wrapDegrees(finalState - current);
        if (wrapDegrees > speed) {
            wrapDegrees = speed;
        }

        if (wrapDegrees < -speed) {
            wrapDegrees = -speed;
        }

        return current + wrapDegrees;
    }

    public static double random(double min, double max) {
        return (double)((float)(min + (max - min) * Math.random()));
    }

    public static float[] getAverageRotations(List list) {
        double d = 0.0;
        double d2 = 0.0;
        double d3 = 0.0;

        for (Object entityw : list) {
            Entity entity = (Entity)entityw;
            d += entity.posX;
            d2 += entity.getEntityBoundingBox().maxY - 2.0;
            d3 += entity.posZ;
        }

        float[] array = new float[2];
        int n = 0;
        d /= (double)list.size();
        d3 /= (double)list.size();
        double var11;
        array[0] = getRotationFromPosition(d, d3, var11 = d2 / (double)list.size())[0];
        array[1] = getRotationFromPosition(d, d3, var11)[1];
        return array;
    }

    public static float getDistanceBetweenAngles(float f, float f2) {
        float f3 = Math.abs(f - f2) % 360.0F;
        if (f3 > 180.0F) {
            f3 = 360.0F - f3;
        }

        return f3;
    }

    public static float getTrajAngleSolutionLow(float f, float f2, float f3) {
        float f4 = f3 * f3 * f3 * f3 - 0.006F * (0.006F * f * f + 2.0F * f2 * f3 * f3);
        return (float)Math.toDegrees(Math.atan(((double)(f3 * f3) - Math.sqrt((double)f4)) / (double)(0.006F * f)));
    }

    public static float[] getRotations(double x, double y, double z) {
        double diffX = x + 0.5 - Minecraft.player.posX;
        double diffY = (y + 0.5) / 2.0 - (Minecraft.player.posY + (double)Minecraft.player.getEyeHeight());
        double diffZ = z + 0.5 - Minecraft.player.posZ;
        double dist = (double)MathHelper.sqrt(diffX * diffX + diffZ * diffZ);
        float yaw = (float)(Math.atan2(diffZ, diffX) * 180.0 / Math.PI) - 90.0F;
        float pitch = (float)(-(Math.atan2(diffY, dist) * 180.0 / Math.PI));
        return new float[]{yaw, pitch};
    }

    public static float[] getRotationFromPosition(double d, double d2, double d3) {
        double d4 = d - Minecraft.player.posX;
        double d5 = d2 - Minecraft.player.posZ;
        double d6 = d3 - Minecraft.player.posY - 0.6;
        double d7 = (double)MathHelper.sqrt(d4 * d4 + d5 * d5);
        float f = (float)(Math.atan2(d5, d4) * 180.0 / Math.PI) - 90.0F;
        float f2 = (float)(-(Math.atan2(d6, d7) * 180.0 / Math.PI));
        return new float[]{f, f2};
    }

    public static float[] getNeededRotations(Entity entityLivingBase) {
        double d = entityLivingBase.posX - Minecraft.player.posX;
        double d2 = entityLivingBase.posZ - Minecraft.player.posZ;
        double d3 = entityLivingBase.posY
                + (double)entityLivingBase.getEyeHeight()
                - (Minecraft.player.getEntityBoundingBox().minY + (Minecraft.player.getEntityBoundingBox().maxY - Minecraft.player.getEntityBoundingBox().minY));
        double d4 = (double)MathHelper.sqrt(d * d + d2 * d2);
        float f = (float)(MathHelper.atan2(d2, d) * 180.0 / Math.PI) - 90.0F;
        float f2 = (float)(-(MathHelper.atan2(d3, d4) * 180.0 / Math.PI));
        return new float[]{f, f2};
    }

    public static float[] getRotations(EntityLivingBase entityLivingBase, String string) {
        if (string == "Head") {
            double d = entityLivingBase.posX;
            double d2 = entityLivingBase.posZ;
            double d3 = entityLivingBase.posY + (double)(entityLivingBase.getEyeHeight() / 2.0F);
            return getRotationFromPosition(d, d2, d3);
        } else if (string == "Chest") {
            double d = entityLivingBase.posX;
            double d4 = entityLivingBase.posZ;
            double d5 = entityLivingBase.posY + (double)(entityLivingBase.getEyeHeight() / 2.0F) - 0.75;
            return getRotationFromPosition(d, d4, d5);
        } else if (string == "Dick") {
            double d = entityLivingBase.posX;
            double d6 = entityLivingBase.posZ;
            double d7 = entityLivingBase.posY + (double)(entityLivingBase.getEyeHeight() / 2.0F) - 1.2;
            return getRotationFromPosition(d, d6, d7);
        } else if (string == "Legs") {
            double d = entityLivingBase.posX;
            double d8 = entityLivingBase.posZ;
            double d9 = entityLivingBase.posY + (double)(entityLivingBase.getEyeHeight() / 2.0F) - 1.5;
            return getRotationFromPosition(d, d8, d9);
        } else {
            double d = entityLivingBase.posX;
            double d10 = entityLivingBase.posZ;
            double d11 = entityLivingBase.posY + (double)(entityLivingBase.getEyeHeight() / 2.0F) - 0.5;
            return getRotationFromPosition(d, d10, d11);
        }
    }

    public static float getNewAngle(float f) {
        if ((f = f % 360.0F) >= 180.0F) {
            f -= 360.0F;
        }

        if (f < -180.0F) {
            f += 360.0F;
        }

        return f;
    }
}
