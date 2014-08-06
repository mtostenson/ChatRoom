package com.michaelt.chatroom;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Vector;

public class ChatServer {

	// Connectivity fields ------------------------------------------------------
	public static final int port = 7000;
	private ServerSocket serverSocket;
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
					while(true) {
						System.out.println("Listening on port " + port + "...");
						Connection newClient = new Connection(context, serverSocket);
						System.out.println("Client " 
												 + newClient
						                   .socket
						                   .getInetAddress()
						                   .getHostAddress() 
						                   + " connected.");
						connections.add(newClient);						
						newClient.listenToConnection();
						System.out.println("Total clients: " + connections.size());						
					}
				} 
				catch(IOException ioe) { 
					System.err.println("here");
					System.err.println(ioe); 
				}
			}
		};	
		listen.start();
	}	
	
	// Send message to all clients ----------------------------------------------
	public void broadcast(Packet packet) {
		for(Connection client : connections) {
			try {				
				client.output.writeObject(packet);
			}
			catch(IOException ioe) {
				System.err.println("CHECK");
				System.err.println(ioe);
			}
		}
	}

	public void dropConnection(Connection pConnection) {
		connections.remove(pConnection);
		System.out.println("Client " + 
								 pConnection.name + 
								 " has disconnected.");
		System.out.println("Total clients: " + connections.size());
   }

	public void sendClientLists() {
		for(Connection connection : connections) {
			broadcast(Packet.sendEnter(connection.name));
		}
   }
}
