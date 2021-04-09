package tests;

import src.*;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


/**
 * TransportLayer class testing
 * 
 * @author Daniel Melero
 */
public class TransportLayerTest {
    TransportLayer transportLayer;
    String msg = "hello world, I am a scuba diver. located in the Maldives.";
    Packet[] pkts;

    @BeforeEach
    public void initialization() throws Exception {
        this.transportLayer = new TransportLayer(new MyProtocol("", 0, 0));
        pkts = transportLayer.createPackets(msg);
    }

    @Test
    public void createPacketsTest() {
        String pktsMsg = "";
        for (Packet pkt : pkts) {
            pktsMsg += pkt.getData();
        }
        assertEquals(msg, pktsMsg);
    }

    @Test
    public void buildMessageTest() {
        for (int i = 0; i < pkts.length - 1; i++) {
            transportLayer.receivedPacket(pkts[i]);
        }
        String pktsMsg = transportLayer.buildMessage(pkts[pkts.length - 1]);
        assertEquals(msg, pktsMsg);
    }

    @Test
    public void missingPacketsTest() throws Exception {
        msg += "I am lost inside a very obscure cave, there is an angry shark around. SOS.";
        Packet[] pkts = transportLayer.createPackets(msg);
        for (int i = 0; i < pkts.length - 2; i++) {
            transportLayer.receivedPacket(pkts[i]);
        }
        assertTrue(transportLayer.missingPackets(pkts[pkts.length - 1]));
        transportLayer.receivedPacket(pkts[pkts.length - 2]);
        assertFalse(transportLayer.missingPackets(pkts[pkts.length - 1]));
    }
}
