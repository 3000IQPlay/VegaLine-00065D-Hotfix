package ru.govno.client.ui.login;

import com.mojang.authlib.Agent;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;
import java.net.Proxy;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;
import net.minecraft.util.text.TextFormatting;

public final class AltLoginThread
extends Thread {
    private final String password;
    private String status;
    private final String username;
    private final Minecraft mc = Minecraft.getMinecraft();

    public AltLoginThread(String username, String password) {
        super("Alt Login Thread");
        this.username = username;
        this.password = password;
        this.status = TextFormatting.GRAY + "Waiting...";
    }

    public Session createSession(String username, String password) {
        YggdrasilAuthenticationService service = new YggdrasilAuthenticationService(Proxy.NO_PROXY, "");
        YggdrasilUserAuthentication auth = (YggdrasilUserAuthentication)service.createUserAuthentication(Agent.MINECRAFT);
        auth.setUsername(username);
        auth.setPassword(password);
        try {
            auth.logIn();
            return new Session(auth.getSelectedProfile().getName(), auth.getSelectedProfile().getId().toString(), auth.getAuthenticatedToken(), password.isEmpty() ? "legacy" : "mojang");
        }
        catch (AuthenticationException localAuthenticationException) {
            localAuthenticationException.printStackTrace();
            return null;
        }
    }

    public String getStatus() {
        return this.status;
    }

    @Override
    public void run() {
        if (this.password.equals("")) {
            this.mc.session = new Session(this.username, "", "", "mojang");
            this.mc.sessionSaver.save();
            this.status = TextFormatting.GREEN + "Logged in. (" + this.username + " - offline name)";
            return;
        }
        this.status = TextFormatting.YELLOW + "Logging in...";
        Session auth = this.createSession(this.username, this.password);
        if (auth == null) {
            this.status = TextFormatting.RED + "Login failed!";
        } else {
            this.status = TextFormatting.GREEN + "Logged in. (" + auth.getUsername() + ")";
            this.mc.session = auth;
            this.mc.sessionSaver.save();
        }
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

