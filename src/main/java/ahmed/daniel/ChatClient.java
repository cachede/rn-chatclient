package ahmed.daniel;

import ahmed.daniel.Messages.CommunicationMessage;
import ahmed.daniel.Messages.ConnectionMessage;
import ahmed.daniel.Messages.Message;
import ahmed.daniel.routing.RoutingTableManager;
import ahmed.daniel.routing.RoutingTableThread;

import java.net.*;
import java.io.*;
import java.util.Map;
import java.util.Set;
import java.util.Timer;

public class ChatClient {
    private final ServerSocket serverSocket;
    private final String name;
    //The started Clients IP-Address
    private InetAddress ipAddress;

    //The started Clients Port
    private final int port;

    private final ActiveConnectionManager activeConnectionManager;
    private final RoutingTableManager routingTableManager;

    private final Thread accThread;
    private final RoutingTableThread routingThread;
    private final Timer timer;
    private final static int delay = 5000;
    private final static int period = 5000;


    public ChatClient(InetAddress ipAddress, int port, String name) throws IOException {
        this.ipAddress = ipAddress;
        this.port = port;
        this.name = name;
        this.activeConnectionManager = new ActiveConnectionManager();
        this.routingTableManager = new RoutingTableManager();
        this.serverSocket = new ServerSocket(this.port, 5, this.ipAddress);

        this.accThread = new Thread(new AcceptThread(this.serverSocket, this.name, this.activeConnectionManager, this.routingTableManager));
        this.routingThread = new RoutingTableThread(routingTableManager, activeConnectionManager, name);
        this.timer = new Timer();

    }

    public void startClient() {
        this.accThread.start();
        this.timer.scheduleAtFixedRate(routingThread, delay, period);
    }

    public void addNewConnection(String ipv4address, int port) {
        try{
            //Connect to new Client
            Socket newSocket = connectTo(InetAddress.getByName(ipv4address), port);

            // Get name from new Client
            Runnable receiverTask = new ReceiverTask(newSocket, this.name, activeConnectionManager, routingTableManager);
            activeConnectionManager.addReceivingTask(receiverTask);
        }
        catch(IOException ie){
            System.out.println("Konnte keine Verbindung herstellen zu " + ipv4address + " mit Port " + port);
        }
    }

    /**
     * This method is used, when the ChatClient wants to Connect to another Client. The other Client is referenced
     * by his IPv4-Address.
     * @param ipAddress -   IPv4-Address of the desired Client
     * @param port      -   Port on which you want to connect
     */
    private Socket connectTo(InetAddress ipAddress, int port) throws IOException {
        Socket newSocket = new Socket(ipAddress, port);
        sendName(newSocket);
        return newSocket;
    }

    private void sendName(Socket socket) throws IOException {
        Message message = new ConnectionMessage(this.name);
        message.sendTo(socket, ProtocolConstants.DESTINATION_NETWORK_NAME_NOT_SET);
    }

    /**
     * Sends a ASCII-Message to the desired IP-Adress.
     * @param payload           -   Message in bytes to be send
     * @param destinationName   -   Name of the Destination which should receive the Message
     */
    public void sendMessage(String payload, String destinationName) {
        if (!activeConnectionManager.getAllActiveConnectionName().contains(destinationName)) {
            System.out.println("You have no active Connection to " + destinationName);
            return;
        }

        Socket pickedSocket = activeConnectionManager.getSocketFromName(destinationName);
        Message message = new CommunicationMessage(this.name, payload);

        try {
            message.sendTo(pickedSocket, destinationName);

        } catch (IOException io){
            activeConnectionManager.removeActiveConnection(destinationName);    //Zu dem man sendet kann schon disconnected sein -> Test
            routingTableManager.setSourceAsUnreachable(destinationName);
        }
    }

    public void stopSocket() {
        try {
            serverSocket.close();

        } catch (IOException e) {
            System.out.println("Stopping Socket");
        }
    }

    public void stopRouting() {
        timer.cancel();
    }

    // Redirect Methods to the Managers
    public Set<String> getActiveConnectionNames() {
        return this.activeConnectionManager.getActiveConnections();
    }

    public void stopActiveConnections() {
        this.activeConnectionManager.stopActiveConnections();
        this.activeConnectionManager.shutdownReceiverPool();
    }

    public void printAllRoutingTables(){
        routingTableManager.printAllRoutingTables();
    }

    public Set<Map.Entry<String, Socket>> getActiveConnections() {
        return activeConnectionManager.getEntrySet();
    }

}
