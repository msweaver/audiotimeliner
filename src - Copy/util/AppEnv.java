package util;

import java.io.File;


public class AppEnv {

    private static final String VERSION = "3.0";
    private static final String COPYRIGHT_INFO = "Copyright 2016 Brent Yorgason";

    private final static String WIN_DATA_DIR = "Local Settings\\Application Data\\Audio Timeliner";
    private final static String UNIX_DATA_DIR = ".audiotimeliner";
    private static String data_dir = null;
    public AppEnv() {
    }
    public static String getOS() {
        String os_name = System.getProperty("os.name");
        if (os_name.indexOf("Windows") != -1) {
            return "Win";
        } else {
            return "Mac";
        }
    }
    public static boolean isGUISupported() {
        String os_name = System.getProperty("os.name");
        if (os_name.indexOf("Windows") != -1 || os_name.indexOf("Mac OS") != -1) {
            return true;
        }
        return false;
    }
    public static String getDataDir(){
        if (data_dir == null) {
            String user_home = System.getProperty("user.home");
            String os_name = System.getProperty("os.name");
            String s = null;
            if (os_name.indexOf("Windows") != -1) {
                // Windows os
                s = user_home + File.separator + WIN_DATA_DIR;
            } else {
                // both Mac OS X and Unix use the same directory
                s = user_home + File.separator + UNIX_DATA_DIR;
            }
            File f = new File(s);
            if ((!f.exists() && !f.mkdirs()) || ! f.isDirectory()) {
                System.err.println("Can not create AudioTimeliner data directory " + s);
                return null;
            }
            data_dir = s;
        }
        return data_dir;
    }
    public static String getVersion() {
        return VERSION;
    }

    public static String getCopyrightInfo() {
        return COPYRIGHT_INFO;
    }

    public static String getMachineID() {
        String ip = null;
        try {
            ip = java.net.InetAddress.getLocalHost().toString();
        } catch (Exception e) {
            ;
        }
        String os = System.getProperty("os.name")+System.getProperty("os.version");
        int hash = (os+ip).hashCode();
        if (hash < 0) {
            hash = - hash;
        }
        return Integer.toHexString(hash);
    }
    public static String getAppDir() {
    String dir = System.getProperty("audiotimeliner.dir");
    if(dir != null) {
            dir += File.separator;
    }else {
            dir = System.getProperty("user.dir") + File.separator;
        }
        return dir;
    }
}

