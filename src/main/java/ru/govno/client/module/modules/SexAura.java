package ru.govno.client.module.modules;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;
import ru.govno.client.Client;
import ru.govno.client.friendsystem.Friend;
import ru.govno.client.module.Module;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.utils.Combat.RotationUtil;
import ru.govno.client.utils.Math.MathUtils;
import ru.govno.client.utils.Movement.MoveMeHelp;

public class SexAura extends Module {
    public static Module get;
    BoolSettings Legit;
    boolean processMove = false;
    boolean sex;

    public SexAura() {
        super("SexAura", 0, Module.Category.COMBAT);
        this.settings.add(this.Legit = new BoolSettings("Legit", false, this));
    }

    private static EntityPlayer getMe() {
        return Minecraft.player;
    }

    private static boolean entityIsCurrentToFilter(EntityLivingBase entity, double range) {
        for (Friend friend : Client.friendManager.getFriends()) {
            if (entity != null
                    && (entity.getName().equalsIgnoreCase(friend.getName()) || FreeCam.fakePlayer != null && entity.getEntityId() == FreeCam.fakePlayer.getEntityId())) {
                return false;
            }
        }

        return entity != null
                && entity.getHealth() != 0.0F
                && !(entity instanceof EntityPlayerSP)
                && !(entity instanceof EntityArmorStand)
                && getMe().canEntityBeSeen(entity)
                && (!(entity instanceof EntityPlayer) || !((EntityPlayer)entity).isCreative())
                && getMe().getSmartDistanceToAABB(RotationUtil.getLookRots(getMe(), entity), entity) <= range;
    }

    public static final EntityLivingBase getCurrentTarget(float range) {
        EntityLivingBase base = null;

        for (Object o : mc.world.getLoadedEntityList()) {
            EntityLivingBase living;
            if (o instanceof EntityLivingBase
                    && entityIsCurrentToFilter(living = (EntityLivingBase)o, (double)range)
                    && living.getHealth() != 0.0F
                    && getMe().getDistanceToEntity(living) <= range) {
                range = getMe().getDistanceToEntity(living);
                base = living;
            }
        }

        return base;
    }

    double getSpeedToFollowCoord(Vec3d pos) {
        double speed = MathUtils.clamp(MoveMeHelp.getSpeed() * 1.1, 0.0, 0.2499) - (Minecraft.player.ticksExisted % 2 == 0 ? 0.003 : 0.0);
        return MathUtils.clamp(speed, 0.0, 1.0 - Minecraft.player.getDistanceXZ(pos.xCoord, pos.zCoord));
    }

    Vec3d getVecButtomAtTarget(EntityLivingBase target, float setedRange) {
        return target.getPositionVector()
                .addVector(
                        Math.sin(Math.toRadians((double)target.renderYawOffset)) * (double)setedRange,
                        0.0,
                        -Math.cos(Math.toRadians((double)target.renderYawOffset)) * (double)setedRange
                );
    }

    boolean movedToProcess(Vec3d vec, float checkR, EntityLivingBase target) {
        boolean moved = Minecraft.player.getDistanceXZ(vec.xCoord, vec.zCoord) <= (double)checkR
                && MathUtils.getDifferenceOf(Minecraft.player.posY, vec.yCoord) < (double)(this.Legit.getBool() ? 0.6F : 1.0F)
                && (!this.Legit.getBool() || !Minecraft.player.isJumping());
        if (!this.Legit.getBool()) {
            float rot = RotationUtil.getMatrixRots(target)[0];
            if (!moved) {
                if (MathUtils.getDifferenceOf(rot, Minecraft.player.rotationYaw) > 6.0) {
                    Minecraft.player.rotationYaw = rot;
                }

                mc.gameSettings.keyBindForward.pressed = true;
                mc.gameSettings.keyBindLeft.pressed = false;
                mc.gameSettings.keyBindRight.pressed = false;
                mc.gameSettings.keyBindBack.pressed = false;
                double speed = this.getSpeedToFollowCoord(vec);
                Vec3d addSelfYaw = Minecraft.player
                        .getPositionVector()
                        .addVector(
                                -Math.sin(Math.toRadians((double)Minecraft.player.rotationYaw)) * 0.8,
                                0.0,
                                Math.cos(Math.toRadians((double)Minecraft.player.rotationYaw)) * 0.8
                        );
                if (Minecraft.player.onGround && Minecraft.player.getDistanceXZ(vec.xCoord, vec.zCoord) > 4.0
                        || Speed.posBlock(addSelfYaw.xCoord, addSelfYaw.yCoord + 0.3, addSelfYaw.zCoord)
                        && !Speed.posBlock(addSelfYaw.xCoord, addSelfYaw.yCoord + 1.3, addSelfYaw.zCoord)) {
                    mc.gameSettings.keyBindJump.pressed = Minecraft.player.onGround && Minecraft.player.getDistanceXZ(vec.xCoord, vec.zCoord) > 1.7;
                } else {
                    mc.gameSettings.keyBindJump.pressed = false;
                }

                this.processMove = true;
            } else if (this.processMove) {
                if (!Minecraft.player.isJumping()) {
                    mc.gameSettings.keyBindJump.pressed = false;
                }

                mc.gameSettings.keyBindForward.pressed = false;
                if (Minecraft.player.getDistanceToVec3d(vec) < 0.2) {
                    Minecraft.player.setPosition(vec.xCoord, Minecraft.player.posY, vec.zCoord);
                    Minecraft.player.multiplyMotionXZ(0.0F);
                }

                this.processMove = false;
            }

            if (MathUtils.getDifferenceOf(rot, Minecraft.player.rotationYaw) > 6.0) {
                Minecraft.player.rotationYaw = rot;
            }
        }

        return moved;
    }

    void doSex(boolean DO) {
        if (DO) {
            mc.gameSettings.keyBindSneak.pressed = Minecraft.player.ticksExisted % 2 == 0;
            if (!this.sex) {
                this.sex = true;
            }
        } else if (this.sex) {
            mc.gameSettings.keyBindSneak.pressed = false;
            this.sex = false;
        }
    }

    @Override
    public void onUpdate() {
        if (!FreeCam.get.isActived()) {
            EntityLivingBase target = getCurrentTarget(25.0F);
            if (target != null) {
                this.doSex(this.movedToProcess(this.getVecButtomAtTarget(target, 0.3F), MathUtils.clamp(target.width / 2.0F, 0.1F, 2.0F), target));
            }
        }
    }

    @Override
    public void onToggled(boolean actived) {
        this.doSex(false);
        super.onToggled(this.processMove);
    }
}
