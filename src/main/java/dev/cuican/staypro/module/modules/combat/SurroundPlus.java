package dev.cuican.staypro.module.modules.combat;



import dev.cuican.staypro.common.annotations.ModuleInfo;
import dev.cuican.staypro.event.events.client.EventPreMotion;
import dev.cuican.staypro.event.events.client.PacketEvents;
import dev.cuican.staypro.module.Category;
import dev.cuican.staypro.module.Module;
import dev.cuican.staypro.setting.Setting;
import dev.cuican.staypro.utils.ChatUtil;
import dev.cuican.staypro.utils.PlayerUtil;
import dev.cuican.staypro.utils.Timer;
import dev.cuican.staypro.utils.block.BlockUtil;
import dev.cuican.staypro.utils.inventory.InventoryUtil;
import jdk.nashorn.internal.ir.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.*;
@ModuleInfo(name = "SurroundPlus", category = Category.COMBAT, description = "Surrounds you with Obsidian")

public class SurroundPlus extends Module {

    public Setting<Boolean> alertPlayerClip = setting("AlertBreaking", false);
    public Setting<Boolean> destroyAboveCrystal = setting("DestroyAboveCrys", false);
    public Setting<Boolean> destroyCrystal = setting("DestroyCrys", false);
    public Setting<Boolean> rotate = setting("Rotate", false);
    public Setting<Boolean> predict = setting("Predict", false);
    public Setting<Boolean> onlyOnSneak = setting("OnlyonSneak", false);
    public Setting<Boolean> disableOnJump = setting("DisableOnJump", false);
    public Setting<Boolean> onlyOnStop = setting("OnStop", false);
    public Setting<Boolean> allowNon1x1 = setting("Allownon1x1", false);
    public Setting<Boolean> centre = setting("Centre", false).whenTrue(allowNon1x1);



    public Setting<Integer> afterRotate = setting("PostSecure", 3, 0, 5).whenTrue(rotate);
    public Setting<Integer> blocksPerTick = setting("BlocksPerTick", 4, 1, 20);
    public Setting<Integer> delayTicks = setting("TickDelay", 3, 0, 10);

    ArrayList<BlockPos> blockChanged = new ArrayList<>();
    public Timer alertDelay = new Timer();
    public Timer delayTimer = new Timer();
    int y;

    @SubscribeEvent
    public void onPacketReceive(PacketEvents.Receive event){

        if(fullNullCheck()){
            return;
        }
        if (event.getPacket() instanceof SPacketBlockChange && this.predict.getValue()) {
            SPacketBlockChange packet = (SPacketBlockChange) event.getPacket();
            if (!blockChanged.contains(packet.getBlockPosition())) {
                for (BlockPos pos : this.getOffsets()) {
                    if (!pos.equals(packet.getBlockPosition()) || packet.getBlockState().getBlock() != Blocks.AIR)
                        continue;
                    int blockSlot = this.getSlot();
                    if (blockSlot == -1) {
                        return;
                    }
                    if (blockSlot != mc.player.inventory.currentItem)
                        mc.player.connection.sendPacket(new CPacketHeldItemChange(blockSlot));
                    BlockUtil.place(pos, EnumHand.MAIN_HAND, false, false);
                    if (blockSlot != mc.player.inventory.currentItem) {
                        mc.player.connection.sendPacket(new CPacketHeldItemChange(mc.player.inventory.currentItem));
                        mc.playerController.updateController();
                    }
                    blockChanged.add(pos);
                    break;
                }
            }
        }
    }

    boolean hasPlaced;
    int lookDown = -1;

    @SubscribeEvent
    public void onUpdateWalkingPlayer(EventPreMotion event){
        if (mc.player == null || mc.world == null || lookDown == -1)
            return;
        mc.player.rotationPitch = (90);
        mc.player.rotationYaw =(0);
        lookDown--;

    }

    int getSlot() {
        int slot = InventoryUtil.findFirstBlockSlot(Blocks.OBSIDIAN.getClass(), 0, 8);
        ;
        if (slot == -1) {
            slot = InventoryUtil.findFirstBlockSlot(Blocks.ENDER_CHEST.getClass(), 0, 8);
            ;
        }
        return slot;
    }
    @Override
    public void onEnable() {
        alertDelay.reset();
        y = (int) Math.floor(mc.player.posY);
    }

    @SubscribeEvent
    public void onUpdate(EventPreMotion e) {

        if (mc.player == null || mc.world == null)
            return;

        if (onlyOnStop.getValue() && (mc.player.motionX != 0 || mc.player.motionY != 0 || mc.player.motionZ != 0))
            return;

        if (onlyOnSneak.getValue() && !mc.gameSettings.keyBindSneak.isPressed())
            return;

        if (disableOnJump.getValue() && Math.abs(Math.abs(y) - Math.abs(mc.player.posY)) >= 0.3) {
            return;
        }
        if (delayTimer.getPassedTimeMs() / 50L >= delayTicks.getValue()) {
            delayTimer.reset();

            int blocksPlaced = 0;

            hasPlaced = false;

            List<BlockPos> offsetPattern = this.getOffsets();
            int maxSteps = offsetPattern.size();
            boolean hasSilentSwitched = false;
            int blockSlot = this.getSlot();
            if (blockSlot == -1)
                return;

            int offsetSteps = 0;
            if (centre.getValue() && !allowNon1x1.getValue()) {
                PlayerUtil.centerPlayer(mc.player.getPositionVector());
            }
            while (blocksPlaced <= blocksPerTick.getValue()) {

                if (offsetSteps >= maxSteps) {
                    break;
                }

                BlockPos targetPos = offsetPattern.get(offsetSteps++);

                if (blockChanged.contains(targetPos))
                    continue;

                mc.world.getEntitiesInAABBexcluding(null, new AxisAlignedBB(targetPos), null);
                boolean foundSomeone = false;
                for (Entity entity : mc.world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(targetPos))) {
                    if (entity instanceof EntityPlayer) {
                        foundSomeone = true;
                        if (alertPlayerClip.getValue() && entity != mc.player) {
                            if (alertDelay.passedMs(1000)) {
                                ChatUtil.NoSpam.sendRawChatMessage("Player " + entity.getName() + " is clipping in your surround");
                                alertDelay.reset();
                            }
                        }
                        break;
                    }
                    if (entity instanceof EntityEnderCrystal && destroyCrystal.getValue()) {
                        if (rotate.getValue()) {
                            rotateTo(new Vec3d(entity.getPosition()).add(0.5, 0, 0.5), e);
                        }
                        mc.player.connection.sendPacket(new CPacketUseEntity(entity));
                        mc.player.connection.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));
                    }
                }
                if (destroyAboveCrystal.getValue()) {
                    for (Entity entity : new ArrayList<>(mc.world.loadedEntityList)) {
                        if (entity instanceof EntityEnderCrystal) {
                            if (sameBlockPos(entity.getPosition(), targetPos)) {
                                if (rotate.getValue()) {
                                    rotateTo(new Vec3d(entity.getPosition()).add(0.5, 0, 0.5), e);
                                }
                                mc.player.connection.sendPacket(new CPacketUseEntity(entity));
                                mc.player.connection.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));
                            }
                        }
                    }
                }
                if (foundSomeone)
                    continue;

                if (!mc.world.getBlockState(targetPos).getMaterial().isReplaceable())
                    continue;

                if (!hasSilentSwitched) {
                    if (blockSlot != mc.player.inventory.currentItem) {
                        mc.player.connection.sendPacket(new CPacketHeldItemChange(blockSlot));
                        hasSilentSwitched = true;
                    }
                }

                if (BlockUtil.place(targetPos, EnumHand.MAIN_HAND, rotate.getValue(), false)) {
                    if (centre.getValue())
                        PlayerUtil.centerPlayer(mc.player.getPositionVector());
                    y = (int) Math.floor(mc.player.posY);
                    blocksPlaced++;
                    if (rotate.getValue())
                        if (afterRotate.getValue() != 0)
                            lookDown = afterRotate.getValue();
                }
            }
            if (hasSilentSwitched) {
                mc.player.connection.sendPacket(new CPacketHeldItemChange(mc.player.inventory.currentItem));
                mc.playerController.updateController();
            }

        }

        BlockUtil.stopSneaking();
        blockChanged.clear();


    }


    public static void rotateTo(Vec3d vec,EventPreMotion event) {
        float[] rotations = getNeededRotations2(vec);
        mc.player.rotationYaw =( rotations[0]);
        mc.player.rotationPitch =((float) MathHelper.normalizeAngle((int) rotations[1], 360));
    }

    public static float[] getNeededRotations2(Vec3d vec) {
        Vec3d eyesPos = new Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ);
        double diffX = vec.x - eyesPos.x;
        double diffY = vec.y - eyesPos.y;
        double diffZ = vec.z - eyesPos.z;
        double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);
        float yaw = (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90F;
        float pitch = (float) -Math.toDegrees(Math.atan2(diffY, diffXZ));
        return new float[]{mc.player.rotationYaw + MathHelper.wrapDegrees(yaw - mc.player.rotationYaw), mc.player.rotationPitch + MathHelper.wrapDegrees(pitch - mc.player.rotationPitch)};
    }


    // Say if two blockPos are the same
    boolean sameBlockPos(BlockPos first, BlockPos second) {
        if (first == null || second == null)
            return false;
        return first.getX() == second.getX() && first.getY() == second.getY() + 2 && first.getZ() == second.getZ();
    }


    List<BlockPos> getOffsets() {
        BlockPos playerPos = this.getPlayerPos();
        ArrayList<BlockPos> offsets = new ArrayList<BlockPos>();
        if (this.allowNon1x1.getValue()) {
            int z;
            int x;
            double decimalX = Math.abs(mc.player.posX) - Math.floor(Math.abs(mc.player.posX));
            double decimalZ = Math.abs(mc.player.posZ) - Math.floor(Math.abs(mc.player.posZ));
            int lengthXPos = this.calcLength(decimalX, false);
            int lengthXNeg = this.calcLength(decimalX, true);
            int lengthZPos = this.calcLength(decimalZ, false);
            int lengthZNeg = this.calcLength(decimalZ, true);
            ArrayList<BlockPos> tempOffsets = new ArrayList<BlockPos>();
            offsets.addAll(this.getOverlapPos());
            for (x = 1; x < lengthXPos + 1; ++x) {
                tempOffsets.add(this.addToPlayer(playerPos, x, 0.0, 1 + lengthZPos));
                tempOffsets.add(this.addToPlayer(playerPos, x, 0.0, -(1 + lengthZNeg)));
            }
            for (x = 0; x <= lengthXNeg; ++x) {
                tempOffsets.add(this.addToPlayer(playerPos, -x, 0.0, 1 + lengthZPos));
                tempOffsets.add(this.addToPlayer(playerPos, -x, 0.0, -(1 + lengthZNeg)));
            }
            for (z = 1; z < lengthZPos + 1; ++z) {
                tempOffsets.add(this.addToPlayer(playerPos, 1 + lengthXPos, 0.0, z));
                tempOffsets.add(this.addToPlayer(playerPos, -(1 + lengthXNeg), 0.0, z));
            }
            for (z = 0; z <= lengthZNeg; ++z) {
                tempOffsets.add(this.addToPlayer(playerPos, 1 + lengthXPos, 0.0, -z));
                tempOffsets.add(this.addToPlayer(playerPos, -(1 + lengthXNeg), 0.0, -z));
            }
            for (BlockPos pos : tempOffsets) {
                if (getDown(pos)) {
                    offsets.add(pos.add(0, -1, 0));
                }
                offsets.add(pos);
            }
        } else {
            offsets.add(playerPos.add(0, -1, 0));
            for (int[] surround : new int[][]{
                    {1, 0},
                    {0, 1},
                    {-1, 0},
                    {0, -1}
            }) {
                if (getDown(playerPos.add(surround[0], 0, surround[1])))
                    offsets.add(playerPos.add(surround[0], -1, surround[1]));

                offsets.add(playerPos.add(surround[0], 0, surround[1]));
            }
        }
        return offsets;
    }

    public static boolean getDown(BlockPos pos) {

        for (EnumFacing e : EnumFacing.values())
            if (!mc.world.isAirBlock(pos.add(e.getDirectionVec())))
                return false;

        return true;

    }

    int calcOffset(double dec) {
        return dec >= 0.7 ? 1 : (dec <= 0.3 ? -1 : 0);
    }

    BlockPos addToPlayer(BlockPos playerPos, double x, double y, double z) {
        if (playerPos.getX() < 0) {
            x = -x;
        }
        if (playerPos.getY() < 0) {
            y = -y;
        }
        if (playerPos.getZ() < 0) {
            z = -z;
        }
        return playerPos.add(x, y, z);
    }

    List<BlockPos> getOverlapPos() {
        ArrayList<BlockPos> positions = new ArrayList<BlockPos>();
        double decimalX = mc.player.posX - Math.floor(mc.player.posX);
        double decimalZ = mc.player.posZ - Math.floor(mc.player.posZ);
        int offX = this.calcOffset(decimalX);
        int offZ = this.calcOffset(decimalZ);
        positions.add(this.getPlayerPos());
        for (int x = 0; x <= Math.abs(offX); ++x) {
            for (int z = 0; z <= Math.abs(offZ); ++z) {
                int properX = x * offX;
                int properZ = z * offZ;
                positions.add(this.getPlayerPos().add(properX, -1, properZ));
            }
        }
        return positions;
    }


    int calcLength(double decimal, boolean negative) {
        if (negative) {
            return decimal <= 0.3 ? 1 : 0;
        }
        return decimal >= 0.7 ? 1 : 0;
    }

    BlockPos getPlayerPos() {
        double decimalPoint = mc.player.posY - Math.floor(mc.player.posY);
        return new BlockPos(mc.player.posX, decimalPoint > 0.8 ? Math.floor(mc.player.posY) + 1.0 : Math.floor(mc.player.posY), mc.player.posZ);
    }


}

