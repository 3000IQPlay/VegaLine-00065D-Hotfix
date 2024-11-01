package ru.govno.client.event.events;

import net.minecraft.network.Packet;
import ru.govno.client.event.Event;

public class EventReceivePacket
extends Event {
    public Packet packet;

    public EventReceivePacket(Packet packet) {
        this.packet = packet;
    }

    public Packet getPacket() {
        return this.packet;
    }

    public void setPacket(Packet packet) {
        this.packet = packet;
    }
}

