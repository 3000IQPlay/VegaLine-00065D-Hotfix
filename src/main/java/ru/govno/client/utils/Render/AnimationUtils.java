package ru.govno.client.utils.Render;

import ru.govno.client.utils.Math.MathUtils;

public class AnimationUtils {
    long mc;
    public float anim;
    public float to;
    public float speed;

    public AnimationUtils(float anim, float to, float speed) {
        this.anim = anim;
        this.to = to;
        this.speed = speed;
        this.mc = System.currentTimeMillis();
    }

    public float getAnim() {
        int count;
        if ((double)Math.abs(this.anim - this.to) < 1.0E-4) {
            this.anim = this.to;
        }
        if ((count = (int)(Math.min((float)(System.currentTimeMillis() - this.mc), 400.0f) / 5.0f)) > 0) {
            this.mc = System.currentTimeMillis();
        }
        for (int i = 0; i < count; ++i) {
            this.anim = MathUtils.lerp(this.anim, this.to, this.speed);
        }
        return this.anim;
    }

    public float getAngleAnim() {
        if ((double)Math.abs(this.anim - this.to) > 1.0E-4) {
            int count = (int)(Math.min((float)(System.currentTimeMillis() - this.mc), 400.0f) / 5.0f);
            if (count > 0) {
                this.mc = System.currentTimeMillis();
            }
            for (int i = 0; i < count; ++i) {
                this.anim = (float)this.lerpAngle(this.anim, this.to, this.speed);
            }
        }
        return MathUtils.wrapAngleTo180_float(this.anim);
    }

    public void setAnim(float anim) {
        this.anim = anim;
        this.mc = System.currentTimeMillis();
    }

    double lerpAngle(float start, float end, float amount) {
        float minAngle = (end - start + 180.0f) % 360.0f - 180.0f;
        return minAngle * amount + start;
    }
}

