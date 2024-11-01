package ru.govno.client.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;

public class SessionSaver {
    private final Minecraft mc;
    private boolean singleLoaded;
    private final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final File saver;

    public SessionSaver(Minecraft mc) {
        this.saver = new File(new File(Minecraft.getMinecraft().mcDataDir, "\\saves\\configurations"), "usercache.sav");
        this.mc = mc;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void save() {
        try {
            JsonObject jsonObject = new JsonObject();
            Session mcSession = this.mc.getSession();
            if (mcSession == null) {
                return;
            }
            jsonObject.addProperty("sessionId", mcSession.getSessionID());
            jsonObject.addProperty("userName", mcSession.getUsername());
            jsonObject.addProperty("token", mcSession.getToken());
            jsonObject.addProperty("playerId", mcSession.getPlayerID());
            jsonObject.addProperty("loginType", mcSession.sessionType.name());
            FileWriter fileWriter = new FileWriter(this.saver);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            this.GSON.toJson((JsonElement)jsonObject, (Appendable)bufferedWriter);
            bufferedWriter.flush();
            bufferedWriter.close();
            fileWriter.flush();
            fileWriter.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            System.out.println("Session " + this.mc.session.getUsername() + " was saved");
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void load() {
        if (!this.singleLoaded) {
            try {
                FileReader fileReader = new FileReader(this.saver);
                BufferedReader bufferedReader = new BufferedReader(fileReader);
                JsonObject jsonObject = (JsonObject)this.GSON.fromJson((Reader)bufferedReader, JsonObject.class);
                bufferedReader.close();
                fileReader.close();
                if (jsonObject == null) {
                    return;
                }
                String[] args = new String[5];
                if (jsonObject.has("sessionId")) {
                    args[0] = jsonObject.get("sessionId").getAsString();
                }
                if (jsonObject.has("userName")) {
                    args[1] = jsonObject.get("userName").getAsString();
                }
                if (jsonObject.has("token")) {
                    args[2] = jsonObject.get("token").getAsString();
                }
                if (jsonObject.has("playerId")) {
                    args[3] = jsonObject.get("playerId").getAsString();
                }
                if (jsonObject.has("loginType")) {
                    args[4] = jsonObject.get("loginType").getAsString();
                }
                this.mc.session = new Session(args[1], args[3], args[2], args[4]);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                System.out.println("Relogin as name " + this.mc.session.getUsername());
            }
            this.singleLoaded = true;
        }
    }
}

