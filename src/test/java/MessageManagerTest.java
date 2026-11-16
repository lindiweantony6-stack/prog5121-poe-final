import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** PoE Part 3 tests, using the five test messages from the brief. */
class MessageManagerTest {

    @TempDir
    Path tempDir;

    private Message m1, m2, m3, m4, m5;

    @BeforeEach
    void setUp() {
        Message.setStorePath(tempDir.resolve("messages.json"));
        Message.clearAll();

        m1 = new Message("0011111111", 0, "+27834557896", "Did you get the cake?");
        m2 = new Message("0022222222", 1, "+27838884567", "Where are you? You are late! I have asked you to be on time.");
        m3 = new Message("0033333333", 2, "+27834484567", "Yohoooo, I am at your gate.");
        m4 = new Message("0838884567", 3, "0838884567", "It is dinner time!");
        m5 = new Message("0055555555", 4, "+27838884567", "Ok, I am leaving without you.");

        m1.sentMessage(Message.SEND);
        m2.sentMessage(Message.STORE);
        m3.sentMessage(Message.DISREGARD);
        m4.sentMessage(Message.SEND);
        m5.sentMessage(Message.STORE);
    }

    @AfterEach
    void tearDown() {
        // put the default file back so other test classes are not affected
        Message.setStorePath(Paths.get(Message.STORE_FILE));
        Message.clearAll();
    }

    private List<String> texts(List<Message> messages) {
        List<String> result = new ArrayList<>();
        for (Message m : messages) {
            result.add(m.getText());
        }
        return result;
    }

    @Test
    void sentMessagesArrayCorrectlyPopulated() {
        assertEquals(Arrays.asList("Did you get the cake?", "It is dinner time!"),
                texts(Message.getSentMessages()));
    }

    @Test
    void disregardedMessagesArrayCorrectlyPopulated() {
        assertEquals(Arrays.asList("Yohoooo, I am at your gate."), texts(Message.getDisregardedMessages()));
    }

    @Test
    void hashAndIdArraysPopulated() {
        assertEquals(5, Message.getMessageIds().size());
        assertEquals(5, Message.getMessageHashes().size());
        assertTrue(Message.getMessageIds().contains("0838884567"));
        assertTrue(Message.getMessageHashes().contains(m2.getMessageHash()));
    }

    @Test
    void storedMessagesReadFromJsonIntoArray() {
        Message.clearAll();             // empty the arrays
        Message.loadStoredMessages();   // read the JSON file back in
        List<Message> stored = Message.getStoredMessages();
        assertEquals(2, stored.size());
        assertEquals(m2.getText(), stored.get(0).getText());
        assertEquals(m5.getText(), stored.get(1).getText());
    }

    @Test
    void longestMessageDisplayed() {
        List<Message> messages1to4 = Arrays.asList(m1, m2, m3, m4);
        assertEquals("Where are you? You are late! I have asked you to be on time.",
                MessageManager.longestMessage(messages1to4));
    }

    @Test
    void searchByMessageId() {
        Message found = MessageManager.findByMessageId(Arrays.asList(m1, m2, m3, m4), "0838884567");
        assertNotNull(found);
        assertEquals("0838884567", found.getRecipient());
        assertEquals("It is dinner time!", found.getText());
    }

    @Test
    void searchByMessageIdNotFound() {
        assertNull(MessageManager.findByMessageId(Message.getStoredMessages(), "9999999999"));
    }

    @Test
    void searchByRecipient() {
        List<Message> sentAndStored = new ArrayList<>(Message.getSentMessages());
        sentAndStored.addAll(Message.getStoredMessages());
        List<Message> matches = MessageManager.findByRecipient(sentAndStored, "+27838884567");
        assertEquals(Arrays.asList(
                "Where are you? You are late! I have asked you to be on time.",
                "Ok, I am leaving without you."), texts(matches));
    }

    @Test
    void deleteMessageUsingHash() {
        String result = MessageManager.deleteByHash(Message.getStoredMessages(), m2.getMessageHash());
        assertEquals("Message: \"Where are you? You are late! I have asked you to be on time.\" successfully deleted.",
                result);
        assertEquals(1, Message.getStoredMessages().size());
    }

    @Test
    void deleteMessageHashNotFound() {
        assertEquals("No message found with hash 99:9:NOPE.",
                MessageManager.deleteByHash(Message.getStoredMessages(), "99:9:NOPE"));
    }

    @Test
    void reportShowsSentMessageDetails() {
        String report = MessageManager.buildReport(Message.getSentMessages());
        assertTrue(report.contains(m1.getMessageHash()));
        assertTrue(report.contains("+27834557896"));
        assertTrue(report.contains("Did you get the cake?"));
        assertTrue(report.contains(m4.getMessageHash()));
        assertTrue(report.contains("It is dinner time!"));
    }

    @Test
    void senderAndRecipientDisplayed() {
        String output = MessageManager.senderAndRecipient(Message.getStoredMessages(), "kyl_1");
        assertTrue(output.contains("Sender: kyl_1 | Recipient: +27838884567"));
    }
}
