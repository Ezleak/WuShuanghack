//package dev.cuican.staypro.hud.huds;
//
//import dev.cuican.staypro.client.FontManager;
//import dev.cuican.staypro.engine.AsyncRenderer;
//import dev.cuican.staypro.event.events.render.Render2DEvent;
//import dev.cuican.staypro.gui.StayHUDEditor;
//import dev.cuican.staypro.module.modules.client.HUDEditor;
//import dev.cuican.staypro.module.modules.player.Timer;
//import dev.cuican.staypro.client.ModuleManager;
//import dev.cuican.staypro.common.annotations.ModuleInfo;
//import dev.cuican.staypro.module.Category;
//import dev.cuican.staypro.module.Module;
//import dev.cuican.staypro.setting.Setting;
//import dev.cuican.staypro.setting.settings.PositionSetting;
//import dev.cuican.staypro.utils.*;
//import dev.cuican.staypro.utils.math.LagCompensator;
//import net.minecraft.client.gui.GuiChat;
//import net.minecraft.client.gui.ScaledResolution;
//import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
//import org.lwjgl.input.Mouse;
//import org.lwjgl.opengl.GL11;
//
//import java.awt.*;
//import java.util.*;
//import java.util.ArrayList;
//import java.util.List;
//@ModuleInfo(name = "Indicators", category = Category.HUD)
//
//public class Indicators extends Module {
//
//
//    private static List<Indicator> indicators = new java.util.ArrayList();
//    public static AstolfoAnimation astolfo = new AstolfoAnimation();
//
//    Setting<Boolean> Memoryy = setting("Memory", true);
//    Setting<Boolean> Timerr = setting("Timer", true);
//    Setting<Boolean> TPS = setting("TPS", true);
//    Setting<Boolean> items = setting("Items", true);
//    Setting<Boolean> blur = setting("Blur", true);
//
//    private Setting<Integer> ccred = setting("RedColor", 255, 0, 255);
//    private Setting<Integer> ccgreen = setting("GreenColor", 255, 0, 255);
//    private Setting<Integer> ccblue = setting("BlueColor", 255, 0, 255);
//
//    private Setting<Integer> red2 = setting("RedRectColor", 255, 0, 255);
//    private Setting<Integer> green2 = setting("GreenRectColor", 255, 0, 255);
//    private Setting<Integer> blue2 = setting("BlueRectColor", 255, 0, 255);
//    public Setting<PositionSetting> pos = new Setting<>("Position", new PositionSetting(0.5f, 0.5f));
//
//    public Setting<Float> grange = setting("GlowRange", 3.6f, 0.0f, 10.0f);
//    public Setting<Float> gmult = setting("GlowMultiplier", 3.6f, 0.0f, 10.0f);
//    public Setting<Float> range = setting("RangeBetween", 46f, 46.0f, 100.0f);
//    public final Setting<String> colorType = setting("Mode", "Astolfo",listOf("Static", "StateBased","Astolfo"));
//
//
//    boolean once = false;
//
//
//    protected void once() {
//        indicators.add(new Indicator() {
//
//            @Override
//            boolean enabled() {
//                return Timerr.getValue();
//            }
//
//            @Override
//            String getName() {
//                return "Timer";
//            }
//
//            @Override
//            double getProgress() {
//                return (10 - Timer.value) / (Math.abs(20) + 10);
//            }
//        });
//        indicators.add(new Indicator() {
//
//            @Override
//            boolean enabled() {
//                return Memoryy.getValue();
//            }
//
//            @Override
//            String getName() {
//                return "Memory";
//            }
//
//            @Override
//            double getProgress() {
//                long total = Runtime.getRuntime().totalMemory();
//                long free = Runtime.getRuntime().freeMemory();
//                long delta = total - free;
//                return (delta / (double) Runtime.getRuntime().maxMemory());
//            }
//        });
//
//
//        indicators.add(new Indicator() {
//
//            @Override
//            boolean enabled() {
//                return TPS.getValue();
//            }
//
//            @Override
//            String getName() {
//                return "TPS";
//            }
//
//            @Override
//            double getProgress() {
//                return LagCompensator.INSTANCE.getTickRate() / 20f;
//            }
//        });
//    }
//
//    @SubscribeEvent
//    public void onRender2D(Render2DEvent e){
//        draw();
//    }
//
//
//    int dragX, dragY = 0;
//    boolean mousestate = false;
//
//    public int normaliseX(){
//        return (int) ((Mouse.getX()/2f));
//    }
//    public int normaliseY(){
//        ScaledResolution sr = new ScaledResolution(mc);
//        return (((-Mouse.getY() + sr.getScaledHeight()) + sr.getScaledHeight())/2);
//    }
//
//    public boolean isHovering(){
//        return normaliseX() > posX && normaliseX()< posX + 150 && normaliseY() > posY &&  normaliseY() < posY + 50;
//    }
//
//    float posX,posY = 0;
//    public Indicators() {
//        asyncRenderer = new AsyncRenderer() {
//
//            @Override
//            public void onUpdate(ScaledResolution resolution, int mouseX, int mouseY) {
//
//                ScaledResolution sr = new ScaledResolution(mc);
//                posX = sr.getScaledWidth() * pos.getValue().getX();
//                posY = sr.getScaledHeight() * pos.getValue().getY();
//                if (mc.currentScreen instanceof GuiChat || mc.currentScreen instanceof StayHUDEditor) {
//                    if (isHovering()) {
//                        if (Mouse.isButtonDown(0) && mousestate) {
//                            pos.getValue().setX((float) (normaliseX() - dragX) / sr.getScaledWidth());
//                            pos.getValue().setY((float) (normaliseY() - dragY) / sr.getScaledHeight());
//                        }
//                    }
//                }
//
//                if (Mouse.isButtonDown(0) && isHovering()) {
//                    if (!mousestate) {
//                        dragX = (int) (normaliseX() - (pos.getValue().getX() * sr.getScaledWidth()));
//                        dragY = (int) (normaliseY() - (pos.getValue().getY() * sr.getScaledHeight()));
//                    }
//                    mousestate = true;
//                } else {
//                    mousestate = false;
//                }
//
//
//                if (!once) {
//                    once();
//                    once = true;
//                    return;
//                }
//                astolfo.update();
//                indicators.forEach(indicator -> indicator.update());
//            }
//        };
//
//    }
//    public void draw() {
//        ScaledResolution sr = new ScaledResolution(mc);
//        GL11.glPushMatrix();
//        GL11.glTranslated(pos.getValue().x * sr.getScaledWidth(), pos.getValue().y * sr.getScaledHeight(), 0);
//
//        List<Indicator> enabledIndicators = new ArrayList();
//        for (Indicator indicator : indicators) {
//            if (indicator.enabled())
//                enabledIndicators.add(indicator);
//        }
//        int enabledCount = enabledIndicators.size();
//        if (enabledCount > 0) {
//            for (int i = 0; i < enabledCount; i++) {
//                GL11.glPushMatrix();
//                GL11.glTranslated(range.getValue() * i, 0, 0);
//                Indicator ind = enabledIndicators.get(i);
//             //   renderShadow(0, 0, 40, 40, ColorShell.rgba(25, 25, 25, 180), 3);
//                if(!blur.getValue()) {
//                    RenderUtil.drawSmoothRect(0, 0, 44, 44, ColorShell.rgba(25, 25, 25, 180));
//                } else {
//                    Color cscolor = new Color(this.red2.getValue(), this.green2.getValue(), this.blue2.getValue());
//                    DrawHelper.drawRectWithGlow(0, 0, 44, 44,grange.getValue(),gmult.getValue(),cscolor);
//                }
//
//
//                GL11.glTranslated(22, 26, 0);
//                drawCircle(ind.getName(), ind.progress());
//                GL11.glPopMatrix();
//            }
//        }
//        GL11.glPopMatrix();
//    }
//
//
//    public void drawCircle(String name, double offset) {
//        GL11.glDisable(GL11.GL_TEXTURE_2D);
//        boolean oldState = GL11.glIsEnabled(GL11.GL_BLEND);
//        GL11.glEnable(GL11.GL_BLEND);
//        GL11.glEnable(GL11.GL_LINE_SMOOTH);
//        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
//        GL11.glShadeModel(GL11.GL_SMOOTH);
//        GL11.glLineWidth(5.5f);
//        GL11.glColor4f(0.1f, 0.1f, 0.1f, 0.5f);
//        GL11.glBegin(GL11.GL_LINE_STRIP);
//        for (int i = 0; i < 360; i++) {
//            double x = Math.cos(Math.toRadians(i)) * 11;
//            double z = Math.sin(Math.toRadians(i)) * 11;
//            GL11.glVertex2d(x, z);
//        }
//        GL11.glEnd();
//        GL11.glBegin(GL11.GL_LINE_STRIP);
//        for (int i = -90; i < -90 + (360 * offset); i++) {
//
//            float red = ccred.getValue();
//            float green = ccgreen.getValue();
//            float blue = ccblue.getValue();
//            if (colorType.getValue().equals("StateBased")) {
//                float[] buffer = getRG(offset);
//                red = buffer[0];
//                green = buffer[1];
//                blue = buffer[2];
//            } else if (colorType.getValue().equals("Astolfo")) {
//                double stage = (i + 90) / 360.;
//                int clr = astolfo.getColor(stage);
//                red = ((clr >> 16) & 255);
//                green = ((clr >> 8) & 255);
//                blue = ((clr & 255));
//            }
//            GL11.glColor4f(red / 255f, green / 255f, blue / 255f, 1);
//            double x = Math.cos(Math.toRadians(i)) * 11;
//            double z = Math.sin(Math.toRadians(i)) * 11;
//            GL11.glVertex2d(x, z);
//        }
//        GL11.glEnd();
//        GL11.glDisable(GL11.GL_LINE_SMOOTH);
//        if (!oldState)
//            GL11.glDisable(GL11.GL_BLEND);
//        GL11.glEnable(GL11.GL_TEXTURE_2D);
//      //  GL11.glShadeModel(GL11.GL_FLAT);
//        GL11.glColor4f(1, 1, 1, 1);
//        if(!Objects.equals(name, "TPS")) {
//            FontManager.drawCentString6((int) (offset * 100) + "%", 0.3f, -0.8f, ColorShell.rgba(200, 200, 200, 255));
//            FontManager.drawCentString6(name, 0, -20f, ColorShell.rgba(200, 200, 200, 255));
//        } else {
//            FontManager.drawCentString6(String.valueOf((int) (offset * 20)), 0f, -0.8f, ColorShell.rgba(200, 200, 200, 255));
//            FontManager.drawCentString6(name, 0f, -20f, ColorShell.rgba(200, 200, 200, 255));
//        }
//    }
//
//    public static float[] getRG(double input) {
//        return new float[] { 255 - 255 * (float) input, 255 * (float) input, 100 * (float) input };
//    }
//
//    public static abstract class Indicator {
//        DynamicAnimation animation = new DynamicAnimation();
//
//        void update() {
//            this.animation.setValue(Math.max(getProgress(), 0));
//            this.animation.update();
//        }
//
//        double progress() {
//            return this.animation.getValue();
//        }
//
//        abstract boolean enabled();
//
//        abstract String getName();
//
//        abstract double getProgress();
//    }
//
//}
