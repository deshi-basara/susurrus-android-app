package rocks.susurrus.susurrus.chat;

import android.content.Intent;
import android.os.IBinder;
import android.app.Service;
import android.util.Log;

import rocks.susurrus.susurrus.network.WifiDirectBroadcastReceiver;

/**
 * Android service.
 * Used for executing a long-running AsyncTask in background even if the user leaves the app.
 * The executed AsyncTask listens for messages, sent via a socket-connection.
 */
public class ReceiverService extends Service {

    private static final String LOG_TAG = "ReceiverService";

    @Override
    /**
     * Used by clients, that want to bind with the service.
     * Method has to be implemented.
     */
    public IBinder onBind(Intent intent) {
        // we don't want to allow binding, that's why we return null
        return null;
    }

    @Override
    /**
     * Is called whenever an Activity requests the start of our service.
     */
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "Started ...");

        // start listening for messages send via socket using an AsyncTask


        //new ReceiveMessage().execute();
        WifiDirectBroadcastReceiver mReceiver = WifiDirectBroadcastReceiver.getInstance();

        //Start the AsyncTask for the server to receive messages
        /*if(mReceiver.isGroupeOwner() == WifiDirectBroadcastReceiver.IS_OWNER){
            // force serial task execution to improve stability

            Log.v(LOG_TAG, "Start the AsyncTask for the server to receive messages");
            new ReceiveMessageServer(getApplicationContext()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[])null);
        }
        else if(mReceiver.isGroupeOwner() == WifiDirectBroadcastReceiver.IS_CLIENT){
            // force serial task execution to improve stability

            Log.v(LOG_TAG, "Start the AsyncTask for the client to receive messages");
            new ReceiveMessageClient(getApplicationContext()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[])null);
        }*/

        Log.d(LOG_TAG, "... start listening for message from the server");

        return START_STICKY;
    }
}
