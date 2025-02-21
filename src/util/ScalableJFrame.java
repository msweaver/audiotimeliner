package util;

import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.HeadlessException;

import javax.swing.JFrame;

/**
 * SwingDPI Frame Scaler
 *
 * This class is an implementation of JFrame
 * Instead of creating/extending JFrame in your project use this class
 * and frame will be automatically scaled according to your SwingDPI API scale factor
 *
 * SwingDPI allows you to scale your application for convenient using on HiDPI screens
 * Call SwingDPI.applyScalingAutomatically() on your application start for easy scaling
 * GitHub Page: https://github.com/krlvm/SwingDPI
 *
 * @version 1.1
 * @author krlvm
 */
public class ScalableJFrame extends JFrame {

    private static final long serialVersionUID = 1L;

	public ScalableJFrame() throws HeadlessException {
        super();
    }

    public ScalableJFrame(GraphicsConfiguration gc) {
        super(gc);
    }

    public ScalableJFrame(String title) throws HeadlessException {
        super(title);
    }

    public ScalableJFrame(String title, GraphicsConfiguration gc) {
        super(title, gc);
    }

    @Override
    public void setSize(Dimension d) {
        super.setSize(SwingDPI.scale(d));
    }

    @Override
    public void setPreferredSize(Dimension preferredSize) {
        super.setPreferredSize(SwingDPI.scale(preferredSize));
    }
}
