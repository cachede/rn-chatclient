package ahmed.daniel.Messages;

import ahmed.daniel.Messages.Message;

import java.io.UnsupportedEncodingException;
import java.net.Socket;

//TODO whole class is not finished
public class RoutingMessage extends Message {
    public RoutingMessage(Socket socket, byte type, String sourceName) {
        super(socket, type, sourceName);
    }


    @Override
    protected byte[] getPayloadInBytes() throws UnsupportedEncodingException {
        return new byte[0];
    }
}
