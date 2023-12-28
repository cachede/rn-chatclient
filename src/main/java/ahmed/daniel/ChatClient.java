package ahmed.daniel;

import ahmed.daniel.Messages.CommunicationMessage;
import ahmed.daniel.Messages.ConnectionMessage;
import ahmed.daniel.Messages.Message;
import ahmed.daniel.routing.RoutingTableManager;
import ahmed.daniel.routing.RoutingTableThread;

import java.net.*;
import java.io.*;
import java.util.Timer;

public class ChatClient {
    private ServerSocket serverSocket;
    private String name;
    //The started Clients IP-Address
    private InetAddress ipAddress;

    //The started Clients Port
    private final int port;

    private final ActiveConnectionManager activeConnectionManager;
    private final RoutingTableManager routingTableManager;

    private final Thread accThread;
    private final RoutingTableThread routingThread;
    private final Timer timer;
    //private final Thread routThread;

    /**
     * This is very important. It shows the possible IPv4-Addresses which can be addressed. Usually gets filled by
     * the receiveMessage()-Method (Eigenes Protokoll)
     *
     */
    public ChatClient(InetAddress ipAddress, int port, String name) {
        this.ipAddress = ipAddress;
        this.port = port;
        this.name = name;
        this.activeConnectionManager = new ActiveConnectionManager();
        this.routingTableManager = new RoutingTableManager();
        try {
            this.serverSocket = new ServerSocket(this.port, 5, this.ipAddress);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("ERROR CREATING SERVER SOCKET");
        }

        this.accThread = new Thread(new AcceptThread(this.serverSocket, this.name, this.activeConnectionManager, this.routingTableManager));
        this.routingThread = new RoutingTableThread(routingTableManager, activeConnectionManager, name);
        timer = new Timer();
        //TODO
        //this.routThread = new Thread(new RoutingThread());
    }

    public void startClient() {
        this.accThread.start();
        timer.scheduleAtFixedRate(routingThread, 5000, 5000);
        // TODO
        //this.routThread.start();
    }

    public Socket accept() {
        try {
            return this.serverSocket.accept();
        }catch(IOException e) {
            System.out.println("Konnte auf keine Verbindung listen");
            return null;
        }
    }

    /**
     * Sends a ASCII-Message to the desired IP-Adress.
     * @param payload           -   Message in bytes to be send
     * @param destinationName   -   Name of the Destination which should receive the Message
     */
    public void sendMessage(String payload, String destinationName) {
        Socket pickedSocket = activeConnectionManager.getSocketFromName(destinationName);
        Message message = new CommunicationMessage(this.name, payload);
        message.sendTo(pickedSocket, destinationName);
    }

    public void sendName(Socket socket){
        Message message = new ConnectionMessage(this.name);
        message.sendTo(socket, ProtocolConstants.DESTINATION_NETWORK_NAME_NOT_SET);
    }

    /**
     * This method is used, when the ChatClient wants to Connect to another Client. The other Client is referenced
     * by his IPv4-Address.
     * @param ipAddress -   IPv4-Address of the desired Client
     * @param port      -   Port on which you want to connect
     */
    public Socket connectTo(InetAddress ipAddress, int port) throws IOException, NullPointerException, IllegalArgumentException{
        Socket newSocket = new Socket(ipAddress, port);
        sendName(newSocket); // unseren namen schicken mit flag 0
        return newSocket;
    }

    public void stopSocket() {
        try {
            serverSocket.close();

        } catch (IOException e) {
            System.out.println("Stopping Socket");
        } catch (NullPointerException no) {
            System.out.println("Cannot close a Connection if there is no Connection");
        }
    }

    public void stopRouting() {
        timer.cancel();
    }

    public String getName() {
        return this.name;
    }

    public void addNewConnection(String ipv4address, int port) {
        try{
            //Connect to new Client
            Socket newSocket = connectTo(InetAddress.getByName(ipv4address), port);

            //TODO Here we have to get destinationname -> wait on socket with new socket
            // Discard new connection if we dont get a valid name


            // Inform the Managers and Start Recv-Thread
            //activeConnectionManager.addActiveConnection(destinationName, newSocket);
            Runnable receiverTask = new ReceiverTask(newSocket, this.name, activeConnectionManager, routingTableManager);
            activeConnectionManager.addReceivingTask(receiverTask);
            //routingTableManager.addRoutingTableEntry(getName(), destinationName, (short)port, (byte)1);
        }
        catch(IOException ie){
            System.out.println("Konnte keine Verbindung herstellen zu " + ipv4address + "mit Port " + port);
        }
    }

    // Redirect Methods to the Managers
    public void printActiveConnections(){
        this.activeConnectionManager.printActiveConnections();
    }

    public void stopActiveConnections(){
        this.activeConnectionManager.stopActiveConnections();
        this.activeConnectionManager.shutdownReceiverPool();
    }

    public void printAllRoutingTables(){
        routingTableManager.printAllRoutingTables();
    }
}
