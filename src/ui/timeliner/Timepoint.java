package ui.timeliner;

import java.awt.*;

import ui.common.UIUtilities;

/**
 * Timepoint class
 * This class represents a timepoint object. Timepoints are created in connection with
 * timeline objects, on which they are drawn.
 *
 * @author Brent Yorgason
 */

public class Timepoint {

  // private variables
  private Color color = new Color(0,0,0);
  private int width = UIUtilities.scalePixels(3);
  private int height = UIUtilities.scalePixels(10);
  private int timepointX;
  private int timepointY;
  private String label = "";
  private String annotation = "";
  private String time = "";
  private boolean selected;
  private boolean visibleTime;
  private int timeWidth = 0;
  private int timeStart = 0;
  private int timeFontSize = 11;
  private boolean visibleLabel = true;
  private int labelWidth = 0;
  private int labelHeight = 0;
  private int labelStart = 0;
  private int labelFontSize = 12;
  private boolean isOverlap = false;
  //private Graphics g;
  private Graphics2D g2d;

  // timepoint rect
  public Rectangle timepointRect = new Rectangle();

  /**
   * constructor
   */
  public Timepoint() {
    selected = false;
    visibleTime = true;
  }

  /**
   * getTimepoint: returns a copy of the timepoint
   */
  protected Timepoint getTimepoint() {
    return this;
  }

  /**
   * contains: returns true if the point passed is contained within the timepoint
   */
  public boolean contains(Point p) {
    return timepointRect.contains(p);
  }

  /**
   * drawTimepoint: given a point and the current graphics, draws a timepoint
   */
  protected void drawTimepoint(Graphics2D graphics, int xLoc, int yLoc, Color background) {
    g2d = graphics;
    timepointX = xLoc - (width / 2);
    timepointY = yLoc - (height / 2);

    // set up the timepoint label
    Font labelFont = new Font("Arial Unicode MS", 0, labelFontSize);
    g2d.setFont(labelFont);
    FontMetrics labelMetrics = g2d.getFontMetrics();
    labelWidth = labelMetrics.stringWidth(label);
    labelHeight = labelMetrics.getHeight();
    int labelX = xLoc - (labelWidth / 2);
    int labelY = yLoc + 34;
    labelStart = labelX;

    // set up the timepoint time
    Font timeFont = new Font("Arial Unicode MS", 0, timeFontSize);
    g2d.setFont(timeFont);
    FontMetrics timeMetrics = g2d.getFontMetrics();
    timeWidth = timeMetrics.stringWidth(time);
    int timeX = xLoc - (timeWidth / 2);
    int timeY = yLoc + 48;
    timeStart = timeX;

    // now draw the timepoint
    if (isOverlap) {
    	//Image overlap = UIUtilities.imgOverlap;
    	//g2d.drawImage(overlap, timepointX-7, timepointY-40, 16, 10, null);
    	//g2d.fillRect(timepointX, timepointY, width, height);
    	int overW = 10;
    	//int overH = 5;
    	int overY = 40;
    	g2d.setColor(Color.black);
    	g2d.drawLine(timepointX-overW, timepointY-overY, timepointX+overW, timepointY-overY);
    	g2d.drawLine(timepointX-overW, timepointY-overY, timepointX-overW, timepointY-overY+5);
    	g2d.drawLine(timepointX+overW, timepointY-overY, timepointX+overW, timepointY-overY+5);
    }
    
    if (!selected) {

      g2d.setColor(color);
      g2d.fillRect(timepointX, timepointY, width, height);
    }
    else {
      g2d.setColor(Color.white);
      g2d.fillRect(timepointX, timepointY, width, height);
      g2d.setColor(color);
      g2d.drawRect(timepointX - 1, timepointY - 1, width + 2, height + 2);
      g2d.drawRect(timepointX, timepointY, width, height);
    }

    // draw the timepoint label
    if (visibleLabel && label.length() > 0) {
      // draw an "opaque" background for the label
      g2d.setColor(background);
      g2d.fillRect(labelX-1, labelY-labelHeight, labelWidth+2, labelHeight);
      // draw the label
      g2d.setColor(Color.black);
      g2d.setFont(labelFont);
      g2d.drawString(label, labelX, labelY);
    }

    // draw the timepoint time
    if (visibleTime && time.length() > 0) {
      g2d.setColor(Color.black);
      g2d.setFont(timeFont);
      g2d.drawString(time, timeX, timeY);
    }

    // update timepoint rect
    timepointRect.setBounds(timepointX, timepointY, width, height);
  }

  /**
   * select: marks the timepoint as selected
   */
  protected void select() {
    selected = true;
  }

  /**
   * deselect: marks the timepoint as deselected
   */
  protected void deselect() {
    selected = false;
  }

  /**
   * isSelected: returns true if the timepoint is selected
   */
  public boolean isSelected() {
    return selected;
  }

  /**
   * getLabel: returns the timepoint label
   */
  public String getLabel() {
    return label;
  }

  /**
   * getOverlap: returns whether the timepoint is an overlap
   */
  public Boolean getOverlap() {
    return isOverlap;
  }

  /**
   * getTimepointX: returns the x position of the timepoint
   */
  protected int getTimepointX() {
    return timepointX;
  }

  /**
   * getTime: returns the text string representing the timepoint time
   */
  protected String getTime() {
    return time;
  }

  /**
   * setLabel: sets the label text for the timepoint
   */
  protected void setLabel(String labelText) {
    label = labelText;
  }

  /**
   * setTime: sets the displayed time for the timepoint
   */
  protected void setTime(String t) {
    time = t;
  }

  /**
   * showTime: sets whether the time should be shown
   */
  protected void showTime (boolean state) {
    visibleTime = state;
  }

  /**
   * showLabel: sets whether the label should be shown
   */
  protected void showLabel (boolean state) {
    visibleLabel = state;
  }

  /**
   * getLabelWidth: returns width of timepoint label
   */
  protected int getLabelWidth() {
    return labelWidth;
  }

  /**
   * getLabelStart: returns start point (in pixels) of timepoint label
   */
  protected int getLabelStart() {
    return labelStart;
  }

  /**
   * setLabelFontSize: sets font size of timepoint label
   */
  protected void setLabelFontSize(int size) {
    labelFontSize = size;
  }

  /**
   * getLabelFontSize: gets font size of timepoint label
   */
  protected int getLabelFontSize() {
    return labelFontSize;
  }

  /**
   * getTimeWidth: gets width of timepoint time
   */
  protected int getTimeWidth() {
    return timeWidth;
  }

  /**
   * getTimeStart: gets start point (in pixels) of timepoint time
   */
  protected int getTimeStart() {
    return timeStart;
  }

  /**
   * setTimeFontSize: sets font size of timepoint time
   */
  protected void setTimeFontSize(int size) {
    timeFontSize = size;
  }

  /**
   * getTimeFontSize: gets font size of timepoint time
   */
  protected int getTimeFontSize() {
    return timeFontSize;
  }

  /**
   * getAnnotation: returns the timepoint annotation
   */
  public String getAnnotation() {
    return annotation;
  }

  /**
   * setAnnotation: sets the annotation for the timepoint
   */
  protected void setAnnotation(String annot) {
    annotation = annot;
  }

  /**
   * setOverlap: sets whether the timepoint is an overlap
   */
  protected void setOverlap(Boolean bool) {
    isOverlap = bool;
  }

  /**
   * toElement: creates and returns an XML element representing the timepoint
   */
  public org.w3c.dom.Element toElement(org.w3c.dom.Document doc, int offset) throws Exception{
    // create Timepoint element
    org.w3c.dom.Element timepointElement = doc.createElement("Timepoint");
    // offset attribute
    timepointElement.setAttribute("offset", String.valueOf(offset));
    // label attribute
    if (label.length() > 0) {
      timepointElement.setAttribute("label", label);
    }
    // annotation attribute--note: timepoint annotations are not currently supported
    if (annotation.length() > 0) {
      org.w3c.dom.Element annotationElement = doc.createElement("Annotation");
      annotationElement.appendChild(doc.createTextNode(annotation));
      timepointElement.appendChild(annotationElement);
    }
    // is overlap element (new in 2022)
    if (isOverlap) {
        timepointElement.setAttribute("overlap", "1");    	
    } 
    
    return timepointElement;
  }
}