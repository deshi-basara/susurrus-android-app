package rocks.susurrus.susurrus.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.securepreferences.SecurePreferences;

import java.security.GeneralSecurityException;

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
     * Singleton
     */
    private static Settings singleInstance;

    /**
     * Data
     */
    Context context;
    SharedPreferences settings;

    /**
     * Maintains a static reference to the lone singleton instance and returns the reference from.
     * @return Settings instance
     */
    public static Settings getInstance() {
        // is there already an instance of the class?
        if(singleInstance == null) {
            // no instance, create one
            singleInstance = new Settings();
        }

        return singleInstance;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public boolean unlock(String password) {
        this.settings = new SecurePreferences(this.context, password, null);

        return true;
    }
}
