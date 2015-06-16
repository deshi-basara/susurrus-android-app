package rocks.susurrus.susurrus;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.afollestad.materialdialogs.MaterialDialog;
import com.skyfishjy.library.RippleBackground;

import rocks.susurrus.susurrus.adapters.RoomAdapter;
import rocks.susurrus.susurrus.models.AuthModel;
import rocks.susurrus.susurrus.models.RoomModel;
import rocks.susurrus.susurrus.network.WifiDirectBroadcastReceiver;
import rocks.susurrus.susurrus.network.WifiDisconnector;
import rocks.susurrus.susurrus.services.WifiDirectService;
import rocks.susurrus.susurrus.services.WifiDirectService.InstanceBinder;
import rocks.susurrus.susurrus.tasks.ClientAuthenticationTask;

public class MainActivity extends ActionBarActivity {
    private static final String LOG_TAG = "MainActivity";

    private boolean firstStart = false;

    /**
     * Networking
     */
    // intent-filter for reacting on network changes
    private final IntentFilter wifiIntentFilter = new IntentFilter();
    private WifiP2pManager wifiDirectManager;
    private WifiP2pManager.Channel wifiChannel;
    private WifiDirectBroadcastReceiver wifiReceiver;
    //private WifiDirectLocalService wifiDirectService;

    /**
     * Services/Threads/Handler
     */
    private Intent intentService;
    private WifiDirectService wifiDirectService;
    private boolean isWifiDirectServiceBound;
    private Handler authHandler;
    private AsyncTask authTask;


    /**
     * Views
     */
    private FloatingActionButton createButton;
    private Button discoverButton;
    private RelativeLayout emptyContainer;
    private RelativeLayout roomsContainer;
    private ListView roomsList;
    private RippleBackground rippleBackground;
    private MaterialDialog roomJoinDialog;
    private MaterialDialog roomPasswordDialog;

    /**
     * Data
     */
    private RoomAdapter roomAdapter;
    private RoomModel clickedRoom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupFirstStart();
        setupView();
        setupHandler();
        setupServices();

        Log.d(LOG_TAG, "MainActivity created.");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    /**
     * Is executed, when the Activity is destroyed.
     */
    protected void onDestroy() {
        super.onDestroy();

        Log.d(LOG_TAG, "ONDESTROY!");

        // destroy running AsyncTasks (needed for lower Android-versions)
        if(this.authTask != null) {
            Log.d(LOG_TAG, "Destroying: authTask [AsyncTask]");
            this.authTask.cancel(true);
        }
        if(this.isWifiDirectServiceBound) {
            Log.d(LOG_TAG, "Unbinding: wifiDirectService [Service]");
            unbindService(serviceConnection);
        }
        /*if(this.intentService != null) {
            Log.d(LOG_TAG, "Destroying: wifiDirectService [Service]");
            stopService(this.intentService);
        }*/

        Log.d(LOG_TAG, "MainActivity destroyed.");
    }

    /**
     * Checks if the user started the app for the first time. If true he is redirected to
     * the IntroActivity.
     */
    private void setupFirstStart() {
        // did the user start the app for the first time?
        if(firstStart) {
            // true, show intro
            Intent intentIntro = new Intent(this, IntroActivity.class);
            startActivity(intentIntro);
        }
    }

    /**
     * Setups all needed views, events, adapters, ...
     */
    private void setupView() {
        // get needed views
        createButton = (FloatingActionButton) findViewById(R.id.button_create);
        discoverButton = (Button) findViewById(R.id.action_discover);
        emptyContainer = (RelativeLayout) findViewById(R.id.main_empty_container);
        roomsContainer = (RelativeLayout) findViewById(R.id.main_rooms_container);
        roomsList = (ListView) findViewById(R.id.main_rooms_list);
        rippleBackground = (RippleBackground) findViewById(R.id.content);

        // set events
        createButton.setOnClickListener(createButtonListener);
        roomsList.setOnItemClickListener(roomsListListener);

        // create adapter to convert the array to views
        roomAdapter = new RoomAdapter(getApplicationContext(),
                R.layout.activity_main_room);
        // attach the adapter to a ListView
        roomsList.setAdapter(roomAdapter);

        // set empty view for our list
        roomsList.setEmptyView(emptyContainer);

        //roomAdapter.add(new RoomModel("Besitzer", "127.0.0.1", "Raum Name", "Freiheit", "img", true));
    }

    private void setupHandler() {
        // attach handler objects to the ui-thread
        this.authHandler = new Handler(Looper.getMainLooper()) {
            @Override
            /**
             * Is executed whenever the handler receives a new message from our Auth-AsyncTask.
             */
            public void handleMessage(Message inputMessage) {
                Log.d(LOG_TAG, "what: " + inputMessage.what);

                showRoomJoinFeedbackUpdate(inputMessage.what);
            }
        };
    }

    /**
     * Starts and binds all needed services.
     */
    private void setupServices() {
        // start and bind the wifiDirectService
        intentService = new Intent(this, WifiDirectService.class);
        startService(intentService);
        bindService(intentService, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * Shows a warning-dialog if the user is already connected to a Wifi-access-point.
     */
    private void showAccessPointWarning() {

        new MaterialDialog.Builder(this)
                .title(R.string.main_warn_wifi_title)
                .content(R.string.main_warn_wifi_content)
                .positiveText(R.string.main_warn_wifi_proceed)
                //.negativeText(R.string.main_warn_wifi_cancel)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
                        Log.d(LOG_TAG, "Forcing disconnect");

                        wifiManager.disconnect();

                        WifiDisconnector disconnector = new WifiDisconnector(wifiManager);
                        registerReceiver(disconnector, new IntentFilter(WifiManager.
                                SUPPLICANT_STATE_CHANGED_ACTION));
                    }
                })
                .show();

    }

    /**
     * Is called whenever an ActionButton from the ActionBar is clicked.
     * @param item Clicked MenuItem
     * @return True, after handling
     */
    public boolean handleActionButton(MenuItem item) {
        int actionButtonId = item.getItemId();

        // which action button was clicked?
        if(actionButtonId == R.id.action_discover) {
            // scan for rooms
            roomAdapter.clear();
            rippleBackground.startRippleAnimation();
            wifiDirectService.discoverLocalServices();
        }
        else if(actionButtonId == R.id.action_settings) {
            // open settings
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
        }

        return true;
    }

    public void updateRoomsUi() {
        rippleBackground.stopRippleAnimation();
    }


    /**
     * On click listener: createButton.
     * Opens a new chat room creation activity.
     */
    private View.OnClickListener createButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // open a new chat creation intent
            Intent intentChat = new Intent(getApplicationContext(), CreateActivity.class);
            startActivity(intentChat);
        }
    };

    /**
     * On item click listener: roomsList.
     * Opens a new chat modal, accordingly to the clicked item.
     */
    private AdapterView.OnItemClickListener roomsListListener = new AdapterView.
            OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // get clicked room
            clickedRoom = roomAdapter.getItem(position);

            // does the room need a password
            if(clickedRoom.hasEncryption()) {
                // show password dialog first
                showPasswordDialog(true);
            }
            else {
                showJoiningDialog();
            }
        }
    };

    /**
     * Shows a password-dialog that updates the password-attribute of the clickedRoom (RoomModel).
     *
     * Executed with "boolean = true":  Wait for password-input and start the "joining-workflow"
     *                                  by calling "showJoiningDialog" after onInput.
     *
     * Executed with "boolean = false": User entered a wrong password and needs to input a new
     *                                  one. Execute "startAuthentication" directly after onInput.
     *
     * @param firstAuthentication
     */
    private void showPasswordDialog(final boolean firstAuthentication) {

        // show a password dialog, before joining
        roomPasswordDialog = new MaterialDialog.Builder(MainActivity.this)
                .title(R.string.main_dialog_headline)
                .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)
                .input(R.string.main_dialog_password_hint, 0, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                        String enteredPassword = input.toString();

                        // valid password?
                        if (enteredPassword.isEmpty()) {
                            return;
                        } else {
                            // valid, set password
                            clickedRoom.setPassword(enteredPassword);

                            // first authentication try?
                            if (firstAuthentication) {
                                // first time, start joining normally
                                showJoiningDialog();
                            } else {
                                // not the first time, client must have entered a wrong password.
                                // Try authentication one more time
                                startAuthentication();
                            }
                        }
                    }
                })
                .negativeText(R.string.main_dialog_cancel)
                .show();

        // give user feedback, that he entered a wrong password
        if(firstAuthentication) {
            roomPasswordDialog.setContent(R.string.main_dialog_password_content);
        }
        else {
            roomPasswordDialog.setContent(R.string.main_dialog_password_wrong);
        }

    }

    /**
     * Starts the "joining-workflow" and gives visual feedback about the "joining-status"
     * Call this method only once.
     */
    private void showJoiningDialog() {

        // show a normal progress dialog
        roomJoinDialog = new MaterialDialog.Builder(MainActivity.this)
                .title(R.string.main_dialog_headline)
                .content(R.string.main_dialog_content)
                .progress(false, 100)
                .negativeText(R.string.main_dialog_cancel)
                .show();

        // try to establish a connection
        wifiDirectService.setupWifiDirectReceiver();
        wifiDirectService.connectToLocalService(clickedRoom, MainActivity.this);
    }

    /**
     * Updates the roomJoinDialog-MaterialDialog with the current connection/authentication-state.
     * @param authenticationState
     */
    public void showRoomJoinFeedbackUpdate(final int authenticationState) {

        if(roomJoinDialog == null) {
            return;
        }

        // delay ui-update to make messages readable
        Handler delayUi = new Handler();
        delayUi.postDelayed(new Runnable() {
            @Override
            public void run() {

                // which authentication state does the client have?
                switch(authenticationState) {
                    case WifiDirectService.GROUP_CONNECTED:
                        roomJoinDialog.setContent(R.string.main_dialog_group_connected);
                        roomJoinDialog.incrementProgress(25);

                        startAuthentication();

                        break;
                    case WifiDirectService.GROUP_NOT_CONNECTED:
                        roomJoinDialog.setContent(R.string.main_dialog_group_not_connected);
                        //@todo stop joining

                        break;
                    case ClientAuthenticationTask.SOCKET_CONNECTED:
                        roomJoinDialog.setContent(R.string.main_dialog_connected);
                        roomJoinDialog.incrementProgress(25);

                        break;
                    case ClientAuthenticationTask.SOCKET_PASSWORD_NEEDED:
                        //@todo not needed?!

                        break;
                    case ClientAuthenticationTask.SOCKET_PASSWORD_WRONG:
                        // wrong password, ask for new password and redo the authentication

                        showPasswordDialog(false);

                        break;
                    case ClientAuthenticationTask.SOCKET_AUTHENTICATED:
                        roomJoinDialog.setContent(R.string.main_dialog_authenticated);
                        roomJoinDialog.incrementProgress(25);

                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                roomJoinDialog.incrementProgress(25);

                                    // open the newly connected chat room
                                    Intent chatIntent = new Intent(MainActivity.this,
                                            ChatActivity.class);
                                    chatIntent.putExtra("ROOM_NAME", clickedRoom.getRoomName());

                                    startActivity(chatIntent);
                                }
                        }, 2000);

                        break;
                    case ClientAuthenticationTask.SOCKET_EXCEPTION:
                        roomJoinDialog.setContent(R.string.main_dialog_authenticated);
                        roomJoinDialog.incrementProgress(100);

                        break;
                }

            }
        }, 1000);
    }

    /**
     * Initiates and starts an authentication-request-task.
     * @return
     */
    public void startAuthentication() {
        Log.d(LOG_TAG, "startAuthentication");

        // create a new authentication model
        AuthModel authRequest = new AuthModel(this.clickedRoom.getRoomPassword(), "public");
        //@todo insert real publicKey

        // get an instance of the wifiDirectReceiver
        WifiDirectBroadcastReceiver wifiDirectReceiver = WifiDirectBroadcastReceiver.getInstance();

        // start authentication
        this.authTask = new ClientAuthenticationTask(
                this.authHandler,
                MainActivity.this,
                wifiDirectReceiver.getMasterAddress(),
                authRequest
        ).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    /**
     * Connection to the external wifiDirectService-process.
     */
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            InstanceBinder localBinder = (InstanceBinder) service;
            wifiDirectService = localBinder.getService();
            wifiDirectService.setMainActivity(MainActivity.this);
            wifiDirectService.setRoomAdapter(roomAdapter);
            isWifiDirectServiceBound = true;

            // user already connected?
            if(wifiDirectService.isAlreadyConnected()) {
                showAccessPointWarning();
                //@todo implement global wifi warning
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isWifiDirectServiceBound = false;
        }
    };
}
