package ru.govno.client.clickgui;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;
import ru.govno.client.Client;
import ru.govno.client.module.Module;
import ru.govno.client.module.modules.ClickGui;
import ru.govno.client.module.modules.ClientTune;
import ru.govno.client.newfont.Fonts;
import ru.govno.client.utils.Math.MathUtils;
import ru.govno.client.utils.Render.AnimationUtils;
import ru.govno.client.utils.Render.ColorUtils;
import ru.govno.client.utils.Render.RenderUtils;
import ru.govno.client.utils.Render.StencilUtil;

public class Panel {
    AnimationUtils anim = new AnimationUtils(0.0F, 0.0F, 0.15F);
    AnimationUtils rotate = new AnimationUtils(0.0F, 0.0F, 0.075F);
    AnimationUtils press = new AnimationUtils(0.0F, 0.0F, 0.075F);
    AnimationUtils drag = new AnimationUtils(0.0F, 0.0F, 0.1F);
    int oldMouseX;
    int oldMouseY;
    float height = 0.0F;
    boolean wantToClose = false;
    public boolean callClicked;
    ArrayList<Mod> mods = new ArrayList<>();
    public Module.Category category;
    public float X;
    public float Y;
    public AnimationUtils posX = new AnimationUtils(0.0F, 0.0F, 0.13F);
    public AnimationUtils posY = new AnimationUtils(0.0F, 0.0F, 0.13F);
    boolean open = true;
    boolean dragging = false;
    float dragX;
    float dragY;
    AnimationUtils animLine = new AnimationUtils(0.0F, 0.0F, 0.1F);
    AnimationUtils animOpen = new AnimationUtils(0.0F, this.getHeight(), 0.1F);
    boolean wantToClick = true;

    public Panel(Module.Category category) {
        this.category = category;
        List<Module> sorted = Client.moduleManager
                .getModuleList()
                .stream()
                .filter(mod -> mod.category == category && mod.isVisible())
                .collect(Collectors.toList());
        sorted.forEach(mod -> this.mods.add(new Mod(mod, mod == sorted.get(sorted.size() - 1), mod == sorted.get(0))));
    }

    public Panel() {
    }

    public int getColor(int step) {
        return ClickGuiScreen.getColor(step, this.category);
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (ClickGuiScreen.colose) {
            this.dragging = false;
        }

        this.height = this.getHeight();
        int ScaledAlpha = (int)ClickGuiScreen.globalAlpha.anim;
        float ScaledAlphaPercent = (float)ScaledAlpha / 255.0F;
        float width = this.getWidth();
        GL11.glPushMatrix();
        this.posX.getAnim();
        if (this.posX.to == this.X
                && MathUtils.getDifferenceOf(this.posX.anim, this.posX.to) < 0.001
                && MathUtils.getDifferenceOf(this.posX.anim, this.posX.to) != 0.0
                || this.X == 0.0F) {
            this.posX.setAnim(this.X == 0.0F ? 0.0F : this.posX.to);
        }

        this.posY.getAnim();
        if (this.posY.to == this.Y
                && MathUtils.getDifferenceOf(this.posY.anim, this.posY.to) < 0.001
                && MathUtils.getDifferenceOf(this.posY.anim, this.posY.to) != 0.0
                || this.Y == 0.0F) {
            this.posY.setAnim(this.Y == 0.0F ? 0.0F : this.posY.to);
        }

        this.posX.to = (float)((int)this.X) + ((double)(this.X - (float)((int)this.X)) >= 0.5 ? 0.5F : 0.0F);
        this.posY.to = (float)((int)this.Y) + ((double)(this.Y - (float)((int)this.Y)) >= 0.5 ? 0.5F : 0.0F);
        ClickGui.setPositionPanel(this, this.X, this.Y);
        if (this.dragging) {
            this.drag.to = 1.016F;
            this.X = (float)mouseX - this.dragX;
            this.Y = (float)mouseY - this.dragY;
            this.rotate.to = (this.posX.anim - this.posX.to) / 7.5F;
            this.press.to = (this.posY.anim - this.posY.to) / 7.5F;
        } else {
            this.drag.to = 1.0F;
            this.rotate.to = -ClickGuiScreen.scrollSmoothX;
            this.press.to = 0.0F;
            if (MathUtils.getDifferenceOf(0.0F, ClickGuiScreen.scrollSmoothX) < 0.01 && MathUtils.getDifferenceOf(0.0F, ClickGuiScreen.scrollSmoothY) < 0.01) {
                float[] pos = ClickGui.getPositionPanel(this);
                this.X = pos[0];
                this.Y = pos[1];
            }
        }

        this.drag.getAnim();
        if (this.ishover(this.X - 5.0F, this.Y - 5.0F, this.X + width + 5.0F, this.Y + 27.0F, mouseX, mouseY)) {
            ClickGuiScreen.resetHolds();
        }

        this.anim.getAnim();
        this.anim.to = this.wantToClose && this.open ? 1.0F : 0.0F;
        if (this.open && this.wantToClose && (double)this.anim.anim < 0.1) {
            this.open = false;
            this.wantToClose = false;
            ClientTune.get.playGuiPenelOpenOrCloseSong(false);
        }

        this.animOpen.getAnim();
        this.animOpen.speed = 0.15F + (this.open && Math.abs(MathUtils.getDifferenceOf(this.animOpen.anim, this.height)) < 2.5 ? 0.5F : 0.0F);
        this.animOpen.to = this.height + 1.5F;
        if (MathUtils.getDifferenceOf(this.animOpen.anim, this.height + 1.5F) < (double)((this.height + 1.5F) / ((float)this.mods.size() * 2.0F))) {
            this.animOpen.setAnim(this.height + 1.5F);
        }

        this.animLine.to = MathUtils.clamp(
                ClickGuiScreen.scale.anim * (ClickGuiScreen.globalAlpha.anim / 255.0F) * ClickGuiScreen.scale.anim, 0.0F, this.open ? 1.0F : 0.37F
        );
        this.animLine.getAnim();
        GL11.glTranslated(
                (double)(this.posX.anim + (this.dragging ? this.dragX : width / 2.0F)), (double)(this.posY.anim + (this.dragging ? this.dragY : 12.0F)), 0.0
        );
        GL11.glRotatef(MathUtils.clamp(-this.rotate.getAnim() * 4.0F, -95.0F, 95.0F), 0.0F, 0.0F, 1.0F);
        GL11.glScaled(1.0, (double)(1.0F + this.press.getAnim() / 30.0F), 1.0);
        GL11.glTranslated(
                (double)(-(this.posX.anim + (this.dragging ? this.dragX : width / 2.0F))), (double)(-(this.posY.anim + (this.dragging ? this.dragY : 12.0F))), 0.0
        );
        int cli1 = ColorUtils.swapAlpha(ClickGuiScreen.getColor(0, this.category), 85.0F * ScaledAlphaPercent);
        int cli2 = ColorUtils.swapAlpha(ClickGuiScreen.getColor(-324, this.category), 85.0F * ScaledAlphaPercent);
        int cli3 = ColorUtils.swapAlpha(ClickGuiScreen.getColor(0, this.category), 45.0F * ScaledAlphaPercent);
        int cli4 = ColorUtils.swapAlpha(ClickGuiScreen.getColor(972, this.category), 45.0F * ScaledAlphaPercent);
        int col1 = ColorUtils.getColor(9, 9, 9, ClickGuiScreen.globalAlpha.anim / 1.6F * ClickGuiScreen.scale.anim);
        int col2 = ColorUtils.getColor(6, 6, 6, ClickGuiScreen.globalAlpha.anim / 1.3F * ClickGuiScreen.scale.anim);
        RenderUtils.drawOutsideAndInsideFullRoundedFullGradientShadowRectWithBloomBool(
                this.posX.anim + 0.5F,
                this.posY.anim + 0.5F,
                this.posX.anim + this.getWidth() - 0.5F,
                this.posY.anim + this.animOpen.anim - 3.5F,
                5.0F * ScaledAlphaPercent,
                cli1,
                cli2,
                cli3,
                cli4,
                true
        );
        RenderUtils.fixShadows();
        RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(
                this.posX.anim + 0.5F,
                this.posY.anim + 0.5F,
                this.posX.anim + this.getWidth() - 0.5F,
                this.posY.anim + this.animOpen.anim - 3.5F,
                4.0F,
                0.5F,
                col1,
                col1,
                col2,
                col2,
                false,
                true,
                true
        );
        RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(
                this.posX.anim,
                this.posY.anim,
                this.posX.anim + this.getWidth(),
                this.posY.anim + this.animOpen.anim - 3.0F,
                4.0F,
                0.5F,
                ColorUtils.toDark(cli1, 0.4F),
                ColorUtils.toDark(cli2, 0.4F),
                ColorUtils.toDark(cli3, 0.4F),
                ColorUtils.toDark(cli4, 0.4F),
                true,
                true,
                true
        );
        RenderUtils.drawFullGradientRectPro(
                this.posX.anim + 1.0F,
                this.posY.anim + 20.0F + 6.0F * (1.0F - this.animLine.anim),
                this.posX.anim + this.getWidth() - 1.0F,
                this.posY.anim + 23.5F,
                0,
                0,
                ColorUtils.getColor(9, 9, 9, ClickGuiScreen.globalAlpha.anim / 2.2F * ClickGuiScreen.scale.anim),
                ColorUtils.getColor(9, 9, 9, ClickGuiScreen.globalAlpha.anim / 2.2F * ClickGuiScreen.scale.anim),
                false
        );
        RenderUtils.fixShadows();
        GlStateManager.resetColor();
        float extend = 18.0F;
        float exX = this.category == Module.Category.MOVEMENT
                ? -0.5F
                : (this.category == Module.Category.RENDER ? -1.0F : (this.category == Module.Category.PLAYER ? -0.5F : 0.0F));
        float exY = this.category == Module.Category.PLAYER ? -0.5F : (this.category == Module.Category.MISC ? 3.0F : 0.0F);
        float yho = 2.0F - this.animLine.anim * 2.0F;
        int setB = ColorUtils.swapAlpha(
                ClickGuiScreen.getColor((int)this.posX.anim / 20, this.category), (float)((int)(90.0F * this.animLine.anim * ScaledAlphaPercent))
        );
        RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(
                this.posX.anim + 2.5F + yho,
                this.posY.anim + 2.5F + yho,
                this.posX.anim + extend + yho - 1.0F,
                this.posY.anim + extend + yho - 1.0F,
                3.0F,
                1.0F,
                setB,
                ColorUtils.toDark(setB, 0.75F),
                ColorUtils.toDark(setB, 0.2F),
                ColorUtils.toDark(setB, 0.5F),
                true,
                true,
                true
        );
        String cattegoryed = this.category == Module.Category.COMBAT
                ? "e"
                : (
                this.category == Module.Category.MOVEMENT
                        ? "d"
                        : (
                        this.category == Module.Category.RENDER
                                ? "f"
                                : (this.category == Module.Category.PLAYER ? "b" : (this.category == Module.Category.MISC ? "c" : ""))
                )
        );
        int texC = ColorUtils.swapAlpha(-1, MathUtils.clamp(this.animLine.anim * ScaledAlphaPercent * 220.0F, 0.0F, 255.0F));
        if (RenderUtils.alpha(texC) >= 33) {
            RenderUtils.fixShadows();
            Fonts.iconswex_36
                    .drawString(cattegoryed, (double)(this.posX.anim + 2.5F + exX + yho + 0.5F), (double)(this.posY.anim + 5.5F + exY + yho + 0.5F), texC);
            String reCategory = this.category.name().charAt(0) + this.category.name().substring(1).toLowerCase();
            Fonts.comfortaaRegular_22.drawString(reCategory, (double)(this.posX.anim + 4.0F + extend + yho), (double)(this.posY.anim + 6.0F + yho), texC);
            texC = ColorUtils.swapAlpha(texC, (float)ColorUtils.getAlphaFromColor(texC) / 5.0F);
            if (ColorUtils.getAlphaFromColor(texC) >= 33) {
                Fonts.comfortaaRegular_22
                        .drawString(reCategory, (double)(this.posX.anim + 4.0F + extend + yho + 1.0F), (double)(this.posY.anim + 6.0F + yho), texC);
                Fonts.comfortaaRegular_22
                        .drawString(reCategory, (double)(this.posX.anim + 4.0F + extend + yho - 1.0F), (double)(this.posY.anim + 6.0F + yho), texC);
                Fonts.comfortaaRegular_22
                        .drawString(reCategory, (double)(this.posX.anim + 4.0F + extend + yho), (double)(this.posY.anim + 6.0F + yho + 1.0F), texC);
                Fonts.comfortaaRegular_22
                        .drawString(reCategory, (double)(this.posX.anim + 4.0F + extend + yho), (double)(this.posY.anim + 6.0F + yho - 1.0F), texC);
                texC = ColorUtils.swapAlpha(texC, (float)ColorUtils.getAlphaFromColor(texC) / 1.25F);
                if (ColorUtils.getAlphaFromColor(texC) >= 33) {
                    Fonts.iconswex_36
                            .drawString(cattegoryed, (double)(this.posX.anim + 2.5F + exX + yho + 0.5F + 1.0F), (double)(this.posY.anim + 5.5F + exY + yho + 0.5F), texC);
                    Fonts.iconswex_36
                            .drawString(cattegoryed, (double)(this.posX.anim + 2.5F + exX + yho + 0.5F - 1.0F), (double)(this.posY.anim + 5.5F + exY + yho + 0.5F), texC);
                    Fonts.iconswex_36
                            .drawString(cattegoryed, (double)(this.posX.anim + 2.5F + exX + yho + 0.5F), (double)(this.posY.anim + 5.5F + exY + yho + 1.5F), texC);
                    Fonts.iconswex_36
                            .drawString(cattegoryed, (double)(this.posX.anim + 2.5F + exX + yho + 0.5F), (double)(this.posY.anim + 5.5F + exY + yho - 0.5F), texC);
                }
            }
        }

        if (this.dragging || (double)this.drag.anim > 1.001) {
            int outAlpha = (int)(37.0 * (double)(this.drag.anim - 1.0F) * 261.375);
            int ouC = ColorUtils.getColor(255 - outAlpha / 5, (int)((float)outAlpha * this.animLine.getAnim()));
            float ext = 2.0F;
            RenderUtils.drawOutsideAndInsideFullRoundedFullGradientShadowRectWithBloomBoolShadowsBoolChangeShadowSize(
                    this.posX.getAnim() - ext - this.drag.anim,
                    this.posY.getAnim() - ext - this.drag.anim,
                    this.posX.anim + this.getWidth() + ext + this.drag.anim,
                    this.posY.anim + this.animOpen.anim + this.drag.anim - 1.0F,
                    5.0F,
                    2.5F,
                    1.5F,
                    ouC,
                    ouC,
                    ouC,
                    ouC,
                    true,
                    true,
                    true
            );
            RenderUtils.fixShadows();
        }

        int step = 1;
        int i = 23;
        if (this.animOpen.getAnim() > 27.5F) {
            for (Mod mod : this.mods) {
                if (MathUtils.getDifferenceOf(this.animOpen.getAnim() - 2.5F, this.height - 1.0F) > 0.5) {
                    StencilUtil.initStencilToWrite();
                    RenderUtils.drawRect(
                            (double)this.posX.anim,
                            (double)(this.posY.anim + 24.0F),
                            (double)(this.posX.anim + this.getWidth()),
                            (double)(this.posY.anim + this.animOpen.anim - 2.0F),
                            -1
                    );
                    StencilUtil.readStencilBuffer(1);
                } else if (mod == this.mods.get(0) || mod == this.mods.get(this.mods.size() - 1)) {
                    StencilUtil.initStencilToWrite();
                    RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(
                            this.posX.anim + 0.5F,
                            this.posY.anim + 23.5F,
                            this.posX.anim + this.getWidth() - 0.5F,
                            this.posY.anim + this.animOpen.anim - 3.5F,
                            3.5F,
                            0.0F,
                            -1,
                            -1,
                            -1,
                            -1,
                            false,
                            true,
                            false
                    );
                    StencilUtil.readStencilBuffer(1);
                }

                if (this.anim.anim * this.height > (float)i) {
                    step++;
                }

                try {
                    if ((float)(i + 10) < this.animOpen.getAnim()) {
                        mod.drawScreen(this.posX.getAnim(), this.posY.getAnim() + (float)i, step, mouseX, mouseY, partialTicks);
                    }

                    if (!this.open) {
                        mod.open = false;
                        mod.openAnim.to = 16.0F;
                    }
                } catch (Exception var25) {
                }

                i = (int)((float)i + (mod.openAnim.anim > 20.15F ? mod.openAnim.anim + 0.5F : mod.openAnim.anim) + 1.0F);
                if (MathUtils.getDifferenceOf(this.animOpen.anim - 2.5F, this.height - 1.0F) > 0.5
                        || mod == this.mods.get(0)
                        || mod == this.mods.get(this.mods.size() - 1)) {
                    StencilUtil.uninitStencilBuffer();
                }
            }
        }

        GL11.glPopMatrix();
        this.oldMouseX = mouseX;
        this.oldMouseY = mouseY;
    }

    public void keyPressed(int key) {
        if (this.open) {
            for (Mod mod : this.mods) {
                mod.keyPressed(key);
            }
        }
    }

    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        this.wantToClick = true;
        if (mouseButton == 0) {
            this.dragging = false;
        }

        if (this.open) {
            for (Mod mod : this.mods) {
                mod.mouseReleased(mouseX, mouseY, mouseButton);
            }
        }
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        this.callClicked = this.ishover(this.X, this.Y, this.X + this.getWidth(), this.Y + this.height, mouseX, mouseY);
        if (this.wantToClick && this.ishover(this.X, this.Y, this.X + this.getWidth(), this.Y + (float)(this.open ? 20 : 22), mouseX, mouseY)) {
            if (Client.clickGuiScreen.panels.stream().noneMatch(panel -> panel.dragging)) {
                if (mouseButton == 0) {
                    this.dragging = true;
                    this.dragX = (float)mouseX - this.X;
                    this.dragY = (float)mouseY - this.Y;
                } else if (mouseButton == 1) {
                    if (!this.open) {
                        this.open = true;
                        ClientTune.get.playGuiPenelOpenOrCloseSong(true);
                    } else {
                        this.wantToClose = true;
                        this.anim.to = 0.0F;
                        boolean playCliseMod = false;

                        for (Mod mod : this.mods) {
                            if (mod.binding) {
                                playCliseMod = true;
                                mod.binding = false;
                            }
                        }

                        if (playCliseMod) {
                            ClientTune.get.playGuiModuleBindingToggleSong(false);
                        }
                    }
                }
            }

            this.wantToClick = false;
        }

        if (this.open) {
            int i = 26;

            for (Mod modx : this.mods) {
                modx.mouseClicked((int)this.X, (int)this.Y + i, mouseX, mouseY, mouseButton);
                i = (int)((float)i + modx.openAnim.anim + 1.0F);
            }
        }
    }

    public float getHeight() {
        float i = 25.0F;
        if (this.open) {
            for (Mod mod : this.mods) {
                i += mod.getHeight() + 1.0F;
            }

            i += 0.5F;
        }

        return i;
    }

    public float getWidth() {
        return 120.0F;
    }

    public void onCloseGui() {
        this.mods.forEach(mod -> mod.onGuiClosed());
        this.dragging = false;
    }

    public boolean ishover(float x1, float y1, float x2, float y2, int mouseX, int mouseY) {
        return (float)mouseX >= x1 && (float)mouseX <= x2 && (float)mouseY >= y1 && (float)mouseY <= y2;
    }
}
