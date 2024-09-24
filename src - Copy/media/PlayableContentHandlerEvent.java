package media;

import java.util.EventObject;

/**
 * Content Handling Event.  May eventually need event hierarchy in place of a single class.
 *
 * @author Rob Pendleton
 * @author Jim Halliday
 */

public class PlayableContentHandlerEvent extends EventObject {

	private static final long serialVersionUID = 1L;
	/**
     * The requested content cannot be found, or is corrupted and unreadable.
     */
    static public final int CONTENT_NOT_FOUND = 1;
    /**
     * Content has entered a buffering state.
     */
    static public final int BUFFERING = 2;
    /**
     * Content has finished buffering and playback is beginning.
     */
    static public final int BUFFERING_COMPLETED = 3;
    /**
     * The end of content has been reached. This may be the end of the current
     * stream, media object, or container, depending on the class that generates
     * this event.
     */
    static public final int END_OF_CONTENT = 4;
    /**
     * There is a problem in accessing content. Playback, for audio content,
     * cannot continue.
     */
    static public final int CONTENT_PROBLEM = 5;

    protected String message;
    protected int type;

    public PlayableContentHandlerEvent(Object source, int type, String message) {
        super(source);
        this.type = type;
        this.message = message;
    }

    /**
     * Returns any message associated with this event.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Returns the event type associated with this event.
     */
     public int getType() {
        return type;
     }
}