package dev.cuican.staypro;



import dev.cuican.staypro.client.*;
import dev.cuican.staypro.concurrent.TaskManager;
import dev.cuican.staypro.concurrent.event.EventManager;
import dev.cuican.staypro.concurrent.event.Listener;
import dev.cuican.staypro.concurrent.event.Priority;
import dev.cuican.staypro.module.modules.client.ClickGUI;
import dev.cuican.staypro.utils.IconUtil;
import dev.cuican.staypro.utils.math.LagCompensator;
import net.minecraft.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.Display;

import java.awt.*;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Random;

import static dev.cuican.staypro.concurrent.TaskManager.launch;


/**
 * Author B_312
 * Since 05/01/2021
 * Last update on 09/21/2021
 */
public class Stay {

    public static final String MOD_NAME = "Stay_Pro";
    public static final String MOD_VERSION = "1.1";

    public static final String AUTHOR = "MoXi";
    public static final String GITHUB = "No open source at the moment";
    public static final String[] MESSAGE = {
            "我记得你爱我,或许是我记反了",
            "我试着销声匿迹,原来我真的无人问津.",
            "后来我终于知道,它并不是我的花,我只是途径了它的盛放.",
            "你说我会遇到比你更好的人,其实,是你想拥有比我更好的人吧.",
            "后来我终于明白,她尽管跟天气一样难以预料,却也跟天气一样无可避免.",
            "一直不明白,两个注定不会在一起的人,为什么要安排相遇."
    };
    public static String CHAT_SUFFIX = "Stay";
    public static final Stay instance;

    public static final Logger log = LogManager.getLogger(MOD_NAME);
    public static float TICK_TIMER = 1;


    private static Thread mainThread;


    public  void preInitialize() {
        mainThread = Thread.currentThread();
    }
    public SystemTray tray = SystemTray.getSystemTray();
    public static Image image = Toolkit.getDefaultToolkit().createImage(Stay.class.getResource("/assets/cuican/icons/icon-32x.png"));
    public static TrayIcon trayIcon ;
    public static void setWindowIcon() {
        if (Util.getOSType() != Util.EnumOS.OSX) {
            try (InputStream inputStream16x = Stay.class.getResourceAsStream("/assets/cuican/icons/icon-16x.png");
                 InputStream inputStream32x = Stay.class.getResourceAsStream("/assets/cuican/icons/icon-32x.png")) {
                ByteBuffer[] icons = new ByteBuffer[]{IconUtil.INSTANCE.readImageToBuffer(inputStream16x), IconUtil.INSTANCE.readImageToBuffer(inputStream32x)};
                Display.setIcon(icons);
            } catch (Exception e) {
                Stay.log.error("Couldn't set Windows Icon", e);
            }
        }
    }


    public  void initialize() {
        long tookTime = TaskManager.runTiming(() -> {
            Display.setTitle(MOD_NAME + " " + MOD_VERSION+" BY:"+AUTHOR+" | "+MESSAGE[new Random().nextInt(MESSAGE.length)]);
            LagCompensator.INSTANCE = new LagCompensator();

            setWindowIcon();
            trayIcon.setImageAutoSize(true);
            trayIcon.setToolTip("Welcome Thanks for using STAY" + Stay.MOD_VERSION);
            try {
                tray.add(trayIcon);
            } catch (AWTException e) {

            }
            trayIcon.displayMessage("Stay Welcome", "Welcome Thanks for using STAY" + Stay.MOD_VERSION, TrayIcon.MessageType.NONE);

            //Parallel load managers
            TaskManager.runBlocking(it -> {

                Stay.log.info("Loading Font Manager");
                FontManager.init();

                Stay.log.info("Loading Module Manager");
                ModuleManager.init();

                Stay.log.info("Loading GUI Manager");
                launch(it, GUIManager::init);

                Stay.log.info("Loading Command Manager");
                launch(it, CommandManager::init);

                Stay.log.info("Loading Friend Manager");
                launch(it, FriendManager::init);

                Stay.log.info("Loading Config Manager");
                launch(it, ConfigManager::init);



            });

        });
        log.info("Took " + tookTime + "ms to launch Stay!");
    }

    public void postInitialize() {
        ClickGUI.instance.disable();
    }

    public static boolean isMainThread(Thread thread) {
        return thread == mainThread;
    }

    public static EventManager EVENT_BUS = new EventManager();
    public static ModuleBus MODULE_BUS = new ModuleBus();
    static {
        trayIcon = new TrayIcon(image, "Auto Queue");
        EVENT_BUS = new EventManager();
        MODULE_BUS = new ModuleBus();

        instance = new Stay();
    }



}