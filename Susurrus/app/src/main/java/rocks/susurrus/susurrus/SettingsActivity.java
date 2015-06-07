package rocks.susurrus.susurrus;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import org.w3c.dom.Text;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Activity for viewing and manipulation persistent setting values
 * from the user's SharedPreferences.
 */
public class SettingsActivity extends ActionBarActivity {
    static final String LOG_TAG = "SettingsActivity";

    /**
     * Constants
     */
    static final String PREF_ID = "SUS_PREFS";
    static final String PREF_USER_NAME = "SUS_USER_NAME";
    static final String PREF_IMAGE = "SUS_USER_IMAGE";

    /**
     * Views
     */
    private CircleImageView userImageView;
    private EditText userNameInput;
    private TextView userNameInputError;

    /**
     * Data
     */
    private SharedPreferences settings;
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // add a backButton to the actionBar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setView();
        setPreferences();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings_done) {

            //@todo validate settings

            this.updatePreferences();
            this.goBack();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Sets all needed view-elements.
     */
    private void setView() {
        // find views
        this.userImageView = (CircleImageView) findViewById(R.id.settings_user_image);
        this.userNameInput = (EditText) findViewById(R.id.settings_user_name);
        this.userNameInputError = (TextView) findViewById(R.id.settings_user_name_error);
    }

    /**
     * Fetches all needed settings from the SharedPreferences-object.
     * Extracts saved settings and inserts them into the input-views.
     */
    private void setPreferences() {
        // get SharedPreferences
        this.settings = getSharedPreferences(PREF_ID, 0);

        // parse settings
        this.userName = settings.getString(PREF_USER_NAME, "Benutzername");

        // insert settings
        this.userNameInput.setText(this.userName);

        //@todo include all settings
    }

    /**
     * Fetches all entered data from the input-views and commits them back into the
     * SharedPreferences-object.
     */
    private void updatePreferences() {
        SharedPreferences.Editor settingsEditor = this.settings.edit();

        settingsEditor.putString(PREF_USER_NAME, userNameInput.getText().toString());

        settingsEditor.commit();
    }

    /**
     * Redirects user to the MainActivity.
     */
    private void goBack() {
        Intent mainIntent = new Intent(this, MainActivity.class);

        startActivity(mainIntent);
    }
}
