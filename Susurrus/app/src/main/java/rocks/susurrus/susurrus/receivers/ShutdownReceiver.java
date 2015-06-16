package rocks.susurrus.susurrus.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class ShutdownReceiver extends BroadcastReceiver {
    static final String LOG_TAG = "ShutdownReceiver";

    @Override
    /**
     * Is called whenever a broadcast message is received by the Receiver.
     */
    public void onReceive(Context context, Intent intent) {
        Log.d(LOG_TAG, "Closing app ...");

        System.exit(0);

        Log.d(LOG_TAG, "... closed");
    }
}
