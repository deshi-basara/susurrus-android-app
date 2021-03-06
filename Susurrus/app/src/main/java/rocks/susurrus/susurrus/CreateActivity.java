package rocks.susurrus.susurrus;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.transitions.everywhere.TransitionManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.wrapp.floatlabelededittext.FloatLabeledEditText;

import rocks.susurrus.susurrus.models.RoomModel;
import rocks.susurrus.susurrus.services.WifiDirectService;
import rocks.susurrus.susurrus.utils.Settings;


public class CreateActivity extends ActionBarActivity {
    private static final String LOG_TAG = "CreateActivity";

    /**
     * Networking
     */
    private WifiDirectService wifiDirectService;
    private Intent wifiServiceIntent;
    private boolean isWifiDirectServiceBound;

    /**
     * Views
     */
    private ViewGroup startingScene;
    private EditText roomNameInput;
    private TextView roomNameInputError;
    private EditText roomCategoryInput;
    private TextView roomCategoryInputError;
    private RelativeLayout roomEncryptionActived;
    private RelativeLayout roomEncryptionDisabled;
    private TextView roomEncryptionActivatedText;
    private TextView roomEncryptionDisabledText;
    private EditText roomPassword;
    private TextView roomPasswordError;
    private FloatLabeledEditText roomPasswordContainer;
    private MaterialDialog createRoomDialog;

    /**
     * Data
     */
    private boolean roomEncrypted = false;
    private RoomModel roomData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);

        // add a backButton to the actionBar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setView();

        //registerWifiRoom();

        // start and bound the wifiDirectService
        this.wifiServiceIntent = new Intent(this, WifiDirectService.class);
        startService(this.wifiServiceIntent);
        bindService(this.wifiServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE);

        Log.d(LOG_TAG, "CreateActivity created.");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_create, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_room_done) {
            Log.d(LOG_TAG, "Room done");

            boolean everythingValid = validateWifiRoom();
            if(everythingValid) {
                // only create a room, if all input values were set
                registerWifiRoom();
            }

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    /**
     * Is executed, when the Activity is destroyed
     */
    protected void onDestroy() {
        super.onDestroy();

        // destroy running AsyncTasks (needed for lower Android-versions)
        if(this.wifiServiceIntent != null && this.isWifiDirectServiceBound) {
            Log.d(LOG_TAG, "Unbinding: wifiDirectService [ServiceConnection]");
            unbindService(serviceConnection);
        }

        Log.d(LOG_TAG, "CreateActivity destroyed.");
    }


    private void setView() {
        // get default layout
        startingScene = (ViewGroup) findViewById(R.id.create_room_container);

        // get needed views
        roomNameInput = (EditText) findViewById(R.id.create_room_name);
        roomNameInputError = (TextView) findViewById(R.id.create_room_name_error);
        roomCategoryInput = (EditText) findViewById(R.id.create_room_category);
        roomCategoryInputError = (TextView) findViewById(R.id.create_room_category_error);
        roomEncryptionActived = (RelativeLayout) findViewById(R.id.create_encryption_activated);
        roomEncryptionDisabled = (RelativeLayout) findViewById(R.id.create_encryption_disabled);
        roomEncryptionActivatedText = (TextView) findViewById(
                R.id.create_encryption_activated_text);
        roomEncryptionDisabledText = (TextView) findViewById(R.id.create_encryption_disabled_text);
        roomPassword = (EditText) findViewById(R.id.create_encryption_password);
        roomPasswordError = (TextView) findViewById(R.id.create_room_encryption_error);
        roomPasswordContainer = (FloatLabeledEditText) findViewById(
                R.id.create_encryption_password_container);

        // set events
        roomEncryptionActived.setOnClickListener(encryptionActivatedListener);
        roomEncryptionDisabled.setOnClickListener(encryptionDisabledListener);
     }

    /**
     * Validates if all needed input values were set.
     * Starts an error-feedback transition if not.
     * @return True, every input was valid.
     */
    private boolean validateWifiRoom() {
        int errorCounter = 0;

        // get form values
        String roomNameValue = roomNameInput.getText().toString().trim();
        String roomCategoryValue = roomCategoryInput.getText().toString().trim();

        // validate input
        if(roomNameValue.isEmpty()) {
            showError(roomNameInputError);
            errorCounter++;
        }
        else {
            hideError(roomNameInputError);
        }

        if(roomCategoryValue.isEmpty()) {
            showError(roomCategoryInputError);
            errorCounter++;
        }
        else {
            hideError(roomCategoryInputError);
        }

        if(roomCategoryValue.isEmpty()) {
            showError(roomCategoryInputError);
            errorCounter++;
        }
        else {
            hideError(roomCategoryInputError);
        }

        // if encryption was specified
        String roomPasswordValue = "";
        if(roomEncrypted) {
            roomPasswordValue = roomPassword.getText().toString().trim();

            if(roomPasswordValue.isEmpty()) {
                showError(roomPasswordError);
                errorCounter++;
            }
            else {
                hideError(roomPasswordError);
            }
        }

        // did we have errors?
        if(errorCounter > 0) {
            // there were errors
            return false;
        }
        else {
            // get the creator's username
            SharedPreferences settings = getSharedPreferences(Settings.PREF_ID, 0);
            String userName = settings.getString(Settings.PREF_USER_NAME, "Glenn Greenwald");

            // everything valid, create a new RoomModel
            //@todo real "ROOM_IMAGE"
            roomData = new RoomModel(userName, roomNameValue, roomCategoryValue, "ROOM_IMAGE",
                    roomEncrypted);
            if(roomEncrypted) {
                roomData.setPassword(roomPasswordValue);
            }

            return true;
        }
    }

    /**
     * Starts an error-transition for the handed TextView.
     * @param errorTextView TextView we want to show an error.
     */
    private void showError(TextView errorTextView) {
        TransitionManager.beginDelayedTransition(startingScene);
        errorTextView.setVisibility(View.VISIBLE);
    }

    /**
     * Undos an error-transition for the handed TextView.
     * @param errorTextView TextView we want to show an error.
     */
    private void hideError(TextView errorTextView) {
        TransitionManager.beginDelayedTransition(startingScene);
        errorTextView.setVisibility(View.INVISIBLE);
    }

    /**
     * Creates a hashMap with all entered room-data.
     * Hands the hashMap to the WifiDirectLocalService for registering the new room service.
     */
    private void registerWifiRoom() {
        showWifiRoomDialog();

        wifiDirectService.setupLocalService(this, roomData);
    }

    private void showWifiRoomDialog() {
        Log.d(LOG_TAG, "showWifiRoomDialog");

        createRoomDialog = new MaterialDialog.Builder(CreateActivity.this)
                .title(R.string.create_dialog_headline)
                .content(R.string.create_dialog_content)
                .progress(false, 100)
                //.negativeText(R.string.main_dialog_cancel)
                .show();
    }

    /**
     * Is executed by the wifiDirectService for notifying the ui of the room creation status.
     * @param creationState
     */
    public void registerWifiDialogUpdate(final int creationState) {

        // wait 1000ms berfore updating the feedback
        Handler uiHandler = new Handler();
        uiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                switch(creationState) {
                    case WifiDirectService.GROUP_CREATING:
                        createRoomDialog.setContent(R.string.create_dialog_group_creating);
                        createRoomDialog.incrementProgress(50);

                        break;
                    case WifiDirectService.GROUP_ERROR:
                        createRoomDialog.setContent(R.string.create_dialog_group_error);

                        break;
                    case WifiDirectService.GROUP_CREATED:
                        createRoomDialog.setContent(R.string.create_dialog_group_created);
                        createRoomDialog.incrementProgress(50);

                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                // open the newly created chat room
                                Intent chatIntent = new Intent(CreateActivity.this, ChatActivity.class);
                                chatIntent.putExtra("ROOM_NAME", roomData.getRoomName());
                                chatIntent.putExtra("ROOM_MODEL", roomData);

                                startActivity(chatIntent);
                            }
                        }, 2000);

                        break;
                }
            }
        }, 1000);
    }

    /**
     * OnClickListener: roomEncryptionActivated.
     * Executed if the user touches the "encryption-activated"-layout.
     */
    private View.OnClickListener encryptionActivatedListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            roomEncrypted = true;

            Log.d(LOG_TAG, "Room encrypted: " + String.valueOf(roomEncrypted));

            // mark element as 'active'
            TransitionManager.beginDelayedTransition(startingScene);
            roomEncryptionDisabled.setBackgroundColor(getResources().getColor(R.color.white));
            roomEncryptionDisabledText.setTextColor(getResources().getColor(R.color.textheadline));
            roomEncryptionActived.setBackgroundColor(getResources().getColor(R.color.darkblue));
            roomEncryptionActivatedText.setTextColor(getResources().getColor(R.color.white));

            // show password input
            roomPasswordContainer.setVisibility(View.VISIBLE);
        }
    };

    /**
     * OnClickListener: roomEncryptionDisabled.
     * Executed if the user touches the "encryption-disabled"-layout.
     */
    private View.OnClickListener encryptionDisabledListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            roomEncrypted = false;

            Log.d(LOG_TAG, "Room encrypted: " + String.valueOf(roomEncrypted));

            // mark element as 'active'
            TransitionManager.beginDelayedTransition(startingScene);
            roomEncryptionDisabled.setBackgroundColor(getResources().getColor(R.color.darkblue));
            roomEncryptionDisabledText.setTextColor(getResources().getColor(R.color.white));

            roomEncryptionActived.setBackgroundColor(getResources().getColor(R.color.white));
            roomEncryptionActivatedText.setTextColor(getResources().getColor(R.color.textheadline));

            // hide password input
            roomPasswordContainer.setVisibility(View.INVISIBLE);
        }
    };

    /**
     * Connection to the external wifiDirectService-process.
     */
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            WifiDirectService.InstanceBinder localBinder = (WifiDirectService.InstanceBinder) service;
            wifiDirectService = localBinder.getService();
            isWifiDirectServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isWifiDirectServiceBound = false;
        }
    };
}
