package ru.govno.client.module.modules;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;
import ru.govno.client.Client;
import ru.govno.client.module.Module;
import ru.govno.client.module.modules.FreeCam;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.module.settings.ColorSettings;
import ru.govno.client.module.settings.FloatSettings;
import ru.govno.client.utils.Math.MathUtils;
import ru.govno.client.utils.Render.ColorUtils;
import ru.govno.client.utils.Render.RenderUtils;

public class BackTrack
extends Module {
    public static BackTrack get;
    private final FloatSettings TargetRange;
    private final FloatSettings MinSpeedThreshold;
    private final FloatSettings MaxSpeedThreshold;
    private final FloatSettings MinDistanceThreshold;
    private final FloatSettings TrackTicksMax;
    private final BoolSettings RenderTracks;
    private final BoolSettings RuleNoElytra;
    private final BoolSettings RuleNoLiquid;
    private final BoolSettings RuleOnlyPlayers;
    private final ColorSettings BoxColor;
    private EntityPlayer self;
    final List<Integer> trackableEntitiesId = new ArrayList<Integer>();
    final HashMap<Integer, PreviousTicksEntityTracker> tracks = new HashMap();

    public BackTrack() {
        super("BackTrack", 0, Module.Category.COMBAT);
        this.TargetRange = new FloatSettings("TargetRange", 6.0f, 12.0f, 3.0f, this);
        this.settings.add(this.TargetRange);
        this.MinSpeedThreshold = new FloatSettings("MinSpeedThreshold", 0.08f, 0.3f, 0.01f, this);
        this.settings.add(this.MinSpeedThreshold);
        this.MaxSpeedThreshold = new FloatSettings("MaxSpeedThreshold", 1.0f, 3.0f, 0.5f, this);
        this.settings.add(this.MaxSpeedThreshold);
        this.MinDistanceThreshold = new FloatSettings("MinDistanceThreshold", 2.7f, 5.0f, 0.0f, this);
        this.settings.add(this.MinDistanceThreshold);
        this.TrackTicksMax = new FloatSettings("TrackTicksMax", 4.0f, 10.0f, 1.0f, this);
        this.settings.add(this.TrackTicksMax);
        this.RenderTracks = new BoolSettings("RenderTracks", true, this);
        this.settings.add(this.RenderTracks);
        this.RuleNoElytra = new BoolSettings("RuleNoElytra", true, this);
        this.settings.add(this.RuleNoElytra);
        this.RuleNoLiquid = new BoolSettings("RuleNoLiquid", false, this);
        this.settings.add(this.RuleNoLiquid);
        this.RuleOnlyPlayers = new BoolSettings("RuleOnlyPlayers", false, this);
        this.settings.add(this.RuleOnlyPlayers);
        this.BoxColor = new ColorSettings("BoxColor", ColorUtils.getColor(31, 133, 255), this, () -> this.RenderTracks.getBool());
        this.settings.add(this.BoxColor);
        get = this;
    }

    @Override
    public void onUpdate() {
        EntityPlayer entityPlayer = this.self = FreeCam.get.isActived() && FreeCam.fakePlayer != null ? FreeCam.fakePlayer : Minecraft.player;
        if (this.self == null) {
            return;
        }
        this.trackableEntitiesId.clear();
        if (BackTrack.mc.world != null) {
            float range = this.TargetRange.getFloat();
            float minDistance = this.MinDistanceThreshold.getFloat();
            this.trackableEntitiesId.addAll(BackTrack.mc.world.getLoadedEntityList().stream().map(Entity::getLivingBaseOf).filter(Objects::nonNull).filter(base -> !(this.RuleOnlyPlayers.getBool() && !(base instanceof EntityOtherPlayerMP) || base == Minecraft.player || base == this.self || base.ticksExisted <= 1 || !base.isEntityAlive() || !(base.getDistanceToEntity(this.self) <= range) || !(base.getDistanceToEntity(this.self) >= minDistance) || this.tracks.get(base.getEntityId()) == null && !this.hasEntityMove((Entity)base) || this.RuleNoElytra.getBool() && base.isElytraFlying() || this.RuleNoLiquid.getBool() && (base.isInWater() || base.isInLava() || base.isInWeb || BackTrack.mc.world.getBlockState(new BlockPos(base.posX, base.posY - (double)0.12f, base.posZ)).getMaterial().isLiquid()) || Client.friendManager.isFriend(base.getName()))).map(Entity::getEntityId).toList());
        }
        for (int entityId : this.trackableEntitiesId) {
            if (this.tracks.containsKey(entityId)) continue;
            this.tracks.put(entityId, new PreviousTicksEntityTracker(this.TrackTicksMax.getInt(), entityId));
        }
        ArrayList<Integer> rems = new ArrayList<Integer>();
        if (!this.tracks.isEmpty()) {
            for (Integer entityId : this.tracks.keySet()) {
                PreviousTicksEntityTracker tracker = this.tracks.get(entityId);
                if (tracker.removeIf()) {
                    rems.add(entityId);
                    continue;
                }
                tracker.setMemoryTicks(this.TrackTicksMax.getInt());
                tracker.updatePrevs();
            }
        }
        Iterator iterator = rems.iterator();
        while (iterator.hasNext()) {
            int rem = (Integer)iterator.next();
            this.tracks.remove(rem);
        }
    }

    @Override
    public void onToggled(boolean actived) {
        this.stateAnim.to = actived ? 1.0f : 0.0f;
        this.tracks.clear();
        super.onToggled(actived);
    }

    @Override
    public void alwaysRender3D(float partialTicks) {
        if (!this.tracks.isEmpty() && this.stateAnim.getAnim() > 0.003921569f && this.RenderTracks.canBeRender()) {
            for (Integer entityId : this.trackableEntitiesId) {
                PreviousTicksEntityTracker tracker = this.tracks.get(entityId);
                if (tracker == null || tracker.getAxises().isEmpty()) continue;
                float aPC = this.stateAnim.anim * this.RenderTracks.getAnimation();
                RenderUtils.setup3dForBlockPos(() -> {
                    GL11.glEnable((int)2929);
                    GL11.glDepthMask((boolean)false);
                    tracker.getAxises().forEach(axis -> {
                        int color = ColorUtils.swapAlpha(this.BoxColor.getCol(), (float)ColorUtils.getAlphaFromColor(this.BoxColor.getCol()) * aPC * axis.getAlphaPC(partialTicks) / 2.0f);
                        if (ColorUtils.getAlphaFromColor(color) >= 1) {
                            AxisAlignedBB aabb = axis.getAabb().expandXyz(-0.02);
                            RenderUtils.drawGradientAlphaBox(aabb, false, true, 0, ColorUtils.swapAlpha(color, (float)ColorUtils.getAlphaFromColor(color) / 12.25f));
                            GL11.glLineWidth((float)0.025f);
                            RenderUtils.drawCanisterBox(aabb, true, false, false, ColorUtils.swapAlpha(color, (float)ColorUtils.getAlphaFromColor(color) / 2.0f), 0, 0);
                            GL11.glLineStipple((int)1, (short)Short.reverseBytes((short)16));
                            GL11.glEnable((int)2852);
                            GL11.glEnable((int)2848);
                            GL11.glHint((int)3154, (int)4354);
                            GL11.glLineWidth((float)40.0f);
                            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA_SATURATE, GlStateManager.DestFactor.ONE_MINUS_CONSTANT_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                            RenderUtils.drawCanisterBox(aabb, true, false, false, color, 0, 0);
                            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA_SATURATE, GlStateManager.DestFactor.ONE_MINUS_CONSTANT_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                            GL11.glDisable((int)2852);
                            GL11.glDisable((int)2848);
                            GL11.glHint((int)3154, (int)4352);
                            GL11.glLineWidth((float)1.0f);
                        }
                    });
                    GL11.glDepthMask((boolean)true);
                }, true);
            }
        }
    }

    public List<AxisAlignedBB> getTracksAsEntity(Entity entity, AxisAlignedBB defaultAxis, boolean sorted) {
        if (entity == null) {
            return null;
        }
        ArrayList<AxisAlignedBB> list = new ArrayList<AxisAlignedBB>();
        list.add(defaultAxis);
        PreviousTicksEntityTracker tracker = this.tracks.get(entity.getEntityId());
        if (tracker != null) {
            list.addAll(tracker.getAxisesToBoxes());
        }
        if (sorted) {
            list.sort(Comparator.comparing(obj -> this.getVecAsAxis((AxisAlignedBB)obj).distanceTo(this.self.getPositionVector())));
        }
        return list;
    }

    private boolean hasEntityMove(Entity entity) {
        double dz;
        double dy;
        double dx = Math.abs(entity.posX - entity.lastTickPosX);
        double sqrt = Math.sqrt(dx * dx + (dy = Math.abs(entity.posY - entity.lastTickPosY)) * dy + (dz = Math.abs(entity.posZ - entity.lastTickPosZ)) * dz);
        return sqrt >= (double)this.MinSpeedThreshold.getFloat() && sqrt <= (double)this.MaxSpeedThreshold.getFloat();
    }

    private Vec3d getVecAsAxis(AxisAlignedBB axis) {
        return new Vec3d(axis.minX + (axis.maxX - axis.minX) / 2.0, axis.minY, axis.minZ + (axis.maxZ - axis.minZ) / 2.0);
    }

    private class PreviousTicksEntityTracker {
        private int memoryTicks;
        private final int entityId;
        private List<TickedAxis> axisList = new ArrayList<TickedAxis>();

        public PreviousTicksEntityTracker(int memoryTicks, int entityId) {
            this.memoryTicks = memoryTicks;
            this.entityId = entityId;
        }

        public void setMemoryTicks(int memoryTicks) {
            this.memoryTicks = memoryTicks;
        }

        private EntityLivingBase getEntity() {
            Entity entity = Module.mc.world.getEntityByID(this.entityId);
            if (entity instanceof EntityLivingBase) {
                EntityLivingBase base = (EntityLivingBase)entity;
                return base;
            }
            return null;
        }

        public void updatePrevs() {
            EntityLivingBase trackableEntity = this.getEntity();
            if (trackableEntity != null && BackTrack.this.hasEntityMove(trackableEntity)) {
                if (this.axisList == null) {
                    this.axisList = new ArrayList<TickedAxis>();
                }
                this.axisList.add(new TickedAxis(trackableEntity.getEntityBoundingBox(), this.memoryTicks));
            }
            this.axisList.removeIf(TickedAxis::removeIf);
            this.axisList.forEach(TickedAxis::update);
        }

        public List<TickedAxis> getAxises() {
            return this.axisList;
        }

        public List<AxisAlignedBB> getAxisesToBoxes() {
            return this.axisList.stream().map(TickedAxis::getAabb).toList();
        }

        public boolean removeIf() {
            EntityLivingBase entity = this.getEntity();
            return entity == null || !((Entity)entity).isEntityAlive() || this.axisList.size() < 2 && !BackTrack.this.hasEntityMove(entity) || Client.friendManager.isFriend(entity.getName());
        }
    }

    private class TickedAxis {
        private final AxisAlignedBB aabb;
        private int ticks;
        private final int ticksMax;

        public TickedAxis(AxisAlignedBB aabb, int ticksAlive) {
            this.aabb = aabb;
            this.ticksMax = this.ticks = ticksAlive;
        }

        public void update() {
            --this.ticks;
        }

        public float getAlphaPC(float partialTicks) {
            return (float)MathUtils.easeOutCubic(MathUtils.valWave01(Math.max(((float)this.ticks + 1.0f - partialTicks) / (float)this.ticksMax, 0.0f)));
        }

        public boolean removeIf() {
            return this.ticks <= 0;
        }

        public AxisAlignedBB getAabb() {
            return this.aabb;
        }
    }
}

