package ru.govno.client.module.modules;

import com.google.common.collect.Lists;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.Chunk;
import org.lwjgl.opengl.GL11;
import ru.govno.client.Client;
import ru.govno.client.module.Module;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.module.settings.ModeSettings;
import ru.govno.client.utils.Math.MathUtils;
import ru.govno.client.utils.Render.ColorUtils;

public class ChinaHat extends Module {
    private final ResourceLocation HAT_BAMBOO_TEXTURE = new ResourceLocation("vegaline/modules/chinahat/hat01.png");
    private final ResourceLocation HAT_CARPET_TEXTURE = new ResourceLocation("vegaline/modules/chinahat/hat02.png");
    private final ResourceLocation HAT_METAL_TEXTURE = new ResourceLocation("vegaline/modules/chinahat/hat03.png");
    private final ResourceLocation HAT_TURBINE_TEXTURE = new ResourceLocation("vegaline/modules/chinahat/hat04.png");
    private final ResourceLocation HAT_ILLUSION_TEXTURE = new ResourceLocation("vegaline/modules/chinahat/hat05.png");
    private final ResourceLocation HAT_MAGMA_TEXTURE = new ResourceLocation("vegaline/modules/chinahat/hat06.png");
    private final ResourceLocation HAT_DIRTY_TEXTURE = new ResourceLocation("vegaline/modules/chinahat/hat07.png");
    private final ResourceLocation HAT_FOIL_TEXTURE = new ResourceLocation("vegaline/modules/chinahat/hat08.png");
    private final ModeSettings MATERIALS;
    private final BoolSettings HIGH_POLY;
    private final BoolSettings ANTIALIASING;
    private final BoolSettings Players;
    private final BoolSettings Friends;
    private final BoolSettings Self;
    private final BoolSettings ShowOnFirstPerson;
    private ArrayList<EntityPlayer> filteredPlayersUpdated;
    private final Tessellator tessellator = Tessellator.getInstance();
    private final BufferBuilder buffer = this.tessellator.getBuffer();
    private Vec3d cameraPosUpdated = Vec3d.ZERO;

    public ChinaHat() {
        super("ChinaHat", 0, Module.Category.RENDER);
        this.settings
                .add(
                        this.MATERIALS = new ModeSettings(
                                "Material", "Metal", this, new String[]{"Bamboo", "Carpet", "Metal", "Turbine", "Illusion", "Magma", "Dirty", "Foil"}
                        )
                );
        this.settings.add(this.HIGH_POLY = new BoolSettings("HighPolygonal", true, this));
        this.settings.add(this.ANTIALIASING = new BoolSettings("Antialiasing", false, this));
        this.settings.add(this.Players = new BoolSettings("OnPlayers", false, this));
        this.settings.add(this.Friends = new BoolSettings("OnFriends", true, this));
        this.settings.add(this.Self = new BoolSettings("OnSelf", true, this));
        this.settings.add(this.ShowOnFirstPerson = new BoolSettings("ShowOnFirstPerson", false, this, () -> this.Self.getBool()));
    }

    @Override
    public boolean isBetaModule() {
        return true;
    }

    @Override
    public void onUpdate() {
        if (!this.Players.getBool() && !this.Self.getBool() && !this.Friends.getBool()) {
            this.filteredPlayersUpdated.clear();
            Client.msg("§f§lModules:§r §7[§l" + this.getName() + "§r§7]: §7включите кого-нибудь в настройках.", false);
            this.toggle(false);
        } else {
            this.filteredPlayersUpdated = this.getFilteredPlayers();
        }
    }

    private ArrayList<EntityPlayer> getFilteredPlayers() {
        return Lists.newArrayList(
                mc.world
                        .getLoadedEntityList()
                        .stream()
                        .map(Entity::getPlayerOf)
                        .filter(Objects::nonNull)
                        .filter(player -> !player.ignoreFrustumCheck || player instanceof EntityPlayerSP)
                        .filter(player -> !player.isInvisible())
                        .filter(
                                player -> player.isEntityAlive()
                                        && (
                                        player instanceof EntityPlayerSP && this.Self.getBool()
                                                || player instanceof EntityOtherPlayerMP mp
                                                && (Client.friendManager.isFriend(mp.getName()) ? this.Friends.getBool() : this.Players.getBool())
                                )
                        )
                        .toList()
        );
    }

    private int[] getDensityShapeTXTY(double distance) {
        return this.HIGH_POLY.getBool() && distance < 24.0 ? new int[]{60, 12} : new int[]{30, 7};
    }

    private Vec3d cameraPos() {
        if (mc.world != null) {
            EntityPlayerSP player = Minecraft.player;
            if (player != null) {
                float partialTicks = mc.getRenderPartialTicks();
                float f = player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * partialTicks;
                float f1 = player.prevRotationYaw + (player.rotationYaw - player.prevRotationYaw) * partialTicks;
                double d0 = player.prevPosX + (player.posX - player.prevPosX) * (double)partialTicks;
                double d1 = player.prevPosY + (player.posY - player.prevPosY) * (double)partialTicks + (double)player.getEyeHeight();
                double d2 = player.prevPosZ + (player.posZ - player.prevPosZ) * (double)partialTicks;
                f += WorldRender.get.offPitchOrient;
                f1 += WorldRender.get.offYawOrient;
                if (Minecraft.getMinecraft().gameSettings.thirdPersonView != 0) {
                    int sideMul = Minecraft.getMinecraft().gameSettings.thirdPersonView == 1 ? 1 : -1;
                    double camDist = WorldRender.get.cameraRedistance(4.0) * (double)sideMul;
                    d0 += Math.sin(Math.toRadians((double)f1)) * camDist;
                    d1 += Math.sin(Math.toRadians((double)f)) * camDist;
                    d2 += -Math.cos(Math.toRadians((double)f1)) * camDist;
                }

                return new Vec3d(d0, d1, d2).add(WorldRender.get.getLastTranslated());
            }
        }

        return new Vec3d(RenderManager.renderPosX, RenderManager.renderPosY, RenderManager.renderPosZ).add(WorldRender.get.getLastTranslated());
    }

    private float[] getMapLightPC(double x, double y, double z) {
        BlockPos pos = new BlockPos(x, y, z);
        Chunk chunk = mc.world.getChunkFromBlockCoords(pos);
        float[] vals = new float[4];
        float sunLight = mc.world.getSunBrightness(1.0F);
        vals[1] = (float)(mc.world.getCombinedLight(pos, (int)(sunLight * 15.0F)) & 65535) / 255.0F;
        vals[0] = sunLight * (0.8F + vals[1] / 5.0F);
        vals[2] = chunk.getBiome(pos, mc.world.provider.getBiomeProvider()).getFloatTemperature(pos);
        return vals;
    }

    private int getColorAsMapLight(int previousColor, float[] lights) {
        float bright = lights[0];
        float light = lights[1];
        float temp = lights[2];
        float brightMulUp = bright + light * (1.0F - bright);
        int colorTemerature = ColorUtils.getOverallColorFrom(ColorUtils.getColor(167, 180, 255), ColorUtils.getColor(255, 191, 117), temp / 2.0F);
        colorTemerature = Color.getHSBColor(
                        (float)ColorUtils.getHueFromColor(colorTemerature), ColorUtils.getSaturateFromColor(colorTemerature), bright * light * 0.3333F
                )
                .getRGB();
        float r = ColorUtils.getGLRedFromColor(previousColor);
        r += ColorUtils.getGLRedFromColor(colorTemerature);
        r *= brightMulUp;
        r = Math.min(r, 1.0F);
        float g = ColorUtils.getGLGreenFromColor(previousColor);
        g += ColorUtils.getGLGreenFromColor(colorTemerature);
        g *= brightMulUp;
        g = Math.min(g, 1.0F);
        float b = ColorUtils.getGLBlueFromColor(previousColor);
        b += ColorUtils.getGLBlueFromColor(colorTemerature);
        b *= brightMulUp;
        b = Math.min(b, 1.0F);
        return ColorUtils.getColor((int)(r * 255.0F), (int)(g * 255.0F), (int)(b * 255.0F), ColorUtils.getAlphaFromColor(previousColor));
    }

    private void renderHatBegins(
            Tessellator tessellator, BufferBuilder buffer, float hatHeight, float hatWidth, int[] countVecsXY, float alphaPC, float[] lights
    ) {
        float prevYOffset = 0.0F;
        float prevXzOffset = 0.0F;
        float prevSlicePC = 0.0F;
        int c = this.getColorAsMapLight(ColorUtils.swapAlpha(-1, 255.0F * alphaPC), lights);

        for (int slice = 0; slice < countVecsXY[1]; slice++) {
            float slicePC = (float)slice / (float)countVecsXY[1];
            float xzOffset = slicePC * hatWidth;
            float yOffset = (1.0F - slicePC) * hatHeight;
            yOffset -= yOffset / 2.0F * (float)MathUtils.easeInOutExpo((double)MathUtils.valWave01(slicePC));
            if (slice == 1) {
                buffer.begin(6, DefaultVertexFormats.POSITION_TEX_COLOR);
                buffer.pos(0.0, (double)prevYOffset, 0.0).tex(0.5, 0.5).color(c).endVertex();

                for (float radian = 0.0F;
                     (double)radian <= Math.toRadians(360.0);
                     radian = (float)((double)radian + Math.toRadians((double)(360.0F / (float)countVecsXY[0])))
                ) {
                    float sin = (float)Math.sin((double)radian);
                    float cos = (float)(-Math.cos((double)radian));
                    buffer.pos((double)(sin * xzOffset), (double)yOffset, (double)(cos * xzOffset))
                            .tex((double)(0.5F + sin * slicePC / 2.0F), (double)(0.5F + cos * slicePC / 2.0F))
                            .color(c)
                            .endVertex();
                }

                tessellator.draw();
            } else {
                buffer.begin(8, DefaultVertexFormats.POSITION_TEX_COLOR);

                for (float radian = 0.0F;
                     (double)radian <= Math.toRadians(360.0);
                     radian = (float)((double)radian + Math.toRadians((double)(360.0F / (float)countVecsXY[0])))
                ) {
                    float sin = (float)Math.sin((double)radian);
                    float cos = (float)(-Math.cos((double)radian));
                    buffer.pos((double)(sin * prevXzOffset), (double)prevYOffset, (double)(cos * prevXzOffset))
                            .tex((double)(0.5F + sin * prevSlicePC / 2.0F), (double)(0.5F + cos * prevSlicePC / 2.0F))
                            .color(c)
                            .endVertex();
                    buffer.pos((double)(sin * xzOffset), (double)yOffset, (double)(cos * xzOffset))
                            .tex((double)(0.5F + sin * slicePC / 2.0F), (double)(0.5F + cos * slicePC / 2.0F))
                            .color(c)
                            .endVertex();
                }

                tessellator.draw();
            }

            if (slice == countVecsXY[1] - 1) {
                buffer.begin(2, DefaultVertexFormats.POSITION_TEX_COLOR);

                for (float var25 = 0.0F;
                     (double)var25 <= Math.toRadians(360.0);
                     var25 = (float)((double)var25 + Math.toRadians((double)(360.0F / (float)countVecsXY[0])))
                ) {
                    float sin = (float)Math.sin((double)var25);
                    float cos = (float)(-Math.cos((double)var25));
                    buffer.pos((double)(sin * xzOffset), (double)yOffset, (double)(cos * xzOffset))
                            .tex((double)(0.5F + sin * slicePC / 2.0F), (double)(0.5F + cos * slicePC / 2.0F))
                            .color(c)
                            .endVertex();
                }

                tessellator.draw();
            }

            prevXzOffset = xzOffset;
            prevYOffset = yOffset;
            prevSlicePC = slicePC;
        }
    }

    private void renderPlayerHat(EntityPlayer player, float partianTicks, ResourceLocation texture, boolean antialiasing) {
        double x;
        boolean playerIsChild;
        float yaw;
        float pitch;
        boolean var10000;
        label75: {
            x = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double)partianTicks;
            playerIsChild = player.isChild();
            yaw = player.prevRotationYawHead + MathUtils.wrapAngleTo180_float(player.rotationYawHead - player.prevRotationYawHead) * partianTicks;
            pitch = player.prevRotationPitchHead + (player.rotationPitchHead - player.prevRotationPitchHead) * partianTicks;
            if (player instanceof EntityPlayerSP sp && mc.gameSettings.thirdPersonView == 0) {
                var10000 = true;
                break label75;
            }

            var10000 = false;
        }

        boolean fistPerson = var10000;
        if (fistPerson || player instanceof EntityOtherPlayerMP) {
            yaw = player.prevRotationYaw + (player.rotationYaw - player.prevRotationYaw) * partianTicks;
            pitch = player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * partianTicks;
        }

        float pitchSin = (float)Math.sin(Math.toRadians((double)pitch)) * (pitch > 0.0F ? 1.0F : -1.0F);
        float hatPitchExt = playerIsChild ? 0.07F : 0.235F;
        float yCenterHeadOffset = fistPerson
                ? player.getEyeHeight() - 0.1F
                : player.height / (playerIsChild ? (player.isSneaking() ? 2.7F : 2.275F) : (player.isSneaking() ? 1.31F : 1.1F)) - pitchSin * hatPitchExt;
        float yUpperHeadOffset = (playerIsChild ? 0.258F : 0.24F) + pitchSin * hatPitchExt;
        if (player.hurtTime != 0 && !fistPerson) {
            float hurtPC = Math.max((float)player.hurtTime - partianTicks, 0.0F) / 10.0F;
            pitch = (float)((double)pitch - MathUtils.easeInOutElastic((double)hurtPC) * 3.0);
            yCenterHeadOffset = (float)(
                    (double)yCenterHeadOffset + MathUtils.easeInOutQuadWave((double)MathUtils.valWave01(hurtPC)) * (double)hurtPC * (double)yUpperHeadOffset / 5.0
            );
        }

        float hatHeight = playerIsChild ? 0.15F : 0.25F;
        float hatWidth = (playerIsChild ? 0.7F : 1.0F) / 2.0F;
        double y = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double)partianTicks + (double)yCenterHeadOffset;
        double z = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double)partianTicks;
        GL11.glPushMatrix();
        GL11.glTranslated(x - mc.renderManager.getRenderPosX(), y - mc.renderManager.getRenderPosY(), z - mc.renderManager.getRenderPosZ());
        GL11.glRotatef(yaw + 90.0F, 0.0F, -1.0F, 0.0F);
        GL11.glRotatef(pitch, 0.0F, 0.0F, -1.0F);
        GL11.glTranslated(0.0, (double)yUpperHeadOffset, 0.0);
        GL11.glEnable(3042);
        GL11.glEnable(3553);
        GL11.glDisable(2884);
        GL11.glAlphaFunc(516, 0.003921569F);
        GL11.glShadeModel(7425);
        mc.getTextureManager().bindTexture(texture);
        if (antialiasing) {
            GL11.glTexParameteri(3553, 10240, 9729);
        }

        double dst = this.cameraPosUpdated.getDistanceAtEyeByVec(Minecraft.player, x, y + (double)yUpperHeadOffset, z);
        float lineWidth = 0.025F + 9.5F * (float)MathUtils.clamp(1.0 - dst / 7.0, 0.0, 1.0);
        GL11.glLineWidth(lineWidth);
        this.renderHatBegins(
                this.tessellator, this.buffer, hatHeight, hatWidth, this.getDensityShapeTXTY(dst), 1.0F, this.getMapLightPC(x, y + (double)yUpperHeadOffset, z)
        );
        GL11.glLineWidth(1.0F);
        if (antialiasing) {
            GL11.glTexParameteri(3553, 10240, 9728);
        }

        GL11.glAlphaFunc(516, 0.1F);
        GL11.glShadeModel(7424);
        GL11.glEnable(2884);
        GL11.glPopMatrix();
    }

    @Override
    public void alwaysRender3D(float partialTicks) {
        if (this.isActived() && mc.world != null && this.filteredPlayersUpdated != null) {
            ArrayList<EntityPlayer> entitiesToDraw = Lists.newArrayList(
                    this.filteredPlayersUpdated.stream().filter(player -> mc.world.getEntityByID(player.getEntityId()) != null).toList()
            );
            if (!entitiesToDraw.isEmpty()) {
                this.cameraPosUpdated = this.cameraPos();
                ResourceLocation loc = null;
                String var4 = this.MATERIALS.getMode();
                switch (var4) {
                    case "Bamboo":
                        loc = this.HAT_BAMBOO_TEXTURE;
                        break;
                    case "Carpet":
                        loc = this.HAT_CARPET_TEXTURE;
                        break;
                    case "Metal":
                        loc = this.HAT_METAL_TEXTURE;
                        break;
                    case "Turbine":
                        loc = this.HAT_TURBINE_TEXTURE;
                        break;
                    case "Illusion":
                        loc = this.HAT_ILLUSION_TEXTURE;
                        break;
                    case "Magma":
                        loc = this.HAT_MAGMA_TEXTURE;
                        break;
                    case "Dirty":
                        loc = this.HAT_DIRTY_TEXTURE;
                        break;
                    case "Foil":
                        loc = this.HAT_FOIL_TEXTURE;
                }

                for (EntityPlayer player : entitiesToDraw) {
                    if (player != null
                            && player.isEntityAlive()
                            && (!player.hasNewVersionMoves || !player.isLay && !player.isNewSneak)
                            && !player.isElytraFlying()
                            && !(this.cameraPosUpdated.distanceTo(player.getPositionVector()) > 92.0)
                            && (!(player instanceof EntityPlayerSP) || mc.gameSettings.thirdPersonView != 0 || this.ShowOnFirstPerson.getBool())) {
                        this.renderPlayerHat(player, partialTicks, loc, this.ANTIALIASING.getBool());
                    }
                }
            }
        }
    }
}
