package ui.timeliner;

import javax.swing.*;
import javax.swing.undo.*;
import java.awt.*;
import javax.swing.border.*;
import java.awt.event.*;
import javax.swing.event.*;
import java.util.*;
import util.logging.*;
import ui.media.*;
import ui.common.*;
//import org.apache.log4j.Logger;

/**
 * TimelineBubbleEditor.java
 */

public class TimelineBubbleEditor extends JDialog {

  private static final long serialVersionUID = 1L;
// external components
  private TimelinePanel pnlTimeline;
  private TimelineFrame frmTimeline;
  private TimelinePlayer tPlayer;
  private TimelineLocalPlayer tLocalPlayer;
  private Timeline timeline;
  private TimelineMenuBar menubTimeline;
  AudioControlPanel pnlAudioControl = new AudioControlPanel();
  //private Logger log = Logger.getLogger(TimelineBubbleEditor.class);
  protected UILogger uilogger;

  // vectors for storing temporary values
  protected Vector<Integer> editedBubbles = new Vector<Integer>();
  protected Vector<String> potentialLabels = new Vector<String>();
  protected Vector<String> potentialAnnotations = new Vector<String>();
  protected Vector<String> oldLabels = new Vector<String>();
  protected Vector<String> oldAnnotations = new Vector<String>();

  // titled borders
  protected TitledBorder bordAnnotation = new TitledBorder(" Annotation ");
  protected TitledBorder bordPlayback = new TitledBorder(" Bubble Playback ");
  protected TitledBorder bordSelect = new TitledBorder(" Select Another Bubble ");
  protected TitledBorder bordLevel = new TitledBorder("Adjust Bubble Level");

  // panels
  protected JPanel pnlLabel = new JPanel();
  protected JPanel pnlAnnotation = new JPanel();
  protected JPanel pnlControls = new JPanel();
  protected JPanel pnlButtons = new JPanel();
  protected JPanel pnlPlayback = new JPanel();
  protected JPanel pnlNavigate = new JPanel();

  // labels
  protected JLabel lblLabel = new JLabel("Label: ");
  protected JLabel lblUp = new JLabel("Up");
  protected JLabel lblDown = new JLabel("Down");
  protected JLabel lblLeft = new JLabel("Left");
  protected JLabel lblRight = new JLabel("Right");
  protected JLabel lblDivider = new JLabel(icoDivider);

  // text fields and panes
  protected JTextField fldBubbleLabel = new JTextField();
  protected JTextPane tpAnnotation = new JTextPane();
  protected JScrollPane scrpAnnotation = new JScrollPane(tpAnnotation);

  // fonts
  private java.awt.Font timelineFont;
  private java.awt.Font unicodeFont = UIUtilities.fontUnicodeBigger;

  // icons
  final static ImageIcon icoUp = UIUtilities.icoUpSmall;
  final static ImageIcon icoDown = UIUtilities.icoDownSmall;
  final static ImageIcon icoLeft = UIUtilities.icoLeftSmall;
  final static ImageIcon icoRight = UIUtilities.icoRightSmall;
  final static ImageIcon icoDivider = UIUtilities.icoMediaDivider;

  // check box
  protected JCheckBox chkLoop = new JCheckBox("Repeat", false);

  // buttons
  protected JButton btnUp = new JButton();
  protected JButton btnDown = new JButton();
  protected JButton btnRight = new JButton();
  protected JButton btnLeft = new JButton();
  protected JButton btnOk = new JButton("OK");
  protected JButton btnCancel = new JButton("Cancel");
  protected JButton btnApply = new JButton("Apply");

  // variables
  protected int buttonWidth;
  protected boolean recentApplyMade = false;
  int currentBubbleNum;
  BubbleTreeNode currNode;

  /**
   * constructor
   */
  public TimelineBubbleEditor(TimelineFrame tf) {
    super(tf);
    frmTimeline = tf;
    pnlTimeline = frmTimeline.getTimelinePanel();
    timeline = pnlTimeline.getTimeline();
    tPlayer = pnlTimeline.getPlayer();
    tLocalPlayer = pnlTimeline.getLocalPlayer();
    menubTimeline = pnlTimeline.getMenuBar();
    final TimelineControlPanel pnlControl = frmTimeline.getControlPanel();
    uilogger = frmTimeline.getUILogger();

    // update variables
    timeline.editingBubbles = true;
    timeline.updatePopupMenus();

    // set up dialog
    if (System.getProperty("os.name").startsWith("Mac OS")) {
      timelineFont = UIUtilities.fontDialogMacSmallest;
      menubTimeline.disableMenuKeyboardShortcuts();
      buttonWidth = 65;
    } else {
      timelineFont = UIUtilities.fontDialogWin;
      buttonWidth = 60;
    }
    this.setTitle("Edit Bubbles");
    this.setLocationRelativeTo(frmTimeline);
    this.setModal(true);
    this.setSize(new Dimension(430, 450));
    this.setLocation(frmTimeline.getWidth() - (this.getWidth() + 5), 30);

    // text fields and panes
    fldBubbleLabel.setBounds(new Rectangle(250, 20));
    fldBubbleLabel.setMinimumSize(new Dimension(250, 25));
    fldBubbleLabel.setPreferredSize(new Dimension(250, 25));
    fldBubbleLabel.setFont(unicodeFont);
    fldBubbleLabel.setToolTipText("Edit the bubble label");
    tpAnnotation.setPreferredSize(new Dimension(430, 375));
    tpAnnotation.setFont(unicodeFont);
    tpAnnotation.setToolTipText("Edit the bubble annotation");

    // media buttons
    if (timeline.playerIsPlaying()) {
      pnlAudioControl.btnPlay.setIcon(UIUtilities.icoPause);
    }
    else {
      pnlAudioControl.btnPlay.setIcon(UIUtilities.icoPlay);
    }
    pnlTimeline.btnBubbleEditorPlay = pnlAudioControl.btnPlay;
    pnlAudioControl.btnPlay.setToolTipText("Play");
    pnlAudioControl.btnPlay.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (timeline.playerIsPlaying()) {
          pnlControl.btn_pauseAction();
          uilogger.log(UIEventType.BUTTON_CLICKED, "pause");
        }
        else {
          timeline.startPlayer();
          pnlControl.btn_playAction();
          uilogger.log(UIEventType.BUTTON_CLICKED, "play");
        }
      }
    });

    pnlAudioControl.btnStop.setIcon(UIUtilities.icoStop);
    pnlAudioControl.btnStop.setToolTipText("Stop");
    pnlAudioControl.btnStop.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        pnlControl.btn_stopAction();
        uilogger.log(UIEventType.BUTTON_CLICKED, "stop");
      }
    });

    pnlAudioControl.btnRW.setToolTipText("Press and hold for RW");
    pnlAudioControl.btnRW.setIcon(UIUtilities.icoRW);
    pnlAudioControl.btnRW.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        pnlControl.doRWTracking();
      }
      public void mouseReleased(MouseEvent e) {
        pnlControl.doStopTracking();
        uilogger.log(UIEventType.BUTTON_CLICKED, "rewind");
      }
    });

    pnlAudioControl.btnFF.setToolTipText("Press and hold for FF");
    pnlAudioControl.btnFF.setIcon(UIUtilities.icoFF);
    pnlAudioControl.btnFF.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        pnlControl.doFFTracking();
      }
      public void mouseReleased(MouseEvent e) {
        pnlControl.doStopTracking();
        uilogger.log(UIEventType.BUTTON_CLICKED, "fast forward");
      }
    });

    chkLoop.setToolTipText("Select to loop the bubble playback");
    chkLoop.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        timeline.loopingOn = chkLoop.isSelected();
        uilogger.log(UIEventType.CHECKBOX_SELECTED, "bubble playback looping: " + chkLoop.isSelected());
      }
    });

    // navigation buttons
    btnUp.setPreferredSize(new Dimension(buttonWidth+5, 18));
    btnUp.setToolTipText("Go to the upper bubble");
    btnUp.setIcon(icoUp);
    btnUp.setText("Up    ");
    btnUp.setMargin(new Insets(0, 0, 0, 0));
    btnUp.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        saveLabelAndAnnotation();
        currNode = currNode.getParentNode();
        updateLabelAndAnnotation();
        updateNavigationButtons();

        uilogger.log(UIEventType.BUTTON_CLICKED, "navigate to upper bubble");
      }
    });

    btnDown.setPreferredSize(new Dimension(buttonWidth+5, 18));
    btnDown.setToolTipText("Go to the first lower bubble");
    btnDown.setIcon(icoDown);
    btnDown.setText("Down");
    btnDown.setMargin(new Insets(0, 0, 0, 0));
    btnDown.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        saveLabelAndAnnotation();
        currNode = currNode.getFirstChildNode();
        updateLabelAndAnnotation();
        updateNavigationButtons();
        uilogger.log(UIEventType.BUTTON_CLICKED, "navigate to lower bubble");
      }
    });

    btnLeft.setPreferredSize(new Dimension(buttonWidth-10, 30));
    btnLeft.setToolTipText("Go to the previous bubble");
    btnLeft.setIcon(icoLeft);
    btnLeft.setText("Left");
    btnLeft.setMargin(new Insets(0, 0, 0, 0));
    btnLeft.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        saveLabelAndAnnotation();
        currNode = currNode.getPreviousSiblingAtLevel(currNode.getBubble().getLevel());
        updateLabelAndAnnotation();
        updateNavigationButtons();
        uilogger.log(UIEventType.BUTTON_CLICKED, "navigate to left bubble");
      }
    });

    btnRight.setPreferredSize(new Dimension(buttonWidth-10, 30));
    btnRight.setToolTipText("Go to the next bubble");
    btnRight.setText("Right");
    btnRight.setIcon(icoRight);
    btnRight.setMargin(new Insets(0, 0, 0, 0));
    btnRight.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        saveLabelAndAnnotation();
        currNode = currNode.getNextSiblingAtLevel(currNode.getBubble().getLevel());
        updateLabelAndAnnotation();
        updateNavigationButtons();
        uilogger.log(UIEventType.BUTTON_CLICKED, "navigate to right bubble");
      }
    });

    // other buttons
    btnOk.setFont(timelineFont);
    btnOk.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        saveLabelAndAnnotation();

        // back up old values for undo and set new values
        undoPreviousApplys();
        for (int i = 0; i < editedBubbles.size(); i++) {
          int currNum = ((Integer)editedBubbles.elementAt(i)).intValue();
          Bubble currBubble = timeline.getBubble(currNum);
          oldLabels.addElement(currBubble.getLabel());
          oldAnnotations.addElement(currBubble.getAnnotation());
          currBubble.setLabel((String)potentialLabels.elementAt(i));
          currBubble.setAnnotation((String)potentialAnnotations.elementAt(i));
        }

        timeline.makeDirty();
        timeline.setLocalStartOffset(0);
        timeline.setLocalEndOffset(timeline.getPlayerDuration());
        timeline.setPlayerOffset(timeline.getSlider().getValue());
        timeline.loopingOn = false;
        pnlTimeline.btnBubbleEditorPlay = null;
        pnlTimeline.btnBubbleEditorUpLevel = null;
        pnlTimeline.btnBubbleEditorDownLevel = null;
        closeWindow();
        pnlControl.updateAnnotationPane();
        pnlTimeline.undoManager.undoableEditHappened(new UndoableEditEvent(pnlTimeline,
            new UndoableEditBubble(oldLabels, potentialLabels, oldAnnotations, potentialAnnotations,
            editedBubbles, timeline)));
        pnlTimeline.updateUndoMenu();
        uilogger.log(UIEventType.BUTTON_CLICKED, "accept bubble edits");
      }
    });

    btnApply.setFont(timelineFont);
    btnApply.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        saveLabelAndAnnotation();

        // back up old values for undo and set new values
        undoPreviousApplys();
        recentApplyMade = true;
        for (int i = 0; i < editedBubbles.size(); i++) {
          int currNum = ((Integer)editedBubbles.elementAt(i)).intValue();
          Bubble currBubble = timeline.getBubble(currNum);
          oldLabels.addElement(currBubble.getLabel());
          oldAnnotations.addElement(currBubble.getAnnotation());
          currBubble.setLabel((String)potentialLabels.elementAt(i));
          currBubble.setAnnotation((String)potentialAnnotations.elementAt(i));
        }

        timeline.makeDirty();
        pnlTimeline.refreshTimeline();
        pnlControl.updateAnnotationPane();
        pnlTimeline.undoManager.undoableEditHappened(new UndoableEditEvent(pnlTimeline,
            new UndoableEditBubble(oldLabels, potentialLabels, oldAnnotations, potentialAnnotations,
            editedBubbles, timeline)));
        pnlTimeline.updateUndoMenu();
        uilogger.log(UIEventType.BUTTON_CLICKED, "apply bubble edits");
      }
    });

    btnCancel.setFont(timelineFont);
    btnCancel.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        timeline.setLocalStartOffset(0);
        timeline.setLocalEndOffset(timeline.getPlayerDuration());
        timeline.setPlayerOffset(timeline.getSlider().getValue());
        timeline.loopingOn = false;
        pnlTimeline.btnBubbleEditorPlay = null;
        pnlTimeline.btnBubbleEditorUpLevel = null;
        pnlTimeline.btnBubbleEditorDownLevel = null;
        closeWindow();
        uilogger.log(UIEventType.BUTTON_CLICKED, "cancel bubble edits");
      }
    });

    // panels
    pnlLabel.setLayout(new FlowLayout(FlowLayout.LEFT));
    pnlAnnotation.setLayout(new BorderLayout());
    pnlAnnotation.setBorder(bordAnnotation);
    pnlControls.setLayout(new FlowLayout(FlowLayout.CENTER));
    pnlPlayback.setLayout(new FlowLayout(FlowLayout.LEFT));
    pnlPlayback.setBorder(bordPlayback);
    pnlNavigate.setBorder(bordSelect);

    // borders
    bordAnnotation.setTitleFont(timelineFont);
    bordPlayback.setTitleFont(timelineFont);
    bordSelect.setTitleFont(timelineFont);

    // labels
    lblLabel.setFont(timelineFont);
    lblUp.setFont(timelineFont);
    lblDown.setFont(timelineFont);
    lblLeft.setFont(timelineFont);
    lblRight.setFont(timelineFont);

    // check box
    chkLoop.setFont(timelineFont);

    // layout
    GridBagLayout gridbag = new GridBagLayout();
    pnlNavigate.setLayout(gridbag);
    GridBagLayout gridbag2 = new GridBagLayout();
    Container pane = this.getContentPane();
    pane.setLayout(gridbag2);
    pnlLabel.add(lblLabel);
    pnlLabel.add(fldBubbleLabel);
    pnlAnnotation.add(scrpAnnotation, BorderLayout.CENTER);
    pnlAnnotation.add(pnlControls, BorderLayout.SOUTH);
    pnlControls.add(pnlPlayback);
    pnlControls.add(pnlNavigate);
    pnlPlayback.add(pnlAudioControl.btnPlay);
    pnlPlayback.add(pnlAudioControl.btnStop);
    pnlPlayback.add(pnlAudioControl.btnRW);
    pnlPlayback.add(pnlAudioControl.btnFF);
    pnlPlayback.add(chkLoop);
    pnlButtons.add(btnOk);
    pnlButtons.add(btnCancel);
    pnlButtons.add(btnApply);

    TimelineUtilities.createConstraints(pnlNavigate, btnLeft, 0, 0, 1, 2, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, 2, 2, 2, 2, 0, 0);
    TimelineUtilities.createConstraints(pnlNavigate, btnUp, 1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, 2, 2, 2, 2, 0, 0);
    TimelineUtilities.createConstraints(pnlNavigate, btnDown, 1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, 2, 2, 2, 2, 0, 0);
    TimelineUtilities.createConstraints(pnlNavigate, btnRight, 2, 0, 1, 2, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, 2, 2, 2, 2, 0, 0);

    TimelineUtilities.createConstraints(pane, pnlLabel, 0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, 2, 2, 2, 2, 0, 0);
    TimelineUtilities.createConstraints(pane, pnlAnnotation, 0, 1, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, 2, 2, 2, 2, 0, 0);
    TimelineUtilities.createConstraints(pane, pnlButtons, 0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE, 2, 2, 2, 2, 0, 0);

    // initialize with current label and annotation
    editedBubbles.removeAllElements();
    currentBubbleNum = ((Integer)(timeline.getSelectedBubbles().elementAt(0))).intValue();
    currNode = timeline.getBubbleNode(currentBubbleNum);
    updateLabelAndAnnotation();

    // update navigation buttons
    updateNavigationButtons();

    // show dialog
    this.setVisible(true);
  }

  /**
   * closes the window
   */
  private void closeWindow() {
    if (System.getProperty("os.name").startsWith("Mac OS")) { // to fix a 1.3 menu key bug
      menubTimeline.enableMenuKeyboardShortcuts();
    }
    timeline.editingBubbles = false;
    this.setVisible(false);
  }

  /**
   * saves the current label and annotation to a temporary vector, to be applied later
   */
  private void saveLabelAndAnnotation() {
    // get annotation and label for currently selected bubble
    int prevSave = editedBubbles.indexOf(Integer.valueOf(currentBubbleNum));
    if (prevSave == -1) { // this bubble has not been saved before
      editedBubbles.addElement(Integer.valueOf(currentBubbleNum));
      potentialLabels.addElement(fldBubbleLabel.getText());
      potentialAnnotations.addElement(tpAnnotation.getText());
    }
    else {
      editedBubbles.setElementAt(Integer.valueOf(currentBubbleNum), prevSave);
      potentialLabels.setElementAt(fldBubbleLabel.getText(), prevSave);
      potentialAnnotations.setElementAt(tpAnnotation.getText(), prevSave);
    }
  }

  /**
   * updates the displayed label and annotation
   */
  private void updateLabelAndAnnotation() {
    currentBubbleNum = timeline.topBubbleNode.getPreOrderIndex(currNode);
    Bubble currBubble = timeline.getBubble(currentBubbleNum);
    timeline.repositionHead(currentBubbleNum);
    int prevPos = editedBubbles.indexOf(Integer.valueOf(currentBubbleNum));
    if (prevPos != -1) { // this bubble has already been edited
      fldBubbleLabel.setText((String)potentialLabels.elementAt(prevPos));
      tpAnnotation.setText((String)potentialAnnotations.elementAt(prevPos));
    }
    else { // it has not been edited
      fldBubbleLabel.setText(currBubble.getLabel());
      tpAnnotation.setText(currBubble.getAnnotation());
    }
    if (!currBubble.isSelected()) {
      timeline.selectBubble(currentBubbleNum, 0);
    }

    // set offset
    int currOffset = timeline.getPlayerOffset();
    int currIndex = timeline.topBubbleNode.getLeafIndex(currNode.getFirstLeaf());
    if (currOffset < timeline.getOffsetAt(currIndex) ||
        currOffset >= timeline.getOffsetAt(currIndex + 1)) {
      timeline.setPlayerOffset(timeline.getOffsetAt(currIndex));
    }
    pnlTimeline.refreshTimeline();

    // update start and stop times
    int startTimepointNum = timeline.getTimepointNumberAtPixel(currBubble.getStart());
    int endTimepointNum = timeline.getTimepointNumberAtPixel(currBubble.getEnd());
    timeline.setLocalStartOffset(timeline.getOffsetAt(startTimepointNum));
    timeline.setLocalEndOffset(timeline.getOffsetAt(endTimepointNum));
    timeline.setNextImportantOffset(timeline.getLocalEndOffset());
  }

  /**
   * updates the navigation buttons
   */
  private void updateNavigationButtons() {
    Bubble currBubble = timeline.getBubble(currentBubbleNum);
    btnUp.setEnabled(!((BubbleTreeNode)currNode.getParent()).isRoot());
    btnDown.setEnabled(currNode.getChildCount() != 0);
    btnLeft.setEnabled(currNode.getPreviousSiblingAtLevel(currBubble.getLevel()) != null);
    btnRight.setEnabled(currNode.getNextSiblingAtLevel(currBubble.getLevel()) != null);
  }

  /**
   * undoes any previous "Apply"s
   * (only allow a single undo for all of the property changes)
   */
  private void undoPreviousApplys() {
    if (recentApplyMade && pnlTimeline.undoManager.canUndo()) {
      if (pnlTimeline.undoManager.getUndoPresentationName().equals("Undo Edit Bubble") ||
          pnlTimeline.undoManager.getUndoPresentationName().equals("Undo Edit Bubbles")) {
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