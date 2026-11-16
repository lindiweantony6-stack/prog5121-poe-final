import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Parts 2 and 3: a single chat message plus the session-wide arrays
 * (sent, disregarded, stored, message hashes and message IDs).
 */
public class Message {

    public static final int MAX_LENGTH = 250;
    public static final String STORE_FILE = "messages.json";

    // Menu choices for SentMessage()
    public static final int SEND = 1;
    public static final int DISREGARD = 2;
    public static final int STORE = 3;

    private static final Random RANDOM = new Random();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // ---- Part 3 arrays: filled while the program runs, never hard-coded ----
    private static final List<Message> sentMessages = new ArrayList<>();
    private static final List<Message> disregardedMessages = new ArrayList<>();
    private static final List<Message> storedMessages = new ArrayList<>();
    private static final List<String> messageHashes = new ArrayList<>();
    private static final List<String> messageIds = new ArrayList<>();

    // Where stored messages are saved (tests point this at a temporary file)
    private static Path storePath = Paths.get(STORE_FILE);

    private final String messageId;
    private final int messageNumber;
    private final String recipient;
    private final String text;
    private final String messageHash;

    /** Normal constructor: the ID is randomly generated. */
    public Message(int messageNumber, String recipient, String text) {
        this(generateMessageId(), messageNumber, recipient, text);
    }

    /** Constructor that accepts an ID so that tests are repeatable. */
    public Message(String messageId, int messageNumber, String recipient, String text) {
        this.messageId = messageId;
        this.messageNumber = messageNumber;
        this.recipient = recipient;
        this.text = text;
        this.messageHash = createMessageHash();
    }

    /** Random ten-digit number, kept as text so leading zeros survive. */
    public static String generateMessageId() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            sb.append(RANDOM.nextInt(10));
        }
        return sb.toString();
    }

    /** The message ID must be no more than ten characters. */
    public boolean checkMessageID() {
        return messageId != null && messageId.length() <= 10;
    }

    /** Reuses the Part 1 cell-number check. */
    public String checkRecipientCell() {
        return checkRecipientCell(recipient);
    }

    public static String checkRecipientCell(String cell) {
        if (new Login().checkCellPhoneNumber(cell)) {
            return "Cell phone number successfully captured.";
        }
        return "Cell phone number is incorrectly formatted or does not contain an international code. Please correct the number and try again.";
    }

    /** Returns "Message ready to send." or how far over the limit the message is. */
    public static String checkMessageLength(String text) {
        int length = text == null ? 0 : text.length();
        if (length <= MAX_LENGTH) {
            return "Message ready to send.";
        }
        return "Message exceeds " + MAX_LENGTH + " characters by " + (length - MAX_LENGTH)
                + "; please reduce the size.";
    }

    /** First two digits of ID : message number : FIRSTLAST word, all caps. */
    public String createMessageHash() {
        String[] words = text.trim().split("\\s+");
        String first = stripPunctuation(words[0]);
        String last = stripPunctuation(words[words.length - 1]);
        String prefix = messageId.length() >= 2 ? messageId.substring(0, 2) : messageId;
        return (prefix + ":" + messageNumber + ":" + first + last).toUpperCase();
    }

    private static String stripPunctuation(String word) {
        return word.replaceAll("[^A-Za-z0-9]", "");
    }

    /**
     * Acts on the user's choice, fills the Part 3 arrays and returns the matching message.
     * 1 = send, 2 = disregard, 3 = store.
     */
    public String sentMessage(int choice) {
        if (choice != SEND && choice != DISREGARD && choice != STORE) {
            return "Invalid choice.";
        }
        // every message that goes through the menu is tracked by ID and hash
        messageIds.add(messageId);
        messageHashes.add(messageHash);

        switch (choice) {
            case SEND:
                sentMessages.add(this);
                return "Message successfully sent.";
            case DISREGARD:
                disregardedMessages.add(this);
                return "Press 0 to delete the message.";
            default:
                storedMessages.add(this);
                storeMessage();
                return "Message successfully stored.";
        }
    }

    /** Full details of this message, in the order required by the brief. */
    public String details() {
        return "Message ID: " + messageId + "\n"
                + "Message Hash: " + messageHash + "\n"
                + "Recipient: " + recipient + "\n"
                + "Message: " + text;
    }

    /** Returns details of all messages sent while the program has been running. */
    public static String printMessages() {
        StringBuilder sb = new StringBuilder();
        for (Message m : sentMessages) {
            sb.append(m.details()).append("\n\n");
        }
        return sb.toString().trim();
    }

    public static int returnTotalMessages() {
        return sentMessages.size();
    }

    /** Test helper: forget the messages sent so far. */
    public static void clearSentMessages() {
        sentMessages.clear();
    }

    /** Test helper (Part 3): empty every array. */
    public static void clearAll() {
        sentMessages.clear();
        disregardedMessages.clear();
        storedMessages.clear();
        messageHashes.clear();
        messageIds.clear();
    }

    /** Appends this message to the JSON file. */
    public void storeMessage() {
        storeMessage(storePath);
    }

    /**
     * Stores this message in a JSON file using the Gson library.
     * Adapted from the Gson User Guide (pretty printing and toJson).
     * Reference: Google, n.d. Gson User Guide.
     * Available at: https://github.com/google/gson/blob/main/UserGuide.md (Accessed: 19 October 2026).
     */
    public void storeMessage(Path file) {
        List<Message> all = readStoredMessages(file);
        all.add(this);
        try (Writer w = Files.newBufferedWriter(file)) {
            GSON.toJson(all, w);
        } catch (IOException e) {
            System.out.println("Could not store message: " + e.getMessage());
        }
    }

    /**
     * Reads the stored messages back from the JSON file using the Gson library.
     * Adapted from the Gson User Guide (fromJson and TypeToken for a list).
     * Reference: Google, n.d. Gson User Guide.
     * Available at: https://github.com/google/gson/blob/main/UserGuide.md (Accessed: 19 October 2026).
     */
    public static List<Message> readStoredMessages(Path file) {
        if (!Files.exists(file)) {
            return new ArrayList<>();
        }
        try (Reader r = Files.newBufferedReader(file)) {
            List<Message> list = GSON.fromJson(r, new TypeToken<List<Message>>() { }.getType());
            return list == null ? new ArrayList<>() : list;
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    /**
     * Part 3: reads the JSON file into the stored-messages array.
     * Call once when the application starts.
     * Uses readStoredMessages(), adapted from the Gson User Guide (Google, n.d.).
     */
    public static void loadStoredMessages() {
        storedMessages.clear();
        storedMessages.addAll(readStoredMessages(storePath));
        for (Message m : storedMessages) {
            if (!messageIds.contains(m.messageId)) {
                messageIds.add(m.messageId);
                messageHashes.add(m.messageHash);
            }
        }
    }

    /**
     * Part 3: writes the whole stored-messages array back to the JSON file
     * (used after a message is deleted so the file matches the array).
     * Adapted from the Gson User Guide (Google, n.d.).
     */
    public static void saveStoredMessages() {
        try (Writer w = Files.newBufferedWriter(storePath)) {
            GSON.toJson(storedMessages, w);
        } catch (IOException e) {
            System.out.println("Could not save stored messages: " + e.getMessage());
        }
    }

    // ---- array access (Part 3) ----
    public static List<Message> getSentMessages() { return sentMessages; }
    public static List<Message> getDisregardedMessages() { return disregardedMessages; }
    public static List<Message> getStoredMessages() { return storedMessages; }
    public static List<String> getMessageHashes() { return messageHashes; }
    public static List<String> getMessageIds() { return messageIds; }

    /** Test helper: change where stored messages are saved. */
    public static void setStorePath(Path path) { storePath = path; }

    public String getMessageId() { return messageId; }
    public int getMessageNumber() { return messageNumber; }
    public String getRecipient() { return recipient; }
    public String getText() { return text; }
    public String getMessageHash() { return messageHash; }

    /** The text shown to the user when the ID is created. */
    public String getMessageIdMessage() {
        return "Message ID generated: " + messageId;
    }
}
