package ui.common.menu;

import javax.swing.*;
import ui.common.BasicWindow;

/**
 * This class represents a menu item in the window menu. Extends JRadioButtonMenuItem so that we can add a
 * reference to the window ID for the window that this item represents. NOTE: this should extend JCheckBoxMenuItem
 * instead, but a current Mac bug prevents this.
 *
 * @author Jim Halliday
 */
public class WindowMenuItem extends JRadioButtonMenuItem {

	private static final long serialVersionUID = 1L;
	BasicWindow windowref;
    public WindowMenuItem(String text, BasicWindow ref) {
        super(text);
        windowref = ref;
    }
    public BasicWindow getWindowRef() {
        return windowref;
    }
}

