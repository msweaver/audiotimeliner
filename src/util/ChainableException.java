package util;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * Simple chainable exception.
 *
 * @author Rob Pendleton
 * @author Ryan Scherle
 */

public class ChainableException extends Exception {

	private static final long serialVersionUID = 1L;
	protected Throwable chainedException;

    public ChainableException(String msg) {
        super(msg);
    }

    public ChainableException(String msg, Throwable chainedException) {
        super(msg);
        this.chainedException = chainedException;
    }

    public Throwable getChainedException() {
        return chainedException;
    }

    public void printStackTrace() {
        printStackTrace(System.err);
    }

    public void printStackTrace(PrintStream s) {
        super.printStackTrace(s);
        if(chainedException != null) {
            s.println("Caused by...");
            chainedException.printStackTrace(s);
        }
    }

    public void printStackTrace(PrintWriter w) {
        super.printStackTrace(w);
        if(chainedException != null) {
            w.println("Caused by...");
            chainedException.printStackTrace(w);
        }
    }
}

