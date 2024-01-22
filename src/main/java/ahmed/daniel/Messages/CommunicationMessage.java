package ahmed.daniel.Messages;

import ahmed.daniel.Messages.Message;
import ahmed.daniel.ProtocolConstants;

import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Extends the Message class in order to convert a plain-text (String) into a bytestream(bytearray)
 */
public class CommunicationMessage extends Message {

    private String payload;

    /**
     *
     * @param sourceName
     * @param ttl
     * @param payload
     */
    public CommunicationMessage(String sourceName, byte ttl, String payload) {
        super(ProtocolConstants.TYPE_MESSAGEPAKET, ttl, sourceName);
        this.payload = payload;
    }


    @Override
    protected byte[] getPayloadInBytes() {
        byte[] payloadBytes = this.payload.getBytes(StandardCharsets.UTF_8);
        int payloadSize = payloadBytes.length;

        byte[] communicationMessage = new byte[ProtocolConstants.COMMUNICATION_MESSAGE_LENGTH_IN_BYTE + payloadSize];
        communicationMessage[ProtocolConstants.COMMUNICATION_MESSAGE_LENGTH_INDEX] = (byte) payloadSize;
        System.arraycopy(payloadBytes, 0, communicationMessage, ProtocolConstants.COMMUNICATION_MESSAGE_LENGTH_INDEX+1, payloadSize);

        return communicationMessage;
    }
}
