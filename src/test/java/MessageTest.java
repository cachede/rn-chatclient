import ahmed.daniel.messages.*;
import ahmed.daniel.ProtocolConstants;
import ahmed.daniel.routing.RoutingTableEntry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


/**
 * This Testclass tests the different messages we defined in our protocol. That would be: CommunicationMessage, RoutingMessage and Connectionmessage
 * This class tests, if our messages which we send through the socket send the right information at the right place in the bytestream.
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
            Message connectionMessage = new ConnectionMessage(sourceName, ProtocolConstants.TTL);
            connectionMessage.sendTo(client, ProtocolConstants.DESTINATION_NETWORK_NAME_NOT_SET);

            byte[] byteStream = new byte[ProtocolConstants.BASISHEADER_SIZE_IN_BYTE];
            in.readFully(byteStream);
            String gotString = new String(byteStream, StandardCharsets.UTF_8);
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
            Message connectionMessage = new ConnectionMessage(sourceName, ProtocolConstants.TTL);
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
            Message connectionMessage = new ConnectionMessage(sourceName, ProtocolConstants.TTL);
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
            Message connectionMessage = new ConnectionMessage(sourceName, ProtocolConstants.TTL);
            connectionMessage.sendTo(client, ProtocolConstants.DESTINATION_NETWORK_NAME_NOT_SET);

            byte[] byteStream = new byte[ProtocolConstants.BASISHEADER_SIZE_IN_BYTE];
            in.readFully(byteStream);
            byte[] sourceNameByte = new byte[ProtocolConstants.SOURCE_NETWORK_NAME_SIZE_IN_BYTE];
            System.arraycopy(byteStream, ProtocolConstants.SOURCE_NETWORK_NAME_LOWER, sourceNameByte, 0, ProtocolConstants.SOURCE_NETWORK_NAME_SIZE_IN_BYTE);
            String sourceNameString = new String(sourceNameByte, StandardCharsets.UTF_8);
            assertEquals(sourceNameString, sourceName);
            assertEquals(sourceNameString.length(), ProtocolConstants.SOURCE_NETWORK_NAME_SIZE_IN_BYTE);

        } catch (IOException e) {
            System.err.println("Cannot create Inputstream for server");
        }
    }

    @Test
    public void testForDestinationnameConnectionMessage() {
        try {
            DataInputStream in = new DataInputStream(server.getInputStream());
            Message connectionMessage = new ConnectionMessage(sourceName, ProtocolConstants.TTL);
            connectionMessage.sendTo(client, ProtocolConstants.DESTINATION_NETWORK_NAME_NOT_SET);

            byte[] byteStream = new byte[ProtocolConstants.BASISHEADER_SIZE_IN_BYTE];
            in.readFully(byteStream);
            byte[] destinationnameByte = new byte[ProtocolConstants.DESTINATION_NETWORK_NAME_SIZE_IN_BYTE];
            System.arraycopy(byteStream, ProtocolConstants.DESTINATION_NETWORK_NAME_LOWER, destinationnameByte, 0, ProtocolConstants.DESTINATION_NETWORK_NAME_SIZE_IN_BYTE);
            String sourceNameString = new String(destinationnameByte, StandardCharsets.UTF_8);
            assertEquals(sourceNameString, "zzz");
            assertEquals(sourceNameString.length(), ProtocolConstants.SOURCE_NETWORK_NAME_SIZE_IN_BYTE);
        } catch (IOException e) {
            System.err.println("Cannot create Inputstream for server");
        }
    }

    @Test
    public void testPayloadLengthIndexCommunicationMessage() {
        try {
            String payloadString = "secret message";
            DataInputStream in = new DataInputStream(server.getInputStream());
            Message communicationMessage = new CommunicationMessage(sourceName, ProtocolConstants.TTL, payloadString);
            communicationMessage.sendTo(client, ProtocolConstants.DESTINATION_NETWORK_NAME_NOT_SET);

            byte[] byteStream = new byte[ProtocolConstants.BASISHEADER_SIZE_IN_BYTE + ProtocolConstants.COMMUNICATION_MESSAGE_LENGTH_IN_BYTE + payloadString.length()];
            in.readFully(byteStream);

            assertEquals(byteStream[ProtocolConstants.BASISHEADER_SIZE_IN_BYTE + ProtocolConstants.COMMUNICATION_MESSAGE_LENGTH_INDEX], payloadString.length());


        } catch (IOException e) {
            System.err.println("Cannot create Inputstream for server");
        }
    }

    @Test
    public void testPayloadCommunicationMessage() {
        try {
            String payloadString = "secret message";
            DataInputStream in = new DataInputStream(server.getInputStream());
            Message communicationMessage = new CommunicationMessage(sourceName, ProtocolConstants.TTL, payloadString);
            communicationMessage.sendTo(client, ProtocolConstants.DESTINATION_NETWORK_NAME_NOT_SET);

            byte[] byteStream = new byte[ProtocolConstants.BASISHEADER_SIZE_IN_BYTE + ProtocolConstants.COMMUNICATION_MESSAGE_LENGTH_IN_BYTE + payloadString.length()];
            in.readFully(byteStream);
            byte[] payloadStream = new byte[payloadString.length()];
            System.arraycopy(byteStream, ProtocolConstants.BASISHEADER_SIZE_IN_BYTE + ProtocolConstants.COMMUNICATION_MESSAGE_LENGTH_IN_BYTE
            ,payloadStream, 0, payloadString.length());
            String payloadGotString = new String(payloadStream, StandardCharsets.UTF_8);
            assertEquals(payloadGotString, payloadString);

        } catch (IOException e) {
            System.err.println("Cannot create Inputstream for server");
        }
    }

    @Test
    public void testRoutingTableAmountOfEntries() {
        RoutingTableEntry routingTable = new RoutingTableEntry("DAN", "DAN", (byte)1);
        List<RoutingTableEntry> routingTableList = new LinkedList<>();
        routingTableList.add(routingTable);

        try {
            DataInputStream in = new DataInputStream(server.getInputStream());
            Message routingMessage = new RoutingMessage(sourceName, ProtocolConstants.TTL, routingTableList);
            routingMessage.sendTo(client, ProtocolConstants.DESTINATION_NETWORK_NAME_NOT_SET);

            byte[] byteStream = new byte[ProtocolConstants.BASISHEADER_SIZE_IN_BYTE + routingTableList.size() + ProtocolConstants.ROUTING_ENTRY_SIZE_IN_BYTE];
            in.readFully(byteStream);
            byte[] routingStream = new byte[ProtocolConstants.ROUTING_AMOUNT_OF_PACKETS_SIZE_IN_BYTE + routingTableList.size() * ProtocolConstants.ROUTING_ENTRY_SIZE_IN_BYTE];
            System.arraycopy(byteStream, ProtocolConstants.BASISHEADER_SIZE_IN_BYTE, routingStream, 0, ProtocolConstants.ROUTING_AMOUNT_OF_PACKETS_SIZE_IN_BYTE + routingTableList.size() * ProtocolConstants.ROUTING_ENTRY_SIZE_IN_BYTE);
            assertEquals(routingTableList.size(), routingStream[ProtocolConstants.ROUTING_AMOUNT_OF_PACKETS_INDEX]);
            //extract routingStream
            byte[] routingStreamDestination = new byte[ProtocolConstants.ROUTING_DESTINATION_SIZE_IN_BYTE];
            System.arraycopy(routingStream, ProtocolConstants.ROUTING_INDEX_OF_DESTINATION, routingStreamDestination, 0, ProtocolConstants.ROUTING_DESTINATION_SIZE_IN_BYTE);
            String routingDestination = new String(routingStreamDestination, StandardCharsets.UTF_8);
            assertEquals(routingTableList.get(0).getDestination(), routingDestination);

            byte[] routingStreamNexthop = new byte[ProtocolConstants.ROUTING_NEXT_HOP_SIZE_IN_BYTE];
            System.arraycopy(routingStream, ProtocolConstants.ROUTING_INDEX_OF_NEXTHOP, routingStreamNexthop, 0, ProtocolConstants.ROUTING_NEXT_HOP_SIZE_IN_BYTE);
            String routingNexthop = new String(routingStreamNexthop, StandardCharsets.UTF_8);
            assertEquals(routingTableList.get(0).getNextHop(), routingNexthop);

            byte routingStreamHopcount = routingStream[ProtocolConstants.ROUTING_INDEX_OF_HOP_COUNT];
            assertEquals(routingTableList.get(0).getHopCount(), routingStreamHopcount);

        } catch (IOException e) {
            System.err.println("Cannot create Inputstraem for server");
        }

    }

    @Test
    public void testCRC32ChecksumForCommunicationMessage() {
        String message = "secret message";
        byte type = ProtocolConstants.TYPE_MESSAGEPAKET;
        byte ttl = ProtocolConstants.TTL;
        byte[] destination = ProtocolConstants.DESTINATION_NETWORK_NAME_NOT_SET.getBytes(StandardCharsets.UTF_8);
        byte[] source = sourceName.getBytes(StandardCharsets.UTF_8);
        byte messageLen = (byte) message.length();
        byte[] messageByte = message.getBytes(StandardCharsets.UTF_8);


        try {
            DataInputStream in = new DataInputStream(server.getInputStream());
            Message communicationMessage = new CommunicationMessage(sourceName, ProtocolConstants.TTL, message);
            communicationMessage.sendTo(client, ProtocolConstants.DESTINATION_NETWORK_NAME_NOT_SET);

            byte[] byteStream = new byte[ProtocolConstants.BASISHEADER_SIZE_IN_BYTE + ProtocolConstants.COMMUNICATION_MESSAGE_LENGTH_IN_BYTE + message.length() + ProtocolConstants.CHECKSUM_CRC32_SIZE];
            in.readFully(byteStream);

            byte[] checkSumByte = new byte[ProtocolConstants.CHECKSUM_CRC32_SIZE];
            System.arraycopy(byteStream, ProtocolConstants.BASISHEADER_SIZE_IN_BYTE + 1 + message.length(), checkSumByte, 0, ProtocolConstants.CHECKSUM_CRC32_SIZE);
            System.out.println(checkSumByte.length);

        } catch (IOException e) {
            System.err.println("Cannot crate Inputstraem for server");
        }
    }

}
