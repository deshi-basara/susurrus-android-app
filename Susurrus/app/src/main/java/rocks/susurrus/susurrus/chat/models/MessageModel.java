package rocks.susurrus.susurrus.chat.models;

/**
 * Data representation of one single text-message.
 */
public class MessageModel {
    public boolean owner;
    public String message;

    public MessageModel(boolean owner, String message) {
        super();
        this.owner = owner;
        this.message = message;
    }
}
