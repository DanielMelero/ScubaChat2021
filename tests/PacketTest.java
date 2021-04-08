package tests;

import src.Packet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Packet class testing
 * 
 * @author Daniel Melero
 */
public class PacketTest {
    public Random rand = new Random();
    private int[] expectedPacket = new int[32];

    private boolean[] transportBoleans = new boolean[2];
    private int[] transportInts = new int[2];
    private int[] data = new int[30];
    private String msg;

    private Packet packetGivenInfo;
    private Packet packetGivenIntArray;

    @BeforeEach
    public void expectedPacketCreation() throws Exception {
        for (int i = 0; i < expectedPacket.length; i++) {
            expectedPacket[i] = rand.nextInt(256);
        }

        //get booleans for hasNext and checksum
        transportBoleans[0] = expectedPacket[1] >>> 7 != 0;
        transportBoleans[1] = ((expectedPacket[1] >>> 6) & 1) != 0;
        //get integers for offset and sequence number
        transportInts[0] = (expectedPacket[1] >>> 3) & 7;
        transportInts[1] = expectedPacket[1] & 7;
        //get data as integer array
        System.arraycopy(expectedPacket, 2, data, 0, data.length);
        msg = "";
        for (int i = 0; i < data.length; i++) {
            msg += Character.toString((char) data[i]);
        }

        packetGivenInfo = new Packet(expectedPacket[0], transportBoleans[0], null, transportInts[0], transportInts[1], msg);
        if (packetGivenInfo.getChecksum()) {
            //the checksum should 1 so if it is 0 it will be added (64 -> 0100 0000)
            expectedPacket[1] += 64 * (transportBoleans[1] ? 0 : 1);
            transportBoleans[1] = true;
        } else {
            //the checksum should 0 so if it is 1 it will be subtracted (64 -> 0100 0000)
            expectedPacket[1] -= 64 * (transportBoleans[1] ? 1 : 0);
            transportBoleans[1] = false;
        }
        packetGivenIntArray = new Packet(expectedPacket);
    }

    @Test
    public void packetInitializationTest() {
        assertTrue(packetGivenInfo != null);
        assertTrue(packetGivenIntArray != null);

        assertEquals(packetGivenInfo, packetGivenIntArray);
        
        Exception checksumExceptionGivenInfo = assertThrows(Exception.class, () -> 
            {new Packet(expectedPacket[0], transportBoleans[0], !transportBoleans[1], transportInts[0], transportInts[1], msg);});
        assertTrue(checksumExceptionGivenInfo.getMessage().contains("checksum"));

        int[] incorrectChecksum = new int[32];
        System.arraycopy(expectedPacket, 0, incorrectChecksum, 0, expectedPacket.length);
        if (transportBoleans[1]) {
            incorrectChecksum[1] -= 64;
        } else {
            incorrectChecksum[1] += 64;
        }
        Exception checksumExceptionGivenIntArray = assertThrows(Exception.class, () -> 
            {new Packet(incorrectChecksum);});
        assertTrue(checksumExceptionGivenIntArray.getMessage().contains("checksum"));

        Exception sizeExceptionGivenInfo = assertThrows(Exception.class, () -> 
            {new Packet(expectedPacket[0], transportBoleans[0], null, transportInts[0], transportInts[1], Arrays.toString(new int[29]));});
        assertTrue(sizeExceptionGivenInfo.getMessage().contains("size"));

        Exception sizeExceptionGivenIntArray = assertThrows(Exception.class, () -> 
            {new Packet(new int[29]);});
        assertTrue(sizeExceptionGivenIntArray.getMessage().contains("size"));
    }
    
    @Test
    public void toIntArrayTest() {
        for(int i = 0; i < expectedPacket.length; i++) {
            assertEquals(packetGivenInfo.toIntArray()[i], expectedPacket[i]);
            assertEquals(packetGivenIntArray.toIntArray()[i], expectedPacket[i]);
        }
    }

    @Test
    public void countOnesTest() {
        Packet p = packetGivenInfo;

        String[] testString = {"", "Hello", "mod3"};
        int[] testInt = {0, 74, 235};

        assertEquals(p.countOnesInInteger(testInt[0]), 0);
        assertEquals(p.countOnesInInteger(testInt[1]), 3);
        assertEquals(p.countOnesInInteger(testInt[2]), 6);

        assertEquals(p.countOnesInString(testString[0]), 0);
        assertEquals(p.countOnesInString(testString[1]), 20);
        assertEquals(p.countOnesInString(testString[2]), 18);

    }

    @Test
    public void oneBitErrorTest() {
        int[] pkt = simulateOneBitError(expectedPacket);
        Exception checksumExceptionGivenIntArray = assertThrows(Exception.class, () -> 
            {new Packet(pkt);});
        assertTrue(checksumExceptionGivenIntArray.getMessage().contains("checksum"));
    }

    @Test
    public void twoBitErrorTest() {
        int[] pkt = simulateOneBitError(simulateOneBitError(expectedPacket));
        Exception checksumExceptionGivenIntArray = assertThrows(Exception.class, () -> 
            {new Packet(pkt);});
        assertTrue(checksumExceptionGivenIntArray.getMessage().contains("checksum"));
    }

    @Test
    public void threeBitErrorTest() {
        int[] pkt = simulateOneBitError(simulateOneBitError(simulateOneBitError(expectedPacket)));
        Exception checksumExceptionGivenIntArray = assertThrows(Exception.class, () -> 
            {new Packet(pkt);});
        assertTrue(checksumExceptionGivenIntArray.getMessage().contains("checksum"));
    }

    @Test
    public void fourBitErrorTest() {
        int[] pkt = simulateOneBitError(simulateOneBitError(simulateOneBitError(simulateOneBitError(expectedPacket))));
        Exception checksumExceptionGivenIntArray = assertThrows(Exception.class, () -> 
            {new Packet(pkt);});
        assertTrue(checksumExceptionGivenIntArray.getMessage().contains("checksum"));
    }

    public int[] simulateOneBitError(int[] array) {
        int selectedByte = rand.nextInt(array.length);
        int selectedBit = rand.nextInt(8);
        int[] res = Arrays.copyOf(array, array.length);
        if (((res[selectedByte] >>> selectedBit) & 1) == 1) {
            res[selectedByte] -= Math.pow(2, selectedBit);
        } else {
            res[selectedByte] += Math.pow(2, selectedBit);
        }
        return res;
    }
}
