package rocks.susurrus.susurrus.models;

import android.util.Log;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import rocks.susurrus.susurrus.network.WifiDirectLocalService;

/**
 * Data representation of one chat-room.
 */
public class RoomModel {
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
     * @param ownerAddr
     * @param roomName
     * @param roomCategory
     * @param roomImage
     * @param roomEncrypted
     */
    public RoomModel(String ownerName, String ownerAddr, String roomName, String roomCategory,
                     String roomImage, boolean roomEncrypted) {
        this.ownerName = ownerName;
        this.ownerAddr = ownerAddr;
        this.roomName = roomName;
        this.roomCategory = roomCategory;
        this.roomImage = roomImage;
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
    public String getOwnerAddr() { return this.ownerAddr; }
    public String getRoomName() {
        return this.roomName;
    }
    public String getRoomCategory() {
        return this.roomName;
    }

    /**
     * Converts our model to an HashMap (without password).
     * @return Model attributes as HashMap.
     */
    public Map toHashMap() {
        Map roomMap = new HashMap();
        roomMap.put("port", String.valueOf(WifiDirectLocalService.SERVICE_PORT));
        roomMap.put("user_name", "OWNER" + (int) (Math.random() * 1000));
        roomMap.put("room_name", this.roomName);
        roomMap.put("room_category", this.roomCategory);
        roomMap.put("room_private", String.valueOf(roomEncrypted));
        roomMap.put("room_image", this.roomImage);
        roomMap.put("available", "visible");

        return roomMap;
    }

    /**
     * Returns if the room is encrypted.
     * @return True, encrypted
     */
    public boolean isRoomEncrypted() {
        return this.roomEncrypted;
    }
}
