package com.michaelt.chatroom;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

@SuppressWarnings("serial")
public class ChatClient extends JFrame {

	// GUI Elements--------------------------------------------------------------	
	private JTextField  text_input   	 = new JTextField();
	private JTextArea   text_display 	 = new JTextArea();
	private JTextArea	  roster       	 = new JTextArea();
	private JScrollPane display_scroller = new JScrollPane(text_display);
	private JScrollPane roster_scroller  = new JScrollPane(roster); 
	
	// Connectivity fields ------------------------------------------------------
	public static final int port = 7000;
	public static final String hostname = "10.157.101.15";
	private ObjectOutputStream output;
	private ObjectInputStream  input;	
	private Socket connection;	
	String clientName;
	
	// Constructor --------------------------------------------------------------
	public ChatClient() {		
		super("ChatClient");
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		text_display.setEditable(false);
		text_display.setWrapStyleWord(true);
		roster.setEditable(false);
		display_scroller.setPreferredSize(new Dimension(300, getHeight()));
		add(display_scroller, BorderLayout.LINE_START);
		roster_scroller.setPreferredSize(new Dimension(94, getHeight()));
		add(roster_scroller, BorderLayout.LINE_END);
		add(text_input, BorderLayout.PAGE_END);
		setSize(400, 300);
		setVisible(true);
		setResizable(false);
		clientName = JOptionPane.showInputDialog(this, 
															  "Name?", 
															  "Provide name",
															  JOptionPane
															  .INFORMATION_MESSAGE);
		setTitle(clientName + "'s Chat Client");
		addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent we) {
				if(JOptionPane.showConfirmDialog(getParent(), 
            "Are you sure to close this window?", "Really Closing?", 
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
            	try {
            		output.writeObject(Packet.sendSignal(clientName, 
            														 Packet.SIGNAL.EXIT));
            		input.close();
            		output.close();
	               connection.close();
               } catch (IOException pException) {
	               pException.printStackTrace();
               }
            	System.exit(0);
				}
				else {
					setVisible(true);					
				}
			}
		});
		connect();
		text_input.addActionListener(new ActionListener() {
			@Override
         public void actionPerformed(ActionEvent ae) {
				try {
					output.writeObject(Packet.sendMessage(clientName, 
																	  text_input.getText()));
					text_input.setText("");
				}
				catch(Exception e) {
					System.err.println(e);
				}				
         }
		});
	}
	
	// Initialize connection to server ------------------------------------------
	public void connect() {		
		try {
			connection = new Socket(InetAddress.getByName(hostname), port);
			output = new ObjectOutputStream(connection.getOutputStream());
			input  = new ObjectInputStream(connection.getInputStream());			
			output.writeObject(Packet.sendSignal(clientName, Packet.SIGNAL.ENTER));
			output.flush();
		}
		catch(IOException ioe) {
			System.err.println(ioe);
		}		
	}	

	// Wait for packets from server ---------------------------------------------
	public void run() {
		while(true) {
			try{
				Packet packet = (Packet)input.readObject();
				switch(packet.packet_type) {
					case MESSAGE:
						text_display.append(packet.source + 
												  ": " 
												  + packet.message +
												  "\n");
						JScrollBar vertical = display_scroller.getVerticalScrollBar();
						vertical.setValue(vertical.getMaximum());
						break;
					case CLIENT_LIST:
						roster.setText("");
						for(String name : packet.client_list) {
							roster.append(" " + name + "\n");							
						}
						break;
					default:
						System.out.println("Coming soon...");
				}
				
			}
			catch(Exception e) {
				System.err.println(e);
			}
		}
	}
	
	// Entry point --------------------------------------------------------------
	public static void main(String args[]) {
		ChatClient client = new ChatClient();
		client.run();
	}	 
}
