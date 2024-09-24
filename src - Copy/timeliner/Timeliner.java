package timeliner;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.apache.commons.io.*;

//import javax.swing.JOptionPane;

//import org.apache.commons.io.FileDeleteStrategy;
import org.apache.log4j.Logger;

//import java.awt.Toolkit;

import org.apache.log4j.PropertyConfigurator;

import javafx.application.Application;
import javafx.stage.Stage;
import ui.common.BasicWindow;
import ui.common.UIUtilities;
import ui.common.WindowManager;
import ui.timeliner.TimelineFrame;
import ui.timeliner.TimelinePanel;
//import ui.timeliner.TimelineSlider;
import ui.timeliner.TimelineXMLAdapter;
import util.AppEnv;
import util.SwingDPI;
import util.logging.LogUtil;

/**
 * Title:        Timeliner
 * Copyright:    Copyright (c) 2004
 * Company:      Indiana University
 * @author Brent Yorgason
 * @version 1.0
 */

public class Timeliner extends Application {

  public static final String DEFAULT_LOG4J_CONF = AppEnv.getAppDir() + "conf/client/timeliner_console.lcf";
 // private static final String APPLICATION_NAME = "Audio Timeliner";
 // private static final String APPLICATION_ICON = AppEnv.getAppDir()+"resources/common/timeliner.gif";
  private static Logger log = Logger.getLogger(Timeliner.class);
  private static String timfile = "";
  private static java.io.File timelineFile;

  public Timeliner() {

  }
  
  public static void main(String[] args) {
	  
	  if (args.length > 0) {
		  timfile = args[0];
		  timelineFile = new java.io.File(timfile);
	  }
	  launch(args);		   
	
  }

  	@Override
   public void start(Stage stage) throws Exception {
  		
  		if (!AppEnv.isGUISupported()){
	        System.err.println("Audio Timeliner currently only supports Windows and Mac OS X");
	        System.exit(1);
	    }
	    // make sure UIUtilities is initialized
	    int x = UIUtilities.fontSizeHTML; 

	    // make sure log4j is initialized
	    if (System.getProperty("log4j.configuration")==null) {
	        String fileSeparator = File.separator;
	        String input = "";
        	if (System.getProperty("os.name").startsWith("Mac OS")) {
        	 	 input = "conf" + fileSeparator + "client" + fileSeparator + "timeliner_console.lcf";
        	} else {
	   	    	 input = "conf/client/timeliner_console.lcf";
        	}
	    	InputStream logstream = getClass().getClassLoader().getResourceAsStream(input);
	    	Path templog = Files.createTempFile("temp", ".lcf");
	    	templog.toFile().deleteOnExit();
	    	Files.copy(logstream, templog, StandardCopyOption.REPLACE_EXISTING);
	    	File logfile = new File(templog.toFile().getPath());
	    	
	    	// delete old temporary files
	    	
	      	String tempPath = System.getProperty("java.io.tmpdir") + File.separator + "atdata" + File.separator;
	    	File tempfile = new File(tempPath);
	    	try {
	    		FileUtils.cleanDirectory(tempfile); 
	    	} catch (Exception e) {
	    		e.printStackTrace();
	    	}
	    
	    	String log = logfile.getPath();
	    	//String logfile = getClass().getResource("/conf/client/timeliner_console.lcf").toString();
	        PropertyConfigurator.configure(log); //DEFAULT_LOG4J_CONF); //logfile
	    }
	    LogUtil.beginSession(Integer.valueOf((int)(Math.random()*100))); // pick a random session number
	    
	    SwingDPI.applyScalingAutomatically();
	    
        //URLDownload.fileUrl("https://maxsteinerinstitute.org/audio/Winters-24-SA.mp3", "test", "C:\\Users\\Brent Yorgason\\Videos\\To Watch");

	    WindowManager.doStartUp();
	    BasicWindow newWindow = WindowManager.openWindow(WindowManager.WINTYPE_LOCAL_TIMELINE, WindowManager.WINLOCATION_CASCADE_DOWN);
	    TimelineFrame newTimelineWindow = (TimelineFrame)newWindow;
	    newTimelineWindow.setTitle("New Timeline");

	    if (timfile !="") { // read in timeline file
	        java.io.File timelineFile = new java.io.File(timfile);
			log.debug("loading file: " + timfile);
			//log.debug("path = " + timelineFile);
		
		    try {
	            TimelineXMLAdapter time = new TimelineXMLAdapter();
	            // log.debug("XML adapter created");
	            time.openingStandalone = true;
	            time.openingInitialStandalone = true;
	            TimelinePanel tp = newTimelineWindow.getTimelinePanel();
	            tp.setSavePath(timelineFile.getPath());
	            // log.debug("path set to: " + timelineFile.getPath());
	
	            time.openTimelineXML(timelineFile.getPath(), tp, newWindow, false);
			  }
			  catch (Exception e) {
			  }
	    }
	    else {

	    	newTimelineWindow.launchWizard();
	    }
  	}
}