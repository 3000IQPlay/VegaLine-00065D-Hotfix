package ru.govno.client.event.events;

import ru.govno.client.event.Event;

public class EventInput
extends Event {
    private int key;

    public EventInput(int key) {
        this.key = key;
    }

    public int getKey() {
        return this.key;
    }

    public void setKey(int key) {
        this.key = key;
    }
}

