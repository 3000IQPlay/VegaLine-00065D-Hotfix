package ru.govno.client.utils.URender;

public class URMath {
    public static final float[] sin = new float[360];
    public static final float[] cos = new float[360];
    private static final int[] LEFT_UP = new int[]{270, 360};
    private static final int[] RIGHT_UP = new int[]{0, 90};
    private static final int[] RIGHT_DOWN = new int[]{180, 270};
    private static final int[] LEFT_DOWN = new int[]{90, 180};
    public static final int[][] sides = new int[][]{LEFT_UP, RIGHT_UP, LEFT_DOWN, RIGHT_DOWN};

    public URMath() {
        for (int rad = 0; rad < 360; ++rad) {
            double math = Math.toRadians(rad);
            URMath.sin[rad] = (float)Math.sin(math);
            URMath.cos[rad] = (float)Math.cos(math);
        }
    }
}

