package util.logging;

import java.awt.Dimension;
import java.awt.Toolkit;

import org.apache.log4j.Logger;

//import java.util.Hashtable;
import util.AppEnv;

public class LogUtil {
    // private static Hashtable windowNames = null;
    private static Integer sessionID = null;
    private static Logger uiLogger = Logger.getLogger("ui");
    private static String serviceName = "";

    public static String SystemInfo(){
        String sysinfo = "System Properties: "
                        + "java " + System.getProperty("java.version")
                        + ", " + System.getProperty("java.vendor")
                        + ", " + System.getProperty("os.name")
                        + ", " + System.getProperty("os.arch")
                        + ", os version " +System.getProperty("os.version");
        return sysinfo;
    }

    public static String DisplayInfo() {
        String displayInfo = "";
        if(AppEnv.isGUISupported()) {
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            displayInfo = "Initial display resolution is x:"
                               + screenSize.getWidth()
                               + " y:"
                               + screenSize.getHeight();
        } else {
            displayInfo = "Unable to get display information";
        }

            return displayInfo;
        }

    public static void setLogLevel(org.apache.log4j.Level level){
        uiLogger.setLevel(level);
    }

    public static void beginSession(Integer id){
        if (serviceName == null ){
            serviceName= "IU";
        }
        sessionID = id;
        getLogger().info("Begin");
        getLogger().info(SystemInfo());
        getLogger().info(DisplayInfo());
    }

    public static void endSession(){
        getLogger().info("End");
    }

    public static UILogger getUILogger(int windowType, long windowSerialNo){
        return new UILogger(getLogger(windowType, windowSerialNo));
    }

    protected static Logger getLogger(){
        String loggerName = "ui." + serviceName + "." + "Session#" + sessionID;
        return Logger.getLogger(loggerName);
    }

    protected static Logger getLogger(int windowType, long windowSerialNo){
        String loggerName = "ui." + serviceName + "." + "Session#" + sessionID + "."
                            + "WINTYPE_LOCAL_TIMELINE" + "#" + Long.toString(windowSerialNo);
        return Logger.getLogger(loggerName);
    }
}
