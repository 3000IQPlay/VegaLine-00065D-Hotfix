package ru.govno.client.module.modules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityMinecartTNT;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemAir;
import net.minecraft.item.ItemAppleGold;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemElytra;
import net.minecraft.item.ItemEndCrystal;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemSkull;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.play.client.CPacketClickWindow;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.potion.Potion;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityBed;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import ru.govno.client.Client;
import ru.govno.client.event.EventTarget;
import ru.govno.client.event.events.EventPlayerMotionUpdate;
import ru.govno.client.event.events.EventRender2D;
import ru.govno.client.module.Module;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.newfont.Fonts;
import ru.govno.client.utils.Combat.RotationUtil;
import ru.govno.client.utils.Math.BlockUtils;
import ru.govno.client.utils.Math.MathUtils;
import ru.govno.client.utils.Math.TimerHelper;
import ru.govno.client.utils.Movement.MoveMeHelp;
import ru.govno.client.utils.Render.AnimationUtils;
import ru.govno.client.utils.Render.ColorUtils;
import ru.govno.client.utils.Render.RenderUtils;
import ru.govno.client.utils.Render.StencilUtil;

public class OffHand extends Module {
   public static TimerHelper timerDelay = new TimerHelper();
   public static TimerHelper timer3 = new TimerHelper();
   public static TimerHelper timer4 = new TimerHelper();
   public static boolean doTotem;
   public static boolean doBackSlot;
   public static boolean totemBackward;
   public static boolean totemTaken;
   public static boolean callNotSave;
   public static boolean fall;
   public static boolean clientSwap;
   public static Item saveSlot;
   public static Item oldSlot;
   public static Item prevOldSlot;
   public static OffHand get;
   private final BoolSettings CanHotbarSwap;
   private final BoolSettings TotemBackward;
   private final BoolSettings ShieldApple;
   private final BoolSettings CrystalApple;
   private final BoolSettings BallApple;
   private final BoolSettings AutoBall;
   private final BoolSettings PutBecauseLack;
   private final TimerHelper afterGroundTime = new TimerHelper();
   private float fallDistance;
   private final List<String> BALL_SAMPLES = Arrays.asList("шар", "сфера", "ball", "руна", "талисман", "талик", "мяч", "аура", "амулет", "колобок");
   private final List<String> CHAR_SAMPLES = Arrays.asList(
      "§1", "§2", "§3", "§4", "§5", "§6", "§7", "§8", "§9", "§0", "§c", "§e", "§a", "§b", "§d", "§f", "§r", "§l", "§k", "§o", "§m", "§n"
   );
   private final List<OffHand.AttributeWithValue> attributeWithValues = new ArrayList<>();
   private OffHand.AttributeType currentAttributeType;
   private OffHand.AttributeType prevAttributeType;
   public static AnimationUtils scaleAnim = new AnimationUtils(0.0F, 0.0F, 0.07F);
   public static AnimationUtils popAnim = new AnimationUtils(0.0F, 0.0F, 0.03F);

   public OffHand() {
      super("OffHand", 0, Module.Category.PLAYER);
      get = this;
      this.settings.add(this.CanHotbarSwap = new BoolSettings("CanHotbarSwap", true, this));
      this.settings.add(this.PutBecauseLack = new BoolSettings("PutBecauseLack", false, this));
      this.settings.add(this.TotemBackward = new BoolSettings("TotemBackward", true, this));
      this.settings.add(this.ShieldApple = new BoolSettings("ShieldApple", true, this));
      this.settings.add(this.CrystalApple = new BoolSettings("CrystalApple", true, this));
      this.settings.add(this.BallApple = new BoolSettings("BallApple", true, this));
      this.settings.add(this.AutoBall = new BoolSettings("AutoBall", true, this));
   }

   @EventTarget
   public void onEventUpdate(EventPlayerMotionUpdate event) {
      if (this.actived) {
         if (!event.onGround()) {
            this.afterGroundTime.reset();
         }

         long timeOfGround = this.afterGroundTime.getTime();
         if (timeOfGround > 1000L) {
            this.fallDistance = 0.0F;
         } else {
            if (Minecraft.player.fallDistance != 0.0F) {
               this.fallDistance = Minecraft.player.fallDistance;
            }

            if (timeOfGround == 0L && Minecraft.player.hurtTime == 9) {
               this.fallDistance = 0.0F;
            }
         }
      }
   }

   private void updateEmptyHandFix() {
      if (this.PutBecauseLack.getBool()) {
         if (Minecraft.player.ticksExisted < 10) {
            if (!totemTaken && Minecraft.player.getHeldItemOffhand().getItem() == Items.TOTEM) {
               int ballSlot = this.getSlotByItem(Items.SKULL);
               if (ballSlot != -1) {
                  oldSlot = Minecraft.player.inventory.getStackInSlot(ballSlot).getItem();
               } else if (this.getSlotByItem(Items.SHIELD) != -1) {
                  oldSlot = Items.SHIELD;
               } else if (this.getSlotByItem(Items.GOLDEN_APPLE) != -1) {
                  oldSlot = Items.GOLDEN_APPLE;
               }
            }
         } else {
            ItemStack offStack = Minecraft.player.getHeldItemOffhand();
            if (offStack.getItem() instanceof ItemAir && !totemTaken && !doBackSlot && oldSlot != null && this.getSlotByItem(prevOldSlot) == -1) {
               List<Item> samples = Arrays.asList(Items.POTIONITEM, Items.BOW, Items.SKULL, Items.SHIELD, Items.END_CRYSTAL, Items.GOLDEN_APPLE);
               samples = samples.stream()
                  .filter(
                     sample -> sample != prevOldSlot
                           && (sample != Items.SHIELD || !(Minecraft.player.getCooldownTracker().getCooldown(Items.SHIELD, 0.0F) > 0.0F))
                           && (
                              sample != Items.GOLDEN_APPLE
                                 || (!PlayerHelper.get.actived || !PlayerHelper.checkApple)
                                    && !(Minecraft.player.getCooldownTracker().getCooldown(Items.GOLDEN_APPLE, 0.0F) > 0.0F)
                           )
                           && (
                              sample != Items.POTIONITEM
                                 || Minecraft.player.getActivePotionEffect(MobEffects.REGENERATION) != null && !(Minecraft.player.getAbsorptionAmount() < 1.0F)
                           )
                           && (
                              sample != Items.BOW
                                 || this.getSlotByItem(Items.ARROW) != -1
                                    && this.getSlotByItem(Items.SPECTRAL_ARROW) != -1
                                    && this.getSlotByItem(Items.TIPPED_ARROW) != -1
                           )
                  )
                  .collect(Collectors.toList());
               if (samples.isEmpty()) {
                  return;
               }

               samples = samples.stream().filter(sample -> this.getSlotByItem(sample) != -1).collect(Collectors.toList());
               if (samples.isEmpty()) {
                  return;
               }

               Collections.reverse(samples);
               oldSlot = samples.get(0);
               doBackSlot = true;
            }
         }
      }
   }

   private boolean haveShar() {
      return this.stackIsBall(Minecraft.player.getHeldItemOffhand()) || this.stackIsBall(Minecraft.player.getItemStackFromSlot(EntityEquipmentSlot.HEAD));
   }

   private float smartTriggerHP() {
      float hp = 5.0F;
      EntityPlayer p = getMe();
      float absorbDT = 1.0F;

      for (int i = 0; i < 4; i++) {
         if (!BlockUtils.isArmor(p, BlockUtils.armorElementByInt(i))) {
            hp++;
            absorbDT += 0.5F;
         }
      }

      if (p.getActivePotionEffect(Potion.getPotionById(10)) != null) {
         hp -= 0.5F;
      }

      if (p.isHandActive() && p.getActiveItemStack().getItem() instanceof ItemAppleGold && p.getItemInUseMaxCount() > 25) {
         hp--;
      }

      if (p.getAbsorptionAmount() > 0.0F) {
         hp -= p.getAbsorptionAmount() / MathUtils.clamp(absorbDT, 1.0F, 2.0F);
      }

      if (p.inventory.armorItemInSlot(2).getItem() instanceof ItemElytra) {
         hp += 3.0F;
      }

      return MathUtils.clamp(hp, 2.0F, 20.0F);
   }

   public static boolean crystalWarn(float isInRange) {
      List<EntityEnderCrystal> enderCrystals = new CopyOnWriteArrayList<>();
      if (mc.world != null) {
         for (Entity e : mc.world.getLoadedEntityList()) {
            if (e != null && e instanceof EntityEnderCrystal && getMe().getSmartDistanceToAABB(RotationUtil.getLookRots(getMe(), e), e) < (double)isInRange) {
               enderCrystals.add((EntityEnderCrystal)e);
            }
         }
      }

      int balls = 0;

      for (int i = 0; i < 4; i++) {
         if (BlockUtils.isArmor(getMe(), BlockUtils.armorElementByInt(i))) {
            balls++;
         }
      }

      boolean isFullArmor = balls == 4;

      for (EntityEnderCrystal crystal : enderCrystals) {
         if (crystal != null
            && !(getMe().getSmartDistanceToAABB(RotationUtil.getLookRots(getMe(), crystal), crystal) >= (double)isInRange)
            && (
               BlockUtils.canPosBeSeenEntity(new Vec3d(crystal.posX, crystal.posY - 0.15F, crystal.posZ), getMe(), BlockUtils.bodyElement.FEET)
                  || !isFullArmor
                     && BlockUtils.canPosBeSeenEntity(new Vec3d(crystal.posX, crystal.posY - 0.15F, crystal.posZ), getMe(), BlockUtils.bodyElement.HEAD)
            )) {
            float rangePardon = 9.0F - (float)balls * 2.0F;
            boolean pardon = crystal.posY >= Minecraft.player.posY + 0.8 && Minecraft.player.getDistanceToEntity(crystal) > rangePardon
               || !BlockUtils.canPosBeSeenEntity(BlockUtils.getEntityBlockPos(crystal), getMe(), BlockUtils.bodyElement.LEGS)
                  && !BlockUtils.canPosBeSeenEntity(BlockUtils.getEntityBlockPos(crystal), getMe(), BlockUtils.bodyElement.FEET)
                  && isFullArmor;
            if (getMe().getHealth() + getMe().getAbsorptionAmount() > 24.0F && isFullArmor) {
               pardon = true;
            }

            return !pardon;
         }
      }

      enderCrystals.clear();
      return false;
   }

   private boolean tntWarn(float isInRange) {
      List<Entity> tntS = new CopyOnWriteArrayList<>();
      if (mc.world != null) {
         for (Entity e : mc.world.getLoadedEntityList()) {
            if (e != null && (e instanceof EntityTNTPrimed || e instanceof EntityMinecartTNT) && getMe().getDistanceToEntity(e) < isInRange) {
               tntS.add(e);
            }
         }
      }

      for (Entity tnt : tntS) {
         if (tnt != null && !(getMe().getDistanceToEntity(tnt) >= isInRange)) {
            return BlockUtils.canPosBeSeenEntity(new Vec3d(tnt.posX, tnt.posY, tnt.posZ), getMe(), BlockUtils.bodyElement.LEGS);
         }
      }

      return false;
   }

   private double getDistanceToTileEntityAtEntity(Entity entity, TileEntity tileEtity) {
      return entity.getDistanceToBlockPos(tileEtity.getPos());
   }

   private boolean bedWarn(float isInRange) {
      if (Minecraft.player.dimension != 0) {
         List<TileEntityBed> bedTiles = new CopyOnWriteArrayList<>();
         if (mc.world != null && Minecraft.player.dimension != 0) {
            for (TileEntity t : mc.world.getLoadedTileEntityList()) {
               if (t != null && t instanceof TileEntityBed && this.getDistanceToTileEntityAtEntity(getMe(), t) < (double)isInRange) {
                  bedTiles.add((TileEntityBed)t);
               }
            }
         }

         for (TileEntityBed bed : bedTiles) {
            if (BlockUtils.canPosBeSeenEntity(
               new Vec3d((double)bed.getPos().getX() + 0.5, (double)bed.getPos().getY() + 0.4, (double)bed.getPos().getZ() + 0.5),
               getMe(),
               BlockUtils.bodyElement.LEGS
            )) {
               return true;
            }
         }
      }

      return false;
   }

   private double getCollideYPosition(BlockPos pos) {
      double value = (double)(pos.getY() + 1);
      IBlockState state = mc.world.getBlockState(pos);
      AxisAlignedBB aabb = state.getSelectedBoundingBox(mc.world, pos);
      return aabb == null ? value : aabb.maxY;
   }

   private boolean isCollidablePos(BlockPos pos) {
      return !mc.world.getCollisionBoxes(null, new AxisAlignedBB(pos)).isEmpty();
   }

   private boolean isLiquidPos(BlockPos pos) {
      Material material = mc.world.getBlockState(pos).getMaterial();
      return material.isLiquid() && material.getMaterialMapColor() == MapColor.WATER;
   }

   private double presentFallDistance(double appendOnPreY, int ticksPre) {
      EntityPlayer self = getMe();
      double fd = (double)this.fallDistance;
      if (fd > 3.0) {
         double underY = self.posY;
         double posX = self.posX;
         double posY = underY + (self.posY - self.lastTickPosY);
         double posZ = self.posZ;

         for (double y = underY; y > 0.0; y--) {
            BlockPos pos = new BlockPos(posX, y, posZ);
            if (this.isCollidablePos(pos)) {
               BlockPos posUp = pos.up();
               if (!this.isLiquidPos(posUp)) {
                  underY = this.getCollideYPosition(pos) - 1.0;
               }
               break;
            }
         }

         double groundDiff = Math.abs(posY - underY);
         double fallSpeed = MathUtils.clamp(Minecraft.player.posY - Minecraft.player.lastTickPosY, 0.0, 10.0 * mc.timer.speed) * appendOnPreY;
         fd = !(groundDiff < 10.0) || !(fallSpeed >= groundDiff / (double)ticksPre) && !(groundDiff < 1.0) ? 0.0 : fd + groundDiff;
      }

      return fd;
   }

   private boolean fallWarn() {
      return Minecraft.player.fallDistanceIsUnsafe(this.presentFallDistance(2.0, 2), 0.0F);
   }

   private boolean isFallWarning() {
      return fall;
   }

   private void updatefallWarn() {
      int ping = 0;
      if (Minecraft.player != null && Minecraft.player.ticksExisted > 100 && mc.getConnection().getPlayerInfo(Minecraft.player.getUniqueID()) != null) {
         try {
            ping = MathUtils.clamp(mc.getConnection().getPlayerInfo(Minecraft.player.getUniqueID()).getResponseTime(), 0, 1000);
         } catch (Exception var3) {
            System.out.println("Module-OffHand: Vegaline failled check ping");
         }
      }

      if (this.fallWarn()) {
         fall = true;
         timer3.reset();
      } else if (fall && timer3.hasReached((double)(100 + ping))) {
         fall = false;
         timer3.reset();
      }
   }

   private boolean healthWarn() {
      float health = this.smartTriggerHP();
      return Minecraft.player.getHealth() <= health;
   }

   private boolean deathWarned() {
      boolean warn = false;
      if (this.getSlotByItem(Item.getItemById(449)) != -1 || Minecraft.player.getHeldItemOffhand().getItem() == Item.getItemById(449)) {
         this.updatefallWarn();
         if (this.healthWarn()) {
            warn = true;
         }

         if (!this.haveShar()) {
            if (crystalWarn(6.656F)) {
               warn = true;
               totemBackward = false;
            }

            if (this.tntWarn(4.87F)) {
               warn = true;
               totemBackward = false;
            }

            if (this.bedWarn(7.92F)) {
               warn = true;
               totemBackward = false;
            }
         }

         this.updatefallWarn();
         if (this.isFallWarning()) {
            warn = true;
         }
      }

      return Minecraft.player.getHeldItemMainhand().getItem() != Items.TOTEM && !Minecraft.player.isCreative() && warn;
   }

   private boolean canUseItemMainHand() {
      return Minecraft.player.getHeldItemMainhand().getItem() instanceof ItemBlock
         && (mc.objectMouseOver.typeOfHit == null || mc.objectMouseOver.typeOfHit == RayTraceResult.Type.BLOCK);
   }

   private static EntityPlayer getMe() {
      return (EntityPlayer)(FreeCam.fakePlayer != null && FreeCam.get.actived ? FreeCam.fakePlayer : Minecraft.player);
   }

   @Override
   public String getDisplayName() {
      int count = Minecraft.player == null ? 0 : this.getTotemCount();
      return count > 0 ? this.getDisplayByInt(count) + "T" : this.getName();
   }

   @Override
   public void onUpdate() {
      if (Minecraft.player != null
         && Minecraft.player.getHealth() != 0.0F
         && !Minecraft.player.isDead
         && (!(mc.currentScreen instanceof GuiContainer) || mc.currentScreen instanceof GuiInventory)) {
         if (Minecraft.player.ticksExisted == 1) {
            oldSlot = null;
         }

         totemTaken = this.deathWarned() && !totemBackward;
         if (timer4.hasReached(2100.0) || !this.TotemBackward.getBool()) {
            totemBackward = false;
         }

         if (callNotSave) {
            callNotSave = false;
         } else {
            label102: {
               label128: {
                  if (Keyboard.isKeyDown(mc.gameSettings.keyBindSwapHands.getKeyCode())) {
                     Minecraft.player.getHeldItemMainhand().getItem();
                     if (Item.getItemById(449) != null) {
                        break label128;
                     }
                  }

                  if (GuiContainer.draggedStack == null || !(mc.currentScreen instanceof GuiInventory) || !Mouse.isButtonDown(0)) {
                     if (!(Minecraft.player.getHeldItemOffhand().getItem() instanceof ItemAir)
                        && Minecraft.player.getHeldItemOffhand().getItem() != Items.TOTEM) {
                        oldSlot = Minecraft.player.getHeldItemOffhand().getItem();
                     }
                     break label102;
                  }
               }

               oldSlot = null;
            }
         }

         if (this.AutoBall.getBool()) {
            this.currentAttributeType = this.getCurrentAttributeType(this.currentAttributeType);
         }

         this.updateOffHandHelps(this.CrystalApple.getBool(), this.ShieldApple.getBool(), this.BallApple.getBool());
         this.updateEmptyHandFix();
         if (this.deathWarned()
            && (this.getSlotByItem(Items.TOTEM) != -1 || Minecraft.player.getHeldItemOffhand().getItem() == Items.TOTEM)
            && !doBackSlot
            && !totemBackward) {
            if (Minecraft.player.getHeldItemOffhand().getItem() != Items.TOTEM) {
               doTotem = true;
            }
         } else if (oldSlot != null && this.getSlotByItem(oldSlot) != -1 && Minecraft.player.getHeldItemOffhand().getItem() != oldSlot) {
            doBackSlot = !Minecraft.player.isHandActive()
               || Minecraft.player.getActiveHand() != EnumHand.MAIN_HAND
               || Minecraft.player.getActiveItemStack().getItem() != oldSlot;
         } else if (this.AutoBall.getBool() && this.currentAttributeType != null && oldSlot instanceof ItemSkull) {
            OffHand.ItemStackWithSlot offStackWithSlot = new OffHand.ItemStackWithSlot(Minecraft.player.getHeldItem(EnumHand.OFF_HAND), 45);
            if (!this.hasAttributeInStack(offStackWithSlot, this.currentAttributeType)
               && !Minecraft.player.isHandActive()
               && this.hasAttributeInInventory(this.currentAttributeType)) {
               doBackSlot = true;
            }
         }

         this.doItem(45, this.CanHotbarSwap.getBool());
         if (this.AutoBall.getBool()) {
            this.prevAttributeType = this.currentAttributeType;
         }
      }
   }

   private boolean haveItem(Item itemIn) {
      return this.getSlotByItem(itemIn) != -1 || Minecraft.player.inventoryContainer.getSlot(45).getStack().getItem() == itemIn;
   }

   private boolean isBadOver() {
      if (mc.objectMouseOver != null && mc.objectMouseOver.getBlockPos() != null) {
         Block block = mc.world.getBlockState(mc.objectMouseOver.getBlockPos()).getBlock();
         List<Integer> badBlockIDs = Arrays.asList(
            96,
            167,
            54,
            130,
            146,
            58,
            64,
            71,
            193,
            194,
            195,
            196,
            197,
            324,
            330,
            427,
            428,
            429,
            430,
            431,
            154,
            61,
            23,
            158,
            145,
            69,
            107,
            187,
            186,
            185,
            184,
            183,
            107,
            116,
            84,
            356,
            404,
            151,
            25,
            219,
            220,
            221,
            222,
            223,
            224,
            225,
            226,
            227,
            228,
            229,
            230,
            231,
            232,
            233,
            234,
            389,
            379,
            380,
            138,
            321,
            323,
            77,
            143,
            379
         );
         return !Minecraft.player.isSneaking()
            && mc.objectMouseOver != null
            && mc.objectMouseOver.getBlockPos() != null
            && block != null
            && badBlockIDs.stream().anyMatch(id -> Block.getIdFromBlock(block) == id);
      } else {
         return false;
      }
   }

   private boolean stackIsBall(ItemStack stack) {
      if (stack != null && !stack.getDisplayName().isEmpty() && stack.getItem() == Items.SKULL) {
         String stackName = stack.getDisplayName();

         for (String sample : this.CHAR_SAMPLES) {
            stackName = stackName.replace(sample, "");
         }

         String finalStackName = stackName.toLowerCase();
         return this.BALL_SAMPLES.stream().anyMatch(samplex -> finalStackName.contains(samplex));
      } else {
         return false;
      }
   }

   private OffHand.AttributeType getAttributeTypeByName(String name) {
      return Arrays.stream(OffHand.AttributeType.values()).filter(attributeType -> name.endsWith(attributeType.getName())).findAny().orElse(null);
   }

   private List<OffHand.AttributeWithValue> getStackAttributes(OffHand.ItemStackWithSlot stackWithSlot) {
      if (!this.attributeWithValues.isEmpty()) {
         this.attributeWithValues.clear();
      }

      if (stackWithSlot.getItemStack() != null) {
         NBTTagCompound nbt = stackWithSlot.getItemStack().getTagCompound();
         if (nbt != null && nbt.hasKey("AttributeModifiers", 9)) {
            NBTTagList attributeList = nbt.getTagList("AttributeModifiers", 10);
            Arrays.stream(IntStream.rangeClosed(0, attributeList.tagCount() - 1).toArray())
               .mapToObj(index -> attributeList.getCompoundTagAt(index))
               .forEach(compound -> {
                  double value = compound.getDouble("Amount");
                  OffHand.AttributeType attributeType;
                  if (value != 0.0 && (attributeType = this.getAttributeTypeByName(compound.getString("AttributeName"))) != null) {
                     this.attributeWithValues.add(new OffHand.AttributeWithValue(attributeType, value, stackWithSlot));
                  }
               });
         }
      }

      return this.attributeWithValues;
   }

   private boolean hasAttributeInStack(OffHand.ItemStackWithSlot stack, OffHand.AttributeType type) {
      return type != null
         && this.getStackAttributes(stack).stream().map(OffHand.AttributeWithValue::getAttributeType).anyMatch(attributeType -> attributeType == type);
   }

   private boolean hasAttributeInInventory(OffHand.AttributeType attributeType) {
      boolean hasCurrentAttribute = false;

      for (int i = 0; i < Minecraft.player.inventoryContainer.inventorySlots.size(); i++) {
         try {
            ItemStack itemStack = Minecraft.player.inventoryContainer.getSlot(i).getStack();
            if (this.stackIsBall(itemStack) && this.hasAttributeInStack(new OffHand.ItemStackWithSlot(itemStack, i), attributeType)) {
               hasCurrentAttribute = true;
               break;
            }
         } catch (Exception var5) {
            var5.printStackTrace();
         }
      }

      return hasCurrentAttribute;
   }

   private boolean hasAttributeInStack(List<OffHand.AttributeWithValue> attributeWithValues, OffHand.ItemStackWithSlot stack, OffHand.AttributeType type) {
      return type != null
         && attributeWithValues.stream().map(OffHand.AttributeWithValue::getAttributeType).anyMatch(attributeType -> attributeType.equals(type));
   }

   private List<OffHand.AttributeWithValue> getSortedByValues(List<OffHand.AttributeWithValue> attributeWithValues) {
      return attributeWithValues.stream().sorted(Comparator.comparingDouble(e -> -e.getValue())).collect(Collectors.toList());
   }

   private List<OffHand.ItemStackWithSlot> getSortedByValuesStacks(List<OffHand.ItemStackWithSlot> itemStackWithSlots) {
      return itemStackWithSlots.stream()
         .sorted(
            Comparator.<OffHand.ItemStackWithSlot>comparingDouble(
                  stackWithSlot -> this.getStackAttributes(stackWithSlot)
                        .stream()
                        .filter(attr -> attr.getAttributeType() == this.currentAttributeType)
                        .findFirst()
                        .map(OffHand.AttributeWithValue::getValue)
                        .orElse(0.0)
               )
               .reversed()
               .thenComparingInt(stackWithSlot -> stackWithSlot.getItemStack().getMetadata())
         )
         .collect(Collectors.toList());
   }

   private void updateOffHandHelps(boolean crystalApple, boolean shieldApple, boolean ballApple) {
      boolean bad = this.isBadOver() || mc.currentScreen != null && !(mc.currentScreen instanceof GuiIngameMenu);
      boolean pcm = Mouse.isButtonDown(1) && mc.currentScreen == null;
      Item toSwapItem = Items.GOLDEN_APPLE;
      boolean mainTeadled = Minecraft.player.isHandActive() && Minecraft.player.getActiveHand() == EnumHand.MAIN_HAND
         || Minecraft.player.getHeldItemMainhand().getItem() == Items.ENDER_PEARL
         || Minecraft.player.getHeldItemMainhand().getItem() instanceof ItemPotion
         || (
               Minecraft.player.getHeldItemMainhand().getItem() instanceof ItemBlock
                  || Minecraft.player.getHeldItemMainhand().getItem() instanceof ItemEndCrystal
            )
            && mc.objectMouseOver != null
            && mc.objectMouseOver.typeOfHit == RayTraceResult.Type.BLOCK;
      boolean crystalTrigger = crystalApple
         && Minecraft.player.getHeldItemOffhand().getItem() == Items.END_CRYSTAL
         && this.haveItem(toSwapItem)
         && saveSlot == null
         && !this.canUseItemMainHand()
         && Minecraft.player.getHeldItemMainhand().getItem() != toSwapItem
         && (Minecraft.player.getHeldItemOffhand().getItem() == Items.END_CRYSTAL || oldSlot == toSwapItem)
         && !mainTeadled
         && pcm
         && !bad;
      boolean shieldTrigger = shieldApple
         && (Minecraft.player.getHeldItemOffhand().getItem() == Items.SHIELD || Minecraft.player.getHeldItemOffhand().getItem() == Items.GOLDEN_APPLE)
         && this.haveItem(toSwapItem)
         && Minecraft.player.getHeldItemMainhand().getItem() != toSwapItem
         && (
            Minecraft.player.getCooldownTracker().getCooldown(Items.SHIELD, 0.0F) > 0.1F
                  && Minecraft.player.getCooldownTracker().getCooldown(Items.SHIELD, 0.0F) < 0.97F
               || Minecraft.player.getHealth() + Minecraft.player.getAbsorptionAmount() <= 11.0F
                  && Minecraft.player.getActiveHand() == EnumHand.OFF_HAND
                  && Minecraft.player.isBlocking()
         )
         && !bad;
      boolean ballTrigger = ballApple
         && this.stackIsBall(Minecraft.player.getHeldItemOffhand())
         && this.haveItem(toSwapItem)
         && Minecraft.player.getHeldItemMainhand().getItem() != toSwapItem
         && !mainTeadled
         && Minecraft.player.getHealth() + Minecraft.player.getAbsorptionAmount() <= 15.0F
         && pcm
         && !bad;
      Item current = crystalTrigger ? Items.END_CRYSTAL : (shieldTrigger ? Items.SHIELD : (ballTrigger ? Items.SKULL : oldSlot));
      boolean isTriggered = crystalTrigger || shieldTrigger || ballTrigger;
      boolean resetTrigger = false;
      if (isTriggered) {
         saveSlot = current;
      } else if (Minecraft.player.getHeldItemOffhand().getItem() == saveSlot) {
         resetTrigger = true;
      }

      if (crystalTrigger || shieldTrigger || ballTrigger) {
         clientSwap = true;
      } else if (resetTrigger) {
         clientSwap = false;
         saveSlot = null;
      }

      if (clientSwap && !resetTrigger && saveSlot != null) {
         Item i = isTriggered ? toSwapItem : (pcm ? toSwapItem : saveSlot);
         if (Minecraft.player.getHeldItemMainhand().getItem() != i && Minecraft.player.getHeldItemOffhand().getItem() != i) {
            oldSlot = i;
         }
      }
   }

   public void invClick(int slotId, boolean pcm) {
      ItemStack itemstack = Minecraft.player.inventoryContainer.slotClick(slotId, !pcm ? 0 : 1, ClickType.PICKUP, Minecraft.player);
      Minecraft.player
         .connection
         .sendPacket(
            new CPacketClickWindow(
               Minecraft.player.inventoryContainer != null ? Minecraft.player.inventoryContainer.windowId : 0,
               slotId,
               !pcm ? 0 : 1,
               ClickType.PICKUP,
               itemstack,
               Minecraft.player.inventoryContainer.getNextTransactionID(Minecraft.player.inventory)
            )
         );
   }

   public void invClick(int slotId, boolean pcm, int ms) {
      mc.playerController
         .windowClickMemory(
            Minecraft.player.inventoryContainer != null ? Minecraft.player.inventoryContainer.windowId : 0,
            slotId,
            !pcm ? 0 : 1,
            ClickType.PICKUP,
            Minecraft.player,
            ms
         );
   }

   public void doItem(int slotIn, boolean canHotbarSwap) {
      int currentItem = doBackSlot ? this.getSlotByItem(oldSlot) : (doTotem ? this.getSlotByItem(Items.TOTEM) : -1);
      if (currentItem < 36 || currentItem > 44) {
         canHotbarSwap = false;
      }

      boolean aac = Bypass.get.isAACWinClick();
      if ((doTotem || doBackSlot) && timerDelay.hasReached(canHotbarSwap ? 50.0 : (double)(150 + (aac ? 50 : 0)))) {
         if (currentItem != -1) {
            if (currentItem >= 36 && currentItem <= 44 && !this.isBadOver() && mc.currentScreen == null) {
               if ((
                     Minecraft.player.inventory.getStackInSlot(currentItem - 36) == Minecraft.player.inventory.getCurrentItem()
                        || !(Minecraft.player.inventoryContainer instanceof ContainerPlayer)
                        || canHotbarSwap
                  )
                  && slotIn == 45) {
                  if (Minecraft.player.inventory.currentItem != currentItem - 36) {
                     Minecraft.player.connection.sendPacket(new CPacketHeldItemChange(currentItem - 36));
                  }

                  Minecraft.player
                     .connection
                     .sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.SWAP_HELD_ITEMS, BlockPos.ORIGIN, EnumFacing.DOWN));
                  Minecraft.player.setHeldItem(EnumHand.OFF_HAND, Minecraft.player.inventory.getStackInSlot(currentItem - 36));
                  if (Minecraft.player.inventory.currentItem != currentItem - 36) {
                     Minecraft.player.connection.sendPacket(new CPacketHeldItemChange(Minecraft.player.inventory.currentItem));
                  }
               } else if (slotIn != currentItem - 36) {
                  mc.playerController.windowClick(Minecraft.player.inventoryContainer.windowId, slotIn, currentItem - 36, ClickType.SWAP, Minecraft.player);
               }

               if (slotIn == 45) {
                  Minecraft.player.setHeldItem(EnumHand.OFF_HAND, Minecraft.player.getHeldItemOffhand());
               }
            } else if (slotIn != currentItem && currentItem != -1) {
               int handSlot = Minecraft.player.inventory.currentItem;
               if (aac) {
                  if (Minecraft.player.inventory.currentItem != currentItem - 36) {
                     Minecraft.player.connection.sendPacket(new CPacketHeldItemChange(Minecraft.player.inventory.currentItem));
                  }

                  ItemStack stackInCurrectSlot = Minecraft.player.inventory.getStackInSlot(currentItem);
                  mc.playerController.windowClick(0, currentItem, handSlot, ClickType.SWAP, Minecraft.player);
                  if (currentItem >= 0) {
                     Minecraft.player.setHeldItem(EnumHand.MAIN_HAND, stackInCurrectSlot);
                  } else {
                     Client.msg("BEBRA", true);
                  }

                  Minecraft.player
                     .connection
                     .sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.SWAP_HELD_ITEMS, BlockPos.ORIGIN, EnumFacing.DOWN));
                  ItemStack offStack = Minecraft.player.getHeldItemOffhand();
                  Minecraft.player.setHeldItem(EnumHand.OFF_HAND, Minecraft.player.getHeldItemMainhand());
                  Minecraft.player.setHeldItem(EnumHand.MAIN_HAND, offStack);
                  mc.playerController.windowClick(0, currentItem, handSlot, ClickType.SWAP, Minecraft.player);
               } else {
                  mc.playerController.windowClick(0, currentItem, handSlot, ClickType.SWAP, Minecraft.player);
                  mc.playerController.windowClick(0, 45, handSlot, ClickType.SWAP, Minecraft.player);
                  mc.playerController.windowClick(0, currentItem, handSlot, ClickType.SWAP, Minecraft.player);
               }

               if (!aac && slotIn == 45 && !(Minecraft.player.getHeldItemOffhand().getItem() instanceof ItemAir)) {
                  Minecraft.player.setHeldItem(EnumHand.OFF_HAND, Minecraft.player.getHeldItemOffhand());
               }
            }

            doBackSlot = false;
            doTotem = false;
            timerDelay.reset();
            callNotSave = true;
         }
      }
   }

   public int getSlotByItem(Item itemIn) {
      List<OffHand.ItemStackWithSlot> inventory = Arrays.stream(
            IntStream.rangeClosed(0, Minecraft.player.inventoryContainer.inventorySlots.size() - 1).toArray()
         )
         .mapToObj(index -> new OffHand.ItemStackWithSlot(Minecraft.player.inventoryContainer.getSlot(index).getStack(), index))
         .filter(Objects::nonNull)
         .toList();
      List<OffHand.ItemStackWithSlot> attributed = new ArrayList<>();
      List<OffHand.ItemStackWithSlot> defaults = attributed;

      for (OffHand.ItemStackWithSlot stackWithSlot : inventory) {
         ItemStack stack = stackWithSlot.getItemStack();
         int slotID = stackWithSlot.getSlot();
         if (stack.getItem() == itemIn) {
            if (this.stackIsBall(stack) && slotID != 45 && this.hasAttributeInStack(stackWithSlot, this.currentAttributeType)) {
               attributed.add(stackWithSlot);
            } else {
               defaults.add(stackWithSlot);
            }
         }
      }

      List<OffHand.ItemStackWithSlot> finalInventory = new ArrayList<>();
      if (!attributed.isEmpty()) {
         this.getSortedByValuesStacks(attributed).forEach(stackWithSlotx -> finalInventory.add(stackWithSlotx));
      }

      defaults.forEach(stackWithSlotx -> finalInventory.add(stackWithSlotx));
      return finalInventory != null && !finalInventory.isEmpty() && finalInventory.get(0) != null ? finalInventory.get(0).getSlot() : -1;
   }

   private OffHand.AttributeType setCurrentAttribute(OffHand.AttributeType attributeType, OffHand.AttributeType currentAttributeType) {
      if (attributeType != currentAttributeType && this.hasAttributeInInventory(currentAttributeType)) {
         attributeType = currentAttributeType;
      }

      return attributeType;
   }

   private OffHand.AttributeType getCurrentAttributeType(OffHand.AttributeType prevAttributeType) {
      prevAttributeType = this.setCurrentAttribute(prevAttributeType, OffHand.AttributeType.ARMOR_UP);
      if (!Timer.get.actived
         && (
            Minecraft.player.isBurning() && !Minecraft.player.isPotionActive(MobEffects.FIRE_RESISTANCE)
               || !(Minecraft.player.getHealth() >= 16.0F)
               || (
                  prevAttributeType == OffHand.AttributeType.SPEED_UP
                     ? !MoveMeHelp.moveKeysPressed() || Minecraft.player.isSneaking()
                     : !(MoveMeHelp.getCuttingSpeed() > 0.1)
               )
               || Fly.get.isActived()
               || ElytraBoost.get.isActived()
               || !mc.world
                  .playerEntities
                  .stream()
                  .map(Entity::getOtherPlayerOf)
                  .filter(Objects::nonNull)
                  .filter(player -> player.isEntityAlive() && !Client.friendManager.isFriend(player.getName()))
                  .noneMatch(player -> Minecraft.player.getDistanceToEntity(player) < 8.0F)
         )) {
         if (Minecraft.player.getHealth() >= 20.0F && Minecraft.player.getAbsorptionAmount() >= 2.0F) {
            prevAttributeType = this.setCurrentAttribute(prevAttributeType, OffHand.AttributeType.HEALTH_UP);
         } else if (HitAura.TARGET_ROTS != null) {
            if (Minecraft.player.getHealth() >= Minecraft.player.getMaxHealth() / 1.75F
               && HitAura.TARGET_ROTS != null
               && HitAura.TARGET_ROTS.getHealth() < HitAura.TARGET_ROTS.getMaxHealth() / 2.0F) {
               prevAttributeType = this.setCurrentAttribute(prevAttributeType, OffHand.AttributeType.DAMAGE_UP);
            } else if (Minecraft.player.getHealth() >= 20.0F
               && Criticals.get.isActived()
               && Criticals.get.EntityHit.getBool()
               && !Minecraft.player.isJumping()
               && !Criticals.get.HitMode.getMode().equalsIgnoreCase("VanillaHop")
               && (EntityLivingBase.isMatrixDamaged || !Criticals.get.HitMode.getMode().equalsIgnoreCase("Matrix2"))
               && (!Criticals.get.HitMode.getMode().equalsIgnoreCase("MatrixStand") || Minecraft.player.onGround && MoveMeHelp.getSpeed() == 0.0)) {
               prevAttributeType = this.setCurrentAttribute(prevAttributeType, OffHand.AttributeType.COOLDOWN_UP);
            }
         }
      } else {
         prevAttributeType = this.setCurrentAttribute(prevAttributeType, OffHand.AttributeType.SPEED_UP);
      }

      return prevAttributeType;
   }

   @EventTarget
   public void onRender2D(EventRender2D event) {
      int totemCount = this.getTotemCount();
      if (totemTaken) {
         scaleAnim.to = 1.05F;
      }

      float scaleAnimVal = scaleAnim.getAnim();
      if (scaleAnimVal > 1.0F) {
         scaleAnim.setAnim(1.0F);
         scaleAnimVal = 1.0F;
      }

      if (scaleAnim.to == 0.0F && (double)scaleAnimVal < 0.1) {
         scaleAnim.setAnim(0.0F);
      }

      float popAnimVal = popAnim.getAnim();
      if (!totemTaken && !(popAnimVal > 0.0F) && scaleAnim.to != 0.0F) {
         scaleAnim.to = 0.0F;
      }

      if (popAnim.to == 0.0F && (double)popAnimVal < 0.03) {
         popAnim.setAnim(0.0F);
      }

      popAnim.speed = 0.02F;
      if (scaleAnimVal != 0.0F) {
         float x = (float)event.getResolution().getScaledWidth() / 2.0F
            + (mc.gameSettings.thirdPersonView != 0 ? -8.0F + 20.0F * AutoApple.get.scaleAnimation.anim : 12.0F);
         float y = (float)event.getResolution().getScaledHeight() / 2.0F - 8.0F;
         x += Crosshair.get.crossPosMotions[0];
         y += Crosshair.get.crossPosMotions[1];
         GL11.glPushMatrix();
         GL11.glDepthMask(false);
         GL11.glEnable(2929);
         GL11.glTranslatef(x, y, 0.0F);
         RenderUtils.customScaledObject2D(0.0F, 0.0F, 16.0F, 16.0F, scaleAnimVal);
         float popAnimPC = 1.0F - popAnimVal;
         if (popAnimVal * 4.0F * 255.0F >= 33.0F) {
            GL11.glPushMatrix();
            RenderUtils.customScaledObject2D(0.0F, 0.0F, 16.0F, 16.0F, popAnimPC);
            RenderUtils.customRotatedObject2D(
               5.0F + popAnimPC * 20.0F, 8.0F - popAnimPC * popAnimPC * popAnimPC * popAnimPC * 8.0F, 0.0F, 0.0F, (double)(-180.0F - popAnimPC * -180.0F)
            );
            Fonts.noise_24
               .drawStringWithShadow(
                  "-1",
                  (double)(2.0F + popAnimPC * 20.0F),
                  (double)(4.0F - popAnimPC * popAnimPC * popAnimPC * popAnimPC * 8.0F),
                  ColorUtils.getColor(255, 0, 0, MathUtils.clamp(popAnimVal * 4.0F * 255.0F, 33.0F, 255.0F))
               );
            GL11.glPopMatrix();
         }

         RenderUtils.customRotatedObject2D(0.0F, 0.0F, 16.0F, 16.0F, (double)(popAnimVal * popAnimVal * popAnimVal * 20.0F));
         RenderUtils.customScaledObject2D(0.0F, 0.0F, 16.0F, 16.0F, 1.0F + popAnimVal * popAnimVal * popAnimVal);
         ItemStack stack = new ItemStack(Items.TOTEM);
         if (popAnimVal != 0.0F) {
            float popAnimValMM = 1.0F - MathUtils.clamp(popAnimVal, 0.0F, 1.0F);
            float popAnimPC2 = ((double)popAnimValMM > 0.5 ? 1.0F - popAnimValMM : popAnimValMM) * 3.0F;
            popAnimPC2 = popAnimPC2 > 1.0F ? 1.0F : popAnimPC2;
            GL11.glPushMatrix();
            StencilUtil.initStencilToWrite();
            GL11.glTranslated((double)(popAnimPC2 * 16.0F), (double)(-popAnimPC2 * 6.0F), 0.0);
            RenderUtils.customScaledObject2D(0.0F, 0.0F, 16.0F, 16.0F, 0.5F + popAnimPC2 * popAnimVal);
            RenderUtils.customRotatedObject2D(0.0F, 0.0F, 16.0F, 16.0F, (double)(270.0F * -popAnimPC * popAnimPC * popAnimPC * popAnimPC * popAnimPC));
            mc.getRenderItem().renderItemIntoGUI(stack, 0, 0);
            StencilUtil.readStencilBuffer(1);
            mc.getRenderItem().renderItemIntoGUI(stack, 0, 0);
            RenderUtils.drawAlphedRect(-24.0, -24.0, 48.0, 48.0, ColorUtils.getColor(255, 255, 255, popAnimVal * 255.0F));
            StencilUtil.uninitStencilBuffer();
            GL11.glPopMatrix();
         }

         if (popAnimVal != 0.0F) {
            GL11.glPushMatrix();
            RenderUtils.customScaledObject2D(0.0F, 0.0F, 16.0F, 16.0F, MathUtils.clamp(popAnimPC * 1.5F, 0.0F, 1.0F));
            mc.getRenderItem().renderItemIntoGUI(stack, 0, 0);
            GL11.glPopMatrix();
         } else {
            mc.getRenderItem().renderItemIntoGUI(stack, 0, 0);
         }

         int c = totemCount == 0
            ? ColorUtils.fadeColor(ColorUtils.getColor(255, 80, 50, 80.0F * scaleAnimVal), ColorUtils.getColor(255, 80, 50, 255.0F * scaleAnimVal), 1.5F)
            : ColorUtils.getColor(255, 255, 255, 255.0F * scaleAnimVal);
         (totemCount == 0 ? Fonts.noise_20 : Fonts.mntsb_12)
            .drawStringWithShadow(totemCount + "x", totemCount == 0 ? 14.0 : 12.0, totemCount == 0 ? 9.0 : 13.5, c);
         GL11.glDepthMask(true);
         GL11.glPopMatrix();
      }
   }

   @Override
   public void onToggled(boolean actived) {
      if (!actived) {
         totemTaken = false;
      }

      super.onToggled(actived);
   }

   private int getTotemCount() {
      int totemCount = 0;

      for (int i = 0; i <= 45; i++) {
         ItemStack is = Minecraft.player.inventoryContainer.getSlot(i).getStack();
         if (is.getItem() == Items.TOTEM) {
            totemCount += is.stackSize;
         }
      }

      return totemCount;
   }

   private static enum AttributeType {
      HEALTH_UP("maxHealth"),
      ANTI_KNOCKBACK("knockbackResistance"),
      DAMAGE_UP("attackDamage"),
      COOLDOWN_UP("attackSpeed"),
      ARMOR_UP("armor"),
      ARMOR_DUR("armorToughness"),
      SPEED_UP("movementSpeed");

      String attributeName;

      private AttributeType(String attributeName) {
         this.attributeName = attributeName;
      }

      String getName() {
         return this.attributeName;
      }
   }

   private class AttributeWithValue {
      private final OffHand.AttributeType attributeType;
      private final double value;
      private final OffHand.ItemStackWithSlot itemStackWithSlot;

      public OffHand.AttributeType getAttributeType() {
         return this.attributeType;
      }

      public double getValue() {
         return this.value;
      }

      public OffHand.ItemStackWithSlot getItemStackWithSlot() {
         return this.itemStackWithSlot;
      }

      public AttributeWithValue(OffHand.AttributeType attributeType, double value, OffHand.ItemStackWithSlot itemStackWithSlot) {
         this.attributeType = attributeType;
         this.value = value;
         this.itemStackWithSlot = itemStackWithSlot;
      }
   }

   private class ItemStackWithSlot {
      private final ItemStack itemStack;
      private int slot;

      public ItemStack getItemStack() {
         return this.itemStack;
      }

      public int getSlot() {
         return this.slot;
      }

      public void setSlot(int slot) {
         this.slot = slot;
      }

      public ItemStackWithSlot(ItemStack itemStack, int slot) {
         this.itemStack = itemStack;
         this.slot = slot;
      }
   }
}
