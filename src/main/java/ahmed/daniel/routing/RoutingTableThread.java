package ahmed.daniel.routing;

import ahmed.daniel.ActiveConnectionManager;


public class RoutingTableThread implements Runnable{

    private ActiveConnectionManager activeConnectionManager;
    public RoutingTableThread(ActiveConnectionManager activeConnectionManager) {
        this.activeConnectionManager = activeConnectionManager;
    }





    @Override
    public void run() {
        //Send RoutingTable to all activeConnections

        //wait x-Seconds
    }
    
}
