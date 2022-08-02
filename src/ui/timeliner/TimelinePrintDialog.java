package ui.timeliner;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import ui.common.*;
import com.borland.jbcl.layout.*;
import util.logging.*;
import org.apache.log4j.Logger;

/**
 * TimelinePrintDialog.java
 */

public class TimelinePrintDialog extends JDialog {

  // external components
  private static TimelinePanel pnlTimeline;
  private static TimelineFrame frmTimeline;
  private static Timeline timeline;
  private static TimelineMenuBar menubTimeline;
  private static Logger log = Logger.getLogger(TimelinePrintDialog.class);
  protected static UILogger uilogger;

  // visual components
  private static JRadioButton radBW;
  private static JRadioButton radColor;
  private static java.awt.Font timelineFont;

  /**
   * constructor
   */
  public TimelinePrintDialog(TimelineFrame tf) {
    super(tf);
    frmTimeline = tf;
    pnlTimeline = frmTimeline.getTimelinePanel();
    timeline = pnlTimeline.getTimeline();
    uilogger = frmTimeline.getUILogger();

    // set up dialog
    int dialogWidth;
    int dialogHeight;
    if (System.getProperty("os.name").startsWith("Mac OS")) {
      timelineFont = UIUtilities.fontDialogMacSmallest;
      dialogWidth = 200;
      dialogHeight = 150;
    } else {
      timelineFont = UIUtilities.fontDialogWin;
      dialogWidth = 150;
      dialogHeight = 170;
    }
    this.setTitle("Print Options");
    this.setLocationRelativeTo(frmTimeline);
    this.setModal(true);
    this.setSize(new Dimension(dialogWidth, dialogHeight));

    // prints in black and white by default
    final boolean oldMode = timeline.getBlackAndWhite();
    timeline.setBlackAndWhite(true);

    // radio buttons
    radBW = new JRadioButton("Black and White", true);
    radBW.setFont(timelineFont);
    radColor = new JRadioButton("Color or Grayscale", false);
    radColor.setFont(timelineFont);
    ButtonGroup grpPrintOptions = new ButtonGroup();
    grpPrintOptions.add(radBW);
    grpPrintOptions.add(radColor);

    radBW.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        timeline.setBlackAndWhite(true);
        pnlTimeline.refreshTimeline();
        uilogger.log(UIEventType.RADIOBUTTON_PICKED, "print in black and white");

      }
    });

    radColor.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        timeline.setBlackAndWhite(false);
        pnlTimeline.refreshTimeline();
        uilogger.log(UIEventType.RADIOBUTTON_PICKED, "print in color");
      }
    });

    // labels
    JLabel printLabel = new JLabel("Print in ...");
    printLabel.setFont(timelineFont);

    // panel
    JPanel buttonPanel = new JPanel(new FlowLayout());

    // buttons
    JButton btnOk = new JButton("OK");
    btnOk.setFont(timelineFont);
    btnOk.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setVisible(false);
        pnlTimeline.repaint();
        pnlTimeline.doPrint(timeline.getBlackAndWhite());
        timeline.setBlackAndWhite(oldMode);
        uilogger.log(UIEventType.BUTTON_CLICKED, "accept print options");
      }
    });

    JButton btnCancel = new JButton("Cancel");
    btnCancel.setFont(timelineFont);
    btnCancel.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setVisible(false);
        timeline.setBlackAndWhite(oldMode);
        pnlTimeline.repaint();
        uilogger.log(UIEventType.BUTTON_CLICKED, "cancel print options");
      }
    });

    // window close handler
    this.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent ev) {
        timeline.setBlackAndWhite(oldMode);
      }
    });

    // layout
    Container pane = this.getContentPane();
    pane.setLayout(new VerticalFlowLayout());
    pane.add(printLabel);
    pane.add(radBW);
    pane.add(radColor);
    buttonPanel.add(btnOk);
    buttonPanel.add(btnCancel);
    pane.add(buttonPanel);

    // show dialog
    this.show();
  }
}