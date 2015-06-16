package rocks.susurrus.susurrus.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;

/**
 * Receives broadcasts from the wifi supplicant client on connection changes.
 * Forces disconnects if the smartphone tries to receonnect to an access point.
 */
public class DisconnectorReceiver extends BroadcastReceiver {
    static final String LOG_TAG = "DisconnectorReceiver";

    /**
     * Constants
     */
    public static final String SUPPLICANT_SCANNING = "SCANNING";

    /**
     * Data
     */
    private WifiManager wifiManager;

    /**
     * Class Constructor.
     * @param wifiManager Instance of the Android wifiManager.
     */
    public DisconnectorReceiver(WifiManager wifiManager) {
        this.wifiManager = wifiManager;
    }

    @Override
    /**
     * Is executed whenever a wifi-supplicant-event was broadcasted.
     */
    public void onReceive(Context context, Intent intent) {
        String wifiState = intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE).toString();

        // supplicant started scanning again?
        if(wifiState.equals(this.SUPPLICANT_SCANNING)) {
            // force disconnect
            wifiManager.disconnect();
        }
    }
}
