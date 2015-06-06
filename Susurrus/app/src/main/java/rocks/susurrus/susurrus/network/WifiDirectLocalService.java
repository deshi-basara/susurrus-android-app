package rocks.susurrus.susurrus.network;

import android.app.Activity;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

import rocks.susurrus.susurrus.CreateActivity;
import rocks.susurrus.susurrus.MainActivity;
import rocks.susurrus.susurrus.adapters.RoomAdapter;
import rocks.susurrus.susurrus.models.RoomModel;

/**
 * Created by simon on 04.06.15.
 */
public class WifiDirectLocalService {
    private final static String LOG_TAG = "WifiDirectLocalService";

    /**
     * Constants
     */
    public static final int SERVICE_PORT = 4040;
    private final String SERVICE_NAME = "_susurrus";
    private final String SERVICE_TYPE = "_presence._tcp";

    /**
     * Singleton
     */
    private static WifiDirectLocalService singleInstance;

    /**
     * Networking
     */
    private WifiP2pManager wifiDirectManager;
    private WifiP2pManager.Channel wifiChannel;

    /**
     * Ui
     */
    private MainActivity mainActivity;

    /**
     * Data
     */
    HashMap<String, Map> availableDevices = new HashMap<String, Map>();
    private RoomAdapter roomAdapter;

    /**
     * Class constructur.
     */
    protected WifiDirectLocalService() { super(); }

    /**
     * Maintains a static reference to the lone singleton instance and returns the reference from.
     * @return WifiDirectLocalService instance
     */
    public static WifiDirectLocalService getInstance() {
        // is there already an instance of the class?
        if(singleInstance == null) {
            // no instance, create one
            singleInstance = new WifiDirectLocalService();
        }

        return singleInstance;
    }

    public void setWifiDirectManager(WifiP2pManager m) {
        this.wifiDirectManager = m;
    }
    public void setWifiChannel(WifiP2pManager.Channel c) {
        this.wifiChannel = c;
    }
    public void setMainActivity(MainActivity a) {
        this.mainActivity = a;
    }
    public void setRoomAdapter(RoomAdapter r) {
        this.roomAdapter = r;
    }

    /**
     * Setups an own local "susurrus"-service (which represents a chat room).
     */
    public void setupLocalService(final CreateActivity feedbackActivity, Map roomData) {
        Log.d(LOG_TAG, "Start registering a new room service ...");

        // Service information. Pass it an instance name, service type
        // _protocol._transportlayer, and the map containing
        // information other devices will want once they connect to this one.
        WifiP2pDnsSdServiceInfo roomInfo =
                WifiP2pDnsSdServiceInfo.newInstance(SERVICE_NAME, SERVICE_TYPE, roomData);

        // Add the local service, sending the service info, network channel,
        // and listener that will be used to indicate success or failure of
        // the request.
        wifiDirectManager.addLocalService(wifiChannel, roomInfo, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(LOG_TAG, "... service created.");
                feedbackActivity.registerWifiRoomFeedback(false, 0);
            }

            @Override
            public void onFailure(int errorCode) {
                // command failed, check for P2P_UNSUPPORTED, ERROR, or BUSY
                if(errorCode == WifiP2pManager.P2P_UNSUPPORTED) {
                    Log.d(LOG_TAG, "... service error code: P2P_UNSUPPORTED");
                }
                else if(errorCode == WifiP2pManager.ERROR) {
                    Log.d(LOG_TAG, "... service error code: ERROR");
                }
                else if(errorCode == WifiP2pManager.BUSY) {
                    Log.d(LOG_TAG, "... service error code: BUSY");
                }

                feedbackActivity.registerWifiRoomFeedback(true, errorCode);
            }
        });
    }

    public void connectToLocalService(String serviceAddress) {
        WifiP2pConfig connectionConfig = new WifiP2pConfig();
        connectionConfig.deviceAddress = serviceAddress;
        connectionConfig.wps.setup = WpsInfo.PBC;

        wifiDirectManager.connect(wifiChannel, connectionConfig, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                // WiFiDirectBroadcastReceiver will notify us. Ignore for now.
                Log.d(LOG_TAG, "Connection to room established");
            }

            @Override
            public void onFailure(int reason) {
                Log.d(LOG_TAG, "Connection to room failed: " + reason);
            }
        });

    }


    public void setupLocalServiceDiscovery() {
        Log.d(LOG_TAG, "Setup discovering rooms.");

        wifiDirectManager.setDnsSdResponseListeners(wifiChannel, servListener, txtListener);

        // get an instance of the WifiP2P service request object
        WifiP2pDnsSdServiceRequest roomRequest = WifiP2pDnsSdServiceRequest.newInstance();

        // add a service discovery request
        wifiDirectManager.addServiceRequest(wifiChannel, roomRequest,
                new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        // Success!
                        Log.d(LOG_TAG, "... wifiDirectManager.addServiceRequest success ...");

                        discoverLocalServices();
                    }

                    @Override
                    public void onFailure(int code) {
                        // Command failed.  Check for P2P_UNSUPPORTED, ERROR, or BUSY
                        Log.d(LOG_TAG, "addServiceRequest error: " + code);
                    }
                }
        );
    }

    /**
     * Starts the discovery of local "susurrus"-services.
     */
    public void discoverLocalServices() {
        Log.d(LOG_TAG, "Start discovering rooms ...");

        // make the request
        wifiDirectManager.discoverServices(wifiChannel, new WifiP2pManager.ActionListener() {

                    @Override
                    public void onSuccess() {
                        // Success!
                        Log.d(LOG_TAG, "... wifiDirectManager.discoverServices success.");

                        // delay feedback execution
                        Handler updateHandler = new Handler();
                        updateHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mainActivity.updateRoomsUi();
                            }
                        }, 10000);
                    }

                    @Override
                    public void onFailure(int code) {
                        Log.d(LOG_TAG, "DiscoverServices error: " + code);
                        // Command failed.  Check for P2P_UNSUPPORTED, ERROR, or BUSY
                        if (code == WifiP2pManager.P2P_UNSUPPORTED) {
                            Log.d(LOG_TAG, "P2P isn't supported on this device.");
                        }
                    }
                }
        );
    }


    /**
     * On receive listener: wifiDirectManager.
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

                    Log.d(LOG_TAG, "DnsSdTxtRecord available: " + srcDevice.deviceAddress);
                    availableDevices.put(srcDevice.deviceAddress, txtRecordMap);
                }
            };

    /**
     * On receive listener: wifiDirectManager.
     * Receives the actual description and connection information of a room.
     * The previous code snippet implemented a Map object to pair a device address with the buddy name. The service response listener uses this to link the DNS record with the corresponding service information
     */
    private WifiP2pManager.DnsSdServiceResponseListener servListener = new WifiP2pManager
            .DnsSdServiceResponseListener() {
        @Override
        public void onDnsSdServiceAvailable(String instanceName, String registrationType,
                                            WifiP2pDevice resourceType) {

            Log.d(LOG_TAG, "OnDnsSdServiceAvailable: " + resourceType.deviceAddress);

            // update the device name with the human-friendly version from
            // the DnsTxtRecord, assuming one arrived.
            if(availableDevices.containsKey(resourceType.deviceAddress)) {
                Log.d(LOG_TAG, "Building new room ...");

                Map roomData = availableDevices.get(resourceType.deviceAddress);
                roomData.put("device", resourceType.deviceName);

                // fetch needed data
                String roomOwner = (String) roomData.get("user_name");
                String ownerAddr = resourceType.deviceAddress;
                String roomName = (String) roomData.get("room_name");
                String roomCategory = (String) roomData.get("room_category");
                String roomImage = (String) roomData.get("room_image");

                boolean roomEncrypted = false;
                if(roomData.get("room_private").equals("true")) {
                    roomEncrypted = true;
                }

                // create a new RoomModel for the discovered service.
                RoomModel newRoom = new RoomModel(roomOwner, ownerAddr, roomName, roomCategory,
                        roomImage, roomEncrypted);

                // add to the adapter
                roomAdapter.add(newRoom);
            }

            /*resourceType.deviceName = availableDevices
                    .containsKey(resourceType.deviceAddress) ? availableDevices
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
