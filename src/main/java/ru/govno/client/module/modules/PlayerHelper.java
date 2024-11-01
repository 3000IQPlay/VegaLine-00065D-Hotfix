package ru.govno.client.module.modules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import net.minecraft.block.Block;
import net.minecraft.block.BlockOre;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiBossOverlay;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemAir;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemEnderPearl;
import net.minecraft.item.ItemExpBottle;
import net.minecraft.item.ItemLingeringPotion;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemSplashPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.client.CPacketPlayerDigging.Action;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.BossInfo;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import ru.govno.client.event.EventTarget;
import ru.govno.client.event.events.EventSendPacket;
import ru.govno.client.module.Module;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.module.settings.FloatSettings;
import ru.govno.client.module.settings.ModeSettings;
import ru.govno.client.utils.InventoryUtil;
import ru.govno.client.utils.Wrapper;
import ru.govno.client.utils.Math.BlockUtils;
import ru.govno.client.utils.Math.MathUtils;
import ru.govno.client.utils.Math.TimerHelper;
import ru.govno.client.utils.Render.AnimationUtils;
import ru.govno.client.utils.Render.ColorUtils;
import ru.govno.client.utils.Render.RenderUtils;

public class PlayerHelper extends Module {
   public static PlayerHelper get;
   public BoolSettings AbsorptionFix;
   public BoolSettings PearlBlockThrow;
   public BoolSettings AutoTool;
   public BoolSettings ToolSwapsInInv;
   public BoolSettings SpeedMine;
   public BoolSettings MineFastBlockHit;
   public BoolSettings MineHaste;
   public BoolSettings PacketMineSilentSlot;
   public BoolSettings NoSlowingBreak;
   public BoolSettings NoBreakReset;
   public BoolSettings FastPlacing;
   public BoolSettings FastEXPThrow;
   public BoolSettings FastBlockPlace;
   public BoolSettings FastInteract;
   public BoolSettings FastPotionThrow;
   public BoolSettings NoPlaceMissing;
   public BoolSettings NoSurvivalCopyBlock;
   public BoolSettings AppleEatCooldown;
   public BoolSettings PearlThrowCooldown;
   public BoolSettings ChorusUseCooldown;
   public BoolSettings CooldownsCheckKT;
   public FloatSettings MineSkeepValue;
   public FloatSettings AppleTimeWait;
   public FloatSettings PearlTimeWait;
   public FloatSettings ChorusUseWait;
   public ModeSettings MineMode;
   public ModeSettings NoSlowBreakIf;
   public static TimerHelper timerApple = new TimerHelper();
   public static TimerHelper timerPearl = new TimerHelper();
   public static TimerHelper timerChorus = new TimerHelper();
   public static boolean checkApple;
   public static boolean checkPearl;
   public static boolean checkChorus;
   private float sizeApple = 0.0F;
   private float sizePearl = 0.0F;
   private float sizeChorus = 0.0F;
   boolean itemBacked = false;
   static int lastSlot = -1;
   public static BlockPos currentBlock;
   public static int ticks = 0;
   Vec3d lastSVPos = null;
   boolean lastGr = false;
   boolean isPearlSlot = false;
   ItemStack lastOfPreMine = null;
   static List<PlayerHelper.SlotsMemory> slotMemories = new ArrayList<>();
   public static BlockPos breakPos = null;
   boolean runPacket = false;
   float breakTicks = 0.0F;
   boolean isStartPacket;
   double progressPacket;
   int startSlot = -1;
   ItemStack tempStackPacketMine;
   boolean tempSetAndResetSlot;
   private final AnimationUtils alphaSelectPC = new AnimationUtils(0.0F, 0.0F, 0.1F);
   private final AnimationUtils progressingSelect = new AnimationUtils(0.0F, 0.0F, 0.115F);
   BlockPos posRender = breakPos;

   public PlayerHelper() {
      super("PlayerHelper", 0, Module.Category.PLAYER);
      get = this;
      this.settings.add(this.AbsorptionFix = new BoolSettings("AbsorptionFix", true, this));
      this.settings.add(this.PearlBlockThrow = new BoolSettings("PearlBlockThrow", true, this));
      this.settings.add(this.AutoTool = new BoolSettings("AutoTool", true, this));
      this.settings.add(this.ToolSwapsInInv = new BoolSettings("ToolSwapsInInv", false, this, () -> this.AutoTool.getBool()));
      this.settings.add(this.SpeedMine = new BoolSettings("SpeedMine", false, this));
      this.settings
         .add(this.MineMode = new ModeSettings("MineMode", "Matrix", this, new String[]{"Matrix", "Custom", "Packet"}, () -> this.SpeedMine.getBool()));
      this.settings
         .add(
            this.MineSkeepValue = new FloatSettings(
               "MineSkeepValue",
               0.5F,
               1.0F,
               0.0F,
               this,
               () -> (this.MineMode.currentMode.equalsIgnoreCase("Custom") || this.MineMode.currentMode.equalsIgnoreCase("Packet")) && this.SpeedMine.getBool()
            )
         );
      this.settings
         .add(
            this.MineFastBlockHit = new BoolSettings(
               "MineFastBlockHit", true, this, () -> this.MineMode.currentMode.equalsIgnoreCase("Custom") && this.SpeedMine.getBool()
            )
         );
      this.settings
         .add(
            this.MineHaste = new BoolSettings("MineHaste", true, this, () -> this.MineMode.currentMode.equalsIgnoreCase("Custom") && this.SpeedMine.getBool())
         );
      this.settings
         .add(
            this.PacketMineSilentSlot = new BoolSettings(
               "PacketMineSilentSlot",
               false,
               this,
               () -> this.MineMode.currentMode.equalsIgnoreCase("Packet") && this.SpeedMine.getBool() && this.AutoTool.getBool()
            )
         );
      this.settings.add(this.NoSlowingBreak = new BoolSettings("NoSlowingBreak", false, this));
      this.settings
         .add(
            this.NoSlowBreakIf = new ModeSettings(
               "NoSlowBreakIf", "InAir", this, new String[]{"Anywhere", "InAir", "InLiquid"}, () -> this.NoSlowingBreak.getBool()
            )
         );
      this.settings.add(this.NoBreakReset = new BoolSettings("NoBreakReset", false, this));
      this.settings.add(this.FastPlacing = new BoolSettings("FastPlacing", true, this));
      this.settings.add(this.FastEXPThrow = new BoolSettings("FastEXPThrow", true, this, () -> this.FastPlacing.getBool()));
      this.settings.add(this.FastBlockPlace = new BoolSettings("FastBlockPlace", true, this, () -> this.FastPlacing.getBool()));
      this.settings.add(this.FastInteract = new BoolSettings("FastInteract", false, this, () -> this.FastPlacing.getBool()));
      this.settings.add(this.FastPotionThrow = new BoolSettings("FastPotionThrow", true, this, () -> this.FastPlacing.getBool()));
      this.settings
         .add(
            this.NoPlaceMissing = new BoolSettings(
               "NoPlaceMissing",
               true,
               this,
               () -> this.FastPlacing.getBool()
                     && (this.FastEXPThrow.getBool() || this.FastBlockPlace.getBool() || this.FastInteract.getBool() || this.FastPotionThrow.getBool())
            )
         );
      this.settings.add(this.NoSurvivalCopyBlock = new BoolSettings("NoSurvivalCopyBlock", true, this));
      this.settings.add(this.AppleEatCooldown = new BoolSettings("AppleEatCooldown", false, this));
      this.settings.add(this.AppleTimeWait = new FloatSettings("AppleTimeWait", 2300.0F, 10000.0F, 100.0F, this, () -> this.AppleEatCooldown.getBool()));
      this.settings.add(this.PearlThrowCooldown = new BoolSettings("PearlThrowCooldown", false, this));
      this.settings.add(this.PearlTimeWait = new FloatSettings("PearlTimeWait", 14000.0F, 30000.0F, 500.0F, this, () -> this.PearlThrowCooldown.getBool()));
      this.settings.add(this.ChorusUseCooldown = new BoolSettings("ChorusUseCooldown", false, this));
      this.settings.add(this.ChorusUseWait = new FloatSettings("ChorusUseWait", 1500.0F, 30000.0F, 1000.0F, this, () -> this.ChorusUseCooldown.getBool()));
      this.settings
         .add(
            this.CooldownsCheckKT = new BoolSettings(
               "CooldownsCheckKT", true, this, () -> this.AppleEatCooldown.getBool() || this.PearlThrowCooldown.getBool() || this.ChorusUseCooldown.getBool()
            )
         );
   }

   @Override
   public void onToggled(boolean actived) {
      if (!actived && this.SpeedMine.getBool() && this.MineMode.currentMode.equalsIgnoreCase("Custom")) {
         Minecraft.player.removeActivePotionEffect(Potion.getPotionById(3));
      }

      ticks = 0;
      super.onToggled(actived);
   }

   @EventTarget
   public void onPacket(EventSendPacket event) {
      if (this.actived && event.getPacket() instanceof CPacketPlayerTryUseItemOnBlock packet && this.PearlBlockThrow.getBool()) {
         if (Minecraft.player != null && Minecraft.player.inventory.getCurrentItem().getItem() instanceof ItemEnderPearl && ticks == 0) {
            ticks = 10;
            event.cancel();
         }

         if (ticks > 0) {
            ticks--;
         }
      }
   }

   @Override
   public void onUpdate() {
      if (this.AbsorptionFix.getBool()
         && Minecraft.player != null
         && !Minecraft.player.isPotionActive(MobEffects.ABSORPTION)
         && !Minecraft.player.isPotionActive(MobEffects.REGENERATION)
         && Minecraft.player.getAbsorptionAmount() > 0.0F) {
         Minecraft.player.setAbsorptionAmount(0.0F);
      }

      if (checkPearl || checkApple || checkChorus) {
         if (!canCooldown() || !this.AppleEatCooldown.getBool() && !this.PearlThrowCooldown.getBool() && !this.ChorusUseCooldown.getBool()) {
            checkApple = false;
            checkPearl = false;
            checkChorus = false;
         }

         float timeApple = this.getAppleTime();
         float timePearl = this.getPearlTime();
         float timeChorus = this.getPearlTime();
         if (checkPearl && timerPearl.hasReached((double)timePearl)) {
            checkPearl = false;
         }

         if (checkApple && timerApple.hasReached((double)timeApple)) {
            checkApple = false;
         }

         if (checkChorus && timerChorus.hasReached((double)timeChorus)) {
            checkChorus = false;
         }
      }

      if (ticks != 0 && !(Minecraft.player.inventory.getCurrentItem().getItem() instanceof ItemEnderPearl)) {
         ticks = 0;
      }

      if (this.FastPlacing.getBool()) {
         boolean exp = this.FastEXPThrow.getBool() && Minecraft.player.inventory.getCurrentItem().getItem() instanceof ItemExpBottle;
         boolean pot = this.FastPotionThrow.getBool()
            && (
               Minecraft.player.inventory.getCurrentItem().getItem() instanceof ItemSplashPotion
                  || Minecraft.player.inventory.getCurrentItem().getItem() instanceof ItemLingeringPotion
                  || Minecraft.player.inventory.getCurrentItem().getItem() == Items.GLASS_BOTTLE
            );
         boolean interact = false;
         if (mc.objectMouseOver != null && mc.objectMouseOver.getBlockPos() != null) {
            Block block = mc.world.getBlockState(mc.objectMouseOver.getBlockPos()).getBlock();
            interact = this.FastInteract.getBool()
               && !Minecraft.player.isSneaking()
               && mc.objectMouseOver != null
               && mc.objectMouseOver.getBlockPos() != null
               && block != null
               && Arrays.asList(
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
                     404,
                     379,
                     323
                  )
                  .stream()
                  .noneMatch(id -> Block.getIdFromBlock(block) == id);
         }

         boolean blocks = this.FastBlockPlace.getBool()
            && Minecraft.player.inventory.getCurrentItem().getItem() instanceof ItemBlock
            && (!interact || Minecraft.player.isSneaking());
         boolean fastPlace = blocks || exp || interact || pot;
         if (fastPlace && (Minecraft.player.ticksExisted % 2 == 1 || !this.NoPlaceMissing.getBool())) {
            mc.rightClickDelayTimer = this.NoPlaceMissing.getBool() ? 1 : 0;
         }
      }

      if (this.AutoTool.getBool()) {
         boolean packetMine = breakPos != null && (!this.PacketMineSilentSlot.getBool() || this.isStartPacket || this.tempSetAndResetSlot);
         this.tempSetAndResetSlot = false;
         boolean isBreak = Mouse.isButtonDown(0) && (!this.SpeedMine.getBool() || !this.MineMode.currentMode.equalsIgnoreCase("Packet"))
            || currentBlock != null
            || packetMine;
         if (packetMine && breakPos != null) {
            currentBlock = breakPos;
         }

         if (isBreak) {
            this.itemBacked = false;
            int bestSlot = -228;
            double max = 0.0;
            if (currentBlock != null) {
               for (int i = this.ToolSwapsInInv.getBool() ? 36 : 8; i > 0; i--) {
                  ItemStack stack = Minecraft.player.inventory.getStackInSlot(i);
                  if (!stack.isEmpty()) {
                     float speed = stack.getStrVsBlock(mc.world.getBlockState(currentBlock));
                     if (speed > 1.0F) {
                        int eff;
                        speed = (float)(
                           (double)speed
                              + ((eff = EnchantmentHelper.getEnchantmentLevel(Enchantments.EFFICIENCY, stack)) > 0 ? Math.pow((double)eff, 2.0) + 1.0 : 0.0)
                        );
                        if ((double)speed > max) {
                           max = (double)speed;
                           bestSlot = i;
                        }

                        if (mc.world.getBlockState(currentBlock).getBlock() == Blocks.WEB && InventoryUtil.getItemInHotbar(Items.SHEARS) != -1) {
                           bestSlot = InventoryUtil.getItemInHotbar(Items.SHEARS);
                        }
                     }
                  }
               }
            }

            if (bestSlot != -228) {
               this.tempStackPacketMine = Minecraft.player.inventory.getStackInSlot(bestSlot);
               equip(bestSlot, false);
               if (bestSlot > 8) {
                  lastSlot = bestSlot;
               }
            }
         } else {
            if (!this.itemBacked && lastSlot != -1) {
               int lastOfPreMineSlot = -1;

               for (int ix = 0; ix < 36; ix++) {
                  if (Minecraft.player.inventory.getStackInSlot(ix) == this.lastOfPreMine) {
                     lastOfPreMineSlot = ix;
                  }
               }

               if (lastOfPreMineSlot != -1) {
                  lastSlot = lastOfPreMineSlot;
               }

               equip(lastSlot, true);
               this.itemBacked = true;
            }

            if (!this.PacketMineSilentSlot.getBool()) {
               this.tempStackPacketMine = null;
            }

            lastSlot = Minecraft.player.inventory.currentItem;
            this.lastOfPreMine = Minecraft.player.inventory.getCurrentItem();
         }

         currentBlock = null;
      }

      if (this.SpeedMine.getBool()) {
         if (this.MineMode.currentMode.equalsIgnoreCase("Custom")) {
            if (this.MineHaste.getBool()) {
               if (mc.playerController.getIsHittingBlock()) {
                  Minecraft.player.addPotionEffect(new PotionEffect(MobEffects.HASTE, 1000, 1));
               } else {
                  Wrapper.getPlayer().removeActivePotionEffect(Potion.getPotionById(3));
               }
            }

            if (this.MineFastBlockHit.getBool()) {
               mc.playerController.blockHitDelay = 0;
            }

            if (mc.playerController.curBlockDamageMP >= 1.0F - this.MineSkeepValue.getFloat()) {
               mc.playerController.curBlockDamageMP = 1.0F;
            }
         } else if (this.MineMode.currentMode.equalsIgnoreCase("Matrix")) {
            this.matrixBreak();
         } else if (this.MineMode.currentMode.equalsIgnoreCase("Packet")) {
            if (Mouse.isButtonDown(0) && mc.playerController.isHittingBlock && mc.objectMouseOver != null && mc.objectMouseOver.entityHit == null) {
               BlockPos pos = mc.objectMouseOver.getBlockPos();
               this.runPacketBreak(pos);
            }

            this.packetMine(this.MineSkeepValue.getFloat());
         }
      }
   }

   void runPacketBreak(BlockPos pos) {
      this.breakTicks = 0.0F;
      breakPos = pos;
      this.runPacket = true;
   }

   EnumFacing getFacing(BlockPos pos) {
      for (EnumFacing facing : EnumFacing.values()) {
         RayTraceResult rayTraceResult = mc.world
            .rayTraceBlocks(
               new Vec3d(this.getMe().posX, this.getMe().posY + (double)this.getMe().getEyeHeight(), this.getMe().posZ),
               new Vec3d(
                  (double)pos.getX() + 0.5 + (double)facing.getDirectionVec().getX() / 2.0,
                  (double)pos.getY() + 0.5 + (double)facing.getDirectionVec().getY() / 2.0,
                  (double)pos.getZ() + 0.5 + (double)facing.getDirectionVec().getZ() / 2.0
               ),
               false,
               true,
               false
            );
         if (rayTraceResult == null || rayTraceResult.typeOfHit == Type.BLOCK && rayTraceResult.getBlockPos().equals(pos)) {
            return facing;
         }
      }

      return (double)pos.getY() > this.getMe().posY + (double)this.getMe().getEyeHeight() ? EnumFacing.DOWN : EnumFacing.UP;
   }

   public float blockBreakSpeed(IBlockState blockMaterial, ItemStack tool) {
      float mineSpeed = tool.getStrVsBlock(blockMaterial);
      int efficiencyFactor = EnchantmentHelper.getEnchantmentLevel(Enchantments.EFFICIENCY, tool);
      mineSpeed = (float)(
         (double)mineSpeed > 1.0 && efficiencyFactor > 0 ? (double)((float)(efficiencyFactor * efficiencyFactor) + mineSpeed) + 1.0 : (double)mineSpeed
      );
      if (Minecraft.player.getActivePotionEffect(MobEffects.HASTE) != null) {
         mineSpeed *= 1.0F + (float)Objects.requireNonNull(Minecraft.player.getActivePotionEffect(MobEffects.HASTE)).getAmplifier() * 0.2F;
      }

      return mineSpeed;
   }

   public double blockBrokenTime(BlockPos pos, ItemStack tool) {
      if (pos != null && tool != null) {
         IBlockState state = mc.world.getBlockState(pos);
         float damageTicks = this.blockBreakSpeed(state, tool) / state.getBlockHardness(mc.world, pos) / 30.0F;
         return Math.ceil(1.0 / (double)damageTicks) * 50.0;
      } else {
         return 0.0;
      }
   }

   private EntityPlayer getMe() {
      return (EntityPlayer)(FreeCam.fakePlayer != null ? FreeCam.fakePlayer : Minecraft.player);
   }

   double getProggress(double ms, BlockPos pos, ItemStack stack) {
      return MathUtils.clamp(ms / this.blockBrokenTime(pos, stack), 0.0, 1.0);
   }

   double getProggress(double ms, double brokenTime) {
      return MathUtils.clamp(ms / brokenTime, 0.0, 1.0);
   }

   ItemStack getBestStack(BlockPos pos, boolean invUse) {
      int bestSlot = -1;
      if (pos == null) {
         return Minecraft.player.inventory.getCurrentItem();
      } else {
         for (int i = invUse ? 36 : 8; i > 0; i--) {
            ItemStack stack = Minecraft.player.inventory.getStackInSlot(i);
            double max = 0.0;
            if (!stack.isEmpty()) {
               float speed = stack.getStrVsBlock(mc.world.getBlockState(pos));
               if (speed > 1.0F) {
                  int eff;
                  speed = (float)(
                     (double)speed
                        + ((eff = EnchantmentHelper.getEnchantmentLevel(Enchantments.EFFICIENCY, stack)) > 0 ? Math.pow((double)eff, 2.0) + 1.0 : 0.0)
                  );
                  if ((double)speed > max) {
                     max = (double)speed;
                     bestSlot = i;
                  }

                  if (mc.world.getBlockState(pos).getBlock() == Blocks.WEB && InventoryUtil.getItemInHotbar(Items.SHEARS) != -1) {
                     bestSlot = InventoryUtil.getItemInHotbar(Items.SHEARS);
                  }
               }
            }
         }

         return bestSlot >= 0 && bestSlot <= (invUse ? 36 : 8)
            ? Minecraft.player.inventory.getStackInSlot(bestSlot)
            : Minecraft.player.inventory.getCurrentItem();
      }
   }

   int getSlotForStack(ItemStack stack, boolean invIse) {
      for (int i = invIse ? 36 : 8; i > 1; i--) {
         if (Minecraft.player.inventory.getStackInSlot(i) == stack) {
            return i;
         }
      }

      return -1;
   }

   void packetMine(float skipProgress) {
      if (breakPos != null) {
         mc.playerController.curBlockDamageMP = 0.0F;
         mc.playerController.isHittingBlock = false;
         if (Minecraft.player.isSneaking() && !breakPos.equals(BlockUtils.getEntityBlockPos(this.getMe()).down())
            || Minecraft.player.getDistanceAtEye((double)breakPos.getX() + 0.5, (double)breakPos.getY() + 0.5, (double)breakPos.getZ() + 0.5) > 5.5) {
            this.isStartPacket = false;
            mc.getConnection().sendPacket(new CPacketPlayerDigging(Action.ABORT_DESTROY_BLOCK, breakPos, EnumFacing.DOWN));
            breakPos = null;
            return;
         }

         mc.gameSettings.keyBindAttack.pressed = false;
         if (mc.playerController.isHittingBlock) {
            this.breakTicks = 0.0F;
            this.isStartPacket = true;
         }

         ItemStack stack = this.tempStackPacketMine != null ? this.tempStackPacketMine : Minecraft.player.getHeldItemMainhand();
         double brokenTime = (this.blockBrokenTime(breakPos, stack) + 100.0) / 50.0;
         double progress = this.progressPacket = this.getProggress((double)this.breakTicks, brokenTime);
         float slowerMine = 1.0F;
         if (!this.NoSlowingBreak.getBool() || !this.NoSlowBreakIf.currentMode.equalsIgnoreCase("Anywhere")) {
            if (Minecraft.player.isInsideOfMaterial(Material.WATER)
               && !EnchantmentHelper.getAquaAffinityModifier(Minecraft.player)
               && (
                  !this.NoSlowingBreak.getBool()
                     || !this.NoSlowBreakIf.currentMode.equalsIgnoreCase("Anywhere") && !this.NoSlowBreakIf.currentMode.equalsIgnoreCase("InLiquid")
               )) {
               slowerMine /= 5.0F;
            }

            if (!Minecraft.player.onGround
               && (
                  !this.NoSlowingBreak.getBool()
                     || !this.NoSlowBreakIf.currentMode.equalsIgnoreCase("Anywhere") && !this.NoSlowBreakIf.currentMode.equalsIgnoreCase("InAir")
               )) {
               slowerMine /= 5.0F;
            }
         }

         this.breakTicks += slowerMine;
         if (skipProgress > 0.0F
            && (skipProgress > 1.0F ? (skipProgress = 1.0F) : skipProgress) <= 1.0F
            && (double)this.breakTicks / brokenTime < (double)skipProgress) {
            this.breakTicks = (float)(brokenTime * (double)skipProgress);
         }

         if (this.isStartPacket) {
            this.startSlot = Minecraft.player.inventory.currentItem;
            EnumFacing face = EnumFacing.UP;
            if (mc.objectMouseOver != null && mc.objectMouseOver.sideHit != null) {
               face = mc.objectMouseOver.sideHit;
            }

            mc.getConnection().sendPacket(new CPacketPlayerDigging(Action.START_DESTROY_BLOCK, breakPos, face));
            this.isStartPacket = false;
         }

         if (!this.tempSetAndResetSlot) {
            double predictProgress = this.getProggress((double)(this.breakTicks + slowerMine), brokenTime);
            if (predictProgress == 1.0) {
               this.tempSetAndResetSlot = true;
            }
         }

         if (progress == 1.0) {
            EnumFacing face = EnumFacing.UP;
            if (mc.objectMouseOver != null && mc.objectMouseOver.sideHit != null) {
               face = mc.objectMouseOver.sideHit;
            }

            mc.getConnection().sendPacket(new CPacketPlayerDigging(Action.STOP_DESTROY_BLOCK, breakPos, face));
            Minecraft.player.swingArm();
            breakPos = null;
            this.tempStackPacketMine = null;
         } else {
            mc.getConnection().sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));
         }
      } else {
         mc.gameSettings.keyBindAttack.pressed = Mouse.isButtonDown(0) && mc.currentScreen == null;
         this.progressPacket = 0.0;
         this.breakTicks = 0.0F;
         this.isStartPacket = true;
         this.tempSetAndResetSlot = false;
      }
   }

   private void drawBlockPos(AxisAlignedBB aabb, int color) {
      int c1 = ColorUtils.swapAlpha(color, (float)ColorUtils.getAlphaFromColor(color) / 2.6F);
      int c2 = ColorUtils.swapAlpha(color, (float)ColorUtils.getAlphaFromColor(color) / 7.2F);
      int c3 = ColorUtils.swapAlpha(color, (float)ColorUtils.getAlphaFromColor(color) / 15.0F);
      RenderUtils.drawCanisterBox(aabb, true, true, true, c1, c2, c3);
   }

   private AxisAlignedBB getAxisExtended(BlockPos pos, AxisAlignedBB aabb, float percent, Vec3d smoothVec) {
      double x1 = aabb.minX - (double)pos.getX() + smoothVec.xCoord;
      double y1 = aabb.minY - (double)pos.getY() + smoothVec.yCoord;
      double z1 = aabb.minZ - (double)pos.getZ() + smoothVec.zCoord;
      double x2 = aabb.maxX - (double)pos.getX() + smoothVec.xCoord;
      double y2 = aabb.maxY - (double)pos.getY() + smoothVec.yCoord;
      double z2 = aabb.maxZ - (double)pos.getZ() + smoothVec.zCoord;
      double XW = x2 - x1;
      double ZW = z2 - z1;
      double YH = y2 - y1;
      Vec3d centerOFAABBox = new Vec3d(x1 + XW / 2.0, y1 + YH / 2.0, z1 + ZW / 2.0);
      AxisAlignedBB finallyAABB = new AxisAlignedBB(
         centerOFAABBox.xCoord - XW / 2.0 * (double)percent,
         centerOFAABBox.yCoord - YH / 2.0 * (double)percent,
         centerOFAABBox.zCoord - ZW / 2.0 * (double)percent,
         centerOFAABBox.xCoord + XW / 2.0 * (double)percent,
         centerOFAABBox.yCoord + YH / 2.0 * (double)percent,
         centerOFAABBox.zCoord + ZW / 2.0 * (double)percent
      );
      return finallyAABB == null ? aabb : finallyAABB;
   }

   @Override
   public void alwaysRender3D() {
      if (this.actived) {
         if (breakPos != null) {
            this.posRender = breakPos;
         }

         int color = ClientColors.getColor1();
         boolean isHitting = this.runPacket && breakPos != null;
         float progressUpdated01 = (float)this.progressPacket;
         this.alphaSelectPC.to = isHitting ? 1.0F : 0.0F;
         this.alphaSelectPC.speed = isHitting ? 0.1F : 0.05F;
         float alphaIn = this.alphaSelectPC.getAnim();
         this.progressingSelect.to = MathUtils.clamp(progressUpdated01 * 1.05F, 0.0F, 1.0F);
         this.progressingSelect.speed = isHitting ? 0.1F : 0.145F;
         if ((double)alphaIn > 0.01) {
            RenderUtils.setup3dForBlockPos(
               () -> this.drawBlockPos(
                     this.getAxisExtended(
                        this.posRender,
                        mc.world.getBlockState(this.posRender).getSelectedBoundingBox(mc.world, this.posRender),
                        this.progressingSelect.getAnim(),
                        new Vec3d((double)this.posRender.getX(), (double)this.posRender.getY(), (double)this.posRender.getZ())
                     ),
                     ColorUtils.swapAlpha(color, MathUtils.clamp((float)ColorUtils.getAlphaFromColor(color) * alphaIn * 2.0F, 0.0F, 255.0F))
                  ),
               true
            );
         }
      }
   }

   private ItemStack currentStack() {
      return Minecraft.player.inventory.getCurrentItem();
   }

   private Item currentItem() {
      return this.currentStack().getItem();
   }

   private int getEff(ItemStack stack) {
      return EnchantmentHelper.getEnchantmentLevel(Enchantments.EFFICIENCY, stack);
   }

   private int stackEff() {
      return this.currentStack() != null && !(this.currentStack().getItem() instanceof ItemAir) ? this.getEff(this.currentStack()) : 0;
   }

   private Material blockMaterial(BlockPos pos) {
      return mc.world.getBlockState(pos).getMaterial();
   }

   private void matrixBreak() {
      if (mc.objectMouseOver != null) {
         if (mc.pointedEntity == null && mc.objectMouseOver.getBlockPos() != null) {
            this.matrix(mc.objectMouseOver.getBlockPos());
         }
      }
   }

   private void matrix(BlockPos pos) {
      IBlockState state = mc.world.getBlockState(pos);
      Block block = state.getBlock();
      Material material = this.blockMaterial(pos);
      Item item = this.currentItem();
      int eff = this.stackEff();
      if (Minecraft.player.onGround && !Minecraft.player.isInWater() && !Minecraft.player.isInWeb && !Minecraft.player.isInLava()) {
         boolean var17 = false;
      } else {
         boolean var10000 = true;
      }

      float skeep = 0.0F;
      int delay = mc.playerController.blockHitDelay;
      if (material != null && material == Material.ROCK && item instanceof ItemPickaxe) {
         String type = ((ItemPickaxe)item).getToolMaterialName();
         boolean isOre = block instanceof BlockOre;
         if (type.equalsIgnoreCase("DIAMOND") && !isOre) {
            delay = 0;
            switch (eff) {
               case 0:
                  skeep = 0.1F;
               case 1:
                  skeep = 0.2F;
               case 2:
                  skeep = 0.3F;
               case 3:
                  skeep = 0.4F;
               case 4:
                  skeep = 0.9F;
               default:
                  skeep = 0.0F;
            }
         }
      }

      if (mc.playerController.curBlockDamageMP >= 1.0F - MathUtils.clamp(skeep, 0.0F, 1.0F)) {
         mc.playerController.curBlockDamageMP = 1.0F;
      }

      if (mc.playerController.blockHitDelay != delay) {
         mc.playerController.blockHitDelay = delay;
      }
   }

   private static void equip(int slot, boolean back) {
      if (slot > 8) {
         if (!back) {
            slotMemories.add(new PlayerHelper.SlotsMemory(slot, lastSlot));
         }

         slotMemories.forEach(Islot -> mc.playerController.windowClick(0, slot, Islot.slotTo, ClickType.SWAP, Minecraft.player));
         if (back) {
            slotMemories.clear();
         }
      } else {
         Minecraft.player.inventory.currentItem = slot;
         mc.playerController.syncCurrentPlayItem();
      }
   }

   float getAppleTime() {
      return this.AppleTimeWait.getFloat();
   }

   float getPearlTime() {
      return this.PearlTimeWait.getFloat();
   }

   float getChorusTime() {
      return this.ChorusUseWait.getFloat();
   }

   public static boolean canCooldown() {
      return get.actived
         && (
            !get.CooldownsCheckKT.getBool()
               || !GuiBossOverlay.mapBossInfos2.isEmpty()
                  && GuiBossOverlay.mapBossInfos2
                        .values()
                        .stream()
                        .map(BossInfo::getName)
                        .<String>map(ITextComponent::getFormattedText)
                        .map(String::toLowerCase)
                        .filter(name -> name.contains("pvp") || name.contains("пвп") || name.contains("сек."))
                        .filter(Objects::nonNull)
                        .toList()
                        .size()
                     != 0
               || mc.world.getScoreboard() != null
                  && mc.world
                     .getScoreboard()
                     .getTeamNames()
                     .stream()
                     .map(String::toLowerCase)
                     .anyMatch(
                        str -> List.of("терка", "боя", "противник", "пвп", "pvp", "режим").stream().map(String::toLowerCase).anyMatch(bad -> str.contains(bad))
                     )
         );
   }

   public static void trackApple() {
      checkApple = true;
      timerApple.reset();
   }

   public static void trackPearl() {
      checkPearl = true;
      timerPearl.reset();
   }

   public static void trackChorus() {
      checkChorus = true;
      timerChorus.reset();
   }

   @Override
   public void onRender2D(ScaledResolution sr) {
      if (checkApple || checkPearl || checkChorus || (double)this.sizeApple > 0.03 || (double)this.sizePearl > 0.03 || (double)this.sizeChorus > 0.03) {
         int curApple = checkApple ? 1 : 0;
         if (this.sizeApple != (float)curApple) {
            this.sizeApple = MathUtils.harp(this.sizeApple, (float)curApple, (float)Minecraft.frameTime * 0.0075F);
         }

         int curPearl = checkPearl ? 1 : 0;
         if (this.sizePearl != (float)curPearl) {
            this.sizePearl = MathUtils.harp(this.sizePearl, (float)curPearl, (float)Minecraft.frameTime * 0.0075F);
         }

         int curChorus = checkChorus ? 1 : 0;
         if (this.sizeChorus != (float)curChorus) {
            this.sizeChorus = MathUtils.harp(this.sizeChorus, (float)curChorus, (float)Minecraft.frameTime * 0.0075F);
         }

         float r = 10.0F;
         float x = (float)sr.getScaledWidth() / 2.0F - (r + 4.0F) * (this.sizeApple + this.sizePearl + this.sizeChorus);
         float y = (float)sr.getScaledHeight() - 130.0F;
         float y2 = y - (r + 4.0F) - this.sizeChorus * r;
         float timeApple = this.getAppleTime();
         float timePearl = this.getPearlTime();
         float timeChorus = this.getChorusTime();
         if ((double)this.sizeApple > 0.025) {
            x += (r + 4.0F) * (1.0F + this.sizeApple) / 2.0F;
            GL11.glPushMatrix();
            RenderUtils.customScaledObject2D(x, y, 0.0F, 0.0F, this.sizeApple);
            RenderUtils.drawSmoothCircle((double)x, (double)y, (r + 2.0F) * this.sizeApple, ColorUtils.getColor(60, 60, 60, 255.0F * this.sizeApple));
            RenderUtils.drawSmoothCircle((double)x, (double)y, r * this.sizeApple, ColorUtils.getColor(11, 11, 11, 190.0F * this.sizeApple));
            RenderUtils.drawClientCircle(x, (double)y, r + 1.25F, 362.0F - (float)timerApple.getTime() / (timeApple / 360.0F), 4.0F);
            RenderUtils.customScaledObject2D(x - 8.0F, y - 8.0F, 16.0F, 16.0F, 1.0F + this.sizeApple / 128.0F);
            RenderUtils.renderItem(new ItemStack(Items.GOLDEN_APPLE), x - 8.0F, y - 8.0F);
            GL11.glPopMatrix();
         }

         if ((double)this.sizePearl > 0.025) {
            x += (r + 4.0F) * (1.0F + this.sizePearl) * (0.5F + this.sizeApple * 0.5F);
            GL11.glPushMatrix();
            RenderUtils.customScaledObject2D(x, y, 0.0F, 0.0F, this.sizePearl);
            RenderUtils.drawSmoothCircle((double)x, (double)y, (r + 2.0F) * this.sizePearl, ColorUtils.getColor(60, 60, 60, 255.0F * this.sizePearl));
            RenderUtils.drawSmoothCircle((double)x, (double)y, r * this.sizePearl, ColorUtils.getColor(11, 11, 11, 190.0F * this.sizePearl));
            RenderUtils.drawClientCircle(x, (double)y, r + 1.25F, 362.0F - (float)timerPearl.getTime() / (timePearl / 360.0F), 4.0F);
            RenderUtils.customScaledObject2D(x - 8.0F, y - 8.0F, 16.0F, 16.0F, 1.0F + this.sizePearl / 128.0F);
            RenderUtils.renderItem(new ItemStack(Items.ENDER_PEARL), x - 8.0F, y - 8.0F);
            GL11.glPopMatrix();
         }

         if ((double)this.sizeChorus > 0.025) {
            x += (r + 4.0F) * (1.0F + this.sizeChorus) * (0.5F + this.sizeApple * (1.0F - this.sizePearl) * 0.5F + this.sizePearl * 0.5F);
            GL11.glPushMatrix();
            RenderUtils.customScaledObject2D(x, y, 0.0F, 0.0F, this.sizeChorus);
            RenderUtils.drawSmoothCircle((double)x, (double)y, (r + 2.0F) * this.sizeChorus, ColorUtils.getColor(60, 60, 60, 255.0F * this.sizeChorus));
            RenderUtils.drawSmoothCircle((double)x, (double)y, r * this.sizeChorus, ColorUtils.getColor(11, 11, 11, 190.0F * this.sizeChorus));
            RenderUtils.drawClientCircle(x, (double)y, r + 1.25F, 362.0F - (float)timerChorus.getTime() / (timeChorus / 360.0F), 4.0F);
            RenderUtils.customScaledObject2D(x - 8.0F, y - 8.0F, 16.0F, 16.0F, 1.0F + this.sizeChorus / 128.0F);
            RenderUtils.renderItem(new ItemStack(Items.CHORUS_FRUIT), x - 8.0F, y - 8.0F);
            GL11.glPopMatrix();
         }
      }
   }

   static class SlotsMemory {
      int slotAt;
      int slotTo;

      public SlotsMemory(int slotAt, int slotTo) {
         this.slotAt = slotAt;
         this.slotTo = slotTo;
      }
   }
}
