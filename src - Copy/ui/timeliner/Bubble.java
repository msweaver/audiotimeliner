package ui.timeliner;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Arc2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JComponent;

import ui.common.UIUtilities;

/**
 * Bubble class
 * This class represents a bubble object. A bubble can be drawn, selected, and
 * have its color changed. Bubbles are created in connection with a timeline object.
 * Bubbles may be drawn in various ways: rounded or square or drawn in outline form.
 *
 * @author Brent Yorgason
 */

public class Bubble extends JComponent {

  private static final long serialVersionUID = 1L;
// private variables
  private Color color = new Color(166, 202, 240); // default color is light blue
  private Color bwColor = new Color (255, 255, 255); // white
  private Color shadeColor;
  //private Graphics g;
  private Graphics2D g2d;
  private int arcTop;
  private int arcHeight;
  //private int squareTop;
  //private int squareHeight;
  private int selectStrokeWidth = 2;
  private Arc2D.Double arc;
  private Rectangle2D.Double square;
  //private Point labelPoint;
  private int labelFontSize;
  private String unicodeFont = "Arial Unicode MS";
  private boolean selected;
  private boolean blackAndWhite;

  // protected variables
  protected int start;
  protected int end;
  protected int base;
  protected int width;
  protected int height;
  protected int top;
  protected int type;
  protected int level = 1;
  protected String label = "";
  protected String annotation = "";
  protected boolean levelWasUserAdjusted = false;
  protected boolean hasCustomColor = false;

  /**
   * default constructor
   */
  public Bubble() {
    selected = false;
    blackAndWhite = false;
  }

  /**
   * alternate constructor that accepts a bubble as a parameter in order to copy its properties
   */
  public Bubble(Bubble bubble) {
    color = bubble.getColor();
    height = bubble.getHeight();
    type = bubble.getType();
    selected = false;
    blackAndWhite = false;
  }

  /**
   * getBubble: returns a copy of the bubble object
   */
  public Bubble getBubble() {
    return this;
  }

  /**
   * contains: returns true if the bubble contains the given point
   */
  public boolean contains(Point p) {
    if (type == 0) { // round bubbles
      if (arc != null && arc.contains((Point2D)p)) {
        return true;
      }
    }
    else if (type == 1) { // square bubbles
      if (square != null && square.contains((Point2D)p)) {
        return true;
      }
    }
    return false;
  }

  /**
   * intersects: returns true if the bubble intersects with the given rect
   */
  protected boolean intersects(Rectangle rect) {
    double x = rect.getX();
    double y = rect.getY();
    double w = rect.getWidth();
    double h = rect.getHeight();

    if (type == 0) { // round bubbles
      if (arc.intersects(x, y, w, h)) {
        return true;
      }
      else if (x < arc.x && y < arcTop && (x + w) > (arc.x + arc.width) && (y + h) > base) { // arc is within rect
        return true;
      }
    }
    else if (type == 1) { // square bubbles
      if (square.intersects(x, y, w, h)) {
        return true;
      }
    }
    return false;
  }

  /**
   * drawBubble: given the graphics, bubble start point, base, end point, and height,
   * draws a bubble on the timeline
   */
  protected void drawBubble(Graphics2D graphics, int bubStart, int bubBase, int bubEnd, int bubHeight, int bubType, boolean bw) {
    // load passed parameters
    start = bubStart;
    base = bubBase;
    end = bubEnd;
    width = bubEnd - bubStart;
    if (bubType == 0) { // formula for setting the height of round bubbles--dependent on user settings and level
      height = (int)((double)bubHeight + ((level - 1) * (double)(bubHeight * .7)));
    }
    else if (bubType == 1) { // formula for setting the height of square bubbles
      height = bubHeight + ((level - 1) * bubHeight);
    }
    top = base - height;
    g2d = graphics;
    g2d.setColor(color);
    arcHeight = height * 2;
    arcTop = base - height;
    type = bubType;
    blackAndWhite = bw;
    int sideAdjust = 2; // tweak value

    // set up the bubble label font size, using a formula dependent on the bubble height
    labelFontSize = UIUtilities.convertFontSize(12 - ((UIUtilities.scalePixels(30) - bubHeight) / 4));

    // impose a limit of font size 14 in level 1 and 18 in level 2
    if (level == 1 && labelFontSize > 14) {
      labelFontSize = UIUtilities.convertFontSize(14);
    }
    else if (level == 2 && labelFontSize > 18) {
      //labelFontSize = UIUtilities.convertFontSize(18);
    }

    // set up label font and calculate label size and position
    Font labelFont = new Font(unicodeFont, 0, labelFontSize);
    g2d.setFont(labelFont);
    FontMetrics labelMetrics = g2d.getFontMetrics();
    int labelWidth = labelMetrics.stringWidth(label);
    int labelHeight = labelMetrics.getHeight();
    int labelX = start + (width / 2) - (labelWidth / 2);
    int labelY = base - height + labelHeight + (bubHeight / 10);

    // create shapes
    arc = new Arc2D.Double(start + 1, top, width - 2, arcHeight, 0, 180, 1);
    square = new Rectangle2D.Double(start + sideAdjust, top, width - (sideAdjust * 2), height);

    // set up shading color
    if (selected) {
      if (!blackAndWhite) {
        if (color.getRed() + color.getGreen() + color.getBlue() > 250) { // darken
          shadeColor = color.darker();
        }
        else {
          shadeColor = color.brighter();
        }
      }
      else {
        shadeColor = Color.lightGray;
      }
    }

    // now draw the bubble
    if (type == 0) {  // round bubbles
      if (selected) { // this bubble is currently selected
        // shade the bubble
        g2d.setColor(shadeColor);
        g2d.fill(arc);

        // draw the outline selection indicator
        g2d.setColor(Color.black);
        g2d.setStroke(new BasicStroke(selectStrokeWidth));
        if (level==1) {
          g2d.drawArc(start, top, width - 1, arcHeight, 0, 180);
        }
        else {
          g2d.drawArc(start, top, width, arcHeight, 0, 180);
        }
        g2d.setStroke(new BasicStroke(1));
      }
      else { // the bubble is not selected
        if (blackAndWhite) { // just draw an outline
          g2d.setColor(Color.black);
          g2d.setStroke(new BasicStroke(1));
          g2d.drawArc(start, top - 1, width, arcHeight + 1, 0, 180);
          g2d.setColor(bwColor);
          g2d.fill(arc);
        }
        else {
          g2d.setColor(color);
          g2d.fill(arc);
        }
      }
    }
    else if (type == 1) { // square bubbles
      if (selected) { // this bubble is currently selected
        // shade the bubble
        g2d.setColor(shadeColor);
        g2d.fill(square);

        // draw the outline selection indicator
        g2d.setColor(Color.black);
        g2d.setStroke(new BasicStroke(selectStrokeWidth));
        g2d.drawRect(start + sideAdjust - 2, top + 1, width - (sideAdjust * 2) + 3, height);
        g2d.setStroke(new BasicStroke(1));

      }
      else { // the bubble is not selected
        if (blackAndWhite) { // just draw an outline
          g2d.setColor(Color.black);
          g2d.setStroke(new BasicStroke(1));
          g2d.drawRect(start + sideAdjust - 1, top - 1, width - (sideAdjust * 2) + 1, height + 1);
          g2d.setColor(bwColor);
          g2d.fill(square);
        }
        else {
          g2d.setColor(color);
          g2d.fill(square);
        }
      }
    }

    // draw the label
    g2d.setColor(Color.black);
    if (this.contains(new Point(labelX - 3, labelY - 3)) && labelY > 0 && labelX > 0 && label.length() > 0) { // WAS 1 instead of 10
      g2d.drawString(label, labelX, labelY);
    }
    else { // resize the label if not all of it will fit in the displayed bubble
//      for (int displayLength = label.length() - 1; displayLength > 0; displayLength--) {
    for (int fontsize = labelFontSize - 1; fontsize > 0; fontsize--) {
    	int displayLength = label.length(); // NEW
        Font smallerfont = new Font(unicodeFont, 0, fontsize); // NEW
        g2d.setFont(smallerfont); // NEW
        labelMetrics = g2d.getFontMetrics(); // NEW
        String displayLabel = label.substring(0, displayLength);
        labelWidth = labelMetrics.stringWidth(displayLabel);
        labelX = start + (width / 2) - (labelWidth / 2);
        labelY = base - height + labelHeight + (bubHeight / 10);

        if (this.contains(new Point(labelX - 3, labelY - 3)) && labelY > 0 && labelX > 0 && displayLabel.length() > 0) { // WAS 1 instead of 10
          g2d.drawString(displayLabel, labelX, labelY);
          break;
        }
      }
    }
  }

  /**
   * select: marks the bubble as selected
   */
  protected void select() {
    selected = true;
  }

  /**
   * deselect: marks the bubble as not selected
   */
  protected void deselect() {
    selected = false;
  }

  /**
   * isSelected: returns true if the bubble is selected
   */
  public boolean isSelected() {
    return selected;
  }

  /**
   * toString: returns a little information about the bubble
   */
  public String toString() {
    if (start > 0) {
      return (new String("bubble start = " + (start - 50)) + " level = " + level);
    }
    else {
      return (new String("root bubble, level = " + level));
    }
  }

  /**
   * getAnnotation: returns the bubble annotation
   */
  public String getAnnotation() {
    return annotation;
  }

  /**
   * getArc: returns the arc object
   */
  protected Arc2D getArc() {
    return arc;
  }

  /**
   * getBlackAndWhite: returns whether bubble is black and white
   */
  public boolean getBlackAndWhite() {
    return blackAndWhite;
  }

  /**
   * getColor: returns bubble color
   */
  public Color getColor() {
    return color;
  }

  /**
   * getEnd: returns bubble's end point (pixels)
   */
  public int getEnd() {
    return end;
  }

  /**
   * getFontSize: gets the font size for the bubble label
   */
  protected int getFontSize() {
    return labelFontSize;
  }

  /**
   * getHeight: returns bubble height in pixels
   */
  public int getHeight() {
    return height;
  }

  /**
   * getLabel: returns bubble label
   */
  public String getLabel() {
    return label;
  }

  /**
   * getLevel: returns level of bubble
   */
  public int getLevel() {
    return level;
  }

  /**
   * getStart: returns bubble start point (in pixels)
   */
  public int getStart() {
    return start;
  }

  /**
   * getType: returns bubble type (round or square)
   */
  public int getType() {
    return type;
  }

  /**
   * setAnnotation: sets the annotation for the bubble
   */
  protected void setAnnotation(String annot) {
    annotation = annot;
  }

  /**
   * setColor: sets bubble color
   */
  protected void setColor(Color c) {
    color = c;
  }

  /**
   * setEnd: sets bubble end point (in pixels)
   */
  protected void setEnd(int e) {
    end = e;
  }

  /**
   * setLabel: sets bubble label
   */
  protected void setLabel(String labelText) {
    label = labelText;
  }

  /**
   * setLevel: sets bubble level
   */
  protected void setLevel(int lev) {
    level = lev;
  }

  /**
   * setStart: sets bubble start point (in pixels)
   */
  protected void setStart(int s) {
    start = s;
  }

  /**
   * toElement: creates and returns an XML element representing the bubble
   */
  public org.w3c.dom.Element toElement(org.w3c.dom.Document doc, Timeline timeline) throws Exception{
    // bubble element
    org.w3c.dom.Element bubbleElement = doc.createElement("Bubble");
    // color attribute
//    if (hasCustomColor) { // all colors now saved, not just custom ones, in order to maintain compatibility with version 2.0
      bubbleElement.setAttribute("color", String.valueOf(color.getRed()) + ","
                               + String.valueOf(color.getGreen()) + ","
                               + String.valueOf(color.getBlue()));
//    }
    // level attribute
    bubbleElement.setAttribute("level", String.valueOf(this.level));
    // label attribute
    if (label.length() > 0) {
      bubbleElement.setAttribute("label", label);
    }
    // time attribute
    if (start == 0) {
      bubbleElement.setAttribute("time", "0:00.0");
    }
    else {
      int tpoint = timeline.getTimepointAt(new Point(start, base));
      String time = timeline.getTimepoint(tpoint).getTime();
      if (time == "0") {
        time = "0:00.0";
      }
      bubbleElement.setAttribute("time", time);
    }
    // levelSetByUser element
    if (this.levelWasUserAdjusted) {
      bubbleElement.setAttribute("levelSetByUser", "true");
    }
    // annotation element
    if (annotation.length() > 0) {
      org.w3c.dom.Element annotationElement = doc.createElement("Annotation");
      annotationElement.appendChild(doc.createTextNode(annotation));
      bubbleElement.appendChild(annotationElement);
    }
     return bubbleElement;
  }

  /**
   * toElementHTML: creates and returns an XML element representing the bubble in HTML display format
   */
  public org.w3c.dom.Element toElementHTML(org.w3c.dom.Document doc, Timeline timeline, int bubbleNum) throws Exception{
    // bubble element
    org.w3c.dom.Element bubbleElement = doc.createElement("Bubble");
    Timepoint t = null;
    // label attribute
    if (label.length() > 0) {
      bubbleElement.setAttribute("label", label);
    }
    // time attribute
    int tpoint = timeline.getTimepointAt(new Point(start, base));
    t = timeline.getTimepoint(tpoint);
    if (start == 0) {
      bubbleElement.setAttribute("time", null);
      bubbleElement.setAttribute("timelabel", null);
    }
    else {
      String time = t.getTime();
      if (time == "0") {
        time = "0:00.0";
      }
      bubbleElement.setAttribute("time", time);
      bubbleElement.setAttribute("timelabel", t.getLabel());
    }
    // annotation element
    if (annotation.length() > 0) {
    	String plainAnnotation = UIUtilities.removeTags(annotation);
      org.w3c.dom.Element annotationElement = doc.createElement("Annotation");
      annotationElement.appendChild(doc.createTextNode(plainAnnotation));
      bubbleElement.appendChild(annotationElement);
    }
    // now add any markers contained in the bubble, if it is a base bubble
    if ((timeline.getBubbleNode(bubbleNum).isLeaf())) {
      int beginOffset = timeline.getSortedPointList()[tpoint];
      int endOffset = timeline.getSortedPointList()[tpoint+1];
      for (int i = 0; i < timeline.getNumMarkers(); i++) {
        int markOffset = timeline.getMarkerList()[i];
        if (markOffset >= beginOffset && markOffset <= endOffset) {
          Marker m = timeline.getMarker(i);
          org.w3c.dom.Element markerAnnotationElement = doc.createElement("MarkerAnnotation");
          markerAnnotationElement.setAttribute("label", m.getLabel());
          markerAnnotationElement.setAttribute("time", m.getTime());
          if (m.getAnnotation().length() > 0) {
            org.w3c.dom.Element annElement = doc.createElement("Ann");
            annElement.appendChild(doc.createTextNode(UIUtilities.removeTags(m.getAnnotation())));
            markerAnnotationElement.appendChild(annElement);
          }
          bubbleElement.appendChild(markerAnnotationElement);
        }
      }
    }
     return bubbleElement;
  }
}
