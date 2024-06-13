package ru.govno.client.module.modules;

import com.mojang.realmsclient.gui.ChatFormatting;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector4d;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemSkull;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec2f;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;
import ru.govno.client.Client;
import ru.govno.client.module.Module;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.newfont.CFontRenderer;
import ru.govno.client.newfont.Fonts;
import ru.govno.client.utils.Combat.RotationUtil;
import ru.govno.client.utils.Math.MathUtils;
import ru.govno.client.utils.Math.ReplaceStrUtils;
import ru.govno.client.utils.Render.ColorUtils;
import ru.govno.client.utils.Render.RenderUtils;

// TODO: Translate

public class NameTags extends Module {
   public static NameTags get;
   public BoolSettings Items;
   public BoolSettings Armor;
   public BoolSettings Enchants;
   public BoolSettings Shadow;
   public BoolSettings Potions;
   public BoolSettings HealthLine;
   public BoolSettings SkinsPreview;
   public BoolSettings ShowSelf;
   public BoolSettings OutfovMarkers;
   private final IntBuffer viewport = GLAllocation.createDirectIntBuffer(16);
   private final FloatBuffer modelview = GLAllocation.createDirectFloatBuffer(16);
   private final FloatBuffer projection = GLAllocation.createDirectFloatBuffer(16);
   private final FloatBuffer vector = GLAllocation.createDirectFloatBuffer(4);
   private final Tessellator tesellator = Tessellator.getInstance();
   private final BufferBuilder buffer = this.tesellator.getBuffer();
   protected static final ResourceLocation TAG_MARK_BASE = new ResourceLocation("vegaline/modules/nametags/tagmarkbase.png");
   protected static final ResourceLocation TAG_MARK_OVERLAY = new ResourceLocation("vegaline/modules/nametags/tagmarkoverlay.png");

   public NameTags() {
      super("NameTags", 0, Module.Category.RENDER);
      get = this;
      this.settings.add(this.Items = new BoolSettings("Items", true, this));
      this.settings.add(this.Armor = new BoolSettings("Armor", false, this, () -> this.Items.getBool()));
      this.settings.add(this.Enchants = new BoolSettings("Enchants", false, this, () -> this.Items.getBool()));
      this.settings.add(this.Shadow = new BoolSettings("Shadow", true, this));
      this.settings.add(this.Potions = new BoolSettings("Potions", false, this));
      this.settings.add(this.HealthLine = new BoolSettings("HealthLine", true, this));
      this.settings.add(this.SkinsPreview = new BoolSettings("SkinsPreview", false, this));
      this.settings.add(this.ShowSelf = new BoolSettings("ShowSelf", false, this));
      this.settings.add(this.OutfovMarkers = new BoolSettings("OutfovMarkers", true, this));
   }

   private Vector3d project2D(int scaleFactor, float x, float y, float z) {
      GL11.glGetFloat(2982, this.modelview);
      GL11.glGetFloat(2983, this.projection);
      GL11.glGetInteger(2978, this.viewport);
      return GLU.gluProject(x, y, z, this.modelview, this.projection, this.viewport, this.vector)
         ? new Vector3d(
            (double)(this.vector.get(0) / (float)scaleFactor),
            (double)(((float)Display.getHeight() - this.vector.get(1)) / (float)scaleFactor),
            (double)this.vector.get(2)
         )
         : null;
   }

   private Vector3d project2D(int scaleFactor, double x, double y, double z) {
      return this.project2D(scaleFactor, (float)x, (float)y, (float)z);
   }

   static String getName(EntityLivingBase entity) {
      String gm = "";
      String ping = "";
      String name = "";
      boolean isAdmin = Client.isClientAdmin(entity);
      if (isAdmin) {
         name = name + TextFormatting.RED + "[VEGA33 - Admin] " + TextFormatting.RESET;
      }

      name = name + entity.getDisplayName().getFormattedText();
      name = ReplaceStrUtils.fixString(name);
      boolean isNpc = true;
      if (!isAdmin && mc.pointedEntity != null && entity == mc.pointedEntity) {
         try {
            if (Minecraft.player.connection.getPlayerInfo(entity.getName()) != null) {
               isNpc = false;
            }
         } catch (Exception var9) {
            var9.printStackTrace();
         }

         if (!isNpc) {
            gm = "" + Minecraft.player.connection.getPlayerInfo(entity.getName()).getGameType() + TextFormatting.GRAY + " | " + TextFormatting.RESET;
            ping = TextFormatting.GRAY
               + " | "
               + TextFormatting.RESET
               + Minecraft.player.connection.getPlayerInfo(entity.getName()).getResponseTime()
               + "ms"
               + TextFormatting.RESET;
         }
      }

      if (Client.friendManager.isFriend(entity.getName())) {
         name = "§aAnother§7 | §r" + gm + name;
      } else {
         name = gm + name;
      }

      String absTd = name.contains(" ") ? "" : " ";
      float hp = entity.getSmoothHealth();
      name = name + "§8" + absTd + "|§r " + String.format("%.1f", hp).replace(".0", "");
      float absp = entity.getAbsorptionAmount();
      if (absp > 0.0F && absp < 1000.0F) {
         name = name + "§e+" + (int)absp;
      }

      return name + "§7ХП" + ping;
   }

   @Override
   public void onToggled(boolean actived) {
      this.stateAnim.setAnim(actived ? 0.0F : 1.0F);
      this.stateAnim.to = actived ? 1.0F : 0.0F;
      super.onToggled(actived);
   }

   @Override
   public void alwaysRender2D(ScaledResolution sr) {
      if (this.actived && (double)this.stateAnim.getAnim() < 0.01) {
         this.stateAnim.to = 1.0F;
      }

      if (!((double)this.stateAnim.getAnim() < 0.01)) {
         this.drawNameTags(sr, this.stateAnim.getAnim());
      }
   }

   private List<EntityPlayer> getShowedLivings(boolean players, boolean self) {
      return mc.world
         .getLoadedEntityList()
         .stream()
         .map(Entity::getPlayerOf)
         .filter(Objects::nonNull)
         .filter(
            entity -> {
               if ((!players || !(entity instanceof EntityOtherPlayerMP otherPMP))
                  && (!self || !(entity instanceof EntityPlayerSP) || mc.gameSettings.thirdPersonView == 0)) {
                  return false;
               }

               return true;
            }
         )
         .filter(Objects::nonNull)
         .toList();
   }

   private Vec3d getEntityPosVec(Entity entity, float pTicks) {
      double x = RenderUtils.interpolate(entity.posX, entity.prevPosX, (double)pTicks);
      double y = RenderUtils.interpolate(entity.posY, entity.prevPosY, (double)pTicks);
      double z = RenderUtils.interpolate(entity.posZ, entity.prevPosZ, (double)pTicks);
      return new Vec3d(x, y, z);
   }

   private Vec2f getNameTagPosVec(EntityLivingBase base, float pTicks, int scaleFactor, ScaledResolution sr, RenderManager renderMng) {
      Vec2f vec2f = new Vec2f(-1617.0F, -1617.0F);
      Vec3d posEnt = this.getEntityPosVec(base, pTicks);
      AxisAlignedBB aabb = new AxisAlignedBB(
         posEnt.xCoord,
         posEnt.yCoord + (double)(base.height / (float)(base.isChild() ? 2 : 1)) + (base.isSneaking() ? 0.3 : 0.27499999999999997),
         posEnt.zCoord
      );
      Vector3d[] vectors = new Vector3d[]{
         new Vector3d(aabb.minX, aabb.minY, aabb.minZ),
         new Vector3d(aabb.minX, aabb.maxY, aabb.minZ),
         new Vector3d(aabb.maxX, aabb.minY, aabb.minZ),
         new Vector3d(aabb.maxX, aabb.maxY, aabb.minZ),
         new Vector3d(aabb.minX, aabb.minY, aabb.maxZ),
         new Vector3d(aabb.minX, aabb.maxY, aabb.maxZ),
         new Vector3d(aabb.maxX, aabb.minY, aabb.maxZ),
         new Vector3d(aabb.maxX, aabb.maxY, aabb.maxZ)
      };
      mc.entityRenderer.setupCameraTransform(pTicks, 1);
      Vector4d position = null;
      Vector3d[] vecList = vectors;
      int vecLength = vectors.length;

      for (int l = 0; l < vecLength; l++) {
         Vector3d vector = this.project2D(
            scaleFactor, vecList[l].x - RenderManager.viewerPosX, vecList[l].y - RenderManager.viewerPosY, vecList[l].z - RenderManager.viewerPosZ
         );
         if (vector != null && vector.z >= 0.0 && vector.z < 1.0) {
            if (position == null) {
               position = new Vector4d(vector.x, vector.y, vector.z, 0.0);
            }

            position.x = Math.min(vector.x, position.x);
            position.y = Math.min(vector.y, position.y);
            position.z = Math.max(vector.x, position.z);
            position.w = Math.max(vector.y, position.w);
         }
      }

      mc.entityRenderer.setupOverlayRendering();
      if (position != null) {
         double posX = position.x;
         double posY = position.y;
         double endPosX = position.z;
         double endPosY = position.w;
         vec2f.x = (float)(posX + (endPosX - posX) / 2.0);
         vec2f.y = (float)posY;
      }

      return vec2f;
   }

   private boolean nameTagVecSetSuccess(Vec2f vec2f) {
      return ((int)vec2f.x != -1617 || (int)vec2f.y != -1617)
         && vec2f.x > 0.0F
         && vec2f.x < (float)mc.displayWidth / 2.0F
         && vec2f.y > 0.0F
         && vec2f.y < (float)mc.displayHeight / 2.0F + 40.0F;
   }

   private void drawNameTags(ScaledResolution sr, float alphaPC) {
      float pTicks = mc.getRenderPartialTicks();
      int scaleFactor = ScaledResolution.getScaleFactor();
      RenderManager renderManager = mc.getRenderManager();
      RenderItem itemRender = mc.getRenderItem();
      int markOffset = 10;
      float partialTicks = mc.getRenderPartialTicks();
      float selfYaw = Minecraft.player.rotationYaw;
      this.getShowedLivings(true, this.ShowSelf.getBool())
         .forEach(
            player -> {
               Vec2f pos = this.getNameTagPosVec(player, pTicks, scaleFactor, sr, renderManager);
               if (this.nameTagVecSetSuccess(pos) && RenderUtils.isInView(player)) {
                  this.drawEntity2DTag(
                     player,
                     alphaPC,
                     this.Items.getBool(),
                     this.Armor.getBool(),
                     this.Enchants.getBool(),
                     this.SkinsPreview.getBool(),
                     this.Potions.getBool(),
                     this.Shadow.getBool(),
                     this.HealthLine.getBool(),
                     pos.x,
                     pos.y,
                     itemRender,
                     sr
                  );
               } else if (this.OutfovMarkers.getBool()) {
                  this.drawEntity2DMark(player, 10, sr, selfYaw, partialTicks, renderManager, this.SkinsPreview.getBool(), alphaPC);
               }
            }
         );
      GL11.glEnable(2929);
      GL11.glDepthMask(true);
   }

   private void drawEntity2DMark(
      EntityLivingBase base, int markOffset, ScaledResolution sr, float selfYaw, float partialTicks, RenderManager rmngr, boolean drawSkin, float alphaPC
   ) {
      float radian;
      float x;
      float y;
      ResourceLocation skin;
      boolean var10000;
      label46: {
         Vec3d posEnt = new Vec3d(
            base.lastTickPosX + (base.posX - base.lastTickPosX) * (double)partialTicks,
            base.lastTickPosY + (base.posY - base.lastTickPosY) * (double)partialTicks,
            base.lastTickPosZ + (base.posZ - base.lastTickPosZ) * (double)partialTicks
         );
         Vec3d selfVec = new Vec3d(rmngr.getRenderPosX(), rmngr.getRenderPosY(), rmngr.getRenderPosZ());
         radian = RotationUtil.getFacePosRemote(selfVec, posEnt)[0] - 90.0F - selfYaw;
         float[] xyOfRads = this.getPointOfRadian(0.0F, 0.0F, (float)sr.getScaledWidth(), (float)sr.getScaledHeight(), (double)radian, (float)markOffset);
         x = xyOfRads[0];
         y = xyOfRads[1];
         skin = null;
         if (drawSkin
            && base instanceof EntityPlayer player
            && mc.getConnection() != null
            && mc.getConnection().getPlayerInfo(player.getUniqueID()) != null
            && (skin = mc.getConnection().getPlayerInfo(base.getUniqueID()).getLocationSkin()) != null) {
            var10000 = true;
            break label46;
         }

         var10000 = false;
      }

      boolean hasDrawSkin = var10000;
      float markStature = hasDrawSkin ? 8.0F : 6.0F;
      float fadeSpeed = 0.3F;
      int textColor = ColorUtils.swapAlpha(ColorUtils.getColor(215), 255.0F * alphaPC);
      GL11.glPushMatrix();
      float rotAngle = radian % 90.0F / 90.0F > 0.5F ? 1.0F - radian % 90.0F / 90.0F : radian % 90.0F / 90.0F;
      rotAngle *= 2.0F;
      rotAngle *= rotAngle;
      rotAngle *= 45.0F;
      GL11.glEnable(3042);
      GL11.glDisable(3008);
      if (hasDrawSkin && skin != null) {
         mc.getTextureManager().bindTexture(WorldRender.get.updatedResourceSkin(skin, base));
         GL11.glPushMatrix();
         RenderUtils.setupColor(-1, 215.0F * alphaPC);
         GL11.glDisable(3008);
         GL11.glEnable(3553);
         GL11.glDisable(2929);
         GL11.glDepthMask(false);
         float headScale = 8.0F;
         float headExtOverlay = 0.5F;
         GL11.glTranslated((double)(x - markStature / 2.0F), (double)(y - markStature / 2.0F), 0.0);
         Gui.drawScaledCustomSizeModalRect(0.0F, 0.0F, 8.0F, 8.0F, 8.0F, 8.0F, headScale, headScale, 64.0F, 64.0F);
         Gui.drawScaledCustomSizeModalRect(
            -headExtOverlay, -headExtOverlay, 39.0F, 8.0F, 10.0F, 8.0F, headScale + headExtOverlay * 2.0F, headScale + headExtOverlay * 2.0F, 64.0F, 64.0F
         );
         GL11.glEnable(3008);
         GL11.glEnable(2929);
         GL11.glDepthMask(true);
         GlStateManager.resetColor();
         GL11.glPopMatrix();
      } else {
         GL11.glEnable(3553);
         GL11.glShadeModel(7425);
         GL11.glBlendFunc(770, 32772);
         int white = ColorUtils.swapAlpha(-1, 255.0F * alphaPC);
         int colorize = Client.friendManager.isFriend(base.getName())
            ? ColorUtils.getColor(40, 255, 60, 255.0F * alphaPC)
            : ColorUtils.getColor(255, 0, 0, 255.0F * alphaPC);
         int markC1 = ColorUtils.fadeColor(colorize, white, fadeSpeed, (int)(0.0F / fadeSpeed));
         int markC2 = ColorUtils.fadeColor(colorize, white, fadeSpeed, (int)(90.0F / fadeSpeed));
         int markC3 = ColorUtils.fadeColor(colorize, white, fadeSpeed, (int)(180.0F / fadeSpeed));
         int markC4 = ColorUtils.fadeColor(colorize, white, fadeSpeed, (int)(240.0F / fadeSpeed));
         mc.getTextureManager().bindTexture(TAG_MARK_BASE);
         this.buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
         this.buffer.pos((double)(x - markStature / 2.0F), (double)(y - markStature / 2.0F)).tex(0.0, 0.0).color(markC1).endVertex();
         this.buffer.pos((double)(x - markStature / 2.0F), (double)(y + markStature / 2.0F)).tex(0.0, 1.0).color(markC2).endVertex();
         this.buffer.pos((double)(x + markStature / 2.0F), (double)(y + markStature / 2.0F)).tex(1.0, 1.0).color(markC3).endVertex();
         this.buffer.pos((double)(x + markStature / 2.0F), (double)(y - markStature / 2.0F)).tex(1.0, 0.0).color(markC4).endVertex();
         this.tesellator.draw();
         float animTimeMax = 500.0F;
         float timePC = (float)((System.currentTimeMillis() + (long)((int)(rotAngle * (animTimeMax / 90.0F)))) % (long)((int)animTimeMax)) / animTimeMax;
         float animAlphaPC = (float)MathUtils.easeInOutQuadWave((double)timePC);
         float markStature2 = markStature * (1.0F + timePC);
         mc.getTextureManager().bindTexture(TAG_MARK_OVERLAY);
         this.buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
         this.buffer
            .pos((double)(x - markStature2 / 2.0F), (double)(y - markStature2 / 2.0F))
            .tex(0.0, 0.0)
            .color(ColorUtils.swapAlpha(markC1, (float)ColorUtils.getAlphaFromColor(markC1) * animAlphaPC))
            .endVertex();
         this.buffer
            .pos((double)(x - markStature2 / 2.0F), (double)(y + markStature2 / 2.0F))
            .tex(0.0, 1.0)
            .color(ColorUtils.swapAlpha(markC2, (float)ColorUtils.getAlphaFromColor(markC2) * animAlphaPC))
            .endVertex();
         this.buffer
            .pos((double)(x + markStature2 / 2.0F), (double)(y + markStature2 / 2.0F))
            .tex(1.0, 1.0)
            .color(ColorUtils.swapAlpha(markC3, (float)ColorUtils.getAlphaFromColor(markC3) * animAlphaPC))
            .endVertex();
         this.buffer
            .pos((double)(x + markStature2 / 2.0F), (double)(y - markStature2 / 2.0F))
            .tex(1.0, 0.0)
            .color(ColorUtils.swapAlpha(markC4, (float)ColorUtils.getAlphaFromColor(markC4) * animAlphaPC))
            .endVertex();
         this.tesellator.draw();
         GL11.glBlendFunc(770, 771);
         GL11.glShadeModel(7424);
      }

      GL11.glPopMatrix();
      CFontRenderer textFont = Fonts.mntsb_10;
      String name = base.getName() + " " + String.format("%.1f", base.getSmoothHealth()).replace(".0", "");
      float textW = (float)textFont.getStringWidth(name);
      float textX = MathUtils.clamp(x - textW / 2.0F, (float)markOffset - markStature, (float)(sr.getScaledWidth() - markOffset) - textW + markStature + 2.0F);
      float textY = y - 10.0F;
      if (ColorUtils.getAlphaFromColor(textColor) >= 33) {
         textFont.drawStringWithShadow(name, (double)textX, (double)textY, textColor);
      }
   }

   private void drawEntity2DTag(
      EntityLivingBase base,
      float alphaPC,
      boolean items,
      boolean armor,
      boolean enchants,
      boolean heads,
      boolean potions,
      boolean shadow,
      boolean healthLine,
      float x,
      float y,
      RenderItem renderItem,
      ScaledResolution sr
   ) {
      alphaPC *= base.getDeathAlpha();
      alphaPC *= Math.min(((float)base.ticksExisted + mc.getRenderPartialTicks()) / 12.0F, 1.0F);
      if ((double)alphaPC > 0.96) {
         alphaPC = 1.0F;
      }

      if (base.getHealth() == 0.0F) {
         healthLine = false;
      }

      if (healthLine) {
         y -= 3.0F;
      }

      int texColor = ColorUtils.swapAlpha(-65537, 255.0F * alphaPC);
      int bgColor = ColorUtils.getColor(0, (int)(65.0F * alphaPC));
      int bgOutColor = ColorUtils.getColor(0, (int)(100.0F * alphaPC));
      String name = getName(base);
      CFontRenderer font = Fonts.comfortaaBold_12;
      if (heads && !(base instanceof EntityPlayer) || base == FakePlayer.fakePlayer || base == FreeCam.fakePlayer) {
         heads = false;
      }

      float xExtOfHeads = heads ? 10.0F : 0.0F;
      float w = (float)font.getStringWidth(name);
      float extXYWH = 1.5F * alphaPC;
      if (shadow) {
         float offset = -1.0F + 1.5F * alphaPC;
         RenderUtils.drawOutsideAndInsideFullRoundedFullGradientShadowRectWithBloomBool(
            x - w / 2.0F - 2.0F - extXYWH + offset,
            y - 9.0F - extXYWH + offset,
            x - w / 2.0F - 2.0F - extXYWH + w + 4.0F + extXYWH * 2.0F - offset + xExtOfHeads,
            y - 9.0F - extXYWH + 9.0F + extXYWH * 2.0F + (healthLine ? 3.0F : 0.0F) - offset,
            3.0F,
            ClientColors.getColor1(45, 0.2F * alphaPC),
            ClientColors.getColor2(0, 0.2F * alphaPC),
            ClientColors.getColor2(45, 0.2F * alphaPC),
            ClientColors.getColor1(0, 0.2F * alphaPC),
            true
         );
      }

      RenderUtils.drawRoundOutline(
         x - w / 2.0F - 2.0F - extXYWH,
         y - 9.0F - extXYWH,
         w + 4.0F + extXYWH * 2.0F + xExtOfHeads,
         9.0F + extXYWH * 2.0F + (healthLine ? 3.0F : 0.0F),
         4.0F,
         0.1F,
         bgColor,
         shadow ? 0 : bgOutColor,
         sr
      );
      if (healthLine) {
         float hpPercent = MathUtils.clamp(base.getSmoothHealth() / base.getMaxHealth(), 0.0F, 1.0F);
         float hpExtX = 3.5F;
         float hpExtY = 0.0F;
         float hpX1 = x - w / 2.0F - 2.0F - extXYWH + 3.5F;
         float hpX2 = hpX1 + (w + 4.0F + extXYWH * 2.0F - 7.0F + xExtOfHeads) * hpPercent;
         float hpX3 = hpX1 + w + 4.0F + extXYWH * 2.0F - 7.0F + xExtOfHeads;
         int hpCol1 = ColorUtils.getProgressColor(0.25F).getRGB();
         hpCol1 = ColorUtils.swapAlpha(hpCol1, 255.0F * alphaPC);
         int hpCol2 = ColorUtils.getProgressColor(0.25F + hpPercent * 0.75F).getRGB();
         hpCol2 = ColorUtils.swapAlpha(hpCol2, 255.0F * alphaPC);
         RenderUtils.drawFullGradientRectPro(hpX1, y + hpExtY, hpX2, y + 1.0F + hpExtY, hpCol1, hpCol2, hpCol2, hpCol1, false);
         RenderUtils.drawAlphedRect((double)hpX2, (double)(y + hpExtY), (double)hpX3, (double)(y + 1.0F + hpExtY), bgOutColor);
      }

      if (RenderUtils.alpha(texColor) >= 32) {
         font.drawStringWithShadow(name, (double)(x - w / 2.0F + xExtOfHeads), (double)(y - 6.0F), texColor);
      }

      if (heads) {
         NetHandlerPlayClient connection = mc.getConnection();
         ResourceLocation head;
         if (connection != null
            && base instanceof EntityPlayer player
            && connection.getPlayerInfo(player.getName()) != null
            && (head = WorldRender.get.updatedResourceSkin(connection.getPlayerInfo(player.getName()).getLocationSkin(), player)) != null) {
            mc.getTextureManager().bindTexture(head);
            GL11.glPushMatrix();
            RenderUtils.setupColor(-1, 215.0F * alphaPC);
            GL11.glDisable(3008);
            GL11.glEnable(3553);
            GL11.glDisable(2929);
            GL11.glDepthMask(false);
            float headScale = 8.0F;
            float headExtOverlay = 0.5F;
            GL11.glTranslated((double)(x - w / 2.0F), (double)(y - 8.5F), 0.0);
            Gui.drawScaledCustomSizeModalRect(0.0F, 0.0F, 8.0F, 8.0F, 8.0F, 8.0F, headScale, headScale, 64.0F, 64.0F);
            Gui.drawScaledCustomSizeModalRect(
               -headExtOverlay, -headExtOverlay, 39.0F, 8.0F, 10.0F, 8.0F, headScale + headExtOverlay * 2.0F, headScale + headExtOverlay * 2.0F, 64.0F, 64.0F
            );
            GL11.glEnable(3008);
            GL11.glEnable(2929);
            GL11.glDepthMask(true);
            GlStateManager.resetColor();
            GL11.glPopMatrix();
         }
      }

      if (potions && base.getActivePotionEffects() != null) {
         List<PotionEffect> activeEffects = base.getActivePotionEffects().stream().filter(Objects::nonNull).filter(effect -> effect.getDuration() > 0).toList();
         if (!activeEffects.isEmpty()) {
            y -= this.drawPotionEffectsReturningHeight(activeEffects, x, y - 11.0F, alphaPC) * alphaPC;
         }
      }

      if ((items || armor) && base instanceof EntityPlayer player) {
         ItemStack hOffHand = player.getHeldItemOffhand();
         ItemStack aHelmet = player.inventory.armorInventory.get(0);
         ItemStack aChestplate = player.inventory.armorInventory.get(1);
         ItemStack aLeggings = player.inventory.armorInventory.get(2);
         ItemStack aFeet = player.inventory.armorInventory.get(3);
         ItemStack hMainHand = player.getHeldItemMainhand();
         List<ItemStack> stacks = (items && armor
               ? Arrays.asList(hOffHand, aHelmet, aChestplate, aLeggings, aFeet, hMainHand)
               : (items ? Arrays.asList(hOffHand, hMainHand) : Arrays.asList(aHelmet, aChestplate, aLeggings, aFeet)))
            .stream()
            .filter(Objects::nonNull)
            .filter(stack -> !stack.func_190926_b())
            .toList();
         if (!stacks.isEmpty()) {
            float itemScale = 0.5F * alphaPC;
            y -= 26.0F * itemScale;
            this.drawItemStackList(stacks, enchants, x, y - 9.0F, alphaPC, renderItem, itemScale);
         }
      }
   }

   private String getPotionEffectString(PotionEffect potionEffect) {
      String power = "";
      ChatFormatting potionColor = null;
      int duration = potionEffect.getDuration();
      if (duration != 0) {
         int level = potionEffect.getAmplifier() == 0 ? 0 : potionEffect.getAmplifier() + 1;
         power = TextFormatting.GRAY + I18n.format("enchantment.level." + level);
         power = power.replace("enchantment.level.0", "");
         power = power.replace("enchantment.level.", "");
         if (duration > 1000) {
            potionColor = ChatFormatting.GREEN;
         }

         if (duration < 800) {
            potionColor = ChatFormatting.YELLOW;
         }

         if (duration < 600) {
            potionColor = ChatFormatting.GOLD;
         }

         if (duration < 200) {
            potionColor = System.currentTimeMillis() % 700L > 350L ? ChatFormatting.RED : ChatFormatting.DARK_RED;
         }

         if (potionEffect.getIsPotionDurationMax()) {
            potionColor = ChatFormatting.LIGHT_PURPLE;
         }
      }

      return (I18n.format(potionEffect.getPotion().getName())
            + " "
            + power
            + TextFormatting.GRAY
            + " "
            + potionColor
            + Potion.getPotionDurationString(potionEffect, 1.0F))
         .replace("  ", " ")
         .replace("null", "");
   }

   private float drawPotionEffectsReturningHeight(List<PotionEffect> potionEffects, float x, float y, float alphaPC) {
      CFontRenderer effectFont = Fonts.minecraftia_16;
      float iXP = (float)potionEffects.stream().filter(effectx -> effectx.getPotion().hasStatusIcon()).count() * -4.5F;

      for (PotionEffect effect : potionEffects) {
         String durStr = Potion.getPotionDurationString(effect, 1.0F);
         float strW = (float)effectFont.getStringWidth(durStr);
         iXP -= strW / 4.0F * alphaPC;
      }

      float iYP = -9.5F;

      for (PotionEffect effect : potionEffects) {
         if (this.onDoDrawPotionEffectIcon(true, x + iXP, y + iYP, 9, effect.getPotion())) {
            String durStr = Potion.getPotionDurationString(effect, 1.0F);
            float strW = (float)effectFont.getStringWidth(durStr);
            String level = "Lv" + (effect.getAmplifier() + 1);
            int durColor = ColorUtils.swapAlpha(
               ColorUtils.getOverallColorFrom(
                  ColorUtils.getOverallColorFrom(
                     ColorUtils.getColor(255, 0, 0), ColorUtils.getColor(0, 255, 0), MathUtils.clamp((float)effect.getDuration() / 1000.0F, 0.0F, 1.0F)
                  ),
                  -1
               ),
               255.0F * alphaPC
            );
            if (ColorUtils.getAlphaFromColor(durColor) >= 33) {
               int lvColor = ColorUtils.swapAlpha(ColorUtils.getOverallColorFrom(ColorUtils.getColor(175, 175, 175), durColor), 255.0F * alphaPC);
               GL11.glScaled(0.5, 0.5, 1.0);
               effectFont.drawString(durStr, (double)((x + iXP + 9.0F) * 2.0F), (double)((y + iYP + 5.0F) * 2.0F), durColor);
               if (ColorUtils.getAlphaFromColor(lvColor) >= 33) {
                  effectFont.drawString(level, (double)((x + iXP + 9.0F) * 2.0F), (double)((y + iYP) * 2.0F), lvColor);
               }

               GL11.glScaled(2.0, 2.0, 1.0);
            }

            iXP += (10.0F + strW / 2.0F) * alphaPC;
         }
      }

      return potionEffects.size() == 0 ? 0.0F : 10.0F * alphaPC;
   }

   private void drawItemStackList(List<ItemStack> stacks, boolean showEnchants, float x, float y, float alphaPC, RenderItem renderItem, float scale) {
      scale *= alphaPC;
      float itemPixScale = 16.0F * scale;
      x -= (float)stacks.size() * (itemPixScale / 2.0F) + itemPixScale / 4.0F;
      float prevZLevel = renderItem.zLevel;
      int index = 0;
      GL11.glEnable(3042);
      GL11.glBlendFunc(770, 771);
      GL11.glDepthMask(true);
      int stackNumber = 0;
      int texColor = ColorUtils.swapAlpha(-65537, 245.0F * alphaPC * alphaPC);

      for (ItemStack stack : stacks) {
         stackNumber++;
         GL11.glDepthRange(0.0, 0.01);
         if (renderItem.zLevel != 300.0F) {
            renderItem.zLevel = 300.0F;
         }

         GL11.glPushMatrix();
         GlStateManager.enableDepth();
         RenderHelper.enableGUIStandardItemLighting();
         GL11.glTranslated((double)x, (double)y, 0.0);
         RenderUtils.customScaledObject2D(0.0F, 0.0F, itemPixScale, itemPixScale, scale);
         renderItem.renderItemAndEffectIntoGUI(stack, 0, 0);
         if (alphaPC * 255.0F >= 32.0F) {
            renderItem.renderItemOverlayIntoGUI(Fonts.minecraftia_16, stack, 0, 0, stack.getCount());
         }

         RenderUtils.drawItemWarnIfLowDur(stack, 0.0F, 0.0F, 1.0F, 1.0F, 3);
         if ((stackNumber == 1 || stackNumber == stacks.size()) && stack.getItem() instanceof ItemSkull) {
            CFontRenderer skullFont = Fonts.mntsb_18;
            String skullDisplay = stack.getDisplayName()
               .replace("§r", "")
               .replace("§l", "")
               .replace("§k", "")
               .replace("§n", "")
               .replace("§n", "")
               .replace("§o", "");
            float headTextX = stacks.size() == 1
               ? (float)(-skullFont.getStringWidth(skullDisplay)) / 2.0F + 8.0F
               : (stackNumber == stacks.size() ? 18.0F : (float)(-skullFont.getStringWidth(skullDisplay)) - 2.0F);
            RenderHelper.disableStandardItemLighting();
            GL11.glDepthMask(false);
            skullFont.drawStringWithShadow(
               skullDisplay, (double)headTextX, stacks.size() == 1 ? -5.0 : 6.0, ColorUtils.swapAlpha(-1, 255.0F * alphaPC * alphaPC)
            );
            GL11.glDepthMask(true);
            RenderHelper.enableGUIStandardItemLighting();
         }

         RenderUtils.customScaledObject2D(0.0F, 0.0F, itemPixScale, itemPixScale, 1.0F / scale);
         GL11.glTranslated((double)(-x), (double)(-y), 0.0);
         RenderHelper.disableStandardItemLighting();
         GL11.glDepthRange(0.0, 1.0);
         if (showEnchants) {
            GL11.glDepthMask(false);
            CFontRenderer enchFont = Fonts.smallestpixel_20;
            float texY = y - 2.0F;
            index = 0;

            for (String enchStr : this.getEnchantNamesOfEnchantsMap(stack)) {
               if (index <= 6) {
                  float strW = (float)enchFont.getStringWidth(enchStr);
                  float texX = x + (16.0F * scale - strW / 2.0F) * scale;
                  if (RenderUtils.alpha(texColor) >= 33) {
                     GL11.glScaled(0.5, 0.5, 1.0);
                     enchFont.drawStringWithShadow(enchStr, (double)((texX + 2.5F) * 2.0F), (double)(texY * 2.0F), texColor);
                     GL11.glScaled(2.0, 2.0, 1.0);
                  }

                  texY -= 4.5F * alphaPC;
                  index++;
               }
            }
         }

         GlStateManager.enableDepth();
         GL11.glDepthMask(true);
         GL11.glPopMatrix();
         x += itemPixScale;
         index++;
      }

      renderItem.zLevel = prevZLevel;
   }

   List<String> getEnchantNamesOfEnchantsMap(ItemStack stack) {
      List<String> list = new ArrayList<>();

      for (Enchantment enchantment : EnchantmentHelper.getEnchantments(stack).keySet()) {
         String translated = enchantment.getTranslatedName(-228).replace(" enchantment.level.-228", "");
         if (translated.length() > 3) {
            translated = translated.replace("Защита", "§3З")
               .replace("Огнеупорность", "§7О")
               .replace("Невесомость", "§bН")
               .replace("Взрывоустойчивость", "§7В")
               .replace("Защита от снарядов", "§7З")
               .replace("Подводное дыхание", "§1П")
               .replace("Подводник", "§1П")
               .replace("Шипы", "§2Ш")
               .replace("Подводная ходьба", "§1П")
               .replace("Ледоход", "§bЛ")
               .replace("Проклятие несъёмности", "§4П")
               .replace("Острота", "§cО")
               .replace("Небесная кара", "§2К")
               .replace("Бич членистоногих", "§2Б")
               .replace("Отдача", "§7О")
               .replace("Заговор огня", "§6З")
               .replace("Добыча", "§2Д")
               .replace("Разящий клинок", "§7Р")
               .replace("Прочность", "§7П")
               .replace("Сила", "§cС")
               .replace("Откидывание", "§7О")
               .replace("Горящая стрела", "§6Г")
               .replace("Бесконечность", "§8Б")
               .replace("Починка", "§aП")
               .replace("Проклятие утраты", "§4У")
               .replace("Эффективность", "§2Э")
               .replace("Шёлковое касание", "§2Ш")
               .replace("Удача", "§2У")
               .replace("Везучий рыбак", "§2В")
               .replace("Приманка", "§2П")
               .replace("Долговечность", "§7П")
               .replace("Огонь", "§6О")
               .replace("Невесомка", "§bH");
            translated = translated + EnchantmentHelper.getEnchantmentLevel(enchantment, stack);
            list.add(translated);
         }
      }

      return list;
   }

   public float[] getPointOfRadian(float x, float y, float x2, float y2, double radian, float offset) {
      x += offset;
      y += offset;
      x2 -= offset;
      y2 -= offset;
      offset /= 2.0F;
      float w = x2 - x;
      float h = y2 - y;
      float xPos = x + w / 2.0F;
      float yPos = y + h / 2.0F;
      xPos = (float)((double)xPos + Math.cos(Math.toRadians(radian)) * (double)w / 1.443333F);
      yPos = (float)((double)yPos + Math.sin(Math.toRadians(radian)) * (double)h / 1.443333F);
      xPos = MathUtils.clamp(xPos, x + offset, x2 - offset);
      yPos = MathUtils.clamp(yPos, y + offset, y2 - offset);
      return new float[]{xPos, yPos};
   }

   public boolean onDoDrawPotionEffectIcon(boolean bindTex, float x, float y, int size, Potion potion) {
      if (potion == null) {
         return false;
      } else if (potion.hasStatusIcon()) {
         if (bindTex) {
            mc.getTextureManager().bindTexture(GuiContainer.INVENTORY_BACKGROUND);
         }

         int indexTex = potion.getStatusIconIndex();
         GL11.glPushMatrix();
         GlStateManager.disableLighting();
         GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
         GL11.glTranslated((double)x, (double)y, 0.0);
         GL11.glScaled(0.05555555555555555, 0.05555555555555555, 1.0);
         GL11.glScaled((double)size, (double)size, 1.0);
         new Gui().drawTexturedModalRect(0, 0, indexTex % 8 * 18, 198 + indexTex / 8 * 18, 18, 18);
         GL11.glPopMatrix();
         return true;
      } else {
         return false;
      }
   }
}
