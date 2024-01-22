package ahmed.daniel;

import java.util.Scanner;

/**
 * Is responsible for Interacting with the Client. It processes all User Inputs from STDIN and calls the corresponding
 * functionality on the ChatClient-Object. Also handles the OUTPUT by printing to STDOUT.
 * The UI offers the user: connecting to another Client by his ipv4-address and port, sending a message to a connected
 * client in the network, show all participants in the network and disconnecting from the network. Also it can show
 * the user a help board, which shows the user of the application how to interact with the program.
 */
public class UI {
    
    public final String CONNECT_TO = "1";
    public final String SEND_MESSAGE = "2";
    public final String SHOW_PARTICIPANTS = "3";
    public final String SHOW_HELP = "h";
    public final String QUIT_PROGRAM = "q";

    private final ChatClient chatClient;

    /**
     * Creates instance of the UI. The UI needs a ChatClient on which it calls the corresponding functionality,
     * which the user enters.
     *
     * @param chatClient    A ChatClient object on which connecting
     */
    public UI(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /**
     * During the mainLoop the User is prompted to give user input through STDIN. It processes the Userinput and
     * calls the corresponding function in order to achieve the users desired. At the beginning of the function the
     * User is shown a small help-board to explain which Input does what. The user can always print it again with "h".
     * This method is in a infinity-loop until the user asks explicity to quit the application by pressing "q"
     */
    public void mainLoop() {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;
        printMenu();

        while(running) {
            System.out.print("COMMAND: ");
            String input = scanner.nextLine().toLowerCase();
            switch (input) {
                case CONNECT_TO: {
                    try {
                        System.out.println("IPV4-ADDRESS: ");
                        String ipv4address = scanner.nextLine();
                        System.out.println("PORT: ");
                        int port = Integer.parseInt(scanner.nextLine());
                        chatClient.addNewConnection(ipv4address, port);

                    }catch (NumberFormatException nfe) {
                        System.out.println("Please enter a valid IP/Port");
                    }
                    break;
                }
                case SEND_MESSAGE: {
                    System.out.print("MESSAGE: ");
                    String message = scanner.nextLine();
                    System.out.print("Send a message to NAME: ");
                    String destinationName = scanner.nextLine();

                    chatClient.sendMessage(message, destinationName);
                    System.out.println("YOU: " + message);
                    break;
                }
                case SHOW_PARTICIPANTS: {
                    System.out.println("Showing Participants ...");
                    for(String name : chatClient.getActiveConnectionNames()) {
                        System.out.println(name);
                    }

                    break;
                }
                case QUIT_PROGRAM: {
                    System.out.println("Quitting Program ...");
                    chatClient.disconnect();
                    scanner.close();
                    running = false;
                    break;
                }
                case SHOW_HELP: {
                    chatClient.printAllRoutingTables();
                    break;
                }
                default: {
                    System.out.println("Input was not valid!");
                }
            }
        }
    }

    /**
     * Prints the usage of the Application
     */
    public void printMenu() {
        System.out.println("+----------------------------------------+");
        System.out.println("|       Usage of ChatClient-Modell       |");
        System.out.println("|    1 - Connect to (IP-v4) Address      |");
        System.out.println("|    2 - Send Message to (IP-v4) Address |");
        System.out.println("|    3 - Show Participants               |");
        System.out.println("|    q - quit the Program                |");
        System.out.println("|    h - print RoutingTable              |");
        System.out.println("+----------------------------------------+");
    }
}
