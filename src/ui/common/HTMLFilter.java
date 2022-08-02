package ui.common;

import java.io.File;
import javax.swing.filechooser.*;

/**
 * A file filter to display only HTML files.
 *
 * @see EditBookmarks
 * @see RecordViewWindow
 */
public class HTMLFilter extends FileFilter {

    // Accept all directories and all HTML files.
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }
        String extension = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');
        if (i > 0 &&  i < s.length() - 1) {
            extension = s.substring(i+1).toLowerCase();
        }
        if (extension != null) {
            if (extension.equals("html") || extension.equals("htm")) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    // The description of this filter
    public String getDescription() {
        return "HTML Files";
    }
}