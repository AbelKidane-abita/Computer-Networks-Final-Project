package Server;

import java.net.DatagramPacket;
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
	
	private static void Organizer() {
				
		//use setReceiveddata to update the value of receiveData
		while (true) {
			//add the code to drop the packet if a clientHandler has already been created
			if (!PacketQueue.isEmpty()) {
				DatagramPacket packet = PacketQueue.poll(); // is a pop method
				LocalTime time = TimeQueue.poll();
				
				//Start ClientSession
				ClientHandler clientHandler = new ClientHandler(packet, time, port); // pass the first packet to the thread
				HandlerList.add(clientHandler);
				
				port+=1;
				clientHandler.start(); //start the client session
				
			}
		}
	}
	
	
	private static void InitiateServerListenerThread() {
		ServerListenerThread serverlistenerthread = new ServerListenerThread(port);
		port+=1;
		serverlistenerthread.start();
	}
	

	public static void main(String[] args) {
		InitiateServerListenerThread();
		Organizer();
	}
}
