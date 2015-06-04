package rocks.susurrus.susurrus.models;

import android.util.Log;

import java.net.InetAddress;

/**
 * Data representation of one chat-room.
 */
public class RoomModel {
    final static String LOG_TAG = "RoomModel";

    /**
     * Networking
     */
    private String ownerName;
    private InetAddress ownerAddr;

    /**
     * Attributes
     */
    private String roomName;
    private String roomCategory;
    private boolean roomEncrypted;
    private int roomMembers;

    /**
     * Encryption
     */
    private String roomPassword;

    /**
     * Constructor.
     * Sets model attributes.
     * @param ownerName
     * @param ownerAddr
     * @param roomName
     * @param roomCategory
     * @param roomEncrypted
     */
    public RoomModel(String ownerName, InetAddress ownerAddr, String roomName, String roomCategory,
                     boolean roomEncrypted) {
        this.ownerName = ownerName;
        this.ownerAddr = ownerAddr;
        this.roomName = roomName;
        this.roomCategory = roomCategory;
        this.roomEncrypted = roomEncrypted;

        Log.d(LOG_TAG, "New room added: " + this.roomName);
    }

    public void setPassword(String pass) {
        this.roomPassword = pass;
    }
    public void setRoomMembers(int count) {
        this.roomMembers = count;
    }

    public String getOwnerName() {
        return this.ownerName;
    }
    public String getRoomName() {
        return this.roomName;
    }
    public String getRoomCategory() {
        return this.roomName;
    }
    public boolean isRoomEncrypted() {
        return this.roomEncrypted;
    }

}
