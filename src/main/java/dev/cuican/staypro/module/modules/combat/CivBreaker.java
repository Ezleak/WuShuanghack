package dev.cuican.staypro.module.modules.combat;


import dev.cuican.staypro.Stay;
import dev.cuican.staypro.client.ModuleManager;
import dev.cuican.staypro.common.annotations.ModuleInfo;
import dev.cuican.staypro.module.Category;
import dev.cuican.staypro.module.Module;
import dev.cuican.staypro.module.modules.player.Instant;
import dev.cuican.staypro.setting.Setting;
import dev.cuican.staypro.utils.*;
import dev.cuican.staypro.utils.block.BlockUtil;
import dev.cuican.staypro.utils.graphics.RenderUtils3D;
import dev.cuican.staypro.utils.graphics.StayTessellator;
import dev.cuican.staypro.utils.inventory.InventoryUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
@ModuleInfo(name = "CivBreaker", category = Category.COMBAT)

public class CivBreaker extends Module {
    public Setting<Float> preDelay = setting("BlockDelay", 0f, 0f, 20f);
    public Setting<Float> crystalDelay = setting("CrystalDelay", 0f, 0f, 20f);
    public Setting<Float> breakDelay = setting("BreakDelay", 0f, 0f, 20f);
    public Setting<Float> attackDelay = setting("AttackDelay", 3.0f, 0f, 20f);
    public Setting<Float> endDelay = setting("EndDelay", 0f, 0f, 20f);
    public Setting<Float> range = setting("Range", 10f, 1f, 20f);

    public Setting<Boolean> tick = setting("Tick", true);
    public Setting<Boolean> toggle = setting("Toggle", true);
    public Setting<Boolean> noSwingBlock = setting("NoSwingBlock", true);
    public Setting<Boolean> silentSwitch = setting("SilentSwitch", false);
    public Setting<Boolean> packetCrystal = setting("PacketCrystal", true);
    public Setting<Boolean> instantBreak = setting("InstantBreak", false);
    public Setting<Boolean> toggleSilentPickel = setting("ToggleSilentPickel", true);
    public Setting<Boolean> packetBreak = setting("PacketBreak", true);
    public Setting<Boolean> offHandBreak = setting("OffhandBreak", true);
    public Setting<Boolean> skip = setting("Skip", false);

    public Setting<Integer> breakAttempts = setting("BreakAttempts", 7, 1, 20);
    public Setting<Float> targetRange = setting("Target Range", 10f, 0f, 20f);

    public Setting<String> targetType = setting("Target","Nearest",listOf("Nearest", "Looking", "Best"));

    public Setting<Boolean> render = setting("Render", true);
    public Setting<Integer> red = setting("Red", 255, 0, 255).whenTrue(render);
    public Setting<Integer> green = setting("Green", 255, 0, 255).whenTrue(render);
    public Setting<Integer> blue = setting("Blue", 255, 0, 255).whenTrue(render);

    public Setting<Boolean> line = setting("Line", false).whenTrue(render);
    public Setting<Float> width = setting("Line Width", 2f, 0.1f, 5f).whenTrue(render).whenTrue(line);


    public BlockPos base , old = null;
    public boolean builtTrap , placedCrystal , brokeBlock , attackedCrystal , done = false;
    public int crystalSlot , obbySlot , pickelSlot = -1;
    public int attempts = 0;
    public static EntityPlayer target = null;
    public Timer blockTimer , crystalTimer , breakTimer , attackTimer , endTimer = null;
    public static boolean breaking = false;
    public EnumHand oldhand = null;
    public int oldslot = -1;


    //target type
    public enum Target
    {
        Nearest ,
        Looking ,
        Best
    };

    @Override
    public void onEnable()
    {
        reset();
        if(toggleSilentPickel.getValue())
            setToggleSilentPickel(true);
    }

    @Override
    public void onDisable()
    {
        if(toggleSilentPickel.getValue())
            setToggleSilentPickel(false);
    }

    public void setToggleSilentPickel(boolean toggle)
    {
        if(toggle)
            ModuleManager.getModuleByName("SilentPickel").enable();
        else
            ModuleManager.getModuleByName("SilentPickel").disable();
    }

    @Override
    public void onTick()
    {
        if(tick.getValue())doCV();
    }

    @Override
    public void onUpdate()
    {
        if(!tick.getValue())doCV();
    }

    public void doCV()
    {
		if(nullCheck()) return;
        try {
            if (!findMaterials()) {
                if (toggle.getValue()) {
                    ChatUtil.NoSpam.sendRawChatMessage("Cannot find materials! disabling...");
                    disable();
                }
                //very important
                return;
            }
            //trying find target
            target = findTarget();
            if (isNull(target)) {
                if (toggle.getValue()) {
                    ChatUtil.NoSpam.sendRawChatMessage("Cannot find target! disabling...");
                    disable();
                }
                //very important
                return;
            }
            if (isNull(base)) {
                if (!findSpace(target)) {
                    if (toggle.getValue()) {
                        ChatUtil.NoSpam.sendRawChatMessage("Cannot find space! disabling...");
                        disable();
                    }
                    //very important
                    return;
                }
            }

            BlockPos targetPos = new BlockPos(target.posX , target.posY , target.posZ);
            if(blockTimer == null)
            {
                blockTimer = new Timer();
            }
            if(!builtTrap)
            {
                if(BlockUtil.getBlock(base) == Blocks.AIR)
                {
                    if(blockTimer.passedX(preDelay.getValue()))
                    {
                        setItem(obbySlot);
                        placeBlock(base, false);
                        blockTimer = null;
                        builtTrap = true;
                    }
                }

            }

            if(builtTrap && (!base.equals(old) && skip.getValue()))
            {
                placedCrystal = true;
            }
            if(crystalTimer == null && builtTrap)
            {
                crystalTimer = new Timer();
            }
            if(builtTrap && !placedCrystal)
            {
                if(crystalTimer.passedX(crystalDelay.getValue()))
                {
                    if (crystalSlot != 999) {
                        setItem(crystalSlot);
                    }

                    EnumHand hand = crystalSlot != 999 ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND;
                    if (packetCrystal.getValue()) {
                        mc.player.connection.sendPacket(
                                new CPacketPlayerTryUseItemOnBlock(base, EnumFacing.DOWN, hand, 0, 0, 0));
                    } else {
                        mc.playerController.processRightClickBlock(mc.player, mc.world, base, EnumFacing.DOWN, new Vec3d(0, 0, 0), hand);
                    }
                    placedCrystal = true;
                }
            }


            if(breakTimer == null && placedCrystal)
            {
                breakTimer = new Timer();
            }
            if(placedCrystal && !brokeBlock)
            {
                if(breakTimer.passedX(breakDelay.getValue()))
                {
                    setItem(pickelSlot);

                    if(BlockUtil.getBlock(base) == Blocks.AIR)
                    {
                        brokeBlock = true;
                    }

                    if(!breaking) {
                        if(!instantBreak.getValue())
                        {
                            if (!noSwingBlock.getValue()) {
                                mc.player.swingArm(EnumHand.MAIN_HAND);
                            }
                            mc.player.connection.sendPacket(new CPacketPlayerDigging(
                                    CPacketPlayerDigging.Action.START_DESTROY_BLOCK, base, EnumFacing.DOWN
                            ));
                            mc.player.connection.sendPacket(new CPacketPlayerDigging(
                                    CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, base, EnumFacing.DOWN
                            ));
                        }
                        else
                        {
                            if(!noSwingBlock.getValue()) {
                                mc.player.swingArm(EnumHand.MAIN_HAND);
                            }
                            ModuleManager.getModuleByName("Instant").enable();
                            Instant.ondeve(base);
                        }
                        breaking = true;
                    }

                }
            }

            if(brokeBlock && (!base.equals(old) && skip.getValue())) attackedCrystal = true;
            if(attackTimer == null && brokeBlock)
            {
                attackTimer = new Timer();
            }
            if(brokeBlock && !attackedCrystal)
            {
                breaking = false;
                if(attackTimer.passedX(attackDelay.getValue()))
                {
                    BlockPos plannedCrystalPos = base.add(0 , 1 , 0);
                    Entity crystal = mc.world.loadedEntityList.stream()
                            .filter(e -> e instanceof EntityEnderCrystal)
                            .filter(e -> new BlockPos(e.posX, e.posY , e.posZ).getDistance(plannedCrystalPos.getX() , plannedCrystalPos.getY() , plannedCrystalPos.getZ()) < 1.5)
                            .min(Comparator.comparing(c -> c.getDistance(target))).orElse(null);
                    if(crystal == null)
                    {
                        if(attempts < breakAttempts.getValue())
                        {
                            reset();
                            return;
                        }

                        attempts++;
                        return;
                    }

                    EnumHand hand = offHandBreak.getValue() ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND;
                    if (packetBreak.getValue()) {
                        mc.player.connection.sendPacket(new CPacketUseEntity(crystal));
                    } else {
                        mc.playerController.attackEntity(mc.player, crystal);
                        mc.player.swingArm(hand);
                    }

                    attackedCrystal = true;
                }
            }

            if(endTimer == null && attackedCrystal)
            {
                endTimer = new Timer();
            }
            if(attackedCrystal && !done)
            {
                if(endTimer.passedX(endDelay.getValue()))
                {
                    done = true;
                    old = new BlockPos(base.getX() , base.getY() , base.getZ());
                    reset();
                }
            }

            restoreItem();

        }
        catch (Exception e){}

    }

    public void reset()
    {
        base = null;
        builtTrap = false;
        placedCrystal = false;
        brokeBlock = false;
        attackedCrystal = false;
        done = false;
        crystalSlot = -1;
        obbySlot = -1;
        pickelSlot = -1;
        attempts = 0;
        target = null;
        blockTimer = null;
        crystalTimer = null;
        breakTimer = null;
        attackTimer = null;
        endTimer = null;
        breaking = false;
        oldslot = -1;
        oldhand = null;
    }

    @Override
    public void onRender3D()
    {
        try {
            if(isNull(target))
            {
                return;
            }

            BlockPos headPos = base;

            if(line.getValue()) {
                StayTessellator.drawBoundingBox(headPos , 1 ,red.getValue(),green.getValue(),blue.getValue(), 200);
            }
            else {
                StayTessellator.drawBox(headPos , 1 , red.getValue(),green.getValue(),blue.getValue() , GeometryMasks.Quad.ALL);
            }
        }
        catch(Exception e){}
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

    }

    public boolean findSpace(EntityPlayer player)
    {
        BlockPos targetPos = new BlockPos(player.posX , player.posY , player.posZ);
        BlockPos mypos = new BlockPos(mc.player.posX , mc.player.posY , mc.player.posZ);

        BlockPos[] offsets = new BlockPos[]
                {
                        new BlockPos(1 , 0 , 0 ) ,
                        new BlockPos(-1 , 0 , 0) ,
                        new BlockPos(0 , 0 , 1) ,
                        new BlockPos(0 , 0 , -1),
                        //update moment
                        //new BlockPos(1 , 0 , 1 ) ,
                        //new BlockPos(-1 , 0 , 1 ) ,
                        //new BlockPos(1 , 0 , -1 ) ,
                        //new BlockPos(-1 , 0 , -1 ) ,
                };
        java.util.List<BlockPos> posess = new ArrayList<>();

        for (BlockPos offset:
                offsets) {
            BlockPos basePos = targetPos.add(offset);
            if(!(BlockUtil.getBlock(basePos) == Blocks.OBSIDIAN || BlockUtil.getBlock(basePos) == Blocks.BEDROCK) ||
                    (!BlockUtil.canPlaceBlockFuture(basePos.add(0 , 1 , 0)) && !(BlockUtil.getBlock(basePos.add(0 , 1 , 0)) == Blocks.OBSIDIAN)) ||
                    (!CrystalUtil.canPlaceCrystal(basePos.add(0 , 1 , 0)))) {
                continue;
            }
            posess.add(basePos.add(0 , 1 , 0));
        }

        base = posess.stream()
                .filter(p -> mc.player.getDistance(p.getX() , p.getY() , p.getZ()) <= range.getValue())
                .max(Comparator.comparing(p ->  PlayerUtil.getDistanceI(p))).orElse(null);
        if(base == null)
        {
            return false;
        }
        return true;
    }

    //search target
    public EntityPlayer findTarget()
    {
        EntityPlayer target = null;
        //players
        java.util.List<EntityPlayer> players = mc.world.playerEntities;
        //nearest
        if(targetType.getValue().equals("Nearest"))
        {
            //search nearest player
            target = PlayerUtil.getNearestPlayer(targetRange.getValue());
        }
        //looking
        if(targetType.getValue().equals("Looking"))
        {
            //search looking player
            target = PlayerUtil.getLookingPlayer(targetRange.getValue());
        }
        //best
        if(targetType.getValue().equals("Best"))
        {
            //loop
            target = players.stream().filter(/*found space*/ p -> findSpace(p))
                    .min(Comparator.comparing(p -> PlayerUtil.getDistance(p))).orElse(null);
        }

        return target;

    }

    //find slot
    public boolean findMaterials()
    {
        crystalSlot = InventoryUtil.getItemHotbar(Items.END_CRYSTAL);
        obbySlot = InventoryUtil.findHotbarBlock(Blocks.OBSIDIAN);
        pickelSlot = InventoryUtil.getItemHotbar(Items.DIAMOND_PICKAXE);

        if(itemCheck(crystalSlot))
        {
            if(mc.player.getHeldItemOffhand().getItem() == Items.END_CRYSTAL)
            {
                crystalSlot = 999;
            }
        }

        //check
        if(itemCheck(crystalSlot) || itemCheck(obbySlot) || itemCheck(pickelSlot))
            return false;

        return true;
    }

    public boolean itemCheck(int slot)
    {
        return slot == -1;
    }

    public boolean isNull(Object o)
    {
        return o == null;
    }

    public void placeBlock(BlockPos pos , Boolean packet)
    {
        BlockUtil.placeBlock(pos , packet);
        if(!noSwingBlock.getValue()) {
            mc.player.swingArm(EnumHand.MAIN_HAND);
        }
    }
}
