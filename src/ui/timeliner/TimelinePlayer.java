package ui.timeliner;

/**
 * A player assigned to a timeline window. This player will play only the specified portion of the container.
 *
 * @author Jim Halliday
 * @author Brent Yorgason
 * STANDALONE: This is a dummy version of Timeline player for the standalone version. Do not update
 * it with the current version
 */

public class TimelinePlayer  {

  public boolean isPlaying = false;
  public int startOffset = 0;
  public int endOffset = 0;
  public int nextImportantOffset = 0;
  public int localStartOffset = 0;
  public int localEndOffset = 0;
  public int shiftAmount = 0;

  public TimelinePlayer(String containerID, int start, int stop, TimelineFrame parent) throws Exception {
    // do nothing!
  }
  protected void openDetails() {
    // do nothing!
  }
  public void doPause() {
    // do nothing!
  }
  public void play() {
    // do nothing!
  }
  public void pause() {
    // do nothing!
  }
  public String getContainerID() {
    return "nachos";
  }
  public int getOffset() {
    return 0;
  }
  public int getDuration() {
    return 0;
  }
  public void setOffset(int i){
    // do nothing!
  }
  public java.awt.Component getQTComponent() {
    return null;
  }
  public void turnOffTimer() {
    // do nothing!
  }
  public void setVolume(float f) {
    // do nothing!
  }
}
