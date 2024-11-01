package ru.govno.client.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.ContainerBrewingStand;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import ru.govno.client.utils.Math.TimerHelper;
import ru.govno.client.utils.PotionUtils;

public class BrewUtil {
    public final int[] SLOTS_BOTTLES = new int[]{0, 1, 2};
    public final int SLOT_INGRIDIENT = 3;
    public final int SLOT_POWER = 4;
    public static final Minecraft mc = Minecraft.getMinecraft();
    public final PotionUtils pt = new PotionUtils();
    public TimerHelper time = TimerHelper.TimerHelperReseted();

    public int getIndexSearchSlotContainerAndBlackSlot(Item item, int ... blackslots) {
        for (int i = 0; i < Minecraft.player.openContainer.inventorySlots.size(); ++i) {
            Slot slot = Minecraft.player.openContainer.inventorySlots.get(i);
            ItemStack stack = slot.getStack();
            if (stack.isEmpty() || stack.getItem() != item) continue;
            boolean any = false;
            for (int blackslot : blackslots) {
                if (blackslot != i) continue;
                any = true;
                break;
            }
            if (any) continue;
            return i;
        }
        return -1;
    }

    private boolean bootlesSlotsHasAir() {
        int[] nArray = this.SLOTS_BOTTLES;
        int n = nArray.length;
        for (int i = 0; i < n; ++i) {
            Integer i2 = nArray[i];
            ItemStack itemStack = Minecraft.player.openContainer.inventorySlots.get(i2).getStack();
            if (!itemStack.getItem().equals(Items.air)) continue;
            return true;
        }
        return false;
    }

    public void handleBrewingStand(ContainerBrewingStand container, boolean splash, boolean boostEffect, String effectName, long swapTime) {
        int slot;
        int n = slot = splash ? PotionUtils.findPotionWithEffectContainer(new String[]{"Throw"}, new String[]{"Water"}, 0, 1, 2) : -1;
        if (!splash || slot == -1 && splash) {
            slot = PotionUtils.findPotionWithEffectContainer(new String[]{"Drink"}, new String[]{"Water"}, 0, 1, 2);
        }
        if (this.bootlesSlotsHasAir()) {
            String[] stringArray;
            if (splash) {
                String[] stringArray2 = new String[2];
                stringArray2[0] = "Drink";
                stringArray = stringArray2;
                stringArray2[1] = "Throw";
            } else {
                String[] stringArray3 = new String[1];
                stringArray = stringArray3;
                stringArray3[0] = "Drink";
            }
            int checkCount = PotionUtils.countPotionsWithEffectInInventory(stringArray, new String[]{"Water"}, 0, 1, 2);
            if (checkCount != -1 && this.time.hasReached(swapTime)) {
                ItemStack stack = Minecraft.player.openContainer.inventorySlots.get(checkCount).getStack();
                int[] nArray = this.SLOTS_BOTTLES;
                int n2 = nArray.length;
                for (int i = 0; i < n2; ++i) {
                    Integer i2 = nArray[i];
                    ItemStack itemStack = Minecraft.player.openContainer.inventorySlots.get(i2).getStack();
                    if (itemStack.getItem().equals(Items.air)) {
                        BrewUtil.mc.playerController.windowClick(container.windowId, slot, 0, ClickType.PICKUP, Minecraft.player);
                        BrewUtil.mc.playerController.windowClick(container.windowId, i2, 1, ClickType.PICKUP, Minecraft.player);
                        BrewUtil.mc.playerController.windowClick(container.windowId, slot, 0, ClickType.PICKUP, Minecraft.player);
                        break;
                    }
                    if (!stack.getItem().equals(Items.POTIONITEM) || PotionUtils.getPotionEffectType(itemStack).equals("Water")) continue;
                    BrewUtil.mc.playerController.windowClick(container.windowId, slot, 1, ClickType.THROW, Minecraft.player);
                }
            }
        } else {
            String type2 = splash ? "Throw" : "Drink";
            int[] nArray = this.SLOTS_BOTTLES;
            int n3 = nArray.length;
            for (int i = 0; i < n3; ++i) {
                Integer i3 = nArray[i];
                ItemStack itemStack = Minecraft.player.openContainer.inventorySlots.get(i3).getStack();
                if (effectName.startsWith("Strength")) {
                    this.brewPotions(container, itemStack, swapTime, i3, type2, new String[]{"Water", "Awkward", "Strength", "StrongStrength"}, "StrongStrength", new Item[]{Items.NETHER_WART, Items.BLAZE_POWDER, Items.GLOWSTONE_DUST});
                    continue;
                }
                if (effectName.startsWith("Speed")) {
                    this.brewPotions(container, itemStack, swapTime, i3, type2, new String[]{"Water", "Awkward", "Speed", "StrongSpeed"}, "StrongSpeed", new Item[]{Items.NETHER_WART, Items.SUGAR, Items.GLOWSTONE_DUST});
                    continue;
                }
                if (effectName.startsWith("Resistance")) {
                    this.brewPotions(container, itemStack, swapTime, i3, type2, new String[]{"Water", "Awkward", "Resistance", "LongResistance"}, "LongResistance", new Item[]{Items.NETHER_WART, Items.MAGMA_CREAM, Items.REDSTONE});
                    continue;
                }
                if (effectName.startsWith("Healing")) {
                    this.brewPotions(container, itemStack, swapTime, i3, type2, new String[]{"Water", "Awkward", "Healing", "StrongHealing"}, "StrongHealing", new Item[]{Items.NETHER_WART, Items.SPECKLED_MELON, Items.GLOWSTONE_DUST});
                    continue;
                }
                if (!effectName.startsWith("Regen")) continue;
                this.brewPotions(container, itemStack, swapTime, i3, type2, new String[]{"Water", "Awkward", "Regeneration", "StrongRegeneration"}, "StrongRegeneration", new Item[]{Items.NETHER_WART, Items.GHAST_TEAR, Items.GLOWSTONE_DUST});
            }
        }
        if (container.tileBrewingStand.getField(1) < 1) {
            int gettedSlot = this.getIndexSearchSlotContainerAndBlackSlot(Items.BLAZE_POWDER, 4);
            BrewUtil.mc.playerController.windowClick(container.windowId, gettedSlot, 0, ClickType.PICKUP, Minecraft.player);
            BrewUtil.mc.playerController.windowClick(container.windowId, 4, 1, ClickType.PICKUP, Minecraft.player);
            BrewUtil.mc.playerController.windowClick(container.windowId, gettedSlot, 1, ClickType.PICKUP, Minecraft.player);
        }
    }

    public void brewPotions(ContainerBrewingStand container, ItemStack itemStack, double swapTime, int i, String potionTy, String[] effectTy, String endSwap, Item[] items) {
        int[] slotsExcludingBlackSlots = PotionUtils.findSlotsByItemsExcludingBlackSlots(container, items, new int[]{3});
        if (this.checkBrewingProgress(container).equals("Not Brewing") && this.time.hasReached(swapTime)) {
            String potionType = potionTy;
            String[] desiredTypes = new String[]{potionType};
            String[] desiredEffectTypes = effectTy;
            for (int it = 0; it < desiredEffectTypes.length; ++it) {
                int slotToUse;
                String effectType = desiredEffectTypes[it];
                int n = slotToUse = slotsExcludingBlackSlots.length > it ? slotsExcludingBlackSlots[it] : -1;
                if (slotToUse == -1 || !PotionUtils.checkSlotsForPotion(desiredTypes, new String[]{effectType}, 0, 1, 2)) continue;
                BrewUtil.mc.playerController.windowClick(container.windowId, slotToUse, 0, ClickType.PICKUP, Minecraft.player);
                BrewUtil.mc.playerController.windowClick(container.windowId, 3, 1, ClickType.PICKUP, Minecraft.player);
                BrewUtil.mc.playerController.windowClick(container.windowId, slotToUse, 0, ClickType.PICKUP, Minecraft.player);
                this.time.reset();
                break;
            }
            if (PotionUtils.checkSlotsForPotion(desiredTypes, new String[]{endSwap}, 0, 1, 2)) {
                String potionTypeInSlot = PotionUtils.getPotionType(itemStack);
                String effectTypeInSlot = PotionUtils.getPotionEffectType(itemStack);
                if (potionTypeInSlot.equals(potionType) && effectTypeInSlot.equals(endSwap)) {
                    BrewUtil.mc.playerController.windowClick(container.windowId, i, i % 2, ClickType.QUICK_MOVE, Minecraft.player);
                }
            }
        }
    }

    private String checkBrewingProgress(ContainerBrewingStand container) {
        int brewTime = container.tileBrewingStand.getField(0);
        if (brewTime > 0) {
            if (brewTime == this.getBrewMaxTime()) {
                return "Start";
            }
            if (brewTime == this.getBrewMaxTime() / 2) {
                return "Mid";
            }
            if (brewTime == 1) {
                return "Finish";
            }
            return "In Progress";
        }
        return "Not Brewing";
    }

    public boolean hasAtLeastTwoWaterBottles() {
        int waterBottleCount = 0;
        for (int i = 0; i < 3; ++i) {
            ItemStack itemStack = Minecraft.player.openContainer.inventorySlots.get(i).getStack();
            if (!PotionUtils.getPotionEffectType(itemStack).equals("Water") && !PotionUtils.getPotionEffectType(itemStack).equals("Awkward")) {
                return false;
            }
            if (!PotionUtils.getPotionType(itemStack).equals("Drink") || !PotionUtils.getPotionEffectType(itemStack).equals("Water")) continue;
            ++waterBottleCount;
        }
        return waterBottleCount > 1;
    }

    public int getBrewMaxTime() {
        return 400;
    }
}

