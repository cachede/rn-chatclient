package ahmed.daniel.messages;

import ahmed.daniel.ProtocolConstants;
import ahmed.daniel.routing.RoutingTableEntry;
import java.nio.charset.StandardCharsets;
import java.util.List;


/**
 * Extends the Message class in order to send routingtable entries to a destination. This class is able to convert the
 * routintableentries into a byte stream and send it to a specific client in the network
 */
public class RoutingMessage extends Message {

    private final List<RoutingTableEntry> payload;

    /**
     * Creates a RoutingMessage with the right type in the basisheader of a message. The payload for a routingmessage is
     * a list of RoutinTableEntries.
     *
     * @param sourceName    The name of the destination, which should unwrap the value of a routingmessage
     * @param payload       A list of routingtableentries, which should be send to the direct connections
     */
    public RoutingMessage(String sourceName, List<RoutingTableEntry> payload) {
        super(ProtocolConstants.TYPE_ROUTINGPAKET, ProtocolConstants.TTL, sourceName);
        this.payload = payload;
    }

    /**
     * Converts every single RoutingTableEntry in the list into a bytestream(byte-array) for the value of the message.
     * @return  a byte-array which contains the routingtableentries converted into a bytestream
     */
    @Override
    protected byte[] getPayloadInBytes() {
        byte[] byteStream = new byte[payload.size() * ProtocolConstants.ROUTING_ENTRY_SIZE_IN_BYTE + ProtocolConstants.ROUTING_AMOUNT_OF_PACKETS_SIZE_IN_BYTE];
        int byteStreamIndex = 0;
        byteStream[byteStreamIndex++] = (byte)payload.size();
        for (RoutingTableEntry routingTable : payload){
            // Destination
            byte[] destinationBytes = routingTable.getDestination().getBytes(StandardCharsets.UTF_8);
            for(int i = 0; i < ProtocolConstants.ROUTING_DESTINATION_SIZE_IN_BYTE; i++){
                byteStream[byteStreamIndex++] =  destinationBytes[i];
            }
            // Next Hop
            byte[] nextHopBytes = routingTable.getNextHop().getBytes(StandardCharsets.UTF_8);
            for(int i = 0; i < ProtocolConstants.ROUTING_NEXT_HOP_SIZE_IN_BYTE; i++) {
                byteStream[byteStreamIndex++] = nextHopBytes[i];
            }
            // Hop Count
            byteStream[byteStreamIndex++] = routingTable.getHopCount();
        }

        return byteStream;
    }
}
