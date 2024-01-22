package ahmed.daniel;

import ahmed.daniel.Messages.CommunicationMessage;
import ahmed.daniel.Messages.ConnectionMessage;
import ahmed.daniel.Messages.Message;
import ahmed.daniel.Messages.ProtocolCRC32;
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

    private boolean checkSumIsCorrect(byte[] basisheader, byte[] payload, byte[] expectedChecsumCRC32Bytes) {
        byte[] tmp = new byte[4 + expectedChecsumCRC32Bytes.length];
        byte[] fillBytes = {0, 0, 0, 0};
        System.arraycopy(fillBytes, 0, tmp, 0, 4);
        System.arraycopy(expectedChecsumCRC32Bytes, 0, tmp, 4, expectedChecsumCRC32Bytes.length);
        long expectedChecksumCRC32 = ProtocolCRC32.bytesToLong(tmp);

        byte[] valuesForCRC32Calculation = new byte[basisheader.length + payload.length];
        System.arraycopy(basisheader, 0, valuesForCRC32Calculation, 0, basisheader.length);

        System.arraycopy(payload, 0, valuesForCRC32Calculation, basisheader.length, payload.length);
        long currentChecksumCRC32 = ProtocolCRC32.getCRC32Checksum(valuesForCRC32Calculation);

        return expectedChecksumCRC32 == currentChecksumCRC32;
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
                byte newTtl= (byte)(ttl-1);

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
                        // compare checksum
                        byte[] checksumBuffer = new byte[ProtocolConstants.CHECKSUM_CRC32_SIZE];
                        in.readFully(checksumBuffer);
                        byte[] payload = new byte[0];
                        if (!checkSumIsCorrect(basisheaderBuffer, payload, checksumBuffer)) {
                            System.out.println("V: Angekommene Checksumme ist nicht korrekt -> Paket verwerfen");
                            break;
                        }

                        activeConnectionManager.addActiveConnection(basisheaderSourceName, this.socket);
                        routingTableManager.addRoutingTableEntry(basisheaderSourceName, basisheaderSourceName, (byte) 1);

                        // Send our name to source if our name is not set (zzz) for them yet
                        if (newTtl>0 && basisheaderDestinationName.equals(ProtocolConstants.DESTINATION_NETWORK_NAME_NOT_SET)) {
                            Message namePackage = new ConnectionMessage(this.name, newTtl);
                            namePackage.sendTo(this.socket, basisheaderSourceName);
                        }
                    }
                    break;
                    case ProtocolConstants.TYPE_ROUTINGPAKET: {
                        byte amountOfRoutingTables = in.readByte();
                        byte[] routingpackageBuffer = new byte[amountOfRoutingTables*ProtocolConstants.ROUTING_ENTRY_SIZE_IN_BYTE];
                        in.readFully(routingpackageBuffer);
                        // compare checksum
                        byte[] routingpackagePayloadForChecksumCRC32 = new byte[ProtocolConstants.ROUTING_AMOUNT_OF_PACKETS_SIZE_IN_BYTE + routingpackageBuffer.length];
                        routingpackagePayloadForChecksumCRC32[ProtocolConstants.ROUTING_AMOUNT_OF_PACKETS_INDEX] = amountOfRoutingTables;
                        System.arraycopy(routingpackageBuffer, 0, routingpackagePayloadForChecksumCRC32, ProtocolConstants.ROUTING_AMOUNT_OF_PACKETS_INDEX+1 , routingpackageBuffer.length);
                        byte[] checkSumBuffer = new byte[ProtocolConstants.CHECKSUM_CRC32_SIZE];
                        in.readFully(checkSumBuffer);
                        if(!checkSumIsCorrect(basisheaderBuffer, routingpackagePayloadForChecksumCRC32, checkSumBuffer)){
                            System.out.println("R: Angekommene Checksumme ist nicht korrekt | ROUTINGPACKAGE");
                            break;
                        }


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
                            if (!destination.equals(this.name)){
                                // wenn wir active con schon haben -> gucken ob hop count von neuer kleiner ist -> dann ersetzen
                                byte currentHopCountForDestination = routingTableManager.getMinHopCountForDestination(destination);

                                // If we do not have a Route yet or the new Route is shorter, we add it to active Connections
                                if (hopCountBytes < currentHopCountForDestination) {
                                    Socket socket = activeConnectionManager.getSocketFromName(basisheaderSourceName);
                                    activeConnectionManager.addActiveConnection(destination, socket);
                                }
                                routingTableManager.addRoutingTableEntry(destination, basisheaderSourceName, (byte) (hopCountBytes + 1));

                                // Check after the Routingtable Update, if we still have a valid route to destination -> if not remove it from active connections
                                currentHopCountForDestination = routingTableManager.getMinHopCountForDestination(destination);

                                if (hopCountBytes == ProtocolConstants.ROUTING_MAX_HOPCOUNT && currentHopCountForDestination == ProtocolConstants.ROUTING_MAX_HOPCOUNT){
                                    activeConnectionManager.removeActiveConnection(destination);
                                }
                            }
                        }
                    }
                    break;
                    case ProtocolConstants.TYPE_MESSAGEPAKET: {
                        byte messagepackageLength = in.readByte();
                        byte[] messagepackageBuffer = new byte[messagepackageLength];
                        in.readFully(messagepackageBuffer);
                        String messageStr = new String(messagepackageBuffer, "UTF-8");
                        // compare checksum
                        byte[] messagepackagePayloadForChecksumCRC32 = new byte[ProtocolConstants.COMMUNICATION_MESSAGE_LENGTH_IN_BYTE + messagepackageBuffer.length];
                        messagepackagePayloadForChecksumCRC32[ProtocolConstants.COMMUNICATION_MESSAGE_LENGTH_INDEX] = messagepackageLength;
                        System.arraycopy(messagepackageBuffer, 0, messagepackagePayloadForChecksumCRC32, ProtocolConstants.COMMUNICATION_MESSAGE_LENGTH_INDEX+1, messagepackageBuffer.length);
                        byte[] checkSumBuffer = new byte[ProtocolConstants.CHECKSUM_CRC32_SIZE];
                        in.readFully(checkSumBuffer);
                        if(!checkSumIsCorrect(basisheaderBuffer, messagepackagePayloadForChecksumCRC32, checkSumBuffer)){
                            System.out.println("R: Angekommene Checksumme ist nicht korrekt");
                            break;
                        }

                        if (basisheaderDestinationName.equals(this.name)) {
                            System.out.println(basisheaderSourceName + ": " + messageStr);
                            //redirect message
                        } else if (newTtl>0 && activeConnectionManager.getAllActiveConnectionNames().contains(basisheaderDestinationName)) {
                            System.out.println("Hier sollten wir weiterleiten an " + basisheaderDestinationName);
                            Message passMessage = new CommunicationMessage(basisheaderSourceName, newTtl, messageStr);
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
            //TODO: IOException richtig behandeln

        } catch (NullPointerException n) {
            System.out.println("A Connection should be set before receiving a Message");

        }

    }

    @Override
    public void run() {
        receiveMessage();
    }
}