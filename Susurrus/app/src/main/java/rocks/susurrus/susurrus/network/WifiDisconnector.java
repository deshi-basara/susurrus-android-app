package rocks.susurrus.susurrus.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiManager;
import android.util.Log;

/**
 * Receives broadcasts from the wifi supplicant client on connection changes.
 * Forces disconnects if the smartphone tries to receonnect to an access point.
 */
public class WifiDisconnector extends BroadcastReceiver {
    private static final String LOG_TAG = "BroadcastReceiver";

    private WifiManager wifiManager;

    public WifiDisconnector(WifiManager m) {
        this.wifiManager = m;

        Log.d(LOG_TAG, "WifiDisconnector established");
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String newWifiState = intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE).toString();

        Log.d(LOG_TAG, newWifiState);
        Log.d(LOG_TAG, "equals: " + newWifiState.equals("SCANNING"));

        if(newWifiState.equals("SCANNING")) {
            Log.d(LOG_TAG, "SCANNING: true");

            wifiManager.disconnect();
        }
    }
}
