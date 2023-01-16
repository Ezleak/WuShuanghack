
package dev.cuican.staypro.module.modules.combat;


import dev.cuican.staypro.common.annotations.ModuleInfo;
import dev.cuican.staypro.module.Category;
import dev.cuican.staypro.module.Module;
import dev.cuican.staypro.utils.EntityUtil;
import dev.cuican.staypro.utils.block.BlockInteractionHelper;
import dev.cuican.staypro.utils.inventory.InventoryUtil;
import net.minecraft.init.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;

@ModuleInfo(name = "AntiPistion", category = Category.COMBAT)
public class AntiPiston extends Module
{
    int oldSlot;
    
    public AntiPiston() {
        this.oldSlot = -1;
    }
    
    public void onDisable() {
        this.oldSlot = -1;
    }
    
    public void onRenderTick() {
        for (final EnumFacing facing : EnumFacing.HORIZONTALS) {
            if (facing == EntityUtil.GetFacing()) {
                return;
            }
            final BlockPos pos = EntityUtil.getLocalPlayerPosFloored();
            final BlockPos currentPos = pos.offset(facing).up();
            if (BlockInteractionHelper.isAirBlock(currentPos) && BlockInteractionHelper.checkForNeighbours(currentPos)) {
                final int blockSlot = InventoryUtil.findHotbarBlock(Blocks.OBSIDIAN);
                if (blockSlot != -1) {
                    this.oldSlot = this.mc.player.inventory.currentItem;
                    this.mc.player.inventory.currentItem = blockSlot;
                    this.mc.playerController.updateController();
                }
                BlockInteractionHelper.place(currentPos, 4.0f, false, false);
                this.mc.player.swingArm(EnumHand.MAIN_HAND);
                this.mc.player.inventory.currentItem = this.oldSlot;
                this.mc.playerController.updateController();
                this.toggle();
            }
        }
    }
}
