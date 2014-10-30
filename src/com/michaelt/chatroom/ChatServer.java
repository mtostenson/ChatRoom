package com.michaelt.chatroom;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class ChatServer {

   // Connectivity fields ------------------------------------------------------
   public static final int port = 7000;
   private ServerSocket serverSocket;
   private ServerSocket downloadServerSocket;
   private ServerSocket sendServerSocket;
   Vector<Connection> connections = new Vector<Connection>();

   // Entry point --------------------------------------------------------------
   public static void main(String args[]) {
      ChatServer server = new ChatServer();
      server.startListening(server);
   }

   // Client listener thread ---------------------------------------------------
   public void startListening(ChatServer pChatServer) {
      final ChatServer context = pChatServer;
      Thread listen = new Thread() {
         public void run() {
            try {
               serverSocket = new ServerSocket(port);
               while (true) {
                  System.out.println("Listening on port " + port + "...");
                  Connection newClient = new Connection(context, serverSocket);
                  System.out.println("Client "
                     + newClient.socket.getInetAddress().getHostAddress()
                     + " connected.");
                  connections.add(newClient);
                  newClient.listenToConnection();
                  System.out.println("Total clients: " + connections.size());
               }
            } catch (IOException ioe) {
               System.err.println("here");
               System.err.println(ioe);
            }
         }
      };
      listen.start();
   }

   // Send message to all clients ----------------------------------------------
   public void broadcast(Packet packet) {
      for (Connection client : connections) {
         try {
            client.output.writeObject(packet);
         } catch (IOException ioe) {
            System.err.println("CHECK");
            System.err.println(ioe);
         }
      }
   }

   // Terminate connection with exited client ----------------------------------
   public void dropConnection(Connection pConnection) {
      connections.remove(pConnection);
      System.out.println("Client " + pConnection.name + " has disconnected.");
      System.out.println("Total clients: " + connections.size());
   }

   // Broadcast client entering event ------------------------------------------
   public void sendClientLists() {
      for (Connection connection : connections) {
         broadcast(Packet.sendEnter(connection.name));
      }
   }

   // Retrieve connection object by client name --------------------------------
   public Connection findConnectionByName(String pName) {
      for (Connection connection : connections) {
         if (connection.name.equals(pName))
            return connection;
      }
      return null;
   }

   // Retrieve file from client as byte[] to forward to other client -----------
   public byte[] download(Packet packet) {
      try {
         System.out.format("Initializing download of file \"%s\" now...\n",
            packet.message);
         if (downloadServerSocket == null) {
            downloadServerSocket = new ServerSocket(6505);
         }
         Socket newSocket = downloadServerSocket.accept();
         System.out.println("Connected to client on port 6505...");
         byte[] filebytes = new byte[(int) packet.filesize];
         System.out.format("Allocated byte[%d]...\n", (int) packet.filesize);
         InputStream is = newSocket.getInputStream();
         int bytesRead = is.read(filebytes, 0, filebytes.length);
         int current = bytesRead;
         System.out.println("Starting loop...");
         do {
            System.out.format("byteRead = %d%n", bytesRead);
            bytesRead = is.read(filebytes, current,
               (filebytes.length - current));
            if (bytesRead >= 0)
               current += bytesRead;
         } while (current < filebytes.length);
         System.out.println("Done looping.");
         return filebytes;
      } catch (Exception e) {
         System.err.println("SERVER EXCEPTION: " + e);
         return null;
      }
   }

   // Forward byte[] to recipient ----------------------------------------------
   public void sendToFileToClient(Packet packet, byte[] filebytes) {
      try {
         if (sendServerSocket == null) {
            sendServerSocket = new ServerSocket(6506);
         }
         Socket send_socket = sendServerSocket.accept();
         System.out.println("Connected to recipient on port 6506.");
         OutputStream os = send_socket.getOutputStream();
         System.out.format("Writing %d bytes to client...", filebytes.length);
         os.write(filebytes, 0, filebytes.length);
         os.flush();
         System.out.println("Done");
         os.close();
         send_socket.close();
         System.out.println("Connection closed");
      } catch (Exception e) {
         System.err.println("SERVER EXCEPTION: " + e);
      }
   }
}
