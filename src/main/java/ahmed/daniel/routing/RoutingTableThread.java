package ahmed.daniel.routing;

import ahmed.daniel.ActiveConnectionManager;
import ahmed.daniel.Messages.Message;
import ahmed.daniel.Messages.RoutingMessage;

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
        //System.out.println("IGEL STARTKLAR");
        List<RoutingTable> routingList = List.copyOf(this.routingTableManager.getRoutingTables());
        Message routingMessage = new RoutingMessage(this.name, routingList);
        for(String connection : this.activeConnectionManager.getAllActiveConnectionName()){
            Socket conSocket = activeConnectionManager.getSocketFromName(connection);
            //System.out.println("WIR HABEN IGEL GESENDET!");
            routingMessage.sendTo(conSocket, connection);
        }
    }
}
