package ahmed.daniel.routing;

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

    public void printAllRoutingTables(){
        for(RoutingTable routingTable : this.routingtables){
            routingTable.printRoutingTable();
        }
    }
    
}

    