package rocks.susurrus.susurrus.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import rocks.susurrus.susurrus.CreateActivity;
import rocks.susurrus.susurrus.MainActivity;
import rocks.susurrus.susurrus.adapters.RoomAdapter;
import rocks.susurrus.susurrus.models.RoomModel;
import rocks.susurrus.susurrus.network.WifiDirectBroadcastReceiver;

/**
 * Created by simon on 04.06.15.
 */
public class WifiDirectService extends Service {
    final static String LOG_TAG = "WifiDirectService";

    /**
     * Constants
     */
    public static final int SERVICE_PORT = 4040;
    private final String SERVICE_NAME = "_susurrus";
    private final String SERVICE_TYPE = "_presence._tcp";

    /**
     * Binder
     */
    private final IBinder binder = new InstanceBinder();
    public class InstanceBinder extends Binder {
        public WifiDirectService getService() {
            return WifiDirectService.this;
        }
    }

    /**
     * Networking
     */
    private final IntentFilter wifiDirectIntentFilter = new IntentFilter();
    private WifiP2pManager wifiDirectManager;
    private WifiP2pManager.Channel wifiDirectChannel;
    private WifiDirectBroadcastReceiver wifiDirectReceiver;

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
     * Setters
     */
    public void setWifiDirectManager(WifiP2pManager m) {
        this.wifiDirectManager = m;
    }
    public void setWifiDirectChannel(WifiP2pManager.Channel c) {
        this.wifiDirectChannel = c;
    }
    public void setMainActivity(MainActivity a) {
        this.mainActivity = a;
    }
    public void setRoomAdapter(RoomAdapter r) {
        this.roomAdapter = r;
    }

    @Override
    /**
     * Binder-Interface that clients use to communicate with the service.
     */
    public IBinder onBind(Intent arg0) {
        return this.binder;
    }

    @Override
    /**
     * Is executed the first time the Service is started.
     */
    public void onCreate() {
        // setup all needed networking interfaces
        this.initiateNetworking();

        // start listening for WifiP2P-broadcasts
        registerReceiver(this.wifiDirectReceiver, this.wifiDirectIntentFilter);
    }

    @Override
    /**
     * Is executed when the Service is killed.
     */
    public void onDestroy() {
        super.onDestroy();

        // unregister WifiP2P-broadcast listener
        unregisterReceiver(this.wifiDirectReceiver);
    }

    @Override
    /**
     * The system calls this method when another component, such as an activity, requests that
     * the service be started, by calling startService().
     */
    public int onStartCommand(Intent intent, int flags, int startId) {

        return Service.START_NOT_STICKY;
    }

    /**
     * Initiates all needed intentFilters, managers, channels and listeners for wifi-direct
     * usage.
     */
    private void initiateNetworking() {
        // set filter-actions, that will later be handled by the WiFiDirectBroadcastReceiver
        this.wifiDirectIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        this.wifiDirectIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        this.wifiDirectIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        this.wifiDirectIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        // get an instance of the WifiP2PManager
        this.wifiDirectManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        // register application with the WifiP2PManager
        this.wifiDirectChannel = this.wifiDirectManager.initialize(this, getMainLooper(), null);

        // get an instance of the broadcast receiver and set needed data
        this.wifiDirectReceiver = WifiDirectBroadcastReceiver.getInstance();
        this.wifiDirectReceiver.setWifiDirectManager(this.wifiDirectManager);
        this.wifiDirectReceiver.setWifiDirectChannel(this.wifiDirectChannel);
        //wifiDirectReceiver.setActivity(this);

        // setup the wifi direct service
        this.setupLocalServiceDiscovery();

        // search for available rooms
        //this.wifiDirectService.discoverLocalServices();

        Log.d(this.LOG_TAG, "Networking initiated.");
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
        wifiDirectManager.addLocalService(wifiDirectChannel, roomInfo, new WifiP2pManager.ActionListener() {
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

    public void connectToLocalService(String serviceAddress, final MainActivity feedbackActivity) {
        WifiP2pConfig connectionConfig = new WifiP2pConfig();
        connectionConfig.deviceAddress = serviceAddress;
        // user connects, don't make him the owner
        connectionConfig.groupOwnerIntent = 0;
        // connectionConfig.wps.setup = WpsInfo.INVALID;
        connectionConfig.wps.setup = WpsInfo.PBC;

        wifiDirectManager.connect(wifiDirectChannel, connectionConfig, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                // WiFiDirectBroadcastReceiver will notify us. Ignore for now.
                Log.d(LOG_TAG, "Connection to room established");

                feedbackActivity.showRoomJoinFeedbackUpdate();
            }

            @Override
            public void onFailure(int reason) {
                Log.d(LOG_TAG, "Connection to room failed: " + reason);
            }
        });

    }


    public void setupLocalServiceDiscovery() {
        Log.d(LOG_TAG, "Setup discovering rooms.");

        wifiDirectManager.setDnsSdResponseListeners(wifiDirectChannel, servListener, txtListener);

        // get an instance of the WifiP2P service request object
        WifiP2pDnsSdServiceRequest roomRequest = WifiP2pDnsSdServiceRequest.newInstance();

        // add a service discovery request
        wifiDirectManager.addServiceRequest(wifiDirectChannel, roomRequest,
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
        wifiDirectManager.discoverServices(wifiDirectChannel, new WifiP2pManager.ActionListener() {

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
                    public void onFailure(int errorCode) {
                        // Command failed.  Check for P2P_UNSUPPORTED, ERROR, or BUSY
                        if(errorCode == WifiP2pManager.P2P_UNSUPPORTED) {
                            Log.d(LOG_TAG, "P2P isn't supported on this device.");
                        }
                        else if(errorCode == WifiP2pManager.ERROR) {
                            Log.d(LOG_TAG, "Error on this device.");
                        }
                        else if(errorCode == WifiP2pManager.BUSY) {
                            Log.d(LOG_TAG, "Busy on this device.");
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
