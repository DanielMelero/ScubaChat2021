package src;

import java.nio.ByteBuffer;
import java.util.Random;

/**
 * Network Layer class
 * 
 * @author Daniel Melero
 */
public class NetworkLayer {
	private static final Random rand = new Random();
	private static final int BITS_FOR_ADDRESSES = 4;
	
	private int userID;

	private TransportLayer transportLayer;
	private MyProtocol protocol;
	
	/**
	 * Initialize network layer with random user id 
	 * 
	 * @param protocol
	 */
	public NetworkLayer(MyProtocol protocol) {
		//random user id
		this.userID = rand.nextInt((int)Math.pow(2, BITS_FOR_ADDRESSES));
		
		this.transportLayer = new TransportLayer(this);
		this.protocol = protocol;
	}

	public NetworkLayer(MyProtocol protocol, int userID) throws Exception {
		//check if given id respect size
		if (userID < 0 || userID >= Math.pow(2, BITS_FOR_ADDRESSES)) throw new Exception("user id does not fit address size");
		this.userID = userID;
		this.protocol = protocol;
		this.transportLayer = new TransportLayer(this);
	}

	/**
	 * Handle received packet
	 * 
	 * @param pkt
	 * @throws Exception
	 */
	public void receivedPacket(ByteBuffer pkt) throws Exception {
		if (pkt.capacity() == 32) {
			//send packet of type DATA to transport layer
			this.sendToTransportLayer(pkt);
		} else if (pkt.capacity() == 2) {
			ShortPacket sp = new ShortPacket(pkt);
			if (sp.getIsAck()) {
				//TODO: Handle ACK
			} else {
				//TODO: send to routing
			}
		}
	}

	/**
	 * send packet to protocol
	 * 
	 * @param pkt
	 */
	public void sendPacket(Packet pkt) {
		protocol.send(pkt.toByteBuffer());
	}
	
	/**
	 * send byte buffer to transport layer
	 * 
	 * @param buffer
	 * @throws Exception
	 */
	private void sendToTransportLayer(ByteBuffer buffer) throws Exception {
		this.transportLayer.receivedPacket(buffer);
	}

	public int getUserID() {
		return this.userID;
	}
}



