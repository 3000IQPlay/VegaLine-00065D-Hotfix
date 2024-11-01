package ru.govno.client.module.modules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityMinecartTNT;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemAir;
import net.minecraft.item.ItemAppleGold;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemElytra;
import net.minecraft.item.ItemEndCrystal;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemSkull;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketClickWindow;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.potion.Potion;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityBed;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import ru.govno.client.Client;
import ru.govno.client.event.EventTarget;
import ru.govno.client.event.events.EventPlayerMotionUpdate;
import ru.govno.client.event.events.EventRender2D;
import ru.govno.client.event.events.EventSendPacket;
import ru.govno.client.module.Module;
import ru.govno.client.module.modules.AutoApple;
import ru.govno.client.module.modules.Bypass;
import ru.govno.client.module.modules.Criticals;
import ru.govno.client.module.modules.Crosshair;
import ru.govno.client.module.modules.ElytraBoost;
import ru.govno.client.module.modules.Fly;
import ru.govno.client.module.modules.FreeCam;
import ru.govno.client.module.modules.HitAura;
import ru.govno.client.module.modules.JesusSpeed;
import ru.govno.client.module.modules.PlayerHelper;
import ru.govno.client.module.modules.Timer;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.newfont.Fonts;
import ru.govno.client.utils.Combat.RotationUtil;
import ru.govno.client.utils.Math.BlockUtils;
import ru.govno.client.utils.Math.MathUtils;
import ru.govno.client.utils.Math.ReplaceStrUtils;
import ru.govno.client.utils.Math.TimerHelper;
import ru.govno.client.utils.Movement.MoveMeHelp;
import ru.govno.client.utils.Render.AnimationUtils;
import ru.govno.client.utils.Render.ColorUtils;
import ru.govno.client.utils.Render.RenderUtils;
import ru.govno.client.utils.Render.StencilUtil;

public class OffHand
extends Module {
    public static TimerHelper timerDelay = new TimerHelper();
    public static TimerHelper timer3 = new TimerHelper();
    public static TimerHelper timer4 = new TimerHelper();
    public static boolean doTotem;
    public static boolean doBackSlot;
    public static boolean totemBackward;
    public static boolean totemTaken;
    public static boolean callNotSave;
    public static boolean fall;
    public static boolean clientSwap;
    public static Item saveSlot;
    public static Item oldSlot;
    public static Item prevOldSlot;
    public static OffHand get;
    private final BoolSettings CanHotbarSwap;
    private final BoolSettings TotemBackward;
    private final BoolSettings ShieldApple;
    private final BoolSettings CrystalApple;
    private final BoolSettings BallApple;
    private final BoolSettings AutoBall;
    private final BoolSettings ShieldBall;
    private final BoolSettings PutBecauseLack;
    private final TimerHelper afterGroundTime = new TimerHelper();
    private float fallDistance;
    private final List<String> BALL_SAMPLES = Arrays.asList("\u0448\u0430\u0440", "\u0441\u0444\u0435\u0440\u0430", "ball", "\u0440\u0443\u043d\u0430", "\u0442\u0430\u043b\u0438\u0441\u043c\u0430\u043d", "\u0442\u0430\u043b\u0438\u043a", "\u043c\u044f\u0447", "\u0430\u0443\u0440\u0430", "\u0430\u043c\u0443\u043b\u0435\u0442", "\u043a\u043e\u043b\u043e\u0431\u043e\u043a");
    private final List<String> CHAR_SAMPLES = Arrays.asList("\u00a71", "\u00a72", "\u00a73", "\u00a74", "\u00a75", "\u00a76", "\u00a77", "\u00a78", "\u00a79", "\u00a70", "\u00a7c", "\u00a7e", "\u00a7a", "\u00a7b", "\u00a7d", "\u00a7f", "\u00a7r", "\u00a7l", "\u00a7k", "\u00a7o", "\u00a7m", "\u00a7n");
    private final List<AttributeWithValue> attributeWithValues = new ArrayList<AttributeWithValue>();
    private final List<String> POTION_EFFECT_STRING_NAMES = Arrays.asList("\u0421\u043a\u043e\u0440\u043e\u0441\u0442\u044c", "\u0421\u043e\u043f\u0440\u043e\u0442\u0438\u0432\u043b\u0435\u043d\u0438\u0435", "\u0421\u0438\u043b\u0430", "\u041e\u0433\u043d\u0435\u0441\u0442\u043e\u0439\u043a\u043e\u0441\u0442\u044c", "\u0421\u043f\u0435\u0448\u043a\u0430");
    private Item curItem = Items.GOLDEN_APPLE;
    private final TimerHelper saveSwapBackTimer = TimerHelper.TimerHelperReseted();
    private AttributeType currentAttributeType;
    private AttributeType prevAttributeType;
    public static AnimationUtils scaleAnim;
    public static AnimationUtils popAnim;

    public OffHand() {
        super("OffHand", 0, Module.Category.PLAYER);
        get = this;
        this.CanHotbarSwap = new BoolSettings("CanHotbarSwap", true, this);
        this.settings.add(this.CanHotbarSwap);
        this.PutBecauseLack = new BoolSettings("PutBecauseLack", false, this);
        this.settings.add(this.PutBecauseLack);
        this.TotemBackward = new BoolSettings("TotemBackward", true, this);
        this.settings.add(this.TotemBackward);
        this.ShieldApple = new BoolSettings("ShieldApple", true, this);
        this.settings.add(this.ShieldApple);
        this.CrystalApple = new BoolSettings("CrystalApple", true, this);
        this.settings.add(this.CrystalApple);
        this.BallApple = new BoolSettings("BallApple", true, this);
        this.settings.add(this.BallApple);
        this.AutoBall = new BoolSettings("AutoBall", true, this);
        this.settings.add(this.AutoBall);
        this.ShieldBall = new BoolSettings("ShieldBall", true, this);
        this.settings.add(this.ShieldBall);
    }

    @EventTarget
    public void onSendPacket(EventSendPacket event) {
        CPacketPlayerTryUseItem iTry;
        Packet packet;
        if (this.actived && (packet = event.getPacket()) instanceof CPacketPlayerTryUseItem && (iTry = (CPacketPlayerTryUseItem)packet).getHand() == EnumHand.OFF_HAND && this.BallApple.getBool() && this.stackIsBall(Minecraft.player.getHeldItemOffhand())) {
            event.cancel();
        }
    }

    @EventTarget
    public void onEventUpdate(EventPlayerMotionUpdate event) {
        long timeOfGround;
        if (!this.actived) {
            return;
        }
        if (!event.onGround()) {
            this.afterGroundTime.reset();
        }
        if ((timeOfGround = this.afterGroundTime.getTime()) > 1000L) {
            this.fallDistance = 0.0f;
        } else {
            if (Minecraft.player.fallDistance != 0.0f) {
                this.fallDistance = Minecraft.player.fallDistance;
            }
            if (timeOfGround == 0L && Minecraft.player.hurtTime == 9) {
                this.fallDistance = 0.0f;
            }
        }
    }

    private void updateEmptyHandFix() {
        if (!this.PutBecauseLack.getBool()) {
            return;
        }
        if (Minecraft.player.ticksExisted < 10) {
            if (!totemTaken) {
                if (this.getSlotByItem(Items.GOLDEN_APPLE) != -1) {
                    oldSlot = Items.GOLDEN_APPLE;
                }
                if (Minecraft.player.getHeldItemOffhand().getItem() == Items.TOTEM) {
                    int ballSlot = this.getSlotByItem(Items.SKULL);
                    if (ballSlot != -1) {
                        oldSlot = Minecraft.player.inventory.getStackInSlot(ballSlot).getItem();
                    } else if (this.getSlotByItem(Items.SHIELD) != -1) {
                        oldSlot = Items.SHIELD;
                    } else if (this.getSlotByItem(Items.GOLDEN_APPLE) != -1) {
                        oldSlot = Items.GOLDEN_APPLE;
                    }
                }
            }
            return;
        }
        ItemStack offStack = Minecraft.player.getHeldItemOffhand();
        if (offStack.getItem() instanceof ItemAir && !totemTaken && !doBackSlot && oldSlot != null && this.getSlotByItem(prevOldSlot) == -1) {
            List<Item> samples = Arrays.asList(Items.POTIONITEM, Items.BOW, Items.SKULL, Items.SHIELD, Items.END_CRYSTAL, Items.GOLDEN_APPLE);
            if ((samples = samples.stream().filter(sample -> !(sample == prevOldSlot || sample == Items.SHIELD && Minecraft.player.getCooldownTracker().getCooldown(Items.SHIELD, 0.0f) > 0.0f || sample == Items.GOLDEN_APPLE && (PlayerHelper.get.actived && PlayerHelper.checkApple || Minecraft.player.getCooldownTracker().getCooldown(Items.GOLDEN_APPLE, 0.0f) > 0.0f) || sample == Items.POTIONITEM && (Minecraft.player.getActivePotionEffect(MobEffects.REGENERATION) == null || Minecraft.player.getAbsorptionAmount() < 1.0f) || sample == Items.BOW && (this.getSlotByItem(Items.ARROW) == -1 || this.getSlotByItem(Items.SPECTRAL_ARROW) == -1 || this.getSlotByItem(Items.TIPPED_ARROW) == -1))).collect(Collectors.toList())).isEmpty()) {
                return;
            }
            if ((samples = samples.stream().filter(sample -> this.getSlotByItem((Item)sample) != -1).collect(Collectors.toList())).isEmpty()) {
                return;
            }
            Collections.reverse(samples);
            oldSlot = samples.get(0);
            doBackSlot = true;
        }
    }

    private boolean haveShar() {
        return this.stackIsBall(Minecraft.player.getHeldItemOffhand()) || this.stackIsBall(Minecraft.player.getItemStackFromSlot(EntityEquipmentSlot.HEAD));
    }

    private float smartTriggerHP() {
        float hp = 5.0f;
        EntityPlayer p = OffHand.getMe();
        float absorbDT = 1.0f;
        for (int i = 0; i < 4; ++i) {
            if (BlockUtils.isArmor(p, BlockUtils.armorElementByInt(i))) continue;
            hp += 1.5f;
            absorbDT += 0.5f;
        }
        if (p.getActivePotionEffect(Potion.getPotionById(10)) != null) {
            hp -= 0.5f;
        }
        if (p.isHandActive() && p.getActiveItemStack().getItem() instanceof ItemAppleGold && p.getItemInUseMaxCount() > 25 && !totemTaken) {
            hp -= 1.0f;
        }
        if (p.getAbsorptionAmount() > 0.0f) {
            hp -= p.getAbsorptionAmount() / MathUtils.clamp(absorbDT, 1.0f, 2.0f);
        }
        if (p.inventory.armorItemInSlot(2).getItem() instanceof ItemElytra) {
            hp += 3.0f;
        }
        return MathUtils.clamp(hp, 2.0f, 20.0f);
    }

    public static boolean crystalWarn(float isInRange) {
        if (OffHand.mc.world != null && OffHand.mc.world.getDifficulty().getDifficultyId() == 0) {
            return false;
        }
        CopyOnWriteArrayList<EntityEnderCrystal> enderCrystals = new CopyOnWriteArrayList<EntityEnderCrystal>();
        if (OffHand.mc.world != null) {
            for (Entity e : OffHand.mc.world.getLoadedEntityList()) {
                if (e == null || !(e instanceof EntityEnderCrystal)) continue;
                EntityEnderCrystal crystal = (EntityEnderCrystal)e;
                if (!(OffHand.getMe().getSmartDistanceToAABB(RotationUtil.getLookRots(OffHand.getMe(), e), e) < (double)isInRange)) continue;
                enderCrystals.add(crystal);
            }
        }
        int balls = 0;
        for (int i = 0; i < 4; ++i) {
            if (!BlockUtils.isArmor(OffHand.getMe(), BlockUtils.armorElementByInt(i))) continue;
            ++balls;
        }
        boolean isFullArmor = balls == 4;
        for (EntityEnderCrystal crystal : enderCrystals) {
            boolean pardon;
            if (crystal == null || OffHand.getMe().getSmartDistanceToAABB(RotationUtil.getLookRots(OffHand.getMe(), crystal), crystal) >= (double)isInRange || !BlockUtils.canPosBeSeenEntityWithCustomVec(BlockUtils.getEntityVec3dPos(crystal).addVector(0.0, -1.0, 0.0), (Entity)OffHand.getMe(), OffHand.getMe().getPositionVector(), BlockUtils.bodyElement.LEGS)) continue;
            float rangePardon = 9.0f - (float)balls * 2.0f;
            boolean bl = pardon = crystal.posY >= OffHand.getMe().posY + 0.8 && BlockUtils.blockMaterialIsCurrent(BlockUtils.getEntityBlockPos(crystal).add(0, -1, 0)) || OffHand.getMe().getDistanceToEntity(crystal) > rangePardon;
            if (!pardon && OffHand.getMe().getHealth() + OffHand.getMe().getAbsorptionAmount() > 18.0f && isFullArmor) {
                pardon = true;
            }
            return !pardon;
        }
        enderCrystals.clear();
        return false;
    }

    private boolean tntWarn(float isInRange) {
        CopyOnWriteArrayList<Entity> tntS = new CopyOnWriteArrayList<Entity>();
        if (OffHand.mc.world != null) {
            for (Entity e : OffHand.mc.world.getLoadedEntityList()) {
                if (e == null || !(e instanceof EntityTNTPrimed) && !(e instanceof EntityMinecartTNT) || !(OffHand.getMe().getDistanceToEntity(e) < isInRange)) continue;
                tntS.add(e);
            }
        }
        for (Entity tnt : tntS) {
            if (tnt == null || OffHand.getMe().getDistanceToEntity(tnt) >= isInRange) continue;
            return BlockUtils.canPosBeSeenEntity(new Vec3d(tnt.posX, tnt.posY, tnt.posZ), (Entity)OffHand.getMe(), BlockUtils.bodyElement.LEGS);
        }
        return false;
    }

    private double getDistanceToTileEntityAtEntity(Entity entity, TileEntity tileEtity) {
        return entity.getDistanceToBlockPos(tileEtity.getPos());
    }

    private boolean bedWarn(float isInRange) {
        if (Minecraft.player.dimension != 0) {
            CopyOnWriteArrayList<TileEntityBed> bedTiles = new CopyOnWriteArrayList<TileEntityBed>();
            if (OffHand.mc.world != null && Minecraft.player.dimension != 0) {
                for (TileEntity t : OffHand.mc.world.getLoadedTileEntityList()) {
                    if (t == null || !(t instanceof TileEntityBed) || !(this.getDistanceToTileEntityAtEntity(OffHand.getMe(), t) < (double)isInRange)) continue;
                    bedTiles.add((TileEntityBed)t);
                }
            }
            for (TileEntityBed bed : bedTiles) {
                if (!BlockUtils.canPosBeSeenEntity(new Vec3d((double)bed.getPos().getX() + 0.5, (double)bed.getPos().getY() + 0.4, (double)bed.getPos().getZ() + 0.5), (Entity)OffHand.getMe(), BlockUtils.bodyElement.LEGS)) continue;
                return true;
            }
        }
        return false;
    }

    private double getCollideYPosition(BlockPos pos) {
        double value = pos.getY() + 1;
        IBlockState state = OffHand.mc.world.getBlockState(pos);
        AxisAlignedBB aabb = state.getSelectedBoundingBox(OffHand.mc.world, pos);
        return aabb == null ? value : aabb.maxY;
    }

    private boolean isCollidablePos(BlockPos pos) {
        return !OffHand.mc.world.getCollisionBoxes(null, new AxisAlignedBB(pos)).isEmpty();
    }

    private boolean isLiquidPos(BlockPos pos) {
        Material material = OffHand.mc.world.getBlockState(pos).getMaterial();
        return material.isLiquid() && material.getMaterialMapColor() == MapColor.WATER;
    }

    private double presentFallDistance(double appendOnPreY, int ticksPre) {
        EntityPlayer self = OffHand.getMe();
        double fd = this.fallDistance;
        if (fd > 3.0) {
            double underY = self.posY;
            double posX = self.posX;
            double posY = underY + (self.posY - self.lastTickPosY);
            double posZ = self.posZ;
            for (double y = underY; y > 0.0; y -= 1.0) {
                BlockPos pos = new BlockPos(posX, y, posZ);
                if (!this.isCollidablePos(pos)) continue;
                BlockPos posUp = pos.up();
                if (this.isLiquidPos(posUp)) {
                    return 0.0;
                }
                underY = this.getCollideYPosition(pos) - 1.0;
                break;
            }
            double groundDiff = Math.abs(posY - underY);
            double fallSpeed = MathUtils.clamp(Minecraft.player.posY - Minecraft.player.lastTickPosY, 0.0, 10.0 * OffHand.mc.timer.speed) * appendOnPreY;
            fd = groundDiff < 10.0 && (fallSpeed >= groundDiff / (double)ticksPre || groundDiff < 1.0) ? fd + groundDiff : 0.0;
        }
        return fd;
    }

    private boolean fallWarn() {
        return !Minecraft.player.capabilities.allowFlying && !Minecraft.player.capabilities.disableDamage && Minecraft.player.fallDistanceIsUnsafe(this.presentFallDistance(2.0, 2), 0.0f);
    }

    private boolean isFallWarning() {
        return fall;
    }

    private void updatefallWarn() {
        int ping = 0;
        if (Minecraft.player != null && Minecraft.player.ticksExisted > 100 && mc.getConnection().getPlayerInfo(Minecraft.player.getUniqueID()) != null) {
            try {
                ping = MathUtils.clamp(mc.getConnection().getPlayerInfo(Minecraft.player.getUniqueID()).getResponseTime(), 0, 1000);
            }
            catch (Exception e) {
                System.out.println("Module-OffHand: Vegaline failled check ping");
            }
        }
        if (this.fallWarn()) {
            fall = true;
            timer3.reset();
        } else if (fall && timer3.hasReached(100 + ping)) {
            fall = false;
            timer3.reset();
        }
    }

    private boolean healthWarn() {
        float health = this.smartTriggerHP();
        return Minecraft.player.getHealth() <= health;
    }

    private boolean deathWarned() {
        boolean warn = false;
        if (this.getSlotByItem(Item.getItemById(449)) != -1 || Minecraft.player.getHeldItemOffhand().getItem() == Item.getItemById(449)) {
            this.updatefallWarn();
            if (this.healthWarn()) {
                warn = true;
            }
            if (!this.haveShar()) {
                if (OffHand.crystalWarn(6.656f)) {
                    warn = true;
                    totemBackward = false;
                }
                if (this.tntWarn(4.87f)) {
                    warn = true;
                    totemBackward = false;
                }
                if (this.bedWarn(7.92f)) {
                    warn = true;
                    totemBackward = false;
                }
            }
            this.updatefallWarn();
            if (this.isFallWarning()) {
                warn = true;
            }
        }
        return Minecraft.player.getHeldItemMainhand().getItem() != Items.TOTEM && !Minecraft.player.isCreative() && warn;
    }

    private boolean canUseItemMainHand() {
        return Minecraft.player.getHeldItemMainhand().getItem() instanceof ItemBlock && (OffHand.mc.objectMouseOver.typeOfHit == null || OffHand.mc.objectMouseOver.typeOfHit == RayTraceResult.Type.BLOCK);
    }

    private static EntityPlayer getMe() {
        return FreeCam.fakePlayer != null && FreeCam.get.actived ? FreeCam.fakePlayer : Minecraft.player;
    }

    @Override
    public String getDisplayName() {
        int count = Minecraft.player == null ? 0 : this.getTotemCount();
        return count > 0 ? this.getDisplayByInt(count) + "T" : this.getName();
    }

    @Override
    public void onUpdate() {
        if (Minecraft.player == null || Minecraft.player.getHealth() == 0.0f || Minecraft.player.isDead || OffHand.mc.currentScreen instanceof GuiContainer && !(OffHand.mc.currentScreen instanceof GuiInventory)) {
            return;
        }
        if (Minecraft.player.ticksExisted == 1) {
            oldSlot = null;
        }
        boolean bl = totemTaken = this.deathWarned() && !totemBackward;
        if (timer4.hasReached(2100.0) || !this.TotemBackward.getBool()) {
            totemBackward = false;
        }
        if (callNotSave) {
            callNotSave = false;
        } else if (Keyboard.isKeyDown((int)OffHand.mc.gameSettings.keyBindSwapHands.getKeyCode()) && Item.getItemById(449) != null || GuiContainer.draggedStack != null && OffHand.mc.currentScreen instanceof GuiInventory && Mouse.isButtonDown((int)0)) {
            oldSlot = null;
        } else if (!(Minecraft.player.getHeldItemOffhand().getItem() instanceof ItemAir) && Minecraft.player.getHeldItemOffhand().getItem() != Items.TOTEM) {
            oldSlot = Minecraft.player.getHeldItemOffhand().getItem();
        }
        if (this.AutoBall.getBool()) {
            this.currentAttributeType = this.getCurrentAttributeType(this.currentAttributeType);
        }
        this.updateOffHandHelps(this.CrystalApple.getBool(), this.ShieldApple.getBool(), this.BallApple.getBool(), this.ShieldBall.getBool());
        this.updateEmptyHandFix();
        if (!(!this.deathWarned() || this.getSlotByItem(Items.TOTEM) == -1 && Minecraft.player.getHeldItemOffhand().getItem() != Items.TOTEM || doBackSlot || totemBackward)) {
            if (Minecraft.player.getHeldItemOffhand().getItem() != Items.TOTEM) {
                doTotem = true;
            }
        } else if (oldSlot != null && this.getSlotByItem(oldSlot) != -1 && Minecraft.player.getHeldItemOffhand().getItem() != oldSlot) {
            doBackSlot = !Minecraft.player.isHandActive() || Minecraft.player.getActiveHand() != EnumHand.MAIN_HAND || Minecraft.player.getActiveItemStack().getItem() != oldSlot;
        } else if (this.AutoBall.getBool() && this.currentAttributeType != null && oldSlot instanceof ItemSkull) {
            ItemStackWithSlot iswsGeted;
            int slotGet;
            ItemStackWithSlot offStackWithSlot = new ItemStackWithSlot(Minecraft.player.getHeldItem(EnumHand.OFF_HAND), 45);
            boolean hasCurrentAttributeInOffStack = this.hasAttributeInStack(offStackWithSlot, this.currentAttributeType);
            boolean hasBetterAttibuteStack = false;
            if (hasCurrentAttributeInOffStack && (slotGet = this.getSlotByItem(Items.SKULL)) != -1 && this.getAttributeValueFromAll(this.getStackAttributes(iswsGeted = new ItemStackWithSlot(Minecraft.player.inventoryContainer.getSlot(slotGet).getStack(), slotGet)), this.currentAttributeType) > this.getAttributeValueFromAll(this.getStackAttributes(offStackWithSlot), this.currentAttributeType)) {
                hasBetterAttibuteStack = true;
            }
            if ((!hasCurrentAttributeInOffStack || hasBetterAttibuteStack) && !Minecraft.player.isHandActive() && this.hasAttributeInInventory(this.currentAttributeType)) {
                doBackSlot = true;
            }
        }
        this.doItem(45, this.CanHotbarSwap.getBool());
        if (this.AutoBall.getBool()) {
            this.prevAttributeType = this.currentAttributeType;
        }
    }

    private boolean haveItem(Item itemIn) {
        return this.getSlotByItem(itemIn) != -1 || Minecraft.player.inventoryContainer.getSlot(45).getStack().getItem() == itemIn;
    }

    private boolean isBadOver() {
        if (OffHand.mc.objectMouseOver != null && OffHand.mc.objectMouseOver.getBlockPos() != null) {
            Block block = OffHand.mc.world.getBlockState(OffHand.mc.objectMouseOver.getBlockPos()).getBlock();
            List<Integer> badBlockIDs = Arrays.asList(96, 167, 54, 130, 146, 58, 64, 71, 193, 194, 195, 196, 197, 324, 330, 427, 428, 429, 430, 431, 154, 61, 23, 158, 145, 69, 107, 187, 186, 185, 184, 183, 107, 116, 84, 356, 404, 151, 25, 219, 220, 221, 222, 223, 224, 225, 226, 227, 228, 229, 230, 231, 232, 233, 234, 389, 379, 380, 138, 321, 323, 77, 143, 379);
            boolean interact = !Minecraft.player.isSneaking() && OffHand.mc.objectMouseOver != null && OffHand.mc.objectMouseOver.getBlockPos() != null && block != null && badBlockIDs.stream().anyMatch(id -> Block.getIdFromBlock(block) == id);
            return interact;
        }
        return false;
    }

    private boolean stackIsBall(ItemStack stack) {
        if (stack == null || stack.getDisplayName().isEmpty() || stack.getItem() != Items.SKULL) {
            return false;
        }
        String stackName = stack.getDisplayName();
        for (String sample2 : this.CHAR_SAMPLES) {
            stackName = stackName.replace(sample2, "");
        }
        String finalStackName = stackName.toLowerCase();
        return this.BALL_SAMPLES.stream().anyMatch(sample -> finalStackName.contains((CharSequence)sample));
    }

    private AttributeType getAttributeTypeByName(String name) {
        return Arrays.stream(AttributeType.values()).filter(attributeType -> name.endsWith(attributeType.getName())).findAny().orElse(null);
    }

    public List<String> getLoresAsStack(ItemStack stack) {
        List<String> list = stack.getTooltip(Minecraft.player, ITooltipFlag.TooltipFlags.NORMAL);
        for (int i = 0; i < list.size(); ++i) {
            if (i == 0) {
                list.set(i, stack.getRarity().rarityColor + list.get(i));
                continue;
            }
            list.set(i, TextFormatting.GRAY + list.get(i));
        }
        list = list.stream().filter(str -> str.length() > 1).map(str -> new TextComponentString((String)str).getFormattedText()).filter(Objects::nonNull).map(str -> ReplaceStrUtils.fixString(ReplaceStrUtils.deformatString(str, 1))).collect(Collectors.toList());
        return list;
    }

    private AttributeType getAttributeTypeAsPotionName(String potionName) {
        switch (potionName) {
            case "\u0421\u043a\u043e\u0440\u043e\u0441\u0442\u044c": {
                return AttributeType.SPEED_UP;
            }
            case "\u0421\u043e\u043f\u0440\u043e\u0442\u0438\u0432\u043b\u0435\u043d\u0438\u0435": {
                return AttributeType.ARMOR_UP;
            }
            case "\u0421\u0438\u043b\u0430": {
                return AttributeType.DAMAGE_UP;
            }
            case "\u0421\u043f\u0435\u0448\u043a\u0430": {
                return AttributeType.COOLDOWN_UP;
            }
        }
        return null;
    }

    private int getPotionEffectLevelAsRims(String rim) {
        return rim.contains("V") ? 5 : (rim.contains("IV") ? 4 : (rim.contains("III") ? 3 : (rim.contains("II") ? 2 : (rim.contains("(") ? 1 : -1))));
    }

    private List<AttributeWithValue> getStackAttributes(ItemStackWithSlot stackWithSlot) {
        if (!this.attributeWithValues.isEmpty()) {
            this.attributeWithValues.clear();
        }
        if (stackWithSlot.getItemStack() != null) {
            NBTTagCompound nbt = stackWithSlot.getItemStack().getTagCompound();
            if (nbt != null && nbt.hasKey("AttributeModifiers", 9)) {
                NBTTagList attributeList = nbt.getTagList("AttributeModifiers", 10);
                Arrays.stream(IntStream.rangeClosed(0, attributeList.tagCount() - 1).toArray()).mapToObj(index -> attributeList.getCompoundTagAt(index)).forEach(compound -> {
                    AttributeType attributeType;
                    double value = compound.getDouble("Amount");
                    if (value != 0.0 && (attributeType = this.getAttributeTypeByName(compound.getString("AttributeName"))) != null) {
                        this.attributeWithValues.add(new AttributeWithValue(attributeType, value, stackWithSlot));
                    }
                });
            } else {
                List<String> loresAsStack = this.getLoresAsStack(stackWithSlot.getItemStack());
                if (loresAsStack.size() > 3) {
                    for (String potionName : this.POTION_EFFECT_STRING_NAMES) {
                        for (String lore : loresAsStack) {
                            int attributeLevel;
                            AttributeType attributeTypeAsPotion;
                            if (lore.length() < 4 || !lore.contains(potionName) || (attributeTypeAsPotion = this.getAttributeTypeAsPotionName(potionName)) == null || (attributeLevel = this.getPotionEffectLevelAsRims(lore)) == -1) continue;
                            this.attributeWithValues.add(new AttributeWithValue(attributeTypeAsPotion, attributeLevel, stackWithSlot));
                        }
                    }
                }
            }
        }
        return this.attributeWithValues;
    }

    private boolean hasAttributeInStack(ItemStackWithSlot stack, AttributeType type2) {
        return type2 != null && this.getStackAttributes(stack).stream().map(AttributeWithValue::getAttributeType).anyMatch(attributeType -> attributeType == type2);
    }

    private double getAttributeValueFromAll(List<AttributeWithValue> attributesInStack, AttributeType currentType) {
        if (!attributesInStack.isEmpty() && !(attributesInStack = this.getSortedByValues(attributesInStack.stream().filter(attributeInStack -> attributeInStack.getAttributeType() == currentType).toList())).isEmpty()) {
            return attributesInStack.get(0).getValue();
        }
        return 0.0;
    }

    private boolean hasAttributeInInventory(AttributeType attributeType) {
        boolean hasCurrentAttribute = false;
        for (int i = 0; i < Minecraft.player.inventory.getSizeInventory(); ++i) {
            try {
                ItemStack itemStack = Minecraft.player.inventory.getStackInSlot(i);
                if (!this.stackIsBall(itemStack) || !this.hasAttributeInStack(new ItemStackWithSlot(itemStack, i), attributeType)) continue;
                hasCurrentAttribute = true;
                break;
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        return hasCurrentAttribute;
    }

    private boolean hasAttributeInStack(List<AttributeWithValue> attributeWithValues, ItemStackWithSlot stack, AttributeType type2) {
        return type2 != null && attributeWithValues.stream().map(AttributeWithValue::getAttributeType).anyMatch(attributeType -> attributeType.equals((Object)type2));
    }

    private List<AttributeWithValue> getSortedByValues(List<AttributeWithValue> attributeWithValues) {
        return attributeWithValues.stream().sorted(Comparator.comparingDouble(AttributeWithValue::getReverseValue)).toList();
    }

    private List<ItemStackWithSlot> getSortedByValuesStacks(List<ItemStackWithSlot> itemStackWithSlots) {
        return itemStackWithSlots.stream().sorted(Comparator.comparingDouble(stackWithSlot -> this.getStackAttributes((ItemStackWithSlot)stackWithSlot).stream().filter(attr -> attr.getAttributeType() == this.currentAttributeType).findFirst().map(AttributeWithValue::getReverseValue).orElse(0.0))).toList();
    }

    private void updateOffHandHelps(boolean crystalApple, boolean shieldApple, boolean ballApple, boolean shieldBall) {
        boolean ballappleTrigger;
        boolean shieldappleTrigger;
        boolean ballshieldTrigger;
        boolean crystalappleTrigger;
        boolean bad = this.isBadOver() || OffHand.mc.currentScreen != null && !(OffHand.mc.currentScreen instanceof GuiIngameMenu);
        Item offItem = Minecraft.player.getHeldItemOffhand().getItem();
        Item mainItem = Minecraft.player.getHeldItemMainhand().getItem();
        boolean hasShieldCooldown = Minecraft.player.getCooldownTracker().getCooldown(Items.SHIELD, 0.0f) > 0.03f && Minecraft.player.getCooldownTracker().getCooldown(Items.SHIELD, 0.0f) < 0.99f;
        boolean pcm = OffHand.mc.gameSettings.keyBindUseItem.isKeyDown();
        if (pcm) {
            this.saveSwapBackTimer.reset();
        }
        if (!this.saveSwapBackTimer.hasReached(100.0)) {
            pcm = true;
        }
        boolean mainTeadled = Minecraft.player.isHandActive() && Minecraft.player.getActiveHand() == EnumHand.MAIN_HAND || mainItem == Items.ENDER_PEARL || Minecraft.player.getHeldItemMainhand().getItem() instanceof ItemPotion || (mainItem instanceof ItemBlock || mainItem instanceof ItemEndCrystal) && OffHand.mc.objectMouseOver != null && OffHand.mc.objectMouseOver.typeOfHit == RayTraceResult.Type.BLOCK;
        boolean bl = crystalappleTrigger = crystalApple && offItem == Items.END_CRYSTAL && this.haveItem(Items.GOLDEN_APPLE) && saveSlot == null && !this.canUseItemMainHand() && mainItem != this.curItem && (offItem == Items.END_CRYSTAL || oldSlot == this.curItem) && !mainTeadled && pcm && !bad;
        if (crystalappleTrigger) {
            this.curItem = Items.GOLDEN_APPLE;
        }
        boolean bl2 = ballshieldTrigger = shieldBall && (offItem == Items.SHIELD || this.stackIsBall(Minecraft.player.getHeldItemOffhand())) && mainItem != Items.SHIELD && Minecraft.player.getHealth() > Minecraft.player.getMaxHealth() / 1.6f && mainItem != Items.SHIELD && !hasShieldCooldown && EntityLivingBase.isMatrixDamaged && pcm && this.haveItem(Items.SHIELD) && !mainTeadled && !(mainItem instanceof ItemFood) && !bad;
        if (ballshieldTrigger) {
            this.curItem = Items.SHIELD;
        }
        boolean bl3 = shieldappleTrigger = !ballshieldTrigger && shieldApple && (offItem == Items.SHIELD || offItem == Items.GOLDEN_APPLE) && this.haveItem(Items.GOLDEN_APPLE) && mainItem != this.curItem && (hasShieldCooldown || Minecraft.player.getHealth() + Minecraft.player.getAbsorptionAmount() <= 11.0f && Minecraft.player.getActiveHand() == EnumHand.OFF_HAND && Minecraft.player.isBlocking()) && !bad && !mainTeadled && !(mainItem instanceof ItemFood);
        if (shieldappleTrigger) {
            this.curItem = Items.GOLDEN_APPLE;
        }
        boolean bl4 = ballappleTrigger = !ballshieldTrigger && ballApple && (offItem == Items.GOLDEN_APPLE || this.stackIsBall(Minecraft.player.getHeldItemOffhand())) && pcm && this.haveItem(Items.GOLDEN_APPLE) && Minecraft.player.getHealth() <= Minecraft.player.getMaxHealth() * 0.7f && !bad && !mainTeadled && !(mainItem instanceof ItemFood);
        if (ballappleTrigger) {
            this.curItem = Items.GOLDEN_APPLE;
        }
        boolean isTriggered = crystalappleTrigger || shieldappleTrigger || ballshieldTrigger || ballappleTrigger;
        boolean resetTrigger = false;
        if (isTriggered) {
            saveSlot = crystalappleTrigger ? Items.END_CRYSTAL : (shieldappleTrigger ? Items.SHIELD : (ballshieldTrigger || ballappleTrigger ? Items.SKULL : oldSlot));
        } else if (Minecraft.player.getHeldItemOffhand().getItem() == saveSlot) {
            resetTrigger = true;
        }
        if (isTriggered) {
            clientSwap = true;
        } else if (resetTrigger) {
            clientSwap = false;
            saveSlot = null;
        }
        if (clientSwap && !resetTrigger && saveSlot != null) {
            Item i;
            Item item = i = (isTriggered || pcm) && this.curItem != Items.SHIELD ? this.curItem : saveSlot;
            if (Minecraft.player.getHeldItemMainhand().getItem() != i && Minecraft.player.getHeldItemOffhand().getItem() != i) {
                oldSlot = i;
            }
        }
    }

    public void invClick(int slotId, boolean pcm) {
        ItemStack itemstack = Minecraft.player.inventoryContainer.slotClick(slotId, !pcm ? 0 : 1, ClickType.PICKUP, Minecraft.player);
        Minecraft.player.connection.sendPacket(new CPacketClickWindow(Minecraft.player.inventoryContainer != null ? Minecraft.player.inventoryContainer.windowId : 0, slotId, !pcm ? 0 : 1, ClickType.PICKUP, itemstack, Minecraft.player.inventoryContainer.getNextTransactionID(Minecraft.player.inventory)));
    }

    public void invClick(int slotId, boolean pcm, int ms) {
        OffHand.mc.playerController.windowClickMemory(Minecraft.player.inventoryContainer != null ? Minecraft.player.inventoryContainer.windowId : 0, slotId, !pcm ? 0 : 1, ClickType.PICKUP, Minecraft.player, ms);
    }

    public void doItem(final int slotIn, boolean canHotbarSwap) {
        if (!(Minecraft.player.openContainer instanceof ContainerPlayer)) {
            return;
        }
        final int currentItem = OffHand.doBackSlot ? this.getSlotByItem(OffHand.oldSlot) : (OffHand.doTotem ? this.getSlotByItem(Items.TOTEM) : -1);
        if (currentItem < 36 || currentItem > 44) {
            canHotbarSwap = false;
        }
        final boolean aac = Bypass.get.isAACWinClick();
        if ((!OffHand.doTotem && !OffHand.doBackSlot) || !OffHand.timerDelay.hasReached(canHotbarSwap ? 50.0 : ((double)(150 + (aac ? 50 : 0))))) {
            return;
        }
        if (currentItem == -1) {
            return;
        }
        if (currentItem >= 36 && currentItem <= 44 && !this.isBadOver() && OffHand.mc.currentScreen == null) {
            if ((Minecraft.player.inventory.getStackInSlot(currentItem - 36) == Minecraft.player.inventory.getCurrentItem() || !(Minecraft.player.inventoryContainer instanceof ContainerPlayer) || canHotbarSwap) && slotIn == 45) {
                final ItemStack stackInCurrectSlot = Minecraft.player.inventory.getStackInSlot(currentItem - 36);
                if (this.stackIsBall(stackInCurrectSlot) || !canHotbarSwap) {
                    final int handSlot = Minecraft.player.inventory.currentItem;
                    OffHand.mc.playerController.windowClick(0, currentItem, handSlot, ClickType.SWAP, (EntityPlayer)Minecraft.player);
                    OffHand.mc.playerController.windowClickMemory(0, 45, handSlot, ClickType.SWAP, (EntityPlayer)Minecraft.player, 50);
                    OffHand.mc.playerController.windowClickMemory(0, currentItem, handSlot, ClickType.SWAP, (EntityPlayer)Minecraft.player, 50);
                }
                else {
                    if (Minecraft.player.inventory.currentItem != currentItem - 36) {
                        Minecraft.player.connection.sendPacket((Packet)new CPacketHeldItemChange(currentItem - 36));
                    }
                    Minecraft.player.connection.sendPacket((Packet)new CPacketPlayerDigging(CPacketPlayerDigging.Action.SWAP_HELD_ITEMS, BlockPos.ORIGIN, EnumFacing.DOWN));
                    Minecraft.player.setHeldItem(EnumHand.OFF_HAND, Minecraft.player.inventory.getStackInSlot(currentItem - 36));
                    if (Minecraft.player.inventory.currentItem != currentItem - 36) {
                        Minecraft.player.connection.sendPacket((Packet)new CPacketHeldItemChange(Minecraft.player.inventory.currentItem));
                    }
                }
            }
            else if (slotIn != currentItem - 36) {
                OffHand.mc.playerController.windowClick(Minecraft.player.inventoryContainer.windowId, slotIn, currentItem - 36, ClickType.SWAP, (EntityPlayer)Minecraft.player);
            }
            if (slotIn == 45) {
                Minecraft.player.setHeldItem(EnumHand.OFF_HAND, Minecraft.player.getHeldItemOffhand());
            }
        }
        else if (slotIn != currentItem && currentItem != -1) {
            final int handSlot2 = Minecraft.player.inventory.currentItem;
            if (aac) {
                if (Minecraft.player.inventory.currentItem != currentItem - 36) {
                    Minecraft.player.connection.sendPacket((Packet)new CPacketHeldItemChange(Minecraft.player.inventory.currentItem));
                }
                final ItemStack stackInCurrectSlot2 = Minecraft.player.inventory.getStackInSlot(currentItem);
                OffHand.mc.playerController.windowClick(0, currentItem, handSlot2, ClickType.SWAP, (EntityPlayer)Minecraft.player);
                if (currentItem >= 0) {
                    Minecraft.player.setHeldItem(EnumHand.MAIN_HAND, stackInCurrectSlot2);
                }
                Minecraft.player.connection.sendPacket((Packet)new CPacketPlayerDigging(CPacketPlayerDigging.Action.SWAP_HELD_ITEMS, BlockPos.ORIGIN, EnumFacing.DOWN));
                final ItemStack offStack = Minecraft.player.getHeldItemOffhand();
                Minecraft.player.setHeldItem(EnumHand.OFF_HAND, Minecraft.player.getHeldItemMainhand());
                Minecraft.player.setHeldItem(EnumHand.MAIN_HAND, offStack);
                OffHand.mc.playerController.windowClick(0, currentItem, handSlot2, ClickType.SWAP, (EntityPlayer)Minecraft.player);
            }
            else {
                OffHand.mc.playerController.windowClick(0, currentItem, handSlot2, ClickType.SWAP, (EntityPlayer)Minecraft.player);
                final ItemStack stackInCurrectSlot2 = Minecraft.player.inventory.getStackInSlot(currentItem);
                if (this.stackIsBall(stackInCurrectSlot2)) {
                    OffHand.mc.playerController.windowClickMemory(0, 45, handSlot2, ClickType.SWAP, (EntityPlayer)Minecraft.player, 50);
                    OffHand.mc.playerController.windowClickMemory(0, currentItem, handSlot2, ClickType.SWAP, (EntityPlayer)Minecraft.player, 50);
                }
                else {
                    OffHand.mc.playerController.windowClick(0, 45, handSlot2, ClickType.SWAP, (EntityPlayer)Minecraft.player);
                    OffHand.mc.playerController.windowClick(0, currentItem, handSlot2, ClickType.SWAP, (EntityPlayer)Minecraft.player);
                }
            }
            if (!aac && slotIn == 45 && !(Minecraft.player.getHeldItemOffhand().getItem() instanceof ItemAir)) {
                Minecraft.player.setHeldItem(EnumHand.OFF_HAND, Minecraft.player.getHeldItemOffhand());
            }
        }
        OffHand.doBackSlot = false;
        OffHand.doTotem = false;
        OffHand.timerDelay.reset();
        OffHand.callNotSave = true;
    }

    public int getSlotByItem(Item itemIn) {
        ArrayList<ItemStackWithSlot> attributed;
        List<ItemStackWithSlot> inventory = Arrays.stream(IntStream.rangeClosed(0, Minecraft.player.inventoryContainer.inventorySlots.size() - 1).toArray()).mapToObj(index -> new ItemStackWithSlot(Minecraft.player.inventoryContainer.getSlot(index).getStack(), index)).filter(Objects::nonNull).toList();
        ArrayList<ItemStackWithSlot> defaults = attributed = new ArrayList<ItemStackWithSlot>();
        for (ItemStackWithSlot stackWithSlot : inventory) {
            ItemStack stack = stackWithSlot.getItemStack();
            int slotID = stackWithSlot.getSlot();
            if (stack.getItem() != itemIn) continue;
            if (this.stackIsBall(stack) && slotID != 45 && this.hasAttributeInStack(stackWithSlot, this.currentAttributeType)) {
                attributed.add(stackWithSlot);
                continue;
            }
            defaults.add(stackWithSlot);
        }
        ArrayList<ItemStackWithSlot> finalInventory = new ArrayList<ItemStackWithSlot>();
        if (!attributed.isEmpty()) {
            finalInventory.addAll(this.getSortedByValuesStacks(attributed));
        }
        if (!defaults.isEmpty()) {
            finalInventory.addAll(defaults);
        }
        return finalInventory.isEmpty() || finalInventory.get(0) == null ? -1 : ((ItemStackWithSlot)finalInventory.get(0)).getSlot();
    }

    private AttributeType setCurrentAttribute(AttributeType attributeType, AttributeType currentAttributeType) {
        if (attributeType != currentAttributeType && this.hasAttributeInInventory(currentAttributeType)) {
            attributeType = currentAttributeType;
        }
        return attributeType;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private AttributeType getCurrentAttributeType(AttributeType prevAttributeType) {
        prevAttributeType = this.setCurrentAttribute(prevAttributeType, AttributeType.ARMOR_UP);
        if (JesusSpeed.isJesused || Timer.get.actived || prevAttributeType != AttributeType.HEALTH_UP && (!Minecraft.player.isBurning() || Minecraft.player.isPotionActive(MobEffects.FIRE_RESISTANCE)) && Minecraft.player.getHealth() >= 16.0f && (this.currentAttributeType == AttributeType.SPEED_UP ? MoveMeHelp.getCuttingSpeed() > 0.0 || JesusSpeed.isJesused || MoveMeHelp.isMoving() : MoveMeHelp.isMoving()) && !Fly.get.isActived() && !ElytraBoost.get.isActived() && OffHand.mc.world.playerEntities.stream().map(Entity::getOtherPlayerOf).filter(Objects::nonNull).filter(player -> player.isEntityAlive() && !Client.friendManager.isFriend(player.getName())).noneMatch(player -> (double)Minecraft.player.getDistanceToEntity((Entity)player) < 6.5)) {
            if (!Minecraft.player.onGround && Minecraft.player.motionY < 0.2) {
                if (Minecraft.player.isJumping()) return this.setCurrentAttribute(prevAttributeType, AttributeType.SPEED_UP);
            }
            if (JesusSpeed.isJesused) return this.setCurrentAttribute(prevAttributeType, AttributeType.SPEED_UP);
            if (this.currentAttributeType == AttributeType.SPEED_UP) {
                return this.setCurrentAttribute(prevAttributeType, AttributeType.SPEED_UP);
            }
        }
        if (Minecraft.player.getHealth() + Minecraft.player.getAbsorptionAmount() >= 24.0f) {
            if (HitAura.TARGET_ROTS == null) return this.setCurrentAttribute(prevAttributeType, AttributeType.HEALTH_UP);
            if (!EntityLivingBase.isNcpDamaged) {
                return this.setCurrentAttribute(prevAttributeType, AttributeType.HEALTH_UP);
            }
        }
        if (HitAura.TARGET_ROTS == null) return prevAttributeType;
        int armC = 0;
        for (int i = 0; i < 4; ++i) {
            if (!BlockUtils.isArmor(Minecraft.player, BlockUtils.armorElementByInt(i))) continue;
            ++armC;
        }
        if (Minecraft.player.getHealth() + Minecraft.player.getAbsorptionAmount() >= Minecraft.player.getMaxHealth() / 1.6f) {
            if (HitAura.TARGET_ROTS.getHealth() + Minecraft.player.getAbsorptionAmount() < HitAura.TARGET_ROTS.getMaxHealth() / (EntityLivingBase.isNcpDamaged ? 1.5f : 1.25f)) return this.setCurrentAttribute(prevAttributeType, AttributeType.DAMAGE_UP);
            if (!EntityLivingBase.isNcpDamaged && Minecraft.player.isPotionActive(MobEffects.STRENGTH) && armC == 4 && Minecraft.player.getTotalArmorValue() >= 10) {
                return this.setCurrentAttribute(prevAttributeType, AttributeType.DAMAGE_UP);
            }
        }
        if (!(Minecraft.player.getHealth() >= 18.0f)) return prevAttributeType;
        if (!Criticals.get.isActived()) return prevAttributeType;
        if (!Criticals.get.EntityHit.getBool()) return prevAttributeType;
        if (Minecraft.player.isJumping()) return prevAttributeType;
        if (Criticals.get.HitMode.getMode().equalsIgnoreCase("VanillaHop")) return prevAttributeType;
        if (!EntityLivingBase.isNcpDamaged) {
            if (Criticals.get.HitMode.getMode().equalsIgnoreCase("Matrix2")) return prevAttributeType;
        }
        if (!Criticals.get.HitMode.getMode().equalsIgnoreCase("MatrixStand")) return this.setCurrentAttribute(prevAttributeType, AttributeType.COOLDOWN_UP);
        if (!Minecraft.player.onGround) return prevAttributeType;
        if (MoveMeHelp.getSpeed() != 0.0) return prevAttributeType;
        return this.setCurrentAttribute(prevAttributeType, AttributeType.COOLDOWN_UP);
    }

    @EventTarget
    public void onRender2D(EventRender2D event) {
        float scaleAnimVal;
        int totemCount = this.getTotemCount();
        if (totemTaken) {
            OffHand.scaleAnim.to = 1.05f;
        }
        if ((scaleAnimVal = scaleAnim.getAnim()) > 1.0f) {
            scaleAnim.setAnim(1.0f);
            scaleAnimVal = 1.0f;
        }
        if (OffHand.scaleAnim.to == 0.0f && (double)scaleAnimVal < 0.1) {
            scaleAnim.setAnim(0.0f);
        }
        float popAnimVal = popAnim.getAnim();
        if (!totemTaken && !(popAnimVal > 0.0f) && OffHand.scaleAnim.to != 0.0f) {
            OffHand.scaleAnim.to = 0.0f;
        }
        if (OffHand.popAnim.to == 0.0f && (double)popAnimVal < 0.03) {
            popAnim.setAnim(0.0f);
        }
        OffHand.popAnim.speed = 0.02f;
        if (scaleAnimVal == 0.0f) {
            return;
        }
        float x = (float)event.getResolution().getScaledWidth() / 2.0f + (OffHand.mc.gameSettings.thirdPersonView != 0 ? -8.0f + 20.0f * AutoApple.get.scaleAnimation.anim : 12.0f);
        float y = (float)event.getResolution().getScaledHeight() / 2.0f - 8.0f;
        GL11.glPushMatrix();
        GL11.glDepthMask((boolean)false);
        GL11.glEnable((int)2929);
        GL11.glTranslatef((float)(x += Crosshair.get.crossPosMotions[0]), (float)(y += Crosshair.get.crossPosMotions[1]), (float)0.0f);
        RenderUtils.customScaledObject2D(0.0f, 0.0f, 16.0f, 16.0f, scaleAnimVal);
        float popAnimPC = 1.0f - popAnimVal;
        if (popAnimVal * 4.0f * 255.0f >= 33.0f) {
            GL11.glPushMatrix();
            RenderUtils.customScaledObject2D(0.0f, 0.0f, 16.0f, 16.0f, popAnimPC);
            RenderUtils.customRotatedObject2D(5.0f + popAnimPC * 20.0f, 8.0f - popAnimPC * popAnimPC * popAnimPC * popAnimPC * 8.0f, 0.0f, 0.0f, -180.0f - popAnimPC * -180.0f);
            Fonts.noise_24.drawStringWithShadow("-1", 2.0f + popAnimPC * 20.0f, 4.0f - popAnimPC * popAnimPC * popAnimPC * popAnimPC * 8.0f, ColorUtils.getColor(255, 0, 0, MathUtils.clamp(popAnimVal * 4.0f * 255.0f, 33.0f, 255.0f)));
            GL11.glPopMatrix();
        }
        RenderUtils.customRotatedObject2D(0.0f, 0.0f, 16.0f, 16.0f, popAnimVal * popAnimVal * popAnimVal * 20.0f);
        RenderUtils.customScaledObject2D(0.0f, 0.0f, 16.0f, 16.0f, 1.0f + popAnimVal * popAnimVal * popAnimVal);
        ItemStack stack = new ItemStack(Items.TOTEM);
        if (popAnimVal != 0.0f) {
            float popAnimValMM = 1.0f - MathUtils.clamp(popAnimVal, 0.0f, 1.0f);
            float popAnimPC2 = ((double)popAnimValMM > 0.5 ? 1.0f - popAnimValMM : popAnimValMM) * 3.0f;
            popAnimPC2 = popAnimPC2 > 1.0f ? 1.0f : popAnimPC2;
            GL11.glPushMatrix();
            StencilUtil.initStencilToWrite();
            GL11.glTranslated((double)(popAnimPC2 * 16.0f), (double)(-popAnimPC2 * 6.0f), (double)0.0);
            RenderUtils.customScaledObject2D(0.0f, 0.0f, 16.0f, 16.0f, 0.5f + popAnimPC2 * popAnimVal);
            RenderUtils.customRotatedObject2D(0.0f, 0.0f, 16.0f, 16.0f, 270.0f * -popAnimPC * popAnimPC * popAnimPC * popAnimPC * popAnimPC);
            mc.getRenderItem().renderItemIntoGUI(stack, 0, 0);
            StencilUtil.readStencilBuffer(1);
            mc.getRenderItem().renderItemIntoGUI(stack, 0, 0);
            RenderUtils.drawAlphedRect(-24.0, -24.0, 48.0, 48.0, ColorUtils.getColor(255, 255, 255, popAnimVal * 255.0f));
            StencilUtil.uninitStencilBuffer();
            GL11.glPopMatrix();
        }
        if (popAnimVal != 0.0f) {
            GL11.glPushMatrix();
            RenderUtils.customScaledObject2D(0.0f, 0.0f, 16.0f, 16.0f, MathUtils.clamp(popAnimPC * 1.5f, 0.0f, 1.0f));
            mc.getRenderItem().renderItemIntoGUI(stack, 0, 0);
            GL11.glPopMatrix();
        } else {
            mc.getRenderItem().renderItemIntoGUI(stack, 0, 0);
        }
        int c = totemCount == 0 ? ColorUtils.fadeColor(ColorUtils.getColor(255, 80, 50, 80.0f * scaleAnimVal), ColorUtils.getColor(255, 80, 50, 255.0f * scaleAnimVal), 1.5f) : ColorUtils.getColor(255, 255, 255, 255.0f * scaleAnimVal);
        (totemCount == 0 ? Fonts.noise_20 : Fonts.mntsb_12).drawStringWithShadow(totemCount + "x", totemCount == 0 ? 14.0 : 12.0, totemCount == 0 ? 9.0 : 13.5, c);
        GL11.glDepthMask((boolean)true);
        GL11.glPopMatrix();
    }

    @Override
    public void onToggled(boolean actived) {
        if (!actived) {
            totemTaken = false;
        }
        super.onToggled(actived);
    }

    private int getTotemCount() {
        int totemCount = 0;
        for (int i = 0; i <= 45; ++i) {
            ItemStack is = Minecraft.player.inventoryContainer.getSlot(i).getStack();
            if (is.getItem() != Items.TOTEM) continue;
            totemCount += is.stackSize;
        }
        return totemCount;
    }

    static {
        scaleAnim = new AnimationUtils(0.0f, 0.0f, 0.07f);
        popAnim = new AnimationUtils(0.0f, 0.0f, 0.03f);
    }

    private static enum AttributeType {
        HEALTH_UP("maxHealth"),
        ANTI_KNOCKBACK("knockbackResistance"),
        DAMAGE_UP("attackDamage"),
        COOLDOWN_UP("attackSpeed"),
        ARMOR_UP("armor"),
        ARMOR_DUR("armorToughness"),
        SPEED_UP("movementSpeed");

        String attributeName;

        private AttributeType(String attributeName) {
            this.attributeName = attributeName;
        }

        String getName() {
            return this.attributeName;
        }
    }

    private class ItemStackWithSlot {
        private final ItemStack itemStack;
        private final int slot;

        public ItemStackWithSlot(ItemStack itemStack, int slot) {
            this.itemStack = itemStack;
            this.slot = slot;
        }

        public ItemStack getItemStack() {
            return this.itemStack;
        }

        public int getSlot() {
            return this.slot;
        }
    }

    private class AttributeWithValue {
        private final AttributeType attributeType;
        private final double value;
        private final ItemStackWithSlot itemStackWithSlot;

        public AttributeWithValue(AttributeType attributeType, double value, ItemStackWithSlot itemStackWithSlot) {
            this.attributeType = attributeType;
            this.value = value;
            this.itemStackWithSlot = itemStackWithSlot;
        }

        public AttributeType getAttributeType() {
            return this.attributeType;
        }

        public double getValue() {
            return this.value;
        }

        public ItemStackWithSlot getItemStackWithSlot() {
            return this.itemStackWithSlot;
        }

        public double getReverseValue() {
            return -this.value;
        }
    }
}

