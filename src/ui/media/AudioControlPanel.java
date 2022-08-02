package ui.media;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import ui.common.*;

/**
 * A panel with the six basic playback controls built in.
 *
 * @author Jim Halliday
 */
public class AudioControlPanel extends JPanel {

 	private static final long serialVersionUID = 1L;
	public FlowLayout flowLay = new FlowLayout();
    public JButton btnFF = new JButton();
    public JButton btnPrev = new JButton();
    public JButton btnNext = new JButton();
    public JButton btnStop = new JButton();
    public JButton btnPlay = new JButton();
    public JButton btnRW = new JButton();

    public AudioControlPanel() {
        setBorder(BorderFactory.createEtchedBorder());
        setMinimumSize(new Dimension(171, 35));
        setPreferredSize(new Dimension(171, 35));
        this.setMaximumSize(new Dimension(171, 35));
        flowLay.setVgap(3);
        setLayout(flowLay);
        add(btnPlay, null);
        add(btnStop, null);
        add(btnPrev, null);
        add(btnRW, null);
        add(btnFF, null);
        add(btnNext, null);
        btnPrev.setMinimumSize(new Dimension(22, 23));
        btnPrev.setPreferredSize(new Dimension(22, 23));
        btnNext.setMinimumSize(new Dimension(22, 23));
        btnNext.setPreferredSize(new Dimension(22, 23));
        btnPlay.setMinimumSize(new Dimension(22, 23));
        btnPlay.setPreferredSize(new Dimension(22, 23));
        btnPlay.setIcon(UIUtilities.icoPlay);
        btnStop.setMinimumSize(new Dimension(22, 23));
        btnStop.setPreferredSize(new Dimension(22, 23));
        btnStop.setIcon(UIUtilities.icoStop);
        btnRW.setMinimumSize(new Dimension(22, 23));
        btnRW.setPreferredSize(new Dimension(22, 23));
        btnFF.setMinimumSize(new Dimension(22, 23));
        btnFF.setPreferredSize(new Dimension(22, 23));
        btnPrev.setMargin(new Insets(0, 0, 0, 0));
        btnPrev.setIcon(UIUtilities.icoPrev);
        btnNext.setMargin(new Insets(0, 0, 0, 0));
        btnNext.setIcon(UIUtilities.icoNext);
        btnStop.setMargin(new Insets(0, 0, 0, 0));
        btnPlay.setMargin(new Insets(0, 0, 0, 0));
        btnRW.setMargin(new Insets(0, 0, 0, 0));
        btnRW.setIcon(UIUtilities.icoRW);
        btnFF.setMargin(new Insets(0, 0, 0, 0));
        btnFF.setIcon(UIUtilities.icoFF);
        if (!(System.getProperty("os.name").startsWith("Mac OS"))) {
            btnNext.setBorder(null);
            btnNext.setFocusPainted(false);
            btnNext.setBounds(new Rectangle(60, 79, 21, 21));
            btnFF.setBorder(null);
            btnFF.setFocusPainted(false);
            btnFF.setBounds(new Rectangle(60, 79, 21, 21));
            btnRW.setBorder(null);
            btnRW.setFocusPainted(false);
            btnRW.setBounds(new Rectangle(60, 79, 21, 21));
            btnStop.setBorder(null);
            btnStop.setFocusPainted(false);
            btnStop.setBounds(new Rectangle(60, 79, 21, 21));
            btnPlay.setBorder(null);
            btnPlay.setFocusPainted(false);
            btnPlay.setBounds(new Rectangle(60, 79, 21, 21));
            btnPrev.setBorder(null);
            btnPrev.setFocusPainted(false);
            btnPrev.setBounds(new Rectangle(60, 79, 21, 21));
            btnPrev.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    UIUtilities.doButtonBorderSwitch(e, btnPrev);
                }
                public void mouseExited(MouseEvent e) {
                    UIUtilities.doButtonBorderSwitch(e, btnPrev);
                }
            });
            btnPlay.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    UIUtilities.doButtonBorderSwitch(e, btnPlay);
                }
                public void mouseExited(MouseEvent e) {
                    UIUtilities.doButtonBorderSwitch(e, btnPlay);
                }
            });
            btnStop.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    UIUtilities.doButtonBorderSwitch(e, btnStop);
                }
                public void mouseExited(MouseEvent e) {
                    UIUtilities.doButtonBorderSwitch(e, btnStop);
                }
            });
            btnRW.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    UIUtilities.doButtonBorderSwitch(e, btnRW);
                }
                public void mouseExited(MouseEvent e) {
                    UIUtilities.doButtonBorderSwitch(e, btnRW);
                }
            });
            btnFF.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    UIUtilities.doButtonBorderSwitch(e, btnFF);
                }
                public void mouseExited(MouseEvent e) {
                    UIUtilities.doButtonBorderSwitch(e, btnFF);
                }
            });
            btnNext.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    UIUtilities.doButtonBorderSwitch(e, btnNext);
                }
                public void mouseExited(MouseEvent e) {
                    UIUtilities.doButtonBorderSwitch(e, btnNext);
                }
            });
            btnNext.addFocusListener(new java.awt.event.FocusAdapter() {
                public void focusGained(FocusEvent e) {
                    UIUtilities.doButtonBorderSwitch(e, btnNext, true);
                }
                public void focusLost(FocusEvent e) {
                    UIUtilities.doButtonBorderSwitch(e, btnNext, false);
                }
            });
            btnFF.addFocusListener(new java.awt.event.FocusAdapter() {
                public void focusGained(FocusEvent e) {
                    UIUtilities.doButtonBorderSwitch(e, btnFF, true);
                }
                public void focusLost(FocusEvent e) {
                    UIUtilities.doButtonBorderSwitch(e, btnFF, false);
                }
            });
            btnPrev.addFocusListener(new java.awt.event.FocusAdapter() {
                public void focusGained(FocusEvent e) {
                    UIUtilities.doButtonBorderSwitch(e, btnPrev, true);
                }
                public void focusLost(FocusEvent e) {
                    UIUtilities.doButtonBorderSwitch(e, btnPrev, false);
                }
            });
            btnRW.addFocusListener(new java.awt.event.FocusAdapter() {
                public void focusGained(FocusEvent e) {
                    UIUtilities.doButtonBorderSwitch(e, btnRW, true);
                }
                public void focusLost(FocusEvent e) {
                    UIUtilities.doButtonBorderSwitch(e, btnRW, false);
                }
            });
            btnPlay.addFocusListener(new java.awt.event.FocusAdapter() {
                public void focusGained(FocusEvent e) {
                    UIUtilities.doButtonBorderSwitch(e, btnPlay, true);
                }
                public void focusLost(FocusEvent e) {
                    UIUtilities.doButtonBorderSwitch(e, btnPlay, false);
                }
            });
            btnStop.addFocusListener(new java.awt.event.FocusAdapter() {
                public void focusGained(FocusEvent e) {
                    UIUtilities.doButtonBorderSwitch(e, btnStop, true);
                }
                public void focusLost(FocusEvent e) {
                    UIUtilities.doButtonBorderSwitch(e, btnStop, false);
                }
            });
        }
    }
}