package ui.timeliner;

import javax.swing.JDialog;
import java.awt.*;
import javax.swing.*;
import java.io.*;
import java.awt.event.*;
import ui.common.*;
import com.borland.jbcl.layout.*;
import util.logging.*;
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
      dialogWidth = 700;
      dialogHeight = 100;
    } else {
      timelineFont = UIUtilities.fontDialogWin;
      dialogWidth = 700;
      dialogHeight = 100;
    }
    this.setTitle("Missing Audio");
    this.setLocationRelativeTo(parentWindow);
    this.setLocation((parentWindow.getWidth()/2) - (dialogWidth/2), 150);
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
        File missingFile = TimelineUtilities.findMissingAudio(parentWindow);
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
    JLabel lblMissing = new JLabel("The audio for this timeline could not be found at: " + mediaContent);
    lblMissing.setFont(timelineFont);

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
    pane.add(lblMissing);
    buttonPanel.add(btnContinue);
    buttonPanel.add(btnBrowse);
    //    buttonPanel.add(btnCancel);     // I'm not sure if this option is a good idea here
    pane.add(buttonPanel);

    // show dialog
    this.setVisible(true);
  }
}