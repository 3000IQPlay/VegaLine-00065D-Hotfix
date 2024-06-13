package ru.govno.client.event.events;

public interface Cancellable {
   boolean isCancelled();

   void setCancelled(boolean var1);
}
