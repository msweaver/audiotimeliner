package ui.common.menu;

import javax.swing.*;
import java.awt.event.*;

import util.logging.*;
import ui.common.*;

/**
 * The Help menu 
 */
public class MenuHelp extends JMenu {

	private static final long serialVersionUID = 1L;
	// MENU ITEMS
    public JMenuItem menuiHelpAbout= new JMenuItem();
    public JMenuItem menuiHelpOpenHelp = new JMenuItem();

    java.awt.Font helpFont;
    protected UILogger uilogger;

    public MenuHelp() {
        menuiHelpAbout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                menuiHelpAbout_actionPerformed(e);
            }
        });
        this.setText("Help");
        menuiHelpAbout.setText("About Audio Timeliner");
        if (System.getProperty("os.name").startsWith("Mac OS")) {
            //Mac specific stuff
            helpFont = UIUtilities.fontMenusMac;
            this.add(menuiHelpAbout);
            menuiHelpAbout.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, InputEvent.META_DOWN_MASK));
        } else {
            //Windows specific stuff
            helpFont = UIUtilities.fontMenusWin;
            this.add(menuiHelpAbout);
            this.setMnemonic('h');
            menuiHelpAbout.setMnemonic('a');
        }
        this.setFont(helpFont);
        menuiHelpAbout.setFont(helpFont);
//        menuiHelpOpenHelp.setFont(helpFont);
    }

    void menuiHelpAbout_actionPerformed(ActionEvent e) {
        try {
          JOptionPane.showMessageDialog(this.getParent().getParent(), new Object[] {"Audio Timeliner" + "\n" +
                                             "Version 3.0" + "\n" +
                                             "Copyright 2022" + "\n" + "Brent Yorgason" + "\n" + "Brigham Young University"},
                                             "About Audio Timeliner", JOptionPane.INFORMATION_MESSAGE, UIUtilities.icoTimeliner);
        } catch (Exception except) {
            JOptionPane.showMessageDialog(this.getParent().getParent(),
                    "Unable to open about box -- possible version conflict.\n" + except,
                    "About box loading error", JOptionPane.ERROR_MESSAGE);
        }
    }

}
