package ru.govno.client.module.modules;

import java.util.Comparator;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketConfirmTeleport;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.network.play.server.SPacketEntityStatus;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.potion.Potion;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.input.Keyboard;
import ru.govno.client.Client;
import ru.govno.client.event.EventTarget;
import ru.govno.client.event.events.EventPlayerMotionUpdate;
import ru.govno.client.event.events.EventReceivePacket;
import ru.govno.client.module.Module;
import ru.govno.client.module.modules.JesusSpeed;
import ru.govno.client.module.modules.Speed;
import ru.govno.client.module.modules.Timer;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.module.settings.ModeSettings;
import ru.govno.client.utils.Combat.EntityUtil;
import ru.govno.client.utils.Math.MathUtils;
import ru.govno.client.utils.Math.TimerHelper;
import ru.govno.client.utils.Movement.MoveMeHelp;
import ru.govno.client.utils.Movement.MovementHelper;
import ru.govno.client.utils.Wrapper;

public class LongJump
extends Module {
    public static boolean isFallDamage;
    private int ticks;
    private double packetMotionY;
    private float speed;
    public static boolean doSpeed;
    public static boolean doBow;
    public static boolean stopBow;
    public static Item oldSlot;
    private final TimerHelper timerHelper = new TimerHelper();
    public static LongJump get;
    public ModeSettings Type;
    public BoolSettings AutoBow;
    public BoolSettings Instant;
    TimerHelper wait = new TimerHelper();
    boolean toDo = false;
    BlockPos state;

    public LongJump() {
        super("LongJump", 0, Module.Category.MOVEMENT);
        this.Type = new ModeSettings("Type", "LongJump", this, new String[]{"LongJump", "BowBoost", "Solid", "DamageFly", "InstantLong", "FlagBoost", "Matrix&AACWait", "Matrix&AACDestruct"});
        this.settings.add(this.Type);
        this.AutoBow = new BoolSettings("AutoBow", true, this, () -> this.Type.currentMode.equalsIgnoreCase("DamageFly"));
        this.settings.add(this.AutoBow);
        this.Instant = new BoolSettings("Instant", false, this, () -> this.Type.currentMode.equalsIgnoreCase("Matrix&AACDestruct"));
        this.settings.add(this.Instant);
        get = this;
    }

    private boolean stackIsBlockStack(ItemStack stack) {
        return stack != null && stack.getItem() instanceof ItemBlock;
    }

    private void silentJumpRotDown(Runnable centre) {
        double y = Minecraft.player.posY;
        double[] offsets = new double[]{0.42f, 0.7531999805212024, 1.0013359791121417, 1.1661092609382138};
        if (!Minecraft.player.onGround) {
            new CPacketPlayer.Position(Minecraft.player.posX, y, Minecraft.player.posZ, true);
        }
        for (double offset : offsets) {
            mc.getConnection().sendPacket(offset == offsets[offsets.length - 1] ? new CPacketPlayer.PositionRotation(Minecraft.player.posX, y + offset, Minecraft.player.posZ, Minecraft.player.rotationYaw, 90.0f, false) : new CPacketPlayer.Position(Minecraft.player.posX, y + offset, Minecraft.player.posZ, false));
            Minecraft.player.setPosition(Minecraft.player.posX, y + offset, Minecraft.player.posZ);
        }
        float prevPitch = Minecraft.player.rotationPitch;
        Minecraft.player.rotationPitch = 90.0f;
        Minecraft.player.rotationPitchHead = 90.0f;
        LongJump.mc.entityRenderer.getMouseOver(1.0f);
        centre.run();
        Minecraft.player.rotationPitch = prevPitch;
        LongJump.mc.entityRenderer.getMouseOver(1.0f);
    }

    private void selfPlace(EnumHand hand) {
        if (LongJump.mc.objectMouseOver != null && LongJump.mc.objectMouseOver.getBlockPos() != null && LongJump.mc.objectMouseOver.hitVec != null && LongJump.mc.objectMouseOver.sideHit != null) {
            if (!Minecraft.player.isSneaking()) {
                Minecraft.player.connection.sendPacket(new CPacketEntityAction(Minecraft.player, CPacketEntityAction.Action.START_SNEAKING));
            }
            LongJump.mc.playerController.processRightClickBlock(Minecraft.player, LongJump.mc.world, LongJump.mc.objectMouseOver.getBlockPos(), LongJump.mc.objectMouseOver.sideHit, LongJump.mc.objectMouseOver.hitVec, hand);
            if (!Minecraft.player.isSneaking()) {
                Minecraft.player.connection.sendPacket(new CPacketEntityAction(Minecraft.player, CPacketEntityAction.Action.STOP_SNEAKING));
            }
        }
    }

    private boolean sentBlockPlacement(boolean canUseInventory) {
        int oldSlot = Minecraft.player.inventory.currentItem;
        int currentSlot = -1;
        EnumHand placeHand = null;
        ItemStack offStack = Minecraft.player.getHeldItemOffhand();
        if (this.stackIsBlockStack(offStack)) {
            placeHand = EnumHand.OFF_HAND;
        } else {
            ItemStack mainStack = Minecraft.player.getHeldItemMainhand();
            if (this.stackIsBlockStack(mainStack)) {
                placeHand = EnumHand.MAIN_HAND;
            }
        }
        if (placeHand == null) {
            for (int slot = 0; slot < (canUseInventory ? 44 : 8); ++slot) {
                ItemStack stackInSlot = Minecraft.player.inventory.getStackInSlot(slot);
                if (!this.stackIsBlockStack(stackInSlot)) continue;
                currentSlot = slot;
                placeHand = EnumHand.MAIN_HAND;
                break;
            }
            if (placeHand == EnumHand.MAIN_HAND && currentSlot != -1) {
                int finalCurrentSlot = currentSlot;
                EnumHand finalPlaceHand = placeHand;
                this.silentJumpRotDown(() -> {
                    if (finalCurrentSlot <= 8) {
                        Minecraft.player.inventory.currentItem = finalCurrentSlot;
                        LongJump.mc.playerController.syncCurrentPlayItem();
                    } else {
                        LongJump.mc.playerController.windowClick(0, finalCurrentSlot, oldSlot, ClickType.SWAP, Minecraft.player);
                        LongJump.mc.playerController.windowClickMemory(0, finalCurrentSlot, oldSlot, ClickType.SWAP, Minecraft.player, 150);
                    }
                    this.selfPlace(finalPlaceHand);
                    if (finalCurrentSlot <= 8) {
                        Minecraft.player.inventory.currentItem = oldSlot;
                        LongJump.mc.playerController.syncCurrentPlayItem();
                    }
                });
                return true;
            }
        }
        if (placeHand != null) {
            EnumHand finalPlaceHand1 = placeHand;
            this.silentJumpRotDown(() -> this.selfPlace(finalPlaceHand1));
            return true;
        }
        return false;
    }

    void flagHop() {
        Minecraft.player.motionY = 0.42336;
        MoveMeHelp.setSpeed(1.972);
    }

    @EventTarget
    public void onReceivePacket(EventReceivePacket event) {
        SPacketChat packet;
        Packet packet2 = event.getPacket();
        if (packet2 instanceof SPacketPlayerPosLook) {
            SPacketPlayerPosLook look = (SPacketPlayerPosLook)packet2;
            if (this.Type.currentMode.equalsIgnoreCase("FlagBoost") && !LongJump.mc.world.getCollisionBoxes(Minecraft.player, Minecraft.player.boundingBox.expand(0.1, 0.0, 1.0).offsetMinDown(1.7)).isEmpty()) {
                Minecraft.player.setPosition(look.getX(), look.getY(), look.getZ());
                mc.getConnection().sendPacket(new CPacketPlayer.Position(look.getX(), look.getY(), look.getZ(), true));
                for (int i = 0; i < 10; ++i) {
                    Minecraft.player.connection.sendPacket(new CPacketConfirmTeleport(look.getTeleportId() + i));
                }
                this.flagHop();
                LongJump.mc.timer.tempSpeed = 5.0;
                event.setCancelled(true);
            }
        }
        if (!isFallDamage) {
            SPacketEntityStatus sPacketEntityStatus;
            if (event.getPacket() instanceof SPacketEntityVelocity && ((SPacketEntityVelocity)event.getPacket()).getEntityID() == Minecraft.player.getEntityId()) {
                this.packetMotionY = (double)((SPacketEntityVelocity)event.getPacket()).motionY / 8000.0;
            }
            if ((packet2 = event.getPacket()) instanceof SPacketEntityStatus && (sPacketEntityStatus = (SPacketEntityStatus)packet2).getOpCode() == 2 && sPacketEntityStatus.getEntity(LongJump.mc.world) == Minecraft.player) {
                doSpeed = true;
            }
        } else {
            EntityLivingBase.isMatrixDamaged = false;
            doSpeed = false;
            isFallDamage = false;
            stopBow = true;
        }
        if ((this.Type.currentMode.equalsIgnoreCase("Matrix&AACWait") || this.Type.currentMode.equalsIgnoreCase("Matrix&AACDestruct")) && (packet2 = event.getPacket()) instanceof SPacketChat && ((packet = (SPacketChat)packet2).getChatComponent().getUnformattedText().contains("\u0418\u0437\u0432\u0438\u043d\u0438\u0442\u0435, \u043d\u043e \u0432\u044b \u043d\u0435 \u043c\u043e\u0436\u0435\u0442\u0435") || packet.getChatComponent().getUnformattedText().contains("but you can't"))) {
            if (this.Type.currentMode.equalsIgnoreCase("Matrix&AACWait")) {
                this.toDo = true;
            }
            event.setCancelled(true);
        }
    }

    @Override
    public void onUpdate() {
        float dir2;
        if (this.Type.currentMode.equalsIgnoreCase("FlagBoost")) {
            if (Minecraft.player.motionY != -0.0784000015258789) {
                this.timerHelper.reset();
            }
            if (!MoveMeHelp.isMoving()) {
                this.timerHelper.setTime(this.timerHelper.getCurrentMS() + 50L);
            } else {
                Minecraft.player.rotationYaw = Minecraft.player.rotationYaw + (Minecraft.player.ticksExisted % 2 == 0 ? 0.01f : -0.01f);
            }
            if (this.timerHelper.hasReached(MoveMeHelp.getSpeed() > 0.7 ? 50.0 : 100.0)) {
                Minecraft.player.onGround = true;
                this.flagHop();
                Entity.motiony = 1.0;
            }
            if (MoveMeHelp.getSpeed() > 0.6) {
                double boostTimerStable;
                double setBPS = 27.0;
                double keepSetSpeedPC = 1.0;
                double curSpeed = MoveMeHelp.getSpeedByBPS(setBPS);
                LongJump.mc.timer.tempSpeed = boostTimerStable = MathUtils.lerp(1.0, Math.max(Math.min(curSpeed / MoveMeHelp.getSpeed(), 2.5), (double)0.1f), keepSetSpeedPC);
            }
        }
        if (this.Type.currentMode.equalsIgnoreCase("InstantLong") && Minecraft.player.hurtTime == 7) {
            MoveMeHelp.setCuttingSpeed(6.603774070739746);
            Minecraft.player.motionY = 0.42;
        }
        if (this.Type.currentMode.equalsIgnoreCase("BowBoost")) {
            if (Minecraft.player.onGround && doSpeed) {
                float dir1 = (float)(-Math.sin(MovementHelper.getDirection())) * (float)(LongJump.mc.gameSettings.keyBindBack.isKeyDown() ? -1 : 1);
                dir2 = (float)Math.cos(MovementHelper.getDirection()) * (float)(LongJump.mc.gameSettings.keyBindBack.isKeyDown() ? -1 : 1);
                if (MovementHelper.isMoving() || LongJump.mc.gameSettings.keyBindForward.isKeyDown() || LongJump.mc.gameSettings.keyBindBack.isKeyDown()) {
                    if (MoveMeHelp.getSpeed() < 0.08) {
                        MoveMeHelp.setSpeed(0.42);
                    } else {
                        Minecraft.player.addVelocity((double)dir1 * 9.8 / 25.0, 0.0, (double)dir2 * 9.8 / 25.0);
                        MoveMeHelp.setSpeed(MoveMeHelp.getSpeed());
                    }
                } else if (Minecraft.player.isInWater()) {
                    Minecraft.player.addVelocity((double)dir1 * 8.5 / 25.0, 0.0, (double)dir2 * 9.5 / 25.0);
                    MoveMeHelp.setSpeed(MoveMeHelp.getSpeed());
                } else if (!Minecraft.player.onGround) {
                    if (MoveMeHelp.getSpeed() < 0.22) {
                        MoveMeHelp.setSpeed(0.22);
                    } else {
                        MoveMeHelp.setSpeed(MoveMeHelp.getSpeed() * (Minecraft.player.isMoving() ? 1.0082 : 1.0088));
                    }
                }
                if (LongJump.mc.gameSettings.keyBindJump.isKeyDown() && MoveMeHelp.getSpeed() > 0.7 && Minecraft.player.fallDistance == 0.0f) {
                    MoveMeHelp.setSpeed(0.7);
                }
            } else {
                MoveMeHelp.setCuttingSpeed(0.0);
            }
        }
        if (this.Type.currentMode.equalsIgnoreCase("LongJump")) {
            if (EntityLivingBase.isMatrixDamaged) {
                Minecraft.player.speedInAir = 0.3f;
            } else if (Minecraft.player.speedInAir == 0.3f) {
                Minecraft.player.speedInAir = 0.02f;
            }
        }
        if (this.Type.currentMode.equalsIgnoreCase("Solid")) {
            this.ticks = Minecraft.player.onGround ? ++this.ticks : 0;
            if (EntityLivingBase.isMatrixDamaged) {
                Minecraft.player.stepHeight = 0.0f;
                if (this.ticks > 1 && MoveMeHelp.getSpeed() < 1.2 && !LongJump.mc.world.getCollisionBoxes(Minecraft.player, Minecraft.player.getEntityBoundingBox().offset(0.0, Minecraft.player.motionY, 0.0)).isEmpty()) {
                    float dir1 = (float)(-Math.sin(MovementHelper.getDirection())) * (float)(LongJump.mc.gameSettings.keyBindBack.isKeyDown() ? -1 : 1);
                    dir2 = (float)Math.cos(MovementHelper.getDirection()) * (float)(LongJump.mc.gameSettings.keyBindBack.isKeyDown() ? -1 : 1);
                    if (MovementHelper.isMoving() || LongJump.mc.gameSettings.keyBindForward.isKeyDown() || LongJump.mc.gameSettings.keyBindBack.isKeyDown()) {
                        if (MoveMeHelp.getSpeed() < 0.08) {
                            MoveMeHelp.setSpeed(0.42);
                        } else {
                            Minecraft.player.addVelocity((double)dir1 * 9.8 / 25.0, 0.0, (double)dir2 * 9.8 / 25.0);
                            MoveMeHelp.setSpeed(MoveMeHelp.getSpeed());
                        }
                    } else if (Minecraft.player.isInWater()) {
                        Minecraft.player.addVelocity((double)dir1 * 8.5 / 15.0, 0.0, (double)dir2 * 9.5 / 15.0);
                        MoveMeHelp.setSpeed(MoveMeHelp.getSpeed());
                    } else if (!Minecraft.player.onGround) {
                        if (MoveMeHelp.getSpeed() < 0.22) {
                            MoveMeHelp.setSpeed(0.22);
                        } else {
                            MoveMeHelp.setSpeed(MoveMeHelp.getSpeed() * (Minecraft.player.isMoving() ? 1.0082 : 1.0088));
                        }
                    }
                    if (LongJump.mc.gameSettings.keyBindJump.isKeyDown() && MoveMeHelp.getSpeed() > 0.7 && Minecraft.player.fallDistance == 0.0f) {
                        MoveMeHelp.setSpeed(0.7);
                    }
                } else if (Speed.canMatrixBoost()) {
                    MoveMeHelp.setSpeed(MoveMeHelp.getSpeed() * 2.0);
                }
                if (this.timerHelper.hasReached(1350.0)) {
                    doSpeed = false;
                    Minecraft.player.stepHeight = 0.6f;
                    Minecraft.player.speedInAir = 0.02f;
                    this.timerHelper.reset();
                    LongJump.mc.gameSettings.keyBindJump.pressed = Keyboard.isKeyDown((int)LongJump.mc.gameSettings.keyBindJump.getKeyCode());
                }
            }
        }
        if (this.Type.currentMode.equalsIgnoreCase("Matrix&AACWait")) {
            boolean has;
            if (EntityLivingBase.isMatrixDamaged) {
                this.wait.reset();
            }
            boolean bl = has = this.state != null && this.wait.hasReached(250.0f + LongJump.mc.world.getBlockState(this.state).getBlockHardness(LongJump.mc.world, this.state) * 1400.0f);
            if (this.state != null && has && (Minecraft.player.onGround || JesusSpeed.isJesused) || this.toDo && Minecraft.player.onGround) {
                double moveYawRad = Math.toRadians(MoveMeHelp.moveYaw(Minecraft.player.rotationYaw));
                Minecraft.player.addVelocity(-Math.sin(moveYawRad) * (double)1.8f, 0.8f, Math.cos(moveYawRad) * (double)1.8f);
                this.wait.reset();
                this.toggle(false);
            }
            if (Minecraft.player.motionY > 0.43 || Minecraft.player.motionY < -0.6) {
                Minecraft.player.jumpMovementFactor = 0.0f;
                Minecraft.player.setSprinting(true);
            }
            CopyOnWriteArrayList<BlockPos> mixPoses = new CopyOnWriteArrayList<BlockPos>();
            Vec3d ePos = new Vec3d(Minecraft.player.posX, Minecraft.player.posY, Minecraft.player.posZ);
            float r = 5.0f;
            for (float x = -5.0f; x < 5.0f; x += 1.0f) {
                for (float y = -5.0f; y < 1.0f; y += 1.0f) {
                    for (float z = -5.0f; z < 5.0f; z += 1.0f) {
                        BlockPos poss = new BlockPos((double)x + ePos.xCoord, (double)y + ePos.yCoord, (double)z + ePos.zCoord);
                        Block block = LongJump.mc.world.getBlockState(poss).getBlock();
                        if (block == Blocks.AIR || block == Blocks.BARRIER || block == Blocks.BEDROCK || poss == null || !(Minecraft.player.getDistanceAtEye(poss.getX(), poss.getY(), poss.getZ()) <= 5.0)) continue;
                        mixPoses.add(poss);
                    }
                }
            }
            if (mixPoses.size() != 0) {
                mixPoses.sort(Comparator.comparing(current -> Float.valueOf(LongJump.mc.world.getBlockState((BlockPos)current).getBlockHardness(LongJump.mc.world, (BlockPos)current))));
                this.state = (BlockPos)mixPoses.get(0);
                if (this.state != null && !this.toDo && !has) {
                    Minecraft.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, this.state, EnumFacing.UP));
                    Minecraft.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, this.state, EnumFacing.UP));
                }
            }
        }
        if (this.Type.currentMode.equalsIgnoreCase("Matrix&AACDestruct")) {
            if (Minecraft.player.motionY >= 0.0 || (double)Minecraft.player.fallDistance > 1.2) {
                this.toggle(false);
            }
            if (!Minecraft.player.onGround) {
                return;
            }
            if (this.sentBlockPlacement(true)) {
                double moveYawRad = Math.toRadians(MoveMeHelp.moveYaw(Minecraft.player.rotationYaw));
                Minecraft.player.onGround = false;
                double speed = this.Instant.getBool() ? 3.3 : 1.4;
                Minecraft.player.addVelocity(-Math.sin(moveYawRad) * speed, this.Instant.getBool() ? (double)1.7f : 0.8, Math.cos(moveYawRad) * speed);
                if (this.Instant.getBool()) {
                    LongJump.mc.world.playSound(Minecraft.player, Minecraft.player.posX, Minecraft.player.posY, Minecraft.player.posZ, SoundEvents.BLOCK_PISTON_EXTEND, SoundCategory.BLOCKS, 1.0f, 1.0f);
                    LongJump.mc.world.playSound(Minecraft.player, Minecraft.player.posX, Minecraft.player.posY, Minecraft.player.posZ, SoundEvents.BLOCK_SLIME_PLACE, SoundCategory.PLAYERS, 1.0f, 1.0f);
                }
                Timer.forceTimer(this.Instant.getBool() ? 0.15f : 0.25f);
            } else {
                Client.msg("\u00a7f\u00a7lModules:\u00a7r \u00a77[\u00a7lLongJump\u00a7r\u00a77]: \u0447\u0442\u043e-\u0442\u043e \u043f\u043e\u0448\u043b\u043e \u043d\u0435 \u0442\u0430\u043a \u0438\u043b\u0438 \u043d\u0435\u0442 \u0431\u043b\u043e\u043a\u043e\u0432 \u0432 \u0438\u043d\u0432\u0435\u043d\u0442\u0430\u0440\u0435.", false);
                this.toggle(false);
            }
        }
    }

    public int oldSlot() {
        for (int i = 0; i < 9; ++i) {
            ItemStack itemStack = Minecraft.player.inventoryContainer.getSlot(i).getStack();
            if (itemStack.getItem() != oldSlot) continue;
            return i;
        }
        return -1;
    }

    @EventTarget
    public void onPlayerMotionUpdate(EventPlayerMotionUpdate e) {
        int i;
        if (this.Type.currentMode.equalsIgnoreCase("DamageFly") && this.actived) {
            if (this.AutoBow.getBool() && !stopBow && !doSpeed) {
                if (!stopBow) {
                    for (i = 0; i < 9; ++i) {
                        if (Minecraft.player.inventory.currentItem != EntityUtil.getBowAtHotbar() && !doBow) {
                            oldSlot = Minecraft.player.inventoryContainer.getSlot(Minecraft.player.inventory.currentItem).getStack().getItem();
                        }
                        if (!(Minecraft.player.inventory.getStackInSlot(i).getItem() instanceof ItemBow) || doSpeed || !doBow) continue;
                        Minecraft.player.inventory.currentItem = EntityUtil.getBowAtHotbar();
                        Minecraft.player.rotationPitchHead = EventPlayerMotionUpdate.pitch = -90.0f;
                    }
                    if (Minecraft.player.inventory.currentItem == EntityUtil.getBowAtHotbar() && !doSpeed && doBow && e.getPitch() == -90.0f) {
                        boolean bl = LongJump.mc.gameSettings.keyBindUseItem.pressed = (double)Minecraft.player.getItemInUseMaxCount() < 2.1;
                        if ((double)Minecraft.player.getItemInUseMaxCount() >= 2.1) {
                            doBow = false;
                        }
                    }
                }
                if (!doBow && oldSlot != null && Minecraft.player.inventory.currentItem != this.oldSlot()) {
                    Minecraft.player.inventory.currentItem = this.oldSlot();
                    oldSlot = null;
                    stopBow = true;
                }
            }
            if (!doSpeed && this.AutoBow.getBool() && Minecraft.player.onGround) {
                MoveMeHelp.setSpeed(0.0);
                e.ground = Minecraft.player.onGround;
                Minecraft.player.onGround = false;
                Minecraft.player.jumpMovementFactor = 0.0f;
                doBow = true;
            }
            if (doSpeed && !MoveMeHelp.isBlockAboveHead()) {
                stopBow = false;
                this.ticks = 0;
                if (this.AutoBow.getBool()) {
                    doBow = false;
                }
                if (EntityLivingBase.isMatrixDamaged) {
                    if (Minecraft.player.onGround && !Minecraft.player.isJumping()) {
                        Minecraft.player.jump();
                    }
                    if (!doBow) {
                        Minecraft.player.motionY = Minecraft.player.onGround ? 0.42 : this.packetMotionY;
                        Minecraft.player.jumpMovementFactor = 0.415f;
                        MoveMeHelp.setCuttingSpeed(MoveMeHelp.getSpeed() / 1.06);
                        stopBow = false;
                    }
                } else if (doSpeed) {
                    doSpeed = false;
                    if (Minecraft.player.onGround) {
                        doBow = true;
                    }
                }
            }
        }
        if (this.Type.currentMode.equalsIgnoreCase("BowBoost") && this.actived) {
            this.speed = MathUtils.lerp(this.speed, doSpeed ? 0.8f : 0.0f, 0.2f);
            for (i = 0; i < 9; ++i) {
                if (Minecraft.player.inventory.currentItem != EntityUtil.getBowAtHotbar() && !doBow) {
                    oldSlot = Minecraft.player.inventoryContainer.getSlot(Minecraft.player.inventory.currentItem).getStack().getItem();
                }
                if (!(Minecraft.player.inventory.getStackInSlot(i).getItem() instanceof ItemBow) || doSpeed || !doBow) continue;
                Minecraft.player.inventory.currentItem = EntityUtil.getBowAtHotbar();
            }
            if (!doBow && oldSlot != null && Minecraft.player.inventory.currentItem != this.oldSlot()) {
                Minecraft.player.inventory.currentItem = this.oldSlot();
                oldSlot = null;
            }
            if (Minecraft.player.inventory.currentItem == EntityUtil.getBowAtHotbar() && !doSpeed && doBow) {
                boolean bl = LongJump.mc.gameSettings.keyBindUseItem.pressed = Minecraft.player.getItemInUseMaxCount() < 4;
                if ((double)Minecraft.player.getItemInUseMaxCount() > 2.5) {
                    Minecraft.player.rotationPitchHead = EventPlayerMotionUpdate.pitch = Wrapper.getPlayer().isPotionActive(Potion.getPotionById(1)) ? -30.0f : -45.0f;
                }
            }
            if ((double)Minecraft.player.getItemInUseMaxCount() > 3.5) {
                doBow = false;
            }
            if (doBow && Minecraft.player.hurtTime != 0) {
                LongJump.mc.gameSettings.keyBindUseItem.pressed = false;
                doBow = false;
            }
            if (Minecraft.player.hurtTime != 0) {
                doSpeed = true;
                if (Minecraft.player.hurtTime > 7) {
                    this.timerHelper.reset();
                }
            }
            if (doSpeed) {
                MoveMeHelp.setSpeed(doSpeed ? (double)this.speed : 0.0);
            }
            if (this.timerHelper.hasReached(1300.0)) {
                doSpeed = false;
                if (this.timerHelper.hasReached(1460.0) && LongJump.mc.gameSettings.keyBindForward.isKeyDown()) {
                    doBow = true;
                    this.timerHelper.reset();
                }
            }
        }
    }

    @Override
    public String getDisplayName() {
        return this.getDisplayByMode(this.Type.currentMode);
    }

    @Override
    public void onToggled(boolean actived) {
        if (actived) {
            this.toDo = false;
            this.wait.reset();
        } else {
            this.toDo = false;
            stopBow = false;
            this.ticks = 0;
            isFallDamage = false;
            LongJump.mc.gameSettings.keyBindJump.pressed = Keyboard.isKeyDown((int)LongJump.mc.gameSettings.keyBindJump.getKeyCode());
            if (this.Type.currentMode.equalsIgnoreCase("BowBoost")) {
                LongJump.mc.gameSettings.keyBindUseItem.pressed = false;
            }
            oldSlot = null;
            doSpeed = false;
            doBow = false;
            Minecraft.player.stepHeight = 0.6f;
            Minecraft.player.speedInAir = 0.02f;
        }
        super.onToggled(actived);
    }

    static {
        oldSlot = null;
    }
}

