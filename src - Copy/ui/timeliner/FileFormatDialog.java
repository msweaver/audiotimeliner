package ui.timeliner;

import java.awt.Container;
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
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

import org.apache.log4j.Logger;

import com.borland.jbcl.layout.VerticalFlowLayout;

//import javazoom.jl.converter.Converter;
import media.AudioHandler;
import ui.common.BasicWindow;
import ui.common.UIUtilities;
import util.logging.UIEventType;
import util.logging.UILogger;

/**
 * MissingAudioDialog: comes up when the audio is missing
 */

public class FileFormatDialog extends JDialog {

  private static final long serialVersionUID = 1L;
  private static Logger logger = Logger.getLogger(AudioHandler.class); 
  // external components
  protected static UILogger uilogger;
  private static BasicWindow parentWindow;
  private static AudioHandler time;
  protected File newPath = null;

  // visual components
  private static JButton btnContinue;
  private static JButton btnConvert;
  private static JButton btnCancel;
  private static java.awt.Font timelineFont;

  public FileFormatDialog(String mediaContent, AudioHandler handler, BasicWindow parent)  {
    super(parent);
    uilogger = parent.getUILogger();
    parentWindow = parent;
    time = handler;

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
    File audioFile = new File(mediaContent);
    String filename= (audioFile).getName();
    this.setTitle("File Format Issue: " + filename);
    this.setLocationRelativeTo(parentWindow);
    
    this.setSize(dialogWidth, dialogHeight);
    this.setLocation((parentWindow.getWidth()/2) -(this.getWidth()/2), 150);
    this.setModal(true);

    //  buttons
    btnContinue = new JButton("Continue without converting");
    btnConvert = new JButton("Convert");
    btnCancel = new JButton("Cancel");
    btnContinue.setFont(timelineFont);
    btnConvert.setFont(timelineFont);
    btnCancel.setFont(timelineFont);
    //this.pack();
     
    btnContinue.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setVisible(false);
        uilogger.log(UIEventType.BUTTON_CLICKED, "continue without converting");
      }
    });
    btnConvert.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
    	  
    	  String fn = audioFile.getName();
    	  fn = fn.substring(0, fn.length()-4);
    	  fn = fn + ".wav";

    	File newFileName = TimelineUtilities.chooseWavLocation(parentWindow, fn);
    	  //logger.debug("new file location: " + newFileName);
	      logger.debug("converting " + audioFile.getName() + " to " + fn + " ...");

	setVisible(false);
	
	JDialog dialog = new JDialog(parentWindow, true); 
   	dialog.setUndecorated(true);
   	JProgressBar bar = new JProgressBar();
   	bar.setIndeterminate(true);
   	bar.setStringPainted(true);
   	bar.setString("Converting audio ... ");
   	dialog.add(bar);
   	dialog.pack();
   	dialog.setLocation((parentWindow.getWidth() / 2)-(dialog.getWidth()/2), 200);
   	
   	SwingWorker<Void,Void> worker = new SwingWorker<Void,Void>()
	{
	    protected Void doInBackground()
	    {
    	
	       	File newWavFile = TimelineUtilities.createWavFile(parentWindow, audioFile.toString(), newFileName.toString());  
	        time.newPath = newWavFile;

	        return null;
	    }
	 
	    protected void done()
	    {
	        dialog.dispose();
	    }
	};
	worker.execute();
	dialog.setVisible(true);
	
        ((TimelineFrame)parent).isNewAudio = true;
         
        uilogger.log(UIEventType.BUTTON_CLICKED, "convert audio");
      }
    });
    
    btnCancel.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setVisible(false);
        uilogger.log(UIEventType.BUTTON_CLICKED, "cancel load audio");
      }
    });

    // labels
    JLabel lblConvert = new JLabel("<html>This MP3 file has a non-standard format and may not work correctly. Would you like to convert it to a WAV file?");
    lblConvert.setFont(timelineFont);

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
    textPanel.add(lblConvert);
    buttonPanel.add(btnConvert);
    buttonPanel.add(btnContinue);
    //buttonPanel.add(btnCancel); 
    pane.add(textPanel);
    pane.add(buttonPanel);

    // show dialog
    this.pack();
    this.setVisible(true);
  }
}