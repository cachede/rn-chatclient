package ahmed.daniel.Messages;

import ahmed.daniel.Messages.Message;
import ahmed.daniel.ProtocolConstants;

import java.io.UnsupportedEncodingException;
import java.net.Socket;

/**
 * Extends the Message class in order to convert a plain-text (String) into a bytestream(bytearray)
 */
public class CommunicationMessage extends Message {

    private String payload;

    /**
     *
     * @param sourceName
     * @param payload
     */
    public CommunicationMessage(String sourceName, String payload) {
        super(ProtocolConstants.TYPE_MESSAGEPAKET, sourceName);
        this.payload = payload;
    }


    @Override
    protected byte[] getPayloadInBytes() throws UnsupportedEncodingException {
        return this.payload.getBytes("UTF-8");
    }
}
