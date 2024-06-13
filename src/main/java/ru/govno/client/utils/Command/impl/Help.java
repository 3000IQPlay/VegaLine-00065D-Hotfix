package ru.govno.client.utils.Command.impl;

import net.minecraft.client.Minecraft;
import ru.govno.client.Client;
import ru.govno.client.utils.Command.Command;

// TODO: Translate

public class Help extends Command {
   private static final Minecraft mc = Minecraft.getMinecraft();

   public Help() {
      super("Help", new String[]{"help", "хелп", "помогите"});
   }

   @Override
   public void onCommand(String[] args) {
      if (args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("хелп") || args[0].equalsIgnoreCase("помогите")) {
         try {
            Client.msg("§6§lClient§r§7: Все комманды клиента §b§lVegaLine§r§7:", false);
            Client.msg("§9§lTeleport:§r §7teleport: teleport/tport/tp [§lName§r§7]", false);
            Client.msg("§9§lTeleport:§r §7teleport: teleport/tport/tp [§lx,y,z/x,z§r§7]", false);
            Client.msg("§e§lMotion:§r §7vmotion: vmotion/vm [§ly+§r§7].", false);
            Client.msg("§e§lMotion:§r §7hmotion: hmotion/hm [§lh+§r§7].", false);
            Client.msg("§3§lPoints:§r §7use points: points/point/p/way", false);
            Client.msg("§3§lPoints:§r §7add: add/to/new [§lname+[x,y,z/x,z]/name/''§r§7]", false);
            Client.msg("§3§lPoints:§r §7clear all: ci/clear", false);
            Client.msg("§3§lPoints:§r §7list: list/see", false);
            Client.msg("§3§lPoints:§r §7remove: del/remove [§lname§r§7]", false);
            Client.msg("§6§lMacros:§r§7 §7Комманды§r: §8[§7macros/macro/mc§8].", false);
            Client.msg("§6§lMacros:§r§7 §7Добавить§r: §8[§7add/new name key msg§8].", false);
            Client.msg("§6§lMacros:§r§7 §7Удалить§r: §8[§7remove/delete/del [name].", false);
            Client.msg("§6§lMacros:§r§7 §7Очистить§r: §8[§7clear all/ci§8].", false);
            Client.msg("§6§lMacros:§r§7 §7Список§r: §8[§7list/see§8].", false);
            Client.msg("§6§lMacros:§r§7 §7Тест§r: §8[§7use/test§8].", false);
            Client.msg("§e§lFriends:§r §7add: add/new [§lNAME§r§7]", false);
            Client.msg("§e§lFriends:§r §7remove: remove/del [§lNAME§r§7]", false);
            Client.msg("§e§lFriends:§r §7clear: clear/ci", false);
            Client.msg("§e§lFriends:§r §7list: list/see", false);
            Client.msg("§e§lFriends:§r §7massage: massage/msg/tell [§lTEXT / coords§r§7]", false);
            Client.msg("§b§lBinds:§r §7bind: bind/bnd/b [§lname§r§7] [§lkey§r§7].", false);
            Client.msg("§b§lBinds:§r §7unbind: unbind/unbnd [§lname§r§7 | §lall§r§7]", false);
            Client.msg("§b§lBinds:§r §7show binds: list/see", false);
            Client.msg("§b§lBinds:§r §7get bind: get [§lname§r§7]", false);
            Client.msg("§d§lClip:§r §7vclip: vclip/vc [§ly+§r§7]", false);
            Client.msg("§d§lClip:§r §7up/down/bd", false);
            Client.msg("§d§lClip:§r §7hclip: hclip/hc [§lh+§r§7]", false);
            Client.msg("§d§lClip:§r §7dclip: dclip/dc [§lv+,h+§r§7]", false);
            Client.msg("§f§lModules:§r §7использовать: ss/setsetting/module/m", false);
            Client.msg("§f§lModules:§r §7перезагрузить всё: reload", false);
            Client.msg("§f§lModules:§r §7включить:[§lNAME§r§7] true/on/+", false);
            Client.msg("§f§lModules:§r §7выключить:[§lNAME§r§7] false/off/-", false);
            Client.msg("§f§lModules:§r §7слайдер:[§lNAME§r§7] [§lSlider§r§7] [§lValue§r§7]", false);
            Client.msg("§f§lModules:§r §7чек:[§lNAME§r§7] [§lCheck§r§7] [§ltrue/+/off/-/toggle/tog/bind [key]/unbind§r§7]", false);
            Client.msg("§f§lModules:§r §7моды:[§lNAME§r§7] [§lModes§r§7] [§lSelected§r§7]", false);
            Client.msg("§f§lModules:§r §7цвет:[§lNAME§r§7] [§lColor§r§7] [§lrgba/rgb/ba/b/int§r§7]", false);
            Client.msg("§a§lConfigs:§r §7сохранить: save [§lNAME§r§7]", false);
            Client.msg("§a§lConfigs:§r §7загрузить: load [§lNAME§r§7]", false);
            Client.msg("§a§lConfigs:§r §7добавить: add/new [§lNAME§r§7]", false);
            Client.msg("§a§lConfigs:§r §7удалить: remove/del [§lNAME§r§7]", false);
            Client.msg("§a§lConfigs:§r §7показать все: list/see", false);
            Client.msg("§a§lConfigs:§r §7показать папку: dir/folder", false);
            Client.msg("§a§lConfigs:§r §7обнулить: reset/unload", false);
            Client.msg("§7§lLogin:§r §7login: login/l [§lNAME / random§r§7]", false);
            Client.msg("§7§lLogin:§r §7connect: connect/con [§lIP / IP + NAME§r§7]", false);
            Client.msg("§9§lRiding:§r §7use: entity/ent/riding/ride", false);
            Client.msg("§9§lRiding:§r §7desync: desync/des", false);
            Client.msg("§9§lRiding:§r §7resync: resync/res", false);
            Client.msg("§9§lRiding:§r §7dismount: dismount/dis", false);
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
            Client.msg("§e§lChat:§r §7use: ignore", false);
            Client.msg("§e§lChat:§r §7add: add/new [§lSTRING§r§7]", false);
            Client.msg("§e§lChat:§r §7remove: remove/del [§lSTRING§r§7]", false);
            Client.msg("§e§lChat:§r §7list: list/see", false);
            Client.msg("§e§lChat:§r §7clear: clear/ci", false);
            Client.msg("§2§lPanic:§r §7panic [§lon/code§r§7]", false);
            Client.msg("§f§lParse:§r §7write: write/get", false);
            Client.msg("§f§lParse:§r §7print: view/show", false);
            Client.msg("§f§lParse:§r §7open filder: dir/open", false);
            Client.msg("§5§lGetCommands:§r §7use: getcommands/getcom/gc", false);
            Client.msg("§8§lKick:§r §7kick:kick/k [§lmethod§r§7]", false);
            Client.msg("§8§lKick:§r §7methods: hit/bp/spam", false);
            Client.msg("§a§lServer:§r §7give ip: ip", false);
            Client.msg("§a§lServer:§r §7give online: online/onl", false);
            Client.msg("§a§lServer:§r §7give ping: ping/delay", false);
            Client.msg("§a§lServer:§r §7give version: version/ver", false);
         } catch (Exception var3) {
            Client.msg("Комманда написана неверно.", true);
            Client.msg("§7HELP: §f?§r§7 .help §f?§r", true);
            var3.printStackTrace();
         }
      }
   }
}
