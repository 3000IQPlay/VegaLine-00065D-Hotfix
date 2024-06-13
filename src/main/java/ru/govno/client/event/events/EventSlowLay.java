package ru.govno.client.event.events;

import ru.govno.client.event.Event;

public class EventSlowLay extends Event {
   public double slowFactor;

   public EventSlowLay(double slowFactor) {
      this.slowFactor = slowFactor;
   }

   public void setSlowFactor(double slowFactor) {
      this.slowFactor = slowFactor;
   }

   public double getSlowFactor() {
      return this.slowFactor;
   }
}