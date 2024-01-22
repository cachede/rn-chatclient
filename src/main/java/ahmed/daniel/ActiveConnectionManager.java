package ahmed.daniel;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Manages active connections, providing a mapping from participant names to sockets.
 * Also handles the receiving of messages through a thread pool.
 */
public class ActiveConnectionManager{
    private final Map<String, Socket> activeConnections;

    private final ExecutorService receiverPool;
    private static final int MAX_RECEIVER_THREADS = 5;

    /**
     * Creates an ActiveConnectionManager
     */
    public ActiveConnectionManager() {
        this.activeConnections = Collections.synchronizedMap(new HashMap<>());
        this.receiverPool= Executors.newFixedThreadPool(MAX_RECEIVER_THREADS);
    }

    /**
     * Gets the map of active connections.
     *
     * @return A synchronized map of active connections
     */
    public Map<String, Socket> getActiveConnections(){
        synchronized (this.activeConnections) {
            return this.activeConnections;
        }
    }

    /**
     * Gets the entry set of active connections.
     *
     * @return A synchronized set of map entries representing active connections
     */
    public Set<Map.Entry<String, Socket>> getActiveConnectionEntrySet(){
        synchronized (this.activeConnections){
            return this.activeConnections.entrySet();
        }
    }

    /**
     * Adds a ReceivingTask, which is responsible to receive any type of messages by his direct Participants.
     * This function will be called, when a new Connection to a Participant has been established.
     *
     * @param recThread     A ReceivingTask to perform the message receiving
     */
    public synchronized void addReceivingTask(Runnable recThread){
        receiverPool.execute(recThread);
    }

    /**
     * Adds a Connection to the Manager. This active Connection is later used, when a message is to be send.
     * @param name  The name which is used to identify the socket
     * @param socket    The associated Socket for the name. This Socket does NOT indicate a direct connection to the
     *                  name. This socket could lead to a complete different participant in the network (see protocol).
     */
    public void addActiveConnection(String name, Socket socket){
        synchronized (this.activeConnections){
            this.activeConnections.put(name, socket);
        }
    }

    /**
     * Gives a specific Socket for the corresponding name. IMPORTANT this socket does not have to be directly connected
     * to the given name. It could be a representative for the name.
     * @param name  Name of the participant, which you want to send a message to
     * @return  a Socket, to which you should send the message, in order to get the paket to the desired name
     */
    public Socket getSocketFromName(String name){
        synchronized (this.activeConnections){
            return this.activeConnections.get(name);
        }
    }


    /**
     * Gets all active connection to which the user has in the network
     * @return  A set of names, which are available in the network
     */
    public Set<String> getAllActiveConnectionNames() {
        synchronized (this.activeConnections){
            return activeConnections.keySet();
        }
    }


    /**
     * Removes an active Connection by first closing the socket for the name and removing this entry in the manager
     * @param toBeClosedName   name which is to be removed
     */
    public void closeActiveConnection(String toBeClosedName){
        synchronized (this.activeConnections){
            if(!this.activeConnections.containsKey(toBeClosedName)){
                return;
            }
            try {
                this.activeConnections.get(toBeClosedName).close();
            } catch (IOException e) {
                System.out.println("RemoveActionConnection konnte Socket nicht schließen");
            }
        }
    }

    /**
     * Removes an active connection by its name.
     *
     * @param toBeRemovedName Name to be removed from active connections
     */
    public void removeActiveConnection(String toBeRemovedName){
        synchronized (this.activeConnections){
            if (!this.activeConnections.containsKey(toBeRemovedName)){
                return;
            }
            this.activeConnections.remove(toBeRemovedName);
        }
    }

    /**
     * Gets the entire EntrySet for all active Connections.
     *
     * @return  A set of entries, which have the name as a key and the corresponding socket as a value
     */
    public Set<Entry<String, Socket>> getEntrySet() {
        synchronized (this.activeConnections){
            return activeConnections.entrySet();
        }
    }

    /**
     * Stops all active Connections by closing all sockets in the manager. This method is called, when a user wants
     * to disconnect from the network
     */
    public void stopActiveConnections() {
        for (String name : this.activeConnections.keySet()) {
            try {
                activeConnections.get(name).close();
            } catch (IOException e) {
                System.out.println("FEHLER BEIM CLOSEN VON SOCKETS");
            }
        }
    }

    /**
     * Shuts the TaskPool down. Every active Task which is currently being worked on shutsdown, as well as the tasks
     * waiting to be executed in the queue
     */
    public void shutdownReceiverPool(){
        this.receiverPool.shutdown();
    }
}