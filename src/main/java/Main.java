import command.Echo;
import command.Get;
import command.Ping;
import command.Set;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    static Map<String, String> cache = new HashMap<>();
  public static void main(String[] args) {

      // You can use print statements as follows for debugging, they'll be visible when running tests.
      System.out.println("Logs from your program will appear here!");
//      for (String s: args){
//          System.out.println(s);
//      }
      if (args.length == 2){
          Redis redis = new Redis(Integer.parseInt(args[1]));
      } else {
          Redis redis = new Redis();
      }
//      ExecutorService executorService = Executors.newCachedThreadPool();
//
//      final int port = 6379;
//      try(ServerSocket serverSocket = new ServerSocket(port)) {
//          serverSocket.setReuseAddress(true);
//
//          while (!serverSocket.isClosed()) {
//              try {
//                 final Socket clientSocket = serverSocket.accept();
//                  executorService.execute(() -> handle(clientSocket));
//              } catch (Exception e) {
//                  System.out.println("IOException: " + e.getMessage());
//              }
//          }
//      } catch (IOException e) {
//          System.out.println("IOException: " + e.getMessage());
//      } finally {
//          executorService.shutdown();
//      }

  }

//      public static void handle (Socket clientSocket){
//          try (InputStream inputStream = clientSocket.getInputStream()){
//
//              BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
//              OutputStream outputStream = clientSocket.getOutputStream();
////              PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
//
//              String command;
//              while ((command = reader.readLine()) != null) {
//                  if (command.startsWith("*")){
//                      int numOfItems = Integer.parseInt(command.substring(1));
//                      //0 = size of 1, 1 = cmd, 2 = size of 3, 3 = key/echo val, 4 = size of set, 5 = setVal
//                      ArrayList<String> inputs = new ArrayList<>(numOfItems * 2);
//                      for (int i =0; i < numOfItems * 2; i++){
//                          inputs.add(reader.readLine());
//                          System.out.println("element " + i + " is: " + inputs.get(i));
//                      }
//                      String cmd = inputs.get(1);
//                      switch (cmd.toLowerCase()) {
//                          case Constants.CMD_PING ->
//                              outputStream.write( new Ping().print(inputs, cache));
//                          case Constants.CMD_ECHO ->
//                              outputStream.write( new Echo().print(inputs,cache));
//                          case Constants.CMD_SET ->
//                              outputStream.write(new Set().print(inputs, cache));
//                          case Constants.CMD_GET ->
//                              outputStream.write(new Get().print(inputs, cache));
//                      }
//                  }
//
//              }
//          } catch (Exception e) {
//              System.out.println("Issue occurred in handle " + e.getMessage());
//          } finally {
//              try {
//
//                  clientSocket.close();
//              } catch (IOException e){
//                  System.out.println("could not close socket " + e.getMessage());
//              }
//          }
//
//      }
  }
