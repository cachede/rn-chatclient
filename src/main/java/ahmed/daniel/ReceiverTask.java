package ahmed.daniel;

import ahmed.daniel.Messages.CommunicationMessage;
import ahmed.daniel.Messages.ConnectionMessage;
import ahmed.daniel.Messages.Message;
import ahmed.daniel.routing.RoutingTable;
import ahmed.daniel.routing.RoutingTableManager;

import java.net.*;
import java.util.Arrays;
import java.io.*;

public class ReceiverTask implements Runnable{

    private final Socket socket;
    private final String name;
    private final ActiveConnectionManager activeConnectionManager;
    private final RoutingTableManager routingTableManager;

    public ReceiverTask(Socket socket, String name, ActiveConnectionManager activeConnections, RoutingTableManager routingTableManager){
        this.socket = socket;
        this.name = name;
        this.activeConnectionManager = activeConnections;
        this.routingTableManager = routingTableManager;
        if (this.socket == null) {
            System.out.println("DER IGEL IST HEUTE SEHR STACHELIG");
        }
    }
    
    /**
     * Pretty important Method. It receives (Pakets|Datagram|Messages) from the direct Connection.
     * There are 3 different Types of Messages to be received.
     * Type 0: Verbindungspaket. This is the first Message which will be received after the initial Connection.
     * Type 1: Routingpaket. This Message contains no Message but a Routingtable from its direct Connections.
     * This is useful, in order to know which Participants are connected and how to get to them. (Teilvermascht)
     * This Message will be parsed and is the Content of the private Membervariable routingTable.
     * Type 2: Nachrichtenpaket: This Message contains the actual Message.
     */
    private void receiveMessage() {
        try {
            //BufferedReader in = new BufferedReader(new InputStreamReader(this.socket.getInputStream(), "UTF-8")); //Wenn es noch keine Verbindung gibt Big Problem
            DataInputStream in = new DataInputStream(socket.getInputStream());

            byte[] buffer = new byte[ProtocolConstants.MAX_MESSAGE_LENGTH_IN_BYTES];
            while(true) {
                in.readFully(buffer);

                //System.out.println("Buffer-Bytes: " + buffer);
                //String res = new String(buffer, "UTF-8");
                //System.out.println("Buffer-String: " + res);

                //TODO Extract header before switch case

                switch(buffer[0]) {
                    case ProtocolConstants.TYPE_VERBINDUNGSPAKET: {

                        byte TYPE = buffer[ProtocolConstants.TYPE_INDEX];
                        byte TTL = buffer[ProtocolConstants.TTL_INDEX];

                        byte[] destinationNameAsBytes = new byte[ProtocolConstants.DESTINATION_NETWORK_NAME_SIZE_IN_BYTE];
                        for(int i = ProtocolConstants.DESTINATION_NETWORK_NAME_LOWER, j = 0; i < ProtocolConstants.DESTINATION_NETWORK_NAME_HIGHER; i++, j++) {
                            destinationNameAsBytes[j] = buffer[i];
                        }

                        String destinationName = new String(destinationNameAsBytes, "UTF-8");

                        byte[] sourceNameAsBytes = new byte[ProtocolConstants.SOURCE_NETWORK_NAME_SIZE_IN_BYTE];
                        for(int i = ProtocolConstants.SOURCE_NETWORK_NAME_LOWER, j = 0; i < ProtocolConstants.SOURCE_NETWORK_NAME_HIGHER; i++, j++) {
                            sourceNameAsBytes[j] = buffer[i];
                        }

                        String sourceName = new String(sourceNameAsBytes, "UTF-8") ;

                        activeConnectionManager.addActiveConnection(sourceName, this.socket);
                        routingTableManager.addRoutingTableEntry(sourceName, sourceName, (byte)1);

// DAN -> AHM dest= ZZZ
                        // Send our name to source if our name is not set for them yet
                        if (destinationName.equals(ProtocolConstants.DESTINATION_NETWORK_NAME_NOT_SET)){
                            Message namePackage  = new ConnectionMessage(this.name);
                            namePackage.sendTo(this.socket, sourceName);
                        }
                    }
                    break;
                    case ProtocolConstants.TYPE_ROUTINGPAKET: {
                        int amountOfRoutingTables = buffer[8];
                        byte[] routingMessage = Arrays.copyOfRange(buffer, 9, buffer.length);
                                                
                        for(int i = 0; i < amountOfRoutingTables; i++) {
                            int offset = i * ProtocolConstants.ROUTING_ENTRY_SIZE_IN_BYTE;

                            // Destination
                            int startIndex = offset;
                            int stopIndex = startIndex + ProtocolConstants.ROUTING_DESTINATION_SIZE_IN_BYTE;
                            byte[] destinationBytes = Arrays.copyOfRange(routingMessage, offset, stopIndex);
                            String destination = new String(destinationBytes, "UTF-8");

                            // Next Hop
                            startIndex = stopIndex;
                            stopIndex = startIndex + ProtocolConstants.ROUTING_NEXT_HOP_SIZE_IN_BYTE;
                            byte[] nextHopBytes = Arrays.copyOfRange(routingMessage, startIndex, stopIndex);
                            String nextHop = new String(nextHopBytes, "UTF-8");

                            // HopCount
                            startIndex = stopIndex;
                            byte hopCountBytes = routingMessage[startIndex];
                            
                            // For Debugging:
                            RoutingTable routingTable = new RoutingTable(destination, nextHop, hopCountBytes);
                            //System.out.println("In Recv erstellter Table:");
                            //routingTable.printRoutingTable();

                            //TODO Add active trasitive connections
                            byte[] sourceNameAsBytes = new byte[ProtocolConstants.SOURCE_NETWORK_NAME_SIZE_IN_BYTE];
                            for(int k = ProtocolConstants.SOURCE_NETWORK_NAME_LOWER, j = 0; k < ProtocolConstants.SOURCE_NETWORK_NAME_HIGHER; k++, j++) {
                                sourceNameAsBytes[j] = buffer[k];
                            }

                            String sourceName = new String(sourceNameAsBytes, "UTF-8") ;
                            // TODO Check if we have next hop in routingtable -> Can we check if we have it in active connections?
                            if(!destination.equals(this.name) && !nextHop.equals(this.name)){
                                // wenn wir active con schon haben -> gucken ob hop count von neuer kleiner ist -> dann ersetzen
                                byte currentHopCountForDestination = routingTableManager.getMinHopCountForDestination(destination);

                                // If we do not have a Route yet or the new Route is shorter, we add it
                                if(currentHopCountForDestination == ProtocolConstants.ROUTING_MAX_HOPCOUNT || hopCountBytes < currentHopCountForDestination){
                                    Socket socket = activeConnectionManager.getSocketFromName(sourceName);
                                    activeConnectionManager.addActiveConnection(destination, socket);
                                }

                                routingTableManager.addRoutingTableEntry(destination, sourceName, (byte)(hopCountBytes+1) );
                            }

                        }                    
                    }
                    break;
                    case ProtocolConstants.TYPE_MESSAGEPAKET: {
                        //Hier Annahme: Jede Nachricht die ankommt ist auch an uns gedacht
                        //Richtig wäre: Jede Nachricht die ankommt MUSS gecheckt werden, ob sie an uns gerichtet war.
                        // ----------> WENN JA: Print auf STDOUT
                        //-----------> WENN NEIN: Leite weiter an richtigen Socket

                        // TODO Refactor

                        byte[] sourceNameAsBytes = new byte[ProtocolConstants.SOURCE_NETWORK_NAME_SIZE_IN_BYTE];
                        for(int i = ProtocolConstants.SOURCE_NETWORK_NAME_LOWER, j = 0; i < ProtocolConstants.SOURCE_NETWORK_NAME_HIGHER; i++, j++) {
                            sourceNameAsBytes[j] = buffer[i];
                        }

                        String sourceName = new String(sourceNameAsBytes, "UTF-8");

                        byte[] message = Arrays.copyOfRange(buffer, 8, buffer.length);
                        String messageStr = new String(message, "UTF-8");

                        byte[] destinationNameAsBytes = new byte[ProtocolConstants.DESTINATION_NETWORK_NAME_SIZE_IN_BYTE];
                        for(int i = ProtocolConstants.DESTINATION_NETWORK_NAME_LOWER, j = 0; i < ProtocolConstants.DESTINATION_NETWORK_NAME_HIGHER; i++, j++) {
                            destinationNameAsBytes[j] = buffer[i];
                        }

                        String destinationName = new String(destinationNameAsBytes, "UTF-8");


                        if(destinationName.equals(this.name)){
                            System.out.println(sourceName + ": " + messageStr);
                        }
                        else if (activeConnectionManager.getAllActiveConnectionName().contains(destinationName)){
                            System.out.println("Hier sollten wir weiterleiten an " + destinationName);
                            Message passMessage = new CommunicationMessage(sourceName, messageStr);
                            Socket passSocket = activeConnectionManager.getSocketFromName(destinationName);
                            passMessage.sendTo(passSocket, destinationName);
                        }

                    }
                    break;
                    default: 
                        System.out.println("Keine Ahnung was das fuer ein Paket"); break;
                }

        }

            } catch (IOException e) {
                //System.out.println("IO-EXCEPTION WHILE RECEIVING");
                
            } catch(NullPointerException n) {
                System.out.println("A Connection should be set before receiving a Message");
                
            }
        
    }

    @Override
    public void run(){
        receiveMessage();
    }
}