package src;

import client.*;

import java.nio.ByteBuffer;
import java.io.Console;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * This is just some example code to show you how to interact with the server
 * using the provided 'Client' class and two queues. Feel free to modify this
 * code in any way you like!
 */

public class MyProtocolTest {
	public String inputMessage = "";
	Message msg;
	Set<Integer> userSet = new HashSet<Integer>();
	private static int userID = -1;
	// The host to connect to. Set this to localhost when using the audio interface
	// tool.
	private static String SERVER_IP = "netsys.ewi.utwente.nl"; // "127.0.0.1";
	// The port to connect to. 8954 for the simulation server.
	private static int SERVER_PORT = 8954;
	// The frequency to use.
	private static int frequency = 4100;// TODO: Set this to your group frequency!

	private BlockingQueue<Message> receivedQueue;
	private BlockingQueue<Message> sendingQueue;

	public MyProtocolTest(String server_ip, int server_port, int frequency) throws InterruptedException {
		receivedQueue = new LinkedBlockingQueue<Message>();
		sendingQueue = new LinkedBlockingQueue<Message>();

		new Client(SERVER_IP, SERVER_PORT, frequency, receivedQueue, sendingQueue); // Give the client the Queues to use

		new receiveThread(receivedQueue).start(); // Start thread to handle received messages!
//try {
		Scanner console = new Scanner(System.in);
		// Console console = System.console();
		// String input = "";
		while (true) {
			// input = console.readLine(); // read input
			System.out.print("Current Online users are:" + userSet + "Please choose who you want to talk to");
			int input = console.nextInt(); // read input
			int destination = input;
			// make the packet, put the header and etc

			System.out.println("Please enter your message");
			Scanner console1 = new Scanner(System.in);
			inputMessage = console1.nextLine();

			byte[] inputBytes = inputMessage.getBytes(); // get bytes from input

			/**
			 * 
			 * 
			 * Here, we can make the packet
			 * 
			 * 
			 * 
			 */

			ByteBuffer toSend = ByteBuffer.allocate(inputBytes.length); // make a new byte buffer with the length of the
																		// input string
			toSend.put(inputBytes, 0, inputBytes.length); // copy the input string into the byte buffer.

			if ((inputMessage.length()) > 2) {
				msg = new Message(MessageType.DATA, toSend);
			} else {
				msg = new Message(MessageType.DATA_SHORT, toSend);
			}

			// keep sending the message in some intervals, cancel timer when it is acknowledged

			Timer timer = new Timer();
			TimerTask task = new timertask(msg);
			timer.schedule(task, 0, 6000);
			// sendingQueue.put(msg);
		}
   // } catch (InterruptedException e){
    //    System.exit(2);
    //}      


	}

	public class timertask extends TimerTask {

		private Message msg;

		public timertask(Message msg) {
			this.msg = msg;
		}

		public void run() {
			try {
				sendingQueue.put(msg);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public String getInput() {

		return inputMessage;
	}

	public static void main(String args[]) {
		if (args.length > 0) {
			userID = Integer.parseInt(args[1]);
			frequency = Integer.parseInt(args[0]);
		}
		try {
			new MyProtocolTest(SERVER_IP, SERVER_PORT, frequency);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
					} else if (m.getType() == MessageType.FREE) { // The channel is no longer busy (no nodes are sending
																	// within our detection range)
						System.out.println("FREE");
					} else if (m.getType() == MessageType.DATA) { // We received a data frame!

						byte[] receivedMessage = new byte[m.getData().remaining()];
						m.getData().get(receivedMessage, 0, receivedMessage.length);

					/**
					 * 
					 * 
					 * We extract the info of the message and send ack if needed
					 * 
					 */
						int source = receivedMessage[0] >>> 6;
						int destination = (receivedMessage[0] << 2) >>> 6;

						if (source != userID && destination == userID) {

							byte[] acknowledgment = new byte[2];

							// fill the ack message
							// Source of the sender has become the destination
							acknowledgment[0] = receivedMessage[0]; // includes the src and dest address
							acknowledgment[1] = (receivedMessage[1]);// flagoffset, etc

							ByteBuffer toSend = ByteBuffer.allocate(acknowledgment.length);
							toSend.put(acknowledgment, 0, acknowledgment.length);
							Message ack = new Message(MessageType.DATA_SHORT, toSend);
							sendingQueue.put(ack);
						}

						else if (destination != userID && source != userID) {
							sendingQueue.put(m);
						}
						// System.out.print("DATA: ");
						// printByteBuffer( m.getData(), m.getData().capacity() ); //Just print the data

						

					} else if (m.getType() == MessageType.DATA_SHORT) { // We received a short data frame!

						byte[] receivedMessage = new byte[m.getData().remaining()];
						m.getData().get(receivedMessage, 0, receivedMessage.length);

						// extract the source,destination, sequence number and etc of the message.

						int source = receivedMessage[0] >>> 6;
						int destination = (receivedMessage[0] << 2) >>> 6;

						if (source != userID && destination == userID) {
							// acknowledged = true;
						} else if (destination != userID && source != userID) {
							sendingQueue.put(m);
						}

						// if data short AND the first byte(type) is ping
						// userSet.add(the id of the username)

						// else if data SHORT and type is ack

						System.out.print("DATA_SHORT: ");
						printByteBuffer(m.getData(), m.getData().capacity()); // Just print the data

						// then the message is an ack, act accordingly

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
