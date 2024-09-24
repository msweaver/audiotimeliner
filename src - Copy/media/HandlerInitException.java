package media;

/**
 * Thrown when a specific content handler (Player or Viewer) fails to load correctly.
 */
public class HandlerInitException extends Exception {

	private static final long serialVersionUID = 1L;
	public HandlerInitException() {}
    public HandlerInitException(String msg) {
        super(msg);
    }
}
