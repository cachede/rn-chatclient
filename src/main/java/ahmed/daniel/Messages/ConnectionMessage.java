package ahmed.daniel.Messages;

import ahmed.daniel.ProtocolConstants;
/**
 * Extends the Message class. Is used to connect to other clients in the network.
 */
public class ConnectionMessage extends Message {


    /**
     * Creates a ConnectionMessage, which will be used to connect to a client
     *
     * @param sourceName name of client
     * @param ttl time to live
     */
    public ConnectionMessage(String sourceName, byte ttl){
        super(ProtocolConstants.TYPE_VERBINDUNGSPAKET, ttl, sourceName);
    }

    @Override
    protected byte[] getPayloadInBytes() {
        return new byte[0];
    }
}
