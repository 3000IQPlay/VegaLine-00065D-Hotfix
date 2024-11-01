package ru.govno.client.event.events;

public interface Cancellable {
    public boolean isCancelled();

    public void setCancelled(boolean var1);
}

