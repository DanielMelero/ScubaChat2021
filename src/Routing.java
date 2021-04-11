package src;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Routing class keeps track of available nodes
 * 
 * @author Daniel Melero
 */
public class Routing {
    static final int TTL = 10;

    private NetworkLayer networkLayer;

    // TODO: private HashMap<Integer[], Integer> availableNodes; key -> {available node, next hop, cost}
    private HashMap<Integer, Integer> availableNodes;

    /**
     * create routing table with a given network layer
     * 
     * @param networkLayer network layer
     */
    public Routing(NetworkLayer networkLayer) {
        availableNodes = new HashMap<>();
        this.networkLayer = networkLayer;
        int id = this.networkLayer.getUserID();
        this.availableNodes.put(addressesToMapKey(id, id), TTL);
        //TODO: create new thread to decrease TTL
    }

    /**
     * get an array with all available nodes' id except itself
     * 
     * @return
     */
    public int[] getNeededAcknowledgements() {
        ArrayList<Integer> availableNodes = this.getAvailableNodes();
        int userID = this.networkLayer.getUserID();

        int[] res;
        if (!availableNodes.contains(userID)) {
            // return all available nodes
            res = new int[availableNodes.size()];
            for (int i = 0; i < res.length; i++) {
                res[i] = availableNodes.get(i);
            }
            return res;
        } else {
            // return all available nodes except itself
            res = new int[availableNodes.size() - 1];
            for (int i = 0; i < res.length; i++) {
                if (availableNodes.get(i) != userID) {
                    res[i] = availableNodes.get(i);
                }
            }
            return res;
        }
    }

    /**
     * get a list of all available nodes' id
     * 
     * @return
     */
    private ArrayList<Integer> getAvailableNodes() {
        ArrayList<Integer> res = new ArrayList<>();

        for (Integer key : availableNodes.keySet()) {
            res.add(mapKeytoAdresses(key)[0]);
        }

        return res;
    }

    /**
     * handle received routing short packets
     * 
     * @param sp
     */
    public void receivedRoutingPacket(ShortPacket sp) {
        addNode(sp.getSourceAddress());
    }

    /**
     * add node to routing table if it is possible
     * 
     * @param nodeAddress
     */
    private void addNode(int nodeAddress) {
        //do not add node if it is itself
        if (nodeAddress == this.networkLayer.getUserID()) return;

        // not using nextHop address because we use data packets as routing information and it does not enough info
        int key = addressesToMapKey(nodeAddress, 0); //TODO: use nextHop
        
        //send routing entry if it is going to be added
        if (!this.availableNodes.containsKey(key)) {
            this.sendEntry(nodeAddress);
        }

        //add node or restart TTL if already exists
        this.availableNodes.put(key, TTL);
    }

    /**
     * send routing table one entry at a time
     */
    private void sendRoutingTable() {
        for (int node : this.getAvailableNodes()) {
            this.sendEntry(node);
        }
    }

    /**
     * send entry from routing table as a short packet
     * 
     * @param nodeAddress
     */
    private void sendEntry(int nodeAddress) {
        ShortPacket sp = new ShortPacket(nodeAddress, this.networkLayer.getUserID());
        this.networkLayer.sendShortPacket(sp);
    }

    /**
     * transform a key from routing table to {available node, next hop node}
     * 
     * @param key
     * @return
     */
    private int[] mapKeytoAdresses(int key) {
        int[] res = new int[2];

        //get source address. 240 -> 1111 0000
        res[0] = key & 240;

        //get destination address. 15 -> 0000 1111
        res[1] = key & 15;

        return res;
    }

    /**
     * transform given values to an entry for the routing table
     * 
     * @param destination
     * @param NextHop
     * @return
     */
    private int addressesToMapKey(int destination, int NextHop) {
        return (destination << 4) + NextHop;
    }
}
