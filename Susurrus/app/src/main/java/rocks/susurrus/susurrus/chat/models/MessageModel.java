package rocks.susurrus.susurrus.chat.models;

import android.media.Image;
import android.net.Uri;
import android.util.Log;

import java.io.Serializable;
import java.net.InetAddress;

/**
 * Data representation of one single text-message.
 */
public class MessageModel implements Serializable {
    static final String LOG_TAG = "MessageModel";

    /**
     * Constants
     */
    public static final int TEXT_MESSAGE = 1;
    public static final int IMAGE_MESSAGE = 2;
    public static final int VIDEO_MESSAGE = 3;
    public static final int AUDIO_MESSAGE = 4;
    public static final int FILE_MESSAGE = 5;

    /**
     * Attributes
     */
    public boolean owner;
    private InetAddress ownerAddress;
    private String ownerName;
    private int messageType;

    /**
     * Optional Attributes
     */
    private String message;
    private byte[] stream = null;
    private int streamLength = 0;

    /**
     * Model constructor.
     * Initiates the model.
     * @param owner
     * @param ownerName
     * @param type
     */
    public MessageModel(boolean owner, String ownerName, int type) {
        super();
        this.owner = owner;
        this.ownerName = ownerName;
        this.messageType = type;
    }

    /**
     * Getter/Setter
     */
    public String getMessage() {
        return this.message;
    }
    public String getOwnerName() { return this.ownerName; }
    public InetAddress getOwnerAddress() {
        return this.ownerAddress;
    }
    public int getMessageType() {
        return this.messageType;
    }

    public Boolean isOwner() {
        return this.owner;
    }

    public void setMessage(String messageText) {
        this.message = messageText;
    }
    public void setOwnerAddress(InetAddress ip) {
        this.ownerAddress = ip;
    }
    public void setOwnership(boolean hasOwnership) {
        this.owner = hasOwnership;
    }
    public void setStream(byte[] stream, int length) {
        this.stream = stream;
        this.streamLength = length;
    }

}
