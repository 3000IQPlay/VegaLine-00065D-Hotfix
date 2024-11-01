package ru.govno.client.module.modules;

import dev.intave.NewPhisicsFixes;
import dev.intave.viamcp.fixes.AttackOrder;
import javax.vecmath.Vector2f;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.potion.Potion;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.RandomUtils;
import org.lwjgl.input.Mouse;
import ru.govno.client.Client;
import ru.govno.client.event.events.EventPlayerMotionUpdate;
import ru.govno.client.module.Module;
import ru.govno.client.module.modules.AirJump;
import ru.govno.client.module.modules.Criticals;
import ru.govno.client.module.modules.ElytraBoost;
import ru.govno.client.module.modules.Fly;
import ru.govno.client.module.modules.FreeCam;
import ru.govno.client.module.modules.HitAura;
import ru.govno.client.module.modules.JesusSpeed;
import ru.govno.client.module.modules.MoveHelper;
import ru.govno.client.module.modules.PushAttack;
import ru.govno.client.module.modules.Speed;
import ru.govno.client.module.modules.TargetStrafe;
import ru.govno.client.module.modules.WaterSpeed;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.module.settings.FloatSettings;
import ru.govno.client.module.settings.ModeSettings;
import ru.govno.client.utils.Combat.RotationUtil;
import ru.govno.client.utils.Math.MathUtils;
import ru.govno.client.utils.Math.TimerHelper;
import ru.govno.client.utils.Movement.MoveMeHelp;
import ru.govno.client.utils.Wrapper;

public class AimAssist
extends Module {
    static EntityLivingBase target;
    BoolSettings OnlyAttackKey;
    BoolSettings AimBot;
    BoolSettings AutoAttack;
    BoolSettings SmartCrits;
    BoolSettings StopSprint;
    BoolSettings Players;
    BoolSettings Invis;
    BoolSettings Walls;
    BoolSettings Mobs;
    FloatSettings Range;
    FloatSettings Fov;
    FloatSettings Speed;
    ModeSettings HitsMode;
    TimerHelper timerHelper = new TimerHelper();

    public AimAssist() {
        super("AimAssist", 0, Module.Category.COMBAT);
        this.OnlyAttackKey = new BoolSettings("OnlyAttackKey", true, this, () -> this.AutoAttack.getBool() || this.AimBot.getBool());
        this.settings.add(this.OnlyAttackKey);
        this.AimBot = new BoolSettings("AimBot", true, this);
        this.settings.add(this.AimBot);
        this.Range = new FloatSettings("Range", 4.0f, 6.0f, 1.0f, this, () -> this.AutoAttack.getBool() || this.AimBot.getBool());
        this.settings.add(this.Range);
        this.Fov = new FloatSettings("Fov", 90.0f, 360.0f, 40.0f, this, () -> this.AimBot.getBool());
        this.settings.add(this.Fov);
        this.Speed = new FloatSettings("Speed", 3.0f, 7.0f, 0.5f, this, () -> this.AimBot.getBool());
        this.settings.add(this.Speed);
        this.AutoAttack = new BoolSettings("AutoAttack", true, this);
        this.settings.add(this.AutoAttack);
        this.HitsMode = new ModeSettings("HitsMode", "Click", this, new String[]{"Click", "Attack"}, () -> this.AutoAttack.getBool());
        this.settings.add(this.HitsMode);
        this.SmartCrits = new BoolSettings("SmartCrits", true, this, () -> this.AutoAttack.getBool());
        this.settings.add(this.SmartCrits);
        this.StopSprint = new BoolSettings("StopSprint", true, this, () -> this.AutoAttack.getBool());
        this.settings.add(this.StopSprint);
        this.Players = new BoolSettings("Players", true, this, () -> this.AutoAttack.getBool() || this.AimBot.getBool());
        this.settings.add(this.Players);
        this.Invis = new BoolSettings("Invis", false, this, () -> this.AutoAttack.getBool() || this.AimBot.getBool());
        this.settings.add(this.Invis);
        this.Walls = new BoolSettings("Walls", false, this, () -> this.AutoAttack.getBool() || this.AimBot.getBool());
        this.settings.add(this.Walls);
        this.Mobs = new BoolSettings("Mobs", false, this, () -> this.AutoAttack.getBool() || this.AimBot.getBool());
        this.settings.add(this.Mobs);
    }

    void hit(EntityLivingBase entityIn, boolean mouse, boolean stopSprint) {
        boolean saveCrit;
        boolean sprint = Minecraft.player.serverSprintState;
        boolean bl = saveCrit = this.canCrits() && sprint && stopSprint;
        if (saveCrit) {
            Minecraft.player.connection.sendPacket(new CPacketEntityAction(Minecraft.player, CPacketEntityAction.Action.STOP_SPRINTING));
        }
        if (mouse) {
            mc.clickMouse();
        } else {
            AttackOrder.sendFixedAttack((EntityPlayer)Minecraft.player, (Entity)target, (EnumHand)EnumHand.MAIN_HAND);
        }
        if (saveCrit) {
            Minecraft.player.serverSprintState = sprint;
        }
    }

    public boolean lowHand() {
        Item item = Minecraft.player.getHeldItemMainhand().getItem();
        return !(item instanceof ItemSword) && !(item instanceof ItemTool);
    }

    public float msCooldown() {
        float handCooled = 5.5f;
        Item item = Minecraft.player.getHeldItemMainhand().getItem();
        if (NewPhisicsFixes.isOldVersion()) {
            handCooled = 0.1f;
        } else if (this.lowHand()) {
            handCooled = 5.0f;
        } else if (item instanceof ItemSword) {
            handCooled = Wrapper.getPlayer().isPotionActive(Potion.getPotionById(3)) ? 4.5f : 5.1f;
        } else if (item instanceof ItemAxe) {
            handCooled = Wrapper.getPlayer().isPotionActive(Potion.getPotionById(3)) ? 9.0f : 11.0f;
        } else if (item instanceof ItemPickaxe) {
            handCooled = Wrapper.getPlayer().isPotionActive(Potion.getPotionById(3)) ? 5.5f : 7.0f;
        } else if (item == Items.DIAMOND_SHOVEL || item == Items.IRON_SHOVEL || item == Items.GOLDEN_SHOVEL || item == Items.STONE_SHOVEL || item == Items.WOODEN_SHOVEL) {
            handCooled = Wrapper.getPlayer().isPotionActive(Potion.getPotionById(3)) ? 7.0f : 9.0f;
        } else if (item == Items.DIAMOND_HOE || item == Items.IRON_HOE || item == Items.STONE_HOE) {
            handCooled = 4.5f;
        } else if (item == Items.GOLDEN_HOE || item == Items.WOODEN_HOE) {
            handCooled = Wrapper.getPlayer().isPotionActive(Potion.getPotionById(3)) ? 7.0f : 8.5f;
        }
        handCooled *= 100.0f;
        return (int)(handCooled += 10.0f);
    }

    boolean hasCooled() {
        return this.timerHelper.hasReached(this.msCooldown()) && Minecraft.player.getCooledAttackStrength(0.0f) != 0.0f;
    }

    public boolean isCritical() {
        double x = Minecraft.player.posX;
        double y = Minecraft.player.posY;
        double z = Minecraft.player.posZ;
        boolean adobeHead = MoveMeHelp.isBlockAboveHead();
        if (!adobeHead ? (double)Minecraft.player.fallDistance <= 0.08 : Minecraft.player.isCollidedVertically && !Minecraft.player.onGround && adobeHead && (double)Minecraft.player.fallDistance < 0.01) {
            return false;
        }
        if (adobeHead && (Minecraft.player.fallDistance != 0.0f || !Minecraft.player.isJumping())) {
            return false;
        }
        if (adobeHead) {
            return Minecraft.player.fallDistance != 0.0f || AimAssist.mc.world.getBlockState(new BlockPos(x, y - 0.2001, z)).getBlock() != Blocks.AIR && (!AirJump.get.actived || AimAssist.mc.world.getBlockState(new BlockPos(x, y - 0.2001, z)).getBlock() == Blocks.AIR || !Minecraft.player.isJumping());
        }
        return true;
    }

    public boolean getBlockWithExpand(float expand, double x, double y, double z, Block block) {
        return AimAssist.mc.world.getBlockState(new BlockPos(x, y + 0.03, z)).getBlock() == block || AimAssist.mc.world.getBlockState(new BlockPos(x + (double)expand, y, z + (double)expand)).getBlock() == block || AimAssist.mc.world.getBlockState(new BlockPos(x - (double)expand, y, z - (double)expand)).getBlock() == block || AimAssist.mc.world.getBlockState(new BlockPos(x + (double)expand, y, z - (double)expand)).getBlock() == block || AimAssist.mc.world.getBlockState(new BlockPos(x - (double)expand, y, z + (double)expand)).getBlock() == block || AimAssist.mc.world.getBlockState(new BlockPos(x + (double)expand, y, z)).getBlock() == block || AimAssist.mc.world.getBlockState(new BlockPos(x - (double)expand, y, z)).getBlock() == block || AimAssist.mc.world.getBlockState(new BlockPos(x, y, z + (double)expand)).getBlock() == block || AimAssist.mc.world.getBlockState(new BlockPos(x, y, z - (double)expand)).getBlock() == block;
    }

    boolean confirmCritsInWater(double x, double y, double z) {
        boolean confirm = false;
        if (WaterSpeed.get.actived) {
            if (Entity.Getmotiony > 0.0 && AimAssist.mc.world.getBlockState(new BlockPos(x, y - 0.3, z)).getBlock() == Blocks.WATER && !(AimAssist.mc.world.getBlockState(new BlockPos(x, y - 1.4, z)) instanceof BlockLiquid) && AimAssist.mc.world.getBlockState(new BlockPos(x, y + 1.0, z)).getBlock() == Blocks.AIR || ru.govno.client.module.modules.Speed.posBlock(x, y - 1.0, z) && AimAssist.mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() == Blocks.WATER) {
                confirm = true;
            }
            if (AimAssist.mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() == Blocks.WATER && AimAssist.mc.world.getBlockState(new BlockPos(x, y + 0.4, z)).getBlock() == Blocks.AIR) {
                confirm = true;
            }
        }
        if (AimAssist.mc.world.getBlockState(new BlockPos(x, y + Minecraft.player.getWaterOffset(), z)).getBlock() == Blocks.WATER && AimAssist.mc.world.getBlockState(new BlockPos(x, y + Minecraft.player.getWaterOffset() + 0.001, z)).getBlock() == Blocks.AIR && (Minecraft.player.isJumping() || Minecraft.player.fallDistance > 0.0f)) {
            confirm = true;
        }
        return confirm;
    }

    boolean canCrits() {
        float ext;
        double x = Minecraft.player.posX;
        double y = Minecraft.player.posY;
        double z = Minecraft.player.posZ;
        float f = ext = Minecraft.player.isJumping() ? 0.04f : 0.0f;
        if (ElytraBoost.get.actived && ElytraBoost.get.Mode.currentMode.equalsIgnoreCase("NcpFly") && ElytraBoost.canElytra()) {
            return false;
        }
        if (Criticals.get.actived && Criticals.get.EntityHit.getBool() && !Criticals.get.HitMode.currentMode.equalsIgnoreCase("VanillaHop") && !Minecraft.player.isJumping() && Minecraft.player.onGround) {
            return false;
        }
        if (Minecraft.player.isInWeb && AimAssist.mc.world.getBlockState(new BlockPos(x, y + 0.01, z)).getBlock() != Blocks.AIR || Minecraft.player.isInWater() && !this.confirmCritsInWater(x, y, z) || Minecraft.player.isInLava()) {
            return false;
        }
        if (JesusSpeed.isSwimming || Minecraft.player.isElytraFlying() || JesusSpeed.isJesused && Minecraft.player.fallDistance != 0.0f) {
            return false;
        }
        if (Fly.get.actived) {
            return false;
        }
        if (FreeCam.get.actived) {
            return false;
        }
        if (Minecraft.player != null && Minecraft.player.isPotionActive(Potion.getPotionById(25))) {
            return false;
        }
        if (ElytraBoost.get.actived && ElytraBoost.get.Mode.currentMode.equalsIgnoreCase("Vanilla")) {
            return false;
        }
        if ((MoveHelper.getBlockWithExpand(Minecraft.player.width / 2.0f, x, y + (double)ext, z, Blocks.WEB) || MoveHelper.getBlockWithExpand(Minecraft.player.width / 2.0f, x, y + (double)ext, z, Blocks.WATER) || MoveHelper.getBlockWithExpand(Minecraft.player.width / 2.0f, x, y + (double)ext, z, Blocks.LAVA)) && !this.confirmCritsInWater(x, y, z)) {
            return false;
        }
        if (!(Minecraft.player.isJumping() || !Minecraft.player.onGround || Criticals.get.actived && Criticals.get.EntityHit.getBool() && !Criticals.get.HitMode.getMode().equalsIgnoreCase("VanillaHop"))) {
            return false;
        }
        if (Minecraft.player.isOnLadder() || Minecraft.player.isElytraFlying() || Minecraft.player.capabilities.isFlying || Minecraft.player.isRiding() || Minecraft.player.isSpectator()) {
            return false;
        }
        return !Criticals.grimUpCriticals();
    }

    Entity getMouseOverEntity() {
        EntityLivingBase base = MathUtils.getPointedEntity(new Vector2f(Minecraft.player.rotationYaw, Minecraft.player.rotationPitch), 100.0, 1.0f, false);
        if (base != null && base != Minecraft.player && base.isEntityAlive()) {
            return base;
        }
        return null;
    }

    @Override
    public void onUpdate() {
        target = this.getTarget(this.Range.getFloat());
        if (target == null) {
            return;
        }
        if (this.AutoAttack.getBool() && target != HitAura.TARGET_ROTS && (Mouse.isButtonDown((int)0) || !this.OnlyAttackKey.getBool())) {
            AimAssist.mc.gameSettings.keyBindAttack.pressed = false;
            if (AimAssist.mc.objectMouseOver == null) {
                return;
            }
            if (this.getMouseOverEntity() == target || AimAssist.mc.objectMouseOver.entityHit == target || this.isValidEntity((EntityLivingBase)AimAssist.mc.pointedEntity)) {
                boolean hitting;
                boolean critted = this.isCritical() || !this.canCrits();
                boolean bl = hitting = !(!this.hasCooled() || !PushAttack.get.actived && Minecraft.player.isHandActive() || !critted && this.SmartCrits.getBool());
                if (hitting) {
                    this.hit(target, this.HitsMode.currentMode.equalsIgnoreCase("Click"), this.StopSprint.getBool());
                    this.timerHelper.reset();
                }
            }
        }
        super.onUpdate();
    }

    @Override
    public void onMovement() {
        TargetStrafe.target = target = this.getTarget(this.Range.getFloat());
        if (target == null) {
            return;
        }
        float f = this.faceTarget(target, 360.0f, 360.0f, false)[0];
        float f2 = this.faceTarget(target, 360.0f, 360.0f, false)[1];
        if ((!this.OnlyAttackKey.getBool() || Mouse.isButtonDown((int)0)) && this.getMouseOverEntity() != target && this.AimBot.getBool() && this.isInFOV(target, this.Fov.getFloat())) {
            if (Minecraft.player.rotationYaw != f) {
                Minecraft.player.rotationYaw = MathUtils.lerp(Minecraft.player.rotationYaw, AimAssist.Rotation(target)[0], 0.05f * this.Speed.getFloat());
            }
            if (this.isInFOVPitch(target, this.Fov.getFloat())) {
                if (Minecraft.player.rotationPitch != f2) {
                    Minecraft.player.rotationPitch = MathUtils.lerp(Minecraft.player.rotationPitch, AimAssist.Rotation(target)[1], 0.025f * this.Speed.getFloat());
                }
                Minecraft.player.rotationPitch = MathUtils.clamp(Minecraft.player.rotationPitch, -90.0f, 90.0f);
            }
        }
    }

    public EntityLivingBase getTarget(float range) {
        if (HitAura.TARGET_ROTS != null) {
            return HitAura.TARGET_ROTS;
        }
        EntityLivingBase base = null;
        for (Object o : AimAssist.mc.world.loadedEntityList) {
            EntityLivingBase living;
            Entity entity = (Entity)o;
            if (!(entity instanceof EntityLivingBase) || !this.isValidEntity(living = (EntityLivingBase)entity) || living.getHealth() == 0.0f || !(Minecraft.player.getDistanceToEntity(living) <= range)) continue;
            range = Minecraft.player.getDistanceToEntity(living);
            base = living;
        }
        return base;
    }

    public boolean isValidEntity(EntityLivingBase baseIn) {
        EntityOtherPlayerMP MP;
        boolean players = this.Players.getBool();
        boolean mobs = this.Mobs.getBool();
        boolean walls = this.Walls.getBool();
        boolean invis = this.Invis.getBool();
        boolean bl = HitAura.TARGET_ROTS != null ? baseIn == HitAura.TARGET_ROTS : baseIn != null && baseIn.getHealth() != 0.0f && (players && baseIn instanceof EntityOtherPlayerMP && (MP = (EntityOtherPlayerMP)baseIn) != FreeCam.fakePlayer || mobs && !(baseIn instanceof EntityPlayer)) && (walls || Minecraft.player.canEntityBeSeen(baseIn)) && !Client.friendManager.isFriend(baseIn.getName()) && (invis || !baseIn.isInvisible()) && !Client.summit(baseIn);
        return bl;
    }

    public static float[] Rotation(Entity e) {
        double d = e.posX - Minecraft.player.posX;
        double d1 = e.posZ - Minecraft.player.posZ;
        if (e instanceof EntityLivingBase) {
            EntityLivingBase entityLivingBase = (EntityLivingBase)e;
        }
        EntityLivingBase entitylivingbase = (EntityLivingBase)e;
        float y = (float)(entitylivingbase.posY + (double)entitylivingbase.getEyeHeight() - (double)(entitylivingbase.getEyeHeight() / 3.0f));
        double lastY = (double)y + (double)0.2f - (Minecraft.player.posY + (double)Minecraft.player.getEyeHeight());
        double d2 = MathHelper.sqrt(d * d + d1 * d1);
        float yaw = (float)(Math.atan2(d1, d) * 180.0 / Math.PI - 92.0) + RandomUtils.nextFloat((float)1.0f, (float)7.0f);
        float pitch = (float)(-(Math.atan2(lastY, d2) * 210.0 / Math.PI)) + RandomUtils.nextFloat((float)1.0f, (float)7.0f);
        yaw = Minecraft.player.rotationYaw + RotationUtil.getSensitivity(MathHelper.wrapDegrees(yaw - Minecraft.player.rotationYaw));
        pitch = Minecraft.player.rotationPitch + RotationUtil.getSensitivity(MathHelper.wrapDegrees(pitch - Minecraft.player.rotationPitch));
        pitch = MathHelper.clamp(pitch, -88.5f, 89.9f);
        return new float[]{yaw, pitch};
    }

    public float[] faceTarget(Entity target, float p_706252, float p_706253, boolean miss) {
        double var7;
        double var4 = target.posX - Minecraft.player.posX;
        double var5 = target.posZ - Minecraft.player.posZ;
        if (target instanceof EntityLivingBase) {
            EntityLivingBase var6 = (EntityLivingBase)target;
            var7 = var6.posY + (double)var6.getEyeHeight() - (Minecraft.player.posY + (double)Minecraft.player.getEyeHeight());
        } else {
            var7 = (target.getEntityBoundingBox().minY + target.getEntityBoundingBox().maxY) / 2.0 - (Minecraft.player.posY + (double)Minecraft.player.getEyeHeight());
        }
        double var8 = MathHelper.sqrt(var4 * var4 + var5 * var5);
        float var9 = (float)(Math.atan2(var5, var4) * 180.0 / Math.PI) - 90.0f;
        float var10 = (float)(-(Math.atan2(var7 - (target instanceof EntityPlayer ? 0.25 : 0.0), var8) * 180.0 / Math.PI));
        float f = AimAssist.mc.gameSettings.mouseSensitivity * 0.9f + 0.2f;
        float gcd = f * f * f * 1.2f;
        float pitch = this.updateRotation(Minecraft.player.rotationPitch, var10, p_706253);
        float yaw = this.updateRotation(Minecraft.player.rotationYaw, var9, p_706252);
        yaw -= yaw % gcd;
        pitch -= pitch % gcd;
        return new float[]{yaw, pitch};
    }

    public float updateRotation(float current, float intended, float speed) {
        float f = MathHelper.wrapDegrees(intended - current);
        if (f > speed) {
            f = speed;
        }
        if (f < -speed) {
            f = -speed;
        }
        return current + f;
    }

    private boolean isInFOV(EntityLivingBase entity, double angle) {
        double angleDiff = AimAssist.getAngleDifference(Minecraft.player.rotationYaw, AimAssist.getRotations(entity.posX, entity.posY, entity.posZ)[0]);
        return angleDiff > 0.0 && angleDiff < (angle *= 0.5) || -angle < angleDiff && angleDiff < 0.0;
    }

    private boolean isInFOVPitch(EntityLivingBase entity, double angle) {
        double angleDiff = AimAssist.getAngleDifferencePitch(EventPlayerMotionUpdate.pitch, AimAssist.getRotations(entity.posX, entity.posY, entity.posZ)[1]);
        return angleDiff > 0.0 && angleDiff < angle || -angle < angleDiff && angleDiff < 0.0;
    }

    private static float getAngleDifference(float dir, float yaw) {
        float f = Math.abs(yaw - dir) % 360.0f;
        return f > 180.0f ? 360.0f - f : f;
    }

    private static float getAngleDifferencePitch(float dir, float pitch) {
        float f = Math.abs(pitch - dir) % 90.0f;
        return f > 0.0f ? 90.0f - f : f;
    }

    private static float[] getRotations(double x, double y, double z) {
        double diffX = x - Minecraft.player.posX;
        double diffY = (y + 0.5) / 2.0 - (Minecraft.player.posY + (double)Minecraft.player.getEyeHeight());
        double diffZ = z - Minecraft.player.posZ;
        double dist = MathHelper.sqrt(diffX * diffX + diffZ * diffZ);
        float yaw = (float)(Math.atan2(diffZ, diffX) * 180.0 / Math.PI) - 90.0f;
        float pitch = (float)(-(Math.atan2(diffY, dist) * 180.0 / Math.PI)) + (float)(Minecraft.player.getDistanceToEntity(target) >= 3.0f ? 3 : 1);
        return new float[]{yaw, pitch};
    }
}

