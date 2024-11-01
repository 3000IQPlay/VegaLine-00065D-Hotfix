package ru.govno.client.utils.UControllers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import ru.govno.client.Client;
import ru.govno.client.cfg.GuiConfig;
import ru.govno.client.clickgui.ClickGuiScreen;
import ru.govno.client.event.EventManager;
import ru.govno.client.event.EventTarget;
import ru.govno.client.event.events.EventMoveKeys;
import ru.govno.client.event.events.EventPlayerMotionUpdate;
import ru.govno.client.event.events.EventRender2D;
import ru.govno.client.module.modules.ClickGui;
import ru.govno.client.module.modules.InvWalk;
import ru.govno.client.utils.Math.MathUtils;
import ru.govno.client.utils.Math.TimerHelper;
import ru.govno.client.utils.UControllers.Dualsense;

public class DualsenseIntegration {
    Minecraft mc = Minecraft.getMinecraft();
    private boolean tempCheckActive;
    private boolean tempCheckClickMouseLeft;
    private boolean tempCheckClickMouseRight;
    private boolean tempCheckClickMouseLeft2;
    private boolean tempCheckClickMouseRight2;
    private boolean tempCheckMovementInput;
    private boolean tempCheckJumpOrSneak;
    private boolean tempCheckClickE;
    private boolean tempCheckClickESC;
    private boolean tempCheckPS;
    TimerHelper test = new TimerHelper();

    public void updateEventsState() {
        if (Dualsense.CONTROLLER_LOADED && !this.tempCheckActive) {
            EventManager.register(this);
            this.tempCheckActive = true;
        } else if (!Dualsense.CONTROLLER_LOADED && this.tempCheckActive) {
            EventManager.unregister(this);
        }
    }

    @EventTarget
    public void onMovementInput(EventMoveKeys event) {
        if (!Dualsense.CONTROLLER_LOADED || !this.canWalkInput()) {
            return;
        }
        boolean[] wasd = Dualsense.getWasdInputFromSenseAxis(true, 8, 50L);
        System.out.println(this.test.getTime());
        this.test.reset();
        if (Dualsense.SenseAxis.LEFT_AXIS.hasAxisMove(Dualsense.SenseAxis.LEFT_AXIS.getInputValues())) {
            event.setForwardKeyDown(wasd[0]);
            event.setBackKeyDown(wasd[2]);
            event.setLeftKeyDown(wasd[1]);
            event.setRightKeyDown(wasd[3]);
            this.mc.gameSettings.keyBindForward.pressed = event.isForwardKeyDown();
            this.mc.gameSettings.keyBindBack.pressed = event.isBackKeyDown();
            this.mc.gameSettings.keyBindLeft.pressed = event.isLeftKeyDown();
            this.mc.gameSettings.keyBindRight.pressed = event.isRightKeyDown();
            this.tempCheckMovementInput = true;
        } else if (this.tempCheckMovementInput) {
            this.mc.gameSettings.keyBindForward.pressed = false;
            this.mc.gameSettings.keyBindBack.pressed = false;
            this.mc.gameSettings.keyBindLeft.pressed = false;
            this.mc.gameSettings.keyBindRight.pressed = false;
            this.tempCheckMovementInput = false;
        }
    }

    @EventTarget
    public void onUpdate(EventPlayerMotionUpdate event) {
        if (!Dualsense.CONTROLLER_LOADED) {
            return;
        }
        if (this.canRotateCamera()) {
            float[] inputValues = Dualsense.SenseAxis.RIGHT_AXIS.getInputValues();
            Minecraft.player.rotationYaw += inputValues[0] * 18.0f;
            Minecraft.player.rotationPitch += inputValues[1] * 12.0f;
            Minecraft.player.rotationPitch = MathUtils.clamp(Minecraft.player.rotationPitch, -90.0f, 90.0f);
        }
        if (this.canInteractInput()) {
            Dualsense.mc.gameSettings.keyBindAttack.pressed = Dualsense.R2_PRESSED;
            if (Dualsense.R2_PRESSED) {
                if (!this.tempCheckClickMouseLeft2) {
                    this.mc.clickMouse();
                    this.tempCheckClickMouseLeft2 = true;
                }
            } else if (this.tempCheckClickMouseLeft2) {
                this.tempCheckClickMouseLeft2 = false;
            }
            Dualsense.mc.gameSettings.keyBindUseItem.pressed = Dualsense.L2_PRESSED;
            if (Dualsense.L2_PRESSED) {
                if (!this.tempCheckClickMouseRight2) {
                    this.mc.rightClickMouse();
                    this.tempCheckClickMouseRight2 = true;
                }
            } else if (this.tempCheckClickMouseRight2) {
                this.tempCheckClickMouseRight2 = false;
            }
        }
        if (this.canWalkInput()) {
            this.mc.gameSettings.keyBindJump.pressed = Dualsense.SQUARE_PRESSED || (this.mc.currentScreen == null || InvWalk.get.isActived()) && Keyboard.isKeyDown((int)this.mc.gameSettings.keyBindJump.getKeyCode());
            this.mc.gameSettings.keyBindSneak.pressed = Dualsense.CIRCLE_PRESSED || (this.mc.currentScreen == null || InvWalk.get.isActived()) && Keyboard.isKeyDown((int)this.mc.gameSettings.keyBindSneak.getKeyCode());
            this.tempCheckJumpOrSneak = true;
        } else if (this.tempCheckJumpOrSneak) {
            this.mc.gameSettings.keyBindJump.pressed = false;
            this.mc.gameSettings.keyBindSneak.pressed = false;
            this.tempCheckJumpOrSneak = false;
        }
    }

    @EventTarget
    public void onUpdate(EventRender2D event) {
        if (!Dualsense.CONTROLLER_LOADED) {
            return;
        }
        if (this.canMoveMouseCursor()) {
            int mouseX = Mouse.getEventX();
            int mouseY = Mouse.getEventY();
            float[] inputValues = Dualsense.SenseAxis.RIGHT_AXIS.getInputValues();
            if (Dualsense.SenseAxis.RIGHT_AXIS.hasAxisMove(inputValues)) {
                Mouse.setCursorPosition((int)((int)((double)mouseX + (double)(inputValues[0] > 0.0f ? 1.0f : -1.0f) * MathUtils.easeInCircle(Math.abs(inputValues[0])) * 30.0)), (int)((int)((double)mouseY - (double)(inputValues[1] > 0.0f ? 1.0f : -1.0f) * MathUtils.easeInCircle(Math.abs(inputValues[1])) * 30.0)));
            }
        }
        if (this.canClickMouseInput()) {
            if (Dualsense.R2_PRESSED) {
                if (!this.tempCheckClickMouseLeft) {
                    this.mc.clickMouse();
                    if (this.mc.currentScreen != null) {
                        try {
                            this.mc.currentScreen.mouseClicked(GuiScreen.staticMouseX, GuiScreen.staticMouseY, 0);
                        }
                        catch (Exception e) {
                            e.fillInStackTrace();
                        }
                    }
                    this.tempCheckClickMouseLeft = true;
                }
            } else if (!Dualsense.R2_PRESSED) {
                this.tempCheckClickMouseLeft = false;
            }
            if (Dualsense.L2_PRESSED) {
                if (!this.tempCheckClickMouseRight) {
                    this.mc.rightClickMouse();
                    if (this.mc.currentScreen != null) {
                        try {
                            this.mc.currentScreen.mouseClicked(GuiScreen.staticMouseX, GuiScreen.staticMouseY, 1);
                        }
                        catch (Exception e) {
                            e.fillInStackTrace();
                        }
                    }
                    this.tempCheckClickMouseRight = true;
                }
            } else if (!Dualsense.L2_PRESSED) {
                this.tempCheckClickMouseRight = false;
            }
        }
        if (this.canBindContainerInput()) {
            if (Dualsense.TRIANGLE_PRESSED) {
                this.mc.gameSettings.keyBindInventory.pressed = true;
                if (!this.tempCheckClickE) {
                    if (this.mc.currentScreen == null) {
                        this.mc.displayGuiScreen(new GuiInventory(Minecraft.player));
                    } else if (this.mc.currentScreen instanceof GuiContainer) {
                        this.mc.displayGuiScreen(null);
                    }
                    this.tempCheckClickE = true;
                }
            } else if (this.tempCheckClickE) {
                this.mc.gameSettings.keyBindInventory.pressed = false;
                this.tempCheckClickE = false;
            }
        } else if (this.tempCheckClickE) {
            this.tempCheckClickE = false;
        }
        if (this.canEscapeInput()) {
            if (Dualsense.OPTIONS_PRESSED || Dualsense.CIRCLE_PRESSED) {
                if (!this.tempCheckClickESC) {
                    if (this.mc.currentScreen != null) {
                        if (this.mc.currentScreen instanceof ClickGuiScreen) {
                            ClickGui.instance.toggle(false);
                        } else {
                            GuiScreen guiScreen = this.mc.currentScreen;
                            if (guiScreen instanceof GuiConfig) {
                                GuiConfig guiConfig = (GuiConfig)guiScreen;
                                guiConfig.colose = true;
                                GuiConfig.cfgScale.to = 1.0f;
                            } else {
                                this.mc.displayGuiScreen(null);
                            }
                        }
                    } else if (this.mc.currentScreen == null) {
                        if (!Dualsense.CIRCLE_PRESSED) {
                            this.mc.displayGuiScreen(new GuiIngameMenu());
                        }
                    } else {
                        this.mc.displayGuiScreen(null);
                    }
                }
                this.tempCheckClickESC = true;
            } else if (this.tempCheckClickESC) {
                this.tempCheckClickESC = false;
            }
        } else if (this.tempCheckClickESC) {
            this.tempCheckClickESC = false;
        }
        if (Dualsense.PS_PRESSED) {
            if (!this.tempCheckPS) {
                if (this.mc.currentScreen == null || this.mc.currentScreen instanceof ClickGuiScreen) {
                    if (!ClickGui.instance.isActived() && this.mc.currentScreen == null) {
                        ClickGui.instance.toggle(true);
                        Client.clickGuiScreen.initGui();
                        this.mc.displayGuiScreen(Client.clickGuiScreen);
                    } else if (Dualsense.mc.currentScreen instanceof ClickGuiScreen) {
                        ClickGui.instance.toggle();
                    }
                }
                this.tempCheckPS = true;
            }
        } else if (this.tempCheckPS) {
            this.tempCheckPS = false;
        }
    }

    private boolean hasCurrentDisplay() {
        try {
            return Display.isCurrent() && Display.isVisible() && Display.isActive();
        }
        catch (Exception e) {
            return false;
        }
    }

    private boolean canWalkInput() {
        return (this.mc.currentScreen == null || InvWalk.get.isActived()) && this.hasCurrentDisplay();
    }

    private boolean canInteractInput() {
        return this.mc.currentScreen == null && this.hasCurrentDisplay();
    }

    private boolean canRotateCamera() {
        return this.mc.currentScreen == null && Minecraft.player != null && this.hasCurrentDisplay();
    }

    private boolean canMoveMouseCursor() {
        return this.mc.currentScreen != null && this.hasCurrentDisplay();
    }

    private boolean canClickMouseInput() {
        return this.mc.currentScreen == null && this.hasCurrentDisplay();
    }

    private boolean canBindContainerInput() {
        return (this.mc.currentScreen == null || this.mc.currentScreen instanceof GuiContainer) && this.hasCurrentDisplay() && Minecraft.player != null;
    }

    private boolean canEscapeInput() {
        return this.hasCurrentDisplay();
    }
}

