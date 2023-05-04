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
	private static String FileToTransmit;

	// Data related variables of the Packet that is sent/received through the socket
	private static byte[] receiveData = new byte[1100];
	private static byte[] sendData = new byte[1100];
	//header contents
	private static String Client_IP;
	private static String HostIP;
	private static String msgType;
	private static int filename_length;
	private static String fileName;
	private static int sequenceNo = 0;
	private static long length;
	//body contents
	private static byte[] bodyData;

	private static String folderPath = "C:\\Users\\abelk\\eclipse-workspace\\Computer-Networks-Final-Project\\";
	private static byte [] partitionData;
	private static int seek=0;


	// Packet content related variables
	private static final int HEADER_SIZE = 1100;// header size
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

	private static void PrintPacketContents() throws IOException{
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
		try {
			Client_IP = InetAddress.getLocalHost().getHostAddress();
			sendData = GeneratePacketServerSide(HostIP, msgType, fileName, sequenceNo, length, bodyData);
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, clientAddress, clientPort);
			serverSocket.send(sendPacket);
			time = LocalTime.now();
			System.out.println("Packet with sequence number " + sequenceNo + " is sent at " + time);

			System.out.println("SENT PACKET CONTENTS.");
			PrintPacketContents();
		}catch(Exception E) {
			System.out.println("Error: Sending file thorugh the socket failed");
			E.printStackTrace();
		}
	}


	private static byte[] GeneratePacketServerSide(String hostIP, String messageType, String filename, 
			int SeqNo, long length, byte[] body) {

		// create a ByteBuffer to hold the message
		ByteBuffer message = ByteBuffer.allocate(HEADER_SIZE ); //change size later
		// set the header fields
		int ip_length = hostIP.length();
		String ip_length_string = ConvertInt(ip_length, 2);
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
	
	
	private static void ReadPacket1() {

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
		String Client_IP1;
		Client_IP = new String(messageData, 0, ip_length, StandardCharsets.UTF_8);
		//System.out.println(UDPServer.Client_IP);
		ip_length+=2;	
		String msgType1;
		msgType = new String(messageData, ip_length, msgType_length, StandardCharsets.UTF_8);
		//System.out.println(UDPServer.msgType);
		String file_length_string = new String(messageData,ip_length + msgType_length, 
				fileName_length_length, StandardCharsets.UTF_8 );
		//System.out.println(file_length_string);
		fileName_length= Integer.parseInt(file_length_string);

		String fileName1;
		fileName = new String(messageData,ip_length + msgType_length +fileName_length_length,
				fileName_length,  StandardCharsets.UTF_8 );

		String sequence_number_string = new String(messageData,ip_length + msgType_length + fileName_length_length
				+fileName_length, sequenceNo_length, StandardCharsets.UTF_8 );
		int sequenceNo1;
		sequenceNo = Integer.parseInt(sequence_number_string);

		String length_string = new String(messageData,ip_length + msgType_length +fileName_length_length
				+ fileName_length + sequenceNo_length, length_length, StandardCharsets.UTF_8 );
		long length;
		length = Integer.parseInt(length_string);


		// Read the message body
		byte[] bodyData1;
		bodyData  = new byte[(int) length];
		int bodyStartIndex = ip_length + msgType_length + fileName_length_length 
				+fileName_length + sequenceNo_length + length_length ;
		int bodyEndIndex = (int) (bodyStartIndex + length);
		bodyData = Arrays.copyOfRange(messageData, bodyStartIndex, bodyEndIndex);
	}


	
	
	
	
	
	
	
	
	
	
	
	
	
	

	private static void ReadPacket() {

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
		String Client_IP1;
		Client_IP1 = new String(messageData, 0, ip_length, StandardCharsets.UTF_8);
		//System.out.println(UDPServer.Client_IP);
		ip_length+=2;	
		String msgType1;
		msgType1 = new String(messageData, ip_length, msgType_length, StandardCharsets.UTF_8);
		//System.out.println(UDPServer.msgType);
		String file_length_string = new String(messageData,ip_length + msgType_length, 
				fileName_length_length, StandardCharsets.UTF_8 );
		//System.out.println(file_length_string);
		fileName_length= Integer.parseInt(file_length_string);

		String fileName1;
		fileName1 = new String(messageData,ip_length + msgType_length +fileName_length_length,
				fileName_length,  StandardCharsets.UTF_8 );

		String sequence_number_string = new String(messageData,ip_length + msgType_length + fileName_length_length
				+fileName_length, sequenceNo_length, StandardCharsets.UTF_8 );
		int sequenceNo1;
		sequenceNo1 = Integer.parseInt(sequence_number_string);

		String length_string = new String(messageData,ip_length + msgType_length +fileName_length_length
				+ fileName_length + sequenceNo_length, length_length, StandardCharsets.UTF_8 );
		long length1;
		length1 = Integer.parseInt(length_string);


		// Read the message body
		byte[] bodyData1;
		bodyData1  = new byte[(int) length];
		int bodyStartIndex = ip_length + msgType_length + fileName_length_length 
				+fileName_length + sequenceNo_length + length_length ;
		int bodyEndIndex = (int) (bodyStartIndex + length);
		bodyData1 = Arrays.copyOfRange(messageData, bodyStartIndex, bodyEndIndex);


		int expectedSequenceNo = GenerateSeqenceNumber(sequenceNo1);
		if (expectedSequenceNo==sequenceNo1) {
			Client_IP = Client_IP1;
			msgType = msgType1;
			fileName = fileName1;
			sequenceNo = sequenceNo1;
			length = length1;
			bodyData = bodyData1;
			NEWDataReceivedSuccessfully = true;
		}

	}

	private static boolean NEWDataReceivedSuccessfully = false;

	//Three way handshake
	private static void ThreeWayHandShake() {
		try {
			HostIP = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		}
		//first step - receive the request to connect
		System.out.println("Executing the first lstep for the three way handshake on the server side");
		// FirstStep(); //packet already received in the ServerListenerThread so instead we need the method below

		//Read packet and assign the packet variables
		ReadPacket1();

//		if (sequenceNo!=1) { 
////			SecondTerminationStep();
//			System.out.println("First sequence number isn't 0");
//		}

		if(true) { //was else before
			//second step -send the ack for the received request for confirmation
			System.out.println("Executing the second step for the three way handshake on the server side");
			SecondStep();

			//third step - receive the ack for the ack
			System.out.println("Executing the third step for the three way handshake on the server side");
			ThirdStep();
			clientconnected = true;
			System.out.println("Three way handshake Successful!");
		}
		sequenceNo = 3; 
//		System.out.println("SequenceNumber Reset: "+sequenceNo);
		System.out.println(">>SequenceNumber After finishing threeway handshake: "+sequenceNo);
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
		//		fileName = "NULL";
		bodyData = port_string.getBytes(); //port number passed from ClientHandler
		length = port_string.length();
		sequenceNo = GenerateSeqenceNumber(sequenceNo);
		clientAddress = receivePacket.getAddress();
		clientPort = receivePacket.getPort();
		System.out.println("Client details: " +clientAddress+ clientPort);

		try {
			SendPacket();
			//			SendPacket();
		}catch(Exception f) {
			System.out.println("Error in the second part of the 3 way handshake");
		}
	}

	private static void ThirdStep() {
		try {
			ReceivePacketWithInterruptAndRetransmissionServer() ;
//			PrintPacketContents();
		} catch (Exception e) {
			System.out.println("Error in the Third part of the 3 way handshake");
		}
	}

	private static void ConfirmReceptionofAckPacket() throws InterruptedException, IOException {
		boolean dontchecksequencenum = true;
		ReceivePacketThread m = new ReceivePacketThread(serverSocket, sequenceNo,sendPacket, dontchecksequencenum); //Threading
		m.start();
		while(true){ //run while the sent ack is not ack'ed back
			// check at certain intervals if the packet was received successfully until timeout 
			int delay = 0;
			while(m.getDataReceivedSuccessfully() == false && delay<=4000) { //wait for a maximum of 2 seconds
				Thread.sleep(10);//Milliseconds
				delay +=10;
			}
			//suspend or stop the thread
			if (m.getDataReceivedSuccessfully() == true) { // data not received
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

	@SuppressWarnings({ "removal", "deprecation" })
	//needs to change -- refer to the second version
	private static void ReceivePacketWithInterruptAndRetransmissionServer() throws InterruptedException, IOException {
		boolean dontchecksequencenum = false;
		ReceivePacketThread m = new ReceivePacketThread(serverSocket, sequenceNo,sendPacket, dontchecksequencenum); //Threading
		m.start(); //start the thread
		while(true){ //run while the sent ack is not ack'ed back
			// check at certain intervals if the packet was received successfully until timeout 
			int delay = 0;
			while(m.getDataReceivedSuccessfully() == false && delay<=2000) { //wait for a maximum of 2 seconds
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
				System.out.println("rECEIVED pACKET");
				PrintPacketContents();
				break;
			}
		}		
	}
	//Checks if file is available on the specified path below
	private static boolean isFileAvailable(String filename) {
		//		String folderPath = folderPath; 
		File folder = new File(folderPath);
		File file = new File(folder, filename);
		if (file.exists())	{return true;}
		else	{return false;}
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
			receivePacket = new DatagramPacket(receiveData, 1100);//receiveData.length);
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
		//		fileName = "NULL";
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
	private static int GetPartionsNum(long fileSize) {

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

	private static byte [] GetPartition(long partition_size) throws IOException {

		byte [] temp = new byte [(int) partition_size];
		partitionData = temp;
		String filePath = folderPath + FileToTransmit;
		RandomAccessFile file = new RandomAccessFile(filePath, "r");
		file.seek(seek);
		file.read(partitionData, 0, (int) partition_size);

		seek += partition_size;

		return partitionData;
	}

	//function to return the num of partions, takes file_size as input
	private static void SendPartitions(long file_size) throws IOException, InterruptedException {

		long size_left = Filesize(FileToTransmit);
		System.out.println("Transmitting File size of: "+ size_left );
		int partition_size =0;
		String filePath = folderPath + FileToTransmit;
		System.out.println("filepath-----------"+ filePath);
		//		RandomAccessFile file = new RandomAccessFile(filePath, "r");
		FileInputStream fis = null;
		//		long File_length = file_size; //change the length
		fileName = FileToTransmit;

		byte [] File_Data = new byte[(int) size_left];

		try {
			fis = new FileInputStream(FileToTransmit);
			fis.read(File_Data); //change the body
			fis.close();
		}catch(IOException file_exp) {
			System.out.println("Error in reading the file.");
		}

		int bodyStartIndex = 0;
		int bodyEndIndex = 0;
		int count = 0;
		while(size_left >0) {

			if (size_left >1000) {
				partition_size = 1000;
			}
			else {
				partition_size = (int) size_left;
			}

			size_left -= partition_size;
			//change the contents of the packet
			sequenceNo = GenerateSeqenceNumber(sequenceNo);
			length = partition_size;
			bodyEndIndex = bodyStartIndex+partition_size;

			System.out.println("size_left: "+size_left);
			System.out.println("partition_size: "+partition_size);
			System.out.println("bodyStartIndex: "+bodyStartIndex);
			System.out.println("bodyEndIndex: "+bodyEndIndex);
			System.out.println("File_Data: "+File_Data.length);

			bodyData = new byte[partition_size];
			bodyData = Arrays.copyOfRange(File_Data, bodyStartIndex, bodyEndIndex);
			bodyStartIndex+=partition_size;
			msgType = "RSP";
			count+=1;
			System.out.println("Partition "+count+" sent to$$$$$$$$$$$$$$ ClientAddress: "+ clientAddress);
			SendPacket();//f
			ReceivePacketWithInterruptAndRetransmissionServer();//f
			System.out.println("My Sequency Number after receiving an ACK"+count+1+": "+sequenceNo);

		}
		//		file.close();
	}

	private static void ProcessGET() throws IOException, InterruptedException {
		System.out.println("File: "+fileName+" exist: "+isFileAvailable(fileName));
		FileToTransmit = fileName;
		if(isFileAvailable(fileName)) {
			long filesize = Filesize(fileName);
			int partitionNum = GetPartionsNum(filesize);
			String partitionNum_string = ""+partitionNum;
			sequenceNo = GenerateSeqenceNumber(sequenceNo);
			msgType = "ACK";
			length = partitionNum_string.length();
			bodyData = partitionNum_string.getBytes();
			SendPacket();  //--f

			ReceivePacketWithInterruptAndRetransmissionServer(); //--f

			//transmit the file partitions 
			System.out.println("Starting to transmit files to client...");
			SendPartitions(filesize);
		}
		else {
			System.out.println("File requested by client is not available.");
			String body_content = "ERROR FILE DOES NOT EXIST.";
			//			fileName = "NULL";
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

	private static void ProcessPUT() throws IOException, InterruptedException {
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
				e.printStackTrace();
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
	private static void ReceivePacketwithoutTimeout(){

		NEWDataReceivedSuccessfully = false;
		receivePacket = new DatagramPacket(receiveData, receiveData.length);
		while(true){
			try {
				serverSocket.receive(receivePacket);
				time = LocalTime.now();
				ReadPacket();
				if(NEWDataReceivedSuccessfully) {
					break;
				}
				else {
//					SendPacket();
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
	}

	public static String port_string=null;
	public static void HandleClient(int ClientHandlerPort) throws InterruptedException, IOException {
		port_string = ClientHandlerPort+"";
		String port = ClientHandlerPort+"";
		// Do the three way handshake
		ThreeWayHandShake();
		System.out.println("_____________________________________________________________________________________________");
		boolean clientconnected = true;
		while(clientconnected) {
			ReceivePacketwithoutTimeout();
			System.out.println("###########################Sequencenumber: "+ sequenceNo);
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