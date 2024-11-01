package ru.govno.client.module.modules;

import com.mojang.realmsclient.gui.ChatFormatting;
import java.awt.Color;
import java.util.ArrayList;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.network.play.server.SPacketMultiBlockChange;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import ru.govno.client.event.EventTarget;
import ru.govno.client.event.events.Event3D;
import ru.govno.client.event.events.EventReceivePacket;
import ru.govno.client.event.events.EventRender2D;
import ru.govno.client.module.Module;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.module.settings.FloatSettings;
import ru.govno.client.module.settings.ModeSettings;
import ru.govno.client.newfont.CFontRenderer;
import ru.govno.client.newfont.Fonts;
import ru.govno.client.utils.Render.ColorUtils;
import ru.govno.client.utils.Render.RenderUtils;

public class Xray
extends Module {
    ArrayList<BlockPos> ores = new ArrayList();
    ArrayList<BlockPos> toCheck = new ArrayList();
    public static int done;
    public static int all;
    public static Xray get;
    public ModeSettings XrayMode = new ModeSettings("XrayMode", "Default", this, new String[]{"Default", "BrutForce"});
    public FloatSettings CheckSpeed;
    public FloatSettings RadiusHorizontal;
    public FloatSettings RadiusVertical;
    public BoolSettings Diamond;
    public BoolSettings Redstone;
    public BoolSettings Emerald;
    public BoolSettings Quartz;
    public BoolSettings Lapis;
    public BoolSettings Gold;
    public BoolSettings Iron;
    public BoolSettings Coal;

    public Xray() {
        super("Xray", 0, Module.Category.PLAYER);
        this.settings.add(this.XrayMode);
        this.CheckSpeed = new FloatSettings("CheckSpeed", 3.0f, 10.0f, 1.0f, this, () -> this.XrayMode.getMode().equalsIgnoreCase("BrutForce"));
        this.settings.add(this.CheckSpeed);
        this.RadiusHorizontal = new FloatSettings("RadiusHorizontal", 20.0f, 100.0f, 0.0f, this, () -> this.XrayMode.getMode().equalsIgnoreCase("BrutForce"));
        this.settings.add(this.RadiusHorizontal);
        this.RadiusVertical = new FloatSettings("RadiusVertical", 8.0f, 30.0f, 0.0f, this, () -> this.XrayMode.getMode().equalsIgnoreCase("BrutForce"));
        this.settings.add(this.RadiusVertical);
        this.Diamond = new BoolSettings("Diamond", true, this);
        this.settings.add(this.Diamond);
        this.Redstone = new BoolSettings("Redstone", false, this);
        this.settings.add(this.Redstone);
        this.Emerald = new BoolSettings("Emerald", false, this);
        this.settings.add(this.Emerald);
        this.Quartz = new BoolSettings("Quartz", false, this);
        this.settings.add(this.Quartz);
        this.Lapis = new BoolSettings("Lapis", false, this);
        this.settings.add(this.Lapis);
        this.Gold = new BoolSettings("Gold", true, this);
        this.settings.add(this.Gold);
        this.Iron = new BoolSettings("Iron", true, this);
        this.settings.add(this.Iron);
        this.Coal = new BoolSettings("Coal", false, this);
        this.settings.add(this.Coal);
        get = this;
    }

    public static IBlockState getState(BlockPos pos) {
        return Xray.mc.world.getBlockState(pos);
    }

    public static ArrayList<BlockPos> getAllInBox(BlockPos from, BlockPos to) {
        ArrayList<BlockPos> blocks = new ArrayList<BlockPos>();
        BlockPos min = new BlockPos(Math.min(from.getX(), to.getX()), Math.min(from.getY(), to.getY()), Math.min(from.getZ(), to.getZ()));
        BlockPos max = new BlockPos(Math.max(from.getX(), to.getX()), Math.max(from.getY(), to.getY()), Math.max(from.getZ(), to.getZ()));
        for (int x = min.getX(); x <= max.getX(); ++x) {
            for (int y = min.getY(); y <= max.getY(); ++y) {
                for (int z = min.getZ(); z <= max.getZ(); ++z) {
                    blocks.add(new BlockPos(x, y, z));
                }
            }
        }
        return blocks;
    }

    @Override
    public void onToggled(boolean actived) {
        this.ores.clear();
        this.toCheck.clear();
        if (this.XrayMode.getMode().equalsIgnoreCase("BrutForce")) {
            int radXZ = (int)this.RadiusHorizontal.getFloat();
            int radY = (int)this.RadiusVertical.getFloat();
            ArrayList<BlockPos> blockPositions = this.getBlocks(radXZ, radY, radXZ);
            for (BlockPos pos : blockPositions) {
                IBlockState state = Xray.getState(pos);
                if (!this.isCheckableOre(Block.getIdFromBlock(state.getBlock()))) continue;
                this.toCheck.add(pos);
            }
        }
        all = this.toCheck.size();
        done = 0;
        if (this.XrayMode.getMode().equalsIgnoreCase("Default")) {
            Xray.mc.renderGlobal.loadRenderers();
        }
        super.onToggled(actived);
    }

    @Override
    public String getDisplayName() {
        if (this.actived && !this.XrayMode.getMode().equalsIgnoreCase("Default")) {
            return this.getDisplayByDouble((double)done / (double)all * 100.0) + "%";
        }
        return this.getName();
    }

    @Override
    @EventTarget
    public void onUpdate() {
        if (!this.actived) {
            return;
        }
        if (this.XrayMode.getMode().equalsIgnoreCase("BrutForce")) {
            for (int i = 0; i < this.CheckSpeed.getInt(); ++i) {
                if (this.toCheck.size() < 1) {
                    return;
                }
                BlockPos pos = this.toCheck.remove(0);
                ++done;
                mc.getConnection().sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, pos, EnumFacing.UP));
                mc.getConnection().sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, pos, EnumFacing.UP));
            }
        }
    }

    @EventTarget
    public void onReceivePacket(EventReceivePacket e) {
        if (!this.actived) {
            return;
        }
        if (this.XrayMode.getMode().equalsIgnoreCase("BrutForce")) {
            Packet object = e.getPacket();
            if (object instanceof SPacketBlockChange) {
                SPacketBlockChange p = (SPacketBlockChange)object;
                if (this.isCheckableOre(Block.getIdFromBlock(p.getBlockState().getBlock()))) {
                    this.ores.add(p.getBlockPosition());
                }
            } else {
                Packet packet = e.getPacket();
                if (packet instanceof SPacketMultiBlockChange) {
                    SPacketMultiBlockChange p = (SPacketMultiBlockChange)packet;
                    for (SPacketMultiBlockChange.BlockUpdateData dat : p.getChangedBlocks()) {
                        if (!this.isCheckableOre(Block.getIdFromBlock(dat.getBlockState().getBlock()))) continue;
                        this.ores.add(dat.getPos());
                    }
                }
            }
        }
    }

    @EventTarget
    public void onEvent(Event3D e) {
        if (!this.actived) {
            return;
        }
        if (this.XrayMode.getMode().equalsIgnoreCase("BrutForce")) {
            if (this.ores.isEmpty()) {
                return;
            }
            RenderUtils.setup3dForBlockPos(() -> this.ores.forEach(pos -> {
                IBlockState state = Xray.getState(pos);
                Block mat = state.getBlock();
                int color = 0;
                switch (Block.getIdFromBlock(mat)) {
                    case 56: {
                        color = ColorUtils.getColor(0, 255, 255);
                    }
                    case 14: {
                        color = ColorUtils.getColor(255, 215, 0);
                    }
                    case 15: {
                        color = ColorUtils.getColor(213, 213, 213);
                    }
                    case 129: {
                        color = ColorUtils.getColor(0, 255, 77);
                    }
                    case 153: {
                        color = ColorUtils.getColor(255, 255, 255);
                    }
                    case 73: {
                        color = ColorUtils.getColor(255, 0, 0);
                    }
                    case 16: {
                        color = ColorUtils.getColor(0, 0, 0);
                    }
                    case 21: {
                        color = ColorUtils.getColor(38, 97, 156);
                    }
                }
                if (color == 0) {
                    return;
                }
                int c1 = ColorUtils.swapAlpha(color, 150.0f);
                int c2 = ColorUtils.swapAlpha(color, 26.0f);
                AxisAlignedBB axis = Xray.mc.world.getBlockState((BlockPos)pos).getSelectedBoundingBox(Xray.mc.world, (BlockPos)pos);
                RenderUtils.drawCanisterBox(axis, true, false, true, c1, 0, c2);
            }), false);
        }
    }

    @EventTarget
    public void onRender2D(EventRender2D e) {
        if (!this.actived) {
            return;
        }
        if (this.XrayMode.getMode().equalsIgnoreCase("BrutForce")) {
            float a = done;
            float b = all;
            ScaledResolution sr = new ScaledResolution(mc);
            int valuePercent = (int)(a / b * 100.0f);
            int value = (int)(b * 100.0f);
            int color = ColorUtils.blendColors(new float[]{0.0f, 1.0f, 1.0f, 0.0f, 1.0f}, new Color[]{new Color(255, 0, 0), Color.GREEN, Color.YELLOW, Color.ORANGE, Color.RED}, a / b - 0.01f).brighter().getRGB();
            CFontRenderer font = Fonts.neverlose500_15;
            String text = ChatFormatting.LIGHT_PURPLE + "Produced: " + ChatFormatting.RESET + valuePercent + "%" + ChatFormatting.GRAY + " / " + ChatFormatting.GOLD + "Total: " + ChatFormatting.RED + "100%";
            RenderUtils.drawVGradientRect((float)sr.getScaledWidth() / 2.0f - (float)(font.getStringWidth(text) / 2) - 6.0f, 10.5f, (float)sr.getScaledWidth() / 2.0f + (float)(font.getStringWidth(text) / 2) + 6.0f, 25.0f, ColorUtils.getColor(12, 12, 12), 0);
            RenderUtils.drawGradientSideways((float)sr.getScaledWidth() / 2.0f - (float)(font.getStringWidth(text) / 2) - 4.5f, 11.5, (float)sr.getScaledWidth() / 2.0f + 5.5f, 12.5, color, 0);
            RenderUtils.drawGradientSideways((float)sr.getScaledWidth() / 2.0f - 5.5f, 11.5, (float)sr.getScaledWidth() / 2.0f + (float)(font.getStringWidth(text) / 2) + 4.5f, 12.5, 0, color);
            RenderUtils.drawVGradientRect((float)sr.getScaledWidth() / 2.0f - (float)(font.getStringWidth(text) / 2) - 5.0f, 12.0f, (float)sr.getScaledWidth() / 2.0f - (float)(font.getStringWidth(text) / 2) - 4.0f, 18.0f, color, 0);
            RenderUtils.drawVGradientRect((float)sr.getScaledWidth() / 2.0f + (float)(font.getStringWidth(text) / 2) + 4.0f, 12.0f, (float)sr.getScaledWidth() / 2.0f + (float)(font.getStringWidth(text) / 2) + 5.0f, 18.0f, color, 0);
            if (valuePercent == 100) {
                font.drawString(text, (float)sr.getScaledWidth() / 2.0f - (float)(font.getStringWidth(text) / 2), 16.5, ColorUtils.TwoColoreffect(new Color(24, 125, 24), new Color(12, 255, 12), (double)Math.abs(System.currentTimeMillis() / 4L) / 150.0 + 0.1275).getRGB());
            } else {
                font.drawString(text, (float)sr.getScaledWidth() / 2.0f - (float)(font.getStringWidth(text) / 2), 16.5, color);
            }
        }
    }

    private boolean isCheckableOre(int id) {
        if (id == 0) {
            return false;
        }
        int check = 0;
        int check1 = 0;
        int check2 = 0;
        int check3 = 0;
        int check4 = 0;
        int check5 = 0;
        int check6 = 0;
        if (this.Diamond.getBool()) {
            check = 56;
        }
        if (this.Gold.getBool()) {
            check1 = 14;
        }
        if (this.Iron.getBool()) {
            check2 = 15;
        }
        if (this.Emerald.getBool()) {
            check3 = 129;
        }
        if (this.Quartz.getBool()) {
            check3 = 153;
        }
        if (this.Redstone.getBool()) {
            check4 = 73;
        }
        if (this.Coal.getBool()) {
            check5 = 16;
        }
        if (this.Lapis.getBool()) {
            check6 = 21;
        }
        return id == check || id == check1 || id == check2 || id == check3 || id == check4 || id == check5 || id == check6;
    }

    private ArrayList<BlockPos> getBlocks(int x, int y, int z) {
        BlockPos min = new BlockPos(Minecraft.player.posX - (double)x, Minecraft.player.posY - (double)y, Minecraft.player.posZ - (double)z);
        BlockPos max = new BlockPos(Minecraft.player.posX + (double)x, Minecraft.player.posY + (double)y, Minecraft.player.posZ + (double)z);
        return Xray.getAllInBox(min, max);
    }
}

