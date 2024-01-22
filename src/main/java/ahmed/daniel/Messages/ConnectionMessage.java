package ahmed.daniel.Messages;

import ahmed.daniel.ProtocolConstants;
/**
 *
 */
public class ConnectionMessage extends Message {

    public ConnectionMessage(String sourceName, byte ttl){
        super(ProtocolConstants.TYPE_VERBINDUNGSPAKET, ttl, sourceName);
    }

    @Override
    protected byte[] getPayloadInBytes() {
        return new byte[0];
    }
}
