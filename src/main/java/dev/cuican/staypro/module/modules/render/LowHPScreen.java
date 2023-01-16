package dev.cuican.staypro.module.modules.render;


import dev.cuican.staypro.common.annotations.ModuleInfo;
import dev.cuican.staypro.event.events.render.Render2DEvent;
import dev.cuican.staypro.module.Category;
import dev.cuican.staypro.module.Module;
import dev.cuican.staypro.setting.Setting;
import dev.cuican.staypro.utils.MathUtil;
import dev.cuican.staypro.utils.RenderUtil;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;
@ModuleInfo(name = "LowHPScreen", category = Category.RENDER, description = "LowHPScreen")

public class LowHPScreen extends Module {



    public Setting<Integer> color = setting("Red", 255, 1, 255);
    public Setting<Integer> color3 = setting("Blue", 203, 1, 255);

    public Setting<Integer> color4 = setting("Green", 198, 1, 255);



    int dynamic_alpha = 0;
    int nuyahz = 0;

    @SubscribeEvent
    public void onRender2D(Render2DEvent e){

        Color color2 = new Color(color.getValue(),color3.getValue(),color4.getValue(), MathUtil.clamp(dynamic_alpha + 40,0,255));

        if(mc.player.getHealth() < 10) {
            ScaledResolution sr = new ScaledResolution(mc);
            RenderUtil.draw2DGradientRect(0, 0, sr.getScaledWidth(), sr.getScaledWidth(), color2.getRGB(), new Color(0,0,0,0).getRGB(),  color2.getRGB(), new Color(0,0,0,0).getRGB());
            if(mc.player.getHealth() > 9){
                nuyahz = 18;
            } else
            if(mc.player.getHealth() > 8){
                nuyahz = 36;
            } else
            if(mc.player.getHealth() > 7){
                nuyahz = 54;
            } else
            if(mc.player.getHealth() > 6){
                nuyahz = 72;
            } else
            if(mc.player.getHealth() > 5){
                nuyahz = 90;
            } else
            if(mc.player.getHealth() > 4){
                nuyahz = 108;
            } else
            if(mc.player.getHealth() > 3){
                nuyahz = 126;
            } else
            if(mc.player.getHealth() > 2){
                nuyahz = 144;
            } else
            if(mc.player.getHealth() > 1){
                nuyahz = 162;
            } else
            if(mc.player.getHealth() > 0){
                nuyahz = 180;
            }
        }

        if(nuyahz > dynamic_alpha){
            dynamic_alpha = dynamic_alpha + 3;
        }
        if(nuyahz < dynamic_alpha){
            dynamic_alpha = dynamic_alpha - 3;
        }

    }

    //dalpha 180/10
    // 18

}
