package ru.govno.client.utils.URender.hide;

import com.google.common.collect.Lists;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.Tessellator;
import ru.govno.client.utils.URender.UBegin;
import ru.govno.client.utils.URender.URenderSettings;

public class ULayer {
    private final List<UBegin> begins = Lists.newArrayList();

    private ULayer() {
    }

    public static ULayer buildNew() {
        return new ULayer();
    }

    public void addBegin(UBegin begin) {
        this.begins.add(begin);
    }

    public void drawBegins(@Nullable Tessellator tessellator) {
        if (URenderSettings.getVBOState()) {
            this.begins.forEach(begin -> begin.doMcVBO(tessellator));
        } else {
            this.begins.forEach(UBegin::doUGL);
        }
        this.begins.clear();
    }
}

