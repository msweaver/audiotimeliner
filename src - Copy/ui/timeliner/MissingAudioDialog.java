package ui.timeliner;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.borland.jbcl.layout.VerticalFlowLayout;

import ui.common.BasicWindow;
import ui.common.UIUtilities;
import util.logging.UIEventType;
import util.logging.UILogger;

//import org.apache.log4j.Logger;

/**
 * MissingAudioDialog: comes up when the audio is missing
 */

public class MissingAudioDialog extends JDialog {

  private static final long serialVersionUID = 1L;
// external components
  //private static Logger log = Logger.getLogger(TimelinePrintDialog.class);
  protected static UILogger uilogger;
  private static BasicWindow parentWindow;
  private static TimelineXMLAdapter time;
  protected File newPath = null;

  // visual components
  private static JButton btnContinue;
  private static JButton btnBrowse;
  private static JButton btnCancel;
  private static java.awt.Font timelineFont;

  public MissingAudioDialog(String mediaContent, TimelineXMLAdapter xml, BasicWindow parent)  {
    super(parent);
    uilogger = parent.getUILogger();
    parentWindow = parent;
    time = xml;

    // set up dialog
    int dialogWidth;
    int dialogHeight;
    if (System.getProperty("os.name").startsWith("Mac OS")) {
      timelineFont = UIUtilities.fontDialogMacSmallest;
      dialogWidth = 600;
      dialogHeight = 100;
    } else {
      timelineFont = UIUtilities.fontDialogWin;
      dialogWidth = 600;
      dialogHeight = 100;
    }
    String filename= new String(new File(mediaContent).getName());
    this.setTitle("Missing Audio: " + filename);
    this.setLocationRelativeTo(parentWindow);
    this.setLocation(50, 100); // (parentWindow.getWidth()/2) - (dialogWidth/2), 150);
    this.setModal(true);
    this.setSize(new Dimension(dialogWidth, dialogHeight));

    //  buttons
    btnContinue = new JButton("Continue without audio");
    btnBrowse = new JButton("Browse for missing audio");
    btnCancel = new JButton("Cancel");
    btnContinue.setFont(timelineFont);
    btnBrowse.setFont(timelineFont);
    btnCancel.setFont(timelineFont);

    btnContinue.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setVisible(false);
        uilogger.log(UIEventType.BUTTON_CLICKED, "continue without audio");
      }
    });
    btnBrowse.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        File missingFile = TimelineUtilities.findMissingAudio(parentWindow, filename);
        time.newPath = missingFile;
        setVisible(false);
        uilogger.log(UIEventType.BUTTON_CLICKED, "browse for missing audio");
      }
    });
    btnCancel.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setVisible(false);
        uilogger.log(UIEventType.BUTTON_CLICKED, "cancel find audio for timeline");
      }
    });

    // labels
    JLabel lblMissing = new JLabel("<html>Oops! The audio for this timeline could not be found at: <br/><br/>" + mediaContent + "<br/><br/>What would you like to do?");
    lblMissing.setFont(timelineFont);

    // panel
    JPanel textPanel = new JPanel(new FlowLayout());
    JPanel buttonPanel = new JPanel(new FlowLayout());
    EmptyBorder border = new EmptyBorder(5, 5, 5, 5);
    textPanel.setBorder(border);

    // window close handler
    this.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent ev) {
      }
    });

    // layout
    Container pane = this.getContentPane();
    pane.setLayout(new VerticalFlowLayout());
    textPanel.add(lblMissing);
    buttonPanel.add(btnBrowse);
    buttonPanel.add(btnContinue);
    //    buttonPanel.add(btnCancel);     // I'm not sure if this option is a good idea here
    pane.add(textPanel);
    pane.add(buttonPanel);

    // show dialog
    this.pack();
    this.setVisible(true);
  }
}