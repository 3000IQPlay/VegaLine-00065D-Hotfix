package ru.govno.client.module.modules;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiScreenResourcePacks;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemAir;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemElytra;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemShield;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraft.network.play.client.CPacketClientSettings;
import net.minecraft.network.play.client.CPacketCloseWindow;
import net.minecraft.network.play.client.CPacketConfirmTransaction;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerAbilities;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.client.CPacketResourcePackStatus;
import net.minecraft.network.play.client.CPacketTabComplete;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.network.play.server.SPacketCloseWindow;
import net.minecraft.network.play.server.SPacketEntityStatus;
import net.minecraft.network.play.server.SPacketOpenWindow;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.network.play.server.SPacketResourcePackSend;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldType;
import org.apache.commons.lang3.RandomStringUtils;
import org.lwjgl.input.Mouse;
import ru.govno.client.Client;
import ru.govno.client.clickgui.ClickGuiScreen;
import ru.govno.client.event.EventTarget;
import ru.govno.client.event.events.EventMove2;
import ru.govno.client.event.events.EventMovementInput;
import ru.govno.client.event.events.EventPlayerMotionUpdate;
import ru.govno.client.event.events.EventReceivePacket;
import ru.govno.client.event.events.EventRotationJump;
import ru.govno.client.event.events.EventRotationStrafe;
import ru.govno.client.event.events.EventSendPacket;
import ru.govno.client.module.Module;
import ru.govno.client.module.modules.CommandGui;
import ru.govno.client.module.modules.ElytraBoost;
import ru.govno.client.module.modules.HitAura;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.utils.Combat.RotationUtil;
import ru.govno.client.utils.IntaveDisabler;
import ru.govno.client.utils.Math.MathUtils;
import ru.govno.client.utils.Math.TimerHelper;
import ru.govno.client.utils.Movement.MoveMeHelp;

public class Bypass
extends Module {
    public static Bypass get;
    public BoolSettings NCPWinclick;
    public BoolSettings AACWinclick;
    public BoolSettings GrimWinclick;
    public BoolSettings VulcanStrafe;
    public BoolSettings VulcanLiquid;
    public BoolSettings MatrixElySpoofs;
    public BoolSettings NoServerGround;
    public BoolSettings NoServerRotate;
    public BoolSettings CloseScreens;
    public BoolSettings FixPearlFlag;
    public BoolSettings InvPopHitFix;
    public BoolSettings RegionUsingItem;
    public BoolSettings FixSettingsKick;
    public BoolSettings FixStackFlags;
    public BoolSettings FixShieldCooldown;
    public BoolSettings SpawnGodmode;
    public BoolSettings ClientSpoof;
    public BoolSettings NoServerPack;
    public BoolSettings FullyVolume;
    public BoolSettings IntaveMovement;
    public BoolSettings LegitScreenshot;
    public BoolSettings GMFlySpoofIfCan;
    public BoolSettings NCPMovement;
    public BoolSettings ElytraClip;
    public BoolSettings BreakingClip;
    public BoolSettings AntiHunger;
    public BoolSettings AntiAfk;
    private final Random RANDOM = new Random();
    private final TimerHelper afkTime = TimerHelper.TimerHelperReseted();
    public static boolean callSneak;
    private static final boolean[] oldPreseds;
    private static final TimerHelper timeAfterSneak;
    public int flagCPS;
    public int flagReduceTicks;
    boolean strafeHacked = false;
    Vec3d lastVecNote = Vec3d.ZERO;
    boolean vulcanStatusHacked;
    boolean noted;
    public boolean doReduceDestack;
    public static boolean hackVolume;
    boolean usingStartPlob;
    private final TimerHelper openContainerOutTime = new TimerHelper();
    float sYaw;
    float sPitch;
    boolean callSRS;
    double gdX;
    double gdY;
    double gdZ;
    float gdYaw;
    float gdPitch;
    private float tempYaw;
    private float tempPitch;
    private int dodgeFromZero;
    public boolean ncpDisabledLocalBool;

    public Bypass() {
        super("Bypass", 0, Module.Category.MISC);
        this.NCPWinclick = new BoolSettings("NCPWinclick", false, this);
        this.settings.add(this.NCPWinclick);
        this.AACWinclick = new BoolSettings("AACWinclick", false, this);
        this.settings.add(this.AACWinclick);
        this.GrimWinclick = new BoolSettings("GrimWinclick", false, this);
        this.settings.add(this.GrimWinclick);
        this.VulcanStrafe = new BoolSettings("VulcanStrafe", false, this);
        this.settings.add(this.VulcanStrafe);
        this.VulcanLiquid = new BoolSettings("VulcanLiquid", false, this);
        this.settings.add(this.VulcanLiquid);
        this.MatrixElySpoofs = new BoolSettings("MatrixElySpoofs", false, this);
        this.settings.add(this.MatrixElySpoofs);
        this.NoServerGround = new BoolSettings("NoServerGround", false, this);
        this.settings.add(this.NoServerGround);
        this.NoServerRotate = new BoolSettings("NoServerRotate", true, this);
        this.settings.add(this.NoServerRotate);
        this.CloseScreens = new BoolSettings("CloseScreens", true, this);
        this.settings.add(this.CloseScreens);
        this.FixPearlFlag = new BoolSettings("FixPearlFlag", false, this);
        this.settings.add(this.FixPearlFlag);
        this.InvPopHitFix = new BoolSettings("InvPopHitFix", false, this);
        this.settings.add(this.InvPopHitFix);
        this.RegionUsingItem = new BoolSettings("RegionUsingItem", false, this);
        this.settings.add(this.RegionUsingItem);
        this.FixSettingsKick = new BoolSettings("FixSettingsKick", true, this);
        this.settings.add(this.FixSettingsKick);
        this.FixStackFlags = new BoolSettings("FixStackFlags", false, this);
        this.settings.add(this.FixStackFlags);
        this.FixShieldCooldown = new BoolSettings("FixShieldCooldown", false, this);
        this.settings.add(this.FixShieldCooldown);
        this.SpawnGodmode = new BoolSettings("SpawnGodmode", false, this);
        this.settings.add(this.SpawnGodmode);
        this.ClientSpoof = new BoolSettings("ClientSpoof", false, this);
        this.settings.add(this.ClientSpoof);
        this.NoServerPack = new BoolSettings("NoServerPack", true, this);
        this.settings.add(this.NoServerPack);
        this.FullyVolume = new BoolSettings("FullyVolume", true, this);
        this.settings.add(this.FullyVolume);
        this.IntaveMovement = new BoolSettings("IntaveMovement", false, this);
        this.settings.add(this.IntaveMovement);
        this.LegitScreenshot = new BoolSettings("LegitScreenshot", false, this);
        this.settings.add(this.LegitScreenshot);
        this.GMFlySpoofIfCan = new BoolSettings("GMFlySpoofIfCan", false, this);
        this.settings.add(this.GMFlySpoofIfCan);
        this.NCPMovement = new BoolSettings("NCPMovement", false, this);
        this.settings.add(this.NCPMovement);
        this.ElytraClip = new BoolSettings("ElytraClip", true, this);
        this.settings.add(this.ElytraClip);
        this.BreakingClip = new BoolSettings("BreakingClip", false, this);
        this.settings.add(this.BreakingClip);
        this.AntiHunger = new BoolSettings("AntiHunger", false, this);
        this.settings.add(this.AntiHunger);
        this.AntiAfk = new BoolSettings("AntiAfk", true, this);
        this.settings.add(this.AntiAfk);
        get = this;
    }

    public static boolean isCancelInvWalk() {
        return get.isActived() && Bypass.get.GrimWinclick.getBool() && callSneak;
    }

    public static boolean onWinClick() {
        if (Bypass.get.actived && Bypass.get.GrimWinclick.getBool()) {
            timeAfterSneak.reset();
            callSneak = true;
            Bypass.oldPreseds[0] = Bypass.mc.gameSettings.keyBindForward.isKeyDown();
            Bypass.oldPreseds[1] = Bypass.mc.gameSettings.keyBindRight.isKeyDown();
            Bypass.oldPreseds[2] = Bypass.mc.gameSettings.keyBindLeft.isKeyDown();
            Bypass.oldPreseds[3] = Bypass.mc.gameSettings.keyBindBack.isKeyDown();
            Bypass.oldPreseds[5] = Bypass.mc.gameSettings.keyBindSneak.isKeyDown();
            return true;
        }
        return false;
    }

    public boolean isAACWinClick() {
        return this.isActived() && this.AACWinclick.getBool();
    }

    private void sendPacket(Packet packet) {
        mc.getConnection().sendPacket(packet);
    }

    public void setStrafeHacked(boolean hack) {
        this.strafeHacked = hack;
    }

    public boolean getIsStrafeHacked() {
        return this.strafeHacked || !this.actived || !this.VulcanStrafe.getBool();
    }

    public boolean canWinClickEdit() {
        return this.actived && this.NCPWinclick.getBool();
    }

    public boolean rayTrace(Entity me, double x, double y, double z) {
        return Bypass.mc.world.rayTraceBlocks(new Vec3d(me.posX, me.posY, me.posZ), new Vec3d(x, y, z), false, true, false) == null || Bypass.mc.world.rayTraceBlocks(new Vec3d(me.posX, me.posY + 1.0, me.posZ), new Vec3d(x, y + 1.0, z), false, true, false) == null;
    }

    public boolean statusVulcanDisabler() {
        return !this.actived || this.vulcanStatusHacked;
    }

    private final BlockPos waterNeared() {
        float r = 5.0f;
        float min = 4.5f;
        float max = 5.5f;
        for (float x = -5.0f; x < 5.0f; x += 1.0f) {
            for (float y = -5.0f; y < 5.0f; y += 1.0f) {
                for (float z = -5.0f; z < 5.0f; z += 1.0f) {
                    BlockPos pos = new BlockPos((float)((int)Minecraft.player.posX) + x + 0.5f, (float)((int)Minecraft.player.posY) + y, (float)((int)Minecraft.player.posZ) + z + 0.5f);
                    if (pos == null || Bypass.mc.world.getBlockState(pos).getBlock() != Blocks.WATER || Bypass.mc.world.getBlockState(pos.up()).getBlock() != Blocks.WATER && Bypass.mc.world.getBlockState(pos.up()).getBlock() != Blocks.AIR || Bypass.mc.world.getBlockState(pos.up().up()).getBlock() != Blocks.WATER && Bypass.mc.world.getBlockState(pos.up().up()).getBlock() != Blocks.AIR) continue;
                    Vec3d vec3d = new Vec3d(Minecraft.player.posX + (double)x + 0.5, Minecraft.player.posY + (double)y, Minecraft.player.posZ + (double)z + 0.5);
                    if (!(Minecraft.player.getDistanceToVec3d(vec3d) > 4.5)) continue;
                    Vec3d vec3d2 = new Vec3d(Minecraft.player.posX + (double)x + 0.5, Minecraft.player.posY + (double)y, Minecraft.player.posZ + (double)z + 0.5);
                    if (!(Minecraft.player.getDistanceToVec3d(vec3d2) < 5.5) || !this.rayTrace(Minecraft.player, pos.getX(), pos.getY(), pos.getZ()) || Bypass.mc.world.getBlockState(pos.up()).getBlock() != Blocks.AIR || Bypass.mc.world.getBlockState(pos.down()).getBlock() == Blocks.WATER) continue;
                    return pos;
                }
            }
        }
        return null;
    }

    private void note() {
        float x = (float)this.waterNeared().getX() + 0.5f;
        float y = (float)this.waterNeared().getY() + 0.2f;
        float z = (float)this.waterNeared().getZ() + 0.5f;
        if (Minecraft.player.fallDistance == 0.0f || (double)Minecraft.player.fallDistance < 0.4 || MoveMeHelp.getSpeed() > 0.1) {
            return;
        }
        this.lastVecNote = new Vec3d(x, y, z);
        Minecraft.player.connection.preSendPacket(new CPacketPlayer.Position(x, (double)y + 0.19, z, true));
        Client.msg("\u00a7f\u00a7lModules:\u00a7r \u00a77[\u00a7lDisabler\u00a7r\u00a77]: \u043f\u044b\u0442\u0430\u044e\u0441\u044c \u0432\u044b\u043a\u043b\u044e\u0447\u0438\u0442\u044c Vulcan.", false);
        this.noted = true;
    }

    public double getDistanceAtVec3dToVec3d(Vec3d first, Vec3d second) {
        double xDiff = first.xCoord - second.xCoord;
        double yDiff = first.yCoord - second.yCoord;
        double zDiff = first.zCoord - second.zCoord;
        return Math.sqrt(xDiff * xDiff + yDiff * yDiff + zDiff * zDiff);
    }

    public Vec3d getEntityVecPosition(Entity entityIn) {
        return new Vec3d(entityIn.posX, entityIn.posY, entityIn.posZ);
    }

    @EventTarget
    public void onMovementState(EventMove2 event) {
        if (!this.doReduceDestack) {
            return;
        }
        event.motion().xCoord = 0.0;
        event.motion().zCoord = 0.0;
    }

    void reduceStackFlags() {
        if (this.doReduceDestack) {
            if (this.flagReduceTicks > 0) {
                Minecraft.player.motionX = 0.0;
                Minecraft.player.motionY = 0.0;
                Minecraft.player.motionZ = 0.0;
                Entity.motionx = 1.0E-45;
                Entity.motiony = 1.0E-45;
                Entity.motionz = 1.0E-45;
                Minecraft.player.jumpMovementFactor = 0.0f;
                Minecraft.player.rotationYaw += (float)(Math.random() * 0.01 - 0.005);
                Minecraft.player.rotationPitch -= (float)(Math.random() * 0.01 - 0.005);
                --this.flagReduceTicks;
            } else {
                this.doReduceDestack = false;
            }
        } else if (this.flagReduceTicks != 4) {
            this.flagReduceTicks = 4;
        }
    }

    private void doRandomPlayerAction() {
        switch (this.RANDOM.nextInt(0, 4)) {
            case 0: {
                Minecraft.player.addVelocity(this.RANDOM.nextFloat(-0.02f, 0.02f), 0.0, this.RANDOM.nextFloat(-0.02f, 0.02f));
                break;
            }
            case 1: {
                mc.getConnection().sendPacket(new CPacketAnimation(EnumHand.OFF_HAND));
                break;
            }
            case 2: {
                mc.getConnection().sendPacket(new CPacketChatMessage("/" + RandomStringUtils.randomAlphabetic((int)5)));
                break;
            }
            case 3: {
                Minecraft.player.rotationYaw += this.RANDOM.nextFloat(-0.1f, 0.1f);
            }
        }
    }

    @Override
    public void onUpdate() {
        boolean samiNotAfk;
        if (this.AntiAfk.getBool() && this.afkTime.hasReached(40000.0)) {
            this.doRandomPlayerAction();
        }
        boolean bl = samiNotAfk = Minecraft.player.rotationYaw - Minecraft.player.prevRotationYaw != 0.0f || Minecraft.player.rotationPitch - Minecraft.player.prevRotationPitch != 0.0f || Minecraft.player.movementInput.jump || Minecraft.player.movementInput.sneak || Minecraft.player.movementInput.forwardKeyDown || Minecraft.player.movementInput.backKeyDown || Minecraft.player.movementInput.rightKeyDown || Minecraft.player.movementInput.leftKeyDown;
        if (samiNotAfk || Minecraft.player.isSwingInProgress) {
            this.afkTime.reset();
        }
        if (this.dodgeFromZero > 0) {
            --this.dodgeFromZero;
        }
        IntaveDisabler.updateIntaveDisablerState(this.isActived() && this.IntaveMovement.getBool());
        if (this.GrimWinclick.getBool() && callSneak) {
            if (timeAfterSneak.hasReached(50.0)) {
                callSneak = false;
                Bypass.mc.gameSettings.keyBindForward.pressed = oldPreseds[0];
                Bypass.mc.gameSettings.keyBindRight.pressed = oldPreseds[1];
                Bypass.mc.gameSettings.keyBindLeft.pressed = oldPreseds[2];
                Bypass.mc.gameSettings.keyBindBack.pressed = oldPreseds[3];
                Bypass.mc.gameSettings.keyBindSneak.pressed = oldPreseds[5];
            } else {
                boolean ticked = Minecraft.player.ticksExisted % 2 != 0;
                Bypass.mc.gameSettings.keyBindForward.pressed = false;
                Bypass.mc.gameSettings.keyBindRight.pressed = false;
                Bypass.mc.gameSettings.keyBindLeft.pressed = false;
                Bypass.mc.gameSettings.keyBindBack.pressed = false;
                Bypass.mc.gameSettings.keyBindSneak.pressed = false;
            }
        }
        hackVolume = this.FullyVolume.getBool();
        if (this.SpawnGodmode.getBool() && Bypass.mc.world.playerEntities.size() <= 2) {
            if (this.gdX != 0.0 || this.gdY != 0.0 || this.gdZ != 0.0) {
                Minecraft.player.setPosition(this.gdX, this.gdY, this.gdZ);
                Minecraft.player.multiplyMotionXZ(0.0f);
            }
            if (this.gdYaw != 0.0f || this.gdPitch != 0.0f) {
                Minecraft.player.rotationYawHead = this.gdYaw;
                Minecraft.player.renderYawOffset = this.gdYaw;
                Minecraft.player.rotationPitchHead = this.gdPitch;
            }
        }
        if (this.FixStackFlags.getBool()) {
            if (this.flagCPS > 2) {
                this.doReduceDestack = true;
                this.flagCPS = 0;
            }
            this.reduceStackFlags();
            if (this.flagCPS > 0 && Minecraft.player.ticksExisted % 5 == 0) {
                --this.flagCPS;
            }
        }
        if (this.VulcanLiquid.getBool()) {
            if (Minecraft.player.ticksExisted == 1) {
                this.noted = false;
                this.strafeHacked = false;
            }
            if (Minecraft.player.ticksExisted > 30 && !this.noted && !this.strafeHacked && this.waterNeared() != null) {
                this.note();
            } else if (!(this.noted || Minecraft.player.ticksExisted != 5 || Bypass.mc.world == null || Bypass.mc.world != null && Bypass.mc.world.getWorldType() == WorldType.FLAT)) {
                Client.msg("\u00a7f\u00a7lModules:\u00a7r \u00a77[\u00a7lDisabler\u00a7r\u00a77]: \u043f\u043e\u0434\u043e\u0439\u0434\u0438 \u043a \u0432\u043e\u0434\u0435.", false);
            }
        } else if (this.noted || this.strafeHacked) {
            this.noted = false;
            this.strafeHacked = false;
        }
        if (this.VulcanStrafe.getBool()) {
            if (Minecraft.player.ticksExisted % 11 == 7) {
                this.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, BlockPos.ORIGIN.down(61), Minecraft.player.getHorizontalFacing().getOpposite()));
            }
            this.setStrafeHacked(!(Minecraft.player.ticksExisted <= 8 || Bypass.mc.playerController.isHittingBlock && Bypass.mc.playerController.curBlockDamageMP > 0.0f));
        }
        if (!(!this.MatrixElySpoofs.getBool() || Minecraft.player.ticksExisted % 4 != 0 || ElytraBoost.get.actived && ElytraBoost.canElytra())) {
            this.sendPacket(new CPacketEntityAction(Minecraft.player, CPacketEntityAction.Action.START_FALL_FLYING));
        }
    }

    public void onAttack(boolean preSendHit) {
        if (this.GMFlySpoofIfCan.getBool() && Minecraft.player.capabilities.allowFlying) {
            if (preSendHit) {
                boolean allow = Minecraft.player.capabilities.allowFlying;
                Minecraft.player.capabilities.allowFlying = false;
                mc.getConnection().preSendPacket(new CPacketPlayerAbilities(Minecraft.player.capabilities));
                Minecraft.player.capabilities.allowFlying = allow;
                if (!Minecraft.player.onGround) {
                    mc.getConnection().preSendPacket(new CPacketPlayer.Position(Minecraft.player.posX, Minecraft.player.posY + MathUtils.clamp(Minecraft.player.motionY, -0.08, -0.002), Minecraft.player.posZ, false));
                }
            } else {
                mc.getConnection().preSendPacket(new CPacketPlayerAbilities(Minecraft.player.capabilities));
            }
        }
    }

    @EventTarget
    public void onSending(EventSendPacket event) {
        CPacketPlayer packet;
        CPacketUseEntity use;
        Object activeStack;
        Packet trans;
        if (!this.actived) {
            return;
        }
        Packet packet2 = event.getPacket();
        if (packet2 instanceof CPacketEntityAction) {
            CPacketEntityAction action = (CPacketEntityAction)packet2;
            if (this.AntiHunger.getBool() && (action.getAction() == CPacketEntityAction.Action.START_SPRINTING || action.getAction() == CPacketEntityAction.Action.STOP_SPRINTING)) {
                event.cancel();
            }
        }
        if ((packet2 = event.getPacket()) instanceof CPacketPlayerAbilities) {
            CPacketPlayerAbilities abilities = (CPacketPlayerAbilities)packet2;
            if (this.GMFlySpoofIfCan.getBool() && abilities.allowFlying) {
                abilities.flying = true;
            }
        }
        IntaveDisabler.onSendingPackets(event);
        if ((event.getPacket() instanceof CPacketPlayer || event.getPacket() instanceof CPacketConfirmTransaction) && this.SpawnGodmode.getBool()) {
            boolean nolo = Bypass.mc.world.playerEntities.size() <= 2;
            Packet packet3 = event.getPacket();
            if (packet3 instanceof CPacketConfirmTransaction && ((CPacketConfirmTransaction)(trans = (CPacketConfirmTransaction)packet3)).getWindowId() != 0) {
                nolo = false;
            }
            if (nolo) {
                event.cancel();
            }
        }
        if (!this.usingStartPlob && event.getPacket() instanceof CPacketPlayerTryUseItem && this.RegionUsingItem.getBool()) {
            boolean bl = this.usingStartPlob = !(Minecraft.player.getHeldItemMainhand().getItem() instanceof ItemBlock) && !(Minecraft.player.getHeldItemOffhand().getItem() instanceof ItemBlock);
        }
        if ((trans = event.getPacket()) instanceof CPacketPlayerTryUseItem) {
            CPacketPlayerTryUseItem cPacketTryUse = (CPacketPlayerTryUseItem)trans;
            if (this.RegionUsingItem.getBool() && Minecraft.player != null) {
                float[] calc;
                activeStack = Minecraft.player.getHeldItem(cPacketTryUse.getHand());
                Item itemInStack = ((ItemStack)activeStack).getItem();
                if (this.dodgeFromZero == 0 && (itemInStack instanceof ItemShield || itemInStack instanceof ItemFood || itemInStack instanceof ItemBow) && (calc = this.calcClearRayTraceRotate())[0] != Minecraft.player.rotationYaw && calc[0] != Minecraft.player.rotationPitch) {
                    this.dodgeFromZero = 4;
                    this.tempYaw = calc[0];
                    this.tempPitch = calc[1];
                }
            }
        }
        if (this.usingStartPlob && Minecraft.player != null && (event.getPacket() instanceof CPacketPlayerTryUseItemOnBlock || (activeStack = event.getPacket()) instanceof CPacketUseEntity && (use = (CPacketUseEntity)activeStack).getAction() != CPacketUseEntity.Action.ATTACK || Minecraft.player.getHeldItemMainhand().getItem() instanceof ItemBlock || Minecraft.player.getHeldItemOffhand().getItem() instanceof ItemBlock)) {
            if (!(Minecraft.player.getHeldItemMainhand().getItem() instanceof ItemBlock) && !(Minecraft.player.getHeldItemOffhand().getItem() instanceof ItemBlock)) {
                event.cancel();
            }
            this.usingStartPlob = false;
        }
        if (Bypass.mc.world != null && Bypass.mc.world.getWorldType() == WorldType.DEFAULT && (activeStack = event.getPacket()) instanceof CPacketUseEntity) {
            EntityLivingBase base;
            Entity used;
            CPacketUseEntity useE = (CPacketUseEntity)activeStack;
            if (this.InvPopHitFix.getBool() && useE.getAction() == CPacketUseEntity.Action.ATTACK && (used = useE.getEntityFromWorld(Bypass.mc.world)) != null && used instanceof EntityLivingBase && (base = (EntityLivingBase)used).isEntityAlive() && (Minecraft.player.openContainer == null || Minecraft.player.openContainer instanceof ContainerPlayer) && !(Bypass.mc.currentScreen instanceof GuiInventory) && this.openContainerOutTime.hasReached(HitAura.get.msCooldown() * 4.0f)) {
                Minecraft.player.connection.preSendPacket(new CPacketCloseWindow(0));
                this.openContainerOutTime.reset();
            }
        }
        if ((packet2 = event.getPacket()) instanceof CPacketPlayer && (packet = (CPacketPlayer)packet2).isOnGround() && this.NoServerGround.getBool()) {
            packet.onGround = false;
        }
        if ((packet2 = event.getPacket()) instanceof CPacketClientSettings) {
            CPacketClientSettings packet4 = (CPacketClientSettings)packet2;
            if (Minecraft.player != null && Minecraft.player.ticksExisted > 250 && Minecraft.player.ticksExisted % 5 != 1 && this.FixSettingsKick.getBool()) {
                event.cancel();
            }
        }
    }

    public boolean canCancelServerRots() {
        return this.actived && this.NoServerRotate.getBool() && !this.doReduceDestack;
    }

    public void callServerRotsSpoof(float sYaw1, float sPitch1) {
        this.sYaw = sYaw1;
        this.sPitch = sPitch1;
        this.callSRS = true;
    }

    public boolean canFixPearlFlag() {
        return this.actived && this.FixPearlFlag.getBool();
    }

    @EventTarget
    public void onSend(EventSendPacket event) {
        block4: {
            CPacketPlayer packet;
            block6: {
                block5: {
                    Packet packet2;
                    if (!this.callSRS || !((packet2 = event.getPacket()) instanceof CPacketPlayer)) break block4;
                    packet = (CPacketPlayer)packet2;
                    if (!(packet instanceof CPacketPlayer.Rotation)) break block5;
                    CPacketPlayer.Rotation fPacket = (CPacketPlayer.Rotation)packet;
                    break block6;
                }
                if (!(packet instanceof CPacketPlayer.PositionRotation)) break block4;
                CPacketPlayer.PositionRotation positionRotation = (CPacketPlayer.PositionRotation)packet;
            }
            if (this.sYaw != 0.0f || this.sPitch != 0.0f) {
                packet.setRotation(this.sYaw, this.sPitch);
                this.callSRS = false;
            }
        }
    }

    @EventTarget
    public void onReceive(EventReceivePacket event) {
        Object openned;
        Object packetVecFlag;
        Object SP;
        SPacketEntityStatus status;
        Object object;
        SPacketPlayerPosLook look;
        Packet packet;
        if (!this.actived) {
            return;
        }
        if (this.AntiAfk.getBool() && (event.getPacket() instanceof CPacketChatMessage || event.getPacket() instanceof CPacketTabComplete || event.getPacket() instanceof CPacketPlayerTryUseItem || event.getPacket() instanceof CPacketPlayerTryUseItemOnBlock || event.getPacket() instanceof CPacketEntityAction)) {
            this.afkTime.reset();
        }
        IntaveDisabler.onReceivePackets(event);
        if (event.getPacket() instanceof SPacketResourcePackSend && this.NoServerPack.getBool()) {
            Minecraft.player.connection.sendPacket(new CPacketResourcePackStatus(CPacketResourcePackStatus.Action.ACCEPTED));
            Minecraft.player.connection.sendPacket(new CPacketResourcePackStatus(CPacketResourcePackStatus.Action.SUCCESSFULLY_LOADED));
            event.setCancelled(true);
            Client.msg("\u00a7f\u00a7lModules:\u00a7r \u00a77[\u00a7lBypass\u00a7r\u00a77]: \u0421\u0435\u0440\u0432\u0435\u0440 \u043f\u0440\u043e\u0441\u0438\u0442 \u0443\u0441\u0442\u0430\u043d\u043e\u0432\u043a\u0443 \u043f\u0430\u043a\u0435\u0442\u0430", false);
            Client.msg("\u00a77\u043e\u0442\u0432\u0435\u0442 \u0437\u0430\u043f\u0440\u043e\u0441 \u0431\u044b\u043b \u043f\u043e\u0434\u043c\u0435\u043d\u0435\u043d \u043d\u0430 \u043b\u043e\u0436\u043d\u044b\u0439", false);
            Client.msg("\u00a77\u043d\u0435\u043e\u0431\u0445\u043e\u0434\u0438\u043c\u043e\u0441\u0442\u044c \u0443\u0441\u0442\u0430\u043d\u043e\u0432\u043a\u0438 \u0431\u044b\u043b\u0430 \u0438\u0433\u043d\u043e\u0440\u0438\u0440\u043e\u0432\u0430\u043d\u0430 \u0438\u0433\u0440\u043e\u0439.", false);
        }
        if ((packet = event.getPacket()) instanceof SPacketPlayerPosLook) {
            look = (SPacketPlayerPosLook)packet;
            if (this.SpawnGodmode.getBool()) {
                this.gdX = look.getX();
                this.gdY = look.getY();
                this.gdZ = look.getZ();
                this.gdYaw = look.getYaw();
                this.gdPitch = look.getPitch();
            }
        }
        if ((object = event.getPacket()) instanceof SPacketEntityStatus && (status = (SPacketEntityStatus)object) != null && Bypass.mc.world != null && status.getEntity(Bypass.mc.world) != null && (object = status.getEntity(Bypass.mc.world)) instanceof EntityPlayerSP) {
            SP = (EntityPlayerSP)object;
            if (this.FixShieldCooldown.getBool() && status.getOpCode() == 30 && ((EntityLivingBase)SP).isBlocking()) {
                ((EntityPlayer)SP).getCooldownTracker().setCooldown(Items.SHIELD, 100);
            }
        }
        if (this.FixStackFlags.getBool() && (SP = event.getPacket()) instanceof SPacketPlayerPosLook) {
            look = (SPacketPlayerPosLook)SP;
            if (Minecraft.player != null && Minecraft.player.getDistance(look.getX(), look.getY(), look.getZ()) < 2.0) {
                ++this.flagCPS;
            }
        }
        if ((SP = event.getPacket()) instanceof SPacketPlayerPosLook) {
            look = (SPacketPlayerPosLook)SP;
            if (this.actived && this.VulcanLiquid.getBool()) {
                packetVecFlag = new Vec3d(look.x, look.y, look.z);
                Vec3d badVec = this.lastVecNote;
                if (this.getDistanceAtVec3dToVec3d((Vec3d)packetVecFlag, badVec) < 0.2 && this.getDistanceAtVec3dToVec3d((Vec3d)packetVecFlag, this.getEntityVecPosition(Minecraft.player)) > 0.1) {
                    this.noted = false;
                    Minecraft.player.ticksExisted = 0;
                    this.strafeHacked = false;
                    Client.msg("\u00a7f\u00a7lModules:\u00a7r \u00a77[\u00a7lBypass\u00a7r\u00a77]: \u0432\u044b\u043a\u043b\u044e\u0447\u0438\u0442\u044c Vulcan \u043d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c.", false);
                    Client.msg("\u00a7f\u00a7lModules:\u00a7r \u00a77[\u00a7lBypass\u00a7r\u00a77]: \u043f\u043e\u043f\u044b\u0442\u0430\u044e\u0441\u044c \u0435\u0449\u0451 \u0440\u0430\u0437.", false);
                } else if (this.noted && !this.strafeHacked) {
                    this.strafeHacked = true;
                    Client.msg("\u00a7f\u00a7lModules:\u00a7r \u00a77[\u00a7lBypass\u00a7r\u00a77]: \u0430\u043d\u0442\u0438\u0447\u0438\u0442 Vulcan \u0432\u044b\u043a\u043b\u044e\u0447\u0435\u043d.", false);
                }
            }
        }
        if ((packetVecFlag = event.getPacket()) instanceof SPacketOpenWindow) {
            SPacketOpenWindow open = (SPacketOpenWindow)packetVecFlag;
            if (Minecraft.player != null && this.CloseScreens.getBool() && (openned = Bypass.mc.currentScreen) != null && (openned instanceof GuiChat && !CommandGui.isHoveredToPanel(false) && (!open.getGuiId().endsWith("container") || Bypass.mc.ingameGUI.getChatGUI().getChatComponent(Mouse.getX(), Mouse.getY()) == null) || openned instanceof ClickGuiScreen || openned instanceof GuiInventory)) {
                this.sendPacket(new CPacketCloseWindow(open.getWindowId()));
                event.setCancelled(true);
            }
        }
        if ((openned = event.getPacket()) instanceof SPacketCloseWindow) {
            SPacketCloseWindow close = (SPacketCloseWindow)openned;
            if (Minecraft.player != null && this.CloseScreens.getBool() && (openned = Bypass.mc.currentScreen) != null && (openned instanceof GuiChat && !CommandGui.isHoveredToPanel(false) || openned instanceof ClickGuiScreen || openned instanceof GuiInventory || openned instanceof GuiOptions || openned instanceof GuiScreenResourcePacks || openned instanceof GuiIngameMenu)) {
                event.setCancelled(true);
            }
        }
    }

    @Override
    public void onToggled(boolean actived) {
        if (actived) {
            this.doReduceDestack = false;
            this.flagCPS = 0;
            this.flagReduceTicks = 0;
        } else {
            this.doReduceDestack = false;
            this.flagCPS = 0;
            this.flagReduceTicks = 0;
            this.setStrafeHacked(false);
        }
        this.afkTime.reset();
        IntaveDisabler.resetDisabler();
        hackVolume = false;
        super.onToggled(actived);
    }

    private float[] calcClearRayTraceRotate() {
        int countOffset = 4;
        float prevPitch = Minecraft.player.rotationPitch;
        float prevYaw = Minecraft.player.rotationYaw;
        float pTicks = mc.getRenderPartialTicks();
        float pitch = prevPitch;
        float yaw = prevYaw;
        ArrayList<Integer> yaws = new ArrayList<Integer>();
        ArrayList<Integer> pitches = new ArrayList<Integer>();
        for (int yaw1 = 0; yaw1 < 360; yaw1 += 3) {
            if (yaw1 + (int)yaw <= 360) {
                yaws.add(yaw1 + (int)yaw);
            }
            if (-yaw1 + (int)yaw < 0) continue;
            yaws.add(-yaw1 + (int)yaw);
        }
        for (int pitch1 = 0; pitch1 < 180; pitch1 += 3) {
            if (pitch1 + (int)pitch <= 90) {
                pitches.add(pitch1 + (int)pitch);
            }
            if (-pitch1 + (int)pitch < -90) continue;
            pitches.add(-pitch1 + (int)pitch);
        }
        boolean doBreak = false;
        Bypass.mc.playerController.setBlockReachDistances(5.5f, 5.5f);
        Bypass.mc.entityRenderer.getMouseOver(1.0f);
        RayTraceResult ray = Bypass.mc.objectMouseOver;
        if (ray != null && ray.entityHit == null && ray.getBlockPos() != null && Bypass.mc.world.isAirBlock(ray.getBlockPos())) {
            return new float[]{yaw, pitch};
        }
        Iterator iterator = yaws.iterator();
        while (iterator.hasNext()) {
            int numYaw = (Integer)iterator.next();
            int counter = countOffset;
            Minecraft.player.rotationYaw = numYaw;
            Iterator iterator2 = pitches.iterator();
            while (iterator2.hasNext()) {
                int numPitch = (Integer)iterator2.next();
                Minecraft.player.rotationPitch = numPitch;
                Bypass.mc.entityRenderer.getMouseOver(1.0f);
                ray = Bypass.mc.objectMouseOver;
                if (ray != null && ray.entityHit == null && ray.getBlockPos() != null && Bypass.mc.world.isAirBlock(ray.getBlockPos())) {
                    --counter;
                }
                if (counter != 0) continue;
                pitch = numPitch;
                yaw = numYaw;
                doBreak = true;
                break;
            }
            if (!doBreak) continue;
            break;
        }
        Minecraft.player.rotationPitch = prevPitch;
        Bypass.mc.playerController.setBlockReachDistances(5.0f, 4.5f);
        Bypass.mc.entityRenderer.getMouseOver(pTicks);
        Minecraft.player.rotationYaw = prevYaw;
        return new float[]{yaw, pitch};
    }

    @EventTarget
    public void onUpdate(EventPlayerMotionUpdate event) {
        if (this.actived && this.NCPMovement.getBool() && ElytraBoost.canElytra()) {
            double x = Minecraft.player.posX;
            double y = Minecraft.player.posY;
            double z = Minecraft.player.posZ;
            boolean fl = true;
            if (!(Minecraft.player.inventory.armorItemInSlot(2).getItem() instanceof ItemElytra)) {
                if (Minecraft.player.inventory.armorItemInSlot(2).getItem() instanceof ItemAir) {
                    Bypass.mc.playerController.windowClick(0, ElytraBoost.getItemElytra(), 1, ClickType.QUICK_MOVE, Minecraft.player);
                } else {
                    Bypass.mc.playerController.windowClick(0, 6, 1, ClickType.QUICK_MOVE, Minecraft.player);
                    Bypass.mc.playerController.windowClick(0, ElytraBoost.getItemElytra(), 1, ClickType.QUICK_MOVE, Minecraft.player);
                    Client.msg("\u00a7f\u00a7lModules:\u00a7r \u00a77[\u00a7lBypass\u00a7r\u00a77]: \u043f\u044b\u0442\u0430\u044e\u0441\u044c \u0432\u044b\u043a\u043b\u044e\u0447\u0438\u0442\u044c NCP.", false);
                }
            }
            if (Minecraft.player.getFlag(7)) {
                event.setGround(false);
                double nY = event.getY() + 0.4 + (Minecraft.player.ticksExisted % 2 != 0 ? 0.001 : 0.0);
                event.setPosY(nY);
                Minecraft.player.posY = nY;
                this.ncpDisabledLocalBool = true;
                float pitch = event.getPitch();
                if (Bypass.mc.objectMouseOver != null && Bypass.mc.objectMouseOver.hitVec != null) {
                    float prevHeight = Minecraft.player.height;
                    Minecraft.player.height = 0.4f;
                    pitch = RotationUtil.getNeededFacing(Bypass.mc.objectMouseOver.hitVec, false, Minecraft.player, false)[1];
                    Minecraft.player.height = prevHeight;
                }
                event.setPitch(MathUtils.clamp(pitch, -45.0f, 30.0f));
                Minecraft.player.prevRotationPitchHead = event.getPitch();
            } else if ((double)Minecraft.player.fallDistance > 0.1 && !this.ncpDisabledLocalBool) {
                Minecraft.player.connection.sendPacket(new CPacketEntityAction(Minecraft.player, CPacketEntityAction.Action.START_FALL_FLYING));
                Minecraft.player.fallDistance = 0.0f;
                this.ncpDisabledLocalBool = true;
            } else if (Minecraft.player.onGround) {
                Minecraft.player.motionY = 0.42f;
                this.ncpDisabledLocalBool = false;
            }
        } else if (this.ncpDisabledLocalBool && Minecraft.player.getFlag(7)) {
            Minecraft.player.connection.sendPacket(new CPacketEntityAction(Minecraft.player, CPacketEntityAction.Action.START_FALL_FLYING));
            Minecraft.player.setFlag(7, false);
        }
        if (this.dodgeFromZero == 1) {
            event.setYaw(this.tempYaw);
            event.setPitch(this.tempPitch);
            Minecraft.player.rotationYawHead = this.tempYaw;
            Minecraft.player.renderYawOffset = this.tempYaw;
            Minecraft.player.rotationPitchHead = this.tempPitch;
            HitAura.get.rotations[0] = this.tempYaw;
            HitAura.get.rotations[1] = this.tempPitch;
            if (Minecraft.player.isHandActive() && Minecraft.player.getActiveHand() != null) {
                mc.getConnection().preSendPacket(new CPacketPlayerTryUseItem(Minecraft.player.getActiveHand()));
            }
        }
    }

    @EventTarget
    public void onMovementInput(EventMovementInput event) {
        if (this.dodgeFromZero == 1) {
            MoveMeHelp.fixDirMove(event, this.tempYaw);
        }
    }

    @EventTarget
    public void onSilentStrafe(EventRotationStrafe event) {
        if (this.dodgeFromZero == 1) {
            event.setYaw(this.tempYaw);
        }
    }

    @EventTarget
    public void onSilentJump(EventRotationJump event) {
        if (this.dodgeFromZero == 1) {
            event.setYaw(this.tempYaw);
        }
    }

    static {
        callSneak = false;
        oldPreseds = new boolean[6];
        timeAfterSneak = new TimerHelper();
    }
}

