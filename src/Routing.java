package src;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Routing class keeps track of available nodes
 * 
 * @author Daniel Melero
 */
public class Routing {
    private NetworkLayer networkLayer;

    private Route itself;

    private ArrayList<Route> routingTable;

    /**
     * create routing table with a given network layer
     * 
     * @param networkLayer network layer
     */
    public Routing(NetworkLayer networkLayer) {
        this.routingTable = new ArrayList<>();
        this.networkLayer = networkLayer;
        int id = this.networkLayer.getUserID();
        this.itself = new Route(id, id, 0);

        //decrease TTL every second
        Timer timer = new Timer();
        timer.schedule(new timeToLiveManager(this), 0, 1000);
    }

    /**
     * get an array with all available nodes' id except itself
     * 
     * @return
     */
    public int[] getNeededAcknowledgements() {
        ArrayList<Integer> availableNodes = this.getAvailableNodes();

        int[] res;
        // return all available nodes
        res = new int[availableNodes.size()];
        for (int i = 0; i < res.length; i++) {
            res[i] = availableNodes.get(i);
        }
            return res;
    }

    /**
     * get a list of all available nodes' id
     * 
     * @return
     */
    private ArrayList<Integer> getAvailableNodes() {
        ArrayList<Integer> res = new ArrayList<>();

        for (Route route : this.routingTable) {
            res.add(route.getAvailableNode());
        }

        return res;
    }

    /**
     * handle received routing short packets
     * 
     * @param sp
     */
    public void receivedRoutingPacket(ShortPacket sp) {
        addRoute(sp.toRoute());
    }

    /**
     * add node to routing table if it is possible
     * 
     * @param nodeAddress
     */
    private void addRoute(Route route) {
        //do not add node if it is itself
        if (route.getAvailableNode() == this.networkLayer.getUserID()) return;
        //do not add node if the cost is too big
        if (route.getCost() >= 4) return;
        
        boolean found = false;
        for (Route r : this.routingTable) {
            if (r.updateRoute(route)) {
                found = true;
                break;
            }
        }

        if (!found) {
            this.routingTable.add(route);
            this.sendRoute(route);
        }
    }

    /**
     * send routing table one entry at a time
     */
    public void sendRoutingTable() {
        this.sendRoute(this.itself);
        for (Route route : this.routingTable) {
            this.sendRoute(route);
        }
    }

    /**
     * send entry from routing table as a short packet
     * 
     * @param nodeAddress
     */
    private void sendRoute(Route route) {
        ShortPacket sp = new ShortPacket(false, route.getAvailableNode(), this.networkLayer.getUserID(), route.getCost() + 1);
        this.networkLayer.sendShortPacket(sp);
    }

    /**
     * routing table getter
     * 
     * @return
     */
    public ArrayList<Route> getRoutingTable() {
        return this.routingTable;
    }

    public NetworkLayer getNetworkLayer() {
        return this.networkLayer;
    }
}

/**
 * This class is called every 1 second and decreases TTL in the routing table
 */
class timeToLiveManager extends TimerTask {
    private Routing routing;
    private int timeToSendAll = 2;

    public timeToLiveManager(Routing routing) {
        this.routing = routing;
    }

    /**
     * decreases TTL and send full routing table if necessary
     */
    @Override
    public void run() {
        //decrease time to live of all entries of the routing table
        ArrayList<Route> routingTable = routing.getRoutingTable();
        ArrayList<Route> toRemove = new ArrayList<>();
        for(Route route : routingTable) {
            if (route.decreaseTimeToLive()) {
                toRemove.add(route);
            }
        }

        //remove all route with no time to live
        for (Route route : toRemove) {
            routingTable.remove(route);
        }

        //send the routing table if necessary
        if (timeToSendAll == 0) {
            this.timeToSendAll = 3;
            this.routing.sendRoutingTable();
        } else {
            this.timeToSendAll--;
        }
    }
}