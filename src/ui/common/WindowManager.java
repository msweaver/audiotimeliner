package ui.common;

import java.util.*;
import java.awt.*;
import javax.swing.*;
import org.apache.log4j.Logger;
import client.*;
import ui.timeliner.*;

/**
 * This class is for keeping track of open windows, and other
 * UI window management functions which are beyond the scope of a single window. In addition, certain global client
 * objects are methods kept here, such as the bookmark tree object and the shutdown/startup methods.
 * All methods and variables are static; the WindowManager class itself cannot be instantiated.
 *
 * @author Jim Halliday
 */
public class WindowManager {

    private static Logger log = Logger.getLogger(WindowManager.class);

    ///////////////////////WINDOW TYPES/////////////////////////
    final static public int WINTYPE_LOCAL_TIMELINE = 0;
    final static public int WINTYPE_TIMELINE = 1;
    final static public int WINTYPE_RECORD_VIEW = 2;
    //////////////////////INITIAL WINDOW LOCATIONS/////////////
    final static public int WINLOCATION_CASCADE_FROM_TOP_LEFT = 0;
    final static public int WINLOCATION_CASCADE_FROM_PARENT_WINDOW = 1;
    final static public int WINLOCATION_TILE_FROM_PARENT_WINDOW = 2;
    final static public int WINLOCATION_SCREEN_CENTER = 3;
    final static public int WINLOCATION_TOP_LEFT = 4;
    final static public int WINLOCATION_CASCADE_DOWN = 5;
    //Number of pixels to move over and down each time when cascading windows
    final static public int CASCADING_OFFSET = 18;
    //minimum number of pixels that the top left-hand corner of window is above the bottom
    //(or to the left of the right side) to stop cascading
    final static public int CASCADING_LIMIT = 110;

    ////////////////////VARIABLES/////////////////////////////////////////////////
    //Used to create ID's for empty windows. This number is incremented upwards as new windows are created. These ID's are used internally
    //by the WindowManager to keep track of open windows. The serial number ID is something different.
    static public long emptyWindowID = -10000;
    //Used to generate window serial numbers.
    static public long serialNumberID = 0;
    //Vector of WindowTracker objects to keep track of the currently open windows.
    private static Vector<WindowTracker> openWindows = new Vector<WindowTracker>();
    //Hashtable of local image files, with URL's as keys.
    //public static Hashtable imageCache = new Hashtable();
    static Image winImage = null;

    /**
     * Constructor is private so that it can't be instatiated
     */
    private WindowManager() {}

    /**
     * This method gets called whenever the application starts, regardless of startup method.
     */
    public static void doStartUp() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());    //set PLAF
        }
        catch(Exception e) {
            JOptionPane.showMessageDialog(null, "Look-and-feel could not be set.",
                                          "Error initializing app.", JOptionPane.ERROR_MESSAGE);
            Client.shutdown();
        }
        try {
/**            if (QTSession.isInitialized() == false) {   //initialize QT
                try {
                    QTSession.open();                   //open a quicktime session
                } catch (QTException e) {
                    JOptionPane.showMessageDialog(null, "Error initializing QuickTime.",
                            "Error initializing app.", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                    Client.shutdown();
                }
                //loop until done
                while (QTSession.isInitialized() == false) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException except) {
                        ; // ignore
                    }
                }
            }
**/        } catch (UnsatisfiedLinkError err) {
            //will happen in QuickTime is not installed AT ALL
            JOptionPane.showMessageDialog(null, "Error initializing QuickTime.",
                            "Error initializing app.", JOptionPane.ERROR_MESSAGE);
                    err.printStackTrace();
                    Client.shutdown();
        }
        if (System.getProperty("os.name").startsWith("Mac OS")) {       //Mac specific stuff
            //put menu bar on the top of screen
            System.setProperty("com.apple.macos.useScreenMenuBar", "true");
            System.setProperty("com.apple.macos.use-file-dialog-packages", "true");  //don't allow navigation into apps
            //the following line does nothing if uncommented, must be set with AppBuilder instead, otherwise no about menu appears
//            //System.setProperty("com.apple.mrj.application.apple.menu.about.name", "About Variations2");  //add 'about' to app menu
            System.setProperty("com.apple.mrj.application.growbox.intrudes", "true");       //looks bad otherwise
            //MacMenuHandler x = new MacMenuHandler(); 
            //trigger application's quit method appropriately
            //MRJApplicationUtils.registerQuitHandler(x);
            //about menu handler for mac
            //MRJApplicationUtils.registerAboutHandler(x);
        }

        if (System.getProperty("os.name").startsWith("Mac OS")) {
            // OpenDocument handler for Mac
            //MacOpenHandler openHandler = new MacOpenHandler();
            //MRJApplicationUtils.registerOpenDocumentHandler(openHandler);
        }
    }

 
    /**
     * Close all windows and shutdown the app.
     */
    public static void doShutDown() {
        log.debug("Starting the shutdown process");
        //don't show confirmation dialog on mac
       // if (!(System.getProperty("os.name").startsWith("Mac OS"))) {
            if (openWindows.size() > 1) {
                //more than one window is open; send warning to user
                int response = JOptionPane.showConfirmDialog(
                    null, "Do you want to close all Timeliner windows and exit the application?",
                    "Exit confirmation", JOptionPane.YES_NO_OPTION);
                if (response == JOptionPane.NO_OPTION ||
                    response == JOptionPane.CANCEL_OPTION ||
                    response == JOptionPane.CLOSED_OPTION) {
                    return;         //user canceled shut down
                }
            }
       // }
        //close all open windows one by one
        int counter = 0;
        int tempsize = openWindows.size();
        log.debug("closing " + tempsize + " windows");
        while (counter < tempsize) {
            BasicWindow tempWindow = ((WindowTracker)openWindows.get(0)).win;
            if (tempWindow instanceof TimelineFrame) {
                if (((TimelineFrame)tempWindow).tryClose() == false) {
                    //abort shut down process
                    return;
                }
            }
            ((WindowTracker)openWindows.get(0)).win.doWindowClose();
            counter = counter + 1;
        }
        Client.shutdown();
    }

    /**
     * This call is made when a user initiates an action that should open a new window.
     * This call should be used each time a new primary (parentless) window should be
     * instantiated.
     *
     * @param windowType the type of window to be created
     * @param windowID a unique ID for the window. If a window with this ID is already open, this window is brought to the front
     *      instead and the parentWindow and windowLocation parameters are ignored. If no ID is specified, an ID will be assigned automatically. Note that if no ID is specified, a new window is
     *      ALWAYS opened.
     * @param parentWindow the window that the new window is related to. Specifying this window is only necessary if the windowLocation
     *      relies on the parent window to place the new window.
     * @param windowLocation how the window manager should choose where to place the new window
     * @return  The window that has been either created or brought to the front. Returns null if the window couldn't be created.
     */
    public static BasicWindow openWindow(int windowType, String windowID, JFrame parentWindow, int windowLocation) {

        WindowTracker wt = new WindowTracker();     //we'll be filling out a WindowTracker object as we go
        //first, assign an ID to the window
        if ((windowID == null)) {
            emptyWindowID = emptyWindowID + 1;      //assign ID to this window - will conflict if more than 10,000 windows are open :)
            wt.winID = String.valueOf(emptyWindowID);
        } else {
            int temptype = -1;                      //check to see if window exists
            String tempID = "";
            int counter = 0;
                wt.winID = windowID;
             while (counter < openWindows.size()) {
                temptype = ((WindowTracker)(openWindows.get(counter))).winType;
                tempID = ((WindowTracker)(openWindows.get(counter))).winID;
                if ((temptype == windowType) && (tempID.equals(wt.winID))) {
                    wt.win = ((WindowTracker)openWindows.get(counter)).win;         //window found
                    WindowManager.toFront(wt.win);                                  //bring to front
                    return wt.win;                                                  //no need to keep going
                }
                counter = counter + 1;
            }
        }
        //next, assign a type
        wt.winType = windowType;
        //default icon
        winImage = UIUtilities.icoWindow.getImage();       //set window icon
        //next, create the window itself
        switch (windowType) {
            case WindowManager.WINTYPE_LOCAL_TIMELINE:
                TimelineFrame tFrame;
                try {
                    tFrame = new TimelineFrame(true);
                } catch (Exception e) {WindowManager.showOpeningErrorDialog(e); return null;}
                tFrame.validate();
                wt.win = tFrame;
                winImage = UIUtilities.icoTimeliner.getImage();
                break;
            default:
                return null;        //type isn't specified in list; abort
        }
        wt.win.setIconImage(winImage);

        //next, let's find a location for this window
        switch (windowLocation) {
            case WindowManager.WINLOCATION_CASCADE_FROM_TOP_LEFT:
                wt.win.setLocation(WindowManager.getNewCascadeLocation(null));
                break;
            case WindowManager.WINLOCATION_CASCADE_FROM_PARENT_WINDOW:
                wt.win.setLocation(WindowManager.getNewCascadeLocation(parentWindow));
                break;
            case WindowManager.WINLOCATION_TILE_FROM_PARENT_WINDOW:
                wt.win.setLocation(WindowManager.getNewTileLocation(parentWindow, wt.win));
                break;
            case WindowManager.WINLOCATION_SCREEN_CENTER:
                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                Dimension frameSize = wt.win.getSize();
                if (frameSize.height > screenSize.height) {
                    frameSize.height = screenSize.height;
                }
                if (frameSize.width > screenSize.width) {
                    frameSize.width = screenSize.width;
                }
                wt.win.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
                break;
            case WindowManager.WINLOCATION_TOP_LEFT:
                wt.win.setLocation(0, 0);
                break;
            case WindowManager.WINLOCATION_CASCADE_DOWN:
                if (windowType == WindowManager.WINTYPE_LOCAL_TIMELINE) {
                  int windowNumber = TimelineFrame.windowNumber - 1;
                  wt.win.setLocation(0, (windowNumber - 1) * CASCADING_OFFSET);
                }
                else {
                  wt.win.setLocation(WindowManager.getNewCascadeLocation(null));
                }
                break;
            default:
                wt.win.setLocation(WindowManager.getNewCascadeLocation());
                break;
        }
        //done! now show window and add it to windows list
        wt.win.setVisible(true);
        openWindows.add(wt);
        updateWindowMenus();        //let open windows know about new addition
        return wt.win;
    }

    public static BasicWindow openWindow(int windowType) {
        return WindowManager.openWindow(windowType, null, null, WindowManager.WINLOCATION_CASCADE_FROM_TOP_LEFT);
    }

    public static BasicWindow openWindow(int windowType, String windowID) {
        return WindowManager.openWindow(windowType, windowID, null, WindowManager.WINLOCATION_CASCADE_FROM_TOP_LEFT);
    }

    public static BasicWindow openWindow(int windowType, JFrame parentWindow) {
        return WindowManager.openWindow(windowType, null, parentWindow, WindowManager.WINLOCATION_CASCADE_FROM_TOP_LEFT);
    }

    public static BasicWindow openWindow(int windowType, String windowID, JFrame parentWindow) {
        return WindowManager.openWindow(windowType, windowID, parentWindow, WindowManager.WINLOCATION_CASCADE_FROM_TOP_LEFT);
    }

    public static BasicWindow openWindow(int windowType, int windowLocation) {
        return WindowManager.openWindow(windowType, null, null, windowLocation);
    }

    public static BasicWindow openWindow(int windowType, String windowID, int windowLocation) {
        return WindowManager.openWindow(windowType, windowID, null, windowLocation);
    }

    public static BasicWindow openWindow(int windowType, JFrame parentWindow, int windowLocation) {
        return WindowManager.openWindow(windowType, null, parentWindow, windowLocation);
    }

    /**
     * Turn off all windows currently playing audio.
     */
    public static void stopAllPlayers() {
        int counter = 0;
        WindowTracker tempTracker;
        BasicWindow tempWindow;
        TimelinePlayer tempTimelinePlayer = null;
        TimelineLocalPlayer tempTimelineLocalPlayer = null;
        while (counter < openWindows.size()) {
            tempTracker = ((WindowTracker)(openWindows.get(counter)));
            tempWindow = tempTracker.win;
            if (tempWindow instanceof TimelineFrame) {
              if (((TimelineFrame)tempWindow).isStandaloneVersion) {
                tempTimelineLocalPlayer = ((TimelineFrame)tempWindow).getTimelineLocalPlayer();
                if (tempTimelineLocalPlayer != null && tempTimelineLocalPlayer.isPlaying) {
                  tempTimelineLocalPlayer.doPause();
                }
              }
              else {
                tempTimelinePlayer = ((TimelineFrame)tempWindow).getTimelinePlayer();
                if (tempTimelinePlayer.isPlaying) {
                    tempTimelinePlayer.doPause();
                }
              }
            }
            counter = counter + 1;
        }
    }

     /**
      * Set the ID for a window. Usually should be called whenever content is set for a window, if not known from the beginning.
      *
      * @param wind The window whose ID should be set
      * @param windID The new ID for the selected window
      * @returns false if the method fails (the specified window isn't in the list, or the specified ID already is being used)
      */
    public static boolean setWindowID(BasicWindow wind, String windID) {
        int counter = 0;
        WindowTracker tempTracker;
        //first check to make sure the specified ID doesn't already exist
        while (counter < openWindows.size()) {
            tempTracker = ((WindowTracker)(openWindows.get(counter)));
            if (tempTracker.winID == windID) {
                return false;           //ID already exists; can't be used again
            }
            counter = counter + 1;
        }
        counter = 0;
        while (counter < openWindows.size()) {
            tempTracker = ((WindowTracker)(openWindows.get(counter)));
            if (tempTracker.win == wind) {      //find the specified window in the window list
                tempTracker.winID = windID;     //window found; set ID
                return true;                    //done now
            }
            counter = counter + 1;
        }
        return false;                 //window was never found
    }

    /**
     * Update the Window menu on all currently open windows.
     */
    public static void updateWindowMenus() {
        int counter = 0;
        while (counter < openWindows.size()) {
            ((WindowTracker)(openWindows.get(counter))).win.doWindowMenuUpdate();
            counter = counter + 1;
        }
    }

    /**
     * Delete a window from the list of windows. This method is called automatically whenever a BasicWindow
     * closes. If no BasicWindow remains open, this method calls for App shutdown.
     *
     * @see BasicWindow
     * @param wind the window to remove
     */
    public static void removeWindow(BasicWindow wind) {
        int counter = 0;
        while (counter < openWindows.size()) {
            if (((WindowTracker)(openWindows.get(counter))).win == wind) {
                openWindows.remove(counter);
                if (wind.isVisible()) {
                    wind.setVisible(false);
//                    wind.dispose();
                }
            }
            counter = counter + 1;
        }
        if (openWindows.isEmpty()) {
            doShutDown();               //shut down if no open windows left
        }
        updateWindowMenus();            //notify all open windows of the change
    }

    /**
     * Retrieve a list of currently open windows objects.
     * Used by the MenuWindow class to populate its menu.
     *
     * @return A vector of BasicWindow objects (not WindowTracker objects).
     * @see ui.common.menu.MenuWindow
     */
     public static Vector<BasicWindow> getOpenWindows() {
        Vector<BasicWindow> windows = new Vector<BasicWindow>();
        int counter = 0;
        while (counter < openWindows.size()) {
            windows.add(((WindowTracker)(openWindows.get(counter))).win);
            counter = counter + 1;
        }
        return windows;
     }

     /**
      * Bring the specified window to the front.
      *
      * @see ui.common.menu.MenuWindow
      * @param windowKey The ID of the window to close; corresponds to the number in the Window menu, and to the
      * index in the openWindows Vector.
      */
     public static void toFront(int windowKey) {
        BasicWindow temp = (((WindowTracker)(openWindows.get(windowKey))).win);
        if (temp == null) {
            return;
        }
        WindowManager.toFront(temp);
     }

     /**
      * Bring the specified window to the front. Called by MenuWindow when the user makes a window selection. May also be called by
      * other classes.
      *
      * @see ui.common.menu.MenuWindow
      * @param window The window to close.
      */
     public static void toFront(BasicWindow window) {
        if (window == null) {
            return;
        }
        if (window.getState() == JFrame.ICONIFIED) {
            window.setState(JFrame.NORMAL);           //deiconify before bringing to front
        }
        window.toFront();     //bring to front
     }

    /**
     * Generate a unique serial number for each BasicWindow. This is different from the regular window ID and cannot change once a
     * window is created. Windows receives an ID when they are first constructed. This is for logging purposes.
     *
     * @return the newly created serial number
     */
    public synchronized static long generateSerialNumber() {
        serialNumberID = serialNumberID + 1;
        return serialNumberID;
    }

    /**
     * Get a location to put a new window on the screen, cascading from the top left corner of screen.
     *
     * @return the Point at which to place the window
     */
    public static Point getNewCascadeLocation() {
        return WindowManager.getNewCascadeLocation(null);
    }

    /**
     * Get a location to put a new window on the screen, cascading from the specified window.
     *
     * @param parentWindow the parent window to the new window
     * @return the Point at which to place the window
     */
     public static Point getNewCascadeLocation(Container parentWindow) {
        int counter = 0;
        int newx;
        int newy;
        int oldx = 0;
        int oldy = 0;
        if (parentWindow == null) {
            newx = 0;           //use top, left-hand corner of screen instead
            newy = 0;
        } else {
            newx = parentWindow.getX();
            newy = parentWindow.getY();
        }
        while (counter < openWindows.size()) {                      //cascade the windows
            oldx = ((WindowTracker)openWindows.get(counter)).win.getX();
            oldy = ((WindowTracker)openWindows.get(counter)).win.getY();
            if ((newx == oldx) && (newy == oldy)) {                 //already a window here
                newx = newx + WindowManager.CASCADING_OFFSET;
                newy = newy + WindowManager.CASCADING_OFFSET;
            }
            counter = counter + 1;
        }
        int screenWidth = (int)java.awt.Toolkit.getDefaultToolkit().getScreenSize().getWidth();
        int screenHeight = (int)java.awt.Toolkit.getDefaultToolkit().getScreenSize().getHeight();
        if ((newx > (screenWidth - WindowManager.CASCADING_LIMIT)) || (newy > (screenHeight - WindowManager.CASCADING_LIMIT))) {
            newx = WindowManager.CASCADING_OFFSET;     //too many windows, back to top
            newy = WindowManager.CASCADING_OFFSET;
        }
        return new Point(newx, newy);
    }

    /**
     * Get a location to put a new window on the screen, tiling from the specified window. The tiling could be horizontal or vertical,
     * depending on where space is available. If no screen space is available, the window will instead cascade from the top corner.
     *
     * @param parentWindow the old window to tile to
     * @param childWindow the new window to tile
     * @return the Point at which to place the window
     */
     public static Point getNewTileLocation(JFrame parentWindow, JFrame childWindow) {
        if (parentWindow == null) {     //no parent, just put in top corner
            return new Point(0, 0);
        }
        Point p = new Point(parentWindow.getX() + parentWindow.getWidth(), parentWindow.getY());        //tile horizontally
        int screenWidth = (int)java.awt.Toolkit.getDefaultToolkit().getScreenSize().getWidth();
        int screenHeight = (int)java.awt.Toolkit.getDefaultToolkit().getScreenSize().getHeight();
        int counter;
        boolean onehere;
        counter = 0;
        onehere = false;
        while (counter < openWindows.size()) {
            WindowTracker tempTracker = (WindowTracker)openWindows.get(counter);
            BasicWindow tempWindow = tempTracker.win;
            if (tempWindow.getX() == p.getX()) {
                onehere = true;
            }
            counter = counter + 1;
        }
        if ((onehere == true) || ((p.getX() + childWindow.getWidth()) > screenWidth)) {
            p.setLocation(parentWindow.getX() - childWindow.getWidth(), parentWindow.getY());       //try the other side
            counter = 0;
            onehere = false;
            while (counter < openWindows.size()) {
                WindowTracker tempTracker = (WindowTracker)openWindows.get(counter);
                BasicWindow tempWindow = tempTracker.win;
                if (tempWindow.getX() == p.getX()) {
                    onehere = true;
                }
                counter = counter + 1;
            }
            if ((p.getX() < 1) || (onehere == true)) {
                p.setLocation(parentWindow.getX(), parentWindow.getY() + parentWindow.getHeight());     //try the bottom
                counter = 0;
                onehere = false;
                while (counter < openWindows.size()) {
                    WindowTracker tempTracker = (WindowTracker)openWindows.get(counter);
                    BasicWindow tempWindow = tempTracker.win;
                    if (tempWindow.getY() == p.getY()) {
                        onehere = true;
                    }
                    counter = counter + 1;
                }
                if ((onehere == true) || ((p.getY() + childWindow.getHeight()) > screenHeight)) {
                    p.setLocation(parentWindow.getX(), parentWindow.getY() -childWindow.getHeight());       //try the top
                    counter = 0;
                    onehere = false;
                    while (counter < openWindows.size()) {
                        WindowTracker tempTracker = (WindowTracker)openWindows.get(counter);
                        BasicWindow tempWindow = tempTracker.win;
                        if (tempWindow.getY() == p.getY()) {
                            onehere = true;
                        }
                        counter = counter + 1;
                    }
                    if ((onehere == true) || (p.getY() < 1)) {
                        p = WindowManager.getNewCascadeLocation();    //would have gone off the screen; cascade from top instead
                    }
                }
            }
        }
        return p;
    }

    /**
     * User has attempted to open a window, but some sort of error occured in initialization. This method displays an error msg only.
     *
     * @param e the exception to display
     */
    protected static void showOpeningErrorDialog(Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(),
                "Error Initiating Window", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Given a Window resizeMe, makes sure that the current size
     * is not less than the minimum size for each dimension; if it is, the
     * window is resized to the minimum in that dimension.
     *
     * @param resizeMe window to be resized
     */
    static public void resizeIfTooSmall(Window resizeMe) {
        int w = resizeMe.getWidth();
        int h = resizeMe.getHeight();
        if (w < resizeMe.getMinimumSize().getWidth()) {
            resizeMe.setSize((int)resizeMe.getMinimumSize().getWidth(), resizeMe.getHeight());
        }
        if (h < resizeMe.getMinimumSize().getHeight()) {
            resizeMe.setSize(resizeMe.getWidth(), (int)resizeMe.getMinimumSize().getHeight());
        }
    }

    /**
     * Given a window resizeMe, make sure that the current size is not less than
     * the minimum width and height specified; if it is, resize the window
     *
     * @param resizeMe window to be resized
     * @param minWidth minimum width for the window
     * @param minHeight minimum height for the window
     */
    static public void resizeIfTooSmall(Window resizeMe, int minWidth, int minHeight) {
        int w = resizeMe.getWidth();
        int h = resizeMe.getHeight();
        if (w < minWidth) {
            resizeMe.setSize(minWidth, resizeMe.getHeight());
        }
        if (h < minHeight) {
            resizeMe.setSize(resizeMe.getWidth(), minHeight);
        }
    }


}
