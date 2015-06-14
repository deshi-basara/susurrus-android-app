package rocks.susurrus.susurrus.models;

import java.io.Serializable;

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
    private boolean authenticated = false;

    /**
     * Constructor.
     * Sets needed model attributes.
     * @param roomPassword
     * @param publicString
     */
    public AuthModel(String roomPassword, String publicString) {
        this.roomPassword = roomPassword;
        this.publicString = publicString;
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
    public boolean getAuthenticationStatus() {
        return this.authenticated;
    }

    public void setAuthenticationStatus(boolean status) {
        this.authenticated = status;
    }

}
