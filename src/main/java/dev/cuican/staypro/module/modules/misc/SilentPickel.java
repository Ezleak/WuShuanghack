package dev.cuican.staypro.module.modules.misc;


import dev.cuican.staypro.client.ModuleManager;
import dev.cuican.staypro.common.annotations.ModuleInfo;
import dev.cuican.staypro.module.Category;
import dev.cuican.staypro.module.Module;
import dev.cuican.staypro.module.modules.combat.CevBreaker;
import dev.cuican.staypro.module.modules.combat.CivBreaker;
import dev.cuican.staypro.module.modules.combat.PistonAura;
import dev.cuican.staypro.module.modules.player.Instant;
import dev.cuican.staypro.setting.Setting;
import dev.cuican.staypro.utils.Timer;
import dev.cuican.staypro.utils.inventory.InventoryUtil;
import net.minecraft.init.Items;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.util.EnumHand;
@ModuleInfo(name = "SilentPickel", category = Category.MISC)

public class SilentPickel extends Module {
    public Setting<Float> delay = setting("Delay", 3.0f, 0f, 25f);

    public Setting<Boolean> noGap = setting("NoGapSwitch", true);
    public Setting<Boolean> noPA = setting("NoPASwitch", true);

    public Timer timer;
    public int oldslot = -1;
    public EnumHand oldhand = null;


    @Override
    public void onEnable()
    {
        timer = new Timer();
    }

    @Override
    public void onUpdate()
    {
		if(nullCheck()) return;
		int pickel = InventoryUtil.getItemHotbar(Items.DIAMOND_PICKAXE);
        if(pickel == -1) return;

        if(mc.player.inventory.getCurrentItem().getItem() == Items.GOLDEN_APPLE && noGap.getValue()) return;
        Module pa = ModuleManager.getModule(PistonAura.class);
        if(pa.isEnabled() && noPA.getValue()) return;
        Module cev = ModuleManager.getModule(CevBreaker.class);
        Module civ = ModuleManager.getModule(CivBreaker.class);
        if(cev.isEnabled() || civ.isEnabled()) return;

        if(Instant.breakPos != null) {
            if(mc.player.inventory.getCurrentItem().getItem() != Items.DIAMOND_PICKAXE) setItem(pickel);
            Instant mine = (Instant) ModuleManager.getModule(Instant.class);
            mine.update();
		    restoreItem();
        }
    }
	
    public void setItem(int slot)
    {
        oldhand = null;
        if(mc.player.isHandActive()) {
            oldhand = mc.player.getActiveHand();
        }
        oldslot = mc.player.inventory.currentItem;
        mc.player.connection.sendPacket(new CPacketHeldItemChange(slot));
    }

    public void restoreItem()
    {
        if(oldslot != -1)
        {
            if(oldhand != null) {
                mc.player.setActiveHand(oldhand);
            }
            mc.player.connection.sendPacket(new CPacketHeldItemChange(oldslot));
            oldslot = -1;
            oldhand = null;
        }
    }
}
