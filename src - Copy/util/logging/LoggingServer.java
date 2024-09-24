package util.logging;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.log4j.Category;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.helpers.AbsoluteTimeDateFormat;
import org.apache.log4j.spi.LoggerRepository;
import org.apache.log4j.spi.LoggingEvent;


public class LoggingServer {

    static Category cat = Category.getInstance(LoggingServer.class.getName());
    static int port;

    public static void main(String argv[]) {
        if(argv.length == 2)
            init(argv[0], argv[1]);
        else
            usage("Wrong number of arguments.");

        try {
            cat.info("Listening on port " + port);
            ServerSocket serverSocket = new ServerSocket(port);
            while(true) {
            // cat.info("Waiting to accept a new client.");
            Socket socket = serverSocket.accept();
            cat.info("Connected to client at " + socket.getInetAddress());
            new Thread(new SocketNode(socket,
                  LogManager.getLoggerRepository())).start();
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }


    static void  usage(String msg) {
        System.err.println(msg);
        System.err.println(
            "Usage: java " +LoggingServer.class.getName() + " port file");
        System.exit(1);
    }

    static void init(String portStr, String configFile) {
        try {
            port = Integer.parseInt(portStr);
        } catch(java.lang.NumberFormatException e) {
            e.printStackTrace();
            usage("Could not interpret port number ["+ portStr +"].");
        }
        PropertyConfigurator.configure(configFile);
    }
}
     class SocketNode implements Runnable {

        Socket socket;
        LoggerRepository hierarchy;
        ObjectInputStream ois;

        static Logger logger = Logger.getLogger(SocketNode.class);

        public
        SocketNode(Socket socket, LoggerRepository hierarchy) {
            this.socket = socket;
            this.hierarchy = hierarchy;
            try {
                ois = new ObjectInputStream(
                             new BufferedInputStream(socket.getInputStream()));
            } catch(Exception e) {
                logger.error("Could not open ObjectInputStream to "+socket, e);
            }
        }

        public void run() {
            LoggingEvent event, newevent;
            Logger remoteLogger;
            StringBuffer msgbuf;
            AbsoluteTimeDateFormat dateFormat = new AbsoluteTimeDateFormat();
            try {
                while(true) {
                event = (LoggingEvent) ois.readObject();
                remoteLogger = hierarchy.getLogger(event.categoryName);

 //               if(event.level.isGreaterOrEqual(remoteLogger.getEffectiveLevel())) {
 //                   event.logger = remoteLogger; // had to comment this out to compile 2022
 //                       msgbuf = new StringBuffer();
 //                      msgbuf.append("[");
 //                       msgbuf.append(socket.getInetAddress());
 //                       msgbuf.append(" -- ");
 //                       dateFormat.format(new java.util.Date(event.timeStamp), msgbuf, null);
 //                       msgbuf.append("] ");
 //                       msgbuf.append(event.getMessage());
//
 //                       newevent = new LoggingEvent(event.fqnOfCategoryClass,
  //                                      remoteLogger, event.level, msgbuf.toString(), null);
   //                 remoteLogger.callAppenders(newevent);
  //              }
                }
            } catch(java.io.EOFException e) {
                logger.info("Connection closed by client at " + socket.getInetAddress());
            } catch(java.net.SocketException e) {
                logger.info("Caught java.net.SocketException closing conneciton.");
            } catch(IOException e) {
                logger.info("Caught java.io.IOException: "+e);
                logger.info("Closing connection.");
            } catch(Exception e) {
                logger.error("Unexpected exception. Closing conneciton.", e);
            }

            try {
                ois.close();
            } catch(Exception e) {
                logger.info("Could not close connection.", e);
            }
        }
    }
