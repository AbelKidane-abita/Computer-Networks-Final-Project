package Server;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.util.Arrays;

// to receive the packet just pass the socket object to listen through that socket
@SuppressWarnings("unused")
public class ReceivePacketThread extends Thread{

	private static DatagramPacket receivePacket;
	private static DatagramSocket serverSocket;
	private static LocalTime time;
	private static boolean DataReceivedSuccessfully = false;
	private byte[] receiveData = new byte[1100];
	private static int PrevioussequenceNo;
	private static int ExpectedsequenceNo;
	// Data related variables of the Packet that is sent/received through the socket
//	private static byte[] sendData = new byte[1100];
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
	private static DatagramPacket sendPacket;
	private static boolean dontchecksequencenum;

	//constructor
	ReceivePacketThread(DatagramSocket serverSocket, int sequenceNo,DatagramPacket sendPacket , boolean dontchecksequencenum){ 
		this.serverSocket = serverSocket; 
		this.ExpectedsequenceNo = GenerateSeqenceNumber(sequenceNo);
		this.sendPacket = sendPacket;
		this.dontchecksequencenum = dontchecksequencenum;
	}
	// getter for the received packet
	public static DatagramPacket getReceivePacket() { return receivePacket; }

	// getter to get the time
	public static LocalTime getTime() { return time; }

	public static boolean getDataReceivedSuccessfully() { return DataReceivedSuccessfully; }
	
	private static int GenerateSeqenceNumber(int receivedSequenceNo) {
		int generatedSequenceNo = receivedSequenceNo + 1;
		if (generatedSequenceNo>999) { generatedSequenceNo=1;}
		return generatedSequenceNo;
	}
	private static void ReadPacket() {

		//		clientAddress = receivePacket.getAddress();
		//		clientPort = receivePacket.getPort();

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
	
	public void run(){
		try {
			while (true) {
				receivePacket = new DatagramPacket(receiveData, receiveData.length);
				serverSocket.receive(receivePacket);
				time = LocalTime.now();
				ReadPacket();
				System.out.println("Expected Sequence Number: "+ExpectedsequenceNo);
				System.out.println("Received Sequence Number: "+ sequenceNo);
				System.out.println("dontchecksequencenum: "+ dontchecksequencenum);
				if(ExpectedsequenceNo==sequenceNo | dontchecksequencenum){
					DataReceivedSuccessfully = true;
//					Thread.sleep(20);
				}
				else {
					//retransmit through the socket 
//					serverSocket.send(sendPacket);
				}
			}
		}catch(Exception mythreadexception) {
			//--thread exception
		}
	}
}