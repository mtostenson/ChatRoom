package com.michaelt.chatroom;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Connection {

   // Fields -------------------------------------------------------------------
   ChatServer server;
   Socket socket;
   ObjectOutputStream output;
   ObjectInputStream input;
   String name;

   // Constructor --------------------------------------------------------------
   public Connection(ChatServer pServer, ServerSocket serverSocket) {
      try {
         server = pServer;
         socket = serverSocket.accept();
         output = new ObjectOutputStream(socket.getOutputStream());
         input = new ObjectInputStream(socket.getInputStream());
      } catch (IOException ioe) {
         System.err.println(ioe);
         ioe.printStackTrace();
      }
   }

   // Fire thread that listens for and handles packets on this connection ------
   public void listenToConnection() {
      Thread clientListener = new Thread() {
         public void run() {
            boolean running = true;
            while (running) {
               try {
                  Packet packet = (Packet) input.readObject();
                  switch (packet.packet_type) {
                  case MESSAGE:
                     server.broadcast(packet);
                     break;
                  case CLIENT_ENTER:
                     name = packet.source;
                     server.sendClientLists();
                     break;
                  case CLIENT_EXIT:
                     terminate();
                     server.broadcast(Packet.sendExit(packet.source));
                     running = false;
                     break;
                  case FILE_REQUEST:
                     System.out.println("FILE REQUEST RECEIVED");
                     System.out.println("Source: " + packet.source);
                     System.out.println("Destination: " + packet.destination);
                     System.out.println("Name: " + packet.message);
                     System.out.println("Size: " + packet.filesize);
                     Connection dest = server
                        .findConnectionByName(packet.destination);
                     byte[] filebytes = server.download(packet);
                     dest.output.writeObject(packet);
                     server.sendToFileToClient(packet, filebytes);
                     break;
                  default:
                     break;
                  }
               } catch (Exception e) {
                  System.err.println("YUP");
                  System.err.println(e);
               }
            }
         }
      };
      clientListener.start();
   }

   // Terminate and clean up connection ----------------------------------------
   public void terminate() {
      try {
         server.dropConnection(this);
         input.close();
         output.close();
         socket.close();
      } catch (IOException ioe) {
         System.err.println(ioe);
         ioe.printStackTrace();
      }
   }
}
