package ru.govno.client.module.modules;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.network.play.client.CPacketUseEntity.Action;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import optifine.Config;
import org.lwjgl.opengl.GL11;
import ru.govno.client.event.EventTarget;
import ru.govno.client.event.events.EventSendPacket;
import ru.govno.client.module.Module;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.utils.CrystalField;
import ru.govno.client.utils.MusicHelper;
import ru.govno.client.utils.Combat.RotationUtil;
import ru.govno.client.utils.Math.MathUtils;
import ru.govno.client.utils.Math.TimerHelper;
import ru.govno.client.utils.Render.AnimationUtils;
import ru.govno.client.utils.Render.ColorUtils;
import ru.govno.client.utils.Render.RenderUtils;

public class FragEffects extends Module {
   private final BoolSettings NoMobsDetect;
   public final AnimationUtils strikeAnimation = new AnimationUtils(0.0F, 0.0F, 0.03F);
   public static FragEffects get;
   private boolean timerForce2;
   private ResourceLocation LIGHT_VIGNETTE_TEX = new ResourceLocation("vegaline/modules/frageffects/vignette.png");
   private EntityLivingBase lastDeceasedBase;
   private final List<FragEffects.EntityDeathMemory> DEATH_MEMORIES_LIST = new ArrayList<>();
   public boolean hasLoadedDesaturate;
   private boolean texHasLoaded;
   private final List<FragEffects.FragParticle> fragParticles = new ArrayList<>();

   public FragEffects() {
      super("FragEffects", 0, Module.Category.RENDER);
      this.settings.add(this.NoMobsDetect = new BoolSettings("NoMobsDetect", true, this));

      try {
         DynamicTexture dynamicTexture = new DynamicTexture(
            TextureUtil.readBufferedImage(Minecraft.getMinecraft().getResourceManager().getResource(this.LIGHT_VIGNETTE_TEX).getInputStream())
         );
         dynamicTexture.setBlurMipmap(true, false);
         this.LIGHT_VIGNETTE_TEX = Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation(dynamicTexture.toString(), dynamicTexture);
      } catch (Exception var2) {
         var2.fillInStackTrace();
      }

      mc.getTextureManager().bindTexture(this.LIGHT_VIGNETTE_TEX);
      get = this;
   }

   private float updateStrikeAnimation() {
      float value = 0.0F;
      if (this.strikeAnimation.to == 1.0F) {
         this.strikeAnimation.speed = 0.07F;
         if (!Config.isShaders()) {
            mc.gameSettings.anaglyph = true;
         }

         if ((value = this.strikeAnimation.getAnim()) > 0.998F) {
            this.strikeAnimation.setAnim(1.0F);
            this.strikeAnimation.to = 0.0F;
            if (this.lastDeceasedBase != null) {
               this.spawnAllParticles(this.lastDeceasedBase, 360, 2400.0F);
               this.spawnAllParticles(this.lastDeceasedBase, 1200, 300.0F);
            }

            value = 1.0F;
         }
      } else if (this.strikeAnimation.to == 0.0F) {
         this.strikeAnimation.speed = 0.07F;
         mc.gameSettings.anaglyph = false;
         if ((value = this.strikeAnimation.getAnim()) < 0.01F && value != 0.0F) {
            this.strikeAnimation.setAnim(0.0F);
         }
      }

      return value;
   }

   private void triggerStrikeAniation() {
      this.strikeAnimation.to = 1.0F;
      this.strikeAnimation.setAnim(0.5F);
   }

   private void playStrikeSound() {
      MusicHelper.playSound("strikesf-2.wav", 0.3F);
   }

   private void doFlickToDeceased(Entity deceased) {
      float[] rot = RotationUtil.getMatrixRotations4(deceased);
      if (RotationUtil.getAngleDifference(Minecraft.player.rotationYaw, rot[0]) > 10.0F
         || RotationUtil.getAngleDifference(Minecraft.player.rotationPitch, rot[1]) > 5.0F) {
         Minecraft.player.rotationYaw = rot[0];
         Minecraft.player.rotationPitch = rot[1];
      }
   }

   private long getMaxTimeMemory() {
      return 1000L;
   }

   private Runnable getMemoryTrigger(Entity deceased) {
      return () -> {
         this.triggerStrikeAniation();
         this.playStrikeSound();
         if (deceased != null) {
            this.doFlickToDeceased(deceased);
         }

         Timer.forceTimer(0.35F);
         this.timerForce2 = true;
         if (deceased instanceof EntityLivingBase deceasedBase) {
            this.spawnAllParticles(deceasedBase, 2400, 150.0F);
            this.spawnAllParticles(deceasedBase, 3600, 450.0F);
            this.lastDeceasedBase = deceasedBase;
         }
      };
   }

   private void controllingAddingMemoryToEntity(EntityLivingBase baseTo) {
      if (baseTo != null && !(baseTo instanceof EntityPlayerSP) && baseTo.ticksExisted >= 2 && baseTo.getHealth() != 0.0F) {
         FragEffects.EntityDeathMemory searchedMemory = null;

         for (FragEffects.EntityDeathMemory memory : this.DEATH_MEMORIES_LIST) {
            if (memory.isValidMemory() && memory.base.getEntityId() == baseTo.getEntityId()) {
               searchedMemory = memory;
            }
         }

         long maxTimeMemory = this.getMaxTimeMemory();
         if (searchedMemory != null) {
            searchedMemory.resetMemory(baseTo, maxTimeMemory);
         } else if ((!this.NoMobsDetect.getBool() || baseTo instanceof EntityOtherPlayerMP) && this.DEATH_MEMORIES_LIST.isEmpty()) {
            this.DEATH_MEMORIES_LIST.add(new FragEffects.EntityDeathMemory(baseTo, maxTimeMemory, this.getMemoryTrigger(baseTo)));
         }
      }
   }

   private void removeAutoMemories() {
      this.DEATH_MEMORIES_LIST.removeIf(FragEffects.EntityDeathMemory::isNotValidMemory);
   }

   @EventTarget
   public void onSendPackets(EventSendPacket event) {
      Entity checkedEntity;
      if (event.getPacket() instanceof CPacketUseEntity useEntityPacket
         && useEntityPacket.getAction() == Action.ATTACK
         && (checkedEntity = useEntityPacket.getEntityFromWorld(mc.world)) != null
         && checkedEntity instanceof EntityLivingBase base) {
         this.controllingAddingMemoryToEntity(base);
      }
   }

   private ResourceLocation getShaderEffectLoc() {
      return new ResourceLocation("shaders/post/desaturate.json");
   }

   private void updateDesaturate(boolean setTrue) {
      if (Config.isShaders() || mc.gameSettings.ofFastRender) {
         setTrue = false;
      }

      if (setTrue) {
         if (!this.hasLoadedDesaturate) {
            mc.entityRenderer.loadShader(this.getShaderEffectLoc());
            this.hasLoadedDesaturate = true;
         }
      } else if (this.hasLoadedDesaturate) {
         if (mc.entityRenderer.isShaderActive()) {
            mc.entityRenderer.theShaderGroup = null;
         }

         this.hasLoadedDesaturate = false;
      }
   }

   private void drawLightVignette(ScaledResolution sr, float alphaPC, boolean loadMode) {
      int color = ColorUtils.getColor(255, (int)(255.0F * (1.0F - alphaPC / 1.25F)), (int)(255.0F * (1.0F - alphaPC / 1.25F)), 190.0F * alphaPC);
      float sideSetX = (float)sr.getScaledWidth() / 2.0F * (1.0F - alphaPC) * (1.0F - alphaPC);
      float sideSetY = (float)sr.getScaledHeight() / 2.0F * (1.0F - alphaPC) * (1.0F - alphaPC);
      GL11.glEnable(3042);
      GL11.glDepthMask(false);
      GL11.glDisable(3008);
      mc.getTextureManager().bindTexture(this.LIGHT_VIGNETTE_TEX);
      RenderUtils.glColor(loadMode ? 0 : color);
      GL11.glBlendFunc(770, 32772);
      Gui.drawModalRectWithCustomSizedTexture(
         -sideSetX,
         -sideSetY,
         0.0F,
         0.0F,
         (float)sr.getScaledWidth() + sideSetX * 2.0F,
         (float)sr.getScaledHeight() + sideSetY * 2.0F,
         (float)sr.getScaledWidth() + sideSetX * 2.0F,
         (float)sr.getScaledHeight() + sideSetY * 2.0F
      );
      GL11.glBlendFunc(770, 771);
      GlStateManager.resetColor();
      GL11.glEnable(3008);
      GL11.glDepthMask(true);
   }

   @Override
   public void onUpdate() {
      if (this.timerForce2) {
         Timer.forceTimer(0.1F);
         this.timerForce2 = false;
      }

      List<EntityLivingBase> toUpdateMemoryEntities = new ArrayList<>();
      if (HitAura.TARGET_ROTS != null) {
         toUpdateMemoryEntities.add(HitAura.TARGET_ROTS);
      }

      if (BowAimbot.target != null) {
         toUpdateMemoryEntities.add(BowAimbot.target);
      }

      if (mc.pointedEntity != null && mc.pointedEntity instanceof EntityLivingBase pointedBase && pointedBase != null) {
         toUpdateMemoryEntities.add(pointedBase);
      }

      if (!CrystalField.listIsEmptyOrNull(CrystalField.getTargets())) {
         for (EntityLivingBase crystalFieldTarget : CrystalField.getTargets()) {
            if (crystalFieldTarget != null) {
               toUpdateMemoryEntities.add(crystalFieldTarget);
            }
         }
      }

      toUpdateMemoryEntities.stream().filter(base -> base.getHealth() != 0.0F).forEach(base -> {
         if (base != null) {
            this.controllingAddingMemoryToEntity(base);
         }
      });
      this.DEATH_MEMORIES_LIST.forEach(FragEffects.EntityDeathMemory::updateMemoryTrigger);
      this.removeAutoMemories();
   }

   private float getGlobalEffectAnimation(boolean setup) {
      if (setup) {
         this.stateAnim.to = this.actived ? 1.0F : 0.0F;
      }

      float value = setup ? this.stateAnim.getAnim() * this.updateStrikeAnimation() : this.stateAnim.anim * this.strikeAnimation.anim;
      if (value < 0.005F) {
         value = 0.0F;
      }

      if (value > 0.995F) {
         value = 1.0F;
      }

      return value;
   }

   @Override
   public void onToggled(boolean actived) {
      if (actived) {
         this.timerForce2 = false;
      } else {
         this.DEATH_MEMORIES_LIST.clear();
         this.timerForce2 = false;
      }

      this.updateDesaturate(false);
      this.strikeAnimation.setAnim(0.0F);
      this.strikeAnimation.to = 0.0F;
      super.onToggled(actived);
   }

   @Override
   public void alwaysRender2D(ScaledResolution sr) {
      if (!this.texHasLoaded) {
         this.drawLightVignette(sr, 1.0F, true);
         this.texHasLoaded = true;
      }

      float alphaPC;
      if ((alphaPC = this.getGlobalEffectAnimation(true)) != 0.0F) {
         this.updateDesaturate(alphaPC >= 0.4F);
         this.drawLightVignette(sr, alphaPC, false);
      }
   }

   @Override
   public void alwaysRender3D() {
      float alphaPC = this.stateAnim.getAnim();
      if (!((double)alphaPC < 0.003)) {
         this.particlesRemoveAuto();
         this.drawAllParticles(alphaPC);
      }
   }

   public float getStrikeEffectFovModifyPC() {
      return this.getGlobalEffectAnimation(false);
   }

   private void spawnAllParticles(EntityLivingBase base, int count, float maxTime) {
      if (base != null) {
         float startDst = 0.5F;
         float yExt = startDst / 5.0F;
         float yMaxRand = yExt / 2.0F;
         Vec3d spawnPos = base.getPositionVector().addVector(0.0, (double)(base.getEyeHeight() / 2.0F), 0.0);

         for (int index = 0; index < count; index++) {
            float pc01 = (float)index / (float)count;
            int yExtMul = 1;
            float yRandom = (float)((double)(-yMaxRand / 2.0F) + Math.random() * (double)yMaxRand);
            float dstRandom = startDst / 2.0F + (float)((double)(-startDst / 40.0F) + Math.random() * (double)startDst / 20.0);
            float yExtRandom = (float)((double)((float)yExtMul * yExt) * Math.random());
            this.fragParticles
               .add(
                  new FragEffects.FragParticle(
                     spawnPos.addVector(0.0, (double)yRandom, 0.0),
                     pc01 * 360.0F,
                     startDst,
                     startDst + dstRandom,
                     36.0F * (float)Math.random(),
                     yExtRandom,
                     maxTime
                  )
               );
         }
      }
   }

   private void particlesRemoveAuto() {
      if (!this.fragParticles.isEmpty()) {
         this.fragParticles.removeIf(FragEffects.FragParticle::toRemove);
      }
   }

   private void drawAllParticles(float alphaPC) {
      if (alphaPC != 0.0F && !this.fragParticles.isEmpty()) {
         RenderUtils.setup3dForBlockPos(
            () -> {
               GL11.glEnable(2929);
               GL11.glDisable(3008);
               GL11.glEnable(2832);
               GL11.glDepthMask(false);
               boolean lines = this.strikeAnimation.to == 0.0F;
               GL11.glBlendFunc(770, 32772);
               if (lines) {
                  GL11.glBegin(4);
               } else {
                  GL11.glPointSize(3.0F);
                  GL11.glBegin(0);
               }

               for (FragEffects.FragParticle fragParticle : this.fragParticles) {
                  float aPC = MathUtils.clamp(Math.min(fragParticle.get010PC() * 3.0F, 1.0F) * fragParticle.getAlphaPC(), 0.0F, 1.0F) * alphaPC;
                  if (aPC == 0.0F) {
                     return;
                  }

                  int color = ColorUtils.getOverallColorFrom(
                     ColorUtils.swapAlpha(0, 255.0F * aPC),
                     ColorUtils.getColor(255, (int)(255.0F * aPC), (int)(255.0F * aPC), 255.0F * aPC),
                     fragParticle.randomFloat
                  );
                  Vec3d pos = fragParticle.getPos();
                  RenderUtils.glColor(color);
                  GL11.glVertex3d(pos.xCoord, pos.yCoord, pos.zCoord);
               }

               GL11.glEnd();
               if (!lines) {
                  GL11.glPointSize(1.0F);
               }

               GL11.glBlendFunc(770, 771);
               GL11.glEnable(3008);
               GL11.glDepthMask(true);
            },
            false
         );
      }
   }

   private class EntityDeathMemory {
      private final TimerHelper startTime = new TimerHelper();
      private long maxTimeMemory;
      private EntityLivingBase base;
      private boolean hasReseted;
      private final Runnable onTrigger;

      public EntityDeathMemory(EntityLivingBase base, long maxTimeMemory, Runnable onTrigger) {
         this.base = base;
         this.startTime.reset();
         this.maxTimeMemory = maxTimeMemory;
         this.onTrigger = onTrigger;
      }

      public void resetMemory(EntityLivingBase base, long maxTimeMemory) {
         if (this.base == null || this.base.getHealth() != 0.0F) {
            this.base = base;
            this.maxTimeMemory = maxTimeMemory;
            this.startTime.reset();
         }
      }

      public boolean isValidMemory() {
         return this.base != null && this.maxTimeMemory != 0L && !this.startTime.hasReached((double)this.maxTimeMemory);
      }

      public boolean isNotValidMemory() {
         return !this.isValidMemory();
      }

      public void updateMemoryTrigger() {
         if (this.isValidMemory() && this.base.getHealth() == 0.0F && !this.hasReseted) {
            this.onTrigger.run();
            this.hasReseted = true;
            this.maxTimeMemory = 300L;
         }
      }
   }

   private class FragParticle {
      public Vec3d spawnPos;
      public long startTime = System.currentTimeMillis();
      public float startYaw;
      public float toYaw;
      public float startDst;
      public float toDst;
      public float toY;
      public float maxTime;
      public float randomFloat = (float)Math.random();

      public float getTimePC() {
         return MathUtils.clamp((float)(System.currentTimeMillis() - this.startTime) / this.maxTime, 0.0F, 1.0F);
      }

      public float get010PC() {
         float timePC = this.getTimePC();
         return ((double)timePC > 0.5 ? 1.0F - timePC : timePC) * 2.0F;
      }

      public float getAlphaPC() {
         return 1.0F - this.getTimePC();
      }

      public boolean toRemove() {
         return this.getTimePC() == 1.0F;
      }

      public FragParticle(Vec3d spawnPos, float yaw, float startDst, float maxDst, float additionYaw, float additionY, float maxTime) {
         this.startDst = startDst;
         this.toDst = maxDst;
         this.startYaw = yaw;
         this.toYaw = this.startYaw + additionYaw;
         this.toY = additionY;
         this.maxTime = maxTime;
         this.spawnPos = spawnPos;
      }

      private Vec3d posAdds(Vec3d vec3d, float yaw, float dst) {
         double yawRadian = Math.toRadians((double)yaw);
         return vec3d.addVector(Math.sin(yawRadian) * (double)dst, 0.0, Math.cos(yawRadian) * (double)dst);
      }

      public Vec3d getPos() {
         float timePC = this.getTimePC();
         float yaw = MathUtils.lerp(this.startYaw, this.toYaw, timePC);
         float dst = MathUtils.lerp(this.startDst, this.toDst, timePC);
         float yExt = this.toY * timePC;
         return this.posAdds(this.spawnPos, yaw, dst).addVector(0.0, (double)yExt, 0.0);
      }
   }
}
