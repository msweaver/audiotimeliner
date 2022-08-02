package ui.timeliner;

import javax.swing.*;
import java.awt.*;
import com.borland.jbcl.layout.*;
import ui.common.*;

/**
 * Timeline Resizer
 * This class represents a timeline resizer object, which permits the user to
 * drag-resize the timeline.
 *
 * @author Brent Yorgason
 */

public class TimelineResizer {

  // private variables
  private Color color = new Color(0,0,0);
  private int width = 4;
  private int height = 13;
  private int xLoc;
  private int yLoc;
  private Graphics2D g2d;
  private Rectangle resizerRect = new Rectangle();
  private TimelinePanel pnlTimeline;
  private boolean enabled = true;

  // other variables
  protected JLabel lblResizer;
  final static ImageIcon icoResizer = UIUtilities.icoResizer;

  /**
   * constructor
   */
  public TimelineResizer(TimelinePanel tPanel) {
    pnlTimeline = tPanel;
    lblResizer = new JLabel(icoResizer);
    if (enabled) {
      width = icoResizer.getIconWidth();
      height = icoResizer.getIconHeight();
      pnlTimeline.add(lblResizer, new XYConstraints(xLoc, yLoc - (height / 2), width, height));
      pnlTimeline.validate();
    }
    resizerRect.setBounds(xLoc, yLoc, width, height);
  }

  /**
   * move: moves the resizer icon to the given coordinates when enabled
   * and draws a immovable timepoint when disabled
   */
  public void move(int x, int y, Graphics2D g2d) {
    xLoc = x - 1;
    yLoc = y - (height / 2) + 1;

    if (enabled) {
      lblResizer.setVisible(true);
      if (!lblResizer.getLocation().equals(new Point (xLoc, yLoc))) {
        lblResizer.setVisible(true);
        pnlTimeline.add(lblResizer, new XYConstraints(xLoc, yLoc, width, height));
        pnlTimeline.validate();
      }
    }
    else if (enabled == false) {
      lblResizer.setVisible(false);
      g2d.setColor(color);
      g2d.fillRect(xLoc, yLoc, width, height);
    }
    resizerRect.setBounds(xLoc, yLoc, width, height);
  }

  /**
   * contains: returns true if the resizer contains the passed point
   */
  protected boolean contains(Point p) {
    if (resizerRect.contains(p)) {
      return true;
    }
    return false;
  }

  /**
   * enable: sets whether the resizer is enabled or disabled
   */
  protected void setEnabled(boolean state) {
    enabled = state;
    if (enabled) {
      width = icoResizer.getIconWidth();
      height = icoResizer.getIconHeight();
    }
    else {
      width = 4;
      height = 13;
    }
    resizerRect.setBounds(xLoc, yLoc, width, height);
  }

}