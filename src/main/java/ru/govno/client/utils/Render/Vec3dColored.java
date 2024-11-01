package ru.govno.client.utils.Render;

import net.minecraft.util.math.Vec3d;

public class Vec3dColored {
    double x;
    double y;
    double z;
    int color = -1;

    public double getX() {
        return this.x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return this.y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return this.z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public int getColor() {
        return this.color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public Vec3dColored(Vec3d pos) {
        this.x = pos.xCoord;
        this.y = pos.yCoord;
        this.z = pos.zCoord;
    }

    public Vec3dColored(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vec3dColored(Vec3d pos, int color) {
        this.x = pos.xCoord;
        this.y = pos.yCoord;
        this.z = pos.zCoord;
        this.color = color;
    }

    public Vec3dColored(double x, double y, double z, int color) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.color = color;
    }
}

