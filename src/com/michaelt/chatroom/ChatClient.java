package com.michaelt.chatroom;

import java.awt.BorderLayout;
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
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

@SuppressWarnings("serial")
public class ChatClient extends JFrame {

	// GUI Elements--------------------------------------------------------------	
	private JTextField  text_input   = new JTextField();
	private JTextArea   text_display = new JTextArea();
	private JScrollPane scroller     = new JScrollPane(text_display);
	
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
		text_display.setEditable(false);
		add(text_input, BorderLayout.SOUTH);
		add(scroller);
		setSize(400, 300);
		setVisible(true);
		clientName = JOptionPane.showInputDialog(this, 
															  "Name?", 
															  "Provide name",
															  JOptionPane
															  .INFORMATION_MESSAGE);
		addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent we) {
				if(JOptionPane.showConfirmDialog(getParent(), 
            "Are you sure to close this window?", "Really Closing?", 
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
            	try {
            		output.writeObject("SERVER_SIGNAL_EXIT");
            		input.close();
            		output.close();
	               connection.close();
               } catch (IOException pException) {
	               pException.printStackTrace();
               }
            	System.exit(0);
				}
			}
		});
		connect();
		text_input.addActionListener(new ActionListener() {
			@Override
         public void actionPerformed(ActionEvent ae) {
				try {
					output.writeObject(text_input.getText());
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
			output.writeObject(clientName);
			output.flush();
		}
		catch(IOException ioe) {
			System.err.println(ioe);
		}		
	}	

	// Wait for broadcasts from server ------------------------------------------
	public void run() {
		while(true) {
			try{
				String message = (String)input.readObject();
				text_display.append(message);				
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
