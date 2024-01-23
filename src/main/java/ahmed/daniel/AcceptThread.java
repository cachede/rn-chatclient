package ahmed.daniel;

import ahmed.daniel.routing.RoutingTableManager;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Is responsible for the Accepting new Connections which come from other participants in the network.
 * Implements Runnable, because accepting new Connections should be handled in an own Thread.
 */
public class AcceptThread implements Runnable {

    private final ServerSocket serverSocket;
    private final String name;
    private final ActiveConnectionManager activeConnectionManager;
    private final RoutingTableManager routingTableManager;

    /**
     * Creates an AcceptThread which should listen for incoming connections and add them to the active connection.
     *
     * @param serverSocket  AcceptThread listens on this ServerSocket for connections
     * @param name      When accepting a new Connection the AcceptThread should send his own name to the other participant
     * @param activeConnectionManager   When connection is established the AcceptThread should add it to the activeConnectionManager
     * @param routingTableManager       When connection is established the AcceptThread should add a new Entry in the RoutingTableManager
     */
    public AcceptThread(ServerSocket serverSocket, String name, ActiveConnectionManager activeConnectionManager, RoutingTableManager routingTableManager) {
        this.serverSocket = serverSocket;
        this.name = name;
        this.activeConnectionManager = activeConnectionManager;
        this.routingTableManager = routingTableManager;
    }

    /**
     * While the current AccepThread is not interrupted it should constantly listen for new Connections.
     * If a new Connection has been accepted, a new ReceiverTask is added to the ActiveConnectionManager
     */
    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            Socket newSocket;
            try {
                newSocket = this.serverSocket.accept();
            } catch (IOException e) {
                System.err.println("ServerSocket of Client cannot accept Connections");
                break;
            }

            Runnable receiverTask = new ReceiverTask(newSocket, this.name, this.activeConnectionManager, this.routingTableManager);
            activeConnectionManager.addReceivingTask(receiverTask);
        }
    }
}
