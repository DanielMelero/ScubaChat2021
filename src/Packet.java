package src;

/**
 * Packet class with all information the packet have
 * 
 * @author Daniel Melero
 */
public class Packet {
    static final int PACKET_SIZE = 32;

    static final int NETWORK_LAYER_SIZE = 1;
    static final int TRANSPORT_LAYER_SIZE = 1;
    static final int APPLICATION_LAYER_SIZE = 30;

    private int sourceAddress;
    private int destinationAddress;

    private boolean hasNext;
    private boolean checksum;
    private int sequenceNumber;
    private int offset;

    private String data;

    /**
     * Initialize new Packet with given information
     * 
     * @param sourceAddress source address
     * @param hasNext is there a next packet
     * @param checksum error detection
     * @param sequenceNumber sequence number
     * @param offset position inside full message
     * @param data data sent
     * @throws Exception
     * 
     * @ensure layer sizes are respected
     * @ensure correct checksum
     */
    public Packet(int sourceAddress, boolean hasNext, Boolean checksum, int sequenceNumber, int offset, String data) throws Exception {
        // check if all layer sizes have been respected
        if (data.getBytes().length != APPLICATION_LAYER_SIZE || sourceAddress >>> 8*NETWORK_LAYER_SIZE != 0) throw new Exception("layer sizes not respected");

        // fill packet information
        this.sourceAddress = sourceAddress;
        this.hasNext = hasNext;
        this.sequenceNumber = sequenceNumber;
        this.offset = offset;
        this.data = data;

        //generate a checksum if there was none
        if (checksum == null) this.checksum = generateChecksum();
        // check if the checksum is correct (no bit errors in packet)
        else if (!isChecksumCorrect()) throw new Exception("incorrect checksum");
    }

    /**
     * Initialize Packet from the given bytes
     * 
     * @param pkt array of bytes
     * @throws Exception
     * 
     * @ensure packet length is correct
     * @ensure checksume is correct
     */
    public Packet(int[] pkt) throws Exception {
        //check if the packet length is correct
        if (pkt.length != PACKET_SIZE) throw new Exception("incorrect packet size");
        
        this.sourceAddress = pkt[0];
        this.hasNext = pkt[1] >>> 7 == 1;
        this.checksum = ((pkt[1] >>> 6) & 1) == 1;
        //TODO: fill packet information

        // check if the checksum is correct (no bit errors in packet)
        if (!isChecksumCorrect()) throw new Exception("incorrect checksum");
    }

    /**
     * generate a checksum: is the number of ones inside the packet even?
     * 
     */
    public boolean generateChecksum() {
        //TODO: generate checksum
        return false;
    }

    /**
     * count the number of ones in the binary representation of an integer
     * 
     * @param n integer
     * @return the number of ones 
     */
    public int countOnesInInteger (int n) {
        int count = 0;
        while (n != 0) {
            n = n & (n-1);
            count++;
        }
        return count;
    }

    /**
     * count the number of ones in the binary representation of a string
     * 
     * @param s string
     * @return the number of ones
     */
    public int countOnesInString (String s) {
        //TODO: count the number of ones in the binary representation of a string
        return 0;
    }

    /**
     * check if the checksum is correct (are there bit errors detected)
     * 
     * @return boolean
     */
    public boolean isChecksumCorrect() {
        return generateChecksum() == this.checksum;
    }

    /**
     * transform this packet to bytes
     * 
     * @return
     */
    public int[] toIntArray() {
        //TODO: transform this packet to bytes
        return null;
    }
}
