import ahmed.daniel.Messages.CommunicationMessage;
import ahmed.daniel.Messages.ConnectionMessage;
import ahmed.daniel.Messages.Message;
import ahmed.daniel.ProtocolConstants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.*;


/**
 * This Testclass tests the different messages we defined in our protocol. That would be: CommunicationMessage, RoutingMessage and Connectionmessage
 */
public class MessageTest {


    public String mockupAddress;
    public int mockupPort2;
    public Socket client;
    public Socket server;
    public ServerSocket serverSocket;
    public String sourceName;

    @BeforeEach
    public void setUp() {
        mockupAddress = "127.0.0.1";
        mockupPort2 = 14000;
        try {
            serverSocket = new ServerSocket(mockupPort2);

            new Thread(() -> {
                try {
                    server = serverSocket.accept();
                } catch (IOException e) {
                    System.err.println("Failed to accept Connection");
                }
            }).start();

            client = new Socket(InetAddress.getByName(mockupAddress), mockupPort2);
        } catch (IOException e) {
            System.err.println("Could not create Mockup-Sockets");

        }
        sourceName = "DAN";
    }

    @AfterEach
    public void shutdown() {
        try {
            client.close();
            server.close();
            serverSocket.close();
        } catch (IOException e) {
            System.err.println("Failed to close the sockets");
        }

    }

    @Test
    public void testForSizeConnectionMessage() {
        try {
            DataInputStream in = new DataInputStream(server.getInputStream());
            Message connectionMessage = new ConnectionMessage(sourceName);
            connectionMessage.sendTo(client, ProtocolConstants.DESTINATION_NETWORK_NAME_NOT_SET);

            byte[] byteStream = new byte[ProtocolConstants.BASISHEADER_SIZE_IN_BYTE];
            in.readFully(byteStream);
            String gotString = new String(byteStream, "UTF-8");
            System.out.println(gotString);

            assertFalse(gotString.isEmpty());
            assertEquals(ProtocolConstants.BASISHEADER_SIZE_IN_BYTE, byteStream.length);

        } catch (IOException e) {
            System.err.println("Cannot create Inputstream for server");
        }
    }

    @Test
    public void testForTypeConnectionMessage() {
        try {
            DataInputStream in = new DataInputStream(server.getInputStream());
            Message connectionMessage = new ConnectionMessage(sourceName);
            connectionMessage.sendTo(client, ProtocolConstants.DESTINATION_NETWORK_NAME_NOT_SET);

            byte type = in.readByte();
            assertEquals(type, ProtocolConstants.TYPE_VERBINDUNGSPAKET);

        } catch (IOException e) {
            System.err.println("Cannot create Inputstream for server");
        }
    }

    @Test
    public void testForTTLConnectionMessage() {
        try {
            DataInputStream in = new DataInputStream(server.getInputStream());
            Message connectionMessage = new ConnectionMessage(sourceName);
            connectionMessage.sendTo(client, ProtocolConstants.DESTINATION_NETWORK_NAME_NOT_SET);

            byte[] byteStream = new byte[ProtocolConstants.BASISHEADER_SIZE_IN_BYTE];
            in.readFully(byteStream);

            assertEquals(byteStream[ProtocolConstants.TTL_INDEX], ProtocolConstants.TTL);

        } catch (IOException e) {
            System.err.println("Cannot create Inputstream for server");
        }
    }

    @Test
    public void testForSourcenameConnectionMessage() {
        try {
            DataInputStream in = new DataInputStream(server.getInputStream());
            Message connectionMessage = new ConnectionMessage(sourceName);
            connectionMessage.sendTo(client, ProtocolConstants.DESTINATION_NETWORK_NAME_NOT_SET);

            byte[] byteStream = new byte[ProtocolConstants.BASISHEADER_SIZE_IN_BYTE];
            in.readFully(byteStream);
            byte[] sourceNameByte = new byte[ProtocolConstants.SOURCE_NETWORK_NAME_SIZE_IN_BYTE];
            System.arraycopy(byteStream, ProtocolConstants.SOURCE_NETWORK_NAME_LOWER, sourceNameByte, 0, ProtocolConstants.SOURCE_NETWORK_NAME_SIZE_IN_BYTE);
            String sourceNameString = new String(sourceNameByte, "UTF-8");
            assertEquals(sourceNameString, sourceName);


        } catch (IOException e) {
            System.err.println("Cannot create Inputstream for server");
        }
    }

    @Test
    public void testForDestinationnameConnectionMessage() {
        try {
            DataInputStream in = new DataInputStream(server.getInputStream());
            Message connectionMessage = new ConnectionMessage(sourceName);
            connectionMessage.sendTo(client, ProtocolConstants.DESTINATION_NETWORK_NAME_NOT_SET);

            byte[] byteStream = new byte[ProtocolConstants.BASISHEADER_SIZE_IN_BYTE];
            in.readFully(byteStream);
            byte[] destinationnameByte = new byte[ProtocolConstants.DESTINATION_NETWORK_NAME_SIZE_IN_BYTE];
            System.arraycopy(byteStream, ProtocolConstants.DESTINATION_NETWORK_NAME_LOWER, destinationnameByte, 0, ProtocolConstants.DESTINATION_NETWORK_NAME_SIZE_IN_BYTE);
            String sourceNameString = new String(destinationnameByte, "UTF-8");
            assertEquals(sourceNameString, "zzz");

        } catch (IOException e) {
            System.err.println("Cannot create Inputstream for server");
        }
    }

    @Test
    public void testForCommunicationMessage() {

    }
}
