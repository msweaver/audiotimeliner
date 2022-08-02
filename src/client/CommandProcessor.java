package client;

import java.io.File;
import java.io.IOException;

import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import ui.timeliner.TimelineXMLAdapter;
//import util.XMLToolkit;
import org.apache.log4j.Logger;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

/**
 * Processes commands from external sources (usually V2X files).
 */

public class CommandProcessor {
    private static Logger log = Logger.getLogger(CommandProcessor.class);

    private DocumentBuilder doc_builder = null;
    private boolean windowOpened = false;

    public CommandProcessor() throws V2XProcessingException{
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            doc_builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new V2XProcessingException("Can't Initialize Command Processor", e);
        }
    }

    public void processCommandFile(String filename) throws V2XProcessingException{
        windowOpened = false;

        if (filename == null) {
            throw new V2XProcessingException("Command file not specified");
        }

        Document doc = null;
        try {
            doc = doc_builder.parse(new File(filename));
        } catch (IOException e) {
            throw new V2XProcessingException("Cannot open " + filename, e);
        } catch (SAXException e) {
            throw new V2XProcessingException("Parsing problem", e);
        }

        Element rootElement = doc.getDocumentElement();
        if (rootElement.getNodeName().equalsIgnoreCase("Timeline")) { 
            openTimeline(filename, doc.getDocumentElement(), true);
            windowOpened = true;
        }
        else {
          if (!(rootElement.getNodeName().equals("V2X") &&
                rootElement.getAttribute("version").equals("2.0"))) {
            throw new V2XProcessingException(
                    "Invalid Commmand file -- must be a timeline file (TIM) or V2X file, version 2.0.\n" +
                    "The file found was of type " +
                    rootElement.getNodeName() + " " +
                    rootElement.getAttribute("version") + "\n"+
                    "You may need to upgrade your software to use this file.");
          }

         // since there is no such thing as a Search window or Timeline bookmark,
          // these commands must be processed separately
          NodeList nl = rootElement.getElementsByTagName("Search");
          nl = rootElement.getElementsByTagName("Timeline");
          if (nl.getLength() > 0) {
              for(int i=0; i < nl.getLength(); i++) {
                  openTimeline(null, (Element) nl.item(i).cloneNode(true), false);
                  windowOpened = true;
              }
          }
        }

        // if we haven't opened any windows, there must be a problem
        if (!windowOpened) {
            throw new V2XProcessingException("Command file " + filename +
                    " did not contain any valid commands.");
        }
    }

    private void openTimeline(String filename, Element root, boolean isV2T) {
        try {
            TimelineXMLAdapter txmlAdapter = new TimelineXMLAdapter();
            if (isV2T) {
              txmlAdapter.openFromV2T(root, filename);
            }
            else {
              txmlAdapter.importTimeline(root, filename);
            }
          }
          catch (Exception e) { 
            JOptionPane.showMessageDialog(null, "Error opening timeline file.", "Open error",
              JOptionPane.ERROR_MESSAGE);
            log.error("cannot open timeline file", e);
           }
    }

}