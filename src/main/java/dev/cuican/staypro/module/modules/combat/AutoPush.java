package dev.cuican.staypro.module.modules.combat;



import dev.cuican.staypro.client.ModuleManager;
import dev.cuican.staypro.common.annotations.ModuleInfo;
import dev.cuican.staypro.concurrent.event.Listener;
import dev.cuican.staypro.event.events.render.RenderEvent;
import dev.cuican.staypro.module.Category;
import dev.cuican.staypro.module.Module;
import dev.cuican.staypro.module.modules.player.Instant;
import dev.cuican.staypro.setting.Setting;
import dev.cuican.staypro.utils.*;
import dev.cuican.staypro.utils.block.BlockInteractionHelper;
import dev.cuican.staypro.utils.block.BlockUtil;
import dev.cuican.staypro.utils.graphics.RenderUtils3D;
import dev.cuican.staypro.utils.inventory.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
@ModuleInfo(name = "AutoPush", category = Category.COMBAT)

public class AutoPush extends Module {
    public Setting<Float> preDelay = setting("BlockDelay", 0f, 0f, 25F);
    public Setting<Float> placeDelay = setting("PlaceDelay", 0f, 0f, 25f);

    public Setting<Boolean> packetPlace = setting("PacketPlace", true);
    public Setting<Boolean> silentSwitch = setting("SilentSwitch", true);
    public Setting<Float> range = setting("Range", 10f, 1f, 20f);
    public Setting<Float> targetRange = setting("Target Range", 10f, 1f, 20f);

    public Setting<String> redStoneType = setting("Redstone","Both",listOf("Both", "Torch", "Block"));
    public Setting<String> targetType = setting("Target","Nearest",listOf("Nearest", "Looking", "Best"));

    public Setting<Boolean> mineRedstone = setting("Mine Redstone", true);



    public EntityPlayer target = null;
    public int pistonSlot , redstoneSlot , obbySlot = -1;
    public BlockPos pistonPos , redstonePos = null;
    public int stage = 0;
    public Timer preTimer , timer = null;
    public int oldslot = -1;
    public EnumHand oldhand = null;
    public boolean isTorch = false,mined = false;;



    public void reset()
    {
        target = null;
        pistonSlot = -1;
        redstoneSlot = -1;
        obbySlot = -1;
        pistonPos = null;
        redstonePos = null;
        stage = 0;
        preTimer = null;
        timer = null;
        oldslot = -1;
        oldhand = null;
        isTorch = false;
        mined=false;
    }

    @Override
    public void onEnable()
    {
        reset();
    }

    @Override
    public void onTick(){
        if(nullCheck()) return;

        if(!findMaterials())
        {
            ChatUtil.printChatMessage("Cannot find materials! disabling...");
            disable();
            return;
        }
        target = findTarget();
        if(target == null)
        {
            ChatUtil.printChatMessage("Cannot find target! disabling...");
            disable();
            return;
        }
        if (isNull(pistonPos) || isNull(redstonePos)) {
            if (!findSpace(target)) {
                ChatUtil.printChatMessage("Cannot find space! disabling...");
                disable();
                ModuleManager.getModuleByName("AutoPushAi").toggle();
                return;
            }
        }

        if(preTimer == null)
        {
            preTimer = new Timer();
        }
        if (preTimer.passedX(preDelay.getValue())) {
            if (!prepareBlock()) {
                restoreItem();
                return;
            }
        }

        if(timer == null)
        {
            timer = new Timer();
        }
        if (stage == 0) {
            if(timer.passedX(placeDelay.getValue())) {
                setItem(pistonSlot);
                BlockPos targetPos = new BlockPos(target.posX , target.posY , target.posZ);
                float[] angle = MathUtil.calcAngle(new Vec3d(pistonPos), new Vec3d(targetPos));
                mc.player.connection.sendPacket(new CPacketPlayer.Rotation(angle[0] + 180.0F, angle[1], true));

                BlockUtil.placeBlock(pistonPos, packetPlace.getValue());
                stage = 1;
                timer.reset();
            }
        }
        if (stage == 1) {
            if(timer.passedX(placeDelay.getValue())) {
                setItem(redstoneSlot);
                BlockUtil.placeBlock(redstonePos , packetPlace.getValue());
                stage = 2;
                disable();
                reset();
            }
        }
        restoreItem();
        if (mineRedstone.getValue()) {
            if (Instant.breakPos != null) {
                if (Instant.breakPos.getZ() == redstonePos.getZ() && Instant.breakPos.getX() == redstonePos.getX() && Instant.breakPos.getY() == redstonePos.getY()) {
                    return;
                }
            }
            Instant.ondeve(redstonePos);
        } else {

        }
    }

    public void setItem(int slot)
    {
        if(silentSwitch.getValue()) {
            oldhand = null;
            if(mc.player.isHandActive()) {
                oldhand = mc.player.getActiveHand();
            }
            oldslot = mc.player.inventory.currentItem;
            mc.player.connection.sendPacket(new CPacketHeldItemChange(slot));
        }
        else {
            mc.player.inventory.currentItem = slot;
            mc.playerController.updateController();
        }
    }

    public void restoreItem()
    {
        if(oldslot != -1 && silentSwitch.getValue())
        {
            if(oldhand != null) {
                mc.player.setActiveHand(oldhand);
            }
            mc.player.connection.sendPacket(new CPacketHeldItemChange(oldslot));
            oldslot = -1;
            oldhand = null;
        }
    }

    public boolean isNull(Object object)
    {
        return object == null;
    }

    public boolean findSpace(EntityPlayer target)
    {
        BlockPos targetPos = new BlockPos(target.posX , target.posY , target.posZ);
        BlockPos mypos = new BlockPos(mc.player.posX , mc.player.posY , mc.player.posZ);
        BlockPos[] offsets = new BlockPos[]
                {
                        new BlockPos(1 , 0 , 0) ,
                        new BlockPos(-1 , 0 , 0),
                        new BlockPos(0 , 0 ,1) ,
                        new BlockPos(0 , 0 , -1) ,
                };
        List<AutoPushPos> poses = new ArrayList<>();
        for(BlockPos offset : offsets)
        {
            AutoPushPos pos = new AutoPushPos();
            BlockPos base = targetPos.add(offset);
            if(BlockUtil.getBlock(base) == Blocks.AIR) continue;
            BlockPos pistonPos = base.add(0 , 1 , 0);
            if(BlockUtil.getBlock(pistonPos) != Blocks.AIR) continue;
            if(checkPos(pistonPos)) continue;
            if(PlayerUtil.getDistance(pistonPos) < 3.6D
                    && pistonPos.getY() > mypos.getY() + 1) continue;
            if(BlockUtil.getBlock(targetPos.add(offset.getX() * -1 , 1 , offset.getZ() * -1))
                        != Blocks.AIR) continue;

            List<BlockPos> redstonePoses = new ArrayList<>();
            List<BlockPos> roffsets = new ArrayList<>();

            roffsets.add(new BlockPos(1 , 0 , 0));
            roffsets.add(new BlockPos(-1 , 0 , 0));
            roffsets.add(new BlockPos(0 , 0 ,1));
            roffsets.add(new BlockPos(0 , 0 , -1));
            if(redStoneType.getValue().equals("Block")) {
                roffsets.add(new BlockPos(0, 1, 0));
            }
            for(BlockPos roffset : roffsets)
            {
                BlockPos redstonePos = pistonPos.add(roffset);
                if(redstonePos.getX() == targetPos.getX() && redstonePos.getZ() == targetPos.getZ()) continue;
                if(checkPos(redstonePos)) continue;
                if(BlockUtil.getBlock(redstonePos) != Blocks.AIR) continue;
                redstonePoses.add(redstonePos);
            }
            BlockPos redstonePos = redstonePoses.stream()
                    .min(Comparator.comparing(b -> mc.player.getDistance(b.getX() , b.getY()  ,b.getZ())))
                    .orElse(null);
            if(redstonePos == null) continue;
            pos.setPiston(pistonPos);
            pos.setRedStone(redstonePos);

            poses.add(pos);
        }

        AutoPushPos bestPos = poses.stream()
                .filter(p -> p.getMaxRange() <= range.getValue())
                .min(Comparator.comparing(p -> p.getMaxRange())).orElse(null);
        if(bestPos != null)
        {
            pistonPos = bestPos.piston;
            redstonePos = bestPos.redstone;
            return true;
        }

        return false;
    }

    public EntityPlayer findTarget()
    {
        EntityPlayer target = null;
        List<EntityPlayer> players = mc.world.playerEntities;
        if(targetType.getValue().equals("Nearest"))
        {
            target = PlayerUtil.getNearestPlayer(targetRange.getValue());
        }
        if(targetType.getValue().equals("Looking"))
        {
            target = PlayerUtil.getLookingPlayer(targetRange.getValue());
        }
        if(targetType.getValue().equals("Best"))
        {
            target = players.stream()
                    .filter(p -> p.entityId != mc.player.entityId)
                    .filter(/*found space*/ p -> findSpace(p))
                    .min(Comparator.comparing(p -> PlayerUtil.getDistance(p))).orElse(null);
        }

        return target;

    }

    public boolean findMaterials()
    {
        pistonSlot = InventoryUtil.findHotbarBlock(Blocks.PISTON);
        int redstoneBlock = InventoryUtil.findHotbarBlock(Blocks.REDSTONE_BLOCK);
        int redstoneTorch = InventoryUtil.findHotbarBlock(Blocks.REDSTONE_TORCH);
        obbySlot = InventoryUtil.findHotbarBlock(Blocks.OBSIDIAN);



        if(itemCheck(pistonSlot))

            pistonSlot = InventoryUtil.findHotbarBlock(Blocks.STICKY_PISTON);


        if(redStoneType.getValue().equals("Block")) {
            isTorch = false;

            redstoneSlot = redstoneBlock;
        }

        if(redStoneType.getValue().equals("Torch")) {
            isTorch = true;

            redstoneSlot = redstoneTorch;
        }


        if(redStoneType.getValue().equals("Both")) {
            isTorch = true;
            redstoneSlot = redstoneTorch;
            if (itemCheck(redstoneSlot)) {
                isTorch = false;
                redstoneSlot = redstoneBlock;
            }
        }
        if(itemCheck(redstoneSlot) || itemCheck(pistonSlot) || itemCheck(obbySlot))
            return false;

        return true;
    }

    public boolean checkPos(BlockPos pos)
    {
        BlockPos mypos = new BlockPos(mc.player.posX , mc.player.posY , mc.player.posZ);
        return pos.getX() == mypos.getX() && pos.getZ() == mypos.getZ();
    }

    public boolean itemCheck(int slot)
    {
        return slot == -1;
    }


    public boolean prepareBlock()
    {

        BlockPos targetPos = new BlockPos(target.posX , target.posY , target.posZ);
        BlockPos piston = pistonPos.add(0 , -1 , 0);
        BlockPos redstone = redstonePos.add(0 , -1 , 0);

        if(BlockUtil.getBlock(piston) == Blocks.AIR)
        {

            setItem(obbySlot);
            BlockUtil.placeBlock(piston , packetPlace.getValue());

            if(delayCheck())
                return false;
        }

        if(BlockUtil.getBlock(redstone) == Blocks.AIR)
        {

            setItem(obbySlot);
            BlockUtil.placeBlock(redstone , packetPlace.getValue());

            if(delayCheck())
                return false;
        }

        return true;
    }

    public boolean delayCheck()
    {
        boolean hasDelay = !(preDelay.getValue() == 0);
        //if(hasDelay)
        //    updateItem();

        return hasDelay;
    }

    public class AutoPushPos
    {
        public BlockPos piston;
        public BlockPos redstone;

        public double getMaxRange()
        {
            if(piston == null || redstone == null) return 999999;
            return Math.max(PlayerUtil.getDistance(piston) , PlayerUtil.getDistance(redstone));
        }

        public void setPiston(BlockPos piston)
        {
            this.piston = piston;
        }

        public void setRedStone(BlockPos redstone)
        {
            this.redstone = redstone;
        }
    }
    public boolean isPistonTriggered(BlockPos pos) {
        return isRedstone(pos.north()) || isRedstone(pos.east()) || isRedstone(pos.south()) || isRedstone(pos.west()) || isRedstone(pos.up());
    }
    public boolean isRedstone(BlockPos pos) {
        return mc.world.getBlockState(pos).getBlock().equals(Blocks.REDSTONE_BLOCK);
    }
//    @Override
//    public void onRenderWorld(final RenderEvent event) {
//        if (this.pistonPos != null && this.pistonPos != null) {
//            if (BlockInteractionHelper.isAirBlock(this.pistonPos)) {
//                RenderUtils3D.drawFullBox(this.pistonPos, 1.0f, 255, 0, 0, 50);
//                RenderUtils3D.drawFullBox(this.pistonPos.up(), 1.0f, 255, 0, 0, 50);
//            }
//            else {
//                RenderUtils3D.drawFullBox(this.pistonPos, 1.0f, 0, 255, 0, 50);
//                RenderUtils3D.drawFullBox(this.pistonPos.up(), 1.0f, 0, 255, 0, 50);
//            }
//        }
//    }
}
