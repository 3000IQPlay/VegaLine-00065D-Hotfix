package ru.govno.client.cfg;

import com.google.gson.JsonObject;

public interface ConfigUpdater {
    public JsonObject save();

    public boolean load(JsonObject var1);
}

