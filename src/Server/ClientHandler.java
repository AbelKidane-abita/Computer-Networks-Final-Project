package Server;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.time.LocalTime;

public class ClientHandler implements Runnable {
	private static DatagramPacket Packet;
	private static LocalTime time;
	private static int ClientHandlerPort;
	// constructor to pass the first packet
	public ClientHandler(DatagramPacket FirstPacket, LocalTime FirstTime, int Port){
		this.Packet = FirstPacket;
		this.time = FirstTime;
		this.ClientHandlerPort = Port;
	}
	
	@Override
	public void run() {
		// code to process client request is put here

		//Do the three way handshake
		

		System.out.println("Server Listening to Client "+""+" Requests...");
		




		// at the end close the used socket
	}

}
