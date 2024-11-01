package ru.govno.client.module.modules;

import java.util.Arrays;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import ru.govno.client.event.EventTarget;
import ru.govno.client.event.events.EventMovementInput;
import ru.govno.client.event.events.EventPlayerMotionUpdate;
import ru.govno.client.event.events.EventRotationJump;
import ru.govno.client.event.events.EventRotationStrafe;
import ru.govno.client.event.events.EventSafeWalk;
import ru.govno.client.module.Module;
import ru.govno.client.module.modules.ClientColors;
import ru.govno.client.module.modules.HitAura;
import ru.govno.client.module.modules.Timer;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.module.settings.FloatSettings;
import ru.govno.client.module.settings.ModeSettings;
import ru.govno.client.newfont.CFontRenderer;
import ru.govno.client.newfont.Fonts;
import ru.govno.client.utils.Combat.RotationUtil;
import ru.govno.client.utils.Math.BlockUtils;
import ru.govno.client.utils.Math.MathUtils;
import ru.govno.client.utils.Math.TimerHelper;
import ru.govno.client.utils.Movement.MoveMeHelp;
import ru.govno.client.utils.Render.ColorUtils;
import ru.govno.client.utils.Render.RenderUtils;

public class ScaffWalk
extends Module {
    public static ScaffWalk get;
    public final TimerHelper placeDelay = new TimerHelper();
    public BoolSettings ElevatorBoost;
    public BoolSettings RotateMoveSide;
    public BoolSettings PlaceFromInventory;
    public FloatSettings PlaceDelay = new FloatSettings("PlaceDelay", 200.0f, 500.0f, 50.0f, this);
    public FloatSettings MultiplyWalkSpeed;
    public ModeSettings Rotation;
    public ModeSettings FallSaver;
    public ModeSettings Elevator;
    public ModeSettings Sprinting;
    public ModeSettings SwingAction;
    public ModeSettings PlaceTick;
    private boolean placeTick;
    private int placed;
    private int haveCount;
    protected boolean hasRClickSucessful = false;
    protected EnumHand getHasActiveHand;
    protected boolean hasSucessfulRClick;
    BlockPos posToPlace;
    BlockPos forceBlockPos;
    boolean forcePlaceFromInventory;
    boolean runPlace;
    boolean forceElevator;
    boolean tempRotationStatus;
    long forceDelay;
    private float lastSilentYaw;

    public ScaffWalk() {
        super("ScaffWalk", 0, Module.Category.PLAYER);
        this.settings.add(this.PlaceDelay);
        this.Rotation = new ModeSettings("Rotation", "None", this, new String[]{"None", "Snap", "Always"});
        this.settings.add(this.Rotation);
        this.FallSaver = new ModeSettings("FallSaver", "Always", this, new String[]{"None", "Always", "InAir", "OnGround"});
        this.settings.add(this.FallSaver);
        this.Elevator = new ModeSettings("Elevator", "None", this, new String[]{"None", "Matrix", "Strict", "NCP"});
        this.settings.add(this.Elevator);
        this.ElevatorBoost = new BoolSettings("ElevatorBoost", false, this, () -> !this.Elevator.getMode().equalsIgnoreCase("None"));
        this.settings.add(this.ElevatorBoost);
        this.Sprinting = new ModeSettings("Sprinting", "Never", this, new String[]{"Default", "Always", "Never", "AlmostRage"});
        this.settings.add(this.Sprinting);
        this.RotateMoveSide = new BoolSettings("RotateMoveSide", true, this);
        this.settings.add(this.RotateMoveSide);
        this.SwingAction = new ModeSettings("SwingAction", "Packet", this, new String[]{"None", "Packet", "Client"});
        this.settings.add(this.SwingAction);
        this.PlaceTick = new ModeSettings("PlaceTick", "Pre", this, new String[]{"Pre", "Post"});
        this.settings.add(this.PlaceTick);
        this.MultiplyWalkSpeed = new FloatSettings("MultiplyWalkSpeed", 0.8f, 1.0f, 0.0f, this);
        this.settings.add(this.MultiplyWalkSpeed);
        this.PlaceFromInventory = new BoolSettings("PlaceFromInventory", false, this);
        this.settings.add(this.PlaceFromInventory);
        get = this;
    }

    @Override
    public boolean isBetaModule() {
        return true;
    }

    private boolean elevatorIsPossible() {
        switch (this.Elevator.currentMode) {
            case "Matrix": {
                return Minecraft.player.isJumping() && !MoveMeHelp.moveKeysPressed() && !ScaffWalk.mc.world.getCollisionBoxes(Minecraft.player, Minecraft.player.boundingBox.offsetMinDown(0.999)).isEmpty() && !Minecraft.player.onGround;
            }
            case "Strict": {
                return Minecraft.player.isJumping() && !MoveMeHelp.moveKeysPressed() && MoveMeHelp.getSpeed() == 0.0 && !ScaffWalk.mc.world.getCollisionBoxes(Minecraft.player, Minecraft.player.boundingBox.offsetMinDown(0.12)).isEmpty();
            }
            case "NCP": {
                return Minecraft.player.isJumping() && Minecraft.player.motionY < 0.02;
            }
        }
        return false;
    }

    private void controlSprinting(boolean isRotated) {
        boolean sprintState = Minecraft.player.isSprinting();
        switch (this.Sprinting.currentMode) {
            case "Always": {
                sprintState = true;
                break;
            }
            case "Never": {
                sprintState = false;
                break;
            }
            case "AlmostRage": {
                sprintState = !isRotated;
            }
        }
        Minecraft.player.setSprinting(sprintState);
        ScaffWalk.mc.gameSettings.keyBindSprint.pressed = sprintState;
        Minecraft.player.serverSprintState = sprintState;
    }

    private boolean fallSaverIsPossible() {
        switch (this.FallSaver.currentMode) {
            case "Always": {
                return true;
            }
            case "InAir": {
                return !Minecraft.player.onGround;
            }
            case "OnGround": {
                return Minecraft.player.onGround;
            }
        }
        return false;
    }

    private void doMulSpeed() {
        float mul = this.MultiplyWalkSpeed.getFloat();
        if (mul == 1.0f || !MoveMeHelp.moveKeysPressed()) {
            return;
        }
        Minecraft.player.multiplyMotionXZ(mul);
    }

    private BlockPos nextPosAtMotion(BlockPos posBase, double mx, double my, double mz) {
        double yaw = MoveMeHelp.moveYaw(Minecraft.player.rotationYaw);
        double sin = Math.sin(Math.toRadians(yaw));
        double cos = -Math.cos(Math.toRadians(yaw));
        return posBase.add(sin, 0.0, cos);
    }

    private BlockPos findBlockPosToPlace(boolean ignoreSelfAABB) {
        BlockPos downSelfBPos;
        BlockPos selfBPos = new BlockPos(Minecraft.player.posX, Minecraft.player.lastTickPosY - 0.999, Minecraft.player.posZ);
        boolean has = this.canPlaceBlock(selfBPos, ignoreSelfAABB);
        if (!has && ScaffWalk.mc.world.isAirBlock(selfBPos) && (has = this.canPlaceBlock(downSelfBPos = selfBPos.down(), ignoreSelfAABB))) {
            selfBPos = downSelfBPos;
        }
        if (!has && ScaffWalk.mc.world.isAirBlock(selfBPos)) {
            BlockPos finalSelfBPos = selfBPos;
            for (BlockPos temp : Arrays.stream(EnumFacing.values()).map(face -> finalSelfBPos.offset((EnumFacing)face)).collect(Collectors.toList())) {
                if (temp == null || !this.canPlaceBlock(temp, ignoreSelfAABB)) continue;
                selfBPos = temp;
                has = true;
                break;
            }
        }
        return has ? selfBPos : null;
    }

    private boolean canRotate() {
        switch (this.Rotation.currentMode) {
            case "Always": {
                return true;
            }
            case "Snap": {
                return this.placeTick;
            }
        }
        return false;
    }

    private void doElevatorMoveActions() {
        switch (this.Elevator.currentMode) {
            case "Matrix": {
                Minecraft.player.onGround = true;
                break;
            }
            case "Strict": {
                Vec3d selfVec = this.virtSelfPos();
                mc.getConnection().sendPacket(new CPacketPlayer.Position(selfVec.xCoord, selfVec.yCoord + 0.41999998688698, selfVec.zCoord, false));
                mc.getConnection().sendPacket(new CPacketPlayer.Position(selfVec.xCoord, selfVec.yCoord + 0.7531999805211997, selfVec.zCoord, false));
                mc.getConnection().sendPacket(new CPacketPlayer.Position(selfVec.xCoord, selfVec.yCoord + 1.00133597911214, selfVec.zCoord, false));
                Minecraft.player.setPosY(Minecraft.player.posY + 1.1);
                break;
            }
            case "NCP": {
                if (!this.placeTick) break;
                Entity.motiony = -0.62;
                Minecraft.player.motionY = -0.078;
            }
        }
    }

    private Vec3d placeRotateVec(BlockPos pos, boolean head) {
        EnumFacing face = BlockUtils.getPlaceableSide(pos);
        if (face != null) {
            Vec3d vec = new Vec3d(pos).addVector(0.5 + (double)face.getFrontOffsetX() * 0.499, 0.5 + (double)face.getFrontOffsetY() * 0.499, 0.5 + (double)face.getFrontOffsetZ() * 0.499);
            if (head && MoveMeHelp.moveKeysPressed()) {
                double yaw = MoveMeHelp.moveYaw(Minecraft.player.rotationYaw);
                double sin = Math.sin(Math.toRadians(yaw));
                double cos = -Math.cos(Math.toRadians(yaw));
                vec = vec.addVector(-sin, 0.0, -cos);
            }
            return vec;
        }
        return null;
    }

    private boolean canPlaceBlock(BlockPos pos, boolean ignoreSelfBox) {
        boolean aired = BlockUtils.getBlockMaterial(pos).isReplaceable();
        boolean neared = BlockUtils.blockMaterialIsCurrentWithSideSets(pos);
        return aired && neared && ScaffWalk.mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos)).stream().filter(e -> !ignoreSelfBox).collect(Collectors.toList()).isEmpty() && this.placeRotateVec(pos, false) != null;
    }

    private boolean itemStackIsCurrentToPlace(ItemStack stack) {
        return stack != null && stack.getItem() instanceof ItemBlock;
    }

    private int getAnyBlocksCount() {
        int count = this.itemStackIsCurrentToPlace(Minecraft.player.getHeldItemOffhand()) ? Minecraft.player.getHeldItemOffhand().stackSize : 0;
        for (int slot = 0; slot < (this.PlaceFromInventory.getBool() ? 44 : 8); ++slot) {
            ItemStack stack = Minecraft.player.inventory.getStackInSlot(slot);
            if (!this.itemStackIsCurrentToPlace(stack)) continue;
            count += stack.stackSize;
        }
        return count;
    }

    private void doSwingAction(EnumHand hand) {
        switch (this.SwingAction.currentMode) {
            case "Packet": {
                mc.getConnection().sendPacket(new CPacketAnimation(hand));
                break;
            }
            case "Client": {
                Minecraft.player.swingArm(hand);
            }
        }
    }

    private long getPlaceDelay(boolean hasElevator) {
        return (long)(hasElevator ? 50.0f : this.PlaceDelay.getFloat() / (float)ScaffWalk.mc.timer.speed);
    }

    private float[] getRotation(Vec3d toVec) {
        float[] fArray;
        if (toVec == null) {
            float[] fArray2 = new float[2];
            fArray2[0] = Minecraft.player.rotationYaw;
            fArray = fArray2;
            fArray2[1] = Minecraft.player.rotationPitch;
        } else {
            fArray = RotationUtil.getNeededFacing(toVec, true, Minecraft.player, false);
        }
        return fArray;
    }

    private float[] setRotation(EventPlayerMotionUpdate event, float[] yaw$pitch) {
        event.setYaw(yaw$pitch[0]);
        event.setPitch(yaw$pitch[1]);
        Minecraft.player.rotationYawHead = event.getYaw();
        Minecraft.player.renderYawOffset = event.getYaw();
        Minecraft.player.rotationPitchHead = event.getPitch();
        HitAura.get.rotations = yaw$pitch;
        HitAura.get.noRotateTick = true;
        return yaw$pitch;
    }

    private void updateObjectMouseOverSilent(float[] rotateSilent, Runnable dataRunner) {
        float prevYaw = Minecraft.player.rotationYaw;
        float prevPitch = Minecraft.player.rotationPitch;
        Minecraft.player.rotationYaw = rotateSilent[0];
        Minecraft.player.rotationPitch = rotateSilent[1];
        ScaffWalk.mc.entityRenderer.getMouseOver(1.0f);
        dataRunner.run();
        Minecraft.player.rotationYaw = prevYaw;
        Minecraft.player.rotationPitch = prevPitch;
        ScaffWalk.mc.entityRenderer.getMouseOver(mc.getRenderPartialTicks());
    }

    private boolean rClickBlockLawFully(BlockPos pos, EnumHand clickHand) {
        Vec3d posOffsetVec;
        if (pos != null && (posOffsetVec = this.placeRotateVec(pos, false)) != null) {
            this.updateObjectMouseOverSilent(RotationUtil.getNeededFacing(posOffsetVec, false, Minecraft.player, false), () -> {
                BlockPos placePos = ScaffWalk.mc.objectMouseOver.getBlockPos();
                if (placePos != null) {
                    placePos = placePos.offset(BlockUtils.getPlaceableSideSeen(pos, Minecraft.player));
                }
                if (placePos != null && ScaffWalk.mc.objectMouseOver.sideHit != null && ScaffWalk.mc.objectMouseOver.hitVec != null) {
                    this.hasRClickSucessful = ScaffWalk.mc.playerController.processRightClickBlock(Minecraft.player, ScaffWalk.mc.world, ScaffWalk.mc.objectMouseOver.getBlockPos(), ScaffWalk.mc.objectMouseOver.sideHit, ScaffWalk.mc.objectMouseOver.hitVec, clickHand) == EnumActionResult.SUCCESS;
                }
            });
        }
        return this.hasRClickSucessful;
    }

    private void afterSwitchActionHand(boolean canPlaceFromInventory, boolean hasElevator, Runnable dataRunner) {
        boolean doSwap;
        this.getHasActiveHand = null;
        int oldSlot = Minecraft.player.inventory.currentItem;
        int currentSlot = -1;
        ItemStack mainhandStack = Minecraft.player.getHeldItemMainhand();
        boolean haveInMainHand = this.itemStackIsCurrentToPlace(mainhandStack);
        if (!haveInMainHand) {
            ItemStack offhandStack = Minecraft.player.getHeldItemOffhand();
            boolean haveInOffHand = this.itemStackIsCurrentToPlace(offhandStack);
            if (haveInOffHand) {
                this.getHasActiveHand = EnumHand.OFF_HAND;
            }
        } else {
            this.getHasActiveHand = EnumHand.MAIN_HAND;
        }
        if (this.getHasActiveHand == null) {
            for (int slot = 0; slot < (canPlaceFromInventory ? 44 : 8); ++slot) {
                ItemStack stack = Minecraft.player.inventory.getStackInSlot(slot);
                if (!this.itemStackIsCurrentToPlace(stack)) continue;
                currentSlot = slot;
                this.getHasActiveHand = EnumHand.MAIN_HAND;
                break;
            }
        }
        boolean bl = doSwap = this.getHasActiveHand == EnumHand.MAIN_HAND && currentSlot != -1;
        if (doSwap) {
            if (currentSlot <= 8) {
                Minecraft.player.inventory.currentItem = currentSlot;
                ScaffWalk.mc.playerController.syncCurrentPlayItem();
            } else {
                ScaffWalk.mc.playerController.windowClick(0, currentSlot, oldSlot, ClickType.SWAP, Minecraft.player);
            }
            if (this.getHasActiveHand != null) {
                dataRunner.run();
            }
            if (currentSlot <= 8) {
                Minecraft.player.inventory.currentItem = oldSlot;
                ScaffWalk.mc.playerController.syncCurrentPlayItem();
            } else {
                ScaffWalk.mc.playerController.windowClick(0, currentSlot, oldSlot, ClickType.SWAP, Minecraft.player);
            }
            return;
        }
        if (this.getHasActiveHand != null) {
            dataRunner.run();
        }
    }

    private boolean hasBlockPlaceAction(BlockPos pos, boolean canPlaceFromInventory, boolean hasElevator) {
        this.hasSucessfulRClick = false;
        this.afterSwitchActionHand(canPlaceFromInventory, hasElevator, () -> {
            if (this.canPlaceBlock(pos, hasElevator) && (this.hasSucessfulRClick = this.rClickBlockLawFully(pos, this.getHasActiveHand))) {
                this.doSwingAction(this.getHasActiveHand);
            }
        });
        return this.hasSucessfulRClick;
    }

    private Vec3d virtSelfPos() {
        return Minecraft.player.getPositionVector();
    }

    @Override
    public void alwaysRender2D(ScaledResolution sr) {
        float aPC = this.stateAnim.getAnim();
        if ((double)aPC > 0.05) {
            CFontRenderer font = Fonts.comfortaaBold_13;
            String first = "Placed: " + this.placed;
            String second = "Have: " + (int)MathUtils.clamp((float)this.haveCount * ((double)aPC > 0.95 ? 1.0f : aPC), 0.0f, (float)this.haveCount);
            float sizeX = (font.getStringWidth(first) > font.getStringWidth(second) ? font.getStringWidth(first) : font.getStringWidth(second)) + 5;
            float sizeY = 18.0f;
            float x = (float)(sr.getScaledWidth() / 2) - sizeX / 2.0f;
            float y = (float)sr.getScaledHeight() / 1.8f - sizeY / 2.0f;
            int bgC = ColorUtils.getColor(0, 0, 9, 120.0f * aPC);
            int bgCOut1 = ClientColors.getColor1(0, aPC);
            int bgCOut2 = ClientColors.getColor2(0, aPC);
            int texC = ColorUtils.swapAlpha(ColorUtils.getFixedWhiteColor(), 255.0f * aPC);
            RenderUtils.drawAlphedRect(x, y, x + sizeX, y + sizeY, bgC);
            float ext = 1.0f;
            RenderUtils.drawLightContureRectFullGradient(x + ext, y + ext, x + sizeX - ext, y + sizeY - ext, bgCOut1, bgCOut2, true);
            font.drawString(first, x + sizeX / 2.0f - (float)(font.getStringWidth(first) / 2), y + 4.0f, texC);
            font.drawString(second, x + sizeX / 2.0f - (float)(font.getStringWidth(second) / 2), y + 4.0f + (float)font.getHeight() + 2.0f, texC);
        }
    }

    private void onSucessPlaceBlock() {
        this.placeTick = true;
        ++this.placed;
        this.placeDelay.reset();
    }

    @EventTarget
    public void onPlayerUpdateEvent(EventPlayerMotionUpdate event) {
        boolean isRotated;
        boolean postPlacing;
        BlockPos temporaryBlockPos;
        if (Minecraft.player == null) {
            return;
        }
        this.haveCount = this.getAnyBlocksCount();
        if (this.haveCount == 0) {
            return;
        }
        this.tempRotationStatus = false;
        this.doMulSpeed();
        boolean hasElevator = this.elevatorIsPossible();
        if (hasElevator) {
            this.doElevatorMoveActions();
            if (this.ElevatorBoost.getBool() && Timer.percent > 0.03) {
                Timer.forceTimer(1.5f);
            }
        }
        if ((temporaryBlockPos = this.findBlockPosToPlace(hasElevator)) != null) {
            this.posToPlace = temporaryBlockPos;
        }
        if (this.posToPlace == null) {
            return;
        }
        boolean bl = postPlacing = this.PlaceTick.currentMode.equalsIgnoreCase("Post") && this.elevatorIsPossible();
        if (this.runPlace && postPlacing && this.forceBlockPos != null && this.placeDelay.hasReached(this.forceDelay)) {
            if (this.hasBlockPlaceAction(this.forceBlockPos, this.forcePlaceFromInventory, this.forceElevator)) {
                this.onSucessPlaceBlock();
            }
            this.forceDelay = Long.MAX_VALUE;
            this.forceBlockPos = null;
            this.forcePlaceFromInventory = false;
            this.forceElevator = false;
            this.runPlace = false;
        }
        if ((isRotated = this.canRotate()) && this.posToPlace != null) {
            Vec3d vecRot;
            BlockPos bpos = this.posToPlace;
            BlockPos newpos = BlockUtils.currentWithSideSetsPos(bpos);
            if (newpos != null) {
                bpos = newpos;
            }
            this.lastSilentYaw = (vecRot = this.placeRotateVec(bpos, true)) != null ? this.setRotation(event, RotationUtil.getNeededFacing(vecRot, false, Minecraft.player, false))[0] : event.getYaw();
            this.tempRotationStatus = true;
        }
        this.controlSprinting(isRotated);
        this.placeTick = false;
        if (temporaryBlockPos != null && this.placeDelay.hasReached(this.getPlaceDelay(hasElevator))) {
            if (!postPlacing) {
                if (this.hasBlockPlaceAction(temporaryBlockPos, this.PlaceFromInventory.getBool(), hasElevator)) {
                    this.onSucessPlaceBlock();
                }
            } else {
                this.forceBlockPos = temporaryBlockPos;
                this.forcePlaceFromInventory = this.PlaceFromInventory.getBool();
                this.forceElevator = hasElevator;
                this.forceDelay = this.getPlaceDelay(hasElevator);
                this.runPlace = true;
            }
        }
    }

    private boolean isSilentStrafeFix() {
        return this.RotateMoveSide.getBool();
    }

    @EventTarget
    public void onMovementInput(EventMovementInput event) {
        if (this.actived && this.isSilentStrafeFix() && this.tempRotationStatus) {
            MoveMeHelp.fixDirMove(event, this.lastSilentYaw);
        }
    }

    @EventTarget
    public void onRotationStrafe(EventRotationStrafe event) {
        if (this.actived && this.isSilentStrafeFix() && this.tempRotationStatus) {
            event.setYaw(this.lastSilentYaw);
        }
    }

    @EventTarget
    public void onRotationJump(EventRotationJump event) {
        if (this.actived && this.isSilentStrafeFix() && this.tempRotationStatus) {
            event.setYaw(this.lastSilentYaw);
        }
    }

    @EventTarget
    public void onSilentSneakEvent(EventSafeWalk event) {
        if (this.isActived() && this.fallSaverIsPossible()) {
            event.cancel();
        }
    }

    @EventTarget
    public void onEvent3d(EventSafeWalk event) {
        if (this.isActived() && this.posToPlace != null && this.placeRotateVec(this.posToPlace, false) != null) {
            RenderUtils.setup3dForBlockPos(() -> {
                Vec3d vec = this.placeRotateVec(this.posToPlace, false);
                RenderUtils.drawCanisterBox(new AxisAlignedBB(vec).expandXyz(0.1f), true, true, true, -1, -1, -1);
            }, false);
        }
    }

    @Override
    public void onToggled(boolean actived) {
        this.stateAnim.to = actived ? 1.0f : 0.0f;
        this.posToPlace = null;
        if (!actived) {
            this.placed = 0;
        }
        super.onToggled(actived);
    }
}

