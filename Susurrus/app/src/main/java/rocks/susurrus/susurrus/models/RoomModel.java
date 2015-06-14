package rocks.susurrus.susurrus.models;

import android.util.Log;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import rocks.susurrus.susurrus.services.WifiDirectService;

/**
 * Data representation of one chat-room.
 */
public class RoomModel implements Serializable {
    final static String LOG_TAG = "RoomModel";

    /**
     * Networking
     */
    private String ownerName;
    private String ownerAddr;

    /**
     * Attributes
     */
    private String roomName;
    private String roomCategory;
    private String roomImage;
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
     * @param roomName
     * @param roomCategory
     * @param roomImage
     * @param roomEncrypted
     */
    public RoomModel(String ownerName, String roomName, String roomCategory,
                     String roomImage, boolean roomEncrypted) {
        this.ownerName = ownerName;
        this.roomName = roomName;
        this.roomCategory = roomCategory;
        this.roomImage = roomImage;
        this.roomEncrypted = roomEncrypted;

        Log.d(LOG_TAG, "New room added: " + this.roomName);
    }

    /**
     * Getter/Setter
     */
    public void setPassword(String pass) {
        this.roomPassword = pass;
    }
    public void setRoomMembers(int count) {
        this.roomMembers = count;
    }
    public void setOwnerAddr(String addr) {
        this.ownerAddr = addr;
    }

    public String getOwnerName() {
        return this.ownerName;
    }
    public String getRoomPassword() {
        return this.roomPassword;
    }
    public String getOwnerAddr() { return this.ownerAddr; }
    public String getRoomName() {
        return this.roomName;
    }
    public String getRoomCategory() {
        return this.roomName;
    }

    public boolean hasEncryption() {
        return this.roomEncrypted;
    }

    /**
     * Converts our model to an HashMap (without password).
     * @return Model attributes as HashMap.
     */
    public Map toHashMap() {
        Map roomMap = new HashMap();
        roomMap.put("port", String.valueOf(WifiDirectService.SERVICE_PORT));
        roomMap.put("user_name", this.ownerName);
        roomMap.put("room_name", this.roomName);
        roomMap.put("room_category", this.roomCategory);
        roomMap.put("room_private", String.valueOf(roomEncrypted));
        roomMap.put("room_image", this.roomImage);
        roomMap.put("available", "visible");

        return roomMap;
    }
}
