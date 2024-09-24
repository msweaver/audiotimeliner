package client;

import org.apache.log4j.Logger;

import util.AppEnv;

/**
 * STANDALONE: this is a short version of Client designed for the standalone timeliner
 */

public class Client {
    public static final String DEFAULT_LOG4J_CONF = AppEnv.getAppDir() + "conf/client/client_console.lcf";
    public static boolean MacOpenFile = false;
    public static boolean isStandalone = true;

    /**
     * Class logger
     */
    private static Logger log = Logger.getLogger(Client.class);

    public static void initialize(){
    }

    public static String getUserName() {
            return new String("");
    }

    public static void cleanup() {
        log.debug("cleaning up connections");

        try {
            if (AppEnv.isGUISupported()){
/**                try {
                    QTSession.close();      //QTSession should always be closed before app can exit
                } catch (LinkageError e) {
                    // If the QTJava is not installed, there will be LinkageError
                    // Ignore this error
                    log.warn("QuickTime for Java is not properly installed");
                }
**/            }
        } catch (Exception except) {
            // apparently QT can throw runtime exceptions and we don't want to bail until we've
            // finished, so catch the exception and ignore it.
            log.warn("Error while shutting down Quicktime", except);
        }
    }

    /**
     * Shutdown the client application.
     */
    public static void shutdown() {
        log.debug("shutting down client application");
        cleanup();
        System.exit(0);
    }
}
