package ui.common.menu;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import ui.common.UIUtilities;
import util.logging.UILogger;

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
    ImageIcon icoTimeliner = new ImageIcon(getClass().getClassLoader().getResource("resources/common/timeliner.gif"));

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
                                             "Version 3.0.0" + "\n" +
                                             "Copyright 2022" + "\n" + "Brent Yorgason" + "\n" + "Brigham Young University"},
                                             "About Audio Timeliner", JOptionPane.INFORMATION_MESSAGE, icoTimeliner);
        } catch (Exception except) {
            JOptionPane.showMessageDialog(this.getParent().getParent(),
                    "Unable to open about box -- possible version conflict.\n" + except,
                    "About box loading error", JOptionPane.ERROR_MESSAGE);
        }
    }

}
