package Server;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.time.LocalTime;

public class ClientHandler  extends Thread{
	private static DatagramPacket Packet;
	private static LocalTime time;
	private static int ClientHandlerPort;
	private static DatagramSocket ServerSocket;
	
	// constructor to pass the first packet
	public ClientHandler(DatagramPacket FirstPacket, LocalTime FirstTime, int Port){
		this.Packet = FirstPacket;
		this.time = FirstTime;
		this.ClientHandlerPort = Port; // a port on a server 
	}
	
	
	@Override
	public void run() {
		// code to process client request is put here
		//Note that the first step of the Three-way handshake
		try {
			ServerSocket = new DatagramSocket(ClientHandlerPort);
		} catch (SocketException e) {
			System.out.println("Client Handler Failed.");
			System.exit(0);//terminate the thread in the case of port access failure
		}
		Server ClientSession = new Server(Packet, ServerSocket);
		
		ClientSession.HandleClient();
		
//		try {
//			ServerSocket = new DatagramSocket(ClientHandlerPort);
//		} catch (SocketException e) {
//			System.out.println("Client Handler Failed.");
//		}
		
		
		
		//Do the three way handshake

		//System.out.println("Server Listening to Client "+""+" Requests...");
		




		// at the end close the used socket
	}

}
