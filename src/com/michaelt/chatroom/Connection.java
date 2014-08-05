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
				boolean running = true;
				while(running) {
					try {
						Packet packet = (Packet)input.readObject();
						switch(packet.packet_type) {
							case MESSAGE:
								server.broadcast(packet);
								break;
							case SIGNAL:
							{
								switch(packet.signal) {
									case EXIT:
										terminate();										
										server.updateClientLists();
										running = false;
										break;
									default:
								}
								break;
							}
						case CLIENT_LIST:
							break;
						case NAME:
							name = packet.source;
							server.broadcast(Packet.sendMessage("SERVER", name + " has entered."));
							server.updateClientLists();
							break;
						default:
							break;
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
			server.dropConnection(this);		
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
