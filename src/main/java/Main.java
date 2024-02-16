import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
  public static void main(String[] args) {
      // You can use print statements as follows for debugging, they'll be visible when running tests.
      System.out.println("Logs from your program will appear here!");

      ExecutorService executorService = Executors.newCachedThreadPool();
//      ServerSocket serverSocket = null;

      final int port = 6379;
      try(ServerSocket serverSocket = new ServerSocket(port)) {
//          serverSocket = new ServerSocket(port);
          serverSocket.setReuseAddress(true);

          while (!serverSocket.isClosed()) {
              try {
                 final Socket clientSocket = serverSocket.accept();
                  executorService.execute(() -> handle(clientSocket));
              } catch (Exception e) {
                  System.out.println("IOException: " + e.getMessage());
              }
          }
//         outputStream.close();
//         reader.close();
      } catch (IOException e) {
          System.out.println("IOException: " + e.getMessage());
      } finally {
          executorService.shutdown();
      }

  }


      public static void handle (Socket clientSocket){
          try (InputStream inputStream = clientSocket.getInputStream()){

              BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
              OutputStream outputStream = clientSocket.getOutputStream();

              String command;
              while ((command = reader.readLine()) != null) {
                  System.out.println(command);
                  if (command.equals("ping")) {
                      outputStream.write("+PONG\r\n" .getBytes());
                      //empties so other threads don't push in that info
                      outputStream.flush();
                  }
              }
          } catch (Exception e) {
              System.out.println("Issue occurred in handle " + e.getMessage());
          } finally {
              try {

                  clientSocket.close();
              } catch (IOException e){
                  System.out.println("could not close socket " + e.getMessage());
              }
          }

      }
  }
