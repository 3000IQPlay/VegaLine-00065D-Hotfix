package ru.govno.client.event.events;

import ru.govno.client.event.Event;

public class EventMoveKeys
extends Event {
    boolean forwardKeyDown;
    boolean backKeyDown;
    boolean leftKeyDown;
    boolean rightKeyDown;

    public EventMoveKeys(boolean forwardKeyDown, boolean backKeyDown, boolean leftKeyDown, boolean rightKeyDown) {
        this.forwardKeyDown = forwardKeyDown;
        this.backKeyDown = backKeyDown;
        this.leftKeyDown = leftKeyDown;
        this.rightKeyDown = rightKeyDown;
    }

    public void setForwardKeyDown(boolean forwardKeyDown) {
        this.forwardKeyDown = forwardKeyDown;
    }

    public void setBackKeyDown(boolean backKeyDown) {
        this.backKeyDown = backKeyDown;
    }

    public void setLeftKeyDown(boolean leftKeyDown) {
        this.leftKeyDown = leftKeyDown;
    }

    public void setRightKeyDown(boolean rightKeyDown) {
        this.rightKeyDown = rightKeyDown;
    }

    public boolean isForwardKeyDown() {
        return this.forwardKeyDown;
    }

    public boolean isBackKeyDown() {
        return this.backKeyDown;
    }

    public boolean isLeftKeyDown() {
        return this.leftKeyDown;
    }

    public boolean isRightKeyDown() {
        return this.rightKeyDown;
    }
}

