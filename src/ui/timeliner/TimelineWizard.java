package ui.timeliner;

import javax.swing.JDialog;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import ui.common.*;
import com.borland.jbcl.layout.*;
import util.logging.*;
import org.apache.log4j.Logger;

/**
 * TimelineWizard: helps users create or open a timeline
 */

public class TimelineWizard extends JDialog {

  // external components
  private static TimelinePanel pnlTimeline;
  private static TimelineFrame frmTimeline;
  private static Timeline timeline;
  private static Logger log = Logger.getLogger(TimelinePrintDialog.class);
  protected static UILogger uilogger;

  // visual components
  private static JButton btnNewTimeline;
  private static JButton btnOpenTimeline;
  private static java.awt.Font timelineFont;

  public TimelineWizard(TimelineFrame tf)  {
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
      dialogWidth = 400;
      dialogHeight = 120;
    } else {
      timelineFont = UIUtilities.fontDialogWin;
      dialogWidth = 300;
      dialogHeight = 130;
      this.setModal(true);
    }
    this.setTitle("Timeline Wizard");
    this.setLocationRelativeTo(frmTimeline);
    this.setLocation((frmTimeline.getWidth()/2) - (dialogWidth/2), 150);
    this.setSize(new Dimension(dialogWidth, dialogHeight));

    //  buttons
    btnNewTimeline = new JButton("Choose Audio for a New Timeline...");
    btnNewTimeline.setFont(timelineFont);
    btnOpenTimeline = new JButton("Open an Existing Timeline...");
    btnOpenTimeline.setFont(timelineFont);

    btnNewTimeline.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setVisible(false);
        TimelineUtilities.openAudioFile(frmTimeline);
        uilogger.log(UIEventType.BUTTON_CLICKED, "create a new timeline");
      }
    });

    btnOpenTimeline.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setVisible(false);
        TimelineUtilities.openTimeline(frmTimeline);
        uilogger.log(UIEventType.BUTTON_CLICKED, "open an existing timeline");
      }
    });

    // labels
    JLabel optionLabel = new JLabel("What would you like to do?");
    optionLabel.setFont(timelineFont);

    // panel
    JPanel buttonPanel = new JPanel(new FlowLayout());

    // window close handler
    this.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent ev) {
      }
    });

    // layout
    Container pane = this.getContentPane();
    pane.setLayout(new VerticalFlowLayout());
    pane.add(optionLabel);
    pane.add(btnNewTimeline);
    pane.add(btnOpenTimeline);

    // show dialog
    this.show();
  }
}