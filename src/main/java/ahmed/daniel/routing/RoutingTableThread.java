package ahmed.daniel.routing;

import ahmed.daniel.ActiveConnectionManager;
import ahmed.daniel.Messages.Message;
import ahmed.daniel.Messages.RoutingMessage;

import java.io.IOException;
import java.net.Socket;
import java.util.TimerTask;

import java.util.List;

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
        //Message routingMessage = new RoutingMessage(this.name, routingList);

        Iterator<String> iterator = this.activeConnectionManager.getAllActiveConnectionName().iterator();

        while(iterator.hasNext()) {
            String connectionName = iterator.next();
            Message routingMessage = new RoutingMessage(this.name, getExtractedRoutingTable(routingList, connectionName));

            if(routingTableManager.getMinHopCountForDestination(connectionName) == 1) {
                Socket conSocket = activeConnectionManager.getSocketFromName(connectionName);

                try {
                    routingMessage.sendTo(conSocket, connectionName);
                } catch (IOException ioException) {
                    routingTableManager.setSourceAsUnreachable(connectionName);
                    iterator.remove();
                }
            }
        }
    }
}
