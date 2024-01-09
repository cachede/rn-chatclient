package ahmed.daniel;

import ahmed.daniel.routing.RoutingTableManager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class AcceptThread implements Runnable {

    private final ServerSocket serverSocket;
    private final String name;
    private final ActiveConnectionManager activeConnectionManager;
    private final RoutingTableManager routingTableManager;

    public AcceptThread(ServerSocket serverSocket, String name, ActiveConnectionManager activeConnectionManager, RoutingTableManager routingTableManager) {
        this.serverSocket = serverSocket;
        this.name = name;
        this.activeConnectionManager = activeConnectionManager;
        this.routingTableManager = routingTableManager;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            Socket newSocket = null;
            try {
                newSocket = this.serverSocket.accept();
            } catch (IOException e) {
                break;
            }

            //System.out.println("NEW CONNECTION: " + newSocket.getPort());
            Runnable receiverTask = new ReceiverTask(newSocket, this.name, this.activeConnectionManager, this.routingTableManager);
            activeConnectionManager.addReceivingTask(receiverTask);
        }
    }
}
