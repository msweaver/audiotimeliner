package ui.common.menu;

import javax.swing.*;
import ui.common.BasicWindow;

/**
 * Creates a JMenuBar and populates it with the Timeliner menus
 *
 * @author Brent Yorgason
 */
public class BasicMenuBar extends JMenuBar {

	private static final long serialVersionUID = 1L;
	public MenuFile menuFile;
    public MenuEdit menuEdit = new MenuEdit();
    public MenuWindow menuWindow = new MenuWindow();
    public MenuHelp menuHelp = new MenuHelp();

    public BasicMenuBar(BasicWindow parent) {
        menuFile = new MenuFile(parent);
        this.add(menuFile);
        this.add(menuEdit);
        this.add(menuWindow);
        this.add(menuHelp);
    }
}