
package src;

import client.*;
//import protocol.TransportLayer;

import java.nio.ByteBuffer;
import java.io.Console;
import java.util.Random;
import java.util.Scanner;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * This is just some example code to show you how to interact with the server
 * using the provided 'Client' class and two queues. Feel free to modify this
 * code in any way you like!
 */

public class MyProtocol {

	boolean collision = false;
	boolean free = true;
	int seq = 0;
	ShortPacket acknowledment;
	boolean acked = false;
	private NetworkLayer networkLayer;
	private static int userID = 0;
	// The host to connect to. Set this to localhost when using the audio interface
	// tool.
	private static String SERVER_IP = "netsys.ewi.utwente.nl"; // "127.0.0.1";
	// The port to connect to. 8954 for the simulation server.
	private static int SERVER_PORT = 8954;
	// The frequency to use.
	private static int frequency = 4100;
	// private Network network;

	private BlockingQueue<Message> receivedQueue;
	private BlockingQueue<Message> sendingQueue;
	// ByteBuffer message;

	// String input;

	public MyProtocol(String server_ip, int server_port, int frequency) {

		receivedQueue = new LinkedBlockingQueue<Message>();
		sendingQueue = new LinkedBlockingQueue<Message>();

		this.networkLayer = new NetworkLayer(this);

		// this.network = new Network();

		new Client(SERVER_IP, SERVER_PORT, frequency, receivedQueue, sendingQueue); // Give the client the Queues to use

		new receiveThread(receivedQueue).start(); // Start thread to handle received messages!

		// handle sending from stdin from this thread.

		// Scanner console = new Scanner(System.in);

		// input = console.nextLine();

	}

	public void sendShort(ByteBuffer inputShort) {
		Message msg;
		msg = new Message(MessageType.DATA_SHORT, inputShort);
		try {
			sendingQueue.put(msg);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	public void send(ByteBuffer input) {

		if (input.capacity() == 2) {
			sendShort(input);
		} else if (input.capacity() == 32) {

			// byte[] inputBytes = input.getBytes(); // get bytes from input
			// ByteBuffer toSend = ByteBuffer.allocate(inputBytes.length); // make a new
			// byte buffer with the length of the input string
			// toSend.put( inputBytes, 0, inputBytes.length ); // copy the input string into
			// the byte buffer.
			Message msg;
			// if( (input.length()) > 2 ){
			msg = new Message(MessageType.DATA, input);
			// } else {
			// msg = new Message(MessageType.DATA_SHORT, toSend);
			// }
			// Send it at random times due to the ALOHA Protocol
			while (!acked) {

				if (free) {
					if (new Random().nextInt(100) < 50) {
						try {
							sendingQueue.put(msg);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					} else {

						send(input);
					}

				} else {
					try {
						Thread.sleep(500);
					} catch (InterruptedException e1) {
						e1.printStackTrace();

					}
					send(input);
				}

			}
		}
	}

	public static void main(String args[]) {

		if (args.length > 0) {
			userID = Integer.parseInt(args[0]);
			frequency = Integer.parseInt(args[1]);
		}
		new MyProtocol(SERVER_IP, SERVER_PORT, frequency);
	}

	private class receiveThread extends Thread {
		private BlockingQueue<Message> receivedQueue;

		public receiveThread(BlockingQueue<Message> receivedQueue) {
			super();
			this.receivedQueue = receivedQueue;
		}

		public void printByteBuffer(ByteBuffer bytes, int bytesLength) {
			for (int i = 0; i < bytesLength; i++) {
				System.out.print(Byte.toString(bytes.get(i)) + " ");
			}
			System.out.println();
		}

		// Handle messages from the server / audio framework
		public void run() {
			while (true) {
				try {
					Message m = receivedQueue.take();
					if (m.getType() == MessageType.BUSY) { // The channel is busy (A node is sending within our
															// detection range)
						System.out.println("BUSY");
						free = false;
					} else if (m.getType() == MessageType.FREE) { // The channel is no longer busy (no nodes are sending
																	// // within our detection range)
						System.out.println("FREE");
						free = true;
					} else if (m.getType() == MessageType.DATA) {// We received a data frame!
						try {
							networkLayer.receivedPacket(m.getData());
						} catch (Exception e) {
							System.out.println("Packet dropped beacuse " + e.getMessage());
						}
					} else if (m.getType() == MessageType.DATA_SHORT) { // We received a short data frame!
						try {
							networkLayer.receivedPacket(m.getData());
						} catch (Exception e) {
							System.out.println("Packet dropped beacuse " + e.getMessage());
						}
					} else if (m.getType() == MessageType.DONE_SENDING) { // This node is done sending
						System.out.println("DONE_SENDING");
					} else if (m.getType() == MessageType.HELLO) { // Server / audio framework hello message. You don't
																	// have to handle this
						System.out.println("HELLO");
					} else if (m.getType() == MessageType.SENDING) { // This node is sending
						System.out.println("SENDING");
					} else if (m.getType() == MessageType.END) { // Server / audio framework disconnect message. You
																	// don't have to handle this
						System.out.println("END");
						System.exit(0);
					}
				} catch (InterruptedException e) {
					System.err.println("Failed to take from queue: " + e);
				}
			}
		}
	}
}