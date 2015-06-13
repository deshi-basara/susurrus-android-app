package rocks.susurrus.susurrus.chat.models;

import android.util.Log;

import java.io.Serializable;
import java.net.InetAddress;

/**
 * Data representation of one single text-message.
 */
public class MessageModel implements Serializable {
    static final String LOG_TAG = "MessageModel";

    /**
     * Attributes
     */
    public boolean owner;
    private InetAddress ownerAddress;
    private String ownerName;
    private String message;

    /**
     * Model constructor.
     * Initiates the model.
     * @param owner
     * @param ownerName
     * @param message
     */
    public MessageModel(boolean owner, String ownerName, String message) {
        super();
        this.owner = owner;
        this.ownerName = ownerName;
        this.message = message;
    }

    /**
     * Getter/Setter
     */
    public String getMessage() {
        return this.message;
    }
    public String getOwnerName() { return this.ownerName; }

    public Boolean isOwner() {
        Log.d(LOG_TAG, "isOwner: " + this.owner);

        return this.owner;
    }

    public void setOwnerAddress(InetAddress ip) {
        this.ownerAddress = ip;
    }
    public void setOwnership(boolean hasOwnership) {
        this.owner = hasOwnership;
    }
}
