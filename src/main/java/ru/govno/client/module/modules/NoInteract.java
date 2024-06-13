package ru.govno.client.module.modules;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.CPacketUseEntity;
import ru.govno.client.event.EventTarget;
import ru.govno.client.event.events.EventSendPacket;
import ru.govno.client.module.Module;
import ru.govno.client.module.settings.BoolSettings;

public class NoInteract extends Module {
   public static NoInteract get;
   public BoolSettings NoEntityInteract;
   public BoolSettings OnlyWithAura;

   public NoInteract() {
      super("NoInteract", 0, Module.Category.PLAYER);
      get = this;
      this.settings.add(this.NoEntityInteract = new BoolSettings("NoEntityInteract", true, this));
      this.settings.add(this.OnlyWithAura = new BoolSettings("OnlyWithAura", true, this));
   }

   @EventTarget
   public void onSendPacket(EventSendPacket event) {
      if (event.getPacket() instanceof CPacketUseEntity cPacketUseEntity
         && this.NoEntityInteract.getBool()
         && (!this.OnlyWithAura.getBool() || HitAura.TARGET_ROTS != null)
         && mc.world != null
         && cPacketUseEntity.getEntityFromWorld(mc.world) instanceof EntityPlayer) {
         event.setCancelled(cPacketUseEntity.getAction() != CPacketUseEntity.Action.ATTACK);
      }
   }
}
