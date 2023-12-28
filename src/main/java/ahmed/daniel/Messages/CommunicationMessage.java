package ahmed.daniel.Messages;

import ahmed.daniel.Messages.Message;
import ahmed.daniel.ProtocolConstants;

import java.io.UnsupportedEncodingException;
import java.net.Socket;

public class CommunicationMessage extends Message {

    private String payload;

    public CommunicationMessage(String sourceName, String payload) {
        super(ProtocolConstants.TYPE_MESSAGEPAKET, sourceName);
        this.payload = payload;
    }


    @Override
    protected byte[] getPayloadInBytes() throws UnsupportedEncodingException {
        return this.payload.getBytes("UTF-8");
    }
}
