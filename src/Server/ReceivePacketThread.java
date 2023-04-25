package Server;

import java.net.DatagramPacket;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.util.Arrays;

public class ReceivePacketThread extends Thread{
	public void run(){
		try {
			while (true) {
				Server.receivePacket = new DatagramPacket(Server.receiveData, Server.receiveData.length);
				Server.serverSocket.receive(Server.receivePacket);
				Server.time = LocalTime.now();

				Server.clientAddress = Server.receivePacket.getAddress();
				Server.clientPort = Server.receivePacket.getPort();


				//define the length of the fields (except for the IP)
				int ip_length = Server.clientAddress.getHostAddress().length();
				int msgType_length = 3;
				int fileName_length_length = 3; //possible no. of characters of 1-999
				int fileName_length = 11; //file123.txt --11 characters
				int sequenceNo_length = 3;
				int length_length = 4;

				byte[] messageData = Server.receivePacket.getData();

				//Assign the values of the header fields
				Server.Client_IP = new String(messageData, 0, ip_length, StandardCharsets.UTF_8);
				//System.out.println(Server.Client_IP);
				Server.msgType = new String(messageData, ip_length, msgType_length, StandardCharsets.UTF_8);
				//System.out.println(Server.msgType);
				String file_length_string = new String(messageData,ip_length + msgType_length, 
						fileName_length_length, StandardCharsets.UTF_8 );
				//System.out.println(file_length_string);
				fileName_length= Integer.parseInt(file_length_string);

				Server.fileName = new String(messageData,ip_length + msgType_length +fileName_length_length,
						fileName_length,  StandardCharsets.UTF_8 );

				String sequence_number_string = new String(messageData,ip_length + msgType_length + fileName_length_length
						+fileName_length, sequenceNo_length, StandardCharsets.UTF_8 );
				Server.sequenceNo = Integer.parseInt(sequence_number_string);

				String length_string = new String(messageData,ip_length + msgType_length +fileName_length_length
						+ fileName_length + sequenceNo_length, length_length, StandardCharsets.UTF_8 );
				Server.length = Integer.parseInt(length_string);


				// Read the message body
				Server.bodyData  = new byte[(int) Server.length];
				int bodyStartIndex = ip_length + msgType_length + fileName_length_length 
						+fileName_length + sequenceNo_length + length_length ;
				int bodyEndIndex = (int) (bodyStartIndex + Server.length);
				Server.bodyData = Arrays.copyOfRange(messageData, bodyStartIndex, bodyEndIndex);

				System.out.println("Received Packet of Sequence number "+Server.sequenceNo+" Time: "+Server.time);
				System.out.println("RECEIVED PACKET CONTENTS");
				Server.PrintPacketContents();
				Server.DataReceivedSuccessfully = true;
			}
		}catch(Exception mythreadexception) {
//			System.out.println("Receiving the Packet has been suspended.");
//			mythreadexception.printStackTrace();
		}
	}
}