package ru.govno.client.utils.Command.impl;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import ru.govno.client.Client;
import ru.govno.client.utils.Command.Command;

// TODO: Translate

public class WorldInfo extends Command {
   public WorldInfo() {
      super("WorldInfo", new String[]{"world", "w"});
   }

   Minecraft mc() {
      return Minecraft.getMinecraft();
   }

   @Override
   public void onCommand(String[] args) {
      try {
         if (this.mc().world == null) {
            Client.msg("§b§lWorldInfo:§r §7world is null", false);
         } else {
            if (args[1].equalsIgnoreCase("mapsize") || args[1].equalsIgnoreCase("mps") || args[1].equalsIgnoreCase("size")) {
               int size = -1010011;
               if (this.mc().world.getWorldBorder() != null) {
                  size = this.mc().world.getWorldBorder().getSize();
               }

               if (size == -1010011) {
                  Client.msg("§b§lWorldInfo:§r §7map border is null" + size, false);
               } else {
                  Client.msg("§b§lWorldInfo:§r §7map size: " + size, false);
               }
            }

            if (args[1].equalsIgnoreCase("biome") || args[1].equalsIgnoreCase("bm")) {
               WorldClient var10000 = this.mc().world;
               this.mc();
               if (var10000.getBiome(Minecraft.player.getPosition().down()) == null) {
                  Client.msg("§b§lWorldInfo:§r §7biome is null", false);
               } else {
                  var10000 = this.mc().world;
                  this.mc();
                  Client.msg("§b§lWorldInfo:§r §7biome is " + var10000.getBiome(Minecraft.player.getPosition().down()).getBiomeName(), false);
               }
            }

            if (args[1].equalsIgnoreCase("rules") || args[1].equalsIgnoreCase("rs")) {
               Client.msg("§b§lWorldInfo:§r §7world rules list:", false);
               int number = 0;

               for (String rule : this.mc().world.getGameRules().getRules()) {
                  boolean ruleIsTrue = this.mc().world.getGameRules().getBoolean(rule);
                  Client.msg("§b§lWorldInfo:§r §7rule №" + ++number + ": " + rule + " = " + ruleIsTrue, false);
               }
            }

            if (args[1].equalsIgnoreCase("difficulty") || args[1].equalsIgnoreCase("dif")) {
               Client.msg("§b§lWorldInfo:§r §7world difficulty is " + this.mc().world.getDifficulty().name(), false);
            }

            if (args[1].equalsIgnoreCase("seed") || args[1].equalsIgnoreCase("s")) {
               long seed = this.mc().world.getSeed();
               if (seed == 0L) {
                  Client.msg("§b§lWorldInfo:§r §7world seed protected", false);
               } else {
                  Client.msg("§b§lWorldInfo:§r §7world seed is " + seed, false);
               }
            }

            if (args[1].equalsIgnoreCase("height") || args[1].equalsIgnoreCase("h")) {
               Client.msg("§b§lWorldInfo:§r §7world height is " + this.mc().world.getHeight(), false);
            }

            if (args[1].equalsIgnoreCase("spawnpoint") || args[1].equalsIgnoreCase("sp")) {
               if (this.mc().world.getSpawnPoint() == null) {
                  Client.msg("§b§lWorldInfo:§r §7spawnpoint is null", false);
               } else {
                  Client.msg(
                     "§b§lWorldInfo:§r §7spawnpoint coords: X="
                        + this.mc().world.getSpawnPoint().getX()
                        + " ,Y="
                        + this.mc().world.getSpawnPoint().getY()
                        + " ,Z="
                        + this.mc().world.getSpawnPoint().getZ(),
                     false
                  );
               }
            }

            if (args[1].equalsIgnoreCase("enities") || args[1].equalsIgnoreCase("ents")) {
               if (this.mc().world.getLoadedEntityList() == null) {
                  Client.msg("§b§lWorldInfo:§r §7enities in world list is null", false);
               } else {
                  Client.msg("§b§lWorldInfo:§r §7enities in world list:", false);
                  int entInt = 0;

                  for (Entity entity : this.mc().world.getLoadedEntityList()) {
                     entInt++;
                     String hp = "";
                     if (entity instanceof EntityLivingBase) {
                        hp = " §r§a§nHP:" + ((EntityLivingBase)entity).getHealth();
                     }

                     Client.msg("§b§lWorldInfo:§r §7obj №" + entInt + ": " + entity.getDisplayName().getUnformattedText() + hp, false);
                  }
               }
            }

            if (args[1].equalsIgnoreCase("gc")) {
               System.gc();
               Client.msg("§b§lWorldInfo:§r §7system memory cleared sucessfully", false);
            }

            if (args[1].equalsIgnoreCase("type") || args[1].equalsIgnoreCase("t")) {
               String type = null;
               if (this.mc().world.getWorldType() != null) {
                  type = this.mc().world.getWorldType().getWorldTypeName();
                  type = type.substring(0, 1).toUpperCase() + type.substring(1, type.length());
               }

               if (type == null) {
                  Client.msg("§b§lWorldInfo:§r §7not has world type info", false);
               } else {
                  Client.msg("§b§lWorldInfo:§r §7world type is: " + type, false);
               }
            }
         }
      } catch (Exception var8) {
         Client.msg("§b§lWorldInfo:§r §7Комманда написана неверно.", false);
         Client.msg("§b§lWorldInfo:§r §7use: world/w", false);
         Client.msg("§b§lWorldInfo:§r §7border size: mapsize/mps/size", false);
         Client.msg("§b§lWorldInfo:§r §7biome: biome/bm", false);
         Client.msg("§b§lWorldInfo:§r §7rules: rules/rs", false);
         Client.msg("§b§lWorldInfo:§r §7difficulty: difficulty/dif", false);
         Client.msg("§b§lWorldInfo:§r §7seed: seed/s", false);
         Client.msg("§b§lWorldInfo:§r §7height: height/h", false);
         Client.msg("§b§lWorldInfo:§r §7spawnpoint: spawnpoint/sp", false);
         Client.msg("§b§lWorldInfo:§r §7entities: enities/ents", false);
         Client.msg("§b§lWorldInfo:§r §7system gc: gc", false);
         Client.msg("§b§lWorldInfo:§r §7world type: type/t", false);
      }
   }
}
