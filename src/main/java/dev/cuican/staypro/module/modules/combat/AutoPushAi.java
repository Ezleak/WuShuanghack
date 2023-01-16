//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "E:\mcp940\conf"!

//Decompiled by Procyon!

package dev.cuican.staypro.module.modules.combat;

import dev.cuican.staypro.client.FriendManager;
import dev.cuican.staypro.client.ModuleManager;
import dev.cuican.staypro.common.annotations.ModuleInfo;
import dev.cuican.staypro.concurrent.event.Listener;
import dev.cuican.staypro.event.events.network.PacketEvent;
import dev.cuican.staypro.event.events.render.RenderEvent;
import dev.cuican.staypro.module.Category;
import dev.cuican.staypro.module.Module;
import dev.cuican.staypro.module.modules.player.Instant;
import dev.cuican.staypro.setting.Setting;
import dev.cuican.staypro.utils.BurrowUtil;
import dev.cuican.staypro.utils.EntityUtil;
import dev.cuican.staypro.utils.RotationUtil;
import dev.cuican.staypro.utils.block.BlockInteractionHelper;
import dev.cuican.staypro.utils.graphics.RenderUtils3D;
import dev.cuican.staypro.utils.inventory.InventoryUtil;
import net.minecraft.entity.player.*;
import net.minecraft.network.play.client.*;

import java.awt.*;
import java.util.*;
import net.minecraft.init.*;
import net.minecraft.block.*;

import net.minecraft.network.*;
import net.minecraft.util.math.*;
import net.minecraft.util.*;

@ModuleInfo(name = "AutoPushAi", category = Category.COMBAT, description = "simple piston push awa")
public class AutoPushAi extends Module
{
    public Setting<Boolean> silentSwitch = setting("SilentSwitch", true);
    public Setting<Boolean> toggle = setting("Toggle", true);
    public Setting<Boolean> aiMode = setting("AI", true);
    public Setting<Boolean> mineRedstone = setting("Mine Redstone", true);

    EntityPlayer target;
    BlockPos pistonBlockPos;
    int oldSlot;
    boolean canRotate;
    public EnumHand oldhand = null;
    public int oldslot = -1;
    public int pistonSlot , redstoneSlot;

    public AutoPushAi() {
        this.oldSlot = -1;
        this.canRotate = false;
    }
    public void reset()
    {
        target = null;
        this.target = null;
        this.pistonBlockPos = null;
        this.oldSlot = -1;
        this.canRotate = false;
        oldslot = -1;
        oldhand = null;

    }

    public void onDisable() {
        this.target = null;
        this.pistonBlockPos = null;
        this.oldSlot = -1;
        this.canRotate = false;
    }

    public void onPacketSend(final PacketEvent.Send event) {
        if (!(event.packet instanceof CPacketPlayer) || this.canRotate) {}
    }

    public void onRenderTick() {
        this.target = (EntityPlayer)this.mc.world.playerEntities.stream().filter(entityPlayer -> entityPlayer != this.mc.player && !FriendManager.isFriend(entityPlayer) && !((EntityPlayer)entityPlayer).isDead && this.mc.player.getDistance(entityPlayer) < 6.0f).min(Comparator.comparing(e -> this.mc.player.getDistance(e))).get();
        if (this.target == null) {
            return;
        }
        final int block1Slot = InventoryUtil.findHotbarBlock((Block)Blocks.PISTON);
        final int redBlockSlot = InventoryUtil.findHotbarBlock(Blocks.REDSTONE_BLOCK);
        final EnumFacing facing = EntityUtil.GetFacing();
        if (block1Slot == -1 || redBlockSlot == -1) {
            return;
        }
        if (this.mc.player.inventory.currentItem != block1Slot) {
            this.oldSlot = this.mc.player.inventory.currentItem;
            this.mc.player.inventory.currentItem = block1Slot;
            this.mc.playerController.updateController();
        }
        this.pistonBlockPos = EntityUtil.getPlayerPosFloored(this.target).offset(facing).up();
        if (this.pistonBlockPos == null) {
            return;
        }
        if (this.pistonBlockPos != null && this.mc.world.getBlockState(this.pistonBlockPos).getBlock().equals(Blocks.PISTON)) {
            return;
        }
        if (!BlockInteractionHelper.checkForNeighbours(this.pistonBlockPos)) {
            final BlockPos[] array;
            final BlockPos[] l_Test = array = new BlockPos[] { this.pistonBlockPos.north(), this.pistonBlockPos.south(), this.pistonBlockPos.east(), this.pistonBlockPos.west(), this.pistonBlockPos.down() };
            for (final BlockPos pos2 : array) {
                final BlockInteractionHelper.ValidResult vailRequest = BlockInteractionHelper.valid(pos2);
                if (BlockInteractionHelper.isAirBlock(pos2) && vailRequest != BlockInteractionHelper.ValidResult.NoNeighbors && vailRequest != BlockInteractionHelper.ValidResult.NoEntityCollision) {
                    BlockInteractionHelper.place(pos2, 6.0f, false, false);
                    break;
                }
            }
        }
        if ((!BlockInteractionHelper.isAirBlock(this.pistonBlockPos) || !BlockInteractionHelper.isAirBlock(this.pistonBlockPos.up())) && (boolean)this.aiMode.getValue()) {
            for (final EnumFacing facing2 : EnumFacing.HORIZONTALS) {
                final BlockPos currentPos = EntityUtil.getPlayerPosFloored(this.target).offset(facing2).up();
                if (BlockInteractionHelper.isAirBlock(currentPos) && BlockInteractionHelper.isAirBlock(currentPos.up())) {
                    this.pistonBlockPos = currentPos;
                    final float[] v = RotationUtil.getRotationsBlock(currentPos.down(), EnumFacing.UP, false);
                    this.mc.player.connection.sendPacket((Packet)new CPacketPlayer.Rotation(v[0], v[1], this.mc.player.onGround));
                    this.canRotate = true;
                    break;
                }
            }
        }
        this.mc.playerController.processRightClickBlock(this.mc.player, this.mc.world, this.pistonBlockPos.down(), EnumFacing.UP, new Vec3d((Vec3i)this.pistonBlockPos), EnumHand.MAIN_HAND);
        this.mc.player.swingArm(EnumHand.MAIN_HAND);
        if (this.mc.world.getBlockState(this.pistonBlockPos).getBlock().equals(Blocks.PISTON)) {
            this.mc.player.inventory.currentItem = redBlockSlot;
            this.mc.playerController.updateController();
            this.mc.playerController.processRightClickBlock(this.mc.player, this.mc.world, this.pistonBlockPos, EnumFacing.UP, new Vec3d((Vec3i)this.pistonBlockPos), EnumHand.MAIN_HAND);
            this.canRotate = false;
            this.mc.player.swingArm(EnumHand.MAIN_HAND);
        }
        this.mc.player.inventory.currentItem = this.oldSlot;
        this.mc.playerController.updateController();
        if (mineRedstone.getValue()) {
            if (Instant.breakPos != null) {
                if (Instant.breakPos.getZ() == pistonBlockPos.up().getZ() && Instant.breakPos.getX() == pistonBlockPos.up().getX() && Instant.breakPos.getY() == pistonBlockPos.up().getY()) {
                    return;
                }
            }
            Instant.ondeve(pistonBlockPos.up());
        } else {

        }


        if (this.toggle.getValue()) {
            this.toggle();
        }



    }

    public String getModuleInfo() {
        return (this.target == null) ? "FindTarget..." : this.target.getDisplayNameString();
    }
@Listener
    public void onRenderWorld(RenderEvent event) {
        if (this.pistonBlockPos != null && this.pistonBlockPos != null) {
            if (BlockInteractionHelper.isAirBlock(this.pistonBlockPos)) {
                RenderUtils3D.drawBoxESP(pistonBlockPos, new Color(0, 0, 255), false, new Color(0, 0, 255), 1.0f, false, true, 42, true);
                RenderUtils3D.drawBoxESP(pistonBlockPos.up(), new Color(255, 0, 0), false, new Color(255, 0, 0), 1.0f, false, true, 42, true);
            }
            else {
                RenderUtils3D.drawBoxESP(pistonBlockPos, new Color(0, 0, 255), false, new Color(0, 0, 255), 1.0f, false, true, 42, true);
                RenderUtils3D.drawBoxESP(pistonBlockPos.up(), new Color(255, 0, 0), false, new Color(255, 0, 0), 1.0f, false, true, 42, true);
            }
        }
    }

}
