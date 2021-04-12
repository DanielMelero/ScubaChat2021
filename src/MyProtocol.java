
package src;

import client.*;
//import protocol.TransportLayer;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Timer;
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
	HashMap<ByteBuffer, Timer> timer = new HashMap();
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

	public void send(ByteBuffer input) {
		this.timer.put(input, new Timer());
        this.timer.get(input).schedule(new TryToSend(input, this), 0, 100);
	}

	private void mediumAccessControl(ByteBuffer input) {
		Message msg;
		if (input.capacity() == 2) {
			msg = new Message(MessageType.DATA_SHORT, input);
		} else  {
			msg = new Message(MessageType.DATA, input);
		}
		// Send it at random times due to the ALOHA Protocol
		if (free) {
			if (new Random().nextInt(100) < 50) {
				try {
					sendingQueue.put(msg);
					Timer t = timer.get(input);
					t.cancel();
        			t.purge();
					this.timer.remove(input);
        			return;
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} else {
				mediumAccessControl(input);
			}
		}
	}

	class TryToSend extends TimerTask {
		private ByteBuffer input;
		private MyProtocol protocol;
	
		public TryToSend(ByteBuffer input, MyProtocol protocol) {
			this.input = input;
			this.protocol = protocol;
		}
	
		/**
		 * resend input
		 */
		public void run() {
			protocol.mediumAccessControl(input);
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
						free = false;
					} else if (m.getType() == MessageType.FREE) { // The channel is no longer busy (no nodes are sending
																	// // within our detection range)
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
					} else if (m.getType() == MessageType.HELLO) { // Server / audio framework hello message. You don't
																	// have to handle this
						System.out.println("HELLO");
					} else if (m.getType() == MessageType.SENDING) { // This node is sending
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