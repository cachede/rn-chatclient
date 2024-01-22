package ahmed.daniel.routing;

import ahmed.daniel.ProtocolConstants;
import java.util.Collections;
import java.util.List;
import java.util.LinkedList;

/**
 * Manages the routing table for a client, handling operations such as adding entries,
 * updating hop counts, marking sources as unreachable, and providing information about the routing table.
 */
public class RoutingTableManager{

    private final List<RoutingTableEntry> routingtable;

    /**
     * Constructs a new RoutingTableManager with an empty synchronized list for the routing table.
     */
    public RoutingTableManager() {
        this.routingtable = Collections.synchronizedList(new LinkedList<>());
    }

    /**
     * Get the current routing table entries.
     *
     * @return routing table
     */
    public List<RoutingTableEntry> getRoutingTables(){
        synchronized (this.routingtable) {
            return this.routingtable;
        }
    }

    /**
     * Adds a new routing table entry or updates the hop count if the entry already exists.
     *
     * @param destination The destination address for the routing entry
     * @param nextHop     The next hop address for the routing entry
     * @param hopCount    The hop count for the routing entry
     */
    public void addRoutingTableEntry(String destination, String nextHop, byte hopCount){
        synchronized (this.routingtable){
            // Unreachable
            if(hopCount >= ProtocolConstants.ROUTING_DESTINATION_UNREACHABLE) {
                hopCount = ProtocolConstants.ROUTING_DESTINATION_UNREACHABLE;
            }

            RoutingTableEntry newRoutingTable = new RoutingTableEntry(destination, nextHop, hopCount);
            if(this.routingtable.contains(newRoutingTable)) {
                updateHopCountOfRoutingTable(destination, nextHop, hopCount);
                return;
            }

            this.routingtable.add(newRoutingTable);
        }
    }

    private void updateHopCountOfRoutingTable(String destination, String nextHop, byte newHopCount){
        synchronized (this.routingtable) {
            for(RoutingTableEntry routingTable : this.routingtable){
                if(routingTable.getDestination().equals(destination) && routingTable.getNextHop().equals(nextHop)){
                    routingTable.setHopCount(newHopCount);
                }
            }
        }
    }


    /**
     * Marks a source as unreachable in the routing table.
     *
     * @param source The source address to mark as unreachable
     */
    public void setSourceAsUnreachable(String source) {
        synchronized (this.routingtable){
            for(RoutingTableEntry routingTable : this.routingtable){
                if (routingTable.getNextHop().equals(source)){
                    routingTable.setAsUnreachable();
                }
            }
        }
    }

    /**
     * Get the minimum hop count for a specific destination from the routing table.
     *
     * @param destination The destination address to find the minimum hop count for.
     * @return The minimum hop count for the specified destination. If destination is not reacheable -> 16
     */
    public byte getMinHopCountForDestination(String destination){
        synchronized (this.routingtable) {
            byte minHopCount = ProtocolConstants.ROUTING_MAX_HOPCOUNT;

            for (RoutingTableEntry routingTable : this.routingtable){
                if(destination.equals(routingTable.getDestination()) && routingTable.getHopCount() < minHopCount){
                    minHopCount = routingTable.getHopCount();
                }
            }

            return minHopCount;
        }

    }

    /**
     * Prints all entries in the routing table.
     */
    public void printAllRoutingTables(){
        synchronized (this.routingtable) {
            for(RoutingTableEntry routingTable : this.routingtable){
                routingTable.printRoutingTable();
            }
        }
    }
}

