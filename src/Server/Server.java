package Server;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.time.LocalTime;

public class Server {
	
	
	public static String ConvertInt(int num, int fixedLength) {
		String str = String.format("%0" + fixedLength + "d", num);
		return str;
	}
	
	public static int GenerateSeqenceNumber(int receivedSequenceNo) {
		int generatedSequenceNo = receivedSequenceNo + 1;
		if (generatedSequenceNo>999) { generatedSequenceNo=1;}
		return generatedSequenceNo;
	}
	
	public static void SendACK() throws IOException {
//		sequenceNo = GenerateSeqenceNumber(sequenceNo);
//		String body_contents = "ACK";
//		bodyData = body_contents.getBytes();
//		length = "ACK".length();
//		SendPacket();
	}
	
	public static void PrintPacketContents() throws IOException{
//		System.out.println("\n-----------------------------------------------------");
//		System.out.println("\t\t HEADER");
//		System.out.println("Host IP: "+Client_IP);
//		System.out.println("Msg Type: "+msgType);
//		System.out.println("File Name: "+fileName);
//		System.out.println("Seqence Number: "+sequenceNo);
//		System.out.println("Length: "+length);
//		System.out.println("\n-----------------------------------------------------");
//		System.out.println("\t\t BODY");
//		String body_contents = new String (bodyData);
//		System.out.println("Body Contents: "+ body_contents);	
//		System.out.println("\n-----------------------------------------------------");
	}
		

}