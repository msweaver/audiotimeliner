package ui.common;
 
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.PasswordAuthentication;

//import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
 
/**
 * A utility that downloads a file from a URL.
 * @author www.codejava.net
 *
 */
public class HttpDownloadUtility {
    private static final int BUFFER_SIZE = 4096;
 
    /**
     * Downloads a file from a URL
     * @param fileURL HTTP URL of the file to be downloaded
     * @param saveDir path of the directory to save the file
     * @throws IOException
     */
    public static void downloadFile(String fileURL, String saveDir)
            throws IOException {
    	// Create a new trust manager that trusts all certificates
    	
    	TrustManager[] trustAllCerts = new TrustManager[]{
    	    new X509TrustManager() {
    	        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
    	            return null;
    	        }
    	        public void checkClientTrusted(
    	            java.security.cert.X509Certificate[] certs, String authType) {
    	        }
    	        public void checkServerTrusted(
    	            java.security.cert.X509Certificate[] certs, String authType) {
    	        }
    	    }
    	};

    	// Activate the new trust manager
    	try {
    	    SSLContext sc = SSLContext.getInstance("SSL");
    	    sc.init(null, trustAllCerts, new java.security.SecureRandom());
    	    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
    	} catch (Exception e) {
    	}
    	
    	java.net.Authenticator.setDefault (new java.net.Authenticator() {
    	    protected PasswordAuthentication getPasswordAuthentication() {
    	        return new PasswordAuthentication ("singanew", "VPgpSO732a#hGNep".toCharArray());
    	    }
    	});
    	
    	//String baseURL = "https://www.singanewsong.org/audiotimeliner/share/mp3/";
        //URL url = new URL(baseURL + fileURL);
    	URL url = new URL(fileURL);
		  
        HttpURLConnection httpConn = null; 
        httpConn = (HttpURLConnection)url.openConnection();
        httpConn.setReadTimeout(60 * 1000);
        httpConn.setConnectTimeout(60 * 1000);

        //JOptionPane.showMessageDialog(null, " downloading " + url);
        int responseCode = httpConn.getResponseCode();
		//  JOptionPane.showMessageDialog(null, "response = " + responseCode);

        // always check HTTP response code first
        if (responseCode == HttpURLConnection.HTTP_OK) {
           String fileName = "";
            String disposition = httpConn.getHeaderField("Content-Disposition");
            String contentType = httpConn.getContentType();
            int contentLength = httpConn.getContentLength();
 
            if (disposition != null) {
                // extracts file name from header field
                int index = disposition.indexOf("filename=");
                if (index > 0) {
                    fileName = disposition.substring(index + 10,
                            disposition.length() - 1);
                }
            } else {
                // extracts file name from URL
                fileName = fileURL.substring(fileURL.lastIndexOf("/") + 1,
                        fileURL.length());
            }
 
            System.out.println("Content-Type = " + contentType);
            System.out.println("Content-Disposition = " + disposition);
            System.out.println("Content-Length = " + contentLength);
            System.out.println("fileName = " + fileName);
 
            // opens input stream from the HTTP connection
            InputStream inputStream = httpConn.getInputStream();
            String saveFilePath = saveDir + File.separator + fileName;
             
            // opens an output stream to save into file
            FileOutputStream outputStream = new FileOutputStream(saveFilePath);
 
            int bytesRead = -1;
            byte[] buffer = new byte[BUFFER_SIZE];
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
 
            outputStream.close();
            inputStream.close();
 
            System.out.println("File downloaded");
            
            /// set delete of file on exit
            
           // File f = new File(saveFilePath);
           // f.deleteOnExit();  
            
        } else {
            System.out.println("No file to download. Server replied HTTP code: " + responseCode);
        }
        httpConn.disconnect();
    }
}