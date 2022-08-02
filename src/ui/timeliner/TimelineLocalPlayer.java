package ui.timeliner;

import java.io.*;

import media.*;
import ui.common.UIUtilities;

import java.util.concurrent.*;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

/**
 * A player assigned to a timeline window. 
 */

public class TimelineLocalPlayer implements PlayableContentHandlerListener {

  // local audio variables
	  File filename;
	  AudioHandler player;
	  int currstart = 0;
	  int currstop = 0;
  //QTContentHandler qtPlayer;
	  private static Logger logger = Logger.getLogger(TimelineLocalPlayer.class);

/////EXTERNAL//////////
  //Will be true if playing OR buffering
  public boolean isPlaying = false;

  public TimelineFrame parentWindow = null;
  TimelinePanel pnlTimeline;
  TimelineControlPanel pnlControl;
  Timeline timeline;

/////INTERNAL//////////
//  java.util.Timer theTimer = new java.util.Timer();
  private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);
  private Runnable theTimer; 
  private ScheduledFuture<?> timerHandle; 
  
  //tracking variables
  protected final static int TRACKING_MAX = 30000;
  protected final static float TRACKING_INCREASE_RATE = 1.05f;
  protected final static int INITIAL_TRACKING_AMOUNT = 150;
  int shiftAmount = INITIAL_TRACKING_AMOUNT;
  protected final static int TRACKING_FF = 1;
  protected final static int TRACKING_RW = -1;
private static final PlayableContentHandlerEvent playerEvent = null;

  //amount of offset in millis that start of excerpt is from start of whole container
  int startOffset = 0;
  //amount of offset in millis that end of excerpt is from start of whole container
  int endOffset = 0;

  // local start and end variables that allow for looping, etc.
  int localStartOffset = 0;
  int localEndOffset = 0;
  int nextImportantOffset = 0;

  /**
   * timeline local player constructor: requires a filename, start and stop offsets, and a timeline frame parent
   */
  public TimelineLocalPlayer(File file, int start, int stop, TimelineFrame parent) throws Exception {
    filename = file;
    parentWindow = parent;
    pnlTimeline = parent.getTimelinePanel();
    pnlControl = parent.getControlPanel();
    currstart = start;
    currstop = stop;

    try {
        player = new AudioHandler();
      } catch (HandlerInitException err) {
        throw new ContentLoadingException("Trouble initiating audio.");
      }
      player.addListener(this);      
                  
      player.setContentRef("file:///" + (filename).toString(), this);
 
  }

  public void startUp() {
	//start up the timer
	  
	  theTimer = new Runnable() {
		    String elapsedString = "";
		    String totalString = "";
		    int prevVal = 0;

		    public void run() {

		      int curshiftamt;
		      if (player == null) {
		        return;
		      }
		      int off = getOffset();
		      //logger.debug(off);
 
		      // if playing
		     	if (player.isPlaying()) { 
		     		isPlaying=true; 
				      pnlControl.lblStatus.setText(pnlControl.STATUS_PLAYING);
		     	}
		     	else {
		     		isPlaying=false;
				      pnlControl.lblStatus.setText(pnlControl.STATUS_IDLE);
		     	}
		      if (timeline != null && isPlaying) {
		        // send message when next important offset is reached
		        if (off > nextImportantOffset) {
		          timeline.notifyOfImportantOffset(off);
		        }
		        if (off != prevVal) {
		        	int currOff = player.getOffset();
		        	// logger.debug("setting slider to " + currOff);
		          timeline.getSlider().setValue(currOff);
		          //setOffset(off);
		        }
//		        else {
//		          pnlControl.lblStatus.setText(pnlControl.STATUS_BUFFERING);
//		        }
		        prevVal = off;
		      }

		      // if tracking
		      else if (timeline != null) {
		        switch (pnlControl.trackingState) {
		          case TRACKING_FF:
		            curshiftamt = getOffset() + shiftAmount;
		            if (curshiftamt > (endOffset)) {
		              curshiftamt = endOffset - 2;        //trying to jump past end of container
		            }
		            else if (!timeline.stopPlayingAtSelectionEnd && curshiftamt > startOffset + localEndOffset) {
		              curshiftamt = startOffset + localEndOffset - 2;
		            }
		            setOffset(curshiftamt);
		            if (shiftAmount < TRACKING_MAX) {      //gradually increase tracking amount
		              shiftAmount = (int)((float)shiftAmount * TRACKING_INCREASE_RATE);
		            }
		            if (off != prevVal) {
		              timeline.getSlider().setValue(off);
		            }
		            prevVal = off;
		            break;
		          case TRACKING_RW:
		            curshiftamt = getOffset() - shiftAmount;
		            if (curshiftamt < startOffset) {
		              curshiftamt = startOffset;        //trying to shift before beginning of container
		            }
		            else if (!timeline.stopPlayingAtSelectionEnd && curshiftamt < startOffset + localStartOffset) {
		              curshiftamt = startOffset + localStartOffset;
		            }
		            setOffset(curshiftamt);
		            if (shiftAmount < TRACKING_MAX) {      //gradually increase tracking amount
		              shiftAmount = (int)((float)shiftAmount * TRACKING_INCREASE_RATE);
		            }
		            if (off != prevVal) {
		              timeline.getSlider().setValue(off);
		            }
		            prevVal = off;
		            break;
		        }
		      }
		      else {
		        timeline = pnlTimeline.getTimeline();
		      }
		      
		      // update time display		      
		      totalString = UIUtilities.convertOffsetToHoursMinutesSeconds(getDuration());
		      elapsedString = UIUtilities.convertOffsetToHoursMinutesSeconds(off);
		      pnlControl.lblElapsed.setText("" + elapsedString + " / " + totalString);


		    }
	  
	  };
	     timerHandle = 
	    	       scheduler.scheduleAtFixedRate(theTimer, 0, 10, TimeUnit.MILLISECONDS);
     	
      //make sure offsets are within valid range for this container
      if ((currstop-currstart) > player.getDuration())  {
          logger.debug("Trouble initiating audio. Start/stop times are incorrect values.");
        } 
      logger.debug("start: " + currstart + ", stop: " + currstop + ", duration: " + player.getDuration());

      //set start offset into container, navigate there
      startOffset = currstart;
      if (filename.toString().endsWith("null.mp3")) {
        startOffset = 0;
        endOffset = currstop - currstart;
      }
      else if (currstop != 1) {
        endOffset = currstop;
      }
      else {
        try {
          endOffset = player.getDuration();
        }
        catch (Exception e) {}
      }
      localStartOffset = 0 ;
      localEndOffset = endOffset - startOffset;
      player.setOffset(startOffset);
      logger.debug("localStartOffset: " + localStartOffset + ", localEndOffset: " + localEndOffset);

    //go to beginning of excerpt
    setOffset(0);

    //tell components that audio is ready at this point
    pnlTimeline.setLocalPlayer(this, parentWindow.isNewTimeline, parentWindow.isNewAudio);
    pnlControl.setLocalPlayer(this);
    pnlControl.doPlayerEnable();
    pnlControl.lblStatus.setText(pnlControl.STATUS_IDLE);

  }

  public void restart() {
		//start up the timer
		  
		  theTimer = new Runnable() {
			    String elapsedString = "";
			    String totalString = "";
			    int prevVal = timeline.getSlider().getValue();

			    public void run() {

			      int curshiftamt;
			      if (player == null) {
			        return;
			      }
			      int off = getOffset();
	 
			      // if playing
			     	if (player.isPlaying()) { 
			     		isPlaying=true; 
					      pnlControl.lblStatus.setText(pnlControl.STATUS_PLAYING);
			     	}
			     	else {
			     		isPlaying=false;
					      pnlControl.lblStatus.setText(pnlControl.STATUS_IDLE);
			     	}
			      if (timeline != null && isPlaying) {
			        // send message when next important offset is reached
				    if (off != prevVal) {
					          timeline.getSlider().setValue(off);
					}
			        if (off > nextImportantOffset) {
			          timeline.notifyOfImportantOffset(off);
			        }
//			        else {
//			          pnlControl.lblStatus.setText(pnlControl.STATUS_BUFFERING);
//			        }
			        prevVal = off;
			      }

			      // if tracking
			      else if (timeline != null) {
			        switch (pnlControl.trackingState) {
			          case TRACKING_FF:
			            curshiftamt = getOffset() + shiftAmount;
			            if (curshiftamt > (endOffset)) {
			              curshiftamt = endOffset - 2;        //trying to jump past end of container
			            }
			            else if (!timeline.stopPlayingAtSelectionEnd && curshiftamt > startOffset + localEndOffset) {
			              curshiftamt = startOffset + localEndOffset - 2;
			            }
			            setOffset(curshiftamt);
			            if (shiftAmount < TRACKING_MAX) {      //gradually increase tracking amount
			              shiftAmount = (int)((float)shiftAmount * TRACKING_INCREASE_RATE);
			            }
			            if (off != prevVal) {
			              timeline.getSlider().setValue(off);
			            }
			            prevVal = off;
			            break;
			          case TRACKING_RW:
			            curshiftamt = getOffset() - shiftAmount;
			            if (curshiftamt < startOffset) {
			              curshiftamt = startOffset;        //trying to shift before beginning of container
			            }
			            else if (!timeline.stopPlayingAtSelectionEnd && curshiftamt < startOffset + localStartOffset) {
			              curshiftamt = startOffset + localStartOffset;
			            }
			            setOffset(curshiftamt);
			            if (shiftAmount < TRACKING_MAX) {      //gradually increase tracking amount
			              shiftAmount = (int)((float)shiftAmount * TRACKING_INCREASE_RATE);
			            }
			            if (off != prevVal) {
			              timeline.getSlider().setValue(off);
			            }
			            prevVal = off;
			            break;
			        }
			      }
			      else {
			        timeline = pnlTimeline.getTimeline();
			      }
			      
			      // update time display		      
			      totalString = UIUtilities.convertOffsetToHoursMinutesSeconds(getDuration());
			      elapsedString = UIUtilities.convertOffsetToHoursMinutesSeconds(off);
			      pnlControl.lblElapsed.setText("" + elapsedString + " / " + totalString);

			    }
		  
		  };
		     timerHandle = 
		    	       scheduler.scheduleAtFixedRate(theTimer, 0, 10, TimeUnit.MILLISECONDS);
		  // System.out.println(timerHandle.toString());

	  }
  
/////EXTERNAL//////////
  /**
   * @return Current offset in milliseconds
   */
  protected int getOffset() {
    int offset;
      offset = player.getOffset();
      if ((offset - this.startOffset) > 0) {
        return offset - this.startOffset;
      }
      else {
        return 0;
      }
  }

  protected java.awt.Component getQTComponent() {
      return ((java.awt.Component)player.getQTComponent());
  }

  /**
   * Set current offset in milliseconds.
   */
  protected void setOffset(int offset) {
	  
    if (offset < 0) return;
    boolean wasPlaying = false;
    if (isPlaying) {
      player.pause();
      wasPlaying = true;
    }

    if ((offset + startOffset) > endOffset) {
      //goes out of excerpt range; can't do it!
    	if (wasPlaying) {
    	      player.play();
    	    }
    	return;
    }

    player.setOffset(offset + startOffset);

    //start playing again, if we were before
    if (wasPlaying) {
      player.play();
    }
  }

  /**
   * @return duration of entire excerpt in milliseconds
   */
  protected int getDuration() {
    return endOffset - startOffset;
  }

  /**
   * play
   */
  protected void play() {
    isPlaying = true;
    player.play();
    if (timerHandle.isDone()) {
    	logger.debug("timer stopped, restarting ...");
    	timerHandle.cancel(true);
    	this.restart();
    }
  }

  /**
   * pause
   */
  public void pause() {
    isPlaying = false;
    player.pause();
  }
  
  public void stop() {
	  isPlaying = false;
	  if (localStartOffset == 0) {
		  player.stop();
	  }
	  else {
		  player.pause();
	  }
	  //player.stop();
      }

  /**
   * performs a pause
   */
  public void doPause() { // called externally
    pnlControl.btn_pauseAction();
  }

  /**
   * turnOffTimer: called before window is destroyed to prevent errors
   */
  protected void turnOffTimer() {
	  scheduler.shutdown();
  }

/////INTERNAL//////////
  /**
   * Receive Player events and process them
   */
  public void receiveEvent(PlayableContentHandlerEvent event) {
    logger.debug("event = " + event.getMessage());
    switch (event.getType()) {
      case PlayableContentHandlerEvent.CONTENT_NOT_FOUND:
        pnlControl.lblStatus.setText(pnlControl.STATUS_STREAM_NOT_FOUND);
        break;
      case PlayableContentHandlerEvent.CONTENT_PROBLEM:
        pnlControl.lblStatus.setText(pnlControl.STATUS_STREAM_ERROR);
        break;
      case PlayableContentHandlerEvent.END_OF_CONTENT:
        //usually, this won't happen, unless excerpt goes all the way to end of container
        if (parentWindow != null) {
          pause();
          setOffset(endOffset - 1);
          parentWindow.endOfContent();
        }
        break;
    }
  }

  /**
   * sets the volume
   */
  protected void setVolume(float f) {
    player.setVolume(f);
  }
}

