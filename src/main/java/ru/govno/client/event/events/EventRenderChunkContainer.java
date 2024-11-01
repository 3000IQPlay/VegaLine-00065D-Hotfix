package ru.govno.client.event.events;

import net.minecraft.client.renderer.chunk.RenderChunk;
import ru.govno.client.event.Event;

public class EventRenderChunkContainer
extends Event {
    public RenderChunk renderChunk;

    public EventRenderChunkContainer(RenderChunk renderChunk) {
        this.renderChunk = renderChunk;
    }

    public RenderChunk getRenderChunk() {
        return this.renderChunk;
    }

    public void setRenderChunk(RenderChunk renderChunk) {
        this.renderChunk = renderChunk;
    }
}

