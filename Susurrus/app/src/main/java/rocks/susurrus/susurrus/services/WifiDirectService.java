package rocks.susurrus.susurrus.services;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
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
import rocks.susurrus.susurrus.R;
import rocks.susurrus.susurrus.adapters.RoomAdapter;
import rocks.susurrus.susurrus.models.RoomModel;
import rocks.susurrus.susurrus.network.WifiDirectBroadcastReceiver;
import rocks.susurrus.susurrus.receivers.ShutdownReceiver;

/**
 * Created by simon on 04.06.15.
 */
public class WifiDirectService extends Service {
    final static String LOG_TAG = "WifiDirectService";

    /**
     * Constants
     */
    public static final int SERVICE_PORT = 4040;
    public static final int SERVICE_AUTH_PORT = 4041;
    public static final int GROUP_NOT_CONNECTED = -1;
    public static final int GROUP_CONNECTED = 0;
    public static final int GROUP_CREATING = 1;
    public static final int GROUP_ERROR = 2;
    public static final int GROUP_CREATED = 3;
    public static final int NOTIFICATION_ID = 001;
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
    private boolean isWifiDirectReceiverRegistered = false;
    private ShutdownReceiver shutdownReceiver;
    private boolean isShutdownReceiverRegistered = false;
    private WifiP2pDnsSdServiceInfo roomInfo;

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

        // setup shutdown broadcast-listener
        this.setupShutdownReceiver();

        // setup status icon
        this.showNotificationIcon();

        Log.d(LOG_TAG, "wifiDirectService created.");
    }

    @Override
    /**
     * Is executed when the Service is killed.
     */
    public void onDestroy() {
        super.onDestroy();

        // unregister WifiP2P-broadcast listener
        if(this.isWifiDirectReceiverRegistered) {
            Log.d(LOG_TAG, "Unregister: wifiDirectReceiver [Broadcast-Receiver]");
            unregisterReceiver(this.wifiDirectReceiver);
        }

        // unregister Shutdown-broadcast listener
        if(this.isShutdownReceiverRegistered) {
            Log.d(LOG_TAG, "Unregister: ShutdownReceiver [Broadcast-Receiver]");
            unregisterReceiver(this.shutdownReceiver);
        }

        Log.d(LOG_TAG, "wifiDirectService destroyed.");
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
     * Opens a status-indicator-notification when the service is started.
     */
    private void showNotificationIcon() {

        // set MainActivity as notification action, which redirects to the last opened Activity
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        notificationIntent.setAction(Intent.ACTION_MAIN);
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        notificationIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        // set a "closing-broadcast" as notification close action, which signals the broadcast-
        // listener to close the whole app
        Intent closingIntent = new Intent(ShutdownReceiver.SHUTDOWN_BROADCAST);
        PendingIntent closePendingIntent =
                PendingIntent.getBroadcast(
                        this,
                        0,
                        closingIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        // set notification content and open it
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification notification  = new Notification.Builder(this)
                .setContentTitle(getString(R.string.notify_running_title))
                .setContentText(getString(R.string.notify_running_content))
                .setSmallIcon(R.drawable.entering_heaven_alive_24)
                .addAction(R.drawable.cancel_24, getString(R.string.notify_running_stop),
                        closePendingIntent)
                .setContentIntent(resultPendingIntent)
                .setOngoing(true)
                .build();
        notificationManager.notify(this.NOTIFICATION_ID, notification);

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

        // setup the wifi direct service
        this.setupLocalServiceDiscovery();

        // search for available rooms
        //this.wifiDirectService.discoverLocalServices();
    }

    public void setupWifiDirectReceiver() {

        if(!this.isWifiDirectReceiverRegistered) {
            // get an instance of the broadcast receiver and set needed data
            this.wifiDirectReceiver = WifiDirectBroadcastReceiver.getInstance();
            this.wifiDirectReceiver.setWifiDirectManager(this.wifiDirectManager);
            this.wifiDirectReceiver.setWifiDirectChannel(this.wifiDirectChannel);
            this.wifiDirectReceiver.setMainActivity(this.mainActivity);

            // start listening for WifiP2P-broadcasts
            registerReceiver(this.wifiDirectReceiver, this.wifiDirectIntentFilter);

            this.isWifiDirectReceiverRegistered = true;
        }
    }

    public void setupShutdownReceiver() {

        if(!this.isShutdownReceiverRegistered) {
            IntentFilter shutdownIntentFilter = new IntentFilter();
            shutdownIntentFilter.addAction(ShutdownReceiver.SHUTDOWN_BROADCAST);

            this.shutdownReceiver = new ShutdownReceiver((NotificationManager)
                    getSystemService(Context.NOTIFICATION_SERVICE));

            registerReceiver(this.shutdownReceiver, shutdownIntentFilter);
        }
    }

    /**
     * Setups listeners that are called, when a new "susurrus"-service is found.
     */
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
     * Setups an own local "susurrus"-service (which represents a chat room).
     */
    public void setupLocalService(final CreateActivity feedbackActivity, final RoomModel roomModel) {
        feedbackActivity.registerWifiDialogUpdate(GROUP_CREATING);

        wifiDirectManager.createGroup(this.wifiDirectChannel, createGroupListener);

        // get the creator's username and add it

        // Service information. Pass it an instance name, service type
        // _protocol._transportlayer, and the map containing
        // information other devices will want once they connect to this one.
        this.roomInfo =
                WifiP2pDnsSdServiceInfo.newInstance(SERVICE_NAME, SERVICE_TYPE, roomModel.toHashMap());

        // Add the local service, sending the service info, network channel,
        // and listener that will be used to indicate success or failure of
        // the request.
        wifiDirectManager.addLocalService(wifiDirectChannel, this.roomInfo,
                new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Log.d(LOG_TAG, "... service created.");

                        // send feedback
                        feedbackActivity.registerWifiDialogUpdate(GROUP_CREATED);

                        discoverLocalServices();
                    }

                    @Override
                    public void onFailure(int errorCode) {
                        // command failed, check for P2P_UNSUPPORTED, ERROR, or BUSY
                        if (errorCode == WifiP2pManager.P2P_UNSUPPORTED) {
                            Log.d(LOG_TAG, "... service error code: P2P_UNSUPPORTED");
                        } else if (errorCode == WifiP2pManager.ERROR) {
                            Log.d(LOG_TAG, "... service error code: ERROR");
                        } else if (errorCode == WifiP2pManager.BUSY) {
                            Log.d(LOG_TAG, "... service error code: BUSY");
                        }

                        feedbackActivity.registerWifiDialogUpdate(GROUP_ERROR);
                    }
                });
    }

    /**
     * Removes a previously created "susurrus"-service, for example when a chat-room is closed
     * by the MasterNode.
     */
    public void removeLocalService() {
        wifiDirectManager.removeLocalService(wifiDirectChannel, this.roomInfo,
                new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Log.d(LOG_TAG, "Service removed.");
                    }

                    @Override
                    public void onFailure(int errorCode) {

                        Log.d(LOG_TAG, "Service remove error: " + errorCode);
                    }
                });
    }

    /**
     * Establishes a connection to a local "susurrus"-service.
     * @param roomModel
     * @param feedbackActivity
     */
    public void connectToLocalService(final RoomModel roomModel, final MainActivity feedbackActivity) {
        WifiP2pConfig connectionConfig = new WifiP2pConfig();
        connectionConfig.deviceAddress = roomModel.getOwnerAddr();
        // user connects, don't make him the owner
        connectionConfig.groupOwnerIntent = 0;
        // connectionConfig.wps.setup = WpsInfo.INVALID;
        connectionConfig.wps.setup = WpsInfo.PBC;

        wifiDirectManager.connect(wifiDirectChannel, connectionConfig, new WifiP2pManager.
                ActionListener() {

            @Override
            public void onSuccess() {
                //feedbackActivity.showRoomJoinFeedbackUpdate(GROUP_CONNECTED);
            }

            @Override
            public void onFailure(int reason) {
                Log.d(LOG_TAG, "Connection to room failed: " + reason);

                feedbackActivity.showRoomJoinFeedbackUpdate(GROUP_NOT_CONNECTED);
            }
        });
    }

    /**
     * Starts the manual discovery of local "susurrus"-services.
     */
    public void discoverLocalServices() {
        Log.d(LOG_TAG, "Discover rooms ...");

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
                        if (errorCode == WifiP2pManager.P2P_UNSUPPORTED) {
                            Log.d(LOG_TAG, "P2P isn't supported on this device.");
                        } else if (errorCode == WifiP2pManager.ERROR) {
                            Log.d(LOG_TAG, "Error on this device.");
                        } else if (errorCode == WifiP2pManager.BUSY) {
                            Log.d(LOG_TAG, "Busy on this device.");
                        }
                    }
                }
        );
    }

    /**
     * Checks if the user is already connected to a wifi access point.
     * @return True, if connected.
     */
    public boolean isAlreadyConnected() {
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
                String roomName = (String) roomData.get("room_name");
                String roomCategory = (String) roomData.get("room_category");
                String roomImage = (String) roomData.get("room_image");

                boolean roomEncrypted = false;
                if(roomData.get("room_private").equals("true")) {
                    roomEncrypted = true;
                }

                // create a new RoomModel for the discovered service.
                RoomModel newRoom = new RoomModel(roomOwner, roomName, roomCategory,
                        roomImage, roomEncrypted);

                // set owner address, for connection
                newRoom.setOwnerAddr(resourceType.deviceAddress);

                // add to the adapter
                roomAdapter.add(newRoom);
            }

            Log.d(LOG_TAG, "onBonjourServiceAvailable " + instanceName);
        }
    };

    private WifiP2pManager.ActionListener createGroupListener = new WifiP2pManager.ActionListener() {
        @Override
        public void onSuccess() {
            Log.d(LOG_TAG, "group created!");
        }

        @Override
        public void onFailure(int reason) {
            // The reason for failure could be one of P2P_UNSUPPORTED, ERROR or BUSY
            Log.d(LOG_TAG, "group error: " + reason);
        }
    };
}
