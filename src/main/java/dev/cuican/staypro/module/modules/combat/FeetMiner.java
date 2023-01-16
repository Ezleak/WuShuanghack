package dev.cuican.staypro.module.modules.combat;

import dev.cuican.staypro.common.annotations.ModuleInfo;
import dev.cuican.staypro.module.Category;
import dev.cuican.staypro.module.Module;
import dev.cuican.staypro.module.modules.player.Instant;
import dev.cuican.staypro.setting.Setting;
import dev.cuican.staypro.utils.MovementUtils;

import dev.cuican.staypro.utils.block.BlockUtil;
import dev.cuican.staypro.utils.tool.CrystalUtil;
import dev.cuican.staypro.utils.tool.SeijaBlockUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

@ModuleInfo(name = "FeetMiner", description = "AutoCity", category = Category.COMBAT)
public class FeetMiner extends Module {
    private final Setting<Double> targetRange = setting("TargetRange", 7.0, 0.0, 8.0);
    private final Setting<Double> targetMinDmg = setting("TargetMinDmg", 7.0, 0.0, 36.0);
    private final Setting<Double> targetMaxSpeed = setting("TargetMaxSpeed", 20.0, 0.0, 100.0);
    private final Setting<Double> mineRange = setting("MineRange", 5.6, 0.0, 8.0);
    private final Setting<Boolean> antiSelfMine = setting("antiSelfMine", true);

    @Override
    public void onTick() {
        if (Instant.breakPos != null) return;
        ArrayList<EntityPlayer> targets = getTargets();
        ArrayList<MinePathData> minePathDatas=new ArrayList<>() ;

        for1:
        for (EntityPlayer target : targets) {
            BlockPos targetPos = SeijaBlockUtil.getFlooredPosition(target);
            for2:
            for (EnumFacing facing : EnumFacing.VALUES) {
                if (facing == EnumFacing.UP || facing == EnumFacing.DOWN) continue for2;
                BlockPos surPos = targetPos.offset(facing);
                MinePathData minePathData = new MinePathData(surPos,targetPos);
                if (minePathData.path!=null)
                    minePathDatas.add(minePathData);

            }
        }
        if (minePathDatas.size()==0)return;
        Collections.sort(minePathDatas,new MinePathDataComparator());
        int i = 0;
        for (MinePathData M:minePathDatas){
            for (BlockPos pos:M.path){
                if (!mc.world.getBlockState(pos).getBlock().equals(Blocks.BEDROCK)){
                    mc.playerController.onPlayerDamageBlock(pos,BlockUtil.getRayTraceFacing(pos));
                    i++;
                    if (i>=2)
                        return;
                }
            }
        }
    }
    private class MinePathData {
        boolean nr;
        ArrayList<BlockPos> path;
        public MinePathData(BlockPos surPos, BlockPos targetPos){
            //surround都哇不开还挖什么anti
            if (canBreak(surPos))return;

            //所有可能的路径
            ArrayList<MinePathData> list=new ArrayList<>();

            //
            for (EnumFacing facing : EnumFacing.VALUES){
                if (facing==EnumFacing.UP||facing == EnumFacing.DOWN
                        ||surPos.offset(facing).equals(targetPos))continue;
                boolean nr1 = false;
                ArrayList<BlockPos> l = new ArrayList<>();
                if (!BlockUtil.isAir(surPos))
                l.add(surPos);

                //Mine AntiCityBlocks
                BlockPos extraPos = surPos.offset(facing);
                if (!canBreak(extraPos)||!canBreak(extraPos.offset(EnumFacing.UP)))nr1 = true;
                if (!BlockUtil.isAir(extraPos))
                    l.add(extraPos);
                if (!BlockUtil.isAir(extraPos.offset(EnumFacing.UP)))
                    l.add(extraPos.offset(EnumFacing.UP));

                //检测斜挖 斜挖要挖sur上面的方块
                if (!extraPos.equals(targetPos.offset(facing,2))){
                    if (!canBreak(surPos.offset(EnumFacing.UP)))nr1 = true;
                    if (!BlockUtil.isAir(surPos.offset(EnumFacing.UP)))
                        l.add(surPos.offset(EnumFacing.UP));

                }
                list.add(new MinePathData(nr1,l));
            }
            if (list.size()==0)return;
            Collections.sort(list,new MinePathDataComparator());
            path =list.get(0).path;
            nr = list.get(0).nr;
        }

        private MinePathData(boolean nr, ArrayList<BlockPos> path) {

            this.nr = nr;
            this.path = path;
        }
    }

    public class MinePathDataComparator implements Comparator<MinePathData>{
        @Override
        public int compare(MinePathData o1, MinePathData o2) {
            if (o1.nr^ o2.nr){
                if (o1.nr){
                    //排序有问题调换正负号
                    return -1;
                }return 1;
            }
            return o1.path.size()-o2.path.size();
        }
    }
//    private class MinePath{
//        ArrayList<BlockPos> path;
//        boolean nr;
//
//        public MinePath(ArrayList<BlockPos> path) {
//            for (BlockPos pos:path)
//        }
//    }

    private boolean canSafeBreak(BlockPos pos) {
        if (!antiSelfMine.getValue()) return true;
        for (EnumFacing facing : EnumFacing.VALUES) {
            if (facing == EnumFacing.UP || facing == EnumFacing.DOWN) continue;
            if (SeijaBlockUtil.getFlooredPosition(mc.player).offset(facing).equals(pos)) return false;
        }
        return true;
    }
    private boolean canBreak(BlockPos pos){
        return  !(mc.world.getBlockState(pos).getBlock().equals(Blocks.BEDROCK)
                || !canSafeBreak(pos)
                || Math.sqrt(mc.player.getDistanceSq(pos))>mineRange.getValue()
        ) ;
    }
    private ArrayList<EntityPlayer> getTargets() {
        ArrayList<EntityPlayer> targets = new ArrayList<>();
        for (EntityPlayer target : mc.world.playerEntities) {
            if (mc.player.getDistance(target) > targetRange.getValue()
                    || CrystalUtil.calculateMaxDmg(target, 6.0) < targetMinDmg.getValue()
                    || MovementUtils.getSpeed(target) > targetMaxSpeed.getValue()
            ) continue;
            targets.add(target);
        }
        return targets;
    }
}
