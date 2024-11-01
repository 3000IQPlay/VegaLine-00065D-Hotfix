package ru.govno.client.soundengine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.init.Biomes;
import net.minecraft.util.Vec2f;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import org.lwjgl.opengl.GL11;
import ru.govno.client.utils.Math.MathUtils;
import ru.govno.client.utils.Render.ColorUtils;
import ru.govno.client.utils.Render.RenderUtils;

public class SoundSurroundTool {
    private final Minecraft mc;
    private EntityPlayerSP player;
    private boolean tooPerfomance;
    private boolean rtxDebug = true;
    private Vec3d headPosition;
    private Vec3d lastHeadPosition;
    private final Random RAND = new Random();
    private final List<Vec2f> listOfChangesRays = new ArrayList();
    private final List<SoundSurroundTool.RTVec> listOfTestVecs = new ArrayList();

    public EntityPlayerSP getPlayer() {
        return this.player;
    }

    public void setPlayer(EntityPlayerSP player) {
        this.player = player;
    }

    public boolean isTooPerfomance() {
        return this.tooPerfomance;
    }

    public boolean isRtxDebug() {
        return this.rtxDebug;
    }

    public void setTooPerfomance(boolean tooPerfomance) {
        this.tooPerfomance = tooPerfomance;
    }

    public void setRtxDebug(boolean rtxDebug) {
        this.rtxDebug = rtxDebug;
    }

    public Vec3d getHeadPosition() {
        return this.headPosition;
    }

    public Vec3d getLastHeadPosition() {
        return this.lastHeadPosition;
    }

    public void setHeadPosition(Vec3d headPosition) {
        this.headPosition = headPosition;
    }

    public void setLastHeadPosition(Vec3d lastHeadPosition) {
        this.lastHeadPosition = lastHeadPosition;
    }

    protected SoundSurroundTool() {
        this.mc = Minecraft.getMinecraft();
    }

    public static SoundSurroundTool build() {
        return new SoundSurroundTool();
    }

    public float[] getGainArgsFromWorld() {
        return this.getGainArgsFromWorld(this.getPlayer());
    }

    public float[] getGainArgsFromWorld(EntityPlayerSP player) {
        if (player != null) {
            boolean isPerfomance = this.isTooPerfomance();
            double xPos = player.posX;
            double yPos = player.posY;
            double eyeHeight = (double)player.getEyeHeight();
            double zPos = player.posZ;
            this.setHeadPosition(new Vec3d(xPos, yPos + eyeHeight, zPos));
            this.setLastHeadPosition(new Vec3d(player.prevPosX, player.prevPosY + eyeHeight, player.prevPosZ));
            int floorX = (int)xPos;
            int floorY = (int)yPos;
            int floorZ = (int)zPos;
            int worldMaxY = this.getWorldYLevel(floorX, floorZ);
            Vec2f rayResults = this.overallRandomRaySignValues(xPos, yPos + eyeHeight, zPos, isPerfomance);
            double liquidDepthPC = this.getLiquidDepth(xPos, yPos + eyeHeight, zPos, isPerfomance);
            double skyFactor = (double)(rayResults.y / 2.6666F);
            double isolationAndSize = (double)rayResults.x;
            double atmospheric = this.getAtmosphericPressure(xPos, yPos + eyeHeight, zPos, worldMaxY, isPerfomance) * (1.0 - skyFactor) * isolationAndSize;
            boolean isOpaquePlayer = this.hasOpaquePushing(player, xPos, yPos, zPos, isPerfomance);
            float echoPC = 0.97F;
            float revPC = 1.62F;
            float echo = (float)(isolationAndSize - isolationAndSize * skyFactor) * echoPC;
            echo = echo > 1.0F ? 1.0F : Math.max(echo, 0.0F);
            float rev = echo / (float)(0.01F + Math.min(skyFactor / (0.01F + isolationAndSize), 1.0));
            float push = 1.0F - (float)Math.min(liquidDepthPC + atmospheric * (double)(1.0F - echo), 1.0) / 1.108F;
            rev = rev > 1.0F ? 1.0F : Math.max(rev, 0.0F);
            push = Math.max(push, 0.0F);
            echo *= 0.35F + 0.65F * push * push;
            if (isOpaquePlayer) {
                rev *= 0.014245F;
                push -= isPerfomance ? 0.35F : 0.95F;
                push = Math.max(push, 0.0F);
            }

            float vol = (float)(1.0 - atmospheric - liquidDepthPC / 2.4F) * (isPerfomance ? push : 1.0F);
            vol = Math.max(vol, 0.25F);
            return new float[]{echo * echoPC, rev * revPC, vol, push};
        } else {
            return new float[]{0.0F, 0.0F, 1.0F, 1.0F};
        }
    }

    private int getWorldYLevel(int x, int z) {
        if (this.mc.world == null) {
            return 0;
        } else {
            Chunk chunk = this.mc.world.getChunkFromChunkCoords(x >> 4, z >> 4);
            return chunk.isLoaded() ? chunk.getHeightValue(x & 15, z & 15) : 0;
        }
    }

    private double getLiquidDepth(double x, double y, double z, boolean perfomance) {
        return this.getLiquidDepth(x, y, z, perfomance ? 9 : 18, perfomance ? 3 : 1, perfomance);
    }

    private double getLiquidDepth(double x, double y, double z, int maxDepth, int blockStep, boolean perfomance) {
        if (this.mc.world == null) {
            return 0.0;
        } else {
            double value = 0.0;
            if (perfomance) {
                BlockPos pos = new BlockPos(x, y, z);
                IBlockState state = this.mc.world.getBlockState(pos);
                if (state.getMaterial().isLiquid()) {
                    double factorY = 1.0 + y - (double)((int)y);
                    int y1 = (int)y;

                    while ((double)y1 < y + (double)maxDepth) {
                        state = this.mc.world.getBlockState(pos = pos.up());
                        if (state.getMaterial().isLiquid()) {
                            factorY++;
                        }

                        y1 += blockStep;
                    }

                    return factorY / (double)((float)maxDepth);
                }
            } else {
                AxisAlignedBB aabb = new AxisAlignedBB(x - 0.3, y, z - 0.3, x + 0.3, y + 1.0E-14, z + 0.3);
                if (this.mc.world.isMaterialInBB(aabb, Material.WATER)) {
                    double factorY = 1.0 + y - (double)((int)y);
                    int y1 = (int)y;

                    while ((double)y1 < y + (double)maxDepth) {
                        aabb = aabb.addCoord(0.0, 1.0, 0.0);
                        if (this.mc.world.isMaterialInBB(aabb, Material.WATER) || this.mc.world.isMaterialInBB(aabb, Material.LAVA)) {
                            factorY++;
                        } else if (this.mc.world.isMaterialInBB(aabb, Material.WEB)) {
                            factorY += 0.15;
                        }

                        y1 += blockStep;
                    }

                    value = factorY / (double)((float)maxDepth);
                }

                double editValue = 1.0 - Math.pow(1.0 - Math.min(value, 1.0), 3.0);
                if (!Double.isNaN(editValue)) {
                    value = editValue;
                }
            }

            return value;
        }
    }

    private double getAtmosphericPressure(double x, double y, double z, int heightMapMaxY, boolean perfomance) {
        if (this.mc.world == null) {
            return 0.0;
        } else {
            double factor = 0.0;
            if (!perfomance) {
                if (this.mc.world.isRaining()) {
                    factor += (double)(this.mc.world.getRainStrength(this.mc.getRenderPartialTicks()) * 0.2F);
                }

                Biome biome = this.mc.world.getBiome(new BlockPos(x, y, z));
                if (biome == Biomes.OCEAN || biome == Biomes.FROZEN_OCEAN || biome == Biomes.FROZEN_RIVER || biome == Biomes.ICE_PLAINS) {
                    factor += 0.2F;
                } else if (biome == Biomes.HELL) {
                    factor += 0.3;
                }

                factor += 0.17F * MathUtils.clamp(((double)MathUtils.clamp(heightMapMaxY, 56, 72) - y) / 48.0, 0.0, 1.0);
            } else if (y < 48.0) {
                factor += y / 30.0 * 0.15F;
            }

            return Math.min(factor, 1.0);
        }
    }

    public List<SoundSurroundTool.RTVec> getListOfTestVecs() {
        return this.listOfTestVecs;
    }

    private Vec2f overallRandomRaySignValues(double x, double y, double z, boolean perfomance) {
        return this.overallRandomRaySignValues(x, y, z, perfomance ? 50 : 120, 50, perfomance ? 350L : 500L, perfomance);
    }

    private Vec2f overallRandomRaySignValues(double x, double y, double z, int raysCount, int maxRayLength, long changeTimeWithAnoise, boolean perfomance) {
        if (this.mc.world == null) {
            return new Vec2f(0.0F, 0.0F);
        } else {
            Vec3d pos = new Vec3d(x, y, z);
            int misses = 0;
            double value = 0.0;
            float a = 0.0F;
            float b = 0.0F;

            for (int rayIndex = 0; rayIndex < raysCount; rayIndex++) {
                Vec3d randRay = new Vec3d(
                        -1.0 + this.RAND.nextDouble(2.0), (double)Math.min(-0.5F + this.RAND.nextFloat(1.5F), 1.0F), (double)(-1.0F + this.RAND.nextFloat(2.0F))
                )
                        .scale((double)maxRayLength);
                RayTraceResult ray = this.mc.world.rayTraceBlocks(pos, pos.add(randRay), !perfomance, true, true);
                if (ray == null || ray.typeOfHit == Type.MISS) {
                    misses++;
                } else if (ray.typeOfHit == Type.BLOCK) {
                    value += ray.hitVec.distanceTo(pos) * 2.6666667F * 2.0;
                    if (this.isRtxDebug()) {
                        this.listOfTestVecs
                                .add(new SoundSurroundTool.RTVec(ray.hitVec, (float)Math.min(ray.hitVec.distanceTo(pos) / (double)((float)maxRayLength), 1.0)));
                    }
                }
            }

            Vec2f result = new Vec2f((float)value / (float)raysCount, (float)misses);
            this.listOfChangesRays.add(result);

            while ((float)this.listOfChangesRays.size() > (float)changeTimeWithAnoise / 50.0F) {
                this.listOfChangesRays.remove(0);
            }

            while (this.isRtxDebug() && (float)this.listOfTestVecs.size() > (float)changeTimeWithAnoise / 50.0F * (float)maxRayLength) {
                this.listOfTestVecs.remove(0);
            }

            for (Vec2f vec : this.listOfChangesRays) {
                a += vec.x;
                b += vec.y;
            }

            if (a != 0.0F || b != 0.0F) {
                a /= (float)this.listOfChangesRays.size();
                b /= (float)this.listOfChangesRays.size();
            }

            return new Vec2f(a / (float)raysCount, b / (float)raysCount);
        }
    }

    private boolean hasOpaquePushing(EntityPlayerSP player, double x, double y, double z, boolean perfomance) {
        if (this.mc.world == null) {
            return false;
        } else if (perfomance) {
            return this.mc.world.getBlockState(new BlockPos(x, y, z)).getMaterial().blocksMovement();
        } else {
            double eyeHeight = (double)player.getEyeHeight();
            double expandY = 0.075;
            double expandXZ = 0.165;
            return !this.mc
                    .world
                    .getCollisionBoxes(player, new AxisAlignedBB(x - 0.165, y + eyeHeight - 0.075, z - 0.165, x + 0.165, y + eyeHeight + 0.075, z + 0.165))
                    .isEmpty();
        }
    }

    private boolean hasLight(int x, int y, int z) {
        return this.mc.world == null ? false : this.mc.world.getBlockState(new BlockPos(x, y, z)).getLightValue() != 0;
    }

    public void draw3dTest() {
        if (this.isRtxDebug() && (this.getHeadPosition() != null || this.getLastHeadPosition() != null)) {
            float pTicks = this.mc.getRenderPartialTicks();
            Vec3d headRenderPos = new Vec3d(
                    MathUtils.lerp(this.getLastHeadPosition().xCoord, this.getHeadPosition().xCoord, (double)pTicks),
                    MathUtils.lerp(this.getLastHeadPosition().yCoord, this.getHeadPosition().yCoord, (double)pTicks),
                    MathUtils.lerp(this.getLastHeadPosition().zCoord, this.getHeadPosition().zCoord, (double)pTicks)
            );
            double glX = RenderManager.viewerPosX;
            double glY = RenderManager.viewerPosY;
            double glZ = RenderManager.viewerPosZ;
            GL11.glPushMatrix();
            GlStateManager.tryBlendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE, SourceFactor.ONE, DestFactor.ZERO);
            this.mc.entityRenderer.disableLightmap();
            GL11.glEnable(3042);
            GL11.glDisable(3553);
            GL11.glDisable(2929);
            GL11.glDisable(2896);
            GL11.glShadeModel(7425);
            GL11.glTranslated(-glX, -glY, -glZ);
            GlStateManager.resetColor();
            GL11.glDisable(3008);
            GL11.glDisable(2896);
            GL11.glLineWidth(0.25F);
            GL11.glEnable(2848);
            GL11.glHint(3154, 4354);
            GL11.glBegin(1);
            this.listOfTestVecs.forEach(vec -> {
                int c = ColorUtils.getOverallColorFrom(ColorUtils.getColor(255, 70, 70), ColorUtils.getColor(70, 255, 70), vec.getDstPC());
                RenderUtils.setupColor(ColorUtils.toDark(c, 0.8F), 3.0F);
                GL11.glVertex3d(headRenderPos.xCoord, headRenderPos.yCoord, headRenderPos.zCoord);
                RenderUtils.setupColor(c, 26.0F + 80.0F * vec.getDstPC());
                GL11.glVertex3d(vec.getPos().xCoord, vec.getPos().yCoord, vec.getPos().zCoord);
            });
            GL11.glEnd();
            if (!this.isTooPerfomance()) {
                List<SoundSurroundTool.TripleRTVec3d> tripleRTVec3ds = this.getSortLimitedToTriangles(this.listOfTestVecs);
                if (!tripleRTVec3ds.isEmpty()) {
                    tripleRTVec3ds.forEach(tripleRTVec -> {
                        GL11.glBegin(3);
                        tripleRTVec.getRTVecs().forEach(rtVec -> {
                            RenderUtils.setupColor(ColorUtils.getColor(30, 30, 255), 30.0F + 40.0F * rtVec.getDstPC());
                            GL11.glVertex3d(rtVec.getPos().xCoord, rtVec.getPos().yCoord, rtVec.getPos().zCoord);
                        });
                        GL11.glEnd();
                    });
                }
            }

            GL11.glLineWidth(1.0F);
            GL11.glHint(3154, 4352);
            GL11.glDisable(2848);
            GL11.glEnable(2832);
            GL11.glPointSize(0.25F);
            GL11.glBegin(0);
            RenderUtils.setupColor(-1, 255.0F);
            GL11.glVertex3d(headRenderPos.xCoord, headRenderPos.yCoord, headRenderPos.zCoord);
            this.listOfTestVecs.forEach(vec -> {
                int c = ColorUtils.getOverallColorFrom(ColorUtils.getColor(255, 70, 70), ColorUtils.getColor(70, 255, 70), vec.getDstPC());
                RenderUtils.setupColor(c, 255.0F);
                GL11.glVertex3d(vec.getPos().xCoord, vec.getPos().yCoord, vec.getPos().zCoord);
            });
            GL11.glEnd();
            GL11.glPointSize(27.0F);
            GL11.glBegin(0);
            RenderUtils.setupColor(ColorUtils.getColor(60, 60, 255), 20.0F);
            GL11.glVertex3d(headRenderPos.xCoord, headRenderPos.yCoord, headRenderPos.zCoord);
            this.listOfTestVecs.forEach(vec -> {
                int c = ColorUtils.getOverallColorFrom(ColorUtils.getColor(255, 70, 70), ColorUtils.getColor(70, 255, 70), vec.getDstPC());
                RenderUtils.setupColor(c, 2.0F);
                GL11.glVertex3d(vec.getPos().xCoord, vec.getPos().yCoord, vec.getPos().zCoord);
            });
            GL11.glEnd();
            GL11.glPointSize(1.0F);
            GlStateManager.resetColor();
            GL11.glEnable(3008);
            GL11.glTranslated(glX, glY, glZ);
            GL11.glShadeModel(7424);
            GL11.glEnable(3553);
            GL11.glEnable(2929);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
            GlStateManager.resetColor();
            GL11.glPopMatrix();
        }
    }

    private List<SoundSurroundTool.TripleRTVec3d> getSortLimitedToTriangles(List<SoundSurroundTool.RTVec> rayVecs) {
        List<SoundSurroundTool.TripleRTVec3d> tempRTGroups = new ArrayList();
        rayVecs = rayVecs.stream().sorted(Comparator.comparingDouble(SoundSurroundTool.RTVec::getSortValue)).toList();
        int indexOfRt = 0;

        for (SoundSurroundTool.RTVec rtVec : rayVecs) {
            if (indexOfRt % 3 == 2) {
                tempRTGroups.add(
                        new SoundSurroundTool.TripleRTVec3d(
                                rtVec, (SoundSurroundTool.RTVec)rayVecs.get(indexOfRt - 1), (SoundSurroundTool.RTVec)rayVecs.get(indexOfRt - 2)
                        )
                );
            }

            indexOfRt++;
        }

        return tempRTGroups.stream().sorted(Comparator.comparingDouble(SoundSurroundTool.TripleRTVec3d::getSortValue)).toList();
    }

    private class RTVec {
        private final Vec3d pos;
        private final float dstPC;

        public Vec3d getPos() {
            return this.pos;
        }

        public float getDstPC() {
            return this.dstPC;
        }

        public RTVec(Vec3d pos, float dstPC) {
            this.pos = pos;
            this.dstPC = dstPC;
        }

        public double getSortValue() {
            return (double)(-this.getDstPC());
        }
    }

    private class TripleRTVec3d {
        private final SoundSurroundTool.RTVec rt0;
        private final SoundSurroundTool.RTVec rt1;
        private final SoundSurroundTool.RTVec rt2;

        public TripleRTVec3d(SoundSurroundTool.RTVec rt0, SoundSurroundTool.RTVec rt1, SoundSurroundTool.RTVec rt2) {
            this.rt0 = rt0;
            this.rt1 = rt1;
            this.rt2 = rt2;
        }

        public SoundSurroundTool.RTVec getRt0() {
            return this.rt0;
        }

        public SoundSurroundTool.RTVec getRt1() {
            return this.rt1;
        }

        public SoundSurroundTool.RTVec getRt2() {
            return this.rt2;
        }

        public double getSortValue() {
            return (double)(-(this.rt0.getDstPC() + this.rt1.getDstPC() + this.rt2.getDstPC()));
        }

        public List<SoundSurroundTool.RTVec> getRTVecs() {
            return Arrays.stream(new SoundSurroundTool.RTVec[]{this.getRt0(), this.getRt1(), this.getRt2()}).toList();
        }
    }
}
