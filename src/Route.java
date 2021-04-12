package src;

/**
 * Stores data of a route in the routing table
 * 
 * @author Daniel Melero
 */
public class Route {
    private static final int TTL = 10;

    private int availableNode;
    private int nextHop;
    private int cost;
    private int timeToLive;

    public Route(int availableNode, int nextHop, int cost, int timeToLive) {
        this.availableNode = availableNode;
        this.nextHop = nextHop;
        this.cost = cost;
        this.timeToLive = timeToLive;
    }

    public Route(int availableNode, int nextHop, int cost) {
        this.availableNode = availableNode;
        this.nextHop = nextHop;
        this.cost = cost;
        this.timeToLive = TTL;
    }

    public boolean decreaseTimeToLive() {
        if (this.timeToLive - 1 <= 0) {
            return true;
        } else {
            this.timeToLive--;
            return false;
        }
    }

    /**
     * check if a given route is better or equal that this instance and update it accordingly
     * 
     * @param route
     * @return true if same available node
     */
    public boolean updateRoute(Route route){
        if(route.getAvailableNode() != this.availableNode) {
            // return false as different available node
            return false;
        }

        if (this.equals(route)) {
            //restore time to live
            this.restoreTimeToLive();
        } else if (this.cost > route.getCost()) {
            //update route
            this.restoreTimeToLive();
            this.nextHop = route.getNextHop();
            this.cost = route.getCost();
        }

        return true;
    }

    public void restoreTimeToLive() {
        this.timeToLive = TTL;
    }

    /**
     * available node getter
     * 
     * @return
     */
    public int getAvailableNode() {
        return this.availableNode;
    }

    /**
     * next hop getter
     * 
     * @return
     */
    public int getNextHop() {
        return this.nextHop;
    }

    /**
     * cost getter
     * 
     * @return
     */
    public int getCost() {
        return this.cost;
    }

    /**
     * time to live getter
     * 
     * @return
     */
    public int getTimeToLive() {
        return this.timeToLive;
    }

    @Override
    public boolean equals(Object o) {
        //check if object is also a Route class
        if (o.getClass() != Route.class) return false;

        Route route = (Route) o;
        return route.getAvailableNode() == this.availableNode || route.getNextHop() == this.nextHop || route.getCost() == this.cost;
    }
}
