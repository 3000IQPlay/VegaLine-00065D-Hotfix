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
import ru.govno.client.module.settings.ModeSettings;

public class AntiBot extends Module {
   ModeSettings Modes;

   public AntiBot() {
      super("AntiBot", 0, Module.Category.COMBAT);
      this.settings.add(this.Modes = new ModeSettings("Modes", "Matrix", this, new String[]{"Matrix", "Matrix2", "WellMore", "Buzz"}));
   }

   @Override
   public String getDisplayName() {
      return this.getDisplayByMode(this.Modes.currentMode);
   }

   @Override
   public void onUpdate() {
      String mode = this.Modes.currentMode;
      boolean remove = true;
      if (mc.world != null && Minecraft.player != null) {
         try {
            mc.world
               .getLoadedEntityList()
               .stream()
               .map(Entity::getLivingBaseOf)
               .filter(Objects::nonNull)
               .filter(base -> this.entityIsBot(mode, base))
               .forEach(bot -> this.processingEntity(mode, bot, this.actived, true));
         } catch (Exception var4) {
            var4.printStackTrace();
            System.out.println(this.name + " module error!");
         }
      }
   }

   private boolean entityIsBot(String mode, Entity entity) {
      if (entity.getEntityId() != 462462998 && entity.getEntityId() != 462462999 && !entity.getName().toLowerCase().contains("npc")) {
         if (mode.equalsIgnoreCase("Matrix")) {
            return entity instanceof EntityOtherPlayerMP
               && Minecraft.player.getDistanceToEntity(entity) <= 25.0F
               && entity.noClip
               && entity.getCustomNameTag().isEmpty()
               && ((EntityOtherPlayerMP)entity).isSwingInProgress
               && entity != FreeCam.fakePlayer;
         } else if (mode.equalsIgnoreCase("Matrix2")) {
            if (!entity.getUniqueID().equals(UUID.nameUUIDFromBytes(("OfflinePlayer:" + entity.getName()).getBytes(StandardCharsets.UTF_8)))
               && entity instanceof EntityOtherPlayerMP MP
               && !MP.onGround) {
               return true;
            }

            return false;
         } else if (mode.equalsIgnoreCase("Wellmore")) {
            return entity instanceof EntityOtherPlayerMP && ((EntityOtherPlayerMP)entity).inventory.armorInventory.isEmpty();
         } else if (!mode.equalsIgnoreCase("Buzz")) {
            return false;
         } else {
            ArrayList<EntityZombie> bi4ariki = new ArrayList<>();
            ArrayList<EntityOtherPlayerMP> normPacani = new ArrayList<>();
            ArrayList<EntityZombie> bots = new ArrayList<>();

            for (Entity entities : mc.world.getLoadedEntityList()) {
               if (entities != null && entities instanceof EntityZombie zombie && zombie.isInvisible()) {
                  bi4ariki.add(zombie);
               }

               if (entities != null && entities instanceof EntityOtherPlayerMP entityOtherPlayerMP) {
                  normPacani.add(entityOtherPlayerMP);
               }
            }

            for (EntityOtherPlayerMP bro : normPacani) {
               for (EntityZombie bi4 : bi4ariki) {
                  if ((double)bi4.getDistanceToEntity(bro) < 2.2 && Minecraft.player.getDistanceToEntity(bi4) < 4.0F && bi4.ticksExisted < 400) {
                     bots.add(bi4);
                  }
               }
            }

            EntityZombie bot = bi4ariki.stream().findAny().orElse(null);
            return bot != null && entity == bot;
         }
      } else {
         return false;
      }
   }

   private void processingEntity(String mode, Entity entity, boolean isActive, boolean removeBot) {
      if (isActive) {
         if (mode.equalsIgnoreCase("Buzz")) {
            mc.getConnection().preSendPacket(new CPacketPlayer(Minecraft.player.onGround));
            mc.playerController.attackEntity(Minecraft.player, entity);
            Minecraft.player.swingArm(EnumHand.MAIN_HAND);
         }

         mc.world.removeEntityFromWorld(entity.getEntityId());
      }
   }
}
