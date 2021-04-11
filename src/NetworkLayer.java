package src;

import java.nio.ByteBuffer;
import java.util.Random;

/**
 * Network Layer handle all acknowledgments for reliable data transfer
 * 
 * @author Daniel Melero
 */
public class NetworkLayer {
	private static final Random rand = new Random();
	private static final int BITS_FOR_ADDRESSES = 4;
	private int ackCounter = 0;
	private int userID;

	private TransportLayer transportLayer;
	private Routing routing;
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
		this.routing = new Routing(this);
		this.protocol = protocol;
	}

	public NetworkLayer(MyProtocol protocol, int userID) throws Exception {
		//check if given id respect size
		if (userID < 0 || userID >= Math.pow(2, BITS_FOR_ADDRESSES)) throw new Exception("user id does not fit address size");
		this.userID = userID;
		this.protocol = protocol;
		this.routing = new Routing(this);
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
				byte[] received = new byte[pkt.remaining()];
				pkt.get(received, 0, received.length);

				if (((received[1] & 240) >> 4) != getUserID() && (received[1] & 15) == getUserID()) {
					ackCounter++;
					if (ackCounter == 3) {
						protocol.acked = true;
					} else {
						Thread.sleep(100);
					}
				} else {
					protocol.sendShort(pkt);
				}
			} else {
				this.routing.receivedRoutingPacket(sp);
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
	 * send short packet to the protocol as a byte buffer
	 * 
	 * @param sp short packet
	 */
	public void sendShortPacket(ShortPacket sp) {
		protocol.send(sp.toByteBuffer());
	}

	/**
	 * send ack to the destination address for the packet corresponding to the given sequence number
	 * 
	 * @param destinationAddress destination address
	 * @param sequenceNumber sequence number
	 */
	public void sendAcknowledgment(int destinationAddress, int sequenceNumber) {
		ShortPacket sp = new ShortPacket(this.userID, destinationAddress, sequenceNumber);
		this.sendShortPacket(sp);
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