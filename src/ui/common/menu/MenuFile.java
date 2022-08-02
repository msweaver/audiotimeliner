package ui.common.menu;

import javax.swing.*;

import java.awt.Font;
import java.awt.event.*;
import ui.common.*;
import ui.timeliner.*;
import util.logging.*;

/**
 * The File menu 
 */
public class MenuFile extends JMenu {

    ////MENU ITEMS
    public JMenuItem menuiFileNewTimeline = new JMenuItem();
    public JMenuItem menuiFileOpenTimeline = new JMenuItem();
    public JMenuItem menuiFilePrint = new JMenuItem();
    public JMenuItem menuiFileClose = new JMenuItem();
    public JMenuItem menuiFileExit = new JMenuItem();
    
    //DEFAULT FONT
    static java.awt.Font fileFont;
    
    static final long serialVersionUID = 0;
   
    BasicWindow parentWindow;

    public MenuFile(BasicWindow parent) {
        parentWindow = parent;
        menuiFileExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                menuiFileExit_actionPerformed(e);
            }
        });
        menuiFileOpenTimeline.addActionListener(new java.awt.event.ActionListener()  {
          public void actionPerformed(ActionEvent e) {
              menuiFileOpenTimeline_actionPerformed(e);
          }
        });
        this.menuiFileNewTimeline.addActionListener(new java.awt.event.ActionListener()  {
          public void actionPerformed(ActionEvent e) {
              menuiFileNewTimeline_actionPerformed(e);
          }
        });
        menuiFileNewTimeline.setText("New Timeline...");
        menuiFileOpenTimeline.setText("Open Timeline...");
        menuiFilePrint.setText("Print...");
        menuiFileClose.setText("Close");
        menuiFileExit.setText("Exit");
        this.setText("File");
        this.add(menuiFileNewTimeline);
        this.add(menuiFileOpenTimeline);
        this.add(menuiFilePrint);
        this.add(menuiFileClose);
        if (System.getProperty("os.name").startsWith("Mac OS")) {
            //Mac specific stuff
            fileFont = UIUtilities.fontMenusMac;
            menuiFileNewTimeline.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.META_DOWN_MASK));
            menuiFileOpenTimeline.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.META_DOWN_MASK));
            menuiFilePrint.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.META_DOWN_MASK));
            menuiFileClose.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.META_DOWN_MASK));
        } else {
            //Windows specific stuff
            fileFont = new Font("Dialog", 0, UIUtilities.convertFontSize(12)); // UIUtilities.fontMenusWin;
            menuiFileNewTimeline.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
            menuiFileOpenTimeline.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
            menuiFilePrint.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK));
            menuiFileClose.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4,InputEvent.ALT_DOWN_MASK));
            menuiFileExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_DOWN_MASK));
            this.setMnemonic('f');
            menuiFileNewTimeline.setMnemonic('t');
            menuiFileOpenTimeline.setMnemonic('o');
            menuiFilePrint.setMnemonic('p');
            menuiFileClose.setMnemonic('c');
            menuiFileExit.setMnemonic('x');
            
            menuiFileClose.setFont(fileFont);
            menuiFileNewTimeline.setFont(fileFont);
            menuiFileOpenTimeline.setFont(fileFont);
            menuiFileExit.setFont(fileFont);
            menuiFilePrint.setFont(fileFont);
            this.setFont(fileFont);
            //Exit only present on Win
            this.add(menuiFileExit);
        }
        //Some options disabled by default
        menuiFilePrint.setEnabled(false);
    }

    void menuiFileExit_actionPerformed(ActionEvent e) {
      ((BasicWindow) this.getParent().getParent().getParent().getParent()).getUILogger().log(UIEventType.MENUITEM_SELECTED, "exit");
      WindowManager.doShutDown();         //attempt to shutdown the app
    }

    void menuiFileOpenTimeline_actionPerformed(ActionEvent e) {
        TimelineUtilities.openTimeline(parentWindow);   //open a timeline from a file
    }

    void menuiFileNewTimeline_actionPerformed(ActionEvent e) {
      TimelineUtilities.openAudioFile((TimelineFrame)parentWindow);
    }
}
