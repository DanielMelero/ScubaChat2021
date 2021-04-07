package src;

import java.util.ArrayList;

/**
 * Transport Layer Mechanism
 * 
 * @author Daniel Melero
 * 
 * Ensure the order of sent messages at the receiving node. 
 * Design a protocol for reliable data transfer.
 */
public class TransportLayer {
    static final int MAX_BYTES_PER_MESSAGE = 240;

    //TODO:have a fixed id

    ArrayList<Packet> packetBuffer = new ArrayList<>();
    ArrayList<Integer> sourceAddressBuffer = new ArrayList<>();
    //TODO: Hash map | source -> buffer

    //TODO: make a builder (initiate id)

    /**
     * Create necessary packet(s) and send everything 
     * 
     * @param msg
     * @throws Exception
     */
    public void sendMessage(String msg) throws Exception {
        Packet[] pkts = createPackets(msg);
        for (Packet pkt : pkts) {
            //TODO: send individual packet
        }
    }

    /**
     * store in buffer if necessary or send full message to application
     * 
     * @param pkt received packet
     */
    public void receivedPacket(Packet pkt) {
        if (pkt.getHasNext()) {
            //TODO: add to correspondent buffer
        } else {
            //TODO: build message, send to Application and clear buffer
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
     * create Packet(s) for given msg
     * 
     * @param msg string to be sent
     * @return array of Packets
     * @throws Exception
     * 
     * @ensure msg respect max number of bytes
     */
    private Packet[] createPackets(String msg) throws Exception {
        //check is message respects its maximum size
        if (msg.getBytes().length > MAX_BYTES_PER_MESSAGE) throw new Exception("message is too long");
        
        //TODO: get the number of packets needed and divide msg

        //TODO: for each packet create a Packet class
        
        return null;
    }

    /**
     * Build message with last packet received and packets stored in the buffer
     * 
     * @param lastPacket last packet received
     * @return message
     * @throws Exception
     */
    private String buildMessage(Packet lastPacket) throws Exception {
        //check if last packet is actually the last packet
        if (lastPacket.getHasNext()) throw new Exception("invalid last packet");
        
        //TODO: compute string length, create char array fill it and covert it to String
            //char[] chs = new char[length];
            //String message = new String(chs);

        return "";
    }

    /**
     * check if there are missing packets or not
     * 
     * @param buffer packets stored until lastPacket
     * @param lastPacket hasNext is false
     * @return 
     * @throws Exception
     * 
     * @ensure last packet is last packet
     */
    private boolean missingPackets(Packet[] buffer, Packet lastPacket) throws Exception {
        //check if last packet is actually the last packet
        if (lastPacket.getHasNext()) throw new Exception("invalid last packet");

        return buffer.length != lastPacket.getOffset();
    }

}
