package ru.govno.client.module.modules;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import net.minecraft.block.BlockRedstoneLight;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.item.Item;
import net.minecraft.item.ItemSkull;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.WorldType;
import ru.govno.client.Client;
import ru.govno.client.event.EventTarget;
import ru.govno.client.event.events.EventReceivePacket;
import ru.govno.client.module.Module;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.module.settings.ModeSettings;
import ru.govno.client.utils.Math.TimerHelper;

public class InfHVHHelper
extends Module {
    private final BoolSettings AutoDuel;
    private final BoolSettings AutoDuelOnlySneak;
    private final BoolSettings SmartKit;
    private final BoolSettings AutoDuelResell;
    private final BoolSettings NoPlayersOnSpawn;
    private final BoolSettings NoTrashMessages;
    private final ModeSettings Kit;
    private final ModeSettings SelectedKitMode;
    private final TimerHelper WAIT_FOR_RESELL = new TimerHelper();
    private final TimerHelper WAIT_FOR_HOTKIT = new TimerHelper();

    public InfHVHHelper() {
        super("InfHVHHelper", 0, Module.Category.MISC);
        this.AutoDuel = new BoolSettings("AutoDuel", true, this);
        this.settings.add(this.AutoDuel);
        this.AutoDuelOnlySneak = new BoolSettings("AutoDuelOnlySneak", true, this, () -> this.AutoDuel.getBool());
        this.settings.add(this.AutoDuelOnlySneak);
        String[] kitsModes = new String[]{"Standart", "Crystal", "Elytra", "Totem", "NoDebuff", "Axe", "EasyGrief", "ReallyWorld", "FunTime"};
        this.Kit = new ModeSettings("Kit", kitsModes[3], this, kitsModes, () -> this.AutoDuel.getBool());
        this.settings.add(this.Kit);
        this.SmartKit = new BoolSettings("SmartKit", false, this);
        this.settings.add(this.SmartKit);
        this.AutoDuelResell = new BoolSettings("AutoDuelResell", true, this, () -> this.AutoDuel.getBool());
        this.settings.add(this.AutoDuelResell);
        this.SelectedKitMode = new ModeSettings("SelectedKitMode", "NotAuto", this, new String[]{"NotAuto", "Normal", "Duped"});
        this.settings.add(this.SelectedKitMode);
        this.NoPlayersOnSpawn = new BoolSettings("NoPlayersOnSpawn", false, this);
        this.settings.add(this.NoPlayersOnSpawn);
        this.NoTrashMessages = new BoolSettings("NoTrashMessages", true, this);
        this.settings.add(this.NoTrashMessages);
    }

    @EventTarget
    public void onPreReceivePackets(EventReceivePacket eventPacket) {
        Packet packet = eventPacket.getPacket();
        if (packet instanceof SPacketChat) {
            String componentText;
            ITextComponent component;
            SPacketChat chatPacket = (SPacketChat)packet;
            if (this.NoTrashMessages.getBool() && (component = chatPacket.chatComponent) != null && (componentText = component.getUnformattedText()) != null && !componentText.isEmpty()) {
                List<String> bad_strings = Arrays.asList("\u250f  ", "| \u041f\u043e\u0434\u043f\u0438\u0448\u0438\u0441\u044c \u043d\u0430 \u043d\u0430\u0448\u0443 \u0433\u0440\u0443\u043f\u043f\u0443 \u0412\u041a, \u0447\u0442\u043e\u0431\u044b \u0431\u044b\u0442\u044c \u0432\u043a\u0443\u0440\u0441\u0435 \u0432\u0441\u0435\u0445 \u0441\u043e\u0431\u044b\u0442\u0438\u0439", "| \u041d\u0430\u0448\u0430 \u0433\u0440\u0443\u043f\u043f\u0430 \u0422\u0413 \u043a\u0430\u043d\u0430\u043b: t.me/infinityhvh_tg", "| \u041d\u0430\u0448 \u0441\u0430\u0439\u0442: InfinityHvH.com", "\u2517  ", "| \u041d\u0435 \u0437\u0430\u0431\u0443\u0434\u044c \u043f\u0440\u043e\u0432\u0435\u0440\u0438\u0442\u044c \u0441\u0432\u043e\u044e \u0441\u0442\u0430\u0442\u0438\u0441\u0442\u0438\u043a\u0443", "| \u0421 \u043f\u043e\u043c\u043e\u0449\u044c\u044e \u043a\u043e\u043c\u0430\u043d\u0434\u044b /stats", "| \u041e\u0431\u0435\u0437\u043e\u043f\u0430\u0441\u044c \u0441\u0432\u043e\u0439 \u0430\u043a\u043a\u0430\u0443\u043d\u0442 \u043e\u0442 \u0445\u0430\u043a\u0435\u0440\u043e\u0432", "| \u041f\u0440\u0438\u0432\u044f\u0436\u0438 \u0441\u0432\u043e\u0439 \u0412\u041a, \u0430 \u0442\u0430\u043a-\u0436\u0435 \u043f\u043e\u0434\u043a\u043b\u044e\u0447\u0438 \u0434\u0432\u0443\u0445\u044d\u0442\u0430\u043f\u043d\u0443\u044e \u0430\u0443\u0442\u0435\u043d\u0442\u0438\u0444\u0438\u043a\u0430\u0446\u0438\u044e \u043e\u0442 \u0412\u041a", "| \u041d\u0430\u0448\u0430 \u0433\u0440\u0443\u043f\u043f\u0430 \u0422\u0413 \u043a\u0430\u043d\u0430\u043b: t.me/infinityhvh_tg", "| \u041f\u043e\u0434\u043f\u0438\u0448\u0438\u0441\u044c \u043d\u0430 \u043d\u0430\u0448\u0443 \u0433\u0440\u0443\u043f\u043f\u0443 \u0412\u041a, \u0447\u0442\u043e\u0431\u044b \u0431\u044b\u0442\u044c \u0432\u043a\u0443\u0440\u0441\u0435 \u0432\u0441\u0435\u0445 \u0441\u043e\u0431\u044b\u0442\u0438\u0439", "| \u0421\u0443\u043c\u0430\u0441\u0448\u0435\u0434\u0448\u0438\u0435 \u0441\u043a\u0438\u0434\u043a\u0438 \u043d\u0430 \u0432\u0441\u0435 \u043f\u0440\u0438\u0432\u0438\u043b\u0435\u0433\u0438\u0438 \u0441\u0435\u0440\u0432\u0435\u0440\u0430", "[!] \u0418\u0437\u0432\u0438\u043d\u0438\u0442\u0435, \u043d\u043e \u0412\u044b \u043d\u0435 \u043c\u043e\u0436\u0435\u0442\u0435 \u0434\u0440\u0430\u0442\u044c\u0441\u044f \u0432 \u044d\u0442\u043e\u043c \u043c\u0435\u0441\u0442\u0435.", "\u239b!\u23a0 \u0412\u0430\u0448 \u043c\u0430\u0442\u0447 \u043d\u0430\u0447\u043d\u0451\u0442\u0441\u044f \u0447\u0435\u0440\u0435\u0437 4 \u0441\u0435\u043a\u0443\u043d\u0434", "\u239b!\u23a0 \u0412\u0430\u0448 \u043c\u0430\u0442\u0447 \u043d\u0430\u0447\u043d\u0451\u0442\u0441\u044f \u0447\u0435\u0440\u0435\u0437 3 \u0441\u0435\u043a\u0443\u043d\u0434", "\u239b!\u23a0 \u0412\u0430\u0448 \u043c\u0430\u0442\u0447 \u043d\u0430\u0447\u043d\u0451\u0442\u0441\u044f \u0447\u0435\u0440\u0435\u0437 2 \u0441\u0435\u043a\u0443\u043d\u0434", "\u239b!\u23a0 \u0412\u0430\u0448 \u043c\u0430\u0442\u0447 \u043d\u0430\u0447\u043d\u0451\u0442\u0441\u044f \u0447\u0435\u0440\u0435\u0437 1 \u0441\u0435\u043a\u0443\u043d\u0434", "\u239b!\u23a0 \u0412 \u0431\u043e\u0439!", "\u239b!\u23a0 \u0412\u044b \u0432\u044b\u0448\u043b\u0438 \u0438\u0437 \u043e\u0447\u0435\u0440\u0435\u0434\u0438!", "\u041f\u043e\u0434\u043e\u0436\u0434\u0438\u0442\u0435 3 \u0441\u0435\u043a\u0443\u043d\u0434, \u043f\u0440\u0435\u0436\u0434\u0435 \u0447\u0435\u043c \u043e\u0442\u043f\u0440\u0430\u0432\u043b\u044f\u0442\u044c \u0441\u043e\u043e\u0431\u0449\u0435\u043d\u0438\u0435 \u0432 \u044d\u0442\u043e\u0442 \u0447\u0430\u0442 \u0441\u043d\u043e\u0432\u0430.", "\u041f\u043e\u0434\u043e\u0436\u0434\u0438\u0442\u0435 2 \u0441\u0435\u043a\u0443\u043d\u0434, \u043f\u0440\u0435\u0436\u0434\u0435 \u0447\u0435\u043c \u043e\u0442\u043f\u0440\u0430\u0432\u043b\u044f\u0442\u044c \u0441\u043e\u043e\u0431\u0449\u0435\u043d\u0438\u0435 \u0432 \u044d\u0442\u043e\u0442 \u0447\u0430\u0442 \u0441\u043d\u043e\u0432\u0430.", "\u041f\u043e\u0434\u043e\u0436\u0434\u0438\u0442\u0435 1 \u0441\u0435\u043a\u0443\u043d\u0434, \u043f\u0440\u0435\u0436\u0434\u0435 \u0447\u0435\u043c \u043e\u0442\u043f\u0440\u0430\u0432\u043b\u044f\u0442\u044c \u0441\u043e\u043e\u0431\u0449\u0435\u043d\u0438\u0435 \u0432 \u044d\u0442\u043e\u0442 \u0447\u0430\u0442 \u0441\u043d\u043e\u0432\u0430.", "\u041f\u043e\u0434\u043e\u0436\u0434\u0438\u0442\u0435 0 \u0441\u0435\u043a\u0443\u043d\u0434, \u043f\u0440\u0435\u0436\u0434\u0435 \u0447\u0435\u043c \u043e\u0442\u043f\u0440\u0430\u0432\u043b\u044f\u0442\u044c \u0441\u043e\u043e\u0431\u0449\u0435\u043d\u0438\u0435 \u0432 \u044d\u0442\u043e\u0442 \u0447\u0430\u0442 \u0441\u043d\u043e\u0432\u0430.", "\u26a0 \u0412\u043d\u0438\u043c\u0430\u043d\u0438\u0435! \u0412\u044b \u043d\u0435 \u043f\u0440\u0438\u0432\u044f\u0437\u0430\u043b\u0438 \u0422\u0435\u043b\u0435\u0433\u0440\u0430\u043c\u043c \u043a \u0441\u0432\u043e\u0435\u043c\u0443 \u0430\u043a\u043a\u0430\u0443\u043d\u0442\u0443", "\u0421\u0434\u0435\u043b\u0430\u0432 \u044d\u0442\u043e, \u0432\u044b \u0441\u043c\u043e\u0436\u0435\u0442\u0435 \u0443\u043f\u0440\u0430\u0432\u043b\u044f\u0442\u044c \u0430\u043a\u043a\u0430\u0443\u043d\u0442\u043e\u043c \u0438 \u043f\u043e\u043b\u0443\u0447\u0430\u0442\u044c \u0443\u0432\u0435\u0434\u043e\u043c\u043b\u0435\u043d\u0438\u044f", "\u0421\u0434\u0435\u043b\u0430\u0439\u0442\u0435 \u044d\u0442\u043e \u043f\u0440\u044f\u043c\u043e \u0441\u0435\u0439\u0447\u0430\u0441, \u043d\u0430\u043f\u0438\u0441\u0430\u0432 \u0432 \u0441\u043e\u043e\u0431\u0449\u0435\u043d\u0438\u044f \u0431\u043e\u0442\u0430: t.me/infinityhvh_bot", "\u0414\u043b\u044f \u043f\u0440\u0438\u0432\u044f\u0437\u043a\u0438 \u0438\u0441\u043f\u043e\u043b\u044c\u0437\u0443\u0439\u0442\u0435 \u043a\u043e\u043c\u0430\u043d\u0434\u0443 \u0412 \u0411\u041e\u0422\u0415 - /start", "\u239b!\u23a0 \u041d\u0435 \u0441\u043f\u0430\u043c\u044c\u0442\u0435!", "\u041d\u0430\u0436\u043c\u0438 \u0447\u0442\u043e\u0431\u044b \u043f\u043e\u0441\u043c\u043e\u0442\u0440\u0435\u0442\u044c \u0438\u043d\u0432\u0435\u043d\u0442\u0430\u0440\u044c", "\u0412\u044b \u043c\u043e\u0436\u0435\u0442\u0435 \u0441\u0442\u0440\u043e\u0438\u0442\u044c \u0441 \u044d\u0442\u0438\u043c \u043a\u0438\u0442\u043e\u043c!");
                String decolorecText = componentText.replace("\u00a70", "").replace("\u00a71", "").replace("\u00a72", "").replace("\u00a73", "").replace("\u00a74", "").replace("\u00a75", "").replace("\u00a76", "").replace("\u00a77", "").replace("\u00a78", "").replace("\u00a79", "").replace("\u00a7a", "").replace("\u00a7b", "").replace("\u00a7c", "").replace("\u00a7d", "").replace("\u00a7e", "").replace("\u00a7f", "").replace("\u00a7k", "").replace("\u00a7l", "").replace("\u00a7m", "").replace("\u00a7n", "").replace("\u00a7o", "").replace("\u00a7r", "");
                if (bad_strings.stream().anyMatch(bad -> decolorecText.equalsIgnoreCase((String)bad)) || decolorecText.contains("\u239b!\u23a0 \u041e\u0436\u0438\u0434\u0430\u043d\u0438\u0435 \u0441\u043e\u043f\u0435\u0440\u043d\u0438\u043a\u0430 \u043d\u0430 \u041d\u0430\u0431\u043e\u0440")) {
                    eventPacket.cancel();
                }
            }
        }
    }

    private String getIp() {
        ServerData data;
        String str = "Unknown";
        if (!mc.isSingleplayer() && (data = mc.getCurrentServerData()) != null && !data.serverIP.isEmpty()) {
            str = data.serverIP;
        }
        return str;
    }

    private boolean isInfinityHVHIP() {
        return this.getIp().toLowerCase().contains("infinityhvh");
    }

    private BlockPos spawnCheckPos() {
        return new BlockPos(277, 185, 183);
    }

    private BlockPos spawnCheckPosALT1() {
        return new BlockPos(278, 186, 203);
    }

    private BlockPos spawnCheckPosALT2() {
        return new BlockPos(319, 173, 152);
    }

    private BlockPos spawnCheckPosALT3() {
        return new BlockPos(244, 167, 173);
    }

    private BlockPos spawnCheckPosALT4() {
        return new BlockPos(270, 177, 223);
    }

    private boolean isInfHVHSpawn(boolean hasInvinityHVH) {
        return hasInvinityHVH && InfHVHHelper.mc.world.getWorldType() == WorldType.FLAT && (BlockRedstoneLight.getIdFromBlock(InfHVHHelper.mc.world.getBlockState(this.spawnCheckPos()).getBlock()) == 95 || BlockRedstoneLight.getIdFromBlock(InfHVHHelper.mc.world.getBlockState(this.spawnCheckPosALT1()).getBlock()) == 130 || BlockRedstoneLight.getIdFromBlock(InfHVHHelper.mc.world.getBlockState(this.spawnCheckPosALT2()).getBlock()) == 251 || BlockRedstoneLight.getIdFromBlock(InfHVHHelper.mc.world.getBlockState(this.spawnCheckPosALT3()).getBlock()) == 251 || BlockRedstoneLight.getIdFromBlock(InfHVHHelper.mc.world.getBlockState(this.spawnCheckPosALT4()).getBlock()) == 251);
    }

    private boolean isInDuelKitSelectorUI() {
        int countGlass = 0;
        if (InfHVHHelper.mc.currentScreen instanceof GuiContainer && !(InfHVHHelper.mc.currentScreen instanceof GuiInventory) && Minecraft.player.openContainer instanceof ContainerChest) {
            for (int index = 0; index < Minecraft.player.openContainer.inventorySlots.size(); ++index) {
                ItemStack stack;
                Item item;
                if (!Minecraft.player.openContainer.inventorySlots.get(index).getHasStack() || (item = (stack = Minecraft.player.openContainer.inventorySlots.get(index).getStack()).getItem()) != Item.getItemFromBlock(Blocks.STAINED_GLASS_PANE)) continue;
                ++countGlass;
            }
        }
        return countGlass == 14;
    }

    private int getCurreentDuelClickSlot(boolean smartSlot, String best\u0421hoice) {
        int selectedSlot = -1;
        if (!best\u0421hoice.isEmpty()) {
            switch (best\u0421hoice) {
                case "Standart": {
                    selectedSlot = 12;
                    break;
                }
                case "Crystal": {
                    selectedSlot = 13;
                    break;
                }
                case "Elytra": {
                    selectedSlot = 14;
                    break;
                }
                case "Totem": {
                    selectedSlot = 21;
                    break;
                }
                case "NoDebuff": {
                    selectedSlot = 22;
                    break;
                }
                case "Axe": {
                    selectedSlot = 23;
                    break;
                }
                case "EasyGrief": {
                    selectedSlot = 30;
                    break;
                }
                case "ReallyWorld": {
                    selectedSlot = 31;
                    break;
                }
                case "FunTime": {
                    selectedSlot = 32;
                }
            }
            if (smartSlot) {
                for (int index = 0; index < Minecraft.player.openContainer.inventorySlots.size(); ++index) {
                    ItemStack stack;
                    Item itemInStack;
                    if (!Minecraft.player.openContainer.inventorySlots.get(index).getHasStack() || !((itemInStack = (stack = Minecraft.player.openContainer.inventorySlots.get(index).getStack()).getItem()) instanceof ItemSkull) || !stack.anyDoubleString("\u043e\u0447\u0435\u0440\u0435\u0434\u0438", "1", false)) continue;
                    selectedSlot = index;
                    break;
                }
            }
        }
        return selectedSlot;
    }

    private void waitClickToDuelSlotWithAction(int currentDuelSlot) {
        if (Minecraft.player.openContainer == null) {
            currentDuelSlot = -1;
        }
        if (currentDuelSlot != -1) {
            InfHVHHelper.mc.playerController.windowClick(Minecraft.player.openContainer.windowId, currentDuelSlot, 1, ClickType.PICKUP, Minecraft.player);
            Minecraft.player.closeScreen();
            InfHVHHelper.mc.currentScreen = null;
        }
    }

    private boolean clickHotbarSlot(int slotHand) {
        NetHandlerPlayClient connect;
        if (slotHand >= 0 && slotHand <= 8 && (connect = mc.getConnection()) != null) {
            connect.sendPacket(new CPacketHeldItemChange(slotHand));
            connect.sendPacket(new CPacketPlayerTryUseItem(EnumHand.MAIN_HAND));
            connect.sendPacket(new CPacketHeldItemChange(Minecraft.player.inventory.currentItem));
            return true;
        }
        return false;
    }

    private boolean hasClickSwordKitter(boolean sneakCheck) {
        return (!sneakCheck || Minecraft.player.isSneaking()) && Minecraft.player.inventory.getStackInSlot(0).getItem() == Items.IRON_SWORD && this.clickHotbarSlot(0);
    }

    private boolean hasClickReseller(boolean canResell, long timeWait, long delayPass) {
        boolean hasRedstone;
        boolean bl = hasRedstone = canResell && Minecraft.player.inventory.getStackInSlot(8).getItem() == Items.MELON;
        if (!hasRedstone) {
            this.WAIT_FOR_RESELL.reset();
        } else if (this.WAIT_FOR_RESELL.hasReached(timeWait)) {
            this.WAIT_FOR_RESELL.setTime(this.WAIT_FOR_RESELL.getTime() - delayPass);
            return this.clickHotbarSlot(8);
        }
        return false;
    }

    private int getCurrentHotkit(String hotKitMode) {
        int slot = -1;
        switch (hotKitMode) {
            case "NotAuto": {
                break;
            }
            case "Normal": {
                slot = 8;
                break;
            }
            case "Duped": {
                slot = 0;
            }
        }
        return slot;
    }

    private boolean hasClickHotkitSelector(int selectedRule, long timeWait, long delayPass) {
        boolean hasCurrentClick;
        boolean bl = hasCurrentClick = selectedRule != -1 && Minecraft.player.inventory.getStackInSlot(selectedRule).getItem() == Items.ENCHANTED_BOOK;
        if (!hasCurrentClick) {
            this.WAIT_FOR_HOTKIT.reset();
        } else if (this.WAIT_FOR_HOTKIT.hasReached(delayPass)) {
            this.WAIT_FOR_HOTKIT.setTime(this.WAIT_FOR_HOTKIT.getTime() - delayPass);
            return this.clickHotbarSlot(selectedRule);
        }
        return false;
    }

    private long[] delaysActionsPassenger() {
        return new long[]{1600L, 350L};
    }

    private void removeAllPlayersFromSpawn(boolean hasSpawnDetected) {
        List<EntityOtherPlayerMP> livingsToRemove;
        if (hasSpawnDetected && !(livingsToRemove = InfHVHHelper.mc.world.getLoadedEntityList().stream().map(Entity::getOtherPlayerOf).filter(Objects::nonNull).filter(otherPlayerMP -> otherPlayerMP.getName().isEmpty() || !Client.friendManager.isFriend(otherPlayerMP.getName())).filter(otherPlayerMP -> otherPlayerMP.getEntityId() != 462462998 && otherPlayerMP.getEntityId() != 462462999).collect(Collectors.toList())).isEmpty()) {
            livingsToRemove.forEach(otherPlayerMP -> InfHVHHelper.mc.world.removeEntityFromWorld(otherPlayerMP.getEntityId()));
        }
    }

    @Override
    public void onUpdate() {
        boolean hasInfinityHVH = this.isInfinityHVHIP();
        if (!hasInfinityHVH) {
            return;
        }
        if (!this.AutoDuel.getBool() && !this.NoPlayersOnSpawn.getBool() && this.SelectedKitMode.currentMode.equalsIgnoreCase("NotAuto")) {
            return;
        }
        boolean nearbyWithSpawn = this.isInfHVHSpawn(hasInfinityHVH);
        long[] delaysActs = this.delaysActionsPassenger();
        this.hasClickHotkitSelector(this.getCurrentHotkit(this.SelectedKitMode.currentMode), delaysActs[0], delaysActs[1]);
        this.removeAllPlayersFromSpawn(nearbyWithSpawn && this.NoPlayersOnSpawn.getBool());
        if (!this.AutoDuel.getBool()) {
            return;
        }
        if (!nearbyWithSpawn) {
            this.WAIT_FOR_RESELL.reset();
            this.WAIT_FOR_HOTKIT.reset();
            return;
        }
        this.hasClickSwordKitter(this.AutoDuelOnlySneak.getBool());
        if (this.isInDuelKitSelectorUI()) {
            this.waitClickToDuelSlotWithAction(this.getCurreentDuelClickSlot(this.SmartKit.getBool(), this.Kit.currentMode));
        }
        this.hasClickReseller(this.AutoDuelResell.getBool(), delaysActs[0], delaysActs[1]);
    }
}

