package ui.timeliner;

import javax.swing.*;
import javax.swing.plaf.basic.*;
import javax.swing.undo.*;
import java.awt.*;
import javax.swing.border.*;
import com.borland.jbcl.layout.*;
import java.awt.event.*;
import javax.swing.event.*;
import java.util.*;
import util.logging.*;
import ui.common.*;
//import org.apache.log4j.Logger;

/**
 * TimelineProperties.java
 */

public class TimelineProperties extends JDialog {

  private static final long serialVersionUID = 1L;
// external components
  private TimelinePanel pnlTimeline;
  private Timeline timeline;
  private TimelineMenuBar menubTimeline;
  //private static Logger log = Logger.getLogger(TimelineProperties.class);
  protected UILogger uilogger;

  // fonts
  private java.awt.Font timelineFont;
  private java.awt.Font unicodeFont;

  // titled borders
  private TitledBorder bordTimelineIs = new TitledBorder(" Timeline Is... ");
  private TitledBorder bordTimelineAppearance = new TitledBorder(" Timeline Appearance ");
  private TitledBorder bordBubbleShape = new TitledBorder(" Bubble Shape ");
  private TitledBorder bordTimelineSize = new TitledBorder(" Bubble Height ");
  private TitledBorder bordTimelineColors = new TitledBorder(" Bubble Level Colors ");
  private TitledBorder bordAudioSettings = new TitledBorder(" Audio Settings ");
  private TitledBorder bordDescription = new TitledBorder(" Description ");

  // icon
  private ImageIcon icoDescription;

  // panels
  private JPanel pnlMain = new JPanel();
  private JPanel pnlSettings = new JPanel();
  private JPanel pnlButtons = new JPanel();
  private JPanel pnlTitle = new JPanel();
  private JPanel pnlDescription = new JPanel();
  private JPanel pnlTextFields = new JPanel();
  private JPanel pnlLeft = new JPanel();
  private JPanel pnlRight = new JPanel();
  private JPanel pnlTimelineIs = new JPanel();
  private JPanel pnlTimelineAppearance = new JPanel();
  private JPanel pnlBubbleShape = new JPanel();
  private JPanel pnlTimelineSize = new JPanel();
  private JPanel pnlAudioSettings = new JPanel();
  private JPanel pnlLevelColors = new JPanel();
  private JPanel pnlColorSchemes = new JPanel();
  private JPanel pnlColorButtons = new JPanel();
  private int currColorScheme = 0;

  // labels
  protected JLabel lblTimelineTitle = new JLabel("Title: ");
  protected JLabel lblTimelineDescription = new JLabel("Description: ");
  protected JLabel lblColorScheme = new JLabel("Color Scheme: ");
  protected JLabel lblDescription = new JLabel();

  // text fields and scroll pane
  protected JTextField fldTimelineTitle = new JTextField();
  protected JTextPane fldTimelineDescription = new JTextPane();
  protected JScrollPane scrpDescription = new JScrollPane(fldTimelineDescription);

  // variables
  protected String oldTimelineTitle;
  protected String oldTimelineDescription;
  protected boolean firstApplyMade = false;

  // combo box
  protected JComboBox<String> combColorSchemes = new JComboBox<String>();

  // check boxes
  protected JCheckBox chkEditable = new JCheckBox("Editable");
  protected JCheckBox chkResizable = new JCheckBox("Resizable");
  protected JCheckBox chkMovable = new JCheckBox("Movable");
  protected JCheckBox chkShowTimes = new JCheckBox("Show Times");
  protected JCheckBox chkBlackAndWhite = new JCheckBox("Black and White");
  protected JCheckBox chkAutoScale = new JCheckBox("Auto-scale height on resize");
  protected JCheckBox chkPlayWhenClicked = new JCheckBox("Start playing when bubble is clicked");
  protected JCheckBox chkStopPlaying = new JCheckBox("Stop playing at end of selection");

  // radio buttons
  protected JRadioButton radRoundBubbles = new JRadioButton("Round Bubbles");
  protected JRadioButton radSquareBubbles = new JRadioButton("Square Bubbles");
  protected ButtonGroup grpBubbleShape = new ButtonGroup();

  // slider
  protected JSlider sldBubbleHeight = new JSlider(0, 0, 10, 5);

  // color buttons
  protected boolean[] levelColorChanged = {false, false, false, false, false, false, false, false, false, false, false};
  protected JButton[] levelButton = {new JButton(), new JButton(), new JButton(), new JButton(), new JButton(), new JButton(), new JButton(), new JButton(), new JButton(), new JButton(), new JButton()};
  protected Color customColors[] = new Color[11];

  // buttons
  private JButton btnOk = new JButton("OK");
  private JButton btnCancel = new JButton("Cancel");
  private JButton btnApply = new JButton("Apply");
  private JButton btnRestore = new JButton("Restore Defaults");
  private JButton btnBackgroundColor = new JButton("Background Color");

  /**
   * constructor
   */
  public TimelineProperties(TimelineFrame frmOwner, TimelinePanel tp, boolean modal) {
    super(frmOwner);
    pnlTimeline = tp;
    timeline = pnlTimeline.getTimeline();
    menubTimeline = tp.getMenuBar();
    uilogger = frmOwner.getUILogger();

    // display variables
    int titleWidth;
    int descriptionWidth;
    int descriptionHeight = UIUtilities.scalePixels(150); // 120
    int dialogHeight;
    int dialogWidth;
    int levelButtonWidth;
    int levelButtonHeight;

    if (System.getProperty("os.name").startsWith("Mac OS")) {
      timelineFont = UIUtilities.fontDialogMacSmallest;
      unicodeFont = UIUtilities.fontUnicodeSmaller;
      titleWidth = 45;
      descriptionWidth = UIUtilities.scalePixels(530);
      dialogWidth = UIUtilities.scalePixels(570);
      dialogHeight = UIUtilities.scalePixels(570);
      levelButtonWidth = 39;
      levelButtonHeight = UIUtilities.scalePixels(20);;
      menubTimeline.disableMenuKeyboardShortcuts();
    } else {
      timelineFont = UIUtilities.fontDialogWin;
      unicodeFont = UIUtilities.fontUnicode;
      titleWidth = 42;
      descriptionWidth = UIUtilities.scalePixels(395);
      dialogWidth = UIUtilities.scalePixels(430);
      dialogHeight = UIUtilities.scalePixels(610);
      levelButtonWidth = 20;
      levelButtonHeight = UIUtilities.scalePixels(20);
    }

    this.setTitle("Timeline Properties: " + pnlTimeline.getFrame().getTitle()); //.substring(10));
    this.setLocationRelativeTo(frmOwner);
    this.setModal(modal);
    this.setSize(new Dimension(dialogWidth, dialogHeight)); // 435, 390
    this.setLocation(30, 30); // frmOwner.getWidth() - (this.getWidth() + 5), 30);
    this.setResizable(false);

    // panel layout
    Container contentPane = this.getContentPane();
    contentPane.setLayout(new VerticalFlowLayout());
    contentPane.add(pnlMain);
    contentPane.add(pnlButtons);
    pnlMain.add(pnlSettings);
    pnlMain.setLayout(new VerticalFlowLayout());
    pnlMain.setBorder(BorderFactory.createEtchedBorder());
    pnlButtons.setLayout(new FlowLayout(FlowLayout.RIGHT));
    pnlButtons.setBorder(BorderFactory.createEtchedBorder());
    pnlSettings.setLayout(new BorderLayout());
    pnlSettings.add(pnlTextFields, BorderLayout.NORTH);
    pnlSettings.add(pnlLeft, BorderLayout.WEST);
    pnlSettings.add(pnlRight, BorderLayout.EAST);
    pnlTextFields.add(pnlTitle);
    pnlTextFields.add(pnlDescription);
    pnlTextFields.setLayout(new VerticalFlowLayout(VerticalFlowLayout.TOP));
    pnlTitle.setLayout(new FlowLayout(FlowLayout.LEFT));
    pnlDescription.setLayout(new FlowLayout());
    pnlLeft.setLayout(new VerticalFlowLayout(VerticalFlowLayout.TOP));
    pnlLeft.add(pnlTimelineIs);
    pnlLeft.add(pnlTimelineAppearance);
    pnlRight.setLayout(new VerticalFlowLayout(VerticalFlowLayout.TOP));
    pnlRight.add(pnlTimelineSize);
    pnlRight.add(pnlLevelColors);
    pnlRight.add(pnlAudioSettings);

    // Timeline title and description fields
    lblTimelineTitle.setFont(timelineFont);
    pnlTitle.add(lblTimelineTitle);
    pnlTitle.add(fldTimelineTitle);
    icoDescription = UIUtilities.icoInfoImage;
    lblDescription.setIcon(icoDescription);
    lblDescription.setPreferredSize(new Dimension(icoDescription.getIconWidth(), icoDescription.getIconHeight()));
    pnlDescription.add(lblDescription);
    pnlDescription.add(scrpDescription);
    fldTimelineTitle.setFont(unicodeFont);
    fldTimelineTitle.setToolTipText("Edit the timeline title");
    fldTimelineTitle.setColumns(titleWidth);
    oldTimelineTitle = pnlTimeline.getFrame().getTitle(); //.substring(10);
    fldTimelineTitle.setText(oldTimelineTitle);
    fldTimelineDescription.setFont(unicodeFont);
    fldTimelineDescription.setToolTipText("Edit the timeline description");
    pnlDescription.setPreferredSize(new Dimension(descriptionWidth, descriptionHeight));
    pnlDescription.setMinimumSize(new Dimension(descriptionWidth, descriptionHeight));
    scrpDescription.setPreferredSize(new Dimension(descriptionWidth - 65, descriptionHeight-45));
    scrpDescription.setMinimumSize(new Dimension(descriptionWidth - 65, descriptionHeight-45));
    fldTimelineDescription.setPreferredSize(new Dimension(descriptionWidth - 65, descriptionHeight-45));
    fldTimelineDescription.setMinimumSize(new Dimension(descriptionWidth - 65, descriptionHeight-45));
    oldTimelineDescription = timeline.getDescription();
    fldTimelineDescription.setText(oldTimelineDescription);
    bordDescription.setTitleFont(timelineFont);
    pnlDescription.setBorder(bordDescription);

    // Timeline is panel
    pnlTimelineIs.setLayout(new VerticalFlowLayout());
    bordTimelineIs.setTitleFont(timelineFont);
    pnlTimelineIs.setBorder(bordTimelineIs);
    pnlTimelineIs.add(chkEditable);
    pnlTimelineIs.add(chkResizable);
    // pnlTimelineIs.add(chkMovable); // not movable in version 2
    chkEditable.setSelected(timeline.isEditable());
    chkEditable.setFont(timelineFont);
    chkEditable.setToolTipText("Set whether the timeline is editable");
    chkResizable.setSelected(timeline.isResizable());
    chkResizable.setFont(timelineFont);
    chkResizable.setToolTipText("Set whether the timeline is resizable");
    chkMovable.setSelected(timeline.isMovable());
    chkMovable.setFont(timelineFont);
    chkMovable.setToolTipText("Set whether the timeline is moveable");

    // Timeline appearance panel
    pnlTimelineAppearance.setLayout(new VerticalFlowLayout());
    bordTimelineAppearance.setTitleFont(timelineFont);
    pnlTimelineAppearance.setBorder(bordTimelineAppearance);
    pnlBubbleShape.setLayout(new VerticalFlowLayout());
    bordBubbleShape.setTitleFont(timelineFont);
    pnlBubbleShape.setBorder(bordBubbleShape);
    pnlTimelineAppearance.add(chkShowTimes);
    pnlTimelineAppearance.add(chkBlackAndWhite);
    pnlTimelineAppearance.add(pnlBubbleShape);
    pnlTimelineAppearance.add(btnBackgroundColor);
    pnlBubbleShape.add(radRoundBubbles);
    pnlBubbleShape.add(radSquareBubbles);
    grpBubbleShape.add(radRoundBubbles);
    grpBubbleShape.add(radSquareBubbles);
    chkShowTimes.setFont(timelineFont);
    chkShowTimes.setSelected(timeline.areTimesShown());
    chkShowTimes.setToolTipText("Show times below each timepoint and marker");
    chkBlackAndWhite.setFont(timelineFont);
    chkBlackAndWhite.setSelected(timeline.getBlackAndWhite());
    chkBlackAndWhite.setToolTipText("Set black and white mode");
    radRoundBubbles.setFont(timelineFont);
    radRoundBubbles.setSelected(timeline.getBubbleType() == 0);
    radRoundBubbles.setToolTipText("Set bubble shape to round");
    radSquareBubbles.setFont(timelineFont);
    radSquareBubbles.setSelected(timeline.getBubbleType() == 1);
    radSquareBubbles.setToolTipText("Set bubble shape to square");
    btnBackgroundColor.setFont(timelineFont);
    btnBackgroundColor.setToolTipText("Change the timeline background color");
    btnBackgroundColor.setBackground(pnlTimeline.getPanelColor());
    btnBackgroundColor.setPreferredSize(new Dimension(140, 23));

    // Timeline size panel
    pnlTimelineSize.setLayout(new VerticalFlowLayout(VerticalFlowLayout.TOP));
    bordTimelineSize.setTitleFont(timelineFont);
    pnlTimelineSize.setBorder(bordTimelineSize);
    sldBubbleHeight.setFont(timelineFont);
    sldBubbleHeight.setPaintTicks(true);
    sldBubbleHeight.setValue(timeline.getBubbleHeight() / 8);
    sldBubbleHeight.setMinorTickSpacing(1);
    sldBubbleHeight.setMajorTickSpacing(5);
    sldBubbleHeight.setSnapToTicks(true);
    sldBubbleHeight.setToolTipText("Adjust the bubble height");
    Hashtable<Integer, JLabel> tickLabels = new Hashtable<Integer, JLabel>();
    tickLabels.clear();
    JLabel lblSmall = new JLabel("small");
    lblSmall.setFont(timelineFont);
    JLabel lblLarge = new JLabel("large");
    lblLarge.setFont(timelineFont);
    tickLabels.put(Integer.valueOf(1), lblSmall);
    tickLabels.put(Integer.valueOf(9), lblLarge);
    sldBubbleHeight.setLabelTable(tickLabels);
    sldBubbleHeight.setPaintLabels(true);
    sldBubbleHeight.validate();
    pnlTimelineSize.add(sldBubbleHeight);
    chkAutoScale.setFont(timelineFont);
    chkAutoScale.setSelected(timeline.isAutoScalingOn());
    chkAutoScale.setToolTipText("Automatically scale bubble height when resizing timeline");
    pnlTimelineSize.add(chkAutoScale);

    // color buttons
    currColorScheme = timeline.colorScheme;
    bordTimelineColors.setTitleFont(timelineFont);
    pnlColorButtons.setLayout(new GridLayout());
    pnlLevelColors.setBorder(bordTimelineColors);
    pnlLevelColors.setLayout(new VerticalFlowLayout());
    for (int i = 1; i < 10; i++) {
      if (System.getProperty("os.name").startsWith("Mac OS")) {
        levelButton[i].setUI(new BasicButtonUI());
      }
      levelButton[i].setPreferredSize(new Dimension(levelButtonWidth,levelButtonHeight));
      levelButton[i].setFont(timelineFont);
      levelButton[i].setBackground(timeline.bubbleLevelColors[i]);
      customColors[i] = levelButton[i].getBackground(); // custom colors array initially set to current colors (even if not custom colors)
      levelButton[i].setMargin(new Insets(0, 0, 0, 0));
      levelButton[i].setText(String.valueOf(i));
      levelButton[i].setToolTipText("Set color for bubble level " + i);
      pnlColorButtons.add(levelButton[i]);
      levelButton[i].addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          int j;
          JButton btnLocal = (JButton)e.getSource();
          for (j = 1; j < 11; j++) {
            if (levelButton[j].equals(btnLocal)) {
              break;
            }
          }
          pnlTimeline.colorChooser.setColor(levelButton[j].getBackground());
          pnlTimeline.colorDialog.setTitle("Set Bubble Level Color");
          pnlTimeline.colorDialog.setVisible(true);

          Color newColor = pnlTimeline.newColor;
          if (newColor != null) {
            levelButton[j].setBackground(newColor);
            levelColorChanged[j] = true;
            for (int k = 1; k < 11; k++) {
               customColors[k] = levelButton[k].getBackground();
             }
             combColorSchemes.setSelectedIndex(0);
         }
          uilogger.log(UIEventType.BUTTON_CLICKED, "bubble level color change: level " + j);
        }
      });
    }
    pnlLevelColors.add(pnlColorButtons);

    // color schemes
    pnlLevelColors.add(pnlColorSchemes);
    lblColorScheme.setFont(timelineFont);
    combColorSchemes.setFont(timelineFont);
    combColorSchemes.addItem("[Custom]");
    combColorSchemes.addItem("Default");
    combColorSchemes.addItem("Pastels");
    combColorSchemes.addItem("Bright Colors");
    combColorSchemes.addItem("High Contrast");
    combColorSchemes.addItem("Warm Colors");
    combColorSchemes.addItem("Cool Colors");
    combColorSchemes.addItem("Earth Tones");
    combColorSchemes.setSelectedIndex(timeline.colorScheme);
    combColorSchemes.setEditable(false);
    combColorSchemes.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        switch (combColorSchemes.getSelectedIndex()) {
          case 0:
            for (int i = 1; i < 10; i++) {
              levelButton[i].setBackground(customColors[i]);
              levelColorChanged[i] = true;
              currColorScheme = 0;
              uilogger.log(UIEventType.BUTTON_CLICKED, "color scheme change: custom");
            }
            break;
          case 1:
            for (int i = 1; i < 10; i++) {
              levelButton[i].setBackground(timeline.defaultBubbleLevelColors[i]);
              levelColorChanged[i] = true;
              currColorScheme = 1;
              uilogger.log(UIEventType.BUTTON_CLICKED, "color scheme change: default");
            }
            break;
          case 2:
            for (int i = 1; i < 10; i++) {
              levelButton[i].setBackground(timeline.pastelBubbleLevelColors[i]);
              levelColorChanged[i] = true;
              currColorScheme = 2;
              uilogger.log(UIEventType.BUTTON_CLICKED, "color scheme change: pastel");
            }
            break;
          case 3:
            for (int i = 1; i < 10; i++) {
              levelButton[i].setBackground(timeline.brightColorsBubbleLevelColors[i]);
              levelColorChanged[i] = true;
              currColorScheme = 3;
              uilogger.log(UIEventType.BUTTON_CLICKED, "color scheme change: bright colors");
            }
            break;
          case 4:
            for (int i = 1; i < 10; i++) {
              levelButton[i].setBackground(timeline.highContrastBubbleLevelColors[i]);
              levelColorChanged[i] = true;
              currColorScheme = 4;
              uilogger.log(UIEventType.BUTTON_CLICKED, "color scheme change: high contrast");
            }
            break;
          case 5:
            for (int i = 1; i < 10; i++) {
              levelButton[i].setBackground(timeline.warmBubbleLevelColors[i]);
              levelColorChanged[i] = true;
              currColorScheme = 5;
              uilogger.log(UIEventType.BUTTON_CLICKED, "color scheme change: warm colors");
            }
            break;
          case 6:
            for (int i = 1; i < 10; i++) {
              levelButton[i].setBackground(timeline.coolBubbleLevelColors[i]);
              levelColorChanged[i] = true;
              currColorScheme = 6;
              uilogger.log(UIEventType.BUTTON_CLICKED, "color scheme change: cool colors");
            }
            break;
          case 7:
            for (int i = 1; i < 10; i++) {
              levelButton[i].setBackground(timeline.earthTonesBubbleLevelColors[i]);
              levelColorChanged[i] = true;
              currColorScheme = 7;
              uilogger.log(UIEventType.BUTTON_CLICKED, "color scheme change: earth tones");
            }
            break;
        }
    }});
    pnlColorSchemes.setLayout(new FlowLayout(FlowLayout.LEFT));
    pnlColorSchemes.add(lblColorScheme);
    pnlColorSchemes.add(combColorSchemes);

    // Audio settings panel
    pnlAudioSettings.setLayout(new VerticalFlowLayout());
    bordAudioSettings.setTitleFont(timelineFont);
    pnlAudioSettings.setBorder(bordAudioSettings);
    chkPlayWhenClicked.setFont(timelineFont);
    chkPlayWhenClicked.setSelected(timeline.playWhenBubbleClicked);
    chkPlayWhenClicked.setToolTipText("Start playing when bubble is clicked");
    chkStopPlaying.setFont(timelineFont);
    chkStopPlaying.setSelected(timeline.stopPlayingAtSelectionEnd);
    chkStopPlaying.setToolTipText("Stop playing at end of selected bubbles");
    pnlAudioSettings.add(chkPlayWhenClicked);
    pnlAudioSettings.add(chkStopPlaying);

    // if not editable when opened
    fldTimelineTitle.setEditable(chkEditable.isSelected());
    fldTimelineDescription.setEditable(chkEditable.isSelected());
    chkResizable.setEnabled(chkEditable.isSelected());
    chkShowTimes.setEnabled(chkEditable.isSelected());
    chkBlackAndWhite.setEnabled(chkEditable.isSelected());
    radRoundBubbles.setEnabled(chkEditable.isSelected());
    radSquareBubbles.setEnabled(chkEditable.isSelected());
    sldBubbleHeight.setEnabled(chkEditable.isSelected());
    chkAutoScale.setEnabled(chkEditable.isSelected());
    chkPlayWhenClicked.setEnabled(chkEditable.isSelected());
    chkStopPlaying.setEnabled(chkEditable.isSelected());
    for (int i = 1; i < 10; i++) {
      levelButton[i].setEnabled(chkEditable.isSelected());
    }

    // action for setting editable to false
    chkEditable.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        fldTimelineTitle.setEditable(chkEditable.isSelected());
        fldTimelineDescription.setEditable(chkEditable.isSelected());
        if (!chkEditable.isSelected()) {
          timeline.wasResizable = chkResizable.isSelected();
          chkResizable.setSelected(false);
        }
        else {
          chkResizable.setSelected(timeline.wasResizable);
        }
        chkResizable.setEnabled(chkEditable.isSelected());
        chkShowTimes.setEnabled(chkEditable.isSelected());
        chkBlackAndWhite.setEnabled(chkEditable.isSelected());
        radRoundBubbles.setEnabled(chkEditable.isSelected());
        radSquareBubbles.setEnabled(chkEditable.isSelected());
        sldBubbleHeight.setEnabled(chkEditable.isSelected());
        chkAutoScale.setEnabled(chkEditable.isSelected());
        chkPlayWhenClicked.setEnabled(chkEditable.isSelected());
        chkStopPlaying.setEnabled(chkEditable.isSelected());
        for (int i = 1; i < 10; i++) {
          levelButton[i].setEnabled(chkEditable.isSelected());
        }

        uilogger.log(UIEventType.CHECKBOX_SELECTED, "timeline is editable: " + chkEditable.isSelected());
      }
    });

    // other event handlers
    btnBackgroundColor.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Color currColor = pnlTimeline.getPanelColor(); // default color
        boolean wasBW = timeline.getBlackAndWhite();
        if (wasBW) {
          timeline.setBlackAndWhite(false);
        }
        // open the color chooser and get the new color
        pnlTimeline.colorChooser.setColor(currColor);
        pnlTimeline.colorDialog.setTitle("Set Background Color");
        pnlTimeline.colorDialog.setVisible(true);

        // set the background color to the new color
        Color newColor = pnlTimeline.newColor;
        if (newColor != null) {
          btnBackgroundColor.setBackground(newColor);
          // now discard new color
          newColor = null;
        }
        uilogger.log(UIEventType.BUTTON_CLICKED, "change background color");
      }
    });
    chkResizable.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        uilogger.log(UIEventType.CHECKBOX_SELECTED, "timeline is resizable: " + chkResizable.isSelected());
      }
    });
    chkShowTimes.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        uilogger.log(UIEventType.CHECKBOX_SELECTED, "show times: " + chkShowTimes.isSelected());
      }
    });
    chkBlackAndWhite.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        uilogger.log(UIEventType.CHECKBOX_SELECTED, "black and white: " + chkBlackAndWhite.isSelected());
      }
    });
    chkAutoScale.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        uilogger.log(UIEventType.CHECKBOX_SELECTED, "auto-scale: " + chkAutoScale.isSelected());
      }
    });
    chkPlayWhenClicked.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        uilogger.log(UIEventType.CHECKBOX_SELECTED, "play when clicked: " + chkPlayWhenClicked.isSelected());
      }
    });
    chkStopPlaying.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        uilogger.log(UIEventType.CHECKBOX_SELECTED, "stop playing at end of bubble: " + chkStopPlaying.isSelected());
      }
    });
    radRoundBubbles.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        uilogger.log(UIEventType.RADIOBUTTON_PICKED, "round bubbles");
      }
    });
    radSquareBubbles.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        uilogger.log(UIEventType.RADIOBUTTON_PICKED, "square bubbles");
      }
    });
    sldBubbleHeight.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        uilogger.log(UIEventType.SLIDER_ADJUSTED, "bubble height");
      }
    });

    // button properties
    int buttonWidth;
    if (System.getProperty("os.name").startsWith("Mac OS")) {
      buttonWidth = UIUtilities.scalePixels(65);
    } else {
      buttonWidth = UIUtilities.scalePixels(60);
    }

    // buttons
    btnApply.setFont(timelineFont);
    btnApply.setPreferredSize(new Dimension(buttonWidth, UIUtilities.scalePixels(23)));
    btnApply.setMargin(new Insets(2, 2, 2, 2));
    btnApply.setToolTipText("Apply property changes");
    btnOk.setFont(timelineFont);
    btnOk.setPreferredSize(new Dimension(buttonWidth, UIUtilities.scalePixels(23)));
    btnOk.setMargin(new Insets(2, 2, 2, 2));
    btnOk.setToolTipText("Accept property changes");
    btnCancel.setFont(timelineFont);
    btnCancel.setMargin(new Insets(2, 2, 2, 2));
    btnCancel.setPreferredSize(new Dimension(buttonWidth, UIUtilities.scalePixels(23)));
    btnRestore.setFont(timelineFont);
    btnCancel.setToolTipText("Discard any property changes");
    btnRestore.setMargin(new Insets(2, 2, 2, 2));
    btnRestore.setPreferredSize(new Dimension(UIUtilities.scalePixels(110), UIUtilities.scalePixels(23)));
    btnRestore.setToolTipText("Restore default property settings");

    // buttons panel
    pnlButtons.add(btnOk);
    pnlButtons.add(btnCancel);
    pnlButtons.add(btnApply);
    pnlButtons.add(new JPanel()); // spacer
    pnlButtons.add(btnRestore);

    // add button actions
    addActions();

    // show dialog
    this.pack();
    this.setVisible(true);
  }

  /**
   * creates button actions
   */
  private void addActions() {
    // button actions
    btnOk.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        applyChanges();
        closeWindow();
        uilogger.log(UIEventType.BUTTON_CLICKED, "accept timeline properties changes");
      }
    });
    btnCancel.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        closeWindow();
        uilogger.log(UIEventType.BUTTON_CLICKED, "cancel timeline properties changes");
      }
    });

    btnApply.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        applyChanges();
        uilogger.log(UIEventType.BUTTON_CLICKED, "apply timeline properties changes");
      }
    });
    btnRestore.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        fldTimelineTitle.setText(oldTimelineTitle);
        fldTimelineDescription.setText(oldTimelineDescription);
        chkEditable.setSelected(true);
        chkResizable.setSelected(true);
        chkShowTimes.setSelected(false);
        chkBlackAndWhite.setSelected(false);
        radRoundBubbles.setSelected(true);
        radSquareBubbles.setSelected(false);
        sldBubbleHeight.setValue(5);
        chkAutoScale.setSelected(true);
        btnBackgroundColor.setBackground(TimelinePanel.defaultPanelColor);
        for (int i = 1; i < 10; i++) {
          if (!levelButton[i].getBackground().equals(timeline.defaultBubbleLevelColors[i])) {
            levelButton[i].setBackground(timeline.defaultBubbleLevelColors[i]);
            levelColorChanged[i] = true;
          }
        }
        chkPlayWhenClicked.setSelected(false);
        chkStopPlaying.setSelected(false);
        fldTimelineTitle.setEditable(true);
        fldTimelineDescription.setEditable(true);
        chkResizable.setEnabled(true);
        chkShowTimes.setEnabled(true);
        chkBlackAndWhite.setEnabled(true);
        radRoundBubbles.setEnabled(true);
        radSquareBubbles.setEnabled(true);
        sldBubbleHeight.setEnabled(true);
        chkAutoScale.setEnabled(true);
        chkPlayWhenClicked.setEnabled(true);
        chkStopPlaying.setEnabled(true);
        for (int i = 1; i < 10; i++) {
          levelButton[i].setEnabled(true);
        }
        uilogger.log(UIEventType.BUTTON_CLICKED, "restore default timeline properties");
      }
    });
  }

  /**
   * closes the dialog
   */
  private void closeWindow() {
    if (System.getProperty("os.name").startsWith("Mac OS")) { // needed to fix a 1.3 key bug
      menubTimeline.enableMenuKeyboardShortcuts();
    }
    this.setVisible(false);
  }

  /**
   * applies any changes made
   */
  private void applyChanges() {
    // set up color variables
    int oldColorScheme = timeline.colorScheme;
    Color[] newLevelColors = new Color[11];
    Color[] oldLevelColors = new Color[11];
    Vector oldColors = new Vector(timeline.getNumTotalBubbles());
    Vector veryOldColors = new Vector(timeline.getNumTotalBubbles());
    Color newBackgroundColor = btnBackgroundColor.getBackground();
    Color oldBackgroundColor = pnlTimeline.getPanelColor();
    Color veryOldBackgroundColor;

    for (int i = 1; i < 11; i++) {
      newLevelColors[i] = levelButton[i].getBackground();
    }
    for (int i = 1; i < 11; i++) {
      oldLevelColors[i] = timeline.bubbleLevelColors[i];
    }
    for (int i = 0; i <= timeline.getNumTotalBubbles(); i++) {
      oldColors.addElement(timeline.getBubble(i).getColor());
    }

    // undo any previous "Apply"s (except colors) within this same dialog
    if (pnlTimeline.undoManager.canUndo()) {
      if (pnlTimeline.undoManager.getUndoPresentationName().equals("Undo Edit Properties")
          && firstApplyMade) {
        try {
          pnlTimeline.undoManager.undo();
        }
        catch (CannotUndoException cue) {
          cue.printStackTrace();
        }
        pnlTimeline.updateUndoMenu();

        // store "very old" colors -- those pertaining to the first property dialog color change
        for (int i = 0; i <= timeline.getNumTotalBubbles(); i++) {
          veryOldColors.addElement(timeline.getBubble(i).getColor());
          oldColors.setElementAt(veryOldColors.elementAt(i), i);
        }

        // now restore the current colors -- don't undo these
        for (int i = 1; i < 11; i++) {
          timeline.bubbleLevelColors[i] = oldLevelColors[i];
          if (levelColorChanged[i]) {
            timeline.setLevelColor(i, timeline.bubbleLevelColors[i]);
          }
        }
        timeline.colorScheme = oldColorScheme;
        for (int i = 0; i <= timeline.getNumTotalBubbles(); i++) {
          Bubble currBubble = timeline.getBubble(i);
          currBubble.setColor((Color)oldColors.elementAt(i));
        }

        veryOldBackgroundColor = pnlTimeline.getPanelColor();
        oldBackgroundColor = veryOldBackgroundColor;
        pnlTimeline.setPanelColor(oldBackgroundColor);
        timeline.lblThumb.setBackground(oldBackgroundColor);

      }
    }

    // set up other variables
    firstApplyMade = true;
    boolean editable, resizable, show, bw, square, autoscale, play, stop;
    boolean wasEditable, wasResizable, wereTimesShown, wasBW, wasSquare, wasAutoScaled, wasPlayed, wasStopped;
    int newHeight, oldHeight;
    editable = chkEditable.isSelected();
    resizable = chkResizable.isSelected();
    show = chkShowTimes.isSelected();
    bw = chkBlackAndWhite.isSelected();
    square = radSquareBubbles.isSelected();
    wasSquare = timeline.getBubbleType() == 1;
    newHeight = sldBubbleHeight.getValue() * 8;
    oldHeight = timeline.getBubbleHeight() / 8;
    autoscale = chkAutoScale.isSelected();
    play = chkPlayWhenClicked.isSelected();
    stop = chkStopPlaying.isSelected();
    wasEditable = timeline.isEditable();
    wasResizable = timeline.isResizable();
    wereTimesShown = timeline.areTimesShown();
    wasBW = timeline.getBlackAndWhite();
    wasAutoScaled = timeline.isAutoScalingOn();
    wasPlayed = timeline.playWhenBubbleClicked;
    wasStopped = timeline.stopPlayingAtSelectionEnd;
    oldTimelineTitle = pnlTimeline.getFrame().getTitle().substring(10);
    oldTimelineDescription = timeline.getDescription();

    // now apply new settings
    TimelineMenuBar tmb = pnlTimeline.getMenuBar();
    pnlTimeline.getFrame().setTitle("Timeline: " + fldTimelineTitle.getText());
    timeline.setDescription(fldTimelineDescription.getText());
    pnlTimeline.setEditableTimeline(editable);
    pnlTimeline.setResizableTimeline(resizable);

    pnlTimeline.setPanelColor(newBackgroundColor);
    timeline.lblThumb.setBackground(newBackgroundColor);

    if (System.getProperty("os.name").startsWith("Mac OS")) {
      pnlTimeline.menuiShowTimesMac.setSelected(show);
      tmb.menuiShowTimesMac.setSelected(show);
      timeline.showTimepointTimes(show);
      pnlTimeline.timelineBlackAndWhite = bw;
      tmb.menuiBlackAndWhiteMac.setSelected(bw);
      timeline.setBlackAndWhite(bw);
      if (!square) {
        tmb.menuiRoundBubblesMac.setSelected(true);
        tmb.menuiSquareBubblesMac.setSelected(false);
        timeline.setBubbleType(0);
      }
      else {
        tmb.menuiRoundBubblesMac.setSelected(false);
        tmb.menuiSquareBubblesMac.setSelected(true);
        timeline.setBubbleType(1);
      }
    }
    else {
      pnlTimeline.menuiShowTimes.setSelected(show);
      tmb.menuiShowTimes.setSelected(show);
      timeline.showTimepointTimes(show);
      pnlTimeline.timelineBlackAndWhite = bw;
      tmb.menuiBlackAndWhite.setSelected(bw);
      timeline.setBlackAndWhite(bw);
      if (!square) {
        tmb.menuiRoundBubbles.setSelected(true);
        tmb.menuiSquareBubbles.setSelected(false);
        timeline.setBubbleType(0);
      }
      else {
        tmb.menuiRoundBubbles.setSelected(false);
        tmb.menuiSquareBubbles.setSelected(true);
        timeline.setBubbleType(1);
      }
    }
    timeline.setBubbleHeight(newHeight);
    timeline.setAutoScaling(autoscale);

    for (int i = 1; i < 11; i++) { // reset bubbles colors for changed levels
      if (levelColorChanged[i]) {
        timeline.bubbleLevelColors[i] = newLevelColors[i];
        timeline.setLevelColor(i, timeline.bubbleLevelColors[i]);
      }
    }
    timeline.colorScheme = currColorScheme;
    timeline.playWhenBubbleClicked = play;
    timeline.stopPlayingAtSelectionEnd = stop;

    if (stop && !timeline.getSelectedBubbles().isEmpty()) {
      // set local start offset
      int pixelStart = timeline.getBubble(((Integer)timeline.getSelectedBubbles().firstElement()).intValue()).getStart();
      timeline.setLocalStartOffset(timeline.getOffsetAt(timeline.getTimepointNumberAtPixel(pixelStart)));
      // set local end offset
      int pixelEnd = timeline.getBubble(((Integer)timeline.getSelectedBubbles().lastElement()).intValue()).getEnd();
      timeline.setLocalEndOffset(timeline.getOffsetAt(timeline.getTimepointNumberAtPixel(pixelEnd)));
    }
    else {
      timeline.setLocalStartOffset(0);
      timeline.setLocalEndOffset(timeline.getPlayerDuration());
    }

    if (!timeline.isEditable()) {
      timeline.deselectAllBubbles();
      timeline.deselectAllTimepointsAndMarkers();
    }
    TimelineControlPanel pnlControl = pnlTimeline.getFrame().getControlPanel();
    if (pnlControl.isDescriptionShowing && timeline.getDescription().length() > 0) {
      pnlControl.showDescription();
    }
    else {
      pnlControl.updateAnnotationPane();
    }
    timeline.makeDirty();

    // allow for undo
    pnlTimeline.undoManager.undoableEditHappened(new UndoableEditEvent(this,
        new UndoableEditProperties(oldTimelineTitle, fldTimelineTitle.getText(), oldTimelineDescription,
        fldTimelineDescription.getText(), wasEditable, editable, wasResizable, resizable, wereTimesShown, show,
        wasBW, bw, wasSquare, square, oldHeight, newHeight, wasAutoScaled, autoscale, oldLevelColors, newLevelColors,
        oldColorScheme, currColorScheme, oldColors, levelColorChanged, wasPlayed, play, wasStopped, stop, pnlTimeline,
        oldBackgroundColor, newBackgroundColor, this)));
    pnlTimeline.updateUndoMenu();

  }

}