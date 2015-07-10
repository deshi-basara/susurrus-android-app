package rocks.susurrus.susurrus.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.securepreferences.SecurePreferences;

import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * Created by simon on 14.06.15.
 */
public class Settings {
    static final String LOG_TAG = "SettingsActivity";

    /**
     * Constants
     */
    public static final String PREF_ID = "SUS_PREFS";
    public static final String PREF_USER_NAME = "SUS_USER_NAME";
    public static final String PREF_IMAGE = "SUS_USER_IMAGE";
    public static final String PREF_PUB_KEY = "SUS_PUB_KEY";
    public static final String PREF_PRIVATE_KEY = "SUS_PUB_PRIVATE";

    /**
     * Exceptions
     */
    private static final String NO_PASSWORD_EXCEPTION = "No password entered";
    private static final String INVALID_PASSWORD_EXCEPTION = "Entered password is not valid";

    /**
     * Singleton
     */
    private static Settings singleInstance;

    /**
     * Data
     */
    private SecurePreferences settings;
    private String settingsPassword;

    /**
     * Constructor.
     *
     * @param settings
     * @param password
     */
    public Settings(SecurePreferences settings, String password) {
        this.settings = settings;
        this.settingsPassword = password;
    }

    /**
     * Maintains a static reference to the lone singleton instance and returns the reference from.
     * @return Settings instance
     */
    public static Settings getInstance() throws RuntimeException {
        // is there already an instance of the class?
        if(singleInstance == null) {
            return null;
        }

        return singleInstance;
    }

    /**
     * Checks if the handed password is valid for unlocking the encrypted settings.
     * @param _context Application context.
     * @param _password Entered password.
     * @return True, if correct.
     */
    public static boolean unlockSettings(Context _context, String _password) {

        // try to open locked settings and ask for a value
        SecurePreferences settings = new SecurePreferences(_context, _password, null);
        if(settings.getString(Settings.PREF_USER_NAME, "default") == null) {
            // value not available, password has to be invalid
            return false;
        }
        else {
            // valid password, create an instance
            singleInstance = new Settings(settings, _password);

            return true;
        }
    }

    public PublicKey getPublicKey() throws RuntimeException {
        String publicKeyString = this.settings.getString(Settings.PREF_PUB_KEY, "empty");
        if(publicKeyString.equals("empty")) {
            throw new RuntimeException("No PublicKey available");
        }

        Log.d(LOG_TAG, "publicKey: " + publicKeyString);
        PublicKey publicKey = Crypto.publicStringToKey(publicKeyString);

        return publicKey;
    }

    public PrivateKey getPrivateKey() throws RuntimeException {
        String privateKeyString = this.settings.getString(Settings.PREF_PRIVATE_KEY, "empty");
        if(privateKeyString.equals("empty")) {
            throw new RuntimeException("No PrivateKey available");
        }

        Log.d(LOG_TAG, "privateKey: " + privateKeyString);
        PrivateKey privateKey = Crypto.privateStringToKey(privateKeyString);

        return privateKey;
    }
}
