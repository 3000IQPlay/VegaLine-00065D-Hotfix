package ru.govno.client.utils.URender.hide;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.function.Supplier;
import ru.govno.client.utils.URender.UBegin;
import ru.govno.client.utils.URender.URMath;
import ru.govno.client.utils.URender.UVertex;
import ru.govno.client.utils.URender.hide.ULayer;

public class URect {
    private final Supplier<ULayer> layer;
    private static final List<UVertex> temp = Lists.newArrayList();
    private int[] colors;
    private float shadowOut;
    private float shadowInPC;
    private float round;
    private static final URMath math = new URMath();

    private URect(Supplier<ULayer> layer) {
        this.layer = layer;
    }

    public static URect addTo(ULayer layer) {
        return new URect(() -> layer);
    }

    public URect setColor(int ... colors) {
        this.colors = colors;
        return this;
    }

    public URect fill(float x, float y, float x2, float y2, int ... colors) {
        this.colors = colors;
        switch (colors.length) {
            case 0: {
                temp.add(UVertex.vertex(x, y, -1));
                temp.add(UVertex.vertex(x2, y, -1));
                temp.add(UVertex.vertex(x2, y2, -1));
                temp.add(UVertex.vertex(x, y2, -1));
                break;
            }
            case 1: {
                temp.add(UVertex.vertex(x, y, colors[0]));
                temp.add(UVertex.vertex(x2, y, colors[0]));
                temp.add(UVertex.vertex(x2, y2, colors[0]));
                temp.add(UVertex.vertex(x, y2, colors[0]));
                break;
            }
            case 2: {
                temp.add(UVertex.vertex(x, y, colors[0]));
                temp.add(UVertex.vertex(x2, y, colors[1]));
                temp.add(UVertex.vertex(x2, y2, colors[1]));
                temp.add(UVertex.vertex(x, y2, colors[0]));
                break;
            }
            case 3: {
                temp.add(UVertex.vertex(x, y, colors[0]));
                temp.add(UVertex.vertex(x2, y, colors[1]));
                temp.add(UVertex.vertex(x2, y2, colors[2]));
                temp.add(UVertex.vertex(x, y2, colors[2]));
                break;
            }
            case 4: {
                temp.add(UVertex.vertex(x, y, colors[0]));
                temp.add(UVertex.vertex(x2, y, colors[1]));
                temp.add(UVertex.vertex(x2, y2, colors[2]));
                temp.add(UVertex.vertex(x, y2, colors[3]));
            }
        }
        this.layer.get().addBegin(UBegin.begin(7, temp));
        temp.clear();
        return this;
    }
}

