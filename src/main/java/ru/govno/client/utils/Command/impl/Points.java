package ru.govno.client.utils.Command.impl;

import java.util.Iterator;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import ru.govno.client.Client;
import ru.govno.client.module.modules.PointTrace;
import ru.govno.client.utils.Command.Command;

public class Points extends Command {
   private static final Minecraft mc = Minecraft.getMinecraft();

   public Points() {
      super("Points", new String[]{"points", "point", "p", "way"});
   }

   public static boolean isNumeric(String str) {
      try {
         Double.parseDouble(str);
         return true;
      } catch (NumberFormatException var2) {
         return false;
      }
   }

   @Override
   public void onCommand(String[] args) {
      try {
         if (args[1].equalsIgnoreCase("del") || args[1].equalsIgnoreCase("remove")) {
            if (PointTrace.getPointList().size() == 0) {
               Client.msg("§3§lPoints:§r §7Список поинтов пуст.", false);
               return;
            }

            Iterator formatException = PointTrace.getPointList().iterator();
            if (formatException.hasNext()) {
               PointTrace point = (PointTrace)formatException.next();
               if (PointTrace.getPointByName(args[2]) != null) {
                  if (PointTrace.points.size() == 1) {
                     PointTrace.clearPoints();
                  } else {
                     PointTrace.removePoint(PointTrace.getPointByName(args[2]));
                  }

                  Client.msg("§3§lPoints:§r §7Поинт [§l" + args[2] + "§r§7] удалён.", false);
                  return;
               }

               Client.msg("§3§lPoints:§r §7Поинта с именем [§l" + args[2] + "§r§7] не существует.", false);
               return;
            }
         }

         if (args[1].equalsIgnoreCase("add") || args[1].equalsIgnoreCase("to") || args[1].equalsIgnoreCase("new")) {
            boolean canAdd = false;
            String pointName = "Point";
            float x = 0.0F;
            float y = 0.0F;
            float z = 0.0F;
            if (args.length == 4 && isNumeric(args[2]) && isNumeric(args[3])) {
               String sampleName = "Point";
               int sampledSize = PointTrace.getPointList()
                  .stream()
                  .filter(Objects::nonNull)
                  .map(PointTrace::getName)
                  .filter(name -> name.startsWith("Point"))
                  .toList()
                  .size();
               String num = PointTrace.getPointList().isEmpty() ? "" : String.valueOf(sampledSize + (sampledSize >= 1 ? 1 : 0));
               if (num.equalsIgnoreCase("0")) {
                  num = "";
               }

               pointName = "Point" + num;
               y = (float)Minecraft.player.posY;

               try {
                  x = Float.parseFloat(args[2]);
                  z = Float.parseFloat(args[3]);
               } catch (NumberFormatException var12) {
                  var12.printStackTrace();
               }

               canAdd = true;
            } else if (args.length == 5 && isNumeric(args[2]) && isNumeric(args[3]) && isNumeric(args[4])) {
               String sampleName = "Point";
               int sampledSize = PointTrace.getPointList()
                  .stream()
                  .filter(Objects::nonNull)
                  .map(PointTrace::getName)
                  .filter(name -> name.startsWith("Point"))
                  .toList()
                  .size();
               String num = PointTrace.getPointList().isEmpty() ? "" : String.valueOf(sampledSize + (sampledSize >= 1 ? 1 : 0));
               if (num.equalsIgnoreCase("0")) {
                  num = "";
               }

               pointName = "Point" + num;

               try {
                  x = Float.parseFloat(args[2]);
                  y = Float.parseFloat(args[3]);
                  z = Float.parseFloat(args[4]);
               } catch (NumberFormatException var11) {
                  var11.printStackTrace();
               }

               canAdd = true;
            } else if (args.length == 2) {
               String sampleName = "Point";
               int sampledSize = PointTrace.getPointList()
                  .stream()
                  .filter(Objects::nonNull)
                  .map(PointTrace::getName)
                  .filter(name -> name.startsWith("Point"))
                  .toList()
                  .size();
               String num = PointTrace.getPointList().isEmpty() ? "" : String.valueOf(sampledSize + (sampledSize >= 1 ? 1 : 0));
               if (num.equalsIgnoreCase("0")) {
                  num = "";
               }

               pointName = "Point" + num;
               x = (float)Minecraft.player.posX;
               y = (float)Minecraft.player.posY;
               z = (float)Minecraft.player.posZ;
               canAdd = true;
            } else if (args.length == 3) {
               String sampleName = String.valueOf(args[2]);
               int sampledSize = PointTrace.getPointList()
                  .stream()
                  .filter(Objects::nonNull)
                  .map(PointTrace::getName)
                  .filter(name -> name.startsWith(sampleName))
                  .toList()
                  .size();
               String num = PointTrace.getPointList().isEmpty() ? "" : String.valueOf(sampledSize + (sampledSize >= 1 ? 1 : 0));
               if (num.equalsIgnoreCase("0")) {
                  num = "";
               }

               pointName = sampleName + num;
               x = (float)Minecraft.player.posX;
               y = (float)Minecraft.player.posY;
               z = (float)Minecraft.player.posZ;
               canAdd = true;
            } else if (args.length == 5) {
               String sampleName = String.valueOf(args[2]);
               int sampledSize = PointTrace.getPointList()
                  .stream()
                  .filter(Objects::nonNull)
                  .map(PointTrace::getName)
                  .filter(name -> name.startsWith(sampleName))
                  .toList()
                  .size();
               String num = PointTrace.getPointList().isEmpty() ? "" : String.valueOf(sampledSize + (sampledSize >= 1 ? 1 : 0));
               if (num.equalsIgnoreCase("0")) {
                  num = "";
               }

               pointName = sampleName + num;
               x = Float.parseFloat(args[3]);
               y = (float)Minecraft.player.posY;
               z = Float.parseFloat(args[4]);
               canAdd = true;
            } else if (args.length == 6) {
               String sampleName = String.valueOf(args[2]);
               int sampledSize = PointTrace.getPointList()
                  .stream()
                  .filter(Objects::nonNull)
                  .map(PointTrace::getName)
                  .filter(name -> name.startsWith(sampleName))
                  .toList()
                  .size();
               String num = PointTrace.getPointList().isEmpty() ? "" : String.valueOf(sampledSize + (sampledSize >= 1 ? 1 : 0));
               if (num.equalsIgnoreCase("0")) {
                  num = "";
               }

               pointName = sampleName + num;
               x = Float.parseFloat(args[3]);
               y = Float.parseFloat(args[4]);
               z = Float.parseFloat(args[5]);
               canAdd = true;
            }

            if (canAdd) {
               PointTrace.addPoint(pointName, x, y, z);
               String xyz = "(X: " + (int)x + " ,Y: " + (int)y + " ,Z: " + (int)z + ")";
               Client.msg("§3§lPoints:§r §7Новый поинт ''" + pointName + "'' на " + xyz + ".", false);
               return;
            }

            Client.msg("§3§lPoints:§r §7Комманда написана неверно.", false);
            Client.msg("§3§lPoints:§r §7add: add/to/new [§lname+[x,y,z/x,z]/name/''§r§7]", false);
         }

         if (args[1].equalsIgnoreCase("ci") || args[1].equalsIgnoreCase("clear")) {
            if (PointTrace.getPointList().size() == 0) {
               Client.msg("§3§lPoints:§r §7Список поинтов пуст.", false);
               return;
            }

            PointTrace.clearPoints();
            Client.msg("§3§lPoints:§r §7Все поинты были удалены.", false);
            return;
         }

         if (args[1].equalsIgnoreCase("list") || args[1].equalsIgnoreCase("see")) {
            if (PointTrace.getPointList().size() == 0) {
               Client.msg("§3§lPoints:§r §7Список поинтов пуст.", false);
            } else {
               for (PointTrace point : PointTrace.getPointList()) {
                  String coords = "X:" + PointTrace.getX(point) + ", Y:" + PointTrace.getY(point) + ", Z:" + PointTrace.getZ(point);
                  String and = PointTrace.getPointList().indexOf(point) == PointTrace.getPointList().size() - 1 ? "." : ",";
                  Client.msg(
                     "§3§lPoints: §r§7№§l[" + (PointTrace.getPointList().indexOf(point) + 1) + "]§r§7: §l[" + point.name + "§r§7§l]§r§7" + coords + and, false
                  );
               }
            }
         }
      } catch (Exception var13) {
         Client.msg("§3§lPoints:§r §7Комманда написана неверно.", false);
         Client.msg("§3§lPoints:§r §7use points: points/point/p/way", false);
         Client.msg("§3§lPoints:§r §7add: add/to/new [§lname+[x,y,z/x,z]/name/[x,y,z/x,z]§r§7]", false);
         Client.msg("§3§lPoints:§r §7clear all: ci/clear", false);
         Client.msg("§3§lPoints:§r §7list: list/see", false);
         Client.msg("§3§lPoints:§r §7remove: del/remove [§lname §r§7]", false);
         var13.printStackTrace();
      }
   }
}
