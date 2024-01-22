package ahmed.daniel;
import java.io.IOException;
import java.net.InetAddress;


/**
 * Main Class from which the Application is started
 */
public class Main {
    private static final byte ARGS_IP_INDEX = 0;
    private static final byte ARGS_PORT_INDEX = 1;
    private static final byte ARGS_NAME_INDEX = 2;
    public static void main(String[] args) {
        if(args[ARGS_NAME_INDEX].equals(ProtocolConstants.DESTINATION_NETWORK_NAME_NOT_SET)) {
            System.out.println("You cannot enter that name, please try another one");
            return;
        }
        try {
            ChatClient client = new ChatClient(InetAddress.getByName(args[ARGS_IP_INDEX]), Integer.parseInt(args[ARGS_PORT_INDEX]), args[ARGS_NAME_INDEX]);
            UI ui = new UI(client);
            client.startClient();
            ui.mainLoop();
        } catch (IOException ioException) {
            System.err.println("The entered IP-Address or Port are not valid, try again");
        }
    }
}
