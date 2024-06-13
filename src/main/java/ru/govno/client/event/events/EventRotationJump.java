package ru.govno.client.event.events;

import ru.govno.client.event.Event;

public class EventRotationJump extends Event {
   private float yaw;

   public EventRotationJump(float yaw) {
      this.yaw = yaw;
   }

   public float getYaw() {
      return this.yaw;
   }

   public void setYaw(float yaw) {
      this.yaw = yaw;
   }
}
