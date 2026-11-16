import java.util.ArrayList;
import java.util.List;

/**
 * Part 3: search, report and delete operations on the message arrays.
 * Every method takes the list to work on, which keeps them easy to unit test.
 */
public class MessageManager {

    /** a) Sender and recipient of every message in the list. */
    public static String senderAndRecipient(List<Message> messages, String sender) {
        if (messages.isEmpty()) {
            return "There are no stored messages.";
        }
        StringBuilder sb = new StringBuilder();
        for (Message m : messages) {
            sb.append("Sender: ").append(sender)
              .append(" | Recipient: ").append(m.getRecipient()).append("\n");
        }
        return sb.toString();
    }

    /** b) The longest message text in the list ("" if the list is empty). */
    public static String longestMessage(List<Message> messages) {
        String longest = "";
        for (Message m : messages) {
            if (m.getText().length() > longest.length()) {
                longest = m.getText();
            }
        }
        return longest;
    }

    /** c) Finds a message by its ID (null if not found). */
    public static Message findByMessageId(List<Message> messages, String messageId) {
        for (Message m : messages) {
            if (m.getMessageId().equals(messageId)) {
                return m;
            }
        }
        return null;
    }

    /** d) All messages for one recipient. */
    public static List<Message> findByRecipient(List<Message> messages, String recipient) {
        List<Message> matches = new ArrayList<>();
        for (Message m : messages) {
            if (m.getRecipient().equals(recipient)) {
                matches.add(m);
            }
        }
        return matches;
    }

    /** e) Deletes the message with the given hash from the list. */
    public static String deleteByHash(List<Message> messages, String hash) {
        for (int i = 0; i < messages.size(); i++) {
            if (messages.get(i).getMessageHash().equalsIgnoreCase(hash)) {
                String text = messages.get(i).getText();
                messages.remove(i);
                return "Message: \"" + text + "\" successfully deleted.";
            }
        }
        return "No message found with hash " + hash + ".";
    }

    /** f) Full report: ID, hash, recipient and message for every message in the list. */
    public static String buildReport(List<Message> messages) {
        if (messages.isEmpty()) {
            return "There are no messages to report.";
        }
        StringBuilder sb = new StringBuilder();
        int count = 1;
        for (Message m : messages) {
            sb.append("--- Message ").append(count++).append(" ---\n")
              .append(m.details()).append("\n");
        }
        return sb.toString();
    }
}
