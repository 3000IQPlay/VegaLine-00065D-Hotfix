package ru.govno.client.utils.Command.impl;

import dev.intave.viamcp.fixes.AttackOrder;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import ru.govno.client.Client;
import ru.govno.client.utils.Command.Command;

public class Kick
extends Command {
    public Kick() {
        super("Kick", new String[]{"kick", "k"});
    }

    @Override
    public void onCommand(String[] args) {
        try {
            if (args[1].isEmpty()) {
                Client.msg("\u00a78\u00a7lKick:\u00a7r \u00a77\u041a\u043e\u043c\u043c\u0430\u043d\u0434\u0430 \u043d\u0430\u043f\u0438\u0441\u0430\u043d\u0430 \u043d\u0435\u0432\u0435\u0440\u043d\u043e.", false);
                Client.msg("\u00a78\u00a7lKick:\u00a7r \u00a77\u0434\u043e\u043f\u0438\u0448\u0438 \u0441\u043f\u043e\u0441\u043e\u0431 \u043a\u0438\u043a\u0430 (hit/bp/spam).", false);
                return;
            }
            if (args[1].equalsIgnoreCase("hit")) {
                if (Minecraft.getMinecraft().isSingleplayer()) {
                    Client.msg("\u00a78\u00a7lKick:\u00a7r \u00a77\u0432 \u043b\u043e\u043a\u0430\u043b\u044c\u043d\u043e\u043c \u043c\u0438\u0440\u0435 \u044d\u0442\u043e \u0438\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u0442\u044c \u043d\u0435\u0432\u043e\u0437\u043c\u043e\u0436\u043d\u043e.", false);
                    return;
                }
                for (int i = 0; i < 2; ++i) {
                    AttackOrder.sendFixedAttack((EntityPlayer)Minecraft.player, (Entity)Minecraft.player, (EnumHand)EnumHand.OFF_HAND);
                }
                Client.msg("\u00a78\u00a7lKick:\u00a7r \u00a77\u043f\u0440\u043e\u0438\u0437\u0432\u043e\u0436\u0443 \u043f\u043e\u043f\u044b\u0442\u043a\u0443 \u043a\u0438\u043d\u0443\u0442\u044c\u0441\u044f.", false);
                return;
            }
            if (args[1].equalsIgnoreCase("bp")) {
                for (int i = 0; i < 300; ++i) {
                    Minecraft.getMinecraft().getConnection().sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, BlockPos.ORIGIN, EnumFacing.UP));
                    Minecraft.getMinecraft().getConnection().sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, BlockPos.ORIGIN, EnumFacing.DOWN));
                }
                Client.msg("\u00a78\u00a7lKick:\u00a7r \u00a77\u043f\u0440\u043e\u0438\u0437\u0432\u043e\u0436\u0443 \u043f\u043e\u043f\u044b\u0442\u043a\u0443 \u043a\u0438\u043d\u0443\u0442\u044c\u0441\u044f.", false);
                return;
            }
            if (args[1].equalsIgnoreCase("spam")) {
                for (int i = 0; i < 1000; ++i) {
                    Minecraft.getMinecraft().getConnection().sendPacket(new CPacketChatMessage("0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"));
                }
                Client.msg("\u00a78\u00a7lKick:\u00a7r \u00a77\u043f\u0440\u043e\u0438\u0437\u0432\u043e\u0436\u0443 \u043f\u043e\u043f\u044b\u0442\u043a\u0443 \u043a\u0438\u043d\u0443\u0442\u044c\u0441\u044f.", false);
                return;
            }
            Client.msg("\u00a78\u00a7lKick:\u00a7r \u00a77\u041a\u043e\u043c\u043c\u0430\u043d\u0434\u0430 \u043d\u0430\u043f\u0438\u0441\u0430\u043d\u0430 \u043d\u0435\u0432\u0435\u0440\u043d\u043e.", false);
            Client.msg("\u00a78\u00a7lKick:\u00a7r \u00a77\u0432\u0438\u0434\u0438\u043c\u043e \u0432\u044b \u0434\u043e\u043f\u0443\u0441\u0442\u0438\u043b\u0438 \u043e\u0448\u0438\u0431\u043a\u0443 \u0432 \u043d\u0430\u0437\u0432\u0430\u043d\u0438\u0438 \u043c\u0435\u0442\u043e\u0434\u0430.", false);
        }
        catch (Exception formatException) {
            Client.msg("\u00a78\u00a7lKick:\u00a7r \u00a77\u041a\u043e\u043c\u043c\u0430\u043d\u0434\u0430 \u043d\u0430\u043f\u0438\u0441\u0430\u043d\u0430 \u043d\u0435\u0432\u0435\u0440\u043d\u043e.", false);
            Client.msg("\u00a78\u00a7lKick:\u00a7r \u00a77kick:kick/k [\u00a7lmethod\u00a7r\u00a77]", false);
            Client.msg("\u00a78\u00a7lKick:\u00a7r \u00a77methods: hit/bp/spam", false);
        }
    }
}

