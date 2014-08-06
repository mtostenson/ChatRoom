package com.michaelt.chatroom;

import java.io.Serializable;

public class Packet implements Serializable {

	// Generated serialVersionUID
   private static final long serialVersionUID = 1811522088531690623L;

	enum PACKET_TYPE { MESSAGE, SIGNAL, CLIENT_ENTER, CLIENT_EXIT }	
	
	PACKET_TYPE packet_type 	= null;	
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
	
	public static Packet sendEnter(String pSource) {
		Packet signal_packet = new Packet(PACKET_TYPE.CLIENT_ENTER);
		signal_packet.source = pSource;		
		return signal_packet;
	}
	
	public static Packet sendExit(String pSource) {
		Packet signal_packet = new Packet(PACKET_TYPE.CLIENT_EXIT);
		signal_packet.source = pSource;		
		return signal_packet;
	}

}
