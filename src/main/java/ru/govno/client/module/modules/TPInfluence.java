package ru.govno.client.module.modules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemShield;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketConfirmTeleport;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;
import ru.govno.client.event.EventTarget;
import ru.govno.client.event.events.Event3D;
import ru.govno.client.event.events.EventReceivePacket;
import ru.govno.client.event.events.EventSendPacket;
import ru.govno.client.module.Module;
import ru.govno.client.module.modules.ElytraBoost;
import ru.govno.client.module.modules.Fly;
import ru.govno.client.module.modules.FreeCam;
import ru.govno.client.module.modules.HitAura;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.module.settings.FloatSettings;
import ru.govno.client.module.settings.ModeSettings;
import ru.govno.client.utils.Math.BlockUtils;
import ru.govno.client.utils.Math.MathUtils;
import ru.govno.client.utils.Math.TimerHelper;
import ru.govno.client.utils.Movement.MoveMeHelp;
import ru.govno.client.utils.Render.ColorUtils;
import ru.govno.client.utils.Render.RenderUtils;

public class TPInfluence
extends Module {
    public static TPInfluence get;
    public BoolSettings UseOnHitAura;
    public BoolSettings UseOnCrystalField;
    public BoolSettings ShowTpPos;
    public BoolSettings CollisionsAviod;
    public BoolSettings ThroughShieldHits;
    public BoolSettings AntiAttraction;
    public ModeSettings HitAuraRule;
    public ModeSettings TeleportAction;
    public FloatSettings SelfTicksAlive;
    public FloatSettings MaxRange;
    private Vec3d lastHandledVec = new Vec3d(0.0, 0.0, 0.0);
    private final List<TimedVec> TIMED_VECS_LIST = new ArrayList<TimedVec>();
    private final List<TimedVec> waitedFlagTPPoses = new ArrayList<TimedVec>();
    private Vec3d lastSelfOldPos;
    private final TimerHelper backTpMinDelayTimer = TimerHelper.TimerHelperReseted();

    public TPInfluence() {
        super("TPInfluence", 0, Module.Category.COMBAT);
        get = this;
        this.UseOnHitAura = new BoolSettings("UseOnHitAura", false, this);
        this.settings.add(this.UseOnHitAura);
        this.ThroughShieldHits = new BoolSettings("ThroughShieldHits", true, this, () -> this.UseOnHitAura.getBool());
        this.settings.add(this.ThroughShieldHits);
        this.HitAuraRule = new ModeSettings("HitAuraRule", "Always", this, new String[]{"Always", "HurtSync", "Fly", "Fly&SelfTicks", "ElytraFlying", "ElyBoost", "ElyBoost&Stand"}, () -> this.UseOnHitAura.getBool());
        this.settings.add(this.HitAuraRule);
        this.SelfTicksAlive = new FloatSettings("SelfTicksAlive", 400.0f, 1000.0f, 100.0f, this, () -> this.UseOnHitAura.getBool() && this.HitAuraRule.getMode().equalsIgnoreCase("Fly&SelfTicks"));
        this.settings.add(this.SelfTicksAlive);
        this.UseOnCrystalField = new BoolSettings("UseOnCrystalField", false, this);
        this.settings.add(this.UseOnCrystalField);
        this.TeleportAction = new ModeSettings("TeleportAction", "StepVH", this, new String[]{"StepVH", "StepV", "StepH", "StepHG", "VanillaVH", "VanillaH"}, () -> this.UseOnHitAura.getBool());
        this.settings.add(this.TeleportAction);
        this.MaxRange = new FloatSettings("MaxRange", 60.0f, 200.0f, 10.0f, this, () -> this.UseOnHitAura.getBool() && this.TeleportAction.getMode().contains("Step"));
        this.settings.add(this.MaxRange);
        this.ShowTpPos = new BoolSettings("ShowTpPos", true, this, () -> this.UseOnHitAura.getBool());
        this.settings.add(this.ShowTpPos);
        this.CollisionsAviod = new BoolSettings("CollisionsAviod", true, this, () -> this.UseOnHitAura.getBool());
        this.settings.add(this.CollisionsAviod);
        this.AntiAttraction = new BoolSettings("AntiAttraction", true, this, () -> this.UseOnHitAura.getBool() || this.UseOnCrystalField.getBool());
        this.settings.add(this.AntiAttraction);
    }

    private double sqrtAt(double val1) {
        return Math.sqrt(val1 * val1);
    }

    private double sqrtAt(double val1, double val2) {
        return Math.sqrt(val1 * val1 + val2 * val2);
    }

    private double sqrtAt(double val1, double val2, double val3) {
        return Math.sqrt(val1 * val1 + val2 * val2 + val3 * val3);
    }

    private double positive(double val) {
        return val < 0.0 ? -val : val;
    }

    public boolean defaultRule() {
        return (!FreeCam.get.isActived() || FreeCam.fakePlayer == null) && Minecraft.player != null && this.isActived();
    }

    public boolean entityRule(EntityLivingBase targetIn) {
        if (targetIn == null || !targetIn.isEntityAlive()) {
            return false;
        }
        boolean selfCollided = Minecraft.player.boundingBox == null || TPInfluence.mc.world.getCollisionBoxes(Minecraft.player, Minecraft.player.boundingBox).isEmpty();
        boolean targetCollided = targetIn.boundingBox == null || TPInfluence.mc.world.getCollisionBoxes(targetIn, targetIn.boundingBox).isEmpty();
        return selfCollided || !targetCollided;
    }

    private void send(double x, double y, double z, boolean ground) {
        mc.getConnection().sendPacket(new CPacketPlayer.Position(x, y, z, ground));
    }

    private void send(double x, double y, double z, float yaw, float pitch, boolean ground) {
        mc.getConnection().sendPacket(new CPacketPlayer.PositionRotation(x, y, z, yaw, pitch, ground));
    }

    private void send(double x, double y, double z) {
        this.send(x, y, z, false);
    }

    private void send(double x, double y, double z, float yaw, float pitch) {
        this.send(x, y, z, yaw, pitch, false);
    }

    private void send(boolean ground) {
        mc.getConnection().sendPacket(new CPacketPlayer(ground));
    }

    private void send() {
        mc.getConnection().sendPacket(new CPacketPlayer());
    }

    private Vec3d axisEntityPoint(Entity entityOf) {
        AxisAlignedBB bb = entityOf.getRenderBoundingBox();
        return bb != null ? new Vec3d(bb.minX + (bb.maxX - bb.minX) / 2.0, bb.minY, bb.minZ + (bb.maxZ - bb.minZ) / 2.0) : entityOf.getPositionVector();
    }

    public void teleportActionOfActionType(boolean pre, Vec3d to, String actionType, Entity copyRotateEnt) {
        if (copyRotateEnt == null) {
            this.teleportActionOfActionType(pre, to, actionType, false);
        } else {
            CPacketPlayer.lastSendedYaw = copyRotateEnt.rotationYaw;
            CPacketPlayer.lastSendedPitch = copyRotateEnt.rotationPitch;
            this.teleportActionOfActionType(pre, to, actionType, true);
        }
    }

    public void teleportActionOfActionType(boolean pre, Vec3d to, String actionType, boolean addRotEnd) {
        Vec3d self = Minecraft.player.getPositionVector();
        double dx = this.positive(self.xCoord - to.xCoord);
        double dy = this.positive(self.yCoord - to.yCoord);
        double dz = this.positive(self.zCoord - to.zCoord);
        int grInt = Minecraft.player.onGround ? 1 : 0;
        float distanceDensity = 1.0f;
        if (pre) {
            switch (actionType) {
                case "StepVH": {
                    double diffs = this.sqrtAt(dx, dy, dz);
                    for (int packetCount = (int)(diffs / (9.64 * (double)distanceDensity)) + 1; packetCount > 0; --packetCount) {
                        this.send(false);
                    }
                    if (grInt == 1) {
                        this.send(to.xCoord, to.yCoord + 0.08, to.zCoord);
                    }
                    this.send(to.xCoord, to.yCoord + 0.01, to.zCoord);
                    if (addRotEnd) {
                        this.send(to.xCoord, to.yCoord + 0.01, to.zCoord, CPacketPlayer.lastSendedYaw, CPacketPlayer.lastSendedPitch);
                    }
                    this.lastHandledVec = new Vec3d(to.xCoord, to.yCoord + 0.01, to.zCoord);
                    break;
                }
                case "StepV": {
                    double diffs = this.positive(dy);
                    int packetCount = 1 + (int)(diffs / (9.73 * (double)distanceDensity));
                    if (grInt == 1) {
                        this.send(to.xCoord, self.yCoord + 0.08, to.zCoord);
                    }
                    while (packetCount > 0) {
                        this.send(false);
                        --packetCount;
                    }
                    if (addRotEnd) {
                        this.send(to.xCoord, to.yCoord + 0.01, to.zCoord, CPacketPlayer.lastSendedYaw, CPacketPlayer.lastSendedPitch);
                    } else {
                        this.send(to.xCoord, to.yCoord + 0.01, to.zCoord);
                    }
                    this.lastHandledVec = new Vec3d(self.xCoord, to.yCoord + 0.01, self.zCoord);
                    break;
                }
                case "StepH": {
                    double diffs = this.sqrtAt(dx, dz);
                    for (int packetCount = (int)(diffs / (8.953 * (double)distanceDensity)) + grInt; packetCount > 0; --packetCount) {
                        this.send(false);
                    }
                    if (addRotEnd) {
                        this.send(to.xCoord, to.yCoord, to.zCoord, CPacketPlayer.lastSendedYaw, CPacketPlayer.lastSendedPitch);
                    } else {
                        this.send(to.xCoord, to.yCoord, to.zCoord);
                    }
                    this.lastHandledVec = new Vec3d(to.xCoord, self.yCoord, to.zCoord);
                    break;
                }
                case "StepHG": {
                    int packetCount;
                    double diffs = this.sqrtAt(dx, dz);
                    for (packetCount = (int)(diffs / (8.317 * (double)distanceDensity)) + grInt; packetCount > 0; --packetCount) {
                        this.send(false);
                    }
                    if (addRotEnd) {
                        this.send(to.xCoord, self.yCoord - (double)grInt * 1.0E-4 * (double)packetCount, to.zCoord, CPacketPlayer.lastSendedYaw, CPacketPlayer.lastSendedPitch);
                    } else {
                        this.send(to.xCoord, self.yCoord - (double)grInt * 1.0E-4 * (double)packetCount, to.zCoord);
                    }
                    this.lastHandledVec = new Vec3d(to.xCoord, self.yCoord - (double)grInt * 1.0E-4 * (double)packetCount, to.zCoord);
                    if (grInt != 0) break;
                    Minecraft.player.setPosY(self.yCoord);
                    break;
                }
                case "VanillaVH": {
                    this.send(false);
                    if (addRotEnd) {
                        this.send(to.xCoord, to.yCoord, to.zCoord, CPacketPlayer.lastSendedYaw, CPacketPlayer.lastSendedPitch);
                    } else {
                        this.send(to.xCoord, to.yCoord, to.zCoord);
                    }
                    this.lastHandledVec = new Vec3d(to.xCoord, to.yCoord, to.zCoord);
                }
                case "VanillaH": {
                    this.send(false);
                    if (addRotEnd) {
                        this.send(to.xCoord, to.yCoord, to.zCoord, CPacketPlayer.lastSendedYaw, CPacketPlayer.lastSendedPitch);
                    } else {
                        this.send(to.xCoord, to.yCoord, to.zCoord);
                    }
                    this.lastHandledVec = new Vec3d(to.xCoord, to.yCoord, to.zCoord);
                }
            }
            return;
        }
        switch (actionType) {
            case "StepVH": 
            case "StepH": {
                this.send(self.xCoord, self.yCoord, self.zCoord);
                break;
            }
            case "StepV": {
                this.send(self.xCoord, self.yCoord, self.zCoord);
                this.send(self.xCoord, self.yCoord + (grInt == 1 ? 0.1 : -1.0E-13), self.zCoord);
                break;
            }
            case "StepHG": {
                this.send(self.xCoord, self.yCoord - this.positive(grInt - 1) * 1.0E-4 * 2.0, self.zCoord);
                break;
            }
            case "VanillaVH": {
                this.send(self.xCoord, self.yCoord + 0.0016, self.zCoord);
                break;
            }
            case "VanillaH": {
                this.send(self.xCoord, self.yCoord, self.zCoord);
            }
        }
    }

    public boolean vectorRule(Vec3d to, double defaultDistanceMax, double distanceMin) {
        String action = this.TeleportAction.getMode();
        Vec3d self = Minecraft.player.getPositionVector();
        double range = action.contains("Step") ? (double)this.MaxRange.getFloat() : (action.contains("Vanilla") ? 9.23 : defaultDistanceMax);
        double dx = self.xCoord - to.xCoord;
        double dy = self.yCoord - to.yCoord;
        double dz = self.zCoord - to.zCoord;
        boolean isInRange = false;
        if (this.sqrtAt(dx, dy, dz) < distanceMin) {
            return false;
        }
        switch (this.TeleportAction.getMode()) {
            case "StepVH": 
            case "VanillaVH": {
                isInRange = this.sqrtAt(dx, dy, dz) < range;
                break;
            }
            case "StepV": {
                isInRange = this.sqrtAt(dx, dz) < defaultDistanceMax / 1.33333 && this.positive(dy) < range;
                break;
            }
            case "StepH": 
            case "StepHG": {
                isInRange = this.positive(dy) < defaultDistanceMax && this.sqrtAt(dx, dz) + this.positive(dy) < range;
                break;
            }
            case "VanillaH": {
                isInRange = this.positive(dy) < defaultDistanceMax - 1.0 && this.sqrtAt(dx, dz) < range;
            }
        }
        return isInRange;
    }

    private List<BlockPos> getBlockPosesAsAABB(AxisAlignedBB aabb) {
        ArrayList<BlockPos> poses = new ArrayList<BlockPos>();
        double x1 = aabb.minX;
        double y1 = aabb.minY;
        double z1 = aabb.maxZ;
        double x2 = aabb.maxX;
        double y2 = aabb.maxY;
        double z2 = aabb.maxZ;
        for (BlockPos corner : Arrays.asList(new BlockPos(x1, y1, z1), new BlockPos(x2, y1, z1), new BlockPos(x2, y1, z2), new BlockPos(x1, y1, z2), new BlockPos(x1, y2, z1), new BlockPos(x2, y2, z1), new BlockPos(x2, y2, z2), new BlockPos(x1, y2, z2))) {
            if (poses.stream().anyMatch(pos -> BlockUtils.wasEqualsBlockPos(pos, corner))) continue;
            poses.add(corner);
        }
        return poses;
    }

    private boolean anyCollisionMaterial(List<BlockPos> positions) {
        return positions.isEmpty() && positions.stream().map(pos -> TPInfluence.mc.world.getBlockState((BlockPos)pos).getMaterial()).filter(Objects::nonNull).anyMatch(Material::blocksMovement);
    }

    private boolean anyCollisionMaterial(AxisAlignedBB aabb) {
        if (aabb == null) {
            return false;
        }
        return this.anyCollisionMaterial(this.getBlockPosesAsAABB(aabb));
    }

    public Vec3d targetWhitePos(EntityLivingBase target, double distanceMin, boolean doAccuracy) {
        AxisAlignedBB targetAABB;
        if (distanceMin < 0.0) {
            distanceMin = 0.0;
        }
        Vec3d vec = (targetAABB = target.getRenderBoundingBox()) != null ? new Vec3d(targetAABB.minX + (targetAABB.maxX - targetAABB.minX) / 2.0, targetAABB.minY, targetAABB.minZ + (targetAABB.maxZ - targetAABB.minZ) / 2.0) : target.getPositionVector();
        double targetX = vec.xCoord;
        double selfY = Minecraft.player.posY;
        double targetY = vec.yCoord;
        double yDst = this.positive(selfY - targetY);
        double selfW = (double)Minecraft.player.width / 2.0;
        double selfH = Minecraft.player.height;
        double targetZ = vec.zCoord;
        if (TPInfluence.mc.world == null || Minecraft.player == null) {
            return vec;
        }
        AxisAlignedBB aabb = new AxisAlignedBB(targetX - selfW, targetY, targetZ - selfW, targetX + selfW, targetY + selfH, targetZ + selfW);
        if (!doAccuracy || !this.CollisionsAviod.getBool() || TPInfluence.mc.world.getCollisionBoxes(null, aabb).isEmpty()) {
            return vec;
        }
        int range = (int)distanceMin;
        ArrayList<Vec3d> toCheck = new ArrayList<Vec3d>();
        float coordStep = 0.5f;
        float minX = (float)(targetX - (double)range);
        float maxX = (float)(targetX + (double)range);
        float minY = (float)(targetY - (double)range);
        float maxY = (float)(targetY + (double)range);
        float minZ = (float)(targetZ - (double)range);
        float maxZ = (float)(targetZ + (double)range);
        for (float xTemp = minX; xTemp < maxX; xTemp += 0.5f) {
            for (float yTemp = minY; yTemp < maxY; yTemp += 0.5f) {
                for (float zTemp = minZ; zTemp < maxZ; zTemp += 0.5f) {
                    Vec3d tempVec = new Vec3d(xTemp, yTemp, zTemp);
                    if (tempVec.distanceTo(vec) > (double)range) continue;
                    toCheck.add(tempVec);
                }
            }
        }
        Vec3d finalVec = vec;
        toCheck.sort(Comparator.comparing(vec1 -> vec1.distanceTo(finalVec)));
        for (Vec3d check : toCheck) {
            check.xCoord = (double)((int)check.xCoord) + 0.5;
            check.zCoord = (double)((int)check.zCoord) + 0.5;
            aabb = new AxisAlignedBB(check.xCoord - selfW, check.yCoord, check.zCoord - selfW, check.xCoord + selfW, check.yCoord + selfH, check.zCoord + selfW);
            if (!TPInfluence.mc.world.getCollisionBoxes(null, aabb).isEmpty()) continue;
            return check;
        }
        return vec;
    }

    public boolean forHitAuraRule(EntityLivingBase target) {
        boolean sata;
        if (target == null || !target.isEntityAlive()) {
            return false;
        }
        boolean bl = sata = this.defaultRule() && this.entityRule(target) && this.UseOnHitAura.getBool();
        if (sata) {
            String rule;
            switch (rule = this.HitAuraRule.getMode()) {
                case "Always": {
                    sata = true;
                    break;
                }
                case "HurtSync": {
                    sata = target.hurtTime <= 1;
                    break;
                }
                case "Fly": {
                    sata = Fly.get.isActived();
                    break;
                }
                case "Fly&SelfTicks": {
                    sata = Fly.get.isActived() && (float)Minecraft.player.ticksExisted < this.SelfTicksAlive.getFloat();
                    break;
                }
                case "ElytraFlying": {
                    sata = Minecraft.player.getFlag(7);
                    break;
                }
                case "ElyBoost": {
                    sata = ElytraBoost.get.isActived() && ElytraBoost.canElytra();
                    break;
                }
                case "ElyBoost&Stand": {
                    sata = ElytraBoost.get.isActived() && ElytraBoost.canElytra() && MoveMeHelp.getSpeed() < 0.05 && !MoveMeHelp.moveKeysPressed() && this.positive(Minecraft.player.motionY) < 0.24;
                }
            }
        }
        double auraRangeMin = MathUtils.clamp((double)HitAura.get.getAuraRange(HitAura.TARGET_ROTS), 3.0, 5.2 - (double)target.height);
        double auraRangeMax = MathUtils.clamp((double)HitAura.get.getAuraRange(HitAura.TARGET_ROTS) - 0.1, 0.0, 5.2);
        return this.isActived() && sata && this.vectorRule(this.targetWhitePos(target, auraRangeMax, false), auraRangeMax, auraRangeMin);
    }

    public void hitAuraTPPre(EntityLivingBase target) {
        EntityPlayer player;
        double auraRangeMax = MathUtils.clamp((double)HitAura.get.getAuraRange(HitAura.TARGET_ROTS) - 0.1, 0.0, 5.2);
        Vec3d truePos = this.targetWhitePos(target, auraRangeMax, true);
        Vec3d samiFalsePos = this.targetWhitePos(target, auraRangeMax, false);
        this.teleportActionOfActionType(true, this.targetWhitePos(target, auraRangeMax, true), this.TeleportAction.getMode(), this.ThroughShieldHits.getBool() && target instanceof EntityPlayer && ((player = (EntityPlayer)target).getHeldItemOffhand().getItem() instanceof ItemShield || player.getHeldItemMainhand().getItem() instanceof ItemShield) ? target : null);
        if (this.ShowTpPos.getBool()) {
            this.addTimedVec(truePos, samiFalsePos);
        }
    }

    public void hitAuraTPPost(EntityLivingBase target) {
        double auraRangeMax = MathUtils.clamp((double)HitAura.get.getAuraRange(HitAura.TARGET_ROTS) - 0.1, 0.0, 5.2);
        this.teleportActionOfActionType(false, this.targetWhitePos(target, auraRangeMax, false), this.TeleportAction.getMode(), false);
    }

    public boolean forCrystalFieldRule() {
        return this.defaultRule() && this.UseOnCrystalField.getBool();
    }

    @Override
    public void onToggled(boolean enable) {
        if (!this.TIMED_VECS_LIST.isEmpty()) {
            this.TIMED_VECS_LIST.clear();
        }
        if (!this.waitedFlagTPPoses.isEmpty()) {
            this.waitedFlagTPPoses.clear();
        }
        super.onToggled(enable);
    }

    private void addTimedVec(Vec3d truePos, Vec3d samiFalsePos) {
        if (this.lastHandledVec == null) {
            return;
        }
        this.TIMED_VECS_LIST.add(new TimedVec(this.lastHandledVec, truePos == samiFalsePos ? null : samiFalsePos, HitAura.get.msCooldown() * 1.5f));
    }

    private int getTpPointColor() {
        return -1;
    }

    @EventTarget
    public void onRender3D(Event3D event) {
        if (!this.isActived()) {
            return;
        }
        if (this.TIMED_VECS_LIST.isEmpty()) {
            return;
        }
        this.TIMED_VECS_LIST.removeIf(TimedVec::isToRemove);
        RenderUtils.setup3dForBlockPos(() -> this.TIMED_VECS_LIST.forEach(timedVec -> {
            float aPC = timedVec.getAlphaPC();
            GL11.glLineWidth((float)0.1f);
            if (aPC * 255.0f >= 1.0f) {
                float range = 0.1f * (0.25f + 0.75f * (float)MathUtils.easeInOutQuad(1.0f - timedVec.getTimePC()));
                Vec3d vec = timedVec.getVec();
                Vec3d falseVec = timedVec.getVecFalse();
                AxisAlignedBB aabb = new AxisAlignedBB(vec.addVector(-range, -range, -range), vec.addVector(range, range, range));
                int color = this.getTpPointColor();
                if (aabb != null) {
                    int pCol = color;
                    color = ColorUtils.swapAlpha(color, (float)ColorUtils.getAlphaFromColor(color) * aPC * 0.5f);
                    RenderUtils.drawCanisterBox(aabb, true, false, true, color, 0, ColorUtils.swapAlpha(color, (float)ColorUtils.getAlphaFromColor(color) / 16.0f));
                    int iterations = 7;
                    for (int i = 0; i < iterations; ++i) {
                        float cPC = (float)i / (float)iterations;
                        aabb = aabb.expandXyz(0.015f * (1.0f - aPC));
                        int c = ColorUtils.swapAlpha(color, (float)ColorUtils.getAlphaFromColor(color) / 6.0f * (float)MathUtils.easeInOutQuadWave(cPC));
                        RenderUtils.drawCanisterBox(aabb, true, true, false, c, c, c);
                    }
                    color = ColorUtils.swapAlpha(pCol, 255.0f * aPC);
                }
                if (falseVec != null) {
                    double lineYTo = vec.yCoord < falseVec.yCoord ? vec.yCoord : falseVec.yCoord;
                    GL11.glLineStipple((int)3, (short)Short.reverseBytes((short)-24769));
                    GL11.glEnable((int)2852);
                    GL11.glEnable((int)2848);
                    GL11.glHint((int)3154, (int)4354);
                    GL11.glLineWidth((float)0.25f);
                    GL11.glBegin((int)3);
                    RenderUtils.glColor(0);
                    GL11.glVertex3d((double)vec.xCoord, (double)vec.yCoord, (double)vec.zCoord);
                    RenderUtils.glColor(color);
                    GL11.glVertex3d((double)vec.xCoord, (double)lineYTo, (double)vec.zCoord);
                    RenderUtils.glColor(0);
                    GL11.glVertex3d((double)falseVec.xCoord, (double)falseVec.yCoord, (double)falseVec.zCoord);
                    GL11.glEnd();
                    GL11.glDisable((int)2852);
                    GL11.glDisable((int)2848);
                    GL11.glHint((int)3154, (int)4352);
                    GL11.glLineWidth((float)1.0f);
                }
            }
            GL11.glLineWidth((float)1.0f);
        }), true);
    }

    @EventTarget
    public void onSend(EventSendPacket event) {
        Packet packet;
        if (this.isActived() && (packet = event.getPacket()) instanceof CPacketPlayer) {
            CPacketPlayer packet2 = (CPacketPlayer)packet;
            if (this.AntiAttraction.getBool()) {
                Vec3d pos = null;
                if (packet2 instanceof CPacketPlayer.Position) {
                    CPacketPlayer.Position posPacket = (CPacketPlayer.Position)packet2;
                    pos = new Vec3d(posPacket.getX(posPacket.x), posPacket.getY(posPacket.y), posPacket.getZ(posPacket.z));
                } else if (packet2 instanceof CPacketPlayer.PositionRotation) {
                    CPacketPlayer.PositionRotation posRotPacket = (CPacketPlayer.PositionRotation)packet2;
                    pos = new Vec3d(posRotPacket.getX(posRotPacket.x), posRotPacket.getY(posRotPacket.y), posRotPacket.getZ(posRotPacket.z));
                }
                if (pos != null && pos.distanceTo(this.lastHandledVec) < 0.1) {
                    this.waitedFlagTPPoses.add(new TimedVec(pos, Minecraft.player.getPositionVector(), 650.0f));
                }
            }
        }
    }

    @Override
    public void onUpdate() {
        double dz;
        double dy;
        double dx;
        double speed;
        double playerPosDiff;
        if (this.waitedFlagTPPoses.isEmpty()) {
            this.lastSelfOldPos = null;
            return;
        }
        this.waitedFlagTPPoses.removeIf(TimedVec::isToRemove);
        if (this.waitedFlagTPPoses.isEmpty()) {
            this.lastSelfOldPos = null;
            return;
        }
        if (this.lastSelfOldPos != null && (playerPosDiff = Minecraft.player.getPositionVector().distanceTo(this.lastSelfOldPos)) > (speed = Math.sqrt((dx = Minecraft.player.posX - Minecraft.player.lastTickPosX) * dx + (dy = Minecraft.player.posY - Minecraft.player.lastTickPosY) * dy + (dz = Minecraft.player.posZ - Minecraft.player.lastTickPosZ) * dz)) + 1.0) {
            this.reduceOldPosSelf();
        }
        this.lastSelfOldPos = Minecraft.player.getPositionVector();
    }

    private Vec3d[] getLastHandledBackPosVecs() {
        Vec3d[] vec3dArray;
        if (this.waitedFlagTPPoses.isEmpty() || this.waitedFlagTPPoses.get(this.waitedFlagTPPoses.size() - 1) == null) {
            vec3dArray = null;
        } else {
            Vec3d[] vec3dArray2 = new Vec3d[2];
            vec3dArray2[0] = this.waitedFlagTPPoses.get(this.waitedFlagTPPoses.size() - 1).getVec();
            vec3dArray = vec3dArray2;
            vec3dArray2[1] = this.waitedFlagTPPoses.get(this.waitedFlagTPPoses.size() - 1).getVecFalse();
        }
        return vec3dArray;
    }

    private void reduceOldPosSelf() {
        Vec3d backPosVec = this.lastSelfOldPos;
        if (backPosVec == null) {
            return;
        }
        int packets = (int)Math.min(backPosVec.distanceTo(Minecraft.player.getPositionVector()) / 9.953 + 2.0, 200.0);
        for (int num = 0; num < Math.max(packets - 1, 0); ++num) {
            this.send(false);
        }
        this.send(false);
        Minecraft.player.setPositionAndUpdate(backPosVec.xCoord, backPosVec.yCoord, backPosVec.zCoord);
    }

    @EventTarget
    public void onReceive(EventReceivePacket event) {
        Packet packet;
        if (this.isActived() && (packet = event.getPacket()) instanceof SPacketPlayerPosLook) {
            SPacketPlayerPosLook posLookPacket = (SPacketPlayerPosLook)packet;
            if (this.AntiAttraction.getBool()) {
                if (Minecraft.player == null) {
                    return;
                }
                this.reduceOldPosSelf();
                Vec3d pos = new Vec3d(posLookPacket.getX(), posLookPacket.getY(), posLookPacket.getZ());
                if (pos == null) {
                    return;
                }
                Vec3d[] backPosVecs = this.getLastHandledBackPosVecs();
                if (backPosVecs == null) {
                    return;
                }
                if (!this.backTpMinDelayTimer.hasReached(500.0)) {
                    return;
                }
                this.backTpMinDelayTimer.reset();
                double distance = pos.distanceTo(backPosVecs[0]);
                if (distance > 1.99999999) {
                    return;
                }
                distance = pos.distanceTo(backPosVecs[1]);
                int packets = (int)Math.min(distance / 9.953 + 1.0, 20.0);
                for (int num = 0; num < Math.max(packets - 1, 0); ++num) {
                    this.send(false);
                }
                this.send(false);
                Minecraft.player.setPositionAndUpdate(backPosVecs[1].xCoord, backPosVecs[1].yCoord, backPosVecs[1].zCoord);
                Minecraft.player.connection.sendPacket(new CPacketConfirmTeleport(posLookPacket.getTeleportId()));
                event.cancel();
            }
        }
    }

    private class TimedVec {
        private final long startTime = System.currentTimeMillis();
        private final float maxTime;
        private final Vec3d vec;
        private final Vec3d vecFalse;

        public TimedVec(Vec3d vec, Vec3d vecFalse, float maxTime) {
            this.vec = vec;
            this.vecFalse = vecFalse;
            this.maxTime = maxTime;
        }

        public float getTimePC() {
            return MathUtils.clamp((float)(System.currentTimeMillis() - this.startTime) / this.maxTime, 0.0f, 1.0f);
        }

        public float getAlphaPC() {
            float pc = 1.0f - this.getTimePC();
            return (float)MathUtils.easeOutCubic(MathUtils.easeInOutQuadWave(pc));
        }

        public Vec3d getVec() {
            return this.vec;
        }

        public Vec3d getVecFalse() {
            return this.vecFalse;
        }

        public boolean isToRemove() {
            return this.getVec() == null || this.getTimePC() == 1.0f;
        }
    }
}

