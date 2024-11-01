package ru.govno.client.module.modules;

import java.util.List;
import java.util.stream.IntStream;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.init.Items;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import org.lwjgl.opengl.GL11;
import ru.govno.client.module.Module;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.newfont.CFontRenderer;
import ru.govno.client.newfont.Fonts;
import ru.govno.client.utils.Math.MathUtils;
import ru.govno.client.utils.Render.RenderUtils;

public class Overlay
extends Module {
    public static Overlay get;
    BoolSettings customHotbar = new BoolSettings("Hotbar rework", true, this);

    public Overlay() {
        super("Overlay", 0, Module.Category.RENDER);
        this.settings.add(this.customHotbar);
        get = this;
    }

    @Override
    public void alwaysRender2D(ScaledResolution sr) {
        this.stateAnim.to = this.isActived() ? 1.0f : 0.0f;
        this.stateAnim.getAnim();
        if (MathUtils.getDifferenceOf(this.stateAnim.anim, this.stateAnim.to) < (double)0.03f) {
            this.stateAnim.setAnim(this.stateAnim.to);
        }
    }

    public void onHotbarRender(ScaledResolution sr, Runnable renderDefault) {
        if (this.customHotbar.canBeRender()) {
            float anim = this.customHotbar.getAnimation() * this.stateAnim.anim;
            float defaultApc = Math.max(1.0f - anim * 2.0f, 0.0f);
            float customApc = Math.min((anim - 0.5f) * 2.0f, 1.0f);
            if (defaultApc != 0.0f) {
                GL11.glPushMatrix();
                RenderUtils.customRotatedObject2D(0.0f, sr.getScaledHeight(), sr.getScaledWidth(), 0.0f, defaultApc);
                renderDefault.run();
                GL11.glPopMatrix();
            } else if (customApc != 1.0f) {
                GL11.glPushMatrix();
                RenderUtils.customRotatedObject2D(0.0f, sr.getScaledHeight(), sr.getScaledWidth(), 0.0f, customApc);
                this.renderCustomHotbar(sr);
                GL11.glPopMatrix();
            } else {
                this.renderCustomHotbar(sr);
            }
        } else {
            renderDefault.run();
        }
    }

    private void renderCustomHotbar(ScaledResolution sr) {
        float x1;
        int pixScale = 16;
        int xPadding = 3;
        int yOffset = 6;
        int yPadding = 2;
        int centerItemsUpPix = 6;
        int slot = Minecraft.player.inventory.currentItem;
        List<ItemStack> stacks = IntStream.range(0, 9).mapToObj(IInt -> Minecraft.player.inventory.getStackInSlot(IInt)).toList();
        float x = x1 = (float)sr.getScaledWidth() / 2.0f - (float)(stacks.size() * pixScale + (stacks.size() - 1) * xPadding) / 2.0f;
        float y1 = sr.getScaledHeight() - pixScale - yOffset;
        for (int index = 0; index < stacks.size(); ++index) {
            ItemStack stack = stacks.get(index);
            float centerPC = (float)MathUtils.easeInOutQuadWave((float)index / (float)stacks.size());
            boolean selected = slot == index;
            float y = y1 - (float)centerItemsUpPix * centerPC;
            this.drawItemStack(stack, x, y, (int)((float)pixScale * (selected ? 1.5f : 1.0f)), Fonts.neverlose500_13, selected);
            x += (float)pixScale;
            if (index == stacks.size() - 1) continue;
            x += (float)xPadding;
        }
    }

    private void drawItemStack(ItemStack stack, float x, float y, int pixScale, CFontRenderer font, boolean selected) {
        float scale = (float)pixScale / 16.0f;
        if (stack.isEmpty() || stack.getItem() == Items.air) {
            return;
        }
        if (scale == 0.0f) {
            return;
        }
        if (scale != 1.0f) {
            GL11.glPushMatrix();
            RenderUtils.customScaledObject2D(x, y, 16.0f, 16.0f, scale);
        }
        GL11.glTranslated((double)x, (double)y, (double)0.0);
        if (Minecraft.player.isSwingInProgress && selected) {
            GL11.glPushMatrix();
            RenderUtils.customRotatedObject2D(0.0f, 0.0f, 16.0f, 16.0f, MathUtils.easeInOutQuadWave(Minecraft.player.swingProgress + mc.getRenderPartialTicks() / 10.0f) * 30.0);
        }
        if (Minecraft.player.getActiveHand() == EnumHand.MAIN_HAND && Minecraft.player.isHandActive() && selected) {
            float scaleBlob;
            GL11.glPushMatrix();
            if (Minecraft.player.getActiveItemStack() != null && (Minecraft.player.getActiveItemStack().getItem() instanceof ItemFood || Minecraft.player.getActiveItemStack().getItem() instanceof ItemPotion)) {
                float usePC = Math.min(((float)Minecraft.player.getItemInUseMaxCount() + mc.getRenderPartialTicks()) / 32.0f, 1.0f);
                scaleBlob = 1.0f + (float)MathUtils.easeInOutQuadWave(MathUtils.valWave01(MathUtils.valWave01(usePC))) * usePC / 3.0f;
            } else {
                float usePC = Math.min(((float)Minecraft.player.getItemInUseMaxCount() + mc.getRenderPartialTicks()) / 8.0f, 1.0f);
                scaleBlob = usePC * 0.3333f + 6666.0f;
            }
            RenderUtils.customScaledObject2D(0.0f, 0.0f, 16.0f, 16.0f, scaleBlob);
        }
        mc.getRenderItem().renderItemIntoGUI(stack, 0, 0);
        if (Minecraft.player.getActiveHand() == EnumHand.MAIN_HAND && Minecraft.player.isHandActive() && selected) {
            GL11.glPopMatrix();
        }
        if (Minecraft.player.isSwingInProgress && selected) {
            GL11.glPopMatrix();
        }
        mc.getRenderItem().renderItemOverlays(font, stack, 0, 0);
        GL11.glTranslated((double)(-x), (double)(-y), (double)0.0);
        if (scale != 1.0f) {
            GL11.glPopMatrix();
        }
    }
}

