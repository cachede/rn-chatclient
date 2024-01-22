package ahmed.daniel;

import ahmed.daniel.Messages.CommunicationMessage;
import ahmed.daniel.Messages.ConnectionMessage;
import ahmed.daniel.Messages.Message;
import ahmed.daniel.Messages.ProtocolCRC32;
import ahmed.daniel.routing.RoutingTableEntry;
import ahmed.daniel.routing.RoutingTableManager;

import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.*;
import java.util.List;

public class ReceiverTask implements Runnable {

    private final Socket socket;
    private final String name;
    private final ActiveConnectionManager activeConnectionManager;
    private final RoutingTableManager routingTableManager;

    // Basisheader
    private byte basisheaderType;
    private byte basisheaderNewTtl;
    private String basisheaderDestinationName;
    private String basisheaderSourceName;


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
            DataInputStream in = new DataInputStream(this.socket.getInputStream());
            byte[] basisheaderBuffer = new byte[ProtocolConstants.BASISHEADER_SIZE_IN_BYTE];
            while (!Thread.currentThread().isInterrupted()) {
                // Read the Basisheader
                in.readFully(basisheaderBuffer);
                extractBasisheader(basisheaderBuffer);
                switch (this.basisheaderType) {
                    case ProtocolConstants.TYPE_VERBINDUNGSPAKET: {
                        // compare checksum
                        byte[] checksumBuffer = new byte[ProtocolConstants.CHECKSUM_CRC32_SIZE];
                        in.readFully(checksumBuffer);
                        byte[] payload = new byte[0];
                        if (ProtocolCRC32.checkSumIsInvalid(basisheaderBuffer, payload, checksumBuffer)) {
                            System.out.println("V: Angekommene Checksumme ist nicht korrekt -> Paket verwerfen");
                            break;
                        }

                        handleVerbindungspaket();
                    }
                    break;
                    case ProtocolConstants.TYPE_ROUTINGPAKET: {
                        byte amountOfRoutingTableEntries = in.readByte();
                        byte[] routingpackageBuffer = new byte[amountOfRoutingTableEntries * ProtocolConstants.ROUTING_ENTRY_SIZE_IN_BYTE];
                        in.readFully(routingpackageBuffer);
                        List<RoutingTableEntry> routingTable = new ArrayList<>();
                        extractRoutingTables(routingpackageBuffer, amountOfRoutingTableEntries, routingTable);

                        // compare checksum
                        byte[] routingpackagePayloadForChecksumCRC32 = new byte[ProtocolConstants.ROUTING_AMOUNT_OF_PACKETS_SIZE_IN_BYTE + routingpackageBuffer.length];
                        routingpackagePayloadForChecksumCRC32[ProtocolConstants.ROUTING_AMOUNT_OF_PACKETS_INDEX] = amountOfRoutingTableEntries;
                        System.arraycopy(routingpackageBuffer, 0, routingpackagePayloadForChecksumCRC32, ProtocolConstants.ROUTING_AMOUNT_OF_PACKETS_INDEX + 1, routingpackageBuffer.length);
                        byte[] checkSumBuffer = new byte[ProtocolConstants.CHECKSUM_CRC32_SIZE];
                        in.readFully(checkSumBuffer);
                        if (ProtocolCRC32.checkSumIsInvalid(basisheaderBuffer, routingpackagePayloadForChecksumCRC32, checkSumBuffer)) {
                            System.out.println("Angekommene Checksumme ist nicht korrekt | ROUTINGPACKAGE");
                            break;
                        }

                        handleRoutingpaket(routingTable);
                    }
                    break;
                    case ProtocolConstants.TYPE_MESSAGEPAKET: {
                        byte messagepackageLength = in.readByte();
                        byte[] messagepackageBuffer = new byte[messagepackageLength];
                        in.readFully(messagepackageBuffer);
                        String messageStr = new String(messagepackageBuffer, StandardCharsets.UTF_8);
                        // compare checksum
                        byte[] messagepackagePayloadForChecksumCRC32 = new byte[ProtocolConstants.COMMUNICATION_MESSAGE_LENGTH_IN_BYTE + messagepackageBuffer.length];
                        messagepackagePayloadForChecksumCRC32[ProtocolConstants.COMMUNICATION_MESSAGE_LENGTH_INDEX] = messagepackageLength;
                        System.arraycopy(messagepackageBuffer, 0, messagepackagePayloadForChecksumCRC32, ProtocolConstants.COMMUNICATION_MESSAGE_LENGTH_INDEX + 1, messagepackageBuffer.length);
                        byte[] checkSumBuffer = new byte[ProtocolConstants.CHECKSUM_CRC32_SIZE];
                        in.readFully(checkSumBuffer);
                        if (ProtocolCRC32.checkSumIsInvalid(basisheaderBuffer, messagepackagePayloadForChecksumCRC32, checkSumBuffer)) {
                            System.out.println("R: Angekommene Checksumme ist nicht korrekt");
                            break;
                        }

                        handleMessagepaket(messageStr);
                    }
                    break;
                    default:
                        System.out.println("Keine Ahnung was das fuer ein Paket");
                        break;
                }

            }

        } catch (IOException e) {
            //TODO: IOException richtig behandeln
        }

    }

    @Override
    public void run() {
        receiveMessage();
    }

    private void extractBasisheader(byte[] basisheaderBuffer) {
        this.basisheaderType = basisheaderBuffer[ProtocolConstants.TYPE_INDEX];
        byte basisheaderTtl = basisheaderBuffer[ProtocolConstants.TTL_INDEX];
        this.basisheaderNewTtl = (byte) (basisheaderTtl - 1);

        byte[] destinationNameAsBytes = new byte[ProtocolConstants.DESTINATION_NETWORK_NAME_SIZE_IN_BYTE];
        for (int basisheaderIndex = ProtocolConstants.DESTINATION_NETWORK_NAME_LOWER, destinationNameIndex = 0;
             basisheaderIndex < ProtocolConstants.DESTINATION_NETWORK_NAME_HIGHER;
             basisheaderIndex++, destinationNameIndex++) {

            destinationNameAsBytes[destinationNameIndex] = basisheaderBuffer[basisheaderIndex];
        }
        this.basisheaderDestinationName = new String(destinationNameAsBytes, StandardCharsets.UTF_8);

        byte[] sourceNameAsBytes = new byte[ProtocolConstants.SOURCE_NETWORK_NAME_SIZE_IN_BYTE];
        for (int basisheaderIndex = ProtocolConstants.SOURCE_NETWORK_NAME_LOWER, sourceNameIndex = 0;
             basisheaderIndex < ProtocolConstants.SOURCE_NETWORK_NAME_HIGHER;
             basisheaderIndex++, sourceNameIndex++) {

            sourceNameAsBytes[sourceNameIndex] = basisheaderBuffer[basisheaderIndex];
        }
        this.basisheaderSourceName = new String(sourceNameAsBytes, StandardCharsets.UTF_8);
    }

    private void extractRoutingTables(byte[] routingpackageBuffer, byte amountOfRoutingTableEntries, List<RoutingTableEntry> routingTable) {
        for (int i = 0; i < amountOfRoutingTableEntries; i++) {
            int offset = i * ProtocolConstants.ROUTING_ENTRY_SIZE_IN_BYTE;

            // Destination
            int startIndex = offset;
            int stopIndex = startIndex + ProtocolConstants.ROUTING_DESTINATION_SIZE_IN_BYTE;
            byte[] routingEntrydestinationBytes = Arrays.copyOfRange(routingpackageBuffer, offset, stopIndex);
            String destination = new String(routingEntrydestinationBytes, StandardCharsets.UTF_8);

            // Next Hop
            startIndex = stopIndex;
            stopIndex = startIndex + ProtocolConstants.ROUTING_NEXT_HOP_SIZE_IN_BYTE;
            byte[] routingEntrynextHopBytes = Arrays.copyOfRange(routingpackageBuffer, startIndex, stopIndex);
            String nextHop = new String(routingEntrynextHopBytes, StandardCharsets.UTF_8);

            // HopCount
            startIndex = stopIndex;
            byte hopCountBytes = routingpackageBuffer[startIndex];

            routingTable.add(new RoutingTableEntry(destination, nextHop, hopCountBytes));
        }
    }

    private void handleVerbindungspaket() throws IOException{
        this.activeConnectionManager.addActiveConnection(this.basisheaderSourceName, this.socket);
        this.routingTableManager.addRoutingTableEntry(this.basisheaderSourceName, this.basisheaderSourceName, (byte) 1);

        // Send our name to source if our name is not set (zzz) for them yet
        if (this.basisheaderNewTtl > 0 && this.basisheaderDestinationName.equals(ProtocolConstants.DESTINATION_NETWORK_NAME_NOT_SET)) {
            Message namePackage = new ConnectionMessage(this.name, this.basisheaderNewTtl);
            namePackage.sendTo(this.socket, this.basisheaderSourceName);
        }
    }

    private void handleRoutingpaket(List<RoutingTableEntry> routingTable){
        for (RoutingTableEntry routingTableEntry : routingTable) {
            if (!routingTableEntry.getDestination().equals(this.name)) {
                // wenn wir active con schon haben -> gucken ob hop count von neuer kleiner ist -> dann ersetzen
                byte currentHopCountForDestination = routingTableManager.getMinHopCountForDestination(routingTableEntry.getDestination());

                // If we do not have a Route yet or the new Route is shorter, we add it to active Connections
                if (routingTableEntry.getHopCount() < currentHopCountForDestination) {
                    Socket socket = activeConnectionManager.getSocketFromName(basisheaderSourceName);
                    activeConnectionManager.addActiveConnection(routingTableEntry.getDestination(), socket);
                }
                routingTableManager.addRoutingTableEntry(routingTableEntry.getDestination(), basisheaderSourceName, (byte) (routingTableEntry.getHopCount() + 1));

                // Check after the Routingtable Update, if we still have a valid route to destination -> if not remove it from active connections
                currentHopCountForDestination = routingTableManager.getMinHopCountForDestination(routingTableEntry.getDestination());

                if (routingTableEntry.getHopCount() == ProtocolConstants.ROUTING_MAX_HOPCOUNT && currentHopCountForDestination == ProtocolConstants.ROUTING_MAX_HOPCOUNT) {
                    activeConnectionManager.removeActiveConnection(routingTableEntry.getDestination());
                }
            }
        }
    }

    private void handleMessagepaket(String messageStr) throws IOException {
        if (this.basisheaderDestinationName.equals(this.name)) {
            System.out.println("\n" + this.basisheaderSourceName + ": " + messageStr);
            //redirect message
        } else if (this.basisheaderNewTtl > 0 && this.activeConnectionManager.getAllActiveConnectionNames().contains(this.basisheaderDestinationName)) {
            System.out.println("Hier sollten wir weiterleiten an " + this.basisheaderDestinationName);
            Message passMessage = new CommunicationMessage(this.basisheaderSourceName, this.basisheaderNewTtl, messageStr);
            Socket passSocket = this.activeConnectionManager.getSocketFromName(this.basisheaderDestinationName);
            passMessage.sendTo(passSocket, this.basisheaderDestinationName);
        }
    }
}

