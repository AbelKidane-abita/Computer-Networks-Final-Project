package Client;

import java.io.File;
import java.net.DatagramPacket;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.util.Arrays;

public class ReceivePacketThreadClient extends Thread{



	private static int sequenceNo;
	private static int ExpectedsequenceNo;
	private static boolean dontchecksequencenum;

	//constructor
	ReceivePacketThreadClient(int sequenceNo, boolean dontchecksequencenum){
		this.ExpectedsequenceNo = GenerateSeqenceNumber(sequenceNo);
		this.dontchecksequencenum = dontchecksequencenum;
	}

	
	private static int GenerateSeqenceNumber(int receivedSequenceNo) {
		int generatedSequenceNo = receivedSequenceNo + 1;
		if (generatedSequenceNo>999) { generatedSequenceNo=1;}
		return generatedSequenceNo;
	}
	private static void ReadPacket() {

		//		clientAddress = receivePacket.getAddress();
		//		clientPort = receivePacket.getPort();

		byte[] messageData = Client.receivePacket.getData();
		// define the length of the fields (except for the IP)
		// int ip_length = clientAddress.getHostAddress().length();
		int ip_length_length = 2; // max of 2 digits
		int msgType_length = 3;
		int fileName_length_length = 3; //possible no. of characters of 1-999
		int fileName_length = 11; //file123.txt --11 characters
		int sequenceNo_length = 3;
		int length_length = 4;

		String Client_IP;
		String msgType;
		String fileName;
		long length;
		byte[] bodyData;

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
				Client.receivePacket = new DatagramPacket(Client.receiveData, Client.receiveData.length);
				Client.clientSocket.receive(Client.receivePacket);
				System.out.println("A packet has been received through the port socket###########");
				Client.time = LocalTime.now();
				ReadPacket();
				System.out.println("Expected Sequence Number: "+ExpectedsequenceNo);
				System.out.println("Received Sequence Number: "+ sequenceNo);
				System.out.println("dontchecksequencenum: "+ dontchecksequencenum);
				if(ExpectedsequenceNo == sequenceNo || dontchecksequencenum) {
					Client.clientAddress = Client.receivePacket.getAddress();
					Client.serverPort = Client.receivePacket.getPort();

					//define the length of the fields (except for the IP)
//					int ip_length = Client.clientAddress.getHostAddress().length();
					
					int ip_length_length = 2; 
					int msgType_length = 3;
					int fileName_length_length = 3; //possible no. of characters of 1-999
					int fileName_length = 11; //file123.txt --11 characters
					int sequenceNo_length = 3;
					int length_length = 4;

					byte[] messageData = Client.receivePacket.getData();

					//Assign the values of the header fields
					String ip_length_string = new String(messageData, 0, ip_length_length, StandardCharsets.UTF_8);
					int ip_length = Integer.parseInt(ip_length_string);
					Client.hostIP = new String(messageData, ip_length_length, ip_length, StandardCharsets.UTF_8);
					ip_length+=2;

					Client.msgType = new String(messageData, ip_length, msgType_length, StandardCharsets.UTF_8);

					String file_length_string = new String(messageData,ip_length + msgType_length, 
							fileName_length_length, StandardCharsets.UTF_8 );
					fileName_length= Integer.parseInt(file_length_string);

					Client.fileName = new String(messageData,ip_length + msgType_length +fileName_length_length,
							fileName_length,  StandardCharsets.UTF_8 );

					String sequence_number_string = new String(messageData,ip_length + msgType_length + fileName_length_length
							+fileName_length, sequenceNo_length, StandardCharsets.UTF_8 );
					Client.sequenceNo = Integer.parseInt(sequence_number_string);

					String length_string = new String(messageData,ip_length + msgType_length +fileName_length_length
							+ fileName_length + sequenceNo_length, length_length, StandardCharsets.UTF_8 );
					Client.length = (long) Integer.parseInt(length_string);


					// Read the message body
					Client.bodyData  = new byte[(int) Client.length];
					int bodyStartIndex = ip_length + msgType_length + fileName_length_length 
							+fileName_length + sequenceNo_length + length_length ;
					int bodyEndIndex = (int) (bodyStartIndex + Client.length);
					Client.bodyData = Arrays.copyOfRange(messageData, bodyStartIndex, bodyEndIndex);

					System.out.println("Received Packet of Sequence number "+ Client.sequenceNo+" Time: "+ Client.time);
					Client.DataReceivedSuccessfully = true;
//					Client.PrintPacketContents();
				}
				
				else{
					Client.SendPacket();
				}
	
				
			}
		}catch(Exception mythreadexception) {
			//System.out.println("Stopped Listening by Exception");
			//mythreadexception.printStackTrace();
		}
	}
}