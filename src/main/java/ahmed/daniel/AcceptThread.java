package ahmed.daniel;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;


public class AcceptThread implements Runnable {

    private ServerSocket serverSocket;
    private ActiveConnectionManager activeConnectionManager;
    private RoutingTableManager routingTableManager;

    public AcceptThread(ServerSocket serverSocket, ActiveConnectionManager activeConnectionManager, RoutingTableManager routingTableManager) {
        this.serverSocket = serverSocket;
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
                throw new RuntimeException(e);
            }

            System.out.println("NEW CONNECTION: " + newSocket.getPort());
            //Das darunter kann besser gemacht werden
            //Thread receivingThread = new Thread(new ReceiverTask(newSocket, activeConnectionManager, routingTableManager));

            //activeConnectionManager.addReceivingThread(receivingThread);

            ///
            Runnable receiverTask = new ReceiverTask(newSocket, activeConnectionManager, routingTableManager);
            activeConnectionManager.addReceivingTask(receiverTask);
        }
    }
}
