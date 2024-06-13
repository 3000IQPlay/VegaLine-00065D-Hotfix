package ru.govno.client.event.events;

import net.minecraft.entity.Entity;
import ru.govno.client.event.Event;

public class EventAttackSilent extends Event {
   public final Entity targetEntity;

   public EventAttackSilent(Entity targetEntity) {
      this.targetEntity = targetEntity;
   }

   public Entity getTargetEntity() {
      return this.targetEntity;
   }
}
