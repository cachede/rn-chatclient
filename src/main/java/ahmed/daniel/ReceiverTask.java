package ahmed.daniel;

import ahmed.daniel.Messages.CommunicationMessage;
import ahmed.daniel.Messages.ConnectionMessage;
import ahmed.daniel.Messages.Message;
import ahmed.daniel.routing.RoutingTable;
import ahmed.daniel.routing.RoutingTableManager;

import java.net.*;
import java.util.Arrays;
import java.io.*;

public class ReceiverTask implements Runnable {

    private final Socket socket;
    private final String name;
    private final ActiveConnectionManager activeConnectionManager;
    private final RoutingTableManager routingTableManager;

    public ReceiverTask(Socket socket, String name, ActiveConnectionManager activeConnections, RoutingTableManager routingTableManager) {
        this.socket = socket;
        this.name = name;
        this.activeConnectionManager = activeConnections;
        this.routingTableManager = routingTableManager;
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
            DataInputStream in = new DataInputStream(socket.getInputStream());
            byte[] basisheaderBuffer = new byte[ProtocolConstants.BASISHEADER_SIZE_IN_BYTE];

            while (true) {
                // Read the Basisheader
                in.readFully(basisheaderBuffer);

                byte type = basisheaderBuffer[ProtocolConstants.TYPE_INDEX];
                byte ttl = basisheaderBuffer[ProtocolConstants.TTL_INDEX];

                byte[] destinationNameAsBytes = new byte[ProtocolConstants.DESTINATION_NETWORK_NAME_SIZE_IN_BYTE];
                for (int basisheaderIndex = ProtocolConstants.DESTINATION_NETWORK_NAME_LOWER, destinationNameIndex = 0;
                     basisheaderIndex < ProtocolConstants.DESTINATION_NETWORK_NAME_HIGHER;
                     basisheaderIndex++, destinationNameIndex++) {

                    destinationNameAsBytes[destinationNameIndex] = basisheaderBuffer[basisheaderIndex];
                }
                String basisheaderDestinationName = new String(destinationNameAsBytes, "UTF-8");

                byte[] sourceNameAsBytes = new byte[ProtocolConstants.SOURCE_NETWORK_NAME_SIZE_IN_BYTE];
                for (int basisheaderIndex = ProtocolConstants.SOURCE_NETWORK_NAME_LOWER, sourceNameIndex = 0;
                     basisheaderIndex < ProtocolConstants.SOURCE_NETWORK_NAME_HIGHER;
                     basisheaderIndex++, sourceNameIndex++) {

                    sourceNameAsBytes[sourceNameIndex] = basisheaderBuffer[basisheaderIndex];
                }
                String basisheaderSourceName = new String(sourceNameAsBytes, "UTF-8");


                switch (type) {
                    case ProtocolConstants.TYPE_VERBINDUNGSPAKET: {
                        activeConnectionManager.addActiveConnection(basisheaderSourceName, this.socket);
                        routingTableManager.addRoutingTableEntry(basisheaderSourceName, basisheaderSourceName, (byte) 1);

                        // Send our name to source if our name is not set (ZZZ) for them yet
                        if (basisheaderDestinationName.equals(ProtocolConstants.DESTINATION_NETWORK_NAME_NOT_SET)) {
                            Message namePackage = new ConnectionMessage(this.name);
                            namePackage.sendTo(this.socket, basisheaderSourceName);
                        }
                    }
                    break;
                    case ProtocolConstants.TYPE_ROUTINGPAKET: {
                        byte amountOfRoutingTables = in.readByte();
                        byte[] routingpackageBuffer = new byte[amountOfRoutingTables*ProtocolConstants.ROUTING_ENTRY_SIZE_IN_BYTE];
                        in.readFully(routingpackageBuffer);


                        for (int i = 0; i < amountOfRoutingTables; i++) {
                            int offset = i * ProtocolConstants.ROUTING_ENTRY_SIZE_IN_BYTE;

                            // Destination
                            int startIndex = offset;
                            int stopIndex = startIndex + ProtocolConstants.ROUTING_DESTINATION_SIZE_IN_BYTE;
                            byte[] routingEntrydestinationBytes = Arrays.copyOfRange(routingpackageBuffer, offset, stopIndex);
                            String destination = new String(routingEntrydestinationBytes, "UTF-8");

                            // Next Hop
                            startIndex = stopIndex;
                            stopIndex = startIndex + ProtocolConstants.ROUTING_NEXT_HOP_SIZE_IN_BYTE;
                            byte[] routingEntrynextHopBytes = Arrays.copyOfRange(routingpackageBuffer, startIndex, stopIndex);
                            String nextHop = new String(routingEntrynextHopBytes, "UTF-8");

                            // HopCount
                            startIndex = stopIndex;
                            byte hopCountBytes = routingpackageBuffer[startIndex];

                            //TODO Add active trasitive connections
                            if (!destination.equals(this.name) && !nextHop.equals(this.name)) {
                                // wenn wir active con schon haben -> gucken ob hop count von neuer kleiner ist -> dann ersetzen
                                byte currentHopCountForDestination = routingTableManager.getMinHopCountForDestination(destination);

                                // If we do not have a Route yet or the new Route is shorter, we add it to acctive Connections
                                if (hopCountBytes < currentHopCountForDestination) {
                                    Socket socket = activeConnectionManager.getSocketFromName(basisheaderSourceName);
                                    activeConnectionManager.addActiveConnection(destination, socket);
                                }

                                routingTableManager.addRoutingTableEntry(destination, basisheaderSourceName, (byte) (hopCountBytes + 1));
                            }
                        }
                    }
                    break;
                    case ProtocolConstants.TYPE_MESSAGEPAKET: {
                        byte messagepackageLength = in.readByte();
                        byte[] messagepackageBuffer = new byte[messagepackageLength];
                        in.readFully(messagepackageBuffer);
                        String messageStr = new String(messagepackageBuffer, "UTF-8");

                        if (basisheaderDestinationName.equals(this.name)) {
                            System.out.println(basisheaderSourceName + ": " + messageStr);
                        } else if (activeConnectionManager.getAllActiveConnectionName().contains(basisheaderDestinationName)) {
                            System.out.println("Hier sollten wir weiterleiten an " + basisheaderDestinationName);
                            Message passMessage = new CommunicationMessage(basisheaderSourceName, messageStr);
                            Socket passSocket = activeConnectionManager.getSocketFromName(basisheaderDestinationName);
                            passMessage.sendTo(passSocket, basisheaderDestinationName);
                        }
                    }
                    break;
                    default:
                        System.out.println("Keine Ahnung was das fuer ein Paket");
                        break;
                }

            }

        } catch (IOException e) {
            //System.out.println("IO-EXCEPTION WHILE RECEIVING");

        } catch (NullPointerException n) {
            System.out.println("A Connection should be set before receiving a Message");

        }

    }

    @Override
    public void run() {
        receiveMessage();
    }
}