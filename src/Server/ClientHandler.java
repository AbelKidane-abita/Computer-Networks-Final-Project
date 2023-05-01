package Server;

import java.io.IOException;
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
		
		try {
			ServerSocket = new DatagramSocket(ClientHandlerPort);
		} catch (SocketException e) {
			System.out.println("Client Handler Failed.");
			System.exit(0);//terminate the thread in the case of port access failure
		}
		Server ClientSession = new Server(Packet, ServerSocket);
		
		try {
			ClientSession.HandleClient(ClientHandlerPort); //will handle all client requests
		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
		}
	}
}
