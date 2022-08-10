package ui.timeliner;

import javax.swing.*;
import javax.swing.undo.*;
//import java.awt.*;
import java.awt.event.*;
import util.logging.*;
//import org.apache.log4j.Logger;
import ui.common.*;

/**
 * Timeline Menu Bar
 * This creates the menu items that are specific to the timeline window. It borrows the rest from the
 * basicMenuBar
 *
 * @author Brent Yorgason
 */

public class TimelineMenuBar extends JMenuBar {

  private static final long serialVersionUID = 1L;
// references to external objects
  private TimelineFrame frmTimeline;
  private TimelinePanel pnlTimeline;
  //private TimelineControlPanel pnlControl;
  //private UndoManager undoManager;
 // private static Logger log = Logger.getLogger(TimelineMenuBar.class);
  protected UILogger uilogger;
  protected TimepointEditor dlgTimepointEditor;
  protected MarkerEditor dlgMarkerEditor;

  // menuitems added to file menu in basicMenuBar
  JMenuItem menuiSaveTimeline;
  JMenuItem menuiSaveTimelineAs;
  JMenuItem menuiSaveAsWebPage;
  JMenuItem menuiRevertToSaved;

  // timeline menu, submenus, and menu items
  JMenu menuTimeline;
  JMenu menuTimepoint;
  JMenu menuBubbles;
  JMenuItem menuiAdd;
  JMenuItem menuiAddMarker;
  JMenuItem menuiEditTimepointOrMarker;
  JMenuItem menuiDeleteTimepointOrMarker;
  JMenuItem menuiSetText;
  JMenuItem menuiChangeColor;
  JMenuItem menuiSetLevelColor;
  JMenuItem menuiGroup;
  JMenuItem menuiUngroup;
  JMenuItem menuiDeleteBubble;
  JMenuItem menuiMoveBubbleUp;
  JMenuItem menuiMoveBubbleDown;
  JMenuItem menuiEditProperties;
  JMenuItem menuiReset;
  JMenuItem menuiCreateExcerpt;
  JMenuItem menuiZoomTo;
  JMenuItem menuiFitTimelineToWindow;
  JMenuItem menuiViewDetails;
  JMenuItem menuiChangeBackgroundColor;
  JRadioButtonMenuItem menuiShowTimesMac;
  JRadioButtonMenuItem menuiShowMarkerTimesMac;
  JRadioButtonMenuItem menuiBlackAndWhiteMac;
  JRadioButtonMenuItem menuiRoundBubblesMac;
  JRadioButtonMenuItem menuiSquareBubblesMac;
  JCheckBoxMenuItem menuiShowTimes;
  JCheckBoxMenuItem menuiShowMarkerTimes;
  JCheckBoxMenuItem menuiBlackAndWhite;
  JCheckBoxMenuItem menuiRoundBubbles;
  JCheckBoxMenuItem menuiSquareBubbles;

  // to fix mac radio item menu bug
  String RADIO_ICON_KEY = "RadioButtonMenuItem.checkIcon";
  String CHECK_ICON_KEY = "CheckBoxMenuItem.checkIcon";

  // menu font
  private java.awt.Font timelineMenuFont;

  /**
   * constructor: creates the menu bar
   */
  public TimelineMenuBar(TimelineFrame tf) {

    frmTimeline = tf;
    pnlTimeline = tf.getTimelinePanel();
    //pnlControl = tf.getControlPanel();
    uilogger = frmTimeline.getUILogger();
    //undoManager = pnlTimeline.undoManager;
    if (System.getProperty("os.name").startsWith("Mac OS")) {
    	timelineMenuFont = UIUtilities.fontMenusMac;
    } else {
    	timelineMenuFont = UIUtilities.fontMenusWin;
    }
    // set up file and edit menus
    createFileMenu();
    createEditMenu();

    //disable and enable options in other menus
    frmTimeline.basicMenuBar.menuEdit.menuiEditCopy.setEnabled(false);
    frmTimeline.basicMenuBar.menuEdit.menuiEditCut.setEnabled(false);
    frmTimeline.basicMenuBar.menuEdit.menuiEditPaste.setEnabled(false);

    // create the timeline menu
    createTimelineMenu();

    if (System.getProperty("os.name").startsWith("Mac OS")) {
      frmTimeline.basicMenuBar.add(menuTimeline, 3);     //so the window menu won't move around on Mac (was 4)
    }
    else {
      frmTimeline.basicMenuBar.add(menuTimeline, 3);     //add player menu in the appropriate place
    }
  }

  /**
   * creates the file menu
   */
  private void createFileMenu() {
    // menu items
    menuiSaveTimeline = new JMenuItem("Save Timeline", 'S');
    menuiSaveTimelineAs = new JMenuItem("Save Timeline As...", 'V');
    menuiSaveAsWebPage = new JMenuItem("Save as Web Page...", 'G');
    menuiRevertToSaved = new JMenuItem("Revert to Saved", 'R');
    menuiSaveTimeline.setFont(timelineMenuFont);
    menuiSaveTimelineAs.setFont(timelineMenuFont);
    menuiSaveAsWebPage.setFont(timelineMenuFont);
    menuiRevertToSaved.setFont(timelineMenuFont);

    // set up accelerators and mnemonics
    if (System.getProperty("os.name").startsWith("Mac OS")) {
        //Mac specific stuff
        menuiSaveTimeline.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.META_DOWN_MASK));
        menuiSaveTimelineAs.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.META_DOWN_MASK));
        menuiSaveAsWebPage.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.META_DOWN_MASK));
    } else {
        //Windows specific stuff
        menuiSaveTimeline.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
        menuiSaveTimelineAs.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_DOWN_MASK));
        menuiSaveAsWebPage.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.CTRL_DOWN_MASK));
        menuiSaveTimeline.setMnemonic('s');
        menuiSaveTimelineAs.setMnemonic('a');
        menuiSaveAsWebPage.setMnemonic('g');
        menuiRevertToSaved.setMnemonic('r');
    }

    // add menu items to menu
    this.frmTimeline.basicMenuBar.menuFile.insertSeparator(3);
    this.frmTimeline.basicMenuBar.menuFile.insert(menuiSaveTimeline, 4);
    this.frmTimeline.basicMenuBar.menuFile.insert(menuiSaveTimelineAs, 5);
    this.frmTimeline.basicMenuBar.menuFile.insert(menuiSaveAsWebPage, 6);
    this.frmTimeline.basicMenuBar.menuFile.insert(menuiRevertToSaved, 7);
    this.frmTimeline.basicMenuBar.menuFile.insertSeparator(8);

    this.frmTimeline.basicMenuBar.menuEdit.insert(frmTimeline.basicMenuBar.menuEdit.menuiEditUndo, 0);
    this.frmTimeline.basicMenuBar.menuEdit.insert(frmTimeline.basicMenuBar.menuEdit.menuiEditRedo, 1);
    this.frmTimeline.basicMenuBar.menuEdit.insertSeparator(2);

    // menu item action listers
    menuiSaveTimeline.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        pnlTimeline.saveTimeline(false); // false indicates not "save as", use current filename
                uilogger.log(UIEventType.MENUITEM_SELECTED, "save timeline");
      }
    });
    menuiSaveTimelineAs.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        pnlTimeline.saveTimeline(true); // true indicates "save as", always open save as dialog
                uilogger.log(UIEventType.MENUITEM_SELECTED, "save timeline as");
      }
    });
    menuiSaveAsWebPage.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        pnlTimeline.saveTimelineAsWebPage();
        uilogger.log(UIEventType.MENUITEM_SELECTED, "save timeline as web page");
      }
    });
    menuiRevertToSaved.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        pnlTimeline.revertToSavedTimeline();
        uilogger.log(UIEventType.MENUITEM_SELECTED, "revert to saved timeline");
      }
    });
    this.frmTimeline.basicMenuBar.menuFile.menuiFilePrint.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        pnlTimeline.printTimelinePanel();
                uilogger.log(UIEventType.MENUITEM_SELECTED, "print timeline");
      }
    });
    this.frmTimeline.basicMenuBar.menuFile.menuiFileClose.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
    	  if (frmTimeline.tryClose()) {
    		  frmTimeline.doWindowClose();
    	  }
                uilogger.log(UIEventType.MENUITEM_SELECTED, "close");
      }
    });
  }

  /**
   * creates the edit menu
   */
  private void createEditMenu() {

    // menu item action listeners
    this.frmTimeline.basicMenuBar.menuEdit.menuiEditUndo.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (pnlTimeline.undoManager.canUndo()) {
          try {
            pnlTimeline.undoManager.undo();
          }
          catch (CannotUndoException cue) {
            cue.printStackTrace(); // todo: improve
          }
          pnlTimeline.updateUndoMenu();
          uilogger.log(UIEventType.MENUITEM_SELECTED, "undo");
        }
      }
    });
    this.frmTimeline.basicMenuBar.menuEdit.menuiEditRedo.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (pnlTimeline.undoManager.canRedo()) {
          try {
            pnlTimeline.undoManager.redo();
          }
          catch (CannotRedoException cre) {
            cre.printStackTrace(); // todo: improve
          }
          pnlTimeline.updateUndoMenu();
                  uilogger.log(UIEventType.MENUITEM_SELECTED, "redo");
        }
      }
    });
    this.frmTimeline.basicMenuBar.menuEdit.menuiEditCut.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
                  uilogger.log(UIEventType.MENUITEM_SELECTED, "cut");
      }
    });
    this.frmTimeline.basicMenuBar.menuEdit.menuiEditCopy.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
                  uilogger.log(UIEventType.MENUITEM_SELECTED, "copy");
      }
    });
    this.frmTimeline.basicMenuBar.menuEdit.menuiEditPaste.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
                  uilogger.log(UIEventType.MENUITEM_SELECTED, "paste");
      }
    });

    // add menu items to edit menu
    this.frmTimeline.basicMenuBar.menuEdit.menuiEditUndo.setEnabled(false);
    this.frmTimeline.basicMenuBar.menuEdit.menuiEditRedo.setEnabled(false);
  }

  /**
   * creates the timeline menu
   */
  private void createTimelineMenu() {

    menuTimeline = new JMenu("Timeline");
    menuTimeline.setFont(frmTimeline.basicMenuBar.menuFile.getFont());

    // assemble timepoint submenu
    menuTimepoint = new JMenu("Timepoint or Marker");
    menuTimepoint.setFont(timelineMenuFont);
    menuTimeline.add(menuTimepoint);
    menuiAdd = new JMenuItem("Add Timepoint");
    menuiAdd.setFont(timelineMenuFont);
    menuiAddMarker = new JMenuItem("Add Marker");
    menuiAddMarker.setFont(timelineMenuFont);
    menuiEditTimepointOrMarker = new JMenuItem("Edit...");
    menuiEditTimepointOrMarker.setFont(timelineMenuFont);
    menuiDeleteTimepointOrMarker = new JMenuItem("Delete");
    menuiDeleteTimepointOrMarker.setFont(timelineMenuFont);
    menuTimepoint.add(menuiAdd);
    menuTimepoint.add(menuiAddMarker);
    menuTimepoint.add(menuiEditTimepointOrMarker);
    menuTimepoint.add(menuiDeleteTimepointOrMarker);

    // assemble bubbles submenu
    menuBubbles = new JMenu("Bubble(s)");
    menuBubbles.setFont(timelineMenuFont);
    menuTimeline.add(menuBubbles);
    menuiSetText = new JMenuItem("Edit...");
    menuiSetText.setFont(timelineMenuFont);
    menuiChangeColor = new JMenuItem("Change Color...");
    menuiChangeColor.setFont(timelineMenuFont);
    menuiSetLevelColor = new JMenuItem("Set Level Color...");
    menuiSetLevelColor.setFont(timelineMenuFont);
    menuiGroup = new JMenuItem("Group");
    menuiGroup.setFont(timelineMenuFont);
    menuiUngroup = new JMenuItem("Ungroup");
    menuiUngroup.setFont(timelineMenuFont);
    menuiDeleteBubble = new JMenuItem("Delete");
    menuiMoveBubbleUp = new JMenuItem("Move Up a Level");
    menuiMoveBubbleDown = new JMenuItem("Move Down a Level");
    menuiMoveBubbleUp.setFont(timelineMenuFont);
    menuiMoveBubbleDown.setFont(timelineMenuFont);
    menuiDeleteBubble.setFont(timelineMenuFont);
    menuBubbles.add(menuiSetText);
    menuBubbles.add(menuiChangeColor);
    menuBubbles.add(menuiSetLevelColor);
    menuBubbles.add(menuiGroup);
    menuBubbles.add(menuiDeleteBubble);
    menuBubbles.add(menuiMoveBubbleUp);
    menuBubbles.add(menuiMoveBubbleDown);

    // assemble timeline menu
    menuiEditProperties = new JMenuItem("Edit Properties...");
    menuiEditProperties.setFont(timelineMenuFont);
    menuiReset = new JMenuItem("Clear All");
    menuiReset.setFont(timelineMenuFont);
    menuiCreateExcerpt = new JMenuItem("Create Excerpt");
    menuiCreateExcerpt.setFont(timelineMenuFont);
    menuiZoomTo = new JMenuItem("Zoom to Selection");
    menuiZoomTo.setFont(timelineMenuFont);
    menuiFitTimelineToWindow = new JMenuItem("Fit to Window");
    menuiFitTimelineToWindow.setFont(timelineMenuFont);
    menuiViewDetails = new JMenuItem("View Recording Details");
    menuiViewDetails.setFont(timelineMenuFont);
    menuiChangeBackgroundColor = new JMenuItem("Change Background Color");
    menuiChangeBackgroundColor.setFont(timelineMenuFont);

    // some mac and pc items need to be handled separately
    if (System.getProperty("os.name").startsWith("Mac OS")) {
      Object radioIcon = UIManager.get(RADIO_ICON_KEY);
      UIManager.put(RADIO_ICON_KEY, UIManager.get(CHECK_ICON_KEY));
      menuiShowTimesMac = new JRadioButtonMenuItem("Show Timepoint Times", false);
      menuiShowMarkerTimesMac = new JRadioButtonMenuItem("Show Marker Times", false);
      menuiBlackAndWhiteMac = new JRadioButtonMenuItem("Black and White", false);
      menuiRoundBubblesMac = new JRadioButtonMenuItem("Round Bubbles", true);
      menuiSquareBubblesMac = new JRadioButtonMenuItem("Square Bubbles", false);
      UIManager.put(RADIO_ICON_KEY, radioIcon);
      menuiShowTimesMac.setFont(timelineMenuFont);
      menuiShowMarkerTimesMac.setFont(timelineMenuFont);
      menuiBlackAndWhiteMac.setFont(timelineMenuFont);
      menuiRoundBubblesMac.setFont(timelineMenuFont);
      menuiSquareBubblesMac.setFont(timelineMenuFont);
    }
    else {
      menuiShowTimes = new JCheckBoxMenuItem("Show Timepoint Times", false);
      menuiShowMarkerTimes = new JCheckBoxMenuItem("Show Marker Times", false);
      menuiBlackAndWhite = new JCheckBoxMenuItem("Black and White", false);
      menuiRoundBubbles = new JCheckBoxMenuItem("Round Bubbles", true);
      menuiSquareBubbles = new JCheckBoxMenuItem("Square Bubbles", false);
      menuiShowTimes.setFont(timelineMenuFont);
      menuiShowMarkerTimes.setFont(timelineMenuFont);
      menuiBlackAndWhite.setFont(timelineMenuFont);
      menuiRoundBubbles.setFont(timelineMenuFont);
      menuiSquareBubbles.setFont(timelineMenuFont);
    }

    // add menu items and submenus to timeline menu
    menuTimeline.add(menuiEditProperties);
    menuTimeline.add(menuiReset);
    menuTimeline.add(menuiCreateExcerpt);
    if (!frmTimeline.isStandaloneVersion) {
      menuTimeline.add(menuiViewDetails);
    }
    menuTimeline.add(menuiChangeBackgroundColor);
    menuTimeline.addSeparator();
    menuTimeline.add(menuiZoomTo);
    menuTimeline.add(menuiFitTimelineToWindow);
    menuTimeline.addSeparator();

    // mac radio items need to be handled separately
    if (System.getProperty("os.name").startsWith("Mac OS")) {
      menuTimeline.add(menuiShowTimesMac);
      menuTimeline.add(menuiShowMarkerTimesMac);
      menuTimeline.add(menuiBlackAndWhiteMac);
      menuTimeline.add(menuiRoundBubblesMac);
      menuTimeline.add(menuiSquareBubblesMac);
    }
    else {
      menuTimeline.add(menuiShowTimes);
      menuTimeline.add(menuiShowMarkerTimes);
      menuTimeline.add(menuiBlackAndWhite);
      menuTimeline.add(menuiRoundBubbles);
      menuTimeline.add(menuiSquareBubbles);
    }

    // set up accelerations and mnemonics
    if (System.getProperty("os.name").startsWith("Mac OS")) {
      menuiAdd.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
      menuiAddMarker.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.META_DOWN_MASK));
      menuiDeleteTimepointOrMarker.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
      menuiDeleteBubble.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
      menuiMoveBubbleUp.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.META_DOWN_MASK));
      menuiMoveBubbleDown.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.META_DOWN_MASK));
    }
    else {
      menuiAdd.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
      menuiAddMarker.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.CTRL_DOWN_MASK));
      menuiDeleteTimepointOrMarker.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
      menuiDeleteBubble.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
      menuiMoveBubbleUp.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.CTRL_DOWN_MASK));
      menuiMoveBubbleDown.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.CTRL_DOWN_MASK));
      menuiAdd.setMnemonic('a');
      menuiAddMarker.setMnemonic('m');
      menuiEditTimepointOrMarker.setMnemonic('e');
      menuiDeleteTimepointOrMarker.setMnemonic('d');
      menuiDeleteBubble.setMnemonic('d');
      menuiMoveBubbleUp.setMnemonic('u');
      menuiMoveBubbleDown.setMnemonic('o');
      menuiSetText.setMnemonic('e');
      menuiChangeColor.setMnemonic('c');
      menuiSetLevelColor.setMnemonic('s');
      menuiGroup.setMnemonic('g');
      menuiUngroup.setMnemonic('u');
      menuiEditProperties.setMnemonic('e');
      menuiReset.setMnemonic('c');
      menuiCreateExcerpt.setMnemonic('x');
      menuiZoomTo.setMnemonic('z');
      menuiFitTimelineToWindow.setMnemonic('f');
      menuiViewDetails.setMnemonic('v');
      menuiChangeBackgroundColor.setMnemonic('b');
      menuiShowTimes.setMnemonic('h');
      menuiShowMarkerTimes.setMnemonic('m');
      menuiBlackAndWhite.setMnemonic('w');
      menuiRoundBubbles.setMnemonic('r');
      menuiSquareBubbles.setMnemonic('q');
      menuTimeline.setMnemonic('t');
      menuTimepoint.setMnemonic('t');
      menuBubbles.setMnemonic('b');
    }

    // menu item action listeners
    menuiAdd.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (menuiAdd.isEnabled() && pnlTimeline.enterKeyAdd) {
          pnlTimeline.addTimepoint();
                  uilogger.log(UIEventType.MENUITEM_SELECTED, "add timepoint");
        }
      }
    });
    menuiAddMarker.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (menuiAddMarker.isEnabled()) {
          pnlTimeline.addMarker();
                  uilogger.log(UIEventType.MENUITEM_SELECTED, "add marker");
        }
      }
    });
    menuiEditTimepointOrMarker.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (pnlTimeline.getTimeline().areTimepointsSelected()) {
          dlgTimepointEditor = new TimepointEditor(frmTimeline);
          uilogger.log(UIEventType.MENUITEM_SELECTED, "edit timepoint label" + pnlTimeline.getTimeline().getLastTimepointClicked());
        }
        else {
          dlgMarkerEditor = new MarkerEditor(frmTimeline);
          uilogger.log(UIEventType.MENUITEM_SELECTED, "edit marker" + pnlTimeline.getTimeline().getLastMarkerClicked());
        }
      }
    });
    menuiDeleteTimepointOrMarker.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (pnlTimeline.getTimeline().areTimepointsSelected()) {
          pnlTimeline.deleteSelectedTimepoint();
          uilogger.log(UIEventType.MENUITEM_SELECTED, "delete timepoint" + pnlTimeline.getTimeline().getLastTimepointClicked());
        }
        else if (pnlTimeline.getTimeline().areMarkersSelected()) {
          pnlTimeline.deleteSelectedMarker();
          uilogger.log(UIEventType.MENUITEM_SELECTED, "delete marker" + pnlTimeline.getTimeline().getLastMarkerClicked());
        }
        else {// this is done so that the delete key will work with bubbles and timepoints / markers
          pnlTimeline.deleteSelectedBubbles();
        }
      }
    });
    menuiSetText.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        pnlTimeline.dlgBubbleEditor = new TimelineBubbleEditor(frmTimeline);
                uilogger.log(UIEventType.MENUITEM_SELECTED, "edit bubble: " + pnlTimeline.getTimeline().getSelectedBubbles());
          }
    });
    menuiChangeColor.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        pnlTimeline.changeBubbleColor();
                uilogger.log(UIEventType.MENUITEM_SELECTED, "change color of selected bubbles: "
                        + pnlTimeline.getTimeline().getSelectedBubbles());
      }
    });
    menuiSetLevelColor.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        pnlTimeline.setLevelColor();
                uilogger.log(UIEventType.MENUITEM_SELECTED, "set selected bubble level color: "
                        + pnlTimeline.getTimeline().getSelectedLevels());
      }
    });
    menuiGroup.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        pnlTimeline.groupSelectedBubbles();
                uilogger.log(UIEventType.MENUITEM_SELECTED, "group selected bubbles: "
                        + pnlTimeline.getTimeline().getSelectedBubbles());
      }
    });
    menuiUngroup.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        pnlTimeline.ungroupSelectedBubbles();
                uilogger.log(UIEventType.MENUITEM_SELECTED, "ungroup selected bubbles: "
                        + pnlTimeline.getTimeline().getSelectedBubbles());
      }
    });
    menuiDeleteBubble.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        pnlTimeline.deleteSelectedBubbles();
                uilogger.log(UIEventType.MENUITEM_SELECTED, "delete selected bubbles: "
                        + pnlTimeline.getTimeline().getSelectedBubbles());
      }
    });
    menuiMoveBubbleUp.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        pnlTimeline.moveSelectedBubblesUp();
        uilogger.log(UIEventType.MENUITEM_SELECTED, "move bubble up a level");
      }
    });
    menuiMoveBubbleDown.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        pnlTimeline.moveSelectedBubblesDown();
        uilogger.log(UIEventType.MENUITEM_SELECTED, "move bubble down a level");
      }
    });
    menuiEditProperties.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        pnlTimeline.openTimelineProperties();
                uilogger.log(UIEventType.MENUITEM_SELECTED, "edit timeline properties");
      }
    });
    menuiReset.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        pnlTimeline.resetTimeline();
                uilogger.log(UIEventType.MENUITEM_SELECTED, "reset timeline");
      }
    });
    menuiCreateExcerpt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        pnlTimeline.createExcerpt();
                uilogger.log(UIEventType.MENUITEM_SELECTED, "create excerpt: "
                        + pnlTimeline.getTimeline().getSelectedBubbles());
      }
    });
    menuiZoomTo.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        pnlTimeline.zoomToSelectedBubbles();
                uilogger.log(UIEventType.MENUITEM_SELECTED, "zoom to selection: "
                        + pnlTimeline.getTimeline().getSelectedBubbles());
      }
    });
    menuiFitTimelineToWindow.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        pnlTimeline.fitToWindow();
        uilogger.log(UIEventType.MENUITEM_SELECTED, "fit timeline to window");
      }
    });
    menuiViewDetails.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        uilogger.log(UIEventType.MENUITEM_SELECTED, "view recording details");
        if (!frmTimeline.isUsingLocalAudio) {
          pnlTimeline.getPlayer().openDetails();
        }
      }
    });
    menuiChangeBackgroundColor.addActionListener(new ActionListener() {
      public void actionPerformed (ActionEvent e) {
        pnlTimeline.setBackgroundColor();
        uilogger.log(UIEventType.POPUPMENUITEM_SELECTED, "change background color");
      }
    });

    if (System.getProperty("os.name").startsWith("Mac OS")) {
      menuiShowTimesMac.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          boolean show = ((JRadioButtonMenuItem)e.getSource()).isSelected();
          pnlTimeline.showTimes(show);
          pnlTimeline.menuiShowTimesMac.setSelected(show);
          pnlTimeline.getFrame().getControlPanel().setShowTimes(show);
          pnlTimeline.refreshTimeline();
                     uilogger.log(UIEventType.MENUITEM_SELECTED, "show timepoint times" + menuiShowTimesMac.isSelected());
        }
      });
      menuiShowMarkerTimesMac.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            boolean show = ((JRadioButtonMenuItem)e.getSource()).isSelected();
            pnlTimeline.showMarkerTimes(show);
            pnlTimeline.menuiShowMarkerTimesMac.setSelected(show);
            pnlTimeline.getFrame().getControlPanel().setShowMarkerTimes(show);
            pnlTimeline.refreshTimeline();
                       uilogger.log(UIEventType.MENUITEM_SELECTED, "show marker times" + menuiShowMarkerTimesMac.isSelected());
          }
        });

      menuiBlackAndWhiteMac.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          boolean state = ((JRadioButtonMenuItem)e.getSource()).isSelected();
          pnlTimeline.setBlackAndWhite(state);
                    uilogger.log(UIEventType.MENUITEM_SELECTED, "black and white"  + menuiBlackAndWhiteMac.isSelected());
        }
      });
      menuiRoundBubblesMac.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          menuiRoundBubblesMac.setSelected(true);
          menuiSquareBubblesMac.setSelected(false);
          pnlTimeline.setBubbleType(0);
                    uilogger.log(UIEventType.MENUITEM_SELECTED, "round bubbles");
        }
      });
      menuiSquareBubblesMac.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          menuiRoundBubblesMac.setSelected(false);
          menuiSquareBubblesMac.setSelected(true);
          pnlTimeline.setBubbleType(1);
                    uilogger.log(UIEventType.MENUITEM_SELECTED, "square bubbles");
        }
      });

    }
    else {
      menuiShowTimes.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          boolean show = ((JCheckBoxMenuItem)e.getSource()).isSelected();
          pnlTimeline.showTimes(show);
          pnlTimeline.menuiShowTimes.setState(show);
          pnlTimeline.getFrame().getControlPanel().setShowTimes(show);
          pnlTimeline.refreshTimeline();
                     uilogger.log(UIEventType.MENUITEM_SELECTED, "show timepoint times" + menuiShowTimes.isSelected());
        }
      });
      menuiShowMarkerTimes.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            boolean show = ((JCheckBoxMenuItem)e.getSource()).isSelected();
            pnlTimeline.showMarkerTimes(show);
            pnlTimeline.menuiShowMarkerTimes.setState(show);
            pnlTimeline.getFrame().getControlPanel().setShowMarkerTimes(show);
            pnlTimeline.refreshTimeline();
                       uilogger.log(UIEventType.MENUITEM_SELECTED, "show marker times" + menuiShowMarkerTimes.isSelected());
          }
        });

      menuiBlackAndWhite.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            boolean state = ((JCheckBoxMenuItem)e.getSource()).isSelected();
          pnlTimeline.setBlackAndWhite(state);
                    uilogger.log(UIEventType.MENUITEM_SELECTED, "black and white"  + menuiBlackAndWhite.isSelected());
        }
      });
      menuiRoundBubbles.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          menuiRoundBubbles.setSelected(true);
          menuiSquareBubbles.setSelected(false);
          pnlTimeline.setBubbleType(0);
                    uilogger.log(UIEventType.MENUITEM_SELECTED, "round bubbles");
        }
      });
      menuiSquareBubbles.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          menuiRoundBubbles.setSelected(false);
          menuiSquareBubbles.setSelected(true);
          pnlTimeline.setBubbleType(1);
                    uilogger.log(UIEventType.MENUITEM_SELECTED, "square bubbles");
        }
      });
     }
  }

  /**
   * disables menu options for selected timepoints
   */
  protected void disableSelectedTimepointOptions() {
    menuiDeleteTimepointOrMarker.setEnabled(false);
    menuiEditTimepointOrMarker.setEnabled(false);
  }

  /**
   * enables menu options for selected timepoints
   */
  protected void enableSelectedTimepointOptions() {
    menuiDeleteTimepointOrMarker.setEnabled(true);
    menuiEditTimepointOrMarker.setEnabled(true);
  }

  /**
   * disables menu options for adding timepoints
   */
  protected void disableAddTimepoint() {
    menuiAdd.setEnabled(false);
  }

  /**
   * enables menu options for adding timepoints
   */
  protected void enableAddTimepoint() {
    menuiAdd.setEnabled(true);
  }

  /**
   * disables menu options for adding markers
   */
  protected void disableAddMarker() {
    menuiAddMarker.setEnabled(false);
  }

  /**
   * enables menu options for adding markers
   */
  protected void enableAddMarker() {
    menuiAddMarker.setEnabled(true);
  }

  /**
   * disables menu options for bubbles
   */
  protected void disableAllBubbleOptions() {
    menuBubbles.setEnabled(false);
    menuiSetText.setEnabled(false);
    menuiChangeColor.setEnabled(false);
    menuiDeleteBubble.setEnabled(false);
    menuiMoveBubbleUp.setEnabled(false);
    menuiMoveBubbleDown.setEnabled(false);
    menuiCreateExcerpt.setEnabled(false);
    menuiZoomTo.setEnabled(false);
    menuiGroup.setEnabled(false);
    menuiUngroup.setEnabled(false);
  }

  /**
   * enables menu options for selected bubbles
   */
  protected void enableSingleSelectedBubbleOptions() {
    menuBubbles.setEnabled(true);
    menuiSetText.setEnabled(true);
    menuiChangeColor.setEnabled(true);
    menuiDeleteBubble.setEnabled(true);
    menuiMoveBubbleUp.setEnabled(true);
    menuiMoveBubbleDown.setEnabled(true);
    menuiCreateExcerpt.setEnabled(true);
    menuiZoomTo.setEnabled(true);
  }

  /**
   * enables menu options for selected grouped bubbles
   */
  protected void enableMultipleSelectedGroupedBubbleOptions() {
    menuBubbles.setEnabled(true);
    menuiSetText.setEnabled(false);
    menuiChangeColor.setEnabled(true);
    menuiDeleteBubble.setEnabled(true);
    menuiMoveBubbleUp.setEnabled(true);
    menuiMoveBubbleDown.setEnabled(true);
    menuiCreateExcerpt.setEnabled(true);
    menuiZoomTo.setEnabled(true);
    menuiUngroup.setEnabled(true);
  }

  /**
   * enables menu options for selected ungrouped bubbles
   */
  protected void enableMultipleSelectedUngroupedBubbleOptions() {
    menuBubbles.setEnabled(true);
    menuiSetText.setEnabled(false);
    menuiChangeColor.setEnabled(true);
    menuiDeleteBubble.setEnabled(true);
    menuiMoveBubbleUp.setEnabled(true);
    menuiMoveBubbleDown.setEnabled(true);
    menuiCreateExcerpt.setEnabled(true);
    menuiZoomTo.setEnabled(true);
    menuiGroup.setEnabled(true);
  }

  /**
   * disables menu keyboard shortcuts
   * this is for a mac bug that prevents certain keys and keyboard shortcuts from working in text fields
   */
  protected void disableMenuKeyboardShortcuts () {
    this.menuiDeleteBubble.setAccelerator(null);
    this.menuiDeleteTimepointOrMarker.setAccelerator(null);
    frmTimeline.basicMenuBar.menuEdit.menuiEditCopy.setAccelerator(null);
    frmTimeline.basicMenuBar.menuEdit.menuiEditCut.setAccelerator(null);
    frmTimeline.basicMenuBar.menuEdit.menuiEditPaste.setAccelerator(null);
    frmTimeline.basicMenuBar.menuEdit.menuiEditUndo.setAccelerator(null);
    frmTimeline.basicMenuBar.menuEdit.menuiEditRedo.setAccelerator(null);
  }

  /**
   * enables menu keyboard shortcuts
   * this is for a mac bug that prevents certain keys and keyboard shortcuts from working in text fields
   */
  protected void enableMenuKeyboardShortcuts() {
    this.menuiDeleteBubble.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
    this.menuiDeleteTimepointOrMarker.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
    if (System.getProperty("os.name").startsWith("Mac OS")) {
        frmTimeline.basicMenuBar.menuEdit.menuiEditCut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.META_DOWN_MASK));
        frmTimeline.basicMenuBar.menuEdit.menuiEditCopy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.META_DOWN_MASK));
        frmTimeline.basicMenuBar.menuEdit.menuiEditPaste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.META_DOWN_MASK));
        frmTimeline.basicMenuBar.menuEdit.menuiEditUndo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.META_DOWN_MASK));
        frmTimeline.basicMenuBar.menuEdit.menuiEditRedo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.META_DOWN_MASK+InputEvent.SHIFT_DOWN_MASK));
    } else {
        frmTimeline.basicMenuBar.menuEdit.menuiEditCut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_DOWN_MASK));
        frmTimeline.basicMenuBar.menuEdit.menuiEditCopy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK));
        frmTimeline.basicMenuBar.menuEdit.menuiEditPaste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK));
        frmTimeline.basicMenuBar.menuEdit.menuiEditUndo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK));
        frmTimeline.basicMenuBar.menuEdit.menuiEditRedo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK+InputEvent.SHIFT_DOWN_MASK));
    }
  }

  /**
   * setSaveEnabled: enables or disables the save menu options
   */
  protected void setSaveEnabled(boolean state) {
    menuiSaveAsWebPage.setEnabled(state);
    menuiSaveTimeline.setEnabled(state);
    menuiSaveTimelineAs.setEnabled(state);
    menuiRevertToSaved.setEnabled(state);
  }

  /**
   * setPrintEnabled: enables or disables the print menu option
   */
  protected void setPrintEnabled(boolean state) {
    this.frmTimeline.basicMenuBar.menuFile.menuiFilePrint.setEnabled(state);
  }

  /**
   * setTimelineMenuEnabled: enables or disables the timeline menu
   */
  protected void setTimelineMenuEnabled (boolean state) {
    menuTimeline.setEnabled(state);
  }
}