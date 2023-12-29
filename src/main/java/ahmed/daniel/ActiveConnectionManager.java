package ahmed.daniel;

import ahmed.daniel.routing.RoutingTable;

import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.io.IOException;
import java.net.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ActiveConnectionManager{
    private final Map<String, Socket> activeConnections;

    private final ExecutorService receiverPool;
    private static final int MAX_RECEIVER_THREADS = 5;
    public ActiveConnectionManager() {
        this.activeConnections = new HashMap<>();
        this.receiverPool= Executors.newFixedThreadPool(MAX_RECEIVER_THREADS);
    }

    public synchronized void addReceivingTask(Runnable recThread){
        receiverPool.execute(recThread);
    }

    public synchronized void addActiveConnection(String name, Socket socket){
        this.activeConnections.put(name, socket);
    }
    
    public synchronized Socket getSocketFromName(String name){
        return this.activeConnections.get(name);
    }

    public synchronized Set<String> getAllActiveConnectionName() {
        return activeConnections.keySet();
    }

    public synchronized void removeActiveConnection(String toBeRemovedName){
        try {
            this.activeConnections.get(toBeRemovedName).close();
            this.activeConnections.remove(toBeRemovedName);
        } catch (IOException e) {
            System.out.println("RemoveActionConnection konnte Socket nicht schließen");
        }
    }

    public synchronized Collection<Socket> getAllActiveConnectionSockets(){
        return activeConnections.values();
    }

    public synchronized Set<Entry<String, Socket>> getEntrySet() {
        return activeConnections.entrySet();
    }

    /*
     * With this Method we close all Connections. By that we also indirectly stop all the receivingThreads because their socket is cut off.
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

    public void printActiveConnections(){
        for (String name : this.activeConnections.keySet()) { 
            System.out.println(name);
        }
    }

    public void sendRoutingTableToAllActiveConnections(List<RoutingTable> routingTable) {
        // Send routingtable to all active connections

    }

    public void shutdownReceiverPool(){
        this.receiverPool.shutdown();
    }
}