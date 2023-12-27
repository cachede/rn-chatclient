package ahmed.daniel.Messages;
import ahmed.daniel.Messages.Message;
import ahmed.daniel.ProtocolConstants;

import java.io.UnsupportedEncodingException;
import java.net.Socket;
public class ConnectionMessage extends Message {

    public ConnectionMessage(Socket socket, String sourceName){
        super(socket, ProtocolConstants.TYPE_VERBINDUNGSPAKET, sourceName);
    }

    @Override
    protected byte[] getPayloadInBytes() throws UnsupportedEncodingException {
        return new byte[0];
    }
}
