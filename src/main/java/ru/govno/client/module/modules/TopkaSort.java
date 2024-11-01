package ru.govno.client.module.modules;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import org.lwjgl.input.Keyboard;
import ru.govno.client.module.Module;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.module.settings.FloatSettings;
import ru.govno.client.utils.Math.TimerHelper;

public class TopkaSort
extends Module {
    private final TimerHelper second = new TimerHelper();
    BoolSettings OnPressAltKey = new BoolSettings("OnPressAltKey", false, this);
    BoolSettings ForBowUser;
    BoolSettings ShlakoDrop;
    BoolSettings BadFood;
    BoolSettings BadArmor;
    BoolSettings LeatherArm;
    BoolSettings ChainmailArm;
    BoolSettings IronArm;
    BoolSettings GoldenArm;
    BoolSettings BadTools;
    BoolSettings WoodenTool;
    BoolSettings StoneTool;
    BoolSettings IronTool;
    BoolSettings GoldenTool;
    BoolSettings DropBlocks;
    BoolSettings OnlyInInv;
    FloatSettings DroperDelay;

    public TopkaSort() {
        super("TopkaSort", 0, Module.Category.PLAYER);
        this.settings.add(this.OnPressAltKey);
        this.DroperDelay = new FloatSettings("DroperDelay", 120.0f, 1000.0f, 50.0f, this);
        this.settings.add(this.DroperDelay);
        this.ForBowUser = new BoolSettings("ForBowUser", true, this);
        this.settings.add(this.ForBowUser);
        this.ShlakoDrop = new BoolSettings("ShlakoDrop", true, this);
        this.settings.add(this.ShlakoDrop);
        this.BadFood = new BoolSettings("BadFood", true, this);
        this.settings.add(this.BadFood);
        this.BadArmor = new BoolSettings("BadArmor", true, this);
        this.settings.add(this.BadArmor);
        this.LeatherArm = new BoolSettings("LeatherArm", true, this);
        this.settings.add(this.LeatherArm);
        this.ChainmailArm = new BoolSettings("ChainmailArm", true, this);
        this.settings.add(this.ChainmailArm);
        this.IronArm = new BoolSettings("IronArm", true, this);
        this.settings.add(this.IronArm);
        this.GoldenArm = new BoolSettings("GoldenArm", true, this);
        this.settings.add(this.GoldenArm);
        this.BadTools = new BoolSettings("BadTools", true, this);
        this.settings.add(this.BadTools);
        this.WoodenTool = new BoolSettings("WoodenTool", true, this);
        this.settings.add(this.WoodenTool);
        this.StoneTool = new BoolSettings("StoneTool", true, this);
        this.settings.add(this.StoneTool);
        this.IronTool = new BoolSettings("IronTool", true, this);
        this.settings.add(this.IronTool);
        this.GoldenTool = new BoolSettings("GoldenTool", true, this);
        this.settings.add(this.GoldenTool);
        this.DropBlocks = new BoolSettings("DropBlocks", true, this);
        this.settings.add(this.DropBlocks);
        this.OnlyInInv = new BoolSettings("OnlyInInv", false, this);
        this.settings.add(this.OnlyInInv);
    }

    @Override
    public void onUpdate() {
        if (!(TopkaSort.mc.currentScreen instanceof GuiInventory) && this.OnlyInInv.getBool()) {
            return;
        }
        if (!Keyboard.isKeyDown((int)56) && this.OnPressAltKey.getBool()) {
            return;
        }
        if ((TopkaSort.mc.currentScreen == null || TopkaSort.mc.currentScreen instanceof GuiInventory || TopkaSort.mc.currentScreen instanceof GuiChat) && this.second.hasReached(0.0)) {
            for (int i = 0; i < 45; ++i) {
                ItemStack is;
                if (!Minecraft.player.inventoryContainer.getSlot(i).getHasStack() || (is = Minecraft.player.inventoryContainer.getSlot(i).getStack()) == Minecraft.player.inventory.armorInventory.get(0) || is == Minecraft.player.inventory.armorInventory.get(1) || is == Minecraft.player.inventory.armorInventory.get(2) || is == Minecraft.player.inventory.armorInventory.get(3) || !this.shouldDrop(is, i) || !this.second.hasReached(this.DroperDelay.getFloat())) continue;
                TopkaSort.mc.playerController.windowClick(Minecraft.player.inventoryContainer.windowId, i, 1, ClickType.THROW, Minecraft.player);
                this.second.reset();
            }
        }
    }

    public boolean shouldDrop(ItemStack stack, int slot) {
        if (stack.getItem() instanceof ItemBlock && this.DropBlocks.getBool() && stack.getItem() != ItemBlock.getItemById(7) && stack.getItem() != ItemBlock.getItemById(33) && stack.getItem() != ItemBlock.getItemById(29) && stack.getItem() != ItemBlock.getItemById(46) && stack.getItem() != ItemBlock.getItemById(120) && stack.getItem() != ItemBlock.getItemById(41) && stack.getItem() != ItemBlock.getItemById(89) && stack.getItem() != ItemBlock.getItemById(14) && stack.getItem() != ItemBlock.getItemById(133) && stack.getItem() != ItemBlock.getItemById(129) && stack.getItem() != ItemBlock.getItemById(56) && stack.getItem() != ItemBlock.getItemById(57) && stack.getItem() != ItemBlock.getItemById(165) && stack.getItem() != ItemBlock.getItemById(52) && stack.getItem() != ItemBlock.getItemById(30) && stack.getItem() != ItemBlock.getItemById(49) && stack.getItem() != ItemBlock.getItemById(130) && stack.getItem() != ItemBlock.getItemById(49) && stack.getItem() != ItemBlock.getItemById(219) && stack.getItem() != ItemBlock.getItemById(220) && stack.getItem() != ItemBlock.getItemById(221) && stack.getItem() != ItemBlock.getItemById(222) && stack.getItem() != ItemBlock.getItemById(223) && stack.getItem() != ItemBlock.getItemById(224) && stack.getItem() != ItemBlock.getItemById(225) && stack.getItem() != ItemBlock.getItemById(226) && stack.getItem() != ItemBlock.getItemById(227) && stack.getItem() != ItemBlock.getItemById(228) && stack.getItem() != ItemBlock.getItemById(229) && stack.getItem() != ItemBlock.getItemById(230) && stack.getItem() != ItemBlock.getItemById(231) && stack.getItem() != ItemBlock.getItemById(232) && stack.getItem() != ItemBlock.getItemById(233) && stack.getItem() != ItemBlock.getItemById(234)) {
            return true;
        }
        if (!(this.ForBowUser.getBool() || stack.getItem() != Items.BOW && stack.getItem() != Items.ARROW && stack.getItem() != Items.TIPPED_ARROW)) {
            return true;
        }
        if (this.ShlakoDrop.getBool() && (stack.getItem() == Items.STICK || stack.getItem() == ItemBlock.getItemById(324) || stack.getItem() == ItemBlock.getItemById(330) || stack.getItem() == ItemBlock.getItemById(427) || stack.getItem() == ItemBlock.getItemById(428) || stack.getItem() == ItemBlock.getItemById(429) || stack.getItem() == ItemBlock.getItemById(430) || stack.getItem() == ItemBlock.getItemById(431) || stack.getItem() == ItemBlock.getItemById(328) || stack.getItem() == ItemBlock.getItemById(407) || stack.getItem() == ItemBlock.getItemById(342) || stack.getItem() == ItemBlock.getItemById(333) || stack.getItem() == ItemBlock.getItemById(444) || stack.getItem() == ItemBlock.getItemById(445) || stack.getItem() == ItemBlock.getItemById(446) || stack.getItem() == ItemBlock.getItemById(447) || stack.getItem() == ItemBlock.getItemById(448) || stack.getItem() == Items.BEETROOT_SEEDS || stack.getItem() == Items.WHEAT || stack.getItem() == Items.MELON_SEEDS || stack.getItem() == Items.PUMPKIN_SEEDS || stack.getItem() == Items.WHEAT_SEEDS || stack.getItem() == Items.DYE || stack.getItem() == Items.CLOCK || stack.getItem() == Items.COMPASS || stack.getItem() == Items.PAPER || stack.getItem() == Items.FISHING_ROD || stack.getItem() == Items.SLIME_BALL || stack.getItem() == Items.CLAY_BALL || stack.getItem() == Items.BONE || stack.getItem() == Items.BOWL || stack.getItem() == Items.CARROT_ON_A_STICK || stack.getItem() == Items.FEATHER || stack.getItem() == Items.GLASS_BOTTLE || stack.getItem() == Items.ENDER_EYE || stack.getItem() == Items.SADDLE || stack.getItem() == Items.SIGN || stack.getItem() == Items.MAP || stack.getItem() == Items.EGG || stack.getItem() == Items.ENDER_EYE || stack.getItem() == Items.HOPPER_MINECART || stack.getItem() == Items.FLOWER_POT || stack.getItem() == Items.LEATHER || stack.getItem() == Items.REEDS || stack.getItem() == Items.NAME_TAG || stack.getItem() == Items.SHEARS || stack.getItem() == Items.ENCHANTED_BOOK || stack.getItem() == Items.LEAD)) {
            return true;
        }
        if (this.BadFood.getBool() && (stack.getItem() == Items.ROTTEN_FLESH || stack.getItem() == Items.POTATO || stack.getItem() == Items.CHICKEN || stack.getItem() == Items.BEEF || stack.getItem() == Items.FISH || stack.getItem() == Items.MUTTON || stack.getItem() == Items.PORKCHOP || stack.getItem() == Items.RABBIT || stack.getItem() == Items.COOKIE)) {
            return true;
        }
        if (this.BadArmor.getBool() && ((stack.getItem() == Items.CHAINMAIL_BOOTS || stack.getItem() == Items.CHAINMAIL_LEGGINGS || stack.getItem() == Items.CHAINMAIL_CHESTPLATE || stack.getItem() == Items.CHAINMAIL_HELMET) && this.ChainmailArm.getBool() || (stack.getItem() == Items.GOLDEN_BOOTS || stack.getItem() == Items.GOLDEN_LEGGINGS || stack.getItem() == Items.GOLDEN_CHESTPLATE || stack.getItem() == Items.GOLDEN_HELMET) && this.GoldenArm.getBool() || (stack.getItem() == Items.LEATHER_BOOTS || stack.getItem() == Items.LEATHER_LEGGINGS || stack.getItem() == Items.LEATHER_CHESTPLATE || stack.getItem() == Items.LEATHER_HELMET) && this.LeatherArm.getBool() || (stack.getItem() == Items.IRON_BOOTS || stack.getItem() == Items.IRON_LEGGINGS || stack.getItem() == Items.IRON_CHESTPLATE || stack.getItem() == Items.IRON_HELMET) && this.IronArm.getBool())) {
            return true;
        }
        return this.BadTools.getBool() && ((stack.getItem() == Items.WOODEN_SWORD || stack.getItem() == Items.WOODEN_PICKAXE || stack.getItem() == Items.WOODEN_AXE || stack.getItem() == Items.WOODEN_SHOVEL || stack.getItem() == Items.WOODEN_HOE) && this.WoodenTool.getBool() || (stack.getItem() == Items.STONE_SWORD || stack.getItem() == Items.STONE_PICKAXE || stack.getItem() == Items.STONE_AXE || stack.getItem() == Items.STONE_SHOVEL || stack.getItem() == Items.STONE_HOE) && this.StoneTool.getBool() || (stack.getItem() == Items.IRON_SWORD || stack.getItem() == Items.IRON_PICKAXE || stack.getItem() == Items.IRON_AXE || stack.getItem() == Items.IRON_SHOVEL || stack.getItem() == Items.IRON_HOE) && this.IronTool.getBool() || (stack.getItem() == Items.GOLDEN_SWORD || stack.getItem() == Items.GOLDEN_PICKAXE || stack.getItem() == Items.GOLDEN_AXE || stack.getItem() == Items.GOLDEN_SHOVEL || stack.getItem() == Items.GOLDEN_HOE) && this.GoldenTool.getBool());
    }
}

