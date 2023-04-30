package Server;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.util.Arrays;

// to receive the packet just pass the socket object to listen through that socket
public class ReceivePacketThread extends Thread{

	private static DatagramPacket receivePacket;
	private static DatagramSocket serverSocket;
	private static LocalTime time;
	private static boolean DataReceivedSuccessfully = false;
	private byte[] receiveData = new byte[1024];

	//constructor
	ReceivePacketThread(DatagramSocket serverSocket){ this.serverSocket = serverSocket; }
	
	// getter for the received packet
	public static DatagramPacket getReceivePacket() { return receivePacket; }

	// getter to get the time
	public static LocalTime getTime() { return time; }

	public static boolean getDataReceivedSuccessfully() { return DataReceivedSuccessfully; }

	public void run(){
		try {
			while (true) {
				receivePacket = new DatagramPacket(receiveData, receiveData.length);
				serverSocket.receive(receivePacket);
				time = LocalTime.now();
				DataReceivedSuccessfully = true;
				Thread.sleep(20);
			}
		}catch(Exception mythreadexception) {
			//--
		}
	}
}