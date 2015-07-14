package rocks.susurrus.susurrus.models;

import android.util.Log;

import java.io.Serializable;
import java.security.PublicKey;

/**
 * Created by simon on 14.06.15.
 */
public class AuthModel implements Serializable {
    final static String LOG_TAG = "RoomModel";

    /**
     * Attributes
     */
    private String roomPassword;
    private String publicString;
    private String masterPublicString;
    private boolean authenticated = false;

    /**
     * Constructor.
     * Sets needed model attributes.
     * @param _roomPassword
     * @param _publicString
     */
    public AuthModel(String _roomPassword, String _publicString) {
        this.roomPassword = _roomPassword;
        this.publicString = _publicString;

        Log.d(LOG_TAG, "Auth-Model: " + this.publicString);
    }

    /**
     * Getter/Setter
     */
    public String getRoomPassword() {
        return this.roomPassword;
    }
    public String getPublicString() {
        return this.publicString;
    }
    public String getMasterPublicString() {
        return this.masterPublicString;
    }
    public boolean getAuthenticationStatus() {
        return this.authenticated;
    }

    public void setAuthenticationStatus(boolean status) {
        this.authenticated = status;
    }
    public void setMasterPublicString(String _masterPublicString) {
        this.masterPublicString = _masterPublicString;
    }
}
