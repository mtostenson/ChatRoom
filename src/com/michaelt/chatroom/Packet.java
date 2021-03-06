package com.michaelt.chatroom;

import java.io.Serializable;

public class Packet implements Serializable {

   // Generated serialVersionUID
   private static final long serialVersionUID = 1811522088531690623L;

   enum PACKET_TYPE {
      MESSAGE, SIGNAL, CLIENT_ENTER, CLIENT_EXIT, FILE_REQUEST
   }

   PACKET_TYPE packet_type = null;
   String message = null;
   String source = null;
   String destination = null;
   long filesize;

   private Packet(PACKET_TYPE pType) {
      packet_type = pType;
   }

   public static Packet sendMessage(String pSource, String pMessage) {
      Packet message_packet = new Packet(PACKET_TYPE.MESSAGE);
      message_packet.message = pMessage;
      message_packet.source = pSource;
      return message_packet;
   }

   public static Packet sendEnter(String pSource) {
      Packet enter_packet = new Packet(PACKET_TYPE.CLIENT_ENTER);
      enter_packet.source = pSource;
      return enter_packet;
   }

   public static Packet sendExit(String pSource) {
      Packet exit_packet = new Packet(PACKET_TYPE.CLIENT_EXIT);
      exit_packet.source = pSource;
      return exit_packet;
   }

   public static Packet sendFileRequest(String pSource, String pDest,
      String pName, long pSize) {
      Packet file_request_packet = new Packet(PACKET_TYPE.FILE_REQUEST);
      file_request_packet.destination = pDest;
      file_request_packet.source = pSource;
      file_request_packet.message = pName;
      file_request_packet.filesize = pSize;
      return file_request_packet;
   }

}
