package rocks.susurrus.susurrus.receivers;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import rocks.susurrus.susurrus.MainActivity;
import rocks.susurrus.susurrus.services.MasterService;
import rocks.susurrus.susurrus.services.WifiDirectService;

public class ShutdownReceiver extends BroadcastReceiver {
    static final String LOG_TAG = "ShutdownReceiver";

    /**
     * Constans
     */
    public static final String SHUTDOWN_BROADCAST =
            "rocks.susurrus.susurrus.receivers.ShutdownReceiver.SHUTDOWN";

    /**
     * Data
     */
    private NotificationManager notificationManager;

    /**
     * Class Constructor.
     * @param notificationManager Android NotificationManager
     */
    public ShutdownReceiver(NotificationManager notificationManager) {
        this.notificationManager = notificationManager;
    }

    @Override
    /**
     * Is called whenever a broadcast message is received by the Receiver.
     */
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if(action.equals(ShutdownReceiver.SHUTDOWN_BROADCAST)) {
            Log.d(LOG_TAG, "Closing app ...");

            // cancel the notification
            this.notificationManager.cancel(MasterService.NOTIFICATION_ID);

            // call the MainActivity which will finish the app
            Intent shutdownIntent = new Intent(context, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("EXIT", true);
            context.startActivity(shutdownIntent);
        }
    }
}
