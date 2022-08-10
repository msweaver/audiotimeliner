package ui.common.menu;

import javax.swing.*;
import java.awt.event.*;
import java.util.*;

import ui.common.*;
import util.logging.*;

/**
 * The Window menu 
 */
public class MenuWindow extends JMenu {

	private static final long serialVersionUID = 1L;
	java.awt.Font windowFont;

    public MenuWindow() {
        this.setText("Window");
        if (System.getProperty("os.name").startsWith("Mac OS")) {
            //Mac specific stuff
            windowFont = UIUtilities.fontMenusMac;
        } else {
            //Windows specific stuff
            windowFont = UIUtilities.fontMenusWin;
            this.setMnemonic('w');
        }
        this.setFont(windowFont);
    }

    /**
     * Populate this Window menu with all the currently open windows.
     */
    public void populateWindowMenu() {
        int counter = 0;
        WindowMenuItem temp;
        Vector<BasicWindow> openWindows = new Vector<>();
        Vector<String> openWindowNames = new Vector<>();
        this.removeAll();                                   //clear out menu and start over
        openWindows = WindowManager.getOpenWindows();       //get list of currently open windows
        String title = "";
        while (counter < openWindows.size()) {              //extract window titles
            title = ((JFrame)(openWindows.get(counter))).getTitle();
            title = (counter + 1) + " " + title;
            openWindowNames.add(title);
            counter = counter + 1;
        }
        counter = 0;
        while (counter < (openWindowNames.size())) {        //populate window menu
            //have to make the radiobuttons look like checkboxes; this is due to a mac bug
            Object radioIcon = UIManager.get("RadioButtonMenuItem.checkIcon");
            UIManager.put("RadioButtonMenuItem.checkIcon", UIManager.get("CheckBoxMenuItem.checkIcon"));
            temp = new WindowMenuItem((String)openWindowNames.get(counter), (BasicWindow)openWindows.get(counter));
            UIManager.put("RadioButtonMenuItem.checkIcon", radioIcon);
            this.add(temp);
            // menubar-less windows will throw null pointer exception here
            try {
                if (((BasicWindow)openWindows.get(counter)).equals((BasicWindow)this.getParent().getParent().getParent().getParent())) {
                   temp.setSelected(true);  //current window
                }
                else {
                    temp.setSelected(false);
                }
            } catch (Exception e) {}
            temp.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    windowMenuSelection_actionPerformed(e);
                }
            });
            temp.setFont(windowFont);
            counter = counter + 1;
        }
    }

    /**
     * User has chosen a specific item in the window menu. Now jump to it, using the WindowManager.
     *
     * @see ui.common.WindowManager
     */
    void windowMenuSelection_actionPerformed(ActionEvent e) {
        WindowMenuItem x = (WindowMenuItem)(e.getSource());
        // log
        ((BasicWindow)this.getTopLevelAncestor()).getUILogger().log(UIEventType.MENUITEM_SELECTED, e.getActionCommand());
        //don't actually toggle the checkbox here
        if (x.getWindowRef().equals((BasicWindow)this.getParent().getParent().getParent().getParent())) {
            x.setSelected(true);    //current window
        } else {
            x.setSelected(false);
        }
        WindowManager.toFront(x.getWindowRef());
    }
}
