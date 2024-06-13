package ru.govno.client.module.modules;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector4d;
import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.model.ModelEnderCrystal;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityBeacon;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityEndPortal;
import net.minecraft.tileentity.TileEntityEnderChest;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.tileentity.TileEntityShulkerBox;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec2f;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import optifine.Config;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.util.glu.GLU;
import ru.govno.client.module.Module;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.module.settings.ColorSettings;
import ru.govno.client.module.settings.FloatSettings;
import ru.govno.client.module.settings.ModeSettings;
import ru.govno.client.newfont.CFontRenderer;
import ru.govno.client.newfont.Fonts;
import ru.govno.client.utils.CrystalField;
import ru.govno.client.utils.Combat.RotationUtil;
import ru.govno.client.utils.Command.impl.Panic;
import ru.govno.client.utils.Math.MathUtils;
import ru.govno.client.utils.Math.TimerHelper;
import ru.govno.client.utils.Render.AnimationUtils;
import ru.govno.client.utils.Render.ColorUtils;
import ru.govno.client.utils.Render.RenderUtils;
import ru.govno.client.utils.Render.ShaderUtil2;
import ru.govno.client.utils.Render.ShaderUtility;
import ru.govno.client.utils.Render.Vec2fColored;

public class ESP extends Module {
   public static ESP get;
   private static final AnimationUtils alphaPC = new AnimationUtils(0.0F, 0.0F, 0.1F);
   private final Random RANDOM = new Random(1234567891L);
   public BoolSettings EnderCrystals;
   public BoolSettings CrystalImprove;
   public BoolSettings Players;
   public BoolSettings Self;
   public BoolSettings GlowBloom;
   public BoolSettings ItemsI;
   public BoolSettings EnderPearl;
   public BoolSettings Spawner;
   public BoolSettings Storage;
   public BoolSettings BreakOver;
   public BoolSettings Beacon;
   public BoolSettings TntPrimed;
   public BoolSettings Targets;
   public BoolSettings VoidHighlight;
   public BoolSettings Portals;
   public FloatSettings GlowRadius;
   public FloatSettings BloomAmount;
   public ModeSettings PlayerMode;
   public ModeSettings SelfMode;
   public ModeSettings TntMode;
   public ModeSettings TargetsMode;
   public ModeSettings TargetTexture;
   public ModeSettings TargetColor;
   public ColorSettings PickNormal;
   public ColorSettings PickHurt;
   private final ResourceLocation BLOOM_TEX = new ResourceLocation("vegaline/modules/" + this.name.toLowerCase() + "/bloomsimulate/bloom.png");
   private final ShaderUtil2 glowShader = new ShaderUtil2("vegaline/modules/esp/shaders/anglecoloredglow.frag");
   Framebuffer framebuffer;
   Framebuffer glowFrameBuffer;
   private final IntBuffer viewport = GLAllocation.createDirectIntBuffer(16);
   private final FloatBuffer modelview = GLAllocation.createDirectFloatBuffer(16);
   private final FloatBuffer projection = GLAllocation.createDirectFloatBuffer(16);
   private final FloatBuffer vector = GLAllocation.createDirectFloatBuffer(4);
   List<EntityLivingBase> updatedTargets = new ArrayList<>();
   List<ESP.TESP> TESP_LIST = new ArrayList<>();
   private final List<ESP.ColVecsWithEnt> colVecsWithEntList = new ArrayList<>();
   private final ResourceLocation TARGET_2D_ESP = new ResourceLocation("vegaline/modules/esp/target/quadstapple.png");
   private final ResourceLocation TARGET_2D_ESP2 = new ResourceLocation("vegaline/modules/esp/target/fuckfinger.png");
   private final ResourceLocation TARGET_2D_ESP3 = new ResourceLocation("vegaline/modules/esp/target/trianglestapple.png");
   private final ResourceLocation TARGET_2D_ESP4 = new ResourceLocation("vegaline/modules/esp/target/trianglestipple.png");
   private final List<ESP.GlareBubble> GLARES_LIST = new ArrayList<>();
   final Item[] garbageitems = new Item[]{
      Items.STRING,
      Items.SPIDER_EYE,
      Items.ROTTEN_FLESH,
      Item.getItemFromBlock(Blocks.MOSSY_COBBLESTONE),
      Item.getItemFromBlock(Blocks.COBBLESTONE),
      Items.SADDLE,
      Items.BONE,
      Items.WHEAT,
      Items.IRON_HORSE_ARMOR,
      Items.GOLDEN_HORSE_ARMOR,
      Items.GUNPOWDER,
      Items.PUMPKIN_SEEDS,
      Items.WHEAT_SEEDS,
      Items.NAME_TAG,
      Items.REDSTONE,
      Items.IRON_INGOT
   };
   int targetColor;
   private final ResourceLocation PEARL_MARK_TEXTURE = new ResourceLocation("vegaline/modules/esp/pearl/pearlmarker.png");
   public BlockPos posOver = new BlockPos(-1, 2, 1);
   private final AnimationUtils alphaSelectPC = new AnimationUtils(0.0F, 0.0F, 0.1F);
   private final AnimationUtils progressingSelect = new AnimationUtils(0.0F, 0.0F, 0.115F);
   private final Vec3d smoothPosSelect = new Vec3d(0.0, 0.0, 0.0);
   private final Tessellator tessellator = Tessellator.getInstance();
   private final BufferBuilder buffer = this.tessellator.getBuffer();

   public ESP() {
      super("ESP", 0, Module.Category.RENDER);
      this.settings.add(this.EnderCrystals = new BoolSettings("Ender crystals", false, this));
      this.settings.add(this.CrystalImprove = new BoolSettings("Crystal improve", true, this));
      this.settings.add(this.Players = new BoolSettings("Players", true, this));
      this.settings.add(this.PlayerMode = new ModeSettings("Player mode", "Glow", this, new String[]{"2D", "Glow"}, () -> this.Players.getBool()));
      this.settings.add(this.Self = new BoolSettings("Self", true, this));
      this.settings.add(this.SelfMode = new ModeSettings("Self mode", "Glow", this, new String[]{"2D", "Glow"}, () -> this.Self.getBool()));
      this.settings
         .add(
            this.GlowRadius = new FloatSettings(
               "Glow radius",
               4.0F,
               15.0F,
               2.0F,
               this,
               () -> this.Players.getBool() && this.PlayerMode.currentMode.equalsIgnoreCase("Glow")
                     || this.Self.getBool() && this.SelfMode.currentMode.equalsIgnoreCase("Glow")
                     || this.EnderCrystals.getBool()
                     || this.TntPrimed.getBool() && this.TntMode.currentMode.equalsIgnoreCase("Glow")
            )
         );
      this.settings
         .add(
            this.GlowBloom = new BoolSettings(
               "Glow bloom",
               true,
               this,
               () -> this.Players.getBool() && this.PlayerMode.currentMode.equalsIgnoreCase("Glow")
                     || this.Self.getBool() && this.SelfMode.currentMode.equalsIgnoreCase("Glow")
                     || this.EnderCrystals.getBool()
                     || this.TntPrimed.getBool() && this.TntMode.currentMode.equalsIgnoreCase("Glow")
            )
         );
      this.settings
         .add(
            this.BloomAmount = new FloatSettings(
               "Bloom amount",
               1.0F,
               1.5F,
               0.5F,
               this,
               () -> this.GlowBloom.getBool()
                     && (
                        this.Players.getBool() && this.PlayerMode.currentMode.equalsIgnoreCase("Glow")
                           || this.Self.getBool() && this.SelfMode.currentMode.equalsIgnoreCase("Glow")
                           || this.EnderCrystals.getBool()
                           || this.TntPrimed.getBool() && this.TntMode.currentMode.equalsIgnoreCase("Glow")
                     )
            )
         );
      this.settings.add(this.ItemsI = new BoolSettings("Items", true, this));
      this.settings.add(this.EnderPearl = new BoolSettings("EnderPearl", true, this));
      this.settings.add(this.Spawner = new BoolSettings("Spawner", false, this));
      this.settings.add(this.Storage = new BoolSettings("Storage", true, this));
      this.settings.add(this.BreakOver = new BoolSettings("BreakOver", true, this));
      this.settings.add(this.Beacon = new BoolSettings("Beacon", false, this));
      this.settings.add(this.TntPrimed = new BoolSettings("TntPrimed", false, this));
      this.settings.add(this.TntMode = new ModeSettings("Tnt mode", "2D", this, new String[]{"2D", "Glow"}, () -> this.TntPrimed.getBool()));
      this.settings.add(this.Targets = new BoolSettings("Targets", true, this));
      this.settings
         .add(this.TargetsMode = new ModeSettings("Targets mode", "Shape", this, new String[]{"Shape", "Hologram", "Glare"}, () -> this.Targets.getBool()));
      this.settings
         .add(
            this.TargetTexture = new ModeSettings(
               "Target texture",
               "TriangleStapple",
               this,
               new String[]{"QuadStapple", "FuckFinger", "TriangleStapple", "TriangleStipple"},
               () -> this.Targets.getBool() && this.TargetsMode.currentMode.equalsIgnoreCase("Shape")
            )
         );
      this.settings
         .add(
            this.TargetColor = new ModeSettings(
               "Target color", "Client", this, new String[]{"Client", "Pick color", "Pick color & hurt"}, () -> this.Targets.getBool()
            )
         );
      this.settings
         .add(
            this.PickNormal = new ColorSettings(
               "Pick normal",
               ColorUtils.getColor(62, 139, 255, 240),
               this,
               () -> this.Targets.getBool()
                     && (this.TargetColor.currentMode.equalsIgnoreCase("Pick color") || this.TargetColor.currentMode.equalsIgnoreCase("Pick color & hurt"))
            )
         );
      this.settings
         .add(
            this.PickHurt = new ColorSettings(
               "Pick hurt",
               ColorUtils.getColor(255, 61, 84, 255),
               this,
               () -> this.Targets.getBool() && this.TargetColor.currentMode.equalsIgnoreCase("Pick color & hurt")
            )
         );
      this.settings.add(this.VoidHighlight = new BoolSettings("VoidHighlight", true, this));
      this.settings.add(this.Portals = new BoolSettings("Portals", true, this));
      get = this;
   }

   private void setupGlowShader(float radius, float alpha) {
      int color1 = ClientColors.getColor1(0);
      int indexPlus = 1296;
      int step = 5;
      float index = (float)(indexPlus / step);
      int color2 = ClientColors.getColor1((int)index);
      index += (float)(indexPlus / step);
      int color3 = ClientColors.getColor1((int)index);
      index += (float)(indexPlus / step);
      int color4 = ClientColors.getColor1((int)index);
      index += (float)(indexPlus / step);
      int color5 = ClientColors.getColor1((int)index);
      this.glowShader.setUniformi("textureToCheck", 16);
      this.glowShader.setUniformf("radius", radius);
      this.glowShader.setUniformf("texelSize", 1.0F / (float)mc.displayWidth, 1.0F / (float)mc.displayHeight);
      this.glowShader.setUniformColor("color1", color1);
      this.glowShader.setUniformColor("color2", color2);
      this.glowShader.setUniformColor("color3", color3);
      this.glowShader.setUniformColor("color4", color4);
      this.glowShader.setUniformColor("color5", color5);
      this.glowShader.setUniformf("exposure", alpha * 3.0F);
      this.glowShader.setUniformi("avoidTexture", 1);
      this.glowShader.setUniformf("time", 0.0F);
      FloatBuffer buffer = BufferUtils.createFloatBuffer((int)(radius + 1.0F));

      for (int i = 1; (float)i <= radius; i++) {
         buffer.put(MathUtils.calculateGaussianValue((float)i, radius / 2.0F));
      }

      buffer.rewind();
      GL20.glUniform1(this.glowShader.getUniform("weights"), buffer);
   }

   private void setupGlowDirs(float dir1, float dir2) {
      this.glowShader.setUniformf("direction", dir1, dir2);
   }

   private Vector3d project2D(int scaleFactor, double x, double y, double z) {
      GL11.glGetFloat(2982, this.modelview);
      GL11.glGetFloat(2983, this.projection);
      GL11.glGetInteger(2978, this.viewport);
      return GLU.gluProject((float)x, (float)y, (float)z, this.modelview, this.projection, this.viewport, this.vector)
         ? new Vector3d(
            (double)(this.vector.get(0) / (float)scaleFactor),
            (double)(((float)Display.getHeight() - this.vector.get(1)) / (float)scaleFactor),
            (double)this.vector.get(2)
         )
         : null;
   }

   private List<EntityLivingBase> getTargets() {
      List<EntityLivingBase> targets = new ArrayList<>();
      if (get.actived) {
         if (HitAura.TARGET != null) {
            targets.add(HitAura.TARGET);
         }

         if (!CrystalField.getTargets().isEmpty()) {
            for (EntityLivingBase Targets : CrystalField.getTargets()) {
               if (Targets != null) {
                  targets.add(Targets);
               }
            }
         }

         if (BowAimbot.target != null) {
            targets.add(BowAimbot.target);
         }

         EntityLivingBase thTarget = TargetHUD.getTarget();
         if (thTarget != null && thTarget != Minecraft.player) {
            targets.add(thTarget);
         }
      }

      return targets;
   }

   private float[] get2DPosForTESP(Entity entity, float partialTicks, int scaleFactor, RenderManager renderMng) {
      double x = RenderUtils.interpolate(entity.posX, entity.prevPosX, (double)partialTicks);
      double y = RenderUtils.interpolate(entity.posY, entity.prevPosY, (double)partialTicks);
      double z = RenderUtils.interpolate(entity.posZ, entity.prevPosZ, (double)partialTicks);
      double height = (double)(entity.height / (entity instanceof EntityLivingBase && ((EntityLivingBase)entity).isChild() ? 1.75F : 1.0F) + 0.1F);
      AxisAlignedBB aabb = new AxisAlignedBB(x, y + height, z, x, y + height, z);
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
      mc.entityRenderer.setupCameraTransform(partialTicks, 0);
      Vector4d position = null;

      for (Vector3d vector : vectors) {
         vector = this.project2D(scaleFactor, vector.x - RenderManager.viewerPosX, vector.y - RenderManager.viewerPosY, vector.z - RenderManager.viewerPosZ);
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
      return position == null ? new float[]{-1.0F, -1.0F} : new float[]{(float)position.x, (float)position.y};
   }

   private void controlTESPList(List<EntityLivingBase> targets) {
      this.updatedTargets = targets;
      this.updatedTargets.forEach(target -> {
         if (!this.TESP_LIST.stream().anyMatch(tesp -> tesp.entity.getEntityId() == target.getEntityId())) {
            this.TESP_LIST.add(new ESP.TESP(target, 10, 5));
         }
      });
      if (!this.TESP_LIST.isEmpty()) {
         this.TESP_LIST.forEach(ESP.TESP::updateTESP);
         this.TESP_LIST.removeIf(ESP.TESP::isToRemove);
      }
   }

   @Override
   public void onUpdate() {
      this.controlTESPList((List<EntityLivingBase>)(this.TargetsMode.currentMode.equalsIgnoreCase("Hologram") ? this.getTargets() : new ArrayList<>()));
   }

   private ESP.ColVecsWithEnt targetESPSPos(EntityLivingBase entity, int index) {
      EntityRenderer entityRenderer = mc.entityRenderer;
      float partialTicks = mc.getRenderPartialTicks();
      int scaleFactor = ScaledResolution.getScaleFactor();
      double x = RenderUtils.interpolate(entity.posX, entity.lastTickPosX, (double)partialTicks);
      double y = RenderUtils.interpolate(entity.posY, entity.lastTickPosY, (double)partialTicks);
      double z = RenderUtils.interpolate(entity.posZ, entity.lastTickPosZ, (double)partialTicks);
      double height = (double)(entity.height / (entity.isChild() ? 1.75F : 1.0F) / 2.0F);
      double width = 0.0;
      AxisAlignedBB aabb = new AxisAlignedBB(x - 0.0, y, z - 0.0, x + 0.0, y + height, z + 0.0);
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
      entityRenderer.setupCameraTransform(partialTicks, 0);
      Vector4d position = null;

      for (Vector3d vector : vectors) {
         vector = this.project2D(scaleFactor, vector.x - RenderManager.viewerPosX, vector.y - RenderManager.viewerPosY, vector.z - RenderManager.viewerPosZ);
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

      entityRenderer.setupOverlayRendering();
      return position != null
         ? new ESP.ColVecsWithEnt(
            new ESP.Vec2fQuadColored(
               (float)position.x,
               (float)position.y,
               this.getTargetColor(entity, index),
               this.getTargetColor(entity, index + 90),
               this.getTargetColor(entity, index + 180),
               this.getTargetColor(entity, index + 270)
            ),
            entity
         )
         : null;
   }

   private void drawImage(ResourceLocation resource, float x, float y, float x2, float y2, int c, int c2, int c3, int c4) {
      mc.getTextureManager().bindTexture(resource);
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder bufferbuilder = tessellator.getBuffer();
      bufferbuilder.begin(9, DefaultVertexFormats.POSITION_TEX_COLOR);
      bufferbuilder.pos((double)x, (double)y2).tex(0.0, 1.0).color(c).endVertex();
      bufferbuilder.pos((double)x2, (double)y2).tex(1.0, 1.0).color(c2).endVertex();
      bufferbuilder.pos((double)x2, (double)y).tex(1.0, 0.0).color(c3).endVertex();
      bufferbuilder.pos((double)x, (double)y).tex(0.0, 0.0).color(c4).endVertex();
      GL11.glShadeModel(7425);
      GL11.glDepthMask(false);
      tessellator.draw();
      GL11.glDepthMask(true);
      GL11.glShadeModel(7424);
   }

   private void drawImage(ResourceLocation resource, float x, float y, float x2, float y2, int c) {
      this.drawImage(resource, x, y, x2, y2, c, c, c, c);
   }

   private ResourceLocation getTargetTexture(String mode) {
      return switch (mode) {
         case "QuadStapple" -> this.TARGET_2D_ESP;
         case "FuckFinger" -> this.TARGET_2D_ESP2;
         case "TriangleStapple" -> this.TARGET_2D_ESP3;
         case "TriangleStipple" -> this.TARGET_2D_ESP4;
         default -> null;
      };
   }

   private void drawTargetESP(float x, float y, int color, int color2, int color3, int color4, float scale, int index) {
      long millis = System.currentTimeMillis() + (long)index * 400L;
      double angle = MathUtils.clamp((Math.sin((double)millis / 150.0) + 1.0) / 2.0 * 30.0, 0.0, 30.0);
      double scaled = MathUtils.clamp((Math.sin((double)millis / 500.0) + 1.0) / 2.0, 0.8, 1.0);
      double rotate = MathUtils.clamp((Math.sin((double)millis / 1000.0) + 1.0) / 2.0 * 360.0, 0.0, 360.0);
      rotate = (double)(this.TargetTexture.currentMode.equalsIgnoreCase("QuadStapple") ? 45 : 0) - (angle - 15.0) + rotate;
      float size = 128.0F * scale * (float)scaled;
      x -= size / 2.0F;
      y -= size / 2.0F;
      float x2 = x + size;
      float y2 = y + size;
      ResourceLocation resource = this.getTargetTexture(this.TargetTexture.currentMode);
      if (resource != null) {
         GlStateManager.pushMatrix();
         RenderUtils.customRotatedObject2D(x, y, size, size, (double)((float)rotate));
         GL11.glDisable(3008);
         GlStateManager.depthMask(false);
         GlStateManager.enableBlend();
         GlStateManager.shadeModel(7425);
         GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
         );
         this.drawImage(resource, x, y, x2, y2, color, color2, color3, color4);
         GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO
         );
         GlStateManager.resetColor();
         GlStateManager.shadeModel(7424);
         GlStateManager.depthMask(true);
         GL11.glEnable(3008);
         GlStateManager.popMatrix();
      }
   }

   private void drawTargets2D(ESP.ColVecsWithEnt colVecsWithEnt) {
      float dst = Minecraft.player.getSmoothDistanceToEntity(colVecsWithEnt.getEntity());
      float scaled = (1.0F - MathUtils.clamp(Math.abs(dst - 6.0F) / 60.0F, 0.0F, 0.75F)) * colVecsWithEnt.alphaPC.getAnim();
      int color = colVecsWithEnt.colVec.getColor();
      color = ColorUtils.swapAlpha(color, (float)ColorUtils.getAlphaFromColor(color) * colVecsWithEnt.alphaPC.anim);
      int color2 = colVecsWithEnt.colVec.getColor2();
      color2 = ColorUtils.swapAlpha(color2, (float)ColorUtils.getAlphaFromColor(color2) * colVecsWithEnt.alphaPC.anim);
      int color3 = colVecsWithEnt.colVec.getColor3();
      color3 = ColorUtils.swapAlpha(color3, (float)ColorUtils.getAlphaFromColor(color3) * colVecsWithEnt.alphaPC.anim);
      int color4 = colVecsWithEnt.colVec.getColor4();
      color4 = ColorUtils.swapAlpha(color4, (float)ColorUtils.getAlphaFromColor(color4) * colVecsWithEnt.alphaPC.anim);
      this.drawTargetESP(colVecsWithEnt.colVec.x, colVecsWithEnt.colVec.y, color, color2, color3, color4, scaled, colVecsWithEnt.randomIndex);
   }

   private void drawTargets3D(List<EntityLivingBase> targets, float moduleAPC, float pTicks) {
      if (!targets.isEmpty()) {
         long time = System.currentTimeMillis();
         int timeDelayVert = 1000;
         int glareMaxTime = 250;
         int rotIndex = 0;
         int offsetDelay = 9000;
         double rotOffset = (double)((float)(time % (long)offsetDelay) / (float)offsetDelay) * 360.0;

         for (EntityLivingBase target : targets) {
            double rx = target.lastTickPosX + (target.posX - target.lastTickPosX) * (double)pTicks;
            double ry = target.lastTickPosY + (target.posY - target.lastTickPosY) * (double)pTicks;
            double rz = target.lastTickPosZ + (target.posZ - target.lastTickPosZ) * (double)pTicks;
            double yMin = ry + (double)target.height / 8.0;
            double yMax = ry + (double)target.height * 0.875;
            double dst = ((double)target.width / 2.0 + (double)target.width / 2.0 * 1.333333) / 2.0;
            double offsetXZMax = dst / 3.75;

            for (int num = 0; num < 4; num++) {
               boolean up = num % 2 == 0;
               float timePC = (float)((time + (long)((int)((float)timeDelayVert / 3.0F * (float)rotIndex))) % (long)timeDelayVert) / (float)timeDelayVert;
               int dir = num % 2 * 2 - 1;
               double y1 = up ? yMax : yMin;
               double y2 = up ? yMin : yMax;
               double y = MathUtils.lerp(y1, y2, MathUtils.easeInOutQuadWave((double)timePC));
               double yawRot = Math.toRadians(((double)timePC * 360.0 * (double)dir + rotOffset * (double)dir + 720.0) % 360.0) + (double)num * 90.0;
               double distance = dst + offsetXZMax * MathUtils.easeInOutQuadWave((double)timePC);
               Vec3d vec = new Vec3d(rx + Math.sin(yawRot) * distance, y, rz - Math.cos(yawRot) * distance);
               this.GLARES_LIST.add(new ESP.GlareBubble(target, vec, (float)glareMaxTime, (int)yawRot));
            }

            rotIndex++;
         }
      }

      if (!(moduleAPC * 255.0F < 1.0F) && !this.GLARES_LIST.isEmpty()) {
         this.GLARES_LIST.removeIf(ESP.GlareBubble::isToRemove);
         if (!this.GLARES_LIST.isEmpty()) {
            Vec3d compense = this.getCompense().scale(-1.0);
            mc.getTextureManager().bindTexture(this.BLOOM_TEX);
            GL11.glEnable(3042);
            GL11.glShadeModel(7425);
            GL11.glDepthMask(false);
            GL11.glEnable(2929);
            GL11.glAlphaFunc(516, 0.003921569F);
            GL11.glDisable(2884);
            mc.entityRenderer.disableLightmap();
            GL11.glEnable(3553);
            GL11.glBlendFunc(770, 32772);
            this.GLARES_LIST
               .forEach(
                  glare -> {
                     int color = this.getTargetColor(glare.target, glare.colorIndex);
                     float aPC = glare.alphaPC(moduleAPC);
                     float scalePC = 0.333333F + 0.666666F * (float)MathUtils.easeInOutQuad((double)aPC);
                     float scale = 0.35F * scalePC;
                     color = ColorUtils.swapAlpha(color, (float)ColorUtils.getAlphaFromColor(color) / 2.25F * aPC);
                     Vec3d vecx = glare.vec;
                     if (ColorUtils.getAlphaFromColor(color) >= 1) {
                        this.buffer.begin(6, DefaultVertexFormats.POSITION_TEX_COLOR);
                        this.buffer.pos(-0.5, -0.5).tex(0.0, 0.0).color(color).endVertex();
                        this.buffer.pos(0.5, -0.5).tex(1.0, 0.0).color(color).endVertex();
                        this.buffer.pos(0.5, 0.5).tex(1.0, 1.0).color(color).endVertex();
                        this.buffer.pos(-0.5, 0.5).tex(0.0, 1.0).color(color).endVertex();
                        GL11.glPushMatrix();
                        GL11.glTranslated(vecx.xCoord + compense.xCoord, vecx.yCoord + compense.yCoord, vecx.zCoord + compense.zCoord);
                        GL11.glRotated((double)(mc.getRenderManager().playerViewY + WorldRender.get.offYawOrient), 0.0, -1.0, 0.0);
                        GL11.glRotated(
                           (double)(mc.getRenderManager().playerViewX + WorldRender.get.offPitchOrient),
                           mc.gameSettings.thirdPersonView == 2 ? -1.0 : 1.0,
                           0.0,
                           0.0
                        );
                        GL11.glScalef(scale, scale, scale);
                        this.tessellator.draw();
                        GL11.glPopMatrix();
                     }
                  }
               );
            GL11.glBlendFunc(770, 771);
            mc.entityRenderer.enableLightmap();
            GL11.glAlphaFunc(516, 0.1F);
            GL11.glShadeModel(7424);
            GL11.glEnable(2884);
            GL11.glDepthMask(true);
            GlStateManager.resetColor();
         }
      }
   }

   public boolean crystalImprove() {
      return !Panic.stop && canRenderModule() && this.CrystalImprove.getBool();
   }

   private void setupAlphaModule() {
      alphaPC.to = this.actived ? 1.0F : 0.0F;
      if (this.actived && (double)getAlphaPC() > 0.995) {
         alphaPC.setAnim(1.0F);
      }
   }

   private static float getAlphaPC() {
      return alphaPC.getAnim();
   }

   private static boolean canRenderModule() {
      return getAlphaPC() >= 0.03F;
   }

   private final Vec3d getCompense() {
      return new Vec3d(RenderManager.renderPosX, RenderManager.renderPosY, RenderManager.renderPosZ);
   }

   private final void setup3dFor(Runnable render) {
      GL11.glPushMatrix();
      GlStateManager.tryBlendFuncSeparate(
         GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
      );
      GL11.glEnable(3042);
      GL11.glLineWidth(1.0E-5F);
      GL11.glDisable(3553);
      GL11.glDisable(2929);
      GL11.glDisable(2896);
      GL11.glDisable(3008);
      mc.entityRenderer.disableLightmap();
      GL11.glShadeModel(7425);
      GlStateManager.translate(-this.getCompense().xCoord, -this.getCompense().yCoord, -this.getCompense().zCoord);
      render.run();
      GlStateManager.translate(this.getCompense().xCoord, this.getCompense().yCoord, this.getCompense().zCoord);
      GL11.glLineWidth(1.0F);
      GL11.glShadeModel(7424);
      GL11.glEnable(3553);
      GL11.glEnable(2929);
      GL11.glEnable(3008);
      GlStateManager.tryBlendFuncSeparate(
         GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
      );
      GL11.glPopMatrix();
   }

   private void drawBlockPos(AxisAlignedBB aabb, int color) {
      int c1 = ColorUtils.swapAlpha(color, (float)ColorUtils.getAlphaFromColor(color) / 2.6F);
      int c2 = ColorUtils.swapAlpha(color, (float)ColorUtils.getAlphaFromColor(color) / 12.0F);
      RenderUtils.drawCanisterBox(aabb, true, false, true, c1, 0, c2);
   }

   private AxisAlignedBB getAxisExtended(BlockPos pos, AxisAlignedBB aabb, float percent, Vec3d smoothVec) {
      double x1 = aabb.minX - (double)pos.getX() + smoothVec.xCoord;
      double y1 = aabb.minY - (double)pos.getY() + smoothVec.yCoord;
      double z1 = aabb.minZ - (double)pos.getZ() + smoothVec.zCoord;
      double x2 = aabb.maxX - (double)pos.getX() + smoothVec.xCoord;
      double y2 = aabb.maxY - (double)pos.getY() + smoothVec.yCoord;
      double z2 = aabb.maxZ - (double)pos.getZ() + smoothVec.zCoord;
      double XW = x2 - x1;
      double ZW = z2 - z1;
      double YH = y2 - y1;
      Vec3d centerOFAABBox = new Vec3d(x1 + XW / 2.0, y1 + YH / 2.0, z1 + ZW / 2.0);
      AxisAlignedBB finallyAABB = new AxisAlignedBB(
         centerOFAABBox.xCoord - XW / 2.0 * (double)percent,
         centerOFAABBox.yCoord - YH / 2.0 * (double)percent,
         centerOFAABBox.zCoord - ZW / 2.0 * (double)percent,
         centerOFAABBox.xCoord + XW / 2.0 * (double)percent,
         centerOFAABBox.yCoord + YH / 2.0 * (double)percent,
         centerOFAABBox.zCoord + ZW / 2.0 * (double)percent
      );
      return finallyAABB == null ? aabb : finallyAABB;
   }

   private static boolean isDangeon(TileEntityMobSpawner spawner) {
      BlockPos pos = spawner.getPos();
      IBlockState state = mc.world.getBlockState(pos.down());
      Block block = state.getBlock();
      return block == Blocks.MOSSY_COBBLESTONE || block == Blocks.COBBLESTONE;
   }

   private static boolean canPosBeSeenPos(BlockPos posFirst, BlockPos posSecond) {
      return Minecraft.getMinecraft()
            .world
            .rayTraceBlocks(
               new Vec3d((double)posFirst.getX() + 0.5, (double)(posFirst.getY() + 1), (double)posFirst.getZ() + 0.5),
               new Vec3d((double)posSecond.getX() + 0.5, (double)posSecond.getY(), (double)posSecond.getZ() + 0.5),
               false,
               true,
               false
            )
         == null;
   }

   private static boolean posSeenSky(BlockPos pos) {
      return canPosBeSeenPos(pos, new BlockPos(pos.getX(), 256, pos.getZ()));
   }

   private boolean dangeonIsLooted(TileEntityMobSpawner spawner) {
      boolean bad = false;
      List<BlockPos> currentable = new CopyOnWriteArrayList<>();
      int range = 5;

      for (Entity ents : mc.world.getLoadedEntityList()) {
         if (ents != null && ents instanceof EntityItem item) {
            Item cur = item.getItem().getItem();
            if (item.getDistanceToTileEntity(spawner) < 10.0F) {
               for (Item itemL : this.garbageitems) {
                  if (cur == itemL) {
                     bad = true;
                     break;
                  }
               }
            }
         }
      }

      for (int x = spawner.getX() - range; x < spawner.getX() + range; x++) {
         for (int z = spawner.getZ() - range; z < spawner.getZ() + range; z++) {
            currentable.add(new BlockPos(x, spawner.getY(), z));
         }
      }

      for (BlockPos poses : currentable) {
         if (posSeenSky(poses)) {
            bad = true;
         }
      }

      return bad;
   }

   private String[] getSpawnerInfo(TileEntityMobSpawner spawner) {
      String str = "Спавнер";
      String str2 = null;
      String str3 = null;
      spawner.getSpawnerBaseLogic().updateTime();
      int chestCounter = 0;

      for (TileEntity tile : this.getTileEntities(true, false, false, false)) {
         if (tile instanceof TileEntityChest && ((TileEntityChest)tile).getChestType() == BlockChest.Type.BASIC && spawner.getDistanceToTileEntity(tile) < 7.0) {
            chestCounter++;
         }
      }

      if (spawner != null && spawner.getSpawnerBaseLogic() != null && spawner.getSpawnerBaseLogic().cachedEntity != null) {
         if (chestCounter != 0) {
            String slog = chestCounter == 1 || chestCounter == 21 || chestCounter == 31 || chestCounter == 41 || chestCounter == 51
               ? ""
               : (
                  chestCounter % 5 != 0
                        && chestCounter % 6 != 0
                        && chestCounter % 7 != 0
                        && chestCounter % 8 != 0
                        && chestCounter % 9 != 0
                        && chestCounter % 10 != 0
                        && chestCounter % 11 != 0
                        && chestCounter % 12 != 0
                        && chestCounter % 13 != 0
                        && chestCounter % 14 != 0
                        && chestCounter % 15 != 0
                        && chestCounter % 16 != 0
                        && chestCounter % 17 != 0
                        && chestCounter % 18 != 0
                        && chestCounter % 19 != 0
                        && chestCounter % 20 != 0
                     ? (chestCounter % 2 != 0 && chestCounter % 3 != 0 && chestCounter % 4 != 0 ? "" : "а")
                     : "ов"
               );
            str2 = "рядом есть " + chestCounter + " сундук" + slog;
            if (isDangeon(spawner)) {
               str3 = this.dangeonIsLooted(spawner) ? "этот дандж возможно залутан" : "это пригодный дандж";
            }
         }

         Entity pidorVSpavnere = spawner.getSpawnerBaseLogic().cachedEntity;
         str = pidorVSpavnere.getName().trim();
         if (spawner.getSpawnerBaseLogic().isActivated()) {
            str = str + " | до спавна: " + spawner.getSpawnerBaseLogic().timeToSpawn + "сек";
         } else {
            str = str + " | не активен";
         }
      } else if (chestCounter != 0) {
         String slog = chestCounter == 1 || chestCounter == 21 || chestCounter == 31 || chestCounter == 41 || chestCounter == 51
            ? ""
            : (
               chestCounter % 5 == 0
                     || chestCounter % 6 == 0
                     || chestCounter % 7 == 0
                     || chestCounter % 8 == 0
                     || chestCounter % 9 == 0
                     || chestCounter % 10 == 0
                     || chestCounter % 11 == 0
                     || chestCounter % 12 == 0
                     || chestCounter % 13 == 0
                     || chestCounter % 14 == 0
                     || chestCounter % 15 == 0
                     || chestCounter % 16 == 0
                     || chestCounter % 17 == 0
                     || chestCounter % 18 == 0
                     || chestCounter % 19 == 0
                     || chestCounter % 20 == 0
                  ? "ов"
                  : (chestCounter % 2 != 0 && chestCounter % 3 != 0 && chestCounter % 4 != 0 ? "" : "а")
            );
         str2 = "рядом есть " + chestCounter + " сундук" + slog;
         if (isDangeon(spawner)) {
            str3 = this.dangeonIsLooted(spawner) ? "этот дандж возможно залутан" : "это пригодный дандж";
         }
      }

      if (str2 != null && str3 != null) {
         return new String[]{str, str2, str3};
      } else {
         return str2 != null && str3 == null ? new String[]{str, str2} : new String[]{str};
      }
   }

   private int getTileEntityStorageColor(TileEntity storage) {
      int col = -1;
      if (storage instanceof TileEntityChest) {
         col = ColorUtils.getColor(255, 160, 10);
         if (((TileEntityChest)storage).getChestType() == BlockChest.Type.TRAP) {
            col = ColorUtils.getColor(255, 85, 0);
         }
      } else if (storage instanceof TileEntityEnderChest) {
         col = ColorUtils.getColor(120, 0, 160);
      } else if (storage instanceof TileEntityShulkerBox) {
         col = ColorUtils.getColor(255, 48, 255);
      }

      return col;
   }

   public void alwaysPreRender2D(float partialTicks, ScaledResolution sr) {
      this.setupAlphaModule();
      if (canRenderModule()) {
         this.draw2d(partialTicks, sr);
      }
   }

   @Override
   public void alwaysRender3DV2(float partialTicks) {
      if (canRenderModule()) {
         this.draw3d(partialTicks);
      }
   }

   @Override
   public void alwaysRender3D(float partialTicks) {
      if (canRenderModule()) {
         boolean targets = this.Targets.getBool() && this.TargetsMode.currentMode.equalsIgnoreCase("Glare");
         if (targets) {
            this.drawTargets3D(this.getTargets(), getAlphaPC(), partialTicks);
         } else if (!this.GLARES_LIST.isEmpty()) {
            this.GLARES_LIST.clear();
         }
      }
   }

   private void draw2d(float partialTicks, ScaledResolution sr) {
      boolean players = this.Players.getBool() && this.PlayerMode.currentMode.equalsIgnoreCase("2D");
      boolean self = this.Self.getBool() && this.SelfMode.currentMode.equalsIgnoreCase("2D");
      boolean players2 = this.Players.getBool() && this.PlayerMode.currentMode.equalsIgnoreCase("Glow");
      boolean self2 = this.Self.getBool() && this.SelfMode.currentMode.equalsIgnoreCase("Glow");
      boolean items = this.ItemsI.getBool();
      boolean pearl = this.EnderPearl.getBool();
      boolean crystal = this.EnderCrystals.getBool();
      boolean beacons = this.Beacon.getBool();
      boolean spawner = this.Spawner.getBool();
      boolean tnt = this.TntPrimed.getBool() && this.TntMode.currentMode.equalsIgnoreCase("2D");
      boolean tnt2 = this.TntPrimed.getBool() && this.TntMode.currentMode.equalsIgnoreCase("Glow");
      boolean targets = this.Targets.getBool();
      GL11.glEnable(3042);
      if (this.getEntities(players2, self2, false, crystal, false, tnt2).size() != 0) {
         this.drawGlows();
      }

      int scaleFactor = ScaledResolution.getScaleFactor();
      double scaling = (double)scaleFactor / Math.pow((double)scaleFactor, 2.0);
      EntityRenderer entityRenderer = mc.entityRenderer;
      if (this.getTileEntities(false, beacons, spawner, false).size() != 0) {
         GL11.glPushMatrix();
         GL11.glScaled(scaling, scaling, scaling);

         for (TileEntity tile : this.getTileEntities(false, beacons, spawner, false)) {
            double x = (double)tile.getX() + 0.5;
            double y = (double)tile.getY();
            double z = (double)tile.getZ() + 0.5;
            double height = 1.0;
            double width = 1.0;
            AxisAlignedBB aabb = new AxisAlignedBB(x - 0.5, y, z - 0.5, x + 0.5, y + 1.0, z + 0.5);
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
            entityRenderer.setupCameraTransform(partialTicks, 0);
            Vector4d position = null;

            for (Vector3d vector : vectors) {
               vector = this.project2D(
                  scaleFactor, vector.x - RenderManager.viewerPosX, vector.y - RenderManager.viewerPosY, vector.z - RenderManager.viewerPosZ
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

            entityRenderer.setupOverlayRendering();
            if (position != null
               && position.w > 1.0
               && position.z > 1.0
               && position.x < (double)(sr.getScaledWidth() - 1)
               && position.y < (double)(sr.getScaledHeight() - 1)) {
               this.drawEspM(position.x, position.y, position.z, position.w, tile);
            }
         }

         GL11.glPopMatrix();
      }

      if (this.getEntities(players, self, items, false, pearl, tnt).size() != 0) {
         GL11.glPushMatrix();
         GL11.glScaled(scaling, scaling, scaling);

         for (Entity entity : this.getEntities(players, self, items, false, pearl, tnt)) {
            double x = RenderUtils.interpolate(entity.posX, entity.prevPosX, (double)partialTicks);
            double y = RenderUtils.interpolate(entity.posY, entity.prevPosY, (double)partialTicks);
            double z = RenderUtils.interpolate(entity.posZ, entity.prevPosZ, (double)partialTicks);
            double height = (double)(entity.height / (entity instanceof EntityLivingBase && ((EntityLivingBase)entity).isChild() ? 1.75F : 1.0F) + 0.1F);
            double width = (double)(entity.width / (entity instanceof EntityLivingBase && ((EntityLivingBase)entity).isChild() ? 1.5F : 1.0F));
            AxisAlignedBB aabb = new AxisAlignedBB(x - width / 2.0, y, z - width / 2.0, x + width / 2.0, y + height, z + width / 2.0);
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
            entityRenderer.setupCameraTransform(partialTicks, 0);
            Vector4d position = null;

            for (Vector3d vectorx : vectors) {
               vectorx = this.project2D(
                  scaleFactor, vectorx.x - RenderManager.viewerPosX, vectorx.y - RenderManager.viewerPosY, vectorx.z - RenderManager.viewerPosZ
               );
               if (vectorx != null && vectorx.z >= 0.0 && vectorx.z < 1.0) {
                  if (position == null) {
                     position = new Vector4d(vectorx.x, vectorx.y, vectorx.z, 0.0);
                  }

                  position.x = Math.min(vectorx.x, position.x);
                  position.y = Math.min(vectorx.y, position.y);
                  position.z = Math.max(vectorx.x, position.z);
                  position.w = Math.max(vectorx.y, position.w);
               }
            }

            entityRenderer.setupOverlayRendering();
            GL11.glDisable(2896);
            GlStateManager.enableAlpha();
            if (position != null
               && position.w > 1.0
               && position.z > 1.0
               && position.x < (double)(sr.getScaledWidth() - 1)
               && position.y < (double)(sr.getScaledHeight() - 1)) {
               this.drawEspS(position.x, position.y, position.z, position.w, entity);
            }
         }

         GL11.glPopMatrix();
      }

      if (targets && this.TargetsMode.currentMode.equalsIgnoreCase("Shape") && (!this.getTargets().isEmpty() || !this.colVecsWithEntList.isEmpty())) {
         List<ESP.ColVecsWithEnt> colVecsWithEntList1 = this.getTargets()
            .stream()
            .filter(Objects::nonNull)
            .map(target -> this.targetESPSPos(target, 0))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
         colVecsWithEntList1.stream()
            .filter(Objects::nonNull)
            .filter(
               colVecsWithEnt -> this.colVecsWithEntList
                     .stream()
                     .filter(Objects::nonNull)
                     .noneMatch(cv -> cv.getEntity().getUniqueID() == colVecsWithEnt.getEntity().getUniqueID())
            )
            .forEach(colVecsWithEnt -> this.colVecsWithEntList.add(colVecsWithEnt));
         this.colVecsWithEntList.stream().filter(Objects::nonNull).forEach(colVecsWithEnt -> colVecsWithEnt.update(colVecsWithEntList1));
         this.colVecsWithEntList.stream().filter(Objects::nonNull).filter(Objects::nonNull).collect(Collectors.toList()).removeIf(ESP.ColVecsWithEnt::toRemove);
         this.colVecsWithEntList.stream().filter(Objects::nonNull).forEach(colVecsWithEnt -> this.drawTargets2D(colVecsWithEnt));
      }

      if (!this.TESP_LIST.isEmpty()) {
         this.TESP_LIST.forEach(tesp -> tesp.drawTESP(Integer.MIN_VALUE, this.getTargetColor(tesp.getEntity(), 0), -1));
      }
   }

   private int getTargetColor(EntityLivingBase target, int index) {
      if (target != null) {
         float aPC = getAlphaPC();
         this.targetColor = ColorUtils.swapAlpha(ClientColors.getColor1(index), (float)ColorUtils.getAlphaFromColor(ClientColors.getColor1(index)) * aPC);
         if (this.TargetColor.currentMode.equalsIgnoreCase("Pick color")) {
            int c1 = this.PickNormal.color;
            this.targetColor = ColorUtils.swapAlpha(c1, (float)ColorUtils.getAlphaFromColor(c1) * aPC);
         } else if (this.TargetColor.currentMode.equalsIgnoreCase("Pick color & hurt")) {
            float pcH = (float)target.hurtTime / 10.0F;
            int c1 = this.PickNormal.color;
            c1 = ColorUtils.swapAlpha(c1, (float)ColorUtils.getAlphaFromColor(c1) * aPC);
            int c2 = this.PickHurt.color;
            c2 = ColorUtils.swapAlpha(c2, (float)ColorUtils.getAlphaFromColor(c2) * aPC);
            this.targetColor = ColorUtils.getOverallColorFrom(c1, c2, pcH);
         }
      }

      return this.targetColor;
   }

   private void drawGlows() {
      mc.gameSettings.ofFastRender = false;
      if (!Config.isShaders()) {
         float radiusShadow = this.GlowRadius.getFloat() * 2.0F - 1.5F;
         float aPC = getAlphaPC() * (this.GlowBloom.getBool() ? (this.BloomAmount.getFloat() - 0.35F) * 1.3F : 1.0F);
         if (this.framebuffer != null) {
            boolean bloom = this.GlowBloom.getBool();
            GlStateManager.pushMatrix();
            GlStateManager.alphaFunc(516, 0.0F);
            GlStateManager.enableBlend();
            OpenGlHelper.glBlendFunc(770, bloom ? 1 : 771, 1, 0);
            this.glowFrameBuffer.framebufferClear();
            this.glowFrameBuffer.bindFramebuffer(false);
            this.glowShader.attach();
            this.setupGlowShader(radiusShadow, aPC);
            this.setupGlowDirs(1.0F, 0.0F);
            this.framebuffer.bindFramebufferTexture();
            ShaderUtility.drawQuads();
            mc.getFramebuffer().bindFramebuffer(false);
            this.setupGlowDirs(0.0F, 1.0F);
            GlStateManager.setActiveTexture(34000);
            this.framebuffer.bindFramebufferTexture();
            GlStateManager.setActiveTexture(33984);
            this.glowFrameBuffer.bindFramebufferTexture();
            GL11.glDisable(2929);
            this.drawGLTex(0.0F, 0.0F);
            GL11.glEnable(2929);
            this.glowShader.detach();
            GlStateManager.bindTexture(0);
            if (bloom) {
               OpenGlHelper.glBlendFunc(770, 771, 1, 0);
            }

            GlStateManager.alphaFunc(516, 0.1F);
            GlStateManager.enableDepth();
            GlStateManager.disableLighting();
            GlStateManager.popMatrix();
         }
      }
   }

   private void drawGLTex(float pedzX, float pedzY) {
      float width = (float)mc.displayWidth / 2.0F;
      float height = (float)mc.displayHeight / 2.0F;
      GL11.glBegin(6);
      GL11.glTexCoord2d(0.0, 1.0);
      GL11.glVertex2d((double)pedzX, (double)pedzY);
      GL11.glTexCoord2d(0.0, 0.0);
      GL11.glVertex2d((double)pedzX, (double)(height + pedzY));
      GL11.glTexCoord2d(1.0, 0.0);
      GL11.glVertex2d((double)(width + pedzX), (double)(height + pedzY));
      GL11.glTexCoord2d(1.0, 1.0);
      GL11.glVertex2d((double)(width + pedzX), (double)pedzY);
      GL11.glEnd();
   }

   private void drawPlayerESP(double x, double y, double x2, double y2, Entity ent) {
      if (ent instanceof EntityPlayer player && ent.ticksExisted > 3) {
         float alphaPercent = (
               ent instanceof EntityPlayerSP
                  ? 1.0F
                  : MathUtils.clamp(
                     Minecraft.player.getSmoothDistanceToEntity(player) / 5.0F * (Minecraft.player.getSmoothDistanceToEntity(player) / 5.0F), 0.0F, 1.0F
                  )
            )
            * getAlphaPC();
         int c1 = ClientColors.getColor1(0);
         int c2 = ClientColors.getColor2(100);
         int color = ColorUtils.swapAlpha(c1, (float)ColorUtils.getAlphaFromColor(c1) * alphaPercent);
         int color2 = ColorUtils.swapAlpha(c2, (float)ColorUtils.getAlphaFromColor(c2) * alphaPercent);
         int bgCol = ColorUtils.getColor(0, 0, 0, (float)ColorUtils.getAlphaFromColor(ColorUtils.getOverallColorFrom(color, color2)) * alphaPercent * 0.5F);
         float offset = -0.5F;
         RenderUtils.drawLightContureRect(
            x - (double)offset,
            y - (double)offset,
            x2 + (double)offset,
            y2 + (double)offset,
            ColorUtils.getColor(0, 0, 0, (float)ColorUtils.getAlphaFromColor(color) * alphaPercent)
         );
         offset += 0.5F;
         RenderUtils.drawLightContureRectFullGradient(
            (float)(x - (double)offset), (float)(y - (double)offset), (float)(x2 + (double)offset), (float)(y2 + (double)offset), color, color2, false
         );
         offset += 0.5F;
         RenderUtils.drawLightContureRectSmooth(
            x - (double)offset,
            y - (double)offset,
            x2 + (double)offset,
            y2 + (double)offset,
            ColorUtils.getColor(0, 0, 0, (float)ColorUtils.getAlphaFromColor(color) * alphaPercent)
         );
         offset = -0.5F;
         float hpPC = MathUtils.clamp(
            (MathUtils.getDifferenceOf(player.getSmoothHealth(), player.getHealth()) < 0.1F ? player.getHealth() : player.getSmoothHealth())
               / player.getMaxHealth(),
            0.0F,
            1.0F
         );
         float diffYCoord = (float)MathUtils.clamp(y2 - y + 1.0, 0.0, 1.0E7);
         float diffFHP = diffYCoord * hpPC;
         boolean showHpText = (int)(y2 - y + 1.0 - (double)diffFHP) > 10;
         int extXMinus = 4;
         int w = 1;
         int colorHPOverall = ColorUtils.getOverallColorFrom(color2, color, hpPC);
         RenderUtils.drawFullGradientRectPro(
            (float)(x - (double)offset - (double)extXMinus) - 0.5F,
            (float)(y2 - (double)offset - (double)diffFHP),
            (float)(x + (double)offset - (double)extXMinus + (double)w) + 1.0F,
            (float)(y2 + (double)offset) + 1.0F,
            color2,
            color2,
            colorHPOverall,
            colorHPOverall,
            false
         );
         offset += 0.5F;
         RenderUtils.drawLightContureRect(
            x - (double)offset - (double)extXMinus,
            y2 - (double)offset - (double)diffYCoord + 0.5,
            x + (double)offset - (double)extXMinus + (double)w,
            y2 + (double)offset + 0.5,
            ColorUtils.getColor(0, 0, 0, (float)ColorUtils.getAlphaFromColor(color) * alphaPercent)
         );
         color = ColorUtils.swapAlpha(color, (float)ColorUtils.getAlphaFromColor(color) / 4.0F);
         color2 = ColorUtils.swapAlpha(color2, (float)ColorUtils.getAlphaFromColor(color2) / 4.0F);
         RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(
            (float)(x - (double)offset),
            (float)(y - (double)offset),
            (float)(x2 + (double)offset),
            (float)(y2 + (double)offset),
            1.0F,
            16.0F,
            color,
            color2,
            color,
            color2,
            true,
            false,
            true
         );
         if (showHpText) {
            int hpTex = ColorUtils.swapAlpha(ColorUtils.getFixedWhiteColor(), 255.0F * alphaPercent);
            if (255.0F * alphaPercent >= 33.0F) {
               CFontRenderer fontUsed = Fonts.time_14;
               String hpString = String.format("%.1f", hpPC * player.getMaxHealth()).replace(".0", "");
               fontUsed.drawStringWithShadow(
                  hpString,
                  x - (double)offset - (double)extXMinus - (double)fontUsed.getStringWidth(hpString) - 2.0,
                  (double)((float)(y2 - (double)offset - (double)diffFHP) + 3.5F),
                  hpTex
               );
            }
         }
      }
   }

   private static void drawItemESP(double x, double y, double x2, double y2, Entity ent) {
      if (ent instanceof EntityItem itemEnt && itemEnt.ticksExisted > 1) {
         CFontRenderer fontUsed = Fonts.comfortaaBold_12;
         float alphaPercent = MathUtils.clamp(
               Minecraft.player.getSmoothDistanceToEntity(itemEnt) / 2.0F * (Minecraft.player.getSmoothDistanceToEntity(itemEnt) / 2.0F), 0.0F, 1.0F
            )
            * getAlphaPC()
            * MathUtils.clamp((float)itemEnt.ticksExisted / 20.0F, 0.0F, 1.0F);
         ItemStack item = itemEnt.getEntityItem();
         String prefex = item.getDisplayName();
         if (item.stackSize > 1 && !prefex.isEmpty()) {
            prefex = prefex + " §cx" + item.stackSize;
         }

         float w = (float)fontUsed.getStringWidth(prefex);
         int bgColor1 = ColorUtils.swapAlpha(ColorUtils.getOverallColorFrom(ColorUtils.getColor(0), ClientColors.getColor1(), 0.15F), 85.0F * alphaPercent);
         int bgColor2 = ColorUtils.swapAlpha(ColorUtils.getOverallColorFrom(ColorUtils.getColor(0), ClientColors.getColor2(), 0.15F), 85.0F * alphaPercent);
         int bgOutColor1 = ColorUtils.swapAlpha(ColorUtils.getOverallColorFrom(ColorUtils.getColor(0), ClientColors.getColor1(), 0.4F), 185.0F * alphaPercent);
         int bgOutColor2 = ColorUtils.swapAlpha(ColorUtils.getOverallColorFrom(ColorUtils.getColor(0), ClientColors.getColor2(), 0.4F), 185.0F * alphaPercent);
         RenderUtils.drawAlphedSideways(
            (double)((float)(x + (x2 - x) / 2.0 - (double)(w / 2.0F)) - 0.5F),
            y - 8.0,
            (double)((float)(x + (x2 - x) / 2.0 + (double)(w / 2.0F)) + 0.5F),
            y - 1.0,
            bgColor1,
            bgColor2
         );
         RenderUtils.drawLightContureRectSidewaysSmooth(
            (double)((float)(x + (x2 - x) / 2.0 - (double)(w / 2.0F)) - 0.5F),
            y - 8.0,
            (double)((float)(x + (x2 - x) / 2.0 + (double)(w / 2.0F)) + 0.5F),
            y - 1.0,
            bgOutColor1,
            bgOutColor2
         );
         if (255.0F * alphaPercent >= 32.0F) {
            fontUsed.drawString(
               prefex, (double)((float)(x + (x2 - x) / 2.0 - (double)(w / 2.0F))), y - 6.0, ColorUtils.getColor(255, 255, 255, 255.0F * alphaPercent)
            );
         }

         RenderUtils.resetBlender();
         GlStateManager.enableTexture2D();
      }
   }

   private void drawTntPrimedsESP(double x, double y, double x2, double y2, Entity ent) {
      if (ent instanceof EntityTNTPrimed && ent.ticksExisted < 81) {
         float timePC = 1.0F - (float)ent.ticksExisted / 80.0F;
         float scalePush = MathUtils.clamp((1.0F - timePC - 0.9F) * 9.0F, 0.0F, 1.0F);
         float scale = 1.0F + scalePush / 5.0F;
         float alphaPercent = MathUtils.clamp(Minecraft.player.getSmoothDistanceToEntity(ent) * Minecraft.player.getSmoothDistanceToEntity(ent), 0.0F, 1.0F)
            * getAlphaPC();
         int c1 = ClientColors.getColor1(0);
         int c2 = ClientColors.getColor2(90);
         int color = ColorUtils.swapAlpha(c1, (float)ColorUtils.getAlphaFromColor(c1) * alphaPercent);
         int color2 = ColorUtils.swapAlpha(c2, (float)ColorUtils.getAlphaFromColor(c1) * alphaPercent);
         int bgCol = ColorUtils.getColor(0, 0, 0, (float)ColorUtils.getAlphaFromColor(ColorUtils.getOverallColorFrom(color, color2)) * alphaPercent);
         float offset = -1.5F;
         float yMinus = 6.0F;
         RenderUtils.customScaledObject2D((float)(x + (x2 - x) / 2.0), (float)y - yMinus, 0.0F, yMinus, scale);
         RenderUtils.drawLightContureRect(
            x - (double)offset,
            y - (double)yMinus - (double)offset,
            x2 + (double)offset,
            y + (double)offset,
            ColorUtils.getColor(0, 0, 0, (float)ColorUtils.getAlphaFromColor(color) * alphaPercent)
         );
         offset += 0.5F;
         RenderUtils.drawLightContureRectFullGradient(
            (float)(x - (double)offset),
            (float)(y - (double)yMinus - (double)offset),
            (float)(x2 + (double)offset),
            (float)(y + (double)offset),
            color,
            color2,
            false
         );
         offset += 0.5F;
         RenderUtils.drawLightContureRectSmooth(
            x - (double)offset,
            y - (double)yMinus - (double)offset,
            x2 + (double)offset,
            y + (double)offset,
            ColorUtils.getColor(0, 0, 0, (float)ColorUtils.getAlphaFromColor(color) * alphaPercent)
         );
         offset = 0.0F;
         RenderUtils.drawAlphedSideways(
            (double)((float)x + 2.0F),
            (double)((float)(y - (double)yMinus - (double)offset) + 2.0F),
            (double)((float)(x + 2.0 + (x2 - x - 4.0) * (double)timePC)),
            (double)((float)y + offset - 2.0F),
            color,
            color2
         );
         color = ColorUtils.swapAlpha(color, (float)ColorUtils.getAlphaFromColor(color) / 4.0F);
         color2 = ColorUtils.swapAlpha(color2, (float)ColorUtils.getAlphaFromColor(color2) / 4.0F);
         RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(
            (float)(x - (double)offset),
            (float)(y - (double)yMinus - (double)offset),
            (float)(x2 + (double)offset),
            (float)(y + (double)offset),
            1.0F,
            8.0F,
            color,
            color2,
            color,
            color2,
            true,
            false,
            true
         );
         RenderUtils.fixShadows();
         RenderUtils.drawLightContureRectSmooth(
            (double)((float)x + 2.0F),
            (double)((float)(y - (double)yMinus - (double)offset) + 2.0F),
            (double)((float)(x + 2.0 + (x2 - x - 4.0) * (double)timePC)),
            (double)((float)y + offset - 2.0F),
            ColorUtils.getColor(0, 0, 0, (float)ColorUtils.getAlphaFromColor(color) * alphaPercent)
         );
         RenderUtils.customScaledObject2D((float)(x + (x2 - x) / 2.0), (float)y - yMinus, 0.0F, yMinus, 1.0F / scale);
      }
   }

   private void drawEspS(double x, double y, double x2, double y2, Entity ent) {
      this.drawPearlsESP(x, y, x2, y2, ent);
      drawItemESP(x, y, x2, y2, ent);
      this.drawPlayerESP(x, y, x2, y2, ent);
      this.drawTntPrimedsESP(x, y, x2, y2, ent);
   }

   public void drawModalRectWithCustomSizedTexture(float x, float y, float u, float v, int width, int height, float textureWidth, float textureHeight) {
      float f = 1.0F / textureWidth;
      float f1 = 1.0F / textureHeight;
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder bufferbuilder = tessellator.getBuffer();
      bufferbuilder.begin(9, DefaultVertexFormats.POSITION_TEX);
      bufferbuilder.pos((double)x, (double)(y + (float)height), 0.0).tex(0.0, 1.0).endVertex();
      bufferbuilder.pos((double)(x + (float)width), (double)(y + (float)height), 0.0).tex(1.0, 1.0).endVertex();
      bufferbuilder.pos((double)(x + (float)width), (double)y, 0.0).tex(1.0, 0.0).endVertex();
      bufferbuilder.pos((double)x, (double)y, 0.0).tex(0.0, 0.0).endVertex();
      tessellator.draw();
   }

   private void drawPearlsESP(double x, double y, double x2, double y2, Entity ent) {
      if (ent instanceof EntityEnderPearl) {
         float texW = 22.0F;
         float texH = 28.0F;
         float texX = (float)(x + (x2 - x) / 2.0 - (double)(texW / 2.0F));
         float texY = (float)(y - (double)texH);
         int color1 = ClientColors.getColor1(0);
         int color2 = ClientColors.getColor2(-90);
         int color3 = ClientColors.getColor2(0);
         int color4 = ClientColors.getColor1(240);
         mc.getTextureManager().bindTexture(this.PEARL_MARK_TEXTURE);
         Tessellator tessellator = Tessellator.getInstance();
         BufferBuilder bufferbuilder = tessellator.getBuffer();
         bufferbuilder.begin(9, DefaultVertexFormats.POSITION_TEX_COLOR);
         bufferbuilder.pos((double)texX, (double)(texY + texH)).tex(0.0, 1.0).color(color1).endVertex();
         bufferbuilder.pos((double)(texX + texW), (double)(texY + texH)).tex(1.0, 1.0).color(color2).endVertex();
         bufferbuilder.pos((double)(texX + texW), (double)texY).tex(1.0, 0.0).color(color3).endVertex();
         bufferbuilder.pos((double)texX, (double)texY).tex(0.0, 0.0).color(color4).endVertex();
         GL11.glShadeModel(7425);
         tessellator.draw();
         GL11.glTranslated((double)(texX + 2.5F), (double)(texY + 5.5F), 0.0);
         GL11.glEnable(2929);
         GL11.glDepthMask(true);
         RenderUtils.enableStandardItemLighting();
         mc.getRenderItem().renderItemAndEffectIntoGUI(new ItemStack(Items.ENDER_PEARL), 0, 0);
         RenderUtils.disableStandardItemLighting();
         GL11.glDepthMask(false);
         GL11.glDisable(2929);
         GL11.glTranslated((double)(-(texX + 2.5F)), (double)(-(texY + 5.5F)), 0.0);
      }
   }

   private void drawEspM(double x, double y, double x2, double y2, TileEntity tile) {
      if (tile instanceof TileEntityBeacon) {
         this.drawBeacon(x, y, x2, y2, (TileEntityBeacon)tile);
      }

      if (tile instanceof TileEntityMobSpawner) {
         this.drawSpawner(x, y, x2, y2, (TileEntityMobSpawner)tile);
      }
   }

   private void drawBeacon(double x, double y, double x2, double y2, TileEntityBeacon beacon) {
      if (beacon != null) {
         double dst = (double)Minecraft.player.getSmoothDistanceToTileEntity(beacon);
         float alphaPercent = MathUtils.clamp(
               Minecraft.player.getSmoothDistanceToTileEntity(beacon) / 5.0F * (Minecraft.player.getSmoothDistanceToTileEntity(beacon) / 5.0F), 0.0F, 1.0F
            )
            * getAlphaPC();
         int lvl = beacon.getLevels();
         int distMax = 11 + 10 * lvl;
         CFontRenderer fontUsed = Fonts.noise_14;
         int c1 = ClientColors.getColor1();
         int c2 = ClientColors.getColor2(90);
         int color = ColorUtils.swapAlpha(c1, (float)ColorUtils.getAlphaFromColor(c1) * alphaPercent);
         int color2 = ColorUtils.swapAlpha(c2, (float)ColorUtils.getAlphaFromColor(c1) * alphaPercent);
         int bgCol = ColorUtils.getColor(0, 0, 0, (float)ColorUtils.getAlphaFromColor(ColorUtils.getOverallColorFrom(color, color2)) * alphaPercent * 0.5F);
         float offset = -0.5F;
         RenderUtils.drawLightContureRect(
            x - (double)offset,
            y - (double)offset,
            x2 + (double)offset,
            y2 + (double)offset,
            ColorUtils.getColor(0, 0, 0, (float)ColorUtils.getAlphaFromColor(color) * alphaPercent)
         );
         offset += 0.5F;
         RenderUtils.drawLightContureRectFullGradient(
            (float)(x - (double)offset), (float)(y - (double)offset), (float)(x2 + (double)offset), (float)(y2 + (double)offset), color, color2, false
         );
         offset += 0.5F;
         RenderUtils.drawLightContureRectSmooth(
            x - (double)offset,
            y - (double)offset,
            x2 + (double)offset,
            y2 + (double)offset,
            ColorUtils.getColor(0, 0, 0, (float)ColorUtils.getAlphaFromColor(color) * alphaPercent)
         );
         offset = 0.0F;
         color = ColorUtils.swapAlpha(color, (float)ColorUtils.getAlphaFromColor(color) / 4.0F);
         color2 = ColorUtils.swapAlpha(color2, (float)ColorUtils.getAlphaFromColor(color2) / 4.0F);
         RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(
            (float)(x - (double)offset),
            (float)(y - (double)offset),
            (float)(x2 + (double)offset),
            (float)(y2 + (double)offset),
            1.0F,
            8.0F,
            color,
            color2,
            color,
            color2,
            true,
            false,
            true
         );
         double ys = y - 12.0;
         String zalupa = " dist:" + (int)MathUtils.clamp(dst - (double)distMax, 0.0, 1000.0);
         if (zalupa.contains(":0")) {
            zalupa = " voted";
         }

         String beaconInfo = "Маяк: lvl:" + lvl + zalupa;
         float w = (float)(fontUsed.getStringWidth(beaconInfo) / 2);
         fontUsed.drawClientColoredString(beaconInfo, x + (x2 - x) / 2.0 - (double)w, ys, alphaPercent, true);
      }
   }

   private void drawSpawner(double x, double y, double x2, double y2, TileEntityMobSpawner spawner) {
      if (spawner != null) {
         CFontRenderer fontUsed = Fonts.noise_14;
         CFontRenderer fontUsed2 = Fonts.mntsb_12;
         float alphaPercent = MathUtils.clamp(
               Minecraft.player.getSmoothDistanceToTileEntity(spawner) / 2.0F * (Minecraft.player.getSmoothDistanceToTileEntity(spawner) / 2.0F), 0.0F, 1.0F
            )
            * alphaPC.anim;
         int c1 = ClientColors.getColor1();
         int c2 = ClientColors.getColor2();
         int color = ColorUtils.swapAlpha(c1, (float)ColorUtils.getAlphaFromColor(c1) * alphaPercent);
         int color2 = ColorUtils.swapAlpha(c2, (float)ColorUtils.getAlphaFromColor(c1) * alphaPercent);
         float offset = -0.5F;
         RenderUtils.drawLightContureRect(
            x - (double)offset,
            y - (double)offset,
            x2 + (double)offset,
            y2 + (double)offset,
            ColorUtils.getColor(0, 0, 0, (float)ColorUtils.getAlphaFromColor(color) * alphaPercent)
         );
         offset += 0.5F;
         RenderUtils.drawLightContureRectFullGradient(
            (float)(x - (double)offset), (float)(y - (double)offset), (float)(x2 + (double)offset), (float)(y2 + (double)offset), color, color2, false
         );
         offset += 0.5F;
         RenderUtils.drawLightContureRectSmooth(
            x - (double)offset,
            y - (double)offset,
            x2 + (double)offset,
            y2 + (double)offset,
            ColorUtils.getColor(0, 0, 0, (float)ColorUtils.getAlphaFromColor(color) * alphaPercent)
         );
         offset = 0.0F;
         color = ColorUtils.swapAlpha(color, (float)ColorUtils.getAlphaFromColor(color) / 4.0F);
         color2 = ColorUtils.swapAlpha(color2, (float)ColorUtils.getAlphaFromColor(color2) / 4.0F);
         RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(
            (float)(x - (double)offset),
            (float)(y - (double)offset),
            (float)(x2 + (double)offset),
            (float)(y2 + (double)offset),
            1.0F,
            8.0F,
            color,
            color2,
            color,
            color2,
            true,
            false,
            true
         );
         String[] info = this.getSpawnerInfo(spawner);
         double ys = y - 9.0 - (double)(7.5F * (float)(info.length - 1));
         int i = 0;

         for (String str : info) {
            CFontRenderer secondFont = i == 0 ? fontUsed : fontUsed2;
            float w = (float)secondFont.getStringWidth(str) / 2.0F;
            secondFont.drawClientColoredString(str, x + (x2 - x) / 2.0 - (double)w, ys, alphaPercent, true);
            ys += 7.5;
            i++;
         }
      }
   }

   private void renderPlayers(float partialTicks, List<Entity> players) {
      players.forEach(l -> {
         if (l != null && l.isEntityAlive()) {
            boolean e = l.getAlwaysRenderNameTag();
            l.setAlwaysRenderNameTag(false);
            ModelEnderCrystal.canDeformate = false;
            mc.getRenderManager().renderEntityStaticNoShadow(l, partialTicks, true);
            ModelEnderCrystal.canDeformate = true;
            l.setAlwaysRenderNameTag(e);
         }
      });
   }

   private void renderOverSelect(float alphaPC) {
      boolean isHitting = mc.playerController.getIsHittingBlock();
      float progressUpdated01 = mc.playerController.curBlockDamageMP;
      this.alphaSelectPC.to = isHitting ? 1.0F : 0.0F;
      this.alphaSelectPC.speed = isHitting ? 0.1F : 0.05F;
      float alphaIn = this.alphaSelectPC.getAnim();
      this.progressingSelect.to = MathUtils.clamp(progressUpdated01 * 1.005F, 0.0F, 1.0F);
      this.progressingSelect.speed = isHitting ? 0.1F : 0.145F;
      float smoothProgressing = this.progressingSelect.getAnim();
      float speedAnim = (float)Minecraft.frameTime * 0.01F;
      this.smoothPosSelect.xCoord = MathUtils.getDifferenceOf(this.smoothPosSelect.xCoord, (double)this.posOver.getX()) > 6.0
         ? (double)this.posOver.getX()
         : (double)MathUtils.lerp((float)this.smoothPosSelect.xCoord, (float)this.posOver.getX(), speedAnim);
      this.smoothPosSelect.yCoord = MathUtils.getDifferenceOf(this.smoothPosSelect.yCoord, (double)this.posOver.getY()) > 6.0
         ? (double)this.posOver.getY()
         : (double)MathUtils.lerp((float)this.smoothPosSelect.yCoord, (float)this.posOver.getY(), speedAnim);
      this.smoothPosSelect.zCoord = MathUtils.getDifferenceOf(this.smoothPosSelect.zCoord, (double)this.posOver.getZ()) > 6.0
         ? (double)this.posOver.getZ()
         : (double)MathUtils.lerp((float)this.smoothPosSelect.zCoord, (float)this.posOver.getZ(), speedAnim);
      if (alphaPC * alphaIn >= 0.05F) {
         this.setup3dFor(
            () -> this.drawBlockPos(
                  this.getAxisExtended(
                     this.posOver, mc.world.getBlockState(this.posOver).getSelectedBoundingBox(mc.world, this.posOver), smoothProgressing, this.smoothPosSelect
                  ),
                  ColorUtils.swapAlpha(ClientColors.getColor1(), (float)ColorUtils.getAlphaFromColor(ClientColors.getColor1()) * alphaIn * alphaPC)
               )
         );
      }
   }

   private AxisAlignedBB getStorageTileAABB(TileEntity storage) {
      BlockPos pos = storage.getPos();
      int x = pos.getX();
      int y = pos.getY();
      int z = pos.getZ();
      Vec3d vec2 = new Vec3d((double)x, (double)y, (double)z);
      Vec3d vec = new Vec3d((double)x, (double)y, (double)z);
      if (storage instanceof TileEntityChest chest) {
         if (chest.adjacentChestZNeg != null) {
            vec = new Vec3d((double)x + 0.0625, (double)y, (double)z - 0.9375);
            vec2 = new Vec3d((double)x + 0.9375, (double)y + 0.875, (double)z + 0.9375);
         } else if (chest.adjacentChestXNeg != null) {
            vec = new Vec3d((double)x + 0.9375, (double)y, (double)z + 0.0625);
            vec2 = new Vec3d((double)x - 0.9375, (double)y + 0.875, (double)z + 0.9375);
         } else if (chest.adjacentChestXPos == null && chest.adjacentChestZPos == null) {
            vec = new Vec3d((double)x + 0.0625, (double)y, (double)z + 0.0625);
            vec2 = new Vec3d((double)x + 0.9375, (double)y + 0.875, (double)z + 0.9375);
         }
      }

      if (storage instanceof TileEntityEnderChest chestx) {
         vec = new Vec3d((double)x + 0.0625, (double)y, (double)z + 0.0625);
         vec2 = new Vec3d((double)x + 0.9375, (double)y + 0.875, (double)z + 0.9375);
      }

      if (storage instanceof TileEntityShulkerBox chestx) {
         vec = new Vec3d((double)x, (double)y, (double)z);
         AxisAlignedBB aabbs = mc.world.getBlockState(pos).getSelectedBoundingBox(mc.world, pos);
         double h = aabbs.maxY - aabbs.minY;
         vec2 = new Vec3d((double)(x + 1), (double)y + h, (double)(z + 1));
      }

      return new AxisAlignedBB(vec.xCoord, vec.yCoord, vec.zCoord, vec2.xCoord, vec2.yCoord, vec2.zCoord);
   }

   private void drawStorages(float alphaPC) {
      this.setup3dFor(() -> this.getTileEntities(true, false, false, false).forEach(l -> this.drawStorageEsp(l, alphaPC)));
   }

   private void drawStorageEsp(TileEntity storage, float alphaPC) {
      if (storage != null) {
         double distance = Minecraft.player == null ? 0.0 : (double)Minecraft.player.getSmoothDistanceToTileEntity(storage);
         float dstPCAlpha = (float)MathUtils.clamp(distance / 10.0 * (distance / 10.0), 0.0, 1.0);
         float yawDiff = (float)MathUtils.getDifferenceOf(
            Minecraft.player.rotationYaw,
            RotationUtil.getNeededFacing(
               new Vec3d((double)storage.getX(), (double)storage.getY(), (double)storage.getZ()).addVector(0.5, 0.5, 0.5), false, Minecraft.player, false
            )[0]
         );
         yawDiff = (float)((double)yawDiff * MathUtils.clamp(1.3F - (45.0 - MathUtils.getDifferenceOf(Minecraft.player.rotationPitch, 0.0F)) / 90.0, 0.0, 1.0));
         float yawPC = MathUtils.clamp(0.1F + yawDiff / 90.0F, 0.0F, 1.0F);
         int storageColor = this.getTileEntityStorageColor(storage);
         this.drawBlockPos(
            this.getStorageTileAABB(storage),
            ColorUtils.swapAlpha(storageColor, (float)ColorUtils.getAlphaFromColor(storageColor) * alphaPC * dstPCAlpha * yawPC)
         );
      }
   }

   private void setupDrawPearl(Runnable render) {
      GL11.glPushMatrix();
      GL11.glEnable(3042);
      GL11.glBlendFunc(770, 32772);
      GL11.glEnable(2848);
      GL11.glEnable(2929);
      GL11.glShadeModel(7425);
      mc.entityRenderer.disableLightmap();
      GL11.glDisable(3553);
      GL11.glDisable(3008);
      GL11.glDepthMask(false);
      RenderUtils.disableStandardItemLighting();
      GL11.glTranslated(-this.getCompense().xCoord, -this.getCompense().yCoord, -this.getCompense().zCoord);
      render.run();
      GL11.glTranslated(this.getCompense().xCoord, this.getCompense().yCoord, this.getCompense().zCoord);
      GL11.glDepthMask(true);
      GL11.glBlendFunc(770, 771);
      GL11.glEnable(3553);
      GL11.glShadeModel(7424);
      GL11.glDisable(2848);
      GL11.glLineWidth(1.0F);
      GL11.glEnable(3042);
      GL11.glEnable(3008);
      GL11.glEnable(2929);
      GL11.glPopMatrix();
   }

   private void drawPearls(float alphaPC) {
      this.setupDrawPearl(() -> {
         GL11.glBlendFunc(770, 32772);

         for (Entity ent : this.getEntities(false, false, false, false, true, false)) {
            this.tesselatePearl((EntityEnderPearl)ent, alphaPC);
         }

         GL11.glBlendFunc(770, 771);
      });
   }

   private void drawEndPortals(float alphaPC) {
      List<TileEntityEndPortal> portals = new ArrayList<>();
      this.getTileEntities(false, false, false, true).forEach(tile -> {
         if (tile instanceof TileEntityEndPortal portalx) {
            portals.add(portalx);
         }
      });
      if (!portals.isEmpty()) {
         List<TileEntityEndPortal> portalsToDraw = new ArrayList<>();

         for (TileEntityEndPortal portal : portals) {
            if (portals.stream()
                  .filter(
                     portalBeta -> (
                              Math.abs(portal.getPos().getX() - portalBeta.getPos().getX()) == 1
                                    && Math.abs(portal.getPos().getZ() - portalBeta.getPos().getZ()) < 2
                                 || Math.abs(portal.getPos().getZ() - portalBeta.getPos().getZ()) == 1
                                    && Math.abs(portal.getPos().getX() - portalBeta.getPos().getX()) < 2
                           )
                           && portal.getPos().getY() == portalBeta.getPos().getY()
                  )
                  .toList()
                  .size()
               == 8) {
               portalsToDraw.add(portal);
            }
         }

         this.setup3dFor(
            () -> {
               for (TileEntityEndPortal portalx : portalsToDraw) {
                  AxisAlignedBB aabb = mc.world.getBlockState(portalx.getPos()).getSelectedBoundingBox(mc.world, portalx.getPos()).addExpandXZ(1.0);
                  aabb = aabb.offset(0.0, aabb.maxY - aabb.minY, 0.0);
                  if (aabb != null) {
                     float aPC = 1.0F;
                     int espFillColor = ColorUtils.getColor(0, 160, 155, 95);
                     int espOutlineColor = ColorUtils.getColor(40, 70, 255, 195);
                     espFillColor = ColorUtils.swapAlpha(espFillColor, (float)ColorUtils.getAlphaFromColor(espFillColor) * aPC * alphaPC);
                     espOutlineColor = ColorUtils.swapAlpha(espOutlineColor, (float)ColorUtils.getAlphaFromColor(espFillColor) * aPC * alphaPC);
                     RenderUtils.drawGradientAlphaBox(aabb.setMaxY(aabb.maxY + 2.0), espOutlineColor != 0, espFillColor != 0, espOutlineColor, espFillColor);
                     GL11.glPushMatrix();
                     GL11.glScaled(1.0, -1.0, 1.0);
                     GL11.glTranslated(0.0, -aabb.minY * 2.0, 0.0);
                     RenderUtils.drawGradientAlphaBoxWithBooleanDownPool(aabb, false, espFillColor != 0, false, 0, espFillColor);
                     GL11.glPopMatrix();
                     GL11.glPushMatrix();
                     int index = (int)portalx.getPos().toLong();
                     long timeAnimMax = 2000L;
                     float deltaTime = (float)((System.currentTimeMillis() + (long)index) % timeAnimMax) / (float)timeAnimMax;
                     float deltaAnim = (double)deltaTime > 0.5 ? 1.0F - deltaTime : deltaTime;
                     deltaAnim *= deltaAnim;
                     GL11.glTranslated(0.0, (double)(2.0F + deltaAnim), 0.0);
                     GL11.glTranslated((double)portalx.getX() + 0.5, (double)portalx.getY() + 0.5, (double)portalx.getZ() + 0.5);
                     GL11.glRotated((double)(deltaTime * 360.0F), 0.0, 1.0, 0.0);
                     GL11.glRotated((double)(45.0F + deltaTime * 180.0F), 1.0, 0.0, 1.0);
                     GL11.glTranslated(-((double)portalx.getX() + 0.5), -((double)portalx.getY() + 0.5), -((double)portalx.getZ() + 0.5));
                     int frags = 40;

                     for (int i = 0; i < frags; i++) {
                        float iPC = (float)i / (float)frags;
                        int flagCol = ColorUtils.swapAlpha(
                           ColorUtils.fadeColor(espFillColor, espOutlineColor, 0.5F, (int)(iPC * 1000.0F)),
                           255.0F * ((double)iPC > 0.5 ? 1.0F - iPC : iPC) * 2.0F
                        );
                        float offsetCube = 0.1F + 0.25F * iPC;
                        GL11.glTranslated((double)portalx.getX() + 0.5, (double)portalx.getY() + 0.5, (double)portalx.getZ() + 0.5);
                        GL11.glRotated((double)(50.0F * deltaAnim * (0.25F + iPC * 0.75F)), 1.0, 0.0, 1.0);
                        GL11.glTranslated(-((double)portalx.getX() + 0.5), -((double)portalx.getY() + 0.5), -((double)portalx.getZ() + 0.5));
                        RenderUtils.drawCanisterBox(
                           new AxisAlignedBB(
                              (double)portalx.getX() + 0.5 - (double)offsetCube,
                              (double)portalx.getY() + 0.5 - (double)offsetCube,
                              (double)portalx.getZ() + 0.5 - (double)offsetCube,
                              (double)portalx.getX() + 0.5 + (double)offsetCube,
                              (double)portalx.getY() + 0.5 + (double)offsetCube,
                              (double)portalx.getZ() + 0.5 + (double)offsetCube
                           ),
                           false,
                           true,
                           false,
                           flagCol,
                           flagCol,
                           flagCol
                        );
                     }

                     GL11.glPopMatrix();
                  }
               }
            }
         );
      }
   }

   private void tesselatePearl(EntityEnderPearl entityEnderPearl, float darknessUnvalue) {
      double posX = RenderUtils.interpolate(entityEnderPearl.posX, entityEnderPearl.prevPosX, (double)mc.getRenderPartialTicks());
      double posY = RenderUtils.interpolate(entityEnderPearl.posY, entityEnderPearl.prevPosY, (double)mc.getRenderPartialTicks());
      double posZ = RenderUtils.interpolate(entityEnderPearl.posZ, entityEnderPearl.prevPosZ, (double)mc.getRenderPartialTicks());
      double motionX = entityEnderPearl.motionX;
      double motionY = entityEnderPearl.motionY;
      double motionZ = entityEnderPearl.motionZ;
      List<Vec3d> vectors = new ArrayList<>();
      vectors.add(new Vec3d(posX, posY, posZ));

      for (int i = 0; i < 90; i++) {
         Vec3d lastPos = new Vec3d(posX, posY, posZ);
         posX += motionX;
         posY += motionY;
         posZ += motionZ;
         double factor = 0.99;
         BlockPos blockPos = new BlockPos(posX, posY, posZ);
         if (mc.world.getBlockState(blockPos).getBlock() == Blocks.WATER) {
            factor = 0.8;
         }

         motionX *= factor;
         motionY *= factor;
         motionZ *= factor;
         if (!entityEnderPearl.hasNoGravity()) {
            motionY -= 0.03;
         }

         RayTraceResult result;
         if ((result = mc.world.rayTraceBlocks(lastPos, new Vec3d(posX, posY, posZ))) != null) {
            posX = result.hitVec.xCoord;
            posY = result.hitVec.yCoord;
            posZ = result.hitVec.zCoord;
            vectors.add(new Vec3d(posX, posY, posZ));
            break;
         }

         vectors.add(new Vec3d(posX, posY, posZ));
      }

      if (!vectors.isEmpty()) {
         int counter = 0;
         int counterMax = vectors.size();
         this.buffer.begin(3, DefaultVertexFormats.POSITION_COLOR);

         for (Vec3d vec : vectors) {
            if (!(++counter % 2 != 0 ^ counterMax % 2 == 0)) {
               float pc = (float)counter / (float)counterMax;
               pc = ((double)pc > 0.5 ? 1.0F - pc : pc) * 2.0F;
               pc = pc > 1.0F ? 1.0F : (pc < 0.0F ? 0.0F : pc);
               float aPC = MathUtils.clamp((float)counterMax / 20.0F, 0.0F, 1.0F) / 2.0F;
               int col = ColorUtils.getOverallColorFrom(
                  ClientColors.getColor1(0, darknessUnvalue / 7.0F * aPC), ClientColors.getColor2(0, darknessUnvalue * aPC), pc
               );
               this.buffer.pos(vec.xCoord, vec.yCoord, vec.zCoord).color(col).endVertex();
            }
         }

         GL11.glLineWidth(1.0F);
         this.tessellator.vboUploader.draw(this.buffer, false);
         GL11.glLineWidth(3.5F);
         this.tessellator.vboUploader.draw(this.buffer, true);
         this.buffer.finishDrawing();
         GL11.glLineWidth(1.0F);
      }
   }

   private void rect3dXZ(double x, double z, double x2, double z2, double y) {
      GL11.glBegin(5);
      GL11.glVertex3d(x, y, z);
      GL11.glVertex3d(x2, y, z);
      GL11.glVertex3d(x2, y, z2);
      GL11.glVertex3d(x, y, z2);
      GL11.glVertex3d(x2, y, z2);
      GL11.glVertex3d(x, y, z2);
      GL11.glVertex3d(x, y, z);
      GL11.glVertex3d(x2, y, z);
      GL11.glEnd();
   }

   private void drawVoidWarn(float partialTicks) {
      RenderUtils.setup3dForBlockPos(
         () -> {
            GL11.glTranslated(this.getCompense().xCoord, 0.0, this.getCompense().zCoord);
            double yLevel = -60.0;
            double extXZIN = 8.0;
            EntityPlayer selfEntity = (EntityPlayer)(FreeCam.fakePlayer != null && FreeCam.get.actived ? FreeCam.fakePlayer : Minecraft.player);
            if (selfEntity.posY < 1.0) {
               float padding = selfEntity.posY < yLevel ? 500.0F : 750.0F;
               float timePC = (float)(System.currentTimeMillis() % (long)((int)padding)) / padding;
               timePC = (double)timePC > 0.5 ? 1.0F - timePC : timePC;
               timePC *= 2.0F;
               double yDiff = RenderUtils.interpolate(selfEntity.posY, selfEntity.prevPosY, (double)partialTicks) - yLevel;
               double width = 1.0 + (double)((double)timePC > 0.5 ? 1.0F - timePC : timePC) * MathUtils.clamp(yDiff / 8.0, 0.0, 1.0);
               if (yDiff < extXZIN) {
                  extXZIN = yDiff > 0.0 ? yDiff : 0.0;
               }

               extXZIN -= width / 2.0;
               extXZIN = extXZIN < 0.0 ? 0.0 : extXZIN;
               yDiff = Math.abs(yDiff);
               int voidColorize = selfEntity.posY < yLevel
                  ? ColorUtils.getOverallColorFrom(ColorUtils.getColor(255, 0, 0), 0, timePC)
                  : ColorUtils.getOverallColorFrom(ColorUtils.getColor(255, 70, 70), ColorUtils.getColor(255, 70, 70, 75), timePC);
               float voidAlpha = 0.2F + 0.8F * (float)MathUtils.clamp((32.0 - yDiff) / 32.0, 0.0, 1.0);
               RenderUtils.setupColor(voidColorize, voidAlpha * 255.0F * ColorUtils.getGLAlphaFromColor(voidColorize));
               this.rect3dXZ(-extXZIN - width, -extXZIN - width, extXZIN + width, -extXZIN, yLevel);
               this.rect3dXZ(-extXZIN - width, extXZIN, extXZIN + width, extXZIN + width, yLevel);
               this.rect3dXZ(-extXZIN - width, -extXZIN, -extXZIN, extXZIN, yLevel);
               this.rect3dXZ(extXZIN, -extXZIN, extXZIN + width, extXZIN, yLevel);
               if (selfEntity.posY > yLevel - 2.0) {
                  GL11.glTranslated(0.0, yLevel - 10.0, 0.0);
                  GL11.glEnable(3553);
                  GlStateManager.resetColor();
                  float sizeXZ = (float)(0.18F + (double)(0.05F * ((double)timePC > 0.5 ? 1.0F - timePC : timePC)) * MathUtils.clamp(yDiff / 8.0, 0.0, 1.0));
                  GL11.glBlendFunc(770, 771);
                  GL11.glEnable(3042);
                  GL11.glTranslated((double)sizeXZ, 0.0, (double)sizeXZ);
                  GL11.glRotated(180.0, 0.0, 0.0, 1.0);
                  GL11.glRotated(90.0, 1.0, 0.0, 0.0);
                  GL11.glRotated(180.0, 0.0, 1.0, 0.0);
                  GL11.glTranslated(0.0, 0.0, -1.0E-5);
                  GL11.glRotated((double)Minecraft.player.rotationYaw, 0.0, 0.0, 1.0);
                  GL11.glRotated((double)MathUtils.clamp(Minecraft.player.rotationPitch - 45.0F, -45.0F, -10.0F), 1.0, 0.0, 0.0);
                  GL11.glTranslated(0.0, 23.0 + width + 0.5, 0.0);
                  GL11.glScaled((double)(-sizeXZ), (double)(-sizeXZ), (double)sizeXZ);
                  String v = "!!!VOID!!!";
                  String d = "Dst to.. = "
                     + (int)MathUtils.clamp(RenderUtils.interpolate(selfEntity.posY, selfEntity.prevPosY, (double)partialTicks) - yLevel, 0.0, 100000.0);
                  RenderUtils.drawRect(
                     (double)((float)(-Fonts.mntsb_36.getStringWidth(v)) / 2.0F - 5.0F),
                     -14.0,
                     (double)((float)Fonts.mntsb_36.getStringWidth(v) / 2.0F + 5.0F),
                     21.0,
                     ColorUtils.getColor(255, 50)
                  );
                  RenderUtils.drawLightContureRectSmooth(
                     (double)((float)(-Fonts.mntsb_36.getStringWidth(v)) / 2.0F - 5.0F),
                     -14.0,
                     (double)((float)Fonts.mntsb_36.getStringWidth(v) / 2.0F + 5.0F),
                     21.0,
                     -1
                  );
                  GL11.glTranslated(0.0, 0.0, -0.01F);
                  Fonts.mntsb_36.drawStringWithShadow(v, (double)(-Fonts.mntsb_36.getStringWidth(v) / 2), -9.0, voidColorize);
                  Fonts.mntsb_20.drawStringWithShadow(d, (double)(-Fonts.mntsb_20.getStringWidth(d) / 2), 9.0, voidColorize);
                  GL11.glDisable(3553);
                  GlStateManager.disableLighting();
                  RenderUtils.disableStandardItemLighting();
                  GlStateManager.resetColor();
               }
            }
         },
         false
      );
   }

   private void draw3d(float partialTicks) {
      boolean players = this.Players.getBool() && this.PlayerMode.currentMode.equalsIgnoreCase("Glow");
      boolean self = this.Self.getBool() && this.SelfMode.currentMode.equalsIgnoreCase("Glow");
      boolean crystal = this.EnderCrystals.getBool();
      boolean breakOver = this.BreakOver.getBool();
      boolean storages = this.Storage.getBool();
      boolean pearls = this.EnderPearl.getBool();
      boolean tnt = this.TntPrimed.getBool() && this.TntMode.currentMode.equalsIgnoreCase("Glow");
      boolean voidHL = this.VoidHighlight.getBool();
      boolean portals = this.Portals.getBool();
      if (voidHL) {
         this.drawVoidWarn(partialTicks);
      }

      if (breakOver) {
         this.renderOverSelect(getAlphaPC());
      }

      if (storages && this.getTileEntities(storages, false, false, false).size() != 0) {
         this.drawStorages(getAlphaPC());
      }

      if (pearls && this.getEntities(false, false, false, false, pearls, false).size() != 0) {
         this.drawPearls(getAlphaPC());
      }

      if (portals && this.getTileEntities(false, false, false, portals).size() != 0) {
         this.drawEndPortals(getAlphaPC());
      }

      if (this.getEntities(players, self, false, crystal, false, tnt).size() != 0) {
         this.framebuffer = RenderUtils.createFrameBuffer(this.framebuffer);
         this.glowFrameBuffer = RenderUtils.createFrameBuffer(this.glowFrameBuffer);
         if (this.framebuffer == null) {
            return;
         }

         List<Entity> listDraw = this.getEntities(players, self, false, crystal, false, tnt);
         this.framebuffer.framebufferClear();
         this.framebuffer.bindFramebuffer(true);
         GL11.glAlphaFunc(516, 0.1F);
         this.renderPlayers(partialTicks, listDraw);
         this.framebuffer.unbindFramebuffer();
         mc.getFramebuffer().bindFramebuffer(true);
      }
   }

   private List<Entity> getEntities(boolean players, boolean self, boolean items, boolean crystal, boolean pearl, boolean tnt) {
      List<Entity> list = new ArrayList<>();
      if (mc.world != null) {
         mc.world
            .getLoadedEntityList()
            .forEach(
               l -> {
                  if (l != null
                     && l.isEntityAlive()
                     && (
                        l instanceof EntityPlayerSP && mc.gameSettings.thirdPersonView != 0
                           || (RenderUtils.isInView(l) || l instanceof EntityEnderPearl || l instanceof EntityTNTPrimed) && !(l instanceof EntityPlayerSP)
                     )
                     && (
                        l instanceof EntityOtherPlayerMP && players
                           || l instanceof EntityPlayerSP && self
                           || l instanceof EntityItem && items
                           || l instanceof EntityEnderCrystal && crystal
                           || l instanceof EntityEnderPearl && pearl
                           || l instanceof EntityTNTPrimed && tnt
                     )) {
                     list.add(l);
                  }
               }
            );
      }

      return list;
   }

   private List<TileEntity> getTileEntities(boolean storages, boolean beacon, boolean spawner, boolean endPortal) {
      List<TileEntity> list = new ArrayList<>();
      if (mc.world != null) {
         mc.world
            .getLoadedTileEntityList()
            .forEach(
               l -> {
                  if (l != null
                     && (
                        (l instanceof TileEntityChest || l instanceof TileEntityEnderChest || l instanceof TileEntityShulkerBox) && storages
                           || l instanceof TileEntityBeacon && beacon
                           || l instanceof TileEntityMobSpawner && spawner
                           || l instanceof TileEntityEndPortal && endPortal
                     )) {
                     list.add(l);
                  }
               }
            );
      }

      return list;
   }

   private class ColVecsWithEnt {
      public int randomIndex = (int)(50000.0 * Math.random());
      ESP.Vec2fQuadColored colVec;
      EntityLivingBase base;
      public AnimationUtils alphaPC = new AnimationUtils(0.0F, 1.0F, 0.04F);

      public ColVecsWithEnt(ESP.Vec2fQuadColored colVec, EntityLivingBase base) {
         this.colVec = colVec;
         this.base = base;
      }

      public EntityLivingBase getEntity() {
         return this.base;
      }

      public void update(List<ESP.ColVecsWithEnt> anothers) {
         if (this.base != null) {
            EntityLivingBase searched = Module.mc
               .world
               .getLoadedEntityList()
               .stream()
               .map(Entity::getLivingBaseOf)
               .filter(Objects::nonNull)
               .filter(e -> e.getUniqueID() == this.base.getUniqueID())
               .findAny()
               .orElse(null);
            if (searched != null) {
               this.base = searched;
            }
         }

         if (anothers.stream().noneMatch(cv -> cv.getEntity().getUniqueID() == this.base.getUniqueID())) {
            if ((double)this.alphaPC.getAnim() > 0.995) {
               this.alphaPC.to = 0.0F;
            }
         } else if (this.alphaPC.to == 0.0F) {
            this.alphaPC.to = 1.0F;
         }

         ESP.ColVecsWithEnt newColVec;
         if (this.getEntity() != null && (newColVec = ESP.this.targetESPSPos(this.getEntity(), this.randomIndex)) != null) {
            this.colVec = newColVec.colVec;
         }
      }

      public boolean toRemove() {
         return this.alphaPC.to == 0.0F && (double)this.alphaPC.getAnim() < 0.01 || this.getEntity() == null;
      }
   }

   private class GlareBubble {
      EntityLivingBase target;
      Vec3d vec;
      long startTime = System.currentTimeMillis();
      float maxTime;
      int colorIndex;

      GlareBubble(EntityLivingBase target, Vec3d vec, float maxTime, int colorIndex) {
         this.target = target;
         this.vec = vec;
         this.maxTime = maxTime;
         this.colorIndex = colorIndex;
      }

      float alphaPC(float moduleAlphaPC) {
         return (1.0F - MathUtils.clamp((float)(System.currentTimeMillis() - this.startTime) / this.maxTime, 0.0F, 1.0F)) * moduleAlphaPC;
      }

      boolean isToRemove() {
         return (float)(System.currentTimeMillis() - this.startTime) >= this.maxTime;
      }
   }

   private class TESP {
      EntityLivingBase entity;
      float x;
      float y;
      float triangleScale;
      AnimationUtils alphaPC = new AnimationUtils(0.0F, 1.0F, 0.05F);
      int storeyCount;
      List<ESP.TriangleElement> TElementLIST = new ArrayList<>();
      boolean toRemove;

      TESP(EntityLivingBase entity, int triangleScale, int storeyCount) {
         this.entity = entity;
         this.triangleScale = (float)triangleScale;
         this.storeyCount = storeyCount;
         this.updateRenderPos();
         this.genTriangleElements((float)triangleScale, storeyCount);
      }

      void genTriangleElements(float triangleScale, int storeyCount) {
         float triangleY = triangleScale;

         for (int yStepI = 0; yStepI < storeyCount; yStepI++) {
            float triangleX = -triangleScale / 1.5F * (float)yStepI;
            triangleY -= triangleScale;
            boolean yIndexHached = ((double)yStepI + (double)storeyCount / 1.5) % 2.0 == 0.0;

            for (int xStepI = 0; xStepI < yStepI * 2 + 1; xStepI++) {
               boolean upSide = xStepI % 2 == (yIndexHached ? 0 : 1);
               this.TElementLIST.add(ESP.this.new TriangleElement(triangleX, triangleY, upSide, triangleScale));
               triangleX += triangleScale / 1.5F;
            }
         }
      }

      float getX() {
         return this.x;
      }

      float getY() {
         return this.y;
      }

      void setX(float x) {
         this.x = x;
      }

      void setY(float y) {
         this.y = y;
      }

      void setXY(float x, float y) {
         this.setX(x);
         this.setY(y);
      }

      EntityLivingBase getEntity() {
         return this.entity;
      }

      void updateRenderPos() {
         if (this.getEntity() != null) {
            float[] pos = ESP.this.get2DPosForTESP(
               this.entity, Module.mc.getRenderPartialTicks(), ScaledResolution.getScaleFactor(), Module.mc.getRenderManager()
            );
            int timeInterval = 1700;
            float pcTime = (float)((System.currentTimeMillis() + (long)this.entity.getEntityId()) % (long)timeInterval) / (float)timeInterval;
            pcTime = ((double)pcTime > 0.5 ? 1.0F - pcTime : pcTime) * 2.0F;
            pcTime *= pcTime;
            this.setXY(pos[0], pos[1] - 68.0F + pcTime * 20.0F);
         }
      }

      float getHeight() {
         return (float)this.storeyCount * this.triangleScale;
      }

      float getWidth() {
         return (float)this.storeyCount * this.triangleScale / 1.5F;
      }

      void updateTESP() {
         boolean doRemove = !ESP.this.updatedTargets.stream().anyMatch(entity -> entity.getEntityId() == this.getEntity().getEntityId());
         if (doRemove && this.alphaPC.to == 1.0F && this.alphaPC.getAnim() >= 0.9975F) {
            this.alphaPC.to = 0.0F;
         }

         this.toRemove = doRemove && this.alphaPC.to == 0.0F && this.alphaPC.getAnim() < 0.03F;
         if (!doRemove && this.alphaPC.to == 0.0F) {
            this.alphaPC.to = 1.0F;
            this.toRemove = false;
         }

         this.TElementLIST.forEach(ESP.TriangleElement::updateElement);
      }

      boolean isToRemove() {
         return this.toRemove;
      }

      boolean canDraw() {
         return (this.getX() != -1.0F || this.getY() != -1.0F) && this.alphaPC.getAnim() >= 0.03F;
      }

      void drawTESP(int baseColor, int elementColor1, int elementColor2) {
         this.updateRenderPos();
         if (this.canDraw()) {
            elementColor1 = ColorUtils.swapAlpha(elementColor1, (float)ColorUtils.getAlphaFromColor(elementColor1) * this.alphaPC.getAnim());
            elementColor2 = ColorUtils.swapAlpha(elementColor2, (float)ColorUtils.getAlphaFromColor(elementColor2) * this.alphaPC.anim);
            List vecs = Arrays.asList(
               new Vec2f(this.getX(), this.getY()),
               new Vec2f(this.getX() - this.getWidth(), this.getY() - this.getHeight()),
               new Vec2f(this.getX() + this.getWidth(), this.getY() - this.getHeight())
            );
            RenderUtils.enableStandardItemLighting();
            GlStateManager.disableLighting();
            GlStateManager.enableDepth();
            GlStateManager.enableBlend();
            GL11.glLineWidth(1.5F);
            RenderUtils.drawSome(vecs, elementColor1, 2);
            GL11.glLineWidth(1.0F);
            GlStateManager.disableDepth();
            this.drawElements(elementColor1, elementColor2, 1.0F);
            GlStateManager.enableDepth();
         }
      }

      void drawElements(int elementColor1, int elementColor2, float alphaPC) {
         GL11.glPushMatrix();
         GL11.glTranslated((double)this.getX(), (double)this.getY(), 0.0);
         int index = 0;

         for (ESP.TriangleElement element : this.TElementLIST) {
            int i = (int)((float)index * 12.0F);
            float rotPC = element.getRevertPC();
            rotPC = ((double)rotPC > 0.5 ? 1.0F - rotPC : rotPC) * 2.0F;
            rotPC *= rotPC;
            float colorFadeDelay = 500.0F;
            float timePC = (float)((int)(System.currentTimeMillis() + (long)((int)(((float)index + rotPC) * 40.0F))) % (int)colorFadeDelay) / colorFadeDelay;
            timePC = (double)timePC > 0.5 ? 1.0F - timePC : timePC;
            int elementColor = ColorUtils.getOverallColorFrom(elementColor1, elementColor2, MathUtils.clamp(rotPC + timePC, 0.0F, 1.0F));
            element.drawVertexes(elementColor);
            index++;
         }

         GL11.glPopMatrix();
      }
   }

   private class TriangleElement {
      float x;
      float y;
      float triangleScale;
      boolean upRot;
      TimerHelper timerRevert = new TimerHelper();
      int waitToRevert;
      AnimationUtils revertAnim = new AnimationUtils(0.0F, 0.0F, 0.015F);
      List<Vec2f> vecsOffsets = new ArrayList<>();

      TriangleElement(float x, float y, boolean upRot, float triangleScale) {
         this.x = x;
         this.y = y;
         this.upRot = upRot;
         this.triangleScale = triangleScale;
         this.genVertexes(triangleScale, upRot);
         this.timerRevert.reset();
      }

      void updateElement() {
         if (this.waitToRevert == 0) {
            this.waitToRevert = 100 + ESP.this.RANDOM.nextInt(700);
         }

         if (this.timerRevert.hasReached((double)this.waitToRevert) && this.revertAnim.getAnim() == 0.0F) {
            this.revertAnim.setAnim(0.0F);
            this.revertAnim.to = 180.0F;
            this.waitToRevert = 1600 + ESP.this.RANDOM.nextInt(550);
            this.timerRevert.reset();
         }

         if (MathUtils.getDifferenceOf(this.revertAnim.to, this.revertAnim.getAnim()) < 2.0) {
            this.revertAnim.setAnim(0.0F);
            this.revertAnim.to = 0.0F;
         }
      }

      float getRevertPC() {
         return (float)(MathUtils.getDifferenceOf(this.revertAnim.getAnim(), this.revertAnim.to) / 180.0);
      }

      void genVertexes(float triangleScale, boolean upRot) {
         if (upRot) {
            this.vecsOffsets.add(new Vec2f(triangleScale / 1.5F, 0.0F));
            this.vecsOffsets.add(new Vec2f(-triangleScale / 1.5F, 0.0F));
            this.vecsOffsets.add(new Vec2f(0.0F, -triangleScale));
         } else {
            this.vecsOffsets.add(new Vec2f(triangleScale / 1.5F, -triangleScale));
            this.vecsOffsets.add(new Vec2f(-triangleScale / 1.5F, -triangleScale));
            this.vecsOffsets.add(new Vec2f(0.0F, 0.0F));
         }
      }

      void drawVertexes(int color) {
         float rotPC = this.revertAnim.getAnim() / 180.0F;
         rotPC = ((double)rotPC > 0.5 ? 1.0F - rotPC : rotPC) * 2.0F;
         GL11.glPushMatrix();
         GL11.glTranslated((double)this.x, (double)this.y, 0.0);
         GL11.glTranslated(0.0, (double)(-this.triangleScale / 2.0F), 0.0);
         GL11.glRotated((double)this.revertAnim.getAnim(), 0.0, 1.0, 0.0);
         GL11.glTranslated(0.0, (double)(this.triangleScale / 2.0F), 0.0);
         GL11.glEnable(2848);
         GL11.glHint(3154, 4354);
         RenderUtils.drawSome(this.vecsOffsets, ColorUtils.swapAlpha(color, (float)ColorUtils.getAlphaFromColor(color) * (1.0F - rotPC)), 9);
         GL11.glLineWidth(rotPC * 2.0F + 1.0F);
         RenderUtils.drawSome(
            this.vecsOffsets, ColorUtils.swapAlpha(color, (float)ColorUtils.getAlphaFromColor(color) * ColorUtils.getGLAlphaFromColor(color)), 2
         );
         GL11.glLineWidth(1.0F);
         GL11.glHint(3154, 4352);
         GL11.glDisable(2848);
         GL11.glPopMatrix();
      }
   }

   private class Vec2fQuadColored {
      public static final Vec2fColored ZERO = new Vec2fColored(0.0F, 0.0F, -1);
      public float x;
      public float y;
      public int color = -1;
      public int color2 = -1;
      public int color3 = -1;
      public int color4 = -1;

      public Vec2fQuadColored(float xIn, float yIn, int color, int color2, int color3, int color4) {
         this.x = xIn;
         this.y = yIn;
         this.color = color;
         this.color2 = color2;
         this.color3 = color3;
         this.color4 = color4;
      }

      public void setXY(float x, float y) {
         this.x = x;
         this.y = y;
      }

      public float[] getXY() {
         return new float[]{this.x, this.y};
      }

      public float getX() {
         return this.x;
      }

      public float getY() {
         return this.y;
      }

      public int getColor() {
         return this.color;
      }

      public int getColor2() {
         return this.color2;
      }

      public int getColor3() {
         return this.color3;
      }

      public int getColor4() {
         return this.color4;
      }
   }
}
