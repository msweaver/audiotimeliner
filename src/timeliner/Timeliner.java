package timeliner;

import java.awt.Toolkit;

import org.apache.log4j.PropertyConfigurator;

//import javafx.embed.swing.JFXPanel;
import javafx.application.Application;
import javafx.stage.Stage;
import ui.common.BasicWindow;
import ui.common.UIUtilities;
import ui.common.WindowManager;
import ui.timeliner.TimelineFrame;
import ui.timeliner.TimelinePanel;
import ui.timeliner.TimelineXMLAdapter;
import util.AppEnv;
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
   
  public Timeliner() {

  }
  
  public static void main(String[] args) {
	  if (args.length > 0) {
		  String timfile = args[0];
          java.io.File timelineFile = new java.io.File(timfile);
		    if (!AppEnv.isGUISupported()){
		        System.err.println("Audio Timeliner currently only supports Windows and Mac OS X");
		        System.exit(1);
		    }
		    // make sure UIUtilities is initialized
		    
		    int x = UIUtilities.fontSizeHTML; x = x + 1;

		    // make sure log4j is initialized
		    if (System.getProperty("log4j.configuration")==null) {
		        PropertyConfigurator.configure(DEFAULT_LOG4J_CONF);
		    }
		    LogUtil.beginSession(Integer.valueOf((int)(Math.random()*100))); // pick a random session number
		  
		    WindowManager.doStartUp();
		    BasicWindow newWindow = WindowManager.openWindow(WindowManager.WINTYPE_LOCAL_TIMELINE, WindowManager.WINLOCATION_CASCADE_DOWN);
		    TimelineFrame newTimelineWindow = (TimelineFrame)newWindow;
		    newTimelineWindow.setTitle("New Timeline");

		    try {
	            TimelineXMLAdapter time = new TimelineXMLAdapter();
	            time.openingStandalone = true;
	            time.openingInitialStandalone = true;
	            TimelinePanel tp = newTimelineWindow.getTimelinePanel();
	            tp.setSavePath(timelineFile.getPath());
	            time.openTimelineXML(timelineFile.getPath(), tp, newWindow, false);
			  }
			  catch (Exception e) {
			  }
	  }
	  else {
		  launch(args);		   
	  }
  }

   public void start(Stage stage) throws Exception {
	    if (!AppEnv.isGUISupported()){
	        System.err.println("Audio Timeliner currently only supports Windows and Mac OS X");
	        System.exit(1);
	    }
	    // make sure UIUtilities is initialized
	    int x = UIUtilities.fontSizeHTML; x = x + 1;

	    // make sure log4j is initialized
	    if (System.getProperty("log4j.configuration")==null) {
	        PropertyConfigurator.configure(DEFAULT_LOG4J_CONF);
	    }
	    LogUtil.beginSession(Integer.valueOf((int)(Math.random()*100))); // pick a random session number
	    
	    WindowManager.doStartUp();
	    BasicWindow newWindow = WindowManager.openWindow(WindowManager.WINTYPE_LOCAL_TIMELINE, WindowManager.WINLOCATION_CASCADE_DOWN);
	    TimelineFrame newTimelineWindow = (TimelineFrame)newWindow;
	    newTimelineWindow.setTitle("New Timeline");
	    newTimelineWindow.launchWizard();
	    }

}
