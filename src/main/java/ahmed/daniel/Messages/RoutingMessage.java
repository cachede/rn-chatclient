package ahmed.daniel.Messages;

import ahmed.daniel.ProtocolConstants;
import ahmed.daniel.routing.RoutingTable;

import java.io.UnsupportedEncodingException;
import java.util.List;


/**
 * Extends the Message class in order to send routingtable entries to a destination. This class is able to convert the
 * routintableentries into a byte stream and send it to a specific client in the network
 */
public class RoutingMessage extends Message {

    private final List<RoutingTable> payload;

    /**
     * Creates a RoutingMessage with the right type in the basisheader of a message. The payload for a routingmessage is
     * a list of RoutinTableEntries.
     * @param sourceName    The name of the destination, which should unwrap the value of a routingmessage
     * @param payload       A list of routingtableentries, which should be send to the direct connections
     */
    public RoutingMessage(String sourceName, List<RoutingTable> payload) {
        super(ProtocolConstants.TYPE_ROUTINGPAKET, sourceName);
        this.payload = payload;
    }

    /**
     * Converts every single RoutingTableEntry in the list into a bytestream(byte-array) for the value of the message.
     * @return  a byte-array which contains the routingtableentries converted into a bytestream
     * @throws UnsupportedEncodingException if strings in the routingtableentries could not be encoded into a utf-8 byte-array
     */
    @Override
    protected byte[] getPayloadInBytes() throws UnsupportedEncodingException {
        byte[] byteStream = new byte[payload.size() * ProtocolConstants.ROUTING_ENTRY_SIZE_IN_BYTE + ProtocolConstants.ROUTING_AMOUNT_OF_PACKETS_SIZE_IN_BYTE];
        int byteStreamIndex = 0;
        byteStream[byteStreamIndex++] = (byte)payload.size();
        for (RoutingTable routingTable : payload){
            // Destination
            byte[] destinationBytes = routingTable.getDestination().getBytes("UTF-8");
            for(int i = 0; i < ProtocolConstants.ROUTING_DESTINATION_SIZE_IN_BYTE; i++){
                byteStream[byteStreamIndex++] =  destinationBytes[i];
            }
            // Next Hop
            byte[] nextHopBytes = routingTable.getNextHop().getBytes("UTF-8");
            for(int i = 0; i < ProtocolConstants.ROUTING_NEXT_HOP_SIZE_IN_BYTE; i++) {
                byteStream[byteStreamIndex++] = nextHopBytes[i];
            }
            // Hop Count
            byteStream[byteStreamIndex++] = routingTable.getHopCount();
        }

        return byteStream;
    }
}
