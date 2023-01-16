package dev.cuican.staypro.utils.tool;

import com.google.common.collect.ImmutableMap;
import dev.cuican.staypro.module.modules.player.Instant;
import dev.cuican.staypro.utils.RotationUtil;
import dev.cuican.staypro.utils.Wrapper;
import dev.cuican.staypro.utils.block.BlockUtil;
import dev.cuican.staypro.utils.inventory.InventoryUtil;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SeijaBlockUtil extends Wrapper {


    public static ArrayList<BlockPos> haveNeighborBlock(BlockPos pos, Block neighbor) {
        ArrayList<BlockPos> blockList = new ArrayList<>();
        if (mc.world.getBlockState(pos.add(1, 0, 0)).getBlock().equals(neighbor))
            blockList.add(pos.add(1, 0, 0));
        if (mc.world.getBlockState(pos.add(-1, 0, 0)).getBlock().equals(neighbor))
            blockList.add(pos.add(-1, 0, 0));
        if (mc.world.getBlockState(pos.add(0, 1, 0)).getBlock().equals(neighbor))
            blockList.add(pos.add(0, 1, 0));
        if (mc.world.getBlockState(pos.add(0, -1, 0)).getBlock().equals(neighbor))
            blockList.add(pos.add(0, -1, 0));
        if (mc.world.getBlockState(pos.add(0, 0, 1)).getBlock().equals(neighbor))
            blockList.add(pos.add(0, 0, 1));
        if (mc.world.getBlockState(pos.add(0, 0, -1)).getBlock().equals(neighbor))
            blockList.add(pos.add(0, 0, -1));
        return blockList;
    }

    public static boolean isPlaceable(BlockPos pos, boolean helpBlock, boolean bBoxCheck) {
        if (!mc.world.getBlockState(pos).getBlock().equals(Blocks.AIR)) {
            //Command.sendMessage("false1");
            return false;
        }
        if (helpBlock) {
            if (haveNeighborBlock(pos, Blocks.AIR).size() >= 6) {
                //Command.sendMessage("false2");
                return false;
            }
        }
        if (bBoxCheck) {
            if (!isNoBBoxBlocked(pos)) {
                //Command.sendMessage("false3");
                return false;
            }
        }
        return true;
    }

    public static boolean isFacing(BlockPos pos, EnumFacing enumFacing) {
        ImmutableMap<IProperty<?>, Comparable<?>> properties = mc.world.getBlockState(pos).getProperties();
        for (IProperty<?> prop : properties.keySet()) {
            if (prop.getValueClass() == EnumFacing.class && (prop.getName().equals("facing") || prop.getName().equals("rotation"))) {
                if (properties.get(prop) == enumFacing) {
                    return true;

                }
            }
        }
        return false;
    }
    public static EnumFacing getFacing(BlockPos pos) {
        ImmutableMap<IProperty<?>, Comparable<?>> properties = mc.world.getBlockState(pos).getProperties();
        for (IProperty<?> prop : properties.keySet()) {
            if (prop.getValueClass() == EnumFacing.class && (prop.getName().equals("facing") || prop.getName().equals("rotation"))) {

                    return (EnumFacing) properties.get(prop);


            }
        }
        return null;
    }

    public static boolean isNoBBoxBlocked(BlockPos pos) {
        AxisAlignedBB axisAlignedBB = new AxisAlignedBB(pos);
        List l = mc.world.getEntitiesWithinAABBExcludingEntity(null, axisAlignedBB);
        if (l.size() == 0) {
            return true;
        }
        return false;
    }

    public static void mine(BlockPos pos1, BlockPos pos2) {
        //if (timer.passedDs(switchDelay.getValue())&&switchDelay.getValue()>0.0){
        int oldslot = mc.player.inventory.currentItem;
        int dSlot = InventoryUtil.getItemHotbar(Items.DIAMOND_PICKAXE);
        if (dSlot != -1) {
            InventoryUtil.switchToHotbarSlot(dSlot, false);
        }
        InventoryUtil.switchToHotbarSlot(oldslot, false);
        //timer.reset();
        //}

        if (Instant.breakPos == null) {
            mc.playerController.onPlayerDamageBlock(pos1, BlockUtil.getRayTraceFacing(pos1));

            mc.playerController.onPlayerDamageBlock(pos2, BlockUtil.getRayTraceFacing(pos2));

        }
    }

    public static void mine(BlockPos minePos) {
        //if (timer.passedDs(switchDelay.getValue())&&switchDelay.getValue()>0.0){
        int oldslot = mc.player.inventory.currentItem;
        int dSlot = InventoryUtil.getItemHotbar(Items.DIAMOND_PICKAXE);
        if (dSlot != -1) {
            InventoryUtil.switchToHotbarSlot(dSlot, false);
        }
        InventoryUtil.switchToHotbarSlot(oldslot, false);
        //timer.reset();
        //}
        if (Instant.breakPos == null) {
            mc.playerController.onPlayerDamageBlock(minePos, BlockUtil.getRayTraceFacing(minePos));
        }
    }

    public static BlockPos getFlooredPosition(Entity entity) {
        return new BlockPos(Math.floor(entity.posX), Math.round(entity.posY), Math.floor(entity.posZ));
    }

    public static boolean isNoBBoxBlocked(BlockPos pos, boolean ignoreSomeEnt) {
        AxisAlignedBB axisAlignedBB = new AxisAlignedBB(pos);
        List<Entity> l = mc.world.getEntitiesWithinAABBExcludingEntity(null, axisAlignedBB);
        if (ignoreSomeEnt) {
            for (Entity entity : l) {
                if (entity instanceof EntityEnderCrystal
                        || entity instanceof EntityItem
                        || entity instanceof EntityArrow
                        || entity instanceof EntityTippedArrow
                        || entity instanceof EntityArrow
                        || entity instanceof EntityXPOrb
                ) {
                    //Command.sendMessage("continue");
                    continue;
                }
                return false;
            }
            return true;
        } else {
            if (l.size() == 0)
                return true;
            return false;
        }
    }

//    public static boolean isPlaceable(BlockPos pos, ArrayList<Block> ignoreBlock, boolean bBoxCheck, boolean helpBlockCheck, boolean rayTrace) {
//        boolean placeable = false;
//        for (Block iGB : ignoreBlock) {
//            if (mc.world.getBlockState(pos).getBlock().equals(iGB) || mc.world.getBlockState(pos).getBlock().equals(Blocks.AIR)) {
//                placeable = true;
//                break;
//            }
//        }
//        if (bBoxCheck) {
//            if (!isNoBBoxBlocked(pos, true))
//                placeable = false;
//        }
//        if (helpBlockCheck) {
//            if (haveNeighborBlock(pos, Blocks.AIR).size() >= 6)
//                placeable = false;
//        }
//        if (rayTrace) {
//            if (!CombatUtil.rayTraceRangeCheck(pos, 0, 0.0d)) {
//                placeable = false;
//            }
//        }
//        return placeable;
//    }

//    public static void placeBlock(final BlockPos pos, final EnumHand hand, final boolean rotate, final boolean packet, EnumFacing placeFac) {
//        EnumFacing side = placeFac;
//        HashMap<EnumFacing, Double> distanceMap = new HashMap<>();
//        for (EnumFacing fac : EnumFacing.values()) {
//            BlockPos offsetBlock = pos.offset(fac);
//            if (!mc.world.getBlockState(offsetBlock).getBlock().equals(Blocks.AIR))
//                distanceMap.put(fac, Math.sqrt(mc.player.getDistanceSq(offsetBlock)));
//        }
//        if (distanceMap.size() != 0) {
//            List<Map.Entry<EnumFacing, Double>> list = new ArrayList<>(distanceMap.entrySet());
//            list.sort(Map.Entry.comparingByValue());
//            side = list.get(0).getKey();
//        }
//
//
//        if (mc.world.getBlockState(pos.offset(side, 1)).getBlock().equals(Blocks.AIR)) {
//            if (placeFac == null) {
//                return;
//            }
//        }
//        if (side == null)
//            return;
//
//        final BlockPos neighbour = pos.offset(side);
//        final EnumFacing opposite = side.getOpposite();
//        final Vec3d hitVec = new Vec3d((Vec3i) neighbour).add(0.5, 0.5, 0.5).add(new Vec3d(opposite.getDirectionVec()).scale(0.5));
//        final Block neighbourBlock = BlockUtil.mc.world.getBlockState(neighbour).getBlock();
//        if (!BlockUtil.mc.player.isSneaking() && (BlockUtil.blackList.contains(neighbourBlock) || BlockUtil.shulkerList.contains(neighbourBlock))) {
//            BlockUtil.mc.player.connection.sendPacket((Packet) new CPacketEntityAction((Entity) BlockUtil.mc.player, CPacketEntityAction.Action.START_SNEAKING));
//            BlockUtil.mc.player.setSneaking(true);
//        }
////        if (rotate && UpdateWalkingPlayerEvent.INSTANCE != null) {
////            UpdateWalkingPlayerEvent.INSTANCE.setRotation(BlockInteractionHelper.getLegitRotations(hitVec)[0], BlockInteractionHelper.getLegitRotations(hitVec)[1]);
////        }
//        if (rotate) {
//            RotationUtil.faceVector(hitVec, true);
//        }
//
//        BlockUtil.rightClickBlock(neighbour, hitVec, hand, opposite, packet);
//        BlockUtil.mc.rightClickDelayTimer = 4;
//    }


    public static void placeBlock(BlockPos pos,EnumFacing facing,EnumHand hand){
        EnumFacing opposite = facing.getOpposite();
        BlockPos neighbour =pos.offset(facing);
        final Vec3d hitVec = new Vec3d((Vec3i) neighbour).add(0.5, 0.5, 0.5).add(new Vec3d(opposite.getDirectionVec()).scale(0.5));
        BlockUtil.rightClickBlock(neighbour, hitVec, hand, opposite, true);
    }

    public static void sneak(BlockPos pos){
        if (!mc.player.isSneaking() && (BlockUtil. blackList.contains(pos) || BlockUtil.shulkerList.contains(pos))) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
            mc.player.setSneaking(true);

        }
    }

    public static BlockPos vec3toBlockPos(Vec3d vec3d, boolean Yfloor) {

        if (Yfloor)
            return new BlockPos(Math.floor(vec3d.x), Math.floor(vec3d.y), Math.floor(vec3d.z));
        else return new BlockPos(Math.floor(vec3d.x), Math.round(vec3d.y), Math.floor(vec3d.z));
    }
    public static BlockPos vec3toBlockPos(Vec3d vec3d) {
        return new BlockPos(Math.floor(vec3d.x), Math.round(vec3d.y), Math.floor(vec3d.z));
    }

    public static boolean fakeBBoxCheck(EntityPlayer player, Vec3d offset, boolean headcheck) {
        Vec3d futurePos = player.getPositionVector().add(offset);

        if (headcheck) {
            Vec3d playerPos = player.getPositionVector();
            return isAir(futurePos.add(0.3, 0, 0.3))
                    && isAir(futurePos.add(-0.3, 0, 0.3))
                    && isAir(futurePos.add(0.3, 0, -0.3))
                    && isAir(futurePos.add(-0.3, 0, 0.3))
                    && isAir(futurePos.add(0.3, 1.8, 0.3))
                    && isAir(futurePos.add(-0.3, 1.8, 0.3))
                    && isAir(futurePos.add(0.3, 1.8, -0.3))
                    && isAir(futurePos.add(-0.3, 1.8, 0.3))
                    && isAir(playerPos.add(0.3, 2.8, 0.3))
                    && isAir(playerPos.add(-0.3, 2.8, 0.3))
                    && isAir(playerPos.add(-0.3, 2.8, -0.3))
                    && isAir(playerPos.add(0.3, 2.8, -0.3))
                    ;
        }

        return isAir(futurePos.add(0.3, 0, 0.3))
                && isAir(futurePos.add(-0.3, 0, 0.3))
                && isAir(futurePos.add(0.3, 0, -0.3))
                && isAir(futurePos.add(-0.3, 0, 0.3))
                && isAir(futurePos.add(0.3, 1.8, 0.3))
                && isAir(futurePos.add(-0.3, 1.8, 0.3))
                && isAir(futurePos.add(0.3, 1.8, -0.3))
                && isAir(futurePos.add(-0.3, 1.8, 0.3));


    }


    public static boolean fakeBBoxCheck2(EntityPlayer player, Vec3d futureVec, boolean headcheck) {

        if (headcheck) {
            Vec3d playerPos = player.getPositionVector();
            return isAir(futureVec.add(0.3, 0, 0.3))
                    && isAir(futureVec.add(-0.3, 0, 0.3))
                    && isAir(futureVec.add(0.3, 0, -0.3))
                    && isAir(futureVec.add(-0.3, 0, 0.3))
                    && isAir(futureVec.add(0.3, 1.8, 0.3))
                    && isAir(futureVec.add(-0.3, 1.8, 0.3))
                    && isAir(futureVec.add(0.3, 1.8, -0.3))
                    && isAir(futureVec.add(-0.3, 1.8, 0.3))
                    && isAir(playerPos.add(0.3, 2.8, 0.3))
                    && isAir(playerPos.add(-0.3, 2.8, 0.3))
                    && isAir(playerPos.add(-0.3, 2.8, -0.3))
                    && isAir(playerPos.add(0.3, 2.8, -0.3))
                    ;
        }

        return isAir(futureVec.add(0.3, 0, 0.3))
                && isAir(futureVec.add(-0.3, 0, 0.3))
                && isAir(futureVec.add(0.3, 0, -0.3))
                && isAir(futureVec.add(-0.3, 0, 0.3))
                && isAir(futureVec.add(0.3, 1.8, 0.3))
                && isAir(futureVec.add(-0.3, 1.8, 0.3))
                && isAir(futureVec.add(0.3, 1.8, -0.3))
                && isAir(futureVec.add(-0.3, 1.8, 0.3));


    }


    public static boolean isAir(Vec3d vec3d) {
        return mc.world.getBlockState(vec3toBlockPos(vec3d, true)).getBlock().equals(Blocks.AIR);
    }
    public static boolean isAir(double x,double y,double z) {
        Vec3d vec3d = new Vec3d(x,y,z);
        return mc.world.getBlockState(vec3toBlockPos(vec3d, true)).getBlock().equals(Blocks.AIR);
    }
//    public static void placeBlock(final Vec3d vec3d, final EnumHand hand, final boolean rotate, final boolean packet) {
//        BlockPos pos=vec3toBlockPos(vec3d);
//        final EnumFacing side = BlockUtil.getFirstFacing(pos);
//        if (side == null) {
//            return;
//        }
//        final BlockPos neighbour = pos.offset(side);
//        final EnumFacing opposite = side.getOpposite();
//        final Vec3d hitVec = new Vec3d((Vec3i) neighbour).add(0.5, 0.5, 0.5).add(new Vec3d(opposite.getDirectionVec()).scale(0.5));
//        final Block neighbourBlock = BlockUtil.mc.world.getBlockState(neighbour).getBlock();
//        if (!BlockUtil.mc.player.isSneaking() && (BlockUtil.blackList.contains(neighbourBlock) || BlockUtil.shulkerList.contains(neighbourBlock))) {
//            BlockUtil.mc.player.connection.sendPacket((Packet) new CPacketEntityAction((Entity) BlockUtil.mc.player, CPacketEntityAction.Action.START_SNEAKING));
//            BlockUtil.mc.player.setSneaking(true);
//        }
////        if (rotate && UpdateWalkingPlayerEvent.INSTANCE != null) {
////            UpdateWalkingPlayerEvent.INSTANCE.setRotation(BlockInteractionHelper.getLegitRotations(hitVec)[0], BlockInteractionHelper.getLegitRotations(hitVec)[1]);
////        }
//        if (rotate) {
//            RotationUtil.faceVector(hitVec, true);
//        }
//
//        BlockUtil. rightClickBlock(neighbour, hitVec, hand, opposite, packet);
//        BlockUtil.mc.rightClickDelayTimer = 4;
//    }

    public static IBlockState getBlock(Vec3d vec){
        return mc.world.getBlockState(vec3toBlockPos(vec));
    }

    public static void placeBlock(final BlockPos pos, final EnumHand hand, final boolean rotate, final boolean packet, EnumFacing placeFac) {
        EnumFacing side = placeFac;
        HashMap<EnumFacing, Double> distanceMap = new HashMap<>();
        for (EnumFacing fac : EnumFacing.values()) {
            BlockPos offsetBlock = pos.offset(fac);
            if (!mc.world.getBlockState(offsetBlock).getBlock().equals(Blocks.AIR))
                distanceMap.put(fac, Math.sqrt(mc.player.getDistanceSq(offsetBlock)));
        }
        if (distanceMap.size() != 0) {
            List<Map.Entry<EnumFacing, Double>> list = new ArrayList<>(distanceMap.entrySet());
            list.sort(Map.Entry.comparingByValue());
            side = list.get(0).getKey();
        }


        if (mc.world.getBlockState(pos.offset(side, 1)).getBlock().equals(Blocks.AIR)) {
            if (placeFac == null) {
                return;
            }
        }
        if (side == null)
            return;

        final BlockPos neighbour = pos.offset(side);
        final EnumFacing opposite = side.getOpposite();
        final Vec3d hitVec = new Vec3d((Vec3i) neighbour).add(0.5, 0.5, 0.5).add(new Vec3d(opposite.getDirectionVec()).scale(0.5));
        final Block neighbourBlock = mc.world.getBlockState(neighbour).getBlock();
        if (!mc.player.isSneaking() && (BlockUtil.blackList.contains(neighbourBlock) || BlockUtil.shulkerList.contains(neighbourBlock))) {
            mc.player.connection.sendPacket((Packet) new CPacketEntityAction((Entity) mc.player, CPacketEntityAction.Action.START_SNEAKING));
           mc.player.setSneaking(true);
        }
//        if (rotate && UpdateWalkingPlayerEvent.INSTANCE != null) {
//            UpdateWalkingPlayerEvent.INSTANCE.setRotation(BlockInteractionHelper.getLegitRotations(hitVec)[0], BlockInteractionHelper.getLegitRotations(hitVec)[1]);
//        }
        if (rotate) {
            RotationUtil.faceVector(hitVec, true);
        }

        BlockUtil.rightClickBlock(neighbour, hitVec, hand, opposite, packet);
        mc.rightClickDelayTimer = 4;
    }
    public static void placeBlock(final Vec3d vec3d, final EnumHand hand, final boolean rotate, final boolean packet) {
        BlockPos pos=vec3toBlockPos(vec3d);
        final EnumFacing side = BlockUtil.getFirstFacing(pos);
        if (side == null) {
            return;
        }
        final BlockPos neighbour = pos.offset(side);
        final EnumFacing opposite = side.getOpposite();
        final Vec3d hitVec = new Vec3d((Vec3i) neighbour).add(0.5, 0.5, 0.5).add(new Vec3d(opposite.getDirectionVec()).scale(0.5));
        final Block neighbourBlock = mc.world.getBlockState(neighbour).getBlock();
        if (!mc.player.isSneaking() && (BlockUtil.blackList.contains(neighbourBlock) || BlockUtil.shulkerList.contains(neighbourBlock))) {
            mc.player.connection.sendPacket((Packet) new CPacketEntityAction((Entity) mc.player, CPacketEntityAction.Action.START_SNEAKING));
            mc.player.setSneaking(true);
        }
//        if (rotate && UpdateWalkingPlayerEvent.INSTANCE != null) {
//            UpdateWalkingPlayerEvent.INSTANCE.setRotation(BlockInteractionHelper.getLegitRotations(hitVec)[0], BlockInteractionHelper.getLegitRotations(hitVec)[1]);
//        }
        if (rotate) {
            RotationUtil.faceVector(hitVec, true);
        }

        BlockUtil. rightClickBlock(neighbour, hitVec, hand, opposite, packet);
        mc.rightClickDelayTimer = 4;
    }
}

