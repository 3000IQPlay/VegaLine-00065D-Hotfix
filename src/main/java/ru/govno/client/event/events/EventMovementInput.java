package ru.govno.client.event.events;

import ru.govno.client.event.Event;

public class EventMovementInput
extends Event {
    private float forward;
    private float strafe;
    private boolean jump;
    private final boolean sneaking;
    private double sneakSlowDownMultiplier;

    public float getForward() {
        return this.forward;
    }

    public void setForward(float forward) {
        this.forward = forward;
    }

    public float getStrafe() {
        return this.strafe;
    }

    public void setStrafe(float strafe) {
        this.strafe = strafe;
    }

    public boolean isJump() {
        return this.jump;
    }

    public void setJump(boolean jump) {
        this.jump = jump;
    }

    public double getSneakSlowDownMultiplier() {
        return this.sneakSlowDownMultiplier;
    }

    public void setSneakSlowDownMultiplier(double sneakSlowDownMultiplier) {
        this.sneakSlowDownMultiplier = sneakSlowDownMultiplier;
    }

    public EventMovementInput(float forward, float strafe, boolean jump, boolean sneak, double sneakSlowDownMultiplier) {
        this.forward = forward;
        this.strafe = strafe;
        this.jump = jump;
        this.sneaking = sneak;
        this.sneakSlowDownMultiplier = sneakSlowDownMultiplier;
    }
}

