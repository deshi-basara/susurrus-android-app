package rocks.susurrus.susurrus.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import rocks.susurrus.susurrus.ChatActivity;

/**
 * A BroadcastReceiver that notifies of important Wi-Fi p2p events.
 */
public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

    private static final String LOG_TAG = "WifiBroadcastReceiver";

    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private ChatActivity mActivity;

    private List peers = new ArrayList();

    /**
     * Constructor.
     * Sets
     * @param manager
     * @param channel
     * @param activity
     */
    public WiFiDirectBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel,
                                       ChatActivity activity) {
        super();
        this.mManager = manager;
        this.mChannel = channel;
        this.mActivity = activity;
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
            if(mManager != null) {
                Log.d(LOG_TAG, "mManager requesting peers ...");
                mManager.requestPeers(mChannel, peerListListener);
            }
            else {
                Log.d(LOG_TAG, "mManager is empty");
            }
        }

        // Respond to new connection or disconnections
        else if(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            //Log.d(LOG_TAG, "WIFI_P2P_CONNECTION_CHANGED_ACTION");
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