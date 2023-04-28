package Server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.time.LocalTime;

public class ServerListenerThread extends Thread{
	
	private static int MainServerPort;
	private static DatagramPacket Packet;
	private static byte[] ReceiveData = new byte[1024];
	private static DatagramSocket ServerSocket;
	private static LocalTime time;
	
	public ServerListenerThread(int port) {
		this.MainServerPort = port;
	}
	
	
	public void run(){
		
		while (true) {
			
			System.out.println("Server Listening to clients...");
			
			try {
				ServerSocket = new DatagramSocket(MainServerPort);
			} catch (SocketException e) {
				System.out.println("Error: Listening through port-"+MainServerPort+" has failed.");
			}
			
			try {
				
				Packet = new DatagramPacket(ReceiveData, ReceiveData.length);
				ServerSocket.receive(Packet);
				time = LocalTime.now();
				
				//add the packet and time to the queue
				Server_Main.PacketQueue.add(Packet);
				Server_Main.TimeQueue.add(time);
				
			} catch (IOException e) {
				System.out.println("Error: Server Listener failed in receiving the packet");
			}
		}
	}
}
