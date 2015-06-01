package rocks.susurrus.susurrus;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

import rocks.susurrus.susurrus.network.WiFiDirectBroadcastReceiver;


public class MainActivity extends ActionBarActivity {
    private static final String LOG_TAG = "MainActivity";

    private boolean firstStart = false;

    /**
     * Networking
     */
    // intent-filter for reacting on network changes
    private final IntentFilter wifiIntentFilter = new IntentFilter();
    private WifiP2pManager wifiManager;
    private WifiP2pManager.Channel wifiChannel;
    private WiFiDirectBroadcastReceiver wifiReceiver;

    /**
     * Views
     */
    private FloatingActionButton createButton;
    private Button discoverButton;



    final HashMap<String, String> buddies = new HashMap<String, String>();

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

        setView();
        setWifi();
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

    private void setWifi() {
        // set filter-actions, that will later be handled by the WiFiDirectBroadcastReceiver
        wifiIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        wifiIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        wifiIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        wifiIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        // get an instance of the WifiP2PManager
        wifiManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        // register application with the WifiP2PManager
        wifiChannel = wifiManager.initialize(this, getMainLooper(), null);
        // get an instance of the broadcast receiver and set needed data
        wifiReceiver = WiFiDirectBroadcastReceiver.getInstance();
        wifiReceiver.setWifiManager(wifiManager);
        wifiReceiver.setWifiChannel(wifiChannel);
        wifiReceiver.setActivity(this);
    }

    private void setView() {
        // get needed views
        createButton = (FloatingActionButton) findViewById(R.id.button_create);
        discoverButton = (Button) findViewById(R.id.action_discover);

        // set events
        createButton.setOnClickListener(createButtonListener);
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
            discoverRooms();
        }

        return true;
    }

    /**
     * Starts the discovery of chat rooms.
     */
    private void discoverRooms() {
        Log.d(LOG_TAG, "Start discovering rooms ...");

        wifiManager.setDnsSdResponseListeners(wifiChannel, servListener, txtListener);

        // get an instance of the WifiP2P service request object
        WifiP2pDnsSdServiceRequest roomRequest = WifiP2pDnsSdServiceRequest.newInstance();

        // add a service discovery request
        wifiManager.addServiceRequest(wifiChannel, roomRequest,
                new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    // Success!
                    Log.d(LOG_TAG, "addServiceRequest success");
                }

                @Override
                public void onFailure(int code) {
                    // Command failed.  Check for P2P_UNSUPPORTED, ERROR, or BUSY
                    Log.d(LOG_TAG, "addServiceRequest error: " + code);
                }
            }
        );

        // make the request
        wifiManager.discoverServices(wifiChannel, new WifiP2pManager.ActionListener() {

                    @Override
                    public void onSuccess() {
                        // Success!
                        Log.d(LOG_TAG, "Request made successfully");
                    }

                    @Override
                    public void onFailure(int code) {
                        // Command failed.  Check for P2P_UNSUPPORTED, ERROR, or BUSY
                        if (code == WifiP2pManager.P2P_UNSUPPORTED) {
                            Log.d(LOG_TAG, "P2P isn't supported on this device.");
                        }
                    }
                }
        );
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
     * On receive listener: wifiManager.
     * Callback invocation when Bonjour TXT record is available for a room.
     */
    private WifiP2pManager.DnsSdTxtRecordListener txtListener =
            new WifiP2pManager.DnsSdTxtRecordListener() {

                @Override
                /* Callback includes:
                 * fullDomain: full domain name: e.g "printer._ipp._tcp.local."
                 * record: TXT record dta as a map of key/value pairs.
                 * device: The device running the advertised service.
                 */
                public void onDnsSdTxtRecordAvailable(String fullDomainName, Map<String,
                        String> txtRecordMap, WifiP2pDevice srcDevice) {

                        Log.d(LOG_TAG, "DnsSdTxtRecord available -" + txtRecordMap.toString());
                        buddies.put(srcDevice.deviceAddress, txtRecordMap.get("buddyname"));
                }
    };

    /**
     * On receive listener: wifiManager.
     * Receives the actual description and connection information of a room.
     * The previous code snippet implemented a Map object to pair a device address with the buddy name. The service response listener uses this to link the DNS record with the corresponding service information
     */
    private WifiP2pManager.DnsSdServiceResponseListener servListener = new WifiP2pManager
            .DnsSdServiceResponseListener() {
        @Override
        public void onDnsSdServiceAvailable(String instanceName, String registrationType,
                                            WifiP2pDevice resourceType) {

            // Update the device name with the human-friendly version from
            // the DnsTxtRecord, assuming one arrived.
            resourceType.deviceName = buddies
                    .containsKey(resourceType.deviceAddress) ? buddies
                    .get(resourceType.deviceAddress) : resourceType.deviceName;

            // Add to the custom adapter defined specifically for showing
            // wifi devices.
            /*WiFiDirectServicesList fragment = (WiFiDirectServicesList) getFragmentManager()
                    .findFragmentById(R.id.frag_peerlist);
            WiFiDevicesAdapter adapter = ((WiFiDevicesAdapter) fragment
                    .getListAdapter());

            adapter.add(resourceType);
            adapter.notifyDataSetChanged();*/
            Log.d(LOG_TAG, "onBonjourServiceAvailable " + instanceName);
        }
    };



}
