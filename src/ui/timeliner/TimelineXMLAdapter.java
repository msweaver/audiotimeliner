package ui.timeliner;

import org.apache.log4j.Logger;
import org.w3c.dom.*;

//import com.sun.tools.javac.util.Log;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import java.util.*;
import java.io.*;
import java.awt.*;
import javax.swing.*;
import ui.common.*;
import util.AppEnv;

/**
 * TimelineXMLAdapter
 * Methods for the creation, saving, and opening of XML representations of the timeline
 */

public class TimelineXMLAdapter {
  DocumentBuilder builder = null;

  // files
  final static String TIMELINE_XSL_FILENAME = AppEnv.getAppDir()+ "data/timeline/xml2html.xsl";
  java.io.File tempFile;
  java.io.File newPath;
  String selectedPath;
  String currOpenPath;
  private static Logger log = Logger.getLogger(TimelineUtilities.class);

  // external
  TimelinePanel pnlTimeline;
  BasicWindow parent;

  // flags
  protected boolean openingFromV2T = false;
  protected boolean restoringTempFile = false;
  protected boolean revertingToSavedFile = false;
  public boolean openingStandalone = false;
  public boolean openingInitialStandalone = false;

  // variables
  Graphics2D g2d;
  protected int numSavedLevels = 9;
  protected int bubCount = 0;

  static String bogusMedia = AppEnv.getAppDir()+ "resources/audio/null.mp3";

  /**
   * constructor
   */
  public TimelineXMLAdapter() throws ParserConfigurationException{
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    builder = factory.newDocumentBuilder();
  }

  /**
   * openTimelineXML: opens a timeline from an XML file, called from a Timeliner window
   */
  public Timeline openTimelineXML(String filename, TimelinePanel tp, Graphics2D g) throws Exception{
    if (filename.endsWith(".~~~")) {
      restoringTempFile = true;
      tempFile = new java.io.File(filename);
    }
    pnlTimeline = tp;
    g2d = g;
    Document doc = builder.parse(new File(filename));
    this.currOpenPath = filename;
    Timeline t = importTimeline(doc.getDocumentElement(), filename);
    if (t!= null && !filename.endsWith(".~~~")) {
      t.currFilename = filename;
    }
    return t;
  }

  /**
   * openTimelineXML: this version is for timelines that are not opened from a Timeliner window
   */
  public void openTimelineXML(String filename, TimelinePanel tp, BasicWindow wind, boolean isExcerpt) throws Exception{
    if (filename.endsWith(".~~~")) {
      restoringTempFile = true;
      tempFile = new java.io.File(filename);
    }
    pnlTimeline = tp; // warning: may be null
    parent = wind;

    Document doc = builder.parse(new File(filename));
    this.currOpenPath = filename;
    Timeline t = importTimeline(doc.getDocumentElement(), filename);
    if (t != null && !isExcerpt && !filename.endsWith(".~~~")) {
      t.currFilename = filename;
    }
    else if (t != null) {
      t.getPanel().getMenuBar().menuiRevertToSaved.setEnabled(false);
    }
  }

  /**
   * revertTimelineXML: reverts to a saved XML file, called from a Timeliner window
   */
  public Timeline revertTimelineXML(String filename, TimelinePanel tp, Graphics2D g) throws Exception{
    revertingToSavedFile = true;
    tempFile = new java.io.File(filename);
    pnlTimeline = tp;
    g2d = g;
    Document doc = builder.parse(new File(filename));
    Timeline t = importTimeline(doc.getDocumentElement(), filename);
    if (t!= null && !filename.endsWith(".~~~")) {
      t.currFilename = filename;
    }
    return t;
  }

  /**
   * excerptTimelineXML: excerpts a timeline to an XML file
   */
  public void excerptTimelineXML(String filename, Timeline timeline, int startPoint, int endPoint) throws Exception {
    Document doc = excerptTimeline(timeline, startPoint, endPoint);
    TransformerFactory factory = TransformerFactory.newInstance();
    Transformer serializer = factory.newTransformer();
    serializer.setOutputProperty(OutputKeys.INDENT, "yes"); // turn on pretty-printing
    serializer.transform(new DOMSource(doc), new StreamResult(new FileOutputStream(filename)));
  }
  
  /**
   * saveTimelineXML: saves a timeline to an XML file
   */
  public void saveTimelineXML(String filename, Timeline timeline, String username) throws Exception {
	  timeline.getPanel().setSavePath(filename);
    Document doc = exportTimeline(timeline);
    addTimelineAnnotation(doc, username);
    TransformerFactory factory = TransformerFactory.newInstance();
    Transformer serializer = factory.newTransformer();
    serializer.setOutputProperty(OutputKeys.INDENT, "yes"); // turn on pretty-printing
    serializer.transform(new DOMSource(doc), new StreamResult(new FileOutputStream(filename)));
  }

  /**
   * saveTimelineHTML: exports a timeline xml representation as an html file
   */
  public void saveTimelineHTML(String filename, Timeline timeline, String username, String gifName) throws Exception {
    Document doc = exportTimelineHTML(timeline);
    addTimelineAnnotation(doc, username);
    addGif(doc, gifName);
    TransformerFactory factory = TransformerFactory.newInstance();
    Source xslSource = new StreamSource(TIMELINE_XSL_FILENAME);
    Transformer trans = factory.newTransformer(xslSource);
    trans.setOutputProperty(OutputKeys.INDENT, "yes"); // turn on pretty-printing
    trans.setParameter("fontString", UIUtilities.fontHTML);
    trans.transform(new DOMSource(doc), new StreamResult(new FileOutputStream(filename)));
  }

  /**
   * excerptTimeline: creates and returns an excerpted timeline document
   */
  public Document excerptTimeline(Timeline timeline, int startPoint, int endPoint) throws Exception {
    Document document = builder.newDocument();
    Element currentElement = null;
    currentElement = timeline.toExcerpt(document, startPoint, endPoint);
    document.appendChild(currentElement);
    return document;
  }

  /**
   * exportTimeline: creates and returns a timeline document
   */
  public Document exportTimeline(Timeline timeline) throws Exception {
    Document document = builder.newDocument();
    processTimelineToXML(document, null, timeline);
    return document;
  }

  /**
   * exportTimelineHTML: creates and returns a timeline document for a web page
   */
  public Document exportTimelineHTML(Timeline timeline) throws Exception {
    Document document = builder.newDocument();
    processTimelineToXMLforHTML(document, null, timeline);
    return document;
  }

  /**
   * importTimeline: given a document, returns a timeline object
   */
  public Timeline importTimeline(Element root, String filename) throws Exception{
    Timeline timeline = processXMLToTimeline(root);
    return timeline;
  }

  /**
   * openFromV2T: opens a timeline from a v2t file
   */
  public void openFromV2T(Element root, String filename) {
    openingFromV2T = true;
    try {
      importTimeline(root, filename);
    }
    catch (Exception e) {
      e.printStackTrace();
      JOptionPane.showMessageDialog(null, "Error opening timeline file.", "Open error",
                                    JOptionPane.ERROR_MESSAGE);
    }
  }

  /**
   * processTimelineToXML: transforms the timeline into its XML representation
   */
  protected void processTimelineToXML(Document doc, Element root, Timeline timeline) throws Exception {
    Element currentElement = null;
    // add comment
    Comment com = doc.createComment("To open this file, start Audio Timeliner and choose \"Open Timeline...\" from the File menu. Locate your copy of this file and open it.)");
    doc.appendChild(com);
    // create timeline tag
    currentElement = timeline.toElement(doc);
    doc.appendChild(currentElement);
  }

  /**
   * processTimelineToXMLforHTML: transforms the timeline into its XML representation for HTML
   */
  protected void processTimelineToXMLforHTML(Document doc, Element root, Timeline timeline) throws Exception {
    Element currentElement = null;
    // create timeline tag
    currentElement = timeline.toElementForHTML(doc);
    doc.appendChild(currentElement);
  }

  /**
   * processBubbleTreeToXML: creates an XML representation of the bubble tree
   * called recursively
   */
  protected void processBubbleTreeToXML(Document doc, Element root, Timeline timeline, BubbleTreeNode node) throws Exception {
    Element currentElement = null;
    currentElement = node.getBubble().toElement(doc, timeline);
    root.appendChild(currentElement);
    Enumeration enumer = node.children();
    if (enumer.hasMoreElements()) {
      while (enumer.hasMoreElements()){
        processBubbleTreeToXML(doc, currentElement, timeline, (BubbleTreeNode)enumer.nextElement());
      }
    }
  }

  /**
   * processBubbleTreeToXMLForHTML: creates an XML representation of the bubble tree
   * called recursively
   */
  protected void processBubbleTreeToXMLForHTML(Document doc, Element root, Timeline timeline, BubbleTreeNode node, int bubNum) throws Exception {
    Element currentElement = null;
    currentElement = node.getBubble().toElementHTML(doc, timeline, bubNum);
    root.appendChild(currentElement);
    bubCount++;
    Enumeration enumer = node.children();
    if (enumer.hasMoreElements()) {
      while (enumer.hasMoreElements()){
        processBubbleTreeToXMLForHTML(doc, currentElement, timeline, (BubbleTreeNode)enumer.nextElement(), bubCount);
      }
    }
  }

  /**
   * processBubbleTreeExcerptToXML: creates an XML representation of the bubble tree
   * called recursively
   */
  protected void processBubbleTreeExcerptToXML(Document doc, Element root, Timeline timeline, BubbleTreeNode node, int startPoint, int endPoint) throws Exception {
    Element currentElement = null;
    currentElement = node.getBubble().toElement(doc, timeline);
    Bubble currBubble = node.getBubble();
    // only add the bubble if it fits within the excerpt range or is the root node
    if (node.isRoot() || currBubble.getStart() >= startPoint && currBubble.getEnd() <= endPoint) {
      root.appendChild(currentElement);
      Enumeration enumer = node.children();
      if (enumer.hasMoreElements()) {
        while (enumer.hasMoreElements()){
          processBubbleTreeExcerptToXML(doc, currentElement, timeline, (BubbleTreeNode)enumer.nextElement(), startPoint, endPoint);
        }
      }
    }
    else {
      Enumeration enumer = node.children();
      if (enumer.hasMoreElements()) {
        while (enumer.hasMoreElements()){
          processBubbleTreeExcerptToXML(doc, root, timeline, (BubbleTreeNode)enumer.nextElement(), startPoint, endPoint);
        }
      }
    }
  }

  /**
   * processXMLToTimeline: creates and returns a timeline object from the XML
   */
  protected Timeline processXMLToTimeline(Element rootElement) throws Exception {

    // validate the XML document

    if (!rootElement.getNodeName().equals("Timeline")) {
      throw new Exception("Error parsing Timeline XML document, root node must be Timeline");
    }

    // timeline properties and elements
    Timeline timeline;
    String timelineTitle;
    String timelineDescription = "";
    int lineStart;
    int lineY;
    int lineLength;
    int bubbleHeight;
    int bubbleType;
    boolean blackAndWhite;
    boolean editable;
    boolean resizable;
    Color panelColor;
    boolean showTimes;
    Color levelColors[] = new Color[numSavedLevels + 1];
    int pointList[] = new int[1000];
    int markerList[] = new int[1000];
    int numBubs = 1;
    int numMarkers = 0;
    Vector<Timepoint> Timepoints = new Vector<Timepoint>(2);
    Vector<Marker> Markers = new Vector<Marker>(0);
    BubbleTreeNode topBubbleNode = null;
    Bubble rootBubble = new Bubble();
    int mediaStart;
    int mediaEnd;
    String mediaContent;
    boolean playWhenClicked;
    boolean stopPlaying;
    boolean autoScalingOn;

    if (pnlTimeline != null && !openingInitialStandalone) { // opened from a pre-existing timeline
      // initialize properties with previous values
      lineStart = pnlTimeline.getDefaultLineStart();
      lineY = pnlTimeline.getDefaultLineY();
      // lineLength = pnlTimeline.getDefaultLineLength();
      bubbleHeight = pnlTimeline.getDefaultBubbleHeight();
      bubbleType = pnlTimeline.getDefaultBubbleType();
      blackAndWhite = false;
      editable = pnlTimeline.getTimeline().isEditable();
      resizable = pnlTimeline.getTimeline().isResizable();
      panelColor = pnlTimeline.getPanelColor();
      showTimes = pnlTimeline.getTimeline().areTimesShown();
      numBubs = pnlTimeline.getTimeline().getNumBaseBubbles();
      numMarkers = pnlTimeline.getTimeline().getNumMarkers();
      if (pnlTimeline.getFrame().isUsingLocalAudio) {
        mediaStart = pnlTimeline.getLocalPlayer().startOffset;
        mediaEnd = pnlTimeline.getLocalPlayer().endOffset;
        mediaContent = pnlTimeline.getLocalPlayer().filename.toString();
        if (mediaContent.endsWith(bogusMedia)) {
          mediaContent = pnlTimeline.getTimeline().getPlayerContent();
        }
      }
      else {
        mediaStart = pnlTimeline.getTimeline().getPlayerStartOffset();
        mediaEnd = pnlTimeline.getTimeline().getPlayerEndOffset();
        mediaContent = pnlTimeline.getTimeline().getPlayerContent();
      }
      playWhenClicked = pnlTimeline.getTimeline().playWhenBubbleClicked;
      stopPlaying = pnlTimeline.getTimeline().stopPlayingAtSelectionEnd;
      autoScalingOn = pnlTimeline.getTimeline().autoScalingOn;
    }

    // read in the timeline attributes from the XML document
    timelineTitle = rootElement.getAttribute("title");
    if (rootElement.hasAttribute("description")){
      timelineDescription = rootElement.getAttribute("description");
    }

    // lineLength = Integer.parseInt(rootElement.getAttribute("length"));
    bubbleHeight = Integer.parseInt(rootElement.getAttribute("bubbleHeight"));
    bubbleType = Integer.parseInt(rootElement.getAttribute("bubbleType"));
    blackAndWhite = (rootElement.getAttribute("BWBubbles").equals("true"));
    editable = (rootElement.getAttribute("editable").equals("true"));
    resizable = (rootElement.getAttribute("resizable").equals("true"));
    StringTokenizer st = new StringTokenizer(rootElement.getAttribute("bgColor"), ",");
    panelColor = new Color(Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()));
    showTimes = (rootElement.getAttribute("visibleTimes").equals("true"));

    // read in media properties
    mediaStart = Integer.parseInt(rootElement.getAttribute("mediaOffset"));
    mediaEnd = mediaStart + Integer.parseInt(rootElement.getAttribute("mediaLength"));
    mediaContent = rootElement.getAttribute("mediaContent");
    playWhenClicked = (rootElement.getAttribute("clickPlay").equals("true"));
    stopPlaying = (rootElement.getAttribute("stopPlay").equals("true"));
    autoScalingOn = (rootElement.getAttribute("autoscale").equals("true"));

    // read in the remaining timeline elements
    NodeList nl = rootElement.getChildNodes();
    for (int i = 0; i < nl.getLength(); i++) {
      String tag = nl.item(i).getNodeName();

      // read in level colors
      if (tag != null) {
        if (tag.equals("LevelColors")) {
          // read in level colors
          NodeList lcl = ((Element)nl.item(i)).getElementsByTagName("LevelColor");
          for (int j = 0; j < numSavedLevels; j++) {
            st = new StringTokenizer(String.valueOf(lcl.item(j).getFirstChild().getNodeValue()), ",");
            levelColors[j + 1] = new Color(Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()));
          }
        }
        // read in timepoint list
        else if (tag.equals("Timepoints")) {
          NodeList tpl = ((Element)nl.item(i)).getElementsByTagName("Timepoint");
          numBubs = tpl.getLength() - 1;
          for (int k = 0; k < tpl.getLength(); k++) {
            pointList[k] = Integer.parseInt(((Element)tpl.item(k)).getAttribute("offset"));
            Timepoint timepoint = new Timepoint();
            Timepoints.addElement(timepoint.getTimepoint());
            timepoint.setLabel(((Element)tpl.item(k)).getAttribute("label"));
            NodeList annList = ((Element)tpl.item(k)).getElementsByTagName("Annotation");
            if (annList.getLength() > 0) {
              Node ann = annList.item(0).getFirstChild();
              if (ann != null) {
                timepoint.setAnnotation(String.valueOf((ann).getNodeValue()));
              }
            }
          }
        }
        // read in marker list
        else if (tag.equals("Markers")) {
          NodeList ml = ((Element)nl.item(i)).getElementsByTagName("Marker");
          numMarkers = ml.getLength();
          for (int k = 0; k < ml.getLength(); k++) {
            markerList[k] = Integer.parseInt(((Element)ml.item(k)).getAttribute("offset"));
            Marker marker = new Marker();
            Markers.addElement(marker.getMarker());
            marker.setLabel(((Element)ml.item(k)).getAttribute("label"));
            NodeList annList = ((Element)ml.item(k)).getElementsByTagName("Annotation");
            if (annList.getLength() > 0) {
              Node ann = annList.item(0).getFirstChild();
              if (ann != null) {
                marker.setAnnotation(String.valueOf((ann).getNodeValue()));
              }
            }
          }
        }
        // read in bubble tree
        else if (tag.equals("Bubble")) {
          topBubbleNode = new BubbleTreeNode((Object)rootBubble);
          Element rootBubbleElement = ((Element)nl.item(i));
          rootBubble.setLevel(Integer.parseInt(rootBubbleElement.getAttribute("level")));
          NodeList bl = rootBubbleElement.getChildNodes();
          for (int m = 0; m < bl.getLength(); m++) {
            String bubTag = bl.item(m).getNodeName();
            if(bubTag != null && bubTag.equals("Bubble")) {
              processXMLToBubbleTree((Element)bl.item(m), topBubbleNode, levelColors);
            }
          }
        }
      }
    }

    File mediaFile;
    if (!restoringTempFile && !revertingToSavedFile) {
      // make a new timeline frame and set its content
      if (!openingStandalone && mediaContent.startsWith("IU")) { // regular timeline
        BasicWindow newWindow = WindowManager.openWindow(WindowManager.WINTYPE_TIMELINE, WindowManager.WINLOCATION_CASCADE_DOWN);
        try {
          Thread.sleep(100);
          newWindow.invalidate();
          } catch (Exception e) {}
          TimelineFrame newTimelineWindow = (TimelineFrame)newWindow;
          newTimelineWindow.isNewTimeline = false;
          newTimelineWindow.setContent(mediaContent, mediaStart, mediaEnd);
          pnlTimeline = newTimelineWindow.getTimelinePanel();
          pnlTimeline.setPanelColor(panelColor);
      }
      else if (mediaContent.startsWith("IU")) { // regular timeline, but opened in standalone
        TimelineFrame newTimelineWindow;
        if (openingInitialStandalone) {
           newTimelineWindow = pnlTimeline.getFrame();
        }
        else {
          BasicWindow newWindow = WindowManager.openWindow(WindowManager.WINTYPE_LOCAL_TIMELINE, WindowManager.WINLOCATION_CASCADE_DOWN);
          try {
            Thread.sleep(100);
            newWindow.invalidate();
            } catch (Exception e) {}
             newTimelineWindow = (TimelineFrame)newWindow;
        }
        newTimelineWindow.setTitle("New Timeline");
        newTimelineWindow.isNewTimeline = false;
        mediaFile = new File(bogusMedia);
        newTimelineWindow.setContent(mediaFile, mediaStart, mediaEnd);
        pnlTimeline = newTimelineWindow.getTimelinePanel();
        JOptionPane.showMessageDialog((JFrame)pnlTimeline.getFrame(),
                "The audio for this timeline is currently unavailable. ", "Audio unavailable",
                JOptionPane.INFORMATION_MESSAGE);
      }
      else { // local audio timeline
        TimelineFrame newTimelineWindow;
        if (openingInitialStandalone) {
           newTimelineWindow = pnlTimeline.getFrame();
        }
        else {
          BasicWindow newWindow = WindowManager.openWindow(WindowManager.WINTYPE_LOCAL_TIMELINE, WindowManager.WINLOCATION_CASCADE_DOWN);
          try {
            Thread.sleep(100);
            newWindow.invalidate();
            } catch (Exception e) {}
             newTimelineWindow = (TimelineFrame)newWindow;
        }
        newTimelineWindow.setTitle("New Timeline");
        newTimelineWindow.isNewTimeline = false;
      if (mediaContent.startsWith("..")) { // relative path
        String absolutePath = TimelineUtilities.absolutePath(pnlTimeline.getSavePath(),mediaContent);
        mediaContent = absolutePath;
      }
        mediaFile = new File(mediaContent);
        if (!mediaFile.exists()) { // there is no media file at the path specified
        	// look for the audio file in the same folder as the timeline first
        	//String relativePath = TimelineUtilities.getRelativePath(new File(mediacontent), new File (mediaContent));
        	//log.debug("relative path: " + relativePath);
        	//log.debug(mediaFile);
        	//log.debug(mediaFile.getName());
        	File currFile = new File(currOpenPath);
        	//log.debug(currOpenPath);
        	//log.debug(currFile.getParent());
        	String altPath = currFile.getParent() + "\\" + mediaFile.getName();
        	File alternatePath = new File(altPath);
        	//log.debug(altPath); 
        	if (alternatePath.exists()) {
        		mediaFile = alternatePath;
        	} else {
        		MissingAudioDialog dlgMissing = new MissingAudioDialog(mediaContent, this, newTimelineWindow);
        		if (newPath != null) {
        			mediaFile = newPath;
        		}
        		else {
        			mediaFile = new File(bogusMedia);
        		}
        	}
        }
        newTimelineWindow.setContent(mediaFile, mediaStart, mediaEnd);
        pnlTimeline = newTimelineWindow.getTimelinePanel();

      }
    }

    g2d = (Graphics2D)pnlTimeline.getGraphics();
    pnlTimeline.getFrame();
	lineLength = pnlTimeline.getFrame().getWidth() - TimelineFrame.SIDE_SPACE;

    // create the new timeline
    timeline = new Timeline(g2d, lineLength, bubbleHeight, bubbleType, blackAndWhite, numBubs, numMarkers, pointList, markerList, Timepoints, Markers, topBubbleNode, pnlTimeline);

    // reset flags
    restoringTempFile = false;
    revertingToSavedFile = false;
    openingFromV2T = false;
    openingStandalone = false;
    openingInitialStandalone = false;

    // set additional timeline properties
    timeline.resetColors();
    pnlTimeline.setPanelColor(panelColor);
    timeline.setDescription(timelineDescription);
    timeline.setEditable(editable);
    timeline.setResizable(resizable);
    timeline.showTimepointTimes(showTimes);
    timeline.setSliderBackground(panelColor);
    timeline.lblThumb.setBackground(panelColor);
    for (int i = 1; i < levelColors.length; i++) {
      timeline.bubbleLevelColors[i] = levelColors[i];
    }
    int scheme = timeline.determineColorScheme();
    timeline.colorScheme = scheme;
    timeline.mediaLength = mediaEnd - mediaStart;
    timeline.autoScalingOn = autoScalingOn;
    timeline.playWhenBubbleClicked = playWhenClicked;
    timeline.stopPlayingAtSelectionEnd = stopPlaying;

    // set as dirty or clean (did it need converted audio?)
    if (pnlTimeline.getFrame().isNewAudio) {
    	timeline.makeDirty();
    } else {
        timeline.makeClean();
    }
    
    // set up menu items
    if (System.getProperty("os.name").startsWith("Mac OS")) {
      pnlTimeline.menuiShowTimesMac.setSelected(showTimes);
      pnlTimeline.menubTimeline.menuiShowTimesMac.setSelected(showTimes);
      pnlTimeline.menubTimeline.menuiBlackAndWhiteMac.setSelected(blackAndWhite);
      pnlTimeline.menubTimeline.menuiRoundBubblesMac.setSelected(bubbleType == 0);
      pnlTimeline.menubTimeline.menuiSquareBubblesMac.setSelected(bubbleType == 1);
    } else {
      pnlTimeline.menuiShowTimes.setSelected(showTimes);
      pnlTimeline.menubTimeline.menuiShowTimes.setSelected(showTimes);
      pnlTimeline.menubTimeline.menuiBlackAndWhite.setSelected(blackAndWhite);
      pnlTimeline.menubTimeline.menuiRoundBubbles.setSelected(bubbleType == 0);
      pnlTimeline.menubTimeline.menuiSquareBubbles.setSelected(bubbleType == 1);
    }

    // set timeline frame title and pass the timeline to the timeline panel
    pnlTimeline.getFrame().setTitle(timelineTitle);
    pnlTimeline.setTimeline(timeline);

    // initially show the timeline description
    pnlTimeline.getFrame().getControlPanel().updateAnnotationPane();
    pnlTimeline.getFrame().getControlPanel().showDescription();

    // pass back the timeline
    return timeline;
  }

  /**
   * processXMLToBubbleTree: creates a bubble tree from the XML representation
   * called recursively
   */
  protected void processXMLToBubbleTree(Element currElement, BubbleTreeNode parentNode, Color levColors[]) throws Exception {
    // create bubble
    Bubble currBubble = new Bubble();

    // read in bubble attributes
    currBubble.setLevel(Integer.parseInt(currElement.getAttribute("level")));
    if (currElement.hasAttribute("color")) {
      StringTokenizer st = new StringTokenizer(currElement.getAttribute("color"), ",");
      currBubble.setColor(new Color(Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken())));
      if (!currBubble.getColor().equals(levColors[currBubble.getLevel()])) {
        currBubble.hasCustomColor = true;
      }
    }
    else {
      currBubble.setColor(levColors[currBubble.getLevel()]);
    }
    currBubble.setLabel(currElement.getAttribute("label"));
    if (currElement.hasAttribute("levelSetByUser")) {
      currBubble.levelWasUserAdjusted = true;
    }
    else {
      currBubble.levelWasUserAdjusted = false;
    }

    BubbleTreeNode currBubbleNode = new BubbleTreeNode(currBubble);
    parentNode.add(currBubbleNode);
    NodeList nl = currElement.getChildNodes();
    for (int i = 0; i < nl.getLength(); i++) {
      String bubTag = nl.item(i).getNodeName();
      if (bubTag != null) {
        if (bubTag.equals("Annotation")) {
          Node ann = nl.item(i).getFirstChild();
          if (ann != null) {
            currBubble.setAnnotation(String.valueOf((ann).getNodeValue()));
          }
        }
        else if (bubTag.equals("Bubble")) {
          processXMLToBubbleTree((Element)nl.item(i), currBubbleNode, levColors);
        }
      }
    }
  }

  /**
   * addTimelineAnnotation: adds comments to the root node <Timeline> of the XML
   * about by who and when it was exported
   */
  protected void addTimelineAnnotation(Document doc, String username) throws Exception{
    Element rootElement = doc.getDocumentElement();
    if (!rootElement.getNodeName().equals("Timeline"))
      throw new Exception("Error parsing Timeline XML document, root node must be Timeline");
    org.w3c.dom.Element annotationElement = doc.createElement("Annotation");
    StringBuffer msg = new StringBuffer();
      msg.append("Exported on ");
    msg.append(new java.util.Date());
    annotationElement.appendChild(doc.createTextNode(msg.toString()));
    rootElement.appendChild(annotationElement);
  }

  /**
   * addGif: adds a gif reference to the xml file--needed only for HTML transform
   */
  protected void addGif(Document doc, String gif) {
    Element rootElement = doc.getDocumentElement();
    org.w3c.dom.Element gifElement = doc.createElement("GifName");
    gifElement.appendChild(doc.createTextNode(gif));
    Node refNode = rootElement.getFirstChild();
    rootElement.insertBefore(gifElement, refNode);

  }
}