package ui.timeliner;

import java.awt.*;
import javax.swing.*;
import java.awt.image.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.BufferedOutputStream;
import client.*;
import javazoom.jl.converter.Converter;
import ui.common.*;
import util.logging.*;
import org.apache.log4j.Logger;
import javazoom.jl.converter.*;

/**
 * TimelineUtilities
 * Utilities that allow saving and opening timelines
 */

public class TimelineUtilities {

  private static JFileChooser fileChooserSave = new JFileChooser();
  private static JFileChooser fileChooserOpen = new JFileChooser();
  private static JFileChooser fileChooserOpenAudio = new JFileChooser();
  private static JFileChooser fileChooserSaveAudio = new JFileChooser();
   private static HTMLFilter HTMLfilt = new HTMLFilter();
  private static Logger logger = Logger.getLogger(TimelineUtilities.class);
  protected static UILogger uilogger;
  private static TimelineMenuBar menubTimeline;
  private static String[] timelineString = {"v2t", "tim"};
  private static TimelineFilter filter = new TimelineFilter(timelineString, "Timeline files");
  private static TimelineFilter saveFilter = new TimelineFilter("tim", "Timeline files");
  private static String[] audioString = {"mp3", "wav", "m4a"};
  private static String[] wavString = {"wav"};
  private static AudioFilter audioFilt = new AudioFilter(audioString, "Audio files");
  private static AudioFilter wavFilt = new AudioFilter(wavString, "WAV files");

  /**
   * empty constructor
   */
  public TimelineUtilities() {
  }

  /**
   * saves the given timeline
   * receives a save as flag and a timeline frame parent
   */
  public static void saveTimeline(Timeline timeline, boolean saveAs, TimelineFrame parentFrame) {
    try {
      uilogger = parentFrame.getUILogger();
      menubTimeline = parentFrame.getTimelinePanel().getMenuBar();

      // set up dialog and file filter
      fileChooserSave.setDialogTitle("Save Timeline As ...");
      fileChooserSave.setAcceptAllFileFilterUsed(false);
      fileChooserSave.removeChoosableFileFilter(HTMLfilt);
      fileChooserSave.addChoosableFileFilter(saveFilter);
      fileChooserSave.setMultiSelectionEnabled(false);

      // set up filenames
      String currFilename = timeline.currFilename;
      String oldFilename = currFilename;

      // no name has been chosen yet or save as explicity chosen, open the save as dialog
      if (currFilename == null || saveAs) {
        if (System.getProperty("os.name").startsWith("Mac OS")) { // needed to fix a 1.3 key bug
          menubTimeline.disableMenuKeyboardShortcuts();
          fileChooserSave.setSize(new Dimension(fileChooserSave.getWidth() + 100, fileChooserSave.getHeight()));
        }
        if (JFileChooser.APPROVE_OPTION == (fileChooserSave.showSaveDialog(parentFrame))) {
          currFilename = fileChooserSave.getSelectedFile().getPath();

          // add the file type if not already in the filename
          if (!currFilename.toLowerCase().endsWith(".tim")) {
              currFilename = currFilename  + ".tim";
              uilogger.log(UIEventType.BUTTON_CLICKED, "accept save timeline as .tim");
            }
          if (currFilename.toLowerCase().endsWith(".v2t")) {
            currFilename = currFilename.replace("v2t", "tim");
            uilogger.log(UIEventType.BUTTON_CLICKED, "accept save timeline as .tim");
          }
          if (System.getProperty("os.name").startsWith("Mac OS")) { // needed to fix a 1.3 key bug
            menubTimeline.enableMenuKeyboardShortcuts();
          }
        }
        else {
          uilogger.log(UIEventType.BUTTON_CLICKED, "cancel save timeline as");
          if (System.getProperty("os.name").startsWith("Mac OS")) { // needed to fix a 1.3 key bug
            menubTimeline.enableMenuKeyboardShortcuts();
          }
          return;
        }
      }

      // remove filter
      fileChooserSave.removeChoosableFileFilter(filter);

      // create file
      java.io.File selectedFile = new java.io.File(currFilename);

      try {
        // warn if file exists
        if (currFilename != oldFilename) {
          if (selectedFile.exists()) {
            int response = JOptionPane.showConfirmDialog(parentFrame, "File " + selectedFile.getName() + " already exists. Overwrite?",
                "Overwrite file?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (response == JOptionPane.NO_OPTION) {
              timeline.currFilename = oldFilename;
              uilogger.log(UIEventType.BUTTON_CLICKED, "cancel overwrite file");
              return;
            }
            else {
              timeline.currFilename = currFilename;
              uilogger.log(UIEventType.BUTTON_CLICKED, "confirm overwrite file");
            }
          } else {
            // create new file
            if (!(selectedFile.createNewFile())) {
              JOptionPane.showMessageDialog(parentFrame, "Error saving timeline.", "Save error",
                  JOptionPane.ERROR_MESSAGE);
              return;
            }
          }
        }
        if (currFilename != null && currFilename != ".tim" && currFilename != ".v2t") {
          // do XML export
          TimelineXMLAdapter tim = new TimelineXMLAdapter();
          if (currFilename.endsWith(".tim") || currFilename.endsWith(".v2t")) {
        	tim.pnlTimeline = parentFrame.getTimelinePanel();
            tim.saveTimelineXML(selectedFile.getPath(), timeline, Client.getUserName());
            timeline.makeClean();
            menubTimeline.menuiRevertToSaved.setEnabled(true);
          }
        }

      } catch (Exception err) {
        JOptionPane.showMessageDialog(parentFrame, "Error saving timeline.", "Save error",
                                      JOptionPane.ERROR_MESSAGE);
        err.printStackTrace();
        return;
      }
    }
    catch (Exception e)
    {
      JOptionPane.showMessageDialog(parentFrame, "Error saving timeline.", "Save error",
                                    JOptionPane.ERROR_MESSAGE);
      e.printStackTrace();
      return;
    }
  }

  /**
   * saves the given timeline as a web page
   * recieves a timeline frame parent
   */
  public static void saveTimelineAsWebPage(Timeline timeline, TimelineFrame parentFrame) {
    try {
      uilogger = parentFrame.getUILogger();
      menubTimeline = parentFrame.getTimelinePanel().getMenuBar();

      // set up dialog and file filter
      fileChooserSave.setDialogTitle("Save Timeline As Web Page ...");
      fileChooserSave.setAcceptAllFileFilterUsed(false);
      fileChooserSave.removeChoosableFileFilter(saveFilter);
      fileChooserSave.addChoosableFileFilter(HTMLfilt);
      fileChooserSave.setMultiSelectionEnabled(false);

      // local file variables
      String currFilename;
      String gifPath = "";
      String gifFilename = "";

      // open the save as dialog
      if (System.getProperty("os.name").startsWith("Mac OS")) { // needed to fix a 1.3 key bug
        menubTimeline.disableMenuKeyboardShortcuts();
        fileChooserSave.setSize(new Dimension(fileChooserSave.getWidth() + 100, fileChooserSave.getHeight()));
      }
      if (JFileChooser.APPROVE_OPTION == (fileChooserSave.showSaveDialog(parentFrame))) {
        currFilename = fileChooserSave.getSelectedFile().getPath();

        if (fileChooserSave.getFileFilter() instanceof HTMLFilter) {
          // add the file type if not already in the filename
          if ((currFilename.toLowerCase().endsWith(".html"))) {
            gifPath = currFilename.substring(0, currFilename.length()-4) + "gif";
          }
          else if((currFilename.toLowerCase().endsWith(".htm"))) {
            gifPath = currFilename.substring(0, currFilename.length()-3) + "gif";
          }
          else {
            currFilename = currFilename + ".html";
            gifPath = currFilename.substring(0, currFilename.length()-4) + "gif";
          }
          int lastSlash = gifPath.lastIndexOf("\\");
          gifFilename = gifPath.substring(lastSlash + 1, gifPath.length());
          uilogger.log(UIEventType.BUTTON_CLICKED, "accept save timeline as web page");
        }
        if (System.getProperty("os.name").startsWith("Mac OS")) { // needed to fix a 1.3 key bug
          menubTimeline.enableMenuKeyboardShortcuts();
        }
      }
      else {
        uilogger.log(UIEventType.BUTTON_CLICKED, "cancel save timeline as web page");
        if (System.getProperty("os.name").startsWith("Mac OS")) { // needed to fix a 1.3 key bug
          menubTimeline.enableMenuKeyboardShortcuts();
        }
        return;
      }

      // remove file filter
      fileChooserSave.removeChoosableFileFilter(HTMLfilt);

      // create files
      java.io.File selectedFile = new java.io.File(currFilename);
      java.io.File selectedFileGIF = new java.io.File(gifPath);

      try {
        // warn if file exists
        if (selectedFile.exists()) {
          int response = JOptionPane.showConfirmDialog(parentFrame, "File " + selectedFile.getName() + " already exists. Overwrite?",
              "Overwrite file?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
          if (response == JOptionPane.NO_OPTION) {
            uilogger.log(UIEventType.BUTTON_CLICKED, "cancel overwrite file");
            return;
          }
          else {
            uilogger.log(UIEventType.BUTTON_CLICKED, "confirm overwrite file");
          }
        } else {
          // create new file
          if (!(selectedFile.createNewFile())) {
            JOptionPane.showMessageDialog(parentFrame, "Error saving timeline as web page.", "Save error",
                JOptionPane.ERROR_MESSAGE);
            return;
          }
        }
        if (currFilename != null && currFilename != ".htm" && currFilename != ".html") {
          // do XML export
          TimelineXMLAdapter tim = new TimelineXMLAdapter();
          if (currFilename.endsWith(".htm") || currFilename.endsWith(".html")) {
            tim.saveTimelineHTML(selectedFile.getPath(), timeline, Client.getUserName(), gifFilename);
            saveGifImage(timeline, selectedFileGIF);
          }
        }

      } catch (Exception err) {
        JOptionPane.showMessageDialog(parentFrame, "Error saving timeline as web page.", "Save error",
                                      JOptionPane.ERROR_MESSAGE);
        err.printStackTrace();
        return;
      }
    }
    catch (Exception e)
    {
      JOptionPane.showMessageDialog(parentFrame, "Error saving timeline as web page.", "Save error",
                                    JOptionPane.ERROR_MESSAGE);
      e.printStackTrace();
      return;
    }
  }

  /**
   * openTimeline: opens a saved timeline with a dialog
   */
  public static void openTimeline(BasicWindow parent) {
    try {
      // set up dialog and file filter
      fileChooserOpen.setDialogTitle("Open Timeline");
      fileChooserOpen.setAcceptAllFileFilterUsed(false);
      fileChooserOpen.addChoosableFileFilter(filter);
      fileChooserOpen.setMultiSelectionEnabled(false);
      if (System.getProperty("os.name").startsWith("Mac OS")) {
        fileChooserOpen.setSize(new Dimension(fileChooserOpen.getWidth() + 100, fileChooserOpen.getHeight()));
      }
      // show file chooser dialog
      if (JFileChooser.APPROVE_OPTION == (fileChooserOpen.showOpenDialog(parent))) {
        String currFilename = fileChooserOpen.getSelectedFile().getPath();
        if (!currFilename.endsWith(".v2t") && !currFilename.endsWith(".tim")) {
          currFilename = currFilename  + ".tim";
        }
        fileChooserOpen.removeChoosableFileFilter(filter);
        if (currFilename != null) {
          java.io.File selectedFile = new java.io.File(currFilename);
          try {
            WindowManager.stopAllPlayers();
            // do XML import
            TimelineXMLAdapter time = new TimelineXMLAdapter();
            TimelineFrame tf = null;
            TimelinePanel tp = null;
            if (Client.isStandalone) {
              time.openingStandalone = true;
              tf = (TimelineFrame)parent;
              tp = tf.getTimelinePanel();
              if (tf.getTimelineLocalPlayer() == null) {
                time.openingInitialStandalone = true;
              }
            }
           tp.setSavePath(selectedFile.getPath());
            time.openTimelineXML(selectedFile.getPath(), tp, parent, false);
          } catch (Exception err) {
            JOptionPane.showMessageDialog(parent, "Error opening timeline.", "Open error",
                JOptionPane.ERROR_MESSAGE);
            err.printStackTrace();
          }
        }
        uilogger = parent.getUILogger();
        uilogger.log(UIEventType.BUTTON_CLICKED, "accept open timeline");
      }
      else {
        uilogger = parent.getUILogger();
        uilogger.log(UIEventType.BUTTON_CLICKED, "cancel open timeline");
      }
    }
    catch (Exception e)
    {
      JOptionPane.showMessageDialog(parent, "Error opening timeline.", "Open error",
                                    JOptionPane.ERROR_MESSAGE);
      e.printStackTrace();
    }
  }

  /**
   * openAudio: opens a local audio file for a new timeline
   */
  public static void openAudioFile(BasicWindow parent) {
    TimelineFrame frmTimeline;
    try {
      fileChooserOpenAudio.setDialogTitle("Choose Audio for the New Timeline");
      fileChooserOpenAudio.setAcceptAllFileFilterUsed(false);
      fileChooserOpenAudio.addChoosableFileFilter(audioFilt);
      fileChooserOpenAudio.setMultiSelectionEnabled(false);

      if (JFileChooser.APPROVE_OPTION == (fileChooserOpenAudio.showOpenDialog(parent))) {
        String currFilename = fileChooserOpenAudio.getSelectedFile().getPath();
        fileChooserOpenAudio.removeChoosableFileFilter(audioFilt);
        
        if (currFilename != null) {
          java.io.File selectedFile = new java.io.File(currFilename);
          try {
              frmTimeline = (TimelineFrame)parent;
          }
          catch (ClassCastException e) {
            frmTimeline = null;
          }
           if (frmTimeline != null && (frmTimeline.getTimelineLocalPlayer() == null && frmTimeline.getTimelinePlayer() == null)) {
            frmTimeline.isNewTimeline = true;
            frmTimeline.isNewAudio = true;
            frmTimeline.setContent(selectedFile, 0, 1);
             //frmTimeline.setTitle(currFilename.substring(currFilename.lastIndexOf("\\")+1));
          }
          else {
            BasicWindow newWindow = WindowManager.openWindow(WindowManager.WINTYPE_LOCAL_TIMELINE, WindowManager.WINLOCATION_CASCADE_DOWN);
            TimelineFrame newTimelineWindow = (TimelineFrame)newWindow;
            newTimelineWindow.isNewTimeline = true;
            newTimelineWindow.isNewAudio = true;
            WindowManager.stopAllPlayers();
            newTimelineWindow.setContent(selectedFile, 0, 1);
          }
        }
      }
      else {
      }
    }
    catch (Exception e)
    {
      JOptionPane.showMessageDialog(parent, "Error opening timeline.", "Open error",
                                    JOptionPane.ERROR_MESSAGE);
      e.printStackTrace();
    }
  }

  /**
   * createWavFile: creates a WAV file for an MP3 file
   */
  public static File createWavFile(BasicWindow parent, String sourcefile, String newfile) {

      try // convert mp3 to wav
      {
	           ((TimelineFrame)parent).getControlPanel().lblStatus.setText("Status: Converting Audio ...");
    	  	   Converter conv = new Converter();
	           int detail = (Converter.PrintWriterProgressListener.VERBOSE_DETAIL);
	           Converter.ProgressListener listener = new Converter.PrintWriterProgressListener(new PrintWriter(System.out, true), detail);
	           
   	  
          conv.convert(sourcefile, newfile, listener);
          
          return new File(newfile);
      }
      catch (Exception ex)
      {
        System.err.println("Conversion failure: "+ex);
        logger.debug("conversion failed");
        JOptionPane.showMessageDialog(parent, "Error converting audio.", "Conversion error",
                JOptionPane.ERROR_MESSAGE);
        ex.printStackTrace();

        return null;
      }
  }

  /**
   * chooseWavLocation: chooses a location for a new WAV file
   */
  public static File chooseWavLocation(BasicWindow parent, String filename) {
    //TimelineFrame frmTimeline;
    try {
        fileChooserSaveAudio.setDialogTitle("Choose Location for WAV file");
        fileChooserSaveAudio.setAcceptAllFileFilterUsed(false);
        fileChooserSaveAudio.removeChoosableFileFilter(saveFilter);
        fileChooserSaveAudio.addChoosableFileFilter(wavFilt);
        fileChooserSaveAudio.setMultiSelectionEnabled(false);
        fileChooserSaveAudio.setSelectedFile(new File(filename));
        	
      if (JFileChooser.APPROVE_OPTION == (fileChooserSaveAudio.showSaveDialog(parent))) {
    	  
        String currFilename = fileChooserSaveAudio.getSelectedFile().getPath();
        fileChooserSaveAudio.removeChoosableFileFilter(audioFilt);
        if (currFilename != null) {
          java.io.File selectedFile = new java.io.File(currFilename);

          // warn if file exists
            if (selectedFile.exists()) {
              int response = JOptionPane.showConfirmDialog(parent, "File " + selectedFile.getName() + " already exists. Overwrite?",
                  "Overwrite file?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
              if (response == JOptionPane.NO_OPTION) {
                return chooseWavLocation(parent, filename);
              }
              else {
                return selectedFile;
              }
            }

          return selectedFile;
       }
      }
    }
    catch (Exception e)
    {
      JOptionPane.showMessageDialog(parent, "Error creating WAV file.", "File error",
                                    JOptionPane.ERROR_MESSAGE);
      e.printStackTrace();
    }
    return null;
  }


  /**
   * findMissingAudio: finds a missing audio file for an opened timeline
   */
  public static File findMissingAudio(BasicWindow parent, String filename) {
    //TimelineFrame frmTimeline;
    try {
      fileChooserOpenAudio.setDialogTitle("Browse for Missing Audio: " + filename);
      fileChooserOpenAudio.setAcceptAllFileFilterUsed(false);
      fileChooserOpenAudio.addChoosableFileFilter(audioFilt);
      fileChooserOpenAudio.setMultiSelectionEnabled(false);

      if (JFileChooser.APPROVE_OPTION == (fileChooserOpenAudio.showOpenDialog(parent))) {
        String currFilename = fileChooserOpenAudio.getSelectedFile().getPath();
        fileChooserOpenAudio.removeChoosableFileFilter(audioFilt);
        if (currFilename != null) {
          java.io.File selectedFile = new java.io.File(currFilename);

          return selectedFile;
       }
      }
    }
    catch (Exception e)
    {
      JOptionPane.showMessageDialog(parent, "Error opening timeline.", "Open error",
                                    JOptionPane.ERROR_MESSAGE);
      e.printStackTrace();
    }
    return null;
  }

  /**
   * excerptTimeline: saves the selected portion of the timeline to a temporary timeline file
   * then opens it up in a new timeline frame
   */
  public static void excerptTimeline(Timeline timeline, int startPoint, int endPoint, TimelineFrame parentFrame) {
    try {
      java.io.File selectedFile = File.createTempFile("excerpt", "tim");
      selectedFile.deleteOnExit();
      try {
        // create new file
        if (selectedFile.exists()) {
          selectedFile.delete();
        }
        if (!(selectedFile.createNewFile())) {
          JOptionPane.showMessageDialog(parentFrame, "Error creating excerpt.", "Error",
                                        JOptionPane.ERROR_MESSAGE);
          return;
        }

        // do XML export
        TimelineXMLAdapter tim = new TimelineXMLAdapter();
        tim.excerptTimelineXML(selectedFile.getPath(), timeline, startPoint, endPoint);

        // now open the excerpted timeline in a new window
        TimelineXMLAdapter time = new TimelineXMLAdapter();
        if (parentFrame.isStandaloneVersion) {
          time.openingStandalone = true;
        }
        time.openTimelineXML(selectedFile.getPath(), parentFrame.getTimelinePanel(), parentFrame, true);

        // now delete the temporary xml file
        selectedFile.delete();

      } catch (Exception err) {
        JOptionPane.showMessageDialog(parentFrame, "Error creating excerpt.", "Error",
                                      JOptionPane.ERROR_MESSAGE);
        err.printStackTrace();
        return;
      }
    }
    catch (Exception e)
    {
      JOptionPane.showMessageDialog(parentFrame, "Error creating excerpt.", "Error",
                                    JOptionPane.ERROR_MESSAGE);
      e.printStackTrace();
      return;
    }
  }

  /**
   * saveGifImage: saves a gif image of the timeline to the given filename
   */
  protected static void saveGifImage(Timeline timeline, File filename) {
    // local variables
    int imageWidth = 660; // this width is chosen to be optimal for printing, not viewing onscreen
    TimelinePanel pnlTimeline = timeline.getPanel();
    boolean wasDirty = timeline.isDirty;
    int timelineX = timeline.getLineStart();
    int timelineY = timeline.getLineY();
    int timelineLength = timeline.getLineLength();
    boolean isResizable = timeline.isResizable();

    // set up print image
    pnlTimeline.fitToWindow();
    timeline.doLastResize(imageWidth - 20, (Graphics2D)pnlTimeline.getGraphics());
    timeline.moveTo(10, timeline.getTotalBubbleHeight() + 2);
    pnlTimeline.refreshTimeline();
    Rectangle oldBounds = pnlTimeline.getBounds();
    Rectangle newBounds = timeline.getRect();
    timeline.titleIsHidden = true;
    timeline.deselectAllBubbles();
    timeline.deselectAllTimepointsAndMarkers();
    timeline.getResizer().setEnabled(false);
    timeline.getSlider().setVisible(false);

    // set the new bounds of the timeline panel
    pnlTimeline.setBounds((int)newBounds.getX() - 10, (int)newBounds.getY(), imageWidth, (int)newBounds.getHeight() + 50);

    // create buffered image
    BufferedImage image = new BufferedImage(imageWidth, pnlTimeline.getSize().height, BufferedImage.TYPE_INT_RGB);
    Graphics2D graph = (Graphics2D)image.getGraphics();

    // paint image
    pnlTimeline.paint(graph);
    timeline.refresh(graph);

    try {
      // encode the image as a GIF
      GIFEncoder encode = new GIFEncoder(image, pnlTimeline.getFrame());
      OutputStream output = new BufferedOutputStream(new FileOutputStream(filename.getPath()));
      encode.Write(output);
    }
    catch (Exception e) {
      JOptionPane.showMessageDialog(timeline.getPanel().getFrame(), "Error creating GIF image.", "Error",
                                    JOptionPane.ERROR_MESSAGE);
      e.printStackTrace();
      return;
    }

    // restore old values
    pnlTimeline.setBounds(oldBounds);
    timeline.moveTo(timelineX, timelineY);
    pnlTimeline.resizeTimeline(timelineLength);
    timeline.getResizer().setEnabled(isResizable);
    timeline.getSlider().setVisible(true);
    timeline.titleIsHidden = false;
    pnlTimeline.undoManager.undo(); // should undo the fit to window command
    timeline.isDirty = wasDirty;
    pnlTimeline.refreshTimeline();
  }


  /**
   * createConstraints: sets up gridbag constraints for components in a gridbag layout
   */
  static public void createConstraints(Container container, Component component,
                                       int gridx, int gridy,
                                       int gridwidth, int gridheight,
                                       double weightx, double weighty,
    int anchor, int fill,
    int top, int left,
    int bottom, int right,
    int ipadx, int ipady) {

    GridBagConstraints constraint = new GridBagConstraints();

    constraint.gridx      = gridx;
    constraint.gridy      = gridy;
    constraint.gridwidth  = gridwidth;
    constraint.gridheight = gridheight;
    constraint.weightx    = weightx;
    constraint.weighty    = weighty;
    constraint.anchor     = anchor;
    constraint.fill       = fill;
    constraint.insets     = new Insets(top, left, bottom, right);
    constraint.ipadx	  = ipadx;
    constraint.ipady      = ipady;

    container.add(component, constraint);
  }

  /**
   * convertOffsetToHoursMinutesSecondsTenths - converts a millisecond offset into a
   *        friendlier hh:mm:ss.t format & returns the string. If time is less than one
   *        hour, returns mm:ss only.
   */
  static public String convertOffsetToHoursMinutesSecondsTenths(int offset) {
    int tenths = (int)(offset / 100);
    int seconds = (int)(offset / 1000);
    int minutes = seconds / 60;
    int hours = minutes / 60;
    String strTemp = "" + (tenths % 10);
    String strTenths = new String();
    String strSeconds = new String();
    String strMinutes = new String();
    seconds = seconds - (minutes * 60);
    minutes = minutes - (hours * 60);
    if (tenths == 0) {
      strTenths = "";
    } else {
      strTenths = "." + strTemp;
    }
    if (seconds < 10) {
      strSeconds = "0" + seconds;
    } else {
      strSeconds = "" + seconds;
    }
    if ((minutes < 10) && (hours > 0)) {
      strMinutes = "0" + minutes;
    } else {
      strMinutes = "" + minutes;
    }
    if (hours > 0) {
      return(hours + ":" + strMinutes + ":" + strSeconds + strTenths);
    } else {
      return(strMinutes + ":" + strSeconds + strTenths);
    }
  }

  /**
   * revertToSavedTimeline: reverts to the last saved timeline
   */
  public static void revertToSavedTimeline(Timeline timeline, TimelineFrame parent) {
    String currFilename = timeline.currFilename;

    // show confirmation dialog
    int response = JOptionPane.showConfirmDialog(parent, "Are you sure you want to revert to the last saved timeline?", "Revert to Saved?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
    if (response == JOptionPane.NO_OPTION) {
      uilogger.log(UIEventType.BUTTON_CLICKED, "cancel revert to saved");
      return;
    }
    uilogger.log(UIEventType.BUTTON_CLICKED, "accept revert to saved");

    // try reverting to saved file
    if (currFilename != null) {
      try {
        WindowManager.stopAllPlayers();
        TimelineXMLAdapter time = new TimelineXMLAdapter();
        TimelinePanel tp = timeline.getPanel();
        time.revertTimelineXML(currFilename, tp, (Graphics2D)tp.getGraphics());
      } catch (Exception err) {
        JOptionPane.showMessageDialog(parent, "Error reverting to saved timeline.", "Reverting error",
                                      JOptionPane.ERROR_MESSAGE);
        err.printStackTrace();
      }
    }
  }

  public static String getRelativePath(File base, File name) throws IOException  {
	    File parent = base.getParentFile();

	    if (parent == null) {
	        throw new IOException("No common directory");
	    }

	    String bpath = base.getCanonicalPath();
	    String fpath = name.getCanonicalPath();

	    if (fpath.startsWith(bpath)) {
	        return fpath.substring(bpath.length() + 1);
	    } else {
	        return (".." + File.separator + getRelativePath(parent, name));
	    }
	}
  
  public static String absolutePath(String relative, String absoluteTo)
  {       
      String[] absoluteDirectories = relative.split("\\\\");
      String[] relativeDirectories = absoluteTo.split("\\\\");
      int relativeLength = relativeDirectories.length;
      int absoluteLength = absoluteDirectories.length; 
      int lastCommonRoot = 0;
      int index;
      for (index = 0; index < relativeLength; index++)
          if (relativeDirectories[index].equals("..\\\\"))
              lastCommonRoot = index;
          else
              break;
      StringBuilder absolutePath = new StringBuilder();
      for (index = 0; index < absoluteLength - lastCommonRoot; index++)
      {  
           if (absoluteDirectories[index].length() > 0) 
               absolutePath.append(absoluteDirectories[index] + "\\\\");                          
      }
      for (index = lastCommonRoot; index < relativeLength  - lastCommonRoot; 
                                                             index++)
      {  
           if (relativeDirectories[index].length() > 0) 
               absolutePath.append(relativeDirectories[index] + "\\\\");                          
      }
      return absolutePath.toString();              
  }
}