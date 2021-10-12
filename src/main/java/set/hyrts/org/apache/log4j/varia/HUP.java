package set.hyrts.org.apache.log4j.varia;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ServerSocket;
import java.net.Socket;
import set.hyrts.org.apache.log4j.helpers.LogLog;

class HUP extends Thread {
   int port;
   ExternallyRolledFileAppender er;

   HUP(ExternallyRolledFileAppender er, int port) {
      this.er = er;
      this.port = port;
   }

   public void run() {
      while(!this.isInterrupted()) {
         try {
            ServerSocket serverSocket = new ServerSocket(this.port);

            while(true) {
               Socket socket = serverSocket.accept();
               LogLog.debug("Connected to client at " + socket.getInetAddress());
               (new Thread(new HUPNode(socket, this.er), "ExternallyRolledFileAppender-HUP")).start();
            }
         } catch (InterruptedIOException var3) {
            Thread.currentThread().interrupt();
            var3.printStackTrace();
         } catch (IOException var4) {
            var4.printStackTrace();
         } catch (RuntimeException var5) {
            var5.printStackTrace();
         }
      }

   }
}
