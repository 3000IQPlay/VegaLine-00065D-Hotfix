package ru.govno.client.module.modules;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemEgg;
import net.minecraft.item.ItemEnderPearl;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemLingeringPotion;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemSnowball;
import net.minecraft.item.ItemSplashPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;
import ru.govno.client.module.Module;
import ru.govno.client.utils.Math.MathUtils;
import ru.govno.client.utils.Render.ColorUtils;
import ru.govno.client.utils.Render.RenderUtils;

public class ThrowDirs extends Module {
   private final Random rand = new Random();
   private final double[] randoms = new double[]{0.0, 0.0, 0.0};
   private List<EntityLivingBase> toRenderEntities = new ArrayList<>();

   public ThrowDirs() {
      super("ThrowDirs", 0, Module.Category.RENDER);
   }

   private boolean itemIsCorrectToThrow(Item itemIn) {
      return itemIn instanceof ItemBow
         || itemIn instanceof ItemSnowball
         || itemIn instanceof ItemEgg
         || itemIn instanceof ItemEnderPearl
         || itemIn instanceof ItemSplashPotion
         || itemIn instanceof ItemLingeringPotion
         || itemIn instanceof ItemFishingRod;
   }

   private ThrowDirs.ItemStackWithHand getCorrectThrowStackOfEntity(EntityLivingBase entityOf) {
      return entityOf == null
         ? null
         : (
            this.itemIsCorrectToThrow(entityOf.getHeldItemMainhand().getItem())
               ? new ThrowDirs.ItemStackWithHand(entityOf.getHeldItemMainhand(), EnumHand.MAIN_HAND)
               : (
                  this.itemIsCorrectToThrow(entityOf.getHeldItemOffhand().getItem())
                     ? new ThrowDirs.ItemStackWithHand(entityOf.getHeldItemOffhand(), EnumHand.OFF_HAND)
                     : null
               )
         );
   }

   private List<Vec3d> getPointsOfThrowable(World worldIn, ThrowDirs.ItemStackWithHand handStack, float partialTicks, EntityLivingBase entityOf, int maxDensity) {
      List<Vec3d> vecs = new ArrayList<>();
      if (handStack != null && entityOf != null && worldIn != null && worldIn.isBlockLoaded(entityOf.getPosition().down((int)entityOf.posY - 1))) {
         Item item = handStack.getItemStack().getItem();
         if (this.itemIsCorrectToThrow(item)) {
            boolean calcTight = item instanceof ItemBow;
            if (!calcTight || entityOf.isBowing()) {
               float throwFactor = calcTight ? 1.0F : 0.4F;
               double[] selfHeadRotateWR = new double[]{
                  (double)(entityOf.prevRotationYawHead + (entityOf.rotationYawHead - entityOf.prevRotationYawHead) * partialTicks),
                  (double)(entityOf.prevRotationPitchHead + (entityOf.rotationPitchHead - entityOf.prevRotationPitchHead) * partialTicks),
                  Math.toRadians((double)(entityOf.prevRotationYawHead + (entityOf.rotationYawHead - entityOf.prevRotationYawHead) * partialTicks)),
                  Math.toRadians((double)(entityOf.prevRotationPitchHead + (entityOf.rotationPitchHead - entityOf.prevRotationPitchHead) * partialTicks))
               };
               Vec3d playerVector = new Vec3d(
                  entityOf.lastTickPosX + (entityOf.posX - entityOf.lastTickPosX) * (double)partialTicks,
                  entityOf.lastTickPosY + (entityOf.posY - entityOf.lastTickPosY) * (double)partialTicks + (double)entityOf.getEyeHeight(),
                  entityOf.lastTickPosZ + (entityOf.posZ - entityOf.lastTickPosZ) * (double)partialTicks
               );
               double offsetStartDir = handStack.getEnumHand() == EnumHand.MAIN_HAND ? 1.0 : -1.0;
               double throwOfX = playerVector.xCoord - Math.cos(selfHeadRotateWR[2]) * 0.1 * offsetStartDir;
               double throwOfY = playerVector.yCoord;
               double throwOfZ = playerVector.zCoord - Math.sin(selfHeadRotateWR[2]) * 0.1 * offsetStartDir;
               double shiftX = -Math.sin(selfHeadRotateWR[2]) * Math.cos(selfHeadRotateWR[3]) * (double)throwFactor;
               double shiftY = -Math.sin(selfHeadRotateWR[3]) * (double)throwFactor + entityOf.posY - entityOf.lastTickPosY;
               double shiftZ = Math.cos(selfHeadRotateWR[2]) * Math.cos(selfHeadRotateWR[3]) * (double)throwFactor;
               double throwMotion = Math.sqrt(shiftX * shiftX + shiftY * shiftY + shiftZ * shiftZ);
               if (calcTight && entityOf instanceof EntityPlayerSP) {
                  shiftX += this.randoms[0];
                  shiftY += this.randoms[1];
                  shiftZ += this.randoms[2];
               }

               shiftX /= throwMotion;
               shiftY /= throwMotion;
               shiftZ /= throwMotion;
               if (calcTight) {
                  float tightPower = (72000.0F - (float)entityOf.getItemInUseCount() + partialTicks) / 20.0F;
                  tightPower = (tightPower * tightPower + tightPower * 2.0F) / 3.0F;
                  tightPower = tightPower < 0.1F ? 1.0F : (tightPower > 1.0F ? 1.0F : tightPower);
                  tightPower *= 3.0F;
                  shiftX *= (double)tightPower;
                  shiftY *= (double)tightPower;
                  shiftZ *= (double)tightPower;
               } else {
                  shiftX *= 1.5;
                  shiftY *= 1.5;
                  shiftZ *= 1.5;
               }

               double gravityFactor = calcTight ? 0.005 : (item instanceof ItemPotion ? 0.04 : (item instanceof ItemFishingRod ? 0.015 : 0.003));

               while (maxDensity > 0) {
                  vecs.add(new Vec3d(throwOfX, throwOfY, throwOfZ));
                  throwOfX += shiftX * 0.1;
                  throwOfY += shiftY * 0.1;
                  throwOfZ += shiftZ * 0.1;
                  double asellate = 0.999;
                  shiftX *= asellate;
                  shiftY = shiftY * asellate - gravityFactor;
                  shiftZ *= asellate;
                  if (worldIn.rayTraceBlocks(playerVector, new Vec3d(throwOfX, throwOfY, throwOfZ)) != null) {
                     break;
                  }

                  maxDensity--;
               }
            }
         }
      }

      return vecs;
   }

   private List<Vec3d> getThowingVecsListOfEntity(EntityLivingBase entityFor) {
      return this.getPointsOfThrowable(mc.world, this.getCorrectThrowStackOfEntity(entityFor), mc.getRenderPartialTicks(), entityFor, 500);
   }

   private void start3DRendering(boolean ignoreDepth, RenderManager renderMngr) {
      GL11.glPushMatrix();
      GL11.glEnable(2848);
      GL11.glBlendFunc(770, 32772);
      GL11.glEnable(3042);
      GL11.glDisable(3553);
      if (ignoreDepth) {
         GL11.glDisable(2929);
      } else {
         GL11.glEnable(2929);
      }

      GL11.glDisable(3008);
      GL11.glShadeModel(7425);
      GL11.glHint(3154, 4354);
      GL11.glEnable(2832);
      GL11.glDepthMask(false);
      GL11.glPointSize(2.0F);
      GL11.glLineWidth(1.8F);
      GL11.glDisable(2896);
      GL11.glTranslated(-RenderManager.renderPosX, -RenderManager.renderPosY, -RenderManager.renderPosZ);
   }

   private void end3DRendering() {
      RenderUtils.resetBlender();
      GL11.glBlendFunc(770, 771);
      GL11.glLineWidth(1.0F);
      GL11.glPointSize(1.0F);
      GL11.glDepthMask(true);
      GL11.glHint(3154, 4352);
      GL11.glShadeModel(7424);
      GL11.glEnable(3008);
      GL11.glEnable(2929);
      GL11.glPopMatrix();
   }

   private void renderLineBegin(List<Vec3d> vecs, int color1, int color2, float alphaPC, float alphaPass) {
      int max = vecs.size();
      alphaPC *= MathUtils.clamp((float)max / 50.0F, 0.0F, 1.0F);
      if (alphaPC != 0.0F) {
         color1 = ColorUtils.swapAlpha(color1, (float)ColorUtils.getAlphaFromColor(color1) * alphaPC);
         color2 = ColorUtils.swapAlpha(color2, (float)ColorUtils.getAlphaFromColor(color2) * alphaPC);
         GL11.glBegin(3);
         int index = 0;
         int lastColor = 0;
         Vec3d last = null;

         for (Vec3d vec : vecs) {
            float pcOfStartVec = (float)index / (float)max;
            float pcOfMiddleVec = (float)Math.abs(index - max / 2) / ((float)max / 2.0F);
            float darkPass = 1.0F - pcOfMiddleVec * alphaPass;
            int currentColor = lastColor = ColorUtils.getOverallColorFrom(color1, color2, pcOfStartVec);
            int finalColor = ColorUtils.swapAlpha(currentColor, (float)ColorUtils.getAlphaFromColor(currentColor) * darkPass);
            if (ColorUtils.getAlphaFromColor(finalColor) >= 1) {
               RenderUtils.glColor(ColorUtils.toDark(finalColor, darkPass));
               GL11.glVertex3d(vec.xCoord, vec.yCoord, vec.zCoord);
            }

            index++;
            last = vec;
         }

         GL11.glEnd();
         if (lastColor != 0 && last != null) {
            if (ColorUtils.getAlphaFromColor(lastColor) < 60) {
               lastColor = ColorUtils.swapAlpha(lastColor, 60.0F);
            }

            RenderUtils.glColor(lastColor);
            GL11.glBegin(0);
            GL11.glVertex3d(last.xCoord, last.yCoord, last.zCoord);
            GL11.glEnd();
         }

         GlStateManager.resetColor();
      }
   }

   @Override
   public void onToggled(boolean actived) {
      this.stateAnim.to = this.actived ? 1.0F : 0.0F;
      super.onToggled(actived);
   }

   private float getAlphaPC() {
      this.stateAnim.to = this.actived ? 1.0F : 0.0F;
      float aPC = this.stateAnim.getAnim();
      return aPC < 0.03F ? 0.0F : (aPC > 0.97F ? 1.0F : aPC);
   }

   @Override
   public void onUpdate() {
      if (!this.toRenderEntities.isEmpty() && Minecraft.player != null && this.toRenderEntities.stream().anyMatch(base -> base.equals(Minecraft.player))) {
         this.randoms[0] = this.rand.nextGaussian() * 0.0075F;
      }

      this.randoms[1] = this.rand.nextGaussian() * 0.0075F;
      this.randoms[2] = this.rand.nextGaussian() * 0.0075F;
   }

   @Override
   public void alwaysRender3D() {
      float alphaPC = this.getAlphaPC();
      if (alphaPC != 0.0F) {
         this.toRenderEntities = mc.world
            .getLoadedEntityList()
            .stream()
            .map(Entity::getLivingBaseOf)
            .filter(Objects::nonNull)
            .filter(EntityLivingBase::isEntityAlive)
            .filter(base -> base instanceof EntityPlayerSP || RenderUtils.isInView(base))
            .collect(Collectors.toList());
         if (!this.toRenderEntities.isEmpty()) {
            List<List<Vec3d>> pageVecLists = new ArrayList<>();

            for (EntityLivingBase entity : this.toRenderEntities) {
               List<Vec3d> vecsList = this.getThowingVecsListOfEntity(entity);
               if (!vecsList.isEmpty()) {
                  pageVecLists.add(vecsList);
               }
            }

            if (!pageVecLists.isEmpty()) {
               int color1 = -1;
               int color2 = ColorUtils.getColor(255, 0, 0);
               float alphaPass = 0.9F;
               this.start3DRendering(false, mc.getRenderManager());
               pageVecLists.forEach(list -> {
                  if (list.size() > 1 && list.get(0).distanceTo(list.get(list.size() - 1)) > 1.0) {
                     this.renderLineBegin((List<Vec3d>)list, -1, color2, alphaPC, 0.9F);
                  }
               });
               this.end3DRendering();
            }
         }
      }
   }

   private class ItemStackWithHand {
      private final ItemStack stack;
      private final EnumHand hand;

      private ItemStackWithHand(ItemStack stack, EnumHand hand) {
         this.stack = stack;
         this.hand = hand;
      }

      public ItemStack getItemStack() {
         return this.stack;
      }

      public EnumHand getEnumHand() {
         return this.hand;
      }
   }
}
