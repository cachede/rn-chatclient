package ahmed.daniel.messages;

import ahmed.daniel.ProtocolConstants;
import java.nio.charset.StandardCharsets;

/**
 * Extends the Message class in order to convert a plain-text (String) into a bytestream(bytearray)
 */
public class CommunicationMessage extends Message {

    private final String payload;

    /**
     * Creates a Communication-Message with a specific type and source name
     *
     * @param sourceName name of client
     * @param ttl time to live
     * @param payload actual message to be send
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
