package ru.govno.client.utils.Minimap;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Iterables;
import java.awt.Color;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.chunk.Chunk;
import ru.govno.client.utils.Math.MathUtils;
import ru.govno.client.utils.Render.ColorUtils;

public class MinimapData {
    public DynamicTexture texture;
    public byte[] colors = new byte[16384];
    public float[][] lights = new float[16384][4];
    public int lastX;
    public int lastZ;
    public int range;
    Minecraft mc = Minecraft.getMinecraft();
    private float lastSunLight;
    private BiomeProvider lastBiomeProvider;
    double lastUpdatedX;
    double lastUpdatedZ;
    double walked;

    public MinimapData(int range) {
        this.texture = new DynamicTexture(range, range);
        for (int i = 0; i < this.texture.getTextureData().length; ++i) {
            this.texture.getTextureData()[i] = 0;
        }
        this.range = range;
    }

    public int getRange() {
        return this.range;
    }

    public void setRange(int range) {
        if (range == this.range) {
            return;
        }
        this.texture = new DynamicTexture(range, range);
        for (int i = 0; i < this.texture.getTextureData().length; ++i) {
            this.texture.getTextureData()[i] = 0;
        }
        this.colors = new byte[range * range];
        this.lights = new float[range * range][4];
        this.range = range;
    }

    public void updateMap(World world, Entity player) {
        if (this.shouldUpdate(player)) {
            this.updateData(world, player);
            this.updateTexture();
        }
    }

    public boolean shouldUpdate(Entity player) {
        int x = player.getPosition().getX();
        int z = player.getPosition().getZ();
        if (this.lastX != x || this.lastZ != z) {
            this.lastX = x;
            this.lastZ = z;
            return true;
        }
        return true;
    }

    private float[] getMapLightPC(Chunk chunk, BlockPos pos, int dst, int rangeHandle) {
        float[] vals = new float[4];
        vals[1] = (float)(this.mc.world.getCombinedLight(pos, (int)(this.lastSunLight * 15.0f)) & 0xFFFF) / 255.0f;
        vals[0] = this.lastSunLight * (0.8f + vals[1] / 5.0f);
        vals[2] = chunk.getBiome(pos, this.lastBiomeProvider).getFloatTemperature(pos);
        return vals;
    }

    public double getLastPlayerDX() {
        return this.lastUpdatedX;
    }

    public double getLastPlayerDZ() {
        return this.lastUpdatedZ;
    }

    public void updateData(World world, Entity player) {
        if (player.dimension == -1) {
            for (int i = 0; i < this.colors.length; ++i) {
                this.colors[i] = 0;
            }
            return;
        }
        double smoothPosX = player.lastTickPosX;
        double smoothPosZ = player.lastTickPosZ;
        double walkedA = Math.sqrt(player.posZ * player.posZ + player.posX * player.posX);
        boolean notMove = Math.abs(walkedA - this.walked) < 0.8;
        float sunLight = this.mc.world.getSunBrightness(1.0f);
        if (notMove && player.ticksExisted % 14 != 0 && MathUtils.getDifferenceOf(this.lastSunLight, sunLight) < (double)0.08f) {
            return;
        }
        this.lastSunLight = sunLight;
        this.lastBiomeProvider = this.mc.world.provider.getBiomeProvider();
        this.lastUpdatedX = (int)player.lastTickPosX;
        this.lastUpdatedZ = (int)player.lastTickPosZ;
        this.walked = walkedA;
        int range = this.getRange();
        if (world.provider.getHasNoSky()) {
            range /= 2;
        }
        int floorRangeD2 = range / 2;
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
        HashMultiset multiset = HashMultiset.create();
        for (int x = floorRangeD2 - range + 1; x < floorRangeD2 + range; ++x) {
            double d0 = 0.0;
            for (int z = floorRangeD2 - range - 1; z < floorRangeD2 + range; ++z) {
                MapColor mapcolor;
                int rsqrt;
                int dx = x - floorRangeD2;
                int dz = z - floorRangeD2;
                if (x < 0 || z < -1 || x >= range || z >= range || dx * dz >= (rsqrt = range * range) || (int)Math.sqrt(((double)dx + 0.5) * ((double)dx + 0.5) + ((double)dz - 0.5) * ((double)dz - 0.5)) >= range / 2) continue;
                int posX = (int)(smoothPosX + (double)x - (double)(range / 2));
                int posZ = (int)(smoothPosZ + (double)z - (double)(range / 2));
                int i = x + z * range;
                int dRxz = dx * dx + dz * dz;
                Chunk chunk = world.getChunkFromChunkCoords(posX >> 4, posZ >> 4);
                if (chunk.isEmpty()) {
                    this.colors[i] = 0;
                    continue;
                }
                int i3 = posX & 0xF;
                int j3 = posZ & 0xF;
                int k3 = 0;
                double d1 = 0.0;
                multiset.clear();
                if (world.provider.getHasNoSky()) {
                    int l3 = posX + posZ * 231871;
                    multiset.add((Object)Blocks.DIRT.getMapColor(), (l3 * l3 * 31287121 + l3 * 11 >> 20 & 1) == 0 ? 10 : 100);
                    d1 = 100.0;
                } else {
                    int heightUP = chunk.getHeightValue(i3, j3) + 1;
                    IBlockState iblockstate = null;
                    if (heightUP > 1) {
                        while (heightUP > 0 && (iblockstate == null || iblockstate.getBlock().getMapColor() == MapColor.AIR)) {
                            iblockstate = chunk.getBlockState(blockpos$mutableblockpos.setPos(i3, --heightUP, j3));
                        }
                        IBlockState state = chunk.getBlockState(i3, --heightUP, j3);
                        if (heightUP > 0 && iblockstate.getMaterial().isLiquid()) {
                            while (heightUP > 0 && (state == null || state.getMaterial().isLiquid()) && k3 < 5) {
                                state = chunk.getBlockState(i3, --heightUP, j3);
                                ++k3;
                            }
                        }
                    }
                    d1 += (double)heightUP;
                    MapColor mapColor = iblockstate == null ? MapColor.AIR : iblockstate.getBlock().getMapColor();
                    multiset.add((Object)mapColor);
                }
                double d2 = (d1 - d0) * 4.0 / 4.0 + ((double)(x + z & 1) - 0.5) * 0.4;
                int i5 = 1;
                if (d2 > 0.6) {
                    i5 = 2;
                }
                if (d2 < -0.6) {
                    i5 = 0;
                }
                if ((mapcolor = (MapColor)Iterables.getFirst((Iterable)multiset, (Object)MapColor.AIR)) == MapColor.WATER) {
                    d2 = (double)k3 * 0.1 + (x % 2 != z % 2 ? 1.0 : 0.0) * 0.2;
                    i5 = 1;
                    if (d2 < 0.5) {
                        i5 = 2;
                    }
                    if (d2 > 0.9) {
                        i5 = 0;
                    }
                }
                d0 = d1;
                if (z < 0 || dRxz >= rsqrt) continue;
                this.colors[i] = (byte)(mapcolor.colorIndex * 4 + i5);
                blockpos$mutableblockpos.setPos(dx + (int)smoothPosX, blockpos$mutableblockpos.getY() + 1, dz + (int)smoothPosZ);
                this.lights[i] = this.getMapLightPC(chunk, blockpos$mutableblockpos, 0, 1);
            }
        }
    }

    public void updateTexture() {
        int range = this.getRange();
        for (int i = 0; i < range * range; ++i) {
            int j = this.colors[i] & 0xFF;
            if (j / 4 == 0) {
                this.texture.getTextureData()[i] = (i + i / range & 1) * 8 + 16 << 24;
                continue;
            }
            MapColor mapColor = MapColor.COLORS[j / 4];
            int color = mapColor.getMapColor(j & 3);
            if (mapColor == MapColor.TNT) {
                color = Color.HSBtoRGB(0.05f, 1.0f, MathUtils.clamp(ColorUtils.getBrightnessFromColor(color) + (float)(j & 3) / 255.0f * 15.0f, 0.0f, 1.0f));
            }
            float bright = this.lights[i][0];
            float light = this.lights[i][1];
            float temp = this.lights[i][2];
            float brightMulUp = bright + light * (1.0f - bright);
            int colorTemerature = ColorUtils.getOverallColorFrom(ColorUtils.getColor(167, 180, 255), ColorUtils.getColor(255, 191, 117), temp / 2.0f);
            colorTemerature = Color.getHSBColor(ColorUtils.getHueFromColor(colorTemerature), ColorUtils.getSaturateFromColor(colorTemerature), bright * light * 0.3333f).getRGB();
            float r = ColorUtils.getGLRedFromColor(color);
            r += ColorUtils.getGLRedFromColor(colorTemerature);
            r *= brightMulUp;
            r = Math.min(r, 1.0f);
            float g = ColorUtils.getGLGreenFromColor(color);
            g += ColorUtils.getGLGreenFromColor(colorTemerature);
            g *= brightMulUp;
            g = Math.min(g, 1.0f);
            float b = ColorUtils.getGLBlueFromColor(color);
            b += ColorUtils.getGLBlueFromColor(colorTemerature);
            b *= brightMulUp;
            b = Math.min(b, 1.0f);
            this.texture.getTextureData()[i] = color = ColorUtils.getColor((int)(r * 255.0f), (int)(g * 255.0f), (int)(b * 255.0f), ColorUtils.getAlphaFromColor(color));
        }
        this.texture.updateDynamicTexture();
    }

    public DynamicTexture getTexture() {
        return this.texture;
    }
}

