package ahmed.daniel;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;


/*
 * TODO: 
 * 
 * - Network with A and B which are connected; If for Example B quits, A should remove it from its activeConnections (Possibly being fixed with routingtables and updates)
 * ---> I dont think so, because
 * 
 * - Send Routing-Table-Updates -> Sender Thread which sends every x seconds routingtable -> Kritischer Bereich drauf achten -> Routintabelle -> synchronized
 * 
 * - Receive Routing-Table-Updates -> Extra thread ? -> Or just use existing one and update the own Routing-Table with the new Information
 * ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
 * Extra:
 * 
 * - Refactor-> Split into Classes
 * 
 */


public class Main {
    // arg[0] = IP-Adresse
    // arg[1] = Port
    // arg[2] = Name
    public static void main(String[] args) throws UnknownHostException {

        //Initialisation of all Components.
        ChatClient client = new ChatClient(InetAddress.getByName(args[0]), Integer.parseInt(args[1]), args[2]);
        UI ui = new UI(client);

        // start client
        client.startClient();

        //Main Loop
        ui.mainLoop();


    }

}
