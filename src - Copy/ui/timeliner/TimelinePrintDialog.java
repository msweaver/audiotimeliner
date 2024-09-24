package ui.timeliner;

import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.EmptyBorder;
//import org.apache.log4j.Logger;

import com.borland.jbcl.layout.VerticalFlowLayout;

import ui.common.UIUtilities;
import util.logging.UIEventType;
import util.logging.UILogger;

/**
 * TimelinePrintDialog.java
 */

public class TimelinePrintDialog extends JDialog {

  private static final long serialVersionUID = 1L;
// external components
  private static TimelinePanel pnlTimeline;
  private static TimelineFrame frmTimeline;
  private static Timeline timeline;
  //private static TimelineMenuBar menubTimeline;
  //private static Logger log = Logger.getLogger(TimelinePrintDialog.class);
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
    //int dialogWidth;
    //int dialogHeight;
    if (System.getProperty("os.name").startsWith("Mac OS")) {
      timelineFont = UIUtilities.fontDialogMacSmallest;
      //dialogWidth = UIUtilities.scalePixels(200);
      //dialogHeight = UIUtilities.scalePixels(200);
    } else {
      timelineFont = UIUtilities.fontDialogWin;
      //dialogWidth = UIUtilities.scalePixels(250);
      //dialogHeight = UIUtilities.scalePixels(200);
    }
    this.setTitle("Print Options");
    this.setLocationRelativeTo(frmTimeline);
    this.setModal(true);
//    this.setSize(new Dimension(dialogWidth, dialogHeight));
    EmptyBorder border = new EmptyBorder(0, 5, 0, 5 );
    JPanel panel = new JPanel();
    panel.setBorder(border);

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
    panel.setLayout(new VerticalFlowLayout());
    panel.setBorder(border);
    panel.add(printLabel);
    panel.add(radBW);
    panel.add(radColor);
    buttonPanel.add(btnOk);
    buttonPanel.add(btnCancel);
    pane.add(panel);
    pane.add(buttonPanel);
   

    // show dialog
    this.pack();
    this.setVisible(true);

  }
}