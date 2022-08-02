package util.logging;

import org.apache.log4j.Logger;


public class UILogger {
    Logger logger;
    public UILogger(Logger logger) {
        this.logger = logger;
    }
    public void log(UIEventType type, String data) {
        String s = "";
        if (type != null) {
            s += type.toString();
        }
        if (data != null) {
            s += "- " + data;
        }
        logger.info(s);
    }

    public void log(UIEventType event_type) {
        log(event_type, null);
    }

    public void log(String data) {
        log(null, data);
    }
}
