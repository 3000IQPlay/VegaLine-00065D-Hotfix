package ru.govno.client.module.modules;

import java.util.Comparator;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import ru.govno.client.Client;
import ru.govno.client.event.EventTarget;
import ru.govno.client.event.events.EventMoveKeys;
import ru.govno.client.module.Module;
import ru.govno.client.module.modules.HitAura;
import ru.govno.client.utils.Combat.RotationUtil;
import ru.govno.client.utils.Math.MathUtils;

public class AutoPlay
extends Module {
    public static AutoPlay get;
    private EntityLivingBase target;
    private boolean botActive;
    private Vec3d walkVec;
    private boolean jumping;
    private Demanour currentDemanour;

    public AutoPlay() {
        super("AutoPlay", 0, Module.Category.COMBAT);
        get = this;
    }

    @Override
    public boolean isBetaModule() {
        return true;
    }

    @Override
    public void onUpdate() {
        this.walkVec = null;
        this.jumping = false;
        if (!this.botActive && Minecraft.player.isSneaking()) {
            this.botActive = true;
            Client.msg("\u00a7f\u00a7lModules:\u00a7r \u00a77[\u00a7lAutoPlay\u00a7r\u00a77]: \u0411\u043e\u0442 \u0430\u043a\u0442\u0438\u0432\u0438\u0440\u043e\u0432\u0430\u043d.", false);
        }
        if (this.botActive && (Minecraft.player == null || AutoPlay.mc.world == null)) {
            this.botActive = false;
            this.target = null;
            this.walkVec = null;
            this.currentDemanour = null;
            Client.msg("\u00a7f\u00a7lModules:\u00a7r \u00a77[\u00a7lAutoPlay\u00a7r\u00a77]: \u0414\u043b\u044f \u0440\u0430\u0431\u043e\u0442\u044b \u0431\u043e\u0442\u0430 \u0442\u0430\u043f\u043d\u0438 \u0448\u0438\u0444\u0442.", false);
            AutoPlay.mc.gameSettings.keyBindForward.pressed = Keyboard.isKeyDown((int)AutoPlay.mc.gameSettings.keyBindForward.getKeyCode());
            AutoPlay.mc.gameSettings.keyBindRight.pressed = Keyboard.isKeyDown((int)AutoPlay.mc.gameSettings.keyBindRight.getKeyCode());
            AutoPlay.mc.gameSettings.keyBindBack.pressed = Keyboard.isKeyDown((int)AutoPlay.mc.gameSettings.keyBindBack.getKeyCode());
            AutoPlay.mc.gameSettings.keyBindLeft.pressed = Keyboard.isKeyDown((int)AutoPlay.mc.gameSettings.keyBindLeft.getKeyCode());
            AutoPlay.mc.gameSettings.keyBindJump.pressed = Keyboard.isKeyDown((int)AutoPlay.mc.gameSettings.keyBindJump.getKeyCode());
            AutoPlay.mc.gameSettings.keyBindUseItem.pressed = Mouse.isButtonDown((int)1) && AutoPlay.mc.currentScreen == null;
            return;
        }
        if (!this.botActive) {
            return;
        }
        this.updateDemanour();
        this.target = this.getNearestTarget();
        if (this.target == null) {
            return;
        }
        float distance = Minecraft.player.getDistanceToEntity(this.target);
        if (distance < 12.0f && distance > 8.0f) {
            if (!HitAura.get.isActived()) {
                HitAura.get.toggle();
            }
        } else if (distance > 15.0f && HitAura.get.isActived()) {
            HitAura.get.toggle();
        }
        if (distance > 1.3f) {
            this.walkVec = this.target.getPositionVector();
        }
        float predictValueMove = 2.0f;
        Vec3d predPos = Minecraft.player.getPositionVector().addVector((Minecraft.player.posX - Minecraft.player.lastTickPosX) * (double)predictValueMove, 0.0, (Minecraft.player.posZ - Minecraft.player.lastTickPosZ) * (double)predictValueMove);
        AxisAlignedBB aabbMovePre = new AxisAlignedBB(predPos.addVector((double)(-Minecraft.player.width) / 2.0, 0.0, (double)(-Minecraft.player.width) / 2.0), predPos.addVector((double)Minecraft.player.width / 2.0, 1.0, (double)Minecraft.player.width / 2.0));
        this.jumping = distance < 9.0f || !AutoPlay.mc.world.getCollisionBoxes(Minecraft.player, aabbMovePre).isEmpty();
        boolean bl = AutoPlay.mc.gameSettings.keyBindJump.pressed = this.jumping || Keyboard.isKeyDown((int)AutoPlay.mc.gameSettings.keyBindJump.getKeyCode());
        if (this.walkVec != null && (HitAura.TARGET_ROTS != null || Minecraft.player.ticksExisted % 9 == 2)) {
            boolean[] wasd = this.walkToCoordKeys(this.walkVec);
            AutoPlay.mc.gameSettings.keyBindForward.pressed = wasd[0] || Keyboard.isKeyDown((int)AutoPlay.mc.gameSettings.keyBindForward.getKeyCode());
            AutoPlay.mc.gameSettings.keyBindRight.pressed = wasd[1] || Keyboard.isKeyDown((int)AutoPlay.mc.gameSettings.keyBindRight.getKeyCode());
            AutoPlay.mc.gameSettings.keyBindBack.pressed = wasd[2] || Keyboard.isKeyDown((int)AutoPlay.mc.gameSettings.keyBindBack.getKeyCode());
            boolean bl2 = AutoPlay.mc.gameSettings.keyBindLeft.pressed = wasd[3] || Keyboard.isKeyDown((int)AutoPlay.mc.gameSettings.keyBindLeft.getKeyCode());
        }
        if (HitAura.TARGET_ROTS != null && Minecraft.player.getHeldItemOffhand().getItem() == Items.SHIELD) {
            AutoPlay.mc.gameSettings.keyBindUseItem.pressed = true;
        }
    }

    @EventTarget
    public void onMoveKeys(EventMoveKeys eventMoveKeys) {
        if (this.actived) {
            // empty if block
        }
    }

    private boolean[] walkToCoordKeys(Vec3d coord) {
        if (coord == null) {
            return new boolean[4];
        }
        float statYawToCoord = RotationUtil.getNeededFacing(coord, false, Minecraft.player, false)[0];
        float keysYaw = MathUtils.wrapAngleTo180_float(statYawToCoord - Minecraft.player.rotationYaw);
        boolean[] WASD = new boolean[4];
        if (RotationUtil.getAngleDifference(0.0f, keysYaw) <= 60.0f) {
            WASD[0] = true;
        }
        if (RotationUtil.getAngleDifference(90.0f, keysYaw) <= 60.0f) {
            WASD[1] = true;
        }
        if (RotationUtil.getAngleDifference(180.0f, keysYaw) <= 60.0f) {
            WASD[2] = true;
        }
        if (RotationUtil.getAngleDifference(270.0f, keysYaw) <= 60.0f) {
            WASD[3] = true;
        }
        return WASD;
    }

    private EntityLivingBase getNearestTarget() {
        if (HitAura.TARGET_ROTS != null) {
            return HitAura.TARGET_ROTS;
        }
        if (AutoPlay.mc.world != null) {
            return AutoPlay.mc.world.getLoadedEntityList().stream().map(Entity::getOtherPlayerOf).filter(Objects::nonNull).filter(player -> player.isEntityAlive() && !Client.friendManager.isFriend(player.getName()) && !Client.summit(player)).sorted(Comparator.comparing(obj -> Float.valueOf(-obj.getDistanceToEntity(Minecraft.player)))).findAny().orElse(null);
        }
        return null;
    }

    private void updateDemanour() {
        this.currentDemanour = Demanour.ATTACK;
        if (this.target != null && this.target.getHealth() + 4.0f < Minecraft.player.getHealth()) {
            this.currentDemanour = Demanour.TROLL_ATTACK;
        }
        if (Minecraft.player.getHealth() < 7.0f) {
            this.currentDemanour = Demanour.RUN;
        }
    }

    @Override
    public void onToggled(boolean actived) {
        this.target = null;
        this.botActive = false;
        this.walkVec = null;
        this.jumping = false;
        super.onToggled(actived);
    }

    private static enum Demanour {
        ATTACK,
        RUN,
        TROLL_ATTACK;

    }
}

