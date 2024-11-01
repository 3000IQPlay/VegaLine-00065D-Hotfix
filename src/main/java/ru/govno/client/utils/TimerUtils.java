package ru.govno.client.utils;

public class TimerUtils {
    private long lastMS;

    private long getCurrentMS() {
        return System.nanoTime() / 1000000L;
    }

    public boolean hasReached(double milliseconds) {
        return (double)(this.getCurrentMS() - this.lastMS) >= milliseconds;
    }

    public void reset() {
        this.lastMS = this.getCurrentMS();
    }

    public void setTime(long time) {
        this.lastMS = time;
    }

    public long getTime() {
        return this.getCurrentMS() - this.lastMS;
    }
}

