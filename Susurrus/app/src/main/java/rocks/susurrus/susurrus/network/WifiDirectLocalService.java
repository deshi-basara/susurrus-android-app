package rocks.susurrus.susurrus.network;

import android.app.Activity;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.Handler;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

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
    private final int SERVICE_PORT = 4040;
    private final String SERVICE_NAME = "_susurrus";
    private final String SERVICE_TYPE = "_presence._tcp";

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

    public WifiDirectLocalService(WifiP2pManager m, WifiP2pManager.Channel c, MainActivity a,
                                  RoomAdapter r) {
        this.wifiDirectManager = m;
        this.wifiChannel = c;
        this.mainActivity = a;
        this.roomAdapter = r;

        Log.d(LOG_TAG, "wifiDirectService initiated.");
    }

    /**
     * Setups an own local "susurrus"-service (which represents a chat room).
     */
    public void setupLocalService() {

        //  Create a string map containing information about the room.
        Map record = new HashMap();
        record.put("port", String.valueOf(SERVICE_PORT));
        record.put("user_name", "John Doe" + (int) (Math.random() * 1000));
        record.put("room_name", "Susurrus Test");
        record.put("room_category", "Testing");
        record.put("room_private", "true");
        record.put("room_image", "img-url");
        record.put("available", "visible");

        // Service information. Pass it an instance name, service type
        // _protocol._transportlayer, and the map containing
        // information other devices will want once they connect to this one.
        WifiP2pDnsSdServiceInfo roomInfo =
                WifiP2pDnsSdServiceInfo.newInstance(SERVICE_NAME, SERVICE_TYPE, record);

        // Add the local service, sending the service info, network channel,
        // and listener that will be used to indicate success or failure of
        // the request.
        wifiDirectManager.addLocalService(wifiChannel, roomInfo, new WifiP2pManager.ActionListener() {
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

    boolean listenerSet = false;

    /**
     * Starts the discovery of local "susurrus"-services.
     */
    public void discoverLocalServices() {
        Log.d(LOG_TAG, "Start discovering rooms ...");

        if(!listenerSet) {
            wifiDirectManager.setDnsSdResponseListeners(wifiChannel, servListener, txtListener);
            listenerSet = true;
            Log.d(LOG_TAG, "ListenerSet");
        }

        // get an instance of the WifiP2P service request object
        WifiP2pDnsSdServiceRequest roomRequest = WifiP2pDnsSdServiceRequest.newInstance();

        // add a service discovery request
        wifiDirectManager.addServiceRequest(wifiChannel, roomRequest,
                new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        // Success!
                        Log.d(LOG_TAG, "... wifiDirectManager.addServiceRequest success ...");
                    }

                    @Override
                    public void onFailure(int code) {
                        // Command failed.  Check for P2P_UNSUPPORTED, ERROR, or BUSY
                        Log.d(LOG_TAG, "addServiceRequest error: " + code);
                    }
                }
        );

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

                    Log.d(LOG_TAG, "DnsSdTxtRecord available: " + txtRecordMap.toString());
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

            Log.d(LOG_TAG, "OnDnsSdServiceAvailable");
            Log.d(LOG_TAG, "resourceType: " + resourceType.toString());

            // update the device name with the human-friendly version from
            // the DnsTxtRecord, assuming one arrived.
            if(availableDevices.containsKey(resourceType.deviceAddress)) {

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
