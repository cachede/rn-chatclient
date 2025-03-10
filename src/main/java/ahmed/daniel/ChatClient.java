package ahmed.daniel;

import ahmed.daniel.messages.CommunicationMessage;
import ahmed.daniel.messages.ConnectionMessage;
import ahmed.daniel.messages.Message;
import ahmed.daniel.routing.RoutingTableManager;
import ahmed.daniel.routing.RoutingTableThread;
import java.net.*;
import java.io.*;
import java.util.Map;
import java.util.Set;
import java.util.Timer;


/**
 * This class implements all the actions a User can do in our Chat-Application like connecting, sending a message to a
 * destination in the network, showing all participants in the network and disconnecting from the network. This class
 * is responsible for all use cases by the user of the application (see use-case-Diagramm)
 */
public class ChatClient {
    private static final int MAX_NUMBER_OF_CLIENTS_WAITING = 5;

    private final ServerSocket serverSocket;
    private final String name;
    private final ActiveConnectionManager activeConnectionManager;
    private final RoutingTableManager routingTableManager;

    private final Thread accThread;
    private final RoutingTableThread routingThread;
    private final Timer timer;


    /**
     * Creates a ChatClient, over which the Client can perform all Client-side actions like connecting and
     * sending a message, showing participants and disconnecting. See use-case-Diagramm.
     *
     * @param ipAddress     The Ipv4-Address of the Client
     * @param port          The Port of the Client
     * @param name          The Name of the Client
     * @throws IOException  Throws IOException if the Socket for the given IPv4-Address and Port could not be instatiated
     */
    public ChatClient(InetAddress ipAddress, int port, String name) throws IOException {
        this.name = name;
        this.activeConnectionManager = new ActiveConnectionManager();
        this.routingTableManager = new RoutingTableManager();
        this.serverSocket = new ServerSocket(port, MAX_NUMBER_OF_CLIENTS_WAITING, ipAddress);

        this.accThread = new Thread(new AcceptThread(this.serverSocket, this.name, this.activeConnectionManager, this.routingTableManager));
        this.routingThread = new RoutingTableThread(routingTableManager, activeConnectionManager, name);
        this.timer = new Timer();
    }

    /**
     * Starts the essential Part of the ChatClient, like the Acceptingthread and the Routingthread. These Threads are
     * important, in order to guarantee a flawless connecting and message exchanging
     */
    public void startClient() {
        this.accThread.start();
        this.timer.scheduleAtFixedRate(routingThread, ProtocolConstants.TIME_BETWEEN_ROUTING_UPDATES_IN_MS, ProtocolConstants.TIME_BETWEEN_ROUTING_UPDATES_IN_MS);
    }

    /**
     * Adds a bidirectional Connection to given Ipv4-Address and Port. After the Socket is established both
     * participants exchange their name, so they know about each other. After that the user is able to send messages
     * to the desired participant on the network.
     *
     * @param ipv4address   the ipv4-address to which the client wants to connect to
     * @param port  the port on which the other participant waits for connections
     */
    public void addNewConnection(String ipv4address, int port) {
        try{
            //Connect to new Client
            Socket newSocket = connectTo(InetAddress.getByName(ipv4address), port);

            // Get name from new Client
            Runnable receiverTask = new ReceiverTask(newSocket, this.name, activeConnectionManager, routingTableManager);
            activeConnectionManager.addReceivingTask(receiverTask);
        }
        catch(IOException ie){
            //Logfile
            System.out.println("Konnte keine Verbindung herstellen zu " + ipv4address + " mit Port " + port);
        }
    }

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
     * Sends a ASCII-Message to a connected Participant in the network. The desired Participant could be directly connected,
     * or transitive connected.
     *
     * @param payload              Plain text message to be send
     * @param destinationName      Name of the Destination which should receive the Message
     */
    public void sendMessage(String payload, String destinationName) {
        if(payload.length() >= ProtocolConstants.MAX_CHARACTERS_PER_MESSAGE){
            System.out.println("Message was to long!");
            return;
        }
        if (!activeConnectionManager.getAllActiveConnectionNames().contains(destinationName)) {
            System.out.println("You have no active Connection to " + destinationName);
            return;
        } else if(activeConnectionManager.getSocketFromName(destinationName) == null || routingTableManager.getMinHopCountForDestination(destinationName) == ProtocolConstants.ROUTING_MAX_HOPCOUNT){
            activeConnectionManager.removeActiveConnection(destinationName);
            System.out.println("Connection to " + destinationName + " was removed!");
            return;
        }

        Socket pickedSocket = activeConnectionManager.getSocketFromName(destinationName);
        Message message = new CommunicationMessage(this.name, ProtocolConstants.TTL, payload);

        try {
            message.sendTo(pickedSocket, destinationName);
        } catch (IOException io){
            activeConnectionManager.closeActiveConnection(destinationName);    //Zu dem man sendet kann schon disconnected sein -> Test
            routingTableManager.setSourceAsUnreachable(destinationName);
        }
    }

    /**
     * Disconnects the Client from the Network. The Client DOES NOT send all active Connections, that he just disconnected.
     * It can never be guaranteed, that every Client in our Network disconnects smoothly
     */
    public void disconnect() {
        stopRouting();
        stopSocket();
        stopActiveConnections();
    }

    private void stopSocket() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            System.out.println("Stopping Socket");
        }
    }

    private void stopRouting() {
        timer.cancel();
    }

    // Redirect Methods to the Managers

    /**
     * Retrieves the names of all active connections managed by this class.
     *
     * @return A Set containing the names of all active connections
     */
    public Set<String> getActiveConnectionNames() {
        return this.activeConnectionManager.getAllActiveConnectionNames();
    }

    private void stopActiveConnections() {
        this.activeConnectionManager.shutdownReceiverPool();
        this.activeConnectionManager.stopActiveConnections();
    }

    /**
     * Prints all Entries of the Routingtable
     */
    public void printAllRoutingTables(){
        routingTableManager.printAllRoutingTables();
    }

    /**
     * Gets all active Connections of the Client
     * @return  A Set of Entries, which map the Name to the corresponding Socket
     */
    public Set<Map.Entry<String, Socket>> getActiveConnections() {
        return activeConnectionManager.getEntrySet();
    }
}
