package ahmed.daniel;

import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class RoutingThread implements Runnable {


    @Override
    public void run() {

        /*
        while(!Thread.interrupted()) {
            try {
                Thread.sleep(30000);

                // Build Routingpackage
                List<RoutingTable> currentRoutingTables = routingTableManager.getRoutingTables();
                byte size = (byte)currentRoutingTables.size();          //byte will be ok
                byte[] routingPackageBytes = new byte[size * protocolConstants.ROUTING_ENTRY_SIZE_IN_BYTE];
                List<Byte> routingPackageBytesList = new LinkedList<>();

                routingPackageBytesList.add(size);
                for(RoutingTable routingTable : currentRoutingTables) {
                    // Source
                    byte[] sourceBytes = routingTable.getSource().getBytes("UTF-8");
                    for(int i = 0; i < protocolConstants.ROUTING_SOURCE_SIZE_IN_BYTE; i++){
                        routingPackageBytesList.add(sourceBytes[i]);
                    }

                    // Destination
                    byte[] destinationBytes = routingTable.getDestination().getBytes("UTF-8");
                    for(int i = 0; i < protocolConstants.ROUTING_DESTINATION_SIZE_IN_BYTE; i++) {
                        routingPackageBytesList.add(destinationBytes[i]);
                    }

                    // Port
                    byte[] byteArray = new byte[2];
                    byteArray[0] = (byte) (routingTable.getPort() & 0xFF);
                    byteArray[1] = (byte) ((routingTable.getPort() >> 8) & 0xFF);
                    routingPackageBytesList.add(byteArray[0]);
                    routingPackageBytesList.add(byteArray[1]);

                    int value = 0;
                    for (int i = 0; i < 2; i++) {
                        value = (value << 8) | (byteArray[i] & 0xFF);
                    }
                    // HopCount
                    routingPackageBytesList.add((byte)routingTable.getHopCount());
                }

                // List to Array
                for(int i = 0; i < routingPackageBytes.length; i++)
                {
                    routingPackageBytes[i] = routingPackageBytesList.get(i);
                }

                for(Map.Entry<String, Socket> entry : activeConnectionManager.getEntrySet()){
                    client.sendRouting(routingPackageBytes, entry.getValue(), entry.getKey());
                }


            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }


        }

         */
    }
}
