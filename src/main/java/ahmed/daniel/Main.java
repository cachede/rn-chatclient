package ahmed.daniel;
import java.io.IOException;
import java.net.InetAddress;
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

/**
 * Main Class from which the Application is started
 */
public class Main {
    public static void main(String[] args) {

        // So fucking ugly
        boolean validInput;
        Scanner userInputScanner = new Scanner(System.in);
        do {
            try {
                System.out.println("Please enter your IP-Address: ");
                String userIpAdress = userInputScanner.nextLine();
                System.out.println("Please enter your Port: ");
                int userPortNumber = Integer.parseInt(userInputScanner.nextLine());
                System.out.println("Please enter your 3 letter Username: ");
                String userName = userInputScanner.nextLine();

                ChatClient client = new ChatClient(InetAddress.getByName(userIpAdress), userPortNumber, userName);
                validInput = false;
                UI ui = new UI(client);
                client.startClient();
                ui.mainLoop();
                userInputScanner.close();
            } catch (IOException e) {
                System.out.println("IP-Address or Port was not valid, please try again");
                validInput = true;
            } catch (IllegalArgumentException e) {
                System.out.println("Arguments passed were not valid, please try again");
                validInput = true;
            }

        } while(validInput);


        //Maybe ask the user for input until the input is correct
        /*                      OLD
        try {
            ChatClient client = new ChatClient(InetAddress.getByName(args[0]), Integer.parseInt(args[1]), args[2]);
            UI ui = new UI(client);
            client.startClient();
            ui.mainLoop();
        } catch (IOException ioException) {
            System.err.println("The entered IP-Address or Port are not valid, try again");
        }

        */
    }

}
