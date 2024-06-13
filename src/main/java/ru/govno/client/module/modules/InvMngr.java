package ru.govno.client.module.modules;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.ContainerShulkerBox;
import net.minecraft.item.Item;
import net.minecraft.item.ItemAppleGold;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemBucket;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemGlassBottle;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemSpade;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionUtils;
import ru.govno.client.module.Module;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.module.settings.FloatSettings;
import ru.govno.client.utils.InventoryUtil;
import ru.govno.client.utils.TimerHelper;
import ru.govno.client.utils.Movement.MoveMeHelp;

public class InvMngr extends Module {
   public static int weaponSlot = 36;
   public static int pickaxeSlot = 37;
   public static int axeSlot = 38;
   public static int shovelSlot = 39;
   public static List<Block> invalidBlocks = Arrays.asList(
      Blocks.ENCHANTING_TABLE,
      Blocks.FURNACE,
      Blocks.CARPET,
      Blocks.CRAFTING_TABLE,
      Blocks.TRAPPED_CHEST,
      Blocks.CHEST,
      Blocks.DISPENSER,
      Blocks.AIR,
      Blocks.WATER,
      Blocks.LAVA,
      Blocks.FLOWING_WATER,
      Blocks.FLOWING_LAVA,
      Blocks.SAND,
      Blocks.SNOW_LAYER,
      Blocks.TORCH,
      Blocks.ANVIL,
      Blocks.JUKEBOX,
      Blocks.STONE_BUTTON,
      Blocks.WOODEN_BUTTON,
      Blocks.LEVER,
      Blocks.NOTEBLOCK,
      Blocks.STONE_PRESSURE_PLATE,
      Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE,
      Blocks.WOODEN_PRESSURE_PLATE,
      Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE,
      Blocks.STONE_SLAB,
      Blocks.WOODEN_SLAB,
      Blocks.STONE_SLAB2,
      Blocks.RED_MUSHROOM,
      Blocks.BROWN_MUSHROOM,
      Blocks.YELLOW_FLOWER,
      Blocks.RED_FLOWER,
      Blocks.ANVIL,
      Blocks.GLASS_PANE,
      Blocks.STAINED_GLASS_PANE,
      Blocks.IRON_BARS,
      Blocks.CACTUS,
      Blocks.LADDER,
      Blocks.WEB
   );
   private final TimerHelper timer = new TimerHelper();
   FloatSettings BlockCap;
   FloatSettings SortDelay;
   BoolSettings ForTheArcher;
   BoolSettings Food;
   BoolSettings Sword;
   BoolSettings InvCleaner;
   BoolSettings OnlyInInv;
   BoolSettings NoMovingSwap;

   public InvMngr() {
      super("InvMngr", 0, Module.Category.PLAYER);
      this.settings.add(this.BlockCap = new FloatSettings("BlockCap", 128.0F, 256.0F, 8.0F, this));
      this.settings.add(this.SortDelay = new FloatSettings("SortDelay", 50.0F, 250.0F, 0.0F, this));
      this.settings.add(this.ForTheArcher = new BoolSettings("ForTheArcher", false, this));
      this.settings.add(this.Food = new BoolSettings("Food", false, this));
      this.settings.add(this.Sword = new BoolSettings("Sword", true, this));
      this.settings.add(this.InvCleaner = new BoolSettings("InvCleaner", true, this));
      this.settings.add(this.OnlyInInv = new BoolSettings("OnlyInInv", true, this));
      this.settings.add(this.NoMovingSwap = new BoolSettings("NoMovingSwap", false, this));
   }

   @Override
   public void onUpdate() {
      long delay = (long)this.SortDelay.getFloat();
      if (!(Minecraft.player.openContainer instanceof ContainerChest) && !(Minecraft.player.openContainer instanceof ContainerShulkerBox)) {
         if (mc.currentScreen instanceof GuiInventory || !this.OnlyInInv.getBool()) {
            if (MoveMeHelp.getSpeed() == 0.0 && !Minecraft.player.serverSprintState && !MoveMeHelp.moveKeysPressed() || !this.NoMovingSwap.getBool()) {
               if (mc.currentScreen == null || mc.currentScreen instanceof GuiInventory || mc.currentScreen instanceof GuiChat) {
                  if (this.timer.hasReached((float)delay) && weaponSlot >= 36) {
                     if (!Minecraft.player.inventoryContainer.getSlot(weaponSlot).getHasStack()) {
                        this.getBestWeapon(weaponSlot);
                     } else if (!this.isBestWeapon(Minecraft.player.inventoryContainer.getSlot(weaponSlot).getStack())) {
                        this.getBestWeapon(weaponSlot);
                     }
                  }

                  if (this.timer.hasReached((float)delay) && pickaxeSlot >= 36) {
                     this.getBestPickaxe();
                  }

                  if (this.timer.hasReached((float)delay) && shovelSlot >= 36) {
                     this.getBestShovel();
                  }

                  if (this.timer.hasReached((float)delay) && axeSlot >= 36) {
                     this.getBestAxe();
                  }

                  if (this.timer.hasReached((float)delay) && this.InvCleaner.getBool()) {
                     for (int i = 9; i < 45; i++) {
                        if (Minecraft.player.inventoryContainer.getSlot(i).getHasStack()) {
                           ItemStack is = Minecraft.player.inventoryContainer.getSlot(i).getStack();
                           if (this.shouldDrop(is, i)) {
                              this.drop(i);
                              if (delay == 0L) {
                                 Minecraft.player.closeScreen();
                              }

                              this.timer.reset();
                              if (delay > 0L) {
                                 break;
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   public void swap(int slot, int hotbarSlot) {
      mc.playerController.windowClick(Minecraft.player.inventoryContainer.windowId, slot, hotbarSlot, ClickType.SWAP, Minecraft.player);
   }

   public void drop(int slot) {
      mc.playerController.windowClick(Minecraft.player.inventoryContainer.windowId, slot, 1, ClickType.THROW, Minecraft.player);
   }

   public boolean isBestWeapon(ItemStack stack) {
      float damage = this.getDamage(stack);

      for (int i = 9; i < 45; i++) {
         if (Minecraft.player.inventoryContainer.getSlot(i).getHasStack()) {
            ItemStack is = Minecraft.player.inventoryContainer.getSlot(i).getStack();
            if (this.getDamage(is) > damage && (is.getItem() instanceof ItemSword || !this.Sword.getBool())) {
               return false;
            }
         }
      }

      return stack.getItem() instanceof ItemSword || !this.Sword.getBool();
   }

   public void getBestWeapon(int slot) {
      for (int i = 9; i < 45; i++) {
         if (Minecraft.player.inventoryContainer.getSlot(i).getHasStack()) {
            ItemStack is = Minecraft.player.inventoryContainer.getSlot(i).getStack();
            if (this.isBestWeapon(is) && this.getDamage(is) > 0.0F && (is.getItem() instanceof ItemSword || !this.Sword.getBool())) {
               this.swap(i, slot - 36);
               this.timer.reset();
               break;
            }
         }
      }
   }

   private float getDamage(ItemStack stack) {
      float damage = 0.0F;
      Item item = stack.getItem();
      if (item instanceof ItemTool tool) {
         damage += tool.getDamageVsEntity();
      }

      if (item instanceof ItemSword sword) {
         damage += sword.getDamageVsEntity();
      }

      return damage
         + (float)EnchantmentHelper.getEnchantmentLevel(Objects.requireNonNull(Enchantment.getEnchantmentByID(16)), stack) * 1.25F
         + (float)EnchantmentHelper.getEnchantmentLevel(Objects.requireNonNull(Enchantment.getEnchantmentByID(20)), stack) * 0.01F;
   }

   public boolean shouldDrop(ItemStack stack, int slot) {
      if (stack.getDisplayName().toLowerCase().contains("/")) {
         return false;
      } else if (stack.getDisplayName().toLowerCase().contains("§k||")) {
         return false;
      } else if (stack.getDisplayName().toLowerCase().contains("kit")) {
         return false;
      } else if (stack.getDisplayName().toLowerCase().contains("wool")) {
         return false;
      } else if ((slot != weaponSlot || !this.isBestWeapon(Minecraft.player.inventoryContainer.getSlot(weaponSlot).getStack()))
         && (slot != pickaxeSlot || !this.isBestPickaxe(Minecraft.player.inventoryContainer.getSlot(pickaxeSlot).getStack()) || pickaxeSlot < 0)
         && (slot != axeSlot || !this.isBestAxe(Minecraft.player.inventoryContainer.getSlot(axeSlot).getStack()) || axeSlot < 0)
         && (slot != shovelSlot || !this.isBestShovel(Minecraft.player.inventoryContainer.getSlot(shovelSlot).getStack()) || shovelSlot < 0)) {
         if (stack.getItem() instanceof ItemBucket) {
            return false;
         } else {
            if (stack.getItem() instanceof ItemArmor) {
               for (int type = 1; type < 5; type++) {
                  if (Minecraft.player.inventoryContainer.getSlot(4 + type).getHasStack()) {
                     ItemStack is = Minecraft.player.inventoryContainer.getSlot(4 + type).getStack();
                     if (InventoryUtil.isBestArmor(is, type)) {
                        continue;
                     }
                  }

                  if (InventoryUtil.isBestArmor(stack, type)) {
                     return false;
                  }
               }
            }

            if (!(stack.getItem() instanceof ItemBlock)
               || !((float)this.getBlockCount() > this.BlockCap.getFloat()) && !invalidBlocks.contains(((ItemBlock)stack.getItem()).getBlock())) {
               if (stack.getItem() instanceof ItemPotion && this.isBadPotion(stack)) {
                  return true;
               } else if (stack.getItem() instanceof ItemFood && this.Food.getBool() && !(stack.getItem() instanceof ItemAppleGold)) {
                  return true;
               } else if (stack.getItem() instanceof ItemHoe
                  || stack.getItem() instanceof ItemTool
                  || stack.getItem() instanceof ItemSword
                  || stack.getItem() instanceof ItemArmor) {
                  return true;
               } else {
                  return (stack.getItem() instanceof ItemBow || stack.getItem().getUnlocalizedName().contains("arrow")) && this.ForTheArcher.getBool()
                     ? true
                     : stack.getItem().getUnlocalizedName().contains("tnt")
                        || stack.getItem().getUnlocalizedName().contains("stick")
                        || stack.getItem().getUnlocalizedName().contains("egg")
                        || stack.getItem().getUnlocalizedName().contains("string")
                        || stack.getItem().getUnlocalizedName().contains("cake")
                        || stack.getItem().getUnlocalizedName().contains("mushroom")
                        || stack.getItem().getUnlocalizedName().contains("flint")
                        || stack.getItem().getUnlocalizedName().contains("dyePowder")
                        || stack.getItem().getUnlocalizedName().contains("feather")
                        || stack.getItem().getUnlocalizedName().contains("bucket")
                        || stack.getItem().getUnlocalizedName().contains("chest") && !stack.getDisplayName().toLowerCase().contains("collect")
                        || stack.getItem().getUnlocalizedName().contains("snow")
                        || stack.getItem().getUnlocalizedName().contains("fish")
                        || stack.getItem().getUnlocalizedName().contains("enchant")
                        || stack.getItem().getUnlocalizedName().contains("exp")
                        || stack.getItem().getUnlocalizedName().contains("shears")
                        || stack.getItem().getUnlocalizedName().contains("anvil")
                        || stack.getItem().getUnlocalizedName().contains("torch")
                        || stack.getItem().getUnlocalizedName().contains("seeds")
                        || stack.getItem().getUnlocalizedName().contains("leather")
                        || stack.getItem().getUnlocalizedName().contains("reeds")
                        || stack.getItem().getUnlocalizedName().contains("skull")
                        || stack.getItem().getUnlocalizedName().contains("wool")
                        || stack.getItem().getUnlocalizedName().contains("record")
                        || stack.getItem().getUnlocalizedName().contains("snowball")
                        || stack.getItem() instanceof ItemGlassBottle
                        || stack.getItem().getUnlocalizedName().contains("piston");
               }
            } else {
               return true;
            }
         }
      } else {
         return false;
      }
   }

   private int getBlockCount() {
      int blockCount = 0;

      for (int i = 0; i < 45; i++) {
         if (Minecraft.player.inventoryContainer.getSlot(i).getHasStack()) {
            ItemStack is = Minecraft.player.inventoryContainer.getSlot(i).getStack();
            Item item = is.getItem();
            if (is.getItem() instanceof ItemBlock && !invalidBlocks.contains(((ItemBlock)item).getBlock())) {
               blockCount += is.stackSize;
            }
         }
      }

      return blockCount;
   }

   private void getBestPickaxe() {
      for (int i = 9; i < 45; i++) {
         if (Minecraft.player.inventoryContainer.getSlot(i).getHasStack()) {
            ItemStack is = Minecraft.player.inventoryContainer.getSlot(i).getStack();
            if (this.isBestPickaxe(is) && pickaxeSlot != i && !this.isBestWeapon(is)) {
               if (!Minecraft.player.inventoryContainer.getSlot(pickaxeSlot).getHasStack()) {
                  this.swap(i, pickaxeSlot - 36);
                  this.timer.reset();
                  if (this.SortDelay.getFloat() > 0.0F) {
                     return;
                  }
               } else if (!this.isBestPickaxe(Minecraft.player.inventoryContainer.getSlot(pickaxeSlot).getStack())) {
                  this.swap(i, pickaxeSlot - 36);
                  this.timer.reset();
                  if (this.SortDelay.getFloat() > 0.0F) {
                     return;
                  }
               }
            }
         }
      }
   }

   private void getBestShovel() {
      for (int i = 9; i < 45; i++) {
         if (Minecraft.player.inventoryContainer.getSlot(i).getHasStack()) {
            ItemStack is = Minecraft.player.inventoryContainer.getSlot(i).getStack();
            if (this.isBestShovel(is) && shovelSlot != i && !this.isBestWeapon(is)) {
               if (!Minecraft.player.inventoryContainer.getSlot(shovelSlot).getHasStack()) {
                  this.swap(i, shovelSlot - 36);
                  this.timer.reset();
                  if (this.SortDelay.getFloat() > 0.0F) {
                     return;
                  }
               } else if (!this.isBestShovel(Minecraft.player.inventoryContainer.getSlot(shovelSlot).getStack())) {
                  this.swap(i, shovelSlot - 36);
                  this.timer.reset();
                  if (this.SortDelay.getFloat() > 0.0F) {
                     return;
                  }
               }
            }
         }
      }
   }

   private void getBestAxe() {
      for (int i = 9; i < 45; i++) {
         if (Minecraft.player.inventoryContainer.getSlot(i).getHasStack()) {
            ItemStack is = Minecraft.player.inventoryContainer.getSlot(i).getStack();
            if (this.isBestAxe(is) && axeSlot != i && !this.isBestWeapon(is)) {
               if (!Minecraft.player.inventoryContainer.getSlot(axeSlot).getHasStack()) {
                  this.swap(i, axeSlot - 36);
                  this.timer.reset();
                  if (this.SortDelay.getFloat() > 0.0F) {
                     return;
                  }
               } else if (!this.isBestAxe(Minecraft.player.inventoryContainer.getSlot(axeSlot).getStack())) {
                  this.swap(i, axeSlot - 36);
                  this.timer.reset();
                  if (this.SortDelay.getFloat() > 0.0F) {
                     return;
                  }
               }
            }
         }
      }
   }

   private boolean isBestPickaxe(ItemStack stack) {
      Item item = stack.getItem();
      if (!(item instanceof ItemPickaxe)) {
         return false;
      } else {
         float value = this.getToolEffect(stack);

         for (int i = 9; i < 45; i++) {
            if (Minecraft.player.inventoryContainer.getSlot(i).getHasStack()) {
               ItemStack is = Minecraft.player.inventoryContainer.getSlot(i).getStack();
               if (this.getToolEffect(is) > value && is.getItem() instanceof ItemPickaxe) {
                  return false;
               }
            }
         }

         return true;
      }
   }

   private boolean isBestShovel(ItemStack stack) {
      Item item = stack.getItem();
      if (!(item instanceof ItemSpade)) {
         return false;
      } else {
         float value = this.getToolEffect(stack);

         for (int i = 9; i < 45; i++) {
            if (Minecraft.player.inventoryContainer.getSlot(i).getHasStack()) {
               ItemStack is = Minecraft.player.inventoryContainer.getSlot(i).getStack();
               if (this.getToolEffect(is) > value && is.getItem() instanceof ItemSpade) {
                  return false;
               }
            }
         }

         return true;
      }
   }

   private boolean isBestAxe(ItemStack stack) {
      Item item = stack.getItem();
      if (!(item instanceof ItemAxe)) {
         return false;
      } else {
         float value = this.getToolEffect(stack);

         for (int i = 9; i < 45; i++) {
            if (Minecraft.player.inventoryContainer.getSlot(i).getHasStack()) {
               ItemStack is = Minecraft.player.inventoryContainer.getSlot(i).getStack();
               if (this.getToolEffect(is) > value && is.getItem() instanceof ItemAxe && !this.isBestWeapon(stack)) {
                  return false;
               }
            }
         }

         return true;
      }
   }

   private float getToolEffect(ItemStack stack) {
      Item item = stack.getItem();
      if (item instanceof ItemTool tool) {
         String name = item.getUnlocalizedName();
         float value;
         if (item instanceof ItemPickaxe) {
            value = tool.getStrVsBlock(stack, Blocks.STONE.getDefaultState());
            if (name.toLowerCase().contains("gold")) {
               value -= 5.0F;
            }
         } else if (item instanceof ItemSpade) {
            value = tool.getStrVsBlock(stack, Blocks.DIRT.getDefaultState());
            if (name.toLowerCase().contains("gold")) {
               value -= 5.0F;
            }
         } else {
            if (!(item instanceof ItemAxe)) {
               return 1.0F;
            }

            value = tool.getStrVsBlock(stack, Blocks.LOG.getDefaultState());
            if (name.toLowerCase().contains("gold")) {
               value -= 5.0F;
            }
         }

         value = (float)(
            (double)value + (double)EnchantmentHelper.getEnchantmentLevel(Objects.requireNonNull(Enchantment.getEnchantmentByID(32)), stack) * 0.0075
         );
         return (float)(
            (double)value + (double)EnchantmentHelper.getEnchantmentLevel(Objects.requireNonNull(Enchantment.getEnchantmentByID(34)), stack) / 100.0
         );
      } else {
         return 0.0F;
      }
   }

   private boolean isBadPotion(ItemStack stack) {
      if (stack != null && stack.getItem() instanceof ItemPotion) {
         for (PotionEffect o : PotionUtils.getEffectsFromStack(stack)) {
            if (o.getPotion() == Potion.getPotionById(19)
               || o.getPotion() == Potion.getPotionById(7)
               || o.getPotion() == Potion.getPotionById(2)
               || o.getPotion() == Potion.getPotionById(18)) {
               return true;
            }
         }
      }

      return false;
   }
}
