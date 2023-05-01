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
	private static DatagramSocket serverSocket;
	private static InetAddress clientAddress;
	private static String ClientHostName;
	private static int clientPort;
	private static DatagramPacket sendPacket;
	private static DatagramPacket receivePacket;

	// Data related variables of the Packet that is sent/received through the socket
	private static byte[] receiveData = new byte[1024];
	private static byte[] sendData = new byte[1024];
	//header contents
	private static String Client_IP;
	private static String HostIP;
	private static String msgType;
	private static int filename_length;
	private static String fileName;
	private static int sequenceNo;
	private static long length;
	//body contents
	private static byte[] bodyData;

	private static String folderPath = "C:\\Users\\abelk\\eclipse-workspace\\Computer-Networks-Final-Project\\";
	private static byte [] partitionData;
	private static int seek=0;


	// Packet content related variables
	private static final int HEADER_SIZE = 1024;// header size
	private static int PACKET_SIZE;

	// Time related 
	private static LocalTime time;

	private static boolean clientconnected = false;
	private static ReceivePacketThread m;

	//-----------------------------------------------------------------------------------------------------
	//Constructor	
	public Server(DatagramPacket Packet, DatagramSocket serverSocket) {
		this.receivePacket = Packet;
		this.serverSocket = serverSocket;

	}

	//-----------------------------------------------------------------------------------------------------
	// Initiate the setters and getters here 


	//-----------------------------------------------------------------------------------------------------

	private static String ConvertInt(int num, int fixedLength) {
		String str = String.format("%0" + fixedLength + "d", num);
		return str;
	}

	private static int GenerateSeqenceNumber(int receivedSequenceNo) {
		int generatedSequenceNo = receivedSequenceNo + 1;
		if (generatedSequenceNo>999) { generatedSequenceNo=1;}
		return generatedSequenceNo;
	}

	private static void SendACK() throws IOException {
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

	private static void SendPacket() throws IOException {
		Client_IP = InetAddress.getLocalHost().getHostAddress();
		sendData = GeneratePacketServerSide(HostIP, msgType, fileName, sequenceNo, length, bodyData);
		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, clientAddress, clientPort);
		serverSocket.send(sendPacket);
		time = LocalTime.now();
		System.out.println("Packet with sequence number " + sequenceNo + " is sent at " + time);

		System.out.println("SENT PACKET CONTENTS.");
		PrintPacketContents();
	}

	public static byte[] GeneratePacketServerSide(String hostIP, String messageType, String filename, 
			int SeqNo, long length, byte[] body) {

		// create a ByteBuffer to hold the message
		ByteBuffer message = ByteBuffer.allocate(HEADER_SIZE ); //change size later
		// set the header fields
		int ip_length = hostIP.length();
		String ip_length_string = ConvertInt(ip_length, 3);
		message.put(ip_length_string.getBytes(StandardCharsets.UTF_8));
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

	private static void ReadPacket() {

		clientAddress = receivePacket.getAddress();
		clientPort = receivePacket.getPort();

		byte[] messageData = receivePacket.getData();
		// define the length of the fields (except for the IP)
		// int ip_length = clientAddress.getHostAddress().length();
		int ip_length_length = 2; // max of 2 digits
		int msgType_length = 3;
		int fileName_length_length = 3; //possible no. of characters of 1-999
		int fileName_length = 11; //file123.txt --11 characters
		int sequenceNo_length = 3;
		int length_length = 4;

		//Assign the values of the header fields
		String ip_length_string = new String(messageData, 0, ip_length_length, StandardCharsets.UTF_8);
		int ip_length = Integer.parseInt(ip_length_string);
		Client_IP = new String(messageData, 0, ip_length, StandardCharsets.UTF_8);
		//System.out.println(UDPServer.Client_IP);
		ip_length+=2;		
		msgType = new String(messageData, ip_length, msgType_length, StandardCharsets.UTF_8);
		//System.out.println(UDPServer.msgType);
		String file_length_string = new String(messageData,ip_length + msgType_length, 
				fileName_length_length, StandardCharsets.UTF_8 );
		//System.out.println(file_length_string);
		fileName_length= Integer.parseInt(file_length_string);

		fileName = new String(messageData,ip_length + msgType_length +fileName_length_length,
				fileName_length,  StandardCharsets.UTF_8 );

		String sequence_number_string = new String(messageData,ip_length + msgType_length + fileName_length_length
				+fileName_length, sequenceNo_length, StandardCharsets.UTF_8 );
		sequenceNo = Integer.parseInt(sequence_number_string);

		String length_string = new String(messageData,ip_length + msgType_length +fileName_length_length
				+ fileName_length + sequenceNo_length, length_length, StandardCharsets.UTF_8 );
		length = Integer.parseInt(length_string);


		// Read the message body
		bodyData  = new byte[(int) length];
		int bodyStartIndex = ip_length + msgType_length + fileName_length_length 
				+fileName_length + sequenceNo_length + length_length ;
		int bodyEndIndex = (int) (bodyStartIndex + length);
		bodyData = Arrays.copyOfRange(messageData, bodyStartIndex, bodyEndIndex);

	}

	//Three way handshake
	private static void ThreeWayHandShake() {
		//first step - receive the request to connect
		System.out.println("Executing the first step for the three way handshake on the server side");
		// FirstStep(); //packet already received in the ServerListenerThread so instead we need the method below

		//Read packet and assign the packet variables
		ReadPacket();

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
	private static void FirstStep() {
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
	private static void SecondStep() {
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
	private static void ThirdStep() {
		try {
			ReceivePacketWithInterruptAndRetransmissionServer() ;
		} catch (Exception e) {
			System.out.println("Error in the Third part of the 3 way handshake");
		}
	}

	@SuppressWarnings({ "removal", "deprecation" })
	//needs to change -- refer to the second version
	private static void ReceivePacketWithInterruptAndRetransmissionServer() throws InterruptedException, IOException {
		ReceivePacketThread m = new ReceivePacketThread(serverSocket, sequenceNo); //Threading
		m.start(); //start the thread
		while(true){ //run while the sent ack is not ack'ed back
			// check at certain intervals if the packet was received successfully until timeout 
			int delay = 0;
			while(m.getDataReceivedSuccessfully() == false && delay<=5000) { //wait for a maximum of 2 seconds
				Thread.sleep(10);//Milliseconds
				delay +=10;
			}
			//suspend or stop the thread
			if (m.getDataReceivedSuccessfully() == false) { // data not received
				m.suspend();
				//code to re-send the packet from the second step
				System.out.println("Retransmission");
				SendPacket();
				//resume the thread
				m.resume();
			}
			else { //data received
				//get the packet from the thread
				receivePacket = m.getReceivePacket();
				ReadPacket();
				m.stop();
				break;
			}
		}		
	}
	//Checks if file is available on the specified path below
	private static boolean isFileAvailable(String filename) {
		//		String folderPath = folderPath; 
		File folder = new File(folderPath);
		File file = new File(folder, filename);
		if (file.exists())	return true;
		else	return false;
	}
	//Returns the file size in a String format
	@SuppressWarnings("null")
	private static long Filesize(String filename){
		//		String folderPath = folderPath; 
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


	private static void ReceivePacket(){

		try {			
			receivePacket = new DatagramPacket(receiveData, 1024);//receiveData.length);
			serverSocket.receive(receivePacket);
			time = LocalTime.now();
			clientAddress = receivePacket.getAddress();
			clientPort = receivePacket.getPort();


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

	private static void TerminationSequence() throws IOException {
		//first step - receive the packet for termination (already implemented in the main) 
		//--COMPLETED

		//second step - send the ack for the termination
		System.out.println("Executing Termination Sequence...");
		SecondTerminationStep();

		//third step - receive an ack for the ack
		ThirdTerminationStep();

	}
	private static void SecondTerminationStep(){
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
	private static void ThirdTerminationStep() throws IOException {
		try {
			ReceivePacketWithInterruptAndRetransmissionServer() ;
		} catch (Exception e) {
			System.out.println("Error in the Third termination step");
		}
	}
	public static int GetPartionsNum(long fileSize) {

		int partitionsize;
		int count=0;

		while (fileSize>0) {
			if (fileSize>1000) {
				partitionsize = 1000;

			}
			else {
				partitionsize = (int)fileSize; //assigns the left over file size to partition size
			}

			fileSize-= partitionsize;
			count+=1;
		}
		return count; //returns the number of partitions
	}

	public static byte [] GetPartition(long partition_size) throws IOException {

		byte [] temp = new byte [(int) partition_size];
		partitionData = temp;
		String filePath = folderPath + fileName;
		RandomAccessFile file = new RandomAccessFile(filePath, "r");
		file.seek(seek);
		file.read(partitionData, 0, (int) partition_size);

		seek += 1000;

		return partitionData;
	}

	//function to return the num of partions, takes file_size as input
	public static void SendPartitions(long file_size) throws IOException, InterruptedException {

		long size_left = file_size;
		int partition_size =0;
		String filePath = folderPath + fileName;
		RandomAccessFile file = new RandomAccessFile(filePath, "r");

		while(size_left >0) {

			if (size_left >1000) {
				partition_size = 1000;

			}
			else {
				partition_size = (int) size_left;
			}
			partitionData = new byte[partition_size];
			partitionData = GetPartition(partition_size);
			file.seek(seek);
			file.read(partitionData, 0, (int) partition_size);
			seek += partition_size;	
			size_left -= partition_size;

			//change the contents of the packet
			sequenceNo = GenerateSeqenceNumber(sequenceNo);
			bodyData = partitionData;
			msgType = "RSP";
			SendPacket();
			ReceivePacketWithInterruptAndRetransmissionServer();
			size_left -= partition_size;
		}
		file.close();
	}

	public static void ProcessGET() throws IOException, InterruptedException {
		if(isFileAvailable(fileName)) {
			long filesize = Filesize(fileName);
			int partitionNum = GetPartionsNum(filesize);
			String partitionNum_string = ""+partitionNum;
			sequenceNo = GenerateSeqenceNumber(sequenceNo);
			msgType = "ACK";
			length = partitionNum_string.length();
			bodyData = partitionNum_string.getBytes();
			SendPacket();
			ReceivePacketWithInterruptAndRetransmissionServer();

		}
		else {
			System.out.println("File requested by client is not available.");
			String body_content = "ERROR FILE DOES NOT EXIST.";
			fileName = "NULL";
			sequenceNo = GenerateSeqenceNumber(sequenceNo);
			length = body_content.length();
			bodyData = body_content.getBytes();
			try {
				SendPacket();
				ReceivePacketWithInterruptAndRetransmissionServer();
			} catch (IOException e) {
				System.out.println("Error: ");
			}
		}
	}

	public static void ProcessPUT() throws IOException, InterruptedException {
		//execute the partition assembler
		String PartitionNum_String = new String(bodyData, StandardCharsets.UTF_8); 
		int PartitionNum = Integer.parseInt(PartitionNum_String);
		FileOutputStream fos = null;
		
		
		for (int i=0;i<PartitionNum;i++) {
			SendACK();
			ReceivePacketWithInterruptAndRetransmissionServer();
			
			fos = new FileOutputStream(fileName);
			int offset = 0;
			fos.write(bodyData, offset, (int)length);
			offset += length;

		}
		fos.close();
	}

	private static void ProcessPacketRequest() {

		if (msgType.equals("GET")) { 
			try {
				ProcessGET();
			} catch (Exception e) {
				System.out.println("Error: Processing GET failed.");
			}
		}

		else if(msgType.equals("PUT")) {
			try {
				ProcessPUT();
			} catch (Exception e) {
				System.out.println("Error: Processing PUT failed.");
			}
		}

		else if (msgType.equals("FIN")) {
			System.out.println("Client Requested to terminate connection.");
			try {
				TerminationSequence();
			} catch (IOException e) {
				System.out.println("Error: TerminationSequence");
			}
			System.out.println("Client Disconnected Successfully.");
		}
		else {
			System.out.println("Cannot process request");
		}
	}

	public static void HandleClient() throws InterruptedException, IOException {

		// Do the three way handshake
		ThreeWayHandShake();
		boolean clientconnected = true;
		while(clientconnected) {
			ReceivePacketWithInterruptAndRetransmissionServer();
			if (msgType.equals("FIN")){
				ProcessPacketRequest();
				clientconnected = false;
				break;
			}
			ProcessPacketRequest();
			System.out.println("Client Request Completed. \nListening for another Request...");	
		}
		serverSocket.close();
		System.out.println("Client Disconnected!");
		System.out.println("____________________________________________________________________________________\n\n");

	}

}