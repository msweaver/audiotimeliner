package ui.timeliner;

import java.awt.*;
import javax.swing.*;
import java.awt.print.*;

/**
 *
 * PrintUtilities class
 *
 *  A simple utility class that lets you very simply print
 *  an arbitrary component. Just pass the component to the
 *  PrintUtilities.printComponent. The component you want to
 *  print doesn't need a print method and doesn't have to
 *  implement any interface or do anything special at all.
 *
 *  7/99 Marty Hall, http://www.apl.jhu.edu/~hall/java/
 *  May be freely used or adapted.
 *
 * adapted by Brent Yorgason, 2002
 */

public class PrintUtilities implements Printable, Pageable {
  private Component componentToBePrinted;
  private PageFormat printFormat;

  public static void printComponent(Component c) {
    new PrintUtilities(c).print();
  }

  public PrintUtilities(Component componentToBePrinted) {
    this.componentToBePrinted = componentToBePrinted;
  }

  public void print() {
    PrinterJob printJob = PrinterJob.getPrinterJob();
    printFormat = new PageFormat();
    printFormat.setOrientation(0);                  // print in landscape format
    printJob.setPrintable(this, printFormat);
    printJob.setPageable(this);
    if (printJob.printDialog())
      try {
        printJob.print();
      } catch(PrinterException pe) {
        System.out.println("Error printing: " + pe);
      }
  }

  public int print(Graphics g, PageFormat pageFormat, int pageIndex) {
    if (pageIndex > 0) {
      return(NO_SUCH_PAGE);
    } else {
      Graphics2D g2d = (Graphics2D)g;
      //Scale the component to fit
     //Calculate the scale factor to fit the window to the page.
      double scaleX = pageFormat.getImageableWidth()/componentToBePrinted.getWidth();
      double scaleY = pageFormat.getImageableHeight()/componentToBePrinted.getHeight();
      double scale = Math.min(scaleX,scaleY);  //Get minimum scale factor

      //Move paper origin to page printing area corner
      g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY() + (pageFormat.getImageableHeight() / 2) - (componentToBePrinted.getHeight() / 2));
      g2d.scale(scale, scale);                   //Apply the scale factor
      disableDoubleBuffering(componentToBePrinted);
      componentToBePrinted.paint(g2d);
      enableDoubleBuffering(componentToBePrinted);
      return(PAGE_EXISTS);
    }
  }

  /** The speed and quality of printing suffers dramatically if
   *  any of the containers have double buffering turned on.
   *  So this turns if off globally.
   *  (see enableDoubleBuffering)
   */
  public static void disableDoubleBuffering(Component c) {
    RepaintManager currentManager = RepaintManager.currentManager(c);
    currentManager.setDoubleBufferingEnabled(false);
  }

  /** Re-enables double buffering globally. */

  public static void enableDoubleBuffering(Component c) {
    RepaintManager currentManager = RepaintManager.currentManager(c);
    currentManager.setDoubleBufferingEnabled(true);
  }
  public Printable getPrintable(int pageIndex) {
    return this;
  }
  public PageFormat getPageFormat(int pageIndex) {
    return printFormat;
  }
  public int getNumberOfPages() {
    return 1;
  }
}
