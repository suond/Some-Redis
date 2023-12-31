import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
  public static void main(String[] args){
    // You can use print statements as follows for debugging, they'll be visible when running tests.
    System.out.println("Logs from your program will appear here!");

    //  Uncomment this block to pass the first stage
       ServerSocket serverSocket = null;
       Socket clientSocket = null;
       int port = 6379;
       try {
         serverSocket = new ServerSocket(port);
         serverSocket.setReuseAddress(true);
         // Wait for connection from client.
         clientSocket = serverSocket.accept();

         InputStream inputStream = clientSocket.getInputStream();
         BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

         OutputStream outputStream = clientSocket.getOutputStream();
//         OutputStreamWriter writer = new OutputStreamWriter(outputStream);
         String command;
         while ( (command = reader.readLine()) != null){
             if (command.equals("ping")){
                 outputStream.write("+PONG\r\n".getBytes());
             }
         }
//         outputStream.write("+PONG\r\n".getBytes());
         outputStream.close();
       } catch (IOException e) {
         System.out.println("IOException: " + e.getMessage());
       } finally {
         try {
           if (clientSocket != null) {
             clientSocket.close();
           }
         } catch (IOException e) {
           System.out.println("IOException: " + e.getMessage());
         }
       }
  }
}
