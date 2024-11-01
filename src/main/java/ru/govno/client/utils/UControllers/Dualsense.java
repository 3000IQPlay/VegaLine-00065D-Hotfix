package ru.govno.client.utils.UControllers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.Minecraft;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Controller;
import org.lwjgl.input.Controllers;

public class Dualsense {
    public static boolean CONTROLLER_LOADED = false;
    private static final List<Controller> gamepads = new ArrayList<Controller>();
    public static boolean SQUARE_PRESSED;
    public static boolean CROSS_PRESSED;
    public static boolean CIRCLE_PRESSED;
    public static boolean TRIANGLE_PRESSED;
    public static boolean L1_PRESSED;
    public static boolean R1_PRESSED;
    public static boolean L2_PRESSED;
    public static boolean R2_PRESSED;
    public static boolean SHARE_PRESSED;
    public static boolean OPTIONS_PRESSED;
    public static boolean PS_PRESSED;
    public static boolean TOUCHPAD_PRESSED;
    public static final Minecraft mc;

    public static void findAndUpdateDualsense() {
        try {
            Controllers.create();
        }
        catch (LWJGLException e) {
            e.fillInStackTrace();
        }
        gamepads.clear();
        for (int i = 0; i < Controllers.getControllerCount(); ++i) {
            Controller controller = Controllers.getController((int)i);
            if (!controller.getName().toLowerCase().contains("dualsense")) continue;
            controller.poll();
            gamepads.add(controller);
        }
        CONTROLLER_LOADED = !gamepads.isEmpty();
        boolean anyGamepad = CONTROLLER_LOADED;
        Dualsense.updateButtonsState(anyGamepad);
        Dualsense.testFeature();
    }

    private static void updateButtonsState(boolean gamepadsLoaded) {
        if (gamepadsLoaded) {
            for (Controller controller : gamepads) {
                block29: for (int indexButton = 0; indexButton < controller.getButtonCount(); ++indexButton) {
                    SenseButton button = SenseButton.getSenseButtonAsIndex(indexButton);
                    if (button == null) continue;
                    switch (button.getName()) {
                        case "Square": {
                            SQUARE_PRESSED = button.isPressed();
                            continue block29;
                        }
                        case "Cross": {
                            CROSS_PRESSED = button.isPressed();
                            continue block29;
                        }
                        case "Circle": {
                            CIRCLE_PRESSED = button.isPressed();
                            continue block29;
                        }
                        case "Triangle": {
                            TRIANGLE_PRESSED = button.isPressed();
                            continue block29;
                        }
                        case "L1": {
                            L1_PRESSED = button.isPressed();
                            continue block29;
                        }
                        case "R1": {
                            R1_PRESSED = button.isPressed();
                            continue block29;
                        }
                        case "L2": {
                            L2_PRESSED = button.isPressed();
                            continue block29;
                        }
                        case "R2": {
                            R2_PRESSED = button.isPressed();
                            continue block29;
                        }
                        case "Share": {
                            SHARE_PRESSED = button.isPressed();
                            continue block29;
                        }
                        case "Options": {
                            OPTIONS_PRESSED = button.isPressed();
                            continue block29;
                        }
                        case "PS": {
                            PS_PRESSED = button.isPressed();
                            continue block29;
                        }
                        case "Touchpad": {
                            TOUCHPAD_PRESSED = button.isPressed();
                        }
                    }
                }
            }
        } else {
            SQUARE_PRESSED = false;
            CROSS_PRESSED = false;
            CIRCLE_PRESSED = false;
            TRIANGLE_PRESSED = false;
            L1_PRESSED = false;
            R1_PRESSED = false;
            L2_PRESSED = false;
            R2_PRESSED = false;
            SHARE_PRESSED = false;
            OPTIONS_PRESSED = false;
            PS_PRESSED = false;
            TOUCHPAD_PRESSED = false;
        }
    }

    public static boolean[] getWasdInputFromSenseAxis(boolean gamepadsLoaded, int maxTickInterval, long updateMS) {
        boolean newInput;
        int tickedMin;
        if (!gamepadsLoaded) {
            return new boolean[4];
        }
        boolean[] wasd = new boolean[4];
        float[] inputValues = SenseAxis.LEFT_AXIS.getInputValues();
        float xInput = inputValues[0];
        float yInput = inputValues[1];
        long ticksRealTime = System.currentTimeMillis() / updateMS;
        if (xInput != 0.0f) {
            tickedMin = (int)(Math.abs(xInput) * (float)(maxTickInterval - 1));
            boolean bl = newInput = ticksRealTime % (long)maxTickInterval <= (long)(tickedMin + 1);
            if (newInput) {
                if (xInput > 0.0f) {
                    wasd[0] = true;
                } else {
                    wasd[2] = true;
                }
            }
        }
        if (yInput != 0.0f) {
            tickedMin = (int)(Math.abs(yInput) * (float)(maxTickInterval - 1));
            boolean bl = newInput = ticksRealTime % (long)maxTickInterval <= (long)(tickedMin + 1);
            if (newInput) {
                if (yInput > 0.0f) {
                    wasd[1] = true;
                } else {
                    wasd[3] = true;
                }
            }
        }
        return wasd;
    }

    public static void testFeature() {
    }

    static {
        mc = Minecraft.getMinecraft();
    }

    public static enum SenseButton {
        SQUARE(0, "Square"),
        CROSS(1, "Cross"),
        CIRCLE(2, "Circle"),
        TRIANGLE(3, "Triangle"),
        L1(4, "L1"),
        R1(5, "R1"),
        L2(6, "L1"),
        R2(7, "R2"),
        SHARE(8, "Share"),
        OPTIONS(9, "Options"),
        PS(12, "PS"),
        TOUCHPAD(13, "Touchpad");

        int id;
        String name;

        public int getId() {
            return this.id;
        }

        public String getName() {
            return this.name;
        }

        private SenseButton(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public boolean isPressed() {
            return !gamepads.isEmpty() && gamepads.stream().anyMatch(gamepad -> gamepad.isButtonPressed(this.id));
        }

        public static SenseButton getSenseButtonAsIndex(int index) {
            return Arrays.stream(SenseButton.values()).filter(s -> s.getId() == index).findFirst().orElse(null);
        }
    }

    public static enum SenseAxis {
        LEFT_AXIS(false, "Left axis"),
        RIGHT_AXIS(true, "Right axis");

        boolean isRight;
        String name;

        public float[] getInputValues() {
            if (gamepads.isEmpty()) {
                return new float[4];
            }
            for (Controller controller : gamepads) {
                if (controller.getAxisCount() < 2) continue;
                if (this.isRight) {
                    return new float[]{controller.getZAxisValue(), controller.getRZAxisValue(), controller.getXAxisDeadZone(), controller.getYAxisDeadZone()};
                }
                return new float[]{-controller.getYAxisValue(), -controller.getXAxisValue(), controller.getXAxisDeadZone(), controller.getYAxisDeadZone()};
            }
            return new float[4];
        }

        public boolean hasAxisMove(float[] axisValues) {
            return Math.sqrt(axisValues[0] * axisValues[0] + axisValues[1] * axisValues[1]) != 0.0;
        }

        public double getAxisMove(float[] axisValues) {
            return Math.sqrt(axisValues[0] * axisValues[0] + axisValues[1] * axisValues[1]);
        }

        public double getsAxisDir(float[] axisValues, float append) {
            return Math.atan2(axisValues[1], axisValues[0]) * 180.0 / Math.PI - 90.0 + (double)append;
        }

        private SenseAxis(boolean isRight, String name) {
            this.isRight = isRight;
            this.name = name;
        }
    }
}

