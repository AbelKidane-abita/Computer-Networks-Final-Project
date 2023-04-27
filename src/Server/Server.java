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
	public static String ClientHostName;
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

	//Three way handshake
	public static void ThreeWayHandShake() {
		//first step - receive the request to connect
		System.out.println("Executing the first step for the three way handshake on the server side");
		FirstStep();
		if (sequenceNo!=1) {
			SecondTerminationStep();			
		}
		else {
			//second step -send the ack for the received request for confirmation
			System.out.println("Executing the second step for the three way handshake on the server side");
			SecondStep();

			//third step - receive the ack for the ack
			System.out.println("Executing the third step for the three way handshake on the server side");
			ThirdStep();
			clientconnected = true;
			System.out.println("Three way handshake Successful!");
		}
	}
	public static void FirstStep() {
		try {
			System.out.println("UDP Server starting at host: " + InetAddress.getLocalHost().getHostName() + 
					", waiting to be contacted by a Client...");

			receivePacket = new DatagramPacket(receiveData, receiveData.length);
			HostIP = InetAddress.getLocalHost().getHostAddress();
			ReceivePacket(); //Continuously listens for the first connection

		}catch(Exception e) {
			System.out.println("Error in the first part of the 3 way handshake");
		}
	}
	public static void SecondStep() {
		msgType = "ACK";
		fileName = "NULL";
		String message = "ACK";
		length = message.length();
		bodyData = message.getBytes();
		sequenceNo = GenerateSeqenceNumber(sequenceNo);
		try {
			SendPacket();
		}catch(Exception f) {
			System.out.println("Error in the second part of the 3 way handshake");
		}
	}
	public static void ThirdStep() {
		try {
			ReceivePacketWithInterruptAndRetransmissionServer() ;
		} catch (Exception e) {
			System.out.println("Error in the Third part of the 3 way handshake");
		}
	}

	
	public static void ReceivePacketWithInterruptAndRetransmissionServer2() throws InterruptedException, IOException {
		DataReceivedSuccessfully = false;
		// check if the packet was received successfully
		
		//
		
	}
	
	
	
	@SuppressWarnings({ "removal", "deprecation" })
	//needs to change -- refer to the second version
	public static void ReceivePacketWithInterruptAndRetransmissionServer() throws InterruptedException, IOException {
		ReceivePacketThread m = new ReceivePacketThread(); //Threading
		DataReceivedSuccessfully = false;
		m.start(); //start the thread
		while(true){ //run while the sent ack is not ack'ed back
			// check at certain intervals if the packet was received successfully until timeout 
			int delay = 0;
			while(DataReceivedSuccessfully == false && delay<=5000) { //wait for a maximum of 2 seconds
				Thread.sleep(10);//Milliseconds
				delay +=10;
			}
			//suspend or stop the 
			if (DataReceivedSuccessfully == false) { // data not received
				m.suspend();
				//code to re-send the packet from the second step
				System.out.println("Retransmission");
				SendPacket();
				//resume the thread
				m.resume();
			}
			else { //data received
				m.stop();
				break;
			}
		}		
	}
	//Checks if file is available on the specified path below
	public static boolean isFileAvailable(String filename) {
		String folderPath = "C:\\Users\\abelk\\eclipse-workspace\\Computer_Networks_Assignment_2\\"; 
		File folder = new File(folderPath);
		File file = new File(folder, filename);
		if (file.exists())	return true;
		else	return false;
	}
	//Returns the file size in a String format
	@SuppressWarnings("null")
	public static long Filesize(String filename){
		String folderPath = "C:\\Users\\abelk\\eclipse-workspace\\Computer_Networks_Assignment_2\\"; 
		File folder = new File(folderPath);
		File file = new File(folder, filename);
		if (file.exists()){
			long filesize = file.length();
			return filesize;
		}
		else{
			return (Long) null;
		}
	}

	public static void ReceivePacket(){

		try {			
			receivePacket = new DatagramPacket(receiveData, 1024);//receiveData.length);
			serverSocket.receive(receivePacket);
			time = LocalTime.now();
			clientAddress = receivePacket.getAddress();
			clientPort = receivePacket.getPort();
			DataReceivedSuccessfully = true;

			//define the length of the fields (except for the IP)
			int ip_length = clientAddress.getHostAddress().length();
			int msgType_length = 3;
			int fileName_length_length = 3; //possible no. of characters of 1-999
			int fileName_length = 11; //file123.txt --11 characters
			int sequenceNo_length = 3;
			int length_length = 4;

			byte[] messageData = receivePacket.getData();

			//Assign the values of the header fields
			Client_IP = new String(messageData, 0, ip_length, StandardCharsets.UTF_8);

			msgType = new String(messageData, ip_length, msgType_length, StandardCharsets.UTF_8);

			String file_length_string = new String(messageData,ip_length + msgType_length, 
					fileName_length_length, StandardCharsets.UTF_8 );
			fileName_length= Integer.parseInt(file_length_string);

			fileName = new String(messageData,ip_length + msgType_length +fileName_length_length,
					fileName_length,  StandardCharsets.UTF_8 );

			String sequence_number_string = new String(messageData,ip_length + msgType_length + fileName_length_length
					+fileName_length, sequenceNo_length, StandardCharsets.UTF_8 );

			sequenceNo = Integer.parseInt(sequence_number_string);

			String length_string = new String(messageData,ip_length + msgType_length +fileName_length_length
					+ fileName_length + sequenceNo_length, length_length, StandardCharsets.UTF_8 );
			length = (long)Integer.parseInt(length_string);


			// Read the message body
			bodyData  = new byte[(int) length];
			int bodyStartIndex = ip_length + msgType_length + fileName_length_length 
					+fileName_length + sequenceNo_length + length_length ;
			int bodyEndIndex = (int) (bodyStartIndex + length);
			bodyData = Arrays.copyOfRange(messageData, bodyStartIndex, bodyEndIndex);

			System.out.println("Received Packet of Sequence number "+sequenceNo+" Time: "+time);
			System.out.println("RECEIVED PACKET CONTENTS");
			PrintPacketContents();		

		} catch (IOException e) {
			System.out.println("Error: Receiving the Packet stopped.");
		}
	}

	public static void TerminationSequence() throws IOException {
		//first step - receive the packet for termination (already implemented in the main) 
		//--COMPLETED

		//second step - send the ack for the termination
		System.out.println("Executing Termination Sequence...");
		SecondTerminationStep();

		//third step - receive an ack for the ack
		ThirdTerminationStep();

	}
	public static void SecondTerminationStep(){
		String body_content = "ACK";
		fileName = "NULL";
		msgType = "ACK";
		sequenceNo = GenerateSeqenceNumber(sequenceNo);
		length = body_content.length();
		bodyData = body_content.getBytes();
		try {
			SendPacket();//-FIX
			SendPacket();
		} catch (IOException e) {
			System.out.println("Error in the Second termination step");
		}
	}
	public static void ThirdTerminationStep() throws IOException {
		try {
			ReceivePacketWithInterruptAndRetransmissionServer() ;
		} catch (Exception e) {
			System.out.println("Error in the Third termination step");
		}
	}

}