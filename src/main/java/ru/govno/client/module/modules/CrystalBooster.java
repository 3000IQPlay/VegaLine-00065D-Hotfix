package ru.govno.client.module.modules;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.network.play.server.SPacketExplosion;
import net.minecraft.network.play.server.SPacketSpawnMob;
import net.minecraft.util.math.Vec3d;
import ru.govno.client.event.EventTarget;
import ru.govno.client.event.events.EventReceivePacket;
import ru.govno.client.module.Module;

public class CrystalBooster extends Module {
   private List<EntityEnderCrystal> NEARED_CRYSTALS = new ArrayList<>();

   public CrystalBooster() {
      super("CrystalBooster", 0, Module.Category.COMBAT);
   }

   private List<EntityEnderCrystal> getNearedCrystals(double maxRNG) {
      Entity entitySelf = (Entity)(FreeCam.get.isActived() && FreeCam.fakePlayer != null ? FreeCam.fakePlayer : Minecraft.player);
      return mc.world
         .getLoadedEntityList()
         .stream()
         .map(entity -> entity instanceof EntityEnderCrystal ? (EntityEnderCrystal)entity : null)
         .filter(crystal -> crystal != null && (maxRNG > 70.0 || (double)entitySelf.getDistanceToEntity(crystal) <= maxRNG))
         .collect(Collectors.toList());
   }

   private void addEntityEnderCrystal(EntityEnderCrystal crystal) {
      if (crystal != null && crystal.isEntityAlive()) {
         if (this.NEARED_CRYSTALS.stream().noneMatch(crystalAdded -> crystalAdded.getEntityId() == crystal.getEntityId())) {
            this.NEARED_CRYSTALS.add(crystal);
         }
      }
   }

   private boolean addCrystalIfToWorld(EntityEnderCrystal crystal, int entityId) {
      if (crystal != null && mc.world != null && !mc.world.getLoadedEntityList().stream().anyMatch(entity -> entity.getEntityId() == entityId)) {
         mc.world.addEntityToWorld(entityId, crystal);
         return true;
      } else {
         return false;
      }
   }

   private void onBlowup(Vec3d posIn, List<EntityEnderCrystal> crystals) {
      if (!crystals.isEmpty() && mc.world != null) {
         crystals.stream().filter(Entity::isEntityAlive).filter(crystal -> crystal.getDistanceToVec3d(posIn) < 6.0).forEach(crystal -> {
            crystal.setDead();
            mc.world.removeEntity(crystal);
         });
      }
   }

   @Override
   public void onUpdate() {
      this.NEARED_CRYSTALS = this.getNearedCrystals(16.0);
   }

   @EventTarget
   public void onReceivePackets(EventReceivePacket event) {
      if (this.isActived()) {
         if (event.getPacket() instanceof SPacketSpawnMob attach
            && EntityList.createEntityByID(attach.getEntityType(), mc.world) instanceof EntityEnderCrystal crystal
            && this.addCrystalIfToWorld(crystal, attach.getEntityID())) {
            this.addEntityEnderCrystal(crystal);
         }

         if (event.getPacket() instanceof SPacketExplosion explosion && explosion.getStrength() != 0.0F) {
            this.onBlowup(new Vec3d(explosion.posX, explosion.posY, explosion.posZ), this.NEARED_CRYSTALS);
         }
      }
   }
}
