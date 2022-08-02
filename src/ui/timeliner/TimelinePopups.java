package ui.timeliner;

import javax.swing.*;
import java.awt.event.*;
import util.logging.*;
//import org.apache.log4j.Logger;

/**
 * TimelinePopups: code for the popup menus in the timeline window
 */

public class TimelinePopups {

  // menus
  public JPopupMenu bubblePopup = new JPopupMenu();
  public JPopupMenu groupBubblePopup = new JPopupMenu();
  public JPopupMenu ungroupBubblePopup = new JPopupMenu();
  public JPopupMenu timepointPopup = new JPopupMenu();
  public JPopupMenu markerPopup = new JPopupMenu();
  public JPopupMenu timelinePopup = new JPopupMenu();
  public JPopupMenu panelPopup = new JPopupMenu();

  // menu items
  public JMenuItem menuiMoveBubbleUp;
  public JMenuItem menuiMoveBubbleDown;
  public JMenuItem menuiMoveBubbleUp2;
  public JMenuItem menuiMoveBubbleDown2;
  public JMenuItem menuiMoveBubbleUp3;
  public JMenuItem menuiMoveBubbleDown3;

  // external components
  //private Logger log = Logger.getLogger(TimelinePopups.class);
  protected UILogger uilogger;
  protected TimelineControlPanel pnlControl;
  protected TimelinePanel pnlTimeline;
  protected TimelineFrame frmTimeline;
  protected MarkerEditor dlgMarkerEditor;
  protected TimepointEditor dlgTimepointEditor;

  // for mac radio item menu bug
  String RADIO_ICON_KEY = "RadioButtonMenuItem.checkIcon";
  String CHECK_ICON_KEY = "CheckBoxMenuItem.checkIcon";

  /**
   * constructor
   */
  public TimelinePopups(TimelineFrame frame) {
    frmTimeline = frame;
    uilogger = frmTimeline.getUILogger();
    pnlControl = frmTimeline.getControlPanel();
    pnlTimeline = frmTimeline.getTimelinePanel();
    JMenuItem item;

    // move up and down menu items
    menuiMoveBubbleUp = new JMenuItem("Move Up a Level");
    menuiMoveBubbleUp.addActionListener(new ActionListener() {
      public void actionPerformed (ActionEvent e) {
         pnlTimeline.moveSelectedBubblesUp();
         uilogger.log(UIEventType.POPUPMENUITEM_SELECTED, "move bubble up a level"
                       + pnlTimeline.getTimeline().getSelectedBubbles());
      }
    });
    menuiMoveBubbleUp2 = new JMenuItem("Move Up a Level");
    menuiMoveBubbleUp2.addActionListener(new ActionListener() {
      public void actionPerformed (ActionEvent e) {
        pnlTimeline.moveSelectedBubblesUp();
        uilogger.log(UIEventType.POPUPMENUITEM_SELECTED, "move bubble up a level"
                     + pnlTimeline.getTimeline().getSelectedBubbles());
      }
    });
    menuiMoveBubbleUp3 = new JMenuItem("Move Up a Level");
    menuiMoveBubbleUp3.addActionListener(new ActionListener() {
      public void actionPerformed (ActionEvent e) {
        pnlTimeline.moveSelectedBubblesUp();
        uilogger.log(UIEventType.POPUPMENUITEM_SELECTED, "move bubble up a level"
                     + pnlTimeline.getTimeline().getSelectedBubbles());
      }
    });
    menuiMoveBubbleDown = new JMenuItem("Move Down a Level");
    menuiMoveBubbleDown.addActionListener(new ActionListener() {
      public void actionPerformed (ActionEvent e) {
        pnlTimeline.moveSelectedBubblesDown();
        uilogger.log(UIEventType.POPUPMENUITEM_SELECTED, "move bubble down a level"
                     + pnlTimeline.getTimeline().getSelectedBubbles());
      }
    });
    menuiMoveBubbleDown2 = new JMenuItem("Move Down a Level");
    menuiMoveBubbleDown2.addActionListener(new ActionListener() {
      public void actionPerformed (ActionEvent e) {
        pnlTimeline.moveSelectedBubblesDown();
        uilogger.log(UIEventType.POPUPMENUITEM_SELECTED, "move bubble down a level"
                     + pnlTimeline.getTimeline().getSelectedBubbles());
      }
    });
    menuiMoveBubbleDown3 = new JMenuItem("Move Down a Level");
    menuiMoveBubbleDown3.addActionListener(new ActionListener() {
      public void actionPerformed (ActionEvent e) {
        pnlTimeline.moveSelectedBubblesDown();
        uilogger.log(UIEventType.POPUPMENUITEM_SELECTED, "move bubble down a level"
                     + pnlTimeline.getTimeline().getSelectedBubbles());
      }
    });

    // popup menu for single bubbles
    bubblePopup.add(item = new JMenuItem("Play"));
    item.addActionListener(new ActionListener() {
      public void actionPerformed (ActionEvent e) {
        pnlControl.btn_playAction();
        uilogger.log(UIEventType.POPUPMENUITEM_SELECTED, "play from bubble: "
                     + pnlTimeline.getTimeline().getSelectedBubbles().elementAt(0));
      }
    });
    bubblePopup.add(item = new JMenuItem("Change Color"));
    item.addActionListener(new ActionListener() {
      public void actionPerformed (ActionEvent e) {
        pnlTimeline.changeBubbleColor();
        uilogger.log(UIEventType.POPUPMENUITEM_SELECTED, "change color of selected bubbles: "
                     + pnlTimeline.getTimeline().getSelectedBubbles());
      }
    });
    bubblePopup.add(item = new JMenuItem("Set Level Color"));
    item.addActionListener(new ActionListener() {
      public void actionPerformed (ActionEvent e) {
        pnlTimeline.setLevelColor();
        uilogger.log(UIEventType.POPUPMENUITEM_SELECTED, "set selected bubble level color: "
                     + pnlTimeline.getTimeline().getSelectedLevels());
      }
    });
    bubblePopup.add(item = new JMenuItem("Edit..."));
    item.addActionListener(new ActionListener() {
      public void actionPerformed (ActionEvent e) {
        pnlTimeline.dlgBubbleEditor = new TimelineBubbleEditor(frmTimeline);
        uilogger.log(UIEventType.POPUPMENUITEM_SELECTED, "edit bubble: "
                     + pnlTimeline.getTimeline().getSelectedBubbles());
      }
    });
    bubblePopup.add(item = new JMenuItem("Zoom to Selection"));
    item.addActionListener(new ActionListener() {
      public void actionPerformed (ActionEvent e) {
        pnlTimeline.zoomToSelectedBubbles();
        uilogger.log(UIEventType.POPUPMENUITEM_SELECTED, "zoom to selection"
                     + pnlTimeline.getTimeline().getSelectedBubbles());
      }
    });
    bubblePopup.add(item = new JMenuItem("Create Excerpt"));
    item.addActionListener(new ActionListener() {
      public void actionPerformed (ActionEvent e) {
        pnlTimeline.createExcerpt();
        uilogger.log(UIEventType.POPUPMENUITEM_SELECTED, "create excerpt: "
                     + pnlTimeline.getTimeline().getSelectedBubbles());
      }
    });
    bubblePopup.add(menuiMoveBubbleUp);
    bubblePopup.add(menuiMoveBubbleDown);
    bubblePopup.add(item = new JMenuItem("Delete"));
    item.addActionListener(new ActionListener() {
      public void actionPerformed (ActionEvent e) {
        pnlTimeline.deleteSelectedBubbles();
        uilogger.log(UIEventType.POPUPMENUITEM_SELECTED, "delete selected bubbles: "
                     + pnlTimeline.getTimeline().getSelectedBubbles());
      }
    });

    // popup menu for multiple ungrouped bubbles
    groupBubblePopup.add(item = new JMenuItem("Group Bubbles"));
    item.addActionListener(new ActionListener() {
      public void actionPerformed (ActionEvent e) {
        pnlTimeline.groupSelectedBubbles();
        uilogger.log(UIEventType.POPUPMENUITEM_SELECTED, "group selected bubbles: "
                     + pnlTimeline.getTimeline().getSelectedBubbles());
      }
    });
    groupBubblePopup.add(item = new JMenuItem("Change Color"));
    item.addActionListener(new ActionListener() {
      public void actionPerformed (ActionEvent e) {
        pnlTimeline.changeBubbleColor();
        uilogger.log(UIEventType.POPUPMENUITEM_SELECTED, "change color of selected bubbles: "
                     + pnlTimeline.getTimeline().getSelectedBubbles());
      }
    });
    groupBubblePopup.add(item = new JMenuItem("Set Level Color"));
    item.addActionListener(new ActionListener() {
      public void actionPerformed (ActionEvent e) {
        pnlTimeline.setLevelColor();
        uilogger.log(UIEventType.POPUPMENUITEM_SELECTED, "set selected bubble level color: "
                     + pnlTimeline.getTimeline().getSelectedLevels());
      }
    });
    groupBubblePopup.add(item = new JMenuItem("Edit..."));
    item.addActionListener(new ActionListener() {
      public void actionPerformed (ActionEvent e) {
        pnlTimeline.dlgBubbleEditor = new TimelineBubbleEditor(frmTimeline);
        uilogger.log(UIEventType.POPUPMENUITEM_SELECTED, "edit bubble: "
                     + pnlTimeline.getTimeline().getSelectedBubbles());
      }
    });
    groupBubblePopup.add(item = new JMenuItem("Zoom to Selection"));
    item.addActionListener(new ActionListener() {
      public void actionPerformed (ActionEvent e) {
        pnlTimeline.zoomToSelectedBubbles();
        uilogger.log(UIEventType.POPUPMENUITEM_SELECTED, "zoom to selection"
                     + pnlTimeline.getTimeline().getSelectedBubbles());
      }
    });
    groupBubblePopup.add(item = new JMenuItem("Create Excerpt"));
    item.addActionListener(new ActionListener() {
      public void actionPerformed (ActionEvent e) {
        pnlTimeline.createExcerpt();
        uilogger.log(UIEventType.POPUPMENUITEM_SELECTED, "create excerpt: "
                     + pnlTimeline.getTimeline().getSelectedBubbles());
      }
    });
    groupBubblePopup.add(menuiMoveBubbleUp2);
    groupBubblePopup.add(menuiMoveBubbleDown2);
    groupBubblePopup.add(item = new JMenuItem("Delete"));
    item.addActionListener(new ActionListener() {
      public void actionPerformed (ActionEvent e) {
        pnlTimeline.deleteSelectedBubbles();
        uilogger.log(UIEventType.POPUPMENUITEM_SELECTED, "delete selected bubbles: "
                     + pnlTimeline.getTimeline().getSelectedBubbles());
      }
    });

//    popup menu for multiple grouped bubbles
    ungroupBubblePopup.add(item = new JMenuItem("Change Color"));
    item.addActionListener(new ActionListener() {
      public void actionPerformed (ActionEvent e) {
        pnlTimeline.changeBubbleColor();
        uilogger.log(UIEventType.POPUPMENUITEM_SELECTED, "change color of selected bubbles: "
                     + pnlTimeline.getTimeline().getSelectedBubbles());
      }
    });
    ungroupBubblePopup.add(item = new JMenuItem("Set Level Color"));
    item.addActionListener(new ActionListener() {
      public void actionPerformed (ActionEvent e) {
        pnlTimeline.setLevelColor();
        uilogger.log(UIEventType.POPUPMENUITEM_SELECTED, "set selected bubble level color: "
                     + pnlTimeline.getTimeline().getSelectedLevels());
      }
    });
    ungroupBubblePopup.add(item = new JMenuItem("Edit..."));
    item.addActionListener(new ActionListener() {
      public void actionPerformed (ActionEvent e) {
       pnlTimeline.dlgBubbleEditor = new TimelineBubbleEditor(frmTimeline);
       uilogger.log(UIEventType.POPUPMENUITEM_SELECTED, "edit bubble: "
                    + pnlTimeline.getTimeline().getSelectedBubbles());
       }
    });
    ungroupBubblePopup.add(item = new JMenuItem("Zoom to Selection"));
    item.addActionListener(new ActionListener() {
      public void actionPerformed (ActionEvent e) {
        pnlTimeline.zoomToSelectedBubbles();
        uilogger.log(UIEventType.POPUPMENUITEM_SELECTED, "zoom to selection"
                     + pnlTimeline.getTimeline().getSelectedBubbles());
      }
    });
    ungroupBubblePopup.add(item = new JMenuItem("Create Excerpt"));
    item.addActionListener(new ActionListener() {
      public void actionPerformed (ActionEvent e) {
        pnlTimeline.createExcerpt();
        uilogger.log(UIEventType.POPUPMENUITEM_SELECTED, "create excerpt: "
                     + pnlTimeline.getTimeline().getSelectedBubbles());
      }
    });
    ungroupBubblePopup.add(menuiMoveBubbleUp3);
    ungroupBubblePopup.add(menuiMoveBubbleDown3);
    ungroupBubblePopup.add(item = new JMenuItem("Delete"));
    item.addActionListener(new ActionListener() {
      public void actionPerformed (ActionEvent e) {
        pnlTimeline.deleteSelectedBubbles();
        uilogger.log(UIEventType.POPUPMENUITEM_SELECTED, "delete selected bubbles: "
                     + pnlTimeline.getTimeline().getSelectedBubbles());
      }
    });

    // popup menu for timepoints
    timepointPopup.add(item = new JMenuItem("Edit..."));
    item.addActionListener(new ActionListener() {
      public void actionPerformed (ActionEvent e) {
        dlgTimepointEditor = new TimepointEditor(frmTimeline);
        uilogger.log(UIEventType.POPUPMENUITEM_SELECTED, "edit timepoint label: "
                     + pnlTimeline.getTimeline().getLastTimepointClicked());
      }
    });
    timepointPopup.add(item = new JMenuItem("Delete"));
    item.addActionListener(new ActionListener() {
      public void actionPerformed (ActionEvent e) {
        if (pnlTimeline.getTimeline().areTimepointsSelected()) {
          pnlTimeline.deleteSelectedTimepoint();
          uilogger.log(UIEventType.POPUPMENUITEM_SELECTED, "delete timepoint: "
                       + pnlTimeline.getTimeline().getLastTimepointClicked());
        }
        else {
          pnlTimeline.deleteSelectedMarker();
          uilogger.log(UIEventType.POPUPMENUITEM_SELECTED, "delete marker: "
                       + pnlTimeline.getTimeline().getLastMarkerClicked());
        }
      }
    });

    // popup menu for markers
    markerPopup.add(item = new JMenuItem("Edit..."));
    item.addActionListener(new ActionListener() {
      public void actionPerformed (ActionEvent e) {
        dlgMarkerEditor = new MarkerEditor(frmTimeline);
        uilogger.log(UIEventType.POPUPMENUITEM_SELECTED, "edit marker: "
                     + pnlTimeline.getTimeline().getLastMarkerClicked());
      }
    });
    markerPopup.add(item = new JMenuItem("Delete"));
    item.addActionListener(new ActionListener() {
      public void actionPerformed (ActionEvent e) {
        pnlTimeline.deleteSelectedMarker();
        uilogger.log(UIEventType.POPUPMENUITEM_SELECTED, "delete marker: "
                     + pnlTimeline.getTimeline().getLastMarkerClicked());
      }
    });

    // popup menu for timeline
    timelinePopup.add(item = new JMenuItem("Clear All"));
    item.addActionListener(new ActionListener() {
      public void actionPerformed (ActionEvent e) {
        pnlTimeline.resetTimeline();
        uilogger.log(UIEventType.POPUPMENUITEM_SELECTED, "clear all");
      }
    });
    if (System.getProperty("os.name").startsWith("Mac OS")) {
      Object radioIcon = UIManager.get(RADIO_ICON_KEY);
      UIManager.put(RADIO_ICON_KEY, UIManager.get(CHECK_ICON_KEY));
      pnlTimeline.menuiShowTimesMac = new JRadioButtonMenuItem("Show Times", false);
      UIManager.put(RADIO_ICON_KEY, radioIcon);
      timelinePopup.add(pnlTimeline.menuiShowTimesMac);
      pnlTimeline.menuiShowTimesMac.addActionListener(new ActionListener() {
        public void actionPerformed (ActionEvent e) {
          boolean show = ((JRadioButtonMenuItem)e.getSource()).isSelected();
          pnlTimeline.showTimes(show);
          pnlTimeline.menubTimeline.menuiShowTimesMac.setSelected(show);
          pnlTimeline.refreshTimeline();
          uilogger.log(UIEventType.POPUPMENUITEM_SELECTED, "show times" + pnlTimeline.menubTimeline.menuiShowTimesMac.isSelected());
        }
      });
    }
    else {
      pnlTimeline.menuiShowTimes = new JCheckBoxMenuItem("Show Times", false);
      timelinePopup.add(pnlTimeline.menuiShowTimes);
      pnlTimeline.menuiShowTimes.addActionListener(new ActionListener() {
        public void actionPerformed (ActionEvent e) {
          boolean show = ((JCheckBoxMenuItem)e.getSource()).isSelected();
          pnlTimeline.showTimes(show);
          pnlTimeline.menubTimeline.menuiShowTimes.setSelected(show);
          uilogger.log(UIEventType.POPUPMENUITEM_SELECTED, "show times" + pnlTimeline.menubTimeline.menuiShowTimes.isSelected());
        }
      });
    }
    timelinePopup.add(item = new JMenuItem("Print"));
    item.addActionListener(new ActionListener() {
      public void actionPerformed (ActionEvent e) {
        pnlTimeline.printTimelinePanel();
        uilogger.log(UIEventType.POPUPMENUITEM_SELECTED, "print timeline");
      }
    });
    timelinePopup.add(item = new JMenuItem("Edit Properties..."));
    item.addActionListener(new ActionListener() {
      public void actionPerformed (ActionEvent e) {
        pnlTimeline.openTimelineProperties();
        uilogger.log(UIEventType.POPUPMENUITEM_SELECTED, "edit timeline properties");
      }
    });
    timelinePopup.add(item = new JMenuItem("Change Background Color"));
    item.addActionListener(new ActionListener() {
      public void actionPerformed (ActionEvent e) {
        pnlTimeline.setBackgroundColor();
        uilogger.log(UIEventType.POPUPMENUITEM_SELECTED, "change background color");
      }
    });

    // popup menu for background panel
    panelPopup.add(item = new JMenuItem("Change Background Color"));
    item.addActionListener(new ActionListener() {
      public void actionPerformed (ActionEvent e) {
        pnlTimeline.setBackgroundColor();
        uilogger.log(UIEventType.POPUPMENUITEM_SELECTED, "change background color");
      }
    });
  }
}