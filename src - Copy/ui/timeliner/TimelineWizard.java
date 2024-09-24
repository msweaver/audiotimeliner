package ui.timeliner;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;

import ui.common.UIUtilities;
//import org.apache.log4j.Logger;
import util.logging.UIEventType;
import util.logging.UILogger;

/**
 * TimelineWizard: helps users create or open a timeline
 */

public class TimelineWizard extends JDialog {

	private static final long serialVersionUID = 1L;
// external components
  //private static TimelinePanel pnlTimeline;
  private static TimelineFrame frmTimeline;
  //private static Timeline timeline;
  //private static Logger log = Logger.getLogger(TimelinePrintDialog.class);
  protected static UILogger uilogger;

  // visual components
  private static JButton btnNewTimeline;
  private static JButton btnOpenTimeline;
  private static java.awt.Font timelineFont;

  public TimelineWizard(TimelineFrame tf)  {
    super(tf);
    frmTimeline = tf;
    //pnlTimeline = frmTimeline.getTimelinePanel();
    //timeline = pnlTimeline.getTimeline();
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
      dialogWidth = (int)(tf.getWidth() * .25); // 300;
      dialogHeight = (int)(tf.getHeight() * .25); // 130;
      this.setModal(true);
    }
    this.setTitle("How would you like to begin?");
    this.setLocationRelativeTo(frmTimeline);
    this.setLocation((frmTimeline.getWidth()/2) - (dialogWidth/2), 100);
    this.setSize(new Dimension(dialogWidth, dialogHeight));

    //  buttons
    btnNewTimeline = new JButton("Choose Audio for a New Timeline...");
    btnNewTimeline.setFont(timelineFont);
    btnOpenTimeline = new JButton("Open an Existing Timeline...");
    btnOpenTimeline.setFont(timelineFont);
//    btnNewTimeline.setMaximumSize(new Dimension(200, 150));

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
    JLabel optionLabel = new JLabel("    How would you like to start?");
    optionLabel.setFont(timelineFont);

    // panel
   // JPanel buttonPanel = new JPanel(new FlowLayout());

    // window close handler
    this.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent ev) {
      }
    });

    // layout
    Container pane = this.getContentPane();
    pane.setLayout(new GridLayout(2,1, 3, 3));
    //pane.add(optionLabel);
    pane.add(btnNewTimeline);
    pane.add(btnOpenTimeline);

    // show dialog
//    this.pack();
    this.setVisible(true);
  }
}