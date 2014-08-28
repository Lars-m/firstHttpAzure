package firsthttpserver;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;

/**
 * @author Lars Mortensen
 */
public class FirstHtttpServer {
  static int port = 8080;
  static String ip = "127.0.0.1";
  public static void main(String[] args) throws Exception {
    if(args.length == 2){
      port = Integer.parseInt(args[0]);
      ip = args[1];
    }
    HttpServer server = HttpServer.create(new InetSocketAddress(ip,port), 0);
    server.createContext("/welcome", new RequestHandler());
    server.createContext("/pages", new RequestForAFileHandler());
    server.setExecutor(null); // Use the default executor
    server.start();
    System.out.println("Server started, listening on port: "+port);
  }

  static class RequestHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange he) throws IOException {
      String response = "Welcome to my very first almost home made Web Server :-)";
      he.sendResponseHeaders(200, response.length());
      try (PrintWriter pw = new PrintWriter(he.getResponseBody())) {
        pw.print(response); //What happens if we use a println instead of print --> Explain
      }
    }
  }
  
   static class RequestForAFileHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange he) throws IOException {
      String requestedFile = he.getRequestURI().toString();
      String f = requestedFile.substring(requestedFile.lastIndexOf("/") + 1);
      String extension = f.substring(f.lastIndexOf("."));
      String mime = "";
      switch (extension) {
        case ".pdf":
          mime = "application/pdf";
          break;
        case ".png":
          mime = "image/png";
          break;
        case ".html":
          mime = "text/html";
          break;
        case ".jar":
          mime = "application/java-archive";
          break;
      }
      File file = new File("public/" + f);
      byte[] bytesToSend = new byte[(int) file.length()];
      String errorMsg = null;
      int responseCode = 200;
      try {
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
        bis.read(bytesToSend, 0, bytesToSend.length);
      } catch (IOException ie) {
        errorMsg = "<h1>404 Not Found</h1>No context found for request";
      }
      if (errorMsg == null) {
        Headers h = he.getResponseHeaders();
        h.set("Content-Type", mime);
      } else {
        responseCode = 404;
        bytesToSend = errorMsg.getBytes();

      }
      he.sendResponseHeaders(responseCode, bytesToSend.length);
      try (OutputStream os = he.getResponseBody()) {
        os.write(bytesToSend, 0, bytesToSend.length);
      }
    }
  }
}