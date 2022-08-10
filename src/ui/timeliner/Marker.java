package ui.timeliner;

import java.awt.*;
//import java.lang.System.Logger;
//import org.apache.log4j.Logger;

import ui.common.UIUtilities;

/**
 * Marker class
 * This class represents a Marker object. Markers are created in connection with
 * timeline objects, on which they are drawn.
 *
 * @author Brent Yorgason
 */

 public class Marker {

  // private variables
  private Color color = new Color(0,0,0);
  private int width = UIUtilities.scalePixels(8);
  private int height = UIUtilities.scalePixels(8);
  private int markerX;
  private int markerY;
  private String label = "";
  private String annotation = "";
  private String time = "";

  public Rectangle markerRect = new Rectangle();
  private boolean selected;
  private boolean visibleTime;
  private int timeWidth = 0;
  private int timeStart = 0;
  private int timeFontSize = UIUtilities.convertFontSize(11);
  private boolean visibleLabel = true;
  private int labelWidth = 0;
  private int labelHeight = 0;
  private int labelStart = 0;
  private int labelFontSize = UIUtilities.convertFontSize(12);
  private String unicodeFont = "Arial Unicode MS";
  //private Graphics g;
  private Graphics2D g2d;
  private Polygon markerPolygon;
  //private static Logger log = Logger.getLogger(Marker.class);

  // constructor
  public Marker() {
    selected = false;
    visibleTime = true;
  }

  /**
   * getMarker: returns a copy of the Marker
   */
  protected Marker getMarker() {
   return this;
  }

  /**
   * contains: returns true if the point passed is contained within the Marker
   */
  public boolean contains(Point p) {
    return markerRect.contains(p);
  }

  /**
   * drawMarker: given a point and the current graphics, draws a Marker
   */
  protected void drawMarker(Graphics2D graphics, int xLoc, int yLoc, Color background) {
    g2d = graphics;

    // set up marker location
    markerX = xLoc - (width / 2);
    markerY = yLoc + 3;

    // set up marker label
    Font labelFont = new Font(unicodeFont, 0, labelFontSize);
    g2d.setFont(labelFont);
    FontMetrics labelMetrics = g2d.getFontMetrics();
    labelWidth = labelMetrics.stringWidth(label);
    labelHeight = labelMetrics.getHeight();
    int labelX = xLoc - (labelWidth / 2);
    int labelY = yLoc + 34;
    labelStart = labelX;

    // set up marker time
    Font timeFont = new Font(unicodeFont, 0, timeFontSize);
    g2d.setFont(timeFont);
    FontMetrics timeMetrics = g2d.getFontMetrics();
    timeWidth = timeMetrics.stringWidth(time);
    int timeX = xLoc - (timeWidth / 2);
    int timeY = yLoc + 48;
    timeStart = timeX;

    // marker boundaries
    int[] xPoints = {markerX, markerX + (width / 2), markerX + width};
    int[] yPoints = {markerY + height, markerY, markerY + height};
    int numPoints = 3;
    markerPolygon = new Polygon(xPoints, yPoints, numPoints);

    // draw the marker label
    if (visibleLabel && label.length() > 0) {
      // draw an "opaque" background for the label
      g2d.setColor(background);
      g2d.fillRect(labelX-1, labelY-labelHeight, labelWidth+2, labelHeight);
      // draw the label
      g2d.setColor(Color.black);
      g2d.setFont(labelFont);
      g2d.drawString(label, labelX, labelY);
    }

   // now draw the Marker
    if (!selected) {
      g2d.setColor(color);
      g2d.fill(markerPolygon);
    }
    else {
      g2d.setColor(Color.white);
      g2d.fill(markerPolygon);
      g2d.setColor(color);
      g2d.draw(markerPolygon);
    }

    // draw the marker time
    if (visibleTime && time.length() > 0) {
      g2d.setColor(Color.black);
      g2d.setFont(timeFont);
      g2d.drawString(time, timeX, timeY);
    }

    // set marker bounds
    markerRect.setBounds(markerX, markerY, width, height);
  }

  /**
   * select: marks the Marker as selected
   */
  protected void select() {
    selected = true;
   }

  /**
   * deselect: marks the Marker as deselected
   */
  protected void deselect() {
    selected = false;
  }

  /**
   * isSelected: returns true if the Marker is selected
   */
  public boolean isSelected() {
    return selected;
  }

  /**
   * getLabel: returns the Marker label
   */
  public String getLabel() {
    return label;
  }

  /**
   * getMarkerX: returns the x position of the Marker
   */
  protected int getMarkerX() {
    return markerX;
  }

  /**
   * getTime: returns the text string representing the Marker time
   */
  protected String getTime() {
    return time;
  }

  /**
   * setLabel: sets the label text for the Marker
   */
  protected void setLabel(String labelText) {
    label = labelText;
  }

  /**
   * setTime: sets the displayed time for the Marker
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
   * getLabelWidth: returns the width of the marker label
   */
  protected int getLabelWidth() {
    return labelWidth;
  }

  /**
   * getLabelStart: returns the start point (in pixels) of the marker label
   */
  protected int getLabelStart() {
    return labelStart;
  }

  /**
   * setLabelFontSize: returns the font size of the marker label
   */
  protected void setLabelFontSize(int size) {
    labelFontSize = size;
  }

  /**
   * getTimeWidth: returns the width of the marker time
   */
  protected int getTimeWidth() {
    return timeWidth;
  }

  /**
   * getTimeStart: returns the start point (in pixels) of the marker time
   */
  protected int getTimeStart() {
    return timeStart;
  }

  /**
   * setTimeFontSize: returns the font size of the marker time
   */
  protected void setTimeFontSize(int size) {
    timeFontSize = size;
  }

  /**
   * getAnnotation: returns the Marker annotation
   */
  public String getAnnotation() {
    return annotation;
  }

  /**
   * setAnnotation: sets the annotation for the Marker
   */
  protected void setAnnotation(String annot) {
    annotation = annot;
  }

  /**
   * toElement: creates and returns an XML element representing the marker
   */
  public org.w3c.dom.Element toElement(org.w3c.dom.Document doc, int offset) throws Exception{
    // Marker element
    org.w3c.dom.Element MarkerElement = doc.createElement("Marker");
    MarkerElement.setAttribute("offset", String.valueOf(offset));
    MarkerElement.setAttribute("time", String.valueOf(time));
    // label attribute
    if (label.length() > 0) {
      MarkerElement.setAttribute("label", label);
    }
    // annotation attribute
    if (annotation.length() > 0) {
      org.w3c.dom.Element annotationElement = doc.createElement("Annotation");
      annotationElement.appendChild(doc.createTextNode(annotation));
      MarkerElement.appendChild(annotationElement);
    }
    return MarkerElement;
  }
  
  /**
   * toElementHTML: creates and returns an XML element representing the marker for HTML format
   */
  public org.w3c.dom.Element toElementHTML(org.w3c.dom.Document doc, int offset) throws Exception{
    // Marker element
    org.w3c.dom.Element MarkerElement = doc.createElement("Marker");
    MarkerElement.setAttribute("offset", String.valueOf(offset));
    MarkerElement.setAttribute("time", String.valueOf(time));
    // label attribute
    if (label.length() > 0) {
      MarkerElement.setAttribute("label", label);
    }
    // annotation attribute
    if (annotation.length() > 0) {
    	String plainAnnotation = UIUtilities.removeTags(annotation);
    	//log.debug(plainAnnotation);
    	org.w3c.dom.Element annotationElement = doc.createElement("Annotation");
      annotationElement.appendChild(doc.createTextNode(plainAnnotation));
      MarkerElement.appendChild(annotationElement);
    }
    return MarkerElement;
  }
 }