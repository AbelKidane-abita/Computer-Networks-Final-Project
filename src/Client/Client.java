package Client;
import java.io.*;
import java.util.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;

import java.util.concurrent.TimeUnit;

public class Client {

	//-----------------------------------------------------------------------------------------------------
	
	// DEFINING VARIABLES
	
	// Socket related variables
	public static DatagramSocket clientSocket;
	public static DatagramPacket receivePacket;
	public static DatagramPacket sendPacket;
	static InetAddress clientAddress;
	static InetAddress serverAddress = null;
	static int serverPort = 9881;
	static String serverName = "";

	// Data related variables
	static byte[] receiveData = new byte[1024];  
	static byte[] sendData = new byte[1024];
	//header contents
	static String hostIP;
	static String msgType;
	// static int fileNameLength;
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


	// Checking for the success of data reception
	public static boolean DataReceivedSuccessfully = false;


	// Other
	static String message;
	static DataInputStream InputFromTerminal = new DataInputStream(System.in);
	static String userinput;

	//static String hostIP_recievePacket;

	//-----------------------------------------------------------------------------------------------------
	
	//Should add save file here 
	
	public static int GenerateSeqenceNumber(int receivedSequenceNo) {
		int generatedSequenceNo = receivedSequenceNo + 1;
		if (generatedSequenceNo>999) { generatedSequenceNo=1;}
		return generatedSequenceNo;
	}

	//a method to convert an integer to a string of a fixed number of characters
	public static String ConvertInt(int num, int fixedLength) {
		String str = String.format("%0" + fixedLength + "d", num);
		return str;
	}

	public static void PrintPacketContents() throws IOException{
		System.out.println("\n---------------------------------------------");
		System.out.println("\t\t HEADER");
		System.out.println("Host IP: "+hostIP);
		System.out.println("Msg Type: "+msgType);
		System.out.println("File Name: "+fileName);
		System.out.println("Seqence Number: "+sequenceNo);
		System.out.println("Length: "+length);
		System.out.println("---------------------------------------------");
		System.out.println("\t\t BODY");
		String body_contents = new String(bodyData);
		System.out.println("Body Contents: "+ body_contents);
		System.out.println("---------------------------------------------\n");

	}

	public static void ReceivePacket(){
		try {
			clientSocket.receive(receivePacket);
			time = LocalTime.now();
			clientAddress = receivePacket.getAddress();
			serverPort = receivePacket.getPort();
			//DataReceivedSuccessfully = true;

			//define the length of the fields (except for the IP)
			int ip_length = clientAddress.getHostAddress().length();
			int msgType_length = 3;
			int fileName_length_length = 3; //possible no. of characters of 1-999
			int fileName_length = 11; //temporary value during testing, will be changed during runtime
			int sequenceNo_length = 3; // three digit number limit
			int length_length = 4; // 

			byte[] messageData = receivePacket.getData();


			//Assign the values of the header fields
			hostIP = new String(messageData, 0, ip_length, StandardCharsets.UTF_8);

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
			length = (long) Integer.parseInt(length_string);


			// Read the message body
			bodyData  = new byte[(int) length];
			int bodyStartIndex = ip_length + msgType_length + fileName_length_length 
					+fileName_length + sequenceNo_length + length_length ;
			int bodyEndIndex = (int) (bodyStartIndex + length);
			bodyData = Arrays.copyOfRange(messageData, bodyStartIndex, bodyEndIndex);

			System.out.println("Received Packet of Sequence number "+sequenceNo+""+time);
			PrintPacketContents();

		} catch (IOException e) {
			System.out.println("Error: Receiving the Packet stopped.");
		}
	}

	//sending ack
	public static void SendACK() throws IOException {
		sequenceNo = GenerateSeqenceNumber(sequenceNo);
		String body_contents = "ACK";
		bodyData = body_contents.getBytes();
		length = "ACK".length();
		//		SendPacket();
	}
	//generate the Packet
	public static byte[] GeneratePacketClientSide(String hostIP, String messageType, 
			String filename, int SeqNo, long length, byte[] body) {
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

	//send a request to the server to connect
	@SuppressWarnings("deprecation")
	public static void FirstStep() {
		while (serverAddress == null) {
			try {
				clientSocket = new DatagramSocket();
				System.out.print("UDP Client starting on host: " + InetAddress.getLocalHost().getHostName() + 
						". \nType name of UDP server: ");
				serverName = InputFromTerminal.readLine();
				serverAddress = InetAddress.getByName(serverName);//  <----
				hostIP = InetAddress.getLocalHost().getHostAddress();
			} catch (Exception e) {
				System.out.println("Unknown host, please try again.");
			}
		}
		String message = "SYN";
		length = message.length();
		msgType = "SYN";
		fileName = "NULL";
		sequenceNo = 1;
		bodyData = message.getBytes();
		//generate the packet

		//		sendData = GeneratePacketClientSide(hostIP, msgType, fileName, sequenceNo, length, bodyData);
		//		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, serverPort);
		//		try {
		//			clientSocket.send(sendPacket);
		//			SendPacket();
		//			
		//
		//		} catch (IOException e1) {
		//			System.out.print("Error: Sending the packet failed.");
		//		}	
	}
	//receive ack for the request from the server
	public static void SecondStep() {
		//------------------------------------------------------------------------------------------------------------------
		try {
			ReceivePacketWithInterruptAndRetransmissionClient();
		}catch(Exception e) {
			System.out.println("Error in the second part of the 3 way handshake");
		}
	}
	//------------------------------------------------------------------------------------------------------------------
	//send the ack for the ack
	public static void ThirdStep() {
		String message = "ACK";
		msgType = "ACK";
		fileName = "NULL";
		sequenceNo = GenerateSeqenceNumber(sequenceNo);
		length = message.length();
		bodyData = message.getBytes();
		//		try {
		//			SendPacket();
		//		}catch(Exception f) {
		//			System.out.println("Error in the third part of the 3 way handshake");
		//		}
	}

	public static void ThreeWayHandShake() {

		System.out.println("Executing the first step for the three way handshake on the client side");
		FirstStep();

		System.out.println("Executing the second step for the three way handshake on the server side");
		SecondStep();

		System.out.println("Executing the third step for the three way handshake on the server side");
		ThirdStep();
	}

	//Receive input from the terminal
	@SuppressWarnings("deprecation")
	public String ReadFromTerminal()  {
		String TerminalMessage="";
		try {
			TerminalMessage = InputFromTerminal.readLine();
			return TerminalMessage;
		} catch (IOException e) {
			System.out.println("Failed to read input from terminal.");
		}
		return null;
	}
	//Checks if file is available on the specified path below
	public static boolean isFileAvailable(String filename) {
		//C:\Users\YOGA SLIM7\eclipse-workspace\Assignment2
		String folderPath = "C:\\Users\\YOGA SLIM7\\eclipse-workspace\\Assignment2\\";
		File folder = new File(folderPath);
		File file = new File(folder, filename);
		if (file.exists())	return true;
		else	return false;
	}
	//Returns the file size in a String format
	@SuppressWarnings("null")
	public static long Filesize(String filename){
		//
		String folderPath = "C:\\Users\\YOGA SLIM7\\eclipse-workspace\\Assignment2\\"; 
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

	public static void FirstTerminationStep() throws IOException {
		String body_content = "FIN";
		msgType = "FIN";
		fileName = "NULL";
		sequenceNo = GenerateSeqenceNumber(sequenceNo);
		length = body_content.length();
		bodyData = body_content.getBytes();
		//		SendPacket();
		//		SendPacket();
	}
	public static void SecondTerminationStep(){
		try {
			ReceivePacketWithInterruptAndRetransmissionClient();
		} catch (IOException e) {
			System.out.println("Error in the second termination step.");
		}
	}
	public static void ThirdTerminationStep() throws IOException {
		String body_content = "ACK";
		msgType = "ACK";
		sequenceNo = GenerateSeqenceNumber(sequenceNo);
		length = body_content.length();
		bodyData = body_content.getBytes();

		//		SendPacket();
	}
	public static void TerminationSequence() throws IOException {

		//first step - send the request for the termination
		System.out.println("Executing the first step for the termination sequence on the client side");
		FirstTerminationStep();

		//second step - receive the ack
		System.out.println("Executing the second step for the termination sequence on the client side");
		SecondTerminationStep();

		//third step - send the ack for the ack
		System.out.println("Executing the third step for the termination sequence on the client side");
		ThirdTerminationStep();

		clientSocket.close();
		System.out.println("Termination successful");
	}
	@SuppressWarnings({ "removal", "deprecation" })
	public static void ReceivePacketWithInterruptAndRetransmissionClient() throws IOException {

		//		receivePacket = new DatagramPacket(receiveData, receiveData.length);
		//		m  = new ReceivePacketThreadClient(); //Threading
		//		//long startTime = System.currentTimeMillis();
		//		DataReceivedSuccessfully = false;
		//		m.start();
		//		while(true){ //run while the sent ack is not ack'ed back
		//			try {
		//				// check at certain intervals if the packet was received successfully until timeout 
		//				int delay = 0;
		//				while(DataReceivedSuccessfully == false && delay<=3000) { //wait for a maximum of 3 seconds
		//					Thread.sleep(10);//Milliseconds
		//					delay +=10;
		//				}
		//				//suspend or stop the 
		//				if (DataReceivedSuccessfully == false) { // data not received
		//					System.out.println("Time out. Stopping the thread...");
		//					m.suspend();
		//					//code to re-send the packet from the second step
		//					System.out.println("Retransmission");
		//					SendPacket();
		//					//resume the thread
		//					m.resume();
		//				}
		//				else { //data received
		//					m.stop();
		//					//PrintPacketContents();
		//					break;
		//				}
		//			}catch(Exception ie) {
		//				System.out.println("Error in the second part of the 3 way handshake");	
		//			}	
		//		}
		//		//PrintPacketContents();
		//		//address of the sender
		//		clientAddress = receivePacket.getAddress();
		//		serverPort = receivePacket.getPort();	


		//Should add process client request here 
	}




}