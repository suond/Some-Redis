import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
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
                  if (command.startsWith("*")){
                      int numOfItems = Integer.parseInt(command.substring(1));
                      ArrayList<String> inputs = new ArrayList<>(numOfItems * 2);
                      for (int i =0; i < numOfItems * 2; i++){
                          inputs.add(reader.readLine());
                          System.out.println("element " + i + " is: " + inputs.get(i));
                      }
                      String cmd = inputs.get(1);
                      if (cmd.equalsIgnoreCase("ping")) {
                          outputStream.write("+PONG\r\n" .getBytes());
                          outputStream.flush();
                      } else if (cmd.equalsIgnoreCase("echo")) {
                          String echoOut = inputs.get(3)+ "\r\n";
                          outputStream.write( echoOut.getBytes());
                          outputStream.flush();
                      }
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
