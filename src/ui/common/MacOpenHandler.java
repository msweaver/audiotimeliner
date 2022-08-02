package ui.common;

import com.apple.mrj.*;
import java.io.File;
import client.*;

/**
 * Handle Apple OpenDocument event
 *
 * @author Jon Dunn
 */
public class MacOpenHandler implements MRJOpenDocumentHandler {

    public MacOpenHandler() {
    }

    public void handleOpenFile(File fileName) {
        Client.MacOpenFile= true;
        try {
          CommandProcessor processor = new CommandProcessor();
          processor.processCommandFile(fileName.getAbsolutePath());
        } catch (Exception e) {
          e.printStackTrace();
        }
    }
}
