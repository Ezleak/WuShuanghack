package dev.cuican.staypro.module.modules.player;


import dev.cuican.staypro.common.annotations.ModuleInfo;
import dev.cuican.staypro.module.Category;
import dev.cuican.staypro.module.Module;
import dev.cuican.staypro.setting.Setting;
import dev.cuican.staypro.utils.tool.SeijaBlockUtil;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.Vec3d;

@ModuleInfo(name = "CityBoss", category = Category.PLAYER, description = "CityBoss")
public class CityBoss extends Module {


    public Setting<Boolean> onBurrow = setting("OnBurrow", false);
    public Setting<Boolean> toggle = setting("AutoToggle", false);
    public Setting<Integer> offset = setting("Offset", 1, 0, 10);

    @Override
    public void onTick() {
        double x = Math.abs(mc.player.posX) - Math.floor(Math.abs(mc.player.posX));
        double z = Math.abs(mc.player.posZ) - Math.floor(Math.abs(mc.player.posZ));
        if (x == 0.700 || x == 0.300 || z == 0.700 || z == 0.300||(!onBurrow.getValue()&&!mc.world.getBlockState(SeijaBlockUtil.getFlooredPosition(mc.player)).getBlock().equals(Blocks.AIR)))
            return ;
        Vec3d playerVec = mc.player.getPositionVector();
        if (!mc.world.getBlockState(SeijaBlockUtil.vec3toBlockPos(playerVec.add(new Vec3d(0.3+offset.getValue()/100.0,0.2,0)))).getBlock().equals(Blocks.AIR)){
            mc.player.setPosition(mc.player.posX+offset.getValue()/100.0,mc.player.posY,mc.player.posZ);
            if (toggle.getValue())disable();
            return;
        }
        if (!mc.world.getBlockState(SeijaBlockUtil.vec3toBlockPos(playerVec.add(new Vec3d(-0.3-offset.getValue()/100.0,0.2,0)))).getBlock().equals(Blocks.AIR)){
            mc.player.setPosition(mc.player.posX-offset.getValue()/100.0,mc.player.posY,mc.player.posZ);
            if (toggle.getValue())disable();
            return;
        }
        if (!mc.world.getBlockState(SeijaBlockUtil.vec3toBlockPos(playerVec.add(new Vec3d(0,0.2,0.3+offset.getValue()/100.0)))).getBlock().equals(Blocks.AIR)){
            mc.player.setPosition(mc.player.posX,mc.player.posY,mc.player.posZ+offset.getValue()/100.0);
            if (toggle.getValue())disable();
            return;
        }
        if (!mc.world.getBlockState(SeijaBlockUtil.vec3toBlockPos(playerVec.add(new Vec3d(0,0.2,-0.3-offset.getValue()/100.0)))).getBlock().equals(Blocks.AIR)){
            mc.player.setPosition(mc.player.posX,mc.player.posY,mc.player.posZ-offset.getValue()/100.0);
            if (toggle.getValue())disable();
            return;
        }
    }
}
