package ru.govno.client.utils;

import dev.intave.NewPhisicsFixes;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.vecmath.Vector2f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemAppleGold;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemElytra;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketCreativeInventoryAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.potion.Potion;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import ru.govno.client.Client;
import ru.govno.client.clickgui.ClickGuiScreen;
import ru.govno.client.event.EventTarget;
import ru.govno.client.event.events.EventMovementInput;
import ru.govno.client.event.events.EventPlayerMotionUpdate;
import ru.govno.client.event.events.EventRotationJump;
import ru.govno.client.event.events.EventRotationStrafe;
import ru.govno.client.module.Module;
import ru.govno.client.module.modules.AntiCrystal;
import ru.govno.client.module.modules.ClientColors;
import ru.govno.client.module.modules.Fly;
import ru.govno.client.module.modules.FreeCam;
import ru.govno.client.module.modules.HitAura;
import ru.govno.client.module.modules.OffHand;
import ru.govno.client.module.modules.TPInfluence;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.module.settings.FloatSettings;
import ru.govno.client.module.settings.ModeSettings;
import ru.govno.client.utils.Combat.RotationUtil;
import ru.govno.client.utils.Math.BlockUtils;
import ru.govno.client.utils.Math.MathUtils;
import ru.govno.client.utils.Math.TimerHelper;
import ru.govno.client.utils.Movement.MoveMeHelp;
import ru.govno.client.utils.Render.AnimationUtils;
import ru.govno.client.utils.Render.ColorUtils;
import ru.govno.client.utils.Render.RenderUtils;

public class CrystalField
extends Module {
    public static CrystalField get;
    public ModeSettings ProcessRateLevel;
    public ModeSettings CrystalHand;
    public ModeSettings ObsidianHand;
    public ModeSettings Swing;
    public ModeSettings SpamRule;
    public FloatSettings OnHealth;
    public FloatSettings MaxTargetsCount;
    public BoolSettings Rotations;
    public BoolSettings RotateMoveSide;
    public BoolSettings PlaceObsidian;
    public BoolSettings PlaceIgnoreWalls;
    public BoolSettings UseInventory;
    public BoolSettings NoSuicide;
    public BoolSettings SpamCrystal;
    public BoolSettings FirstPosSpammer;
    public BoolSettings CPSBypass;
    private final TimerHelper placeObsDelay = new TimerHelper();
    private final TimerHelper placeCrysDelay = new TimerHelper();
    private final TimerHelper attackDelay = new TimerHelper();
    private final TimerHelper tpaDelay = new TimerHelper();
    final List<EntityLivingBase> notCurrents = new CopyOnWriteArrayList<EntityLivingBase>();
    final List<BlockPos> positionsCrys = new ArrayList<BlockPos>();
    final List<BlockPos> positionsObs = new ArrayList<BlockPos>();
    public static BlockPos forCrystalPos;
    public static BlockPos forObsidianPos;
    private final List<BlockPos> sphere = new ArrayList<BlockPos>();
    public static EntityEnderCrystal crystal;
    public static List<EntityLivingBase> targetezs;
    final List<EntityEnderCrystal> crystals = new CopyOnWriteArrayList<EntityEnderCrystal>();
    private static boolean hasActionToCPSHint;
    private float cpsDelta = 0.0f;
    private int ticks;
    private boolean skipTicks = false;
    private boolean callRotateUpOnHitCrystal;
    private final ArrayList<PopEffect> hitPops = new ArrayList();
    AnimationUtils pointEffaPC = new AnimationUtils(0.0f, 0.0f, 0.05f);
    AnimationUtils pointXSmooth = new AnimationUtils(0.0f, 0.0f, 0.2f);
    AnimationUtils pointYSmooth = new AnimationUtils(0.0f, 0.0f, 0.2f);
    AnimationUtils pointZSmooth = new AnimationUtils(0.0f, 0.0f, 0.2f);
    Vec3d lastRotatedVec;
    Vec3d lastRotatedVecNotNulled;
    float callYawMoveYaw = -1.2345679E8f;

    public CrystalField() {
        super("CrystalField", 0, Module.Category.COMBAT);
        this.ProcessRateLevel = new ModeSettings("ProcessRateLevel", "Normal", this, new String[]{"Low", "Normal", "High", "Powerful"});
        this.settings.add(this.ProcessRateLevel);
        this.Rotations = new BoolSettings("Rotations", false, this);
        this.settings.add(this.Rotations);
        this.RotateMoveSide = new BoolSettings("RotateMoveSide", false, this, () -> this.Rotations.getBool());
        this.settings.add(this.RotateMoveSide);
        this.CrystalHand = new ModeSettings("CrystalHand", "Auto", this, new String[]{"OffHand", "MainHand", "Auto"});
        this.settings.add(this.CrystalHand);
        this.PlaceObsidian = new BoolSettings("PlaceObsidian", true, this);
        this.settings.add(this.PlaceObsidian);
        this.ObsidianHand = new ModeSettings("ObsidianHand", "MainHand", this, new String[]{"OffHand", "MainHand", "Auto"}, () -> this.PlaceObsidian.getBool());
        this.settings.add(this.ObsidianHand);
        this.Swing = new ModeSettings("Swing", "Packet", this, new String[]{"None", "Packet", "Client"});
        this.settings.add(this.Swing);
        this.PlaceIgnoreWalls = new BoolSettings("PlaceIgnoreWalls", true, this, () -> this.PlaceObsidian.getBool());
        this.settings.add(this.PlaceIgnoreWalls);
        this.UseInventory = new BoolSettings("UseInventory", true, this, () -> !this.CrystalHand.currentMode.equalsIgnoreCase("OffHand") || this.PlaceObsidian.getBool() && !this.ObsidianHand.currentMode.equalsIgnoreCase("OffHand"));
        this.settings.add(this.UseInventory);
        this.NoSuicide = new BoolSettings("NoSuicide", true, this);
        this.settings.add(this.NoSuicide);
        this.FirstPosSpammer = new BoolSettings("FirstPosSpammer", true, this);
        this.settings.add(this.FirstPosSpammer);
        this.SpamRule = new ModeSettings("SpamRule", "Always", this, new String[]{"Never", "Always", "LowHP", "LowArm", "LowHpOrArm"});
        this.settings.add(this.SpamRule);
        this.OnHealth = new FloatSettings("OnHealth", 10.0f, 20.0f, 1.0f, this, () -> this.SpamRule.currentMode.toLowerCase().contains("hp"));
        this.settings.add(this.OnHealth);
        this.MaxTargetsCount = new FloatSettings("MaxTargetsCount", 3.0f, 6.0f, 1.0f, this);
        this.settings.add(this.MaxTargetsCount);
        this.CPSBypass = new BoolSettings("CPSBypass", false, this);
        this.settings.add(this.CPSBypass);
        get = this;
    }

    private float[] getDelaysAsProcessRateLevel(boolean isNewVersion) {
        float[] delays = new float[2];
        switch (this.ProcessRateLevel.currentMode) {
            case "Low": {
                delays[0] = 250.0f;
                delays[1] = 125.0f;
                break;
            }
            case "Normal": {
                delays[0] = 150.0f;
                delays[1] = 150.0f;
                break;
            }
            case "High": {
                delays[0] = 50.0f;
                delays[1] = 50.0f;
                break;
            }
            case "Powerful": {
                delays[0] = 25.0f;
                delays[1] = 25.0f;
            }
        }
        return delays;
    }

    private int getPosChangeDelayAsProcessRateLevel() {
        return switch (this.ProcessRateLevel.currentMode) {
            case "Low" -> 4;
            case "Normal" -> 2;
            case "High", "Powerful" -> 1;
            default -> 2;
        };
    }

    private float getTPDelayAsProcessRateLevel() {
        return switch (this.ProcessRateLevel.currentMode) {
            case "Low" -> 800.0f;
            case "Normal" -> 400.0f;
            case "High" -> 100.0f;
            case "Powerful" -> 50.0f;
            default -> 400.0f;
        };
    }

    private String getAuraUpdaterAsProcessRateLevel() {
        return switch (this.ProcessRateLevel.currentMode) {
            case "Low", "Normal" -> "Default";
            case "High", "Powerful" -> "FpsThread";
            default -> "Default";
        };
    }

    private float[] getAuraRanges() {
        float selfHPPC = Minecraft.player != null ? (Minecraft.player.getHealth() + Minecraft.player.getAbsorptionAmount()) / (Minecraft.player.getMaxHealth() + Minecraft.player.getAbsorptionAmount()) : 0.0f;
        return new float[]{5.0f, 1.0f + selfHPPC * 2.25f};
    }

    private float getTpRange() {
        return Fly.get.isActived() ? 60.0f : (Minecraft.player != null && Minecraft.player.onGround ? 30.0f : 45.0f);
    }

    private boolean canCrystalSetDeadAsProcessRateLevel() {
        return this.is1l13lplusVersoin() && (this.ProcessRateLevel.currentMode.equalsIgnoreCase("Powerful") || this.ProcessRateLevel.currentMode.equalsIgnoreCase("High"));
    }

    private boolean isTPA(BlockPos pos) {
        return TPInfluence.get.forCrystalFieldRule() && (pos == null || pos != null && this.getMe().getDistanceToBlockPos(pos) > 5.0);
    }

    private void setTeleportForActs(Runnable actions, Vec3d toPos, boolean usingThis) {
        if (usingThis) {
            int crate;
            double xDiff = this.getMe().posX - toPos.xCoord;
            double yDiff = this.getMe().posY - toPos.yCoord;
            double zDiff = this.getMe().posZ - toPos.zCoord;
            float dst = (float)Math.sqrt(xDiff * xDiff + yDiff * yDiff + zDiff * zDiff);
            int step = (int)(dst / 8.67f) + 1;
            boolean gr = this.getMe().onGround;
            int n = crate = gr ? -1 : 0;
            while (crate < step) {
                Minecraft.player.connection.preSendPacket(new CPacketPlayer(false));
                ++crate;
            }
            Minecraft.player.connection.preSendPacket(new CPacketPlayer.Position(toPos.xCoord, toPos.yCoord, toPos.zCoord, false));
            this.updatePointTP(toPos, true);
            actions.run();
            Minecraft.player.connection.preSendPacket(new CPacketPlayer.Position(this.getMe().posX, this.getMe().posY + 0.42, this.getMe().posZ, false));
            return;
        }
        actions.run();
    }

    private void setTeleportForActs(Runnable actions, BlockPos toPos, boolean usingThis) {
        Vec3d toPosFORCE = new Vec3d(toPos).addVector(0.5, 0.0, 0.5);
        this.setTeleportForActs(actions, toPosFORCE, usingThis);
    }

    private BlockPos bestCurrentablePosStandOpacityNeareble(BlockPos pos, int r) {
        if (pos == null) {
            pos = BlockPos.ORIGIN;
        }
        ArrayList<BlockPos> curs = new ArrayList<BlockPos>();
        for (int x = pos.getX() - r; x < pos.getX() + r; ++x) {
            for (int y = pos.getY(); y > pos.getY() - r; --y) {
                for (int z = pos.getZ() - r; z < pos.getZ() + r; ++z) {
                    BlockPos adds = new BlockPos(x, y, z);
                    if (!(adds.getDistanceToBlockPos(pos) < (double)r) || !CrystalField.mc.world.isAirBlock(adds) || !CrystalField.mc.world.isAirBlock(adds.up()) || adds.getX() == pos.getX() && adds.getZ() == pos.getZ()) continue;
                    curs.add(adds);
                }
            }
        }
        BlockPos pos2 = pos;
        if (curs != null && curs.size() > 1) {
            curs.sort(Comparator.comparing(POS -> POS.getDistanceToBlockPos(pos2)));
        }
        return curs != null && curs.size() != 0 && curs.get(0) != null ? (BlockPos)curs.get(0) : pos;
    }

    private Vec3d vecOfPos(BlockPos pos) {
        return new Vec3d((float)pos.getX() + 0.5f, pos.getY(), (float)pos.getZ() + 0.5f);
    }

    private boolean switchIsSilent() {
        return true;
    }

    private float getStackDurPC(ItemStack stackIn) {
        return stackIn.isItemDamaged() ? 1.0f - MathUtils.clamp((float)stackIn.getItemDamage() / (float)stackIn.getMaxDamage(), 0.0f, 1.0f) : 0.0f;
    }

    private float[] getLivingArmorPC(EntityLivingBase baseIn) {
        float armPC = 0.0f;
        float armMax = 4.0f;
        int armC = 0;
        Iterable<ItemStack> stacks = baseIn.getArmorInventoryList();
        if (stacks != null) {
            for (ItemStack stack : stacks) {
                Item itemInStack = stack.getItem();
                if (!(itemInStack instanceof ItemArmor)) continue;
                armPC += this.getStackDurPC(stack);
                ++armC;
            }
        }
        return new float[]{armPC * 4.0f / armMax, armC};
    }

    private boolean spammingCrystals(EntityLivingBase target, boolean l1l13lplus, boolean forPlaceObsCheck) {
        boolean spam;
        switch (this.SpamRule.currentMode) {
            case "Always": {
                spam = true;
                break;
            }
            case "LowHP": {
                spam = target.getHealth() <= this.OnHealth.getFloat();
                break;
            }
            case "LowArm": {
                float[] armorPCS = this.getLivingArmorPC(target);
                spam = armorPCS[0] < 2.25f || armorPCS[1] < 4.0f;
                break;
            }
            case "LowHpOrArm": {
                boolean bl = spam = target.getHealth() <= this.OnHealth.getFloat();
                if (spam) break;
                float[] armorPCS = this.getLivingArmorPC(target);
                spam = armorPCS[0] < 2.25f || armorPCS[1] < 4.0f;
                break;
            }
            default: {
                spam = false;
            }
        }
        return spam && (forPlaceObsCheck || !BlockUtils.canAttackFeetEntity(target, l1l13lplus));
    }

    private boolean is1l13lplusVersoin() {
        return NewPhisicsFixes.isNewVersion();
    }

    private List<EntityLivingBase> doNotBlowUpEnts(float[] ranges) {
        this.notCurrents.clear();
        for (Entity entity : CrystalField.mc.world.getLoadedEntityList()) {
            String name;
            if (entity == null || !(entity instanceof EntityLivingBase)) continue;
            EntityLivingBase base = (EntityLivingBase)entity;
            if (base instanceof EntityPlayerSP) {
                EntityPlayerSP sp = (EntityPlayerSP)base;
            } else if (!((double)this.getMe().getDistanceToEntity(base) < (double)(ranges[0] + ranges[1]) + 3.1)) continue;
            if ((name = base.getName()).isEmpty() || !Client.friendManager.isFriend(name) && !base.equals(this.getMe())) continue;
            this.notCurrents.add(base);
        }
        return this.notCurrents;
    }

    private boolean posIsAcceptable(BlockPos pos, EntityLivingBase target, List<EntityLivingBase> friendsOrSelf, boolean headPlace, boolean isObsPlaceCheck) {
        EntityPlayer player;
        boolean hasTargetBlow = false;
        boolean hasFRSLFBlow = false;
        if (!(target instanceof EntityPlayer) || !(player = (EntityPlayer)target).isCreative() && !player.isSpectator()) {
            hasTargetBlow = BlockUtils.canPosBeSeenEntity(pos, (Entity)target, BlockUtils.bodyElement.LEGS) || BlockUtils.canPosBeSeenEntity(pos, (Entity)target, BlockUtils.bodyElement.CHEST) && headPlace;
        }
        for (EntityLivingBase bases : friendsOrSelf) {
            EntityPlayer player2;
            if (isObsPlaceCheck && pos.getY() >= BlockUtils.getEntityBlockPos(bases).getY() || bases instanceof EntityPlayer && ((player2 = (EntityPlayer)bases).isCreative() || player2.isSpectator()) || !BlockUtils.canPosBeSeenEntity(pos, (Entity)bases, BlockUtils.bodyElement.LEGS)) continue;
            hasFRSLFBlow = true;
        }
        return hasTargetBlow && !hasFRSLFBlow;
    }

    private boolean isFatalPosition(BlockPos pos, Entity entityFor) {
        BlockPos entityPos = BlockUtils.getEntityBlockPos(entityFor);
        boolean any = false;
        if (entityPos != null && pos != null) {
            for (int y = entityPos.getY() - 4; y < entityPos.getY() - 1; ++y) {
                if (!(MathUtils.getDifferenceOf(pos.getX(), entityPos.getX()) < 2.0) && !(MathUtils.getDifferenceOf(pos.getZ(), entityPos.getZ()) < 2.0)) continue;
                any = true;
            }
        }
        return any;
    }

    private BlockPos posCrystal(List<BlockPos> sphere, float[] ranges, List<EntityLivingBase> targets) {
        this.positionsCrys.clear();
        boolean is1l13lplusVersoin = this.is1l13lplusVersoin();
        boolean nofirstPosSpammer = !this.FirstPosSpammer.getBool();
        for (BlockPos pos2 : sphere) {
            if (!BlockUtils.canPlaceCrystal(pos2, is1l13lplusVersoin)) continue;
            for (EntityLivingBase target : targets) {
                if (target == null || BlockUtils.isOccupiedByEnt(pos2.up(), nofirstPosSpammer) || !is1l13lplusVersoin && BlockUtils.isOccupiedByEnt(pos2.up(2), nofirstPosSpammer) || BlockUtils.getDistanceAtPosToVec(pos2, BlockUtils.getEntityVec3dPos(target)) > 5.65 || !this.posIsAcceptable(pos2, target, this.doNotBlowUpEnts(ranges), false, false) || !this.positionsCrys.stream().noneMatch(added -> added.equals(pos2))) continue;
                this.positionsCrys.add(pos2);
            }
        }
        if (this.positionsCrys.isEmpty()) {
            for (BlockPos pos2 : sphere) {
                if (!BlockUtils.canPlaceCrystal(pos2, is1l13lplusVersoin)) continue;
                for (EntityLivingBase target : targets) {
                    if (target == null || BlockUtils.isOccupiedByEnt(pos2.up(), nofirstPosSpammer) || !is1l13lplusVersoin && BlockUtils.isOccupiedByEnt(pos2.up(2), nofirstPosSpammer) || BlockUtils.getDistanceAtPosToVec(pos2, BlockUtils.getEntityVec3dPos(target)) > 5.65 || !this.posIsAcceptable(pos2, target, this.doNotBlowUpEnts(ranges), this.spammingCrystals(target, is1l13lplusVersoin, false), false) || this.positionsCrys.stream().anyMatch(added -> added.equals(pos2))) continue;
                    this.positionsCrys.add(pos2);
                }
            }
        }
        if (this.positionsCrys.size() > 1 && targets != null && targets.size() > 0) {
            this.positionsCrys.sort(Comparator.comparing(pos -> BlockUtils.getDistanceAtVecToVec(new Vec3d((Vec3i)pos).addVector(0.5, 0.1, 0.5), BlockUtils.getEntityVec3dPos((Entity)targets.get(0)))));
        }
        return this.positionsCrys.size() > 0 ? this.positionsCrys.get(0) : null;
    }

    private BlockPos posObsidian(List<BlockPos> sphere, float[] ranges, List<EntityLivingBase> targets, boolean ignoreWalls) {
        this.positionsObs.clear();
        boolean is1l13lplusVersoin = this.is1l13lplusVersoin();
        List<EntityLivingBase> doNotBlowUpEnts = this.doNotBlowUpEnts(ranges);
        for (BlockPos pos2 : sphere) {
            if (!BlockUtils.canPlaceObsidian(pos2, ranges[0], false, true)) continue;
            for (EntityLivingBase target : targets) {
                if (target == null || pos2 == BlockUtils.getEntityBlockPos(target) || (double)pos2.getY() > target.posY - 0.3780711 || crystal != null) continue;
                if (forCrystalPos != null) {
                    if (BlockUtils.getDistanceAtVecToVec(new Vec3d(forCrystalPos).addVector(0.5, 0.5, 0.5), target.getPositionVector()) < 3.85) continue;
                    if ((double)forCrystalPos.getY() > target.posY + (target.posY - target.lastTickPosY > 0.0 ? 0.01 : (double)0.4f)) continue;
                    if ((double)pos2.getY() > target.posY + (target.posY - target.lastTickPosY > 0.0 ? 0.01 : (double)0.4f)) continue;
                }
                if (BlockUtils.getDistanceAtVecToVec(new Vec3d(pos2).addVector(0.5, 1.0, 0.5), target.getPositionVector()) >= 5.0 || BlockUtils.isOccupiedByEnt(pos2, false) || !ignoreWalls && BlockUtils.getPlaceableSideSeen(pos2, this.getMe()) == null || !this.posIsAcceptable(pos2, target, doNotBlowUpEnts, false, true)) continue;
                this.positionsObs.add(pos2);
            }
        }
        if (this.positionsObs.isEmpty()) {
            for (BlockPos pos2 : sphere) {
                if (!BlockUtils.canPlaceObsidian(pos2, ranges[0], false, true)) continue;
                for (EntityLivingBase target : targets) {
                    if (target == null || pos2 == BlockUtils.getEntityBlockPos(target)) continue;
                    boolean spamCrystals = this.spammingCrystals(target, is1l13lplusVersoin, true);
                    if ((double)pos2.getY() > target.posY - 0.3780711 + (double)(spamCrystals ? 1 : 0) || crystal != null) continue;
                    if (forCrystalPos != null) {
                        if (BlockUtils.getDistanceAtVecToVec(new Vec3d(forCrystalPos).addVector(0.5, 0.5, 0.5), target.getPositionVector()) < 3.85) continue;
                        if ((double)forCrystalPos.getY() > target.posY + (target.posY - target.lastTickPosY > 0.0 ? 0.01 : (double)0.4f)) continue;
                        if ((double)pos2.getY() > target.posY + (target.posY - target.lastTickPosY > 0.0 ? 0.01 : (double)0.4f)) continue;
                    }
                    if (BlockUtils.getDistanceAtVecToVec(new Vec3d(pos2).addVector(0.5, 1.0, 0.5), target.getPositionVector()) >= 5.0 || BlockUtils.isOccupiedByEnt(pos2, false) || BlockUtils.getPlaceableSideSeen(pos2, this.getMe()) == null || !this.posIsAcceptable(pos2, target, doNotBlowUpEnts, spamCrystals, true)) continue;
                    this.positionsObs.add(pos2);
                }
            }
        }
        if (this.positionsObs.size() > 1 && targets != null && targets.size() > 0) {
            this.positionsObs.sort(Comparator.comparing(pos -> BlockUtils.getDistanceAtVecToVec(new Vec3d((Vec3i)pos).addVector(0.5, 0.5, 0.5), BlockUtils.getEntityVec3dPos((Entity)targets.get(0)))));
        }
        return this.positionsObs.size() > 0 ? this.positionsObs.get(0) : null;
    }

    private Vec3d selfVirtPos() {
        BlockPos virtAt;
        BlockPos blockPos = virtAt = !CrystalField.listIsEmptyOrNull(CrystalField.getTargets()) ? BlockUtils.getEntityBlockPos(CrystalField.getTargets().get(0)) : null;
        if (virtAt != null && this.isTPA(virtAt)) {
            return new Vec3d(virtAt).addVector(0.5, 0.5, 0.5);
        }
        return BlockUtils.getEntityVec3dPos(this.getMe());
    }

    private boolean updateSphere(List<BlockPos> sphere, float range, Vec3d atMyVirtPos) {
        sphere.clear();
        List<EntityLivingBase> targets = CrystalField.getTargets();
        BlockUtils.getSphere(atMyVirtPos.addVector(0.0, this.getMe().getEyeHeight(), 0.0), range).forEach(pos -> {
            if (pos != null) {
                if (targets.isEmpty()) {
                    sphere.add((BlockPos)pos);
                } else {
                    double maxY = 0.0;
                    for (EntityLivingBase target : targets) {
                        if (maxY >= target.posY) continue;
                        maxY = target.posY;
                    }
                    if ((double)pos.getY() <= maxY + 1.0) {
                        sphere.add((BlockPos)pos);
                    }
                }
            }
        });
        return !CrystalField.listIsEmptyOrNull(sphere);
    }

    private void updatePosForPlace(List<EntityLivingBase> targets, float[] radiuses, Vec3d atMyVirtPos, boolean canUseInventory, boolean onlyReset, boolean ignoreWalls) {
        if (onlyReset || CrystalField.listIsEmptyOrNull(CrystalField.getTargets()) || !this.updateSphere(this.sphere, radiuses[0], atMyVirtPos) || !this.haveItem(this.itemCrystal(), canUseInventory) || CrystalField.listIsEmptyOrNull(targets) || this.stopBreaks(this.NoSuicide.getBool())) {
            forCrystalPos = null;
            forObsidianPos = null;
            return;
        }
        forCrystalPos = this.posCrystal(this.sphere, radiuses, targets);
        AntiCrystal.get.removeCache(forCrystalPos);
        forObsidianPos = this.PlaceObsidian.getBool() && this.haveItem(this.itemObsidian(), canUseInventory) ? this.posObsidian(this.sphere, radiuses, targets, ignoreWalls) : null;
        AntiCrystal.get.removeCache(forObsidianPos);
    }

    private Item itemCrystal() {
        return Items.END_CRYSTAL;
    }

    private Item itemObsidian() {
        return Item.getItemFromBlock(Blocks.OBSIDIAN);
    }

    public static int getItem(Item designatedItem) {
        for (int i = 0; i < 44; ++i) {
            Item item = Minecraft.player.inventory.getStackInSlot(i).getItem();
            if (!(item instanceof Item) || !item.equals(designatedItem)) continue;
            return i;
        }
        return -1;
    }

    private boolean haveItemInInventory(Item item, boolean searchInInventory) {
        return this.getSlotForItem(item, searchInInventory) != -1;
    }

    private boolean haveItem(Item item, boolean searchInInventory) {
        return (this.haveItemInInventory(item, searchInInventory) || Minecraft.player.getHeldItemOffhand().getItem() == item) && this.getUsedHand(item, searchInInventory) != null;
    }

    private int getSlotForItem(Item item, boolean canUseInventory) {
        int slot = CrystalField.getItem(item);
        return slot > 8 && !canUseInventory ? -1 : slot;
    }

    private boolean itemInOffHand(Item item) {
        return Minecraft.player.getHeldItemOffhand().getItem() == item;
    }

    private boolean canAddTargetez(EntityLivingBase target) {
        return target != null && target.isEntityAlive() && target.getHealth() > 0.0f && target != Minecraft.player && target != this.getMe() && !Client.friendManager.isFriend(target.getName()) && !(target instanceof EntityArmorStand) && !Client.summit(target);
    }

    private double getDistanceToTargetEntity(Entity target) {
        return this.getMe().getSmartDistanceToAABB(RotationUtil.getLookRots(this.getMe(), target), target);
    }

    private double getEntityValueToSort(EntityLivingBase target) {
        if (target != null && (double)target.getHealth() > 0.0) {
            double value = 1.0;
            double health = (double)target.getHealth() + (double)target.getAbsorptionAmount();
            double maxHealth = (double)target.getMaxHealth() + (double)target.getAbsorptionAmount();
            value *= MathUtils.clamp(health / maxHealth * 3.0, 0.25, 1.0);
            value *= MathUtils.clamp((double)target.getTotalArmorValue() / 10.0, 0.65, 1.0);
            return value *= MathUtils.clamp(1.0 - (double)BlockUtils.feetCrackPosesCount(target, this.is1l13lplusVersoin()) / 2.0, 0.0, 1.0);
        }
        return 0.0;
    }

    private boolean canBreakCrystal(EntityEnderCrystal crys, float[] ranges) {
        if (crys == null) {
            return false;
        }
        for (EntityLivingBase frOrSelf : this.doNotBlowUpEnts(ranges)) {
            EntityPlayer player;
            if (frOrSelf == null || CrystalField.listIsEmptyOrNull(CrystalField.getTargets()) || !CrystalField.getTargets().stream().anyMatch(target -> target.getSmoothDistanceToEntity(crys) < ranges[0] + ranges[1]) || frOrSelf instanceof EntityPlayer && ((player = (EntityPlayer)frOrSelf).isCreative() || player.isSpectator())) continue;
            return !BlockUtils.canPosBeSeenEntityWithCustomVec(BlockUtils.getEntityVec3dPos(crys).addVector(0.0, -1.0, 0.0), (Entity)frOrSelf, frOrSelf == this.getMe() ? this.selfVirtPos() : BlockUtils.getEntityVec3dPos(frOrSelf), BlockUtils.bodyElement.FEET) && BlockUtils.blockMaterialIsCurrent(BlockUtils.getEntityBlockPos(crys).down());
        }
        return BlockUtils.blockMaterialIsCurrent(BlockUtils.getEntityBlockPos(crys).down());
    }

    public static boolean listIsEmptyOrNull(List list) {
        return list == null || list.isEmpty() || list.size() == 0;
    }

    private void updateTargets(float[] ranges, int maxCountTargets, boolean onlyReset) {
        targetezs.clear();
        if (onlyReset) {
            return;
        }
        CrystalField.mc.world.getLoadedEntityList().forEach(entity -> {
            EntityLivingBase base;
            float newRange = ranges[0] + ranges[1];
            if (entity != null && entity instanceof EntityLivingBase && this.canAddTargetez(base = (EntityLivingBase)entity) && this.getDistanceToTargetEntity(base) <= (double)newRange) {
                newRange = (float)this.getDistanceToTargetEntity(base);
                if (targetezs.size() < maxCountTargets) {
                    targetezs.add(base);
                }
            }
        });
        if (!CrystalField.listIsEmptyOrNull(targetezs) && targetezs.size() > 1) {
            targetezs.sort(Comparator.comparing(target -> this.getEntityValueToSort((EntityLivingBase)target)));
        }
    }

    private void updateCrystals(float[] ranges, boolean onlyReset) {
        this.crystals.clear();
        if (onlyReset) {
            CrystalField.crystal = null;
            return;
        }
        for (Entity entity : CrystalField.mc.world.getLoadedEntityList()) {
            if (entity == null || !(entity instanceof EntityEnderCrystal)) continue;
            EntityEnderCrystal crystal2 = (EntityEnderCrystal)entity;
            if (!(this.getMe().getDistanceAtEye(crystal2.posX, crystal2.posY, crystal2.posZ) <= (double)(ranges[this.isTPA(BlockUtils.getEntityBlockPos(crystal2)) ? 2 : 0] + crystal2.width / 1.36f)) || crystal2 == null || crystal2.isDead || !this.canBreakCrystal(crystal2, ranges) || crystal2.ticksExisted == 1) continue;
            this.crystals.add(crystal2);
        }
        if (!CrystalField.listIsEmptyOrNull(this.crystals)) {
            if (this.crystals.size() > 1) {
                this.crystals.sort(Comparator.comparing(crystal -> BlockUtils.getDistanceAtVecToVec(this.selfVirtPos(), BlockUtils.getEntityVec3dPos(crystal))));
            }
            CrystalField.crystal = this.stopBreaks(this.NoSuicide.getBool()) ? null : this.crystals.get(0);
        }
    }

    private EntityPlayer getMe() {
        return FreeCam.get.actived && FreeCam.fakePlayer != null ? FreeCam.fakePlayer : Minecraft.player;
    }

    private float getSmartBlowupWarnHP() {
        float hp = 4.0f;
        int armorCount = 0;
        EntityPlayer p = this.getMe();
        float absorbDT = 1.0f;
        for (int i = 0; i < 4; ++i) {
            if (!BlockUtils.isArmor(p, BlockUtils.armorElementByInt(i))) {
                hp += 1.5f;
                absorbDT += 0.5f;
                continue;
            }
            ++armorCount;
        }
        if (p.getActivePotionEffect(Potion.getPotionById(10)) != null) {
            hp -= 0.5f;
        }
        if (p.isHandActive() && p.getActiveItemStack().getItem() instanceof ItemAppleGold && p.getItemInUseMaxCount() > 30) {
            hp -= 1.0f;
        }
        if (p.getAbsorptionAmount() > 0.0f) {
            hp -= p.getAbsorptionAmount() / MathUtils.clamp(absorbDT, 1.0f, 2.0f);
        }
        if (p.inventory.armorItemInSlot(2).getItem() instanceof ItemElytra || armorCount < 4) {
            hp += 3.0f;
        }
        return MathUtils.clamp(hp, 5.0f, 20.0f);
    }

    private boolean stopBreaks(boolean checkCrystal) {
        return checkCrystal && !Minecraft.player.isCreative() && !Minecraft.player.isSpectator() && (OffHand.totemTaken || OffHand.crystalWarn(6.656f) || Minecraft.player.getHealth() / Minecraft.player.getMaxHealth() < this.getSmartBlowupWarnHP() / Minecraft.player.getMaxHealth());
    }

    private boolean slotIsNan(Item itemIn, boolean canUseInventory) {
        int sl = this.getSlotForItem(itemIn, canUseInventory);
        return sl < 0 || sl > (canUseInventory ? 44 : 8);
    }

    private void switcherForAction(EnumHand placeHand, boolean packetSwap, Item swapTo, Runnable action, boolean useInventory) {
        boolean isInHand;
        if (swapTo == null) {
            return;
        }
        int slotHand = Minecraft.player.inventory.currentItem;
        int slotItem = this.getSlotForItem(swapTo, false);
        boolean hasInvUse = false;
        if (useInventory && slotItem == -1 && placeHand == EnumHand.MAIN_HAND) {
            slotItem = this.getSlotForItem(swapTo, useInventory);
            hasInvUse = true;
        }
        boolean bl = isInHand = slotHand == slotItem;
        if (placeHand == EnumHand.OFF_HAND) {
            action.run();
            return;
        }
        if (this.slotIsNan(swapTo, hasInvUse)) {
            return;
        }
        if (slotItem < 9) {
            if (placeHand == EnumHand.MAIN_HAND && !isInHand && !this.slotIsNan(swapTo, false)) {
                boolean packetSync = Minecraft.player.inventory.currentItem != slotItem;
                Minecraft.player.inventory.currentItem = slotItem;
                if (packetSync) {
                    CrystalField.mc.playerController.syncCurrentPlayItem();
                }
            }
            if (isInHand || !this.slotIsNan(swapTo, false)) {
                action.run();
            }
            if (placeHand == EnumHand.MAIN_HAND && !isInHand && !this.slotIsNan(swapTo, false) && packetSwap) {
                Minecraft.player.inventory.currentItem = slotHand;
                CrystalField.mc.playerController.syncCurrentPlayItem();
            }
        } else if (hasInvUse) {
            ItemStack stack = Minecraft.player.inventory.getStackInSlot(slotItem);
            ItemStack prevHandStack = Minecraft.player.getHeldItemMainhand();
            if (prevHandStack != null && Minecraft.player.isCreative() && (Minecraft.player.openContainer == null || Minecraft.player.openContainer instanceof ContainerPlayer)) {
                Minecraft.player.connection.sendPacket(new CPacketCreativeInventoryAction(Minecraft.player.inventory.currentItem + 36, stack));
                CrystalField.mc.playerController.syncCurrentPlayItem();
                action.run();
                int slotPrevStack = -1;
                for (int i = 0; i < 44; ++i) {
                    ItemStack geted = Minecraft.player.inventory.getStackInSlot(i);
                    if (geted != prevHandStack) continue;
                    slotPrevStack = i;
                }
                CrystalField.mc.playerController.windowClick(0, slotPrevStack, Minecraft.player.inventory.currentItem, ClickType.SWAP, Minecraft.player);
                return;
            }
            CrystalField.mc.playerController.windowClick(0, slotItem, Minecraft.player.inventory.currentItem, ClickType.SWAP, Minecraft.player);
            action.run();
            CrystalField.mc.playerController.windowClick(0, slotItem, Minecraft.player.inventory.currentItem, ClickType.SWAP, Minecraft.player);
        }
    }

    private void placeCrystal(BlockPos pos, EnumHand placeHand, boolean packetSwap, TimerHelper placeCrysDelay, boolean useTeleport, boolean useInventory) {
        this.setTeleportForActs(() -> this.switcherForAction(placeHand, packetSwap, this.itemCrystal(), () -> {
            Minecraft.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(pos, EnumFacing.UP, placeHand));
            placeCrysDelay.reset();
            hasActionToCPSHint = true;
        }, useInventory), this.vecOfPos(this.bestCurrentablePosStandOpacityNeareble(pos, 5)), useTeleport);
    }

    private void placeObsidian(BlockPos pos, EnumHand placeHand, boolean packetSwap, TimerHelper placeObsDelay, boolean useTeleport, boolean useInventory) {
        if (pos == null) {
            return;
        }
        this.setTeleportForActs(() -> this.switcherForAction(placeHand, packetSwap, this.itemObsidian(), () -> {
            EnumFacing enumFace = BlockUtils.getPlaceableSideSeen(pos, this.getMe());
            if (enumFace == null) {
                enumFace = BlockUtils.getPlaceableSide(pos);
                if (enumFace == null) {
                    return;
                }
                EnumFacing faceOpposite = enumFace.getOpposite();
                BlockPos offsetPos = pos.offset(enumFace);
                Vec3d facingVec = new Vec3d(offsetPos).addVector(0.5, 0.5, 0.5).add(new Vec3d(faceOpposite.getDirectionVec()).scale(0.5));
                CrystalField.mc.playerController.processRightClickBlock(Minecraft.player, CrystalField.mc.world, offsetPos, faceOpposite, facingVec, placeHand);
                this.swingAction(placeHand);
                return;
            }
            Vec3d placeFaceVec = new Vec3d(pos).addVector(0.5, 0.5, 0.5).addVector((double)enumFace.getFrontOffsetX() * 0.5, (double)enumFace.getFrontOffsetY() * 0.5, (double)enumFace.getFrontOffsetZ() * 0.5);
            if (this.getMe() == Minecraft.player && Minecraft.player.getDistanceAtEye(placeFaceVec.xCoord, placeFaceVec.yCoord, placeFaceVec.zCoord) <= 5.0) {
                float[] rotate = RotationUtil.getNeededFacing(placeFaceVec, false, Minecraft.player, false);
                float prevYaw = Minecraft.player.rotationYaw;
                float prevPitch = Minecraft.player.rotationPitch;
                Minecraft.player.rotationYaw = rotate[0];
                Minecraft.player.rotationPitch = rotate[1];
                HitAura.get.rotations = rotate;
                CrystalField.mc.entityRenderer.getMouseOver(1.0f);
                BlockPos placePos = CrystalField.mc.objectMouseOver.getBlockPos();
                if (placePos != null && (placePos = placePos.offset(enumFace)) != null && CrystalField.mc.objectMouseOver.sideHit != null && CrystalField.mc.objectMouseOver.hitVec != null) {
                    CrystalField.mc.playerController.processRightClickBlock(Minecraft.player, CrystalField.mc.world, CrystalField.mc.objectMouseOver.getBlockPos(), CrystalField.mc.objectMouseOver.sideHit, CrystalField.mc.objectMouseOver.hitVec, placeHand);
                    this.swingAction(placeHand);
                }
                Minecraft.player.rotationYaw = prevYaw;
                Minecraft.player.rotationPitch = prevPitch;
                CrystalField.mc.entityRenderer.getMouseOver(1.0f);
            }
            this.skipTicks = !CrystalField.listIsEmptyOrNull(targetezs);
        }, useInventory), this.vecOfPos(this.bestCurrentablePosStandOpacityNeareble(pos, 5)), useTeleport);
        placeObsDelay.reset();
    }

    private void swingAction(EnumHand breakHand) {
        switch (this.Swing.currentMode) {
            case "Packet": {
                Minecraft.player.connection.sendPacket(new CPacketAnimation(breakHand));
                break;
            }
            case "Client": {
                Minecraft.player.swingArm(breakHand);
                break;
            }
        }
    }

    private void breakCrystal(EntityEnderCrystal crystal, EnumHand breakHand, boolean setDead, TimerHelper attackDelay, boolean useTeleport) {
        this.setTeleportForActs(() -> {
            if (this.canSpawnPopEffect()) {
                this.addPopsEffToPos(BlockUtils.getEntityBlockPos(crystal));
            }
            Minecraft.player.connection.sendPacket(new CPacketUseEntity(crystal));
            this.swingAction(breakHand);
            attackDelay.reset();
            hasActionToCPSHint = true;
            if (setDead) {
                crystal.setDead();
            }
            this.callRotateUpOnHitCrystal = !crystal.isDead;
        }, this.vecOfPos(this.bestCurrentablePosStandOpacityNeareble(BlockUtils.getEntityBlockPos(crystal).down(), 6)), useTeleport);
    }

    private EnumHand getUsedHand(Item itemIn, boolean canUseInventory) {
        String mode;
        String string = mode = itemIn == Items.END_CRYSTAL ? this.CrystalHand.currentMode : this.ObsidianHand.currentMode;
        if (mode == null) {
            return null;
        }
        switch (mode) {
            case "OffHand": {
                return this.itemInOffHand(itemIn) ? EnumHand.OFF_HAND : null;
            }
            case "MainHand": {
                return this.haveItemInInventory(itemIn, canUseInventory) ? EnumHand.MAIN_HAND : null;
            }
            case "Auto": {
                return this.itemInOffHand(itemIn) ? EnumHand.OFF_HAND : (this.haveItemInInventory(itemIn, canUseInventory) ? EnumHand.MAIN_HAND : null);
            }
        }
        return null;
    }

    @Override
    public void onToggled(boolean actived) {
        targetezs.clear();
        crystal = null;
        forCrystalPos = null;
        forObsidianPos = null;
        this.skipTicks = true;
        this.ticks = 1;
        super.onToggled(actived);
    }

    private float[] getRanges(boolean teleportMode) {
        float[] ranges = this.getAuraRanges();
        float tpRange = this.getTpRange();
        return new float[]{ranges[0], teleportMode ? tpRange : ranges[1], tpRange};
    }

    private float getCPSRandomizer(boolean doRandom) {
        if (doRandom && hasActionToCPSHint) {
            this.cpsDelta = 75.0f * (float)Math.random();
            hasActionToCPSHint = false;
        }
        return doRandom ? this.cpsDelta : 0.0f;
    }

    private float[] getDelays(boolean teleportMode, boolean cpsBypass) {
        float random = this.getCPSRandomizer(cpsBypass && !this.ProcessRateLevel.currentMode.equalsIgnoreCase("Powerful") && !this.ProcessRateLevel.currentMode.equalsIgnoreCase("High"));
        if (teleportMode) {
            float DL = Math.max(this.getTPDelayAsProcessRateLevel() + random, 0.0f);
            return new float[]{DL, DL};
        }
        float[] delays = this.getDelaysAsProcessRateLevel(this.is1l13lplusVersoin());
        return new float[]{Math.max(delays[0] + random, 0.0f), Math.max(delays[0] + random, 0.0f)};
    }

    private TimerHelper[] getTimers(boolean teleportMode) {
        if (teleportMode) {
            return new TimerHelper[]{this.tpaDelay, this.tpaDelay, this.tpaDelay};
        }
        return new TimerHelper[]{this.placeCrysDelay, this.placeObsDelay, this.attackDelay};
    }

    @Override
    public void onRenderUpdate() {
        if (this.getAuraUpdaterAsProcessRateLevel().equalsIgnoreCase("FpsThread")) {
            this.updateCrystalAura(this.isTPA(forObsidianPos != null ? forObsidianPos : (forCrystalPos != null ? forCrystalPos : (crystal != null ? BlockUtils.getEntityBlockPos(crystal) : null))));
            this.crystalAura();
        }
    }

    @Override
    public void onUpdate() {
        if (this.MaxTargetsCount.getFloat() != (float)((int)this.MaxTargetsCount.getFloat()) && CrystalField.mc.currentScreen instanceof ClickGuiScreen != Mouse.isButtonDown((int)0)) {
            this.MaxTargetsCount.setFloat((int)this.MaxTargetsCount.getFloat());
        }
        this.popsEffRemoveAuto();
        if (this.getAuraUpdaterAsProcessRateLevel().equalsIgnoreCase("Default")) {
            this.updateCrystalAura(this.isTPA(forObsidianPos != null ? forObsidianPos : (forCrystalPos != null ? forCrystalPos : (crystal != null ? BlockUtils.getEntityBlockPos(crystal) : null))));
            this.crystalAura();
        }
    }

    public static List<EntityLivingBase> getTargets() {
        return targetezs;
    }

    private boolean canUseCrystalFieldNow() {
        return true;
    }

    private void updateCrystalAura(boolean teleportMode) {
        boolean canUseInventory = this.UseInventory.getBool() && this.UseInventory.isVisible();
        float[] ranges = this.getRanges(teleportMode);
        int delay = this.getPosChangeDelayAsProcessRateLevel();
        boolean canUseAuraNow = this.canUseCrystalFieldNow();
        boolean ignoreWalls = this.PlaceIgnoreWalls.getBool();
        this.updateCrystals(ranges, !canUseAuraNow);
        if (delay == 1 || delay == 2 ? this.ticks % delay == 0 : this.ticks % delay == 0 || this.ticks % delay == 1 || this.skipTicks) {
            boolean f2;
            boolean f1 = delay <= 2 || this.ticks % delay == 0 || this.skipTicks;
            boolean bl = f2 = delay <= 2 || this.ticks % delay == 1 || this.skipTicks;
            if (f1) {
                this.updateTargets(ranges, (int)MathUtils.clamp(this.MaxTargetsCount.getFloat(), 1.0f, 8.0f) * (this.getUsedHand(this.itemCrystal(), canUseInventory) != null ? 1 : 0), !canUseAuraNow);
            }
            if (f2) {
                this.updatePosForPlace(CrystalField.getTargets(), ranges, this.selfVirtPos(), canUseInventory, !canUseAuraNow, ignoreWalls);
            }
            this.skipTicks = false;
        }
        this.ticks = this.ticks > 1000 ? 0 : ++this.ticks;
    }

    @Override
    public String getDisplayName() {
        boolean teleportAction = this.isTPA(forObsidianPos != null ? forObsidianPos : (forCrystalPos != null ? forCrystalPos : (crystal != null ? BlockUtils.getEntityBlockPos(crystal) : null)));
        float[] ranges = this.getRanges(teleportAction);
        return this.getName() + TextFormatting.GRAY + " - " + String.format("%.1f", Float.valueOf(ranges[0])) + "+" + String.format("%.1f", Float.valueOf(ranges[1])) + (String)(targetezs == null ? "error" : "-c" + targetezs.size());
    }

    private void crystalAura() {
        boolean hasPlaceCrysDelay;
        EnumHand obsidianHand;
        boolean cpsBypass = this.CPSBypass.getBool();
        boolean teleportAction = this.isTPA(forObsidianPos != null ? forObsidianPos : (forCrystalPos != null ? forCrystalPos : (crystal != null ? BlockUtils.getEntityBlockPos(crystal) : null)));
        float[] delaysActions = this.getDelays(teleportAction, cpsBypass);
        TimerHelper[] timers = this.getTimers(teleportAction);
        boolean setDeadCrystal = this.canCrystalSetDeadAsProcessRateLevel();
        boolean canUseInventory = this.UseInventory.getBool() && this.UseInventory.isVisible();
        EnumHand crystalHand = this.getUsedHand(this.itemCrystal(), canUseInventory);
        if (crystalHand == null) {
            crystalHand = EnumHand.MAIN_HAND;
        }
        if ((obsidianHand = this.getUsedHand(this.itemObsidian(), canUseInventory)) == null) {
            obsidianHand = EnumHand.MAIN_HAND;
        }
        boolean buseMainHand = Minecraft.player.isHandActive() && Minecraft.player.getActiveHand() == EnumHand.MAIN_HAND;
        boolean stopBuseCrys = crystalHand == EnumHand.MAIN_HAND && buseMainHand;
        boolean stopBuseObs = obsidianHand == EnumHand.MAIN_HAND && buseMainHand;
        boolean placeObsidian = this.PlaceObsidian.getBool();
        boolean hasAttackDelay = delaysActions[1] == 0.0f || timers[2].hasReached(delaysActions[1]);
        boolean hasPlaceObsDelay = delaysActions[0] == 0.0f || timers[1].hasReached(delaysActions[0]);
        boolean bl = hasPlaceCrysDelay = delaysActions[0] == 0.0f || timers[0].hasReached(delaysActions[0]);
        if (CrystalField.listIsEmptyOrNull(CrystalField.getTargets()) || !this.canUseCrystalFieldNow()) {
            crystal = null;
            return;
        }
        if (crystal != null && hasAttackDelay) {
            this.breakCrystal(crystal, crystalHand != null ? crystalHand : EnumHand.MAIN_HAND, setDeadCrystal, timers[2], teleportAction);
            crystal = null;
        }
        if (forCrystalPos != null && hasPlaceCrysDelay && this.haveItem(this.itemCrystal(), canUseInventory) && !stopBuseCrys) {
            this.placeCrystal(forCrystalPos, crystalHand, this.switchIsSilent(), timers[0], teleportAction, canUseInventory);
        }
        if (forObsidianPos != null && hasPlaceObsDelay && this.haveItem(this.itemCrystal(), canUseInventory) && this.haveItem(this.itemObsidian(), canUseInventory) && !stopBuseObs && placeObsidian) {
            this.placeObsidian(forObsidianPos, obsidianHand, this.switchIsSilent(), timers[1], teleportAction, canUseInventory);
        }
    }

    private void drawObsidianPosESP(BlockPos pos, int color) {
        AxisAlignedBB aabb = new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), (double)pos.getX() + 1.0, (double)pos.getY() + 1.0, (double)pos.getZ() + 1.0);
        int col1 = color;
        int col2 = ColorUtils.swapAlpha(col1, (float)ColorUtils.getAlphaFromColor(col1) / 3.0f);
        RenderUtils.drawCanisterBox(aabb, true, true, true, col1, col1, col2);
    }

    private void drawCrystalPosESP(BlockPos pos, int color) {
        AxisAlignedBB aabb = new AxisAlignedBB(pos.getX(), (double)pos.getY() + 1.0, pos.getZ(), (double)pos.getX() + 1.0, (double)pos.getY() + 1.025, (double)pos.getZ() + 1.0);
        int col1 = ColorUtils.swapAlpha(color, (float)ColorUtils.getAlphaFromColor(color) / 2.0f);
        int col2 = ColorUtils.swapAlpha(color, (float)ColorUtils.getAlphaFromColor(color) / 8.0f);
        RenderUtils.drawCanisterBox(aabb, true, false, true, col1, 0, col2);
    }

    private void drawAllPopEffects() {
        int i = 0;
        float aPC = this.stateAnim.getAnim();
        for (PopEffect effect : this.hitPops) {
            this.drawPopEffect(effect, ClientColors.getColor1(i, aPC));
            i += 60;
        }
    }

    @Override
    public void alwaysRender3D() {
        float f = this.stateAnim.to = this.actived ? 1.0f : 0.0f;
        if ((double)this.stateAnim.getAnim() < 0.03) {
            return;
        }
        this.drawPointTP();
        if (!CrystalField.listIsEmptyOrNull(this.hitPops)) {
            RenderUtils.setup3dForBlockPos(() -> this.drawAllPopEffects(), false);
        }
        if (forObsidianPos != null || forCrystalPos != null) {
            RenderUtils.setup3dForBlockPos(() -> {
                if (forObsidianPos != null) {
                    this.drawObsidianPosESP(forObsidianPos, ColorUtils.getColor(80, 0, 255, 95));
                }
                if (forCrystalPos != null) {
                    this.drawCrystalPosESP(forCrystalPos, ColorUtils.getColor(245, 180, 255));
                }
            }, true);
        }
    }

    private boolean canSpawnPopEffect() {
        return forCrystalPos != null;
    }

    private final void popsEffRemoveAuto() {
        if (!CrystalField.listIsEmptyOrNull(this.hitPops)) {
            this.hitPops.removeIf(effect -> effect != null && effect.getDeltaTime() >= 1.0f);
        }
    }

    private float getPopsMaxTime() {
        return 1150.0f;
    }

    private void addPopsEffToPos(BlockPos toPos) {
        Vec3d pos = new Vec3d(toPos).addVector(0.5, 0.0, 0.5);
        this.hitPops.add(new PopEffect(this.getPopsMaxTime(), pos));
    }

    private void drawPopEffect(PopEffect effect, int startColor) {
        double cos;
        double sin;
        int index;
        if ((double)effect.getDeltaTime() > 1.0) {
            return;
        }
        float lineWidth = 0.001f + 5.999f * (1.0f - effect.getDeltaTime()) * this.stateAnim.anim;
        float aPC = effect.getDeltaTime();
        aPC = aPC > 0.5f ? 1.0f - effect.getDeltaTime() : effect.getDeltaTime();
        aPC = (aPC *= 3.5f) < 0.0f ? 0.0f : (aPC > 1.0f ? 1.0f : aPC);
        double range = 0.375f * (1.0f - aPC) * (1.0f - effect.getDeltaTime()) + 0.225f;
        int effectColor = ColorUtils.swapAlpha(startColor, (float)ColorUtils.getAlphaFromColor(startColor) * aPC);
        Vec3d vert = effect.getPos().addVector(0.0, (double)effect.getDeltaTime() * 0.3, 0.0);
        GL11.glEnable((int)2848);
        GL11.glHint((int)3154, (int)4354);
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GL11.glLineWidth((float)lineWidth);
        RenderUtils.glColor(effectColor);
        GL11.glBegin((int)2);
        for (index = 0; index <= 360; index += 10) {
            sin = -Math.sin(Math.toRadians(index)) * range * (double)(index % 20 == 0 ? 0.9f : 1.0f);
            cos = Math.cos(Math.toRadians(index)) * range * (double)(index % 20 == 0 ? 0.9f : 1.0f);
            GL11.glVertex3d((double)(vert.xCoord + sin), (double)(vert.yCoord + (index % 20 == 0 ? 0.03 : 0.0)), (double)(vert.zCoord + cos));
        }
        GL11.glEnd();
        GL11.glLineWidth((float)(lineWidth + 6.0f));
        RenderUtils.glColor(ColorUtils.swapAlpha(effectColor, (float)ColorUtils.getAlphaFromColor(effectColor) / 8.0f));
        GL11.glBegin((int)2);
        for (index = 0; index <= 360; index += 10) {
            sin = -Math.sin(Math.toRadians(index)) * range * (double)(index % 20 == 0 ? 0.9f : 1.0f);
            cos = Math.cos(Math.toRadians(index)) * range * (double)(index % 20 == 0 ? 0.9f : 1.0f);
            GL11.glVertex3d((double)(vert.xCoord + sin), (double)(vert.yCoord + (index % 20 == 0 ? 0.03 : 0.0)), (double)(vert.zCoord + cos));
        }
        GL11.glEnd();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GL11.glHint((int)3154, (int)4352);
        GL11.glDisable((int)2848);
    }

    void updatePointTP(Vec3d vec, boolean force) {
        if (force) {
            this.pointEffaPC.setAnim(1.0f);
            if (MathUtils.getDifferenceOf((double)this.pointXSmooth.getAnim(), vec.xCoord) > 16.0) {
                this.pointXSmooth.setAnim((float)vec.xCoord);
            }
            if (MathUtils.getDifferenceOf((double)this.pointYSmooth.getAnim(), vec.yCoord) > 16.0) {
                this.pointYSmooth.setAnim((float)vec.yCoord);
            }
            if (MathUtils.getDifferenceOf((double)this.pointZSmooth.getAnim(), vec.zCoord) > 16.0) {
                this.pointZSmooth.setAnim((float)vec.zCoord);
            }
        }
        for (int i = 0; i < 3; ++i) {
            if (!BlockUtils.blockMaterialIsCurrent(new BlockPos(this.pointXSmooth.getAnim(), this.pointYSmooth.getAnim(), this.pointZSmooth.getAnim()))) continue;
            this.pointYSmooth.setAnim(this.pointYSmooth.getAnim() + 1.0f);
        }
        if (force) {
            this.pointXSmooth.to = (float)vec.xCoord;
            this.pointYSmooth.to = (float)vec.yCoord;
            this.pointZSmooth.to = (float)vec.zCoord;
        }
    }

    void drawPointTP() {
        if ((double)this.pointEffaPC.getAnim() < 0.03) {
            return;
        }
        this.updatePointTP(null, false);
        RenderUtils.setup3dForBlockPos(() -> {
            int c = ColorUtils.swapAlpha(-1, 150.0f * this.pointEffaPC.getAnim() * this.stateAnim.getAnim());
            float x = this.pointXSmooth.getAnim();
            float y = this.pointYSmooth.getAnim();
            float z = this.pointZSmooth.getAnim();
            float w = this.getMe().width / 2.0f;
            float h = this.getMe().height;
            RenderUtils.drawGradientAlphaBox(new AxisAlignedBB(x - w, y, z - w, x + w, y + h, z + w), true, true, c, c);
            c = ColorUtils.swapAlpha(c, (float)ColorUtils.getAlphaFromColor(c) / 3.0f);
            RenderUtils.drawCanisterBox(new AxisAlignedBB(x - w, y, z - w, x + w, y + h, z + w), true, true, true, c, c, c);
        }, false);
    }

    private float[] getRotateToBlockPos(BlockPos pos) {
        return RotationUtil.getNeededFacing(new Vec3d((double)pos.getX() + 0.5, (float)pos.getY() + 1.5f, (double)pos.getZ() + 0.5), false, Minecraft.player, false);
    }

    private float[] getRotateToVec3d(Vec3d pos) {
        return RotationUtil.getNeededFacing(new Vec3d(pos.xCoord, pos.yCoord, pos.zCoord), true, Minecraft.player, false);
    }

    private boolean canRotate() {
        return this.actived && this.Rotations.getBool() && this.rotatePos() != null && this.getMe() != null;
    }

    private BlockPos rotatePos() {
        return forObsidianPos != null ? forObsidianPos : (forCrystalPos != null ? forCrystalPos : (crystal != null ? BlockUtils.getEntityBlockPos(crystal) : null));
    }

    private Vec3d getOverallVec3dOfVec3ds(Vec3d first, Vec3d second, float pc) {
        double dx = (second.xCoord - first.xCoord) * (double)pc;
        double dy = (second.yCoord - first.yCoord) * (double)pc;
        double dz = (second.zCoord - first.zCoord) * (double)pc;
        return first.addVector(dx, dy, dz);
    }

    private void rotateToBlockPos(EventPlayerMotionUpdate event, BlockPos pos, boolean silentMove) {
        if (this.getMe() != null && pos != null) {
            Vec3d toRot = new Vec3d(pos).addVector(0.5, 0.5, 0.5);
            RayTraceResult result = MathUtils.getPointed(new Vector2f(this.getRotateToVec3d(toRot)[0], this.getRotateToVec3d(toRot)[1]), 200.0, 1.0F, true);
            if (crystal != null && result != null && result.entityHit != crystal && !this.callRotateUpOnHitCrystal) {
                EnumFacing face = BlockUtils.getPlaceableSide(pos);
                toRot = this.getOverallVec3dOfVec3ds(crystal.getBestVec3dOnEntityBox(false), crystal.getPositionVector().addVector(0.0, 0.6, 0.0), 0.35F);
            } else if (forCrystalPos != null) {
                toRot = new Vec3d(forCrystalPos).addVector(0.5, 0.5, 0.5);
                if (!this.getMe().canEntityBeSeenVec3d(toRot)) {
                    toRot = toRot.addVector(0.0, 0.501, 0.0);
                }

                if (!this.getMe().canEntityBeSeenVec3d(toRot)) {
                    toRot = toRot.addVector(0.0, -0.501, 0.0);
                }

                if (this.callRotateUpOnHitCrystal) {
                    toRot = toRot.addVector(0.0, 1.501, 0.0);
                    this.callRotateUpOnHitCrystal = false;
                }
            } else if (pos == forObsidianPos) {
                EnumFacing face = BlockUtils.getPlaceableSide(pos);
                toRot = new Vec3d(pos)
                        .addVector(0.5, 0.5, 0.5)
                        .addVector((double)face.getFrontOffsetX() * 0.5, (double)face.getFrontOffsetY() * 0.5, (double)face.getFrontOffsetZ() * 0.5);
                this.callRotateUpOnHitCrystal = false;
            } else {
                this.callRotateUpOnHitCrystal = false;
            }

            this.lastRotatedVec = toRot;
            if (toRot != null) {
                this.lastRotatedVecNotNulled = this.lastRotatedVec;
            }

            float[] rotate = this.getRotateToVec3d(toRot);
            if (rotate != null) {
                event.setYaw(rotate[0]);
                event.setPitch(rotate[1]);
                this.getMe().rotationYawHead = rotate[0];
                this.getMe().renderYawOffset = rotate[0];
                this.getMe().rotationPitchHead = rotate[1];
                HitAura.get.rotations = rotate;
                boolean test = false;
                if (test) {
                    Minecraft.player.rotationYaw = rotate[0];
                    Minecraft.player.rotationPitch = rotate[1];
                }

                if (silentMove) {
                    this.callYawMoveYaw = rotate[0];
                    if (Minecraft.player.toCancelSprintTicks <= 1 && MathUtils.getDifferenceOf(Minecraft.player.rotationYaw, rotate[0]) >= 45.0) {
                        Minecraft.player.toCancelSprintTicks = 2;
                    }
                }
            }
        }
    }

    @EventTarget
    public void onSilentMoveStrafe(EventRotationStrafe event) {
        if (this.callYawMoveYaw != -1.2345679E8f) {
            event.setYaw(this.callYawMoveYaw);
        }
    }

    @EventTarget
    public void onSilentMoveJump(EventRotationJump event) {
        if (this.callYawMoveYaw != -1.2345679E8f) {
            event.setYaw(this.callYawMoveYaw);
        }
    }

    @EventTarget
    public void onMovementInput(EventMovementInput event) {
        if (this.callYawMoveYaw != -1.2345679E8f) {
            MoveMeHelp.fixDirMove(event, this.callYawMoveYaw);
        }
    }

    @EventTarget
    public void onPreUpds(EventPlayerMotionUpdate event) {
        if (this.canRotate()) {
            this.rotateToBlockPos(event, this.rotatePos(), this.RotateMoveSide.getBool());
        } else if (this.callYawMoveYaw != -1.2345679E8f) {
            this.callYawMoveYaw = -1.2345679E8f;
        }
    }

    static {
        forCrystalPos = null;
        forObsidianPos = null;
        crystal = null;
        targetezs = new ArrayList<EntityLivingBase>();
        hasActionToCPSHint = false;
    }

    private class PopEffect {
        long time = System.currentTimeMillis();
        float maxTime;
        Vec3d to;

        PopEffect(float maxTime, Vec3d to) {
            this.maxTime = maxTime;
            this.to = to;
        }

        private final float getDeltaTime() {
            return (float)(System.currentTimeMillis() - this.time) / this.maxTime;
        }

        Vec3d getPos() {
            return this.to;
        }
    }
}

