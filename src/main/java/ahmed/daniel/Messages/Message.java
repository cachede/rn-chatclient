package ahmed.daniel.Messages;

import ahmed.daniel.ProtocolConstants;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;

/**
 * A message which is being send to Participants in the network contains a Header which is implemented in this abstract
 * class. The header has always the same structure (see protocol). Building the header is always the same process, just
 * differs in the parameters.
 */
public abstract class Message {

    private final String sourceName;
    private final byte type;

    /**
     * Creates a Message with a specific type and source name. The type differentiates between messages/connections/routing
     * @param type          used to identify what the message contains(text/connection/routing-entries
     * @param sourceName    The name, from which this message comes from
     */
    public Message(byte type, String sourceName) {
        this.type = type;
        this.sourceName = sourceName;
    }

    private byte[] buildMessage(String destinationName) throws UnsupportedEncodingException {
        byte[] payload = getPayloadInBytes();
        byte[] basisHeader = getBasisHeader(destinationName);

        // Build message with header and paylaod
        byte[] message = new byte[payload.length + basisHeader.length];
        System.arraycopy(basisHeader, 0, message, 0, basisHeader.length);
        System.arraycopy(payload, 0, message, basisHeader.length, payload.length);

        byte[] filledMessage = fillWithFillbytes(message);
        return filledMessage;
    }

    /**
     * The value in a Message differs, in how it is encoded. Every class that extends the Message class should provide
     * a implementation of this function, for whatever the message should contain as a value.
     * @return  a byte-array which contains the value encoded in bytes
     * @throws UnsupportedEncodingException
     */
    protected abstract byte[] getPayloadInBytes() throws UnsupportedEncodingException;

    /**
     * This method is called to send a Message to a destination name. First it build the protocolheader and fills it
     * with the destination name and source name. After the header is build the method sends the byte-stream to the
     * given socket
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
     * TODO: remove this method and introduce a length for the value
     * @param byteStream
     * @return
     */
    private byte[] fillWithFillbytes(byte[] byteStream) {
        int len = byteStream.length;
        if (len == ProtocolConstants.MAX_MESSAGE_LENGTH_IN_BYTES) {
            return byteStream;
        }
        byte[] filledByteStream = new byte[ProtocolConstants.MAX_MESSAGE_LENGTH_IN_BYTES];
        System.arraycopy(byteStream, 0, filledByteStream, 0, len);

        for (int i = len; i < ProtocolConstants.MAX_MESSAGE_LENGTH_IN_BYTES; i++) {
            filledByteStream[i] = 0;
        }

        return filledByteStream;
    }

    private byte[] getBasisHeader(String destinationName) {
        byte[] basisHeader = new byte[ProtocolConstants.BASISHEADER_SIZE_IN_BYTE];
        // Type and TTL
        basisHeader[0] = this.type;
        basisHeader[1] = ProtocolConstants.TTL;

        // Destination-Name
        byte[] destNameBytes = new byte[ProtocolConstants.DESTINATION_NETWORK_NAME_SIZE_IN_BYTE];
        try {
            destNameBytes = destinationName.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            System.out.println("ERROR: ADDBASISHEADER CONVERT DEST TO BYTE[]");
        }

        for (int destIndex = 0, byteStreamIndex = ProtocolConstants.TYPE_SIZE_IN_BYTE + ProtocolConstants.TTL_SIZE_IN_BYTE;
             destIndex < ProtocolConstants.DESTINATION_NETWORK_NAME_SIZE_IN_BYTE;
             destIndex++, byteStreamIndex++) {
            basisHeader[byteStreamIndex] = destNameBytes[destIndex];
            basisHeader[byteStreamIndex] = destNameBytes[destIndex];
            basisHeader[byteStreamIndex] = destNameBytes[destIndex];
        }

        // Source-Name
        byte[] sourceNameBytes = new byte[ProtocolConstants.SOURCE_NETWORK_NAME_SIZE_IN_BYTE];
        try {
            sourceNameBytes = this.sourceName.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            System.out.println("ERROR: ADDBASISHEADER CONVERT SRC TO BYTE[]");
        }
        for (int srcIndex = 0, byteStreamIndex = ProtocolConstants.TYPE_SIZE_IN_BYTE + ProtocolConstants.TTL_SIZE_IN_BYTE
                + ProtocolConstants.DESTINATION_NETWORK_NAME_SIZE_IN_BYTE; srcIndex < ProtocolConstants.SOURCE_NETWORK_NAME_SIZE_IN_BYTE;
             srcIndex++, byteStreamIndex++) {
            basisHeader[byteStreamIndex] = sourceNameBytes[srcIndex];
            basisHeader[byteStreamIndex] = sourceNameBytes[srcIndex];
            basisHeader[byteStreamIndex] = sourceNameBytes[srcIndex];
        }

        return basisHeader;
    }

    //TODO: crc32 checksum berechnen
}
