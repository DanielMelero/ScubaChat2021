package src;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Network Layer handle all acknowledgments for reliable data transfer
 * 
 * @author Daniel Melero
 */
public class NetworkLayer {
	private static final Random rand = new Random();
	private static final int BITS_FOR_ADDRESSES = 4;
	private static final int TIMEOUT_TIME = 3000;

	private Timer timer;
	private int userID;
	//TODO: change this so there is a waiting list in case same seq num
	private HashMap<Packet, ArrayList<Integer>> ackMap;

	private TransportLayer transportLayer;
	private Routing routing;
	private MyProtocol protocol;

	/**
	 * Initialize network layer with random user id
	 * 
	 * @param protocol
	 */
	public NetworkLayer(MyProtocol protocol) {
		// random user id
		this.userID = rand.nextInt((int) Math.pow(2, BITS_FOR_ADDRESSES));

		this.transportLayer = new TransportLayer(this);
		this.routing = new Routing(this);
		this.protocol = protocol;
		this.ackMap = new HashMap<>();
		this.timer = new Timer();
	}

	public NetworkLayer(MyProtocol protocol, int userID) throws Exception {
		// check if given id respect size
		if (userID < 0 || userID >= Math.pow(2, BITS_FOR_ADDRESSES))
			throw new Exception("user id does not fit address size");
		this.userID = userID;
		this.protocol = protocol;
		this.routing = new Routing(this);
		this.ackMap = new HashMap<>();
		this.transportLayer = new TransportLayer(this);
		this.timer = new Timer();
	}

	/**
	 * Handle received packet
	 * 
	 * @param pkt
	 * @throws Exception
	 */
	public void receivedPacket(ByteBuffer pkt) throws Exception {
		if (pkt.capacity() == 32) {
			// send packet of type DATA to transport layer
			this.sendToTransportLayer(pkt);
		} else if (pkt.capacity() == 2) {
			ShortPacket sp = new ShortPacket(pkt);
			if (sp.getIsAck()) {
				// handle packet of type SHORT_DATA as an acknowledgment
				this.handleAck(sp);
			} else {
				// send packet of type SHORT_DATA to the routing protocol
				this.routing.receivedRoutingPacket(sp);
			}
		}
	}

	/**
	 * handle acknowledgments
	 * 
	 * @param sp short packet
	 */
	public void handleAck(ShortPacket sp) {
		if (sp.getSourceAddress() == this.userID) {
			//return if it is sent from this instance (our ack is being forwarded)
			return;
		} else if (sp.getDestinationAddress() != this.userID) {
			//this ack is not meant for this instance
			//TODO: forward it
			return;
		}

		//add ack to correct packet list
		for(Packet key : this.ackMap.keySet()) {
			if (key.getSequenceNumber() == sp.getSequenceNumber()) {
				ArrayList<Integer> acks = this.ackMap.get(key);
				if (!acks.contains(sp.getSourceAddress())) {
					acks.add(sp.getSourceAddress());
				}
				break;
			}
		}
	}

	/**
	 * send packet to protocol
	 * 
	 * @param pkt
	 */
	public void sendPacket(Packet pkt) {
		
		//if it is a retransmission do not erase acks already received
		if (!ackMap.containsKey(pkt)){
			//store new packet waiting for acks
			ackMap.put(pkt, new ArrayList<>());
		}

		//set up a time out
        this.timer.schedule(new TimeOut(this, pkt), TIMEOUT_TIME);

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
	 * send ack to the destination address for the packet corresponding to the given
	 * sequence number
	 * 
	 * @param destinationAddress destination address
	 * @param sequenceNumber     sequence number
	 */
	public void sendAcknowledgment(int destinationAddress, int sequenceNumber) {
		ShortPacket sp = new ShortPacket(true, this.userID, destinationAddress, sequenceNumber);
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

	public ArrayList<Integer> getAcks(Packet packet) {
		return this.ackMap.get(packet);
	}

	public void eraseAckMapEntry(Packet packet) {
		this.ackMap.remove(packet);
	}

	public int getUserID() {
		return this.userID;
	}

	public Routing getRouting() {
		return this.routing;
	}
}

/**
 * This class is called after timeout to check if a retransmission is necessary
 */
class TimeOut extends TimerTask {
    private NetworkLayer networkLayer;
	private Packet packet;

    public TimeOut(NetworkLayer networkLayer, Packet packet) {
        this.networkLayer = networkLayer;
		this.packet = packet;
    }

    /**
     * check if retransmission is needed
     */
	@Override
    public void run() {
        ArrayList<Integer> acks = this.networkLayer.getAcks(this.packet);

		boolean allAcknoledged = true;
		for(int node : this.networkLayer.getRouting().getNeededAcknowledgements()) {
			if (!acks.contains(node)) {
				allAcknoledged = false;
				break;
			}
		}

		if (!allAcknoledged) {
			this.networkLayer.sendPacket(this.packet);
		} else {
			this.networkLayer.eraseAckMapEntry(this.packet);
		}

		// debugging(acks, this.networkLayer.getRouting().getNeededAcknowledgements());

		//finish task
		this.cancel();
    }

	/*
	private void debugging(ArrayList<Integer> acks, int[] needed) {
		System.out.println(this.networkLayer.getUserID());
		System.out.println("acks length: " + acks.size());
		for (Integer i:acks) {
			System.out.println(i+" ");
		}
		System.out.println();
		System.out.println("needed length: " + needed.length);
		for (Integer i:needed) {
			System.out.println(i+" ");
		}
		System.out.println();

	}
	*/
}