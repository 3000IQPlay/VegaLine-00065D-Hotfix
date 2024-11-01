package ru.govno.client.module.modules;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.EnumHand;
import ru.govno.client.module.Module;
import ru.govno.client.module.modules.FreeCam;
import ru.govno.client.module.settings.ModeSettings;

public class AntiBot
extends Module {
    ModeSettings Modes = new ModeSettings("Modes", "Matrix", this, new String[]{"Matrix", "Matrix2", "WellMore", "Buzz"});

    public AntiBot() {
        super("AntiBot", 0, Module.Category.COMBAT);
        this.settings.add(this.Modes);
    }

    @Override
    public String getDisplayName() {
        return this.getDisplayByMode(this.Modes.currentMode);
    }

    @Override
    public void onUpdate() {
        String mode = this.Modes.currentMode;
        boolean remove = true;
        if (AntiBot.mc.world == null || Minecraft.player == null) {
            return;
        }
        try {
            AntiBot.mc.world.getLoadedEntityList().stream().map(Entity::getLivingBaseOf).filter(Objects::nonNull).filter(base -> this.entityIsBot(mode, (Entity)base)).forEach(bot -> this.processingEntity(mode, (Entity)bot, this.actived, true));
        }
        catch (Exception e) {
            e.printStackTrace();
            System.out.println(this.name + " module error!");
        }
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private boolean entityIsBot(String mode, Entity entity) {
        if (entity.getEntityId() == 462462998) return false;
        if (entity.getEntityId() == 462462999) return false;
        if (entity.getName().toLowerCase().contains("npc")) {
            return false;
        }
        if (mode.equalsIgnoreCase("Matrix")) {
            if (!(entity instanceof EntityOtherPlayerMP)) return false;
            if (!(Minecraft.player.getDistanceToEntity(entity) <= 25.0f)) return false;
            if (!entity.noClip) return false;
            if (!entity.getCustomNameTag().isEmpty()) return false;
            if (!((EntityOtherPlayerMP)entity).isSwingInProgress) return false;
            if (entity == FreeCam.fakePlayer) return false;
            return true;
        }
        if (mode.equalsIgnoreCase("Matrix2")) {
            if (entity.getUniqueID().equals(UUID.nameUUIDFromBytes(("OfflinePlayer:" + entity.getName()).getBytes(StandardCharsets.UTF_8)))) return false;
            if (!(entity instanceof EntityOtherPlayerMP)) return false;
            EntityOtherPlayerMP MP = (EntityOtherPlayerMP)entity;
            if (MP.onGround) return false;
            return true;
        }
        if (mode.equalsIgnoreCase("Wellmore")) {
            if (!(entity instanceof EntityOtherPlayerMP)) return false;
            if (!((EntityOtherPlayerMP)entity).inventory.armorInventory.isEmpty()) return false;
            return true;
        }
        if (!mode.equalsIgnoreCase("Buzz")) return false;
        ArrayList<EntityZombie> bi4ariki = new ArrayList<EntityZombie>();
        ArrayList<EntityOtherPlayerMP> normPacani = new ArrayList<EntityOtherPlayerMP>();
        ArrayList<EntityZombie> bots = new ArrayList<EntityZombie>();
        for (Entity entities : AntiBot.mc.world.getLoadedEntityList()) {
            EntityZombie zombie;
            if (entities != null && entities instanceof EntityZombie && (zombie = (EntityZombie)entities).isInvisible()) {
                bi4ariki.add(zombie);
            }
            if (entities == null || !(entities instanceof EntityOtherPlayerMP)) continue;
            EntityOtherPlayerMP entityOtherPlayerMP = (EntityOtherPlayerMP)entities;
            normPacani.add(entityOtherPlayerMP);
        }
        for (EntityOtherPlayerMP bro : normPacani) {
            for (EntityZombie bi4 : bi4ariki) {
                if (!((double)bi4.getDistanceToEntity(bro) < 2.2) || !(Minecraft.player.getDistanceToEntity(bi4) < 4.0f) || bi4.ticksExisted >= 400) continue;
                bots.add(bi4);
            }
        }
        EntityZombie bot = bi4ariki.stream().findAny().orElse(null);
        if (bot == null) return false;
        if (entity != bot) return false;
        return true;
    }

    private void processingEntity(String mode, Entity entity, boolean isActive, boolean removeBot) {
        if (isActive) {
            if (mode.equalsIgnoreCase("Buzz")) {
                mc.getConnection().preSendPacket(new CPacketPlayer(Minecraft.player.onGround));
                AntiBot.mc.playerController.attackEntity(Minecraft.player, entity);
                Minecraft.player.swingArm(EnumHand.MAIN_HAND);
            }
            AntiBot.mc.world.removeEntityFromWorld(entity.getEntityId());
        }
    }
}

