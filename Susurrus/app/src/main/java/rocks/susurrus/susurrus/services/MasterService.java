package rocks.susurrus.susurrus.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import rocks.susurrus.susurrus.ChatActivity;
import rocks.susurrus.susurrus.R;
import rocks.susurrus.susurrus.models.RoomModel;
import rocks.susurrus.susurrus.threads.ServerAuthenticationThread;
import rocks.susurrus.susurrus.threads.ServerReceiveThread;

public class MasterService extends Service {
    final static String LOG_TAG = "MessengerService";

    /**
     * Networking
     */
    private ServerAuthenticationThread authRunnable;
    private ServerReceiveThread receiveRunnable;
    private Thread authThread;
    private Thread chatThread;
    private boolean isAuthenticated = false;
    private boolean hasChat = false;

    /**
     * Handler
     */
    private Handler chatHandler;

    /**
     * Binder
     */
    private final IBinder binder = new InstanceBinder();
    public class InstanceBinder extends Binder {
        public MasterService getService() {
            return MasterService.this;
        }
    }

    /**
     * Data
     */
    private RoomModel roomData;

    @Override
    /**
     * Binder-Interface that clients use to communicate with the service.
     */
    public IBinder onBind(Intent intent) {
        return this.binder;
    }

    @Override
    /**
     * Is executed the first time the Service is started.
     */
    public int onStartCommand(Intent intent, int flags, int startId) {
        // get roomData via intent extras
        roomData = (RoomModel) intent.getSerializableExtra("ROOM_MODEL");

        Log.d(LOG_TAG, "MasterService created.");

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(this.isAuthenticated) {
            Log.d(LOG_TAG, "Stopping: authThread [Thread]");

            this.authRunnable.terminate();
            this.isAuthenticated = false;
        }
        if(this.hasChat) {
            Log.d(LOG_TAG, "Stopping: chatThread [Thread]");

            this.receiveRunnable.terminate();
            this.hasChat = false;
        }

        Log.d(LOG_TAG, "MasterService destroyed.");
    }

    /**
     * Starts thread that runs the authentication socket.
     */
    private void initiateAuthentication() {

        if(!isAuthenticated) {
            // create a thread, for authenticating with the service/room
            this.authRunnable = new ServerAuthenticationThread(
                    this.roomData);

            this.authRunnable.start();

            //this.authThread = new Thread(this.authRunnable);
            //this.authThread.start();

            this.isAuthenticated = true;
        }

    }

    /**
     * Starts thread that runs the messaging socket.
     */
    private void initiateChat() {

        if(!this.hasChat) {
            // create a thread, for messaging with the service/room
            this.receiveRunnable = new ServerReceiveThread(this.chatHandler);

            this.receiveRunnable.start();

            //this.chatThread = new Thread(this.receiveRunnable);
            //this.chatThread.start();

            this.hasChat = true;
        }
    }


    /**
     * Getter/Setter
     */
    public void setChatHandler(Handler handler) {
        this.chatHandler = handler;
    }

    public void startChatThread() {
        this.initiateChat();
    }

    public void startAuthThread() {
        this.initiateAuthentication();
    }
}
