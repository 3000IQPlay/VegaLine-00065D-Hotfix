package ru.govno.client.event.events;

import ru.govno.client.event.Event;

public class EventSetSneak
extends Event {
    public boolean sneak;

    public EventSetSneak(boolean sneak) {
    }

    public boolean isSneaking() {
        return this.sneak;
    }

    public void setSneaking(boolean sneak) {
        this.sneak = sneak;
    }
}

