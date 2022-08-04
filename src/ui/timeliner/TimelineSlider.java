package ui.timeliner;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import util.AppEnv;
import util.logging.*;
import org.apache.log4j.Logger;
import ui.common.*;

/**
 * TimelineSlider class
 * This class represents a timeline slider, which is an extension of JSlider. The slider uses the
 * TimelineLookAndFeel through the TimelineSliderUI. The class also implements a number of
 * event handlers that affect the timeline.
 *
 * @author Brent Yorgason
 */

public class TimelineSlider extends JSlider {

  private static final long serialVersionUID = 1L;
// external components
  private TimelineSliderUI sliderUI;
  private Timeline timeline;
  private TimelineResizer tResizer;
  protected TimelinePanel pnlTimeline;
  protected MarkerEditor dlgMarkerEditor;
  protected TimepointEditor dlgTimepointEditor;
  Toolkit kit = Toolkit.getDefaultToolkit();
  private static Logger log = Logger.getLogger(TimelineSlider.class);
  protected UILogger uilogger;

  // variables
  final private int offset = 5;  // this has to do with the layout of the slider object
  private boolean wasPlaying = false;
  private boolean dragStarted = false;
  private boolean dragHappened = false;
  private boolean doubleclickHappened = false;
  private boolean timepointWasDragged = false;
  private boolean markerWasDragged = false;
  private boolean rightClickHappened = false;
  private Point dragAnchor = new Point();
  private Point dragEnd = new Point();

  /**
   * constructor: creates a timeline slider, given the orientation, min and max values, current
   * value, and a reference to the timeline
   */
  public TimelineSlider(int orient, int min, int max, int value, int width, int height, Timeline t) {

    // set the slider UI
    sliderUI = new TimelineSliderUI(this);

    // set up the slider properties
    this.setUI(sliderUI);
    this.setOrientation(orient);
    this.setMinimum(min);
    this.setMaximum(max);
    this.setValue(value);
    this.setSize(width, height);
    this.setPaintTicks(true);
    this.setVisible(true);
    this.setOpaque(false); 

    // get external references
    timeline = t;
    tResizer = timeline.getResizer();
    pnlTimeline = timeline.getPanel();
    uilogger = pnlTimeline.getFrame().getUILogger();

    // handle events
    addListeners();
  }

  /**
   * addListeners: adds listeners to the slider and behavior to respond to mouse events
   */
  private void addListeners() {
    this.addMouseListener(new MouseAdapter() {

      /**
       * mousePressed: determines if the user clicked on the slider, on a timepoint or marker, or on the resizer
       * handling events accordingly
       */
      public void mousePressed(MouseEvent e) {
        pnlTimeline.requestFocus();
        int numClicks = e.getClickCount();

        // this complex equation gets the location of the mouse click within the timeline panel
        Point panelPoint = new Point(e.getX() + timeline.getLineStart() - offset,
                                     e.getY() + timeline.getLineY());
        rightClickHappened = ((e.getModifiers() == e.BUTTON3_MASK) || e.isPopupTrigger());

        // determine if the user clicked on a timepoint
        if (timeline.timepointWasClicked(panelPoint)) {
          if (timeline.isEditable()) {
            int timepointClicked = timeline.getLastTimepointClicked();
            pnlTimeline.setTimepointClicked(timepointClicked);
            sliderUI.setDraggableThumb(false);                       // disallow thumb repositioning

            // determine how the timepoint was clicked
            if (rightClickHappened) { // right-click: open timepoint popup menu
              timeline.deselectAllTimepointsAndMarkers();
              pnlTimeline.timelinePopups.timepointPopup.show(pnlTimeline, (int)panelPoint.getX(), (int)panelPoint.getY());
              pnlTimeline.selectTimepoint(timepointClicked);  // 'select' the timepoint
              uilogger.log(UIEventType.ITEM_RIGHT_CLICK, "timepoint: " + timepointClicked);
            }
            else if (numClicks == 2) { // double click: open timepoint editor
              dlgTimepointEditor = new TimepointEditor(pnlTimeline.getFrame());
              uilogger.log(UIEventType.ITEM_DOUBLE_CLICK, "timepoint (edit label): " + timepointClicked);
            }
            else { // regular click: start a possible drag
              pnlTimeline.dragStartOffset = timeline.getOffsetAt(timepointClicked);
              timeline.setTimepointDragging(true);                      // signal a dragging event
              pnlTimeline.selectTimepoint(timepointClicked);  // 'select' the timepoint
              uilogger.log(UIEventType.ITEM_SINGLE_CLICK, "timepoint select: " + timepointClicked);
              if (timeline.playerIsPlaying()) {
                  timeline.pausePlayer();        // pause playback
                  wasPlaying = true;
                }
            }
          }
        }

        // determine if the user clicked on a marker
        else if (timeline.markerWasClicked(panelPoint)) {
          if (timeline.isEditable()) {
            int markerClicked = timeline.getLastMarkerClicked();
            pnlTimeline.setMarkerClicked(markerClicked);
            sliderUI.setDraggableThumb(false);                       // disallow thumb repositioning

            // determine how the marker was clicked
            if (rightClickHappened) { // right click: open the marker popup menu
              timeline.deselectAllTimepointsAndMarkers();
              pnlTimeline.timelinePopups.markerPopup.show(pnlTimeline, (int)panelPoint.getX(), (int)panelPoint.getY());
              pnlTimeline.selectMarker(markerClicked);  // 'select' the marker
              uilogger.log(UIEventType.ITEM_RIGHT_CLICK, "marker: " + markerClicked);
            }
            else if (numClicks == 2) { // double click: open the marker editor
              dlgMarkerEditor = new MarkerEditor(pnlTimeline.getFrame());
              uilogger.log(UIEventType.ITEM_DOUBLE_CLICK, "marker (edit): " + markerClicked);
            }
            else { // regular click: start a possible drag
              pnlTimeline.dragMarkerStartOffset = timeline.getMarkerOffsetAt(markerClicked);
              timeline.setMarkerDragging(true);                      // signal a dragging event
              pnlTimeline.selectMarker(markerClicked);  // 'select' the marker
              uilogger.log(UIEventType.ITEM_SINGLE_CLICK, "marker select: " + markerClicked);
              if (timeline.playerIsPlaying()) {
                  timeline.pausePlayer();        // pause playback
                  wasPlaying = true;
                }
           }
          }
        }

        // determine if the user clicked on the resizer
        else if (tResizer.contains(panelPoint)) {
          if (timeline.isResizable() && timeline.isEditable()) {

            // start a resize action: change the cursor
            pnlTimeline.getFrame().setCursor(kit.createCustomCursor(UIUtilities.imgHandClosed, new Point(8, 8), "Cursor"));
            pnlTimeline.oldTimelineLength = timeline.getLineLength();
            pnlTimeline.setLineResizing(true);
            timeline.getSlider().setVisible(false);
            timeline.timelineZoomed = false;
            pnlTimeline.refreshTimeline();
          }
        }

        // determine if the user clicked on the slider
        else {
          // right click: open the timeline popup menu
          if (rightClickHappened && timeline.isEditable()) {
            pnlTimeline.timelinePopups.timelinePopup.show(pnlTimeline, (int)panelPoint.getX(), (int)panelPoint.getY());
            uilogger.log(UIEventType.ITEM_RIGHT_CLICK, "timeline");
          }
          // double click: add a timepoint
          else if (e.getClickCount() == 2 && timeline.isEditable()) {
          	doubleclickHappened = true;
            pnlTimeline.addTimepoint();
            if (timeline.areTimesShown()) {
              timeline.showTime(false);
            }
            uilogger.log(UIEventType.ITEM_DOUBLE_CLICK, "timeline (add timepoint)");
          }
          // regular click
          else {
            if (timeline.playerIsPlaying()) {
              timeline.pausePlayer();        // pause playback
              wasPlaying = true;
            }
            // clicked on the slider thumb
            if (sliderUI.thumbRect.contains(e.getPoint())) { // change cursor
              pnlTimeline.getFrame().setCursor(kit.createCustomCursor(UIUtilities.imgHandClosed, new Point(8, 8), "Cursor"));
            }
            else { // potential drag-select
              dragStarted = true;
              timeline.clearSelectedBoxBubbles();
              dragAnchor = e.getPoint();

              // adjust to panel rect
              dragAnchor.x = dragAnchor.x + timeline.getLineStart();
              dragAnchor.y = dragAnchor.y + timeline.getLineY();

              // deselect all, unless shift or control is down
              if (!e.isShiftDown() && !e.isControlDown()) {
                // remove any selectedItems
                timeline.deselectAllBubbles();
                timeline.clearSelectedBubbles();
                timeline.deselectAllTimepointsAndMarkers();
                pnlTimeline.refreshTimeline();
                pnlTimeline.getFrame().getControlPanel().updateAnnotationPane();
              }
            }
          }
        }
      }

      /**
       * mouseReleased: if the user was dragging a timepoint or marker or the resizer, responds to the event
       * also responds to standard thumb repositioning--positioning playback
       */
      public void mouseReleased(MouseEvent e) {
        // if user was dragging a timepoint
        if (timeline.isTimepointDragging() && timepointWasDragged) {
          pnlTimeline.refreshTimeline();
          timeline.setTimepointDragging(false);      // signal the end of the dragging event
          timepointWasDragged = false;
          int tNum = timeline.getLastTimepointClicked();
          Timepoint t = timeline.getTimepoint(tNum);
          t.showTime(timeline.areTimesShown());
          t.deselect();
          timeline.selectTimepoint(tNum);         // select the timepoint
          sliderUI.setDraggableThumb(true);       // slider can now be repositioned again
          uilogger.log(UIEventType.ITEM_DRAGGED, "timepoint repositioned: " + tNum);

          // check to see if the timepoint was dragged off the line
          if (timeline.isTimepointOffLine()) {
            pnlTimeline.deleteTimepoint(tNum);
            timeline.setTimepointOffLine(false);
            wasPlaying = false;
            uilogger.log(UIEventType.ITEM_DRAGGED, "timepoint removed: " + tNum);
          }
          else { // or reposition the timepoint
            int newOffset = timeline.getOffsetAt(tNum);
            pnlTimeline.undoManager.undoableEditHappened(new UndoableEditEvent(this,
                new UndoableDragTimepoint(tNum, pnlTimeline.dragStartOffset, newOffset, pnlTimeline)));
            pnlTimeline.updateUndoMenu();

            // reposition the playback head
            setValue(timeline.getSortedPointList()[timeline.getLastTimepointClicked()]);
            pnlTimeline.refreshTimeline();

            // set the player offset
            timeline.setPlayerOffset(getValue());
            if (wasPlaying) {
              timeline.startPlayer();
              wasPlaying = false;
              timeline.showTime(false);
            }
            else {timeline.showTime(true); }
            pnlTimeline.getFrame().getControlPanel().updateAnnotationPane();
          }
        }

        // if a drag started but didn't "happen"
        else if (timeline.isTimepointDragging() ) {
          timeline.setTimepointDragging(false);      // signal the end of the dragging event
          setValue(timeline.getSortedPointList()[timeline.getLastTimepointClicked()]); // reposition the playback head
          pnlTimeline.refreshTimeline();             // refresh the timeline

          // set the player offset
          timeline.setPlayerOffset(getValue());
          if (wasPlaying) {
            timeline.startPlayer();
            wasPlaying = false;
            timeline.showTime(false);
          }
          else {timeline.showTime(true); }
        }

        // if user was dragging a marker
        else if (timeline.isMarkerDragging() && markerWasDragged) {
          pnlTimeline.refreshTimeline();
          timeline.setMarkerDragging(false);      // signal the end of the dragging event
          markerWasDragged = false;
          int mNum = timeline.getLastMarkerClicked();
          Marker m = timeline.getMarker(mNum);
          m.showTime(timeline.areTimesShown());
          m.deselect();
          timeline.selectMarker(mNum);            // select the marker
          sliderUI.setDraggableThumb(true);       // slider can now be repositioned again
          uilogger.log(UIEventType.ITEM_DRAGGED, "marker repositioned: " + mNum);

          // check to see if the marker was dragged off the line
          if (timeline.isMarkerOffLine()) {
            pnlTimeline.deleteMarker(mNum);
            timeline.setMarkerOffLine(false);
            wasPlaying = false;
            uilogger.log(UIEventType.ITEM_DRAGGED, "marker removed: " + mNum);
          }
          else { // or reposition the marker
            int newOffset = timeline.getMarkerOffsetAt(mNum);
            pnlTimeline.undoManager.undoableEditHappened(new UndoableEditEvent(this,
                new UndoableDragMarker(mNum, pnlTimeline.dragMarkerStartOffset, newOffset, pnlTimeline)));
            pnlTimeline.updateUndoMenu();
            // reposition the playback head
            setValue(timeline.getMarkerList()[timeline.getLastMarkerClicked()]);
            pnlTimeline.refreshTimeline();

            // set the player offset
            timeline.setPlayerOffset(getValue());
            if (wasPlaying) {
              timeline.startPlayer();
              wasPlaying = false;
              timeline.showTime(false);
            }
            else {timeline.showTime(true); }
          }
          // reset the cursor
          pnlTimeline.getFrame().setCursor(kit.createCustomCursor(UIUtilities.imgHandOpen, new Point(8, 8), "Cursor"));
        }

        // if a marker drag started but didn't "happen"
        else if (timeline.isMarkerDragging() ) {
          timeline.setMarkerDragging(false);      // signal the end of the dragging event
          pnlTimeline.getFrame().setCursor(kit.createCustomCursor(UIUtilities.imgHandOpen, new Point(8, 8), "Cursor"));
          // reposition the playback head
          setValue(timeline.getMarkerList()[timeline.getLastMarkerClicked()]);
          pnlTimeline.refreshTimeline();

          // set the player offset
          timeline.setPlayerOffset(getValue());
          if (wasPlaying) {
            timeline.startPlayer();
            wasPlaying = false;
            timeline.showTime(false);
          }
          else {timeline.showTime(true); }
        }

        // if user was resizing the line
        else if (pnlTimeline.isLineResizing()) {
          // re-blows the bubbles one last time
          pnlTimeline.getFrame().setCursor(kit.createCustomCursor(UIUtilities.imgHandOpen, new Point(8, 8), "Cursor"));
          int newSize = e.getX() - offset;
          pnlTimeline.doResize(newSize);
          pnlTimeline.setLineResizing(false);
          timeline.getSlider().setVisible(true);
          pnlTimeline.undoManager.undoableEditHappened(new UndoableEditEvent(this,
              new UndoableResizeTimeline(pnlTimeline.oldTimelineLength, newSize, pnlTimeline)));
          pnlTimeline.updateUndoMenu();
          uilogger.log(UIEventType.ITEM_DRAGGED, "timeline resized: " + newSize);
        }

        // reset drag flag
        else if (dragHappened) {
          dragStarted = false;
          dragHappened = false;
          timeline.clearSelectedBoxBubbles();
          pnlTimeline.refreshTimeline();
          if (wasPlaying) {
              timeline.startPlayer();
              wasPlaying = false;
            }
          uilogger.log(UIEventType.ITEM_DRAGGED, "drag-box-select");
        }
        // reset right-click flag
        else if (rightClickHappened) {
          rightClickHappened = false;
        }
        // reset double-click flag
        else if (doubleclickHappened) {
          doubleclickHappened = false;
        }
        
        // user clicked on slider without dragging or moved the thumb
        else {
          dragStarted = false;
          sliderUI.jumpDueToClickInTrack(e.getX());  // have the thumb jump to the point
          log.debug("milliseconds = " + getValue());
          log.debug("jump to " + e.getX());
          timeline.setPlayerOffset(getValue());
          timeline.deselectAllBubbles();             // deselect any selections
          timeline.clearSelectedBubbles();
          timeline.deselectAllTimepointsAndMarkers();
          pnlTimeline.refreshTimeline();             // refresh the timeline

          // set the player offset
          if (wasPlaying) {
            timeline.startPlayer();
            wasPlaying = false;
            timeline.showTime(false);
          }
          else {
            timeline.showTime(true);
          }
          // change the cursor back to a hand
          pnlTimeline.getFrame().setCursor(kit.createCustomCursor(UIUtilities.imgHandOpen, new Point(8, 8), "Cursor"));
          pnlTimeline.getFrame().getControlPanel().updateAnnotationPane();
        }
      }

      /**
       * mouseEntered: sets custom cursors
       */
      public void mouseEntered (MouseEvent e) {
        Point panelPoint = new Point(e.getX() + timeline.getLineStart() - offset,
                                     e.getY() + timeline.getLineY());
        // cursor over timepoint
        if (timeline.timepointsContain(panelPoint) && timeline.isEditable()) {
          if (System.getProperty("os.name").startsWith("Mac OS")) {
            pnlTimeline.getFrame().setCursor(kit.createCustomCursor(UIUtilities.imgMovePoint, new Point(8, 8), "Cursor"));
          } else {
            pnlTimeline.getFrame().setCursor(kit.createCustomCursor(UIUtilities.imgMovePoint, new Point(16, 16), "Cursor"));
          }
        }

        // cursor over marker
        if (timeline.markersContain(panelPoint) && timeline.isEditable()) {
          pnlTimeline.getFrame().setCursor(kit.createCustomCursor(UIUtilities.imgHandOpen, new Point(8, 8), "Cursor"));
        }

        // cursor over thumb
        else if (sliderUI.thumbRect.contains(new Point(e.getX(), e.getY()))) {
          pnlTimeline.getFrame().setCursor(kit.createCustomCursor(UIUtilities.imgHandOpen, new Point(8, 8), "Cursor"));
        }

        // cursor over resizer
        else if (timeline.isResizable() && timeline.isEditable() && tResizer.contains(panelPoint)) {
          pnlTimeline.getFrame().setCursor(kit.createCustomCursor(UIUtilities.imgHandOpen, new Point(8, 8), "Cursor"));
        }

        // not over anything
        else {
          pnlTimeline.getFrame().setCursor(Cursor.getDefaultCursor());
        }
      }
    });

    this.addMouseMotionListener(new MouseMotionAdapter() {

      /**
       * mouseDragged: determine if the user is dragging a timepoint or marker or the resizer, and execute
       * handlers accordingly
       */
      public void mouseDragged(MouseEvent e) {
        // get the current mouse location in the timeline panel
        Point panelPoint = new Point(e.getX() + timeline.getLineStart() - offset,
                                     e.getY() + timeline.getLineY());

        // if the user is dragging a timepoint, call dragTimepoint with the current point
        if (timeline.isTimepointDragging() ) {
          pnlTimeline.getFrame().setCursor(kit.createCustomCursor(UIUtilities.imgHandClosed, new Point(8, 8), "Cursor"));
          timeline.dragTimepoint(panelPoint);
          timepointWasDragged = true;
        }

        // if the user is dragging a marker, call dragMarker with the current point
        else if (timeline.isMarkerDragging() ) {
          pnlTimeline.getFrame().setCursor(kit.createCustomCursor(UIUtilities.imgHandClosed, new Point(8, 8), "Cursor"));
          timeline.dragMarker(panelPoint);
          markerWasDragged = true;
        }

        // if the user is dragging the resizer, resize the line
        else if (pnlTimeline.isLineResizing()) {
          if (System.getProperty("os.name").startsWith("Mac OS")) {
            pnlTimeline.getFrame().setCursor(kit.createCustomCursor(kit.getImage(AppEnv.getAppDir()+"resources/media/handclosed.gif"), new Point(8, 8), "Cursor"));
          }
          else {
            pnlTimeline.getFrame().setCursor(kit.createCustomCursor(kit.getImage(AppEnv.getAppDir()+"resources/media/handclosed.gif"), new Point(8, 8), "Cursor"));
          }
          int newLength = e.getX() - offset;
          if (newLength > 0) {
            pnlTimeline.resizeTimeline(newLength);
          }
        }

        // or draw the drag selection box
        else if (dragStarted) {
          dragHappened = true;
          timeline.clearSelectedBoxBubbles();
          dragEnd = e.getPoint();
          dragEnd.x = dragEnd.x + timeline.getLineStart();
          dragEnd.y = dragEnd.y + timeline.getLineY();
          pnlTimeline.selectRect(dragAnchor, dragEnd);
        }

        // otherwise drag the slider pointer
        else {
          pnlTimeline.getFrame().setCursor(kit.createCustomCursor(UIUtilities.imgHandClosed, new Point(8, 8), "Cursor"));
          pnlTimeline.refreshTimeline();
          sliderUI.jumpDueToClickInTrack(e.getX());
          timeline.showTime(true);
        }
      }

      /**
       * mouseEntered: sets custom cursors when moving items
       */
      public void mouseMoved (MouseEvent e) {
        Point panelPoint = new Point(e.getX() + timeline.getLineStart() - offset,
                                     e.getY() + timeline.getLineY());
        if (timeline.timepointsContain(panelPoint) && timeline.isEditable()) {
          if (System.getProperty("os.name").startsWith("Mac OS")) {
            pnlTimeline.getFrame().setCursor(kit.createCustomCursor(UIUtilities.imgMovePoint, new Point(8, 8), "Cursor"));
          } else {
            pnlTimeline.getFrame().setCursor(kit.createCustomCursor(UIUtilities.imgMovePoint, new Point(16, 16), "Cursor"));
          }
          timeline.getSlider().setToolTipText(null);
        }
        else if (timeline.markersContain(panelPoint) && timeline.isEditable()) {
          pnlTimeline.getFrame().setCursor(kit.createCustomCursor(UIUtilities.imgHandOpen, new Point(8, 8), "Cursor"));
          int markerHover = timeline.getMarkerAt(panelPoint);
          String tip = ((Marker)timeline.getMarker(markerHover)).getAnnotation();
          if (tip.length() == 0) {
            timeline.getSlider().setToolTipText(null);
          }
          else {
            timeline.getSlider().setToolTipText(tip);
          }
        }
        else if (sliderUI.thumbRect.contains(e.getPoint())) {
          pnlTimeline.getFrame().setCursor(kit.createCustomCursor(UIUtilities.imgHandOpen, new Point(8, 8), "Cursor"));
          timeline.getSlider().setToolTipText(null);
        }
        else if (tResizer.contains(panelPoint) && timeline.isResizable() && timeline.isEditable()) {
          pnlTimeline.getFrame().setCursor(kit.createCustomCursor(UIUtilities.imgHandOpen, new Point(8, 8), "Cursor"));
          timeline.getSlider().setToolTipText(null);
        }
        else {
          pnlTimeline.getFrame().setCursor(Cursor.getDefaultCursor());
          timeline.getSlider().setToolTipText(null);
        }
      }
    });
  }

}