package Server;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class ClientHandler implements Runnable {
	private Server ServerName;
	// constructor to pass the Server object
	public ClientHandler(Server server) {
		this.ServerName = server;
	}
	
	//setters and getters for received data
	public byte[] getReceiveddata() {
		return ServerName.receiveData;
	}
	public void setReceiveddata(byte[] receiveData) {
		ServerName.receiveData = receiveData;
	}
	
	//setters and getters for DataReceivedSuccessfully
	public boolean getDataReceivedSuccessfully() {
		return ServerName.DataReceivedSuccessfully;
	}
	public void setDataReceivedSuccessfully(boolean DataReceivedSuccessfully) {
		ServerName.DataReceivedSuccessfully = DataReceivedSuccessfully;
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
