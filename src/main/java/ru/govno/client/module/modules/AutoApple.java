package ru.govno.client.module.modules;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemAppleGold;
import net.minecraft.item.ItemEnderEye;
import net.minecraft.item.ItemEnderPearl;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemLingeringPotion;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemShield;
import net.minecraft.item.ItemSnowball;
import net.minecraft.item.ItemSplashPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import ru.govno.client.module.Module;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.module.settings.FloatSettings;
import ru.govno.client.newfont.CFontRenderer;
import ru.govno.client.newfont.Fonts;
import ru.govno.client.utils.Math.MathUtils;
import ru.govno.client.utils.Render.AnimationUtils;
import ru.govno.client.utils.Render.ColorUtils;
import ru.govno.client.utils.Render.RenderUtils;
import ru.govno.client.utils.Render.StencilUtil;

public class AutoApple extends Module {
   public static AutoApple get;
   FloatSettings MinHealth;
   BoolSettings OnlyTargetStrafe;
   BoolSettings PlusAbsorb;
   public boolean pverAllowUseInput;
   public boolean wantToEat;
   public AnimationUtils scaleAnimation = new AnimationUtils(0.0F, 0.0F, 0.05F);
   public AnimationUtils triggerAnimation = new AnimationUtils(0.0F, 0.0F, 0.2F);
   private ItemStack lastUsedStack = new ItemStack(Items.GOLDEN_APPLE, 1);

   public AutoApple() {
      super("AutoApple", 0, Module.Category.COMBAT);
      get = this;
      this.settings.add(this.MinHealth = new FloatSettings("MinHealth", 14.5F, 20.0F, 5.0F, this));
      this.settings.add(this.OnlyTargetStrafe = new BoolSettings("OnlyTargetStrafe", true, this));
      this.settings.add(this.PlusAbsorb = new BoolSettings("PlusAbsorb", true, this));
   }

   @Override
   public String getDisplayName() {
      return this.name
         + this.getSuff()
         + this.MinHealth.getInt()
         + "H"
         + (
            Minecraft.player == null
               ? ""
               : (Minecraft.player.getHeldItemOffhand().getItem() instanceof ItemAppleGold ? " " + Minecraft.player.getHeldItemOffhand().stackSize + "G" : "")
         );
   }

   private boolean healthWarning() {
      return Minecraft.player.getHealth() + (get.PlusAbsorb.getBool() ? Minecraft.player.getAbsorptionAmount() : 0.0F) < get.MinHealth.getFloat();
   }

   public boolean canEat() {
      return (!get.OnlyTargetStrafe.getBool() || TargetStrafe.goStrafe())
         && Minecraft.player.getHeldItemOffhand().getItem() instanceof ItemAppleGold
         && !Minecraft.player.isBlocking()
         && !Minecraft.player.isDrinking()
         && this.healthWarning()
         && !Minecraft.player.isCreative()
         && !this.itemStackCanBeUsed(Minecraft.player.getHeldItemMainhand());
   }

   private void stopProcessEat() {
      if (this.wantToEat) {
         if (!Mouse.isButtonDown(1)) {
            mc.playerController.onStoppedUsingItem(Minecraft.player);
            Minecraft.player.resetActiveHand();
            mc.gameSettings.keyBindUseItem.pressed = false;
         }

         if (mc.currentScreen != null) {
            mc.currentScreen.allowUserInput = this.pverAllowUseInput;
         }

         this.wantToEat = false;
      }
   }

   private boolean itemStackCanBeUsed(ItemStack stack) {
      if (stack == null) {
         return false;
      } else {
         Item item = stack.getItem();
         return item != null
            && item != Items.air
            && (
               !Minecraft.player.isCreative() && !Minecraft.player.isSpectator() && item instanceof ItemFood
                  || item instanceof ItemShield
                  || item instanceof ItemPotion
                  || item instanceof ItemSplashPotion
                  || item instanceof ItemLingeringPotion
                  || item instanceof ItemEnderPearl
                  || item instanceof ItemEnderEye
                  || item instanceof ItemSnowball
            );
      }
   }

   private void processEat() {
      if (this.canEat() && Minecraft.player != null && !Minecraft.player.isDead && get.actived) {
         if (!Minecraft.player.isHandActive() && !mc.gameSettings.keyBindUseItem.pressed) {
            mc.playerController.processRightClick(Minecraft.player, mc.world, EnumHand.OFF_HAND);
            mc.gameSettings.keyBindUseItem.pressed = true;
            Minecraft.player.setActiveHand(EnumHand.OFF_HAND);
            this.wantToEat = true;
         }

         if (mc.currentScreen != null && !mc.currentScreen.allowUserInput) {
            this.pverAllowUseInput = mc.currentScreen.allowUserInput;
            mc.currentScreen.allowUserInput = true;
         }
      } else {
         this.stopProcessEat();
      }
   }

   @Override
   public void onUpdate() {
      this.processEat();
   }

   @Override
   public void onToggled(boolean actived) {
      if (!actived && !Mouse.isButtonDown(1)) {
         this.stopProcessEat();
         super.onToggled(actived);
      }
   }

   public void onEatenSucessfully(ItemStack stack) {
      if (stack != null && stack == Minecraft.player.getActiveItemStack()) {
         this.triggerAnimation.to = 1.0F;
      }
   }

   @Override
   public void alwaysRender2D(ScaledResolution sr) {
      this.scaleAnimation.to = (!this.isActived() || !this.canEat()) && !(this.triggerAnimation.anim > 0.001F) ? 0.0F : 1.0F;
      float alphaPC = 0.0F;
      float scale;
      if ((alphaPC = MathUtils.clamp(scale = this.scaleAnimation.getAnim(), 0.0F, 1.0F)) != 0.0F) {
         float triggerAnim;
         if ((triggerAnim = this.triggerAnimation.getAnim()) > 0.975F) {
            this.triggerAnimation.speed = 0.025F;
            this.triggerAnimation.setAnim(0.0F);
            this.triggerAnimation.to = 0.0F;
         }

         triggerAnim = triggerAnim > 0.5F ? 1.0F - triggerAnim : triggerAnim;
         boolean isEatingApple = Minecraft.player.isHandActive() && Minecraft.player.getActiveItemStack().getItem() instanceof ItemAppleGold;
         if (isEatingApple) {
            this.lastUsedStack = Minecraft.player.getActiveItemStack();
         }

         float pTicks = mc.getRenderPartialTicks();
         float eatProgress = MathUtils.clamp(
               Minecraft.player.getItemInUseMaxCount() == 0 ? 0.0F : (float)Minecraft.player.getItemInUseMaxCount() + pTicks, 0.0F, 32.0F
            )
            / 32.0F;
         float x = (float)sr.getScaledWidth() / 2.0F
            + (mc.gameSettings.thirdPersonView != 0 ? -8.0F - 20.0F * OffHand.scaleAnim.anim : 12.0F - 40.0F * OffHand.scaleAnim.anim)
            + Crosshair.get.crossPosMotions[0];
         float yExtPC = (float)(OffHand.scaleAnim.to > 0.0F ? 1 : -1)
            * (OffHand.scaleAnim.anim > 0.5F ? 1.0F - OffHand.scaleAnim.anim : OffHand.scaleAnim.anim)
            * 2.0F;
         float y = (float)sr.getScaledHeight() / 2.0F - 8.0F + 4.0F * (yExtPC + yExtPC + yExtPC * (0.5F + yExtPC / 2.0F)) + Crosshair.get.crossPosMotions[1];
         int magnitudeColor = ColorUtils.getColor(255, (int)(90.0F * alphaPC * (1.0F - triggerAnim) * (1.0F - triggerAnim)));
         int textColor = this.lastUsedStack.stackSize == 0
            ? ColorUtils.fadeColor(ColorUtils.getColor(255, 80, 50, 80.0F * alphaPC), ColorUtils.getColor(255, 80, 50, 255.0F * alphaPC), 1.5F)
            : ColorUtils.getColor(255, (int)(255.0F * alphaPC));
         int triggerTextColor = ColorUtils.getColor(240, 30, 0, 255.0F * (1.0F - triggerAnim) * alphaPC);
         CFontRenderer font = this.lastUsedStack.stackSize == 0 ? Fonts.noise_20 : Fonts.mntsb_12;
         CFontRenderer font2 = Fonts.mntsb_20;
         GL11.glPushMatrix();
         GL11.glDepthMask(false);
         GL11.glEnable(2929);
         GL11.glTranslatef(x, y, 0.0F);
         RenderUtils.customScaledObject2D(0.0F, 0.0F, 16.0F, 16.0F, scale);
         RenderUtils.customRotatedObject2D(0.0F, 0.0F, 8.0F, 16.0F, (double)(triggerAnim * 15.0F));
         mc.getRenderItem().renderItemIntoGUI(this.lastUsedStack, 0, 0);
         if (eatProgress != 0.0F) {
            float yOffset = 0.5F;
            StencilUtil.initStencilToWrite();
            GL11.glPushMatrix();
            RenderUtils.customScaledObject2D(0.0F, 0.0F, 16.0F, 16.0F, 0.925F);
            RenderUtils.resetBlender();
            mc.getRenderItem().renderItemIntoGUI(this.lastUsedStack, 0, 0);
            GL11.glEnable(3042);
            GL11.glPopMatrix();
            StencilUtil.readStencilBuffer(1);
            RenderUtils.drawAlphedSideways(
               0.0, (double)(yOffset + (16.0F - yOffset) * (1.0F - eatProgress * 1.1F)), 16.0, (double)(16.0F - yOffset), magnitudeColor, magnitudeColor, true
            );
            StencilUtil.uninitStencilBuffer();
         }

         if (ColorUtils.getAlphaFromColor(textColor) >= 26) {
            String countString = this.lastUsedStack.stackSize + "x";
            if (OffHand.get.actived) {
               int itemCount = 0;

               for (int i = 0; i < 44; i++) {
                  ItemStack stack;
                  if ((stack = Minecraft.player.inventory.getStackInSlot(i)).getItem() instanceof ItemAppleGold) {
                     itemCount += stack.stackSize;
                  }
               }

               countString = itemCount + "x";
            }

            float textX = MathUtils.lerp(14.5F, (float)(-font.getStringWidth(countString)) + 1.5F, OffHand.scaleAnim.anim);
            font.drawStringWithShadow(countString, (double)textX, 12.0, textColor);
         }

         if (triggerAnim > 0.0F && ColorUtils.getAlphaFromColor(triggerTextColor) >= 33) {
            float triggerAPC = 1.0F - triggerAnim;
            GL11.glBlendFunc(770, 32772);
            GL11.glPushMatrix();
            RenderUtils.customScaledObject2D(0.0F, 0.0F, 16.0F, 16.0F, (0.925F + triggerAnim * 7.0F) * triggerAPC * triggerAPC);
            mc.getRenderItem().renderItemIntoGUI(this.lastUsedStack, 0, 0);
            GL11.glPopMatrix();
            GL11.glBlendFunc(770, 771);
            float textW = (float)font2.getStringWidth("-1");
            float textX = MathUtils.lerp(9.0F + 9.0F * triggerAnim, -1.5F - 9.0F * triggerAnim, OffHand.scaleAnim.anim);
            GL11.glTranslated((double)textX, (double)(8.0F * triggerAPC), 0.0);
            RenderUtils.customRotatedObject2D(
               0.0F,
               -2.0F,
               textW,
               (float)font.getHeight(),
               (double)((20.0F - 40.0F * triggerAnim - 20.0F * triggerAnim) * (float)(OffHand.scaleAnim.to == 0.0F ? -1 : 1))
            );
            font2.drawStringWithShadow("-1", 0.0, 0.0, triggerTextColor);
         }

         GL11.glDisable(2896);
         GL11.glDepthMask(true);
         GL11.glPopMatrix();
      }
   }
}
