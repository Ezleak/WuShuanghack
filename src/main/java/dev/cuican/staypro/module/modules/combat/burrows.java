package dev.cuican.staypro.module.modules.combat;

import com.mojang.realmsclient.gui.ChatFormatting;
import dev.cuican.staypro.Stay;
import dev.cuican.staypro.client.CommandManager;
import dev.cuican.staypro.command.Command;
import dev.cuican.staypro.common.annotations.ModuleInfo;
import dev.cuican.staypro.event.events.network.PacketEvent;
import dev.cuican.staypro.module.Category;
import dev.cuican.staypro.module.Module;
import dev.cuican.staypro.notification.NotificationManager;
import dev.cuican.staypro.setting.Setting;
import dev.cuican.staypro.utils.*;
import dev.cuican.staypro.utils.block.BlockUtil;
import dev.cuican.staypro.utils.inventory.InventoryUtil;
import dev.cuican.staypro.utils.position.PositionUtil;
import dev.cuican.staypro.utils.tool.SeijaBlockUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemTool;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@ModuleInfo(name = "StayProBurrowMax+", category = Category.COMBAT, description = "Stable 3 grid burrow")
public class burrows extends Module {
    private boolean isSneaking=false;

    private final Setting<Boolean> breakCrystal = setting("BreakCrystal", true);
    private final Setting<Boolean> onlyOnGround = setting("OnlyOnGround", true);
    private final Setting<Boolean> multiPlace = setting("MultiPlace", true);
    private final Setting<Boolean> rotate = setting("Rotate", true);
    private final Setting<Boolean> smartOffset = setting("SmartOffset", true);
    public Setting<Boolean> tpcenter = setting("TPCenter", false);

    private final Setting<Double> offsetX =setting("OffsetX", -7.0D, -10.0D, 10.0D);
    private final Setting<Double> offsetY =setting("OffsetY", -7.0D, -10.0D, 10.0D);
    private final Setting<Double> offsetZ =setting("OffsetZ", -7.0D, -10.0D, 10.0D);

    public Setting<String> mode = setting("BlockMode","Obsidian",listOf("Chest", "Smart", "Obsidian"));
    private final Setting<Boolean> fristObby = setting("FristObby", true);
    public void onDisable() {
        this.isSneaking = EntityUtil.stopSneaking(this.isSneaking);
    }
    public static int getSlotByDmg(Double minDmg) {
        for (int i = 0; i < 9; i++) {
            if (mc.player.inventory.getStackInSlot(i).getItem() instanceof ItemTool) {
                ItemTool currItemTool = (ItemTool) mc.player.inventory.getStackInSlot(i).getItem();
                if (currItemTool.attackDamage >= minDmg) return i;
            }
        }
        return -1;
    }
    public int teleportID;

    @Override
    public void onTick() {

        //if (burBind.getValue().isDown())
        //timer.reset();
        if (onlyOnGround.getValue() & (!mc.player.onGround | mc.world.getBlockState(SeijaBlockUtil.getFlooredPosition(mc.player).offset(EnumFacing.DOWN)).getBlock().equals(Blocks.AIR)))
            return;
        this.isSneaking = EntityUtil.stopSneaking(this.isSneaking);

        if (((Boolean) this.breakCrystal.getValue()).booleanValue()) {
            burrows burrow2 = this;
            if (breakCrystal.getValue())BurrowUtil.back();
        }
        if (!Wrapper.mc.world.isBlockLoaded(Wrapper.mc.player.getPosition())) {
            return;
        }
//        if (!Util.mc.player.onGround || Util.mc.world.getBlockState(new BlockPos(Util.mc.player.posX, Util.mc.player.posY + 2.0, Util.mc.player.posZ)).getBlock() != Blocks.AIR /*|| !Util.mc.world.getEntitiesWithinAABB((Class)EntityEnderCrystal.class, new AxisAlignedBB(new BlockPos(Util.mc.player.posX, Util.mc.player.posY, Util.mc.player.posZ))).isEmpty()*/) {
//            this.disable();
//            return;
//        }
//        if (Util.mc.world.getBlockState(new BlockPos(Util.mc.player.posX, Math.round(Util.mc.player.posY), Util.mc.player.posZ)).getBlock() != Blocks.AIR /*||Util.mc.world.getBlockState(new BlockPos(Util.mc.player.posX, Util.mc.player.posY, Util.mc.player.posZ)).getBlock()==Blocks.ENDER_CHEST*/) {
//            this.disable();
//            return;
//        }
        if (mode.getValue().equals("Obsidian"))
            if (InventoryUtil.getItemHotbar(Item.getItemFromBlock(Blocks.OBSIDIAN)) == -1) {
                ChatUtil.printChatMessage(ChatFormatting.RED + "Obsidian ?");
                this.disable();
                return;
            }
        if (mode.getValue().equals("Chest"))
            if (InventoryUtil.getItemHotbar(Item.getItemFromBlock(Blocks.ENDER_CHEST)) == -1) {
                ChatUtil.printChatMessage(ChatFormatting.RED + "Ender Chest ?");
                this.disable();
                return;
            }
        if (mode.getValue().equals("Smart")) {
            if (InventoryUtil.getItemHotbar(Item.getItemFromBlock(Blocks.OBSIDIAN)) == -1) {
                if (InventoryUtil.getItemHotbar(Item.getItemFromBlock(Blocks.ENDER_CHEST)) == -1) {
                    ChatUtil.printChatMessage(ChatFormatting.RED + "Obsidian/Ender Chest ?");
                    this.disable();
                    return;
                }
            }
        }
        if (tpcenter.getValue().booleanValue()) {
            BlockPos startPos = EntityUtil.getRoundedBlockPos(Surround.mc.player);
            CommandManager.setPositionPacket((double) startPos.getX() + 0.5, startPos.getY(), (double) startPos.getZ() + 0.5, true, true, true);
        }
        if (!SeijaBlockUtil.fakeBBoxCheck(mc.player, new Vec3d(0, 0, 0), true)) {

            BlockPos pos = getOffsetBlock(mc.player);
            if (!mc.world.getBlockState(SeijaBlockUtil.getFlooredPosition(mc.player).offset(EnumFacing.UP,2)).getBlock().equals(Blocks.AIR)){
                for (EnumFacing facing:EnumFacing.VALUES){
                    if (facing==EnumFacing.UP||facing==EnumFacing.DOWN)continue;
                    BlockPos offPos = SeijaBlockUtil.getFlooredPosition(mc.player).offset(facing);
                    if (BlockUtil.isAir(offPos)&&BlockUtil.isAir(offPos.offset(EnumFacing.UP))){
                        Wrapper.mc.player.connection.sendPacket((Packet) new CPacketPlayer.Position(mc.player.posX+(offPos.getX()+0.5-mc.player.posX)/2, Wrapper.mc.player.posY +0.188383748, mc.player.posZ+(offPos.getZ()+0.5-mc.player.posZ)/2, false));
                        Wrapper.mc.player.connection.sendPacket((Packet) new CPacketPlayer.Position(mc.player.posX+(offPos.getX()+0.5-mc.player.posX), Wrapper.mc.player.posY+0.123232, mc.player.posZ+(offPos.getZ()+0.5-mc.player.posZ), false));

                    }
                }
            }else if (pos != null&&mc.world.getBlockState(SeijaBlockUtil.getFlooredPosition(mc.player).offset(EnumFacing.UP,2)).getBlock().equals(Blocks.AIR)) {
                double offX = pos.getX()+0.5-mc.player.posX;
                double offZ = pos.getZ()+0.5-mc.player.posZ;
                Wrapper.mc.player.connection.sendPacket((Packet) new CPacketPlayer.Position(/*Math.floor(mc.player.posX)*/mc.player.posX+offX*0.25, Wrapper.mc.player.posY + 0.419999986886978, mc.player.posZ+offZ*0.25, false));
                Wrapper.mc.player.connection.sendPacket((Packet) new CPacketPlayer.Position(mc.player.posX+offX*0.5, Wrapper.mc.player.posY + 0.7531999805212015, mc.player.posZ+offZ*0.5, false));
                Wrapper.mc.player.connection.sendPacket((Packet) new CPacketPlayer.Position(mc.player.posX+offX*0.75, Wrapper.mc.player.posY + 1.001335979112147, mc.player.posZ+offZ*0.75, false));
                Wrapper.mc.player.connection.sendPacket((Packet) new CPacketPlayer.Position(pos.getX() + 0.5, Wrapper.mc.player.posY + 1.166109260938214, pos.getZ() + 0.5, false));
            }else  {
                disable();
                return;
            }
        } else {
            Wrapper.mc.player.connection.sendPacket((Packet) new CPacketPlayer.Position(mc.player.posX, Wrapper.mc.player.posY + 0.419999986886978, mc.player.posZ, false));
            Wrapper.mc.player.connection.sendPacket((Packet) new CPacketPlayer.Position(mc.player.posX, Wrapper.mc.player.posY + 0.7531999805212015, mc.player.posZ, false));
            Wrapper.mc.player.connection.sendPacket((Packet) new CPacketPlayer.Position(mc.player.posX, Wrapper.mc.player.posY + 1.001335979112147, mc.player.posZ, false));
            Wrapper.mc.player.connection.sendPacket((Packet) new CPacketPlayer.Position(mc.player.posX, Wrapper.mc.player.posY + 1.166109260938214, mc.player.posZ, false));
        }
        final int a = Wrapper.mc.player.inventory.currentItem;
        if (mode.getValue().equals("Obsidian")) {
            InventoryUtil.switchToHotbarSlot(InventoryUtil.getItemHotbar(Item.getItemFromBlock(Blocks.OBSIDIAN)), false);
        }
        if (mode.getValue().equals("Chest")) {
            InventoryUtil.switchToHotbarSlot(InventoryUtil.getItemHotbar(Item.getItemFromBlock(Blocks.ENDER_CHEST)), false);
        }
        if (mode.getValue().equals("Chest")) {
            if (fristObby.getValue()) {
                if (InventoryUtil.getItemHotbar(Item.getItemFromBlock(Blocks.OBSIDIAN)) != -1) {
                    InventoryUtil.switchToHotbarSlot(InventoryUtil.getItemHotbar(Item.getItemFromBlock(Blocks.OBSIDIAN)), false);
                } else if (InventoryUtil.getItemHotbar(Item.getItemFromBlock(Blocks.ENDER_CHEST)) != -1) {
                    InventoryUtil.switchToHotbarSlot(InventoryUtil.getItemHotbar(Item.getItemFromBlock(Blocks.ENDER_CHEST)), false);
                }
            } else {
                if (InventoryUtil.getItemHotbar(Item.getItemFromBlock(Blocks.ENDER_CHEST)) != -1) {
                    InventoryUtil.switchToHotbarSlot(InventoryUtil.getItemHotbar(Item.getItemFromBlock(Blocks.ENDER_CHEST)), false);
                } else if (InventoryUtil.getItemHotbar(Item.getItemFromBlock(Blocks.OBSIDIAN)) != -1) {
                    InventoryUtil.switchToHotbarSlot(InventoryUtil.getItemHotbar(Item.getItemFromBlock(Blocks.OBSIDIAN)), false);
                }
            }
        }
        if (!multiPlace.getValue()) {
            this.isSneaking = BlockUtil.placeBlock(new BlockPos((Vec3i) getPlayerPosFixY((EntityPlayer) Burrow.mc.player)), EnumHand.MAIN_HAND, this.rotate.getValue(), true, this.isSneaking);
        } else {
            Vec3d baseVec = new Vec3d(mc.player.posX, mc.player.posY, mc.player.posZ);
            if (!SeijaBlockUtil.isAir(baseVec.add(0.3, -1, 0.3)))
                SeijaBlockUtil.placeBlock(baseVec.add(0.3, 0, 0.3), EnumHand.MAIN_HAND, false, true);
            if (!SeijaBlockUtil.isAir(baseVec.add(-0.3, -1, 0.3)))
                SeijaBlockUtil.placeBlock(baseVec.add(-0.3, 0, 0.3), EnumHand.MAIN_HAND, false, true);
            if (!SeijaBlockUtil.isAir(baseVec.add(0.3, -1, -0.3)))
                SeijaBlockUtil.placeBlock(baseVec.add(0.3, 0, -0.3), EnumHand.MAIN_HAND, false, true);
            if (!SeijaBlockUtil.isAir(baseVec.add(-0.3, -1, -0.3)))
                SeijaBlockUtil.placeBlock(baseVec.add(-0.3, 0, -0.3), EnumHand.MAIN_HAND, false, true);
        }

        Wrapper.mc.playerController.updateController();
        Wrapper.mc.player.connection.sendPacket((Packet) new CPacketHeldItemChange(a));
        Wrapper.mc.player.inventory.currentItem = a;
        Wrapper.mc.playerController.updateController();
        this.mc.player.connection.sendPacket((Packet) new CPacketPlayerTryUseItemOnBlock(new BlockPos(this.mc.player.posX, this.mc.player.posY - 1.0, this.mc.player.posZ), EnumFacing.UP, EnumHand.MAIN_HAND, 0.0f, 0.0f, 0.0f));

        if (smartOffset.getValue()) {
            boolean defaultOffset = true;
            if (mc.player.posY >= 3) {
                for (int i = -10; i < 10; i++) {
                    if (i == -1)
                        i = 4;
                    if (mc.world.getBlockState(SeijaBlockUtil.getFlooredPosition(mc.player).add(0, i, 0)).getBlock().equals(Blocks.AIR)
                            && mc.world.getBlockState(SeijaBlockUtil.getFlooredPosition(mc.player).add(0, i + 1, 0)).getBlock().equals(Blocks.AIR)
                    ) {
                        BlockPos pos = SeijaBlockUtil.getFlooredPosition(mc.player).add(0, i, 0);
                        Wrapper.mc.player.connection.sendPacket((Packet) new CPacketPlayer.Position(pos.getX() + 0.3, pos.getY(), pos.getZ() + 0.3, false));
                        defaultOffset = false;
                        break;
                    }
                }
            }

            if (defaultOffset)
                Wrapper.mc.player.connection.sendPacket((Packet) new CPacketPlayer.Position(Wrapper.mc.player.posX + this.offsetX.getValue(), Wrapper.mc.player.posY + this.offsetY.getValue(), Wrapper.mc.player.posZ + offsetZ.getValue(), false));

        } else {
            Wrapper.mc.player.connection.sendPacket((Packet) new CPacketPlayer.Position(Wrapper.mc.player.posX + this.offsetX.getValue(), Wrapper.mc.player.posY + this.offsetY.getValue(), Wrapper.mc.player.posZ + offsetZ.getValue(), false));
            //mc.player.move(MoverType.PLAYER,0,1000,0);
        }
        this.disable();
    }

    public static BlockPos getPlayerPosFixY(EntityPlayer player) {
        return new BlockPos(Math.floor(player.posX), Math.round(player.posY), Math.floor(player.posZ));
    }

    public BlockPos getOffsetBlock(EntityPlayer player) {
        Vec3d vec3d1 = new Vec3d(player.boundingBox.minX, player.boundingBox.minY, player.boundingBox.minZ);
        if (canBur(vec3d1)) return SeijaBlockUtil.vec3toBlockPos(vec3d1);
        Vec3d vec3d2 = new Vec3d(player.boundingBox.maxX, player.boundingBox.minY, player.boundingBox.minZ);
        if (canBur(vec3d2)) return SeijaBlockUtil.vec3toBlockPos(vec3d2);
        Vec3d vec3d3 = new Vec3d(player.boundingBox.minX, player.boundingBox.minY, player.boundingBox.maxZ);
        if (canBur(vec3d3)) return SeijaBlockUtil.vec3toBlockPos(vec3d3);
        Vec3d vec3d4 = new Vec3d(player.boundingBox.maxX, player.boundingBox.minY, player.boundingBox.maxZ);
        if (canBur(vec3d4)) return SeijaBlockUtil.vec3toBlockPos(vec3d4);
        return null;

    }

    public boolean canBur(Vec3d vec3d) {
        BlockPos pos = SeijaBlockUtil.vec3toBlockPos(vec3d);
        return BlockUtil.isAir(pos) && BlockUtil.isAir(pos.offset(EnumFacing.UP)) && BlockUtil.isAir(pos.offset(EnumFacing.UP, 2));
    }
    @Override
    public void onRenderTick() {
        if (breakCrystal.getValue())BurrowUtil.back();
    }



}

