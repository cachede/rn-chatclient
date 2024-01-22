package ahmed.daniel.routing;

import ahmed.daniel.ProtocolConstants;
import java.util.Collections;
import java.util.List;
import java.util.LinkedList;

public class RoutingTableManager{

    private final List<RoutingTableEntry> routingtable;

    public RoutingTableManager() {
        this.routingtable = Collections.synchronizedList(new LinkedList<>());
    }
    
    public List<RoutingTableEntry> getRoutingTables(){
        synchronized (this.routingtable) {
            return this.routingtable;
        }
    }
    
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

    public int getSize() {
        synchronized (this.routingtable) {
            return routingtable.size();
        }
    }

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
     *
     * @param destination Destination that we are searching the shortest path for
     * @return if we do not find a root -> null
     *         else -> the nextHop for the shortest path
     */
    public String getRouteWithMinHopCountForDestination(String destination){
        synchronized (this.routingtable) {
            String minHopCountNextHop = null;
            byte minHopCount = ProtocolConstants.ROUTING_MAX_HOPCOUNT;

            for (RoutingTableEntry routingTable : this.routingtable){
                if (destination.equals(routingTable.getDestination()) && routingTable.getHopCount() < minHopCount){
                    minHopCountNextHop = routingTable.getNextHop();
                    minHopCount = routingTable.getHopCount();
                }
            }

            return minHopCountNextHop;
        }
    }

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

    public void printAllRoutingTables(){
        synchronized (this.routingtable) {
            for(RoutingTableEntry routingTable : this.routingtable){
                routingTable.printRoutingTable();
            }
        }
    }
}

    