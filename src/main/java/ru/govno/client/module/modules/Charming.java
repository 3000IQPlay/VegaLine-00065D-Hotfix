package ru.govno.client.module.modules;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;
import ru.govno.client.module.Module;
import ru.govno.client.module.settings.ModeSettings;
import ru.govno.client.utils.Combat.RotationUtil;
import ru.govno.client.utils.Math.MathUtils;
import ru.govno.client.utils.Movement.MoveMeHelp;
import ru.govno.client.utils.Render.AnimationUtils;
import ru.govno.client.utils.Render.ColorUtils;
import ru.govno.client.utils.Render.RenderUtils;

public class Charming extends Module {
   private final String defaultLoc = "vegaline/modules/charming/models/default/";
   Charming.ResourceLocationWithSizes ACTIVE_RESOURCE_LOCATIONS;
   public static Charming get;
   ModeSettings CharmModel;
   ModeSettings ReactiveSkin;
   private final int[] sides = new int[]{0, 1, 2, 3, 4, 5};
   public Charming.Charm charm;

   private void reinitActiveResourceGroup(String name) {
      if (this.ACTIVE_RESOURCE_LOCATIONS == null || !this.ACTIVE_RESOURCE_LOCATIONS.getResource().getResourcePath().contains(name)) {
         this.ACTIVE_RESOURCE_LOCATIONS = new Charming.ResourceLocationWithSizes(
            new ResourceLocation("vegaline/modules/charming/models/default/" + name + ".png")
         );
      }
   }

   public Charming() {
      super("Charming", 0, Module.Category.RENDER);
      this.settings
         .add(this.CharmModel = new ModeSettings("CharModel", "MaksSteel", this, new String[]{"MaksSteel", "Draco", "Reactive", "Overheated", "Robot"}));
      this.settings
         .add(
            this.ReactiveSkin = new ModeSettings(
               "ReactiveSkin", "Blue", this, new String[]{"Orange", "Pink", "Blue", "Green"}, () -> this.CharmModel.currentMode.equalsIgnoreCase("Reactive")
            )
         );
      String loc = this.CharmModel.currentMode.toLowerCase();
      if (this.CharmModel.currentMode.equalsIgnoreCase("Reactive")) {
         loc = loc + this.ReactiveSkin.currentMode.toLowerCase();
      }

      this.reinitActiveResourceGroup(loc);
      get = this;
   }

   private int[] getTextureResolution(ResourceLocation location) {
      try {
         InputStream stream = mc.getResourceManager().getResource(location).getInputStream();
         BufferedImage image = ImageIO.read(stream);
         return new int[]{image.getWidth(), image.getHeight()};
      } catch (Exception var4) {
         var4.printStackTrace();
         return new int[]{0, 0};
      }
   }

   private double[][][] getAllUVFromTexWithSizes(Charming.ResourceLocationWithSizes resWithSizes) {
      float w = (float)resWithSizes.getResolution()[0];
      float h = (float)resWithSizes.getResolution()[1];
      float des = 0.25F / w;
      float full = 63.9F / w;
      return new double[][][]{
         {
               {(double)(8.0F / w), (double)des},
               {(double)(16.0F / w), (double)des},
               {(double)(16.0F / w), (double)((8.0F - des) / h)},
               {(double)(8.0F / w), (double)((8.0F - des) / h)}
         },
         {
               {(double)(16.0F / w), (double)des},
               {(double)(24.0F / w), (double)des},
               {(double)(24.0F / w), (double)((8.0F - des) / h)},
               {(double)(16.0F / w), (double)((8.0F - des) / h)}
         },
         {
               {(double)((24.0F + des) / w), (double)((8.0F + des) / h)},
               {(double)((32.0F - des) / w), (double)((8.0F + des) / h)},
               {(double)((32.0F - des) / w), (double)((16.0F - des) / h)},
               {(double)((24.0F + des) / w), (double)((16.0F - des) / h)}
         },
         {
               {(double)des, (double)((8.0F + des) / h)},
               {(double)((8.0F - des) / w), (double)((8.0F + des) / h)},
               {(double)((8.0F - des) / w), (double)((16.0F - des) / h)},
               {(double)des, (double)((16.0F - des) / h)}
         },
         {
               {(double)((8.0F + des) / w), (double)((8.0F + des) / h)},
               {(double)((16.0F - des) / w), (double)((8.0F + des) / h)},
               {(double)((16.0F - des) / w), (double)((16.0F - des) / h)},
               {(double)((8.0F + des) / w), (double)((16.0F - des) / h)}
         },
         {
               {(double)((16.0F + des) / w), (double)((8.0F + des) / h)},
               {(double)((24.0F - des) / w), (double)((8.0F + des) / h)},
               {(double)((24.0F - des) / w), (double)((16.0F - des) / h)},
               {(double)((16.0F + des) / w), (double)((16.0F - des) / h)}
         },
         {
               {(double)(40.0F / w), (double)des},
               {(double)(48.0F / w), (double)des},
               {(double)(48.0F / w), (double)((8.0F - des) / h)},
               {(double)(40.0F / w), (double)((8.0F - des) / h)}
         },
         {
               {(double)(48.0F / w), (double)des},
               {(double)(56.0F / w), (double)des},
               {(double)(56.0F / w), (double)((8.0F - des) / h)},
               {(double)(48.0F / w), (double)((8.0F - des) / h)}
         },
         {
               {(double)(48.0F / w), (double)((8.0F + des) / h)},
               {(double)(56.0F / w), (double)((8.0F + des) / h)},
               {(double)(56.0F / w), (double)((16.0F - des) / h)},
               {(double)(48.0F / w), (double)((16.0F - des) / h)}
         },
         {
               {(double)(32.0F / w), (double)((8.0F + des) / h)},
               {(double)(40.0F / w), (double)((8.0F + des) / h)},
               {(double)(40.0F / w), (double)((16.0F - des) / h)},
               {(double)(32.0F / w), (double)((16.0F - des) / h)}
         },
         {
               {(double)(40.0F / w), (double)((8.0F + des) / h)},
               {(double)(48.0F / w), (double)((8.0F + des) / h)},
               {(double)(48.0F / w), (double)((16.0F - des) / h)},
               {(double)(40.0F / w), (double)((16.0F - des) / h)}
         },
         {
               {(double)(56.0F / w), (double)((8.0F + des) / h)},
               {(double)full, (double)((8.0F + des) / h)},
               {(double)full, (double)((16.0F - des) / h)},
               {(double)(56.0F / w), (double)((16.0F - des) / h)}
         }
      };
   }

   private void drawTexturedCubeSide(
      AxisAlignedBB axisAlignedBB,
      Charming.ResourceLocationWithSizes resourceLocationWithSizes,
      int sideIndex,
      int color,
      Tessellator tessellator,
      boolean secondLayer
   ) {
      BufferBuilder buffer = tessellator.getBuffer();
      double w = axisAlignedBB.maxX - axisAlignedBB.minX;
      double ext = w * 0.19999999F / 4.0;
      double x = axisAlignedBB.minX;
      double y = axisAlignedBB.minY;
      double z = axisAlignedBB.minZ;
      double x2 = axisAlignedBB.maxX;
      double y2 = axisAlignedBB.maxY;
      double z2 = axisAlignedBB.maxZ;
      if (sideIndex >= 0 && sideIndex <= 5) {
         double[][][] TEX_UV_PARTS = this.getAllUVFromTexWithSizes(resourceLocationWithSizes);
         buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
         switch (sideIndex) {
            case 0:
               buffer.pos(x, y2, z).tex(TEX_UV_PARTS[sideIndex][0]).color(color).endVertex();
               buffer.pos(x, y2, z2).tex(TEX_UV_PARTS[sideIndex][3]).color(color).endVertex();
               buffer.pos(x2, y2, z2).tex(TEX_UV_PARTS[sideIndex][2]).color(color).endVertex();
               buffer.pos(x2, y2, z).tex(TEX_UV_PARTS[sideIndex][1]).color(color).endVertex();
               break;
            case 1:
               buffer.pos(x, y, z).tex(TEX_UV_PARTS[sideIndex][0]).color(color).endVertex();
               buffer.pos(x, y, z2).tex(TEX_UV_PARTS[sideIndex][3]).color(color).endVertex();
               buffer.pos(x2, y, z2).tex(TEX_UV_PARTS[sideIndex][2]).color(color).endVertex();
               buffer.pos(x2, y, z).tex(TEX_UV_PARTS[sideIndex][1]).color(color).endVertex();
               break;
            case 2:
               buffer.pos(x, y2, z).tex(TEX_UV_PARTS[sideIndex][0]).color(color).endVertex();
               buffer.pos(x2, y2, z).tex(TEX_UV_PARTS[sideIndex][1]).color(color).endVertex();
               buffer.pos(x2, y, z).tex(TEX_UV_PARTS[sideIndex][2]).color(color).endVertex();
               buffer.pos(x, y, z).tex(TEX_UV_PARTS[sideIndex][3]).color(color).endVertex();
               break;
            case 3:
               buffer.pos(x2, y2, z).tex(TEX_UV_PARTS[sideIndex][0]).color(color).endVertex();
               buffer.pos(x2, y2, z2).tex(TEX_UV_PARTS[sideIndex][1]).color(color).endVertex();
               buffer.pos(x2, y, z2).tex(TEX_UV_PARTS[sideIndex][2]).color(color).endVertex();
               buffer.pos(x2, y, z).tex(TEX_UV_PARTS[sideIndex][3]).color(color).endVertex();
               break;
            case 4:
               buffer.pos(x, y2, z2).tex(TEX_UV_PARTS[sideIndex][0]).color(color).endVertex();
               buffer.pos(x2, y2, z2).tex(TEX_UV_PARTS[sideIndex][1]).color(color).endVertex();
               buffer.pos(x2, y, z2).tex(TEX_UV_PARTS[sideIndex][2]).color(color).endVertex();
               buffer.pos(x, y, z2).tex(TEX_UV_PARTS[sideIndex][3]).color(color).endVertex();
               break;
            case 5:
               buffer.pos(x, y2, z).tex(TEX_UV_PARTS[sideIndex][0]).color(color).endVertex();
               buffer.pos(x, y2, z2).tex(TEX_UV_PARTS[sideIndex][1]).color(color).endVertex();
               buffer.pos(x, y, z2).tex(TEX_UV_PARTS[sideIndex][2]).color(color).endVertex();
               buffer.pos(x, y, z).tex(TEX_UV_PARTS[sideIndex][3]).color(color).endVertex();
         }

         tessellator.draw();
         if (secondLayer) {
            x -= ext;
            y -= ext;
            z -= ext;
            x2 += ext;
            y2 += ext;
            z2 += ext;
            buffer.begin(9, DefaultVertexFormats.POSITION_TEX_COLOR);
            sideIndex += 6;
            switch (sideIndex) {
               case 6:
                  buffer.pos(x, y2, z).tex(TEX_UV_PARTS[sideIndex][1]).color(color).endVertex();
                  buffer.pos(x, y2, z2).tex(TEX_UV_PARTS[sideIndex][2]).color(color).endVertex();
                  buffer.pos(x2, y2, z2).tex(TEX_UV_PARTS[sideIndex][3]).color(color).endVertex();
                  buffer.pos(x2, y2, z).tex(TEX_UV_PARTS[sideIndex][0]).color(color).endVertex();
                  break;
               case 7:
                  buffer.pos(x, y, z).tex(TEX_UV_PARTS[sideIndex][1]).color(color).endVertex();
                  buffer.pos(x, y, z2).tex(TEX_UV_PARTS[sideIndex][2]).color(color).endVertex();
                  buffer.pos(x2, y, z2).tex(TEX_UV_PARTS[sideIndex][3]).color(color).endVertex();
                  buffer.pos(x2, y, z).tex(TEX_UV_PARTS[sideIndex][0]).color(color).endVertex();
                  break;
               case 8:
                  buffer.pos(x2, y2, z).tex(TEX_UV_PARTS[sideIndex][1]).color(color).endVertex();
                  buffer.pos(x2, y2, z2).tex(TEX_UV_PARTS[sideIndex][0]).color(color).endVertex();
                  buffer.pos(x2, y, z2).tex(TEX_UV_PARTS[sideIndex][3]).color(color).endVertex();
                  buffer.pos(x2, y, z).tex(TEX_UV_PARTS[sideIndex][2]).color(color).endVertex();
                  break;
               case 9:
                  buffer.pos(x, y2, z).tex(TEX_UV_PARTS[sideIndex][0]).color(color).endVertex();
                  buffer.pos(x, y2, z2).tex(TEX_UV_PARTS[sideIndex][1]).color(color).endVertex();
                  buffer.pos(x, y, z2).tex(TEX_UV_PARTS[sideIndex][2]).color(color).endVertex();
                  buffer.pos(x, y, z).tex(TEX_UV_PARTS[sideIndex][3]).color(color).endVertex();
                  break;
               case 10:
                  buffer.pos(x, y2, z2).tex(TEX_UV_PARTS[sideIndex][0]).color(color).endVertex();
                  buffer.pos(x2, y2, z2).tex(TEX_UV_PARTS[sideIndex][1]).color(color).endVertex();
                  buffer.pos(x2, y, z2).tex(TEX_UV_PARTS[sideIndex][2]).color(color).endVertex();
                  buffer.pos(x, y, z2).tex(TEX_UV_PARTS[sideIndex][3]).color(color).endVertex();
                  break;
               case 11:
                  buffer.pos(x, y2, z).tex(TEX_UV_PARTS[sideIndex][0]).color(color).endVertex();
                  buffer.pos(x2, y2, z).tex(TEX_UV_PARTS[sideIndex][1]).color(color).endVertex();
                  buffer.pos(x2, y, z).tex(TEX_UV_PARTS[sideIndex][2]).color(color).endVertex();
                  buffer.pos(x, y, z).tex(TEX_UV_PARTS[sideIndex][3]).color(color).endVertex();
            }

            tessellator.draw();
         }
      }
   }

   private void drawTexturedCube(AxisAlignedBB axisAlignedBB, Charming.ResourceLocationWithSizes skinLoc, int color, Tessellator tessellator) {
      if (skinLoc != null) {
         mc.getTextureManager().bindTexture(skinLoc.getResource());
         int[] var5 = this.sides;
         int var6 = var5.length;

         for (int var7 = 0; var7 < var6; var7++) {
            Integer sideNum = var5[var7];
            this.drawTexturedCubeSide(axisAlignedBB, skinLoc, sideNum, color, tessellator, true);
         }
      }
   }

   private AxisAlignedBB axisOfPos(Vec3d pos, float offset) {
      return new AxisAlignedBB(pos).expandXyz((double)offset);
   }

   private void drawFartParticle(Charming.FartParticle fartParticle, int color, float lineWidth) {
      if (color != 0) {
         float timePC = 1.0F - fartParticle.getTimePC();
         float timePC010 = (timePC > 0.5F ? 1.0F - timePC : timePC) * 2.0F;
         color = ColorUtils.swapAlpha(color, (float)ColorUtils.getAlphaFromColor(color) * timePC010);
         if (color != 0) {
            RenderUtils.glColor(color);
            fartParticle.addGLVertexes(lineWidth);
            GlStateManager.resetColor();
         }
      }
   }

   @Override
   public void onUpdate() {
      if (this.charm != null
         && Minecraft.player.ticksExisted != 1
         && (this.charm == null || !(Minecraft.player.getDistance(this.charm.posX, this.charm.posY, this.charm.posZ) > 70.0))) {
         if (this.charm != null) {
            String loc = this.CharmModel.currentMode.toLowerCase();
            if (this.CharmModel.currentMode.equalsIgnoreCase("Reactive")) {
               loc = loc + this.ReactiveSkin.currentMode.toLowerCase();
            }

            this.reinitActiveResourceGroup(loc);
            this.charm.updateTexture(this.ACTIVE_RESOURCE_LOCATIONS);
            this.charm.update();
         }
      } else {
         this.charm = new Charming.Charm(
            Minecraft.player
               .getPositionVector()
               .addVector(
                  -Math.sin(Math.toRadians((double)Minecraft.player.rotationYaw)) * 5.0,
                  0.0,
                  Math.cos(Math.toRadians((double)Minecraft.player.rotationYaw)) * 5.0
               ),
            Minecraft.player,
            3,
            this.ACTIVE_RESOURCE_LOCATIONS
         );
      }
   }

   @Override
   public void alwaysRender3D(float partialTicks) {
      if (this.isActived() && this.stateAnim.to != 1.0F) {
         this.stateAnim.to = 1.0F;
      } else if (!this.isActived() && this.stateAnim.to != 0.0F) {
         this.stateAnim.to = 0.0F;
      }

      float alphaPC = this.stateAnim.getAnim();
      if (alphaPC > 0.97F) {
         alphaPC = 1.0F;
      } else if (alphaPC < 0.03F) {
         alphaPC = 0.0F;
      }

      if (alphaPC == 0.0F) {
         if (this.charm != null) {
            this.charm = null;
            this.ACTIVE_RESOURCE_LOCATIONS = null;
         }
      } else {
         if (this.charm != null) {
            this.charm.drawCharm(partialTicks, true, alphaPC);
         }
      }
   }

   private class Charm {
      public double posX;
      public double posY;
      public double posZ;
      private double prevPosX;
      private double prevPosY;
      private double prevPosZ;
      private double speedXZ;
      private double speedY;
      private float prevRotationYaw;
      private float prevRotationPitch;
      private float rotationYaw;
      private float rotationPitch;
      private final float rotationSpeed = 0.1F;
      private boolean boost;
      private boolean angry;
      private boolean moving;
      private Entity folowEntity;
      private int ticksAlive;
      private final int fartOnceInTicks;
      private final List<Charming.FartParticle> particles = new ArrayList<>();
      private final Tessellator tessellator = Tessellator.getInstance();
      private Charming.ResourceLocationWithSizes TEXTURE_WITH_SIZES;
      AnimationUtils breathAnim = new AnimationUtils(0.0F, 0.0F, 0.0F);
      private final ResourceLocation BLOOM_TEX = new ResourceLocation("vegaline/modules/charming/bloom.png");

      public Charm(Vec3d spawnPos, Entity folowEntity, int fartOnceInTicks, Charming.ResourceLocationWithSizes textureWithSizes) {
         this.posX = spawnPos.xCoord;
         this.posY = spawnPos.yCoord;
         this.posZ = spawnPos.zCoord;
         this.prevPosX = this.posX;
         this.prevPosY = this.posY;
         this.prevPosZ = this.posZ;
         if (folowEntity != null) {
            this.folowEntity = folowEntity;
            float[] rot = RotationUtil.getVecNeeded(folowEntity.getPositionVector(), this.getLookVec());
            this.setLook(rot);
            this.prevRotationYaw = this.rotationYaw;
            this.prevRotationPitch = this.rotationPitch;
         }

         this.fartOnceInTicks = fartOnceInTicks;
         this.TEXTURE_WITH_SIZES = textureWithSizes;
      }

      public float updateBreathAnim() {
         float anim = this.breathAnim.anim;
         float speed = 0.015F + (float)this.speedXZ / 15.0F;
         float temp = 0.2F;
         this.breathAnim.speed = speed;
         if (anim > 1.0F - temp) {
            this.breathAnim.to = 0.0F;
         } else if (anim < temp) {
            this.breathAnim.to = 1.0F;
         }

         return this.breathAnim.getAnim();
      }

      private void updateFartParticles() {
         if (!this.particles.isEmpty()) {
            this.particles.removeIf(Charming.FartParticle::toRemove);
         }

         if (this.ticksAlive % this.fartOnceInTicks == this.fartOnceInTicks - 1) {
            this.particles.add(Charming.this.new FartParticle(675.0F, 0.12F, 0.025F, -0.1425F));
         }
      }

      private Vec3d getLookVec() {
         double radian = Math.toRadians((double)this.rotationYaw);
         double dst = 0.15;
         return new Vec3d(this.posX - Math.sin(radian) * dst, this.posY, this.posZ + Math.cos(radian) * dst);
      }

      private float[] getLook() {
         return new float[]{this.rotationYaw, this.rotationPitch};
      }

      private void setLook(float[] rot) {
         this.rotationYaw = rot[0];
         this.rotationPitch = rot[1];
      }

      private float lerpAngle(float start, float end, float amount) {
         float minAngle = (end - start + 180.0F) % 360.0F - 180.0F;
         return minAngle * amount + start;
      }

      private float[] lerpLook(float[] rot, float lerpSpeed) {
         this.rotationYaw = MathUtils.lerp(this.rotationYaw, rot[0], lerpSpeed);
         this.rotationPitch = this.lerpAngle(this.rotationPitch, rot[1], lerpSpeed);
         return this.getLook();
      }

      private double[] updateSpeeds() {
         if (this.folowEntity == null) {
            this.speedXZ /= 1.8F;
         } else {
            double dx = this.posX - this.folowEntity.posX;
            double dz = this.posZ - this.folowEntity.posZ;
            double curDst = this.folowEntity instanceof EntityItem
               ? 0.5
               : (this.folowEntity instanceof EntityPlayerSP ? 0.6F + MoveMeHelp.getSpeed() * 5.0 : (double)(this.folowEntity.width * 1.25F));
            double dst = Math.sqrt(dx * dx + dz * dz);
            double curSpeed = MathUtils.clamp((dst - curDst * 1.5) / curDst / 5.0, -0.4F, 1.0);
            float lerpSpeed = curSpeed < this.speedXZ ? 0.5F : 0.1F;
            if (this.folowEntity instanceof EntityItem) {
               curSpeed *= Math.abs(curSpeed);
               curSpeed *= Math.abs(curSpeed);
            }

            if (dst - curDst < curDst) {
               this.speedXZ /= 1.1F;
            }

            this.speedXZ = MathUtils.lerp(this.speedXZ, curSpeed, (double)lerpSpeed);
         }

         if (this.folowEntity == null) {
            this.speedY /= 1.8F;
         } else {
            double dy = this.folowEntity.posY - this.posY + (double)(this.folowEntity instanceof EntityItem ? 0.1F : this.folowEntity.getEyeHeight() * 0.9F);
            double curDstx = this.folowEntity instanceof EntityLivingBase ? 0.05F : 0.15F;
            curDstx /= 2.0;
            float lerpSpeedx = 0.5F;
            double curSpeedx = MathUtils.clamp((dy - curDstx * 2.0) / curDstx / 100.0, -1.0, 1.0);
            curSpeedx *= Math.abs(curSpeedx);
            curSpeedx /= 2.0;
            this.speedY = MathUtils.lerp(this.speedY, curSpeedx, (double)lerpSpeedx);
         }

         return new double[]{this.speedXZ, this.speedY};
      }

      private boolean updateMovingStatus() {
         return this.moving = this.speedXZ > 0.02F || this.speedY > 0.05F;
      }

      private float[] updateLook() {
         float[] look = this.getLook();
         this.prevRotationYaw = look[0];
         this.prevRotationPitch = look[1];
         if (this.moving) {
            look[1] = (float)MathUtils.clamp((MathUtils.clamp(-this.speedY, -1.0, 0.0) + this.speedXZ / 4.0) * 180.0, -70.0, 70.0);
            if (look[1] < -10.0F) {
               look[0] = RotationUtil.getVecNeeded(this.folowEntity.getPositionEyes(1.0F), this.getLookVec())[0];
            } else {
               look[0] = (float)MoveMeHelp.getDirDiffOfMotionsNoAbs(this.posX - this.prevPosX, this.posZ - this.prevPosZ, 0.0);
            }
         } else {
            look[1] = 0.0F;
            if (this.folowEntity != null) {
               float yaw = RotationUtil.getVecNeeded(this.folowEntity.getPositionEyes(1.0F), this.getLookVec())[0];
               if (RotationUtil.getAngleDifference(yaw, this.rotationYaw) >= 5.0F) {
                  look[0] = this.lerpAngle(look[0], yaw, RotationUtil.getAngleDifference(look[0], yaw) / 15.0F);
               }
            }
         }

         return this.lerpLook(look, this.folowEntity == null ? 0.2F : 0.15F);
      }

      public void setFollower(Entity followEntity) {
         this.folowEntity = followEntity;
      }

      private void updateFollower() {
         if (HitAura.TARGET_ROTS != null && (double)Minecraft.player.getDistanceToEntity(HitAura.TARGET_ROTS) < 20.0) {
            this.folowEntity = HitAura.TARGET_ROTS;
         } else {
            Entity firstItemEntity = Module.mc.world.getLoadedEntityList().stream().<Entity>map(Entity::getItemOf).filter(Objects::nonNull).filter(item -> {
               double dxx = this.posX - item.posX;
               double dyx = this.posY - item.posY;
               double dzx = this.posZ - item.posZ;
               double dx2 = item.posX - Minecraft.player.posX;
               double dy2 = item.posY - Minecraft.player.posY;
               double dz2 = item.posZ - Minecraft.player.posZ;
               return Math.sqrt(dxx * dxx + dyx * dyx + dzx * dzx) < 7.0 && Math.sqrt(dx2 * dx2 + dy2 * dy2 + dz2 * dz2) < 9.0;
            }).findAny().orElse(null);
            if (firstItemEntity != null) {
               this.folowEntity = firstItemEntity;
            } else if (Minecraft.player != null) {
               double dx = this.posX - Minecraft.player.posX;
               double dy = this.posY - Minecraft.player.posY;
               double dz = this.posZ - Minecraft.player.posZ;
               if (Math.sqrt(dx * dx + dy * dy + dz * dz) < 70.0) {
                  this.folowEntity = Minecraft.player;
               }
            }
         }
      }

      private void updateMovement() {
         this.prevPosX = this.posX;
         this.prevPosY = this.posY;
         this.prevPosZ = this.posZ;
         float rotationYaw = this.rotationYaw;
         if (this.folowEntity != null) {
            float[] rot = RotationUtil.getVecNeeded(this.folowEntity.getPositionEyes(1.0F), this.getLookVec());
            rotationYaw = rot[0];
         }

         double motionX = -Math.sin(Math.toRadians((double)rotationYaw)) * this.speedXZ;
         double motionY = this.speedY;
         double motionZ = Math.cos(Math.toRadians((double)rotationYaw)) * this.speedXZ;
         Vec3d pos = new Vec3d(this.posX, this.posY, this.posZ);
         if (Module.mc.world != null) {
            float offsetBox = 0.225F;
            if (Module.mc.world.getCollisionBoxes(null, Charming.this.axisOfPos(pos.addVector(motionX, 0.0, 0.0), 0.25F)).isEmpty()) {
               this.posX += motionX;
            }

            boolean collideV = !Module.mc.world.getCollisionBoxes(null, Charming.this.axisOfPos(pos.addVector(0.0, motionY, 0.0), offsetBox)).isEmpty();
            if (motionY > 0.05 || !collideV) {
               this.posY += motionY * (double)(collideV ? 3 : 1);
            } else if (collideV && Module.mc.world.getCollisionBoxes(null, Charming.this.axisOfPos(pos.addVector(0.0, 1.0, 0.0), offsetBox)).isEmpty()) {
               this.posY += (double)(offsetBox / 4.0F);
            }

            if (Module.mc.world.getCollisionBoxes(null, Charming.this.axisOfPos(pos.addVector(0.0, 0.0, motionZ), offsetBox)).isEmpty()) {
               this.posZ += motionZ;
            }
         }
      }

      private void updateTexture(Charming.ResourceLocationWithSizes resourceLocationWithSizes) {
         this.TEXTURE_WITH_SIZES = resourceLocationWithSizes;
      }

      public void update() {
         this.ticksAlive++;
         this.updateFollower();
         this.updateSpeeds();
         this.updateMovingStatus();
         this.updateLook();
         this.updateFartParticles();
         this.updateMovement();
      }

      private float calcLineWidthFromDistance() {
         double dx = this.posX - RenderManager.renderPosX;
         double dy = this.posY - RenderManager.renderPosY;
         double dz = this.posZ - RenderManager.renderPosZ;
         return (float)MathUtils.clamp(1.0 - Math.sqrt(dx * dx + dy * dy + dz * dz) / 60.0, 0.0, 2.0);
      }

      private void drawFartParticles(int color, float lineWidth) {
         if (color != 0) {
            this.particles.forEach(fartParticle -> Charming.this.drawFartParticle(fartParticle, color, lineWidth));
         }
      }

      private void drawCharm(float pTicks, boolean renderFartParticles, float alphaPC) {
         alphaPC *= MathUtils.clamp(((float)this.ticksAlive + pTicks) / 20.0F, 0.0F, 1.0F);
         if (alphaPC != 0.0F) {
            double x = this.getRenderPositionX(pTicks);
            double y = this.getRenderPositionY(pTicks);
            double z = this.getRenderPositionZ(pTicks);
            double yaw = this.getRenderYaw(pTicks);
            double pitch = this.getRenderPitch(pTicks);
            float offset = 0.125F;
            AxisAlignedBB axisAlignedBB = Charming.this.axisOfPos(Vec3d.ZERO, offset);
            Frustum frustum = new Frustum(Module.mc.getRenderViewEntity().posX, Module.mc.getRenderViewEntity().posY, Module.mc.getRenderViewEntity().posZ);
            if (frustum.isBoundingBoxInFrustum(axisAlignedBB.expandXyz((double)(offset / 1.5F)).addCoord(x, y, z))) {
               int color = ColorUtils.swapAlpha(ColorUtils.getColor(105, 100, 255, 255.0F * alphaPC), 255.0F * alphaPC);
               int texColor = ColorUtils.swapAlpha(-1, 255.0F * alphaPC);
               int bloomCol = ColorUtils.swapAlpha(ColorUtils.getOverallColorFrom(color, texColor), (float)ColorUtils.getAlphaFromColor(texColor) * 0.25F);
               float bloomSize = offset * 5.5F;
               GL11.glPushMatrix();
               GL11.glDisable(2896);
               GL11.glEnable(3042);
               GL11.glAlphaFunc(516, 0.003921569F);
               GL11.glDisable(2884);
               Module.mc.entityRenderer.disableLightmap();
               if (renderFartParticles) {
                  GL11.glPushMatrix();
                  GL11.glDisable(3553);
                  GL11.glDepthMask(false);
                  GL11.glTranslated(-RenderManager.renderPosX, -RenderManager.renderPosY, -RenderManager.renderPosZ);
                  GL11.glBlendFunc(770, 32772);
                  GL11.glTranslated(x, y, z);
                  GL11.glRotated(yaw, 0.0, -1.0, 0.0);
                  GL11.glRotated(pitch, 1.0, 0.0, 0.0);
                  GL11.glTranslated(0.0, (double)(-offset) * 1.25, 0.0);
                  float lineW = this.calcLineWidthFromDistance();
                  this.drawFartParticles(color, lineW);
                  this.drawFartParticles(ColorUtils.swapAlpha(color, (float)ColorUtils.getAlphaFromColor(color) * alphaPC * 0.2F), lineW + 8.0F);
                  this.drawFartParticles(ColorUtils.swapAlpha(color, (float)ColorUtils.getAlphaFromColor(color) * alphaPC * 0.1F), lineW + 15.0F);
                  GL11.glPopMatrix();
               }

               GL11.glDepthMask(true);
               GL11.glBlendFunc(770, 771);
               GL11.glEnable(3553);
               GL11.glTranslated(-RenderManager.renderPosX + x, -RenderManager.renderPosY + y, -RenderManager.renderPosZ + z);
               GL11.glRotated(yaw, 0.0, -1.0, 0.0);
               GL11.glRotated(pitch, 1.0, 0.0, 0.0);
               Charming.this.drawTexturedCube(axisAlignedBB, this.TEXTURE_WITH_SIZES, texColor, this.tessellator);
               GL11.glPopMatrix();
               Module.mc.getTextureManager().bindTexture(this.BLOOM_TEX);
               GL11.glPushMatrix();
               GL11.glDepthMask(false);
               GL11.glBlendFunc(770, 1);
               GL11.glTranslated(-RenderManager.renderPosX + x, -RenderManager.renderPosY + y, -RenderManager.renderPosZ + z);
               GL11.glRotated((double)(Module.mc.getRenderManager().playerViewY + WorldRender.get.offYawOrient), 0.0, -1.0, 0.0);
               GL11.glRotated(
                  (double)(Module.mc.getRenderManager().playerViewX + WorldRender.get.offPitchOrient),
                  Module.mc.gameSettings.thirdPersonView == 2 ? -1.0 : 1.0,
                  0.0,
                  0.0
               );
               GL11.glScalef(bloomSize, bloomSize, bloomSize);
               this.tessellator.getBuffer().begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
               this.tessellator.getBuffer().pos(-0.5, -0.5).tex(0.0, 0.0).color(bloomCol).endVertex();
               this.tessellator.getBuffer().pos(0.5, -0.5).tex(1.0, 0.0).color(bloomCol).endVertex();
               this.tessellator.getBuffer().pos(0.5, 0.5).tex(1.0, 1.0).color(bloomCol).endVertex();
               this.tessellator.getBuffer().pos(-0.5, 0.5).tex(0.0, 1.0).color(bloomCol).endVertex();
               this.tessellator.draw();
               GL11.glBlendFunc(770, 771);
               GL11.glDepthMask(true);
               GL11.glEnable(2884);
               GL11.glPopMatrix();
            }
         }
      }

      public double getRenderPositionX(float pTicks) {
         return MathUtils.lerp(this.posX, this.prevPosX, (double)(1.0F - pTicks));
      }

      public double getRenderPositionY(float pTicks) {
         float anim = this.updateBreathAnim();
         anim *= anim;
         return MathUtils.lerp(this.posY, this.prevPosY, (double)(1.0F - pTicks)) + (double)(anim * (0.015F + (float)this.speedXZ / 15.0F));
      }

      public double getRenderPositionZ(float pTicks) {
         return MathUtils.lerp(this.posZ, this.prevPosZ, (double)(1.0F - pTicks));
      }

      public double getRenderYaw(float pTicks) {
         return (double)MathUtils.lerp(this.rotationYaw, this.prevRotationYaw, 1.0F - pTicks);
      }

      public double getRenderPitch(float pTicks) {
         float anim = this.updateBreathAnim();
         anim = -0.25F + anim * 0.75F;
         return (double)(MathUtils.lerp(this.rotationPitch, this.prevRotationPitch, 1.0F - pTicks) + anim * 3.0F);
      }
   }

   private class FartParticle {
      long startTime = System.currentTimeMillis();
      float maxTime;
      float startRadius;
      float endRadius;
      float offsetY;

      public FartParticle(float maxTime, float startRadius, float endRadius, float offsetY) {
         this.maxTime = maxTime;
         this.startRadius = startRadius;
         this.endRadius = endRadius;
         this.offsetY = offsetY;
      }

      private float getTimePC() {
         return MathUtils.clamp((float)(System.currentTimeMillis() - this.startTime) / this.maxTime, 0.0F, 1.0F);
      }

      public List<Vec3d> getVertexes(float timePC) {
         float yOffset = this.offsetY * timePC;
         float radius = MathUtils.lerp(this.startRadius, this.endRadius, timePC);
         return IntStream.rangeClosed(0, 30).mapToObj(index -> {
            double radian = Math.toRadians((double)(index * 12));
            double sin = Math.sin(radian) * (double)radius;
            double cos = Math.cos(radian) * (double)radius;
            return new Vec3d(sin, (double)yOffset, cos);
         }).collect(Collectors.toList());
      }

      public void addGLVertexes(float lineWidth) {
         GL11.glEnable(2848);
         GL11.glHint(3154, 4354);
         GL11.glLineWidth(Float.valueOf(0.001F + lineWidth * (1.0F - this.getTimePC())));
         GL11.glBegin(3);
         this.getVertexes(this.getTimePC()).forEach(vec -> GL11.glVertex3d(vec.xCoord, vec.yCoord, vec.zCoord));
         GL11.glEnd();
         GL11.glLineWidth(1.0F);
         GL11.glHint(3154, 4352);
         GL11.glDisable(2848);
      }

      public boolean toRemove() {
         return this.getTimePC() == 1.0F;
      }
   }

   private class ResourceLocationWithSizes {
      private final ResourceLocation source;
      private final int[] resolution;

      private ResourceLocationWithSizes(ResourceLocation source) {
         this.source = source;
         this.resolution = Charming.this.getTextureResolution(source);
      }

      private ResourceLocation getResource() {
         return this.source;
      }

      private int[] getResolution() {
         return this.resolution;
      }
   }
}
