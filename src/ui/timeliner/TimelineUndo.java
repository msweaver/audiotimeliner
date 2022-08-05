package ui.timeliner;

import javax.swing.*;
import javax.swing.undo.*;
import java.awt.*;
import java.util.*;
import ui.common.*;

/**
 * TimelineUndo: contains the UndoableEdit classes for the timeliner
 */

public class TimelineUndo {

  /**
   * blank constructor
   */
  public TimelineUndo() {
  }

}
/// UNDO classes ///

class UndoableSetBackgroundColor extends AbstractUndoableEdit
{
  private static final long serialVersionUID = 1L;
  Color oldColor;
  Color newColor;
  boolean wasBlackAndWhite;
  TimelinePanel pnlTimeline;
  Timeline timeline;

  public UndoableSetBackgroundColor(Color oldc, Color newc, boolean bw, TimelinePanel tp) {
    oldColor = oldc;
    newColor = newc;
    wasBlackAndWhite = bw;
    pnlTimeline = tp;
    timeline = tp.getTimeline();
  }
  public String getPresentationName() {
    return "Set Background Color";
  }
  public void undo() {
    super.undo();
    pnlTimeline.setPanelColor(oldColor);
    timeline.lblThumb.setBackground(oldColor);
    if (wasBlackAndWhite) {
      timeline.setBlackAndWhite(true);
    }
    pnlTimeline.scheduleRefresh();
  }
  public void redo() {
    super.redo();
    if (timeline.getBlackAndWhite()) {
      timeline.setBlackAndWhite(false);
    }
    pnlTimeline.setPanelColor(newColor);
    timeline.lblThumb.setBackground(newColor);
    pnlTimeline.scheduleRefresh();
  }
}

class UndoableChangeBubbleColor extends AbstractUndoableEdit
{
  private static final long serialVersionUID = 1L;
  Vector oldColors;
  Color newColor;
  Vector selectedBubbles;
  boolean wasBlackAndWhite;
  TimelinePanel pnlTimeline;
  Timeline timeline;

  UndoableChangeBubbleColor(Vector sb, Vector oldc, Color newc, boolean bw, TimelinePanel tp) {
    oldColors = (Vector)oldc.clone();
    newColor = newc;
    selectedBubbles = (Vector)sb.clone();
    wasBlackAndWhite = bw;
    pnlTimeline = tp;
    timeline = pnlTimeline.getTimeline();
  }
  public String getPresentationName() {
    if (selectedBubbles.size() > 1) {
      return "Change Bubble Colors";
    }
    else return "Change Bubble Color";
  }
  public void undo() {
    super.undo();
    for (int i = 0; i < selectedBubbles.size(); i++) {
      Bubble currBubble = timeline.getBubble(((Integer)selectedBubbles.elementAt(i)).intValue());
      currBubble.setColor((Color)oldColors.elementAt(i));
    }
    if (wasBlackAndWhite) {
      timeline.setBlackAndWhite(true);
    }
    pnlTimeline.scheduleRefresh();
  }
  public void redo() {
    super.redo();
    if (timeline.getBlackAndWhite()) {
      timeline.setBlackAndWhite(false);
    }
    timeline.setSelectedBubbles(selectedBubbles);
    timeline.setSelectedBubbleColor(newColor);
    pnlTimeline.scheduleRefresh();
  }
}

class UndoableSetLevelColor extends AbstractUndoableEdit
{

  private static final long serialVersionUID = 1L;
  Vector levels;
  Vector oldColors;
  Color newColor;
  boolean wasBlackAndWhite;
  TimelinePanel pnlTimeline;
  Timeline timeline;

  UndoableSetLevelColor(Vector lev, Vector oldc, Color newc, boolean bw, TimelinePanel tp) {
    levels = (Vector)lev.clone();
    oldColors = (Vector)oldc.clone();
    newColor = newc;
    wasBlackAndWhite = bw;
    pnlTimeline = tp;
    timeline = pnlTimeline.getTimeline();
  }
  public String getPresentationName() {
    return "Set Level Color";
  }
  public void undo() {
    super.undo();
    for (int i = 0; i < timeline.getNumTotalBubbles(); i++) {
      Bubble currBubble = timeline.getBubble(i);
      if (levels.contains(Integer.valueOf(currBubble.getLevel()))) {
        currBubble.setColor((Color)oldColors.elementAt(i));
      }
    }
    if (wasBlackAndWhite) {
      timeline.setBlackAndWhite(true);
    }
    pnlTimeline.scheduleRefresh();
  }
  public void redo() {
    super.redo();
    if (timeline.getBlackAndWhite()) {
      timeline.setBlackAndWhite(false);
    }
    for (int i = 0; i < levels.size(); i++) {
      timeline.setLevelColor(((Integer)levels.elementAt(i)).intValue(), newColor);
      pnlTimeline.scheduleRefresh();
    }
  }
}

class UndoableAddTimepoint extends AbstractUndoableEdit
{
  private static final long serialVersionUID = 1L;
  int offset;
  int timepointIndex;
  TimelinePanel pnlTimeline;
  Timeline timeline;

  public UndoableAddTimepoint(int off, int timeNum, TimelinePanel tp ) {
    offset = off;
    timepointIndex = timeNum;
    pnlTimeline = tp;
    timeline = pnlTimeline.getTimeline();
  }
  public String getPresentationName() {
    return "Add Timepoint";
  }
  public void undo() {
    super.undo();
    timeline.deleteTimepoint(timepointIndex);
    pnlTimeline.refreshTimeline();
  }
  public void redo() {
    super.redo();
    timeline.addTimepoint(offset);
    pnlTimeline.refreshTimeline();
  }
}

class UndoableAddMarker extends AbstractUndoableEdit
{
  private static final long serialVersionUID = 1L;
  int offset;
  int markerIndex;
  TimelinePanel pnlTimeline;
  Timeline timeline;

  public UndoableAddMarker(int off, int markNum, TimelinePanel tp ) {
    offset = off;
    markerIndex = markNum;
    pnlTimeline = tp;
    timeline = pnlTimeline.getTimeline();
  }
  public String getPresentationName() {
    return "Add Marker";
  }
  public void undo() {
    super.undo();
    timeline.deleteMarker(markerIndex);
    pnlTimeline.refreshTimeline();
  }
  public void redo() {
    super.redo();
    timeline.addMarker(offset);
    pnlTimeline.refreshTimeline();
  }
}

class UndoableResetTimeline extends AbstractUndoableEdit
{
  private static final long serialVersionUID = 1L;
  String savedTimeline;
  TimelinePanel pnlTimeline;
  Timeline tLine;

  public UndoableResetTimeline(String saved, TimelinePanel tp) {
    savedTimeline = saved;
    pnlTimeline = tp;
    tLine = pnlTimeline.getTimeline();
  }
  public String getPresentationName() {
    return "Clear All";
  }
  public void undo() {
    super.undo();
    try {
      TimelineXMLAdapter txmla = new TimelineXMLAdapter();
      Timeline t = txmla.openTimelineXML(savedTimeline, pnlTimeline, (Graphics2D)pnlTimeline.getGraphics());
      pnlTimeline.setTimeline(t);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    pnlTimeline.getFrame().getControlPanel().btn_stopAction();
    pnlTimeline.refreshTimeline();
  }
  public void redo() {
    super.redo();
    pnlTimeline.getFrame().getControlPanel().btn_stopAction();
    pnlTimeline.getTimeline().resetTimeline();
  }
}

class UndoableRevertTimeline extends AbstractUndoableEdit
{
  private static final long serialVersionUID = 1L;
  String savedTimeline;
  String previousSave;
  TimelinePanel pnlTimeline;
  Timeline timeline;

  public UndoableRevertTimeline(String saved, String previous, Timeline t, TimelinePanel tp) {
    savedTimeline = saved;
    previousSave = previous;
    timeline = t;
    pnlTimeline = tp;
  }
  public String getPresentationName() {
    return "Revert to Saved";
  }
  public void undo() {
    super.undo();
    try {
      TimelineXMLAdapter txmla = new TimelineXMLAdapter();
      Timeline t = txmla.openTimelineXML(savedTimeline, pnlTimeline, (Graphics2D)pnlTimeline.getGraphics());
      pnlTimeline.setTimeline(t);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    pnlTimeline.getFrame().getControlPanel().btn_stopAction();
    pnlTimeline.refreshTimeline();
  }
  public void redo() {
    super.redo();
    if (savedTimeline != null) {
      try {
        WindowManager.stopAllPlayers();
        TimelineXMLAdapter time = new TimelineXMLAdapter();
        TimelinePanel tp = timeline.getPanel();
        time.revertTimelineXML(previousSave, tp, (Graphics2D)tp.getGraphics());
      } catch (Exception err) {
        JOptionPane.showMessageDialog(timeline.getPanel().getFrame(), "Error reverting to saved timeline.", "Reverting error",
                                      JOptionPane.ERROR_MESSAGE);
        err.printStackTrace();
      }
    }
  }
}

class UndoableShowTimes extends AbstractUndoableEdit
{
  private static final long serialVersionUID = 1L;
  boolean show;
  TimelinePanel pnlTimeline;

  public UndoableShowTimes(boolean sh, TimelinePanel tp) {
    show = sh;
    pnlTimeline = tp;
  }
  public String getPresentationName() {
    return "Show Times";
  }
  public void undo() {
    super.undo();
    if (System.getProperty("os.name").startsWith("Mac OS")) {
      pnlTimeline.menuiShowTimesMac.setSelected(!show);
      pnlTimeline.menubTimeline.menuiShowTimesMac.setSelected(!show);
    }
    else {
      pnlTimeline.menuiShowTimes.setSelected(!show);
      pnlTimeline.menubTimeline.menuiShowTimes.setSelected(!show);
    }
    pnlTimeline.getTimeline().showTimepointTimes(!show);
    pnlTimeline.refreshTimeline();
  }
  public void redo() {
    super.redo();
    if (System.getProperty("os.name").startsWith("Mac OS")) {
      pnlTimeline.menuiShowTimesMac.setSelected(show);
      pnlTimeline.menubTimeline.menuiShowTimesMac.setSelected(show);
    }
    else {
      pnlTimeline.menuiShowTimes.setSelected(show);
      pnlTimeline.menubTimeline.menuiShowTimes.setSelected(show);
    }
    pnlTimeline.getTimeline().showTimepointTimes(show);
    pnlTimeline.refreshTimeline();
  }
}

class UndoableSetBlackAndWhite extends AbstractUndoableEdit
{
  private static final long serialVersionUID = 1L;
  boolean blackAndWhite;
  TimelinePanel pnlTimeline;

  public UndoableSetBlackAndWhite(boolean bw, TimelinePanel tp) {
    blackAndWhite = bw;
    pnlTimeline = tp;
  }
  public String getPresentationName() {
    return "Black and White";
  }
  public void undo() {
    super.undo();
    pnlTimeline.timelineBlackAndWhite = !blackAndWhite;
    if (System.getProperty("os.name").startsWith("Mac OS")) {
      pnlTimeline.menubTimeline.menuiBlackAndWhiteMac.setSelected(!blackAndWhite);
    }
    else {
      pnlTimeline.menubTimeline.menuiBlackAndWhite.setSelected(!blackAndWhite);
    }
    pnlTimeline.getTimeline().setBlackAndWhite (!blackAndWhite);
    pnlTimeline.refreshTimeline();
  }
  public void redo() {
    super.redo();
    pnlTimeline.timelineBlackAndWhite = blackAndWhite;
    if (System.getProperty("os.name").startsWith("Mac OS")) {
      pnlTimeline.menubTimeline.menuiBlackAndWhiteMac.setSelected(blackAndWhite);
    }
    else {
      pnlTimeline.menubTimeline.menuiBlackAndWhite.setSelected(blackAndWhite);
    }
    pnlTimeline.getTimeline().setBlackAndWhite (blackAndWhite);
    pnlTimeline.refreshTimeline();
  }
}

class UndoableRoundBubbles extends AbstractUndoableEdit
{
  private static final long serialVersionUID = 1L;
  TimelinePanel pnlTimeline;

  public UndoableRoundBubbles(TimelinePanel tp) {
    pnlTimeline = tp;
  }
  public String getPresentationName() {
    return "Round Bubbles";
  }
  public void undo() {
    super.undo();
    if (System.getProperty("os.name").startsWith("Mac OS")) {
      pnlTimeline.menubTimeline.menuiRoundBubblesMac.setSelected(false);
      pnlTimeline.menubTimeline.menuiSquareBubblesMac.setSelected(true);
    }
    else {
      pnlTimeline.menubTimeline.menuiRoundBubbles.setSelected(false);
      pnlTimeline.menubTimeline.menuiSquareBubbles.setSelected(true);
    }
    pnlTimeline.getTimeline().setBubbleType(1);
    pnlTimeline.refreshTimeline();
  }
  public void redo() {
    super.redo();
    if (System.getProperty("os.name").startsWith("Mac OS")) {
      pnlTimeline.menubTimeline.menuiRoundBubblesMac.setSelected(true);
      pnlTimeline.menubTimeline.menuiSquareBubblesMac.setSelected(false);
    }
    else {
      pnlTimeline.menubTimeline.menuiRoundBubbles.setSelected(true);
      pnlTimeline.menubTimeline.menuiSquareBubbles.setSelected(false);
    }
    pnlTimeline.getTimeline().setBubbleType(0);
    pnlTimeline.refreshTimeline();
  }
}

class UndoableSquareBubbles extends AbstractUndoableEdit
{
  private static final long serialVersionUID = 1L;
  TimelinePanel pnlTimeline;

  public UndoableSquareBubbles(TimelinePanel tp) {
    pnlTimeline = tp;
  }
  public String getPresentationName() {
    return "Square Bubbles";
  }
  public void undo() {
    super.undo();
    if (System.getProperty("os.name").startsWith("Mac OS")) {
      pnlTimeline.menubTimeline.menuiRoundBubblesMac.setSelected(true);
      pnlTimeline.menubTimeline.menuiSquareBubblesMac.setSelected(false);
    }
    else {
      pnlTimeline.menubTimeline.menuiRoundBubbles.setSelected(true);
      pnlTimeline.menubTimeline.menuiSquareBubbles.setSelected(false);
    }
    pnlTimeline.getTimeline().setBubbleType(0);
    pnlTimeline.refreshTimeline();
  }
  public void redo() {
    super.redo();
    if (System.getProperty("os.name").startsWith("Mac OS")) {
      pnlTimeline.menubTimeline.menuiRoundBubbles.setSelected(false);
      pnlTimeline.menubTimeline.menuiSquareBubbles.setSelected(true);
    }
    else {
      pnlTimeline.menubTimeline.menuiRoundBubbles.setSelected(false);
      pnlTimeline.menubTimeline.menuiSquareBubbles.setSelected(true);
    }
    pnlTimeline.getTimeline().setBubbleType(1);
    pnlTimeline.refreshTimeline();
  }
}

class UndoableFitToWindow extends AbstractUndoableEdit
{
  int oldTimelineLength;
  int newTimelineLength;
  Dimension oldPanelSize;
  Dimension newPanelSize;
  boolean wasZoomed;
  Vector oldSelection;
  Rectangle oldScrollRect;
  TimelinePanel pnlTimeline;
  Timeline timeline;
  Graphics2D g2d;

  public UndoableFitToWindow(int oldtl, int newtl, Dimension oldps, Dimension newps, boolean zoom, Vector old, Rectangle oldRect, TimelinePanel tp) {
    oldTimelineLength = oldtl;
    newTimelineLength = newtl;
    newPanelSize = newps;
    wasZoomed = zoom;
    oldSelection = (Vector)old.clone();
    oldScrollRect = oldRect;
    pnlTimeline = tp;
    timeline = pnlTimeline.getTimeline();
    g2d = (Graphics2D)pnlTimeline.getGraphics();
  }
  public String getPresentationName() {
    return "Fit to Window";
  }
  public void undo() {
    super.undo();
    if (wasZoomed) {
      timeline.setSelectedBubbles(oldSelection);
      int newLength = timeline.zoomToSelectedBubbles();
      timeline.doLastResize(newLength, g2d);
      pnlTimeline.scrollRectToVisible(oldScrollRect);
      timeline.refresh(g2d);
    }
    else {
      timeline.doLastResize(oldTimelineLength, g2d);
      pnlTimeline.refreshTimeline();
    }
  }
  public void redo() {
    super.redo();
    timeline.timelineZoomed = false;
    pnlTimeline.timelineLength = newTimelineLength;
    timeline.doLastResize(newTimelineLength, g2d);
    timeline.refresh(g2d);
    pnlTimeline.setSize(newPanelSize);
    pnlTimeline.setPreferredSize(newPanelSize);
    pnlTimeline.setMinimumSize(newPanelSize);
    timeline.refresh(g2d);
  }
}

class UndoableZoomToSelectedBubbles extends AbstractUndoableEdit
{
  Vector oldSelectedBubbles = new Vector();
  Vector newSelectedBubbles = new Vector();
  int oldTimelineLength;
  int newTimelineLength;
  Dimension oldPanelSize;
  Dimension newPanelSize;
  Rectangle scrollRect;
  TimelinePanel pnlTimeline;
  Timeline timeline;
  Graphics2D g2d;
  boolean wasZoomed;

  public UndoableZoomToSelectedBubbles(Vector sb, int oldtl, int newtl,
                                       Dimension oldps, Dimension newps, boolean wz, Rectangle rect, TimelinePanel tp) {
    pnlTimeline = tp;
    timeline = pnlTimeline.getTimeline();
    oldSelectedBubbles = (Vector)sb.clone();
    newSelectedBubbles = (Vector)timeline.getSelectedBubbles().clone();
    oldTimelineLength = oldtl;
    newTimelineLength = newtl;
    oldPanelSize = oldps;
    newPanelSize = newps;
    scrollRect = rect;
    wasZoomed = wz;
    g2d  = (Graphics2D)pnlTimeline.getGraphics();
  }
  public String getPresentationName() {
    return "Zoom to Selection";
  }
  public void undo() {
    super.undo();
    if (wasZoomed && oldSelectedBubbles != null) {
      timeline.timelineZoomed = false;
      timeline.setSelectedBubbles(oldSelectedBubbles);
      pnlTimeline.setSize(oldPanelSize);
      pnlTimeline.setPreferredSize(oldPanelSize);
      pnlTimeline.setMinimumSize(oldPanelSize);
      pnlTimeline.scrollRectToVisible(scrollRect);
      int newLength = timeline.zoomToSelectedBubbles();
      timeline.freshZoom = true;
      timeline.doLastResize(newLength, g2d);
      timeline.refresh(g2d);
      pnlTimeline.oldZoomSelection = (Vector)oldSelectedBubbles.clone();
    }
    else {
      timeline.doLastResize(oldTimelineLength, g2d);
      timeline.refresh(g2d);
    }
  }
  public void redo() {
    super.redo();
    if (wasZoomed) {
      timeline.timelineZoomed = false;
      pnlTimeline.timelineLength = newTimelineLength;
      timeline.freshZoom = true;
      timeline.doLastResize(newTimelineLength, g2d);
      pnlTimeline.setSize(newPanelSize);
      pnlTimeline.setPreferredSize(newPanelSize);
      pnlTimeline.setMinimumSize(newPanelSize);
      pnlTimeline.scrollRectToVisible(scrollRect);
      timeline.refresh(g2d);
    }
    timeline.setSelectedBubbles(newSelectedBubbles);
    int newLength = timeline.zoomToSelectedBubbles();
    timeline.freshZoom = true;
    timeline.doLastResize(newLength, g2d);
    timeline.refresh(g2d);
    pnlTimeline.oldZoomSelection = (Vector)newSelectedBubbles.clone();
  }
}

class UndoableGroupBubbles extends AbstractUndoableEdit
{
  Vector selectedBubbles;
  TimelinePanel pnlTimeline;
  Timeline timeline;

  public UndoableGroupBubbles(Vector selBub, TimelinePanel tp) {
    selectedBubbles = (Vector)selBub.clone();
    pnlTimeline = tp;
    timeline = pnlTimeline.getTimeline();
  }
  public String getPresentationName() {
    return "Group Bubbles";
  }
  public void undo() {
    super.undo();
    Vector alteredBubbles = (Vector)selectedBubbles.clone();
    int startBub = ((Integer)alteredBubbles.elementAt(0)).intValue();
    int lastBub = (((Integer)alteredBubbles.elementAt(alteredBubbles.size()-1)).intValue());
    // add one to each bubble to offset the added group bubble
    alteredBubbles.setElementAt(Integer.valueOf(startBub + 1), 0);
    alteredBubbles.setElementAt(Integer.valueOf(lastBub + 1), alteredBubbles.size()-1);
    timeline.setSelectedBubbles(alteredBubbles);
    timeline.ungroupSelectedBubbles();
    pnlTimeline.refreshTimeline();
  }
  public void redo() {
    super.redo();
    timeline.setSelectedBubbles(selectedBubbles);
    timeline.groupSelectedBubbles();
    pnlTimeline.refreshTimeline();
  }
}

class UndoableUngroupBubbles extends AbstractUndoableEdit
{
  Vector selectedBubbles;
  Bubble oldBub = new Bubble();
  int bubbleNum;
  TimelinePanel pnlTimeline;
  Timeline timeline;

  public UndoableUngroupBubbles(Vector selBub, Bubble bub, int bubNum, TimelinePanel tp) {
    selectedBubbles = (Vector)selBub.clone();
    oldBub.setColor(bub.getColor());
    oldBub.setLabel(bub.getLabel());
    oldBub.setAnnotation(bub.getAnnotation());
    bubbleNum = bubNum;
    pnlTimeline = tp;
    timeline = pnlTimeline.getTimeline();
  }
  public String getPresentationName() {
    return "Ungroup Bubbles";
  }
  public void undo() {
    super.undo();
    Vector alteredBubbles = (Vector)selectedBubbles.clone();
    int startBub = ((Integer)alteredBubbles.elementAt(0)).intValue();
    int lastBub = (((Integer)alteredBubbles.elementAt(alteredBubbles.size()-1)).intValue());
    // delete one from each bubble to offset the removed group bubble
    alteredBubbles.setElementAt(Integer.valueOf(startBub - 1), 0);
    alteredBubbles.setElementAt(Integer.valueOf(lastBub - 1), alteredBubbles.size()-1);
    timeline.setSelectedBubbles(alteredBubbles);
    timeline.groupSelectedBubbles();
    Bubble newBub = timeline.getBubble(bubbleNum);
    newBub.setColor(oldBub.getColor());
    newBub.setLabel(oldBub.getLabel());
    newBub.setAnnotation(oldBub.getAnnotation());
    pnlTimeline.refreshTimeline();
  }
  public void redo() {
    super.redo();
    timeline.setSelectedBubbles(selectedBubbles);
    timeline.ungroupSelectedBubbles();
    pnlTimeline.refreshTimeline();
  }
}

class UndoableDeleteBubble extends AbstractUndoableEdit
{
  Vector selectedBubbles;
  Vector offsets;
  Vector timepointLabels;
  Vector baseColors;
  Vector baseLabels;
  Vector baseAnnotations;
  Vector groupStarts;
  Vector groupEnds;
  Vector groupColors;
  Vector groupLabels;
  Vector groupAnnotations;
  TimelinePanel pnlTimeline;
  Timeline timeline;

  public UndoableDeleteBubble(Vector selBub, Vector off, Vector time, Vector basec, Vector basel, Vector basea, Vector starts, Vector ends,
                              Vector groupc, Vector groupl, Vector groupa, TimelinePanel tp) {
    selectedBubbles = (Vector)selBub.clone();
    offsets = (Vector)off.clone();
    timepointLabels = (Vector)time.clone();
    baseColors = (Vector)basec.clone();
    baseLabels = (Vector)basel.clone();
    baseAnnotations = (Vector)basea.clone();
    groupStarts = (Vector)starts.clone();
    groupEnds = (Vector)ends.clone();
    groupColors = (Vector)groupc.clone();
    groupLabels = (Vector)groupl.clone();
    groupAnnotations = (Vector)groupa.clone();
    pnlTimeline = tp;
    timeline = pnlTimeline.getTimeline();
  }
  public String getPresentationName() {
    if (selectedBubbles.size() > 1)	{
      return "Delete Bubbles";
    }
    else {
      return "Delete Bubble";
    }
  }
  public void undo() {
    super.undo();

    // first re-add the points and set the annotations, colors and labels for the base bubbles
    for (int i = 0; i < offsets.size(); i++) {
      int currOffset = ((Integer)offsets.elementAt(i)).intValue();
      timeline.addTimepoint(currOffset);
      int currNum = timeline.getTimepointNumberAt(currOffset);
      timeline.setTimepointLabel(currNum, ((String)timepointLabels.elementAt(i)));
      Bubble currBubble = timeline.getBaseBubble(currNum);
      currBubble.setColor((Color)baseColors.elementAt(i));
      currBubble.setLabel((String)baseLabels.elementAt(i));
      currBubble.setAnnotation((String)baseAnnotations.elementAt(i));
    }
    pnlTimeline.refreshTimeline();
    timeline.blowBubbles();
    timeline.sortList();

    // now re-group bubbles and set their annotations, colors and labels
    Vector currSelected = new Vector();
    for (int i = 0; i < groupStarts.size(); i++) {
      int startPixels = (((Integer)groupStarts.elementAt(i)).intValue());
      int endPixels = (((Integer)groupEnds.elementAt(i)).intValue());
      int startPoint = timeline.getTimepointNumberAtPixel(startPixels);
      int endPoint = timeline.getTimepointNumberAtPixel(endPixels);
      int startOffset = timeline.getOffsetAt(startPoint);
      int endOffset = timeline.getOffsetAt(endPoint);
      BubbleTreeNode startNode = timeline.getBaseBubbleNode(startPoint);
      BubbleTreeNode endNode = timeline.getBaseBubbleNode(endPoint - 1);
      int startPos = timeline.topBubbleNode.getPreOrderIndex(startNode);
      int endPos = timeline.topBubbleNode.getPreOrderIndex(endNode);
      currSelected.addElement(Integer.valueOf(startPos));
      currSelected.addElement(Integer.valueOf(endPos));
      timeline.setSelectedBubbles(currSelected);
      timeline.groupSelectedBubbles();
      timeline.blowBubbles();
      Bubble currBubble = (timeline.getHighestLevelNodeWithin(startPixels, endPixels)).getBubble();
      currBubble.setColor((Color)groupColors.elementAt(i));
      currBubble.setLabel((String)groupLabels.elementAt(i));
      currBubble.setAnnotation((String)groupAnnotations.elementAt(i));
      currSelected.removeAllElements();
    }
    pnlTimeline.refreshTimeline();
    pnlTimeline.getFrame().getControlPanel().updateAnnotationPane();
  }
  public void redo() {
    super.redo();
    timeline.setSelectedBubbles(selectedBubbles);
    timeline.deleteSelectedBubbles();
    pnlTimeline.refreshTimeline();
  }
}

class UndoableDeleteTimepoint extends AbstractUndoableEdit
{
  int timepointNum;
  int offset;
  Vector timepointLabels;
  Vector baseColors;
  Vector baseLabels;
  Vector baseAnnotations;
  Vector groupStarts;
  Vector groupEnds;
  Vector groupColors;
  Vector groupLabels;
  Vector groupAnnotations;
  TimelinePanel pnlTimeline;
  Timeline timeline;

  public UndoableDeleteTimepoint(int tn, int off, Vector time, Vector basec, Vector basel, Vector basea, Vector starts, Vector ends,
                                 Vector groupc, Vector groupl, Vector groupa, TimelinePanel tp) {
    timepointNum = tn;
    offset = off;
    timepointLabels = (Vector)time.clone();
    baseColors = (Vector)basec.clone();
    baseLabels = (Vector)basel.clone();
    baseAnnotations = (Vector)basea.clone();
    groupStarts = (Vector)starts.clone();
    groupEnds = (Vector)ends.clone();
    groupColors = (Vector)groupc.clone();
    groupLabels = (Vector)groupl.clone();
    groupAnnotations = (Vector)groupa.clone();
    pnlTimeline = tp;
    timeline = pnlTimeline.getTimeline();
  }
  public String getPresentationName() {
    return "Delete Timepoint";
  }
  public void undo() {
    super.undo();

    // first re-add the point and set the base bubble color, annotation, and label
    timeline.addTimepoint(offset);
    timeline.setTimepointLabel(timepointNum, ((String)timepointLabels.elementAt(0)));
    Bubble curBubble = timeline.getBaseBubble(timepointNum);
    curBubble.setColor((Color)baseColors.elementAt(0));
    curBubble.setLabel((String)baseLabels.elementAt(0));
    curBubble.setAnnotation((String)baseAnnotations.elementAt(0));
    pnlTimeline.refreshTimeline();
    timeline.blowBubbles();
    timeline.sortList();

    // now re-group bubbles and set their annotations, colors and labels
    Vector currSelected = new Vector();
    for (int i = 0; i < groupStarts.size(); i++) {
      int startPixels = (((Integer)groupStarts.elementAt(i)).intValue());
      int endPixels = (((Integer)groupEnds.elementAt(i)).intValue());
      int startPoint = timeline.getTimepointNumberAtPixel(startPixels);
      int endPoint = timeline.getTimepointNumberAtPixel(endPixels);
      int startOffset = timeline.getOffsetAt(startPoint);
      int endOffset = timeline.getOffsetAt(endPoint);
      BubbleTreeNode startNode = timeline.getBaseBubbleNode(startPoint);
      BubbleTreeNode endNode = timeline.getBaseBubbleNode(endPoint - 1);
      int startPos = timeline.topBubbleNode.getPreOrderIndex(startNode);
      int endPos = timeline.topBubbleNode.getPreOrderIndex(endNode);
      currSelected.addElement(Integer.valueOf(startPos));
      currSelected.addElement(Integer.valueOf(endPos));
      timeline.setSelectedBubbles(currSelected);
      timeline.groupSelectedBubbles();
      timeline.blowBubbles();
      Bubble currBubble = (timeline.getHighestLevelNodeWithin(startPixels, endPixels)).getBubble();
      currBubble.setColor((Color)groupColors.elementAt(i));
      currBubble.setLabel((String)groupLabels.elementAt(i));
      currBubble.setAnnotation((String)groupAnnotations.elementAt(i));
      currSelected.removeAllElements();
    }
    pnlTimeline.refreshTimeline();

  }
  public void redo() {
    super.redo();
    timeline.deleteTimepoint(timepointNum);
    pnlTimeline.refreshTimeline();
  }
}

class UndoableDeleteMarker extends AbstractUndoableEdit
{
  int markerNum;
  int offset;
  String markerLabel;
  String markerAnnotation;
  TimelinePanel pnlTimeline;
  Timeline timeline;

  public UndoableDeleteMarker(int mn, int off, String label, String annotation, TimelinePanel tp) {
    markerNum = mn;
    offset = off;
    markerLabel = label;
    markerAnnotation = annotation;
    pnlTimeline = tp;
    timeline = pnlTimeline.getTimeline();
  }
  public String getPresentationName() {
    return "Delete Marker";
  }
  public void undo() {
    super.undo();
    timeline.addMarker(offset);
    timeline.setMarkerLabel(markerNum, markerLabel);
    timeline.setMarkerAnnotation(markerNum, markerAnnotation);
    pnlTimeline.refreshTimeline();
  }
  public void redo() {
    super.redo();
    timeline.deleteMarker(markerNum);
    pnlTimeline.refreshTimeline();
  }
}

class UndoableMoveSelectedBubblesUp extends AbstractUndoableEdit
{
  Vector selectedBubbles;
  Timeline time;

  public UndoableMoveSelectedBubblesUp(Vector sb, Timeline t) {
    selectedBubbles = (Vector)sb.clone();
    time = t;
  }
  public String getPresentationName() {
    return "Move Bubble Up a Level";
  }
  public void undo() {
    super.undo();
    try {
      for (int i = 0; i < selectedBubbles.size(); i++) {
        int currBub = ((Integer)selectedBubbles.elementAt(i)).intValue();
        Bubble bub = time.getBubble(currBub);
        BubbleTreeNode currNode = time.getBubbleNode(currBub);
        boolean hasRoom = true;
        for (int j = 0; j < currNode.getChildCount(); j++) {
          if (((BubbleTreeNode)currNode.getChildAt(j)).getBubble().getLevel()==(bub.level-1)) {
            hasRoom = false;
          }
        }
        if (bub.level > 1 && hasRoom) {
          if (bub.getColor().equals(time.bubbleLevelColors[bub.level])) {
            bub.setColor(time.bubbleLevelColors[bub.level-1]);
          }
          bub.setLevel(bub.level - 1);
          bub.levelWasUserAdjusted = true;
          // update the popup menus
          time.updatePopupMenus();
        }
      }
    } catch (Exception err) {
      err.printStackTrace();
    }
  }
  public void redo() {
    super.redo();
    try {
      for (int i = 0; i < selectedBubbles.size(); i++) {
        int currBub = ((Integer)selectedBubbles.elementAt(i)).intValue();
        Bubble bub = time.getBubble(currBub);
        BubbleTreeNode currNode = time.getBubbleNode(currBub);
        if (((BubbleTreeNode)currNode.getParent()).getBubble().getLevel()>(bub.getLevel()+1)) {
          if (bub.getColor().equals(time.bubbleLevelColors[bub.level])) {
            bub.setColor(time.bubbleLevelColors[bub.level+1]);
          }
          bub.setLevel(bub.level + 1);
          bub.levelWasUserAdjusted = true;
          // update the popup menus
          time.updatePopupMenus();
        }
      }
    } catch (Exception err) {
      err.printStackTrace();
    }
  }
}

class UndoableMoveSelectedBubblesDown extends AbstractUndoableEdit
{
  Vector selectedBubbles;
  Timeline time;

  public UndoableMoveSelectedBubblesDown(Vector sb, Timeline t) {
    selectedBubbles = (Vector)sb.clone();
    time = t;
  }
  public String getPresentationName() {
    return "Move Bubble Down a Level";
  }
  public void undo() {
    super.undo();
    try {
      for (int i = 0; i < selectedBubbles.size(); i++) {
        int currBub = ((Integer)selectedBubbles.elementAt(i)).intValue();
        Bubble bub = time.getBubble(currBub);
        BubbleTreeNode currNode = time.getBubbleNode(currBub);
        if (((BubbleTreeNode)currNode.getParent()).getBubble().getLevel()>(bub.getLevel()+1)) {
          if (bub.getColor().equals(time.bubbleLevelColors[bub.level])) {
            bub.setColor(time.bubbleLevelColors[bub.level+1]);
          }
          bub.setLevel(bub.level + 1);
          bub.levelWasUserAdjusted = true;
          // update the popup menus
          time.updatePopupMenus();
        }
      }
    } catch (Exception err) {
      err.printStackTrace();
    }
  }
  public void redo() {
    super.redo();
    try {
      for (int i = 0; i < selectedBubbles.size(); i++) {
        int currBub = ((Integer)selectedBubbles.elementAt(i)).intValue();
        Bubble bub = time.getBubble(currBub);
        BubbleTreeNode currNode = time.getBubbleNode(currBub);
        boolean hasRoom = true;
        for (int j = 0; j < currNode.getChildCount(); j++) {
          if (((BubbleTreeNode)currNode.getChildAt(j)).getBubble().getLevel()==(bub.level-1)) {
            hasRoom = false;
          }
        }
        if (bub.level > 1 && hasRoom) {
          if (bub.getColor().equals(time.bubbleLevelColors[bub.level])) {
            bub.setColor(time.bubbleLevelColors[bub.level-1]);
          }
          bub.setLevel(bub.level - 1);
          bub.levelWasUserAdjusted = true;
          // update the popup menus
          time.updatePopupMenus();
        }
      }
    } catch (Exception err) {
      err.printStackTrace();
    }
  }
}

class UndoableDragTimepoint extends AbstractUndoableEdit
{
  int oldOffset;
  int newOffset;
  int timepointNum;
  TimelinePanel pnlTimeline;
  Timeline timeline;

  public UndoableDragTimepoint(int tnum, int oldoff, int newoff, TimelinePanel tp) {
    timepointNum = tnum;
    oldOffset = oldoff;
    newOffset = newoff;
    pnlTimeline = tp;
    timeline = tp.getTimeline();
  }
  public String getPresentationName() {
    return "Drag Timepoint";
  }
  public void undo() {
    super.undo();
    timeline.setPointAt(timepointNum, oldOffset);
    Timepoint t = timeline.getTimepoint(timepointNum);
    t.showTime(timeline.areTimesShown());
    t.deselect();
    timeline.selectTimepoint(timepointNum);
    pnlTimeline.refreshTimeline();
  }
  public void redo() {
    super.redo();
    timeline.setPointAt(timepointNum, newOffset);
    Timepoint t = timeline.getTimepoint(timepointNum);
    t.showTime(timeline.areTimesShown());
    t.deselect();
    timeline.selectTimepoint(timepointNum);
    pnlTimeline.refreshTimeline();
  }
}

class UndoableDragMarker extends AbstractUndoableEdit
{
  int oldOffset;
  int newOffset;
  int markerNum;
  TimelinePanel pnlTimeline;
  Timeline timeline;

  public UndoableDragMarker(int mnum, int oldoff, int newoff, TimelinePanel tp) {
    markerNum = mnum;
    oldOffset = oldoff;
    newOffset = newoff;
    pnlTimeline = tp;
    timeline = tp.getTimeline();
  }
  public String getPresentationName() {
    return "Drag Marker";
  }
  public void undo() {
    super.undo();
    timeline.setMarkerAt(markerNum, oldOffset);
    Marker m = timeline.getMarker(markerNum);
    m.showTime(timeline.areTimesShown());
    m.deselect();
    timeline.selectMarker(markerNum);
    pnlTimeline.refreshTimeline();
  }
  public void redo() {
    super.redo();
    timeline.setMarkerAt(markerNum, newOffset);
    Marker m = timeline.getMarker(markerNum);
    m.showTime(timeline.areTimesShown());
    m.deselect();
    timeline.selectMarker(markerNum);
    pnlTimeline.refreshTimeline();
  }
}

class UndoableResizeTimeline extends AbstractUndoableEdit
{
  int oldTimelineLength;
  int newTimelineLength;
  TimelinePanel pnlTimeline;

  public UndoableResizeTimeline(int oldtl, int newtl, TimelinePanel tp) {
    oldTimelineLength = oldtl;
    newTimelineLength = newtl;
    pnlTimeline = tp;
  }
  public String getPresentationName() {
    return "Resize Timeline";
  }
  public void undo() {
    super.undo();
    pnlTimeline.doResize(oldTimelineLength);
  }
  public void redo() {
    super.redo();
    pnlTimeline.doResize(newTimelineLength);
  }
}

class UndoableEditTimepoint extends AbstractUndoableEdit
{
  Vector oldLabels;
  Vector newLabels;
  Vector oldOverlaps;
  Vector newOverlaps;
  Vector timepointNumbers;
  Timeline timeline;

  public UndoableEditTimepoint(Vector oldl, Vector newl, Vector oldo, Vector newo, Vector tpNum, Timeline t) {
    oldLabels = (Vector)oldl.clone();
    newLabels = (Vector)newl.clone();
    oldOverlaps = (Vector)oldo.clone();
    newOverlaps = (Vector)newo.clone();
    timepointNumbers = (Vector)tpNum.clone();
    timeline = t;
  }
  public String getPresentationName() {
    if (timepointNumbers.size() > 1) {
      return "Edit Timepoints";
    }
    else return "Edit Timepoint";
  }
  public void undo() {
    super.undo();
    for (int i = 0; i < timepointNumbers.size(); i++) {
      Timepoint currTimepoint = timeline.getTimepoint(((Integer)timepointNumbers.elementAt(i)).intValue());
      currTimepoint.setLabel((String)oldLabels.elementAt(i));
      currTimepoint.setOverlap((Boolean)oldOverlaps.elementAt(i));
    }
    timeline.getPanel().refreshTimeline();
    timeline.getPanel().getFrame().getControlPanel().updateAnnotationPane();
  }
  public void redo() {
    super.redo();
    for (int i = 0; i < timepointNumbers.size(); i++) {
      Timepoint currTimepoint = timeline.getTimepoint(((Integer)timepointNumbers.elementAt(i)).intValue());
      currTimepoint.setLabel((String)newLabels.elementAt(i));
      currTimepoint.setOverlap((Boolean)newOverlaps.elementAt(i));
    }
    timeline.getPanel().refreshTimeline();
    timeline.getPanel().getFrame().getControlPanel().updateAnnotationPane();
  }
}

class UndoableEditMarker extends AbstractUndoableEdit
{
  Vector oldLabels;
  Vector newLabels;
  Vector oldAnnotations;
  Vector newAnnotations;
  Vector markerNumbers;
  Timeline timeline;

  public UndoableEditMarker(Vector oldl, Vector newl, Vector olda, Vector newa, Vector markNum, Timeline t) {
    oldLabels = (Vector)oldl.clone();
    newLabels = (Vector)newl.clone();
    oldAnnotations = (Vector)olda.clone();
    newAnnotations = (Vector)newa.clone();
    markerNumbers = (Vector)markNum.clone();
    timeline = t;
  }
  public String getPresentationName() {
    if (markerNumbers.size() > 1) {
      return "Edit Markers";
    }
    else return "Edit Marker";
  }
  public void undo() {
    super.undo();
    for (int i = 0; i < markerNumbers.size(); i++) {
      Marker currMarker = timeline.getMarker(((Integer)markerNumbers.elementAt(i)).intValue());
      currMarker.setLabel((String)oldLabels.elementAt(i));
      currMarker.setAnnotation((String)oldAnnotations.elementAt(i));
    }
    timeline.getPanel().refreshTimeline();
    timeline.getPanel().getFrame().getControlPanel().updateAnnotationPane();
  }
  public void redo() {
    super.redo();
    for (int i = 0; i < markerNumbers.size(); i++) {
      Marker currMarker = timeline.getMarker(((Integer)markerNumbers.elementAt(i)).intValue());
      currMarker.setLabel((String)newLabels.elementAt(i));
      currMarker.setAnnotation((String)newAnnotations.elementAt(i));
    }
    timeline.getPanel().refreshTimeline();
    timeline.getPanel().getFrame().getControlPanel().updateAnnotationPane();
  }
}

class UndoableEditBubble extends AbstractUndoableEdit
{
  Vector oldLabels;
  Vector newLabels;
  Vector oldAnnotations;
  Vector newAnnotations;
  Vector bubbleNumbers;
  Timeline timeline;

  public UndoableEditBubble(Vector oldl, Vector newl, Vector olda, Vector newa, Vector bubNum, Timeline t) {
    oldLabels = (Vector)oldl.clone();
    newLabels = (Vector)newl.clone();
    oldAnnotations = (Vector)olda.clone();
    newAnnotations = (Vector)newa.clone();
    bubbleNumbers = (Vector)bubNum.clone();
    timeline = t;
  }
  public String getPresentationName() {
    if (bubbleNumbers.size() > 1) {
      return "Edit Bubbles";
    }
    else return "Edit Bubble";
  }
  public void undo() {
    super.undo();
    for (int i = 0; i < bubbleNumbers.size(); i++) {
      Bubble currBubble = timeline.getBubble(((Integer)bubbleNumbers.elementAt(i)).intValue());
      currBubble.setLabel((String)oldLabels.elementAt(i));
      currBubble.setAnnotation((String)oldAnnotations.elementAt(i));
    }
    timeline.getPanel().refreshTimeline();
    timeline.getPanel().getFrame().getControlPanel().updateAnnotationPane();
  }
  public void redo() {
    super.redo();
    for (int i = 0; i < bubbleNumbers.size(); i++) {
      Bubble currBubble = timeline.getBubble(((Integer)bubbleNumbers.elementAt(i)).intValue());
      currBubble.setLabel((String)newLabels.elementAt(i));
      currBubble.setAnnotation((String)newAnnotations.elementAt(i));
    }
    timeline.getPanel().refreshTimeline();
    timeline.getPanel().getFrame().getControlPanel().updateAnnotationPane();
  }
}

class UndoableEditProperties extends AbstractUndoableEdit
{
  String oldTitle;
  String newTitle;
  String oldDescription;
  String newDescription;
  boolean oldEditable;
  boolean newEditable;
  boolean oldResizable;
  boolean newResizable;
  boolean oldShowTimes;
  boolean newShowTimes;
  boolean oldBW;
  boolean newBW;
  boolean oldSquareBubbles;
  boolean newSquareBubbles;
  int oldBubbleHeight;
  int newBubbleHeight;
  boolean oldAutoScale;
  boolean newAutoScale;
  Color[] oldLevelColors = new Color[11];
  Color[] newLevelColors = new Color[11];
  Color oldBackgroundColor;
  Color newBackgroundColor;
  int oldColorScheme;
  int newColorScheme;
  Vector oldColors;
  boolean[] levelChanged = new boolean[11];
  boolean oldPlayWhenClicked;
  boolean newPlayWhenClicked;
  boolean oldStopAtEnd;
  boolean newStopAtEnd;
  Timeline timeline;
  TimelinePanel pnlTimeline;
  TimelineProperties tProp;

  public UndoableEditProperties(String oldt, String newt, String oldd, String newd, boolean olde, boolean newe, boolean oldr, boolean newr, boolean oldst, boolean newst,
                                boolean oldbw, boolean newbw, boolean oldsb, boolean newsb, int oldbh, int newbh, boolean oldas, boolean newas, Color oldlc[], Color newlc[],
                                int oldcs, int newcs, Vector oldc, boolean lc[], boolean oldpwc, boolean newpwc, boolean oldsae, boolean newsae, TimelinePanel tp,
                                Color oldbc, Color newbc, TimelineProperties tpr) {
    oldTitle = oldt;
    newTitle = newt;
    oldDescription = oldd;
    newDescription = newd;
    oldEditable = olde;
    newEditable = newe;
    oldResizable = oldr;
    newResizable = newr;
    oldShowTimes = oldst;
    newShowTimes = newst;
    oldBW = oldbw;
    newBW = newbw;
    oldSquareBubbles = oldsb;
    newSquareBubbles = newsb;
    oldBubbleHeight = oldbh;
    newBubbleHeight = newbh;
    oldAutoScale = oldas;
    newAutoScale = newas;
    for (int i = 1; i < 11; i++) {
      oldLevelColors[i] = oldlc[i];
    }
    for (int i = 1; i < 11; i++) {
      newLevelColors[i] = newlc[i];
    }
    oldColorScheme = oldcs;
    newColorScheme = newcs;
    oldColors = (Vector)oldc.clone();
    for (int i = 1; i < 11; i++) {
      levelChanged[i] = lc[i];
    }
    oldPlayWhenClicked = oldpwc;
    newPlayWhenClicked = newpwc;
    oldStopAtEnd = oldsae;
    newStopAtEnd = newsae;
    pnlTimeline = tp;
    timeline = tp.getTimeline();
    oldBackgroundColor = oldbc;
    newBackgroundColor = newbc;
    tProp = tpr;
  }
  public String getPresentationName() {
    return "Edit Properties";
  }
  public void undo() {
    super.undo();
    TimelineMenuBar tmb = pnlTimeline.getMenuBar();

    // undo property changes
    pnlTimeline.getFrame().setTitle("Timeline: " + oldTitle);
    timeline.setDescription(oldDescription);
    pnlTimeline.setEditableTimeline(oldEditable);
    pnlTimeline.setResizableTimeline(oldResizable);
    if (System.getProperty("os.name").startsWith("Mac OS")) {
      pnlTimeline.menuiShowTimesMac.setSelected(oldShowTimes);
      tmb.menuiShowTimesMac.setSelected(oldShowTimes);
      tmb.menuiBlackAndWhiteMac.setSelected(oldBW);
      tmb.menuiRoundBubblesMac.setSelected(!oldSquareBubbles);
      tmb.menuiSquareBubblesMac.setSelected(oldSquareBubbles);
    }
    else {
      pnlTimeline.menuiShowTimes.setSelected(oldShowTimes);
      tmb.menuiShowTimes.setSelected(oldShowTimes);
      tmb.menuiBlackAndWhite.setSelected(oldBW);
      tmb.menuiRoundBubbles.setSelected(!oldSquareBubbles);
      tmb.menuiSquareBubbles.setSelected(oldSquareBubbles);
    }
    timeline.showTimepointTimes(oldShowTimes);
    pnlTimeline.setPanelColor(oldBackgroundColor);
    timeline.lblThumb.setBackground(oldBackgroundColor);
    pnlTimeline.timelineBlackAndWhite = oldBW;
    timeline.setBlackAndWhite(oldBW);
    if (oldSquareBubbles){
      timeline.setBubbleType(1);
    }
    else {
      timeline.setBubbleType(0);
    }
    timeline.setBubbleHeight(oldBubbleHeight * 8);
    timeline.setAutoScaling(oldAutoScale);
    for (int i = 1; i < 11; i++) {
      timeline.bubbleLevelColors[i] = oldLevelColors[i];
      if (levelChanged[i]) {
        timeline.setLevelColor(i, timeline.bubbleLevelColors[i]);
      }
    }
    timeline.colorScheme = oldColorScheme;
    for (int i = 0; i <= timeline.getNumTotalBubbles(); i++) {
      Bubble currBubble = timeline.getBubble(i);
      currBubble.setColor((Color)oldColors.elementAt(i));
    }

    timeline.playWhenBubbleClicked = oldPlayWhenClicked;
    timeline.stopPlayingAtSelectionEnd = oldStopAtEnd;

  }
  public void redo() {
    super.redo();
    TimelineMenuBar tmb = pnlTimeline.getMenuBar();

    // redo property changes
    pnlTimeline.getFrame().setTitle("Timeline: " + newTitle);
    timeline.setDescription(newDescription);
    pnlTimeline.setEditableTimeline(newEditable);
    pnlTimeline.setResizableTimeline(newResizable);
    if (System.getProperty("os.name").startsWith("Mac OS")) {
      pnlTimeline.menuiShowTimesMac.setSelected(newShowTimes);
      pnlTimeline.getMenuBar().menuiShowTimesMac.setSelected(newShowTimes);
      pnlTimeline.getMenuBar().menuiBlackAndWhiteMac.setSelected(newBW);
      tmb.menuiRoundBubblesMac.setSelected(!newSquareBubbles);
      tmb.menuiSquareBubblesMac.setSelected(newSquareBubbles);
    }
    else {
      pnlTimeline.menuiShowTimes.setSelected(newShowTimes);
      pnlTimeline.getMenuBar().menuiShowTimes.setSelected(newShowTimes);
      pnlTimeline.getMenuBar().menuiBlackAndWhite.setSelected(newBW);
      tmb.menuiRoundBubbles.setSelected(!newSquareBubbles);
      tmb.menuiSquareBubbles.setSelected(newSquareBubbles);
    }
    timeline.showTimepointTimes(newShowTimes);
    pnlTimeline.setPanelColor(newBackgroundColor);
    timeline.lblThumb.setBackground(newBackgroundColor);
    pnlTimeline.timelineBlackAndWhite = newBW;
    timeline.setBlackAndWhite(newBW);
    if (newSquareBubbles){
      timeline.setBubbleType(1);
    }
    else {
      timeline.setBubbleType(0);
    }
    timeline.setBubbleHeight(newBubbleHeight);
    timeline.setAutoScaling(newAutoScale);
    for (int i = 1; i < 11; i++) {
      timeline.bubbleLevelColors[i] = newLevelColors[i];
      if (levelChanged[i]) {
        timeline.setLevelColor(i, timeline.bubbleLevelColors[i]);
      }
    }
    timeline.colorScheme = newColorScheme;
    timeline.playWhenBubbleClicked = newPlayWhenClicked;
    timeline.stopPlayingAtSelectionEnd = newStopAtEnd;
  }
}