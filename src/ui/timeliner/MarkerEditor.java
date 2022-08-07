package ui.timeliner;

import javax.swing.*;
import javax.swing.undo.*;
import java.awt.*;
import javax.swing.border.*;
//import com.borland.jbcl.layout.*;
import java.awt.event.*;
import java.io.StringWriter;

import javax.swing.event.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.EditorKit;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.html.HTMLEditorKit;

import java.util.*;

import util.logging.*;
import ui.common.*;

import org.apache.log4j.Logger;

/**
 * MarkerEditor
 */

public class MarkerEditor extends JDialog {

  private static final long serialVersionUID = 1L;
// external components
  private  TimelinePanel pnlTimeline;
  private  TimelineFrame frmTimeline;
  private  Timeline timeline;
  private  TimelineMenuBar menubTimeline;
  //private  Logger log = Logger.getLogger(MarkerEditor.class);
  protected  UILogger uilogger;

  // visual components
  private  JLabel lblLabel = new JLabel("Label: ");
  private  TitledBorder bordAnnotation = new TitledBorder(" Annotation ");
  private  JTextField fldMarkerLabel;
  private  JTextPane tpAnnotation = new JTextPane();
  private  JScrollPane scrpAnnotation = new JScrollPane(tpAnnotation);
  protected JButton btnRight = new JButton();
  protected JButton btnLeft = new JButton();
  protected JLabel lblLeft = new JLabel("Left");
  protected JLabel lblRight = new JLabel("Right");
  protected JButton btnOk = new JButton("OK");
  protected JButton btnCancel = new JButton("Cancel");
  protected JButton btnApply = new JButton("Apply");

  // text editing
  protected AbstractDocument doc; 
  EditorKit kit = tpAnnotation.getEditorKit();
  HTMLEditorKit htmlKit = new HTMLEditorKit();
  StringWriter output = new StringWriter();
  
  // variables
  //private  String oldText;
  //private  String oldAnnotation;
  private  java.awt.Font timelineFont;
  private  java.awt.Font unicodeFont;
  protected int currMarker;
  protected int buttonWidth;
  protected boolean recentApplyMade = false;

  // layout
  protected JPanel pnlNavigate = new JPanel();
  protected JPanel pnlLabel = new JPanel();
  protected JPanel pnlAnnotation = new JPanel();
  protected JPanel pnlButtons = new JPanel();

  // icons
  final  ImageIcon icoLeft = UIUtilities.icoLeftSmall;
  final  ImageIcon icoRight = UIUtilities.icoRightSmall;

  // vectors for temporary storage
  protected Vector<Integer> editedMarkers = new Vector<Integer>();
  protected Vector<String> potentialLabels = new Vector<String>();
  protected Vector<String> potentialAnnotations = new Vector<String>();
  protected Vector<String> oldLabels = new Vector<String>();
  protected Vector<String> oldAnnotations = new Vector<String>();

  /**
   * MarkerEditor: constructor
   */
  public MarkerEditor(TimelineFrame tf) {
    super(tf);
    frmTimeline = tf;
    pnlTimeline = frmTimeline.getTimelinePanel();
    timeline = pnlTimeline.getTimeline();
    uilogger = frmTimeline.getUILogger();
    menubTimeline = pnlTimeline.getMenuBar();

    // set up dialog
    int dialogWidth;
    int dialogHeight;
    if (System.getProperty("os.name").startsWith("Mac OS")) {
      timelineFont = UIUtilities.fontDialogMacSmaller;
      unicodeFont = UIUtilities.fontUnicodeBigger;
      dialogWidth = 500;
      dialogHeight = 350;
      menubTimeline.disableMenuKeyboardShortcuts();
      buttonWidth = 65;
    } else {
      timelineFont = UIUtilities.fontDialogWin;
      unicodeFont = UIUtilities.fontUnicodeBigger;
      dialogWidth = 450;
      dialogHeight = 350;
      buttonWidth = 60;
    }
    this.setTitle("Edit Markers");
    this.setLocation((frmTimeline.getWidth()/2)-(this.getWidth()/2), (frmTimeline.getHeight()/2)-(this.getHeight()/2));
    this.setModal(true);
    this.setSize(new Dimension(dialogWidth, dialogHeight));
    GridBagLayout gridbag2 = new GridBagLayout();
    Container pane = this.getContentPane();
    pane.setLayout(gridbag2);

    // text fields and text panes
    fldMarkerLabel = new JTextField();
    fldMarkerLabel.setFont(unicodeFont);
    fldMarkerLabel.setBounds(new Rectangle(250, 20));
    fldMarkerLabel.setMinimumSize(new Dimension(250, 25));
    fldMarkerLabel.setPreferredSize(new Dimension(250, 25));
    fldMarkerLabel.setToolTipText("Edit the marker label");
    Font annotationFont = new Font("Arial Unicode MS", 0, 24);
    tpAnnotation.setFont(annotationFont);
    tpAnnotation.setContentType("text/html");
    tpAnnotation.setPreferredSize(new Dimension(430, 250));//375));
    tpAnnotation.setToolTipText("Edit the marker annotation");

    tpAnnotation.setMargin(new Insets(5,5,5,5));
    StyledDocument styledDoc = tpAnnotation.getStyledDocument();
    if (styledDoc instanceof AbstractDocument) {
        doc = (AbstractDocument)styledDoc;
    } else {
     }
    
    // menu bar
    JMenu styleMenu = createStyleMenu();
    //JMenu sizeMenu = createSizeMenu();
    JMenu colorMenu = createColorMenu();
    JMenuBar mb = new JMenuBar();
    mb.add(styleMenu);
    //mb.add(sizeMenu);
    mb.add(colorMenu);
    setJMenuBar(mb);


    // buttons
//    btnLeft.setPreferredSize(new Dimension(buttonWidth + 10, 30));
    btnLeft.setToolTipText("Go to the previous marker");
    btnLeft.setIcon(icoLeft);
    btnLeft.setText("Previous");
    btnLeft.setFont(timelineFont);
    btnLeft.setMargin(new Insets(0, 0, 0, 0));
    btnLeft.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        saveLabelAndAnnotation();
        if (currMarker > 0) {
          currMarker = currMarker - 1;
        }
        updateLabelAndAnnotation();
        updateNavigationButtons();
        uilogger.log(UIEventType.BUTTON_CLICKED, "navigate to previous marker");
      }
    });

//    btnRight.setPreferredSize(new Dimension(buttonWidth + 10, 30));
    btnRight.setToolTipText("Go to the next marker");
    btnRight.setText("Next");
    btnRight.setIcon(icoRight);
    btnRight.setFont(timelineFont);
    btnRight.setMargin(new Insets(0, 0, 0, 0));
    btnRight.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        saveLabelAndAnnotation();
        if (currMarker < timeline.getNumMarkers()) {
          currMarker = currMarker + 1;
        }
        updateLabelAndAnnotation();
        updateNavigationButtons();
        uilogger.log(UIEventType.BUTTON_CLICKED, "navigate to next marker");
      }
    });

    btnOk.setFont(timelineFont);
    btnOk.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        saveLabelAndAnnotation();

        // back up old values for undo and set new values
        undoPreviousApplys();
        for (int i = 0; i < editedMarkers.size(); i++) {
          int currNum = ((Integer)editedMarkers.elementAt(i)).intValue();
          Marker currMarker = timeline.getMarker(currNum);
          oldLabels.addElement(currMarker.getLabel());
          oldAnnotations.addElement(currMarker.getAnnotation());
          currMarker.setLabel((String)potentialLabels.elementAt(i));
          currMarker.setAnnotation((String)potentialAnnotations.elementAt(i));
        }

        timeline.makeDirty();
        closeWindow();
        frmTimeline.getControlPanel().updateAnnotationPane();
        pnlTimeline.undoManager.undoableEditHappened(new UndoableEditEvent(pnlTimeline,
            new UndoableEditMarker(oldLabels, potentialLabels, oldAnnotations, potentialAnnotations,
            editedMarkers, timeline)));
        pnlTimeline.updateUndoMenu();
        uilogger.log(UIEventType.BUTTON_CLICKED, "accept marker edits");
      }
    });

    btnApply.setFont(timelineFont);
    btnApply.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        saveLabelAndAnnotation();

        // back up old values for undo and set new values
        undoPreviousApplys();
        recentApplyMade = true;
        for (int i = 0; i < editedMarkers.size(); i++) {
          int currNum = ((Integer)editedMarkers.elementAt(i)).intValue();
          Marker currMarker = timeline.getMarker(currNum);
          oldLabels.addElement(currMarker.getLabel());
          oldAnnotations.addElement(currMarker.getAnnotation());
          currMarker.setLabel((String)potentialLabels.elementAt(i));
          currMarker.setAnnotation((String)potentialAnnotations.elementAt(i));
        }

        timeline.makeDirty();
        pnlTimeline.refreshTimeline();
        frmTimeline.getControlPanel().updateAnnotationPane();
        pnlTimeline.undoManager.undoableEditHappened(new UndoableEditEvent(pnlTimeline,
            new UndoableEditMarker(oldLabels, potentialLabels, oldAnnotations, potentialAnnotations,
            editedMarkers, timeline)));
        pnlTimeline.updateUndoMenu();
        uilogger.log(UIEventType.BUTTON_CLICKED, "apply marker edits");
      }
    });

    btnCancel.setFont(timelineFont);
    btnCancel.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        closeWindow();
        uilogger.log(UIEventType.BUTTON_CLICKED, "cancel bubble edits");
      }
    });

    // panels
    pnlLabel.setLayout(new FlowLayout(FlowLayout.LEFT));
    pnlAnnotation.setLayout(new BorderLayout());
    pnlAnnotation.setBorder(bordAnnotation);
    pnlNavigate.setLayout(new FlowLayout(FlowLayout.LEFT));
    pnlButtons.setLayout(new FlowLayout(FlowLayout.RIGHT));
    if (System.getProperty("os.name").startsWith("Mac OS")) {
      pnlNavigate.setPreferredSize(new Dimension((dialogWidth / 2) - 50, 40));
      pnlNavigate.setMinimumSize(new Dimension((dialogWidth / 2) - 50, 40));
      pnlButtons.setPreferredSize(new Dimension((dialogWidth / 2) + 50, 40));
      pnlButtons.setMinimumSize(new Dimension((dialogWidth / 2) + 50, 40));
    }
    else {
      pnlNavigate.setPreferredSize(new Dimension((dialogWidth / 2) - 10, 40));
      pnlNavigate.setMinimumSize(new Dimension((dialogWidth / 2) - 10, 40));
      pnlButtons.setPreferredSize(new Dimension((dialogWidth / 2) + 10, 40));
      pnlButtons.setMinimumSize(new Dimension((dialogWidth / 2) + 10, 40));
    }

    // labels
    lblLabel.setFont(timelineFont);

    // borders
    bordAnnotation.setTitleFont(timelineFont);

    // layout
    pnlLabel.add(lblLabel);
    pnlLabel.add(fldMarkerLabel);
    pnlAnnotation.add(scrpAnnotation, BorderLayout.CENTER);
    pnlNavigate.add(btnLeft);
    pnlNavigate.add(btnRight);
    pnlButtons.add(btnOk);
    pnlButtons.add(btnCancel);
    pnlButtons.add(btnApply);

    TimelineUtilities.createConstraints(pane, pnlLabel, 0, 0, 2, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, 2, 2, 2, 2, 0, 0);
    TimelineUtilities.createConstraints(pane, pnlAnnotation, 0, 1, 2, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, 2, 2, 2, 2, 0, 0);
    TimelineUtilities.createConstraints(pane, pnlNavigate, 0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.SOUTHWEST, GridBagConstraints.NONE, 2, 2, 2, 2, 0, 0);
    TimelineUtilities.createConstraints(pane, pnlButtons, 1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE, 2, 2, 2, 2, 0, 0);

    // set up initial label and annotation, update navigation buttons
    editedMarkers.removeAllElements();
    currMarker = timeline.getLastMarkerClicked();
    updateLabelAndAnnotation();
    updateNavigationButtons();

    // show dialog
    this.pack();
    this.setVisible(true);
  }

  /**
   * closeWindow: performs clean up code
   */
  private void closeWindow() {
    if (System.getProperty("os.name").startsWith("Mac OS")) { // to fix a 1.3 menu key bug
      menubTimeline.enableMenuKeyboardShortcuts();
    }
    timeline.deselectAllTimepointsAndMarkers();
    this.setVisible(false);
  }

  /**
   * saveLabelAndAnnotation: saves current label and annotation in vectors for later application
   */
  private void saveLabelAndAnnotation() {
    // get annotation and label for currently selected bubble
    int prevSave = editedMarkers.indexOf(Integer.valueOf(currMarker));
    if (prevSave == -1) { // this bubble has not been saved before
      editedMarkers.addElement(Integer.valueOf(currMarker));
      potentialLabels.addElement(fldMarkerLabel.getText());
      //potentialAnnotations.addElement(tpAnnotation.getText());
      try {     
    	  output.getBuffer().setLength(0);
       	  htmlKit.write( output, doc, 0, doc.getLength());
       	  String html = output.toString();
    	  html = UIUtilities.htmlCleanup(html);
    	  potentialAnnotations.addElement(html);
    	  //log.debug("html = " + html);

      } catch (Exception e) {
          System.err.println("Error saving annotation");
      }

    }
    else {
      editedMarkers.setElementAt(Integer.valueOf(currMarker), prevSave);
      potentialLabels.setElementAt(fldMarkerLabel.getText(), prevSave);
      //potentialAnnotations.setElementAt(tpAnnotation.getText(), prevSave);
      try {      
    	  output.getBuffer().setLength(0);
    	  htmlKit.write( output, doc, 0, doc.getLength());
    	  String html = "";
    	  html = output.toString();
    	  html = UIUtilities.htmlCleanup(html);
    	  potentialAnnotations.setElementAt(html, prevSave);
   	      // log.debug("html = " + html);
    	  
      } catch (Exception e) {
          System.err.println("Error saving annotation");
          
      }

    }
  }

  /**
   * updateLabelAndAnnotation: updates the displayed label and annotation
   */
  private void updateLabelAndAnnotation() {
	  
	int annotationFontSize = UIUtilities.convertFontSize(18);
    Marker currentMarker = timeline.getMarker(currMarker);
    int prevPos = editedMarkers.indexOf(Integer.valueOf(currMarker));
    if (prevPos != -1) { // this marker has already been edited
      fldMarkerLabel.setText((String)potentialLabels.elementAt(prevPos));
      tpAnnotation.setText("<html><body><span style='margin-bottom:0em; font-size: " + annotationFontSize + "pt; font-family: " + unicodeFont + "'>" + (String)potentialAnnotations.elementAt(prevPos) + "</span></body></html>");
    }
    else { // it has not been edited
      fldMarkerLabel.setText(currentMarker.getLabel());
      tpAnnotation.setText("<html><body><span style='margin-bottom:0em; font-size: " + annotationFontSize + "pt; font-family: " + unicodeFont + "'>" + currentMarker.getAnnotation() + "&nbsp;</span></body></html>");
    }
    if (!currentMarker.isSelected()) {
      timeline.selectMarker(currMarker);
    }
    pnlTimeline.refreshTimeline();
  }

  /**
   * updateNavigationButtons: updates the navigation buttons
   */
  private void updateNavigationButtons() {
    btnLeft.setEnabled(currMarker > 0);
    btnRight.setEnabled(currMarker < timeline.getNumMarkers()-1);
  }

  /**
   * undoPreviousApplys: undoes any previous "Apply"s
   * (only allow a single undo for all of the property changes)
   */
  private void undoPreviousApplys() {
    if (recentApplyMade && pnlTimeline.undoManager.canUndo()) {
      if (pnlTimeline.undoManager.getUndoPresentationName().equals("Undo Edit Marker") ||
          pnlTimeline.undoManager.getUndoPresentationName().equals("Undo Edit Markers")) {
        try {
          pnlTimeline.undoManager.undo();
        }
        catch (CannotUndoException cue) {
          cue.printStackTrace();
        }
        pnlTimeline.updateUndoMenu();
      }
    }
    recentApplyMade = false;
  }
  
  // text editing functions
  
  protected JMenu createSizeMenu() {
      JMenu menu = new JMenu("Size");

      menu.add(new StyledEditorKit.FontSizeAction("12", 12));
      menu.add(new StyledEditorKit.FontSizeAction("14", 14));
      menu.add(new StyledEditorKit.FontSizeAction("18", 18));
      menu.add(new StyledEditorKit.FontSizeAction("24", 24));

      return menu;
  }
  
  protected JMenu createStyleMenu() {
      JMenu menu = new JMenu("Style");

      Action action = new StyledEditorKit.BoldAction();
      action.putValue(Action.NAME, "Bold");
      menu.add(action);
      menu.getItem(0).setIcon(UIUtilities.icoBold);

      action = new StyledEditorKit.ItalicAction();
      action.putValue(Action.NAME, "Italic");
      menu.add(action);
      menu.getItem(1).setIcon(UIUtilities.icoItalic);

      action = new StyledEditorKit.UnderlineAction();
      action.putValue(Action.NAME, "Underline");
      menu.add(action);
      menu.getItem(2).setIcon(UIUtilities.icoUnderline);

      menu.addSeparator();
      
      JMenuItem menuiReturn = new JMenuItem();
      menu.add(menuiReturn);
      menuiReturn.setText("Hard Return");
      menuiReturn.addActionListener(new java.awt.event.ActionListener() {
          public void actionPerformed(ActionEvent e) {
             try {
             doc.insertString(tpAnnotation.getCaretPosition(), "\r\n", null);
             } catch (Exception ex) {}
          }
      });

      //menu.add(new StyledEditorKit.FontFamilyAction("Serif", "Serif"));
      //menu.add(new StyledEditorKit.FontFamilyAction("SansSerif", "SansSerif"));

      if (System.getProperty("os.name").startsWith("Mac OS")) {
          //Mac specific stuff
          menu.getItem(0).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, InputEvent.META_DOWN_MASK));
          menu.getItem(1).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.META_DOWN_MASK));
          menu.getItem(2).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.META_DOWN_MASK));
          menu.getItem(4).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.META_DOWN_MASK));
      } else {
          //Windows specific stuff
          menu.getItem(0).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, InputEvent.CTRL_DOWN_MASK));
          menu.getItem(1).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_DOWN_MASK));
          menu.getItem(2).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.CTRL_DOWN_MASK));
          menu.getItem(4).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.SHIFT_DOWN_MASK));
          menu.setMnemonic('f');
          menu.getItem(0).setMnemonic('b');
          menu.getItem(1).setMnemonic('i');
          menu.getItem(2).setMnemonic('u');
          menu.getItem(4).setMnemonic('r');


      }

      return menu;
  }

  protected JMenu createColorMenu() {
      JMenu menu = new JMenu("Color");

      menu.add(new StyledEditorKit.ForegroundAction("Red", Color.red));
      menu.getItem(0).setIcon(UIUtilities.icoRed);
      
      menu.add(new StyledEditorKit.ForegroundAction("Green", Color.green));
      menu.getItem(1).setIcon(UIUtilities.icoGreen);
      
      menu.add(new StyledEditorKit.ForegroundAction("Blue", Color.blue));
      menu.getItem(2).setIcon(UIUtilities.icoBlue);
      
      menu.add(new StyledEditorKit.ForegroundAction("Yellow", Color.yellow));
      menu.getItem(3).setIcon(UIUtilities.icoYellow);

       menu.add(new StyledEditorKit.ForegroundAction("Orange", Color.orange));
       menu.getItem(4).setIcon(UIUtilities.icoOrange);

       menu.add(new StyledEditorKit.ForegroundAction("Pink", Color.pink));
       menu.getItem(5).setIcon(UIUtilities.icoPink);

       menu.add(new StyledEditorKit.ForegroundAction("Cyan", Color.cyan));
       menu.getItem(6).setIcon(UIUtilities.icoCyan);

       menu.add(new StyledEditorKit.ForegroundAction("Magenta", Color.magenta));
       menu.getItem(7).setIcon(UIUtilities.icoMagenta);

       menu.add(new StyledEditorKit.ForegroundAction("Gray", Color.darkGray));
       menu.getItem(8).setIcon(UIUtilities.icoGray);

     menu.add(new StyledEditorKit.ForegroundAction("Black", Color.black));
       menu.getItem(9).setIcon(UIUtilities.icoBlack);

      return menu;
  }

}