package ui.timeliner;

import javax.swing.*;
import javax.swing.tree.*;

import org.apache.log4j.Logger;

import java.awt.*;
import java.util.*;
import javax.swing.event.*;
import com.borland.jbcl.layout.*;
import java.awt.geom.*;
import java.io.File;

import ui.common.*;

/**
 * Timeline class
 * This class represents a timeline object. To create a timeline object, you need
 * to place it within a timeline panel. The timeline object in turn contains
 * other objects, such as a slider, a set of bubbles and timepoints, etc. There are
 * a number of methods for accessing and changing the timeline, accessible
 * primarily through the timeline panel object.
 *
 * @author Brent Yorgason
 */

public class Timeline extends JPanel {

 private static final long serialVersionUID = 1L;
// timeline properties
  private int start;
  private int end;
  private int yLoc;
  private int length;
  private int bubbleHeight; // the default height fneor individual bubbles
  private int bubbleType;
  private boolean blackAndWhite;
  private String description = "";
  private int totalBubbleHeight; // the height of all bubble levels together
  private int numBaseBubbles; // the number of bubbles on the base level
  private int numTotalBubbles; // the total number of bubbles
  private int numMarkers; // the total number of markers
  private int anchorBubble = -1; // the first bubble selected in a shift-click-select, etc.
  private Rectangle rect;
  protected int colorScheme = 1; // 1 is the default bubble levels color scheme
  public String currFilename = null;
  protected int widthBuffer;
  protected int bottomSpace;
  protected int smallestBubbleHeight = 13;

  private static Logger log = Logger.getLogger(TimelineControlPanel.class);

  // description icon
  protected Image imgDescription = UIUtilities.imgDescription;
  protected Image imgDescriptionHover = UIUtilities.imgDescriptionHover;
  protected Image imgDescriptionOutline = UIUtilities.imgDescriptionOutline;
  protected Rectangle descriptionRect = new Rectangle(0,0,0,0);
  protected int currDescriptionState = 0;

  /* the sortedPointList contains the horizontal offset values of the timepoints on the timeline */
  private int sortedPointList[] = new int[1000];  // arbitrary large number of points

  /* the sortedPixelList is a separate list of pixel positions based on the sortedPointList */
  private int sortedPixelList[] = new int[1000];

  /* the markerList contains the horizontal offset values of the markers on the timeline */
  private int markerList[] = new int[1000];

  /* the markerPixelList is a separate list of pixel positions based on the markerList */
  private int markerPixelList[] = new int[1000];

  /* this Vector allows access to individual timepoint objects */
  private Vector<Timepoint> Timepoints = new Vector<Timepoint>(2);

  /* this Vector allows access to individual timepoint objects */
  private Vector<Marker> Markers = new Vector<Marker>(0);

  /* these vectors keep track of which bubbles are selected */
  private Vector selectedBubbles = new Vector();
  private Vector<Integer> selectedBaseBubbles = new Vector<Integer>();
  private Vector<Integer> selectedBoxBubbles = new Vector<Integer>();

  // other components of the timeline
  private TimelineResizer tResizer;
  private TimelineSlider slide;
  private TimelineSliderUI sliderUI;
  protected JLabel lblThumb;
  private JLabel lblTimepoint;

  // references to external objects
  private TimelinePanel pnlTimeline;
  private TimelineControlPanel pnlControl;
  private TimelinePlayer tPlayer;
  private TimelineLocalPlayer tLocalPlayer;
  //private Graphics g;
  private Graphics2D g2d;
  final private int offset = 5; // this has to do with the layout of the slider component

  // properties that can be set by the user
  private boolean draggable = false;
  private boolean editable = true;
  private boolean resizable = true;
  private boolean showTimepointTimes = false;
  protected boolean autoScalingOn = true;
  protected boolean playWhenBubbleClicked = false;
  protected boolean stopPlayingAtSelectionEnd = false;
  protected boolean wasResizable = true;

  // event related
  private boolean draggingTimepoint;
  private boolean draggingMarker;
  private boolean timepointOffLine;
  private boolean markerOffLine;
  protected boolean markerWasPassed = false;
  private int dragMouseX;
  private int dragMouseY;
  protected boolean isDirty;
  protected boolean editingBubbles = false;
  protected boolean titleIsHidden = false;
  protected int lastTimepointClicked;
  protected int lastMarkerClicked = -1;
  private int lastMarkerPassed = -1;
  protected int nextOffset;
  protected int nextMarkerOffset;
  protected int previousMarkerOffset = -1;
  protected boolean loopingOn = false;

  // media length
  protected int mediaLength = 100000;  // default value here, later set to duration of audio

  // bubble tree
  protected BubbleTreeNode topBubbleNode;
  private JTree bubbleTree;
  private Bubble rootBubble = new Bubble();
  protected DefaultTreeModel treeModel;

  // zoom variables
  protected boolean timelineZoomed = false;
  protected boolean freshZoom = false;
  private int topZoomLevel = 1;
  private int zoomIndex;

  // the default colors for the various bubble levels
  protected Color defaultBubbleLevelColors[] = {null,
    new Color(166, 202, 240), new Color(255, 255, 120), new Color(255, 200, 100),
    new Color(200, 255, 200), new Color(252, 215, 215), new Color(209,173,209),
    new Color(249,188,162), new Color(156,222,235), new Color(210,210,205)};
  protected Color pastelBubbleLevelColors[] = {null,
    new Color(166,200,240), new Color(240,166,202), new Color(202,240,166),
    new Color(204,166,240), new Color(231,245,121), new Color(240,204,166),
    new Color(172,250,101), new Color(190,237,214), new Color(242,184,121)};
  protected Color highContrastBubbleLevelColors[] = {null,
    new Color(102,255,51), new Color(0,204,255), new Color(255,51,51),
    new Color(255,153,51), new Color(153,0,204), new Color(0,153,153),
    new Color(51,102,0), new Color(204,153,153), new Color(204,204,204)};
  protected Color brightColorsBubbleLevelColors[] = {null,
    new Color(245,12,12), new Color(245,250,4), new Color(5,238,6),
    new Color(128,255,0), new Color(9,81,246), new Color(7,0,255),
    new Color(151,5,210), new Color(223,4,162), new Color (241,5,5)};
  protected Color warmBubbleLevelColors[] = {null,
    new Color(247,144,41), new Color(246,246,58), new Color(235,47,22),
    new Color(205,150,49), new Color(246,191,6), new Color(252,5,5),
    new Color(255,153,51), new Color(234,246,16), new Color(134,134,31)};
  protected Color coolBubbleLevelColors[] = {null,
    new Color(102,102,226), new Color(69,200,69), new Color(120,197,217),
    new Color(0,51,202), new Color(7,120,67), new Color(150,211,204),
    new Color(76,55,255), new Color(102,143,255), new Color(14,252,103)};
  protected Color earthTonesBubbleLevelColors[] = {null,
    new Color(142,96,40), new Color(153,133,0), new Color(211,149,0),
    new Color(103,51,0), new Color(204,87,0), new Color(102,107,0),
    new Color(79,56,19), new Color(255,255,102), new Color(153,51,0)};
  protected Color bubbleLevelColors[] = new Color[100];

  /**
   * constructor: creates and draws a timeline. The parameters needed are the current graphics
   * environment, the x and y points for the line, the length of the line, the bubble height,
   * and a reference to the timeline panel that contains it
   */
  public Timeline(Graphics2D graphics, int lineStart, int lineY, int lineLength, int bHeight,
                  int bType, boolean bw, TimelinePanel tp) {
    // store parameters
    g2d = graphics;
    start = lineStart;
    end = lineStart + lineLength;
    yLoc = lineY;
    length = lineLength;
    bubbleHeight = bHeight;
    totalBubbleHeight = bubbleHeight;
    bubbleType = bType;
    blackAndWhite = bw;
    pnlTimeline = tp;
    pnlControl = tp.getFrame().getControlPanel();

    currFilename = null;
    timelineInit(); // performs additional initialization
    tResizer = new TimelineResizer(pnlTimeline);

    // create the bubble tree
    createBubbleTree();
  }

  /**
   * alternate constructor: creates and draws a timeline. (used primarily when opening a saved timeline)
   * The parameters needed are the current graphics environment, the bubble height, black and white state,
   * the number of bubbles, a list of time points, the timepoint vector, the marker vector, the top node of the bubble tree,
   * and a reference to the timeline panel that contains it
   */
  public Timeline(Graphics2D graphics, int lineLength, int bHeight, int bType,
                  boolean bw, int numBubs, int numMarks, int[] spList, int[] mList, Vector tPoints, Vector mPoints, BubbleTreeNode topNode, TimelinePanel tp) {

    // store parameters
    pnlTimeline = tp;
    pnlControl = tp.getFrame().getControlPanel();
    g2d = graphics;
    start = pnlTimeline.getDefaultLineStart();
    end = start + lineLength;
    yLoc = pnlTimeline.getDefaultLineY();
    length = lineLength;
    bubbleHeight = bHeight;
    bubbleType = bType;
    blackAndWhite = bw;
    topBubbleNode = topNode;

    timelineInit();  // performs additional initialization

    createBubbleTree();
    clearSelectedBubbles();

    numBaseBubbles = numBubs;
    numMarkers = numMarks;
    numTotalBubbles = topBubbleNode.getDescendentCount();

    if (pnlTimeline.getFrame().isUsingLocalAudio) {
      tLocalPlayer = pnlTimeline.getLocalPlayer();
    }
    else {
      tPlayer = pnlTimeline.getPlayer();
    }
    mediaLength = getPlayerDuration();

    pnlTimeline.removeAll();
    tResizer = new TimelineResizer(pnlTimeline);

    // now add the slider
    slide = new TimelineSlider(0, 0, mediaLength, 0, length + (offset * 2), 30, this);
    slide.setBackground(pnlTimeline.getPanelColor());

    // add the slider to the panel in a specific location (right below the bubbles)
    pnlTimeline.add(slide, new XYConstraints(start - offset, yLoc,
        length + (offset * 2), slide.getHeight()));
    slide.setSize(length + (offset * 2), slide.getHeight());
    pnlTimeline.updateUI();

    // read in the saved list of points
    sortedPointList = spList;
    markerList = mList;
    Timepoints = tPoints;
    Markers = mPoints;
    createTimepointAndMarkerTimes();

    sortList();

    pnlTimeline.doResize(length);
  }

  /*****************************************
   * methods for initialization of components
   *****************************************/

  /**
   * createBubbleTree: creates a tree representation of the timeline bubbles
   */
  private void createBubbleTree() {
    if (topBubbleNode == null) {
      topBubbleNode = new BubbleTreeNode((Object)rootBubble);
    }
    treeModel = new DefaultTreeModel(topBubbleNode);
    bubbleTree = new JTree(treeModel);
  }

  /**
   * createTimepointAndMarkerTimes: initializes the timepoint and marker times
   */
  public void createTimepointAndMarkerTimes() {
    for (int i = 0; i < numBaseBubbles; i++) {
      Timepoint timepoint = (Timepoint)Timepoints.elementAt(i);
      String str = UIUtilities.convertOffsetToHoursMinutesSecondsTenths(sortedPointList[i]);
      if (i == 0) {
        timepoint.setTime("0");
      }
      else {
        timepoint.setTime(str);
      }
    }
    for (int i = 0; i < numMarkers; i++) {
      Marker marker = (Marker)Markers.elementAt(i);
      String str = UIUtilities.convertOffsetToHoursMinutesSecondsTenths(markerList[i]);
      marker.setTime(str);
    }
  }

  /**
   * determineColorScheme: determines if the current bubble level colors match a color scheme
   * returns the number of the scheme
   * Note: the first color of each scheme needs to be unique for this to work
   */
  protected int determineColorScheme() {
    int numLevels = 10;

    if(bubbleLevelColors[1].equals(defaultBubbleLevelColors[1])) {
      for (int i = 2; i < numLevels; i++) {
        if (!bubbleLevelColors[i].equals(defaultBubbleLevelColors[i])) {
          return 0; // custom scheme
        }
      }
      return 1; // default scheme
    }
    else if (bubbleLevelColors[1].equals(pastelBubbleLevelColors[1])) {
      for (int i = 2; i < numLevels; i++) {
        if (!bubbleLevelColors[i].equals(pastelBubbleLevelColors[i])) {
          return 0; // custom scheme
        }
      }
      return 2; // pastel scheme
    }
    else if (bubbleLevelColors[1].equals(brightColorsBubbleLevelColors[1])) {
      for (int i = 2; i < numLevels; i++) {
        if (!bubbleLevelColors[i].equals(brightColorsBubbleLevelColors[i])) {
          return 0; // custom scheme
        }
      }
      return 3; // bright colors scheme
    }
    else if (bubbleLevelColors[1].equals(highContrastBubbleLevelColors[1])) {
      for (int i = 2; i < numLevels; i++) {
        if (!bubbleLevelColors[i].equals(highContrastBubbleLevelColors[i])) {
          return 0; // custom scheme
        }
      }
      return 4; // high contrast scheme
    }
    else if (bubbleLevelColors[1].equals(warmBubbleLevelColors[1])) {
      for (int i = 2; i < numLevels; i++) {
        if (!bubbleLevelColors[i].equals(warmBubbleLevelColors[i])) {
          return 0; // custom scheme
        }
      }
      return 5; // warm scheme
    }
    else if (bubbleLevelColors[1].equals(coolBubbleLevelColors[1])) {
      for (int i = 2; i < numLevels; i++) {
        if (!bubbleLevelColors[i].equals(coolBubbleLevelColors[i])) {
          return 0; // custom scheme
        }
      }
      return 6; // cool scheme
    }
    else if (bubbleLevelColors[1].equals(earthTonesBubbleLevelColors[1])) {
      for (int i = 2; i < numLevels; i++) {
        if (!bubbleLevelColors[i].equals(earthTonesBubbleLevelColors[i])) {
          return 0; // custom scheme
        }
      }
      return 7; // earth tones scheme
    }
    else {
      return 0; // custom scheme
    }

  }

  /**
   * drawResizer: draws the line resizer as well as a faux line to make resizer updating appear smoother
   */
  protected void drawResizer() {
    tResizer.move(end - 3, yLoc, g2d);
  }

  /**
   * drawDescriptionIcon: draws the description icon
   * the icon state is also passed
   */
  protected void drawDescriptionIcon(Graphics2D g2, int state) {
    if (this.description.length() > 0) {
      int descHeight = 20;
      int descWidth = 20;
      int descY = yLoc - this.getHighestBubbleHeight()- descWidth/2 - 13 ;
      descriptionRect = new Rectangle(start, descY, descWidth, descHeight);
      currDescriptionState = state;

      switch(state) {
        case(0): // normal
          g2.drawImage(imgDescription, start, descY, descWidth, descHeight, this);
          break;
        case(1): // hover
          g2.drawImage(imgDescriptionHover, start, descY, descWidth, descHeight, this);
          break;
        case(2): // outline
          g2.drawImage(imgDescriptionOutline, start, descY, descWidth, descHeight, this);
          break;
      }
    }
  }

  /**
   * drawTitle: draws the timeline title
   */
  protected void drawTitle() {
    g2d.setFont(UIUtilities.fontTitleBold);
    int offset = (this.description.length() > 0)? 24 : 0;
    g2d.drawString(pnlTimeline.getFrame().getTitle(), start + offset, yLoc - this.getHighestBubbleHeight() - 10);
  }

  /**
   * initializeLists: initializes the timepoint vector, marker vector, percentPointList vector, and sortedPointList.
   * Also clears and reloads the bubble tree
   */
  private void initializeLists() {
    Timepoints.removeAllElements();
    Markers.removeAllElements();
    for (int i = 0; i <= numBaseBubbles; i++) {
      sortedPointList[i] = 0;
    }
    for (int i = 0; i < numMarkers; i++) {
      markerList[i] = 0;
    }
    topBubbleNode.removeAllChildren();
    treeModel.reload();
  }

  /**
   * resetColors: resets the panel and bubble colors to default values
   */
  protected void resetColors() {
    pnlTimeline.setPanelColor(Color.white);
    int offset = 0;
    for (int i = 0; i < 100; i++) {
      bubbleLevelColors[i] = defaultBubbleLevelColors[offset];
      offset++;
      // rotate the colors
      if (offset == defaultBubbleLevelColors.length) {
        offset = 1;
      }
    }

  }

  /**
   * setPlayer: sets the player object for the timeline
   */
  protected void setPlayer(TimelinePlayer tp) {
    tPlayer = tp;
    mediaLength = tPlayer.getDuration();
    tPlayer.nextImportantOffset = Math.min(sortedPointList[nextOffset], markerList[nextMarkerOffset]);
    tPlayer.localStartOffset = 0;
    tPlayer.localEndOffset = mediaLength;

    setupSlider();
  }

  /**
   * setLocalPlayer: sets the local player object for the timeline
   */
  protected void setLocalPlayer(TimelineLocalPlayer tlp) {
    tLocalPlayer = tlp;
    mediaLength = tLocalPlayer.getDuration();
    tLocalPlayer.nextImportantOffset = Math.min(sortedPointList[nextOffset], markerList[nextMarkerOffset]);
    tLocalPlayer.localStartOffset = 0;
    tLocalPlayer.localEndOffset = mediaLength;

    setupSlider();
  }

  /**
   * setupSlider: initialization for the slider
   */
  private void setupSlider() {
    // now add the slider
    slide = new TimelineSlider(0, 0, mediaLength, 0, length + (offset * 2), 30, this);
    slide.setBackground(pnlTimeline.getPanelColor());

    // add the slider to the panel in a specific location (right below the bubbles)
    pnlTimeline.add(slide, new XYConstraints(start - offset, yLoc,
        length + (offset * 2), slide.getHeight()));
    slide.setSize(length + (offset * 2), slide.getHeight());
    pnlTimeline.updateUI();

    // reset to draw the timeline in its initial state
    resetColors();
    resetTimeline();

    // add any initial timepoints
    Vector timepointsToAdd = pnlTimeline.getFrame().initialTimepoints;
    Vector markersToAdd = pnlTimeline.getFrame().initialMarkers;
    for (int i = 0; i < timepointsToAdd.size(); i++) {
      addTimepoint(((Integer)timepointsToAdd.elementAt(i)).intValue());
    }
    for (int i = 0; i < markersToAdd.size(); i++) {
      addMarker(((Integer)markersToAdd.elementAt(i)).intValue());
    }
    makeClean();

  }

  /**
   * timelineInit: initialization procedures
   */
  private void timelineInit() {
    draggingTimepoint = false;
    draggingMarker = false;
    makeClean();

    // overall rect of the bubble diagram--used for mouse-click checking
    rect = new Rectangle(start, yLoc - totalBubbleHeight, length, totalBubbleHeight);

    // create line components
    // time labels
    lblThumb = pnlTimeline.createTimeLabel();
    lblThumb.setBackground(pnlTimeline.getBackground());
    lblThumb.setOpaque(true);
    lblTimepoint = pnlTimeline.createTimeLabel();

    nextOffset = 1;
    nextMarkerOffset = 0;
    if (System.getProperty("os.name").startsWith("Mac OS")) {
      widthBuffer = 40;
    } else {
      widthBuffer = 50;
    }
    pnlTimeline.getFrame();
	bottomSpace = TimelineFrame.BOTTOM_SPACE;
  }

  /**
   * updatePopupMenus: sets the properties of the bubble popup menus
   */
  protected void updatePopupMenus() {
    boolean canMoveUp = true;
    boolean canMoveDown = true;
    for (int i = 0; i < selectedBubbles.size(); i ++) {
      int currBubble = ((Integer)selectedBubbles.elementAt(i)).intValue();
      Bubble bub = getBubble(currBubble);
      BubbleTreeNode thisNode = getBubbleNode(currBubble);

      // calculate room to move bubble up or down
      BubbleTreeNode parentNode = (BubbleTreeNode)thisNode.getParent();
      if (!parentNode.isRoot() && ((BubbleTreeNode)thisNode.getParent()).getBubble().getLevel()==(bub.getLevel()+1)) {
        canMoveUp = false;
      }
      if (bub.getLevel() == 1) {
        canMoveDown = false;
      }
      else {
        for (int j = 0; j < thisNode.getChildCount(); j++) {
          if (((BubbleTreeNode)thisNode.getChildAt(j)).getBubble().getLevel()==(bub.getLevel()-1)) {
            canMoveDown = false;
          }
        }
      }
    }

    // now enable or disable menu options
    pnlTimeline.menubTimeline.menuiMoveBubbleDown.setEnabled(canMoveDown);
    pnlTimeline.menubTimeline.menuiMoveBubbleUp.setEnabled(canMoveUp);
    pnlTimeline.timelinePopups.menuiMoveBubbleDown.setEnabled(canMoveDown);
    pnlTimeline.timelinePopups.menuiMoveBubbleUp.setEnabled(canMoveUp);
    pnlTimeline.timelinePopups.menuiMoveBubbleDown2.setEnabled(canMoveDown);
    pnlTimeline.timelinePopups.menuiMoveBubbleUp2.setEnabled(canMoveUp);
    pnlTimeline.timelinePopups.menuiMoveBubbleDown3.setEnabled(canMoveDown);
    pnlTimeline.timelinePopups.menuiMoveBubbleUp3.setEnabled(canMoveUp);
    if (pnlTimeline.btnBubbleEditorDownLevel != null) {
      pnlTimeline.btnBubbleEditorDownLevel.setEnabled(canMoveDown);
    }
    if (pnlTimeline.btnBubbleEditorUpLevel != null) {
      pnlTimeline.btnBubbleEditorUpLevel.setEnabled(canMoveUp);
    }
  }

  /****************************************
   * methods having to do with the timeline
   ***************************************/

  /**
   * contains: returns true if the timeline rect contains point p
   */
  public boolean contains(Point p) {
    if (rect.contains(p)) {
      return true;
    }
    else return false;
  }

  /**
   * doLastResize: the final resize after a user releases the mouse--also used by zoomTo for resizing
   */
  protected void doLastResize(int len, Graphics2D graphics) {
    int oldLength = getLineLength();
    setLineLength(len);
    setLineEnd(length + start);
    TimelineFrame frmTimeline = pnlTimeline.getFrame();
    boolean doShift = true;
    Dimension d = new Dimension(frmTimeline.scrollPane.getWidth(), frmTimeline.scrollPane.getHeight());
    int panelWidth = pnlTimeline.getWidth();
    int screenWidth = (int)Toolkit.getDefaultToolkit().getScreenSize().getWidth();
    int frameWidth = frmTimeline.getWidth();
    int frameHeight = frmTimeline.getHeight();
    int panelHeight = frameHeight - pnlControl.height - TimelineFrame.SPACER;
    int tallestBubble = (int)((double)totalBubbleHeight * ((double)len / (double)oldLength));
    boolean isTooTall = false;
    boolean isTooWide = false;
    int oldPanelHeight = panelHeight;
    if ((tallestBubble + bottomSpace) > panelHeight) {
      panelHeight = tallestBubble + bottomSpace;
      isTooTall = true;
    }

    if (end < frameWidth - widthBuffer) {
      pnlTimeline.setMinimumSize(new Dimension(frameWidth - widthBuffer, panelHeight));
      pnlTimeline.setPreferredSize(new Dimension(frameWidth - widthBuffer, panelHeight));
    }
    else if (end >= panelWidth - widthBuffer) {
      pnlTimeline.setMinimumSize(new Dimension(end + 500, panelHeight));
      pnlTimeline.setPreferredSize(new Dimension(end + 500, panelHeight));
      isTooWide = true;
    }
    else if (end < panelWidth - frameWidth + widthBuffer) {
      pnlTimeline.setMinimumSize(new Dimension(end + 500, panelHeight));
      pnlTimeline.setPreferredSize(new Dimension(end + 500, panelHeight));
      isTooWide = true;
    }
    else {
      pnlTimeline.setMinimumSize(new Dimension(panelWidth, panelHeight));
      pnlTimeline.setPreferredSize(new Dimension(panelWidth, panelHeight));
      isTooWide = true;
    }

    // make sure scrollpane doesn't resize
    frmTimeline.scrollPane.setPreferredSize(d);
    frmTimeline.scrollPane.setSize(d);

    // change rect and hide time mark
    yLoc = panelHeight - bottomSpace;
    if (isTooTall && !isTooWide) {
      int diff = panelHeight - oldPanelHeight;
      if (diff >= 17) { // 17 is the height of the windows scroll bar (need to check for mac!)
        yLoc = yLoc - 17;
      }
      else {
        yLoc = yLoc - (diff);
      }
    }
    rect = new Rectangle(start, yLoc - totalBubbleHeight, length, totalBubbleHeight);
    showTime(false);

    // adjust the position of the slider in the panel
    pnlTimeline.add(slide, new XYConstraints(start - offset, yLoc,
        length + (offset * 2), slide.getHeight()));
    slide.setSize(length + (offset * 2), slide.getHeight());

    // update timepoint list
    respaceTimepointsAndMarkers(oldLength, length);

    // determine if there is a zoom offset associated with this resize action
    if (timelineZoomed && freshZoom) {
      int zoomOffset = this.sortedPixelList[zoomIndex] + start;
      pnlTimeline.scrollRectToVisible(new Rectangle(zoomOffset, panelHeight-100, pnlTimeline.getFrame().getWidth() - start, panelHeight));
      freshZoom = false;
    }
    else {
      Rectangle newRect = new Rectangle((int)pnlTimeline.getVisibleRect().getX(), panelHeight-100, pnlTimeline.getFrame().getWidth() - start, panelHeight);
      pnlTimeline.scrollRectToVisible(newRect);
    }
  }

  /**
   * makeClean: makes the timeline clean, so that a save is not required
   */
  protected void makeClean() {
    if (isDirty) {
      isDirty = false;
      if (System.getProperty("os.name").startsWith("Mac OS")) {
        pnlTimeline.getFrame().getRootPane().putClientProperty("windowModified", Boolean.FALSE);
      }
    }
  }

  /**
   * makeDirty: makes the timeline dirty, so that a save is required
   */
  protected void makeDirty() {
    if (!isDirty) {
      isDirty = true;
      if (System.getProperty("os.name").startsWith("Mac OS")) {
        pnlTimeline.getFrame().getRootPane().putClientProperty("windowModified", Boolean.TRUE);
      }
    }
  }

  /**
   * moveTo: moves the timeline to the point specified
   */
  protected void moveTo (int pointX, int pointY) {
    // set up dragging constraints
    if (pointX >= 0) { // may be dragged off to the right edge but not the left
      start = pointX;
      end = start + length;
    }
    yLoc = pointY;

    // set the new timeline rect
    rect = new Rectangle(start, yLoc - totalBubbleHeight, length, totalBubbleHeight);

    // move the slider
    pnlTimeline.add(slide, new XYConstraints(start - offset, yLoc,
        length + (offset * 2), slide.getHeight()));
    slide.setSize(length + (offset * 2), slide.getHeight());
    pnlTimeline.updateUI();

    showTime(false);
  }

  /**
   * notifyOfImportantOffset: receive a notification of an offset reached from the timer and decide
   * how to handle it
   */
  protected void notifyOfImportantOffset(int offset) {
    // case: end of bubble, when stop at end or looping is on
    if (nextMarkerOffset < numMarkers && getNextImportantOffset() == markerList[nextMarkerOffset]) {
      markerWasPassed = true;
      lastMarkerPassed = nextMarkerOffset;
    }
    if (offset >= getLocalEndOffset() && offset < (getLocalEndOffset() + 1000)) { // end reached
      pausePlayer();
      if (!editingBubbles) {
        setNextImportantOffset(sortedPointList[nextOffset+1]);
      }
      else {
        if (pnlTimeline.getFrame().isUsingLocalAudio) {
          setPlayerOffset(tLocalPlayer.localStartOffset);
        }
        else {
          setPlayerOffset(tPlayer.localStartOffset);
        }
      }
      if (loopingOn) {
        startPlayer();
      }
      else {
        pnlControl.btn_pauseAction();
      }
      showTime(false);
    }
    // case: end of bubble otherwise, update annotation
    else {
        log.debug("important offset = " + offset);
      pnlControl.updateAnnotationPane();
      if (nextMarkerOffset < numMarkers && markerList[nextMarkerOffset] < sortedPointList[nextOffset]) {
        setNextImportantOffset(markerList[nextMarkerOffset]);
      }
      else {
        setNextImportantOffset(sortedPointList[nextOffset]);
      }
    }
  }

  /**
   * refresh: refreshes the timeline by repainting all lightweight parts
   */
  protected void refresh(Graphics2D graphics) {
    g2d = graphics;

    // draw fake slider line beneath actual one (eliminates blinking)
    g2d.setStroke(new BasicStroke(5));
    g2d.drawLine(start + 3, yLoc, length + start - 6, yLoc);
    g2d.setStroke(new BasicStroke(1));

    drawResizer();
    blowBubbles();
  }

  /**
   * repositionHead: reposition the playback head to the beginning of the bubble clicked
   */
  protected void repositionHead(int bubClicked) {
    //Bubble currBubble = getBubble(bubClicked);
    BubbleTreeNode currNode = getBubbleNode(bubClicked);
    int currPoint = topBubbleNode.getLeafIndex(currNode.getFirstLeaf());
    int buffer = 10;
    int currOff = sortedPointList[currPoint];

    // handle playback
    /**    if (pnlTimeline.getFrame().isUsingLocalAudio) {
      TimelineLocalPlayer tLocalPlayer = pnlTimeline.getLocalPlayer();
    }
    else {
      TimelinePlayer tPlayer = pnlTimeline.getPlayer();
    }
    */
    if (playerIsPlaying() || playWhenBubbleClicked) {
      pausePlayer();
      slide.setValue(currOff);
      setPlayerOffset(currOff);
      showTime(false);
      pnlControl.btn_playAction();
    }
    else {
      slide.setValue(currOff+buffer);
      setPlayerOffset(currOff+buffer);
      pnlControl.btn_pauseAction();
      if (showTimepointTimes) {
        showTime(false);
      }
      else {
        showTime(true);
      }
    }
  }


  /**
   * resetTimeline: reinitializes lists and resets variables, refreshes the display
   */
  protected void resetTimeline() {
    makeDirty();
    initializeLists();
    clearSelectedBubbles();
    numBaseBubbles = 0;
    numMarkers = 0;
    numTotalBubbles = 0;
    slide.setValue(0);
    showTime(false);
    timelineZoomed = false;
    int frameWidth = pnlTimeline.getFrame().getWidth();
    pnlTimeline.doResize(pnlTimeline.timelineLength);
    Graphics2D g2 = (Graphics2D)pnlTimeline.getGraphics();
    doLastResize(frameWidth - pnlTimeline.getFrame().SIDE_SPACE, g2);
    refresh(g2);
    Dimension newPanelSize = new Dimension(frameWidth - pnlTimeline.getFrame().FRAME_SIDE_SPACE, pnlTimeline.getHeight());
    Rectangle scrollRect = new Rectangle(0, 0, frameWidth - pnlTimeline.getFrame().FRAME_SIDE_SPACE, 0);
    pnlTimeline.setSize(newPanelSize);
    pnlTimeline.setPreferredSize(newPanelSize);
    pnlTimeline.setMinimumSize(newPanelSize);
    addTimepoint(getPlayerDuration()); // add a bubble by adding the given end point
    Enumeration enumer = topBubbleNode.preorderEnumeration();
    BubbleTreeNode currNode = (BubbleTreeNode)enumer.nextElement();
    while (enumer.hasMoreElements()) {
      currNode = (BubbleTreeNode)enumer.nextElement();
      Bubble currBubble = currNode.getBubble();
      currBubble.setColor(bubbleLevelColors[currBubble.getLevel()]);
    }
    drawResizer();
    blowBubbles();
    updateLevels();
    pnlTimeline.refreshTimeline();
  }

  /**
   * resizeTimeline: resizes the timeline to the given length
   */
  protected void resizeTimeline (int len, Graphics2D graphics) {
    int oldLength = getLineLength();
    setLineLength(len);
    setLineEnd(length + start);

    // change rect and slider length, hide time mark
    rect = new Rectangle(start, yLoc - totalBubbleHeight, length, totalBubbleHeight);

    // adjust the position of the slider in the panel
    pnlTimeline.add(slide, new XYConstraints(start - offset, yLoc,
        length + (offset * 2), slide.getHeight()));
    slide.setSize(length + (offset * 2), slide.getHeight());
    showTime(false);

    // update timepoint list
    respaceTimepointsAndMarkers(oldLength, length);

    if (!pnlTimeline.isLineResizing()) {
      doLastResize(len, g2d);
    }
  }

  /**
   * showTime: formats and displays the time label below the playback head
   */
  protected void showTime(boolean show) {
    if (show) {
      // set up the formatting
      Font labelFont = UIUtilities.fontUnicode;
      g2d.setFont(labelFont);
      FontMetrics labelMetrics = g2d.getFontMetrics();
      // determine the current location (time)
      int value = (int)((float)slide.getValue() * ((float)length / (float)slide.getMaximum()));

      // display the time label
      String str = UIUtilities.convertOffsetToHoursMinutesSecondsTenths(slide.getValue());
      lblThumb.setText(str);
      int currX = start + value - (labelMetrics.stringWidth(str) / 2);
      pnlTimeline.add(lblThumb, new XYConstraints (currX, yLoc + 36, -1, -1 ));
      lblThumb.setVisible(true);
    }
    else {
      lblThumb.setVisible(false);
    }
  }

  /**
   * toElement: creates and returns an XML element representing the timeline
   */
  public org.w3c.dom.Element toElement(org.w3c.dom.Document doc) throws Exception{
    // timeline element
    org.w3c.dom.Element timelineElement = doc.createElement("Timeline");
    // title attribute
    timelineElement.setAttribute("title", pnlTimeline.getFrame().getTitle());
    // description attribute
    timelineElement.setAttribute("description", description);
    // bubble height attribute
    timelineElement.setAttribute("bubbleHeight", String.valueOf(this.bubbleHeight));
    // bubble type attribute
    timelineElement.setAttribute("bubbleType", String.valueOf(this.bubbleType));
    // black and white attribute
    timelineElement.setAttribute("BWBubbles", String.valueOf(this.blackAndWhite));
    // editable attribute
    timelineElement.setAttribute("editable", String.valueOf(this.editable));
    // resizable attribute
    timelineElement.setAttribute("resizable", String.valueOf(this.resizable));
    // background color attribute
    timelineElement.setAttribute("bgColor", String.valueOf(pnlTimeline.getPanelColor().getRed()) + ","
                                 + String.valueOf(pnlTimeline.getPanelColor().getGreen()) + ","
                                 + String.valueOf(pnlTimeline.getPanelColor().getBlue()));
    // show timepoint times attribute
    timelineElement.setAttribute("visibleTimes", String.valueOf(this.showTimepointTimes));

    // media attributes
    timelineElement.setAttribute("mediaOffset", String.valueOf(getPlayerStartOffset()));
    timelineElement.setAttribute("mediaLength", String.valueOf(getPlayerDuration()));
    timelineElement.setAttribute("mediaContent", getPlayerContent());
    timelineElement.setAttribute("clickPlay", String.valueOf(this.playWhenBubbleClicked));
    timelineElement.setAttribute("stopPlay", String.valueOf(this.stopPlayingAtSelectionEnd));
    timelineElement.setAttribute("autoscale", String.valueOf(this.autoScalingOn));

    // level colors element
    org.w3c.dom.Element levelColorsElement = doc.createElement("LevelColors");
    for (int i = 1; i < this.defaultBubbleLevelColors.length; i++) {
      org.w3c.dom.Element levelColorElement = doc.createElement("LevelColor");
      levelColorElement.appendChild(doc.createTextNode(String.valueOf(bubbleLevelColors[i].getRed()) + "," +
          String.valueOf(bubbleLevelColors[i].getGreen()) + "," +
          String.valueOf(bubbleLevelColors[i].getBlue()))) ;
      levelColorsElement.appendChild(levelColorElement);
    }

    // needed to ensure compatibility with 2.0: three bogus level colors
    for (int i = 0; i < 3; i++) {
      org.w3c.dom.Element levelColorElement = doc.createElement("LevelColor");
      levelColorElement.appendChild(doc.createTextNode("255,255,255")) ;
      levelColorsElement.appendChild(levelColorElement);
    }

    timelineElement.appendChild(levelColorsElement);

    // bubble tree element
    TimelineXMLAdapter tim = new TimelineXMLAdapter();
    tim.processBubbleTreeToXML(doc, timelineElement, this, this.getBubbleNode(0));

    // timepoint list element
    org.w3c.dom.Element timepointListElement = doc.createElement("Timepoints");
    for (int i = 0; i < this.numBaseBubbles; i++) {
      Timepoint currTimepoint = ((Timepoint)Timepoints.elementAt(i));
      org.w3c.dom.Element timepointElement = currTimepoint.toElement(doc, sortedPointList[i]);
      timepointListElement.appendChild(timepointElement);
    }
    // add final timepoint
    org.w3c.dom.Element timepointElement = doc.createElement("Timepoint");
    timepointElement.setAttribute("offset", String.valueOf(getPlayerDuration()));
    timepointListElement.appendChild(timepointElement);

    timelineElement.appendChild(timepointListElement);

    // marker list element
    org.w3c.dom.Element markerListElement = doc.createElement("Markers");
    for (int i = 0; i < numMarkers; i++) {
      Marker currMarker = ((Marker)Markers.elementAt(i));
      org.w3c.dom.Element markerElement = currMarker.toElement(doc, markerList[i]);
      markerListElement.appendChild(markerElement);
    }

    timelineElement.appendChild(markerListElement);

    return timelineElement;
  }

  /**
   * toElementForHTML: creates and returns an XML element representing the timeline, as used for the web page
   */
  public org.w3c.dom.Element toElementForHTML(org.w3c.dom.Document doc) throws Exception{
    // timeline element
    org.w3c.dom.Element timelineElement = doc.createElement("Timeline");
    // title attribute
    timelineElement.setAttribute("title", pnlTimeline.getFrame().getTitle());
    // description attribute
    timelineElement.setAttribute("description", description);
    // media attributes
    timelineElement.setAttribute("mediaOffset", String.valueOf(getPlayerStartOffset()));
    timelineElement.setAttribute("mediaLength", String.valueOf(getPlayerDuration()));
    timelineElement.setAttribute("mediaContent", getPlayerContent());
    // bubble tree element
    TimelineXMLAdapter tim = new TimelineXMLAdapter();
    tim.bubCount = 0;
    tim.processBubbleTreeToXMLForHTML(doc, timelineElement, this, this.getBubbleNode(0), 0);

    // timepoint list element
    org.w3c.dom.Element timepointListElement = doc.createElement("Timepoints");
    for (int i = 0; i < this.numBaseBubbles; i++) {
      Timepoint currTimepoint = ((Timepoint)Timepoints.elementAt(i));
      org.w3c.dom.Element timepointElement = currTimepoint.toElement(doc, sortedPointList[i]);
      timepointListElement.appendChild(timepointElement);
    }
    // add final timepoint
    org.w3c.dom.Element timepointElement = doc.createElement("Timepoint");
    timepointElement.setAttribute("offset", String.valueOf(getPlayerDuration()));
    timepointListElement.appendChild(timepointElement);

    timelineElement.appendChild(timepointListElement);

    // marker list element
//    if (numMarkers > 0) {
      org.w3c.dom.Element markerListElement = doc.createElement("Markers");
      for (int i = 0; i < numMarkers; i++) {
        Marker currMarker = ((Marker)Markers.elementAt(i));
        org.w3c.dom.Element markerElement = currMarker.toElement(doc, markerList[i]);
        markerListElement.appendChild(markerElement);
//      }

      timelineElement.appendChild(markerListElement);
    }
    return timelineElement;
  }

  /**
   * toExcerpt: creates and returns an XML element representing an excerpt of timeline
   * recieves as input the start and end timepoint numbers
   */
  public org.w3c.dom.Element toExcerpt(org.w3c.dom.Document doc, int startPoint, int endPoint) throws Exception{
    // timeline element
    org.w3c.dom.Element timelineElement = doc.createElement("Timeline");
    // title attribute
    timelineElement.setAttribute("title", pnlTimeline.getFrame().getTitle() + " (Excerpt)");
    // bubble height attribute
    timelineElement.setAttribute("bubbleHeight", String.valueOf(this.bubbleHeight));
    // bubble type attribute
    timelineElement.setAttribute("bubbleType", String.valueOf(this.bubbleType));
    // black and white attribute
    timelineElement.setAttribute("BWBubbles", String.valueOf(this.blackAndWhite));
    // editable attribute
    timelineElement.setAttribute("editable", String.valueOf(true)); // excerpt is always editable
    // resizable attribute
    timelineElement.setAttribute("resizable", String.valueOf(this.resizable));
    // bubble type attribute
    timelineElement.setAttribute("bubbleType", String.valueOf(this.bubbleType));
    // background color attribute
    timelineElement.setAttribute("bgColor", String.valueOf(pnlTimeline.getPanelColor().getRed()) + ","
                                 + String.valueOf(pnlTimeline.getPanelColor().getGreen()) + ","
                                 + String.valueOf(pnlTimeline.getPanelColor().getBlue()));
    // show timepoint times attribute
    timelineElement.setAttribute("visibleTimes", String.valueOf(this.showTimepointTimes));

    // media attributes -- adjust to selected subset
    int newOffset, offsetShift;
    newOffset = getPlayerStartOffset() + sortedPointList[startPoint];
    offsetShift = newOffset - getPlayerStartOffset();
    int newDuration = sortedPointList[endPoint] - sortedPointList[startPoint];
    timelineElement.setAttribute("mediaOffset", String.valueOf(newOffset));
    timelineElement.setAttribute("mediaLength", String.valueOf(newDuration));
    timelineElement.setAttribute("mediaContent", getPlayerContent());
    timelineElement.setAttribute("clickPlay", String.valueOf(this.playWhenBubbleClicked));
    timelineElement.setAttribute("stopPlay", String.valueOf(this.stopPlayingAtSelectionEnd));
    timelineElement.setAttribute("autoscale", String.valueOf(this.autoScalingOn));

    // level colors element
    org.w3c.dom.Element levelColorsElement = doc.createElement("LevelColors");
    for (int i = 1; i < this.bubbleLevelColors.length; i++) {
      org.w3c.dom.Element levelColorElement = doc.createElement("LevelColor");
      levelColorElement.appendChild(doc.createTextNode(String.valueOf(bubbleLevelColors[i].getRed()) + "," +
          String.valueOf(bubbleLevelColors[i].getGreen()) + "," +
          String.valueOf(bubbleLevelColors[i].getBlue()))) ;
      levelColorsElement.appendChild(levelColorElement);
    }
    timelineElement.appendChild(levelColorsElement);

    // needed to ensure compatibility with 2.0: three bogus level colors
    for (int i = 0; i < 3; i++) {
      org.w3c.dom.Element levelColorElement = doc.createElement("LevelColor");
      levelColorElement.appendChild(doc.createTextNode("255, 255, 255")) ;
      levelColorsElement.appendChild(levelColorElement);
    }

    // bubble tree element
    TimelineXMLAdapter tim = new TimelineXMLAdapter();
    int startPixels = sortedPixelList[startPoint] + start;
    int endPixels = sortedPixelList[endPoint] + start;
    tim.processBubbleTreeExcerptToXML(doc, timelineElement, this, this.getBubbleNode(0), startPixels, endPixels);

    // timepoint list element
    org.w3c.dom.Element timepointListElement = doc.createElement("Timepoints");
    for (int i = startPoint; i <= endPoint; i++) {
      if (i == numBaseBubbles) {
        org.w3c.dom.Element timepointElement = doc.createElement("Timepoint");
        timepointElement.setAttribute("offset", String.valueOf(newDuration));
        timepointListElement.appendChild(timepointElement);
      }
      else {
        Timepoint currTimepoint = ((Timepoint)Timepoints.elementAt(i));
        org.w3c.dom.Element timepointElement = currTimepoint.toElement(doc, sortedPointList[i] - offsetShift);
        timepointListElement.appendChild(timepointElement);
      }
    }
    timelineElement.appendChild(timepointListElement);

    // marker list element
    org.w3c.dom.Element markerListElement = doc.createElement("Markers");
    for (int i = 0; i < numMarkers; i++) {
      if ((markerList[i] >= sortedPointList[startPoint]) && (markerList[i] <= sortedPointList[endPoint])) {
        Marker currMarker = ((Marker)Markers.elementAt(i));
        org.w3c.dom.Element markerElement = currMarker.toElement(doc, markerList[i] - offsetShift);
        markerListElement.appendChild(markerElement);
      }
    }
    if (numMarkers > 0) {
      timelineElement.appendChild(markerListElement);
    }
    return timelineElement;
  }

  /**********************************
   * methods having to do with bubbles
   **********************************/

  /**
   * addBubble: adds a bubble to the timeline, respacing the current base bubbles to equal widths
   */
  protected void addBubble() {
    makeDirty();
    numBaseBubbles++;
    numTotalBubbles++;

    // space bubbles equally
    for (int i = 0; i <= numBaseBubbles; i++) {
      sortedPointList[i] = ((i * getPlayerDuration()) / numBaseBubbles);
    }

    // sort list and add new elements
    sortList();
    Bubble bubble = new Bubble();
    bubble.setLevel(1);
    bubble.setColor(bubbleLevelColors[1]);
    treeModel.insertNodeInto(new BubbleTreeNode(bubble), topBubbleNode, topBubbleNode.getChildCount());

    Timepoint timepoint = new Timepoint();
    Timepoints.addElement(timepoint.getTimepoint());

    // deselect all bubbles
    clearSelectedBubbles();
  }

  /**
   * blowBubbles: draws the bubbles
   */
  protected void blowBubbles() {
    int bStart, bEnd;

    // first set the start and end points of the base bubbles
    BubbleTreeNode currBaseBub = topBubbleNode.getFirstLeaf();

    for (int i = 0; i < numBaseBubbles; i++) {

      // determine the bounds of the bubble
      bStart = sortedPixelList[i] + start;
      bEnd = sortedPixelList[i + 1] + start;

      // set the points
      Bubble bubble = currBaseBub.getBubble();
      bubble.setStart(bStart);
      bubble.setEnd(bEnd);

      // advance to next bubble node
      currBaseBub = currBaseBub.getNextLeaf();
    }

    // now set the actual bubble height for drawing purposes if auto-scaling is on
    double drawBubbleHeight;
    if (autoScalingOn) {
      // for each three base bubbles, delete a pixel from the height
      drawBubbleHeight = bubbleHeight - (topBubbleNode.getLeafCount() / 3);

      // scale the bubble height to the length of the line -- reference length is 1000 pixels
      drawBubbleHeight = drawBubbleHeight * ((double)length / 1000);

      // smallest possible height for a base bubble is smallestBubbleHeight pixels
      if (drawBubbleHeight < smallestBubbleHeight) {
        drawBubbleHeight = smallestBubbleHeight;
      }
    }
    else {
      drawBubbleHeight = bubbleHeight;
    }

    // if the timeline is zoomed, adjust the height of the bubbles to show the zoomed selections
    if (timelineZoomed) {
      int bubbleDrawSpace = pnlTimeline.getFrame().scrollPane.getHeight() - bottomSpace - 25;
      if ((drawBubbleHeight * topZoomLevel) > bubbleDrawSpace) {
        drawBubbleHeight = bubbleDrawSpace / topZoomLevel;
      }
    }

    // now draw the bubbles in a pre-order traversal
    Enumeration enumer = topBubbleNode.preorderEnumeration();
    BubbleTreeNode currNode = (BubbleTreeNode)enumer.nextElement(); // load root bubble
    int currTimepoint = 0;
    int groupBubbleHeight = 0;

    int startBubble = 0;
    int endBubble = numTotalBubbles;

    for (int i = startBubble; i < endBubble; i++) {

      // get next bubble in enumeration
      currNode = (BubbleTreeNode)enumer.nextElement();
      Bubble currBubble = currNode.getBubble();

      // if it is a grouping bubble
      if (currBubble.getLevel() > 1 && !currNode.isLeaf()) {

        // get start and end points
        bStart = currNode.getFirstLeafBubble().start;
        bEnd = currNode.getLastLeafBubble().end;

        // determine the height for round bubbles
        // scales height by adding one pixel height for each 50 pixels wide
        if (bubbleType == 0) {
          groupBubbleHeight = (int)drawBubbleHeight + (((bEnd - bStart)/50) * 1);
        }
        else {
          groupBubbleHeight = (int)drawBubbleHeight;
        }
        // draw the group bubble
        currBubble.drawBubble(g2d, bStart, yLoc, bEnd, groupBubbleHeight, bubbleType, blackAndWhite);
      }

      else { // it is a base bubble
        // draw the bubble
        currBubble.drawBubble(g2d, currBubble.getStart(), yLoc, currBubble.getEnd(), (int)drawBubbleHeight, bubbleType, blackAndWhite);

        // create a timepoint
        Timepoint timepoint = (Timepoint)Timepoints.elementAt(currTimepoint);
        timepoint.setLabelFontSize(currBubble.getFontSize());

        // draw the timepoint
        if (timepointOffLine && currTimepoint == this.lastTimepointClicked) {
          timepoint.showLabel(false);
          timepoint.showTime(false);
          timepoint.drawTimepoint(g2d, dragMouseX, dragMouseY, pnlTimeline.getBackground());
        }
        else if (this.draggingTimepoint && currTimepoint == this.lastTimepointClicked) {
          timepoint.showLabel(false);
          timepoint.showTime(true);
          timepoint.drawTimepoint(g2d, currBubble.getStart(), yLoc, pnlTimeline.getBackground());
        }
        else {
          timepoint.showLabel(true);
          timepoint.showTime(showTimepointTimes);
          timepoint.drawTimepoint(g2d, currBubble.getStart(), yLoc, pnlTimeline.getBackground());
        }

        // advance the timepoint count
        currTimepoint++;
      }
    }

    // now add the markers
    int currMarker = 0;
    for (int j = 0; j < numMarkers; j++) {
      Marker marker = (Marker)Markers.elementAt(currMarker);
      marker.setLabelFontSize(getTimepoint(0).getLabelFontSize());

      if (markerOffLine && currMarker == this.lastMarkerClicked) {
        marker.showLabel(false);
        marker.showTime(false);
        marker.drawMarker(g2d, dragMouseX, dragMouseY, pnlTimeline.getBackground());
      }
      else if (this.draggingMarker && currMarker == this.lastMarkerClicked) {
        marker.showLabel(true);
        marker.showTime(true);
        marker.drawMarker(g2d, markerPixelList[j] + start, yLoc, pnlTimeline.getBackground());
      }
      else {
        marker.showLabel(true);
        marker.showTime(showTimepointTimes);
        marker.drawMarker(g2d, markerPixelList[j] + start, yLoc, pnlTimeline.getBackground());
      }
      // advance the marker count
      currMarker++;
    }

    // update timeline rect with current number of bubble levels
    totalBubbleHeight = getHighestBubbleHeight();
    rect = new Rectangle(start, yLoc - totalBubbleHeight, length, totalBubbleHeight);

    // draw the timeline title and description
    if (!titleIsHidden) {
      drawTitle();
      drawDescriptionIcon(g2d, currDescriptionState);
    }
  }

  /**
   * clearSelectedBaseBubbles: removes all items from the selectedBaseBubbles vector only
   */
  protected void clearSelectedBaseBubbles() {
    selectedBaseBubbles.removeAllElements();
  }

  /**
   * clearSelectedBoxBubbles: removes all items from the selectedBoxBubbles vector
   */
  protected void clearSelectedBoxBubbles() {
    selectedBoxBubbles.removeAllElements();
  }

  /**
   * clearSelectedBubbles: removes all items from all selectedBubbles vectors
   */
  protected void clearSelectedBubbles() {
    selectedBubbles.removeAllElements();
    selectedBaseBubbles.removeAllElements();
  }

  /**
   * deleteSelectedBubbles: deletes the currently selected bubbles
   * timepoints are only deleted if the bubble deleted is a base bubble
   * returns true if the delete took place, false otherwise
   */
  protected boolean deleteSelectedBubbles() {
    // local variables
    boolean deleteTookPlace = false;
    Bubble currBubble;
    int currBaseBubbleNum;
    boolean deletingBaseBubble = false;
    int startPointDeleted = 0;
    // create a vector of bubble nodes to delete
    Vector nodesToDelete = new Vector();
    BubbleTreeNode nextNode;

    for (int i = 0; i < selectedBubbles.size(); i++) {
      nextNode = getBubbleNode(((Integer)selectedBubbles.elementAt(i)).intValue());
      nodesToDelete.addElement(nextNode);
    }
    // now cycle through the nodes in the vector nodes and delete each one
    for (int i = 0; i < nodesToDelete.size(); i++) {

      BubbleTreeNode currNode = (BubbleTreeNode)nodesToDelete.elementAt(i);
      if (currNode != null) {
        currBubble = currNode.getBubble();

        // don't allow deletion of the (invisible) root bubble or the first base bubble
        if (!currNode.isRoot() && currBubble != topBubbleNode.getFirstLeafBubble()) {

          // if this is a base bubble, delete the timepoint, etc.
          if (currNode.isLeaf()) {
            deletingBaseBubble = true;
            startPointDeleted = currBubble.getStart();
            currBaseBubbleNum = topBubbleNode.getLeafIndex(currNode);
            pnlTimeline.undoOffsets.addElement(Integer.valueOf(sortedPointList[currBaseBubbleNum]));
            pnlTimeline.undoTimepointLabels.addElement(getTimepoint(currBaseBubbleNum).getLabel());
            pnlTimeline.undoBaseColors.addElement(currNode.getBubble().getColor());
            pnlTimeline.undoBaseLabels.addElement(currNode.getBubble().getLabel());
            pnlTimeline.undoBaseAnnotations.addElement(currNode.getBubble().getAnnotation());

            // delete the timepoint if it is not being dragged
            if (!draggingTimepoint) {
              Timepoints.removeElementAt(currBaseBubbleNum);
            }

            // shift the list points one to the left
            for (int j = currBaseBubbleNum; j < numTotalBubbles; j++) {
              sortedPointList[j] = sortedPointList[j + 1];
              sortedPixelList[j] = sortedPixelList[j + 1];
            }

            // decrease the number of base bubbles
            numBaseBubbles--;

            // find possible additional deletions since a timepoint was removed
            Enumeration enumer = topBubbleNode.preorderEnumeration();
            nextNode = (BubbleTreeNode)enumer.nextElement();
            Bubble nextBubble;

            // cycle through the nodes to find the ones affected by the deletion
            for (int k = 0; k < numTotalBubbles; k++) {
              nextNode = (BubbleTreeNode)enumer.nextElement();
              if (nextNode != null) {
                nextBubble = nextNode.getBubble();
                // if a higher-level node has a boundary at the deleted timepoint, add it to the vector of nodes to delete
                if (nextBubble.getLevel() > 1 && !nextNode.isLeaf()
                    && (nextBubble.getStart() == startPointDeleted || nextBubble.getEnd() == startPointDeleted) && !nodesToDelete.contains(nextNode)) {
                  nodesToDelete.addElement(nextNode);
                }
              }
            }
          }

          // set up undo of group bubble deletion
          Bubble currBub = currNode.getBubble();
          if (currBub.getLevel() > 1 && !currNode.isLeaf()) {
            pnlTimeline.undoGroupStarts.addElement(Integer.valueOf(currBub.getStart()));
            pnlTimeline.undoGroupEnds.addElement(Integer.valueOf(currBub.getEnd()));
            pnlTimeline.undoGroupColors.addElement(currBub.getColor());
            pnlTimeline.undoGroupLabels.addElement(currBub.getLabel());
            pnlTimeline.undoGroupAnnotations.addElement(currBub.getAnnotation());
          }

          // back up parents of current bubble
          Vector parents = new Vector();
          Vector parentStarts = new Vector();
          Vector parentEnds = new Vector();
          BubbleTreeNode parentNode = currNode.getParentNode();
          while (!parentNode.isRoot()) {
            Bubble parentBubble = parentNode.getBubble();
            Bubble backupBubble = new Bubble(parentBubble);
            backupBubble.setLabel(parentBubble.getLabel());
            backupBubble.setAnnotation(parentBubble.getAnnotation());
            parents.addElement(backupBubble);
            parentStarts.addElement(Integer.valueOf(parentBubble.start));
            parentEnds.addElement(Integer.valueOf(parentBubble.end));
            parentNode = parentNode.getParentNode();
          }

          // finally delete the current node from the tree
          int numDeleted = currNode.delete(); 	  // returns the number of deleted bubbles, since deleting one bubble may result in the deletion of others
          treeModel.reload();
          deleteTookPlace = true;

          // decrease the total number of bubbles
          numTotalBubbles = numTotalBubbles - numDeleted;

          // now set up undo of any parents automatically deleted
          for (int m = 0; m < numDeleted - 1; m++) {
            Bubble undoBubble = (Bubble)parents.elementAt(m);
            pnlTimeline.undoGroupStarts.addElement(parentStarts.elementAt(m));
            pnlTimeline.undoGroupEnds.addElement(parentEnds.elementAt(m));
            pnlTimeline.undoGroupColors.addElement(undoBubble.getColor());
            pnlTimeline.undoGroupLabels.addElement(undoBubble.getLabel());
            pnlTimeline.undoGroupAnnotations.addElement(undoBubble.getAnnotation());
          }
          // update grouping bubble levels
          updateLevels();
        }
      }
    }
    // now sort the list and deselect all bubbles
    sortList();
    clearSelectedBubbles();
    deselectAllBubbles();
    pnlControl.updateAnnotationPane();
    pnlControl.lblDuration.setText("");

    if (deleteTookPlace){
      makeDirty();
      return true;
    }
    else return false;
  }

  /**
   * deselectAllBubbles: marks each bubble in the Bubbles vector as deselected
   */
  protected void deselectAllBubbles() {
    Enumeration enumer = topBubbleNode.preorderEnumeration();
    BubbleTreeNode currNode = (BubbleTreeNode)enumer.nextElement(); // load root bubble

    for (int i = 0; i < numTotalBubbles; i++) {
      currNode = (BubbleTreeNode)enumer.nextElement();
      Bubble currBubble = currNode.getBubble();
      currBubble.deselect();
    }
    pnlTimeline.disableAllBubbleControls();
    pnlControl.lblDuration.setText("");
  }

  /**
   * getBubble: returns a reference to the requested bubble
   * the bubbles are numbered according to a pre-order traversal
   */
  protected Bubble getBubble(int bubNum) {
    Enumeration enumer = topBubbleNode.preorderEnumeration();
    BubbleTreeNode currNode = (BubbleTreeNode)enumer.nextElement(); // load root bubble

    if (bubNum <= numTotalBubbles) {
      for (int i = 0; i < bubNum; i++) {
        currNode = (BubbleTreeNode)enumer.nextElement();
      }
      return currNode.getBubble();
    }
    else {
      return null;
    }
  }

  /**
   * getBubbleNode: returns a reference to the requested BubbleTreeNode
   * the bubbles are numbered according to a pre-order traversal
   */
  protected BubbleTreeNode getBubbleNode(int bubNum) {
    Enumeration enumer = topBubbleNode.preorderEnumeration();
    BubbleTreeNode currNode = (BubbleTreeNode)enumer.nextElement(); // load root bubble

    if (bubNum <= numTotalBubbles) {
      for (int i = 0; i < bubNum; i++) {
        currNode = (BubbleTreeNode)enumer.nextElement();
      }
      return currNode;
    }
    else {
      return null;
    }
  }

  /**
   * getBaseBubble: returns a reference to the requested base bubble
   */
  protected Bubble getBaseBubble(int bubNum) {
    if (bubNum < topBubbleNode.getLeafCount()) {
      BubbleTreeNode currNode = topBubbleNode.getFirstLeaf();
      for (int i = 0; i < bubNum; i++) {
        currNode = currNode.getNextLeaf();
      }
      return currNode.getBubble();
    }
    else {
      return null;
    }
  }

  /**
   * getBaseBubbleNode: returns a reference to the requested base BubbleTreeNode
   */
  protected BubbleTreeNode getBaseBubbleNode(int bubNum) {
    if (bubNum < topBubbleNode.getLeafCount()) {
      BubbleTreeNode currNode = topBubbleNode.getFirstLeaf();
      for (int i = 0; i < bubNum; i++) {
        currNode = currNode.getNextLeaf();
      }
      return currNode;
    }
    else {
      return null;
    }
  }

  /**
   * getBubbleClicked: passed a point p, returns the number of the bubble that was clicked
   */
  protected int getBubbleClicked(Point p) {
    int startBubble = numTotalBubbles;

    // query bubbles from the bottom up
    for (int i = startBubble; i > 0; i--) {
      Bubble currBubble = getBubble(i);
      if (currBubble.contains(p)) {
        return i;
      }
    }
    return -1; // if no bubble was clicked
  }

  /**
   * getBaseBubbleClicked: passed a point p, returns the number of the base bubble that was clicked
   */
  protected int getBaseBubbleClicked(Point p) {

    for (int i = 0; i < numBaseBubbles; i++) {
      Bubble currBubble = getBaseBubble(i);
      if (currBubble.contains(p)) {
        return i;
      }
    }
    return -1; // if no base bubble was clicked
  }

  /**
   * lastImportantOffsetIsMarker: returns true if the offset that precedes the current offset
   * is a marker; also sets the previousMarker variable
   */
  protected boolean lastImportantOffsetIsMarker() {
    if (numMarkers == 0 || previousMarkerOffset < 0) {
      return false;
    }
    int currOffset = getPlayerOffset();
    int currBaseBub = getBaseBubbleNumAtCurrentOffset()-1;
    if (currBaseBub >= 0) {
      int previousTimepoint = sortedPointList[getBaseBubbleNumAtCurrentOffset()-1];
      int previousMarker = markerList[previousMarkerOffset];
      if (previousMarker >= previousTimepoint) {
        return true;
      }
    }
    return false;
  }

  /**
   * getBaseBubbleNumAtCurrentOffset: returns the number of the base bubble that is aligned
   * with the current offset on the slider (also sets the nextOffset and nextMarkerOffset variable)
   */
  protected int getBaseBubbleNumAtCurrentOffset() {
    int offset = getPlayerOffset();
    for (int i = 0; i < numBaseBubbles + 1; i++) {
      if (sortedPointList[i] > offset) {
        nextOffset = i;
        if (!editingBubbles) {
          setNextImportantOffset(Math.min(sortedPointList[nextOffset], markerList[nextMarkerOffset]));
        }
        if (this.stopPlayingAtSelectionEnd) {
          if (!selectedBubbles.isEmpty()) {
            int pixelStart = getBubble(((Integer)selectedBubbles.firstElement()).intValue()).getStart();
            int pixelEnd = getBubble(((Integer)selectedBubbles.lastElement()).intValue()).getEnd();
            setLocalStartOffset(getOffsetAt(getTimepointNumberAtPixel(pixelStart)));
            setLocalEndOffset(getOffsetAt(getTimepointNumberAtPixel(pixelEnd)));
          }
          else {
            setLocalEndOffset(getPlayerDuration());
            setLocalStartOffset(0);
          }
        }
        return i;
      }
    }
    return -1;
  }

  /**
   * getSelectedBubbleText: if the selected bubbles have the same text, or if a single bubble has
   * text, returns the text, else returns an empty string
   */
  protected String getSelectedBubbleText() {
    String bubbleText = getBubble(((Integer)selectedBubbles.elementAt(0)).intValue()).getLabel();
    for (int i = 1; i < selectedBubbles.size(); i++) {
      if (getBubble(((Integer)selectedBubbles.elementAt(i)).intValue()).getLabel() != bubbleText) {
        return "";
      }
    }
    return bubbleText;
  }

  /**
   * getSelectedLevels: returns a vector of the selected levels
   */
  protected Vector<Integer> getSelectedLevels() {
    Vector<Integer> selectedLevels = new Vector<Integer>();

    // determine the selected levels
    for (int i = 0; i < selectedBubbles.size(); i++) {
      Integer lev = Integer.valueOf(getBubble(((Integer)selectedBubbles.elementAt(i)).intValue()).getLevel());
      if (!selectedLevels.contains(lev)) {
        selectedLevels.addElement(lev);
      }
    }
    return selectedLevels;
  }

  /**
   * getSelectionRange: returns an array of two ints: the start and end timepoint indexes
   * of the selected range of bubbles
   */
  protected int[] getSelectionRange() {
    int first = 5000; // dummy values
    int last = 0;
    for (int i = 0; i < selectedBubbles.size(); i++) {
      Bubble currBubble = getBubble(((Integer)selectedBubbles.elementAt(i)).intValue());
      if (currBubble.getStart() < first) {
        first = currBubble.getStart();
      }
      if (currBubble.getEnd() > last) {
        last = currBubble.getEnd();
      }
    }
    int startIndex = -1;
    int endIndex = -1;
    for (int i = 0; i <= numBaseBubbles; i++) {
      if (sortedPixelList[i] == (first - start)) {
        startIndex = i;
      }
      else if (sortedPixelList[i] == (last - start)) {
        endIndex = i;
      }
    }
    int[] range = {startIndex, endIndex};
    return range;
  }

  /**
   * goToNextBubble: moves the slider and playback to the beginning of the next bubble
   */
  protected void goToNextBubble() {
    int currPos = getPlayerOffset();
    for (int i = 0; i < numBaseBubbles; i++) {
      if (sortedPointList[i] - 250 > currPos) {
        slide.setValue(sortedPointList[i]);
        setPlayerOffset(sortedPointList[i]);
        if (showTimepointTimes) {
          showTime(false);
        }
        else {
          showTime(true);
        }
        return;
      }
    }
  }

  /**
   * goToPreviousBubble: moves the slider and playback to the beginning of the previous bubble
   */
  protected void goToPreviousBubble() {
    int currPos = getPlayerOffset();
    for (int i = numBaseBubbles - 1; i >= 0; i--) {
      if (sortedPointList[i] + 250 < currPos) {
        slide.setValue(sortedPointList[i]);
        setPlayerOffset(sortedPointList[i]);
        if (showTimepointTimes) {
          showTime(false);
        }
        else {
          showTime(true);
        }
        return;
      }
    }
  }

  /**
   * groupSelectedBubbles: groups the selected bubbles within a higher level bubble
   * returns false if the bubbles cannot be grouped
   */
  protected boolean groupSelectedBubbles() {
    if (selectedBubbles.size() > 1) {
      sortSelectedBubbles();
      // get the start and ending points of the grouping bubble
      int startPosition = (((Integer)selectedBubbles.elementAt(0)).intValue());
      int endPosition = (((Integer)selectedBubbles.elementAt(selectedBubbles.size() - 1)).intValue());

      int startPoint = getBubble(startPosition).start;
      int endPoint = getBubble(endPosition).end;

      // check to make sure the bubble does not overlap grouping boundaries
      if (overlapsBubbleBoundaries(startPoint, endPoint)) {
        return false;
      }
      else if (!isDuplicateBubble(startPoint, endPoint)) {
        makeDirty();
        // create the grouping bubble and locate its parent, children, and index
        Bubble groupBubble = new Bubble();
        BubbleTreeNode groupNode = new BubbleTreeNode((Object)groupBubble);
        BubbleTreeNode highestLevelChildNode = getHighestLevelNodeWithin(startPoint, endPoint);
        BubbleTreeNode groupParentNode = highestLevelChildNode.getParentNode();
        BubbleTreeNode firstChildNode = getChildStartingAt(groupParentNode, startPoint);
        BubbleTreeNode lastChildNode = getChildEndingAt(groupParentNode, endPoint);
        int groupIndex = groupParentNode.getIndex(firstChildNode);
        int numGroupedChildren = groupParentNode.getIndex(lastChildNode) - groupIndex + 1;

        // find all of the children and link them to this parent grouping bubble
        BubbleTreeNode currNode = firstChildNode;
        BubbleTreeNode nextNode;
        for (int i = 0; i < numGroupedChildren; i++) {
          nextNode = currNode.getNextSibling();
          treeModel.insertNodeInto(currNode, groupNode, i);
          treeModel.reload();
          currNode = nextNode;
        }

        // add the grouping bubble to its own parent
        treeModel.insertNodeInto(groupNode, groupParentNode, groupIndex);
        treeModel.reload();

        numTotalBubbles++;

        // set grouping bubble properties
        int currLevel = highestLevelChildNode.getBubble().getLevel() + 1;
        groupBubble.setLevel(currLevel);
        groupBubble.setColor(bubbleLevelColors[currLevel]);
        groupBubble.start = startPoint;
        groupBubble.end = endPoint;

        // update root and intervening parent levels
        setParentLevels(groupNode);
      }
    }

    // deselect all bubbles
    deselectAllBubbles();
    clearSelectedBubbles();
    return true;
  }

  /**
   * isBubbleGroupSelected: returns true if the bubbles selected are in the same bubble group
   */
  protected boolean isBubbleGroupSelected() {
    sortSelectedBubbles();

    // get the start and ending points of the potential bubble group
    int startPoint = getBubble((((Integer)selectedBubbles.elementAt(0)).intValue())).start;
    int endPoint = getBubble((((Integer)selectedBubbles.elementAt(selectedBubbles.size() - 1)).intValue())).end;

    // now check for a grouping bubble with those endpoints
    if (isDuplicateBubble(startPoint, endPoint)) {
      return true;
    }
    else {
      return false;
    }
  }

  /**
   * moveSelectedBubblesDown: bumps the selected bubbles down a level
   */
  protected void moveSelectedBubblesDown(){
    for (int i = 0; i < selectedBubbles.size(); i++) {
      int currBub = ((Integer)selectedBubbles.elementAt(i)).intValue();
      Bubble bub = getBubble(currBub);
      BubbleTreeNode currNode = this.getBubbleNode(currBub);
      boolean hasRoom = true;
      for (int j = 0; j < currNode.getChildCount(); j++) {
        if (((BubbleTreeNode)currNode.getChildAt(j)).getBubble().getLevel()==(bub.level-1)) {
          hasRoom = false;
        }
      }
      if (bub.level > 1 && hasRoom) {
        if (bub.getColor().equals(bubbleLevelColors[bub.level])) {
          bub.setColor(bubbleLevelColors[bub.level-1]);
        }
        bub.setLevel(bub.level - 1);
        bub.levelWasUserAdjusted = true;
        pnlTimeline.undoManager.undoableEditHappened(new UndoableEditEvent(pnlTimeline,
            new UndoableMoveSelectedBubblesDown(selectedBubbles, this)));
        pnlTimeline.updateUndoMenu();
        // update the popup menus
        updatePopupMenus();
      }
    }
  }

  /**
   * moveSelectedBubblesUp: bumps the selected bubbles up a level
   */
  protected void moveSelectedBubblesUp(){
    for (int i = 0; i < selectedBubbles.size(); i++) {
      int currBub = ((Integer)selectedBubbles.elementAt(i)).intValue();
      Bubble bub = getBubble(currBub);
      BubbleTreeNode currNode = this.getBubbleNode(currBub);
      BubbleTreeNode parentNode = (BubbleTreeNode)currNode.getParent();
      if (parentNode.isRoot()) { // move root up if it is in the way
        parentNode.getBubble().setLevel(parentNode.getBubble().getLevel()+1);
      }
      if (parentNode.getBubble().getLevel()>(bub.getLevel()+1)) { // now check for room
        if (bub.getColor().equals(bubbleLevelColors[bub.level])) {
          bub.setColor(bubbleLevelColors[bub.level+1]);
        }
        bub.setLevel(bub.level + 1);
        bub.levelWasUserAdjusted = true;
        pnlTimeline.undoManager.undoableEditHappened(new UndoableEditEvent(pnlTimeline,
            new UndoableMoveSelectedBubblesUp(selectedBubbles, this)));
        pnlTimeline.updateUndoMenu();
        // update the popup menus
        updatePopupMenus();
      }
    }
  }

  /**
   * selectBubble: mark the bubble passed as selected
   */
  protected void selectBubble(int bubbleClicked, int selectType) {
    Integer bClicked = Integer.valueOf(bubbleClicked);
    Bubble checkBubble = getBubble(bubbleClicked);
    BubbleTreeNode currNode = getBubbleNode(bubbleClicked);

    // switch by the selection type
    switch (selectType) {

      // single click
      case 0:
        // check to see if they are re-clicking on a single selected bubble, in which case deselect
        if (selectedBubbles.size() == 1 && selectedBubbles.contains(bClicked)) {
          clearSelectedBubbles();
          anchorBubble = -1;
        }
        else { // otherwise clear list and select the single bubble clicked, setting it as the anchor click
          clearSelectedBubbles();
          selectedBubbles.add(bClicked);

          if (currNode.isLeaf()) {
            selectedBaseBubbles.add(Integer.valueOf(topBubbleNode.getLeafIndex(currNode)));
          }

          anchorBubble = bubbleClicked;
        }
        break;

        // shift click -- clear list and add elements from the anchor to the current selection
        // multiple level shift click selects only intervening elements on the levels involved
      case 1:
        clearSelectedBubbles();
        if (anchorBubble == -1 || anchorBubble == bubbleClicked) {
          anchorBubble = bubbleClicked;
          selectedBubbles.add(bClicked);
          if (currNode.isLeaf()) {
            selectedBaseBubbles.add(Integer.valueOf(topBubbleNode.getLeafIndex(currNode)));
          }
        }
        else {
          // get the levels involved
          int anchorLevel = getBubble(anchorBubble).getLevel();
          int clickLevel = getBubble(bubbleClicked).getLevel();
          int highLevel = (anchorLevel >= clickLevel) ? anchorLevel : clickLevel;
          int lowLevel = (anchorLevel <= clickLevel) ? anchorLevel : clickLevel;

          // get the start and end points of the region involved
          int anchorStart = getBubble(anchorBubble).getStart();
          int clickStart = getBubble(bubbleClicked).getStart();
          int anchorEnd = getBubble(anchorBubble).getEnd();
          int clickEnd = getBubble(bubbleClicked).getEnd();
          int startPoint = (anchorStart <= clickStart) ? anchorStart : clickStart;
          int endPoint = (anchorEnd >= clickEnd) ? anchorEnd : clickEnd;

          // now select the bubbles in the affected region
          Enumeration enumer = topBubbleNode.preorderEnumeration();
          BubbleTreeNode node = (BubbleTreeNode)enumer.nextElement();

          for (int i = 1; i <= numTotalBubbles; i++) {
            node = (BubbleTreeNode)enumer.nextElement();
            Bubble bub = node.getBubble();
            if (bub.getLevel() >= lowLevel && bub.getLevel() <= highLevel && bub.getStart() >= startPoint && bub.getEnd() <= endPoint) {
              selectedBubbles.add(Integer.valueOf(i));
              if (node.isLeaf()) {
                selectedBaseBubbles.add(Integer.valueOf(topBubbleNode.getLeafIndex(node)));
              }
            }
          }
        }
        break;

        // control click -- add current selection to the previous list of selections
      case 2:
        if (selectedBubbles.contains(bClicked)) {
          selectedBubbles.removeElement(bClicked);

          if (currNode.isLeaf()) {
            selectedBaseBubbles.removeElement(Integer.valueOf(topBubbleNode.getLeafIndex(currNode)));
          }
        }
        else {
          selectedBubbles.add(bClicked);

          if (currNode.isLeaf()) {
            selectedBaseBubbles.add(Integer.valueOf(topBubbleNode.getLeafIndex(currNode)));
          }
        }
        if (selectedBubbles.size() == 1) {
          anchorBubble = bubbleClicked;
        }
        break;

        // "list click" -- just add the selection (called from selections in the timepoint list)
      case 3:
        // check to see if they are re-clicking on a single selected bubble, in which case deselect
        if (selectedBubbles.size() == 1 && checkBubble.isSelected()) {
          clearSelectedBubbles();
        }
        else {
          // nothing
        }
        break;

        // drag selection click
      case 4:
        if (selectedBubbles.contains(bClicked)) {
          // do nothing
        }
        else {
          selectedBubbles.add(bClicked);

          if (currNode.isLeaf()) {
            selectedBaseBubbles.add(new Integer(topBubbleNode.getLeafIndex(currNode)));
          }
        }

        // playback selection
      case 5:
        if (selectedBubbles.contains(bClicked)) {
          // do nothing
        }
        else {
          selectedBubbles.add(bClicked);

          if (currNode.isLeaf()) {
            selectedBaseBubbles.add(new Integer(topBubbleNode.getLeafIndex(currNode)));
          }
        }

      default:
        break;
    }

    // make a list of the item number selected
    int items[] = new int[selectedBaseBubbles.size()];

    // deselect any selected bubbles
    deselectAllBubbles();

    // cycle through the bubbles and select the selected ones
    // also calculate the total selected duration
    if (selectedBubbles.size() != 0) {
      int currBubble = ((Integer)selectedBubbles.elementAt(0)).intValue();
      BubbleTreeNode currParent = getBubbleNode(currBubble);
      int startOffset = sortedPointList[getTimepointNumberAtPixel(currParent.getFirstLeafBubble().start)];
      int endOffset = sortedPointList[getTimepointNumberAtPixel(currParent.getLastLeafBubble().end)];
      int totalDuration = endOffset-startOffset;
      sortSelectedBubbles();
      log.debug(startOffset);

      for (int i = 0; i < selectedBubbles.size(); i ++) {
        currBubble = ((Integer)selectedBubbles.elementAt(i)).intValue();
        Bubble bubClicked = getBubble(currBubble);
        bubClicked.select();

        // calculate duration
        BubbleTreeNode thisNode = getBubbleNode(currBubble);
        if (i > 0 && !currParent.isNodeDescendant(thisNode)) { // current bubble is not included in last parent
          currParent = thisNode;
          startOffset = sortedPointList[getTimepointNumberAtPixel(currParent.getFirstLeafBubble().start)];
          endOffset = sortedPointList[getTimepointNumberAtPixel(currParent.getLastLeafBubble().end)];
          int currDuration = endOffset-startOffset;
          totalDuration = totalDuration + currDuration;
        }
      }
      if (totalDuration != 0) {
        pnlControl.lblDuration.setText("Duration of Selection: " + TimelineUtilities.convertOffsetToHoursMinutesSecondsTenths(totalDuration));
      }
    }
    else {
      pnlControl.lblDuration.setText("");
    }

    // cycle through the selectedBaseBubbles to set selected list items
    for (int i = 0; i < selectedBaseBubbles.size(); i++) {
      int currBaseBubble = ((Integer)selectedBaseBubbles.elementAt(i)).intValue();
      items[i] = currBaseBubble;
    }

    if (selectType != 5) { // to avoid a loop
      pnlControl.updateAnnotationPane();
    }

    // enable the pertinent bubble buttons in the control panel
    if (selectedBubbles.size() == 1) {
      pnlTimeline.enableSingleSelectedBubbleControls();
    }
    else if (selectedBubbles.size() > 1) {
      if (this.isBubbleGroupSelected()) {
        pnlTimeline.enableMultipleSelectedGroupedBubbleControls();
      }
      else {
        pnlTimeline.enableMultipleSelectedUngroupedBubbleControls();
      }
    }

    if (!editable) {
      if (selectedBubbles.size() > 0) {
        pnlControl.btnZoomTo.setEnabled(true);
        pnlTimeline.menubTimeline.menuiZoomTo.setEnabled(true);
        pnlTimeline.menubTimeline.menuiCreateExcerpt.setEnabled(true);
      }
      else {
        pnlControl.btnZoomTo.setEnabled(false);
        pnlTimeline.menubTimeline.menuiZoomTo.setEnabled(false);
        pnlTimeline.menubTimeline.menuiCreateExcerpt.setEnabled(false);
      }
    }

    // update the popup menus
    updatePopupMenus();
  }

  /**
   * selectBubbles: selects all of the bubbles in the given rect
   */
  protected void selectBubbles(Rectangle selectionRect, boolean fromBelow) {
    anchorBubble = -1;
    for (int i = 1; i <= numTotalBubbles; i++) {
      Integer bub = new Integer(i);
      Bubble bubble = getBubble(i);
      BubbleTreeNode node = getBubbleNode(i);
      Area a = new Area(bubble.getArc());
      // calculate the displayed area of a bubble by subtracting its children
      // (this is a slower method, so only use it if the rectangle is dragged from below)
      if (fromBelow) {
        int numChildren = node.getChildCount();
        for (int j = 0; j < numChildren; j++) {
          Area b = new Area((((BubbleTreeNode)node.getChildAt(j)).getBubble()).getArc());
          a.subtract(b);
        }
      }
      if (a.intersects(selectionRect)) {
        bubble.select();
        selectBubble(i, 4);
        if (!selectedBoxBubbles.contains(bub)) {
          selectedBoxBubbles.add(bub);
        }
      }
      else if (selectedBoxBubbles.contains(bub)) {
        selectedBubbles.remove(bub);
        selectedBoxBubbles.remove(bub);
        bubble.deselect();
      }
    }
    if (selectedBubbles.isEmpty()) {
      pnlTimeline.disableAllBubbleControls();
    }

    // update the popup menus
    updatePopupMenus();
  }

  /**
   * setBubbleColor: sets the color of the bubble at bubbleNum to newColor
   */
  protected void setBubbleColor(int bubbleNum, Color newColor) {
    makeDirty();
    getBubble(bubbleNum).setColor(newColor);
  }

  /**
   * setBubbleLabel: sets the label of the bubble at bubbleNum to bubbleLabel
   */
  protected void setBubbleLabel(int bubbleNum, String bubbleLabel) {
    makeDirty();
    getBubble(bubbleNum).setLabel(bubbleLabel);
  }

  /**
   * setItemSelected: sets as selected the base bubble whose number is passed here
   * used by the timepointlist to select list items
   */
  protected void setItemSelected(Integer i) {
    int indexedItem = topBubbleNode.getPreOrderIndex(getBaseBubbleNode(i.intValue()));
    selectedBubbles.addElement(Integer.valueOf(indexedItem));
    selectedBaseBubbles.addElement(Integer.valueOf(i.intValue()));
  }

  /**
   * setLevelColor: sets the color of all bubbles at a given level
   */
  protected void setLevelColor(int currLevel, Color newColor) {
    makeDirty();
    // change the default color for this level
    bubbleLevelColors[currLevel] = newColor;

    // now change the color of existing bubbles at this level
    Enumeration enumer = topBubbleNode.preorderEnumeration();
    BubbleTreeNode currNode = (BubbleTreeNode)enumer.nextElement();

    for (int i = 0; i < numTotalBubbles; i++) {
      currNode = (BubbleTreeNode)enumer.nextElement();
      Bubble currBubble = currNode.getBubble();
      if (currBubble.getLevel() == currLevel) {
        currBubble.setColor(newColor);
      }
    }
  }

  /**
   * setSelectedBubbleAnnotation: sets the annotation of any selected bubbles
   */
  public void setSelectedBubbleAnnotation(String str) {
    for (int i = 0; i < selectedBubbles.size(); i++) {
      getBubble(((Integer)selectedBubbles.elementAt(i)).intValue()).setAnnotation(str);
    }
  }

  /**
   * setSelectedBubbles: sets the bubbles in the vector passed
   */
  protected void setSelectedBubbles(Vector sb) {
    selectedBubbles.clear();
    deselectAllBubbles();
    for (int i = 0; i < sb.size(); i++) {
      selectedBubbles.addElement(sb.elementAt(i));
    }
    // update the popup menus
    updatePopupMenus();
  }

  /**
   * setSelectedBubbleColor: sets the color for all currently selected bubbles
   */
  protected void setSelectedBubbleColor(Color c) {
    makeDirty();
    for (int i = 0; i < selectedBubbles.size(); i++) {
      int currSel = ((Integer)selectedBubbles.elementAt(i)).intValue();
      Bubble currBubble = getBubble(currSel);
      currBubble.setColor(c);
      currBubble.hasCustomColor = true;
    }
    // once the change color action has been performed, deselect the bubbles
    this.clearSelectedBubbles();
    this.deselectAllBubbles();
  }

  /**
   * setSelectedBubbleLabel: sets the text of any selected bubbles to the indicated text
   */
  protected void setSelectedBubbleLabel(String bubbleText) {
    makeDirty();
    for (int i = 0; i < selectedBubbles.size(); i++) {
      getBubble(((Integer)selectedBubbles.elementAt(i)).intValue()).setLabel(bubbleText);
    }
  }

  /**
   * setSelectedLevelColor: sets the color of all bubbles at a given level
   */
  protected void setSelectedLevelColor(Color newColor) {
    makeDirty();
    Vector<Integer> selectedLevels = new Vector<Integer>();

    // determine the selected levels
    for (int i = 0; i < selectedBubbles.size(); i++) {
      Integer lev = Integer.valueOf(getBubble(((Integer)selectedBubbles.elementAt(i)).intValue()).getLevel());
      if (!selectedLevels.contains(lev)) {
        selectedLevels.addElement(lev);
        // change the default color for the selected levels
        bubbleLevelColors[lev.intValue()] = newColor;
      }
    }

    // now change the color of existing bubbles at this level
    Enumeration enumer = topBubbleNode.preorderEnumeration();
    BubbleTreeNode currNode = (BubbleTreeNode)enumer.nextElement();

    for (int i = 0; i < numTotalBubbles; i++) {
      currNode = (BubbleTreeNode)enumer.nextElement();
      Bubble currBubble = currNode.getBubble();
      if (selectedLevels.contains(Integer.valueOf(currBubble.getLevel()))) {
        currBubble.setColor(newColor);
      }
    }
  }

  /**
   * sortselectedBaseBubbles: sorts the vector SelectedBaseBubbles
   */
  private void sortSelectedBaseBubbles() {
    int items[] = new int[selectedBaseBubbles.size()];
    int loopNum = selectedBaseBubbles.size();

    for (int i = 0; i < loopNum; i ++) {
      int currBubble = ((Integer)selectedBaseBubbles.elementAt(i)).intValue();
      items[i] = currBubble;
    }
    Arrays.sort(items);
    clearSelectedBaseBubbles();

    for (int i = 0; i < loopNum; i++) {
      selectedBaseBubbles.add(Integer.valueOf(items[i]));
    }
  }

  /**
   * sortselectedBubbles: sorts the vector SelectedBubbles
   */
  protected void sortSelectedBubbles() {
    int items[] = new int[selectedBubbles.size()];
    int loopNum = selectedBubbles.size();

    for (int i = 0; i < loopNum; i ++) {
      int currBubble = ((Integer)selectedBubbles.elementAt(i)).intValue();
      items[i] = currBubble;
    }
    Arrays.sort(items);
    clearSelectedBubbles();

    for (int i = 0; i < loopNum; i++) {
      selectedBubbles.add(Integer.valueOf(items[i]));
    }
  }

  /**
   * ungroupSelectedBubbles: ungroups the selected bubbles if the selected bubbles are grouped
   * returns the deleted bubble
   */
  protected Bubble ungroupSelectedBubbles() {
    makeDirty();
    Bubble tempBub = null;
    if (selectedBubbles.size() > 1) {
      sortSelectedBubbles();

      // get the start and ending points of the grouping bubble
      int startPosition = (((Integer)selectedBubbles.elementAt(0)).intValue());
      int endPosition = (((Integer)selectedBubbles.elementAt(selectedBubbles.size() - 1)).intValue());

      int startPoint = getBubble(startPosition).start;
      int endPoint = getBubble(endPosition).end;

      // check to make sure the grouping bubble already exists
      if (isDuplicateBubble(startPoint, endPoint)) {

        BubbleTreeNode groupNode = getHighestLevelNodeWithin(startPoint, endPoint);
        Bubble groupBubble = groupNode.getBubble();
        tempBub = new Bubble(groupBubble);
        tempBub.setLabel(groupBubble.getLabel());
        tempBub.setAnnotation(groupBubble.getAnnotation());
        pnlTimeline.bubbleDeleted = topBubbleNode.getPreOrderIndex(groupNode);

        // now delete this bubble
        int numDeleted = groupNode.delete();
        treeModel.reload();
        numTotalBubbles = numTotalBubbles - numDeleted;
        updateLevels();
      }
    }

    // deselect all bubbles
    deselectAllBubbles();
    clearSelectedBubbles();
    return tempBub;
  }

  /**
   * zoomToSelectedBubbles: zooms to the range of bubbles delimited by the outmost selected bubbles
   */
  protected int zoomToSelectedBubbles() {
    float zoomFactor = 1;

    if (areBubblesSelected()) {
      sortSelectedBubbles();
      timelineZoomed = true;

      // get the start and ending timepoints of the zoom region
      int firstBubble = (((Integer)selectedBubbles.elementAt(0)).intValue());
      int lastBubble = (((Integer)selectedBubbles.elementAt(selectedBubbles.size() - 1)).intValue());
      BubbleTreeNode startNode = getBubbleNode(firstBubble);
      BubbleTreeNode endNode = getBubbleNode(lastBubble);
      int zoomStartPoint = startNode.getFirstLeafBubble().start;
      int zoomEndPoint = endNode.getLastLeafBubble().end;
      int zoomRange = zoomEndPoint - zoomStartPoint;
      zoomIndex = topBubbleNode.getLeafIndex(startNode.getFirstLeaf());

      // get top zoom level
      topZoomLevel = getHighestLevelNodeWithin(zoomStartPoint, zoomEndPoint).getBubble().getLevel();

      pnlTimeline.getFrame();
	// determine the zoom factor and return the new length
      float zoomWidth = pnlTimeline.getFrame().getWidth() - TimelineFrame.SIDE_SPACE;
      zoomFactor = (float)zoomWidth / (float)zoomRange;
    }
    return ((int)(length * zoomFactor));
  }


  /******************
   * Bubble Tree Utilities
   ******************/

  /**
   * getChildEndingAt: returns the child node of the passed parent that has the given end point
   */
  private BubbleTreeNode getChildEndingAt(BubbleTreeNode parentNode, int endPoint) {
    BubbleTreeNode currNode = parentNode.getFirstChildNode();
    for (int i = 0; i < parentNode.getChildCount(); i++) {
      if (currNode.getBubble().end == endPoint) {
        return currNode;
      }
      currNode = currNode.getNextSibling();
    }
    return null;
  }

  /**
   * getChildStartingAt: returns the child node of the passed parent that has the given starting point
   */
  private BubbleTreeNode getChildStartingAt(BubbleTreeNode parentNode, int startPoint) {
    BubbleTreeNode currNode = parentNode.getFirstChildNode();
    for (int i = 0; i < parentNode.getChildCount(); i++) {
      if (currNode.getBubble().start == startPoint) {
        return currNode;
      }
      currNode = currNode.getNextSibling();
    }
    return null;
  }

  /**
   * getFirstBaseBubbleNum: returns the pre-order index of the first base bubble
   */
  private int getFirstBaseBubbleNum() {
    Enumeration enumer = topBubbleNode.preorderEnumeration();
    BubbleTreeNode currNode = (BubbleTreeNode)enumer.nextElement(); // load root bubble

    for (int i = 0; i < numTotalBubbles; i++) {
      currNode = (BubbleTreeNode)enumer.nextElement();
      if (currNode.isLeaf()) {
        return i;
      }
    }
    return -1; // should never happen
  }

  /**
   * getFirstBubbleWithin: returns the number of the first bubble that starts at the given starting point
   * and ends before the given ending point
   */
  public int getFirstBubbleWithin(int startPoint, int endPoint) {
    Enumeration enumer = topBubbleNode.preorderEnumeration();
    BubbleTreeNode currNode = (BubbleTreeNode)enumer.nextElement(); // load root bubble

    for (int i = 1; i < numTotalBubbles; i++) {
      currNode = (BubbleTreeNode)enumer.nextElement();
      Bubble currBubble = currNode.getBubble();
      if (currBubble.getStart() == startPoint && currBubble.getEnd() <= endPoint) {
        return i;
      }
    }
    return -1; // should never happen
  }

  /**
   * getHighestBubbleHeight: returns the height of the tallest bubble
   */
  protected int getHighestBubbleHeight () {
    Enumeration enumer = topBubbleNode.preorderEnumeration();
    BubbleTreeNode currNode = (BubbleTreeNode)enumer.nextElement(); // load root bubble
    int highestBubbleHeight = 0;

    for (int i = 0; i < numTotalBubbles; i++) {
      currNode = (BubbleTreeNode)enumer.nextElement();
      Bubble currBubble = currNode.getBubble();
      if (currBubble.getHeight() > highestBubbleHeight) {
        highestBubbleHeight = currBubble.getHeight();
      }
    }
    return highestBubbleHeight;
  }

  /**
   * getHighestLevelNodeWithin: returns the node of the bubble that has the highest
   * level within the given range (from startPoint to endPoint)
   */
  protected BubbleTreeNode getHighestLevelNodeWithin (int startPoint, int endPoint) {
    Enumeration enumer = topBubbleNode.preorderEnumeration();
    BubbleTreeNode currNode = (BubbleTreeNode)enumer.nextElement(); // load root bubble
    BubbleTreeNode highestLevelBubbleNode = null;
    int highestLevel = 0;

    for (int i = 0; i < numTotalBubbles; i++) {
      currNode = (BubbleTreeNode)enumer.nextElement();
      Bubble currBubble = currNode.getBubble();
      if (currBubble.start >= startPoint && currBubble.end <= endPoint) {
        if (currBubble.getLevel() > highestLevel) {
          highestLevel = currBubble.getLevel();
          highestLevelBubbleNode = currNode;
        }
      }
    }
    return highestLevelBubbleNode;
  }

  /**
   * getLastBubbleWithin: returns the number of the last bubble that starts after the given starting point
   * and ends at the given ending point
   */
  public int getLastBubbleWithin(int startPoint, int endPoint) {
    Enumeration enumer = topBubbleNode.preorderEnumeration();
    BubbleTreeNode currNode = (BubbleTreeNode)enumer.nextElement(); // load root bubble
    int lastBubble = 1;

    for (int i = 1; i <= numTotalBubbles; i++) {
      currNode = (BubbleTreeNode)enumer.nextElement();
      Bubble currBubble = currNode.getBubble();
      if (currBubble.getStart() >= startPoint && currBubble.getEnd() == endPoint) {
        lastBubble = i;
      }
    }
    return lastBubble;
  }

  /**
   * isDuplicateBubble: returns true if the endpoints given would duplicate an
   * existing grouping bubble. Can be used to determine if ungrouping should be done
   */
  private boolean isDuplicateBubble(int startPoint, int endPoint) {
    Enumeration enumer = topBubbleNode.preorderEnumeration();
    BubbleTreeNode currNode = (BubbleTreeNode)enumer.nextElement(); // load root bubble

    for (int i = 0; i < numTotalBubbles; i++) {
      currNode = (BubbleTreeNode)enumer.nextElement();
      Bubble currBubble = currNode.getBubble();
      if (currBubble.getLevel() > 1 && !currNode.isLeaf()) {
        if (currBubble.start == startPoint && currBubble.end == endPoint) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * overlapsBubbleBoundaries: returns true if the range given would overlap with existing
   * grouping bubbles. Bubbles that are completely within or completely enclose another
   * bubble do not overlap with it. Overlapping refers to the crossing of boundaries by
   * adjacent bubbles.
   */
  private boolean overlapsBubbleBoundaries(int startPoint, int endPoint) {
    Enumeration enumer = topBubbleNode.preorderEnumeration();
    BubbleTreeNode currNode = (BubbleTreeNode)enumer.nextElement(); // load root bubble

    for (int i = 0; i < numTotalBubbles; i++) {
      currNode = (BubbleTreeNode)enumer.nextElement();
      Bubble currBubble = currNode.getBubble();
      if (currBubble.getLevel() > 1 && !currNode.isLeaf()) {
        if (currBubble.start < startPoint && currBubble.end > startPoint && currBubble.end < endPoint) {
          return true;
        }
        else if (currBubble.start > startPoint && currBubble.start < endPoint && currBubble.end > endPoint) {
          return true;
        }
      }
      currNode = currNode.getNextNode();
    }
    return false;
  }

  /**
   * setParentLevels: traverses the tree from the current node up, setting the levels higher if
   * a new level has been created. Only changes the bubble color if it is a default color
   */
  private void setParentLevels(BubbleTreeNode currNode) {
    int startLevel = currNode.getBubble().getLevel();
    int i = 1;
    int currLevel;
    int prevLevel;
    while (currNode.getParentNode() != null) {
      Bubble parentBubble = currNode.getParentBubble();
      currLevel = startLevel + i;
      prevLevel = parentBubble.getLevel();
      if (prevLevel < (currLevel)) {
        parentBubble.setLevel(currLevel);
        if (parentBubble.getColor().equals(bubbleLevelColors[prevLevel])) {
          parentBubble.setColor(bubbleLevelColors[currLevel]);
        }
        currNode = currNode.getParentNode();
        i++;
      }
      else return; // if the parent level is already higher, do not change it
    }
  }

  /**
   * updateLevels: compresses the bubble tree after deleting a bubble or timepoint
   */
  private void updateLevels() {
    Enumeration enumer = topBubbleNode.preorderEnumeration();
    BubbleTreeNode currNode = (BubbleTreeNode)enumer.nextElement(); // load root bubble
    currNode.getBubble().setLevel(currNode.getDepth() + 1);
    Bubble currBubble;

    for (int i = 0; i < numTotalBubbles; i++) {
      currNode = (BubbleTreeNode)enumer.nextElement();
      currBubble = currNode.getBubble();
      int oldLevel = currBubble.getLevel();
      int newLevel = currNode.getDepth() + 1;
      if (!currBubble.levelWasUserAdjusted) {
        currBubble.setLevel(newLevel);
        if (currBubble.getColor().equals(bubbleLevelColors[oldLevel])) {
          currBubble.setColor(bubbleLevelColors[newLevel]);
        }
      }
    }
  }

  /**********************************
   * methods having to do with timepoints and markers
   **********************************/

  /**
   * addMarker: adds a marker corresponding to the current slider position
   * returns an int representing the index of the marker
   */
  protected int addMarker(int newPoint) {
    makeDirty();
    // check to see if the marker already exists (duplicates not allowed)
    for (int i = 0; i < numMarkers; i++) {
      if ((markerList[i] / 100) == (newPoint / 100)) { // do not allow within a tenth of a second
        return -1;
      }
    }
    // increment, add to list, and sort list
    markerList[numMarkers] = newPoint;
    numMarkers++;
    sortList();

    // get marker position
    int markerPosition = 0;
    for (int i = 0; i < numMarkers; i++) {
      if (markerList[i] == newPoint) {
        markerPosition = i;
        break;
      }
    }

    // insert marker into list
    Marker marker = new Marker();
    Markers.insertElementAt(marker.getMarker(), markerPosition);

    // set the marker time
    String str = UIUtilities.convertOffsetToHoursMinutesSecondsTenths(newPoint);
    lblThumb.setText(str);
    marker.setTime(str);
    marker.showTime(showTimepointTimes);

    return markerPosition;
  }

  /**
   * addTimepoint: adds a specific point corresponding to the current slider position
   * returns an int representing the index of the timepoint
   */
  protected int addTimepoint(int newPoint) {
    makeDirty();
    // check to see if the point already exists (duplicates not allowed)
    for (int i = 0; i < numBaseBubbles; i++)
      if ((sortedPointList[i] / 100) == (newPoint / 100)) { // do not allow within a tenth of a second
    return -1;
      }

      // increment, add to list, and sort list
      numBaseBubbles++;
      numTotalBubbles++;
      sortedPointList[numBaseBubbles] = newPoint;
      sortList();

      // get bubble position
      int bubblePosition = 0;
      int timepointPosition;
      for (int i = 0; i < numBaseBubbles; i++) {
        if (sortedPointList[i] == newPoint) {
          bubblePosition = i;
          break;
        }
      }

      // apply settings of divided bubble to both resulting bubbles
      Bubble bubble;
      if (bubblePosition == 0) {
        bubble = new Bubble();
      }
      else {
        Bubble prevBubble = getBaseBubble(bubblePosition - 1);
        bubble = new Bubble(prevBubble);
      }
      bubble.setLevel(1);
      bubble.setColor(bubbleLevelColors[1]);

      // insert timepoint into list
      timepointPosition = bubblePosition;
      Timepoint timepoint = new Timepoint();
      Timepoints.insertElementAt(timepoint.getTimepoint(), timepointPosition);

      // set the timepoint time
      String str = UIUtilities.convertOffsetToHoursMinutesSecondsTenths(newPoint);
      lblThumb.setText(str);
      if (timepointPosition == 0) {
        timepoint.setTime("0");
      }
      else {
        timepoint.setTime(str);
      }
      timepoint.showTime(showTimepointTimes);


      // insert bubble into bubble tree
      BubbleTreeNode prevNode = getBaseBubbleNode(0);
      BubbleTreeNode bubNode = getBaseBubbleNode(0);
      if (bubblePosition > 0) {
        prevNode = getBaseBubbleNode(bubblePosition - 1);
        bubNode = getBaseBubbleNode(bubblePosition);
      }
      BubbleTreeNode bubbleParent = prevNode.getParentNode();
      if (bubbleParent == null) {
        treeModel.insertNodeInto(new BubbleTreeNode(bubble), bubNode, 0);
      }
      else {
        int bubIndex = bubbleParent.getIndex((TreeNode)prevNode) + 1;
        treeModel.insertNodeInto(new BubbleTreeNode(bubble), bubbleParent, bubIndex);
      }

      // deselect all bubbles
      clearSelectedBubbles();
      return bubblePosition;
  }

  /**
   * deleteMarker: deletes the passed marker
   */
  protected void deleteMarker(int markerNum) {
    makeDirty();
    // delete the marker if it is not being dragged
    if (!draggingMarker) {
      Markers.removeElementAt(markerNum);
    }

    // shift the list points one to the left
    for (int j = markerNum; j < numMarkers-1; j++) {
      markerList[j] = markerList[j + 1];
    }
    numMarkers--;
    sortList();
    this.deselectAllTimepointsAndMarkers();
  }

  /**
   * deleteTimepoint: deletes the passed timepoint
   */
  protected void deleteTimepoint(int timepointNum) {
    makeDirty();
    BubbleTreeNode currNode = getBaseBubbleNode(timepointNum);
    int bubbleIndex = topBubbleNode.getPreOrderIndex(currNode);
    selectBubble(bubbleIndex, 0);
    deleteSelectedBubbles();
    this.deselectAllTimepointsAndMarkers();
  }

  /**
   * deselectAllTimepointsAndMarkers: marks all timepoints in the Timepoints vector
   * and markers in the Markers vector as deselected
   */
  protected void deselectAllTimepointsAndMarkers() {
    for (int i = 0; i < numBaseBubbles; i++) {
      Timepoint point = (Timepoint)Timepoints.elementAt(i);
      point.deselect();
    }
    for (int i = 0; i < numMarkers; i++) {
      Marker marker = (Marker)Markers.elementAt(i);
      marker.deselect();
    }
    pnlTimeline.disableAllTimepointControls();
  }

  /**
   * dragMarker: this method handles a timepoint being dragged
   */
  protected void dragMarker (Point p) {
    if (!isDirty) {
      makeDirty();
    }
    dragMouseX = (int)p.getX();
    dragMouseY = (int)p.getY();
    draggingMarker = true;

    // determine the point within the panel
    Point panelPoint = new Point((int)p.getX() - start, (int)p.getY() - yLoc);

    /* simulate dragging */
    // allow dragging the marker the whole width of the timeline
    int prevX = sortedPixelList[0];
    int nextX = sortedPixelList[numBaseBubbles];
    int currMouseX = (int)panelPoint.getX();
    int currMouseY = (int)panelPoint.getY();
    if ((currMouseX > prevX) && (currMouseX < (nextX))) {

      // now check to see if the user has dragged the marker OFF of the timeline
      // that is, the mouse has moved substantially below or above the timeline
      // for it to be considered intential removal (30 pixels currently)
      if (currMouseY < -30 || currMouseY > 30) {
        if (markerOffLine == false) {
          // temporarily 'delete' the marker (allowing for undo if redragged to line)
          markerOffLine = true;                   // set a flag that the marker is off the line
        }

        // redraw
        getMarker(lastMarkerClicked).showTime(false);
        pnlTimeline.paintDraggingMarker();
        sortList();
      }
      else {
        // undo drag-off if one took place
        if (markerOffLine){
          markerOffLine = false;
          getMarker(lastMarkerClicked).showTime(true);
        }

        // adjust the point in the list to the current one, then sort and display the list
        int currOff = (int)((((float)panelPoint.getX()) / (float) length) * (float)mediaLength);
        markerList[lastMarkerClicked] = currOff;

        sortList();
        int oldPosition = lastMarkerClicked;
        // now check to see if the marker has changed positions
        if (markerList[lastMarkerClicked] != currOff) {
          for (int j = 0; j < markerList.length; j++) {
            if (markerList[j] == currOff) {
              lastMarkerClicked = j;
              Marker m = (Marker)Markers.elementAt(oldPosition);
              Markers.removeElementAt(oldPosition);
              Markers.insertElementAt(m, lastMarkerClicked);
            }
          }
        }
        pnlTimeline.paintDraggingMarker();
        getMarker(lastMarkerClicked).showTime(true);
        getMarker(lastMarkerClicked).select();
        showDragTime(currMouseX);
        showTime(false);
      }
    }
  }

  /**
   * dragTimepoint: this method determines how to redraw the bubbles when a timepoint is being dragged
   */
  protected void dragTimepoint (Point p) {
    if (!isDirty) {
      makeDirty();
    }
    dragMouseX = (int)p.getX();
    dragMouseY = (int)p.getY();
    draggingTimepoint = true;

    // determine the point within the panel
    Point panelPoint = new Point((int)p.getX() - start, (int)p.getY() - yLoc);

    /* simulate dragging */
    // don't move the initial timepoint
    if (lastTimepointClicked != 0) {

      // only drag the timepoint within the bounds of the surrounding timepoints
      int prevX = sortedPixelList[lastTimepointClicked - 1];
      int nextX = (lastTimepointClicked == numBaseBubbles) ? length : sortedPixelList[lastTimepointClicked + 1];
      int currMouseX = (int)panelPoint.getX();
      int currMouseY = (int)panelPoint.getY();
      int pixelBuffer = 4;
      if ((currMouseX > prevX + pixelBuffer) && (currMouseX < (nextX - pixelBuffer + 2))) {

        // now check to see if the user has dragged the timepoint OFF of the timeline
        // that is, the mouse has moved substantially below or above the timeline
        // for it to be considered intential removal (30 pixels currently)
        if (currMouseY < -30 || currMouseY > 30) {
          if (timepointOffLine == false) {
            // temporarily 'delete' the timepoint (allowing for undo if redragged to line)
            timepointOffLine = true;                   // set a flag that the timepoint is off the line
          }

          // redraw
          getTimepoint(lastTimepointClicked).showTime(false);
          pnlTimeline.paintDraggingTimepoint();
          sortList();
        }
        else {
          // undo drag-off if one took place
          if (timepointOffLine){
            timepointOffLine = false;
            getTimepoint(lastTimepointClicked).showTime(true);
          }

          // adjust the point in the list to the current one, then sort and display the list
          int currOff = (int)((((float)panelPoint.getX()) / (float) length) * (float)mediaLength);
          sortedPointList[lastTimepointClicked] = currOff;

          sortList();
          pnlTimeline.paintDraggingTimepoint();
          getTimepoint(lastTimepointClicked).showTime(true);
          getTimepoint(lastTimepointClicked).select();
          showDragTime(currMouseX);
          showTime(false);
        }
      }
    }
  }

  /**
   * getMarker: returns a reference to the requested marker
   */
  public Marker getMarker(int markerNum) {
    if (markerNum < 0 || markerNum > numMarkers) {
      return null;
    }
    return (Marker)Markers.elementAt(markerNum);
  }

  /**
   * getMarkerAt: passed a point p, returns the number of the marker at that point
   */
  protected int getMarkerAt(Point p) {
    for (int i = 0; i < numMarkers; i++) {
      Marker marker = (Marker)Markers.elementAt(i);
      if (marker.contains(p)) {
        return i;
      }
    }
    return -1; // no marker at this point
  }

  /**
   * getNextMarkerOffset: returns the next marker offset
   */
  protected int getNextMarkerOffset() {
    int offset = getPlayerOffset();
    for (int i = 0; i < numMarkers; i++) {
      if (markerList[i] >= offset) {
        nextMarkerOffset = i;
        previousMarkerOffset = i-1;
        return markerList[nextMarkerOffset];
      }
    }
    nextMarkerOffset = numMarkers;
    previousMarkerOffset = numMarkers - 1;
    return -1;
  }

  /**
   * getTimepoint: returns a reference to the requested timepoint
   */
  public Timepoint getTimepoint(int timepointNum) {
    if (timepointNum < 0 || timepointNum > numBaseBubbles) {
      return null;
    }
    return (Timepoint)Timepoints.elementAt(timepointNum);
  }

  /**
   * getTimepointAt: passed a point p, returns the number of the timepoint at that point
   */
  protected int getTimepointAt(Point p) {
    for (int i = 0; i < numBaseBubbles; i++) {
      Timepoint timepoint = (Timepoint)Timepoints.elementAt(i);
      if (timepoint.contains(p)) {
        return i;
      }
    }
    return -1; // no timepoint at this point
  }

  /**
   * markersContain: checks to see whether the point p is contained by any marker
   */
  protected boolean markersContain(Point p) {
    for (int i = 0; i < numMarkers; i++) {
      Marker marker = (Marker)Markers.elementAt(i);
      if (marker.contains(p)) {
        return true;
      }
    }
    return false;
  }

  /**
   * markerWasClicked: returns true if a marker was clicked. The number of the clicked
   * marker is stored in lastMarkerClicked
   */
  protected boolean markerWasClicked(Point p) {
    for (int i = 0; i < numMarkers; i++) {
      Marker marker = (Marker)Markers.elementAt(i);
      if (marker.contains(p)) {

        // now remember the current marker clicked
        lastMarkerClicked = i;
        return true;
      }
    }
    lastMarkerClicked = -1;
    return false;
  }

  /**
   * paintTimepointsAndMarkers: paints only the region with timepoints and markers in it
   */
  protected void paintTimepointsAndMarkers(Graphics2D g2) {
    // first set the start and end points of the base bubbles
    int currTimepoint = 0;
    for (int i = 0; i < numBaseBubbles; i++) {

      // draw the initiating timepoint
      Timepoint timepoint = (Timepoint)Timepoints.elementAt(currTimepoint);
      timepoint.setLabelFontSize(getBaseBubble(0).getFontSize());

      // hide overlapping timepoint labels and times
      if (currTimepoint > 0) {
        Timepoint prevTimepoint = (Timepoint)Timepoints.elementAt(currTimepoint - 1);
        if (timepoint.getLabelStart() < prevTimepoint.getLabelStart() + prevTimepoint.getLabelWidth()) {
          timepoint.showLabel(false);
        }
        else {
          timepoint.showLabel(true);
        }
        if (timepoint.getTimeStart() < prevTimepoint.getTimeStart() + prevTimepoint.getTimeWidth()) {
          timepoint.showTime(false);
        }
        else if (this.draggingTimepoint && currTimepoint == this.lastTimepointClicked) {
          timepoint.showTime(true);
        }
        else {
          timepoint.showTime(showTimepointTimes);
        }
      }

      // check to see if the timepoint is being dragged off the line
      if (timepointOffLine && lastTimepointClicked == currTimepoint) {
        timepoint.drawTimepoint(g2, dragMouseX, dragMouseY, pnlTimeline.getBackground());
      }
      else {
        timepoint.drawTimepoint(g2, sortedPixelList[i] + start, yLoc, pnlTimeline.getBackground());
      }
      // advance the timepoint count
      currTimepoint++;
    }
    // now add the markers
    int currMarker = 0;
    for (int j = 0; j < numMarkers; j++) {
      Marker marker = (Marker)Markers.elementAt(currMarker);
      marker.setLabelFontSize(getTimepoint(0).getLabelFontSize());

      // hide overlapping marker labels and times
      if (currMarker > 0) {
        Marker prevMarker = (Marker)Markers.elementAt(currMarker - 1);
        if (marker.getLabelStart() < prevMarker.getLabelStart() + prevMarker.getLabelWidth()) {
          marker.showLabel(false);
        }
        else if (markerOffLine && currMarker == this.lastMarkerClicked) {
          marker.showLabel(false);
        }
        else {
          marker.showLabel(true);
        }
        if (this.draggingMarker && currMarker == this.lastMarkerClicked) {
          marker.showTime(true);
        }
        else if (marker.getTimeStart() < prevMarker.getTimeStart() + prevMarker.getTimeWidth()) {
          marker.showTime(false);
        }
        else if (markerOffLine && currMarker == this.lastMarkerClicked) {
          marker.showTime(false);
        }
        else {
          marker.showTime(showTimepointTimes);
        }
      }
      // check to see if the marker is being dragged off the line
      if (markerOffLine && lastMarkerClicked == currMarker) {
        marker.drawMarker(g2d, dragMouseX, dragMouseY, pnlTimeline.getBackground());
      }
      else {
        marker.drawMarker(g2d, markerList[j], yLoc, pnlTimeline.getBackground());
      }
      // advance the marker count
      currMarker++;
    }
  }

  /**
   * respaceTimepointsAndMarkers: respaces the timepoints and markers when the line is resized, recieving as parameters the old length and the new length
   */
  private void respaceTimepointsAndMarkers(int oldLength, int newLength) {
    BubbleTreeNode currBaseBub = topBubbleNode.getFirstLeaf();
    for (int i = 0; i < numBaseBubbles; i++) {
      sortedPixelList[i] = ((TimelineSliderUI)slide.getUI()).xPositionForValue(sortedPointList[i], newLength - 4) - offset;

      // determine the bounds of the bubble
      int bStart = sortedPixelList[i] + start;
      int bEnd = sortedPixelList[i + 1] + start;

      // set the points
      Bubble bubble = currBaseBub.getBubble();
      bubble.setStart(bStart);
      bubble.setEnd(bEnd);

      // advance to next bubble node
      currBaseBub = currBaseBub.getNextLeaf();
    }
    sortedPixelList[numBaseBubbles] = newLength;

    for (int i = 0; i < numMarkers; i++) {
      markerPixelList[i] = ((TimelineSliderUI)slide.getUI()).xPositionForValue(markerList[i], newLength - 4) - offset;
    }
  }

  /**
   * selectMarker: mark the passed marker as selected
   */
  protected void selectMarker(int mSelected) {
    Marker markerClicked = (Marker)Markers.elementAt(mSelected);

    // deselect bubble selections
    deselectAllBubbles();

    if (markerClicked.isSelected()) {
      markerClicked.deselect();        // if it is already selected, deselect it
      pnlTimeline.disableAllTimepointControls();
      deselectAllTimepointsAndMarkers();
    }
    else {
      deselectAllTimepointsAndMarkers();    // deselect other markers
      deselectAllBubbles();
      clearSelectedBubbles();
      markerClicked.select();           // select this marker
      pnlTimeline.enableSelectedTimepointControls();
    }
    markerWasPassed = true;
    lastMarkerPassed = mSelected;
    setNextImportantOffset(markerList[mSelected]);
    setPlayerOffset(markerList[mSelected]);
    pnlControl.updateAnnotationPane();
  }

  /**
   * selectTimepoint: mark the passed timepoint as selected
   */
  protected void selectTimepoint(int tSelected) {
    Timepoint timepointClicked = (Timepoint)Timepoints.elementAt(tSelected);

    // deselect bubble selections
    deselectAllBubbles();

    if (timepointClicked.isSelected()) {
      timepointClicked.deselect();        // if it is already selected, deselect it
      pnlTimeline.disableAllTimepointControls();
      deselectAllTimepointsAndMarkers();
    }
    else {
      deselectAllTimepointsAndMarkers();    // deselect other timepoints
      deselectAllBubbles();
      clearSelectedBubbles();
      timepointClicked.select();           // select this timepoint
      pnlTimeline.enableSelectedTimepointControls();
    }
    setNextImportantOffset(sortedPointList[tSelected]);
    setPlayerOffset(sortedPointList[tSelected]);
    log.debug(sortedPointList[tSelected]);
    pnlControl.updateAnnotationPane();
  }

  /**
   * setMarkerAnnotation: sets the annotation  of the marker at markerNum to markerLabel
   */
  protected void setMarkerAnnotation(int markerNum, String markerAnnotation) {
    makeDirty();
    Marker marker = (Marker)Markers.elementAt(markerNum);
    marker.setAnnotation(markerAnnotation);
  }

  /**
   * setMarkerLabel: sets the label  of the marker at markerNum to markerLabel
   */
  protected void setMarkerLabel(int markerNum, String markerLabel) {
    makeDirty();
    Marker marker = (Marker)Markers.elementAt(markerNum);
    marker.setLabel(markerLabel);
  }

  /**
   * setSelectedMarkerAnnotation: sets the annotation of any selected markers
   */
  public void setSelectedMarkerAnnotation(String str) {
    if (this.areMarkersSelected()) {
      getMarker(lastMarkerClicked).setAnnotation(str);
    }
  }

  /**
   * setSelectedTimepointAnnotation: sets the annotation of any selected timepoints (inactive)
   */
  public void setSelectedTimepointAnnotation(String str) {
    if (this.areTimepointsSelected()) {
      getTimepoint(lastTimepointClicked).setAnnotation(str);
    }
  }

  /**
   * setTimepointLabel: sets the label  of the timepoint at timepointNum to timepointLabel
   */
  protected void setTimepointLabel(int timepointNum, String timepointLabel) {
    makeDirty();
    Timepoint timepoint = (Timepoint)Timepoints.elementAt(timepointNum);
    timepoint.setLabel(timepointLabel);
  }

  /**
   * showDragTime: formats and displays the current time below the dragging timepoint
   */
  protected void showDragTime(int value) {
    // determine the current location (time)
    double currValue = (((double)value / (double)length) * (mediaLength));

    // display the time label
    String str = UIUtilities.convertOffsetToHoursMinutesSecondsTenths((int)(currValue));
    if (draggingTimepoint) {
      getTimepoint(lastTimepointClicked).setTime(str);
    }
    else {
      getMarker(lastMarkerClicked).setTime(str);
    }
  }

  /**
   * showTimepointTimes: sets whether the times are shown below each timepoint and marker
   */
  protected void showTimepointTimes(boolean show) {
    showTimepointTimes = show;
    for (int i = 0; i < Timepoints.size(); i++) {
      getTimepoint(i).showTime(show);
    }
    for (int i = 0; i < Markers.size(); i++) {
      getMarker(i).showTime(show);
    }
  }

  /**
   * timepointsContain: checks to see whether the point p is contained by any timepoint,
   */
  protected boolean timepointsContain(Point p) {
    for (int i = 0; i < numBaseBubbles; i++) {
      Timepoint timepoint = (Timepoint)Timepoints.elementAt(i);
      if (timepoint.contains(p)) {
        return true;
      }
    }
    return false;
  }

  /**
   * timepointWasClicked: returns true if a timepoint was clicked. The number of the clicked
   * timepoint is stored in lastTimepointClicked
   */
  protected boolean timepointWasClicked(Point p) {
    for (int i = 0; i < numBaseBubbles; i++) {
      Timepoint timepoint = (Timepoint)Timepoints.elementAt(i);
      if (timepoint.contains(p)) {

        // now remember the current timepoint clicked
        lastTimepointClicked = i;
        return true;
      }
    }
    lastTimepointClicked = 0;
    return false;
  }

  /********************************
   * methods having to do with lists
   ********************************/

  /**
   * printList: a utility for viewing the current list of points
   */
  private void printList() {
    System.out.println("Num bubbles = " + numBaseBubbles);
    for (int i = 0; i <= numBaseBubbles; i++) {
      System.out.println(sortedPointList[i]);
      System.out.println(sortedPixelList[i]);
    }
  }

  /**
   * sortList: sorts the sortedPointList and markerList, creating the pixel lists
   */
  protected void sortList() {
    Arrays.sort(sortedPointList, 0, numBaseBubbles + 1);
    for (int i = 0; i <= numBaseBubbles; i++) {
      sortedPixelList[i] = ((TimelineSliderUI)slide.getUI()).xPositionForValue(sortedPointList[i], ((TimelineSliderUI)slide.getUI()).getTrackLength()) - offset;
    }

    Arrays.sort(markerList, 0, numMarkers);
    for (int i = 0; i < numMarkers; i++) {
      markerPixelList[i] = ((TimelineSliderUI)slide.getUI()).xPositionForValue(markerList[i], ((TimelineSliderUI)slide.getUI()).getTrackLength()) - offset;
    }
  }

  /*****************
   * various getters
   *****************/

  /**
   * returns bubble height
   */
  public int getBubbleHeight() {
    return bubbleHeight;
  }

  /**
   * returns bubble tree
   */
  protected JTree getBubbleTree() {
    return bubbleTree;
  }

  /**
   * returns bubble type (round or square)
   */
  public int getBubbleType() {
    return bubbleType;
  }

  /**
   * returns timeline description
   */
  public String getDescription() {
    return description;
  }

  /**
   * returns last marker clicked
   */
  public int getLastMarkerClicked() {
    return lastMarkerClicked;
  }

  /**
   * returns last marker passed
   */
  public int getLastMarkerPassed() {
    return lastMarkerPassed;
  }

  /**
   * returns last timepoint clicked
   */
  public int getLastTimepointClicked() {
    return lastTimepointClicked;
  }

  /**
   * returns timeline end in pixels
   */
  public int getLineEnd() {
    return end;
  }

  /**
   * returns timeline length in pixels
   */
  public int getLineLength() {
    return length;
  }

  /**
   * returns timeline start in pixels
   */
  public int getLineStart() {
    return start;
  }

  /**
   * returns y position of timeline
   */
  public int getLineY() {
    return yLoc;
  }

  /**
   * returns the list of markers
   */
  protected int[] getMarkerList() {
    return markerList;
  }

  /**
   * returns the marker number at the given offset
   */
  protected int getMarkerNumberAt(int offset) {
    for (int i = 0; i < numMarkers; i++) {
      if (markerList[i] == offset) {
        return i;
      }
    }
    return -1;
  }

  /**
   * returns the marker number at the given x location
   */
  protected int getMarkerNumberAtPixel(int pix) {
    for (int i = 0; i <= numMarkers; i++) {
      if (markerPixelList[i] == (pix - start)) {
        return i;
      }
    }
    return -1;
  }

  /**
   * returns the marker offset at the given marker number
   */
  protected int getMarkerOffsetAt(int loc) {
    return markerList[loc];
  }

  /**
   * returns the number of base bubbles
   */
  public int getNumBaseBubbles() {
    return numBaseBubbles;
  }

  /**
   * returns the number of markers
   */
  public int getNumMarkers() {
    return numMarkers;
  }

  /**
   * returns the total number of bubbles
   */
  public int getNumTotalBubbles() {
    return numTotalBubbles;
  }

  /**
   * returns true if black and white mode is active
   */
  protected boolean getBlackAndWhite() {
    return blackAndWhite;
  }

  /**
   * returns the timepoint offset at a given x location
   */
  protected int getOffsetAt(int loc) {
    return sortedPointList[loc];
  }

  /**
   * returns the timeline panel
   */
  protected TimelinePanel getPanel() {
    return pnlTimeline;
  }

  /**
   * returns the timeline rect (the bounding box in which the timeline is drawn)
   */
  public Rectangle getRect() {
    return rect;
  }

  /**
   * returns the timeline resizer
   */
  protected TimelineResizer getResizer() {
    return tResizer;
  }

  /**
   * returns the timeline slider
   */
  protected JSlider getSlider() {
    return slide;
  }

  /**
   * returns the list of sorted timepoints
   */
  protected int[] getSortedPointList() {
    return sortedPointList;
  }

  /**
   * returns a vector of selected bubbles
   */
  protected Vector getSelectedBubbles() {
    return selectedBubbles;
  }

  /**
   * returns a vector or selected base bubbles
   */
  protected Vector getSelectedBaseBubbles() {
    return selectedBaseBubbles;
  }

  /**
   * returns an int representing the current slider position (value)
   */
  public int getSliderPosition() {
    int value = (int)((float)slide.getValue() * ((float)length/(float)slide.getMaximum()));
    return value;
  }

  /**
   * returns the timepoint number at a given offset
   */
  protected int getTimepointNumberAt(int offset) {
    for (int i = 0; i < numTotalBubbles; i++) {
      if (sortedPointList[i] == offset) {
        return i;
      }
    }
    return -1;
  }

  /**
   * returns the timepoint number at a given x value
   */
  protected int getTimepointNumberAtPixel(int pix) {
    for (int i = 0; i <= numTotalBubbles; i++) {
      if (sortedPixelList[i] == (pix - start)) {
        return i;
      }
    }
    return -1;
  }

  /**
   * returns the total bubble height (height of the tallest bubble)
   */
  public int getTotalBubbleHeight() {
    return totalBubbleHeight;
  }

  /******************
   *  various checkers
   ******************/

  /**
   * returns true if any bubbles are selected
   */
  public boolean areBubblesSelected() {
    return (!selectedBubbles.isEmpty());
  }

  /**
   * returns true if any markers are selected
   */
  public boolean areMarkersSelected() {
    Marker currMarker = getMarker(lastMarkerClicked);
    if (currMarker != null) {
      return (currMarker.isSelected());
    }
    else return false;
  }

  /**
   * returns true if any timepoints are selected
   */
  public boolean areTimepointsSelected() {
    Timepoint currTimepoint = getTimepoint(lastTimepointClicked);
    if (currTimepoint != null) {
      return (currTimepoint.isSelected());
    }
    else return false;
  }

  /**
   * returns true if times are being shown below timepoints / markers
   */
  public boolean areTimesShown() {
    return showTimepointTimes;
  }

  /**
   * returns true if auto-scaling on resize is on
   */
  public boolean isAutoScalingOn() {
    return autoScalingOn;
  }

  /**
   * returns true if the given bubble is selected
   */
  public boolean isBubbleSelected(int bubNum) {
    return (selectedBubbles.contains((new Integer(bubNum))));
  }

  /**
   * returns true if the timeline is editable
   */
  public boolean isEditable() {
    return editable;
  }

  /**
   * returns true if a marker is being dragged
   */
  public boolean isMarkerDragging() {
    return draggingMarker;
  }

  /**
   * returns true if a marker has been dragged off the line
   */
  public boolean isMarkerOffLine() {
    return markerOffLine;
  }

  /**
   * returns true if the given marker is selected
   */
  public boolean isMarkerSelected(int markerNum) {
    if (markerNum < 0 || markerNum > numMarkers) {
      return false;
    }
    Marker marker = (Marker)Markers.elementAt(markerNum);
    return (marker.isSelected());
  }

  /**
   * returns true if the timeline is movable
   */
  public boolean isMovable() {
    return draggable;
  }

  /**
   * returns true if the timeline is resizable
   */
  public boolean isResizable() {
    return resizable;
  }

  /**
   * returns true if a timepoint is being dragged
   */
  public boolean isTimepointDragging() {
    return draggingTimepoint;
  }

  /**
   * returns true if a timepoint has been dragged off the line
   */
  public boolean isTimepointOffLine() {
    return timepointOffLine;
  }

  /**
   * returns true if the given timepoint is selected
   */
  public boolean isTimepointSelected(int timepointNum) {
    Timepoint point = (Timepoint)Timepoints.elementAt(timepointNum);
    return (point.isSelected());
  }

  /******************
   * various setters
   ******************/

  /**
   * sets auto-scaling on resize to true or false
   */
  protected void setAutoScaling(boolean state) {
    if (autoScalingOn != state) {
      makeDirty();
      autoScalingOn = state;
    }
  }

  /**
   * sets black and white mode to true or false
   */
  protected void setBlackAndWhite (boolean state) {
    if (blackAndWhite != state) {
      makeDirty();
      blackAndWhite = state;
    }
  }

  /**
   * sets the default bubble height to the given height
   */
  protected void setBubbleHeight(int bubbleH) {
    if (bubbleHeight != bubbleH) {
      makeDirty();
      bubbleHeight = bubbleH;
    }
  }

  /**
   * sets the bubble type to round (0) or square (1)
   */
  protected void setBubbleType(int bubbleT) {
    if (bubbleType != bubbleT) {
      makeDirty();
      bubbleType = bubbleT;
    }
  }

  /**
   * sets the timeline description to the given string
   */
  protected void setDescription(String desc) {
    makeDirty();
    description = desc;
  }

  /**
   * sets whether the timeline is editable to true or false
   */
  protected void setEditable(boolean state) {
    if (editable != state) {
      makeDirty();
      this.editable = state;
    }
  }

  /**
   * sets the timeline end (in pixels) to the given value
   */
  protected void setLineEnd(int lineEnd) {
    if (end != lineEnd) {
      // isDirty = true;
      end = lineEnd;
    }
  }

  /**
   * sets the timeline length (in pixels) to the given value
   */
  protected void setLineLength(int lineLength) {
    if (length != lineLength) {
      // isDirty = true;
      length = lineLength;
    }
  }

  /**
   * sets the line start (in pixels) to the given value
   */
  protected void setLineStart(int lineStart) {
    if (start != lineStart) {
      // isDirty = true;
      start = lineStart;
    }
  }

  /**
   * sets the line y location to the given value
   */
  protected void setLineY(int lineY) {
    if (yLoc != lineY) {
      // isDirty = true;
      yLoc = lineY;
    }
  }

  /**
   * sets the marker at the given marker number to the given offset
   */
  protected void setMarkerAt(int num, int offset) {
    markerList[num] = offset;
    sortList();
  }

  /**
   * signals that the user is dragging a marker
   */
  protected void setMarkerDragging (boolean b) {
    draggingMarker = b;
    getMarker(lastMarkerClicked).showTime(showTimepointTimes);
  }

  /**
   * signals that the marker has been dragged off the line
   */
  protected void setMarkerOffLine(boolean b) {
    markerOffLine = b;
  }

  /**
   * sets whether the timeline is movable to true or false
   */
  protected void setMovable(boolean state) {
    if (draggable != state) {
      makeDirty();
      this.draggable = state;
    }
  }

  /**
   * sets the timepoint at the given timepoint number to the given offset
   */
  protected void setPointAt(int num, int offset) {
    sortedPointList[num] = offset;
    sortList();
  }

  /**
   * sets whether the timeline is resizable to true or false
   */
  protected void setResizable(boolean state) {
    if (resizable != state) {
      makeDirty();
      this.resizable = state;
      tResizer.setEnabled(state);
    }
  }

  /**
   * sets the slider background color
   */
  protected void setSliderBackground(Color c) {
    if (slide != null) {
      slide.setBackground(c);
    }
  }

  /**
   *   signals that the user is dragging a timepoint
   */
  protected void setTimepointDragging (boolean b) {
    draggingTimepoint = b;
    getTimepoint(lastTimepointClicked).showTime(showTimepointTimes);
  }

  /**
   *   signals that the a timepoint has been dragged off the line
   */
  protected void setTimepointOffLine(boolean b) {
    timepointOffLine = b;
  }

  // player utilities

  /**
   * getPlayerDuration: returns the duration of the current player
   */
  protected int getPlayerDuration() {
    if (pnlTimeline.getFrame().isUsingLocalAudio) {
      return tLocalPlayer.getDuration();
    }
    else {
      return tPlayer.getDuration();
    }
  }

  /**
   * getNextImportantOffset: returns the next important offset of the current player
   */
  protected int getNextImportantOffset() {
    if (pnlTimeline.getFrame().isUsingLocalAudio) {
      return tLocalPlayer.nextImportantOffset;
    }
    else {
      return tPlayer.nextImportantOffset;
    }
  }
  /**
   * setNextImportantOffset: sets the next important offset for the current player
   */
  protected void setNextImportantOffset(int offset) {
    if (pnlTimeline.getFrame().isUsingLocalAudio) {
      tLocalPlayer.nextImportantOffset = offset;
    }
    else {
      tPlayer.nextImportantOffset = offset;
    }
  }
  /**
   * setPlayerOffset: sets the offset for the current player
   */
  protected void setPlayerOffset(int offset) {
    if (pnlTimeline.getFrame().isUsingLocalAudio) {
      tLocalPlayer.setOffset(offset);
    }
    else {
      tPlayer.setOffset(offset);
    }
  }
  /**
   * getLocalStartOffset: returns the local start offset of the current player
   */
  protected int getLocalStartOffset() {
    if (pnlTimeline.getFrame().isUsingLocalAudio) {
      return tLocalPlayer.localStartOffset;
    }
    else {
      return tPlayer.localStartOffset;
    }
  }
  /**
   * getLocalEndOffset: returns the local end offset of the current player
   */
  protected int getLocalEndOffset() {
    if (pnlTimeline.getFrame().isUsingLocalAudio) {
      return tLocalPlayer.localEndOffset;
    }
    else {
      return tPlayer.localEndOffset;
    }
  }
  /**
   * setLocalStartOffset: sets the local start offset for the current player
   */
  protected void setLocalStartOffset(int offset) {
    if (pnlTimeline.getFrame().isUsingLocalAudio) {
      tLocalPlayer.localStartOffset = offset;
    }
    else {
      tPlayer.localStartOffset = offset;
    }
  }
  /**
   * setLocalEndOffset: returns the local end offset for the current player
   */
  protected void setLocalEndOffset(int offset) {
    if (pnlTimeline.getFrame().isUsingLocalAudio) {
      tLocalPlayer.localEndOffset = offset;
    }
    else {
      tPlayer.localEndOffset = offset;
    }
  }
  /**
   * pausePlayer: pauses the current player
   */
  protected void pausePlayer() {
    if (pnlTimeline.getFrame().isUsingLocalAudio) {
      tLocalPlayer.pause();
    }
    else {
      tPlayer.pause();
    }
  }
  
  /**
   * StopPlayer: stops the current player
   */
  protected void stopPlayer() {
    if (pnlTimeline.getFrame().isUsingLocalAudio) {
      tLocalPlayer.stop();
    }
    else {
     // tPlayer.stop();
    }
  }

  /**
   * startPlayer: starts the current player
   */
  protected void startPlayer() {
    if (pnlTimeline.getFrame().isUsingLocalAudio) {
      tLocalPlayer.play();
    }
    else {
      tPlayer.play();
    }
  }
  /**
   * playerIsPlaying: returns true if the current player is playing
   */
  protected boolean playerIsPlaying() {
    if (pnlTimeline.getFrame().isUsingLocalAudio) {
      return tLocalPlayer.isPlaying;
    }
    else {
      return tPlayer.isPlaying;
    }
  }
  /**
   * getPlayerOffset: returns the offset of the current player
   */
  protected int getPlayerOffset() {
    if (pnlTimeline.getFrame().isUsingLocalAudio) {
      return tLocalPlayer.getOffset();
    }
    else {
      return tPlayer.getOffset();
    }
  }
  /**
   * getPlayerStartOffset: returns the start offset of the current player
   */
  protected int getPlayerStartOffset() {
    if (pnlTimeline.getFrame().isUsingLocalAudio) {
      return tLocalPlayer.startOffset;
    }
    else {
      return tPlayer.startOffset;
    }
  }
  /**
   * getPlayerEndOffset: returns the end offset of the current player
   */
  protected int getPlayerEndOffset() {
    if (pnlTimeline.getFrame().isUsingLocalAudio) {
      return tLocalPlayer.endOffset;
    }
    else {
      return tPlayer.endOffset;
    }
  }
  /**
   * getPlayerContent: returns the media content of the current player
   */
  protected String getPlayerContent() {
    if (pnlTimeline.getFrame().isUsingLocalAudio) {
      String savePath = pnlTimeline.getSavePath();
      String mediaPath = tLocalPlayer.filename.toString();
      String relativePath = "";
//      log.debug("save path = " + savePath);
//      log.debug("file path = " + mediaPath);
      try {
    	  relativePath = TimelineUtilities.getRelativePath(new File(savePath), new File (mediaPath));
      }
      catch (Exception e) {}
//      log.debug("relative path = " + relativePath);
//      if (System.getProperty("os.name").startsWith("Mac OS")) {
    	  return mediaPath;
//      }
//      else {
//    	  return relativePath;
//      }
    }
    else {
      return tPlayer.getContainerID();
    }
  }
  /**
   * setPlayerShift: sets the shiftAmount for the current player
   */
  protected void setPlayerShift(int shift) {
    if (pnlTimeline.getFrame().isUsingLocalAudio) {
      tLocalPlayer.shiftAmount = shift;
    }
    else {
      tPlayer.shiftAmount = shift;
    }
  }
  /**
   * setPlayerVolume: sets the volume for the current player
   */
  protected void setPlayerVolume(float f) {
    if (pnlTimeline.getFrame().isUsingLocalAudio) {
      tLocalPlayer.setVolume(f);
    }
    else {
      tPlayer.setVolume(f);
    }
  }
}