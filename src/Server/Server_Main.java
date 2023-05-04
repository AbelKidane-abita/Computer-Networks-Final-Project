package Server;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.Queue;
import java.time.LocalTime;

public class Server_Main {

	private static Server ServerName;
	
	// linked lists to store
	public static LinkedList<Server> ClientsList = new LinkedList<Server>();
	public static Queue<DatagramPacket> PacketQueue = new LinkedList<DatagramPacket>();
	public static LinkedList<ClientHandler> HandlerList = new LinkedList<ClientHandler>();
	public static Queue<LocalTime> TimeQueue = new LinkedList<LocalTime>();
	private static int port = 5000;
	private static DatagramSocket ServerSocket;
	
	public static int getport() {
		return port;
	}
	
	private static void Organizer() {
		int NumberofClients=0;
		//use setReceiveddata to update the value of receiveData
		System.out.println("Organizer is running...");
		
		while (true) {
			//add the code to drop the packet if a clientHandler has already been created
//			System.out.println("!PacketQueue.isEmpty()" + !PacketQueue.isEmpty());
			
			if (!PacketQueue.isEmpty()) {
				System.out.println("this line--------------------");
				System.out.println("this line");
				System.out.println(port);
				DatagramPacket packet = PacketQueue.remove(); // is a pop method
				LocalTime time = TimeQueue.remove();
				
				//Start ClientSession
				System.out.println("Client-"+NumberofClients+" session has been created.");
				
				ClientHandler clientHandler = new ClientHandler(packet, time, port); // pass the first packet to the thread
				HandlerList.add(clientHandler);
				
				port+=1;
				clientHandler.start(); //start the client session
				
			}
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private static void StartServer() {
		ServerListenerThread serverlistenerthread = new ServerListenerThread(port);
		port+=1;
		serverlistenerthread.start();
		Organizer();
	}
	
	public static void main(String[] args) {
		StartServer();
	}
}
