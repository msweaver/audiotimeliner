package ui.timeliner;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.awt.*;
import java.util.*;
import java.awt.event.*;
import com.borland.jbcl.layout.*;
import ui.media.*;
import ui.common.*;
import util.logging.*;
//import org.apache.log4j.Logger;
import javax.swing.text.html.*;

/**
 * Timeline Control Panel
 * This panel contains a default user interface for the basic functions that are used
 * to manipulate a timeline.
 *
 * @author Brent Yorgason
 */

public class TimelineControlPanel extends JPanel {
  private static final long serialVersionUID = 1L;
// external components
  private TimelinePanel pnlTimeline;
  private Timeline timeline;
  private TimelineFrame frmTimeline;
  private TimelineMenuBar menubTimeline;
  public JScrollPane treeView;
  private TimelinePlayer tPlayer;
  private TimelineLocalPlayer tLocalPlayer;
  protected MarkerEditor dlgMarkerEditor;
  protected TimepointEditor dlgTimepointEditor;
  protected AudioControlPanel pnlAudioControl = new AudioControlPanel();
  VolumePanel pnlVolumeControl = new VolumePanel();
  //private static Logger log = Logger.getLogger(TimelineControlPanel.class);
  protected UILogger uilogger;

  // fonts
  protected java.awt.Font timelineFont;
  private java.awt.Font annotationFont;
  protected String unicodeFont = "Arial Unicode MS";
  private int annotationFontSize = 13;

  // control panel buttons
  protected JButton btnAddTimepoint;
  protected JButton btnAddMarker;
  protected JButton btnEditTimepointOrMarker;
  protected JButton btnDeleteTimepointOrMarker;
  protected JButton btnEditBubble;
  protected JButton btnChangeColor;
  protected JButton btnDeleteBubble;
  protected JButton btnZoomTo;
  protected JButton btnGroupBubbles;
  protected JButton btnUngroupBubbles;
  protected JButton btnClearAll;
  protected JButton btnEditProperties;
  protected JButton btnFitToWindow;
  protected JButton btnFontLarger;
  protected JButton btnFontSmaller;
  protected JLabel lblElapsed;
  protected JLabel lblStatus = new JLabel();
  protected JLabel lblDuration = new JLabel();
  protected JLabel lblAnnotations = new JLabel("Annotations");
  protected JRadioButton radAllLevels;
  protected JRadioButton radSelectedLevels;
  protected JCheckBox chkShowMarkers;
  protected ButtonGroup grpShowLevels = new ButtonGroup();
  protected final JSlider slideVolume = pnlVolumeControl.slideVolume;

  // icons
  final static ImageIcon icoEdit = UIUtilities.icoEdit;
  final static ImageIcon icoAdd = UIUtilities.icoAdd;
  final static ImageIcon icoAddMarker = UIUtilities.icoAddMarker;
  final static ImageIcon icoBigger = UIUtilities.icoBigger;
  final static ImageIcon icoSmaller = UIUtilities.icoSmaller;

  // layout elements
  private JPanel pnlStatus = new JPanel();
  private JPanel pnlDuration = new JPanel();
  private JPanel pnlPlayback = new JPanel();
  private JPanel pnlElapsedVolume = new JPanel();
  private JPanel pnlBubbleButtons = new JPanel();
  private JPanel pnlTimepointButtons = new JPanel();
  private JPanel pnlTimelineButtons = new JPanel();
  private JPanel pnlAnnotations = new JPanel();
  private JPanel pnlAnnotationTools = new JPanel(); 
  private JPanel pnlLevels = new JPanel();
  private JPanel pnlMarkers = new JPanel();
  private JPanel pnlShow = new JPanel();
  private JPanel pnlFontButtons = new JPanel();
  private TitledBorder titledBorderPlayback;
  private TitledBorder titledBorderBubbles;
  private TitledBorder titledBorderTimepoints;
  private TitledBorder titledBorderTimeline;
  private TitledBorder titledBorderAnnotations;
  private TitledBorder titledBorderLevels;
  private TitledBorder titledBorderShow;

  // media variables
  protected boolean playing = false;
  protected boolean buffering = false;
  protected boolean muted = false;
  protected final static int VOLUME_INCREMENTS = 200;
  protected float vol = 1f;
  protected final static int TIMER_FIRE_FREQUENCY = 80;
  protected boolean timerDisable = false;
  protected boolean timerCanceled = false;

  //tracking variables
  protected final static int INITIAL_TRACKING_AMOUNT = 150;
  protected int shiftAmount = INITIAL_TRACKING_AMOUNT;
  protected final static int TRACKING_NONE = 0;
  protected final static int TRACKING_FF = 1;
  protected final static int TRACKING_RW = -1;
  protected int trackingState = TRACKING_NONE;

  // status messages
  final static String STATUS_INITIALIZING = "Status: Initializing Timeliner";
  final static String STATUS_BUFFERING = "Status: Buffering Stream";
  final static String STATUS_PLAYING = "Status: Playing Content";
  final static String STATUS_IDLE = "Status: Idle";
  final static String STATUS_TRACKING = "Status: Adjusting Playback Position";
  final static String STATUS_STREAM_ERROR = "Status: Stream Error";
  final static String STATUS_STREAM_NOT_FOUND = "Status: Stream Not Found";

  // annotation pane
  protected JTextPane tpAnnotations = new JTextPane();
  protected JScrollPane scrpAnnotations = new JScrollPane(tpAnnotations);
  protected boolean showAllAnnotations = true;
  protected StyledDocument doc;
  protected Style selectedStyle;
  protected Style normalStyle;
  protected Style boldStyle;
  protected boolean isDescriptionShowing = false;
  protected int timelineFontSize;

  // other variables
  protected int height;

  /**
   * constructor
   * recieves a timeline frame, initial width, and initial height
   */
  public TimelineControlPanel(TimelineFrame tf, int initWidth, int initHeight) {
    frmTimeline = tf;
    pnlTimeline = frmTimeline.getTimelinePanel();
    height = initHeight;
    this.setMinimumSize(new Dimension(initWidth, height));
    this.setPreferredSize(new Dimension(initWidth, height));
    uilogger = frmTimeline.getUILogger();

    try {
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
    this.disableAllControls();
  }

  /**
   * jbInit: Initializes the panel layout, then creates and adds the buttons and timepoint list
   */
  private void jbInit() throws Exception {

    // set font
    if (System.getProperty("os.name").startsWith("Mac OS")) {
      timelineFont = UIUtilities.fontDialogMacSmallest;
      timelineFontSize = 10;
      annotationFont = new Font(unicodeFont, 0, annotationFontSize - 2);
    } else {
      timelineFont = UIUtilities.fontDialogWin;
      timelineFontSize = 11;
      annotationFont = new Font(unicodeFont, 0, annotationFontSize);
    }

    // set panel layout models
    GridBagLayout gridbagMain = new GridBagLayout();
    GridBagLayout gridBagBubbles = new GridBagLayout();
    GridBagLayout gridBagTimepoints = new GridBagLayout();
    GridBagLayout gridBagTimeline = new GridBagLayout();
    GridBagLayout gridBagAnnotations = new GridBagLayout();
    GridBagLayout gridBagElapsed = new GridBagLayout();
    this.setLayout(gridbagMain);
    pnlStatus.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 1));
    pnlDuration.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 1));
    pnlPlayback.setLayout(new VerticalFlowLayout());
    pnlAudioControl.flowLay.setVgap(1);
    pnlAudioControl.setBorder(null);
    pnlAudioControl.setMinimumSize(null);
    pnlAudioControl.setPreferredSize(null);
    pnlElapsedVolume.setLayout(gridBagElapsed);
    pnlBubbleButtons.setLayout(gridBagBubbles);
    pnlTimepointButtons.setLayout(gridBagTimepoints);
    pnlTimelineButtons.setLayout(gridBagTimeline);
    pnlAnnotations.setLayout(gridBagAnnotations);
    pnlAnnotationTools.setLayout(new FlowLayout());
    pnlLevels.setLayout(new BorderLayout());
    pnlMarkers.setLayout(new BorderLayout());
    pnlShow.setLayout(new GridLayout());
    pnlFontButtons.setLayout(new FlowLayout(FlowLayout.CENTER));

    // set up annotation pane
    tpAnnotations.setEditable(false);
    tpAnnotations.setFont(annotationFont);
    scrpAnnotations.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

    // create panel borders
    titledBorderPlayback = new TitledBorder(" Playback ");
    titledBorderTimepoints = new TitledBorder(" Timepoints and Markers ");
    titledBorderTimeline = new TitledBorder(" Timeline ");
    titledBorderBubbles = new TitledBorder(" Bubbles ");
    titledBorderAnnotations = new TitledBorder(" Annotations ");
    titledBorderShow = new TitledBorder(" Show Levels ");
    titledBorderLevels = new TitledBorder(" Levels: ");
    titledBorderPlayback.setTitleFont(timelineFont);
    titledBorderShow.setTitleFont(timelineFont);
    titledBorderTimepoints.setTitleFont(timelineFont);
    titledBorderTimeline.setTitleFont(timelineFont);
    titledBorderBubbles.setTitleFont(timelineFont);
    titledBorderAnnotations.setTitleFont(timelineFont);
    titledBorderLevels.setTitleFont(timelineFont);
    pnlPlayback.setBorder(titledBorderPlayback);
    pnlTimepointButtons.setBorder(titledBorderTimepoints);
    pnlTimelineButtons.setBorder(titledBorderTimeline);
    pnlBubbleButtons.setBorder(titledBorderBubbles);
    pnlAnnotations.setBorder(titledBorderAnnotations);
    pnlShow.setBorder(titledBorderShow);

    // set up status bar
    lblStatus.setFont(timelineFont);
    lblStatus.setText(STATUS_INITIALIZING);
    lblDuration.setFont(timelineFont);
    lblDuration.setText("");
    lblAnnotations.setFont(timelineFont);
    pnlStatus.setBorder(BorderFactory.createLoweredBevelBorder());
    pnlDuration.setBorder(BorderFactory.createLoweredBevelBorder());
    pnlStatus.add(lblStatus, null);
    pnlDuration.add(lblDuration, null);

    // add panels to inner panels
    pnlPlayback.add(pnlAudioControl);
    pnlPlayback.add(pnlElapsedVolume, null);
    
//    pnlAnnotationTools.add(pnlFontButtons);
  //  pnlAnnotationTools.add(pnlShow);
    //pnlAnnotationTools.add(pnlMarkers);
    
    // set up grid bag layout constraints and add inner panels to the main panel
    TimelineUtilities.createConstraints(this, pnlPlayback, 0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, 2, 2, 2, 2, 0, 0);
    TimelineUtilities.createConstraints(this, pnlTimelineButtons, 0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, 2, 2, 2, 2, 0, 0);
    TimelineUtilities.createConstraints(this, pnlTimepointButtons, 1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, 2, 2, 2, 2, 0, 0);
    TimelineUtilities.createConstraints(this, pnlBubbleButtons, 1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, 2, 2, 2, 2, 0, 0);
    TimelineUtilities.createConstraints(this, pnlAnnotations, 2, 0, 1, 3, 1.0, 0.8, GridBagConstraints.CENTER, GridBagConstraints.BOTH, 2, 2, 2, 2, 0, 0);
    TimelineUtilities.createConstraints(this, pnlAnnotationTools, 0, 2, 2, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, 2, 2, 2, 2, 0, 0);
    TimelineUtilities.createConstraints(this, pnlStatus, 0, 3, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, 2, 2, 2, 2, 0, 0);
    TimelineUtilities.createConstraints(this, pnlDuration, 2, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, 2, 2, 2, 2, 0, 0);

    // create controls
    createPlaybackControls();
    createTimelinerButtons();

    // disable selection buttons
    this.disableAllBubbleControls();
    this.disableSelectedTimepointControls();
    this.doPlayerDisable();

    // change cursor listener
    this.addMouseListener(new MouseAdapter() {
      public void mouseEntered (MouseEvent e) {
        frmTimeline.setCursor(Cursor.getDefaultCursor());
      }
    });
  }

  /**
   * createPlaybackControls: adds playback controls to the control panel and adds
   * listeners to each control
   */
  private void createPlaybackControls() {

    // play button
    pnlAudioControl.btnPlay.setIcon(UIUtilities.icoPlay);
    setEnterAction(pnlAudioControl.btnPlay);
    pnlAudioControl.btnPlay.setToolTipText("Play");
    pnlAudioControl.btnPlay.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (pnlTimeline.getTimeline().playerIsPlaying()) {
          btn_pauseAction();
          uilogger.log(UIEventType.BUTTON_CLICKED, "pause");
        }
        else {
          btn_playAction();
          uilogger.log(UIEventType.BUTTON_CLICKED, "play");
        }
      }
    });

    // "stop" button
    pnlAudioControl.btnStop.setIcon(UIUtilities.icoStop);
    pnlAudioControl.btnStop.setToolTipText("Stop");
    setEnterAction(pnlAudioControl.btnStop);
    pnlAudioControl.btnStop.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        btn_stopAction();
        uilogger.log(UIEventType.BUTTON_CLICKED, "stop");
      }
    });

    // "previous" button
    pnlAudioControl.btnPrev.setToolTipText("Previous Timepoint");
    pnlAudioControl.btnPrev.setIcon(UIUtilities.icoPrev);
    setEnterAction(pnlAudioControl.btnPrev);
    pnlAudioControl.btnPrev.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        pnlTimeline.goToPreviousBubble();
        updateAnnotationPane();
        uilogger.log(UIEventType.BUTTON_CLICKED, "previous timepoint");
      }
    });

    // "rewind" button
    pnlAudioControl.btnRW.setToolTipText("Press and hold for RW");
    pnlAudioControl.btnRW.setIcon(UIUtilities.icoRW);
    setEnterAction(pnlAudioControl.btnRW);
    pnlAudioControl.btnRW.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        doRWTracking();
      }
      public void mouseReleased(MouseEvent e) {
        doStopTracking();
        updateAnnotationPane();
        uilogger.log(UIEventType.BUTTON_CLICKED, "rewind");
      }
    });

    // "forward" button
    pnlAudioControl.btnFF.setToolTipText("Press and hold for FF");
    pnlAudioControl.btnFF.setIcon(UIUtilities.icoFF);
    setEnterAction(pnlAudioControl.btnFF);
    pnlAudioControl.btnFF.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        doFFTracking();
      }
      public void mouseReleased(MouseEvent e) {
        doStopTracking();
        updateAnnotationPane();
        uilogger.log(UIEventType.BUTTON_CLICKED, "fast forward");
      }
    });

    // "next" button
    pnlAudioControl.btnNext.setToolTipText("Next Timepoint");
    pnlAudioControl.btnNext.setIcon(UIUtilities.icoNext);
    setEnterAction(pnlAudioControl.btnNext);
    pnlAudioControl.btnNext.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        pnlTimeline.goToNextBubble();
        updateAnnotationPane();
        uilogger.log(UIEventType.BUTTON_CLICKED, "next timepoint");
      }
    });

    // "mute" button
    setEnterAction(pnlVolumeControl.btnMute);
    pnlVolumeControl.btnMute.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (muted) {
          uilogger.log(UIEventType.BUTTON_CLICKED,"unmute");
        } else {
          uilogger.log(UIEventType.BUTTON_CLICKED,"mute");
        }
        doMute();
      }
    });

    // elapsed time
    lblElapsed = new JLabel("00:00 / 00:00");
    lblElapsed.setFont(timelineFont);

    // volume slider
    slideVolume.setMinimum(0);
    setEnterAction(slideVolume);
    slideVolume.setMaximum(TimelineControlPanel.VOLUME_INCREMENTS);
    slideVolume.setValue(TimelineControlPanel.VOLUME_INCREMENTS);
    slideVolume.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        vol = ((float)slideVolume.getValue()) / ((float)VOLUME_INCREMENTS);
        doVolumeUpdate();
        uilogger.log(UIEventType.SLIDER_ADJUSTED);
      }
    });
  }

  /**
   * play action
   */
  protected void btn_playAction() {
    WindowManager.stopAllPlayers();
    pnlTimeline.getTimeline().startPlayer();
    pnlTimeline.getTimeline().showTime(false);
    lblStatus.setText(TimelineControlPanel.STATUS_BUFFERING);
    buffering = false;
    playing = true;
    pnlAudioControl.btnPlay.setIcon(UIUtilities.icoPause);
    pnlTimeline.enableAddTimepoint();
    pnlTimeline.enableAddMarker();
    if (pnlTimeline.btnBubbleEditorPlay != null) {
      pnlTimeline.btnBubbleEditorPlay.setIcon(UIUtilities.icoPause);
    }
  }

  /**
   * pause action
   */
  protected void btn_pauseAction() {
    pnlTimeline.getTimeline().pausePlayer();
    pnlAudioControl.btnPlay.setIcon(UIUtilities.icoPlay);
    lblStatus.setText(STATUS_IDLE);
    buffering = false;
    playing = false;
    pnlTimeline.scheduleRefresh();
    if (pnlTimeline.btnBubbleEditorPlay != null) {
      pnlTimeline.btnBubbleEditorPlay.setIcon(UIUtilities.icoPlay);
    }
  }

  // stop action
  protected void btn_stopAction() {
    timeline = pnlTimeline.getTimeline();
    timeline.stopPlayer();
    timeline.setPlayerOffset(timeline.getLocalStartOffset());
    pnlAudioControl.btnPlay.setIcon(UIUtilities.icoPlay);
    lblStatus.setText(STATUS_IDLE);
    pnlTimeline.scheduleRefresh();
    timeline.getSlider().setValue(timeline.getLocalStartOffset());
    timeline.showTime(false);
    updateAnnotationPane();
    if (pnlTimeline.btnBubbleEditorPlay != null) {
      pnlTimeline.btnBubbleEditorPlay.setIcon(UIUtilities.icoPlay);
    }

    timeline.setNextImportantOffset(Math.min(timeline.getSortedPointList()[1], timeline.getMarkerList()[0]));
  }

  /**
   * createTimelinerButtons: adds buttons to the control panel and adds listeners to each button
   */
  private void createTimelinerButtons() {

    // "add timepoint" button
    btnAddTimepoint = new JButton("Add");
    btnAddTimepoint.setIcon(icoAdd);
    btnAddTimepoint.setFont(timelineFont);
    setEnterAction(btnAddTimepoint);
    btnAddTimepoint.setToolTipText("Create a bubble by adding a timepoint at the current location");
    if (System.getProperty("os.name").startsWith("Mac OS")) {
      btnAddTimepoint.setMinimumSize(new Dimension(85, 23));
      btnAddTimepoint.setPreferredSize(new Dimension(85, 23));
    } else {
      btnAddTimepoint.setMinimumSize(new Dimension(80, 23));//(133, 23));
      btnAddTimepoint.setPreferredSize(new Dimension(80, 23));//(133, 23));
    }
    btnAddTimepoint.setMargin(new Insets(0, 0, 0, 0));
    btnAddTimepoint.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        pnlTimeline.addTimepoint();
        uilogger.log(UIEventType.BUTTON_CLICKED, "add timepoint");
      }
    });
    btnAddTimepoint.setEnabled(true); // add timepoint always enabled

    // "add marker" button
    btnAddMarker = new JButton("Mark");
    btnAddMarker.setIcon(icoAddMarker);
    btnAddMarker.setFont(timelineFont);
    setEnterAction(btnAddMarker);
    btnAddMarker.setToolTipText("Mark an event at the current location");
    if (System.getProperty("os.name").startsWith("Mac OS")) {
      btnAddMarker.setMinimumSize(new Dimension(85, 23));
      btnAddMarker.setPreferredSize(new Dimension(85, 23));
    } else {
      btnAddMarker.setMinimumSize(new Dimension(80, 23));//(133, 23));
      btnAddMarker.setPreferredSize(new Dimension(80, 23));//(133, 23));
    }
    btnAddMarker.setMargin(new Insets(0, 0, 0, 0));
    btnAddMarker.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        pnlTimeline.addMarker();
        uilogger.log(UIEventType.BUTTON_CLICKED, "add marker");
      }
    });
    btnAddMarker.setEnabled(true); // add marker always enabled

    // "change bubble color" button
    btnChangeColor = new JButton("Color...");
    btnChangeColor.setFont(timelineFont);
    btnChangeColor.setToolTipText("Change the color of the selected bubble(s)");
    if (System.getProperty("os.name").startsWith("Mac OS")) {
      btnChangeColor.setMinimumSize(new Dimension(85, 23));
      btnChangeColor.setPreferredSize(new Dimension(85, 23));
    } else {
      btnChangeColor.setMinimumSize(new Dimension(80, 23));
      btnChangeColor.setPreferredSize(new Dimension(80, 23));
    }
    btnChangeColor.setMargin(new Insets(0, 0, 0, 0));
    setEnterAction(btnChangeColor);
    btnChangeColor.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        pnlTimeline.changeBubbleColor();
        uilogger.log(UIEventType.BUTTON_CLICKED, "change color of selected bubbles: "
                     + pnlTimeline.getTimeline().getSelectedBubbles());
      }
    });

    // "delete" bubble button
    btnDeleteBubble = new JButton("Delete");
    btnDeleteBubble.setFont(timelineFont);
    btnDeleteBubble.setToolTipText("Delete the selected bubble(s)");
    if (System.getProperty("os.name").startsWith("Mac OS")) {
      btnDeleteBubble.setMinimumSize(new Dimension(65, 23));
      btnDeleteBubble.setPreferredSize(new Dimension(65, 23));
    } else {
      btnDeleteBubble.setMinimumSize(new Dimension(55, 23));
      btnDeleteBubble.setPreferredSize(new Dimension(55, 23));
    }
    btnDeleteBubble.setMargin(new Insets(0, 0, 0, 0));
    setEnterAction(btnDeleteBubble);
    btnDeleteBubble.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        pnlTimeline.deleteSelectedBubbles();
        uilogger.log(UIEventType.BUTTON_CLICKED, "delete selected bubbles: "
                     + pnlTimeline.getTimeline().getSelectedBubbles());
      }
    });

    // "delete" timepoint or marker button
    btnDeleteTimepointOrMarker = new JButton("Delete");
    btnDeleteTimepointOrMarker.setFont(timelineFont);
    btnDeleteTimepointOrMarker.setToolTipText("Delete the selected timepoint or marker");
    if (System.getProperty("os.name").startsWith("Mac OS")) {
      btnDeleteTimepointOrMarker.setMinimumSize(new Dimension(65, 23));
      btnDeleteTimepointOrMarker.setPreferredSize(new Dimension(65, 23));
    } else {
      btnDeleteTimepointOrMarker.setMinimumSize(new Dimension(55, 23));
      btnDeleteTimepointOrMarker.setPreferredSize(new Dimension(55, 23));
    }
    btnDeleteTimepointOrMarker.setMargin(new Insets(0, 0, 0, 0));
    setEnterAction(btnDeleteTimepointOrMarker);
    btnDeleteTimepointOrMarker.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (pnlTimeline.getTimeline().areTimepointsSelected()) {
          pnlTimeline.deleteSelectedTimepoint();
          uilogger.log(UIEventType.BUTTON_CLICKED, "delete timepoint "
                       + pnlTimeline.getTimeline().getLastTimepointClicked());
        }
        else {
          pnlTimeline.deleteSelectedMarker();
          uilogger.log(UIEventType.BUTTON_CLICKED, "delete marker "
                       + pnlTimeline.getTimeline().getLastMarkerClicked());
        }
      }
    });

    // "edit properties" button
    btnEditProperties = new JButton("Edit Properties...");
    btnEditProperties.setFont(timelineFont);
    btnEditProperties.setToolTipText("Edit the timeline properties");
    if (System.getProperty("os.name").startsWith("Mac OS")) {
      btnEditProperties.setMinimumSize(new Dimension(120, 23));
      btnEditProperties.setPreferredSize(new Dimension(120, 23));
    } else {
      btnEditProperties.setMinimumSize(new Dimension(100, 23));
      btnEditProperties.setPreferredSize(new Dimension(100, 23));
    }
    btnEditProperties.setMargin(new Insets(0, 0, 0, 0));
    setEnterAction(btnEditProperties);
    btnEditProperties.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        pnlTimeline.openTimelineProperties();
        uilogger.log(UIEventType.BUTTON_CLICKED, "edit timeline properties");
      }
    });

    // "fit to window" button
    btnFitToWindow = new JButton("Fit to Window");
    btnFitToWindow.setFont(timelineFont);
    btnFitToWindow.setToolTipText("Fit the timeline to the current window size");
    if (System.getProperty("os.name").startsWith("Mac OS")) {
      btnFitToWindow.setMinimumSize(new Dimension(95, 23));
      btnFitToWindow.setPreferredSize(new Dimension(95, 23));
    } else {
      btnFitToWindow.setMinimumSize(new Dimension(82, 23));
      btnFitToWindow.setPreferredSize(new Dimension(82, 23));
    }
    btnFitToWindow.setMargin(new Insets(0, 0, 0, 0));
    setEnterAction(btnFitToWindow);
    btnFitToWindow.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        pnlTimeline.fitToWindow();
        uilogger.log(UIEventType.BUTTON_CLICKED, "fit timeline to window");
      }
    });

    // "group bubbles" button
    btnGroupBubbles = new JButton("Group");
    btnGroupBubbles.setFont(timelineFont);
    btnGroupBubbles.setToolTipText("Group the selected bubbles");
    if (System.getProperty("os.name").startsWith("Mac OS")) {
      btnGroupBubbles.setMinimumSize(new Dimension(65, 23));
      btnGroupBubbles.setPreferredSize(new Dimension(65, 23));
    } else {
      btnGroupBubbles.setMinimumSize(new Dimension(55, 23));
      btnGroupBubbles.setPreferredSize(new Dimension(55, 23));
    }
    btnGroupBubbles.setMargin(new Insets(0, 0, 0, 0));
    setEnterAction(btnGroupBubbles);
    btnGroupBubbles.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        pnlTimeline.groupSelectedBubbles();
        uilogger.log(UIEventType.BUTTON_CLICKED, "group selected bubbles: "
                     + pnlTimeline.getTimeline().getSelectedBubbles());
      }
    });

    // "clear all" button
    btnClearAll = new JButton("Clear All");
    btnClearAll.setFont(timelineFont);
    btnClearAll.setToolTipText("Clear all timepoints, markers, and timeline settings");

    if (System.getProperty("os.name").startsWith("Mac OS")) {
      btnClearAll.setMinimumSize(new Dimension(95, 23));
      btnClearAll.setPreferredSize(new Dimension(95, 23));
    } else {
      btnClearAll.setMinimumSize(new Dimension(82, 23));
      btnClearAll.setPreferredSize(new Dimension(82, 23));
    }
    btnClearAll.setMargin(new Insets(0, 0, 0, 0));
    setEnterAction(btnClearAll);
    btnClearAll.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        pnlTimeline.resetTimeline();
      }
    });


    // "edit..." bubble button
    btnEditBubble = new JButton("Edit..."); //("Label / Annotate...");
    btnEditBubble.setIcon(icoEdit);
    btnEditBubble.setFont(timelineFont);
    btnEditBubble.setToolTipText("Edit the label and annotation of the selected bubble");
    if (System.getProperty("os.name").startsWith("Mac OS")) {
      btnEditBubble.setMinimumSize(new Dimension(85, 23));
      btnEditBubble.setPreferredSize(new Dimension(85, 23));
    } else {
      btnEditBubble.setMinimumSize(new Dimension(80, 23)); //(80, 23));
      btnEditBubble.setPreferredSize(new Dimension(80, 23)); //(80, 23));
    }
    btnEditBubble.setMargin(new Insets(0, 0, 0, 0));
    setEnterAction(btnEditBubble);
    btnEditBubble.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (pnlTimeline.getTimeline() != null) {
          if (pnlTimeline.getTimeline().getSelectedBubbles().size() == 1) { // currenly can only edit one bubble at a time
            pnlTimeline.dlgBubbleEditor = new TimelineBubbleEditor(frmTimeline);
            uilogger.log(UIEventType.BUTTON_CLICKED, "edit bubble: " + pnlTimeline.getTimeline().getSelectedBubbles());
          }
        }
      }
    });

    // "edit label" timepoint button
    btnEditTimepointOrMarker = new JButton("Edit...");
    btnEditTimepointOrMarker.setFont(timelineFont);
    btnEditTimepointOrMarker.setToolTipText("Edit the selected timepoint or marker");
    if (System.getProperty("os.name").startsWith("Mac OS")) {
      btnEditTimepointOrMarker.setMinimumSize(new Dimension(65, 23));
      btnEditTimepointOrMarker.setPreferredSize(new Dimension(65, 23));
    } else {
      btnEditTimepointOrMarker.setMinimumSize(new Dimension(55, 23));
      btnEditTimepointOrMarker.setPreferredSize(new Dimension(55, 23));
    }
    btnEditTimepointOrMarker.setMargin(new Insets(0, 0, 0, 0));
    setEnterAction(btnEditTimepointOrMarker);
    btnEditTimepointOrMarker.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (pnlTimeline.getTimeline() != null) {
          if (pnlTimeline.getTimeline().areTimepointsSelected()) {
            dlgTimepointEditor = new TimepointEditor(frmTimeline);
            uilogger.log(UIEventType.BUTTON_CLICKED, "edit timepoint label: " + pnlTimeline.getTimeline().getLastTimepointClicked());
          }
          else if (pnlTimeline.getTimeline().areMarkersSelected()) {
            dlgMarkerEditor = new MarkerEditor(frmTimeline);
            uilogger.log(UIEventType.BUTTON_CLICKED, "edit marker: " + pnlTimeline.getTimeline().getLastMarkerClicked());
          }
        }
      }
    });

    // "ungroup bubbles" button
    btnUngroupBubbles = new JButton("Ungroup");
    btnUngroupBubbles.setFont(timelineFont);
    btnUngroupBubbles.setToolTipText("Ungroup the selected grouped bubbles");
    if (System.getProperty("os.name").startsWith("Mac OS")) {
      btnUngroupBubbles.setMinimumSize(new Dimension(80, 23));
      btnUngroupBubbles.setPreferredSize(new Dimension(80, 23));
    } else {
      btnUngroupBubbles.setMinimumSize(new Dimension(55, 23));
      btnUngroupBubbles.setPreferredSize(new Dimension(55, 23));
    }
    btnUngroupBubbles.setMargin(new Insets(0, 0, 0, 0));
    setEnterAction(btnUngroupBubbles);
    btnUngroupBubbles.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        pnlTimeline.ungroupSelectedBubbles();
        uilogger.log(UIEventType.BUTTON_CLICKED, "ungroup selected bubbles: " + pnlTimeline.getTimeline().getSelectedBubbles());
      }
    });

    // "zoom to selection" button
    btnZoomTo = new JButton("Zoom to Selection");
    btnZoomTo.setFont(timelineFont);
    btnZoomTo.setToolTipText("Zoom in to the selected bubble(s)");
    if (System.getProperty("os.name").startsWith("Mac OS")) {
      btnZoomTo.setMinimumSize(new Dimension(120, 23));
      btnZoomTo.setPreferredSize(new Dimension(120, 23));
    } else {
      btnZoomTo.setMinimumSize(new Dimension(100, 23));
      btnZoomTo.setPreferredSize(new Dimension(100, 23));
    }
    btnZoomTo.setMargin(new Insets(0, 0, 0, 0));
    setEnterAction(btnZoomTo);
    btnZoomTo.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (pnlTimeline.getTimeline() != null) {
          pnlTimeline.zoomToSelectedBubbles();
          uilogger.log(UIEventType.BUTTON_CLICKED, "zoom to selection: " + pnlTimeline.getTimeline().getSelectedBubbles());
        }
      }
    });

    // "larger font" button
    btnFontLarger = new JButton("");
    btnFontLarger.setIcon(icoBigger);
    btnFontLarger.setFont(timelineFont);
    btnFontLarger.setToolTipText("Make the annotation text larger");
    if (System.getProperty("os.name").startsWith("Mac OS")) {
      btnFontLarger.setMinimumSize(new Dimension(45, 23));
      btnFontLarger.setPreferredSize(new Dimension(45, 23));
    } else {
      btnFontLarger.setMinimumSize(new Dimension(40, 23));
      btnFontLarger.setPreferredSize(new Dimension(40, 23));
    }
    btnFontLarger.setMargin(new Insets(0, 0, 0, 0));
    setEnterAction(btnFontLarger);
    btnFontLarger.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (pnlTimeline.getTimeline() != null) {
          annotationFontSize = annotationFontSize + 2;
          if (System.getProperty("os.name").startsWith("Mac OS")) {
            annotationFont = new Font(unicodeFont, 0, annotationFontSize - 2);
            Style fontStyle = doc.addStyle("FontSize", null);
            StyleConstants.setFontSize(fontStyle, annotationFontSize - 2);
            doc.setParagraphAttributes(0, doc.getLength(), fontStyle, false);
          } else {
            annotationFont = new Font(unicodeFont, Font.PLAIN, annotationFontSize);
            Style fontStyle = doc.addStyle("FontSize", null);
            StyleConstants.setFontSize(fontStyle, annotationFontSize);
            doc.setParagraphAttributes(0, doc.getLength(), fontStyle, false);
          }
          if (isDescriptionShowing) {
            showDescription();
          }
          else {
            updateAnnotationPane();
          }
          uilogger.log(UIEventType.BUTTON_CLICKED, "Annotation text larger");
        }
      }
    });

    // "smaller font" button
    btnFontSmaller = new JButton("");
    btnFontSmaller.setIcon(icoSmaller);
    btnFontSmaller.setFont(timelineFont);
    btnFontSmaller.setToolTipText("Make the annotation text smaller");
    if (System.getProperty("os.name").startsWith("Mac OS")) {
      btnFontSmaller.setMinimumSize(new Dimension(45, 23));
      btnFontSmaller.setPreferredSize(new Dimension(45, 23));
    } else {
      btnFontSmaller.setMinimumSize(new Dimension(40, 23));
      btnFontSmaller.setPreferredSize(new Dimension(40, 23));
    }
    btnFontSmaller.setMargin(new Insets(0, 0, 0, 0));
    setEnterAction(btnFontSmaller);
    btnFontSmaller.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (pnlTimeline.getTimeline() != null) {
          if (annotationFontSize > 1) {
            annotationFontSize = annotationFontSize - 2;
          }
          if (System.getProperty("os.name").startsWith("Mac OS")) {
            annotationFont = new Font(unicodeFont, 0, annotationFontSize - 2);
            Style fontStyle = doc.addStyle("FontSize", null);
            StyleConstants.setFontSize(fontStyle, annotationFontSize - 2);
            doc.setParagraphAttributes(0, doc.getLength(), fontStyle, false);
          } else {
            annotationFont = new Font(unicodeFont, Font.PLAIN, annotationFontSize);
            Style fontStyle = doc.addStyle("FontSize", null);
            StyleConstants.setFontSize(fontStyle, annotationFontSize);
            doc.setParagraphAttributes(0, doc.getLength(), fontStyle, false);
          }
          if (isDescriptionShowing) {
            showDescription();
          }
          else {
            updateAnnotationPane();
          }
          uilogger.log(UIEventType.BUTTON_CLICKED, "Annotation text larger");
        }
      }
    });

    // "all levels" radio button
    radAllLevels = new JRadioButton("All");
    radAllLevels.setFont(timelineFont);
    radAllLevels.setToolTipText("Show the annotations for all bubble levels");
    radAllLevels.setSelected(true);
    if (System.getProperty("os.name").startsWith("Mac OS")) {
      radAllLevels.setMinimumSize(new Dimension(72, 23));
      radAllLevels.setPreferredSize(new Dimension(72, 23));
    } else {
      radAllLevels.setMinimumSize(new Dimension(70, 23));
      radAllLevels.setPreferredSize(new Dimension(70, 23));
    }
    radAllLevels.setMargin(new Insets(0, 0, 0, 0));
    setEnterAction(radAllLevels);
    radAllLevels.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (pnlTimeline.getTimeline() != null) {
          showAllAnnotations = true;
          updateAnnotationPane();
          uilogger.log(UIEventType.RADIOBUTTON_PICKED, "show all levels");
        }
      }
    });

    // "selected levels" radio button
    radSelectedLevels = new JRadioButton("Selected");
    radSelectedLevels.setFont(timelineFont);
    radSelectedLevels.setToolTipText("Show the annotations for the selected bubble level(s)");
    if (System.getProperty("os.name").startsWith("Mac OS")) {
      radSelectedLevels.setMinimumSize(new Dimension(72, 23));
      radSelectedLevels.setPreferredSize(new Dimension(72, 23));
    } else {
      radSelectedLevels.setMinimumSize(new Dimension(70, 23));
      radSelectedLevels.setPreferredSize(new Dimension(70, 23));
    }
    radSelectedLevels.setMargin(new Insets(0, 0, 0, 0));
    setEnterAction(radSelectedLevels);
    radSelectedLevels.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (pnlTimeline.getTimeline() != null) {
          showAllAnnotations = false;
          updateAnnotationPane();
          uilogger.log(UIEventType.RADIOBUTTON_PICKED, "show selected levels");
        }
      }
    });

    // "markers" check box
    chkShowMarkers = new JCheckBox("<html><head></head><body>" + "<DIV STYLE='font-size : " + timelineFontSize + "pt; "
                                   + "font-family : " + timelineFont + "'>"
                                   + "Show Marker<br>Annotations");
    chkShowMarkers.setFont(timelineFont);
    chkShowMarkers.setSelected(true);
    chkShowMarkers.setToolTipText("Show the current marker annotations");
    if (System.getProperty("os.name").startsWith("Mac OS")) {
      chkShowMarkers.setMinimumSize(new Dimension(100, 23));
      chkShowMarkers.setPreferredSize(new Dimension(100, 23));
    } else {
      chkShowMarkers.setMinimumSize(new Dimension(90, 23));
      chkShowMarkers.setPreferredSize(new Dimension(90, 23));
    }
    chkShowMarkers.setMargin(new Insets(0, 0, 0, 0));
    setEnterAction(chkShowMarkers);
    chkShowMarkers.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (pnlTimeline.getTimeline() != null) {
          updateAnnotationPane();
          uilogger.log(UIEventType.CHECKBOX_SELECTED, "show marker annotations");
        }
      }
    });

    // add components to show levels panel
    grpShowLevels.add(radAllLevels);
    grpShowLevels.add(radSelectedLevels);
    pnlLevels.add(radAllLevels, BorderLayout.NORTH);
    pnlLevels.add(radSelectedLevels, BorderLayout.CENTER);
    pnlMarkers.add(chkShowMarkers, BorderLayout.NORTH);
    pnlShow.add(pnlLevels, null);
    pnlFontButtons.add(btnFontLarger);
    pnlFontButtons.add(btnFontSmaller);

    // add buttons
    TimelineUtilities.createConstraints(pnlElapsedVolume, lblElapsed, 0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, 2, 2, 2, 2, 0, 0);
    TimelineUtilities.createConstraints(pnlElapsedVolume, pnlVolumeControl.btnMute, 1, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, 2, 2, 2, 2, 0, 0);
    TimelineUtilities.createConstraints(pnlElapsedVolume, pnlVolumeControl.slideVolume, 2, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, 2, 2, 2, 2, 0, 0);
    TimelineUtilities.createConstraints(pnlBubbleButtons, btnEditBubble, 0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.NORTH, GridBagConstraints.NONE, 2, 2, 2, 2, 0, 0);
    TimelineUtilities.createConstraints(pnlBubbleButtons, btnDeleteBubble, 1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.NORTH, GridBagConstraints.NONE, 2, 2, 2, 2, 0, 0);
    TimelineUtilities.createConstraints(pnlBubbleButtons, btnChangeColor, 0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, 2, 2, 2, 2, 0, 0);
    TimelineUtilities.createConstraints(pnlBubbleButtons, btnGroupBubbles, 1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, 2, 2, 2, 2, 0, 0);
    TimelineUtilities.createConstraints(pnlTimepointButtons, btnAddTimepoint, 0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.NONE, 2, 2, 2, 2, 0, 0);
    TimelineUtilities.createConstraints(pnlTimepointButtons, btnAddMarker, 0, 1, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.NONE, 2, 2, 2, 2, 0, 0);
    TimelineUtilities.createConstraints(pnlTimepointButtons, btnDeleteTimepointOrMarker, 1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, 2, 2, 2, 2, 0, 0);
    TimelineUtilities.createConstraints(pnlTimepointButtons, btnEditTimepointOrMarker, 1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, 2, 2, 2, 2, 0, 0);
    TimelineUtilities.createConstraints(pnlTimelineButtons, btnEditProperties, 0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.NORTH, GridBagConstraints.NONE, 2, 2, 2, 2, 0, 0);
    TimelineUtilities.createConstraints(pnlTimelineButtons, btnClearAll, 1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.NORTH, GridBagConstraints.NONE, 2, 2, 2, 2, 0, 0);
    TimelineUtilities.createConstraints(pnlTimelineButtons, btnZoomTo, 0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, 2, 2, 2, 2, 0, 0);
    TimelineUtilities.createConstraints(pnlTimelineButtons, btnFitToWindow, 1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, 2, 2, 2, 2, 0, 0);
    TimelineUtilities.createConstraints(pnlAnnotations, scrpAnnotations, 0, 0, 1, 3, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, 2, 2, 2, 2, 0, 0);
    TimelineUtilities.createConstraints(pnlAnnotations, pnlFontButtons, 1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, 2, 2, 2, 2, 0, 0);
    TimelineUtilities.createConstraints(pnlAnnotations, pnlShow, 1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.NORTH, GridBagConstraints.BOTH, 2, 2, 2, 2, 0, 0);
    TimelineUtilities.createConstraints(pnlAnnotations, pnlMarkers, 1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.NORTH, GridBagConstraints.NONE, 2, 2, 2, 2, 0, 0);
  }

  /**
   * returns height of panel
   */
  public int getHeight() {
    return height;
  }

/// methods to enable or disable certain controls

  /**
   * disables controls for selected timepoints
   */
  protected void disableSelectedTimepointControls() {
    btnDeleteTimepointOrMarker.setEnabled(false);
    btnEditTimepointOrMarker.setEnabled(false);
  }

  /**
   * enables controls for selected timepoints
   */
  protected void enableSelectedTimepointControls() {
    btnDeleteTimepointOrMarker.setEnabled(true);
    btnEditTimepointOrMarker.setEnabled(true);
  }

  /**
   * disables adding timepoints
   */
  protected void disableAddTimepoint() {
    btnAddTimepoint.setEnabled(false);
  }

  /**
   * enables adding timepoints
   */
  protected void enableAddTimepoint() {
    btnAddTimepoint.setEnabled(true);
  }

  /**
   * disables adding markers
   */
  protected void disableAddMarker() {
    btnAddMarker.setEnabled(false);
  }

  /**
   * enables adding markers
   */
  protected void enableAddMarker() {
    btnAddMarker.setEnabled(true);
  }

  /**
   * disables controls for bubbles
   */
  protected void disableAllBubbleControls() {
    btnEditBubble.setEnabled(false);
    btnChangeColor.setEnabled(false);
    btnDeleteBubble.setEnabled(false);
    btnZoomTo.setEnabled(false);
    btnGroupBubbles.setEnabled(false);
    btnUngroupBubbles.setEnabled(false);
  }

  /**
   * enables controls for bubbles
   */
  protected void enableAllBubbleControls() {
    btnEditBubble.setEnabled(true);
    btnChangeColor.setEnabled(true);
    btnDeleteBubble.setEnabled(true);
    btnZoomTo.setEnabled(true);
    btnGroupBubbles.setEnabled(true);
    btnUngroupBubbles.setEnabled(true);
  }

  /**
   * enables controls for single selected bubbles
   */
  protected void enableSingleSelectedBubbleControls() {
    btnEditBubble.setEnabled(true);
    btnChangeColor.setEnabled(true);
    btnDeleteBubble.setEnabled(true);
    btnZoomTo.setEnabled(true);
  }

  /**
   * enables controls for multiple selected grouped bubbles
   */
  protected void enableMultipleSelectedGroupedBubbleControls() {
    btnEditBubble.setEnabled(false);
    btnChangeColor.setEnabled(true);
    btnDeleteBubble.setEnabled(true);
    btnZoomTo.setEnabled(true);
    btnUngroupBubbles.setEnabled(true);
  }

  /**
   * enables controls for multiple selected ungrouped bubbles
   */
  protected void enableMultipleSelectedUngroupedBubbleControls() {
    btnEditBubble.setEnabled(false);
    btnChangeColor.setEnabled(true);
    btnDeleteBubble.setEnabled(true);
    btnZoomTo.setEnabled(true);
    btnGroupBubbles.setEnabled(true);
  }

  /**
   * disableAllControls
   */
  protected void disableAllControls (){
    this.disableAllBubbleControls();
    this.disableAddMarker();
    this.disableAddTimepoint();
    this.disableSelectedTimepointControls();
    this.btnEditProperties.setEnabled(false);
    this.btnFitToWindow.setEnabled(false);
    this.btnFontLarger.setEnabled(false);
    this.btnFontSmaller.setEnabled(false);
    this.btnClearAll.setEnabled(false);
    this.radAllLevels.setEnabled(false);
    this.radSelectedLevels.setEnabled(false);
    this.chkShowMarkers.setEnabled(false);
    this.pnlVolumeControl.btnMute.setEnabled(false);
    this.pnlVolumeControl.slideVolume.setEnabled(false);
  }

  /**
   * enableMostControls: enables the controls for when nothing is selected
   */
  protected void enableMostControls (){
    this.enableAddMarker();
    this.enableAddTimepoint();
    this.btnEditProperties.setEnabled(true);
    this.btnFitToWindow.setEnabled(true);
    this.btnFontLarger.setEnabled(true);
    this.btnFontSmaller.setEnabled(true);
    this.btnClearAll.setEnabled(true);
    this.radAllLevels.setEnabled(true);
    this.radSelectedLevels.setEnabled(true);
    this.chkShowMarkers.setEnabled(true);
    this.pnlVolumeControl.btnMute.setEnabled(true);
    this.pnlVolumeControl.slideVolume.setEnabled(true);
  }

  /**
   * setMenuBar: sets the menu bar for the timeline panel
   */
  protected void setMenuBar(TimelineMenuBar tmb) {
    menubTimeline = tmb;
  }

  /**
   * doPlayerDisable: disables all playback controls
   */
  protected void doPlayerDisable() {
    pnlAudioControl.btnPlay.setEnabled(false);
    pnlAudioControl.btnStop.setEnabled(false);
    pnlAudioControl.btnFF.setEnabled(false);
    pnlAudioControl.btnRW.setEnabled(false);
    pnlAudioControl.btnPrev.setEnabled(false);
    pnlAudioControl.btnNext.setEnabled(false);
  }

  /**
   * doPlayerEnable: enables all playback controls
   */
  protected void doPlayerEnable() {
    pnlAudioControl.btnPlay.setEnabled(true);
    pnlAudioControl.btnStop.setEnabled(true);
    pnlAudioControl.btnFF.setEnabled(true);
    pnlAudioControl.btnRW.setEnabled(true);
    pnlAudioControl.btnPrev.setEnabled(true);
    pnlAudioControl.btnNext.setEnabled(true);
  }

  /**
   * setPlayer: sets the player object for the play controls
   */
  protected void setPlayer (TimelinePlayer tp) {
    tPlayer = tp;
    this.enableMostControls();
  }

  /**
   * setLocalPlayer: sets the local player object for the play controls
   */
  protected void setLocalPlayer (TimelineLocalPlayer tlp) {
    tLocalPlayer = tlp;
    this.enableMostControls();
  }

  /// Media Methods

  /**
   * Stop tracking (RW or FF). Called when a MouseReleased event is received from the FF or RW buttons.
   */
  protected void doStopTracking() {
    timeline = pnlTimeline.getTimeline();
    trackingState = TimelineControlPanel.TRACKING_NONE;
    timeline.setPlayerShift(TimelineControlPanel.INITIAL_TRACKING_AMOUNT);
    if (playing || buffering) {     //start playing again if we were before
      timeline.startPlayer();
    } else {
      lblStatus.setText(TimelineControlPanel.STATUS_IDLE);
    }
  }

  /**
   * Initiate RW. Called when the RW button is pressed.
   */
  protected void doRWTracking() {
    timeline = pnlTimeline.getTimeline();
    if (playing || buffering) {     //stop playing for now
      timeline.pausePlayer();
    }
    trackingState = TimelineControlPanel.TRACKING_RW;
    lblStatus.setText(TimelineControlPanel.STATUS_TRACKING);
  }

  /**
   * Initiate FF. Called when the FF button is pressed.
   */
  protected void doFFTracking() {
    timeline = pnlTimeline.getTimeline();
    if (playing || buffering) {     //stop playing for now
      timeline.pausePlayer();
    }
    trackingState = TimelineControlPanel.TRACKING_FF;
    lblStatus.setText(TimelineControlPanel.STATUS_TRACKING);
  }

  /**
   * Volume slider was moved, or volume menu items were selected; update the volume.
   * Note that vol and the volume slider should already be set correctly before this method is called
   */
  protected void doVolumeUpdate() {
    if (!muted) {
      pnlTimeline.getTimeline().setPlayerVolume(vol);
    }
  }

  /**
   * Mute (or unmute) the volume
   */
  protected void doMute() {
    if (muted) {
      muted = false;
      pnlTimeline.getTimeline().setPlayerVolume(vol);
      pnlVolumeControl.btnMute.setIcon(UIUtilities.icoSpeaker);
    } else {
      muted = true;
      pnlTimeline.getTimeline().setPlayerVolume(0f);
      pnlVolumeControl.btnMute.setIcon(UIUtilities.icoSpeakerMute);
    }
  }

  /**
   * setEnterAction: overrides the default enter action for a component
   */
  protected void setEnterAction(JComponent comp) {
    comp.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "EnterAction");
    comp.getActionMap().put("EnterAction", new AbstractAction() {
 		private static final long serialVersionUID = 1L;

	public void actionPerformed(ActionEvent ae) {
        pnlTimeline.addTimepoint();
      }
    });
    comp.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.CTRL_DOWN_MASK), "ControlEnterAction");
    comp.getActionMap().put("ControlEnterAction", new AbstractAction() {
 		private static final long serialVersionUID = 1L;

	public void actionPerformed(ActionEvent ae) {
        pnlTimeline.addMarker();
      }
    });
  }

  /**
   * setAnnotationText: sets the annotation text to the string passed
   */
  protected void setAnnotationText(String str) {
    tpAnnotations.setText(str);
  }

  /**
   * clearAnnotationPane: clears the annotation pane
   */
  protected void clearAnnotationPane() {
    setAnnotationText("");
  }

  /**
   * showDescription: shows the timeline description in the annotation pane
   */
  protected void showDescription() {
    if (!isDescriptionShowing) {
      tpAnnotations.setContentType("text/html");
      HTMLEditorKit editorKit = new HTMLEditorKit();
      tpAnnotations.setEditorKit(editorKit);
    }
    try {
      clearAnnotationPane();
      isDescriptionShowing = true;

      HTMLDocument htmldoc = (HTMLDocument)tpAnnotations.getDocument();
      String description = pnlTimeline.getTimeline().getDescription();
      if (description.length() > 0) {
        // create information icon and description
        String imageString = UIUtilities.infoString;
        ImageIcon image = UIUtilities.icoInfoImage;
        tpAnnotations.setText("<html><head></head><body>"
                              + "<DIV STYLE='font-size : " + annotationFontSize + "pt; "
                              + "font-family : " + unicodeFont + "'>"
                              + "<img src = 'file:" + imageString + "'> &nbsp;"
                              + description + "</div></body></html>");
      }
    } catch (Exception e) {
      System.err.println("Error showing description");
    }
    tpAnnotations.setCaretPosition(0);
    scrpAnnotations.revalidate();

  }

  /**
   * updateAnnotationPane: displays the currently active annotations in the annotation pane
   */
  protected void updateAnnotationPane() {
    tpAnnotations = new JTextPane();
    tpAnnotations.setEditable(false);
    tpAnnotations.setFont(annotationFont);
    if (scrpAnnotations != null && tpAnnotations != null && scrpAnnotations.getViewport() !=null) {
    scrpAnnotations.setViewportView(tpAnnotations);
    }
    Timeline timeline = pnlTimeline.getTimeline();
    Vector currentBubbles = new Vector();
    timeline.getNextMarkerOffset();
    isDescriptionShowing = false;
    boolean markerPrecedesOffset = timeline.lastImportantOffsetIsMarker();
    BubbleTreeNode currNode = timeline.getBaseBubbleNode(timeline.getBaseBubbleNumAtCurrentOffset()-1);

    if (showAllAnnotations) { // add base bubble and all parent bubbles to currentbubbles
      currentBubbles.add(currNode);
      while (!((BubbleTreeNode)currNode.getParent()).isRoot()) {
        currNode = (BubbleTreeNode)currNode.getParent();
        if (currNode.getBubble().getAnnotation()!="" || currNode.getBubble().getLabel()!="") {
          currentBubbles.add(currNode);
        }
      }
    }
    else { // add only bubbles from selected levels to currentbubbles

      // get the selected levels, deselect all bubbles, and reselect bubbles at same levels at current offset
      Vector selectedLevels = timeline.getSelectedLevels();
      timeline.clearSelectedBubbles();

      int level = 1;
      if (selectedLevels.contains(Integer.valueOf(level))) { // base level selected?
        currentBubbles.add(currNode);
        timeline.selectBubble(timeline.topBubbleNode.getPreOrderIndex(currNode), 5);
      }
      while (!((BubbleTreeNode)currNode.getParent()).isRoot()) {
        level++;
        currNode = (BubbleTreeNode)currNode.getParent();
        if (selectedLevels.contains(Integer.valueOf(level))) {
          currentBubbles.add(currNode);
          timeline.selectBubble(timeline.topBubbleNode.getPreOrderIndex(currNode), 5);
        }
      }
      pnlTimeline.refreshTimeline();
    }
    try {
      clearAnnotationPane();
      StyledEditorKit sek = new StyledEditorKit();
      tpAnnotations.setEditorKit(sek);
      tpAnnotations.setContentType("text/plain");
      tpAnnotations.setVisible(false);
      doc = (StyledDocument)tpAnnotations.getDocument();
      selectedStyle = doc.addStyle("Selected", null);
      normalStyle = doc.addStyle("Normal", null);
      boldStyle = doc.addStyle("Bold", null);
      StyleConstants.setBackground(selectedStyle, new Color(230, 230, 230));
      StyleConstants.setBold(boldStyle, true);
      StyleConstants.setFontSize(selectedStyle, annotationFontSize);
      StyleConstants.setFontSize(normalStyle, annotationFontSize);
      StyleConstants.setFontFamily(selectedStyle, unicodeFont);
      StyleConstants.setFontFamily(normalStyle, unicodeFont);
      StyleConstants.setFontFamily(boldStyle, unicodeFont);

      for (int i = currentBubbles.size()- 1; i >= 0; i--) {
        int prevLength = doc.getLength();
        Bubble currBubble = ((BubbleTreeNode)currentBubbles.elementAt(i)).getBubble();
        String currAnnotation = currBubble.getAnnotation();
        String currLabel = currBubble.getLabel();
        if (showAllAnnotations && currBubble.isSelected()) { // selected bubble
          if (currLabel.equals("") && !currAnnotation.equals("")) { // if there is no label, use "Level 1", etc.
            doc.insertString(doc.getLength(), "Level " + currBubble.getLevel(), normalStyle);
          }
          else if (!currLabel.equals("")){ // or use the label
            doc.insertString(doc.getLength(), currLabel, boldStyle);
          } // put in the annotation
          if (!currAnnotation.equals("")) {
            doc.insertString(doc.getLength(), ": ", normalStyle);
            doc.insertString(doc.getLength(), currAnnotation, selectedStyle);
          }
          StyleConstants.setLeftIndent(selectedStyle, ((currentBubbles.size()-1-i) * 20));
          doc.setParagraphAttributes(prevLength, doc.getLength()-prevLength, selectedStyle, false);
        }
        else { // non selected bubble -- or if selected levels is selected, a selected bubble :)
          if (currLabel.equals("") && !currAnnotation.equals("")) { // if there is no label, use "Level 1", etc.
            doc.insertString(doc.getLength(), "Level " + currBubble.getLevel(), normalStyle);
          }
          else if (!currLabel.equals("")){ // or use the label
            doc.insertString(doc.getLength(), currLabel, boldStyle);
          } // put in the annotation
          if (!currAnnotation.equals("")) {
            doc.insertString(doc.getLength(), ": " + currAnnotation, normalStyle);
          }
          StyleConstants.setLeftIndent(normalStyle, ((currentBubbles.size()-1-i) * 20));
          doc.setParagraphAttributes(prevLength, doc.getLength()-prevLength, normalStyle, false);
        }
        if (currLabel.equals("") && currAnnotation.equals("")) {
          // do not add a line
        }
        else {
          doc.insertString(doc.getLength(), "\n", normalStyle);
        }
      }
      // now add marker, if any
      if (chkShowMarkers.isSelected() && markerPrecedesOffset) {
        Marker currMarker = timeline.getMarker(timeline.previousMarkerOffset);
        if (currMarker != null && currMarker.getAnnotation()!="") {
          StyleConstants.setLeftIndent(normalStyle, 0);
          doc.insertString(doc.getLength(), "\u25B2 " + currMarker.getLabel() + ": ", boldStyle);
          if (currMarker.isSelected()) {
            doc.insertString(doc.getLength(), currMarker.getAnnotation(), selectedStyle);
          }
          else {
            doc.insertString(doc.getLength(), currMarker.getAnnotation(), normalStyle);
          }
        }
      }
      tpAnnotations.setVisible(true);
    } catch (BadLocationException ble) {
      System.err.println("Error displaying annotation");
    }
    tpAnnotations.setCaretPosition(0);
    scrpAnnotations.revalidate();
  }

}