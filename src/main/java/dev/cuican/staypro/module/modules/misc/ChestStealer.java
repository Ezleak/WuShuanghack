package dev.cuican.staypro.module.modules.misc;


import dev.cuican.staypro.common.annotations.ModuleInfo;
import dev.cuican.staypro.module.Category;
import dev.cuican.staypro.module.Module;
import dev.cuican.staypro.setting.Setting;
import dev.cuican.staypro.utils.Timer;
import dev.cuican.staypro.utils.Wrapper;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.item.Item;

@ModuleInfo(name = "ChestStealer", category = Category.MISC, description = "ChestStealer")

public class ChestStealer extends Module {

    Timer timer = new Timer();

    private final Setting<Integer> delayed = setting("Delay", 100, 0, 1000);


    @Override
    public void onRenderTick() {
        if (Wrapper.mc.player.openContainer != null) {
            if (Wrapper.mc.player.openContainer instanceof ContainerChest) {
                ContainerChest container = (ContainerChest)Wrapper.mc.player.openContainer;
                for (int i = 0; i < container.inventorySlots.size(); ++i) {
                    if (container.getLowerChestInventory().getStackInSlot(i).getItem() != Item.getItemById(0) && timer.passedMs(delayed.getValue())) {
                        mc.playerController.windowClick(container.windowId, i, 0, ClickType.QUICK_MOVE, Wrapper.mc.player);
                        this.timer.reset();
                        continue;
                    }
                    if (!this.empty(container)) continue;
                    Wrapper.mc.player.closeScreen();
                }
            }
        }
    }

    public boolean empty(Container container) {
        boolean voll = true;
        int slotAmount = container.inventorySlots.size() == 90 ? 54 : 27;
        for (int i = 0; i < slotAmount; ++i) {
            if (!container.getSlot(i).getHasStack()) continue;
            voll = false;
        }
        return voll;
    }
}
