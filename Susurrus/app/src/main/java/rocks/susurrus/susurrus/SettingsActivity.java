package rocks.susurrus.susurrus;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.securepreferences.SecurePreferences;

import org.w3c.dom.Text;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import rocks.susurrus.susurrus.services.WifiDirectService;
import rocks.susurrus.susurrus.utils.Crypto;
import rocks.susurrus.susurrus.utils.Settings;

/**
 * Activity for viewing and manipulation persistent setting values
 * from the user's SharedPreferences.
 */
public class SettingsActivity extends ActionBarActivity {
    static final String LOG_TAG = "SettingsActivity";

    /**
     * Views
     */
    private CircleImageView userImageView;
    private EditText userNameInput;
    private TextView userNameInputError;
    private RelativeLayout generatePubContainer;
    private RelativeLayout changePwContainer;


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
        this.generatePubContainer = (RelativeLayout) findViewById(R.id.settings_generate_container);
        this.changePwContainer = (RelativeLayout) findViewById(
                R.id.settings_change_password_container);

        // set listeners
        this.generatePubContainer.setOnClickListener(this.onGenerateListener);
        this.changePwContainer.setOnClickListener(this.onChangeListener);
    }

    /**
     * Fetches all needed settings from the SharedPreferences-object.
     * Extracts saved settings and inserts them into the input-views.
     */
    private void setPreferences() {
        // get SharedPreferences
        //this.settings = getSharedPreferences(PREF_ID, 0);
        this.settings = new SecurePreferences(getApplicationContext(), "userpassword", null);

        // parse settings
        this.userName = settings.getString(Settings.PREF_USER_NAME, "Benutzername");

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

        settingsEditor.putString(Settings.PREF_USER_NAME, userNameInput.getText().toString());

        settingsEditor.commit();
    }

    /**
     * Redirects user to the MainActivity.
     */
    private void goBack() {
        Intent mainIntent = new Intent(this, MainActivity.class);

        startActivity(mainIntent);
    }

    /**
     * Opens a new Dialog for entering a new application password.
     * The new password will be validated and a password change request will be sent.
     * @param _oldPassword Current password.
     */
    private void changePasswordDialog(final String _oldPassword) {
        new MaterialDialog.Builder(SettingsActivity.this)
                .title(R.string.settings_change_password_dialog_title_new)
                .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)
                .input(R.string.settings_change_password_dialog_hint_new, 0,
                        new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(MaterialDialog dialog, CharSequence input) {
                                String newPassword = input.toString();

                                if (newPassword.isEmpty()) {
                                    return;
                                } else {
                                    boolean updated = Settings.changePassword(
                                            getApplicationContext(),
                                            _oldPassword,
                                            newPassword
                                    );

                                    Log.d(LOG_TAG, "updated: " + updated);
                                    //@todo give update feedback
                                }
                            }
                        })
                .negativeText(R.string.main_dialog_cancel)
                .show();
    }

    /**
     * OnClick: R.id.settings_generate_container.
     * Generates a new Private-/Public-Key.
     */
    private View.OnClickListener onGenerateListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // show generation-feedback
            final MaterialDialog generateDialog = new MaterialDialog.Builder(SettingsActivity.this)
                    .title(R.string.settings_generate_key_title)
                    .content(R.string.settings_generate_key_content)
                    .progress(true, 0)
                    .show();

            Log.d(LOG_TAG, "currentPub: " + settings.getString(Settings.PREF_PUB_KEY, "NoPubKey"));

            // start generation
            ArrayList generatedKeys = Crypto.generateKeys();
            PrivateKey privateKey = (PrivateKey) generatedKeys.get(0);
            PublicKey publicKey = (PublicKey) generatedKeys.get(1);

            // valid keys generated?
            if(privateKey != null && publicKey != null) {
                // save keys as string
                String privateString = Crypto.keyToString(privateKey);
                String publicString = Crypto.keyToString(publicKey);

                // save as preferences
                SharedPreferences.Editor settingsEditor = settings.edit();
                settingsEditor.putString(Settings.PREF_PRIVATE_KEY, privateString);
                settingsEditor.putString(Settings.PREF_PUB_KEY, publicString);

                settingsEditor.commit();

                /*Log.d(LOG_TAG, "before: " + privateString);
                PrivateKey test = Crypto.privateStringToKey(privateString);
                Log.d(LOG_TAG, "after: " + Crypto.keyToString(test));*/
            }
            else {
                Log.e(LOG_TAG, "No valid keys generated");
            }

            generateDialog.setContent(R.string.settings_generate_key_done);

            // s low down ui-feedback
            Handler uiHandler = new Handler();
            uiHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    generateDialog.hide();
                }
            }, 2000);
        }
    };

    /**
     * OnClick: R.id.settings_change_password_container.
     * Changes the application-password, if the user enters the current password.
     */
    private View.OnClickListener onChangeListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // start generation
            new MaterialDialog.Builder(SettingsActivity.this)
                    .iconRes(R.drawable.lock_portrait_512)
                    .limitIconToDefaultSize()
                    .title(R.string.settings_change_password_dialog_title)
                    .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)
                    .input(R.string.main_dialog_password_hint, 0, new MaterialDialog.InputCallback() {
                        @Override
                        public void onInput(MaterialDialog dialog, CharSequence input) {
                            String enteredPassword = input.toString();

                            // is the password valid?
                            boolean unlocked = Settings.unlockSettings(
                                    getApplicationContext(),
                                    enteredPassword
                            );

                            Log.d(LOG_TAG, "unlocked: " + unlocked);

                            if(unlocked) {
                                changePasswordDialog(enteredPassword);
                            }
                            else {
                                //dialog.setContent(R.string.main_dialog_password_wrong);
                                //@todo Wrong password handling

                                return;
                            }
                        }
                    })
                    .negativeText(R.string.main_dialog_cancel)
                    .show();
        }
    };
}
