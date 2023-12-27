package ahmed.daniel;

import java.util.Scanner;

public class UI {
    
    public final String CONNECT_TO = "1";
    public final String SEND_MESSAGE = "2";
    public final String SHOW_PARTICIPANTS = "3";
    public final String SHOW_HELP = "h";
    public final String QUIT_PROGRAM = "q";

    private final ChatClient chatClient;

    public UI(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public void mainLoop() {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;
        printMenu();

        while(running) {
            System.out.print("COMMAND: ");
            String input = scanner.nextLine().toLowerCase();
            System.out.println("User Input was:" + input);
            switch (input) {
                case CONNECT_TO: {
                    System.out.println("IPV4-ADDRESS: ");
                    String ipv4address = scanner.nextLine();
                    System.out.println("PORT: ");
                    int port = Integer.parseInt(scanner.nextLine());
                    chatClient.addNewConnection(ipv4address, port);
                    break;
                }
                case SEND_MESSAGE: {
                    System.out.print("MESSAGE: ");
                    String message = scanner.nextLine();
                    System.out.print("Sende eine Message an NAME: ");
                    String destinationName = scanner.nextLine();

                    chatClient.sendMessage(message, destinationName);
                    System.out.println("Sending Message to IPv4 Address ...");
                    System.out.println("Message to be send: " + message);
                    break;
                }
                case SHOW_PARTICIPANTS: {
                    System.out.println("Showing Participants ...");
                    chatClient.printActiveConnections();        //TODO: should show transitive Partners
                    break;
                }
                case QUIT_PROGRAM: {
                    System.out.println("Quitting Program ...");
                    chatClient.stopSocket();
                    chatClient.stopActiveConnections();
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

    public void printMenu() {
        System.out.println("+----------------------------------------+");
        System.out.println("|       Usage of ChatClient-Modell       |");
        System.out.println("|    1 - Connect to (IP-v4) Address      |");
        System.out.println("|    2 - Send Message to (IP-v4) Address |");
        System.out.println("|    3 - Show Participants               |");
        System.out.println("|    q - quit the Program                |");
        System.out.println("|    h - print the Usage                 |");
        System.out.println("+----------------------------------------+");
    }

}
