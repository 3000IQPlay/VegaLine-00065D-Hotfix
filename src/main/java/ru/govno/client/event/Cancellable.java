package ru.govno.client.event;

public interface Cancellable {
   boolean isCancelled();

   void setCancelled(boolean var1);
}
