package ui.timeliner;

import javax.swing.plaf.basic.BasicLookAndFeel;
import javax.swing.*;
import java.awt.*;
import javax.swing.plaf.*;

/**
 * Timeline Look and Feel
 * This alternate look and feel currently supports an alternate slider appearance only,
 * through the TimelineSliderUI class. TimelineLookAndFeel is thus not designed
 * to be applied globally, but to the slider component only.
 *
 * @author Brent Yorgason
 */

public class TimelineLookAndFeel extends BasicLookAndFeel {

  private static final long serialVersionUID = 1L;
// color information -- maybe more than we need
  //private static ColorUIResource primary1 = new ColorUIResource(102, 102, 153);
  private static ColorUIResource primary2 = new ColorUIResource(153, 153, 204);
  private static ColorUIResource primary3 = new ColorUIResource(204, 204, 255);
  private static ColorUIResource secondary1 = new ColorUIResource(102, 102, 102);
  private static ColorUIResource secondary2 = new ColorUIResource(153, 153, 153);
  private static ColorUIResource secondary3 = new ColorUIResource(204, 204, 204);
  private static ColorUIResource white = new ColorUIResource( 255, 255, 255 );
  private static ColorUIResource black = new ColorUIResource (0, 0, 0);

  // constructor
  public TimelineLookAndFeel() {
    super();
  }

  /**
   * initComponentDefaults: component defaults are currently coming from BasicLookAndFeel
   * this may need to be overriden with local defaults
   */
  protected void initComponentDefaults(UIDefaults table) {
    super.initComponentDefaults( table );
  }

  /**
   * getDefaults: returns defaults from BasicLookAndFeel
   * this may need to be overriden with local defaults
   */
  public UIDefaults getDefaults() {
    UIDefaults table = super.getDefaults();
    return table;
  }

  public boolean isSupportedLookAndFeel() {
    return true;
  }

  public boolean isNativeLookAndFeel() {
    return false;
  }
  public String getID() {
    return "TimelineLookAndFeel";
  }
  public String getName() {
    return "Timeline Look and Feel";
  }
  public String getDescription() {
    return "The Timeline Look and Feel";
  }
  protected void initClassDefaults(UIDefaults table) {
    super.initClassDefaults(table);
    table.put("SliderUI", "TimelineSliderUI");
 }

  public static ColorUIResource getControlShadow() { return secondary2; }
  public static ColorUIResource getControlDarkShadow() { return secondary1; }
  public static ColorUIResource getControlHighlight() { return white; }
  public static ColorUIResource getPrimaryControlShadow() { return primary2; }
  public static ColorUIResource getFocusColor() { return primary2; }
  public static ColorUIResource getControl() { return secondary3; }
  public static ColorUIResource getPrimaryControlInfo() { return black; }
  public static ColorUIResource getPrimaryControl() { return primary3; }

  static boolean isLeftToRight( Component c ) {
    return c.getComponentOrientation().isLeftToRight();
  }
}
