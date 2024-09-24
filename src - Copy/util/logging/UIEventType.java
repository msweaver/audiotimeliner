package util.logging;


 public class UIEventType {

    private final String name;
    public String toString() { return name + " ";}
    private UIEventType(String s) { name  = s;}

    public final static UIEventType WINDOW_OPENED = new UIEventType("window opened");
    public final static UIEventType WINDOW_CLOSED = new UIEventType("window closed");
    public final static UIEventType WINDOW_RESIZED= new UIEventType("window resized");
    public final static UIEventType WINDOW_MOVED= new UIEventType("window moved");
        public final static UIEventType WINDOW_MAXIMIZED = new UIEventType("window maximized");
        public final static UIEventType WINDOW_RESTORED = new UIEventType("window restored");

    public final static UIEventType MENUITEM_SELECTED= new UIEventType("menu item selected");
        public final static UIEventType POPUPMENUITEM_SELECTED = new UIEventType("popup menu item selected");
    public final static UIEventType BUTTON_PRESSED= new UIEventType("button pressed");
    public final static UIEventType BUTTON_RELEASED = new UIEventType("button release");
    public final static UIEventType BUTTON_CLICKED = new UIEventType("button clicked");
    public final static UIEventType TAB_SELECTED= new UIEventType("tab selected");
    public final static UIEventType SLIDER_ADJUSTED= new UIEventType("slider adjusted");
    public final static UIEventType ITEM_DRAG_DROP= new UIEventType("item drag and drop");
    public final static UIEventType ITEM_SINGLE_CLICK= new UIEventType("item single click");
    public final static UIEventType ITEM_DOUBLE_CLICK= new UIEventType("item double click");
        public final static UIEventType ITEM_RIGHT_CLICK = new UIEventType("item right click");
        public final static UIEventType ITEM_DRAGGED = new UIEventType("item dragged");
    public final static UIEventType HYPERLINK_CLICKED = new UIEventType("hyperlink click");
    public final static UIEventType COMBOBOX_PICKED = new UIEventType("combobox pick");
    public final static UIEventType TREENODE_CLICKED = new UIEventType("treenode click");
    public final static UIEventType RADIOBUTTON_PICKED = new UIEventType("radiobutton pick");
        public final static UIEventType CHECKBOX_SELECTED = new UIEventType("checkbox selected");
        public final static UIEventType KEY_PRESSED = new UIEventType("key pressed");

}
