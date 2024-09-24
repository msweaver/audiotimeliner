package ui.media;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.FocusEvent;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSlider;

import ui.common.UIUtilities;

/**
 * A panel with the basic volume controls built in.
 *
 * @author Jim Halliday
 */
public class VolumePanel extends JPanel {

	private static final long serialVersionUID = 1L;
	public JButton btnMute = new JButton();
    public JSlider slideVolume = new JSlider();
    VariationsWindowsSliderUI volumeSliderUI;
    VariationsMacSliderUI volumeSliderMacUI;
    final ImageIcon speaker = new ImageIcon(getClass().getClassLoader().getResource("resources/media/speaker.gif"));
    final ImageIcon mute = new ImageIcon(getClass().getClassLoader().getResource("resources/media/speaker-mute.gif"));

    public VolumePanel() {
        if (System.getProperty("os.name").startsWith("Mac OS")) {
            volumeSliderMacUI = new VariationsMacSliderUI(slideVolume);
            slideVolume.setUI(volumeSliderMacUI);
            volumeSliderMacUI.hasVolumeWedge(true);
        } else {
            volumeSliderUI = new VariationsWindowsSliderUI(slideVolume);
            slideVolume.setUI(volumeSliderUI);
            volumeSliderUI.hasVolumeWedge(true);

            btnMute.setBorder(null);
            btnMute.setFocusPainted(false);
            btnMute.setBounds(new Rectangle(60, 79, 21, 21));
            btnMute.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    UIUtilities.doButtonBorderSwitch(e, btnMute);
                }
                public void mouseExited(MouseEvent e) {
                    UIUtilities.doButtonBorderSwitch(e, btnMute);
                }
            });
        }
        slideVolume.setMinimumSize(new Dimension(60, 23));
        slideVolume.setPreferredSize(new Dimension(60, 23));
        btnMute.setMinimumSize(new Dimension(22, 23));
        btnMute.setPreferredSize(new Dimension(22, 23));
        btnMute.setToolTipText("Mute");
        btnMute.setMargin(new Insets(0, 0, 0, 0));
        btnMute.setIcon(speaker);
        btnMute.addFocusListener(new java.awt.event.FocusAdapter() {
                public void focusGained(FocusEvent e) {
                    UIUtilities.doButtonBorderSwitch(e, btnMute, true);
                }
                public void focusLost(FocusEvent e) {
                    UIUtilities.doButtonBorderSwitch(e, btnMute, false);
                }
            });
        this.add(btnMute, null);
        this.add(slideVolume, BorderLayout.CENTER);
    }
}