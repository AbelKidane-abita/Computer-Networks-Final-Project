package Server;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.time.LocalTime;

public class Server {

	//-----------------------------------------------------------------------------------------------------

	// DEFINING VARIABLES

	// Socket related variables
	public static DatagramSocket serverSocket;
	public static InetAddress clientAddress;
	public static int clientPort;
	public static DatagramPacket sendPacket;
	public static DatagramPacket receivePacket;

	// Data related variables
	public static byte[] receiveData = new byte[1024];
	public static byte[] sendData = new byte[1024];
	//header contents
	static String Client_IP;
	static String HostIP;
	static String msgType;
	public static int filename_length;
	static String fileName;
	static int sequenceNo;
	static long length;
	//body contents
	static byte[] bodyData;

	// Packet content related variables
	private static final int HEADER_SIZE = 1024;// header size
	public static int PACKET_SIZE;

	// Time related 
	public static LocalTime time;

	// Other
	public static boolean DataReceivedSuccessfully = false;
	public static boolean clientconnected = false;
	public static ReceivePacketThread m;

	//-----------------------------------------------------------------------------------------------------


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
				sequenceNo = GenerateSeqenceNumber(sequenceNo);
				String body_contents = "ACK";
				bodyData = body_contents.getBytes();
				length = "ACK".length();
				SendPacket();
	}

	public static void PrintPacketContents() throws IOException{
				System.out.println("\n-----------------------------------------------------");
				System.out.println("\t\t HEADER");
				System.out.println("Host IP: "+Client_IP);
				System.out.println("Msg Type: "+msgType);
				System.out.println("File Name: "+fileName);
				System.out.println("Seqence Number: "+sequenceNo);
				System.out.println("Length: "+length);
				System.out.println("\n-----------------------------------------------------");
				System.out.println("\t\t BODY");
				String body_contents = new String (bodyData);
				System.out.println("Body Contents: "+ body_contents);	
				System.out.println("\n-----------------------------------------------------");
	}
	
	public static void SendPacket() throws IOException {
		Client_IP = InetAddress.getLocalHost().getHostAddress();
		sendData = GeneratePacketServerSide(HostIP, msgType, fileName, sequenceNo, length, bodyData);
		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, clientAddress, clientPort);
		serverSocket.send(sendPacket);
		time = LocalTime.now();
		System.out.println("Packet with sequence number " + sequenceNo + " is sent at " + time);
		
		System.out.println("SENT PACKET CONTENTS");
		PrintPacketContents();
	}
	
	public static byte[] GeneratePacketServerSide(String hostIP, String messageType, String filename, 
			int SeqNo, long length, byte[] body) {

		// create a ByteBuffer to hold the message
		ByteBuffer message = ByteBuffer.allocate(HEADER_SIZE ); //change size later
		// set the header fields
		message.put(hostIP.getBytes(StandardCharsets.UTF_8));
		message.put(messageType.getBytes(StandardCharsets.UTF_8));
		String filename_length = ConvertInt(filename.length(), 3);
		message.put(filename_length.getBytes(StandardCharsets.UTF_8));
		//message.putInt(SeqNo);
		message.put(filename.getBytes(StandardCharsets.UTF_8));

		String SequenceNo_in_String = ConvertInt(SeqNo, 3);
		message.put(SequenceNo_in_String.getBytes(StandardCharsets.UTF_8));

		String length_in_string = ConvertInt((int)length, 4);
		message.put(length_in_string.getBytes(StandardCharsets.UTF_8));

		// set the message body
		message.put(body);

		return message.array(); // convert the ByteBuffer to a byte array
	}


}