package media;

import util.ChainableException;

/**
 * Thrown when errors occur in content rendering and retrieval.
 */
public class ContentLoadingException extends ChainableException {

	private static final long serialVersionUID = 1L;

	public ContentLoadingException(String msg) {
        super(msg);
    }

    public ContentLoadingException(String msg, Exception chainedException) {
        super(msg, chainedException);
    }
}