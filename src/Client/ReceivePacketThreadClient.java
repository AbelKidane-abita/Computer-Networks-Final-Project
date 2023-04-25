package Client;

import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.util.Arrays;

public class ReceivePacketThreadClient extends Thread{
	public void run(){
		try {
			while (true) {

				Client.clientSocket.receive(Client.receivePacket);
				Client.time = LocalTime.now();
				Client.clientAddress = Client.receivePacket.getAddress();
				Client.serverPort = Client.receivePacket.getPort();

				//define the length of the fields (except for the IP)
				int ip_length = Client.clientAddress.getHostAddress().length();
				int msgType_length = 3;
				int fileName_length_length = 3; //possible no. of characters of 1-999
				int fileName_length = 11; //file123.txt --11 characters
				int sequenceNo_length = 3;
				int length_length = 4;

				byte[] messageData = Client.receivePacket.getData();

				//Assign the values of the header fields
				Client.hostIP = new String(messageData, 0, ip_length, StandardCharsets.UTF_8);

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
				Client.PrintPacketContents();
			}
		}catch(Exception mythreadexception) {
			//System.out.println("Stopped Listening by Exception");
			//mythreadexception.printStackTrace();
		}
	}
}