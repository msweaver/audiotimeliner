package ui.timeliner;

import javax.swing.*;
import javax.swing.undo.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import java.util.*;
import util.logging.*;
import ui.common.*;
//import org.apache.log4j.Logger;

/**
 * TimepointEditor.java
 */

public class TimepointEditor extends JDialog {

	private static final long serialVersionUID = 1L;
// external components
  private  TimelinePanel pnlTimeline;
  private  TimelineFrame frmTimeline;
  private  Timeline timeline;
  private  TimelineMenuBar menubTimeline;
  //private  Logger log = Logger.getLogger(TimepointEditor.class);
  protected  UILogger uilogger;

  // visual components
  private  JTextField fldTimepointLabel;
  private  JLabel lblLabel = new JLabel("Label: ");

  // variables
  //private  String oldText;
  protected Vector<Integer> editedTimepoints = new Vector<Integer>();
  protected Vector<String> potentialLabels = new Vector<String>();
  protected Vector<String> oldLabels = new Vector<String>();
  protected int currTimepoint;
  protected int buttonWidth;
  protected boolean recentApplyMade = false;

  // fonts
  private  java.awt.Font timelineFont;
  private  java.awt.Font unicodeFont;

  // layout
  protected JPanel pnlNavigate = new JPanel();
  protected JPanel pnlLabel = new JPanel();
  protected JPanel pnlButtons = new JPanel();

  // icons
  final  ImageIcon icoLeft = UIUtilities.icoLeftSmall;
  final  ImageIcon icoRight = UIUtilities.icoRightSmall;

  // buttons
  protected JButton btnRight = new JButton();
  protected JButton btnLeft = new JButton();
  protected JButton btnOk = new JButton("OK");
  protected JButton btnCancel = new JButton("Cancel");
  protected JButton btnApply = new JButton("Apply");

  // labels
  protected JLabel lblLeft = new JLabel("Left");
  protected JLabel lblRight = new JLabel("Right");

  /**
   * constructor
   */
  public TimepointEditor(TimelineFrame tf) {
    super(tf);
    frmTimeline = tf;
    pnlTimeline = frmTimeline.getTimelinePanel();
    timeline = pnlTimeline.getTimeline();
    uilogger = frmTimeline.getUILogger();
    menubTimeline = pnlTimeline.getMenuBar();

    // set up dialog
    int dialogWidth;
    int dialogHeight;
    if (System.getProperty("os.name").startsWith("Mac OS")) {
        timelineFont = UIUtilities.fontDialogMacSmaller;
        unicodeFont = UIUtilities.fontUnicodeBigger;
        dialogWidth = 480;
        dialogHeight = 110;
        menubTimeline.disableMenuKeyboardShortcuts();
        buttonWidth = 65;
     } else {
        timelineFont = UIUtilities.fontDialogWin;
        unicodeFont = UIUtilities.fontUnicodeBigger;
        dialogWidth = 400;
        dialogHeight = 120;
        buttonWidth = 60;
     }
     this.setTitle("Edit Timepoints");
     this.setLocation((frmTimeline.getWidth()/2)-(this.getWidth()/2), (frmTimeline.getHeight()/2)-(this.getHeight()/2));
     this.setModal(true);
     this.setSize(new Dimension(dialogWidth, dialogHeight));

    // text field
    fldTimepointLabel = new JTextField();
    fldTimepointLabel.setFont(unicodeFont);
    fldTimepointLabel.setBounds(new Rectangle(dialogWidth - 100, 20));
    fldTimepointLabel.setMinimumSize(new Dimension(dialogWidth - 100, 25));
    fldTimepointLabel.setPreferredSize(new Dimension(dialogWidth - 100, 25));

    // buttons
    btnLeft.setPreferredSize(new Dimension(buttonWidth + 10, 30));
    btnLeft.setToolTipText("Go to the previous timepoint");
    btnLeft.setIcon(icoLeft);
    btnLeft.setText("Previous");
    btnLeft.setMargin(new Insets(0, 0, 0, 0));
    btnLeft.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        saveTimepointLabel();
        if (currTimepoint > 0) {
          currTimepoint = currTimepoint - 1;
        }
        updateTimepointLabel();
        updateNavigationButtons();
        uilogger.log(UIEventType.BUTTON_CLICKED, "navigate to previous timepoint");
      }
    });

    btnRight.setPreferredSize(new Dimension(buttonWidth + 10, 30));
    btnRight.setToolTipText("Go to the next timepoint");
    btnRight.setText("Next");
    btnRight.setIcon(icoRight);
    btnRight.setMargin(new Insets(0, 0, 0, 0));
    btnRight.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        saveTimepointLabel();
        if (currTimepoint < timeline.getNumBaseBubbles()) {
          currTimepoint = currTimepoint + 1;
        }
        updateTimepointLabel();
        updateNavigationButtons();
        uilogger.log(UIEventType.BUTTON_CLICKED, "navigate to next timepoint");
      }
    });

    btnOk.setFont(timelineFont);
    btnOk.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        saveTimepointLabel();

        // back up old values for undo and set new values
        undoPreviousApplys();
        for (int i = 0; i < editedTimepoints.size(); i++) {
          int currNum = ((Integer)editedTimepoints.elementAt(i)).intValue();
          Timepoint currTimepoint = timeline.getTimepoint(currNum);
          oldLabels.addElement(currTimepoint.getLabel());
          currTimepoint.setLabel((String)potentialLabels.elementAt(i));
        }

        timeline.makeDirty();
        closeWindow();
        frmTimeline.getControlPanel().updateAnnotationPane();
        pnlTimeline.undoManager.undoableEditHappened(new UndoableEditEvent(pnlTimeline,
            new UndoableEditTimepoint(oldLabels, potentialLabels, editedTimepoints, timeline)));
        pnlTimeline.updateUndoMenu();
        uilogger.log(UIEventType.BUTTON_CLICKED, "accept timepoint edits");
      }
    });

    btnApply.setFont(timelineFont);
    btnApply.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        saveTimepointLabel();

        // back up old values for undo and set new values
        undoPreviousApplys();
        recentApplyMade = true;
        for (int i = 0; i < editedTimepoints.size(); i++) {
          int currNum = ((Integer)editedTimepoints.elementAt(i)).intValue();
          Timepoint currTimepoint = timeline.getTimepoint(currNum);
          oldLabels.addElement(currTimepoint.getLabel());
          currTimepoint.setLabel((String)potentialLabels.elementAt(i));
        }

        timeline.makeDirty();
        pnlTimeline.refreshTimeline();
        frmTimeline.getControlPanel().updateAnnotationPane();
        pnlTimeline.undoManager.undoableEditHappened(new UndoableEditEvent(pnlTimeline,
            new UndoableEditTimepoint(oldLabels, potentialLabels, editedTimepoints, timeline)));
        pnlTimeline.updateUndoMenu();
        uilogger.log(UIEventType.BUTTON_CLICKED, "apply timepoint edits");
      }
    });

    btnCancel.setFont(timelineFont);
    btnCancel.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        closeWindow();
        uilogger.log(UIEventType.BUTTON_CLICKED, "cancel bubble edits");
      }
    });

    // panels
    pnlLabel.setLayout(new FlowLayout(FlowLayout.LEFT));
    pnlButtons.setLayout(new FlowLayout(FlowLayout.RIGHT));
    pnlNavigate.setLayout(new FlowLayout(FlowLayout.LEFT));

    // labels
    lblLabel.setFont(timelineFont);

    // layout
    Container pane = this.getContentPane();
    GridBagLayout gridbag = new GridBagLayout();
    pane.setLayout(gridbag);
    pnlLabel.add(lblLabel);
    pnlLabel.add(fldTimepointLabel);
    pnlButtons.add(btnOk);
    pnlButtons.add(btnCancel);
    pnlButtons.add(btnApply);
    pnlNavigate.add(btnLeft);
    pnlNavigate.add(btnRight);

    TimelineUtilities.createConstraints(pane, pnlLabel, 0, 0, 2, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, 2, 2, 2, 2, 0, 0);
    TimelineUtilities.createConstraints(pane, pnlNavigate, 0, 1, 1, 1, 1.0, 0.0, GridBagConstraints.SOUTHWEST, GridBagConstraints.NONE, 2, 2, 2, 2, 0, 0);
    TimelineUtilities.createConstraints(pane, pnlButtons, 1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE, 2, 2, 2, 2, 0, 0);

    // set initial label and update navigation buttons
    editedTimepoints.removeAllElements();
    currTimepoint = timeline.getLastTimepointClicked();
    updateTimepointLabel();
    updateNavigationButtons();

    // show dialog
    this.setVisible(true); // show();
  }

  /**
   * CloseWindow: closes the window
   */
  private void closeWindow() {
    if (System.getProperty("os.name").startsWith("Mac OS")) { // to fix a 1.3 menu key bug
      menubTimeline.enableMenuKeyboardShortcuts();
    }
    timeline.deselectAllTimepointsAndMarkers();
    this.setVisible(false); // hide();
  }

  /**
   * saveTimepointLabel: save timepoint label before navigating, to apply later
   */
  private void saveTimepointLabel() {
    // get label for currently selected timepoint
    int prevSave = editedTimepoints.indexOf(Integer.valueOf(currTimepoint));
    if (prevSave == -1) { // this bubble has not been saved before
      editedTimepoints.addElement(Integer.valueOf(currTimepoint));
      potentialLabels.addElement(fldTimepointLabel.getText());
    }
    else {
      editedTimepoints.setElementAt(Integer.valueOf(currTimepoint), prevSave);
      potentialLabels.setElementAt(fldTimepointLabel.getText(), prevSave);
    }
  }

  /**
   *  updateTimepointLabel: update the displayed timepoint label
   */
  private void updateTimepointLabel() {
    Timepoint currentTimepoint = timeline.getTimepoint(currTimepoint);
    int prevPos = editedTimepoints.indexOf(Integer.valueOf(currTimepoint));
    if (prevPos != -1) { // this timepoint has already been edited
      fldTimepointLabel.setText((String)potentialLabels.elementAt(prevPos));
    }
    else { // it has not been edited
      fldTimepointLabel.setText(currentTimepoint.getLabel());
    }
    if (!currentTimepoint.isSelected()) {
      timeline.selectTimepoint(currTimepoint);
    }
    pnlTimeline.refreshTimeline();
  }

  /**
   * updateNavigationButtons: update the navigation buttons
   */
  private void updateNavigationButtons() {
    btnLeft.setEnabled(currTimepoint > 0);
    btnRight.setEnabled(currTimepoint < timeline.getNumBaseBubbles() - 1);
  }

  /**
   * undoPreviousApplys: undo any previous "Apply"s
   * (only allow a single undo for all of the property changes)
   */
  private void undoPreviousApplys() {
    if (recentApplyMade && pnlTimeline.undoManager.canUndo()) {
      if (pnlTimeline.undoManager.getUndoPresentationName().equals("Undo Edit Timepoint") ||
          pnlTimeline.undoManager.getUndoPresentationName().equals("Undo Edit Timepoints")) {
        try {
          pnlTimeline.undoManager.undo();
        }
        catch (CannotUndoException cue) {
          cue.printStackTrace();
        }
        pnlTimeline.updateUndoMenu();
      }
    }
    recentApplyMade = false;
  }
}