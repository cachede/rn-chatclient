package ahmed.daniel;

import java.util.Objects;

public class RoutingTable {
    
    private String source;
    private String destination;
    private short port;
    private byte hopCount;


    public RoutingTable(String source, String destination, short port, byte hopCount) {
        this.source = source;
        this.destination = destination;
        this.port = port;
        this.hopCount = hopCount;
    }

    public String getSource() {
        return this.source;
    }
    
    public String getDestination() {
        return this.destination;
    }

    public byte getHopCount() {
        return this.hopCount;
    }

    public short getPort() {
        return this.port;
    }

    public void printRoutingTable() {
    System.out.println("------------------------");
    System.out.println("Routing Table:");
    System.out.println("Source: " + source);
    System.out.println("Destination: " + destination);
    System.out.println("Port: " + port);
    System.out.println("Hop Count: " + hopCount);
    System.out.println("------------------------");
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        RoutingTable other = (RoutingTable) obj;

        return Objects.equals(source, other.source) &&
            Objects.equals(destination, other.destination) &&
            port == other.port &&
            hopCount == other.hopCount;
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, destination, port, hopCount);
    }
}