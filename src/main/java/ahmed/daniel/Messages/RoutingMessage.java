package ahmed.daniel.Messages;

import ahmed.daniel.Messages.Message;
import ahmed.daniel.ProtocolConstants;
import ahmed.daniel.routing.RoutingTable;
import ahmed.daniel.routing.RoutingTableManager;

import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Arrays;


//TODO whole class is not finished
public class RoutingMessage extends Message {

    private final List<RoutingTable> payload;
    public RoutingMessage(String sourceName, List<RoutingTable> payload) {
        super(ProtocolConstants.TYPE_ROUTINGPAKET, sourceName);
        this.payload = payload;
    }

    @Override
    protected byte[] getPayloadInBytes() throws UnsupportedEncodingException {

        byte[] byteStream = new byte[payload.size() * ProtocolConstants.ROUTING_ENTRY_SIZE_IN_BYTE + ProtocolConstants.ROUTING_ENTRY_SIZE_IN_BYTE];
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
