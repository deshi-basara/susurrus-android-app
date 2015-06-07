package rocks.susurrus.susurrus;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBarActivity;
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
import rocks.susurrus.susurrus.models.RoomModel;
import rocks.susurrus.susurrus.network.WifiDirectBroadcastReceiver;
import rocks.susurrus.susurrus.network.WifiDisconnector;
import rocks.susurrus.susurrus.services.WifiDirectService;
import rocks.susurrus.susurrus.services.WifiDirectService.InstanceBinder;

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

    private WifiDirectService wifiDirectService;
    private boolean isWifiDirectServiceBound;

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

    /**
     * Data
     */
    private RoomAdapter roomAdapter;
    private RoomModel clickedRoom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // did the user start the app for the first time?
        if(firstStart) {
            // true, show intro
            Intent intentIntro = new Intent(this, IntroActivity.class);
            startActivity(intentIntro);
        }
        else if(false) {
            Intent intentChat = new Intent(this, ChatActivity.class);
            intentChat.putExtra("ROOM_NAME", "Simon@testing");
            startActivity(intentChat);
        }

        if(isAlreadyConnected()) {
            WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            Log.d(LOG_TAG, "Forcing disconnect");

            wifiManager.disconnect();

            WifiDisconnector disconnector = new WifiDisconnector(wifiManager);
            registerReceiver(disconnector, new IntentFilter(WifiManager.
                    SUPPLICANT_STATE_CHANGED_ACTION));
        }

        setView();

        // start and bind the wifiDirectService
        Intent intentService = new Intent(this, WifiDirectService.class);
        startService(intentService);
        bindService(intentService, serviceConnection, Context.BIND_AUTO_CREATE);
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

    /* register the broadcast receiver with the intent values to be matched */
    @Override
    protected void onResume() {
        super.onResume();
        // register our broadcast receiver, which is called when an intentFilter matches
        //registerReceiver(wifiReceiver, wifiIntentFilter);
    }
    /* unregister the broadcast receiver */
    @Override
    protected void onPause() {
        super.onPause();
        //unregisterReceiver(wifiReceiver);
    }

    /**
     * Checks if the user is already connected to a wifi access point.
     * @return True, if connected.
     */
    private boolean isAlreadyConnected() {
        // get an instance of the wifi-manager and information of the current access point
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        SupplicantState supplicantState = wifiInfo.getSupplicantState();

        // does the supplicant wifi-cli have a completed-state?
        if(SupplicantState.COMPLETED.equals(supplicantState)) {
            // completed state, connected to access point
            Log.d(LOG_TAG, "Already connected to an access point");

            return true;
        }
        else {
            return false;
        }
    }

    private void setView() {
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

    public void updateRooms() {

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
            showRoomJoinFeedback();

            // get room deviceAddress and try to establish a connection
            clickedRoom = roomAdapter.getItem(position);
            wifiDirectService.connectToLocalService(clickedRoom, MainActivity.this);

            Log.d(LOG_TAG, "Connection to room established: " + clickedRoom.getRoomName());
        }
    };

    public void showRoomJoinFeedback() {
        roomJoinDialog = new MaterialDialog.Builder(MainActivity.this)
                .title(R.string.main_dialog_headline)
                .content(R.string.main_dialog_content)
                .progress(true, 0)
                .negativeText(R.string.main_dialog_cancel)
                .show();
    }

    public void showRoomJoinFeedbackUpdate() {
        roomJoinDialog.setContent("... beigetreten");

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // open the newly connected chat room
                Intent chatIntent = new Intent(MainActivity.this, ChatActivity.class);
                chatIntent.putExtra("ROOM_NAME", clickedRoom.getRoomName());

                startActivity(chatIntent);
            }
        }, 5000);

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
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isWifiDirectServiceBound = false;
        }
    };
}
