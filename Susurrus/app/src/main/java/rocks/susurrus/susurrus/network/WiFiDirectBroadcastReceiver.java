package rocks.susurrus.susurrus.network;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import rocks.susurrus.susurrus.ChatActivity;

/**
 * A BroadcastReceiver that notifies of important Wi-Fi p2p events.
 * Allow only one instance of the class by using the Singleton pattern.
 */
public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {
    private static final String LOG_TAG = "WifiBroadcastReceiver";

    // singleton instance
    private static WiFiDirectBroadcastReceiver singleInstance;


    private WifiP2pManager wifiManager;
    private WifiP2pManager.Channel wifiChannel;
    private Activity baseActivity;

    private List peers = new ArrayList();

    /**
     * Class constructor.
     */
    protected WiFiDirectBroadcastReceiver() {
        super();
    }

    /**
     * Maintains a static reference to the lone singleton instance and returns the reference from.
     * @return WifiDirectBroadcastReceiver instance
     */
    public static WiFiDirectBroadcastReceiver getInstance() {
        // is there already an instance of the class?
        if(singleInstance == null) {
            // no instance, create one
            singleInstance = new WiFiDirectBroadcastReceiver();
        }

        return singleInstance;
    }

    @Override
    /**
     * Receives the current WifiP2pManager-state from the intentFilter in our ChatActivity.
     * Handles WifiP2pManager-actions, according to their state
     */
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        // Respond to network changes
        if(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            // Check to see if Wi-Fi is enabled and notify appropriate activity
            boolean enabled = isWifiP2pEnabled(intent);
        }

        // Respond to peer-list changes
        else if(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            // Call WifiP2pManager.requestPeers() to get a list of current peers
            Log.d(LOG_TAG, "WIFI_P2P_PEERS_CHANGED_ACTION");

            // request available peers from the wifi p2p manager. This is an
            // asynchronous call and the calling activity is notified with a
            // callback on PeerListListener.onPeersAvailable()
            if(this.wifiManager != null) {
                Log.d(LOG_TAG, "mManager requesting peers ...");
                this.wifiManager.requestPeers(this.wifiChannel, peerListListener);
            }
            else {
                Log.d(LOG_TAG, "mManager is empty");
            }
        }

        // Respond to new connection or disconnections
        else if(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            Log.d(LOG_TAG, "WIFI_P2P_CONNECTION_CHANGED_ACTION");

            // broadcast received before having a valid wifiManager instance?
            if(this.wifiManager == null) {
                return;
            }
            else {
                // get network information
                NetworkInfo networkInfo = (NetworkInfo) intent
                        .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);


            }


        }

        // Respond to this device's wifi state changing
        else if(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            //Log.d(LOG_TAG, "WIFI_P2P_THIS_DEVICE_CHANGED_ACTION");
        }
    }

    /**
     * Helper method.
     * Checks if the device supports wifi p2p.
     * @param receivedIntent Passed intent via intentFilter.
     * @return True, if enabled
     */
    private boolean isWifiP2pEnabled(Intent receivedIntent) {
        // get wifi-state and validate it
        int state = receivedIntent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);

        if(state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
            // wifi p2p enabled
            Log.d(LOG_TAG, "wifi p2p supported");
            return true;
        }
        else {
            // wifi p2p disabled
            Log.d(LOG_TAG, "wifi p2p is not supported");
            return false;
        }
    }

    public void createNewRoom(Map roomData) {
        // Service information. Pass it an instance name, service type
        // _protocol._transportlayer, and the map containing
        // information other devices will want once they connect to this one.
        WifiP2pDnsSdServiceInfo roomServiceInfo =
                WifiP2pDnsSdServiceInfo.newInstance("_test", "_presence._tcp", roomData);

        // Add the local service, sending the service info, network channel,
        // and listener that will be used to indicate success or failure of
        // the request.
        wifiManager.addLocalService(wifiChannel, roomServiceInfo, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                // Command successful! Code isn't necessarily needed here,
                // Unless you want to update the UI or add logging statements.
                Log.d(LOG_TAG, "Room created");
            }

            @Override
            public void onFailure(int arg0) {
                // Command failed.  Check for P2P_UNSUPPORTED, ERROR, or BUSY
                Log.d(LOG_TAG, "Error code: " + arg0);
            }
        });
    }

    /**
     * Setter methods.
     */
    public void setWifiDirectManager(WifiP2pManager manager) {
        this.wifiManager = manager;
    }
    public void setWifiDirectChannel(WifiP2pManager.Channel channel) {
        this.wifiChannel = channel;
    }
    public void setActivity(Activity activity) {
        this.baseActivity = activity;
    }



    /**
     * Custom listener called by mManager.requestPeers(...).
     * Empties our old peer-List and inserts all newly found devices.
     */
    private WifiP2pManager.PeerListListener peerListListener =
            new WifiP2pManager.PeerListListener() {

        @Override
        /**
         * Async callback.
         * Is called after peer scan was completed.
         */
        public void onPeersAvailable(WifiP2pDeviceList peerList) {
            Log.d(LOG_TAG, "onPeersAvailable");

            // Out with the old, in with the new.
            peers.clear();
            peers.addAll(peerList.getDeviceList());

            if (peers.size() == 0) {
                Log.d(LOG_TAG, "No devices found");
                return;
            }
            else {
                Log.d(LOG_TAG, "Devices found: " + peers.size());

                for(int i = 0; i < peers.size(); i++) {
                    WifiP2pDevice currentDevice = (WifiP2pDevice) peers.get(i);
                    String deviceName = currentDevice.deviceName;

                    Log.d(LOG_TAG, "DeviceName: " + deviceName);
                }
            }
        }
    };
}