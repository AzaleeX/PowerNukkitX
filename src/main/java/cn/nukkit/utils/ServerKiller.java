package cn.nukkit.utils;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * @author MagicDroidX (Nukkit Project)
 */
public class ServerKiller extends Thread {

    private static final Logger LOGGER = Logger.getLogger("ServerKiller");
    
    public final long sleepTime;

    public ServerKiller(long time) {
        this(time, TimeUnit.SECONDS);
    }

    public ServerKiller(long time, TimeUnit unit) {
        this.sleepTime = unit.toMillis(time);
        this.setDaemon(true);
        this.setName("Server Killer");
    }

    @Override
    public void run() {
        try {
            sleep(sleepTime);
            LOGGER.severe("Took too long to stop, server was killed forcefully!");
            System.exit(1);
        } catch (InterruptedException e) {
            // The thread was interrupted, which might mean that Ctrl+C was pressed
            // Support Ctrl+C
            LOGGER.severe("Server stopping process was interrupted. Killing server...");
            System.exit(1);
        }
    }
}
