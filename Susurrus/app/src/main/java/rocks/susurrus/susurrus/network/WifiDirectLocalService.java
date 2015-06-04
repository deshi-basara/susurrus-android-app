package rocks.susurrus.susurrus.network;

import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

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

    final HashMap<String, String> buddies = new HashMap<String, String>();

    public WifiDirectLocalService(WifiP2pManager m, WifiP2pManager.Channel c) {
        this.wifiDirectManager = m;
        this.wifiChannel = c;

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
        record.put("room_private", true);
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

    /**
     * Starts the discovery of local "susurrus"-services.
     */
    public void discoverLocalServices() {
        Log.d(LOG_TAG, "Start discovering rooms ...");

        wifiDirectManager.setDnsSdResponseListeners(wifiChannel, servListener, txtListener);

        // get an instance of the WifiP2P service request object
        WifiP2pDnsSdServiceRequest roomRequest = WifiP2pDnsSdServiceRequest.newInstance();

        // add a service discovery request
        wifiDirectManager.addServiceRequest(wifiChannel, roomRequest,
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
        wifiDirectManager.discoverServices(wifiChannel, new WifiP2pManager.ActionListener() {

                    @Override
                    public void onSuccess() {
                        // Success!
                        Log.d(LOG_TAG, "DiscoverServices successfully");
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

                    Log.d(LOG_TAG, "DnsSdTxtRecord available -" + txtRecordMap.toString());
                    buddies.put(srcDevice.deviceAddress, txtRecordMap.get("buddyname"));
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
