package Server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.time.LocalTime;

public class ServerListenerThread extends Thread{
	
	private static int MainServerPort;
	private static DatagramPacket Packet;
	private static byte[] ReceiveData = new byte[1024];
	private static DatagramSocket ServerSocket;
	private static LocalTime time;
	private static InetAddress clientAddress;
	private static int clientPort;
	
	public ServerListenerThread(int port) {
		this.MainServerPort = port;
	}
	
	
	public void run(){
		InetAddress inetAddress;
		String hostName= null;
		try {
			inetAddress = InetAddress.getLocalHost();
			hostName = inetAddress.getHostName();
		} catch (UnknownHostException e1) {
			System.out.println("Error in getting the host name.");
		}
		try {
			ServerSocket = new DatagramSocket(MainServerPort);
		} catch (SocketException e) {
			System.out.println("Error: Listening through port-"+MainServerPort+" has failed.");
		}
	    
		while (true) {
			
			System.out.println("Server "+hostName+ " Listening to clients...");
			try {
				Packet = new DatagramPacket(ReceiveData, ReceiveData.length);
				ServerSocket.receive(Packet);
				time = LocalTime.now();
				//add the packet and time to the queue
				Server_Main.PacketQueue.add(Packet);
				Server_Main.TimeQueue.add(time);
				
				clientAddress = Packet.getAddress();
				clientPort = Packet.getPort();
//				String port_string = Server_Main.getport() + "";
//				byte [] sendData= (port_string.getBytes());
//				DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, clientAddress, clientPort);
//				ServerSocket.send(sendPacket); //send the port number to the client
				
			} catch (IOException e) {
				System.out.println("Error: Server Listener failed in receiving the packet");
			}
			
		}	
	}
}
