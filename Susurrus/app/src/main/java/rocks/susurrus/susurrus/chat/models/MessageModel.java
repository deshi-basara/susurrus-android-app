package rocks.susurrus.susurrus.chat.models;

import java.net.InetAddress;

/**
 * Data representation of one single text-message.
 */
public class MessageModel {
    public boolean owner;
    private InetAddress ownerAddress;
    public String message;

    public MessageModel(boolean owner, String message) {
        super();
        this.owner = owner;
        this.message = message;
    }

    public Boolean isOwner() {
        return this.owner;
    }

    public void setOwnerAddress(InetAddress ip) {
        this.ownerAddress = ip;
    }
}
