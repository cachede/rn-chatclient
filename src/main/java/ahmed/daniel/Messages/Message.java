package ahmed.daniel.Messages;

import ahmed.daniel.ProtocolConstants;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * A message which is being send to Participants in the network contains a Header which is implemented in this abstract
 * class. The header has always the same structure (see protocol). Building the header is always the same process, just
 * differs in the parameters.
 */
public abstract class Message {
    private final byte type;
    private final byte ttl;
    private final String sourceName;

    /**
     * Creates a Message with a specific type and source name. The type differentiates between messages/connections/routing
     *
     * @param type          used to identify what the message contains(text/connection/routing-entries
     * @param ttl           The time to live which exists for every Message
     * @param sourceName    The name, from which this message comes from
     */
    public Message(byte type, byte ttl, String sourceName) {
        this.type = type;
        this.ttl = ttl;
        this.sourceName = sourceName;
    }

    /**
     * This method is called to send a Message to a destination name. First it build the protocolheader and fills it
     * with the destination name and source name. After the header is build the method sends the byte-stream to the
     * given socket
     *
     * @param socket    where the message should be send to
     * @param destinationName   the name of the destination which should unwrap the message
     * @throws IOException  the destination could disconnect in the process of the message building.
     */
    public void sendTo(Socket socket, String destinationName) throws IOException{

        byte[] message = buildMessage(destinationName);
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        out.write(message);

    }

    /**
     * The value in a Message differs, in how it is encoded. Every class that extends the Message class should provide
     * a implementation of this function, for whatever the message should contain as a value.
     *
     * @return  a byte-array which contains the value encoded in bytes
     */
    protected abstract byte[] getPayloadInBytes();

    private byte[] buildMessage(String destinationName) {
        byte[] payload = getPayloadInBytes();
        byte[] basisHeader = getBasisHeader(destinationName);

        // Build message with header and paylaod
        byte[] message = new byte[basisHeader.length + payload.length];
        System.arraycopy(basisHeader, 0, message, 0, basisHeader.length);
        System.arraycopy(payload, 0, message, basisHeader.length, payload.length);

        byte[] messageWithChecksumCRC32 = new byte[message.length + ProtocolConstants.CHECKSUM_CRC32_SIZE];
        long checksumCRC32 = ProtocolCRC32.getCRC32Checksum(message);
        byte[] checksumCRC32Bytes =  ProtocolCRC32.getChecksumBytes(checksumCRC32);
        System.arraycopy(message, 0, messageWithChecksumCRC32, 0, message.length);
        System.arraycopy(checksumCRC32Bytes, 0, messageWithChecksumCRC32, message.length, ProtocolConstants.CHECKSUM_CRC32_SIZE);
        return messageWithChecksumCRC32;
    }

    private byte[] getBasisHeader(String destinationName) {
        byte[] basisHeader = new byte[ProtocolConstants.BASISHEADER_SIZE_IN_BYTE];
        // Type and TTL
        basisHeader[ProtocolConstants.TYPE_INDEX] = this.type;
        basisHeader[ProtocolConstants.TTL_INDEX] = this.ttl;

        // Destination-Name
        byte[] destNameBytes =destinationName.getBytes(StandardCharsets.UTF_8);


        for (int destIndex = 0, byteStreamIndex = ProtocolConstants.TYPE_SIZE_IN_BYTE + ProtocolConstants.TTL_SIZE_IN_BYTE;
             destIndex < ProtocolConstants.DESTINATION_NETWORK_NAME_SIZE_IN_BYTE;
             destIndex++, byteStreamIndex++) {
            basisHeader[byteStreamIndex] = destNameBytes[destIndex];
            basisHeader[byteStreamIndex] = destNameBytes[destIndex];
            basisHeader[byteStreamIndex] = destNameBytes[destIndex];
        }

        // Source-Name
        byte[] sourceNameBytes = this.sourceName.getBytes(StandardCharsets.UTF_8);

        for (int srcIndex = 0, byteStreamIndex = ProtocolConstants.TYPE_SIZE_IN_BYTE + ProtocolConstants.TTL_SIZE_IN_BYTE
                + ProtocolConstants.DESTINATION_NETWORK_NAME_SIZE_IN_BYTE; srcIndex < ProtocolConstants.SOURCE_NETWORK_NAME_SIZE_IN_BYTE;
             srcIndex++, byteStreamIndex++) {
            basisHeader[byteStreamIndex] = sourceNameBytes[srcIndex];
            basisHeader[byteStreamIndex] = sourceNameBytes[srcIndex];
            basisHeader[byteStreamIndex] = sourceNameBytes[srcIndex];
        }

        return basisHeader;
    }

}
