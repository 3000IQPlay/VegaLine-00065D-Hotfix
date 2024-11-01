package ru.govno.client.event.events;

import ru.govno.client.event.Event;

public class Event3D
extends Event {
    public final float partialTicks;

    public Event3D(float partialTicks) {
        this.partialTicks = partialTicks;
    }

    public float getPartialTicks() {
        return this.partialTicks;
    }
}

