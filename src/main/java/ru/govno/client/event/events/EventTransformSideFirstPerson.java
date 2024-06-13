package ru.govno.client.event.events;

import net.minecraft.util.EnumHandSide;
import ru.govno.client.event.Event;

public class EventTransformSideFirstPerson extends Event {
   private final EnumHandSide enumHandSide;

   public EventTransformSideFirstPerson(EnumHandSide enumHandSide) {
      this.enumHandSide = enumHandSide;
   }

   public EnumHandSide getEnumHandSide() {
      return this.enumHandSide;
   }
}
