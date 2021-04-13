package src;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Transport Layer transform a given string into packet(s) and transfom given packet(s) into a message
 * 
 * @author Daniel Melero
 * 
 * Ensure the order of sent messages at the receiving node. 
 * Design a protocol for reliable data transfer.
 */
public class TransportLayer {
    static final int MAX_BYTES_PER_MESSAGE = 240;
    static final int MAX_MSG_BYTES_PER_PKT = 30;

    private NetworkLayer networkLayer;
    private ApplicationLayer applicationLayer;

    private HashMap<Integer, ArrayList<Packet>> bufferMap = new HashMap<>();
    private HashMap<Integer, Packet> waitingForMissingPackets = new HashMap<>();

    private HashMap<Integer, ArrayList<Packet>> packetsAlreadyReceived = new HashMap<>();;

    /**
     * Create Transport Layer instance with a given Application Layer
     * 
     */
    public TransportLayer(NetworkLayer networkLayer) {
        this.applicationLayer = new ApplicationLayer(this);
        this.networkLayer = networkLayer;
    }

    /**
     * Create necessary packet(s) and send everything 
     * 
     * @param msg
     * @throws Exception
     */
    public void sendMessage(String msg) throws Exception {
        Packet[] pkts = createPackets(msg);
        for (Packet pkt : pkts) {
            this.networkLayer.sendPacket(pkt);
        }
    }

    /**
     * store in buffer if necessary or send full message to application
     * 
     * @param pkt received packet
     */
    public void receivedPacket(Packet pkt) {
        int src = pkt.getSourceAddress();

        //always send ack when receiving packet
        this.sendAcknowledgment(src, pkt.getSequenceNumber());

        ArrayList<Packet> storage = packetsAlreadyReceived.get(src);
        if (storage == null) {
            //create storage for packets from this node
            packetsAlreadyReceived.put(src, new ArrayList<>());
            packetsAlreadyReceived.get(src).add(pkt);
        } else if (storage.contains(pkt)) {
            //packet have already been received so return
            return;
        } else {
            //add new packet to the storage
            storage.add(pkt);
        }

        if (pkt.getHasNext()) {
            //packet indicate that more packets are following
            if(!bufferMap.containsKey(src)){
                //add new buffer for the source of this packet
                ArrayList<Packet> buf = new ArrayList<>();
                buf.add(pkt);
                bufferMap.put(src, buf);
                waitingForMissingPackets.put(src, null);
            } else if (waitingForMissingPackets.get(src) != null &&
                        !missingPackets(waitingForMissingPackets.get(src))) {
                //this packet was missing in the buffer and now message can be reassembled
                reassemblePackets(waitingForMissingPackets.get(src));
            } else {
                //packet stored in buffer as last packet has not arrive or there are missing packets;
                bufferMap.get(src).add(pkt);
            }
        } else {
            if (bufferMap.containsKey(src)) {
                if (missingPackets(pkt)) {
                    //there are missing packets on the buffer
                    waitingForMissingPackets.put(src, pkt);
                } else {
                    //last packet of a series of packets ready to be reassemble
                    reassemblePackets(pkt);
                }
            } else {
                if (missingPackets(pkt)) {
                    //there are missing packets but the buffer did not exist yet
                    bufferMap.put(src, new ArrayList<>());
                    waitingForMissingPackets.put(src, pkt);
                } else {
                    //single packet and no buffer
                    giveToApplicationLayer(pkt.getSourceAddress(), pkt.getData());
                }
            }
            
        }
    }

    /**
     * redirect received packet as Packet class
     * 
     * @param pkt received packet
     * @throws Exception
     */
    public void receivedPacket(int[] pkt) throws Exception {
        receivedPacket(new Packet(pkt));
    }

    /**
     * redirect received packet as Packet class
     * 
     * @param buffer byte buffer
     * @throws Exception
     */
    public void receivedPacket(ByteBuffer buffer) throws Exception {
        int[] array = new int[buffer.capacity()];
        for (int i = 0; i < array.length; i++) {
            //bytes to unsigned byte in an integer.
            array[i] = buffer.get(i) & 0xff;
        }
        receivedPacket(new Packet(array));
    }

    /**
     * send ack to the source address of the packet with its corresponding sequence number
     * 
     * @param sourceAddress source address
     * @param sequenceNumber sequence number
     */
    private void sendAcknowledgment(int sourceAddress, int sequenceNumber) {
        this.networkLayer.sendAcknowledgment(sourceAddress, sequenceNumber);
    }

    /**
     * reassemble the packets, give the message to the application layer and clear the buffer
     * 
     * @param lastPacket
     */
    private void reassemblePackets (Packet lastPacket) {
        int src = lastPacket.getSourceAddress();

        //reassemble the packets
        String msg = buildMessage(lastPacket);

        //give the message to the application layer
        giveToApplicationLayer(src, msg);

        //clear the buffer
        bufferMap.get(src).clear();
        waitingForMissingPackets.put(src, null);
    }

    /**
     * give a received message with its source address to the application layer
     * 
     * @param sourceAddress source address
     * @param msg message
     */
    private void giveToApplicationLayer (int sourceAddress, String msg) {
        this.applicationLayer.receiveMessage(sourceAddress, msg);
    }

    /**
     * create Packet(s) for given msg
     * 
     * @param msg string to be sent
     * @return array of Packets
     * @throws Exception
     * 
     * @ensure msg respect max number of bytes
     */
    public Packet[] createPackets(String msg) throws Exception {
        //check is message respects its maximum size
        if (msg.length() > MAX_BYTES_PER_MESSAGE) throw new Exception("message is too long");
        
        //divide into the necessary number of packets
        String[] substrings = new String[(int)Math.ceil(msg.length()/(double)MAX_MSG_BYTES_PER_PKT)];

        //fill message divisions with the maximum number of bytes
        for (int i = 0; i < substrings.length - 1; i++) {
            int x = i * MAX_MSG_BYTES_PER_PKT;
            substrings[i] = msg.substring(x, x + MAX_MSG_BYTES_PER_PKT);
        }
        
        //fill last part of the message until max bytes are reached
        int index = (int)Math.floor(msg.length()/MAX_MSG_BYTES_PER_PKT);
        substrings[substrings.length - 1] = msg.substring(index * 30, msg.length());
        while (substrings[substrings.length - 1].length() < MAX_MSG_BYTES_PER_PKT) {
            substrings[substrings.length - 1] += Character.toString((char) 0);
        }

        
        // for each msg create a Packet class
        int sequenceNumber = 0; //TODO: figure out how to work with sequence numbers (8 slots)
        //TODO: keep track of last seq
        Packet[] pkts = new Packet[substrings.length];
        for (int i = 0; i < pkts.length; i++) {
            boolean hasNext = (i != pkts.length - 1);
            pkts[i] = new Packet(this.networkLayer.getUserID(), hasNext, null, sequenceNumber, i, substrings[i]);
            sequenceNumber++;
        }
        
        return pkts;
    }

    /**
     * Build message with last packet received and packets stored in the buffer
     * 
     * @param lastPacket last packet received
     * @return message
     */
    public String buildMessage(Packet lastPacket) {
        //compute the length of the message
        int length = 0;
        int[] lp = lastPacket.toIntArray();
        while (lp[length+2] != 0) {
            length++;
        }
        char[] chars = new char[lastPacket.getOffset()*30 + length];

        //fill the message with buffer packets if buffer exists
        if (bufferMap.containsKey(lastPacket.getSourceAddress())) {
            for (Packet p : bufferMap.get(lastPacket.getSourceAddress())) {
                char[] c = p.getData().toCharArray();
                System.arraycopy(c, 0, chars, p.getOffset() * 30, 30);
            }
        }

        //fill message with last packet
        System.arraycopy(lastPacket.getData().toCharArray(), 0, chars, lastPacket.getOffset() * 30, length);

        //return message as a string
        return new String(chars);
    }

    /**
     * check if there are missing packets or not
     * 
     * @param buffer packets stored until lastPacket
     * @param lastPacket hasNext is false
     * @return
     */
    public boolean missingPackets(Packet lastPacket) {
        //check if last packet is missing
        if (lastPacket == null) return true;

        //check if a packet is missing in the buffer
        int len;
        int src = lastPacket.getSourceAddress();
        if (bufferMap.containsKey(src)) {
            len = bufferMap.get(src).size();
        } else {
            len = 0;
        }
        return len != lastPacket.getOffset();
    }

    /**
     * get buffer corresponding to the given address
     * 
     * @param sourceAddress source address
     * @return
     */
    public Packet[] getBuffer(int sourceAddress) {
        return (Packet[]) bufferMap.get(sourceAddress).toArray();
    }

    /**
     * network layer getter
     * 
     * @return
     */
    public NetworkLayer getNetworkLayer() {
        return this.networkLayer;
    }
}