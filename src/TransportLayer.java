package src;

import java.io.UnsupportedEncodingException;

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
        if (msg.getBytes().length > MAX_BYTES_PER_MESSAGE) throw new Exception("message is too long");
        
        //TODO: get the number of packets needed and divide msg

        //TODO: for each packet create a Packet class
        
        return null;
    }

}
