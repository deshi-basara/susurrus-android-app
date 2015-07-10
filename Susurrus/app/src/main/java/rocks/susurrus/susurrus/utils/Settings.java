package rocks.susurrus.susurrus.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.securepreferences.SecurePreferences;

import java.security.GeneralSecurityException;
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
    public static final String PREF_PLAIN_ID = "SUS_PREFS_PLAIN"; // unencrypted settings
    public static final String PREF_ID = "SUS_PREFS";
    public static final String PREF_USER_NAME = "SUS_USER_NAME";
    public static final String PREF_IMAGE = "SUS_USER_IMAGE";
    public static final String PREF_PUB_KEY = "SUS_PUB_KEY";
    public static final String PREF_PRIVATE_KEY = "SUS_PUB_PRIVATE";
    public static final String PREF_PLAIN_STARTED = "SUS_PLAIN_STARTED";

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
    private SharedPreferences settings;
    private String settingsPassword;

    /**
     * Constructor.
     *
     * @param settings
     * @param password
     */
    public Settings(SharedPreferences settings, String password) {
        this.settings = settings;
        this.settingsPassword = password;

        Log.d(LOG_TAG, "Settings Instance created");
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
     * Checks weather the app was started for the first time (and needs initialization).
     * @param _context Application context.
     * @return True, if first time.
     */
    public static boolean isFirstRun(Context _context) {
        // get unencrypted settings and check if PREF_PLAIN_STARTED was initialized yet
        SharedPreferences plainPrefs = _context.getSharedPreferences(PREF_PLAIN_ID,
                Context.MODE_PRIVATE);

        if(plainPrefs.getBoolean(PREF_PLAIN_STARTED, true)) {
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Checks if the handed password is valid for unlocking the encrypted settings.
     * @param _context Application context.
     * @param _password Entered password.
     * @return True, if correct.
     */
    public static boolean unlockSettings(Context _context, String _password) {

        // try to open locked settings and ask for a value
        SharedPreferences settings = new SecurePreferences(_context, _password, null);
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

    /**
     * Updates the current application password with a new password, if the old one is correct.
     * @param _context Application context calling the method.
     * @param _oldPassword Current application password.
     * @param _newPassword New application password.
     * @return True, if the update was successfully.
     */
    public static boolean changePassword(Context _context, String _oldPassword, String _newPassword) {
        SecurePreferences securePrefs = new SecurePreferences(_context, _oldPassword, null);
        try {
            securePrefs.handlePasswordChange(_newPassword, _context);
        } catch(GeneralSecurityException e) {
            e.printStackTrace();

            return false;
        }

        return true;
    }

    public void setPublicKey(PublicKey _publicKey) {
        // convert to string and save
        String publicString = Crypto.keyToString(_publicKey);

        this.settings.edit().putString(Settings.PREF_PUB_KEY, publicString).commit();

        Log.d(LOG_TAG, "New publicKey: " + publicString);
    }

    public void setPrivateKey(PrivateKey _privateKey) {
        // convert to string and save
        String privateString = Crypto.keyToString(_privateKey);

        this.settings.edit().putString(Settings.PREF_PRIVATE_KEY, privateString).commit();

        Log.d(LOG_TAG, "New privateKey: " + privateString);
    }

    /**
     * Returns the PublicKey saved in SharedPreferences.
     * @return
     * @throws RuntimeException
     */
    public PublicKey getPublicKey() throws RuntimeException {
        String publicKeyString = this.settings.getString(Settings.PREF_PUB_KEY, "empty");
        if(publicKeyString == null || publicKeyString.equals("empty")) {
            throw new RuntimeException("No PublicKey available");
        }

        Log.d(LOG_TAG, "publicKey: " + publicKeyString);
        PublicKey publicKey = Crypto.publicStringToKey(publicKeyString);

        return publicKey;
    }

    /**
     * Returns the PrivateKey saved in SharedPreferences.
     * @return
     * @throws RuntimeException
     */
    public PrivateKey getPrivateKey() throws RuntimeException {
        String privateKeyString = this.settings.getString(Settings.PREF_PRIVATE_KEY, "empty");
        if(privateKeyString == null || privateKeyString.equals("empty")) {
            throw new RuntimeException("No PrivateKey available");
        }

        Log.d(LOG_TAG, "privateKey: " + privateKeyString);
        PrivateKey privateKey = Crypto.privateStringToKey(privateKeyString);

        return privateKey;
    }
}
