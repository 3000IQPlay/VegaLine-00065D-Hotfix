package ru.govno.client.module.modules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.gui.GuiBossOverlay;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemArrow;
import net.minecraft.item.ItemEnchantedBook;
import net.minecraft.item.ItemEnderEye;
import net.minecraft.item.ItemEnderPearl;
import net.minecraft.item.ItemRedstone;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayerAbilities;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.BossInfo;
import ru.govno.client.Client;
import ru.govno.client.event.EventTarget;
import ru.govno.client.event.events.EventSendPacket;
import ru.govno.client.module.Module;
import ru.govno.client.module.modules.AirJump;
import ru.govno.client.module.modules.Bypass;
import ru.govno.client.module.modules.ClientTune;
import ru.govno.client.module.modules.FreeCam;
import ru.govno.client.module.modules.ProContainer;
import ru.govno.client.module.modules.Strafe;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.module.settings.ModeSettings;
import ru.govno.client.utils.Math.MathUtils;
import ru.govno.client.utils.Math.TimerHelper;
import ru.govno.client.utils.Movement.MoveMeHelp;

public class StormHVHHelper
extends Module {
    public static StormHVHHelper get;
    BoolSettings AutoDuel;
    BoolSettings AutoResellDuel;
    BoolSettings OnlySneakDuel;
    BoolSettings SmartDuel;
    BoolSettings AutoKitSelect;
    BoolSettings NoPlayersOnSpawn;
    BoolSettings NoSpectate;
    BoolSettings AllowRtpType;
    BoolSettings AutoHeal;
    BoolSettings AutoFixall;
    BoolSettings AutoFlyMode;
    BoolSettings AutoNear;
    BoolSettings AutoStrafeSet;
    BoolSettings AbuseFlyMode;
    ModeSettings DuelType;
    ModeSettings KitSelect;
    private static final List<BPWID> verifyBlocks;
    private static final List<BPWID> verifyBlocks2;
    boolean goSword = false;
    boolean goDuel = false;
    TimerHelper waitResell = TimerHelper.TimerHelperReseted();
    TimerHelper waitHeal = TimerHelper.TimerHelperReseted();
    TimerHelper waitFixall = TimerHelper.TimerHelperReseted();
    TimerHelper waitFlyMode = TimerHelper.TimerHelperReseted();
    TimerHelper waitNear = TimerHelper.TimerHelperReseted();
    TimerHelper waitPvpTime = TimerHelper.TimerHelperReseted();
    TimerHelper waitPvpTime2 = TimerHelper.TimerHelperReseted();
    boolean runEQ = false;
    List<ItemStackInfo> findArmor = new ArrayList<ItemStackInfo>();
    private String RTPTYPE;
    private int rtpWaitTicks = 0;

    public StormHVHHelper() {
        super("StormHVHHelper", 0, Module.Category.MISC);
        this.AutoDuel = new BoolSettings("AutoDuel", true, this);
        this.settings.add(this.AutoDuel);
        this.AutoResellDuel = new BoolSettings("AutoResellDuel", true, this, () -> this.AutoDuel.getBool());
        this.settings.add(this.AutoResellDuel);
        this.OnlySneakDuel = new BoolSettings("OnlySneakDuel", true, this, () -> this.AutoDuel.getBool());
        this.settings.add(this.OnlySneakDuel);
        this.SmartDuel = new BoolSettings("SmartDuel", true, this, () -> this.AutoDuel.getBool());
        this.settings.add(this.SmartDuel);
        String[] kits = new String[]{"Standart", "Thorns", "MSTNW", "Shield", "NetheriteOP", "Reallyworld", "Sunrise", "Crystals", "Craftyou", "Prostocraft"};
        this.DuelType = new ModeSettings("DuelType", kits[7], this, kits, () -> this.AutoDuel.getBool());
        this.settings.add(this.DuelType);
        this.AutoKitSelect = new BoolSettings("AutoKitSelect", true, this);
        this.settings.add(this.AutoKitSelect);
        this.KitSelect = new ModeSettings("KitSelect", "Duped", this, new String[]{"Duped", "Standart"}, () -> this.AutoKitSelect.getBool());
        this.settings.add(this.KitSelect);
        this.NoPlayersOnSpawn = new BoolSettings("NoPlayersOnSpawn", false, this);
        this.settings.add(this.NoPlayersOnSpawn);
        this.NoSpectate = new BoolSettings("NoSpectate", true, this);
        this.settings.add(this.NoSpectate);
        this.AllowRtpType = new BoolSettings("AllowRtpType", true, this);
        this.settings.add(this.AllowRtpType);
        this.AutoHeal = new BoolSettings("AutoHeal", true, this);
        this.settings.add(this.AutoHeal);
        this.AutoFixall = new BoolSettings("AutoFixall", true, this);
        this.settings.add(this.AutoFixall);
        this.AutoFlyMode = new BoolSettings("AutoFlyMode", true, this);
        this.settings.add(this.AutoFlyMode);
        this.AutoNear = new BoolSettings("AutoNear", true, this);
        this.settings.add(this.AutoNear);
        this.AutoStrafeSet = new BoolSettings("AutoStrafeSet", true, this);
        this.settings.add(this.AutoStrafeSet);
        this.AbuseFlyMode = new BoolSettings("AbuseFlyMode", false, this);
        this.settings.add(this.AbuseFlyMode);
        get = this;
    }

    public static boolean noRenderPlayersInWorld() {
        return get.isActived() && StormHVHHelper.get.NoPlayersOnSpawn.getBool();
    }

    private boolean canSelectKit(boolean dupe) {
        return Minecraft.player.inventory.getStackInSlot(dupe ? 0 : 8).getItem() == Items.ENCHANTED_BOOK;
    }

    private boolean canDisband() {
        return Minecraft.player.inventory.getStackInSlot(7).getItem() == Items.REDSTONE;
    }

    private boolean hasGroup() {
        return Minecraft.player.inventory.getStackInSlot(8).getItem() == Items.CLOCK && Minecraft.player.inventory.getStackInSlot(7).getItem() == Items.REDSTONE && Minecraft.player.inventory.getStackInSlot(6).getItem() == Items.PAPER && Minecraft.player.inventory.getStackInSlot(0).getItem() == Items.DIAMOND_SWORD;
    }

    private void disbandGroup() {
        int slot = 7;
        if (this.canDisband()) {
            if (Minecraft.player.inventory.currentItem != slot) {
                Minecraft.player.connection.sendPacket(new CPacketHeldItemChange(slot));
            }
            Minecraft.player.connection.sendPacket(new CPacketPlayerTryUseItem(EnumHand.MAIN_HAND));
            if (Minecraft.player.inventory.currentItem != slot) {
                Minecraft.player.connection.sendPacket(new CPacketHeldItemChange(Minecraft.player.inventory.currentItem));
            }
        }
    }

    private void selectKitAuto(boolean dupe) {
        int slot;
        int n = slot = dupe ? 0 : 8;
        if (this.canSelectKit(dupe)) {
            if (Minecraft.player.inventory.currentItem != slot) {
                Minecraft.player.connection.sendPacket(new CPacketHeldItemChange(slot));
            }
            Minecraft.player.connection.sendPacket(new CPacketPlayerTryUseItem(EnumHand.MAIN_HAND));
            if (Minecraft.player.inventory.currentItem != slot) {
                Minecraft.player.connection.sendPacket(new CPacketHeldItemChange(Minecraft.player.inventory.currentItem));
            }
        }
    }

    private boolean canSpectateLeave(boolean leave) {
        return leave && Block.getBlockById(Item.getIdFromItem(Minecraft.player.inventory.getStackInSlot(8).getItem())).toString().contains("minecraft:red_flower");
    }

    private void clickSpectateLeave(boolean leave) {
        if (this.canSpectateLeave(leave)) {
            Minecraft.player.connection.sendPacket(new CPacketHeldItemChange(8));
            Minecraft.player.connection.sendPacket(new CPacketPlayerTryUseItem(EnumHand.MAIN_HAND));
            Minecraft.player.connection.sendPacket(new CPacketHeldItemChange(Minecraft.player.inventory.currentItem));
        }
    }

    private boolean isAutoDuel() {
        return this.AutoDuel.getBool();
    }

    private boolean isAutoHeal() {
        return this.AutoHeal.getBool() && (Minecraft.player.getFoodStats().getFoodLevel() < 20 || Minecraft.player.getHealth() < Minecraft.player.getMaxHealth() / 2.0f);
    }

    private boolean isAutoFixall() {
        if (this.AutoFixall.getBool()) {
            ItemStack stack;
            int i;
            for (i = 0; i < 4; ++i) {
                stack = Minecraft.player.inventory.armorItemInSlot(i);
                if (stack.isEmpty() || !stack.isItemStackDamageable() || !((float)stack.getItemDamage() / (float)stack.getMaxDamage() > 0.046f)) continue;
                return true;
            }
            for (i = 0; i < 36; ++i) {
                stack = Minecraft.player.inventory.getStackInSlot(i);
                if (stack.isEmpty() || !stack.isItemStackDamageable() || !((float)stack.getItemDamage() / (float)stack.getMaxDamage() > 0.1f)) continue;
                return true;
            }
        }
        return false;
    }

    private boolean isAutoFlyMode() {
        boolean can = false;
        if (this.AutoFlyMode.getBool() && !Minecraft.player.capabilities.allowFlying) {
            can = !Minecraft.player.getDisplayName().getUnformattedText().equalsIgnoreCase(Minecraft.player.getName());
        }
        return can;
    }

    private boolean isAutoNear() {
        return this.AutoNear.getBool() && Minecraft.player.capabilities.allowFlying && Minecraft.player.capabilities.isFlying && Minecraft.player.getHealth() >= Minecraft.player.getMaxHealth() - 1.0f && MoveMeHelp.isMoving() && Minecraft.player.getHeldItemMainhand().getItem() == Items.DIAMOND_SWORD && Minecraft.player.getHeldItemOffhand().getItem() != Items.air && Minecraft.player.dimension == 0 && StormHVHHelper.mc.world.getLoadedEntityList().stream().map(Entity::getOtherPlayerOf).filter(Objects::nonNull).filter(player -> (double)player.getDistanceToEntity(Minecraft.player) < 38.0 && player.canEntityBeSeen(Minecraft.player)).filter(player -> !Client.friendManager.isFriend(player.getName())).toList().isEmpty();
    }

    private boolean canClickSword() {
        return Minecraft.player.inventory.getStackInSlot(0).getItem() == Items.DIAMOND_SWORD;
    }

    private void clickSword() {
        if (this.canClickSword()) {
            Minecraft.player.connection.sendPacket(new CPacketHeldItemChange(0));
            Minecraft.player.connection.sendPacket(new CPacketPlayerTryUseItem(EnumHand.MAIN_HAND));
            Minecraft.player.connection.sendPacket(new CPacketHeldItemChange(Minecraft.player.inventory.currentItem));
        }
    }

    private boolean canClickRedstone() {
        return Minecraft.player.inventory.getStackInSlot(8).getItem() == Items.REDSTONE;
    }

    private void clickRedstone() {
        if (this.canClickRedstone()) {
            if (Minecraft.player.inventory.currentItem != 8) {
                Minecraft.player.connection.sendPacket(new CPacketHeldItemChange(8));
            }
            Minecraft.player.connection.sendPacket(new CPacketPlayerTryUseItem(EnumHand.MAIN_HAND));
            if (Minecraft.player.inventory.currentItem != 8) {
                Minecraft.player.connection.sendPacket(new CPacketHeldItemChange(Minecraft.player.inventory.currentItem));
            }
        }
    }

    private boolean canClickSlot(String duelType, boolean smart) {
        return this.getSlotByDuel(duelType, smart) != -1;
    }

    private int getSlotByDuel(String duelType, boolean smart) {
        int slot = -1;
        int smartSlot = -1;
        for (int index = 0; index < Minecraft.player.openContainer.inventorySlots.size(); ++index) {
            ItemStack stack;
            Item item;
            if (!Minecraft.player.openContainer.inventorySlots.get(index).getHasStack() || (item = (stack = Minecraft.player.openContainer.inventorySlots.get(index).getStack()).getItem()) == Items.air || item == Item.getItemFromBlock(Blocks.STAINED_GLASS_PANE) || item == Items.ARROW || stack.stackSize != 2) continue;
            smartSlot = index;
        }
        if (smart && smartSlot != -1) {
            slot = smartSlot;
        } else {
            switch (duelType) {
                case "Standart": {
                    slot = 20;
                    break;
                }
                case "Thorns": {
                    slot = 21;
                    break;
                }
                case "MSTNW": {
                    slot = 22;
                    break;
                }
                case "Shield": {
                    slot = 23;
                    break;
                }
                case "NetheriteOP": {
                    slot = 24;
                    break;
                }
                case "Reallyworld": {
                    slot = 29;
                    break;
                }
                case "Sunrise": {
                    slot = 30;
                    break;
                }
                case "Crystals": {
                    slot = 31;
                    break;
                }
                case "Craftyou": {
                    slot = 32;
                    break;
                }
                case "Prostocraft": {
                    slot = 33;
                }
            }
        }
        return slot;
    }

    private void clickSlot(int slot) {
        StormHVHHelper.mc.playerController.windowClick(Minecraft.player.openContainer.windowId, slot, 1, ClickType.PICKUP, Minecraft.player);
        Minecraft.player.closeScreen();
        StormHVHHelper.mc.currentScreen = null;
    }

    private boolean isInSelectDuelMenu() {
        boolean hasArrow1 = false;
        int countGlass = 0;
        if (StormHVHHelper.mc.currentScreen instanceof GuiContainer && !(StormHVHHelper.mc.currentScreen instanceof GuiInventory) && Minecraft.player.openContainer instanceof ContainerChest && Minecraft.player.openContainer.inventorySlots.size() == 90) {
            for (int index = 0; index < Minecraft.player.openContainer.inventorySlots.size(); ++index) {
                if (!Minecraft.player.openContainer.inventorySlots.get(index).getHasStack()) continue;
                ItemStack stack = Minecraft.player.openContainer.inventorySlots.get(index).getStack();
                Item item = stack.getItem();
                if (item == Item.getItemFromBlock(Blocks.STAINED_GLASS_PANE) && stack.stackSize == 1) {
                    ++countGlass;
                }
                if (!(item instanceof ItemArrow) || stack.stackSize != 1) continue;
                hasArrow1 = true;
            }
        }
        return countGlass == 24 && hasArrow1;
    }

    public static boolean isInStormServer() {
        return !mc.isSingleplayer() && mc.getCurrentServerData() != null && StormHVHHelper.mc.getCurrentServerData().serverIP != null && (StormHVHHelper.mc.getCurrentServerData().serverIP.toLowerCase().contains("stormhvh") || StormHVHHelper.mc.getCurrentServerData().serverIP.toLowerCase().contains("galaxyhvh"));
    }

    private static boolean hasVoidBedrock() {
        return StormHVHHelper.mc.world.getBlockState(new BlockPos(Minecraft.player.posX, 0.0, Minecraft.player.posZ)).getBlock() == Blocks.BEDROCK;
    }

    private static boolean hasBlockVerifyInWorld(BPWID bpwid) {
        IBlockState state;
        boolean has = false;
        if (bpwid != null && bpwid.getPos() != null && Block.getIdFromBlock((state = StormHVHHelper.mc.world.getBlockState(bpwid.getPos())).getBlock()) == bpwid.getCurID()) {
            has = true;
        }
        return has;
    }

    public static boolean isInStormSpawn(boolean isDuelsSpawn) {
        return isDuelsSpawn || !StormHVHHelper.hasVoidBedrock() && verifyBlocks2.stream().anyMatch(StormHVHHelper::hasBlockVerifyInWorld);
    }

    public static boolean isInStormDuelsSpawn() {
        return !StormHVHHelper.hasVoidBedrock() && verifyBlocks.stream().anyMatch(StormHVHHelper::hasBlockVerifyInWorld);
    }

    private boolean inPvpTime() {
        return !GuiBossOverlay.mapBossInfos2.isEmpty() && !GuiBossOverlay.mapBossInfos2.values().stream().map(BossInfo::getName).map(ITextComponent::getUnformattedText).map(String::toLowerCase).filter(name -> name.contains("pvp") || name.contains("\u043f\u0432\u043f") || name.contains("\u0441\u0435\u043a.")).filter(Objects::nonNull).toList().isEmpty();
    }

    private boolean compareItemStacksInfos(ItemStackInfo first, ItemStackInfo second) {
        int[] args = new int[]{first.stacksize == second.stacksize ? 1 : 0, first.type == second.type ? 1 : 0, first.itemId == second.itemId ? 1 : 0, first.enchHashes == second.enchHashes ? 1 : 0, first.displayInt == second.displayInt ? 1 : 0};
        return Arrays.stream(args).allMatch(arg -> arg == 1);
    }

    private boolean compareItemStacksInfos(ItemStackInfo first, ItemStack second1) {
        ItemStackInfo second = new ItemStackInfo(second1);
        return this.compareItemStacksInfos(first, second);
    }

    @Override
    public void onUpdate() {
        boolean hasStormServer = isInStormServer();
        boolean[] isOnSpawn = new boolean[2];
        if (hasStormServer) {
            isOnSpawn[0] = isInStormDuelsSpawn();
            isOnSpawn[1] = isInStormSpawn(isOnSpawn[0]);
        }

        if (hasStormServer && isOnSpawn[1] && noRenderPlayersInWorld() && mc.world != null) {
            for (Entity entity : mc.world.getLoadedEntityList()) {
                if (entity instanceof EntityOtherPlayerMP) {
                    EntityOtherPlayerMP mp = (EntityOtherPlayerMP)entity;
                    if (mp != FreeCam.fakePlayer && mp.getEntityId() != 462462998 && !Client.friendManager.isFriend(mp.getName())) {
                        mc.world.removeEntityFromWorld(mp.getEntityId());
                    }
                }
            }

            mc.world.playerEntities.clear();
        }

        if (this.AutoKitSelect.getBool()
                && hasStormServer
                && !isOnSpawn[0]
                && mc.currentScreen == null
                && this.canSelectKit(this.KitSelect.currentMode.equalsIgnoreCase("Duped"))) {
            this.selectKitAuto(this.KitSelect.currentMode.equalsIgnoreCase("Duped"));
        }

        if (this.isAutoDuel()
                && hasStormServer
                && isOnSpawn[0]
                && this.canClickSword()
                && mc.currentScreen == null
                && (!this.OnlySneakDuel.getBool() || Minecraft.player.isSneaking())) {
            if (this.canDisband()) {
                this.disbandGroup();
            } else {
                this.goSword = true;
            }
        }

        if (this.goSword) {
            this.clickSword();
            this.goDuel = true;
            this.goSword = false;
        }

        this.clickSpectateLeave(this.NoSpectate.getBool());
        if (this.goDuel && this.isInSelectDuelMenu() && this.canClickSlot(this.DuelType.currentMode, this.SmartDuel.getBool())) {
            this.clickSlot(this.getSlotByDuel(this.DuelType.currentMode, this.SmartDuel.getBool()));
        }

        if (this.AutoResellDuel.getBool()
                && this.AutoDuel.getBool()
                && mc.currentScreen == null
                && (!this.OnlySneakDuel.getBool() || Minecraft.player.isSneaking())) {
            if (Minecraft.player.inventory.getStackInSlot(8).getItem() == Items.REDSTONE) {
                if (this.waitResell.hasReached(200.0)) {
                    this.clickRedstone();
                    this.waitResell.reset();
                }
            } else {
                this.waitResell.reset();
            }
        }

        if (!this.isInSelectDuelMenu() && (Minecraft.player.inventory.getStackInSlot(8).getItem() instanceof ItemRedstone || !isOnSpawn[0])) {
            this.goDuel = false;
        }

        if (this.rtpWaitTicks > 0) {
            if (this.RTPTYPE != null && this.isInRtpSelectGui()) {
                this.selectRtpTypeInMenu(this.RTPTYPE, true);
                this.RTPTYPE = null;
            }

            this.rtpWaitTicks--;
        } else {
            this.RTPTYPE = null;
        }

        boolean inPvpTime = this.inPvpTime();
        boolean damaged = Minecraft.player.hurtTime != 0;
        if (inPvpTime) {
            this.waitPvpTime.reset();
        }

        if (damaged) {
            this.waitPvpTime2.reset();
        }

        inPvpTime = !this.waitPvpTime.hasReached(150.0) || !this.waitPvpTime2.hasReached(5000.0);
        if (hasStormServer && this.isAutoHeal() && this.waitHeal.hasReached(60500.0) && !inPvpTime) {
            mc.getConnection().sendPacket(new CPacketChatMessage("/heal"));
            this.waitHeal.reset();
            this.waitNear.reset();
        }

        if (hasStormServer && (this.isAutoFixall() || this.runEQ) && !inPvpTime && Minecraft.player.openContainer instanceof ContainerPlayer inventoryContiner) {
            if (this.waitFixall.hasReached(250.0)) {
                if (!this.findArmor.isEmpty()) {
                    for (int slotI = 0; slotI < 36; slotI++) {
                        ItemStack stack = Minecraft.player.inventory.getStackInSlot(slotI);
                        if (stack.getItem() instanceof ItemArmor) {
                            boolean ae = this.findArmor.stream().anyMatch(stackFind -> this.compareItemStacksInfos(stackFind, stack));
                            if (ae) {
                                mc.playerController.windowClick(0, slotI, 1 - slotI % 2, ClickType.QUICK_MOVE, Minecraft.player);
                            }
                        }
                    }
                }

                ProContainer.autoArmorOFF = true;
                this.findArmor.clear();
                this.runEQ = false;
            }

            if (this.waitFixall.hasReached(60500.0)) {
                if (!mc.world
                        .getLoadedEntityList()
                        .stream()
                        .map(Entity::getOtherPlayerOf)
                        .filter(Objects::nonNull)
                        .filter(
                                player -> (double)player.getDistanceToEntity(Minecraft.player) < 7.0
                                        && player.getTotalArmorValue() != 0
                                        && !Client.friendManager.isFriend(player.getName())
                        )
                        .toList()
                        .isEmpty()) {
                    return;
                }

                int emptySlots = 0;
                int slotIx = 0;

                for (ItemStack stack : inventoryContiner.getInventory()) {
                    if (slotIx >= 9 && slotIx <= 44 && stack.getItem() == Items.air) {
                        if (++emptySlots == 4) {
                            break;
                        }
                    }

                    slotIx++;
                }

                slotIx = 0;
                if (emptySlots == 4 && !this.runEQ) {
                    for (ItemStack stack : inventoryContiner.getInventory()) {
                        if (slotIx >= 5 && slotIx < 5 + emptySlots && stack.getItem() instanceof ItemArmor && stack.isItemDamaged()) {
                            this.findArmor.add(new StormHVHHelper.ItemStackInfo(stack));
                            mc.playerController.windowClick(0, slotIx, 1 - slotIx % 2, ClickType.QUICK_MOVE, Minecraft.player);
                        }

                        slotIx++;
                    }

                    mc.getConnection().sendPacket(new CPacketChatMessage("/fix all"));
                    this.runEQ = true;
                    this.waitFixall.reset();
                    this.waitNear.reset();
                }
            }
        }

        if (hasStormServer && this.isAutoFlyMode() && this.waitFlyMode.hasReached(1300.0) && !inPvpTime) {
            mc.getConnection().sendPacket(new CPacketChatMessage("/fly"));
            boolean prevFly = Minecraft.player.capabilities.isFlying;
            Minecraft.player.capabilities.isFlying = true;
            mc.getConnection().sendPacket(new CPacketPlayerAbilities(Minecraft.player.capabilities));
            Minecraft.player.capabilities.isFlying = prevFly;
            this.waitFlyMode.reset();
            this.waitNear.reset();
        }

        if (hasStormServer && this.isAutoNear() && this.waitNear.hasReached(1500.0) && !inPvpTime) {
            mc.getConnection().sendPacket(new CPacketChatMessage("/near"));
            this.waitNear.reset();
        }

        boolean canStrict = false;
        if (hasStormServer && this.AbuseFlyMode.getBool()) {
            boolean hasBypass = Bypass.get.isActived() && Bypass.get.GMFlySpoofIfCan.getBool();
            if (!hasBypass) {
                this.AbuseFlyMode.setBool(false);
                ClientTune.get.playGuiScreenCheckBox(true);
                Client.msg("§f§lModules:§r §7[§lStormHVHHelper§r§7]: для абуза флай мода:", false);
                Client.msg("§f§lModules:§r §7[§lStormHVHHelper§r§7]: включите модуль Bypass.", false);
                Client.msg("§f§lModules:§r §7[§lStormHVHHelper§r§7]: включите в нём чек GMFlySpoofIfCan.", false);
            } else if (Minecraft.player.capabilities.allowFlying && !Minecraft.player.capabilities.isFlying && !inPvpTime) {
                canStrict = true;
                if (Minecraft.player.onGround && !Minecraft.player.isJumping()) {
                    if (Strafe.get.actived && Strafe.get.Mode.currentMode.equalsIgnoreCase("Strict")) {
                        Minecraft.player.onGround = MoveMeHelp.getSpeed() < 0.16F;
                    }

                    MoveMeHelp.setSpeed(MathUtils.clamp(MoveMeHelp.getSpeed() * (double)(Minecraft.player.onGround ? 1.5F : 5.0F), 0.26, 1.0));
                } else {
                    Minecraft.player.jumpMovementFactor *= 3.7F;
                    if (Minecraft.player.isMoving()) {
                        MoveMeHelp.setCuttingSpeed(MoveMeHelp.getSpeed() * 1.2 / 1.06);
                    }
                }
            }
        }

        if (hasStormServer && this.AutoStrafeSet.getBool()) {
            String strafeMode = Strafe.get.Mode.currentMode;
            String current = Minecraft.player.capabilities.allowFlying && !inPvpTime ? (canStrict ? "Strict" : "Matrix5") : "Matrix&AAC";
            if (!strafeMode.equalsIgnoreCase(current)) {
                if (mc.currentScreen == Client.clickGuiScreen) {
                    Client.msg("§f§lModules:§r §7[§lStormHVHHelper§r§7]: для смены мода Strafe", false);
                    Client.msg("§f§lModules:§r §7[§lStormHVHHelper§r§7]: отключите чек AutoStrafeSet.", false);
                }

                Strafe.get.Mode.setMode(current);
            }

            strafeMode = AirJump.get.Mode.currentMode;
            current = Minecraft.player.capabilities.allowFlying && !inPvpTime ? (canStrict ? "Default" : "Matrix") : "Matrix2";
            if (!strafeMode.equalsIgnoreCase(current)) {
                if (mc.currentScreen == Client.clickGuiScreen) {
                    Client.msg("§f§lModules:§r §7[§lStormHVHHelper§r§7]: для смены мода AirJump", false);
                    Client.msg("§f§lModules:§r §7[§lStormHVHHelper§r§7]: отключите чек AutoStrafeSet.", false);
                }

                AirJump.get.Mode.setMode(current);
            }
        }
    }

    @EventTarget
    public void onSendPacket(EventSendPacket event) {
        Packet packet;
        if (this.actived && (packet = event.getPacket()) instanceof CPacketChatMessage) {
            CPacketChatMessage chatPacket = (CPacketChatMessage)packet;
            if (this.actived && this.RTPTYPE == null && this.AllowRtpType.getBool() && StormHVHHelper.isInStormServer()) {
                this.RTPTYPE = null;
                String msg = chatPacket.getMessage();
                if (msg.toLowerCase().startsWith("/rtp")) {
                    switch (msg.toLowerCase()) {
                        case "/rtp": {
                            this.RTPTYPE = "normal";
                            break;
                        }
                        case "/rtp far": {
                            this.RTPTYPE = "far";
                            break;
                        }
                        case "/rtp near": {
                            this.RTPTYPE = "near";
                        }
                    }
                    if (Minecraft.player.experienceLevel < 1 && this.RTPTYPE != null && !this.RTPTYPE.equalsIgnoreCase("normal")) {
                        Client.msg("\u00a7f\u00a7lModules:\u00a7r \u00a77[\u00a7l" + this.name + "\u00a7r\u00a77]: \u0423 \u0432\u0430\u0441 \u043d\u043a\u0435 \u0445\u0432\u0430\u0442\u0430\u0435\u0442 \u0443\u0440\u043e\u0432\u043d\u044f \u043e\u043f\u044b\u0442\u0430 \u0434\u043b\u044f \u0440\u0442\u043f.", false);
                        event.cancel();
                    } else if (this.RTPTYPE != null) {
                        chatPacket.setMessage("/rtp");
                        this.rtpWaitTicks = 12;
                    }
                }
            }
        }
    }

    private boolean isInRtpSelectGui() {
        int countGlass = 0;
        int hasProp = 0;
        int airCount = 0;
        Container container = Minecraft.player.openContainer;
        if (container instanceof ContainerChest) {
            ContainerChest chest = (ContainerChest)container;
            if (chest.inventorySlots.size() == 81) {
                for (int index = 0; index < 45; ++index) {
                    ItemStack stack = ((Slot)chest.inventorySlots.get(index)).getStack();
                    if (stack == null) continue;
                    Item item = stack.getItem();
                    if (stack.stackSize != 1) continue;
                    if (item == Item.getItemFromBlock(Blocks.STAINED_GLASS_PANE)) {
                        ++countGlass;
                        continue;
                    }
                    if (item instanceof ItemEnderPearl || item instanceof ItemEnderEye || item instanceof ItemArrow) {
                        ++hasProp;
                        continue;
                    }
                    if (item != Items.air) continue;
                    ++airCount;
                }
            }
        }
        return countGlass == 10 && hasProp == 3 && airCount == 31;
    }

    private void selectRtpTypeInMenu(String rtpType, boolean msg) {
        int slot = -1;
        switch (rtpType) {
            case "normal": {
                slot = 20;
                break;
            }
            case "far": {
                slot = 22;
                break;
            }
            case "near": {
                slot = 24;
            }
        }
        if (slot != -1) {
            this.clickSlot(slot);
            if (msg) {
                Client.msg("\u00a7f\u00a7lModules:\u00a7r \u00a77[\u00a7l" + this.name + "\u00a7r\u00a77]: \u041f\u043e\u043c\u043e\u0433\u0430\u044e \u0438\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u0442\u044c /rtp " + rtpType + ".", false);
            }
        }
    }

    static {
        verifyBlocks = Arrays.asList(new BPWID(new BlockPos(-30, 63, 40), 35), new BPWID(new BlockPos(-77, 63, 40), 188), new BPWID(new BlockPos(-43, 76, 98), 251), new BPWID(new BlockPos(-2, 61, 104), 2), new BPWID(new BlockPos(-65, 58, 160), 38), new BPWID(new BlockPos(-118, 57, 108), 2), new BPWID(new BlockPos(-128, 57, 2), 35), new BPWID(new BlockPos(-53, 76, -41), 35), new BPWID(new BlockPos(-5, 59, -14), 251), new BPWID(new BlockPos(-36, 78, -45), 251), new BPWID(new BlockPos(-21, 64, -40), 252), new BPWID(new BlockPos(-78, 63, -40), 252));
        verifyBlocks2 = Arrays.asList(new BPWID(new BlockPos(8, 87, 8), 138), new BPWID(new BlockPos(8, 84, -21), 99), new BPWID(new BlockPos(8, 87, -35), 130), new BPWID(new BlockPos(7, 74, -74), 35), new BPWID(new BlockPos(8, 48, -112), 12), new BPWID(new BlockPos(6, 115, 49), 2), new BPWID(new BlockPos(8, 110, 83), 161), new BPWID(new BlockPos(56, 89, -21), 1), new BPWID(new BlockPos(66, 57, -95), 35), new BPWID(new BlockPos(121, 71, -46), 2), new BPWID(new BlockPos(108, 50, 29), 2), new BPWID(new BlockPos(86, 73, 101), 35), new BPWID(new BlockPos(21, 58, 91), 31), new BPWID(new BlockPos(-70, 113, 23), 31), new BPWID(new BlockPos(-62, 79, -53), 24), new BPWID(new BlockPos(-39, 54, -104), 251));
    }

    private static class BPWID {
        private final BlockPos pos;
        private final int curID;

        public BlockPos getPos() {
            return this.pos;
        }

        public int getCurID() {
            return this.curID;
        }

        public BPWID(BlockPos pos, int curID) {
            this.pos = pos;
            this.curID = curID;
        }
    }

    private class ItemStackInfo {
        int stacksize = 0;
        int type = 0;
        int itemId = 0;
        int enchHashes = 0;
        int displayInt = 0;

        public ItemStackInfo(ItemStack stack) {
            this.stacksize = stack.stackSize;
            this.itemId = Item.getIdFromItem(stack.getItem());
            NBTTagList nbttaglist = stack.getItem() == Items.ENCHANTED_BOOK ? ItemEnchantedBook.getEnchantments(stack) : stack.getEnchantmentTagList();
            for (int i = 0; i < nbttaglist.tagCount(); ++i) {
                NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
                Enchantment enchantment = Enchantment.getEnchantmentByID(nbttagcompound.getShort("id"));
                if (enchantment == null) continue;
                if (enchantment.type == EnumEnchantmentType.ARMOR) {
                    this.enchHashes += String.valueOf(enchantment.hashCode()).length();
                }
                if (enchantment.type == null) continue;
                this.type = String.valueOf(enchantment.type.name().hashCode()).length();
            }
            for (char ch : stack.getDisplayName().toCharArray()) {
                this.displayInt += String.valueOf(String.valueOf(ch).hashCode()).length();
            }
        }
    }
}

