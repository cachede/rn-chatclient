package ahmed.daniel.routing;
import ahmed.daniel.ProtocolConstants;
import java.util.Objects;

/**
 * Represents an entry in a routing table, containing information about a destination,
 * the next hop towards that destination, and the hop count to reach the destination.
 */
public class RoutingTableEntry {
    

    private final String destination;
    private final String nextHop;
    private byte hopCount;


    /**
     * Constructs a new RoutingTableEntry with the specified destination, next hop,
     * and hop count.
     *
     * @param destination The destination for which this entry provides routing information
     * @param nextHop The next hop towards the destination
     * @param hopCount The hop count (amount of hops) to reach the destination
     */
    public RoutingTableEntry(String destination, String nextHop, byte hopCount) {
        this.destination = destination;
        this.nextHop= nextHop;
        this.hopCount = hopCount;
    }

    /**
     * Gets the destination for which this entry provides routing information.
     *
     * @return The destination
     */
    public String getDestination() {
        return this.destination;
    }

    /**
     * Gets the next hop towards the destination.
     *
     * @return The next hop
     */
    public String getNextHop() {
        return nextHop;
    }

    /**
     * Gets the hop count to reach the destination.
     *
     * @return The hop count
     */
    public byte getHopCount() {
        return this.hopCount;
    }

    /**
     * Sets a new hop count value.
     *
     * @param newHopCount The new hop count
     */
    public void setHopCount(byte newHopCount){
        this.hopCount = newHopCount;
    }

    /**
     * Sets the hop count to indicate that the destination is unreachable. The unrechable hopcount is 16
     */
    public void setAsUnreachable(){
        this.hopCount = ProtocolConstants.ROUTING_DESTINATION_UNREACHABLE;
    }

    /**
     * Prints the routing table entry information.
     */
    public void printRoutingTable() {
        System.out.println(this);
    }

    @Override
    public String toString(){
        return "------------------------\n" +
                "Routingtable:\n" +
                "Destination:\t" + this.destination + "\n" +
                "Next Hop:\t" + this.nextHop + "\n" +
                "Hop Count:\t" + this.hopCount + "\n" +
                "------------------------\n";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoutingTableEntry that = (RoutingTableEntry) o;
        return Objects.equals(destination, that.destination) && Objects.equals(nextHop, that.nextHop);
    }

    @Override
    public int hashCode() {
        return Objects.hash(destination, nextHop);
    }
}