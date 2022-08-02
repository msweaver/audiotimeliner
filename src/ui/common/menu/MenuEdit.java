package ui.common.menu;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.text.*;

import ui.common.UIUtilities;

/**
 * The Edit menu 
 */
public class MenuEdit extends JMenu {

	private static final long serialVersionUID = 1L;
	// MENU ITEMS
    public JMenuItem menuiEditCut = new JMenuItem();
    public JMenuItem menuiEditCopy = new JMenuItem();
    public JMenuItem menuiEditPaste = new JMenuItem();
    public JMenuItem menuiEditUndo = new JMenuItem();
    public JMenuItem menuiEditRedo = new JMenuItem();

    java.awt.Font editFont;

    public MenuEdit() {
        menuiEditCut.setText("Cut");
        menuiEditCopy.setText("Copy");
        menuiEditPaste.setText("Paste");
        menuiEditUndo.setText("Undo");
        menuiEditRedo.setText("Redo");
        this.setText("Edit");
        this.add(menuiEditCut);
        this.add(menuiEditCopy);
        this.add(menuiEditPaste);
        // don't add undo and redo at this point

        if (System.getProperty("os.name").startsWith("Mac OS")) {
            // Mac specific stuff
            editFont = UIUtilities.fontMenusMac;
            menuiEditCut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.META_DOWN_MASK));
            menuiEditCopy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.META_DOWN_MASK));
            menuiEditPaste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.META_DOWN_MASK));
            menuiEditUndo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.META_DOWN_MASK));
            menuiEditRedo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.META_DOWN_MASK+InputEvent.SHIFT_DOWN_MASK));
        } else {
            //Windows specific stuff
            editFont = UIUtilities.fontMenusWin;
            menuiEditCut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_DOWN_MASK));
            menuiEditCopy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK));
            menuiEditPaste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK));
            menuiEditUndo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK));
            menuiEditRedo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK+InputEvent.SHIFT_DOWN_MASK));
            this.setMnemonic('e');
            menuiEditCut.setMnemonic('t');
            menuiEditCopy.setMnemonic('c');
            menuiEditPaste.setMnemonic('p');
            menuiEditUndo.setMnemonic('u');
            menuiEditRedo.setMnemonic('r');
        }
        menuiEditCut.setFont(editFont);
        menuiEditCopy.setFont(editFont);
        menuiEditPaste.setFont(editFont);
        menuiEditUndo.setFont(editFont);
        menuiEditRedo.setFont(editFont);
        this.setFont(editFont);
        menuiEditCut.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                menuiEditCut_actionPerformed(e);
            }});
        menuiEditCopy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                menuiEditCopy_actionPerformed(e);
            }});
        menuiEditPaste.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                menuiEditPaste_actionPerformed(e);
            }});
    }


    /**
     * Cut
     */
    void menuiEditCut_actionPerformed(ActionEvent e) {
        javax.swing.SwingUtilities.invokeLater( new Runnable() {
            public void run() {
                Object x = ((Window)getTopLevelAncestor()).getFocusOwner();
                if (x instanceof JTextComponent) {
                    ((JTextComponent)x).cut();
                }
            }
        });
    }

    /**
     * Copy
     */
    void menuiEditCopy_actionPerformed(ActionEvent e) {
        javax.swing.SwingUtilities.invokeLater( new Runnable() {
            public void run() {
                Object x = ((Window)getTopLevelAncestor()).getFocusOwner();
                if (x instanceof JTextComponent) {
                    ((JTextComponent)x).copy();
                }
            }
        });
    }

    /**
      * Paste
      */
    void menuiEditPaste_actionPerformed(ActionEvent e) {
        javax.swing.SwingUtilities.invokeLater( new Runnable() {
            public void run() {
                Object x = ((Window)getTopLevelAncestor()).getFocusOwner();
                if (x instanceof JTextComponent) {
                    ((JTextComponent)x).paste();
                }
            }
        });
    }
}
