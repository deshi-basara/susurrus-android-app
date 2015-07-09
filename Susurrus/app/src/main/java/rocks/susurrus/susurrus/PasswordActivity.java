package rocks.susurrus.susurrus;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import rocks.susurrus.susurrus.services.WifiDirectService;
import rocks.susurrus.susurrus.utils.Settings;


public class PasswordActivity extends ActionBarActivity {
    static final String LOG_TAG = "PasswordActivity";

    /**
     * Views
     */
    EditText passwordInput;
    TextView passwordError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);

        setView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_password, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_password_done) {

            // did the user enter a valid password?
            validatePassword();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Sets all needed views.
     */
    private void setView() {
        this.passwordInput = (EditText) findViewById(R.id.password_input);
        this.passwordError = (TextView) findViewById(R.id.password_input_error);
    }

    /**
     * Validates the entered password form the passwordInput-EditText.
     * If the unlocking of the SecuredPreferences was successfully the MainActivity is opened.
     * Otherwise an error-message will be shown.
     */
    private void validatePassword() {

        // show feedback dialog
        final MaterialDialog passwordDialog = new MaterialDialog.Builder(this)
                .content(R.string.password_dialog_content)
                .progress(true, 0)
                .show();

        // fetch entered password and try to unlock the settings
        String enteredPassword = this.passwordInput.getText().toString().trim();
        boolean unlocked = Settings.unlockSettings(getApplicationContext(), enteredPassword);

        Handler uiHandler = new Handler();
        if(unlocked) {
            final Intent mainIntent = new Intent(this, MainActivity.class);

            // wait 500ms before updating the feedback
            uiHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    passwordDialog.hide();
                    startActivity(mainIntent);
                }
            }, 1000);
        }
        else {

            // wait 500ms before updating the feedback
            uiHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    passwordDialog.hide();
                    passwordError.setVisibility(View.VISIBLE);
                }
            }, 1000);
        }
    }
}
