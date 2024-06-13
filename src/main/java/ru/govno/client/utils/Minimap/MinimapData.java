package ru.govno.client.utils.Minimap;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multiset;
import java.awt.Color;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import ru.govno.client.utils.Math.MathUtils;
import ru.govno.client.utils.Render.ColorUtils;

public class MinimapData {
   public DynamicTexture texture;
   public byte[] colors = new byte[16384];
   public int lastX;
   public int lastZ;
   public int range;

   public MinimapData(int range) {
      this.texture = new DynamicTexture(range, range);

      for (int i = 0; i < this.texture.getTextureData().length; i++) {
         this.texture.getTextureData()[i] = 0;
      }

      this.range = range;
   }

   public int getRange() {
      return this.range;
   }

   public void setRange(int range) {
      if (range != this.range) {
         this.texture = new DynamicTexture(range, range);

         for (int i = 0; i < this.texture.getTextureData().length; i++) {
            this.texture.getTextureData()[i] = 0;
         }

         this.colors = new byte[range * range];
         this.range = range;
      }
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
      if (this.lastX == x && this.lastZ == z) {
         return true;
      } else {
         this.lastX = x;
         this.lastZ = z;
         return true;
      }
   }

   public void updateData(World world, Entity player) {
      double smoothPosX = player.lastTickPosX;
      double smoothPosZ = player.lastTickPosZ;
      int range = this.getRange();
      if (world.provider.getHasNoSky()) {
         range /= 2;
      }

      int floorRangeD2 = range / 2;

      for (int x = floorRangeD2 - range + 1; x < floorRangeD2 + range; x++) {
         double d0 = 0.0;

         for (int z = floorRangeD2 - range - 1; z < floorRangeD2 + range; z++) {
            int dx = x - floorRangeD2;
            int dz = z - floorRangeD2;
            if (x >= 0 && z >= -1 && x < range && z < range && dx * dz < range * range) {
               int posX = (int)(smoothPosX + (double)x - (double)(range / 2));
               int posZ = (int)(smoothPosZ + (double)z - (double)(range / 2));
               if ((int)Math.sqrt(((double)dx + 0.5) * ((double)dx + 0.5) + ((double)dz - 0.5) * ((double)dz - 0.5)) < range / 2) {
                  Multiset<MapColor> multiset = HashMultiset.create();
                  Chunk chunk = world.getChunkFromChunkCoords(posX >> 4, posZ >> 4);
                  if (chunk.isEmpty()) {
                     if (z >= 0 && dx * dx + dz * dz < range * range) {
                        this.colors[x + z * range] = 0;
                     }
                  } else {
                     int i3 = posX & 15;
                     int j3 = posZ & 15;
                     int k3 = 0;
                     double d1 = 0.0;
                     if (world.provider.getHasNoSky()) {
                        int l3 = posX + posZ * 231871;
                        l3 = l3 * l3 * 31287121 + l3 * 11;
                        if ((l3 >> 20 & 1) == 0) {
                           multiset.add(Blocks.DIRT.getMapColor(), 10);
                        } else {
                           multiset.add(Blocks.STONE.getMapColor(), 100);
                        }

                        d1 = 100.0;
                     } else {
                        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
                        int heightUP = chunk.getHeightValue(i3, j3) + 1;
                        IBlockState iblockstate = null;
                        if (heightUP > 1) {
                           do {
                              iblockstate = chunk.getBlockState(blockpos$mutableblockpos.setPos(i3, --heightUP, j3));
                           } while (iblockstate.getBlock().getMapColor() == MapColor.AIR && heightUP > 0);

                           if (heightUP > 0 && iblockstate.getMaterial().isLiquid()) {
                              while (heightUP > 0) {
                                 IBlockState state = chunk.getBlockState(i3, --heightUP, j3);
                                 k3++;
                                 if (!state.getMaterial().isLiquid()) {
                                    break;
                                 }
                              }
                           }
                        }

                        d1 += (double)heightUP;
                        MapColor mapColor = iblockstate == null ? MapColor.AIR : iblockstate.getBlock().getMapColor();
                        multiset.add(mapColor);
                     }

                     double d2 = (d1 - d0) * 4.0 / 4.0 + ((double)(x + z & 1) - 0.5) * 0.4;
                     int i5 = 1;
                     if (d2 > 0.6) {
                        i5 = 2;
                     }

                     if (d2 < -0.6) {
                        i5 = 0;
                     }

                     MapColor mapcolor = Iterables.getFirst(multiset, MapColor.AIR);
                     if (mapcolor == MapColor.WATER) {
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
                     if (z >= 0 && dx * dx + dz * dz < range * range) {
                        this.colors[x + z * range] = (byte)(mapcolor.colorIndex * 4 + i5);
                     }
                  }
               }
            }
         }
      }
   }

   public void updateTexture() {
      int range = this.getRange();

      for (int i = 0; i < range * range; i++) {
         int j = this.colors[i] & 255;
         if (j / 4 == 0) {
            this.texture.getTextureData()[i] = (i + i / range & 1) * 8 + 16 << 24;
         } else {
            MapColor mapColor = MapColor.COLORS[j / 4];
            int color = mapColor.getMapColor(j & 3);
            if (mapColor == MapColor.TNT) {
               color = Color.HSBtoRGB(0.05F, 1.0F, MathUtils.clamp(ColorUtils.getBrightnessFromColor(color) + (float)(j & 3) / 255.0F * 15.0F, 0.0F, 1.0F));
            }

            this.texture.getTextureData()[i] = color;
         }
      }

      this.texture.updateDynamicTexture();
   }

   public DynamicTexture getTexture() {
      return this.texture;
   }
}
