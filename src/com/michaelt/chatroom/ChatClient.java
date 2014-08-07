package com.michaelt.chatroom;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

@SuppressWarnings("serial")
public class ChatClient extends JFrame {

	// GUI Elements--------------------------------------------------------------	
	private JTextField  text_input   	 = new JTextField();
	private JTextArea   text_display 	 = new JTextArea();
	private JScrollPane display_scroller = new JScrollPane(text_display);
	private JMenuItem	  send_file		    = new JMenuItem("Send File");
	private JList 		  roster;
	
	// Connectivity fields ------------------------------------------------------
	public static final int port = 7000;
	public static final String hostname = "10.157.101.15";
	private ObjectOutputStream output;
	private ObjectInputStream  input;	
	private Socket connection;	
	String clientName;
	DefaultListModel model;
	
	// Constructor --------------------------------------------------------------
	public ChatClient() {		
		super("ChatClient");
		model = new DefaultListModel();
		roster = new JList(model);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		text_display.setEditable(false);
		text_display.setWrapStyleWord(true);		
		display_scroller.setPreferredSize(new Dimension(300, getHeight()));
		add(display_scroller, BorderLayout.LINE_START);
		roster.setPreferredSize(new Dimension(94, getHeight()));
		add(roster, BorderLayout.LINE_END);
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
            		output.writeObject(Packet.sendExit(clientName));
            		input.close();
            		output.close();
	               connection.close();
               } catch (IOException pException) {
	               pException.printStackTrace();
               } finally {
               	System.exit(0);
               }
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
					output.writeObject(
						Packet.sendMessage(clientName, text_input.getText())
					);
					text_input.setText("");
				}
				catch(Exception e) {
					System.err.println(e);
				}				
         }
		});
		send_file.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				System.out.println("Send File clicked");
				JFileChooser chooser = new JFileChooser();
				int option = chooser.showOpenDialog(getParent());
				if(option == JFileChooser.APPROVE_OPTION) {
					File selection = chooser.getSelectedFile();
					text_display.append("Sending \"" + selection.getName() +
					"\" to " + roster.getSelectedValue().toString() + "...\n");
					try {
						output.writeObject(
							Packet.sendFileRequest(
								clientName, 
								roster.getSelectedValue().toString(), 
								selection.getName(), 
								selection.length()
							)
						);
						sendFileToServer(selection);
					}
					catch(IOException ioe) {
						System.err.println(ioe);
					}
				}
			}			
		});
		roster.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if(SwingUtilities.isRightMouseButton(e) ) {
					JList list = (JList)e.getSource();
					int row = list.locationToIndex(e.getPoint());
					list.setSelectedIndex(row);
					JPopupMenu jp = new JPopupMenu();					
					jp.add(send_file);
					jp.show(list, e.getX(), e.getY());
				}
			}
		});
	}
	
	// Upload file to server ----------------------------------------------------
	private void sendFileToServer(File pFile) {	 
		try{
			Socket file_connection = 
				new Socket(InetAddress.getByName(hostname), 6505);
			System.out.println("Connected to server on port 6505...");
			byte[] filebytes = new byte[(int)pFile.length()];
			System.out.format("Allocated byte[%d]%n", (int)pFile.length());
			FileInputStream fis = new FileInputStream(pFile);
			BufferedInputStream bis = new BufferedInputStream(fis);
			int bytesRead = bis.read(filebytes, 0, filebytes.length);
			System.out.format(
				"Read %d bytes from %s.%n", bytesRead, pFile.getName()
			);
			OutputStream os = file_connection.getOutputStream();
			System.out.println("Writing to outputstream....");
			os.write(filebytes, 0, filebytes.length);
			os.flush();
			System.out.println("Done");
			bis.close();
			os.close();
			file_connection.close();
			System.out.println("Connection closed");
		}
		catch (Exception e) {
			System.err.println("CLIENT EXCEPTION");
			System.err.println(e);
		}
   }
	
	// Initialize connection to server ------------------------------------------
	public void connect() {		
		try {
			connection = new Socket(InetAddress.getByName(hostname), port);
			output = new ObjectOutputStream(connection.getOutputStream());
			input  = new ObjectInputStream(connection.getInputStream());			
			output.writeObject(Packet.sendEnter(clientName));
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
					case CLIENT_ENTER:
						if(!model.contains(packet.source)) {
							model.add(model.size(), packet.source);
							text_display.append(packet.source + " has joined.\n");							
						}
						break;
					case CLIENT_EXIT:
						if(model.contains(packet.source)) {
							model.remove(model.indexOf(packet.source));
							text_display.append(packet.source + " has left.\n");
						}
						break;
					case FILE_REQUEST:
						text_display.append(packet.source + 
												  " wants to send you a file.\n");
						downloadFromServer(packet);
						break;
					default:
						System.out.println("Coming soon...");
				}
				
			}
			catch(Exception e) {				
				System.err.println(e);
				System.exit(0);
			}
		}
	}
	
	// Retrieve the file from server sent by other client -----------------------
	private void downloadFromServer(Packet packet) {
		try {
			Socket download_socket = 
				new Socket(InetAddress.getByName(hostname), 6506);
			System.out.println("Connected to server on port 6506...");
			byte[] filebytes = new byte[(int)packet.filesize];
			System.out.format("Allocated byte[%d]...\n", (int)packet.filesize);
			InputStream is = download_socket.getInputStream();
			FileOutputStream fos = new FileOutputStream(new File(packet.message));
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			int bytesRead = is.read(filebytes, 0, filebytes.length);
			int current = bytesRead;System.out.println("Starting loop...");
			do{
				System.out.format("bytesRead = %d%n", bytesRead);
				bytesRead = 
					is.read(filebytes, current, (filebytes.length - current));
				if(bytesRead >= 0) current += bytesRead;
			} while (current < filebytes.length);
			System.out.println("Done looping.");
			bos.write(filebytes, 0, current);
			System.out.println("Bytes writtin to file.");
			bos.flush();
			fos.close();
			bos.close();
			download_socket.close();
			System.out.println("Connection closed");
			text_display.append("File successfully downloaded :D\n");
		}
		catch (Exception e) {
			System.err.println("CLIENT EXCEPTION: " + e);
		}
   }

	// Entry point --------------------------------------------------------------
	public static void main(String args[]) {
		ChatClient client = new ChatClient();
		client.run();
	}	 
}
