package media;

/**
* Content handler interface for audio renderable media content.
*
* @author Rob Pendleton
* @author Jim Halliday
*/
public interface PlayableContentHandler {

    /**
     * Sets the reference for content. If this is called while content is playing
     * or buffering, playback will stop and playback must be reinstated by calling play().
     */
    public void setContentRef(String ref);
    public String getContentRef();
    public void play();
    public void pause();

    /**
     * Used to pass the required QuickTime component up to the higher level classes. When not using QuickTime, this method
     * should return a null value.
     *
     * @return An invisible java.awt.Component with the necessary QuickTime component embedded within it.
     */
    public java.awt.Component getQTComponent();
    /**
     * Sets the offset in milliseconds into the current stream, MO, or container
     * (depending on the class that makes this call).
     * This should work whether currently playing or not.
     *
     * @param offset the offset in milliseconds into the content.
     */
    public void setOffset(int offset);
    /**
     * Gets the offset in milliseconds into the current stream, MO, or container
     * (depending on the class that makes this call).
     */
    public int getOffset();
    /**
     * Sets volume level relative to system volume.
     * Zero represents silence and 1 represents maximum volume.
     */
    public void setVolume(float vol);
    /**
     * Gets volume level relative to system volume.
     *
     * @return the current volume
     */
    public float getVolume();
    public void addListener(PlayableContentHandlerListener listener);
    public void removeListener(PlayableContentHandlerListener listener);
}

