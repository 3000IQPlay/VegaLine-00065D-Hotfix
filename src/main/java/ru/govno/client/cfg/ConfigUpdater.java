package ru.govno.client.cfg;

import com.google.gson.JsonObject;

public interface ConfigUpdater {
   JsonObject save();

   void load(JsonObject var1);
}
