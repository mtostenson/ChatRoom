package com.michaelt.chatroom;

import java.io.Serializable;
import java.util.Vector;

public class Packet implements Serializable {

	// Generated serialVersionUID
   private static final long serialVersionUID = 1811522088531690623L;

	enum PACKET_TYPE { MESSAGE, CLIENT_LIST, SIGNAL }
	enum SIGNAL { ENTER, EXIT }
	
	PACKET_TYPE packet_type 	= null;
	SIGNAL signal 					= null;
	Vector<String> client_list = null;
	String message 				= null;
	String source					= null;

	private Packet(PACKET_TYPE pType) {		
		packet_type = pType;		
	}
	
	public static Packet sendMessage(String pSource, String pMessage) {
		Packet message_packet = new Packet(PACKET_TYPE.MESSAGE);
		message_packet.source = pSource;
		message_packet.message = pMessage;
		return message_packet;
	}
	
	public static Packet sendSignal(String pSource, SIGNAL pSignal) {
		Packet signal_packet = new Packet(PACKET_TYPE.SIGNAL);
		signal_packet.source = pSource;
		signal_packet.signal = pSignal;
		return signal_packet;
	}
	
	public static Packet sendClientList(String pSource, Vector<String> pList) {
		Packet client_list_packet = new Packet(PACKET_TYPE.CLIENT_LIST);
		client_list_packet.source = pSource;
		client_list_packet.client_list = pList;
		return client_list_packet;
	}
}
