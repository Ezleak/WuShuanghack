//package dev.cuican.staypro.module.modules.render;
//
//
//import dev.cuican.staypro.Stay;
//import dev.cuican.staypro.client.FriendManager;
//import dev.cuican.staypro.common.annotations.ModuleInfo;
//import dev.cuican.staypro.event.events.render.Render2DEvent;
//import dev.cuican.staypro.module.Category;
//import dev.cuican.staypro.module.Module;
//import dev.cuican.staypro.module.modules.client.HUDEditor;
//import dev.cuican.staypro.setting.Setting;
//import dev.cuican.staypro.setting.settings.PositionSetting;
//import dev.cuican.staypro.utils.AstolfoAnimation;
//import dev.cuican.staypro.utils.DrawHelper;
//import dev.cuican.staypro.utils.PaletteHelper;
//import dev.cuican.staypro.utils.graphics.RenderHelper;
//import net.minecraft.client.gui.GuiChat;
//import net.minecraft.client.gui.ScaledResolution;
//
//import net.minecraft.client.renderer.GlStateManager;
//import net.minecraft.entity.Entity;
//import net.minecraft.entity.player.EntityPlayer;
//import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
//import org.lwjgl.input.Mouse;
//import org.lwjgl.opengl.GL11;
//
//import java.awt.*;
//
//import static org.lwjgl.opengl.GL11.*;
//
//@ModuleInfo(name = "AkrienRadar", category = Category.RENDER)
//
//public class RadarRewrite extends Module {
//    public Setting<String> p = setting("Page","GENERAL",listOf( "GENERAL", "Color1","Color2"));
//
//    public Setting<Float> width = setting("TracerHeight", 2.28f, 0.1f, 5f);
//    public Setting<Float> rad22ius = setting("TracerDown", 3.63f, 0.1f, 20.0f);
//    public Setting<Float> tracerA = setting("TracerWidth", 0.44f, 0.0f, 8.0f);
//    public Setting<Integer> xOffset = setting("TracerRadius", 68, 20, 100);
//    public Setting<Integer> maxup2 = setting("PitchLock", 42, -90, 90);
//    public Setting<PositionSetting> pos = new Setting<>("Position", new PositionSetting(0.5f, 0.78f));
//
//    public  Setting<Boolean> glow = setting("TracerGlow", false).whenAtMode(p, "GENERAL");
//
//    public Setting<Integer> glowe = setting("GlowRadius", 10, 1, 20).whenAtMode(p, "GENERAL");
//    public Setting<Integer> glowa = setting("GlowAlpha", 170, 0, 255).whenAtMode(p, "GENERAL");
//
//    public Setting<String> triangleMode = setting("TracerCMode","Astolfo",listOf( "Astolfo", "Rainbow")).whenAtMode(p,"GENERAL");
//    public Setting<String> Mode2 = setting("CircleCMode","Astolfo",listOf( "Astolfo", "Rainbow")).whenAtMode(p,"GENERAL");
//
//    public Setting<Float> CRadius = setting("CompasRadius", 47f, 0.1f, 70.0f);
//
//    public Setting<Integer> fsef = setting("Correct", 12, -90, 90).whenAtMode(p, "GENERAL");
//
//
//    public Setting<Integer> cColorRed = setting("CompassRed", 255, 1, 255).whenAtMode(p, "Color1");
//    public Setting<Integer> cColorBlu = setting("CompassBlue", 255, 1, 255).whenAtMode(p, "Color1");
//    public Setting<Integer> cColorGreen = setting("CompassGreen", 255, 1, 255).whenAtMode(p, "Color1");
//
//    public Setting<Integer> ciColorRed = setting("CircleRed", 255, 1, 255).whenAtMode(p, "Color1");
//    public Setting<Integer> ciColorBlu = setting("CircleBlue", 255, 1, 255).whenAtMode(p, "Color1");
//    public Setting<Integer> ciColorGreen = setting("CircleGreen", 255, 1, 255).whenAtMode(p, "Color1");
//
//    public Setting<Integer> colorfColorRed = setting("FriendColorRed", 255, 1, 255).whenAtMode(p, "Color2");
//    public Setting<Integer> colorfColorBlu = setting("FriendColorBlue", 255, 1, 255).whenAtMode(p, "Color2");
//    public Setting<Integer> colorfColorGreen = setting("FriendColorGreen", 255, 1, 255).whenAtMode(p, "Color2");
//
//    public Setting<Integer> colorsrRed = setting("TracerColorRed", 255, 1, 255).whenAtMode(p, "Color2");
//    public Setting<Integer> colorsBlu = setting("TracerColorBlue", 255, 1, 255).whenAtMode(p, "Color2");
//    public Setting<Integer> colorsGreen = setting("TracerColorGreen", 255, 1, 255).whenAtMode(p, "Color2");
//
//
//
//
//
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
//        return normaliseX() > xOffset2 - 50 && normaliseX()< xOffset2 + 50 && normaliseY() > yOffset2 - 50 &&  normaliseY() < yOffset2 + 50;
//    }
//
//    float xOffset2 = 0;
//    float yOffset2 = 0;
//
//
//    @Override
//    public void onUpdate(){
//        astolfo.update();
//    }
//
//    @SubscribeEvent
//    public void onRender2D(Render2DEvent event) {
//
//        ScaledResolution sr = new ScaledResolution(mc);
//
//        if(mc.currentScreen instanceof GuiChat ){
//            if(isHovering()){
//                if(Mouse.isButtonDown(0) && mousestate){
//                    pos.getValue().setX( (float) (normaliseX() - dragX) /  sr.getScaledWidth());
//                    pos.getValue().setY( (float) (normaliseY() - dragY) / sr.getScaledHeight());
//                }
//
//            }
//        }
//
//        if(Mouse.isButtonDown(0) && isHovering()){
//            if(!mousestate){
//                dragX = (int) (normaliseX() - (pos.getValue().getX() * sr.getScaledWidth()));
//                dragY = (int) (normaliseY() - (pos.getValue().getY() * sr.getScaledHeight()));
//            }
//            mousestate = true;
//        } else {
//            mousestate = false;
//        }
//
//        GlStateManager.pushMatrix();
//        rendercompass();
//        GlStateManager.popMatrix();
//
//        xOffset2 = (sr.getScaledWidth() * pos.getValue().getX());
//        yOffset2 = (sr.getScaledHeight() * pos.getValue().getY());
//
//        int color = 0;
//        switch (triangleMode.getValue()) {
//            case "Astolfo":
//                color = DrawHelper.astolfo(false, 1).getRGB();
//                break;
//            case "Rainbow":
//                color = DrawHelper.rainbow(300, 1, 1).getRGB();
//                break;
//        }
//        float xOffset = sr.getScaledWidth() * pos.getValue().getX() ;
//        float yOffset = sr.getScaledHeight() * pos.getValue().getY() ;
//
//        GlStateManager.pushMatrix();
//        GlStateManager.translate(xOffset2, yOffset2, 0);
//        GL11.glRotatef(90f / Math.abs(90f / clamp2(mc.player.rotationPitch ,maxup2.getValue(),90f)) - 90 -fsef.getValue(), 1.0f, 0.0f, 0.0f);
//        GlStateManager.translate(-xOffset2, -yOffset2, 0);
//
//
//        for (EntityPlayer e : mc.world.playerEntities) {
//            if (e != mc.player) {
//                GL11.glPushMatrix();
//                float yaw = getRotations(e) - mc.player.rotationYaw;
//                GL11.glTranslatef(xOffset, yOffset, 0.0F);
//                GL11.glRotatef(yaw, 0.0F, 0.0F, 1.0F);
//                GL11.glTranslatef(-xOffset, -yOffset, 0.0F);
//                if(FriendManager.isFriend(e)){
//                    drawTracerPointer(xOffset, yOffset - this.xOffset.getValue(), width.getValue() * 5F, colorfColorRed.getValue(),colorfColorBlu.getValue(),colorfColorGreen.getValue());
//                }
//                GL11.glTranslatef(xOffset, yOffset, 0.0F);
//                GL11.glRotatef(-yaw, 0.0F, 0.0F, 1.0F);
//                GL11.glTranslatef(-xOffset, -yOffset, 0.0F);
//                GL11.glColor4f(1F, 1F, 1F, 1F);
//                GL11.glPopMatrix();
//            }
//        }
//        GL11.glColor4f(1F, 1F, 1F, 1F);
//        GlStateManager.popMatrix();
//    }
//
//
//    public void rendercompass() {
//            ScaledResolution sr = new ScaledResolution(mc);
//            float x = sr.getScaledWidth() * pos.getValue().getX();
//            float y = sr.getScaledHeight() * pos.getValue().getY();
//
//            float nigga = Math.abs(90f / clamp2(mc.player.rotationPitch,maxup2.getValue(),90f));
//
//            if (Mode2.getValue().equals("Rainbow")) {
//                RenderHelper.drawEllipsCompas(-(int) mc.player.rotationYaw, x, y,nigga,1f, CRadius.getValue() -2, PaletteHelper.rainbow(300, 1, 1),false);
//                RenderHelper.drawEllipsCompas(-(int) mc.player.rotationYaw, x, y,nigga,1f, CRadius.getValue()-2.5f, PaletteHelper.rainbow(300, 1, 1),false);
//            }
//            if (Mode2.getValue().equals("Astolfo")) {
//                RenderHelper.drawEllipsCompas(-(int) mc.player.rotationYaw, x, y,nigga,1f, CRadius.getValue() -2, null,false);
//                RenderHelper.drawEllipsCompas(-(int) mc.player.rotationYaw, x, y,nigga,1f, CRadius.getValue() -2.5f, null,false);
//            }
//            RenderHelper.drawEllipsCompas(-(int) mc.player.rotationYaw, x, y,nigga,1f, CRadius.getValue(), cColor.getValue().getColorObject(),true);
//    }
//
//    public static float clamp2(float num, float min, float max) {
//        if (num < min) {
//            return min;
//        } else {
//            return Math.min(num, max);
//        }
//    }
//
//    public static AstolfoAnimation astolfo = new AstolfoAnimation();
//
//    public  void drawTracerPointer(float x, float y, float size, int color1,int color2,int color3) {
//        boolean blend = GL11.glIsEnabled(GL_BLEND);
//        GL11.glEnable(GL_BLEND);
//        boolean depth = GL11.glIsEnabled(GL_DEPTH_TEST);
//        glDisable(GL_DEPTH_TEST);
//
//        GL11.glDisable(GL_TEXTURE_2D);
//        GL11.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
//        GL11.glEnable(GL_LINE_SMOOTH);
//        GL11.glPushMatrix();
//
//        hexColor(color);
//        GL11.glBegin(7);
//        GL11.glVertex2d(x, y);
//        GL11.glVertex2d((x - size * tracerA.getValue() ), (y + size));
//        GL11.glVertex2d(x, (y + size- rad22ius.getValue()));
//        GL11.glVertex2d(x, y);
//        GL11.glEnd();
//
//        hexColor(ColorUtil.darker(new Color(color),0.8f).getRGB());
//        GL11.glBegin(7);
//        GL11.glVertex2d(x, y); //top
//        GL11.glVertex2d(x, (y + size- rad22ius.getValue())); //midle
//        GL11.glVertex2d((x + size * tracerA.getValue()), (y + size)); // left right
//        GL11.glVertex2d(x, y); //top
//        GL11.glEnd();
//
//
//        hexColor(ColorUtil.darker(new Color(color),0.6f).getRGB());
//        GL11.glBegin(7);
//        GL11.glVertex2d((x - size * tracerA.getValue() ), (y + size ));
//        GL11.glVertex2d((x + size * tracerA.getValue()), (y + size )); // left right
//        GL11.glVertex2d(x, (y + size - rad22ius.getValue())); //midle
//        GL11.glVertex2d((x - size * tracerA.getValue() ), (y + size ));
//        GL11.glEnd();
//        GL11.glPopMatrix();
//
//        GL11.glEnable(GL_TEXTURE_2D);
//        if (!blend)
//            GL11.glDisable(GL_BLEND);
//        GL11.glDisable(GL_LINE_SMOOTH);
//
//        if(glow.getValue())
//            Drawable.drawBlurredShadow(x- size * tracerA.getValue(),y,(x + size * tracerA.getValue()) -(x - size * tracerA.getValue() ),size,glowe.getValue(),DrawHelper.injectAlpha(new Color(color),glowa.getValue()) );
//
//        if(depth)
//            glEnable(GL_DEPTH_TEST);
//
//
//    }
//    public static void hexColor(int hexColor) {
//        float red = (float) (hexColor >> 16 & 0xFF) / 255.0f;
//        float green = (float) (hexColor >> 8 & 0xFF) / 255.0f;
//        float blue = (float) (hexColor & 0xFF) / 255.0f;
//        float alpha = (float) (hexColor >> 24 & 0xFF) / 255.0f;
//        GL11.glColor4f(red, green, blue, alpha);
//    }
//
//    private float getRotations(Entity entity) {
//        double x = interp(entity.posX,entity.lastTickPosX) - interp(mc.player.posX,mc.player.lastTickPosX);
//        double z = interp(entity.posZ,entity.lastTickPosZ) - interp(mc.player.posZ, mc.player.lastTickPosZ);
//        return (float)-(Math.atan2(x, z) * (180 / Math.PI));
//    }
//    public double interp(double d, double d2) {
//        return d2 + (d - d2) * (double)mc.getRenderPartialTicks();
//    }
//}