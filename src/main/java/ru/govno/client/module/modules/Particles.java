package ru.govno.client.module.modules;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;
import ru.govno.client.event.EventTarget;
import ru.govno.client.event.events.Event3D;
import ru.govno.client.event.events.EventSendPacket;
import ru.govno.client.module.Module;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.module.settings.FloatSettings;
import ru.govno.client.module.settings.ModeSettings;
import ru.govno.client.utils.Math.MathUtils;
import ru.govno.client.utils.Math.TimerHelper;
import ru.govno.client.utils.Render.AnimationUtils;
import ru.govno.client.utils.Render.ColorUtils;
import ru.govno.client.utils.Render.RenderUtils;

public class Particles extends Module {
   List<Particles.Part> parts = new ArrayList<>();
   ModeSettings Mode;
   ModeSettings Particle;
   BoolSettings RandomizePos;
   FloatSettings RandomStrength;
   private static final ResourceLocation STAR_TEXTURE = new ResourceLocation("vegaline/modules/particles/particle1.png");
   private final Tessellator tessellator = Tessellator.getInstance();
   private final BufferBuilder buffer = this.tessellator.getBuffer();

   public Particles() {
      super("Particles", 0, Module.Category.RENDER);
      this.settings.add(this.Mode = new ModeSettings("Mode", "Client", this, new String[]{"Client", "Minecraft"}));
      this.settings
         .add(
            this.Particle = new ModeSettings(
               "Particle",
               "Crit",
               this,
               new String[]{
                  "Explode",
                  "Largeexplode",
                  "HugeExplosion",
                  "FireworksSpark",
                  "Bubble",
                  "Splash",
                  "Wake",
                  "Suspended",
                  "DepthSuspend",
                  "Crit",
                  "MagicCrit",
                  "Smoke",
                  "LargeSmoke",
                  "Spell",
                  "InstantSpell",
                  "MobSpell",
                  "MobSpellAmbient",
                  "WitchMagic",
                  "DripWater",
                  "DripLava",
                  "AngryVillager",
                  "HappyVillager",
                  "TownAura",
                  "Note",
                  "Portal",
                  "EnchantmentTable",
                  "Flame",
                  "Lava",
                  "Footstep",
                  "Cloud",
                  "Reddust",
                  "SnowBallPoof",
                  "SnowShovel",
                  "Slime",
                  "Heart",
                  "Barrier",
                  "IconCrack",
                  "BlockCrack",
                  "BlockDust",
                  "Droplet",
                  "Take",
                  "MobAppearance",
                  "DragonBreath",
                  "EndRod",
                  "DamageIndicator",
                  "SweepAttack",
                  "FallingDust",
                  "Totem",
                  "Spit"
               },
               () -> this.Mode.currentMode.equalsIgnoreCase("Minecraft")
            )
         );
      this.settings.add(this.RandomizePos = new BoolSettings("RandomizePos", true, this, () -> this.Mode.getMode().equalsIgnoreCase("Minecraft")));
      this.settings
         .add(this.RandomStrength = new FloatSettings("RandomStrength", 0.9F, 1.0F, 0.05F, this, () -> this.Mode.getMode().equalsIgnoreCase("Minecraft")));
   }

   @EventTarget
   public void onPacketSend(EventSendPacket event) {
      if (event.getPacket() instanceof CPacketUseEntity packet) {
         Entity entityIn = null;
         if (packet.getAction() == CPacketUseEntity.Action.ATTACK && packet instanceof CPacketUseEntity) {
            if (mc.world == null) {
               return;
            }

            entityIn = packet.getEntityFromWorld(mc.world);
         }

         if (!Minecraft.player.isEntityAlive()) {
            return;
         }

         if (this.Mode.getMode().equalsIgnoreCase("Client")) {
            if (entityIn instanceof EntityLivingBase) {
               int maxTime = 6000;
               if (entityIn == null && !entityIn.isEntityAlive() || entityIn instanceof EntityPlayerSP) {
                  return;
               }

               float w = entityIn.width / 2.0F;
               float h = entityIn.height;
               Vec3d vec = entityIn.getPositionVector()
                  .addVector((double)(-w) + (double)(w * 2.0F) * Math.random(), (double)h * Math.random(), (double)(-w) + (double)(w * 2.0F) * Math.random());
               this.parts.add(new Particles.Part(vec, (long)maxTime));
               this.parts.add(new Particles.Part(vec, (long)maxTime));
            }
         } else if (event.getPacket() instanceof CPacketUseEntity && packet.getAction() == CPacketUseEntity.Action.ATTACK && packet instanceof CPacketUseEntity
            )
          {
            for (int count = 0; count < 60; count++) {
               this.addParticlesFor(entityIn, 1.0F);
            }
         }
      }
   }

   int getById(String mode) {
      if (mode.equalsIgnoreCase("Explode")) {
         return 0;
      } else if (mode.equalsIgnoreCase("Largeexplode")) {
         return 1;
      } else if (mode.equalsIgnoreCase("HugeExplosion")) {
         return 2;
      } else if (mode.equalsIgnoreCase("FireworksSpark")) {
         return 3;
      } else if (mode.equalsIgnoreCase("Bubble")) {
         return 4;
      } else if (mode.equalsIgnoreCase("Splash")) {
         return 5;
      } else if (mode.equalsIgnoreCase("Wake")) {
         return 6;
      } else if (mode.equalsIgnoreCase("Suspended")) {
         return 7;
      } else if (mode.equalsIgnoreCase("DepthSuspend")) {
         return 8;
      } else if (mode.equalsIgnoreCase("Crit")) {
         return 9;
      } else if (mode.equalsIgnoreCase("MagicCrit")) {
         return 10;
      } else if (mode.equalsIgnoreCase("Smoke")) {
         return 11;
      } else if (mode.equalsIgnoreCase("LargeSmoke")) {
         return 12;
      } else if (mode.equalsIgnoreCase("Spell")) {
         return 13;
      } else if (mode.equalsIgnoreCase("InstantSpell")) {
         return 14;
      } else if (mode.equalsIgnoreCase("MobSpell")) {
         return 15;
      } else if (mode.equalsIgnoreCase("MobSpellAmbient")) {
         return 16;
      } else if (mode.equalsIgnoreCase("WitchMagic")) {
         return 17;
      } else if (mode.equalsIgnoreCase("DripWater")) {
         return 18;
      } else if (mode.equalsIgnoreCase("DripLava")) {
         return 19;
      } else if (mode.equalsIgnoreCase("AngryVillager")) {
         return 20;
      } else if (mode.equalsIgnoreCase("HappyVillager")) {
         return 21;
      } else if (mode.equalsIgnoreCase("TownAura")) {
         return 22;
      } else if (mode.equalsIgnoreCase("Note")) {
         return 23;
      } else if (mode.equalsIgnoreCase("Portal")) {
         return 24;
      } else if (mode.equalsIgnoreCase("EnchantmentTable")) {
         return 25;
      } else if (mode.equalsIgnoreCase("Flame")) {
         return 26;
      } else if (mode.equalsIgnoreCase("Lava")) {
         return 27;
      } else if (mode.equalsIgnoreCase("Footstep")) {
         return 28;
      } else if (mode.equalsIgnoreCase("Cloud")) {
         return 29;
      } else if (mode.equalsIgnoreCase("Reddust")) {
         return 30;
      } else if (mode.equalsIgnoreCase("SnowBallPoof")) {
         return 31;
      } else if (mode.equalsIgnoreCase("SnowShovel")) {
         return 32;
      } else if (mode.equalsIgnoreCase("Slime")) {
         return 33;
      } else if (mode.equalsIgnoreCase("Heart")) {
         return 34;
      } else if (mode.equalsIgnoreCase("Barrier")) {
         return 35;
      } else if (mode.equalsIgnoreCase("IconCrack")) {
         return 36;
      } else if (mode.equalsIgnoreCase("BlockCrack")) {
         return 37;
      } else if (mode.equalsIgnoreCase("BlockDust")) {
         return 38;
      } else if (mode.equalsIgnoreCase("Droplet")) {
         return 39;
      } else if (mode.equalsIgnoreCase("Take")) {
         return 40;
      } else if (mode.equalsIgnoreCase("MobAppearance")) {
         return 41;
      } else if (mode.equalsIgnoreCase("DragonBreath")) {
         return 42;
      } else if (mode.equalsIgnoreCase("EndRod")) {
         return 43;
      } else if (mode.equalsIgnoreCase("DamageIndicator")) {
         return 44;
      } else if (mode.equalsIgnoreCase("SweepAttack")) {
         return 45;
      } else if (mode.equalsIgnoreCase("FallingDust")) {
         return 46;
      } else if (mode.equalsIgnoreCase("Totem")) {
         return 47;
      } else {
         return mode.equalsIgnoreCase("Spit") ? 48 : -1;
      }
   }

   void addParticlesFor(Entity entityIn, float amount) {
      if (entityIn != null) {
         String mode = this.Particle.currentMode;
         double posX = entityIn.posX;
         double posY = entityIn.posY;
         double posZ = entityIn.posZ;
         double next = 0.0;
         double next2 = (double)((float)MathUtils.getRandomInRange(-1, 1) * this.RandomStrength.getFloat());
         double next3 = (double)((float)(-MathUtils.getRandomInRange(-1, 1)) * this.RandomStrength.getFloat());
         double next4 = (double)((float)MathUtils.getRandomInRange(-1, 1) * -this.RandomStrength.getFloat());
         if (this.RandomizePos.getBool()) {
            next = (double)(2.0F * this.RandomStrength.getFloat());
            double w = (double)(entityIn.width * this.RandomStrength.getFloat());
            double h = (double)(entityIn.getEyeHeight() * this.RandomStrength.getFloat());
            posX += MathUtils.getRandomInRange(-w / 2.0, w / 2.0) * next * 4.0;
            posY += MathUtils.getRandomInRange(-h / 2.0, h / 2.0) * next * 2.0;
            posZ += MathUtils.getRandomInRange(-w / 2.0, w / 2.0) * next * 4.0;
         }

         EnumParticleTypes particle = null;
         if (this.getById(mode) != -1) {
            particle = EnumParticleTypes.getParticleFromId(this.getById(mode));
         }

         if (particle != null) {
            int oldSetting = mc.gameSettings.particleSetting;
            boolean oldSetting2 = mc.gameSettings.ofFireworkParticles;
            boolean oldSetting3 = mc.gameSettings.ofPortalParticles;
            boolean oldSetting4 = mc.gameSettings.ofPotionParticles;
            boolean oldSetting5 = mc.gameSettings.ofVoidParticles;
            boolean oldSetting6 = mc.gameSettings.ofWaterParticles;
            mc.gameSettings.particleSetting = 1;
            mc.gameSettings.ofFireworkParticles = true;
            mc.gameSettings.ofPortalParticles = true;
            mc.gameSettings.ofPotionParticles = true;
            mc.gameSettings.ofVoidParticles = true;
            mc.gameSettings.ofWaterParticles = true;
            mc.world.spawnParticle(particle, posX, posY, posZ, next2, next3, next4, new int[]{1});
            mc.gameSettings.particleSetting = oldSetting;
            mc.gameSettings.ofFireworkParticles = oldSetting2;
            mc.gameSettings.ofPortalParticles = oldSetting3;
            mc.gameSettings.ofPotionParticles = oldSetting4;
            mc.gameSettings.ofVoidParticles = oldSetting5;
            mc.gameSettings.ofWaterParticles = oldSetting6;
         }
      }
   }

   @EventTarget
   public void onRender3D(Event3D event) {
      if (this.Mode.currentMode.equalsIgnoreCase("Client") && !this.parts.isEmpty()) {
         this.setupRenderParts(
            () -> {
               float i = 0.0F;
               if (!this.parts.isEmpty()) {
                  mc.getTextureManager().bindTexture(STAR_TEXTURE);

                  for (Particles.Part part : this.parts) {
                     if (part != null && !part.toRemove) {
                        float gradPC = i / (float)this.parts.size();
                        int col = ColorUtils.getOverallColorFrom(
                           ClientColors.getColor1((int)(i * 5.0F)), ClientColors.getColor2((int)(i * 5.0F)), MathUtils.clamp(gradPC, 0.0F, 1.0F)
                        );
                        part.vertexColored(col);
                        i++;
                     }
                  }
               }
            },
            true
         );
      }
   }

   boolean setupRenderParts(Runnable render, boolean bloom) {
      Vec3d renderPos = new Vec3d(RenderManager.viewerPosX, RenderManager.viewerPosY, RenderManager.viewerPosZ);
      GlStateManager.pushMatrix();
      GlStateManager.enableBlend();
      GlStateManager.depthMask(false);
      GlStateManager.tryBlendFuncSeparate(
         GlStateManager.SourceFactor.SRC_ALPHA,
         bloom ? GlStateManager.DestFactor.ONE : GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
         GlStateManager.SourceFactor.ONE,
         GlStateManager.DestFactor.ZERO
      );
      GlStateManager.translate(-renderPos.xCoord, -renderPos.yCoord, -renderPos.zCoord);
      GlStateManager.shadeModel(7425);
      mc.entityRenderer.disableLightmap();
      render.run();
      GlStateManager.translate(renderPos.xCoord, renderPos.yCoord, renderPos.zCoord);
      GlStateManager.shadeModel(7424);
      if (bloom) {
         GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO
         );
      }

      GlStateManager.depthMask(true);
      GlStateManager.resetColor();
      GlStateManager.popMatrix();
      return true;
   }

   @Override
   public void onUpdate() {
      if (this.Mode.currentMode.equalsIgnoreCase("Client") && !this.parts.isEmpty()) {
         for (int i = 0; i < this.parts.size(); i++) {
            Particles.Part part = this.parts.get(i);
            if (part != null) {
               if (part.toRemove) {
                  this.parts.remove(i);
               } else {
                  part.updatePart();
               }
            }
         }
      } else if (!this.parts.isEmpty()) {
         this.parts.clear();
      }
   }

   class Part {
      AnimationUtils alphaPC = new AnimationUtils(0.1F, 1.0F, 0.035F);
      boolean toRemove = false;
      float[] randomXYZM = new float[]{
         (float)MathUtils.getRandomInRange(0.3, -0.3), (float)MathUtils.getRandomInRange(0.12, -0.09), (float)MathUtils.getRandomInRange(0.3, -0.3)
      };
      AnimationUtils posX;
      AnimationUtils posY;
      AnimationUtils posZ;
      float motionX = this.randomXYZM[0];
      float motionY = this.randomXYZM[1];
      float motionZ = this.randomXYZM[2];
      TimerHelper time = new TimerHelper();
      long maxTime;

      Part(Vec3d vec, long maxTime) {
         this.posX = new AnimationUtils((float)vec.xCoord, (float)vec.xCoord, 0.08F);
         this.posY = new AnimationUtils((float)vec.yCoord, (float)vec.yCoord, 0.08F);
         this.posZ = new AnimationUtils((float)vec.zCoord, (float)vec.zCoord, 0.08F);
         this.maxTime = maxTime;
         this.time.reset();
      }

      long getTime() {
         return this.time.getTime();
      }

      float getSpeed() {
         return (float)Math.sqrt((double)(this.motionX * this.motionX + this.motionZ * this.motionZ));
      }

      void gravityAndMove() {
         this.posX.to = this.posX.getAnim() + this.motionX;
         this.posY.to = this.posY.getAnim() + this.motionY;
         this.posZ.to = this.posZ.getAnim() + this.motionZ;
         float x = this.posX.getAnim();
         float y = this.posY.getAnim();
         float z = this.posZ.getAnim();
         float motionX = this.motionX;
         float motionY = this.motionY;
         float motionZ = this.motionZ;
         BlockPos xPrePos = new BlockPos((double)(x + motionX * 2.0F), (double)(y - motionY) + 0.1, (double)z);
         BlockPos yPrePos = new BlockPos((double)x, (double)(y + motionY * 2.0F), (double)z);
         BlockPos zPrePos = new BlockPos((double)x, (double)(y - motionY) + 0.1, (double)(z + motionZ * 2.0F));
         IBlockState xState = Module.mc.world.getBlockState(xPrePos);
         IBlockState yState = Module.mc.world.getBlockState(yPrePos);
         IBlockState zState = Module.mc.world.getBlockState(zPrePos);
         boolean collideX = xState.getCollisionBoundingBox(Module.mc.world, xPrePos) != null;
         boolean collideY = yState.getCollisionBoundingBox(Module.mc.world, yPrePos) != null;
         boolean collideZ = zState.getCollisionBoundingBox(Module.mc.world, zPrePos) != null;
         boolean isInLiquid = xState.getBlock() instanceof BlockLiquid || zState.getBlock() instanceof BlockLiquid || yState.getBlock() instanceof BlockLiquid;
         boolean gravityY = motionY != 0.0F;
         if (gravityY) {
            this.motionY -= 0.01F;
         }

         if (isInLiquid) {
            this.motionX *= 0.975F;
            this.motionY *= 0.75F;
            this.motionY -= 0.025F;
            this.motionZ *= 0.975F;
         }

         if (collideY) {
            this.motionY = -this.motionY * 0.9F;
         }

         if (collideX) {
            this.motionX *= -1.0F;
         }

         if (collideZ) {
            this.motionZ *= -1.0F;
         }
      }

      void updatePart() {
         if (this.getTime() > this.maxTime) {
            this.alphaPC.to = 0.0F;
         }

         if ((double)this.alphaPC.getAnim() < 0.005) {
            this.toRemove = true;
         }

         if (!this.toRemove) {
            this.gravityAndMove();
         }
      }

      void vertexColored(int color) {
         float alphaPC = ColorUtils.getGLAlphaFromColor(color) * this.alphaPC.getAnim();
         color = ColorUtils.swapAlpha(color, alphaPC * 255.0F);
         GlStateManager.pushMatrix();
         GL11.glTranslated((double)this.posX.getAnim(), (double)this.posY.getAnim() + 0.25, (double)this.posZ.getAnim());
         float fixed = Module.mc.gameSettings.thirdPersonView == 2 ? -1.0F : 1.0F;
         GL11.glRotatef(-Module.mc.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
         GL11.glRotatef(Module.mc.getRenderManager().playerViewX, fixed, 0.0F, 0.0F);
         GL11.glScaled(-0.2, -0.2, -0.2);
         Particles.this.buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
         Particles.this.buffer.pos(0.0, 0.0, 0.0).tex(0.0, 0.0).color(color).endVertex();
         Particles.this.buffer.pos(0.0, 1.0, 0.0).tex(0.0, 1.0).color(color).endVertex();
         Particles.this.buffer.pos(1.0, 1.0, 0.0).tex(1.0, 1.0).color(color).endVertex();
         Particles.this.buffer.pos(1.0, 0.0, 0.0).tex(1.0, 0.0).color(color).endVertex();
         RenderUtils.customRotatedObject2D(
            0.0F, 0.0F, 1.0F, 1.0F, (double)((float)this.getTime() / (float)this.maxTime * 1200.0F * (float)(this.randomXYZM[0] > 0.0F ? 1 : -1))
         );
         Particles.this.tessellator.draw();
         GlStateManager.popMatrix();
      }
   }
}
