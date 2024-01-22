import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ahmed.daniel.ChatClient;
import org.awaitility.Durations;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

import org.awaitility.Awaitility.*;

public class ChatClientTest {


    private final PrintStream standardOut = System.out;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();


    ChatClient chatClient1;
    ChatClient chatClient2;

    @BeforeEach
    void setup() throws IOException {
        chatClient1 = new ChatClient(InetAddress.getByName("localhost"), 8080, "DAN");
        chatClient2 = new ChatClient(InetAddress.getByName("localhost"), 8090, "AHM");
        chatClient1.startClient();
        chatClient2.startClient();

        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @AfterEach
    void cleanUp() {
        chatClient1.disconnect();

        chatClient2.disconnect();

        System.setOut(standardOut);
    }

    @Test
    void testOneConnection() {
        chatClient1.addNewConnection("localhost", 8090);

        await().atMost(5, TimeUnit.SECONDS).until(() -> chatClient1.getActiveConnectionNames().size() == 1);

        assertEquals(1, chatClient1.getActiveConnections().size());
        assertEquals(1, chatClient2.getActiveConnections().size());

    }

    @Test
    void testNameExchange() {
        chatClient1.addNewConnection("localhost", 8090);

        await().atMost(5, TimeUnit.SECONDS).until(() -> chatClient1.getActiveConnectionNames().size() == 1
                && chatClient2.getActiveConnectionNames().size() == 1);

        assertTrue(chatClient1.getActiveConnectionNames().contains("AHM"));
        assertTrue(chatClient2.getActiveConnectionNames().contains("DAN"));
    }


    /**
     * This method tests, if the send Message is the same on the receiving end
     */
    @Test
    void testSendMessage() {
        String textMessage = "secretMessage";
        chatClient1.addNewConnection("localhost", 8090);
        if (chatClient1.getActiveConnectionNames().size() == 1) {}

        await().atMost(5, TimeUnit.SECONDS).until(() -> chatClient1.getActiveConnectionNames().size() == 1
                && chatClient2.getActiveConnectionNames().size() == 1);

        chatClient1.sendMessage(textMessage, "AHM");

        await().atMost(5, TimeUnit.SECONDS).until(() -> !outputStreamCaptor.toString().trim().isEmpty());

        assertEquals("DAN: " + textMessage, outputStreamCaptor.toString().trim());
    }

    //TODO: test if the TTL-Count was decremented






}
