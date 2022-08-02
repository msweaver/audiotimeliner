package media;

import java.util.*;

//import javax.swing.JOptionPane;

import java.awt.*;
import java.io.*;
import javax.sound.sampled.*;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.util.Duration;
import ui.timeliner.TimelineLocalPlayer;
import util.AppEnv;

import org.apache.log4j.Logger;

/**
 * The AudioHandler
 */
public class AudioHandler implements PlayableContentHandler {

    private static Logger logger = Logger.getLogger(AudioHandler.class);
    //how often the TASK thread fires
   final static String CONTENT_EOS = "End of stream.";
   final static int TASK_FREQ = 1;
   static String bogusMedia = AppEnv.getAppDir()+ "resources/audio/null.mp3";
   //private final Object disposeLock = new Object();

    /////////////////////VARIABLES////////////////////////////
    /**
     * The local copy of the content reference.
     */
    private String contentRef;
    /**
     * End-of-stream flag
     */
    private boolean eos;
    /**
     * The list of the current event listeners.
     */
    private Vector<PlayableContentHandlerListener> listenerList;
    /**
     * The object responsible for controlling playback.
     */
//    private QTComponent qtPlay;
    /**
     * Used to wait for EOS
     */
    java.util.Timer tim;
    /**
     * Whether or not content is local
     */
    //private boolean local = false;
    //private boolean stopper = false;
    
    public Media audioFile;
    public MediaPlayer audioPlayer;

    /**
     */
    public AudioHandler() throws HandlerInitException{
        tim = new java.util.Timer();    //initialize the timer that will wait for EOS events
        AudioTask eosTask = new AudioTask(tim);
        tim.schedule(eosTask, 0, TASK_FREQ);     //timer fires every hundredth of a sec.
        listenerList = new Vector<PlayableContentHandlerListener>();             //initialize list of listeners
        contentRef = null;                       //contentref will be set later
        eos = false;
    }

    /**
     */
    public Component getQTComponent() {
            return null;
    }

    /**
     * returns the duration
     * @throws IOException 
     * @throws UnsupportedAudioFileException 
     */
    public int getDuration()  {
      return (int)audioFile.getDuration().toMillis(); // return milliseconds?
    }
    
    public void setContentRef(String ref) {
    	return;
    }
    public void setContentRef(String ref, TimelineLocalPlayer tplayer) {
        logger.debug("Open url " + ref);
        
        String mp3FilePath=null;

                mp3FilePath = ref.replaceAll("\\\\", "/");
                mp3FilePath = mp3FilePath.replaceAll(" ", "%20");
                logger.debug("mp3 path " + mp3FilePath);

                //is file local?
        //if (mp3FilePath.startsWith("file://")) {
        //    local = true;
        //} else {
        //    local = false;
        //}
       
            audioFile = new Media(mp3FilePath);
            logger.debug("audio file " + audioFile.getSource());
            
        	audioPlayer = new MediaPlayer(audioFile);
        
 /**        audioPlayer.setOnReady(new Runnable() {

            @Override
            public void run() {

                System.out.println("Duration: "+audioFile.getDuration().toSeconds());

                // display media's metadata
                for (Map.Entry<String, Object> entry : audioFile.getMetadata().entrySet()){
                    System.out.println(entry.getKey() + ": " + entry.getValue());
                }
                
                tplayer.startUp();
                
            }
        });
     **/    
         audioPlayer.setOnEndOfMedia(new Runnable() {
         	
         	@Override
         	public void run() {
//         		logger.debug("found the end");
         		eos = true;
         	}
         	
         }
     );

         while (audioPlayer.getStatus() != MediaPlayer.Status.READY) {
        	 logger.debug("loading...");
        	 }
         //System.out.println("Duration: "+audioFile.getDuration().toSeconds());

         // display media's metadata
         for (Map.Entry<String, Object> entry : audioFile.getMetadata().entrySet()){
             //System.out.println(entry.getKey() + ": " + entry.getValue());
             logger.debug(entry.getKey() + ": " + entry.getValue());
         }
         tplayer.startUp();
        

        try {
//        	DataRef newRef = null;
            if (ref.startsWith("http://") || ref.startsWith("HTTP://")) {
                java.net.URL url = new java.net.URL(ref);
                java.io.InputStream in = url.openStream();
                byte[] x = new byte[1000];
                int counter = 0;
                while (counter < 1000) {
                    int val = in.read();
                    if (val == -1) {
                        break;
                    }
                    x[counter] = (byte)val;
                    counter = counter + 1;

                }
//                newRef = new DataRef(new QTHandle(x), StdQTConstants.kDataRefExtensionMIMEType , "video/quicktime");
            } else {
//                newRef = new DataRef(ref);
            }
//            Movie mov = Movie.fromDataRef(newRef, StdQTConstants.newMovieActive);
//            MovieController mc = new MovieController(mov);
//            if (qtPlay != null) {
//                qtPlay.setMovieController(mc);
//            } else {
//                qtPlay = QTFactory.makeQTComponent(mc);
 //           }
        } catch (Exception e) {
            System.err.println("Error with URL: " + ref);
            e.printStackTrace();
            return;
        }
        contentRef = ref;
        eos = false;
        return;
    }

    public String getContentRef() {
        return contentRef;
    }

    public void play() {
    	
   	audioPlayer.play();
    //	try {
    //	thePlayer.play(); } catch (Exception e) {}
    
   	}
 
    public void pause() {
        audioPlayer.pause();
   }
    
    public boolean isPlaying() {
    	return audioPlayer.getStatus().equals(Status.PLAYING);
    }
    public void stop() {
    	audioPlayer.stop();
    	setOffset(0);
    }

    public void setOffset(int offset) {
       // double x = 0f;
        boolean playflag = false;
        try {
            //start player at new position
        	audioPlayer.pause();
        	//audioPlayer.setStartTime(Duration.millis(offset));
        	Duration off = new Duration(offset);
        	double seekSeconds = off.toMillis() / 1000.0;
        	logger.debug("seekSeconds = " + seekSeconds);
        	audioPlayer.seek(Duration.millis(offset)); 
            logger.debug("setting offset to " + offset);
            if (playflag) {                     //start playback again if playing before
            	audioPlayer.play();
            }
            logger.debug("current offset in player is = " + audioPlayer.getCurrentTime());
        } catch (Exception e) {
        }
    }

    public int getOffset() {
    	double x = audioPlayer.getCurrentTime().toMillis();
    	//logger.debug(audioPlayer.getCurrentTime().toMillis());
    	return (int)x;
    }

    public void setVolume(float vol) {
        if ((vol >= 0) && (vol <= 1)) {
        	audioPlayer.setVolume((double)vol);
        }
    }

    public float getVolume() {
    	float x = (float)audioPlayer.getVolume();
        return x;
    }

    public void addListener(PlayableContentHandlerListener listener) {
        if (!(listenerList.contains(listener))) {       //add this listener if not already there
            listenerList.add(listener);
        }
    }

    public void removeListener(PlayableContentHandlerListener listener) {
        if (listenerList.contains(listener)) {          //delete this listener if it already exists
            listenerList.remove(listener);
        }
    }

    /**
     * Send an event to all of this classes' listeners
     *
     * @param msgType   the type of PlayableContentHandlerEvent to be sent
     * @param msg       the message to be sent
     */
    private void sendEvent(int msgType, String msg){
        //get list size and iterate through entire list
        int totlist = listenerList.size();
        while (totlist > 0) {
            ((PlayableContentHandlerListener)listenerList.get(totlist - 1)).receiveEvent(new PlayableContentHandlerEvent(this, msgType, msg));
            totlist = totlist - 1;
        }
    }

    private class AudioTask extends TimerTask {
        //java.util.Timer myTimer = null;
        public AudioTask(java.util.Timer aTimer) {
            super();
            //myTimer = aTimer;
        }
        public void run() {
            if (eos) {
                eos = false;
                sendEvent(PlayableContentHandlerEvent.END_OF_CONTENT, "End of stream");
                setOffset(0);
                audioPlayer.stop();
            }
        }
    }
    
}