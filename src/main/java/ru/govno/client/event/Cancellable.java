package ru.govno.client.event;

public interface Cancellable {
    public boolean isCancelled();

    public void setCancelled(boolean var1);
}

