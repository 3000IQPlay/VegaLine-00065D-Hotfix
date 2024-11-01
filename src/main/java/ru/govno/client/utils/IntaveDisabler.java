package ru.govno.client.utils;

import java.util.Arrays;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketTabComplete;
import net.minecraft.network.play.client.CPacketEntityAction.Action;
import net.minecraft.network.play.client.CPacketPlayer.Position;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.network.play.server.SPacketTabComplete;
import net.minecraft.util.math.BlockPos;
import ru.govno.client.Client;
import ru.govno.client.event.events.EventReceivePacket;
import ru.govno.client.event.events.EventSendPacket;

public class IntaveDisabler {
    private static final Minecraft mc = Minecraft.getMinecraft();
    public static boolean disableState;
    private static final ru.govno.client.utils.Math.TimerHelper checkReceiveTimer = new ru.govno.client.utils.Math.TimerHelper();
    private static int sitTicks;
    private static int motionTicks;
    private static boolean canCheckSitCommand;
    private static boolean sitCommandFound;

    private static String decoloredString(String toDecolor) {
        List<String> CHAR_SAMPLES = Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "c", "e", "a", "b", "d", "f", "r", "l", "k", "o", "m", "n");
        String formatChar = "§";

        for (String sample : CHAR_SAMPLES) {
            toDecolor = toDecolor.replace("§" + sample, "");
        }

        return toDecolor;
    }

    private static long getReceiveTimeout() {
        long ms = 450L;

        try {
            ms += (long)Minecraft.player.connection.getPlayerInfo(Minecraft.player.getUniqueID()).getResponseTime();
        } catch (Exception var3) {
            var3.printStackTrace();
        }

        return ms;
    }

    private static int getMinTicksCheck() {
        return 8;
    }

    private static String getStringAsMassive(String[] massiveString) {
        String string = "";

        for (String str : massiveString) {
            string = string + str;
        }

        return string;
    }

    public static void resetDisabler() {
        canCheckSitCommand = false;
        sitCommandFound = false;
        disableState = false;
        Minecraft.player.ridingEntity = null;
        sitTicks = 6;
        Minecraft.player.connection.sendPacket(new CPacketEntityAction(Minecraft.player, Action.START_SNEAKING));
        Minecraft.player.connection.sendPacket(new CPacketEntityAction(Minecraft.player, Action.STOP_SNEAKING));
    }

    public static void updateIntaveDisablerState(boolean disablerEnabled) {
        if (disablerEnabled) {
            double dx = 0.0;
            double dy = 0.0;
            double dz = 0.0;
            double vecSpeed = 0.0;
            if (Minecraft.player != null) {
                dx = Minecraft.player.posX - Minecraft.player.lastTickPosX;
                dy = Minecraft.player.posY - Minecraft.player.lastTickPosY;
                dz = Minecraft.player.posZ - Minecraft.player.lastTickPosZ;
                vecSpeed = Math.sqrt(dx * dx + dy * dy + dz * dz);
            }

            if (motionTicks > 0 && Minecraft.player.fallDistance > 0.0F) {
                Minecraft.player.ridingEntity = null;
                Minecraft.player.motionY = 0.16;
                motionTicks--;
            }

            if (Minecraft.player != null
                    && !mc.isSingleplayer()
                    && mc.getCurrentServerData() != null
                    && !Minecraft.player.getName().equalsIgnoreCase(Minecraft.player.getDisplayName().getUnformattedText())
                    && vecSpeed <= 1000.3
                    && Minecraft.player.ticksExisted >= getMinTicksCheck()) {
                if (!sitCommandFound && !canCheckSitCommand && Minecraft.player.onGround && Minecraft.player.ticksExisted >= getMinTicksCheck()) {
                    canCheckSitCommand = true;
                    mc.getConnection().sendPacket(new CPacketTabComplete("/l", BlockPos.ORIGIN, false));
                    checkReceiveTimer.reset();
                }

                if (sitCommandFound && !disableState && Minecraft.player.onGround) {
                    canCheckSitCommand = false;
                    Minecraft.player.sendChatMessage("/sit");
                    Minecraft.player.connection.sendPacket(new Position(Minecraft.player.posX, Minecraft.player.posY + 4.0, Minecraft.player.posZ, false));
                    motionTicks = 4;
                    disableState = true;
                    String msg = "§f§lModules:§r §7[§lBypass§r§7]: Intave скорее всего обезврежен.";
                    Client.msg(msg, false);
                    checkReceiveTimer.reset();
                }

                if (canCheckSitCommand && checkReceiveTimer.hasReached((double)getReceiveTimeout())) {
                    String msg = "§f§lModules:§r §7[§lBypass§r§7]: дисаблер IntaveMove не сработает,";
                    Client.msg(msg, false);
                    msg = "§f§lModules:§r §7[§lBypass§r§7]: возможно стоит перезайти на сервер.";
                    Client.msg(msg, false);
                    checkReceiveTimer.reset();
                    canCheckSitCommand = false;
                }

                if (Minecraft.player.isRiding() && disableState) {
                    Minecraft.player.connection.preSendPacket(new CPacketEntityAction(Minecraft.player, Action.START_SNEAKING));
                    Minecraft.player.connection.preSendPacket(new CPacketEntityAction(Minecraft.player, Action.STOP_SNEAKING));
                    Minecraft.player.ridingEntity = null;
                    sitTicks++;
                }
            } else {
                disableState = false;
                sitCommandFound = false;
                canCheckSitCommand = false;
                sitTicks = 0;
            }
        } else if (disableState || sitCommandFound) {
            if (disableState) {
                String msg = "§f§lModules:§r §7[§lBypass§r§7]: Intave дисаблер отключен.";
                Client.msg(msg, false);
                disableState = false;
                sitTicks = 0;
            }

            resetDisabler();
            sitCommandFound = false;
        }
    }

    public static void onReceivePackets(EventReceivePacket event) {
        if (event.getPacket() instanceof SPacketTabComplete completed && canCheckSitCommand && !sitCommandFound && completed.getMatches().length != 0) {
            List<String> outPut = Arrays.asList(getStringAsMassive(completed.getMatches()).split("/"))
                    .stream()
                    .filter(str -> str.length() > 0)
                    .map(str -> str.replace(":", "").replace(";", "").replace(" ", ""))
                    .toList();
            if (outPut.stream().anyMatch(out -> out.toLowerCase().endsWith("lay"))) {
                sitCommandFound = true;
                String msg = "§f§lModules:§r §7[§lBypass§r§7]: произвожу попытку обезвреживания Intave.";
                Client.msg(msg, false);
                canCheckSitCommand = false;
            }
        }

        if (event.getPacket() instanceof SPacketPlayerPosLook look && disableState) {
            if (Minecraft.player == null) {
                return;
            }

            double dx = Minecraft.player.posX - look.getX();
            double dy = Minecraft.player.posY - look.getY();
            double dz = Minecraft.player.posZ - look.getZ();
            if (Math.sqrt(dx * dx + dy * dy + dz * dz) <= 260.0) {
                return;
            }

            disableState = false;
            canCheckSitCommand = true;
        }

        if (event.getPacket() instanceof SPacketChat chat && disableState) {
            String msg = decoloredString(chat.getChatComponent().getUnformattedText()).toLowerCase().trim().replace(" ", "");
            if (msg.contains("неможетеиспользоватьэтукомандусейчас") || msg.contains("подожитенемногопередтем") || msg.contains("подождиеще1")) {
                disableState = false;
                sitCommandFound = false;
                canCheckSitCommand = true;
                msg = "§f§lModules:§r §7[§lBypass§r§7]: произвожу повтор обезвреживания Intave.";
                Client.msg(msg, false);
            }
        }
    }

    public static void onSendingPackets(EventSendPacket event) {
        if (event.getPacket() instanceof CPacketEntityAction MAction
                && disableState
                && (MAction.getAction() == Action.START_SNEAKING || MAction.getAction() == Action.STOP_SNEAKING)) {
            event.cancel();
        }
    }
}
