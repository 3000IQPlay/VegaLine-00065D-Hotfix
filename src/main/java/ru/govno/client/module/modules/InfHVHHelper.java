package ru.govno.client.module.modules;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import net.minecraft.block.BlockRedstoneLight;
import net.minecraft.block.BlockStainedGlass;
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

public class InfHVHHelper extends Module {
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
      this.settings.add(this.AutoDuel = new BoolSettings("AutoDuel", true, this));
      this.settings.add(this.AutoDuelOnlySneak = new BoolSettings("AutoDuelOnlySneak", true, this, () -> this.AutoDuel.getBool()));
      String[] kitsModes = new String[]{"Standart", "Prostocraft", "Thorns", "Sunrise", "Craftyou", "MST", "Reallyworld", "Crystals"};
      this.settings.add(this.Kit = new ModeSettings("Kit", kitsModes[6], this, kitsModes, () -> this.AutoDuel.getBool()));
      this.settings.add(this.SmartKit = new BoolSettings("SmartKit", false, this));
      this.settings.add(this.AutoDuelResell = new BoolSettings("AutoDuelResell", true, this, () -> this.AutoDuel.getBool()));
      this.settings.add(this.SelectedKitMode = new ModeSettings("SelectedKitMode", "NotAuto", this, new String[]{"NotAuto", "Normal", "Duped"}));
      this.settings.add(this.NoPlayersOnSpawn = new BoolSettings("NoPlayersOnSpawn", false, this));
      this.settings.add(this.NoTrashMessages = new BoolSettings("NoTrashMessages", true, this));
   }

   @EventTarget
   public void onPreReceivePackets(EventReceivePacket eventPacket) {
      if (eventPacket.getPacket() instanceof SPacketChat chatPacket && this.NoTrashMessages.getBool()) {
         ITextComponent component = chatPacket.chatComponent;
         String componentText;
         if (chatPacket.chatComponent != null && (componentText = component.getUnformattedText()) != null && !componentText.isEmpty()) {
            List<String> bad_strings = Arrays.asList(
               "┏  ",
               "| Подпишись на нашу группу ВК, чтобы быть вкурсе всех событий",
               "| Наша группа ТГ канал: t.me/infinityhvh_tg",
               "| Наш сайт: InfinityHvH.com",
               "┗  ",
               "| Не забудь проверить свою статистику",
               "| С помощью команды /stats",
               "| Обезопась свой аккаунт от хакеров",
               "| Привяжи свой ВК, а так-же подключи двухэтапную аутентификацию от ВК",
               "| Наша группа ТГ канал: t.me/infinityhvh_tg",
               "| Подпишись на нашу группу ВК, чтобы быть вкурсе всех событий",
               "| Сумасшедшие скидки на все привилегии сервера",
               "[!] Извините, но Вы не можете драться в этом месте.",
               "⎛!⎠ Ваш матч начнётся через 4 секунд",
               "⎛!⎠ Ваш матч начнётся через 3 секунд",
               "⎛!⎠ Ваш матч начнётся через 2 секунд",
               "⎛!⎠ Ваш матч начнётся через 1 секунд",
               "⎛!⎠ В бой!",
               "⎛!⎠ Вы вышли из очереди!",
               "Подождите 3 секунд, прежде чем отправлять сообщение в этот чат снова.",
               "Подождите 2 секунд, прежде чем отправлять сообщение в этот чат снова.",
               "Подождите 1 секунд, прежде чем отправлять сообщение в этот чат снова.",
               "Подождите 0 секунд, прежде чем отправлять сообщение в этот чат снова.",
               "⚠ Внимание! Вы не привязали Телеграмм к своему аккаунту",
               "Сделав это, вы сможете управлять аккаунтом и получать уведомления",
               "Сделайте это прямо сейчас, написав в сообщения бота: t.me/infinityhvh_bot",
               "Для привязки используйте команду В БОТЕ - /start",
               "⎛!⎠ Не спамьте!",
               "Нажми чтобы посмотреть инвентарь",
               "Вы можете строить с этим китом!"
            );
            String decolorecText = componentText.replace("§0", "")
               .replace("§1", "")
               .replace("§2", "")
               .replace("§3", "")
               .replace("§4", "")
               .replace("§5", "")
               .replace("§6", "")
               .replace("§7", "")
               .replace("§8", "")
               .replace("§9", "")
               .replace("§a", "")
               .replace("§b", "")
               .replace("§c", "")
               .replace("§d", "")
               .replace("§e", "")
               .replace("§f", "")
               .replace("§k", "")
               .replace("§l", "")
               .replace("§m", "")
               .replace("§n", "")
               .replace("§o", "")
               .replace("§r", "");
            if (bad_strings.stream().anyMatch(bad -> decolorecText.equalsIgnoreCase(bad)) || decolorecText.contains("⎛!⎠ Ожидание соперника на Набор")) {
               eventPacket.cancel();
            }
         }
      }
   }

   private String getIp() {
      String str = "Unknown";
      ServerData data;
      if (!mc.isSingleplayer() && (data = mc.getCurrentServerData()) != null && !data.serverIP.isEmpty()) {
         str = data.serverIP;
      }

      return str;
   }

   private boolean isInfinityHVHIP() {
      return this.getIp().toLowerCase().contains("infinityhvh");
   }

   private BlockPos spawnCheckPos() {
      return new BlockPos(-1, 34, -5);
   }

   private BlockPos spawnCheckPosALT1() {
      return new BlockPos(-46, 15, -5);
   }

   private BlockPos spawnCheckPosALT2() {
      return new BlockPos(62, 15, -5);
   }

   private BlockPos spawnCheckPosALT3() {
      return new BlockPos(0, 15, 54);
   }

   private BlockPos spawnCheckPosALT4() {
      return new BlockPos(-2, 15, -59);
   }

   private boolean isInfHVHSpawn(boolean hasInvinityHVH) {
      return hasInvinityHVH
         && mc.world.getWorldType() == WorldType.FLAT
         && (
            mc.world.getBlockState(this.spawnCheckPos()).getBlock() instanceof BlockRedstoneLight
               || mc.world.getBlockState(this.spawnCheckPosALT1()).getBlock() instanceof BlockStainedGlass
               || mc.world.getBlockState(this.spawnCheckPosALT2()).getBlock() instanceof BlockStainedGlass
               || mc.world.getBlockState(this.spawnCheckPosALT3()).getBlock() instanceof BlockStainedGlass
               || mc.world.getBlockState(this.spawnCheckPosALT4()).getBlock() instanceof BlockStainedGlass
         );
   }

   private boolean isInDuelKitSelectorUI() {
      int countGlass = 0;
      if (mc.currentScreen instanceof GuiContainer && !(mc.currentScreen instanceof GuiInventory) && Minecraft.player.openContainer instanceof ContainerChest) {
         for (int index = 0; index < Minecraft.player.openContainer.inventorySlots.size(); index++) {
            if (Minecraft.player.openContainer.inventorySlots.get(index).getHasStack()) {
               ItemStack stack = Minecraft.player.openContainer.inventorySlots.get(index).getStack();
               Item item = stack.getItem();
               if (item == Item.getItemFromBlock(Blocks.STAINED_GLASS_PANE)) {
                  countGlass++;
               }
            }
         }
      }

      return countGlass == 26;
   }

   private int getCurreentDuelClickSlot(boolean smartSlot, String bestСhoice) {
      int selectedSlot = -1;
      if (!bestСhoice.isEmpty()) {
         switch (bestСhoice) {
            case "Standart":
               selectedSlot = 20;
               break;
            case "Prostocraft":
               selectedSlot = 21;
               break;
            case "Thorns":
               selectedSlot = 22;
               break;
            case "Sunrise":
               selectedSlot = 23;
               break;
            case "Craftyou":
               selectedSlot = 24;
               break;
            case "MST":
               selectedSlot = 30;
               break;
            case "Reallyworld":
               selectedSlot = 31;
               break;
            case "Crystals":
               selectedSlot = 32;
         }

         if (smartSlot) {
            for (int index = 0; index < Minecraft.player.openContainer.inventorySlots.size(); index++) {
               if (Minecraft.player.openContainer.inventorySlots.get(index).getHasStack()) {
                  ItemStack stack = Minecraft.player.openContainer.inventorySlots.get(index).getStack();
                  Item itemInStack = stack.getItem();
                  if (itemInStack instanceof ItemSkull && stack.anyDoubleString("очереди", "1", false)) {
                     selectedSlot = index;
                     break;
                  }
               }
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
         mc.playerController.windowClick(Minecraft.player.openContainer.windowId, currentDuelSlot, 1, ClickType.PICKUP, Minecraft.player);
         Minecraft.player.closeScreen();
         mc.currentScreen = null;
      }
   }

   private boolean clickHotbarSlot(int slotHand) {
      NetHandlerPlayClient connect;
      if (slotHand >= 0 && slotHand <= 8 && (connect = mc.getConnection()) != null) {
         connect.sendPacket(new CPacketHeldItemChange(slotHand));
         connect.sendPacket(new CPacketPlayerTryUseItem(EnumHand.MAIN_HAND));
         connect.sendPacket(new CPacketHeldItemChange(Minecraft.player.inventory.currentItem));
         return true;
      } else {
         return false;
      }
   }

   private boolean hasClickSwordKitter(boolean sneakCheck) {
      return (!sneakCheck || Minecraft.player.isSneaking())
         && Minecraft.player.inventory.getStackInSlot(0).getItem() == Items.IRON_SWORD
         && this.clickHotbarSlot(0);
   }

   private boolean hasClickRedstoneReseller(boolean canResell, long timeWait, long delayPass) {
      boolean hasRedstone = canResell && Minecraft.player.inventory.getStackInSlot(8).getItem() == Items.REDSTONE;
      if (!hasRedstone) {
         this.WAIT_FOR_RESELL.reset();
      } else if (this.WAIT_FOR_RESELL.hasReached((double)timeWait)) {
         this.WAIT_FOR_RESELL.setTime(this.WAIT_FOR_RESELL.getTime() - delayPass);
         return this.clickHotbarSlot(8);
      }

      return false;
   }

   private int getCurrentHotkit(final String hotKitMode) {
      int slot = -1;
      switch (hotKitMode) {
         case "Normal": {
            slot = 8;
            break;
         }
         case "Duped": {
            slot = 0;
            break;
         }
      }
      return slot;
   }

   private boolean hasClickHotkitSelector(int selectedRule, long timeWait, long delayPass) {
      boolean hasCurrentClick = selectedRule != -1 && Minecraft.player.inventory.getStackInSlot(selectedRule).getItem() == Items.ENCHANTED_BOOK;
      if (!hasCurrentClick) {
         this.WAIT_FOR_HOTKIT.reset();
      } else if (this.WAIT_FOR_HOTKIT.hasReached((double)delayPass)) {
         this.WAIT_FOR_HOTKIT.setTime(this.WAIT_FOR_HOTKIT.getTime() - delayPass);
         return this.clickHotbarSlot(selectedRule);
      }

      return false;
   }

   private long[] delaysActionsPassenger() {
      return new long[]{1600L, 350L};
   }

   private void removeAllPlayersFromSpawn(boolean hasSpawnDetected) {
      if (hasSpawnDetected) {
         List<EntityOtherPlayerMP> livingsToRemove = mc.world
            .getLoadedEntityList()
            .stream()
            .map(Entity::getOtherPlayerOf)
            .filter(Objects::nonNull)
            .filter(otherPlayerMP -> otherPlayerMP.getName().isEmpty() || !Client.friendManager.isFriend(otherPlayerMP.getName()))
            .filter(otherPlayerMP -> otherPlayerMP.getEntityId() != 462462998 && otherPlayerMP.getEntityId() != 462462999)
            .collect(Collectors.toList());
         if (!livingsToRemove.isEmpty()) {
            livingsToRemove.forEach(otherPlayerMP -> mc.world.removeEntityFromWorld(otherPlayerMP.getEntityId()));
         }
      }
   }

   @Override
   public void onUpdate() {
      boolean hasInfinityHVH;
      if (hasInfinityHVH = this.isInfinityHVHIP()) {
         if (this.AutoDuel.getBool() || this.NoPlayersOnSpawn.getBool() || !this.SelectedKitMode.currentMode.equalsIgnoreCase("NotAuto")) {
            boolean nearbyWithSpawn = this.isInfHVHSpawn(hasInfinityHVH);
            long[] delaysActs = this.delaysActionsPassenger();
            this.hasClickHotkitSelector(this.getCurrentHotkit(this.SelectedKitMode.currentMode), delaysActs[0], delaysActs[1]);
            this.removeAllPlayersFromSpawn(nearbyWithSpawn && this.NoPlayersOnSpawn.getBool());
            if (this.AutoDuel.getBool()) {
               if (!nearbyWithSpawn) {
                  this.WAIT_FOR_RESELL.reset();
                  this.WAIT_FOR_HOTKIT.reset();
               } else {
                  this.hasClickSwordKitter(this.AutoDuelOnlySneak.getBool());
                  if (this.isInDuelKitSelectorUI()) {
                     this.waitClickToDuelSlotWithAction(this.getCurreentDuelClickSlot(this.SmartKit.getBool(), this.Kit.currentMode));
                  }

                  this.hasClickRedstoneReseller(this.AutoDuelResell.getBool(), delaysActs[0], delaysActs[1]);
               }
            }
         }
      }
   }
}
