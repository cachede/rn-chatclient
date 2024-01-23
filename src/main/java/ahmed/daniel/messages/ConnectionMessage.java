package ahmed.daniel.messages;

import ahmed.daniel.ProtocolConstants;
/**
 * Extends the Message class. Is used to connect to other clients in the network.
 */
public class ConnectionMessage extends Message {


    /**
     * Creates a ConnectionMessage, which will be used to connect to a client
     *
     * @param sourceName name of client
     */
    public ConnectionMessage(String sourceName){
        super(ProtocolConstants.TYPE_VERBINDUNGSPAKET, ProtocolConstants.TTL, sourceName);
    }

    @Override
    protected byte[] getPayloadInBytes() {
        return new byte[0];
    }
}
