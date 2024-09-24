package ui.timeliner;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.StringWriter;
import java.util.Vector;
import java.awt.*;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
//import java.awt.font.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.UndoableEditEvent;
import javax.swing.text.AbstractDocument;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.EditorKit;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.undo.CannotUndoException;

import org.apache.log4j.Logger;

import ui.common.UIUtilities;
import ui.media.AudioControlPanel;
import util.HtmlLineBreakDocumentFilter;
import util.logging.UIEventType;
import util.logging.UILogger;

//import org.apache.log4j.Logger;

/**
 * TimelineBubbleEditor.java
 */

public class TimelineBubbleEditor extends JDialog {

  private static final long serialVersionUID = 1L;
// external components
  private TimelinePanel pnlTimeline;
  private TimelineFrame frmTimeline;
  //private TimelinePlayer tPlayer;
  //private TimelineLocalPlayer tLocalPlayer;
  private Timeline timeline;
  private TimelineMenuBar menubTimeline;
  AudioControlPanel pnlAudioControl = new AudioControlPanel();
  //private Logger log = Logger.getLogger(TimelineBubbleEditor.class);
  protected UILogger uilogger;
  private static Logger logger = Logger.getLogger(TimelineControlPanel.class);

  protected Style normalStyle;
  ImageIcon icoBold = new ImageIcon(getClass().getClassLoader().getResource("resources/annotation/BOLD_1.GIF"));
  ImageIcon icoUnderline = new ImageIcon(getClass().getClassLoader().getResource("resources/annotation/UNDERLINE_1.GIF"));
  ImageIcon icoItalic = new ImageIcon(getClass().getClassLoader().getResource("resources/annotation/ITALIC_1.GIF"));
  ImageIcon icoRed = new ImageIcon(getClass().getClassLoader().getResource("resources/annotation/lineColorRed.GIF"));
  ImageIcon icoGreen = new ImageIcon(getClass().getClassLoader().getResource("resources/annotation/lineColorGreen2.gif"));
  ImageIcon icoBlue = new ImageIcon(getClass().getClassLoader().getResource("resources/annotation/lineColorBlue.GIF"));
  ImageIcon icoBlack = new ImageIcon(getClass().getClassLoader().getResource("resources/annotation/lineColorBlack.GIF"));
  ImageIcon icoYellow = new ImageIcon(getClass().getClassLoader().getResource("resources/annotation/lineColorYellow.GIF"));
  ImageIcon icoOrange = new ImageIcon(getClass().getClassLoader().getResource("resources/annotation/lineColorOrange.GIF"));
  ImageIcon icoGray = new ImageIcon(getClass().getClassLoader().getResource("resources/annotation/lineColorGray.GIF"));
  ImageIcon icoPink = new ImageIcon(getClass().getClassLoader().getResource("resources/annotation/lineColorPink.gif"));
  ImageIcon icoCyan = new ImageIcon(getClass().getClassLoader().getResource("resources/annotation/lineColorCyan.gif"));
  ImageIcon icoMagenta = new ImageIcon(getClass().getClassLoader().getResource("resources/annotation/lineColorMagenta.gif"));

  // vectors for storing temporary values
  protected Vector<Integer> editedBubbles = new Vector<>();
  protected Vector<String> potentialLabels = new Vector<>();
  protected Vector<String> potentialAnnotations = new Vector<>();
  protected Vector<String> oldLabels = new Vector<>();
  protected Vector<String> oldAnnotations = new Vector<>();

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

  // text fields and panes
  protected JTextField fldBubbleLabel = new JTextField();
  protected JTextPane tpAnnotation = new JTextPane();
  protected JScrollPane scrpAnnotation = new JScrollPane(tpAnnotation);

  // text editing
  protected AbstractDocument doc; 
  EditorKit kit = tpAnnotation.getEditorKit();
  HTMLEditorKit htmlKit = new HTMLEditorKit();
  StringWriter output = new StringWriter();
  
  // fonts
  private java.awt.Font timelineFont;
  private java.awt.Font unicodeFont = UIUtilities.fontUnicodeBigger;
	int annotationFontSize = UIUtilities.convertFontSize(18);

  // icons
  //final static ImageIcon icoUp = UIUtilities.icoUpSmall;
  //final static ImageIcon icoDown = UIUtilities.icoDownSmall;
  //final static ImageIcon icoLeft = UIUtilities.icoLeftSmall;
  //final static ImageIcon icoRight = UIUtilities.icoRightSmall;
  //final static ImageIcon icoDivider = UIUtilities.icoMediaDivider;
  final ImageIcon icoUp = new ImageIcon(getClass().getClassLoader().getResource("resources/media/moveupSmall.gif"));
  final ImageIcon icoDown = new ImageIcon(getClass().getClassLoader().getResource("resources/media/movedownSmall.gif"));
  final ImageIcon icoLeft = new ImageIcon(getClass().getClassLoader().getResource("resources/media/moveleftSmall.gif"));
  final ImageIcon icoRight = new ImageIcon(getClass().getClassLoader().getResource("resources/media/moverightSmall.gif"));
  final ImageIcon icoDivider = new ImageIcon(getClass().getClassLoader().getResource("resources/media/divider.gif"));
  protected JLabel lblDivider = new JLabel(icoDivider);

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
  int key;
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
    //tPlayer = pnlTimeline.getPlayer();
    //tLocalPlayer = pnlTimeline.getLocalPlayer();
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
    this.setLocation(30, 30); // frmTimeline.getWidth() - (this.getWidth() + 5), 30);

    // text fields and panes
    fldBubbleLabel.setBounds(new Rectangle(250, 20));
    fldBubbleLabel.setMinimumSize(new Dimension(250, 25));
    fldBubbleLabel.setPreferredSize(new Dimension(250, 25));
    fldBubbleLabel.setFont(unicodeFont);
    fldBubbleLabel.setToolTipText("Edit the bubble label");
    tpAnnotation.setPreferredSize(new Dimension(430, 375));
    tpAnnotation.setEditorKit(htmlKit); // NEW
    tpAnnotation.setContentType("text/html");
    tpAnnotation.setToolTipText("Edit the bubble annotation");
    ((AbstractDocument) tpAnnotation.getDocument()).setDocumentFilter(new HtmlLineBreakDocumentFilter());

    tpAnnotation.addKeyListener(new java.awt.event.KeyAdapter() {
        public void keyPressed(java.awt.event.KeyEvent evt) {
            key = evt.getKeyCode();
            //logger.debug("key pressed");
            StyledDocument styledDoc = tpAnnotation.getStyledDocument();
            try {
            if (styledDoc.getLength()<3) {
                //logger.debug("empty");
                if(key == KeyEvent.VK_BACK_SPACE)
                {  
                    evt.consume();
                    Robot robot = new Robot();
                    robot.keyPress(KeyEvent.VK_RIGHT);
                }
            }
            } catch (Exception e) {}
        }
    });
    tpAnnotation.setMargin(new Insets(5,5,5,5));
    StyledDocument styledDoc = tpAnnotation.getStyledDocument();
    if (styledDoc instanceof AbstractDocument) {
        doc = (AbstractDocument)styledDoc;
    } else {
     }
	
    // menu bar
    JMenu styleMenu = createStyleMenu();
    //JMenu sizeMenu = createSizeMenu();
    JMenu colorMenu = createColorMenu();
    JMenuBar mb = new JMenuBar();
    mb.add(styleMenu);
    //mb.add(sizeMenu);
    mb.add(colorMenu);
    setJMenuBar(mb);

    // media buttons
    if (timeline.playerIsPlaying()) {
      pnlAudioControl.btnPlay.setIcon(pnlControl.sPause);
    }
    else {
      pnlAudioControl.btnPlay.setIcon(pnlControl.sPlay);
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

    pnlAudioControl.btnStop.setIcon(pnlControl.sStop);
    pnlAudioControl.btnStop.setToolTipText("Stop");
    pnlAudioControl.btnStop.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        pnlControl.btn_stopAction();
        uilogger.log(UIEventType.BUTTON_CLICKED, "stop");
      }
    });

    pnlAudioControl.btnRW.setToolTipText("Press and hold for RW");
    pnlAudioControl.btnRW.setIcon(pnlControl.sRW);
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
    pnlAudioControl.btnFF.setIcon(pnlControl.sFF);
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
 //   btnUp.setPreferredSize(new Dimension(buttonWidth+5, 18));
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
        
        // do an apply"
       // saveLabelAndAnnotation();
       // doApply();
        
        uilogger.log(UIEventType.BUTTON_CLICKED, "navigate to upper bubble");
      }
    });

  //  btnDown.setPreferredSize(new Dimension(buttonWidth+5, 18));
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

        // do an apply"
       // saveLabelAndAnnotation();
       // doApply();
        
        uilogger.log(UIEventType.BUTTON_CLICKED, "navigate to lower bubble");
      }
    });

  //  btnLeft.setPreferredSize(new Dimension(buttonWidth-10, 30));
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
       
        // do an apply"
       // saveLabelAndAnnotation();
       // doApply();
        
         uilogger.log(UIEventType.BUTTON_CLICKED, "navigate to left bubble");
      }
    });

//    btnRight.setPreferredSize(new Dimension(buttonWidth-10, 30));
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
        
        // do an apply"
        //saveLabelAndAnnotation();
        //doApply();
        
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
        undoPreviousApplys();
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
    if (timeline.getSelectedBubbles() != null) {
      currentBubbleNum = ((Integer)(timeline.getSelectedBubbles().elementAt(0))).intValue();
    }
    currNode = timeline.getBubbleNode(currentBubbleNum);
    updateLabelAndAnnotation();

    // update navigation buttons
    updateNavigationButtons();

    // show dialog
    this.pack();
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
      //potentialAnnotations.addElement(tpAnnotation.getText());
      try {     
    	  output.getBuffer().setLength(0);
       	  htmlKit.write( output, doc, 0, doc.getLength());
       	  String html = output.toString();
       	 // log.debug("html = " + html);
    	  html = UIUtilities.htmlCleanup(html);
    	  potentialAnnotations.addElement(html);

      } catch (Exception e) {
          System.err.println("Error saving annotation");
      }
    }
    else {
      editedBubbles.setElementAt(Integer.valueOf(currentBubbleNum), prevSave);
      potentialLabels.setElementAt(fldBubbleLabel.getText(), prevSave);
      //potentialAnnotations.setElementAt(tpAnnotation.getText(), prevSave);
      try {      
    	  output.getBuffer().setLength(0);
    	  htmlKit.write( output, doc, 0, doc.getLength());
    	  String html = "";
    	  html = output.toString();
    	  html = UIUtilities.htmlCleanup(html);
   	     // log.debug("html = " + html);
    	  potentialAnnotations.setElementAt(html, prevSave);
    	  
      } catch (Exception e) {
          System.err.println("Error saving annotation");
          
      }

    }
  }

  /**
   * Executes an apply (for navigational buttons)
   */
  
  private void doApply() {
  	
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
      frmTimeline.getControlPanel().updateAnnotationPane();
      pnlTimeline.undoManager.undoableEditHappened(new UndoableEditEvent(pnlTimeline,
          new UndoableEditBubble(oldLabels, potentialLabels, oldAnnotations, potentialAnnotations,
          editedBubbles, timeline)));
      pnlTimeline.updateUndoMenu();
      uilogger.log(UIEventType.BUTTON_CLICKED, "apply bubble edits");

  }
  

  /**
   * updates the displayed label and annotation
   */
  private void updateLabelAndAnnotation() {
    
    currentBubbleNum = timeline.topBubbleNode.getPreOrderIndex(currNode);
    Bubble currBubble = timeline.getBubble(currentBubbleNum);
    //timeline.repositionHead(currentBubbleNum);
    int prevPos = editedBubbles.indexOf(Integer.valueOf(currentBubbleNum));
    if (prevPos != -1) { // this bubble has already been edited
      fldBubbleLabel.setText((String)potentialLabels.elementAt(prevPos));
      //tpAnnotation.setText((String)potentialAnnotations.elementAt(prevPos));
      Font f = new Font("Arial Unicode MS", Font.PLAIN, 20);
      tpAnnotation.setFont(f);
      //MutableAttributeSet attrs = tpAnnotation.getInputAttributes();
      //StyleConstants.setFontFamily(attrs, f.getFamily());
      //StyleConstants.setFontSize(attrs, f.getSize());
      //StyleConstants.setItalic(attrs, (f.getStyle() & Font.ITALIC) != 0);
      //StyleConstants.setBold(attrs, (f.getStyle() & Font.BOLD) != 0);
      //StyledDocument doc = tpAnnotation.getStyledDocument();  
    //  doc.setCharacterAttributes(0, doc.getLength() + 1, attrs, false);
  tpAnnotation.setText("<html><body><span style='margin-bottom:0em; font-size: " + annotationFontSize + "pt; font-family: Arial Unicode MS'>" + (String)potentialAnnotations.elementAt(prevPos) + "&#8203;</span></body></html>");
 // doc.putProperty(DefaultEditorKit.EndOfLineStringProperty, "<br/>");
  }
    else { // it has not been edited
        //tpAnnotation.setText(currBubble.getAnnotation());
        fldBubbleLabel.setText(currBubble.getLabel());
        Font f = new Font("Arial Unicode MS", Font.PLAIN, 20);
        tpAnnotation.setFont(f); 
        //MutableAttributeSet attrs = tpAnnotation.getInputAttributes();
        //StyleConstants.setFontFamily(attrs, f.getFamily());
        //StyleConstants.setFontSize(attrs, f.getSize());
        //StyleConstants.setItalic(attrs, (f.getStyle() & Font.ITALIC) != 0);
        //StyleConstants.setBold(attrs, (f.getStyle() & Font.BOLD) != 0);
        //StyledDocument doc = tpAnnotation.getStyledDocument();  
    //    doc.setCharacterAttributes(0, doc.getLength() + 1, attrs, false);
        tpAnnotation.setText("<html><body><span style='margin-bottom:0em; font-size: " + annotationFontSize + "pt; font-family: Arial Unicode MS'>" + currBubble.getAnnotation() + "&#8203;</span></body></html>");
        
        // doc.putProperty(DefaultEditorKit.EndOfLineStringProperty, "<br/>");

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
    fldBubbleLabel.requestFocus();
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
  
  // text editing functions
  
  protected JMenu createSizeMenu() {
      JMenu menu = new JMenu("Size");

      menu.add(new StyledEditorKit.FontSizeAction("12", 12));
      menu.add(new StyledEditorKit.FontSizeAction("14", 14));
      menu.add(new StyledEditorKit.FontSizeAction("18", 18));
      menu.add(new StyledEditorKit.FontSizeAction("24", 24));

      return menu;
  }
  
  protected JMenu createStyleMenu() {
      JMenu menu = new JMenu("Style");

      Action action = new StyledEditorKit.BoldAction();
      action.putValue(Action.NAME, "Bold");
      menu.add(action);
      menu.getItem(0).setIcon(icoBold);

      action = new StyledEditorKit.ItalicAction();
      action.putValue(Action.NAME, "Italic");
      menu.add(action);
      menu.getItem(1).setIcon(icoItalic);

      action = new StyledEditorKit.UnderlineAction();
      action.putValue(Action.NAME, "Underline");
      menu.add(action);
      menu.getItem(2).setIcon(icoUnderline);

      menu.addSeparator();
      
      JMenuItem menuiReturn = new JMenuItem();
      // menu.add(menuiReturn); // don't need this feature now!
      menuiReturn.setText("Hard Return");
      menuiReturn.addActionListener(new java.awt.event.ActionListener() {
          public void actionPerformed(ActionEvent e) {
             try {
             doc.insertString(tpAnnotation.getCaretPosition(), "\r\n", null);
             } catch (Exception ex) {}
          }
      });

      //menu.add(new StyledEditorKit.FontFamilyAction("Serif", "Serif"));
      //menu.add(new StyledEditorKit.FontFamilyAction("SansSerif", "SansSerif"));

      if (System.getProperty("os.name").startsWith("Mac OS")) {
          //Mac specific stuff
          menu.getItem(0).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, InputEvent.META_DOWN_MASK));
          menu.getItem(1).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.META_DOWN_MASK));
          menu.getItem(2).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.META_DOWN_MASK));
         // menu.getItem(4).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.META_DOWN_MASK));
      } else {
          //Windows specific stuff
          menu.getItem(0).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, InputEvent.CTRL_DOWN_MASK));
          menu.getItem(1).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_DOWN_MASK));
          menu.getItem(2).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.CTRL_DOWN_MASK));
          //menu.getItem(4).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.SHIFT_DOWN_MASK));
          menu.setMnemonic('f');
          menu.getItem(0).setMnemonic('b');
          menu.getItem(1).setMnemonic('i');
          menu.getItem(2).setMnemonic('u');
         // menu.getItem(4).setMnemonic('r');

      }

      return menu;
  }

  protected JMenu createColorMenu() {
      JMenu menu = new JMenu("Color");

      menu.add(new StyledEditorKit.ForegroundAction("Red", Color.red));
      menu.getItem(0).setIcon(icoRed);
      
      menu.add(new StyledEditorKit.ForegroundAction("Green", Color.green));
      menu.getItem(1).setIcon(icoGreen);
      
      menu.add(new StyledEditorKit.ForegroundAction("Blue", Color.blue));
      menu.getItem(2).setIcon(icoBlue);
      
      menu.add(new StyledEditorKit.ForegroundAction("Yellow", Color.yellow));
      menu.getItem(3).setIcon(icoYellow);

       menu.add(new StyledEditorKit.ForegroundAction("Orange", Color.orange));
       menu.getItem(4).setIcon(icoOrange);

       menu.add(new StyledEditorKit.ForegroundAction("Pink", Color.pink));
       menu.getItem(5).setIcon(icoPink);

       menu.add(new StyledEditorKit.ForegroundAction("Cyan", Color.cyan));
       menu.getItem(6).setIcon(icoCyan);

       menu.add(new StyledEditorKit.ForegroundAction("Magenta", Color.magenta));
       menu.getItem(7).setIcon(icoMagenta);

       menu.add(new StyledEditorKit.ForegroundAction("Gray", Color.darkGray));
       menu.getItem(8).setIcon(icoGray);

     menu.add(new StyledEditorKit.ForegroundAction("Black", Color.black));
       menu.getItem(9).setIcon(icoBlack);

      return menu;
  }

  }

