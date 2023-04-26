package Router;

import java.io.IOException;
import java.net.*;

public class Router {

	private DatagramSocket receiveSocket;
	private DatagramSocket sendSocket;

	public Router(int receivePort, int sendPort) throws SocketException {
		receiveSocket = new DatagramSocket(receivePort);
		sendSocket = new DatagramSocket();
	}

	public void forwardPacket() throws IOException {
		byte[] buffer = new byte[1024];
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
		receiveSocket.receive(packet);

		// simulate 10% packet loss
		if (Math.random() < 0.1) {
			System.out.println("Packet dropped: " + new String(packet.getData(), 0, packet.getLength()));
			return;
		}

		InetAddress destAddress = packet.getAddress();
		int destPort = packet.getPort();
		DatagramPacket sendPacket = new DatagramPacket(packet.getData(), packet.getLength(), destAddress, destPort);
		sendSocket.send(sendPacket);
		System.out.println("Packet forwarded: " + new String(packet.getData(), 0, packet.getLength()));
	}

	public void close() {
		receiveSocket.close();
		sendSocket.close();
	}

	public static void main(String[] args) throws IOException {
		// create a router instance
		Router router = new Router(9000, 9001);

		// forward packets until user interrupts the program
		while (true) {
			try {
				router.forwardPacket();
			} catch (IOException e) {
				e.printStackTrace();
				break;
			}
		}

		// close the sockets
		router.close();
	}
}