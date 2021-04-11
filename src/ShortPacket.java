package src;

import java.nio.ByteBuffer;

/**
 * Short Packet class with all information the short packet have
 * 
 * @author Daniel Melero
 */
public class ShortPacket {
    static final int SHORT_PACKET_SIZE = 2;

    //Routing information
    private int sourceAddress;
    private int destinationAddress;

    //acknowledgement
    private int sequenceNumber;

    //type of short packet
    private boolean isAck;

    /**
     * create short packet with given byte buffer
     * 
     * @param buffer
     * @throws Exception
     */
    public ShortPacket(ByteBuffer buffer) throws Exception {
        //check if byte buffer correspond to short packet
        if (buffer.capacity() != SHORT_PACKET_SIZE) throw new Exception("Data short size not respected");
        
        int[] array = new int[buffer.capacity()];
        for (int i = 0; i < array.length; i++) {
            array[i] = buffer.get(i);
        }

        isAck = (array[0] >>> 7) != 0;

        if (isAck) {
            //get sequence number. 127 -> 0111 1111
            this.sequenceNumber = array[0] & 127;
        }
        //get source address. 240 -> 1111 0000
        this.sourceAddress = array[1] & 240;

        //get destination address. 15 -> 0000 1111
        this.destinationAddress = array[1] & 15;
    }

    /**
     * create routing short packet
     * 
     * @param sourceAddress source address
     * @param destinationAddress destination address
     */
    public ShortPacket(int sourceAddress, int destinationAddress) {
        this.sourceAddress = sourceAddress;
        this.destinationAddress = destinationAddress;
        this.sequenceNumber = 0;

        this.isAck = false;
    }

    /**
     * create acknowledgment short packet
     * 
     * @param sourceAddress source address
     * @param destinationAddress destination address
     * @param sequenceNumber sequence number
     */
    public ShortPacket(int sourceAddress, int destinationAddress, int sequenceNumber) {
        this.sourceAddress = sourceAddress;
        this.destinationAddress = destinationAddress;
        this.sequenceNumber = sequenceNumber;

        this.isAck = true;
    }

    /**
     * transform short packet to an int array
     * 
     * @return
     */
    public int[] toIntArray() {
        int[] pkt = new int[SHORT_PACKET_SIZE];

        pkt[0] = this.sequenceNumber + (this.isAck ? 1 : 0) * 128;
        pkt[1] = (this.sourceAddress << 4) + this.destinationAddress;

        return pkt;
    }

    /**
     * transform short packet to a ByteBuffer
     * 
     * @return
     */
    public ByteBuffer toByteBuffer() {
        int[] array = this.toIntArray();

        // get bytes from input
		byte[] inputBytes = new byte[array.length];
        for (int i = 0; i < array.length; i++) {
            inputBytes[i] = (byte) array[i];
        }

        // make a new byte buffer with the length of the
		// input string
		ByteBuffer toSend = ByteBuffer.allocate(inputBytes.length); 
		toSend.put(inputBytes, 0, inputBytes.length);
        
        return toSend;
    }

    /**
     * get the short packet type
     * 
     * @return
     */
    public boolean getIsAck() {
        return this.isAck;
    }

    /**
     * source address getter
     * 
     * @return
     */
    public int getSourceAddress() {
        return this.sourceAddress;
    }
    
    /**
     * destination address getter
     * 
     * @return
     */
    public int getDestinationAddress() {
        return this.destinationAddress;
    }

    /**
     * sequence number getter
     * 
     * @return
     */
    public int getSequenceNumber() {
        return this.sequenceNumber;
    }
}
