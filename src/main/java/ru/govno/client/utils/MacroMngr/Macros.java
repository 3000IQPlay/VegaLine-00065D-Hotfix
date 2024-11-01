package ru.govno.client.utils.MacroMngr;

import net.minecraft.client.Minecraft;

public class Macros {
    public String name;
    public String massage;
    public int key;

    public String getName() {
        return this.name;
    }

    public String getMassage() {
        return this.massage;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMassage(String massage) {
        this.massage = massage;
    }

    public int getKey() {
        return this.key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public Macros(String name, int key, String massage) {
        this.massage = massage;
        this.key = key;
        this.name = name;
    }

    public void use() {
        Minecraft.player.sendChatMessage(this.massage);
    }
}

