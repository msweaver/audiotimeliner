package ui.common;

import com.apple.mrj.*;
import javax.swing.*;

/**
 * Handle Apple application menu events - quit and about
 *
 * @author Jim Halliday
 */
public class MacMenuHandler implements MRJAboutHandler, MRJQuitHandler {

    public MacMenuHandler() {
    }

    /**
     * Mac-only 'about' menu option called
     */
    public void handleAbout() {
        JOptionPane.showMessageDialog(null, new Object[] {"Audio Timeliner" + "\n" +
                "Version 3.0" + "\n" +
                "Copyright 2022" + "\n" + "Brent Yorgason" + "\n" + "Brigham Young University"},
                "About Audio Timeliner", JOptionPane.INFORMATION_MESSAGE, UIUtilities.icoTimeliner);
    }

    /**
     * Mac-only quit menu option called. We must call the shut down process
     * directly AND from a thread
     */
    public void handleQuit() {
        WindowManager.doShutDown();

        QuitThread quitThread = new QuitThread();
        quitThread.start();
    }

    /**
     * The quit thread. Has to be in a thread due to a MRJ Mac bug.
     */
    static private class QuitThread extends Thread {

        public void run() {
            WindowManager.doShutDown();
        }
    }
}
