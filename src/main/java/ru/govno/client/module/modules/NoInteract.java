package ru.govno.client.module.modules;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketUseEntity;
import ru.govno.client.event.EventTarget;
import ru.govno.client.event.events.EventSendPacket;
import ru.govno.client.module.Module;
import ru.govno.client.module.modules.HitAura;
import ru.govno.client.module.settings.BoolSettings;

public class NoInteract
extends Module {
    public static NoInteract get;
    public BoolSettings NoEntityInteract;
    public BoolSettings OnlyWithAura;

    public NoInteract() {
        super("NoInteract", 0, Module.Category.PLAYER);
        get = this;
        this.NoEntityInteract = new BoolSettings("NoEntityInteract", true, this);
        this.settings.add(this.NoEntityInteract);
        this.OnlyWithAura = new BoolSettings("OnlyWithAura", true, this);
        this.settings.add(this.OnlyWithAura);
    }

    @EventTarget
    public void onSendPacket(EventSendPacket event) {
        Packet packet = event.getPacket();
        if (packet instanceof CPacketUseEntity) {
            CPacketUseEntity cPacketUseEntity = (CPacketUseEntity)packet;
            if (this.NoEntityInteract.getBool() && (!this.OnlyWithAura.getBool() || HitAura.TARGET_ROTS != null) && NoInteract.mc.world != null && cPacketUseEntity.getEntityFromWorld(NoInteract.mc.world) instanceof EntityPlayer) {
                event.setCancelled(cPacketUseEntity.getAction() != CPacketUseEntity.Action.ATTACK);
            }
        }
    }
}

