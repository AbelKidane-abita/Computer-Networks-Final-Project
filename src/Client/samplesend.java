//package Client;
//
//import java.net.DatagramPacket;
//import java.net.DatagramSocket;
//import java.net.SocketAddress;
//import java.net.SocketException;
//
//public class samplesend {
//
//	public static void main(String[] args) {
//		DatagramSocket ServerSocket;
//		int MainServerPort = 3999;
//		try {
//			
//			ServerSocket = new DatagramSocket(MainServerPort);
//		} catch (SocketException e) {
//			System.out.println("Error: Listening through port-"+MainServerPort+" has failed.");
//		}
//		
//		byte[] senddata = "Ack".getBytes();
//		int address;
//		SocketAddress port;
//		DatagramPacket packet = new DatagramPacket(senddata, senddata.length,address, port);
//		//ServerSocket.send(packet);
//		
//
//	}
//
//}
