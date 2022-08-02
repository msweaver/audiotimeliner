package client;

import util.ChainableException;

/**
 * An exception that indicates something has gone wrong with parsing or processing a V2X file.
 * @author Ryan Scherle
 * @version 1.0
 */

public class V2XProcessingException extends ChainableException {

 	private static final long serialVersionUID = 1L;

	public V2XProcessingException(String msg) {
        super(msg);
    }

    public V2XProcessingException(String msg, Exception chainedException) {
      super(msg, chainedException);
    }
}