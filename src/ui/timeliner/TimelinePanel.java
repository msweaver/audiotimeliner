package ui.timeliner;

import javax.swing.*;
import javax.swing.undo.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import com.borland.jbcl.layout.*;
import java.util.*;
import java.lang.Math;
import java.io.*;
import util.logging.*;
import ui.common.*;
import org.apache.log4j.Logger;

/**
 * Timeline Panel
 * The timeline panel is a container for timeline objects. Events that are caught
 * by the panel are applied to timeline objects. Most calls to update the display
 * of the timeline originate here.
 *
 * @author Brent Yorgason
 */

public class TimelinePanel extends JPanel
    implements MouseListener, MouseMotionListener {

  private static final long serialVersionUID = 1L;
// associated objects
  private Timeline timeline;
  private TimelineFrame frmTimeline;
  private TimelineControlPanel pnlControl;
  protected TimelineMenuBar menubTimeline;
  private TimelinePlayer tPlayer;
  private TimelineLocalPlayer tLocalPlayer;
  protected TimelineBubbleEditor dlgBubbleEditor;
  protected TimelinePrintDialog dlgPrint;
  protected TimepointEditor dlgTimepointEditor;
  protected JButton btnBubbleEditorPlay;
  protected JButton btnBubbleEditorUpLevel;
  protected JButton btnBubbleEditorDownLevel;

  // constants that define the paint context
  private final int DONT_PAINT = -1;
  private final int DEFAULT = 0;
  private final int NEW_TIMELINE = 1;
  private final int REFRESH_TIMELINE = 2;
  private final int RESET_TIMELINE = 3;
  private final int ADD_TIMEPOINT = 4;
  private final int ADD_BUBBLE = 5;
  private final int DELETE_BUBBLE = 6;
  private final int SELECT_BUBBLE = 7;
  private final int SELECT_TIMEPOINT = 8;
  private final int DRAGGING_TIMEPOINT = 9;
  private final int DELETE_TIMEPOINT = 10;
  private final int RESIZING_LINE = 11;
  private final int SET_COLOR = 12;
  private final int OPEN_TIMELINE = 13;
  private final int GROUP_BUBBLES = 14;
  private final int UNGROUP_BUBBLES = 15;
  private final int ZOOM_TO_SELECTION = 16;
  private final int DRAG_SELECT = 17;
  private final int DO_RESIZE = 18;
  private final int ADD_MARKER = 19;
  private final int SELECT_MARKER = 20;
  private final int DRAGGING_MARKER = 21;
  private final int DELETE_MARKER = 22;
  private int paintContext = DEFAULT;

  // event handling variables
  private final int SINGLE_CLICK = 0;
  private final int SHIFT_CLICK = 1;
  private final int CONTROL_CLICK = 2;
  private final int LIST_CLICK = 3;
  private int selectType = 0;
  private int bubbleClicked = 0;
  protected int bubbleDeleted = 0;
  private int timepointClicked = 0;
  private int timepointToDelete = 0;
  private boolean draggingTimepoint = false;
  private boolean timepointWasDragged = false;
  private int markerClicked = 0;
  private int markerToDelete = 0;
  private boolean draggingMarker = false;
  private boolean markerWasDragged = false;
  public boolean resizingLine = false;
  private boolean lineWasResized = false;
  private boolean draggingTimeline = false;
  private boolean dragStarted = false;
  private boolean dragHappened = false;
  private boolean wasPlaying = false;
  protected boolean wasWithinDescription = false;
  protected boolean descriptionWasClicked = false;
  protected boolean enterKeyAdd = true;
  protected boolean sliderRepainted = false;
  protected int oldTimelineLength = 0;
  protected int dragStartOffset = 0;
  protected int dragMarkerStartOffset = 0;
  private Point dragAnchor = new Point();
  private Point dragEnd = new Point();
  private int dragOffsetX;
  private int dragOffsetY;
  private boolean scheduledRefresh = false;
  private boolean scheduledSelect = false;
  private boolean scheduledResize = false;
  private boolean audioAdded = false;

  // default property settings
  static protected Color defaultPanelColor = new Color(255,255,255); // white panel
  private Color panelColor = defaultPanelColor;
  private Color newBubbleColor = new Color(0,0,0); // black
  static private int defaultLineY;
  static private int defaultLineStart = 50;
  static private int defaultLineLength;
  static private int defaultBubbleHeight = 40;
  static private final int ROUND_BUBBLES = 0;
  static private final int SQUARE_BUBBLES = 1;
  static private int defaultBubbleType = ROUND_BUBBLES;
  static private boolean defaultBlackAndWhite = false;

  // properties for created timelines
  private int timelineY;
  private int timelineStart;
  protected int timelineLength;
  private int timelineBubbleHeight;
  private int timelineBubbleType;
  protected boolean timelineBlackAndWhite;

  // panel properties
  public Rectangle panelRect = this.getBounds();

  // graphics related variables
  protected RenderingHints renderHints =  new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
  protected JColorChooser colorChooser = new JColorChooser();
  protected JDialog colorDialog;
  protected Color newColor;
  protected Toolkit kit = Toolkit.getDefaultToolkit();
  protected ToolTipManager ttm = ToolTipManager.sharedInstance();

  // dialogs and popups
  protected TimelineProperties dlgTimelineProperties;
  TimelinePopups timelinePopups;
  protected JCheckBoxMenuItem menuiShowTimes;
  protected JRadioButtonMenuItem menuiShowTimesMac;
  protected JCheckBoxMenuItem menuiShowMarkerTimes;
  protected JRadioButtonMenuItem menuiShowMarkerTimesMac;
  protected boolean showGroupingError = false;

  // undo
  protected UndoManager undoManager = new UndoManager();
  protected Vector oldZoomSelection = new Vector();
  protected Vector undoOffsets = new Vector();
  protected Vector undoTimepointLabels = new Vector();
  protected Vector undoMarkerLabels = new Vector();
  protected Vector undoBaseColors = new Vector();
  protected Vector undoBaseLabels = new Vector();
  protected Vector undoBaseAnnotations = new Vector();
  protected Vector undoGroupStarts = new Vector();
  protected Vector undoGroupEnds = new Vector();
  protected Vector undoGroupColors = new Vector();
  protected Vector undoGroupLabels = new Vector();
  protected Vector undoGroupAnnotations = new Vector();

  // logging
  private static Logger log = Logger.getLogger(TimelinePanel.class);
  protected UILogger uilogger;
  
  public String savePath;

  /**
   * constructor
   */
  public TimelinePanel(TimelineFrame frame, int initWidth, int initHeight) {
    frmTimeline = frame;
    uilogger = frmTimeline.getUILogger();

    // add listeners
    addMouseListener(this);
    addMouseMotionListener(this);

    // set up panel
    this.setLayout(new XYLayout());
    this.setMinimumSize(new Dimension(initWidth, initHeight));
    this.setPreferredSize(new Dimension(initWidth, initHeight));
    this.setSize(initWidth, initHeight);
    this.setDoubleBuffered(true);
    setBackground(panelColor);

    // turn on high quality rendering and antialiased text
    renderHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    renderHints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

    // set up the color chooser
    ActionListener okListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        newColor = colorChooser.getColor();
        uilogger.log(UIEventType.BUTTON_CLICKED, "accept color");
      }
    };
    ActionListener cancelListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        newColor = null;
        uilogger.log(UIEventType.BUTTON_CLICKED, "cancel color");
      }
    };
    colorDialog = JColorChooser.createDialog(frmTimeline, "Set Bubble Color", true, colorChooser, okListener, cancelListener);

    // set up tool tips
    ttm.setDismissDelay(10000); // change tool tip dismiss time to 10 seconds (default is 4)

    // perform other initialization
    initTimelineProperties();
  }

  /**
   * The methods are arranged here in alphabetical order, followed by the paintComponent method,
   * the event handler methods, and the undo methods
   */

  /**
   * addBubble: adds a bubble to the timeline and respaces the current bubbles
   * so that they are all equally spaced. The purpose of this method is to enable the creation of
   * "abstract" timelines that are not linked to external sound files
   */
  public void addBubble() {
    if (timeline != null && timeline.isEditable()) {
      paintContext = ADD_BUBBLE;
      repaint();
    }
  }

  /**
   * addMarker: adds a marker at a specific point (the current position of the slider thumb).
   */
  public void addMarker() {
    if (timeline != null && timeline.isEditable()) {
      paintContext = ADD_MARKER;
      repaint();
    }
  }

  /**
   * addTimepoint: adds a timepoint at a specific point (the current position of the slider thumb).
   * Use this method rather than addBubble in most cases
   */
  public void addTimepoint() {
    if (timeline != null && timeline.isEditable()) {
      paintContext = ADD_TIMEPOINT;
      repaint();
    }
  }

  /**
   * changeBubbleColor: opens a color chooser and changes the color of the selected
   * bubbles to the selected color
   */
  protected void changeBubbleColor() {
    Color currColor = new Color(255, 255, 255); // default color
    if (timeline != null) {
      if (!timeline.areBubblesSelected() || !timeline.isEditable()) {
        return;
      }
      else {
        // determine the current color of the first selected bubble
        int firstBubble = ((Integer)(timeline.getSelectedBubbles().elementAt(0))).intValue();
        Bubble firstBubbleSelected = timeline.getBubble(firstBubble);
        currColor = firstBubbleSelected.getColor();
      }

      // open the color chooser and get the new color
      colorChooser.setColor(currColor);
      colorDialog.setTitle("Set Bubble Color");
      colorDialog.setVisible(true);

      // set the bubble color to the new color
      if (newColor != null) {
        Vector selectedBubbles = (Vector)timeline.getSelectedBubbles().clone();
        Vector oldColors = new Vector();
        for (int i = 0; i < selectedBubbles.size(); i++) {
          Bubble currBubble = timeline.getBubble(((Integer)selectedBubbles.elementAt(i)).intValue());
          oldColors.addElement(currBubble.getColor());
        }
        setSelectedBubbleColor(newColor);
        boolean wasBW = timeline.getBlackAndWhite();
        if (wasBW) {
          this.setBlackAndWhite(false);
        }
       scheduleRefresh();
        undoManager.undoableEditHappened(new UndoableEditEvent(this,
            new UndoableChangeBubbleColor(selectedBubbles, oldColors, newColor, wasBW, this)));
        updateUndoMenu();

        // now discard new color
        newColor = null;
       }
    }
  }

  /**
   * clearUndoData: clears the vectors that store data needed to undo bubble and timepoint deletion
   */
  protected void clearUndoData() {
    undoOffsets.clear();
    undoTimepointLabels.clear();
    undoMarkerLabels.clear();
    undoBaseColors.clear();
    undoBaseLabels.clear();
    undoBaseAnnotations.clear();
    undoGroupStarts.clear();
    undoGroupEnds.clear();
    undoGroupColors.clear();
    undoGroupLabels.clear();
    undoGroupAnnotations.clear();
  }

  /**
   * createExcerpt: creates a new timeline excerpted from the current one, based on the current selection
   */
  protected void createExcerpt() {
    if (timeline.areBubblesSelected()) {
      int[] range = timeline.getSelectionRange();
      WindowManager.stopAllPlayers();
      TimelineUtilities.excerptTimeline(timeline, range[0], range[1], frmTimeline);
    }
  }

  /**
   * createTimeLabel: creates and returns a time label, which is added to the timeline panel,
   * although initially not visible
   */
  protected JLabel createTimeLabel() {
    JLabel label = new JLabel("0");
    label.setVisible(false);
    this.add(label);
    return label;
  }

  /**
   * createTimeline: Creates and adds a timeline to the timeline panel using defaults
   */
  public void createTimeline() {
    paintContext = NEW_TIMELINE;
    repaint();
  }

  /**
   * createTimeline: Creates and adds a timeline to the timeline panel, taking in six parameters:
   * lineStart: x location within the panel for the starting point of the timeline
   * lineY: y location within the panel for the timeline
   * lineLength: length of the line in pixels
   * bubbleHeight: height of the bubbles for the timeline
   * bubbleType: 0 = round, 1 = square
   * blackAndWhite: whether in black and white mode or not
   */
  public void createTimeline(int lineStart, int lineY, int lineLength, int bubbleHeight, int bubbleType, boolean blackAndWhite) {

    // load parameters
    timelineY = lineY;
    timelineStart = lineStart;
    timelineLength = lineLength;
    timelineBubbleHeight = bubbleHeight;
    timelineBubbleType = bubbleType;
    timelineBlackAndWhite = blackAndWhite;
    oldTimelineLength = timelineLength;

    // paint the line
    paintContext = NEW_TIMELINE;
    repaint();
  }

  /**
   * deleteMarker: deletes the specified marker
   */
  public void deleteMarker(int m) {
    if (timeline != null && timeline.isEditable()) {
      paintContext = DELETE_MARKER;
      markerToDelete = m;
      repaint();
    }
  }

  /**
   * deleteSelectedBubbles: deletes the bubbles that are currently selected.
   * More precisely, it removes their initiating timepoints from the list. Thus, the first bubble
   * is delete protected, since its initial point is 0.
   */
  public void deleteSelectedBubbles() {
    if (timeline != null && timeline.isEditable()) {
      paintContext = DELETE_BUBBLE;
      repaint();
    }
  }

  /**
   * deleteSelectedMarker: deletes the currently selected marker
   */
  public void deleteSelectedMarker() {
    if (timeline != null && timeline.isEditable()) {
      markerClicked = timeline.getLastMarkerClicked();
      if (timeline.isMarkerSelected(markerClicked)) {
        deleteMarker(markerClicked);
        disableAllTimepointControls();
      }
    }
  }

  /**
   * deleteSelectedTimepoint: deletes the currently selected timepoint
   */
  public void deleteSelectedTimepoint() {
    if (timeline != null && timeline.isEditable()) {
      timepointClicked = timeline.getLastTimepointClicked();
      if (timeline.isTimepointSelected(timepointClicked)) {
        deleteTimepoint(timepointClicked);
        disableAllTimepointControls();
      }
    }
  }

  /**
   * deleteTimeline: deletes the current timeline
   */
  public void deleteTimeline() {
    if (timeline != null && timeline.isEditable()) {
      this.removeAll();
      timeline.showTime(false);
      timeline = null;
      repaint();
    }
  }

  /**
   * deleteTimepoint: deletes the specified timepoint
   */
  public void deleteTimepoint(int t) {
    if (timeline != null && timeline.isEditable()) {
      paintContext = DELETE_TIMEPOINT;
      timepointToDelete = t;
      repaint();
    }
  }

  /**
   * disableAddMarker: disables the controls that allow the addition of a marker
   */
  protected void disableAddMarker() {
    pnlControl.disableAddMarker();
    menubTimeline.disableAddMarker();
  }

  /**
   * disableAddTimepoint: disables the controls that allow the addition of a timepoint
   */
  protected void disableAddTimepoint() {
    pnlControl.disableAddTimepoint();
    menubTimeline.disableAddTimepoint();
  }

  /**
   * disableAllBubbleControls: disables all bubble controls in the control panel
   */
  protected void disableAllBubbleControls() {
    pnlControl.disableAllBubbleControls();
    menubTimeline.disableAllBubbleOptions();
  }

  /**
   * disableAllTimepointControls: disables the timepoint controls
   */
  protected void disableAllTimepointControls() {
    pnlControl.disableSelectedTimepointControls();
    menubTimeline.disableSelectedTimepointOptions();
  }

  /**
   * disableEnterKeyAdd: disables add timepoint by means of enter key press
   */
  protected void disableEnterKeyAdd() {
    enterKeyAdd = false;
  }

  /**
   * dontPaint: call this to prevent unwanted painting
   * Comment: not currently being used
   */
  protected void dontPaint() {
    paintContext = DONT_PAINT;
  }

  /**
   * doPrint: private method that does the timeline printing
   */
  protected void doPrint(boolean printBW) {
    // store old values
    boolean wasDirty = timeline.isDirty;
    int timelineX = timeline.getLineStart();
    int timelineY = timeline.getLineY();
    boolean isResizable = timeline.isResizable();
    boolean isBlackandWhite = timeline.getBlackAndWhite();
    Rectangle oldBounds = this.getBounds();
    Rectangle newBounds = timeline.getRect();

    // setup print image
    timeline.moveTo(2, timeline.getTotalBubbleHeight() + 2);
    timeline.deselectAllBubbles();
    timeline.deselectAllTimepointsAndMarkers();
    timeline.getResizer().setEnabled(false);
    timeline.setBlackAndWhite(printBW);

    // hide the slider
    timeline.getSlider().setVisible(false);

    // set the new bounds of the timeline panel
    this.setBounds((int)newBounds.getX() - 2, (int)newBounds.getY(), (int)newBounds.getWidth() + 5, (int)newBounds.getHeight() - 2);

    // print
    PrintUtilities.printComponent(this);

    // restore old values and move timeline back
    this.setBounds(oldBounds);
    timeline.moveTo(timelineX, timelineY);
    timeline.getResizer().setEnabled(isResizable);
    timeline.getSlider().setVisible(true);
    timeline.isDirty = wasDirty;
  }

  /**
   * doResize: resizes the timeline to the given length, use for a single resize action
   */
  public void doResize(int length) {
    if (timeline != null && timeline.isResizable() && timeline.isEditable()) {
      timelineLength = length;
      paintContext = DO_RESIZE;
      repaint();
    }
  }

  /**
   * drawSelectionBox: draws a selection box and selects all bubbles within the box
   * (not timepoints or markers--since only one of these can currently be selected at a time,
   * drawing a box around things would necessitate selecting multiple timepoints--
   * nor can you currently select timepoints/markers and bubbles together)
   */
  protected void drawSelectionBox(Graphics2D g, Rectangle rect) {
    g.setColor(new Color(232, 232, 232, 100));
    g.fill3DRect((int)rect.getX(), (int)rect.getY(), (int)rect.getWidth(), (int)rect.getHeight(), true);
  }

  /**
   * enableAddMarker: enables the controls that allow the addition of a marker
   */
  protected void enableAddMarker() {
    if (timeline.isEditable()) {
      pnlControl.enableAddMarker();
      menubTimeline.enableAddMarker();
    }
  }

  /**
   * enableAddTimepoint: enables the controls that allow the addition of a timepoint
   */
  protected void enableAddTimepoint() {
    if (timeline.isEditable()) {
      pnlControl.enableAddTimepoint();
      menubTimeline.enableAddTimepoint();
    }
  }

  /**
   * enableMultipleSelectedGroupedBubbleControls: enables the controls that are active when
   * multiple grouped bubbles are selected
   */
  protected void enableMultipleSelectedGroupedBubbleControls() {
    if (timeline.isEditable()) {
      pnlControl.enableMultipleSelectedGroupedBubbleControls();
      menubTimeline.enableMultipleSelectedGroupedBubbleOptions();
    }
  }

  /**
   * enableMultipleSelectedUngroupedBubbleControls: enables the controls that are active when
   * multiple ungrouped bubbles are selected
   */
  protected void enableMultipleSelectedUngroupedBubbleControls() {
    if (timeline.isEditable()) {
      pnlControl.enableMultipleSelectedUngroupedBubbleControls();
      menubTimeline.enableMultipleSelectedUngroupedBubbleOptions();
    }
  }

  /**
   * enableSelectedTimepointControls: enables the controls on the control panel and the options in
   * the menu that are active for a selected timepoint
   */
  protected void enableSelectedTimepointControls () {
    if (timeline.isEditable()) {
      pnlControl.enableSelectedTimepointControls();
      menubTimeline.enableSelectedTimepointOptions();
    }
  }

  /**
   * enableSingleSelectedBubbleControls: enables the controls on the control panel and the options in
   * the menu that are active for a single selected bubble
   */
  protected void enableSingleSelectedBubbleControls () {
    if (timeline.isEditable()) {
      pnlControl.enableSingleSelectedBubbleControls();
      menubTimeline.enableSingleSelectedBubbleOptions();
    }
  }

  /**
   * enableEnterKeyAdd: enables add timepoint by means of enter key press
   */
  protected void enableEnterKeyAdd() {
    if (timeline.isEditable()) {
      enterKeyAdd = true;
    }
  }

  /**
   * fitToWindow: fits the current bubble diagram to fit in the window
   */
  protected void fitToWindow() {
    boolean wasZoomed = timeline.timelineZoomed;
    timeline.timelineZoomed = false;
    int frameWidth = frmTimeline.getWidth();
    oldTimelineLength = timelineLength;
    timelineLength = (frameWidth - TimelineFrame.SIDE_SPACE);
    Rectangle oldScrollRect = this.getVisibleRect();
    oldScrollRect.setBounds((int)oldScrollRect.getX(), this.getHeight()*2, (int)oldScrollRect.getWidth(), 20);
    Graphics2D g2 = (Graphics2D)this.getGraphics();
    timeline.doLastResize(timelineLength, g2);
    timeline.refresh(g2);
    Dimension oldPanelSize = this.getSize();
    Dimension newPanelSize = new Dimension(frameWidth - TimelineFrame.FRAME_SIDE_SPACE -10, this.getHeight() -10);
    this.setSize(newPanelSize);
    this.setPreferredSize(newPanelSize);
    this.setMinimumSize(newPanelSize);
    undoManager.undoableEditHappened(new UndoableEditEvent(this,
        new UndoableFitToWindow(oldTimelineLength, timelineLength, oldPanelSize, newPanelSize, wasZoomed, timeline.getSelectedBubbles(), oldScrollRect, this)));
    updateUndoMenu();
  }

  /**
   * various getters
   */

  /**
   * getDefaultBlackAndWhite: returns true if the bubbles are being drawn in black and white
   */
  public boolean getDefaultBlackAndWhite() {
    return defaultBlackAndWhite;
  }

  /**
   * returns default bubble height
   */
  public int getDefaultBubbleHeight() {
    return defaultBubbleHeight;
  }

  /**
   * returns default bubble type (0=round; 1=square)
   */
  public int getDefaultBubbleType() {
    return defaultBubbleType;
  }

  /**
   * returns default line length
   */
  public int getDefaultLineLength() {
    return defaultLineLength;
  }

  /**
   * returns default line start (x position)
   */
  public int getDefaultLineStart() {
    return defaultLineStart;
  }

  /**
   * returns default line y position
   */
  public int getDefaultLineY() {
    return defaultLineY;
  }

  /**
   * getFrame: returns a reference to the timeline frame
   */
  protected TimelineFrame getFrame() {
    return frmTimeline;
  }

  /**
   * getMenuBar: returns the timeline menu bar
   */
  protected TimelineMenuBar getMenuBar() {
    return menubTimeline;
  }

  /**
   * getPaintContext: returns the current paint context
   */
  protected int getPaintContext() {
    return paintContext;
  }

  /**
   * getPanelColor: returns the panel's current color
   */
  public Color getPanelColor() {
    return panelColor;
  }

  /**
   * getPlayer: returns a reference to the timeline player
   */
  protected TimelinePlayer getPlayer() {
    return tPlayer;
  }

  /**
   * getLocalPlayer: returns a reference to the local player
   */
  protected TimelineLocalPlayer getLocalPlayer() {
    return tLocalPlayer;
  }

  /**
   * getRect: given two points, returns a rect
   */
  protected Rectangle getRect(Point startPoint, Point endPoint) {
    double width = Math.abs((endPoint.getX() - startPoint.getX()));
    double height = Math.abs((endPoint.getY() - startPoint.getY()));
    double startX = (startPoint.getX() <= endPoint.getX()) ? startPoint.getX() : endPoint.getX();
    double startY = (startPoint.getY() <= endPoint.getY()) ? startPoint.getY() : endPoint.getY();
    Rectangle selectionRect = new Rectangle((int)startX, (int)startY, (int)width, (int)height);

    return selectionRect;
  }

  /**
   * getTimeline: returns a reference to the timeline
   */
  protected Timeline getTimeline() {
    return timeline;
  }

  /**
   * goToNextBubble: moves the slider and playback to the beginning of the next bubble
   */
  protected void goToNextBubble() {
    timeline.goToNextBubble();
  }

  /**
   * goToPreviousBubble: moves the slider and playback to the beginning of the previous bubble
   */
  protected void goToPreviousBubble() {
    timeline.goToPreviousBubble();
  }

  /**
   * groupSelectedBubbles: groups selected bubbles by adding a higher level bubble
   */
  public void groupSelectedBubbles() {
    if (timeline != null && timeline.isEditable()) {
      paintContext = GROUP_BUBBLES;
      repaint();
    }
  }

  /**
   * initTimelineProperties: sets the timeline properties to default values
   */
  private void initTimelineProperties() {
    defaultLineY = this.getHeight() - TimelineFrame.BOTTOM_SPACE;
    defaultLineLength = frmTimeline.getWidth() - TimelineFrame.SIDE_SPACE;
    timelineY = defaultLineY;
    timelineStart = defaultLineStart;
    timelineLength = defaultLineLength;
    oldTimelineLength = timelineLength;
    timelineBubbleHeight = defaultBubbleHeight;
    timelineBubbleType = defaultBubbleType;
    timelineBlackAndWhite = defaultBlackAndWhite;
  }

  /**
   * isLineResizing: returns true if the timeline is being resized
   */
  public boolean isLineResizing() {
    return resizingLine;
  }

  /**
   * isLineDragging: returns true if the timeline is being dragged
   */
  public boolean isLineDragging() {
    return draggingTimeline;
  }

  /**
   * moveSelectedBubblesDown: bumps the selected bubbles down a level
   */
  protected void moveSelectedBubblesDown(){
    timeline.moveSelectedBubblesDown();
    refreshTimeline();
  }

  /**
   * moveSelectedBubblesUp: bumps the selected bubbles up a level
   */
  protected void moveSelectedBubblesUp(){
    timeline.moveSelectedBubblesUp();
    refreshTimeline();
  }

  /**
   * openTimeline: opens an open dialog to open a saved timeline
   */
  public void openTimeline() {
    pnlControl.clearAnnotationPane();
    paintContext = OPEN_TIMELINE;
    repaint();
  }

  /**
   * openTimelineProperties: opens the timeline properties dialog
   */
  public void openTimelineProperties() {
    dlgTimelineProperties = new TimelineProperties(frmTimeline, this, true); // modal
  }

  /**
   * paintDraggingTimepoint: call this method when a timepoint is being dragged to repaint the timeline
   */
  protected void paintDraggingTimepoint() {
    paintContext = DRAGGING_TIMEPOINT;
    repaint();
  }

  /**
   * paintDraggingMarker: call this method when a marker is being dragged to repaint the timeline
   */
  protected void paintDraggingMarker() {
    paintContext = DRAGGING_MARKER;
    repaint();
  }

  /**
   * printTimelinePanel: opens a print dialog
   */
  public void printTimelinePanel() {
    if (timeline != null) {
      // ask the user whether to print in color or black and white
      // this method will then call doPrint
      dlgPrint = new TimelinePrintDialog(frmTimeline);
    }
  }

  /**
   * refreshTimeline: repaints the timeline.
   */
  public void refreshTimeline() {
    paintContext = REFRESH_TIMELINE;
    repaint();
  }

  /**
   * repositionTimeline: repositions the timeline in the panel based on the current window size
   */
  protected void repositionTimeline() {
    defaultLineY = this.getHeight() - TimelineFrame.BOTTOM_SPACE;
    defaultLineLength = this.getWidth() - 100;
    timeline.moveTo(timeline.getLineStart(), defaultLineY);
  }

  /**
   * resetTimeline: resets the timeline to its initial creation state
   */
  public void resetTimeline() {
    if (timeline != null && timeline.isEditable()) {
      int response = JOptionPane.showConfirmDialog(frmTimeline, "Are you sure you want to clear all of your timepoints and markers?", "Clear All?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
      if (response == JOptionPane.NO_OPTION) {
        uilogger.log(UIEventType.BUTTON_CLICKED, "cancel clear all");
        return;
      }
      pnlControl.clearAnnotationPane();
      paintContext = RESET_TIMELINE;
      repaint();
      uilogger.log(UIEventType.BUTTON_CLICKED, "confirm clear all");
    }
  }

  /**
   * resizeTimeline: resizes the timeline to the given length, mainly used for manual resizing by dragging
   */
  public void resizeTimeline(int length) {
    if (timeline != null && timeline.isResizable() && timeline.isEditable()) {
      timelineLength = length;
      paintContext = RESIZING_LINE;
      repaint();
    }
  }

  /**
   * saveTimeline: opens a save dialog to save the timeline
   * saveMode: 0 = save, 1 = save as
   */
  public void saveTimeline(boolean saveAs) {
    TimelineUtilities.saveTimeline(timeline, saveAs, frmTimeline);
  }

  /**
   * saveTimelineAsWebPage: saves the timeline as a web page
   */
  public void saveTimelineAsWebPage() {
    TimelineUtilities.saveTimelineAsWebPage(timeline, frmTimeline);
  }

  /**
   * revertToSavedTimeline: reverts to the last saved timeline
   */
  public void revertToSavedTimeline() {
    try {
    File savedFile = File.createTempFile("timeline", ".~~~");
    savedFile.deleteOnExit();
    String filePath = savedFile.getPath();
    TimelineXMLAdapter txmla = new TimelineXMLAdapter();
    txmla.saveTimelineXML(filePath, timeline, "Client");
    undoManager.undoableEditHappened(new UndoableEditEvent(this,
        new UndoableRevertTimeline(filePath, timeline.currFilename, timeline, this)));
    updateUndoMenu();
    TimelineUtilities.revertToSavedTimeline(timeline, frmTimeline);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  public String getSavePath() {
	  return savePath;
  }
  
  public void setSavePath(String s) {
	  savePath = s;
  }
  /**
   * scheduleRefresh: if you make changes to components that will cause repainting,
   * you will need to refresh the timeline display AFTER the paintComponent call has been made.
   * This can be done by scheduling a refresh. The refresh will occur after the next repaint
   */
  public void scheduleRefresh() {
    scheduledRefresh = true;
  }

  /**
   * selectBubble: marks a bubble as selected. Parameters are the number of the selected bubble
   * (starting at 0), and the selection type (whether it was a control-click, shift-click, etc.)
   * See constants above for the selection types
   */
  public void selectBubble(int bSelected, int sType) {
    if (timeline != null) {
      paintContext = SELECT_BUBBLE;
      selectType = sType;
      bubbleClicked = bSelected;
      repaint();
    }
  }

  /**
   * selectMarker: marks a marker as selected, given the number of the marker (starting at 0)
   */
  public void selectMarker(int mSelected) {
    if (timeline != null && timeline.isEditable()) {
      paintContext = SELECT_MARKER;
      markerClicked = mSelected;
      repaint();
    }
  }

  /**
   * selectRect: draws a selection box around the rect created by the two points passed
   */
  protected void selectRect (Point p1, Point p2) {
    dragAnchor = p1;
    dragEnd = p2;
    paintContext = DRAG_SELECT;
    repaint();
  }

  /**
   * selectTimepoint: marks a timepoint as selected, given the number of the timepoint (starting at 0)
   */
  public void selectTimepoint(int tSelected) {
    if (timeline != null && timeline.isEditable()) {
      paintContext = SELECT_TIMEPOINT;
      timepointClicked = tSelected;
      repaint();
    }
  }

  /**
   * setBackgroundColor: opens a color chooser and sets the background color to the selected color
   */
  public void setBackgroundColor() {
    if (timeline.isEditable()) {
      Color currColor = this.getPanelColor(); // default color
      boolean wasBW = timeline.getBlackAndWhite();
      if (wasBW) {
        timeline.setBlackAndWhite(false);
      }
      // open the color chooser and get the new color
      colorChooser.setColor(currColor);
      colorDialog.setTitle("Set Background Color");
      colorDialog.setVisible(true);

      // set the background color to the new color
      if (newColor != null) {
        setPanelColor(newColor);
        timeline.lblThumb.setBackground(newColor);
        refreshTimeline();
        undoManager.undoableEditHappened(new UndoableEditEvent(this,
            new UndoableSetBackgroundColor(currColor, newColor, wasBW, this)));
        updateUndoMenu();

        // now discard new color
        newColor = null;
      }
    }
  }

  /**
   * setBlackAndWhite: sets the bubble drawing style to black and white
   */
  public void setBlackAndWhite (boolean bw) {
    timelineBlackAndWhite = bw;
    if (System.getProperty("os.name").startsWith("Mac OS")) {
      menubTimeline.menuiBlackAndWhiteMac.setSelected(bw);
    }
    else {
      menubTimeline.menuiBlackAndWhite.setSelected(bw);
    }
    if (timeline != null) {
      timeline.setBlackAndWhite(bw);
      refreshTimeline();
      undoManager.undoableEditHappened(new UndoableEditEvent(this,
          new UndoableSetBlackAndWhite(bw, this)));
      updateUndoMenu();
    }
  }

  /**
   * setBubbleType: sets the bubble type to the type passed
   * (0 = round bubbles, 1 = square bubbles)
   */
  public void setBubbleType (int bType) {
    timelineBubbleType = bType;
    if (bType == 0) {
      if (System.getProperty("os.name").startsWith("Mac OS")) {
        menubTimeline.menuiRoundBubblesMac.setSelected(true);
        menubTimeline.menuiSquareBubblesMac.setSelected(false);
      }
      else {
        menubTimeline.menuiRoundBubbles.setSelected(true);
        menubTimeline.menuiSquareBubbles.setSelected(false);
      }
      undoManager.undoableEditHappened(new UndoableEditEvent(this,
          new UndoableRoundBubbles(this)));
      updateUndoMenu();
    }
    else {
      if (System.getProperty("os.name").startsWith("Mac OS")) {
        menubTimeline.menuiRoundBubblesMac.setSelected(false);
        menubTimeline.menuiSquareBubblesMac.setSelected(true);
      }
      else {
        menubTimeline.menuiRoundBubbles.setSelected(false);
        menubTimeline.menuiSquareBubbles.setSelected(true);
      }
      undoManager.undoableEditHappened(new UndoableEditEvent(this,
          new UndoableSquareBubbles(this)));
      updateUndoMenu();
    }
    if (timeline != null) {
      timeline.setBubbleType(bType);
      refreshTimeline();
    }
  }

  /**
   * setControlPanel: sets the control panel for the timeline panel
   */
  protected void setControlPanel(TimelineControlPanel tcp) {
    pnlControl = tcp;
  }

  /**
   * setEditableTimeline: sets whether the timeline can be edited or not
   * With a timeline that is not editable, you cannot add timepoints, markers or bubbles, select
   * timepoints, markers or bubbles, change bubble colors, group bubbles, delete bubbles, timepoints or markers, etc.
   */
  public void setEditableTimeline(boolean state) {
    if (timeline != null) {
      timeline.setEditable(state);
      pnlControl.btnClearAll.setEnabled(state);
      menubTimeline.menuiReset.setEnabled(state);
      if (System.getProperty("os.name").startsWith("Mac OS")) {
        menuiShowTimesMac.setEnabled(state);
        menubTimeline.menuiShowTimesMac.setEnabled(state);
        menuiShowMarkerTimesMac.setEnabled(state);
        menubTimeline.menuiShowMarkerTimesMac.setEnabled(state);
        menubTimeline.menuiRoundBubblesMac.setEnabled(state);
        menubTimeline.menuiSquareBubblesMac.setEnabled(state);
        menubTimeline.menuiBlackAndWhiteMac.setEnabled(state);
      }
      else {
        menuiShowTimes.setEnabled(state);
        menubTimeline.menuiShowTimes.setEnabled(state);
        menuiShowMarkerTimes.setEnabled(state);
        menubTimeline.menuiShowMarkerTimes.setEnabled(state);
        menubTimeline.menuiRoundBubbles.setEnabled(state);
        menubTimeline.menuiSquareBubbles.setEnabled(state);
        menubTimeline.menuiBlackAndWhite.setEnabled(state);
      }
      if (state == false) {
        this.disableAddTimepoint();
        this.disableAddMarker();
      }
      else {
        this.enableAddTimepoint();
        this.enableAddMarker();
      }
    }
  }

  /**
   * setItemSelected: the TimepointList uses this method to select bubbles based on the selected items
   * in its list
   */
  protected void setItemSelected(Integer i) {
    if (timeline != null && timeline.isEditable()) {
      timeline.setItemSelected(i);
    }
  }

  /**
   * setLevelColor: sets the new default color for the selected level of bubbles
   */
  public void setLevelColor() {
    boolean wasBW = timeline.getBlackAndWhite();
    Bubble currBubble = timeline.getBubble(bubbleClicked);
    Color currColor = currBubble.getColor();
    int currLevel = currBubble.getLevel();

    // find out all of the levels involved, store old colors
    Vector<Integer> levels = new Vector<>();
    for (int i = 0; i < timeline.getSelectedBubbles().size(); i++) {
      Integer lev = Integer.valueOf(timeline.getBubble(((Integer)timeline.getSelectedBubbles().elementAt(i)).intValue()).getLevel());
      if (!levels.contains(lev)) {
        levels.addElement(lev);
      }
    }
    Vector oldColors = new Vector(timeline.getNumTotalBubbles());
    for (int i = 0; i <= timeline.getNumTotalBubbles(); i++) {
      oldColors.addElement(timeline.getBubble(i).getColor());
    }

    // open the color chooser and get the new color
    colorChooser.setColor(currColor);
    colorDialog.setTitle("Set Level Color");
    colorDialog.setVisible(true);

    // set the new level color
    if (newColor != null) {
      if (timeline.getSelectedBubbles().size() > 1) {
        timeline.setSelectedLevelColor(newColor);
      }
      else {
        if (wasBW) {
          timeline.setBlackAndWhite(true);
        }
        timeline.setLevelColor(currLevel, newColor);
      }
      refreshTimeline();
      undoManager.undoableEditHappened(new UndoableEditEvent(this,
          new UndoableSetLevelColor(levels, oldColors, newColor, wasBW, this)));
      updateUndoMenu();

      // now discard new color
      newColor = null;
    }
  }

  /**
   * setLineResizing: informs the timeline panel that the line is being resized
   */
  protected void setLineResizing(boolean b) {
    resizingLine = b;
  }

  /**
   * setMarkerClicked: sets the variable markerClicked
   */
  protected void setMarkerClicked (int mClicked) {
    markerClicked = mClicked;
  }

  /**
   * setMenuBar: sets the menu bar for the timeline panel
   */
  protected void setMenuBar(TimelineMenuBar tmb) {
    menubTimeline = tmb;
  }

  protected void setPaintContext(int i) {
    paintContext = i;
  }

  /**
   * setMovableTimeline: sets whether the timeline can be dragged or not
   */
  public void setMovableTimeline(boolean state) {
    if (timeline != null) {
      timeline.setMovable(state);
    }
  }

  /**
   * setPanelColor: sets the timeline panel background color
   */
  public void setPanelColor(Color c) {
    panelColor = c;
    repaint();
    if (timeline != null) {
      timeline.makeDirty();
    }
  }

  /**
   * setPlayer: sets the player for the timeline panel
   */
  protected void setPlayer(TimelinePlayer tp, boolean isNew) {
    tPlayer = tp;
    timelinePopups = new TimelinePopups(frmTimeline);
    if (isNew) {
      createTimeline();
    }
  }

  /**
   * setLocalPlayer: sets the local player for the timeline panel
   */
  protected void setLocalPlayer(TimelineLocalPlayer tlp, boolean isNew, boolean isNewAudio) {
    tLocalPlayer = tlp;
    timelinePopups = new TimelinePopups(frmTimeline);
    audioAdded = isNewAudio;
    if (isNew) {
      createTimeline();
    }
  }

  /**
   * setResizableTimeline: sets whether the timeline may be resized or not
   */
  public void setResizableTimeline(boolean state) {
    if (timeline != null) {
      timeline.setResizable(state);
      refreshTimeline();
    }
  }

  /**
   * setSelectedBubbleAnnotation: sets the annotation of any selected bubbles
   */
  public void setSelectedBubbleAnnotation(String str) {
    timeline.setSelectedBubbleAnnotation(str);
  }

  /**
   * setSelectedBubbbleColor: sets the color of any selected bubbles to the color passed
   */
  public void setSelectedBubbleColor(Color c) {
    if (timeline != null && timeline.isEditable()) {
      newBubbleColor = c;
      paintContext = SET_COLOR;
      repaint();
    }
  }

  /**
   * setSelectedMarkerAnnotation: sets the annotation of any selected markers
   */
  public void setSelectedMarkerAnnotation(String str) {
    timeline.setSelectedMarkerAnnotation(str);
  }

  /**
   * setSelectedTimepointAnnotation: sets the annotation of any selected timepoints (inactive)
   */
  public void setSelectedTimepointAnnotation(String str) {
    timeline.setSelectedTimepointAnnotation(str);
  }

  /**
   * setTimeline: sets the timeline to the passed timeline
   */
  protected void setTimeline (Timeline t) {
    if (t != null) {
      timeline = t;
      if (frmTimeline.isUsingLocalAudio) {
        setLocalPlayer(this.getLocalPlayer(), false, false);
      }
      else {
        setPlayer(this.getPlayer(), false);
      }
      refreshTimeline();
    }
  }

  /**
   * setTimepointClicked: sets the variable timepointClicked
   */
  protected void setTimepointClicked (int tClicked) {
    timepointClicked = tClicked;
  }

  /**
   * showTimes: sets whether the times are shown below each timepoint
   */
  public void showTimes(boolean show) {
    if (System.getProperty("os.name").startsWith("Mac OS")) {
      this.menuiShowTimesMac.setSelected(show);
      menubTimeline.menuiShowTimesMac.setSelected(show);
    }
    else {
      this.menuiShowTimes.setSelected(show);
      menubTimeline.menuiShowTimes.setSelected(show);
    }
    timeline.showTimepointTimes(show);
    undoManager.undoableEditHappened(new UndoableEditEvent(this,
        new UndoableShowTimes(show, this)));
    updateUndoMenu();

  }

  /**
   * showMarkerTimes: sets whether the times are shown below each marker
   */
  public void showMarkerTimes(boolean show) {
    if (System.getProperty("os.name").startsWith("Mac OS")) {
      this.menuiShowMarkerTimesMac.setSelected(show);
      menubTimeline.menuiShowMarkerTimesMac.setSelected(show);
    }
    else {
      this.menuiShowMarkerTimes.setSelected(show);
      menubTimeline.menuiShowMarkerTimes.setSelected(show);
    }
    timeline.showMarkerTimes(show);
    undoManager.undoableEditHappened(new UndoableEditEvent(this,
        new UndoableShowMarkerTimes(show, this)));
    updateUndoMenu();

  }

  /**
   * showToolTip: sets the text for the tool tip at the given point
   */
  public void showToolTip(Point p) {
    if (timeline != null) {
      String tip = "";
      int markerHover = timeline.getMarkerAt(p);
      int bubHover = timeline.getBubbleClicked(p);

      // hovering over a marker
      if (markerHover != -1 && timeline.isEditable()) {
        frmTimeline.setCursor(kit.createCustomCursor(UIUtilities.imgHandOpen, new Point(8, 8), "Cursor"));
        tip = UIUtilities.removeTags(((Marker)timeline.getMarker(markerHover)).getAnnotation());
        if (tip.length() == 0) {
          this.setToolTipText(null);
        }
        else {
          this.setToolTipText(tip);
        }
      }

      // hovering over a bubble
      else if (bubHover != -1) {
        if (System.getProperty("os.name").startsWith("Mac OS")) {
          frmTimeline.setCursor(kit.createCustomCursor(UIUtilities.imgHandPoint, new Point(8, 8), "Cursor"));
          tip = UIUtilities.removeTags(((Bubble)timeline.getBubble(bubHover)).getAnnotation());
          if (tip.length() == 0) {
            this.setToolTipText(null);
           }
          else {
            if (tip.length() > 100) {
              tip = tip.substring(0, 100);
              tip = tip + " ...";
            }
            this.setToolTipText(tip);
          }
        } else {
          frmTimeline.setCursor(kit.createCustomCursor(UIUtilities.imgHandPoint, new Point(8, 8), "Cursor"));
          tip = UIUtilities.removeTags(((Bubble)timeline.getBubble(bubHover)).getAnnotation());
          if (tip.length() == 0) {
            this.setToolTipText(null);
          }
          else {
            this.setToolTipText(tip);
          }
        }
      }

      // hovering over the resizer
      else if (timeline.isResizable() && timeline.isEditable() && timeline.getResizer().contains(p) && !resizingLine) {
        frmTimeline.setCursor(kit.createCustomCursor(UIUtilities.imgHandOpen, new Point(8, 8), "Cursor"));
      }

      // hovering over the description icon
      else if (timeline.descriptionRect.contains(p)) {
        if (!wasWithinDescription) {
          wasWithinDescription = true;
          timeline.drawDescriptionIcon((Graphics2D)this.getGraphics(), 1);
          refreshTimeline();
        }
        frmTimeline.setCursor(kit.createCustomCursor(UIUtilities.imgHandPoint, new Point(8, 8), "Cursor"));
        this.setToolTipText("Click to view timeline description");
      }

      // left hovering over description icon
      else if (wasWithinDescription && !timeline.descriptionRect.contains(p)) {
        wasWithinDescription = false;
        timeline.drawDescriptionIcon((Graphics2D)this.getGraphics(), 0);
        refreshTimeline();
        frmTimeline.setCursor(Cursor.getDefaultCursor());
        this.setToolTipText(null);
      }

      // not hovering over anything with a tool tip
      else {
        frmTimeline.setCursor(Cursor.getDefaultCursor());
        this.setToolTipText(null);
      }
    }
  }

  /**
   * ungroupSelectedBubbles: ungroups selected bubbles if those bubbles are already grouped
   */
  protected void ungroupSelectedBubbles() {
    paintContext = UNGROUP_BUBBLES;
    repaint();
  }

  /**
   * updateUndoMenu: updates the undo menu with the right undo/redo text
   */
  public void updateUndoMenu() {
    frmTimeline.basicMenuBar.menuEdit.menuiEditUndo.setText(undoManager.getUndoPresentationName());
    frmTimeline.basicMenuBar.menuEdit.menuiEditRedo.setText(undoManager.getRedoPresentationName());
    frmTimeline.basicMenuBar.menuEdit.menuiEditUndo.setEnabled(undoManager.canUndo());
    frmTimeline.basicMenuBar.menuEdit.menuiEditRedo.setEnabled(undoManager.canRedo());

    if (!undoManager.canUndo()) { // if all user actions have been undone, timeline is not dirty
      timeline.makeClean();
    }
    else {
      timeline.makeDirty(); // otherwise, any undo or redo would call for a save warning
    }
  }

  /**
   * zoomToSelectedBubbles: zooms to the range of bubbles delimited by the outmost selected bubbles
   */
  protected void zoomToSelectedBubbles() {
    paintContext = ZOOM_TO_SELECTION;
    repaint();
  }

/// paintComponent method ///

  /**
   * paintComponent: This method handles all of the repainting calls in the other methods. The
   * current paintContext determines what is painted when this method is called. When the
   * paintContext is not explicitly set beforehand, the context is DEFAULT. Most calls to the
   * timeline object are made from this method
   */
  public void paintComponent (Graphics g) {
    // System.out.println("Paint Context = " + paintContext);
    Graphics2D g2d = (Graphics2D)g;
    g2d.setRenderingHints(renderHints);

    // call super and set background colors
    if (paintContext != DONT_PAINT) {
      super.paintComponent(g);
      if (timeline != null && timeline.getBlackAndWhite()) {
        setBackground(defaultPanelColor);
        timeline.setSliderBackground(defaultPanelColor);
      }
       else {
        setBackground(panelColor);
        if (timeline != null) {
          timeline.setSliderBackground(panelColor);
        }
      }
    }
    // execute customized paint commands, depending on the current paint context
    switch (paintContext) {
      // don't do anything!
      case DONT_PAINT:
        break;

        // the default call to paintComponent currently refreshes the timeline
      case DEFAULT:
        if (timeline != null) {
          // if this is primarily a slider repaint, do not refresh timeline, just paint timepoints and markers
          if (g.getClipBounds().getHeight() == timeline.getSlider().getHeight()) {
            if (sliderRepainted) {
              sliderRepainted = false;
              if (timeline.playerIsPlaying() || pnlControl.trackingState != 0) {
                timeline.paintTimepointsAndMarkers(g2d);
              }
              else {
                if (timeline != null) {
                   timeline.refresh(g2d);
                 }
              }
            }
            else {
              if (timeline != null) {
                 timeline.refresh(g2d);
               }
            }
          }
          else {
            if (timeline != null) {
              timeline.refresh(g2d);
            }
          }
        }
        break;

        // creates a new timeline (if one has not been created) and populates the timepoint list (if any)
      case NEW_TIMELINE:
        if (timeline == null) {
          timeline = new Timeline(g2d, timelineStart, timelineY, timelineLength, timelineBubbleHeight, timelineBubbleType, timelineBlackAndWhite, this);
          if (frmTimeline.isUsingLocalAudio) {
            timeline.setLocalPlayer(tLocalPlayer);
          }
          else {
            timeline.setPlayer(tPlayer);
          }
//          createTimeline();
          frmTimeline.doWindowResize();
          initTimelineProperties();
          if (audioAdded) {
            timeline.makeDirty();
          }
          this.fitToWindow();
        }
        break;

        // marks selected bubbles and refreshes the timeline
      case REFRESH_TIMELINE:
        if (timeline != null) {
          timeline.refresh(g2d);
        }
        break;

        // resets the timeline to a single bubble with its initial settings
      case RESET_TIMELINE:
        if (timeline != null) {
          try {
            pnlControl.btn_stopAction();
            File savedFile = File.createTempFile("timeline", ".~~~");
            savedFile.deleteOnExit();
            String filePath = savedFile.getPath();
            TimelineXMLAdapter txmla = new TimelineXMLAdapter();
            txmla.saveTimelineXML(filePath, timeline, "Client");
            undoManager.undoableEditHappened(new UndoableEditEvent(this,
                new UndoableResetTimeline(filePath, this)));
            updateUndoMenu();
          }
          catch (Exception e) {
            e.printStackTrace();
          }
          timeline.resetTimeline();
          pnlControl.updateAnnotationPane();
        }
        break;

        // adds a timepoint at the current slider position, then refreshes the timeline and timepoint list
      case ADD_TIMEPOINT:
        if (timeline != null) {
          int off = timeline.getPlayerOffset();
          int timepointNum = timeline.addTimepoint(off);
          timeline.refresh(g2d);
          undoManager.undoableEditHappened(new UndoableEditEvent(this,
              new UndoableAddTimepoint(off, timepointNum, this)));
          updateUndoMenu();
        }
        break;

        // adds a marker at the current slider position, then refreshes the timeline
      case ADD_MARKER:
        if (timeline != null) {
          int off = timeline.getPlayerOffset();
          int markerNum = timeline.addMarker(off);
          timeline.refresh(g2d);
          undoManager.undoableEditHappened(new UndoableEditEvent(this,
              new UndoableAddMarker(off, markerNum, this)));
          updateUndoMenu();
        }
        break;

        // adds a bubble, then refreshes the timeline and timepoint list
      case ADD_BUBBLE:
        if (timeline != null) {
          timeline.addBubble();
          timeline.refresh(g2d);
        }
        break;

        // deletes any selected bubbles
      case DELETE_BUBBLE:
        if (timeline != null) {
          clearUndoData();
          Vector oldSelectedBubbles = (Vector)(timeline.getSelectedBubbles().clone());
          boolean couldDelete = timeline.deleteSelectedBubbles();
          timeline.refresh(g2d);
          if (couldDelete) {
            undoManager.undoableEditHappened(new UndoableEditEvent(this,
                new UndoableDeleteBubble(oldSelectedBubbles, undoOffsets, undoTimepointLabels, undoBaseColors, undoBaseLabels, undoBaseAnnotations,
                undoGroupStarts, undoGroupEnds, undoGroupColors, undoGroupLabels, undoGroupAnnotations, this)));
            updateUndoMenu();
          }
        }
        break;

        // repaints the bubbles in the selectedBubbles Vector
      case SELECT_BUBBLE:
        if (timeline != null) {
          timeline.selectBubble(bubbleClicked, selectType);
          timeline.refresh(g2d);
        }
        break;

        // repaints the currently selected timepoint
      case SELECT_TIMEPOINT:
        if (timeline != null) {
          timeline.selectTimepoint(timepointClicked);
          timeline.refresh(g2d);
        }
        break;

        // repaints the currently selected marker
      case SELECT_MARKER:
        if (timeline != null) {
          timeline.selectMarker(markerClicked);
          timeline.refresh(g2d);
        }
        break;

        // sets the color of the currently selected bubbles
      case SET_COLOR:
        if (timeline != null) {
          timeline.setSelectedBubbleColor(newBubbleColor);
          timeline.refresh(g2d);
        }
        break;

        // refresh when dragging
      case DRAGGING_TIMEPOINT:
        if (timeline != null) {
          timeline.refresh(g2d);
        }
        break;

        // refresh when dragging marker
      case DRAGGING_MARKER:
        if (timeline != null) {
          timeline.refresh(g2d);
        }
        break;

        // delete the specified timepoint
      case DELETE_TIMEPOINT:
        if (timeline != null) {
          if (timepointToDelete != 0) {
          clearUndoData();
          int offset = timeline.getOffsetAt(timepointToDelete);
          timeline.deleteTimepoint(timepointToDelete);
          timeline.refresh(g2d);
          undoManager.undoableEditHappened(new UndoableEditEvent(this,
              new UndoableDeleteTimepoint(timepointToDelete, offset, undoTimepointLabels, undoBaseColors, undoBaseLabels, undoBaseAnnotations,
              undoGroupStarts, undoGroupEnds, undoGroupColors, undoGroupLabels, undoGroupAnnotations, this)));
          updateUndoMenu();
          }
          else {
            timeline.refresh(g2d);
          }
        }
        break;

        // delete the specified marker
      case DELETE_MARKER:
        if (timeline != null) {
          int offset = timeline.getMarkerOffsetAt(markerToDelete);
          Marker m = timeline.getMarker(markerToDelete);
          String label = m.getLabel();
          String annotation = m.getAnnotation();
          timeline.deleteMarker(markerToDelete);
          timeline.refresh(g2d);
          undoManager.undoableEditHappened(new UndoableEditEvent(this,
              new UndoableDeleteMarker(markerToDelete, offset, label, annotation, this)));
          updateUndoMenu();
        }
        break;

        // resizing the line
      case RESIZING_LINE:
        if (timeline != null) {
          timeline.resizeTimeline(timelineLength, g2d);
          timeline.refresh(g2d);
          log.debug("resize length = " + timelineLength);
        }
        break;

        // open a timeline
      case OPEN_TIMELINE:
        TimelineUtilities.openTimeline((BasicWindow)frmTimeline);
        break;

        // group the selected bubbles
      case GROUP_BUBBLES:
        if (timeline != null) {
          timeline.sortSelectedBubbles();
          undoManager.undoableEditHappened(new UndoableEditEvent(this,
              new UndoableGroupBubbles(timeline.getSelectedBubbles(), this)));
          updateUndoMenu();
          paintContext = DEFAULT;

          boolean result = timeline.groupSelectedBubbles();
          timeline.refresh(g2d);
          if (result == false) {
            showGroupingError = true;
          }
        }
        break;

        // ungroup the selected bubbles
      case UNGROUP_BUBBLES:
        if (timeline != null) {
          timeline.sortSelectedBubbles();
          Vector oldSelectedBubbles = (Vector)(timeline.getSelectedBubbles().clone());
          Bubble bubRemoved = timeline.ungroupSelectedBubbles();
          timeline.refresh(g2d);
          undoManager.undoableEditHappened(new UndoableEditEvent(this,
              new UndoableUngroupBubbles(oldSelectedBubbles, bubRemoved, bubbleDeleted, this)));
          updateUndoMenu();
        }
        break;

        // zoom to selected bubbles
      case ZOOM_TO_SELECTION:
        if (timeline != null) {
          Dimension oldPanelSize = null;
          Dimension newPanelSize = null;
          Rectangle scrollRect = null;

          boolean alreadyZoomed = timeline.timelineZoomed;

          if (alreadyZoomed) {
            if (undoManager.getUndoPresentationName().equals("Undo Zoom to Selection") &&
                oldZoomSelection.equals((Vector)timeline.getSelectedBubbles())) {
              timeline.refresh(g2d);
              return;
            }
            //timeline.timelineZoomed = false;
          //}
            int frameWidth = frmTimeline.getWidth();
            oldTimelineLength = timelineLength;
            timelineLength = frameWidth - TimelineFrame.SIDE_SPACE; /// CHANGED from frmTimeline.SIDE_SPACE;
            timeline.freshZoom = true;
            timeline.doLastResize(frameWidth - TimelineFrame.SIDE_SPACE, g2d); /// CHANGED from frmTimeline.SIDE_SPACE;
            oldPanelSize = this.getSize();
            newPanelSize = new Dimension(frameWidth - TimelineFrame.FRAME_SIDE_SPACE, this.getHeight()); /// CHANGED from frmTimeline.FRAME_SIDE_SPACE;
            this.setSize(newPanelSize);
            this.setPreferredSize(newPanelSize);
            this.setMinimumSize(newPanelSize);

            scrollRect = new Rectangle(0, 0, frameWidth - TimelineFrame.FRAME_SIDE_SPACE, 0); /// CHANGED from frmTimeline.FRAME_SIDE_SPACE;
            scrollRectToVisible(scrollRect); 
            timeline.refresh(g2d);
          }
          int newLength = timeline.zoomToSelectedBubbles();
          log.debug("new length = " + newLength);
          timeline.freshZoom = true;
          timeline.doLastResize(newLength, g2d);
          undoManager.undoableEditHappened(new UndoableEditEvent(this,
              new UndoableZoomToSelectedBubbles(oldZoomSelection, oldTimelineLength,
              timelineLength, oldPanelSize, newPanelSize, alreadyZoomed, scrollRect, this)));
          updateUndoMenu();
          oldZoomSelection = (Vector)timeline.getSelectedBubbles().clone();
          	///this.setLineResizing(true);	
          	//this.doResize(newLength/100);
          	//this.setLineResizing(false);
          //timeline.getSlider().setVisible(true);
        timeline.refresh(g2d);
        }
        break;

        // drag selection
      case DRAG_SELECT:
        if (timeline != null) {
          Rectangle r = getRect(dragAnchor, dragEnd);
          boolean draggedFromBelow = dragAnchor.getY() > dragEnd.getY();
          timeline.selectBubbles(r, draggedFromBelow);
          timeline.refresh(g2d);
          drawSelectionBox(g2d, r);
        }
        break;

      // do a single or final line resize
      case DO_RESIZE:
       paintContext = DEFAULT;
       if (timeline != null) {
         timeline.doLastResize(timelineLength, g2d);
         timeline.refresh(g2d);
        }
        break;

      default:
        break;
    }

    // set the context to default
    if (paintContext != DONT_PAINT) {
      paintContext = DEFAULT;
    }

    // now that painting is done, perform any scheduled refreshes
    if (scheduledRefresh) {
      scheduledRefresh = false;
      refreshTimeline();
    }
    else if (scheduledSelect) {
      scheduledSelect = false;
      paintContext = SELECT_BUBBLE;
      repaint();
    }
    else if (scheduledResize) {
      scheduledResize = false;
      paintContext = RESIZING_LINE;
      repaint();
    }

    if (showGroupingError) {
      showGroupingError();
      showGroupingError = false;
      // deselect bubbles
      timeline.deselectAllBubbles();
      timeline.clearSelectedBubbles();
    }

  }

  protected void showGroupingError() {
    javax.swing.SwingUtilities.invokeLater( new Runnable()
    {
      public void run()
      {
        JOptionPane.showMessageDialog(frmTimeline, "Unable to group selected bubbles. Bubble boundaries may not overlap.", "Grouping error", JOptionPane.ERROR_MESSAGE);
      }
    });
  }

  /**
   * event handling methods
   */

  /**
   * mousePressed: recieves mouse presses in the timeline panel, determines if the user clicked on
   * a timepoint or a bubble, and performs actions accordingly
   */
  public void mousePressed(MouseEvent e) {
    this.requestFocus();
    int numClicks = e.getClickCount();
    boolean rightClick = ((e.getModifiers() == e.BUTTON3_MASK) || e.isPopupTrigger());

    if (timeline != null) {
      // they clicked on the resizer
      if (timeline.getResizer().contains(e.getPoint())) {
        if (timeline.isResizable() && timeline.isEditable()) {
          frmTimeline.setCursor(kit.createCustomCursor(UIUtilities.imgHandClosed, new Point(8, 8), "Cursor"));
          oldTimelineLength = timeline.getLineLength();
          resizingLine = true;
          timeline.timelineZoomed = false;
          timeline.getSlider().setVisible(false);
          refreshTimeline();
        }
      }

      // they clicked on the description icon
      else if (timeline.descriptionRect.contains(e.getPoint())) {
         timeline.drawDescriptionIcon((Graphics2D)this.getGraphics(), 2);
         refreshTimeline();
         descriptionWasClicked = true;
         pnlControl.showDescription();
       }

      // they clicked on the timeline rect
      else if (timeline.contains(e.getPoint())) {

        // they clicked on a timepoint
        if (timeline.timepointWasClicked(e.getPoint())) {
          if (timeline.isEditable()) {
            timepointClicked = timeline.getLastTimepointClicked();
            if (timeline.playerIsPlaying()) {
              timeline.pausePlayer();
              wasPlaying = true;
            }
            // right-click: open timepoint popup menu
            if (rightClick) {
              timeline.deselectAllTimepointsAndMarkers();
              timelinePopups.timepointPopup.show(this, e.getX(), e.getY());
              paintContext = SELECT_TIMEPOINT;     // select this timepoint
              repaint();
              uilogger.log(UIEventType.ITEM_RIGHT_CLICK, "timepoint: " + timepointClicked);
            }
            // double-click: open timepoint editor
            else if (numClicks == 2) {
              dlgTimepointEditor = new TimepointEditor(frmTimeline);
              uilogger.log(UIEventType.ITEM_DOUBLE_CLICK, "timepoint (edit label): " + timepointClicked);
            }
            // regular click: set up possible drag
            else {
              dragStartOffset = timeline.getOffsetAt(timepointClicked);
              timeline.setTimepointDragging(true);
              paintContext = SELECT_TIMEPOINT;     // select this timepoint
              repaint();
              uilogger.log(UIEventType.ITEM_SINGLE_CLICK, "timepoint: " + timepointClicked);
            }
          }
        }

        // they may have clicked on a bubble
        else {
          timeline.deselectAllTimepointsAndMarkers();

          // they did click on a bubble
          if (timeline.getBubbleClicked(e.getPoint()) != -1) {

            // right-click: open bubble popup menu
            if (rightClick && timeline.isEditable()) {
              // if not already selected, select the current bubble
              // otherwise, apply property changes to all selected bubbles
              bubbleClicked = timeline.getBubbleClicked(e.getPoint());
              if (timeline.isBubbleSelected(bubbleClicked)) {
              }
              else {
                //  paintContext = SELECT_BUBBLE;
                selectType = SINGLE_CLICK;
                if (!timeline.isBubblePlaying(bubbleClicked)) { // reposition playback if this bubble is not playing
              	  timeline.repositionHead(bubbleClicked);
                 }

                //   scheduledSelect = true;
                timeline.selectBubble(bubbleClicked, selectType);
                refreshTimeline();
              }

              // choose a popup menu based on how many bubbles are selected
              if (timeline.getSelectedBubbles().size() > 1 && timeline.isBubbleSelected(bubbleClicked)) {
                if (timeline.isBubbleGroupSelected() ) {
                  timelinePopups.ungroupBubblePopup.show(this, e.getX(), e.getY());
                }
                else {
                  timelinePopups.groupBubblePopup.show(this, e.getX(), e.getY());
                }
              }
              else if (timeline.getSelectedBubbles().size() == 1) {
                timelinePopups.bubblePopup.show(this, e.getX(), e.getY());
              }
              uilogger.log(UIEventType.ITEM_RIGHT_CLICK, "bubble: " + bubbleClicked);
            }

            // regular click: select the bubble
            else if (numClicks == 1) {
              bubbleClicked = timeline.getBubbleClicked(e.getPoint());
              paintContext = SELECT_BUBBLE;
              
              // determine the selection type
              if (e.isControlDown() || (System.getProperty("os.name").startsWith("Mac OS") && e.isMetaDown())) {
                selectType = CONTROL_CLICK;
                timeline.selectBubble(bubbleClicked, selectType);
                refreshTimeline();
                uilogger.log(UIEventType.ITEM_SINGLE_CLICK, "bubble control-click select: " + bubbleClicked);
              }
              else if (e.isShiftDown()) {
                selectType = SHIFT_CLICK;
                timeline.selectBubble(bubbleClicked, selectType);
                refreshTimeline();
                uilogger.log(UIEventType.ITEM_SINGLE_CLICK, "bubble shift-click select: " + bubbleClicked);
              }
              else { // single click

                  if (!timeline.isBubblePlaying(bubbleClicked)) { // reposition playback if this bubble is not playing
                	  timeline.repositionHead(bubbleClicked);
                   }

                selectType = SINGLE_CLICK;
                timeline.selectBubble(bubbleClicked, selectType);
                refreshTimeline();

                // a single click may initiate a drag
                if (timeline.isMovable()) {
                  draggingTimeline = true;
                  dragOffsetX = e.getX() - timeline.getX() - timeline.getLineStart();
                  dragOffsetY = e.getY() - timeline.getY() - timeline.getLineY();
                }
                uilogger.log(UIEventType.ITEM_SINGLE_CLICK, "bubble select: " + bubbleClicked);
              }
            }

            // double click: open the bubble editor
            else if (numClicks == 2 && timeline.isEditable()) {
              timeline.clearSelectedBubbles();
              timeline.selectBubble(bubbleClicked, this.SINGLE_CLICK);
              uilogger.log(UIEventType.ITEM_DOUBLE_CLICK, "bubble (edit label): " + bubbleClicked);
              dlgBubbleEditor = new TimelineBubbleEditor(frmTimeline);
            }
          }

          // they clicked somewhere between the bubbles in the timeline rect
          else {
            if (rightClick && timeline.isEditable()) {
              timelinePopups.panelPopup.show(this, e.getX(), e.getY());
              uilogger.log(UIEventType.ITEM_RIGHT_CLICK, "timeline panel");
            }

            // set up possible drag select
            dragStarted = true;
            timeline.clearSelectedBoxBubbles();
            dragAnchor = e.getPoint();

            // deselect all, unless shift or control is down
            if (!e.isShiftDown() && !e.isControlDown()) {
              // remove any selectedItems
              timeline.deselectAllBubbles();
              timeline.clearSelectedBubbles();
              timeline.deselectAllTimepointsAndMarkers();
              refreshTimeline();
              pnlControl.updateAnnotationPane();
            }
          }
        }
      }

      // they clicked on a spot other than the timeline rect or the resizer
      else {
        if (rightClick && timeline.isEditable()) {
          timelinePopups.panelPopup.show(this, e.getX(), e.getY());
          uilogger.log(UIEventType.ITEM_RIGHT_CLICK, "timeline panel");
        }

        // set up possible drag select
        dragStarted = true;
        dragAnchor = e.getPoint();

        // deselect all, unless shift or control is down
        if (!e.isShiftDown() && !e.isControlDown()) {
          // remove any selectedItems
          timeline.deselectAllBubbles();
          timeline.clearSelectedBubbles();
          timeline.deselectAllTimepointsAndMarkers();
          refreshTimeline();
          pnlControl.updateAnnotationPane();
        }
      }
    }

    // they right-clicked on the background panel
    else if (rightClick && timeline.isEditable()) {
      timelinePopups.panelPopup.show(this, e.getX(), e.getY());
      uilogger.log(UIEventType.ITEM_RIGHT_CLICK, "timeline panel");
    }
  }

  /**
   * mouseReleased: executes cleanup if a timepoint or marker or the resizer was being dragged
   */
  public void mouseReleased(MouseEvent e) {
    if (timeline != null) {

      // they stopped dragging a timepoint
      if (timeline.isTimepointDragging() && timepointWasDragged) {
        refreshTimeline();
        timepointWasDragged = false;
        timeline.setTimepointDragging(false);
        int tNum = timeline.getLastTimepointClicked();
        Timepoint t = timeline.getTimepoint(tNum);
        t.showTime(timeline.areTimesShown() && timeline.areMarkerTimesShown());
        t.deselect();
        timeline.selectTimepoint(tNum);
        uilogger.log(UIEventType.ITEM_DRAGGED, "timepoint repositioned: " + tNum);

        // check to see if the timepoint was dragged off the line
        if (timeline.isTimepointOffLine()) {
          deleteTimepoint(tNum);
          timeline.setTimepointOffLine(false);
          uilogger.log(UIEventType.ITEM_DRAGGED, "timepoint removed: " + tNum);
        }

        // reposition the timepoint
        else {
          int newOffset = timeline.getOffsetAt(tNum);
          undoManager.undoableEditHappened(new UndoableEditEvent(this,
              new UndoableDragTimepoint(tNum, dragStartOffset, newOffset, this)));
          updateUndoMenu();
          // reposition the playback head
          timeline.getSlider().setValue(timeline.getSortedPointList()[timeline.getLastTimepointClicked()]);
          refreshTimeline();
          //wasPlaying = false;
        }
        // set the player offset
        timeline.setPlayerOffset(timeline.getSlider().getValue());
        if (wasPlaying) {
          timeline.startPlayer();
          wasPlaying = false;
          timeline.showTime(false);
        }
        else {
        	timeline.showTime(true); 
        	timeline.pausePlayer();
        }
      }

      // a timepoint drag began but didn't actually "happen"
      else if (timeline.isTimepointDragging()) {
        timeline.setTimepointDragging(false);
        // reposition the playback head
        //log.debug(timeline.getLastTimepointClicked());
        timeline.getSlider().setValue(timeline.getSortedPointList()[timeline.getLastTimepointClicked()]);
        refreshTimeline();

        // set the player offset
        timeline.setPlayerOffset(timeline.getSlider().getValue());
        if (wasPlaying) {
          timeline.startPlayer();
          wasPlaying = false;
          timeline.showTime(false);                   // show the time under the thumb
        }
        else {timeline.showTime(true); }
      }

      // they stopped dragging a marker
      else if (timeline.isMarkerDragging() && markerWasDragged) {
        refreshTimeline();
        markerWasDragged = false;
        timeline.setMarkerDragging(false);
        int mNum = timeline.getLastMarkerClicked();
        Marker m = timeline.getMarker(mNum);
        m.showTime(timeline.areMarkerTimesShown());
        m.deselect();
        timeline.selectMarker(mNum);
        uilogger.log(UIEventType.ITEM_DRAGGED, "marker repositioned: " + mNum);

        // check to see if the marker was dragged off the line
        if (timeline.isMarkerOffLine()) {
          deleteMarker(mNum);
          timeline.setMarkerOffLine(false);
          uilogger.log(UIEventType.ITEM_DRAGGED, "marker removed: " + mNum);
        }

        // reposition the marker
        else {
          int newOffset = timeline.getMarkerOffsetAt(mNum);
          undoManager.undoableEditHappened(new UndoableEditEvent(this,
              new UndoableDragMarker(mNum, dragMarkerStartOffset, newOffset, this)));
          updateUndoMenu();
        }
        frmTimeline.setCursor(kit.createCustomCursor(UIUtilities.imgHandOpen, new Point(8, 8), "Cursor"));
       }

      // a marker drag began but never "happened"
      else if (timeline.isMarkerDragging()) {
        timeline.setMarkerDragging(false);
        frmTimeline.setCursor(kit.createCustomCursor(UIUtilities.imgHandOpen, new Point(8, 8), "Cursor"));
       }

      // they resized the timeline
      else if (resizingLine) {
        // re-blows the bubbles one last time
        frmTimeline.setCursor(kit.createCustomCursor(UIUtilities.imgHandOpen, new Point(8, 8), "Cursor"));
        timeline.freshZoom = true;
        doResize(e.getX() - timeline.getLineStart());
        resizingLine = false;
        timeline.getSlider().setVisible(true);
        if (lineWasResized) {
          lineWasResized = false;
          undoManager.undoableEditHappened(new UndoableEditEvent(this,
              new UndoableResizeTimeline(oldTimelineLength, timelineLength, this)));
          updateUndoMenu();
          uilogger.log(UIEventType.ITEM_DRAGGED, "timeline resized: " + timelineLength);
        }
      }

      // they dragged the timeline
      else if (draggingTimeline) {
        draggingTimeline = false;
      }

      // reset drag flag
      else if (dragStarted) {
        dragStarted = false;
        if (dragHappened) {
          dragHappened = false;
          timeline.clearSelectedBoxBubbles();
          refreshTimeline();
          uilogger.log(UIEventType.ITEM_DRAGGED, "drag-box-select");
        }
        else {
          // remove any selected items
          timeline.deselectAllBubbles();
          timeline.clearSelectedBubbles();
          timeline.deselectAllTimepointsAndMarkers();
          refreshTimeline();
        }
      }

      // reset description click flag
      else if (descriptionWasClicked) {
        descriptionWasClicked = false;
        timeline.drawDescriptionIcon((Graphics2D)this.getGraphics(), 1); // change back to hover graphic
        refreshTimeline();
      }
    }
  }

  /**
   * mouseDragged: determine if the user is dragging a timepoint or marker or the resizer, and execute
   * handlers accordingly
   */
  public void mouseDragged(MouseEvent e) {
    // if the user is dragging a timepoint, call dragTimepoint with the current point
    if (timeline != null && timeline.isTimepointDragging()) {
      timeline.dragTimepoint(e.getPoint());
      timepointWasDragged = true;
    }
    // if the user is dragging a marker, call dragMarker with the current point
    else if (timeline != null && timeline.isMarkerDragging()) {
      timeline.dragMarker(e.getPoint());
      markerWasDragged = true;
    }
    // if the user is dragging the resizer, resize the line
    else if (isLineResizing()) {
      frmTimeline.setCursor(kit.createCustomCursor(UIUtilities.imgHandClosed, new Point(8, 8), "Cursor"));
      int newLength = e.getX() - timeline.getLineStart();
      if (newLength > 0) {
        resizeTimeline(newLength);
      }
      lineWasResized = true;
    }

    // the user is dragging the timeline (not active in this version)
    else if (isLineDragging()) {
      timeline.moveTo(e.getX() - dragOffsetX, e.getY() - dragOffsetY);
      refreshTimeline();
    }

    // set drag happened to true
    else if (dragStarted) {
      dragHappened = true;
      dragEnd = e.getPoint();
      paintContext = DRAG_SELECT;
      repaint();
    }
  }

  /**
   * mouseMoved: used to detect tool tip changes
   */
  public void mouseMoved (MouseEvent e) {
    showToolTip(e.getPoint());
  }

  public void mouseClicked(MouseEvent e) {
  }

  public void mouseEntered(MouseEvent e) {
  }

  public void mouseExited(MouseEvent e) {
  }

}