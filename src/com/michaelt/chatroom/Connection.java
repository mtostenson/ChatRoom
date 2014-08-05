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
						server.broadcast(name, (String)input.readObject());
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
