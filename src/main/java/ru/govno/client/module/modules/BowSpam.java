package ru.govno.client.module.modules;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemBow;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.input.Mouse;
import ru.govno.client.module.Module;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.module.settings.FloatSettings;

public class BowSpam
extends Module {
    FloatSettings Charge = new FloatSettings("Charge", 4.0f, 20.0f, 2.0f, this);
    BoolSettings OnlyTightly;

    public BowSpam() {
        super("BowSpam", 0, Module.Category.COMBAT);
        this.settings.add(this.Charge);
        this.OnlyTightly = new BoolSettings("OnlyTightly", true, this);
        this.settings.add(this.OnlyTightly);
    }

    @Override
    public void onUpdateMovement() {
        if (Minecraft.player.isBowing() && Mouse.isButtonDown((int)1) && (!this.OnlyTightly.getBool() || BowSpam.mc.pointedEntity != null && (double)Minecraft.player.getDistanceToEntity(BowSpam.mc.pointedEntity) <= 2.9) && (double)Minecraft.player.getItemInUseMaxCount() * BowSpam.mc.timer.speed > (double)this.Charge.getFloat()) {
            EnumHand active = Minecraft.player.getActiveHand();
            BowSpam.mc.playerController.onStoppedUsingItem(Minecraft.player);
            Minecraft.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, Minecraft.player.getHorizontalFacing()));
            if (active != null) {
                Minecraft.player.connection.sendPacket(new CPacketPlayerTryUseItem(active));
                if (Minecraft.player.getHeldItemMainhand().getItem() instanceof ItemBow) {
                    Minecraft.player.setActiveHand(EnumHand.MAIN_HAND);
                } else if (Minecraft.player.getHeldItemOffhand().getItem() instanceof ItemBow) {
                    Minecraft.player.setActiveHand(EnumHand.OFF_HAND);
                }
            }
        }
    }
}

