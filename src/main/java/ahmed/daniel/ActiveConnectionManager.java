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
 * Handles all the connections of a ChatClient. Is used, when the ChatClient wants to send a message to a participant
 * in the network. In order to get the right Socket, to which the ChatClient sends the message, it asks the
 * ActiveConnectionManager to get a Socket, to which he sends a message. The picked Socket could be the desired
 * Participant, or a representative that could send the message further through the network. (See protocol)
 *
 * Also this class holds a TaskPool for every active Connection, because every active Connection could send a message
 * to the ChatClient. Therefore the ChatClient needs a Thread, which receives the Messages from those active Connections
 *
 */
public class ActiveConnectionManager{
    private final Map<String, Socket> activeConnections;

    private final ExecutorService receiverPool;
    private static final int MAX_RECEIVER_THREADS = 5;

    /**
     * Creates a ActiveConnectionManager, which handles the mapping from Name to Socket.
     * If a message is to be send to another participant in the network, it goes through the activeConnectionManager to
     * get the right Socket (see protocol), regardless of what Message(see protocol).
     * Also creates a TaskPool of 5 Threads, which handle the receiving of Messages.
     */
    public ActiveConnectionManager() {
        this.activeConnections = Collections.synchronizedMap(new HashMap<>());
        this.receiverPool= Executors.newFixedThreadPool(MAX_RECEIVER_THREADS);
    }

    public Map<String, Socket> getActiveConnections(){
        synchronized (this.activeConnections) {
            return this.activeConnections;
        }
    }

    public Set<Map.Entry<String, Socket>> getActiveConnectionEntrySet(){
        synchronized (this.activeConnections){
            return this.activeConnections.entrySet();
        }
    }

    /**
     * Adds a ReceivingTask, which is responsible to receive any type of messages by his direct Participants.
     * This function will be called, when a new Connection to a Participant has been established.
     *
     * TODO: I think the parameter should not be Runnable, because this function should only allow ReceiverTasks, but in reality it allows any Tasks
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
    public void CloseActiveConnection(String toBeClosedName){
        synchronized (this.activeConnections){
            try {
                this.activeConnections.get(toBeClosedName).close();
                //TODO I think we just close here
                //this.activeConnections.remove(toBeClosedName);
            } catch (IOException e) {
                System.out.println("RemoveActionConnection konnte Socket nicht schließen");
            }
        }
    }

    // TODO
    public void removeActiveConnection(String toBeRemovedName){
        synchronized (this.activeConnections){
            this.activeConnections.remove(toBeRemovedName);
        }
    }

    /**
     * Gets the entire EntrySet for all active Connections.
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