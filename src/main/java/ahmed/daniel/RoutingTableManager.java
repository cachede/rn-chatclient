package ahmed.daniel;

import java.util.List;
import java.util.Iterator;
import java.util.LinkedList;

public class RoutingTableManager{

    private List<RoutingTable> routingtables;

    public RoutingTableManager() {
        this.routingtables = new LinkedList<>();
    }
    
    public synchronized List<RoutingTable> getRoutingTables(){
        return this.routingtables;
    }
    
    public synchronized void addRoutingTableEntry(String source, String destination, short port, byte hopCount){
        RoutingTable newRoutingTable = new RoutingTable(source, destination, port, hopCount);
        this.routingtables.add(newRoutingTable);
    }

    public void removeRoutingTableEntry(String source) {
        Iterator<RoutingTable> iterator = routingtables.iterator();
        while (iterator.hasNext()) {
            RoutingTable routingTable = iterator.next();
            if (routingTable.getSource().equals(source)) {
                iterator.remove();
            }
        }
    }

    public void printAllRoutingTables(){
        for(RoutingTable routingTable : this.routingtables){
            routingTable.printRoutingTable();
        }
    }
    
}

    