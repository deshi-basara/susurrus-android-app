package rocks.susurrus.susurrus.network;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.util.Log;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import rocks.susurrus.susurrus.MainActivity;

/**
 * A BroadcastReceiver that notifies of important Wi-Fi p2p events.
 * Allow only one instance of the class by using the Singleton pattern.
 */
public class WifiDirectBroadcastReceiver extends BroadcastReceiver {
    private static final String LOG_TAG = "WifiBroadcastReceiver";

    // singleton instance
    private static WifiDirectBroadcastReceiver singleInstance;

    /**
     * Attributes
     */
    private WifiP2pManager wifiManager;
    private WifiP2pManager.Channel wifiChannel;
    private MainActivity mainActivity;

    /**
     * Data
     */
    private List peers = new ArrayList();
    private boolean isMaster = true;
    private InetAddress masterAddress;


    /**
     * Class constructor.
     */
    protected WifiDirectBroadcastReceiver() {
        super();
    }

    /**
     * Maintains a static reference to the lone singleton instance and returns the reference from.
     * @return WifiDirectBroadcastReceiver instance
     */
    public static WifiDirectBroadcastReceiver getInstance() {
        // is there already an instance of the class?
        if(singleInstance == null) {
            // no instance, create one
            singleInstance = new WifiDirectBroadcastReceiver();
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
            /*Log.d(LOG_TAG, "WIFI_P2P_PEERS_CHANGED_ACTION");

            // request available peers from the wifi p2p manager. This is an
            // asynchronous call and the calling activity is notified with a
            // callback on PeerListListener.onPeersAvailable()
            if(this.wifiManager != null) {
                Log.d(LOG_TAG, "mManager requesting peers ...");
                this.wifiManager.requestPeers(this.wifiChannel, peerListListener);
            }
            else {
                Log.d(LOG_TAG, "mManager is empty");
            }*/
        }

        // Respond to new connection or disconnections
        else if(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            Log.d(LOG_TAG, "WIFI_P2P_CONNECTION_CHANGED_ACTION: New connection.");

            // broadcast received before having a valid wifiManager instance?
            if(this.wifiManager == null) {
                return;
            }
            else {
                // get network information
                NetworkInfo networkInfo = (NetworkInfo) intent
                        .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

                Log.d(LOG_TAG, "networkInfo: " + networkInfo.isConnected());

                if(networkInfo.isConnected()){
                    this.wifiManager.requestConnectionInfo(wifiChannel, new WifiP2pManager.ConnectionInfoListener() {

                        @Override
                        public void onConnectionInfoAvailable(WifiP2pInfo info) {
                            masterAddress = info.groupOwnerAddress;

                            Log.d(LOG_TAG, "Group owner address: " + masterAddress);
                            Log.d(LOG_TAG, "Group formed: " + info.groupFormed);
                            Log.d(LOG_TAG, "Group owner: " + info.isGroupOwner);

                            // is our user the chat-room owner?
                            if(info.groupFormed && info.isGroupOwner) {
                                // is owner, mark the user as owner create a server instance
                                isMaster = true;
                            }
                            // not the chat-room owner, has to be a client instead
                            else if(info.groupFormed) {
                                isMaster = false;

                                // start authentication, after we have received the address
                                // of the authentication-socket
                                mainActivity.startAuthentication();
                            }

                            Log.d(LOG_TAG, "IsServer: " + isMaster);
                        }
                    });
                }

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



    /**
     * Setter methods.
     */
    public void setWifiDirectManager(WifiP2pManager manager) {
        this.wifiManager = manager;
    }
    public void setWifiDirectChannel(WifiP2pManager.Channel channel) {
        this.wifiChannel = channel;
    }
    public void setMainActivity(MainActivity activity) {
        this.mainActivity = activity;
    }

    public InetAddress getMasterAddress() {
        return this.masterAddress;
    }

    public boolean isMaster() {
        return this.isMaster;
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
            /*Log.d(LOG_TAG, "onPeersAvailable");

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
            }*/
        }
    };


}