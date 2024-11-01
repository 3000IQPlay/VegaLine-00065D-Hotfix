package ru.govno.client.utils.Command.impl;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketEntityAction.Action;
import net.minecraft.network.play.client.CPacketPlayer.Position;
import net.minecraft.util.math.AxisAlignedBB;
import ru.govno.client.Client;
import ru.govno.client.module.modules.Bypass;
import ru.govno.client.utils.Command.Command;
import ru.govno.client.utils.Math.MathUtils;

public class Teleport extends Command {
    private static final Minecraft mc = Minecraft.getMinecraft();

    public Teleport() {
        super("Teleport", new String[]{"teleport", "tport", "tp"});
    }

    @Override
    public void onCommand(String[] args) {
        try {
            String ip = "";
            if (!mc.isSingleplayer()) {
                ip = mc.getCurrentServerData().serverIP;
            }

            if (args.length == 2) {
                if (args[0].equalsIgnoreCase("teleport") || args[0].equalsIgnoreCase("tport") || args[0].equalsIgnoreCase("tp")) {
                    boolean tp = false;

                    for (EntityPlayer entity : mc.world.playerEntities) {
                        if (entity.getName().equalsIgnoreCase(args[1])) {
                            double xe = entity.posX;
                            double ye = entity.posY;
                            double ze = entity.posZ;
                            if (!ip.equalsIgnoreCase("mc.reallyworld.ru")
                                    && (!Bypass.get.isActived() || !Bypass.get.VulcanStrafe.getBool() || !Bypass.get.getIsStrafeHacked())) {
                                if (ip.equalsIgnoreCase("mc.mstnw.net")) {
                                    Minecraft.player.connection.sendPacket(new CPacketPlayer(false));
                                    Minecraft.player.setPositionAndUpdate(xe, ye, ze);
                                    Client.msg("§9§lTeleport:§r §7Вы тепнулись к [§l" + args[1] + "§r§7].", false);
                                    tp = true;
                                } else {
                                    double dY = (double)((int)Math.abs(ye - Minecraft.player.posY));
                                    double dx = Math.abs(xe - Minecraft.player.posX);
                                    double dz = Math.abs(ze - Minecraft.player.posZ);
                                    double dzx = Math.sqrt(dx * dx + dz * dz);
                                    double maxXZSpeed = Minecraft.player.capabilities.isFlying ? 1.2 : 0.25;
                                    double maxYSpeed = 10.0;
                                    int xzCrate = (int)(dzx / maxXZSpeed + 1.0);
                                    int yCrate = (int)(dY / maxYSpeed);
                                    double mw = (double)Minecraft.player.width;
                                    double mh = (double)Minecraft.player.height;
                                    AxisAlignedBB aabb = null;
                                    boolean xzIsFirst = false;

                                    for (int indexXZ = 0; indexXZ < xzCrate; indexXZ++) {
                                        float circlePC = (float)indexXZ / (float)xzCrate;
                                        double x = MathUtils.lerp(Minecraft.player.posX, xe, (double)circlePC);
                                        double z = MathUtils.lerp(Minecraft.player.posZ, ze, (double)circlePC);
                                        double y = Minecraft.player.posY;
                                        aabb = new AxisAlignedBB(x - mw / 2.0, y, z - mw / 2.0, x + mw / 2.0, y + mh, z + mw / 2.0);
                                        if (!mc.world.getCollisionBoxes(Minecraft.player, aabb).isEmpty()) {
                                            xzIsFirst = true;
                                            break;
                                        }
                                    }

                                    if (xzIsFirst) {
                                        for (int indexXZx = 0; indexXZx < xzCrate; indexXZx++) {
                                            float circlePC = (float)indexXZx / (float)xzCrate;
                                            double x = MathUtils.lerp(Minecraft.player.posX, xe, (double)circlePC);
                                            double z = MathUtils.lerp(Minecraft.player.posZ, ze, (double)circlePC);
                                            double y = Minecraft.player.posY;
                                            Minecraft.player.connection.sendPacket(new Position(x, y, z, Minecraft.player.onGround));
                                        }

                                        for (int indexY = 0; indexY < yCrate; indexY++) {
                                            float circlePC = (float)indexY / (float)yCrate;
                                            double y = MathUtils.lerp(Minecraft.player.posY, ye, (double)circlePC);
                                            Minecraft.player.connection.sendPacket(new Position(xe, y, ze, Minecraft.player.onGround));
                                        }
                                    } else {
                                        for (int indexY = 0; indexY < yCrate; indexY++) {
                                            float circlePC = (float)indexY / (float)yCrate;
                                            double y = MathUtils.lerp(Minecraft.player.posY, ye, (double)circlePC);
                                            Minecraft.player.connection.sendPacket(new Position(xe, y, ze, Minecraft.player.onGround));
                                        }

                                        for (int indexXZx = 0; indexXZx < xzCrate; indexXZx++) {
                                            float circlePC = (float)indexXZx / (float)xzCrate;
                                            double x = MathUtils.lerp(Minecraft.player.posX, xe, (double)circlePC);
                                            double z = MathUtils.lerp(Minecraft.player.posZ, ze, (double)circlePC);
                                            double y = Minecraft.player.posY;
                                            Minecraft.player.connection.sendPacket(new Position(x, y, z, Minecraft.player.onGround));
                                        }
                                    }

                                    Minecraft.player.setPositionAndUpdate(xe, ye, ze);
                                }
                            } else {
                                Minecraft.player.connection.sendPacket(new CPacketEntityAction(Minecraft.player, Action.START_SNEAKING));
                                Minecraft.player.connection.sendPacket(new Position(xe, ye - 1.0, ze, false));
                                Minecraft.player.connection.sendPacket(new Position(xe, ye, ze, false));
                                Minecraft.player.connection.sendPacket(new Position(xe, 1.0, ze, false));
                                Minecraft.player.connection.sendPacket(new Position(xe, ye, ze, false));
                                Minecraft.player.connection.sendPacket(new Position(xe, ye + 0.42, ze, true));
                                Minecraft.player.connection.sendPacket(new Position(xe, ye, ze, false));
                                Minecraft.player.connection.sendPacket(new CPacketEntityAction(Minecraft.player, Action.STOP_SNEAKING));
                                Client.msg("§9§lTeleport:§r §7Вы тепнулись к [§l" + args[1] + "§r§7].", false);
                                tp = true;
                            }

                            Client.msg("§9§lTeleport:§r §7Вы тепнулись к [§l" + args[1] + "§r§7].", false);
                            tp = true;
                        }
                    }

                    if (!tp) {
                        Client.msg("§9§lTeleport:§r §7Игрока с ником [§l" + args[1] + "§r§7] нет в мире.", false);
                    }
                }
            } else if (args[0].equalsIgnoreCase("teleport") || args[0].equalsIgnoreCase("tport") || args[0].equalsIgnoreCase("tp")) {
                boolean xyz = args.length == 4;
                float xs = (float)Integer.valueOf(args[1]).intValue() + 0.5F;
                float ys = (float)(xyz ? Integer.valueOf(args[2]) : (int)Minecraft.player.posY);
                float zs = (float)(xyz ? Integer.valueOf(args[3]) : Integer.valueOf(args[2])).intValue() + 0.5F;
                if (ip.equalsIgnoreCase("mc.reallyworld.ru")) {
                }

                Client.msg("§9§lTeleport:§r §7Вы тепнулись на [§lx: " + (int)xs + ",y: " + (int)ys + ",z: " + (int)zs + "§r§7]", false);
                if (!ip.equalsIgnoreCase("mc.reallyworld.ru")) {
                    mc.renderGlobal.loadRenderers();
                }
            }
        } catch (Exception var40) {
            Client.msg("Комманда написана неверно.", false);
            Client.msg("§9§lTeleport:§r §7Комманда написана неверно.", false);
            Client.msg("§9§lTeleport:§r §7teleport: teleport/tport/tp [§lName§r§7]", false);
            Client.msg("§9§lTeleport:§r §7teleport: teleport/tport/tp [§lx,y,z/x,z§r§7]", false);
            var40.printStackTrace();
        }
    }
}
