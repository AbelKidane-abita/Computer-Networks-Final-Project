package Server;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class ClientHandler implements Runnable {
	private Server ServerName;
	// constructor to pass the values

	public ClientHandler(Server server) {
		this.ServerName = server;
	}

	@Override
	public void run() {
		// code to process client request is put here

		//Do the three way handshake
		ServerName.ThreeWayHandShake();

		System.out.println("Server Listening to Client "+""+" Requests...");
		




		// at the end close the used socket
	}

}
