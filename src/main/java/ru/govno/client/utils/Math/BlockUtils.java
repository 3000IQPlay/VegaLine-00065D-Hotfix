package ru.govno.client.utils.Math;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemArmor;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class BlockUtils {
   private static final Minecraft mc = Minecraft.getMinecraft();

   public static BlockPos getOverallPos(BlockPos first, BlockPos second) {
      int x1 = first.getX();
      int y1 = first.getY();
      int z1 = first.getZ();
      int x2 = second.getX();
      int y2 = second.getY();
      int z2 = second.getZ();
      int diffX = x2 - x1;
      int diffY = y2 - y1;
      int diffZ = z2 - z1;
      return new BlockPos(x1 + diffX, y1 + diffY, z1 + diffZ);
   }

   public static Vec3d getOverallVec3d(Vec3d first, Vec3d second, float pc) {
      double x1 = first.xCoord;
      double y1 = first.yCoord;
      double z1 = first.zCoord;
      double x2 = second.xCoord;
      double y2 = second.yCoord;
      double z2 = second.zCoord;
      double diffX = x2 - x1;
      double diffY = y2 - y1;
      double diffZ = z2 - z1;
      return new Vec3d(x1 + diffX * (double)pc, y1 + diffY * (double)pc, z1 + diffZ * (double)pc);
   }

   public static final List<BlockPos> getSphere(Vec3d at, float radius) {
      List<BlockPos> posses = new CopyOnWriteArrayList<>();
      if (at != null) {
         for (int x = (int)(at.xCoord - (double)radius); x <= (int)(at.xCoord + (double)radius); x++) {
            for (int y = (int)(at.yCoord - (double)radius); y <= (int)(at.yCoord + (double)radius); y++) {
               for (int z = (int)(at.zCoord - (double)radius); z <= (int)(at.zCoord + (double)radius); z++) {
                  if (at.distanceTo(new Vec3d((double)x + 0.5, (double)y + 0.5, (double)z + 0.5)) <= (double)radius) {
                     posses.add(new BlockPos(x, y, z));
                  }
               }
            }
         }
      }

      return posses;
   }

   public static final List<BlockPos> getSphere(EntityLivingBase at, float radius) {
      return getSphere(at.getPositionEyes(1.0F), radius);
   }

   public static boolean canPlaceCrystal(BlockPos pos, boolean is1l13lplusVersoin) {
      Block block1 = mc.world.getBlockState(pos).getBlock();
      return (block1 == Blocks.OBSIDIAN || block1 == Blocks.BEDROCK) && mc.world.isAirBlock(pos.up()) && (is1l13lplusVersoin || mc.world.isAirBlock(pos.up(2)));
   }

   public static boolean getBlockWithExpand(double expand, BlockPos checkPos, Block block, boolean sideSetsMode) {
      if (block == null) {
         return false;
      } else {
         for (int xs = (int)((double)checkPos.getX() - expand); xs < (int)((double)checkPos.getX() + expand); xs++) {
            for (int zs = (int)((double)checkPos.getZ() - expand); zs < (int)((double)checkPos.getZ() + expand); zs++) {
               double dx = (double)(xs - checkPos.getX());
               double dz = (double)(zs - checkPos.getZ());
               if (!(Math.sqrt(dx * dx + dz * dz) > expand)) {
                  BlockPos checkedPos = new BlockPos(xs, checkPos.getY(), zs);
                  Block check = mc.world.getBlockState(checkedPos).getBlock();
                  if (check == block
                     && sideSetsMode
                     && (
                        blockMaterialIsCurrent(checkPos.down())
                           || blockMaterialIsCurrent(checkPos.down(2))
                           || blockMaterialIsCurrent(checkPos.down(3))
                           || blockMaterialIsCurrentWithSideSetsCount(checkPos, true) == 0
                     )) {
                     return true;
                  }
               }
            }
         }

         return false;
      }
   }

   public static boolean getBlockWithExpand(double expand, BlockPos checkPos, Block block) {
      return getBlockWithExpand(expand, checkPos, block, false);
   }

   public static final Material getBlockMaterial(BlockPos pos) {
      return mc.world.getBlockState(pos).getBlock().getMaterial(mc.world.getBlockState(pos));
   }

   public static boolean blockMaterialIsCurrent(BlockPos pos) {
      return !getBlockMaterial(pos).isReplaceable() && !getBlockMaterial(pos).isLiquid() && getBlockMaterial(pos).blocksMovement();
   }

   public static int blockMaterialIsCurrentWithSideSetsCount(BlockPos pos, boolean onlyXZCheck) {
      List<Boolean> ifHas = Arrays.asList(
         blockMaterialIsCurrent(pos.west()), blockMaterialIsCurrent(pos.east()), blockMaterialIsCurrent(pos.south()), blockMaterialIsCurrent(pos.north())
      );
      if (!onlyXZCheck) {
         ifHas.add(blockMaterialIsCurrent(pos.down()));
         ifHas.add(blockMaterialIsCurrent(pos.up()));
      }

      ifHas = ifHas.stream().filter(b -> b).collect(Collectors.toList());
      return ifHas.size();
   }

   public static boolean blockMaterialIsCurrentWithSideSets(BlockPos pos) {
      return blockMaterialIsCurrent(pos.west())
         || blockMaterialIsCurrent(pos.east())
         || blockMaterialIsCurrent(pos.south())
         || blockMaterialIsCurrent(pos.north())
         || blockMaterialIsCurrent(pos.down())
         || blockMaterialIsCurrent(pos.up());
   }

   public static boolean isOccupiedByEnt(BlockPos pos, float offsetXYZ, boolean ignoreCrystal) {
      return mc.world
         .getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos).expandXyz((double)offsetXYZ))
         .stream()
         .anyMatch(e -> ignoreCrystal || !(e instanceof EntityEnderCrystal));
   }

   public static boolean isOccupiedByEnt(BlockPos pos, boolean ignoreCrystal) {
      return isOccupiedByEnt(pos, 0.0F, ignoreCrystal);
   }

   public static boolean canPlaceObsidian(BlockPos pos, float range, boolean firstPosSpammer, boolean sideSetsMode) {
      boolean aired = getBlockMaterial(pos).isReplaceable() && getBlockMaterial(pos.up()).isReplaceable() && getBlockMaterial(pos.up().up()).isReplaceable();
      boolean neared = blockMaterialIsCurrentWithSideSets(pos);
      return aired
         && neared
         && !isOccupiedByEnt(pos.up(), firstPosSpammer)
         && (
            !getBlockWithExpand(2.0, pos, Blocks.OBSIDIAN, sideSetsMode) && !getBlockWithExpand(2.0, pos, Blocks.BEDROCK, sideSetsMode)
               || mc.world.getBlockState(pos).getBlock() == Blocks.OBSIDIAN
               || mc.world.getBlockState(pos).getBlock() == Blocks.BEDROCK
         );
   }

   public static BlockPos currentWithSideSetsPos(BlockPos pos) {
      if (blockMaterialIsCurrent(pos.east())) {
         return pos.east();
      } else if (blockMaterialIsCurrent(pos.west())) {
         return pos.west();
      } else if (blockMaterialIsCurrent(pos.south())) {
         return pos.south();
      } else if (blockMaterialIsCurrent(pos.north())) {
         return pos.north();
      } else {
         return blockMaterialIsCurrent(pos.down()) ? pos.down() : null;
      }
   }

   public static boolean canPlaceBlock(BlockPos pos) {
      boolean aired = getBlockMaterial(pos).isReplaceable();
      boolean neared = currentWithSideSetsPos(pos) != null;
      return aired && neared && !isOccupiedByEnt(pos, false);
   }

   public static final boolean canPosBeSeenCoord(BlockPos pos, double x, double y, double z) {
      return mc.world
            .rayTraceBlocks(
               new Vec3d((double)pos.getX() + 0.5, (double)(pos.getY() + 1), (double)pos.getZ() + 0.5), new Vec3d(x, y + 0.2, z), false, true, false
            )
         == null;
   }

   public static boolean isArmor(EntityPlayer entityIn, BlockUtils.armorElementSlot slot) {
      return entityIn.inventory.armorItemInSlot(slot.slot).getItem() instanceof ItemArmor;
   }

   public static Vec3d getEntityVec3dPos(Entity entity) {
      return entity == null ? Vec3d.ZERO : new Vec3d(entity.posX, entity.posY, entity.posZ);
   }

   public static int feetCrackPosesCount(EntityLivingBase target, boolean l1l13lplus) {
      int balls = 0;
      List<BlockPos> linears = new CopyOnWriteArrayList<>();
      BlockPos getEPOS = getEntityBlockPos(target);

      for (int i = 0; i < 3; i++) {
         linears.add(getEPOS.east(i));
         linears.add(getEPOS.west(i));
         linears.add(getEPOS.south(i));
         linears.add(getEPOS.north(i));
      }

      for (BlockPos pos : linears) {
         if (mc.world.isObsidOrBdBlock(pos.down()) && mc.world.isAirBlock(pos) && (l1l13lplus || mc.world.isAirBlock(pos.up()))) {
            balls++;
         }
      }

      return balls;
   }

   public static boolean canAttackFeetEntity(EntityLivingBase baseIn, boolean l1l13lplus) {
      return feetCrackPosesCount(baseIn, l1l13lplus) > 0;
   }

   public static boolean canPosBeSeenEntity(BlockPos pos, Entity entityIn, BlockUtils.bodyElement bodyElement) {
      double w = (double)(entityIn.width / 3.3F);
      double x = entityIn.posX;
      double y = entityIn.posY + (double)entityIn.height * bodyElement.height;
      double z = entityIn.posZ;
      return canPosBeSeenCoord(pos, x, y, z)
         || canPosBeSeenCoord(pos, x + w, y, z + w)
         || canPosBeSeenCoord(pos, x - w, y, z - w)
         || canPosBeSeenCoord(pos, x + w, y, z - w)
         || canPosBeSeenCoord(pos, x - w, y, z + w);
   }

   public static boolean canPosBeSeenEntityWithCustomVec(BlockPos pos, Entity entityIn, Vec3d entityVec, BlockUtils.bodyElement bodyElement) {
      double w = (double)(entityIn.width / 3.3F);
      double x = entityVec.xCoord;
      double y = entityVec.yCoord + (double)entityIn.height * bodyElement.height;
      double z = entityVec.zCoord;
      return canPosBeSeenCoord(pos, x, y, z)
         || canPosBeSeenCoord(pos, x + w, y, z + w)
         || canPosBeSeenCoord(pos, x - w, y, z - w)
         || canPosBeSeenCoord(pos, x + w, y, z - w)
         || canPosBeSeenCoord(pos, x - w, y, z + w);
   }

   public static double getDistanceAtPosToPos(BlockPos first, BlockPos second) {
      return Math.sqrt(
         (double)(
            (first.getX() - second.getX()) * (first.getX() - second.getX())
               + (first.getY() - second.getY()) * (first.getY() - second.getY())
               + (first.getZ() - second.getZ()) * (first.getZ() - second.getZ())
         )
      );
   }

   public static double getDistanceAtPosToVec(BlockPos first, Vec3d second) {
      return Math.sqrt(
         ((double)first.getX() - second.xCoord) * ((double)first.getX() - second.xCoord)
            + ((double)first.getY() - second.yCoord) * ((double)first.getY() - second.yCoord)
            + ((double)first.getZ() - second.zCoord) * ((double)first.getZ() - second.zCoord)
      );
   }

   public static final double getDistanceAtVecToVec(Vec3d first, Vec3d second) {
      return Math.sqrt(
         (first.xCoord - second.xCoord) * (first.xCoord - second.xCoord)
            + (first.yCoord - second.yCoord) * (first.yCoord - second.yCoord)
            + (first.zCoord - second.zCoord) * (first.zCoord - second.zCoord)
      );
   }

   public static BlockUtils.armorElementSlot armorElementByInt(int i) {
      return i == 0
         ? BlockUtils.armorElementSlot.FEET
         : (i == 1 ? BlockUtils.armorElementSlot.LEGS : (i == 2 ? BlockUtils.armorElementSlot.CHEST : BlockUtils.armorElementSlot.HEAD));
   }

   public static boolean canPosBeSeenCoord(Vec3d pos, double x, double y, double z) {
      return Minecraft.getMinecraft().world.rayTraceBlocks(new Vec3d(pos.xCoord, pos.yCoord + 1.0, pos.zCoord), new Vec3d(x, y, z), false, true, false) == null;
   }

   public static boolean canPosBeSeenEntity(Vec3d pos, Entity entityIn, BlockUtils.bodyElement bodyElement) {
      double w = (double)(entityIn.width / 2.0F);
      double x = entityIn.posX;
      double y = entityIn.posY + (double)entityIn.height * bodyElement.height;
      double z = entityIn.posZ;
      return canPosBeSeenCoord(pos, x, y, z)
         || canPosBeSeenCoord(pos, x + w, y, z + w)
         || canPosBeSeenCoord(pos, x - w, y, z - w)
         || canPosBeSeenCoord(pos, x + w, y, z - w)
         || canPosBeSeenCoord(pos, x - w, y, z + w);
   }

   public static boolean canPosBeSeenEntityWithCustomVec(Vec3d pos, Entity entityIn, Vec3d entityVirtPos, BlockUtils.bodyElement bodyElement) {
      double w = (double)(entityIn.width / 2.0F);
      double x = entityVirtPos.xCoord;
      double y = entityVirtPos.yCoord + (double)entityIn.height * bodyElement.height;
      double z = entityVirtPos.zCoord;
      return canPosBeSeenCoord(pos, x, y, z)
         || canPosBeSeenCoord(pos, x + w, y, z + w)
         || canPosBeSeenCoord(pos, x - w, y, z - w)
         || canPosBeSeenCoord(pos, x + w, y, z - w)
         || canPosBeSeenCoord(pos, x - w, y, z + w);
   }

   public static EnumFacing getPlaceableSide(BlockPos pos) {
      for (EnumFacing facing : new EnumFacing[]{EnumFacing.EAST, EnumFacing.WEST, EnumFacing.SOUTH, EnumFacing.NORTH, EnumFacing.UP, EnumFacing.DOWN}) {
         BlockPos offset = pos.offset(facing);
         if (mc.world.getBlockState(offset).getBlock().canCollideCheck(mc.world.getBlockState(offset), false)) {
            IBlockState state = mc.world.getBlockState(offset);
            if (!state.getMaterial().isReplaceable()) {
               return facing;
            }
         }
      }

      return null;
   }

   public static EnumFacing getPlaceableSideSeen(BlockPos pos, Entity self) {
      for (EnumFacing facing : new EnumFacing[]{EnumFacing.EAST, EnumFacing.WEST, EnumFacing.SOUTH, EnumFacing.NORTH, EnumFacing.UP, EnumFacing.DOWN}) {
         BlockPos offset = pos.offset(facing);
         if (mc.world.getBlockState(offset).getBlock().canCollideCheck(mc.world.getBlockState(offset), false)) {
            IBlockState state = mc.world.getBlockState(offset);
            if (!state.getMaterial().isReplaceable()) {
               Vec3d placeFaceVec = new Vec3d(pos)
                  .addVector(0.5, 0.5, 0.5)
                  .addVector((double)facing.getFrontOffsetX() * 0.5, (double)facing.getFrontOffsetY() * 0.5, (double)facing.getFrontOffsetZ() * 0.5);
               if (self.canEntityBeSeenVec3d(placeFaceVec)) {
                  return facing;
               }
            }
         }
      }

      return null;
   }

   public static BlockPos getEntityBlockPos(Entity entity) {
      return entity == null ? BlockPos.ORIGIN : new BlockPos(entity.posX, entity.posY, entity.posZ);
   }

   public static enum armorElementSlot {
      HEAD(3),
      CHEST(2),
      LEGS(1),
      FEET(0);

      public int slot;

      private armorElementSlot(int slot) {
         this.slot = slot;
      }
   }

   public static enum bodyElement {
      HEAD(0.9),
      CHEST(0.65),
      LEGS(0.35),
      FEET(0.15);

      public double height;

      private bodyElement(double height) {
         this.height = height;
      }
   }
}
