package ru.govno.client.module.modules;

import net.minecraft.client.Minecraft;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import ru.govno.client.event.EventTarget;
import ru.govno.client.event.events.EventSendPacket;
import ru.govno.client.module.Module;
import ru.govno.client.module.settings.BoolSettings;

public class PushAttack
extends Module {
    public static Module get;
    BoolSettings NCPBypass = new BoolSettings("NCPBypass", false, this);

    public PushAttack() {
        super("PushAttack", 0, Module.Category.COMBAT);
        this.settings.add(this.NCPBypass);
        get = this;
    }

    @Override
    public void onMouseClick(int mouseButton) {
        if (mouseButton == 0 && Minecraft.player != null && Minecraft.player.isHandActive() && PushAttack.mc.currentScreen == null && (PushAttack.mc.objectMouseOver == null || PushAttack.mc.objectMouseOver.typeOfHit != RayTraceResult.Type.BLOCK)) {
            mc.clickMouse();
        }
    }

    @EventTarget
    public void onPacket(EventSendPacket event) {
        CPacketUseEntity useEntity;
        Packet packet;
        if (this.isActived() && (packet = event.getPacket()) instanceof CPacketUseEntity && (useEntity = (CPacketUseEntity)packet).getEntityFromWorld(PushAttack.mc.world) != null && useEntity.getAction() == CPacketUseEntity.Action.ATTACK && this.NCPBypass.getBool()) {
            Minecraft.getMinecraft().getConnection().preSendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.RELEASE_USE_ITEM, new BlockPos(-1, -1, -1), EnumFacing.DOWN));
        }
    }
}

