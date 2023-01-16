package dev.cuican.staypro.utils.tool;

import dev.cuican.staypro.utils.BlockInteractionHelper;
import dev.cuican.staypro.utils.Wrapper;
import dev.cuican.staypro.utils.particles.DamageUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class CrystalUtil extends Wrapper {
    public static boolean canPlaceCrystal(BlockPos blockPos) {

        BlockPos boost = blockPos.add(0, 1, 0);

        BlockPos boost2 = blockPos.add(0, 2, 0);

        if (mc.world.getBlockState(blockPos).getBlock() != Blocks.BEDROCK && mc.world.getBlockState(blockPos).getBlock() != Blocks.OBSIDIAN)
            return false;
        if (mc.world.getBlockState(boost).getBlock() != Blocks.AIR || mc.world.getBlockState(boost2).getBlock() != Blocks.AIR)
            return false;
        return mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(boost)).isEmpty()
                && mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(boost2)).isEmpty();
    }

    public static List<BlockPos> findCrystalBlocks(BlockPos targetPos, double range) {
        NonNullList<BlockPos> positions = NonNullList.create();
        positions.addAll(BlockInteractionHelper.getSphere(targetPos, (float) range, (int) range, false, true, 0)
                .stream()
                .filter(CrystalUtil::canPlaceCrystal)
                .sorted(Comparator.comparing(it -> mc.player.getDistance(it.getX(), it.getY(), it.getZ())))
                .collect(Collectors.toList()));
        return positions;
    }

    public static double calculateMaxDmg(EntityPlayer player, double placeRange) {
        List<BlockPos> crystalBlocks = findCrystalBlocks(SeijaBlockUtil.getFlooredPosition(player), placeRange);
        double maxDamage = 0;
        for (BlockPos blockPos : crystalBlocks) {
            //过滤掉超出计算范围的方块
            if (player.getDistanceSq(blockPos) >= placeRange * placeRange) continue;
            //过滤掉超出放置范围的方块
            if (mc.player.getDistance(blockPos.getX(), blockPos.getY(), blockPos.getZ()) > placeRange)
                continue;
            //对敌人的伤害
            double targetDamage = DamageUtil.calculateDamage(blockPos.getX() + 0.5, blockPos.getY() + 1, blockPos.getZ() + 0.5, player);
            //如果这个位置的伤害比之前的最高伤害伤害低，跳过这个位置.这样可以获取到伤害最高的位置
            if (targetDamage < maxDamage) continue;


            //这里已经获取到了想要的目标位置了，先暂存起来，康康后面是不是还有更好的位置
            maxDamage = targetDamage;

        }
        //如果已经有炸这个敌人的目标方块，就不计算下一个敌人了


        return maxDamage;
    }

}
