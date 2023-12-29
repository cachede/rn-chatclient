package ahmed.daniel.routing;

import ahmed.daniel.ProtocolConstants;

import java.util.Collections;
import java.util.List;
import java.util.Iterator;
import java.util.LinkedList;

public class RoutingTableManager{

    private final List<RoutingTable> routingtables;

    public RoutingTableManager() {
        this.routingtables = new LinkedList<>();
    }
    
    public synchronized List<RoutingTable> getRoutingTables(){
        return this.routingtables;
    }
    
    public synchronized void addRoutingTableEntry(String destination, String nextHop, byte hopCount){
        RoutingTable newRoutingTable = new RoutingTable(destination, nextHop, hopCount);
        if(routingtables.contains(newRoutingTable)) {
            return;
        }
        this.routingtables.add(newRoutingTable);
    }

    public synchronized int getSize() {
        return routingtables.size();
    }

    public void removeRoutingTableEntry(String source) {
        routingtables.removeIf(routingTable -> routingTable.getNextHop().equals(source));
    }

    /**
     *
     * @param destination Destination that we are searching the shortest path for
     * @return if we do not find a root -> null
     *         else -> the nextHop for the shortest path
     */
    public String getRouteWithMinHopCountForDestination(String destination){
        String minHopCountNextHop = null;
        byte minHopCount = ProtocolConstants.ROUTING_MAX_HOPCOUNT;

        for (RoutingTable routingTable : this.routingtables){
            if (destination.equals(routingTable.getDestination()) && routingTable.getHopCount() < minHopCount){
               minHopCountNextHop = routingTable.getNextHop();
               minHopCount = routingTable.getHopCount();
            }
        }

        return minHopCountNextHop;
    }

    public byte getMinHopCountForDestination(String destination){
        byte minHopCount = ProtocolConstants.ROUTING_MAX_HOPCOUNT;

        for (RoutingTable routingTable : this.routingtables){
            if(destination.equals(routingTable.getDestination()) && routingTable.getHopCount() < minHopCount){
                minHopCount = routingTable.getHopCount();
            }
        }

        return minHopCount;
    }

    public void printAllRoutingTables(){
        for(RoutingTable routingTable : this.routingtables){
            routingTable.printRoutingTable();
        }
    }
    
}

    