package ahmed.daniel;

import java.net.*;
import java.util.Arrays;
import java.io.*;

import java.nio.ByteBuffer;

public class ReceiverTask implements Runnable{

    private final Socket socket;
    private final String name;
    private final ActiveConnectionManager activeConnectionManager;
    private final RoutingTableManager routingTableManager;

    public ReceiverTask(Socket socket, String name, ActiveConnectionManager activeConnections, RoutingTableManager routingTableManager){
        this.socket = socket;
        this.name = name;
        this.activeConnectionManager = activeConnections;
        this.routingTableManager = routingTableManager;
        if (this.socket == null) {
            System.out.println("DER IGEL IST HEUTE SEHR STACHELIG");
        }
    }
    
    /**
     * Pretty important Method. It receives (Pakets|Datagram|Messages) from the direct Connection.
     * There are 3 different Types of Messages to be received.
     * Type 0: Verbindungspaket. This is the first Message which will be received after the initial Connection.
     * Type 1: Routingpaket. This Message contains no Message but a Routingtable from its direct Connections.
     * This is useful, in order to know which Participants are connected and how to get to them. (Teilvermascht)
     * This Message will be parsed and is the Content of the private Membervariable routingTable.
     * Type 2: Nachrichtenpaket: This Message contains the actual Message.
     */
    private void receiveMessage() {
        try {
            //BufferedReader in = new BufferedReader(new InputStreamReader(this.socket.getInputStream(), "UTF-8")); //Wenn es noch keine Verbindung gibt Big Problem
            DataInputStream in = new DataInputStream(socket.getInputStream());

            byte[] buffer = new byte[ProtocolConstants.MAX_MESSAGE_LENGTH_IN_BYTES];
            while(true) {
                in.readFully(buffer);

                System.out.println("Buffer-Bytes: "+ buffer);
                String res = new String(buffer, "UTF-8");
                System.out.println("Buffer-String: " + res);


                switch(buffer[0]) {
                    case ProtocolConstants.TYPE_VERBINDUNGSPAKET: {

                        byte TYPE = buffer[0];
                        byte TTL = buffer[1];
                        
                        byte destinationName1 = buffer[2];
                        byte destinationName2 = buffer[3];
                        byte desinationName3 = buffer[4];
                        byte[] destinationNameAsBytes = {destinationName1, destinationName2, desinationName3};
                        String destinationName = new String(destinationNameAsBytes, "UTF-8");

                        byte sourceName1 = buffer[5];
                        byte sourceName2 = buffer[6];
                        byte sourceName3 = buffer[7];

                        byte[] sourceNameAsBytes = {sourceName1, sourceName2, sourceName3};
                        String sourceName = new String(sourceNameAsBytes, "UTF-8") ;

                        activeConnectionManager.addActiveConnection(sourceName, this.socket);
                        // Here we had to swap the positions of destName and sourceName TODO gkaub das ist falsch + 1 magic number
                        routingTableManager.addRoutingTableEntry(destinationName, sourceName, (short)socket.getPort(),(byte)1);

                        // Send our name to source if our name is not set for them yet
                        if (destinationName.equals(ProtocolConstants.DESTINATION_NETWORK_NAME_NOT_SET)){
                            Message namePackage  = new ConnectionMessage(this.socket, this.name);
                            namePackage.sendTo(sourceName);
                        }

                        System.out.println("NAMENPAKET");
                    }
                    break;
                    case ProtocolConstants.TYPE_ROUTINGPAKET: {
                        System.out.println("Routingpaket");
                        int amountOfRoutingTables = buffer[8];
                        byte[] routingMessage = Arrays.copyOfRange(buffer, 9, buffer.length);
                                                
                        for(int i = 0; i < amountOfRoutingTables; i++) {
                            int offset = i* ProtocolConstants.ROUTING_ENTRY_SIZE_IN_BYTE;

                            // Source
                            int startIndex = offset;
                            int stopIndex = startIndex + ProtocolConstants.ROUTING_SOURCE_SIZE_IN_BYTE;
                            byte[] sourceBytes = Arrays.copyOfRange(routingMessage, offset, stopIndex);
                            String source = new String(sourceBytes, "UTF-8");

                            // Destination
                            startIndex = stopIndex;
                            stopIndex = startIndex + ProtocolConstants.ROUTING_DESTINATION_SIZE_IN_BYTE;
                            byte[] destinationBytes = Arrays.copyOfRange(routingMessage, startIndex, stopIndex);
                            String destination = new String(destinationBytes, "UTF-8");
                            // Port
                            startIndex = stopIndex;
                            stopIndex = startIndex + ProtocolConstants.PORT_SIZE_IN_BYTE;
                            byte[] portBytes = Arrays.copyOfRange(routingMessage, startIndex, stopIndex);
                            short port =  ByteBuffer.wrap(portBytes).getShort();

                            // Port versuch 2
                            // Two byte values
                            byte byte1 = portBytes[0];   // Binary: 00011111
                            byte byte2 = portBytes[1]; // Binary: 10011010 (Note: byte range is -128 to 127)

                            // Convert bytes to their binary strings
                            String byte1Binary = String.format("%8s", Integer.toBinaryString(byte1 & 0xFF)).replace(' ', '0');
                            String byte2Binary = String.format("%8s", Integer.toBinaryString(byte2 & 0xFF)).replace(' ', '0');

                            // Combine the binary strings
                            String combinedBinary = byte1Binary + byte2Binary;

                            // Convert the combined binary string to decimal
                            int portRichtig = Integer.parseInt(combinedBinary, 2);

                            System.out.println("PORTERGEBNIS: " + portRichtig);
                            // HopCount
                            startIndex = stopIndex;
                            byte hopCountBytes = routingMessage[startIndex];
                            
                            // For Debugging:
                            RoutingTable routingTable = new RoutingTable(source, destination, (short)portRichtig, hopCountBytes);
                            System.out.println("In Recv erstellter Table:");
                            routingTable.printRoutingTable();

                            routingTableManager.addRoutingTableEntry(source, destination, (short)portRichtig, hopCountBytes);
                        }                    
                    }
                    break;
                    case ProtocolConstants.TYPE_MESSAGEPAKET: {
                        //Hier Annahme: Jede Nachricht die ankommt ist auch an uns gedacht
                        //Richtig wäre: Jede Nachricht die ankommt MUSS gecheckt werden, ob sie an uns gerichtet war.
                        // ----------> WENN JA: Print auf STDOUT
                        //-----------> WENN NEIN: Leite weiter an richtigen Socket
                        byte destinationName1 = buffer[2];
                        byte destinationName2 = buffer[3];
                        byte desinationName3 = buffer[4];
                        byte[] destinationNameAsBytes = {destinationName1, destinationName2, desinationName3};
                        String destinationName = new String(destinationNameAsBytes, "UTF-8");
                        
                        // If message is for us -> print it
                        
                        // TODO Woher kenne ich den source name

                        //if(destinationName.equals()){
                            byte[] message = Arrays.copyOfRange(buffer, 8, buffer.length);
                            String messageStr = new String(message, "UTF-8");

                            System.out.println("Nachrichtenpaket: " + messageStr);
                            //break;
                        //}

                        // if not -> redirect message




                    }
                    break;
                    default: 
                        System.out.println("Keine Ahnung was das fuer ein Paket"); break;
                }
            //}
        }

            } catch (IOException e) {
                //System.out.println("IO-EXCEPTION WHILE RECEIVING");
                
            } catch(NullPointerException n) {
                System.out.println("A Connection should be set before receiving a Message");
                
            }
        
    }

    @Override
    public void run(){
        receiveMessage();
    }
}