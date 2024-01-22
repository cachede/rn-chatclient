package ahmed.daniel.routing;

import ahmed.daniel.ActiveConnectionManager;
import ahmed.daniel.Messages.Message;
import ahmed.daniel.Messages.RoutingMessage;
import ahmed.daniel.ProtocolConstants;

import java.io.IOException;
import java.net.Socket;
import java.util.*;

public class RoutingTableThread extends TimerTask {

    private final RoutingTableManager routingTableManager;
    private final ActiveConnectionManager activeConnectionManager;
    private final String name;

    public RoutingTableThread(RoutingTableManager routingTableManager, ActiveConnectionManager activeConnectionManager, String name) {
        this.routingTableManager = routingTableManager;
        this.activeConnectionManager = activeConnectionManager;
        this.name = name;
    }

    @Override
    public void run() {

        List<RoutingTable> routingList = List.copyOf(this.routingTableManager.getRoutingTables());

        synchronized (activeConnectionManager.getActiveConnections()){
            Iterator<Map.Entry<String, Socket>> iterator = activeConnectionManager.getActiveConnectionEntrySet().iterator();

            while(iterator.hasNext()) {
                String connectionName = iterator.next().getKey();
                Message routingMessage = new RoutingMessage(this.name, ProtocolConstants.TTL, getExtractedRoutingTable(routingList, connectionName));

                // Check if active Connection is still active
                int minHopCountToConnection = routingTableManager.getMinHopCountForDestination(connectionName);
                if (minHopCountToConnection >= ProtocolConstants.ROUTING_MAX_HOPCOUNT){
                    iterator.remove();
                    continue;
                }

                // Send Routingtables to direct Neighbors
                if(routingTableManager.getMinHopCountForDestination(connectionName) == 1) {
                    Socket conSocket = activeConnectionManager.getSocketFromName(connectionName);

                    try {
                        routingMessage.sendTo(conSocket, connectionName);
                    } catch (IOException ioException) {
                        System.out.println("No connection to " + connectionName + "... setting as unreachable");
                        routingTableManager.setSourceAsUnreachable(connectionName);

                        activeConnectionManager.CloseActiveConnection(connectionName);
                        iterator.remove();
                    }
                }
            }
        }

    }

    private List<RoutingTable> getExtractedRoutingTable(List<RoutingTable> routingTables, String connectionName){
        List<RoutingTable> newRoutingTable = List.copyOf(routingTables);

        // Only keep the best Route to a Destination
        Map<String, RoutingTable> bestRoutes = new HashMap<>();
        for (RoutingTable routingTableEntry : newRoutingTable) {
            String destination = routingTableEntry.getDestination();
            String nextHop = routingTableEntry.getNextHop();
            int hopCount = routingTableEntry.getHopCount();


            if (!bestRoutes.containsKey(destination)) {
                bestRoutes.put(destination, routingTableEntry);
            } else if (hopCount < bestRoutes.get(destination).getHopCount()){
                bestRoutes.put(destination, routingTableEntry);
            } else if (hopCount == bestRoutes.get(destination).getHopCount() && !nextHop.equals(connectionName)){
                bestRoutes.put(destination, routingTableEntry);
            }
        }

        //Remove If destination if same as conncetion name
        List<RoutingTable> bestRoutesList = new ArrayList<>(bestRoutes.values());
        bestRoutesList.removeIf(routingTableEntry -> routingTableEntry.getDestination().equals(connectionName)); //|| routingTable.getNextHop().equals(connectionName));

        List<RoutingTable> routesAfterPoisionReverse = new ArrayList<>();

        // Poision Reverse
        for(RoutingTable routingTableEntry : bestRoutesList){
            if (routingTableEntry.getNextHop().equals(connectionName)){
                routesAfterPoisionReverse.add(new RoutingTable(routingTableEntry.getDestination(), routingTableEntry.getNextHop(), ProtocolConstants.ROUTING_MAX_HOPCOUNT));
            } else {
                routesAfterPoisionReverse.add(routingTableEntry);
            }
        }

        return routesAfterPoisionReverse;
    }
}
