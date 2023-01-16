package dev.cuican.staypro.module.modules.movement;


import dev.cuican.staypro.Stay;
import dev.cuican.staypro.common.annotations.ModuleInfo;
import dev.cuican.staypro.common.annotations.Parallel;
import dev.cuican.staypro.event.events.network.EventMove;
import dev.cuican.staypro.event.events.network.PacketEvent;
import dev.cuican.staypro.module.Category;
import dev.cuican.staypro.module.Module;
import dev.cuican.staypro.setting.Setting;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.concurrent.atomic.AtomicLong;
@ModuleInfo(name = "TickShift", category = Category.MOVEMENT, description = "TickShift")

public class TickShift extends Module {
    public final Setting<Integer> timer = setting("Timer", 2, 0, 100);
    public final Setting<Integer> packets = setting("Timer", 20, 0, 1000);
    public final Setting<Integer> lagTime = setting("Timer", 1000, 0, 10000);
    private final Setting<Boolean> sneaking = setting("Sneaking", true);
    private final Setting<Boolean> cancelGround = setting("CancelGround", true);
    private final Setting<Boolean> cancelRotations = setting("CancelRotation", true);

    private final AtomicLong lagTimer  = new AtomicLong();
    private int ticks;



    public boolean passed(int ms)
    {
        return System.currentTimeMillis() - lagTimer.get() >= ms;
    }

    @SubscribeEvent
    public void onTick(TickEvent e){
        if (mc.player == null || mc.world == null || !passed(lagTime.getValue()))
        {
            rozetked();
        }
        else if (ticks <= 0 || noMovementKeys() || !sneaking.getValue() && mc.player.isSneaking())
        {
            Stay.TICK_TIMER = 1.0f;
        }
    }



    @SubscribeEvent
    public void onEventMove(EventMove e){
        Stay.TICK_TIMER = 1.0f;
        int maxPackets = packets.getValue();
        ticks = ticks >= maxPackets ? maxPackets : ticks + 1;
    }



    @SubscribeEvent
    public void onPacketReceive(PacketEvent.Receive e){
        if(e.getPacket() instanceof SPacketPlayerPosLook){
            lagTimer.set(System.currentTimeMillis());
        }
    }


    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send e){
        if(e.getPacket() instanceof CPacketPlayer.PositionRotation){
            hth(e, true);
        }
        if(e.getPacket() instanceof CPacketPlayer.Rotation){
            if (cancelRotations.getValue()
                    && (cancelGround.getValue()
                    || ((CPacketPlayer.Rotation) e.getPacket()).isOnGround() == mc.player.onGround))
            {
                e.setCanceled(true);
            }
            else
            {
                hth(e, false);
            }
        }
        if(e.getPacket() instanceof CPacketPlayer.Position){
            hth(e, true);
        }
        if(e.getPacket() instanceof CPacketPlayer){
            if (cancelGround.getValue())
            {
                e.setCanceled(true);
            }
            else
            {
                hth(e, false);
            }
        }
    }



    public static boolean noMovementKeys()
    {
        return !mc.player.movementInput.forwardKeyDown
                && !mc.player.movementInput.backKeyDown
                && !mc.player.movementInput.rightKeyDown
                && !mc.player.movementInput.leftKeyDown;
    }

    @Override
    public String getModuleInfo()
    {
        return ticks + "";
    }

    @Override
    public void onEnable()
    {
        rozetked();
    }

    @Override
    public void onDisable()
    {
        rozetked();
    }

    private void hth(PacketEvent.Send event, boolean moving)
    {

        if (event.isCanceled())
        {
            return;
        }

        if (moving && !noMovementKeys() && (sneaking.getValue() || !mc.player.isSneaking()))
        {
            Stay.TICK_TIMER = timer.getValue();

        }

        ticks = ticks <= 0 ? 0 : ticks - 1;
    }

    public void rozetked()
    {
        Stay.TICK_TIMER = 1.0f;
        ticks = 0;
    }
}
