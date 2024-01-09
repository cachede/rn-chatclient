package ahmed.daniel.routing;

import java.util.Objects;

public class RoutingTable {
    

    private String destination;
    private String nextHop;
    private byte hopCount;


    public RoutingTable(String destination, String nextHop, byte hopCount) {

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
        RoutingTable that = (RoutingTable) o;
        return hopCount == that.hopCount && Objects.equals(destination, that.destination) && Objects.equals(nextHop, that.nextHop);
    }

    @Override
    public int hashCode() {
        return Objects.hash(destination, nextHop);
    }
}