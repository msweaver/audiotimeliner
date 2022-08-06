package ui.common;

import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.MouseEvent;
import java.util.*;

import javax.swing.*;
import javax.swing.border.Border;

import util.*;

/**
 * A collection of utility methods and objects 
 */

public class UIUtilities {

    // private static Logger log = Logger.getLogger(UIUtilities.class);

    //------------------------- COLORS ----------------------------//
    static public final Color colorAdminTab = new Color(214, 192, 214);

    //------------------------- FONTS -----------------------------//
    
    static public final Font fontUnicode = new Font("Arial Unicode MS", 0, convertFontSize(11));
    static public final Font fontUnicodeBigger = new Font("Arial Unicode MS", 0, convertFontSize(12));
    static public final Font fontUnicodeBiggest = new Font("Arial Unicode MS", 0, convertFontSize(14));
    static public final Font fontUnicodeSmaller = new Font("Arial Unicode MS", 0, convertFontSize(10));
    static public final Font fontMenusWin = new Font("Dialog", 0, convertFontSize(12));  //windows font for menus
    static public final Font fontMenusMac = new Font("Dialog", 0, convertFontSize(14));            //mac font for menus
    static public final Font fontDialogWin = new Font("Dialog", 0, convertFontSize(11));           //windows font for regular text
    static public final Font fontDialogMac = new Font("Dialog", 0, convertFontSize(12));           //mac font for regular text
    static public final Font fontDialogMacSmaller = new Font("Dialog", 0, convertFontSize(11));    // timeline uses smaller mac fonts
    static public final Font fontDialogMacSmallest = new Font("Dialog", 0, convertFontSize(10));
    static public final Font fontDialogWinBold = new Font("Dialog", Font.BOLD, convertFontSize(11));
    static public final Font fontDialogMacBold = new Font("Dialog", Font.BOLD, convertFontSize(12));
    static public final Font fontTitleBold = new Font("SansSerif", Font.BOLD, convertFontSize(14));
    static public final Font fontSansSerifWin = new Font("SansSerif", 0, convertFontSize(11));     //windows generic sans serif text
    static public final Font fontSansSerifMac = new Font("SansSerif", 0, convertFontSize(12));     //mac generic sans serif text

    //------------------------- ICONS -----------------------------//
    final public static ImageIcon icoPlay = new ImageIcon(AppEnv.getAppDir()+ "resources/media/playPL.gif");
    final public static ImageIcon icoPause = new ImageIcon(AppEnv.getAppDir()+ "resources/media/pausePL.gif");
    final public static ImageIcon icoStop = new ImageIcon(AppEnv.getAppDir()+ "resources/media/stopPL.gif");
    final public static ImageIcon icoPrev = new ImageIcon(AppEnv.getAppDir()+ "resources/media/prevPL.gif");
    final public static ImageIcon icoNext = new ImageIcon(AppEnv.getAppDir()+"resources/media/nextPL.gif");
    final public static ImageIcon icoRW = new ImageIcon(AppEnv.getAppDir()+"resources/media/rewindPL.gif");
    final public static ImageIcon icoFF = new ImageIcon(AppEnv.getAppDir()+"resources/media/fastforwardPL.gif");
    final public static ImageIcon icoSpeaker = new ImageIcon(AppEnv.getAppDir()+ "resources/media/speaker.gif");
    final public static ImageIcon icoSpeakerMute = new ImageIcon(AppEnv.getAppDir()+ "resources/media/speaker-mute.gif");
    // up & down arrow icons
    final public static ImageIcon icoDivider = new ImageIcon(AppEnv.getAppDir()+"resources/admin/divider.gif");
    final public static ImageIcon icoLogo = new ImageIcon(AppEnv.getAppDir()+"resources/common/logo-large.gif");
    ///////////////////////WINDOW ICONS/////////////////////////
    final public static ImageIcon icoWindow = new ImageIcon(AppEnv.getAppDir()+"resources/common/v2generic.gif");
    final public static ImageIcon icoTimeliner = new ImageIcon(AppEnv.getAppDir()+"resources/common/timeliner.gif");

    // menu icons
    final public static ImageIcon icoBold = new ImageIcon(AppEnv.getAppDir()+ "resources/annotation/BOLD_1.gif");
    final public static ImageIcon icoItalic = new ImageIcon(AppEnv.getAppDir()+ "resources/annotation/ITALIC_1.gif");
    final public static ImageIcon icoUnderline = new ImageIcon(AppEnv.getAppDir()+ "resources/annotation/UNDERLINE_1.gif");
    final public static ImageIcon icoRed = new ImageIcon(AppEnv.getAppDir()+ "resources/annotation/lineColorRed.gif");
    final public static ImageIcon icoGreen = new ImageIcon(AppEnv.getAppDir()+ "resources/annotation/lineColorGreen2.gif");
    final public static ImageIcon icoBlue = new ImageIcon(AppEnv.getAppDir()+ "resources/annotation/lineColorBlue.gif");
    final public static ImageIcon icoBlack = new ImageIcon(AppEnv.getAppDir()+ "resources/annotation/lineColorBlack.gif");
    final public static ImageIcon icoYellow = new ImageIcon(AppEnv.getAppDir()+ "resources/annotation/lineColorYellow.gif");
    final public static ImageIcon icoOrange = new ImageIcon(AppEnv.getAppDir()+ "resources/annotation/lineColorOrange.gif");
    final public static ImageIcon icoGray = new ImageIcon(AppEnv.getAppDir()+ "resources/annotation/lineColorGray.gif");
    final public static ImageIcon icoPink = new ImageIcon(AppEnv.getAppDir()+ "resources/annotation/lineColorPink.gif");
    final public static ImageIcon icoCyan = new ImageIcon(AppEnv.getAppDir()+ "resources/annotation/lineColorCyan.gif");
    final public static ImageIcon icoMagenta = new ImageIcon(AppEnv.getAppDir()+ "resources/annotation/lineColorMagenta.gif");
    final public static Image imgOverlap = Toolkit.getDefaultToolkit().getImage(AppEnv.getAppDir()+ "resources/media/over.gif");

    // timeliner icons
    final public static ImageIcon icoLeftSmall = new ImageIcon(AppEnv.getAppDir()+"resources/media/moveleftSmall.gif");
    final public static ImageIcon icoRightSmall = new ImageIcon(AppEnv.getAppDir()+"resources/media/moverightSmall.gif");
    final public static ImageIcon icoUpSmall = new ImageIcon(AppEnv.getAppDir()+"resources/media/moveupSmall.gif");
    final public static ImageIcon icoDownSmall = new ImageIcon(AppEnv.getAppDir()+"resources/media/movedownSmall.gif");
    final public static ImageIcon icoMediaDivider = new ImageIcon(AppEnv.getAppDir()+"resources/media/divider.gif");
    final public static ImageIcon icoEdit = new ImageIcon(AppEnv.getAppDir()+"resources/media/editbubble.gif");
    final public static ImageIcon icoAdd = new ImageIcon(AppEnv.getAppDir()+"resources/media/addtimepoint.gif");
    final public static ImageIcon icoAddMarker = new ImageIcon(AppEnv.getAppDir()+"resources/media/addmarker.gif");
    final public static ImageIcon icoBigger = new ImageIcon(AppEnv.getAppDir()+"resources/media/bigger.gif");
    final public static ImageIcon icoSmaller = new ImageIcon(AppEnv.getAppDir()+"resources/media/smaller.gif");
    final public static ImageIcon icoInfoImage = new ImageIcon(AppEnv.getAppDir()+"resources/media/info.gif");
    final public static ImageIcon icoResizer = new ImageIcon(AppEnv.getAppDir()+"resources/media/resizer.gif");

    final public static String infoString = AppEnv.getAppDir()+"resources/media/info.gif";
    final public static Image imgDescription = Toolkit.getDefaultToolkit().getImage(AppEnv.getAppDir()+"resources/media/info.gif");
    final public static Image imgDescriptionHover = Toolkit.getDefaultToolkit().getImage(AppEnv.getAppDir()+"resources/media/info_hover.gif");
    final public static Image imgDescriptionOutline = Toolkit.getDefaultToolkit().getImage(AppEnv.getAppDir()+"resources/media/info_outline.gif");

    // timeline cursors
    final public static Image imgMovePoint = Toolkit.getDefaultToolkit().getImage(AppEnv.getAppDir()+"resources/media/movepoint.gif");
    final public static Image imgHandPoint = Toolkit.getDefaultToolkit().getImage(AppEnv.getAppDir()+"resources/media/handpoint.gif");
    final public static Image imgHandOpen = Toolkit.getDefaultToolkit().getImage(AppEnv.getAppDir()+"resources/media/handopen.gif");
    final public static Image imgHandClosed = Toolkit.getDefaultToolkit().getImage(AppEnv.getAppDir()+"resources/media/handclosed.gif");

    //the following string gets passed to panes that are rendering HTML
    static public final String fontHTML = "body { font-family : Arial Unicode MS, Lucida Sans Unicode, Lucida Grande; }";
    static public final int fontSizeHTML = convertFontSize(12); // font size for HTML rendered text

    static public Border bordButton = BorderFactory.createEtchedBorder(Color.white, new Color(148, 145, 140));
    static JFileChooser fc = new JFileChooser();

    private UIUtilities() {
    }

    //-----------------------------GENERAL UTILITIES----------------------------//

    /**
     * Switches the border on for buttons when the mouse is over them. Only works on Windows
     * platform, since behavior on Mac is erratic.
     */
    public static void doButtonBorderSwitch(MouseEvent e, JButton button) {
        if (!(System.getProperty("os.name").startsWith("Mac OS"))) {
            if (!(button.hasFocus())) {
                if (button.getBorder() == null) {
                    button.setBorder(UIUtilities.bordButton);
                } else {
                    button.setBorder(null);
                }
            }
        }
    }

    /**
     * Switches button borders on or off.
     */
    public static void doButtonBorderSwitch(FocusEvent e, JButton button, boolean turnOn) {
        if (!(System.getProperty("os.name").startsWith("Mac OS"))) {
            if (turnOn) {
                button.setBorder(UIUtilities.bordButton);
            } else {
                button.setBorder(null);
            }
        }
    }

    //-----------------------------TIME CONVERSION UTILITIES----------------------------//

    /**
     * Converts a millisecond offset into a
     * friendlier hh:mm:ss format & returns the string. If time is less than one
     * hour, returns mm:ss only.
     *
     * @param offset the offset to be converted
     * @return the String representation of the time value
     */
    static public String convertOffsetToHoursMinutesSeconds(int offset) {
        int seconds = (int)(offset / 1000);
        int minutes = seconds / 60;
        int hours = minutes / 60;
        String strSeconds = new String();
        String strMinutes = new String();
        seconds = seconds - (minutes * 60);
        minutes = minutes - (hours * 60);
        if (seconds < 10) {
            strSeconds = "0" + seconds;
        } else {
            strSeconds = "" + seconds;
        }
        if ((minutes < 10) && (hours > 0)) {
            strMinutes = "0" + minutes;
        } else {
            strMinutes = "" + minutes;
        }
        if (hours > 0) {
            return(hours + ":" + strMinutes + ":" + strSeconds);
        } else {
            return(strMinutes + ":" + strSeconds);
        }
    }

    /**
     * Converts a millisecond offset into a
     * friendlier mm:ss:ccc format & returns the string.
     *
     * @param offset the offset to be converted
     * @return the String representation of the time value
     */
    static public String convertOffsetToMinutesSecondsMillis(int offset) {
        int millis = offset;
        int seconds = (int)(millis / 1000);
        int minutes = seconds / 60;
        String strMillis = new String();
        String strSeconds = new String();
        String strMinutes = new String();
        millis = millis - (seconds * 1000);
        seconds = seconds - (minutes * 60);
        if (millis < 10) {
            strMillis = "00" + millis;
        } else if (millis < 100) {
            strMillis = "0" + millis;
        } else {
            strMillis = "" + millis;
        }
        if (seconds < 10) {
            strSeconds = "0" + seconds;
        } else {
            strSeconds = "" + seconds;
        }
        strMinutes = "" + minutes;
        return(strMinutes + ":" + strSeconds + ":" + strMillis);
    }

    /**
     * Converts a millisecond offset into a
     * friendlier hh:mm:ss.t format & returns the string. If time is less than one
     * hour, returns mm:ss only.
     *
     * @param offset the offset to be converted
     * @return the String representation of the time value
     */
    static public String convertOffsetToHoursMinutesSecondsTenths(int offset) {
        int tenths = (int)(offset / 100);
        int seconds = (int)(offset / 1000);
        int minutes = seconds / 60;
        int hours = minutes / 60;
        String strTemp = "" + (tenths % 10);
        String strTenths = new String();
        String strSeconds = new String();
        String strMinutes = new String();
        seconds = seconds - (minutes * 60);
        minutes = minutes - (hours * 60);
        if (tenths == 0) {
            strTenths = "";
        } else {
            strTenths = "." + strTemp;
        }
        if (seconds < 10) {
            strSeconds = "0" + seconds;
        } else {
            strSeconds = "" + seconds;
        }
        if ((minutes < 10) && (hours > 0)) {
            strMinutes = "0" + minutes;
        } else {
            strMinutes = "" + minutes;
        }
        if (hours > 0) {
            return(hours + ":" + strMinutes + ":" + strSeconds + strTenths);
        } else {
            return(strMinutes + ":" + strSeconds + strTenths);
        }
    }

    /**
     * Given a string of the form mm:ss:ccc or mm:ss, returns the total
     * milliseconds
     *
     * @param offset the offset in String form
     * @return the int value in milliseconds
     * @throws NumberFormatException if String can't be converted
     */
    static public int convertMinutesSecondsMillisToMilliseconds(String offset) throws NumberFormatException {
        boolean isOK = true;
        StringTokenizer tokenizer = new StringTokenizer(offset,":");
        String token = null;
        int minutes = 0;
        int seconds = 0;
        int milliseconds = 0;

        try { token = tokenizer.nextToken(); } catch (NoSuchElementException e) { isOK = false; }
        try { minutes = Integer.parseInt(token); } catch (NumberFormatException e) { isOK = false; }
        try { token = tokenizer.nextToken(); } catch (NoSuchElementException e) { isOK = false; }
        try { seconds = Integer.parseInt(token); } catch (NumberFormatException e) { isOK = false; }
        try { token = tokenizer.nextToken(); } catch (NoSuchElementException e) { }    ///still ok, since we have mins and secs now
        try { milliseconds = Integer.parseInt(token); } catch (NumberFormatException e) { }

        if (!isOK) {
            throw new NumberFormatException();
        }
        return((minutes * 60000) + (seconds * 1000) + milliseconds);
    }

    
    /**
     * Given a font size, converts it to look right in different screen resolutions
     */
    static public int convertFontSize(int size) {
    	
	    double ppi = Toolkit.getDefaultToolkit().getScreenResolution();
	    int newsize = (int)Math.round( size / (96 / ppi));
	    return newsize;
	    //return size;
    }
    
    /**
     * Given a pixel amount, converts it to look right in different screen resolutions
     */
    static public int scalePixels(int size) {
    	
	    double ppi = Toolkit.getDefaultToolkit().getScreenResolution();
	    int newsize = (int)Math.round( size / (96 / ppi));
	    return newsize;
	    //return size;
    }
    
    static public int scaleHeight(int size) {
    	
	    double ppi = Toolkit.getDefaultToolkit().getScreenResolution();
	    int newsize = (int)Math.round( size / (110 / ppi));
	    return newsize;
	    //return size;
    }
 
    /**
     * Given a string of the form hh:mm:ss or mm:ss, returns the total
     * milliseconds
     *
     * @param offset the offset in String form
     * @return the int value in milliseconds
     * @throws NumberFormatException if String can't be converted
     */
    static public int convertHoursMinutesSecondsToMilliseconds(String offset) throws NumberFormatException {
        boolean isOK = true;
        StringTokenizer tokenizer = new StringTokenizer(offset, ":");
        String token = null;

        int first = -1;
        int second = -1;
        int third = -1;

        try { token = tokenizer.nextToken(); } catch (NoSuchElementException e) { isOK = false; }
        try { first = Integer.parseInt(token); } catch (NumberFormatException e) { isOK = false; }

        try { token = tokenizer.nextToken(); } catch (NoSuchElementException e) { isOK = false; }
        try { second = Integer.parseInt(token); } catch (NumberFormatException e) { isOK = false; }

        if (tokenizer.hasMoreTokens()) {
            try { token = tokenizer.nextToken(); } catch (NoSuchElementException e) { } { isOK = false; }
            try { third = Integer.parseInt(token); } catch (NumberFormatException e) {} { isOK = false; }
        }

        if (!isOK) {
            throw new NumberFormatException();
        }

        if (third == -1) {
            return((first * 60000) + (second * 1000));
        } else {
            return((first * 3600000) + (second * 60000) + (third * 1000));
        }
    }
    
    static public String htmlCleanup(String html) {
    	
  	  html = html.replaceAll("<html>", "");
  	  html = html.replaceAll("<head>", "");
  	  html = html.replaceAll("<body>", "");
  	  html = html.replaceAll("</body>", "");
  	  html = html.replaceAll("</html>", "");
  	  html = html.replaceAll("</head>", "");
  	  html = html.replaceAll("<style>(.|\\n)*?</style>", "");
  	  html = html.replaceAll("font-size: 11pt; font-family: Arial Unicode MS", "");
  	  html = html.replaceAll("font-size: 12pt; font-family: Arial Unicode MS", "");
  	  html = html.replaceAll("font-size: 14pt; font-family: Arial Unicode MS", "");
  	  html = html.replaceAll("font-size: 18pt; font-family: Arial Unicode MS", "");
  	  html = html.replaceAll("font-size: 22pt; font-family: Arial Unicode MS", "");
  	  html = html.replaceAll("font-size: 24pt; font-family: Arial Unicode MS", "");
  	  html = html.replaceAll("<span style=\"\">", "");
  	  html = html.replaceAll("<p class=default>", "");
  	  html = html.replaceAll("</p>", "");
  	  html = html.replaceAll("</span>", "");
  	  //html = html.replaceAll("</span> </span>", "</span>");
  	  html = html.replaceAll("&#160;", "");
  	  html = html.replaceAll("\\s+", " ");
  	  //html = html.trim();
  	  //html = html.strip();
  	  
  	  return html;
  	  
    }
    
    static public String removeTags(String html) {
    	String nohtml = html.toString().replaceAll("\\<.*?>","");
    	return nohtml;
    }
}
