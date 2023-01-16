package dev.cuican.staypro.module.modules.combat;


import dev.cuican.staypro.client.FriendManager;
import dev.cuican.staypro.client.ModuleManager;
import dev.cuican.staypro.common.annotations.ModuleInfo;
import dev.cuican.staypro.event.events.render.RenderEvent;
import dev.cuican.staypro.module.Category;
import dev.cuican.staypro.module.Module;
import dev.cuican.staypro.module.modules.player.Instant;
import dev.cuican.staypro.setting.Setting;
import dev.cuican.staypro.utils.EntityUtil;
import dev.cuican.staypro.utils.block.BlockInteractionHelper;
import dev.cuican.staypro.utils.graphics.RenderUtils3D;
import dev.cuican.staypro.utils.inventory.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

@ModuleInfo(name = "AutoCity", category = Category.COMBAT, description = "AutoCity")
public class AutoCity extends Module {
    public final Setting<Integer> alpha = setting("alpha", 255, 0, 255);
    private final Setting<Boolean> aimode =setting ("Ai", true);

    public EntityPlayer target;
    boolean burrowBreak=false;
    BlockPos currentPos=null;
    BlockPos burrowPos=null;
    public void onDisable() {

        this.target = null;
        this.burrowBreak = false;
        this.currentPos = null;
        this.burrowPos = null;
    }

    public void onRenderTick() {
        this.target = EntityUtil.getTarget(6);
        if (this.target == null) {
            return;
        }
        this.burrowPos = EntityUtil.getPlayerPosFloored(this.target);
        if (this.burrowPos != null && EntityUtil.isPlayerBurrow(this.target) && !this.burrowBreak) {
            this.mc.player.connection.sendPacket((Packet)new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, this.burrowPos, EnumFacing.UP));
            this.mc.player.connection.sendPacket((Packet)new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, this.burrowPos, EnumFacing.UP));
            this.burrowBreak = true;
        }
        else {
            this.burrowBreak = false;
        }
        for (final EnumFacing facing : EnumFacing.HORIZONTALS) {
            final BlockPos currentPos1 = this.burrowPos.offset(facing);
            if (BlockInteractionHelper.canBreak(currentPos1)) {
                if (!BlockInteractionHelper.isAirBlock(currentPos1)) {
                    if (this.mc.player.getDistance((double)currentPos1.getX(), (double)currentPos1.getY(), (double)currentPos1.getZ()) <= 6.0) {
                        if (BlockInteractionHelper.canBreak(this.burrowPos.offset(EntityUtil.GetFacing().getOpposite())) && this.currentPos == null) {
                            this.currentPos = this.burrowPos.offset(EntityUtil.GetFacing().getOpposite());
                        }
                        else if (this.currentPos == null) {
                            this.currentPos = currentPos1;
                        }
                    }
                }
            }
        }
        if (this.currentPos == null) {
            return;
        }
        if (Instant.breakPos != this.currentPos) {
            if (this.aimode.getValue()) {
                if (!BlockInteractionHelper.isAirBlock(this.burrowPos) || Instant.breakPos == this.currentPos) {
                    return;
                }
                this.mc.playerController.onPlayerDamageBlock(this.currentPos, EnumFacing.UP);
            }
            else {
                this.mc.playerController.onPlayerDamageBlock(this.currentPos, EnumFacing.UP);
            }
            this.currentPos = null;
        }
    }

    public void onRenderWorld(final RenderEvent event) {
        if (this.currentPos != null) {
            if (BlockInteractionHelper.isAirBlock(this.currentPos)) {
                RenderUtils3D.drawFullBox(this.currentPos, 1.0f, 0, 255, 0, (int)this.alpha.getValue());
            }
            else {
                RenderUtils3D.drawFullBox(this.currentPos, 1.0f, 255, 0, 0, (int)this.alpha.getValue());
            }
        }
        if (this.burrowPos != null) {
            if (BlockInteractionHelper.isAirBlock(this.burrowPos)) {
                RenderUtils3D.drawFullBox(this.burrowPos, 1.0f, 0, 255, 0, (int)this.alpha.getValue());
            }
            else {
                RenderUtils3D.drawFullBox(this.burrowPos, 1.0f, 255, 0, 0, (int)this.alpha.getValue());
            }
        }
    }


}
