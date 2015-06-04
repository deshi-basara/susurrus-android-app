package rocks.susurrus.susurrus;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.skyfishjy.library.RippleBackground;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import rocks.susurrus.susurrus.adapters.RoomAdapter;
import rocks.susurrus.susurrus.models.RoomModel;
import rocks.susurrus.susurrus.network.WifiDirectBroadcastReceiver;
import rocks.susurrus.susurrus.network.WifiDirectLocalService;
import rocks.susurrus.susurrus.network.WifiDisconnector;


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
    private WifiDirectLocalService wifiDirectService;

    /**
     * Views
     */
    private FloatingActionButton createButton;
    private Button discoverButton;
    private RelativeLayout emptyContainer;
    private RelativeLayout roomsContainer;
    private ListView roomsList;
    private RippleBackground rippleBackground;

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
            startActivity(intentChat);
        }

        if(isAlreadyConnected()) {
            WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            Log.d(LOG_TAG, "Forcing disconnect");

            wifiManager.disconnect();

            WifiDisconnector disconnector = new WifiDisconnector(wifiManager);
            registerReceiver(disconnector, new IntentFilter(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION));
        }

        setView();
        setWifiDirect();
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


    /**
     * Initiates all needed intentFilter, manager, channels and listener for wifi-direct
     * usage.
     */
    private void setWifiDirect() {
        // set filter-actions, that will later be handled by the WiFiDirectBroadcastReceiver
        wifiIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        wifiIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        wifiIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        wifiIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        // get an instance of the WifiP2PManager
        wifiDirectManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        // register application with the WifiP2PManager
        wifiChannel = wifiDirectManager.initialize(this, getMainLooper(), null);
        // get an instance of the broadcast receiver and set needed data
        wifiReceiver = WifiDirectBroadcastReceiver.getInstance();
        wifiReceiver.setWifiDirectManager(wifiDirectManager);
        wifiReceiver.setWifiDirectChannel(wifiChannel);
        wifiReceiver.setActivity(this);

        // setup the wifi direct service
        wifiDirectService = new WifiDirectLocalService(wifiDirectManager, wifiChannel);
        wifiDirectService.discoverLocalServices();

        Log.d(LOG_TAG, "WifiDirect set");
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

        // create adapter to convert the array to views
        RoomAdapter roomAdapter = new RoomAdapter(getApplicationContext(),
                R.layout.activity_main_room);
        // attach the adapter to a ListView
        roomsList.setAdapter(roomAdapter);

        try {
            roomAdapter.add(new RoomModel("Besitzer", InetAddress.getByName("127.0.0.1"), "Raum Name", "Freiheit", true));
        } catch(UnknownHostException e) {
            e.printStackTrace();
        }
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
            rippleBackground.startRippleAnimation();
            wifiDirectService.discoverLocalServices();
        }

        return true;
    }

    /**
     * On click listener: createButton.
     * Opens a new chat room creation activity.
     */
    private View.OnClickListener createButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // open a new chat creation intent
            /*Intent intentChat = new Intent(getApplicationContext(), CreateActivity.class);
            startActivity(intentChat);*/
            wifiDirectService.setupLocalService();
        }
    };
}
