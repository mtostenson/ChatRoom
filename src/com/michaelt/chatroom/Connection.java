package com.michaelt.chatroom;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Connection {

	ChatServer server;
	Socket socket;
	ObjectOutputStream output;
	ObjectInputStream input;
	String name;
	
	public Connection(ChatServer pServer, ServerSocket serverSocket) {
		try{
			server = pServer;
			socket = serverSocket.accept();
			output = new ObjectOutputStream(socket.getOutputStream());
			input  = new ObjectInputStream(socket.getInputStream());
		}
		catch (IOException ioe) {
			System.err.println(ioe);
			ioe.printStackTrace();
		}		
	}
	
	public void listenToConnection() {
		Thread clientListener = new Thread() {
			public void run() {
				while(true) {
					try {
						String message = (String)input.readObject();
						if(message.equals("SERVER_SIGNAL_EXIT")) {							
							terminate();
						}
						else {
							server.broadcast(name, message);							
						}	
					}
					catch(Exception e) {
						System.err.println(e);
					}
				}
			}
		};
		clientListener.start();
	}
	
	public void terminate() {
		try {
			server.broadcast("SERVER", name + " has disconnected.");
			server.connections.remove(this);
	      input.close();
	      output.close();
	      socket.close();
      } 
		catch (IOException ioe) {
			System.err.println(ioe);
			ioe.printStackTrace();
      }
	}	
}
