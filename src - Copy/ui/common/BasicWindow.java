package ui.common;

import java.awt.AWTEvent;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import ui.common.menu.BasicMenuBar;
import util.logging.LogUtil;
import util.logging.UIEventType;
import util.logging.UILogger;

/**
 * Base class for all (parentless) windows. 
 */
public class BasicWindow extends JFrame {

	private static final long serialVersionUID = 1L;
	// Whether this window is displaying a MenuBar.
    public boolean hasMenuBar = false;
    public BasicMenuBar basicMenuBar = new BasicMenuBar(this);
    protected long serialNumber;
    // Type of window. See WindowManager for the list of possible window type constants.
    protected int windowType;
    protected UILogger uilogger;

    /**
     * This contructor should not be used in application. It is there so JBuilder
     * GUI designer will work since it needs to use BasicWindow() to create an instance
     */
    public BasicWindow() {
        super();
        windowType = -1;
        uilogger= null;
    }
    /**
     * Empty constructor does NOT add a menubar by default.
     *
     * @param windowType window type See WindowManager for a list of types
     */
    public BasicWindow(int windowType) {
        this.windowType = windowType;
        enableEvents(AWTEvent.WINDOW_EVENT_MASK);
        // get a unique number for this window for logging purposes
        serialNumber = WindowManager.generateSerialNumber();
        uilogger = getUILogger();
    }

    /**
     * This constructor gives the option of adding a menu bar.
     *
     * @param windowType window type See WindowManager for a list of types
     * @param addMenuBar when set to true will create a menu bar for this window automatically with common menus preloaded.
     */
    public BasicWindow(int windowType, boolean addMenuBar) {
        this(windowType);
        if (addMenuBar) {
            this.setJMenuBar(basicMenuBar);
            hasMenuBar = true;
        } else if (System.getProperty("os.name").startsWith("Mac OS")) {
            // ALL Mac windows should get a menu
            this.setJMenuBar(basicMenuBar);
            hasMenuBar = true;
        }
    }

    /**
     * Add a menu bar to this window.
     */
    public void addMenuBar() {
        if (hasMenuBar == false) {
            this.setJMenuBar(basicMenuBar);
            hasMenuBar = true;
        }
    }

    /**
     * Get the unique serial number for this window (for logging purposes only, this is NOT the regular window ID).
     *
     * @return the serial number for this window
     */
    public long getSerialNumber() {
        return serialNumber;
    }

    /**
     * Catches changes to a window's title, so that the WindowManager can be informed of the change
     *
     * @see ui.common.WindowManager
     * @param title the title of this window
     */
    public void setTitle(String title) {
        //programatically change 'flat' and 'sharp' signs to the words 'flat' and 'sharp'
        if (title.length() > 75) {
            title = title.substring(0, 73);
        }
        int counter = 0;
        int length = title.length();
        while (counter < length) {
            if ((title.substring(counter, counter + 1).equals("\u266D")) || (title.substring(counter, counter + 1).equals("\u266F"))) {
                String part1 = title.substring(0, counter);
                String part2 = title.substring(counter + 1, length);
                if (title.substring(counter, counter + 1).equals("\u266D")) {
                    title = part1 + "-flat" + part2;
                } else {
                    title = part1 + "-sharp" + part2;
                }
                //recurse to catch all instances
                setTitle(title);
                return;
            }
            counter = counter + 1;
        }
        super.setTitle(title);
        WindowManager.updateWindowMenus();
    }

    /**
     * Update the window menu for this window. Called by the WindowManager.
     *
     * @see ui.common.WindowManager
     */
    public void doWindowMenuUpdate() {
        if (this.basicMenuBar != null) {
            basicMenuBar.menuWindow.populateWindowMenu();
        }
    }

    protected void processWindowEvent(WindowEvent e) {
        super.processWindowEvent(e);
        if (e.getID() == WindowEvent.WINDOW_CLOSING) {
            doWindowClose();
        }
    }

    /**
     * Close this window, remove it from the WindowManager list, and dispose it
     */
    protected void doWindowClose() {
        /* logger has to be put in front of removeWindow(), because the program */
        /* may exit when calling removeWindow() */
        uilogger.log(UIEventType.WINDOW_CLOSED, "remaining open window count - " + (WindowManager.getOpenWindows().size() - 1));
        WindowManager.removeWindow(this);
//        this.dispose();                 //assuming for now that closed windows should be disposed
    }

    public int getWindowType() {
        return windowType;
    }

    public UILogger getUILogger() {
        if (uilogger == null) {
            uilogger = LogUtil.getUILogger(getWindowType(), getSerialNumber());
        }
        return uilogger;
    }
}
