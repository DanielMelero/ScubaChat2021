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
        if (data.length() != APPLICATION_LAYER_SIZE || sourceAddress >>> 8*NETWORK_LAYER_SIZE != 0) throw new Exception("layer sizes not respected");
        // fill packet information
        this.sourceAddress = sourceAddress;
        this.hasNext = hasNext;
        this.sequenceNumber = sequenceNumber;
        this.offset = offset;
        this.data = data;

        //generate a checksum if there was none
        if (checksum == null) {
            this.checksum = generateChecksum();
        } else {
            this.checksum = checksum;
            // check if the checksum is correct (no bit errors in packet)
            if (!isChecksumCorrect()) throw new Exception("incorrect checksum");
        }
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
        this.sequenceNumber = (pkt[1] >>> 3) & 7;
        this.offset = pkt[1] & 7;
        this.data = "";
        for (int i = 2; i < pkt.length; i++) {
            this.data += Character.toString((char) pkt[i]);
        }

        // check if the checksum is correct (no bit errors in packet)
        if (!isChecksumCorrect()) throw new Exception("incorrect checksum");
    }

    /**
     * generate a checksum: is the number of ones inside the packet even?
     * 
     */
    public boolean generateChecksum() {
        int ones = 0;
        
        //Network Layer
        ones += countOnesInInteger(this.sourceAddress);

        //Transport Layer
        ones += (this.hasNext ? 1 : 0);
        ones += countOnesInInteger(this.sequenceNumber) + countOnesInInteger(this.offset);

        //Application Layer
        ones += countOnesInString(this.data);

        return ones % 2 == 0;
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
        byte[] bs = s.getBytes();
        int ones = 0;
        for(byte b : bs) {
            while (b > 0) {
                if ((b & 0x01) == 0x01) {
                    ones++;
                }
                b = (byte) (b >>> 0x01);
            }
        }
        return ones;
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
     * @return int array
     */
    public int[] toIntArray() {
        int[] pkt = new int[32];
        
        //Network Layer
        pkt[0] = this.sourceAddress;

        //Transport Layer
        pkt[1] = 128 * (this.hasNext ? 1 : 0) + 64 * (this.checksum ? 1 : 0);
        pkt[1] += (this.sequenceNumber << 3) + this.offset;

        //Application Layer
        for (int i = 0; i < this.data.length(); i++) {
            pkt[i+2] = this.data.codePointAt(i);
        }

        return pkt;
    }

    /**
     * source address getter
     * 
     * @return source address
     */
    public int getSourceAddress() {
        return this.sourceAddress;
    }

    /**
     * source destination getter
     * 
     * @return destination address
     */
    public int getDestinationAddress() {
        return this.destinationAddress;
    }

    /**
     * hasNext getter
     * 
     * @return hasNext
     */
    public boolean getHasNext() {
        return this.hasNext;
    }

    /**
     * checksum getter
     * 
     * @return checksum
     */
    public boolean getChecksum() {
        return this.checksum;
    }

    /**
     * sequence number getter
     * 
     * @return sequence number
     */
    public int getSequenceNumber() {
        return this.sequenceNumber;
    }

    /**
     * offset getter
     * 
     * @return offset
     */
    public int getOffset() {
        return this.offset;
    }

    /**
     * data getter
     * 
     * @return data
     */
    public String getData() {
        return this.data;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        //check if the object is a Packet class
        if (o.getClass() != Packet.class) return false;
        Packet p = (Packet) o;

        boolean networkLayer = this.sourceAddress == p.sourceAddress;
        boolean transportLayer = this.hasNext == p.hasNext || this.checksum == p.checksum || this.sequenceNumber == p.sequenceNumber || this.offset == p.offset;
        boolean applicationLayer = this.data.equals(p.data);

        return networkLayer || transportLayer || applicationLayer;
    }
}
