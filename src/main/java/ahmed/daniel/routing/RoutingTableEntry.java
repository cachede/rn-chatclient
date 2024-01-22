package ahmed.daniel.routing;
import ahmed.daniel.ProtocolConstants;
import java.util.Objects;

public class RoutingTableEntry {
    

    private String destination;
    private String nextHop;
    private byte hopCount;


    public RoutingTableEntry(String destination, String nextHop, byte hopCount) {

        this.destination = destination;
        this.nextHop= nextHop;
        this.hopCount = hopCount;
    }

    public String getDestination() {
        return this.destination;
    }

    public String getNextHop() {
        return nextHop;
    }


    public byte getHopCount() {
        return this.hopCount;
    }
    public void setHopCount(byte newHopCount){
        this.hopCount = newHopCount;
    }

    public void setAsUnreachable(){
        this.hopCount = ProtocolConstants.ROUTING_DESTINATION_UNREACHABLE;
    }

    public void printRoutingTable() {
        System.out.println(this.toString());
    }

    @Override
    public String toString(){
        return "------------------------\n" +
                "Routingtable:\n" +
                "Destination:\t" + this.destination + "\n" +
                "Next Hop:\t" + this.nextHop + "\n" +
                "Hop Count:\t" + this.hopCount + "\n" +
                "------------------------\n";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoutingTableEntry that = (RoutingTableEntry) o;
        return Objects.equals(destination, that.destination) && Objects.equals(nextHop, that.nextHop);
    }

    @Override
    public int hashCode() {
        return Objects.hash(destination, nextHop);
    }
}